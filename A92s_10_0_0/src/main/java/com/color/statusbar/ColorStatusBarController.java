package com.color.statusbar;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.util.Singleton;
import android.view.OppoWindowManager;
import android.view.View;
import android.view.WindowInsets;

public class ColorStatusBarController {
    public static final int NAVIGATION_BAR = 1;
    public static final int OPPO_NAVIGATION_BAR_COLOR = -1;
    public static final int STATUS_BAR = 0;
    private static final String TAG = ColorStatusBarController.class.getSimpleName();
    private static final Singleton<ColorStatusBarController> sColorStatusBarControllerSingleton = new Singleton<ColorStatusBarController>() {
        /* class com.color.statusbar.ColorStatusBarController.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public ColorStatusBarController create() {
            return new ColorStatusBarController();
        }
    };
    private int mNavigationGuardColor;
    private OppoWindowManager mWm;

    private ColorStatusBarController() {
        this.mNavigationGuardColor = 0;
        this.mWm = null;
        this.mWm = new OppoWindowManager();
    }

    public static ColorStatusBarController getInstance() {
        return sColorStatusBarControllerSingleton.get();
    }

    public int caculateSystemBarColor(String pkg, String activityName, int themeColor, int bar) {
        int barColor = getBarColorFromAdaptation(pkg, activityName, bar);
        if (barColor != 0) {
            return barColor;
        }
        return (bar != 1 || !isUseDefaultNavigationBarColor(pkg, activityName)) ? themeColor : barColor;
    }

    public int getBottomInset(boolean isImeWindow, int normalBottomInset, WindowInsets insets) {
        if (isImeWindow) {
            return insets.getSystemWindowInsetBottom();
        }
        return normalBottomInset;
    }

    public void updateColorNavigationGuardColor(Context context, int color, int windowColor, View decor) {
        this.mNavigationGuardColor = getImeBgColor(context, color, windowColor);
    }

    public int getNavigationGuardColor() {
        return this.mNavigationGuardColor;
    }

    public boolean shouldDrawSystemBarBackgroundColor(boolean isStartingWindow, String pkg) {
        return false;
    }

    public boolean shouldDrawLegacyNavigationBarBackground() {
        return false;
    }

    public boolean isSupportNavBarScrim(Context context) {
        String pkg = context.getPackageName();
        if (pkg == null) {
            return false;
        }
        if (pkg.contains("com.google.android") || pkg.contains("com.shazam.android")) {
            return true;
        }
        return false;
    }

    private int getImeBgColor(Context context, int color, int windowColor) {
        if (windowColor != -16777216 && windowColor != -1) {
            return windowColor;
        }
        String str = TAG;
        Log.d(str, "getImeBgColor: " + color);
        return color;
    }

    private int getBarColorFromAdaptation(String pkg, String activityName, int bar) {
        try {
            if (this.mWm == null) {
                return 0;
            }
            if (bar == 0) {
                return this.mWm.getStatusBarColorFromAdaptation(pkg, activityName);
            }
            if (bar == 1) {
                return this.mWm.getNavBarColorFromAdaptation(pkg, activityName);
            }
            return 0;
        } catch (RemoteException e) {
            String str = TAG;
            Log.w(str, "getNavBarColorFromAdaptation " + e);
            return 0;
        }
    }

    private boolean isUseDefaultNavigationBarColor(String pkg, String activityName) {
        if ("com.google.android.setupwizard".equals(pkg) || "com.google.android.apps.gsa.staticplugins.opa.errorui.OpaErrorActivity".equals(activityName) || "com.google.android.apps.gsa.velour.dynamichosts.TransparentVelvetDynamicHostActivity".equals(activityName) || "com.google.android.finsky.setup.VpaSelectionOptionalStepActivity".equals(activityName) || "com.google.android.finsky.setupui.VpaSelectionOptionalStepActivity".equals(activityName) || "com.google.android.apps.gsa.staticplugins.opa.setupwizard.SetupWizardActivity".equals(activityName) || "com.google.android.play.games".equals(pkg) || "com.google.android.googlequicksearchbox".equals(pkg) || "com.google.android.apps.restore".equals(pkg) || "com.google.android.gms".equals(pkg)) {
            return true;
        }
        return false;
    }

    private boolean isColorDark(int color) {
        if (color == 0) {
            return false;
        }
        int alpha = (-16777216 & color) >>> 24;
        if (((int) ((((double) ((16711680 & color) >>> 16)) * 0.299d) + (((double) ((65280 & color) >>> 8)) * 0.587d) + (((double) (color & 255)) * 0.114d))) <= 150 || alpha <= 156) {
            return false;
        }
        return true;
    }
}
