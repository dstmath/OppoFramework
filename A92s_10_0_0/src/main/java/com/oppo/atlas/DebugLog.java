package com.oppo.atlas;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.util.Log;

public final class DebugLog {
    private static final boolean DEBUG_MODE = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean LOGD = true;
    private static final boolean LOGE = true;
    private static final boolean LOGI = true;
    private static final boolean LOGV = true;
    private static final boolean LOGW = true;
    private static final boolean SHOWDETAILINFO = false;

    public static int v(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.v(tag, msg);
        }
        return -1;
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.v(tag, msg, tr);
        }
        return -1;
    }

    public static int v(String tag, boolean debug, String msg) {
        if (!debug || !DEBUG_MODE) {
            return -1;
        }
        return v(tag, msg);
    }

    public static int d(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.d(tag, msg);
        }
        return -1;
    }

    public static int d(String tag, boolean debug, String msg) {
        if (!debug || !DEBUG_MODE) {
            return -1;
        }
        return d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.d(tag, msg, tr);
        }
        return -1;
    }

    public static int i(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.i(tag, msg);
        }
        return -1;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.i(tag, msg, tr);
        }
        return -1;
    }

    public static int w(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.w(tag, msg);
        }
        return -1;
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.w(tag, msg, tr);
        }
        return -1;
    }

    public static int e(String tag, String msg) {
        if (DEBUG_MODE) {
            return Log.e(tag, msg);
        }
        return -1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (DEBUG_MODE) {
            return Log.e(tag, msg, tr);
        }
        return -1;
    }

    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (!st.isNativeMethod() && !st.getClassName().equals(Thread.class.getName()) && !st.getClassName().equals(DebugLog.class.getName())) {
                return "[ " + Thread.currentThread().getName() + ": " + st.getLineNumber() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + st.getMethodName() + " ]";
            }
        }
        return null;
    }
}
