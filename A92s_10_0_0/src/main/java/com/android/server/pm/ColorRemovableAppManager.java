package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.os.BackgroundThread;
import com.color.content.ColorRemovableAppInfo;
import com.color.util.ColorTypeCastingHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.io.IoUtils;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorRemovableAppManager implements IColorRemovableAppManager {
    private static final String ACTION_EVENTID_RESTORE_REMOVABLEAPP = "PMS_restore_removableapp";
    private static final String APP_CODE = "20120";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_PERFORMANCE = true;
    private static final boolean DEBUG_SCAN = true;
    private static final boolean DEBUG_VERSION_CODE_EQUAL = SystemProperties.getBoolean(PROPERTY_NAME_DEBUG_VERSIONCODE, (boolean) DEBUG_VERSION_CODE_EQUAL);
    private static final String DIR_DATA_ENGINEERMODE = "/data/engineermode";
    private static final String ENV_OPPO_COMMON_PRELOAD_ROOT = "OPPO_COMMON_PRELOAD_ROOT";
    private static final int MSG_WHAT = 20190820;
    private static final List<String> PREINSTALLED_PATH_LIST = new ArrayList();
    private static final String PROPERTY_NAME_ASSERT_PANIC = "persist.sys.assert.panic";
    private static final String PROPERTY_NAME_DEBUG_SCAN = "persist.debug.removableapp.scan";
    private static final String PROPERTY_NAME_DEBUG_VERSIONCODE = "persist.debug.versioncode.equal";
    private static final boolean SWITCH_UPDATE_ENGINEER_MODE = true;
    private static final String TAG = "ColorRemovableAppManager";
    public static final String VERSION = "1.2.21";
    private static Context mContext = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean("persist.sys.assert.panic", (boolean) DEBUG_VERSION_CODE_EQUAL);
    private static ColorRemovableAppManager sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private boolean isBootFromOTA = DEBUG_VERSION_CODE_EQUAL;
    private boolean isScanFinished = DEBUG_VERSION_CODE_EQUAL;
    private boolean isShouldScan = DEBUG_VERSION_CODE_EQUAL;
    private List<String> mAppInfoKeyList = new ArrayList();
    /* access modifiers changed from: private */
    public Map<String, ColorRemovableAppInfo> mAppInfoMap = new HashMap();
    private List<ColorRemovableAppInfo> mAppInfoValueList = new ArrayList();
    boolean mDynamicDebug = DEBUG_VERSION_CODE_EQUAL;
    private Handler mHandler = new MyHandler();
    private List<String> mNewAppsList = new ArrayList();
    private PackageManagerService mPms = null;
    private IColorPackageManagerServiceEx mPmsEx = null;
    private IColorPackageManagerServiceInner mPmsInner = null;
    /* access modifiers changed from: private */
    public RemovableAppSettings mSettings = new RemovableAppSettings();
    private ArraySet<String> mWhiteList = new ArraySet<>();

    static {
        PREINSTALLED_PATH_LIST.add("/system/preload");
    }

    public static ColorRemovableAppManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorRemovableAppManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorRemovableAppManager();
                }
            }
        }
        return sInstance;
    }

    private ColorRemovableAppManager() {
        Slog.d(TAG, "Constructor");
        Slog.d(TAG, "Version[1.2.21]");
    }

    public void init(IColorPackageManagerServiceEx ex) {
        Slog.d(TAG, "init");
        this.mPmsEx = ex;
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = this.mPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            this.mPms = iColorPackageManagerServiceEx.getPackageManagerService();
            this.mPmsInner = this.mPmsEx.getColorPackageManagerServiceInner();
            this.isBootFromOTA = isBootFromOTA();
        }
        mContext = this.mPms.mContext;
        this.mAppInfoMap = this.mSettings.loadFromXml();
        registerLogModule();
    }

    public void systemReady(Context context) {
        Slog.d(TAG, "systemReady");
        int count = 100;
        while (this.isShouldScan && !this.isScanFinished && count > 0) {
            count--;
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.isShouldScan && !this.isScanFinished && count <= 0) {
            Slog.e(TAG, "systemReady, there is something wrong with scanPreinstalledApp thread.");
        }
    }

    public boolean install(String packageName) {
        Slog.e(TAG, "not support install");
        return DEBUG_VERSION_CODE_EQUAL;
    }

    public boolean uninstall(String packageName) {
        Slog.e(TAG, "not support uninstall");
        return DEBUG_VERSION_CODE_EQUAL;
    }

    public boolean isRemovable(String packageName) {
        if (this.mAppInfoMap.get(packageName) != null) {
            return true;
        }
        return DEBUG_VERSION_CODE_EQUAL;
    }

    public long getVersionCode(String pkgName) {
        ColorRemovableAppInfo appInfo = this.mAppInfoMap.get(pkgName);
        if (appInfo != null) {
            return appInfo.getVersionCode();
        }
        return -1;
    }

    public String getCodePath(String pkgName) {
        ColorRemovableAppInfo appInfo = this.mAppInfoMap.get(pkgName);
        if (appInfo != null) {
            return appInfo.getCodePath();
        }
        return null;
    }

    public List<String> getRemovableAppList() {
        return this.mAppInfoKeyList;
    }

    public List<ColorRemovableAppInfo> getRemovedAppInfos() {
        Slog.e(TAG, "not support getRemovedAppInfos");
        return null;
    }

    public List<ColorRemovableAppInfo> getRemovableAppInfos() {
        return this.mAppInfoValueList;
    }

    public ColorRemovableAppInfo getRemovableAppInfo(String pkgName) {
        return this.mAppInfoMap.get(pkgName);
    }

    public void updateConfigurations() {
        if (this.isBootFromOTA) {
            Slog.d(TAG, "updateConfigurations()");
            String path = System.getenv(ENV_OPPO_COMMON_PRELOAD_ROOT);
            if (!TextUtils.isEmpty(path)) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File engineermodeDir = new File(dir, "engineermode");
                    if (engineermodeDir.exists() && engineermodeDir.isDirectory()) {
                        FileHelper.copyFileRecursive(engineermodeDir.getAbsolutePath(), DIR_DATA_ENGINEERMODE);
                    } else if (sDebugfDetail) {
                        Slog.w(TAG, "directory " + engineermodeDir.getAbsolutePath() + " not exists.");
                    }
                } else if (sDebugfDetail) {
                    Slog.w(TAG, "directory OPPO_COMMON_PRELOAD_ROOT not exists.");
                }
            } else if (sDebugfDetail) {
                Slog.w(TAG, "env OPPO_COMMON_PRELOAD_ROOT not exists.");
            }
        }
    }

    public void setDataPackageNameList(ArraySet<String> arraySet) {
        if (arraySet != null) {
            Slog.d(TAG, "setDataPackageNameList()");
            this.mWhiteList = arraySet;
        }
    }

    public void scanPreinstalledApps() {
        boolean isDebugMode = SystemProperties.getBoolean(PROPERTY_NAME_DEBUG_SCAN, (boolean) DEBUG_VERSION_CODE_EQUAL);
        Slog.d(TAG, "scanPreinstalledApps, isFirstBoot = " + this.mPms.isFirstBoot() + ", isBootFromOTA = " + this.isBootFromOTA);
        if (this.isBootFromOTA || isDebugMode) {
            scanPreinstalledAppsInner();
        } else {
            new Thread(new Runnable() {
                /* class com.android.server.pm.ColorRemovableAppManager.AnonymousClass1 */

                public void run() {
                    ColorRemovableAppManager.this.scanPreinstalledAppsInner();
                }
            }).start();
        }
    }

    public void changePackageInstalledState(PackageSetting ps, String packageName, boolean install) {
        if (isRemovable(packageName)) {
            if (install) {
                UserUninstallRecorder.getInstance().addManualOperatedPackage(packageName, install);
                return;
            }
            boolean isAllUninstalled = true;
            boolean isSystemApp = ps != null && (ps.isSystem() || ps.isUpdatedSystem());
            Slog.d(TAG, "isSystemApp = " + isSystemApp);
            if (!isSystemApp) {
                PackageSetting tPs = this.mPms.mSettings.getPackageLPr(packageName);
                int[] installedUsers = null;
                if (tPs != null) {
                    PackageManagerService packageManagerService = this.mPms;
                    int[] allUsers = PackageManagerService.sUserManager.getUserIds();
                    installedUsers = tPs.queryInstalledUsers(allUsers, true);
                    if (allUsers != null && allUsers.length > 0) {
                        for (int i = 0; i < allUsers.length; i++) {
                            Slog.d(TAG, "package " + packageName + " in user " + allUsers[i] + " is installed");
                        }
                    }
                } else {
                    Slog.w(TAG, "tPs is null");
                }
                if (!(tPs == null || installedUsers == null || installedUsers.length == 0)) {
                    isAllUninstalled = false;
                }
                Slog.d(TAG, "uninstalled by all users = " + isAllUninstalled);
                if (isAllUninstalled) {
                    UserUninstallRecorder.getInstance().addManualOperatedPackage(packageName, DEBUG_VERSION_CODE_EQUAL);
                }
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        List<String> list;
        pw.println("ColorRemovableAppManager:");
        pw.println("  Version 1.2.21");
        pw.println();
        pw.println("  isBootFromOTA[" + this.isBootFromOTA + "]");
        pw.println();
        ArraySet<String> arraySet = this.mWhiteList;
        if (arraySet != null && !arraySet.isEmpty()) {
            pw.println("  Data Package Name List:");
            Iterator<String> it = this.mWhiteList.iterator();
            while (it.hasNext()) {
                pw.println("    " + it.next());
            }
            pw.println();
        }
        pw.println("  size = " + this.mAppInfoValueList.size());
        for (ColorRemovableAppInfo info : this.mAppInfoValueList) {
            if (info != null) {
                pw.println("    name = " + info.getPackageName() + ", codePath = " + info.getCodePath() + ", baseCodePath = " + info.getBaseCodePath() + ", versionCode = " + info.getVersionCode() + ", versionName = " + info.getVersionName() + ", fileSize = " + info.getFileSize());
            }
        }
        pw.println();
        if (UserUninstallRecorder.getInstance() != null && (list = UserUninstallRecorder.getInstance().getUninstalledList()) != null && list.size() > 0) {
            pw.println("  Uninstalled By User:");
            Iterator<String> it2 = list.iterator();
            while (it2.hasNext()) {
                pw.println("    " + it2.next());
            }
            pw.println();
        }
    }

    public static boolean isBootFromOTA() {
        File file = new File("/cache/recovery/intent");
        boolean result = DEBUG_VERSION_CODE_EQUAL;
        String resultStr = "";
        if (file.exists() && file.canRead()) {
            Slog.i(TAG, "/cache/recovery/intent file is exist!!!");
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                resultStr = reader2.readLine();
                result = ("0".equals(resultStr) || "2".equals(resultStr)) ? true : DEBUG_VERSION_CODE_EQUAL;
                try {
                    reader2.close();
                } catch (IOException e1) {
                    Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e1);
                }
            } catch (IOException e) {
                Slog.e(TAG, "readOTAUpdateResult failed!!!", e);
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e12);
                    }
                }
                throw th;
            }
        } else if (sDebugfDetail) {
            Slog.i(TAG, "OTA file path is no exist,normal boot");
        }
        Slog.d(TAG, "isBootFromOTA::resultStr = " + resultStr + ", result = " + result);
        return result;
    }

    /* access modifiers changed from: private */
    public void scanPreinstalledAppsInner() {
        Slog.i(TAG, "scanPreinstalledAppsInner()");
        this.isShouldScan = true;
        long startTime = System.currentTimeMillis();
        boolean isDebugMode = SystemProperties.getBoolean(PROPERTY_NAME_DEBUG_SCAN, (boolean) DEBUG_VERSION_CODE_EQUAL);
        if (this.mPms.isFirstBoot() || this.isBootFromOTA || isDebugMode) {
            Slog.i(TAG, "scanPreinstalledAppsInner, first boot or boot from ota");
            List<String> preinstalledAppList = new ArrayList<>();
            for (String path : PREINSTALLED_PATH_LIST) {
                Slog.i(TAG, "scanPreinstalledAppsInner, scan " + path + " for preinstalled app");
                List<String> apkPaths = new ArrayList<>();
                findApksRecursive(path, apkPaths);
                Slog.i(TAG, "scanPreinstalledAppsInner, find " + apkPaths.size() + " apks in path " + path);
                for (String codePath : apkPaths) {
                    scanPackage(codePath, preinstalledAppList, this.mWhiteList);
                }
            }
            Iterator<String> it = this.mNewAppsList.iterator();
            while (it.hasNext()) {
                Slog.d(TAG, "scanPreinstalledAppsInner, add package " + it.next());
            }
            Iterator<Map.Entry<String, ColorRemovableAppInfo>> preinstallIterator = this.mAppInfoMap.entrySet().iterator();
            while (preinstallIterator.hasNext()) {
                Map.Entry<String, ColorRemovableAppInfo> entry = preinstallIterator.next();
                if (!preinstalledAppList.contains(entry.getKey())) {
                    Slog.w(TAG, "scanPreinstalledAppsInner, package " + entry.getKey() + " has remove from preinstalled app");
                    preinstallIterator.remove();
                }
            }
        }
        Iterator<Map.Entry<String, ColorRemovableAppInfo>> iterator = this.mAppInfoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ColorRemovableAppInfo> entry2 = iterator.next();
            if (!this.mWhiteList.contains(entry2.getKey())) {
                iterator.remove();
                Slog.d(TAG, "scanPreinstalledAppsInner, package " + entry2.getKey() + " isn't preinstall in this platform");
            } else {
                this.mAppInfoKeyList.add(entry2.getKey());
                this.mAppInfoValueList.add(entry2.getValue());
            }
        }
        persistence();
        this.isScanFinished = true;
        long endTime = System.currentTimeMillis();
        Slog.d(TAG, "scan preload app cost " + (endTime - startTime) + "ms");
    }

    private void scanPackage(String codePath, List<String> preinstalledAppList, ArraySet<String> whiteList) {
        Slog.d(TAG, "parse package  " + codePath);
        PackageParser.Package pkg = null;
        File apkFile = new File(codePath);
        try {
            pkg = new PackageParser().parsePackage(apkFile, 0);
        } catch (PackageParser.PackageParserException e) {
            Slog.w(TAG, "failed to parse " + codePath, e);
        }
        if (pkg != null) {
            String pkgName = pkg.packageName;
            long versionCode = pkg.getLongVersionCode();
            String versionName = pkg.mVersionName;
            if (TextUtils.isEmpty(pkgName)) {
                Slog.e(TAG, "package " + codePath + " name is empty");
            } else if (!whiteList.contains(pkgName)) {
                Slog.w(TAG, "package " + pkgName + " not in white list, ignore it.");
            } else {
                Slog.d(TAG, "package " + codePath + " name is " + pkgName);
                preinstalledAppList.add(pkgName);
                PackageSetting ps = getPackageSetting(pkgName);
                if (!this.mAppInfoMap.containsKey(pkgName)) {
                    Slog.d(TAG, "package " + codePath + " name " + pkgName + " is new apk");
                    this.mNewAppsList.add(pkgName);
                    if (isBootFromRecoveryReset() || this.isBootFromOTA) {
                        try {
                            PackageParser.collectCertificates(pkg, (boolean) DEBUG_VERSION_CODE_EQUAL);
                        } catch (PackageParser.PackageParserException e2) {
                            Slog.w(TAG, "failed to parse " + codePath, e2);
                        }
                        boolean isNewPackage = ps == null ? true : DEBUG_VERSION_CODE_EQUAL;
                        boolean isVersionCodeMatch = (isNewPackage || (versionCode <= ps.versionCode && (!DEBUG_VERSION_CODE_EQUAL || versionCode != ps.versionCode))) ? DEBUG_VERSION_CODE_EQUAL : true;
                        boolean isSignatureMatch = (!isVersionCodeMatch || !isSignatureMatch(pkg, ps)) ? DEBUG_VERSION_CODE_EQUAL : true;
                        Slog.d(TAG, "isNewPackage = " + isNewPackage + ", isVersionCodeMatch = " + isVersionCodeMatch + ", isSignatureMatch = " + isSignatureMatch);
                        if (isNewPackage) {
                            Slog.d(TAG, "copy new package " + pkgName + " from " + codePath);
                            copyPackageToData(true, codePath, null);
                        } else if (isVersionCodeMatch && isSignatureMatch) {
                            Slog.d(TAG, "update package " + pkgName + " installed by user, copy  from " + codePath + " to " + ps.codePathString);
                            synchronized (this.mPms.mInstallLock) {
                                this.mPms.removeCodePathLI(ps.codePath);
                            }
                            copyPackageToData(DEBUG_VERSION_CODE_EQUAL, codePath, ps.codePathString);
                        }
                    }
                    ColorRemovableAppInfo appInfo = new ColorRemovableAppInfo(pkgName);
                    appInfo.setVersionCode(versionCode);
                    appInfo.setVersionName(versionName);
                    appInfo.setCodePath(apkFile.getParent());
                    appInfo.setBaseCodePath(codePath);
                    appInfo.setFileSize(apkFile.length());
                    this.mAppInfoMap.put(pkgName, appInfo);
                    return;
                }
                ColorRemovableAppInfo appInfo2 = this.mAppInfoMap.get(pkgName);
                if (appInfo2 == null) {
                    Slog.e(TAG, "package " + pkgName + "appInfo is null, check it please");
                    return;
                }
                if (isBootFromRecoveryReset() || this.isBootFromOTA) {
                    try {
                        PackageParser.collectCertificates(pkg, (boolean) DEBUG_VERSION_CODE_EQUAL);
                    } catch (PackageParser.PackageParserException e3) {
                        Slog.w(TAG, "failed to parse " + codePath, e3);
                    }
                    boolean isNewPackage2 = ps == null ? true : DEBUG_VERSION_CODE_EQUAL;
                    boolean isVersionCodeMatch2 = (isNewPackage2 || (versionCode <= ps.versionCode && (!DEBUG_VERSION_CODE_EQUAL || versionCode != ps.versionCode))) ? DEBUG_VERSION_CODE_EQUAL : true;
                    boolean isSignatureMatch2 = (!isVersionCodeMatch2 || !isSignatureMatch(pkg, ps)) ? DEBUG_VERSION_CODE_EQUAL : true;
                    Slog.d(TAG, "isNewPackage = " + isNewPackage2 + ", isVersionCodeMatch = " + isVersionCodeMatch2 + ", isSignatureMatch = " + isSignatureMatch2);
                    if (UserUninstallRecorder.getInstance().isPkgUninstalledByUser(pkgName)) {
                        Slog.d(TAG, "package " + pkgName + " uninstalled by user, don't update it.");
                    } else if (isNewPackage2) {
                        Slog.d(TAG, "copy new package " + pkgName + " from " + codePath);
                        copyPackageToData(true, codePath, null);
                    } else if (isVersionCodeMatch2 && isSignatureMatch2) {
                        Slog.d(TAG, "update package " + pkgName + " installed by user, copy  from " + codePath + " to " + ps.codePathString);
                        synchronized (this.mPms.mInstallLock) {
                            this.mPms.removeCodePathLI(ps.codePath);
                        }
                        copyPackageToData(DEBUG_VERSION_CODE_EQUAL, codePath, ps.codePathString);
                    }
                }
                appInfo2.setVersionCode(versionCode);
                appInfo2.setVersionName(versionName);
                appInfo2.setCodePath(apkFile.getParent());
                appInfo2.setBaseCodePath(codePath);
                appInfo2.setFileSize(apkFile.length());
            }
        }
    }

    public void uploadRestoreRemovableAppInfo(Context context, String installerPackageName, String packageName, long versionCode, String versionName, int returnCode, String returnMsg) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("installerPackageName", TextUtils.isEmpty(installerPackageName) ? "unknown installerPackageName" : installerPackageName);
            map.put("packageName", TextUtils.isEmpty(packageName) ? "unknown packageName" : packageName);
            map.put("versionCode", "" + versionCode);
            map.put("versionName", TextUtils.isEmpty(versionName) ? "unknown versionName" : versionName);
            map.put("returnCode", returnCode + "");
            map.put("returnMsg", TextUtils.isEmpty(returnMsg) ? "unknown returnMsg" : returnMsg);
            OppoStatistics.onCommon(context, APP_CODE, ACTION_EVENTID_RESTORE_REMOVABLEAPP, map, (boolean) DEBUG_VERSION_CODE_EQUAL);
            if (PackageManagerService.DEBUG_INSTALL) {
                Slog.d(TAG, "uploadRestoreRemovableAppInfo:" + map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findApksRecursive(String path, List<String> list) {
        File[] childFiles;
        if (TextUtils.isEmpty(path)) {
            Slog.e(TAG, "path is empty, stop findApksRecursive");
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            Slog.e(TAG, "path " + path + " not exists.");
        } else if (!file.canRead()) {
            Slog.e(TAG, "path " + path + " can't read.");
        } else if (file.isFile()) {
            if (file.getPath().endsWith(".apk")) {
                Slog.d(TAG, "find preinstalled package " + file.getPath());
                list.add(path);
            }
        } else if (file.isDirectory() && (childFiles = file.listFiles()) != null) {
            for (File child : childFiles) {
                findApksRecursive(child.getPath(), list);
            }
        }
    }

    private boolean isSignatureMatch(PackageParser.Package pkg, PackageSetting ps) {
        boolean signatureMatch = DEBUG_VERSION_CODE_EQUAL;
        if (pkg == null || pkg.mSigningDetails == null) {
            if (this.DEBUG_SWITCH) {
                StringBuilder sb = new StringBuilder();
                sb.append("there's something wrong with apk ");
                sb.append(pkg != null ? pkg.packageName : "null");
                sb.append(" signatures.");
                Slog.d(TAG, sb.toString());
            }
        } else if (ps != null && ps.signatures != null && ps.signatures.mSigningDetails != null && ps.signatures.mSigningDetails.signatures != null && ps.signatures.mSigningDetails.signatures.length > 0 && ps.signatures.mSigningDetails.signatures[0] != null) {
            signatureMatch = pkg.mSigningDetails.hasCertificate(ps.signatures.mSigningDetails.signatures[0]);
            if (!signatureMatch) {
                Slog.e(TAG, "Package " + pkg.packageName + " signature verify failed.");
            }
        } else if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "there's something wrong with package " + pkg.packageName + " signatures.");
        }
        String pkgName = null;
        if (!TextUtils.isEmpty(pkg.packageName)) {
            pkgName = pkg.packageName;
        } else if (!TextUtils.isEmpty(ps.name)) {
            pkgName = ps.name;
        }
        if (!TextUtils.isEmpty(pkgName)) {
            Slog.e(TAG, "package " + pkgName + " signature match to PackageSetting : " + signatureMatch);
        }
        return signatureMatch;
    }

    private boolean copyPackageToData(boolean newApp, String src, String dst) {
        if (newApp) {
            String apkFileName = new File(src).getName().replace(".apk", "");
            FileHelper.copyPackageToDir(src, Environment.getDataAppDirectory(null) + File.separator + apkFileName);
            return DEBUG_VERSION_CODE_EQUAL;
        }
        FileHelper.copyPackageToDir(src, dst);
        return DEBUG_VERSION_CODE_EQUAL;
    }

    private boolean isBootFromRecoveryReset() {
        return DEBUG_VERSION_CODE_EQUAL;
    }

    private PackageSetting getPackageSetting(String pkgName) {
        PackageSetting packageLPr;
        if (this.mPms.mSettings == null) {
            return null;
        }
        synchronized (this.mPms.mPackages) {
            packageLPr = this.mPms.mSettings.getPackageLPr(pkgName);
        }
        return packageLPr;
    }

    /* access modifiers changed from: package-private */
    public class RemovableAppSettings {
        private static final String ATTR_PACKAGE_BASE_CODE_PATH = "baseCodePath";
        private static final String ATTR_PACKAGE_CODE_PATH = "codePath";
        private static final String ATTR_PACKAGE_FILE_SIZE = "fileSize";
        private static final String ATTR_PACKAGE_NAME = "packageName";
        private static final String ATTR_PACKAGE_UNINSTALLED = "uninstalled";
        private static final String ATTR_PACKAGE_VERSION_CODE = "versionCode";
        private static final String ATTR_PACKAGE_VERSION_NAME = "versionName";
        private static final boolean DEBUG_PARSE = true;
        private static final String REMOVABLE_APP_STATE_FILE = "/data/oppo/coloros/removableapp/removable_app_state.xml";
        private static final String REMOVABLE_ROOT_PATH = "/data/oppo/coloros/removableapp";
        private static final String TAG_PACKAGE = "package";
        private static final String TAG_REMOVABLE_APP = "removalbeapp";

        RemovableAppSettings() {
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0038, code lost:
            if (1 == 0) goto L_0x003a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x003a, code lost:
            r2.delete();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0059, code lost:
            if (0 != 0) goto L_0x005c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x005c, code lost:
            return r1;
         */
        public Map<String, ColorRemovableAppInfo> loadFromXml() {
            Slog.d(ColorRemovableAppManager.TAG, "loadFromXml()");
            Map<String, ColorRemovableAppInfo> result = new HashMap<>();
            File stateFile = new File(REMOVABLE_APP_STATE_FILE);
            if (!stateFile.exists()) {
                Slog.i(ColorRemovableAppManager.TAG, "/data/oppo/coloros/removableapp/removable_app_state.xml not exist.");
                return result;
            }
            try {
                FileInputStream in = new AtomicFile(stateFile).openRead();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    parseAppState(parser, result);
                    IoUtils.closeQuietly(in);
                } catch (IOException | XmlPullParserException e) {
                    Slog.e(ColorRemovableAppManager.TAG, "Failed parsing update state file: " + stateFile, e);
                    IoUtils.closeQuietly(in);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(in);
                    if (1 == 0) {
                        stateFile.delete();
                    }
                    throw th;
                }
            } catch (FileNotFoundException fnfe) {
                Slog.i(ColorRemovableAppManager.TAG, "no data update state.");
                fnfe.printStackTrace();
                return result;
            }
        }

        private void parseAppState(XmlPullParser parser, Map<String, ColorRemovableAppInfo> result) throws IOException, XmlPullParserException {
            int outerDepth;
            XmlPullParser xmlPullParser = parser;
            Map<String, ColorRemovableAppInfo> map = result;
            int outerDepth2 = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth2) {
                    return;
                }
                if (type == 3) {
                    xmlPullParser = parser;
                    map = result;
                } else if (type != 4) {
                    if ("package".equals(parser.getName())) {
                        String name = xmlPullParser.getAttributeValue(null, ATTR_PACKAGE_NAME);
                        if (TextUtils.isEmpty(name)) {
                            outerDepth = outerDepth2;
                        } else if (map.get(name) == null) {
                            ColorRemovableAppInfo info = new ColorRemovableAppInfo(name);
                            map.put(name, info);
                            String versionCode = xmlPullParser.getAttributeValue(null, ATTR_PACKAGE_VERSION_CODE);
                            if (!TextUtils.isEmpty(versionCode)) {
                                try {
                                    info.setVersionCode((long) Integer.parseInt(versionCode));
                                    String versionName = xmlPullParser.getAttributeValue(null, ATTR_PACKAGE_VERSION_NAME);
                                    if (!TextUtils.isEmpty(versionName)) {
                                        info.setVersionName(versionName);
                                    } else {
                                        Slog.e(ColorRemovableAppManager.TAG, "package " + name + " versionName is null.");
                                    }
                                    String codePath = xmlPullParser.getAttributeValue(null, ATTR_PACKAGE_CODE_PATH);
                                    if (!TextUtils.isEmpty(codePath)) {
                                        info.setCodePath(codePath);
                                        String baseCodePath = xmlPullParser.getAttributeValue(null, ATTR_PACKAGE_BASE_CODE_PATH);
                                        if (!TextUtils.isEmpty(baseCodePath)) {
                                            info.setBaseCodePath(baseCodePath);
                                            String uninstalledString = xmlPullParser.getAttributeValue(null, ATTR_PACKAGE_UNINSTALLED);
                                            info.setUninstalled((uninstalledString == null || !"true".equals(uninstalledString)) ? ColorRemovableAppManager.DEBUG_VERSION_CODE_EQUAL : true);
                                            outerDepth = outerDepth2;
                                            String fileSizeString = xmlPullParser.getAttributeValue(null, ATTR_PACKAGE_FILE_SIZE);
                                            if (!TextUtils.isEmpty(fileSizeString)) {
                                                try {
                                                    info.setFileSize(Long.parseLong(fileSizeString));
                                                } catch (NumberFormatException e) {
                                                    info.setFileSize(0);
                                                    Slog.e(ColorRemovableAppManager.TAG, "package " + name + ATTR_PACKAGE_FILE_SIZE + fileSizeString + " is invalid.");
                                                    e.printStackTrace();
                                                    xmlPullParser = parser;
                                                    map = result;
                                                    outerDepth2 = outerDepth;
                                                }
                                            }
                                        } else {
                                            Slog.e(ColorRemovableAppManager.TAG, "package " + name + " baseCodePath is null.");
                                            xmlPullParser = parser;
                                            map = result;
                                        }
                                    } else {
                                        Slog.e(ColorRemovableAppManager.TAG, "package " + name + " codePath is null.");
                                        xmlPullParser = parser;
                                        map = result;
                                    }
                                } catch (NumberFormatException e2) {
                                    info.setVersionCode(0);
                                    Slog.e(ColorRemovableAppManager.TAG, "package " + name + " versionCode " + versionCode + " is invalid.");
                                    e2.printStackTrace();
                                    xmlPullParser = parser;
                                    map = result;
                                    outerDepth2 = outerDepth2;
                                }
                            } else {
                                Slog.e(ColorRemovableAppManager.TAG, "package " + name + " versionCode is null.");
                                xmlPullParser = parser;
                                map = result;
                            }
                        } else {
                            Slog.e(ColorRemovableAppManager.TAG, "dunplicate record about " + name + " in removable_app_state.xml");
                            xmlPullParser = parser;
                            map = result;
                        }
                        xmlPullParser = parser;
                        map = result;
                        outerDepth2 = outerDepth;
                    } else {
                        xmlPullParser = parser;
                        map = result;
                    }
                }
            }
        }

        public void persistenceState(Map<String, ColorRemovableAppInfo> infos) {
            if (!new File(REMOVABLE_ROOT_PATH).exists() && !ensureRemovableRootPath()) {
                Slog.e(ColorRemovableAppManager.TAG, "/data/oppo/coloros/removableapp not exists");
            } else if (infos == null) {
                Slog.e(ColorRemovableAppManager.TAG, "persistenceState, infos is null");
            } else {
                AtomicFile destination = new AtomicFile(new File(REMOVABLE_APP_STATE_FILE));
                FileOutputStream out = null;
                try {
                    out = destination.startWrite();
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(out, StandardCharsets.UTF_8.name());
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    serializer.startDocument(null, true);
                    serializer.startTag(null, TAG_REMOVABLE_APP);
                    for (ColorRemovableAppInfo info : infos.values()) {
                        serializer.startTag(null, "package");
                        serializer.attribute(null, ATTR_PACKAGE_NAME, info.getPackageName());
                        serializer.attribute(null, ATTR_PACKAGE_VERSION_CODE, Long.toString(info.getVersionCode()));
                        String str = "";
                        serializer.attribute(null, ATTR_PACKAGE_VERSION_NAME, info.getVersionName() == null ? str : info.getVersionName());
                        serializer.attribute(null, ATTR_PACKAGE_CODE_PATH, info.getCodePath() == null ? str : info.getCodePath());
                        if (info.getBaseCodePath() != null) {
                            str = info.getBaseCodePath();
                        }
                        serializer.attribute(null, ATTR_PACKAGE_BASE_CODE_PATH, str);
                        serializer.attribute(null, ATTR_PACKAGE_UNINSTALLED, Boolean.toString(info.isUninstalled()));
                        serializer.attribute(null, ATTR_PACKAGE_FILE_SIZE, Long.toString(info.getFileSize()));
                        serializer.endTag(null, "package");
                    }
                    serializer.endTag(null, TAG_REMOVABLE_APP);
                    serializer.endDocument();
                    destination.finishWrite(out);
                } catch (Throwable th) {
                    IoUtils.closeQuietly((AutoCloseable) null);
                    throw th;
                }
                IoUtils.closeQuietly(out);
            }
        }

        private boolean ensureRemovableRootPath() {
            File rootPath = new File(REMOVABLE_ROOT_PATH);
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            if (!rootPath.exists()) {
                Slog.e(ColorRemovableAppManager.TAG, "create /data/oppo/coloros/removableapp fail");
            }
            return rootPath.exists();
        }
    }

    private final class MyHandler extends Handler {
        public MyHandler() {
            super(BackgroundThread.getHandler().getLooper());
        }

        public void handleMessage(Message message) {
            ColorRemovableAppManager.this.mSettings.persistenceState(ColorRemovableAppManager.this.mAppInfoMap);
        }
    }

    private void persistence() {
        if (this.mHandler.hasMessages(MSG_WHAT)) {
            this.mHandler.removeMessages(MSG_WHAT);
        }
        this.mHandler.sendEmptyMessage(MSG_WHAT);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorRemovableAppManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    private IColorPackageManagerServiceInner getInner() {
        OppoBasePackageManagerService basePms;
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null || (basePms = (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, packageManagerService)) == null || basePms.mColorPmsInner == null) {
            return IColorPackageManagerServiceInner.DEFAULT;
        }
        return basePms.mColorPmsInner;
    }
}
