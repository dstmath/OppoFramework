package com.android.server.wm.startingwindow;

import com.android.server.policy.WindowManagerPolicy;

public class ColorStartingSurfaceRemoveRunnable implements Runnable {
    WindowManagerPolicy.StartingSurface startingSurface;

    public ColorStartingSurfaceRemoveRunnable(WindowManagerPolicy.StartingSurface surface) {
        this.startingSurface = surface;
    }

    public void run() {
        WindowManagerPolicy.StartingSurface startingSurface2 = this.startingSurface;
        if (startingSurface2 != null) {
            try {
                startingSurface2.remove();
            } catch (Exception e) {
                ColorStartingWindowUtils.logE("Exception when removing starting window e =: " + e.getMessage());
            }
        }
    }
}
