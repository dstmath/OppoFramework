package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.PathInterpolator;

public class ColorExpandCollapseHelper {
    public static final int ANIMATION_HEIGHT = 0;
    public static final int ANIMATION_MARGIN = 1;
    public static int AnimatorType = 1;
    protected static boolean isCollapsing;
    protected static boolean isExpanding;

    public static void animateCollapsing(final View view) {
        ValueAnimator animator;
        int origHeight = view.getHeight();
        if (AnimatorType == 0) {
            animator = createHeightAnimator(view, origHeight, 0);
        } else if (view.getLayoutParams() instanceof MarginLayoutParams) {
            animator = createMarginAnimator(view, 0, -origHeight);
        } else {
            animator = createHeightAnimator(view, origHeight, 0);
        }
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                view.setVisibility(0);
                ColorExpandCollapseHelper.isCollapsing = true;
            }

            public void onAnimationEnd(Animator animator) {
                view.setVisibility(8);
                ColorExpandCollapseHelper.isCollapsing = false;
            }
        });
        animator.start();
    }

    public static void animateExpanding(final View view) {
        ValueAnimator animator;
        view.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        if (AnimatorType == 0) {
            animator = createHeightAnimator(view, 0, view.getMeasuredHeight());
        } else if (view.getLayoutParams() instanceof MarginLayoutParams) {
            animator = createMarginAnimator(view, -view.getMeasuredHeight(), 0);
        } else {
            animator = createHeightAnimator(view, 0, view.getMeasuredHeight());
        }
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                view.setVisibility(0);
                ColorExpandCollapseHelper.isExpanding = true;
            }

            public void onAnimationEnd(Animator animator) {
                ColorExpandCollapseHelper.isExpanding = false;
            }
        });
        animator.start();
    }

    public static ValueAnimator createHeightAnimator(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{start, end});
        animator.setDuration(300);
        animator.setInterpolator(new PathInterpolator(0.133f, 0.0f, 0.3f, 1.0f));
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    public static ValueAnimator createMarginAnimator(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{start, end});
        animator.setDuration(300);
        animator.setInterpolator(new PathInterpolator(0.133f, 0.0f, 0.3f, 1.0f));
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
                layoutParams.bottomMargin = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                view.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
