package com.oppo.util;

import android.os.SystemProperties;
import android.util.Slog;

public final class OppoAnimSynthesisNumber {
    private static final boolean BDEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "OppoAnimSynthesisNumber";
    private static int mFlagSynthesis = 0;

    private OppoAnimSynthesisNumber() {
    }

    public static int getSynthesisNumber(int high, int lower) {
        if (BDEBUG) {
            Slog.d(TAG, "getSynthesisNumber high = " + high + " lower = " + lower);
        }
        return (lower & 4095) + ((high << 12) & 16773120);
    }

    public static int getHighDigit(int synthesis) {
        if (isSynthesisNumber(synthesis)) {
            return (synthesis >> 12) & 4095;
        }
        return synthesis;
    }

    public static int getLowerDigit(int synthesis) {
        if (isSynthesisNumber(synthesis)) {
            return synthesis & 4095;
        }
        return synthesis;
    }

    public static boolean isSynthesisNumber(int synthesis) {
        if (synthesis != 0 && mFlagSynthesis == (-16777216 & synthesis)) {
            return true;
        }
        return false;
    }
}
