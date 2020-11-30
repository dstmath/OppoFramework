package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.app.ApplicationLoaders;
import android.app.IColorCommonInjector;
import android.common.OppoFeatureCache;
import android.content.pm.SharedLibraryInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Environment;
import android.os.IInstalld;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.ZygoteProcess;
import android.os.storage.StorageManager;
import android.provider.SettingsStringUtil;
import android.security.keystore.AndroidKeyStoreProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructCapUserData;
import android.system.StructCapUserHeader;
import android.telephony.SmsManager;
import android.text.Hyphenator;
import android.util.EventLog;
import android.util.Log;
import android.util.TimingsTraceLog;
import android.webkit.WebViewFactory;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.RuntimeInit;
import com.android.internal.util.Preconditions;
import com.oppo.phoenix.Phoenix;
import dalvik.system.DexFile;
import dalvik.system.VMRuntime;
import dalvik.system.ZygoteHooks;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Provider;
import java.security.Security;
import libcore.io.IoUtils;

public class ZygoteInit {
    private static final String ABI_LIST_ARG = "--abi-list=";
    private static final int LOG_BOOT_PROGRESS_PRELOAD_END = 3030;
    private static final int LOG_BOOT_PROGRESS_PRELOAD_START = 3020;
    private static final String PRELOADED_CLASSES = "/system/etc/preloaded-classes";
    private static final int PRELOAD_GC_THRESHOLD = 50000;
    public static final boolean PRELOAD_RESOURCES = true;
    private static final String PROPERTY_DISABLE_GRAPHICS_DRIVER_PRELOADING = "ro.zygote.disable_gl_preload";
    private static final int ROOT_GID = 0;
    private static final int ROOT_UID = 0;
    private static final String SOCKET_NAME_ARG = "--socket-name=";
    private static final String TAG = "Zygote";
    private static final int UNPRIVILEGED_GID = 9999;
    private static final int UNPRIVILEGED_UID = 9999;
    @UnsupportedAppUsage
    private static Resources mResources;
    private static ClassLoader sCachedSystemServerClassLoader = null;
    private static boolean sMtprofDisable = false;
    private static boolean sPreloadComplete;

    private static native void nativePreloadAppProcessHALs();

    static native void nativePreloadGraphicsDriver();

    private static final native void nativeZygoteInit();

    private static void addBootEvent(String bootevent) {
        if (!sMtprofDisable) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream("/proc/bootprof");
                fos.write(bootevent.getBytes());
                fos.flush();
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e("BOOTPROF", "Failure close /proc/bootprof entry", e);
                }
            } catch (FileNotFoundException e2) {
                Log.e("BOOTPROF", "Failure open /proc/bootprof, not found!", e2);
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e3) {
                Log.e("BOOTPROF", "Failure open /proc/bootprof entry", e3);
                if (fos != null) {
                    fos.close();
                }
            } catch (Throwable th) {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e4) {
                        Log.e("BOOTPROF", "Failure close /proc/bootprof entry", e4);
                    }
                }
                throw th;
            }
        }
    }

    static void preload(TimingsTraceLog bootTimingsTraceLog) {
        Log.d(TAG, "begin preload");
        bootTimingsTraceLog.traceBegin("BeginPreload");
        beginPreload();
        bootTimingsTraceLog.traceEnd();
        bootTimingsTraceLog.traceBegin("PreloadClasses");
        preloadClasses();
        bootTimingsTraceLog.traceEnd();
        bootTimingsTraceLog.traceBegin("CacheNonBootClasspathClassLoaders");
        cacheNonBootClasspathClassLoaders();
        bootTimingsTraceLog.traceEnd();
        bootTimingsTraceLog.traceBegin("PreloadResources");
        preloadResources();
        bootTimingsTraceLog.traceEnd();
        Trace.traceBegin(16384, "PreloadAppProcessHALs");
        nativePreloadAppProcessHALs();
        Trace.traceEnd(16384);
        Trace.traceBegin(16384, "PreloadGraphicsDriver");
        maybePreloadGraphicsDriver();
        Trace.traceEnd(16384);
        preloadSharedLibraries();
        preloadTextResources();
        WebViewFactory.prepareWebViewInZygote();
        endPreload();
        warmUpJcaProviders();
        Log.d(TAG, "end preload");
        sPreloadComplete = true;
    }

    public static void lazyPreload() {
        Preconditions.checkState(!sPreloadComplete);
        Log.i(TAG, "Lazily preloading resources.");
        preload(new TimingsTraceLog("ZygoteInitTiming_lazy", 16384));
    }

    private static void beginPreload() {
        Log.i(TAG, "Calling ZygoteHooks.beginPreload()");
        ZygoteHooks.onBeginPreload();
    }

    private static void endPreload() {
        ZygoteHooks.onEndPreload();
        Log.i(TAG, "Called ZygoteHooks.endPreload()");
    }

    private static void preloadSharedLibraries() {
        Log.i(TAG, "Preloading shared libraries...");
        System.loadLibrary("android");
        System.loadLibrary("compiler_rt");
        System.loadLibrary("jnigraphics");
    }

    private static void maybePreloadGraphicsDriver() {
        if (!SystemProperties.getBoolean(PROPERTY_DISABLE_GRAPHICS_DRIVER_PRELOADING, false)) {
            nativePreloadGraphicsDriver();
        }
    }

    private static void preloadTextResources() {
        Hyphenator.init();
        TextView.preloadFontCache();
    }

    private static void warmUpJcaProviders() {
        long startTime = SystemClock.uptimeMillis();
        Trace.traceBegin(16384, "Starting installation of AndroidKeyStoreProvider");
        AndroidKeyStoreProvider.install();
        Log.i(TAG, "Installed AndroidKeyStoreProvider in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
        Trace.traceEnd(16384);
        long startTime2 = SystemClock.uptimeMillis();
        Trace.traceBegin(16384, "Starting warm up of JCA providers");
        for (Provider p : Security.getProviders()) {
            p.warmUpServiceProvision();
        }
        Log.i(TAG, "Warmed up JCA providers in " + (SystemClock.uptimeMillis() - startTime2) + "ms.");
        Trace.traceEnd(16384);
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x021f  */
    private static void preloadClasses() {
        int count;
        String str;
        String str2;
        Throwable th;
        int regid;
        float defaultUtilization;
        IOException e;
        String str3 = "ms";
        String str4 = "Zygote:Preload ";
        VMRuntime runtime = VMRuntime.getRuntime();
        try {
            InputStream is = new FileInputStream(PRELOADED_CLASSES);
            Log.i(TAG, "Preloading classes...");
            long startTime = SystemClock.uptimeMillis();
            int reuid = Os.getuid();
            int regid2 = Os.getgid();
            boolean droppedPriviliges = false;
            if (reuid == 0 && regid2 == 0) {
                try {
                    Os.setregid(0, 9999);
                    Os.setreuid(0, 9999);
                    droppedPriviliges = true;
                } catch (ErrnoException ex) {
                    throw new RuntimeException("Failed to drop root", ex);
                }
            }
            float defaultUtilization2 = runtime.getTargetHeapUtilization();
            runtime.setTargetHeapUtilization(0.8f);
            float defaultUtilization3 = defaultUtilization2;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is), 256);
                count = 0;
                while (true) {
                    try {
                        String line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        try {
                            String line2 = line.trim();
                            try {
                                if (line2.startsWith("#") || line2.equals("")) {
                                    reuid = reuid;
                                    str3 = str3;
                                    str4 = str4;
                                } else {
                                    try {
                                        Trace.traceBegin(16384, line2);
                                        try {
                                            Class.forName(line2, true, null);
                                            count++;
                                        } catch (ClassNotFoundException e2) {
                                            Log.w(TAG, "Class not found for preloading: " + line2);
                                        } catch (UnsatisfiedLinkError e3) {
                                            Log.w(TAG, "Problem preloading " + line2 + ": " + e3);
                                        } catch (Throwable t) {
                                            Log.e(TAG, "Error preloading " + line2 + ".", t);
                                            if (t instanceof Error) {
                                                throw ((Error) t);
                                            } else if (t instanceof RuntimeException) {
                                                throw ((RuntimeException) t);
                                            } else {
                                                throw new RuntimeException(t);
                                            }
                                        }
                                        Trace.traceEnd(16384);
                                        reuid = reuid;
                                        str3 = str3;
                                        str4 = str4;
                                    } catch (IOException e4) {
                                        e = e4;
                                        regid = regid2;
                                        defaultUtilization = defaultUtilization3;
                                        str = str3;
                                        str2 = str4;
                                        try {
                                            Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                                            IoUtils.closeQuietly(is);
                                            runtime.setTargetHeapUtilization(defaultUtilization);
                                            Trace.traceBegin(16384, "PreloadDexCaches");
                                            runtime.preloadDexCaches();
                                            Trace.traceEnd(16384);
                                            if (droppedPriviliges) {
                                            }
                                            addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                                        } catch (Throwable th2) {
                                            th = th2;
                                            defaultUtilization3 = defaultUtilization;
                                            IoUtils.closeQuietly(is);
                                            runtime.setTargetHeapUtilization(defaultUtilization3);
                                            Trace.traceBegin(16384, "PreloadDexCaches");
                                            runtime.preloadDexCaches();
                                            Trace.traceEnd(16384);
                                            if (droppedPriviliges) {
                                            }
                                            addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        str = str3;
                                        str2 = str4;
                                        IoUtils.closeQuietly(is);
                                        runtime.setTargetHeapUtilization(defaultUtilization3);
                                        Trace.traceBegin(16384, "PreloadDexCaches");
                                        runtime.preloadDexCaches();
                                        Trace.traceEnd(16384);
                                        if (droppedPriviliges) {
                                        }
                                        addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                                        throw th;
                                    }
                                }
                            } catch (IOException e5) {
                                e = e5;
                                str2 = str4;
                                regid = regid2;
                                str = str3;
                                defaultUtilization = defaultUtilization3;
                                Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                                IoUtils.closeQuietly(is);
                                runtime.setTargetHeapUtilization(defaultUtilization);
                                Trace.traceBegin(16384, "PreloadDexCaches");
                                runtime.preloadDexCaches();
                                Trace.traceEnd(16384);
                                if (droppedPriviliges) {
                                }
                                addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                            } catch (Throwable th4) {
                                th = th4;
                                str2 = str4;
                                str = str3;
                                IoUtils.closeQuietly(is);
                                runtime.setTargetHeapUtilization(defaultUtilization3);
                                Trace.traceBegin(16384, "PreloadDexCaches");
                                runtime.preloadDexCaches();
                                Trace.traceEnd(16384);
                                if (droppedPriviliges) {
                                }
                                addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                                throw th;
                            }
                        } catch (IOException e6) {
                            e = e6;
                            str2 = str4;
                            regid = regid2;
                            str = str3;
                            defaultUtilization = defaultUtilization3;
                            Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                            IoUtils.closeQuietly(is);
                            runtime.setTargetHeapUtilization(defaultUtilization);
                            Trace.traceBegin(16384, "PreloadDexCaches");
                            runtime.preloadDexCaches();
                            Trace.traceEnd(16384);
                            if (droppedPriviliges) {
                            }
                            addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                        } catch (Throwable th5) {
                            th = th5;
                            str2 = str4;
                            str = str3;
                            IoUtils.closeQuietly(is);
                            runtime.setTargetHeapUtilization(defaultUtilization3);
                            Trace.traceBegin(16384, "PreloadDexCaches");
                            runtime.preloadDexCaches();
                            Trace.traceEnd(16384);
                            if (droppedPriviliges) {
                            }
                            addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                            throw th;
                        }
                    } catch (IOException e7) {
                        e = e7;
                        str2 = str4;
                        regid = regid2;
                        str = str3;
                        defaultUtilization = defaultUtilization3;
                        Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                        IoUtils.closeQuietly(is);
                        runtime.setTargetHeapUtilization(defaultUtilization);
                        Trace.traceBegin(16384, "PreloadDexCaches");
                        runtime.preloadDexCaches();
                        Trace.traceEnd(16384);
                        if (droppedPriviliges) {
                            try {
                                Os.setreuid(0, 0);
                                Os.setregid(0, 0);
                            } catch (ErrnoException ex2) {
                                throw new RuntimeException("Failed to restore root", ex2);
                            }
                        }
                        addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                    } catch (Throwable th6) {
                        th = th6;
                        str2 = str4;
                        str = str3;
                        IoUtils.closeQuietly(is);
                        runtime.setTargetHeapUtilization(defaultUtilization3);
                        Trace.traceBegin(16384, "PreloadDexCaches");
                        runtime.preloadDexCaches();
                        Trace.traceEnd(16384);
                        if (droppedPriviliges) {
                            try {
                                Os.setreuid(0, 0);
                                Os.setregid(0, 0);
                            } catch (ErrnoException ex3) {
                                throw new RuntimeException("Failed to restore root", ex3);
                            }
                        }
                        addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                        throw th;
                    }
                }
                try {
                    Log.i(TAG, "...preloaded " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
                    IoUtils.closeQuietly(is);
                    runtime.setTargetHeapUtilization(defaultUtilization3);
                    Trace.traceBegin(16384, "PreloadDexCaches");
                    runtime.preloadDexCaches();
                    Trace.traceEnd(16384);
                    if (droppedPriviliges) {
                        try {
                            Os.setreuid(0, 0);
                            Os.setregid(0, 0);
                        } catch (ErrnoException ex4) {
                            throw new RuntimeException("Failed to restore root", ex4);
                        }
                    }
                    addBootEvent(str4 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str3);
                } catch (IOException e8) {
                    e = e8;
                    regid = regid2;
                    defaultUtilization = defaultUtilization3;
                    str = str3;
                    str2 = str4;
                    Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                    IoUtils.closeQuietly(is);
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    Trace.traceBegin(16384, "PreloadDexCaches");
                    runtime.preloadDexCaches();
                    Trace.traceEnd(16384);
                    if (droppedPriviliges) {
                    }
                    addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                } catch (Throwable th7) {
                    th = th7;
                    str = str3;
                    str2 = str4;
                    IoUtils.closeQuietly(is);
                    runtime.setTargetHeapUtilization(defaultUtilization3);
                    Trace.traceBegin(16384, "PreloadDexCaches");
                    runtime.preloadDexCaches();
                    Trace.traceEnd(16384);
                    if (droppedPriviliges) {
                    }
                    addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                    throw th;
                }
            } catch (IOException e9) {
                e = e9;
                str2 = str4;
                regid = regid2;
                str = str3;
                defaultUtilization = defaultUtilization3;
                count = 0;
                Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                IoUtils.closeQuietly(is);
                runtime.setTargetHeapUtilization(defaultUtilization);
                Trace.traceBegin(16384, "PreloadDexCaches");
                runtime.preloadDexCaches();
                Trace.traceEnd(16384);
                if (droppedPriviliges) {
                }
                addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
            } catch (Throwable th8) {
                th = th8;
                str2 = str4;
                str = str3;
                count = 0;
                IoUtils.closeQuietly(is);
                runtime.setTargetHeapUtilization(defaultUtilization3);
                Trace.traceBegin(16384, "PreloadDexCaches");
                runtime.preloadDexCaches();
                Trace.traceEnd(16384);
                if (droppedPriviliges) {
                }
                addBootEvent(str2 + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + str);
                throw th;
            }
        } catch (FileNotFoundException e10) {
            Log.e(TAG, "Couldn't find /system/etc/preloaded-classes.");
        }
    }

    private static void cacheNonBootClasspathClassLoaders() {
        SharedLibraryInfo hidlBase = new SharedLibraryInfo("/system/framework/android.hidl.base-V1.0-java.jar", null, null, null, 0, 0, null, null, null);
        SharedLibraryInfo hidlManager = new SharedLibraryInfo("/system/framework/android.hidl.manager-V1.0-java.jar", null, null, null, 0, 0, null, null, null);
        hidlManager.addDependency(hidlBase);
        ApplicationLoaders.getDefault().createAndCacheNonBootclasspathSystemClassLoaders(new SharedLibraryInfo[]{hidlBase, hidlManager});
    }

    private static void preloadResources() {
        VMRuntime.getRuntime();
        try {
            mResources = Resources.getSystem();
            mResources.startPreloading();
            Log.i(TAG, "Preloading resources...");
            ((IColorCommonInjector) OppoFeatureCache.getOrCreate(IColorCommonInjector.DEFAULT, new Object[0])).hookPreloadResources(mResources, TAG);
            long startTime = SystemClock.uptimeMillis();
            TypedArray ar = mResources.obtainTypedArray(R.array.preloaded_drawables);
            int N = preloadDrawables(ar);
            ar.recycle();
            Log.i(TAG, "...preloaded " + N + " resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            addBootEvent("Zygote:Preload " + N + " obtain resources in " + (SystemClock.uptimeMillis() - startTime) + "ms");
            long startTime2 = SystemClock.uptimeMillis();
            TypedArray ar2 = mResources.obtainTypedArray(R.array.preloaded_color_state_lists);
            int N2 = preloadColorStateLists(ar2);
            ar2.recycle();
            Log.i(TAG, "...preloaded " + N2 + " resources in " + (SystemClock.uptimeMillis() - startTime2) + "ms.");
            if (mResources.getBoolean(R.bool.config_freeformWindowManagement)) {
                startTime2 = SystemClock.uptimeMillis();
                TypedArray ar3 = mResources.obtainTypedArray(R.array.preloaded_freeform_multi_window_drawables);
                N2 = preloadDrawables(ar3);
                ar3.recycle();
                Log.i(TAG, "...preloaded " + N2 + " resource in " + (SystemClock.uptimeMillis() - startTime2) + "ms.");
            }
            addBootEvent("Zygote:Preload " + N2 + " resources in " + (SystemClock.uptimeMillis() - startTime2) + "ms");
            mResources.finishPreloading();
        } catch (RuntimeException e) {
            Log.w(TAG, "Failure preloading resources", e);
        }
    }

    private static int preloadColorStateLists(TypedArray ar) {
        int N = ar.length();
        for (int i = 0; i < N; i++) {
            int id = ar.getResourceId(i, 0);
            if (id != 0 && mResources.getColorStateList(id, null) == null) {
                throw new IllegalArgumentException("Unable to find preloaded color resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    private static int preloadDrawables(TypedArray ar) {
        int N = ar.length();
        for (int i = 0; i < N; i++) {
            int id = ar.getResourceId(i, 0);
            if (id != 0 && mResources.getDrawable(id, null) == null) {
                throw new IllegalArgumentException("Unable to find preloaded drawable resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    private static void gcAndFinalize() {
        ZygoteHooks.gcAndFinalize();
    }

    private static Runnable handleSystemServerProcess(ZygoteArguments parsedArgs) {
        Log.d(TAG, "increase system server priority to -15");
        Process.setThreadPriority(-15);
        Os.umask(OsConstants.S_IRWXG | OsConstants.S_IRWXO);
        if (parsedArgs.mNiceName != null) {
            Process.setArgV0(parsedArgs.mNiceName);
        }
        String systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH");
        if (systemServerClasspath != null) {
            if (performSystemServerDexOpt(systemServerClasspath)) {
                sCachedSystemServerClassLoader = null;
            }
            if (SystemProperties.getBoolean("dalvik.vm.profilesystemserver", false) && (Build.IS_USERDEBUG || Build.IS_ENG)) {
                try {
                    prepareSystemServerProfile(systemServerClasspath);
                } catch (Exception e) {
                    Log.wtf(TAG, "Failed to set up system server profile", e);
                }
            }
        }
        if (parsedArgs.mInvokeWith != null) {
            String[] args = parsedArgs.mRemainingArgs;
            if (systemServerClasspath != null) {
                String[] amendedArgs = new String[(args.length + 2)];
                amendedArgs[0] = "-cp";
                amendedArgs[1] = systemServerClasspath;
                System.arraycopy(args, 0, amendedArgs, 2, args.length);
                args = amendedArgs;
            }
            WrapperInit.execApplication(parsedArgs.mInvokeWith, parsedArgs.mNiceName, parsedArgs.mTargetSdkVersion, VMRuntime.getCurrentInstructionSet(), null, args);
            throw new IllegalStateException("Unexpected return from WrapperInit.execApplication");
        }
        createSystemServerClassLoader();
        ClassLoader cl = sCachedSystemServerClassLoader;
        if (cl != null) {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return zygoteInit(parsedArgs.mTargetSdkVersion, parsedArgs.mRemainingArgs, cl);
    }

    private static void createSystemServerClassLoader() {
        String systemServerClasspath;
        if (sCachedSystemServerClassLoader == null && (systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH")) != null) {
            sCachedSystemServerClassLoader = createPathClassLoader(systemServerClasspath, 10000);
        }
    }

    private static void prepareSystemServerProfile(String systemServerClasspath) throws RemoteException {
        if (!systemServerClasspath.isEmpty()) {
            String[] codePaths = systemServerClasspath.split(SettingsStringUtil.DELIMITER);
            IInstalld.Stub.asInterface(ServiceManager.getService("installd")).prepareAppProfile("android", 0, UserHandle.getAppId(1000), "primary.prof", codePaths[0], null);
            VMRuntime.registerAppInfo(new File(Environment.getDataProfilesDePackageDirectory(0, "android"), "primary.prof").getAbsolutePath(), codePaths);
        }
    }

    public static void setApiBlacklistExemptions(String[] exemptions) {
        VMRuntime.getRuntime().setHiddenApiExemptions(exemptions);
    }

    public static void setHiddenApiAccessLogSampleRate(int percent) {
        VMRuntime.getRuntime().setHiddenApiAccessLogSamplingRate(percent);
    }

    public static void setHiddenApiUsageLogger(VMRuntime.HiddenApiUsageLogger logger) {
        VMRuntime.getRuntime();
        VMRuntime.setHiddenApiUsageLogger(logger);
    }

    static ClassLoader createPathClassLoader(String classPath, int targetSdkVersion) {
        String libraryPath = System.getProperty("java.library.path");
        return ClassLoaderFactory.createClassLoader(classPath, libraryPath, libraryPath, ClassLoader.getSystemClassLoader().getParent(), targetSdkVersion, true, null);
    }

    private static boolean performSystemServerDexOpt(String classPath) {
        int i;
        int i2;
        int dexoptNeeded;
        String classPathForElement;
        String classPathElement;
        String[] classPathElements = classPath.split(SettingsStringUtil.DELIMITER);
        IInstalld installd = IInstalld.Stub.asInterface(ServiceManager.getService("installd"));
        String instructionSet = VMRuntime.getRuntime().vmInstructionSet();
        int length = classPathElements.length;
        String classPathForElement2 = "";
        boolean compiledSomething = false;
        int i3 = 0;
        while (i3 < length) {
            String classPathElement2 = classPathElements[i3];
            String systemServerFilter = SystemProperties.get("dalvik.vm.systemservercompilerfilter", "speed");
            try {
                dexoptNeeded = DexFile.getDexOptNeeded(classPathElement2, instructionSet, systemServerFilter, null, false, false);
            } catch (FileNotFoundException e) {
                i2 = i3;
                i = length;
                Log.w(TAG, "Missing classpath element for system server: " + classPathElement2);
                classPathForElement2 = classPathForElement2;
            } catch (IOException e2) {
                Log.w(TAG, "Error checking classpath element for system server: " + classPathElement2, e2);
                dexoptNeeded = 0;
            }
            if (dexoptNeeded != 0) {
                classPathForElement = classPathForElement2;
                i2 = i3;
                i = length;
                try {
                    installd.dexopt(classPathElement2, 1000, "*", instructionSet, dexoptNeeded, null, 0, systemServerFilter, StorageManager.UUID_PRIVATE_INTERNAL, getSystemServerClassLoaderContext(classPathForElement2), null, false, 0, null, null, "server-dexopt");
                    compiledSomething = true;
                    classPathElement = classPathElement2;
                } catch (RemoteException | ServiceSpecificException e3) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Failed compiling classpath element for system server: ");
                    classPathElement = classPathElement2;
                    sb.append(classPathElement);
                    Log.w(TAG, sb.toString(), e3);
                }
            } else {
                classPathElement = classPathElement2;
                classPathForElement = classPathForElement2;
                i2 = i3;
                i = length;
            }
            classPathForElement2 = encodeSystemServerClassPath(classPathForElement, classPathElement);
            i3 = i2 + 1;
            length = i;
        }
        return compiledSomething;
    }

    private static String getSystemServerClassLoaderContext(String classPath) {
        if (classPath == null) {
            return "PCL[]";
        }
        return "PCL[" + classPath + "]";
    }

    private static String encodeSystemServerClassPath(String classPath, String newElement) {
        if (classPath == null || classPath.isEmpty()) {
            return newElement;
        }
        return classPath + SettingsStringUtil.DELIMITER + newElement;
    }

    private static Runnable forkSystemServer(String abiList, String socketName, ZygoteServer zygoteServer) {
        long capabilities = posixCapabilitiesAsBits(OsConstants.CAP_IPC_LOCK, OsConstants.CAP_KILL, OsConstants.CAP_NET_ADMIN, OsConstants.CAP_NET_BIND_SERVICE, OsConstants.CAP_NET_BROADCAST, OsConstants.CAP_NET_RAW, OsConstants.CAP_SYS_MODULE, OsConstants.CAP_SYS_NICE, OsConstants.CAP_SYS_PTRACE, OsConstants.CAP_SYS_TIME, OsConstants.CAP_SYS_TTY_CONFIG, OsConstants.CAP_WAKE_ALARM, OsConstants.CAP_BLOCK_SUSPEND);
        try {
            StructCapUserData[] data = Os.capget(new StructCapUserHeader(OsConstants._LINUX_CAPABILITY_VERSION_3, 0));
            long capabilities2 = ((((long) data[1].effective) << 32) | ((long) data[0].effective)) & capabilities;
            try {
                ZygoteArguments parsedArgs = new ZygoteArguments(new String[]{"--setuid=1000", "--setgid=1000", "--setgroups=1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1018,1021,1023,1024,1032,1065,3001,3002,3003,3006,3007,3009,3010", "--capabilities=" + capabilities2 + SmsManager.REGEX_PREFIX_DELIMITER + capabilities2, "--nice-name=system_server", "--runtime-args", "--target-sdk-version=10000", "com.android.server.SystemServer"});
                Zygote.applyDebuggerSystemProperty(parsedArgs);
                Zygote.applyInvokeWithSystemProperty(parsedArgs);
                if (SystemProperties.getBoolean("dalvik.vm.profilesystemserver", false)) {
                    parsedArgs.mRuntimeFlags |= 16384;
                }
                if (Zygote.forkSystemServer(parsedArgs.mUid, parsedArgs.mGid, parsedArgs.mGids, parsedArgs.mRuntimeFlags, null, parsedArgs.mPermittedCapabilities, parsedArgs.mEffectiveCapabilities) != 0) {
                    return null;
                }
                if (hasSecondZygote(abiList)) {
                    waitForSecondaryZygote(socketName);
                }
                zygoteServer.closeServerSocket();
                return handleSystemServerProcess(parsedArgs);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            }
        } catch (ErrnoException ex2) {
            throw new RuntimeException("Failed to capget()", ex2);
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

    @UnsupportedAppUsage
    public static void main(String[] argv) {
        Runnable r;
        ZygoteServer zygoteServer = null;
        ZygoteHooks.startZygoteNoThreadCreation();
        try {
            Os.setpgid(0, 0);
            try {
                if (!WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("sys.boot_completed"))) {
                    MetricsLogger.histogram(null, "boot_zygote_init", (int) SystemClock.elapsedRealtime());
                }
                TimingsTraceLog bootTimingsTraceLog = new TimingsTraceLog(Process.is64Bit() ? "Zygote64Timing" : "Zygote32Timing", 16384);
                bootTimingsTraceLog.traceBegin("ZygoteInit");
                RuntimeInit.enableDdms();
                boolean startSystemServer = false;
                String zygoteSocketName = Zygote.PRIMARY_SOCKET_NAME;
                String abiList = null;
                boolean enableLazyPreload = false;
                for (int i = 1; i < argv.length; i++) {
                    if ("start-system-server".equals(argv[i])) {
                        startSystemServer = true;
                    } else if ("--enable-lazy-preload".equals(argv[i])) {
                        enableLazyPreload = true;
                    } else if (argv[i].startsWith("--abi-list=")) {
                        abiList = argv[i].substring("--abi-list=".length());
                    } else if (argv[i].startsWith(SOCKET_NAME_ARG)) {
                        zygoteSocketName = argv[i].substring(SOCKET_NAME_ARG.length());
                    } else {
                        throw new RuntimeException("Unknown command line argument: " + argv[i]);
                    }
                }
                boolean isPrimaryZygote = zygoteSocketName.equals(Zygote.PRIMARY_SOCKET_NAME);
                if (abiList != null) {
                    if (!enableLazyPreload) {
                        bootTimingsTraceLog.traceBegin("ZygotePreload");
                        EventLog.writeEvent((int) LOG_BOOT_PROGRESS_PRELOAD_START, SystemClock.uptimeMillis());
                        addBootEvent("Zygote:Preload Start");
                        Phoenix.setBootstage(Phoenix.ANDROID_ZYGOTE_PRELOAD_START);
                        preload(bootTimingsTraceLog);
                        Phoenix.setBootstage(Phoenix.ANDROID_ZYGOTE_PRELOAD_END);
                        EventLog.writeEvent((int) LOG_BOOT_PROGRESS_PRELOAD_END, SystemClock.uptimeMillis());
                        bootTimingsTraceLog.traceEnd();
                    } else {
                        Zygote.resetNicePriority();
                    }
                    bootTimingsTraceLog.traceBegin("PostZygoteInitGC");
                    if (!enableLazyPreload) {
                        Phoenix.setBootstage(Phoenix.ANDROID_ZYGOTE_GC_INIT_START);
                    }
                    gcAndFinalize();
                    if (!enableLazyPreload) {
                        Phoenix.setBootstage(Phoenix.ANDROID_ZYGOTE_GC_INIT_END);
                    }
                    bootTimingsTraceLog.traceEnd();
                    bootTimingsTraceLog.traceEnd();
                    Trace.setTracingEnabled(false, 0);
                    Zygote.initNativeState(isPrimaryZygote);
                    addBootEvent("Zygote:Preload End");
                    ZygoteHooks.stopZygoteNoThreadCreation();
                    ZygoteServer zygoteServer2 = new ZygoteServer(isPrimaryZygote);
                    if (!startSystemServer || (r = forkSystemServer(abiList, zygoteSocketName, zygoteServer2)) == null) {
                        Log.i(TAG, "Accepting command socket connections");
                        Runnable caller = zygoteServer2.runSelectLoop(abiList);
                        zygoteServer2.closeServerSocket();
                        if (caller != null) {
                            caller.run();
                            return;
                        }
                        return;
                    }
                    r.run();
                    zygoteServer2.closeServerSocket();
                    return;
                }
                throw new RuntimeException("No ABI list supplied.");
            } catch (Throwable ex) {
                if (0 != 0) {
                    zygoteServer.closeServerSocket();
                }
                throw ex;
            }
        } catch (ErrnoException ex2) {
            throw new RuntimeException("Failed to setpgid(0,0)", ex2);
        }
    }

    private static boolean hasSecondZygote(String abiList) {
        return !SystemProperties.get("ro.product.cpu.abilist").equals(abiList);
    }

    private static void waitForSecondaryZygote(String socketName) {
        String otherZygoteName = Zygote.PRIMARY_SOCKET_NAME;
        if (otherZygoteName.equals(socketName)) {
            otherZygoteName = Zygote.SECONDARY_SOCKET_NAME;
        }
        ZygoteProcess.waitForConnectionToZygote(otherZygoteName);
    }

    static boolean isPreloadComplete() {
        return sPreloadComplete;
    }

    private ZygoteInit() {
    }

    public static final Runnable zygoteInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) {
        Trace.traceBegin(64, "ZygoteInit");
        RuntimeInit.redirectLogStreams();
        RuntimeInit.commonInit();
        nativeZygoteInit();
        return RuntimeInit.applicationInit(targetSdkVersion, argv, classLoader);
    }

    static final Runnable childZygoteInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) {
        RuntimeInit.Arguments args = new RuntimeInit.Arguments(argv);
        return RuntimeInit.findStaticMain(args.startClass, args.startArgs, classLoader);
    }
}
