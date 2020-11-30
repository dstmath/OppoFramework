package com.mediatek.server.pm;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInfoLite;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import com.android.server.pm.PackageManagerException;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageSetting;
import com.android.server.pm.UserManagerService;
import java.io.PrintWriter;
import java.util.List;

public class PmsExt {
    public static final int INDEX_CIP_FW = 1;
    public static final int INDEX_CUSTOM_APP = 7;
    public static final int INDEX_CUSTOM_PLUGIN = 8;
    public static final int INDEX_OP_APP = 4;
    public static final int INDEX_ROOT_PLUGIN = 5;
    public static final int INDEX_RSC_APP = 12;
    public static final int INDEX_RSC_FW = 10;
    public static final int INDEX_RSC_OVERLAY = 9;
    public static final int INDEX_RSC_PLUGIN = 13;
    public static final int INDEX_RSC_PRIV = 11;
    public static final int INDEX_VENDOR_FW = 2;
    public static final int INDEX_VENDOR_PLUGIN = 6;
    public static final int INDEX_VENDOR_PRIV = 3;

    public void init(PackageManagerService pms, UserManagerService ums) {
    }

    public void scanDirLI(int ident, int defParseFlags, int defScanFlags, long currentTime) {
    }

    public void scanMoreDirLi(int defParseFlags, int defScanFlags) {
    }

    public void checkMtkResPkg(PackageParser.Package pkg) throws PackageManagerException {
    }

    public boolean needSkipScanning(PackageParser.Package pkg, PackageSetting updatedPkg, PackageSetting ps) {
        return false;
    }

    public boolean needSkipAppInfo(ApplicationInfo ai) {
        return false;
    }

    public void onPackageAdded(String packageName, PackageSetting pkgSetting, int userId) {
    }

    public void initBeforeScan() {
    }

    public void initAfterScan(ArrayMap<String, PackageSetting> arrayMap) {
    }

    public int customizeInstallPkgFlags(int installFlags, PackageInfoLite pkgLite, ArrayMap<String, PackageSetting> arrayMap, UserHandle user) {
        return installFlags;
    }

    public void updatePackageSettings(int userId, String pkgName, PackageParser.Package newPackage, PackageSetting ps, int[] allUsers, String installerPackageName) {
    }

    public int customizeDeletePkgFlags(int deleteFlags, String packageName) {
        return deleteFlags;
    }

    public int customizeDeletePkg(int[] users, String packageName, int versionCode, int delFlags) {
        return 1;
    }

    public boolean dumpCmdHandle(String cmd, PrintWriter pw, String[] args, int opti) {
        return false;
    }

    public ApplicationInfo updateApplicationInfoForRemovable(ApplicationInfo oldAppInfo) {
        return oldAppInfo;
    }

    public ApplicationInfo updateApplicationInfoForRemovable(String nameForUid, ApplicationInfo oldAppInfo) {
        return oldAppInfo;
    }

    public ActivityInfo updateActivityInfoForRemovable(ActivityInfo info) throws RemoteException {
        return info;
    }

    public List<ResolveInfo> updateResolveInfoListForRemovable(List<ResolveInfo> apps) throws RemoteException {
        return apps;
    }

    public PackageInfo updatePackageInfoForRemovable(PackageInfo oldPkgInfo) {
        return oldPkgInfo;
    }

    public boolean isRemovableSysApp(String pkgName) {
        return false;
    }

    public boolean updateNativeLibDir(ApplicationInfo info, String codePath) {
        return false;
    }
}
