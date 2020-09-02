package com.android.server.wm;

import android.os.SystemProperties;

public class ActivityTaskManagerDebugConfig {
    public static boolean APPEND_CATEGORY_NAME = false;
    public static boolean DEBUG_ADD_REMOVE = (DEBUG_ALL_ACTIVITIES);
    public static boolean DEBUG_ALL = false;
    public static boolean DEBUG_ALL_ACTIVITIES = (DEBUG_ALL);
    public static boolean DEBUG_AMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static boolean DEBUG_APP = (DEBUG_ALL_ACTIVITIES);
    public static boolean DEBUG_CLEANUP = (DEBUG_ALL);
    public static boolean DEBUG_CONFIGURATION = (DEBUG_ALL);
    public static boolean DEBUG_CONTAINERS = (DEBUG_ALL_ACTIVITIES);
    public static boolean DEBUG_FOCUS = false;
    public static boolean DEBUG_FSTRIM = (DEBUG_ALL);
    public static boolean DEBUG_IDLE = (DEBUG_ALL_ACTIVITIES);
    public static boolean DEBUG_IMMERSIVE = (DEBUG_ALL);
    static boolean DEBUG_JUNK = false;
    public static boolean DEBUG_LOCKTASK = (DEBUG_ALL);
    public static boolean DEBUG_METRICS = (DEBUG_ALL);
    public static boolean DEBUG_PAUSE = (DEBUG_ALL);
    static boolean DEBUG_PERMISSION = (DEBUG_ALL);
    public static boolean DEBUG_PERMISSIONS_REVIEW = (DEBUG_ALL);
    public static boolean DEBUG_RECENTS = (DEBUG_ALL);
    public static boolean DEBUG_RECENTS_TRIM_TASKS = (DEBUG_RECENTS);
    public static boolean DEBUG_RELEASE = (DEBUG_ALL_ACTIVITIES);
    public static boolean DEBUG_RESULTS = (DEBUG_ALL);
    public static boolean DEBUG_SAVED_STATE = (DEBUG_ALL_ACTIVITIES);
    public static boolean DEBUG_STACK = (DEBUG_ALL);
    public static boolean DEBUG_STATES = (DEBUG_ALL_ACTIVITIES);
    public static boolean DEBUG_SWITCH = (DEBUG_ALL);
    public static boolean DEBUG_TASKS = (DEBUG_ALL);
    public static boolean DEBUG_TRANSITION = (DEBUG_ALL);
    public static boolean DEBUG_USER_LEAVING = (DEBUG_ALL);
    private static final boolean DEBUG_VERSION = "userdebug".equals(SystemProperties.get("ro.build.type"));
    public static boolean DEBUG_VISIBILITY = (DEBUG_ALL);
    public static boolean DEBUG_VISIBLE_BEHIND = (DEBUG_ALL_ACTIVITIES);
    static final String POSTFIX_ADD_REMOVE;
    static final String POSTFIX_APP;
    static final String POSTFIX_CLEANUP;
    public static final String POSTFIX_CONFIGURATION;
    static final String POSTFIX_CONTAINERS;
    static final String POSTFIX_FOCUS;
    static final String POSTFIX_IDLE;
    static final String POSTFIX_IMMERSIVE;
    public static final String POSTFIX_LOCKTASK;
    static final String POSTFIX_PAUSE;
    static final String POSTFIX_RECENTS;
    static final String POSTFIX_RELEASE;
    static final String POSTFIX_RESULTS;
    static final String POSTFIX_SAVED_STATE;
    static final String POSTFIX_STACK;
    static final String POSTFIX_STATES;
    public static final String POSTFIX_SWITCH;
    static final String POSTFIX_TASKS;
    static final String POSTFIX_TRANSITION;
    static final String POSTFIX_USER_LEAVING;
    static final String POSTFIX_VISIBILITY;
    static final String TAG_ATM = "ActivityTaskManager";
    static final boolean TAG_WITH_CLASS_NAME = false;

    static {
        boolean z = true;
        if (!DEBUG_ALL) {
            z = false;
        }
        DEBUG_JUNK = z;
        String str = "";
        POSTFIX_APP = APPEND_CATEGORY_NAME ? "_App" : str;
        POSTFIX_CLEANUP = APPEND_CATEGORY_NAME ? "_Cleanup" : str;
        POSTFIX_IDLE = APPEND_CATEGORY_NAME ? "_Idle" : str;
        POSTFIX_RELEASE = APPEND_CATEGORY_NAME ? "_Release" : str;
        POSTFIX_USER_LEAVING = APPEND_CATEGORY_NAME ? "_UserLeaving" : str;
        POSTFIX_ADD_REMOVE = APPEND_CATEGORY_NAME ? "_AddRemove" : str;
        POSTFIX_CONFIGURATION = APPEND_CATEGORY_NAME ? "_Configuration" : str;
        POSTFIX_CONTAINERS = APPEND_CATEGORY_NAME ? "_Containers" : str;
        POSTFIX_FOCUS = APPEND_CATEGORY_NAME ? "_Focus" : str;
        POSTFIX_IMMERSIVE = APPEND_CATEGORY_NAME ? "_Immersive" : str;
        POSTFIX_LOCKTASK = APPEND_CATEGORY_NAME ? "_LockTask" : str;
        POSTFIX_PAUSE = APPEND_CATEGORY_NAME ? "_Pause" : str;
        POSTFIX_RECENTS = APPEND_CATEGORY_NAME ? "_Recents" : str;
        POSTFIX_SAVED_STATE = APPEND_CATEGORY_NAME ? "_SavedState" : str;
        POSTFIX_STACK = APPEND_CATEGORY_NAME ? "_Stack" : str;
        POSTFIX_STATES = APPEND_CATEGORY_NAME ? "_States" : str;
        POSTFIX_SWITCH = APPEND_CATEGORY_NAME ? "_Switch" : str;
        POSTFIX_TASKS = APPEND_CATEGORY_NAME ? "_Tasks" : str;
        POSTFIX_TRANSITION = APPEND_CATEGORY_NAME ? "_Transition" : str;
        POSTFIX_VISIBILITY = APPEND_CATEGORY_NAME ? "_Visibility" : str;
        if (APPEND_CATEGORY_NAME) {
            str = "_Results";
        }
        POSTFIX_RESULTS = str;
    }
}
