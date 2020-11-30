package com.color.widget;

import android.view.animation.Interpolator;

public interface ColorPagerCallback {
    public static final Interpolator ANIMATOR_INTERPOLATOR = new Interpolator() {
        /* class com.color.widget.ColorPagerCallback.AnonymousClass1 */

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float t) {
            float t2 = t - 1.0f;
            return (t2 * t2 * t2 * t2 * t2) + 1.0f;
        }
    };
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
}
