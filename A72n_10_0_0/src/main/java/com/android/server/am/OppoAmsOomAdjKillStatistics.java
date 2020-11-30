package com.android.server.am;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.theia.NoFocusWindow;
import java.util.HashMap;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoAmsOomAdjKillStatistics {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_TYPE = "type";
    private static final String KEY_VERSION = "version";
    public static final int KILL_TYPE_CACHED_NUMBER = 5;
    public static final int KILL_TYPE_EMPTY = 2;
    public static final int KILL_TYPE_EMPTY_FOR = 3;
    public static final int KILL_TYPE_EMPTY_NUMBER = 4;
    public static final int KILL_TYPE_EXCESSIVE_CPU = 1;
    public static final int KILL_TYPE_ISOLATED_NOT_NEEDED = 6;
    public static final int KILL_TYPE_REMOVE_TASK = 7;
    private static final String PROPERTY_OPPOROM = "ro.build.version.opporom";
    private static final String UPLOAD_AMS_KILL_EVENTID = "ams_kill_event";
    private static final String UPLOAD_LOGTAG = "20120";
    private static final String VERSION_ALPHA = "alpha";
    private static final String VERSION_BETA = "beta";
    private static OppoAmsOomAdjKillStatistics sInstance = null;
    private final String TAG = "OppoAmsOomAdjKillStatistics";
    private boolean mIsForumVersion = false;

    private OppoAmsOomAdjKillStatistics() {
        initVersionInfo();
    }

    public static final OppoAmsOomAdjKillStatistics getInstance() {
        if (sInstance == null) {
            sInstance = new OppoAmsOomAdjKillStatistics();
        }
        return sInstance;
    }

    private void initVersionInfo() {
        boolean result = false;
        String ver = SystemProperties.get(PROPERTY_OPPOROM);
        if (ver != null) {
            String ver2 = ver.toLowerCase();
            if (ver2.endsWith(VERSION_ALPHA) || ver2.endsWith(VERSION_BETA)) {
                result = true;
            }
        }
        if (DEBUG) {
            Log.d("OppoAmsOomAdjKillStatistics", "initVersionInfo " + result);
        }
        this.mIsForumVersion = result;
    }

    private boolean isForumVersion() {
        return this.mIsForumVersion;
    }

    /* access modifiers changed from: protected */
    public void doStatistics(Context context, ProcessRecord r, int killType) {
        if (isForumVersion() && r != null) {
            if (DEBUG) {
                Log.d("OppoAmsOomAdjKillStatistics", "doStatistics type:" + killType + ", package:" + r.info.packageName);
            }
            Map<String, String> statisticsMap = new HashMap<>();
            statisticsMap.put(KEY_PACKAGE_NAME, r.info.packageName);
            statisticsMap.put("type", Integer.toString(killType));
            statisticsMap.put("version", NoFocusWindow.HUNG_CONFIG_ENABLE);
            OppoStatistics.onCommon(context, "20120", UPLOAD_AMS_KILL_EVENTID, statisticsMap, false);
        }
    }
}
