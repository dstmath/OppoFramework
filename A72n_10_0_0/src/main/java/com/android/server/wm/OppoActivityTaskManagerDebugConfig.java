package com.android.server.wm;

import android.os.SystemProperties;

public class OppoActivityTaskManagerDebugConfig {
    public static boolean DEBUG_AMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
}
