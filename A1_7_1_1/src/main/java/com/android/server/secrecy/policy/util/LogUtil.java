package com.android.server.secrecy.policy.util;

import android.util.Log;

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
public class LogUtil {
    private static boolean DEBUG = false;
    private static boolean ERROR = false;
    private static int FILE_LOG_LEVEL = 0;
    private static boolean INFO = false;
    private static final boolean IS_DEBUGING = false;
    private static int LOGCAT_LEVEL = 0;
    static final int LOG_LEVEL_DEBUG = 2;
    static final int LOG_LEVEL_ERROR = 16;
    static final int LOG_LEVEL_INFO = 4;
    static final int LOG_LEVEL_VERBOSE = 0;
    static final int LOG_LEVEL_WARN = 8;
    private static final String LOG_TAG_STRING = "SecrecyService.LogUtil";
    private static boolean VERBOSE;
    private static boolean WARN;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.policy.util.LogUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.policy.util.LogUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.secrecy.policy.util.LogUtil.<clinit>():void");
    }

    public static boolean isDebug() {
        return IS_DEBUGING;
    }

    public static void i(String tag, String msg) {
        if (INFO) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable error) {
        if (INFO) {
            Log.i(tag, msg, error);
        }
    }

    public static void v(String tag, String msg) {
        if (VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable error) {
        if (VERBOSE) {
            Log.v(tag, msg, error);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable error) {
        if (DEBUG) {
            Log.d(tag, msg, error);
        }
    }

    public static void w(String tag, String msg) {
        if (WARN) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable error) {
        if (WARN) {
            Log.w(tag, msg, error);
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable error) {
        if (ERROR) {
            Log.e(tag, msg, error);
        }
    }

    public static void wtf(String tag, String msg) {
        if (ERROR) {
            Log.wtf(tag, msg);
        }
    }

    public static void wtf(String tag, String msg, Throwable error) {
        if (ERROR) {
            Log.wtf(tag, msg, error);
        }
    }

    public static void dynamicallyConfigLog(boolean on) {
        VERBOSE = on;
        DEBUG = on;
        INFO = on;
        WARN = on;
        Log.d(LOG_TAG_STRING, "dynamicallyConfigLog ==> " + on);
    }

    public static String getLevelString() {
        return ((((("(" + " VERBOSE = " + VERBOSE) + ", DEBUG = " + DEBUG) + ", INFO = " + INFO) + ", WARN = " + WARN) + ", ERROR = " + ERROR) + " )";
    }
}
