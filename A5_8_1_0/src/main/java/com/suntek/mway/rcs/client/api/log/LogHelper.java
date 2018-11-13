package com.suntek.mway.rcs.client.api.log;

import android.util.Log;

public final class LogHelper {
    private static final String CLASS_METHOD_LINE_FORMAT = "%s.%s()  Line:%d";
    public static final String OPEN_DEBUG = "_SETTING_SERVICE_OPEN_DEBUG";
    private static String TAG = "RCS_Service_API";
    private static boolean isSensitiveLog = false;
    private static final int logLevel = 2;
    private static boolean mIsDebugMode = true;

    private static void log(String str, int level) {
        log(str, level, null);
    }

    private static void log(String str, int level, Throwable throwable) {
        if (mIsDebugMode && 2 <= level) {
            StackTraceElement traceElement;
            StackTraceElement[] array = Thread.currentThread().getStackTrace();
            if (array == null || array.length <= 5) {
                traceElement = array[array.length - 1];
            } else {
                traceElement = array[5];
            }
            String logText = String.format(CLASS_METHOD_LINE_FORMAT, new Object[]{traceElement.getClassName(), traceElement.getMethodName(), Integer.valueOf(traceElement.getLineNumber())});
            if (level == 2) {
                Log.v(TAG, logText + "->" + str);
            } else if (level == 3) {
                Log.d(TAG, logText + "->" + str);
            } else if (level == 4) {
                Log.i(TAG, logText + "->" + str);
            } else if (level == 5) {
                Log.w(TAG, logText + "->" + str);
            } else if (level != 6) {
            } else {
                if (throwable != null) {
                    Log.e(TAG, logText + "->" + str, throwable);
                } else {
                    Log.e(TAG, logText + "->" + str);
                }
            }
        }
    }

    public static void trace(String str) {
        log(str, 3);
    }

    public static void v(String str) {
        log(str, 2);
    }

    public static void d(String str) {
        log(str, 3);
    }

    public static void w(String str) {
        log(str, 5);
    }

    public static void i(String str) {
        log(str, 4);
    }

    public static void e(String str) {
        log(str, 6);
    }

    public static void e(String str, Throwable throwable) {
        log(str, 6, throwable);
    }

    public static String sensitive(String str) {
        if (isSensitiveLog) {
            return "*****";
        }
        return str;
    }

    public static void printStackTrace(Throwable throwable) {
        if (mIsDebugMode) {
            Log.w(TAG, "", throwable);
        }
    }
}
