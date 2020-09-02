package com.color.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.MotionEvent;
import com.color.view.IColorScrollBarEffect;

public class ColorScrollBarEffect implements IColorScrollBarEffect {
    public static final IColorScrollBarEffect NO_EFFECT = new NoEffect();
    private boolean mIsTouchPressed = false;
    private final int mMinHeightNormal;
    private final int mMinHeightOverScroll;
    private int mOverScrollLengthY = 0;
    private final int mPadding;
    private final IColorScrollBarEffect.ViewCallback mViewCallback;

    public ColorScrollBarEffect(Context context, IColorScrollBarEffect.ViewCallback viewCallback) {
        this.mViewCallback = viewCallback;
        Resources res = context.getResources();
        this.mPadding = res.getDimensionPixelSize(201655361);
        this.mMinHeightOverScroll = res.getDimensionPixelSize(201655362);
        this.mMinHeightNormal = res.getDimensionPixelSize(201655363);
    }

    public ColorScrollBarEffect(Resources res, IColorScrollBarEffect.ViewCallback viewCallback) {
        this.mViewCallback = viewCallback;
        this.mPadding = res.getDimensionPixelSize(201655361);
        this.mMinHeightOverScroll = res.getDimensionPixelSize(201655362);
        this.mMinHeightNormal = res.getDimensionPixelSize(201655363);
    }

    @Override // com.color.view.IColorScrollBarEffect
    public void getDrawRect(Rect rect) {
        rect.inset(0, this.mPadding);
        rect.offset(getOffsetX(), 0);
    }

    @Override // com.color.view.IColorScrollBarEffect
    public int getThumbLength(int size, int thickness, int extent, int range) {
        int minLength = this.mOverScrollLengthY != 0 ? this.mMinHeightOverScroll : this.mMinHeightNormal;
        int length = (Math.max(Math.round((((float) size) * ((float) extent)) / ((float) range)), this.mMinHeightNormal) - this.mOverScrollLengthY) - (this.mPadding * 2);
        return length < minLength ? minLength : length;
    }

    @Override // com.color.view.IColorScrollBarEffect
    public void onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            this.mIsTouchPressed = true;
        } else if (actionMasked == 1 || actionMasked == 3) {
            this.mIsTouchPressed = false;
            this.mViewCallback.awakenScrollBars();
        }
    }

    @Override // com.color.view.IColorScrollBarEffect
    public void onOverScrolled(int scrollX, int scrollY, int scrollRangeX, int scrollRangeY) {
        this.mOverScrollLengthY = getOverScrollLength(scrollY, scrollRangeY);
    }

    @Override // com.color.view.IColorScrollBarEffect
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

    private static class NoEffect implements IColorScrollBarEffect {
        private NoEffect() {
        }

        @Override // com.color.view.IColorScrollBarEffect
        public void getDrawRect(Rect rect) {
        }

        @Override // com.color.view.IColorScrollBarEffect
        public int getThumbLength(int size, int thickness, int extent, int range) {
            return ColorScrollBarUtils.getThumbLength(size, thickness, extent, range);
        }

        @Override // com.color.view.IColorScrollBarEffect
        public void onTouchEvent(MotionEvent event) {
        }

        @Override // com.color.view.IColorScrollBarEffect
        public void onOverScrolled(int scrollX, int scrollY, int scrollRangeX, int scrollRangeY) {
        }

        @Override // com.color.view.IColorScrollBarEffect
        public boolean isTouchPressed() {
            return false;
        }
    }
}
