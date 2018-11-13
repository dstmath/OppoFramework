package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.PathInterpolator;
import com.color.widget.ColorRecyclerView.ViewHolder;

public class ColorDeleteAnimation implements AnimatorListener {
    private final AnimatorSet mAnimatorSet;
    public boolean mEnded;
    private float mFraction;
    public boolean mIsPendingCleanup;
    public boolean mOverridden;
    final float mStartDx;
    final float mStartDy;
    final float mTargetX;
    final float mTargetY;
    public View mView;
    public ViewHolder mViewHolder;
    float mX;
    float mY;

    public ColorDeleteAnimation(View view, View fadeview, float startDx, float startDy, float targetX, float targetY) {
        this.mOverridden = false;
        this.mEnded = false;
        this.mView = view;
        this.mStartDx = startDx;
        this.mStartDy = startDy;
        this.mTargetX = targetX;
        this.mTargetY = targetY;
        this.mAnimatorSet = new AnimatorSet();
        ValueAnimator anim = ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, targetX});
        if (fadeview != null) {
            this.mAnimatorSet.play(anim).with(ObjectAnimator.ofFloat(fadeview, "alpha", new float[]{1.0f, 0.0f}));
        } else {
            this.mAnimatorSet.play(anim);
        }
        this.mAnimatorSet.setInterpolator(new PathInterpolator(0.133f, 0.0f, 0.3f, 1.0f));
        this.mAnimatorSet.addListener(this);
    }

    public ColorDeleteAnimation(ViewHolder viewHolder, float startDx, float startDy, float targetX, float targetY) {
        this.mOverridden = false;
        this.mEnded = false;
        this.mViewHolder = viewHolder;
        this.mStartDx = startDx;
        this.mStartDy = startDy;
        this.mTargetX = targetX;
        this.mTargetY = targetY;
        ValueAnimator animator = ObjectAnimator.ofFloat(this.mViewHolder.itemView, "translationX", new float[]{0.0f, targetX});
        this.mAnimatorSet = new AnimatorSet();
        this.mAnimatorSet.play(animator);
        this.mAnimatorSet.setInterpolator(new PathInterpolator(0.133f, 0.0f, 0.3f, 1.0f));
        this.mAnimatorSet.addListener(this);
    }

    public ColorDeleteAnimation(View view, float startDx, float startDy, float targetX, float targetY) {
        this(view, null, startDx, startDy, targetX, targetY);
    }

    public void setDuration(long duration) {
        this.mAnimatorSet.setDuration(duration);
    }

    public void start() {
        if (this.mViewHolder != null) {
            this.mViewHolder.setIsRecyclable(false);
        }
        this.mAnimatorSet.start();
    }

    public void cancel() {
        this.mAnimatorSet.cancel();
    }

    public void onAnimationStart(Animator animation) {
    }

    public void onAnimationEnd(Animator animation) {
        this.mEnded = true;
    }

    public void onAnimationCancel(Animator animation) {
    }

    public void onAnimationRepeat(Animator animation) {
    }
}
