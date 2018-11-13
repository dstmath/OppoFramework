package com.android.server.pm;

import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;

public class ExpPackageManagerHelper {
    private static final ArrayMap<String, String> APP_NEED_DEL_DEPS = new ArrayMap();
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "ExpPackageManagerHelper";

    static {
        APP_NEED_DEL_DEPS.put("com.facebook.katana", "105");
        APP_NEED_DEL_DEPS.put("com.facebook.orca", "99");
        APP_NEED_DEL_DEPS.put("com.facebook.pages.app", "97");
        APP_NEED_DEL_DEPS.put("com.facebook.moments", "15");
        APP_NEED_DEL_DEPS.put("com.facebook.workchat", "100");
        APP_NEED_DEL_DEPS.put("com.facebook.work", "106");
        APP_NEED_DEL_DEPS.put("com.facebook.groups", "1000");
    }

    public static void appNeedDelDeps(String pkgName, String pkgVersion) {
        if (pkgName != null && pkgVersion != null) {
            String SavedVer = (String) APP_NEED_DEL_DEPS.get(pkgName);
            if (SavedVer != null) {
                String[] RealVer = pkgVersion.split("\\.", 2);
                if (RealVer[0] != null && compareVersion(RealVer[0], SavedVer)) {
                    if (DEBUG) {
                        Slog.d(TAG, "Del deps for " + pkgName + " RealVer=" + RealVer[0] + " SavedVer=" + SavedVer);
                    }
                    SystemProperties.set("oppo.facebook.del_deps", pkgName);
                }
            }
        }
    }

    private static boolean compareVersion(String oldVersion, String newVersion) {
        if (oldVersion == null || oldVersion.length() <= 0 || newVersion == null || newVersion.length() <= 0) {
            return false;
        }
        try {
            if (Integer.parseInt(oldVersion) <= Integer.parseInt(newVersion)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "compareVersion error. oldVersion=" + oldVersion + " newVersion=" + newVersion);
            return false;
        }
    }
}
