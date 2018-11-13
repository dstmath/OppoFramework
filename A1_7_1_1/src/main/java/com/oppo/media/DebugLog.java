package com.oppo.media;

import android.util.Log;

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
public final class DebugLog {
    private static final boolean DEBUG_MODE = false;
    private static final boolean LOGD = true;
    private static final boolean LOGE = true;
    private static final boolean LOGI = true;
    private static final boolean LOGV = true;
    private static final boolean LOGW = true;
    private static final boolean SHOWDETAILINFO = false;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.media.DebugLog.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.media.DebugLog.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.DebugLog.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.media.DebugLog.<init>():void, dex: 
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
    public DebugLog() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.media.DebugLog.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.DebugLog.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.DebugLog.getFunctionName():java.lang.String, dex: 
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
    private static java.lang.String getFunctionName() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.DebugLog.getFunctionName():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.DebugLog.getFunctionName():java.lang.String");
    }

    public static int v(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.v(tag, msg);
        }
        return -1;
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.v(tag, msg, tr);
        }
        return -1;
    }

    public static int v(String tag, boolean debug, String msg) {
        if (debug && DEBUG_MODE) {
            return v(tag, msg);
        }
        return -1;
    }

    public static int d(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.d(tag, msg);
        }
        return -1;
    }

    public static int d(String tag, boolean debug, String msg) {
        if (debug && DEBUG_MODE) {
            return d(tag, msg);
        }
        return -1;
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.d(tag, msg, tr);
        }
        return -1;
    }

    public static int i(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.i(tag, msg);
        }
        return -1;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.i(tag, msg, tr);
        }
        return -1;
    }

    public static int w(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.w(tag, msg);
        }
        return -1;
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.w(tag, msg, tr);
        }
        return -1;
    }

    public static int e(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.e(tag, msg);
        }
        return -1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.e(tag, msg, tr);
        }
        return -1;
    }
}
