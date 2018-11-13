package com.android.server;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.util.Log;
import android.util.Slog;
import com.android.server.input.InputManagerService;
import com.android.server.input.InputManagerService.WiredAccessoryCallbacks;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class WiredAccessoryManager implements WiredAccessoryCallbacks {
    private static final int BIT_HDMI_AUDIO = 16;
    private static final int BIT_HEADSET = 2;
    private static final int BIT_HEADSET_NO_MIC = 1;
    private static final int BIT_LINEOUT = 32;
    private static final int BIT_USB_HEADSET_ANLG = 4;
    private static final int BIT_USB_HEADSET_DGTL = 8;
    private static final boolean LOG = true;
    private static final int MSG_NEW_DEVICE_STATE = 1;
    private static final int MSG_SYSTEM_READY = 2;
    private static final String NAME_DP_AUDIO = "soc:qcom,msm-ext-disp";
    private static final String NAME_H2W = "h2w";
    private static final String NAME_HDMI = "hdmi";
    private static final String NAME_HDMI_AUDIO = "hdmi_audio";
    private static final String NAME_USB_AUDIO = "usb_audio";
    private static final int SUPPORTED_HEADSETS = 63;
    private static final String TAG = WiredAccessoryManager.class.getSimpleName();
    private final AudioManager mAudioManager;
    private final Handler mHandler = new Handler(Looper.myLooper(), null, true) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    WiredAccessoryManager.this.setDevicesState(msg.arg1, msg.arg2, (String) msg.obj);
                    WiredAccessoryManager.this.mWakeLock.release();
                    return;
                case 2:
                    WiredAccessoryManager.this.onSystemReady();
                    WiredAccessoryManager.this.mWakeLock.release();
                    return;
                default:
                    return;
            }
        }
    };
    private int mHeadsetState;
    private final InputManagerService mInputManager;
    private final Object mLock = new Object();
    private final WiredAccessoryObserver mObserver;
    private int mSwitchValues;
    private final boolean mUseDevInputEventForAudioJack;
    private final WakeLock mWakeLock;

    class WiredAccessoryObserver extends UEventObserver {
        private final List<UEventInfo> mUEventInfo = makeObservedUEventList();

        private final class UEventInfo {
            private int mCableIndex;
            private int mDevIndex;
            private final String mDevName;
            private final int mState1Bits;
            private final int mState2Bits;
            private final int mStateNbits;

            public UEventInfo(String devName, int state1Bits, int state2Bits, int stateNbits) {
                this.mDevName = devName;
                this.mState1Bits = state1Bits;
                this.mState2Bits = state2Bits;
                this.mStateNbits = stateNbits;
                if (this.mDevName.equals(WiredAccessoryManager.NAME_DP_AUDIO)) {
                    getDevIndex();
                    getCableIndex();
                }
            }

            private void getDevIndex() {
                int index = 0;
                char[] buffer = new char[1024];
                while (true) {
                    try {
                        FileReader file = new FileReader(String.format(Locale.US, "/sys/class/switch/extcon%d/name", new Object[]{Integer.valueOf(index)}));
                        int len = file.read(buffer, 0, 1024);
                        file.close();
                        if (new String(buffer, 0, len).trim().equals(this.mDevName)) {
                            this.mDevIndex = index;
                            return;
                        }
                        index++;
                    } catch (FileNotFoundException e) {
                        return;
                    } catch (Exception e2) {
                        Slog.e(WiredAccessoryManager.TAG, "", e2);
                        return;
                    }
                }
            }

            private void getCableIndex() {
                int index = 0;
                char[] buffer = new char[1024];
                while (true) {
                    try {
                        FileReader file = new FileReader(String.format(Locale.US, "/sys/class/switch/extcon%d/cable.%d/name", new Object[]{Integer.valueOf(this.mDevIndex), Integer.valueOf(index)}));
                        int len = file.read(buffer, 0, 1024);
                        file.close();
                        if (new String(buffer, 0, len).trim().equals("DP")) {
                            this.mCableIndex = index;
                            return;
                        }
                        index++;
                    } catch (FileNotFoundException e) {
                        return;
                    } catch (Exception e2) {
                        Slog.e(WiredAccessoryManager.TAG, "", e2);
                        return;
                    }
                }
            }

            public String getDevName() {
                return this.mDevName;
            }

            public String getDevPath() {
                if (this.mDevName.equals(WiredAccessoryManager.NAME_DP_AUDIO)) {
                    return String.format(Locale.US, "/devices/platform/soc/%s/extcon/extcon%d", new Object[]{this.mDevName, Integer.valueOf(this.mDevIndex)});
                }
                return String.format(Locale.US, "/devices/virtual/switch/%s", new Object[]{this.mDevName});
            }

            public String getSwitchStatePath() {
                if (this.mDevName.equals(WiredAccessoryManager.NAME_DP_AUDIO)) {
                    return String.format(Locale.US, "/sys/class/switch/extcon%d/cable.%d/state", new Object[]{Integer.valueOf(this.mDevIndex), Integer.valueOf(this.mCableIndex)});
                }
                return String.format(Locale.US, "/sys/class/switch/%s/state", new Object[]{this.mDevName});
            }

            public boolean checkSwitchExists() {
                return new File(getSwitchStatePath()).exists();
            }

            public int computeNewHeadsetState(int headsetState, int switchState) {
                int preserveMask = ~((this.mState1Bits | this.mState2Bits) | this.mStateNbits);
                int setBits = switchState == 1 ? this.mState1Bits : switchState == 2 ? this.mState2Bits : switchState == this.mStateNbits ? this.mStateNbits : 0;
                return (headsetState & preserveMask) | setBits;
            }
        }

        void init() {
            int i;
            synchronized (WiredAccessoryManager.this.mLock) {
                Slog.v(WiredAccessoryManager.TAG, "init()");
                char[] buffer = new char[1024];
                for (i = 0; i < this.mUEventInfo.size(); i++) {
                    UEventInfo uei = (UEventInfo) this.mUEventInfo.get(i);
                    try {
                        FileReader file = new FileReader(uei.getSwitchStatePath());
                        int len = file.read(buffer, 0, 1024);
                        file.close();
                        int curState = Integer.parseInt(new String(buffer, 0, len).trim());
                        if (curState > 0) {
                            updateStateLocked(uei.getDevPath(), uei.getDevName(), curState);
                        }
                    } catch (FileNotFoundException e) {
                        Slog.w(WiredAccessoryManager.TAG, uei.getSwitchStatePath() + " not found while attempting to determine initial switch state");
                    } catch (Exception e2) {
                        Slog.e(WiredAccessoryManager.TAG, "", e2);
                    }
                }
            }
            for (i = 0; i < this.mUEventInfo.size(); i++) {
                startObserving("DEVPATH=" + ((UEventInfo) this.mUEventInfo.get(i)).getDevPath());
            }
        }

        private List<UEventInfo> makeObservedUEventList() {
            UEventInfo uei;
            List<UEventInfo> retVal = new ArrayList();
            if (!WiredAccessoryManager.this.mUseDevInputEventForAudioJack) {
                uei = new UEventInfo(WiredAccessoryManager.NAME_H2W, 2, 1, 32);
                if (uei.checkSwitchExists()) {
                    retVal.add(uei);
                } else {
                    Slog.w(WiredAccessoryManager.TAG, "This kernel does not have wired headset support");
                }
            }
            uei = new UEventInfo(WiredAccessoryManager.NAME_USB_AUDIO, 4, 8, 0);
            if (uei.checkSwitchExists()) {
                retVal.add(uei);
            } else {
                Slog.w(WiredAccessoryManager.TAG, "This kernel does not have usb audio support");
            }
            uei = new UEventInfo(WiredAccessoryManager.NAME_HDMI_AUDIO, 16, 0, 0);
            if (uei.checkSwitchExists()) {
                retVal.add(uei);
            } else {
                uei = new UEventInfo(WiredAccessoryManager.NAME_HDMI, 16, 0, 0);
                if (uei.checkSwitchExists()) {
                    retVal.add(uei);
                } else {
                    Slog.w(WiredAccessoryManager.TAG, "This kernel does not have HDMI audio support");
                }
            }
            uei = new UEventInfo(WiredAccessoryManager.NAME_DP_AUDIO, 16, 0, 0);
            if (uei.checkSwitchExists()) {
                retVal.add(uei);
            } else {
                Slog.w(WiredAccessoryManager.TAG, "This kernel does not have DP audio support");
            }
            return retVal;
        }

        public void onUEvent(UEvent event) {
            String devPath = event.get("DEVPATH");
            String name = event.get("NAME");
            int state = 0;
            if (name == null) {
                name = event.get("SWITCH_NAME");
            }
            try {
                if (name.equals(WiredAccessoryManager.NAME_DP_AUDIO)) {
                    String state_str = event.get("STATE");
                    int offset = 0;
                    int length = state_str.length();
                    while (offset < length) {
                        int equals = state_str.indexOf(61, offset);
                        if (equals > offset && state_str.substring(offset, equals).equals("DP")) {
                            state = Integer.parseInt(state_str.substring(equals + 1, equals + 2));
                            break;
                        }
                        offset = equals + 3;
                    }
                } else {
                    state = Integer.parseInt(event.get("SWITCH_STATE"));
                }
            } catch (NumberFormatException e) {
                Slog.i(WiredAccessoryManager.TAG, "couldn't get state from event, checking node");
                int i = 0;
                while (i < this.mUEventInfo.size()) {
                    UEventInfo uei = (UEventInfo) this.mUEventInfo.get(i);
                    if (name.equals(uei.getDevName())) {
                        char[] buffer = new char[1024];
                        int len = 0;
                        try {
                            FileReader file = new FileReader(uei.getSwitchStatePath());
                            len = file.read(buffer, 0, 1024);
                            file.close();
                        } catch (FileNotFoundException e2) {
                            Slog.e(WiredAccessoryManager.TAG, "file not found");
                        } catch (Exception e11) {
                            Slog.e(WiredAccessoryManager.TAG, "", e11);
                        }
                        try {
                            state = Integer.parseInt(new String(buffer, 0, len).trim());
                            break;
                        } catch (NumberFormatException e3) {
                            Slog.e(WiredAccessoryManager.TAG, "could not convert to number");
                        }
                    } else {
                        i++;
                    }
                }
            }
            synchronized (WiredAccessoryManager.this.mLock) {
                updateStateLocked(devPath, name, state);
            }
        }

        private void updateStateLocked(String devPath, String name, int state) {
            for (int i = 0; i < this.mUEventInfo.size(); i++) {
                UEventInfo uei = (UEventInfo) this.mUEventInfo.get(i);
                Slog.w(WiredAccessoryManager.TAG, "uei.getDevPath=" + uei.getDevPath());
                Slog.w(WiredAccessoryManager.TAG, "uevent.getDevPath=" + devPath);
                if (devPath.equals(uei.getDevPath())) {
                    WiredAccessoryManager.this.updateLocked(name, uei.computeNewHeadsetState(WiredAccessoryManager.this.mHeadsetState, state));
                    return;
                }
            }
        }
    }

    public WiredAccessoryManager(Context context, InputManagerService inputManager) {
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "WiredAccessoryManager");
        this.mWakeLock.setReferenceCounted(false);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mInputManager = inputManager;
        this.mUseDevInputEventForAudioJack = context.getResources().getBoolean(17957050);
        this.mObserver = new WiredAccessoryObserver();
    }

    private void onSystemReady() {
        if (this.mUseDevInputEventForAudioJack) {
            int switchValues = 0;
            if (this.mInputManager.getSwitchState(-1, -256, 2) == 1) {
                switchValues = 4;
            }
            if (this.mInputManager.getSwitchState(-1, -256, 4) == 1) {
                switchValues |= 16;
            }
            if (this.mInputManager.getSwitchState(-1, -256, 6) == 1) {
                switchValues |= 64;
            }
            notifyWiredAccessoryChanged(0, switchValues, 84);
        }
        this.mObserver.init();
    }

    public void notifyWiredAccessoryChanged(long whenNanos, int switchValues, int switchMask) {
        Slog.v(TAG, "notifyWiredAccessoryChanged: when=" + whenNanos + " bits=" + switchCodeToString(switchValues, switchMask) + " mask=" + Integer.toHexString(switchMask));
        synchronized (this.mLock) {
            int headset;
            this.mSwitchValues = (this.mSwitchValues & (~switchMask)) | switchValues;
            switch (this.mSwitchValues & 84) {
                case 0:
                    headset = 0;
                    break;
                case 4:
                    headset = 1;
                    break;
                case 16:
                    headset = 2;
                    break;
                case 20:
                    headset = 2;
                    break;
                case 64:
                    headset = 32;
                    break;
                default:
                    headset = 0;
                    break;
            }
            updateLocked(NAME_H2W, (this.mHeadsetState & -36) | headset);
        }
    }

    public void systemReady() {
        synchronized (this.mLock) {
            this.mWakeLock.acquire();
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, 0, 0, null));
        }
    }

    private void updateLocked(String newName, int newState) {
        int headsetState = newState & 63;
        int usb_headset_anlg = headsetState & 4;
        int usb_headset_dgtl = headsetState & 8;
        int h2w_headset = headsetState & 35;
        boolean h2wStateChange = true;
        boolean usbStateChange = true;
        Slog.v(TAG, "newName=" + newName + " newState=" + newState + " headsetState=" + headsetState + " prev headsetState=" + this.mHeadsetState);
        if (this.mHeadsetState == headsetState) {
            Log.e(TAG, "No state change.");
            return;
        }
        if (h2w_headset == 35) {
            Log.e(TAG, "Invalid combination, unsetting h2w flag");
            h2wStateChange = false;
        }
        if (usb_headset_anlg == 4 && usb_headset_dgtl == 8) {
            Log.e(TAG, "Invalid combination, unsetting usb flag");
            usbStateChange = false;
        }
        if (h2wStateChange || (usbStateChange ^ 1) == 0) {
            this.mWakeLock.acquire();
            Log.i(TAG, "MSG_NEW_DEVICE_STATE");
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, headsetState, this.mHeadsetState, ""));
            this.mHeadsetState = headsetState;
            return;
        }
        Log.e(TAG, "invalid transition, returning ...");
    }

    private void setDevicesState(int headsetState, int prevHeadsetState, String headsetName) {
        synchronized (this.mLock) {
            int allHeadsets = 63;
            int curHeadset = 1;
            while (allHeadsets != 0) {
                if ((curHeadset & allHeadsets) != 0) {
                    setDeviceStateLocked(curHeadset, headsetState, prevHeadsetState, headsetName);
                    allHeadsets &= ~curHeadset;
                }
                curHeadset <<= 1;
            }
        }
    }

    private void setDeviceStateLocked(int headset, int headsetState, int prevHeadsetState, String headsetName) {
        if ((headsetState & headset) != (prevHeadsetState & headset)) {
            int state;
            int outDevice;
            int inDevice = 0;
            if ((headsetState & headset) != 0) {
                state = 1;
            } else {
                state = 0;
            }
            if (headset == 2) {
                outDevice = 4;
                inDevice = -2147483632;
            } else if (headset == 1) {
                outDevice = 8;
            } else if (headset == 32) {
                outDevice = DumpState.DUMP_INTENT_FILTER_VERIFIERS;
            } else if (headset == 4) {
                outDevice = 2048;
            } else if (headset == 8) {
                outDevice = 4096;
            } else if (headset == 16) {
                outDevice = 1024;
            } else {
                Slog.e(TAG, "setDeviceState() invalid headset type: " + headset);
                return;
            }
            Slog.v(TAG, "headsetName: " + headsetName + (state == 1 ? " connected" : " disconnected"));
            if (outDevice != 0) {
                if (prevHeadsetState == 1 && headsetState == 2 && outDevice == 8) {
                    state = 2;
                }
                this.mAudioManager.setWiredDeviceConnectionState(outDevice, state, "", headsetName);
            }
            if (inDevice != 0) {
                this.mAudioManager.setWiredDeviceConnectionState(inDevice, state, "", headsetName);
            }
        }
    }

    private String switchCodeToString(int switchValues, int switchMask) {
        StringBuffer sb = new StringBuffer();
        if (!((switchMask & 4) == 0 || (switchValues & 4) == 0)) {
            sb.append("SW_HEADPHONE_INSERT ");
        }
        if (!((switchMask & 16) == 0 || (switchValues & 16) == 0)) {
            sb.append("SW_MICROPHONE_INSERT");
        }
        return sb.toString();
    }
}
