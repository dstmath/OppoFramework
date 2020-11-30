package com.android.server.connectivity.util;

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper {
    private static final String TAG = "ReflectionHelper";

    public static Object callMethod(Object target, String clsName, String methodName, Class[] parameterTypes, Object[] args) {
        try {
            Method method = Class.forName(clsName).getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            Log.i(TAG, "callDeclaredMethod exception caught : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Object getDeclaredField(Object target, String clsName, String fieldName) {
        try {
            Field field = Class.forName(clsName).getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            Log.i(TAG, "getDeclaredField exception caught : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void setDeclaredField(Object target, String clsName, String fieldName, Object value) {
        try {
            Field field = Class.forName(clsName).getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
