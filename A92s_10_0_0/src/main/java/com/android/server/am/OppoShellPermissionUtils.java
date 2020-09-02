package com.android.server.am;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoShellPermissionUtils {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean IS_ALPHA_VERSION = isAlphaVersion();
    private static final boolean IS_BETA_VERSION = isBetaVersion();
    private static final boolean IS_FORUM_VERSION = isForumVersion();
    private static final String KEY_PERMISSION_NAME = "permission";
    private static final String PROPERTY_OPPOROM = "ro.build.version.opporom";
    private static final String PROPERTY_PERMISSION_ENABLE = "persist.sys.permission.enable";
    private static final List<String> SHELL_REVOKE_PERMISSIONS = new ArrayList();
    private static final String TAG = "OSPUtils";
    private static final String UPLOAD_LOGTAG = "20089";
    private static final String UPLOAD_LOG_EVENTID = "shell_revoke";
    private static final String VERSION_ALPHA = "alpha";
    private static final String VERSION_BETA = "beta";

    static {
        SHELL_REVOKE_PERMISSIONS.add("android.permission.SEND_SMS");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.WRITE_SETTINGS");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.WRITE_SECURE_SETTINGS");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.GRANT_RUNTIME_PERMISSIONS");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.REVOKE_RUNTIME_PERMISSIONS");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.ADJUST_RUNTIME_PERMISSIONS_POLICY");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.UPDATE_APP_OPS_STATS");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.MANAGE_APP_OPS_MODES");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.KILL_BACKGROUND_PROCESSES");
        SHELL_REVOKE_PERMISSIONS.add("android.permission.CLEAR_APP_USER_DATA");
        SHELL_REVOKE_PERMISSIONS.add("oppo.permission.OPPO_COMPONENT_SAFE");
    }

    public static boolean revokeShellPermission(Context cotnext, String permName, int uid) {
        boolean result = false;
        if (IS_FORUM_VERSION) {
            return false;
        }
        if (2000 == uid && SHELL_REVOKE_PERMISSIONS.contains(permName) && SystemProperties.getBoolean("persist.sys.permission.enable", true)) {
            result = true;
            doShellStatistics(cotnext, permName);
            if (DEBUG) {
                Log.d(TAG, permName);
            }
        }
        return result;
    }

    private static boolean isForumVersion() {
        return IS_ALPHA_VERSION;
    }

    private static boolean isAlphaVersion() {
        boolean result = false;
        String ver = SystemProperties.get(PROPERTY_OPPOROM);
        if (ver != null && ver.toLowerCase().endsWith(VERSION_ALPHA)) {
            result = true;
        }
        if (DEBUG) {
            Log.d(TAG, "isAlphaVersion " + result);
        }
        return result;
    }

    private static boolean isBetaVersion() {
        boolean result = false;
        String ver = SystemProperties.get(PROPERTY_OPPOROM);
        if (ver != null && ver.toLowerCase().endsWith(VERSION_BETA)) {
            result = true;
        }
        if (DEBUG) {
            Log.d(TAG, "isBetaVersion " + result);
        }
        return result;
    }

    static void updateShellPermissions(List<String> permissionList) {
        if (permissionList != null) {
            SHELL_REVOKE_PERMISSIONS.clear();
            if (!permissionList.contains("")) {
                SHELL_REVOKE_PERMISSIONS.addAll(permissionList);
                if (DEBUG) {
                    Log.d(TAG, "updated.");
                }
            }
        }
    }

    static void doShellStatistics(Context context, String permission) {
        if (IS_BETA_VERSION && permission != null) {
            Map<String, String> statisticsMap = new HashMap<>();
            statisticsMap.put(KEY_PERMISSION_NAME, permission);
            OppoStatistics.onCommon(context, "20089", UPLOAD_LOG_EVENTID, statisticsMap, false);
        }
    }
}
