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
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.ColorOSTelephonyManager;
import android.util.Log;
import android.util.MathUtils;
import com.android.server.WifiRomUpdateHelper;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.TelephonyUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
    private static final int BUFFER_LENGTH = 40;
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
    private static final int DISABLED_REASON_AUTHENTICATION_NO_CREDENTIALS = 8;
    private static final int DISABLED_REASON_AUTH_FAILURE = 3;
    private static final int DISABLED_REASON_BY_WIFI_MANAGER = 10;
    private static final int DISABLED_REASON_CONTINUOUS_DEBOUNCE_DISCONNECT = 200;
    private static final int DISABLED_REASON_DHCP_FAILURE = 4;
    private static final int DISABLED_REASON_DISABLED_BAD_LINK = 1;
    private static final int DISABLED_REASON_DNS_FAILURE = 5;
    private static final int DISABLED_REASON_INVALID = 0;
    private static final int DISABLED_REASON_NO_INTERNET = 9;
    private static final int DISABLED_REASON_OPPO_AUTO_CONNECT_BASE = 200;
    private static final int DISABLED_REASON_TLS_VERSION_MISMATCH = 7;
    private static final int DISABLED_REASON_UNKNWON = 102;
    private static final int DISABLED_REASON_WRONG_KEY = 12;
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
    private static final String TAG = "OppoAutoConnectManager";
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
    private static Context sContext;
    private static OppoAutoConnectManager sInstance;
    private static SimpleDateFormat sSimpleDateFormat;
    private static WifiConfigManager sWifiConfigManager;
    private static WifiNative sWifiNative;
    private static OppoWifiAssistantStateTraker sWifiNetworkStateTraker;
    private static WifiRomUpdateHelper sWifiRomUpdateHelper;
    private static WifiStateMachine sWifiStateMachine;
    private final BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {
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
    private int mConnectingNetId = -1;
    private HashMap<Integer, String> mDisableReasonStrHashMap = new HashMap();
    private HashMap<Integer, String> mEvtStrHashMap = new HashMap();
    private OppoAutoConnectManagerHandler mHandler;
    private OppoAutoConnectConfiguration mLastConnectedConfiguration = new OppoAutoConnectConfiguration();
    private int mLastManuConnectNetId = -1;
    private long mLastManuReassociateTime = 0;
    private int mLastManuReassociateToken = 0;
    private long mLastTriggerEvtTimeMillis = 0;
    private NetworkInfo mNetworkInfo = null;
    private HashMap<String, OppoAutoConnectConfiguration> mOppoAutoConnectConfigurationHashMap = new HashMap();
    private List<ScanResult> mScanResultList = null;
    private boolean mSettingManuConnect = false;
    private boolean mSupplicantCompleted = false;
    private boolean mThirdAPKConnect = false;
    private int mTriggerAutoConnectEvtSet = 0;
    private boolean mWifiAssistantEnabled = true;
    private boolean mWifiAssistantRomupdate = true;

    public class OppoAutoConnectConfiguration {
        private String mConfigKey = null;
        private int mConituousDebounceDisconnectCount = 0;
        private int mContinuousDisabledCount = 0;
        private int mDisabledLevel = -127;
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
                case 64:
                    OppoAutoConnectManager.this.handleWifiConnectStateChanged(msg.arg1, msg.arg2);
                    break;
                case 128:
                    OppoAutoConnectManager.this.handleUpdateNetworkDisabledCount(msg.arg1, msg.arg2);
                    break;
                case 256:
                    OppoAutoConnectManager.this.handleScreenOn();
                    break;
                case 512:
                    OppoAutoConnectManager.this.handleEnableAllNetworks();
                    break;
                case 2048:
                    OppoAutoConnectManager.this.handleNetworkReSaved((WifiConfiguration) msg.obj);
                    break;
                case 4096:
                    OppoAutoConnectManager.this.handleThirdAPKConnect(((Integer) msg.obj).intValue());
                    break;
            }
        }
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
                this.mLastConnectedConfiguration.mDisabledLevel = -127;
                this.mLastConnectedConfiguration.mDisabledUntilTime = 0;
                this.mLastConnectedConfiguration.mLastConnectedTime = 0;
            }
        }
    }

    private int getCurrentScanResultBestLevel(List<ScanResult> srList, String configKey) {
        if (configKey == null || srList == null || srList.size() <= 0) {
            loge("configKey is null,or srList is null/empty!!");
            return -127;
        }
        int level = -127;
        String tmpConfigKey = " ";
        for (ScanResult sr : srList) {
            tmpConfigKey = WifiConfiguration.configKey(sr);
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
        int rssiGood = getRssiGood();
        int keepDisabledTimeMax = getKeepDisabledTimeMax();
        int dhcpFailureStepCount = getDhcpFailureStepCount();
        int dhcpFailureMultipleBase = getDhcpFailureMultipleBase();
        int dhcpFailureExpBase = getDhcpFailureExpBase();
        int dhcpFailureRetryTimeMin = getDhcpFailureRetryTimeMin();
        int assocRejectStepCount = getAssocRejectStepCount();
        int assocRejectMultipleBase = getAssocRejectMultipleBase();
        int assocRejectExpBase = getAssocRejectExpBase();
        int assocRejectRetryTimeMin = getAssocRejectRetryTimeMin();
        int wrongKeyStepCount = getWrongKeyStepCount();
        int wrongKeyMultipleBase = getWrongKeyMultipleBase();
        int wrongKeyExpBase = getWrongKeyExpBase();
        int wrongKeyRetryTimeMin = getWrongKeyRetryTimeMin();
        int authFailureStepCount = getAuthFailureStepCount();
        int authFailureMultipleBase = getAuthFailureMultipleBase();
        int authFailureExpBase = getAuthFailureExpBase();
        int authFailureRetryTimeMin = getAuthFailureRetryRimeMin();
        int continuousDebounceDisconnectStepCount = getContinuousDebounceDisconnectStepCount();
        int continuousDebounceDisconnectStepTime = getContinuousDebounceDisconnectStepTime();
        int noNetworkStepCount = getNoInternetStepCount();
        int noNetworkStepTime = getNoInternetStepTime();
        logd("mDisabledReason:" + oppoAutoConnConf.mDisabledReason + ",mContinuousDisabledCount:" + oppoAutoConnConf.mContinuousDisabledCount);
        int integerMultiple;
        switch (oppoAutoConnConf.mDisabledReason) {
            case 2:
                if (continuousDisabledCount >= assocRejectStepCount) {
                    integerMultiple = continuousDisabledCount / assocRejectStepCount;
                    if (disabledLevel > rssiGood) {
                        keepDisabledUntilAdd = ((long) Math.pow((double) assocRejectExpBase, (double) integerMultiple)) * 1000;
                    } else {
                        keepDisabledUntilAdd = (long) ((assocRejectMultipleBase * integerMultiple) * OppoManuConnectManager.UID_DEFAULT);
                    }
                    if (keepDisabledUntilAdd <= ((long) assocRejectRetryTimeMin)) {
                        keepDisabledUntilAdd = (long) assocRejectRetryTimeMin;
                        break;
                    }
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
                break;
            case 3:
                if (continuousDisabledCount >= authFailureStepCount) {
                    integerMultiple = continuousDisabledCount / authFailureStepCount;
                    if (disabledLevel > rssiGood) {
                        keepDisabledUntilAdd = ((long) Math.pow((double) authFailureExpBase, (double) integerMultiple)) * 1000;
                    } else {
                        keepDisabledUntilAdd = (long) ((authFailureMultipleBase * integerMultiple) * OppoManuConnectManager.UID_DEFAULT);
                    }
                    if (keepDisabledUntilAdd <= ((long) authFailureRetryTimeMin)) {
                        keepDisabledUntilAdd = (long) authFailureRetryTimeMin;
                        break;
                    }
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
                break;
            case 4:
                if (continuousDisabledCount >= dhcpFailureStepCount) {
                    integerMultiple = continuousDisabledCount / dhcpFailureStepCount;
                    if (disabledLevel > rssiGood) {
                        keepDisabledUntilAdd = ((long) Math.pow((double) dhcpFailureExpBase, (double) integerMultiple)) * 1000;
                    } else {
                        keepDisabledUntilAdd = (long) ((dhcpFailureMultipleBase * integerMultiple) * OppoManuConnectManager.UID_DEFAULT);
                    }
                    if (keepDisabledUntilAdd <= ((long) dhcpFailureRetryTimeMin)) {
                        keepDisabledUntilAdd = (long) dhcpFailureRetryTimeMin;
                        break;
                    }
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
                break;
            case 9:
                if (continuousDisabledCount % noNetworkStepCount == 0) {
                    keepDisabledUntilAdd = (long) ((noNetworkStepTime * (continuousDisabledCount / noNetworkStepCount)) * OppoManuConnectManager.UID_DEFAULT);
                    break;
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
            case 12:
                if (continuousDisabledCount >= wrongKeyStepCount) {
                    integerMultiple = continuousDisabledCount / wrongKeyStepCount;
                    if (disabledLevel > rssiGood) {
                        keepDisabledUntilAdd = ((long) Math.pow((double) wrongKeyExpBase, (double) integerMultiple)) * 1000;
                    } else {
                        keepDisabledUntilAdd = (long) ((wrongKeyMultipleBase * integerMultiple) * OppoManuConnectManager.UID_DEFAULT);
                    }
                    if (keepDisabledUntilAdd <= ((long) wrongKeyRetryTimeMin)) {
                        keepDisabledUntilAdd = (long) wrongKeyRetryTimeMin;
                        break;
                    }
                }
                baseTime = oppoAutoConnConf.mDisabledUntilTime;
                break;
                break;
            case ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS /*200*/:
                keepDisabledUntilAdd = (long) ((continuousDisabledCount * continuousDebounceDisconnectStepTime) * OppoManuConnectManager.UID_DEFAULT);
                break;
        }
        logd("keep Disabled from base Time:" + getFormatDateTime(baseTime) + " add " + keepDisabledUntilAdd + " millis");
        if (keepDisabledUntilAdd - ((long) keepDisabledTimeMax) > 0) {
            logd("keepDisabledUntilAdd is too larger adjust to " + keepDisabledTimeMax + " millis");
            keepDisabledUntilAdd = (long) keepDisabledTimeMax;
        }
        return baseTime + keepDisabledUntilAdd;
    }

    private void handleManuConnect(int netId) {
        this.mLastManuReassociateToken = 0;
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
            return;
        }
        if (msg.obj.state == SupplicantState.DISCONNECTED) {
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
                    canEnabledNetworksList = getNetworksExcept(scannedConfiguredNetworksList, 9);
                } else {
                    canEnabledNetworksList = scannedConfiguredNetworksList;
                }
            }
        }
        enableNetworks(canEnabledNetworksList);
    }

    private void handleScanResult() {
        this.mScanResultList = getScanResultList();
        if (getIsOppoManuConnecting()) {
            logd("man connect in progress,manuReassociate if possible!!");
            if (!wifiIsConnectedOrConnecting()) {
                int netId = getOppoManuConnectNetworkId();
                WifiConfiguration wConf = getWifiConfiguration(netId);
                String ssid = null;
                String gbkSsid = null;
                boolean isHiddenGBKSsid = false;
                boolean isHiddenAP = false;
                if (wConf != null) {
                    ssid = wConf.SSID;
                    isHiddenAP = wConf.hiddenSSID;
                }
                if (!(!isHiddenAP || sWifiNative == null || ssid == null)) {
                    gbkSsid = sWifiNative.ssidStrFromGbkHistory(ssid);
                    if (!(gbkSsid == null || (gbkSsid.equals(ssid) ^ 1) == 0)) {
                        isHiddenGBKSsid = true;
                    }
                }
                if (isHiddenGBKSsid) {
                    logd("connect hidden gbk ssid = " + gbkSsid);
                    sWifiStateMachine.startConnectToNetwork(netId, OppoManuConnectManager.UID_DEFAULT, "any");
                } else {
                    manuReassociate();
                }
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
            if (reason == 102 || reason == 10) {
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
                if (oppoAutoConnConf.mDisabledLevel == -127) {
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
        enableNetworksConditional();
        if (checkBasicConditions()) {
            startAutoConnect();
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
                            manuReassociate();
                        } else if (!getIsOppoManuConnected()) {
                            reconnect(selectedConf.networkId);
                        }
                    }
                    if (this.mLastConnectedConfiguration != null && this.mLastConnectedConfiguration.mConfigKey != null) {
                        if (this.mLastConnectedConfiguration.mConfigKey.equals(configKey)) {
                            long currTime = System.currentTimeMillis();
                            int continuousDebounceDisconnectIntervalTime = getContinuousDebounceDisconnectIntervalTime();
                            int continuousDebounceDisconnectStepCount = getContinuousDebounceDisconnectStepCount();
                            if (currTime - this.mLastConnectedConfiguration.mLastConnectedTime <= ((long) continuousDebounceDisconnectIntervalTime)) {
                                if (this.mSupplicantCompleted) {
                                    OppoAutoConnectConfiguration oppoAutoConnectConfiguration = this.mLastConnectedConfiguration;
                                    oppoAutoConnectConfiguration.mConituousDebounceDisconnectCount = oppoAutoConnectConfiguration.mConituousDebounceDisconnectCount + 1;
                                }
                            } else if (this.mSupplicantCompleted) {
                                this.mLastConnectedConfiguration.mConituousDebounceDisconnectCount = 0;
                                OppoAutoConnectConfiguration oaccf = getAutoConnectConfiguration(configKey);
                                if (oaccf != null && oaccf.mDisabledReason == ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS) {
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
                            break;
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
                    OppoAutoConnectConfiguration oacc = getAutoConnectConfiguration(configKey);
                    if (!(oacc == null || oacc.mDisabledReason == ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS)) {
                        removeAutoConnectConfiguration(configKey);
                        break;
                    }
                case 3:
                    DetailedState detailState = DetailedState.DISCONNECTED;
                    if (this.mNetworkInfo != null) {
                        detailState = this.mNetworkInfo.getDetailedState();
                        break;
                    }
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
            setTriggerAutoConnectEvt(1024);
        } else if (wifiState == 1 && Global.getInt(sContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE, 1) == 0) {
            this.mScanResultList = new ArrayList();
        }
    }

    public void sendEnableAllNetworksEvt() {
        sendMessage(512);
    }

    void handleEnableAllNetworks() {
        logd("handleEnableAllNetworks");
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
                    if (!(currConfig.preSharedKey == null || oldConfig.preSharedKey == null || (currConfig.preSharedKey.equals(oldConfig.preSharedKey) ^ 1) == 0)) {
                        logd("curr pwd:" + currConfig.preSharedKey + " old pwd: " + oldConfig.preSharedKey);
                        pwdChanged = true;
                    }
                } else if (isWepAP(currConfig)) {
                    if (!(currConfig.wepKeys == null || oldConfig.wepKeys == null || currConfig.wepKeys[currConfig.wepTxKeyIndex] == null || oldConfig.wepKeys[oldConfig.wepTxKeyIndex] == null || (currConfig.wepKeys[currConfig.wepTxKeyIndex].equals(oldConfig.wepKeys[oldConfig.wepTxKeyIndex]) ^ 1) == 0)) {
                        logd("curr pwd:" + currConfig.wepKeys[currConfig.wepTxKeyIndex] + " old pwd: " + oldConfig.wepKeys[oldConfig.wepTxKeyIndex]);
                        pwdChanged = true;
                    }
                } else if (isWapiPskAp(currConfig)) {
                    if (!(currConfig.preSharedKey == null || oldConfig.preSharedKey == null || (currConfig.preSharedKey.equals(oldConfig.preSharedKey) ^ 1) == 0)) {
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
                    if (!(currId == null || oldId == null || currPwd == null || oldPwd == null || (currId.equals(oldId) && (currPwd.equals(oldPwd) ^ 1) == 0))) {
                        logd("curr (id:" + currId + " pwd:" + currPwd + ") " + "old (id:" + oldId + " pwd:" + oldPwd + ")");
                        pwdChanged = true;
                    }
                }
                logd("pwd changed: " + pwdChanged);
                if (pwdChanged && oacc != null && (oacc.mDisabledReason == 12 || oacc.mDisabledReason == 3)) {
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
        if (getTriggerAutoConnectEvt(1024)) {
            evtStr = evtStr + "," + ((String) this.mEvtStrHashMap.get(Integer.valueOf(1024)));
        }
        if (getTriggerAutoConnectEvt(32)) {
            return evtStr + "," + ((String) this.mEvtStrHashMap.get(Integer.valueOf(32)));
        }
        return evtStr;
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
            if (wnrList == null || wnrList.size() <= 0 || (this.mWifiAssistantRomupdate ^ 1) != 0) {
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
            List<WifiConfiguration> internetedNetworkList = new ArrayList();
            List<OppoWifiAssistantRecord> wnrList = sWifiNetworkStateTraker.getWifiNetworkRecords();
            if (wnrList == null || wnrList.size() <= 0 || (this.mWifiAssistantRomupdate ^ 1) != 0) {
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
        String srConfigKey = " ";
        String wcConfigKey = " ";
        List<WifiConfiguration> scannedConfiguredNetworks = new ArrayList();
        for (WifiConfiguration wc : wcList) {
            wcConfigKey = wc.configKey();
            if (wcConfigKey != null) {
                for (ScanResult sr : srList) {
                    srConfigKey = WifiConfiguration.configKey(sr);
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
        int rssiLow = getRssiLow();
        int rssiBad = getRssiBad();
        int disabledReason = oppoAutoConnConf.mDisabledReason;
        int disabledLevel = oppoAutoConnConf.mDisabledLevel;
        int rssiGap = bestLevel - disabledLevel;
        int rssiGapAbsoluteValue = (int) MathUtils.abs((float) rssiGap);
        logd("rssiGap:" + rssiGap + ", rssiGapAbsoluteValue:" + rssiGapAbsoluteValue);
        long timeGap = oppoAutoConnConf.mDisabledUntilTime - System.currentTimeMillis();
        logd("disabled reason:" + disabledReason + "(" + ((String) this.mDisableReasonStrHashMap.get(Integer.valueOf(disabledReason))) + ")" + ",best level:" + bestLevel + ", last disabledLevel:" + disabledLevel);
        boolean needEnable = false;
        int rssiGood = getRssiGood();
        int rssiStep = getRssiStep();
        switch (disabledReason) {
            case 2:
            case 4:
                if (disabledLevel < rssiGood) {
                    if (disabledLevel < rssiLow) {
                        if (disabledLevel < rssiBad) {
                            if (rssiGap > rssiStep) {
                                needEnable = true;
                                break;
                            }
                        } else if (bestLevel < rssiLow) {
                            if (rssiGap < rssiStep) {
                                if (rssiGapAbsoluteValue < rssiStep) {
                                    if (timeGap <= 0) {
                                        needEnable = true;
                                        break;
                                    }
                                    logd("bad rssi (+/-" + rssiStep + ") ,and not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
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
                    } else if (bestLevel < rssiGood) {
                        if (rssiGap < rssiStep) {
                            if (rssiGapAbsoluteValue < rssiStep) {
                                if (timeGap <= 0) {
                                    needEnable = true;
                                    break;
                                }
                                logd("low rssi(+/-" + rssiStep + ") ,and not reach the disabledUntil Time:" + getFormatDateTime(oppoAutoConnConf.mDisabledUntilTime) + " timeGap=" + timeGap + "ms,ignore");
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
                } else if (bestLevel < rssiGood) {
                    if (rssiGapAbsoluteValue < rssiStep) {
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
                if (rssiGap <= rssiStep) {
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
            case 9:
                if (this.mWifiAssistantEnabled) {
                    if (bestLevel < rssiGood || disabledLevel > rssiLow) {
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
            case 12:
                if (rssiGap <= rssiStep) {
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
                if (bestLevel < rssiGood || disabledLevel > rssiLow) {
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
        String configuredConfigKey = " ";
        String oppoAutoConnConigKey = " ";
        for (WifiConfiguration wc : wcList) {
            if (wc != null) {
                configuredConfigKey = wc.configKey();
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

    public void addOrUpdateSingtelAp() {
        if (isSingtelArea()) {
            String added = SystemProperties.get(SINGTEL_WIFI_ADDED_PROPERTY, SINGTEL_WIFI_NO_ADDED_STR);
            logd("singtel_wifi_added = " + added);
            if (SINGTEL_WIFI_ADDED_STR.equals(added)) {
                logd("singtel wifi already added!");
                return;
            }
            WifiConfiguration savedWconf = null;
            String savedConfigKey = "\"Singtel WIFI\"-" + KeyMgmt.strings[2];
            if (sWifiConfigManager != null) {
                savedWconf = sWifiConfigManager.getConfiguredNetwork(savedConfigKey);
            }
            if (savedWconf != null) {
                logd("singtel wifi is already exists!");
                SystemProperties.set(SINGTEL_WIFI_ADDED_PROPERTY, SINGTEL_WIFI_ADDED_STR);
                return;
            }
            int defaultSimSlot;
            WifiConfiguration wc = new WifiConfiguration();
            wc.SSID = SINGTEL_OPERATOR_AP_SSID_QUOTED;
            wc.allowedKeyManagement.set(2);
            wc.allowedKeyManagement.set(3);
            wc.enterpriseConfig = new WifiEnterpriseConfig();
            int simIndex = getValidSingtelOpratorSimCardNum();
            if (simIndex == 0) {
            }
            if (simIndex == 1 || simIndex == 2) {
                defaultSimSlot = simIndex;
            } else {
                defaultSimSlot = 1;
            }
            wc.enterpriseConfig.setEapMethod(4);
            wc.enterpriseConfig.setSimNum(defaultSimSlot);
            if (sWifiConfigManager != null) {
                logd("addOrUpdateNetwork singetal wifi");
                if (sWifiConfigManager.addOrUpdateNetwork(wc, OppoManuConnectManager.UID_DEFAULT).isSuccess()) {
                    SystemProperties.set(SINGTEL_WIFI_ADDED_PROPERTY, SINGTEL_WIFI_ADDED_STR);
                }
            }
            return;
        }
        logd("not sintel areas or no valid ");
    }

    private boolean isSingtelArea() {
        if (SystemProperties.get("persist.sys.oppo.region", "CN").equals("SG") && SystemProperties.get("ro.oppo.operator", "NULL").equals("SINGTEL")) {
            return true;
        }
        return false;
    }

    boolean hasValidSingtelOperatorSimCard() {
        boolean z = false;
        if (SystemProperties.get("persist.sys.oppo.region", "CN").equals("SG") && (SystemProperties.get("ro.oppo.operator", "NULL").equals("SINGTEL") ^ 1) == 0) {
            int validSingtelSimCardNum = getValidSingtelOpratorSimCardNum();
            if (validSingtelSimCardNum <= 0) {
                logd("has no valid singtel simcard!!");
            }
            if (validSingtelSimCardNum > 0) {
                z = true;
            }
            return z;
        }
        logd("not in singtel area!");
        return false;
    }

    public int getValidOpratorSimCardNum() {
        int simCardIndex = 0;
        ColorOSTelephonyManager cotm = ColorOSTelephonyManager.getDefault(sContext);
        if (cotm == null) {
            return 0;
        }
        if (!cotm.hasIccCardGemini(0) && (cotm.hasIccCardGemini(1) ^ 1) != 0) {
            return 0;
        }
        if (cotm.hasIccCardGemini(0) && cotm.getSimOperatorGemini(0) != null) {
            simCardIndex = 1;
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
        if (!cotm.hasIccCardGemini(0) && (cotm.hasIccCardGemini(1) ^ 1) != 0) {
            return 0;
        }
        String operator1 = cotm.getSimOperatorGemini(0);
        String operator2 = cotm.getSimOperatorGemini(1);
        if (operator1 != null && (operator1.equals("52501") || operator1.equals("52502") || operator1.equals("52507"))) {
            simCardIndex = 1;
        }
        if (operator2 != null && (operator2.equals("52501") || operator2.equals("52502") || operator2.equals("52507"))) {
            simCardIndex += 2;
        }
        logd("singtel simCardIndex = " + simCardIndex);
        return simCardIndex;
    }

    public boolean getSingtelAutoConnectEnabled() {
        int enabled;
        if (sWifiRomUpdateHelper == null) {
            enabled = 0;
        } else {
            enabled = sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ENABLE_SINGTEL_AUTOCONNECT", Integer.valueOf(1)).intValue();
        }
        logd("getSingtelAutoConnectEnabled  = " + enabled);
        if (enabled > 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:6:0x001d, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        if (str != null && str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
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
            List<WifiConfiguration> candicatList = new ArrayList();
            for (WifiConfiguration wc : wcList) {
                if (wc != null && isSingtelOperatorAp(wc.networkId)) {
                    logd("add singtel ap:" + wc.SSID);
                    candicatList.add(wc);
                }
            }
            if (candicatList == null || candicatList.size() <= 0) {
                logd("candicatList is null or emptey!!");
                return null;
            }
            WifiConfiguration candicator = null;
            if (candicatList.size() == 1) {
                candicator = (WifiConfiguration) candicatList.get(0);
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
                    if (isSingtelOperatorAp(netId) && hasValidSingtelOperatorSimCard()) {
                        logd("manu connect, try switch simslot!");
                    } else {
                        logd("manu connect , no need switch!");
                        return;
                    }
                }
                int simCardIndex = getValidOpratorSimCardNum();
                int simSlot = 1;
                try {
                    simSlot = Integer.parseInt(candicator.enterpriseConfig.getSimNum());
                } catch (Exception ex) {
                    logd("ex = " + ex);
                }
                if (1 == simCardIndex || 2 == simCardIndex) {
                    targetSimCard = simCardIndex;
                } else if (3 == simCardIndex) {
                    targetSimCard = (simSlot % 2) + 1;
                } else {
                    logd("no need set or switch!!");
                }
                logd("targetSimCard = " + targetSimCard);
                if (targetSimCard != -1) {
                    candicator.enterpriseConfig.setSimNum(targetSimCard);
                    candicator.enterpriseConfig.setIdentity(null);
                    if (sWifiConfigManager == null || sWifiConfigManager.addOrUpdateNetwork(candicator, OppoManuConnectManager.UID_DEFAULT).isSuccess()) {
                        WifiConfiguration targetWconf = null;
                        if (sWifiStateMachine != null) {
                            targetWconf = sWifiStateMachine.getTargetWifiConfiguration();
                        }
                        if (targetWconf == null || targetWconf.enterpriseConfig == null || (!targetWconf.enterpriseConfig.getSimNum().equals(candicator.enterpriseConfig.getSimNum()) && targetWconf.networkId == candicator.networkId)) {
                            logd("reconnect  ap with simSlot " + targetSimCard);
                            reconnect(candicator.networkId);
                        }
                    } else {
                        logd("addOrUpdateNetwork to switch simSlot failed");
                    }
                }
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
        int bestLevel = -127;
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
            if (getSingtelAutoConnectEnabled()) {
                selectedConf = pickSingtelWifiConfiguration(savedAndEnaledAndScannedNetworkList);
                if (selectedConf != null) {
                    logd("pick a Singtel WifiConfiguration:" + selectedConf.configKey());
                    return selectedConf;
                }
            }
            if (!this.mWifiAssistantEnabled) {
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
            } else if (saveAndEnabledAndScannedAndCanInternetNetworkList == null || saveAndEnabledAndScannedAndCanInternetNetworkList.size() <= 0) {
                logd("wlan assistant enabled but has no can internet networks!!");
                selectedConf = tryPickCandinate(getNetworksExcept(savedAndEnaledAndScannedNetworkList, 9));
            } else {
                logd("wlan assistant enabled and  find enabled and can internet networks,do nothing!!");
                return null;
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
                    if (getSingtelAutoConnectEnabled() && hasValidSingtelOperatorSimCard() && isSingtelOperatorAp(selectedConf.networkId) && (isSingtelOperatorAp(this.mConnectingNetId) ^ 1) != 0) {
                        logd("trigger auto connect since we find a singtel AP!!");
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
                    if (sWifiNetworkStateTraker != null && this.mWifiAssistantRomupdate) {
                        sWifiNetworkStateTraker.detectScanResult(System.currentTimeMillis());
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
            if (sWifiStateMachine.isSupplicantAvailable()) {
                sWifiConfigManager.enableNetworkEx(netId, false, OppoManuConnectManager.UID_DEFAULT, true);
            } else {
                logd("wifi is in disable or disable pending state,cancel enableNetwork!!");
            }
        }
    }

    private void enableNetworkWithoutBroadcast(int netId) {
        if (!checkInterfaceNull()) {
            if (sWifiStateMachine.isSupplicantAvailable()) {
                sWifiConfigManager.enableNetworkEx(netId, false, OppoManuConnectManager.UID_DEFAULT, false);
            } else {
                logd("wifi is in disable or disable pending state,cancel enableNetworkWithoutBroadcast!!");
            }
        }
    }

    private void disableNetwork(int netId, int reason) {
        if (!checkInterfaceNull()) {
            if (sWifiStateMachine.isSupplicantAvailable()) {
                sWifiConfigManager.disableNetworkEx(netId, reason, true);
            } else {
                logd("wifi is in disable or disable pending state,cancel disableNetwork!!");
            }
        }
    }

    private void disableNetworkWithoutBoradcast(int netId, int reason) {
        if (!checkInterfaceNull()) {
            if (sWifiStateMachine.isSupplicantAvailable()) {
                sWifiConfigManager.disableNetworkEx(netId, reason, false);
            } else {
                logd("wifi is in disable or disable pending state,cancel disableNetworkWithoutBoradcast!!");
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
        return sWifiStateMachine.syncGetScanResultsList();
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
            if (sWifiStateMachine.isSupplicantAvailable()) {
                long timeDiff = System.currentTimeMillis() - this.mLastManuReassociateTime;
                logd("manu reassociate timeDiff:" + timeDiff);
                if (timeDiff > 1000) {
                    sWifiNative.manuReassociate();
                    this.mLastManuReassociateTime = System.currentTimeMillis();
                } else {
                    logd("manu reassociate too frequence,ignore!!");
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
            } else if (!sWifiStateMachine.isSupplicantAvailable()) {
                logd("wifi is in disable or disable pending state,cancel reconnect!!");
            } else if (sWifiStateMachine.isNetworkAutoConnectingOrConnected(networkId)) {
                logd("networkid: " + networkId + " is connecting or connected, do nothing!");
            } else {
                sWifiStateMachine.setTargetNetworkId(networkId);
                sWifiStateMachine.clearTargetBssid(TAG);
                sWifiStateMachine.prepareForForcedConnection(networkId);
                sWifiStateMachine.startConnectToNetwork(networkId, OppoManuConnectManager.UID_DEFAULT, "any");
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
        return wc.allowedKeyManagement.get(1) || wc.allowedKeyManagement.get(4);
    }

    private boolean isWpaEapAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(2)) {
            return true;
        }
        return false;
    }

    private boolean isWapiPskAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(190)) {
            return true;
        }
        return false;
    }

    private boolean isWapiCertAp(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(191)) {
            return true;
        }
        return false;
    }

    private String getFormatDateTime(long timeMillis) {
        if (sSimpleDateFormat == null) {
            return Long.toString(timeMillis);
        }
        Date date = new Date(timeMillis);
        if (date == null) {
            return Long.toString(timeMillis);
        }
        return sSimpleDateFormat.format(date);
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
        this.mEvtStrHashMap.put(Integer.valueOf(4096), "EVT_THIRD_APK_CONNECT");
        this.mEvtStrHashMap.put(Integer.valueOf(2), "EVT_SCAN_RESULT");
        this.mEvtStrHashMap.put(Integer.valueOf(4), "EVT_DEBUG_DATA_SHOW");
        this.mEvtStrHashMap.put(Integer.valueOf(8), "EVT_NETWORK_DISABLED");
        this.mEvtStrHashMap.put(Integer.valueOf(32), "EVT_NETWORK_DELETED");
        this.mEvtStrHashMap.put(Integer.valueOf(64), "EVT_WIFI_CONNECT_STATE_CHANGED");
        this.mEvtStrHashMap.put(Integer.valueOf(128), "EVT_UPDATE_NETWORK_DISABLED_COUNT");
        this.mEvtStrHashMap.put(Integer.valueOf(256), "EVT_SCREEN_ON");
        this.mEvtStrHashMap.put(Integer.valueOf(512), "EVT_ENABLE_ALL_NETWORKS");
        this.mEvtStrHashMap.put(Integer.valueOf(1024), "EVT_WIFI_STATE_ON");
        this.mEvtStrHashMap.put(Integer.valueOf(2048), "EVT_NETWORK_RESAVED");
        this.mEvtStrHashMap.put(Integer.valueOf(16), "EVT_DELAYED_MANU_REASSOCIATE");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(0), "DISABLED_REASON_INVALID");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(102), "DISABLED_REASON_UNKNWON");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(5), "DISABLED_REASON_DNS_FAILURE");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(4), "DISABLED_REASON_DHCP_FAILURE");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(3), "DISABLED_REASON_AUTH_FAILURE");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(2), "DISABLED_REASON_ASSOCIATION_REJECT");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(10), "DISABLED_REASON_BY_WIFI_MANAGER");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(12), "DISABLED_REASON_WRONG_KEY");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS), "DISABLED_REASON_CONTINUOUS_DEBOUNCE_DISCONNECT");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(9), "DISABLED_REASON_NO_INTERNET");
        this.mDisableReasonStrHashMap.put(Integer.valueOf(10), "DISABLED_REASON_BY_WIFI_MANAGER");
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
        sContext.registerReceiver(this.mBroadcastReciever, intentFilter, null, this.mHandler);
        sContext.getContentResolver().registerContentObserver(Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT), true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                OppoAutoConnectManager oppoAutoConnectManager = OppoAutoConnectManager.this;
                if (Global.getInt(OppoAutoConnectManager.sContext.getContentResolver(), OppoAutoConnectManager.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                    z = false;
                }
                oppoAutoConnectManager.mWifiAssistantEnabled = z;
                Log.d(OppoAutoConnectManager.TAG, "onChange mWifiAssistantEnabled= " + OppoAutoConnectManager.this.mWifiAssistantEnabled);
            }
        });
        sContext.getContentResolver().registerContentObserver(Global.getUriFor(WIFI_ASSISTANT_ROMUPDATE), true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                OppoAutoConnectManager oppoAutoConnectManager = OppoAutoConnectManager.this;
                if (Global.getInt(OppoAutoConnectManager.sContext.getContentResolver(), OppoAutoConnectManager.WIFI_ASSISTANT_ROMUPDATE, 1) != 1) {
                    z = false;
                }
                oppoAutoConnectManager.mWifiAssistantRomupdate = z;
                Log.d(OppoAutoConnectManager.TAG, "onChange mwar= " + OppoAutoConnectManager.this.mWifiAssistantRomupdate);
            }
        });
        sContext.registerReceiver(new BroadcastReceiver() {
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

    private int getRssiGood() {
        if (sWifiRomUpdateHelper == null) {
            return RSSI_GOOD;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_GOOD", Integer.valueOf(RSSI_GOOD)).intValue();
    }

    private int getRssiBad() {
        if (sWifiRomUpdateHelper == null) {
            return RSSI_BAD;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_BAD", Integer.valueOf(RSSI_BAD)).intValue();
    }

    private int getRssiLow() {
        if (sWifiRomUpdateHelper == null) {
            return RSSI_LOW;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_LOW", Integer.valueOf(RSSI_LOW)).intValue();
    }

    private int getRssiStep() {
        if (sWifiRomUpdateHelper == null) {
            return 3;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_RSSI_STEP", Integer.valueOf(3)).intValue();
    }

    private int getContinuousDebounceDisconnectStepCount() {
        if (sWifiRomUpdateHelper == null) {
            return 5;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_COUNT", Integer.valueOf(5)).intValue();
    }

    private int getContinuousDebounceDisconnectStepTime() {
        if (sWifiRomUpdateHelper == null) {
            return 60;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME", Integer.valueOf(60)).intValue();
    }

    private int getContinuousDebounceDisconnectIntervalTime() {
        if (sWifiRomUpdateHelper == null) {
            return OppoManuConnectManager.UID_DEFAULT;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_INTERVAL_TIME", Integer.valueOf(OppoManuConnectManager.UID_DEFAULT)).intValue();
    }

    private int getNoInternetStepCount() {
        if (sWifiRomUpdateHelper == null) {
            return 1;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_NO_INTERNET_STEP_COUNT", Integer.valueOf(1)).intValue();
    }

    private int getNoInternetStepTime() {
        if (sWifiRomUpdateHelper == null) {
            return NO_INTERNET_STEP_TIME;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_NO_INTERNET_STEP_TIME", Integer.valueOf(NO_INTERNET_STEP_TIME)).intValue();
    }

    private int getAssocRejectStepCount() {
        if (sWifiRomUpdateHelper == null) {
            return 4;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_STEP_COUNT", Integer.valueOf(4)).intValue();
    }

    private int getAssocRejectExpBase() {
        if (sWifiRomUpdateHelper == null) {
            return 2;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_EXP_BASE", Integer.valueOf(2)).intValue();
    }

    private int getAssocRejectMultipleBase() {
        if (sWifiRomUpdateHelper == null) {
            return 5;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_MULTIPLE_BASE", Integer.valueOf(5)).intValue();
    }

    private int getAssocRejectRetryTimeMin() {
        if (sWifiRomUpdateHelper == null) {
            return ASSOC_REJECT_RETRY_TIME_MIN;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_ASSOC_REJECT_RETRY_TIME_MIN", Integer.valueOf(ASSOC_REJECT_RETRY_TIME_MIN)).intValue();
    }

    private int getDhcpFailureStepCount() {
        if (sWifiRomUpdateHelper == null) {
            return 4;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_STEP_COUNT", Integer.valueOf(4)).intValue();
    }

    private int getDhcpFailureExpBase() {
        if (sWifiRomUpdateHelper == null) {
            return 2;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_EXP_BASE", Integer.valueOf(2)).intValue();
    }

    private int getDhcpFailureMultipleBase() {
        if (sWifiRomUpdateHelper == null) {
            return 5;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_MULTIPLE_BASE", Integer.valueOf(5)).intValue();
    }

    private int getDhcpFailureRetryTimeMin() {
        if (sWifiRomUpdateHelper == null) {
            return 30000;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_DHCP_FAILURE_RETRY_TIME_MIN", Integer.valueOf(30000)).intValue();
    }

    private int getWrongKeyStepCount() {
        if (sWifiRomUpdateHelper == null) {
            return 2;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_STEP_COUNT", Integer.valueOf(2)).intValue();
    }

    private int getWrongKeyExpBase() {
        if (sWifiRomUpdateHelper == null) {
            return 2;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_EXP_BASE", Integer.valueOf(2)).intValue();
    }

    private int getWrongKeyMultipleBase() {
        if (sWifiRomUpdateHelper == null) {
            return 10;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_MULTIPLE_BASE", Integer.valueOf(10)).intValue();
    }

    private int getWrongKeyRetryTimeMin() {
        if (sWifiRomUpdateHelper == null) {
            return 10000;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_WRONG_KEY_RETRY_TIME_MIN", Integer.valueOf(10000)).intValue();
    }

    private int getAuthFailureStepCount() {
        if (sWifiRomUpdateHelper == null) {
            return 2;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_STEP_COUNT", Integer.valueOf(2)).intValue();
    }

    private int getAuthFailureExpBase() {
        if (sWifiRomUpdateHelper == null) {
            return 2;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_EXP_BASE", Integer.valueOf(2)).intValue();
    }

    private int getAuthFailureMultipleBase() {
        if (sWifiRomUpdateHelper == null) {
            return 10;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_MULTIPLE_BASE", Integer.valueOf(10)).intValue();
    }

    private int getAuthFailureRetryRimeMin() {
        if (sWifiRomUpdateHelper == null) {
            return 10000;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_AUTH_FAILURE_RETRY_TIME_MIN", Integer.valueOf(10000)).intValue();
    }

    private int getKeepDisabledTimeMax() {
        if (sWifiRomUpdateHelper == null) {
            return KEEP_DISABLED_TIME_MAX;
        }
        return sWifiRomUpdateHelper.getIntegerValue("OPPO_AUTO_CONNECT_KEEP_DISABLED_TIME_MAX", Integer.valueOf(KEEP_DISABLED_TIME_MAX)).intValue();
    }

    public boolean isManuConnect() {
        return getIsOppoManuConnect();
    }

    private OppoAutoConnectManager(Context mCtxt, WifiStateMachine mWsm, WifiConfigManager mWcm, WifiNative mWnt) {
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
        if (Global.getInt(sContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
            z = false;
        }
        this.mWifiAssistantEnabled = z;
    }

    public static void init(Context mCtxt, WifiStateMachine mWsm, WifiConfigManager mWcs, OppoWifiAssistantStateTraker mWns, WifiNative mWnt, WifiRomUpdateHelper mWruh) {
        sContext = mCtxt;
        sWifiStateMachine = mWsm;
        sWifiConfigManager = mWcs;
        sWifiNetworkStateTraker = mWns;
        sWifiNative = mWnt;
        sWifiRomUpdateHelper = mWruh;
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
}
