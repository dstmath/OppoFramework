package com.oppo.internal.telephony.rf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.OppoRIL;
import com.oppo.internal.telephony.OppoTelephonyController;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class OemProximitySensorManager {
    private static int AP_BAND_DUAL;
    private static String EXTRA_WIFI_AP_WORKING_FREQUENCY;
    static String TAG = "OemProximitySensor";
    private static String WIFI_AP_CHANNEL_CHANGED_ACTION;
    private static String WIFI_SHARING_STATE_CHANGED_ACTION;
    private static int WIFI_SHARING_STATE_ENABLED;
    static OemProximitySensorManager sInstance = null;
    ProximitySensorEventListener mProximitySensorListener;

    public interface IOemListener {
        boolean isTestCard();

        void onAudioDeviceRouteChanged(int i);

        void onCallChange(PhoneState phoneState);

        void onDataChange(PhoneState phoneState);

        void onHeadsetChange(boolean z);

        void onScreenChange(boolean z);

        void onSensorChange(SensorState sensorState);

        void onSuscriptionChange();

        void onSwitchChange(int i);

        void onUsbChange(boolean z);

        void onWifiChange(int i);
    }

    public enum PhoneState {
        IDLE,
        OFFHOOK
    }

    public enum SensorState {
        FAR,
        NEAR,
        INVALID
    }

    public enum SwitchWifiSar {
        SAR_OFF,
        SAR_ON_Group1,
        SAR_ON_Group2
    }

    /* access modifiers changed from: private */
    public static class ProximitySensorEventListener extends Handler implements SensorEventListener {
        private static final int BIT_CHARGER = 8;
        private static final int BIT_HEADBODY = 1;
        private static final int BIT_HEADSET = 16;
        private static final int BIT_OTG = 4;
        private static final int BIT_USB = 2;
        private static final int BIT_WIFI2P4G = 32;
        private static final int BIT_WIFI5G = 64;
        protected static final int EVENT_OEM_DATA_DELAY = 297;
        protected static final int EVENT_OEM_SCREEN_CHANGED = 298;
        protected static final int EVENT_PS_Wifi_STATE_CHANGED = 301;
        protected static final int EVENT_WIFI_SAR = 299;
        private static final float FAR_THRESHOLD = 5.0f;
        private static final int ROUTE_EARPIECE = 1;
        protected static final int TIMER_DATA_DELAY = 3000;
        private static final int WIFI_STATE_CELLULAR_ONLY = 0;
        private static final int WIFI_STATE_CELLULAR_WIFI_2p4G = 1;
        private static final int WIFI_STATE_CELLULAR_WIFI_2p4G_5G = 3;
        private static final int WIFI_STATE_CELLULAR_WIFI_5G = 2;
        private int mApBand = -1;
        private boolean mIsHotspot24 = false;
        private boolean mIsNormal24 = false;
        private boolean mIsNormal5 = false;
        private boolean mIsP2P5 = false;
        private volatile boolean mIsProximListen = false;
        private volatile boolean mIsTestCard = false;
        private volatile int mLastAudioDeviceRoute;
        private PhoneState mLastCallState;
        private volatile boolean mLastChargerPresent;
        private PhoneState mLastDataState;
        private volatile boolean mLastHeadsetPresent;
        private volatile boolean mLastOtgPresent;
        private volatile boolean mLastScreenOn;
        private SensorState mLastSensorState;
        private int[] mLastSubId = null;
        private volatile int mLastSwitchScene;
        private volatile boolean mLastUsbPresent;
        private volatile SwitchWifiSar mLastWifiSar;
        private volatile int mLastWifiState = 0;
        private final IOemListener mListener;
        private final float mMaxValue;
        private OemReceiver mOemReceiver = null;
        private SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangedListener = null;
        private OtgListener mOtgListener = null;
        private boolean mP2pStart = false;
        private PhoneStateListener[] mPhoneStateListeners = null;
        private final Sensor mProximitySensor;
        OemMTKSarConfigParser mSarConfigParser;
        private volatile boolean mSarStatusReseted = false;
        private final SensorManager mSensorManager;
        private int mcheckband = 0;
        private int mcheckmode = 0;

        protected class OemPhoneStateListener extends PhoneStateListener {
            ProximitySensorEventListener mPssListener;

            public OemPhoneStateListener(ProximitySensorEventListener pssListener, int slotId) {
                this.mPssListener = pssListener;
            }

            public void onDataActivity(int direction) {
                this.mPssListener.onDataActivity(direction);
            }

            public void onCallStateChanged(int value, String incomingNumber) {
                this.mPssListener.onCallStateChanged(value, incomingNumber);
            }
        }

        protected class OemSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
            ProximitySensorEventListener mPssListener;

            public OemSubscriptionsChangedListener(ProximitySensorEventListener pssListener) {
                this.mPssListener = pssListener;
            }

            public void onSubscriptionsChanged() {
                this.mPssListener.onSubscriptionsChanged();
            }
        }

        /* access modifiers changed from: protected */
        public class OemReceiver extends BroadcastReceiver {
            private static final String ACTION_AUDIO_DEVICE_ROUTE_CHANGED = "android.media.ACTION_AUDIO_DEVICE_ROUTE_CHANGED";
            private static final String ACTION_BATTERY_CHANGED = "oppo.intent.action.BATTERY_PLUGGED_CHANGED";
            private static final String ACTION_SAR_UPDATE_BY_ENG = "oppo.intent.action.SAR_UPDATE_BY_ENG";
            ProximitySensorEventListener mProximityListener;

            public OemReceiver(ProximitySensorEventListener orienListener) {
                this.mProximityListener = orienListener;
            }

            public void onReceive(Context context, Intent intent) {
                String action;
                if (intent != null && (action = intent.getAction()) != null) {
                    synchronized (this) {
                        boolean z = false;
                        if (action.equals("android.hardware.usb.action.USB_STATE")) {
                            boolean usbPresent = false;
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "ACTION_USB_STATE:" + intent.getBooleanExtra("connected", false));
                            }
                            if (intent.getBooleanExtra("connected", false)) {
                                usbPresent = true;
                            }
                            this.mProximityListener.onUsbStateChanged(usbPresent);
                        } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                            int plugged = intent.getIntExtra("plugged", 0);
                            boolean chargerPresent = false;
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "ACTION_BATTERY_CHANGED:" + plugged);
                            }
                            if (1 == plugged) {
                                chargerPresent = true;
                            } else if (2 == plugged) {
                                chargerPresent = true;
                            }
                            this.mProximityListener.onChargerStateChanged(chargerPresent);
                        } else if (action.equals("android.intent.action.HEADSET_PLUG")) {
                            boolean headsetPresent = false;
                            if (intent.getIntExtra("state", 0) == 1) {
                                headsetPresent = true;
                            }
                            this.mProximityListener.onHeadsetStateChanged(headsetPresent);
                        } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "Receive NETWORK_STATE_CHANGED_ACTION");
                            }
                            WifiInfo mWifiinfo = null;
                            WifiManager mwifimanager = null;
                            if (!(context == null || context.getSystemService("wifi") == null)) {
                                mwifimanager = (WifiManager) context.getSystemService("wifi");
                            }
                            if (mwifimanager != null) {
                                mWifiinfo = mwifimanager.getConnectionInfo();
                            }
                            NetworkInfo networkinfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                            if (networkinfo != null) {
                                networkinfo.isConnected();
                            }
                            if (mWifiinfo != null) {
                                int isWifiConnected = mWifiinfo.getNetworkId();
                                if (OemConstant.SWITCH_LOG) {
                                    Rlog.d(OemProximitySensorManager.TAG, "isWifiConnected: " + isWifiConnected);
                                }
                                if (-1 != isWifiConnected) {
                                    ProximitySensorEventListener.this.mIsNormal24 = mWifiinfo.is24GHz();
                                    ProximitySensorEventListener.this.mIsNormal5 = mWifiinfo.is5GHz();
                                    if (OemConstant.SWITCH_LOG) {
                                        Rlog.d(OemProximitySensorManager.TAG, "mIsNormal24: " + ProximitySensorEventListener.this.mIsNormal24 + " mIsNormal5: " + ProximitySensorEventListener.this.mIsNormal5);
                                    }
                                } else {
                                    ProximitySensorEventListener.this.mIsNormal24 = false;
                                    ProximitySensorEventListener.this.mIsNormal5 = false;
                                }
                            } else {
                                ProximitySensorEventListener.this.mIsNormal24 = false;
                                ProximitySensorEventListener.this.mIsNormal5 = false;
                                if (OemConstant.SWITCH_LOG) {
                                    Rlog.d(OemProximitySensorManager.TAG, "Ash info is NULL!");
                                }
                            }
                            this.mProximityListener.onWifiStateChanged(oppoGetWifiState());
                        } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "Receive WIFI_AP_STATE_CHANGED_ACTION");
                            }
                            boolean isconnected = false;
                            WifiManager mwm = null;
                            if (!(context == null || context.getSystemService("wifi") == null)) {
                                mwm = (WifiManager) context.getSystemService("wifi");
                            }
                            WifiConfiguration wc = null;
                            if (mwm != null) {
                                try {
                                    wc = mwm.getWifiApConfiguration();
                                } catch (Exception e) {
                                    Rlog.e(OemProximitySensorManager.TAG, "WIFI_AP_STATE_CHANGED_ACTION getWifiApConfiguration Exception");
                                }
                                if (mwm.getWifiApState() == 13) {
                                    z = true;
                                }
                                isconnected = z;
                            }
                            int apChl = -1;
                            int apBand = -1;
                            if (wc != null) {
                                apChl = wc.apChannel;
                                apBand = wc.apBand;
                            }
                            if (isconnected) {
                                if (apBand == 0 || apBand == 1) {
                                    ProximitySensorEventListener.this.mApBand = apBand;
                                } else {
                                    ProximitySensorEventListener.this.mApBand = -1;
                                }
                            }
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "AP isconnected: " + isconnected + ", mLastWifiState = " + ProximitySensorEventListener.this.mLastWifiState + ", apChl = " + apChl + ", apBand = " + apBand);
                            }
                            this.mProximityListener.onWifiStateChanged(oppoGetWifiState());
                        } else if (action.equals(OemProximitySensorManager.WIFI_SHARING_STATE_CHANGED_ACTION)) {
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "Receive WIFI_SHARING_STATE_CHANGED_ACTION");
                            }
                            int wifiSharingState = intent.getIntExtra("wifi_state", 14);
                            if (wifiSharingState == OemProximitySensorManager.WIFI_SHARING_STATE_ENABLED) {
                                ProximitySensorEventListener.this.mIsHotspot24 = true;
                            } else {
                                ProximitySensorEventListener.this.mIsHotspot24 = false;
                            }
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "wifiSharingState: " + wifiSharingState + ", mIsHotspot24: " + ProximitySensorEventListener.this.mIsHotspot24);
                            }
                            this.mProximityListener.onWifiStateChanged(oppoGetWifiState());
                        } else if (action.equals("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")) {
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "Receive WIFI_P2P_CONNECTION_CHANGED_ACTION");
                            }
                            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                            if (networkInfo == null || !networkInfo.isConnected()) {
                                ProximitySensorEventListener.this.mIsP2P5 = false;
                                ProximitySensorEventListener.this.mP2pStart = false;
                            } else {
                                ProximitySensorEventListener.this.mIsP2P5 = true;
                                ProximitySensorEventListener.this.mP2pStart = true;
                            }
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "mIsP2P5:" + ProximitySensorEventListener.this.mIsP2P5 + ", mP2pStart:" + ProximitySensorEventListener.this.mP2pStart);
                            }
                            this.mProximityListener.onWifiStateChanged(oppoGetWifiState());
                        } else if (action.equals(ACTION_SAR_UPDATE_BY_ENG)) {
                            this.mProximityListener.onEngineerModeChanged();
                        } else if (action.equals(ACTION_AUDIO_DEVICE_ROUTE_CHANGED)) {
                            int audioDeviceRoute = intent.getIntExtra("android.media.EXTRA_DEVICE_TYPE", 0);
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "Receive ACTION_AUDIO_DEVICE_ROUTE_CHANGED:" + audioDeviceRoute);
                            }
                            this.mProximityListener.onAudioDeviceRouteStateChanged(audioDeviceRoute);
                        }
                    }
                }
            }

            public void register(Context context) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_SAR_UPDATE_BY_ENG);
                if (!OemConstant.usePSensorForSarDetect(ProximitySensorEventListener.getContext()) && (ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 1) != 0) {
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "register for broadcast event: earpiece");
                    }
                    filter.addAction(ACTION_AUDIO_DEVICE_ROUTE_CHANGED);
                }
                if ((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 2) != 0) {
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "register for broadcast event: usb");
                    }
                    filter.addAction("android.hardware.usb.action.USB_STATE");
                }
                if ((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 16) != 0) {
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "register for broadcast event: headset");
                    }
                    filter.addAction("android.intent.action.HEADSET_PLUG");
                }
                if ((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 8) != 0) {
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "register for broadcast event: charger");
                    }
                    filter.addAction("android.intent.action.BATTERY_CHANGED");
                }
                if (!((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 32) == 0 && (ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 64) == 0)) {
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "register for broadcast event: wifi state");
                    }
                    filter.addAction("android.net.wifi.STATE_CHANGE");
                    filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                    filter.addAction(OemProximitySensorManager.WIFI_SHARING_STATE_CHANGED_ACTION);
                    filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
                }
                context.registerReceiver(this, filter);
            }

            public void unregister(Context context) {
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemProximitySensorManager.TAG, "unregister for broadcast event");
                }
                if (!OemConstant.usePSensorForSarDetect(ProximitySensorEventListener.getContext()) && (ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 1) != 0) {
                    ProximitySensorEventListener.this.mLastAudioDeviceRoute = 0;
                }
                if ((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 2) != 0) {
                    ProximitySensorEventListener.this.mLastUsbPresent = false;
                }
                if ((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 16) != 0) {
                    ProximitySensorEventListener.this.mLastHeadsetPresent = false;
                }
                if ((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 8) != 0) {
                    ProximitySensorEventListener.this.mLastChargerPresent = false;
                }
                if (!((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 32) == 0 && (ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 64) == 0)) {
                    ProximitySensorEventListener.this.mLastWifiState = 0;
                }
                context.unregisterReceiver(this);
            }

            private int oppoGetWifiState() {
                int wifiState;
                if ((ProximitySensorEventListener.this.mIsNormal5 && ProximitySensorEventListener.this.mIsHotspot24) || (ProximitySensorEventListener.this.mIsNormal24 && ProximitySensorEventListener.this.mIsP2P5)) {
                    wifiState = 3;
                } else if (ProximitySensorEventListener.this.mIsNormal5 || ProximitySensorEventListener.this.mIsP2P5 || ProximitySensorEventListener.this.mApBand == 1) {
                    wifiState = 2;
                } else if (ProximitySensorEventListener.this.mIsNormal24 || ProximitySensorEventListener.this.mIsHotspot24 || ProximitySensorEventListener.this.mApBand == 0) {
                    wifiState = 1;
                } else {
                    wifiState = 0;
                }
                String str = OemProximitySensorManager.TAG;
                Rlog.d(str, "oppoGetWifiState wifiState:" + wifiState);
                return wifiState;
            }
        }

        /* access modifiers changed from: protected */
        public class OtgListener extends StorageEventListener {
            String lastPath = null;
            ProximitySensorEventListener mPssListener;
            StorageManager sm;

            public OtgListener(ProximitySensorEventListener pssListener, Context context) {
                this.mPssListener = pssListener;
                this.sm = (StorageManager) context.getSystemService("storage");
            }

            public void register(Context context) {
                if ((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 4) != 0) {
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "register for otg event");
                    }
                    this.sm.registerListener(this);
                }
            }

            public void unregister(Context context) {
                if ((ProximitySensorEventListener.this.mSarConfigParser.getXmlScenariosBit() & 4) != 0) {
                    ProximitySensorEventListener.this.mLastOtgPresent = false;
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "unregister for otg event");
                    }
                    this.sm.unregisterListener(this);
                }
            }

            public void onStorageStateChanged(String path, String oldState, String newState) {
                DiskInfo disk_info;
                if (OemConstant.SWITCH_LOG) {
                    String str = OemProximitySensorManager.TAG;
                    Rlog.i(str, "path:" + path + ",old:" + oldState + ",new:" + newState);
                }
                if (path != null && newState != null && !newState.equals(oldState)) {
                    if (newState.equals("mounted")) {
                        boolean isFind = false;
                        Iterator<VolumeInfo> it = this.sm.getVolumes().iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            VolumeInfo volume = it.next();
                            if (path.equals(volume.path)) {
                                isFind = true;
                                String disk_id = volume.getDiskId();
                                if (disk_id != null && (disk_info = this.sm.findDiskById(disk_id)) != null) {
                                    if (OemConstant.SWITCH_LOG) {
                                        String str2 = OemProximitySensorManager.TAG;
                                        Rlog.i(str2, "isUsb:" + disk_info.isUsb());
                                    }
                                    if (!disk_info.isUsb()) {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                        }
                        if (isFind) {
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.i(OemProximitySensorManager.TAG, "onOtgStateChanged 0 true:");
                            }
                            this.lastPath = path;
                            this.mPssListener.onOtgStateChanged(true);
                        }
                    } else if (newState.equals("unmounted") || path.equals(this.lastPath)) {
                        if (OemConstant.SWITCH_LOG) {
                            Rlog.i(OemProximitySensorManager.TAG, "onOtgStateChanged 0 false:");
                        }
                        this.lastPath = null;
                        this.mPssListener.onOtgStateChanged(false);
                    }
                }
            }
        }

        public ProximitySensorEventListener(SensorManager sensorManager, Sensor proximitySensor, IOemListener listener) {
            this.mSensorManager = sensorManager;
            this.mProximitySensor = proximitySensor;
            this.mMaxValue = proximitySensor.getMaximumRange();
            this.mListener = listener;
            this.mLastSensorState = SensorState.INVALID;
            this.mLastDataState = PhoneState.IDLE;
            this.mLastCallState = PhoneState.IDLE;
            this.mLastScreenOn = true;
            this.mLastUsbPresent = false;
            this.mLastChargerPresent = false;
            this.mLastOtgPresent = false;
            this.mLastAudioDeviceRoute = 0;
            this.mLastHeadsetPresent = false;
            this.mLastSwitchScene = -1;
            this.mLastWifiSar = SwitchWifiSar.SAR_OFF;
            this.mSarConfigParser = OemMTKSarConfigParser.getInstance();
            Context context = getContext();
            this.mOnSubscriptionsChangedListener = new OemSubscriptionsChangedListener(this);
            int numPhones = TelephonyManager.from(context).getPhoneCount();
            this.mPhoneStateListeners = new OemPhoneStateListener[numPhones];
            for (int i = 0; i < numPhones; i++) {
                this.mPhoneStateListeners[i] = new OemPhoneStateListener(this, i);
            }
            this.mOemReceiver = new OemReceiver(this);
            this.mOtgListener = new OtgListener(this, context);
        }

        public static Context getContext() {
            return PhoneFactory.getPhone(0).getContext();
        }

        public void register() {
            Context context = getContext();
            this.mOemReceiver.register(context);
            this.mOtgListener.register(context);
            registerScreen();
            registerSubInfo();
            if (!(PhoneFactory.getDefaultPhone() == null || PhoneFactory.getDefaultPhone().mCi == null)) {
                ReflectionHelper.callMethod(PhoneFactory.getDefaultPhone().mCi, "com.android.internal.telephony.CommandsInterface", "registerForPsWifiStateChanged", new Class[]{Handler.class, Integer.TYPE, Object.class}, new Object[]{this, Integer.valueOf((int) EVENT_PS_Wifi_STATE_CHANGED), null});
            }
            Rlog.d(OemProximitySensorManager.TAG, "Initial register, processSwitch");
            processSwitch();
        }

        public void unregister() {
            Context context = getContext();
            this.mOemReceiver.unregister(context);
            this.mOtgListener.unregister(context);
            unregisterScreen();
            unregisterSubInfo();
            if (!(PhoneFactory.getDefaultPhone() == null || PhoneFactory.getDefaultPhone().mCi == null)) {
                ReflectionHelper.callMethod(PhoneFactory.getDefaultPhone().mCi, "com.android.internal.telephony.CommandsInterface", "unregisterForPsWifiStateChanged", new Class[]{Handler.class}, new Object[]{this});
            }
            Rlog.d(OemProximitySensorManager.TAG, "Initial unregister, processSwitch");
            processSwitch();
        }

        public void registerScreen() {
            if (OemConstant.SWITCH_LOG) {
                Rlog.d(OemProximitySensorManager.TAG, "register for screen event");
            }
            OppoTelephonyController.getInstance(getContext()).registerForOemScreenChanged(this, EVENT_OEM_SCREEN_CHANGED, null);
        }

        public void unregisterScreen() {
            if (OemConstant.SWITCH_LOG) {
                Rlog.d(OemProximitySensorManager.TAG, "unregister for screen event");
            }
            OppoTelephonyController.getInstance(getContext()).unregisterOemScreenChanged(this);
        }

        public void registerSubInfo() {
            SubscriptionManager sm = SubscriptionManager.from(PhoneFactory.getPhone(0).getContext());
            if (OemConstant.SWITCH_LOG) {
                Rlog.d(OemProximitySensorManager.TAG, "register for subscription event");
            }
            sm.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        }

        public void unregisterSubInfo() {
            SubscriptionManager sm = SubscriptionManager.from(PhoneFactory.getPhone(0).getContext());
            if (OemConstant.SWITCH_LOG) {
                Rlog.d(OemProximitySensorManager.TAG, "unregister for subscription event");
            }
            sm.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        }

        public void registerProximi() {
            if (OemConstant.usePSensorForSarDetect(getContext()) && (this.mSarConfigParser.getXmlScenariosBit() & 1) != 0 && !this.mIsProximListen) {
                this.mIsProximListen = true;
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemProximitySensorManager.TAG, "register for proximity sensor event");
                }
                this.mSensorManager.registerListener(this, this.mProximitySensor, 2);
            }
        }

        public void unregisterProximi() {
            if (OemConstant.usePSensorForSarDetect(getContext()) && (this.mSarConfigParser.getXmlScenariosBit() & 1) != 0 && this.mIsProximListen) {
                this.mIsProximListen = false;
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemProximitySensorManager.TAG, "unregister for proximity sensor event");
                }
                this.mSensorManager.unregisterListener(this);
                this.mLastSensorState = SensorState.INVALID;
                this.mListener.onSensorChange(SensorState.INVALID);
                processSwitch();
                processWifiSwitch();
            }
        }

        public void registerPhone() {
            if (OemConstant.usePSensorForSarDetect(getContext())) {
                TelephonyManager tm = TelephonyManager.from(PhoneFactory.getPhone(0).getContext());
                int numPhones = tm.getPhoneCount();
                for (int i = 0; i < numPhones; i++) {
                    int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(i);
                    if (SubscriptionController.getInstance().isActiveSubId(subId)) {
                        tm.createForSubscriptionId(subId).listen(this.mPhoneStateListeners[i], 160);
                    }
                }
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemProximitySensorManager.TAG, "register for phone event");
                }
            }
        }

        public void unregisterPhone() {
            if (OemConstant.usePSensorForSarDetect(getContext())) {
                TelephonyManager tm = TelephonyManager.from(PhoneFactory.getPhone(0).getContext());
                int numPhones = tm.getPhoneCount();
                for (int i = 0; i < numPhones; i++) {
                    tm.listen(this.mPhoneStateListeners[i], 0);
                }
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemProximitySensorManager.TAG, "unregister for phone event");
                }
            }
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.values != null && event.values.length != 0) {
                SensorState state = getStateFromValue(event.values[0]);
                synchronized (this) {
                    if (state != this.mLastSensorState) {
                        this.mLastSensorState = state;
                        this.mListener.onSensorChange(state);
                        String str = OemProximitySensorManager.TAG;
                        Rlog.d(str, "onSensorChanged:" + state + ", processSwitch");
                        processSwitch();
                        processWifiSwitch();
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == EVENT_OEM_DATA_DELAY) {
                synchronized (this) {
                    if (this.mLastDataState != PhoneState.IDLE) {
                        if (!this.mIsProximListen) {
                            registerProximi();
                        }
                    }
                }
            } else if (i == EVENT_OEM_SCREEN_CHANGED) {
                AsyncResult arscreen = (AsyncResult) msg.obj;
                if (arscreen != null) {
                    onScreenChanged(((Boolean) arscreen.result).booleanValue());
                } else {
                    Rlog.w(OemProximitySensorManager.TAG, "EVENT_OEM_SCREEN_CHANGED error");
                }
            } else if (i == EVENT_PS_Wifi_STATE_CHANGED) {
                AsyncResult ret = (AsyncResult) msg.obj;
                if (ret != null) {
                    int[] iArr = new int[8];
                    int[] response = (int[]) ret.result;
                    this.mcheckmode = response[0];
                    this.mcheckband = response[1];
                }
                processWifiSwitch();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0037, code lost:
            r4.mListener.onDataChange(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
            if (com.android.internal.telephony.OemConstant.SWITCH_LOG == false) goto L_0x005b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0040, code lost:
            r1 = com.oppo.internal.telephony.rf.OemProximitySensorManager.TAG;
            android.telephony.Rlog.d(r1, "onDataActivity:" + r0 + ", processSwitch");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x005b, code lost:
            processSwitch();
            processWifiSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0061, code lost:
            return;
         */
        public void onDataActivity(int direction) {
            PhoneState state = getDataStateFromValue(direction);
            synchronized (this) {
                if (state != this.mLastDataState) {
                    this.mLastDataState = state;
                    if (this.mLastDataState == PhoneState.OFFHOOK) {
                        Message msg = Message.obtain();
                        msg.what = EVENT_OEM_DATA_DELAY;
                        sendMessageDelayed(msg, 3000);
                    } else {
                        removeMessages(EVENT_OEM_DATA_DELAY);
                        if (this.mLastCallState == PhoneState.IDLE && this.mIsProximListen && !this.mIsTestCard) {
                            unregisterProximi();
                        }
                    }
                }
            }
        }

        public void onCallStateChanged(int value, String incomingNumber) {
            PhoneState state = getCallStateFromValue(value);
            synchronized (this) {
                if (state != this.mLastCallState) {
                    this.mLastCallState = state;
                    if (state == PhoneState.OFFHOOK) {
                        if (!this.mIsProximListen) {
                            registerProximi();
                        }
                    } else if (this.mLastDataState == PhoneState.IDLE && this.mIsProximListen && !this.mIsTestCard) {
                        unregisterProximi();
                    }
                    this.mListener.onCallChange(state);
                    String str = OemProximitySensorManager.TAG;
                    Rlog.d(str, "onCallStateChanged:" + state + ", processSwitch");
                    processSwitch();
                    processWifiSwitch();
                }
            }
        }

        public void onScreenChanged(boolean isOn) {
            synchronized (this) {
                if (this.mLastScreenOn != isOn) {
                    this.mLastScreenOn = isOn;
                    if (this.mLastScreenOn && this.mIsProximListen && this.mLastDataState == PhoneState.IDLE && this.mLastCallState == PhoneState.IDLE && !this.mIsTestCard) {
                        unregisterProximi();
                    }
                    this.mListener.onScreenChange(isOn);
                    String str = OemProximitySensorManager.TAG;
                    Rlog.d(str, "onScreenChanged:" + isOn + ", processSwitch");
                    processSwitch();
                    processWifiSwitch();
                }
            }
        }

        public void onSubscriptionsChanged() {
            boolean lastTestCard = this.mIsTestCard;
            this.mListener.onSuscriptionChange();
            this.mIsTestCard = this.mListener.isTestCard();
            int numPhones = TelephonyManager.from(getContext()).getPhoneCount();
            if (OemConstant.SWITCH_LOG) {
                String str = OemProximitySensorManager.TAG;
                Rlog.d(str, "onSubscriptionsChanged, lastTestCard:" + lastTestCard + ", mIsTestCard:" + this.mIsTestCard);
            }
            boolean changed = lastTestCard != this.mIsTestCard;
            if (this.mLastSubId == null) {
                this.mLastSubId = new int[numPhones];
                for (int i = 0; i < numPhones; i++) {
                    this.mLastSubId[i] = PhoneFactory.getPhone(i).getSubId();
                }
                changed = true;
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemProximitySensorManager.TAG, "onSubscriptionsChanged, initialize");
                }
            } else {
                for (int i2 = 0; i2 < numPhones; i2++) {
                    if (this.mLastSubId[i2] != PhoneFactory.getPhone(i2).getSubId()) {
                        this.mLastSubId[i2] = PhoneFactory.getPhone(i2).getSubId();
                        changed = true;
                    }
                }
            }
            if (changed) {
                Rlog.d(OemProximitySensorManager.TAG, "onSubscriptionsChanged, changed");
                synchronized (this) {
                    if (this.mIsTestCard) {
                        unregisterPhone();
                        registerProximi();
                    } else {
                        unregisterProximi();
                        unregisterPhone();
                        registerPhone();
                    }
                }
                if (OemConstant.SWITCH_LOG) {
                    String str2 = OemProximitySensorManager.TAG;
                    Rlog.d(str2, "onSubscriptionsChanged, force re-processSwitch scene " + this.mLastSwitchScene + " to default");
                }
                this.mLastSwitchScene = -1;
                processSwitch();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
            r0 = com.oppo.internal.telephony.rf.OemProximitySensorManager.TAG;
            android.telephony.Rlog.d(r0, "onUsbStateChanged:" + r4 + ", processSwitch");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0029, code lost:
            r3.mListener.onUsbChange(r3.mLastUsbPresent);
            processSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0033, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
            if (com.android.internal.telephony.OemConstant.SWITCH_LOG == false) goto L_0x0029;
         */
        public void onUsbStateChanged(boolean usbPresent) {
            synchronized (this) {
                if (this.mLastUsbPresent != usbPresent) {
                    this.mLastUsbPresent = usbPresent;
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
            r0 = com.oppo.internal.telephony.rf.OemProximitySensorManager.TAG;
            android.telephony.Rlog.d(r0, "onChargerStateChanged:" + r4 + ", processSwitch");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0029, code lost:
            r3.mListener.onUsbChange(r3.mLastChargerPresent);
            processSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0033, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
            if (com.android.internal.telephony.OemConstant.SWITCH_LOG == false) goto L_0x0029;
         */
        public void onChargerStateChanged(boolean chargerPresent) {
            synchronized (this) {
                if (this.mLastChargerPresent != chargerPresent) {
                    this.mLastChargerPresent = chargerPresent;
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
            r0 = com.oppo.internal.telephony.rf.OemProximitySensorManager.TAG;
            android.telephony.Rlog.d(r0, "onOtgStateChanged:" + r4 + ", processSwitch");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0029, code lost:
            r3.mListener.onUsbChange(r3.mLastOtgPresent);
            processSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0033, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
            if (com.android.internal.telephony.OemConstant.SWITCH_LOG == false) goto L_0x0029;
         */
        public void onOtgStateChanged(boolean otgPresent) {
            synchronized (this) {
                if (this.mLastOtgPresent != otgPresent) {
                    this.mLastOtgPresent = otgPresent;
                }
            }
        }

        public void onAudioDeviceRouteStateChanged(int audioDeviceRoute) {
            synchronized (this) {
                if (this.mLastAudioDeviceRoute != audioDeviceRoute) {
                    this.mLastAudioDeviceRoute = audioDeviceRoute;
                    String str = OemProximitySensorManager.TAG;
                    Rlog.d(str, "onAudioDeviceRouteStateChanged:" + audioDeviceRoute + ", processSwitch");
                    this.mListener.onAudioDeviceRouteChanged(this.mLastAudioDeviceRoute);
                    processSwitch();
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
            r0 = com.oppo.internal.telephony.rf.OemProximitySensorManager.TAG;
            android.telephony.Rlog.d(r0, "onHeadsetStateChanged:" + r4 + ", processSwitch");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0029, code lost:
            r3.mListener.onHeadsetChange(r3.mLastHeadsetPresent);
            processSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0033, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
            if (com.android.internal.telephony.OemConstant.SWITCH_LOG == false) goto L_0x0029;
         */
        public void onHeadsetStateChanged(boolean headsetPresent) {
            synchronized (this) {
                if (this.mLastHeadsetPresent != headsetPresent) {
                    this.mLastHeadsetPresent = headsetPresent;
                }
            }
        }

        public void onWifiStateChanged(int wifiState) {
            synchronized (this) {
                if (this.mLastWifiState != wifiState) {
                    this.mLastWifiState = wifiState;
                    String str = OemProximitySensorManager.TAG;
                    Rlog.d(str, "onWifiStateChanged:" + wifiState + ", processSwitch");
                    this.mListener.onWifiChange(this.mLastWifiState);
                    processSwitch();
                }
            }
        }

        public void onEngineerModeChanged() {
            Rlog.d(OemProximitySensorManager.TAG, "onEngineerModeChanged, processSwitch");
            processSwitch();
        }

        private void processWifiSwitch() {
            boolean result;
            SwitchWifiSar state;
            if ((OemConstant.EXP_VERSION || OemConstant.needSetSarForWifi()) && !OemConstant.isSupportWifiSingleSar()) {
                synchronized (this) {
                    SwitchWifiSar switchWifiSar = SwitchWifiSar.SAR_OFF;
                    boolean isSensorSwitchOn = false;
                    if (OemConstant.needSetSarForWifi()) {
                        String country = OemConstant.getCountryForWifi();
                        String Project = OemConstant.getProjectForWifi();
                        String str = OemProximitySensorManager.TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("mLastSensorState == SensorState.NEAR=");
                        sb.append(this.mLastSensorState == SensorState.NEAR);
                        Rlog.d(str, sb.toString());
                        if (this.mIsTestCard || (!this.mIsTestCard && !this.mLastScreenOn && (this.mLastDataState == PhoneState.OFFHOOK || this.mLastCallState == PhoneState.OFFHOOK))) {
                            isSensorSwitchOn = true;
                        }
                        SwitchWifiSar state2 = isSensorSwitchOn ? SwitchWifiSar.SAR_ON_Group1 : SwitchWifiSar.SAR_OFF;
                        if (this.mLastWifiSar != state2) {
                            this.mLastWifiSar = state2;
                            if (OemConstant.SWITCH_LOG) {
                                Rlog.d(OemProximitySensorManager.TAG, "mLastWifiSar.ordinal()=" + this.mLastWifiSar.ordinal() + ",country=" + country + ",Project=" + Project);
                            }
                            result = OemConstant.runExecCmd(this.mLastWifiSar.ordinal(), country, Project);
                        } else {
                            return;
                        }
                    } else {
                        if (OemConstant.SWITCH_LOG) {
                            String str2 = OemProximitySensorManager.TAG;
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("(mLastDataState == PhoneState.OFFHOOK)=");
                            sb2.append(this.mLastDataState == PhoneState.OFFHOOK);
                            sb2.append(",(mLastCallState == PhoneState.OFFHOOK)=");
                            sb2.append(this.mLastCallState == PhoneState.OFFHOOK);
                            sb2.append(",(mLastSensorState == SensorState.NEAR)=");
                            sb2.append(this.mLastSensorState == SensorState.NEAR);
                            sb2.append(",mIsNormal5=");
                            sb2.append(this.mIsNormal5);
                            sb2.append(",mIsNormal24=");
                            sb2.append(this.mIsNormal24);
                            sb2.append(",mLastScreenOn=");
                            sb2.append(this.mLastScreenOn);
                            Rlog.d(str2, sb2.toString());
                        }
                        if ((this.mLastDataState == PhoneState.OFFHOOK || this.mLastCallState == PhoneState.OFFHOOK) && true == this.mIsNormal5) {
                            isSensorSwitchOn = true;
                        }
                        if (!isSensorSwitchOn) {
                            state = SwitchWifiSar.SAR_OFF;
                        } else if (this.mLastSensorState != SensorState.NEAR || this.mLastScreenOn) {
                            state = SwitchWifiSar.SAR_ON_Group2;
                        } else {
                            state = SwitchWifiSar.SAR_ON_Group1;
                        }
                        if (this.mLastWifiSar != state) {
                            this.mLastWifiSar = state;
                            result = OemConstant.setWifiSar(this.mLastWifiSar.ordinal());
                        } else {
                            return;
                        }
                    }
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "processWifiSwitch:" + this.mLastWifiSar + ",test:" + this.mIsTestCard + ",result:" + result);
                    }
                }
            }
        }

        private void processSwitch() {
            int scene;
            int scene2;
            int scene3;
            int scene4;
            int scene5;
            int scene6;
            synchronized (this) {
                if (OemConstant.usePSensorForSarDetect(getContext())) {
                    if (this.mLastSensorState != SensorState.NEAR || (!this.mIsTestCard && ((this.mIsTestCard || this.mLastDataState != PhoneState.OFFHOOK) && (this.mLastCallState != PhoneState.OFFHOOK || this.mLastScreenOn)))) {
                        scene = 0 & -2;
                    } else {
                        scene = 0 | 1;
                    }
                } else if (this.mLastAudioDeviceRoute == 1) {
                    scene = 0 | 1;
                } else {
                    scene = 0 & -2;
                }
                String fakeProxiState = SystemProperties.get("core.oppo.network.fake_near", "-1");
                if (this.mIsTestCard) {
                    if (fakeProxiState.equals("1")) {
                        scene |= 1;
                        if (OemConstant.SWITCH_LOG) {
                            Rlog.d(OemProximitySensorManager.TAG, "processSwitch probe, engineermode fake head");
                        }
                    } else if (fakeProxiState.equals("0")) {
                        scene &= -2;
                        if (OemConstant.SWITCH_LOG) {
                            Rlog.d(OemProximitySensorManager.TAG, "processSwitch probe, engineermode fake body");
                        }
                    }
                }
                if (this.mLastUsbPresent) {
                    scene2 = scene | 2;
                } else {
                    scene2 = scene & -3;
                }
                if (this.mLastOtgPresent) {
                    scene3 = scene2 | 4;
                } else {
                    scene3 = scene2 & -5;
                }
                if (this.mLastChargerPresent) {
                    scene4 = scene3 | 8;
                } else {
                    scene4 = scene3 & -9;
                }
                if (this.mLastHeadsetPresent) {
                    scene5 = scene4 | 16;
                } else {
                    scene5 = scene4 & -17;
                }
                String testWifiState = SystemProperties.get("core.oppo.network.wifistate", "-1");
                if (testWifiState.equals("1")) {
                    scene6 = scene5 | 32;
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "processSwitch probe, engineermode fake wifi 2.4G");
                    }
                } else if (testWifiState.equals(RegionLockConstant.TEST_OP_CUANDCMCC)) {
                    scene6 = scene5 | 64;
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "processSwitch probe, engineermode fake wifi 5G");
                    }
                } else if (testWifiState.equals("3")) {
                    scene6 = scene5 | 32 | 64;
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "processSwitch probe, engineermode fake wifi 2.4 and 5G");
                    }
                } else {
                    scene6 = this.mLastWifiState == 1 ? scene5 | 32 : this.mLastWifiState == 2 ? scene5 | 64 : this.mLastWifiState == 3 ? scene5 | 32 | 64 : scene5 & -33 & -65;
                }
                if (this.mLastSwitchScene == scene6) {
                    if (OemConstant.SWITCH_LOG) {
                        String str = OemProximitySensorManager.TAG;
                        Rlog.d(str, "processSwitch probe, return, duplicated scene: " + scene6);
                    }
                    return;
                }
                this.mLastSwitchScene = scene6;
                if (SystemProperties.get("core.oppo.network.bypass_sar", "-1").equals("1")) {
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.d(OemProximitySensorManager.TAG, "processSwitch probe, return, engineermode bypass sar scene");
                    }
                    return;
                }
                boolean isEngSar = SystemProperties.get("core.oppo.network.eng_sar", "-1").equals("1");
                if (this.mIsTestCard && !isEngSar) {
                    if (OemProximitySensorManager.getSwtpStatus()) {
                        if (!this.mSarStatusReseted) {
                            if (PhoneFactory.getDefaultPhone() != null) {
                                ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, PhoneFactory.getDefaultPhone())).oppoSetSarRfStateByScene(-1);
                            }
                            this.mSarStatusReseted = true;
                        }
                        if (OemConstant.SWITCH_LOG) {
                            Rlog.d(OemProximitySensorManager.TAG, "processSwitch probe, return, testsim default conductive state");
                        }
                        return;
                    }
                    this.mSarStatusReseted = false;
                }
                String str2 = OemProximitySensorManager.TAG;
                Rlog.d(str2, "processSwitch, onSwitchChange:" + this.mLastSwitchScene + " /testcard:" + this.mIsTestCard);
                this.mListener.onSwitchChange(this.mLastSwitchScene);
            }
        }

        private SensorState getStateFromValue(float value) {
            return (value > FAR_THRESHOLD || value == this.mMaxValue) ? SensorState.FAR : SensorState.NEAR;
        }

        private PhoneState getDataStateFromValue(int value) {
            return (value == 1 || value == 2 || value == 3) ? PhoneState.OFFHOOK : PhoneState.IDLE;
        }

        private PhoneState getCallStateFromValue(int value) {
            return value == 0 ? PhoneState.IDLE : PhoneState.OFFHOOK;
        }
    }

    public OemProximitySensorManager(Context context) {
        this(context, new IOemListener() {
            /* class com.oppo.internal.telephony.rf.OemProximitySensorManager.AnonymousClass1 */
            boolean testCard = false;

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onSensorChange(SensorState state) {
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onDataChange(PhoneState state) {
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onCallChange(PhoneState state) {
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onScreenChange(boolean isOn) {
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onUsbChange(boolean usbPresent) {
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onAudioDeviceRouteChanged(int audioDeviceRoute) {
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onHeadsetChange(boolean headsetPresent) {
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onWifiChange(int wifiState) {
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onSuscriptionChange() {
                this.testCard = initTestCard();
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public void onSwitchChange(int state) {
                if (PhoneFactory.getDefaultPhone() != null) {
                    ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, PhoneFactory.getDefaultPhone())).oppoSetSarRfStateByScene(state);
                }
            }

            @Override // com.oppo.internal.telephony.rf.OemProximitySensorManager.IOemListener
            public boolean isTestCard() {
                return this.testCard;
            }

            private synchronized boolean initTestCard() {
                for (Phone phone : PhoneFactory.getPhones()) {
                    AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone);
                    if (tmpPhone != null && tmpPhone.is_test_card()) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public OemProximitySensorManager(Context context, IOemListener listener) {
        this.mProximitySensorListener = null;
        SensorManager sensorManager = (SensorManager) context.getSystemService("sensor");
        Sensor proximitySensor = sensorManager.getDefaultSensor(8);
        if (proximitySensor == null) {
            this.mProximitySensorListener = null;
        } else {
            initValueByReflection();
            this.mProximitySensorListener = new ProximitySensorEventListener(sensorManager, proximitySensor, listener);
            this.mProximitySensorListener.register();
        }
        Rlog.d(TAG, "OemProximitySensorManager: init and release first");
        OemConstant.setWifiSar(SwitchWifiSar.SAR_OFF.ordinal());
    }

    private void initValueByReflection() {
        Object obj = ReflectionHelper.getDeclaredField(WifiManager.class, "android.net.wifi.WifiManager", "WIFI_AP_CHANNEL_CHANGED_ACTION");
        WIFI_AP_CHANNEL_CHANGED_ACTION = obj != null ? (String) obj : "android.net.wifi.WIFI_AP_CHANNEL_CHANGED";
        Object obj2 = ReflectionHelper.getDeclaredField(WifiManager.class, "android.net.wifi.WifiManager", "EXTRA_WIFI_AP_WORKING_FREQUENCY");
        EXTRA_WIFI_AP_WORKING_FREQUENCY = obj2 != null ? (String) obj2 : "wifi_ap_working_frequency";
        Object obj3 = ReflectionHelper.getDeclaredField(WifiConfiguration.class, "android.net.wifi.WifiConfiguration", "AP_BAND_DUAL");
        AP_BAND_DUAL = obj3 != null ? ((Integer) obj3).intValue() : 2;
        Object obj4 = ReflectionHelper.getDeclaredField(WifiManager.class, "android.net.wifi.WifiManager", "WIFI_SHARING_STATE_CHANGED_ACTION");
        WIFI_SHARING_STATE_CHANGED_ACTION = obj4 != null ? (String) obj4 : "oppo.intent.action.wifi.WIFI_SHARING_STATE_CHANGED";
        Object obj5 = ReflectionHelper.getDeclaredField(WifiManager.class, "android.net.wifi.WifiManager", "WIFI_SHARING_STATE_ENABLED");
        WIFI_SHARING_STATE_ENABLED = obj5 != null ? ((Integer) obj5).intValue() : OppoRIL.SYS_OEM_NW_DIAG_CAUSE_DATA_STALL_ERROR;
    }

    public static synchronized OemProximitySensorManager getDefault(Context context) {
        OemProximitySensorManager oemProximitySensorManager;
        synchronized (OemProximitySensorManager.class) {
            if (sInstance == null) {
                sInstance = new OemProximitySensorManager(context);
            }
            oemProximitySensorManager = sInstance;
        }
        return oemProximitySensorManager;
    }

    public void register() {
        this.mProximitySensorListener.register();
    }

    public void unregister() {
        ProximitySensorEventListener proximitySensorEventListener = this.mProximitySensorListener;
        if (proximitySensorEventListener != null) {
            proximitySensorEventListener.unregister();
        }
    }

    public static boolean getSwtpStatus() {
        FileInputStream inputStream = null;
        StringBuilder sb = new StringBuilder("");
        try {
            FileInputStream inputStream2 = new FileInputStream("proc/swtp_status_value");
            byte[] buffer = new byte[2];
            for (int len = inputStream2.read(buffer); len > 0; len = inputStream2.read(buffer)) {
                sb.append(new String(buffer, 0, len));
            }
            inputStream2.close();
            try {
                inputStream2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e2) {
            if (OemConstant.SWITCH_LOG) {
                Rlog.d(TAG, "SWTP proc node does not exist");
            }
            if (0 != 0) {
                inputStream.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
        if (sb.toString().length() <= 1 || !"1".equals(sb.toString().substring(0, 1))) {
            if (OemConstant.SWITCH_LOG) {
                Rlog.d(TAG, "SWTP status false");
            }
            return false;
        }
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(TAG, "SWTP status true");
        }
        return true;
    }
}
