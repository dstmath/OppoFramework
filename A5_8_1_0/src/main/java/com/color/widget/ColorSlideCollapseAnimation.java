package com.color.widget;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.PathInterpolator;
import android.view.animation.Transformation;

public abstract class ColorSlideCollapseAnimation extends Animation implements AnimationListener {
    int mInitialHeight = this.mView.getMeasuredHeight();
    View mView;

    public abstract void onItemDelete();

    public ColorSlideCollapseAnimation(View view) {
        this.mView = view;
        setInterpolator(new PathInterpolator(0.133f, 0.0f, 0.3f, 1.0f));
        setDuration(200);
        setAnimationListener(this);
    }

    public void onAnimationEnd(Animation animation) {
        onItemDelete();
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (interpolatedTime == 1.0f) {
            this.mView.setVisibility(8);
            return;
        }
        this.mView.getLayoutParams().height = this.mInitialHeight - ((int) (((float) this.mInitialHeight) * interpolatedTime));
        this.mView.requestLayout();
    }

    public boolean willChangeBounds() {
        return true;
    }
}
