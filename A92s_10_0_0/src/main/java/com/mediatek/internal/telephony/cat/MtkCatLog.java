package com.mediatek.internal.telephony.cat;

import android.os.Build;
import android.telephony.Rlog;
import android.text.TextUtils;

public abstract class MtkCatLog {
    static final boolean DEBUG = true;
    static final boolean ENGDEBUG = TextUtils.equals(Build.TYPE, "eng");
    static final String TAG = "MTKCAT";

    public static void d(Object caller, String msg) {
        String className = caller.getClass().getName();
        Rlog.d(TAG, className.substring(className.lastIndexOf(46) + 1) + ": " + msg);
    }

    public static void d(String caller, String msg) {
        Rlog.d(TAG, caller + ": " + msg);
    }

    public static void e(Object caller, String msg) {
        String className = caller.getClass().getName();
        Rlog.e(TAG, className.substring(className.lastIndexOf(46) + 1) + ": " + msg);
    }

    public static void e(String caller, String msg) {
        Rlog.e(TAG, caller + ": " + msg);
    }

    public static void w(Object caller, String msg) {
        if (ENGDEBUG) {
            String className = caller.getClass().getName();
            Rlog.w(TAG, className.substring(className.lastIndexOf(46) + 1) + ": " + msg);
        }
    }

    public static void w(String caller, String msg) {
        if (ENGDEBUG) {
            Rlog.w(TAG, caller + ": " + msg);
        }
    }

    public static void v(Object caller, String msg) {
        if (ENGDEBUG) {
            String className = caller.getClass().getName();
            Rlog.v(TAG, className.substring(className.lastIndexOf(46) + 1) + ": " + msg);
        }
    }

    public static void v(String caller, String msg) {
        if (ENGDEBUG) {
            Rlog.v(TAG, caller + ": " + msg);
        }
    }
}
