package com.color.util;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import com.android.internal.graphics.ColorUtils;
import java.lang.reflect.Method;

public class ColorDarkModeUtil {
    public static boolean isNightMode(Context context) {
        return 32 == (context.getResources().getConfiguration().uiMode & 48);
    }

    public static void setForceDarkAllow(View view, boolean allow) {
        if (Build.VERSION.SDK_INT >= 29) {
            view.setForceDarkAllowed(allow);
            return;
        }
        try {
            Method method = View.class.getDeclaredMethod("setForceDarkAllowed", Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(view, Boolean.valueOf(allow));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int makeDarkLimit(int color, float minLight) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        float newL = Math.max(minLight, 1.0f - hsl[2]);
        if (newL >= hsl[2]) {
            return color;
        }
        hsl[2] = newL;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int makeLight(int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        float newL = 1.0f - hsl[2];
        if (newL <= hsl[2]) {
            return color;
        }
        hsl[2] = newL;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int makeDark(int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        float newL = 1.0f - hsl[2];
        if (newL >= hsl[2]) {
            return color;
        }
        hsl[2] = newL;
        return ColorUtils.HSLToColor(hsl);
    }

    public static void makeImageViewDark(ImageView imageView) {
        if (imageView != null) {
            imageView.setColorFilter(getDarkFilter());
        }
    }

    public static void makeDrawableDark(Drawable drawable) {
        if (drawable != null) {
            drawable.setColorFilter(getDarkFilter());
        }
    }

    private static ColorFilter getDarkFilter() {
        return new LightingColorFilter(-2236963, 0);
    }
}
