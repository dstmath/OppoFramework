package com.mediatek.server.wm;

import android.view.WindowManager;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;

public class WindowManagerDebugger {
    public static final String TAG = "WindowManagerDebugger";
    public static boolean WMS_DEBUG_ENG = false;
    public static boolean WMS_DEBUG_LOG_OFF = false;
    public static boolean WMS_DEBUG_USER = false;

    public void runDebug(PrintWriter pw, String[] args, int opti) {
    }

    public void debugInterceptKeyBeforeQueueing(String tag, int keycode, boolean interactive, boolean keyguardActive, int policyFlags, boolean down, boolean canceled, boolean isWakeKey, boolean screenshotChordVolumeDownKeyTriggered, int result, boolean useHapticFeedback, boolean isInjected) {
    }

    public void debugApplyPostLayoutPolicyLw(String tag, WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs, WindowManagerPolicy.WindowState mTopFullscreenOpaqueWindowState, WindowManagerPolicy.WindowState attached, WindowManagerPolicy.WindowState imeTarget, boolean dreamingLockscreen, boolean showingDream) {
    }

    public void debugLayoutWindowLw(String tag, int adjust, int type, int fl, boolean canHideNavigationBar, int sysUiFl) {
    }

    public void debugGetOrientation(String tag, boolean displayFrozen, int lastWindowForcedOrientation, int lastKeyguardForcedOrientation) {
    }

    public void debugGetOrientingWindow(String tag, WindowManagerPolicy.WindowState w, WindowManager.LayoutParams attrs, boolean isVisible, boolean policyVisibilityAfterAnim, int policyVisibility, boolean destroying) {
    }

    public void debugPrepareSurfaceLocked(String tag, boolean isWallpaper, WindowManagerPolicy.WindowState win, boolean wallpaperVisible, boolean isOnScreen, int policyVisibility, boolean hasSurface, boolean destroying, boolean lastHidden) {
    }

    public void debugRelayoutWindow(String tag, WindowManagerPolicy.WindowState win, int originType, int changeType) {
    }

    public void debugInputAttr(String tag, WindowManager.LayoutParams attrs) {
    }

    public void debugViewVisibility(String tag, WindowManagerPolicy.WindowState win, int viewVisibility, int oldVisibility, boolean focusMayChange) {
    }
}
