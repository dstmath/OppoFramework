package com.android.server.face.utils;

import android.util.Log;
import com.android.server.face.FaceService;

public class LogUtil {
    static final int LOG_LEVEL_DEBUG = 2;
    static final int LOG_LEVEL_ERROR = 16;
    static final int LOG_LEVEL_INFO = 4;
    static final int LOG_LEVEL_VERBOSE = 0;
    static final int LOG_LEVEL_WARN = 8;
    private static final String LOG_TAG_STRING = "FaceService.LogUtil";
    private static boolean sDEBUG;
    private static boolean sERROR;
    private static int sFileLogLevel;
    private static boolean sINFO;
    public static boolean sIsDebuging = FaceService.DEBUG;
    private static int sLogcatLevel = (sIsDebuging ? 2 : 16);
    private static boolean sVERBOSE;
    private static boolean sWARN;

    static {
        int i;
        boolean z;
        boolean z2 = true;
        if (sIsDebuging) {
            i = 2;
        } else {
            i = 16;
        }
        sFileLogLevel = i;
        if (sLogcatLevel <= 0) {
            z = true;
        } else {
            z = false;
        }
        sVERBOSE = z;
        if (sLogcatLevel <= 2) {
            z = true;
        } else {
            z = false;
        }
        sDEBUG = z;
        if (sLogcatLevel <= 4) {
            z = true;
        } else {
            z = false;
        }
        sINFO = z;
        if (sLogcatLevel <= 8) {
            z = true;
        } else {
            z = false;
        }
        sWARN = z;
        if (sLogcatLevel > 16) {
            z2 = false;
        }
        sERROR = z2;
    }

    public static boolean isDebug() {
        return sIsDebuging;
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
    }

    public static String getLevelString() {
        return ((((("(" + " sVERBOSE = " + sVERBOSE) + " ,sDEBUG = " + sDEBUG) + " ,sINFO = " + sINFO) + " ,sWARN = " + sWARN) + " ,sERROR = " + sERROR) + " )";
    }
}
