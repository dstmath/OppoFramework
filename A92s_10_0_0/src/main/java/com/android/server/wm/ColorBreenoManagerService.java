package com.android.server.wm;

import android.os.Bundle;

public class ColorBreenoManagerService implements IColorBreenoManager {
    public static final String TAG = "ColorBreenoManagerService";
    private static final Object mLock = new Object();
    private static ColorBreenoManagerService sInstance = null;
    private IColorWindowManagerServiceEx mColorWmsEx = null;
    private WindowManagerService mWms = null;

    public static ColorBreenoManagerService getInstance() {
        ColorBreenoManagerService colorBreenoManagerService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new ColorBreenoManagerService();
            }
            colorBreenoManagerService = sInstance;
        }
        return colorBreenoManagerService;
    }

    public void init(IColorWindowManagerServiceEx wmsEx) {
        if (wmsEx != null) {
            this.mColorWmsEx = wmsEx;
            this.mWms = wmsEx.getWindowManagerService();
            ColorDragWindowHelper.init(this.mWms.mContext, this.mWms);
        }
    }

    public boolean isBreeno() {
        return ColorDragWindowHelper.getInstance().isBreeno();
    }

    public boolean inDragWindowing() {
        return ColorDragWindowHelper.getInstance().inDragWindowing();
    }

    public boolean hasColorDragWindowAnimation() {
        return ColorDragWindowHelper.getInstance().mColorDragWindowAnimation != null;
    }

    public boolean stepAnimation(long currentTime) {
        if (ColorDragWindowHelper.getInstance() != null) {
            return ColorDragWindowHelper.getInstance().mColorDragWindowAnimation.stepAnimation(currentTime);
        }
        return false;
    }

    public void startColorDragWindow(String packageName, int resId, int mode, Bundle options) {
        if (ColorDragWindowHelper.getInstance() != null) {
            ColorDragWindowHelper.getInstance().startColorDragWindow(packageName, resId, mode, options);
        }
    }

    public void setBreenoState(String winName) {
        if (ColorDragWindowHelper.getInstance() != null) {
            ColorDragWindowHelper.getInstance().setBreenoState(winName);
        }
    }

    public boolean canMagnificationSpec(WindowState win) {
        return ColorDragWindowHelper.getInstance().canMagnificationSpec(win);
    }

    public void recoveryState() {
        ColorDragWindowHelper.getInstance().recoveryState();
    }
}
