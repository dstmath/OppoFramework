package com.android.server.biometrics.fingerprint.util;

import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.util.Log;
import java.lang.reflect.Method;

public class SecrecyServiceHelper {
    public static final int ADB_TYPE = 4;
    public static final int ALL_TYPE = 255;
    public static final int APP_TYPE = 2;
    private static final boolean DEBUG = false;
    public static final int LOG_TYPE = 1;
    private static final String SECRECY_SERVICE = "secrecy";
    private static final String SECRECY_SUPPORT_FEATURE = "oppo.secrecy.support";
    private static final String TAG = "SecrecyServiceHelper";
    private static Object mSecrecyService = null;

    private static void secrecyServiceInit() {
        if (mSecrecyService == null) {
            try {
                IBinder b = (IBinder) Class.forName("android.os.ServiceManager").getMethod("checkService", String.class).invoke(null, SECRECY_SERVICE);
                if (b != null) {
                    Method m = Class.forName("android.secrecy.ISecrecyService$Stub").getMethod("asInterface", IBinder.class);
                    Log.v(TAG, "getMethod as interface");
                    mSecrecyService = m.invoke(null, b);
                }
            } catch (Exception e) {
                Log.w(TAG, "secrecyServiceInit failed Exception", e);
            }
        }
    }

    public static boolean isSecrecySupported() {
        secrecyServiceInit();
        Object obj = mSecrecyService;
        if (obj == null) {
            return false;
        }
        try {
            Boolean result = (Boolean) obj.getClass().getDeclaredMethod("isSecrecySupport", new Class[0]).invoke(mSecrecyService, new Object[0]);
            if (result != null) {
                return result.booleanValue();
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "isSecrecySupported failed Exception", e);
            return false;
        }
    }

    public static boolean getAdbDecryptStatus(int type) {
        secrecyServiceInit();
        Object obj = mSecrecyService;
        if (obj == null) {
            return true;
        }
        try {
            Boolean result = (Boolean) obj.getClass().getDeclaredMethod("getSecrecyState", Integer.TYPE).invoke(mSecrecyService, Integer.valueOf(type));
            if (result != null) {
                return result.booleanValue();
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "getAdbDecryptStatus failed Exception", e);
            return true;
        }
    }

    public static boolean isInEncryptedAppList(ActivityInfo info, String callingPackage, int callingUid, int callingPid) {
        secrecyServiceInit();
        Object obj = mSecrecyService;
        if (obj == null) {
            return false;
        }
        try {
            Boolean result = (Boolean) obj.getClass().getDeclaredMethod("isInEncryptedAppList", ActivityInfo.class, String.class, Integer.TYPE, Integer.TYPE).invoke(mSecrecyService, info, callingPackage, Integer.valueOf(callingUid), Integer.valueOf(callingPid));
            if (result != null) {
                return result.booleanValue();
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "isInEncryptedAppList failed Exception", e);
            return false;
        }
    }

    public static boolean isKeyImported() {
        secrecyServiceInit();
        Object obj = mSecrecyService;
        if (obj == null) {
            return false;
        }
        try {
            Boolean result = (Boolean) obj.getClass().getDeclaredMethod("isKeyImported", new Class[0]).invoke(mSecrecyService, new Object[0]);
            if (result != null) {
                return result.booleanValue();
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "isKeyImported failed Exception", e);
            return false;
        }
    }
}
