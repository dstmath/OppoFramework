package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.IntProperty;
import android.util.Slog;
import android.view.Choreographer;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
final class RampAnimator<T> {
    private static int mAnimationFrameCount;
    private static int mSlowAnimtionBrightnessVaule;
    private float mAnimatedValue;
    private boolean mAnimating;
    private final Runnable mAnimationCallback;
    private final Choreographer mChoreographer;
    private int mCurrentValue;
    private boolean mFirstTime;
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.display.RampAnimator.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.display.RampAnimator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.RampAnimator.<clinit>():void");
    }

    public RampAnimator(T object, IntProperty<T> property) {
        this.mFirstTime = true;
        this.mAnimationCallback = new Runnable() {
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
                if ((OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || RampAnimator.this.mTargetValue >= RampAnimator.this.mCurrentValue || AutomaticBrightnessController.mProximityNear) && ((OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || RampAnimator.this.mTargetValue <= RampAnimator.this.mCurrentValue) && (!OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || RampAnimator.this.mTargetValue == RampAnimator.this.mCurrentValue))) {
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
            if (target < this.mCurrentValue && this.mCurrentValue > this.mTargetValue && rate != 0 && !DisplayPowerController.mScreenDimQuicklyDark) {
                OppoBrightUtils oppoBrightUtils = this.mOppoBrightUtils;
                if (!OppoBrightUtils.mManualSetAutoBrightness) {
                    oppoBrightUtils = this.mOppoBrightUtils;
                    if (!OppoBrightUtils.mBrightnessNoAnimation) {
                        if (!((this.mAnimating || target == this.mCurrentValue || AutomaticBrightnessController.mProximityNear || OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT) && (this.mAnimating || target == this.mCurrentValue || !OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT))) {
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
                if (!OppoBrightUtils.mUseWindowBrightness && amountIn < 1.0f) {
                    amount = 1.0f;
                }
            }
        } else if (2 == OppoBrightUtils.mBrightnessBitsConfig) {
            oppoBrightUtils = this.mOppoBrightUtils;
            if (OppoBrightUtils.mUseAutoBrightness && this.mAnimatedValue <= ((float) mSlowAnimtionBrightnessVaule)) {
                oppoBrightUtils = this.mOppoBrightUtils;
                if (!(OppoBrightUtils.mUseWindowBrightness || this.mTargetValue >= this.mCurrentValue || this.mRate == DisplayPowerController.BRIGHTNESS_RAMP_RATE_FAST)) {
                    amount = 0.5f;
                }
            }
        } else {
            oppoBrightUtils = this.mOppoBrightUtils;
            if (OppoBrightUtils.mUseAutoBrightness && this.mAnimatedValue <= ((float) mSlowAnimtionBrightnessVaule)) {
                oppoBrightUtils = this.mOppoBrightUtils;
                if (!(OppoBrightUtils.mUseWindowBrightness || this.mTargetValue >= this.mCurrentValue || this.mRate == DisplayPowerController.BRIGHTNESS_RAMP_RATE_FAST)) {
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
