package com.android.server.pm;

import android.app.ActivityManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import oppo.util.OppoStatistics;

public class OppoStatsNotLaunchedAppUtils {
    private static final String COLD_LAUNCH_TIME = "ColdLaunchTime";
    private static final String COLD_LAUNCH_TIME_EVENT_ID = "cold_launch_time";
    private static final String COMPILED = "Compiled";
    /* access modifiers changed from: private */
    public static final boolean DEBUG_SWITCH = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String DM_LOG_TAG = "DexMetadata";
    private static final String LONG_VERSION_CODE = "LongVersionCode";
    private static final String PACKAGE_NAME = "PackageName";
    private static final String SHORT_COMPONENT_NAME = "ShortComponentName";
    private static final String TAG = "OppoStatsNotLaunchedAppUtils";
    private static final String VERSION_NAME = "VersionName";
    private static volatile OppoStatsNotLaunchedAppUtils sOppoStatLaunchedAppUtils = null;
    private HashMap<String, Long> mCompiledApps = new HashMap<>();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public FileOperatorInternal mFileOperationInternal = new FileOperatorInternal();
    /* access modifiers changed from: private */
    public HashMap<String, Long> mNotLaunchedApps = new HashMap<>();
    /* access modifiers changed from: private */
    public IPackageManager mPackageManager;
    private final ExecutorService mServicePool = Executors.newSingleThreadExecutor();

    public static OppoStatsNotLaunchedAppUtils getInstance() {
        if (sOppoStatLaunchedAppUtils == null) {
            synchronized (OppoStatsNotLaunchedAppUtils.class) {
                if (sOppoStatLaunchedAppUtils == null) {
                    sOppoStatLaunchedAppUtils = new OppoStatsNotLaunchedAppUtils();
                }
            }
        }
        return sOppoStatLaunchedAppUtils;
    }

    private OppoStatsNotLaunchedAppUtils() {
        final File notLaunchedAppFile = new File("/data/oppo/profiles/not_launched_apps.txt");
        if (notLaunchedAppFile.exists()) {
            this.mServicePool.execute(new Runnable() {
                /* class com.android.server.pm.OppoStatsNotLaunchedAppUtils.AnonymousClass1 */

                public void run() {
                    OppoStatsNotLaunchedAppUtils oppoStatsNotLaunchedAppUtils = OppoStatsNotLaunchedAppUtils.this;
                    HashMap unused = oppoStatsNotLaunchedAppUtils.mNotLaunchedApps = oppoStatsNotLaunchedAppUtils.mFileOperationInternal.readDataFromFile(notLaunchedAppFile);
                }
            });
        }
    }

    public void init(Context context) {
        this.mContext = context;
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService(Settings.ATTR_PACKAGE));
    }

    private boolean isFirstLaunch(ApplicationInfo appInfo) {
        if (this.mNotLaunchedApps.isEmpty() || appInfo == null) {
            return false;
        }
        long currentLongVersionCode = appInfo.longVersionCode;
        try {
            if (!this.mNotLaunchedApps.containsKey(appInfo.packageName)) {
                return false;
            }
            if (currentLongVersionCode == this.mNotLaunchedApps.get(appInfo.packageName).longValue()) {
                this.mNotLaunchedApps.remove(appInfo.packageName);
                File outFile = new File("/data/oppo/profiles/not_launched_apps.txt");
                if (!outFile.exists()) {
                    return true;
                }
                this.mFileOperationInternal.writeDataToFile(this.mNotLaunchedApps, outFile);
                return true;
            }
            Slog.w(TAG, "isFirstLaunch return false unexpectly");
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "isFirstLaunch Exception: " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean isDcsSwitchEnable() {
        if (OppoFeatureCache.get(IColorDexMetadataManager.DEFAULT).switchOfUploadDcsForDexMetadata()) {
            return true;
        }
        return false;
    }

    public void uploadColdLaunchTimeToDcs(final ApplicationInfo appInfo, final String shortComponentName, final long coldLaunchTime) {
        this.mServicePool.execute(new Runnable() {
            /* class com.android.server.pm.OppoStatsNotLaunchedAppUtils.AnonymousClass2 */

            public void run() {
                if (OppoStatsNotLaunchedAppUtils.this.isNeedToUploadColdLaunchTimeToDCS(appInfo)) {
                    Slog.d(OppoStatsNotLaunchedAppUtils.TAG, "uploadColdLaunchTimeToDcs , shortComponentName = " + shortComponentName + " ,coldLaunchTime = " + coldLaunchTime);
                    int userId = ActivityManager.getCurrentUser();
                    if (userId >= 0) {
                        try {
                            PackageInfo packageInfo = OppoStatsNotLaunchedAppUtils.this.mPackageManager.getPackageInfo(appInfo.packageName, 0, userId);
                            if (packageInfo != null) {
                                boolean isCompiled = OppoStatsNotLaunchedAppUtils.this.isCompileCompleted(appInfo);
                                HashMap<String, String> result = new HashMap<>();
                                result.put(OppoStatsNotLaunchedAppUtils.PACKAGE_NAME, appInfo.packageName);
                                result.put(OppoStatsNotLaunchedAppUtils.VERSION_NAME, packageInfo.versionName);
                                result.put(OppoStatsNotLaunchedAppUtils.LONG_VERSION_CODE, String.valueOf(packageInfo.getLongVersionCode()));
                                result.put(OppoStatsNotLaunchedAppUtils.COLD_LAUNCH_TIME, String.valueOf(coldLaunchTime));
                                result.put(OppoStatsNotLaunchedAppUtils.COMPILED, String.valueOf(isCompiled));
                                result.put(OppoStatsNotLaunchedAppUtils.SHORT_COMPONENT_NAME, shortComponentName);
                                OppoStatistics.onCommon(OppoStatsNotLaunchedAppUtils.this.mContext, OppoStatsNotLaunchedAppUtils.DM_LOG_TAG, OppoStatsNotLaunchedAppUtils.COLD_LAUNCH_TIME_EVENT_ID, result, false);
                            } else if (OppoStatsNotLaunchedAppUtils.DEBUG_SWITCH) {
                                Slog.w(OppoStatsNotLaunchedAppUtils.TAG, "uploadColdLaunchTimeToDcs , packageInfo == null");
                            }
                        } catch (Exception e) {
                            Slog.e(OppoStatsNotLaunchedAppUtils.TAG, "uploadColdLaunchTimeToDcs , Exception: " + e.getMessage());
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public boolean isNeedToUploadColdLaunchTimeToDCS(ApplicationInfo appInfo) {
        if (!isDcsSwitchEnable()) {
            if (DEBUG_SWITCH) {
                Slog.d(TAG, "isNeedToUploadColdLaunchTimeToDCS , isDcsSwitchEnable is false");
            }
            return false;
        } else if (!isFirstLaunch(appInfo)) {
            return false;
        } else {
            if (this.mContext == null) {
                Slog.w(TAG, "isNeedToUploadColdLaunchTimeToDCS , mContext == null");
                return false;
            } else if (this.mPackageManager == null) {
                Slog.w(TAG, "isNeedToUploadColdLaunchTimeToDCS , mPackageManager == null");
                return false;
            } else if (appInfo != null) {
                return true;
            } else {
                Slog.w(TAG, "isNeedToUploadColdLaunchTimeToDCS , appInfo == null");
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isCompileCompleted(ApplicationInfo appInfo) {
        if (appInfo == null) {
            return false;
        }
        File outFile = new File("/data/oppo/profiles/compiled_apps.txt");
        try {
            if (outFile.exists()) {
                this.mCompiledApps = this.mFileOperationInternal.readDataFromFile(outFile);
                if (DEBUG_SWITCH) {
                    Slog.d(TAG, "deserialization , mCompiledApps " + this.mCompiledApps.toString());
                }
                if (!this.mCompiledApps.containsKey(appInfo.packageName) || appInfo.longVersionCode != this.mCompiledApps.get(appInfo.packageName).longValue()) {
                    return false;
                }
                return true;
            }
            if (DEBUG_SWITCH) {
                Slog.d(TAG, "isCompileCompleted , file is not exist");
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "isCompileCompleted Exception: " + e.getMessage());
            return false;
        }
    }

    public void notePackageInstalled(final ApplicationInfo applicationInfo) {
        this.mServicePool.execute(new Runnable() {
            /* class com.android.server.pm.OppoStatsNotLaunchedAppUtils.AnonymousClass3 */

            public void run() {
                if (!OppoStatsNotLaunchedAppUtils.this.isDcsSwitchEnable()) {
                    Slog.d(OppoStatsNotLaunchedAppUtils.TAG, "notePackageInstalled , isDcsSwitchEnable is false");
                    return;
                }
                ApplicationInfo applicationInfo = applicationInfo;
                if (applicationInfo != null && !applicationInfo.isSystemApp()) {
                    if (OppoStatsNotLaunchedAppUtils.DEBUG_SWITCH) {
                        Slog.d(OppoStatsNotLaunchedAppUtils.TAG, "notePackageInstalled , packageName = " + applicationInfo.packageName + " , longVersionCode = " + applicationInfo.longVersionCode);
                    }
                    OppoStatsNotLaunchedAppUtils.this.addNotLaunchedAppLock(applicationInfo);
                }
            }
        });
    }

    public void notePackageReplaced(final ApplicationInfo applicationInfo) {
        this.mServicePool.execute(new Runnable() {
            /* class com.android.server.pm.OppoStatsNotLaunchedAppUtils.AnonymousClass4 */

            public void run() {
                if (!OppoStatsNotLaunchedAppUtils.this.isDcsSwitchEnable()) {
                    Slog.d(OppoStatsNotLaunchedAppUtils.TAG, "notePackageReplaced , isDcsSwitchEnable is false, return");
                    return;
                }
                ApplicationInfo applicationInfo = applicationInfo;
                if (applicationInfo != null && !applicationInfo.isSystemApp()) {
                    if (OppoStatsNotLaunchedAppUtils.DEBUG_SWITCH) {
                        Slog.d(OppoStatsNotLaunchedAppUtils.TAG, "notePackageReplaced , packageName = " + applicationInfo.packageName + " , longVersionCode = " + applicationInfo.longVersionCode);
                    }
                    OppoStatsNotLaunchedAppUtils.this.addNotLaunchedAppLock(applicationInfo);
                }
            }
        });
    }

    public void notePackageRemove(final String pkgName) {
        this.mServicePool.execute(new Runnable() {
            /* class com.android.server.pm.OppoStatsNotLaunchedAppUtils.AnonymousClass5 */

            public void run() {
                if (!OppoStatsNotLaunchedAppUtils.this.isDcsSwitchEnable()) {
                    Slog.d(OppoStatsNotLaunchedAppUtils.TAG, "notePackageRemove , isDcsSwitchEnable is false");
                    return;
                }
                if (OppoStatsNotLaunchedAppUtils.DEBUG_SWITCH) {
                    Slog.d(OppoStatsNotLaunchedAppUtils.TAG, "notePackageRemove , packageName = " + pkgName);
                }
                OppoStatsNotLaunchedAppUtils.this.removeNotLaunchedAppLock(pkgName);
            }
        });
    }

    /* access modifiers changed from: private */
    public void addNotLaunchedAppLock(ApplicationInfo applicationInfo) {
        if ((!this.mNotLaunchedApps.containsKey(applicationInfo.packageName) || this.mNotLaunchedApps.get(applicationInfo.packageName).longValue() != applicationInfo.longVersionCode) && new File("/data/oppo/profiles/").exists()) {
            File notLaunchedAppFile = new File("/data/oppo/profiles/not_launched_apps.txt");
            this.mNotLaunchedApps.put(applicationInfo.packageName, Long.valueOf(applicationInfo.longVersionCode));
            this.mFileOperationInternal.writeDataToFile(this.mNotLaunchedApps, notLaunchedAppFile);
        }
    }

    /* access modifiers changed from: private */
    public void removeNotLaunchedAppLock(String pkgName) {
        if (pkgName != null && this.mNotLaunchedApps.containsKey(pkgName) && new File("/data/oppo/profiles/").exists()) {
            File notLaunchedAppFile = new File("/data/oppo/profiles/not_launched_apps.txt");
            this.mNotLaunchedApps.remove(pkgName);
            this.mFileOperationInternal.writeDataToFile(this.mNotLaunchedApps, notLaunchedAppFile);
        }
    }

    /* access modifiers changed from: private */
    public class FileOperatorInternal {
        private static final String COMPILED_APPS_FILENAME = "compiled_apps.txt";
        private static final String NOT_LAUNCHED_APPS_FILENAME = "not_launched_apps.txt";
        private static final String PROFILES_URI = "/data/oppo/profiles/";

        private FileOperatorInternal() {
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x003c, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x003d, code lost:
            $closeResource(r3, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0040, code lost:
            throw r4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0044, code lost:
            $closeResource(r2, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0047, code lost:
            throw r3;
         */
        public void writeDataToFile(HashMap<String, Long> refreshList, File outFile) {
            if (OppoStatsNotLaunchedAppUtils.DEBUG_SWITCH) {
                Slog.d(OppoStatsNotLaunchedAppUtils.TAG, "writeDataToFile, content = " + refreshList.toString());
            }
            try {
                FileOutputStream fos = new FileOutputStream(outFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(refreshList);
                oos.flush();
                $closeResource(null, oos);
                $closeResource(null, fos);
            } catch (Exception e) {
                Slog.e(OppoStatsNotLaunchedAppUtils.TAG, "writeDataToFile Exception : " + e.getMessage());
            }
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
            $closeResource(r3, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
            throw r4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0029, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x002a, code lost:
            $closeResource(r2, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x002d, code lost:
            throw r3;
         */
        public HashMap<String, Long> readDataFromFile(File file) {
            HashMap<String, Long> result = new HashMap<>();
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                HashMap<String, Long> result2 = (HashMap) ois.readObject();
                $closeResource(null, ois);
                $closeResource(null, fis);
                return result2;
            } catch (Exception e) {
                Slog.e(OppoStatsNotLaunchedAppUtils.TAG, "readDataFromFile Exception : " + e.getMessage());
                return result;
            }
        }
    }
}
