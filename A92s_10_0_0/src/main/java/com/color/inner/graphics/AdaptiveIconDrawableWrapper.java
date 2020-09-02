package com.color.inner.graphics;

import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorAdaptiveIconDrawable;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class AdaptiveIconDrawableWrapper {
    private static final String TAG = "AdaptiveIconDrawbaleWrapper";

    public static float getForegroundScalePercent(AdaptiveIconDrawable drawable) {
        try {
            ColorAdaptiveIconDrawable colorAdaptiveIconDrawable = typeCasting(drawable);
            if (colorAdaptiveIconDrawable != null) {
                return colorAdaptiveIconDrawable.getForegroundScalePercent(drawable);
            }
            return 0.0f;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0.0f;
        }
    }

    private static ColorAdaptiveIconDrawable typeCasting(AdaptiveIconDrawable drawable) {
        return (ColorAdaptiveIconDrawable) ColorTypeCastingHelper.typeCasting(ColorAdaptiveIconDrawable.class, drawable);
    }
}
