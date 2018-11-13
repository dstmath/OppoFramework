package sun.misc;

import java.lang.Thread.State;
import java.util.Properties;

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
public class VM {
    private static final int JVMTI_THREAD_STATE_ALIVE = 1;
    private static final int JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 1024;
    private static final int JVMTI_THREAD_STATE_RUNNABLE = 4;
    private static final int JVMTI_THREAD_STATE_TERMINATED = 2;
    private static final int JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 16;
    private static final int JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 32;
    @Deprecated
    public static final int STATE_GREEN = 1;
    @Deprecated
    public static final int STATE_RED = 3;
    @Deprecated
    public static final int STATE_YELLOW = 2;
    private static boolean allowArraySyntax;
    private static boolean allowGetCallerClass;
    private static volatile boolean booted;
    private static boolean defaultAllowArraySyntax;
    private static long directMemory;
    private static volatile int finalRefCount;
    private static boolean pageAlignDirectMemory;
    private static volatile int peakFinalRefCount;
    private static final Properties savedProps = null;
    private static boolean suspended;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.misc.VM.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.misc.VM.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.VM.<clinit>():void");
    }

    @Deprecated
    public static boolean threadsSuspended() {
        return suspended;
    }

    public static boolean allowThreadSuspension(ThreadGroup g, boolean b) {
        return g.allowThreadSuspension(b);
    }

    @Deprecated
    public static boolean suspendThreads() {
        suspended = true;
        return true;
    }

    @Deprecated
    public static void unsuspendThreads() {
        suspended = false;
    }

    @Deprecated
    public static void unsuspendSomeThreads() {
    }

    @Deprecated
    public static final int getState() {
        return 1;
    }

    @Deprecated
    public static void asChange(int as_old, int as_new) {
    }

    @Deprecated
    public static void asChange_otherthread(int as_old, int as_new) {
    }

    public static void booted() {
        booted = true;
    }

    public static boolean isBooted() {
        return booted;
    }

    public static long maxDirectMemory() {
        return directMemory;
    }

    public static boolean isDirectMemoryPageAligned() {
        return pageAlignDirectMemory;
    }

    public static boolean allowArraySyntax() {
        return allowArraySyntax;
    }

    public static boolean allowGetCallerClass() {
        return allowGetCallerClass;
    }

    public static String getSavedProperty(String key) {
        return savedProps.getProperty(key);
    }

    public static void saveAndRemoveProperties(Properties props) {
        if (booted) {
            throw new IllegalStateException("System initialization has completed");
        }
        boolean z;
        savedProps.putAll(props);
        String s = (String) props.remove("sun.nio.MaxDirectMemorySize");
        if (s != null) {
            if (s.equals("-1")) {
                directMemory = Runtime.getRuntime().maxMemory();
            } else {
                long l = Long.parseLong(s);
                if (l > -1) {
                    directMemory = l;
                }
            }
        }
        if ("true".equals((String) props.remove("sun.nio.PageAlignDirectMemory"))) {
            pageAlignDirectMemory = true;
        }
        s = props.getProperty("sun.lang.ClassLoader.allowArraySyntax");
        if (s == null) {
            z = defaultAllowArraySyntax;
        } else {
            z = Boolean.parseBoolean(s);
        }
        allowArraySyntax = z;
        s = props.getProperty("jdk.reflect.allowGetCallerClass");
        if (s == null || s.isEmpty() || Boolean.parseBoolean(s)) {
            z = true;
        } else {
            z = Boolean.valueOf(props.getProperty("jdk.logging.allowStackWalkSearch")).booleanValue();
        }
        allowGetCallerClass = z;
        props.remove("java.lang.Integer.IntegerCache.high");
        props.remove("sun.zip.disableMemoryMapping");
        props.remove("sun.java.launcher.diag");
    }

    public static void initializeOSEnvironment() {
    }

    public static int getFinalRefCount() {
        return finalRefCount;
    }

    public static int getPeakFinalRefCount() {
        return peakFinalRefCount;
    }

    public static void addFinalRefCount(int n) {
        finalRefCount += n;
        if (finalRefCount > peakFinalRefCount) {
            peakFinalRefCount = finalRefCount;
        }
    }

    public static State toThreadState(int threadStatus) {
        if ((threadStatus & 4) != 0) {
            return State.RUNNABLE;
        }
        if ((threadStatus & 1024) != 0) {
            return State.BLOCKED;
        }
        if ((threadStatus & 16) != 0) {
            return State.WAITING;
        }
        if ((threadStatus & 32) != 0) {
            return State.TIMED_WAITING;
        }
        if ((threadStatus & 2) != 0) {
            return State.TERMINATED;
        }
        if ((threadStatus & 1) == 0) {
            return State.NEW;
        }
        return State.RUNNABLE;
    }
}
