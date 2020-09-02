package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.widget.LockPatternUtils;
import com.color.animation.DynamicAnimation;
import com.color.animation.FloatPropertyCompat;
import com.color.animation.SpringAnimation;
import com.color.animation.SpringForce;
import com.color.widget.ShutdownView;

public class ShutdownViewControl {
    private static final int MSG_TEARDOWN = 0;
    private static final int REBOOT_TRANSITION = 180;
    private static final int ROAD_TRANSITION = 80;
    private static final int SHUTDOWN_TRANSITION = -180;
    private static final int SPRING_ANIMATION_TO_BASE = 3;
    private static final int SPRING_ANIMATION_TO_REBOOT = 1;
    private static final int SPRING_ANIMATION_TO_SHUTDOWN = 2;
    private final String CHILDREN_MODE_KEY = "children_mode_on";
    private final String COLOROS_FACE_UNLOCK_SWITCH = "coloros_face_unlock_switch";
    private final String FINGERPRINT_UNLOCK_SWITCH = "coloros_fingerprint_unlock_switch";
    private final FloatPropertyCompat<ShutdownView> HANDLER_TRANSITION = new FloatPropertyCompat<ShutdownView>("handlerTransition") {
        /* class com.color.widget.ShutdownViewControl.AnonymousClass2 */

        public float getValue(ShutdownView shutdownView) {
            return shutdownView.getHandlerTransition();
        }

        public void setValue(ShutdownView shutdownView, float v) {
            shutdownView.setHandlerTransition(v);
        }
    };
    private final String MANUALLY_LOCK_TOGGLE = "manually_lock_toggle";
    /* access modifiers changed from: private */
    public float mDownX;
    /* access modifiers changed from: private */
    public float mDownY;
    /* access modifiers changed from: private */
    public AnimatorSet mEnterAnimator;
    /* access modifiers changed from: private */
    public AnimatorSet mExitAnimator;
    private Handler mHandler = new Handler() {
        /* class com.color.widget.ShutdownViewControl.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Log.d("OppoGlobalActions", "cancel animation");
                if (ShutdownViewControl.this.mRoadAnimatorSet != null && ShutdownViewControl.this.mRoadAnimatorSet.isRunning()) {
                    ShutdownViewControl.this.mRoadAnimatorSet.cancel();
                    AnimatorSet unused = ShutdownViewControl.this.mRoadAnimatorSet = null;
                }
                if (ShutdownViewControl.this.mEnterAnimator != null && ShutdownViewControl.this.mEnterAnimator.isRunning()) {
                    ShutdownViewControl.this.mEnterAnimator.cancel();
                    AnimatorSet unused2 = ShutdownViewControl.this.mEnterAnimator = null;
                }
                if (ShutdownViewControl.this.mExitAnimator != null && ShutdownViewControl.this.mExitAnimator.isRunning()) {
                    ShutdownViewControl.this.mExitAnimator.cancel();
                    AnimatorSet unused3 = ShutdownViewControl.this.mExitAnimator = null;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public AnimatorSet mHandlerDownAnimator;
    private AnimatorSet mHandlerUpAnimator;
    /* access modifiers changed from: private */
    public boolean mIsPressEmergency;
    /* access modifiers changed from: private */
    public boolean mIsPressEmpty;
    /* access modifiers changed from: private */
    public boolean mIsPressHandler;
    /* access modifiers changed from: private */
    public boolean mIsPressManuallyLock;
    /* access modifiers changed from: private */
    public OnCancelListener mOnCancelListener;
    /* access modifiers changed from: private */
    public OnEmergencyListener mOnEmergencyListener;
    /* access modifiers changed from: private */
    public OnRebootListener mOnRebootListener;
    /* access modifiers changed from: private */
    public OnShutdownListener mOnShutdownListener;
    private ShutdownView.OperationListener mOperationListener = new ShutdownView.OperationListener() {
        /* class com.color.widget.ShutdownViewControl.AnonymousClass10 */

        @Override // com.color.widget.ShutdownView.OperationListener
        public void cancel() {
            ShutdownViewControl.this.mExitAnimator.start();
        }

        @Override // com.color.widget.ShutdownView.OperationListener
        public void emergency() {
            ShutdownViewControl.this.mShutdownView.setEmergencyPressed(false);
            if (ShutdownViewControl.this.mOnEmergencyListener != null) {
                ShutdownViewControl.this.mOnEmergencyListener.onEmergency();
            }
        }

        @Override // com.color.widget.ShutdownView.OperationListener
        public void manuallyLock() {
            ShutdownViewControl.this.mShutdownView.setManuallyLockPressed(false);
            ShutdownViewControl.this.onManuallyLockChanged();
            ShutdownViewControl.this.mExitAnimator.start();
        }
    };
    private ShutdownView.OrientationChangeListener mOrientationChangeListener = new ShutdownView.OrientationChangeListener() {
        /* class com.color.widget.ShutdownViewControl.AnonymousClass6 */

        @Override // com.color.widget.ShutdownView.OrientationChangeListener
        public void onOrientationChanged() {
            ShutdownViewControl.this.startEnterAnimator();
        }
    };
    private ObjectAnimator mRebootRatioUpAnimator;
    private ObjectAnimator mRebootScaleUpAnimator;
    /* access modifiers changed from: private */
    public int mRebootThreshold;
    /* access modifiers changed from: private */
    public AnimatorSet mRoadAnimatorSet;
    private ObjectAnimator mRoadShowAnimator;
    /* access modifiers changed from: private */
    public AnimatorSet mScaleAndRatioAnimator;
    /* access modifiers changed from: private */
    public boolean mShouldShowManuallyLock;
    /* access modifiers changed from: private */
    public AnimatorSet mShutdownAnimator;
    private ObjectAnimator mShutdownRatioUpAnimator;
    private ObjectAnimator mShutdownScaleUpAnimator;
    /* access modifiers changed from: private */
    public int mShutdownThreshold;
    /* access modifiers changed from: private */
    public ShutdownView mShutdownView;
    /* access modifiers changed from: private */
    public AnimatorSet mToBaseScaleAndRatioAnimator;
    /* access modifiers changed from: private */
    public SpringAnimation mToBaseSpringAnimation;
    /* access modifiers changed from: private */
    public SpringAnimation mToRebootSpringAnimation;
    private DynamicAnimation.OnAnimationEndListener mToRebootSpringListener = new DynamicAnimation.OnAnimationEndListener() {
        /* class com.color.widget.ShutdownViewControl.AnonymousClass9 */

        @Override // com.color.animation.DynamicAnimation.OnAnimationEndListener
        public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
            if (ShutdownViewControl.this.mOnRebootListener != null) {
                ShutdownViewControl.this.mOnRebootListener.onReboot();
            }
            ShutdownViewControl.this.mShutdownAnimator.start();
        }
    };
    /* access modifiers changed from: private */
    public SpringAnimation mToShutdownSpringAnimation;
    private DynamicAnimation.OnAnimationEndListener mToShutdownSpringListener = new DynamicAnimation.OnAnimationEndListener() {
        /* class com.color.widget.ShutdownViewControl.AnonymousClass8 */

        @Override // com.color.animation.DynamicAnimation.OnAnimationEndListener
        public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
            if (ShutdownViewControl.this.mOnShutdownListener != null) {
                ShutdownViewControl.this.mOnShutdownListener.onShutdown();
            }
            ShutdownViewControl.this.mShutdownAnimator.start();
        }
    };
    private ShutdownView.TouchEventListener mTouchEventListener = new ShutdownView.TouchEventListener() {
        /* class com.color.widget.ShutdownViewControl.AnonymousClass7 */

        @Override // com.color.widget.ShutdownView.TouchEventListener
        public boolean onDown(float downX, float downY) {
            boolean z = false;
            if ((ShutdownViewControl.this.mToShutdownSpringAnimation != null && ShutdownViewControl.this.mToShutdownSpringAnimation.isRunning()) || ((ShutdownViewControl.this.mToBaseSpringAnimation != null && ShutdownViewControl.this.mToBaseSpringAnimation.isRunning()) || ((ShutdownViewControl.this.mToRebootSpringAnimation != null && ShutdownViewControl.this.mToRebootSpringAnimation.isRunning()) || ((ShutdownViewControl.this.mToBaseScaleAndRatioAnimator != null && ShutdownViewControl.this.mToBaseScaleAndRatioAnimator.isRunning()) || (ShutdownViewControl.this.mEnterAnimator != null && ShutdownViewControl.this.mEnterAnimator.isRunning()))))) {
                return false;
            }
            float unused = ShutdownViewControl.this.mDownX = downX;
            float unused2 = ShutdownViewControl.this.mDownY = downY;
            if (ShutdownViewControl.this.mShutdownView.isPortrait()) {
                ShutdownViewControl shutdownViewControl = ShutdownViewControl.this;
                boolean unused3 = shutdownViewControl.mIsPressHandler = shutdownViewControl.mShutdownView.mHandlerRectF.contains(downX - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler, downY);
            } else {
                ShutdownViewControl shutdownViewControl2 = ShutdownViewControl.this;
                boolean unused4 = shutdownViewControl2.mIsPressHandler = shutdownViewControl2.mShutdownView.mHandlerRectF.contains(downX, downY - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler);
            }
            if (ShutdownViewControl.this.mShutdownView.isAccessibilityEnabled()) {
                ShutdownViewControl shutdownViewControl3 = ShutdownViewControl.this;
                boolean unused5 = shutdownViewControl3.mIsPressHandler = shutdownViewControl3.mShutdownView.mAccessibilityHandlerRect.contains(downX, downY);
            }
            if (ShutdownViewControl.this.mShutdownView.isPortrait()) {
                ShutdownViewControl shutdownViewControl4 = ShutdownViewControl.this;
                boolean unused6 = shutdownViewControl4.mIsPressEmergency = shutdownViewControl4.mShutdownView.mEmergencyBarRectF.contains((float) ((int) (downX - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler)), (float) ((int) downY));
                ShutdownViewControl shutdownViewControl5 = ShutdownViewControl.this;
                boolean unused7 = shutdownViewControl5.mIsPressEmpty = !shutdownViewControl5.mShutdownView.mBarRectF.contains(downX - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler, downY) && !ShutdownViewControl.this.mIsPressEmergency;
                ShutdownViewControl shutdownViewControl6 = ShutdownViewControl.this;
                if (shutdownViewControl6.mShutdownView.mManuallyLockBarRectF.contains((float) ((int) (downX - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler)), (float) ((int) downY)) && ShutdownViewControl.this.mShouldShowManuallyLock) {
                    z = true;
                }
                boolean unused8 = shutdownViewControl6.mIsPressManuallyLock = z;
            } else {
                ShutdownViewControl shutdownViewControl7 = ShutdownViewControl.this;
                boolean unused9 = shutdownViewControl7.mIsPressEmergency = shutdownViewControl7.mShutdownView.mEmergencyBarRectF.contains((float) ((int) downX), (float) ((int) (downY - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler)));
                ShutdownViewControl shutdownViewControl8 = ShutdownViewControl.this;
                boolean unused10 = shutdownViewControl8.mIsPressEmpty = !shutdownViewControl8.mShutdownView.mBarRectF.contains(downX, downY - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler) && !ShutdownViewControl.this.mIsPressEmergency;
                ShutdownViewControl shutdownViewControl9 = ShutdownViewControl.this;
                if (shutdownViewControl9.mShutdownView.mManuallyLockBarRectF.contains((float) ((int) downX), (float) ((int) (downY - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler))) && ShutdownViewControl.this.mShouldShowManuallyLock) {
                    z = true;
                }
                boolean unused11 = shutdownViewControl9.mIsPressManuallyLock = z;
            }
            if (ShutdownViewControl.this.mIsPressManuallyLock) {
                ShutdownViewControl.this.mShutdownView.setManuallyLockPressed(true);
            }
            if (ShutdownViewControl.this.mIsPressHandler) {
                ShutdownViewControl.this.mHandlerDownAnimator.start();
                ShutdownViewControl.this.mScaleAndRatioAnimator.start();
                if (ShutdownViewControl.this.mRoadAnimatorSet != null) {
                    ShutdownViewControl.this.mRoadAnimatorSet.pause();
                }
                ShutdownViewControl.this.mShutdownView.setRoadAlpha1(0.0f);
                ShutdownViewControl.this.mShutdownView.setRoadAlpha2(0.0f);
                ShutdownViewControl.this.mShutdownView.setRoadAlpha3(0.0f);
            }
            if (ShutdownViewControl.this.mIsPressEmergency) {
                ShutdownViewControl.this.mShutdownView.setEmergencyPressed(true);
            }
            return true;
        }

        @Override // com.color.widget.ShutdownView.TouchEventListener
        public void onMove(float moveX, float moveY) {
            if (ShutdownViewControl.this.mIsPressHandler) {
                if (ShutdownViewControl.this.mScaleAndRatioAnimator.isRunning()) {
                    ShutdownViewControl.this.mScaleAndRatioAnimator.end();
                }
                float distance = moveX - ShutdownViewControl.this.mDownX;
                if (ShutdownViewControl.this.mShutdownView.isPortrait()) {
                    distance = moveY - ShutdownViewControl.this.mDownY;
                }
                if (distance < ((float) ShutdownViewControl.this.mRebootThreshold)) {
                    distance = (float) ShutdownViewControl.this.mRebootThreshold;
                }
                if (distance > ((float) ShutdownViewControl.this.mShutdownThreshold)) {
                    distance = (float) ShutdownViewControl.this.mShutdownThreshold;
                }
                if (distance < 0.0f) {
                    ShutdownViewControl.this.mShutdownView.setHint(0);
                } else {
                    ShutdownViewControl.this.mShutdownView.setHint(1);
                }
                ShutdownViewControl.this.mShutdownView.setHandlerTransition(distance);
                if (distance > ((float) ViewConfiguration.get(ShutdownViewControl.this.mShutdownView.getContext()).getScaledTouchSlop())) {
                    boolean unused = ShutdownViewControl.this.mIsPressEmpty = false;
                }
            }
            if (ShutdownViewControl.this.mIsPressEmergency) {
                if (ShutdownViewControl.this.mShutdownView.isPortrait()) {
                    ShutdownViewControl shutdownViewControl = ShutdownViewControl.this;
                    boolean unused2 = shutdownViewControl.mIsPressEmergency = shutdownViewControl.mShutdownView.mEmergencyBarRectF.contains((float) ((int) (moveX - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler)), (float) ((int) moveY));
                } else {
                    ShutdownViewControl shutdownViewControl2 = ShutdownViewControl.this;
                    boolean unused3 = shutdownViewControl2.mIsPressEmergency = shutdownViewControl2.mShutdownView.mEmergencyBarRectF.contains((float) ((int) moveX), (float) ((int) (moveY - ShutdownViewControl.this.mShutdownView.mCanvasStranslateXForHandler)));
                }
                ShutdownViewControl.this.mShutdownView.setEmergencyPressed(ShutdownViewControl.this.mIsPressEmergency);
            }
        }

        @Override // com.color.widget.ShutdownView.TouchEventListener
        public void onUp(float upX, float upY) {
            if (ShutdownViewControl.this.mIsPressHandler) {
                float distance = upX - ShutdownViewControl.this.mDownX;
                if (ShutdownViewControl.this.mShutdownView.isPortrait()) {
                    distance = upY - ShutdownViewControl.this.mDownY;
                }
                if (distance < ((float) (ShutdownViewControl.this.mRebootThreshold / 2))) {
                    ShutdownViewControl.this.mToRebootSpringAnimation.animateToFinalPosition((float) ShutdownViewControl.this.mRebootThreshold);
                } else if (distance > ((float) (ShutdownViewControl.this.mShutdownThreshold / 2))) {
                    ShutdownViewControl.this.mToShutdownSpringAnimation.animateToFinalPosition((float) ShutdownViewControl.this.mShutdownThreshold);
                } else {
                    ShutdownViewControl.this.mToBaseSpringAnimation.animateToFinalPosition(0.0f);
                }
            }
            if (ShutdownViewControl.this.mIsPressEmergency) {
                ShutdownViewControl.this.mShutdownView.setEmergencyPressed(false);
                if (ShutdownViewControl.this.mOnEmergencyListener != null) {
                    ShutdownViewControl.this.mOnEmergencyListener.onEmergency();
                }
            }
            if (ShutdownViewControl.this.mIsPressManuallyLock) {
                ShutdownViewControl.this.mShutdownView.setManuallyLockPressed(false);
                ShutdownViewControl.this.onManuallyLockChanged();
            }
            if (ShutdownViewControl.this.mExitAnimator == null) {
                return;
            }
            if (ShutdownViewControl.this.mIsPressEmpty || ShutdownViewControl.this.mIsPressManuallyLock) {
                ShutdownViewControl.this.mExitAnimator.start();
            }
        }

        @Override // com.color.widget.ShutdownView.TouchEventListener
        public void onCancel(float cancelX, float cancelY) {
            if (ShutdownViewControl.this.mIsPressHandler) {
                ShutdownViewControl.this.mToBaseSpringAnimation.animateToFinalPosition(0.0f);
            }
            if (ShutdownViewControl.this.mIsPressEmergency) {
                ShutdownViewControl.this.mShutdownView.setEmergencyPressed(false);
            }
            if (ShutdownViewControl.this.mIsPressManuallyLock) {
                ShutdownViewControl.this.mShutdownView.setManuallyLockPressed(false);
            }
        }

        @Override // com.color.widget.ShutdownView.TouchEventListener
        public void onPointerDown() {
        }
    };

    public interface OnCancelListener {
        void onCancel();
    }

    public interface OnEmergencyListener {
        void onEmergency();
    }

    public interface OnRebootListener {
        void onReboot();
    }

    public interface OnShutdownListener {
        void onShutdown();
    }

    public ShutdownViewControl(Context context) {
        initShutdownView(context);
        initAnimators();
    }

    private void initShutdownView(Context context) {
        this.mShutdownView = (ShutdownView) LayoutInflater.from(context).inflate(201917502, (ViewGroup) null);
        this.mShutdownView.setTouchEventListener(this.mTouchEventListener);
        this.mShutdownView.setOrientationChangeListener(this.mOrientationChangeListener);
        this.mShutdownView.setHandlerTransition(0.0f);
        this.mShutdownView.setRebootTransition(180);
        this.mShutdownView.setShutdownTransition(SHUTDOWN_TRANSITION);
        this.mShutdownView.setRoadTransition(80);
        this.mShutdownView.setIconScale(0.6f);
        this.mShutdownView.setHandlerAlpha(0.0f);
        this.mShutdownView.setScale(1.0f);
        this.mShutdownView.setOperationListener(this.mOperationListener);
        this.mRebootThreshold = this.mShutdownView.getRebootTransitionThreshold();
        this.mShutdownThreshold = this.mShutdownView.getShutdownTransitionThreshold();
        initManuallyLock(context);
    }

    private void initAnimators() {
        initEnterAnimator();
        initExitAnimator();
        initHandlerDownAnimator();
        initHandlerUpAnimator();
        initShutdownAnimator();
        initRoadAnimator();
        initToBaseEndAnimator();
    }

    private void initEnterAnimator() {
        Interpolator alphaInterpolator = new PathInterpolator(0.33f, 0.0f, 0.66f, 1.0f);
        this.mEnterAnimator = new AnimatorSet();
        this.mEnterAnimator.addListener(new SimpleAnimatorListener() {
            /* class com.color.widget.ShutdownViewControl.AnonymousClass3 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                ShutdownViewControl.this.mShutdownView.sendHandlerAccessibilityFocused();
                if (ShutdownViewControl.this.mRoadAnimatorSet != null) {
                    ShutdownViewControl.this.mRoadAnimatorSet.start();
                }
            }
        });
        Interpolator scaleInterpolator = new PathInterpolator(0.15f, 0.0f, 0.0f, 1.0f);
        ObjectAnimator rebootAnimator = ObjectAnimator.ofInt(this.mShutdownView, "rebootTransition", 180, 0);
        rebootAnimator.setDuration(400L);
        rebootAnimator.setInterpolator(scaleInterpolator);
        ObjectAnimator shutdownAnimator = ObjectAnimator.ofInt(this.mShutdownView, "shutdownTransition", SHUTDOWN_TRANSITION, 0);
        shutdownAnimator.setDuration(400L);
        shutdownAnimator.setInterpolator(scaleInterpolator);
        ObjectAnimator roadAnimator = ObjectAnimator.ofInt(this.mShutdownView, "roadTransition", 80, 0);
        roadAnimator.setDuration(400L);
        roadAnimator.setInterpolator(scaleInterpolator);
        ObjectAnimator mainAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "barAlpha", 0.0f, 0.8f);
        mainAlphaAnimator.setDuration(400L);
        mainAlphaAnimator.setInterpolator(alphaInterpolator);
        ObjectAnimator backgroundAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "backgroundAlpha", 0.0f, 0.7f);
        backgroundAlphaAnimator.setDuration(250L);
        backgroundAlphaAnimator.setInterpolator(alphaInterpolator);
        ObjectAnimator iconScaleAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "iconScale", 0.6f, 1.0f);
        iconScaleAnimator.setDuration(250L);
        iconScaleAnimator.setStartDelay(150);
        iconScaleAnimator.setInterpolator(scaleInterpolator);
        ObjectAnimator handlerAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "handlerAlpha", 0.0f, 1.0f);
        handlerAlphaAnimator.setStartDelay(150);
        handlerAlphaAnimator.setDuration(400L);
        handlerAlphaAnimator.setInterpolator(alphaInterpolator);
        ObjectAnimator cancelAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "emergencyAlpha", 0.0f, 1.0f);
        cancelAlphaAnimator.setStartDelay(150);
        cancelAlphaAnimator.setDuration(400L);
        cancelAlphaAnimator.setInterpolator(alphaInterpolator);
        ObjectAnimator forceRebootHintAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "forceRebootHintAlpha", 0.0f, 0.0f);
        forceRebootHintAlphaAnimator.setStartDelay(150);
        forceRebootHintAlphaAnimator.setDuration(400L);
        forceRebootHintAlphaAnimator.setInterpolator(alphaInterpolator);
        ObjectAnimator canvasTransactionAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "canvasStranslateXForHandler", 0.0f, 1.0f);
        canvasTransactionAnimator.setDuration(600L);
        canvasTransactionAnimator.setInterpolator(alphaInterpolator);
        Log.d("Bard_ShutDown", "initEnterAnimation");
        ObjectAnimator iconAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "iconAlpha", 0.0f, 1.0f);
        iconAnimator.setDuration(250L);
        AnimatorSet.Builder builder = this.mEnterAnimator.play(canvasTransactionAnimator).with(rebootAnimator).with(shutdownAnimator).with(roadAnimator).with(backgroundAlphaAnimator).with(iconScaleAnimator).with(handlerAlphaAnimator).with(cancelAlphaAnimator).with(forceRebootHintAlphaAnimator).with(mainAlphaAnimator).with(iconAnimator);
        if (this.mShouldShowManuallyLock) {
            ObjectAnimator manuallyAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "manuallyAlpha", 0.0f, 1.0f);
            manuallyAlphaAnimator.setStartDelay(150);
            manuallyAlphaAnimator.setDuration(400L);
            manuallyAlphaAnimator.setInterpolator(alphaInterpolator);
            builder.with(manuallyAlphaAnimator);
            return;
        }
        this.mShutdownView.setManuallyAlpha(0.0f);
    }

    private void initExitAnimator() {
        this.mExitAnimator = new AnimatorSet();
        this.mExitAnimator.setInterpolator(new PathInterpolator(0.15f, 0.0f, 0.0f, 1.0f));
        this.mExitAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.color.widget.ShutdownViewControl.AnonymousClass4 */

            @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
            public void onAnimationStart(Animator animation) {
                if (ShutdownViewControl.this.mRoadAnimatorSet != null) {
                    ShutdownViewControl.this.mRoadAnimatorSet.pause();
                }
            }

            @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
            public void onAnimationEnd(Animator animation) {
                Log.d("OppoGlobalActions", "onExitAnimatorEnd onCancel");
                if (ShutdownViewControl.this.mOnCancelListener != null) {
                    ShutdownViewControl.this.mOnCancelListener.onCancel();
                }
            }
        });
        ObjectAnimator handlerAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "handlerAlpha", 1.0f, 0.0f);
        handlerAlphaAnimator.setDuration(150L);
        handlerAlphaAnimator.setStartDelay(75);
        ObjectAnimator barAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "barAlpha", 0.8f, 0.0f);
        barAlphaAnimator.setDuration(250L);
        barAlphaAnimator.setStartDelay(75);
        ObjectAnimator emergencyAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "emergencyAlpha", 1.0f, 0.0f);
        emergencyAlphaAnimator.setDuration(250L);
        emergencyAlphaAnimator.setStartDelay(75);
        ObjectAnimator backgroundAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "backgroundAlpha", 0.7f, 0.0f);
        backgroundAlphaAnimator.setStartDelay(150);
        backgroundAlphaAnimator.setDuration(250L);
        ObjectAnimator iconAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "iconAlpha", 1.0f, 0.0f);
        iconAnimator.setStartDelay(150);
        iconAnimator.setDuration(250L);
        ShutdownView shutdownView = this.mShutdownView;
        ObjectAnimator roadAlpha1Animator = ObjectAnimator.ofFloat(shutdownView, "roadAlpha1", shutdownView.getRoadAlpha1(), 0.0f);
        roadAlpha1Animator.setStartDelay(75);
        roadAlpha1Animator.setDuration(250L);
        ShutdownView shutdownView2 = this.mShutdownView;
        ObjectAnimator roadAlpha2Animator = ObjectAnimator.ofFloat(shutdownView2, "roadAlpha2", shutdownView2.getRoadAlpha2(), 0.0f);
        roadAlpha2Animator.setStartDelay(75);
        roadAlpha2Animator.setDuration(250L);
        ShutdownView shutdownView3 = this.mShutdownView;
        ObjectAnimator roadAlpha3Animator = ObjectAnimator.ofFloat(shutdownView3, "roadAlpha3", shutdownView3.getRoadAlpha3(), 0.0f);
        roadAlpha3Animator.setStartDelay(75);
        roadAlpha3Animator.setDuration(250L);
        ShutdownView shutdownView4 = this.mShutdownView;
        ObjectAnimator forceRebootHintAlphaAnimator = ObjectAnimator.ofFloat(shutdownView4, "forceRebootHintAlpha", shutdownView4.getForceRebootHintAlpha(), 0.0f);
        forceRebootHintAlphaAnimator.setStartDelay(75);
        forceRebootHintAlphaAnimator.setDuration(250L);
        ObjectAnimator canvasTransactionAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "canvasStranslateXForHandler", 1.0f, 0.0f);
        canvasTransactionAnimator.setDuration(600L);
        AnimatorSet.Builder builder = this.mExitAnimator.play(canvasTransactionAnimator).with(barAlphaAnimator).with(emergencyAlphaAnimator).with(backgroundAlphaAnimator).with(iconAnimator).with(roadAlpha1Animator).with(roadAlpha2Animator).with(roadAlpha3Animator).with(forceRebootHintAlphaAnimator).with(handlerAlphaAnimator);
        if (this.mShouldShowManuallyLock) {
            ObjectAnimator manuallyLockExitAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "manuallyAlpha", 1.0f, 0.0f);
            manuallyLockExitAnimator.setDuration(250L);
            manuallyLockExitAnimator.setStartDelay(75);
            builder.with(manuallyLockExitAnimator);
        }
    }

    private void initHandlerDownAnimator() {
        this.mHandlerDownAnimator = new AnimatorSet();
        ObjectAnimator barAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "barAlpha", 0.8f, 0.15f);
        barAlphaAnimator.setDuration(200L);
        barAlphaAnimator.setInterpolator(new PathInterpolator(0.5f, 0.0f, 0.5f, 1.0f));
        ObjectAnimator forceRebootAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "forceRebootHintAlpha", 0.5f, 0.0f);
        forceRebootAlphaAnimator.setDuration(300L);
        forceRebootAlphaAnimator.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator handlerScaleAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "handlerScale", 1.0f, 1.2f);
        handlerScaleAnimator.setDuration(200L);
        handlerScaleAnimator.setInterpolator(new PathInterpolator(0.5f, 0.0f, 0.5f, 1.0f));
        this.mHandlerDownAnimator.play(handlerScaleAnimator);
        this.mScaleAndRatioAnimator = new AnimatorSet();
        Interpolator rebootScaleInterpolator = new PathInterpolator(0.15f, 0.0f, 0.0f, 1.0f);
        this.mScaleAndRatioAnimator.setDuration(200L);
        this.mScaleAndRatioAnimator.setInterpolator(rebootScaleInterpolator);
        ObjectAnimator rebootScaleAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "rebootScale", 1.0f, 1.0f);
        ObjectAnimator rebootRatioAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "rebootRatio", 1.0f, 0.67f);
        this.mScaleAndRatioAnimator.play(rebootRatioAnimator).with(rebootScaleAnimator).with(ObjectAnimator.ofFloat(this.mShutdownView, "shutdownRatio", 1.0f, 0.75f)).with(ObjectAnimator.ofFloat(this.mShutdownView, "shutdownScale", 1.0f, 1.0f));
    }

    private void initHandlerUpAnimator() {
        this.mHandlerUpAnimator = new AnimatorSet();
        ObjectAnimator barAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "barAlpha", 0.15f, 0.8f);
        Interpolator interpolator = new PathInterpolator(0.15f, 0.0f, 0.0f, 1.0f);
        barAlphaAnimator.setDuration(250L);
        barAlphaAnimator.setInterpolator(interpolator);
        ObjectAnimator forceRebootHintAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "forceRebootHintAlpha", 0.0f, 0.0f);
        forceRebootHintAlphaAnimator.setDuration(300L);
        forceRebootHintAlphaAnimator.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator handlerScaleAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "handlerScale", 1.2f, 1.0f);
        handlerScaleAnimator.setDuration(200L);
        handlerScaleAnimator.setInterpolator(new PathInterpolator(0.5f, 0.0f, 0.5f, 1.0f));
        this.mHandlerUpAnimator.play(handlerScaleAnimator);
        this.mToShutdownSpringAnimation = new SpringAnimation(this.mShutdownView, this.HANDLER_TRANSITION);
        SpringForce shutdownSpring = new SpringForce();
        shutdownSpring.setStiffness(5000.0f);
        shutdownSpring.setDampingRatio(1.0f);
        this.mToShutdownSpringAnimation.setSpring(shutdownSpring);
        this.mToShutdownSpringAnimation.addEndListener(this.mToShutdownSpringListener);
        this.mToRebootSpringAnimation = new SpringAnimation(this.mShutdownView, this.HANDLER_TRANSITION);
        SpringForce rebootSpring = new SpringForce();
        rebootSpring.setStiffness(5000.0f);
        rebootSpring.setDampingRatio(1.0f);
        this.mToRebootSpringAnimation.setSpring(rebootSpring);
        this.mToRebootSpringAnimation.addEndListener(this.mToRebootSpringListener);
        this.mToBaseSpringAnimation = new SpringAnimation(this.mShutdownView, this.HANDLER_TRANSITION);
        SpringForce baseSpring = new SpringForce();
        baseSpring.setStiffness(800.0f);
        baseSpring.setDampingRatio(1.0f);
        this.mToBaseSpringAnimation.setSpring(baseSpring);
        this.mToBaseSpringAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            /* class com.color.widget.ShutdownViewControl.AnonymousClass5 */

            @Override // com.color.animation.DynamicAnimation.OnAnimationEndListener
            public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
                if (ShutdownViewControl.this.mRoadAnimatorSet != null) {
                    ShutdownViewControl.this.mRoadAnimatorSet.resume();
                }
                ShutdownViewControl.this.animateWhenToBase();
            }
        });
    }

    private void initShutdownAnimator() {
        Interpolator interpolator = new PathInterpolator(0.33f, 0.0f, 0.66f, 1.0f);
        this.mShutdownAnimator = new AnimatorSet();
        this.mShutdownAnimator.setStartDelay(75);
        this.mShutdownAnimator.setDuration(400L);
        this.mShutdownAnimator.setInterpolator(interpolator);
        ObjectAnimator foregroundAlphaAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "foregroundAlpha", 0.0f, 1.0f);
        this.mShutdownAnimator.play(foregroundAlphaAnimator).with(ObjectAnimator.ofFloat(this.mShutdownView, BatteryManager.EXTRA_SCALE, 1.0f, 0.9f));
    }

    private void initRoadAnimator() {
        this.mRoadAnimatorSet = new AnimatorSet();
        this.mRoadAnimatorSet.setDuration(1075L);
        PropertyValuesHolder road1Holder = PropertyValuesHolder.ofKeyframe("roadAlpha1", Keyframe.ofFloat(0.0f, 0.1f), Keyframe.ofFloat(0.12f, 0.1f), Keyframe.ofFloat(0.44f, 0.5f), Keyframe.ofFloat(0.63f, 0.1f), Keyframe.ofFloat(1.0f, 0.1f));
        ObjectAnimator road1AlphaAnimator = ObjectAnimator.ofPropertyValuesHolder(this.mShutdownView, road1Holder);
        road1AlphaAnimator.setRepeatCount(-1);
        PropertyValuesHolder road2Holder = PropertyValuesHolder.ofKeyframe("roadAlpha2", Keyframe.ofFloat(0.0f, 0.1f), Keyframe.ofFloat(0.23f, 0.1f), Keyframe.ofFloat(0.56f, 0.5f), Keyframe.ofFloat(0.74f, 0.1f), Keyframe.ofFloat(1.0f, 0.1f));
        ObjectAnimator road2AlphaAnimator = ObjectAnimator.ofPropertyValuesHolder(this.mShutdownView, road2Holder);
        road2AlphaAnimator.setRepeatCount(-1);
        PropertyValuesHolder road3Holder = PropertyValuesHolder.ofKeyframe("roadAlpha3", Keyframe.ofFloat(0.0f, 0.1f), Keyframe.ofFloat(0.35f, 0.1f), Keyframe.ofFloat(0.67f, 0.5f), Keyframe.ofFloat(0.86f, 0.1f), Keyframe.ofFloat(1.0f, 0.1f));
        ObjectAnimator road3AlphaAnimator = ObjectAnimator.ofPropertyValuesHolder(this.mShutdownView, road3Holder);
        road3AlphaAnimator.setRepeatCount(-1);
        this.mRoadAnimatorSet.play(road1AlphaAnimator).with(road2AlphaAnimator).with(road3AlphaAnimator);
    }

    private void initToBaseEndAnimator() {
        Interpolator scaleInterpolator = new PathInterpolator(0.15f, 0.0f, 0.0f, 1.0f);
        this.mRebootScaleUpAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "rebootScale", 0.0f);
        this.mRebootRatioUpAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "rebootRatio", 0.0f);
        this.mShutdownScaleUpAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "shutdownScale", 0.0f);
        this.mShutdownRatioUpAnimator = ObjectAnimator.ofFloat(this.mShutdownView, "shutdownRatio", 0.0f);
        this.mToBaseScaleAndRatioAnimator = new AnimatorSet();
        this.mToBaseScaleAndRatioAnimator.setDuration(300L);
        this.mToBaseScaleAndRatioAnimator.setInterpolator(scaleInterpolator);
    }

    public ShutdownView getShutdownView() {
        return this.mShutdownView;
    }

    public void startEnterAnimator() {
        AnimatorSet animatorSet = this.mEnterAnimator;
        if (animatorSet != null) {
            animatorSet.start();
        }
    }

    public void tearDown() {
        Log.d("OppoGlobalActions", "tearnDown");
        this.mHandler.sendEmptyMessage(0);
    }

    /* access modifiers changed from: private */
    public void animateWhenToBase() {
        this.mRebootScaleUpAnimator.setFloatValues(this.mShutdownView.getRebootScale(), 1.0f);
        this.mRebootRatioUpAnimator.setFloatValues(this.mShutdownView.getRebootRatio(), 1.0f);
        this.mShutdownScaleUpAnimator.setFloatValues(this.mShutdownView.getShutdownScale(), 1.0f);
        this.mShutdownRatioUpAnimator.setFloatValues(this.mShutdownView.getShutdownRatio(), 1.0f);
        this.mToBaseScaleAndRatioAnimator.play(this.mRebootScaleUpAnimator).with(this.mRebootRatioUpAnimator).with(this.mShutdownScaleUpAnimator).with(this.mShutdownRatioUpAnimator);
        this.mToBaseScaleAndRatioAnimator.start();
        this.mHandlerUpAnimator.start();
    }

    public void setOnShutdownListener(OnShutdownListener shutdownListener) {
        this.mOnShutdownListener = shutdownListener;
    }

    public void setOnRebootListener(OnRebootListener rebootListener) {
        this.mOnRebootListener = rebootListener;
    }

    public void setOnCancelListener(OnCancelListener cancelListener) {
        this.mOnCancelListener = cancelListener;
    }

    public void setOnEmergencyListener(OnEmergencyListener emergencyListener) {
        this.mOnEmergencyListener = emergencyListener;
    }

    private class SimpleAnimatorListener implements Animator.AnimatorListener {
        private SimpleAnimatorListener() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }

    private void initManuallyLock(Context context) {
        int val = Settings.Secure.getIntForUser(context.getContentResolver(), "manually_lock_toggle", -1, -2);
        int strongAuthTracker = new LockPatternUtils(context).getStrongAuthForUser(ActivityManager.getCurrentUser());
        boolean z = false;
        boolean isChildrenMode = Settings.Global.getInt(context.getContentResolver(), "children_mode_on", 0) == 1;
        if (isRealme(context) && manuallyLockCanBeSeen(context) && val != 1 && strongAuthTracker != 1 && !isChildrenMode) {
            z = true;
        }
        this.mShouldShowManuallyLock = z;
        this.mShutdownView.setManuallyLockEnable(this.mShouldShowManuallyLock);
    }

    private boolean manuallyLockCanBeSeen(Context context) {
        int userId = ActivityManager.getCurrentUser();
        boolean faceEnable = ((FaceManager) context.getSystemService(Context.FACE_SERVICE)).hasEnrolledTemplates(userId) && Settings.Secure.getInt(context.getContentResolver(), "coloros_face_unlock_switch", -1) == 1;
        int fpState = Settings.Secure.getInt(context.getContentResolver(), "coloros_fingerprint_unlock_switch", -1);
        FingerprintManager fpm = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        return faceEnable || (fpm != null && fpm.isHardwareDetected() && fpm.hasEnrolledFingerprints(userId) && fpState == 1 && !isFpDisabledByDPM(userId, context));
    }

    private boolean isFpDisabledByDPM(int userId, Context context) {
        return (((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).getKeyguardDisabledFeatures(null, userId) & 32) != 0;
    }

    private boolean isRealme(Context context) {
        return "realme".equalsIgnoreCase(SystemProperties.get("ro.product.brand.sub", ""));
    }

    /* access modifiers changed from: private */
    public void onManuallyLockChanged() {
        Context context = this.mShutdownView.getContext();
        Settings.Secure.putIntForUser(context.getContentResolver(), "manually_lock_toggle", 1, -2);
        lockKeyguard(context);
    }

    private void lockKeyguard(Context context) {
        try {
            new LockPatternUtils(context).requireStrongAuth(32, -1);
            WindowManagerGlobal.getWindowManagerService().lockNow(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
