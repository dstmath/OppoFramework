package com.oppo.rutils;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.OppoActivityManager;
import android.media.MediaDrm;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;

public class RUtils {
    private static String MD5_PROP = "oppo.service.md5daemon.enable";
    private static final int RETRY_TIMES = 30;
    private static final int SLEEP_TIME = 50;
    private static String TAG = "RUtils";
    private static IActivityManager sActivitymanager = null;
    private static OppoActivityManager sOppoAm = null;

    private static native int NativeGetPidByName(String str);

    private static native int NativeMd5Cmd(String str);

    private static native int NativeOppoRUtilsCompareSystemMD5();

    private static native int NativeOppoRutilsTestValue();

    private static native int NativeRUtilsChmod(String str, int i);

    private static native int NativeRUtilsCmd(String str);

    private static native int NativeRUtilsCmdForExternal(String str);

    private static native int NativeRUtilsModifyFileTime(String str, long j);

    private static native int NativeSetSystemProperties(String str, String str2);

    static {
        System.loadLibrary("rutils");
        Log.d(TAG, "Load RUtils!!!");
    }

    private static boolean isRUtilsEnable() {
        if (SystemProperties.get("oppo.service.rutils.enable", WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_ENABLE)) {
            Log.d(TAG, "isRUtilsEnable oppo.service.rutils.enable = 1!");
            return true;
        }
        Log.d(TAG, "isRUtilsEnable oppo.service.rutils.enable = 0!");
        return false;
    }

    private static void setRutilsEnable() {
        if (sActivitymanager == null) {
            Log.d(TAG, "mActivitymanager == null getService ACTIVITY_SERVICE!");
            sActivitymanager = ActivityManagerNative.asInterface(ServiceManager.getService("activity"));
            sOppoAm = new OppoActivityManager();
        }
        if (sActivitymanager != null && sOppoAm != null) {
            Log.d(TAG, "mActivitymanager != null setRutilsEnable!");
            try {
                sOppoAm.launchRutils();
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "RemoteException happened! " + e);
            }
        }
    }

    private static void increaseRutilsUsedCount() {
        OppoActivityManager oppoActivityManager;
        if (sActivitymanager == null) {
            Log.d(TAG, "mActivitymanager == null getService ACTIVITY_SERVICE!");
            sActivitymanager = ActivityManagerNative.asInterface(ServiceManager.getService("activity"));
        }
        if (sOppoAm == null) {
            sOppoAm = new OppoActivityManager();
        }
        if (sActivitymanager != null && (oppoActivityManager = sOppoAm) != null) {
            try {
                oppoActivityManager.increaseRutilsUsedCount();
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "RemoteException happened! " + e);
            }
        }
    }

    private static void decreaseRutilsUsedCount() {
        OppoActivityManager oppoActivityManager;
        if (sActivitymanager == null) {
            Log.d(TAG, "mActivitymanager == null getService ACTIVITY_SERVICE!");
            sActivitymanager = ActivityManagerNative.asInterface(ServiceManager.getService("activity"));
        }
        if (sOppoAm == null) {
            sOppoAm = new OppoActivityManager();
        }
        if (sActivitymanager != null && (oppoActivityManager = sOppoAm) != null) {
            try {
                oppoActivityManager.decreaseRutilsUsedCount();
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "RemoteException happened! " + e);
            }
        }
    }

    private static boolean waitForRUtilsEnable() {
        for (int i = 0; i < 30; i++) {
            if (isRUtilsEnable()) {
                Log.d(TAG, "waitForRUtilsEnable OK!");
                return true;
            }
            String str = TAG;
            Log.d(str, "setRutilsEnable times == " + i);
            setRutilsEnable();
            SystemClock.sleep(50);
        }
        Log.d(TAG, "waitForRUtilsEnable Failed!");
        return false;
    }

    public static int RUtilsChmod(String path, int mod) {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        } else if (Process.myUid() == 1000 || Process.myUid() == 1001) {
            int result = NativeRUtilsChmod(path, mod);
            decreaseRutilsUsedCount();
            return result;
        } else {
            String str = TAG;
            Log.d(str, "uid = " + Process.myUid() + " Permission Denied ~");
            decreaseRutilsUsedCount();
            return -1;
        }
    }

    public static int RUtilsCmd(String cmd) {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        }
        int result = NativeRUtilsCmd(cmd);
        decreaseRutilsUsedCount();
        return result;
    }

    public static int RUtilsGetPidByName(String processName) {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        }
        int result = NativeGetPidByName(processName);
        decreaseRutilsUsedCount();
        return result;
    }

    public static int RUtilsSetSystemPropertiesStringInt(String key, int value) {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        } else if (key == null) {
            decreaseRutilsUsedCount();
            return -1;
        } else {
            int result = NativeSetSystemProperties(key, String.valueOf(value));
            decreaseRutilsUsedCount();
            return result;
        }
    }

    public static int RUtilsSetSystemPropertiesString(String key, String value) {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        } else if (key == null || value == null) {
            decreaseRutilsUsedCount();
            return -1;
        } else {
            int result = NativeSetSystemProperties(key, value);
            decreaseRutilsUsedCount();
            return result;
        }
    }

    public static int oppoRutilsTestValue() {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        }
        Log.e(TAG, "RUtils oppoRutilsTestValue enter!!!");
        int result = NativeOppoRutilsTestValue();
        decreaseRutilsUsedCount();
        return result;
    }

    public static int OppoRUtilsCompareSystemMD5() {
        return 0;
    }

    public static void OppoRUtilsPartitionCheck2() {
        if (SystemProperties.get(MD5_PROP, WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
            SystemProperties.set(MD5_PROP, WifiEnterpriseConfig.ENGINE_ENABLE);
        }
        Log.e(TAG, "RUtils OppoRUtilsPartitionCheck2 enter!!!");
        NativeMd5Cmd("system");
        NativeMd5Cmd(MediaDrm.PROPERTY_VENDOR);
        SystemProperties.set(MD5_PROP, WifiEnterpriseConfig.ENGINE_DISABLE);
    }

    public static int RUtilsModifyFileTime(String path, long time) {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        }
        int result = NativeRUtilsModifyFileTime(path, time);
        decreaseRutilsUsedCount();
        return result;
    }
}
