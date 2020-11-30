package com.color.antivirus;

import android.os.SystemProperties;
import android.util.Log;

public class AntivirusLog {
    private static final String TAG = "AntivirusLog";
    private static boolean sDevelopMode;
    private static boolean sQELogOn = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static boolean sQELogOnMTK = SystemProperties.getBoolean("persist.sys.assert.enable", false);

    static {
        boolean z = false;
        if (sQELogOn || sQELogOnMTK) {
            z = true;
        }
        sDevelopMode = z;
        Log.i(TAG, "OppoLog, sQELogOn = " + sQELogOn + ", sQELogOnMTK = " + sQELogOnMTK);
        if (sQELogOn || sQELogOnMTK) {
            sDevelopMode = true;
        }
    }

    public static void i(String tag, String msg) {
        if (sDevelopMode) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (sDevelopMode) {
            Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (sDevelopMode) {
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (sDevelopMode) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Exception e) {
        Log.e(tag, msg, e);
    }
}
