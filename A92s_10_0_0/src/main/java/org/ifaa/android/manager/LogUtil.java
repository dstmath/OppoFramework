package org.ifaa.android.manager;

import android.os.SystemProperties;
import android.util.Log;

public class LogUtil {
    private static final boolean IS_DEBUGING = SystemProperties.getBoolean("persist.sys.assert.panic", (boolean) IS_DEBUGING);
    static final int LOG_LEVEL_DEBUG = 2;
    static final int LOG_LEVEL_ERROR = 16;
    static final int LOG_LEVEL_INFO = 4;
    static final int LOG_LEVEL_VERBOSE = 0;
    static final int LOG_LEVEL_WARN = 8;
    private static final String TAG_PREFIX = "IFAA.";
    private static boolean sDEBUG = (sLogcatLevel <= 2);
    private static boolean sERROR;
    private static boolean sINFO = (sLogcatLevel <= LOG_LEVEL_INFO);
    private static int sLogcatLevel = (IS_DEBUGING ? 2 : LOG_LEVEL_ERROR);
    private static boolean sVERBOSE = (sLogcatLevel <= 0);
    private static boolean sWARN = (sLogcatLevel <= LOG_LEVEL_WARN);

    static {
        boolean z = IS_DEBUGING;
        if (sLogcatLevel <= LOG_LEVEL_ERROR) {
            z = true;
        }
        sERROR = z;
    }

    public static boolean isDebug() {
        return IS_DEBUGING;
    }

    public static void i(String tag, String msg) {
        if (sINFO) {
            Log.i(TAG_PREFIX + tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable error) {
        if (sINFO) {
            Log.i(TAG_PREFIX + tag, msg, error);
        }
    }

    public static void v(String tag, String msg) {
        if (sVERBOSE) {
            Log.v(TAG_PREFIX + tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable error) {
        if (sVERBOSE) {
            Log.v(TAG_PREFIX + tag, msg, error);
        }
    }

    public static void d(String tag, String msg) {
        if (sDEBUG) {
            Log.d(TAG_PREFIX + tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable error) {
        if (sDEBUG) {
            Log.d(TAG_PREFIX + tag, msg, error);
        }
    }

    public static void w(String tag, String msg) {
        if (sWARN) {
            Log.w(TAG_PREFIX + tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable error) {
        if (sWARN) {
            Log.w(TAG_PREFIX + tag, msg, error);
        }
    }

    public static void e(String tag, String msg) {
        if (sERROR) {
            Log.e(TAG_PREFIX + tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable error) {
        if (sERROR) {
            Log.e(TAG_PREFIX + tag, msg, error);
        }
    }

    public static void wtf(String tag, String msg) {
        if (sERROR) {
            Log.wtf(TAG_PREFIX + tag, msg);
        }
    }

    public static void wtf(String tag, String msg, Throwable error) {
        if (sERROR) {
            Log.wtf(TAG_PREFIX + tag, msg, error);
        }
    }

    public static String getLevelString() {
        return ((((("(" + " sVERBOSE = " + sVERBOSE) + " ,sDEBUG = " + sDEBUG) + " ,sINFO = " + sINFO) + " ,sWARN = " + sWARN) + " ,sERROR = " + sERROR) + " )";
    }
}
