package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.IpConfiguration.IpAssignment;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.MathUtils;
import com.android.server.WifiRomUpdateHelper;
import com.android.server.wifi.scanner.ChannelHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoAutoConnectManager {
    private static final String ACTION_DEBUG_DATA_SHOW = "com.oppo.autoconnectmanager.intent.action.debug.data.show";
    private static final String ACTION_DEBUG_DISABLE = "com.oppo.autoconnectmanager.intent.action.debug.disable";
    private static final String ACTION_DEBUG_ENABLE = "com.oppo.autoconnectmanager.intent.action.debug.enable";
    private static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
    private static final int ASSOC_REJECT_EXP_BASE = 2;
    private static final int ASSOC_REJECT_MULTIPLE_BASE = 5;
    private static final int ASSOC_REJECT_RETRY_TIME_MIN = 4000;
    private static final int ASSOC_REJECT_STEP_COUNT = 4;
    private static final int AUTH_FAILURE_EXP_BASE = 2;
    private static final int AUTH_FAILURE_MULTIPLE_BASE = 10;
    private static final int AUTH_FAILURE_RETRY_TIME_MIN = 10000;
    private static final int AUTH_FAILURE_STEP_COUNT = 2;
    private static final int CONTINUOUS_DEBOUNCE_DISCONNECT_INTERVAL_TIME = 1000;
    private static final int CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_COUNT = 5;
    private static final int CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME = 60;
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static boolean DEBUG = false;
    private static final int DHCP_FAILURE_EXP_BASE = 2;
    private static final int DHCP_FAILURE_MULTIPLE_BASE = 5;
    private static final int DHCP_FAILURE_RETRY_TIME_MIN = 30000;
    private static final int DHCP_FAILURE_STEP_COUNT = 4;
    private static final int DISABLED_REASON_ASSOCIATION_REJECT = 2;
    private static final int DISABLED_REASON_AUTHENTICATION_NO_CREDENTIALS = 7;
    private static final int DISABLED_REASON_AUTH_FAILURE = 3;
    private static final int DISABLED_REASON_BY_WIFI_MANAGER = 9;
    private static final int DISABLED_REASON_CONTINUOUS_DEBOUNCE_DISCONNECT = 200;
    private static final int DISABLED_REASON_DHCP_FAILURE = 4;
    private static final int DISABLED_REASON_DISABLED_BAD_LINK = 1;
    private static final int DISABLED_REASON_DNS_FAILURE = 5;
    private static final int DISABLED_REASON_INVALID = 0;
    private static final int DISABLED_REASON_NO_INTERNET = 8;
    private static final int DISABLED_REASON_OPPO_AUTO_CONNECT_BASE = 200;
    private static final int DISABLED_REASON_TLS_VERSION_MISMATCH = 6;
    private static final int DISABLED_REASON_UNKNWON = 102;
    private static final int DISABLED_REASON_WRONG_KEY = 101;
    private static final int EVT_ALL_MASK = 65535;
    private static final int EVT_DEBUG_DATA_SHOW = 4;
    private static final int EVT_DELAYED_MANU_REASSOCIATE = 16;
    private static final int EVT_ENABLE_ALL_NETWORKS = 512;
    private static final int EVT_MANU_CONNECT = 1;
    private static final int EVT_NETWORK_DELETED = 32;
    private static final int EVT_NETWORK_DISABLED = 8;
    private static final int EVT_NETWORK_RESAVED = 2048;
    private static final int EVT_SCAN_RESULT = 2;
    private static final int EVT_SCREEN_ON = 256;
    private static final int EVT_THIRD_APK_CONNECT = 4096;
    private static final int EVT_UPDATE_NETWORK_DISABLED_COUNT = 128;
    private static final int EVT_WIFI_CONNECT_STATE_CHANGED = 64;
    private static final int EVT_WIFI_STATE_ON = 1024;
    private static final int KEEP_DISABLED_TIME_MAX = 3600000;
    private static final int MANU_REASSOCIATE_INTERVAL_TIME = 1000;
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int NO_INTERNET_STEP_COUNT = 1;
    private static final int NO_INTERNET_STEP_TIME = 120;
    private static final int RSSI_BAD = -85;
    private static final int RSSI_CAN_SCANNED = -90;
    private static final int RSSI_GOOD = -70;
    private static final int RSSI_INVALID = -127;
    private static final int RSSI_LOW = -78;
    private static final int RSSI_STEP = 3;
    private static final String TAG = "OppoAutoConnectManager";
    private static final String WIFI_ASSISTANT_ROMUPDATE = "rom.update.wifi.assistant";
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final int WIFI_CONNECT_STATE_COMPLETED = 2;
    private static final int WIFI_CONNECT_STATE_DISCONNECTED = 3;
    private static final int WIFI_CONNECT_STATE_SUPPLICANT_COMPLETED = 1;
    private static final int WIFI_CONNECT_STATE_SUPPLICANT_DISCONNECTED = 0;
    private static final int WRONG_KEY_EXP_BASE = 2;
    private static final int WRONG_KEY_MULTIPLE_BASE = 10;
    private static final int WRONG_KEY_RETRY_TIME_MIN = 10000;
    private static final int WRONG_KEY_STEP_COUNT = 2;
    private static Context mContext;
    private static OppoAutoConnectManager mInstance;
    private static SimpleDateFormat mSimpleDateFormat;
    private static WifiConfigManager mWifiConfigManager;
    private static WifiNative mWifiNative;
    private static WifiNetworkStateTraker mWifiNetworkStateTraker;
    private static WifiRomUpdateHelper mWifiRomUpdateHelper;
    private static WifiStateMachine mWifiStateMachine;
    private final BroadcastReceiver mBroadcastReciever;
    private int mConnectingNetId;
    private HashMap<Integer, String> mDisableReasonStrHashMap;
    private HashMap<Integer, String> mEvtStrHashMap;
    private OppoAutoConnectManagerHandler mHandler;
    private OppoAutoConnectConfiguration mLastConnectedConfiguration;
    private int mLastManuConnectNetId;
    private long mLastManuReassociateTime;
    private int mLastManuReassociateToken;
    private long mLastTriggerEvtTimeMillis;
    private NetworkInfo mNetworkInfo;
    private HashMap<String, OppoAutoConnectConfiguration> mOppoAutoConnectConfigurationHashMap;
    private OppoManuConnectState mOppoManuConnectState;
    List<ScanResult> mScanResultList;
    private boolean mSettingManuConnect;
    private boolean mSupplicantCompleted;
    private boolean mThirdAPKConnect;
    private int mTriggerAutoConnectEvtSet;
    private boolean mWifiAssistantEnabled;
    private boolean mWifiAssistantRomupdate;

    public class OppoAutoConnectConfiguration {
        private String mConfigKey = null;
        private int mConituousDebounceDisconnectCount = 0;
        private int mContinuousDisabledCount = 0;
        private int mDisabledLevel = OppoAutoConnectManager.RSSI_INVALID;
        private int mDisabledReason = 0;
        private long mDisabledUntilTime = System.currentTimeMillis();
        private long mLastConnectedTime = 0;
        private String mPwd = null;

        public String toString() {
            return "\nmConfigKey:" + this.mConfigKey + "\n" + "mDisabledReason:" + this.mDisabledReason + "\n" + "mDisabledLevel:" + this.mDisabledLevel + "\n" + "mContinuousDisabledCount:" + this.mContinuousDisabledCount + "\n" + "mDisabledUntilTime:" + OppoAutoConnectManager.this.getFormatDateTime(this.mDisabledUntilTime) + "\n" + "mLastConnectedTime:" + OppoAutoConnectManager.this.getFormatDateTime(this.mLastConnectedTime) + "\n" + "mConituousDebounceDisconnectCount:" + this.mConituousDebounceDisconnectCount;
        }
    }

    private final class OppoAutoConnectManagerHandler extends Handler {
        public OppoAutoConnectManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                OppoAutoConnectManager.this.loge("msg is null!!");
                return;
            }
            int message = msg.what;
            OppoAutoConnectManager.this.logd("handle event:" + ((String) OppoAutoConnectManager.this.mEvtStrHashMap.get(Integer.valueOf(message))));
            switch (message) {
                case 1:
                    OppoAutoConnectManager.this.handleManuConnect(((Integer) msg.obj).intValue());
                    break;
                case 2:
                    OppoAutoConnectManager.this.handleScanResult();
                    break;
                case 4:
                    OppoAutoConnectManager.this.handleDebugDataShow();
                    break;
                case 8:
                    OppoAutoConnectManager.this.handleNetworkDisabled(msg.arg1, msg.arg2);
                    break;
                case 16:
                    int token = msg.arg1;
                    if (token != OppoAutoConnectManager.this.mLastManuReassociateToken) {
                        OppoAutoConnectManager.this.logd("token mismatch: token=" + token + " and mLastManuReassociateToken=" + OppoAutoConnectManager.this.mLastManuReassociateToken);
                        break;
                    } else {
                        OppoAutoConnectManager.this.handleDelayedManuReassociate();
                        break;
                    }
                case 32:
                    OppoAutoConnectManager.this.handleNetworkDeleted((WifiConfiguration) msg.obj);
                    break;
                case OppoAutoConnectManager.EVT_WIFI_CONNECT_STATE_CHANGED /*64*/:
                    OppoAutoConnectManager.this.handleWifiConnectStateChanged(msg.arg1, msg.arg2);
                    break;
                case 128:
                    OppoAutoConnectManager.this.handleUpdateNetworkDisabledCount(msg.arg1, msg.arg2);
                    break;
                case 256:
                    OppoAutoConnectManager.this.handleScreenOn();
                    break;
                case OppoAutoConnectManager.EVT_ENABLE_ALL_NETWORKS /*512*/:
                    OppoAutoConnectManager.this.handleEnableAllNetworks();
                    break;
                case OppoAutoConnectManager.EVT_NETWORK_RESAVED /*2048*/:
                    OppoAutoConnectManager.this.handleNetworkReSaved((WifiConfiguration) msg.obj);
                    break;
                case OppoAutoConnectManager.EVT_THIRD_APK_CONNECT /*4096*/:
                    OppoAutoConnectManager.this.handleThirdAPKConnect(((Integer) msg.obj).intValue());
                    break;
            }
        }
    }

    public class OppoManuConnectState {
        private static final int STATE_CONNECTED = 1;
        private static final String STATE_CONNECTED_STR = "connected";
        private static final int STATE_DISCONNECTED = 2;
        private static final String STATE_DISCONNECTED_STR = "disconnected";
        private static final int STATE_INVALID = -1;
        private static final String STATE_INVALID_STR = "invalid";
        private static final int STATE_MAX = 3;
        private static final int STATE_STARTED = 0;
        private static final String STATE_STARTED_STR = "started";
        private static final int TYPE_INVALID = -1;
        private static final String TYPE_INVALID_STR = "invalid";
        private static final int TYPE_MAX = 3;
        private static final int TYPE_SETTING = 0;
        private static final String TYPE_SETTING_STR = "setting";
        private static final int TYPE_THIRDAPK = 1;
        private static final String TYPE_THIRDAPK_STR = "thirdapk";
        private static final int TYPE_WPS = 2;
        private static final String TYPE_WPS_STR = "wps";
        private HashMap<Integer, String> mStateStrHashMap = new HashMap();
        private HashMap<Integer, String> mTypeStrHashMap = new HashMap();
        private int netId = -1;
        private int state = -1;
        private int type = -1;

        public int getState() {
            OppoAutoConnectManager.this.logd("get manu connect State:" + ((String) this.mStateStrHashMap.get(Integer.valueOf(this.state))));
            return this.state;
        }

        public void setState(int newState) {
            OppoAutoConnectManager.this.logd("set manu connect State:" + newState + " " + ((String) this.mStateStrHashMap.get(Integer.valueOf(newState))));
            if (newState >= 3) {
                OppoAutoConnectManager.this.logd("invalid state:" + newState);
            } else {
                this.state = newState;
            }
        }

        public int getType() {
            OppoAutoConnectManager.this.logd("get manu connect type:" + ((String) this.mTypeStrHashMap.get(Integer.valueOf(this.type))));
            return this.type;
        }

        public void setType(int newType) {
            OppoAutoConnectManager.this.logd("set manu connect Type:" + newType + " " + ((String) this.mTypeStrHashMap.get(Integer.valueOf(newType))));
            if (newType >= 3) {
                OppoAutoConnectManager.this.logd("invalid type:" + newType);
            } else {
                this.type = newType;
            }
        }

        public int getNetId() {
            OppoAutoConnectManager.this.logd("get manu connect NetId:" + this.netId);
            return this.netId;
        }

        public void setNetId(int newNetId) {
            OppoAutoConnectManager.this.logd("set manu connect NetId:" + newNetId);
            this.netId = newNetId;
        }

        public void update(int state, int type, int netId) {
            setState(state);
            setType(type);
            setNetId(netId);
        }

        public String toString() {
            return "manu connect: netId=" + this.netId + ",type=[" + this.type + "]" + ((String) this.mTypeStrHashMap.get(Integer.valueOf(this.type))) + ",state=[" + this.state + "]" + ((String) this.mStateStrHashMap.get(Integer.valueOf(this.state)));
        }

        public OppoManuConnectState() {
            this.mStateStrHashMap.put(Integer.valueOf(-1), "invalid");
            this.mStateStrHashMap.put(Integer.valueOf(0), STATE_STARTED_STR);
            this.mStateStrHashMap.put(Integer.valueOf(1), STATE_CONNECTED_STR);
            this.mStateStrHashMap.put(Integer.valueOf(2), STATE_DISCONNECTED_STR);
            this.mTypeStrHashMap.put(Integer.valueOf(-1), "invalid");
            this.mTypeStrHashMap.put(Integer.valueOf(0), TYPE_SETTING_STR);
            this.mTypeStrHashMap.put(Integer.valueOf(1), TYPE_THIRDAPK_STR);
            this.mTypeStrHashMap.put(Integer.valueOf(2), TYPE_WPS_STR);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.OppoAutoConnectManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.OppoAutoConnectManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.OppoAutoConnectManager.<clinit>():void");
    }

    private OppoAutoConnectConfiguration getAutoConnectConfiguration(String configKey) {
        if (this.mOppoAutoConnectConfigurationHashMap == null) {
            loge("mOppoAutoConnectConfigurationHashMap is null!!");
            return null;
        } else if (configKey == null) {
            loge("configKey is null!!");
            return null;
        } else if (this.mOppoAutoConnectConfigurationHashMap.keySet().contains(configKey)) {
            return (OppoAutoConnectConfiguration) this.mOppoAutoConnectConfigurationHashMap.get(configKey);
        } else {
            logi(configKey + " is not exist in mOppoAutoConnectConfigurationHashMap");
            return null;
        }
    }

    private void setAutoConnectConfiguration(String configKey, OppoAutoConnectConfiguration oppoAutoConnConf) {
        if (oppoAutoConnConf == null || configKey == null) {
            logi("oppoAutoConnConf is null or mConfigKey is null");
        } else if (this.mOppoAutoConnectConfigurationHashMap == null) {
            loge("mOppoAutoConnectConfigurationHashMap is null!!");
        } else {
            logd("setAutoConnectConfiguration:" + configKey);
            this.mOppoAutoConnectConfigurationHashMap.put(configKey, oppoAutoConnConf);
        }
    }

    private void addOrUpdateAutoConnectConfiguration(OppoAutoConnectConfiguration oppoAutoConnConf) {
        if (oppoAutoConnConf == null) {
            loge("oppoAutoConnConf is null");
        } else {
            setAutoConnectConfiguration(oppoAutoConnConf.mConfigKey, oppoAutoConnConf);
        }
    }

    public void removeAutoConnectConfiguration(int netId) {
        WifiConfiguration wc = getWifiConfiguration(netId);
        if (wc == null) {
            loge("wc is null!!");
        } else {
            removeAutoConnectConfiguration(wc.configKey());
        }
    }

    public void removeAutoConnectConfiguration(String configKey) {
        if (this.mOppoAutoConnectConfigurationHashMap == null) {
            logd("mOppoAutoConnectConfigurationHashMap is null!!");
        } else if (configKey == null) {
            logd("configKey is null!!");
        } else {
            logd("removeAutoConnectConfiguration,configKey=" + configKey);
            if (getAutoConnectConfiguration(configKey) != null) {
                this.mOppoAutoConnectConfigurationHashMap.remove(configKey);
            }
        }
    }

    private void clearLastConnectedConfiguration(int netId) {
        WifiConfiguration wc = getWifiConfiguration(netId);
        if (wc == null) {
            loge("wc is null!!");
        } else {
            clearLastConnectedConfiguration(wc.configKey());
        }
    }

    private void clearLastConnectedConfiguration(String configKey) {
        if (configKey == null) {
            logd("confKey is null!!");
        } else if (this.mLastConnectedConfiguration == null) {
            logd("mLastConnectedConfiguration is null!!");
        } else {
            if (configKey.equals(this.mLastConnectedConfiguration.mConfigKey)) {
                logd("clear mLastConnectedConfiguration!!!");
                this.mLastConnectedConfiguration.mConfigKey = null;
                this.mLastConnectedConfiguration.mPwd = null;
                this.mLastConnectedConfiguration.mDisabledReason = 0;
                this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount = 0;
                this.mLastConnectedConfiguration.mContinuousDisabledCount = 0;
                this.mLastConnectedConfiguration.mDisabledLevel = RSSI_INVALID;
                this.mLastConnectedConfiguration.mDisabledUntilTime = 0;
                this.mLastConnectedConfiguration.mLastConnectedTime = 0;
            }
        }
    }

    private int getCurrentScanResultBestLevel(List<ScanResult> srList, String configKey) {
        if (configKey == null || srList == null || srList.size() <= 0) {
            loge("configKey is null,or srList is null/empty!!");
            return RSSI_INVALID;
        }
        int level = RSSI_INVALID;
        for (ScanResult sr : srList) {
            String tmpConfigKey = WifiConfiguration.configKey(sr);
            if (tmpConfigKey != null && tmpConfigKey.equals(configKey) && sr.level > level) {
                level = sr.level;
            }
        }
        logd("configKey:" + configKey + " best level:" + level);
        return level;
    }

    private long getKeepDisabledUntil(OppoAutoConnectConfiguration oppoAutoConnConf) {
        if (oppoAutoConnConf == null) {
            logd("oppoAutoConnConf is null!!");
            return 0;
        }
        long keepDisabledUntilAdd = 0;
        long baseTime = System.currentTimeMillis();
        int continuousDisabledCount = oppoAutoConnConf.mContinuousDisabledCount;
        int disabledLevel = oppoAutoConnConf.mDisabledLevel;
        int rssi_good = get_rssi_good();
        int keep_disabled_time_max = get_keep_disabled_time_max();
        int dhcp_failure_step_count = get_dhcp_failure_step_count();
        int dhcp_failure_multiple_base = get_dhcp_failure_multiple_base();
        int dhcp_failure_exp_base = get_dhcp_failure_exp_base();
        int dhcp_failure_retry_time_min = get_dhcp_failure_retry_time_min();
        int assoc_reject_step_count = get_assoc_reject_step_count();
        int assoc_reject_multiple_base = get_assoc_reject_multiple_base();
        int assoc_reject_exp_base = get_assoc_reject_exp_base();
        int assoc_reject_retry_time_min = get_assoc_reject_retry_time_min();
        int wrong_key_step_count = get_wrong_key_step_count();
        int wrong_key_multiple_base = get_wrong_key_multiple_base();
        int wrong_key_exp_base = get_wrong_key_exp_base();
        int wrong_key_retry_time_min = get_wrong_key_retry_time_min();
        int auth_failure_step_count = get_auth_failure_step_count();
        int auth_failure_multiple_base = get_auth_failure_multiple_base();
        int auth_failure_exp_base = get_auth_failure_exp_base();
        int auth_failure_retry_time_min = get_auth_failure_retry_time_min();
        int continuous_debounce_disconnect_step_count = get_continuous_debounce_disconnect_step_count();
        int continuous_debounce_disconnect_step_time = get_continuous_debounce_disconnect_step_time();
        int no_network_step_count = get_no_internet_step_count();
        int no_network_step_time = get_no_internet_step_time();
        logd("mDisabledReason:" + oppoAutoConnConf.mDisabledReason + ",mContinuousDisabledCount:" + oppoAutoConnConf.mContinuousDisabledCount);
        int integerMultiple;
        switch (oppoAutoConnConf.mDisabledReason) {
            case 2:
                if (continuousDisabledCount >= assoc_reject_step_count) {
                    integerMultiple = continuousDisabledCount / assoc_reject_step_count;
                    if (disabledLevel > rssi_good) {
                        keepDisabledUntilAdd = ((long) Math.pow((double) assoc_reject_exp_base, (double) integerMultiple)) * 1000;
                    } else {
                        keepDisabledUntilAdd = (((long) assoc_reject_multiple_base) * ((long) integerMultiple)) * 1000;
                    }
                    if (keepDisabledUntilAdd <= ((long) assoc_reject_retry_time_min)) {
                        keepDisabledUntilAdd = (long) assoc_reject_retry_time_min;
                        break;
                    }
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
                break;
            case 3:
                if (continuousDisabledCount >= auth_failure_step_count) {
                    integerMultiple = continuousDisabledCount / auth_failure_step_count;
                    if (disabledLevel > rssi_good) {
                        keepDisabledUntilAdd = ((long) Math.pow((double) auth_failure_exp_base, (double) integerMultiple)) * 1000;
                    } else {
                        keepDisabledUntilAdd = (((long) auth_failure_multiple_base) * ((long) integerMultiple)) * 1000;
                    }
                    if (keepDisabledUntilAdd <= ((long) auth_failure_retry_time_min)) {
                        keepDisabledUntilAdd = (long) auth_failure_retry_time_min;
                        break;
                    }
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
                break;
            case 4:
                if (continuousDisabledCount >= dhcp_failure_step_count) {
                    integerMultiple = continuousDisabledCount / dhcp_failure_step_count;
                    if (disabledLevel > rssi_good) {
                        keepDisabledUntilAdd = ((long) Math.pow((double) dhcp_failure_exp_base, (double) integerMultiple)) * 1000;
                    } else {
                        keepDisabledUntilAdd = (((long) dhcp_failure_multiple_base) * ((long) integerMultiple)) * 1000;
                    }
                    if (keepDisabledUntilAdd <= ((long) dhcp_failure_retry_time_min)) {
                        keepDisabledUntilAdd = (long) dhcp_failure_retry_time_min;
                        break;
                    }
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
                break;
            case 8:
                if (continuousDisabledCount % no_network_step_count == 0) {
                    keepDisabledUntilAdd = (((long) no_network_step_time) * ((long) (continuousDisabledCount / no_network_step_count))) * 1000;
                    break;
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
            case DISABLED_REASON_WRONG_KEY /*101*/:
                if (continuousDisabledCount >= wrong_key_step_count) {
                    integerMultiple = continuousDisabledCount / wrong_key_step_count;
                    if (disabledLevel > rssi_good) {
                        keepDisabledUntilAdd = ((long) Math.pow((double) wrong_key_exp_base, (double) integerMultiple)) * 1000;
                    } else {
                        keepDisabledUntilAdd = (((long) wrong_key_multiple_base) * ((long) integerMultiple)) * 1000;
                    }
                    if (keepDisabledUntilAdd <= ((long) wrong_key_retry_time_min)) {
                        keepDisabledUntilAdd = (long) wrong_key_retry_time_min;
                        break;
                    }
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
                break;
            case ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS /*200*/:
                if (continuousDisabledCount >= continuous_debounce_disconnect_step_count) {
                    keepDisabledUntilAdd = (((long) continuous_debounce_disconnect_step_time) * ((long) (continuousDisabledCount / continuous_debounce_disconnect_step_count))) * 1000;
                    break;
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
        }
        logd("keep Disabled from base Time:" + getFormatDateTime(baseTime) + " add " + keepDisabledUntilAdd + " millis");
        if (keepDisabledUntilAdd - ((long) keep_disabled_time_max) > 0) {
            logd("keepDisabledUntilAdd is too larger adjust to " + keep_disabled_time_max + " millis");
            keepDisabledUntilAdd = (long) keep_disabled_time_max;
        }
        return baseTime + keepDisabledUntilAdd;
    }

    private void handleManuConnect(int netId) {
        this.mLastManuReassociateToken = 0;
        this.mOppoManuConnectState.update(0, 0, netId);
        removeAutoConnectConfiguration(netId);
        clearLastConnectedConfiguration(netId);
    }

    public void sendManuConnectEvt(int netId) {
        handleManuConnect(netId);
    }

    public void sendThirdAPKConnectEvt(int netId) {
        handleThirdAPKConnect(netId);
    }

    public void handleThirdAPKConnect(int netId) {
        logd("handleThirdAPKConnectEvt:" + netId);
        this.mLastManuReassociateToken = 0;
        this.mOppoManuConnectState.update(0, 1, netId);
        removeAutoConnectConfiguration(netId);
        clearLastConnectedConfiguration(netId);
    }

    public void handleWpsConnect(boolean StartConnect, boolean ResetState) {
        logd("handleWpsConnect StartConnect = " + StartConnect + "Updatestate = " + ResetState);
        this.mOppoManuConnectState.setNetId(-1);
        if (StartConnect) {
            if (!(mWifiConfigManager == null || mWifiStateMachine == null || !mWifiStateMachine.isSupplicantAvailable())) {
                mWifiConfigManager.disableAllNetworksNative();
            }
            this.mOppoManuConnectState.setType(2);
        } else {
            this.mOppoManuConnectState.setType(0);
        }
        if (ResetState) {
            this.mOppoManuConnectState.setState(2);
        } else {
            this.mOppoManuConnectState.setState(0);
        }
    }

    private boolean checkBasicConditions() {
        if (checkInterfaceNull()) {
            return false;
        }
        if (getWifiState() != 3) {
            logd("Wifi is not in enabled state!!");
            return false;
        } else if (this.mOppoManuConnectState.getState() != 0) {
            return true;
        } else {
            logd("manu connect ,do nothing!!");
            return false;
        }
    }

    private void enableNetworksConditional() {
        List<WifiConfiguration> scannedConfiguredNetworksList = getAllScannedAndConfiguredNetworks();
        boolean isAllScannedConfiguredNetworksDisabled = isAllScannedConfiguredNetworksDisabled(scannedConfiguredNetworksList);
        if (scannedConfiguredNetworksList == null || scannedConfiguredNetworksList.size() <= 0) {
            logd("find no scanned and configured networks!!");
            return;
        }
        List<WifiConfiguration> canEnabledNetworksList = new ArrayList();
        List<WifiConfiguration> tmpCanEnabledNetworkList = new ArrayList();
        logi("try add disabled unknown reason!");
        canEnabledNetworksList = getNormalDisabledNetworks(scannedConfiguredNetworksList);
        tmpCanEnabledNetworkList = getCanEnabledConfiguredNetworks(scannedConfiguredNetworksList);
        if (canEnabledNetworksList == null || tmpCanEnabledNetworkList == null) {
            canEnabledNetworksList = tmpCanEnabledNetworkList;
        } else {
            logi("try add disabled networks in mOppoAutoConnectConfigurationHashMap!");
            canEnabledNetworksList.addAll(tmpCanEnabledNetworkList);
        }
        if (canEnabledNetworksList == null || canEnabledNetworksList.size() <= 0) {
            logd("time gap from trigger evt to now is :" + (System.currentTimeMillis() - this.mLastTriggerEvtTimeMillis) + "ms");
            if (getTriggerAutoConnectEvt(65535) && isAllScannedConfiguredNetworksDisabled) {
                logd("all networks are disabled,but " + getTriggerAutoConnectEvtString() + " happened, trigger auto connect immediately!");
                if (this.mWifiAssistantEnabled) {
                    canEnabledNetworksList = getNetworksExcept(scannedConfiguredNetworksList, 8);
                } else {
                    canEnabledNetworksList = scannedConfiguredNetworksList;
                }
            }
        }
        enableNetworks(canEnabledNetworksList);
    }

    private void handleScanResult() {
        this.mScanResultList = getScanResultList();
        if (this.mOppoManuConnectState.getState() == 0) {
            logd("man connect in progress,manuReassociate if possible!!");
            if (!wifiIsConnectedOrConnecting()) {
                manuReassociate();
            }
            return;
        }
        enableNetworksConditional();
        if (checkBasicConditions()) {
            startAutoConnect();
        }
    }

    public void sendScanResultEvt() {
        sendMessage(2);
    }

    public void sendUpdateNetworkDisabledCountEvt(int netId, int reason) {
        sendMessage(128, netId, reason, null);
    }

    private void handleUpdateNetworkDisabledCount(int netId, int reason) {
        logd("handleUpdateNetworkDisabledCount,netId = " + netId + ",reason= " + reason + "(" + ((String) this.mDisableReasonStrHashMap.get(Integer.valueOf(reason))) + ")");
        if (!checkInterfaceNull()) {
            if (reason == DISABLED_REASON_UNKNWON || reason == 9) {
                logd("no need to record reason,do nothing!!");
            } else if (this.mOppoAutoConnectConfigurationHashMap == null) {
                loge("mOppoAutoConnectConfigurationHashMap is null!!");
            } else {
                WifiConfiguration wc = getWifiConfiguration(netId);
                String configKey = "";
                if (wc == null) {
                    loge("wc is null!!");
                    return;
                }
                configKey = wc.configKey();
                OppoAutoConnectConfiguration oppoAutoConnConf = getAutoConnectConfiguration(configKey);
                if (oppoAutoConnConf == null) {
                    oppoAutoConnConf = new OppoAutoConnectConfiguration();
                    oppoAutoConnConf.mConfigKey = configKey;
                }
                oppoAutoConnConf.mDisabledLevel = getCurrentScanResultBestLevel(getScanResultList(), configKey);
                if (oppoAutoConnConf.mDisabledLevel == RSSI_INVALID) {
                    oppoAutoConnConf.mDisabledLevel = RSSI_CAN_SCANNED;
                }
                if (oppoAutoConnConf.mDisabledReason != reason) {
                    oppoAutoConnConf.mContinuousDisabledCount = 1;
                } else {
                    oppoAutoConnConf.mContinuousDisabledCount = oppoAutoConnConf.mContinuousDisabledCount + 1;
                }
                oppoAutoConnConf.mDisabledReason = reason;
                oppoAutoConnConf.mDisabledUntilTime = getKeepDisabledUntil(oppoAutoConnConf);
                addOrUpdateAutoConnectConfiguration(oppoAutoConnConf);
            }
        }
    }

    private void handleNetworkDisabled(int netId, int reason) {
        logd("handleNetworkDisabled:" + netId);
        handleUpdateNetworkDisabledCount(netId, reason);
        if ((this.mOppoManuConnectState.getState() == 0 || this.mOppoManuConnectState.getState() == 1) && this.mOppoManuConnectState.getNetId() == netId) {
            this.mOppoManuConnectState.update(2, -1, -1);
            enableNetworksConditional();
            if (checkBasicConditions()) {
                startAutoConnect();
            }
        }
    }

    public void sendNetworkDisabledEvt(int netId, int reason) {
        handleNetworkDisabled(netId, reason);
    }

    private void handleNetworkDeleted(WifiConfiguration wConf) {
        logd("handleNetworkDeleted");
        if (wConf == null) {
            loge("wConf is null!!");
            return;
        }
        String configKey = wConf.configKey();
        boolean needConnect = false;
        if (!(configKey == null || this.mLastConnectedConfiguration == null || !configKey.equals(this.mLastConnectedConfiguration.mConfigKey))) {
            this.mLastTriggerEvtTimeMillis = System.currentTimeMillis();
            setTriggerAutoConnectEvt(32);
            needConnect = true;
            logd("current connected network deleted,trigger auto connect!");
        }
        if ((this.mOppoManuConnectState.getState() == 0 || this.mOppoManuConnectState.getState() == 1) && this.mOppoManuConnectState.getNetId() == wConf.networkId) {
            logd("manu connect network deleted!");
            this.mOppoManuConnectState.update(2, -1, -1);
        }
        if (needConnect) {
            enableNetworksConditional();
            if (checkBasicConditions()) {
                startAutoConnect();
            } else {
                return;
            }
        }
        removeAutoConnectConfiguration(configKey);
        clearLastConnectedConfiguration(configKey);
    }

    public void sendNetworkDeletedEvt(int netId) {
        handleNetworkDeleted(getWifiConfiguration(netId));
    }

    private void handleWifiConnectStateChanged(int state, int networkId) {
        logd("handleWifiConnectStateChanged,connected = " + state + " netId = " + networkId);
        if (!checkInterfaceNull()) {
            WifiConfiguration wc = getWifiConfiguration(networkId);
            if (wc == null) {
                logd("wc is null!!");
                return;
            }
            String configKey = wc.configKey();
            if (configKey == null) {
                logd("configKey is null!!");
                return;
            }
            if (this.mLastConnectedConfiguration == null) {
                this.mLastConnectedConfiguration = new OppoAutoConnectConfiguration();
            }
            switch (state) {
                case 0:
                    logd("supplicant disconnected netId:" + networkId + " mLastManuConnect:" + this.mOppoManuConnectState.getNetId());
                    WifiConfiguration selectedConf = getWifiConfiguration(networkId);
                    if (getWifiState() != 3) {
                        logd("wifi is not in enabled state yet!!");
                    } else if (!(selectedConf == null || selectedConf.status == 1)) {
                        logd("try to reconnect the manu network id until disabled ,net id:" + networkId);
                        if (this.mOppoManuConnectState.getNetId() == networkId) {
                            if (this.mOppoManuConnectState.getState() == 0) {
                                manuReassociate();
                            } else if (this.mOppoManuConnectState.getState() != 1) {
                                reconnect(selectedConf.networkId);
                            }
                        }
                    }
                    if (this.mLastConnectedConfiguration != null && this.mLastConnectedConfiguration.mConfigKey != null) {
                        if (this.mLastConnectedConfiguration.mConfigKey.equals(configKey)) {
                            long currTime = System.currentTimeMillis();
                            int continuous_debounce_disconnect_interval_time = get_continuous_debounce_disconnect_interval_time();
                            int continuous_debounce_disconnect_step_count = get_continuous_debounce_disconnect_step_count();
                            if (currTime - this.mLastConnectedConfiguration.mLastConnectedTime <= ((long) continuous_debounce_disconnect_interval_time)) {
                                OppoAutoConnectConfiguration oppoAutoConnectConfiguration = this.mLastConnectedConfiguration;
                                oppoAutoConnectConfiguration.mConituousDebounceDisconnectCount = oppoAutoConnectConfiguration.mConituousDebounceDisconnectCount + 1;
                            } else if (this.mSupplicantCompleted) {
                                this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount = 0;
                            } else {
                                logd("not a couple connect-disconnect evt!,ignore");
                            }
                            this.mSupplicantCompleted = false;
                            logd("configKey:" + configKey + " mConituousDebounceDisconnectCount=" + this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount);
                            if (this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount >= continuous_debounce_disconnect_step_count && this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount % continuous_debounce_disconnect_step_count == 0) {
                                logi("mConituousDebounceDisconnectCount = " + this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount + " disable it!");
                                disableNetwork(networkId, ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS);
                                break;
                            }
                        }
                        logd("configKey mismatch,ignore!!");
                        return;
                    }
                    return;
                    break;
                case 1:
                    if (this.mLastConnectedConfiguration != null) {
                        if (this.mLastConnectedConfiguration.mConfigKey == null || !this.mLastConnectedConfiguration.mConfigKey.equals(configKey)) {
                            this.mLastConnectedConfiguration.mConfigKey = configKey;
                            this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount = 0;
                        }
                        this.mLastConnectedConfiguration.mLastConnectedTime = System.currentTimeMillis();
                        this.mSupplicantCompleted = true;
                        break;
                    }
                    logd("mLastConnectedConfiguration is null!!");
                    this.mSupplicantCompleted = true;
                    break;
                case 2:
                    if (this.mOppoManuConnectState.getState() == 0) {
                        if (this.mOppoManuConnectState.getNetId() == networkId) {
                            logd("manu connect network connected!");
                            this.mOppoManuConnectState.setState(1);
                        } else if (this.mOppoManuConnectState.getType() == 2) {
                            logd("connect wps network");
                            this.mOppoManuConnectState.setState(1);
                        }
                    }
                    if (!(this.mOppoManuConnectState.getState() != 1 || this.mOppoManuConnectState.getNetId() == networkId || this.mOppoManuConnectState.getNetId() == -1)) {
                        logd("manu connect disconnect may missed,reset manu connect!!");
                        this.mOppoManuConnectState.update(2, -1, -1);
                        if (mWifiNetworkStateTraker != null) {
                            mWifiNetworkStateTraker.setManualConnect(false, false, 1000);
                        }
                    }
                    OppoAutoConnectConfiguration oacc = getAutoConnectConfiguration(configKey);
                    if (!(oacc == null || oacc.mDisabledReason == ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS)) {
                        removeAutoConnectConfiguration(configKey);
                        break;
                    }
                case 3:
                    DetailedState detailState = DetailedState.DISCONNECTED;
                    if (this.mNetworkInfo != null) {
                        detailState = this.mNetworkInfo.getDetailedState();
                    }
                    if (this.mOppoManuConnectState.getType() != 2 || DetailedState.OBTAINING_IPADDR == detailState || DetailedState.CONNECTED == detailState) {
                        if (this.mOppoManuConnectState.getNetId() == networkId && this.mOppoManuConnectState.getState() == 1) {
                            this.mOppoManuConnectState.update(2, -1, -1);
                            break;
                        }
                    }
                    logd("Start Wps conncet got disconnected event ,ignore");
                    break;
                    break;
            }
        }
    }

    public void sendNetworkStateChangedEvt(Intent intent) {
        handleNetworkStateChanged(intent);
    }

    private void handleNetworkStateChanged(Intent intent) {
        if (intent == null) {
            loge("intent is null!!!");
            return;
        }
        this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (this.mNetworkInfo != null) {
            logd("detailed status:" + this.mNetworkInfo.getDetailedState());
        }
    }

    public void sendWifiConnectStateChangedEvt(boolean connected, int netId) {
        if (connected) {
            handleWifiConnectStateChanged(2, netId);
        } else {
            handleWifiConnectStateChanged(3, netId);
        }
    }

    public void sendWifiSupplicantConnectStateChangedEvt(Message message) {
        if (message != null) {
            StateChangeResult stateChangeResult = message.obj;
            if (stateChangeResult != null) {
                SupplicantState state = stateChangeResult.state;
                if (SupplicantState.isConnecting(state)) {
                    this.mConnectingNetId = stateChangeResult.networkId;
                    logd("connecting netId:" + this.mConnectingNetId);
                    if (this.mOppoManuConnectState.getType() == 2) {
                        logd("set manuConnect netId to : " + this.mConnectingNetId);
                        this.mOppoManuConnectState.setNetId(this.mConnectingNetId);
                    }
                }
                if (state == SupplicantState.COMPLETED) {
                    handleWifiConnectStateChanged(1, stateChangeResult.networkId);
                } else if (state == SupplicantState.DISCONNECTED) {
                    handleWifiConnectStateChanged(0, stateChangeResult.networkId);
                }
            }
        }
    }

    private void sendScreenOnEvt() {
        sendMessage(256);
    }

    private void handleScreenOn() {
        setTriggerAutoConnectEvt(256);
        this.mLastTriggerEvtTimeMillis = System.currentTimeMillis();
        logd("handleScreenOn:" + this.mLastTriggerEvtTimeMillis);
    }

    public void sendWifiStateChangedEvt(int wifiState) {
        handleWifiStateChanged(wifiState);
    }

    private void handleWifiStateChanged(int wifiState) {
        logd("handleWifiStateChanged,state = " + wifiState);
        if (wifiState == 3) {
            this.mLastTriggerEvtTimeMillis = System.currentTimeMillis();
            setTriggerAutoConnectEvt(EVT_WIFI_STATE_ON);
        } else if (wifiState == 1) {
            this.mOppoManuConnectState.update(-1, -1, -1);
        }
    }

    public void sendEnableAllNetworksEvt() {
        sendMessage(EVT_ENABLE_ALL_NETWORKS);
    }

    void handleEnableAllNetworks() {
        logd("handleEnableAllNetworks");
        if (this.mOppoManuConnectState.getState() != 0) {
            enableNetworksConditional();
            if (checkBasicConditions()) {
                logd("trigger auto connect as soon as possible!!");
                startAutoConnect();
            }
        }
    }

    public void sendNetworkReSavedEvt(WifiConfiguration oldConfig) {
        sendMessage(EVT_NETWORK_RESAVED, oldConfig);
    }

    private void handleNetworkReSaved(WifiConfiguration oldConfig) {
        logd("handleNetworkReSaved");
        if (oldConfig == null) {
            loge("oldConfig is null!!");
            return;
        }
        WifiConfiguration currConfig = getWifiConfiguration(oldConfig.networkId);
        if (currConfig == null) {
            loge("currConfig is null!!");
        } else if (currConfig.status != 1) {
            logd("network is not disabled yet!");
            if (checkBasicConditions()) {
                startAutoConnect();
            }
        } else {
            boolean networkNeedEnable = false;
            OppoAutoConnectConfiguration oacc = getAutoConnectConfiguration(currConfig.configKey());
            boolean dhcpDisabled = false;
            if (currConfig.getIpAssignment() == IpAssignment.STATIC && oldConfig.getIpAssignment() == IpAssignment.DHCP) {
                dhcpDisabled = true;
            }
            logd("dhcp changed to disabled: " + dhcpDisabled);
            if (dhcpDisabled && oacc != null && oacc.mDisabledReason == 4) {
                networkNeedEnable = true;
            }
            if (!networkNeedEnable) {
                boolean pwdChanged = false;
                if (isWpaPskAP(currConfig)) {
                    if (!(currConfig.preSharedKey == null || oldConfig.preSharedKey == null || currConfig.preSharedKey.equals(oldConfig.preSharedKey))) {
                        logd("curr pwd:" + currConfig.preSharedKey + " old pwd: " + oldConfig.preSharedKey);
                        pwdChanged = true;
                    }
                } else if (isWepAP(currConfig)) {
                    if (!(currConfig.wepKeys == null || oldConfig.wepKeys == null || currConfig.wepKeys[currConfig.wepTxKeyIndex] == null || oldConfig.wepKeys[oldConfig.wepTxKeyIndex] == null || currConfig.wepKeys[currConfig.wepTxKeyIndex].equals(oldConfig.wepKeys[oldConfig.wepTxKeyIndex]))) {
                        logd("curr pwd:" + currConfig.wepKeys[currConfig.wepTxKeyIndex] + " old pwd: " + oldConfig.wepKeys[oldConfig.wepTxKeyIndex]);
                        pwdChanged = true;
                    }
                } else if (isWapiPskAp(currConfig)) {
                    if (!(currConfig.preSharedKey == null || oldConfig.preSharedKey == null || currConfig.preSharedKey.equals(oldConfig.preSharedKey))) {
                        logd("curr pwd:" + currConfig.preSharedKey + " old pwd: " + oldConfig.preSharedKey);
                        pwdChanged = true;
                    }
                } else if (isWpaEapAp(currConfig)) {
                    WifiEnterpriseConfig currEntConfig = currConfig.enterpriseConfig;
                    String currId = null;
                    String currPwd = null;
                    if (currEntConfig != null) {
                        currId = currEntConfig.getIdentity();
                        currPwd = currEntConfig.getPassword();
                    }
                    WifiEnterpriseConfig oldEntConfig = oldConfig.enterpriseConfig;
                    String oldId = null;
                    String oldPwd = null;
                    if (oldEntConfig != null) {
                        oldId = oldEntConfig.getIdentity();
                        oldPwd = oldEntConfig.getPassword();
                    }
                    if (!(currId == null || oldId == null || currPwd == null || oldPwd == null || (currId.equals(oldId) && currPwd.equals(oldPwd)))) {
                        logd("curr (id:" + currId + " pwd:" + currPwd + ") " + "old (id:" + oldId + " pwd:" + oldPwd + ")");
                        pwdChanged = true;
                    }
                }
                logd("pwd changed: " + pwdChanged);
                if (pwdChanged && oacc != null && (oacc.mDisabledReason == DISABLED_REASON_WRONG_KEY || oacc.mDisabledReason == 3)) {
                    networkNeedEnable = true;
                }
            }
            if (networkNeedEnable) {
                enableNetwork(currConfig.networkId);
            }
            if (checkBasicConditions()) {
                startAutoConnect();
            }
        }
    }

    private void setTriggerAutoConnectEvt(int evt) {
        this.mTriggerAutoConnectEvtSet |= 65535 & evt;
    }

    private boolean getTriggerAutoConnectEvt(int evt) {
        return (this.mTriggerAutoConnectEvtSet & (65535 & evt)) != 0;
    }

    private void clearTriggerAutoConnectEvt() {
        this.mTriggerAutoConnectEvtSet = 0;
    }

    private String getTriggerAutoConnectEvtString() {
        String evtStr = "";
        if (getTriggerAutoConnectEvt(256)) {
            evtStr = evtStr + ((String) this.mEvtStrHashMap.get(Integer.valueOf(256)));
        }
        if (getTriggerAutoConnectEvt(EVT_WIFI_STATE_ON)) {
            evtStr = evtStr + "," + ((String) this.mEvtStrHashMap.get(Integer.valueOf(EVT_WIFI_STATE_ON)));
        }
        if (getTriggerAutoConnectEvt(32)) {
            return evtStr + "," + ((String) this.mEvtStrHashMap.get(Integer.valueOf(32)));
        }
        return evtStr;
    }

    private boolean checkCanInternet(WifiConfiguration wifiConfig) {
        if (mWifiNetworkStateTraker == null) {
            logd("mWifiNetworkStateTraker is null!!");
            return false;
        } else if (wifiConfig == null) {
            logd("wifiConfig is null!!");
            return false;
        } else {
            String configKey = wifiConfig.configKey();
            if (configKey == null) {
                return false;
            }
            List<WifiNetworkRecord> wnrList = mWifiNetworkStateTraker.getWifiNetworkRecords();
            if (wnrList == null || wnrList.size() <= 0 || !this.mWifiAssistantRomupdate) {
                logd("wnrList is null !!");
                return false;
            }
            for (WifiNetworkRecord wnr : wnrList) {
                if (configKey.equals(wnr.mConfigkey) && wnr.mNetworkValid) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean checkCanInternet(int netId) {
        return checkCanInternet(getWifiConfiguration(netId));
    }

    private List<WifiConfiguration> getAllScannedAndEnabledAndInternetNetworks(List<WifiConfiguration> wcList) {
        if (mWifiNetworkStateTraker == null) {
            logd("mWifiNetworkStateTraker is null!!");
            return null;
        } else if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return null;
        } else {
            List<WifiConfiguration> internetedNetworkList = new ArrayList();
            List<WifiNetworkRecord> wnrList = mWifiNetworkStateTraker.getWifiNetworkRecords();
            if (wnrList == null || wnrList.size() <= 0 || !this.mWifiAssistantRomupdate) {
                logd("wnrList is null !!");
                return null;
            }
            for (WifiConfiguration wc : wcList) {
                String configKey = wc.configKey();
                if (configKey != null) {
                    for (WifiNetworkRecord wnr : wnrList) {
                        if (configKey.equals(wnr.mConfigkey) && wnr.mNetworkValid && wc.status != 1) {
                            logd("add enabled and can internet network:" + configKey);
                            internetedNetworkList.add(wc);
                        }
                    }
                }
            }
            return internetedNetworkList;
        }
    }

    private List<WifiConfiguration> getAllScannedAndConfiguredNetworks() {
        List<ScanResult> srList = this.mScanResultList;
        List<WifiConfiguration> wcList = getConfiguredNetworks();
        if (srList == null || srList.size() <= 0 || wcList == null || wcList.size() <= 0) {
            logi("srList or wcList is null/empty!!");
            return null;
        }
        List<WifiConfiguration> scannedConfiguredNetworks = new ArrayList();
        for (WifiConfiguration wc : wcList) {
            String wcConfigKey = wc.configKey();
            if (wcConfigKey != null) {
                for (ScanResult sr : srList) {
                    String srConfigKey = WifiConfiguration.configKey(sr);
                    if (srConfigKey != null && wcConfigKey.equals(srConfigKey)) {
                        if (DEBUG) {
                            OppoAutoConnectConfiguration oacc = getAutoConnectConfiguration(wcConfigKey);
                            int reason = oacc != null ? oacc.mDisabledReason : wc.disableReason;
                            logd("find configured and scanned " + wcConfigKey + " disabled reason " + reason + "(" + ((String) this.mDisableReasonStrHashMap.get(Integer.valueOf(reason))) + ")");
                        }
                        scannedConfiguredNetworks.add(wc);
                    }
                }
            }
        }
        return scannedConfiguredNetworks;
    }

    private boolean isAllScannedConfiguredNetworksDisabled(List<WifiConfiguration> scanndConfiguredList) {
        if (scanndConfiguredList == null || scanndConfiguredList.size() <= 0) {
            logd("wcList is null or length is zero");
            return false;
        }
        for (WifiConfiguration wc : scanndConfiguredList) {
            logd("configkey: " + wc.configKey() + " status:" + wc.status);
            if (wc.status != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCanEnableDisabledConfiguredNetwork(OppoAutoConnectConfiguration oppoAutoConnConf) {
        List<ScanResult> srList = this.mScanResultList;
        if (oppoAutoConnConf == null || srList == null || srList.size() <= 0) {
            logi("oppoAutoConnConf is null,or srList is null/empty!!");
            return false;
        }
        String configKey = oppoAutoConnConf.mConfigKey;
        if (configKey == null) {
            logi("configKey is null!!");
            return false;
        }
        int bestLevel = getCurrentScanResultBestLevel(srList, configKey);
        int rssi_low = get_rssi_low();
        int rssi_bad = get_rssi_bad();
        int disabledReason = oppoAutoConnConf.mDisabledReason;
        int disabledLevel = oppoAutoConnConf.mDisabledLevel;
        int rssiGap = bestLevel - disabledLevel;
        int rssiGapAbsoluteValue = (int) MathUtils.abs((float) rssiGap);
        logd("rssiGap:" + rssiGap + ", rssiGapAbsoluteValue:" + rssiGapAbsoluteValue);
        long timeGap = oppoAutoConnConf.mDisabledUntilTime - System.currentTimeMillis();
        logd("disabled reason:" + disabledReason + "(" + ((String) this.mDisableReasonStrHashMap.get(Integer.valueOf(disabledReason))) + ")" + ",best level:" + bestLevel + ", last disabledLevel:" + disabledLevel);
        boolean needEnable = false;
        int rssi_good = get_rssi_good();
        int rssi_step = get_rssi_step();
        switch (disabledReason) {
            case 2:
            case 4:
                if (disabledLevel < rssi_good) {
                    if (disabledLevel < rssi_low) {
                        if (disabledLevel < rssi_bad) {
                            if (rssiGap > rssi_step) {
                                needEnable = true;
                                break;
                            }
                        } else if (bestLevel < rssi_low) {
                            if (rssiGap < rssi_step) {
                                if (rssiGapAbsoluteValue < rssi_step) {
                                    if (timeGap <= 0) {
                                        needEnable = true;
                                        break;
                                    }
                                    logd("bad rssi (+/-" + rssi_step + ") ,and not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                                    needEnable = false;
                                    break;
                                }
                            }
                            needEnable = true;
                            break;
                        } else {
                            needEnable = true;
                            break;
                        }
                    } else if (bestLevel < rssi_good) {
                        if (rssiGap < rssi_step) {
                            if (rssiGapAbsoluteValue < rssi_step) {
                                if (timeGap <= 0) {
                                    needEnable = true;
                                    break;
                                }
                                logd("low rssi(+/-" + rssi_step + ") ,and not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                                needEnable = false;
                                break;
                            }
                        }
                        needEnable = true;
                        break;
                    } else {
                        needEnable = true;
                        break;
                    }
                } else if (bestLevel < rssi_good) {
                    if (rssiGapAbsoluteValue < rssi_step) {
                        if (timeGap <= 0) {
                            needEnable = true;
                            break;
                        }
                        logd("good(-) rssi but not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                        needEnable = false;
                        break;
                    }
                } else if (timeGap <= 0) {
                    needEnable = true;
                    break;
                } else {
                    logd("good rssi but not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                    needEnable = false;
                    break;
                }
                break;
            case 3:
                if (rssiGap <= rssi_step) {
                    if (timeGap <= 0) {
                        needEnable = true;
                        break;
                    }
                    logd("good rssi but not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                    needEnable = false;
                    break;
                }
                needEnable = true;
                break;
            case 8:
                if (this.mWifiAssistantEnabled) {
                    if (bestLevel < rssi_good || disabledLevel > rssi_low) {
                        if (timeGap <= 0) {
                            needEnable = true;
                            break;
                        }
                        logd("no reach internet:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                        needEnable = false;
                        break;
                    }
                    needEnable = true;
                    break;
                }
                needEnable = true;
                break;
                break;
            case DISABLED_REASON_WRONG_KEY /*101*/:
                if (rssiGap <= rssi_step) {
                    if (timeGap <= 0) {
                        needEnable = true;
                        break;
                    }
                    logd("good rssi but not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                    needEnable = false;
                    break;
                }
                needEnable = true;
                break;
            case ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS /*200*/:
                if (bestLevel < rssi_good || disabledLevel > rssi_low) {
                    if (timeGap <= 0) {
                        needEnable = true;
                        break;
                    }
                    logd("not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                    needEnable = false;
                    break;
                }
                needEnable = true;
                break;
        }
        return needEnable;
    }

    private List<WifiConfiguration> getCanEnabledConfiguredNetworks(List<WifiConfiguration> wcList) {
        if (wcList == null || wcList.size() <= 0) {
            logi("wcList is null or empty!!");
            return null;
        }
        List<WifiConfiguration> canEnabledConfiguredNetworksList = new ArrayList();
        for (WifiConfiguration wc : wcList) {
            if (wc != null) {
                String configuredConfigKey = wc.configKey();
                if (configuredConfigKey != null) {
                    OppoAutoConnectConfiguration tmpOppoAutoConnConf = getAutoConnectConfiguration(configuredConfigKey);
                    if (tmpOppoAutoConnConf != null && checkCanEnableDisabledConfiguredNetwork(tmpOppoAutoConnConf)) {
                        logd("add can enabled configured network:" + configuredConfigKey);
                        canEnabledConfiguredNetworksList.add(wc);
                    }
                }
            }
        }
        return canEnabledConfiguredNetworksList;
    }

    private List<WifiConfiguration> getNormalDisabledNetworks(List<WifiConfiguration> wcList) {
        logd("getNormalDisabledNetworks");
        if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return null;
        }
        List<WifiConfiguration> normalDisabledNetworksList = new ArrayList();
        for (WifiConfiguration wc : wcList) {
            String configKey = wc.configKey();
            if (configKey != null && wc.status == 1) {
                logd("configkey = " + configKey + " disableReason = " + wc.disableReason);
                if (getAutoConnectConfiguration(configKey) == null) {
                    logd("add normal disabled network:" + configKey);
                    normalDisabledNetworksList.add(wc);
                }
            }
        }
        return normalDisabledNetworksList;
    }

    private List<WifiConfiguration> getNormalNetworks(List<WifiConfiguration> wcList) {
        logd("getNormalNetworks");
        if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return null;
        }
        List<WifiConfiguration> normalDisabledNetworksList = new ArrayList();
        for (WifiConfiguration wc : wcList) {
            String configKey = wc.configKey();
            if (configKey != null) {
                logd("configkey = " + configKey + " disableReason = " + wc.disableReason);
                if (getAutoConnectConfiguration(configKey) == null) {
                    logd("add normal network:" + configKey);
                    normalDisabledNetworksList.add(wc);
                }
            }
        }
        return normalDisabledNetworksList;
    }

    private List<WifiConfiguration> getNetworksWith(List<WifiConfiguration> wcList, int disabledReason) {
        logd("getNetworksWith reason:" + disabledReason);
        if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return null;
        }
        List<WifiConfiguration> networksList = new ArrayList();
        for (WifiConfiguration wc : wcList) {
            String configKey = wc.configKey();
            if (configKey != null) {
                logd("configkey = " + configKey + " disableReason = " + wc.disableReason);
                if (wc.disableReason == disabledReason) {
                    networksList.add(wc);
                }
            }
        }
        return networksList;
    }

    private List<WifiConfiguration> getNetworksExcept(List<WifiConfiguration> wcList, int disabledReason) {
        logd("getNetworksExcept reason:" + disabledReason);
        if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return null;
        }
        List<WifiConfiguration> networksList = new ArrayList();
        for (WifiConfiguration wc : wcList) {
            String configKey = wc.configKey();
            if (configKey != null) {
                logd("configkey = " + configKey + " disableReason = " + wc.disableReason);
                networksList.add(wc);
            }
        }
        return networksList;
    }

    private void enableNetworks(List<WifiConfiguration> wcList) {
        if (!checkInterfaceNull()) {
            if (!mWifiStateMachine.isSupplicantAvailable()) {
                logd("wifi is in disable or disable pending state,cancel enable!!");
            } else if (wcList == null || wcList.size() <= 0) {
                logi("wcList is null or empty!!");
            } else {
                for (WifiConfiguration wc : wcList) {
                    if (wc != null) {
                        logd("enable netId:" + wc.networkId + " SSID:" + wc.SSID);
                        enableNetworkWithoutBroadcast(wc.networkId);
                    }
                }
                mWifiConfigManager.sendConfiguredNetworksChangedBroadcast();
            }
        }
    }

    private WifiConfiguration tryPickCandinate(List<WifiConfiguration> wcList) {
        if (wcList == null || wcList.size() <= 0) {
            logi("wcList is null or empty!!");
            return null;
        }
        List<ScanResult> srList = this.mScanResultList;
        if (srList == null || srList.size() <= 0) {
            logi("srList is null or empty!!");
            return null;
        }
        int bestLevel = RSSI_INVALID;
        WifiConfiguration selectedConf = null;
        for (WifiConfiguration wc : wcList) {
            String configKey = wc.configKey();
            if (configKey != null) {
                int rssi = getCurrentScanResultBestLevel(srList, configKey);
                if (rssi >= bestLevel) {
                    bestLevel = rssi;
                    selectedConf = wc;
                }
            }
        }
        return selectedConf;
    }

    private WifiConfiguration pickValidWifiConfiguration() {
        logd("pickValidWifiConfiguration");
        if (checkInterfaceNull()) {
            return null;
        }
        WifiConfiguration selectedConf = null;
        List<WifiConfiguration> savedNetworksList = getConfiguredNetworks();
        List<WifiConfiguration> scannedConfiguredNetworksList = getAllScannedAndConfiguredNetworks();
        List<WifiConfiguration> savedAndEnabledNetworkList = new ArrayList();
        List<WifiConfiguration> savedAndEnaledAndScannedNetworkList = new ArrayList();
        List<WifiConfiguration> saveAndEnabledAndScannedAndCanInternetNetworkList = new ArrayList();
        List<WifiConfiguration> candinateNetworkList = new ArrayList();
        if (savedNetworksList == null || savedNetworksList.size() <= 0) {
            logi("savedNetworksList is null or empty!!");
            return null;
        } else if (scannedConfiguredNetworksList == null || scannedConfiguredNetworksList.size() <= 0) {
            logi("scannedConfiguredNetworksList is null or empty!!");
            return null;
        } else {
            for (WifiConfiguration wc : savedNetworksList) {
                if (wc.status != 1) {
                    savedAndEnabledNetworkList.add(wc);
                }
            }
            if (savedAndEnabledNetworkList == null || savedAndEnabledNetworkList.size() <= 0) {
                logi("savedAndEnabledNetworkList is null or empty!!");
                return null;
            }
            for (WifiConfiguration sewc : savedAndEnabledNetworkList) {
                String configKey = sewc.configKey();
                if (configKey != null) {
                    for (WifiConfiguration swc : scannedConfiguredNetworksList) {
                        if (configKey.equals(swc.configKey())) {
                            savedAndEnaledAndScannedNetworkList.add(sewc);
                        }
                    }
                }
            }
            saveAndEnabledAndScannedAndCanInternetNetworkList = getAllScannedAndEnabledAndInternetNetworks(savedAndEnaledAndScannedNetworkList);
            if (this.mWifiAssistantEnabled) {
                if ((this.mOppoManuConnectState.getState() == -1 || this.mOppoManuConnectState.getState() == 2) && mWifiStateMachine != null && mWifiStateMachine.isSupplicantStateDisconnected() && mWifiNetworkStateTraker != null && mWifiNetworkStateTraker.getManualConnect()) {
                    logd("sync manu connect sate to wlan assistant!!");
                    mWifiNetworkStateTraker.setManualConnect(false, false, 1000);
                }
                if (saveAndEnabledAndScannedAndCanInternetNetworkList == null || saveAndEnabledAndScannedAndCanInternetNetworkList.size() <= 0) {
                    logd("wlan assistant enabled but has no can internet networks!!");
                    selectedConf = tryPickCandinate(getNetworksExcept(savedAndEnaledAndScannedNetworkList, 8));
                } else {
                    logd("wlan assistant enabled and  find enabled and can internet networks,do nothing!!");
                    return null;
                }
            }
            logd("wlan assitant is disabled!");
            List<WifiConfiguration> normalNetworkList = new ArrayList();
            if (saveAndEnabledAndScannedAndCanInternetNetworkList != null && saveAndEnabledAndScannedAndCanInternetNetworkList.size() > 0) {
                normalNetworkList = getNormalNetworks(saveAndEnabledAndScannedAndCanInternetNetworkList);
                selectedConf = tryPickCandinate(normalNetworkList);
                if (selectedConf == null) {
                    selectedConf = tryPickCandinate(saveAndEnabledAndScannedAndCanInternetNetworkList);
                }
            }
            if (selectedConf == null) {
                logd("pick no networks in can internet network list,try to pick in all enabled network list");
                if (normalNetworkList != null) {
                    normalNetworkList.clear();
                }
                selectedConf = tryPickCandinate(getNormalNetworks(savedAndEnaledAndScannedNetworkList));
                if (selectedConf == null) {
                    selectedConf = tryPickCandinate(savedAndEnaledAndScannedNetworkList);
                }
            }
            if (selectedConf != null) {
                logd("selected network id:" + selectedConf.networkId + " configKey:" + selectedConf.configKey());
            }
            return selectedConf;
        }
    }

    private void startAutoConnect() {
        if (!checkInterfaceNull()) {
            clearTriggerAutoConnectEvt();
            WifiConfiguration selectedConf = pickValidWifiConfiguration();
            if (selectedConf != null) {
                boolean needReconnect = false;
                if (this.mWifiAssistantEnabled) {
                    if (this.mOppoManuConnectState.getState() == 0 || this.mOppoManuConnectState.getState() == 1) {
                        logd("manu connection, do nothing!!");
                        return;
                    } else if (wifiIsConnectedOrConnecting()) {
                        logd("wifi assitant enabled and already in connecting/connected,do nothing!!");
                        return;
                    } else {
                        needReconnect = true;
                    }
                } else if (this.mOppoManuConnectState.getState() == 0 || this.mOppoManuConnectState.getState() == 1) {
                    logd("manu connection, do nothing!!");
                    return;
                } else if (wifiIsConnectedOrConnecting()) {
                    boolean canInternetCur = checkCanInternet(this.mConnectingNetId);
                    boolean canInternetSelected = checkCanInternet(selectedConf);
                    if (!canInternetCur && canInternetSelected) {
                        logd("select a can inernet AP instead of non-internet connecting!!!");
                        needReconnect = true;
                    }
                } else {
                    needReconnect = true;
                }
                if (needReconnect) {
                    logd("select " + selectedConf.configKey() + " to reconnect!");
                    reconnect(selectedConf.networkId);
                }
            } else {
                logd("pick no networks!!");
                if (this.mWifiAssistantEnabled) {
                    logd("notify wlan assist to connect if possible!");
                    if (mWifiNetworkStateTraker != null && this.mWifiAssistantRomupdate) {
                        mWifiNetworkStateTraker.detectScanResult(System.currentTimeMillis());
                    }
                }
            }
        }
    }

    private boolean checkInterfaceNull() {
        if (mContext == null) {
            loge("mContext is null!!");
            return true;
        } else if (mWifiConfigManager == null) {
            loge("mWifiConfigManager is null!!");
            return true;
        } else if (mWifiStateMachine == null) {
            loge("mWifiStateMachine is null!!");
            return true;
        } else if (mWifiNative != null) {
            return false;
        } else {
            loge("mWifiNative is null!!");
            return true;
        }
    }

    private void enableNetwork(int netId) {
        if (!checkInterfaceNull()) {
            if (mWifiStateMachine.isSupplicantAvailable()) {
                mWifiConfigManager.enableNetworkEx(getWifiConfiguration(netId), false, 1000, true);
            } else {
                logd("wifi is in disable or disable pending state,cancel enableNetwork!!");
            }
        }
    }

    private void enableNetworkWithoutBroadcast(int netId) {
        if (!checkInterfaceNull()) {
            if (mWifiStateMachine.isSupplicantAvailable()) {
                mWifiConfigManager.enableNetworkEx(getWifiConfiguration(netId), false, 1000, false);
            } else {
                logd("wifi is in disable or disable pending state,cancel enableNetworkWithoutBroadcast!!");
            }
        }
    }

    private void disableNetwork(int netId, int reason) {
        if (!checkInterfaceNull()) {
            if (mWifiStateMachine.isSupplicantAvailable()) {
                mWifiConfigManager.disableNetworkEx(netId, reason, true);
            } else {
                logd("wifi is in disable or disable pending state,cancel disableNetwork!!");
            }
        }
    }

    private void disableNetworkWithoutBoradcast(int netId, int reason) {
        if (!checkInterfaceNull()) {
            if (mWifiStateMachine.isSupplicantAvailable()) {
                mWifiConfigManager.disableNetworkEx(netId, reason, false);
            } else {
                logd("wifi is in disable or disable pending state,cancel disableNetworkWithoutBoradcast!!");
            }
        }
    }

    private List<WifiConfiguration> getConfiguredNetworks() {
        if (checkInterfaceNull()) {
            return null;
        }
        return mWifiConfigManager.getSavedNetworksAll();
    }

    private WifiConfiguration getWifiConfiguration(int netId) {
        if (checkInterfaceNull()) {
            return null;
        }
        return mWifiConfigManager.getWifiConfigurationForAll(netId);
    }

    private List<ScanResult> getScanResultList() {
        if (checkInterfaceNull()) {
            return null;
        }
        return mWifiStateMachine.syncGetScanResultsList();
    }

    private int getWifiState() {
        if (checkInterfaceNull()) {
            return 1;
        }
        return mWifiStateMachine.syncGetWifiState();
    }

    private int getOperationMode() {
        if (checkInterfaceNull()) {
            return 3;
        }
        return mWifiStateMachine.getOperationalMode();
    }

    private boolean wifiIsConnectedOrConnecting() {
        this.mNetworkInfo = mWifiStateMachine.getNetworkInfo();
        if (this.mNetworkInfo == null) {
            return false;
        }
        return this.mNetworkInfo.isConnectedOrConnecting();
    }

    private void manuReassociate() {
        if (!checkInterfaceNull()) {
            this.mLastManuReassociateToken++;
            sendMessage(16, this.mLastManuReassociateToken, 0, null);
        }
    }

    private void handleDelayedManuReassociate() {
        logd("handleDelayedManuReassociate");
        if (!checkInterfaceNull()) {
            if (mWifiStateMachine.isSupplicantAvailable()) {
                if (this.mOppoManuConnectState.getState() == 0) {
                    long timeDiff = System.currentTimeMillis() - this.mLastManuReassociateTime;
                    logd("manu reassociate timeDiff:" + timeDiff);
                    if (timeDiff > 1000) {
                        mWifiNative.manuReassociate();
                        this.mLastManuReassociateTime = System.currentTimeMillis();
                    } else {
                        logd("manu reassociate too frequence,ignore!!");
                    }
                }
                return;
            }
            logd("wifi is in disable or disable pending state,cancel ManuReassociate!!");
        }
    }

    private void reconnect(int networkId) {
        if (!checkInterfaceNull()) {
            WifiConfiguration conf = getWifiConfiguration(networkId);
            if (conf == null) {
                logd("conf is null!!");
            } else if (1 == conf.status) {
                logd("networkid :" + networkId + " is disabled yet,cancel reconnect!!");
            } else if (mWifiStateMachine.isSupplicantAvailable()) {
                mWifiStateMachine.clearConfigBSSID(conf, TAG);
                mWifiStateMachine.setTargetNetworkId(networkId);
                if (mWifiConfigManager.selectNetwork(conf, false, 1000)) {
                    logd("start to reconnect!");
                    mWifiNative.reconnect();
                }
            } else {
                logd("wifi is in disable or disable pending state,cancel reconnect!!");
            }
        }
    }

    private boolean isWepAP(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(0) && wc.wepTxKeyIndex >= 0 && wc.wepTxKeyIndex < wc.wepKeys.length && wc.wepKeys[wc.wepTxKeyIndex] != null) {
            return true;
        }
        return false;
    }

    private boolean isWpaPskAP(WifiConfiguration wc) {
        if (wc == null) {
            return false;
        }
        return wc.allowedKeyManagement.get(1) || wc.allowedKeyManagement.get(4);
    }

    private boolean isWpaEapAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(2)) {
            return true;
        }
        return false;
    }

    private boolean isWapiPskAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(6)) {
            return true;
        }
        return false;
    }

    private boolean isWapiCertAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(7)) {
            return true;
        }
        return false;
    }

    private String getFormatDateTime(long timeMillis) {
        if (mSimpleDateFormat == null) {
            return Long.toString(timeMillis);
        }
        Date date = new Date(timeMillis);
        if (date == null) {
            return Long.toString(timeMillis);
        }
        return mSimpleDateFormat.format(date);
    }

    private void handleDebugDataShow() {
        logd("handleDebugDataShow");
        if (this.mOppoAutoConnectConfigurationHashMap == null) {
            loge("mOppoAutoConnectConfigurationHashMap is null!!");
            return;
        }
        HashMap<String, OppoAutoConnectConfiguration> tmpHashMap = this.mOppoAutoConnectConfigurationHashMap;
        logd("mOppoAutoConnectConfigurationHashMap in details!");
        for (String key : tmpHashMap.keySet()) {
            OppoAutoConnectConfiguration oppoAutoConnConf = (OppoAutoConnectConfiguration) tmpHashMap.get(key);
            if (oppoAutoConnConf != null) {
                logd(oppoAutoConnConf.toString());
            }
        }
        logd("mLastConnectedConfiguration in details!");
        if (this.mLastConnectedConfiguration != null) {
            logd(this.mLastConnectedConfiguration.toString());
        }
    }

    private void sendDebugDataShowEvt() {
        sendMessage(4);
    }

    private void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, "debug:" + str);
        }
    }

    private void loge(String str) {
        Log.d(TAG, "error:" + str);
    }

    private void logi(String str) {
        if (DEBUG) {
            Log.d(TAG, "Info:" + str);
        }
    }

    private void logw(String str) {
        if (DEBUG) {
            Log.d(TAG, "Warning:" + str);
        }
    }

    public void enableVerboseLogging(int verbose) {
        Log.d(TAG, "enableVerboseLogging verbose = " + verbose);
        if (verbose > 0) {
            DEBUG = true;
        } else {
            DEBUG = false;
        }
    }

    private void printStringInit() {
        logd("printStringInit");
        this.mEvtStrHashMap.put(Integer.valueOf(1), "EVT_MANU_CONNECT");
        this.mEvtStrHashMap.put(Integer.valueOf(EVT_THIRD_APK_CONNECT), "EVT_THIRD_APK_CONNECT");
        this.mEvtStrHashMap.put(Integer.valueOf(2), "EVT_SCAN_RESULT");
        this.mEvtStrHashMap.put(Integer.valueOf(4), "EVT_DEBUG_DATA_SHOW");
        this.mEvtStrHashMap.put(Integer.valueOf(8), "EVT_NETWORK_DISABLED");
        this.mEvtStrHashMap.put(Integer.valueOf(32), "EVT_NETWORK_DELETED");
        this.mEvtStrHashMap.put(Integer.valueOf(EVT_WIFI_CONNECT_STATE_CHANGED), "EVT_WIFI_CONNECT_STATE_CHANGED");
        this.mEvtStrHashMap.put(Integer.valueOf(128), "EVT_UPDATE_NETWORK_DISABLED_COUNT");
        this.mEvtStrHashMap.put(Integer.valueOf(256), "EVT_SCREEN_ON");
        this.mEvtStrHashMap.put(Integer.valueOf(EVT_ENABLE_ALL_NETWORKS), "EVT_ENABLE_ALL_NETWORKS");
        this.mEvtStrHashMap.put(Integer.valueOf(EVT_WIFI_STATE_ON), "EVT_WIFI_STATE_ON");
        this.mEvtStrHashMap.put(Integer.valueOf(EVT_NETWORK_RESAVED), "EVT_NETWORK_RESAVED");
        this.mEvtStrHashMap.put(Integer.valueOf(16), "EVT_DELAYED_MANU_REASSOCIATE");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(0), "DISABLED_REASON_INVALID");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(DISABLED_REASON_UNKNWON), "DISABLED_REASON_UNKNWON");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(5), "DISABLED_REASON_DNS_FAILURE");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(4), "DISABLED_REASON_DHCP_FAILURE");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(3), "DISABLED_REASON_AUTH_FAILURE");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(2), "DISABLED_REASON_ASSOCIATION_REJECT");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(9), "DISABLED_REASON_BY_WIFI_MANAGER");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(DISABLED_REASON_WRONG_KEY), "DISABLED_REASON_WRONG_KEY");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS), "DISABLED_REASON_CONTINUOUS_DEBOUNCE_DISCONNECT");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(8), "DISABLED_REASON_NO_INTERNET");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(9), "DISABLED_REASON_BY_WIFI_MANAGER");
    }

    private void sendMessage(int message) {
        this.mHandler.sendEmptyMessage(message);
    }

    private void sendMessage(int message, Object obj) {
        this.mHandler.obtainMessage(message, obj).sendToTarget();
    }

    private void sendMessage(int message, int arg1, int arg2, Object obj) {
        this.mHandler.obtainMessage(message, arg1, arg2, obj).sendToTarget();
    }

    private void listenForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction(ACTION_DEBUG_DATA_SHOW);
        intentFilter.addAction(ACTION_DEBUG_ENABLE);
        intentFilter.addAction(ACTION_DEBUG_DISABLE);
        intentFilter.addAction(ACTION_SCREEN_ON);
        mContext.registerReceiver(this.mBroadcastReciever, intentFilter, null, this.mHandler);
        mContext.getContentResolver().registerContentObserver(Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT), true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                OppoAutoConnectManager oppoAutoConnectManager = OppoAutoConnectManager.this;
                if (Global.getInt(OppoAutoConnectManager.mContext.getContentResolver(), OppoAutoConnectManager.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                    z = false;
                }
                oppoAutoConnectManager.mWifiAssistantEnabled = z;
                Log.d(OppoAutoConnectManager.TAG, "onChange mWifiAssistantEnabled= " + OppoAutoConnectManager.this.mWifiAssistantEnabled);
            }
        });
        mContext.getContentResolver().registerContentObserver(Global.getUriFor(WIFI_ASSISTANT_ROMUPDATE), true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                OppoAutoConnectManager oppoAutoConnectManager = OppoAutoConnectManager.this;
                if (Global.getInt(OppoAutoConnectManager.mContext.getContentResolver(), OppoAutoConnectManager.WIFI_ASSISTANT_ROMUPDATE, 1) != 1) {
                    z = false;
                }
                oppoAutoConnectManager.mWifiAssistantRomupdate = z;
                Log.d(OppoAutoConnectManager.TAG, "onChange mwar= " + OppoAutoConnectManager.this.mWifiAssistantRomupdate);
            }
        });
    }

    private int get_rssi_good() {
        if (mWifiRomUpdateHelper == null) {
            return -70;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_GOOD", Integer.valueOf(-70)).intValue();
    }

    private int get_rssi_bad() {
        if (mWifiRomUpdateHelper == null) {
            return -85;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_BAD", Integer.valueOf(-85)).intValue();
    }

    private int get_rssi_low() {
        if (mWifiRomUpdateHelper == null) {
            return RSSI_LOW;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_LOW", Integer.valueOf(RSSI_LOW)).intValue();
    }

    private int get_rssi_step() {
        if (mWifiRomUpdateHelper == null) {
            return 3;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_STEP", Integer.valueOf(3)).intValue();
    }

    private int get_continuous_debounce_disconnect_step_count() {
        if (mWifiRomUpdateHelper == null) {
            return 5;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_COUNT", Integer.valueOf(5)).intValue();
    }

    private int get_continuous_debounce_disconnect_step_time() {
        if (mWifiRomUpdateHelper == null) {
            return CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME", Integer.valueOf(CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME)).intValue();
    }

    private int get_continuous_debounce_disconnect_interval_time() {
        if (mWifiRomUpdateHelper == null) {
            return 1000;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_INTERVAL_TIME", Integer.valueOf(1000)).intValue();
    }

    private int get_no_internet_step_count() {
        if (mWifiRomUpdateHelper == null) {
            return 1;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_NO_INTERNET_STEP_COUNT", Integer.valueOf(1)).intValue();
    }

    private int get_no_internet_step_time() {
        if (mWifiRomUpdateHelper == null) {
            return NO_INTERNET_STEP_TIME;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_NO_INTERNET_STEP_TIME", Integer.valueOf(NO_INTERNET_STEP_TIME)).intValue();
    }

    private int get_assoc_reject_step_count() {
        if (mWifiRomUpdateHelper == null) {
            return 4;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_STEP_COUNT", Integer.valueOf(4)).intValue();
    }

    private int get_assoc_reject_exp_base() {
        if (mWifiRomUpdateHelper == null) {
            return 2;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_EXP_BASE", Integer.valueOf(2)).intValue();
    }

    private int get_assoc_reject_multiple_base() {
        if (mWifiRomUpdateHelper == null) {
            return 5;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_MULTIPLE_BASE", Integer.valueOf(5)).intValue();
    }

    private int get_assoc_reject_retry_time_min() {
        if (mWifiRomUpdateHelper == null) {
            return ASSOC_REJECT_RETRY_TIME_MIN;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_RETRY_TIME_MIN", Integer.valueOf(ASSOC_REJECT_RETRY_TIME_MIN)).intValue();
    }

    private int get_dhcp_failure_step_count() {
        if (mWifiRomUpdateHelper == null) {
            return 4;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_STEP_COUNT", Integer.valueOf(4)).intValue();
    }

    private int get_dhcp_failure_exp_base() {
        if (mWifiRomUpdateHelper == null) {
            return 2;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_EXP_BASE", Integer.valueOf(2)).intValue();
    }

    private int get_dhcp_failure_multiple_base() {
        if (mWifiRomUpdateHelper == null) {
            return 5;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_MULTIPLE_BASE", Integer.valueOf(5)).intValue();
    }

    private int get_dhcp_failure_retry_time_min() {
        if (mWifiRomUpdateHelper == null) {
            return DHCP_FAILURE_RETRY_TIME_MIN;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_RETRY_TIME_MIN", Integer.valueOf(DHCP_FAILURE_RETRY_TIME_MIN)).intValue();
    }

    private int get_wrong_key_step_count() {
        if (mWifiRomUpdateHelper == null) {
            return 2;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_STEP_COUNT", Integer.valueOf(2)).intValue();
    }

    private int get_wrong_key_exp_base() {
        if (mWifiRomUpdateHelper == null) {
            return 2;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_EXP_BASE", Integer.valueOf(2)).intValue();
    }

    private int get_wrong_key_multiple_base() {
        if (mWifiRomUpdateHelper == null) {
            return 10;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_MULTIPLE_BASE", Integer.valueOf(10)).intValue();
    }

    private int get_wrong_key_retry_time_min() {
        if (mWifiRomUpdateHelper == null) {
            return 10000;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_RETRY_TIME_MIN", Integer.valueOf(10000)).intValue();
    }

    private int get_auth_failure_step_count() {
        if (mWifiRomUpdateHelper == null) {
            return 2;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_STEP_COUNT", Integer.valueOf(2)).intValue();
    }

    private int get_auth_failure_exp_base() {
        if (mWifiRomUpdateHelper == null) {
            return 2;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_EXP_BASE", Integer.valueOf(2)).intValue();
    }

    private int get_auth_failure_multiple_base() {
        if (mWifiRomUpdateHelper == null) {
            return 10;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_MULTIPLE_BASE", Integer.valueOf(10)).intValue();
    }

    private int get_auth_failure_retry_time_min() {
        if (mWifiRomUpdateHelper == null) {
            return 10000;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_RETRY_TIME_MIN", Integer.valueOf(10000)).intValue();
    }

    private int get_keep_disabled_time_max() {
        if (mWifiRomUpdateHelper == null) {
            return KEEP_DISABLED_TIME_MAX;
        }
        return mWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_KEEP_DISABLED_TIME_MAX", Integer.valueOf(KEEP_DISABLED_TIME_MAX)).intValue();
    }

    private OppoAutoConnectManager(Context mCtxt, WifiStateMachine mWsm, WifiConfigManager mWcm, WifiNative mWnt) {
        boolean z = true;
        this.mEvtStrHashMap = new HashMap();
        this.mDisableReasonStrHashMap = new HashMap();
        this.mOppoManuConnectState = new OppoManuConnectState();
        this.mLastConnectedConfiguration = new OppoAutoConnectConfiguration();
        this.mConnectingNetId = -1;
        this.mOppoAutoConnectConfigurationHashMap = new HashMap();
        this.mSupplicantCompleted = false;
        this.mNetworkInfo = null;
        this.mLastTriggerEvtTimeMillis = 0;
        this.mTriggerAutoConnectEvtSet = 0;
        this.mWifiAssistantEnabled = true;
        this.mWifiAssistantRomupdate = true;
        this.mScanResultList = null;
        this.mLastManuConnectNetId = -1;
        this.mThirdAPKConnect = false;
        this.mSettingManuConnect = false;
        this.mLastManuReassociateTime = 0;
        this.mLastManuReassociateToken = 0;
        this.mBroadcastReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    OppoAutoConnectManager.this.loge("intent is null");
                    return;
                }
                String action = intent.getAction();
                if (action == null) {
                    OppoAutoConnectManager.this.loge("action is null");
                    return;
                }
                if (action.equals(OppoAutoConnectManager.ACTION_DEBUG_ENABLE)) {
                    OppoAutoConnectManager.this.enableVerboseLogging(1);
                } else if (action.equals(OppoAutoConnectManager.ACTION_DEBUG_DISABLE)) {
                    OppoAutoConnectManager.this.enableVerboseLogging(0);
                } else if (action.equals(OppoAutoConnectManager.ACTION_DEBUG_DATA_SHOW)) {
                    OppoAutoConnectManager.this.sendDebugDataShowEvt();
                } else if (action.equals(OppoAutoConnectManager.ACTION_SCREEN_ON)) {
                    OppoAutoConnectManager.this.sendScreenOnEvt();
                }
            }
        };
        mContext = mCtxt;
        mWifiStateMachine = mWsm;
        mWifiConfigManager = mWcm;
        mWifiNative = mWnt;
        Handler hldler = mWifiStateMachine.getHandler();
        if (hldler != null) {
            this.mHandler = new OppoAutoConnectManagerHandler(hldler.getLooper());
        }
        printStringInit();
        listenForBroadcasts();
        if (Global.getInt(mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
            z = false;
        }
        this.mWifiAssistantEnabled = z;
    }

    public static void init(Context mCtxt, WifiStateMachine mWsm, WifiConfigManager mWcs, WifiNetworkStateTraker mWns, WifiNative mWnt, WifiRomUpdateHelper mWruh) {
        mContext = mCtxt;
        mWifiStateMachine = mWsm;
        mWifiConfigManager = mWcs;
        mWifiNetworkStateTraker = mWns;
        mWifiNative = mWnt;
        mWifiRomUpdateHelper = mWruh;
        mSimpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    }

    public static OppoAutoConnectManager getInstance() {
        if (mContext == null) {
            Log.d(TAG, "mContext is null");
            return null;
        } else if (mWifiStateMachine == null) {
            Log.d(TAG, "mWifiStateMachine is null");
            return null;
        } else if (mWifiConfigManager == null) {
            Log.d(TAG, "mWifiConfigManager is null");
            return null;
        } else if (mWifiNative == null) {
            Log.d(TAG, "mWifiNative is null");
            return null;
        } else {
            synchronized (OppoAutoConnectManager.class) {
                if (mInstance == null) {
                    mInstance = new OppoAutoConnectManager(mContext, mWifiStateMachine, mWifiConfigManager, mWifiNative);
                }
            }
            return mInstance;
        }
    }
}
