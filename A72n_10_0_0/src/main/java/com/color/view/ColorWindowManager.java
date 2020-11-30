package com.color.view;

import android.os.RemoteException;
import android.view.ColorBaseLayoutParams;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.color.util.ColorTypeCastingHelper;
import com.color.view.IColorWindowManagerConstans;

public final class ColorWindowManager implements IColorWindowManagerConstans {
    public static final String TAG = "ColorWindowManager";

    public static class LayoutParams extends IColorWindowManagerConstans.BaseLayoutParams {
    }

    public static void setNoMoveAnimation(WindowManager.LayoutParams lp, boolean noAnim) {
        if (noAnim) {
            lp.privateFlags |= 64;
        } else {
            lp.privateFlags &= -65;
        }
    }

    public static void setNavigationBarColor(WindowManager.LayoutParams lp, int color, boolean update) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp == null) {
            return;
        }
        if (update) {
            cblp.mColorLayoutParams.setUpdateNavigationBar(update);
            cblp.mColorLayoutParams.setNavigationBarColor(color);
        } else if (isColorLight(color)) {
            cblp.navigationBarVisibility |= Integer.MIN_VALUE;
            cblp.navigationBarColor = color;
        } else {
            cblp.navigationBarVisibility &= Integer.MAX_VALUE;
            cblp.navigationBarColor = color;
        }
    }

    public static boolean updateDarkNavigationBar(WindowManager.LayoutParams lp) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp == null) {
            return true;
        }
        if (cblp.mColorLayoutParams.hasNavigationBar() && cblp.mColorLayoutParams.isUpdateNavigationBar() && !isColorLight(cblp.mColorLayoutParams.getNavigationBarColor())) {
            return true;
        }
        return false;
    }

    public static void setHasStatusBar(WindowManager.LayoutParams lp, boolean value) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            cblp.mColorLayoutParams.setHasStatusBar(value);
        }
    }

    public static void setHasNavigationBar(WindowManager.LayoutParams lp, boolean value) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            cblp.mColorLayoutParams.setHasNavigationBar(value);
        }
    }

    public static void setCustomSystemBar(WindowManager.LayoutParams lp, boolean value) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            cblp.mColorLayoutParams.setCustomSystemBar(value);
        }
    }

    public static void setSystemAppWindow(WindowManager.LayoutParams lp, boolean value) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            cblp.mColorLayoutParams.setSystemAppWindow(value);
        }
    }

    public static void setFullScreenWindow(WindowManager.LayoutParams lp, boolean value) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            cblp.mColorLayoutParams.setFullScreenWindow(value);
        }
    }

    public static boolean skipSystemUiVisibility(WindowManager.LayoutParams lp) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            return cblp.mColorLayoutParams.getSkipSystemUiVisibility();
        }
        return false;
    }

    public static void setSkipSystemUiVisibility(WindowManager.LayoutParams lp, boolean value) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            cblp.mColorLayoutParams.setSkipSystemUiVisibility(value);
        }
    }

    public static boolean isUseLastStatusBarTint(WindowManager.LayoutParams lp) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            return cblp.mColorLayoutParams.isUseLastStatusBarTint();
        }
        return false;
    }

    public static void setUseLastStatusBarTint(WindowManager.LayoutParams lp, boolean value) {
        ColorBaseLayoutParams cblp = typeCast(lp);
        if (cblp != null) {
            cblp.mColorLayoutParams.setUseLastStatusBarTint(value);
        }
    }

    public static boolean updateSpecialSystemBar(WindowManager.LayoutParams lp) {
        if (!isUseLastStatusBarTint(lp) && !updateDarkNavigationBar(lp)) {
            return false;
        }
        return true;
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
        if (((int) ((((float) ((16711680 & color) >>> 16)) * 0.299f) + (((float) ((65280 & color) >>> 8)) * 0.587f) + (((float) (color & 255)) * 0.114f))) <= 192 || alpha <= 156) {
            return false;
        }
        return true;
    }

    private static ColorBaseLayoutParams typeCast(WindowManager.LayoutParams lp) {
        return (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, lp);
    }
}
