package com.color.inner.view;

import android.graphics.Region;
import android.util.Log;
import android.view.ColorBaseLayoutParams;
import android.view.InputEvent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.color.util.ColorTypeCastingHelper;

public class WindowManagerWrapper {
    private static final String TAG = "WindowManagerWrapper";

    public static int getInitialDisplayDensity(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().getInitialDisplayDensity(displayId);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return -1;
        }
    }

    public static int getBaseDisplayDensity(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().getBaseDisplayDensity(displayId);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return -1;
        }
    }

    public static boolean hasNavigationBar(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar(displayId);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return false;
        }
    }

    public static int getDockedStackSide() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return -1;
        }
    }

    public static Region getCurrentImeTouchRegion() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getCurrentImeTouchRegion();
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return null;
        }
    }

    public static boolean injectInputAfterTransactionsApplied(InputEvent ev, int mode) {
        try {
            return WindowManagerGlobal.getWindowManagerService().injectInputAfterTransactionsApplied(ev, mode);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return false;
        }
    }

    public static class LayoutParamsWrapper {
        public static final int DEFAULT_STATUS_BAR = 0;
        public static final int DISABLE_STATUS_BAR = 1;
        public static final int ENABLE_STATUS_BAR = 2;
        public static final int IGNORE_HOME_KEY = 2;
        public static final int IGNORE_HOME_MENU_KEY = 1;
        public static final int IGNORE_MENU_KEY = 3;
        public static final int UNSET_ANY_KEY = 0;

        public static int getStatusBarStateByWindowManager(WindowManager.LayoutParams params) {
            try {
                return WindowManagerWrapper.typeCasting(params).isDisableStatusBar;
            } catch (Exception e) {
                Log.e(WindowManagerWrapper.TAG, e.toString());
                return -1;
            }
        }

        public static void setStatusBarStateByWindowManager(WindowManager.LayoutParams params, int disableStatusBar) {
            try {
                WindowManagerWrapper.typeCasting(params).isDisableStatusBar = disableStatusBar;
            } catch (Exception e) {
                Log.e(WindowManagerWrapper.TAG, e.toString());
            }
        }

        public static int getHomeAndMenuKeyState(WindowManager.LayoutParams params) {
            try {
                return WindowManagerWrapper.typeCasting(params).ignoreHomeMenuKey;
            } catch (Exception e) {
                Log.e(WindowManagerWrapper.TAG, e.toString());
                return -1;
            }
        }

        public static void setHomeAndMenuKeyState(WindowManager.LayoutParams params, int ignoreHomeMenuKey) {
            try {
                WindowManagerWrapper.typeCasting(params).ignoreHomeMenuKey = ignoreHomeMenuKey;
            } catch (Exception e) {
                Log.e(WindowManagerWrapper.TAG, e.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public static ColorBaseLayoutParams typeCasting(WindowManager.LayoutParams params) {
        return (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, params);
    }
}
