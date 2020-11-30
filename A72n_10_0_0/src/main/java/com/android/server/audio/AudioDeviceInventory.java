package com.android.server.audio;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.AudioDevicePort;
import android.media.AudioFormat;
import android.media.AudioPort;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.IAudioRoutesObserver;
import android.os.Binder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.EventLogTags;
import com.android.server.audio.AudioDeviceInventory;
import com.android.server.audio.AudioEventLogger;
import com.android.server.audio.AudioServiceEvents;
import com.android.server.audio.BtHelper;
import com.android.server.pm.PackageManagerService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class AudioDeviceInventory {
    private static final String CONNECT_INTENT_KEY_ADDRESS = "address";
    private static final String CONNECT_INTENT_KEY_DEVICE_CLASS = "class";
    private static final String CONNECT_INTENT_KEY_HAS_CAPTURE = "hasCapture";
    private static final String CONNECT_INTENT_KEY_HAS_MIDI = "hasMIDI";
    private static final String CONNECT_INTENT_KEY_HAS_PLAYBACK = "hasPlayback";
    private static final String CONNECT_INTENT_KEY_PORT_NAME = "portName";
    private static final String CONNECT_INTENT_KEY_STATE = "state";
    private static final int DEVICE_OVERRIDE_A2DP_ROUTE_ON_PLUG = 67264524;
    private static final String TAG = "AS.AudioDeviceInventory";
    private int mBecomingNoisyIntentDevices = 201490316;
    private final ArrayMap<String, DeviceInfo> mConnectedDevices = new ArrayMap<>();
    final AudioRoutesInfo mCurAudioRoutes = new AudioRoutesInfo();
    private final AudioDeviceBroker mDeviceBroker;
    private String mDockAddress;
    final RemoteCallbackList<IAudioRoutesObserver> mRoutesObservers = new RemoteCallbackList<>();

    AudioDeviceInventory(AudioDeviceBroker broker) {
        this.mDeviceBroker = broker;
    }

    /* access modifiers changed from: private */
    public static class DeviceInfo {
        final String mDeviceAddress;
        int mDeviceCodecFormat;
        final String mDeviceName;
        final int mDeviceType;

        DeviceInfo(int deviceType, String deviceName, String deviceAddress, int deviceCodecFormat) {
            this.mDeviceType = deviceType;
            this.mDeviceName = deviceName;
            this.mDeviceAddress = deviceAddress;
            this.mDeviceCodecFormat = deviceCodecFormat;
        }

        public String toString() {
            return "[DeviceInfo: type:0x" + Integer.toHexString(this.mDeviceType) + " name:" + this.mDeviceName + " addr:" + this.mDeviceAddress + " codec: " + Integer.toHexString(this.mDeviceCodecFormat) + "]";
        }

        /* access modifiers changed from: private */
        public static String makeDeviceListKey(int device, String deviceAddress) {
            return "0x" + Integer.toHexString(device) + ":" + deviceAddress;
        }
    }

    /* access modifiers changed from: package-private */
    public class WiredDeviceConnectionState {
        public final String mAddress;
        public final String mCaller;
        public final String mName;
        public final int mState;
        public final int mType;

        WiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
            this.mType = type;
            this.mState = state;
            this.mAddress = address;
            this.mName = name;
            this.mCaller = caller;
        }
    }

    /* access modifiers changed from: package-private */
    public void onRestoreDevices() {
        synchronized (this.mConnectedDevices) {
            for (int i = 0; i < this.mConnectedDevices.size(); i++) {
                DeviceInfo di = this.mConnectedDevices.valueAt(i);
                AudioSystem.setDeviceConnectionState(di.mDeviceType, 1, di.mDeviceAddress, di.mDeviceName, di.mDeviceCodecFormat);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"AudioDeviceBroker.mDeviceStateLock"})
    public void onSetA2dpSinkConnectionState(BtHelper.BluetoothA2dpDeviceInfo btInfo, int state) {
        String address;
        BluetoothDevice btDevice = btInfo.getBtDevice();
        int a2dpVolume = btInfo.getVolume();
        if (AudioService.DEBUG_DEVICES) {
            Log.d(TAG, "onSetA2dpSinkConnectionState btDevice=" + btDevice + " state=" + state + " is dock=" + btDevice.isBluetoothDock() + " vol=" + a2dpVolume);
        }
        String address2 = btDevice.getAddress();
        if (!BluetoothAdapter.checkBluetoothAddress(address2)) {
            address = "";
        } else {
            address = address2;
        }
        AudioEventLogger audioEventLogger = AudioService.sDeviceLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("A2DP sink connected: device addr=" + address + " state=" + state + " vol=" + a2dpVolume));
        int a2dpCodec = btInfo.getCodec();
        synchronized (this.mConnectedDevices) {
            DeviceInfo di = this.mConnectedDevices.get(DeviceInfo.makeDeviceListKey(128, btDevice.getAddress()));
            boolean isConnected = di != null;
            if (!isConnected || state == 2) {
                if (!isConnected && state == 2) {
                    if (btDevice.isBluetoothDock()) {
                        this.mDeviceBroker.cancelA2dpDockTimeout();
                        this.mDockAddress = address;
                    } else if (this.mDeviceBroker.hasScheduledA2dpDockTimeout() && this.mDockAddress != null) {
                        this.mDeviceBroker.cancelA2dpDockTimeout();
                        makeA2dpDeviceUnavailableNow(this.mDockAddress, 0);
                    }
                    if (a2dpVolume != -1) {
                        boolean isAvrcpAbsVolSupported = this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
                        int mFinalA2dpVolume = this.mDeviceBroker.getA2dpVolume(false, a2dpVolume * 10);
                        Log.d(TAG, "20H onSetA2dpSinkConnectionState a2dpVolume:" + a2dpVolume + " final index:" + mFinalA2dpVolume + " support:" + isAvrcpAbsVolSupported);
                        this.mDeviceBroker.postSetVolumeIndexOnDevice(3, mFinalA2dpVolume, 128, "onSetA2dpSinkConnectionState");
                        if (isAvrcpAbsVolSupported) {
                            Log.d(TAG, "20H post");
                            this.mDeviceBroker.postSetAvrcpAbsoluteVolumeIndex(mFinalA2dpVolume / 10);
                        }
                    }
                    makeA2dpDeviceAvailable(address, BtHelper.getName(btDevice), "onSetA2dpSinkConnectionState", a2dpCodec);
                }
            } else if (!btDevice.isBluetoothDock()) {
                makeA2dpDeviceUnavailableNow(address, di.mDeviceCodecFormat);
            } else if (state == 0) {
                lambda$disconnectA2dp$1$AudioDeviceInventory(address, EventLogTags.JOB_DEFERRED_EXECUTION);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSetA2dpSourceConnectionState(BtHelper.BluetoothA2dpDeviceInfo btInfo, int state) {
        BluetoothDevice btDevice = btInfo.getBtDevice();
        if (AudioService.DEBUG_DEVICES) {
            Log.d(TAG, "onSetA2dpSourceConnectionState btDevice=" + btDevice + " state=" + state);
        }
        String address = btDevice.getAddress();
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            address = "";
        }
        synchronized (this.mConnectedDevices) {
            boolean isConnected = this.mConnectedDevices.get(DeviceInfo.makeDeviceListKey(-2147352576, address)) != null;
            if (isConnected && state != 2) {
                lambda$disconnectA2dpSink$3$AudioDeviceInventory(address);
            } else if (!isConnected && state == 2) {
                makeA2dpSrcAvailable(address);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSetHearingAidConnectionState(BluetoothDevice btDevice, int state, int streamType) {
        String address = btDevice.getAddress();
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            address = "";
        }
        AudioEventLogger audioEventLogger = AudioService.sDeviceLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("onSetHearingAidConnectionState addr=" + address));
        if (AudioService.DEBUG_DEVICES) {
            Log.d(TAG, "onSetHearingAidConnectionState btDevice=" + address + " state=" + state);
        }
        synchronized (this.mConnectedDevices) {
            boolean isConnected = this.mConnectedDevices.get(DeviceInfo.makeDeviceListKey(134217728, btDevice.getAddress())) != null;
            if (isConnected && state != 2) {
                lambda$disconnectHearingAid$5$AudioDeviceInventory(address);
            } else if (!isConnected && state == 2) {
                makeHearingAidDeviceAvailable(address, BtHelper.getName(btDevice), streamType, "onSetHearingAidConnectionState");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"AudioDeviceBroker.mDeviceStateLock"})
    public void onBluetoothA2dpActiveDeviceChange(BtHelper.BluetoothA2dpDeviceInfo btInfo, int event) {
        String address;
        BluetoothDevice btDevice = btInfo.getBtDevice();
        if (btDevice != null) {
            if (AudioService.DEBUG_DEVICES) {
                Log.d(TAG, "onBluetoothA2dpActiveDeviceChange btDevice=" + btDevice + " event=" + BtHelper.a2dpDeviceEventToString(event));
            }
            int a2dpVolume = btInfo.getVolume();
            int a2dpCodec = btInfo.getCodec();
            String address2 = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address2)) {
                address = "";
            } else {
                address = address2;
            }
            AudioEventLogger audioEventLogger = AudioService.sDeviceLogger;
            audioEventLogger.log(new AudioEventLogger.StringEvent("onBluetoothA2dpActiveDeviceChange addr=" + address + " event=" + BtHelper.a2dpDeviceEventToString(event)));
            synchronized (this.mConnectedDevices) {
                if (this.mDeviceBroker.hasScheduledA2dpSinkConnectionState(btDevice)) {
                    AudioService.sDeviceLogger.log(new AudioEventLogger.StringEvent("A2dp config change ignored"));
                    return;
                }
                String key = DeviceInfo.makeDeviceListKey(128, address);
                DeviceInfo di = this.mConnectedDevices.get(key);
                if (di == null) {
                    Log.e(TAG, "invalid null DeviceInfo in onBluetoothA2dpActiveDeviceChange");
                    return;
                }
                if (event == 1) {
                    if (a2dpVolume != -1) {
                        boolean isAvrcpAbsVolSupported = this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
                        int mFinalA2dpVolume = this.mDeviceBroker.getA2dpVolume(true, a2dpVolume * 10);
                        Log.d(TAG, "20H onSetA2dpSinkConnectionState a2dpVolume:" + a2dpVolume + " final index:" + mFinalA2dpVolume + " support:" + isAvrcpAbsVolSupported);
                        this.mDeviceBroker.postSetVolumeIndexOnDevice(3, mFinalA2dpVolume, 128, "onSetA2dpSinkConnectionState");
                        if (isAvrcpAbsVolSupported) {
                            Log.d(TAG, "20H post");
                            this.mDeviceBroker.postSetAvrcpAbsoluteVolumeIndex(mFinalA2dpVolume / 10);
                        }
                    }
                } else if (event == 0 && di.mDeviceCodecFormat != a2dpCodec) {
                    di.mDeviceCodecFormat = a2dpCodec;
                    this.mConnectedDevices.replace(key, di);
                }
                if (AudioSystem.handleDeviceConfigChange(128, address, BtHelper.getName(btDevice), a2dpCodec) != 0) {
                    setBluetoothA2dpDeviceConnectionState(btDevice, 0, 2, false, this.mDeviceBroker.getDeviceForStream(3), -1);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onMakeA2dpDeviceUnavailableNow(String address, int a2dpCodec) {
        synchronized (this.mConnectedDevices) {
            makeA2dpDeviceUnavailableNow(address, a2dpCodec);
        }
    }

    /* access modifiers changed from: package-private */
    public void onReportNewRoutes() {
        AudioRoutesInfo routes;
        int n = this.mRoutesObservers.beginBroadcast();
        if (n > 0) {
            synchronized (this.mCurAudioRoutes) {
                routes = new AudioRoutesInfo(this.mCurAudioRoutes);
            }
            while (n > 0) {
                n--;
                try {
                    this.mRoutesObservers.getBroadcastItem(n).dispatchAudioRoutesChanged(routes);
                } catch (RemoteException e) {
                }
            }
        }
        this.mRoutesObservers.finishBroadcast();
        this.mDeviceBroker.postObserveDevicesForAllStreams();
    }

    /* access modifiers changed from: package-private */
    public void onSetWiredDeviceConnectionState(WiredDeviceConnectionState wdcs) {
        AudioService.sDeviceLogger.log(new AudioServiceEvents.WiredDevConnectEvent(wdcs));
        synchronized (this.mConnectedDevices) {
            boolean z = true;
            if (wdcs.mState == 0 && (wdcs.mType & DEVICE_OVERRIDE_A2DP_ROUTE_ON_PLUG) != 0) {
                this.mDeviceBroker.setBluetoothA2dpOnInt(true, "onSetWiredDeviceConnectionState state DISCONNECTED");
            }
            if (wdcs.mState != 1) {
                z = false;
            }
            if (handleDeviceConnection(z, wdcs.mType, wdcs.mAddress, wdcs.mName)) {
                if (wdcs.mState != 0) {
                    if ((wdcs.mType & DEVICE_OVERRIDE_A2DP_ROUTE_ON_PLUG) != 0) {
                        this.mDeviceBroker.setBluetoothA2dpOnInt(false, "onSetWiredDeviceConnectionState state not DISCONNECTED");
                    }
                    this.mDeviceBroker.checkMusicActive(wdcs.mType, wdcs.mCaller);
                }
                if (wdcs.mType == 1024) {
                    this.mDeviceBroker.checkVolumeCecOnHdmiConnection(wdcs.mState, wdcs.mCaller);
                }
                sendDeviceConnectionIntent(wdcs.mType, wdcs.mState, wdcs.mAddress, wdcs.mName);
                updateAudioRoutes(wdcs.mType, wdcs.mState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onToggleHdmi() {
        synchronized (this.mConnectedDevices) {
            if (this.mConnectedDevices.get(DeviceInfo.makeDeviceListKey(1024, "")) == null) {
                Log.e(TAG, "invalid null DeviceInfo in onToggleHdmi");
                return;
            }
            setWiredDeviceConnectionState(1024, 0, "", "", PackageManagerService.PLATFORM_PACKAGE_NAME);
            setWiredDeviceConnectionState(1024, 1, "", "", PackageManagerService.PLATFORM_PACKAGE_NAME);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean handleDeviceConnection(boolean connect, int device, String address, String deviceName) {
        String mDeviceInString;
        if (AudioService.DEBUG_DEVICES) {
            Slog.i(TAG, "handleDeviceConnection(" + connect + " dev:" + Integer.toHexString(device) + " address:" + address + " name:" + deviceName + ")");
            if ((device & Integer.MIN_VALUE) == Integer.MIN_VALUE) {
                mDeviceInString = AudioSystem.getInputDeviceName(device);
            } else {
                mDeviceInString = AudioSystem.getOutputDeviceName(device);
            }
            Log.i(TAG, "handleDeviceConnection(" + connect + " dev:" + Integer.toHexString(device) + "[" + mDeviceInString + "] address:" + address + " name:" + deviceName + ")");
        }
        synchronized (this.mConnectedDevices) {
            String deviceKey = DeviceInfo.makeDeviceListKey(device, address);
            if (AudioService.DEBUG_DEVICES) {
                Slog.i(TAG, "deviceKey:" + deviceKey);
            }
            DeviceInfo di = this.mConnectedDevices.get(deviceKey);
            boolean isConnected = di != null;
            if (AudioService.DEBUG_DEVICES) {
                Slog.i(TAG, "deviceInfo:" + di + " is(already)Connected:" + isConnected);
            }
            if (connect && !isConnected) {
                int res = AudioSystem.setDeviceConnectionState(device, 1, address, deviceName, 0);
                if (res != 0) {
                    Slog.e(TAG, "not connecting device 0x" + Integer.toHexString(device) + " due to command error " + res);
                    if (device == -2147483640) {
                        disconnectSco();
                    }
                    return false;
                }
                this.mConnectedDevices.put(deviceKey, new DeviceInfo(device, deviceName, address, 0));
                this.mDeviceBroker.postAccessoryPlugMediaUnmute(device);
                return true;
            } else if (connect || !isConnected) {
                Log.w(TAG, "handleDeviceConnection() failed, deviceKey=" + deviceKey + ", deviceSpec=" + di + ", connect=" + connect);
                return false;
            } else {
                AudioSystem.setDeviceConnectionState(device, 0, address, deviceName, 0);
                this.mConnectedDevices.remove(deviceKey);
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectA2dp() {
        synchronized (this.mConnectedDevices) {
            ArraySet<String> toRemove = new ArraySet<>();
            this.mConnectedDevices.values().forEach(new Consumer(toRemove) {
                /* class com.android.server.audio.$$Lambda$AudioDeviceInventory$y5XThSW6MLia8Z0qpQToEJpUJk0 */
                private final /* synthetic */ ArraySet f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AudioDeviceInventory.lambda$disconnectA2dp$0(this.f$0, (AudioDeviceInventory.DeviceInfo) obj);
                }
            });
            if (toRemove.size() > 0) {
                toRemove.stream().forEach(new Consumer(checkSendBecomingNoisyIntentInt(128, 0, 0)) {
                    /* class com.android.server.audio.$$Lambda$AudioDeviceInventory$qhWWEgLKYMNfyt5ffemHAtlRkpw */
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        AudioDeviceInventory.this.lambda$disconnectA2dp$1$AudioDeviceInventory(this.f$1, (String) obj);
                    }
                });
            }
        }
    }

    static /* synthetic */ void lambda$disconnectA2dp$0(ArraySet toRemove, DeviceInfo deviceInfo) {
        if (deviceInfo.mDeviceType == 128) {
            toRemove.add(deviceInfo.mDeviceAddress);
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectA2dpSink() {
        synchronized (this.mConnectedDevices) {
            ArraySet<String> toRemove = new ArraySet<>();
            this.mConnectedDevices.values().forEach(new Consumer(toRemove) {
                /* class com.android.server.audio.$$Lambda$AudioDeviceInventory$0fz2EAO4sH309I_0WQlshYm7ShE */
                private final /* synthetic */ ArraySet f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AudioDeviceInventory.lambda$disconnectA2dpSink$2(this.f$0, (AudioDeviceInventory.DeviceInfo) obj);
                }
            });
            toRemove.stream().forEach(new Consumer() {
                /* class com.android.server.audio.$$Lambda$AudioDeviceInventory$xRa5RyFQAe2dGU7YDh18NalwMMg */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AudioDeviceInventory.this.lambda$disconnectA2dpSink$3$AudioDeviceInventory((String) obj);
                }
            });
        }
    }

    static /* synthetic */ void lambda$disconnectA2dpSink$2(ArraySet toRemove, DeviceInfo deviceInfo) {
        if (deviceInfo.mDeviceType == -2147352576) {
            toRemove.add(deviceInfo.mDeviceAddress);
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectHearingAid() {
        synchronized (this.mConnectedDevices) {
            ArraySet<String> toRemove = new ArraySet<>();
            this.mConnectedDevices.values().forEach(new Consumer(toRemove) {
                /* class com.android.server.audio.$$Lambda$AudioDeviceInventory$nQz4ldQjburNlVucAV7ieYoic28 */
                private final /* synthetic */ ArraySet f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AudioDeviceInventory.lambda$disconnectHearingAid$4(this.f$0, (AudioDeviceInventory.DeviceInfo) obj);
                }
            });
            if (toRemove.size() > 0) {
                checkSendBecomingNoisyIntentInt(134217728, 0, 0);
                toRemove.stream().forEach(new Consumer() {
                    /* class com.android.server.audio.$$Lambda$AudioDeviceInventory$9yZUrl4jHdQ7A5G79yQVDbYVSI */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        AudioDeviceInventory.this.lambda$disconnectHearingAid$5$AudioDeviceInventory((String) obj);
                    }
                });
            }
        }
    }

    static /* synthetic */ void lambda$disconnectHearingAid$4(ArraySet toRemove, DeviceInfo deviceInfo) {
        if (deviceInfo.mDeviceType == 134217728) {
            toRemove.add(deviceInfo.mDeviceAddress);
        }
    }

    /* access modifiers changed from: package-private */
    public int checkSendBecomingNoisyIntent(int device, int state, int musicDevice) {
        int checkSendBecomingNoisyIntentInt;
        synchronized (this.mConnectedDevices) {
            checkSendBecomingNoisyIntentInt = checkSendBecomingNoisyIntentInt(device, state, musicDevice);
        }
        return checkSendBecomingNoisyIntentInt;
    }

    /* access modifiers changed from: package-private */
    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        AudioRoutesInfo routes;
        synchronized (this.mCurAudioRoutes) {
            routes = new AudioRoutesInfo(this.mCurAudioRoutes);
            this.mRoutesObservers.register(observer);
        }
        return routes;
    }

    /* access modifiers changed from: package-private */
    public AudioRoutesInfo getCurAudioRoutes() {
        return this.mCurAudioRoutes;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"AudioDeviceBroker.mDeviceStateLock"})
    public void setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state, int profile, boolean suppressNoisyIntent, int musicDevice, int a2dpVolume) {
        int delay;
        if (profile == 2 || profile == 11) {
            synchronized (this.mConnectedDevices) {
                if (profile != 2 || suppressNoisyIntent) {
                    delay = 0;
                } else {
                    delay = checkSendBecomingNoisyIntentInt(128, state == 2 ? 1 : 0, musicDevice);
                    if (delay > 0) {
                        delay = 500;
                    }
                }
                if (state == 2) {
                    Log.d(TAG, "20H setBluetoothA2dpDeviceConnectionState checkMusicActive");
                    this.mDeviceBroker.checkMusicActive(128, TAG);
                }
                int a2dpCodec = this.mDeviceBroker.getA2dpCodec(device);
                if (AudioService.DEBUG_DEVICES) {
                    Log.i(TAG, "setBluetoothA2dpDeviceConnectionState device: " + device + " state: " + state + " delay(ms): " + delay + "codec:" + a2dpCodec + " suppressNoisyIntent: " + suppressNoisyIntent);
                }
                BtHelper.BluetoothA2dpDeviceInfo a2dpDeviceInfo = new BtHelper.BluetoothA2dpDeviceInfo(device, a2dpVolume, a2dpCodec);
                if (profile != 2) {
                    this.mDeviceBroker.postA2dpSourceConnection(state, a2dpDeviceInfo, delay);
                } else if (delay == 0) {
                    onSetA2dpSinkConnectionState(a2dpDeviceInfo, state);
                } else {
                    this.mDeviceBroker.postA2dpSinkConnection(state, a2dpDeviceInfo, delay);
                }
            }
            return;
        }
        throw new IllegalArgumentException("invalid profile " + profile);
    }

    /* access modifiers changed from: package-private */
    public int setWiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
        int delay;
        synchronized (this.mConnectedDevices) {
            delay = checkSendBecomingNoisyIntentInt(type, state, 0);
            this.mDeviceBroker.postSetWiredDeviceConnectionState(new WiredDeviceConnectionState(type, state, address, name, caller), delay);
        }
        return delay;
    }

    /* access modifiers changed from: package-private */
    public int setBluetoothHearingAidDeviceConnectionState(BluetoothDevice device, int state, boolean suppressNoisyIntent, int musicDevice) {
        int intState;
        synchronized (this.mConnectedDevices) {
            intState = 0;
            if (!suppressNoisyIntent) {
                if (state == 2) {
                    intState = 1;
                }
                intState = checkSendBecomingNoisyIntentInt(134217728, intState, musicDevice);
            }
            this.mDeviceBroker.postSetHearingAidConnectionState(state, device, intState);
        }
        return intState;
    }

    @GuardedBy({"mConnectedDevices"})
    private void makeA2dpDeviceAvailable(String address, String name, String eventSource, int a2dpCodec) {
        this.mDeviceBroker.setBluetoothA2dpOnInt(true, eventSource);
        AudioSystem.setDeviceConnectionState(128, 1, address, name, a2dpCodec);
        AudioSystem.setParameters("A2dpSuspended=false");
        this.mConnectedDevices.put(DeviceInfo.makeDeviceListKey(128, address), new DeviceInfo(128, name, address, a2dpCodec));
        this.mDeviceBroker.postAccessoryPlugMediaUnmute(128);
        setCurrentAudioRouteNameIfPossible(name);
    }

    @GuardedBy({"mConnectedDevices"})
    private void makeA2dpDeviceUnavailableNow(String address, int a2dpCodec) {
        if (address != null) {
            this.mDeviceBroker.setAvrcpAbsoluteVolumeSupported(false);
            AudioSystem.setDeviceConnectionState(128, 0, address, "", a2dpCodec);
            this.mConnectedDevices.remove(DeviceInfo.makeDeviceListKey(128, address));
            setCurrentAudioRouteNameIfPossible(null);
            if (this.mDockAddress == address) {
                this.mDockAddress = null;
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mConnectedDevices"})
    /* renamed from: makeA2dpDeviceUnavailableLater */
    public void lambda$disconnectA2dp$1$AudioDeviceInventory(String address, int delayMs) {
        int a2dpCodec;
        AudioSystem.setParameters("A2dpSuspended=true");
        String deviceKey = DeviceInfo.makeDeviceListKey(128, address);
        DeviceInfo deviceInfo = this.mConnectedDevices.get(deviceKey);
        if (deviceInfo != null) {
            a2dpCodec = deviceInfo.mDeviceCodecFormat;
        } else {
            a2dpCodec = 0;
        }
        this.mConnectedDevices.remove(deviceKey);
        this.mDeviceBroker.setA2dpDockTimeout(address, a2dpCodec, delayMs);
    }

    @GuardedBy({"mConnectedDevices"})
    private void makeA2dpSrcAvailable(String address) {
        AudioSystem.setDeviceConnectionState(-2147352576, 1, address, "", 0);
        this.mConnectedDevices.put(DeviceInfo.makeDeviceListKey(-2147352576, address), new DeviceInfo(-2147352576, "", address, 0));
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mConnectedDevices"})
    /* renamed from: makeA2dpSrcUnavailable */
    public void lambda$disconnectA2dpSink$3$AudioDeviceInventory(String address) {
        AudioSystem.setDeviceConnectionState(-2147352576, 0, address, "", 0);
        this.mConnectedDevices.remove(DeviceInfo.makeDeviceListKey(-2147352576, address));
    }

    @GuardedBy({"mConnectedDevices"})
    private void makeHearingAidDeviceAvailable(String address, String name, int streamType, String eventSource) {
        this.mDeviceBroker.postSetHearingAidVolumeIndex(this.mDeviceBroker.getVssVolumeForDevice(streamType, 134217728), streamType);
        AudioSystem.setDeviceConnectionState(134217728, 1, address, name, 0);
        this.mConnectedDevices.put(DeviceInfo.makeDeviceListKey(134217728, address), new DeviceInfo(134217728, name, address, 0));
        this.mDeviceBroker.postAccessoryPlugMediaUnmute(134217728);
        this.mDeviceBroker.postApplyVolumeOnDevice(streamType, 134217728, "makeHearingAidDeviceAvailable");
        setCurrentAudioRouteNameIfPossible(name);
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mConnectedDevices"})
    /* renamed from: makeHearingAidDeviceUnavailable */
    public void lambda$disconnectHearingAid$5$AudioDeviceInventory(String address) {
        AudioSystem.setDeviceConnectionState(134217728, 0, address, "", 0);
        this.mConnectedDevices.remove(DeviceInfo.makeDeviceListKey(134217728, address));
        setCurrentAudioRouteNameIfPossible(null);
    }

    @GuardedBy({"mConnectedDevices"})
    private void setCurrentAudioRouteNameIfPossible(String name) {
        synchronized (this.mCurAudioRoutes) {
            if (!TextUtils.equals(this.mCurAudioRoutes.bluetoothName, name)) {
                if (name != null || !isCurrentDeviceConnected()) {
                    this.mCurAudioRoutes.bluetoothName = name;
                    this.mDeviceBroker.postReportNewRoutes();
                }
            }
        }
    }

    @GuardedBy({"mConnectedDevices"})
    private boolean isCurrentDeviceConnected() {
        return this.mConnectedDevices.values().stream().anyMatch(new Predicate() {
            /* class com.android.server.audio.$$Lambda$AudioDeviceInventory$MfLl81BWvF9OIWh52LJfesOjVdw */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AudioDeviceInventory.this.lambda$isCurrentDeviceConnected$6$AudioDeviceInventory((AudioDeviceInventory.DeviceInfo) obj);
            }
        });
    }

    public /* synthetic */ boolean lambda$isCurrentDeviceConnected$6$AudioDeviceInventory(DeviceInfo deviceInfo) {
        return TextUtils.equals(deviceInfo.mDeviceName, this.mCurAudioRoutes.bluetoothName);
    }

    @GuardedBy({"mConnectedDevices"})
    private int checkSendBecomingNoisyIntentInt(int device, int state, int musicDevice) {
        if (state != 0 || (this.mBecomingNoisyIntentDevices & device) == 0) {
            return 0;
        }
        int devices = 0;
        for (int i = 0; i < this.mConnectedDevices.size(); i++) {
            int dev = this.mConnectedDevices.valueAt(i).mDeviceType;
            if ((Integer.MIN_VALUE & dev) == 0 && (this.mBecomingNoisyIntentDevices & dev) != 0) {
                devices |= dev;
            }
        }
        if (musicDevice == 0) {
            musicDevice = this.mDeviceBroker.getDeviceForStream(3);
        }
        if (!((device == musicDevice || this.mDeviceBroker.isInCommunication()) && device == devices && !this.mDeviceBroker.hasMediaDynamicPolicy() && (32768 & musicDevice) == 0)) {
            return 0;
        }
        if (AudioSystem.isStreamActive(3, 0) || this.mDeviceBroker.hasAudioFocusUsers()) {
            this.mDeviceBroker.postBroadcastBecomingNoisy();
            return 1000;
        }
        AudioService.sDeviceLogger.log(new AudioEventLogger.StringEvent("dropping ACTION_AUDIO_BECOMING_NOISY").printLog(TAG));
        return 0;
    }

    private void sendDeviceConnectionIntent(int device, int state, String address, String deviceName) {
        String mDeviceInString;
        if (AudioService.DEBUG_DEVICES) {
            Slog.i(TAG, "sendDeviceConnectionIntent(dev:0x" + Integer.toHexString(device) + " state:0x" + Integer.toHexString(state) + " address:" + address + " name:" + deviceName + ");");
            if ((device & Integer.MIN_VALUE) == Integer.MIN_VALUE) {
                mDeviceInString = AudioSystem.getInputDeviceName(device);
            } else {
                mDeviceInString = AudioSystem.getOutputDeviceName(device);
            }
            Log.i(TAG, "sendDeviceConnectionIntent(dev:0x" + Integer.toHexString(device) + " state:0x" + Integer.toHexString(state) + "[" + mDeviceInString + "] address:" + address + " name:" + deviceName + ");");
        }
        Intent intent = new Intent();
        if (device != -2113929216) {
            if (device != 4) {
                int i = 0;
                if (device != 8) {
                    if (device != 1024) {
                        if (device != 131072) {
                            if (device != 262144) {
                                if (device == 67108864) {
                                    intent.setAction("android.intent.action.HEADSET_PLUG");
                                    if (AudioSystem.getDeviceConnectionState(-2113929216, "") == 1) {
                                        i = 1;
                                    }
                                    intent.putExtra("microphone", i);
                                }
                            }
                        }
                    }
                    configureHdmiPlugIntent(intent, state);
                }
                intent.setAction("android.intent.action.HEADSET_PLUG");
                intent.putExtra("microphone", 0);
            } else {
                intent.setAction("android.intent.action.HEADSET_PLUG");
                intent.putExtra("microphone", 1);
            }
        } else if (AudioSystem.getDeviceConnectionState(67108864, "") == 1) {
            intent.setAction("android.intent.action.HEADSET_PLUG");
            intent.putExtra("microphone", 1);
        } else {
            return;
        }
        if (intent.getAction() == null) {
            if (AudioService.DEBUG_DEVICES) {
                Log.e(TAG, "Action=null, skip Headset Pluged-out broadcast.");
            }
        } else if (state != 0 || intent.getAction() != "android.intent.action.HEADSET_PLUG" || (AudioSystem.getDeviceConnectionState(67108864, "") != 1 && AudioSystem.getDeviceConnectionState(8, "") != 1 && AudioSystem.getDeviceConnectionState(4, "") != 1)) {
            intent.putExtra(CONNECT_INTENT_KEY_STATE, state);
            intent.putExtra(CONNECT_INTENT_KEY_ADDRESS, address);
            intent.putExtra(CONNECT_INTENT_KEY_PORT_NAME, deviceName);
            intent.addFlags(1073741824);
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityManager.broadcastStickyIntent(intent, -2);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (AudioService.DEBUG_DEVICES) {
            Log.e(TAG, "Headset Pluged-out broadcast is not send.Still headset is Pluged-in");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001a, code lost:
        if (r5 != 67108864) goto L_0x0027;
     */
    private void updateAudioRoutes(int device, int state) {
        int newConn;
        int connType = 0;
        if (device != 4) {
            if (device != 8) {
                if (device != 1024) {
                    if (device != 16384) {
                        if (device != 131072) {
                            if (device != 262144) {
                            }
                        }
                    }
                    connType = 16;
                }
                connType = 8;
            }
            connType = 2;
        } else {
            connType = 1;
        }
        synchronized (this.mCurAudioRoutes) {
            if (connType != 0) {
                int newConn2 = this.mCurAudioRoutes.mainType;
                if (state != 0) {
                    newConn = newConn2 | connType;
                } else {
                    newConn = newConn2 & (~connType);
                }
                if (newConn != this.mCurAudioRoutes.mainType) {
                    this.mCurAudioRoutes.mainType = newConn;
                    this.mDeviceBroker.postReportNewRoutes();
                }
            }
        }
    }

    private void configureHdmiPlugIntent(Intent intent, int state) {
        intent.setAction("android.media.action.HDMI_AUDIO_PLUG");
        intent.putExtra("android.media.extra.AUDIO_PLUG_STATE", state);
        if (state == 1) {
            ArrayList<AudioPort> ports = new ArrayList<>();
            int status = AudioSystem.listAudioPorts(ports, new int[1]);
            if (status != 0) {
                Log.e(TAG, "listAudioPorts error " + status + " in configureHdmiPlugIntent");
                return;
            }
            Iterator<AudioPort> it = ports.iterator();
            while (it.hasNext()) {
                AudioPort port = it.next();
                if (port instanceof AudioDevicePort) {
                    AudioDevicePort devicePort = (AudioDevicePort) port;
                    if (devicePort.type() == 1024 || devicePort.type() == 262144) {
                        int[] formats = AudioFormat.filterPublicFormats(devicePort.formats());
                        if (formats.length > 0) {
                            ArrayList<Integer> encodingList = new ArrayList<>(1);
                            for (int format : formats) {
                                if (format != 0) {
                                    encodingList.add(Integer.valueOf(format));
                                }
                            }
                            intent.putExtra("android.media.extra.ENCODINGS", encodingList.stream().mapToInt($$Lambda$AudioDeviceInventory$u_r8SlQF9hKqpPB7hUtpbqyzdc.INSTANCE).toArray());
                        }
                        int maxChannels = 0;
                        for (int mask : devicePort.channelMasks()) {
                            int channelCount = AudioFormat.channelCountFromOutChannelMask(mask);
                            if (channelCount > maxChannels) {
                                maxChannels = channelCount;
                            }
                        }
                        intent.putExtra("android.media.extra.MAX_CHANNEL_COUNT", maxChannels);
                    }
                }
            }
        }
    }

    private void disconnectSco() {
        DeviceInfo scodi = null;
        String deviceKey = null;
        int connectedScoCount = 0;
        Log.d(TAG, "ConnectedDevices size : " + this.mConnectedDevices.size());
        for (int i = 0; i < this.mConnectedDevices.size(); i++) {
            DeviceInfo di = this.mConnectedDevices.valueAt(i);
            Log.d(TAG, "mDeviceType :" + di.mDeviceType + " connectedScoCount:" + connectedScoCount);
            if (di.mDeviceType == 16 || di.mDeviceType == 32) {
                connectedScoCount++;
                scodi = di;
                deviceKey = DeviceInfo.makeDeviceListKey(scodi.mDeviceType, scodi.mDeviceAddress);
            }
        }
        if (!(connectedScoCount != 1 || scodi == null || deviceKey == null)) {
            AudioSystem.setDeviceConnectionState(scodi.mDeviceType, 0, scodi.mDeviceAddress, scodi.mDeviceName, 0);
            this.mConnectedDevices.remove(deviceKey);
        }
    }

    public int getCurrentConnectedScoDevices() {
        int i = 0;
        while (i < this.mConnectedDevices.size() && this.mConnectedDevices.valueAt(i).mDeviceType != 32) {
            if (this.mConnectedDevices.valueAt(i).mDeviceType == 16) {
                return 16;
            }
            i++;
        }
        return 32;
    }
}
