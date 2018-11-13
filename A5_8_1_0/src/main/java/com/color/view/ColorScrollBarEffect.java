package com.color.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.MotionEvent;
import com.color.view.IColorScrollBarEffect.ViewCallback;

public class ColorScrollBarEffect implements IColorScrollBarEffect {
    public static final IColorScrollBarEffect NO_EFFECT = new NoEffect();
    private boolean mIsTouchPressed = false;
    private final int mMinHeightNormal;
    private final int mMinHeightOverScroll;
    private int mOverScrollLengthY = 0;
    private final int mPadding;
    private final ViewCallback mViewCallback;

    private static class NoEffect implements IColorScrollBarEffect {
        /* synthetic */ NoEffect(NoEffect -this0) {
            this();
        }

        private NoEffect() {
        }

        public void getDrawRect(Rect rect) {
        }

        public int getThumbLength(int size, int thickness, int extent, int range) {
            return ColorScrollBarUtils.getThumbLength(size, thickness, extent, range);
        }

        public void onTouchEvent(MotionEvent event) {
        }

        public void onOverScrolled(int scrollX, int scrollY, int scrollRangeX, int scrollRangeY) {
        }

        public boolean isTouchPressed() {
            return false;
        }
    }

    public ColorScrollBarEffect(Context context, ViewCallback viewCallback) {
        this.mViewCallback = viewCallback;
        Resources res = context.getResources();
        this.mPadding = res.getDimensionPixelSize(201655361);
        this.mMinHeightOverScroll = res.getDimensionPixelSize(201655362);
        this.mMinHeightNormal = res.getDimensionPixelSize(201655363);
    }

    public ColorScrollBarEffect(Resources res, ViewCallback viewCallback) {
        this.mViewCallback = viewCallback;
        this.mPadding = res.getDimensionPixelSize(201655361);
        this.mMinHeightOverScroll = res.getDimensionPixelSize(201655362);
        this.mMinHeightNormal = res.getDimensionPixelSize(201655363);
    }

    public void getDrawRect(Rect rect) {
        rect.inset(0, this.mPadding);
        rect.offset(getOffsetX(), 0);
    }

    public int getThumbLength(int size, int thickness, int extent, int range) {
        int minLength = this.mOverScrollLengthY != 0 ? this.mMinHeightOverScroll : this.mMinHeightNormal;
        int length = (Math.max(Math.round((((float) size) * ((float) extent)) / ((float) range)), this.mMinHeightNormal) - this.mOverScrollLengthY) - (this.mPadding * 2);
        if (length < minLength) {
            return minLength;
        }
        return length;
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 0:
                this.mIsTouchPressed = true;
                return;
            case 1:
            case 3:
                this.mIsTouchPressed = false;
                this.mViewCallback.awakenScrollBars();
                return;
            default:
                return;
        }
    }

    public void onOverScrolled(int scrollX, int scrollY, int scrollRangeX, int scrollRangeY) {
        this.mOverScrollLengthY = getOverScrollLength(scrollY, scrollRangeY);
    }

    public boolean isTouchPressed() {
        return this.mIsTouchPressed;
    }

    private int getOverScrollLength(int scrollPos, int scrollRange) {
        if (scrollPos < 0) {
            return -scrollPos;
        }
        if (scrollPos > scrollRange) {
            return scrollPos - scrollRange;
        }
        return 0;
    }

    private int getOffsetX() {
        return this.mViewCallback.isLayoutRtl() ? this.mPadding : -this.mPadding;
    }
}
