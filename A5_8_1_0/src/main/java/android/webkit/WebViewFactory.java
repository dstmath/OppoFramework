package android.webkit;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.Trace;
import android.util.AndroidException;
import android.util.AndroidRuntimeException;
import android.util.ArraySet;
import android.util.Log;
import android.webkit.IWebViewUpdateService.Stub;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.lang.reflect.Method;

public final class WebViewFactory {
    private static final String CHROMIUM_WEBVIEW_FACTORY = "com.android.webview.chromium.WebViewChromiumFactoryProviderForOMR1";
    private static final String CHROMIUM_WEBVIEW_FACTORY_METHOD = "create";
    private static final String CHROMIUM_WEBVIEW_FACTORY_PRE_O = "com.android.webview.chromium.WebViewChromiumFactoryProvider";
    public static final String CHROMIUM_WEBVIEW_VMSIZE_SIZE_PROPERTY = "persist.sys.webview.vmsize";
    private static final boolean DEBUG = false;
    public static final int LIBLOAD_ADDRESS_SPACE_NOT_RESERVED = 2;
    public static final int LIBLOAD_FAILED_JNI_CALL = 7;
    public static final int LIBLOAD_FAILED_LISTING_WEBVIEW_PACKAGES = 4;
    public static final int LIBLOAD_FAILED_TO_FIND_NAMESPACE = 10;
    public static final int LIBLOAD_FAILED_TO_LOAD_LIBRARY = 6;
    public static final int LIBLOAD_FAILED_TO_OPEN_RELRO_FILE = 5;
    public static final int LIBLOAD_FAILED_WAITING_FOR_RELRO = 3;
    public static final int LIBLOAD_FAILED_WAITING_FOR_WEBVIEW_REASON_UNKNOWN = 8;
    public static final int LIBLOAD_SUCCESS = 0;
    public static final int LIBLOAD_WRONG_PACKAGE_NAME = 1;
    private static final String LOGTAG = "WebViewFactory";
    private static final String NULL_WEBVIEW_FACTORY = "com.android.webview.nullwebview.NullWebViewFactoryProvider";
    private static String WEBVIEW_UPDATE_SERVICE_NAME = "webviewupdate";
    private static PackageInfo sPackageInfo;
    private static WebViewFactoryProvider sProviderInstance;
    private static final Object sProviderLock = new Object();

    static class MissingWebViewPackageException extends Exception {
        public MissingWebViewPackageException(String message) {
            super(message);
        }

        public MissingWebViewPackageException(Exception e) {
            super(e);
        }
    }

    private static String getWebViewPreparationErrorReason(int error) {
        switch (error) {
            case 3:
                return "Time out waiting for Relro files being created";
            case 4:
                return "No WebView installed";
            case 8:
                return "Crashed for unknown reason";
            default:
                return "Unknown";
        }
    }

    public static String getWebViewLibrary(ApplicationInfo ai) {
        if (ai.metaData != null) {
            return ai.metaData.getString("com.android.webview.WebViewLibrary");
        }
        return null;
    }

    public static PackageInfo getLoadedPackageInfo() {
        PackageInfo packageInfo;
        synchronized (sProviderLock) {
            packageInfo = sPackageInfo;
        }
        return packageInfo;
    }

    public static Class<WebViewFactoryProvider> getWebViewProviderClass(ClassLoader clazzLoader) throws ClassNotFoundException {
        return Class.forName(CHROMIUM_WEBVIEW_FACTORY, true, clazzLoader);
    }

    private static Class<WebViewFactoryProvider> getWebViewProviderClassForPackage(ClassLoader clazzLoader, String pkgName) throws ClassNotFoundException {
        return Class.forName(pkgName != null ? CHROMIUM_WEBVIEW_FACTORY_PRE_O : CHROMIUM_WEBVIEW_FACTORY, true, clazzLoader);
    }

    public static int loadWebViewNativeLibraryFromPackage(String packageName, ClassLoader clazzLoader) {
        try {
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            if (response.status != 0 && response.status != 3) {
                return response.status;
            }
            if (!response.packageInfo.packageName.equals(packageName)) {
                return 1;
            }
            try {
                try {
                    int loadNativeRet = WebViewLibraryLoader.loadNativeLibrary(clazzLoader, AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(packageName, 268435584));
                    if (loadNativeRet == 0) {
                        return response.status;
                    }
                    return loadNativeRet;
                } catch (MissingWebViewPackageException e) {
                    Log.e(LOGTAG, "Couldn't load native library: " + e);
                    return 6;
                }
            } catch (NameNotFoundException e2) {
                Log.e(LOGTAG, "Couldn't find package " + packageName);
                return 1;
            }
        } catch (RemoteException e3) {
            Log.e(LOGTAG, "error waiting for relro creation", e3);
            return 8;
        }
    }

    static WebViewFactoryProvider getProvider() {
        synchronized (sProviderLock) {
            WebViewFactoryProvider webViewFactoryProvider;
            if (sProviderInstance != null) {
                webViewFactoryProvider = sProviderInstance;
                return webViewFactoryProvider;
            }
            int uid = Process.myUid();
            if (uid == 0 || uid == 1000 || uid == 1001 || uid == 1027 || uid == 1002) {
                throw new UnsupportedOperationException("For security reasons, WebView is not allowed in privileged processes");
            }
            ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
            Trace.traceBegin(16, "WebViewFactory.getProvider()");
            try {
                Method staticFactory;
                Class providerClass = getProviderClass();
                try {
                    staticFactory = providerClass.getMethod(CHROMIUM_WEBVIEW_FACTORY_METHOD, new Class[]{WebViewDelegate.class});
                } catch (Exception e) {
                }
                Trace.traceBegin(16, "WebViewFactoryProvider invocation");
                if (staticFactory == null) {
                    try {
                        sProviderInstance = (WebViewFactoryProvider) providerClass.getConstructor(new Class[]{WebViewDelegate.class}).newInstance(new Object[]{new WebViewDelegate()});
                    } catch (Exception e2) {
                        Log.e(LOGTAG, "error instantiating provider", e2);
                        throw new AndroidRuntimeException(e2);
                    } catch (Throwable th) {
                        Trace.traceEnd(16);
                    }
                } else {
                    sProviderInstance = (WebViewFactoryProvider) staticFactory.invoke(null, new Object[]{new WebViewDelegate()});
                }
                webViewFactoryProvider = sProviderInstance;
                Trace.traceEnd(16);
                Trace.traceEnd(16);
                StrictMode.setThreadPolicy(oldPolicy);
                return webViewFactoryProvider;
            } finally {
                Trace.traceEnd(16);
                StrictMode.setThreadPolicy(oldPolicy);
            }
        }
    }

    private static boolean signaturesEquals(Signature[] s1, Signature[] s2) {
        int i = 0;
        if (s1 == null) {
            boolean z;
            if (s2 == null) {
                z = true;
            }
            return z;
        } else if (s2 == null) {
            return false;
        } else {
            ArraySet<Signature> set1 = new ArraySet();
            for (Signature signature : s1) {
                set1.add(signature);
            }
            ArraySet<Signature> set2 = new ArraySet();
            int length = s2.length;
            while (i < length) {
                set2.add(s2[i]);
                i++;
            }
            return set1.equals(set2);
        }
    }

    private static void verifyPackageInfo(PackageInfo chosen, PackageInfo toUse) throws MissingWebViewPackageException {
        if (!chosen.packageName.equals(toUse.packageName)) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, packageName mismatch, expected: " + chosen.packageName + " actual: " + toUse.packageName);
        } else if (chosen.versionCode > toUse.versionCode) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, version code is lower than expected: " + chosen.versionCode + " actual: " + toUse.versionCode);
        } else if (getWebViewLibrary(toUse.applicationInfo) == null) {
            throw new MissingWebViewPackageException("Tried to load an invalid WebView provider: " + toUse.packageName);
        } else if (!signaturesEquals(chosen.signatures, toUse.signatures)) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, signature mismatch");
        }
    }

    public static String getWebViewPackageName(String initialPackageName) {
        if (ActivityThread.inCptWhiteList(MetricsEvent.ACTION_PERMISSION_REVOKE_READ_CALL_LOG, initialPackageName)) {
            return "com.oppo.webview";
        }
        return null;
    }

    private static void fixupStubApplicationInfo(ApplicationInfo ai, PackageManager pm) throws MissingWebViewPackageException {
        String donorPackageName = null;
        if (ai.metaData != null) {
            donorPackageName = ai.metaData.getString("com.android.webview.WebViewDonorPackage");
        }
        if (donorPackageName != null) {
            try {
                ApplicationInfo donorInfo = pm.getPackageInfo(donorPackageName, 270541824).applicationInfo;
                ai.sourceDir = donorInfo.sourceDir;
                ai.splitSourceDirs = donorInfo.splitSourceDirs;
                ai.nativeLibraryDir = donorInfo.nativeLibraryDir;
                ai.secondaryNativeLibraryDir = donorInfo.secondaryNativeLibraryDir;
                ai.primaryCpuAbi = donorInfo.primaryCpuAbi;
                ai.secondaryCpuAbi = donorInfo.secondaryCpuAbi;
            } catch (NameNotFoundException e) {
                throw new MissingWebViewPackageException("Failed to find donor package: " + donorPackageName);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0065 A:{ExcHandler: android.os.RemoteException (r2_0 'e' android.util.AndroidException), Splitter: B:1:0x0005} */
    /* JADX WARNING: Missing block: B:21:0x0065, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:23:0x007f, code:
            throw new android.webkit.WebViewFactory.MissingWebViewPackageException("Failed to load WebView provider: " + r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Context getWebViewContextAndSetProvider() throws MissingWebViewPackageException {
        Application initialApplication = AppGlobals.getInitialApplication();
        try {
            String packageName = getWebViewPackageName(AppGlobals.getInitialPackage());
            if (packageName != null) {
                try {
                    sPackageInfo = initialApplication.getPackageManager().getPackageInfo(packageName, 268444864);
                    return initialApplication.createApplicationContext(sPackageInfo.applicationInfo, 3);
                } catch (NameNotFoundException e) {
                }
            }
            Trace.traceBegin(16, "WebViewUpdateService.waitForAndGetProvider()");
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            Trace.traceEnd(16);
            if (response.status == 0 || response.status == 3) {
                Trace.traceBegin(16, "ActivityManager.addPackageDependency()");
                ActivityManager.getService().addPackageDependency(response.packageInfo.packageName);
                Trace.traceEnd(16);
                PackageManager pm = initialApplication.getPackageManager();
                Trace.traceBegin(16, "PackageManager.getPackageInfo()");
                PackageInfo newPackageInfo = pm.getPackageInfo(response.packageInfo.packageName, 268444864);
                Trace.traceEnd(16);
                verifyPackageInfo(response.packageInfo, newPackageInfo);
                ApplicationInfo ai = newPackageInfo.applicationInfo;
                fixupStubApplicationInfo(ai, pm);
                Trace.traceBegin(16, "initialApplication.createApplicationContext");
                Context webViewContext = initialApplication.createApplicationContext(ai, 3);
                sPackageInfo = newPackageInfo;
                Trace.traceEnd(16);
                return webViewContext;
            }
            throw new MissingWebViewPackageException("Failed to load WebView provider: " + getWebViewPreparationErrorReason(response.status));
        } catch (AndroidException e2) {
        } catch (Throwable th) {
            Trace.traceEnd(16);
        }
    }

    private static Class<WebViewFactoryProvider> getProviderClass() {
        Application initialApplication = AppGlobals.getInitialApplication();
        try {
            Trace.traceBegin(16, "WebViewFactory.getWebViewContextAndSetProvider()");
            Context webViewContext = getWebViewContextAndSetProvider();
            Trace.traceEnd(16);
            Log.i(LOGTAG, "Loading " + sPackageInfo.packageName + " version " + sPackageInfo.versionName + " (code " + sPackageInfo.versionCode + ")");
            Trace.traceBegin(16, "WebViewFactory.getChromiumProviderClass()");
            try {
                initialApplication.getAssets().addAssetPathAsSharedLibrary(webViewContext.getApplicationInfo().sourceDir);
                ClassLoader clazzLoader = webViewContext.getClassLoader();
                Trace.traceBegin(16, "WebViewFactory.loadNativeLibrary()");
                WebViewLibraryLoader.loadNativeLibrary(clazzLoader, sPackageInfo);
                Trace.traceEnd(16);
                Trace.traceBegin(16, "Class.forName()");
                Class<WebViewFactoryProvider> webViewProviderClassForPackage = getWebViewProviderClassForPackage(clazzLoader, getWebViewPackageName(AppGlobals.getInitialPackage()));
                Trace.traceEnd(16);
                Trace.traceEnd(16);
                return webViewProviderClassForPackage;
            } catch (Exception e) {
                try {
                    Log.e(LOGTAG, "error loading provider", e);
                    throw new AndroidRuntimeException(e);
                } catch (Throwable th) {
                    Trace.traceEnd(16);
                }
            } catch (Throwable th2) {
                Trace.traceEnd(16);
            }
        } catch (Exception e2) {
            try {
                return Class.forName(NULL_WEBVIEW_FACTORY);
            } catch (ClassNotFoundException e3) {
                Log.e(LOGTAG, "Chromium WebView package does not exist", e2);
                throw new AndroidRuntimeException(e2);
            }
        } catch (Throwable th3) {
            Trace.traceEnd(16);
        }
    }

    public static void prepareWebViewInZygote() {
        try {
            WebViewLibraryLoader.reserveAddressSpaceInZygote();
        } catch (Throwable t) {
            Log.e(LOGTAG, "error preparing native loader", t);
        }
    }

    private static int prepareWebViewInSystemServer(String[] nativeLibraryPaths) {
        int numRelros = 0;
        if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
            WebViewLibraryLoader.createRelroFile(false, nativeLibraryPaths);
            numRelros = 1;
        }
        if (Build.SUPPORTED_64_BIT_ABIS.length <= 0) {
            return numRelros;
        }
        WebViewLibraryLoader.createRelroFile(true, nativeLibraryPaths);
        return numRelros + 1;
    }

    public static int onWebViewProviderChanged(PackageInfo packageInfo) {
        String[] nativeLibs = null;
        String originalSourceDir = packageInfo.applicationInfo.sourceDir;
        try {
            fixupStubApplicationInfo(packageInfo.applicationInfo, AppGlobals.getInitialApplication().getPackageManager());
            nativeLibs = WebViewLibraryLoader.updateWebViewZygoteVmSize(packageInfo);
        } catch (Throwable t) {
            Log.e(LOGTAG, "error preparing webview native library", t);
        }
        WebViewZygote.onWebViewProviderChanged(packageInfo, originalSourceDir);
        return prepareWebViewInSystemServer(nativeLibs);
    }

    public static IWebViewUpdateService getUpdateService() {
        return Stub.asInterface(ServiceManager.getService(WEBVIEW_UPDATE_SERVICE_NAME));
    }
}
