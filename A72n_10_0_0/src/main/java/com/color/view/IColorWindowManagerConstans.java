package com.color.view;

import android.drm.DrmManagerClient;
import android.view.WindowManager;

public interface IColorWindowManagerConstans {
    public static final int NAVIGATION_BAR_HIDE_AUTO = 2;
    public static final int NAVIGATION_BAR_HIDE_MANUAL = 1;
    public static final int NAVIGATION_BAR_HIDE_PERMANENT = 3;
    public static final int NAVIGATION_BAR_SHOW_TRANSIENT = 0;
    public static final int PRIVATE_FLAG_FORCE_CLEAR_STARING_WINDOW = 1073741824;
    public static final int PRIVATE_FLAG_FORCE_SHOW_SPLASH_WINDOW = 268435456;
    public static final int PRIVATE_FLAG_NAVIGATION_BAR_LIGHT = Integer.MIN_VALUE;
    public static final int PRIVATE_FLAG_SUGGEST_SHOW_SPLASH_WINDOW = 536870912;

    public static class BaseLayoutParams extends WindowManager.LayoutParams {
        public static final int COLOROS_FIRST_SYSTEM_WINDOW = 300;
        public static final int TYPE_DISPLAY_FULL_SCREEN = 2100;
        public static final int TYPE_DRAG_SCREEN_BACKGROUND = 2301;
        public static final int TYPE_DRAG_SCREEN_FOREGROUND = 2302;
        public static final int TYPE_OPPO_APP_BACK_ANIMATION_WINDOW = 2316;
        public static final int TYPE_POWERVIEW = 2300;
        public static final int TYPE_SYSTEM_EDGE_PANEL = 2314;
        public static final int TYPE_SYSTEM_GAME_HUNG_UP_VIEW = 2313;
        public static final int TYPE_SYSTEM_GESTURE_TRANSITION_VIEW = 2312;
        public static final int TYPE_SYSTEM_LONGSHOT = 2303;
        public static final int TYPE_SYSTEM_LONGSHOT_EDIT = 2307;
        public static final int TYPE_SYSTEM_LONGSHOT_GUIDE = 2306;
        public static final int TYPE_SYSTEM_LONGSHOT_MASK = 2308;
        public static final int TYPE_SYSTEM_LONGSHOT_POPUP = 2310;
        public static final int TYPE_SYSTEM_LONGSHOT_SCROLL = 2304;
        public static final int TYPE_SYSTEM_LONGSHOT_TOAST = 2305;
        public static final int TYPE_SYSTEM_LONGSHOT_VIEW = 2309;
        public static final int TYPE_SYSTEM_ONSCREEN_FINGERPRINT = 2315;
        public static final int TYPE_SYSTEM_SCREENASSISTANT_VIEW = 2311;
        public static final int TYPE_SYSTEM_SCREENSHOT_LONGSHOT = 2317;
        public static final int TYPE_ZOOM_CONTROLLER = 2318;

        public static boolean isFullscreen(WindowManager.LayoutParams attrs) {
            return attrs.x == 0 && attrs.y == 0 && attrs.width == -1 && attrs.height == -1;
        }

        public static boolean isForceFullScreen(int type) {
            if (type == 2310 || type == 2318) {
                return true;
            }
            int offset = (type + DrmManagerClient.ERROR_UNKNOWN) - 300;
            if (offset < 0 || offset > 2) {
                return false;
            }
            return true;
        }

        public static boolean isLongshotWindow(int type) {
            int offset = (type + DrmManagerClient.ERROR_UNKNOWN) - 300;
            return (offset >= 3 && offset <= 10) || offset == 17;
        }

        public static boolean isSpecialAppWindow(WindowManager.LayoutParams attrs) {
            if (!isFullscreen(attrs) || !isLongshotWindow(attrs.type)) {
                return false;
            }
            return true;
        }
    }
}
