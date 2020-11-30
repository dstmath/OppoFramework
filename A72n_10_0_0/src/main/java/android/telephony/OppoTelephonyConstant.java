package android.telephony;

import com.android.internal.telephony.TelephonyProperties;

public class OppoTelephonyConstant {
    public static final String ACTION_DEFAULT_SIM_REMOVED = "android.intent.action.DEFAULT_SIM_REMOVED";
    public static final String ACTION_DOWNLOAD_CALIBRATION_DATA = "android.intent.action.DOWNLOAD_CALIBRATION_DATA";
    public static final String ACTION_EMT_CALL_STATE_CHANGED = "android.intent.action.ACTION_EMT_CALL_STATE_CHANGED";
    public static final String ACTION_NEW_INCOMING_CALL = "android.intent.action.NEW_INCOMING_CALL";
    public static final String ACTION_NEW_SIM_DETECTED = "android.intent.action.NEW_SIM_DETECTED";
    public static final String ACTION_RADIO_OFF = "android.intent.action.RADIO_OFF";
    public static final String ACTION_SIM_INFO_UPDATE = "oppo.intent.action.SIM_INFO_UPDATE";
    public static final String ACTION_SIM_INSERTED_STATUS = "android.intent.action.SIM_INSERTED_STATUS";
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String ACTION_WIFI_FAILOVER_GPRS_DIALOG = "oppo.intent.action_WIFI_FAILOVER_GPRS_DIALOG";
    public static final int APN_REQUEST_FAILED_DUE_TO_RADIO_OFF = 98;
    public static final String APN_TYPE_CMMAIL = "cmmail";
    public static final int APN_TYPE_DISABLE_ONGOING = 100;
    public static final String APN_TYPE_DM = "dm";
    public static final String APN_TYPE_NET = "net";
    public static final int APN_TYPE_NOT_AVAILABLE_DUE_TO_RECORDS_NOT_LOADED = 99;
    public static final String APN_TYPE_RCSE = "rcse";
    public static final String APN_TYPE_TETHERING = "tethering";
    public static final String APN_TYPE_WAP = "wap";
    public static final int BM_ALL_HW_SUPPORT_CHINA = 42;
    public static final int BM_ALL_HW_SUPPORT_FOREIGN = 43;
    public static final int BM_GSM_1800 = 33;
    public static final int BM_GSM_1900 = 34;
    public static final int BM_GSM_850 = 31;
    public static final int BM_GSM_850_AND_1800 = 38;
    public static final int BM_GSM_850_AND_1900 = 40;
    public static final int BM_GSM_900 = 32;
    public static final int BM_GSM_900_AND_1800 = 39;
    public static final int BM_WCDMA_1900 = 36;
    public static final int BM_WCDMA_2100 = 37;
    public static final int BM_WCDMA_850 = 35;
    public static final int BM_WCDMA_850_AND_1900 = 41;
    public static final boolean CTA_ENABLE = false;
    public static final String DISCONNECT_DATA_FLAG = "disconnectPdpFlag";
    public static String DORMANT_PROBE_INTERVAL = "prop.oppo.data_dormant_interval";
    public static final int EF_CC = 20259;
    public static final int EMT_CALL_STATE_ACTIVE = 2;
    public static final int EMT_CALL_STATE_ALERTING = 5;
    public static final int EMT_CALL_STATE_DIALING = 1;
    public static final int EMT_CALL_STATE_ERROR = -1;
    public static final int EMT_CALL_STATE_HOLDING = 3;
    public static final int EMT_CALL_STATE_IDLE = 0;
    public static final int EMT_CALL_STATE_UNKNOWN = 4;
    public static final int ENCODING_7BIT_LOCKING = 12;
    public static final int ENCODING_7BIT_LOCKING_SINGLE = 13;
    public static final int ENCODING_7BIT_SINGLE = 11;
    public static final int EVENT_CALL_FORWARD_DELAY = 8888;
    public static final int EVENT_COPY_TEXT_MESSAGE_DONE = 110;
    public static final int EVENT_EM_CALL_STATUS_CHANGED = 115;
    public static final int EVENT_EM_DIAL_DONE = 107;
    public static final int EVENT_EM_GET_CURRENT_CALLS_DONE = 108;
    public static final int EVENT_EM_HANGUP_DONE = 109;
    public static final int EVENT_GET_ALL_SPACE = 101;
    public static final int EVENT_GET_USED_SPACE = 100;
    public static final int EVENT_GO_DORMANT_DONE = 7777;
    public static final int EVENT_SEND_UPLINK_DATA_DONE = 114;
    public static final int EVENT_USIM_ANR_LOAD_DONE = 5;
    public static final String EXTRA_CALIBRATION_DATA = "calibrationData";
    public static final String EXTRA_EMT_CALL_NUMBER = "number";
    public static final String EXTRA_EMT_CALL_STATE = "state";
    public static final String EXTRA_INCOMING_NUMBER = "PHONE_NUMBER";
    public static final String EXTRA_PARAMS_VALIDITY_PERIOD = "validity_period";
    public static final String FEATURE_ENABLE_CMMAIL = "enableCMMAIL";
    public static final String FEATURE_ENABLE_DM = "enableDM";
    public static final String FEATURE_ENABLE_NET = "enableNET";
    public static final String FEATURE_ENABLE_WAP = "enableWAP";
    public static final String GEMINI_DEFAULT_SIM_MODE = "persist.radio.default_sim_mode";
    public static final String GEMINI_DEFAULT_SIM_PROP = "persist.radio.default_sim";
    public static final String GEMINI_GPRS_TRANSFER_TYPE = "gemini.gprs.transfer.type";
    public static final int GEMINI_SIM_1 = 0;
    public static final int GEMINI_SIM_2 = 1;
    public static final int GEMINI_SIM_3 = 2;
    public static final int GEMINI_SIM_4 = 3;
    public static final String GEMINI_SIM_ID_KEY = "simId";
    public static final String GEMINI_SIM_NUM_PROP = "persist.gemini.sim_num";
    public static final int GEMINI_SIP_CALL = -1;
    public static final int HEADER_SIZE = 8;
    public static final String INTENT_KEY_ICC_STATE = "state";
    public static final int INT_SIZE = 4;
    public static final String IS_VT_CALL = "isVtCall";
    public static final String LOCK_TAG = "MMDATA_UP";
    public static final int MAXNUM_RETRY_SIMRECORD = 3;
    public static final int MAX_USER_DATA_BYTES_WITH_DATA_SMS_HEADER = 133;
    public static final int MAX_USER_DATA_BYTES_WITH_DATA_SMS_HEADER_AND_CONCATENATED_SMS_HEADER = 128;
    public static final String MULTI_SIM_ID_KEY = "simid";
    public static final int NV_SERVICE_DOMAIN_PREF_I = 850;
    public static final int OPPO_ADN_FILE_SIZE = 250;
    public static final int OPPO_EVENT_ADN_LOAD_ALL_DONE = 95;
    public static final int OPPO_EVENT_ADN_SIZE_DONE = 990;
    public static final int OPPO_EVENT_GET_ADN_FIELD_INFO_DONE = 102;
    public static final int OPPO_EVENT_GET_PBC_SIZE_DONE = 94;
    public static final int OPPO_EVENT_GET_SMSC = 101;
    public static final int OPPO_EVENT_READ_CC_DONE = 93;
    public static final int OPPO_EVENT_READ_PBC_DONE = 92;
    public static final int OPPO_EVENT_SET_SMSC = 100;
    public static final int OPPO_EVENT_UPDATE_ADN_DONE = 90;
    public static final int OPPO_EVENT_UPDATE_ANR_DONE = 993;
    public static final int OPPO_EVENT_UPDATE_EMAIL_DONE = 991;
    public static final int OPPO_EVENT_UPDATE_IAP_DONE = 992;
    public static final int OPPO_EVENT_UPDATE_PBC_DONE = 91;
    public static final int OPPO_MAX_PB_EMAIL_LENGTH = 30;
    public static final int OPPO_MAX_PB_NAME_LENGTH = 14;
    public static final int OPPO_MAX_PB_NUMBER_LENGTH = 20;
    public static final int OPPO_SIMPHONEBOOK_STATE_ERROR = -1;
    public static final int OPPO_SIMPHONEBOOK_STATE_NOT_READY = 0;
    public static final int OPPO_SIMPHONEBOOK_STATE_READY = 1;
    public static final int PHB_POLL_TIMEOUT = 240000;
    public static final int PHB_START_POLL_TIME1 = 120000;
    public static final int PHB_START_POLL_TIME2 = 240000;
    public static final int POLL_DORMANT_IDLE_MAX_TIMES = 1;
    public static final int POLL_DORMANT_MAX_TIMES = 2;
    public static final int POLL_DORMANT_MILLIS = 6000;
    public static final int POLL_MAX_TIME = 10;
    public static final int PORT_WAP_VCAL_S = 9207;
    public static final int PORT_WAP_VCARD = 9204;
    public static final String PROPERTY_CS_NETWORK_TYPE = "gsm.cs.network.type";
    public static final String PROPERTY_CS_NETWORK_TYPE_2 = "gsm.cs.network.type.2";
    public static String PROPERTY_DATA_NETWORK_TYPE_2 = TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE_2;
    public static final String PROPERTY_GSM_SIM_INSERTED = "gsm.sim.inserted";
    public static String PROPERTY_ICC_OPERATOR_ALPHA_2 = TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA_2;
    public static String PROPERTY_ICC_OPERATOR_DEFAULT_NAME = TelephonyProperties.PROPERTY_ICC_OPERATOR_DEFAULT_NAME;
    public static String PROPERTY_ICC_OPERATOR_DEFAULT_NAME_2 = TelephonyProperties.PROPERTY_ICC_OPERATOR_DEFAULT_NAME_2;
    public static String PROPERTY_ICC_OPERATOR_ISO_COUNTRY_2 = TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY_2;
    public static String PROPERTY_ICC_OPERATOR_NUMERIC_2 = TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC_2;
    public static String PROPERTY_ICC_OPERATOR_PNN_NAME = "gsm.sim.operator.pnn.name";
    public static final String PROPERTY_OPERATOR_ALPHA_2 = "gsm.operator.alpha.2";
    public static final String PROPERTY_OPERATOR_ISMANUAL_2 = "operator.ismanual.2";
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY_2 = "gsm.operator.iso-country.2";
    public static final String PROPERTY_OPERATOR_ISROAMING_2 = "gsm.operator.isroaming.2";
    public static final String PROPERTY_OPERATOR_NUMERIC_2 = "gsm.operator.numeric.2";
    public static final String PROPERTY_ROAMING_INDICATOR_NEEDED = "gsm.roaming.indicator.needed";
    public static final String PROPERTY_ROAMING_INDICATOR_NEEDED_2 = "gsm.roaming.indicator.needed.2";
    public static final String PROPERTY_SIM_INFO_READY = "gsm.siminfo.ready";
    public static String PROPERTY_SIM_STATE_2 = TelephonyProperties.PROPERTY_SIM_STATE_2;
    public static final int QCRILHOOK_BASE = 524288;
    public static final int QCRILHOOK_GO_DORMANT = 524291;
    public static final int QCRILHOOK_NV_READ = 524289;
    public static final int QCRILHOOK_NV_WRITE = 524290;
    public static final String REASON_GPRS_ATTACHED_TIMEOUT = "gprsAttachedTimeout";
    public static final String REASON_NO_SUCH_PDP = "noSuchPdp";
    public static final String REASON_ON_RADIO_AVAILABLE = "onRadioAvailable";
    public static final String REASON_ON_RECORDS_LOADED = "onRecordsLoaded";
    public static final String REASON_PDP_NOT_ACTIVE = "pdpNotActive";
    public static final String REASON_POLL_STATE_DONE = "pollStateDone";
    public static final int RESULT_BLOCK = 1;
    public static final int RESULT_ERROR_INVALID_ADDRESS = 8;
    public static final int RESULT_ERROR_SIM_MEM_FULL = 7;
    public static final int RESULT_ERROR_SUCCESS = 0;
    public static final int RESULT_PASS = 0;
    public static final int SIM_INDICATOR_ABSENT = 0;
    public static final int SIM_INDICATOR_CONNECTED = 7;
    public static final int SIM_INDICATOR_INVALID = 3;
    public static final int SIM_INDICATOR_LOCKED = 2;
    public static final int SIM_INDICATOR_NORMAL = 5;
    public static final int SIM_INDICATOR_RADIOOFF = 1;
    public static final int SIM_INDICATOR_ROAMING = 6;
    public static final int SIM_INDICATOR_ROAMINGCONNECTED = 8;
    public static final int SIM_INDICATOR_SEARCHING = 4;
    public static final int SIM_INDICATOR_UNKNOWN = -1;
    public static final String SIM_PLUGIN = "PLUGIN";
    public static final String SIM_PLUGOUT = "PLUGOUT";
    public static final int SIM_STATE_SIMREFRESH = 100;
    public static final int SIM_STATE_SWICTHCARD = 99;
    public static final int SRV_DOMAIN_PREF_SIZE = 4;
    public static final String SUBSCRIPTION_KEY = "subscription";
    public static final int TOTAL_SIM_COLOR_COUNT = 4;
    public static final int mHeaderSize = (mOemIdentifier.length() + 8);
    public static final String mOemIdentifier = "QOEMHOOK";
    public static final int mOppoMatchLen = -1;

    public interface RILConstants {
        public static final int RIL_REQUEST_FACTORY_MODE_MODEM_GPIO = 157;
        public static final int RIL_REQUEST_FACTORY_MODE_NV_PROCESS = 156;
        public static final int RIL_REQUEST_GET_BAND_MODE = 158;
        public static final int RIL_REQUEST_GET_MDM_BASEBAND = 164;
        public static final int RIL_REQUEST_GET_RFFE_DEV_INFO = 160;
        public static final int RIL_REQUEST_GO_TO_ERROR_FATAL = 163;
        public static final int RIL_REQUEST_OEM_BASE = 155;
        public static final int RIL_REQUEST_OEM_COMMON_REQ = 180;
        public static final int RIL_REQUEST_OEM_SET_ECC_LIST = 186;
        public static final int RIL_REQUEST_OPPO_CONTROL_MODEM_FEATURE = 178;
        public static final int RIL_REQUEST_OPPO_EXP_IND_REGION_CHANGED_FOR_RIL_ECCLIST = 171;
        public static final int RIL_REQUEST_OPPO_GET_ASDIV_STATE = 179;
        public static final int RIL_REQUEST_OPPO_GET_LTE_BW = 181;
        public static final int RIL_REQUEST_OPPO_GET_NW_SEARCH_COUNT = 185;
        public static final int RIL_REQUEST_OPPO_GET_RADIO_INFO = 167;
        public static final int RIL_REQUEST_OPPO_GET_TX_RX_INFO = 170;
        public static final int RIL_REQUEST_OPPO_LOCK_GSM_ARFCN = 175;
        public static final int RIL_REQUEST_OPPO_LOCK_LTE_CELL = 177;
        public static final int RIL_REQUEST_OPPO_RFFE_CMD = 176;
        public static final int RIL_REQUEST_OPPO_SET_FAKEBS_WEIGHT = 172;
        public static final int RIL_REQUEST_OPPO_SET_FILTER_ARFCN = 168;
        public static final int RIL_REQUEST_OPPO_SET_PPLMN_LIST = 169;
        public static final int RIL_REQUEST_OPPO_SET_VOLTE_FR1 = 174;
        public static final int RIL_REQUEST_OPPO_SET_VOLTE_FR2 = 173;
        public static final int RIL_REQUEST_OPPO_SIMLOCK_REQ = 186;
        public static final int RIL_REQUEST_REPORT_BOOTUPNVRESTOR_STATE = 159;
        public static final int RIL_REQUEST_SET_TDD_LTE = 165;
        public static final int RIL_REQUEST_SIM_TRANSMIT_BASIC = 161;
        public static final int RIL_REQUEST_SIM_TRANSMIT_CHANNEL = 162;
    }
}