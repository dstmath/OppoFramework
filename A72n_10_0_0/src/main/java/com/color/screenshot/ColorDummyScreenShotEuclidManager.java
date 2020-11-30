package com.color.screenshot;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.IColorLongshotWindowManager;
import android.view.WindowManager;

public class ColorDummyScreenShotEuclidManager implements IColorScreenShotEuclidManager {
    private static volatile ColorDummyScreenShotEuclidManager sInstance = null;

    public static ColorDummyScreenShotEuclidManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyScreenShotEuclidManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyScreenShotEuclidManager();
                }
            }
        }
        return sInstance;
    }

    ColorDummyScreenShotEuclidManager() {
    }

    @Override // com.color.screenshot.IColorScreenShotEuclidManager
    public boolean updateSpecialSystemBar(WindowManager.LayoutParams lp) {
        return false;
    }

    @Override // com.color.screenshot.IColorScreenShotEuclidManager
    public boolean skipSystemUiVisibility(WindowManager.LayoutParams lp) {
        return false;
    }

    @Override // com.color.screenshot.IColorScreenShotEuclidManager
    public boolean isSpecialAppWindow(boolean appWindow, WindowManager.LayoutParams attrs) {
        return appWindow;
    }

    @Override // com.color.screenshot.IColorScreenShotEuclidManager
    public boolean takeScreenshot(Context context, int screenshotType, boolean hasStatus, boolean hasNav, Handler handler) {
        return false;
    }

    @Override // com.color.screenshot.IColorScreenShotEuclidManager
    public Handler getScreenShotHandler(Looper looper) {
        return new Handler(Looper.getMainLooper());
    }

    @Override // com.color.screenshot.IColorScreenShotEuclidManager
    public IColorLongshotWindowManager getIColorLongshotWindowManager() {
        return null;
    }
}
