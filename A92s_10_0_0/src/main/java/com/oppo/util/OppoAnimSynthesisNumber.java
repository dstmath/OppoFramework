package com.oppo.util;

import android.os.SystemProperties;
import android.util.Slog;

public final class OppoAnimSynthesisNumber {
    private static final boolean BDEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int FLAG_HEIGHT = 16773120;
    private static final int FLAG_LAUNCHER_ANIM = -16777216;
    private static final int FLAG_LOW = 4095;
    private static final int FLAG_SYNTHESIS = 0;
    private static final int SHILFT_NUM = 12;
    private static final String TAG = "OppoAnimSynthesisNumber";

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
        if (synthesis != 0 && (-16777216 & synthesis) == 0) {
            return true;
        }
        return false;
    }

    public static boolean hasGestureAnimFlag(int num) {
        if ((num & -16777216) == -16777216) {
            return true;
        }
        return false;
    }

    public static int packageGestureAnimFlag(int num) {
        return -16777216 | 0;
    }

    public static int unpackageGestureAnimFlag(int num) {
        return 16777215 & num;
    }
}
