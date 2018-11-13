package com.qualcomm.qcrilhook;

import android.os.AsyncResult;
import android.os.Handler;

public interface IQcRilHook {
    public static final int QCRILHOOK_BASE = 524288;
    public static final int QCRILHOOK_DMS_GET_DEVICE_SERIAL_NUMBERS = 528394;
    public static final int QCRILHOOK_DMS_GET_FTM_MODE = 528391;
    public static final int QCRILHOOK_DMS_GET_SPC_CHANGE_ENABLED = 528395;
    public static final int QCRILHOOK_DMS_GET_SW_VERSION = 528392;
    public static final int QCRILHOOK_DMS_SET_SPC_CHANGE_ENABLED = 528396;
    public static final int QCRILHOOK_DMS_UPDATE_SERVICE_PROGRAMING_CODE = 528393;
    public static final int QCRILHOOK_GO_DORMANT = 524291;
    public static final int QCRILHOOK_ME_DEPERSONALIZATION = 524292;
    public static final int QCRILHOOK_NAS_GET_3GPP2_SUBSCRIPTION_INFO = 528385;
    public static final int QCRILHOOK_NAS_GET_MOB_CAI_REV = 528387;
    public static final int QCRILHOOK_NAS_GET_RTRE_CONFIG = 528389;
    public static final int QCRILHOOK_NAS_SET_3GPP2_SUBSCRIPTION_INFO = 528386;
    public static final int QCRILHOOK_NAS_SET_MOB_CAI_REV = 528388;
    public static final int QCRILHOOK_NAS_SET_RTRE_CONFIG = 528390;
    public static final int QCRILHOOK_NAS_UPDATE_AKEY = 528384;
    public static final int QCRILHOOK_NV_READ = 524289;
    public static final int QCRILHOOK_NV_WRITE = 524290;
    public static final int QCRILHOOK_QMI_OEMHOOK_REQUEST_ID = 524388;
    public static final int QCRILHOOK_REQUEST_ID_BASE = 524289;
    public static final int QCRILHOOK_REQUEST_ID_MAX = 524387;
    public static final int QCRILHOOK_UNSOL_BASE = 525288;
    public static final int QCRILHOOK_UNSOL_CARD_STATE_CHANGED = 525318;
    public static final int QCRILHOOK_UNSOL_CDMA_BURST_DTMF = 525289;
    public static final int QCRILHOOK_UNSOL_CDMA_CONT_DTMF_START = 525290;
    public static final int QCRILHOOK_UNSOL_CDMA_CONT_DTMF_STOP = 525291;
    public static final int QCRILHOOK_UNSOL_EXTENDED_DBM_INTL = 525288;
    public static final int QCRILHOOK_UNSOL_LOCAL_RINGBACK_START = 525292;
    public static final int QCRILHOOK_UNSOL_LOCAL_RINGBACK_STOP = 525293;
    public static final int QCRILHOOK_UNSOL_MAX = 525387;
    public static final int QCRILHOOK_UNSOL_MAX_DATA_ALLOWED_CHANGED = 525342;
    public static final int QCRILHOOK_UNSOL_OEMHOOK = 525388;
    public static final int QCRILHOOK_UNSOL_PDC_CLEAR_CONFIGS = 525305;
    public static final int QCRILHOOK_UNSOL_PDC_CONFIG = 525302;
    public static final int QCRILHOOK_UNSOL_PDC_LIST_CONFIG = 525320;
    public static final int QCRILHOOK_UNSOL_SIMLOCK_TEMP_UNLOCK_EXPIRED = 525317;
    public static final int QCRILHOOK_UNSOL_SLOT_STATUS_CHANGE_IND = 525321;
    public static final int QCRILHOOK_UNSOL_UICC_PROVISION_STATUS_CHANGED = 525316;
    public static final int QCRILHOOK_VOICE_GET_CONFIG = 528398;
    public static final int QCRILHOOK_VOICE_SET_CONFIG = 528397;
    public static final int QCRIL_EVT_HOOK_ABORT_NW_SCAN = 524383;
    public static final int QCRIL_EVT_HOOK_ACT_CONFIGS = 524338;
    public static final int QCRIL_EVT_HOOK_CARD_POWER_REQ = 524503;
    public static final int QCRIL_EVT_HOOK_CDMA_AVOID_CUR_NWK = 524302;
    public static final int QCRIL_EVT_HOOK_CDMA_CLEAR_AVOIDANCE_LIST = 524303;
    public static final int QCRIL_EVT_HOOK_CDMA_GET_AVOIDANCE_LIST = 524304;
    public static final int QCRIL_EVT_HOOK_DEACT_CONFIGS = 524332;
    public static final int QCRIL_EVT_HOOK_DELETE_ALL_CONFIGS = 524319;
    public static final int QCRIL_EVT_HOOK_ENABLE_ENGINEER_MODE = 524307;
    public static final int QCRIL_EVT_HOOK_ENTER_DEPERSONALIZATION_CODE = 524504;
    public static final int QCRIL_EVT_HOOK_GET_ADN_RECORD = 524509;
    public static final int QCRIL_EVT_HOOK_GET_ATR = 524499;
    public static final int QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS = 524311;
    public static final int QCRIL_EVT_HOOK_GET_CARD_STATE = 524498;
    public static final int QCRIL_EVT_HOOK_GET_CONFIG = 524310;
    public static final int QCRIL_EVT_HOOK_GET_CSG_ID = 524312;
    public static final int QCRIL_EVT_HOOK_GET_MAX_DATA_ALLOWED = 524381;
    public static final int QCRIL_EVT_HOOK_GET_META_INFO = 524321;
    public static final int QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE = 524336;
    public static final int QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID = 524337;
    public static final int QCRIL_EVT_HOOK_GET_OMH_CALL_PROFILE = 524490;
    public static final int QCRIL_EVT_HOOK_GET_PAGING_PRIORITY = 524296;
    public static final int QCRIL_EVT_HOOK_GET_PERSONALIZATION_STATUS = 525314;
    public static final int QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_ACQ_ORDER = 524316;
    public static final int QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_BAND_PREF = 524326;
    public static final int QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE = 524333;
    public static final int QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID = 524335;
    public static final int QCRIL_EVT_HOOK_GET_SLOTS_STATUS_REQ = 524506;
    public static final int QCRIL_EVT_HOOK_GET_SLOT_MAPPING = 524502;
    public static final int QCRIL_EVT_HOOK_GET_TERMINAL_CAPABILITY_LPA_TLV_REQ = 524507;
    public static final int QCRIL_EVT_HOOK_GET_TUNEAWAY = 524294;
    public static final int QCRIL_EVT_HOOK_GET_UICC_ICCID = 524380;
    public static final int QCRIL_EVT_HOOK_GET_UICC_PROVISION_PREFERENCE = 524378;
    public static final int QCRIL_EVT_HOOK_INFORM_SHUTDOWN = 524298;
    public static final int QCRIL_EVT_HOOK_LTE_DIRECT_DISC_REQ = 524390;
    public static final int QCRIL_EVT_HOOK_PERFORM_INCREMENTAL_NW_SCAN = 524306;
    public static final int QCRIL_EVT_HOOK_PROTOBUF_MSG = 524389;
    public static final int QCRIL_EVT_HOOK_SEL_CONFIG = 524320;
    public static final int QCRIL_EVT_HOOK_SEND_APDU_CMD = 524500;
    public static final int QCRIL_EVT_HOOK_SET_APN_INFO = 524330;
    public static final int QCRIL_EVT_HOOK_SET_ATEL_UI_STATUS = 524314;
    public static final int QCRIL_EVT_HOOK_SET_BUILTIN_PLMN_LIST = 524305;
    public static final int QCRIL_EVT_HOOK_SET_CDMA_SUB_SRC_WITH_SPC = 524299;
    public static final int QCRIL_EVT_HOOK_SET_CONFIG = 524309;
    public static final int QCRIL_EVT_HOOK_SET_DATA_SUBSCRIPTION = 524327;
    public static final int QCRIL_EVT_HOOK_SET_IS_DATA_ENABLED = 524328;
    public static final int QCRIL_EVT_HOOK_SET_IS_DATA_ROAMING_ENABLED = 524329;
    public static final int QCRIL_EVT_HOOK_SET_LTE_TUNE_AWAY = 524331;
    public static final int QCRIL_EVT_HOOK_SET_PAGING_PRIORITY = 524295;
    public static final int QCRIL_EVT_HOOK_SET_PERSONALIZATION = 525313;
    public static final int QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_ACQ_ORDER = 524315;
    public static final int QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_BAND_PREF = 524325;
    public static final int QCRIL_EVT_HOOK_SET_QCRILLOG_ON = 524511;
    public static final int QCRIL_EVT_HOOK_SET_TERMINAL_CAPABILITY_LPA_TLV_REQ = 524508;
    public static final int QCRIL_EVT_HOOK_SET_TUNEAWAY = 524293;
    public static final int QCRIL_EVT_HOOK_SET_UICC_PROVISION_PREFERENCE = 524379;
    public static final int QCRIL_EVT_HOOK_SWITCH_SLOT = 524501;
    public static final int QCRIL_EVT_HOOK_UNSOL_LTE_DIRECT_DISC = 524391;
    public static final int QCRIL_EVT_HOOK_UNSOL_RAT_RAC_CHANGE_IND = 525341;
    public static final int QCRIL_EVT_HOOK_UPDATE_ADN_RECORD = 524510;
    public static final int QCRIL_EVT_HOOK_VALIDATE_CONFIG = 524334;
    public static final int QCRIL_EVT_REQ_HOOK_GET_L_PLUS_L_FEATURE_SUPPORT_STATUS_REQ = 524382;
    public static final int QCRIL_EVT_SET_LOCAL_CALL_HOLD = 531288;
    public static final int SERVICE_PROGRAMMING_BASE = 4096;

    public static class QcRilExtendedDbmIntlKddiAocr {
        public byte chg_ind;
        public byte db_subtype;
        public short mcc;
        public byte sub_unit;
        public byte unit;
    }

    boolean getLpluslSupportStatus();

    boolean qcRilCleanupConfigs();

    boolean qcRilGetAllConfigs();

    @Deprecated
    String[] qcRilGetAvailableConfigs(String str);

    String qcRilGetConfig();

    int qcRilGetCsgId();

    int qcRilGetPrioritySub();

    int qcRilGetPrioritySubscription();

    boolean qcRilGetTuneAway();

    boolean qcRilGoDormant(String str);

    byte[] qcRilSendProtocolBufferMessage(byte[] bArr, int i);

    boolean qcRilSetCdmaSubSrcWithSpc(int i, String str);

    boolean qcRilSetConfig(String str);

    boolean qcRilSetPrioritySub(int i);

    boolean qcRilSetPrioritySubscription(int i);

    boolean qcRilSetTuneAway(boolean z);

    void registerForExtendedDbmIntl(Handler handler, int i, Object obj);

    void registerForFieldTestData(Handler handler, int i, Object obj);

    AsyncResult sendQcRilHookMsg(int i);

    AsyncResult sendQcRilHookMsg(int i, byte b);

    AsyncResult sendQcRilHookMsg(int i, int i2);

    AsyncResult sendQcRilHookMsg(int i, String str);

    AsyncResult sendQcRilHookMsg(int i, byte[] bArr);

    void unregisterForExtendedDbmIntl(Handler handler);

    void unregisterForFieldTestData(Handler handler);
}
