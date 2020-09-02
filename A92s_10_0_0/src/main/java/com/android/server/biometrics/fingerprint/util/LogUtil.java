package com.android.server.biometrics.fingerprint.util;

import android.util.Log;
import com.android.server.biometrics.fingerprint.FingerprintService;

public class LogUtil {
    private static final boolean IS_DEBUGING = FingerprintService.FINGER_DEBUG;
    static final int LOG_LEVEL_DEBUG = 2;
    static final int LOG_LEVEL_ERROR = 16;
    static final int LOG_LEVEL_INFO = 4;
    static final int LOG_LEVEL_VERBOSE = 0;
    static final int LOG_LEVEL_WARN = 8;
    private static final String LOG_TAG_STRING = "FingerprintServiceLogUtil";
    private static boolean sDEBUG = (sLogcatLevel <= 2);
    private static boolean sERROR;
    private static int sFileLogLevel = (IS_DEBUGING ? 2 : 16);
    private static boolean sINFO = (sLogcatLevel <= 4);
    private static int sLogcatLevel = (IS_DEBUGING ? 2 : 16);
    private static boolean sVERBOSE = (sLogcatLevel <= 0);
    private static boolean sWARN = (sLogcatLevel <= 8);

    static {
        boolean z = true;
        if (sLogcatLevel > 16) {
            z = false;
        }
        sERROR = z;
    }

    public static boolean isDebug() {
        return IS_DEBUGING;
    }

    public static void i(String tag, String msg) {
        if (sINFO) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable error) {
        if (sINFO) {
            Log.i(tag, msg, error);
        }
    }

    public static void v(String tag, String msg) {
        if (sVERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable error) {
        if (sVERBOSE) {
            Log.v(tag, msg, error);
        }
    }

    public static void d(String tag, String msg) {
        if (sDEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable error) {
        if (sDEBUG) {
            Log.d(tag, msg, error);
        }
    }

    public static void w(String tag, String msg) {
        if (sWARN) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable error) {
        if (sWARN) {
            Log.w(tag, msg, error);
        }
    }

    public static void e(String tag, String msg) {
        if (sERROR) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable error) {
        if (sERROR) {
            Log.e(tag, msg, error);
        }
    }

    public static void wtf(String tag, String msg) {
        if (sERROR) {
            Log.wtf(tag, msg);
        }
    }

    public static void wtf(String tag, String msg, Throwable error) {
        if (sERROR) {
            Log.wtf(tag, msg, error);
        }
    }

    public static void dynamicallyConfigLog(boolean on) {
        sVERBOSE = on;
        sDEBUG = on;
        sINFO = on;
        sWARN = on;
        Log.d(LOG_TAG_STRING, "dynamicallyConfigLog ==> " + on);
    }

    public static String getLevelString() {
        return ((((("(" + " sVERBOSE = " + sVERBOSE) + " ,sDEBUG = " + sDEBUG) + " ,sINFO = " + sINFO) + " ,sWARN = " + sWARN) + " ,sERROR = " + sERROR) + " )";
    }
}
