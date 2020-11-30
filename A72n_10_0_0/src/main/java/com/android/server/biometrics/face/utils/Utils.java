package com.android.server.biometrics.face.utils;

import android.content.Context;
import android.os.SystemProperties;

public class Utils {
    public static final String PLATFORM_QUALCOMM = "oppo.hw.manufacturer.qualcomm";

    public static boolean isQualcommPlatform(Context context) {
        return context.getPackageManager().hasSystemFeature(PLATFORM_QUALCOMM);
    }

    public static boolean isMtkPlatform() {
        return SystemProperties.get("ro.board.platform", "oppo").toLowerCase().startsWith("mt");
    }

    public static boolean isReleaseVersion() {
        if (SystemProperties.getBoolean("ro.build.release_type", false) || SystemProperties.getBoolean("persist.sys.test.release_type", false)) {
            return true;
        }
        return false;
    }

    public static boolean canCatchLog() {
        return SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }
}
