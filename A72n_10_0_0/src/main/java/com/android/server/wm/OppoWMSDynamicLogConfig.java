package com.android.server.wm;

import android.os.SystemProperties;
import android.util.Log;
import com.android.server.oppo.DumpObject;
import com.android.server.theia.NoFocusWindow;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class OppoWMSDynamicLogConfig {
    public static boolean DEBUG_ORIENT = DEBUG_WMS;
    public static boolean DEBUG_PERMISSION = false;
    public static boolean DEBUG_POINTER = false;
    public static boolean DEBUG_WMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    static final String TAG = "OppoWMDebugConfig";
    private static final boolean mVerbosePrint = Log.isLoggable(TAG, 2);

    private OppoWMSDynamicLogConfig() {
    }

    private static class InstanceHolder {
        static final OppoWMSDynamicLogConfig INSTANCE = new OppoWMSDynamicLogConfig();

        private InstanceHolder() {
        }
    }

    public static OppoWMSDynamicLogConfig getInstance() {
        Log.d(TAG, "getInstance.");
        return InstanceHolder.INSTANCE;
    }

    public static void dynamicallyConfigLogTag(PrintWriter pw, String[] args, int opti) {
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
        if ("window".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_ADD_REMOVE, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_FOCUS, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_FOCUS_LIGHT, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_WINDOW_MOVEMENT, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_TASK_MOVEMENT, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_STARTING_WINDOW, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_STACK, on);
        } else if ("fresh".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_LAYOUT, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_RESIZE, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_VISIBILITY, on);
        } else if ("anim".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_ANIM, on);
        } else if ("input".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_INPUT, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_INPUT_METHOD, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_DRAG, on);
        } else if ("screen".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_SCREEN_ON, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_SCREENSHOT, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_BOOT, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_KEEP_SCREEN_ON, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.SHOW_TRANSACTIONS, on);
        } else if ("apptoken".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_TOKEN_MOVEMENT, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_APP_TRANSITIONS, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_APP_ORIENTATION, on);
        } else if ("wallpaper".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_WALLPAPER, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_WALLPAPER_LIGHT, on);
        } else if ("config".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_ORIENTATION, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_APP_ORIENTATION, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_CONFIGURATION, on);
        } else if ("profile".equals(tag)) {
            OppoMirrorWmsService.setBooleanValue(OppoMirrorWmsService.PROFILE_ORIENTATION, on);
        } else if ("trace".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_WINDOW_TRACE, on);
        } else if ("surface".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.SHOW_SURFACE_ALLOC, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.SHOW_TRANSACTIONS, on);
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.SHOW_LIGHT_TRANSACTIONS, on);
        } else if ("layer".equals(tag)) {
            OppoMirrorWmsConfig.setBooleanValue(OppoMirrorWmsConfig.DEBUG_LAYERS, on);
        } else if (!"policy".equals(tag)) {
            if ("local".equals(tag)) {
                OppoMirrorWmsService.setBooleanValue(OppoMirrorWmsService.localLOGV, on);
            } else if ("intercept".equals(tag)) {
                OppoWindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT = on;
            } else if ("systembar".equals(tag)) {
                OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR = on;
            } else {
                pw.println("Failed! Invalid argument! Type cmd for help: dumpsys window log");
            }
        }
    }

    protected static void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 Window add or remove:DEBUG_ADD_REMOVE | DEBUG_FOCUS | DEBUG_STARTING_WINDOW | DEBUG_WINDOW_MOVEMENT | DEBUG_FOCUS_LIGHT | DEBUG_TASK_MOVEMENT | DEBUG_STACK");
        pw.println("cmd: dumpsys window log window 0/1");
        pw.println("----------------------------------");
        pw.println("2 Window fresh: DEBUG_LAYOUT | DEBUG_RESIZE | DEBUG_VISIBILITY");
        pw.println("cmd: dumpsys window log fresh 0/1");
        pw.println("----------------------------------");
        pw.println("3 Animation:DEBUG_ANIM");
        pw.println("cmd: dumpsys window log anim 0/1");
        pw.println("----------------------------------");
        pw.println("4 Input envent:DEBUG_INPUT | DEBUG_INPUT_METHOD | DEBUG_DRAG");
        pw.println("cmd: dumpsys window log input 0/1");
        pw.println("----------------------------------");
        pw.println("5 Screen status change:DEBUG_SCREEN_ON | DEBUG_SCREENSHOT | DEBUG_BOOT");
        pw.println("cmd: dumpsys window log screen 0/1");
        pw.println("----------------------------------");
        pw.println("6 App token:DEBUG_TOKEN_MOVEMENT | DEBUG_APP_TRANSITIONS | DEBUG_APP_ORIENTATION");
        pw.println("cmd: dumpsys window log apptoken 0/1");
        pw.println("----------------------------------");
        pw.println("7 Wallpaper change:DEBUG_WALLPAPER | DEBUG_WALLPAPER_LIGH");
        pw.println("cmd: dumpsys window log wallpaper 0/1");
        pw.println("----------------------------------");
        pw.println("8 Config change:DEBUG_ORIENTATION | DEBUG_APP_ORIENTATION | DEBUG_CONFIGURATION");
        pw.println("cmd: dumpsys window log config 0/1");
        pw.println("----------------------------------");
        pw.println("9 Trace surface and window:DEBUG_WINDOW_TRACE");
        pw.println("cmd: dumpsys window log trace 0/1");
        pw.println("----------------------------------");
        pw.println("10 Surface show change:SHOW_SURFACE_ALLOC | SHOW_TRANSACTIONS | SHOW_LIGHT_TRANSACTIONS");
        pw.println("cmd: dumpsys window log surface 0/1");
        pw.println("----------------------------------");
        pw.println("11 Layer change:DEBUG_LAYERS");
        pw.println("cmd: dumpsys window log layer 0/1");
        pw.println("----------------------------------");
        pw.println("12 PhoneWindowManager log:All PhoneWindowManager debug log switch");
        pw.println("cmd: dumpsys window log policy 0/1");
        pw.println("----------------------------------");
        pw.println("13 local log:localLOGV");
        pw.println("cmd: dumpsys window log local 0/1");
        pw.println("----------------------------------");
        pw.println("14 profile change:PROFILE_ORIENTATION");
        pw.println("cmd: dumpsys window log config 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    protected static void dumpDynamicallyLogSwitch(PrintWriter pw, String[] args, int opti) {
        OppoMirrorWmsConfig.initArray();
        boolean surfaceFlagValue = false;
        if (args.length == 1) {
            boolean windowFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_ADD_REMOVE") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_FOCUS") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_FOCUS_LIGHT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_WINDOW_MOVEMENT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_TASK_MOVEMENT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_STARTING_WINDOW") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_STACK");
            boolean freshFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_LAYOUT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_RESIZE") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_VISIBILITY") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_VISIBILITY") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_LAYOUT");
            boolean animFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_ANIM");
            boolean inputFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_INPUT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_INPUT_METHOD") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_DRAG");
            boolean screenFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_SCREEN_ON") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_SCREENSHOT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_BOOT");
            boolean appTokenFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_TOKEN_MOVEMENT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_APP_TRANSITIONS") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_APP_ORIENTATION");
            boolean wallpaperFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_WALLPAPER") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_WALLPAPER_LIGHT");
            boolean configFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_ORIENTATION") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_APP_ORIENTATION") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_ORIENTATION");
            boolean traceFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_WINDOW_TRACE");
            if (OppoMirrorWmsConfig.getFlagValueByName("SHOW_SURFACE_ALLOC") && OppoMirrorWmsConfig.getFlagValueByName("SHOW_TRANSACTIONS") && OppoMirrorWmsConfig.getFlagValueByName("SHOW_LIGHT_TRANSACTIONS")) {
                surfaceFlagValue = true;
            }
            boolean layerFlagValue = OppoMirrorWmsConfig.getFlagValueByName("DEBUG_LAYERS");
            pw.println("  window=" + windowFlagValue + "  fresh=" + freshFlagValue + "  anim=" + animFlagValue + "  input=" + inputFlagValue + "  screen=" + screenFlagValue + "  apptoken=" + appTokenFlagValue + "  wallpaper=" + wallpaperFlagValue + "  config=" + configFlagValue + "  profile=" + OppoMirrorWmsService.PROFILE_ORIENTATION.get((Object) null) + "  trace=" + traceFlagValue + "  surface=" + surfaceFlagValue + "  layer=" + layerFlagValue + "  local=" + OppoMirrorWmsService.localLOGV.get((Object) null));
        } else if (args.length == 2) {
            String tag = args[1];
            if ("window".equals(tag)) {
                StringBuilder sb = new StringBuilder();
                sb.append("  window=");
                sb.append(OppoMirrorWmsConfig.getFlagValueByName("DEBUG_ADD_REMOVE") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_FOCUS") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_FOCUS_LIGHT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_WINDOW_MOVEMENT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_TASK_MOVEMENT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_STARTING_WINDOW") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_STACK"));
                pw.println(sb.toString());
            } else if ("fresh".equals(tag)) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("  fresh=");
                sb2.append(OppoMirrorWmsConfig.getFlagValueByName("DEBUG_LAYOUT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_RESIZE") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_VISIBILITY") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_VISIBILITY") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_LAYOUT"));
                pw.println(sb2.toString());
            } else if ("anim".equals(tag)) {
                pw.println("  anim=" + OppoMirrorWmsConfig.getFlagValueByName("DEBUG_ANIM"));
            } else if ("input".equals(tag)) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("  input=");
                sb3.append(OppoMirrorWmsConfig.getFlagValueByName("DEBUG_INPUT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_INPUT_METHOD") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_DRAG"));
                pw.println(sb3.toString());
            } else if ("screen".equals(tag)) {
                StringBuilder sb4 = new StringBuilder();
                sb4.append("  screen=");
                sb4.append(OppoMirrorWmsConfig.getFlagValueByName("DEBUG_SCREEN_ON") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_SCREENSHOT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_BOOT"));
                pw.println(sb4.toString());
            } else if ("apptoken".equals(tag)) {
                StringBuilder sb5 = new StringBuilder();
                sb5.append("  apptoken=");
                sb5.append(OppoMirrorWmsConfig.getFlagValueByName("DEBUG_TOKEN_MOVEMENT") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_APP_TRANSITIONS") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_APP_ORIENTATION"));
                pw.println(sb5.toString());
            } else if ("wallpaper".equals(tag)) {
                StringBuilder sb6 = new StringBuilder();
                sb6.append("  wallpaper=");
                sb6.append(OppoMirrorWmsConfig.getFlagValueByName("DEBUG_WALLPAPER") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_WALLPAPER_LIGHT"));
                pw.println(sb6.toString());
            } else if ("config".equals(tag)) {
                StringBuilder sb7 = new StringBuilder();
                sb7.append("  config=");
                sb7.append(OppoMirrorWmsConfig.getFlagValueByName("DEBUG_ORIENTATION") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_APP_ORIENTATION") && OppoMirrorWmsConfig.getFlagValueByName("DEBUG_CONFIGURATION"));
                pw.println(sb7.toString());
            } else if ("profile".equals(tag)) {
                pw.println("  profile=" + OppoMirrorWmsService.PROFILE_ORIENTATION.get((Object) null));
            } else if ("trace".equals(tag)) {
                pw.println("  trace=" + OppoMirrorWmsConfig.getFlagValueByName("DEBUG_WINDOW_TRACE"));
            } else if ("surface".equals(tag)) {
                StringBuilder sb8 = new StringBuilder();
                sb8.append("  surface=");
                sb8.append(OppoMirrorWmsConfig.getFlagValueByName("SHOW_SURFACE_ALLOC") && OppoMirrorWmsConfig.getFlagValueByName("SHOW_TRANSACTIONS") && OppoMirrorWmsConfig.getFlagValueByName("SHOW_LIGHT_TRANSACTIONS"));
                pw.println(sb8.toString());
            } else if ("layer".equals(tag)) {
                pw.println("  layer=" + OppoMirrorWmsConfig.getFlagValueByName("DEBUG_LAYERS"));
            } else {
                pw.println("Failed! Invalid argument! Type cmd for help: dumpsys window log");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dynamicGetValue(PrintWriter pw, WindowManagerService wms, String[] args) {
        if (args.length == 1 || args.length == 2) {
            new DumpObject().dumpValue(pw, wms, args.length == 2 ? args[1] : "");
            return;
        }
        pw.println("get_value usage:");
        pw.println("dumpsys window get_value");
        pw.println("or");
        pw.println("dumpsys window get_value variable");
    }
}
