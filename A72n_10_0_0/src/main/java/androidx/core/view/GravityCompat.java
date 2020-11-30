package androidx.core.view;

import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;

public final class GravityCompat {
    public static void apply(int gravity, int w, int h, Rect container, Rect outRect, int layoutDirection) {
        if (Build.VERSION.SDK_INT >= 17) {
            Gravity.apply(gravity, w, h, container, outRect, layoutDirection);
        } else {
            Gravity.apply(gravity, w, h, container, outRect);
        }
    }

    public static int getAbsoluteGravity(int gravity, int layoutDirection) {
        if (Build.VERSION.SDK_INT >= 17) {
            return Gravity.getAbsoluteGravity(gravity, layoutDirection);
        }
        return -8388609 & gravity;
    }
}
