package com.android.server.am;

import android.os.SystemProperties;

class ActivityManagerDebugConfig {
    static final boolean APPEND_CATEGORY_NAME = false;
    static boolean DEBUG_ADD_REMOVE = false;
    static final boolean DEBUG_ALL = false;
    static final boolean DEBUG_ALL_ACTIVITIES = false;
    public static boolean DEBUG_AMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    static boolean DEBUG_ANR = false;
    static boolean DEBUG_APP = false;
    static boolean DEBUG_BACKGROUND_CHECK = false;
    static boolean DEBUG_BACKUP = false;
    static boolean DEBUG_BROADCAST = (DEBUG_VERSION);
    static boolean DEBUG_BROADCAST_BACKGROUND = false;
    static boolean DEBUG_BROADCAST_LIGHT = false;
    static boolean DEBUG_CLEANUP = false;
    static boolean DEBUG_CONFIGURATION = false;
    static boolean DEBUG_CONTAINERS = false;
    static boolean DEBUG_FOCUS = false;
    static boolean DEBUG_FOREGROUND_SERVICE = false;
    static boolean DEBUG_FSTRIM = false;
    static boolean DEBUG_IDLE = false;
    static boolean DEBUG_IMMERSIVE = false;
    static boolean DEBUG_JUNK = false;
    static boolean DEBUG_LOCKSCREEN = false;
    static boolean DEBUG_LOCKTASK = false;
    static boolean DEBUG_LRU = false;
    static final boolean DEBUG_METRICS = false;
    static boolean DEBUG_MU = false;
    static boolean DEBUG_MULTI_WINDOW = false;
    static boolean DEBUG_NETWORK = false;
    static boolean DEBUG_OOM_ADJ = false;
    static boolean DEBUG_OOM_ADJ_REASON = false;
    static boolean DEBUG_OPPO_FREEFORM = false;
    static boolean DEBUG_PAUSE = false;
    static boolean DEBUG_PERMISSION = false;
    static boolean DEBUG_PERMISSIONS_REVIEW = false;
    static boolean DEBUG_POWER = false;
    static boolean DEBUG_POWER_QUICK = false;
    static boolean DEBUG_PROCESSES = false;
    static boolean DEBUG_PROCESS_OBSERVERS = false;
    static boolean DEBUG_PROVIDER = false;
    static boolean DEBUG_PSS = false;
    static boolean DEBUG_RECENTS = false;
    static boolean DEBUG_RELEASE = false;
    static boolean DEBUG_RESULTS = false;
    static boolean DEBUG_SAVED_STATE = false;
    static boolean DEBUG_SCREENSHOTS = false;
    static boolean DEBUG_SERVICE = false;
    static boolean DEBUG_SERVICE_EXECUTING = false;
    static boolean DEBUG_STACK = false;
    static boolean DEBUG_STATES = false;
    static boolean DEBUG_SWITCH = false;
    static boolean DEBUG_TASKS = false;
    static boolean DEBUG_THUMBNAILS = false;
    static boolean DEBUG_TRANSITION = false;
    static boolean DEBUG_UID_OBSERVERS = false;
    static boolean DEBUG_URI_PERMISSION = false;
    static boolean DEBUG_USAGE_STATS = false;
    static boolean DEBUG_USER_LEAVING = false;
    private static final boolean DEBUG_VERSION = "userdebug".equals(SystemProperties.get("ro.build.type"));
    static boolean DEBUG_VISIBILITY = false;
    static boolean DEBUG_VISIBLE_BEHIND = false;
    static boolean DEBUG_WHITELISTS = false;
    static final String POSTFIX_ADD_REMOVE = "";
    static final String POSTFIX_APP = "";
    static final String POSTFIX_BACKUP = "";
    static final String POSTFIX_BROADCAST = "";
    static final String POSTFIX_CLEANUP = "";
    static final String POSTFIX_CONFIGURATION = "";
    static final String POSTFIX_CONTAINERS = "";
    static final String POSTFIX_FOCUS = "";
    static final String POSTFIX_IDLE = "";
    static final String POSTFIX_IMMERSIVE = "";
    static final String POSTFIX_LOCKSCREEN = "";
    static final String POSTFIX_LOCKTASK = "";
    static final String POSTFIX_LRU = "";
    static final String POSTFIX_MU = "_MU";
    static final String POSTFIX_NETWORK = "_Network";
    static final String POSTFIX_OOM_ADJ = "";
    static final String POSTFIX_PAUSE = "";
    static final String POSTFIX_POWER = "";
    static final String POSTFIX_PROCESSES = "";
    static final String POSTFIX_PROCESS_OBSERVERS = "";
    static final String POSTFIX_PROVIDER = "";
    static final String POSTFIX_PSS = "";
    static final String POSTFIX_RECENTS = "";
    static final String POSTFIX_RELEASE = "";
    static final String POSTFIX_RESULTS = "";
    static final String POSTFIX_SAVED_STATE = "";
    static final String POSTFIX_SCREENSHOTS = "";
    static final String POSTFIX_SERVICE = "";
    static final String POSTFIX_SERVICE_EXECUTING = "";
    static final String POSTFIX_STACK = "";
    static final String POSTFIX_STATES = "";
    static final String POSTFIX_SWITCH = "";
    static final String POSTFIX_TASKS = "";
    static final String POSTFIX_THUMBNAILS = "";
    static final String POSTFIX_TRANSITION = "";
    static final String POSTFIX_UID_OBSERVERS = "";
    static final String POSTFIX_URI_PERMISSION = "";
    static final String POSTFIX_USER_LEAVING = "";
    static final String POSTFIX_VISIBILITY = "";
    static final String POSTFIX_VISIBLE_BEHIND = "";
    static final String TAG_AM = "ActivityManager";
    static final boolean TAG_WITH_CLASS_NAME = false;

    ActivityManagerDebugConfig() {
    }

    static {
        boolean z;
        boolean z2 = true;
        if (DEBUG_BROADCAST) {
            z = true;
        } else {
            z = false;
        }
        DEBUG_BROADCAST_BACKGROUND = z;
        if (DEBUG_BROADCAST) {
            z = true;
        } else {
            z = false;
        }
        DEBUG_BROADCAST_LIGHT = z;
        z = DEBUG_AMS;
        if (!DEBUG_POWER) {
            z2 = false;
        }
        DEBUG_POWER_QUICK = z2;
    }
}
