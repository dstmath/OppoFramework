package com.android.internal.telephony;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class TelephonyIntents {
    public static final String ACTION_ACMT_NETWORK_SERVICE_STATUS_INDICATOR = "mediatek.intent.action.acmt_nw_service_status";
    public static final String ACTION_ANY_DATA_CONNECTION_STATE_CHANGED = "android.intent.action.ANY_DATA_STATE";
    public static final String ACTION_CARRIER_SETUP = "android.intent.action.ACTION_CARRIER_SETUP";
    public static final String ACTION_CARRIER_SIGNAL_PCO_VALUE = "android.intent.action.CARRIER_SIGNAL_PCO_VALUE";
    public static final String ACTION_CARRIER_SIGNAL_REDIRECTED = "android.intent.action.CARRIER_SIGNAL_REDIRECTED";
    public static final String ACTION_CARRIER_SIGNAL_REQUEST_NETWORK_FAILED = "android.intent.action.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED";
    public static final String ACTION_COMMON_SLOT_NO_CHANGED = "com.mediatek.phone.ACTION_COMMON_SLOT_NO_CHANGED";
    public static final String ACTION_DATA_CONNECTION_FAILED = "android.intent.action.DATA_CONNECTION_FAILED";
    public static final String ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED";
    public static final String ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED";
    public static final String ACTION_DEFAULT_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED";
    public static final String ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED";
    public static final String ACTION_EF_CSP_CONTENT_NOTIFY = "android.intent.action.ACTION_EF_CSP_CONTENT_NOTIFY";
    public static final String ACTION_EF_RAT_CONTENT_NOTIFY = "android.intent.action.ACTION_EF_RAT_CONTENT_NOTIFY";
    public static final String ACTION_EMBMS_SESSION_STATUS_CHANGED = "mediatek.intent.action.EMBMS_SESSION_STATUS_CHANGED";
    public static final String ACTION_EMERGENCY_CALLBACK_MODE_CHANGED = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED";
    public static final String ACTION_EMERGENCY_CALL_STATE_CHANGED = "android.intent.action.EMERGENCY_CALL_STATE_CHANGED";
    public static final String ACTION_EXCEPTION_HAPPENED = "com.mediatek.log2server.EXCEPTION_HAPPEND";
    public static final String ACTION_FORBIDDEN_NO_SERVICE_AUTHORIZATION = "android.intent.action.ACTION_FORBIDDEN_NO_SERVICE_AUTHORIZATION";
    public static final String ACTION_HIDE_NETWORK_STATE = "mediatek.intent.action.ACTION_HIDE_NETWORK_STATE";
    public static final String ACTION_IVSR_NOTIFY = "mediatek.intent.action.IVSR_NOTIFY";
    public static final String ACTION_LOCATED_PLMN_CHANGED = "mediatek.intent.action.LOCATED_PLMN_CHANGED";
    public static final String ACTION_LTE_ACCESS_STRATUM_STATE_CHANGED = "mediatek.intent.action.LTE_ACCESS_STRATUM_STATE_CHANGED";
    public static final String ACTION_LTE_MESSAGE_WAITING_INDICATION = "android.intent.action.lte.mwi";
    public static final String ACTION_MD_TYPE_CHANGE = "android.intent.action.ACTION_MD_TYPE_CHANGE";
    public static final String ACTION_NETWORK_EVENT = "android.intent.action.ACTION_NETWORK_EVENT";
    public static final String ACTION_NETWORK_SET_TIME = "android.intent.action.NETWORK_SET_TIME";
    public static final String ACTION_NETWORK_SET_TIMEZONE = "android.intent.action.NETWORK_SET_TIMEZONE";
    public static final String ACTION_NOTIFY_MODULATION_INFO = "mediatek.intent.action.ACTION_NOTIFY_MODULATION_INFO";
    public static final String ACTION_PBM_STATE_READY = "android.intent.action.PBM_STATE_READY";
    public static final String ACTION_PCO_STATUS = "com.mediatek.intent.action.ACTION_PCO_STATUS";
    public static final String ACTION_PHB_STATE_CHANGED = "android.intent.action.PHB_STATE_CHANGED";
    public static final String ACTION_PSEUDO_BS_DETECTED = "android.intent.action.ACTION_PSEUDO_BS_DETECTED";
    public static final String ACTION_PSEUDO_CELL_CHANGED = "android.intent.action.ACTION_PSEUDO_CELL_CHANGED";
    public static final String ACTION_PS_NETWORK_TYPE_CHANGED = "mediatek.intent.action.PS_NETWORK_TYPE_CHANGED";
    public static final String ACTION_RADIO_STATE_CHANGED = "android.intent.action.RADIO_STATE_CHANGED";
    public static final String ACTION_RADIO_TECHNOLOGY_CHANGED = "android.intent.action.RADIO_TECHNOLOGY";
    public static final String ACTION_RAT_CHANGED = "android.intent.action.ACTION_RAT_CHANGED";
    public static final String ACTION_REMOVE_IDLE_TEXT = "android.intent.aciton.stk.REMOVE_IDLE_TEXT";
    public static final String ACTION_REMOVE_IDLE_TEXT_2 = "android.intent.aciton.stk.REMOVE_IDLE_TEXT_2";
    public static final String ACTION_REQUEST_OMADM_CONFIGURATION_UPDATE = "com.android.omadm.service.CONFIGURATION_UPDATE";
    public static final String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";
    public static final String ACTION_SET_PHONE_RAT_FAMILY_DONE = "android.intent.action.ACTION_SET_PHONE_RAT_FAMILY_DONE";
    public static final String ACTION_SET_PHONE_RAT_FAMILY_FAILED = "android.intent.action.ACTION_SET_PHONE_RAT_FAMILY_FAILED";
    public static final String ACTION_SET_RADIO_CAPABILITY_DONE = "android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE";
    public static final String ACTION_SET_RADIO_CAPABILITY_FAILED = "android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED";
    public static final String ACTION_SHARED_DEFAULT_APN_STATE_CHANGED = "mediatek.intent.action.SHARED_DEFAULT_APN_STATE_CHANGED";
    public static final String ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS = "android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS";
    public static final String ACTION_SIGNAL_STRENGTH_CHANGED = "android.intent.action.SIG_STR";
    public static final String ACTION_SIM_RECOVERY_DONE = "com.android.phone.ACTION_SIM_RECOVERY_DONE";
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String ACTION_SIM_STATE_CHANGED_MULTI_APPLICATION = "mediatek.intent.action.ACTION_SIM_STATE_CHANGED_MULTI_APPLICATION";
    public static final String ACTION_SUBINFO_CONTENT_CHANGE = "android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE";
    public static final String ACTION_SUBINFO_RECORD_UPDATED = "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED";
    public static final String ACTION_UNLOCK_SIM_LOCK = "mediatek.intent.action.ACTION_UNLOCK_SIM_LOCK";
    public static final String ACTION_WORLD_MODE_CHANGED = "android.intent.action.ACTION_WORLD_MODE_CHANGED";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_APN_PROTO_KEY = "apnProto";
    public static final String EXTRA_APN_TYPE_KEY = "apnType";
    public static final String EXTRA_COLUMN_NAME = "columnName";
    public static final String EXTRA_CSG_ID = "csgId";
    public static final String EXTRA_DATA_SPN = "spnData";
    public static final String EXTRA_DOMAIN = "domain";
    public static final String EXTRA_EF_RAT_CONTENT = "ef_rat_content";
    public static final String EXTRA_EF_RAT_STATUS = "ef_rat_status";
    public static final String EXTRA_ERROR_CODE_KEY = "errorCode";
    public static final String EXTRA_EVENT_TYPE = "eventType";
    public static final String EXTRA_FEMTO = "femtocell";
    public static final String EXTRA_HNB_NAME = "hnbName";
    public static final String EXTRA_IMS_REG_STATE_KEY = "regState";
    public static final String EXTRA_INT_CONTENT = "intContent";
    public static final String EXTRA_ISO = "iso";
    public static final String EXTRA_IS_ACTIVE = "isActived";
    public static final String EXTRA_LTE_MWI_BODY = "lte_mwi_body";
    public static final String EXTRA_MD_TYPE = "mdType";
    public static final String EXTRA_MODULATION_INFO = "modulation_info";
    public static final String EXTRA_PCO_ID_KEY = "pcoId";
    public static final String EXTRA_PCO_TYPE = "pcoType";
    public static final String EXTRA_PCO_VALUE_KEY = "pcoValue";
    public static final String EXTRA_PLMN = "plmn";
    public static final String EXTRA_PLMN_MODE_BIT = "plmn_mode_bit";
    public static final String EXTRA_PSEUDO_BS_INFO = "pseudoInfo";
    public static final String EXTRA_PSEUDO_BS_PHONE = "phoneId";
    public static final String EXTRA_PSEUDO_CELL_INFO = "info";
    public static final String EXTRA_PSEUDO_CELL_STAMP = "timeStamp";
    public static final String EXTRA_PSEUDO_CELL_STATE = "state";
    public static final String EXTRA_RADIO_ACCESS_FAMILY = "rafs";
    public static final String EXTRA_RAT = "rat";
    public static final String EXTRA_REAL_SERVICE_STATE = "state";
    public static final String EXTRA_REDIRECTION_URL_KEY = "redirectionUrl";
    public static final String EXTRA_SHOW_PLMN = "showPlmn";
    public static final String EXTRA_SHOW_SPN = "showSpn";
    public static final String EXTRA_SPN = "spn";
    public static final String EXTRA_STRING_CONTENT = "stringContent";
    public static final String EXTRA_WORLD_MODE_CHANGE_STATE = "worldModeState";
    public static final String INTENT_KEY_IVSR_ACTION = "action";
    public static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    public static final String SPN_STRINGS_UPDATED_ACTION = "android.provider.Telephony.SPN_STRINGS_UPDATED";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.TelephonyIntents.<init>():void, dex: 
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
    public TelephonyIntents() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.TelephonyIntents.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.TelephonyIntents.<init>():void");
    }
}
