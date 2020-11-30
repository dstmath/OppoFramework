package com.color.widget;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.OppoBezierInterpolator;
import android.widget.ImageView;

public class ColorFadeBackImageView extends ImageView {
    private static final String TAG = "ColorFadeBackImageView";
    private static final int TOUCH_END_DURATION = 300;
    private static final int TOUCH_START_DURATION = 66;
    private int m3dip;
    private ValueAnimator mAnimator;
    private int mCurrentPadding;
    private Rect mPadding;
    private OppoBezierInterpolator mTouchEndInterpolator;
    private OppoBezierInterpolator mTouchStartInterpolator;

    public ColorFadeBackImageView(Context context) {
        this(context, null);
    }

    public ColorFadeBackImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFadeBackImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTouchStartInterpolator = new OppoBezierInterpolator(0.25d, 0.1d, 0.1d, 1.0d, true);
        this.mTouchEndInterpolator = new OppoBezierInterpolator(0.25d, 0.1d, 0.25d, 1.0d, true);
        this.mPadding = new Rect();
        this.mPadding.left = this.mPaddingLeft;
        this.mPadding.top = this.mPaddingTop;
        this.mPadding.right = this.mPaddingRight;
        this.mPadding.bottom = this.mPaddingBottom;
        this.m3dip = dip2px(getContext(), 1.5f);
        this.mCurrentPadding = this.m3dip;
        setPadding(this.mPaddingLeft + this.m3dip, this.mPaddingTop + this.m3dip, this.mPaddingRight + this.m3dip, this.mPaddingBottom + this.m3dip);
    }

    @Override // android.view.View
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == 0) {
            performTouchStartAnim();
        } else if (event.getAction() == 3 || event.getAction() == 1) {
            performTouchEndAnim();
        }
        super.dispatchTouchEvent(event);
        return true;
    }

    private int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void performTouchStartAnim() {
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mAnimator.cancel();
        }
        this.mAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofInt("widthHolder", this.mCurrentPadding, 0));
        this.mAnimator.setInterpolator(this.mTouchStartInterpolator);
        this.mAnimator.setDuration(66L);
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.ColorFadeBackImageView.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                ColorFadeBackImageView.this.mCurrentPadding = ((Integer) animation.getAnimatedValue("widthHolder")).intValue();
                ColorFadeBackImageView colorFadeBackImageView = ColorFadeBackImageView.this;
                colorFadeBackImageView.setPadding(colorFadeBackImageView.mPadding.left + ColorFadeBackImageView.this.mCurrentPadding, ColorFadeBackImageView.this.mPadding.top + ColorFadeBackImageView.this.mCurrentPadding, ColorFadeBackImageView.this.mPadding.right + ColorFadeBackImageView.this.mCurrentPadding, ColorFadeBackImageView.this.mPadding.bottom + ColorFadeBackImageView.this.mCurrentPadding);
            }
        });
        this.mAnimator.start();
    }

    private void performTouchEndAnim() {
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mAnimator.cancel();
        }
        this.mAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofInt("widthHolder", this.mCurrentPadding, this.m3dip));
        this.mAnimator.setInterpolator(this.mTouchEndInterpolator);
        this.mAnimator.setDuration(300L);
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.ColorFadeBackImageView.AnonymousClass2 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                ColorFadeBackImageView.this.mCurrentPadding = ((Integer) animation.getAnimatedValue("widthHolder")).intValue();
                ColorFadeBackImageView colorFadeBackImageView = ColorFadeBackImageView.this;
                colorFadeBackImageView.setPadding(colorFadeBackImageView.mPadding.left + ColorFadeBackImageView.this.mCurrentPadding, ColorFadeBackImageView.this.mPadding.top + ColorFadeBackImageView.this.mCurrentPadding, ColorFadeBackImageView.this.mPadding.right + ColorFadeBackImageView.this.mCurrentPadding, ColorFadeBackImageView.this.mPadding.bottom + ColorFadeBackImageView.this.mCurrentPadding);
            }
        });
        this.mAnimator.start();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mAnimator.cancel();
            setPadding(this.mPadding.left + this.m3dip, this.mPadding.top + this.m3dip, this.mPadding.right + this.m3dip, this.mPadding.bottom + this.m3dip);
        }
    }
}
