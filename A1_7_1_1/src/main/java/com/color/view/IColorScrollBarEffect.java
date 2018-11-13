package com.color.view;

import android.graphics.Rect;
import android.view.MotionEvent;

public interface IColorScrollBarEffect {

    public interface ViewCallback {
        boolean awakenScrollBars();

        boolean isLayoutRtl();
    }

    void getDrawRect(Rect rect);

    int getThumbLength(int i, int i2, int i3, int i4);

    boolean isTouchPressed();

    void onOverScrolled(int i, int i2, int i3, int i4);

    void onTouchEvent(MotionEvent motionEvent);
}
