package com.mediatek.server.pm;

import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInfoLite;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.pm.PackageManagerException;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageSetting;
import com.android.server.pm.UserManagerService;
import com.mediatek.omadm.PalConstDefs;
import com.mediatek.powerhalwrapper.PowerHalWrapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PmsExtImpl extends PmsExt {
    private static final String KEY_WORD1 = "benchmark";
    private static final int PARSE_IS_OPERATOR = 128;
    private static final String PRODUCT_RSC_PATH_CAP = "/product";
    private static final File REMOVABLE_SYS_APP_LIST_BAK = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", "pms_sysapp_removable_list_bak.txt"});
    private static final File REMOVABLE_SYS_APP_LIST_SYSTEM = Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "permissions", "pms_sysapp_removable_system_list.txt"});
    private static final File REMOVABLE_SYS_APP_LIST_VENDOR = Environment.buildPath(Environment.getVendorDirectory(), new String[]{"etc", "permissions", "pms_sysapp_removable_vendor_list.txt"});
    static final int SCAN_AS_OEM = 524288;
    static final int SCAN_AS_PRIVILEGED = 262144;
    static final int SCAN_AS_PRODUCT = 2097152;
    static final int SCAN_AS_SYSTEM = 131072;
    static final int SCAN_AS_VENDOR = 1048576;
    private static final String SCAN_NAME_NO_DEX = "SCAN_NO_DEX";
    static final int SCAN_NO_DEX = 1;
    private static final String SYS_RSC_PATH_CAP = "/system";
    static final String TAG = "PmsExtImpl";
    private static final String VND_RSC_PATH_CAP = "/vendor";
    private static File mAppLib32InstallDir;
    private static boolean sLogEnabled = false;
    private static String sProductRscPath = SystemProperties.get("ro.product.current_rsc_path", PalConstDefs.EMPTY_STRING);
    private static boolean sRemovableSysAppEnabled = (SystemProperties.getInt("persist.vendor.pms_removable", 0) == 1);
    private static HashSet<String> sRemovableSystemAppSet = new HashSet<>();
    private static HashSet<String> sRemovableSystemAppSetBak = new HashSet<>();
    private static int sScanNoDex;
    private static boolean sSkipScanAppEnabled;
    private static HashSet<String> sSkipScanAppSet = new HashSet<>();
    private static String sSysRscPath = SystemProperties.get("ro.sys.current_rsc_path", PalConstDefs.EMPTY_STRING);
    private static HashSet<String> sUninstallerAppSet = new HashSet<>();
    private static String sVndRscPath = SystemProperties.get("ro.vendor.vnd.current_rsc_path", PalConstDefs.EMPTY_STRING);
    private ApplicationInfo mMediatekApplication = null;
    private PackageManagerService mPms;
    private PowerHalWrapper mPowerHalWrapper = null;
    private UserManagerService mUms;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.vendor.mtk_telephony_add_on_policy", 0) == 1) {
            z = true;
        }
        sSkipScanAppEnabled = z;
    }

    public PmsExtImpl() {
        sScanNoDex = ReflectionHelper.getIntValue(PackageManagerService.class, SCAN_NAME_NO_DEX);
        if (SYS_RSC_PATH_CAP.equals(sSysRscPath)) {
            sSysRscPath = PalConstDefs.EMPTY_STRING;
        }
        if (VND_RSC_PATH_CAP.equals(sVndRscPath)) {
            sVndRscPath = PalConstDefs.EMPTY_STRING;
        }
        if (PRODUCT_RSC_PATH_CAP.equals(sProductRscPath)) {
            sProductRscPath = PalConstDefs.EMPTY_STRING;
        }
        mAppLib32InstallDir = new File(Environment.getDataDirectory(), "app-lib");
    }

    public void init(PackageManagerService pms, UserManagerService ums) {
        this.mPms = pms;
        this.mUms = ums;
    }

    /* JADX INFO: Multiple debug info for r0v27 int: [D('targetFile' java.io.File), D('parseFlags' int)] */
    /* JADX INFO: Multiple debug info for r0v33 int: [D('targetFile' java.io.File), D('parseFlags' int)] */
    /* JADX INFO: Multiple debug info for r0v48 int: [D('targetFile' java.io.File), D('parseFlags' int)] */
    /* JADX INFO: Multiple debug info for r0v61 int: [D('targetFile' java.io.File), D('parseFlags' int)] */
    /* JADX INFO: Multiple debug info for r0v66 int: [D('targetFile' java.io.File), D('parseFlags' int)] */
    public void scanDirLI(int index, int defParseFlags, int defScanFlags, long currentTime) {
        File targetFile;
        File targetFile2;
        File targetFile3;
        File targetFile4;
        File targetFile5;
        File targetFile6;
        File targetFile7;
        File targetFile8;
        File targetFile9;
        File targetFile10;
        File targetFile11;
        switch (index) {
            case 1:
                this.mPms.scanDirTracedLI(new File("/custom/framework"), defParseFlags | 16, defScanFlags | sScanNoDex | SCAN_AS_SYSTEM, currentTime);
                return;
            case 2:
                File targetFile12 = new File(Environment.getVendorDirectory(), "framework");
                try {
                    targetFile = targetFile12.getCanonicalFile();
                } catch (IOException e) {
                    targetFile = targetFile12;
                }
                this.mPms.scanDirTracedLI(targetFile, defParseFlags | 16, defScanFlags | sScanNoDex | SCAN_AS_SYSTEM, currentTime);
                return;
            case PalConstDefs.ADMIN_NET_LOST /* 3 */:
                File targetFile13 = new File(Environment.getVendorDirectory(), "priv-app");
                try {
                    targetFile2 = targetFile13.getCanonicalFile();
                } catch (IOException e2) {
                    targetFile2 = targetFile13;
                }
                this.mPms.scanDirTracedLI(targetFile2, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM | SCAN_AS_PRIVILEGED, currentTime);
                return;
            case 4:
                File targetFile14 = new File(Environment.getVendorDirectory(), "/operator/app");
                try {
                    targetFile3 = targetFile14.getCanonicalFile();
                } catch (IOException e3) {
                    targetFile3 = targetFile14;
                }
                this.mPms.scanDirTracedLI(targetFile3, defParseFlags | PARSE_IS_OPERATOR, defScanFlags, currentTime);
                return;
            case 5:
                this.mPms.scanDirTracedLI(new File(Environment.getRootDirectory(), "plugin"), defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM, currentTime);
                return;
            case 6:
                File targetFile15 = new File(Environment.getVendorDirectory(), "plugin");
                try {
                    targetFile4 = targetFile15.getCanonicalFile();
                } catch (IOException e4) {
                    targetFile4 = targetFile15;
                }
                this.mPms.scanDirTracedLI(targetFile4, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM, currentTime);
                return;
            case PalConstDefs.RET_ERR_ARGS /* 7 */:
                this.mPms.scanDirTracedLI(new File("/custom/app"), defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM, currentTime);
                return;
            case PalConstDefs.RET_ERR_UNDEF /* 8 */:
                this.mPms.scanDirTracedLI(new File("/custom/plugin"), defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM, currentTime);
                return;
            case 9:
                if (!sSysRscPath.isEmpty()) {
                    this.mPms.scanDirTracedLI(new File(sSysRscPath, "overlay"), defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM, currentTime);
                }
                if (!sVndRscPath.isEmpty()) {
                    File targetFile16 = new File(sVndRscPath, "overlay");
                    try {
                        targetFile5 = targetFile16.getCanonicalFile();
                    } catch (IOException e5) {
                        targetFile5 = targetFile16;
                    }
                    this.mPms.scanDirTracedLI(targetFile5, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM | SCAN_AS_VENDOR, currentTime);
                    return;
                }
                return;
            case PalConstDefs.RET_ERR_STATE /* 10 */:
                if (!sSysRscPath.isEmpty()) {
                    this.mPms.scanDirTracedLI(new File(sSysRscPath, "framework"), defParseFlags | 16, defScanFlags | 1 | SCAN_AS_SYSTEM | SCAN_AS_PRIVILEGED, currentTime);
                }
                if (!sVndRscPath.isEmpty()) {
                    File targetFile17 = new File(sVndRscPath, "framework");
                    try {
                        targetFile6 = targetFile17.getCanonicalFile();
                    } catch (IOException e6) {
                        targetFile6 = targetFile17;
                    }
                    this.mPms.scanDirTracedLI(targetFile6, defParseFlags | 16, defScanFlags | 1 | SCAN_AS_SYSTEM | SCAN_AS_VENDOR | SCAN_AS_PRIVILEGED, currentTime);
                    return;
                }
                return;
            case PalConstDefs.RET_ERR_NORES /* 11 */:
                if (!sSysRscPath.isEmpty()) {
                    targetFile7 = new File(sSysRscPath, "priv-app");
                    this.mPms.scanDirTracedLI(targetFile7, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM | SCAN_AS_PRIVILEGED, currentTime);
                } else {
                    targetFile7 = null;
                }
                if (!sVndRscPath.isEmpty()) {
                    File targetFile18 = new File(sVndRscPath, "priv-app");
                    try {
                        targetFile18 = targetFile18.getCanonicalFile();
                    } catch (IOException e7) {
                    }
                    this.mPms.scanDirTracedLI(targetFile18, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM | SCAN_AS_VENDOR | SCAN_AS_PRIVILEGED, currentTime);
                }
                if (!sProductRscPath.isEmpty()) {
                    File targetFile19 = new File(sProductRscPath, "priv-app");
                    try {
                        targetFile8 = targetFile19.getCanonicalFile();
                    } catch (IOException e8) {
                        targetFile8 = targetFile19;
                    }
                    this.mPms.scanDirTracedLI(targetFile8, defParseFlags | 16, SCAN_AS_PRODUCT | defScanFlags | SCAN_AS_SYSTEM | SCAN_AS_PRIVILEGED, currentTime);
                    return;
                }
                return;
            case PalConstDefs.RET_ERR_NOPERM /* 12 */:
                if (!sSysRscPath.isEmpty()) {
                    targetFile9 = new File(sSysRscPath, "app");
                    this.mPms.scanDirTracedLI(targetFile9, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM, currentTime);
                } else {
                    targetFile9 = null;
                }
                if (!sVndRscPath.isEmpty()) {
                    File targetFile20 = new File(sVndRscPath, "app");
                    try {
                        targetFile20 = targetFile20.getCanonicalFile();
                    } catch (IOException e9) {
                    }
                    this.mPms.scanDirTracedLI(targetFile20, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM | SCAN_AS_VENDOR, currentTime);
                }
                if (!sProductRscPath.isEmpty()) {
                    File targetFile21 = new File(sProductRscPath, "app");
                    try {
                        targetFile10 = targetFile21.getCanonicalFile();
                    } catch (IOException e10) {
                        targetFile10 = targetFile21;
                    }
                    this.mPms.scanDirTracedLI(targetFile10, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM | SCAN_AS_PRODUCT, currentTime);
                    return;
                }
                return;
            case PalConstDefs.RET_ERR_TMOUT /* 13 */:
                if (!sSysRscPath.isEmpty()) {
                    this.mPms.scanDirTracedLI(new File(sSysRscPath, "plugin"), defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM, currentTime);
                }
                if (!sVndRscPath.isEmpty()) {
                    File targetFile22 = new File(sVndRscPath, "plugin");
                    try {
                        targetFile11 = targetFile22.getCanonicalFile();
                    } catch (IOException e11) {
                        targetFile11 = targetFile22;
                    }
                    this.mPms.scanDirTracedLI(targetFile11, defParseFlags | 16, defScanFlags | SCAN_AS_SYSTEM | SCAN_AS_VENDOR, currentTime);
                    return;
                }
                return;
            default:
                Slog.d(TAG, "Unknown index for ext:" + index);
                return;
        }
    }

    public void scanMoreDirLi(int defParseFlags, int defScanFlags) {
        scanDirLI(2, defParseFlags, defScanFlags, 0);
        scanDirLI(6, defParseFlags, defScanFlags, 0);
        scanDirLI(4, defParseFlags, defScanFlags, 0);
        scanDirLI(13, defParseFlags, defScanFlags, 0);
        scanDirLI(5, defParseFlags, defScanFlags, 0);
        scanDirLI(1, defParseFlags, defScanFlags, 0);
        carrierExpressInstall(defParseFlags, defScanFlags, 0);
    }

    public void checkMtkResPkg(PackageParser.Package pkg) throws PackageManagerException {
        if (!pkg.packageName.equals("com.mediatek")) {
            return;
        }
        if (this.mMediatekApplication == null) {
            this.mMediatekApplication = pkg.applicationInfo;
        } else {
            Slog.w(TAG, "Core mediatek package being redefined. Skipping.");
            throw new PackageManagerException(-5, "Core android package being redefined. Skipping.");
        }
    }

    public boolean needSkipScanning(PackageParser.Package pkg, PackageSetting updatedPkg, PackageSetting ps) {
        if (sSkipScanAppEnabled && sSkipScanAppSet.contains(pkg.packageName)) {
            Slog.d(TAG, "Skip scan package:" + pkg.packageName);
            return true;
        } else if (this.mPms.isFirstBoot() || !isRemovableSysApp(pkg.packageName) || ps != null || updatedPkg != null) {
            if (ps != null || updatedPkg == null) {
                return false;
            }
            Slog.d(TAG, "Skip scanning uninstalled package: " + pkg.packageName);
            return true;
        } else if (!this.mPms.isDeviceUpgrading() || sRemovableSystemAppSetBak.contains(pkg.packageName)) {
            Slog.d(TAG, "Skip scanning uninstalled sys package " + pkg.packageName);
            return true;
        } else {
            Slog.d(TAG, "New added removable sys app by OTA:" + pkg.packageName);
            return false;
        }
    }

    public boolean needSkipAppInfo(ApplicationInfo ai) {
        if (!sRemovableSysAppEnabled || ai == null || (ai.flags & 8388608) != 0) {
            return false;
        }
        return isRemovableSysApp(ai.packageName);
    }

    public void onPackageAdded(String packageName, PackageSetting pkgSetting, int userId) {
        if (pkgSetting != null) {
            checkBenchmark(pkgSetting.getPackage());
        }
        updateUninstallerAppSetWithPkg(packageName, userId);
    }

    public void initBeforeScan() {
        if (sLogEnabled) {
            Slog.d(TAG, "initBeforeScan start");
        }
        if (sRemovableSysAppEnabled) {
            buildRemovableSystemAppSet();
        }
        if (sSkipScanAppEnabled) {
            buildSkipScanAppSet();
        }
        if (sLogEnabled) {
            Slog.d(TAG, "initBeforeScan end");
        }
    }

    public void initAfterScan(ArrayMap<String, PackageSetting> settingsPackages) {
        if (sRemovableSysAppEnabled) {
            if (sLogEnabled) {
                Slog.d(TAG, "initAfterScan start");
            }
            buildUninstallerAppSet();
            if (this.mPms.isFirstBoot() || this.mPms.isDeviceUpgrading()) {
                if (sRemovableSystemAppSetBak.isEmpty()) {
                    sWriteRemovableSystemAppToFile(sRemovableSystemAppSet, REMOVABLE_SYS_APP_LIST_BAK);
                } else if (onUpgradeRemovableSystemAppList(sRemovableSystemAppSetBak, sRemovableSystemAppSet, settingsPackages)) {
                    sWriteRemovableSystemAppToFile(sRemovableSystemAppSet, REMOVABLE_SYS_APP_LIST_BAK);
                }
            }
            if (sLogEnabled) {
                Slog.d(TAG, "initAfterScan end");
            }
        }
    }

    public int customizeInstallPkgFlags(int installFlags, PackageInfoLite pkgLite, ArrayMap<String, PackageSetting> settingsPackages, UserHandle user) {
        PackageSetting ps = settingsPackages.get(pkgLite.packageName);
        if (ps == null || !isRemovableSysApp(pkgLite.packageName)) {
            return installFlags;
        }
        int[] installedUsers = ps.queryInstalledUsers(this.mUms.getUserIds(), true);
        if (sLogEnabled) {
            Slog.d(TAG, "getUser()=" + user + " installedUsers=" + Arrays.toString(installedUsers));
        }
        if ((user != UserHandle.ALL && ArrayUtils.contains(installedUsers, user.getIdentifier())) || installedUsers == null || installedUsers.length == this.mUms.getUserIds().length) {
            return installFlags;
        }
        Slog.d(TAG, "built in app, set replace and allow downgrade");
        return installFlags | SCAN_AS_VENDOR | 2;
    }

    public void updatePackageSettings(int userId, String pkgName, PackageParser.Package newPackage, PackageSetting ps, int[] allUsers, String installerPackageName) {
        if (userId == -1 && isRemovableSysApp(pkgName) && (newPackage.applicationInfo.flags & 1) != 0) {
            for (int currentUserId : allUsers) {
                ps.setInstalled(true, currentUserId);
                ps.setEnabled(0, currentUserId, installerPackageName);
            }
        }
    }

    public int customizeDeletePkgFlags(int deleteFlags, String packageName) {
        if (isRemovableSysApp(packageName)) {
            return deleteFlags | 4;
        }
        return deleteFlags;
    }

    public int customizeDeletePkg(int[] users, String packageName, int versionCode, int delFlags) {
        int userFlags = delFlags & -3;
        int returnCode = 1;
        for (int userId : users) {
            returnCode = this.mPms.deletePackageX(packageName, (long) versionCode, userId, userFlags);
            if (returnCode != 1) {
                Slog.w(TAG, "Package delete failed for user " + userId + ", returnCode " + returnCode);
            }
        }
        return returnCode;
    }

    public boolean dumpCmdHandle(String cmd, PrintWriter pw, String[] args, int opti) {
        if ("log".equals(cmd)) {
            configLogTag(pw, args, opti);
            return true;
        } else if (!"removable".equals(cmd)) {
            return PmsExtImpl.super.dumpCmdHandle(cmd, pw, args, opti);
        } else {
            dumpRemovableSysApps(pw, args, opti);
            return true;
        }
    }

    public ApplicationInfo updateApplicationInfoForRemovable(ApplicationInfo oldAppInfo) {
        if (!sRemovableSysAppEnabled || oldAppInfo == null) {
            return oldAppInfo;
        }
        return updateApplicationInfoForRemovable(this.mPms.getNameForUid(Binder.getCallingUid()), oldAppInfo);
    }

    public ApplicationInfo updateApplicationInfoForRemovable(String nameForUid, ApplicationInfo oldAppInfo) {
        if (!sRemovableSysAppEnabled || oldAppInfo == null) {
            return oldAppInfo;
        }
        boolean clearSystemFlag = false;
        String packageName = oldAppInfo.packageName;
        if (!(Binder.getCallingPid() == Process.myPid() || !isRemovableSysApp(packageName) || nameForUid == null)) {
            String[] strs = nameForUid.split(":");
            int i = 0;
            if (strs.length == 1) {
                clearSystemFlag = isUninstallerApp(strs[0]);
            } else if (strs.length > 1 && !(clearSystemFlag = strs[1].equals("1000"))) {
                try {
                    String[] pkgs = AppGlobals.getPackageManager().getPackagesForUid(Integer.valueOf(strs[1]).intValue());
                    int length = pkgs.length;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        String pkg = pkgs[i];
                        clearSystemFlag = isUninstallerApp(pkg);
                        if (!clearSystemFlag) {
                            i++;
                        } else if (sLogEnabled) {
                            Slog.d(TAG, "shared uid=" + strs[1] + " pkg=" + pkg);
                        }
                    }
                } catch (RemoteException e) {
                }
            }
            if (sLogEnabled) {
                Slog.d(TAG, "judge for " + packageName + " name=" + nameForUid + " clear ? " + clearSystemFlag);
            }
        }
        if (!clearSystemFlag) {
            return oldAppInfo;
        }
        ApplicationInfo newAppInfo = new ApplicationInfo(oldAppInfo);
        newAppInfo.flags &= -130;
        return newAppInfo;
    }

    public ActivityInfo updateActivityInfoForRemovable(ActivityInfo info) throws RemoteException {
        if (info != null) {
            info.applicationInfo = updateApplicationInfoForRemovable(AppGlobals.getPackageManager().getNameForUid(Binder.getCallingUid()), info.applicationInfo);
        }
        return info;
    }

    public List<ResolveInfo> updateResolveInfoListForRemovable(List<ResolveInfo> apps) throws RemoteException {
        if (apps != null) {
            for (ResolveInfo info : apps) {
                info.activityInfo.applicationInfo = updateApplicationInfoForRemovable(AppGlobals.getPackageManager().getNameForUid(Binder.getCallingUid()), info.activityInfo.applicationInfo);
            }
        }
        return apps;
    }

    public PackageInfo updatePackageInfoForRemovable(PackageInfo oldPkgInfo) {
        if (!sRemovableSysAppEnabled || oldPkgInfo == null) {
            return oldPkgInfo;
        }
        oldPkgInfo.applicationInfo = updateApplicationInfoForRemovable(oldPkgInfo.applicationInfo);
        return oldPkgInfo;
    }

    public boolean isRemovableSysApp(String pkgName) {
        if (sRemovableSysAppEnabled) {
            return sRemovableSystemAppSet.contains(pkgName);
        }
        return false;
    }

    public boolean updateNativeLibDir(ApplicationInfo info, String codePath) {
        if (codePath == null || !codePath.contains("vendor/operator/app")) {
            return false;
        }
        info.nativeLibraryRootDir = new File(mAppLib32InstallDir, PackageManagerService.deriveCodePathName(codePath)).getAbsolutePath();
        info.nativeLibraryRootRequiresIsa = false;
        info.nativeLibraryDir = info.nativeLibraryRootDir;
        return true;
    }

    private void configLogTag(PrintWriter pw, String[] args, int opti) {
        if (opti + 1 >= args.length) {
            pw.println("  Invalid argument!");
            return;
        }
        String tag = args[opti];
        boolean on = "on".equals(args[opti + 1]);
        if ("a".equals(tag)) {
            PackageManagerService.DEBUG_SETTINGS = on;
            PackageManagerService.DEBUG_PREFERRED = on;
            PackageManagerService.DEBUG_UPGRADE = on;
            PackageManagerService.DEBUG_DOMAIN_VERIFICATION = on;
            PackageManagerService.DEBUG_BACKUP = on;
            PackageManagerService.DEBUG_INSTALL = on;
            PackageManagerService.DEBUG_REMOVE = on;
            PackageManagerService.DEBUG_BROADCASTS = on;
            PackageManagerService.DEBUG_PACKAGE_INFO = on;
            PackageManagerService.DEBUG_INTENT_MATCHING = on;
            PackageManagerService.DEBUG_PACKAGE_SCANNING = on;
            PackageManagerService.DEBUG_VERIFY = on;
            PackageManagerService.DEBUG_PERMISSIONS = on;
            PackageManagerService.DEBUG_SHARED_LIBRARIES = on;
            PackageManagerService.DEBUG_DEXOPT = on;
            PackageManagerService.DEBUG_ABI_SELECTION = on;
            PackageManagerService.DEBUG_INSTANT = on;
            PackageManagerService.DEBUG_APP_DATA = on;
        }
    }

    private void carrierExpressInstall(int defParseFlags, int defScanFlags, long currentTime) {
        if (!"1".equals(SystemProperties.get("ro.vendor.mtk_carrierexpress_inst_sup"))) {
            scanDirLI(7, defParseFlags, defScanFlags, currentTime);
            scanDirLI(8, defParseFlags, defScanFlags, currentTime);
            return;
        }
        scanOperatorDirLI(defScanFlags);
    }

    private void scanOperatorDirLI(int scanFlags) {
        String opStr = SystemProperties.get("persist.vendor.operator.optr");
        if (opStr == null || opStr.length() <= 0) {
            Slog.d(TAG, "No operater defined.");
            return;
        }
        String opFileName = "usp-apks-path-" + opStr + ".txt";
        File customUniDir = new File("/custom/usp");
        if (customUniDir.exists()) {
            scanCxpApp(customUniDir, opFileName, scanFlags);
            return;
        }
        File systemUniDir = new File("/system/usp");
        if (systemUniDir.exists()) {
            scanCxpApp(systemUniDir, opFileName, scanFlags);
        } else {
            Slog.d(TAG, "No Carrier Express Pack directory.");
        }
    }

    private void scanCxpApp(File uniPath, String opFileName, int scanFlags) {
        File opFilePath;
        File opFilePath2;
        String str;
        String str2;
        String str3;
        PackageManagerException e;
        PmsExtImpl pmsExtImpl = this;
        File opFilePath3 = new File(uniPath, opFileName);
        List<String> appPathList = pmsExtImpl.readPathsFromFile(opFilePath3);
        int i = 0;
        while (i < appPathList.size()) {
            String path = appPathList.get(i);
            File file = new File(path);
            int flag = path.contains("removable") ? PARSE_IS_OPERATOR : 131088;
            long startScanTime = SystemClock.uptimeMillis();
            Slog.d(TAG, "scan package: " + file.toString() + " , start at: " + startScanTime + "ms.");
            try {
                PackageManagerService packageManagerService = pmsExtImpl.mPms;
                int i2 = flag | 1;
                str3 = TAG;
                str2 = "ms.";
                str = "scan package: ";
                opFilePath = opFilePath3;
                opFilePath2 = file;
                try {
                    packageManagerService.scanPackageTracedLI(file, i2, scanFlags, 0, (UserHandle) null);
                } catch (PackageManagerException e2) {
                    e = e2;
                }
            } catch (PackageManagerException e3) {
                e = e3;
                opFilePath = opFilePath3;
                str3 = TAG;
                str2 = "ms.";
                str = "scan package: ";
                opFilePath2 = file;
                Slog.w(str3, "Failed to parse " + opFilePath2 + ": " + e.getMessage());
                long endScanTime = SystemClock.uptimeMillis();
                Slog.d(str3, str + opFilePath2.toString() + " , end at: " + endScanTime + "ms. elapsed time = " + (endScanTime - startScanTime) + str2);
                i++;
                pmsExtImpl = this;
                opFilePath3 = opFilePath;
            }
            long endScanTime2 = SystemClock.uptimeMillis();
            Slog.d(str3, str + opFilePath2.toString() + " , end at: " + endScanTime2 + "ms. elapsed time = " + (endScanTime2 - startScanTime) + str2);
            i++;
            pmsExtImpl = this;
            opFilePath3 = opFilePath;
        }
    }

    private List<String> readPathsFromFile(File packagePathsFile) {
        byte[] bArr = new byte[((int) packagePathsFile.length())];
        List<String> fileContents = new ArrayList<>();
        try {
            FileInputStream inputStream = new FileInputStream(packagePathsFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                String receiveString = bufferedReader.readLine();
                if (receiveString == null) {
                    break;
                }
                fileContents.add(receiveString);
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            Slog.d(TAG, "File not found: " + e.toString());
        } catch (IOException e2) {
            Slog.d(TAG, "Can not read file: " + e2.toString());
        }
        return fileContents;
    }

    private void dumpRemovableSysApps(PrintWriter pw, String[] args, int opti) {
        pw.println(" sRemovableSysAppEnabled: " + sRemovableSysAppEnabled);
        Iterator<String> it = sRemovableSystemAppSet.iterator();
        pw.println(" sRemovableSystemAppSet:");
        while (it.hasNext()) {
            pw.println("  " + it.next());
        }
        Iterator<String> it2 = sUninstallerAppSet.iterator();
        pw.println(" sUninstallerAppSet:");
        while (it2.hasNext()) {
            pw.println("  " + it2.next());
        }
    }

    private void buildRemovableSystemAppSet() {
        if (sRemovableSysAppEnabled) {
            if (sLogEnabled) {
                Slog.d(TAG, "BuildRemovableSystemAppSet start");
            }
            sGetRemovableSystemAppFromFile(sRemovableSystemAppSet, REMOVABLE_SYS_APP_LIST_SYSTEM);
            sGetRemovableSystemAppFromFile(sRemovableSystemAppSet, REMOVABLE_SYS_APP_LIST_VENDOR);
            sGetRemovableSystemAppFromFile(sRemovableSystemAppSetBak, REMOVABLE_SYS_APP_LIST_BAK);
            if (sLogEnabled) {
                Slog.d(TAG, "BuildRemovableSystemAppSet end");
            }
        }
    }

    private void buildUninstallerAppSet() {
        if (sRemovableSysAppEnabled) {
            if (sLogEnabled) {
                Slog.d(TAG, "buildUninstallerAppSet start");
            }
            int[] allUserIds = this.mUms.getUserIds();
            for (int i = 0; i < allUserIds.length; i++) {
                Intent settingIntent = new Intent("android.settings.SETTINGS");
                settingIntent.addCategory("android.intent.category.DEFAULT");
                getAppSetByIntent(sUninstallerAppSet, settingIntent, allUserIds[i]);
                Intent launcherIntent = new Intent("android.intent.action.MAIN");
                launcherIntent.addCategory("android.intent.category.HOME");
                launcherIntent.addCategory("android.intent.category.DEFAULT");
                getAppSetByIntent(sUninstallerAppSet, launcherIntent, allUserIds[i]);
                Intent storeIntent = new Intent("android.intent.action.MAIN");
                storeIntent.addCategory("android.intent.category.APP_MARKET");
                storeIntent.addCategory("android.intent.category.DEFAULT");
                getAppSetByIntent(sUninstallerAppSet, storeIntent, allUserIds[i]);
                Intent installIntent = new Intent("android.intent.action.INSTALL_PACKAGE");
                installIntent.addCategory("android.intent.category.DEFAULT");
                installIntent.setData(Uri.fromParts("package", "foo.bar", null));
                Intent uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE");
                uninstallIntent.addCategory("android.intent.category.DEFAULT");
                uninstallIntent.setData(Uri.fromParts("package", "foo.bar", null));
                getAppSetByIntent(sUninstallerAppSet, installIntent, allUserIds[i]);
                getAppSetByIntent(sUninstallerAppSet, uninstallIntent, allUserIds[i]);
                if (sLogEnabled) {
                    Slog.d(TAG, "buildUninstallerAppSet end");
                }
            }
        }
    }

    private void updateUninstallerAppSetWithPkg(String pkgName, int userId) {
        if (sRemovableSysAppEnabled && pkgName != null) {
            if (sUninstallerAppSet.contains(pkgName)) {
                Slog.d(TAG, "already in set:" + pkgName);
                return;
            }
            if (sLogEnabled) {
                Slog.d(TAG, "updateUninstallerAppSetWithPkg for:" + pkgName + " with:" + userId);
            }
            Intent launcherIntent = new Intent("android.intent.action.MAIN");
            launcherIntent.addCategory("android.intent.category.HOME");
            launcherIntent.addCategory("android.intent.category.DEFAULT");
            launcherIntent.setPackage(pkgName);
            getAppSetByIntent(sUninstallerAppSet, launcherIntent, userId);
            Intent storeIntent = new Intent("android.intent.action.MAIN");
            storeIntent.addCategory("android.intent.category.APP_MARKET");
            storeIntent.setPackage(pkgName);
            getAppSetByIntent(sUninstallerAppSet, storeIntent, userId);
            if (sLogEnabled) {
                Slog.d(TAG, "updateUninstallerAppSetWithPkg end");
            }
        }
    }

    private static void sGetRemovableSystemAppFromFile(HashSet<String> resultSet, File file) {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            if (file.exists()) {
                FileReader fr2 = new FileReader(file);
                BufferedReader br2 = new BufferedReader(fr2);
                while (true) {
                    String line = br2.readLine();
                    if (line != null) {
                        String line2 = line.trim();
                        if (!TextUtils.isEmpty(line2)) {
                            if (sLogEnabled) {
                                Slog.d(TAG, "read line " + line2);
                            }
                            resultSet.add(line2);
                        }
                    } else {
                        try {
                            br2.close();
                            fr2.close();
                            return;
                        } catch (IOException io) {
                            Slog.d(TAG, io.getMessage());
                            return;
                        }
                    }
                }
            } else {
                Slog.d(TAG, "file in " + file + " does not exist!");
                if (0 != 0) {
                    try {
                        br.close();
                    } catch (IOException io2) {
                        Slog.d(TAG, io2.getMessage());
                        return;
                    }
                }
                if (0 != 0) {
                    fr.close();
                }
            }
        } catch (IOException io3) {
            Slog.d(TAG, io3.getMessage());
            if (0 != 0) {
                br.close();
            }
            if (0 != 0) {
                fr.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    br.close();
                } catch (IOException io4) {
                    Slog.d(TAG, io4.getMessage());
                    throw th;
                }
            }
            if (0 != 0) {
                fr.close();
            }
            throw th;
        }
    }

    private static void sWriteRemovableSystemAppToFile(HashSet<String> resultSet, File file) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            FileWriter fw2 = new FileWriter(file, false);
            BufferedWriter bw2 = new BufferedWriter(fw2);
            if (resultSet != null) {
                if (!resultSet.isEmpty()) {
                    Iterator<String> it = resultSet.iterator();
                    while (it.hasNext()) {
                        bw2.write(it.next());
                        bw2.newLine();
                    }
                    bw2.flush();
                    try {
                        bw2.close();
                        fw2.close();
                        return;
                    } catch (IOException io) {
                        Slog.d(TAG, io.getMessage());
                        return;
                    }
                }
            }
            bw2.write(PalConstDefs.EMPTY_STRING);
            bw2.flush();
            try {
                bw2.close();
                fw2.close();
            } catch (IOException io2) {
                Slog.d(TAG, io2.getMessage());
            }
        } catch (IOException io3) {
            Slog.d(TAG, io3.getMessage());
            if (0 != 0) {
                bw.close();
            }
            if (0 != 0) {
                fw.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bw.close();
                } catch (IOException io4) {
                    Slog.d(TAG, io4.getMessage());
                    throw th;
                }
            }
            if (0 != 0) {
                fw.close();
            }
            throw th;
        }
    }

    private static boolean isUninstallerApp(String pkgName) {
        if (sRemovableSysAppEnabled) {
            return sUninstallerAppSet.contains(pkgName);
        }
        return false;
    }

    private void getAppSetByIntent(HashSet<String> resultSet, Intent targetIntent, int userId) {
        List<ResolveInfo> matches = this.mPms.queryIntentActivitiesInternal(targetIntent, (String) null, 786944, userId);
        int size = matches.size();
        if (sLogEnabled) {
            Slog.d(TAG, "getAppSetByIntent:" + targetIntent + " size=" + size);
        }
        if (size >= 1) {
            for (int i = 0; i < size; i++) {
                resultSet.add(matches.get(i).getComponentInfo().packageName);
            }
        }
    }

    private boolean onUpgradeRemovableSystemAppList(HashSet<String> oldSet, HashSet<String> newSet, ArrayMap<String, PackageSetting> settingsPackages) {
        HashSet<String> added = new HashSet<>();
        HashSet<String> removed = new HashSet<>();
        added.addAll(newSet);
        added.removeAll(oldSet);
        removed.addAll(oldSet);
        removed.removeAll(newSet);
        if (sLogEnabled) {
            Slog.d(TAG, "onUpgradeRemovableSystemAppList: add=" + added.size() + " removed=" + removed.size());
        }
        int[] allUserIds = this.mUms.getUserIds();
        Iterator<String> it = removed.iterator();
        boolean updated = false;
        while (true) {
            boolean z = true;
            if (!it.hasNext()) {
                break;
            }
            PackageSetting ps = settingsPackages.get(it.next());
            if (ps != null) {
                int[] uninstalledUsers = ps.queryInstalledUsers(allUserIds, false);
                if (uninstalledUsers.length > 0) {
                    int i = 0;
                    while (i < uninstalledUsers.length) {
                        ps.setInstalled(z, uninstalledUsers[i]);
                        ps.setEnabled(0, uninstalledUsers[i], "android");
                        updated = true;
                        i++;
                        z = true;
                    }
                }
            }
        }
        if (updated) {
            this.mPms.scheduleWriteSettingsLocked();
        }
        if (removed.size() > 0 || added.size() > 0) {
            return true;
        }
        return false;
    }

    public void checkBenchmark(PackageParser.Package pkg) {
        boolean isNeedAdd = false;
        String pkgName = pkg.packageName;
        if (pkgName.contains(KEY_WORD1)) {
            Slog.d(TAG, "care package name is " + pkg.packageName);
            isNeedAdd = true;
        }
        if (!isNeedAdd) {
            Iterator it = pkg.requestedPermissions.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String requestedPermission = (String) it.next();
                if (requestedPermission.contains(KEY_WORD1)) {
                    Slog.d(TAG, pkgName + " requestedPermission = " + requestedPermission);
                    isNeedAdd = true;
                    break;
                }
            }
        }
        if (!isNeedAdd) {
            Iterator it2 = pkg.activities.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                String className = ((PackageParser.Activity) it2.next()).className;
                if (className.contains(KEY_WORD1)) {
                    Slog.d(TAG, pkgName + " ActivityClassName = " + className);
                    isNeedAdd = true;
                    break;
                }
            }
        }
        if (!isNeedAdd) {
            Iterator it3 = pkg.receivers.iterator();
            while (it3.hasNext()) {
                Iterator it4 = ((PackageParser.Activity) it3.next()).intents.iterator();
                while (it4.hasNext()) {
                    PackageParser.ActivityIntentInfo intent = (PackageParser.ActivityIntentInfo) it4.next();
                    int count = intent.countCategories();
                    int i = 0;
                    while (true) {
                        if (i >= count) {
                            break;
                        }
                        String category = intent.getCategory(i);
                        if (category.contains(KEY_WORD1)) {
                            Slog.d(TAG, "care package name is " + pkgName + " category =" + category);
                            isNeedAdd = true;
                            break;
                        }
                        i++;
                    }
                }
            }
        }
        if (isNeedAdd && this.mPowerHalWrapper != null) {
            Slog.d(TAG, "setSportsApk " + pkgName);
        }
    }

    private void buildSkipScanAppSet() {
        String fileNameStr = SystemProperties.get("ro.vendor.mtk_telephon_add_on_pkg_file");
        if (sSkipScanAppEnabled) {
            if (sLogEnabled) {
                Slog.d(TAG, "BuildSkipScanAppSet start");
            }
            sGetRemovableSystemAppFromFile(sSkipScanAppSet, new File(sSysRscPath, fileNameStr));
            if (sLogEnabled) {
                Slog.d(TAG, "BuildSkipScanAppSet end");
            }
        }
    }
}
