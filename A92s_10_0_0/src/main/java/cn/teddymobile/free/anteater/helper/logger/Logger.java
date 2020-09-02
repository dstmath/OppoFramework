package cn.teddymobile.free.anteater.helper.logger;

import android.os.SystemProperties;
import android.util.Log;

public class Logger {
    public static final int D = 2;
    public static final int E = 4;
    public static final int I = 1;
    private static final boolean LOG_DEBUG = SystemProperties.getBoolean("log.favorite.debug", false);
    private static final int LOG_LEVEL = 1;
    private static final boolean LOG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean UNIFIED = true;
    private static final String UNIFIED_TAG = "AnteaterHelper";
    public static final int V = 0;
    public static final int W = 3;

    public static void v(String tag, String message) {
        doLog(0, tag, message);
    }

    public static void i(String tag, String message) {
        doLog(1, tag, message);
    }

    public static void d(String tag, String message) {
        doLog(2, tag, message);
    }

    public static void w(String tag, String message) {
        doLog(3, tag, message);
    }

    public static void e(String tag, String message) {
        doLog(4, tag, message);
    }

    public static void v(String tag, String message, Throwable throwable) {
        doLog(0, tag, message, throwable);
    }

    public static void i(String tag, String message, Throwable throwable) {
        doLog(1, tag, message, throwable);
    }

    public static void d(String tag, String message, Throwable throwable) {
        doLog(2, tag, message, throwable);
    }

    public static void w(String tag, String message, Throwable throwable) {
        doLog(3, tag, message, throwable);
    }

    public static void e(String tag, String message, Throwable throwable) {
        doLog(4, tag, message, throwable);
    }

    private static boolean isLogOn(int level) {
        if ((LOG_PANIC || LOG_DEBUG) && level >= 1) {
            return true;
        }
        return false;
    }

    private static void doLog(int level, String tag, String message) {
        if (isLogOn(level)) {
            String message2 = "[" + tag + "] " + message;
            if (level == 0) {
                Log.v(UNIFIED_TAG, message2);
            } else if (level == 1) {
                Log.i(UNIFIED_TAG, message2);
            } else if (level == 2) {
                Log.d(UNIFIED_TAG, message2);
            } else if (level == 3) {
                Log.w(UNIFIED_TAG, message2);
            } else if (level == 4) {
                Log.e(UNIFIED_TAG, message2);
            }
        }
    }

    private static void doLog(int level, String tag, String message, Throwable throwable) {
        if (isLogOn(level)) {
            String message2 = "[" + tag + "] " + message;
            if (level == 0) {
                Log.v(UNIFIED_TAG, message2, throwable);
            } else if (level == 1) {
                Log.i(UNIFIED_TAG, message2, throwable);
            } else if (level == 2) {
                Log.d(UNIFIED_TAG, message2, throwable);
            } else if (level == 3) {
                Log.w(UNIFIED_TAG, message2, throwable);
            } else if (level == 4) {
                Log.e(UNIFIED_TAG, message2, throwable);
            }
        }
    }
}
