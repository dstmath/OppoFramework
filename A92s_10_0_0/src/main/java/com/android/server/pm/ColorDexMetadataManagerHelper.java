package com.android.server.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.dex.DexMetadataHelper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.display.ai.utils.BrightnessConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import oppo.util.OppoStatistics;

public class ColorDexMetadataManagerHelper {
    private static final String BACKGROUND_COMPILE_PACKAGE_SET_PATH = "/data/oppo/profiles/background_compile_package_set.xml";
    static final ArraySet<String> BACKGROUND_COMPILE_SET = new ArraySet<>();
    private static final String BACKGROUND_DEXOPT_PACKAGE_SET_PATH = "/data/oppo/profiles/background_dexopt_package_set.xml";
    static final ArraySet<String> BACKGROUND_DEXOPT_SET = new ArraySet<>();
    private static final String COMPILED_APP_FILE_FILENAME = "compiled_apps.txt";
    private static final String COMPILE_COST_TIME = "CostTime";
    private static final String DESCRIPTION = "Description";
    public static final String DEX_METADATA_FILE_EXTENSION = ".dm";
    public static final String DM_BACKGROUNG_COMPILE_EVENT_ID = "background_compile";
    public static final String DM_INSTALL_COMPILE_EVENT_ID = "install_compile";
    public static final String DM_LOG_TAG = "DexMetadata";
    public static final String DM_PREPROCESS_EVENT_ID = "dm_preprocess";
    public static final String DM_REUSE_EVENT_ID = "dm_reuse";
    public static final String HOT_METHOD_FILE_EXTENSION = ".txt";
    private static final String LONG_VERSION_CODE = "LongVersionCode";
    public static final String OPPO_PROFILE_DIR = "/data/oppo/profiles/";
    public static final String OPPO_PROFILE_DOWNLOAD_PATH = "/data/oppo/profiles/download/";
    public static final String OPPO_PROFILE_RESTORE_PATH = "/data/oppo/profiles/restore/";
    public static final String OPPO_PROFILE_SNAPSHOT_PATH = "/data/oppo/profiles/snapshot/";
    public static final String OPPO_PROFILE_UPDATE_PATH = "/data/oppo/profiles/update/";
    public static final String OPPO_PROFMAN_DIR = "/data/oppo/profman/";
    private static final String PACKAGE_NAME = "PackageName";
    private static final String PATH_SPLIT = "_";
    private static final String PROFILES_URI = "/data/oppo/profiles/";
    public static final int PROFILE_DEFAULT_TYPE = -1;
    public static final int PROFILE_DOWNLOAD_TYPE = 1;
    public static final String PROFILE_FILE_EXTENSION = ".prof";
    public static final int PROFILE_GOOGLE_TYPE = 0;
    public static final int PROFILE_RESTORE_TYPE = 3;
    public static final int PROFILE_UPDATE_TYPE = 2;
    private static final String PROPERTY_ROM_VERSION = "ro.build.version.opporom";
    private static final String RESULT = "Result";
    private static final String TAG = "ColorDexMetadataManagerHelper";
    public static final String USER_REFERENCE_PROFILE_NAME = "/primary.prof";
    public static final String USER_REFERENCE_PROFILE_PATH = "/data/misc/profiles/ref/";
    private static final String VERSION_ALPHA = "alpha";
    private static final String VERSION_BETA = "beta";
    private static final String VERSION_NAME = "VersionName";
    private static volatile ColorDexMetadataManagerHelper sDmManagerHelper = null;
    private BootReceiver mBootReceiver;
    private ColorDexMetadataCompileHelper mCompileHelper = ColorDexMetadataCompileHelper.getInstance();
    /* access modifiers changed from: private */
    public HashMap<String, Long> mCompiledApp = new HashMap<>();
    private ColorDexMetadataConfigHelper mConfigHelper = ColorDexMetadataConfigHelper.getInstance();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public boolean mDebugSwitch = ColorDexMetadataManager.sDebugDetail;
    private final Object mHelperLock = new Object();
    private boolean mIsForumVersion = false;
    private PackageRemoveReceiver mPackageRemoveReceiver;
    private ExecutorService mServicePool = Executors.newCachedThreadPool();

    public static ColorDexMetadataManagerHelper getInstance() {
        if (sDmManagerHelper == null) {
            synchronized (ColorDexMetadataManagerHelper.class) {
                if (sDmManagerHelper == null) {
                    sDmManagerHelper = new ColorDexMetadataManagerHelper();
                }
            }
        }
        return sDmManagerHelper;
    }

    private ColorDexMetadataManagerHelper() {
        initVersionInfo();
    }

    private void initVersionInfo() {
        boolean result = false;
        String ver = SystemProperties.get(PROPERTY_ROM_VERSION);
        if (!TextUtils.isEmpty(ver)) {
            String ver2 = ver.toLowerCase();
            if (ver2.endsWith(VERSION_ALPHA) || ver2.endsWith(VERSION_BETA)) {
                result = true;
            }
        }
        if (this.mDebugSwitch) {
            Slog.d(TAG, "ROM Version = " + result);
        }
        this.mIsForumVersion = result;
    }

    public void onSystemReady(Context context) {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "ManagerHelper: onSystemReady!");
        }
        this.mContext = context;
        this.mConfigHelper.onSystemReady(context);
        this.mCompileHelper.onSystemReady(context);
        this.mBootReceiver = new BootReceiver();
        this.mPackageRemoveReceiver = new PackageRemoveReceiver();
        this.mBootReceiver.register();
        this.mPackageRemoveReceiver.register();
        OppoStatsNotLaunchedAppUtils.getInstance().init(context);
    }

    public boolean isNeedToDumpProfile(PackageParser.Package pkg) {
        String packageName = pkg.packageName;
        if (ColorDexMetadataUtils.isScreenOn()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "screen on, skip reusing profile for " + packageName);
            }
            return false;
        } else if (!this.mConfigHelper.isMainSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "main switch is disable!");
            }
            return false;
        } else if (!this.mConfigHelper.isUseUpgradeProfileSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "use upgrade profile switch is disable!");
            }
            return false;
        } else if (isCloudDexMetadataExist(pkg)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "cloud dex metadata is exist, no need to reuse profile for " + packageName);
            }
            return false;
        } else if (this.mConfigHelper.isInBlackListForPackage(packageName)) {
            if (this.mDebugSwitch) {
                Slog.w(TAG, "in the optimized black list, skip reusing profile for " + packageName);
            }
            return false;
        } else if (this.mConfigHelper.isInWhiteListForPackage(packageName)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "in the optimized white list, need to reuse profile for " + packageName);
            }
            return true;
        } else {
            if (ColorDexMetadataUtils.isFrequentUsageApp(this.mContext, packageName, this.mConfigHelper.getAppUsageInRecentMonths(), this.mConfigHelper.getTopAppNumber())) {
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "is frequent usage app, need to reuse profile for " + packageName);
                }
                return true;
            }
            if (this.mDebugSwitch) {
                Slog.d(TAG, "is not frequent usage app, no need to reuse profile for " + packageName);
            }
            return false;
        }
    }

    public boolean isNeedToCreateProfile(PackageParser.Package pkg) {
        String packageName = pkg.packageName;
        File hotMethodFile = new File(OPPO_PROFMAN_DIR + packageName + HOT_METHOD_FILE_EXTENSION);
        if (!checkHotMethodFileExist(hotMethodFile)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "hot method file is not exist!");
            }
            return false;
        } else if (ColorDexMetadataUtils.isScreenOn()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "screen on, skip reusing profile for " + packageName);
            }
            deleteFile(hotMethodFile);
            return false;
        } else if (!this.mConfigHelper.isMainSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "main switch is disable!");
            }
            deleteFile(hotMethodFile);
            return false;
        } else if (!this.mConfigHelper.isUseUpgradeProfileSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "use upgrade profile switch is disable!");
            }
            deleteFile(hotMethodFile);
            return false;
        } else if (isGoogleDexMetadataExist(pkg)) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "google dex metadata found, skip reusing profile for " + packageName);
            }
            deleteFile(hotMethodFile);
            return false;
        } else if (!isCloudDexMetadataExist(pkg)) {
            return true;
        } else {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "cloud dex metadata found, skip reusing profile for " + packageName);
            }
            deleteFile(hotMethodFile);
            return false;
        }
    }

    private boolean checkHotMethodFileExist(File hotMethodFile) {
        if (hotMethodFile == null || !hotMethodFile.exists()) {
            return false;
        }
        if (hotMethodFile.length() > ((long) this.mConfigHelper.getValidFileSizeThreshold())) {
            return true;
        }
        if (this.mDebugSwitch) {
            Slog.d(TAG, "hot method file is invalid at: " + hotMethodFile.getAbsolutePath());
        }
        deleteFile(hotMethodFile);
        return false;
    }

    public int getDexMetadataType(PackageParser.Package pkg) {
        if (isGoogleDexMetadataExist(pkg)) {
            if (!this.mDebugSwitch) {
                return 0;
            }
            Slog.i(TAG, "google dex metadata founded for " + pkg.packageName);
            return 0;
        } else if (isCloudDexMetadataExist(pkg)) {
            if (!this.mDebugSwitch) {
                return 1;
            }
            Slog.d(TAG, "download dex metadata founded for " + pkg.packageName);
            return 1;
        } else if (!isUpgradeDexMetadataExist(pkg)) {
            return -1;
        } else {
            if (!this.mDebugSwitch) {
                return 2;
            }
            Slog.d(TAG, "update reuse dex metadata founded for " + pkg.packageName);
            return 2;
        }
    }

    private boolean isGoogleDexMetadataExist(PackageParser.Package pkg) {
        return DexMetadataHelper.findDexMetadataForFile(new File(pkg.baseCodePath)) != null;
    }

    private boolean isCloudDexMetadataExist(PackageParser.Package pkg) {
        if (findDexMetadataForPackage(pkg, 1) == null) {
            return false;
        }
        return true;
    }

    private boolean isUpgradeDexMetadataExist(PackageParser.Package pkg) {
        return findDexMetadataForPackage(pkg, 2) != null;
    }

    private boolean isRestoreDexMetadataExist(PackageParser.Package pkg) {
        return findDexMetadataForPackage(pkg, 3) != null;
    }

    public boolean isNeedToMergeProfile(int type) {
        if (type < 0) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "profile not found, invalid profile type: " + type);
            }
            return false;
        } else if (!this.mConfigHelper.isMainSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "main switch is disable!");
            }
            return false;
        } else if (type == 0) {
            return false;
        } else {
            if (type == 1) {
                return this.mConfigHelper.isUseCloudProfileSwitchEnable();
            }
            if (type == 2) {
                return this.mConfigHelper.isUseUpgradeProfileSwitchEnable();
            }
            if (type != 3) {
                return false;
            }
            return this.mConfigHelper.isUseRestoreProfileSwitchEnable();
        }
    }

    public File findDexMetadataForPackage(PackageParser.Package pkg, int type) {
        String packagePath = buildPackagePath(pkg);
        String dexMetadataPath = null;
        if (type == 1) {
            dexMetadataPath = OPPO_PROFILE_DOWNLOAD_PATH + packagePath + DEX_METADATA_FILE_EXTENSION;
        } else if (type == 2) {
            dexMetadataPath = OPPO_PROFILE_UPDATE_PATH + packagePath + PROFILE_FILE_EXTENSION;
        } else if (type == 3) {
            dexMetadataPath = OPPO_PROFILE_RESTORE_PATH + packagePath + PROFILE_FILE_EXTENSION;
        }
        if (dexMetadataPath == null) {
            return null;
        }
        File dexMetadataFile = new File(dexMetadataPath);
        if (dexMetadataFile.exists()) {
            return dexMetadataFile;
        }
        return null;
    }

    public String buildPackagePath(PackageParser.Package pkg) {
        return pkg.packageName + PATH_SPLIT + pkg.mVersionName + PATH_SPLIT + pkg.getLongVersionCode();
    }

    public void deleteFile(File file) {
        if (file == null || !file.exists()) {
            Slog.w(TAG, "delete file is not exist");
        } else if (!file.delete()) {
            Slog.w(TAG, "delete file failed at: " + file.getPath());
        }
    }

    public boolean deleteDownloadDexMetadataForPackage(final String packageName) {
        File[] files;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        File downloadDir = new File(OPPO_PROFILE_DOWNLOAD_PATH);
        if (!downloadDir.exists() || !downloadDir.isDirectory() || (files = downloadDir.listFiles(new FilenameFilter() {
            /* class com.android.server.pm.ColorDexMetadataManagerHelper.AnonymousClass1 */

            public boolean accept(File dir, String name) {
                return name.startsWith(packageName);
            }
        })) == null || files.length == 0) {
            return false;
        }
        for (File file : files) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "delete download dexmetadata at: " + file.getPath());
            }
            file.delete();
        }
        return true;
    }

    public boolean isFileValid(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.length() <= ((long) this.mConfigHelper.getValidFileSizeThreshold())) {
            return false;
        }
        return true;
    }

    public boolean isPrecompileEnable() {
        if (this.mConfigHelper.isPrecompileForAllPackageSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "all packages in precompile switch is enable!");
            }
            return true;
        } else if (this.mConfigHelper.isBackgroundCompileForAllPackageSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "all packages in background compile switch is enable!");
            }
            return false;
        } else if (ColorDexMetadataUtils.isScreenOn()) {
            return false;
        } else {
            return true;
        }
    }

    public void registerToCompile(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "Invalid package name: " + packageName);
        } else if (this.mConfigHelper.isBackgroundCompileForAllPackageSwitchEnable() || !this.mConfigHelper.isOppoBackgroundCompileSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "compile at background dexopt service for " + packageName);
            }
            addIntoBackgroundDexOptPackageSet(packageName);
        } else {
            if (checkNeedToDexOptAtBackgroundService() && this.mDebugSwitch) {
                Slog.d(TAG, "compile package set is too big, need to compile at background dexopt service");
            }
            addIntoCompilePackageSet(packageName);
            if (this.mDebugSwitch) {
                Slog.d(TAG, "compile at oppo background compile for " + packageName);
            }
        }
    }

    private void addIntoBackgroundDexOptPackageSet(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "Invalid package: " + packageName);
            return;
        }
        synchronized (BACKGROUND_DEXOPT_SET) {
            if (!BACKGROUND_DEXOPT_SET.contains(packageName)) {
                BACKGROUND_DEXOPT_SET.add(packageName);
            }
        }
    }

    private void addIntoCompilePackageSet(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "Invalid package: " + packageName);
            return;
        }
        synchronized (BACKGROUND_COMPILE_SET) {
            if (!BACKGROUND_COMPILE_SET.contains(packageName)) {
                BACKGROUND_COMPILE_SET.add(packageName);
            }
        }
    }

    private boolean checkNeedToDexOptAtBackgroundService() {
        if (BACKGROUND_COMPILE_SET.size() < this.mConfigHelper.getCompilePackageSetSize()) {
            return false;
        }
        synchronized (BACKGROUND_COMPILE_SET) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "background compile set size = " + BACKGROUND_COMPILE_SET.size());
            }
            Iterator<String> it = BACKGROUND_COMPILE_SET.iterator();
            while (it.hasNext()) {
                addIntoBackgroundDexOptPackageSet(it.next());
            }
            BACKGROUND_COMPILE_SET.clear();
        }
        return true;
    }

    public boolean isNeedToDexOptForce(String packageName) {
        ArraySet<String> arraySet = BACKGROUND_DEXOPT_SET;
        if (arraySet == null || arraySet.isEmpty() || !BACKGROUND_DEXOPT_SET.contains(packageName)) {
            return false;
        }
        synchronized (BACKGROUND_DEXOPT_SET) {
            BACKGROUND_DEXOPT_SET.remove(packageName);
        }
        return true;
    }

    public ArraySet<String> getCompilePackageSet() {
        ArraySet<String> pkgs = new ArraySet<>();
        synchronized (BACKGROUND_COMPILE_SET) {
            Iterator<String> it = BACKGROUND_COMPILE_SET.iterator();
            while (it.hasNext()) {
                String pkg = it.next();
                if (pkg != null) {
                    pkgs.add(pkg);
                }
            }
        }
        return pkgs;
    }

    public void removeFromCompilePackageSet(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "Invalid package name: " + packageName);
            return;
        }
        synchronized (BACKGROUND_COMPILE_SET) {
            if (BACKGROUND_COMPILE_SET.contains(packageName)) {
                BACKGROUND_COMPILE_SET.remove(packageName);
            }
        }
    }

    /* access modifiers changed from: private */
    public void readCompilePackageSetFromFile() {
        File file = new File(BACKGROUND_COMPILE_PACKAGE_SET_PATH);
        if (file.exists()) {
            synchronized (this.mHelperLock) {
                ArraySet<String> pkgs = ColorDexMetadataUtils.readFromXmlFile(file);
                if (pkgs != null && !pkgs.isEmpty()) {
                    Iterator<String> it = pkgs.iterator();
                    while (it.hasNext()) {
                        addIntoCompilePackageSet(it.next());
                    }
                }
                deleteFile(file);
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "read background compile package set done, delete it!");
                }
            }
        } else if (this.mDebugSwitch) {
            Slog.d(TAG, "the file that saves background compile package set not found!");
        }
    }

    /* access modifiers changed from: private */
    public void readDexOptPackageSetFromFile() {
        File file = new File(BACKGROUND_DEXOPT_PACKAGE_SET_PATH);
        if (file.exists()) {
            synchronized (this.mHelperLock) {
                ArraySet<String> pkgs = ColorDexMetadataUtils.readFromXmlFile(file);
                if (pkgs != null && !pkgs.isEmpty()) {
                    Iterator<String> it = pkgs.iterator();
                    while (it.hasNext()) {
                        addIntoBackgroundDexOptPackageSet(it.next());
                    }
                }
                deleteFile(file);
                if (this.mDebugSwitch) {
                    Slog.d(TAG, "read background dexopt package set done, delete it!");
                }
            }
        } else if (this.mDebugSwitch) {
            Slog.d(TAG, "the file that saves background dexopt package set not found!");
        }
    }

    /* access modifiers changed from: private */
    public void saveCompilePackageSetToFile() {
        if (!BACKGROUND_COMPILE_SET.isEmpty()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "need to save compile package set!");
            }
            File file = new File(BACKGROUND_COMPILE_PACKAGE_SET_PATH);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            synchronized (this.mHelperLock) {
                ColorDexMetadataUtils.saveAsXmlFile(file, BACKGROUND_COMPILE_SET);
            }
        }
    }

    /* access modifiers changed from: private */
    public void saveDexOptPackageSetToFile() {
        if (!BACKGROUND_DEXOPT_SET.isEmpty()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "need to save background dexopt package set!");
            }
            File file = new File(BACKGROUND_DEXOPT_PACKAGE_SET_PATH);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            synchronized (this.mHelperLock) {
                ColorDexMetadataUtils.saveAsXmlFile(file, BACKGROUND_DEXOPT_SET);
            }
        }
    }

    private class BootReceiver extends BroadcastReceiver {
        private BootReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (ColorDexMetadataManagerHelper.this.mDebugSwitch) {
                    Slog.d(ColorDexMetadataManagerHelper.TAG, "BootReceiver: action = " + action);
                }
                if (BrightnessConstants.ACTION_BOOT_COMPLETED.equals(action)) {
                    ColorDexMetadataManagerHelper.this.readCompilePackageSetFromFile();
                    ColorDexMetadataManagerHelper.this.readDexOptPackageSetFromFile();
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    ColorDexMetadataManagerHelper.this.saveCompilePackageSetToFile();
                    ColorDexMetadataManagerHelper.this.saveDexOptPackageSetToFile();
                }
            }
        }

        public void register() {
            if (ColorDexMetadataManagerHelper.this.mContext != null) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(BrightnessConstants.ACTION_BOOT_COMPLETED);
                intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
                ColorDexMetadataManagerHelper.this.mContext.registerReceiver(this, intentFilter);
            }
        }
    }

    private class PackageRemoveReceiver extends BroadcastReceiver {
        private PackageRemoveReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String packageName = intent.getData().getSchemeSpecificPart();
            if (action != null && packageName != null && "android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (ColorDexMetadataManagerHelper.this.mDebugSwitch) {
                    Slog.i(ColorDexMetadataManagerHelper.TAG, "PackageRemoveReceiver: action = " + action + ", pkg = " + packageName);
                }
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    if (ColorDexMetadataManagerHelper.BACKGROUND_COMPILE_SET.contains(packageName)) {
                        synchronized (ColorDexMetadataManagerHelper.BACKGROUND_COMPILE_SET) {
                            ColorDexMetadataManagerHelper.BACKGROUND_COMPILE_SET.remove(packageName);
                        }
                        if (ColorDexMetadataManagerHelper.this.mDebugSwitch) {
                            Slog.d(ColorDexMetadataManagerHelper.TAG, "package has removed, need to remove from background compile set: " + packageName);
                        }
                    }
                    if (ColorDexMetadataManagerHelper.BACKGROUND_DEXOPT_SET.contains(packageName)) {
                        synchronized (ColorDexMetadataManagerHelper.BACKGROUND_DEXOPT_SET) {
                            ColorDexMetadataManagerHelper.BACKGROUND_DEXOPT_SET.remove(packageName);
                        }
                        if (ColorDexMetadataManagerHelper.this.mDebugSwitch) {
                            Slog.d(ColorDexMetadataManagerHelper.TAG, "package has removed, need to remove from dexopt compile set: " + packageName);
                        }
                    }
                }
            }
        }

        public void register() {
            if (ColorDexMetadataManagerHelper.this.mContext != null) {
                IntentFilter packageIntentFilter = new IntentFilter();
                packageIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
                packageIntentFilter.addDataScheme(BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                ColorDexMetadataManagerHelper.this.mContext.registerReceiver(this, packageIntentFilter);
            }
        }
    }

    public void recordCompiledApp(final String pkgName) {
        if (pkgName != null && this.mContext != null) {
            this.mServicePool.execute(new Runnable() {
                /* class com.android.server.pm.ColorDexMetadataManagerHelper.AnonymousClass2 */

                /* JADX WARNING: Code restructure failed: missing block: B:14:0x0055, code lost:
                    r5 = move-exception;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:15:0x0056, code lost:
                    $closeResource(r2, r4);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:16:0x0059, code lost:
                    throw r5;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:20:0x005c, code lost:
                    r4 = move-exception;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:21:0x005d, code lost:
                    $closeResource(r2, r1);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
                    throw r4;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:45:0x0109, code lost:
                    r5 = move-exception;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:46:0x010a, code lost:
                    $closeResource(r2, r4);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:47:0x010d, code lost:
                    throw r5;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:51:0x0110, code lost:
                    r4 = move-exception;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:52:0x0111, code lost:
                    $closeResource(r2, r1);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:53:0x0114, code lost:
                    throw r4;
                 */
                public void run() {
                    File outFile = new File("/data/oppo/profiles/compiled_apps.txt");
                    if (outFile.exists()) {
                        try {
                            FileInputStream fis = new FileInputStream(outFile);
                            ObjectInputStream ois = new ObjectInputStream(fis);
                            HashMap unused = ColorDexMetadataManagerHelper.this.mCompiledApp = (HashMap) ois.readObject();
                            if (ColorDexMetadataManagerHelper.this.mDebugSwitch) {
                                Slog.d(ColorDexMetadataManagerHelper.TAG, "deserialization , mCompiledApp " + ColorDexMetadataManagerHelper.this.mCompiledApp.toString());
                            }
                            $closeResource(null, ois);
                            $closeResource(null, fis);
                        } catch (Exception e) {
                            Slog.e(ColorDexMetadataManagerHelper.TAG, "recordCompiledApp Exception : " + e.getMessage());
                            return;
                        }
                    } else {
                        ColorDexMetadataManagerHelper.this.mCompiledApp.clear();
                        if (!new File("/data/oppo/profiles/").exists()) {
                            return;
                        }
                    }
                    try {
                        FileOutputStream fos = new FileOutputStream(outFile);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        PackageInfo packageInfo = ColorDexMetadataManagerHelper.this.mContext.getPackageManager().getPackageInfo(pkgName, 0);
                        if (packageInfo == null) {
                            $closeResource(null, oos);
                            $closeResource(null, fos);
                            return;
                        }
                        ColorDexMetadataManagerHelper.this.mCompiledApp.put(pkgName, Long.valueOf(packageInfo.getLongVersionCode()));
                        if (ColorDexMetadataManagerHelper.this.mDebugSwitch) {
                            Slog.d(ColorDexMetadataManagerHelper.TAG, "pkgName = " + pkgName + " , packageInfo.getLongVersionCode() : " + packageInfo.getLongVersionCode());
                        }
                        oos.writeObject(ColorDexMetadataManagerHelper.this.mCompiledApp);
                        oos.flush();
                        $closeResource(null, oos);
                        $closeResource(null, fos);
                    } catch (Exception e2) {
                        Slog.e(ColorDexMetadataManagerHelper.TAG, "recordCompiledApp Exception 2 : " + e2.getMessage());
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
            });
        }
    }

    public void removeCompiledApp(final String pkgName) {
        this.mServicePool.execute(new Runnable() {
            /* class com.android.server.pm.ColorDexMetadataManagerHelper.AnonymousClass3 */

            /* JADX WARNING: Code restructure failed: missing block: B:23:0x0085, code lost:
                r8 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:24:0x0086, code lost:
                $closeResource(r7, r6);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:25:0x0089, code lost:
                throw r8;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:29:0x008c, code lost:
                r7 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:30:0x008d, code lost:
                $closeResource(r6, r4);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:31:0x0090, code lost:
                throw r7;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d9, code lost:
                r5 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:42:0x00da, code lost:
                $closeResource(r4, r3);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:43:0x00dd, code lost:
                throw r5;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:47:0x00e0, code lost:
                r4 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:48:0x00e1, code lost:
                $closeResource(r3, r2);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:49:0x00e4, code lost:
                throw r4;
             */
            public void run() {
                File outFile = new File("/data/oppo/profiles/compiled_apps.txt");
                if (outFile.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(outFile);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        HashMap unused = ColorDexMetadataManagerHelper.this.mCompiledApp = (HashMap) ois.readObject();
                        if (ColorDexMetadataManagerHelper.this.mDebugSwitch) {
                            Slog.d(ColorDexMetadataManagerHelper.TAG, "deserialization , mCompiledApp " + ColorDexMetadataManagerHelper.this.mCompiledApp.toString());
                        }
                        if (ColorDexMetadataManagerHelper.this.mCompiledApp.containsKey(pkgName)) {
                            ColorDexMetadataManagerHelper.this.mCompiledApp.remove(pkgName);
                            try {
                                FileOutputStream fos = new FileOutputStream(outFile);
                                ObjectOutputStream oos = new ObjectOutputStream(fos);
                                oos.writeObject(ColorDexMetadataManagerHelper.this.mCompiledApp);
                                oos.flush();
                                $closeResource(null, oos);
                                $closeResource(null, fos);
                            } catch (Exception e) {
                                Slog.e(ColorDexMetadataManagerHelper.TAG, "removeCompiledApp Serialization Exception : " + e.getMessage());
                            }
                        }
                        if (ColorDexMetadataManagerHelper.this.mDebugSwitch) {
                            Slog.d(ColorDexMetadataManagerHelper.TAG, "deserialization , mCompiledApp after removing: " + ColorDexMetadataManagerHelper.this.mCompiledApp.toString());
                        }
                        $closeResource(null, ois);
                        $closeResource(null, fis);
                    } catch (Exception e2) {
                        Slog.e(ColorDexMetadataManagerHelper.TAG, "removeCompiledApp deserialization Exception : " + e2.getMessage());
                    }
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
        });
    }

    public void uploadToDcs(boolean result, String description, PackageParser.Package pkg, String eventId) {
        if (!switchOfUploadDcsForDexMetadata()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "switchOfUploadDcsForDexMetadata is disable, uploadToDcs return");
            }
        } else if (this.mContext != null && pkg != null) {
            HashMap<String, String> eventMap = new HashMap<>();
            eventMap.put(PACKAGE_NAME, pkg.packageName);
            eventMap.put(VERSION_NAME, pkg.mVersionName);
            eventMap.put(LONG_VERSION_CODE, String.valueOf(pkg.getLongVersionCode()));
            eventMap.put(RESULT, result ? "success" : "failed");
            eventMap.put(DESCRIPTION, description);
            OppoStatistics.onCommon(this.mContext, DM_LOG_TAG, eventId, eventMap, false);
        }
    }

    public void uploadToDcs(String description, PackageInfo packageInfo) {
        if (!switchOfUploadDcsForDexMetadata()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "switchOfUploadDcsForDexMetadata is disable, uploadToDcs return");
            }
        } else if (this.mContext != null && packageInfo != null) {
            HashMap<String, String> eventMap = new HashMap<>();
            eventMap.put(PACKAGE_NAME, packageInfo.packageName);
            eventMap.put(VERSION_NAME, packageInfo.versionName);
            eventMap.put(LONG_VERSION_CODE, String.valueOf(packageInfo.getLongVersionCode()));
            eventMap.put(COMPILE_COST_TIME, description);
            OppoStatistics.onCommon(this.mContext, DM_LOG_TAG, DM_BACKGROUNG_COMPILE_EVENT_ID, eventMap, false);
        }
    }

    public boolean switchOfUploadDcsForDexMetadata() {
        if (this.mContext == null || !this.mIsForumVersion) {
            return false;
        }
        if (!this.mConfigHelper.isMainSwitchEnable()) {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "main switch is disable!");
            }
            return false;
        } else if (this.mConfigHelper.isDcsSwitchEnable()) {
            return true;
        } else {
            if (this.mDebugSwitch) {
                Slog.d(TAG, "dcs switch is disable!");
            }
            return false;
        }
    }
}
