package com.color.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.color.view.animation.ColorPathInterpolator;

public class ColorRotateView extends ImageView {
    private long mDuration;
    private Interpolator mInterpolator;
    private boolean mIsExpanded;

    public ColorRotateView(Context context) {
        this(context, null);
    }

    public ColorRotateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorRotateView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, 0, 0);
    }

    public ColorRotateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mInterpolator = ColorPathInterpolator.create();
        this.mDuration = 300;
        this.mIsExpanded = false;
        animate().setDuration(this.mDuration).setInterpolator(this.mInterpolator);
    }

    public void startExpandAnimation() {
        animate().rotation(180.0f);
        this.mIsExpanded = true;
    }

    public void startCollapseAnimation() {
        animate().rotation(0.0f);
        this.mIsExpanded = false;
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public void setExpanded(boolean expand) {
        if (this.mIsExpanded != expand) {
            this.mIsExpanded = expand;
            setRotation((float) (expand ? 180 : 0));
        }
    }

    public void startRotateAnimation() {
        if (this.mIsExpanded) {
            startCollapseAnimation();
        } else {
            startExpandAnimation();
        }
    }
}
