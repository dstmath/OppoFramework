package com.android.server.wm;

import java.io.PrintWriter;

public class ActivityTaskManagerDynamicLogConfig {
    public static boolean dynamicallyConfigLogTag(PrintWriter pw, String[] args, int opti) {
        if (args == null || args.length != 3) {
            return false;
        }
        String tag = args[1];
        boolean on = "1".equals(args[2]);
        if ("life".equals(tag)) {
            pw.println("dl ams lf, A:" + ActivityTaskManagerDebugConfig.DEBUG_SWITCH + ", " + ActivityTaskManagerDebugConfig.DEBUG_PAUSE + ", " + ActivityTaskManagerDebugConfig.DEBUG_RESULTS);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_SWITCH, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_PAUSE, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_RESULTS, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_CLEANUP, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_RECENTS, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS, on);
            pw.println("dl ams lf, B:" + ActivityTaskManagerDebugConfig.DEBUG_SWITCH + ", " + ActivityTaskManagerDebugConfig.DEBUG_PAUSE + ", " + ActivityTaskManagerDebugConfig.DEBUG_RESULTS);
            return true;
        } else if ("visibility".equals(tag)) {
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_FOCUS, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_VISIBILITY, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_VISIBLE_BEHIND, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_TRANSITION, on);
            return true;
        } else if ("config".equals(tag)) {
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION, on);
            return true;
        } else if ("task".equals(tag)) {
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_TASKS, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_CLEANUP, on);
            return true;
        } else if ("stack".equals(tag)) {
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_STACK, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_APP, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_CONTAINERS, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_IDLE, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_RELEASE, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_SAVED_STATE, on);
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_STATES, on);
            return true;
        } else if ("fstrim".equals(tag)) {
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_FSTRIM, on);
            return true;
        } else if ("permission".equals(tag)) {
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_PERMISSION, on);
            return true;
        } else if (!"junk".equals(tag)) {
            return false;
        } else {
            OppoMirrorActivityTaskManagerDebugConfig.setBooleanValue(OppoMirrorActivityTaskManagerDebugConfig.DEBUG_JUNK, on);
            return true;
        }
    }
}
