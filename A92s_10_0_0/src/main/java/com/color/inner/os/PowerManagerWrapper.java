package com.color.inner.os;

import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.reflect.Method;

public class PowerManagerWrapper {
    private static final String TAG = "PowerManagerWrapper";

    private PowerManagerWrapper() {
    }

    public static int getMaximumScreenBrightnessSetting(PowerManager powerManager) {
        try {
            return powerManager.getMaximumScreenBrightnessSetting();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int getMinimumScreenBrightnessSetting(PowerManager powerManager) {
        try {
            return powerManager.getMinimumScreenBrightnessSetting();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void wakeUp(PowerManager powerManager, long time, String details) {
        try {
            powerManager.wakeUp(time, details);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void goToSleep(PowerManager powerManager, long time) {
        try {
            powerManager.goToSleep(time);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static boolean getDisplayAodStatus(PowerManager powerManager) {
        return ((Boolean) callMethodByReflect(powerManager, "getDisplayAodStatus")).booleanValue();
    }

    public static int getRealMaximumScreenBrightnessSetting() {
        try {
            return ((Integer) callMethodByReflect(IPowerManager.Stub.asInterface(ServiceManager.getService("power")), "getMaximumScreenBrightnessSetting")).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 255;
        }
    }

    public static int getRealMinimumScreenBrightnessSetting() {
        try {
            return ((Integer) callMethodByReflect(IPowerManager.Stub.asInterface(ServiceManager.getService("power")), "getMinimumScreenBrightnessSetting")).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 255;
        }
    }

    public static void shutdown(PowerManager powerManager, boolean confirm, String reason, boolean wait) {
        try {
            powerManager.shutdown(confirm, reason, wait);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static int[] getWakeLockedUids(PowerManager powerManager) {
        return (int[]) callMethodByReflect(powerManager, "getWakeLockedUids");
    }

    private static Object callMethodByReflect(Object object, String methodName) {
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, new Class[0]);
            method.setAccessible(true);
            return method.invoke(object, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
