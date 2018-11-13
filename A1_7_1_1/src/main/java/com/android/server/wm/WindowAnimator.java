package com.android.server.wm;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.os.Trace;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.Choreographer.FrameCallback;
import android.view.SurfaceControl;
import android.view.WindowManagerPolicy;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import java.io.PrintWriter;
import java.util.ArrayList;

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
public class WindowAnimator {
    static final int KEYGUARD_ANIMATING_OUT = 2;
    private static final long KEYGUARD_ANIM_TIMEOUT_MS = 1000;
    static final int KEYGUARD_NOT_SHOWN = 0;
    static final int KEYGUARD_SHOWN = 1;
    private static final String TAG = null;
    private int mAnimTransactionSequence;
    private boolean mAnimating;
    final FrameCallback mAnimationFrameCallback;
    final Runnable mAnimationRunnable;
    boolean mAppWindowAnimating;
    int mBulkUpdateParams;
    final Context mContext;
    long mCurrentTime;
    SparseArray<DisplayContentsAnimator> mDisplayContentsAnimators;
    boolean mDisplayed;
    int mForceHiding;
    boolean mInitialized;
    boolean mKeyguardGoingAway;
    int mKeyguardGoingAwayFlags;
    private WindowState mLastShowWinWhenLocked;
    Object mLastWindowFreezeSource;
    final WindowManagerPolicy mPolicy;
    Animation mPostKeyguardExitAnimation;
    private boolean mRemoveReplacedWindows;
    final WindowManagerService mService;
    private final AppTokenList mTmpExitingAppTokens;
    WindowState mWindowDetachedWallpaper;
    private final WindowSurfacePlacer mWindowPlacerLocked;

    private class DisplayContentsAnimator {
        ScreenRotationAnimation mScreenRotationAnimation;

        /* synthetic */ DisplayContentsAnimator(WindowAnimator this$0, DisplayContentsAnimator displayContentsAnimator) {
            this();
        }

        private DisplayContentsAnimator() {
            this.mScreenRotationAnimation = null;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowAnimator.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowAnimator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowAnimator.<clinit>():void");
    }

    private String forceHidingToString() {
        switch (this.mForceHiding) {
            case 0:
                return "KEYGUARD_NOT_SHOWN";
            case 1:
                return "KEYGUARD_SHOWN";
            case 2:
                return "KEYGUARD_ANIMATING_OUT";
            default:
                return "KEYGUARD STATE UNKNOWN " + this.mForceHiding;
        }
    }

    WindowAnimator(WindowManagerService service) {
        this.mWindowDetachedWallpaper = null;
        this.mBulkUpdateParams = 0;
        this.mDisplayContentsAnimators = new SparseArray(2);
        this.mInitialized = false;
        this.mForceHiding = 0;
        this.mRemoveReplacedWindows = false;
        this.mTmpExitingAppTokens = new AppTokenList();
        this.mDisplayed = false;
        this.mService = service;
        this.mContext = service.mContext;
        this.mPolicy = service.mPolicy;
        this.mAnimationRunnable = new Runnable() {
            public void run() {
                synchronized (WindowAnimator.this.mService.mWindowMap) {
                    WindowAnimator.this.mService.mAnimationScheduled = false;
                    WindowAnimator.this.animateLocked(System.nanoTime());
                }
            }
        };
        this.mWindowPlacerLocked = service.mWindowPlacerLocked;
        this.mAnimationFrameCallback = new FrameCallback() {
            public void doFrame(long frameTimeNs) {
                synchronized (WindowAnimator.this.mService.mWindowMap) {
                    WindowAnimator.this.mService.mAnimationScheduled = false;
                    Trace.traceBegin(32, "wmAnimate");
                    WindowAnimator.this.animateLocked(frameTimeNs);
                    Trace.traceEnd(32);
                }
            }
        };
    }

    void addDisplayLocked(int displayId) {
        getDisplayContentsAnimatorLocked(displayId);
        if (displayId == 0) {
            this.mInitialized = true;
        }
    }

    void removeDisplayLocked(int displayId) {
        DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.get(displayId);
        if (!(displayAnimator == null || displayAnimator.mScreenRotationAnimation == null)) {
            displayAnimator.mScreenRotationAnimation.kill();
            displayAnimator.mScreenRotationAnimation = null;
        }
        this.mDisplayContentsAnimators.delete(displayId);
    }

    private void updateAppWindowsLocked(int displayId) {
        ArrayList<TaskStack> stacks = this.mService.getDisplayContentLocked(displayId).getStacks();
        for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
            AppWindowAnimator appAnimator;
            TaskStack stack = (TaskStack) stacks.get(stackNdx);
            ArrayList<Task> tasks = stack.getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                    appAnimator = ((AppWindowToken) tokens.get(tokenNdx)).mAppAnimator;
                    appAnimator.wasAnimating = appAnimator.animating;
                    if (appAnimator.stepAnimationLocked(this.mCurrentTime, displayId)) {
                        appAnimator.animating = true;
                        setAnimating(true);
                        this.mAppWindowAnimating = true;
                    } else if (appAnimator.wasAnimating) {
                        setAppLayoutChanges(appAnimator, 4, "appToken " + appAnimator.mAppToken + " done", displayId);
                        if (WindowManagerDebugConfig.DEBUG_ANIM) {
                            Slog.v(TAG, "updateWindowsApps...: done animating " + appAnimator.mAppToken);
                        }
                    }
                }
            }
            this.mTmpExitingAppTokens.clear();
            this.mTmpExitingAppTokens.addAll(stack.mExitingAppTokens);
            int exitingCount = this.mTmpExitingAppTokens.size();
            for (int i = 0; i < exitingCount; i++) {
                appAnimator = ((AppWindowToken) this.mTmpExitingAppTokens.get(i)).mAppAnimator;
                if (stack.mExitingAppTokens.contains(appAnimator)) {
                    appAnimator.wasAnimating = appAnimator.animating;
                    if (appAnimator.stepAnimationLocked(this.mCurrentTime, displayId)) {
                        setAnimating(true);
                        this.mAppWindowAnimating = true;
                    } else if (appAnimator.wasAnimating) {
                        setAppLayoutChanges(appAnimator, 4, "exiting appToken " + appAnimator.mAppToken + " done", displayId);
                        if (WindowManagerDebugConfig.DEBUG_ANIM) {
                            Slog.v(TAG, "updateWindowsApps...: done animating exiting " + appAnimator.mAppToken);
                        }
                    }
                }
            }
        }
    }

    private WindowState getWinShowWhenLockedOrAnimating() {
        WindowState winShowWhenLocked = (WindowState) this.mPolicy.getWinShowWhenLockedLw();
        if (winShowWhenLocked != null) {
            return winShowWhenLocked;
        }
        if (this.mLastShowWinWhenLocked == null || !this.mLastShowWinWhenLocked.isOnScreen() || !this.mLastShowWinWhenLocked.isAnimatingLw() || (this.mLastShowWinWhenLocked.mAttrs.flags & DumpState.DUMP_FROZEN) == 0) {
            return null;
        }
        return this.mLastShowWinWhenLocked;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Wanjiang.Xiong@Plf.DesktopApp.Keyguard add for ColorOS Keyguard", property = OppoRomType.OPPO)
    private boolean shouldForceHide(WindowState win) {
        boolean allowWhenLocked;
        int i;
        int i2 = 0;
        WindowState imeTarget = this.mService.mInputMethodTarget;
        boolean showImeOverKeyguard = (imeTarget == null || !imeTarget.isVisibleNow()) ? false : (imeTarget.getAttrs().flags & DumpState.DUMP_FROZEN) == 0 ? !this.mPolicy.canBeForceHidden(imeTarget, imeTarget.mAttrs) : true;
        WindowState winShowWhenLocked = getWinShowWhenLockedOrAnimating();
        AppWindowToken appShowWhenLocked = winShowWhenLocked == null ? null : winShowWhenLocked.mAppToken;
        if (win.mIsImWindow || imeTarget == win) {
            allowWhenLocked = showImeOverKeyguard;
        } else {
            allowWhenLocked = false;
        }
        if ((win.mAttrs.flags & DumpState.DUMP_FROZEN) != 0) {
            i = win.mTurnOnScreen;
        } else {
            i = 0;
        }
        allowWhenLocked |= i;
        if (appShowWhenLocked != null) {
            i = (appShowWhenLocked == win.mAppToken || (win.mAttrs.flags & DumpState.DUMP_FROZEN) != 0) ? 1 : (win.mAttrs.privateFlags & 256) != 0 ? 1 : 0;
            allowWhenLocked |= i;
        }
        if ((win.mAttrs.flags & 4194304) != 0) {
            i2 = this.mPolicy.canShowDismissingWindowWhileLockedLw();
        }
        allowWhenLocked |= i2;
        boolean keyguardOn = this.mPolicy.isKeyguardShowingOrOccluded() ? this.mForceHiding == 1 : false;
        if (win.getWindowTag() != null && "com.zing.zalo".equals(win.getWindowTag().toString())) {
            keyguardOn = this.mPolicy.isKeyguardShowingOrOccluded();
        }
        boolean hideDockDivider = (win.mAttrs.type == 2034 && keyguardOn) ? win.getDisplayContent().getDockedStackLocked() == null : false;
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.d(TAG, "shouldForceHide: keyguardOn=" + keyguardOn + ", isKeyguardShowingOrOccluded=" + this.mPolicy.isKeyguardShowingOrOccluded() + ", mForceHiding=" + forceHidingToString() + ", allowWhenLocked=" + allowWhenLocked + ", hideDockDivider=" + hideDockDivider + ", appShowWhenLocked=" + appShowWhenLocked + ", flag=#" + Integer.toHexString(win.mAttrs.flags) + ", privateFlags=#" + Integer.toHexString(win.mAttrs.privateFlags) + ", mIsImWindow=" + win.mIsImWindow + ", mTurnOnScreen=" + win.mTurnOnScreen + ", w=" + win);
        }
        if (keyguardOn && !allowWhenLocked && win.getDisplayId() == 0) {
            return true;
        }
        return hideDockDivider;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "YuHao@Plf.DesktopApp.Keyguard add for ColorOS Keyguard", property = OppoRomType.OPPO)
    private void updateWindowsLocked(int displayId) {
        int i;
        WindowState win;
        WindowStateAnimator winAnimator;
        this.mAnimTransactionSequence++;
        WindowList windows = this.mService.getWindowListLocked(displayId);
        boolean keyguardGoingAwayToShade = (this.mKeyguardGoingAwayFlags & 1) != 0;
        boolean keyguardGoingAwayNoAnimation = (this.mKeyguardGoingAwayFlags & 2) != 0;
        boolean keyguardGoingAwayWithWallpaper = (this.mKeyguardGoingAwayFlags & 4) != 0;
        if (this.mKeyguardGoingAway) {
            i = windows.size() - 1;
            while (i >= 0) {
                win = (WindowState) windows.get(i);
                if (this.mPolicy.isKeyguardHostWindow(win.mAttrs)) {
                    winAnimator = win.mWinAnimator;
                    if ((win.mAttrs.privateFlags & 1024) == 0) {
                        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                            Slog.d(TAG, "updateWindowsLocked: StatusBar is no longer keyguard");
                        }
                        this.mKeyguardGoingAway = false;
                        winAnimator.clearAnimation();
                    } else if (!winAnimator.mAnimating) {
                        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                            Slog.d(TAG, "updateWindowsLocked: creating delay animation");
                        }
                        winAnimator.mAnimation = new AlphaAnimation(1.0f, 1.0f);
                        winAnimator.mAnimation.setDuration(1000);
                        winAnimator.mAnimationIsEntrance = false;
                        winAnimator.mAnimationStartTime = -1;
                        winAnimator.mKeyguardGoingAwayAnimation = true;
                        winAnimator.mKeyguardGoingAwayWithWallpaper = keyguardGoingAwayWithWallpaper;
                    }
                } else {
                    i--;
                }
            }
        }
        this.mForceHiding = 0;
        boolean wallpaperInUnForceHiding = false;
        boolean startingInUnForceHiding = false;
        ArrayList unForceHiding = null;
        WindowState wallpaper = null;
        WallpaperController wallpaperController = this.mService.mWallpaperControllerLocked;
        for (i = windows.size() - 1; i >= 0; i--) {
            win = (WindowState) windows.get(i);
            winAnimator = win.mWinAnimator;
            int flags = win.mAttrs.flags;
            boolean canBeForceHidden = this.mPolicy.canBeForceHidden(win, win.mAttrs);
            boolean shouldBeForceHidden = shouldForceHide(win);
            boolean needToFinishAnim = false;
            if (winAnimator.hasSurface()) {
                boolean wasAnimating = winAnimator.mWasAnimating;
                if (!(winAnimator.mAppAnimator == null || !winAnimator.mAppAnimator.animating || win.mIsImWindow || win.mAttrs.type == 3 || winAnimator.mLocalAnimating)) {
                    winAnimator.clearAnimation();
                }
                boolean nowAnimating = winAnimator.stepAnimationLocked(this.mCurrentTime);
                winAnimator.mWasAnimating = nowAnimating;
                orAnimating(nowAnimating);
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    Slog.v(TAG, win + ": wasAnimating=" + wasAnimating + ", nowAnimating=" + nowAnimating);
                }
                if (!(wasAnimating || nowAnimating)) {
                    needToFinishAnim = winAnimator.checkAnimationExitError(true);
                    if (WindowManagerDebugConfig.DEBUG_ANIM) {
                        Slog.d(TAG, "updateWindowsLocked:needToFinishAnim = " + needToFinishAnim);
                    }
                    if (needToFinishAnim) {
                        Slog.w(TAG, "updateWindowsLocked, this win need to finish anim, win:" + win);
                    }
                }
                if (wasAnimating && !winAnimator.mAnimating && wallpaperController.isWallpaperTarget(win)) {
                    this.mBulkUpdateParams |= 2;
                    setPendingLayoutChanges(0, 4);
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                        this.mWindowPlacerLocked.debugLayoutRepeats("updateWindowsAndWallpaperLocked 2", getPendingLayoutChanges(0));
                    }
                }
                if (this.mPolicy.isForceHiding(win.mAttrs) || this.mPolicy.isForceHiding(win)) {
                    if (!wasAnimating && nowAnimating) {
                        if (WindowManagerDebugConfig.DEBUG_KEYGUARD || WindowManagerDebugConfig.DEBUG_ANIM || WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG, "Animation started that could impact force hide: " + win);
                        }
                        this.mBulkUpdateParams |= 4;
                        setPendingLayoutChanges(displayId, 4);
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                            this.mWindowPlacerLocked.debugLayoutRepeats("updateWindowsAndWallpaperLocked 3", getPendingLayoutChanges(displayId));
                        }
                        this.mService.mFocusMayChange = true;
                    } else if (this.mKeyguardGoingAway && !nowAnimating) {
                        Slog.e(TAG, "Timeout waiting for animation to startup");
                        this.mPolicy.startKeyguardExitAnimation(0, 0);
                        this.mKeyguardGoingAway = false;
                    }
                    if (win.isReadyForDisplay()) {
                        if (nowAnimating && win.mWinAnimator.mKeyguardGoingAwayAnimation) {
                            this.mForceHiding = 2;
                        } else {
                            this.mForceHiding = win.isDrawnLw() ? 1 : 0;
                        }
                    }
                    if (win.mHideKeyguard) {
                        Slog.d(TAG, "wj_Keyguard, mForceHiding = " + this.mForceHiding);
                        this.mForceHiding = 0;
                    }
                    if ((WindowManagerDebugConfig.DEBUG_KEYGUARD || WindowManagerDebugConfig.DEBUG_VISIBILITY) && !WindowManagerService.IS_USER_BUILD) {
                        Slog.v(TAG, "Force hide " + forceHidingToString() + " hasSurface=" + win.mHasSurface + " policyVis=" + win.mPolicyVisibility + " destroying=" + win.mDestroying + " attHidden=" + win.mAttachedHidden + " vis=" + win.mViewVisibility + " hidden=" + win.mRootToken.hidden + " anim=" + win.mWinAnimator.mAnimation);
                    }
                } else if (canBeForceHidden) {
                    if (!shouldBeForceHidden) {
                        boolean applyExistingExitAnimation = (this.mPostKeyguardExitAnimation == null || this.mPostKeyguardExitAnimation.hasEnded() || winAnimator.mKeyguardGoingAwayAnimation || !win.hasDrawnLw() || win.mAttachedWindow != null || win.mIsImWindow) ? false : displayId == 0;
                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG, "applyExistingExitAnimation=" + applyExistingExitAnimation + " isVisibleNow=" + win.isVisibleNow());
                        }
                        if (!win.showLw(false, false) && !applyExistingExitAnimation) {
                            if (needToFinishAnim) {
                                Slog.w(TAG, "updateWindowsLocked: the win was aleady showing but need to finish anim. win:" + win);
                                winAnimator.finishExitInner();
                            }
                        } else if (win.isVisibleNow()) {
                            if (WindowManagerDebugConfig.DEBUG_KEYGUARD || WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG, "Now policy shown: " + win);
                            }
                            if ((this.mBulkUpdateParams & 4) != 0 && win.mAttachedWindow == null) {
                                if (unForceHiding == null) {
                                    unForceHiding = new ArrayList();
                                }
                                unForceHiding.add(winAnimator);
                                if ((DumpState.DUMP_DEXOPT & flags) != 0) {
                                    wallpaperInUnForceHiding = true;
                                }
                                if (win.mAttrs.type == 3) {
                                    startingInUnForceHiding = true;
                                }
                            } else if (applyExistingExitAnimation) {
                                if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                                    Slog.v(TAG, "Applying existing Keyguard exit animation to new window: win=" + win);
                                }
                                winAnimator.setAnimation(this.mPolicy.createForceHideEnterAnimation(false, keyguardGoingAwayToShade), this.mPostKeyguardExitAnimation.getStartTime(), 1);
                                winAnimator.mKeyguardGoingAwayAnimation = true;
                                winAnimator.mKeyguardGoingAwayWithWallpaper = keyguardGoingAwayWithWallpaper;
                            }
                            WindowState currentFocus = this.mService.mCurrentFocus;
                            if (currentFocus == null || currentFocus.mLayer < win.mLayer) {
                                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                                    Slog.v(TAG, "updateWindowsLocked: setting mFocusMayChange true");
                                }
                                this.mService.mFocusMayChange = true;
                            }
                        } else {
                            win.hideLw(false, false);
                            if (needToFinishAnim) {
                                Slog.w(TAG, "updateWindowsLocked: the win couldn't really show but need to finish anim. win:" + win);
                                winAnimator.finishExitInner();
                            }
                        }
                    } else if (!win.hideLw(false, false)) {
                        if (needToFinishAnim) {
                            Slog.w(TAG, "updateWindowsLocked: the win was aleady hidden but need to finish anim. win:" + win);
                            winAnimator.finishExitInner();
                        }
                    } else if (WindowManagerDebugConfig.DEBUG_KEYGUARD || WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG, "Now policy hidden: " + win);
                    }
                    if ((DumpState.DUMP_DEXOPT & flags) != 0) {
                        this.mBulkUpdateParams |= 2;
                        setPendingLayoutChanges(0, 4);
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                            this.mWindowPlacerLocked.debugLayoutRepeats("updateWindowsAndWallpaperLocked 4", getPendingLayoutChanges(0));
                        }
                    }
                }
            } else if (canBeForceHidden) {
                if (shouldBeForceHidden) {
                    win.hideLw(false, false);
                } else {
                    win.showLw(false, false);
                }
            }
            AppWindowToken atoken = win.mAppToken;
            if (winAnimator.mDrawState == 3 && ((atoken == null || atoken.allDrawn) && winAnimator.performShowLocked())) {
                setPendingLayoutChanges(displayId, 8);
                if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                    this.mWindowPlacerLocked.debugLayoutRepeats("updateWindowsAndWallpaperLocked 5", getPendingLayoutChanges(displayId));
                }
            }
            AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
            if (!(appAnimator == null || appAnimator.thumbnail == null)) {
                if (appAnimator.thumbnailTransactionSeq != this.mAnimTransactionSequence) {
                    appAnimator.thumbnailTransactionSeq = this.mAnimTransactionSequence;
                    appAnimator.thumbnailLayer = 0;
                }
                if (appAnimator.thumbnailLayer < winAnimator.mAnimLayer) {
                    appAnimator.thumbnailLayer = winAnimator.mAnimLayer;
                }
            }
            if (win.mIsWallpaper) {
                wallpaper = win;
            }
            if (needToFinishAnim) {
                Slog.w(TAG, "updateWindowsLocked:need to finish anim for win:" + win);
                winAnimator.checkAnimationExitError(false);
                Slog.w(TAG, "updateWindowsLocked:need to finish anim end.");
            }
        }
        if (unForceHiding != null) {
            Animation a;
            if (!keyguardGoingAwayNoAnimation) {
                boolean first = true;
                for (i = unForceHiding.size() - 1; i >= 0; i--) {
                    winAnimator = (WindowStateAnimator) unForceHiding.get(i);
                    WindowManagerPolicy windowManagerPolicy = this.mPolicy;
                    boolean z = wallpaperInUnForceHiding && !startingInUnForceHiding;
                    a = windowManagerPolicy.createForceHideEnterAnimation(z, keyguardGoingAwayToShade);
                    if (a != null) {
                        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                            Slog.v(TAG, "Starting keyguard exit animation on window " + winAnimator.mWin);
                        }
                        winAnimator.setAnimation(a, 1);
                        winAnimator.mKeyguardGoingAwayAnimation = true;
                        winAnimator.mKeyguardGoingAwayWithWallpaper = keyguardGoingAwayWithWallpaper;
                        if (first) {
                            this.mPostKeyguardExitAnimation = a;
                            this.mPostKeyguardExitAnimation.setStartTime(this.mCurrentTime);
                            first = false;
                        }
                    }
                }
            } else if (this.mKeyguardGoingAway) {
                this.mPolicy.startKeyguardExitAnimation(this.mCurrentTime, 0);
                this.mKeyguardGoingAway = false;
            }
            if (!(wallpaperInUnForceHiding || wallpaper == null || keyguardGoingAwayNoAnimation)) {
                if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                    Slog.d(TAG, "updateWindowsLocked: wallpaper animating away");
                }
                a = this.mPolicy.createForceHideWallpaperExitAnimation(keyguardGoingAwayToShade);
                if (a != null) {
                    wallpaper.mWinAnimator.setAnimation(a);
                }
            }
        }
        if (this.mPostKeyguardExitAnimation != null) {
            if (this.mKeyguardGoingAway) {
                this.mPolicy.startKeyguardExitAnimation(this.mCurrentTime + this.mPostKeyguardExitAnimation.getStartOffset(), this.mPostKeyguardExitAnimation.getDuration());
                this.mKeyguardGoingAway = false;
            } else if (this.mPostKeyguardExitAnimation.hasEnded() || this.mCurrentTime - this.mPostKeyguardExitAnimation.getStartTime() > this.mPostKeyguardExitAnimation.getDuration()) {
                if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                    Slog.v(TAG, "Done with Keyguard exit animations.");
                }
                this.mPostKeyguardExitAnimation = null;
            }
        }
        WindowState winShowWhenLocked = (WindowState) this.mPolicy.getWinShowWhenLockedLw();
        if (winShowWhenLocked != null) {
            this.mLastShowWinWhenLocked = winShowWhenLocked;
        }
    }

    private void updateWallpaperLocked(int displayId) {
        this.mService.getDisplayContentLocked(displayId).resetAnimationBackgroundAnimator();
        WindowList windows = this.mService.getWindowListLocked(displayId);
        WindowState detachedWallpaper = null;
        for (int i = windows.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) windows.get(i);
            WindowStateAnimator winAnimator = win.mWinAnimator;
            if (winAnimator.mSurfaceController != null && winAnimator.hasSurface()) {
                int color;
                TaskStack stack;
                int flags = win.mAttrs.flags;
                if (winAnimator.mAnimating) {
                    if (winAnimator.mAnimation != null) {
                        if ((flags & DumpState.DUMP_DEXOPT) != 0 && winAnimator.mAnimation.getDetachWallpaper()) {
                            detachedWallpaper = win;
                        }
                        color = winAnimator.mAnimation.getBackgroundColor();
                        if (color != 0) {
                            stack = win.getStack();
                            if (stack != null) {
                                stack.setAnimationBackground(winAnimator, color);
                            }
                        }
                    }
                    setAnimating(true);
                }
                AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
                if (!(appAnimator == null || appAnimator.animation == null || !appAnimator.animating)) {
                    if ((flags & DumpState.DUMP_DEXOPT) != 0 && appAnimator.animation.getDetachWallpaper()) {
                        detachedWallpaper = win;
                    }
                    color = appAnimator.animation.getBackgroundColor();
                    if (color != 0) {
                        stack = win.getStack();
                        if (stack != null) {
                            stack.setAnimationBackground(winAnimator, color);
                        }
                    }
                }
            }
        }
        if (this.mWindowDetachedWallpaper != detachedWallpaper) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                Slog.v(TAG, "Detached wallpaper changed from " + this.mWindowDetachedWallpaper + " to " + detachedWallpaper);
            }
            this.mWindowDetachedWallpaper = detachedWallpaper;
            this.mBulkUpdateParams |= 2;
        }
    }

    private void testTokenMayBeDrawnLocked(int displayId) {
        ArrayList<Task> tasks = this.mService.getDisplayContentLocked(displayId).getTasks();
        int numTasks = tasks.size();
        for (int taskNdx = 0; taskNdx < numTasks; taskNdx++) {
            AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
            int numTokens = tokens.size();
            for (int tokenNdx = 0; tokenNdx < numTokens; tokenNdx++) {
                AppWindowToken wtoken = (AppWindowToken) tokens.get(tokenNdx);
                AppWindowAnimator appAnimator = wtoken.mAppAnimator;
                boolean allDrawn = wtoken.allDrawn;
                if (allDrawn != appAnimator.allDrawn) {
                    appAnimator.allDrawn = allDrawn;
                    if (allDrawn) {
                        if (appAnimator.freezingScreen) {
                            appAnimator.showAllWindowsLocked();
                            this.mService.unsetAppFreezingScreenLocked(wtoken, false, true);
                            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.i(TAG, "Setting mOrientationChangeComplete=true because wtoken " + wtoken + " numInteresting=" + wtoken.numInterestingWindows + " numDrawn=" + wtoken.numDrawnWindows);
                            }
                            setAppLayoutChanges(appAnimator, 4, "testTokenMayBeDrawnLocked: freezingScreen", displayId);
                        } else {
                            setAppLayoutChanges(appAnimator, 8, "testTokenMayBeDrawnLocked", displayId);
                            if (!this.mService.mOpeningApps.contains(wtoken)) {
                                orAnimating(appAnimator.showAllWindowsLocked());
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x019a  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0374  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x03b0  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x03c8  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x03f0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void animateLocked(long frameTimeNs) {
        int displayNdx;
        boolean doRequest;
        if (this.mInitialized) {
            int numDisplays;
            boolean hasPendingLayoutChanges;
            this.mCurrentTime = frameTimeNs / 1000000;
            this.mBulkUpdateParams = 8;
            boolean wasAnimating = this.mAnimating;
            setAnimating(false);
            this.mAppWindowAnimating = false;
            if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                Slog.i(TAG, "!!! animate: entry time=" + this.mCurrentTime);
            }
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                Slog.i(TAG, ">>> OPEN TRANSACTION animateLocked");
            }
            SurfaceControl.openTransaction();
            if (this.mService.blockSurfaceFlinger) {
                Slog.i(TAG, ">>> OPEN TRANSACTION animateLocked skip");
            } else {
                SurfaceControl.setAnimationTransaction();
            }
            try {
                int i;
                int displayId;
                ScreenRotationAnimation screenRotationAnimation;
                this.mDisplayed = false;
                numDisplays = this.mDisplayContentsAnimators.size();
                for (i = 0; i < numDisplays; i++) {
                    displayId = this.mDisplayContentsAnimators.keyAt(i);
                    updateAppWindowsLocked(displayId);
                    DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.valueAt(i);
                    screenRotationAnimation = displayAnimator.mScreenRotationAnimation;
                    if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                        if (screenRotationAnimation.stepAnimationLocked(this.mCurrentTime)) {
                            setAnimating(true);
                        } else {
                            this.mBulkUpdateParams |= 1;
                            screenRotationAnimation.kill();
                            displayAnimator.mScreenRotationAnimation = null;
                            if (this.mService.mAccessibilityController != null && displayId == 0) {
                                this.mService.mAccessibilityController.onRotationChangedLocked(this.mService.getDefaultDisplayContentLocked(), this.mService.mRotation);
                            }
                        }
                    }
                    updateWindowsLocked(displayId);
                    updateWallpaperLocked(displayId);
                    WindowList windows = this.mService.getWindowListLocked(displayId);
                    int N = windows.size();
                    for (int j = 0; j < N; j++) {
                        ((WindowState) windows.get(j)).mWinAnimator.prepareSurfaceLocked(true);
                    }
                    if (!(this.mDisplayed || this.mService.isWindowsFreezingScreenTimeout())) {
                        this.mBulkUpdateParams &= -9;
                    }
                }
                for (i = 0; i < numDisplays; i++) {
                    displayId = this.mDisplayContentsAnimators.keyAt(i);
                    testTokenMayBeDrawnLocked(displayId);
                    screenRotationAnimation = ((DisplayContentsAnimator) this.mDisplayContentsAnimators.valueAt(i)).mScreenRotationAnimation;
                    if (screenRotationAnimation != null) {
                        screenRotationAnimation.updateSurfacesInTransaction();
                    }
                    orAnimating(this.mService.getDisplayContentLocked(displayId).animateDimLayers());
                    orAnimating(this.mService.getDisplayContentLocked(displayId).getDockedDividerController().animate(this.mCurrentTime));
                    if (this.mService.mAccessibilityController != null && displayId == 0 && (this.mService.mDisplayMagnificationEnabled || (!this.mService.mDisplayMagnificationEnabled && this.mService.mMagnificationBorderDisappearCnt > 0))) {
                        if (this.mService.mMagnificationBorderDisappearCnt > 0) {
                            WindowManagerService windowManagerService = this.mService;
                            windowManagerService.mMagnificationBorderDisappearCnt--;
                        }
                        this.mService.mAccessibilityController.drawMagnifiedRegionBorderIfNeededLocked();
                    }
                }
                if (this.mService.mDragState != null) {
                    this.mAnimating |= this.mService.mDragState.stepAnimationLocked(this.mCurrentTime);
                }
                if (this.mAnimating) {
                    this.mService.scheduleAnimationLocked();
                }
                if (this.mService.mWatermark != null) {
                    this.mService.mWatermark.drawIfNeeded();
                }
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION animateLocked begin");
                }
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION animateLocked end");
                }
            } catch (RuntimeException e) {
                Slog.d(TAG, "Unhandled exception in Window Manager", e);
                hasPendingLayoutChanges = false;
                numDisplays = this.mService.mDisplayContents.size();
                while (displayNdx < numDisplays) {
                }
                doRequest = false;
                if (this.mBulkUpdateParams != 0) {
                }
                this.mWindowPlacerLocked.requestTraversal();
                Trace.asyncTraceBegin(32, "animating", 0);
                this.mWindowPlacerLocked.requestTraversal();
                if (Trace.isTagEnabled(32)) {
                }
                if (this.mRemoveReplacedWindows) {
                }
                this.mService.stopUsingSavedSurfaceLocked();
                this.mService.destroyPreservedSurfaceLocked();
                this.mService.mWindowPlacerLocked.destroyPendingSurfaces();
                if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                }
            } finally {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION animateLocked begin");
                }
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION animateLocked end");
                }
            }
            hasPendingLayoutChanges = false;
            numDisplays = this.mService.mDisplayContents.size();
            for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                int pendingChanges = getPendingLayoutChanges(((DisplayContent) this.mService.mDisplayContents.valueAt(displayNdx)).getDisplayId());
                if ((pendingChanges & 4) != 0) {
                    this.mBulkUpdateParams |= 32;
                }
                if (pendingChanges != 0) {
                    hasPendingLayoutChanges = true;
                }
            }
            doRequest = false;
            if (this.mBulkUpdateParams != 0) {
                doRequest = this.mWindowPlacerLocked.copyAnimToLayoutParamsLocked();
            }
            if (hasPendingLayoutChanges || doRequest) {
                this.mWindowPlacerLocked.requestTraversal();
            }
            if (this.mAnimating && !wasAnimating && Trace.isTagEnabled(32)) {
                Trace.asyncTraceBegin(32, "animating", 0);
            }
            if (!this.mAnimating && wasAnimating) {
                this.mWindowPlacerLocked.requestTraversal();
                if (Trace.isTagEnabled(32)) {
                    Trace.asyncTraceEnd(32, "animating", 0);
                }
            }
            if (this.mRemoveReplacedWindows) {
                removeReplacedWindowsLocked();
            }
            this.mService.stopUsingSavedSurfaceLocked();
            this.mService.destroyPreservedSurfaceLocked();
            this.mService.mWindowPlacerLocked.destroyPendingSurfaces();
            if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                Slog.i(TAG, "!!! animate: exit mAnimating=" + this.mAnimating + " mBulkUpdateParams=" + Integer.toHexString(this.mBulkUpdateParams) + " mPendingLayoutChanges(DEFAULT_DISPLAY)=" + Integer.toHexString(getPendingLayoutChanges(0)));
            }
        }
    }

    private void removeReplacedWindowsLocked() {
        boolean z = false;
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            Slog.i(TAG, ">>> OPEN TRANSACTION removeReplacedWindows");
        }
        SurfaceControl.openTransaction();
        try {
            for (int i = this.mService.mDisplayContents.size() - 1; i >= 0; i--) {
                WindowList windows = this.mService.getWindowListLocked(((DisplayContent) this.mService.mDisplayContents.valueAt(i)).getDisplayId());
                for (int j = windows.size() - 1; j >= 0; j--) {
                    if (j < windows.size()) {
                        ((WindowState) windows.get(j)).maybeRemoveReplacedWindow();
                    }
                }
            }
            this.mRemoveReplacedWindows = z;
        } finally {
            SurfaceControl.closeTransaction();
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                z = "<<< CLOSE TRANSACTION removeReplacedWindows";
                Slog.i(TAG, z);
            }
        }
    }

    private static String bulkUpdateParamsToString(int bulkUpdateParams) {
        StringBuilder builder = new StringBuilder(128);
        if ((bulkUpdateParams & 1) != 0) {
            builder.append(" UPDATE_ROTATION");
        }
        if ((bulkUpdateParams & 2) != 0) {
            builder.append(" WALLPAPER_MAY_CHANGE");
        }
        if ((bulkUpdateParams & 4) != 0) {
            builder.append(" FORCE_HIDING_CHANGED");
        }
        if ((bulkUpdateParams & 8) != 0) {
            builder.append(" ORIENTATION_CHANGE_COMPLETE");
        }
        if ((bulkUpdateParams & 16) != 0) {
            builder.append(" TURN_ON_SCREEN");
        }
        return builder.toString();
    }

    public void dumpLocked(PrintWriter pw, String prefix, boolean dumpAll) {
        String subPrefix = "  " + prefix;
        String subSubPrefix = "  " + subPrefix;
        for (int i = 0; i < this.mDisplayContentsAnimators.size(); i++) {
            pw.print(prefix);
            pw.print("DisplayContentsAnimator #");
            pw.print(this.mDisplayContentsAnimators.keyAt(i));
            pw.println(":");
            DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.valueAt(i);
            WindowList windows = this.mService.getWindowListLocked(this.mDisplayContentsAnimators.keyAt(i));
            int N = windows.size();
            for (int j = 0; j < N; j++) {
                WindowStateAnimator wanim = ((WindowState) windows.get(j)).mWinAnimator;
                pw.print(subPrefix);
                pw.print("Window #");
                pw.print(j);
                pw.print(": ");
                pw.println(wanim);
            }
            if (displayAnimator.mScreenRotationAnimation != null) {
                pw.print(subPrefix);
                pw.println("mScreenRotationAnimation:");
                displayAnimator.mScreenRotationAnimation.printTo(subSubPrefix, pw);
            } else if (dumpAll) {
                pw.print(subPrefix);
                pw.println("no ScreenRotationAnimation ");
            }
            pw.println();
        }
        pw.println();
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mAnimTransactionSequence=");
            pw.print(this.mAnimTransactionSequence);
            pw.print(" mForceHiding=");
            pw.println(forceHidingToString());
            pw.print(prefix);
            pw.print("mCurrentTime=");
            pw.println(TimeUtils.formatUptime(this.mCurrentTime));
        }
        if (this.mBulkUpdateParams != 0) {
            pw.print(prefix);
            pw.print("mBulkUpdateParams=0x");
            pw.print(Integer.toHexString(this.mBulkUpdateParams));
            pw.println(bulkUpdateParamsToString(this.mBulkUpdateParams));
        }
        if (this.mWindowDetachedWallpaper != null) {
            pw.print(prefix);
            pw.print("mWindowDetachedWallpaper=");
            pw.println(this.mWindowDetachedWallpaper);
        }
    }

    int getPendingLayoutChanges(int displayId) {
        int i = 0;
        if (displayId < 0) {
            return 0;
        }
        DisplayContent displayContent = this.mService.getDisplayContentLocked(displayId);
        if (displayContent != null) {
            i = displayContent.pendingLayoutChanges;
        }
        return i;
    }

    void setPendingLayoutChanges(int displayId, int changes) {
        if (displayId >= 0) {
            DisplayContent displayContent = this.mService.getDisplayContentLocked(displayId);
            if (displayContent != null) {
                displayContent.pendingLayoutChanges |= changes;
            }
        }
    }

    void setAppLayoutChanges(AppWindowAnimator appAnimator, int changes, String reason, int displayId) {
        WindowList windows = appAnimator.mAppToken.allAppWindows;
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (displayId == ((WindowState) windows.get(i)).getDisplayId()) {
                setPendingLayoutChanges(displayId, changes);
                if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                    this.mWindowPlacerLocked.debugLayoutRepeats(reason, getPendingLayoutChanges(displayId));
                    return;
                }
                return;
            }
        }
    }

    private DisplayContentsAnimator getDisplayContentsAnimatorLocked(int displayId) {
        DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.get(displayId);
        if (displayAnimator != null) {
            return displayAnimator;
        }
        displayAnimator = new DisplayContentsAnimator(this, null);
        this.mDisplayContentsAnimators.put(displayId, displayAnimator);
        return displayAnimator;
    }

    void setScreenRotationAnimationLocked(int displayId, ScreenRotationAnimation animation) {
        if (displayId >= 0) {
            getDisplayContentsAnimatorLocked(displayId).mScreenRotationAnimation = animation;
        }
    }

    ScreenRotationAnimation getScreenRotationAnimationLocked(int displayId) {
        if (displayId < 0) {
            return null;
        }
        return getDisplayContentsAnimatorLocked(displayId).mScreenRotationAnimation;
    }

    void requestRemovalOfReplacedWindows(WindowState win) {
        this.mRemoveReplacedWindows = true;
    }

    boolean isAnimating() {
        return this.mAnimating;
    }

    void setAnimating(boolean animating) {
        this.mAnimating = animating;
    }

    void orAnimating(boolean animating) {
        this.mAnimating |= animating;
    }
}
