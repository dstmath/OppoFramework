package com.android.server.wm.startingwindow;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Debug;
import android.os.SystemProperties;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ColorToastHelper;
import java.util.Set;

public class ColorStartingWindowUtils {
    private static final boolean DBG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static final boolean DEBUG = (false | DBG);
    private static final String TAG = "ColorStartingWindowManager";

    public static void logD(String content) {
        if (DEBUG) {
            Slog.d(TAG, content);
        }
    }

    public static void logE(String content) {
        if (DEBUG) {
            Slog.e(TAG, content);
        }
    }

    public static void logBackTrace(String method) {
        logD(method + " called by \n" + Debug.getCallers(12, " "));
    }

    public static void traceBegin(String method) {
        if (DEBUG) {
            Trace.traceBegin(32, method);
        }
    }

    public static void traceEnd() {
        if (DEBUG) {
            Trace.traceEnd(32);
        }
    }

    public static boolean isSystemApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (ColorStartingWindowContants.SYSTEM_APPS.contains(packageName)) {
            return true;
        }
        if (packageName.startsWith(ColorToastHelper.PKG_PERFIX_OPPO) || packageName.startsWith("com.heytap.") || packageName.startsWith(ColorToastHelper.PKG_PERFIX_NEARME) || packageName.startsWith(ColorToastHelper.PKG_PERFIX_COLOR)) {
            return true;
        }
        return false;
    }

    public static boolean supportSnapshotSplash(String packageName) {
        return !isSplashBlackPackageStartFromLauncher(packageName) && (isSystemApp(packageName) || isWhiteThirdApp(packageName));
    }

    public static boolean isWhiteThirdApp(String packageName) {
        if (ColorStartingWindowRUSHelper.isSupportSplashSnapshotForAllApps()) {
            return true;
        }
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(8);
        if (set != null) {
            return set.contains(packageName);
        }
        return ColorStartingWindowContants.SPLASH_SNAPSHOT_WHITE_THIRD_PARTY_APP.contains(packageName);
    }

    public static boolean isSplashBlackPackageStartFromLauncher(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return true;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(0);
        if (set != null) {
            return set.contains(packageName);
        }
        return ColorStartingWindowContants.SPLASH_BLACK_LIST_PACKAGES_START_FROM_LAUNCHER.contains(packageName);
    }

    public static boolean isSplashBlackTokenStartFromLauncher(String token) {
        if (TextUtils.isEmpty(token)) {
            return true;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(1);
        if (set != null) {
            return set.contains(token);
        }
        return ColorStartingWindowContants.SPLASH_BLACK_LIST_TOKENS_START_FROM_LAUNCHER.contains(token);
    }

    public static boolean isSplashBlackPackageForSystemApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return true;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(2);
        if (set != null) {
            return set.contains(packageName);
        }
        return ColorStartingWindowContants.SPLASH_BLACK_LIST_PACKAGES_FOR_SYSTEM_APPS.contains(packageName);
    }

    public static boolean isSplashBlackTokenForSystemApp(String token) {
        if (TextUtils.isEmpty(token)) {
            return true;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(3);
        if (set != null) {
            return set.contains(token);
        }
        return ColorStartingWindowContants.SPLASH_BLACK_LIST_TOKENS_FOR_SYSTEM_APPS.contains(token);
    }

    public static boolean isTaskSnapshotBlackPackage(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(6);
        if (set != null) {
            return set.contains(packageName);
        }
        return ColorStartingWindowContants.TASK_SNAPSHOT_BLACK_PACKAGES.contains(packageName);
    }

    public static boolean isTaskSnapshotBlackToken(String token) {
        if (TextUtils.isEmpty(token)) {
            return false;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(7);
        if (set != null) {
            return set.contains(token);
        }
        return ColorStartingWindowContants.TASK_SNAPSHOT_BLACK_TOKENS.contains(token);
    }

    public static boolean forceClearSplashWindowForPackage(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(4);
        if (set != null) {
            return set.contains(packageName);
        }
        return ColorStartingWindowContants.FORCE_CLEAR_SPLASH_PACKAGES_START_FROM_LAUNCHER.contains(packageName);
    }

    public static boolean forceClearSplashWindowForToken(String token) {
        if (TextUtils.isEmpty(token)) {
            return false;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(5);
        if (set != null) {
            return set.contains(token);
        }
        return ColorStartingWindowContants.FORCE_CLEAR_SPLASH_TOKENS.contains(token);
    }

    public static boolean forUSeColorDrawableForSplashWindow(String packageName) {
        Set<String> set;
        if (!TextUtils.isEmpty(packageName) && (set = ColorStartingWindowRUSHelper.getStartingWindowListByType(9)) != null) {
            return set.contains(packageName);
        }
        return false;
    }

    public static boolean removeStartingWindowImmediately(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(10);
        if (set != null) {
            return !set.contains(packageName);
        }
        return !ColorStartingWindowContants.STARTING_WINDOW_EXIT_LONG_PACKAGE.contains(packageName);
    }

    public static boolean blockSplashSnapshotWhenProcessRunning(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return true;
        }
        return ColorStartingWindowContants.SPLASH_SNAPSHOT_BLACK_PACKAGES_FOR_PROCESSRUNNING.contains(packageName);
    }

    public static boolean isOppoLauncher(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        return "com.oppo.launcher".equals(packageName);
    }

    public static boolean supportSnapshotSplashForCallingApp(String packageName) {
        return "com.oppo.launcher".equals(packageName);
    }

    public static boolean clearStartingWindowWhenDiffOrientation(String packageName) {
        Set<String> set = ColorStartingWindowRUSHelper.getStartingWindowListByType(11);
        if (set != null) {
            return set.contains(packageName);
        }
        return ColorStartingWindowContants.SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION.contains(packageName);
    }

    public static boolean checkBitmapValid(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == 0 || height == 0) {
            return false;
        }
        int heightSpacing = Math.max(1, height / 30);
        int left = (int) (((float) width) * 0.33f);
        int right = (int) (((float) width) * 0.67f);
        int widthSpacing = Math.max(1, (right - left) / 15);
        for (int i = left; i < right; i += widthSpacing) {
            for (int j = 0; j < height; j += heightSpacing) {
                int pixel = bitmap.getPixel(i, j);
                if (Color.alpha(pixel) == 0 || Color.luminance(pixel) > 5.0E-4f) {
                    return true;
                }
            }
        }
        return false;
    }
}
