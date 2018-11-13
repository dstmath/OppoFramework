package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import com.color.animation.ColorAnimatorUtil;
import com.color.animation.ColorAnimatorWrapper;
import com.color.util.ColorLog;
import java.util.List;

public class ColorChoiceModeAnimator implements AnimatorListener {
    public static final int AFTER = 1;
    public static final int BEFORE = 2;
    public static final int WITH = 0;
    List<ColorAnimatorWrapper> mAnimWrapperList;
    private int mDependency;
    private String mTag;
    protected final Class<?> mTagClass;

    public ColorChoiceModeAnimator(List<ColorAnimatorWrapper> list, String tag) {
        this(list, tag, 0);
    }

    public ColorChoiceModeAnimator(List<ColorAnimatorWrapper> list, String tag, int dependency) {
        this.mTagClass = getClass();
        this.mAnimWrapperList = null;
        this.mDependency = 0;
        this.mTag = null;
        this.mAnimWrapperList = list;
        this.mTag = tag;
        this.mDependency = dependency;
    }

    public void onAnimationCancel(Animator animation) {
        if (ColorLog.getDebug("log.key.bottom_menu.anim")) {
            dumpAnimation(animation, ColorLog.joinString(new String[]{"onAnimationCancel : ", getTag(), onAnimCancel(animation)}));
        }
    }

    public void onAnimationEnd(Animator animation) {
        animation.removeListener(this);
        if (ColorLog.getDebug("log.key.bottom_menu.anim")) {
            dumpAnimation(animation, ColorLog.joinString(new String[]{"onAnimationEnd : ", getTag(), onAnimEnd(animation)}));
        }
    }

    public void onAnimationRepeat(Animator animation) {
        if (ColorLog.getDebug("log.key.bottom_menu.anim")) {
            dumpAnimation(animation, ColorLog.joinString(new String[]{"onAnimationRepeat : ", getTag(), onAnimRepeat(animation)}));
        }
    }

    public void onAnimationStart(Animator animation) {
        if (ColorLog.getDebug("log.key.bottom_menu.anim")) {
            dumpAnimation(animation, ColorLog.joinString(new String[]{"onAnimationStart : ", getTag(), onAnimStart(animation)}));
        }
    }

    public void initialize() {
    }

    public void addListener(AnimatorListener listener) {
    }

    public List<ColorAnimatorWrapper> getAnimations() {
        return this.mAnimWrapperList;
    }

    String onAnimCancel(Animator animation) {
        return "";
    }

    String onAnimEnd(Animator animation) {
        return "";
    }

    String onAnimRepeat(Animator animation) {
        return "";
    }

    String onAnimStart(Animator animation) {
        return "";
    }

    String getTag() {
        return this.mTag;
    }

    int getDependency() {
        return this.mDependency;
    }

    void appendTag(String suffix) {
        this.mTag += suffix;
    }

    private void dumpAnimation(Animator anim, String message) {
        ColorAnimatorUtil.dump("log.key.bottom_menu.anim2", this.mTagClass, anim, new Object[]{message});
    }
}
