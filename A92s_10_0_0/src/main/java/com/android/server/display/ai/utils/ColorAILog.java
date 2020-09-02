package com.android.server.display.ai.utils;

import android.os.SystemProperties;
import android.util.Slog;

public class ColorAILog {
    public static final String OPPO_LOG_KEY = "persist.sys.assert.panic";
    public static boolean sIsLogOn = SystemProperties.getBoolean(OPPO_LOG_KEY, false);

    private ColorAILog() {
    }

    public static void d(String tag, String msg) {
        if (sIsLogOn) {
            Slog.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (sIsLogOn) {
            Slog.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (sIsLogOn) {
            Slog.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (sIsLogOn) {
            Slog.e(tag, msg);
        }
    }
}
