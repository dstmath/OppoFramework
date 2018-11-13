package com.color.view;

import android.graphics.Rect;
import android.os.RemoteException;
import android.text.Spanned;
import android.view.IOppoWindowManagerImpl;
import android.view.WindowManagerGlobal;

public final class ColorWindowManager {
    public static final int NAVIGATION_BAR_HIDE_AUTO = 2;
    public static final int NAVIGATION_BAR_HIDE_MANUAL = 1;
    public static final int NAVIGATION_BAR_HIDE_PERMANENT = 3;
    public static final int NAVIGATION_BAR_SHOW_TRANSIENT = 0;
    public static final String TAG = "ColorWindowManager";

    public static class LayoutParams extends android.view.WindowManager.LayoutParams {
        public static final int COLOROS_FIRST_SYSTEM_WINDOW = 300;
        public static final int TYPE_DISPLAY_FULL_SCREEN = 2100;
        public static final int TYPE_DRAG_SCREEN_BACKGROUND = 2301;
        public static final int TYPE_DRAG_SCREEN_FOREGROUND = 2302;
        public static final int TYPE_POWERVIEW = 2300;
        public static final int TYPE_SYSTEM_GESTURE_TRANSITION_VIEW = 2312;
        public static final int TYPE_SYSTEM_LONGSHOT = 2303;
        public static final int TYPE_SYSTEM_LONGSHOT_EDIT = 2307;
        public static final int TYPE_SYSTEM_LONGSHOT_GUIDE = 2306;
        public static final int TYPE_SYSTEM_LONGSHOT_MASK = 2308;
        public static final int TYPE_SYSTEM_LONGSHOT_POPUP = 2310;
        public static final int TYPE_SYSTEM_LONGSHOT_SCROLL = 2304;
        public static final int TYPE_SYSTEM_LONGSHOT_TOAST = 2305;
        public static final int TYPE_SYSTEM_LONGSHOT_VIEW = 2309;
        public static final int TYPE_SYSTEM_SCREENASSISTANT_VIEW = 2311;

        public static boolean isFullscreen(android.view.WindowManager.LayoutParams attrs) {
            if (attrs.x == 0 && attrs.y == 0 && attrs.width == -1 && attrs.height == -1) {
                return true;
            }
            return false;
        }

        public static boolean isForceFullScreen(int type) {
            boolean z = true;
            switch (type) {
                case TYPE_SYSTEM_LONGSHOT_POPUP /*2310*/:
                    return true;
                default:
                    int offset = (type - 2000) - 300;
                    if (offset < 0 || offset > 2) {
                        z = false;
                    }
                    return z;
            }
        }

        public static boolean isLongshotWindow(int type) {
            int offset = (type - 2000) - 300;
            if (offset < 3 || offset > 10) {
                return false;
            }
            return true;
        }

        public static boolean isSpecialAppWindow(android.view.WindowManager.LayoutParams attrs) {
            if (isFullscreen(attrs) && isLongshotWindow(attrs.type)) {
                return true;
            }
            return false;
        }
    }

    @Deprecated
    public static void getFocusedWindowFrame(Rect frame) {
        try {
            new IOppoWindowManagerImpl().getFocusedWindowFrame(frame);
        } catch (RemoteException e) {
        }
    }

    @Deprecated
    public static boolean isInputShow() {
        boolean result = false;
        try {
            return new IOppoWindowManagerImpl().isInputShow();
        } catch (RemoteException e) {
            return result;
        }
    }

    @Deprecated
    public static int getLongshotSurfaceLayer() {
        int layer = 0;
        try {
            return new IOppoWindowManagerImpl().getLongshotSurfaceLayer();
        } catch (RemoteException e) {
            return layer;
        }
    }

    @Deprecated
    public static int getLongshotSurfaceLayerByType(int type) {
        int layer = 0;
        try {
            return new IOppoWindowManagerImpl().getLongshotSurfaceLayerByType(type);
        } catch (RemoteException e) {
            return layer;
        }
    }

    public static void setNoMoveAnimation(android.view.WindowManager.LayoutParams lp, boolean noAnim) {
        if (noAnim) {
            lp.privateFlags |= 64;
        } else {
            lp.privateFlags &= -65;
        }
    }

    public static void setNavigationBarColor(android.view.WindowManager.LayoutParams lp, int color, boolean update) {
        if (update) {
            lp.mColorLayoutParams.setUpdateNavigationBar(update);
            lp.mColorLayoutParams.setNavigationBarColor(color);
        } else if (isColorLight(color)) {
            lp.navigationBarVisibility |= Integer.MIN_VALUE;
            lp.navigationBarColor = color;
        } else {
            lp.navigationBarVisibility &= Integer.MAX_VALUE;
            lp.navigationBarColor = color;
        }
    }

    public static boolean updateDarkNavigationBar(android.view.WindowManager.LayoutParams lp) {
        if (lp.mColorLayoutParams.hasNavigationBar() && lp.mColorLayoutParams.isUpdateNavigationBar() && !isColorLight(lp.mColorLayoutParams.getNavigationBarColor())) {
            return true;
        }
        return false;
    }

    public static void setHasStatusBar(android.view.WindowManager.LayoutParams lp, boolean value) {
        lp.mColorLayoutParams.setHasStatusBar(value);
    }

    public static void setHasNavigationBar(android.view.WindowManager.LayoutParams lp, boolean value) {
        lp.mColorLayoutParams.setHasNavigationBar(value);
    }

    public static void setCustomSystemBar(android.view.WindowManager.LayoutParams lp, boolean value) {
        lp.mColorLayoutParams.setCustomSystemBar(value);
    }

    public static void setSystemAppWindow(android.view.WindowManager.LayoutParams lp, boolean value) {
        lp.mColorLayoutParams.setSystemAppWindow(value);
    }

    public static void setFullScreenWindow(android.view.WindowManager.LayoutParams lp, boolean value) {
        lp.mColorLayoutParams.setFullScreenWindow(value);
    }

    public static boolean skipSystemUiVisibility(android.view.WindowManager.LayoutParams lp) {
        return lp.mColorLayoutParams.getSkipSystemUiVisibility();
    }

    public static void setSkipSystemUiVisibility(android.view.WindowManager.LayoutParams lp, boolean value) {
        lp.mColorLayoutParams.setSkipSystemUiVisibility(value);
    }

    public static boolean isUseLastStatusBarTint(android.view.WindowManager.LayoutParams lp) {
        return lp.mColorLayoutParams.isUseLastStatusBarTint();
    }

    public static void setUseLastStatusBarTint(android.view.WindowManager.LayoutParams lp, boolean value) {
        lp.mColorLayoutParams.setUseLastStatusBarTint(value);
    }

    public static boolean updateSpecialSystemBar(android.view.WindowManager.LayoutParams lp) {
        if (isUseLastStatusBarTint(lp) || updateDarkNavigationBar(lp)) {
            return true;
        }
        return false;
    }

    @Deprecated
    public static boolean isInMultiWindowMode() {
        int dockSide = -1;
        try {
            dockSide = WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
        }
        return -1 != dockSide;
    }

    public static boolean isColorLight(int color) {
        if (color == 0) {
            return false;
        }
        int alpha = (-16777216 & color) >>> 24;
        if (((int) (((((float) ((Spanned.SPAN_PRIORITY & color) >>> 16)) * 0.299f) + (((float) ((65280 & color) >>> 8)) * 0.587f)) + (((float) (color & 255)) * 0.114f))) <= 192 || alpha <= 156) {
            return false;
        }
        return true;
    }
}
