package com.color.view;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.SurfaceControl;

public final class ColorSurfaceControl {
    public static Bitmap screenshot(int width, int height) {
        return SurfaceControl.screenshot(width, height);
    }

    public static Bitmap screenshot(int width, int height, int maxLayer) {
        return SurfaceControl.screenshot(new Rect(), width, height, 0, maxLayer, false, 0);
    }

    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int maxLayer) {
        return SurfaceControl.screenshot(sourceCrop, width, height, 0, maxLayer, false, 0);
    }
}
