package com.android.server.coloros;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OppoFloatWindowListManager {
    private static final int INIT_BUILDINS_PERMISSION = 1;
    private static final String PROPERTY_FIRST_BOOT = "oppo.device.firstboot";
    private static final String TAG = "OppoFloatWindowListManager";
    private static final List<String> mDefaultGrantBuildinApps = Arrays.asList(new String[]{"com.coloros.screenrecorder", "com.oppo.community", "com.oppo.reader"});
    private Context mContext;
    private Handler mFloatWindowManagerHandler;
    private HandlerThread mHandlerThread = new HandlerThread(TAG, 10);

    public OppoFloatWindowListManager(Context context) {
        this.mContext = context;
        initialize();
    }

    private void initialize() {
        this.mHandlerThread.start();
        this.mFloatWindowManagerHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (OppoFloatWindowListManager.this.isFirstBoot()) {
                            Log.d(OppoFloatWindowListManager.TAG, "Receive msg INIT_BUILDINS_PERMISSION. First Boot.");
                            for (String packageName : OppoFloatWindowListManager.mDefaultGrantBuildinApps) {
                                Log.d(OppoFloatWindowListManager.TAG, "mDefaultGrantBuildinApps pkg:" + packageName);
                                OppoFloatWindowListManager.this.grantFloatWindowPermission(packageName);
                            }
                            return;
                        }
                        Log.d(OppoFloatWindowListManager.TAG, "Receive msg INIT_BUILDINS_PERMISSION. Not First Boot.");
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void initBuildInAppsFloatWindowPermission() {
        Log.d(TAG, "initBuildInAppsFloatWindowPermission");
        Message msg = this.mFloatWindowManagerHandler.obtainMessage();
        msg.what = 1;
        this.mFloatWindowManagerHandler.sendMessage(msg);
    }

    private boolean isFirstBoot() {
        if (SystemProperties.getInt(PROPERTY_FIRST_BOOT, 0) == 1) {
            return true;
        }
        return false;
    }

    private void grantFloatWindowPermission(String packageName) {
        Log.d(TAG, "grantFloatWindowPermission pkg:" + packageName);
        AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService("appops");
        if (appOps == null) {
            Log.d(TAG, "AppOpsManager is null");
            return;
        }
        ApplicationInfo appInfo = null;
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(packageName, 4096);
            if (packageInfo != null) {
                appInfo = packageInfo.applicationInfo;
            } else {
                Log.e(TAG, "grantFloatWindowPermission exception: packageInfo is null.");
            }
            if (appInfo != null) {
                appOps.setMode(24, appInfo.uid, packageName, 0);
            } else {
                Log.e(TAG, "grantFloatWindowPermission exception: appInfo is null.");
            }
        } catch (Exception e) {
            Log.e(TAG, "grantFloatWindowPermission exception: " + e);
        }
    }

    public ArrayList<String> getDefaultGrantBuildinApps() {
        ArrayList<String> list = new ArrayList();
        list.addAll(mDefaultGrantBuildinApps);
        return list;
    }
}
