package android_maps_conflict_avoidance.com.google.debug;

import java.util.Hashtable;

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
public class Log {
    private static final String[] LEVEL_NAMES = null;
    private static final Logger logger = null;
    private static final Hashtable timers = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.debug.Log.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.debug.Log.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.debug.Log.<clinit>():void");
    }

    private static Class logger() {
        String name = "android_maps_conflict_avoidance.com.google.debug.StdoutLogger";
        try {
            if (DebugUtil.isAntPropertyExpanded("android_maps_conflict_avoidance.com.google.debug.StdoutLogger")) {
                return Class.forName("android_maps_conflict_avoidance.com.google.debug.StdoutLogger");
            }
            String sysName = System.getProperty("LOGGER");
            if (sysName != null) {
                return Class.forName(sysName);
            }
            System.err.println("WARNING: Missing logger class - using default logger com.google.debug.StdoutLogger");
            System.err.println("         For Ant: Specify the logger class using the LOGGER property");
            System.err.println("         For Bolide: Specify the logger class using constant injection");
            System.err.println("         For J2SE:  Specify the logger class via the LOGGER system property");
            System.err.println("         See JavaDoc or source of com.google.debug.Log.");
            return Class.forName("android_maps_conflict_avoidance.com.google.debug.StdoutLogger");
        } catch (ClassNotFoundException e) {
            throw new Error("Missing logger class com.google.debug.StdoutLogger");
        }
    }

    public static void logThrowable(Object message, Throwable exception) {
        xlogThrowable(message, exception, null, null, -1);
    }

    public static void xlogThrowable(Object message, Throwable exception, String className, String methodName, int lineNumber) {
        xlogThrowable(message, exception, 5, className, methodName, lineNumber);
    }

    public static void xlogThrowable(Object message, Throwable exception, int logLevel, String className, String methodName, int lineNumber) {
        logger.logThrowable(message, exception, logLevel, className, methodName, lineNumber);
    }
}
