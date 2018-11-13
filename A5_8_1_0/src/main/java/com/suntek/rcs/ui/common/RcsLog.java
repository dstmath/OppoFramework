package com.suntek.rcs.ui.common;

import android.os.SystemProperties;
import android.util.Log;

public class RcsLog {
    private static final int DEFAULT_LOG_LEVEL = 1;
    private static final int LOG_LEVEL_DEBUG = 2;
    private static final int LOG_LEVEL_ERROR = 5;
    private static final int LOG_LEVEL_INFO = 3;
    private static final int LOG_LEVEL_NONE = 0;
    private static final String LOG_LEVEL_PROPERTIES = "persist.sys.rcs.log.level";
    private static final int LOG_LEVEL_VERBOSE = 1;
    private static final int LOG_LEVEL_WARNNING = 4;
    private static String sTag = "RCS_UI";

    public static void v(String msg) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 1) {
            Log.v(sTag, msg);
        }
    }

    public static void d(String msg) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 2) {
            Log.d(sTag, msg);
        }
    }

    public static void i(String msg) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 3) {
            Log.i(sTag, msg);
        }
    }

    public static void w(String msg) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 4) {
            Log.w(sTag, msg);
        }
    }

    public static void w(Throwable tr) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 4) {
            Log.w(sTag, tr);
        }
    }

    public static void w(String msg, Throwable tr) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 4) {
            Log.w(sTag, msg, tr);
        }
    }

    public static void e(String msg) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 5) {
            Log.e(sTag, msg);
        }
    }

    public static void e(Throwable tr) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 5) {
            Log.e(sTag, "", tr);
        }
    }

    public static void e(String msg, Throwable tr) {
        int logLevel = getLogLevel();
        if (logLevel > 0 && logLevel <= 5) {
            Log.e(sTag, msg, tr);
        }
    }

    private static int getLogLevel() {
        return SystemProperties.getInt(LOG_LEVEL_PROPERTIES, 1);
    }

    public static void setTag(String tag) {
        sTag = tag;
    }
}
