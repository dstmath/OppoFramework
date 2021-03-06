package com.mediatek.internal.telephony;

import com.android.internal.telephony.RILConstants;

public interface MtkRILConstants extends RILConstants {
    public static final int ACTIVATE_REASON_BASE = 1000;
    public static final int ACTIVATE_REASON_SSC_MODE3 = 1001;
    public static final int DATA_PROFILE_ALL = 1011;
    public static final int DATA_PROFILE_BIP = 1008;
    public static final int DATA_PROFILE_EMERGENCY = 1005;
    public static final int DATA_PROFILE_HIPRI = 1003;
    public static final int DATA_PROFILE_MCX = 1010;
    public static final int DATA_PROFILE_MMS = 1001;
    public static final int DATA_PROFILE_RCS = 1007;
    public static final int DATA_PROFILE_SUPL = 1002;
    public static final int DATA_PROFILE_VSIM = 1009;
    public static final int DATA_PROFILE_WAP = 1004;
    public static final int DATA_PROFILE_XCAP = 1006;
    public static final int DEACTIVATE_REASON_APN_CHANGED = 2005;
    public static final int DEACTIVATE_REASON_BASE = 2000;
    public static final int DEACTIVATE_REASON_NORMAL = 2001;
    public static final int DEACTIVATE_REASON_NO_PCSCF = 2003;
    public static final int DEACTIVATE_REASON_RA_INITIAL_FAIL = 2002;
    public static final int DEACTIVATE_REASON_RA_REFRESH_FAIL = 2004;
    public static final int DEACTIVATE_REASON_TEMP_DATA_SWITCHING = 2006;
    public static final String MSIM_MODE_SETTING = "msim_mode_setting";
    public static final int NETWORK_MODE_CDMA_EVDO_GSM = 104;
    public static final int NETWORK_MODE_CDMA_GSM = 103;
    public static final int NETWORK_MODE_LTE_CDMA_EVDO_GSM = 105;
    public static final int NETWORK_MODE_LTE_GSM = 101;
    public static final int NETWORK_MODE_LTE_TDD_ONLY = 102;
    public static final int PHB_ADN = 0;
    public static final int PHB_ECC = 3;
    public static final int PHB_FDN = 1;
    public static final int PHB_MAX_ENTRY = 10;
    public static final int PHB_MSISDN = 2;
    public static final int RIL_REQUEST_ABORT_FEMTOCELL_LIST = 2056;
    public static final int RIL_REQUEST_ABORT_QUERY_AVAILABLE_NETWORKS = 2007;
    public static final int RIL_REQUEST_ADD_IMS_CONFERENCE_CALL_MEMBER = 2090;
    public static final int RIL_REQUEST_CANCEL_USSI = 2094;
    public static final int RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE_EX = 2172;
    public static final int RIL_REQUEST_CONFERENCE_DIAL = 2089;
    public static final int RIL_REQUEST_CONFIG_A2_OFFSET = 2190;
    public static final int RIL_REQUEST_CONFIG_B1_OFFSET = 2191;
    public static final int RIL_REQUEST_DATA_CONNECTION_ATTACH = 2144;
    public static final int RIL_REQUEST_DATA_CONNECTION_DETACH = 2145;
    public static final int RIL_REQUEST_DELETE_UPB_ENTRY = 2041;
    public static final int RIL_REQUEST_DIAL_WITH_SIP_URI = 2086;
    public static final int RIL_REQUEST_DISABLE_NR = 2193;
    public static final int RIL_REQUEST_ECC_PREFERRED_RAT = 2110;
    public static final int RIL_REQUEST_ECC_REDIAL_APPROVE = 2198;
    public static final int RIL_REQUEST_EDIT_UPB_ENTRY = 2040;
    public static final int RIL_REQUEST_EMBMS_AT_CMD = 2060;
    public static final int RIL_REQUEST_ENABLE_DSDA_INDICATION = 2185;
    public static final int RIL_REQUEST_ENABLE_SCG_FAILURE = 2192;
    public static final int RIL_REQUEST_ENTER_DEPERSONALIZATION = 2143;
    public static final int RIL_REQUEST_ENTER_DEVICE_NETWORK_DEPERSONALIZATION = 2171;
    public static final int RIL_REQUEST_FORCE_RELEASE_CALL = 2034;
    public static final int RIL_REQUEST_GENERAL_SIM_AUTH = 2064;
    public static final int RIL_REQUEST_GET_COLP = 2104;
    public static final int RIL_REQUEST_GET_COLR = 2105;
    public static final int RIL_REQUEST_GET_DSDA_STATUS = 2186;
    public static final int RIL_REQUEST_GET_ECC_NUM = 2149;
    public static final int RIL_REQUEST_GET_FEMTOCELL_LIST = 2055;
    public static final int RIL_REQUEST_GET_GSM_SMS_BROADCAST_ACTIVATION = 2115;
    public static final int RIL_REQUEST_GET_LTE_RELEASE_VERSION = 2152;
    public static final int RIL_REQUEST_GET_PHB_MEM_STORAGE = 2046;
    public static final int RIL_REQUEST_GET_PHB_STRING_LENGTH = 2045;
    public static final int RIL_REQUEST_GET_POL_CAPABILITY = 2107;
    public static final int RIL_REQUEST_GET_POL_LIST = 2108;
    public static final int RIL_REQUEST_GET_PROVISION_VALUE = 2078;
    public static final int RIL_REQUEST_GET_PSEUDO_CELL_INFO = 2022;
    public static final int RIL_REQUEST_GET_ROAMING_ENABLE = 2112;
    public static final int RIL_REQUEST_GET_SMS_PARAMS = 2012;
    public static final int RIL_REQUEST_GET_SMS_RUIM_MEM_STATUS = 2024;
    public static final int RIL_REQUEST_GET_SMS_SIM_MEM_STATUS = 2011;
    public static final int RIL_REQUEST_GET_SUGGESTED_PLMN_LIST = 2189;
    public static final int RIL_REQUEST_GET_XCAP_STATUS = 2163;
    public static final int RIL_REQUEST_GSM_GET_BROADCAST_LANGUAGE = 2010;
    public static final int RIL_REQUEST_GSM_SET_BROADCAST_LANGUAGE = 2009;
    public static final int RIL_REQUEST_HANGUP_ALL = 2019;
    public static final int RIL_REQUEST_HANGUP_WITH_REASON = 2183;
    public static final int RIL_REQUEST_HOLD_CALL = 2084;
    public static final int RIL_REQUEST_IMS_BEARER_STATE_CONFIRM = 2080;
    public static final int RIL_REQUEST_IMS_CONFIG_GET_FEATURE = 2137;
    public static final int RIL_REQUEST_IMS_CONFIG_GET_PROVISION = 2139;
    public static final int RIL_REQUEST_IMS_CONFIG_GET_RESOURCE_CAP = 2141;
    public static final int RIL_REQUEST_IMS_CONFIG_SET_FEATURE = 2136;
    public static final int RIL_REQUEST_IMS_CONFIG_SET_PROVISION = 2138;
    public static final int RIL_REQUEST_IMS_DEREG_NOTIFICATION = 2082;
    public static final int RIL_REQUEST_IMS_DIAL = 2098;
    public static final int RIL_REQUEST_IMS_ECT = 2083;
    public static final int RIL_REQUEST_IMS_EMERGENCY_DIAL = 2087;
    public static final int RIL_REQUEST_IMS_SEND_SMS_EX = 2133;
    public static final int RIL_REQUEST_IMS_VT_DIAL = 2099;
    public static final int RIL_REQUEST_IWLAN_REGISTER_CELLULAR_QUALITY_REPORT = 2188;
    public static final int RIL_REQUEST_MODEM_POWEROFF = 2004;
    public static final int RIL_REQUEST_MODEM_POWERON = 2003;
    public static final int RIL_REQUEST_MODIFY_MODEM_TYPE = 2184;
    public static final int RIL_REQUEST_NOTIFY_EPDG_SCREEN_STATE = 2179;
    public static final int RIL_REQUEST_OEM_HOOK_ATCI_INTERNAL = 2008;
    public static final int RIL_REQUEST_PULL_CALL = 2096;
    public static final int RIL_REQUEST_QUERY_AVAILABLE_NETWORKS_WITH_ACT = 2006;
    public static final int RIL_REQUEST_QUERY_CALL_FORWARD_IN_TIME_SLOT = 2125;
    public static final int RIL_REQUEST_QUERY_FEMTOCELL_SYSTEM_SELECTION_MODE = 2058;
    public static final int RIL_REQUEST_QUERY_PHB_STORAGE_INFO = 2036;
    public static final int RIL_REQUEST_QUERY_SIM_NETWORK_LOCK = 2067;
    public static final int RIL_REQUEST_QUERY_UPB_AVAILABLE = 2050;
    public static final int RIL_REQUEST_QUERY_UPB_CAPABILITY = 2039;
    public static final int RIL_REQUEST_QUERY_VOPS_STATUS = 2178;
    public static final int RIL_REQUEST_READ_ANR_ENTRY = 2053;
    public static final int RIL_REQUEST_READ_EMAIL_ENTRY = 2051;
    public static final int RIL_REQUEST_READ_PHB_ENTRY = 2038;
    public static final int RIL_REQUEST_READ_PHB_ENTRY_EXT = 2048;
    public static final int RIL_REQUEST_READ_SNE_ENTRY = 2052;
    public static final int RIL_REQUEST_READ_UPB_AAS_LIST = 2054;
    public static final int RIL_REQUEST_READ_UPB_GAS_LIST = 2042;
    public static final int RIL_REQUEST_READ_UPB_GRP = 2043;
    public static final int RIL_REQUEST_REMOVE_CB_MESSAGE = 2015;
    public static final int RIL_REQUEST_REMOVE_IMS_CONFERENCE_CALL_MEMBER = 2091;
    public static final int RIL_REQUEST_RESET_ALL_CONNECTIONS = 2146;
    public static final int RIL_REQUEST_RESET_MD_DATA_RETRY_COUNT = 2063;
    public static final int RIL_REQUEST_RESET_SUPP_SERV = 2164;
    public static final int RIL_REQUEST_RESTART_RILD = 2150;
    public static final int RIL_REQUEST_RESUME_CALL = 2085;
    public static final int RIL_REQUEST_RESUME_REGISTRATION = 2000;
    public static final int RIL_REQUEST_RTT_MODIFY_REQUST_RESPONSE = 2177;
    public static final int RIL_REQUEST_RUN_GBA = 2127;
    public static final int RIL_REQUEST_SEARCH_RAT = 2196;
    public static final int RIL_REQUEST_SEARCH_STORED_FREQUENCY_INFO = 2195;
    public static final int RIL_REQUEST_SELECT_FEMTOCELL = 2057;
    public static final int RIL_REQUEST_SEND_CNAP = 2106;
    public static final int RIL_REQUEST_SEND_RTT_MODIFY_REQUEST = 2175;
    public static final int RIL_REQUEST_SEND_RTT_TEXT = 2176;
    public static final int RIL_REQUEST_SEND_SAR_INDICATOR = 2201;
    public static final int RIL_REQUEST_SEND_USSI = 2093;
    public static final int RIL_REQUEST_SETPROP_IMS_HANDOVER = 2159;
    public static final int RIL_REQUEST_SETUP_XCAP_USER_AGENT_STRING = 2167;
    public static final int RIL_REQUEST_SET_BACKGROUND_SEARCH_TIMER = 2197;
    public static final int RIL_REQUEST_SET_CALL_FORWARD_IN_TIME_SLOT = 2126;
    public static final int RIL_REQUEST_SET_CALL_INDICATION = 2016;
    public static final int RIL_REQUEST_SET_CLIP = 2103;
    public static final int RIL_REQUEST_SET_COLP = 2123;
    public static final int RIL_REQUEST_SET_COLR = 2124;
    public static final int RIL_REQUEST_SET_E911_STATE = 2129;
    public static final int RIL_REQUEST_SET_ECC_LIST = 2030;
    public static final int RIL_REQUEST_SET_ECC_MODE = 2035;
    public static final int RIL_REQUEST_SET_ECC_NUM = 2148;
    public static final int RIL_REQUEST_SET_EMERGENCY_ADDRESS_ID = 2121;
    public static final int RIL_REQUEST_SET_ETWS = 2014;
    public static final int RIL_REQUEST_SET_FD_MODE = 2025;
    public static final int RIL_REQUEST_SET_FEMTOCELL_SYSTEM_SELECTION_MODE = 2059;
    public static final int RIL_REQUEST_SET_GEO_LOCATION = 2120;
    public static final int RIL_REQUEST_SET_GWSD_CALL_VALID = 2181;
    public static final int RIL_REQUEST_SET_GWSD_IGNORE_CALL_INTERVAL = 2182;
    public static final int RIL_REQUEST_SET_GWSD_KEEP_ALIVE_IPDATA = 2200;
    public static final int RIL_REQUEST_SET_GWSD_KEEP_ALIVE_PDCP = 2199;
    public static final int RIL_REQUEST_SET_GWSD_MODE = 2180;
    public static final int RIL_REQUEST_SET_IMSCFG = 2077;
    public static final int RIL_REQUEST_SET_IMS_BEARER_NOTIFICATION = 2135;
    public static final int RIL_REQUEST_SET_IMS_ENABLE = 2069;
    public static final int RIL_REQUEST_SET_IMS_REGISTRATION_REPORT = 2097;
    public static final int RIL_REQUEST_SET_IMS_RTP_REPORT = 2088;
    public static final int RIL_REQUEST_SET_IMS_VIDEO_ENABLE = 2075;
    public static final int RIL_REQUEST_SET_IMS_VOICE_ENABLE = 2074;
    public static final int RIL_REQUEST_SET_LTE_ACCESS_STRATUM_REPORT = 2065;
    public static final int RIL_REQUEST_SET_LTE_RELEASE_VERSION = 2151;
    public static final int RIL_REQUEST_SET_LTE_UPLINK_DATA_TRANSFER = 2066;
    public static final int RIL_REQUEST_SET_MD_IMSCFG = 2128;
    public static final int RIL_REQUEST_SET_NATT_KEEPALIVE_STATUS = 2131;
    public static final int RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL_WITH_ACT = 2005;
    public static final int RIL_REQUEST_SET_OVERRIDE_APN = 2161;
    public static final int RIL_REQUEST_SET_PDN_NAME_REUSE = 2162;
    public static final int RIL_REQUEST_SET_PDN_REUSE = 2160;
    public static final int RIL_REQUEST_SET_PHB_MEM_STORAGE = 2047;
    public static final int RIL_REQUEST_SET_PHONEBOOK_READY = 2157;
    public static final int RIL_REQUEST_SET_POL_ENTRY = 2109;
    public static final int RIL_REQUEST_SET_PROVISION_VALUE = 2079;
    public static final int RIL_REQUEST_SET_PSEUDO_CELL_MODE = 2021;
    public static final int RIL_REQUEST_SET_PS_REGISTRATION = 2020;
    public static final int RIL_REQUEST_SET_RCS_UA_ENABLE = 2166;
    public static final int RIL_REQUEST_SET_REMOVE_RESTRICT_EUTRAN_MODE = 2100;
    public static final int RIL_REQUEST_SET_ROAMING_ENABLE = 2111;
    public static final int RIL_REQUEST_SET_RTT_MODE = 2174;
    public static final int RIL_REQUEST_SET_SERVICE_STATE = 2130;
    public static final int RIL_REQUEST_SET_SIM_NETWORK_LOCK = 2068;
    public static final int RIL_REQUEST_SET_SIM_POWER = 2002;
    public static final int RIL_REQUEST_SET_SMS_FWK_READY = 2134;
    public static final int RIL_REQUEST_SET_SMS_PARAMS = 2013;
    public static final int RIL_REQUEST_SET_SS_PROPERTY = 2168;
    public static final int RIL_REQUEST_SET_TRM = 2028;
    public static final int RIL_REQUEST_SET_TX_POWER = 2194;
    public static final int RIL_REQUEST_SET_TX_POWER_STATUS = 2158;
    public static final int RIL_REQUEST_SET_VENDOR_SETTING = 2173;
    public static final int RIL_REQUEST_SET_VILTE_ENABLE = 2072;
    public static final int RIL_REQUEST_SET_VIWIFI_ENABLE = 2073;
    public static final int RIL_REQUEST_SET_VOICE_DOMAIN_PREFERENCE = 2122;
    public static final int RIL_REQUEST_SET_VOICE_PREFER_STATUS = 2147;
    public static final int RIL_REQUEST_SET_VOLTE_ENABLE = 2070;
    public static final int RIL_REQUEST_SET_WFC_CONFIG = 2187;
    public static final int RIL_REQUEST_SET_WFC_ENABLE = 2071;
    public static final int RIL_REQUEST_SET_WFC_PROFILE = 2095;
    public static final int RIL_REQUEST_SET_WIFI_ASSOCIATED = 2117;
    public static final int RIL_REQUEST_SET_WIFI_ENABLED = 2116;
    public static final int RIL_REQUEST_SET_WIFI_IP_ADDRESS = 2119;
    public static final int RIL_REQUEST_SET_WIFI_PING_RESULT = 2132;
    public static final int RIL_REQUEST_SET_WIFI_SIGNAL_LEVEL = 2118;
    public static final int RIL_REQUEST_SIGNAL_STRENGTH_WITH_WCDMA_ECIO = 2153;
    public static final int RIL_REQUEST_SIM_GET_ATR = 2001;
    public static final int RIL_REQUEST_SIM_GET_ICCID = 2142;
    public static final int RIL_REQUEST_SMS_ACKNOWLEDGE_EX = 2170;
    public static final int RIL_REQUEST_STK_HANDLE_CALL_SETUP_REQUESTED_FROM_SIM_WITH_RESULT_CODE = 2029;
    public static final int RIL_REQUEST_SWITCH_MODE_FOR_ECC = 2023;
    public static final int RIL_REQUEST_SYNC_DATA_SETTINGS_TO_MD = 2062;
    public static final int RIL_REQUEST_VENDOR_BASE = 2000;
    public static final int RIL_REQUEST_VIDEO_CALL_ACCEPT = 2076;
    public static final int RIL_REQUEST_VSIM_NOTIFICATION = 2113;
    public static final int RIL_REQUEST_VSIM_OPERATION = 2114;
    public static final int RIL_REQUEST_VSS_ANTENNA_CONF = 2101;
    public static final int RIL_REQUEST_VSS_ANTENNA_INFO = 2102;
    public static final int RIL_REQUEST_VT_DIAL_WITH_SIP_URI = 2092;
    public static final int RIL_REQUEST_WRITE_PHB_ENTRY = 2037;
    public static final int RIL_REQUEST_WRITE_PHB_ENTRY_EXT = 2049;
    public static final int RIL_REQUEST_WRITE_UPB_GRP = 2044;
    public static final int RIL_UNSOL_ACTIVE_WIFI_PDN_COUNT = 3077;
    public static final int RIL_UNSOL_ATCI_RESPONSE = 3009;
    public static final int RIL_UNSOL_CALLMOD_CHANGE_INDICATOR = 3034;
    public static final int RIL_UNSOL_CALL_ADDITIONAL_INFO = 3126;
    public static final int RIL_UNSOL_CALL_FORWARDING = 3070;
    public static final int RIL_UNSOL_CALL_INFO_INDICATION = 3031;
    public static final int RIL_UNSOL_CARD_DETECTED_IND = 3125;
    public static final int RIL_UNSOL_CDMA_CALL_ACCEPTED = 3069;
    public static final int RIL_UNSOL_CDMA_CARD_INITIAL_ESN_OR_MEID = 3023;
    public static final int RIL_UNSOL_CDMA_PLMN_CHANGED = 3004;
    public static final int RIL_UNSOL_CIPHER_INDICATION = 3024;
    public static final int RIL_UNSOL_CRSS_NOTIFICATION = 3025;
    public static final int RIL_UNSOL_DATA_ALLOWED = 3014;
    public static final int RIL_UNSOL_DATA_ATTACH_APN_CHANGED = 3021;
    public static final int RIL_UNSOL_DEDICATE_BEARER_ACTIVATED = 3082;
    public static final int RIL_UNSOL_DEDICATE_BEARER_DEACTIVATED = 3084;
    public static final int RIL_UNSOL_DEDICATE_BEARER_MODIFIED = 3083;
    public static final int RIL_UNSOL_DSBP_STATE_CHANGED = 3114;
    public static final int RIL_UNSOL_ECC_NUM = 3095;
    public static final int RIL_UNSOL_ECONF_RESULT_INDICATION = 3032;
    public static final int RIL_UNSOL_ECONF_SRVCC_INDICATION = 3072;
    public static final int RIL_UNSOL_ECT_INDICATION = 3046;
    public static final int RIL_UNSOL_EMBMS_AT_INFO = 3055;
    public static final int RIL_UNSOL_EMBMS_SESSION_STATUS = 3054;
    public static final int RIL_UNSOL_ENCRYPTED_SERIAL_ID_UPDATED = 3202;
    public static final int RIL_UNSOL_FEMTOCELL_INFO = 3029;
    public static final int RIL_UNSOL_GET_PROVISION_DONE = 3037;
    public static final int RIL_UNSOL_GMSS_RAT_CHANGED = 3003;
    public static final int RIL_UNSOL_GTT_CAPABILITY_INDICATION = 3048;
    public static final int RIL_UNSOL_IMEI_LOCK = 3007;
    public static final int RIL_UNSOL_IMSI_REFRESH_DONE = 3008;
    public static final int RIL_UNSOL_IMS_BEARER_INIT = 3051;
    public static final int RIL_UNSOL_IMS_BEARER_STATE_NOTIFY = 3049;
    public static final int RIL_UNSOL_IMS_CONFERENCE_INFO_INDICATION = 3089;
    public static final int RIL_UNSOL_IMS_CONFIG_CONFIG_CHANGED = 3093;
    public static final int RIL_UNSOL_IMS_CONFIG_CONFIG_LOADED = 3094;
    public static final int RIL_UNSOL_IMS_CONFIG_DYNAMIC_IMS_SWITCH_COMPLETE = 3091;
    public static final int RIL_UNSOL_IMS_CONFIG_FEATURE_CHANGED = 3092;
    public static final int RIL_UNSOL_IMS_DATA_INFO_NOTIFY = 3111;
    public static final int RIL_UNSOL_IMS_DEREG_DONE = 3052;
    public static final int RIL_UNSOL_IMS_DISABLE_DONE = 3043;
    public static final int RIL_UNSOL_IMS_DISABLE_START = 3045;
    public static final int RIL_UNSOL_IMS_ENABLE_DONE = 3042;
    public static final int RIL_UNSOL_IMS_ENABLE_START = 3044;
    public static final int RIL_UNSOL_IMS_EVENT_PACKAGE_INDICATION = 3040;
    public static final int RIL_UNSOL_IMS_MULTIIMS_COUNT = 3085;
    public static final int RIL_UNSOL_IMS_REGISTRATION_INFO = 3041;
    public static final int RIL_UNSOL_IMS_RTP_INFO = 3038;
    public static final int RIL_UNSOL_INCOMING_CALL_INDICATION = 3015;
    public static final int RIL_UNSOL_INVALID_SIM = 3016;
    public static final int RIL_UNSOL_IWLAN_CELLULAR_QUALITY_CHANGED_IND = 3132;
    public static final int RIL_UNSOL_LTE_ACCESS_STRATUM_STATE_CHANGE = 3062;
    public static final int RIL_UNSOL_LTE_MESSAGE_WAITING_INDICATION = 3090;
    public static final int RIL_UNSOL_MCCMNC_CHANGED = 3096;
    public static final int RIL_UNSOL_MD_DATA_RETRY_COUNT_RESET = 3059;
    public static final int RIL_UNSOL_MELOCK_NOTIFICATION = 3056;
    public static final int RIL_UNSOL_ME_SMS_STORAGE_FULL = 3011;
    public static final int RIL_UNSOL_MOBILE_DATA_USAGE = 3133;
    public static final int RIL_UNSOL_MOBILE_WIFI_HANDOVER = 3076;
    public static final int RIL_UNSOL_MOBILE_WIFI_ROVEOUT = 3075;
    public static final int RIL_UNSOL_MODULATION_INFO = 3019;
    public static final int RIL_UNSOL_NATT_KEEP_ALIVE_CHANGED = 3086;
    public static final int RIL_UNSOL_NETWORK_EVENT = 3018;
    public static final int RIL_UNSOL_NETWORK_INFO = 3030;
    public static final int RIL_UNSOL_NETWORK_REJECT_CAUSE = 3109;
    public static final int RIL_UNSOL_NO_EMERGENCY_CALLBACK_MODE = 3117;
    public static final int RIL_UNSOL_NW_LIMIT = 3134;
    public static final int RIL_UNSOL_ON_DSDA_CHANGED = 3131;
    public static final int RIL_UNSOL_ON_USSI = 3036;
    public static final int RIL_UNSOL_ON_VOLTE_SUBSCRIPTION = 3110;
    public static final int RIL_UNSOL_ON_XUI = 3039;
    public static final int RIL_UNSOL_PCO_DATA_AFTER_ATTACHED = 3053;
    public static final int RIL_UNSOL_PCO_STATUS = 3061;
    public static final int RIL_UNSOL_PHB_READY_NOTIFICATION = 3028;
    public static final int RIL_UNSOL_PSEUDO_CELL_INFO = 3017;
    public static final int RIL_UNSOL_QUALIFIED_NETWORK_TYPES_CHANGED = 3130;
    public static final int RIL_UNSOL_REMOVE_RESTRICT_EUTRAN = 3060;
    public static final int RIL_UNSOL_REQUEST_GEO_LOCATION = 3080;
    public static final int RIL_UNSOL_RESET_ATTACH_APN = 3020;
    public static final int RIL_UNSOL_RESPONSE_CDMA_NEW_SMS_EX = 3116;
    public static final int RIL_UNSOL_RESPONSE_CS_NETWORK_STATE_CHANGED = 3013;
    public static final int RIL_UNSOL_RESPONSE_ETWS_NOTIFICATION = 3010;
    public static final int RIL_UNSOL_RESPONSE_NEW_SMS_EX = 3113;
    public static final int RIL_UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT_EX = 3112;
    public static final int RIL_UNSOL_RESPONSE_PLMN_CHANGED = 3000;
    public static final int RIL_UNSOL_RESPONSE_PS_NETWORK_CHANGED = 3002;
    public static final int RIL_UNSOL_RESPONSE_REGISTRATION_SUSPENDED = 3001;
    public static final int RIL_UNSOL_RSU_SIM_LOCK_NOTIFICATION = 3128;
    public static final int RIL_UNSOL_RTT_AUDIO_INDICATION = 3122;
    public static final int RIL_UNSOL_RTT_CAPABILITY_INDICATION = 3120;
    public static final int RIL_UNSOL_RTT_MODIFY_REQUEST_RECEIVE = 3121;
    public static final int RIL_UNSOL_RTT_MODIFY_RESPONSE = 3118;
    public static final int RIL_UNSOL_RTT_TEXT_RECEIVE = 3119;
    public static final int RIL_UNSOL_SIGNAL_STRENGTH_WITH_WCDMA_ECIO = 3097;
    public static final int RIL_UNSOL_SIM_COMMON_SLOT_NO_CHANGED = 3068;
    public static final int RIL_UNSOL_SIM_MISSING = 3065;
    public static final int RIL_UNSOL_SIM_PLUG_IN = 3063;
    public static final int RIL_UNSOL_SIM_PLUG_OUT = 3064;
    public static final int RIL_UNSOL_SIM_POWER_CHANGED = 3124;
    public static final int RIL_UNSOL_SIM_RECOVERY = 3066;
    public static final int RIL_UNSOL_SIM_SLOT_LOCK_POLICY_NOTIFY = 3115;
    public static final int RIL_UNSOL_SIP_CALL_PROGRESS_INDICATOR = 3033;
    public static final int RIL_UNSOL_SML_DEVICE_LOCK_INFO_NOTIFY = 3201;
    public static final int RIL_UNSOL_SMS_READY_NOTIFICATION = 3012;
    public static final int RIL_UNSOL_SPEECH_CODEC_INFO = 3027;
    public static final int RIL_UNSOL_STK_BIP_PROACTIVE_COMMAND = 3057;
    public static final int RIL_UNSOL_STK_SETUP_MENU_RESET = 3071;
    public static final int RIL_UNSOL_SUPP_SVC_NOTIFICATION_EX = 3026;
    public static final int RIL_UNSOL_TRAY_PLUG_IN = 3067;
    public static final int RIL_UNSOL_TRIGGER_OTASP = 3058;
    public static final int RIL_UNSOL_TX_POWER_STATUS = 3107;
    public static final int RIL_UNSOL_VENDOR_BASE = 3000;
    public static final int RIL_UNSOL_VIDEO_CAPABILITY_INDICATOR = 3035;
    public static final int RIL_UNSOL_VIRTUAL_SIM_OFF = 3006;
    public static final int RIL_UNSOL_VIRTUAL_SIM_ON = 3005;
    public static final int RIL_UNSOL_VIRTUAL_SIM_STATUS_CHANGED = 3129;
    public static final int RIL_UNSOL_VOLTE_LTE_CONNECTION_STATUS = 3073;
    public static final int RIL_UNSOL_VOLTE_SETTING = 3047;
    public static final int RIL_UNSOL_VOPS_INDICATION = 3123;
    public static final int RIL_UNSOL_VSIM_OPERATION_INDICATION = 3074;
    public static final int RIL_UNSOL_WFC_PDN_STATE = 3081;
    public static final int RIL_UNSOL_WIFI_LOCK = 3127;
    public static final int RIL_UNSOL_WIFI_PDN_ERROR = 3079;
    public static final int RIL_UNSOL_WIFI_PDN_OOS = 3088;
    public static final int RIL_UNSOL_WIFI_PING_REQUEST = 3087;
    public static final int RIL_UNSOL_WIFI_RSSI_MONITORING_CONFIG = 3078;
    public static final int RIL_UNSOL_WORLD_MODE_CHANGED = 3022;
    public static final int SIM_POWER_OFF = 0;
    public static final int SIM_POWER_ON = 1;
    public static final int SIM_POWER_RESET = 2;
}
