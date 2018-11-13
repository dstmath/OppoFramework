package com.android.server.pm;

import android.content.pm.IPackageInstallObserver2;
import com.android.server.oppo.IElsaManager;

public class OppoAdbInstallerEntry {
    private static final String TAG = "OppoAdbInstallerManager";
    public String mApkPath = IElsaManager.EMPTY_PACKAGE;
    public IPackageInstallObserver2 mObserver = null;
    public String mPackageName = IElsaManager.EMPTY_PACKAGE;

    private OppoAdbInstallerEntry() {
    }

    public static OppoAdbInstallerEntry Builder(String apkPath, IPackageInstallObserver2 obs) {
        OppoAdbInstallerEntry oaie = new OppoAdbInstallerEntry();
        oaie.mApkPath = apkPath;
        oaie.mObserver = obs;
        return oaie;
    }

    public static OppoAdbInstallerEntry Builder(String apkPath, IPackageInstallObserver2 obs, String packageName) {
        OppoAdbInstallerEntry oaie = new OppoAdbInstallerEntry();
        oaie.mApkPath = apkPath;
        oaie.mObserver = obs;
        oaie.mPackageName = packageName;
        return oaie;
    }
}
