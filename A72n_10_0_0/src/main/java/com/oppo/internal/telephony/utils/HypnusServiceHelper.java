package com.oppo.internal.telephony.utils;

import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

public class HypnusServiceHelper {
    private static final String HYPNUS_SERVICE = "hypnus";
    private static final String TAG = "HypnusServiceHelper";
    private static Object mHypnusService = null;

    public static void hypnusServiceInit() {
        if (mHypnusService == null) {
            try {
                IBinder b = ServiceManager.getService(HYPNUS_SERVICE);
                if (b != null) {
                    mHypnusService = Class.forName("com.android.internal.app.IHypnusService$Stub").getMethod("asInterface", IBinder.class).invoke(null, b);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public static void hypnusSetAction(int action, int timeout) {
        Object obj = mHypnusService;
        if (obj != null) {
            try {
                obj.getClass().getDeclaredMethod("hypnusSetAction", Integer.TYPE, Integer.TYPE).invoke(mHypnusService, Integer.valueOf(action), Integer.valueOf(timeout));
                Log.v(TAG, "hypnusSetAction action:" + action + " timeout:" + timeout);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
