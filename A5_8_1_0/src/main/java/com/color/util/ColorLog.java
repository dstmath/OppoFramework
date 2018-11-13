package com.color.util;

import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Log;

public class ColorLog {
    private static final String KEY_EXTRA_TAG = "log.tag.extra";
    private static final String STACK_TAG = "StackTrace";
    private static final boolean STACK_TRACE = false;

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void v(boolean dbg, String tag, String msg) {
        if (dbg) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        Log.v(tag, msg, tr);
    }

    public static void v(boolean dbg, String tag, String msg, Throwable tr) {
        if (dbg) {
            Log.v(tag, msg, tr);
        }
    }

    public static void v(Class<?> cls, Object... args) {
        Log.v(getTag((Class) cls), buildMessage(args));
    }

    public static void v(String key, String tag, Object... args) {
        if (getDebug(key)) {
            Log.v(tag, buildMessage(args));
        }
    }

    public static void v(String key, Class<?> cls, Object... args) {
        if (getDebug(key)) {
            Log.v(getTag((Class) cls), buildMessage(args));
        }
    }

    public static void v(String key, String tag, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.v(tag, buildMessage(args), tr);
        }
    }

    public static void v(String key, Class<?> cls, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.v(getTag((Class) cls), buildMessage(args), tr);
        }
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void d(boolean dbg, String tag, String msg) {
        if (dbg) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        Log.d(tag, msg, tr);
    }

    public static void d(boolean dbg, String tag, String msg, Throwable tr) {
        if (dbg) {
            Log.d(tag, msg, tr);
        }
    }

    public static void d(Class<?> cls, Object... args) {
        Log.d(getTag((Class) cls), buildMessage(args));
    }

    public static void d(String key, String tag, Object... args) {
        if (getDebug(key)) {
            Log.d(tag, buildMessage(args));
        }
    }

    public static void d(String key, Class<?> cls, Object... args) {
        if (getDebug(key)) {
            Log.d(getTag((Class) cls), buildMessage(args));
        }
    }

    public static void d(String key, String tag, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.d(tag, buildMessage(args), tr);
        }
    }

    public static void d(String key, Class<?> cls, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.d(getTag((Class) cls), buildMessage(args), tr);
        }
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void i(boolean dbg, String tag, String msg) {
        if (dbg) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        Log.i(tag, msg, tr);
    }

    public static void i(boolean dbg, String tag, String msg, Throwable tr) {
        if (dbg) {
            Log.i(tag, msg, tr);
        }
    }

    public static void i(Class<?> cls, Object... args) {
        Log.i(getTag((Class) cls), buildMessage(args));
    }

    public static void i(String key, String tag, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.i(tag, buildMessage(args), tr);
        }
    }

    public static void i(String key, String tag, Object... args) {
        if (getDebug(key)) {
            Log.i(tag, buildMessage(args));
        }
    }

    public static void i(String key, Class<?> cls, Object... args) {
        if (getDebug(key)) {
            Log.i(getTag((Class) cls), buildMessage(args));
        }
    }

    public static void i(String key, Class<?> cls, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.i(getTag((Class) cls), buildMessage(args), tr);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(boolean dbg, String tag, String msg) {
        if (dbg) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }

    public static void w(boolean dbg, String tag, String msg, Throwable tr) {
        if (dbg) {
            Log.w(tag, msg, tr);
        }
    }

    public static void w(Class<?> cls, Object... args) {
        Log.w(getTag((Class) cls), buildMessage(args));
    }

    public static void w(String key, String tag, Object... args) {
        if (getDebug(key)) {
            Log.w(tag, buildMessage(args));
        }
    }

    public static void w(String key, Class<?> cls, Object... args) {
        if (getDebug(key)) {
            Log.w(getTag((Class) cls), buildMessage(args));
        }
    }

    public static void w(String key, String tag, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.w(tag, buildMessage(args), tr);
        }
    }

    public static void w(String key, Class<?> cls, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.w(getTag((Class) cls), buildMessage(args), tr);
        }
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(boolean dbg, String tag, String msg) {
        if (dbg) {
            Log.e(tag, msg);
        }
    }

    public static void e(Class<?> cls, Object... args) {
        Log.e(getTag((Class) cls), buildMessage(args));
    }

    public static void e(String key, String tag, Object... args) {
        if (getDebug(key)) {
            Log.e(tag, buildMessage(args));
        }
    }

    public static void e(String key, Class<?> cls, Object... args) {
        if (getDebug(key)) {
            Log.e(getTag((Class) cls), buildMessage(args));
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    public static void e(boolean dbg, String tag, String msg, Throwable tr) {
        if (dbg) {
            Log.e(tag, msg, tr);
        }
    }

    public static void e(String key, String tag, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.e(tag, buildMessage(args), tr);
        }
    }

    public static void e(String key, Class<?> cls, Throwable tr, Object... args) {
        if (getDebug(key)) {
            Log.e(getTag((Class) cls), buildMessage(args), tr);
        }
    }

    public static void wtf(String tag, String msg) {
        Log.wtf(tag, msg);
    }

    public static void wtf(boolean dbg, String tag, String msg) {
        if (dbg) {
            Log.wtf(tag, msg);
        }
    }

    public static void wtf(String tag, Throwable tr) {
        Log.wtf(tag, tr);
    }

    public static void wtf(boolean dbg, String tag, Throwable tr) {
        if (dbg) {
            Log.wtf(tag, tr);
        }
    }

    public static void wtf(String key, String tag, Object... args) {
        if (getDebug(key)) {
            Log.wtf(tag, new Exception(buildMessage(args)));
        }
    }

    public static void wtf(String key, Class<?> cls, Object... args) {
        if (getDebug(key)) {
            Log.wtf(getTag((Class) cls), new Exception(buildMessage(args)));
        }
    }

    public static void printStackTrace(StackTraceElement[] stacks, String tag, boolean enabled) {
    }

    public static boolean getDebug(String key) {
        return "1".equals(SystemProperties.get(key));
    }

    public static String getTag(Class<?> cls) {
        return joinString(getExtraTag(), cls.getSimpleName());
    }

    public static String getTag(Object obj) {
        return getTag(obj.getClass());
    }

    public static String buildMessage(Object[] args) {
        if (args == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                builder.append(arg.toString());
            }
        }
        return builder.toString();
    }

    public static String joinString(String... args) {
        return buildMessage(args);
    }

    private static String getExtraTag() {
        if (TextUtils.isEmpty(SystemProperties.get(KEY_EXTRA_TAG))) {
            return "";
        }
        return joinString(SystemProperties.get(KEY_EXTRA_TAG), SettingsStringUtil.DELIMITER);
    }
}
