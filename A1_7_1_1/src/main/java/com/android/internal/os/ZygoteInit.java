package com.android.internal.os;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.icu.impl.CacheValue;
import android.icu.impl.CacheValue.Strength;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.ULocale;
import android.net.LocalServerSocket;
import android.opengl.EGL14;
import android.os.Process;
import android.os.Process.ZygoteState;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.security.keystore.AndroidKeyStoreProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.text.Hyphenator;
import android.util.EventLog;
import android.util.Log;
import android.webkit.WebViewFactory;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.common.PluginLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import dalvik.system.VMRuntime;
import dalvik.system.ZygoteHooks;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import libcore.io.IoUtils;
import oppo.content.res.OppoThemeResources;

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
public class ZygoteInit {
    private static final String ABI_LIST_ARG = "--abi-list=";
    private static final String ANDROID_SOCKET_PREFIX = "ANDROID_SOCKET_";
    static boolean DEBUG_ZYGOTE_ON_DEMAND = false;
    private static final int LOG_BOOT_PROGRESS_PRELOAD_END = 3030;
    private static final int LOG_BOOT_PROGRESS_PRELOAD_START = 3020;
    private static boolean MTPROF_DISABLE = false;
    private static final String PRELOADED_CLASSES = "/system/etc/preloaded-classes";
    private static final int PRELOAD_GC_THRESHOLD = 0;
    public static final boolean PRELOAD_RESOURCES = true;
    private static final String PROPERTY_DISABLE_OPENGL_PRELOADING = "ro.zygote.disable_gl_preload";
    private static final String PROPERTY_RUNNING_IN_CONTAINER = "ro.boot.container";
    private static final String PROP_ZYGOTE_ON_DEMAND_DEBUG = "persist.sys.mtk_zygote_debug";
    private static final String PROP_ZYGOTE_ON_DEMAND_ENABLE = "ro.mtk_gmo_zygote_on_demand";
    private static final String PROP_ZYGOTE_ON_DEMAND_PRELOAD = "persist.sys.mtk_zygote_preload";
    private static final int ROOT_GID = 0;
    private static final int ROOT_UID = 0;
    private static final String SOCKET_NAME_ARG = "--socket-name=";
    private static final int SPECIAL_TARGET_SDK_VERSION_OF_SYSTEM_SERVER = 963852741;
    private static final String TAG = "Zygote";
    private static final String TAG1 = "Plug-PluginLoad";
    private static final int UNPRIVILEGED_GID = 9999;
    private static final int UNPRIVILEGED_UID = 9999;
    private static final String heapgrowthlimit = null;
    private static Resources mResources;
    private static LocalServerSocket sServerSocket;
    static final boolean sZygoteOnDemandEnabled = false;
    static boolean sZygoteReady;

    public static class MethodAndArgsCaller extends Exception implements Runnable {
        private final String[] mArgs;
        private final Method mMethod;

        public MethodAndArgsCaller(Method method, String[] args) {
            this.mMethod = method;
            this.mArgs = args;
        }

        public void run() {
            try {
                Method method = this.mMethod;
                Object[] objArr = new Object[1];
                objArr[0] = this.mArgs;
                method.invoke(null, objArr);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex2) {
                Throwable cause = ex2.getCause();
                if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else if (cause instanceof Error) {
                    throw ((Error) cause);
                } else {
                    throw new RuntimeException(ex2);
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.os.ZygoteInit.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.os.ZygoteInit.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ZygoteInit.<clinit>():void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0051 A:{SYNTHETIC, Splitter: B:27:0x0051} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0036 A:{SYNTHETIC, Splitter: B:19:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0063 A:{SYNTHETIC, Splitter: B:33:0x0063} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void addBootEvent(String bootevent) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (!MTPROF_DISABLE) {
            FileOutputStream fos = null;
            try {
                FileOutputStream fos2 = new FileOutputStream("/proc/bootprof");
                try {
                    fos2.write(bootevent.getBytes());
                    fos2.flush();
                    if (fos2 != null) {
                        try {
                            fos2.close();
                        } catch (IOException e3) {
                            Log.e("BOOTPROF", "Failure close /proc/bootprof entry", e3);
                        }
                    }
                    fos = fos2;
                } catch (FileNotFoundException e4) {
                    e2 = e4;
                    fos = fos2;
                    Log.e("BOOTPROF", "Failure open /proc/bootprof, not found!", e2);
                    if (fos != null) {
                    }
                } catch (IOException e5) {
                    e3 = e5;
                    fos = fos2;
                    try {
                        Log.e("BOOTPROF", "Failure open /proc/bootprof entry", e3);
                        if (fos != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fos != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fos = fos2;
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e32) {
                            Log.e("BOOTPROF", "Failure close /proc/bootprof entry", e32);
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e6) {
                e2 = e6;
                Log.e("BOOTPROF", "Failure open /proc/bootprof, not found!", e2);
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e322) {
                        Log.e("BOOTPROF", "Failure close /proc/bootprof entry", e322);
                    }
                }
            } catch (IOException e7) {
                e322 = e7;
                Log.e("BOOTPROF", "Failure open /proc/bootprof entry", e322);
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e3222) {
                        Log.e("BOOTPROF", "Failure close /proc/bootprof entry", e3222);
                    }
                }
            }
        }
    }

    private static void registerZygoteSocket(String socketName) {
        if (sServerSocket == null) {
            String fullSocketName = ANDROID_SOCKET_PREFIX + socketName;
            try {
                int fileDesc = Integer.parseInt(System.getenv(fullSocketName));
                try {
                    FileDescriptor fd = new FileDescriptor();
                    fd.setInt$(fileDesc);
                    sServerSocket = new LocalServerSocket(fd);
                } catch (IOException ex) {
                    throw new RuntimeException("Error binding to local socket '" + fileDesc + "'", ex);
                }
            } catch (RuntimeException ex2) {
                throw new RuntimeException(fullSocketName + " unset or invalid", ex2);
            }
        }
    }

    private static ZygoteConnection acceptCommandPeer(String abiList) {
        try {
            return new ZygoteConnection(sServerSocket.accept(), abiList);
        } catch (IOException ex) {
            throw new RuntimeException("IOException during accept()", ex);
        }
    }

    static void closeServerSocket() {
        try {
            if (sServerSocket != null) {
                FileDescriptor fd = sServerSocket.getFileDescriptor();
                sServerSocket.close();
                if (fd != null) {
                    Os.close(fd);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Zygote:  error closing sockets", ex);
        } catch (ErrnoException ex2) {
            Log.e(TAG, "Zygote:  error closing descriptor", ex2);
        }
        sServerSocket = null;
    }

    static FileDescriptor getServerSocketFileDescriptor() {
        return sServerSocket.getFileDescriptor();
    }

    static void preloadByName(String name) {
        if (!sZygoteOnDemandEnabled) {
            preload();
        } else if ("zygote".equals(name)) {
            preload();
        } else {
            preloadSecondary();
        }
    }

    static void preloadSecondary() {
        Log.d(TAG, "begin preload 2");
        preloadClasses();
        if ("1".equals(SystemProperties.get(PROP_ZYGOTE_ON_DEMAND_PRELOAD, "0"))) {
            preloadResources();
            preloadOpenGL();
            preloadSharedLibraries();
            preloadTextResources();
            WebViewFactory.prepareWebViewInZygote();
        }
        Log.d(TAG, "end preload 2");
    }

    static void preload() {
        Log.d(TAG, "begin preload");
        Trace.traceBegin(65536, "BeginIcuCachePinning");
        beginIcuCachePinning();
        Trace.traceEnd(65536);
        Trace.traceBegin(65536, "PreloadClasses");
        preloadClasses();
        Trace.traceEnd(65536);
        Trace.traceBegin(65536, "PreloadResources");
        preloadResources();
        Trace.traceEnd(65536);
        Trace.traceBegin(65536, "PreloadOpenGL");
        preloadOpenGL();
        Trace.traceEnd(65536);
        preloadSharedLibraries();
        preloadTextResources();
        WebViewFactory.prepareWebViewInZygote();
        endIcuCachePinning();
        warmUpJcaProviders();
        Log.d(TAG, "end preload");
    }

    private static void beginIcuCachePinning() {
        int i = 0;
        Log.i(TAG, "Installing ICU cache reference pinning...");
        CacheValue.setStrength(Strength.STRONG);
        Log.i(TAG, "Preloading ICU data...");
        ULocale[] localesToPin = new ULocale[3];
        localesToPin[0] = ULocale.ROOT;
        localesToPin[1] = ULocale.US;
        localesToPin[2] = ULocale.getDefault();
        int length = localesToPin.length;
        while (i < length) {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(localesToPin[i]);
            i++;
        }
        Log.i(TAG, "Preloading ICU data --- End");
    }

    private static void endIcuCachePinning() {
        CacheValue.setStrength(Strength.SOFT);
        Log.i(TAG, "Uninstalled ICU cache reference pinning...");
    }

    private static void preloadSharedLibraries() {
        Log.i(TAG, "Preloading shared libraries...");
        System.loadLibrary(OppoThemeResources.FRAMEWORK_PACKAGE);
        System.loadLibrary("compiler_rt");
        System.loadLibrary("jnigraphics");
        Log.i(TAG, "Preloading shared libraries --- End");
    }

    private static void preloadOpenGL() {
        Log.i(TAG, "Preloading OpenGL...");
        if (!SystemProperties.getBoolean(PROPERTY_DISABLE_OPENGL_PRELOADING, false)) {
            EGL14.eglGetDisplay(0);
        }
        Log.i(TAG, "Preloading OpenGL --- End");
    }

    private static void preloadTextResources() {
        Log.i(TAG, "Preloading TextResources...");
        Hyphenator.init();
        TextView.preloadFontCache();
        Log.i(TAG, "Preloading TextResources --- End");
    }

    private static void warmUpJcaProviders() {
        long startTime = SystemClock.uptimeMillis();
        Trace.traceBegin(65536, "Starting installation of AndroidKeyStoreProvider");
        AndroidKeyStoreProvider.install();
        Log.i(TAG, "Installed AndroidKeyStoreProvider in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
        Trace.traceEnd(65536);
        startTime = SystemClock.uptimeMillis();
        Trace.traceBegin(65536, "Starting warm up of JCA providers");
        for (Provider p : Security.getProviders()) {
            p.warmUpServiceProvision();
        }
        Log.i(TAG, "Warmed up JCA providers in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
        Trace.traceEnd(65536);
    }

    private static void preloadClasses() {
        VMRuntime runtime = VMRuntime.getRuntime();
        try {
            InputStream is = new FileInputStream(PRELOADED_CLASSES);
            Log.i(TAG, "Preloading classes...");
            long startTime = SystemClock.uptimeMillis();
            int reuid = Os.getuid();
            int regid = Os.getgid();
            boolean droppedPriviliges = false;
            if (reuid == 0 && regid == 0) {
                try {
                    Os.setregid(0, 9999);
                    Os.setreuid(0, 9999);
                    droppedPriviliges = true;
                } catch (ErrnoException ex) {
                    throw new RuntimeException("Failed to drop root", ex);
                }
            }
            float defaultUtilization = runtime.getTargetHeapUtilization();
            runtime.setTargetHeapUtilization(0.8f);
            int count = 0;
            String line;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is), 256);
                while (true) {
                    line = br.readLine();
                    if (line != null) {
                        line = line.trim();
                        if (!(line.startsWith("#") || line.equals(PhoneConstants.MVNO_TYPE_NONE))) {
                            Trace.traceBegin(65536, "PreloadClass " + line);
                            Class.forName(line, true, null);
                            count++;
                            Trace.traceEnd(65536);
                        }
                    } else {
                        Log.i(TAG, "...preloaded " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
                        IoUtils.closeQuietly(is);
                        runtime.setTargetHeapUtilization(defaultUtilization);
                        Trace.traceBegin(65536, "PreloadDexCaches");
                        runtime.preloadDexCaches();
                        Trace.traceEnd(65536);
                        if (droppedPriviliges) {
                            try {
                                Os.setreuid(0, 0);
                                Os.setregid(0, 0);
                            } catch (ErrnoException ex2) {
                                throw new RuntimeException("Failed to restore root", ex2);
                            }
                        }
                        addBootEvent("Zygote:Preload " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms");
                    }
                }
            } catch (ClassNotFoundException e) {
                Log.w(TAG, "Class not found for preloading: " + line);
            } catch (UnsatisfiedLinkError e2) {
                Log.w(TAG, "Problem preloading " + line + ": " + e2);
            } catch (IOException e3) {
                Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e3);
                IoUtils.closeQuietly(is);
                runtime.setTargetHeapUtilization(defaultUtilization);
                Trace.traceBegin(65536, "PreloadDexCaches");
                runtime.preloadDexCaches();
                Trace.traceEnd(65536);
                if (droppedPriviliges) {
                    try {
                        Os.setreuid(0, 0);
                        Os.setregid(0, 0);
                    } catch (ErrnoException ex22) {
                        throw new RuntimeException("Failed to restore root", ex22);
                    }
                }
                addBootEvent("Zygote:Preload " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms");
            } catch (Throwable th) {
                IoUtils.closeQuietly(is);
                runtime.setTargetHeapUtilization(defaultUtilization);
                Trace.traceBegin(65536, "PreloadDexCaches");
                runtime.preloadDexCaches();
                Trace.traceEnd(65536);
                if (droppedPriviliges) {
                    try {
                        Os.setreuid(0, 0);
                        Os.setregid(0, 0);
                    } catch (ErrnoException ex222) {
                        throw new RuntimeException("Failed to restore root", ex222);
                    }
                }
                addBootEvent("Zygote:Preload " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms");
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "Couldn't find /system/etc/preloaded-classes.");
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jianjun.Dan@Plf.SDK : Modify for preload oppo resources", property = OppoRomType.ROM)
    private static void preloadResources() {
        VMRuntime runtime = VMRuntime.getRuntime();
        try {
            mResources = Resources.getSystem();
            mResources.startPreloading();
            Log.i(TAG, "Preloading resources...");
            preloadOppoResources(runtime);
            long startTime = SystemClock.uptimeMillis();
            TypedArray ar = mResources.obtainTypedArray(R.array.preloaded_drawables);
            int N = preloadDrawables(ar);
            ar.recycle();
            Log.i(TAG, "...preloaded " + N + " resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            addBootEvent("Zygote:Preload " + N + " obtain resources in " + (SystemClock.uptimeMillis() - startTime) + "ms");
            startTime = SystemClock.uptimeMillis();
            ar = mResources.obtainTypedArray(R.array.preloaded_color_state_lists);
            N = preloadColorStateLists(ar);
            ar.recycle();
            Log.i(TAG, "...preloaded " + N + " resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            if (mResources.getBoolean(R.bool.config_freeformWindowManagement)) {
                startTime = SystemClock.uptimeMillis();
                ar = mResources.obtainTypedArray(R.array.preloaded_freeform_multi_window_drawables);
                N = preloadDrawables(ar);
                ar.recycle();
                Log.i(TAG, "...preloaded " + N + " resource in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            }
            addBootEvent("Zygote:Preload " + N + " resources in " + (SystemClock.uptimeMillis() - startTime) + "ms");
            mResources.finishPreloading();
        } catch (RuntimeException e) {
            Log.w(TAG, "Failure preloading resources", e);
        }
    }

    private static int preloadColorStateLists(TypedArray ar) {
        int N = ar.length();
        int i = 0;
        while (i < N) {
            int id = ar.getResourceId(i, 0);
            if (id == 0 || mResources.getColorStateList(id, null) != null) {
                i++;
            } else {
                throw new IllegalArgumentException("Unable to find preloaded color resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    private static int preloadDrawables(TypedArray ar) {
        int N = ar.length();
        int i = 0;
        while (i < N) {
            int id = ar.getResourceId(i, 0);
            if (id == 0 || mResources.getDrawable(id, null) != null) {
                i++;
            } else {
                throw new IllegalArgumentException("Unable to find preloaded drawable resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    static void gcAndFinalize() {
        VMRuntime runtime = VMRuntime.getRuntime();
        System.gc();
        runtime.runFinalizationSync();
        System.gc();
    }

    private static void handleSystemServerProcess(Arguments parsedArgs) throws MethodAndArgsCaller {
        closeServerSocket();
        Os.umask(OsConstants.S_IRWXG | OsConstants.S_IRWXO);
        if (parsedArgs.niceName != null) {
            Process.setArgV0(parsedArgs.niceName);
        }
        String systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH");
        if (systemServerClasspath != null) {
            performSystemServerDexOpt(systemServerClasspath);
        }
        if (parsedArgs.invokeWith != null) {
            String[] args = parsedArgs.remainingArgs;
            if (systemServerClasspath != null) {
                String[] amendedArgs = new String[(args.length + 2)];
                amendedArgs[0] = "-cp";
                amendedArgs[1] = systemServerClasspath;
                System.arraycopy(parsedArgs.remainingArgs, 0, amendedArgs, 2, parsedArgs.remainingArgs.length);
            }
            WrapperInit.execApplication(parsedArgs.invokeWith, parsedArgs.niceName, parsedArgs.targetSdkVersion, VMRuntime.getCurrentInstructionSet(), null, args);
            return;
        }
        ClassLoader cl = null;
        if (systemServerClasspath != null) {
            cl = createSystemServerClassLoader(systemServerClasspath, parsedArgs.targetSdkVersion);
            Thread.currentThread().setContextClassLoader(cl);
        }
        RuntimeInit.zygoteInit(SPECIAL_TARGET_SDK_VERSION_OF_SYSTEM_SERVER, parsedArgs.remainingArgs, cl);
    }

    private static PathClassLoader createSystemServerClassLoader(String systemServerClasspath, int targetSdkVersion) {
        return PathClassLoaderFactory.createClassLoader(systemServerClasspath, System.getProperty("java.library.path"), null, ClassLoader.getSystemClassLoader(), targetSdkVersion, true);
    }

    private static void performSystemServerDexOpt(String classPath) {
        String[] classPathElements = classPath.split(":");
        InstallerConnection installer = new InstallerConnection();
        installer.waitForConnection();
        String instructionSet = VMRuntime.getRuntime().vmInstructionSet();
        int dexoptNeeded;
        try {
            String sharedLibraries = PhoneConstants.MVNO_TYPE_NONE;
            for (String classPathElement : classPathElements) {
                dexoptNeeded = DexFile.getDexOptNeeded(classPathElement, instructionSet, "speed", false);
                if (dexoptNeeded != 0) {
                    try {
                        installer.dexopt(classPathElement, 1000, instructionSet, dexoptNeeded, 0, "speed", null, sharedLibraries);
                    } catch (InstallerException e) {
                        Log.w(TAG, "Failed compiling classpath element for system server: " + classPathElement, e);
                    }
                }
                if (!sharedLibraries.isEmpty()) {
                    sharedLibraries = sharedLibraries + ":";
                }
                sharedLibraries = sharedLibraries + classPathElement;
            }
            installer.disconnect();
        } catch (FileNotFoundException e2) {
            Log.w(TAG, "Missing classpath element for system server: " + classPathElement);
        } catch (IOException e3) {
            Log.w(TAG, "Error checking classpath element for system server: " + classPathElement, e3);
            dexoptNeeded = 0;
        } catch (Throwable th) {
            installer.disconnect();
        }
    }

    private static boolean startSystemServer(String abiList, String socketName) throws MethodAndArgsCaller, RuntimeException {
        IllegalArgumentException ex;
        int[] iArr = new int[11];
        iArr[0] = OsConstants.CAP_IPC_LOCK;
        iArr[1] = OsConstants.CAP_KILL;
        iArr[2] = OsConstants.CAP_NET_ADMIN;
        iArr[3] = OsConstants.CAP_NET_BIND_SERVICE;
        iArr[4] = OsConstants.CAP_NET_BROADCAST;
        iArr[5] = OsConstants.CAP_NET_RAW;
        iArr[6] = OsConstants.CAP_SYS_MODULE;
        iArr[7] = OsConstants.CAP_SYS_NICE;
        iArr[8] = OsConstants.CAP_SYS_RESOURCE;
        iArr[9] = OsConstants.CAP_SYS_TIME;
        iArr[10] = OsConstants.CAP_SYS_TTY_CONFIG;
        long capabilities = posixCapabilitiesAsBits(iArr);
        if (!SystemProperties.getBoolean(PROPERTY_RUNNING_IN_CONTAINER, false)) {
            iArr = new int[1];
            iArr[0] = OsConstants.CAP_BLOCK_SUSPEND;
            capabilities |= posixCapabilitiesAsBits(iArr);
        }
        String[] args = new String[7];
        args[0] = "--setuid=1000";
        args[1] = "--setgid=1000";
        args[2] = "--setgroups=1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1018,1021,1032,2000,3001,3002,3003,3006,3007,3009,3010";
        args[3] = "--capabilities=" + capabilities + "," + capabilities;
        args[4] = "--nice-name=system_server";
        args[5] = "--runtime-args";
        args[6] = "com.android.server.SystemServer";
        try {
            Arguments parsedArgs = new Arguments(args);
            try {
                ZygoteConnection.applyDebuggerSystemProperty(parsedArgs);
                ZygoteConnection.applyInvokeWithSystemProperty(parsedArgs);
                if (Zygote.forkSystemServer(parsedArgs.uid, parsedArgs.gid, parsedArgs.gids, parsedArgs.debugFlags, null, parsedArgs.permittedCapabilities, parsedArgs.effectiveCapabilities) == 0) {
                    if (hasSecondZygote(abiList)) {
                        waitForSecondaryZygote(socketName);
                    }
                    handleSystemServerProcess(parsedArgs);
                }
                return true;
            } catch (IllegalArgumentException e) {
                ex = e;
            }
        } catch (IllegalArgumentException e2) {
            ex = e2;
            throw new RuntimeException(ex);
        }
    }

    private static long posixCapabilitiesAsBits(int... capabilities) {
        long result = 0;
        for (int capability : capabilities) {
            if (capability < 0 || capability > OsConstants.CAP_LAST_CAP) {
                throw new IllegalArgumentException(String.valueOf(capability));
            }
            result |= 1 << capability;
        }
        return result;
    }

    public static void main(String[] argv) {
        DEBUG_ZYGOTE_ON_DEMAND = SystemProperties.get(PROP_ZYGOTE_ON_DEMAND_DEBUG).equals("1");
        if (DEBUG_ZYGOTE_ON_DEMAND) {
            Log.d(TAG, "ZygoteOnDemand: Zygote ready = " + sZygoteReady);
        }
        String socketName = "zygote";
        ZygoteHooks.startZygoteNoThreadCreation();
        try {
            Trace.traceBegin(65536, "ZygoteInit");
            RuntimeInit.enableDdms();
            SamplingProfilerIntegration.start();
            boolean startSystemServer = false;
            String abiList = null;
            for (int i = 1; i < argv.length; i++) {
                if ("start-system-server".equals(argv[i])) {
                    startSystemServer = true;
                } else if (argv[i].startsWith(ABI_LIST_ARG)) {
                    abiList = argv[i].substring(ABI_LIST_ARG.length());
                } else if (argv[i].startsWith(SOCKET_NAME_ARG)) {
                    socketName = argv[i].substring(SOCKET_NAME_ARG.length());
                } else {
                    throw new RuntimeException("Unknown command line argument: " + argv[i]);
                }
            }
            if (abiList == null) {
                throw new RuntimeException("No ABI list supplied.");
            }
            registerZygoteSocket(socketName);
            Trace.traceBegin(65536, "ZygotePreload");
            EventLog.writeEvent(3020, SystemClock.uptimeMillis());
            addBootEvent("Zygote:Preload Start");
            preloadByName(socketName);
            EventLog.writeEvent((int) LOG_BOOT_PROGRESS_PRELOAD_END, SystemClock.uptimeMillis());
            Trace.traceEnd(65536);
            SamplingProfilerIntegration.writeZygoteSnapshot();
            Trace.traceBegin(65536, "PostZygoteInitGC");
            gcAndFinalize();
            Trace.traceEnd(65536);
            Trace.traceEnd(65536);
            Trace.setTracingEnabled(false);
            Zygote.nativeUnmountStorageOnInit();
            addBootEvent("Zygote:Preload End");
            ZygoteHooks.stopZygoteNoThreadCreation();
            boolean preloadMPlugin = false;
            if (!sZygoteOnDemandEnabled) {
                preloadMPlugin = true;
            } else if ("zygote".equals(socketName)) {
                preloadMPlugin = true;
            }
            if (preloadMPlugin) {
                Log.i(TAG1, "preloadMappingTable() -- start ");
                PluginLoader.preloadPluginInfo();
                Log.i(TAG1, "preloadMappingTable() -- end ");
            }
            if (startSystemServer) {
                startSystemServer(abiList, socketName);
            }
            sZygoteReady = true;
            if (DEBUG_ZYGOTE_ON_DEMAND) {
                Log.d(TAG, "ZygoteOnDemand: Zygote ready = " + sZygoteReady + ", socket name: " + socketName);
            }
            Log.i(TAG, "Accepting command socket connections");
            runSelectLoop(abiList);
            zygoteStopping("ZygoteOnDemand: End of runSelectLoop", socketName);
            closeServerSocket();
            zygoteStopping("ZygoteOnDemand: End of main function", socketName);
        } catch (MethodAndArgsCaller caller) {
            caller.run();
        } catch (Throwable ex) {
            Log.e(TAG, "Zygote died with exception", ex);
            closeServerSocket();
        }
    }

    private static void zygoteStopping(String reason, String socketName) {
        sZygoteReady = false;
        if (DEBUG_ZYGOTE_ON_DEMAND) {
            Log.d(TAG, "ZygoteOnDemand: zygoteStopping for " + socketName + ", reason: " + reason);
        }
        if (sZygoteOnDemandEnabled && socketName.equals("zygote_secondary") && Process.isSecondaryZygoteRunning()) {
            Log.d(TAG, "ZygoteOnDemand: stop secondary Zygote for " + socketName + ", reason: " + reason);
            Process.stopSecondaryZygote();
        }
    }

    private static boolean hasSecondZygote(String abiList) {
        return !SystemProperties.get("ro.product.cpu.abilist").equals(abiList);
    }

    private static void waitForSecondaryZygote(String socketName) {
        String otherZygoteName = "zygote".equals(socketName) ? "zygote_secondary" : "zygote";
        if (sZygoteOnDemandEnabled) {
            if (DEBUG_ZYGOTE_ON_DEMAND) {
                Log.d(TAG, "ZygoteOnDemand: skip waitForSecondaryZygote: " + socketName + " wait " + otherZygoteName);
            }
            return;
        }
        while (true) {
            try {
                ZygoteState.connect(otherZygoteName).close();
                break;
            } catch (IOException ioe) {
                Log.w(TAG, "Got error connecting to zygote, retrying. msg= " + ioe.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static void runSelectLoop(String abiList) throws MethodAndArgsCaller {
        ArrayList<FileDescriptor> fds = new ArrayList();
        ArrayList<ZygoteConnection> peers = new ArrayList();
        fds.add(sServerSocket.getFileDescriptor());
        peers.add(null);
        while (true) {
            int i;
            StructPollfd[] pollFds = new StructPollfd[fds.size()];
            for (i = 0; i < pollFds.length; i++) {
                pollFds[i] = new StructPollfd();
                pollFds[i].fd = (FileDescriptor) fds.get(i);
                pollFds[i].events = (short) OsConstants.POLLIN;
            }
            try {
                Os.poll(pollFds, -1);
                for (i = pollFds.length - 1; i >= 0; i--) {
                    if ((pollFds[i].revents & OsConstants.POLLIN) != 0) {
                        if (i == 0) {
                            ZygoteConnection newPeer = acceptCommandPeer(abiList);
                            peers.add(newPeer);
                            fds.add(newPeer.getFileDesciptor());
                        } else if (((ZygoteConnection) peers.get(i)).runOnce()) {
                            peers.remove(i);
                            fds.remove(i);
                        }
                    }
                }
            } catch (ErrnoException ex) {
                throw new RuntimeException("poll failed", ex);
            }
        }
    }

    private ZygoteInit() {
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Jianjun.Dan@Plf.SDK : Add for preload resource", property = OppoRomType.ROM)
    private static void preloadOppoResources(VMRuntime runtime) {
        long startTime = SystemClock.uptimeMillis();
        TypedArray ar = mResources.obtainTypedArray(201786385);
        int N = preloadDrawables(ar);
        ar.recycle();
        Log.i(TAG, "...preloaded " + N + " oppo drawable resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
        startTime = SystemClock.uptimeMillis();
        ar = mResources.obtainTypedArray(201786386);
        N = preloadColorStateLists(ar);
        ar.recycle();
        Log.i(TAG, "...preloaded " + N + " oppo color resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
    }
}
