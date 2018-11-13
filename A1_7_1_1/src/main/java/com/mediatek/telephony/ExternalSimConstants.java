package com.mediatek.telephony;

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
public class ExternalSimConstants {
    public static final int MSG_ID_EVENT_REQUEST = 3;
    public static final int MSG_ID_EVENT_RESPONSE = 1003;
    public static final int MSG_ID_FINALIZATION_REQUEST = 8;
    public static final int MSG_ID_FINALIZATION_RESPONSE = 1008;
    public static final int MSG_ID_GET_PLATFORM_CAPABILITY_REQUEST = 2;
    public static final int MSG_ID_GET_PLATFORM_CAPABILITY_RESPONSE = 1002;
    public static final int MSG_ID_GET_SERVICE_STATE_REQUEST = 7;
    public static final int MSG_ID_GET_SERVICE_STATE_RESPONSE = 1007;
    public static final int MSG_ID_INITIALIZATION_REQUEST = 1;
    public static final int MSG_ID_INITIALIZATION_RESPONSE = 1001;
    public static final int MSG_ID_UICC_APDU_REQUEST = 1005;
    public static final int MSG_ID_UICC_APDU_RESPONSE = 5;
    public static final int MSG_ID_UICC_POWER_DOWN_REQUEST = 1006;
    public static final int MSG_ID_UICC_POWER_DOWN_RESPONSE = 6;
    public static final int MSG_ID_UICC_RESET_REQUEST = 1004;
    public static final int MSG_ID_UICC_RESET_RESPONSE = 4;
    public static final int MULTISIM_CONFIG_DSDA = 2;
    public static final int MULTISIM_CONFIG_DSDS = 1;
    public static final int MULTISIM_CONFIG_TSTS = 3;
    public static final int MULTISIM_CONFIG_UNKNOWN = 0;
    public static final int PERSIST_TYPE_DISABLE = 0;
    public static final int PERSIST_TYPE_ENABLED = 1;
    public static final int REQUEST_TYPE_DISABLE_EXTERNAL_SIM = 2;
    public static final int REQUEST_TYPE_ENABLE_EXTERNAL_SIM = 1;
    public static final int REQUEST_TYPE_PLUG_IN = 4;
    public static final int REQUEST_TYPE_PLUG_OUT = 3;
    public static final int REQUEST_TYPE_SET_PERSIST_TYPE = 5;
    public static final int RESPONSE_RESULT_GENERIC_ERROR = -1;
    public static final int RESPONSE_RESULT_OK = 0;
    public static final int RESPONSE_RESULT_PLATFORM_NOT_READY = -2;
    public static final int RESPONSE_RESULT_PLATFORM_RETRYING = -3;
    public static final int SIM_TYPE_LOCAL_SIM = 1;
    public static final int SIM_TYPE_REMOTE_SIM = 2;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.telephony.ExternalSimConstants.<init>():void, dex: 
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
    public ExternalSimConstants() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.telephony.ExternalSimConstants.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.ExternalSimConstants.<init>():void");
    }
}
