package com.oppo.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import com.color.util.ColorLog;

@Deprecated
public class OppoAnimationHelper implements AnimationListener {
    public static final int DEFAULT_VISIBILITY = -1;
    private Animation mAnimation;
    private boolean mClickable;
    private long mDelay;
    private long mDuration;
    private boolean mFillAfter;
    private boolean mIn;
    private AnimationListener mListener;
    private boolean mRunning;
    private String mTag;
    protected final Class<?> mTagClass;
    private View mView;
    private int mVisibility;

    public OppoAnimationHelper(Animation animation, boolean in) {
        this(animation, in, null);
    }

    public OppoAnimationHelper(Animation animation, boolean in, View view) {
        this(animation, in, view, false);
    }

    public OppoAnimationHelper(Animation animation, boolean in, View view, boolean fillAfter) {
        this(animation, in, view, fillAfter, 0);
    }

    public OppoAnimationHelper(Animation animation, boolean in, View view, boolean fillAfter, long offset) {
        this.mTagClass = getClass();
        this.mView = null;
        this.mAnimation = null;
        this.mListener = null;
        this.mTag = null;
        this.mFillAfter = false;
        this.mClickable = false;
        this.mRunning = false;
        this.mIn = false;
        this.mDelay = 0;
        this.mDuration = 0;
        this.mVisibility = -1;
        this.mAnimation = animation;
        this.mFillAfter = fillAfter;
        this.mIn = in;
        adjustDuration(offset);
        setView(view);
    }

    public void onAnimationStart(Animation animation) {
        ColorLog.d("log.key.multi_select.anim", this.mTagClass, new Object[]{"onAnimationStart : ", this.mTag, " : ", this.mView});
        this.mRunning = true;
        this.mClickable = this.mView.isClickable();
        this.mView.setClickable(false);
        if (this.mListener != null) {
            this.mListener.onAnimationStart(animation);
        }
    }

    public void onAnimationEnd(Animation animation) {
        ColorLog.d("log.key.multi_select.anim", this.mTagClass, new Object[]{"onAnimationEnd : ", this.mTag, " : ", this.mView});
        this.mRunning = false;
        this.mView.setClickable(this.mClickable);
        if (this.mListener != null) {
            this.mListener.onAnimationEnd(animation);
        }
    }

    public void onAnimationRepeat(Animation animation) {
        if (this.mListener != null) {
            this.mListener.onAnimationRepeat(animation);
        }
    }

    public void setFinalVisibility(int visibility) {
        this.mVisibility = visibility;
    }

    public Animation getAnimation() {
        return this.mAnimation;
    }

    public View getView() {
        return this.mView;
    }

    public boolean getFillAfter() {
        return this.mFillAfter;
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    public void clear() {
        if (this.mView != null) {
            int visibility = getFinalVisibility();
            ColorLog.d("log.key.multi_select.anim", this.mTagClass, new Object[]{"clear : ", this.mTag, " : ", this.mView, " => ", Integer.valueOf(visibility)});
            this.mView.setVisibility(visibility);
            this.mView.clearAnimation();
        }
    }

    public void start(boolean delay, AnimationListener listener) {
        if (this.mView != null) {
            this.mRunning = false;
            this.mListener = listener;
            this.mAnimation.setDuration(this.mDuration);
            this.mAnimation.setStartOffset(getStartOffset(delay));
            this.mAnimation.setAnimationListener(this);
            this.mAnimation.setFillEnabled(true);
            this.mAnimation.setFillAfter(this.mFillAfter);
            this.mView.setVisibility(0);
            ColorLog.d("log.key.multi_select.anim", this.mTagClass, new Object[]{"startAnimation : ", this.mTag, " : ", this.mView, " : duration=", Long.valueOf(this.mAnimation.getDuration()), " : startOffset=", Long.valueOf(this.mAnimation.getStartOffset())});
            this.mView.startAnimation(this.mAnimation);
        }
    }

    public void start(boolean delay, AnimationListener listener, View view) {
        setView(view);
        start(delay, listener);
    }

    public void start(boolean delay, AnimationListener listener, float rateX, float rateY) {
    }

    public void start(boolean delay, AnimationListener listener, View view, float rateX, float rateY) {
    }

    public void cancel() {
        this.mAnimation.cancel();
    }

    public void end() {
        if (this.mView != null) {
            int visibility = getFinalVisibility();
            ColorLog.d("log.key.multi_select.anim", this.mTagClass, new Object[]{"end : ", this.mTag, " : ", this.mView, " => ", Integer.valueOf(visibility)});
            this.mView.setVisibility(visibility);
        }
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    public String getTag() {
        return this.mTag;
    }

    private void adjustDuration(long offset) {
        this.mDuration = this.mAnimation.getDuration();
        this.mDuration += offset;
        if (this.mDuration <= 0) {
            this.mDuration = 1;
        }
    }

    private void setView(View view) {
        this.mView = view;
        if (this.mView != null) {
            this.mDelay = (long) this.mView.getContext().getResources().getInteger(202179600);
        }
    }

    private int getFinalVisibility() {
        if (this.mVisibility != -1) {
            return this.mVisibility;
        }
        return this.mIn ? 0 : 8;
    }

    private long getStartOffset(boolean delay) {
        return delay ? this.mDelay : 0;
    }
}
