package com.color.darkmode;

import android.graphics.Color;
import com.android.internal.graphics.ColorUtils;

public class ColorDarkModeUtils {
    public static int makeDark(int color) {
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double newL = 100.0d - lab[0];
        if (newL >= lab[0]) {
            return color;
        }
        lab[0] = newL;
        int newColor = ColorUtils.LABToColor(lab[0], lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(newColor), Color.green(newColor), Color.blue(newColor));
    }

    public static int makeLight(int color) {
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double newL = 100.0d - lab[0];
        if (newL <= lab[0]) {
            return color;
        }
        lab[0] = newL;
        int newColor = ColorUtils.LABToColor(lab[0], lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(newColor), Color.green(newColor), Color.blue(newColor));
    }
}
