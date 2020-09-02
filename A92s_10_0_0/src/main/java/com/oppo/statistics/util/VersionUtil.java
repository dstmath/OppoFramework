package com.oppo.statistics.util;

import android.content.Context;
import android.content.pm.PackageManager;

public class VersionUtil {
    private static final String DCS_PKG_NAME = "com.nearme.statistics.rom";
    private static final int SUPPORT_PERIOD_DATA_VERSION = 5118000;

    public static boolean isSupportPeriodData(Context context) {
        if (context == null) {
            return false;
        }
        try {
            if (context.getPackageManager().getPackageInfo(DCS_PKG_NAME, 1).versionCode >= SUPPORT_PERIOD_DATA_VERSION) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
