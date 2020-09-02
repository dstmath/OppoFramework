package com.android.server.am;

import android.os.SystemProperties;

public class ActivityManagerDebugConfig {
    public static boolean APPEND_CATEGORY_NAME = false;
    public static boolean DEBUG_ALL = false;
    public static boolean DEBUG_AMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static boolean DEBUG_ANR = true;
    public static boolean DEBUG_BACKGROUND_CHECK = (DEBUG_ALL);
    public static boolean DEBUG_BACKUP = (DEBUG_ALL);
    public static boolean DEBUG_BROADCAST = (DEBUG_ALL);
    public static boolean DEBUG_BROADCAST_BACKGROUND = (DEBUG_BROADCAST);
    public static boolean DEBUG_BROADCAST_DEFERRAL = (DEBUG_BROADCAST);
    public static boolean DEBUG_BROADCAST_LIGHT = (DEBUG_BROADCAST);
    public static boolean DEBUG_COMPACTION = (DEBUG_ALL);
    public static boolean DEBUG_FOREGROUND_SERVICE = (DEBUG_ALL);
    public static boolean DEBUG_LRU = (DEBUG_ALL);
    public static boolean DEBUG_MU = (DEBUG_ALL);
    public static boolean DEBUG_NETWORK = (DEBUG_ALL);
    public static boolean DEBUG_OOM_ADJ = (DEBUG_ALL);
    public static boolean DEBUG_OOM_ADJ_REASON = (DEBUG_ALL);
    public static boolean DEBUG_PERMISSIONS_REVIEW = (DEBUG_ALL);
    public static boolean DEBUG_POWER = (DEBUG_ALL);
    public static boolean DEBUG_POWER_QUICK = (DEBUG_POWER);
    public static boolean DEBUG_PROCESSES = (DEBUG_ALL);
    public static boolean DEBUG_PROCESS_OBSERVERS = (DEBUG_ALL);
    public static boolean DEBUG_PROVIDER = (DEBUG_ALL);
    public static boolean DEBUG_PSS = (DEBUG_ALL);
    public static boolean DEBUG_SERVICE = (DEBUG_ALL);
    public static boolean DEBUG_SERVICE_EXECUTING = (DEBUG_ALL);
    public static boolean DEBUG_UID_OBSERVERS = (DEBUG_ALL);
    public static boolean DEBUG_USAGE_STATS = (DEBUG_ALL);
    private static final boolean DEBUG_VERSION = "userdebug".equals(SystemProperties.get("ro.build.type"));
    public static boolean DEBUG_WHITELISTS = false;
    static final String POSTFIX_BACKUP;
    static final String POSTFIX_BROADCAST;
    static final String POSTFIX_CLEANUP;
    static final String POSTFIX_LRU;
    static final String POSTFIX_MU = "_MU";
    static final String POSTFIX_NETWORK = "_Network";
    static final String POSTFIX_OOM_ADJ;
    static final String POSTFIX_POWER;
    static final String POSTFIX_PROCESSES;
    static final String POSTFIX_PROCESS_OBSERVERS;
    static final String POSTFIX_PROVIDER;
    static final String POSTFIX_PSS;
    static final String POSTFIX_SERVICE;
    static final String POSTFIX_SERVICE_EXECUTING;
    static final String POSTFIX_UID_OBSERVERS;
    static final String TAG_AM = "ActivityManager";
    static final boolean TAG_WITH_CLASS_NAME = false;

    static {
        boolean z = false;
        if (DEBUG_ALL) {
            z = true;
        }
        DEBUG_WHITELISTS = z;
        String str = "";
        POSTFIX_BACKUP = APPEND_CATEGORY_NAME ? "_Backup" : str;
        POSTFIX_BROADCAST = APPEND_CATEGORY_NAME ? "_Broadcast" : str;
        POSTFIX_CLEANUP = APPEND_CATEGORY_NAME ? "_Cleanup" : str;
        POSTFIX_LRU = APPEND_CATEGORY_NAME ? "_LRU" : str;
        POSTFIX_OOM_ADJ = APPEND_CATEGORY_NAME ? "_OomAdj" : str;
        POSTFIX_POWER = APPEND_CATEGORY_NAME ? "_Power" : str;
        POSTFIX_PROCESS_OBSERVERS = APPEND_CATEGORY_NAME ? "_ProcessObservers" : str;
        POSTFIX_PROCESSES = APPEND_CATEGORY_NAME ? "_Processes" : str;
        POSTFIX_PROVIDER = APPEND_CATEGORY_NAME ? "_Provider" : str;
        POSTFIX_PSS = APPEND_CATEGORY_NAME ? "_Pss" : str;
        POSTFIX_SERVICE = APPEND_CATEGORY_NAME ? "_Service" : str;
        POSTFIX_SERVICE_EXECUTING = APPEND_CATEGORY_NAME ? "_ServiceExecuting" : str;
        if (APPEND_CATEGORY_NAME) {
            str = "_UidObservers";
        }
        POSTFIX_UID_OBSERVERS = str;
    }
}
