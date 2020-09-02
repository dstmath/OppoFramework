package com.oppo.internal.telephony.rf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.OppoRIL;
import com.oppo.internal.telephony.OppoTelephonyController;
import com.oppo.internal.telephony.explock.RegionLockPlmnListParser;

public class WifiOemProximitySensorManager {
    private static int AP_BAND_DUAL = 0;
    private static String EXTRA_WIFI_AP_WORKING_FREQUENCY = null;
    private static final String LOG_TAG = "Wifisar";
    static String TAG = "WifiOemProximitySensor";
    private static String WIFI_AP_CHANNEL_CHANGED_ACTION = null;
    protected static final int WIFI_SAR_EXEC_6771 = 1;
    protected static final int WIFI_SAR_EXEC_6779 = 2;
    /* access modifiers changed from: private */
    public static String WIFI_SHARING_STATE_CHANGED_ACTION;
    /* access modifiers changed from: private */
    public static int WIFI_SHARING_STATE_ENABLED;
    /* access modifiers changed from: private */
    public static final String[] mCeCountryList = {"SG", "ID", "KZ", "NP", "NG", "IQ", "HK", "MY", "TR", "MM", "EG", "JO", "TH", "AU", "PK", "MA", "KE", "LB", "KH", "NZ", "LK", "DZ", "SA", "PS", "PH", "UA", "BD", "TN", "AE", "UZ"};
    /* access modifiers changed from: private */
    public static final String[] mEuropeCountryList = {"AT", "BE", "BG", "CY", "HR", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "GB", "SE", "CH"};
    /* access modifiers changed from: private */
    public static final String[] mFccCountryList = {"US", "MA"};
    /* access modifiers changed from: private */
    public static Handler mHandler;
    private static HandlerThread mHandlerThread;
    public static WifiManager mWifiManager = null;
    static WifiOemProximitySensorManager sInstance = null;
    ProximitySensorEventListener mProximitySensorListener;

    public interface IOemListener {
        boolean isTestCard();

        void onCallChange(PhoneState phoneState);

        void onDataChange(PhoneState phoneState);

        void onSensorChange(SensorState sensorState);

        void onSreenChange(boolean z);
    }

    public enum PhoneState {
        IDLE,
        OFFHOOK
    }

    public enum SensorState {
        FAR,
        NEAR
    }

    public enum SwitchWifiSar {
        SAR_OFF,
        SAR_ON_Group1,
        SAR_ON_Group2,
        SAR_ON_Group3,
        SAR_ON_Group4,
        SAR_ON_Group5,
        SAR_ON_Group6,
        SAR_ON_Group7,
        SAR_ON_Group8,
        SAR_ON_Group9,
        SAR_ON_Group10,
        SAR_ON_Group11,
        SAR_ON_Group12,
        SAR_ON_Group13,
        SAR_ON_Group14,
        SAR_ON_Group15,
        SAR_ON_Group16,
        SAR_ON_Group17,
        SAR_ON_Group18,
        SAR_ON_Group19,
        SAR_ON_Group20,
        SAR_ON_Group21,
        SAR_ON_Group22,
        SAR_ON_Group23,
        SAR_ON_Group24,
        SAR_ON_Group25,
        SAR_ON_Group26,
        SAR_ON_Group27,
        SAR_ON_Group28,
        SAR_ON_Group29,
        SAR_ON_Group30,
        SAR_ON_Group31,
        SAR_ON_Group32
    }

    public enum WifiState {
        CLOSED,
        WIFI_CONCURRENT,
        WIFI_2P4GHZ,
        WIFI_5GHZ
    }

    protected class OemWifiSarHandler extends Handler {
        public OemWifiSarHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                OemConstant.setWifiSar(msg.arg1);
            } else if (i == 2) {
                Bundle date = msg.getData();
                OemConstant.runExecCmd(date.getInt("wifisar"), date.getString(RegionLockPlmnListParser.PlmnCodeEntry.COUNTRY_ATTR), date.getString("project"));
            } else if (OemConstant.SWITCH_LOG) {
                Log.d(WifiOemProximitySensorManager.LOG_TAG, "msg is error");
            }
        }
    }

    private static class ProximitySensorEventListener extends Handler implements SensorEventListener {
        private static final String ACTION_SAR_UPDATE_BY_ENG = "oppo.intent.action.SAR_UPDATE_BY_ENG";
        protected static final int EVENT_OEM_DATA_DELAY = 297;
        protected static final int EVENT_OEM_DELAY_REGISTER_PROXIMI = 299;
        protected static final int EVENT_OEM_SCREEN_CHANGED = 298;
        private static final float FAR_THRESHOLD = 5.0f;
        protected static final int TIMER_DATA_DELAY = 3000;
        private static int WIFI_SAR_TEST_MODEM_WORK_TYPE_4G_MODE = 2;
        private static int WIFI_SAR_TEST_MODEM_WORK_TYPE_5G_MODE = 3;
        private static int WIFI_SAR_TEST_MODEM_WORK_TYPE_DISABLE = 1;
        private static int WIFI_SAR_TEST_MODEM_WORK_TYPE_FOLLOW = 0;
        private static int WIFI_SAR_TEST_WIFI_CHANNEL_TYPE_DISABLE = 0;
        /* access modifiers changed from: private */
        public static int WIFI_SAR_TEST_WIFI_CHANNEL_TYPE_FOLLOW = -1;
        /* access modifiers changed from: private */
        public int mApBand = -1;
        private AudioManager mAudioManager = null;
        private int mConcurenceWifiState = 0;
        /* access modifiers changed from: private */
        public boolean mIsConnected = false;
        private boolean mIsHotspot24 = false;
        private boolean mIsHotspot5 = false;
        /* access modifiers changed from: private */
        public boolean mIsNormal24 = false;
        /* access modifiers changed from: private */
        public boolean mIsNormal5 = false;
        private boolean mIsP2P24 = false;
        private boolean mIsP2P5 = false;
        private volatile boolean mIsPhoneListen = false;
        private volatile boolean mIsProximListen = false;
        /* access modifiers changed from: private */
        public volatile boolean mIsTestCard = false;
        private boolean mIsWifistate24 = false;
        private boolean mIsWifistate5 = false;
        private int mLastCallSlotId = -1;
        /* access modifiers changed from: private */
        public PhoneState mLastCallState;
        /* access modifiers changed from: private */
        public PhoneState mLastDataState;
        /* access modifiers changed from: private */
        public volatile boolean mLastScreenOn;
        private SensorState mLastSensorState;
        private volatile SwitchWifiSar mLastWifiSar;
        private final IOemListener mListener;
        private final float mMaxValue;
        private OemReceiver mOemReceiver = null;
        /* access modifiers changed from: private */
        public WifiP2pGroup mP2pGroup = null;
        /* access modifiers changed from: private */
        public boolean mP2pStart = false;
        private PhoneStateListener[] mPhoneStateListeners = null;
        private final Sensor mProximitySensor;
        private final SensorManager mSensorManager;
        /* access modifiers changed from: private */
        public int mSoftApFreq = -1;
        /* access modifiers changed from: private */
        public boolean mSoftApStart = false;
        /* access modifiers changed from: private */
        public int mWifiSarTestChanel2G = -1;
        /* access modifiers changed from: private */
        public int mWifiSarTestChanel5G = -1;
        /* access modifiers changed from: private */
        public String mWifiSarTestCountryCode = "";
        /* access modifiers changed from: private */
        public boolean mWifiSarTestEnable = false;
        /* access modifiers changed from: private */
        public int mWifiSarTestModeWorkType = -1;
        /* access modifiers changed from: private */
        public boolean mWifiShareInDiffChannel = false;

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

        protected class OemReceiver extends BroadcastReceiver {
            private static final String ACTION_WIFI_SAR_TEST_CFG = "oppo.intent.action.WIFI_SAR_TEST_CONFIG";
            private static final String CFG_COUNTRY_CODE = "countryCode";
            private static final String CFG_MODEM_TYPE = "modemWorkType";
            private static final String CFG_SAR_ENABLE = "enableSarTest";
            private static final String CFG_WIFI_CHANNEL_2G = "wifiChannel2G";
            private static final String CFG_WIFI_CHANNEL_5G = "wifiChannel5G";
            ProximitySensorEventListener mProximityListener;

            public OemReceiver(ProximitySensorEventListener orienListener) {
                this.mProximityListener = orienListener;
                ProximitySensorEventListener.this.registerPhone();
            }

            /* JADX WARNING: Code restructure failed: missing block: B:108:0x0253, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ac, code lost:
                return;
             */
            public void onReceive(Context context, Intent intent) {
                String action;
                if (intent != null && (action = intent.getAction()) != null) {
                    synchronized (this) {
                        boolean isModemWork = true;
                        if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                            if (OemConstant.SWITCH_LOG) {
                                Log.d(WifiOemProximitySensorManager.TAG, "Ash recevice WIFI_AP_STATE_CHANGED_ACTION");
                            }
                            WifiConfiguration mWifiConfiguration = null;
                            if (intent.getIntExtra("wifi_state", 11) == 13) {
                                boolean unused = ProximitySensorEventListener.this.mSoftApStart = true;
                            } else {
                                boolean unused2 = ProximitySensorEventListener.this.mSoftApStart = false;
                                int unused3 = ProximitySensorEventListener.this.mSoftApFreq = -1;
                            }
                            int unused4 = ProximitySensorEventListener.this.mApBand = -1;
                            if (ProximitySensorEventListener.this.mSoftApStart) {
                                if (WifiOemProximitySensorManager.mWifiManager != null) {
                                    try {
                                        mWifiConfiguration = WifiOemProximitySensorManager.mWifiManager.getWifiApConfiguration();
                                    } catch (Exception e) {
                                        Rlog.e(WifiOemProximitySensorManager.TAG, "WIFI_AP_STATE_CHANGED_ACTION getWifiApConfiguration Exception");
                                    }
                                    if (mWifiConfiguration != null) {
                                        int unused5 = ProximitySensorEventListener.this.mApBand = mWifiConfiguration.apBand;
                                        Rlog.d(WifiOemProximitySensorManager.TAG, "mApBand " + ProximitySensorEventListener.this.mApBand);
                                    }
                                } else {
                                    Rlog.e(WifiOemProximitySensorManager.TAG, "mWifiManager is NULL!");
                                }
                            }
                            ProximitySensorEventListener.this.wifiChangeState();
                            ProximitySensorEventListener.this.processWifiSwitch();
                        } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                            if (OemConstant.SWITCH_LOG) {
                                Log.d(WifiOemProximitySensorManager.TAG, "Ash recevice WIFI_STATE_CHANGED_ACTION");
                            }
                        } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                            if (OemConstant.SWITCH_LOG) {
                                Log.d(WifiOemProximitySensorManager.TAG, "Recevice NETWORK_STATE_CHANGED_ACTION");
                            }
                            WifiInfo wifiInfo = null;
                            if (WifiOemProximitySensorManager.mWifiManager != null) {
                                wifiInfo = WifiOemProximitySensorManager.mWifiManager.getConnectionInfo();
                            }
                            NetworkInfo networkinfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                            if (networkinfo != null) {
                                boolean unused6 = ProximitySensorEventListener.this.mIsConnected = networkinfo.isConnected();
                            }
                            if (OemConstant.SWITCH_LOG) {
                                Log.d(WifiOemProximitySensorManager.TAG, "mIsConnected: " + ProximitySensorEventListener.this.mIsConnected);
                            }
                            if (!(ProximitySensorEventListener.this.mLastDataState == PhoneState.OFFHOOK || ProximitySensorEventListener.this.mLastCallState == PhoneState.OFFHOOK)) {
                                isModemWork = false;
                            }
                            if (!ProximitySensorEventListener.this.mIsTestCard) {
                                if (ProximitySensorEventListener.this.mLastScreenOn || !ProximitySensorEventListener.this.mIsConnected || !isModemWork) {
                                    ProximitySensorEventListener.this.unregisterProximi();
                                } else {
                                    ProximitySensorEventListener.this.registerProximi();
                                }
                            } else if (ProximitySensorEventListener.this.mIsConnected) {
                                ProximitySensorEventListener.this.registerProximi();
                            } else {
                                ProximitySensorEventListener.this.unregisterProximi();
                            }
                            if (ProximitySensorEventListener.this.mIsConnected) {
                                ProximitySensorEventListener.this.registerPhone();
                            }
                            if (wifiInfo != null) {
                                int haveNetworkId = wifiInfo.getNetworkId();
                                if (OemConstant.SWITCH_LOG) {
                                    Log.d(WifiOemProximitySensorManager.TAG, "haveNetworkId: " + haveNetworkId);
                                }
                                if (-1 != haveNetworkId) {
                                    boolean unused7 = ProximitySensorEventListener.this.mIsNormal24 = wifiInfo.is24GHz();
                                    boolean unused8 = ProximitySensorEventListener.this.mIsNormal5 = wifiInfo.is5GHz();
                                    if (OemConstant.SWITCH_LOG) {
                                        Log.d(WifiOemProximitySensorManager.TAG, "mIsNormal24: " + ProximitySensorEventListener.this.mIsNormal24 + " mIsNormal5: " + ProximitySensorEventListener.this.mIsNormal5);
                                    }
                                    ProximitySensorEventListener.this.processWifiSwitch();
                                } else {
                                    boolean unused9 = ProximitySensorEventListener.this.mIsNormal24 = false;
                                    boolean unused10 = ProximitySensorEventListener.this.mIsNormal5 = false;
                                }
                            } else {
                                boolean unused11 = ProximitySensorEventListener.this.mIsNormal24 = false;
                                boolean unused12 = ProximitySensorEventListener.this.mIsNormal5 = false;
                                if (OemConstant.SWITCH_LOG) {
                                    Log.d(WifiOemProximitySensorManager.TAG, "Ash info is NULL!");
                                }
                            }
                            ProximitySensorEventListener.this.wifiChangeState();
                            ProximitySensorEventListener.this.processWifiSwitch();
                        } else if (action.equals(WifiOemProximitySensorManager.WIFI_SHARING_STATE_CHANGED_ACTION)) {
                            if (OemConstant.SWITCH_LOG) {
                                Log.d(WifiOemProximitySensorManager.TAG, "Recevice WIFI_SHARING_STATE_CHANGED_ACTION");
                            }
                            int wifiSharingState = intent.getIntExtra("wifi_state", 14);
                            if (OemConstant.SWITCH_LOG) {
                                Log.d(WifiOemProximitySensorManager.TAG, "wifiSharingState: " + wifiSharingState);
                            }
                            if (wifiSharingState == WifiOemProximitySensorManager.WIFI_SHARING_STATE_ENABLED && ProximitySensorEventListener.this.mIsNormal5 && Build.HARDWARE.equals("mt6779")) {
                                boolean unused13 = ProximitySensorEventListener.this.mWifiShareInDiffChannel = true;
                                ProximitySensorEventListener.this.processWifiSwitch();
                            } else {
                                boolean unused14 = ProximitySensorEventListener.this.mWifiShareInDiffChannel = false;
                            }
                        } else if (action.equals("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")) {
                            if (OemConstant.SWITCH_LOG) {
                                Log.d(WifiOemProximitySensorManager.TAG, "Recevice WIFI_P2P_CONNECTION_CHANGED_ACTION");
                            }
                            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                            WifiP2pGroup unused15 = ProximitySensorEventListener.this.mP2pGroup = (WifiP2pGroup) intent.getParcelableExtra("p2pGroupInfo");
                            if (ProximitySensorEventListener.this.mP2pGroup == null || networkInfo == null || !networkInfo.isConnected()) {
                                boolean unused16 = ProximitySensorEventListener.this.mP2pStart = false;
                            } else {
                                boolean unused17 = ProximitySensorEventListener.this.mP2pStart = true;
                                ProximitySensorEventListener.this.wifiChangeState();
                            }
                            ProximitySensorEventListener.this.processWifiSwitch();
                        } else if (action.equals(ACTION_WIFI_SAR_TEST_CFG)) {
                            Log.d(WifiOemProximitySensorManager.TAG, "Ash recevice ACTION_WIFI_SAR_TEST_CFG");
                            int enable = intent.getIntExtra(CFG_SAR_ENABLE, 0);
                            ProximitySensorEventListener proximitySensorEventListener = ProximitySensorEventListener.this;
                            if (enable == 0) {
                                isModemWork = false;
                            }
                            boolean unused18 = proximitySensorEventListener.mWifiSarTestEnable = isModemWork;
                            if (ProximitySensorEventListener.this.mWifiSarTestEnable) {
                                String unused19 = ProximitySensorEventListener.this.mWifiSarTestCountryCode = intent.getStringExtra(CFG_COUNTRY_CODE);
                                int unused20 = ProximitySensorEventListener.this.mWifiSarTestModeWorkType = intent.getIntExtra(CFG_MODEM_TYPE, -1);
                                int unused21 = ProximitySensorEventListener.this.mWifiSarTestChanel2G = intent.getIntExtra(CFG_WIFI_CHANNEL_2G, ProximitySensorEventListener.WIFI_SAR_TEST_WIFI_CHANNEL_TYPE_FOLLOW);
                                int unused22 = ProximitySensorEventListener.this.mWifiSarTestChanel5G = intent.getIntExtra(CFG_WIFI_CHANNEL_5G, ProximitySensorEventListener.WIFI_SAR_TEST_WIFI_CHANNEL_TYPE_FOLLOW);
                            }
                            ProximitySensorEventListener.this.processWifiSwitch();
                        } else if (action.equals(ProximitySensorEventListener.ACTION_SAR_UPDATE_BY_ENG)) {
                            Log.d(WifiOemProximitySensorManager.TAG, "onEngineerModeChanged, processWifiSwitch");
                            ProximitySensorEventListener.this.processWifiSwitch();
                        }
                    }
                }
            }

            public void register(Context context) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
                filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                filter.addAction("android.net.wifi.STATE_CHANGE");
                filter.addAction(WifiOemProximitySensorManager.WIFI_SHARING_STATE_CHANGED_ACTION);
                filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
                filter.addAction(ACTION_WIFI_SAR_TEST_CFG);
                filter.addAction(ProximitySensorEventListener.ACTION_SAR_UPDATE_BY_ENG);
                context.registerReceiver(this, filter);
            }

            public void unregister(Context context) {
                context.unregisterReceiver(this);
            }
        }

        public ProximitySensorEventListener(SensorManager sensorManager, Sensor proximitySensor, IOemListener listener) {
            this.mSensorManager = sensorManager;
            this.mProximitySensor = proximitySensor;
            this.mMaxValue = proximitySensor.getMaximumRange();
            this.mListener = listener;
            this.mLastSensorState = SensorState.FAR;
            this.mLastDataState = PhoneState.IDLE;
            this.mLastCallState = PhoneState.IDLE;
            this.mLastScreenOn = true;
            Context context = getContext();
            this.mLastWifiSar = SwitchWifiSar.SAR_OFF;
            int numPhones = TelephonyManager.from(context).getPhoneCount();
            this.mPhoneStateListeners = new OemPhoneStateListener[numPhones];
            for (int i = 0; i < numPhones; i++) {
                this.mPhoneStateListeners[i] = new OemPhoneStateListener(this, i);
            }
            this.mOemReceiver = new OemReceiver(this);
        }

        public static Context getContext() {
            return PhoneFactory.getPhone(0).getContext();
        }

        public void register() {
            registerSceeen();
            this.mOemReceiver.register(getContext());
        }

        public void unregister() {
            unregisterScreen();
            this.mOemReceiver.unregister(getContext());
        }

        public void registerSceeen() {
            OppoTelephonyController.getInstance(getContext()).registerForOemScreenChanged(this, EVENT_OEM_SCREEN_CHANGED, null);
        }

        public void unregisterScreen() {
            OppoTelephonyController.getInstance(getContext()).unregisterOemScreenChanged(this);
        }

        public void registerProximi() {
            if (!this.mIsProximListen) {
                if (OemConstant.SWITCH_LOG) {
                    Log.d(WifiOemProximitySensorManager.TAG, "registerProximi");
                }
                this.mIsProximListen = true;
                this.mSensorManager.registerListener(this, this.mProximitySensor, 2);
            }
        }

        public void unregisterProximi() {
            if (this.mIsProximListen) {
                if (OemConstant.SWITCH_LOG) {
                    Log.d(WifiOemProximitySensorManager.TAG, "unregisterProximi");
                }
                this.mIsProximListen = false;
                this.mSensorManager.unregisterListener(this);
            }
        }

        public void registerPhone() {
            if (!this.mIsPhoneListen) {
                Log.d(WifiOemProximitySensorManager.TAG, "registerPhone");
                this.mIsPhoneListen = true;
                TelephonyManager tm = TelephonyManager.from(PhoneFactory.getPhone(0).getContext());
                int numPhones = tm.getPhoneCount();
                for (int i = 0; i < numPhones; i++) {
                    int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(i);
                    if (SubscriptionController.getInstance().isActiveSubId(subId)) {
                        tm.createForSubscriptionId(subId).listen(this.mPhoneStateListeners[i], 160);
                    }
                }
            }
        }

        public void unregisterPhone() {
            if (this.mIsPhoneListen) {
                if (OemConstant.SWITCH_LOG) {
                    Log.d(WifiOemProximitySensorManager.TAG, "unregisterPhone");
                }
                this.mIsPhoneListen = false;
                TelephonyManager tm = TelephonyManager.from(PhoneFactory.getPhone(0).getContext());
                int numPhones = tm.getPhoneCount();
                for (int i = 0; i < numPhones; i++) {
                    tm.listen(this.mPhoneStateListeners[i], 0);
                }
            }
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.values != null && event.values.length != 0) {
                SensorState state = getStateFromValue(event.values[0]);
                if (OemConstant.SWITCH_LOG) {
                    String str = WifiOemProximitySensorManager.TAG;
                    Log.d(str, "onSensorChanged:" + state);
                }
                synchronized (this) {
                    if (state != this.mLastSensorState) {
                        this.mLastSensorState = state;
                        this.mListener.onSensorChange(state);
                        processWifiSwitch();
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            return;
         */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_OEM_DATA_DELAY /*{ENCODED_INT: 297}*/:
                    synchronized (this) {
                        if (this.mLastDataState != PhoneState.IDLE) {
                            if (!this.mIsProximListen) {
                                registerProximi();
                                break;
                            }
                        } else {
                            return;
                        }
                    }
                    break;
                case EVENT_OEM_SCREEN_CHANGED /*{ENCODED_INT: 298}*/:
                    AsyncResult arscreen = (AsyncResult) msg.obj;
                    if (arscreen != null) {
                        boolean isOn = ((Boolean) arscreen.result).booleanValue();
                        if (OemConstant.SWITCH_LOG) {
                            String str = WifiOemProximitySensorManager.TAG;
                            Log.d(str, "EVENT_OEM_SCREEN_CHANGED " + isOn);
                        }
                        onSreenChanged(isOn);
                        return;
                    } else if (OemConstant.SWITCH_LOG) {
                        Log.w(WifiOemProximitySensorManager.TAG, "EVENT_OEM_SCREEN_CHANGED error");
                        return;
                    } else {
                        return;
                    }
                case EVENT_OEM_DELAY_REGISTER_PROXIMI /*{ENCODED_INT: 299}*/:
                    if (OemConstant.SWITCH_LOG) {
                        Log.d(WifiOemProximitySensorManager.TAG, "EVENT_OEM_DELAY_REGISTER_PROXIMI ");
                    }
                    registerProximi();
                    return;
                default:
                    return;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0049, code lost:
            r4.mListener.onDataChange(r0);
            processWifiSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0051, code lost:
            return;
         */
        public void onDataActivity(int direction) {
            PhoneState state = getDataStateFromValue(direction);
            String str = WifiOemProximitySensorManager.TAG;
            Log.d(str, "onDataActivity state:" + state);
            synchronized (this) {
                if (state != this.mLastDataState) {
                    this.mLastDataState = state;
                    if (this.mLastDataState == PhoneState.OFFHOOK) {
                        Message msg = Message.obtain();
                        msg.what = EVENT_OEM_DATA_DELAY;
                        sendMessageDelayed(msg, 3000);
                    } else {
                        removeMessages(EVENT_OEM_DATA_DELAY);
                        if (this.mLastCallState == PhoneState.IDLE && this.mIsProximListen) {
                            unregisterProximi();
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
            r4.mListener.onCallChange(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002f, code lost:
            if (com.android.internal.telephony.OemConstant.SWITCH_LOG == false) goto L_0x004c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0031, code lost:
            r1 = com.oppo.internal.telephony.rf.WifiOemProximitySensorManager.TAG;
            android.util.Log.d(r1, "onCallStateChanged:" + r0 + ", processSwitch");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x004c, code lost:
            processWifiSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x004f, code lost:
            return;
         */
        public void onCallStateChanged(int value, String incomingNumber) {
            PhoneState state = getCallStateFromValue(value);
            if (OemConstant.SWITCH_LOG) {
                String str = WifiOemProximitySensorManager.TAG;
                Log.d(str, "onCallStateChanged:" + state);
            }
            synchronized (this) {
                if (state != this.mLastCallState) {
                    this.mLastCallState = state;
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:22:0x003b, code lost:
            r4.mListener.onSreenChange(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0042, code lost:
            if (com.android.internal.telephony.OemConstant.SWITCH_LOG == false) goto L_0x005f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0044, code lost:
            r0 = com.oppo.internal.telephony.rf.WifiOemProximitySensorManager.TAG;
            android.util.Log.d(r0, "onSreenChange:" + r5 + ", processSwitch");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x005f, code lost:
            processWifiSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0062, code lost:
            return;
         */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x001d  */
        public void onSreenChanged(boolean isOn) {
            synchronized (this) {
                if (this.mLastScreenOn != isOn) {
                    this.mLastScreenOn = isOn;
                    if (this.mLastDataState != PhoneState.OFFHOOK) {
                        if (this.mLastCallState != PhoneState.OFFHOOK) {
                            if (!this.mIsTestCard) {
                                if (this.mLastScreenOn || !this.mIsConnected) {
                                    removeMessages(EVENT_OEM_DELAY_REGISTER_PROXIMI);
                                    unregisterProximi();
                                } else {
                                    Message msg = Message.obtain();
                                    msg.what = EVENT_OEM_DELAY_REGISTER_PROXIMI;
                                    sendMessageDelayed(msg, 2000);
                                }
                            }
                        }
                    }
                    if (!this.mIsTestCard) {
                    }
                }
            }
        }

        private boolean isEuropeCountry(String country) {
            if (country == null || country.isEmpty()) {
                Log.d(WifiOemProximitySensorManager.TAG, "country = null");
                return false;
            } else if (WifiOemProximitySensorManager.mEuropeCountryList == null) {
                Log.e(WifiOemProximitySensorManager.TAG, "mEuropeCountryList = null");
                return false;
            } else {
                for (int i = 0; i < WifiOemProximitySensorManager.mEuropeCountryList.length; i++) {
                    if (country.equalsIgnoreCase(WifiOemProximitySensorManager.mEuropeCountryList[i])) {
                        String str = WifiOemProximitySensorManager.TAG;
                        Log.d(str, "is Europe Country " + country);
                        return true;
                    }
                }
                return false;
            }
        }

        private boolean isCeCountry(String country) {
            if (country == null || country.isEmpty()) {
                Log.d(WifiOemProximitySensorManager.TAG, "ce country = null");
                return false;
            } else if (WifiOemProximitySensorManager.mCeCountryList == null) {
                Log.e(WifiOemProximitySensorManager.TAG, "mCeCountryList = null");
                return false;
            } else {
                for (int i = 0; i < WifiOemProximitySensorManager.mCeCountryList.length; i++) {
                    if (country.equalsIgnoreCase(WifiOemProximitySensorManager.mCeCountryList[i])) {
                        String str = WifiOemProximitySensorManager.TAG;
                        Log.d(str, "is ce Country " + country);
                        return true;
                    }
                }
                return false;
            }
        }

        private boolean isFccCountry(String country) {
            if (country == null || country.isEmpty()) {
                Log.d(WifiOemProximitySensorManager.TAG, "Fcc country = null");
                return false;
            } else if (WifiOemProximitySensorManager.mFccCountryList == null) {
                Log.e(WifiOemProximitySensorManager.TAG, "mCeCountryList = null");
                return false;
            } else {
                for (int i = 0; i < WifiOemProximitySensorManager.mFccCountryList.length; i++) {
                    if (country.equalsIgnoreCase(WifiOemProximitySensorManager.mFccCountryList[i])) {
                        String str = WifiOemProximitySensorManager.TAG;
                        Log.d(str, "is Fcc Country " + country);
                        return true;
                    }
                }
                return false;
            }
        }

        /* access modifiers changed from: private */
        public void wifiChangeState() {
            if (this.mP2pStart) {
                int freq = this.mP2pGroup.getFrequency();
                if (freq >= 2412 && freq <= 2472) {
                    this.mIsP2P5 = false;
                    this.mIsP2P24 = true;
                } else if (freq > 5000) {
                    this.mIsP2P5 = true;
                    this.mIsP2P24 = false;
                }
                String str = WifiOemProximitySensorManager.TAG;
                Log.d(str, " p2p freq = " + freq);
            }
            int freq2 = this.mApBand;
            if (freq2 == 1) {
                this.mIsHotspot24 = false;
                this.mIsHotspot5 = true;
            } else if (freq2 == 0) {
                this.mIsHotspot24 = true;
                this.mIsHotspot5 = false;
            } else {
                this.mIsHotspot24 = false;
                this.mIsHotspot5 = false;
            }
            if (this.mIsP2P24 || this.mIsHotspot24 || this.mIsNormal24) {
                this.mIsWifistate24 = true;
            } else {
                this.mIsWifistate24 = false;
            }
            if (this.mIsP2P5 || this.mIsHotspot5 || this.mIsNormal5) {
                this.mIsWifistate5 = true;
            } else {
                this.mIsWifistate5 = false;
            }
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Removed duplicated region for block: B:114:0x022e  */
        /* JADX WARNING: Removed duplicated region for block: B:117:0x0234  */
        /* JADX WARNING: Removed duplicated region for block: B:118:0x0255  */
        /* JADX WARNING: Removed duplicated region for block: B:121:0x025f  */
        /* JADX WARNING: Removed duplicated region for block: B:186:0x02f9  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x007a  */
        /* JADX WARNING: Removed duplicated region for block: B:390:0x04db A[DONT_GENERATE] */
        /* JADX WARNING: Removed duplicated region for block: B:392:0x04dd  */
        /* JADX WARNING: Removed duplicated region for block: B:393:0x0517  */
        /* JADX WARNING: Removed duplicated region for block: B:408:0x053e  */
        /* JADX WARNING: Removed duplicated region for block: B:418:0x0551  */
        /* JADX WARNING: Removed duplicated region for block: B:457:0x05cc  */
        /* JADX WARNING: Removed duplicated region for block: B:49:0x00e2  */
        /* JADX WARNING: Removed duplicated region for block: B:500:0x063d A[DONT_GENERATE] */
        /* JADX WARNING: Removed duplicated region for block: B:502:0x063f  */
        /* JADX WARNING: Removed duplicated region for block: B:52:0x00e8  */
        /* JADX WARNING: Removed duplicated region for block: B:93:0x0199  */
        public void processWifiSwitch() {
            boolean z;
            boolean isSensorSwitchOn;
            boolean isModemWork;
            boolean headSar;
            this.mIsTestCard = this.mListener.isTestCard();
            synchronized (this) {
                SystemProperties.get("ro.oppo.regionmark", "EX").equalsIgnoreCase("IN");
                SwitchWifiSar state = SwitchWifiSar.SAR_OFF;
                String country = "";
                if (WifiOemProximitySensorManager.mWifiManager != null) {
                    country = WifiOemProximitySensorManager.mWifiManager.getCountryCode();
                }
                String Project = OemConstant.getProjectForWifi();
                boolean fakeCardNear = this.mIsTestCard && this.mLastSensorState == SensorState.NEAR;
                boolean fakeCardFar = this.mIsTestCard && this.mLastSensorState == SensorState.FAR;
                boolean realCardNearScrOff = !this.mIsTestCard && this.mLastSensorState == SensorState.NEAR && !this.mLastScreenOn;
                if (this.mLastDataState != PhoneState.OFFHOOK) {
                    if (this.mLastCallState != PhoneState.OFFHOOK) {
                        z = false;
                        boolean isModemWork2 = z;
                        if (!OemConstant.SWITCH_LOG) {
                            String str = WifiOemProximitySensorManager.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append("processWifiSwitch mIsTestCard=");
                            sb.append(this.mIsTestCard);
                            sb.append(",realCardNear=");
                            sb.append(this.mLastSensorState == SensorState.NEAR);
                            sb.append(",DataState=");
                            sb.append(this.mLastDataState == PhoneState.OFFHOOK);
                            sb.append(",CallState=");
                            sb.append(this.mLastCallState == PhoneState.OFFHOOK);
                            sb.append(",country=");
                            sb.append(country);
                            sb.append(",Project=");
                            sb.append(Project);
                            sb.append(",mLastScreenOn=");
                            sb.append(this.mLastScreenOn);
                            Log.d(str, sb.toString());
                        }
                        if (this.mWifiSarTestEnable) {
                            country = this.mWifiSarTestCountryCode;
                            isModemWork2 = this.mWifiSarTestModeWorkType == WIFI_SAR_TEST_MODEM_WORK_TYPE_4G_MODE;
                            if (this.mWifiSarTestChanel2G != WIFI_SAR_TEST_WIFI_CHANNEL_TYPE_FOLLOW) {
                                this.mIsWifistate24 = this.mWifiSarTestChanel2G != WIFI_SAR_TEST_WIFI_CHANNEL_TYPE_DISABLE;
                                this.mIsNormal24 = this.mIsWifistate24;
                            } else {
                                this.mIsNormal24 = false;
                            }
                            if (this.mWifiSarTestChanel5G != WIFI_SAR_TEST_WIFI_CHANNEL_TYPE_FOLLOW) {
                                this.mIsWifistate5 = this.mWifiSarTestChanel5G != WIFI_SAR_TEST_WIFI_CHANNEL_TYPE_DISABLE;
                                this.mIsNormal5 = this.mIsWifistate5;
                                if (this.mIsWifistate5) {
                                    if (this.mWifiSarTestChanel5G < 5180 || this.mWifiSarTestChanel5G > 5320) {
                                        if (this.mWifiSarTestChanel5G < 5500 || this.mWifiSarTestChanel5G > 5700) {
                                            if (this.mWifiSarTestChanel5G < 5745 || this.mWifiSarTestChanel5G > 5825) {
                                                this.mIsWifistate5 = false;
                                            }
                                        }
                                    }
                                }
                            } else {
                                this.mIsWifistate5 = false;
                            }
                            Log.d(WifiOemProximitySensorManager.TAG, "countryCode: " + country + " isModemWork: " + isModemWork2 + " mIsWifistate24: " + this.mIsWifistate24 + " mIsWifistate5: " + this.mIsWifistate5);
                        }
                        if (!Build.HARDWARE.equals("mt6779")) {
                            String fakeProxiState = SystemProperties.get("core.oppo.network.fake_near", "-1");
                            if (this.mIsTestCard) {
                                if (OemConstant.SWITCH_LOG) {
                                    Log.d(WifiOemProximitySensorManager.TAG, "fakeProxiState=" + fakeProxiState);
                                }
                                if (fakeProxiState.equals("1")) {
                                    fakeCardNear = true;
                                    fakeCardFar = false;
                                } else if (fakeProxiState.equals("0")) {
                                    fakeCardNear = false;
                                    fakeCardFar = true;
                                }
                            }
                            if (OemConstant.SWITCH_LOG) {
                                Log.d(WifiOemProximitySensorManager.TAG, "processWifiSwitch1, fakeCardNear=" + fakeCardNear + ",realCardNearScrOff=" + realCardNearScrOff + ",isModemWork=" + isModemWork2 + ",mIsNormal24=" + this.mIsNormal24 + ",mIsNormal5=" + this.mIsNormal5);
                            }
                            if (!fakeCardNear) {
                                if (!realCardNearScrOff) {
                                    headSar = false;
                                    boolean isAirplaneModeOn = false;
                                    if (Settings.Global.getInt(getContext().getContentResolver(), "airplane_mode_on", 0) == 1) {
                                        isAirplaneModeOn = true;
                                    }
                                    if (!OemConstant.SWITCH_LOG) {
                                        Log.d(WifiOemProximitySensorManager.TAG, "processWifiSwitch2, headSar=" + headSar + ",bodySar=" + fakeCardFar);
                                    }
                                    if (!"IN".equals(country)) {
                                        if (headSar) {
                                            if (!isModemWork2 && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                                state = SwitchWifiSar.SAR_ON_Group1;
                                            } else if (!isModemWork2 && this.mIsWifistate24 && this.mIsWifistate5) {
                                                state = SwitchWifiSar.SAR_ON_Group2;
                                            } else if (isModemWork2 && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                                state = SwitchWifiSar.SAR_ON_Group3;
                                            } else if (isModemWork2 && this.mIsWifistate24 && this.mIsWifistate5) {
                                                state = SwitchWifiSar.SAR_ON_Group4;
                                            }
                                        } else if (isAirplaneModeOn && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                            state = SwitchWifiSar.SAR_ON_Group5;
                                        } else if (isAirplaneModeOn && this.mIsWifistate24 && this.mIsWifistate5) {
                                            state = SwitchWifiSar.SAR_ON_Group6;
                                        } else if (!isAirplaneModeOn && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                            state = SwitchWifiSar.SAR_ON_Group7;
                                        } else if (!isAirplaneModeOn && this.mIsWifistate24 && this.mIsWifistate5) {
                                            state = SwitchWifiSar.SAR_ON_Group8;
                                        }
                                    } else if (isCeCountry(country)) {
                                        if (headSar) {
                                            if (!isModemWork2 && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                                state = SwitchWifiSar.SAR_ON_Group9;
                                            } else if (!isModemWork2 && this.mIsWifistate24 && this.mIsWifistate5) {
                                                state = SwitchWifiSar.SAR_ON_Group10;
                                            } else if (isModemWork2 && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                                state = SwitchWifiSar.SAR_ON_Group11;
                                            } else if (isModemWork2 && this.mIsWifistate24 && this.mIsWifistate5) {
                                                state = SwitchWifiSar.SAR_ON_Group12;
                                            }
                                        } else if (isAirplaneModeOn && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                            state = SwitchWifiSar.SAR_ON_Group13;
                                        } else if (isAirplaneModeOn && this.mIsWifistate24 && this.mIsWifistate5) {
                                            state = SwitchWifiSar.SAR_ON_Group14;
                                        } else if (!isAirplaneModeOn && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                            state = SwitchWifiSar.SAR_ON_Group15;
                                        } else if (!isAirplaneModeOn && this.mIsWifistate24 && this.mIsWifistate5) {
                                            state = SwitchWifiSar.SAR_ON_Group16;
                                        }
                                    } else if (isFccCountry(country)) {
                                        if (headSar) {
                                            if (!isModemWork2 && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                                state = SwitchWifiSar.SAR_ON_Group17;
                                            } else if (!isModemWork2 && this.mIsWifistate24 && this.mIsWifistate5) {
                                                state = SwitchWifiSar.SAR_ON_Group18;
                                            } else if (isModemWork2 && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                                state = SwitchWifiSar.SAR_ON_Group19;
                                            } else if (isModemWork2 && this.mIsWifistate24 && this.mIsWifistate5) {
                                                state = SwitchWifiSar.SAR_ON_Group20;
                                            }
                                        } else if (isAirplaneModeOn && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                            state = SwitchWifiSar.SAR_ON_Group21;
                                        } else if (isAirplaneModeOn && this.mIsWifistate24 && this.mIsWifistate5) {
                                            state = SwitchWifiSar.SAR_ON_Group22;
                                        } else if (!isAirplaneModeOn && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                            state = SwitchWifiSar.SAR_ON_Group23;
                                        } else if (!isAirplaneModeOn && this.mIsWifistate24 && this.mIsWifistate5) {
                                            state = SwitchWifiSar.SAR_ON_Group24;
                                        }
                                    } else if (!isEuropeCountry(country)) {
                                        state = SwitchWifiSar.SAR_OFF;
                                    } else if (headSar) {
                                        if (!isModemWork2 && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                            state = SwitchWifiSar.SAR_ON_Group25;
                                        } else if (!isModemWork2 && this.mIsWifistate24 && this.mIsWifistate5) {
                                            state = SwitchWifiSar.SAR_ON_Group26;
                                        } else if (isModemWork2 && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                            state = SwitchWifiSar.SAR_ON_Group27;
                                        } else if (isModemWork2 && this.mIsWifistate24 && this.mIsWifistate5) {
                                            state = SwitchWifiSar.SAR_ON_Group28;
                                        }
                                    } else if (isAirplaneModeOn && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                        state = SwitchWifiSar.SAR_ON_Group29;
                                    } else if (isAirplaneModeOn && this.mIsWifistate24 && this.mIsWifistate5) {
                                        state = SwitchWifiSar.SAR_ON_Group30;
                                    } else if (!isAirplaneModeOn && ((this.mIsWifistate24 && !this.mIsWifistate5) || (!this.mIsWifistate24 && this.mIsWifistate5))) {
                                        state = SwitchWifiSar.SAR_ON_Group31;
                                    } else if (!isAirplaneModeOn && this.mIsWifistate24 && this.mIsWifistate5) {
                                        state = SwitchWifiSar.SAR_ON_Group32;
                                    }
                                    if (this.mLastWifiSar == state) {
                                        this.mLastWifiSar = state;
                                        Message msg = Message.obtain();
                                        msg.what = 2;
                                        Bundle data = new Bundle();
                                        data.putInt("wifisar", this.mLastWifiSar.ordinal());
                                        data.putString(RegionLockPlmnListParser.PlmnCodeEntry.COUNTRY_ATTR, country);
                                        data.putString("project", Project);
                                        msg.setData(data);
                                        WifiOemProximitySensorManager.mHandler.removeMessages(2);
                                        WifiOemProximitySensorManager.mHandler.sendMessage(msg);
                                    } else {
                                        return;
                                    }
                                }
                            }
                            headSar = true;
                            boolean isAirplaneModeOn2 = false;
                            if (Settings.Global.getInt(getContext().getContentResolver(), "airplane_mode_on", 0) == 1) {
                            }
                            if (!OemConstant.SWITCH_LOG) {
                            }
                            if (!"IN".equals(country)) {
                            }
                            if (this.mLastWifiSar == state) {
                            }
                        } else {
                            boolean headSar2 = false;
                            if (Build.HARDWARE.equals("mt6771")) {
                                if (!this.mIsTestCard) {
                                    if (!this.mIsTestCard) {
                                        if (!this.mIsNormal24) {
                                            if (this.mIsNormal5) {
                                            }
                                        }
                                    }
                                    isSensorSwitchOn = false;
                                    if (this.mLastDataState != PhoneState.OFFHOOK) {
                                        if (this.mLastCallState != PhoneState.OFFHOOK) {
                                            isModemWork = false;
                                            if (fakeCardNear || realCardNearScrOff) {
                                                headSar2 = true;
                                            }
                                            if (isSensorSwitchOn || !"18531".equals(Project)) {
                                                if (isSensorSwitchOn || !"19531".equals(Project)) {
                                                    state = SwitchWifiSar.SAR_OFF;
                                                } else if ("IN".equals(country)) {
                                                    if (!isModemWork && (this.mIsNormal24 || this.mIsNormal5)) {
                                                        state = headSar2 ? SwitchWifiSar.SAR_ON_Group3 : SwitchWifiSar.SAR_OFF;
                                                    } else if (isModemWork && (this.mIsNormal24 || this.mIsNormal5)) {
                                                        state = headSar2 ? SwitchWifiSar.SAR_ON_Group4 : fakeCardFar ? SwitchWifiSar.SAR_OFF : SwitchWifiSar.SAR_OFF;
                                                    }
                                                } else if (isModemWork || (!this.mIsNormal24 && !this.mIsNormal5)) {
                                                    if (isModemWork && (this.mIsNormal24 || this.mIsNormal5)) {
                                                        state = headSar2 ? SwitchWifiSar.SAR_ON_Group6 : fakeCardFar ? SwitchWifiSar.SAR_OFF : SwitchWifiSar.SAR_OFF;
                                                    }
                                                } else if (!headSar2) {
                                                    state = SwitchWifiSar.SAR_OFF;
                                                }
                                            } else if (isModemWork && this.mIsNormal24) {
                                                state = (this.mLastSensorState != SensorState.NEAR || this.mLastScreenOn) ? this.mLastSensorState == SensorState.FAR ? SwitchWifiSar.SAR_ON_Group2 : SwitchWifiSar.SAR_OFF : SwitchWifiSar.SAR_ON_Group1;
                                            } else if (isModemWork && this.mIsNormal5) {
                                                state = (this.mLastSensorState != SensorState.NEAR || this.mLastScreenOn) ? this.mLastSensorState == SensorState.FAR ? SwitchWifiSar.SAR_ON_Group2 : SwitchWifiSar.SAR_OFF : SwitchWifiSar.SAR_ON_Group1;
                                            } else if (this.mIsNormal24) {
                                                state = (this.mLastSensorState != SensorState.NEAR || this.mLastScreenOn) ? this.mLastSensorState == SensorState.FAR ? SwitchWifiSar.SAR_ON_Group2 : SwitchWifiSar.SAR_OFF : SwitchWifiSar.SAR_ON_Group1;
                                            } else {
                                                boolean z2 = this.mIsNormal5;
                                            }
                                            if (this.mLastWifiSar != state) {
                                                this.mLastWifiSar = state;
                                                Message msg2 = Message.obtain();
                                                msg2.what = 1;
                                                msg2.arg1 = this.mLastWifiSar.ordinal();
                                                WifiOemProximitySensorManager.mHandler.removeMessages(1);
                                                WifiOemProximitySensorManager.mHandler.sendMessage(msg2);
                                            } else {
                                                return;
                                            }
                                        }
                                    }
                                    isModemWork = true;
                                    headSar2 = true;
                                    if (isSensorSwitchOn) {
                                    }
                                    if (isSensorSwitchOn) {
                                    }
                                    state = SwitchWifiSar.SAR_OFF;
                                    if (this.mLastWifiSar != state) {
                                    }
                                }
                                isSensorSwitchOn = true;
                                if (this.mLastDataState != PhoneState.OFFHOOK) {
                                }
                                isModemWork = true;
                                headSar2 = true;
                                if (isSensorSwitchOn) {
                                }
                                if (isSensorSwitchOn) {
                                }
                                state = SwitchWifiSar.SAR_OFF;
                                if (this.mLastWifiSar != state) {
                                }
                            } else {
                                Log.d(WifiOemProximitySensorManager.TAG, "processWifiSwitch project is error");
                            }
                        }
                        Log.d(WifiOemProximitySensorManager.TAG, "processWifiSwitch mLastWifiSar.ordinal()=" + this.mLastWifiSar.ordinal());
                    }
                }
                z = true;
                boolean isModemWork22 = z;
                if (!OemConstant.SWITCH_LOG) {
                }
                if (this.mWifiSarTestEnable) {
                }
                if (!Build.HARDWARE.equals("mt6779")) {
                }
                Log.d(WifiOemProximitySensorManager.TAG, "processWifiSwitch mLastWifiSar.ordinal()=" + this.mLastWifiSar.ordinal());
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

    public WifiOemProximitySensorManager(Context context) {
        this(context, new IOemListener() {
            /* class com.oppo.internal.telephony.rf.WifiOemProximitySensorManager.AnonymousClass1 */
            boolean mIsTestCard = false;

            @Override // com.oppo.internal.telephony.rf.WifiOemProximitySensorManager.IOemListener
            public void onSensorChange(SensorState state) {
                this.mIsTestCard = initTestCard();
            }

            @Override // com.oppo.internal.telephony.rf.WifiOemProximitySensorManager.IOemListener
            public void onDataChange(PhoneState state) {
            }

            @Override // com.oppo.internal.telephony.rf.WifiOemProximitySensorManager.IOemListener
            public void onCallChange(PhoneState state) {
            }

            @Override // com.oppo.internal.telephony.rf.WifiOemProximitySensorManager.IOemListener
            public void onSreenChange(boolean isOn) {
            }

            @Override // com.oppo.internal.telephony.rf.WifiOemProximitySensorManager.IOemListener
            public boolean isTestCard() {
                return this.mIsTestCard;
            }

            private synchronized boolean initTestCard() {
                for (Phone phone : PhoneFactory.getPhones()) {
                    if (((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone)).is_test_card()) {
                        return true;
                    }
                }
                return false;
            }
        });
        mHandlerThread = new HandlerThread("wifi-sarthread");
        mHandlerThread.start();
        mHandler = new OemWifiSarHandler(mHandlerThread.getLooper());
        mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    public WifiOemProximitySensorManager(Context context, IOemListener listener) {
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
        Log.d(TAG, "WifiOemProximitySensorManager: init and release first");
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

    public static synchronized WifiOemProximitySensorManager getDefault(Context context) {
        WifiOemProximitySensorManager wifiOemProximitySensorManager;
        synchronized (WifiOemProximitySensorManager.class) {
            if (sInstance == null) {
                sInstance = new WifiOemProximitySensorManager(context);
            }
            wifiOemProximitySensorManager = sInstance;
        }
        return wifiOemProximitySensorManager;
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
}
