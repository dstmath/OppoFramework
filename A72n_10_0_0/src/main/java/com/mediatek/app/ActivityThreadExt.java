package com.mediatek.app;

import android.app.ActivityThread;
import android.hardware.Camera;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;

public class ActivityThreadExt {
    public static void enableActivityThreadLog(ActivityThread activityThread) {
        String activitylog = SystemProperties.get("persist.vendor.sys.activitylog", null);
        if (activitylog != null && !activitylog.equals("")) {
            if (activitylog.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER) == -1 || activitylog.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER) + 1 > activitylog.length()) {
                SystemProperties.set("persist.vendor.sys.activitylog", "");
                return;
            }
            String option = activitylog.substring(0, activitylog.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER));
            boolean isEnable = Camera.Parameters.FLASH_MODE_ON.equals(activitylog.substring(activitylog.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER) + 1, activitylog.length()));
            if (option.equals("x")) {
                enableActivityThreadLog(isEnable, activityThread);
            }
        }
    }

    public static void enableActivityThreadLog(boolean isEnable, ActivityThread activityThread) {
        ActivityThread.localLOGV = isEnable;
        ActivityThread.DEBUG_MESSAGES = isEnable;
        ActivityThread.DEBUG_BROADCAST = isEnable;
        ActivityThread.DEBUG_RESULTS = isEnable;
        ActivityThread.DEBUG_BACKUP = isEnable;
        ActivityThread.DEBUG_CONFIGURATION = isEnable;
        ActivityThread.DEBUG_SERVICE = isEnable;
        ActivityThread.DEBUG_MEMORY_TRIM = isEnable;
        ActivityThread.DEBUG_PROVIDER = isEnable;
        ActivityThread.DEBUG_ORDER = isEnable;
    }
}
