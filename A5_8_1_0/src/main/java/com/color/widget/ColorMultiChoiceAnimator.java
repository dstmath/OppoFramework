package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.view.View;
import com.color.animation.ColorAnimatorWrapper;
import java.util.List;

public class ColorMultiChoiceAnimator extends ColorChoiceModeAnimator {
    public static final int DEFAULT_VISIBILITY = -1;
    private long mDuration;
    private boolean mIn;
    private String mTag;
    private int mVisibility;

    public ColorMultiChoiceAnimator(List<ColorAnimatorWrapper> list, boolean in) {
        this(list, in, -1);
    }

    public ColorMultiChoiceAnimator(List<ColorAnimatorWrapper> list, boolean in, int visibility) {
        this(list, in, visibility, 0);
    }

    public ColorMultiChoiceAnimator(List<ColorAnimatorWrapper> list, boolean in, int visibility, long duration) {
        this(list, in, visibility, duration, 0);
    }

    public ColorMultiChoiceAnimator(List<ColorAnimatorWrapper> list, boolean in, int visibility, long duration, int dependency) {
        this(list, in, visibility, duration, dependency, null);
    }

    public ColorMultiChoiceAnimator(List<ColorAnimatorWrapper> list, boolean in, int visibility, long duration, int dependency, String tag) {
        super(list, tag, dependency);
        this.mTag = null;
        this.mIn = false;
        this.mDuration = 0;
        this.mVisibility = -1;
        this.mIn = in;
        this.mVisibility = visibility;
        this.mDuration = duration;
    }

    public String onAnimEnd(Animator animation) {
        int visibility = getFinalVisibility();
        setTargetVisibility(animation, visibility);
        return " => " + visibility;
    }

    public void addListener(AnimatorListener listener) {
        if (!this.mAnimWrapperList.isEmpty()) {
            ((ColorAnimatorWrapper) this.mAnimWrapperList.get(0)).getAnimation().addListener(listener);
        }
    }

    public void initialize() {
        for (ColorAnimatorWrapper animWrap : this.mAnimWrapperList) {
            Animator animation = animWrap.getAnimation();
            animation.setDuration(this.mDuration);
            animation.addListener(this);
        }
    }

    void end() {
        for (ColorAnimatorWrapper animWrap : this.mAnimWrapperList) {
            onAnimationEnd(animWrap.getAnimation());
        }
    }

    private View getTarget(Animator animation) {
        if (animation instanceof ObjectAnimator) {
            Object target = ((ObjectAnimator) animation).getTarget();
            if (target instanceof View) {
                return (View) target;
            }
        }
        return null;
    }

    private void setTargetVisibility(Animator animation, int visibility) {
        View target = getTarget(animation);
        if (target != null) {
            target.setVisibility(visibility);
        }
    }

    private int getFinalVisibility() {
        if (this.mVisibility != -1) {
            return this.mVisibility;
        }
        return this.mIn ? 0 : 8;
    }
}
