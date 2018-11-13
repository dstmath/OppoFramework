package com.android.server.secrecy.policy.util;

import android.util.Log;
import com.android.server.secrecy.SecrecyService;

public class LogUtil {
    private static boolean DEBUG = false;
    private static boolean ERROR = false;
    private static int FILE_LOG_LEVEL = 0;
    private static boolean INFO = false;
    private static final boolean IS_DEBUGING = SecrecyService.DEBUG;
    private static int LOGCAT_LEVEL = 0;
    static final int LOG_LEVEL_DEBUG = 2;
    static final int LOG_LEVEL_ERROR = 16;
    static final int LOG_LEVEL_INFO = 4;
    static final int LOG_LEVEL_VERBOSE = 0;
    static final int LOG_LEVEL_WARN = 8;
    private static final String LOG_TAG_STRING = "SecrecyService.LogUtil";
    private static boolean VERBOSE;
    private static boolean WARN;

    static {
        int i;
        boolean z;
        boolean z2 = true;
        if (IS_DEBUGING) {
            i = 2;
        } else {
            i = 16;
        }
        LOGCAT_LEVEL = i;
        if (IS_DEBUGING) {
            i = 2;
        } else {
            i = 16;
        }
        FILE_LOG_LEVEL = i;
        if (LOGCAT_LEVEL <= 0) {
            z = true;
        } else {
            z = false;
        }
        VERBOSE = z;
        if (LOGCAT_LEVEL <= 2) {
            z = true;
        } else {
            z = false;
        }
        DEBUG = z;
        if (LOGCAT_LEVEL <= 4) {
            z = true;
        } else {
            z = false;
        }
        INFO = z;
        if (LOGCAT_LEVEL <= 8) {
            z = true;
        } else {
            z = false;
        }
        WARN = z;
        if (LOGCAT_LEVEL > 16) {
            z2 = false;
        }
        ERROR = z2;
    }

    public static boolean isDebug() {
        return IS_DEBUGING;
    }

    public static void i(String tag, String msg) {
        if (INFO) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable error) {
        if (INFO) {
            Log.i(tag, msg, error);
        }
    }

    public static void v(String tag, String msg) {
        if (VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable error) {
        if (VERBOSE) {
            Log.v(tag, msg, error);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable error) {
        if (DEBUG) {
            Log.d(tag, msg, error);
        }
    }

    public static void w(String tag, String msg) {
        if (WARN) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable error) {
        if (WARN) {
            Log.w(tag, msg, error);
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable error) {
        if (ERROR) {
            Log.e(tag, msg, error);
        }
    }

    public static void wtf(String tag, String msg) {
        if (ERROR) {
            Log.wtf(tag, msg);
        }
    }

    public static void wtf(String tag, String msg, Throwable error) {
        if (ERROR) {
            Log.wtf(tag, msg, error);
        }
    }

    public static void dynamicallyConfigLog(boolean on) {
        VERBOSE = on;
        DEBUG = on;
        INFO = on;
        WARN = on;
        Log.d(LOG_TAG_STRING, "dynamicallyConfigLog ==> " + on);
    }

    public static String getLevelString() {
        return ((((("(" + " VERBOSE = " + VERBOSE) + ", DEBUG = " + DEBUG) + ", INFO = " + INFO) + ", WARN = " + WARN) + ", ERROR = " + ERROR) + " )";
    }
}
