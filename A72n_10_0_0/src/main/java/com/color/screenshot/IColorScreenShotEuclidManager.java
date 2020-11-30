package com.color.screenshot;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.IColorLongshotWindowManager;
import android.view.WindowManager;

public interface IColorScreenShotEuclidManager extends IOppoCommonFeature {
    public static final IColorScreenShotEuclidManager DEFAULT = new IColorScreenShotEuclidManager() {
        /* class com.color.screenshot.IColorScreenShotEuclidManager.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorScreenShotEuclidManager getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorScreenShotEuclidManager;
    }

    default boolean updateSpecialSystemBar(WindowManager.LayoutParams lp) {
        return false;
    }

    default boolean skipSystemUiVisibility(WindowManager.LayoutParams lp) {
        return false;
    }

    default boolean isSpecialAppWindow(boolean appWindow, WindowManager.LayoutParams attrs) {
        return appWindow;
    }

    default boolean takeScreenshot(Context context, int screenshotType, boolean hasStatus, boolean hasNav, Handler handler) {
        return false;
    }

    default Handler getScreenShotHandler(Looper looper) {
        return new Handler(Looper.getMainLooper());
    }

    default IColorLongshotWindowManager getIColorLongshotWindowManager() {
        return null;
    }
}
