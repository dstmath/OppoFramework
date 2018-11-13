package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.IntProperty;
import android.util.Slog;
import android.view.Choreographer;

final class RampAnimator<T> {
    private static int mAnimationFrameCount = 0;
    private static int mSlowAnimtionBrightnessVaule = -1;
    private float mAnimatedValue;
    private boolean mAnimating;
    private final Runnable mAnimationCallback = new Runnable() {
        public void run() {
            int tempRate;
            int i = 300;
            long frameTimeNanos = RampAnimator.this.mChoreographer.getFrameTimeNanos();
            float timeDelta = ((float) (frameTimeNanos - RampAnimator.this.mLastFrameTimeNanos)) * 1.0E-9f;
            RampAnimator.this.mLastFrameTimeNanos = frameTimeNanos;
            RampAnimator.mAnimationFrameCount = RampAnimator.mAnimationFrameCount + 1;
            int -get9;
            if (RampAnimator.this.mTargetValue <= RampAnimator.this.mCurrentValue) {
                tempRate = RampAnimator.this.mRate;
            } else if (3 == OppoBrightUtils.mBrightnessBitsConfig) {
                -get9 = RampAnimator.this.mRate;
                if (RampAnimator.mAnimationFrameCount <= 300) {
                    i = RampAnimator.mAnimationFrameCount;
                }
                tempRate = -get9 + i;
            } else if (2 == OppoBrightUtils.mBrightnessBitsConfig) {
                -get9 = RampAnimator.this.mRate;
                if (RampAnimator.mAnimationFrameCount <= 300) {
                    i = RampAnimator.mAnimationFrameCount;
                }
                tempRate = -get9 + i;
            } else {
                tempRate = RampAnimator.this.mRate;
            }
            RampAnimator.this.mOppoBrightUtils;
            float scale = OppoBrightUtils.mBrightnessNoAnimation ? OppoBrightUtils.MIN_LUX_LIMITI : ValueAnimator.getDurationScale();
            if (scale == OppoBrightUtils.MIN_LUX_LIMITI) {
                RampAnimator.this.mAnimatedValue = (float) RampAnimator.this.mTargetValue;
            } else {
                float amount = RampAnimator.this.caculateAmount((((float) tempRate) * timeDelta) / scale);
                if (RampAnimator.this.mTargetValue > RampAnimator.this.mCurrentValue) {
                    RampAnimator.this.mAnimatedValue = Math.min(RampAnimator.this.mAnimatedValue + amount, (float) RampAnimator.this.mTargetValue);
                } else {
                    RampAnimator.this.mAnimatedValue = Math.max(RampAnimator.this.mAnimatedValue - amount, (float) RampAnimator.this.mTargetValue);
                }
            }
            int oldCurrentValue = RampAnimator.this.mCurrentValue;
            RampAnimator.this.mCurrentValue = Math.round(RampAnimator.this.mAnimatedValue);
            if (oldCurrentValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.mProperty.setValue(RampAnimator.this.mObject, RampAnimator.this.mCurrentValue);
            }
            if ((OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || RampAnimator.this.mTargetValue >= RampAnimator.this.mCurrentValue || (AutomaticBrightnessController.mProximityNear ^ 1) == 0) && ((OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || RampAnimator.this.mTargetValue <= RampAnimator.this.mCurrentValue) && (!OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || RampAnimator.this.mTargetValue == RampAnimator.this.mCurrentValue))) {
                RampAnimator.this.mOppoBrightUtils;
                if (!OppoBrightUtils.mManualSetAutoBrightness || RampAnimator.this.mTargetValue == RampAnimator.this.mCurrentValue) {
                    RampAnimator.mAnimationFrameCount = 0;
                    RampAnimator.this.mAnimating = false;
                    if (RampAnimator.this.mListener != null) {
                        RampAnimator.this.mListener.onAnimationEnd();
                        RampAnimator.this.mOppoBrightUtils;
                        OppoBrightUtils.mUseWindowBrightness = false;
                        RampAnimator.this.mOppoBrightUtils;
                        OppoBrightUtils.mCameraUseAdjustmentSetting = false;
                        return;
                    }
                    return;
                }
            }
            RampAnimator.this.postAnimationCallback();
        }
    };
    private final Choreographer mChoreographer;
    private int mCurrentValue;
    private boolean mFirstTime = true;
    private long mLastFrameTimeNanos;
    private Listener mListener;
    private final T mObject;
    private OppoBrightUtils mOppoBrightUtils;
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
        this.mOppoBrightUtils = OppoBrightUtils.getInstance();
        mSlowAnimtionBrightnessVaule = this.mOppoBrightUtils.getSlowAnimationBrightness();
    }

    public boolean animateTo(int target, int rate) {
        if (!this.mFirstTime && rate > 0) {
            if (OppoBrightUtils.DEBUG) {
                Slog.d("RampAnimator", "mTargetValue=" + this.mTargetValue + " ,mRate=" + this.mRate + " ,mCurrentValue=" + this.mCurrentValue + " ,target=" + target + " rate=" + rate);
            }
            if (target < this.mCurrentValue && this.mCurrentValue > this.mTargetValue && rate != 0 && (DisplayPowerController.mScreenDimQuicklyDark ^ 1) != 0) {
                OppoBrightUtils oppoBrightUtils = this.mOppoBrightUtils;
                if ((OppoBrightUtils.mManualSetAutoBrightness ^ 1) != 0) {
                    oppoBrightUtils = this.mOppoBrightUtils;
                    if ((OppoBrightUtils.mBrightnessNoAnimation ^ 1) != 0) {
                        if (!((this.mAnimating || target == this.mCurrentValue || (AutomaticBrightnessController.mProximityNear ^ 1) == 0 || (OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT ^ 1) == 0) && (this.mAnimating || target == this.mCurrentValue || !OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT))) {
                            this.mAnimating = true;
                            this.mAnimatedValue = (float) this.mCurrentValue;
                            mAnimationFrameCount = 0;
                            this.mLastFrameTimeNanos = System.nanoTime();
                            postAnimationCallback();
                        }
                        this.mTargetValue = target;
                        return true;
                    }
                }
            }
            if (!this.mAnimating || rate > this.mRate || ((target <= this.mCurrentValue && this.mCurrentValue <= this.mTargetValue) || (this.mTargetValue <= this.mCurrentValue && this.mCurrentValue <= target))) {
                this.mRate = rate;
            }
            if ((target <= this.mCurrentValue && this.mCurrentValue <= this.mTargetValue) || (this.mTargetValue <= this.mCurrentValue && this.mCurrentValue <= target)) {
                mAnimationFrameCount = 0;
            }
            boolean changed = this.mTargetValue != target;
            this.mTargetValue = target;
            if (!(this.mAnimating || target == this.mCurrentValue)) {
                this.mAnimating = true;
                this.mAnimatedValue = (float) this.mCurrentValue;
                mAnimationFrameCount = 0;
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
            if (this.mListener != null) {
                this.mListener.onAnimationEnd();
            }
            return true;
        }
    }

    public void updateCurrentToTarget() {
        this.mRate = 0;
        this.mCurrentValue = this.mTargetValue;
        this.mProperty.setValue(this.mObject, this.mTargetValue);
        if (this.mAnimating) {
            this.mAnimating = false;
            cancelAnimationCallback();
        }
        if (this.mListener != null) {
            this.mListener.onAnimationEnd();
        }
    }

    private float caculateAmount(float amountIn) {
        float amount = amountIn;
        OppoBrightUtils oppoBrightUtils;
        if (1 == OppoBrightUtils.mBrightnessBitsConfig) {
            oppoBrightUtils = this.mOppoBrightUtils;
            if (OppoBrightUtils.mUseAutoBrightness && this.mAnimatedValue <= ((float) mSlowAnimtionBrightnessVaule)) {
                oppoBrightUtils = this.mOppoBrightUtils;
                if ((OppoBrightUtils.mUseWindowBrightness ^ 1) != 0 && amountIn < 1.0f) {
                    amount = 1.0f;
                }
            }
        } else if (2 == OppoBrightUtils.mBrightnessBitsConfig) {
            oppoBrightUtils = this.mOppoBrightUtils;
            if (OppoBrightUtils.mUseAutoBrightness && this.mAnimatedValue <= ((float) mSlowAnimtionBrightnessVaule)) {
                oppoBrightUtils = this.mOppoBrightUtils;
                if (!((OppoBrightUtils.mUseWindowBrightness ^ 1) == 0 || this.mTargetValue >= this.mCurrentValue || this.mRate == DisplayPowerController.BRIGHTNESS_RAMP_RATE_FAST)) {
                    amount = 0.5f;
                }
            }
        } else {
            oppoBrightUtils = this.mOppoBrightUtils;
            if (OppoBrightUtils.mUseAutoBrightness && this.mAnimatedValue <= ((float) mSlowAnimtionBrightnessVaule)) {
                oppoBrightUtils = this.mOppoBrightUtils;
                if (!((OppoBrightUtils.mUseWindowBrightness ^ 1) == 0 || this.mTargetValue >= this.mCurrentValue || this.mRate == DisplayPowerController.BRIGHTNESS_RAMP_RATE_FAST)) {
                    amount = 0.3f;
                }
            }
        }
        if (!DisplayPowerController.mScreenDimQuicklyDark) {
            return amount;
        }
        if (3 == OppoBrightUtils.mBrightnessBitsConfig) {
            return 20.0f;
        }
        if (2 == OppoBrightUtils.mBrightnessBitsConfig) {
            return 10.0f;
        }
        return 2.0f;
    }

    public boolean isAnimating() {
        return this.mAnimating;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    private void postAnimationCallback() {
        this.mChoreographer.postCallback(1, this.mAnimationCallback, null);
    }

    private void cancelAnimationCallback() {
        this.mChoreographer.removeCallbacks(1, this.mAnimationCallback, null);
    }
}
