package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.IntProperty;
import android.view.Choreographer;

final class RampAnimator<T> {
    private float mAnimatedValue;
    private boolean mAnimating;
    private final Runnable mAnimationCallback = new Runnable() {
        /* class com.android.server.display.RampAnimator.AnonymousClass1 */

        /* JADX DEBUG: Multi-variable search result rejected for r5v12, resolved type: android.util.IntProperty */
        /* JADX WARN: Multi-variable type inference failed */
        public void run() {
            long frameTimeNanos = RampAnimator.this.mChoreographer.getFrameTimeNanos();
            float timeDelta = ((float) (frameTimeNanos - RampAnimator.this.mLastFrameTimeNanos)) * 1.0E-9f;
            RampAnimator.this.mLastFrameTimeNanos = frameTimeNanos;
            float scale = ValueAnimator.getDurationScale();
            if (scale == OppoBrightUtils.MIN_LUX_LIMITI) {
                RampAnimator rampAnimator = RampAnimator.this;
                rampAnimator.mAnimatedValue = (float) rampAnimator.mTargetValue;
            } else {
                float amount = (((float) RampAnimator.this.mRate) * timeDelta) / scale;
                if (RampAnimator.this.mTargetValue > RampAnimator.this.mCurrentValue) {
                    RampAnimator rampAnimator2 = RampAnimator.this;
                    rampAnimator2.mAnimatedValue = Math.min(rampAnimator2.mAnimatedValue + amount, (float) RampAnimator.this.mTargetValue);
                } else {
                    RampAnimator rampAnimator3 = RampAnimator.this;
                    rampAnimator3.mAnimatedValue = Math.max(rampAnimator3.mAnimatedValue - amount, (float) RampAnimator.this.mTargetValue);
                }
            }
            int oldCurrentValue = RampAnimator.this.mCurrentValue;
            RampAnimator rampAnimator4 = RampAnimator.this;
            rampAnimator4.mCurrentValue = Math.round(rampAnimator4.mAnimatedValue);
            if (oldCurrentValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.mProperty.setValue(RampAnimator.this.mObject, RampAnimator.this.mCurrentValue);
            }
            if (RampAnimator.this.mTargetValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.postAnimationCallback();
                return;
            }
            RampAnimator.this.mAnimating = false;
            if (RampAnimator.this.mListener != null) {
                RampAnimator.this.mListener.onAnimationEnd();
            }
        }
    };
    private final Choreographer mChoreographer;
    private int mCurrentValue;
    private boolean mFirstTime = true;
    private long mLastFrameTimeNanos;
    private Listener mListener;
    private final T mObject;
    private final IntProperty<T> mProperty;
    private int mRate;
    private int mTargetValue;

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
    /* access modifiers changed from: public */
    private void postAnimationCallback() {
        this.mChoreographer.postCallback(1, this.mAnimationCallback, null);
    }

    private void cancelAnimationCallback() {
        this.mChoreographer.removeCallbacks(1, this.mAnimationCallback, null);
    }
}
