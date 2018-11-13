package com.android.server.wm;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WindowManagerDebugConfig {
    static boolean DEBUG = false;
    static boolean DEBUG_ADD_REMOVE = false;
    static boolean DEBUG_ANIM = false;
    static boolean DEBUG_APP_ORIENTATION = false;
    static boolean DEBUG_APP_TRANSITIONS = false;
    static boolean DEBUG_BINDER = false;
    static boolean DEBUG_BOOT = false;
    static boolean DEBUG_CONFIGURATION = false;
    static boolean DEBUG_DIM_LAYER = false;
    static boolean DEBUG_DISPLAY = false;
    static boolean DEBUG_DRAG = false;
    static boolean DEBUG_FOCUS = false;
    static boolean DEBUG_FOCUS_LIGHT = false;
    static boolean DEBUG_INPUT = false;
    static boolean DEBUG_INPUT_METHOD = false;
    static boolean DEBUG_KEEP_SCREEN_ON = false;
    static boolean DEBUG_KEYGUARD = false;
    static boolean DEBUG_LAYERS = false;
    static boolean DEBUG_LAYOUT = false;
    static boolean DEBUG_LAYOUT_REPEATS = false;
    public static boolean DEBUG_OPPO_SYSTEMBAR = false;
    static boolean DEBUG_ORIENTATION = false;
    static boolean DEBUG_POWER = false;
    static boolean DEBUG_RESIZE = false;
    static boolean DEBUG_SCREENSHOT = false;
    static boolean DEBUG_SCREEN_ON = false;
    static boolean DEBUG_STACK = false;
    static boolean DEBUG_STARTING_WINDOW = false;
    static boolean DEBUG_SURFACE_TRACE = false;
    static boolean DEBUG_TASK_MOVEMENT = false;
    static boolean DEBUG_TASK_POSITIONING = false;
    static boolean DEBUG_TOKEN_MOVEMENT = false;
    static boolean DEBUG_VISIBILITY = false;
    static boolean DEBUG_WAKEUP = false;
    static boolean DEBUG_WALLPAPER = false;
    static boolean DEBUG_WALLPAPER_LIGHT = false;
    static boolean DEBUG_WINDOW_CROP = false;
    static boolean DEBUG_WINDOW_MOVEMENT = false;
    static boolean DEBUG_WINDOW_TRACE = false;
    static boolean SHOW_LIGHT_TRANSACTIONS = false;
    static boolean SHOW_STACK_CRAWLS = false;
    static boolean SHOW_SURFACE_ALLOC = false;
    static boolean SHOW_TRANSACTIONS = false;
    static boolean SHOW_VERBOSE_TRANSACTIONS = false;
    static final String TAG_KEEP_SCREEN_ON = "DebugKeepScreenOn";
    static final boolean TAG_WITH_CLASS_NAME = false;
    static final String TAG_WM = "WindowManager";
    static final boolean enableAll = false;
    static boolean localLOGV;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowManagerDebugConfig.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowManagerDebugConfig.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowManagerDebugConfig.<clinit>():void");
    }
}
