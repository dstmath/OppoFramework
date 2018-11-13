package com.android.server;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.android.server.oppo.IElsaManager;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WifiRomUpdateHelper extends RomUpdateHelper {
    public static final String BASIC_FOOL_PROOF_ON = "BASIC_FOOL_PROOF_ON";
    public static final String BASIC_SCAN_CMD_DOWN_COUNT = "BASIC_SCAN_CMD_DOWN_COUNT";
    public static final String BASIC_SCAN_REJECT_COUNT = "BASIC_SCAN_REJECT_COUNT";
    public static final String BASIC_WIFI_OPEN_TIME = "BASIC_WIFI_OPEN_TIME";
    public static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    public static final String CONNECT_DEFAULT_MAX_DHCP_RETRIES = "CONNECT_DEFAULT_MAX_DHCP_RETRIES";
    public static final String CONNECT_DUMPWIFI_WITH_SCREENSHOT = "CONNECT_DUMPWIFI_WITH_SCREENSHOT";
    public static final String CONNECT_ENABLE_REMOVE_NETWORK_WITH_WRONGKEY = "CONNECT_ENABLE_REMOVE_NETWORK_WITH_WRONGKEY";
    public static final String CONNECT_GOOD_RSSI_SWITCH_VALUE = "CONNECT_GOOD_RSSI_SWITCH_VALUE";
    public static final String CONNECT_LINK_FLAPPING_DEBOUNCE_MSEC = "CONNECT_LINK_FLAPPING_DEBOUNCE_MSEC";
    public static final String CONNECT_MAX_RETRIES_MANUAL_ASSOCIATION_REJECT = "CONNECT_MAX_RETRIES_MANUAL_ASSOCIATION_REJECT";
    public static final String CONNECT_MAX_RETRIES_ON_ASSOCIATION_REJECT = "CONNECT_MAX_RETRIES_ON_ASSOCIATION_REJECT";
    public static final String CONNECT_MAX_RETRIES_ON_AUTHENTICATION_FAILURE = "CONNECT_MAX_RETRIES_ON_AUTHENTICATION_FAILURE";
    public static final String CONNECT_MAX_WRONG_KEY_COUNT = "CONNECT_MAX_WRONG_KEY_COUNT";
    public static final String CONNECT_OBTAINING_IP_ADDRESS_GUARD_TIMER_MSEC = "CONNECT_OBTAINING_IP_ADDRESS_GUARD_TIMER_MSEC";
    public static final String CONNECT_ROAM_GUARD_TIMER_MSEC = "CONNECT_ROAM_GUARD_TIMER_MSEC";
    public static final String CONNECT_TIMEOUT_ASSOC_REJECT = "CONNECT_TIMEOUT_ASSOC_REJECT";
    public static final String CONNECT_TIMEOUT_AUTH_FAILURET = "CONNECT_TIMEOUT_AUTH_FAILURET";
    public static final String CONNECT_TIMEOUT_AUTO_CONNECT = "CONNECT_TIMEOUT_AUTO_CONNECT";
    public static final String CONNECT_TIMEOUT_MANUAL_CONNECT = "CONNECT_TIMEOUT_MANUAL_CONNECT";
    public static final String CONNECT_TIMEOUT_P2P_CONNECTED_SELECT = "CONNECT_TIMEOUT_P2P_CONNECTED_SELECT";
    public static final String CONNECT_TIMEOUT_SELECT = "CONNECT_TIMEOUT_SELECT";
    public static final String CONNECT_TRIGGER_DUMPINFO_THRESHOLD = "CONNECT_TRIGGER_DUMPINFO_THRESHOLD";
    private static final String DATA_FILE_PATH = "/data/misc/wifi/sys_wifi_par_config_list.xml";
    private static final String FILE_NAME = "sys_wifi_par_config_list";
    public static final String NETWORK_BLACK_LIST = "NETWORK_BLACK_LIST";
    public static final String NETWORK_CHECK_INTERNET_AFTER_DRIVER_ROAMING = "NETWORK_CHECK_INTERNET_AFTER_DRIVER_ROAMING";
    public static final String NETWORK_DEFAULT_DNS = "NETWORK_DEFAULT_DNS";
    public static final String NETWORK_HANDLE_GATEWAY_CONFLICT = "NETWORK_HANDLE_GATEWAY_CONFLICT";
    public static final String NETWORK_LOCATION_APP = "NETWORK_LOCATION_APP";
    public static final String NETWORK_MTU = "NETWORK_MTU";
    public static final String NETWORK_MTU_SERVER = "NETWORK_MTU_SERVER";
    public static final String NETWORK_PUBLIC_SERVERS_URL = "NETWORK_PUBLIC_SERVERS_URL";
    public static final String NETWORK_SPECIAL_REDIRECT_URL = "NETWORK_SPECIAL_REDIRECT_URL";
    public static final String NETWORK_SYSTEM_APP = "NETWORK_SYSTEM_APP";
    public static final String OPPO_AUTO_CONNECT_ASSOC_REJECT_EXP_BASE = "OPPO_AUTO_CONNECT_ASSOC_REJECT_EXP_BASE";
    public static final String OPPO_AUTO_CONNECT_ASSOC_REJECT_MULTIPLE_BASE = "OPPO_AUTO_CONNECT_ASSOC_REJECT_MULTIPLE_BASE";
    public static final String OPPO_AUTO_CONNECT_ASSOC_REJECT_RETRY_TIME_MIN = "OPPO_AUTO_CONNECT_ASSOC_REJECT_RETRY_TIME_MIN";
    public static final String OPPO_AUTO_CONNECT_ASSOC_REJECT_STEP_COUNT = "OPPO_AUTO_CONNECT_ASSOC_REJECT_STEP_COUNT";
    public static final String OPPO_AUTO_CONNECT_AUTH_FAILURE_EXP_BASE = "OPPO_AUTO_CONNECT_AUTH_FAILURE_EXP_BASE";
    public static final String OPPO_AUTO_CONNECT_AUTH_FAILURE_MULTIPLE_BASE = "OPPO_AUTO_CONNECT_AUTH_FAILURE_MULTIPLE_BASE";
    public static final String OPPO_AUTO_CONNECT_AUTH_FAILURE_RETRY_TIME_MIN = "OPPO_AUTO_CONNECT_AUTH_FAILURE_RETRY_TIME_MIN";
    public static final String OPPO_AUTO_CONNECT_AUTH_FAILURE_STEP_COUNT = "OPPO_AUTO_CONNECT_AUTH_FAILURE_STEP_COUNT";
    public static final String OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_INTERVAL_TIME = "OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_INTERVAL_TIME";
    public static final String OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_COUNT = "OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_COUNT";
    public static final String OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME = "OPPO_AUTO_CONNECT_CONTINUOUS_DEBOUNCE_DISCONNECT_STEP_TIME";
    public static final String OPPO_AUTO_CONNECT_DHCP_FAILURE_EXP_BASE = "OPPO_AUTO_CONNECT_DHCP_FAILURE_EXP_BASE";
    public static final String OPPO_AUTO_CONNECT_DHCP_FAILURE_MULTIPLE_BASE = "OPPO_AUTO_CONNECT_DHCP_FAILURE_MULTIPLE_BASE";
    public static final String OPPO_AUTO_CONNECT_DHCP_FAILURE_RETRY_TIME_MIN = "OPPO_AUTO_CONNECT_DHCP_FAILURE_RETRY_TIME_MIN";
    public static final String OPPO_AUTO_CONNECT_DHCP_FAILURE_STEP_COUNT = "OPPO_AUTO_CONNECT_DHCP_FAILURE_STEP_COUNT";
    public static final String OPPO_AUTO_CONNECT_KEEP_DISABLED_TIME_MAX = "OPPO_AUTO_CONNECT_KEEP_DISABLED_TIME_MAX";
    public static final String OPPO_AUTO_CONNECT_NO_INTERNET_STEP_COUNT = "OPPO_AUTO_CONNECT_NO_INTERNET_STEP_COUNT";
    public static final String OPPO_AUTO_CONNECT_NO_INTERNET_STEP_TIME = "OPPO_AUTO_CONNECT_NO_INTERNET_STEP_TIME";
    public static final String OPPO_AUTO_CONNECT_RSSI_BAD = "OPPO_AUTO_CONNECT_RSSI_BAD";
    public static final String OPPO_AUTO_CONNECT_RSSI_GOOD = "OPPO_AUTO_CONNECT_RSSI_GOOD";
    public static final String OPPO_AUTO_CONNECT_RSSI_LOW = "OPPO_AUTO_CONNECT_RSSI_LOW";
    public static final String OPPO_AUTO_CONNECT_RSSI_STEP = "OPPO_AUTO_CONNECT_RSSI_STEP";
    public static final String OPPO_AUTO_CONNECT_WRONG_KEY_EXP_BASE = "OPPO_AUTO_CONNECT_WRONG_KEY_EXP_BASE";
    public static final String OPPO_AUTO_CONNECT_WRONG_KEY_MULTIPLE_BASE = "OPPO_AUTO_CONNECT_WRONG_KEY_MULTIPLE_BASE";
    public static final String OPPO_AUTO_CONNECT_WRONG_KEY_RETRY_TIME_MIN = "OPPO_AUTO_CONNECT_WRONG_KEY_RETRY_TIME_MIN";
    public static final String OPPO_AUTO_CONNECT_WRONG_KEY_STEP_COUNT = "OPPO_AUTO_CONNECT_WRONG_KEY_STEP_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_COUNT = "OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_DISABLE_TIME = "OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_DISABLE_TIME";
    public static final String OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_TIME = "OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_TIME";
    public static final String OPPO_WIFI_ASSISTANT_BAD_LINK_LOSS_THRESHOLD = "OPPO_WIFI_ASSISTANT_BAD_LINK_LOSS_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_BAD_LINK_SAMPL_INTERVAL = "OPPO_WIFI_ASSISTANT_BAD_LINK_SAMPL_INTERVAL";
    public static final String OPPO_WIFI_ASSISTANT_BAD_LINK_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_BAD_LINK_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_BAD_RSSI_24 = "OPPO_WIFI_ASSISTANT_BAD_RSSI_24";
    public static final String OPPO_WIFI_ASSISTANT_BAD_RSSI_5 = "OPPO_WIFI_ASSISTANT_BAD_RSSI_5";
    public static final String OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_BAD_TCP_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_BAD_TCP_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_CONTROL_SOFTAP = "OPPO_WIFI_ASSISTANT_CONTROL_SOFTAP";
    public static final String OPPO_WIFI_ASSISTANT_DETECT_CONNECT = "OPPO_WIFI_ASSISTANT_DETECT_CONNECT";
    public static final String OPPO_WIFI_ASSISTANT_DIFF_CONNRATE_THRESHOLD = "OPPO_WIFI_ASSISTANT_DIFF_CONNRATE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_DIFF_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_DIFF_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_FEATURE = "OPPO_WIFI_ASSISTANT_FEATURE";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT = "OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_LINK_LOSS_THRESHOLD = "OPPO_WIFI_ASSISTANT_GOOD_LINK_LOSS_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL = "OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_LINK_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_GOOD_LINK_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_RSSI_24 = "OPPO_WIFI_ASSISTANT_GOOD_RSSI_24";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_RSSI_5 = "OPPO_WIFI_ASSISTANT_GOOD_RSSI_5";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_TCP_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_GOOD_TCP_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_INVALID_TRAFFIC_SAMPL_INTERVAL = "OPPO_WIFI_ASSISTANT_INVALID_TRAFFIC_SAMPL_INTERVAL";
    public static final String OPPO_WIFI_ASSISTANT_LOW_LINK_LOSS_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_LINK_LOSS_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_LOW_LINK_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_LINK_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_LOW_RSSI_24 = "OPPO_WIFI_ASSISTANT_LOW_RSSI_24";
    public static final String OPPO_WIFI_ASSISTANT_LOW_RSSI_5 = "OPPO_WIFI_ASSISTANT_LOW_RSSI_5";
    public static final String OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_LOW_TCP_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_TCP_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_MANUAL_DIALOG_TIME = "OPPO_WIFI_ASSISTANT_MANUAL_DIALOG_TIME";
    public static final String OPPO_WIFI_ASSISTANT_NETINVALID_COUNT = "OPPO_WIFI_ASSISTANT_NETINVALID_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_NETSERVER = "OPPO_WIFI_ASSISTANT_NETSERVER";
    public static final String OPPO_WIFI_ASSISTANT_NO_LINK_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_NO_LINK_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_PORTAL_AUTO_DETECT_COUNT = "OPPO_WIFI_ASSISTANT_PORTAL_AUTO_DETECT_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_PORTAL_MANUL_DETECT_COUNT = "OPPO_WIFI_ASSISTANT_PORTAL_MANUL_DETECT_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_ROAM_DETECT = "OPPO_WIFI_ASSISTANT_ROAM_DETECT";
    public static final String OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD = "OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_TEST = "OPPO_WIFI_ASSISTANT_TEST";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_POLL_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_POLL_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_SCORE_GOOD = "OPPO_WIFI_ASSISTANT_WLAN_SCORE_GOOD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_DATA_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_DATA_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD";
    public static final String OPPO_WIFI_DHCP6CLIENT = "OPPO_WIFI_DHCP6CLIENT";
    public static final String POWER_APP_DETECT_AND_KILL = "POWER_APP_DETECT_AND_KILL";
    public static final String POWER_APP_SCAN_COUNT = "POWER_APP_SCAN_COUNT";
    public static final String POWER_APP_SCAN_FREQ = "POWER_APP_SCAN_FREQ";
    public static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String SYS_FILE_PATH = "/system/etc/sys_wifi_par_config_list.xml";
    private static final String TAG = "WifiRomUpdateHelper";
    private boolean DEBUG = false;
    private HashMap<String, String> mKeyValuePair = new HashMap();
    private String[] mMtuServer;

    public class WifiRomUpdateInfo extends UpdateInfo {
        public /* bridge */ /* synthetic */ void clear() {
            super.clear();
        }

        public /* bridge */ /* synthetic */ boolean clone(UpdateInfo other) {
            return super.clone(other);
        }

        public /* bridge */ /* synthetic */ void dump() {
            super.dump();
        }

        public /* bridge */ /* synthetic */ long getVersion() {
            return super.getVersion();
        }

        public /* bridge */ /* synthetic */ boolean insert(int type, String verifyStr) {
            return super.insert(type, verifyStr);
        }

        public /* bridge */ /* synthetic */ boolean updateToLowerVersion(String newContent) {
            return super.updateToLowerVersion(newContent);
        }

        public WifiRomUpdateInfo() {
            super(WifiRomUpdateHelper.this);
        }

        /* JADX WARNING: Removed duplicated region for block: B:29:0x00a5  */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x00a5  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void parseContentFromXML(String content) {
            XmlPullParserException e;
            IOException e2;
            Throwable th;
            if (WifiRomUpdateHelper.this.DEBUG) {
                Log.d(WifiRomUpdateHelper.TAG, "parseContentFromXML");
            }
            if (content == null) {
                Log.d(WifiRomUpdateHelper.TAG, "\tcontent is null");
                return;
            }
            String mTagName = IElsaManager.EMPTY_PACKAGE;
            String mText = IElsaManager.EMPTY_PACKAGE;
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                StringReader strReader2 = new StringReader(content);
                try {
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        switch (eventType) {
                            case 2:
                                mTagName = parser.getName();
                                eventType = parser.next();
                                mText = parser.getText();
                                if (WifiRomUpdateHelper.this.DEBUG) {
                                    Log.d(WifiRomUpdateHelper.TAG, "\t" + mTagName + ":" + mText);
                                }
                                if (!WifiRomUpdateHelper.NETWORK_MTU_SERVER.equals(mTagName)) {
                                    WifiRomUpdateHelper.this.mKeyValuePair.put(mTagName, mText);
                                    break;
                                } else {
                                    WifiRomUpdateHelper.this.mMtuServer = mText.split(",");
                                    break;
                                }
                            default:
                                break;
                        }
                    }
                    if (strReader2 != null) {
                        strReader2.close();
                    }
                } catch (XmlPullParserException e3) {
                    e = e3;
                    strReader = strReader2;
                } catch (IOException e4) {
                    e2 = e4;
                    strReader = strReader2;
                } catch (Throwable th2) {
                    th = th2;
                    strReader = strReader2;
                }
            } catch (XmlPullParserException e5) {
                e = e5;
                try {
                    WifiRomUpdateHelper.this.log("Got execption parsing permissions.", e);
                    if (strReader != null) {
                        strReader.close();
                    }
                    if (WifiRomUpdateHelper.this.DEBUG) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (strReader != null) {
                        strReader.close();
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e2 = e6;
                WifiRomUpdateHelper.this.log("Got execption parsing permissions.", e2);
                if (strReader != null) {
                    strReader.close();
                }
                if (WifiRomUpdateHelper.this.DEBUG) {
                }
            }
            if (WifiRomUpdateHelper.this.DEBUG) {
                Log.d(WifiRomUpdateHelper.TAG, "\txml file parse end!");
            }
        }
    }

    public WifiRomUpdateHelper(Context context) {
        super(context, FILE_NAME, SYS_FILE_PATH, DATA_FILE_PATH);
        setUpdateInfo(new WifiRomUpdateInfo(), new WifiRomUpdateInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getValue(String key, String defaultVal) {
        String value = (String) this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        return value;
    }

    public Integer getIntegerValue(String key, Integer defaultVal) {
        String value = (String) this.mKeyValuePair.get(key);
        Integer result = defaultVal;
        if (value == null) {
            return defaultVal;
        }
        try {
            result = Integer.valueOf(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            result = defaultVal;
        }
        return result;
    }

    public boolean getBooleanValue(String key, boolean defaultVal) {
        String value = (String) this.mKeyValuePair.get(key);
        Boolean result = Boolean.valueOf(defaultVal);
        if (value == null) {
            return defaultVal;
        }
        try {
            result = Boolean.valueOf(Boolean.parseBoolean(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            result = Boolean.valueOf(defaultVal);
        }
        return result.booleanValue();
    }

    public Double getFloatValue(String key, Double defaultVal) {
        String value = (String) this.mKeyValuePair.get(key);
        Double result = defaultVal;
        if (value == null) {
            return defaultVal;
        }
        try {
            result = Double.valueOf(Double.parseDouble(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            result = defaultVal;
        }
        return result;
    }

    public Long getLongValue(String key, Long defaultVal) {
        String value = (String) this.mKeyValuePair.get(key);
        Long result = defaultVal;
        if (value == null) {
            return defaultVal;
        }
        try {
            result = Long.valueOf(Long.parseLong(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            result = defaultVal;
        }
        return result;
    }

    public void enableVerboseLogging(int level) {
        boolean z = false;
        if (level > 0) {
            z = true;
        }
        this.DEBUG = z;
    }

    public void dump() {
        if (this.DEBUG) {
            Log.d(TAG, "dump:");
        }
        for (String key : this.mKeyValuePair.keySet()) {
            Log.d(TAG, "\t" + key + ":" + ((String) this.mKeyValuePair.get(key)));
        }
    }

    public String[] getMtuServer() {
        return this.mMtuServer;
    }
}
