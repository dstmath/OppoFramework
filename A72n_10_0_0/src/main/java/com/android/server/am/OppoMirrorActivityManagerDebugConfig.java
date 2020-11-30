package com.android.server.am;

import com.oppo.reflect.RefBoolean;
import com.oppo.reflect.RefClass;

public class OppoMirrorActivityManagerDebugConfig {
    public static RefBoolean DEBUG_BACKGROUND_CHECK;
    public static RefBoolean DEBUG_BACKUP;
    public static RefBoolean DEBUG_BROADCAST;
    public static RefBoolean DEBUG_BROADCAST_BACKGROUND;
    public static RefBoolean DEBUG_BROADCAST_LIGHT;
    public static RefBoolean DEBUG_FOREGROUND_SERVICE;
    public static RefBoolean DEBUG_LRU;
    public static RefBoolean DEBUG_MU;
    public static RefBoolean DEBUG_OOM_ADJ;
    public static RefBoolean DEBUG_POWER;
    public static RefBoolean DEBUG_PROCESSES;
    public static RefBoolean DEBUG_PROCESS_OBSERVERS;
    public static RefBoolean DEBUG_PROVIDER;
    public static RefBoolean DEBUG_SERVICE;
    public static RefBoolean DEBUG_SERVICE_EXECUTING;
    public static Class<?> TYPE = RefClass.load(OppoMirrorActivityManagerDebugConfig.class, ActivityManagerDebugConfig.class);

    public static void setBooleanValue(RefBoolean refBoolean, boolean value) {
        if (refBoolean != null) {
            refBoolean.set((Object) null, value);
        }
    }
}
