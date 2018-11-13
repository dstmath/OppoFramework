package com.mediatek.common.voicecommand;

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
public abstract class VoiceCommandListener {
    private static final int ACTION_COMMON_INDEX = 1;
    public static final String ACTION_EXTRA_RESULT = "Result";
    public static final int ACTION_EXTRA_RESULT_ERROR = 10;
    public static final String ACTION_EXTRA_RESULT_INFO = "Result_Info";
    public static final String ACTION_EXTRA_RESULT_INFO1 = "Reslut_Info1";
    public static final int ACTION_EXTRA_RESULT_SUCCESS = 1;
    public static final String ACTION_EXTRA_SEND = "Send";
    public static final String ACTION_EXTRA_SEND_INFO = "Send_Info";
    public static final String ACTION_EXTRA_SEND_INFO1 = "Send_Info1";
    public static final String ACTION_EXTRA_SEND_INFO2 = "Send_Info2";
    public static final int ACTION_MAIN_VOICE_COMMON = 1;
    public static final int ACTION_MAIN_VOICE_CONTACTS = 5;
    public static final int ACTION_MAIN_VOICE_RECOGNITION = 4;
    public static final int ACTION_MAIN_VOICE_TRAINING = 3;
    public static final int ACTION_MAIN_VOICE_UI = 2;
    public static final int ACTION_MAIN_VOICE_WAKEUP = 6;
    public static final int ACTION_VOICE_COMMON_COMMAND_PATH = 2;
    public static final int ACTION_VOICE_COMMON_KEYWORD = 1;
    public static final int ACTION_VOICE_COMMON_PROCESS_STATE = 3;
    public static final int ACTION_VOICE_CONTACTS_DISABLE = 4;
    public static final int ACTION_VOICE_CONTACTS_ENABLE = 3;
    public static final int ACTION_VOICE_CONTACTS_INTENSITY = 5;
    public static final int ACTION_VOICE_CONTACTS_NAME = 8;
    public static final int ACTION_VOICE_CONTACTS_NOTIFY = 7;
    public static final int ACTION_VOICE_CONTACTS_ORIENTATION = 11;
    public static final int ACTION_VOICE_CONTACTS_RECOGNITION_DISABLE = 13;
    public static final int ACTION_VOICE_CONTACTS_RECOGNITION_ENABLE = 12;
    public static final int ACTION_VOICE_CONTACTS_SEARCH_COUNT = 10;
    public static final int ACTION_VOICE_CONTACTS_SELECTED = 6;
    public static final int ACTION_VOICE_CONTACTS_SPEECH_DETECTED = 9;
    public static final int ACTION_VOICE_CONTACTS_START = 1;
    public static final int ACTION_VOICE_CONTACTS_STOP = 2;
    public static final int ACTION_VOICE_RECOGNITION_INTENSITY = 2;
    public static final int ACTION_VOICE_RECOGNITION_NOTIFY = 3;
    public static final int ACTION_VOICE_RECOGNITION_START = 1;
    public static final int ACTION_VOICE_TRAINING_CONTINUE = 9;
    public static final int ACTION_VOICE_TRAINING_FINISH = 8;
    public static final int ACTION_VOICE_TRAINING_INTENSITY = 3;
    public static final int ACTION_VOICE_TRAINING_MODIFY = 7;
    public static final int ACTION_VOICE_TRAINING_NOTIFY = 5;
    public static final int ACTION_VOICE_TRAINING_PASSWORD_FILE = 4;
    public static final int ACTION_VOICE_TRAINING_RESET = 6;
    public static final int ACTION_VOICE_TRAINING_START = 1;
    public static final int ACTION_VOICE_TRAINING_STOP = 2;
    public static final int ACTION_VOICE_UI_DISALBE = 4;
    public static final int ACTION_VOICE_UI_ENABLE = 3;
    public static final int ACTION_VOICE_UI_NOTIFY = 5;
    public static final int ACTION_VOICE_UI_START = 1;
    public static final int ACTION_VOICE_UI_STOP = 2;
    public static final int ACTION_VOICE_WAKEUP_COMMAND_STATUS = 7;
    public static final int ACTION_VOICE_WAKEUP_DISABLE = 3;
    public static final int ACTION_VOICE_WAKEUP_ENABLE = 2;
    public static final int ACTION_VOICE_WAKEUP_INIT = 5;
    public static final int ACTION_VOICE_WAKEUP_IPO_SHUTDOWN_STATUS = 8;
    public static final int ACTION_VOICE_WAKEUP_MODE = 6;
    public static final int ACTION_VOICE_WAKEUP_NOTIFY = 4;
    public static final int ACTION_VOICE_WAKEUP_START = 1;
    public static final String VOICE_COMMAND_SERVICE = "voicecommand";
    private static final int VOICE_ERROR_COMMON = 1000;
    public static final int VOICE_ERROR_COMMON_ILLEGAL_PROCESS = 1005;
    public static final int VOICE_ERROR_COMMON_INVALID_ACTION = 1007;
    public static final int VOICE_ERROR_COMMON_INVALID_DATA = 1008;
    public static final int VOICE_ERROR_COMMON_NOTIFY_FAIL = 1009;
    public static final int VOICE_ERROR_COMMON_NO_PERMISSION = 1002;
    public static final int VOICE_ERROR_COMMON_PROCESS_OFF = 1001;
    public static final int VOICE_ERROR_COMMON_REGISTERED = 1003;
    public static final int VOICE_ERROR_COMMON_SERVICE = 1006;
    public static final int VOICE_ERROR_COMMON_UNREGISTER = 1004;
    private static final int VOICE_ERROR_CONTACTS = 400;
    public static final int VOICE_ERROR_CONTACTS_SEND_INVALID = 402;
    public static final int VOICE_ERROR_CONTACTS_VOICE_INVALID = 401;
    private static final int VOICE_ERROR_RECOGNIZE = 0;
    public static final int VOICE_ERROR_RECOGNIZE_DENIED = 1;
    public static final int VOICE_ERROR_RECOGNIZE_LOWLY = 3;
    public static final int VOICE_ERROR_RECOGNIZE_NOISY = 2;
    private static final int VOICE_ERROR_SETTING = 200;
    private static final int VOICE_ERROR_TRAINING = 100;
    public static final int VOICE_ERROR_TRAINING_NOISY = 102;
    public static final int VOICE_ERROR_TRAINING_NOT_ENOUGH = 101;
    public static final int VOICE_ERROR_TRAINING_PASSWORD_DIFF = 103;
    public static final int VOICE_ERROR_TRAINING_PASSWORD_EXIST = 104;
    private static final int VOICE_ERROR_UI = 300;
    public static final int VOICE_ERROR_UI_INVALID = 301;
    public static final int VOICE_NO_ERROR = 0;
    public static final String VOICE_SERVICE_ACTION = "com.mediatek.voicecommand";
    public static final String VOICE_SERVICE_CATEGORY = "com.mediatek.nativeservice";
    public static final String VOICE_SERVICE_PACKAGE_NAME = "com.mediatek.voicecommand";
    public static final String VOICE_TRAINING_SERVICE_ACTION = "com.mediatek.intent.action.bindEnrollmentService";
    public static final String VOICE_TRAINING_SERVICE_PACKAGE_NAME = "com.mediatek.voicecommand.vis";
    public static final String VOICE_WAKEUP_ACTIVTY_ACTION = "com.mediatek.voicecommand.VOW_INTERACT";
    public static final int VOICE_WAKEUP_MODE_SPEAKER_DEPENDENT = 2;
    public static final int VOICE_WAKEUP_MODE_SPEAKER_INDEPENDENT = 1;
    public static final int VOICE_WAKEUP_MODE_UNLOCK = 0;
    public static final String VOICE_WAKEUP_SERVICE_ACTION = "com.mediatek.voicecommand.VoiceWakeupInteractionService";
    public static final int VOICE_WAKEUP_STATUS_COMMAND_CHECKED = 2;
    public static final int VOICE_WAKEUP_STATUS_COMMAND_UNCHECKED = 1;
    public static final int VOICE_WAKEUP_STATUS_NOCOMMAND_UNCHECKED = 0;
    public static final String VOW_ENROLLMENT_BCP47_LOCALE = "en-US";
    public static final String VOW_ENROLLMENT_TEXT = "Hello There";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.voicecommand.VoiceCommandListener.<init>():void, dex: 
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
    public VoiceCommandListener() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.voicecommand.VoiceCommandListener.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.voicecommand.VoiceCommandListener.<init>():void");
    }
}
