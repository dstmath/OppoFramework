package com.oppo.internal.telephony.utils;

import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.util.Log;
import java.lang.reflect.Method;

public class ConnectivityManagerHelper {
    private static final String CLASS_NAME = "android.net.ConnectivityManager";
    private static final String TAG = "ConnectivityManagerHelper";

    public static NetworkRequest getCelluarNetworkRequest(ConnectivityManager cm) {
        try {
            Method method = Class.forName(CLASS_NAME).getMethod("getCelluarNetworkRequest", new Class[0]);
            method.setAccessible(true);
            return (NetworkRequest) method.invoke(cm, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, "ClassNotFoundException" + e.getMessage());
            return null;
        }
    }

    public static boolean shouldKeepCelluarNetwork(ConnectivityManager cm, boolean keep) {
        try {
            Method method = Class.forName(CLASS_NAME).getMethod("shouldKeepCelluarNetwork", Boolean.TYPE);
            method.setAccessible(true);
            return ((Boolean) method.invoke(cm, Boolean.valueOf(keep))).booleanValue();
        } catch (Exception e) {
            Log.e(TAG, "ClassNotFoundException" + e.getMessage());
            return false;
        }
    }

    public static boolean measureDataState(ConnectivityManager cm, int siganlLevel) {
        try {
            Method method = Class.forName(CLASS_NAME).getMethod("measureDataState", Integer.TYPE);
            method.setAccessible(true);
            return ((Boolean) method.invoke(cm, Integer.valueOf(siganlLevel))).booleanValue();
        } catch (Exception e) {
            Log.e(TAG, "ClassNotFoundException" + e.getMessage());
            return true;
        }
    }

    public static boolean hasCache(ConnectivityManager cm) {
        try {
            Method method = Class.forName(CLASS_NAME).getMethod("hasCache", new Class[0]);
            method.setAccessible(true);
            return ((Boolean) method.invoke(cm, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.e(TAG, "ClassNotFoundException" + e.getMessage());
            return false;
        }
    }

    public static long getCurrentTimeMillis(ConnectivityManager cm) {
        try {
            Method method = Class.forName(CLASS_NAME).getMethod("getCurrentTimeMillis", new Class[0]);
            method.setAccessible(true);
            return ((Long) method.invoke(cm, new Object[0])).longValue();
        } catch (Exception e) {
            Log.e(TAG, "ClassNotFoundException" + e.getMessage());
            return Long.MAX_VALUE;
        }
    }

    public static void setTelephonyPowerState(ConnectivityManager cm, String powerState) {
        try {
            Method method = Class.forName(CLASS_NAME).getMethod("setTelephonyPowerState", String.class);
            method.setAccessible(true);
            method.invoke(cm, powerState);
        } catch (Exception e) {
            Log.e(TAG, "ClassNotFoundException" + e.getMessage());
        }
    }

    public static void setAlreadyUpdated(ConnectivityManager cm, boolean alreadyUpdated) {
        try {
            Method method = Class.forName(CLASS_NAME).getMethod("setAlreadyUpdated", Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(cm, Boolean.valueOf(alreadyUpdated));
        } catch (Exception e) {
            Log.e(TAG, "ClassNotFoundException" + e.getMessage());
        }
    }

    public static void setTelephonyPowerLost(ConnectivityManager cm, double powerLost) {
        try {
            Method method = Class.forName(CLASS_NAME).getMethod("setTelephonyPowerLost", Double.TYPE);
            method.setAccessible(true);
            method.invoke(cm, Double.valueOf(powerLost));
        } catch (Exception e) {
            Log.e(TAG, "ClassNotFoundException" + e.getMessage());
        }
    }
}
