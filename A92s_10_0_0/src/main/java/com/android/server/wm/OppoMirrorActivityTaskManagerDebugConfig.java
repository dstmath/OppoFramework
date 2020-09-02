package com.android.server.wm;

import com.oppo.reflect.RefBoolean;
import com.oppo.reflect.RefClass;

public class OppoMirrorActivityTaskManagerDebugConfig {
    public static RefBoolean DEBUG_ADD_REMOVE;
    public static RefBoolean DEBUG_APP;
    public static RefBoolean DEBUG_CLEANUP;
    public static RefBoolean DEBUG_CONFIGURATION;
    public static RefBoolean DEBUG_CONTAINERS;
    public static RefBoolean DEBUG_FOCUS;
    public static RefBoolean DEBUG_FSTRIM;
    public static RefBoolean DEBUG_IDLE;
    public static RefBoolean DEBUG_JUNK;
    public static RefBoolean DEBUG_PAUSE;
    public static RefBoolean DEBUG_PERMISSION;
    public static RefBoolean DEBUG_RECENTS;
    public static RefBoolean DEBUG_RECENTS_TRIM_TASKS;
    public static RefBoolean DEBUG_RELEASE;
    public static RefBoolean DEBUG_RESULTS;
    public static RefBoolean DEBUG_SAVED_STATE;
    public static RefBoolean DEBUG_STACK;
    public static RefBoolean DEBUG_STATES;
    public static RefBoolean DEBUG_SWITCH;
    public static RefBoolean DEBUG_TASKS;
    public static RefBoolean DEBUG_TRANSITION;
    public static RefBoolean DEBUG_USER_LEAVING;
    public static RefBoolean DEBUG_VISIBILITY;
    public static RefBoolean DEBUG_VISIBLE_BEHIND;
    public static Class<?> TYPE = RefClass.load(OppoMirrorActivityTaskManagerDebugConfig.class, ActivityTaskManagerDebugConfig.class);

    public static void setBooleanValue(RefBoolean refBoolean, boolean value) {
        if (refBoolean != null) {
            refBoolean.set((Object) null, value);
        }
    }
}
