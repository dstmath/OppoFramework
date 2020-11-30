package com.android.server.wm;

import android.os.SystemProperties;
import com.mediatek.server.MtkSystemServiceFactory;
import com.mediatek.server.wm.WindowManagerDebugger;

public class WindowManagerDebugConfig {
    static boolean DEBUG = false;
    static boolean DEBUG_ADD_REMOVE = false;
    static boolean DEBUG_ANIM = false;
    static boolean DEBUG_APP_ORIENTATION = false;
    static boolean DEBUG_APP_TRANSITIONS = false;
    static boolean DEBUG_BINDER = false;
    static boolean DEBUG_BOOT = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    static boolean DEBUG_CONFIGURATION = false;
    static boolean DEBUG_DIM_LAYER = false;
    static boolean DEBUG_DISPLAY = false;
    static boolean DEBUG_DRAG = false;
    static boolean DEBUG_FOCUS = false;
    static boolean DEBUG_FOCUS_LIGHT = (DEBUG_FOCUS);
    static boolean DEBUG_INPUT = false;
    static boolean DEBUG_INPUT_METHOD = false;
    static boolean DEBUG_KEEP_SCREEN_ON = false;
    static boolean DEBUG_KEYGUARD = false;
    static boolean DEBUG_LAYERS = false;
    static boolean DEBUG_LAYOUT = false;
    static boolean DEBUG_LAYOUT_REPEATS = false;
    static boolean DEBUG_OPPO_INTERCEPT = false;
    public static boolean DEBUG_OPPO_SYSTEMBAR = false;
    static boolean DEBUG_ORIENTATION = false;
    static boolean DEBUG_POWER = false;
    static boolean DEBUG_RECENTS_ANIMATIONS = (DEBUG_ANIM);
    static boolean DEBUG_REMOTE_ANIMATIONS = false;
    static boolean DEBUG_RESIZE = false;
    static boolean DEBUG_SCREENSHOT = false;
    static boolean DEBUG_SCREEN_ON = false;
    static boolean DEBUG_STACK = false;
    static boolean DEBUG_STARTING_WINDOW = (DEBUG_STARTING_WINDOW_VERBOSE);
    static boolean DEBUG_STARTING_WINDOW_VERBOSE = false;
    static boolean DEBUG_TASK_MOVEMENT = false;
    static boolean DEBUG_TASK_POSITIONING = false;
    static boolean DEBUG_TOKEN_MOVEMENT = false;
    static boolean DEBUG_UNKNOWN_APP_VISIBILITY = false;
    static boolean DEBUG_VISIBILITY = false;
    static boolean DEBUG_WALLPAPER = false;
    static boolean DEBUG_WALLPAPER_LIGHT = DEBUG_WALLPAPER;
    static boolean DEBUG_WINDOW_CROP = false;
    static boolean DEBUG_WINDOW_MOVEMENT = false;
    static boolean DEBUG_WINDOW_TRACE = false;
    static boolean DEBUG_WMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    static boolean SHOW_LIGHT_TRANSACTIONS = SHOW_TRANSACTIONS;
    static boolean SHOW_STACK_CRAWLS = false;
    static boolean SHOW_SURFACE_ALLOC = false;
    static boolean SHOW_TRANSACTIONS = false;
    static boolean SHOW_VERBOSE_TRANSACTIONS = false;
    static final String TAG_KEEP_SCREEN_ON = "DebugKeepScreenOn";
    static final boolean TAG_WITH_CLASS_NAME = false;
    static final String TAG_WM = "WindowManager";
    private static WindowManagerDebugger mWindowManagerDebugger = MtkSystemServiceFactory.getInstance().makeWindowManagerDebugger();

    static {
        boolean z = true;
        if (!DEBUG_APP_TRANSITIONS) {
            z = false;
        }
        DEBUG_REMOTE_ANIMATIONS = z;
    }
}
