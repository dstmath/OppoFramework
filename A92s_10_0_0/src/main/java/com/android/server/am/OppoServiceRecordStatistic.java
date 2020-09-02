package com.android.server.am;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoServiceRecordStatistic {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String EVENT_ID = "background_service_invalid_notification";
    private static final String EVENT_KEY_APP_NAME = "appName";
    private static final String EVENT_KEY_APP_UID = "appUid";
    private static final String EVENT_KEY_PACKAGE_NAME = "packageName";
    private static final String EVENT_LOG_TAG = "NotificationService";
    private static final int ONE_DAY_MILLS = 86400000;
    private static final String TAG = "OppoServiceRecordStatistic";
    private static List<String> sList = new ArrayList();
    private static long sStart = 0;

    public static void postStatisticEventWithCheck(Context cxt, String appName, String pkg, int userId) {
        if (cxt != null) {
            long cur = System.currentTimeMillis();
            if (cur - sStart > 86400000) {
                sList.clear();
                sStart = cur;
                realPostStatisticEvent(cxt, appName, pkg, userId);
            } else if (!sList.contains(pkg)) {
                realPostStatisticEvent(cxt, appName, pkg, userId);
            }
        }
    }

    private static void realPostStatisticEvent(Context cxt, String appName, String pkg, int userId) {
        if (DEBUG) {
            Log.d(TAG, "doStatistics appName:" + appName + ", package:" + pkg);
        }
        sList.add(pkg);
        HashMap<String, String> map = new HashMap<>();
        map.put(EVENT_KEY_APP_NAME, appName);
        map.put("packageName", pkg);
        map.put(EVENT_KEY_APP_UID, String.valueOf(userId));
        OppoStatistics.onCommon(cxt, EVENT_LOG_TAG, EVENT_ID, map, false);
    }
}
