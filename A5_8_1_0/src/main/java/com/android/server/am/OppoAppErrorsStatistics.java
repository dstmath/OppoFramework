package com.android.server.am;

import android.app.ApplicationErrorReport.CrashInfo;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import java.util.HashMap;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoAppErrorsStatistics {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean IS_FORUM_VERSION = isForumVersion();
    private static final String KEY_MESSAGE = "exceptionMessage";
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_TRACE = "stackTrace";
    private static final String PROPERTY_OPPOROM = "ro.build.version.opporom";
    private static final String SECURITY_EXCEPTION = "java.lang.SecurityException";
    private static final String TAG = "OAEStatistic";
    private static final String UPLOAD_LOGTAG = "20089";
    private static final String UPLOAD_LOG_EVENTID = "security_exception";
    private static final String VERSION_ALPHA = "alpha";
    private static final String VERSION_BETA = "beta";

    private static boolean isForumVersion() {
        boolean result = false;
        String ver = SystemProperties.get(PROPERTY_OPPOROM);
        if (ver != null) {
            ver = ver.toLowerCase();
            if (ver.endsWith(VERSION_ALPHA) || ver.endsWith(VERSION_BETA)) {
                result = true;
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "isForumVersion " + result);
        }
        return result;
    }

    public static void doErrorsStatistics(Context context, ProcessRecord r, CrashInfo crashInfo) {
        if (r != null && r.info != null && crashInfo != null && IS_FORUM_VERSION && SECURITY_EXCEPTION.equals(crashInfo.exceptionClassName)) {
            if (DEBUG) {
                Slog.d(TAG, "crash app is " + r.info.packageName + " ; crash info is " + 10 + "exceptionClassName : " + crashInfo.exceptionClassName + 10 + "exceptionMessage : " + crashInfo.exceptionMessage);
            }
            Slog.d(TAG, "handle SecurityException" + r.info.packageName);
            Map<String, String> statisticsMap = new HashMap();
            statisticsMap.put(KEY_PACKAGE_NAME, r.info.packageName);
            statisticsMap.put(KEY_MESSAGE, crashInfo.exceptionMessage);
            statisticsMap.put(KEY_TRACE, crashInfo.stackTrace);
            OppoStatistics.onCommon(context, UPLOAD_LOGTAG, UPLOAD_LOG_EVENTID, statisticsMap, false);
        }
    }
}
