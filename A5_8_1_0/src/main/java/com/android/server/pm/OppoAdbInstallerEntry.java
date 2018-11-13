package com.android.server.pm;

import android.content.pm.IPackageInstallObserver2;

public class OppoAdbInstallerEntry {
    private static final String TAG = "OppoAdbInstallerManager";
    public String mApkPath = "";
    public IPackageInstallObserver2 mObserver = null;
    public String mPackageName = "";

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
