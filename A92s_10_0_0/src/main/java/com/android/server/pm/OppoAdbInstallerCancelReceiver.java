package com.android.server.pm;

import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Slog;

public class OppoAdbInstallerCancelReceiver extends BroadcastReceiver {
    public static final String TAG = "OppoAdbInstallerCancelReceiver";

    public void onReceive(Context context, Intent intent) {
        String apkPath = intent.getStringExtra("apkPath");
        String packageName = intent.getStringExtra("packageName");
        Slog.d(TAG, "OPPO_ADB_INSTALL_CANCEL apkPath == " + apkPath + ",  packageName=" + packageName);
        OppoFeatureCache.get(IColorPackageInstallInterceptManager.DEFAULT).handForAdbSessionInstallerCancel(packageName);
    }
}
