package com.android.internal.os;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport.CrashInfo;
import android.ddm.DdmRegister;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.DeadObjectException;
import android.os.Debug;
import android.os.IBinder;
import android.os.OppoManager;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.AndroidConfig;
import com.android.internal.os.ZygoteInit.MethodAndArgsCaller;
import com.android.internal.telephony.PhoneConstants;
import com.android.server.NetworkManagementSocketTagger;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.TimeZone;
import java.util.logging.LogManager;
import org.apache.harmony.luni.internal.util.TimezoneGetter;

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
public class RuntimeInit {
    private static final boolean DEBUG = false;
    private static final String TAG = "AndroidRuntime";
    private static boolean initialized;
    private static IBinder mApplicationObject;
    private static volatile boolean mCrashing;

    static class Arguments {
        String[] startArgs;
        String startClass;

        Arguments(String[] args) throws IllegalArgumentException {
            parseArgs(args);
        }

        private void parseArgs(String[] args) throws IllegalArgumentException {
            int curArg = 0;
            while (curArg < args.length) {
                String arg = args[curArg];
                if (!arg.equals("--")) {
                    if (!arg.startsWith("--")) {
                        break;
                    }
                    curArg++;
                } else {
                    curArg++;
                    break;
                }
            }
            if (curArg == args.length) {
                throw new IllegalArgumentException("Missing classname argument to RuntimeInit!");
            }
            int curArg2 = curArg + 1;
            this.startClass = args[curArg];
            this.startArgs = new String[(args.length - curArg2)];
            System.arraycopy(args, curArg2, this.startArgs, 0, this.startArgs.length);
        }
    }

    private static class UncaughtHandler implements UncaughtExceptionHandler {
        /* synthetic */ UncaughtHandler(UncaughtHandler uncaughtHandler) {
            this();
        }

        private UncaughtHandler() {
        }

        private void checkToInstallGr(String appPkgName) {
            if (OppoManager.DEBUG_GR) {
                Log.d(RuntimeInit.TAG, "Geloin: Our system not contains gsf, let's download.");
            }
            if (OppoManager.canShowDialog(appPkgName).booleanValue()) {
                if (OppoManager.DEBUG_GR) {
                    Log.d(RuntimeInit.TAG, "Geloin: Will leader when Request from " + appPkgName);
                }
                OppoManager.doGr(null, null, appPkgName, "DO_GR_DOWN_INSTALL");
                OppoManager.exit(appPkgName);
                return;
            }
            if (OppoManager.DEBUG_GR) {
                Log.d(RuntimeInit.TAG, "Geloin: Will not leader when Request from " + appPkgName);
            }
            OppoManager.exit(appPkgName);
        }

        private void checkToReinstall(String appPkgName) {
            if (OppoManager.DEBUG_GR) {
                Log.d(RuntimeInit.TAG, "Geloin: Has installed GSF, need reinstall.");
            }
            OppoManager.doGr(null, null, appPkgName, "DO_GR_REINSTALL");
            OppoManager.exit(appPkgName);
        }

        public void uncaughtException(Thread t, Throwable e) {
            Boolean hadCatched = Boolean.valueOf(false);
            try {
                if (RuntimeInit.mCrashing) {
                    if (SystemProperties.get("persist.mtk.aee.mode", "0").equals("4")) {
                        Log.i("RuntimeInit", "RuntimeInit: enable FTRACE");
                        RuntimeInit.executeCommand("aee -d ftraceon");
                    }
                    if (!hadCatched.booleanValue()) {
                        Process.killProcess(Process.myPid());
                        System.exit(10);
                    }
                    return;
                }
                RuntimeInit.mCrashing = true;
                if (RuntimeInit.mApplicationObject == null) {
                    RuntimeInit.Clog_e(RuntimeInit.TAG, "*** FATAL EXCEPTION IN SYSTEM PROCESS: " + t.getName(), e);
                } else {
                    StringBuilder message = new StringBuilder();
                    message.append("FATAL EXCEPTION: ").append(t.getName()).append("\n");
                    String processName = ActivityThread.currentProcessName();
                    if (processName != null) {
                        if ("com.wififreekey.wifi".equals(processName) && e.getClass().equals(ArrayIndexOutOfBoundsException.class)) {
                            RuntimeInit.Clog_e(RuntimeInit.TAG, "*** FATAL EXCEPTION IN SYSTEM PROCESS: " + t.getName(), e);
                            if (SystemProperties.get("persist.mtk.aee.mode", "0").equals("4")) {
                                Log.i("RuntimeInit", "RuntimeInit: enable FTRACE");
                                RuntimeInit.executeCommand("aee -d ftraceon");
                            }
                            if (!hadCatched.booleanValue()) {
                                Process.killProcess(Process.myPid());
                                System.exit(10);
                            }
                            return;
                        }
                        message.append("Process: ").append(processName).append(", ");
                        if (OppoManager.isInnerVersion.booleanValue()) {
                            String msg = e.getMessage();
                            if (msg != null) {
                                if (msg.contains("does not have package com.google.android.gsf") && !OppoManager.grExists().booleanValue()) {
                                    hadCatched = Boolean.valueOf(true);
                                    checkToInstallGr(processName);
                                } else if (msg.contains("without permission com.google.android.c2dm.permission.RECEIVE") || msg.contains("requires com.google.android.providers.gsf.permission.READ_GSERVICES, or grantUriPermission()") || msg.contains("requires com.google.android.providers.gsf.permission.READ_GSERVICES or com.google.android.providers.gsf.permission.WRITE_GSERVICES") || msg.contains("No Activity found to handle Intent { act=android.intent.action.VIEW dat=market://search")) {
                                    if (OppoManager.grExists().booleanValue()) {
                                        hadCatched = Boolean.valueOf(true);
                                        checkToReinstall(processName);
                                    } else {
                                        hadCatched = Boolean.valueOf(true);
                                        checkToInstallGr(processName);
                                    }
                                }
                            }
                        }
                    }
                    message.append("PID: ").append(Process.myPid());
                    if (!hadCatched.booleanValue()) {
                        RuntimeInit.Clog_e(RuntimeInit.TAG, message.toString(), e);
                    }
                }
                if (ActivityThread.currentActivityThread() != null) {
                    ActivityThread.currentActivityThread().stopProfiling();
                }
                if (!hadCatched.booleanValue()) {
                    ActivityManagerNative.getDefault().handleApplicationCrash(RuntimeInit.mApplicationObject, new CrashInfo(e));
                }
                if (SystemProperties.get("persist.mtk.aee.mode", "0").equals("4")) {
                    Log.i("RuntimeInit", "RuntimeInit: enable FTRACE");
                    RuntimeInit.executeCommand("aee -d ftraceon");
                }
                if (!hadCatched.booleanValue()) {
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                }
            } catch (Throwable th) {
            }
            if (SystemProperties.get("persist.mtk.aee.mode", "0").equals("4")) {
                Log.i("RuntimeInit", "RuntimeInit: enable FTRACE");
                RuntimeInit.executeCommand("aee -d ftraceon");
            }
            if (!hadCatched.booleanValue()) {
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.RuntimeInit.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.RuntimeInit.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.RuntimeInit.<clinit>():void");
    }

    private static final native void nativeFinishInit();

    private static final native void nativeSetExitWithoutCleanup(boolean z);

    private static final native void nativeZygoteInit();

    private static int Clog_e(String tag, String msg, Throwable tr) {
        return Log.printlns(4, 6, tag, msg, tr);
    }

    private static String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str = PhoneConstants.MVNO_TYPE_NONE;
            while (true) {
                str = reader.readLine();
                if (str == null) {
                    break;
                }
                output.append(str + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    private static final void commonInit() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler());
        TimezoneGetter.setInstance(new TimezoneGetter() {
            public String getId() {
                return SystemProperties.get("persist.sys.timezone");
            }
        });
        TimeZone.setDefault(null);
        LogManager.getLogManager().reset();
        AndroidConfig androidConfig = new AndroidConfig();
        System.setProperty("http.agent", getDefaultUserAgent());
        NetworkManagementSocketTagger.install();
        if (SystemProperties.get("ro.kernel.android.tracing").equals("1")) {
            Slog.i(TAG, "NOTE: emulator trace profiling enabled");
            Debug.enableEmulatorTraceOutput();
        }
        initialized = true;
    }

    private static String getDefaultUserAgent() {
        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version"));
        result.append(" (Linux; U; Android ");
        String version = VERSION.RELEASE;
        if (version.length() <= 0) {
            version = "1.0";
        }
        result.append(version);
        if ("REL".equals(VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                result.append("; ");
                result.append(model);
            }
        }
        String id = Build.ID;
        if (id.length() > 0) {
            result.append(" Build/");
            result.append(id);
        }
        result.append(")");
        return result.toString();
    }

    private static void invokeStaticMain(String className, String[] argv, ClassLoader classLoader) throws MethodAndArgsCaller {
        boolean z = false;
        try {
            Class<?> cl = Class.forName(className, true, classLoader);
            try {
                Class[] clsArr = new Class[1];
                clsArr[0] = String[].class;
                Method m = cl.getMethod("main", clsArr);
                int modifiers = m.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    z = Modifier.isPublic(modifiers);
                }
                if (z) {
                    throw new MethodAndArgsCaller(m, argv);
                }
                throw new RuntimeException("Main method is not public and static on " + className);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Missing static main on " + className, ex);
            } catch (SecurityException ex2) {
                throw new RuntimeException("Problem getting static main on " + className, ex2);
            }
        } catch (ClassNotFoundException ex3) {
            throw new RuntimeException("Missing class when invoking static main " + className, ex3);
        }
    }

    public static final void main(String[] argv) {
        enableDdms();
        if (argv.length == 2 && argv[1].equals("application")) {
            redirectLogStreams();
        }
        commonInit();
        nativeSetExitWithoutCleanup(true);
        nativeFinishInit();
    }

    public static final void zygoteInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) throws MethodAndArgsCaller {
        Trace.traceBegin(64, "RuntimeInit");
        redirectLogStreams();
        commonInit();
        nativeZygoteInit();
        applicationInit(targetSdkVersion, argv, classLoader);
    }

    public static void wrapperInit(int targetSdkVersion, String[] argv) throws MethodAndArgsCaller {
        applicationInit(targetSdkVersion, argv, null);
    }

    private static void applicationInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) throws MethodAndArgsCaller {
        nativeSetExitWithoutCleanup(true);
        VMRuntime.getRuntime().setTargetHeapUtilization(0.75f);
        VMRuntime.getRuntime().setTargetSdkVersion(targetSdkVersion);
        try {
            Arguments args = new Arguments(argv);
            Trace.traceEnd(64);
            invokeStaticMain(args.startClass, args.startArgs, classLoader);
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, ex.getMessage());
        }
    }

    public static void redirectLogStreams() {
        System.out.close();
        System.setOut(new AndroidPrintStream(4, "System.out"));
        System.err.close();
        System.setErr(new AndroidPrintStream(5, "System.err"));
    }

    public static void wtf(String tag, Throwable t, boolean system) {
        try {
            if (ActivityManagerNative.getDefault().handleApplicationWtf(mApplicationObject, tag, system, new CrashInfo(t))) {
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        } catch (Throwable t2) {
            if (!(t2 instanceof DeadObjectException)) {
                Slog.e(TAG, "Error reporting WTF", t2);
                Slog.e(TAG, "Original WTF:", t);
            }
        }
    }

    public static final void setApplicationObject(IBinder app) {
        mApplicationObject = app;
    }

    public static final IBinder getApplicationObject() {
        return mApplicationObject;
    }

    static final void enableDdms() {
        DdmRegister.registerHandlers();
    }
}
