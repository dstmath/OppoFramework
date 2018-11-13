package com.oppo.media;

import android.util.Log;

public final class DebugLog {
    private static final boolean DEBUG_MODE = true;
    private static final boolean LOGD = true;
    private static final boolean LOGE = true;
    private static final boolean LOGI = true;
    private static final boolean LOGV = true;
    private static final boolean LOGW = true;
    private static final boolean SHOWDETAILINFO = false;

    public static int v(String tag, String msg) {
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return Log.v(tag, msg, tr);
    }

    public static int v(String tag, boolean debug, String msg) {
        if (debug) {
            return v(tag, msg);
        }
        return -1;
    }

    public static int d(String tag, String msg) {
        return Log.d(tag, msg);
    }

    public static int d(String tag, boolean debug, String msg) {
        if (debug) {
            return d(tag, msg);
        }
        return -1;
    }

    public static int d(String tag, String msg, Throwable tr) {
        return Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (!st.isNativeMethod() && !st.getClassName().equals(Thread.class.getName()) && !st.getClassName().equals(DebugLog.class.getName())) {
                return "[ " + Thread.currentThread().getName() + ": " + st.getLineNumber() + " " + st.getMethodName() + " ]";
            }
        }
        return null;
    }
}
