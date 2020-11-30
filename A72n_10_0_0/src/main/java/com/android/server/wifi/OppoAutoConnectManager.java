package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.IpConfiguration;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.ColorOSTelephonyManager;
import android.util.Log;
import android.util.MathUtils;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.scanner.OppoWiFiScanBlockPolicy;
import com.android.server.wifi.util.GbkUtil;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.TelephonyUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoAutoConnectManager {
    private static final String ACTION_DEBUG_DATA_SHOW = "com.oppo.autoconnectmanager.intent.action.debug.data.show";
    private static final String ACTION_DEBUG_DISABLE = "com.oppo.autoconnectmanager.intent.action.debug.disable";
    private static final String ACTION_DEBUG_ENABLE = "com.oppo.autoconnectmanager.intent.action.debug.enable";
    private static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    private static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
    private static final int AIS_DISABLE_COUNT = 3;
    private static final int AIS_DISABLE_TIME = 86400;
    private static final int ASSOC_REJECT_EXP_BASE = 2;
    private static final int ASSOC_REJECT_MULTIPLE_BASE = 5;
    private static final int ASSOC_REJECT_RETRY_TIME_MIN = 4000;
    private static final int ASSOC_REJECT_STEP_COUNT = 4;
    private static final int AUTH_FAILURE_EXP_BASE = 2;
    private static final int AUTH_FAILURE_MULTIPLE_BASE = 10;
    private static final int AUTH_FAILURE_RETRY_TIME_MIN = 10000;
    private static final int AUTH_FAILURE_STEP_COUNT = 2;
    private static final String BOUYGUE_OPERATOR_AP_SSID = "Wi-Fi EAP Bouygues Telecom";
    private static final String BOUYGUE_WIFI_ADDED_PROPERTY = "persist.sys.oppo.bouygue_wifi_added";
    private static final int BUFFER_LENGTH = 40;
    private static final int CONTINUOUS_DEBOUNCE_DISCONNECT_INTERVAL_TIME = 1000;
    private static final int CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_COUNT = 5;
    private static final int CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME = 60;
    private static final int CONTINUOUS_DISABLED_COUNT_MAX = 12;
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static boolean DEBUG = false;
    private static final String DEFAULT_OPERATOR_AP_SSID = "Swisscom_Auto_Login,Wi-Fi EAP Bouygues Telecom,TURKCELL,  AIS SMART Login";
    private static final int DHCP_FAILURE_EXP_BASE = 2;
    private static final int DHCP_FAILURE_MULTIPLE_BASE = 5;
    private static final int DHCP_FAILURE_RETRY_TIME_MIN = 30000;
    private static final int DHCP_FAILURE_STEP_COUNT = 4;
    private static final int DISABLED_REASON_ASSOCIATION_REJECT = 2;
    private static final int DISABLED_REASON_AUTHENTICATION_NO_CREDENTIALS = 9;
    private static final int DISABLED_REASON_AUTH_FAILURE = 3;
    private static final int DISABLED_REASON_BY_WIFI_MANAGER = 11;
    private static final int DISABLED_REASON_CONTINUOUS_DEBOUNCE_DISCONNECT = 200;
    private static final int DISABLED_REASON_DHCP_FAILURE = 4;
    private static final int DISABLED_REASON_DISABLED_BAD_LINK = 1;
    private static final int DISABLED_REASON_DNS_FAILURE = 5;
    private static final int DISABLED_REASON_INVALID = 0;
    private static final int DISABLED_REASON_NO_INTERNET = 10;
    private static final int DISABLED_REASON_OPPO_AUTO_CONNECT_BASE = 200;
    private static final int DISABLED_REASON_TLS_VERSION_MISMATCH = 8;
    private static final int DISABLED_REASON_UNEXPECT_DISCONNECT = 15;
    private static final int DISABLED_REASON_UNKNWON = 102;
    private static final int DISABLED_REASON_WRONG_KEY = 13;
    private static final int DISABLE_UNECPECT_DISCONNECT_THROTTL = 360000;
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
    private static final int MCC_SUB_BEG = 0;
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int MNC_SUB_BEG = 3;
    private static final int MNC_SUB_END = 5;
    private static final int NO_INTERNET_STEP_COUNT = 1;
    private static final int NO_INTERNET_STEP_TIME = 120;
    private static final int OPERATOR_AUTOCONNECT_ENABLED = 1;
    private static final String OPERATOR_WIFI_ADDED_STR = "yes";
    private static final String OPERATOR_WIFI_NO_ADDED_STR = "no";
    private static final int RSSI_BAD = -85;
    private static final int RSSI_CAN_SCANNED = -90;
    private static final int RSSI_GOOD = -70;
    private static final int RSSI_INVALID = -127;
    private static final int RSSI_LOW = -78;
    private static final int RSSI_STEP = 3;
    private static final int SIM_INDEX_0 = 1;
    private static final int SIM_INDEX_1 = 2;
    private static final int SIM_INDEX_DOUBLE = 3;
    private static final int SIM_INDEX_NULL = 0;
    private static final int SINGTEL_AUTOCONNECT_ENABLED = 1;
    private static final String SINGTEL_OPERATOR_AP2_SSID = "Wireless@SGx";
    private static final String SINGTEL_OPERATOR_AP2_SSID_QUOTED = "\"Wireless@SGx\"";
    private static final String SINGTEL_OPERATOR_AP_SSID = "Singtel WIFI";
    private static final String SINGTEL_OPERATOR_AP_SSID_QUOTED = "\"Singtel WIFI\"";
    private static final String SINGTEL_WIFI_ADDED_PROPERTY = "persist.sys.oppo.singtel_wifi_added";
    private static final String SINGTEL_WIFI_ADDED_STR = "yes";
    private static final String SINGTEL_WIFI_NO_ADDED_STR = "no";
    private static final String SWISSCOM_OPERATOR_AP_SSID = "Swisscom_Auto_Login";
    private static final String SWISSCOM_WIFI_ADDED_PROPERTY = "persist.sys.oppo.swisscom_wifi_added";
    private static final String TAG = "OppoAutoConnectManager";
    private static final String THAILAND_OPERATOR_AP_SSID = "  AIS SMART Login";
    private static final String THAILAND_WIFI_ADDED_PROPERTY = "persist.sys.oppo.thailand_wifi_added";
    private static final int TRIGGER_DISABLE_UNEXPECT_DISCONNECT_THROTTL = 3;
    private static final String TURKCELL_OPERATOR_AP_SSID = "TURKCELL";
    private static final String TURKCELL_WIFI_ADDED_PROPERTY = "persist.sys.oppo.turkcell_wifi_added";
    private static final int UNEXPECT_DISCONNECT_DISABLE_MAXTIME = 30;
    private static final String WIFIFRE_DISCONNECT_APVENDORINFO = "apvendorinfo";
    private static final String WIFIFRE_DISCONNECT_CONFIGKEY = "configkey";
    private static final String WIFIFRE_DISCONNECT_EVENTID = "wifi_frequence_disconnect";
    private static final String WIFIFRE_DISCONNECT_REASONCODE = "reason";
    private static final int WIFIFRE_DISCONNECT_THOTTL = 10;
    private static final String WIFI_ASSISTANT_ROMUPDATE = "rom.update.wifi.assistant";
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final int WIFI_CONNECT_STATE_COMPLETED = 2;
    private static final int WIFI_CONNECT_STATE_DISCONNECTED = 3;
    private static final int WIFI_CONNECT_STATE_SUPPLICANT_COMPLETED = 1;
    private static final int WIFI_CONNECT_STATE_SUPPLICANT_DISCONNECTED = 0;
    private static final String WIFI_SCAN_ALWAYS_AVAILABLE = "wifi_scan_always_enabled";
    private static final int WRONG_KEY_EXP_BASE = 2;
    private static final int WRONG_KEY_MULTIPLE_BASE = 10;
    private static final int WRONG_KEY_RETRY_TIME_MIN = 10000;
    private static final int WRONG_KEY_STEP_COUNT = 2;
    private static ScanRequestProxy mScanRequestProxy;
    private static Context sContext;
    private static OppoAutoConnectManager sInstance;
    private static SimpleDateFormat sSimpleDateFormat;
    private static WifiConfigManager sWifiConfigManager;
    private static WifiNative sWifiNative;
    private static OppoWifiAssistantStateTraker sWifiNetworkStateTraker;
    private static WifiRomUpdateHelper sWifiRomUpdateHelper;
    private static ClientModeImpl sWifiStateMachine;
    private final BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {
        /* class com.android.server.wifi.OppoAutoConnectManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                OppoAutoConnectManager.this.loge("intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                OppoAutoConnectManager.this.loge("action is null");
            } else if (action.equals(OppoAutoConnectManager.ACTION_DEBUG_ENABLE)) {
                OppoAutoConnectManager.this.enableVerboseLogging(1);
            } else if (action.equals(OppoAutoConnectManager.ACTION_DEBUG_DISABLE)) {
                OppoAutoConnectManager.this.enableVerboseLogging(0);
            } else if (action.equals(OppoAutoConnectManager.ACTION_DEBUG_DATA_SHOW)) {
                OppoAutoConnectManager.this.sendDebugDataShowEvt();
            } else if (action.equals(OppoAutoConnectManager.ACTION_SCREEN_ON)) {
                OppoAutoConnectManager.this.sendScreenOnEvt();
            } else if (action.equals(OppoAutoConnectManager.ACTION_SCREEN_OFF)) {
                OppoAutoConnectManager.this.mScreenOn = false;
            }
        }
    };
    private int mConnectingNetId = -1;
    private int mDelayManuReassociateCount = 0;
    private HashMap<Integer, String> mDisableReasonStrHashMap = new HashMap<>();
    private HashMap<Integer, String> mEvtStrHashMap = new HashMap<>();
    private OppoAutoConnectManagerHandler mHandler;
    private String mInterfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN0;
    private OppoAutoConnectConfiguration mLastConnectedConfiguration = new OppoAutoConnectConfiguration();
    private int mLastManuConnectNetId = -1;
    private long mLastManuReassociateTime = 0;
    private int mLastManuReassociateToken = 0;
    private long mLastTriggerEvtTimeMillis = 0;
    private NetworkInfo mNetworkInfo = null;
    private String[] mOperaterSsids = null;
    private HashMap<String, OppoAutoConnectConfiguration> mOppoAutoConnectConfigurationHashMap = new HashMap<>();
    private List<ScanResult> mScanResultList = null;
    private boolean mScreenOn = true;
    private boolean mSettingManuConnect = false;
    private boolean mSupplicantCompleted = false;
    private boolean mThirdAPKConnect = false;
    private int mTriggerAutoConnectEvtSet = 0;
    private String mUnexpectDisconnectApVendorInfo = "";
    private String mUnexpectDisconnectConfigKeyForStatistic = "";
    private HashMap<Integer, Integer> mUnexpectDisconnectCount = new HashMap<>();
    private HashMap<String, Integer> mUnexpectDisconnectDisableCount = new HashMap<>();
    private int mUnexpectDisconnectReasonForStatistic = -1;
    private boolean mWifiAssistantEnabled = true;
    private boolean mWifiAssistantRomupdate = true;
    private boolean mWlanAssistConnect = true;

    public class OppoAutoConnectConfiguration {
        private String mConfigKey = null;
        private int mConituousDebounceDisconnectCount = 0;
        private int mContinuousDisabledCount = 0;
        private int mDisabledLevel = -127;
        private int mDisabledReason = 0;
        private long mDisabledUntilTime = System.currentTimeMillis();
        private long mLastConnectedTime = 0;
        private String mPwd = null;

        static /* synthetic */ int access$408(OppoAutoConnectConfiguration x0) {
            int i = x0.mConituousDebounceDisconnectCount;
            x0.mConituousDebounceDisconnectCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$508(OppoAutoConnectConfiguration x0) {
            int i = x0.mContinuousDisabledCount;
            x0.mContinuousDisabledCount = i + 1;
            return i;
        }

        public OppoAutoConnectConfiguration() {
        }

        public String toString() {
            return "\nmConfigKey:" + this.mConfigKey + "\nmDisabledReason:" + this.mDisabledReason + "\nmDisabledLevel:" + this.mDisabledLevel + "\nmContinuousDisabledCount:" + this.mContinuousDisabledCount + "\nmDisabledUntilTime:" + OppoAutoConnectManager.this.getFormatDateTime(this.mDisabledUntilTime) + "\nmLastConnectedTime:" + OppoAutoConnectManager.this.getFormatDateTime(this.mLastConnectedTime) + "\nmConituousDebounceDisconnectCount:" + this.mConituousDebounceDisconnectCount;
        }
    }

    private OppoAutoConnectConfiguration getAutoConnectConfiguration(String configKey) {
        HashMap<String, OppoAutoConnectConfiguration> hashMap = this.mOppoAutoConnectConfigurationHashMap;
        if (hashMap == null) {
            loge("mOppoAutoConnectConfigurationHashMap is null!!");
            return null;
        } else if (configKey == null) {
            loge("configKey is null!!");
            return null;
        } else if (hashMap.keySet().contains(configKey)) {
            return this.mOppoAutoConnectConfigurationHashMap.get(configKey);
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
            return;
        }
        OppoAutoConnectConfiguration oppoAutoConnectConfiguration = this.mLastConnectedConfiguration;
        if (oppoAutoConnectConfiguration == null) {
            logd("mLastConnectedConfiguration is null!!");
        } else if (configKey.equals(oppoAutoConnectConfiguration.mConfigKey)) {
            logd("clear mLastConnectedConfiguration!!!");
            this.mLastConnectedConfiguration.mConfigKey = null;
            this.mLastConnectedConfiguration.mPwd = null;
            this.mLastConnectedConfiguration.mDisabledReason = 0;
            this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount = 0;
            this.mLastConnectedConfiguration.mContinuousDisabledCount = 0;
            this.mLastConnectedConfiguration.mDisabledLevel = -127;
            this.mLastConnectedConfiguration.mDisabledUntilTime = 0;
            this.mLastConnectedConfiguration.mLastConnectedTime = 0;
        }
    }

    private int getCurrentScanResultBestLevel(List<ScanResult> srList, String configKey) {
        if (configKey == null || srList == null || srList.size() <= 0) {
            loge("configKey is null,or srList is null/empty!!");
            return -127;
        }
        int level = -127;
        for (ScanResult sr : srList) {
            String tmpConfigKey = WifiConfiguration.configKey(sr);
            if (tmpConfigKey != null && tmpConfigKey.equals(configKey) && sr.level > level) {
                level = sr.level;
            }
        }
        logd("configKey:" + configKey + " best level:" + level);
        return level;
    }

    /* JADX INFO: Multiple debug info for r2v1 int: [D('integerMultiple' int), D('baseTime' long)] */
    /* JADX INFO: Multiple debug info for r2v51 long: [D('keepDisabledUntilAdd' long), D('assocRejectRetryTimeMin' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x03e6  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0402  */
    private long getKeepDisabledUntil(OppoAutoConnectConfiguration oppoAutoConnConf) {
        int rssiGood;
        int disabledLevel;
        int authFailureExpBase;
        long keepDisabledUntilAdd;
        int wrongKeyExpBase;
        long keepDisabledUntilAdd2;
        long keepDisabledUntilAdd3;
        long keepDisabledUntilAdd4;
        long keepDisabledUntilAdd5;
        int assocRejectRetryTimeMin;
        int authFailureRetryTimeMin;
        long keepDisabledUntilAdd6;
        OppoAutoConnectManager oppoAutoConnectManager = this;
        if (oppoAutoConnConf == null) {
            oppoAutoConnectManager.logd("oppoAutoConnConf is null!!");
            return 0;
        }
        long baseTime = System.currentTimeMillis();
        int continuousDisabledCount = oppoAutoConnConf.mContinuousDisabledCount;
        int disabledLevel2 = oppoAutoConnConf.mDisabledLevel;
        int rssiGood2 = getRssiGood();
        int keepDisabledTimeMax = getKeepDisabledTimeMax();
        int dhcpFailureStepCount = getDhcpFailureStepCount();
        int dhcpFailureMultipleBase = getDhcpFailureMultipleBase();
        int dhcpFailureExpBase = getDhcpFailureExpBase();
        int dhcpFailureRetryTimeMin = getDhcpFailureRetryTimeMin();
        int assocRejectStepCount = getAssocRejectStepCount();
        int assocRejectMultipleBase = getAssocRejectMultipleBase();
        int assocRejectExpBase = getAssocRejectExpBase();
        int assocRejectRetryTimeMin2 = getAssocRejectRetryTimeMin();
        int wrongKeyStepCount = getWrongKeyStepCount();
        int wrongKeyMultipleBase = getWrongKeyMultipleBase();
        int wrongKeyExpBase2 = getWrongKeyExpBase();
        int wrongKeyRetryTimeMin = getWrongKeyRetryTimeMin();
        int authFailureStepCount = getAuthFailureStepCount();
        int authFailureMultipleBase = getAuthFailureMultipleBase();
        int authFailureExpBase2 = getAuthFailureExpBase();
        int authFailureRetryTimeMin2 = getAuthFailureRetryRimeMin();
        getContinuousDebounceDisconnectStepCount();
        int continuousDebounceDisconnectStepTime = getContinuousDebounceDisconnectStepTime();
        int noNetworkStepCount = getNoInternetStepCount();
        int noNetworkStepTime = getNoInternetStepTime();
        int continuousDisabledCountMax = getContinuousDisabledCountMax();
        oppoAutoConnectManager.logd("mDisabledReason:" + oppoAutoConnConf.mDisabledReason + ",mContinuousDisabledCount:" + oppoAutoConnConf.mContinuousDisabledCount);
        int i = oppoAutoConnConf.mDisabledReason;
        if (i != 0) {
            if (i == 13) {
                authFailureExpBase = assocRejectExpBase;
                if (continuousDisabledCount < wrongKeyStepCount) {
                    disabledLevel = wrongKeyRetryTimeMin;
                    rssiGood = wrongKeyStepCount;
                    keepDisabledUntilAdd2 = oppoAutoConnConf.mDisabledUntilTime;
                    wrongKeyExpBase = wrongKeyExpBase2;
                    keepDisabledUntilAdd = 0;
                } else {
                    int integerMultiple = continuousDisabledCount / wrongKeyStepCount;
                    if (disabledLevel2 > rssiGood2) {
                        wrongKeyExpBase = wrongKeyExpBase2;
                        keepDisabledUntilAdd3 = ((long) Math.pow((double) wrongKeyExpBase, (double) integerMultiple)) * 1000;
                    } else {
                        wrongKeyExpBase = wrongKeyExpBase2;
                        keepDisabledUntilAdd3 = (long) (wrongKeyMultipleBase * integerMultiple * 1000);
                    }
                    rssiGood = wrongKeyStepCount;
                    disabledLevel = wrongKeyRetryTimeMin;
                    keepDisabledUntilAdd = keepDisabledUntilAdd3 > ((long) disabledLevel) ? keepDisabledUntilAdd3 : (long) disabledLevel;
                    keepDisabledUntilAdd2 = baseTime;
                }
            } else if (i == 15) {
                authFailureExpBase = assocRejectExpBase;
                int unexpectDisconnectCount = 0;
                oppoAutoConnectManager = this;
                HashMap<String, Integer> hashMap = oppoAutoConnectManager.mUnexpectDisconnectDisableCount;
                if (hashMap == null || hashMap.get(oppoAutoConnConf.mConfigKey) != null) {
                    HashMap<String, Integer> hashMap2 = oppoAutoConnectManager.mUnexpectDisconnectDisableCount;
                    if (hashMap2 != null && hashMap2.get(oppoAutoConnConf.mConfigKey) != null) {
                        int unexpectDisconnectCount2 = oppoAutoConnectManager.mUnexpectDisconnectDisableCount.get(oppoAutoConnConf.mConfigKey).intValue() + 1;
                        int i2 = 30;
                        if (unexpectDisconnectCount2 <= 30) {
                            i2 = unexpectDisconnectCount2;
                        }
                        unexpectDisconnectCount = i2;
                        oppoAutoConnectManager.mUnexpectDisconnectDisableCount.put(oppoAutoConnConf.mConfigKey, Integer.valueOf(unexpectDisconnectCount));
                        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
                        if (!(wifiRomUpdateHelper == null || sWifiNative == null || unexpectDisconnectCount != wifiRomUpdateHelper.getIntegerValue("WIFIFRE_DISCONNECT_THOTTL", 10).intValue())) {
                            oppoAutoConnectManager.mUnexpectDisconnectConfigKeyForStatistic = oppoAutoConnConf.mConfigKey;
                            oppoAutoConnectManager.mUnexpectDisconnectApVendorInfo = sWifiNative.getApVendorSpec(oppoAutoConnectManager.mInterfaceName);
                            handleOppoStatistic();
                        }
                    }
                } else {
                    oppoAutoConnectManager.mUnexpectDisconnectDisableCount.put(oppoAutoConnConf.mConfigKey, 1);
                    unexpectDisconnectCount = 0 + 1;
                }
                keepDisabledUntilAdd2 = baseTime;
                rssiGood = wrongKeyStepCount;
                disabledLevel = wrongKeyRetryTimeMin;
                keepDisabledUntilAdd = (long) (unexpectDisconnectCount * continuousDebounceDisconnectStepTime * 1000);
                wrongKeyExpBase = wrongKeyExpBase2;
            } else if (i == 102) {
                authFailureExpBase = assocRejectExpBase;
                oppoAutoConnectManager = this;
                wrongKeyExpBase = wrongKeyExpBase2;
                rssiGood = wrongKeyStepCount;
                disabledLevel = wrongKeyRetryTimeMin;
            } else if (i == 200) {
                authFailureExpBase = assocRejectExpBase;
                wrongKeyExpBase = wrongKeyExpBase2;
                rssiGood = wrongKeyStepCount;
                disabledLevel = wrongKeyRetryTimeMin;
                keepDisabledUntilAdd = (long) (continuousDisabledCount * continuousDebounceDisconnectStepTime * 1000);
                keepDisabledUntilAdd2 = baseTime;
                oppoAutoConnectManager = this;
            } else if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        if (i == 5) {
                            oppoAutoConnectManager = this;
                            wrongKeyExpBase = wrongKeyExpBase2;
                            rssiGood = wrongKeyStepCount;
                            authFailureExpBase = assocRejectExpBase;
                            disabledLevel = wrongKeyRetryTimeMin;
                        } else if (i != 10) {
                            if (i != 11) {
                                oppoAutoConnectManager = this;
                                wrongKeyExpBase = wrongKeyExpBase2;
                                rssiGood = wrongKeyStepCount;
                                authFailureExpBase = assocRejectExpBase;
                                disabledLevel = wrongKeyRetryTimeMin;
                            } else {
                                oppoAutoConnectManager = this;
                                wrongKeyExpBase = wrongKeyExpBase2;
                                rssiGood = wrongKeyStepCount;
                                authFailureExpBase = assocRejectExpBase;
                                disabledLevel = wrongKeyRetryTimeMin;
                            }
                        } else if (continuousDisabledCount % noNetworkStepCount != 0) {
                            oppoAutoConnectManager = this;
                            keepDisabledUntilAdd = 0;
                            wrongKeyExpBase = wrongKeyExpBase2;
                            rssiGood = wrongKeyStepCount;
                            authFailureExpBase = assocRejectExpBase;
                            keepDisabledUntilAdd2 = oppoAutoConnConf.mDisabledUntilTime;
                            disabledLevel = wrongKeyRetryTimeMin;
                        } else {
                            oppoAutoConnectManager = this;
                            rssiGood = wrongKeyStepCount;
                            authFailureExpBase = assocRejectExpBase;
                            disabledLevel = wrongKeyRetryTimeMin;
                            keepDisabledUntilAdd = (long) (noNetworkStepTime * (continuousDisabledCount / noNetworkStepCount) * 1000);
                            keepDisabledUntilAdd2 = baseTime;
                            wrongKeyExpBase = wrongKeyExpBase2;
                        }
                    } else if (continuousDisabledCount < dhcpFailureStepCount) {
                        oppoAutoConnectManager = this;
                        rssiGood = wrongKeyStepCount;
                        authFailureExpBase = assocRejectExpBase;
                        keepDisabledUntilAdd2 = oppoAutoConnConf.mDisabledUntilTime;
                        disabledLevel = wrongKeyRetryTimeMin;
                        wrongKeyExpBase = wrongKeyExpBase2;
                        keepDisabledUntilAdd = 0;
                    } else {
                        int integerMultiple2 = continuousDisabledCount / dhcpFailureStepCount;
                        if (disabledLevel2 > rssiGood2) {
                            authFailureRetryTimeMin = authFailureRetryTimeMin2;
                            assocRejectRetryTimeMin = assocRejectRetryTimeMin2;
                            keepDisabledUntilAdd6 = ((long) Math.pow((double) dhcpFailureExpBase, (double) integerMultiple2)) * 1000;
                        } else {
                            authFailureRetryTimeMin = authFailureRetryTimeMin2;
                            assocRejectRetryTimeMin = assocRejectRetryTimeMin2;
                            keepDisabledUntilAdd6 = (long) (dhcpFailureMultipleBase * integerMultiple2 * 1000);
                        }
                        oppoAutoConnectManager = this;
                        keepDisabledUntilAdd = keepDisabledUntilAdd6 > ((long) dhcpFailureRetryTimeMin) ? keepDisabledUntilAdd6 : (long) dhcpFailureRetryTimeMin;
                        wrongKeyExpBase = wrongKeyExpBase2;
                        rssiGood = wrongKeyStepCount;
                        disabledLevel = wrongKeyRetryTimeMin;
                        authFailureExpBase = assocRejectExpBase;
                        keepDisabledUntilAdd2 = baseTime;
                    }
                } else if (continuousDisabledCount < authFailureStepCount) {
                    oppoAutoConnectManager = this;
                    keepDisabledUntilAdd = 0;
                    rssiGood = wrongKeyStepCount;
                    disabledLevel = wrongKeyRetryTimeMin;
                    authFailureExpBase = assocRejectExpBase;
                    keepDisabledUntilAdd2 = oppoAutoConnConf.mDisabledUntilTime;
                    wrongKeyExpBase = wrongKeyExpBase2;
                } else {
                    int integerMultiple3 = continuousDisabledCount / authFailureStepCount;
                    if (disabledLevel2 > rssiGood2) {
                        keepDisabledUntilAdd5 = ((long) Math.pow((double) authFailureExpBase2, (double) integerMultiple3)) * 1000;
                    } else {
                        keepDisabledUntilAdd5 = (long) (authFailureMultipleBase * integerMultiple3 * 1000);
                    }
                    oppoAutoConnectManager = this;
                    authFailureExpBase = assocRejectExpBase;
                    rssiGood = wrongKeyStepCount;
                    disabledLevel = wrongKeyRetryTimeMin;
                    keepDisabledUntilAdd = keepDisabledUntilAdd5 > ((long) authFailureRetryTimeMin2) ? keepDisabledUntilAdd5 : (long) authFailureRetryTimeMin2;
                    keepDisabledUntilAdd2 = baseTime;
                    wrongKeyExpBase = wrongKeyExpBase2;
                }
            } else if (continuousDisabledCount < assocRejectStepCount) {
                oppoAutoConnectManager = this;
                wrongKeyExpBase = wrongKeyExpBase2;
                rssiGood = wrongKeyStepCount;
                disabledLevel = wrongKeyRetryTimeMin;
                authFailureExpBase = assocRejectExpBase;
                keepDisabledUntilAdd2 = oppoAutoConnConf.mDisabledUntilTime;
                keepDisabledUntilAdd = 0;
            } else {
                int integerMultiple4 = continuousDisabledCount / assocRejectStepCount;
                if (disabledLevel2 > rssiGood2) {
                    keepDisabledUntilAdd4 = ((long) Math.pow((double) assocRejectExpBase, (double) integerMultiple4)) * 1000;
                } else {
                    keepDisabledUntilAdd4 = (long) (assocRejectMultipleBase * integerMultiple4 * 1000);
                }
                authFailureExpBase = assocRejectExpBase;
                wrongKeyExpBase = wrongKeyExpBase2;
                rssiGood = wrongKeyStepCount;
                disabledLevel = wrongKeyRetryTimeMin;
                keepDisabledUntilAdd = keepDisabledUntilAdd4 > ((long) assocRejectRetryTimeMin2) ? keepDisabledUntilAdd4 : (long) assocRejectRetryTimeMin2;
                keepDisabledUntilAdd2 = baseTime;
                oppoAutoConnectManager = this;
            }
            oppoAutoConnectManager.logd("keep Disabled from base Time:" + oppoAutoConnectManager.getFormatDateTime(keepDisabledUntilAdd2) + " add " + keepDisabledUntilAdd + " millis");
            if (keepDisabledUntilAdd - ((long) keepDisabledTimeMax) > 0) {
                oppoAutoConnectManager.logd("keepDisabledUntilAdd is too larger adjust to " + keepDisabledTimeMax + " millis");
                keepDisabledUntilAdd = (long) keepDisabledTimeMax;
            }
            if (continuousDisabledCount >= continuousDisabledCountMax) {
                ClientModeImpl clientModeImpl = sWifiStateMachine;
                if (ClientModeImpl.isNotChineseOperator()) {
                    oppoAutoConnectManager.logd("keep disabled for oversea");
                    keepDisabledUntilAdd = (long) keepDisabledTimeMax;
                }
            }
            return keepDisabledUntilAdd2 + keepDisabledUntilAdd;
        }
        wrongKeyExpBase = wrongKeyExpBase2;
        rssiGood = wrongKeyStepCount;
        authFailureExpBase = assocRejectExpBase;
        disabledLevel = wrongKeyRetryTimeMin;
        keepDisabledUntilAdd = 0;
        keepDisabledUntilAdd2 = baseTime;
        oppoAutoConnectManager.logd("keep Disabled from base Time:" + oppoAutoConnectManager.getFormatDateTime(keepDisabledUntilAdd2) + " add " + keepDisabledUntilAdd + " millis");
        if (keepDisabledUntilAdd - ((long) keepDisabledTimeMax) > 0) {
        }
        if (continuousDisabledCount >= continuousDisabledCountMax) {
        }
        return keepDisabledUntilAdd2 + keepDisabledUntilAdd;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleManuConnect(int netId) {
        this.mLastManuReassociateToken = 0;
        this.mDelayManuReassociateCount = 0;
        removeAutoConnectConfiguration(netId);
        clearLastConnectedConfiguration(netId);
        clearUnexpectDisconnectCount();
    }

    public void clearUnexpectDisconnectCount() {
        HashMap<String, Integer> hashMap = this.mUnexpectDisconnectDisableCount;
        if (hashMap != null) {
            hashMap.clear();
        }
    }

    public void handleunexpectedDiconnectDisabled(long connectionTimeStamp, int netId, int disconnectReason) {
        WifiRomUpdateHelper wifiRomUpdateHelper;
        long ConnectedDuration = System.currentTimeMillis() - connectionTimeStamp;
        if (this.mUnexpectDisconnectCount != null && sWifiStateMachine != null && !this.mScreenOn && (wifiRomUpdateHelper = sWifiRomUpdateHelper) != null) {
            if (ConnectedDuration <= ((long) wifiRomUpdateHelper.getIntegerValue("BEGIN_DISABLE_UNECPECT_DISCONNECT_THROTTL", Integer.valueOf((int) DISABLE_UNECPECT_DISCONNECT_THROTTL)).intValue())) {
                if (this.mUnexpectDisconnectCount.get(Integer.valueOf(netId)) != null) {
                    this.mUnexpectDisconnectCount.put(Integer.valueOf(netId), Integer.valueOf(this.mUnexpectDisconnectCount.get(Integer.valueOf(netId)).intValue() + 1));
                } else if (this.mUnexpectDisconnectCount.get(Integer.valueOf(netId)) == null) {
                    this.mUnexpectDisconnectCount.put(Integer.valueOf(netId), 1);
                }
            } else if (ConnectedDuration > ((long) sWifiRomUpdateHelper.getIntegerValue("BEGIN_DISABLE_UNECPECT_DISCONNECT_THROTTL", Integer.valueOf((int) DISABLE_UNECPECT_DISCONNECT_THROTTL)).intValue())) {
                this.mUnexpectDisconnectCount.put(Integer.valueOf(netId), 0);
                clearUnexpectDisconnectCount();
            }
            if (this.mUnexpectDisconnectCount.get(Integer.valueOf(netId)) != null && this.mUnexpectDisconnectCount.get(Integer.valueOf(netId)).intValue() >= sWifiRomUpdateHelper.getIntegerValue("TRIGGER_DISABLE_UNEXPECT_DISCONNECT_THROTTL", 3).intValue() && disconnectReason != 3) {
                Log.d(TAG, "prepare to disable  unexpected disconnect");
                this.mUnexpectDisconnectReasonForStatistic = disconnectReason;
                this.mUnexpectDisconnectCount.put(Integer.valueOf(netId), 0);
                disableNetwork(netId, 15);
            }
        }
    }

    public void handleOppoStatistic() {
        HashMap<String, String> map = new HashMap<>();
        map.put(WIFIFRE_DISCONNECT_CONFIGKEY, this.mUnexpectDisconnectConfigKeyForStatistic);
        map.put(WIFIFRE_DISCONNECT_APVENDORINFO, this.mUnexpectDisconnectApVendorInfo);
        map.put(WIFIFRE_DISCONNECT_REASONCODE, Integer.toString(this.mUnexpectDisconnectReasonForStatistic));
        OppoStatistics.onCommon(sContext, "wifi_fool_proof", WIFIFRE_DISCONNECT_EVENTID, map, false);
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
        removeAutoConnectConfiguration(netId);
        clearLastConnectedConfiguration(netId);
    }

    public void handleWpsConnect(boolean startConnect, boolean resetState) {
        logd("handleWpsConnect StartConnect = " + startConnect + "Updatestate = " + resetState);
    }

    public void handleWpsSupplicantStateChanged(Message msg) {
        logd("handleWpsSupplicantStateChanged");
        if (msg == null) {
            logd("msg is null!!");
        } else if (((StateChangeResult) msg.obj).state == SupplicantState.DISCONNECTED) {
            manuReassociate();
        }
    }

    private boolean checkBasicConditions() {
        if (checkInterfaceNull()) {
            return false;
        }
        if (getWifiState() != 3) {
            logd("Wifi is not in enabled state!!");
            return false;
        } else if (!getIsOppoManuConnect()) {
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
        new ArrayList();
        new ArrayList();
        logi("try add disabled unknown reason!");
        List<WifiConfiguration> canEnabledNetworksList = getNormalDisabledNetworks(scannedConfiguredNetworksList);
        List<WifiConfiguration> tmpCanEnabledNetworkList = getCanEnabledConfiguredNetworks(scannedConfiguredNetworksList);
        if (canEnabledNetworksList == null || tmpCanEnabledNetworkList == null) {
            canEnabledNetworksList = tmpCanEnabledNetworkList;
        } else {
            logi("try add disabled networks in mOppoAutoConnectConfigurationHashMap!");
            canEnabledNetworksList.addAll(tmpCanEnabledNetworkList);
        }
        if (canEnabledNetworksList == null || canEnabledNetworksList.size() <= 0) {
            long timeGap = System.currentTimeMillis() - this.mLastTriggerEvtTimeMillis;
            logd("time gap from trigger evt to now is :" + timeGap + "ms");
            if (getTriggerAutoConnectEvt(65535) && isAllScannedConfiguredNetworksDisabled) {
                logd("all networks are disabled,but " + getTriggerAutoConnectEvtString() + " happened, trigger auto connect immediately!");
                if (this.mWifiAssistantEnabled) {
                    canEnabledNetworksList = getNetworksExcept(scannedConfiguredNetworksList, 10);
                } else {
                    canEnabledNetworksList = scannedConfiguredNetworksList;
                }
            }
        }
        enableNetworks(canEnabledNetworksList);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanResult() {
        String ssidtmp;
        this.mScanResultList = getScanResultList();
        if (getIsOppoManuConnecting()) {
            logd("man connect in progress,manuReassociate if possible!!");
            if (!wifiIsConnectedOrConnecting()) {
                int netId = getOppoManuConnectNetworkId();
                WifiConfiguration wConf = getWifiConfiguration(netId);
                String ssid = null;
                ArrayList<Byte> gbkSsid = null;
                boolean isHiddenGBKSsid = false;
                boolean isHiddenAP = false;
                if (wConf != null) {
                    ssid = wConf.SSID;
                    isHiddenAP = wConf.hiddenSSID;
                }
                if (isHiddenAP && sWifiNative != null && ssid != null && GbkUtil.isGbkSsid(ssid)) {
                    if (ssid.length() > 1 && ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"') {
                        ssidtmp = ssid.substring(1, ssid.length() - 1);
                    } else {
                        ssidtmp = ssid;
                    }
                    gbkSsid = GbkUtil.stringToByteArrayList(ssidtmp);
                    wConf.SSID = NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList(gbkSsid));
                    if (!sWifiNative.isSameNetwork(this.mInterfaceName, wConf)) {
                        isHiddenGBKSsid = true;
                    }
                    wConf.SSID = ssid;
                }
                if (isHiddenGBKSsid) {
                    logd("connect hidden gbk ssid = " + gbkSsid);
                    sWifiStateMachine.startConnectToNetwork(netId, 1000, "any");
                    return;
                }
                manuReassociate();
                return;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateNetworkDisabledCount(int netId, int reason) {
        logd("handleUpdateNetworkDisabledCount,netId = " + netId + ",reason= " + reason + "(" + this.mDisableReasonStrHashMap.get(Integer.valueOf(reason)) + ")");
        if (!checkInterfaceNull()) {
            if (reason == 102 || reason == 11) {
                logd("no need to record reason,do nothing!!");
            } else if (this.mOppoAutoConnectConfigurationHashMap == null) {
                loge("mOppoAutoConnectConfigurationHashMap is null!!");
            } else {
                WifiConfiguration wc = getWifiConfiguration(netId);
                if (wc == null) {
                    loge("wc is null!!");
                    return;
                }
                String configKey = wc.configKey();
                OppoAutoConnectConfiguration oppoAutoConnConf = getAutoConnectConfiguration(configKey);
                if (oppoAutoConnConf == null) {
                    oppoAutoConnConf = new OppoAutoConnectConfiguration();
                    oppoAutoConnConf.mConfigKey = configKey;
                }
                oppoAutoConnConf.mDisabledLevel = getCurrentScanResultBestLevel(getScanResultList(), configKey);
                if (oppoAutoConnConf.mDisabledLevel == -127) {
                    oppoAutoConnConf.mDisabledLevel = RSSI_CAN_SCANNED;
                }
                if (oppoAutoConnConf.mDisabledReason != reason) {
                    oppoAutoConnConf.mContinuousDisabledCount = 1;
                } else {
                    OppoAutoConnectConfiguration.access$508(oppoAutoConnConf);
                }
                oppoAutoConnConf.mDisabledReason = reason;
                if (isThailandAisAutoConnect(oppoAutoConnConf)) {
                    oppoAutoConnConf.mDisabledUntilTime = getKeepAisDisabledUntil(oppoAutoConnConf);
                } else {
                    oppoAutoConnConf.mDisabledUntilTime = getKeepDisabledUntil(oppoAutoConnConf);
                }
                addOrUpdateAutoConnectConfiguration(oppoAutoConnConf);
            }
        }
    }

    private boolean isThailandAisAutoConnect(OppoAutoConnectConfiguration oppoAutoConnConf) {
        if (oppoAutoConnConf == null) {
            logd("oppoAutoConnConf is null!!");
            return false;
        }
        if (("\"  AIS SMART Login\"-" + WifiConfiguration.KeyMgmt.strings[2]).equals(oppoAutoConnConf.mConfigKey)) {
            return true;
        }
        return false;
    }

    private long getKeepAisDisabledUntil(OppoAutoConnectConfiguration oppoAutoConnConf) {
        if (oppoAutoConnConf == null) {
            logd("oppoAutoConnConf is null!!");
            return 0;
        }
        long keepDisabledUntilAdd = 0;
        long baseTime = System.currentTimeMillis();
        int continuousDisabledCount = oppoAutoConnConf.mContinuousDisabledCount;
        logd("AIS ContinuousDisabledCount:" + continuousDisabledCount);
        if (continuousDisabledCount > 3) {
            keepDisabledUntilAdd = 86400000;
        }
        return baseTime + keepDisabledUntilAdd;
    }

    public void enableAisAutoConnect() {
        WifiConfiguration savedWconf = null;
        String AisConfigKey = "\"  AIS SMART Login\"-" + WifiConfiguration.KeyMgmt.strings[2];
        WifiConfigManager wifiConfigManager = sWifiConfigManager;
        if (wifiConfigManager != null) {
            savedWconf = wifiConfigManager.getConfiguredNetwork(AisConfigKey);
        }
        OppoAutoConnectConfiguration autoConf = getAutoConnectConfiguration(AisConfigKey);
        if (savedWconf != null && autoConf != null) {
            sWifiConfigManager.enableNetworkEx(savedWconf.networkId, false, 1000, false);
            autoConf.mDisabledUntilTime = System.currentTimeMillis();
            addOrUpdateAutoConnectConfiguration(autoConf);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkDisabled(int netId, int reason) {
        logd("handleNetworkDisabled:" + netId);
        handleUpdateNetworkDisabledCount(netId, reason);
        enableNetworksConditional();
        if (checkBasicConditions()) {
            startAutoConnect();
        }
    }

    public void sendNetworkDisabledEvt(int netId, int reason) {
        handleNetworkDisabled(netId, reason);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkDeleted(WifiConfiguration wConf) {
        OppoAutoConnectConfiguration oppoAutoConnectConfiguration;
        logd("handleNetworkDeleted");
        if (wConf == null) {
            loge("wConf is null!!");
            return;
        }
        String configKey = wConf.configKey();
        boolean needConnect = false;
        if (!(configKey == null || (oppoAutoConnectConfiguration = this.mLastConnectedConfiguration) == null || !configKey.equals(oppoAutoConnectConfiguration.mConfigKey))) {
            this.mLastTriggerEvtTimeMillis = System.currentTimeMillis();
            setTriggerAutoConnectEvt(32);
            needConnect = true;
            logd("current connected network deleted,trigger auto connect!");
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
            if (state == 0) {
                logd("supplicant disconnected netId:" + networkId);
                WifiConfiguration selectedConf = getWifiConfiguration(networkId);
                if (getWifiState() != 3) {
                    logd("wifi is not in enabled state yet!!");
                } else if (!(selectedConf == null || selectedConf.status == 1)) {
                    logd("try to reconnect the manu network id until disabled ,net id:" + networkId);
                    if (getOppoManuConnectNetworkId() != networkId) {
                        manuReassociate();
                        handleEnableAllNetworks();
                    } else if (getIsOppoManuConnecting()) {
                        this.mDelayManuReassociateCount = (this.mDelayManuReassociateCount + 1) % 2;
                        if (this.mDelayManuReassociateCount == 1) {
                            manuReassociate();
                        } else {
                            manuReassociateDelayed();
                        }
                    } else if (!getIsOppoManuConnected()) {
                        reconnect(selectedConf.networkId);
                    }
                }
                OppoAutoConnectConfiguration oppoAutoConnectConfiguration = this.mLastConnectedConfiguration;
                if (oppoAutoConnectConfiguration != null && oppoAutoConnectConfiguration.mConfigKey != null) {
                    if (!this.mLastConnectedConfiguration.mConfigKey.equals(configKey)) {
                        logd("configKey mismatch,ignore!!");
                        return;
                    }
                    long currTime = System.currentTimeMillis();
                    int continuousDebounceDisconnectIntervalTime = getContinuousDebounceDisconnectIntervalTime();
                    int continuousDebounceDisconnectStepCount = getContinuousDebounceDisconnectStepCount();
                    if (currTime - this.mLastConnectedConfiguration.mLastConnectedTime <= ((long) continuousDebounceDisconnectIntervalTime)) {
                        if (this.mSupplicantCompleted) {
                            OppoAutoConnectConfiguration.access$408(this.mLastConnectedConfiguration);
                        }
                    } else if (this.mSupplicantCompleted) {
                        this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount = 0;
                        OppoAutoConnectConfiguration oaccf = getAutoConnectConfiguration(configKey);
                        if (oaccf != null && oaccf.mDisabledReason == 200) {
                            removeAutoConnectConfiguration(this.mLastConnectedConfiguration.mConfigKey);
                        }
                    } else {
                        logd("not a couple connect-disconnect evt!,ignore");
                    }
                    logd("configKey:" + configKey + " mConituousDebounceDisconnectCount=" + this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount);
                    if (this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount >= continuousDebounceDisconnectStepCount && this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount % continuousDebounceDisconnectStepCount == 0 && this.mSupplicantCompleted) {
                        logi("mConituousDebounceDisconnectCount = " + this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount + " disable it!");
                        disableNetwork(networkId, ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS);
                    }
                    this.mSupplicantCompleted = false;
                }
            } else if (state == 1) {
                OppoAutoConnectConfiguration oacc = this.mLastConnectedConfiguration;
                if (oacc == null) {
                    logd("mLastConnectedConfiguration is null!!");
                    this.mSupplicantCompleted = true;
                    return;
                }
                if (oacc.mConfigKey == null || !this.mLastConnectedConfiguration.mConfigKey.equals(configKey)) {
                    this.mLastConnectedConfiguration.mConfigKey = configKey;
                    this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount = 0;
                }
                this.mLastConnectedConfiguration.mLastConnectedTime = System.currentTimeMillis();
                this.mSupplicantCompleted = true;
            } else if (state == 2) {
                OppoAutoConnectConfiguration oacc2 = getAutoConnectConfiguration(configKey);
                if (oacc2 != null && oacc2.mDisabledReason != 200) {
                    removeAutoConnectConfiguration(configKey);
                }
            } else if (state == 3) {
                NetworkInfo.DetailedState detailedState = NetworkInfo.DetailedState.DISCONNECTED;
                NetworkInfo networkInfo = this.mNetworkInfo;
                if (networkInfo != null) {
                    networkInfo.getDetailedState();
                }
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
        StateChangeResult stateChangeResult;
        if (message != null && (stateChangeResult = (StateChangeResult) message.obj) != null) {
            SupplicantState state = stateChangeResult.state;
            if (SupplicantState.isConnecting(state)) {
                this.mConnectingNetId = stateChangeResult.networkId;
                logd("connecting netId:" + this.mConnectingNetId);
            }
            if (state == SupplicantState.COMPLETED) {
                handleWifiConnectStateChanged(1, stateChangeResult.networkId);
            } else if (state == SupplicantState.DISCONNECTED) {
                handleWifiConnectStateChanged(0, stateChangeResult.networkId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendScreenOnEvt() {
        sendMessage(256);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOn() {
        setTriggerAutoConnectEvt(256);
        this.mLastTriggerEvtTimeMillis = System.currentTimeMillis();
        logd("handleScreenOn:" + this.mLastTriggerEvtTimeMillis);
        clearUnexpectDisconnectCount();
        this.mScreenOn = true;
    }

    public void sendWifiStateChangedEvt(int wifiState) {
        handleWifiStateChanged(wifiState);
    }

    private void handleWifiStateChanged(int wifiState) {
        logd("handleWifiStateChanged,state = " + wifiState);
        if (wifiState == 3) {
            this.mLastTriggerEvtTimeMillis = System.currentTimeMillis();
            setTriggerAutoConnectEvt(1024);
        } else if (wifiState == 1 && Settings.Global.getInt(sContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE, 1) == 0) {
            this.mScanResultList = new ArrayList();
        }
    }

    public void sendEnableAllNetworksEvt() {
        sendMessage(512);
    }

    /* access modifiers changed from: package-private */
    public void handleEnableAllNetworks() {
        logd("handleEnableAllNetworks");
        this.mScanResultList = getScanResultList();
        if (!getIsOppoManuConnecting()) {
            enableNetworksConditional();
            if (checkBasicConditions()) {
                logd("trigger auto connect as soon as possible!!");
                startAutoConnect();
            }
        }
    }

    public void sendNetworkReSavedEvt(WifiConfiguration oldConfig) {
        sendMessage(2048, oldConfig);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
            if (currConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC && oldConfig.getIpAssignment() == IpConfiguration.IpAssignment.DHCP) {
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
                        logd("curr (id:" + currId + " pwd:" + currPwd + ") old (id:" + oldId + " pwd:" + oldPwd + ")");
                        pwdChanged = true;
                    }
                }
                logd("pwd changed: " + pwdChanged);
                if (pwdChanged && oacc != null && (oacc.mDisabledReason == 13 || oacc.mDisabledReason == 3)) {
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
            evtStr = evtStr + this.mEvtStrHashMap.get(256);
        }
        if (getTriggerAutoConnectEvt(1024)) {
            evtStr = evtStr + "," + this.mEvtStrHashMap.get(1024);
        }
        if (!getTriggerAutoConnectEvt(32)) {
            return evtStr;
        }
        return evtStr + "," + this.mEvtStrHashMap.get(32);
    }

    private boolean checkCanInternet(WifiConfiguration wifiConfig) {
        if (sWifiNetworkStateTraker == null) {
            logd("sWifiNetworkStateTraker is null!!");
            return false;
        } else if (wifiConfig == null) {
            logd("wifiConfig is null!!");
            return false;
        } else {
            String configKey = wifiConfig.configKey();
            if (configKey == null) {
                return false;
            }
            List<OppoWifiAssistantRecord> wnrList = sWifiNetworkStateTraker.getWifiNetworkRecords();
            if (wnrList == null || wnrList.size() <= 0 || !this.mWifiAssistantRomupdate) {
                logd("wnrList is null !!");
                return false;
            }
            for (OppoWifiAssistantRecord wnr : wnrList) {
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
        if (sWifiNetworkStateTraker == null) {
            logd("sWifiNetworkStateTraker is null!!");
            return null;
        } else if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return null;
        } else {
            List<WifiConfiguration> internetedNetworkList = new ArrayList<>();
            List<OppoWifiAssistantRecord> wnrList = sWifiNetworkStateTraker.getWifiNetworkRecords();
            if (wnrList == null || wnrList.size() <= 0 || !this.mWifiAssistantRomupdate) {
                logd("wnrList is null !!");
                return null;
            }
            for (WifiConfiguration wc : wcList) {
                String configKey = wc.configKey();
                if (configKey != null) {
                    for (OppoWifiAssistantRecord wnr : wnrList) {
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
        List<WifiConfiguration> scannedConfiguredNetworks = new ArrayList<>();
        for (WifiConfiguration wc : wcList) {
            String wcConfigKey = wc.configKey();
            if (wcConfigKey != null) {
                Iterator<ScanResult> it = srList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ScanResult sr = it.next();
                    String srConfigKey = WifiConfiguration.configKey(sr);
                    if (srConfigKey != null) {
                        if (wcConfigKey.equals(srConfigKey)) {
                            if (DEBUG) {
                                OppoAutoConnectConfiguration oacc = getAutoConnectConfiguration(wcConfigKey);
                                int reason = oacc != null ? oacc.mDisabledReason : wc.disableReason;
                                logd("find configured and scanned " + wcConfigKey + " disabled reason " + reason + "(" + this.mDisableReasonStrHashMap.get(Integer.valueOf(reason)) + ")");
                            }
                            scannedConfiguredNetworks.add(wc);
                        } else if (!wc.SSID.equals(ClientModeImpl.convertToQuotedSSID(sr.SSID))) {
                            continue;
                        } else if (ScanResultUtil.isScanResultForPskSaeTransitionNetwork(sr)) {
                            Log.i(TAG, "find a PSK-SAE AP connected with PSK, update to SAE, config:" + wc);
                            wc.allowedKeyManagement.clear(1);
                            wc.allowedKeyManagement.set(8);
                            wc.requirePMF = true;
                            sWifiConfigManager.addOrUpdateNetwork(wc, 1000);
                            scannedConfiguredNetworks.add(wc);
                            break;
                        } else if (ScanResultUtil.isScanResultForOweNetwork(sr)) {
                            Log.i(TAG, "find a OWE AP connected with NONE, update to OWE, config:" + wc);
                            wc.allowedKeyManagement.clear(0);
                            wc.allowedKeyManagement.set(9);
                            wc.requirePMF = true;
                            sWifiConfigManager.addOrUpdateNetwork(wc, 1000);
                            scannedConfiguredNetworks.add(wc);
                            break;
                        } else if (ScanResultUtil.isScanResultForOpenNetwork(sr)) {
                            Log.i(TAG, "find a OPEN AP connected with OWE, update to NONE, config:" + wc);
                            wc.allowedKeyManagement.clear(9);
                            wc.allowedKeyManagement.set(0);
                            wc.requirePMF = false;
                            sWifiConfigManager.addOrUpdateNetwork(wc, 1000);
                            scannedConfiguredNetworks.add(wc);
                            break;
                        }
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
        boolean needEnable;
        HashMap<String, Integer> hashMap;
        List<ScanResult> srList = this.mScanResultList;
        if (oppoAutoConnConf != null && srList != null) {
            if (srList.size() > 0) {
                String configKey = oppoAutoConnConf.mConfigKey;
                if (configKey == null) {
                    logi("configKey is null!!");
                    return false;
                }
                int bestLevel = getCurrentScanResultBestLevel(srList, configKey);
                int rssiLow = getRssiLow();
                int rssiBad = getRssiBad();
                int disabledReason = oppoAutoConnConf.mDisabledReason;
                int disabledLevel = oppoAutoConnConf.mDisabledLevel;
                int rssiGap = bestLevel - disabledLevel;
                int rssiGapAbsoluteValue = (int) MathUtils.abs((float) rssiGap);
                logd("rssiGap:" + rssiGap + ", rssiGapAbsoluteValue:" + rssiGapAbsoluteValue);
                long timeGap = oppoAutoConnConf.mDisabledUntilTime - System.currentTimeMillis();
                logd("disabled reason:" + disabledReason + "(" + this.mDisableReasonStrHashMap.get(Integer.valueOf(disabledReason)) + "),best level:" + bestLevel + ", last disabledLevel:" + disabledLevel);
                int rssiGood = getRssiGood();
                int rssiStep = getRssiStep();
                if (disabledReason == 0) {
                    needEnable = false;
                } else if (disabledReason != 10) {
                    needEnable = false;
                    if (disabledReason != 13) {
                        if (disabledReason != 15) {
                            if (disabledReason != 102) {
                                if (disabledReason != 200) {
                                    if (disabledReason != 2) {
                                        if (disabledReason != 3) {
                                            if (disabledReason != 4) {
                                            }
                                        } else if (rssiGap > rssiStep && disabledLevel < rssiGood) {
                                            return true;
                                        } else {
                                            if (timeGap <= 0) {
                                                return true;
                                            }
                                            logd("good rssi but not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                                            return false;
                                        }
                                    }
                                    if (disabledLevel >= rssiGood) {
                                        if (bestLevel >= rssiGood) {
                                            if (timeGap <= 0) {
                                                return true;
                                            }
                                            logd("good rssi but not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                                            return false;
                                        } else if (rssiGapAbsoluteValue < rssiStep) {
                                            if (timeGap <= 0) {
                                                return true;
                                            }
                                            logd("good(-) rssi but not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                                            return false;
                                        }
                                    } else if (disabledLevel >= rssiLow) {
                                        if (bestLevel >= rssiGood) {
                                            return true;
                                        }
                                        if (rssiGap >= rssiStep) {
                                            return true;
                                        }
                                        if (rssiGapAbsoluteValue < rssiStep) {
                                            if (timeGap <= 0) {
                                                return true;
                                            }
                                            logd("low rssi(+/-" + rssiStep + ") ,and not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                                            return false;
                                        }
                                    } else if (disabledLevel >= rssiBad) {
                                        if (bestLevel >= rssiLow) {
                                            return true;
                                        }
                                        if (rssiGap >= rssiStep) {
                                            return true;
                                        }
                                        if (rssiGapAbsoluteValue < rssiStep) {
                                            if (timeGap <= 0) {
                                                return true;
                                            }
                                            logd("bad rssi (+/-" + rssiStep + ") ,and not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                                            return false;
                                        }
                                    } else if (rssiGap > rssiStep) {
                                        return true;
                                    }
                                } else if ((bestLevel >= rssiGood && disabledLevel <= rssiLow) || timeGap <= 0) {
                                    return true;
                                } else {
                                    logd("not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                                    return false;
                                }
                            }
                        } else if ((bestLevel >= rssiGood && disabledLevel <= rssiLow) || timeGap <= 0 || (hashMap = this.mUnexpectDisconnectDisableCount) == null || hashMap.isEmpty()) {
                            return true;
                        } else {
                            logd("not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                            return false;
                        }
                    } else if ((rssiGap > rssiStep && disabledLevel < rssiGood) || timeGap <= 0) {
                        return true;
                    } else {
                        logd("good rssi but not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                        return false;
                    }
                } else if (!this.mWifiAssistantEnabled) {
                    return true;
                } else {
                    if ((bestLevel >= rssiGood && disabledLevel <= rssiLow) || timeGap <= 0) {
                        return true;
                    }
                    logd("no reach internet:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
                    return false;
                }
                return needEnable;
            }
        }
        logi("oppoAutoConnConf is null,or srList is null/empty!!");
        return false;
    }

    private List<WifiConfiguration> getCanEnabledConfiguredNetworks(List<WifiConfiguration> wcList) {
        String configuredConfigKey;
        OppoAutoConnectConfiguration tmpOppoAutoConnConf;
        if (wcList == null || wcList.size() <= 0) {
            logi("wcList is null or empty!!");
            return null;
        }
        List<WifiConfiguration> canEnabledConfiguredNetworksList = new ArrayList<>();
        for (WifiConfiguration wc : wcList) {
            if (!(wc == null || (configuredConfigKey = wc.configKey()) == null || (tmpOppoAutoConnConf = getAutoConnectConfiguration(configuredConfigKey)) == null || !checkCanEnableDisabledConfiguredNetwork(tmpOppoAutoConnConf))) {
                logd("add can enabled configured network:" + configuredConfigKey);
                canEnabledConfiguredNetworksList.add(wc);
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
        List<WifiConfiguration> normalDisabledNetworksList = new ArrayList<>();
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
        List<WifiConfiguration> normalDisabledNetworksList = new ArrayList<>();
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
        List<WifiConfiguration> networksList = new ArrayList<>();
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
        List<WifiConfiguration> networksList = new ArrayList<>();
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
            if (!sWifiStateMachine.isSupplicantAvailable()) {
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
            }
        }
    }

    public void addOrUpdateAutoConnectAp() {
        if (isSingtelArea()) {
            addOrUpdateSingtelAp();
            logd("add the Singtel ssid");
        }
        if (isSwisscomArea()) {
            addOrUpdateOperatorAutoConnectAp(SWISSCOM_WIFI_ADDED_PROPERTY, SWISSCOM_OPERATOR_AP_SSID);
            logd("add swisscom ssid");
        }
        if (isBouygueArea()) {
            addOrUpdateOperatorAutoConnectAp(BOUYGUE_WIFI_ADDED_PROPERTY, BOUYGUE_OPERATOR_AP_SSID);
            logd("add Bouygue ssid");
        }
        if (isTurkcellArea()) {
            addOrUpdateOperatorAutoConnectAp(TURKCELL_WIFI_ADDED_PROPERTY, TURKCELL_OPERATOR_AP_SSID);
            logd("add Turkcell ssid");
        }
        if (isThailandArea()) {
            addOrUpdateOperatorAutoConnectAp(THAILAND_WIFI_ADDED_PROPERTY, THAILAND_OPERATOR_AP_SSID);
            logd("add ThailandAis ssid");
        }
    }

    public void addOrUpdateOperatorAutoConnectAp(String addedProperty, String ssid) {
        String added = SystemProperties.get(addedProperty, "no");
        logd("Operator_wifi_added = " + added);
        if ("yes".equals(added)) {
            logd(ssid + "Operator wifi already added!");
            return;
        }
        String quoted_ssid_str = "\"" + ssid + "\"";
        WifiConfiguration savedWconf = null;
        String savedConfigKey = quoted_ssid_str + "-" + WifiConfiguration.KeyMgmt.strings[2];
        WifiConfigManager wifiConfigManager = sWifiConfigManager;
        if (wifiConfigManager != null) {
            savedWconf = wifiConfigManager.getConfiguredNetwork(savedConfigKey);
        }
        if (savedWconf != null) {
            logd("Operator wifi is already exists!");
            SystemProperties.set(addedProperty, "yes");
            return;
        }
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = quoted_ssid_str;
        wc.allowedKeyManagement.set(2);
        wc.allowedKeyManagement.set(3);
        wc.enterpriseConfig = new WifiEnterpriseConfig();
        if (TURKCELL_OPERATOR_AP_SSID.equals(ssid)) {
            wc.enterpriseConfig.setEapMethod(5);
        } else {
            wc.enterpriseConfig.setEapMethod(4);
        }
        if (sWifiConfigManager != null) {
            logd("addOrUpdateNetwork Operator wifi:" + quoted_ssid_str);
            if (sWifiConfigManager.addOrUpdateNetwork(wc, 1000).isSuccess()) {
                SystemProperties.set(addedProperty, "yes");
            }
        }
    }

    public void addOrUpdateSingtelAp() {
        if (!isSingtelArea()) {
            logd("not sintel areas or no valid ");
            return;
        }
        String added = SystemProperties.get(SINGTEL_WIFI_ADDED_PROPERTY, "no");
        logd("singtel_wifi_added = " + added);
        if ("yes".equals(added)) {
            logd("singtel wifi already added!");
            return;
        }
        WifiConfiguration savedWconf = null;
        String savedConfigKey = "\"Singtel WIFI\"-" + WifiConfiguration.KeyMgmt.strings[2];
        WifiConfigManager wifiConfigManager = sWifiConfigManager;
        if (wifiConfigManager != null) {
            savedWconf = wifiConfigManager.getConfiguredNetwork(savedConfigKey);
        }
        if (savedWconf != null) {
            logd("singtel wifi is already exists!");
            SystemProperties.set(SINGTEL_WIFI_ADDED_PROPERTY, "yes");
            return;
        }
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = SINGTEL_OPERATOR_AP_SSID_QUOTED;
        wc.allowedKeyManagement.set(2);
        wc.allowedKeyManagement.set(3);
        wc.enterpriseConfig = new WifiEnterpriseConfig();
        int simIndex = getValidSingtelOpratorSimCardNum();
        if (simIndex == 0) {
            wc.enterpriseConfig.setSimNum(0);
        }
        if (simIndex == 1 || simIndex == 2) {
            wc.enterpriseConfig.setSimNum(simIndex - 1);
        } else {
            wc.enterpriseConfig.setSimNum(1);
        }
        wc.enterpriseConfig.setEapMethod(4);
        if (sWifiConfigManager != null) {
            logd("addOrUpdateNetwork singetal wifi");
            if (sWifiConfigManager.addOrUpdateNetwork(wc, 1000).isSuccess()) {
                SystemProperties.set(SINGTEL_WIFI_ADDED_PROPERTY, "yes");
            }
        }
    }

    private boolean isSingtelArea() {
        if (!SystemProperties.get("persist.sys.oppo.region", "CN").equals("SG") || !SystemProperties.get("ro.oppo.operator", "NULL").equals("SINGTEL")) {
            return false;
        }
        return true;
    }

    private boolean isSwisscomArea() {
        if (SystemProperties.get("ro.oppo.operator", "NULL").equals("SWISSCOM")) {
            return true;
        }
        return false;
    }

    private boolean isBouygueArea() {
        if (SystemProperties.get("ro.oppo.operator", "NULL").equals("BOUYGUE")) {
            return true;
        }
        return false;
    }

    private boolean isTurkcellArea() {
        if (SystemProperties.get("ro.oppo.euex.country", "NULL").equals("TR")) {
            return true;
        }
        return false;
    }

    private boolean isThailandArea() {
        if (SystemProperties.get("ro.oppo.regionmark", "NULL").equals("TH")) {
            return true;
        }
        return false;
    }

    private boolean isInsertVailedSimCard() {
        boolean isInsertSimCard = false;
        if (isSingtelArea() && hasValidSingtelOperatorSimCard()) {
            logd("hasValidSingtelOperatorSimCard");
            isInsertSimCard = true;
        }
        if (isSwisscomArea() && hasValidSwisscomOperatorSimCard()) {
            logd("hasValidSwisscomOperatorSimCard");
            isInsertSimCard = true;
        }
        if (isBouygueArea()) {
            logd("add Bouygue ssid");
        }
        if (isTurkcellArea() && hasValidTurkcellOperatorSimCard()) {
            logd("hasValidTurkcellOperatorSimCard");
            isInsertSimCard = true;
        }
        if (!isThailandArea() || !hasValidThailandOperatorSimCard()) {
            return isInsertSimCard;
        }
        logd("hasValidThailandOperatorSimCard");
        return true;
    }

    private boolean isInOperatorArea() {
        if (isSingtelArea() || isSwisscomArea() || isBouygueArea() || isTurkcellArea() || isThailandArea()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasValidSwisscomOperatorSimCard() {
        if (!isSwisscomArea()) {
            logd("not in Swisscom Operator area!");
            return false;
        }
        int validSimCardNum = getValidSwisscomOpratorSimCardNum();
        if (validSimCardNum <= 0) {
            logd("has no valid Operator simcard!!");
        }
        if (validSimCardNum > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasValidTurkcellOperatorSimCard() {
        if (!isTurkcellArea()) {
            logd("not in Turkcell Operator area!");
            return false;
        }
        int validSimCardNum = getValidTurkcellOpratorSimCardNum();
        if (validSimCardNum <= 0) {
            logd("has no valid Operator simcard!!");
        }
        if (validSimCardNum > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasValidThailandOperatorSimCard() {
        if (!isThailandArea()) {
            logd("not in Thailand Operator area!");
            return false;
        }
        int validSimCardNum = getValidThailandOpratorSimCardNum();
        if (validSimCardNum <= 0) {
            logd("has no valid Operator simcard!!");
        }
        if (validSimCardNum > 0) {
            return true;
        }
        return false;
    }

    private int getValidSwisscomOpratorSimCardNum() {
        int simCardIndex = 0;
        ColorOSTelephonyManager cotm = ColorOSTelephonyManager.getDefault(sContext);
        if (cotm == null) {
            logd("1741 Swisscom simCardIndex = 0");
            return 0;
        } else if (cotm.hasIccCardGemini(0) || cotm.hasIccCardGemini(1)) {
            String operator1 = cotm.getSimOperatorGemini(0);
            String operator2 = cotm.getSimOperatorGemini(1);
            if (operator1 != null && operator1.equals("22801")) {
                simCardIndex = 0 + 1;
            }
            if (operator2 != null && operator2.equals("22801")) {
                simCardIndex += 2;
            }
            logd("Swisscom simCardIndex = " + simCardIndex);
            return simCardIndex;
        } else {
            logd("1745 Swisscom simCardIndex = 0");
            return 0;
        }
    }

    private int getValidThailandOpratorSimCardNum() {
        int simCardIndex = 0;
        ColorOSTelephonyManager cotm = ColorOSTelephonyManager.getDefault(sContext);
        if (cotm == null) {
            logd("1741 Thailand simCardIndex = 0");
            return 0;
        } else if (cotm.hasIccCardGemini(0) || cotm.hasIccCardGemini(1)) {
            String operator1 = cotm.getSimOperatorGemini(0);
            String operator2 = cotm.getSimOperatorGemini(1);
            if (operator1 != null && (operator1.equals("52001") || operator1.equals("52003"))) {
                simCardIndex = 0 + 1;
            }
            if (operator2 != null && (operator2.equals("52001") || operator2.equals("52003"))) {
                simCardIndex += 2;
            }
            logd("Thailand simCardIndex = " + simCardIndex);
            return simCardIndex;
        } else {
            logd("1745 Thailand simCardIndex = 0");
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasValidSingtelOperatorSimCard() {
        if (!SystemProperties.get("persist.sys.oppo.region", "CN").equals("SG") || !SystemProperties.get("ro.oppo.operator", "NULL").equals("SINGTEL")) {
            logd("not in singtel area!");
            return false;
        }
        int validSingtelSimCardNum = getValidSingtelOpratorSimCardNum();
        if (validSingtelSimCardNum <= 0) {
            logd("has no valid singtel simcard!!");
        }
        if (validSingtelSimCardNum > 0) {
            return true;
        }
        return false;
    }

    private int getValidTurkcellOpratorSimCardNum() {
        int simCardIndex = 0;
        ColorOSTelephonyManager cotm = ColorOSTelephonyManager.getDefault(sContext);
        if (cotm == null) {
            logd("1741 Turkcell simCardIndex = 0");
            return 0;
        } else if (cotm.hasIccCardGemini(0) || cotm.hasIccCardGemini(1)) {
            String operator1 = cotm.getSimOperatorGemini(0);
            String operator2 = cotm.getSimOperatorGemini(1);
            if (operator1 != null && operator1.equals("28601")) {
                simCardIndex = 0 + 1;
            }
            if (operator2 != null && operator2.equals("28601")) {
                simCardIndex += 2;
            }
            logd("Turkcell simCardIndex = " + simCardIndex);
            return simCardIndex;
        } else {
            logd("1745 Turkcell simCardIndex = 0");
            return 0;
        }
    }

    public int getValidOpratorSimCardNum() {
        int simCardIndex = 0;
        ColorOSTelephonyManager cotm = ColorOSTelephonyManager.getDefault(sContext);
        if (cotm == null) {
            return 0;
        }
        if (!cotm.hasIccCardGemini(0) && !cotm.hasIccCardGemini(1)) {
            return 0;
        }
        if (cotm.hasIccCardGemini(0) && cotm.getSimOperatorGemini(0) != null) {
            simCardIndex = 0 + 1;
        }
        if (cotm.hasIccCardGemini(1) && cotm.getSimOperatorGemini(1) != null) {
            simCardIndex += 2;
        }
        logd("simCardIndex = " + simCardIndex);
        return simCardIndex;
    }

    private int getValidSingtelOpratorSimCardNum() {
        int simCardIndex = 0;
        ColorOSTelephonyManager cotm = ColorOSTelephonyManager.getDefault(sContext);
        if (cotm == null) {
            return 0;
        }
        if (!cotm.hasIccCardGemini(0) && !cotm.hasIccCardGemini(1)) {
            return 0;
        }
        String operator1 = cotm.getSimOperatorGemini(0);
        String operator2 = cotm.getSimOperatorGemini(1);
        if (operator1 != null && (operator1.equals("52501") || operator1.equals("52502") || operator1.equals("52507"))) {
            simCardIndex = 0 + 1;
        }
        if (operator2 != null && (operator2.equals("52501") || operator2.equals("52502") || operator2.equals("52507"))) {
            simCardIndex += 2;
        }
        logd("singtel simCardIndex = " + simCardIndex);
        return simCardIndex;
    }

    public boolean getSingtelAutoConnectEnabled() {
        int enabled;
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            enabled = 0;
        } else {
            enabled = wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ENABLE_SINGTEL_AUTOCONNECT", 1).intValue();
        }
        logd("getSingtelAutoConnectEnabled  = " + enabled);
        if (enabled > 0) {
            return true;
        }
        return false;
    }

    public boolean getOperatorAutoConnectEnabled() {
        int enabled;
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            enabled = 0;
        } else {
            enabled = wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ENABLE_OPERATOR_AUTOCONNECT", 1).intValue();
        }
        logd("getOperatorAutoConnectEnabled  = " + enabled);
        if (enabled > 0) {
            return true;
        }
        return false;
    }

    private String[] getOperatorApSsids() {
        String value = sWifiRomUpdateHelper.getValue("OPPO_AUTO_CONNECT_OPERATOR_AP", DEFAULT_OPERATOR_AP_SSID);
        if (value == null) {
            logd("get from rom update null\n");
            value = DEFAULT_OPERATOR_AP_SSID;
        }
        return value.split(",");
    }

    private boolean isOperatorAp(int netId) {
        WifiConfiguration network = getWifiConfiguration(netId);
        if (network != null) {
            String name = network.getPrintableSsid();
            for (int i = 0; i < this.mOperaterSsids.length; i++) {
                String quoted_ssid_str = "\"" + this.mOperaterSsids[i] + "\"";
                if (this.mOperaterSsids[i].equals(name) || quoted_ssid_str.equals(name)) {
                    return true;
                }
            }
        }
        logd("netid " + netId + " is not OperatorAp");
        return false;
    }

    private boolean isSingtelOperatorAp(int netId) {
        WifiConfiguration network = getWifiConfiguration(netId);
        if (network != null) {
            String name = network.getPrintableSsid();
            if (SINGTEL_OPERATOR_AP_SSID.equals(name) || SINGTEL_OPERATOR_AP_SSID_QUOTED.equals(name) || SINGTEL_OPERATOR_AP2_SSID.equals(name) || SINGTEL_OPERATOR_AP2_SSID_QUOTED.equals(name)) {
                return true;
            }
        }
        logd("netid " + netId + " is not singtelOperatorAp");
        return false;
    }

    private String addQuote(String str) {
        if (str != null && str.startsWith("\"") && str.endsWith("\"")) {
            return str;
        }
        return "\"" + str + "\"";
    }

    public String delQuote(String str) {
        if (str == null || !str.startsWith("\"") || !str.endsWith("\"")) {
            return str;
        }
        return str.substring(1, str.length() - 1);
    }

    private String makeNAI(String imsi, String eapMethod) {
        if (imsi == null) {
            return "error";
        }
        StringBuffer NAI = new StringBuffer(40);
        if (eapMethod.equals("SIM")) {
            NAI.append("1");
        } else if (eapMethod.equals("AKA")) {
            NAI.append("0");
        }
        NAI.append(imsi);
        NAI.append("@wlan.mnc");
        NAI.append("0");
        NAI.append(imsi.substring(3, 5));
        NAI.append(".mcc");
        NAI.append(imsi.substring(0, 3));
        NAI.append(".3gppnetwork.org");
        logd(NAI.toString());
        return addQuote(NAI.toString());
    }

    private WifiConfiguration pickSingtelWifiConfiguration(List<WifiConfiguration> wcList) {
        logd("pickSingtelWifiConfiguration");
        if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return null;
        } else if (!hasValidSingtelOperatorSimCard()) {
            return null;
        } else {
            List<WifiConfiguration> candicatList = new ArrayList<>();
            for (WifiConfiguration wc : wcList) {
                if (wc != null && isSingtelOperatorAp(wc.networkId)) {
                    logd("add singtel ap:" + wc.SSID);
                    candicatList.add(wc);
                }
            }
            if (candicatList.size() <= 0) {
                logd("candicatList is null or emptey!!");
                return null;
            }
            WifiConfiguration candicator = null;
            if (candicatList.size() == 1) {
                candicator = candicatList.get(0);
            } else {
                for (WifiConfiguration wcc : candicatList) {
                    if (candicator == null) {
                        candicator = wcc;
                    } else if (wcc.lastConnected > candicator.lastConnected) {
                        candicator = wcc;
                    }
                }
            }
            if (candicator != null) {
                return candicator;
            }
            logd("candicator is null!!");
            return null;
        }
    }

    private WifiConfiguration pickOperatorWifiConfiguration(List<WifiConfiguration> wcList) {
        logd("pickOperatorWifiConfiguration");
        if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return null;
        } else if (!isInsertVailedSimCard()) {
            return null;
        } else {
            List<WifiConfiguration> candicatList = new ArrayList<>();
            for (WifiConfiguration wc : wcList) {
                if (wc != null && isOperatorAp(wc.networkId)) {
                    logd("add Operator ap:" + wc.SSID);
                    candicatList.add(wc);
                }
            }
            if (candicatList.size() <= 0) {
                logd("candicatList is null or emptey!!");
                return null;
            }
            WifiConfiguration candicator = null;
            if (candicatList.size() == 1) {
                candicator = candicatList.get(0);
            } else {
                for (WifiConfiguration wcc : candicatList) {
                    if (candicator == null) {
                        candicator = wcc;
                    } else if (wcc.lastConnected > candicator.lastConnected) {
                        candicator = wcc;
                    }
                }
            }
            if (candicator != null) {
                return candicator;
            }
            logd("candicator is null!!");
            return null;
        }
    }

    public void switchConfigurationSimSlot(int netId) {
        int targetSimCard = -1;
        logd("switchConfigurationSimSlot for netId = " + netId);
        WifiConfiguration candicator = getWifiConfiguration(netId);
        if (candicator != null) {
            if (!TelephonyUtil.isSimConfig(candicator)) {
                logd("not sim config!!");
            } else if (candicator.enterpriseConfig == null) {
                logd("enterpriseConfig is null!");
            } else {
                if (isManuConnect()) {
                    if (!isSingtelOperatorAp(netId) || !hasValidSingtelOperatorSimCard()) {
                        logd("manu connect , no need switch!");
                        return;
                    }
                    logd("manu connect, try switch simslot!");
                }
                int simCardIndex = getValidOpratorSimCardNum();
                if (1 == simCardIndex || 2 == simCardIndex) {
                    targetSimCard = simCardIndex - 1;
                } else if (3 == simCardIndex) {
                    targetSimCard = 1;
                    int simSlot = candicator.enterpriseConfig.getSimNum();
                    if (simSlot >= 0) {
                        targetSimCard = (simSlot + 1) % 2;
                    }
                } else {
                    logd("no need set or switch!!");
                }
                logd("targetSimCard = " + targetSimCard);
                if (targetSimCard != -1) {
                    candicator.enterpriseConfig.setSimNum(targetSimCard);
                    WifiConfigManager wifiConfigManager = sWifiConfigManager;
                    if (wifiConfigManager == null || wifiConfigManager.addOrUpdateNetwork(candicator, 1000).isSuccess()) {
                        WifiConfiguration targetWconf = null;
                        ClientModeImpl clientModeImpl = sWifiStateMachine;
                        if (clientModeImpl != null) {
                            targetWconf = clientModeImpl.getmTargetWifiConfiguration();
                        }
                        if (targetWconf == null || targetWconf.enterpriseConfig == null || (targetWconf.enterpriseConfig.getSimNum() != candicator.enterpriseConfig.getSimNum() && targetWconf.networkId == candicator.networkId)) {
                            logd("reconnect  ap with simSlot " + targetSimCard);
                            reconnect(candicator.networkId);
                            return;
                        }
                        return;
                    }
                    logd("addOrUpdateNetwork to switch simSlot failed");
                }
            }
        }
    }

    private WifiConfiguration tryPickCandinate(List<WifiConfiguration> wcList) {
        int rssi;
        if (wcList == null || wcList.size() <= 0) {
            logi("wcList is null or empty!!");
            return null;
        }
        List<ScanResult> srList = this.mScanResultList;
        if (srList == null || srList.size() <= 0) {
            logi("srList is null or empty!!");
            return null;
        }
        int bestLevel = -127;
        WifiConfiguration selectedConf = null;
        for (WifiConfiguration wc : wcList) {
            String configKey = wc.configKey();
            if (configKey != null && ((!isInOperatorArea() || !isOperatorAp(wc.networkId)) && (rssi = getCurrentScanResultBestLevel(srList, configKey)) >= bestLevel)) {
                bestLevel = rssi;
                selectedConf = wc;
            }
        }
        return selectedConf;
    }

    private WifiConfiguration pickValidWifiConfiguration() {
        WifiConfiguration selectedConf;
        logd("pickValidWifiConfiguration");
        if (checkInterfaceNull()) {
            return null;
        }
        WifiConfiguration selectedConf2 = null;
        List<WifiConfiguration> savedNetworksList = getConfiguredNetworks();
        List<WifiConfiguration> scannedConfiguredNetworksList = getAllScannedAndConfiguredNetworks();
        List<WifiConfiguration> savedAndEnabledNetworkList = new ArrayList<>();
        List<WifiConfiguration> savedAndEnaledAndScannedNetworkList = new ArrayList<>();
        new ArrayList();
        new ArrayList();
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
            if (savedAndEnabledNetworkList.size() <= 0) {
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
            List<WifiConfiguration> saveAndEnabledAndScannedAndCanInternetNetworkList = getAllScannedAndEnabledAndInternetNetworks(savedAndEnaledAndScannedNetworkList);
            if (!getSingtelAutoConnectEnabled() || (selectedConf2 = pickSingtelWifiConfiguration(savedAndEnaledAndScannedNetworkList)) == null) {
                if (!this.mWifiAssistantEnabled) {
                    logd("wlan assitant is disabled!");
                    List<WifiConfiguration> normalNetworkList = new ArrayList<>();
                    if (saveAndEnabledAndScannedAndCanInternetNetworkList != null && saveAndEnabledAndScannedAndCanInternetNetworkList.size() > 0 && (selectedConf2 = tryPickCandinate((normalNetworkList = getNormalNetworks(saveAndEnabledAndScannedAndCanInternetNetworkList)))) == null) {
                        selectedConf2 = tryPickCandinate(saveAndEnabledAndScannedAndCanInternetNetworkList);
                    }
                    if (selectedConf2 == null) {
                        logd("pick no networks in can internet network list,try to pick in all enabled network list");
                        if (normalNetworkList != null) {
                            normalNetworkList.clear();
                        }
                        WifiConfiguration selectedConf3 = tryPickCandinate(getNormalNetworks(savedAndEnaledAndScannedNetworkList));
                        if (selectedConf3 == null) {
                            selectedConf = tryPickCandinate(savedAndEnaledAndScannedNetworkList);
                        } else {
                            selectedConf = selectedConf3;
                        }
                    } else {
                        selectedConf = selectedConf2;
                    }
                    if (selectedConf == null && getOperatorAutoConnectEnabled() && (selectedConf = pickOperatorWifiConfiguration(savedAndEnaledAndScannedNetworkList)) != null) {
                        logd("pick a Operator WifiConfiguration:" + selectedConf.configKey());
                        return selectedConf;
                    }
                } else if (saveAndEnabledAndScannedAndCanInternetNetworkList == null || saveAndEnabledAndScannedAndCanInternetNetworkList.size() <= 0) {
                    logd("wlan assistant enabled but has no can internet networks!!");
                    this.mWlanAssistConnect = false;
                    selectedConf = tryPickCandinate(getNetworksExcept(savedAndEnaledAndScannedNetworkList, 10));
                } else {
                    logd("wlan assistant enabled and  find enabled and can internet networks,do nothing!!");
                    this.mWlanAssistConnect = true;
                    HandleForPnoBeforeTriggerConnect(saveAndEnabledAndScannedAndCanInternetNetworkList);
                    return null;
                }
                if (selectedConf != null) {
                    logd("selected network id:" + selectedConf.networkId + " configKey:" + selectedConf.configKey());
                }
                return selectedConf;
            }
            logd("pick a Singtel WifiConfiguration:" + selectedConf2.configKey());
            return selectedConf2;
        }
    }

    private void startAutoConnect() {
        if (!checkInterfaceNull()) {
            clearTriggerAutoConnectEvt();
            WifiConfiguration selectedConf = pickValidWifiConfiguration();
            if (selectedConf != null) {
                boolean needReconnect = false;
                if (this.mWifiAssistantEnabled) {
                    if (getIsOppoManuConnect()) {
                        logd("manu connection, do nothing!!");
                        return;
                    } else if (wifiIsConnectedOrConnecting()) {
                        logd("wifi assitant enabled and already in connecting/connected,do nothing!!");
                        return;
                    } else {
                        needReconnect = true;
                    }
                } else if (getIsOppoManuConnect()) {
                    logd("manu connection, do nothing!!");
                    return;
                } else if (wifiIsConnectedOrConnecting()) {
                    boolean canInternetCur = checkCanInternet(this.mConnectingNetId);
                    boolean canInternetSelected = checkCanInternet(selectedConf);
                    if (!canInternetCur && canInternetSelected) {
                        logd("select a can inernet AP instead of non-internet connecting!!!");
                        needReconnect = true;
                    }
                    if (getSingtelAutoConnectEnabled() && hasValidSingtelOperatorSimCard() && isSingtelOperatorAp(selectedConf.networkId) && !isSingtelOperatorAp(this.mConnectingNetId)) {
                        logd("trigger auto connect since we find a singtel AP!!");
                        needReconnect = true;
                    }
                    if (getOperatorAutoConnectEnabled() && isInsertVailedSimCard() && isOperatorAp(selectedConf.networkId) && !isOperatorAp(this.mConnectingNetId)) {
                        logd("trigger auto connect since we find a Operator AP!!");
                        needReconnect = true;
                    }
                } else {
                    needReconnect = true;
                    if (getOperatorAutoConnectEnabled() && isInOperatorArea() && isOperatorAp(selectedConf.networkId)) {
                        if (isInsertVailedSimCard()) {
                            logd("trigger auto connect since we find a Operator AP!!");
                        } else {
                            needReconnect = false;
                        }
                    }
                }
                if (needReconnect) {
                    logd("select " + selectedConf.configKey() + " to reconnect!");
                    reconnect(selectedConf.networkId);
                    return;
                }
                return;
            }
            logd("pick no networks!!");
            if (this.mWifiAssistantEnabled) {
                logd("notify wlan assist to connect if possible!");
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = sWifiNetworkStateTraker;
                if (oppoWifiAssistantStateTraker != null && this.mWifiAssistantRomupdate) {
                    oppoWifiAssistantStateTraker.detectScanResult(System.currentTimeMillis());
                }
            }
        }
    }

    public void HandleForPnoBeforeTriggerConnect(List<WifiConfiguration> wcList) {
        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker;
        String configKey;
        boolean isPnoStarted = false;
        boolean allConnExp = true;
        if (wcList == null || wcList.size() <= 0) {
            logd("wcList is null or empty!!");
            return;
        }
        ClientModeImpl clientModeImpl = sWifiStateMachine;
        if (clientModeImpl != null) {
            isPnoStarted = clientModeImpl.isPnoStarted();
        }
        if (isPnoStarted && (oppoWifiAssistantStateTraker = sWifiNetworkStateTraker) != null) {
            List<OppoWifiAssistantRecord> wnrList = oppoWifiAssistantStateTraker.getWifiNetworkRecords();
            if (wnrList == null || wnrList.size() <= 0) {
                logd("wnrList is null or empty!!");
                return;
            }
            for (WifiConfiguration wc : wcList) {
                if (!(wc == null || (configKey = wc.configKey()) == null)) {
                    Iterator<OppoWifiAssistantRecord> it = wnrList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        OppoWifiAssistantRecord owar = it.next();
                        if (owar != null && configKey.equals(owar.mConfigkey) && !owar.mConnExp) {
                            allConnExp = false;
                            break;
                        }
                    }
                    if (!allConnExp) {
                        break;
                    }
                }
            }
            logd("allConnExp = " + allConnExp);
            if (allConnExp) {
                for (OppoWifiAssistantRecord owar2 : wnrList) {
                    if (owar2 != null) {
                        sWifiNetworkStateTraker.resetConnExp(owar2.mConfigkey);
                    }
                }
            }
        }
    }

    private boolean checkInterfaceNull() {
        if (sContext == null) {
            loge("sContext is null!!");
            return true;
        } else if (sWifiConfigManager == null) {
            loge("sWifiConfigManager is null!!");
            return true;
        } else if (sWifiStateMachine == null) {
            loge("sWifiStateMachine is null!!");
            return true;
        } else if (sWifiNative != null) {
            return false;
        } else {
            loge("sWifiNative is null!!");
            return true;
        }
    }

    private void enableNetwork(int netId) {
        if (!checkInterfaceNull()) {
            if (!sWifiStateMachine.isSupplicantAvailable()) {
                logd("wifi is in disable or disable pending state,cancel enableNetwork!!");
            } else if (!sWifiConfigManager.isCustomizeAutoConnectDisabled()) {
                sWifiConfigManager.enableNetworkEx(netId, false, 1000, true);
            }
        }
    }

    private void enableNetworkWithoutBroadcast(int netId) {
        if (!checkInterfaceNull()) {
            if (!sWifiStateMachine.isSupplicantAvailable()) {
                logd("wifi is in disable or disable pending state,cancel enableNetworkWithoutBroadcast!!");
            } else if (!sWifiConfigManager.isCustomizeAutoConnectDisabled()) {
                sWifiConfigManager.enableNetworkEx(netId, false, 1000, false);
            }
        }
    }

    private void disableNetwork(int netId, int reason) {
        if (!checkInterfaceNull()) {
            if (!sWifiStateMachine.isSupplicantAvailable()) {
                logd("wifi is in disable or disable pending state,cancel disableNetwork!!");
            } else {
                sWifiConfigManager.disableNetworkEx(netId, reason, true);
            }
        }
    }

    private void disableNetworkWithoutBoradcast(int netId, int reason) {
        if (!checkInterfaceNull()) {
            if (!sWifiStateMachine.isSupplicantAvailable()) {
                logd("wifi is in disable or disable pending state,cancel disableNetworkWithoutBoradcast!!");
            } else {
                sWifiConfigManager.disableNetworkEx(netId, reason, false);
            }
        }
    }

    private List<WifiConfiguration> getConfiguredNetworks() {
        if (checkInterfaceNull()) {
            return null;
        }
        return sWifiConfigManager.getSavedNetworksAll();
    }

    private WifiConfiguration getWifiConfiguration(int netId) {
        if (checkInterfaceNull()) {
            return null;
        }
        return sWifiConfigManager.getWifiConfigurationForAll(netId);
    }

    private List<ScanResult> getScanResultList() {
        if (checkInterfaceNull()) {
            return null;
        }
        return mScanRequestProxy.syncGetScanResultsList();
    }

    private int getWifiState() {
        if (checkInterfaceNull()) {
            return 1;
        }
        return sWifiStateMachine.syncGetWifiState();
    }

    private int getOperationMode() {
        if (checkInterfaceNull()) {
            return 3;
        }
        return sWifiStateMachine.getOperationalModeForTest();
    }

    private boolean wifiIsConnectedOrConnecting() {
        this.mNetworkInfo = sWifiStateMachine.getNetworkInfo();
        NetworkInfo networkInfo = this.mNetworkInfo;
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isConnectedOrConnecting();
    }

    private void manuReassociate() {
        if (!checkInterfaceNull()) {
            this.mLastManuReassociateToken++;
            sendMessage(16, this.mLastManuReassociateToken, 0, null);
        }
    }

    private void manuReassociateDelayed() {
        if (!checkInterfaceNull()) {
            this.mLastManuReassociateToken++;
            sendMessageDelayed(16, this.mLastManuReassociateToken, 0, null, OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDelayedManuReassociate() {
        logd("handleDelayedManuReassociate");
        if (!checkInterfaceNull()) {
            if (!sWifiStateMachine.isSupplicantAvailable()) {
                logd("wifi is in disable or disable pending state,cancel ManuReassociate!!");
                return;
            }
            long timeDiff = System.currentTimeMillis() - this.mLastManuReassociateTime;
            logd("manu reassociate timeDiff:" + timeDiff);
            if (timeDiff > 1000) {
                sWifiNative.manuReassociate(this.mInterfaceName);
                this.mLastManuReassociateTime = System.currentTimeMillis();
                return;
            }
            logd("manu reassociate too frequence,ignore!!");
        }
    }

    private void reconnect(int networkId) {
        if (!checkInterfaceNull()) {
            if (sWifiStateMachine.isDupDhcp()) {
                logd("[bug#1131400] dupDhcp, wait DHCP retry.");
                return;
            }
            WifiConfiguration conf = getWifiConfiguration(networkId);
            if (conf == null) {
                logd("conf is null!!");
            } else if (1 == conf.status) {
                logd("networkid :" + networkId + " is disabled yet,cancel reconnect!!");
            } else if (!sWifiStateMachine.isSupplicantAvailable()) {
                logd("wifi is in disable or disable pending state,cancel reconnect!!");
            } else if (sWifiStateMachine.isNetworkAutoConnectingOrConnected(networkId)) {
                logd("networkid: " + networkId + " is connecting or connected, do nothing!");
            } else {
                sWifiStateMachine.setTargetNetworkId(networkId);
                sWifiStateMachine.clearTargetBssid(TAG);
                sWifiStateMachine.prepareForForcedConnection(networkId);
                sWifiStateMachine.startConnectToNetwork(networkId, 1000, "any");
            }
        }
    }

    private boolean getIsOppoManuConnecting() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnecting();
        }
        return false;
    }

    private boolean getIsOppoManuConnected() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnected();
        }
        return false;
    }

    private boolean getIsOppoManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnect();
        }
        return false;
    }

    private int getOppoManuConnectNetworkId() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().getNetId();
        }
        return -1;
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
        if (!wc.allowedKeyManagement.get(1) && !wc.allowedKeyManagement.get(4)) {
            return false;
        }
        return true;
    }

    private boolean isWpaEapAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(2)) {
            return true;
        }
        return false;
    }

    private boolean isWapiPskAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(13)) {
            return true;
        }
        return false;
    }

    private boolean isWapiCertAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(14)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getFormatDateTime(long timeMillis) {
        if (sSimpleDateFormat == null) {
            return Long.toString(timeMillis);
        }
        return sSimpleDateFormat.format(new Date(timeMillis));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDebugDataShow() {
        logd("handleDebugDataShow");
        if (this.mOppoAutoConnectConfigurationHashMap == null) {
            loge("mOppoAutoConnectConfigurationHashMap is null!!");
            return;
        }
        HashMap<String, OppoAutoConnectConfiguration> tmpHashMap = this.mOppoAutoConnectConfigurationHashMap;
        logd("mOppoAutoConnectConfigurationHashMap in details!");
        for (String key : tmpHashMap.keySet()) {
            OppoAutoConnectConfiguration oppoAutoConnConf = tmpHashMap.get(key);
            if (oppoAutoConnConf != null) {
                logd(oppoAutoConnConf.toString());
            }
        }
        logd("mLastConnectedConfiguration in details!");
        OppoAutoConnectConfiguration oppoAutoConnectConfiguration = this.mLastConnectedConfiguration;
        if (oppoAutoConnectConfiguration != null) {
            logd(oppoAutoConnectConfiguration.toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDebugDataShowEvt() {
        sendMessage(4);
    }

    public void setInterfaceName(String interfaceName) {
        this.mInterfaceName = interfaceName;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, "debug:" + str);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        this.mEvtStrHashMap.put(1, "EVT_MANU_CONNECT");
        this.mEvtStrHashMap.put(4096, "EVT_THIRD_APK_CONNECT");
        this.mEvtStrHashMap.put(2, "EVT_SCAN_RESULT");
        this.mEvtStrHashMap.put(4, "EVT_DEBUG_DATA_SHOW");
        this.mEvtStrHashMap.put(8, "EVT_NETWORK_DISABLED");
        this.mEvtStrHashMap.put(32, "EVT_NETWORK_DELETED");
        this.mEvtStrHashMap.put(64, "EVT_WIFI_CONNECT_STATE_CHANGED");
        this.mEvtStrHashMap.put(128, "EVT_UPDATE_NETWORK_DISABLED_COUNT");
        this.mEvtStrHashMap.put(256, "EVT_SCREEN_ON");
        this.mEvtStrHashMap.put(512, "EVT_ENABLE_ALL_NETWORKS");
        this.mEvtStrHashMap.put(1024, "EVT_WIFI_STATE_ON");
        this.mEvtStrHashMap.put(2048, "EVT_NETWORK_RESAVED");
        this.mEvtStrHashMap.put(16, "EVT_DELAYED_MANU_REASSOCIATE");
        this.mDisableReasonStrHashMap.put(0, "DISABLED_REASON_INVALID");
        this.mDisableReasonStrHashMap.put(102, "DISABLED_REASON_UNKNWON");
        this.mDisableReasonStrHashMap.put(5, "DISABLED_REASON_DNS_FAILURE");
        this.mDisableReasonStrHashMap.put(4, "DISABLED_REASON_DHCP_FAILURE");
        this.mDisableReasonStrHashMap.put(3, "DISABLED_REASON_AUTH_FAILURE");
        this.mDisableReasonStrHashMap.put(2, "DISABLED_REASON_ASSOCIATION_REJECT");
        this.mDisableReasonStrHashMap.put(11, "DISABLED_REASON_BY_WIFI_MANAGER");
        this.mDisableReasonStrHashMap.put(13, "DISABLED_REASON_WRONG_KEY");
        this.mDisableReasonStrHashMap.put(15, "DISABLED_REASON_UNEXPECT_DISCONNECT");
        this.mDisableReasonStrHashMap.put(Integer.valueOf((int) ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS), "DISABLED_REASON_CONTINUOUS_DEBOUNCE_DISCONNECT");
        this.mDisableReasonStrHashMap.put(10, "DISABLED_REASON_NO_INTERNET");
        this.mDisableReasonStrHashMap.put(11, "DISABLED_REASON_BY_WIFI_MANAGER");
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

    private void sendMessageDelayed(int message, int arg1, int arg2, Object obj, int delay) {
        OppoAutoConnectManagerHandler oppoAutoConnectManagerHandler = this.mHandler;
        oppoAutoConnectManagerHandler.sendMessageDelayed(oppoAutoConnectManagerHandler.obtainMessage(message, arg1, arg2, obj), (long) delay);
    }

    private void listenForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction(ACTION_DEBUG_DATA_SHOW);
        intentFilter.addAction(ACTION_DEBUG_ENABLE);
        intentFilter.addAction(ACTION_DEBUG_DISABLE);
        intentFilter.addAction(ACTION_SCREEN_ON);
        intentFilter.addAction(ACTION_SCREEN_OFF);
        sContext.registerReceiver(this.mBroadcastReciever, intentFilter, null, this.mHandler);
        sContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.wifi.OppoAutoConnectManager.AnonymousClass2 */

            public void onChange(boolean selfChange) {
                OppoAutoConnectManager oppoAutoConnectManager = OppoAutoConnectManager.this;
                boolean z = true;
                if (Settings.Global.getInt(OppoAutoConnectManager.sContext.getContentResolver(), OppoAutoConnectManager.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                    z = false;
                }
                oppoAutoConnectManager.mWifiAssistantEnabled = z;
                Log.d(OppoAutoConnectManager.TAG, "onChange mWifiAssistantEnabled= " + OppoAutoConnectManager.this.mWifiAssistantEnabled);
            }
        });
        sContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WIFI_ASSISTANT_ROMUPDATE), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.wifi.OppoAutoConnectManager.AnonymousClass3 */

            public void onChange(boolean selfChange) {
                OppoAutoConnectManager oppoAutoConnectManager = OppoAutoConnectManager.this;
                boolean z = true;
                if (Settings.Global.getInt(OppoAutoConnectManager.sContext.getContentResolver(), OppoAutoConnectManager.WIFI_ASSISTANT_ROMUPDATE, 1) != 1) {
                    z = false;
                }
                oppoAutoConnectManager.mWifiAssistantRomupdate = z;
                Log.d(OppoAutoConnectManager.TAG, "onChange mwar= " + OppoAutoConnectManager.this.mWifiAssistantRomupdate);
            }
        });
        sContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoAutoConnectManager.AnonymousClass4 */

            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("scan_enabled", 1) == 1) {
                    if (OppoAutoConnectManager.DEBUG) {
                        Log.d(OppoAutoConnectManager.TAG, "SCAN is DISABLED, clear ScanResultList!");
                    }
                    OppoAutoConnectManager.this.mScanResultList = new ArrayList();
                }
            }
        }, new IntentFilter("wifi_scan_available"));
    }

    /* access modifiers changed from: private */
    public final class OppoAutoConnectManagerHandler extends Handler {
        public OppoAutoConnectManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                OppoAutoConnectManager.this.loge("msg is null!!");
                return;
            }
            int message = msg.what;
            OppoAutoConnectManager oppoAutoConnectManager = OppoAutoConnectManager.this;
            oppoAutoConnectManager.logd("handle event:" + ((String) OppoAutoConnectManager.this.mEvtStrHashMap.get(Integer.valueOf(message))));
            if (message == 1) {
                OppoAutoConnectManager.this.handleManuConnect(((Integer) msg.obj).intValue());
            } else if (message == 2) {
                OppoAutoConnectManager.this.handleScanResult();
            } else if (message == 4) {
                OppoAutoConnectManager.this.handleDebugDataShow();
            } else if (message == 8) {
                OppoAutoConnectManager.this.handleNetworkDisabled(msg.arg1, msg.arg2);
            } else if (message == 16) {
                int token = msg.arg1;
                if (token == OppoAutoConnectManager.this.mLastManuReassociateToken) {
                    OppoAutoConnectManager.this.handleDelayedManuReassociate();
                    return;
                }
                OppoAutoConnectManager oppoAutoConnectManager2 = OppoAutoConnectManager.this;
                oppoAutoConnectManager2.logd("token mismatch: token=" + token + " and mLastManuReassociateToken=" + OppoAutoConnectManager.this.mLastManuReassociateToken);
            } else if (message == 32) {
                OppoAutoConnectManager.this.handleNetworkDeleted((WifiConfiguration) msg.obj);
            } else if (message == 64) {
                OppoAutoConnectManager.this.handleWifiConnectStateChanged(msg.arg1, msg.arg2);
            } else if (message == 128) {
                OppoAutoConnectManager.this.handleUpdateNetworkDisabledCount(msg.arg1, msg.arg2);
            } else if (message == 256) {
                OppoAutoConnectManager.this.handleScreenOn();
            } else if (message == 512) {
                OppoAutoConnectManager.this.handleEnableAllNetworks();
            } else if (message == 2048) {
                OppoAutoConnectManager.this.handleNetworkReSaved((WifiConfiguration) msg.obj);
            } else if (message == 4096) {
                OppoAutoConnectManager.this.handleThirdAPKConnect(((Integer) msg.obj).intValue());
            }
        }
    }

    private int getRssiGood() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return RSSI_GOOD;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_GOOD", Integer.valueOf((int) RSSI_GOOD)).intValue();
    }

    private int getRssiBad() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return RSSI_BAD;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_BAD", Integer.valueOf((int) RSSI_BAD)).intValue();
    }

    private int getRssiLow() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return RSSI_LOW;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_LOW", Integer.valueOf((int) RSSI_LOW)).intValue();
    }

    private int getRssiStep() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 3;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_STEP", 3).intValue();
    }

    private int getContinuousDebounceDisconnectStepCount() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 5;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_COUNT", 5).intValue();
    }

    private int getContinuousDebounceDisconnectStepTime() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 60;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME", 60).intValue();
    }

    private int getContinuousDebounceDisconnectIntervalTime() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 1000;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_INTERVAL_TIME", 1000).intValue();
    }

    private int getNoInternetStepCount() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 1;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_NO_INTERNET_STEP_COUNT", 1).intValue();
    }

    private int getNoInternetStepTime() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 120;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_NO_INTERNET_STEP_TIME", 120).intValue();
    }

    private int getAssocRejectStepCount() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 4;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_STEP_COUNT", 4).intValue();
    }

    private int getAssocRejectExpBase() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 2;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_EXP_BASE", 2).intValue();
    }

    private int getAssocRejectMultipleBase() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 5;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_MULTIPLE_BASE", 5).intValue();
    }

    private int getAssocRejectRetryTimeMin() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return ASSOC_REJECT_RETRY_TIME_MIN;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_RETRY_TIME_MIN", Integer.valueOf((int) ASSOC_REJECT_RETRY_TIME_MIN)).intValue();
    }

    private int getDhcpFailureStepCount() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 4;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_STEP_COUNT", 4).intValue();
    }

    private int getDhcpFailureExpBase() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 2;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_EXP_BASE", 2).intValue();
    }

    private int getDhcpFailureMultipleBase() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 5;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_MULTIPLE_BASE", 5).intValue();
    }

    private int getDhcpFailureRetryTimeMin() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 30000;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_RETRY_TIME_MIN", 30000).intValue();
    }

    private int getWrongKeyStepCount() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 2;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_STEP_COUNT", 2).intValue();
    }

    private int getWrongKeyExpBase() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 2;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_EXP_BASE", 2).intValue();
    }

    private int getWrongKeyMultipleBase() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 10;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_MULTIPLE_BASE", 10).intValue();
    }

    private int getWrongKeyRetryTimeMin() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 10000;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_RETRY_TIME_MIN", 10000).intValue();
    }

    private int getAuthFailureStepCount() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 2;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_STEP_COUNT", 2).intValue();
    }

    private int getAuthFailureExpBase() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 2;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_EXP_BASE", 2).intValue();
    }

    private int getAuthFailureMultipleBase() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 10;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_MULTIPLE_BASE", 10).intValue();
    }

    private int getAuthFailureRetryRimeMin() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 10000;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_RETRY_TIME_MIN", 10000).intValue();
    }

    private int getKeepDisabledTimeMax() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 3600000;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_KEEP_DISABLED_TIME_MAX", 3600000).intValue();
    }

    private int getContinuousDisabledCountMax() {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper == null) {
            return 12;
        }
        return wifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DISABLED_COUNT_MAX", 12).intValue();
    }

    public boolean isManuConnect() {
        return getIsOppoManuConnect();
    }

    private OppoAutoConnectManager(Context mCtxt, ClientModeImpl mWsm, WifiConfigManager mWcm, WifiNative mWnt) {
        boolean z = true;
        sContext = mCtxt;
        sWifiStateMachine = mWsm;
        sWifiConfigManager = mWcm;
        sWifiNative = mWnt;
        Handler hldler = sWifiStateMachine.getHandler();
        if (hldler != null) {
            this.mHandler = new OppoAutoConnectManagerHandler(hldler.getLooper());
        }
        printStringInit();
        listenForBroadcasts();
        this.mWifiAssistantEnabled = Settings.Global.getInt(sContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1 ? false : z;
        this.mOperaterSsids = getOperatorApSsids();
    }

    public static void init(Context mCtxt, ClientModeImpl mWsm, WifiConfigManager mWcs, OppoWifiAssistantStateTraker mWns, WifiNative mWnt, WifiRomUpdateHelper mWruh, ScanRequestProxy mSrp) {
        sContext = mCtxt;
        sWifiStateMachine = mWsm;
        sWifiConfigManager = mWcs;
        sWifiNetworkStateTraker = mWns;
        sWifiNative = mWnt;
        sWifiRomUpdateHelper = mWruh;
        mScanRequestProxy = mSrp;
        sSimpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    }

    public static OppoAutoConnectManager getInstance() {
        if (sContext == null) {
            Log.d(TAG, "sContext is null");
            return null;
        } else if (sWifiStateMachine == null) {
            Log.d(TAG, "sWifiStateMachine is null");
            return null;
        } else if (sWifiConfigManager == null) {
            Log.d(TAG, "sWifiConfigManager is null");
            return null;
        } else if (sWifiNative == null) {
            Log.d(TAG, "sWifiNative is null");
            return null;
        } else {
            synchronized (OppoAutoConnectManager.class) {
                if (sInstance == null) {
                    sInstance = new OppoAutoConnectManager(sContext, sWifiStateMachine, sWifiConfigManager, sWifiNative);
                }
            }
            return sInstance;
        }
    }

    public boolean isWlanAssistAutoConnectNetwork() {
        Log.d(TAG, "isWlanAssistAutoConnectNetwork is " + this.mWlanAssistConnect);
        return this.mWlanAssistConnect;
    }
}
