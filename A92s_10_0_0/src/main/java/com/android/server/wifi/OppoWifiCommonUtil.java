package com.android.server.wifi;

import android.app.StatusBarManager;
import android.content.Context;

class OppoWifiCommonUtil {
    public static void disableStatusBar(Context context, boolean disable) {
        StatusBarManager statusBar;
        if (context != null && (statusBar = (StatusBarManager) context.getSystemService("statusbar")) != null) {
            int state = 0;
            if (disable) {
                state = 0 | 65536;
            }
            statusBar.disable(state);
        }
    }
}
