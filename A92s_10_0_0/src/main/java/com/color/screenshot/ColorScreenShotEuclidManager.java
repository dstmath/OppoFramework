package com.color.screenshot;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ColorBaseLayoutParams;
import android.view.ColorLongshotWindowManager;
import android.view.IColorLongshotWindowManager;
import android.view.WindowManager;
import com.color.util.ColorLog;
import com.color.util.ColorTypeCastingHelper;
import com.color.view.ColorWindowManager;

public class ColorScreenShotEuclidManager implements IColorScreenShotEuclidManager {
    private static volatile ColorScreenShotEuclidManager sInstance = null;

    public static ColorScreenShotEuclidManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorScreenShotEuclidManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorScreenShotEuclidManager();
                }
            }
        }
        return sInstance;
    }

    ColorScreenShotEuclidManager() {
    }

    public boolean updateSpecialSystemBar(WindowManager.LayoutParams lp) {
        if (!ColorWindowManager.isUseLastStatusBarTint(lp) && !ColorWindowManager.updateDarkNavigationBar(lp)) {
            return false;
        }
        return true;
    }

    public boolean skipSystemUiVisibility(WindowManager.LayoutParams lp) {
        ColorBaseLayoutParams cbp = (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, lp);
        boolean result = false;
        if (cbp != null) {
            result = cbp.mColorLayoutParams.getSkipSystemUiVisibility();
        }
        if (result && ColorLongshotDump.DBG) {
            Log.d("LongshotDump", "updateSystemUiVisibilityLw : skip " + result);
        }
        return result;
    }

    public boolean isSpecialAppWindow(boolean appWindow, WindowManager.LayoutParams attrs) {
        if (!ColorWindowManager.LayoutParams.isFullscreen(attrs) || !ColorWindowManager.LayoutParams.isLongshotWindow(attrs.type)) {
            return appWindow;
        }
        return true;
    }

    public boolean takeScreenshot(Context context, int screenshotType, boolean hasStatus, boolean hasNav, Handler handler) {
        ColorScreenshotManager sm = ColorLongshotUtils.getScreenshotManager(context);
        if (sm == null || !sm.isScreenshotSupported()) {
            StringBuilder sb = new StringBuilder();
            sb.append("takeScreenshot : FAIL sm = ");
            sb.append(sm);
            sb.append(" , ");
            sb.append(sm == null ? false : sm.isScreenshotSupported());
            ColorLog.e("LongshotDump", sb.toString());
            return false;
        }
        Bundle extras = new Bundle();
        if (handler instanceof IColorScreenshotHelper) {
            IColorScreenshotHelper helper = (IColorScreenshotHelper) handler;
            extras.putString("screenshot_source", helper.getSource());
            extras.putBoolean("global_action_visible", helper.isGlobalAction());
        }
        extras.putBoolean("statusbar_visible", hasStatus);
        extras.putBoolean("navigationbar_visible", hasNav);
        extras.putBoolean("screenshot_orientation", isLandscape(context));
        sm.takeScreenshot(extras);
        ColorLog.d("LongshotDump", "takeScreenshot : PASS");
        return true;
    }

    public Handler getScreenShotHandler(Looper looper) {
        return new ColorGlobalActionHandler(Looper.getMainLooper());
    }

    public IColorLongshotWindowManager getIColorLongshotWindowManager() {
        return new ColorLongshotWindowManager();
    }

    private boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }
}
