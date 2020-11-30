package com.color.inner.app;

import android.app.AppOpsManager;
import android.util.Log;

public class AppOpsManagerWrapper {
    public static final int OP_GET_USAGE_STATS = 43;
    public static final int OP_WRITE_SETTINGS = 23;

    public static void setMode(AppOpsManager appOpsManager, int code, int uid, String packageName, int mode) {
        appOpsManager.setMode(code, uid, packageName, mode);
    }

    public static void setUidMode(AppOpsManager appOpsManager, String appOp, int uid, int mode) {
        try {
            appOpsManager.setUidMode(appOp, uid, mode);
        } catch (Throwable e) {
            Log.e("AppOpsManagerWrapper", e.toString());
        }
    }
}
