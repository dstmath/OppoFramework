package com.android.server.pm;

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommonSoftReflectionHelper {
    private static final String TAG = "CommonSoftReflectionHelper";

    public static boolean oppoTelephonyFunction_colorIsSimLockedEnabledTH() {
        Object isSimLockedEnabledTH = callDeclaredMethod(null, "android.telephony.OppoTelephonyFunction", "colorIsSimLockedEnabledTH", null, null);
        Log.i(TAG, "oppoTelephonyFunction_colorIsSimLockedEnabledTH = " + isSimLockedEnabledTH);
        if (isSimLockedEnabledTH == null) {
            return false;
        }
        return ((Boolean) isSimLockedEnabledTH).booleanValue();
    }

    public static boolean oppoTelephonyFunction_colorIsSimLockedEnabled() {
        Object isSimLockedEnabled = callDeclaredMethod(null, "android.telephony.OppoTelephonyFunction", "colorIsSimLockedEnabled", null, null);
        Log.i(TAG, "oppoTelephonyFunction_colorIsSimLockedEnabled = " + isSimLockedEnabled);
        if (isSimLockedEnabled == null) {
            return false;
        }
        return ((Boolean) isSimLockedEnabled).booleanValue();
    }

    public static byte[] oppoEngineerNative_getDownloadStatus() {
        Object downloadStatus = callDeclaredMethod(null, "com.android.server.engineer.OppoEngineerNative", "native_getDownloadStatus", null, null);
        Log.i(TAG, "oppoEngineergetDownloadStatus = " + downloadStatus);
        if (downloadStatus == null) {
            return null;
        }
        return (byte[]) downloadStatus;
    }

    public static Object callDeclaredMethod(Object target, String cls_name, String method_name, Class[] parameterTypes, Object[] args) {
        Log.i(TAG, target + " callDeclaredMethod : " + cls_name + "." + method_name);
        try {
            Method method = Class.forName(cls_name).getDeclaredMethod(method_name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "ClassNotFoundException : " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            Log.i(TAG, "NoSuchMethodException : " + e2.getMessage());
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return null;
        } catch (SecurityException e5) {
            e5.printStackTrace();
            return null;
        }
    }

    public static Object getDeclaredField(Object target, String cls_name, String field_name) {
        Log.i(TAG, target + " getDeclaredField : " + cls_name + "." + field_name);
        try {
            Field field = Class.forName(cls_name).getDeclaredField(field_name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "ClassNotFoundException : " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (NoSuchFieldException e2) {
            Log.i(TAG, "NoSuchFieldException : " + e2.getMessage());
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (SecurityException e4) {
            e4.printStackTrace();
            return null;
        }
    }
}
