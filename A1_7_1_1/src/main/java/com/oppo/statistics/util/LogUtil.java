package com.oppo.statistics.util;

import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;

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
    private static boolean D = false;
    private static boolean E = false;
    private static boolean I = false;
    public static final String TAG = "com.coloros.statistics--";
    private static boolean V;
    private static boolean W;
    private static boolean isDebug;
    public static boolean isDebugMode;
    private static String seprateor;
    private static String special;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.statistics.util.LogUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.statistics.util.LogUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.statistics.util.LogUtil.<clinit>():void");
    }

    public static void e(String tag, Throwable e) {
        if (isDebug && E) {
            Log.e(tag, e.toString());
        }
    }

    public static void e(Exception e) {
        if (isDebug && E) {
            e.printStackTrace();
        }
    }

    public static void v(String tag, String debugInfo) {
        if (isDebug && V) {
            Log.v(tag, special + seprateor + debugInfo);
        }
    }

    public static void d(String tag, String debugInfo) {
        if (isDebug && D) {
            Log.d(tag, special + seprateor + debugInfo);
        }
    }

    public static void i(String tag, String debugInfo) {
        if (isDebug && I) {
            Log.i(tag, special + seprateor + debugInfo);
        }
    }

    public static void w(String tag, String debugInfo) {
        if (isDebug && W) {
            Log.w(tag, special + seprateor + debugInfo);
        }
    }

    public static void e(String tag, String debugInfo) {
        if (isDebug && E) {
            Log.e(tag, special + seprateor + debugInfo);
        }
    }

    public static void v(String debugInfo) {
        if (isDebug && V) {
            Log.v(TAG, special + seprateor + debugInfo);
        }
    }

    public static void d(String debugInfo) {
        if (isDebug && D) {
            Log.d(TAG, special + seprateor + debugInfo);
        }
    }

    public static void i(String debugInfo) {
        if (isDebug && I) {
            Log.i(TAG, special + seprateor + debugInfo);
        }
    }

    public static void w(String debugInfo) {
        if (isDebug && W) {
            Log.w(TAG, special + seprateor + debugInfo);
        }
    }

    public static void e(String debugInfo) {
        if (isDebug && E) {
            Log.e(TAG, special + seprateor + debugInfo);
        }
    }

    public static String getSpecial() {
        return special;
    }

    public static void setSpecial(String special) {
        special = special;
    }

    public static boolean isV() {
        return V;
    }

    public static void setV(boolean v) {
        V = v;
    }

    public static boolean isD() {
        return D;
    }

    public static void setD(boolean d) {
        D = d;
    }

    public static boolean isI() {
        return I;
    }

    public static void setI(boolean i) {
        I = i;
    }

    public static boolean isW() {
        return W;
    }

    public static void setW(boolean w) {
        W = w;
    }

    public static boolean isE() {
        return E;
    }

    public static void setE(boolean e) {
        E = e;
    }

    public static void setDebugs(boolean b) {
        isDebug = b;
        if (isDebug && isDebugMode) {
            V = true;
            D = true;
            I = true;
            W = true;
            E = true;
            return;
        }
        V = false;
        D = false;
        I = false;
        W = false;
        E = false;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static String getSeprateor() {
        return seprateor;
    }

    public static void setSeprateor(String seprateor) {
        seprateor = seprateor;
    }

    public static void reocrdExceptionInfo(Throwable e) {
        File file = new File("/data/data/com.nearme.statistics.rom/exception_info.txt");
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null) {
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    file.createNewFile();
                } else {
                    return;
                }
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            StringWriter strWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(strWriter));
            raf.write(strWriter.toString().getBytes());
            raf.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
