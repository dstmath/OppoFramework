package com.android.server.wm;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.animation.LinearInterpolator;
import com.android.server.display.OppoBrightUtils;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class BoundsAnimationController {
    private static final boolean DEBUG = false;
    private static final int DEBUG_ANIMATION_SLOW_DOWN_FACTOR = 1;
    private static final boolean DEBUG_LOCAL = false;
    private static final String TAG = null;
    private final AppTransition mAppTransition;
    private final AppTransitionNotifier mAppTransitionNotifier;
    private boolean mFinishAnimationAfterTransition;
    private final Handler mHandler;
    private ArrayMap<AnimateBoundsUser, BoundsAnimator> mRunningAnimations;

    public interface AnimateBoundsUser {
        void getFullScreenBounds(Rect rect);

        void moveToFullscreen();

        void onAnimationEnd();

        void onAnimationStart();

        boolean setPinnedStackSize(Rect rect, Rect rect2);

        boolean setSize(Rect rect);
    }

    private final class AppTransitionNotifier extends AppTransitionListener implements Runnable {
        /* synthetic */ AppTransitionNotifier(BoundsAnimationController this$0, AppTransitionNotifier appTransitionNotifier) {
            this();
        }

        private AppTransitionNotifier() {
        }

        public void onAppTransitionCancelledLocked() {
            animationFinished();
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
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

    private final class BoundsAnimator extends ValueAnimator implements AnimatorUpdateListener, AnimatorListener {
        private final Rect mFrom;
        private final int mFrozenTaskHeight;
        private final int mFrozenTaskWidth;
        private final boolean mMoveToFullScreen;
        private final boolean mReplacement;
        private final AnimateBoundsUser mTarget;
        private final Rect mTmpRect = new Rect();
        private final Rect mTmpTaskBounds = new Rect();
        private final Rect mTo;
        private boolean mWillReplace;

        BoundsAnimator(AnimateBoundsUser target, Rect from, Rect to, boolean moveToFullScreen, boolean replacement) {
            this.mTarget = target;
            this.mFrom = from;
            this.mTo = to;
            this.mMoveToFullScreen = moveToFullScreen;
            this.mReplacement = replacement;
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

        boolean animatingToLargerSize() {
            if (this.mFrom.width() * this.mFrom.height() > this.mTo.width() * this.mTo.height()) {
                return false;
            }
            return true;
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
                animation.cancel();
            }
        }

        public void onAnimationStart(Animator animation) {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "onAnimationStart: mTarget=" + this.mTarget + " mReplacement=" + this.mReplacement);
            }
            BoundsAnimationController.this.mFinishAnimationAfterTransition = false;
            if (!this.mReplacement) {
                this.mTarget.onAnimationStart();
            }
            if (animatingToLargerSize()) {
                this.mTmpRect.set(this.mFrom.left, this.mFrom.top, this.mFrom.left + this.mFrozenTaskWidth, this.mFrom.top + this.mFrozenTaskHeight);
                this.mTarget.setPinnedStackSize(this.mFrom, this.mTmpRect);
            }
        }

        public void onAnimationEnd(Animator animation) {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "onAnimationEnd: mTarget=" + this.mTarget + " mMoveToFullScreen=" + this.mMoveToFullScreen + " mWillReplace=" + this.mWillReplace);
            }
            if (!BoundsAnimationController.this.mAppTransition.isRunning() || BoundsAnimationController.this.mFinishAnimationAfterTransition) {
                finishAnimation();
                this.mTarget.setPinnedStackSize(this.mTo, null);
                if (this.mMoveToFullScreen && !this.mWillReplace) {
                    this.mTarget.moveToFullscreen();
                }
                return;
            }
            BoundsAnimationController.this.mFinishAnimationAfterTransition = true;
        }

        public void onAnimationCancel(Animator animation) {
            finishAnimation();
        }

        public void cancel() {
            this.mWillReplace = true;
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "cancel: willReplace mTarget=" + this.mTarget);
            }
            super.cancel();
        }

        public boolean isAnimatingTo(Rect bounds) {
            return this.mTo.equals(bounds);
        }

        private void finishAnimation() {
            if (BoundsAnimationController.DEBUG) {
                Slog.d(BoundsAnimationController.TAG, "finishAnimation: mTarget=" + this.mTarget + " callers" + Debug.getCallers(2));
            }
            if (!this.mWillReplace) {
                this.mTarget.onAnimationEnd();
            }
            removeListener(this);
            removeUpdateListener(this);
            BoundsAnimationController.this.mRunningAnimations.remove(this.mTarget);
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.BoundsAnimationController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.BoundsAnimationController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.BoundsAnimationController.<clinit>():void");
    }

    BoundsAnimationController(AppTransition transition, Handler handler) {
        this.mRunningAnimations = new ArrayMap();
        this.mAppTransitionNotifier = new AppTransitionNotifier(this, null);
        this.mFinishAnimationAfterTransition = false;
        this.mHandler = handler;
        this.mAppTransition = transition;
        this.mAppTransition.registerListenerLocked(this.mAppTransitionNotifier);
    }

    void animateBounds(AnimateBoundsUser target, Rect from, Rect to, int animationDuration) {
        boolean moveToFullscreen = false;
        if (to == null) {
            to = new Rect();
            target.getFullScreenBounds(to);
            moveToFullscreen = true;
        }
        BoundsAnimator existing = (BoundsAnimator) this.mRunningAnimations.get(target);
        boolean replacing = existing != null;
        if (DEBUG) {
            Slog.d(TAG, "animateBounds: target=" + target + " from=" + from + " to=" + to + " moveToFullscreen=" + moveToFullscreen + " replacing=" + replacing);
        }
        if (replacing) {
            if (existing.isAnimatingTo(to)) {
                if (DEBUG) {
                    Slog.d(TAG, "animateBounds: same destination as existing=" + existing + " ignoring...");
                }
                return;
            }
            existing.cancel();
        }
        BoundsAnimator animator = new BoundsAnimator(target, from, to, moveToFullscreen, replacing);
        this.mRunningAnimations.put(target, animator);
        animator.setFloatValues(new float[]{OppoBrightUtils.MIN_LUX_LIMITI, 1.0f});
        if (animationDuration == -1) {
            animationDuration = 336;
        }
        animator.setDuration((long) (animationDuration * 1));
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }
}
