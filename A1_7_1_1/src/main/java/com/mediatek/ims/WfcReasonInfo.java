package com.mediatek.ims;

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
public class WfcReasonInfo {
    public static final int CODE_UNSPECIFIED = 999;
    public static final int CODE_WFC_403_AUTH_SCHEME_UNSUPPORTED = 1604;
    public static final int CODE_WFC_403_HANDSET_BLACKLISTED = 1605;
    public static final int CODE_WFC_403_MISMATCH_IDENTITIES = 1603;
    public static final int CODE_WFC_403_ROAMING_NOT_ALLOWED = 1602;
    public static final int CODE_WFC_403_UNKNOWN_USER = 1601;
    public static final int CODE_WFC_911_MISSING = 1701;
    public static final int CODE_WFC_ANY_OTHER_CONN_ERROR = 1407;
    public static final int CODE_WFC_DEFAULT = 100;
    public static final int CODE_WFC_DNS_RECV_NAPTR_QUERY_RSP_ERROR = 1201;
    public static final int CODE_WFC_DNS_RECV_RSP_QUERY_ERROR = 1203;
    public static final int CODE_WFC_DNS_RECV_RSP_SRV_QUERY_ERROR = 1202;
    public static final int CODE_WFC_DNS_RESOLVE_FQDN_ERROR = 1041;
    public static final int CODE_WFC_EPDG_CON_OR_LOCAL_OR_NULL_PTR_ERROR = 1081;
    public static final int CODE_WFC_EPDG_IPSEC_SETUP_ERROR = 1082;
    public static final int CODE_WFC_INCORRECT_SIM_CARD_ERROR = 1301;
    public static final int CODE_WFC_INTERNAL_SERVER_ERROR = 1406;
    public static final int CODE_WFC_LOCAL_OR_NULL_PTR_ERROR = 1401;
    public static final int CODE_WFC_NO_AVAILABLE_QUALIFIED_MOBILE_NETWORK = 2004;
    public static final int CODE_WFC_RNS_ALLOWED_RADIO_DENY = 2006;
    public static final int CODE_WFC_RNS_ALLOWED_RADIO_NONE = 2007;
    public static final int CODE_WFC_SERVER_CERT_INVALID_ERROR = 1504;
    public static final int CODE_WFC_SERVER_CERT_VALIDATION_ERROR = 1501;
    public static final int CODE_WFC_SERVER_IPSEC_CERT_INVALID_ERROR = 1111;
    public static final int CODE_WFC_SERVER_IPSEC_CERT_VALIDATION_ERROR = 1101;
    public static final int CODE_WFC_SUCCESS = 99;
    public static final int CODE_WFC_TLS_CONN_ERROR = 1405;
    public static final int CODE_WFC_UNABLE_TO_COMPLETE_CALL = 2003;
    public static final int CODE_WFC_UNABLE_TO_COMPLETE_CALL_CD = 2005;
    public static final int CODE_WFC_WIFI_SIGNAL_LOST = 2001;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.ims.WfcReasonInfo.<init>():void, dex: 
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
    public WfcReasonInfo() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.ims.WfcReasonInfo.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.WfcReasonInfo.<init>():void");
    }

    public static int getImsStatusCodeString(int status) {
        switch (status) {
            case CODE_WFC_SUCCESS /*99*/:
                return 134545675;
            case CODE_WFC_DEFAULT /*100*/:
                return 134545676;
            case CODE_WFC_DNS_RESOLVE_FQDN_ERROR /*1041*/:
                return 134545682;
            case CODE_WFC_EPDG_CON_OR_LOCAL_OR_NULL_PTR_ERROR /*1081*/:
                return 134545685;
            case CODE_WFC_EPDG_IPSEC_SETUP_ERROR /*1082*/:
                return 134545686;
            case CODE_WFC_SERVER_IPSEC_CERT_VALIDATION_ERROR /*1101*/:
                return 134545691;
            case CODE_WFC_SERVER_IPSEC_CERT_INVALID_ERROR /*1111*/:
                return 134545692;
            case CODE_WFC_DNS_RECV_NAPTR_QUERY_RSP_ERROR /*1201*/:
                return 134545679;
            case CODE_WFC_DNS_RECV_RSP_SRV_QUERY_ERROR /*1202*/:
                return 134545680;
            case CODE_WFC_DNS_RECV_RSP_QUERY_ERROR /*1203*/:
                return 134545681;
            case CODE_WFC_INCORRECT_SIM_CARD_ERROR /*1301*/:
                return 134545683;
            case CODE_WFC_LOCAL_OR_NULL_PTR_ERROR /*1401*/:
                return 134545684;
            case CODE_WFC_TLS_CONN_ERROR /*1405*/:
                return 134545687;
            case CODE_WFC_INTERNAL_SERVER_ERROR /*1406*/:
                return 134545688;
            case CODE_WFC_SERVER_CERT_VALIDATION_ERROR /*1501*/:
                return 134545690;
            case CODE_WFC_SERVER_CERT_INVALID_ERROR /*1504*/:
                return 134545693;
            case CODE_WFC_403_UNKNOWN_USER /*1601*/:
                return 134545694;
            case CODE_WFC_403_ROAMING_NOT_ALLOWED /*1602*/:
                return 134545695;
            case CODE_WFC_403_MISMATCH_IDENTITIES /*1603*/:
                return 134545696;
            case CODE_WFC_403_AUTH_SCHEME_UNSUPPORTED /*1604*/:
                return 134545697;
            case CODE_WFC_403_HANDSET_BLACKLISTED /*1605*/:
                return 134545698;
            case CODE_WFC_911_MISSING /*1701*/:
                return 134545699;
            case CODE_WFC_RNS_ALLOWED_RADIO_DENY /*2006*/:
                return 134545677;
            case CODE_WFC_RNS_ALLOWED_RADIO_NONE /*2007*/:
                return 134545678;
            default:
                return 134545689;
        }
    }
}
