package com.android.server.face.utils;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.WindowManager;
import com.android.server.am.OppoProcessManager;

public class CameraUtils {
    public static final int FOCUS_HEIGHT = 160;
    public static final int FOCUS_WIDTH = 160;

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public static int getDisplayRotation(Context context) {
        switch (((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getRotation()) {
            case 0:
                return 90;
            case 1:
                return 0;
            case 2:
                return 270;
            case 3:
                return OppoProcessManager.MSG_UPLOAD;
            default:
                return 90;
        }
    }
}
