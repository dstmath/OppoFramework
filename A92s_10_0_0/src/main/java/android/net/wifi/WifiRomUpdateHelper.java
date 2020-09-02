package android.net.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.RomUpdateHelper;
import android.os.SystemProperties;
import android.provider.BrowserContract;
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Xml;
import com.oppo.luckymoney.LMManager;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WifiRomUpdateHelper extends RomUpdateHelper {
    public static final String AVAILABLE_NETWORK_KEYWORD = "AVAILABLE_NETWORK_KEYWORD";
    public static final String BASIC_FOOL_PROOF_ON = "BASIC_FOOL_PROOF_ON";
    public static final String BASIC_SCAN_CMD_DOWN_COUNT = "BASIC_SCAN_CMD_DOWN_COUNT";
    public static final String BASIC_SCAN_REJECT_COUNT = "BASIC_SCAN_REJECT_COUNT";
    public static final String BASIC_WIFI_OPEN_TIME = "BASIC_WIFI_OPEN_TIME";
    public static final String BEGIN_DISABLE_UNECPECT_DISCONNECT_THROTTL = "BEGIN_DISABLE_UNECPECT_DISCONNECT_THROTTL";
    public static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    public static final String CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS = "CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS";
    public static final String CANACCESS_SCANRESULT_WITHOUT_LOCCATIONON = "CANACCESS_SCANRESULT_WITHOUT_LOCCATIONON";
    public static final String CHANGE_TCP_RANDOM_TIMESTAMP = "CHANGE_TCP_RANDOM_TIMESTAMP";
    public static final String CONNECT_DEFAULT_MAX_DHCP_RETRIES = "CONNECT_DEFAULT_MAX_DHCP_RETRIES";
    public static final String CONNECT_DUMPWIFI_WITH_SCREENSHOT = "CONNECT_DUMPWIFI_WITH_SCREENSHOT";
    public static final String CONNECT_ENABLE_REMOVE_NETWORK_WITH_WRONGKEY = "CONNECT_ENABLE_REMOVE_NETWORK_WITH_WRONGKEY";
    public static final String CONNECT_GOOD_RSSI_SWITCH_VALUE = "CONNECT_GOOD_RSSI_SWITCH_VALUE";
    public static final String CONNECT_LINK_FLAPPING_DEBOUNCE_MSEC = "CONNECT_LINK_FLAPPING_DEBOUNCE_MSEC";
    public static final String CONNECT_MAX_RETRIES_MANUAL_ASSOCIATION_REJECT = "CONNECT_MAX_RETRIES_MANUAL_ASSOCIATION_REJECT";
    public static final String CONNECT_MAX_RETRIES_MANUAL_WRONG_KEY_COUNT = "CONNECT_MAX_RETRIES_MANUAL_WRONG_KEY_COUNT";
    public static final String CONNECT_MAX_RETRIES_ON_ASSOCIATION_REJECT = "CONNECT_MAX_RETRIES_ON_ASSOCIATION_REJECT";
    public static final String CONNECT_MAX_RETRIES_ON_AUTHENTICATION_FAILURE = "CONNECT_MAX_RETRIES_ON_AUTHENTICATION_FAILURE";
    public static final String CONNECT_MAX_RETRIES_ON_WRONG_KEY_COUNT = "CONNECT_MAX_RETRIES_ON_WRONG_KEY_COUNT";
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
    public static final String DATA_STALL_DONT_UPLOAD_LOG_REASON = "DATA_STALL_DONT_UPLOAD_LOG_REASON";
    public static final String DATA_STALL_IDLE_SLOT_PERCENT_THRESOLD = "DATA_STALL_IDLE_SLOT_PERCENT_THRESOLD";
    public static final String DATA_STALL_RX_PER_SECOND_ERR_CNT_THRESOLD = "DATA_STALL_RX_PER_SECOND_ERR_CNT_THRESOLD";
    public static final String DATA_STALL_TX_DIVIDE_RX_MULTIPLE_THRESOLD = "DATA_STALL_TX_DIVIDE_RX_MULTIPLE_THRESOLD";
    public static final String DATA_STALL_TX_RETRY_RATIO_THRESOLD = "DATA_STALL_TX_RETRY_RATIO_THRESOLD";
    public static final String DEFAULT_MAC_RANDOMIZATION_SETTING = "DEFAULT_MAC_RANDOMIZATION_SETTING";
    public static final String DHCP_SEND_DISCOVER_PKT_TIME_DELAY = "DHCP_SEND_DISCOVER_PKT_TIME_DELAY";
    public static final String DHCP_SEND_DISCOVER_PTK_ENABLED = "DHCP_SEND_DISCOVER_PTK_ENABLED";
    public static final String DHCP_SEND_DISCOVER_PTK_TIME_THRESHHOD = "DHCP_SEND_DISCOVER_PTK_TIME_THRESHHOD";
    public static final String EVALUATION_BLACKLIST = "EVALUATION_BLACKLIST";
    private static final String FILE_NAME = "sys_wifi_par_config_list";
    public static final String FORBIDDEN_WIFI_DISNETWORK_APP_LIST = "FORBIDDEN_WIFI_DISNETWORK_APP_LIST";
    public static final String FORBIDDEN_WIFI_ENNETWORK_APP_LIST = "FORBIDDEN_WIFI_ENNETWORK_APP_LIST";
    public static final String IGNORE_NETWORK_KEYWORD = "IGNORE_NETWORK_KEYWORD";
    public static final String INVALID_REDIRCT_URL = "INVALID_REDIRCT_URL";
    public static final String LONG_CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS = "LONG_CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS";
    public static final String MAC_RANDOMIZATION_SETTING_PROPERTY = "persist.sys.wifi.mac_randomization";
    public static final String NETWORKS_USE_LOGIN_ACTIVITY = "NETWORKS_USE_LOGIN_ACTIVITY";
    public static final String NETWORKS_USE_OPPOSTACK = "NETWORKS_USE_OPPOSTACK";
    public static final String NETWORK_BACKUP_DNS = "NETWORK_BACKUP_DNS";
    public static final String NETWORK_BACKUP_DNS_EXP = "NETWORK_BACKUP_DNS_EXP";
    public static final String NETWORK_CAPTIVE_SERVER_FIRST_URL = "NETWORK_CAPTIVE_SERVER_FIRST_URL";
    public static final String NETWORK_CAPTIVE_SERVER_SECOND_URL = "NETWORK_CAPTIVE_SERVER_SECOND_URL";
    public static final String NETWORK_CHECK_INTERNET_AFTER_DRIVER_ROAMING = "NETWORK_CHECK_INTERNET_AFTER_DRIVER_ROAMING";
    public static final String NETWORK_CHECK_INTERNET_FOR_DATA_STALL = "NETWORK_CHECK_INTERNET_FOR_DATA_STALL";
    public static final String NETWORK_COLLECT_CAPTIVERESULT = "NETWORK_COLLECT_CAPTIVERESULT";
    public static final String NETWORK_CRL_READ_TIMEOUT = "NETWORK_CRL_READ_TIMEOUT";
    public static final String NETWORK_DEFAULT_DNS = "NETWORK_DEFAULT_DNS";
    public static final String NETWORK_DETECT_IP_CONFLICT = "NETWORK_DETECT_IP_CONFLICT";
    public static final String NETWORK_DNS_QUERY_RETRY_COUNT = "NETWORK_DNS_QUERY_RETRY_COUNT";
    public static final String NETWORK_DNS_QUERY_SERVER = "NETWORK_DNS_QUERY_SERVER";
    public static final String NETWORK_DNS_QUERY_SERVER_EXP = "NETWORK_DNS_QUERY_SERVER_EXP";
    public static final String NETWORK_DNS_QUERY_TIMEOUT = "NETWORK_DNS_QUERY_TIMEOUT";
    public static final String NETWORK_DNS_RECOVERY_ENGINE_ENABLE = "NETWORK_DNS_RECOVERY_ENGINE_ENABLE";
    public static final String NETWORK_DOWNLOAD_APPS = "NETWORK_DOWNLOAD_APPS";
    public static final String NETWORK_DUAL_STA_APPS = "NETWORK_DUAL_STA_APPS";
    public static final String NETWORK_DUAL_STA_APPS_EXP = "NETWORK_DUAL_STA_APPS_EXP";
    public static final String NETWORK_DUAL_STA_BLACK_LIST = "NETWORK_DUAL_STA_BLACK_LIST";
    public static final String NETWORK_DUAL_STA_CAP_HOST_BLACK_LIST = "NETWORK_DUAL_STA_CAP_HOST_BLACK_LIST";
    public static final String NETWORK_DUAL_STA_ENABLED = "NETWORK_DUAL_STA_ENABLED";
    public static final String NETWORK_EXP_CAPTIVE_SERVER_HTTPS_URL = "NETWORK_EXP_CAPTIVE_SERVER_HTTPS_URL";
    public static final String NETWORK_EXP_CAPTIVE_SERVER_HTTP_URL = "NETWORK_EXP_CAPTIVE_SERVER_HTTP_URL";
    public static final String NETWORK_EXP_DEFAULT_DNS = "NETWORK_EXP_DEFAULT_DNS";
    public static final String NETWORK_FALLBACK_HTTP_SERVERS_URL = "NETWORK_FALLBACK_HTTP_SERVERS_URL";
    public static final String NETWORK_GAME_APP = "NETWORK_GAME_APP";
    public static final String NETWORK_HANDLE_GATEWAY_CONFLICT = "NETWORK_HANDLE_GATEWAY_CONFLICT";
    public static final String NETWORK_INVALID_NS_THRESHOLD = "NETWORK_INVALID_NS_THRESHOLD";
    public static final String NETWORK_IPV6_RETRIES = "NETWORK_IPV6_RETRIES";
    public static final String NETWORK_IPV6_SIMEPLE_QUERY = "NETWORK_IPV6_SIMEPLE_QUERY";
    public static final String NETWORK_IPV6_TIMEOUT = "NETWORK_IPV6_TIMEOUT";
    public static final String NETWORK_LOCATION_APP = "NETWORK_LOCATION_APP";
    public static final String NETWORK_LONG_CACHE_APP = "NETWORK_LONG_CACHE_APP";
    public static final String NETWORK_MTU = "NETWORK_MTU";
    public static final String NETWORK_MTU_SERVER = "NETWORK_MTU_SERVER";
    public static final String NETWORK_MTU_SERVER_EXP = "NETWORK_MTU_SERVER_EXP";
    public static final String NETWORK_NAVIGATION_APP = "NETWORK_NAVIGATION_APP";
    public static final String NETWORK_PUBLIC_HTTPS_SERVERS_URL = "NETWORK_PUBLIC_HTTPS_SERVERS_URL";
    public static final String NETWORK_REMOVE_APP = "NETWORK_REMOVE_APP";
    public static final String NETWORK_SKIP_DESTROY_SOCKET_APPS = "NETWORK_SKIP_DESTROY_SOCKET_APPS";
    public static final String NETWORK_SLA_APPS = "NETWORK_SLA_APPS";
    public static final String NETWORK_SLA_APPS_DEFAULT_STATE = "NETWORK_SLA_APPS_DEFAULT_STATE";
    public static final String NETWORK_SLA_APPS_EXP = "NETWORK_SLA_APPS_EXP";
    public static final String NETWORK_SLA_AUTO_ENABLE_THRESHOLD = "NETWORK_SLA_AUTO_ENABLE_THRESHOLD";
    public static final String NETWORK_SLA_BLACK_LIST = "NETWORK_SLA_BLACK_LIST";
    public static final String NETWORK_SLA_CELL_USAGE_THRESHOLD = "NETWORK_SLA_CELL_USAGE_THRESHOLD";
    public static final String NETWORK_SLA_ENABLED = "NETWORK_SLA_ENABLED";
    public static final String NETWORK_SLA_ENABLED_MCC = "NETWORK_SLA_ENABLED_MCC";
    public static final String NETWORK_SLA_GAME_APPS = "NETWORK_SLA_GAME_APPS";
    public static final String NETWORK_SLA_GAME_APPS_EXP = "NETWORK_SLA_GAME_APPS_EXP";
    public static final String NETWORK_SLA_GAME_PARAMS = "NETWORK_SLA_GAME_PARAMS";
    public static final String NETWORK_SLA_HONOR_OIFACE_STATE = "NETWORK_SLA_HONOR_OIFACE_STATE";
    public static final String NETWORK_SLA_PARAMS = "NETWORK_SLA_PARAMS";
    public static final String NETWORK_SPECIAL_REDIRECT_URL = "NETWORK_SPECIAL_REDIRECT_URL";
    public static final String NETWORK_SPEED_RTT_PARAMS = "NETWORK_SPEED_RTT_PARAMS";
    public static final String NETWORK_SYSTEM_APP = "NETWORK_SYSTEM_APP";
    public static final String NETWORK_TCP_TS_ERROR_THRESHOLD = "NETWORK_TCP_TS_ERROR_THRESHOLD";
    public static final String NETWORK_UNEXPECTED_IO_MSG = "NETWORK_UNEXPECTED_IO_MSG";
    public static final String NETWORK_VIDEO_APPS = "NETWORK_VIDEO_APPS";
    public static final String NETWORK_WECHAT_LM_PARAMS = "NETWORK_WECHAT_LM_PARAMS";
    public static final String NETWORK_WIFI_APP = "NETWORK_WIFI_APP";
    public static final String NETWORK_WIFI_APP_BLACKLIST = "NETWORK_WIFI_APP_BLACKLIST";
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
    public static final String OPPO_AUTO_CONNECT_CONTINUOUS_DISABLED_COUNT_MAX = "OPPO_AUTO_CONNECT_CONTINUOUS_DISABLED_COUNT_MAX";
    public static final String OPPO_AUTO_CONNECT_DHCP_FAILURE_EXP_BASE = "OPPO_AUTO_CONNECT_DHCP_FAILURE_EXP_BASE";
    public static final String OPPO_AUTO_CONNECT_DHCP_FAILURE_MULTIPLE_BASE = "OPPO_AUTO_CONNECT_DHCP_FAILURE_MULTIPLE_BASE";
    public static final String OPPO_AUTO_CONNECT_DHCP_FAILURE_RETRY_TIME_MIN = "OPPO_AUTO_CONNECT_DHCP_FAILURE_RETRY_TIME_MIN";
    public static final String OPPO_AUTO_CONNECT_DHCP_FAILURE_STEP_COUNT = "OPPO_AUTO_CONNECT_DHCP_FAILURE_STEP_COUNT";
    public static final String OPPO_AUTO_CONNECT_ENABLE_BOUYGUE_AUTOCONNECT = "OPPO_AUTO_CONNECT_ENABLE_BOUYGUE_AUTOCONNECT";
    public static final String OPPO_AUTO_CONNECT_ENABLE_OPERATOR_AUTOCONNECT = "OPPO_AUTO_CONNECT_ENABLE_OPERATOR_AUTOCONNECT";
    public static final String OPPO_AUTO_CONNECT_ENABLE_SINGTEL_AUTOCONNECT = "OPPO_AUTO_CONNECT_ENABLE_SINGTEL_AUTOCONNECT";
    public static final String OPPO_AUTO_CONNECT_ENABLE_SWISSCOM_AUTOCONNECT = "OPPO_AUTO_CONNECT_ENABLE_SWISSCOM_AUTOCONNECT";
    public static final String OPPO_AUTO_CONNECT_KEEP_DISABLED_TIME_MAX = "OPPO_AUTO_CONNECT_KEEP_DISABLED_TIME_MAX";
    public static final String OPPO_AUTO_CONNECT_NO_INTERNET_STEP_COUNT = "OPPO_AUTO_CONNECT_NO_INTERNET_STEP_COUNT";
    public static final String OPPO_AUTO_CONNECT_NO_INTERNET_STEP_TIME = "OPPO_AUTO_CONNECT_NO_INTERNET_STEP_TIME";
    public static final String OPPO_AUTO_CONNECT_OPERATOR_AP = "OPPO_AUTO_CONNECT_OPERATOR_AP";
    public static final String OPPO_AUTO_CONNECT_RSSI_BAD = "OPPO_AUTO_CONNECT_RSSI_BAD";
    public static final String OPPO_AUTO_CONNECT_RSSI_GOOD = "OPPO_AUTO_CONNECT_RSSI_GOOD";
    public static final String OPPO_AUTO_CONNECT_RSSI_LOW = "OPPO_AUTO_CONNECT_RSSI_LOW";
    public static final String OPPO_AUTO_CONNECT_RSSI_STEP = "OPPO_AUTO_CONNECT_RSSI_STEP";
    public static final String OPPO_AUTO_CONNECT_WRONG_KEY_EXP_BASE = "OPPO_AUTO_CONNECT_WRONG_KEY_EXP_BASE";
    public static final String OPPO_AUTO_CONNECT_WRONG_KEY_MULTIPLE_BASE = "OPPO_AUTO_CONNECT_WRONG_KEY_MULTIPLE_BASE";
    public static final String OPPO_AUTO_CONNECT_WRONG_KEY_RETRY_TIME_MIN = "OPPO_AUTO_CONNECT_WRONG_KEY_RETRY_TIME_MIN";
    public static final String OPPO_AUTO_CONNECT_WRONG_KEY_STEP_COUNT = "OPPO_AUTO_CONNECT_WRONG_KEY_STEP_COUNT";
    public static final String OPPO_BASIC_SCAN_AND_SOFTAP_CONCURRENCY_ENABLED = "OPPO_BASIC_SCAN_AND_SOFTAP_CONCURRENCY_ENABLED";
    public static final String OPPO_BASIC_SCAN_AND_SOFTAP_CONCURRENCY_LOCATION_PKG = "OPPO_BASIC_SCAN_AND_SOFTAP_CONCURRENCY_LOCATION_PKG";
    public static final String OPPO_BASIC_WIFI_COLLECT_WIFI_COREDUMP = "OPPO_BASIC_WIFI_COLLECT_WIFI_COREDUMP";
    public static final String OPPO_BASIC_WIFI_CUSTOM_P2P_BADN_LIMIT = "OPPO_BASIC_WIFI_CUSTOM_P2P_BADN_LIMIT";
    public static final String OPPO_BASIC_WIFI_CUSTOM_P2P_BAND4_COUNTEY_LISIT = "OPPO_BASIC_WIFI_CUSTOM_P2P_BAND4_COUNTEY_LISIT";
    public static final String OPPO_BASIC_WIFI_CUSTOM_P2P_ONLY2G_BADN_LIMIT = "OPPO_BASIC_WIFI_CUSTOM_P2P_ONLY2G_BADN_LIMIT";
    public static final String OPPO_BASIC_WIFI_CUSTOM_P2P_ONLY2G_COUNTEY_LISIT = "OPPO_BASIC_WIFI_CUSTOM_P2P_ONLY2G_COUNTEY_LISIT";
    public static final String OPPO_BASIC_WIFI_CUSTOM_SOFTAP_BAND4_COUNTEY_LISIT = "OPPO_BASIC_WIFI_CUSTOM_SOFTAP_BAND4_COUNTEY_LISIT";
    public static final String OPPO_BASIC_WIFI_CUSTOM_SOFTAP_ONLY2G_BADN_LIMIT = "OPPO_BASIC_WIFI_CUSTOM_SOFTAP_ONLY2G_BADN_LIMIT";
    public static final String OPPO_BASIC_WIFI_CUSTOM_SOFTAP_ONLY2G_COUNTEY_LISIT = "OPPO_BASIC_WIFI_CUSTOM_SOFTAP_ONLY2G_COUNTEY_LISIT";
    public static final String OPPO_BASIC_WIFI_CUSTOM_WCN_SAU = "OPPO_BASIC_WIFI_CUSTOM_WCN_SAU";
    public static final String OPPO_BASIC_WIFI_CUSTOM_WCN_SAU_MTK = "OPPO_BASIC_WIFI_CUSTOM_WCN_SAU_MTK";
    public static final String OPPO_BASIC_WIFI_CUSTOM_WCN_SAU_QCOM = "OPPO_BASIC_WIFI_CUSTOM_WCN_SAU_QCOM";
    public static final String OPPO_BASIC_WIFI_FRAMEWORK_CHECK_WIFI_SWITCH = "OPPO_BASIC_WIFI_FRAMEWORK_CHECK_WIFI_SWITCH";
    public static final String OPPO_BASIC_WIFI_P2P_OSHARE_LOG_COLLECT_ENBALE = "OPPO_BASIC_WIFI_P2P_OSHARE_LOG_COLLECT_ENBALE";
    public static final String OPPO_BASIC_WIFI_P2P_WFD_LOG_COLLECT_ENBALE = "OPPO_BASIC_WIFI_P2P_WFD_LOG_COLLECT_ENBALE";
    public static final String OPPO_BASIC_WIFI_SWITCH_LOG_CELLULAR_ENABLED = "OPPO_BASIC_WIFI_SWITCH_LOG_CELLULAR_ENABLED";
    public static final String OPPO_BASIC_WIFI_SWITCH_LOG_COLLECT_ENABLED = "OPPO_BASIC_WIFI_SWITCH_LOG_COLLECT_ENABLED";
    public static final String OPPO_BASIC_WIFI_SWITCH_LOG_LIMIT = "OPPO_BASIC_WIFI_SWITCH_LOG_LIMIT";
    public static final String OPPO_BASIC_WIFI_SWITCH_LOG_STAGE = "OPPO_BASIC_WIFI_SWITCH_LOG_STAGE";
    public static final String OPPO_BASIC_WIFI_SWITCH_RETRY_LIMIT = "OPPO_BASIC_WIFI_SWITCH_RETRY_LIMIT";
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    public static final String OPPO_DHCP_HOST_NAME = "OPPO_DHCP_HOST_NAME";
    public static final String OPPO_DHCP_IP_RECOVERY_ENABLED = "OPPO_DHCP_IP_RECOVERY_ENABLED";
    public static final String OPPO_DHCP_OPTIONAL_ON = "OPPO_DHCP_OPTIONAL_ON";
    public static final String OPPO_DHCP_PARAMETER_LIST = "OPPO_DHCP_PARAMETER_LIST";
    public static final String OPPO_DHCP_VENDOR_CLASS_ID = "OPPO_DHCP_VENDOR_CLASS_ID";
    public static final String OPPO_DUAL_STA_CONNECT_CAPTIVE_AP = "OPPO_DUAL_STA_CONNECT_CAPTIVE_AP";
    public static final String OPPO_DUAL_STA_DISABLED_MCC = "OPPO_DUAL_STA_DISABLED_MCC";
    public static final String OPPO_DUAL_STA_DISABLE_DURING = "OPPO_DUAL_STA_DISABLE_DURING";
    public static final String OPPO_DUAL_STA_DISCONNECT_FREQ_THROTTLED = "OPPO_DUAL_STA_DISCONNECT_FREQ_THROTTLED";
    public static final String OPPO_DUAL_STA_DUP_NOTIFICATION_PERIOD = "OPPO_DUAL_STA_DUP_NOTIFICATION_PERIOD";
    public static final String OPPO_DUAL_STA_LOW_RSSI = "OPPO_DUAL_STA_LOW_RSSI";
    public static final String OPPO_DUAL_STA_MAX_SPEED = "OPPO_DUAL_STA_MAX_SPEED";
    public static final String OPPO_DUAL_STA_NET_GOOD = "OPPO_DUAL_STA_NET_GOOD";
    public static final String OPPO_DUAL_STA_NOTIFICATION_SHOW_DELAY = "OPPO_DUAL_STA_NOTIFICATION_SHOW_DELAY";
    public static final String OPPO_DUAL_STA_NOTIFICATION_SHOW_DELAY_MS = "OPPO_DUAL_STA_NOTIFICATION_SHOW_DELAY_MS";
    public static final String OPPO_DUAL_STA_RESTRICT_DUP_NOTIFICATION = "OPPO_DUAL_STA_RESTRICT_DUP_NOTIFICATION";
    public static final String OPPO_DUAL_STA_TRIGGER_DISABLE_COUNT = "OPPO_DUAL_STA_TRIGGER_DISABLE_COUNT";
    public static final String OPPO_FEW_AP_NUM = "OPPO_FEW_AP_NUM";
    public static final String OPPO_LIMIT_SPEED_FEATURE = "OPPO_SPEED_LIMIT_FEATURE";
    public static final String OPPO_LIMIT_SPEED_MONITOR_RSSI_COUNTOUR = "OPPO_LIMIT_SPEED_MONITOR_RSSI_COUNTOUR";
    public static final String OPPO_LIMIT_SPEED_MONITOR_RSSI_THRESHOLD = "OPPO_LIMIT_SPEED_MONITOR_RSSI_THRESHOLD";
    public static final String OPPO_LIMIT_SPEED_STATISTIC_ENABLE = "OPPO_LIMIT_SPEED_STATISTIC_ENABLE";
    public static final String OPPO_LIMIT_SPEED_WHITE_APPS = "NETWORK_LIMIT_SPEED_WHITE_APPS";
    public static final String OPPO_NETWORKMONITOR_STATISTIC_CELLULAR = "OPPO_NETWORKMONITOR_STATISTIC_CELLULAR";
    public static final String OPPO_NETWORKMONITOR_STATISTIC_WIFI = "OPPO_NETWORKMONITOR_STATISTIC_WIFI";
    public static final String OPPO_NETWORK_DUP_DHCP_CHECK = "OPPO_NETWORK_DUP_DHCP_CHECK";
    public static final String OPPO_PORTAL_DETECT = "OPPO_PORTAL_DETECT";
    public static final String OPPO_PRIVATE_DNS_SHOW_DIALOG = "OPPO_PRIVATE_DNS_SHOW_DIALOG";
    public static final String OPPO_SLA_SET_DEBUG = "OPPO_SLA_SET_DEBUG";
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
    public static final String OPPO_WIFI_ASSISTANT_CONTROL_APP_LIST = "OPPO_WIFI_ASSISTANT_CONTROL_APP_LIST";
    public static final String OPPO_WIFI_ASSISTANT_CONTROL_SOFTAP = "OPPO_WIFI_ASSISTANT_CONTROL_SOFTAP";
    public static final String OPPO_WIFI_ASSISTANT_DETECT_CONNECT = "OPPO_WIFI_ASSISTANT_DETECT_CONNECT";
    public static final String OPPO_WIFI_ASSISTANT_DIFF_CONNRATE_THRESHOLD = "OPPO_WIFI_ASSISTANT_DIFF_CONNRATE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_DIFF_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_DIFF_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_ENABLE_CHANGE_LEVEL = "OPPO_WIFI_ASSISTANT_ENABLE_CHANGE_LEVEL";
    public static final String OPPO_WIFI_ASSISTANT_FEATURE = "OPPO_WIFI_ASSISTANT_FEATURE";
    public static final String OPPO_WIFI_ASSISTANT_FOUR_VERSION_ENABLE = "OPPO_WIFI_ASSISTANT_FOUR_VERSION_ENABLE";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT = "OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_LINK_LOSS_THRESHOLD = "OPPO_WIFI_ASSISTANT_GOOD_LINK_LOSS_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL = "OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_LINK_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_GOOD_LINK_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_RSSI_24 = "OPPO_WIFI_ASSISTANT_GOOD_RSSI_24";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_RSSI_5 = "OPPO_WIFI_ASSISTANT_GOOD_RSSI_5";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_GOOD_TCP_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_GOOD_TCP_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_HISTORY_RECORD_TRIGGER_THRESHOLD = "OPPO_WIFI_ASSISTANT_HISTORY_RECORD_TRIGGER_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_HISTORY_RECORD_VALID_THRESHOLD = "OPPO_WIFI_ASSISTANT_HISTORY_RECORD_VALID_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_INVALID_TRAFFIC_SAMPL_INTERVAL = "OPPO_WIFI_ASSISTANT_INVALID_TRAFFIC_SAMPL_INTERVAL";
    public static final String OPPO_WIFI_ASSISTANT_LOW_LINK_LOSS_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_LINK_LOSS_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_LOW_LINK_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_LINK_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_LOW_RSSI_24 = "OPPO_WIFI_ASSISTANT_LOW_RSSI_24";
    public static final String OPPO_WIFI_ASSISTANT_LOW_RSSI_5 = "OPPO_WIFI_ASSISTANT_LOW_RSSI_5";
    public static final String OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_LOW_TCP_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_TCP_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_LOW_TRAFFICE_THRESHOLD = "OPPO_WIFI_ASSISTANT_LOW_TRAFFICE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_MANUAL_DIALOG_TIME = "OPPO_WIFI_ASSISTANT_MANUAL_DIALOG_TIME";
    public static final String OPPO_WIFI_ASSISTANT_NETINVALID_COUNT = "OPPO_WIFI_ASSISTANT_NETINVALID_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_NETSERVER = "OPPO_WIFI_ASSISTANT_NETSERVER";
    public static final String OPPO_WIFI_ASSISTANT_NO_LINK_SCORE_THRESHOLD = "OPPO_WIFI_ASSISTANT_NO_LINK_SCORE_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_PORTAL_AUTO_DETECT_COUNT = "OPPO_WIFI_ASSISTANT_PORTAL_AUTO_DETECT_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_PORTAL_MANUL_DETECT_COUNT = "OPPO_WIFI_ASSISTANT_PORTAL_MANUL_DETECT_COUNT";
    public static final String OPPO_WIFI_ASSISTANT_RIGHT_ROTATION = "OPPO_WIFI_ASSISTANT_RIGHT_ROTATION";
    public static final String OPPO_WIFI_ASSISTANT_ROAM_DETECT = "OPPO_WIFI_ASSISTANT_ROAM_DETECT";
    public static final String OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_24 = "OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_24";
    public static final String OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_5 = "OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_5";
    public static final String OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD = "OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_TEST = "OPPO_WIFI_ASSISTANT_TEST";
    public static final String OPPO_WIFI_ASSISTANT_VALID_LINK_LOSS_NUM = "OPPO_WIFI_ASSISTANT_VALID_LINK_LOSS_NUM";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_POLL_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_POLL_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_SCORE_GOOD = "OPPO_WIFI_ASSISTANT_WLAN_SCORE_GOOD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_SCORE_POOR = "OPPO_WIFI_ASSISTANT_WLAN_SCORE_POOR";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_DATA_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_DATA_THRESHOLD";
    public static final String OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD = "OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD";
    public static final String OPPO_WIFI_AUTO_CONNECT_PORTAL_DETECT_COUNT = "OPPO_WIFI_AUTO_CONNECT_PORTAL_DETECT_COUNT";
    public static final String OPPO_WIFI_AUTO_CONNECT_PORTAL_FEATURE = "OPPO_WIFI_AUTO_CONNECT_PORTAL_FEATURE";
    public static final String OPPO_WIFI_AUTO_CONNECT_PORTAL_STATIC = "OPPO_WIFI_AUTO_CONNECT_PORTAL_STATIC";
    public static final String OPPO_WIFI_ROM_UPDATE_CHANGED_ACTION = "oppo.intent.action.WIFI_ROM_UPDATE_CHANGED";
    public static final String OPPO_WIFI_SMARTGEAR_FEATURE = "OPPO_WIFI_SMARTGEAR_FEATURE";
    public static final String PORTAL_RESPONSE_CODE_HIGH = "PORTAL_RESPONSE_CODE_HIGH";
    public static final String PORTAL_RESPONSE_CODE_LOW = "PORTAL_RESPONSE_CODE_LOW";
    public static final String POWER_APP_DETECT_AND_KILL = "POWER_APP_DETECT_AND_KILL";
    public static final String POWER_APP_SCAN_COUNT = "POWER_APP_SCAN_COUNT";
    public static final String POWER_APP_SCAN_FREQ = "POWER_APP_SCAN_FREQ";
    public static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    public static final String SCAN_REQUEST_THROTTLE_MAX_IN_TIME_WINDOW = "SCAN_REQUEST_THROTTLE_MAX_IN_TIME_WINDOW";
    public static final String SCAN_REQUEST_THROTTLE_TIME_WINDOW = "SCAN_REQUEST_THROTTLE_TIME_WINDOW";
    private static final String SMART_BW_PARAMS = "SMART_BW_PARAMS";
    public static final String SYSTEMSCAN_BLACK_LIST = "SYSTEMSCAN_BLACK_LIST";
    private static final String SYS_FILE_PATH = "/system/etc/sys_wifi_par_config_list.xml";
    private static final String TAG = "WifiRomUpdateHelper";
    public static final String TRIGGER_DISABLE_UNEXPECT_DISCONNECT_THROTTL = "TRIGGER_DISABLE_UNEXPECT_DISCONNECT_THROTTL";
    public static final String UNEXPECTED_DISCONNECT_DISABLE_REASON = "UNEXPECTED_DISCONNECT_DISABLE_REASON";
    public static final String WEBVIEW_AVAILABLE_HEADER = "WEBVIEW_AVAILABLE_HEADER";
    public static final String WEBVIEW_BAIDU_BOX_HEADER = "WEBVIEW_BAIDU_BOX_HEADER";
    public static final String WEBVIEW_URL = "WEBVIEW_URL";
    public static final String WIFIFRE_DISCONNECT_THOTTL = "WIFIFRE_DISCONNECT_THOTTL";
    public static final String WIFILOCK_HIGH_PERFORMANCE_BLACKLIST = "WIFILOCK_HIGH_PERFORMANCE_BLACKLIST";
    public static final String WIFILOCK_LOW_LATENCY_BLACKLIST = "WIFILOCK_LOW_LATENCY_BLACKLIST";
    private static WifiRomUpdateHelper sInstance;
    /* access modifiers changed from: private */
    public boolean DEBUG = false;
    /* access modifiers changed from: private */
    public String[] mDownloadApps = null;
    /* access modifiers changed from: private */
    public String[] mDualStaApps = {"com.heytap.browser", BrowserContract.AUTHORITY, "com.coloros.browser", "com.UCMobile", LMManager.MM_PACKAGENAME, LMManager.QQ_PACKAGENAME, "com.sina.weibo", "com.netease.newsreader.activity", "com.ss.android.article.news", "com.jingdong.app.mall", "com.taobao.taobao", "com.tmall.wireless", "com.achievo.vipshop", "com.xunmeng.pinduoduo", "com.baidu.tieba", "com.qzone", "com.zhihu.android", "com.xingin.xhs", "com.baidu.browser.apps", "com.tencent.mtt", "com.eg.android.AlipayGphone", "me.ele", "com.sankuai.meituan", "com.sankuai.meituan.takeoutnew", "com.dianping.v1", "com.moji.mjweather", "ctrip.android.view", "com.Qunar", "com.tencent.news", "com.tencent.reading", "com.tencent.qqlive", "com.youku.phone", "com.qiyi.video", "com.sohu.sohuvideo", "com.tencent.android.qqdownloader", "com.oppo.market", "com.nearme.gamecenter", "com.xunlei.downloadprovider", "tv.danmaku.bili", "com.ss.android.ugc.aweme", "com.smile.gifmaker", "air.tv.douyu.android", "com.ss.android.ugc.live", "com.hunantv.imgo.activity", "com.ss.android.article.video", "com.duowan.kiwi", "com.netease.cloudmusic", "com.kugou.android", "com.tencent.qqmusic"};
    /* access modifiers changed from: private */
    public String[] mDualStaAppsExp = {"com.whatsapp", "in.mohalla.sharechat", "app.buzz.share", "com.facebook.orca", "com.UCMobile.intl", "com.mcent.browser", "com.redefine.welike", "com.instagram.android", "com.heytap.browser", BrowserContract.AUTHORITY, "com.coloros.browser", "com.android.chrome", "com.facebook.katana", "org.mozilla.firefox", "com.opera.browser"};
    /* access modifiers changed from: private */
    public String[] mDualStaBlackList = null;
    /* access modifiers changed from: private */
    public String[] mDualStaCapHostBlackList = null;
    /* access modifiers changed from: private */
    public String[] mDualStaDisableMcc = {"200-299"};
    /* access modifiers changed from: private */
    public HashMap<String, String> mKeyValuePair = new HashMap<>();
    /* access modifiers changed from: private */
    public String[] mLmParams = {"1350#1200#0#4#17f10304", "1360#1250#0#4#17f10305", "1500#1250#0#4#17f10306"};
    private String[] mMtuServer = {"conn1.oppomobile.com", "conn2.oppomobile.com", "www.baidu.com", "www.jd.com", "www.taobao.com", "www.qq.com"};
    /* access modifiers changed from: private */
    public String[] mSkipDestroySocketApps = {LMManager.MM_PACKAGENAME, LMManager.QQ_PACKAGENAME};
    /* access modifiers changed from: private */
    public String[] mSlaApps = {"com.heytap.browser", BrowserContract.AUTHORITY, "com.coloros.browser", "com.UCMobile", LMManager.MM_PACKAGENAME, LMManager.QQ_PACKAGENAME, "com.sina.weibo", "com.netease.newsreader.activity", "com.ss.android.article.news", "com.jingdong.app.mall", "com.taobao.taobao", "com.tmall.wireless", "com.achievo.vipshop", "com.xunmeng.pinduoduo", "com.baidu.tieba", "com.qzone", "com.zhihu.android", "com.xingin.xhs", "com.baidu.browser.apps", "com.tencent.mtt", "com.eg.android.AlipayGphone", "me.ele,com.sankuai.meituan", "com.sankuai.meituan.takeoutnew", "com.dianping.v1", "com.moji.mjweather", "ctrip.android.view", "com.Qunar", "com.tencent.news", "com.tencent.reading"};
    /* access modifiers changed from: private */
    public String[] mSlaAppsExp = {"com.whatsapp", "in.mohalla.sharechat", "app.buzz.share", "com.facebook.orca", "com.UCMobile.intl", "com.mcent.browser", "com.redefine.welike", "com.instagram.android", BrowserContract.AUTHORITY, "com.coloros.browser", "com.android.chrome", "com.facebook.katana", "org.mozilla.firefox", "com.opera.browser", "com.heytap.browser"};
    /* access modifiers changed from: private */
    public String[] mSlaBlackList = null;
    /* access modifiers changed from: private */
    public String[] mSlaEnabledMCC = {"460", "404-405-406"};
    /* access modifiers changed from: private */
    public String[] mSlaGameApps = {"not.defined", "com.tencent.tmgp.sgame", "com.tencent.tmgp.pubgmhd"};
    /* access modifiers changed from: private */
    public String[] mSlaGameAppsExp = {"not.defined", "not.defined", "com.tencent.ig"};
    /* access modifiers changed from: private */
    public String[] mSlaGameParams = {"4#8#0000000100010003", "4#8#000003e900040003", "5#5#0864100118", "5#5#0865100018"};
    /* access modifiers changed from: private */
    public String[] mSlaParams = {"200", "500", "1000", "500", "230", "200", "220", "55", "75", "2000", "2000", "200", "55"};
    private int[] mSmartBWParams = {1, 0, 1, 60, 30, 25, -75, 10, 15, 5, 3, 5, 20, 6, 135, 20, 15, 200};
    private int[] mSpeedRttParams = {150, 100, 250, 200, 150, 5, 10, 15, 10, 5, 5};
    /* access modifiers changed from: private */
    public String[] mVideoApps = null;

    public int[] getSmartBWParams() {
        return this.mSmartBWParams;
    }

    /* access modifiers changed from: private */
    public void setSmartBWParams(String text) {
        String[] params;
        if (text != null && (params = text.split(SmsManager.REGEX_PREFIX_DELIMITER)) != null && params.length == this.mSmartBWParams.length) {
            try {
                int SELECT_BW_WHEN_CONNECT_FEATRUE_ENABLE = Integer.parseInt(params[0]);
                int DYNAMIC_SW_BW_FEATRUE_ENABLE = Integer.parseInt(params[1]);
                int DBG = Integer.parseInt(params[2]);
                int SCAN_RESULT_EXPIRE = Integer.parseInt(params[3]);
                int SAVE_CONNECT_LOG_THRESHOLD = Integer.parseInt(params[4]);
                int CLEAN_AP_NUMBER = Integer.parseInt(params[5]);
                int GOOD_RSSI = Integer.parseInt(params[6]);
                int CLEAN_CH_IDLE_SLOT_RATIO = Integer.parseInt(params[7]);
                int CLEAN_BAND_IDLE_SLOT_RATIO = Integer.parseInt(params[8]);
                Integer.parseInt(params[9]);
                this.mSmartBWParams = new int[]{SELECT_BW_WHEN_CONNECT_FEATRUE_ENABLE, DYNAMIC_SW_BW_FEATRUE_ENABLE, DBG, SCAN_RESULT_EXPIRE, SAVE_CONNECT_LOG_THRESHOLD, CLEAN_AP_NUMBER, GOOD_RSSI, CLEAN_CH_IDLE_SLOT_RATIO, CLEAN_BAND_IDLE_SLOT_RATIO, 5, Integer.parseInt(params[10]), Integer.parseInt(params[11]), Integer.parseInt(params[12]), Integer.parseInt(params[13]), Integer.parseInt(params[14]), Integer.parseInt(params[15]), Integer.parseInt(params[16]), Integer.parseInt(params[17])};
                if (DBG > 0) {
                    Log.d(TAG, "setSmartBWParams parse params:" + text);
                }
            } catch (Exception e) {
                Log.e(TAG, "setSmartBWParams failed to parse params:" + text);
                this.mSmartBWParams = null;
            }
        }
    }

    public class WifiRomUpdateInfo extends RomUpdateHelper.UpdateInfo {
        @Override // android.net.wifi.RomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ void clear() {
            super.clear();
        }

        @Override // android.net.wifi.RomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ boolean clone(RomUpdateHelper.UpdateInfo updateInfo) {
            return super.clone(updateInfo);
        }

        @Override // android.net.wifi.RomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ void dump() {
            super.dump();
        }

        @Override // android.net.wifi.RomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ long getVersion() {
            return super.getVersion();
        }

        @Override // android.net.wifi.RomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ boolean insert(int i, String str) {
            return super.insert(i, str);
        }

        @Override // android.net.wifi.RomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ boolean updateToLowerVersion(String str) {
            return super.updateToLowerVersion(str);
        }

        public WifiRomUpdateInfo() {
            super();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:90:0x0201, code lost:
            if (r4 == null) goto L_0x0204;
         */
        @Override // android.net.wifi.RomUpdateHelper.UpdateInfo
        public void parseContentFromXML(String content) {
            if (WifiRomUpdateHelper.this.DEBUG) {
                Log.d(WifiRomUpdateHelper.TAG, "parseContentFromXML");
            }
            if (content == null) {
                Log.d(WifiRomUpdateHelper.TAG, "\tcontent is null");
                return;
            }
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                strReader = new StringReader(content);
                parser.setInput(strReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0) {
                        if (eventType == 2) {
                            String mTagName = parser.getName();
                            parser.next();
                            String mText = parser.getText();
                            if (WifiRomUpdateHelper.this.DEBUG) {
                                Log.d(WifiRomUpdateHelper.TAG, "\t" + mTagName + SettingsStringUtil.DELIMITER + mText);
                            }
                            if ("NETWORK_SLA_APPS".equals(mTagName)) {
                                String[] unused = WifiRomUpdateHelper.this.mSlaApps = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_SLA_APPS_EXP".equals(mTagName)) {
                                String[] unused2 = WifiRomUpdateHelper.this.mSlaAppsExp = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_SLA_BLACK_LIST".equals(mTagName)) {
                                String[] unused3 = WifiRomUpdateHelper.this.mSlaBlackList = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_SLA_GAME_APPS".equals(mTagName)) {
                                String[] unused4 = WifiRomUpdateHelper.this.mSlaGameApps = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_SLA_GAME_APPS_EXP".equals(mTagName)) {
                                String[] unused5 = WifiRomUpdateHelper.this.mSlaGameAppsExp = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_SLA_ENABLED_MCC".equals(mTagName)) {
                                String[] unused6 = WifiRomUpdateHelper.this.mSlaEnabledMCC = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("OPPO_DUAL_STA_DISABLED_MCC".equals(mTagName)) {
                                String[] unused7 = WifiRomUpdateHelper.this.mDualStaDisableMcc = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_SLA_PARAMS".equals(mTagName)) {
                                String[] unused8 = WifiRomUpdateHelper.this.mSlaParams = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_SPEED_RTT_PARAMS".equals(mTagName)) {
                                WifiRomUpdateHelper.this.setSpeedRttParams(mText);
                            } else if ("NETWORK_SLA_GAME_PARAMS".equals(mTagName)) {
                                String[] unused9 = WifiRomUpdateHelper.this.mSlaGameParams = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_DUAL_STA_APPS".equals(mTagName)) {
                                String[] unused10 = WifiRomUpdateHelper.this.mDualStaApps = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_DUAL_STA_APPS_EXP".equals(mTagName)) {
                                String[] unused11 = WifiRomUpdateHelper.this.mDualStaAppsExp = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_DUAL_STA_BLACK_LIST".equals(mTagName)) {
                                String[] unused12 = WifiRomUpdateHelper.this.mDualStaBlackList = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_WECHAT_LM_PARAMS".equals(mTagName)) {
                                String[] unused13 = WifiRomUpdateHelper.this.mLmParams = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_SKIP_DESTROY_SOCKET_APPS".equals(mTagName)) {
                                String[] unused14 = WifiRomUpdateHelper.this.mSkipDestroySocketApps = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_VIDEO_APPS".equals(mTagName)) {
                                String[] unused15 = WifiRomUpdateHelper.this.mVideoApps = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_DOWNLOAD_APPS".equals(mTagName)) {
                                String[] unused16 = WifiRomUpdateHelper.this.mDownloadApps = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if ("NETWORK_DUAL_STA_CAP_HOST_BLACK_LIST".equals(mTagName)) {
                                String[] unused17 = WifiRomUpdateHelper.this.mDualStaCapHostBlackList = mText.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else if (WifiRomUpdateHelper.SMART_BW_PARAMS.equals(mTagName)) {
                                WifiRomUpdateHelper.this.setSmartBWParams(mText);
                            } else if ("DEFAULT_MAC_RANDOMIZATION_SETTING".equals(mTagName)) {
                                WifiRomUpdateHelper.this.setDefaultMacRandomizationSetting(mText);
                            } else {
                                WifiRomUpdateHelper.this.mKeyValuePair.put(mTagName, mText);
                            }
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                WifiRomUpdateHelper.this.log("Got execption parsing permissions.", e);
            } catch (IOException e2) {
                WifiRomUpdateHelper.this.log("Got execption parsing permissions.", e2);
                if (strReader != null) {
                }
            } catch (Throwable th) {
                if (strReader != null) {
                    strReader.close();
                }
                throw th;
            }
            strReader.close();
            WifiRomUpdateHelper.this.sendWifiRomUpdateChangedBroadcast();
            if (WifiRomUpdateHelper.this.DEBUG) {
                Log.d(WifiRomUpdateHelper.TAG, "\txml file parse end!");
            }
        }
    }

    private WifiRomUpdateHelper(Context context) {
        super(context, FILE_NAME, SYS_FILE_PATH, DATA_FILE_PATH);
        setUpdateInfo(new WifiRomUpdateInfo(), new WifiRomUpdateInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized WifiRomUpdateHelper getInstance(Context context) {
        WifiRomUpdateHelper wifiRomUpdateHelper;
        synchronized (WifiRomUpdateHelper.class) {
            if (sInstance == null) {
                synchronized (WifiRomUpdateHelper.class) {
                    if (sInstance == null) {
                        sInstance = new WifiRomUpdateHelper(context);
                    }
                }
            }
            wifiRomUpdateHelper = sInstance;
        }
        return wifiRomUpdateHelper;
    }

    public String getValue(String key, String defaultVal) {
        String value = this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        return value;
    }

    public Integer getIntegerValue(String key, Integer defaultVal) {
        String value = this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        try {
            return Integer.valueOf(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            return defaultVal;
        }
    }

    public boolean getBooleanValue(String key, boolean defaultVal) {
        Boolean result;
        String value = this.mKeyValuePair.get(key);
        Boolean.valueOf(defaultVal);
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
        String value = this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        try {
            return Double.valueOf(Double.parseDouble(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            return defaultVal;
        }
    }

    public Long getLongValue(String key, Long defaultVal) {
        String value = this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        try {
            return Long.valueOf(Long.parseLong(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            return defaultVal;
        }
    }

    public void enableVerboseLogging(int level) {
        this.DEBUG = level > 0;
    }

    @Override // android.net.wifi.RomUpdateHelper
    public void dump() {
        if (this.DEBUG) {
            Log.d(TAG, "dump:");
        }
        for (String key : this.mKeyValuePair.keySet()) {
            String value = this.mKeyValuePair.get(key);
            Log.d(TAG, "\t" + key + SettingsStringUtil.DELIMITER + value);
        }
    }

    public String[] getMtuServer() {
        return this.mMtuServer;
    }

    public String[] getSlaWhiteListApps() {
        return this.mSlaApps;
    }

    public String[] getSlaWhiteListAppsExp() {
        return this.mSlaAppsExp;
    }

    public String[] getSlaBlackListApps() {
        return this.mSlaBlackList;
    }

    public String[] getSlaGameApps() {
        return this.mSlaGameApps;
    }

    public String[] getSlaGameAppsExp() {
        return this.mSlaGameAppsExp;
    }

    public String[] getSlaEnabledMcc() {
        return this.mSlaEnabledMCC;
    }

    public String[] getSlaParams() {
        return this.mSlaParams;
    }

    public int[] getSpeedRttParams() {
        return this.mSpeedRttParams;
    }

    public String[] getSkipDestroySocketApps() {
        return this.mSkipDestroySocketApps;
    }

    /* access modifiers changed from: private */
    public void setSpeedRttParams(String text) {
        String[] params;
        if (text != null && (params = text.split(SmsManager.REGEX_PREFIX_DELIMITER)) != null && params.length == 11) {
            try {
                this.mSpeedRttParams = new int[]{Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Integer.parseInt(params[5]), Integer.parseInt(params[6]), Integer.parseInt(params[7]), Integer.parseInt(params[8]), Integer.parseInt(params[9]), Integer.parseInt(params[10])};
            } catch (Exception e) {
                Log.e(TAG, "setSpeedRttParams failed to parse params:" + text);
                this.mSpeedRttParams = null;
            }
        }
    }

    public String[] getSlaGameParams() {
        return this.mSlaGameParams;
    }

    public String[] getDualStaWhiteListApps() {
        return this.mDualStaApps;
    }

    public String[] getDualStaWhiteListAppsExp() {
        return this.mDualStaAppsExp;
    }

    public String[] getDualStaBlackListApps() {
        return this.mDualStaBlackList;
    }

    public String[] getDualStaBlackListCapHosts() {
        return this.mDualStaCapHostBlackList;
    }

    public String[] getDualStaDisabledMcc() {
        return this.mDualStaDisableMcc;
    }

    public String[] getAllVideoApps() {
        return this.mVideoApps;
    }

    public String[] getDownloadApps() {
        return this.mDownloadApps;
    }

    public String[] getWechatLmParams() {
        return this.mLmParams;
    }

    /* access modifiers changed from: private */
    public void sendWifiRomUpdateChangedBroadcast() {
        Intent intent = new Intent("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED");
        intent.addFlags(67108864);
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
        }
    }

    /* access modifiers changed from: private */
    public void setDefaultMacRandomizationSetting(String value) {
        int result = 0;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse int exception:" + ex);
        }
        if (result == 0 || result == 1) {
            SystemProperties.set("persist.sys.wifi.mac_randomization", Integer.toString(result));
            return;
        }
        Log.d(TAG, "random mac value invalid!");
        SystemProperties.set("persist.sys.wifi.mac_randomization", Integer.toString(0));
    }
}
