package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.IntProperty;
import android.util.Slog;
import android.view.Choreographer;

final class OppoRampAnimator<T> {
    /* access modifiers changed from: private */
    public static int mAnimationFrameCount = 0;
    private static int mSlowAnimtionBrightnessVaule = -1;
    /* access modifiers changed from: private */
    public float mAnimatedValue;
    /* access modifiers changed from: private */
    public boolean mAnimating;
    private final Runnable mAnimationCallback = new Runnable() {
        /* class com.android.server.display.OppoRampAnimator.AnonymousClass1 */

        public void run() {
            int tempRate;
            long frameTimeNanos = OppoRampAnimator.this.mChoreographer.getFrameTimeNanos();
            float timeDelta = ((float) (frameTimeNanos - OppoRampAnimator.this.mLastFrameTimeNanos)) * 1.0E-9f;
            long unused = OppoRampAnimator.this.mLastFrameTimeNanos = frameTimeNanos;
            OppoRampAnimator.access$208();
            if (OppoRampAnimator.this.mTargetValue > OppoRampAnimator.this.mCurrentValue) {
                int i = 300;
                if (3 == OppoBrightUtils.mBrightnessBitsConfig) {
                    int access$500 = OppoRampAnimator.this.mRate;
                    if (OppoRampAnimator.mAnimationFrameCount <= 300) {
                        i = OppoRampAnimator.mAnimationFrameCount;
                    }
                    tempRate = access$500 + i;
                } else if (2 == OppoBrightUtils.mBrightnessBitsConfig) {
                    int access$5002 = OppoRampAnimator.this.mRate;
                    if (OppoRampAnimator.mAnimationFrameCount <= 300) {
                        i = OppoRampAnimator.mAnimationFrameCount;
                    }
                    tempRate = access$5002 + i;
                } else {
                    tempRate = OppoRampAnimator.this.mRate;
                }
            } else {
                tempRate = OppoRampAnimator.this.mRate;
            }
            OppoBrightUtils unused2 = OppoRampAnimator.this.mOppoBrightUtils;
            float scale = OppoBrightUtils.mBrightnessNoAnimation ? 0.0f : ValueAnimator.getDurationScale();
            if (scale == OppoBrightUtils.MIN_LUX_LIMITI) {
                OppoRampAnimator oppoRampAnimator = OppoRampAnimator.this;
                float unused3 = oppoRampAnimator.mAnimatedValue = (float) oppoRampAnimator.mTargetValue;
            } else {
                float amount = OppoRampAnimator.this.caculateAmount((((float) tempRate) * timeDelta) / scale);
                if (OppoRampAnimator.this.mOppoBrightUtils.isAIBrightnessOpen()) {
                    OppoBrightUtils unused4 = OppoRampAnimator.this.mOppoBrightUtils;
                    if (!OppoBrightUtils.mManualSetAutoBrightness && OppoRampAnimator.this.mRate != OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST && !DisplayPowerController.mScreenDimQuicklyDark) {
                        OppoBrightUtils unused5 = OppoRampAnimator.this.mOppoBrightUtils;
                        if (!OppoBrightUtils.mReduceBrightnessAnimating) {
                            amount = OppoRampAnimator.this.mOppoBrightUtils.getAIBrightnessHelper().getNextChange(OppoRampAnimator.this.mTargetValue, OppoRampAnimator.this.mAnimatedValue, timeDelta);
                        }
                    }
                }
                if (OppoRampAnimator.this.mTargetValue > OppoRampAnimator.this.mCurrentValue) {
                    OppoRampAnimator oppoRampAnimator2 = OppoRampAnimator.this;
                    float unused6 = oppoRampAnimator2.mAnimatedValue = Math.min(oppoRampAnimator2.mAnimatedValue + amount, (float) OppoRampAnimator.this.mTargetValue);
                } else {
                    OppoBrightUtils unused7 = OppoRampAnimator.this.mOppoBrightUtils;
                    if (!OppoBrightUtils.mUseAutoBrightness && amount < 10.0f) {
                        amount = (float) (OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST / 60);
                    }
                    OppoRampAnimator oppoRampAnimator3 = OppoRampAnimator.this;
                    float unused8 = oppoRampAnimator3.mAnimatedValue = Math.max(oppoRampAnimator3.mAnimatedValue - amount, (float) OppoRampAnimator.this.mTargetValue);
                }
            }
            int oldCurrentValue = OppoRampAnimator.this.mCurrentValue;
            OppoRampAnimator oppoRampAnimator4 = OppoRampAnimator.this;
            int unused9 = oppoRampAnimator4.mCurrentValue = Math.round(oppoRampAnimator4.mAnimatedValue);
            if (oldCurrentValue != OppoRampAnimator.this.mCurrentValue) {
                OppoRampAnimator.this.mProperty.setValue(OppoRampAnimator.this.mObject, OppoRampAnimator.this.mCurrentValue);
            }
            if ((OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || OppoRampAnimator.this.mTargetValue >= OppoRampAnimator.this.mCurrentValue || OppoAutomaticBrightnessController.mProximityNear) && ((OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || OppoRampAnimator.this.mTargetValue <= OppoRampAnimator.this.mCurrentValue) && (!OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || OppoRampAnimator.this.mTargetValue == OppoRampAnimator.this.mCurrentValue))) {
                OppoBrightUtils unused10 = OppoRampAnimator.this.mOppoBrightUtils;
                if (!OppoBrightUtils.mManualSetAutoBrightness || OppoRampAnimator.this.mTargetValue == OppoRampAnimator.this.mCurrentValue) {
                    int unused11 = OppoRampAnimator.mAnimationFrameCount = 0;
                    boolean unused12 = OppoRampAnimator.this.mAnimating = false;
                    if (OppoRampAnimator.this.mListener != null) {
                        OppoRampAnimator.this.mListener.onAnimationEnd();
                        OppoBrightUtils unused13 = OppoRampAnimator.this.mOppoBrightUtils;
                        OppoBrightUtils.mUseWindowBrightness = false;
                        OppoBrightUtils unused14 = OppoRampAnimator.this.mOppoBrightUtils;
                        OppoBrightUtils.mCameraUseAdjustmentSetting = false;
                        return;
                    }
                    return;
                }
            }
            OppoRampAnimator.this.postAnimationCallback();
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
    public OppoBrightUtils mOppoBrightUtils;
    /* access modifiers changed from: private */
    public final IntProperty<T> mProperty;
    /* access modifiers changed from: private */
    public int mRate;
    /* access modifiers changed from: private */
    public int mTargetValue;

    public interface Listener {
        void onAnimationEnd();
    }

    static /* synthetic */ int access$208() {
        int i = mAnimationFrameCount;
        mAnimationFrameCount = i + 1;
        return i;
    }

    public OppoRampAnimator(T object, IntProperty<T> property) {
        this.mObject = object;
        this.mProperty = property;
        this.mChoreographer = Choreographer.getInstance();
        this.mOppoBrightUtils = OppoBrightUtils.getInstance();
        mSlowAnimtionBrightnessVaule = this.mOppoBrightUtils.getSlowAnimationBrightness();
    }

    public boolean animateTo(int target, int rate) {
        int i;
        int i2;
        int i3;
        int i4;
        if (!this.mFirstTime && rate > 0) {
            if (OppoBrightUtils.DEBUG) {
                Slog.d("RampAnimator", "mTargetValue=" + this.mTargetValue + " ,mRate=" + this.mRate + " ,mCurrentValue=" + this.mCurrentValue + " ,target=" + target + " rate=" + rate);
            }
            int i5 = this.mCurrentValue;
            if (target < i5 && i5 > this.mTargetValue && rate != 0 && !DisplayPowerController.mScreenDimQuicklyDark) {
                OppoBrightUtils oppoBrightUtils = this.mOppoBrightUtils;
                if (!OppoBrightUtils.mManualSetAutoBrightness) {
                    OppoBrightUtils oppoBrightUtils2 = this.mOppoBrightUtils;
                    if (OppoBrightUtils.mUseAutoBrightness) {
                        OppoBrightUtils oppoBrightUtils3 = this.mOppoBrightUtils;
                        if (!OppoBrightUtils.mBrightnessNoAnimation) {
                            if ((!this.mAnimating && target != this.mCurrentValue && !OppoAutomaticBrightnessController.mProximityNear && !OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT) || (!this.mAnimating && target != this.mCurrentValue && OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT)) {
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
            }
            if (!this.mAnimating || rate > this.mRate || ((target <= (i3 = this.mCurrentValue) && i3 <= this.mTargetValue) || (this.mTargetValue <= (i4 = this.mCurrentValue) && i4 <= target))) {
                this.mRate = rate;
            }
            int i6 = this.mCurrentValue;
            if ((target <= i6 && i6 <= this.mTargetValue) || (this.mTargetValue <= (i2 = this.mCurrentValue) && i2 <= target)) {
                mAnimationFrameCount = 0;
            }
            boolean changed = this.mTargetValue != target;
            this.mTargetValue = target;
            if (!this.mAnimating && target != (i = this.mCurrentValue)) {
                this.mAnimating = true;
                this.mAnimatedValue = (float) i;
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
            Listener listener = this.mListener;
            if (listener != null) {
                listener.onAnimationEnd();
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public float caculateAmount(float amountIn) {
        float amount = amountIn;
        if (1 == OppoBrightUtils.mBrightnessBitsConfig) {
            OppoBrightUtils oppoBrightUtils = this.mOppoBrightUtils;
            if (OppoBrightUtils.mUseAutoBrightness && this.mAnimatedValue <= ((float) mSlowAnimtionBrightnessVaule)) {
                OppoBrightUtils oppoBrightUtils2 = this.mOppoBrightUtils;
                if (!OppoBrightUtils.mUseWindowBrightness && amountIn < 1.0f) {
                    amount = 1.0f;
                }
            }
        } else if (2 == OppoBrightUtils.mBrightnessBitsConfig) {
            OppoBrightUtils oppoBrightUtils3 = this.mOppoBrightUtils;
            if (OppoBrightUtils.mUseAutoBrightness && this.mAnimatedValue <= ((float) mSlowAnimtionBrightnessVaule)) {
                OppoBrightUtils oppoBrightUtils4 = this.mOppoBrightUtils;
                if (!OppoBrightUtils.mUseWindowBrightness && this.mTargetValue < this.mCurrentValue && this.mRate != OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST) {
                    amount = 0.5f;
                    if (OppoBrightUtils.mScreenGlobalHBMSupport && this.mAnimatedValue > ((float) OppoBrightUtils.mMaxBrightness) && ((double) 0.5f) < 4.0d) {
                        amount = 4.0f;
                    }
                }
            }
        } else {
            OppoBrightUtils oppoBrightUtils5 = this.mOppoBrightUtils;
            if (OppoBrightUtils.mUseAutoBrightness && this.mAnimatedValue <= ((float) mSlowAnimtionBrightnessVaule)) {
                OppoBrightUtils oppoBrightUtils6 = this.mOppoBrightUtils;
                if (!OppoBrightUtils.mUseWindowBrightness && this.mTargetValue < this.mCurrentValue && this.mRate != OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST) {
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

    /* access modifiers changed from: private */
    public void postAnimationCallback() {
        this.mChoreographer.postCallback(1, this.mAnimationCallback, null);
    }

    private void cancelAnimationCallback() {
        this.mChoreographer.removeCallbacks(1, this.mAnimationCallback, null);
    }

    public void updateCurrentToTarget() {
        this.mRate = 0;
        int i = this.mTargetValue;
        this.mCurrentValue = i;
        this.mProperty.setValue(this.mObject, i);
        if (this.mAnimating) {
            this.mAnimating = false;
            cancelAnimationCallback();
        }
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onAnimationEnd();
        }
    }
}
