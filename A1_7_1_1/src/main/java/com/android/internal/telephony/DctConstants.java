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
public class DctConstants {
    public static final int APN_BIP_ID = 17;
    public static final int APN_CBS_ID = 7;
    public static final int APN_CMMAIL_ID = 13;
    public static final int APN_DEFAULT_ID = 0;
    public static final int APN_DM_ID = 10;
    public static final int APN_DUN_ID = 3;
    public static final int APN_EMERGENCY_ID = 9;
    public static final int APN_FOTA_ID = 6;
    public static final int APN_HIPRI_ID = 4;
    public static final int APN_IA_ID = 8;
    public static final int APN_IMS_ID = 5;
    public static final int APN_INVALID_ID = -1;
    public static final int APN_MMS_ID = 1;
    public static final int APN_NET_ID = 12;
    public static final int APN_NUM_TYPES = 18;
    public static final int APN_RCSE_ID = 14;
    public static final int APN_RCS_ID = 16;
    public static final int APN_SUPL_ID = 2;
    public static final String APN_TYPE_KEY = "apnType";
    public static final int APN_WAP_ID = 11;
    public static final int APN_XCAP_ID = 15;
    public static final int BASE = 270336;
    public static final int CMD_CLEAR_PROVISIONING_SPINNER = 270378;
    public static final int CMD_DELAY_SETUP_DATA = 270384;
    public static final int CMD_ENABLE_MOBILE_PROVISIONING = 270373;
    public static final int CMD_IS_PROVISIONING_APN = 270374;
    public static final int CMD_NET_STAT_POLL = 270376;
    public static final int CMD_SET_DEPENDENCY_MET = 270367;
    public static final int CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA = 270372;
    public static final int CMD_SET_POLICY_DATA_ENABLE = 270368;
    public static final int CMD_SET_USER_DATA_ENABLE = 270366;
    public static final int DISABLED = 0;
    public static final int ENABLED = 1;
    public static final int EVENT_APN_CHANGED = 270355;
    public static final int EVENT_APN_CHANGED_DONE = 270839;
    public static final int EVENT_ATTACH_APN_CHANGED = 270852;
    public static final int EVENT_CDMA_DATA_DETACHED = 270356;
    public static final int EVENT_CDMA_OTA_PROVISION = 270361;
    public static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 270357;
    public static final int EVENT_CLEAN_UP_ALL_CONNECTIONS = 270365;
    public static final int EVENT_CLEAN_UP_CONNECTION = 270360;
    public static final int EVENT_DATA_ALLOWED = 270853;
    public static final int EVENT_DATA_CONNECTION_ATTACHED = 270352;
    public static final int EVENT_DATA_CONNECTION_DETACHED = 270345;
    public static final int EVENT_DATA_ENABLED_SETTINGS = 270856;
    public static final int EVENT_DATA_RAT_CHANGED = 270377;
    public static final int EVENT_DATA_ROAMING_CHANGED = 270837;
    public static final int EVENT_DATA_SETUP_COMPLETE = 270336;
    public static final int EVENT_DATA_SETUP_COMPLETE_ERROR = 270371;
    public static final int EVENT_DATA_STALL_ALARM = 270353;
    public static final int EVENT_DATA_STATE_CHANGED = 270340;
    public static final int EVENT_DEFAULT_APN_REFERENCE_COUNT_CHANGED = 270846;
    public static final int EVENT_DEVICE_PROVISIONED_CHANGE = 270379;
    public static final int EVENT_DISCONNECT_DC_RETRYING = 270370;
    public static final int EVENT_DISCONNECT_DONE = 270351;
    public static final int EVENT_DO_RECOVERY = 270354;
    public static final int EVENT_ENABLE_NEW_APN = 270349;
    public static final int EVENT_FDN_CHANGED = 270841;
    public static final int EVENT_ICC_CHANGED = 270369;
    public static final int EVENT_INIT_EMERGENCY_APN_SETTINGS = 270838;
    public static final int EVENT_LINK_STATE_CHANGED = 270346;
    public static final int EVENT_LTE_ACCESS_STRATUM_STATE = 270847;
    public static final int EVENT_MD_DATA_RETRY_COUNT_RESET = 270855;
    public static final int EVENT_OEM_SCREEN_CHANGED = 270383;
    public static final int EVENT_PCO_DATA_RECEIVED = 270381;
    public static final int EVENT_PCO_STATUS = 270851;
    public static final int EVENT_POLL_PDP = 270341;
    public static final int EVENT_POST_CREATE_PHONE = 270845;
    public static final int EVENT_PROVISIONING_APN_ALARM = 270375;
    public static final int EVENT_PS_RESTRICT_DISABLED = 270359;
    public static final int EVENT_PS_RESTRICT_ENABLED = 270358;
    public static final int EVENT_RADIO_AVAILABLE = 270337;
    public static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 270342;
    public static final int EVENT_RECORDS_LOADED = 270338;
    public static final int EVENT_REDIRECTION_DETECTED = 270380;
    public static final int EVENT_REG_PLMN_CHANGED = 270848;
    public static final int EVENT_REG_SUSPENDED = 270849;
    public static final int EVENT_REMOVE_RESTRICT_EUTRAN = 270842;
    public static final int EVENT_RESET_ATTACH_APN = 270844;
    public static final int EVENT_RESET_DONE = 270364;
    public static final int EVENT_RESET_PDP_DONE = 270843;
    public static final int EVENT_RESTART_RADIO = 270362;
    public static final int EVENT_RESTORE_DEFAULT_APN = 270350;
    public static final int EVENT_ROAMING_OFF = 270348;
    public static final int EVENT_ROAMING_ON = 270347;
    public static final int EVENT_ROAMING_TYPE_CHANGED = 270854;
    public static final int EVENT_SETUP_DATA_WHEN_LOADED = 270840;
    public static final int EVENT_SET_CARRIER_DATA_ENABLED = 270382;
    public static final int EVENT_SET_INTERNAL_DATA_ENABLE = 270363;
    public static final int EVENT_SET_RESUME = 270850;
    public static final int EVENT_TRY_SETUP_DATA = 270339;
    public static final int EVENT_VOICE_CALL_ENDED = 270344;
    public static final int EVENT_VOICE_CALL_STARTED = 270343;
    public static final int INVALID = -1;
    public static final int MTK_BASE = 270836;
    public static final String PROVISIONING_URL_KEY = "provisioningUrl";

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum Activity {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.DctConstants.Activity.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.DctConstants.Activity.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.DctConstants.Activity.<clinit>():void");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum State {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.DctConstants.State.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.DctConstants.State.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.DctConstants.State.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.DctConstants.<init>():void, dex: 
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
    public DctConstants() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.DctConstants.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.DctConstants.<init>():void");
    }
}
