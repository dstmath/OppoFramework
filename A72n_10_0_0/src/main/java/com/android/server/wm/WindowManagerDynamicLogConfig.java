package com.android.server.wm;

import android.os.Build;
import android.os.SystemProperties;
import com.android.server.oppo.DumpObject;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.theia.NoFocusWindow;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class WindowManagerDynamicLogConfig {
    public static boolean DEBUG_WMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);

    WindowManagerDynamicLogConfig() {
    }

    static boolean doDump(WindowManagerService wms, String cmd, FileDescriptor fd, PrintWriter pw, String[] args, int opti) {
        if ((Build.HARDWARE.startsWith("mt") ? "oppo-log" : "log").equals(cmd)) {
            dynamicallyConfigLogTag(wms, pw, args, opti);
            return true;
        } else if (!"get_value".equals(cmd)) {
            return false;
        } else {
            dynamicGetValue(wms, pw, args);
            return true;
        }
    }

    static void enableDefaultLogIfNeed() {
        if (DEBUG_WMS) {
            setLogEnableByTag(null, "window", true);
            setLogEnableByTag(null, "anim", true);
            setLogEnableByTag(null, "apptoken", true);
        }
        if (SystemProperties.getBoolean("ro.sys.engineering.pre", false) || !SystemProperties.getBoolean("ro.build.release_type", false)) {
            SystemProperties.set("debug.screencapdump.enable", TemperatureProvider.SWITCH_ON);
        }
    }

    private static void dynamicallyConfigLogTag(WindowManagerService wms, PrintWriter pw, String[] args, int opti) {
        pw.println("dynamicallyConfigLogTag, opti:" + opti + ", args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]:" + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag:" + tag + ", on:" + on);
        if (!setLogEnableByTag(wms, tag, on)) {
            pw.println("Failed! Invalid argument! Type cmd for help: dumpsys window log");
        }
    }

    private static boolean setLogEnableByTag(WindowManagerService wms, String tag, boolean on) {
        if ("window".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_ADD_REMOVE = on;
            WindowManagerDebugConfig.DEBUG_FOCUS = on;
            WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT = on;
            WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT = on;
            WindowManagerDebugConfig.DEBUG_TASK_MOVEMENT = on;
            WindowManagerDebugConfig.DEBUG_STARTING_WINDOW = on;
            WindowManagerDebugConfig.DEBUG_STACK = on;
            WindowManagerDebugConfig.DEBUG_DIM_LAYER = on;
            return true;
        } else if ("fresh".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_LAYOUT = on;
            WindowManagerDebugConfig.DEBUG_RESIZE = on;
            WindowManagerDebugConfig.DEBUG_VISIBILITY = on;
            return true;
        } else if ("anim".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_ANIM = on;
            WindowManagerDebugConfig.DEBUG_RECENTS_ANIMATIONS = on;
            WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS = on;
            return true;
        } else if ("input".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_INPUT = on;
            WindowManagerDebugConfig.DEBUG_INPUT_METHOD = on;
            WindowManagerDebugConfig.DEBUG_DRAG = on;
            return true;
        } else if ("screen".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_SCREEN_ON = on;
            WindowManagerDebugConfig.DEBUG_SCREENSHOT = on;
            WindowManagerDebugConfig.DEBUG_BOOT = on;
            WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON = on;
            return true;
        } else if ("apptoken".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT = on;
            WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS = on;
            WindowManagerDebugConfig.DEBUG_APP_ORIENTATION = on;
            return true;
        } else if ("wallpaper".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_WALLPAPER = on;
            WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT = on;
            return true;
        } else if ("config".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_ORIENTATION = on;
            WindowManagerDebugConfig.DEBUG_APP_ORIENTATION = on;
            WindowManagerDebugConfig.DEBUG_CONFIGURATION = on;
            return true;
        } else if ("profile".equals(tag)) {
            WindowManagerService.PROFILE_ORIENTATION = on;
            return true;
        } else if ("trace".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_WINDOW_TRACE = on;
            return true;
        } else if ("surface".equals(tag)) {
            WindowManagerDebugConfig.SHOW_SURFACE_ALLOC = on;
            WindowManagerDebugConfig.SHOW_TRANSACTIONS = on;
            WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS = on;
            WindowManagerDebugConfig.DEBUG_WINDOW_CROP = on;
            return true;
        } else if ("layer".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_LAYERS = on;
            return true;
        } else if ("policy".equals(tag)) {
            if (wms == null) {
                return true;
            }
            try {
                Class<?> cls = Class.forName("com.android.server.policy.PhoneWindowManager");
                Class<?>[] clsArr = {Boolean.TYPE};
                cls.getDeclaredMethod("setDynamicalLogEnable", clsArr).invoke((PhoneWindowManager) wms.mPolicy, Boolean.valueOf(on));
                return true;
            } catch (Exception e) {
                return true;
            }
        } else if ("local".equals(tag)) {
            WindowManagerService.localLOGV = on;
            return true;
        } else if ("intercept".equals(tag)) {
            OppoWindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT = on;
            return true;
        } else if (!"systembar".equals(tag)) {
            return false;
        } else {
            OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR = on;
            return true;
        }
    }

    private static void dynamicGetValue(WindowManagerService wms, PrintWriter pw, String[] args) {
        if (args.length == 1 || args.length == 2) {
            new DumpObject().dumpValue(pw, wms, args.length == 2 ? args[1] : "");
            return;
        }
        pw.println("get_value usage:");
        pw.println("dumpsys window get_value");
        pw.println("or");
        pw.println("dumpsys window get_value variable");
    }

    private static void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 Window add or remove:DEBUG_ADD_REMOVE | DEBUG_FOCUS | DEBUG_STARTING_WINDOW | DEBUG_WINDOW_MOVEMENT | DEBUG_FOCUS_LIGHT | DEBUG_TASK_MOVEMENT | DEBUG_STACK");
        pw.println("cmd: dumpsys window oppo-log window 0/1");
        pw.println("----------------------------------");
        pw.println("2 Window fresh: DEBUG_LAYOUT | DEBUG_RESIZE | DEBUG_VISIBILITY");
        pw.println("cmd: dumpsys window oppo-log fresh 0/1");
        pw.println("----------------------------------");
        pw.println("3 Animation:DEBUG_ANIM");
        pw.println("cmd: dumpsys window oppo-log anim 0/1");
        pw.println("----------------------------------");
        pw.println("4 Input envent:DEBUG_INPUT | DEBUG_INPUT_METHOD | DEBUG_DRAG");
        pw.println("cmd: dumpsys window oppo-log input 0/1");
        pw.println("----------------------------------");
        pw.println("5 Screen status change:DEBUG_SCREEN_ON | DEBUG_SCREENSHOT | DEBUG_BOOT");
        pw.println("cmd: dumpsys window oppo-log screen 0/1");
        pw.println("----------------------------------");
        pw.println("6 App token:DEBUG_TOKEN_MOVEMENT | DEBUG_APP_TRANSITIONS | DEBUG_APP_ORIENTATION");
        pw.println("cmd: dumpsys window oppo-log apptoken 0/1");
        pw.println("----------------------------------");
        pw.println("7 Wallpaper change:DEBUG_WALLPAPER | DEBUG_WALLPAPER_LIGH");
        pw.println("cmd: dumpsys window oppo-log wallpaper 0/1");
        pw.println("----------------------------------");
        pw.println("8 Config change:DEBUG_ORIENTATION | DEBUG_APP_ORIENTATION | DEBUG_CONFIGURATION | PROFILE_ORIENTATION");
        pw.println("cmd: dumpsys window oppo-log config 0/1");
        pw.println("----------------------------------");
        pw.println("9 Trace surface and window:DEBUG_SURFACE_TRACE | DEBUG_WINDOW_TRACE");
        pw.println("cmd: dumpsys window oppo-log trace 0/1");
        pw.println("----------------------------------");
        pw.println("10 Surface show change:SHOW_SURFACE_ALLOC | SHOW_TRANSACTIONS | SHOW_LIGHT_TRANSACTIONS");
        pw.println("cmd: dumpsys window oppo-log surface 0/1");
        pw.println("----------------------------------");
        pw.println("11 Layer change:DEBUG_LAYERS");
        pw.println("cmd: dumpsys window oppo-log layer 0/1");
        pw.println("----------------------------------");
        pw.println("12 PhoneWindowManager log:All PhoneWindowManager debug log switch");
        pw.println("cmd: dumpsys window oppo-log policy 0/1");
        pw.println("----------------------------------");
        pw.println("13 local log:localLOGV");
        pw.println("cmd: dumpsys window oppo-log local 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    protected static void dumpDynamicallyLogSwitch(PrintWriter pw, String[] args, int opti) {
    }

    public static void dynamicallyConfigLogTag(PrintWriter pw, String[] args, int opti) {
        dynamicallyConfigLogTag(null, pw, args, opti);
    }
}
