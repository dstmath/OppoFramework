package com.android.server.wm;

import android.content.Context;
import android.os.Handler;
import com.android.internal.util.ScreenshotHelper;
import com.android.server.statusbar.StatusBarManagerInternal;

public interface IColorDisplayPolicyInner {
    default Context getContext() {
        return null;
    }

    default WindowManagerService getWindowManagerService() {
        return null;
    }

    default ScreenshotHelper getScreenshotHelper() {
        return null;
    }

    default Object getServiceAcquireLock() {
        return null;
    }

    default WindowState getStatusBar() {
        return null;
    }

    default WindowState getNavigationBar() {
        return null;
    }

    default int[] getNavigationBarHeightForRotationDefault() {
        return null;
    }

    default WindowState getTopFullscreenOpaqueWindowState() {
        return null;
    }

    default StatusBarManagerInternal getStatusBarManagerInternal() {
        return null;
    }

    default Handler getHandler() {
        return null;
    }
}
