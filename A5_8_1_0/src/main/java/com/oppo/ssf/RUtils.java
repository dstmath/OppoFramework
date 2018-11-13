package com.oppo.ssf;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.OppoActivityManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;

public class RUtils {
    private static final String SSF_SERVICE_NAME = "oppo.com.IRUtils2";
    private static String TAG = "ssf";
    private static IActivityManager mActivitymanager = null;
    private static OppoActivityManager mOppoAm = null;

    private static native int NativeRUtilsCmd(String str);

    static {
        System.loadLibrary("ssf");
    }

    private static boolean isSsfEnable() {
        if (SystemProperties.get("oppo.service.ssf.enable", "0").equals("1")) {
            return true;
        }
        return false;
    }

    private static void setSsfEnable() {
        if (mActivitymanager == null) {
            mActivitymanager = ActivityManagerNative.asInterface(ServiceManager.getService("activity"));
            mOppoAm = new OppoActivityManager();
        }
        if (mActivitymanager != null && mOppoAm != null) {
            try {
                mOppoAm.setSystemProperties("oppo.service.ssf.enable", "1");
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException happened! " + e);
            }
        }
    }

    private static boolean waitForSsfEnable() {
        for (int i = 0; i < 30; i++) {
            if (isSsfEnable()) {
                return true;
            }
            setSsfEnable();
            SystemClock.sleep(50);
        }
        Log.d(TAG, "waitForSsfEnable Failed!");
        return false;
    }

    public static int RUtilsCmd(String cmd) {
        if (!waitForSsfEnable()) {
            return -1;
        }
        int uid = Process.myUid();
        if (uid == 1000) {
            return NativeRUtilsCmd(cmd);
        }
        Log.d(TAG, uid + " Permission Denial!");
        return -1;
    }
}
