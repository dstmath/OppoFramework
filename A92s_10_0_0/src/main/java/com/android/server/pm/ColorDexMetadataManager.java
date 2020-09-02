package com.android.server.pm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.dex.ArtManager;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColorDexMetadataManager implements IColorDexMetadataManager {
    private static final String TAG = "ColorDexMetadataManager";
    public static boolean sDebugDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static volatile ColorDexMetadataManager sInstance = null;
    private OppoBaseInstaller mBaseInstaller;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mDebugSwitch = (sDebugDetail | this.mDynamicDebug);
    private boolean mDynamicDebug = false;
    /* access modifiers changed from: private */
    public ExecutorService mExecutor = Executors.newCachedThreadPool();
    private Object mInstallLock;
    @GuardedBy({"mInstallLock"})
    private Installer mInstaller;
    private ColorDexMetadataManagerHelper mManagerHelper = null;
    private final Object mManagerLock = new Object();
    private IPackageManager mPackageManager;
    private PackageManagerService mPms = null;
    private IColorPackageManagerServiceEx mPmsEx = null;
    private FileObserverPolicy mRestoreProfileObserver;
    private boolean mSystemReady = false;

    public static ColorDexMetadataManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDexMetadataManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDexMetadataManager();
                }
            }
        }
        return sInstance;
    }

    public ColorDexMetadataManager() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "constructor!");
        }
        this.mManagerHelper = ColorDexMetadataManagerHelper.getInstance();
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "init!");
        }
        this.mPmsEx = pmsEx;
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = this.mPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            this.mPms = iColorPackageManagerServiceEx.getPackageManagerService();
            this.mContext = this.mPms.mContext;
        }
    }

    public void onStart(IPackageManager pm, Installer installer, Object installLock) {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "onStart!");
        }
        this.mPackageManager = pm;
        this.mInstaller = installer;
        this.mInstallLock = installLock;
        this.mBaseInstaller = typeCasting(this.mInstaller);
    }

    public void onSystemReady(Context context) {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "onSystemReady!");
        }
        this.mContext = context;
        this.mSystemReady = true;
        this.mManagerHelper.onSystemReady(context);
        this.mRestoreProfileObserver = new FileObserverPolicy(ColorDexMetadataManagerHelper.OPPO_PROFILE_RESTORE_PATH);
        this.mRestoreProfileObserver.startWatching();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:45:0x011f, code lost:
        return false;
     */
    public boolean dumpProfile(PackageParser.Package pkg) {
        String packageName = pkg.packageName;
        if (!this.mSystemReady) {
            if (this.mDebugSwitch) {
                Slog.w(TAG, "dumpProfile: system not ready!");
            }
            return false;
        } else if (this.mManagerHelper.isNeedToDumpProfile(pkg)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "dumpProfile: need to dump profile for " + packageName);
            }
            int userId = ActivityManager.getCurrentUser();
            if (userId < 0) {
                Slog.wtf(TAG, "Invalid user id: " + userId);
                return false;
            }
            String profilePath = ColorDexMetadataManagerHelper.USER_REFERENCE_PROFILE_PATH + packageName + ColorDexMetadataManagerHelper.USER_REFERENCE_PROFILE_NAME;
            if (!this.mManagerHelper.isFileValid(new File(profilePath))) {
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "dumpProfile: invalid reference profile: " + profilePath);
                }
                return false;
            }
            String codePath = pkg.baseCodePath;
            if (!PackageParser.isApkPath(codePath)) {
                if (this.mDebugSwitch) {
                    Slog.w(TAG, "dumpProfile: code path is not an apk: " + codePath);
                }
                return false;
            }
            String outputMethodPath = ColorDexMetadataManagerHelper.OPPO_PROFMAN_DIR + packageName + ColorDexMetadataManagerHelper.HOT_METHOD_FILE_EXTENSION;
            try {
                synchronized (this.mManagerLock) {
                    if (this.mBaseInstaller != null) {
                        if (this.mBaseInstaller.dumpAppClassAndMethod(userId, packageName, profilePath, codePath, outputMethodPath)) {
                            return true;
                        }
                        Slog.e(TAG, "dumpProfile: failed to dump app class and method for " + packageName);
                        this.mManagerHelper.uploadToDcs(false, "dumpProfile: failed to dump app class and method for " + packageName, pkg, ColorDexMetadataManagerHelper.DM_REUSE_EVENT_ID);
                        return false;
                    } else if (this.mDebugSwitch) {
                        Slog.w(TAG, "oppoBaseInstaller is null!");
                    }
                }
            } catch (Exception e) {
                Slog.e(TAG, "Exception at dumpProfile: ", e);
                this.mManagerHelper.uploadToDcs(false, "Exception at dumpProfile: " + e, pkg, ColorDexMetadataManagerHelper.DM_REUSE_EVENT_ID);
                return false;
            }
        } else {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "dumpProfile: no need to dump profile for " + packageName);
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0136, code lost:
        r14.mManagerHelper.deleteFile(new java.io.File(r9));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0142, code lost:
        if (r14.mDebugSwitch == false) goto L_0x015a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0144, code lost:
        android.util.Slog.d(com.android.server.pm.ColorDexMetadataManager.TAG, "createProfile done, delete hot method file at: " + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x015a, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x015d, code lost:
        r14.mManagerHelper.deleteFile(new java.io.File(r9));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0169, code lost:
        if (r14.mDebugSwitch == false) goto L_0x0181;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x016b, code lost:
        android.util.Slog.d(com.android.server.pm.ColorDexMetadataManager.TAG, "createProfile done, delete hot method file at: " + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0181, code lost:
        r14.mManagerHelper.uploadToDcs(false, "createProfile done!", r15, com.android.server.pm.ColorDexMetadataManagerHelper.DM_REUSE_EVENT_ID);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x018c, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0199, code lost:
        r14.mManagerHelper.deleteFile(new java.io.File(r9));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x01a5, code lost:
        if (r14.mDebugSwitch == false) goto L_0x01bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01a7, code lost:
        android.util.Slog.d(com.android.server.pm.ColorDexMetadataManager.TAG, "createProfile done, delete hot method file at: " + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01bd, code lost:
        return false;
     */
    public boolean createProfile(PackageParser.Package pkg) {
        String packageName = pkg.packageName;
        if (!this.mSystemReady) {
            if (this.mDebugSwitch) {
                Slog.w(TAG, "createProfile: system not ready!");
            }
            return false;
        } else if (this.mManagerHelper.isNeedToCreateProfile(pkg)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "createProfile: need to create profile for " + packageName);
            }
            int userId = ActivityManager.getCurrentUser();
            if (userId < 0) {
                Slog.wtf(TAG, "Invalid user id: " + userId);
                return false;
            }
            String hotMethodPath = ColorDexMetadataManagerHelper.OPPO_PROFMAN_DIR + packageName + ColorDexMetadataManagerHelper.HOT_METHOD_FILE_EXTENSION;
            if (!this.mManagerHelper.isFileValid(new File(hotMethodPath))) {
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "createProfile: invalid hot method file  " + hotMethodPath);
                }
                this.mManagerHelper.deleteFile(new File(hotMethodPath));
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "createProfile done, delete hot method file at: " + hotMethodPath);
                }
                return false;
            }
            String codePath = pkg.baseCodePath;
            if (!PackageParser.isApkPath(codePath)) {
                if (this.mDebugSwitch) {
                    Slog.w(TAG, "createProfile: code path is not an apk: " + codePath);
                }
                return false;
            }
            String outputProfilePath = ColorDexMetadataManagerHelper.OPPO_PROFILE_UPDATE_PATH + this.mManagerHelper.buildPackagePath(pkg) + ColorDexMetadataManagerHelper.PROFILE_FILE_EXTENSION;
            try {
                synchronized (this.mManagerLock) {
                    if (this.mBaseInstaller != null) {
                        if (!this.mBaseInstaller.createAppProfile(userId, packageName, hotMethodPath, codePath, outputProfilePath)) {
                            Slog.e(TAG, "createProfile: failed to create profile for " + packageName);
                            this.mManagerHelper.uploadToDcs(false, "createProfile: failed to create profile for " + packageName, pkg, ColorDexMetadataManagerHelper.DM_REUSE_EVENT_ID);
                        }
                    } else if (this.mDebugSwitch) {
                        Slog.w(TAG, "oppoBaseInstaller is null!");
                    }
                }
            } catch (Exception e) {
                Slog.e(TAG, "Exception at createProfile: ", e);
                this.mManagerHelper.uploadToDcs(false, "Exception at createProfile: " + e, pkg, ColorDexMetadataManagerHelper.DM_REUSE_EVENT_ID);
                this.mManagerHelper.deleteFile(new File(hotMethodPath));
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "createProfile done, delete hot method file at: " + hotMethodPath);
                }
                return false;
            } catch (Throwable th) {
                this.mManagerHelper.deleteFile(new File(hotMethodPath));
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "createProfile done, delete hot method file at: " + hotMethodPath);
                }
                throw th;
            }
        } else {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "createProfile: no need to create profile for " + packageName);
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:98:0x025e  */
    public boolean mergeProfile(PackageParser.Package pkg) {
        String dexMetadataPath;
        File dexMetadata;
        Object obj;
        String packageName = pkg.packageName;
        if (!this.mSystemReady) {
            if (this.mDebugSwitch) {
                Slog.w(TAG, "mergeProfile: system not ready!");
            }
            return false;
        }
        int type = this.mManagerHelper.getDexMetadataType(pkg);
        if (this.mManagerHelper.isNeedToMergeProfile(type)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "mergeProfile: need to merge profile for " + packageName);
            }
            int userId = ActivityManager.getCurrentUser();
            if (userId < 0) {
                Slog.wtf(TAG, "Invalid user id: " + userId);
                return false;
            }
            int appId = UserHandle.getAppId(pkg.applicationInfo.uid);
            if (appId < 0) {
                Slog.wtf(TAG, "Invalid app id: " + appId);
                return false;
            }
            String profileName = ArtManager.getProfileName((String) null);
            String codePath = pkg.baseCodePath;
            if (!PackageParser.isApkPath(codePath)) {
                if (this.mDebugSwitch) {
                    Slog.w(TAG, "mergeProfile: code path is not an apk: " + codePath);
                }
                return false;
            }
            File dexMetadata2 = this.mManagerHelper.findDexMetadataForPackage(pkg, type);
            if (dexMetadata2 == null) {
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "mergeProfile: dex metadata is null for " + packageName);
                }
                return false;
            }
            String dexMetadataPath2 = dexMetadata2.getAbsolutePath();
            if (!this.mManagerHelper.isFileValid(dexMetadata2)) {
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "mergeProfile: dex metadata is invalid at: " + dexMetadataPath2);
                }
                this.mManagerHelper.deleteFile(dexMetadata2);
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "mergeProfile done, delete profile at: " + dexMetadataPath2);
                }
                return false;
            }
            if (this.mDebugSwitch) {
                Slog.i(TAG, "mergeProfile: dex metadata found at: " + dexMetadataPath2);
            }
            try {
                Object obj2 = this.mManagerLock;
                synchronized (obj2) {
                    try {
                        if (this.mBaseInstaller != null) {
                            obj = obj2;
                            try {
                                if (!this.mBaseInstaller.updateAppProfile(packageName, userId, appId, profileName, codePath, dexMetadataPath2)) {
                                    Slog.e(TAG, "mergeProfile: failed to merge profile for " + packageName);
                                    this.mManagerHelper.uploadToDcs(false, "mergeProfile: failed to merge profile for " + packageName, pkg, ColorDexMetadataManagerHelper.DM_PREPROCESS_EVENT_ID);
                                    this.mManagerHelper.deleteFile(dexMetadata2);
                                    if (this.mDebugSwitch) {
                                        Slog.d(TAG, "mergeProfile done, delete profile at: " + dexMetadataPath2);
                                    }
                                    return false;
                                }
                                dexMetadataPath = dexMetadataPath2;
                                dexMetadata = dexMetadata2;
                                try {
                                    this.mManagerHelper.deleteFile(dexMetadata);
                                    if (!this.mDebugSwitch) {
                                        return true;
                                    }
                                    Slog.d(TAG, "mergeProfile done, delete profile at: " + dexMetadataPath);
                                    return true;
                                } catch (Throwable th) {
                                    th = th;
                                    try {
                                        throw th;
                                    } catch (Exception e) {
                                        e = e;
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                dexMetadataPath = dexMetadataPath2;
                                dexMetadata = dexMetadata2;
                                throw th;
                            }
                        } else if (this.mDebugSwitch) {
                            Slog.w(TAG, "oppoBaseInstaller is null!");
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        obj = obj2;
                        dexMetadataPath = dexMetadataPath2;
                        dexMetadata = dexMetadata2;
                        throw th;
                    }
                }
                this.mManagerHelper.deleteFile(dexMetadata2);
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "mergeProfile done, delete profile at: " + dexMetadataPath2);
                }
                return false;
            } catch (Exception e2) {
                e = e2;
                dexMetadataPath = dexMetadataPath2;
                dexMetadata = dexMetadata2;
                try {
                    Slog.e(TAG, "Exception at mergeProfile: ", e);
                    this.mManagerHelper.uploadToDcs(false, "Exception at mergeProfile: " + e, pkg, ColorDexMetadataManagerHelper.DM_PREPROCESS_EVENT_ID);
                    this.mManagerHelper.deleteFile(dexMetadata);
                    if (this.mDebugSwitch) {
                        Slog.d(TAG, "mergeProfile done, delete profile at: " + dexMetadataPath);
                    }
                    return false;
                } catch (Throwable th4) {
                    e = th4;
                    this.mManagerHelper.deleteFile(dexMetadata);
                    if (this.mDebugSwitch) {
                        Slog.d(TAG, "mergeProfile done, delete profile at: " + dexMetadataPath);
                    }
                    throw e;
                }
            } catch (Throwable th5) {
                e = th5;
                dexMetadataPath = dexMetadataPath2;
                dexMetadata = dexMetadata2;
                this.mManagerHelper.deleteFile(dexMetadata);
                if (this.mDebugSwitch) {
                }
                throw e;
            }
        } else {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "mergeProfile: no need to merge profile for " + packageName);
            }
            try {
                if (this.mManagerHelper.deleteDownloadDexMetadataForPackage(packageName) && this.mDebugSwitch) {
                    Slog.d(TAG, "delete download dexmetadata for " + packageName);
                }
            } catch (Exception e3) {
                Slog.e(TAG, "delete dex metadata exception: ", e3);
            }
            return false;
        }
    }

    public boolean isPrecompileEnable() {
        if (this.mSystemReady) {
            boolean result = this.mManagerHelper.isPrecompileEnable();
            if (this.mDebugSwitch) {
                Slog.i(TAG, "oppo precompile profile enable: " + result);
            }
            return result;
        } else if (!this.mDebugSwitch) {
            return false;
        } else {
            Slog.w(TAG, "precompile: system not ready!");
            return false;
        }
    }

    public void registerToCompile(String packageName) {
        if (this.mSystemReady) {
            this.mManagerHelper.registerToCompile(packageName);
            if (this.mDebugSwitch) {
                Slog.i(TAG, "register to background compile: " + packageName);
            }
        } else if (this.mDebugSwitch) {
            Slog.w(TAG, "register to compile: system not ready!");
        }
    }

    public boolean isNeedToDexOptForce(String packageName) {
        if (this.mSystemReady) {
            boolean result = this.mManagerHelper.isNeedToDexOptForce(packageName);
            if (this.mDebugSwitch && result) {
                Slog.d(TAG, "need to dexopt force for: " + packageName);
            }
            return result;
        } else if (!this.mDebugSwitch) {
            return false;
        } else {
            Slog.w(TAG, "dexopt force: system not ready!");
            return false;
        }
    }

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8) {
                if (ColorDexMetadataManager.this.mDebugSwitch) {
                    Slog.d(ColorDexMetadataManager.TAG, "FileObserverPolicy: restore profile found at: " + path);
                }
                String[] splitString = path.split("_");
                if (splitString.length > 0) {
                    final String packageName = splitString[0];
                    if (!ColorDexMetadataUtils.isValidPackageName(packageName)) {
                        Slog.w(ColorDexMetadataManager.TAG, "FileObserverPolicy: invalid package name: " + packageName);
                        return;
                    }
                    final String restoredProfilePath = this.mFocusPath + path;
                    ColorDexMetadataManager.this.mExecutor.execute(new Runnable() {
                        /* class com.android.server.pm.ColorDexMetadataManager.FileObserverPolicy.AnonymousClass1 */

                        public void run() {
                            if (ColorDexMetadataManager.this.restoreProfile(packageName, restoredProfilePath)) {
                                ColorDexMetadataManager.this.registerToCompile(packageName);
                            }
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean restoreProfile(String packageName, String restoredProfilePath) {
        Object obj;
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(restoredProfilePath)) {
            return false;
        }
        File restoredProfile = new File(restoredProfilePath);
        if (!this.mManagerHelper.isFileValid(restoredProfile)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "restoreProfile: profile is invalid at: " + restoredProfilePath);
            }
            this.mManagerHelper.deleteFile(restoredProfile);
            if (this.mDebugSwitch) {
                Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
            }
            return false;
        } else if (this.mManagerHelper.isNeedToMergeProfile(3)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "restoreProfile: need to restore profile for " + packageName);
            }
            try {
                int userId = ActivityManager.getCurrentUser();
                if (userId < 0) {
                    Slog.wtf(TAG, "Invalid user id: " + userId);
                    this.mManagerHelper.deleteFile(restoredProfile);
                    if (this.mDebugSwitch) {
                        Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                    }
                    return false;
                } else if (this.mPackageManager == null) {
                    Slog.w(TAG, "restoreProfile: mPackageManager is null");
                    this.mManagerHelper.deleteFile(restoredProfile);
                    if (this.mDebugSwitch) {
                        Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                    }
                    return false;
                } else {
                    int appId = UserHandle.getAppId(this.mPackageManager.getPackageUid(packageName, 8192, 0));
                    if (appId < 0) {
                        Slog.wtf(TAG, "Invalid app id: " + appId);
                        this.mManagerHelper.deleteFile(restoredProfile);
                        if (this.mDebugSwitch) {
                            Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                        }
                        return false;
                    }
                    String profileName = ArtManager.getProfileName((String) null);
                    PackageInfo packageInfo = this.mPackageManager.getPackageInfo(packageName, 0, userId);
                    if (packageInfo == null) {
                        Slog.w(TAG, "restoreProfile: package not found " + packageName);
                        this.mManagerHelper.deleteFile(restoredProfile);
                        if (this.mDebugSwitch) {
                            Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                        }
                        return false;
                    }
                    String codePath = packageInfo.applicationInfo.getBaseCodePath();
                    if (!PackageParser.isApkPath(codePath)) {
                        if (this.mDebugSwitch) {
                            Slog.w(TAG, "restoreProfile: code path is not an apk: " + codePath);
                        }
                        this.mManagerHelper.deleteFile(restoredProfile);
                        if (this.mDebugSwitch) {
                            Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                        }
                        return false;
                    }
                    Object obj2 = this.mManagerLock;
                    synchronized (obj2) {
                        try {
                            if (this.mBaseInstaller != null) {
                                obj = obj2;
                                try {
                                    if (!this.mBaseInstaller.updateAppProfile(packageName, userId, appId, profileName, codePath, restoredProfilePath)) {
                                        Slog.e(TAG, "restoreProfile: failed to restore profile for " + packageName);
                                        this.mManagerHelper.deleteFile(restoredProfile);
                                        if (this.mDebugSwitch) {
                                            Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                                        }
                                        return false;
                                    }
                                    this.mManagerHelper.deleteFile(restoredProfile);
                                    if (!this.mDebugSwitch) {
                                        return true;
                                    }
                                    Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                                    return true;
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            } else if (this.mDebugSwitch) {
                                Slog.w(TAG, "oppoBaseInstaller is null!");
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            obj = obj2;
                            throw th;
                        }
                    }
                    this.mManagerHelper.deleteFile(restoredProfile);
                    if (this.mDebugSwitch) {
                        Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                    }
                    return false;
                }
            } catch (Exception e) {
                Slog.e(TAG, "Exception at restoreProfile: ", e);
                this.mManagerHelper.deleteFile(restoredProfile);
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                }
                return false;
            } catch (Throwable th3) {
                this.mManagerHelper.deleteFile(restoredProfile);
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "restoreProfile done, delete profile at: " + restoredProfilePath);
                }
                throw th3;
            }
        } else {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "restoreProfile: no need to restore profile for " + packageName);
            }
            return false;
        }
    }

    private static OppoBaseInstaller typeCasting(Installer installer) {
        return (OppoBaseInstaller) ColorTypeCastingHelper.typeCasting(OppoBaseInstaller.class, installer);
    }

    public void recordCompiledApp(String pkgName) {
        if (switchOfUploadDcsForDexMetadata()) {
            this.mManagerHelper.recordCompiledApp(pkgName);
        } else if (this.mDebugSwitch) {
            Slog.d(TAG, "switchOfUploadDcsForDexMetadata is disable, recordCompiledApp return");
        }
    }

    public void removeCompiledApp(String pkgName) {
        if (switchOfUploadDcsForDexMetadata()) {
            this.mManagerHelper.removeCompiledApp(pkgName);
        } else if (this.mDebugSwitch) {
            Slog.d(TAG, "switchOfUploadDcsForDexMetadata is disable, removeCompiledApp return");
        }
    }

    public boolean switchOfUploadDcsForDexMetadata() {
        return this.mManagerHelper.switchOfUploadDcsForDexMetadata();
    }
}
