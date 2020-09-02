package com.color.view;

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.Rect;
import android.util.Log;
import android.view.OppoWindowManager;
import android.view.SurfaceControl;
import com.color.util.ColorLog;

public final class ColorSurfaceControl {
    private static final String TAG = "ColorSurfaceControl";

    @Deprecated
    public static Bitmap screenshot(int width, int height) {
        return screenshot(new Rect(0, 0, width, height), width, height, 0);
    }

    @Deprecated
    public static Bitmap screenshot(int width, int height, int maxLayer) {
        return SurfaceControl.screenshot(new Rect(0, 0, width, height), width, height, 0);
    }

    @Deprecated
    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int maxLayer) {
        return screenshot(sourceCrop, width, height, maxLayer, 0);
    }

    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int maxLayer, int rotation) {
        if (width != 0 && height != 0) {
            return SurfaceControl.screenshot(sourceCrop, width, height, rotation);
        }
        try {
            return Bitmap.wrapHardwareBuffer(SurfaceControl.captureLayers(new OppoWindowManager().getLongshotWindowByType(-1), sourceCrop, 1.0f).getGraphicBuffer(), ColorSpace.get(ColorSpace.Named.SRGB));
        } catch (Exception e) {
            ColorLog.e(TAG, "[ERROR] captureLayers : " + Log.getStackTraceString(e));
            return null;
        }
    }
}
