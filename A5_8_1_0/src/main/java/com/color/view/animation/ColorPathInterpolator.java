package com.color.view.animation;

import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

public class ColorPathInterpolator {
    public static Interpolator create() {
        return new PathInterpolator(0.133f, 0.0f, 0.3f, 1.0f);
    }
}
