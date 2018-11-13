package com.android.server.policy;

import android.os.Bundle;
import android.view.WindowManagerPolicy.WindowState;

public class ColorShotScreenHelper extends OppoShotScreenHelper {
    public void shotScreen(WindowState statusBar, WindowState navigationBar, int direction, boolean isGlobalActionVisible, boolean isLandscape) {
        boolean z = false;
        synchronized (this.mScreenshotLock) {
            Bundle extras = new Bundle();
            extras.putString("screenshot_source", "ThreeFingers");
            extras.putBoolean("statusbar_visible", statusBar != null ? statusBar.isVisibleLw() : false);
            String str = "navigationbar_visible";
            if (navigationBar != null) {
                z = navigationBar.isVisibleLw();
            }
            extras.putBoolean(str, z);
            extras.putBoolean("global_action_visible", isGlobalActionVisible);
            extras.putBoolean("screenshot_orientation", isLandscape);
            extras.putInt("screenshot_direction", direction);
            PhoneWindowManager.takeScreenshot(this.mContext, extras);
        }
    }
}
