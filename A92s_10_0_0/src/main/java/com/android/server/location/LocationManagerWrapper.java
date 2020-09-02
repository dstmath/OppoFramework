package com.android.server.location;

import android.location.LocationManager;
import android.location.OppoMirrorLocationManager;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class LocationManagerWrapper {
    private static final String TAG = "LocationManagerWrapper";

    public static void getLocAppsOp(LocationManager locationManager, int flag, LocAppsOpWrapper locAppsOp) {
        if (locationManager != null) {
            try {
                OppoMirrorLocationManager.getLocAppsOp.call(locationManager, new Object[]{Integer.valueOf(flag), locAppsOp.getLocAppsOp()});
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }
    }

    public static void setLocAppsOp(LocationManager locationManager, int cmd, LocAppsOpWrapper locAppsOp) {
        if (locationManager != null) {
            try {
                OppoMirrorLocationManager.setLocAppsOp.call(locationManager, new Object[]{Integer.valueOf(cmd), locAppsOp.getLocAppsOp()});
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }
    }

    public static List<String> getInUsePackagesList(LocationManager locationManager) {
        return callMethodByReflect(locationManager, "getInUsePackagesList");
    }

    private static List<String> callMethodByReflect(LocationManager locationManager, String methodName) {
        try {
            Method method = LocationManager.class.getDeclaredMethod(methodName, new Class[0]);
            method.setAccessible(true);
            return (List) method.invoke(locationManager, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }
}
