package com.android.server.am;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class ActivityManagerDebugConfig {
    static final boolean APPEND_CATEGORY_NAME = false;
    static boolean DEBUG_ADD_REMOVE = false;
    static boolean DEBUG_ALL = false;
    static boolean DEBUG_ALL_ACTIVITIES = false;
    public static boolean DEBUG_AMS = false;
    static boolean DEBUG_ANR = false;
    static boolean DEBUG_APP = false;
    static boolean DEBUG_BACKUP = false;
    static boolean DEBUG_BROADCAST = false;
    static boolean DEBUG_BROADCAST_BACKGROUND = false;
    static boolean DEBUG_BROADCAST_LIGHT = false;
    static boolean DEBUG_CLEANUP = false;
    static boolean DEBUG_CONFIGURATION = false;
    static boolean DEBUG_CONTAINERS = false;
    static boolean DEBUG_FOCUS = false;
    static boolean DEBUG_FSTRIM = false;
    static boolean DEBUG_IDLE = false;
    static boolean DEBUG_IMMERSIVE = false;
    static boolean DEBUG_JUNK = false;
    static boolean DEBUG_LOCKSCREEN = false;
    static boolean DEBUG_LOCKTASK = false;
    static boolean DEBUG_LRU = false;
    static boolean DEBUG_MU = false;
    static boolean DEBUG_MULTIWINDOW = false;
    static boolean DEBUG_MULTI_WINDOW = false;
    static boolean DEBUG_OOM_ADJ = false;
    static boolean DEBUG_PAUSE = false;
    static boolean DEBUG_PERMISSION = false;
    static boolean DEBUG_PERMISSIONS_REVIEW = false;
    static boolean DEBUG_POWER = false;
    static boolean DEBUG_POWER_QUICK = false;
    static boolean DEBUG_PROCESSES = false;
    static boolean DEBUG_PROCESS_OBSERVERS = false;
    static boolean DEBUG_PROVIDER = false;
    static boolean DEBUG_PSS = false;
    static boolean DEBUG_RECENTS = false;
    static boolean DEBUG_RELEASE = false;
    static boolean DEBUG_RESULTS = false;
    static boolean DEBUG_SAVED_STATE = false;
    static boolean DEBUG_SCREENSHOTS = false;
    static boolean DEBUG_SERVICE = false;
    static boolean DEBUG_SERVICE_EXECUTING = false;
    static boolean DEBUG_STACK = false;
    static boolean DEBUG_STATES = false;
    static boolean DEBUG_SWITCH = false;
    static boolean DEBUG_TASKS = false;
    static boolean DEBUG_TASK_RETURNTO = false;
    static boolean DEBUG_THUMBNAILS = false;
    static boolean DEBUG_TRANSITION = false;
    static boolean DEBUG_UID_OBSERVERS = false;
    static boolean DEBUG_URI_PERMISSION = false;
    static boolean DEBUG_USAGE_STATS = false;
    static boolean DEBUG_USER_LEAVING = false;
    private static final boolean DEBUG_VERSION = false;
    static boolean DEBUG_VISIBILITY = false;
    static boolean DEBUG_VISIBLE_BEHIND = false;
    static boolean DEBUG_WHITELISTS = false;
    static final String POSTFIX_ADD_REMOVE = null;
    static final String POSTFIX_APP = null;
    static final String POSTFIX_BACKUP = null;
    static final String POSTFIX_BROADCAST = null;
    static final String POSTFIX_CLEANUP = null;
    static final String POSTFIX_CONFIGURATION = null;
    static final String POSTFIX_CONTAINERS = null;
    static final String POSTFIX_FOCUS = null;
    static final String POSTFIX_IDLE = null;
    static final String POSTFIX_IMMERSIVE = null;
    static final String POSTFIX_LOCKSCREEN = null;
    static final String POSTFIX_LOCKTASK = null;
    static final String POSTFIX_LRU = null;
    static final String POSTFIX_MU = "_MU";
    static final String POSTFIX_OOM_ADJ = null;
    static final String POSTFIX_PAUSE = null;
    static final String POSTFIX_POWER = null;
    static final String POSTFIX_PROCESSES = null;
    static final String POSTFIX_PROCESS_OBSERVERS = null;
    static final String POSTFIX_PROVIDER = null;
    static final String POSTFIX_PSS = null;
    static final String POSTFIX_RECENTS = null;
    static final String POSTFIX_RELEASE = null;
    static final String POSTFIX_RESULTS = null;
    static final String POSTFIX_SAVED_STATE = null;
    static final String POSTFIX_SCREENSHOTS = null;
    static final String POSTFIX_SERVICE = null;
    static final String POSTFIX_SERVICE_EXECUTING = null;
    static final String POSTFIX_STACK = null;
    static final String POSTFIX_STATES = null;
    static final String POSTFIX_SWITCH = null;
    static final String POSTFIX_TASKS = null;
    static final String POSTFIX_THUMBNAILS = null;
    static final String POSTFIX_TRANSITION = null;
    static final String POSTFIX_UID_OBSERVERS = null;
    static final String POSTFIX_URI_PERMISSION = null;
    static final String POSTFIX_USER_LEAVING = null;
    static final String POSTFIX_VISIBILITY = null;
    static final String POSTFIX_VISIBLE_BEHIND = null;
    static final String TAG_AM = "ActivityManager";
    static final boolean TAG_WITH_CLASS_NAME = false;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityManagerDebugConfig.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityManagerDebugConfig.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityManagerDebugConfig.<clinit>():void");
    }

    ActivityManagerDebugConfig() {
    }
}
