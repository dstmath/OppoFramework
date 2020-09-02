package com.android.server.wm;

import android.util.ArrayMap;
import com.oppo.reflect.RefBoolean;
import com.oppo.reflect.RefClass;

public class OppoMirrorWmsConfig {
    public static RefBoolean DEBUG_ADD_REMOVE;
    public static RefBoolean DEBUG_ANIM;
    public static RefBoolean DEBUG_APP_ORIENTATION;
    public static RefBoolean DEBUG_APP_TRANSITIONS;
    public static RefBoolean DEBUG_BOOT;
    public static RefBoolean DEBUG_CONFIGURATION;
    public static RefBoolean DEBUG_DRAG;
    public static RefBoolean DEBUG_FOCUS;
    public static RefBoolean DEBUG_FOCUS_LIGHT;
    public static RefBoolean DEBUG_INPUT;
    public static RefBoolean DEBUG_INPUT_METHOD;
    public static RefBoolean DEBUG_KEEP_SCREEN_ON;
    public static RefBoolean DEBUG_LAYERS;
    public static RefBoolean DEBUG_LAYOUT;
    public static RefBoolean DEBUG_ORIENTATION;
    public static RefBoolean DEBUG_RESIZE;
    public static RefBoolean DEBUG_SCREENSHOT;
    public static RefBoolean DEBUG_SCREEN_ON;
    public static RefBoolean DEBUG_STACK;
    public static RefBoolean DEBUG_STARTING_WINDOW;
    public static RefBoolean DEBUG_TASK_MOVEMENT;
    public static RefBoolean DEBUG_TOKEN_MOVEMENT;
    public static RefBoolean DEBUG_VISIBILITY;
    public static RefBoolean DEBUG_WALLPAPER;
    public static RefBoolean DEBUG_WALLPAPER_LIGHT;
    public static RefBoolean DEBUG_WINDOW_MOVEMENT;
    public static RefBoolean DEBUG_WINDOW_TRACE;
    public static ArrayMap<String, RefBoolean> OBJECTS_ARRAY = new ArrayMap<>();
    public static RefBoolean SHOW_LIGHT_TRANSACTIONS;
    public static RefBoolean SHOW_SURFACE_ALLOC;
    public static RefBoolean SHOW_TRANSACTIONS;
    public static Class<?> TYPE = RefClass.load(OppoMirrorWmsConfig.class, WindowManagerDebugConfig.class);
    private static boolean mArrayInit = false;

    public static void setBooleanValue(RefBoolean refBoolean, boolean value) {
        if (refBoolean != null) {
            refBoolean.set((Object) null, value);
        }
    }

    public static void initArray() {
        if (!mArrayInit) {
            OBJECTS_ARRAY.put("DEBUG_ADD_REMOVE", DEBUG_ADD_REMOVE);
            OBJECTS_ARRAY.put("DEBUG_FOCUS", DEBUG_FOCUS);
            OBJECTS_ARRAY.put("DEBUG_FOCUS_LIGHT", DEBUG_FOCUS_LIGHT);
            OBJECTS_ARRAY.put("DEBUG_WINDOW_MOVEMENT", DEBUG_WINDOW_MOVEMENT);
            OBJECTS_ARRAY.put("DEBUG_TASK_MOVEMENT", DEBUG_TASK_MOVEMENT);
            OBJECTS_ARRAY.put("DEBUG_STARTING_WINDOW", DEBUG_STARTING_WINDOW);
            OBJECTS_ARRAY.put("DEBUG_STACK", DEBUG_STACK);
            OBJECTS_ARRAY.put("DEBUG_LAYOUT", DEBUG_LAYOUT);
            OBJECTS_ARRAY.put("DEBUG_RESIZE", DEBUG_RESIZE);
            OBJECTS_ARRAY.put("DEBUG_VISIBILITY", DEBUG_VISIBILITY);
            OBJECTS_ARRAY.put("DEBUG_ANIM", DEBUG_ANIM);
            OBJECTS_ARRAY.put("DEBUG_INPUT", DEBUG_INPUT);
            OBJECTS_ARRAY.put("DEBUG_INPUT_METHOD", DEBUG_INPUT_METHOD);
            OBJECTS_ARRAY.put("DEBUG_DRAG", DEBUG_DRAG);
            OBJECTS_ARRAY.put("DEBUG_SCREEN_ON", DEBUG_SCREEN_ON);
            OBJECTS_ARRAY.put("DEBUG_SCREENSHOT", DEBUG_SCREENSHOT);
            OBJECTS_ARRAY.put("DEBUG_BOOT", DEBUG_BOOT);
            OBJECTS_ARRAY.put("DEBUG_KEEP_SCREEN_ON", DEBUG_KEEP_SCREEN_ON);
            OBJECTS_ARRAY.put("SHOW_TRANSACTIONS", SHOW_TRANSACTIONS);
            OBJECTS_ARRAY.put("DEBUG_TOKEN_MOVEMENT", DEBUG_TOKEN_MOVEMENT);
            OBJECTS_ARRAY.put("DEBUG_APP_TRANSITIONS", DEBUG_APP_TRANSITIONS);
            OBJECTS_ARRAY.put("DEBUG_APP_ORIENTATION", DEBUG_APP_ORIENTATION);
            OBJECTS_ARRAY.put("DEBUG_WALLPAPER", DEBUG_WALLPAPER);
            OBJECTS_ARRAY.put("DEBUG_WALLPAPER_LIGHT", DEBUG_WALLPAPER_LIGHT);
            OBJECTS_ARRAY.put("DEBUG_ORIENTATION", DEBUG_ORIENTATION);
            OBJECTS_ARRAY.put("DEBUG_CONFIGURATION", DEBUG_CONFIGURATION);
            OBJECTS_ARRAY.put("DEBUG_WINDOW_TRACE", DEBUG_WINDOW_TRACE);
            OBJECTS_ARRAY.put("SHOW_SURFACE_ALLOC", SHOW_SURFACE_ALLOC);
            OBJECTS_ARRAY.put("SHOW_LIGHT_TRANSACTIONS", SHOW_LIGHT_TRANSACTIONS);
            OBJECTS_ARRAY.put("DEBUG_LAYERS", DEBUG_LAYERS);
            mArrayInit = true;
        }
    }

    public static boolean getFlagValueByName(String flagName) {
        RefBoolean refObj = OBJECTS_ARRAY.get(flagName);
        if (refObj != null) {
            return refObj.get((Object) null);
        }
        return false;
    }
}
