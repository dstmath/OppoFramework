package android.icu.impl;

import android.icu.util.VersionInfo;

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
public final class ICUDebug {
    private static boolean debug;
    private static boolean help;
    public static final boolean isJDK14OrHigher = false;
    public static final VersionInfo javaVersion = null;
    public static final String javaVersionString = null;
    private static String params;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUDebug.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUDebug.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUDebug.<clinit>():void");
    }

    public static VersionInfo getInstanceLenient(String s) {
        int[] ver = new int[4];
        boolean numeric = false;
        int i = 0;
        int vidx = 0;
        while (i < s.length()) {
            int i2 = i + 1;
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                if (!numeric) {
                    continue;
                } else if (vidx == 3) {
                    i = i2;
                    break;
                } else {
                    numeric = false;
                    vidx++;
                }
            } else if (numeric) {
                ver[vidx] = (ver[vidx] * 10) + (c - 48);
                if (ver[vidx] > 255) {
                    ver[vidx] = 0;
                    i = i2;
                    break;
                }
            } else {
                numeric = true;
                ver[vidx] = c - 48;
            }
            i = i2;
        }
        return VersionInfo.getInstance(ver[0], ver[1], ver[2], ver[3]);
    }

    public static boolean enabled() {
        return debug;
    }

    public static boolean enabled(String arg) {
        if (!debug) {
            return false;
        }
        boolean result = params.indexOf(arg) != -1;
        if (help) {
            System.out.println("\nICUDebug.enabled(" + arg + ") = " + result);
        }
        return result;
    }

    public static String value(String arg) {
        String result = "false";
        if (debug) {
            int index = params.indexOf(arg);
            if (index != -1) {
                index += arg.length();
                if (params.length() <= index || params.charAt(index) != '=') {
                    result = "true";
                } else {
                    index++;
                    int limit = params.indexOf(",", index);
                    String str = params;
                    if (limit == -1) {
                        limit = params.length();
                    }
                    result = str.substring(index, limit);
                }
            }
            if (help) {
                System.out.println("\nICUDebug.value(" + arg + ") = " + result);
            }
        }
        return result;
    }
}
