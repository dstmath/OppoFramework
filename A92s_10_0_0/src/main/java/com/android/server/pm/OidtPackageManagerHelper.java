package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.os.OppoManager;
import android.util.ArrayMap;
import com.android.server.pm.PackageManagerService;
import java.util.HashMap;
import java.util.Map;

public class OidtPackageManagerHelper {
    private static final String TAG = "OidtPackageManagerHelper";
    private static final Object mLock = new Object();
    private static OidtPackageManagerHelper sInstance = null;

    public static OidtPackageManagerHelper getInstance() {
        OidtPackageManagerHelper oidtPackageManagerHelper;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new OidtPackageManagerHelper();
            }
            oidtPackageManagerHelper = sInstance;
        }
        return oidtPackageManagerHelper;
    }

    public void detectInstallForOIDT(Context context, PackageManagerService.PackageInstalledInfo res, PackageManagerService.InstallArgs args) {
        if (res.pkg != null && args != null && (res.pkg.applicationInfo.flags & 1) == 0) {
            Map<String, String> logMap = new HashMap<>();
            logMap.put("packageName", res.pkg.packageName);
            logMap.put("installerPackageName", args.installerPackageName);
            logMap.put("versionName", res.pkg.mVersionName);
            logMap.put("packageLabel", String.valueOf(context.getPackageManager().getApplicationLabel(res.pkg.applicationInfo)));
            OppoManager.onStamp("020203", logMap);
        }
    }

    public void detectUninstallForOIDT(Context context, ArrayMap<String, PackageParser.Package> mPackages, String packageName) {
        PackageParser.Package uninstallPkg;
        synchronized (mPackages) {
            uninstallPkg = mPackages.get(packageName);
        }
        if (uninstallPkg != null && (uninstallPkg.applicationInfo.flags & 1) == 0) {
            Map<String, String> logMap = new HashMap<>();
            PackageManager pm = context.getPackageManager();
            logMap.put("packageName", uninstallPkg.packageName);
            logMap.put("installerPackageName", pm.getInstallerPackageName(uninstallPkg.packageName));
            logMap.put("versionName", uninstallPkg.mVersionName);
            logMap.put("packageLabel", String.valueOf(pm.getApplicationLabel(uninstallPkg.applicationInfo)));
            OppoManager.onStamp("020204", logMap);
        }
    }
}
