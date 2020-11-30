package com.color.inner.os;

import android.os.SystemProperties;
import android.util.Log;

public class SystemPropertiesWrapper {
    private static final String TAG = "SystemPropertiesWrapper";

    private SystemPropertiesWrapper() {
    }

    public static String get(String key) {
        try {
            return SystemProperties.get(key);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static String get(String key, String def) {
        try {
            return SystemProperties.get(key, def);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return def;
        }
    }

    public static int getInt(String key, int def) {
        try {
            return SystemProperties.getInt(key, def);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return def;
        }
    }

    public static long getLong(String key, long def) {
        try {
            return SystemProperties.getLong(key, def);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return def;
        }
    }

    public static boolean getBoolean(String key, boolean def) {
        try {
            return SystemProperties.getBoolean(key, def);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return def;
        }
    }

    public static void set(String key, String val) {
        try {
            SystemProperties.set(key, val);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}
