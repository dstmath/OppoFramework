package sun.util.logging;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.List;

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
public class LoggingSupport {
    private static final String DEFAULT_FORMAT = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";
    private static final String FORMAT_PROP_KEY = "java.util.logging.SimpleFormatter.format";
    private static final LoggingProxy proxy = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.logging.LoggingSupport.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.logging.LoggingSupport.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.logging.LoggingSupport.<clinit>():void");
    }

    private LoggingSupport() {
    }

    public static boolean isAvailable() {
        return proxy != null;
    }

    private static void ensureAvailable() {
        if (proxy == null) {
            throw new AssertionError((Object) "Should not here");
        }
    }

    public static List<String> getLoggerNames() {
        ensureAvailable();
        return proxy.getLoggerNames();
    }

    public static String getLoggerLevel(String loggerName) {
        ensureAvailable();
        return proxy.getLoggerLevel(loggerName);
    }

    public static void setLoggerLevel(String loggerName, String levelName) {
        ensureAvailable();
        proxy.setLoggerLevel(loggerName, levelName);
    }

    public static String getParentLoggerName(String loggerName) {
        ensureAvailable();
        return proxy.getParentLoggerName(loggerName);
    }

    public static Object getLogger(String name) {
        ensureAvailable();
        return proxy.getLogger(name);
    }

    public static Object getLevel(Object logger) {
        ensureAvailable();
        return proxy.getLevel(logger);
    }

    public static void setLevel(Object logger, Object newLevel) {
        ensureAvailable();
        proxy.setLevel(logger, newLevel);
    }

    public static boolean isLoggable(Object logger, Object level) {
        ensureAvailable();
        return proxy.isLoggable(logger, level);
    }

    public static void log(Object logger, Object level, String msg) {
        ensureAvailable();
        proxy.log(logger, level, msg);
    }

    public static void log(Object logger, Object level, String msg, Throwable t) {
        ensureAvailable();
        proxy.log(logger, level, msg, t);
    }

    public static void log(Object logger, Object level, String msg, Object... params) {
        ensureAvailable();
        proxy.log(logger, level, msg, params);
    }

    public static Object parseLevel(String levelName) {
        ensureAvailable();
        return proxy.parseLevel(levelName);
    }

    public static String getLevelName(Object level) {
        ensureAvailable();
        return proxy.getLevelName(level);
    }

    public static int getLevelValue(Object level) {
        ensureAvailable();
        return proxy.getLevelValue(level);
    }

    public static String getSimpleFormat() {
        return getSimpleFormat(true);
    }

    static String getSimpleFormat(boolean useProxy) {
        String format = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(LoggingSupport.FORMAT_PROP_KEY);
            }
        });
        if (useProxy && proxy != null && format == null) {
            format = proxy.getProperty(FORMAT_PROP_KEY);
        }
        if (format == null) {
            return DEFAULT_FORMAT;
        }
        try {
            Object[] objArr = new Object[6];
            objArr[0] = new Date();
            objArr[1] = "";
            objArr[2] = "";
            objArr[3] = "";
            objArr[4] = "";
            objArr[5] = "";
            String.format(format, objArr);
            return format;
        } catch (IllegalArgumentException e) {
            return DEFAULT_FORMAT;
        }
    }
}
