package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.IntProperty;
import android.view.Choreographer;

final class RampAnimator<T> {
    /* access modifiers changed from: private */
    public float mAnimatedValue;
    /* access modifiers changed from: private */
    public boolean mAnimating;
    private final Runnable mAnimationCallback = new Runnable() {
        /* class com.android.server.display.RampAnimator.AnonymousClass1 */

        public void run() {
            long frameTimeNanos = RampAnimator.this.mChoreographer.getFrameTimeNanos();
            float timeDelta = ((float) (frameTimeNanos - RampAnimator.this.mLastFrameTimeNanos)) * 1.0E-9f;
            long unused = RampAnimator.this.mLastFrameTimeNanos = frameTimeNanos;
            float scale = ValueAnimator.getDurationScale();
            if (scale == OppoBrightUtils.MIN_LUX_LIMITI) {
                RampAnimator rampAnimator = RampAnimator.this;
                float unused2 = rampAnimator.mAnimatedValue = (float) rampAnimator.mTargetValue;
            } else {
                float amount = (((float) RampAnimator.this.mRate) * timeDelta) / scale;
                if (RampAnimator.this.mTargetValue > RampAnimator.this.mCurrentValue) {
                    RampAnimator rampAnimator2 = RampAnimator.this;
                    float unused3 = rampAnimator2.mAnimatedValue = Math.min(rampAnimator2.mAnimatedValue + amount, (float) RampAnimator.this.mTargetValue);
                } else {
                    RampAnimator rampAnimator3 = RampAnimator.this;
                    float unused4 = rampAnimator3.mAnimatedValue = Math.max(rampAnimator3.mAnimatedValue - amount, (float) RampAnimator.this.mTargetValue);
                }
            }
            int oldCurrentValue = RampAnimator.this.mCurrentValue;
            RampAnimator rampAnimator4 = RampAnimator.this;
            int unused5 = rampAnimator4.mCurrentValue = Math.round(rampAnimator4.mAnimatedValue);
            if (oldCurrentValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.mProperty.setValue(RampAnimator.this.mObject, RampAnimator.this.mCurrentValue);
            }
            if (RampAnimator.this.mTargetValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.postAnimationCallback();
                return;
            }
            boolean unused6 = RampAnimator.this.mAnimating = false;
            if (RampAnimator.this.mListener != null) {
                RampAnimator.this.mListener.onAnimationEnd();
            }
        }
    };
    /* access modifiers changed from: private */
    public final Choreographer mChoreographer;
    /* access modifiers changed from: private */
    public int mCurrentValue;
    private boolean mFirstTime = true;
    /* access modifiers changed from: private */
    public long mLastFrameTimeNanos;
    /* access modifiers changed from: private */
    public Listener mListener;
    /* access modifiers changed from: private */
    public final T mObject;
    /* access modifiers changed from: private */
    public final IntProperty<T> mProperty;
    /* access modifiers changed from: private */
    public int mRate;
    /* access modifiers changed from: private */
    public int mTargetValue;

    public interface Listener {
        void onAnimationEnd();
    }

    public RampAnimator(T object, IntProperty<T> property) {
        this.mObject = object;
        this.mProperty = property;
        this.mChoreographer = Choreographer.getInstance();
    }

    public boolean animateTo(int target, int rate) {
        int i;
        int i2;
        int i3;
        boolean changed = false;
        if (!this.mFirstTime && rate > 0) {
            if (!this.mAnimating || rate > this.mRate || ((target <= (i2 = this.mCurrentValue) && i2 <= this.mTargetValue) || (this.mTargetValue <= (i3 = this.mCurrentValue) && i3 <= target))) {
                this.mRate = rate;
            }
            if (this.mTargetValue != target) {
                changed = true;
            }
            this.mTargetValue = target;
            if (!this.mAnimating && target != (i = this.mCurrentValue)) {
                this.mAnimating = true;
                this.mAnimatedValue = (float) i;
                this.mLastFrameTimeNanos = System.nanoTime();
                postAnimationCallback();
            }
            return changed;
        } else if (!this.mFirstTime && target == this.mCurrentValue) {
            return false;
        } else {
            this.mFirstTime = false;
            this.mRate = 0;
            this.mTargetValue = target;
            this.mCurrentValue = target;
            this.mProperty.setValue(this.mObject, target);
            if (this.mAnimating) {
                this.mAnimating = false;
                cancelAnimationCallback();
            }
            Listener listener = this.mListener;
            if (listener != null) {
                listener.onAnimationEnd();
            }
            return true;
        }
    }

    public boolean isAnimating() {
        return this.mAnimating;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: private */
    public void postAnimationCallback() {
        this.mChoreographer.postCallback(1, this.mAnimationCallback, null);
    }

    private void cancelAnimationCallback() {
        this.mChoreographer.removeCallbacks(1, this.mAnimationCallback, null);
    }
}
