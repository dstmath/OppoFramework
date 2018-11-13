package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.MagnificationSpec;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import com.android.server.LocationManagerService;
import com.android.server.display.OppoBrightUtils;
import java.io.PrintWriter;

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
class WindowStateAnimator {
    static final int COMMIT_DRAW_PENDING = 2;
    static boolean DEBUG_ANIM = false;
    static boolean DEBUG_LAYERS = false;
    static boolean DEBUG_ORIENTATION = false;
    static boolean DEBUG_STARTING_WINDOW = false;
    static boolean DEBUG_SURFACE_TRACE = false;
    static boolean DEBUG_VISIBILITY = false;
    static final int DRAW_PENDING = 1;
    static final int HAS_DRAWN = 4;
    static final int NO_SURFACE = 0;
    static final long PENDING_TRANSACTION_FINISH_WAIT_TIME = 100;
    static final int READY_TO_SHOW = 3;
    static boolean SHOW_LIGHT_TRANSACTIONS = false;
    static boolean SHOW_SURFACE_ALLOC = false;
    static boolean SHOW_TRANSACTIONS = false;
    static final int STACK_CLIP_AFTER_ANIM = 0;
    static final int STACK_CLIP_BEFORE_ANIM = 1;
    static final int STACK_CLIP_NONE = 2;
    static final String TAG = null;
    static final int WINDOW_FREEZE_LAYER = 2000000;
    static boolean localLOGV;
    float mAlpha;
    private int mAnimDx;
    private int mAnimDy;
    int mAnimLayer;
    private boolean mAnimateMove;
    boolean mAnimating;
    Animation mAnimation;
    boolean mAnimationIsEntrance;
    private boolean mAnimationStartDelayed;
    long mAnimationStartTime;
    final WindowAnimator mAnimator;
    AppWindowAnimator mAppAnimator;
    final WindowStateAnimator mAttachedWinAnimator;
    int mAttrType;
    Rect mClipRect;
    final Context mContext;
    boolean mDestroyDeferredFlag;
    private boolean mDestroyPreservedSurfaceUponRedraw;
    boolean mDrawNeeded;
    int mDrawState;
    float mDsDx;
    float mDsDy;
    float mDtDx;
    float mDtDy;
    boolean mEnterAnimationPending;
    boolean mEnteringAnimation;
    float mExtraHScale;
    float mExtraVScale;
    boolean mForceScaleUntilResize;
    boolean mHasClipRect;
    boolean mHasLocalTransformation;
    boolean mHasTransformation;
    boolean mHaveMatrix;
    final boolean mIsKeyguard;
    final boolean mIsWallpaper;
    boolean mKeyguardGoingAwayAnimation;
    boolean mKeyguardGoingAwayWithWallpaper;
    float mLastAlpha;
    long mLastAnimationTime;
    Rect mLastClipRect;
    float mLastDsDx;
    float mLastDsDy;
    float mLastDtDx;
    float mLastDtDy;
    Rect mLastFinalClipRect;
    boolean mLastHidden;
    int mLastLayer;
    private final Rect mLastSystemDecorRect;
    boolean mLocalAnimating;
    private WindowSurfaceController mPendingDestroySurface;
    final WindowManagerPolicy mPolicy;
    boolean mReportSurfaceResized;
    final WindowManagerService mService;
    final Session mSession;
    float mShownAlpha;
    int mStackClip;
    WindowSurfaceController mSurfaceController;
    boolean mSurfaceDestroyDeferred;
    int mSurfaceFormat;
    boolean mSurfaceResized;
    private final Rect mSystemDecorRect;
    Rect mTmpClipRect;
    Rect mTmpFinalClipRect;
    private final Rect mTmpSize;
    Rect mTmpStackBounds;
    final Transformation mTransformation;
    final WallpaperController mWallpaperControllerLocked;
    boolean mWasAnimating;
    final WindowState mWin;
    final int mWmDuration;
    final int mWmOffset;
    final int mWmSleep;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowStateAnimator.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowStateAnimator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowStateAnimator.<clinit>():void");
    }

    String drawStateToString() {
        switch (this.mDrawState) {
            case 0:
                return "NO_SURFACE";
            case 1:
                return "DRAW_PENDING";
            case 2:
                return "COMMIT_DRAW_PENDING";
            case 3:
                return "READY_TO_SHOW";
            case 4:
                return "HAS_DRAWN";
            default:
                return Integer.toString(this.mDrawState);
        }
    }

    WindowStateAnimator(WindowState win) {
        WindowStateAnimator windowStateAnimator;
        AppWindowAnimator appWindowAnimator = null;
        this.mTransformation = new Transformation();
        this.mStackClip = 1;
        this.mShownAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mLastAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mClipRect = new Rect();
        this.mTmpClipRect = new Rect();
        this.mTmpFinalClipRect = new Rect();
        this.mLastClipRect = new Rect();
        this.mLastFinalClipRect = new Rect();
        this.mTmpStackBounds = new Rect();
        this.mSystemDecorRect = new Rect();
        this.mLastSystemDecorRect = new Rect();
        this.mAnimateMove = false;
        this.mDsDx = 1.0f;
        this.mDtDx = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mDsDy = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mDtDy = 1.0f;
        this.mLastDsDx = 1.0f;
        this.mLastDtDx = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mLastDsDy = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mLastDtDy = 1.0f;
        this.mExtraHScale = 1.0f;
        this.mExtraVScale = 1.0f;
        this.mTmpSize = new Rect();
        this.mDestroyDeferredFlag = false;
        this.mWmDuration = SystemProperties.getInt("debug.wm.duration", -1);
        this.mWmOffset = SystemProperties.getInt("debug.wm.offset", -1);
        this.mWmSleep = SystemProperties.getInt("debug.wm.sleep", -1);
        this.mDrawNeeded = true;
        WindowManagerService service = win.mService;
        this.mService = service;
        this.mAnimator = service.mAnimator;
        this.mPolicy = service.mPolicy;
        this.mContext = service.mContext;
        DisplayContent displayContent = win.getDisplayContent();
        if (displayContent != null) {
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            this.mAnimDx = displayInfo.appWidth;
            this.mAnimDy = displayInfo.appHeight;
        } else {
            Slog.w(TAG, "WindowStateAnimator ctor: Display has been removed");
        }
        this.mWin = win;
        if (win.mAttachedWindow == null) {
            windowStateAnimator = null;
        } else {
            windowStateAnimator = win.mAttachedWindow.mWinAnimator;
        }
        this.mAttachedWinAnimator = windowStateAnimator;
        if (win.mAppToken != null) {
            appWindowAnimator = win.mAppToken.mAppAnimator;
        }
        this.mAppAnimator = appWindowAnimator;
        this.mSession = win.mSession;
        this.mAttrType = win.mAttrs.type;
        this.mIsWallpaper = win.mIsWallpaper;
        this.mWallpaperControllerLocked = this.mService.mWallpaperControllerLocked;
        this.mIsKeyguard = "Keyguard".equals(this.mWin.mAttrs.getTitle());
    }

    public boolean isKeyguard() {
        return this.mIsKeyguard;
    }

    public void setAnimation(Animation anim, long startTime, int stackClip) {
        int i = 0;
        if (localLOGV) {
            Slog.v(TAG, "Setting animation in " + this + ": " + anim);
        }
        String name = null;
        if (!(this.mAppAnimator == null || this.mAppAnimator.mAppToken == null || this.mAppAnimator.mAppToken.token == null)) {
            name = this.mAppAnimator.mAppToken.token.toString();
        }
        this.mAnimating = false;
        this.mLocalAnimating = false;
        this.mAnimation = anim;
        this.mAnimation.restrictDuration(10000);
        if (name == null || !name.contains("plugin.luckymoney.ui.LuckyMoney")) {
            this.mAnimation.scaleCurrentDuration(this.mService.getWindowAnimationScaleLocked());
        } else {
            this.mAnimation.scaleCurrentDuration(OppoBrightUtils.MIN_LUX_LIMITI);
            Slog.d(TAG, "Cancel Animation, name = " + name);
        }
        this.mTransformation.clear();
        Transformation transformation = this.mTransformation;
        if (!this.mLastHidden) {
            i = 1;
        }
        transformation.setAlpha((float) i);
        this.mHasLocalTransformation = true;
        this.mAnimationStartTime = startTime;
        this.mStackClip = stackClip;
    }

    public void setAnimation(Animation anim, int stackClip) {
        setAnimation(anim, -1, stackClip);
    }

    public void setAnimation(Animation anim) {
        setAnimation(anim, -1, 0);
    }

    public void clearAnimation() {
        if (this.mAnimation != null) {
            this.mAnimating = true;
            this.mLocalAnimating = false;
            this.mAnimation.cancel();
            this.mAnimation = null;
            this.mKeyguardGoingAwayAnimation = false;
            this.mKeyguardGoingAwayWithWallpaper = false;
            this.mStackClip = 1;
        }
    }

    boolean isAnimationSet() {
        if (this.mAnimation == null && (this.mAttachedWinAnimator == null || this.mAttachedWinAnimator.mAnimation == null)) {
            return this.mAppAnimator != null ? this.mAppAnimator.isAnimating() : false;
        } else {
            return true;
        }
    }

    boolean isAnimationStarting() {
        return isAnimationSet() && !this.mAnimating;
    }

    boolean isDummyAnimation() {
        if (this.mAppAnimator == null || this.mAppAnimator.animation != AppWindowAnimator.sDummyAnimation) {
            return false;
        }
        return true;
    }

    boolean isWindowAnimationSet() {
        return this.mAnimation != null;
    }

    boolean isWaitingForOpening() {
        if (this.mService.mAppTransition.isTransitionSet() && isDummyAnimation()) {
            return this.mService.mOpeningApps.contains(this.mWin.mAppToken);
        }
        return false;
    }

    void cancelExitAnimationForNextAnimationLocked() {
        if (DEBUG_ANIM) {
            Slog.d(TAG, "cancelExitAnimationForNextAnimationLocked: " + this.mWin);
        }
        if (this.mAnimation != null) {
            this.mAnimation.cancel();
            this.mAnimation = null;
            this.mLocalAnimating = false;
            this.mWin.destroyOrSaveSurface();
        }
    }

    private boolean stepAnimation(long currentTime) {
        if (this.mAnimation == null || !this.mLocalAnimating) {
            return false;
        }
        currentTime = getAnimationFrameTime(this.mAnimation, currentTime);
        this.mTransformation.clear();
        boolean more = this.mAnimation.getTransformation(currentTime, this.mTransformation);
        if (this.mAnimationStartDelayed && this.mAnimationIsEntrance) {
            this.mTransformation.setAlpha(OppoBrightUtils.MIN_LUX_LIMITI);
        }
        return more;
    }

    boolean stepAnimationLocked(long currentTime) {
        this.mWasAnimating = this.mAnimating;
        DisplayContent displayContent = this.mWin.getDisplayContent();
        if (displayContent != null && this.mService.okToDisplay()) {
            if (this.mWin.isDrawnLw() && this.mAnimation != null) {
                this.mHasTransformation = true;
                this.mHasLocalTransformation = true;
                if (!this.mLocalAnimating) {
                    long j;
                    if (DEBUG_ANIM) {
                        Slog.v(TAG, "Starting animation in " + this + " @ " + currentTime + ": ww=" + this.mWin.mFrame.width() + " wh=" + this.mWin.mFrame.height() + " dx=" + this.mAnimDx + " dy=" + this.mAnimDy + " scale=" + this.mService.getWindowAnimationScaleLocked());
                    }
                    this.mService.mLayersController.setDockDividerAnimLayerAdjustment(1000);
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    if (this.mAnimateMove) {
                        this.mAnimateMove = false;
                        this.mAnimation.initialize(this.mWin.mFrame.width(), this.mWin.mFrame.height(), this.mAnimDx, this.mAnimDy);
                    } else {
                        this.mAnimation.initialize(this.mWin.mFrame.width(), this.mWin.mFrame.height(), displayInfo.appWidth, displayInfo.appHeight);
                    }
                    this.mAnimDx = displayInfo.appWidth;
                    this.mAnimDy = displayInfo.appHeight;
                    Animation animation = this.mAnimation;
                    if (this.mAnimationStartTime != -1) {
                        j = this.mAnimationStartTime;
                    } else {
                        j = currentTime;
                    }
                    animation.setStartTime(j);
                    this.mLocalAnimating = true;
                    this.mAnimating = true;
                }
                if (this.mAnimation != null && this.mLocalAnimating) {
                    this.mLastAnimationTime = currentTime;
                    if (stepAnimation(currentTime)) {
                        return true;
                    }
                }
                if (DEBUG_ANIM) {
                    Slog.v(TAG, "Finished animation in " + this + " @ " + currentTime);
                }
            }
            this.mHasLocalTransformation = false;
            if ((!this.mLocalAnimating || this.mAnimationIsEntrance) && this.mAppAnimator != null && this.mAppAnimator.animation != null) {
                this.mAnimating = true;
                this.mHasTransformation = true;
                this.mTransformation.clear();
                return false;
            } else if (this.mHasTransformation) {
                this.mAnimating = true;
            } else if (isAnimationSet()) {
                this.mAnimating = true;
            }
        } else if (this.mAnimation != null) {
            this.mAnimating = true;
        }
        if (!this.mAnimating && !this.mLocalAnimating) {
            return false;
        }
        Trace.traceBegin(4128, "win animation done");
        if (DEBUG_ANIM) {
            Slog.v(TAG, "Animation done in " + this + ": exiting=" + this.mWin.mAnimatingExit + ", reportedVisible=" + (this.mWin.mAppToken != null ? this.mWin.mAppToken.reportedVisible : false));
        }
        Trace.traceEnd(4128);
        this.mAnimating = false;
        this.mKeyguardGoingAwayAnimation = false;
        this.mKeyguardGoingAwayWithWallpaper = false;
        this.mLocalAnimating = false;
        if (this.mAnimation != null) {
            this.mAnimation.cancel();
            this.mAnimation = null;
        }
        if (this.mAnimator.mWindowDetachedWallpaper == this.mWin) {
            this.mAnimator.mWindowDetachedWallpaper = null;
        }
        this.mAnimLayer = this.mWin.mLayer + this.mService.mLayersController.getSpecialWindowAnimLayerAdjustment(this.mWin);
        if (DEBUG_LAYERS) {
            Slog.v(TAG, "Stepping win " + this + " anim layer: " + this.mAnimLayer);
        }
        this.mService.mLayersController.updateDockDividerAnimLayer(0);
        this.mHasTransformation = false;
        this.mHasLocalTransformation = false;
        this.mStackClip = 1;
        this.mWin.checkPolicyVisibilityChange();
        this.mTransformation.clear();
        if (this.mDrawState == 4 && this.mWin.mAttrs.type == 3 && this.mWin.mAppToken != null && this.mWin.mAppToken.firstWindowDrawn && this.mWin.mAppToken.startingData != null) {
            if (DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Finish starting " + this.mWin.mToken + ": first real window done animating");
            }
            this.mService.mFinishedStarting.add(this.mWin.mAppToken);
            this.mService.mH.sendEmptyMessage(7);
            if (this.mService.isFastStartingWindowSupport() && this.mService.isCacheFirstFrame()) {
                doCacheBitmap();
            }
        } else if (this.mAttrType == 2000 && this.mWin.mPolicyVisibility && displayContent != null) {
            displayContent.layoutNeeded = true;
        }
        finishExit();
        int displayId = this.mWin.getDisplayId();
        this.mAnimator.setPendingLayoutChanges(displayId, 8);
        if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
            this.mService.mWindowPlacerLocked.debugLayoutRepeats("WindowStateAnimator", this.mAnimator.getPendingLayoutChanges(displayId));
        }
        if (this.mWin.mAppToken != null) {
            this.mWin.mAppToken.updateReportedVisibilityLocked();
        }
        return false;
    }

    void finishExit() {
        if (DEBUG_ANIM) {
            Slog.v(TAG, "finishExit in " + this + ": exiting=" + this.mWin.mAnimatingExit + " remove=" + this.mWin.mRemoveOnExit + " windowAnimating=" + isWindowAnimationSet());
        }
        if (!this.mWin.mChildWindows.isEmpty()) {
            WindowList childWindows = new WindowList(this.mWin.mChildWindows);
            for (int i = childWindows.size() - 1; i >= 0; i--) {
                ((WindowState) childWindows.get(i)).mWinAnimator.finishExit();
            }
        }
        if (this.mEnteringAnimation) {
            this.mEnteringAnimation = false;
            this.mService.requestTraversal();
            if (this.mWin.mAppToken == null) {
                try {
                    this.mWin.mClient.dispatchWindowShown();
                } catch (RemoteException e) {
                }
            }
        }
        if (!(isWindowAnimationSet() || this.mService.mAccessibilityController == null || this.mWin.getDisplayId() != 0)) {
            this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
        }
        if (this.mWin.mAnimatingExit && !isWindowAnimationSet()) {
            if (WindowManagerService.localLOGV || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                Slog.v(TAG, "Exit animation finished in " + this + ": remove=" + this.mWin.mRemoveOnExit);
            }
            this.mWin.mDestroying = true;
            boolean hasSurface = hasSurface();
            if (hasSurface) {
                hide("finishExit");
            }
            if (this.mWin.mAppToken != null) {
                this.mWin.mAppToken.destroySurfaces();
            } else {
                if (hasSurface) {
                    this.mService.mDestroySurface.add(this.mWin);
                }
                if (this.mWin.mRemoveOnExit) {
                    this.mService.mPendingRemove.add(this.mWin);
                    this.mWin.mRemoveOnExit = false;
                }
            }
            this.mWin.mAnimatingExit = false;
            this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
        }
    }

    void hide(String reason) {
        if (!this.mLastHidden) {
            this.mLastHidden = true;
            if (this.mSurfaceController != null) {
                this.mService.updateNonSystemOverlayWindowsVisibilityIfNeeded(this.mWin, false);
                this.mSurfaceController.hideInTransaction(reason);
            }
        }
    }

    boolean finishDrawingLocked() {
        boolean startingWindow = this.mWin.mAttrs.type == 3;
        if (DEBUG_STARTING_WINDOW && startingWindow) {
            Slog.v(TAG, "Finishing drawing window " + this.mWin + ": mDrawState=" + drawStateToString());
        }
        boolean layoutNeeded = this.mWin.clearAnimatingWithSavedSurface();
        if (this.mDrawState != 1) {
            return layoutNeeded;
        }
        if (DEBUG_SURFACE_TRACE || DEBUG_ANIM || SHOW_TRANSACTIONS || DEBUG_ORIENTATION || !WindowManagerService.IS_USER_BUILD || WindowManagerService.DEBUG_WMS) {
            Slog.v(TAG, "finishDrawingLocked: mDrawState=COMMIT_DRAW_PENDING " + this.mWin + " in " + this.mSurfaceController);
        }
        if (DEBUG_STARTING_WINDOW && startingWindow) {
            Slog.v(TAG, "Draw state now committed in " + this.mWin);
        }
        this.mDrawState = 2;
        return true;
    }

    boolean commitFinishDrawingLocked() {
        if (DEBUG_STARTING_WINDOW && this.mWin.mAttrs.type == 3) {
            Slog.i(TAG, "commitFinishDrawingLocked: " + this.mWin + " cur mDrawState=" + drawStateToString());
        }
        if (this.mDrawState != 2 && this.mDrawState != 3) {
            return false;
        }
        if (DEBUG_SURFACE_TRACE || DEBUG_ANIM) {
            Slog.i(TAG, "commitFinishDrawingLocked: mDrawState=READY_TO_SHOW " + this.mSurfaceController);
        }
        this.mDrawState = 3;
        boolean result = false;
        AppWindowToken atoken = this.mWin.mAppToken;
        if (atoken == null || atoken.allDrawn || this.mWin.mAttrs.type == 3) {
            result = performShowLocked();
        }
        return result;
    }

    void preserveSurfaceLocked() {
        if (this.mDestroyPreservedSurfaceUponRedraw) {
            this.mSurfaceDestroyDeferred = false;
            destroySurfaceLocked();
            this.mSurfaceDestroyDeferred = true;
            return;
        }
        if (SHOW_TRANSACTIONS) {
            WindowManagerService.logSurface(this.mWin, "SET FREEZE LAYER", false);
        }
        if (this.mSurfaceController != null) {
            this.mSurfaceController.setLayer(this.mAnimLayer + 1);
        }
        this.mDestroyPreservedSurfaceUponRedraw = true;
        this.mSurfaceDestroyDeferred = true;
        destroySurfaceLocked();
    }

    void destroyPreservedSurfaceLocked() {
        if (this.mDestroyPreservedSurfaceUponRedraw) {
            destroyDeferredSurfaceLocked();
            this.mDestroyPreservedSurfaceUponRedraw = false;
        }
    }

    void destroyPreservedSurfaceLocked(boolean cleanupOnResume) {
        if (this.mWin == null || this.mWin.mDragResizing || !this.mDestroyPreservedSurfaceUponRedraw || !cleanupOnResume || this.mSurfaceController == null || this.mSurfaceController.getShown()) {
            destroyPreservedSurfaceLocked();
        }
    }

    void markPreservedSurfaceForDestroy() {
        if (this.mDestroyPreservedSurfaceUponRedraw && !this.mService.mDestroyPreservedSurface.contains(this.mWin)) {
            this.mService.mDestroyPreservedSurface.add(this.mWin);
        }
    }

    WindowSurfaceController createSurfaceLocked() {
        WindowState w = this.mWin;
        if (w.hasSavedSurface()) {
            if (DEBUG_ANIM) {
                Slog.i(TAG, "createSurface: " + this + ": called when we had a saved surface");
            }
            w.restoreSavedSurface();
            return this.mSurfaceController;
        } else if (this.mSurfaceController != null) {
            return this.mSurfaceController;
        } else {
            w.setHasSurface(false);
            if (DEBUG_ANIM || DEBUG_ORIENTATION) {
                Slog.i(TAG, "createSurface " + this + ": mDrawState=DRAW_PENDING");
            }
            this.mDrawState = 1;
            if (w.mAppToken != null) {
                if (w.mAppToken.mAppAnimator.animation == null) {
                    w.mAppToken.clearAllDrawn();
                } else {
                    w.mAppToken.deferClearAllDrawn = true;
                }
            }
            this.mService.makeWindowFreezingScreenIfNeededLocked(w);
            int flags = 4;
            LayoutParams attrs = w.mAttrs;
            if (this.mService.isSecureLocked(w)) {
                flags = 132;
            }
            this.mTmpSize.set(w.mFrame.left + w.mXOffset, w.mFrame.top + w.mYOffset, 0, 0);
            calculateSurfaceBounds(w, attrs);
            int width = this.mTmpSize.width();
            int height = this.mTmpSize.height();
            if (this.mService.isFastStartingWindowSupport() && w.isFastStartingWindow()) {
                if (DEBUG_VISIBILITY) {
                    Slog.v(TAG, "[StartingWindow] window " + this);
                }
                if (width == 1 && height == 1) {
                    DisplayContent displayContent = w.getDisplayContent();
                    if (displayContent != null) {
                        DisplayInfo displayInfo = displayContent.getDisplayInfo();
                        width = displayInfo.logicalWidth;
                        height = displayInfo.logicalHeight;
                        if (DEBUG_VISIBILITY) {
                            Slog.v(TAG, "[StartingWindow] apply logic width height");
                        }
                    }
                }
            }
            if (DEBUG_VISIBILITY) {
                Slog.v(TAG, "Creating surface in session " + this.mSession.mSurfaceSession + " window " + this + " w=" + width + " h=" + height + " x=" + this.mTmpSize.left + " y=" + this.mTmpSize.top + " format=" + attrs.format + " flags=" + flags);
            }
            this.mLastSystemDecorRect.set(0, 0, 0, 0);
            this.mHasClipRect = false;
            this.mClipRect.set(0, 0, 0, 0);
            this.mLastClipRect.set(0, 0, 0, 0);
            try {
                int format = (attrs.flags & 16777216) != 0 ? -3 : attrs.format;
                if (!PixelFormat.formatHasAlpha(attrs.format) && attrs.surfaceInsets.left == 0 && attrs.surfaceInsets.top == 0 && attrs.surfaceInsets.right == 0 && attrs.surfaceInsets.bottom == 0 && !w.isDragResizing()) {
                    flags |= 1024;
                }
                this.mSurfaceController = new WindowSurfaceController(this.mSession.mSurfaceSession, attrs.getTitle().toString(), width, height, format, flags, this);
                w.setHasSurface(true);
                if (SHOW_TRANSACTIONS || SHOW_SURFACE_ALLOC || !WindowManagerService.IS_USER_BUILD) {
                    Slog.i(TAG, "  CREATE SURFACE " + this.mSurfaceController + " IN SESSION " + this.mSession.mSurfaceSession + ": pid=" + this.mSession.mPid + " format=" + attrs.format + " flags=0x" + Integer.toHexString(flags) + " / " + this);
                }
                if (WindowManagerService.localLOGV) {
                    Slog.v(TAG, "Got surface: " + this.mSurfaceController + ", set left=" + w.mFrame.left + " top=" + w.mFrame.top + ", animLayer=" + this.mAnimLayer);
                }
                if (SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i(TAG, ">>> OPEN TRANSACTION createSurfaceLocked");
                    WindowManagerService.logSurface(w, "CREATE pos=(" + w.mFrame.left + "," + w.mFrame.top + ") (" + width + "x" + height + "), layer=" + this.mAnimLayer + " HIDE", false);
                }
                this.mSurfaceController.setPositionAndLayer((float) this.mTmpSize.left, (float) this.mTmpSize.top, w.getDisplayContent().getDisplay().getLayerStack(), this.mAnimLayer);
                this.mLastHidden = true;
                if (WindowManagerService.localLOGV) {
                    Slog.v(TAG, "Created surface " + this);
                }
                if (this.mService.isFastStartingWindowSupport() && w.isFastStartingWindow()) {
                    new AsyncTask<Void, Void, Void>() {
                        protected Void doInBackground(Void... para) {
                            try {
                                WindowStateAnimator.this.drawIfNeeded();
                            } catch (Exception e) {
                                Slog.e(WindowStateAnimator.TAG, "FSW Exception: " + e);
                            }
                            return null;
                        }
                    }.execute(new Void[0]);
                }
                return this.mSurfaceController;
            } catch (OutOfResourcesException e) {
                Slog.w(TAG, "OutOfResourcesException creating surface");
                this.mService.reclaimSomeSurfaceMemoryLocked(this, "create", true);
                this.mDrawState = 0;
                return null;
            } catch (Exception e2) {
                Slog.e(TAG, "Exception creating surface", e2);
                this.mDrawState = 0;
                return null;
            }
        }
    }

    private void calculateSurfaceBounds(WindowState w, LayoutParams attrs) {
        if ((attrs.flags & 16384) != 0) {
            this.mTmpSize.right = this.mTmpSize.left + w.mRequestedWidth;
            this.mTmpSize.bottom = this.mTmpSize.top + w.mRequestedHeight;
        } else if (w.isDragResizing()) {
            if (w.getResizeMode() == 0) {
                this.mTmpSize.left = 0;
                this.mTmpSize.top = 0;
            }
            DisplayInfo displayInfo = w.getDisplayInfo();
            this.mTmpSize.right = this.mTmpSize.left + displayInfo.logicalWidth;
            this.mTmpSize.bottom = this.mTmpSize.top + displayInfo.logicalHeight;
        } else {
            this.mTmpSize.right = this.mTmpSize.left + w.mCompatFrame.width();
            this.mTmpSize.bottom = this.mTmpSize.top + w.mCompatFrame.height();
        }
        if (this.mTmpSize.width() < 1) {
            this.mTmpSize.right = this.mTmpSize.left + 1;
        }
        if (this.mTmpSize.height() < 1) {
            this.mTmpSize.bottom = this.mTmpSize.top + 1;
        }
        Rect rect = this.mTmpSize;
        rect.left -= attrs.surfaceInsets.left;
        rect = this.mTmpSize;
        rect.top -= attrs.surfaceInsets.top;
        rect = this.mTmpSize;
        rect.right += attrs.surfaceInsets.right;
        rect = this.mTmpSize;
        rect.bottom += attrs.surfaceInsets.bottom;
    }

    boolean hasSurface() {
        if (this.mWin.hasSavedSurface() || this.mSurfaceController == null) {
            return false;
        }
        return this.mSurfaceController.hasSurface();
    }

    void destroySurfaceLocked() {
        AppWindowToken wtoken = this.mWin.mAppToken;
        if (wtoken != null && this.mWin == wtoken.startingWindow) {
            wtoken.startingDisplayed = false;
        }
        this.mWin.clearHasSavedSurface();
        if (this.mSurfaceController != null) {
            int i = this.mWin.mChildWindows.size();
            while (!this.mDestroyPreservedSurfaceUponRedraw && i > 0) {
                i--;
                ((WindowState) this.mWin.mChildWindows.get(i)).mAttachedHidden = true;
            }
            try {
                if (DEBUG_VISIBILITY) {
                    WindowManagerService.logWithStack(TAG, "Window " + this + " destroying surface " + this.mSurfaceController + ", session " + this.mSession);
                }
                if (this.mSurfaceDestroyDeferred) {
                    if (!(this.mSurfaceController == null || this.mPendingDestroySurface == this.mSurfaceController)) {
                        if (this.mPendingDestroySurface != null) {
                            if (SHOW_TRANSACTIONS || SHOW_SURFACE_ALLOC) {
                                WindowManagerService.logSurface(this.mWin, "DESTROY PENDING", true);
                            }
                            this.mPendingDestroySurface.destroyInTransaction();
                        }
                        this.mPendingDestroySurface = this.mSurfaceController;
                    }
                    if (this.mDestroyDeferredFlag && this.mWin.mAppToken != null && this.mWin.mAppToken.clientHidden) {
                        if (SHOW_TRANSACTIONS) {
                            Slog.i(TAG, "HideDeferSurface mWin:" + this.mWin);
                        }
                        hide("HideDeferSurface");
                    }
                } else {
                    if (SHOW_TRANSACTIONS || SHOW_SURFACE_ALLOC) {
                        WindowManagerService.logSurface(this.mWin, "DESTROY", true);
                    }
                    destroySurface();
                    if (!(this.mPendingDestroySurface == null || wtoken == null || ((!wtoken.mAppStopped && !wtoken.mSplitStoped) || !wtoken.hiddenRequested))) {
                        destroyPreservedSurfaceLocked();
                        Slog.v(TAG, "mPendingDestroySurface: " + this.mPendingDestroySurface + "==" + this);
                    }
                }
                if (!this.mDestroyPreservedSurfaceUponRedraw) {
                    this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
                }
            } catch (RuntimeException e) {
                Slog.w(TAG, "Exception thrown when destroying Window " + this + " surface " + this.mSurfaceController + " session " + this.mSession + ": " + e.toString());
            }
            this.mWin.setHasSurface(false);
            if (this.mSurfaceController != null) {
                this.mSurfaceController.setShown(false);
            }
            this.mSurfaceController = null;
            this.mDrawState = 0;
        }
    }

    void destroyDeferredSurfaceLocked() {
        try {
            if (this.mPendingDestroySurface != null) {
                if (SHOW_TRANSACTIONS || SHOW_SURFACE_ALLOC) {
                    WindowManagerService.logSurface(this.mWin, "DESTROY PENDING", true);
                }
                this.mPendingDestroySurface.destroyInTransaction();
                if (!this.mDestroyPreservedSurfaceUponRedraw) {
                    this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
                }
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Exception thrown when destroying Window " + this + " surface " + this.mPendingDestroySurface + " session " + this.mSession + ": " + e.toString());
        }
        this.mSurfaceDestroyDeferred = false;
        this.mPendingDestroySurface = null;
    }

    void applyMagnificationSpec(MagnificationSpec spec, Matrix transform) {
        int surfaceInsetLeft = this.mWin.mAttrs.surfaceInsets.left;
        int surfaceInsetTop = this.mWin.mAttrs.surfaceInsets.top;
        if (spec != null && !spec.isNop()) {
            float scale = spec.scale;
            transform.postScale(scale, scale);
            transform.postTranslate(spec.offsetX, spec.offsetY);
            transform.postTranslate(-((((float) surfaceInsetLeft) * scale) - ((float) surfaceInsetLeft)), -((((float) surfaceInsetTop) * scale) - ((float) surfaceInsetTop)));
        }
    }

    void computeShownFrameLocked() {
        boolean selfTransformation = this.mHasLocalTransformation;
        Transformation attachedTransformation = (this.mAttachedWinAnimator == null || !this.mAttachedWinAnimator.mHasLocalTransformation) ? null : this.mAttachedWinAnimator.mTransformation;
        Transformation appTransformation = (this.mAppAnimator == null || !this.mAppAnimator.hasTransformation) ? null : this.mAppAnimator.transformation;
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        if (this.mIsWallpaper && wallpaperTarget != null && this.mService.mAnimateWallpaperWithTarget) {
            WindowStateAnimator wallpaperAnimator = wallpaperTarget.mWinAnimator;
            if (!(!wallpaperAnimator.mHasLocalTransformation || wallpaperAnimator.mAnimation == null || wallpaperAnimator.mAnimation.getDetachWallpaper())) {
                attachedTransformation = wallpaperAnimator.mTransformation;
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER && attachedTransformation != null) {
                    Slog.v(TAG, "WP target attached xform: " + attachedTransformation);
                }
            }
            AppWindowAnimator wpAppAnimator = wallpaperTarget.mAppToken == null ? null : wallpaperTarget.mAppToken.mAppAnimator;
            if (!(wpAppAnimator == null || !wpAppAnimator.hasTransformation || wpAppAnimator.animation == null || wpAppAnimator.animation.getDetachWallpaper())) {
                appTransformation = wpAppAnimator.transformation;
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER && appTransformation != null) {
                    Slog.v(TAG, "WP target app xform: " + appTransformation);
                }
            }
        }
        int displayId = this.mWin.getDisplayId();
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
        boolean screenAnimation = screenRotationAnimation != null ? screenRotationAnimation.isAnimating() : false;
        this.mHasClipRect = false;
        Rect frame;
        float[] tmpFloats;
        Matrix tmpMatrix;
        if (selfTransformation || attachedTransformation != null || appTransformation != null || screenAnimation) {
            frame = this.mWin.mFrame;
            tmpFloats = this.mService.mTmpFloats;
            tmpMatrix = this.mWin.mTmpMatrix;
            if (screenAnimation && screenRotationAnimation.isRotating()) {
                float w = (float) frame.width();
                float h = (float) frame.height();
                if (w < 1.0f || h < 1.0f) {
                    tmpMatrix.reset();
                } else {
                    tmpMatrix.setScale((2.0f / w) + 1.0f, (2.0f / h) + 1.0f, w / 2.0f, h / 2.0f);
                }
            } else {
                tmpMatrix.reset();
            }
            tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
            if (selfTransformation) {
                tmpMatrix.postConcat(this.mTransformation.getMatrix());
            }
            tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
            if (attachedTransformation != null) {
                tmpMatrix.postConcat(attachedTransformation.getMatrix());
            }
            if (appTransformation != null) {
                tmpMatrix.postConcat(appTransformation.getMatrix());
            }
            if (screenAnimation) {
                tmpMatrix.postConcat(screenRotationAnimation.getEnterTransformation().getMatrix());
            }
            if (this.mService.mAccessibilityController != null && displayId == 0) {
                applyMagnificationSpec(this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin), tmpMatrix);
            }
            this.mHaveMatrix = true;
            tmpMatrix.getValues(tmpFloats);
            this.mDsDx = tmpFloats[0];
            this.mDtDx = tmpFloats[3];
            this.mDsDy = tmpFloats[1];
            this.mDtDy = tmpFloats[4];
            float x = tmpFloats[2];
            float y = tmpFloats[5];
            this.mWin.mShownPosition.set((int) x, (int) y);
            this.mShownAlpha = this.mAlpha;
            if (!(this.mService.mLimitedAlphaCompositing && PixelFormat.formatHasAlpha(this.mWin.mAttrs.format) && (!this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDsDy, this.mDtDy) || x != ((float) frame.left) || y != ((float) frame.top)))) {
                if (selfTransformation) {
                    this.mShownAlpha *= this.mTransformation.getAlpha();
                }
                if (attachedTransformation != null) {
                    this.mShownAlpha *= attachedTransformation.getAlpha();
                }
                if (appTransformation != null) {
                    this.mShownAlpha *= appTransformation.getAlpha();
                    if (appTransformation.hasClipRect()) {
                        this.mClipRect.set(appTransformation.getClipRect());
                        this.mHasClipRect = true;
                        if (this.mWin.layoutInParentFrame()) {
                            this.mClipRect.offset(this.mWin.mContainingFrame.left - this.mWin.mFrame.left, this.mWin.mContainingFrame.top - this.mWin.mFrame.top);
                        }
                    }
                }
                if (screenAnimation) {
                    this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
                }
            }
            if ((DEBUG_SURFACE_TRACE || WindowManagerService.localLOGV) && (((double) this.mShownAlpha) == 1.0d || ((double) this.mShownAlpha) == 0.0d)) {
                Float valueOf;
                String str = TAG;
                StringBuilder append = new StringBuilder().append("computeShownFrameLocked: Animating ").append(this).append(" mAlpha=").append(this.mAlpha).append(" self=").append(selfTransformation ? Float.valueOf(this.mTransformation.getAlpha()) : "null").append(" attached=").append(attachedTransformation == null ? "null" : Float.valueOf(attachedTransformation.getAlpha())).append(" app=").append(appTransformation == null ? "null" : Float.valueOf(appTransformation.getAlpha())).append(" screen=");
                if (screenAnimation) {
                    valueOf = Float.valueOf(screenRotationAnimation.getEnterTransformation().getAlpha());
                } else {
                    valueOf = "null";
                }
                Slog.v(str, append.append(valueOf).toString());
            }
        } else if ((!this.mIsWallpaper || !this.mService.mWindowPlacerLocked.mWallpaperActionPending) && !this.mWin.isDragResizeChanged()) {
            if (WindowManagerService.localLOGV) {
                Slog.v(TAG, "computeShownFrameLocked: " + this + " not attached, mAlpha=" + this.mAlpha);
            }
            MagnificationSpec spec = null;
            if (this.mService.mAccessibilityController != null && displayId == 0) {
                spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
            }
            if (spec != null) {
                frame = this.mWin.mFrame;
                tmpFloats = this.mService.mTmpFloats;
                tmpMatrix = this.mWin.mTmpMatrix;
                tmpMatrix.setScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                applyMagnificationSpec(spec, tmpMatrix);
                tmpMatrix.getValues(tmpFloats);
                this.mHaveMatrix = true;
                this.mDsDx = tmpFloats[0];
                this.mDtDx = tmpFloats[3];
                this.mDsDy = tmpFloats[1];
                this.mDtDy = tmpFloats[4];
                this.mWin.mShownPosition.set((int) tmpFloats[2], (int) tmpFloats[5]);
                this.mShownAlpha = this.mAlpha;
            } else {
                this.mWin.mShownPosition.set(this.mWin.mFrame.left, this.mWin.mFrame.top);
                if (!(this.mWin.mXOffset == 0 && this.mWin.mYOffset == 0)) {
                    this.mWin.mShownPosition.offset(this.mWin.mXOffset, this.mWin.mYOffset);
                }
                this.mShownAlpha = this.mAlpha;
                this.mHaveMatrix = false;
                this.mDsDx = this.mWin.mGlobalScale;
                this.mDtDx = OppoBrightUtils.MIN_LUX_LIMITI;
                this.mDsDy = OppoBrightUtils.MIN_LUX_LIMITI;
                this.mDtDy = this.mWin.mGlobalScale;
            }
        }
    }

    private void calculateSystemDecorRect() {
        boolean cropToDecor = false;
        WindowState w = this.mWin;
        Rect decorRect = w.mDecorFrame;
        int width = w.mFrame.width();
        int height = w.mFrame.height();
        int left = w.mXOffset + w.mFrame.left;
        int top = w.mYOffset + w.mFrame.top;
        if (w.isDockedResizing() || (w.isChildWindow() && w.mAttachedWindow.isDockedResizing())) {
            DisplayInfo displayInfo = w.getDisplayContent().getDisplayInfo();
            this.mSystemDecorRect.set(0, 0, Math.max(width, displayInfo.logicalWidth), Math.max(height, displayInfo.logicalHeight));
        } else {
            this.mSystemDecorRect.set(0, 0, width, height);
        }
        if (!(w.inFreeformWorkspace() && w.isAnimatingLw())) {
            cropToDecor = true;
        }
        if (cropToDecor) {
            this.mSystemDecorRect.intersect(decorRect.left - left, decorRect.top - top, decorRect.right - left, decorRect.bottom - top);
        }
        if (w.mEnforceSizeCompat && w.mInvGlobalScale != 1.0f) {
            float scale = w.mInvGlobalScale;
            this.mSystemDecorRect.left = (int) ((((float) this.mSystemDecorRect.left) * scale) - 0.5f);
            this.mSystemDecorRect.top = (int) ((((float) this.mSystemDecorRect.top) * scale) - 0.5f);
            this.mSystemDecorRect.right = (int) ((((float) (this.mSystemDecorRect.right + 1)) * scale) - 0.5f);
            this.mSystemDecorRect.bottom = (int) ((((float) (this.mSystemDecorRect.bottom + 1)) * scale) - 0.5f);
        }
    }

    void calculateSurfaceWindowCrop(Rect clipRect, Rect finalClipRect) {
        WindowState w = this.mWin;
        DisplayContent displayContent = w.getDisplayContent();
        if (displayContent == null) {
            clipRect.setEmpty();
            finalClipRect.setEmpty();
            return;
        }
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d(TAG, "Updating crop win=" + w + " mLastCrop=" + this.mLastClipRect);
        }
        if (!w.isDefaultDisplay()) {
            this.mSystemDecorRect.set(0, 0, w.mCompatFrame.width(), w.mCompatFrame.height());
            this.mSystemDecorRect.intersect(-w.mCompatFrame.left, -w.mCompatFrame.top, displayInfo.logicalWidth - w.mCompatFrame.left, displayInfo.logicalHeight - w.mCompatFrame.top);
        } else if (w.mLayer >= this.mService.mSystemDecorLayer) {
            this.mSystemDecorRect.set(0, 0, w.mCompatFrame.width(), w.mCompatFrame.height());
        } else if (w.mDecorFrame.isEmpty()) {
            this.mSystemDecorRect.set(0, 0, w.mCompatFrame.width(), w.mCompatFrame.height());
        } else if (w.mAttrs.type == 2013 && this.mAnimator.isAnimating()) {
            this.mTmpClipRect.set(this.mSystemDecorRect);
            calculateSystemDecorRect();
            this.mSystemDecorRect.union(this.mTmpClipRect);
        } else {
            calculateSystemDecorRect();
            if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
                Slog.d(TAG, "Applying decor to crop win=" + w + " mDecorFrame=" + w.mDecorFrame + " mSystemDecorRect=" + this.mSystemDecorRect);
            }
        }
        boolean fullscreen = w.isFrameFullscreen(displayInfo);
        boolean isFreeformResizing = w.isDragResizing() && w.getResizeMode() == 0;
        Rect rect = (!this.mHasClipRect || fullscreen) ? this.mSystemDecorRect : this.mClipRect;
        clipRect.set(rect);
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d(TAG, "win=" + w + " Initial clip rect: " + clipRect + " mHasClipRect=" + this.mHasClipRect + " fullscreen=" + fullscreen);
        }
        if (isFreeformResizing && !w.isChildWindow()) {
            clipRect.offset(w.mShownPosition.x, w.mShownPosition.y);
        }
        LayoutParams attrs = w.mAttrs;
        clipRect.left -= attrs.surfaceInsets.left;
        clipRect.top -= attrs.surfaceInsets.top;
        clipRect.right += attrs.surfaceInsets.right;
        clipRect.bottom += attrs.surfaceInsets.bottom;
        if (this.mHasClipRect && fullscreen) {
            clipRect.intersect(this.mClipRect);
        }
        clipRect.offset(attrs.surfaceInsets.left, attrs.surfaceInsets.top);
        finalClipRect.setEmpty();
        adjustCropToStackBounds(w, clipRect, finalClipRect, isFreeformResizing);
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d(TAG, "win=" + w + " Clip rect after stack adjustment=" + clipRect);
        }
        w.transformClipRectFromScreenToSurfaceSpace(clipRect);
        if (w.hasJustMovedInStack() && this.mLastClipRect.isEmpty() && !clipRect.isEmpty()) {
            clipRect.setEmpty();
        }
    }

    void updateSurfaceWindowCrop(Rect clipRect, Rect finalClipRect, boolean recoveringMemory) {
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d(TAG, "updateSurfaceWindowCrop: win=" + this.mWin + " clipRect=" + clipRect + " finalClipRect=" + finalClipRect);
        }
        if (this.mWin.mHideKeyguard) {
            Slog.d(TAG, "setKeyguardHide");
            clipRect.set(0, 0, 1, 1);
        }
        if (clipRect == null) {
            this.mSurfaceController.clearCropInTransaction(recoveringMemory);
        } else if (!clipRect.equals(this.mLastClipRect)) {
            this.mLastClipRect.set(clipRect);
            this.mSurfaceController.setCropInTransaction(clipRect, recoveringMemory);
        }
        if (!finalClipRect.equals(this.mLastFinalClipRect)) {
            this.mLastFinalClipRect.set(finalClipRect);
            this.mSurfaceController.setFinalCropInTransaction(finalClipRect);
            if (this.mDestroyPreservedSurfaceUponRedraw && this.mPendingDestroySurface != null) {
                this.mPendingDestroySurface.setFinalCropInTransaction(finalClipRect);
            }
        }
    }

    private int resolveStackClip() {
        if (this.mAppAnimator == null || this.mAppAnimator.animation == null) {
            return this.mStackClip;
        }
        return this.mAppAnimator.getStackClip();
    }

    private void adjustCropToStackBounds(WindowState w, Rect clipRect, Rect finalClipRect, boolean isFreeformResizing) {
        DisplayContent displayContent = w.getDisplayContent();
        if (displayContent == null || displayContent.isDefaultDisplay) {
            Task task = w.getTask();
            if (task != null && task.cropWindowsToStackBounds()) {
                int stackClip = resolveStackClip();
                if (!isAnimationSet() || stackClip != 2) {
                    if (w != ((WindowState) this.mPolicy.getWinShowWhenLockedLw()) || !this.mPolicy.isKeyguardShowingOrOccluded()) {
                        int frameX;
                        int frameY;
                        boolean useFinalClipRect;
                        TaskStack stack = task.mStack;
                        stack.getDimBounds(this.mTmpStackBounds);
                        Rect surfaceInsets = w.getAttrs().surfaceInsets;
                        if (isFreeformResizing) {
                            frameX = (int) this.mSurfaceController.getX();
                        } else {
                            frameX = (w.mFrame.left + this.mWin.mXOffset) - surfaceInsets.left;
                        }
                        if (isFreeformResizing) {
                            frameY = (int) this.mSurfaceController.getY();
                        } else {
                            frameY = (w.mFrame.top + this.mWin.mYOffset) - surfaceInsets.top;
                        }
                        if (isAnimationSet() && stackClip == 0) {
                            useFinalClipRect = true;
                        } else {
                            useFinalClipRect = this.mDestroyPreservedSurfaceUponRedraw;
                        }
                        if (useFinalClipRect) {
                            finalClipRect.set(this.mTmpStackBounds);
                        } else {
                            if (StackId.hasWindowShadow(stack.mStackId) && !StackId.isTaskResizeAllowed(stack.mStackId)) {
                                this.mTmpStackBounds.inset(-surfaceInsets.left, -surfaceInsets.top, -surfaceInsets.right, -surfaceInsets.bottom);
                            }
                            clipRect.left = Math.max(0, Math.max(this.mTmpStackBounds.left, clipRect.left + frameX) - frameX);
                            clipRect.top = Math.max(0, Math.max(this.mTmpStackBounds.top, clipRect.top + frameY) - frameY);
                            clipRect.right = Math.max(0, Math.min(this.mTmpStackBounds.right, clipRect.right + frameX) - frameX);
                            clipRect.bottom = Math.max(0, Math.min(this.mTmpStackBounds.bottom, clipRect.bottom + frameY) - frameY);
                        }
                    }
                }
            }
        }
    }

    void setSurfaceBoundariesLocked(boolean recoveringMemory) {
        WindowState w = this.mWin;
        Task task = w.getTask();
        if (!w.isResizedWhileNotDragResizing() || w.isGoneForLayoutLw()) {
            this.mTmpSize.set(w.mShownPosition.x, w.mShownPosition.y, 0, 0);
            calculateSurfaceBounds(w, w.getAttrs());
            this.mExtraHScale = 1.0f;
            this.mExtraVScale = 1.0f;
            boolean wasResized = this.mSurfaceResized;
            boolean wasForceScaled = this.mForceScaleUntilResize;
            boolean wasSeamlesslyRotated = w.mSeamlesslyRotated;
            if (!w.mRelayoutCalled || w.mInRelayout) {
                this.mSurfaceResized = this.mSurfaceController.setSizeInTransaction(this.mTmpSize.width(), this.mTmpSize.height(), recoveringMemory);
            } else {
                this.mSurfaceResized = false;
            }
            boolean z = this.mForceScaleUntilResize && !this.mSurfaceResized;
            this.mForceScaleUntilResize = z;
            WindowManagerService windowManagerService = this.mService;
            z = w.mSeamlesslyRotated && !this.mSurfaceResized;
            windowManagerService.markForSeamlessRotation(w, z);
            calculateSurfaceWindowCrop(this.mTmpClipRect, this.mTmpFinalClipRect);
            float surfaceWidth = this.mSurfaceController.getWidth();
            float surfaceHeight = this.mSurfaceController.getHeight();
            if ((task == null || !task.mStack.getForceScaleToCrop()) && !this.mForceScaleUntilResize) {
                if (!w.mSeamlesslyRotated) {
                    this.mSurfaceController.setPositionInTransaction((float) this.mTmpSize.left, (float) this.mTmpSize.top, recoveringMemory);
                }
                if (w.mIsWallpaper && wasResized != this.mSurfaceResized) {
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                        Slog.d(TAG, w + " forceScaleableInTransaction " + this.mSurfaceResized);
                    }
                    this.mSurfaceController.forceScaleableInTransaction(this.mSurfaceResized);
                }
            } else {
                int hInsets = w.getAttrs().surfaceInsets.left + w.getAttrs().surfaceInsets.right;
                int vInsets = w.getAttrs().surfaceInsets.top + w.getAttrs().surfaceInsets.bottom;
                if (!this.mForceScaleUntilResize) {
                    this.mSurfaceController.forceScaleableInTransaction(true);
                }
                this.mExtraHScale = ((float) (this.mTmpClipRect.width() - hInsets)) / (surfaceWidth - ((float) hInsets));
                this.mExtraVScale = ((float) (this.mTmpClipRect.height() - vInsets)) / (surfaceHeight - ((float) vInsets));
                this.mSurfaceController.setPositionInTransaction((float) Math.floor((double) ((int) (((float) ((int) (((float) this.mTmpSize.left) - (((float) w.mAttrs.x) * (1.0f - this.mExtraHScale))))) + (((float) w.getAttrs().surfaceInsets.left) * (1.0f - this.mExtraHScale))))), (float) Math.floor((double) ((int) (((float) ((int) (((float) this.mTmpSize.top) - (((float) w.mAttrs.y) * (1.0f - this.mExtraVScale))))) + (((float) w.getAttrs().surfaceInsets.top) * (1.0f - this.mExtraVScale))))), recoveringMemory);
                this.mTmpClipRect.set(0, 0, (int) surfaceWidth, (int) surfaceHeight);
                this.mTmpFinalClipRect.setEmpty();
                this.mForceScaleUntilResize = true;
            }
            if ((wasForceScaled && !this.mForceScaleUntilResize) || (wasSeamlesslyRotated && !w.mSeamlesslyRotated)) {
                this.mSurfaceController.setGeometryAppliesWithResizeInTransaction(true);
                this.mSurfaceController.forceScaleableInTransaction(false);
            }
            Rect clipRect = this.mTmpClipRect;
            if (w.inPinnedWorkspace()) {
                clipRect = null;
                task.mStack.getDimBounds(this.mTmpFinalClipRect);
                this.mTmpFinalClipRect.inset(-w.mAttrs.surfaceInsets.left, -w.mAttrs.surfaceInsets.top, -w.mAttrs.surfaceInsets.right, -w.mAttrs.surfaceInsets.bottom);
            }
            if (!w.mSeamlesslyRotated) {
                updateSurfaceWindowCrop(clipRect, this.mTmpFinalClipRect, recoveringMemory);
                this.mSurfaceController.setMatrixInTransaction((this.mDsDx * w.mHScale) * this.mExtraHScale, (this.mDtDx * w.mVScale) * this.mExtraVScale, (this.mDsDy * w.mHScale) * this.mExtraHScale, (this.mDtDy * w.mVScale) * this.mExtraVScale, recoveringMemory);
            }
            if (this.mSurfaceResized) {
                this.mReportSurfaceResized = true;
                this.mAnimator.setPendingLayoutChanges(w.getDisplayId(), 4);
                w.applyDimLayerIfNeeded();
            }
        }
    }

    void prepareSurfaceLocked(boolean recoveringMemory) {
        WindowState w = this.mWin;
        if (!hasSurface()) {
            if (w.mOrientationChanging) {
                if (DEBUG_ORIENTATION) {
                    Slog.v(TAG, "Orientation change skips hidden " + w);
                }
                w.mOrientationChanging = false;
            }
        } else if (!isWaitingForOpening()) {
            boolean isLayoutAttrChange;
            boolean displayed = false;
            computeShownFrameLocked();
            setSurfaceBoundariesLocked(recoveringMemory);
            if (WindowManagerDebugConfig.DEBUG_POWER) {
                Slog.d(TAG, "prepareSurfaceLocked, attachedHidden:" + w.mAttachedHidden + ", isOnScreen:" + w.isOnScreen() + ", mLastHidden:" + this.mLastHidden + ", turnScreenOn:" + this.mWin.mTurnOnScreen + ", for win:" + this.mWin);
                if (this.mWin.mTurnOnScreen) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("mLastLayer:").append(this.mLastLayer).append(", mAnimLayer:").append(this.mAnimLayer);
                    sb.append(", mLastAlpha:").append(this.mLastAlpha).append(", mShownAlpha:").append(this.mShownAlpha);
                    sb.append(", mLastDsDx:").append(this.mLastDsDx).append(", mDsDx:").append(this.mDsDx);
                    sb.append(", mLastDtDx:").append(this.mLastDtDx).append(", mDtDx:").append(this.mDtDx);
                    sb.append(", mLastDsDy:").append(this.mLastDsDy).append(", mDsDy:").append(this.mDsDy);
                    sb.append(", mLastDtDy:").append(this.mLastDtDy).append(", mDtDy:").append(this.mDtDy);
                    sb.append(", mLastHScale:").append(w.mLastHScale).append(", mHScale:").append(w.mHScale);
                    sb.append(", mLastVScale:").append(w.mLastVScale).append(", mVScale:").append(w.mVScale);
                    sb.append(", mSurfaceController:").append(this.mSurfaceController);
                    Slog.d(TAG, "prepareSurfaceLocked, win turnScreenOn:" + sb.toString());
                }
            }
            boolean isWinRequestTurnScreenOn = this.mWin.mTurnOnScreen;
            if (this.mLastLayer == this.mAnimLayer && this.mLastAlpha == this.mShownAlpha && this.mLastDsDx == this.mDsDx && this.mLastDtDx == this.mDtDx && this.mLastDsDy == this.mDsDy && this.mLastDtDy == this.mDtDy && w.mLastHScale == w.mHScale && w.mLastVScale == w.mVScale) {
                isLayoutAttrChange = this.mLastHidden;
            } else {
                isLayoutAttrChange = true;
            }
            boolean isStillNeedToWakeupWhenLayoutNotChange = false;
            if (!(this.mIsWallpaper || this.mService.mWinRequestTurnScreenOnWhenRelayout == null || this.mService.mWinRequestTurnScreenOnWhenRelayout != this.mWin)) {
                if (WindowManagerDebugConfig.DEBUG_POWER) {
                    Slog.d(TAG, "prepareSurfaceLocked for win who request turn on screen, win:" + this.mWin);
                }
                if (!(isLayoutAttrChange || !isWinRequestTurnScreenOn || this.mService.mTurnOnScreen)) {
                    isStillNeedToWakeupWhenLayoutNotChange = true;
                    if (WindowManagerDebugConfig.DEBUG_POWER) {
                        Slog.d(TAG, "prepareSurfaceLocked isStillNeedToWakeupWhenLayoutNotChange");
                    }
                }
            }
            if (this.mIsWallpaper && !this.mWin.mWallpaperVisible) {
                hide("prepareSurfaceLocked");
            } else if (w.mAttachedHidden || !w.isOnScreen() || w.mHideKeyguard) {
                hide("prepareSurfaceLocked");
                this.mWallpaperControllerLocked.hideWallpapers(w);
                if (w.mOrientationChanging) {
                    w.mOrientationChanging = false;
                    if (DEBUG_ORIENTATION) {
                        Slog.v(TAG, "Orientation change skips hidden " + w);
                    }
                }
            } else if (this.mLastLayer == this.mAnimLayer && this.mLastAlpha == this.mShownAlpha && this.mLastDsDx == this.mDsDx && this.mLastDtDx == this.mDtDx && this.mLastDsDy == this.mDsDy && this.mLastDtDy == this.mDtDy && w.mLastHScale == w.mHScale && w.mLastVScale == w.mVScale && !this.mLastHidden && !isStillNeedToWakeupWhenLayoutNotChange) {
                if (DEBUG_ANIM && isAnimationSet()) {
                    Slog.v(TAG, "prepareSurface: No changes in animation for " + this);
                }
                displayed = true;
            } else {
                displayed = true;
                this.mLastAlpha = this.mShownAlpha;
                this.mLastLayer = this.mAnimLayer;
                this.mLastDsDx = this.mDsDx;
                this.mLastDtDx = this.mDtDx;
                this.mLastDsDy = this.mDsDy;
                this.mLastDtDy = this.mDtDy;
                w.mLastHScale = w.mHScale;
                w.mLastVScale = w.mVScale;
                if (SHOW_TRANSACTIONS) {
                    WindowManagerService.logSurface(w, "controller=" + this.mSurfaceController + "alpha=" + this.mShownAlpha + " layer=" + this.mAnimLayer + " matrix=[" + this.mDsDx + "*" + w.mHScale + "," + this.mDtDx + "*" + w.mVScale + "][" + this.mDsDy + "*" + w.mHScale + "," + this.mDtDy + "*" + w.mVScale + "]", false);
                }
                if ((this.mSurfaceController.prepareToShowInTransaction(this.mShownAlpha, this.mAnimLayer, (this.mDsDx * w.mHScale) * this.mExtraHScale, (this.mDtDx * w.mVScale) * this.mExtraVScale, (this.mDsDy * w.mHScale) * this.mExtraHScale, (this.mDtDy * w.mVScale) * this.mExtraVScale, recoveringMemory) && this.mLastHidden && this.mDrawState == 4) || isStillNeedToWakeupWhenLayoutNotChange) {
                    if (showSurfaceRobustlyLocked()) {
                        markPreservedSurfaceForDestroy();
                        this.mAnimator.requestRemovalOfReplacedWindows(w);
                        this.mLastHidden = false;
                        if (this.mIsWallpaper) {
                            this.mWallpaperControllerLocked.dispatchWallpaperVisibility(w, true);
                        }
                        this.mAnimator.setPendingLayoutChanges(w.getDisplayId(), 8);
                    } else {
                        w.mOrientationChanging = false;
                    }
                }
                if (hasSurface()) {
                    w.mToken.hasVisible = true;
                }
                this.mService.mWinRequestTurnScreenOnWhenRelayout = null;
            }
            WindowAnimator windowAnimator = this.mAnimator;
            windowAnimator.mDisplayed |= displayed;
            if (displayed) {
                if (w.mOrientationChanging) {
                    if (w.isDrawnLw()) {
                        w.mOrientationChanging = false;
                        if (DEBUG_ORIENTATION) {
                            Slog.v(TAG, "Orientation change complete in " + w);
                        }
                    } else {
                        windowAnimator = this.mAnimator;
                        windowAnimator.mBulkUpdateParams &= -9;
                        this.mAnimator.mLastWindowFreezeSource = w;
                        if (DEBUG_ORIENTATION) {
                            Slog.v(TAG, "Orientation continue waiting for draw in " + w);
                        }
                    }
                }
                w.mToken.hasVisible = true;
            }
        }
    }

    void setTransparentRegionHintLocked(Region region) {
        if (this.mSurfaceController == null) {
            Slog.w(TAG, "setTransparentRegionHint: null mSurface after mHasSurface true");
        } else {
            this.mSurfaceController.setTransparentRegionHint(region);
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void setWallpaperOffset(Point shownPosition) {
        LayoutParams attrs = this.mWin.getAttrs();
        int left = shownPosition.x - attrs.surfaceInsets.left;
        int top = shownPosition.y - attrs.surfaceInsets.top;
        try {
            if (SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, ">>> OPEN TRANSACTION setWallpaperOffset");
            }
            SurfaceControl.openTransaction();
            this.mSurfaceController.setPositionInTransaction((float) (this.mWin.mFrame.left + left), (float) (this.mWin.mFrame.top + top), false);
            calculateSurfaceWindowCrop(this.mTmpClipRect, this.mTmpFinalClipRect);
            updateSurfaceWindowCrop(this.mTmpClipRect, this.mTmpFinalClipRect, false);
            SurfaceControl.closeTransaction();
            if (SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, "<<< CLOSE TRANSACTION setWallpaperOffset");
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error positioning surface of " + this.mWin + " pos=(" + left + "," + top + ")", e);
            SurfaceControl.closeTransaction();
            if (SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, "<<< CLOSE TRANSACTION setWallpaperOffset");
            }
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            if (SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, "<<< CLOSE TRANSACTION setWallpaperOffset");
            }
            throw th;
        }
    }

    boolean tryChangeFormatInPlaceLocked() {
        boolean z = false;
        if (this.mSurfaceController == null) {
            return false;
        }
        boolean isHwAccelerated;
        LayoutParams attrs = this.mWin.getAttrs();
        if ((attrs.flags & 16777216) != 0) {
            isHwAccelerated = true;
        } else {
            isHwAccelerated = false;
        }
        if ((isHwAccelerated ? -3 : attrs.format) != this.mSurfaceFormat) {
            return false;
        }
        if (!PixelFormat.formatHasAlpha(attrs.format)) {
            z = true;
        }
        setOpaqueLocked(z);
        return true;
    }

    void setOpaqueLocked(boolean isOpaque) {
        if (this.mSurfaceController != null) {
            this.mSurfaceController.setOpaque(isOpaque);
        }
    }

    void setSecureLocked(boolean isSecure) {
        if (this.mSurfaceController != null) {
            this.mSurfaceController.setSecure(isSecure);
        }
    }

    boolean performShowLocked() {
        if (this.mWin.isHiddenFromUserLocked()) {
            if (DEBUG_VISIBILITY) {
                Slog.w(TAG, "hiding " + this.mWin + ", belonging to " + this.mWin.mOwnerUid);
            }
            this.mWin.hideLw(false);
            return false;
        }
        String str;
        StringBuilder append;
        boolean z;
        if (DEBUG_VISIBILITY || (DEBUG_STARTING_WINDOW && this.mWin.mAttrs.type == 3)) {
            str = TAG;
            append = new StringBuilder().append("performShow on ").append(this).append(": mDrawState=").append(drawStateToString()).append(" readyForDisplay=").append(this.mWin.isReadyForDisplayIgnoringKeyguard()).append(" starting=").append(this.mWin.mAttrs.type == 3).append(" during animation: policyVis=").append(this.mWin.mPolicyVisibility).append(" attHidden=").append(this.mWin.mAttachedHidden).append(" tok.hiddenRequested=");
            if (this.mWin.mAppToken != null) {
                z = this.mWin.mAppToken.hiddenRequested;
            } else {
                z = false;
            }
            append = append.append(z).append(" tok.hidden=");
            if (this.mWin.mAppToken != null) {
                z = this.mWin.mAppToken.hidden;
            } else {
                z = false;
            }
            append = append.append(z).append(" animating=").append(this.mAnimating).append(" tok animating=");
            if (this.mAppAnimator != null) {
                z = this.mAppAnimator.animating;
            } else {
                z = false;
            }
            Slog.v(str, append.append(z).append(" Callers=").append(Debug.getCallers(3)).toString());
        }
        if ((SystemProperties.get("ro.mtk_low_band_tran_anim").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mDrawState == 3 && this.mWin.mAppToken != null && this.mWin.mAppToken.startingWindow != null && this.mAppAnimator != null && this.mAppAnimator.animating) || this.mDrawState != 3 || !this.mWin.isReadyForDisplayIgnoringKeyguard()) {
            return false;
        }
        if (DEBUG_VISIBILITY || (DEBUG_STARTING_WINDOW && this.mWin.mAttrs.type == 3)) {
            str = TAG;
            append = new StringBuilder().append("Showing ").append(this).append(" during animation: policyVis=").append(this.mWin.mPolicyVisibility).append(" attHidden=").append(this.mWin.mAttachedHidden).append(" tok.hiddenRequested=");
            if (this.mWin.mAppToken != null) {
                z = this.mWin.mAppToken.hiddenRequested;
            } else {
                z = false;
            }
            append = append.append(z).append(" tok.hidden=");
            if (this.mWin.mAppToken != null) {
                z = this.mWin.mAppToken.hidden;
            } else {
                z = false;
            }
            append = append.append(z).append(" animating=").append(this.mAnimating).append(" tok animating=");
            if (this.mAppAnimator != null) {
                z = this.mAppAnimator.animating;
            } else {
                z = false;
            }
            Slog.v(str, append.append(z).toString());
        }
        this.mService.enableScreenIfNeededLocked();
        applyEnterAnimationLocked();
        this.mLastAlpha = -1.0f;
        if (DEBUG_SURFACE_TRACE || DEBUG_ANIM) {
            Slog.v(TAG, "performShowLocked: mDrawState=HAS_DRAWN in " + this.mWin);
        }
        this.mDrawState = 4;
        this.mService.scheduleAnimationLocked();
        int i = this.mWin.mChildWindows.size();
        while (i > 0) {
            i--;
            WindowState c = (WindowState) this.mWin.mChildWindows.get(i);
            if (c.mAttachedHidden) {
                c.mAttachedHidden = false;
                if (c.mWinAnimator.mSurfaceController != null) {
                    c.mWinAnimator.performShowLocked();
                    DisplayContent displayContent = c.getDisplayContent();
                    if (displayContent != null) {
                        displayContent.layoutNeeded = true;
                    }
                }
            }
        }
        if (!(this.mWin.mAttrs.type == 3 || this.mWin.mAppToken == null)) {
            this.mWin.mAppToken.onFirstWindowDrawn(this.mWin, this);
        }
        if (this.mWin.mAttrs.type == 2011) {
            this.mWin.mDisplayContent.mDividerControllerLocked.resetImeHideRequested();
        }
        return true;
    }

    private boolean showSurfaceRobustlyLocked() {
        Task task = this.mWin.getTask();
        if (task != null && StackId.windowsAreScaleable(task.mStack.mStackId)) {
            this.mSurfaceController.forceScaleableInTransaction(true);
        }
        if (!this.mSurfaceController.showRobustlyInTransaction()) {
            return false;
        }
        this.mService.updateNonSystemOverlayWindowsVisibilityIfNeeded(this.mWin, true);
        if (this.mWin.mTurnOnScreen) {
            if (DEBUG_VISIBILITY || !WindowManagerService.IS_USER_BUILD) {
                Slog.v(TAG, "Show surface turning screen on: " + this.mWin);
            }
            this.mWin.mTurnOnScreen = false;
            WindowAnimator windowAnimator = this.mAnimator;
            windowAnimator.mBulkUpdateParams |= 16;
        }
        return true;
    }

    void applyEnterAnimationLocked() {
        if (!this.mWin.mSkipEnterAnimationForSeamlessReplacement) {
            int transit;
            if (this.mEnterAnimationPending) {
                this.mEnterAnimationPending = false;
                transit = 1;
            } else {
                transit = 3;
            }
            applyAnimationLocked(transit, true);
            if (this.mService.mAccessibilityController != null && this.mWin.getDisplayId() == 0) {
                this.mService.mAccessibilityController.onWindowTransitionLocked(this.mWin, transit);
            }
        }
    }

    boolean applyAnimationLocked(int transit, boolean isEntrance) {
        boolean z = true;
        if ((this.mLocalAnimating && this.mAnimationIsEntrance == isEntrance) || this.mKeyguardGoingAwayAnimation) {
            if (this.mAnimation != null && this.mKeyguardGoingAwayAnimation && transit == 5) {
                applyFadeoutDuringKeyguardExitAnimation();
            }
            return true;
        }
        Trace.traceBegin(32, "WSA#applyAnimationLocked");
        if (this.mService.okToDisplay()) {
            int anim = this.mPolicy.selectAnimationLw(this.mWin, transit);
            int attr = -1;
            Animation a = null;
            if (anim == 0) {
                switch (transit) {
                    case 1:
                        attr = 0;
                        break;
                    case 2:
                        attr = 1;
                        break;
                    case 3:
                        attr = 2;
                        break;
                    case 4:
                        attr = 3;
                        break;
                }
                if (attr >= 0) {
                    a = this.mService.mAppTransition.loadAnimationAttr(this.mWin.mAttrs, attr);
                }
            } else if (anim != -1) {
                a = AnimationUtils.loadAnimation(this.mContext, anim);
            } else {
                a = null;
            }
            if (DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: win=" + this + " anim=" + anim + " attr=0x" + Integer.toHexString(attr) + " a=" + a + " transit=" + transit + " isEntrance=" + isEntrance + " Callers " + Debug.getCallers(3));
            }
            if (a != null) {
                if (DEBUG_ANIM) {
                    WindowManagerService.logWithStack(TAG, "Loaded animation " + a + " for " + this);
                }
                if (SystemProperties.get("ro.mtk_low_band_tran_anim").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && transit == 5) {
                    a.setDuration(PENDING_TRANSACTION_FINISH_WAIT_TIME);
                }
                if (this.mService.isFastStartingWindowSupport() && this.mWin.isFastStartingWindow() && transit == 5) {
                    if (this.mWmDuration != -1) {
                        a.setDuration((long) this.mWmDuration);
                    }
                    if (this.mWmOffset != -1) {
                        a.setStartOffset((long) this.mWmOffset);
                    }
                }
                setAnimation(a);
                this.mAnimationIsEntrance = isEntrance;
            }
        } else {
            clearAnimation();
        }
        Trace.traceEnd(32);
        if (this.mWin.mAttrs.type == 2011) {
            this.mService.adjustForImeIfNeeded(this.mWin.mDisplayContent);
            if (isEntrance) {
                this.mWin.setDisplayLayoutNeeded();
                this.mService.mWindowPlacerLocked.requestTraversal();
            }
        }
        if (this.mAnimation == null) {
            z = false;
        }
        return z;
    }

    private void applyFadeoutDuringKeyguardExitAnimation() {
        long startTime = this.mAnimation.getStartTime();
        long duration = this.mAnimation.getDuration();
        long elapsed = this.mLastAnimationTime - startTime;
        long fadeDuration = duration - elapsed;
        if (fadeDuration > 0) {
            AnimationSet newAnimation = new AnimationSet(false);
            newAnimation.setDuration(duration);
            newAnimation.setStartTime(startTime);
            newAnimation.addAnimation(this.mAnimation);
            Animation fadeOut = AnimationUtils.loadAnimation(this.mContext, 17432593);
            fadeOut.setDuration(fadeDuration);
            fadeOut.setStartOffset(elapsed);
            newAnimation.addAnimation(fadeOut);
            newAnimation.initialize(this.mWin.mFrame.width(), this.mWin.mFrame.height(), this.mAnimDx, this.mAnimDy);
            this.mAnimation = newAnimation;
        }
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (this.mAnimating || this.mLocalAnimating || this.mAnimationIsEntrance || this.mAnimation != null) {
            pw.print(prefix);
            pw.print("mAnimating=");
            pw.print(this.mAnimating);
            pw.print(" mLocalAnimating=");
            pw.print(this.mLocalAnimating);
            pw.print(" mAnimationIsEntrance=");
            pw.print(this.mAnimationIsEntrance);
            pw.print(" mAnimation=");
            pw.print(this.mAnimation);
            pw.print(" mStackClip=");
            pw.println(this.mStackClip);
        }
        if (this.mHasTransformation || this.mHasLocalTransformation) {
            pw.print(prefix);
            pw.print("XForm: has=");
            pw.print(this.mHasTransformation);
            pw.print(" hasLocal=");
            pw.print(this.mHasLocalTransformation);
            pw.print(" ");
            this.mTransformation.printShortString(pw);
            pw.println();
        }
        if (this.mSurfaceController != null) {
            this.mSurfaceController.dump(pw, prefix, dumpAll);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mDrawState=");
            pw.print(drawStateToString());
            pw.print(prefix);
            pw.print(" mLastHidden=");
            pw.println(this.mLastHidden);
            pw.print(prefix);
            pw.print("mSystemDecorRect=");
            this.mSystemDecorRect.printShortString(pw);
            pw.print(" last=");
            this.mLastSystemDecorRect.printShortString(pw);
            pw.print(" mHasClipRect=");
            pw.print(this.mHasClipRect);
            pw.print(" mLastClipRect=");
            this.mLastClipRect.printShortString(pw);
            if (!this.mLastFinalClipRect.isEmpty()) {
                pw.print(" mLastFinalClipRect=");
                this.mLastFinalClipRect.printShortString(pw);
            }
            pw.println();
        }
        if (this.mPendingDestroySurface != null) {
            pw.print(prefix);
            pw.print("mPendingDestroySurface=");
            pw.println(this.mPendingDestroySurface);
        }
        if (this.mSurfaceResized || this.mSurfaceDestroyDeferred) {
            pw.print(prefix);
            pw.print("mSurfaceResized=");
            pw.print(this.mSurfaceResized);
            pw.print(" mSurfaceDestroyDeferred=");
            pw.println(this.mSurfaceDestroyDeferred);
        }
        if (!(this.mShownAlpha == 1.0f && this.mAlpha == 1.0f && this.mLastAlpha == 1.0f)) {
            pw.print(prefix);
            pw.print("mShownAlpha=");
            pw.print(this.mShownAlpha);
            pw.print(" mAlpha=");
            pw.print(this.mAlpha);
            pw.print(" mLastAlpha=");
            pw.println(this.mLastAlpha);
        }
        if (this.mHaveMatrix || this.mWin.mGlobalScale != 1.0f) {
            pw.print(prefix);
            pw.print("mGlobalScale=");
            pw.print(this.mWin.mGlobalScale);
            pw.print(" mDsDx=");
            pw.print(this.mDsDx);
            pw.print(" mDtDx=");
            pw.print(this.mDtDx);
            pw.print(" mDsDy=");
            pw.print(this.mDsDy);
            pw.print(" mDtDy=");
            pw.println(this.mDtDy);
        }
        if (this.mAnimationStartDelayed) {
            pw.print(prefix);
            pw.print("mAnimationStartDelayed=");
            pw.print(this.mAnimationStartDelayed);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("WindowStateAnimator{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.mWin.mAttrs.getTitle());
        sb.append('}');
        return sb.toString();
    }

    void reclaimSomeSurfaceMemory(String operation, boolean secure) {
        this.mService.reclaimSomeSurfaceMemoryLocked(this, operation, secure);
    }

    boolean getShown() {
        if (this.mSurfaceController != null) {
            return this.mSurfaceController.getShown();
        }
        return false;
    }

    void destroySurface() {
        try {
            if (this.mSurfaceController != null) {
                this.mSurfaceController.destroyInTransaction();
            }
            this.mWin.setHasSurface(false);
            this.mSurfaceController = null;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Exception thrown when destroying surface " + this + " surface " + this.mSurfaceController + " session " + this.mSession + ": " + e);
            this.mWin.setHasSurface(false);
            this.mSurfaceController = null;
        } catch (Throwable th) {
            this.mWin.setHasSurface(false);
            this.mSurfaceController = null;
            this.mDrawState = 0;
        }
        this.mDrawState = 0;
    }

    void setMoveAnimation(int left, int top) {
        setAnimation(AnimationUtils.loadAnimation(this.mContext, 17432753));
        this.mAnimDx = this.mWin.mLastFrame.left - left;
        this.mAnimDy = this.mWin.mLastFrame.top - top;
        this.mAnimateMove = true;
    }

    void deferTransactionUntilParentFrame(long frameNumber) {
        if (this.mWin.isChildWindow() && this.mWin.mAttachedWindow.mWinAnimator.mSurfaceController != null) {
            this.mSurfaceController.deferTransactionUntil(this.mWin.mAttachedWindow.mWinAnimator.mSurfaceController.getHandle(), frameNumber);
        }
    }

    private long getAnimationFrameTime(Animation animation, long currentTime) {
        if (!this.mAnimationStartDelayed) {
            return currentTime;
        }
        animation.setStartTime(currentTime);
        return 1 + currentTime;
    }

    void startDelayingAnimationStart() {
        this.mAnimationStartDelayed = true;
    }

    void endDelayingAnimationStart() {
        this.mAnimationStartDelayed = false;
    }

    void seamlesslyRotateWindow(int oldRotation, int newRotation) {
        WindowState w = this.mWin;
        if (w.isVisibleNow() && !w.mIsWallpaper) {
            Rect cropRect = this.mService.mTmpRect;
            Rect displayRect = this.mService.mTmpRect2;
            RectF frameRect = this.mService.mTmpRectF;
            Matrix transform = this.mService.mTmpTransform;
            float x = (float) w.mFrame.left;
            float y = (float) w.mFrame.top;
            float width = (float) w.mFrame.width();
            float height = (float) w.mFrame.height();
            this.mService.getDefaultDisplayContentLocked().getLogicalDisplayRect(displayRect);
            DisplayContent.createRotationMatrix(DisplayContent.deltaRotation(newRotation, oldRotation), x, y, (float) displayRect.width(), (float) displayRect.height(), transform);
            if (w.isChildWindow() && this.mSurfaceController.getTransformToDisplayInverse()) {
                frameRect.set(x, y, x + width, y + height);
                transform.mapRect(frameRect);
                w.mAttrs.x = ((int) frameRect.left) - w.mAttachedWindow.mFrame.left;
                w.mAttrs.y = ((int) frameRect.top) - w.mAttachedWindow.mFrame.top;
                w.mAttrs.width = (int) Math.ceil((double) frameRect.width());
                w.mAttrs.height = (int) Math.ceil((double) frameRect.height());
                w.setWindowScale(w.mRequestedWidth, w.mRequestedHeight);
                w.applyGravityAndUpdateFrame(w.mContainingFrame, w.mDisplayFrame);
                computeShownFrameLocked();
                setSurfaceBoundariesLocked(false);
                cropRect.set(0, 0, w.mRequestedWidth, w.mRequestedWidth + w.mRequestedHeight);
                this.mSurfaceController.setCropInTransaction(cropRect, false);
            } else {
                this.mService.markForSeamlessRotation(w, true);
                transform.getValues(this.mService.mTmpFloats);
                float DsDx = this.mService.mTmpFloats[0];
                float DtDx = this.mService.mTmpFloats[3];
                float DsDy = this.mService.mTmpFloats[1];
                float DtDy = this.mService.mTmpFloats[4];
                this.mSurfaceController.setPositionInTransaction(this.mService.mTmpFloats[2], this.mService.mTmpFloats[5], false);
                this.mSurfaceController.setMatrixInTransaction(w.mHScale * DsDx, w.mVScale * DtDx, w.mHScale * DsDy, w.mVScale * DtDy, false);
            }
        }
    }

    void drawIfNeeded() {
        if (this.mSurfaceController != null) {
            if (this.mSurfaceController.mSurfaceControl == null) {
                Slog.i(TAG, "drawIfNeeded, mSurfaceControl is released");
                return;
            }
            if (this.mDrawNeeded) {
                Slog.i(TAG, "drawIfNeeded");
                Surface mSurface = new Surface();
                try {
                    mSurface.copyFrom(this.mSurfaceController.mSurfaceControl);
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "copyFrom, IllegalArgumentException");
                } catch (OutOfResourcesException e2) {
                    Slog.e(TAG, "copyFrom, OutOfResourcesException");
                } catch (NullPointerException e3) {
                    Slog.e(TAG, "copyFrom, NullPointerException");
                }
                if (mSurface != null) {
                    int dw = (int) this.mSurfaceController.mSurfaceW;
                    int dh = (int) this.mSurfaceController.mSurfaceH;
                    this.mDrawNeeded = false;
                    try {
                        Canvas c = mSurface.lockCanvas(new Rect(0, 0, dw, dh));
                        Slog.i(TAG, "lockCanvas, mToken =" + this.mWin.mToken);
                        if (!(c == null || this.mWin.mToken == null)) {
                            Bitmap bitmap = this.mService.getBitmapByToken(this.mWin.mToken.token);
                            if (bitmap != null) {
                                c.drawBitmap(bitmap, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, null);
                            }
                            Slog.i(TAG, "unlockCanvasAndPost");
                            mSurface.unlockCanvasAndPost(c);
                        }
                        if (mSurface != null) {
                            mSurface.release();
                        }
                    } catch (IllegalArgumentException e4) {
                        Slog.e(TAG, "Could not unlock surface, surface = " + mSurface + ", canvas = " + null + ", this = " + this, e4);
                        if (mSurface != null) {
                            mSurface.release();
                        }
                    } catch (IllegalStateException e5) {
                        Slog.e(TAG, "IllegalStateException, this = " + this, e5);
                        if (mSurface != null) {
                            mSurface.release();
                        }
                    } catch (OutOfResourcesException e6) {
                        Slog.e(TAG, "OutOfResourcesException, surface = " + mSurface + ", canvas = " + null + ", this = " + this, e6);
                        if (mSurface != null) {
                            mSurface.release();
                        }
                    } catch (Throwable th) {
                        if (mSurface != null) {
                            mSurface.release();
                        }
                    }
                } else {
                    Slog.i(TAG, "drawIfNeeded, mSurface is null");
                }
            }
        }
    }

    void doCacheBitmap() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... para) {
                try {
                    if (WindowStateAnimator.this.mWmSleep != -1) {
                        Thread.sleep((long) WindowStateAnimator.this.mWmSleep);
                    } else {
                        Thread.sleep(WindowStateAnimator.PENDING_TRANSACTION_FINISH_WAIT_TIME);
                    }
                } catch (Exception e) {
                }
                Trace.traceBegin(32, "AsyncScreenshot");
                Bitmap bmShot = SurfaceControl.screenshot(new Rect(), (int) WindowStateAnimator.this.mWin.mWinAnimator.mSurfaceController.mSurfaceW, (int) WindowStateAnimator.this.mWin.mWinAnimator.mSurfaceController.mSurfaceH, WindowStateAnimator.this.mSurfaceController.mSurfaceLayer, WindowStateAnimator.this.mSurfaceController.mSurfaceLayer, false, 0);
                Slog.i(WindowStateAnimator.TAG, "doCacheBitmap, mToken =" + WindowStateAnimator.this.mWin.mToken);
                if (!(bmShot == null || WindowStateAnimator.this.mWin.mToken == null)) {
                    WindowStateAnimator.this.mService.setBitmapByToken(WindowStateAnimator.this.mWin.mToken.token, bmShot.copy(bmShot.getConfig(), true));
                    bmShot.recycle();
                }
                Trace.traceEnd(32);
                return null;
            }
        }.execute(new Void[0]);
    }

    void doCacheBitmap(View view) {
        if (this.mWin.mToken != null) {
            IBinder token = this.mWin.mToken.token;
            Trace.traceBegin(32, "SyncScreenshot");
            try {
                view.setDrawingCacheEnabled(true);
                Bitmap bmShot = Bitmap.createBitmap(view.getDrawingCache());
                Slog.i(TAG, "doCacheBitmap, mToken =" + token);
                if (!(bmShot == null || token == null)) {
                    this.mService.setBitmapByToken(token, bmShot.copy(bmShot.getConfig(), true));
                    bmShot.recycle();
                }
            } catch (Throwable e) {
                Slog.e(TAG, "doSyncCacheBitmap, this = " + this, e);
            }
            Trace.traceEnd(32);
        }
    }

    public boolean checkAnimationExitError(boolean onlyCheck) {
        boolean foundError = false;
        if (!(this.mAnimating || this.mLocalAnimating || this.mAnimation != null)) {
            if (DEBUG_ANIM) {
                Slog.d(TAG, "checkAnimationExitError:no animation or finished! mRemoveOnExit:" + this.mWin.mRemoveOnExit + ", mAnimatingExit:" + this.mWin.mAnimatingExit + ", mAnimationIsEntrance:" + this.mAnimationIsEntrance + ", onlyCheck:" + onlyCheck);
            }
            boolean carryoutFinishExit = false;
            if (!this.mAnimationIsEntrance && this.mWin.mRemoveOnExit && this.mWin.mAnimatingExit) {
                Slog.w(TAG, "checkAnimationExitError:AnimationIsExit, should finishExit but not yet!");
                if (!onlyCheck) {
                    carryoutFinishExit = true;
                }
                foundError = true;
            } else if (this.mAnimationIsEntrance && this.mSurfaceController == null) {
                Slog.w(TAG, "checkAnimationExitError:AnimationIsEntrance, no surface any more, exit will be good");
                if (!onlyCheck) {
                    carryoutFinishExit = true;
                }
                foundError = true;
            }
            if (carryoutFinishExit) {
                Slog.w(TAG, "checkAnimationExitError:finishExit begin.");
                finishExit();
                Slog.w(TAG, "checkAnimationExitError:finishExit end.");
            }
        }
        return foundError;
    }

    public void finishExitInner() {
        Slog.d(TAG, "finishExitInner:now finish and exit the anim");
        finishExit();
    }
}
