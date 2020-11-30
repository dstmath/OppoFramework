package com.oppo.autotest.olt.testlib.utils;

import android.util.Log;
import com.alibaba.fastjson.parser.JSONToken;

public class LogUtils {
    private static boolean sDebug = true;

    private static void log(int level, String tag, String className, String funName, String msg, Throwable e) {
        switch (level) {
            case 0:
                Log.v(tag, "@" + className + "#" + funName + ":" + msg);
                return;
            case 1:
                Log.d(tag, "@" + className + "#" + funName + ":" + msg);
                return;
            case 2:
                Log.i(tag, "@" + className + "#" + funName + ":" + msg);
                return;
            case 3:
                Log.w(tag, "@" + className + "#" + funName + ":" + msg);
                return;
            case 4:
                Log.e(tag, "@" + className + "#" + funName + ":" + msg);
                return;
            case 5:
                Log.e(tag, "@" + className + "#" + funName + ":" + msg, e);
                return;
            case JSONToken.TRUE /* 6 */:
                Log.e(tag, "@" + className + "#" + funName + ":" + msg);
                return;
            default:
                return;
        }
    }

    public static void logInfo(String msg) {
        String[] trace = getTrace();
        log(2, "OLTTEST", trace[0], trace[1], msg, null);
    }

    public static void logError(String msg) {
        String[] trace = getTrace();
        log(4, "OLTTEST", trace[0], trace[1], msg, null);
    }

    private static String[] getTrace() {
        String[] clazzNameStr = Thread.currentThread().getStackTrace()[4].getClassName().split("\\.");
        return new String[]{clazzNameStr[clazzNameStr.length - 1], Thread.currentThread().getStackTrace()[4].getMethodName()};
    }
}
