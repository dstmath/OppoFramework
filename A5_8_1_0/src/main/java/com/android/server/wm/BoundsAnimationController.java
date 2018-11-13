package com.android.server.wm;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.server.display.OppoBrightUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BoundsAnimationController {
    private static final boolean DEBUG = WindowManagerDebugConfig.DEBUG_ANIM;
    private static final int DEBUG_ANIMATION_SLOW_DOWN_FACTOR = 1;
    private static final boolean DEBUG_LOCAL = false;
    private static final int DEFAULT_TRANSITION_DURATION = 425;
    public static final int NO_PIP_MODE_CHANGED_CALLBACKS = 0;
    public static final int SCHEDULE_PIP_MODE_CHANGED_ON_END = 2;
    public static final int SCHEDULE_PIP_MODE_CHANGED_ON_START = 1;
    private static final String TAG = "WindowManager";
    private static final int WAIT_FOR_DRAW_TIMEOUT_MS = 3000;
    private final AnimationHandler mAnimationHandler;
    private final AppTransition mAppTransition;
    private final AppTransitionNotifier mAppTransitionNotifier = new AppTransitionNotifier(this, null);
    private final Interpolator mFastOutSlowInInterpolator;
    private boolean mFinishAnimationAfterTransition = false;
    private final Handler mHandler;
    private ArrayMap<BoundsAnimationTarget, BoundsAnimator> mRunningAnimations = new ArrayMap();

    private final class AppTransitionNotifier extends AppTransitionListener implements Runnable {
        /* synthetic */ AppTransitionNotifier(BoundsAnimationController this$0, AppTransitionNotifier -this1) {
            this();
        }

        private AppTransitionNotifier() {
        }

        public void onAppTransitionCancelledLocked() {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "onAppTransitionCancelledLocked: mFinishAnimationAfterTransition=" + BoundsAnimationController.this.mFinishAnimationAfterTransition);
            }
            animationFinished();
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "onAppTransitionFinishedLocked: mFinishAnimationAfterTransition=" + BoundsAnimationController.this.mFinishAnimationAfterTransition);
            }
            animationFinished();
        }

        private void animationFinished() {
            if (BoundsAnimationController.this.mFinishAnimationAfterTransition) {
                BoundsAnimationController.this.mHandler.removeCallbacks(this);
                BoundsAnimationController.this.mHandler.post(this);
            }
        }

        public void run() {
            for (int i = 0; i < BoundsAnimationController.this.mRunningAnimations.size(); i++) {
                ((BoundsAnimator) BoundsAnimationController.this.mRunningAnimations.valueAt(i)).onAnimationEnd(null);
            }
        }
    }

    final class BoundsAnimator extends ValueAnimator implements AnimatorUpdateListener, AnimatorListener {
        private final Rect mFrom = new Rect();
        private final int mFrozenTaskHeight;
        private final int mFrozenTaskWidth;
        private boolean mMoveFromFullscreen;
        private boolean mMoveToFullscreen;
        private int mPrevSchedulePipModeChangedState;
        private final Runnable mResumeRunnable = new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 6, this);
        private int mSchedulePipModeChangedState;
        private boolean mSkipAnimationEnd;
        private boolean mSkipFinalResize;
        private final BoundsAnimationTarget mTarget;
        private final Rect mTmpRect = new Rect();
        private final Rect mTmpTaskBounds = new Rect();
        private final Rect mTo = new Rect();

        BoundsAnimator(BoundsAnimationTarget target, Rect from, Rect to, int schedulePipModeChangedState, int prevShedulePipModeChangedState, boolean moveFromFullscreen, boolean moveToFullscreen) {
            this.mTarget = target;
            this.mFrom.set(from);
            this.mTo.set(to);
            this.mSchedulePipModeChangedState = schedulePipModeChangedState;
            this.mPrevSchedulePipModeChangedState = prevShedulePipModeChangedState;
            this.mMoveFromFullscreen = moveFromFullscreen;
            this.mMoveToFullscreen = moveToFullscreen;
            addUpdateListener(this);
            addListener(this);
            if (animatingToLargerSize()) {
                this.mFrozenTaskWidth = this.mTo.width();
                this.mFrozenTaskHeight = this.mTo.height();
                return;
            }
            this.mFrozenTaskWidth = this.mFrom.width();
            this.mFrozenTaskHeight = this.mFrom.height();
        }

        public void onAnimationStart(Animator animation) {
            boolean z = true;
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "onAnimationStart: mTarget=" + this.mTarget + " mPrevSchedulePipModeChangedState=" + this.mPrevSchedulePipModeChangedState + " mSchedulePipModeChangedState=" + this.mSchedulePipModeChangedState);
            }
            BoundsAnimationController.this.mFinishAnimationAfterTransition = false;
            this.mTmpRect.set(this.mFrom.left, this.mFrom.top, this.mFrom.left + this.mFrozenTaskWidth, this.mFrom.top + this.mFrozenTaskHeight);
            BoundsAnimationController.this.updateBooster();
            if (this.mPrevSchedulePipModeChangedState == 0) {
                BoundsAnimationTarget boundsAnimationTarget = this.mTarget;
                if (this.mSchedulePipModeChangedState != 1) {
                    z = false;
                }
                boundsAnimationTarget.onAnimationStart(z, false);
                if (this.mMoveFromFullscreen) {
                    pause();
                }
            } else if (this.mPrevSchedulePipModeChangedState == 2 && this.mSchedulePipModeChangedState == 1) {
                this.mTarget.onAnimationStart(true, true);
            }
            if (animatingToLargerSize()) {
                this.mTarget.setPinnedStackSize(this.mFrom, this.mTmpRect);
                if (this.mMoveToFullscreen) {
                    pause();
                }
            }
        }

        public void pause() {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "pause: waiting for windows drawn");
            }
            super.pause();
            BoundsAnimationController.this.mHandler.postDelayed(this.mResumeRunnable, 3000);
        }

        /* renamed from: resume */
        public void lambda$-com_android_server_wm_BoundsAnimationController$BoundsAnimator_7429() {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "resume:");
            }
            BoundsAnimationController.this.mHandler.removeCallbacks(this.mResumeRunnable);
            super.resume();
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            float remains = 1.0f - value;
            this.mTmpRect.left = (int) (((((float) this.mFrom.left) * remains) + (((float) this.mTo.left) * value)) + 0.5f);
            this.mTmpRect.top = (int) (((((float) this.mFrom.top) * remains) + (((float) this.mTo.top) * value)) + 0.5f);
            this.mTmpRect.right = (int) (((((float) this.mFrom.right) * remains) + (((float) this.mTo.right) * value)) + 0.5f);
            this.mTmpRect.bottom = (int) (((((float) this.mFrom.bottom) * remains) + (((float) this.mTo.bottom) * value)) + 0.5f);
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "animateUpdate: mTarget=" + this.mTarget + " mBounds=" + this.mTmpRect + " from=" + this.mFrom + " mTo=" + this.mTo + " value=" + value + " remains=" + remains);
            }
            this.mTmpTaskBounds.set(this.mTmpRect.left, this.mTmpRect.top, this.mTmpRect.left + this.mFrozenTaskWidth, this.mTmpRect.top + this.mFrozenTaskHeight);
            if (!this.mTarget.setPinnedStackSize(this.mTmpRect, this.mTmpTaskBounds)) {
                if (BoundsAnimationController.DEBUG) {
                    Slog.d(BoundsAnimationController.TAG, "animateUpdate: cancelled");
                }
                if (this.mSchedulePipModeChangedState == 1) {
                    this.mSchedulePipModeChangedState = 2;
                }
                cancelAndCallAnimationEnd();
            }
        }

        public void onAnimationEnd(Animator animation) {
            boolean z = true;
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "onAnimationEnd: mTarget=" + this.mTarget + " mSkipFinalResize=" + this.mSkipFinalResize + " mFinishAnimationAfterTransition=" + BoundsAnimationController.this.mFinishAnimationAfterTransition + " mAppTransitionIsRunning=" + BoundsAnimationController.this.mAppTransition.isRunning() + " callers=" + Debug.getCallers(2));
            }
            if (!BoundsAnimationController.this.mAppTransition.isRunning() || (BoundsAnimationController.this.mFinishAnimationAfterTransition ^ 1) == 0) {
                if (!this.mSkipAnimationEnd) {
                    if (BoundsAnimationController.DEBUG) {
                        Slog.d(BoundsAnimationController.TAG, "onAnimationEnd: mTarget=" + this.mTarget + " moveToFullscreen=" + this.mMoveToFullscreen);
                    }
                    BoundsAnimationTarget boundsAnimationTarget = this.mTarget;
                    if (this.mSchedulePipModeChangedState != 2) {
                        z = false;
                    }
                    boundsAnimationTarget.onAnimationEnd(z, !this.mSkipFinalResize ? this.mTo : null, this.mMoveToFullscreen);
                }
                removeListener(this);
                removeUpdateListener(this);
                BoundsAnimationController.this.mRunningAnimations.remove(this.mTarget);
                BoundsAnimationController.this.updateBooster();
                return;
            }
            BoundsAnimationController.this.mFinishAnimationAfterTransition = true;
        }

        public void onAnimationCancel(Animator animation) {
            this.mSkipFinalResize = true;
            this.mMoveToFullscreen = false;
        }

        private void cancelAndCallAnimationEnd() {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "cancelAndCallAnimationEnd: mTarget=" + this.mTarget);
            }
            this.mSkipAnimationEnd = false;
            super.cancel();
        }

        public void cancel() {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "cancel: mTarget=" + this.mTarget);
            }
            this.mSkipAnimationEnd = true;
            super.cancel();
        }

        boolean isAnimatingTo(Rect bounds) {
            return this.mTo.equals(bounds);
        }

        boolean animatingToLargerSize() {
            return this.mFrom.width() * this.mFrom.height() <= this.mTo.width() * this.mTo.height();
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public AnimationHandler getAnimationHandler() {
            if (BoundsAnimationController.this.mAnimationHandler != null) {
                return BoundsAnimationController.this.mAnimationHandler;
            }
            return super.getAnimationHandler();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SchedulePipModeChangedState {
    }

    BoundsAnimationController(Context context, AppTransition transition, Handler handler, AnimationHandler animationHandler) {
        this.mHandler = handler;
        this.mAppTransition = transition;
        this.mAppTransition.registerListenerLocked(this.mAppTransitionNotifier);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mAnimationHandler = animationHandler;
    }

    public void animateBounds(BoundsAnimationTarget target, Rect from, Rect to, int animationDuration, int schedulePipModeChangedState, boolean moveFromFullscreen, boolean moveToFullscreen) {
        animateBoundsImpl(target, from, to, animationDuration, schedulePipModeChangedState, moveFromFullscreen, moveToFullscreen);
    }

    BoundsAnimator animateBoundsImpl(BoundsAnimationTarget target, Rect from, Rect to, int animationDuration, int schedulePipModeChangedState, boolean moveFromFullscreen, boolean moveToFullscreen) {
        BoundsAnimator existing = (BoundsAnimator) this.mRunningAnimations.get(target);
        boolean replacing = existing != null;
        int prevSchedulePipModeChangedState = 0;
        if (DEBUG) {
            Slog.d(TAG, "animateBounds: target=" + target + " from=" + from + " to=" + to + " schedulePipModeChangedState=" + schedulePipModeChangedState + " replacing=" + replacing);
        }
        if (replacing) {
            if (existing.isAnimatingTo(to)) {
                if (DEBUG) {
                    Slog.d(TAG, "animateBounds: same destination as existing=" + existing + " ignoring...");
                }
                return existing;
            }
            prevSchedulePipModeChangedState = existing.mSchedulePipModeChangedState;
            if (existing.mSchedulePipModeChangedState == 1) {
                if (schedulePipModeChangedState != 1) {
                    if (DEBUG) {
                        Slog.d(TAG, "animateBounds: fullscreen animation canceled, callback on start already processed, schedule deferred update on end");
                    }
                    schedulePipModeChangedState = 2;
                } else if (DEBUG) {
                    Slog.d(TAG, "animateBounds: still animating to fullscreen, keep existing deferred state");
                }
            } else if (existing.mSchedulePipModeChangedState == 2) {
                if (schedulePipModeChangedState != 1) {
                    if (DEBUG) {
                        Slog.d(TAG, "animateBounds: still animating from fullscreen, keep existing deferred state");
                    }
                    schedulePipModeChangedState = 2;
                } else if (DEBUG) {
                    Slog.d(TAG, "animateBounds: non-fullscreen animation canceled, callback on start will be processed");
                }
            }
            existing.cancel();
        }
        BoundsAnimator animator = new BoundsAnimator(target, from, to, schedulePipModeChangedState, prevSchedulePipModeChangedState, moveFromFullscreen, moveToFullscreen);
        this.mRunningAnimations.put(target, animator);
        animator.setFloatValues(new float[]{OppoBrightUtils.MIN_LUX_LIMITI, 1.0f});
        if (animationDuration == -1) {
            animationDuration = DEFAULT_TRANSITION_DURATION;
        }
        animator.setDuration((long) (animationDuration * 1));
        animator.setInterpolator(this.mFastOutSlowInInterpolator);
        animator.start();
        return animator;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public void onAllWindowsDrawn() {
        if (DEBUG) {
            Slog.d(TAG, "onAllWindowsDrawn:");
        }
        this.mHandler.post(new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 7, this));
    }

    private void resume() {
        for (int i = 0; i < this.mRunningAnimations.size(); i++) {
            ((BoundsAnimator) this.mRunningAnimations.valueAt(i)).lambda$-com_android_server_wm_BoundsAnimationController$BoundsAnimator_7429();
        }
    }

    private void updateBooster() {
        WindowManagerService.sThreadPriorityBooster.setBoundsAnimationRunning(this.mRunningAnimations.isEmpty() ^ 1);
    }
}
