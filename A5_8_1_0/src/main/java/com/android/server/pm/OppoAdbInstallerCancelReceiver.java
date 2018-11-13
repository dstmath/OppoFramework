package com.android.server.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OppoAdbInstallerCancelReceiver extends BroadcastReceiver {
    public static final String TAG = "OppoAdbInstallerManager";

    public void onReceive(Context context, Intent intent) {
        String apkPath = intent.getStringExtra("apkPath");
        String packageName = intent.getStringExtra("packageName");
        Log.d("OppoAdbInstallerManager", "OPPO_ADB_INSTALL_CANCEL apkPath == " + apkPath + ",  packageName=" + packageName);
        OppoAdbInstallerManager.getInstance().handForAdbSessionInstallerCancel(packageName);
    }
}
