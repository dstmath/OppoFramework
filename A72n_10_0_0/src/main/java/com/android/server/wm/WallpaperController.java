package com.android.server.wm;

import android.app.ActivityThread;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.Animation;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Predicate;

/* access modifiers changed from: package-private */
public class WallpaperController {
    private static final String TAG = "WindowManager";
    private static final int WALLPAPER_DRAW_NORMAL = 0;
    private static final int WALLPAPER_DRAW_PENDING = 1;
    private static final long WALLPAPER_DRAW_PENDING_TIMEOUT_DURATION = 500;
    private static final int WALLPAPER_DRAW_TIMEOUT = 2;
    private static final long WALLPAPER_TIMEOUT = 150;
    private static final long WALLPAPER_TIMEOUT_RECOVERY = 10000;
    WindowState mDeferredHideWallpaper = null;
    private final DisplayContent mDisplayContent;
    private final FindWallpaperTargetResult mFindResults = new FindWallpaperTargetResult();
    private final ToBooleanFunction<WindowState> mFindWallpaperTargetFunction = new ToBooleanFunction() {
        /* class com.android.server.wm.$$Lambda$WallpaperController$6pruPGLeSJAwNl9vGfC87eso21w */

        public final boolean apply(Object obj) {
            return WallpaperController.this.lambda$new$0$WallpaperController((WindowState) obj);
        }
    };
    private int mLastWallpaperDisplayOffsetX = Integer.MIN_VALUE;
    private int mLastWallpaperDisplayOffsetY = Integer.MIN_VALUE;
    private long mLastWallpaperTimeoutTime;
    private float mLastWallpaperX = -1.0f;
    private float mLastWallpaperXStep = -1.0f;
    private float mLastWallpaperY = -1.0f;
    private float mLastWallpaperYStep = -1.0f;
    private WindowState mPrevWallpaperTarget = null;
    private WindowManagerService mService;
    private WindowState mTmpTopWallpaper;
    private WindowState mWaitingOnWallpaper;
    private int mWallpaperDrawState = 0;
    private WindowState mWallpaperTarget = null;
    private final ArrayList<WallpaperWindowToken> mWallpaperTokens = new ArrayList<>();

    public /* synthetic */ boolean lambda$new$0$WallpaperController(WindowState w) {
        WindowAnimator windowAnimator = this.mService.mAnimator;
        if (w.mAttrs.type == 2013) {
            if (this.mFindResults.topWallpaper == null || this.mFindResults.resetTopWallpaper) {
                this.mFindResults.setTopWallpaper(w);
                this.mFindResults.resetTopWallpaper = false;
            }
            return false;
        }
        this.mFindResults.resetTopWallpaper = true;
        if (w.mAppToken == null || !w.mAppToken.isHidden() || w.mAppToken.isSelfAnimating()) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                Slog.v("WindowManager", "Win " + w + ": isOnScreen=" + w.isOnScreen() + " mDrawState=" + w.mWinAnimator.mDrawState);
            }
            if (w.mWillReplaceWindow && this.mWallpaperTarget == null && !this.mFindResults.useTopWallpaperAsTarget) {
                this.mFindResults.setUseTopWallpaperAsTarget(true);
            }
            boolean keyguardGoingAwayWithWallpaper = w.mAppToken != null && w.mAppToken.isSelfAnimating() && AppTransition.isKeyguardGoingAwayTransit(w.mAppToken.getTransit()) && (w.mAppToken.getTransitFlags() & 4) != 0;
            boolean needsShowWhenLockedWallpaper = false;
            if ((w.mAttrs.flags & DumpState.DUMP_FROZEN) != 0 && this.mService.mPolicy.isKeyguardLocked() && this.mService.mPolicy.isKeyguardOccluded()) {
                needsShowWhenLockedWallpaper = !isFullscreen(w.mAttrs) || (w.mAppToken != null && !w.mAppToken.fillsParent());
            }
            if (keyguardGoingAwayWithWallpaper || needsShowWhenLockedWallpaper) {
                this.mFindResults.setUseTopWallpaperAsTarget(true);
            }
            RecentsAnimationController recentsAnimationController = this.mService.getRecentsAnimationController();
            boolean adjustNotShowWallpaperByOppo = false;
            try {
                if (!(w.mAppToken == null || w.mAppToken.appToken == null || w.mAppToken.appToken.getName() == null)) {
                    adjustNotShowWallpaperByOppo = ActivityThread.inCptWhiteList((int) CompatibilityHelper.FORCE_NOT_SHOW_WALLPAPER_WHEN_TASK_TRANSITION, w.mAppToken.appToken.getName());
                    Slog.v("WindowManager", "adjustNotShowWallpaperByOppo: " + adjustNotShowWallpaperByOppo);
                }
            } catch (RemoteException e) {
                Slog.e("WindowManager", "w.mAppToken.appToken.getName() RemoteException.");
            }
            boolean hasWallpaper = (w.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0 || (w.mAppToken != null && w.mAppToken.getAnimation() != null && w.mAppToken.getAnimation().getShowWallpaper() && !adjustNotShowWallpaperByOppo);
            if (recentsAnimationController != null && recentsAnimationController.isWallpaperVisible(w)) {
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    Slog.v("WindowManager", "Found recents animation wallpaper target: " + w);
                }
                this.mFindResults.setWallpaperTarget(w);
                return true;
            } else if (!hasWallpaper || !w.isOnScreen() || (this.mWallpaperTarget != w && !w.isDrawFinishedLw())) {
                return false;
            } else {
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    Slog.v("WindowManager", "Found wallpaper target: " + w);
                }
                this.mFindResults.setWallpaperTarget(w);
                if (w == this.mWallpaperTarget && w.isAnimating() && WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    Slog.v("WindowManager", "Win " + w + ": token animating, looking behind.");
                }
                return true;
            }
        } else {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                Slog.v("WindowManager", "Skipping hidden and not animating token: " + w);
            }
            return false;
        }
    }

    WallpaperController(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
    }

    /* access modifiers changed from: package-private */
    public WindowState getWallpaperTarget() {
        return this.mWallpaperTarget;
    }

    /* access modifiers changed from: package-private */
    public boolean isWallpaperTarget(WindowState win) {
        return win == this.mWallpaperTarget;
    }

    /* access modifiers changed from: package-private */
    public boolean isBelowWallpaperTarget(WindowState win) {
        WindowState windowState = this.mWallpaperTarget;
        return windowState != null && windowState.mLayer >= win.mBaseLayer;
    }

    /* access modifiers changed from: package-private */
    public boolean isWallpaperVisible() {
        return isWallpaperVisible(this.mWallpaperTarget);
    }

    /* access modifiers changed from: package-private */
    public void startWallpaperAnimation(Animation a) {
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            this.mWallpaperTokens.get(curTokenNdx).startAnimation(a);
        }
    }

    private final boolean isWallpaperVisible(WindowState wallpaperTarget) {
        RecentsAnimationController recentsAnimationController = this.mService.getRecentsAnimationController();
        boolean isAnimatingWithRecentsComponent = recentsAnimationController != null && recentsAnimationController.isWallpaperVisible(wallpaperTarget);
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
            StringBuilder sb = new StringBuilder();
            sb.append("Wallpaper vis: target ");
            sb.append(wallpaperTarget);
            sb.append(", obscured=");
            sb.append(wallpaperTarget != null ? Boolean.toString(wallpaperTarget.mObscured) : "??");
            sb.append(" animating=");
            sb.append((wallpaperTarget == null || wallpaperTarget.mAppToken == null) ? null : Boolean.valueOf(wallpaperTarget.mAppToken.isSelfAnimating()));
            sb.append(" prev=");
            sb.append(this.mPrevWallpaperTarget);
            sb.append(" recentsAnimationWallpaperVisible=");
            sb.append(isAnimatingWithRecentsComponent);
            Slog.v("WindowManager", sb.toString());
        }
        if (wallpaperTarget != null) {
            if (!wallpaperTarget.mObscured || isAnimatingWithRecentsComponent) {
                return true;
            }
            if (wallpaperTarget.mAppToken != null && wallpaperTarget.mAppToken.isSelfAnimating()) {
                return true;
            }
        }
        return this.mPrevWallpaperTarget != null;
    }

    /* access modifiers changed from: package-private */
    public boolean isWallpaperTargetAnimating() {
        WindowState windowState = this.mWallpaperTarget;
        return windowState != null && windowState.isAnimating() && (this.mWallpaperTarget.mAppToken == null || !this.mWallpaperTarget.mAppToken.isWaitingForTransitionStart());
    }

    /* access modifiers changed from: package-private */
    public void updateWallpaperVisibility() {
        boolean visible = isWallpaperVisible(this.mWallpaperTarget);
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            this.mWallpaperTokens.get(curTokenNdx).updateWallpaperVisibility(visible);
        }
    }

    /* access modifiers changed from: package-private */
    public void hideDeferredWallpapersIfNeeded() {
        WindowState windowState = this.mDeferredHideWallpaper;
        if (windowState != null) {
            hideWallpapers(windowState);
            this.mDeferredHideWallpaper = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void hideWallpapers(WindowState winGoingAway) {
        WindowState windowState = this.mWallpaperTarget;
        if (windowState == null || (windowState == winGoingAway && this.mPrevWallpaperTarget == null)) {
            WindowState windowState2 = this.mWallpaperTarget;
            if (windowState2 == null || !windowState2.getDisplayContent().mAppTransition.isRunning()) {
                boolean wasDeferred = this.mDeferredHideWallpaper == winGoingAway;
                for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
                    WallpaperWindowToken token = this.mWallpaperTokens.get(i);
                    token.hideWallpaperToken(wasDeferred, "hideWallpapers");
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT && !token.isHidden()) {
                        Slog.d("WindowManager", "Hiding wallpaper " + token + " from " + winGoingAway + " target=" + this.mWallpaperTarget + " prev=" + this.mPrevWallpaperTarget + StringUtils.LF + Debug.getCallers(5, "  "));
                    }
                }
                return;
            }
            this.mDeferredHideWallpaper = winGoingAway;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateWallpaperOffset(WindowState wallpaperWin, int dw, int dh, boolean sync) {
        float wpy;
        int offset;
        int offset2;
        boolean rawChanged;
        boolean rawChanged2 = false;
        float defaultWallpaperX = wallpaperWin.isRtl() ? 1.0f : 0.0f;
        float wpx = this.mLastWallpaperX;
        if (wpx < OppoBrightUtils.MIN_LUX_LIMITI) {
            wpx = defaultWallpaperX;
        }
        float wpxs = this.mLastWallpaperXStep;
        if (wpxs < OppoBrightUtils.MIN_LUX_LIMITI) {
            wpxs = -1.0f;
        }
        int availw = (wallpaperWin.getFrameLw().right - wallpaperWin.getFrameLw().left) - dw;
        int offset3 = availw > 0 ? -((int) ((((float) availw) * wpx) + 0.5f)) : 0;
        int i = this.mLastWallpaperDisplayOffsetX;
        if (i != Integer.MIN_VALUE) {
            offset3 += i;
        }
        if (!(wallpaperWin.mWallpaperX == wpx && wallpaperWin.mWallpaperXStep == wpxs)) {
            wallpaperWin.mWallpaperX = wpx;
            wallpaperWin.mWallpaperXStep = wpxs;
            rawChanged2 = true;
        }
        float wpy2 = this.mLastWallpaperY;
        if (wpy2 < OppoBrightUtils.MIN_LUX_LIMITI) {
            wpy2 = 0.5f;
        }
        float wpys = this.mLastWallpaperYStep;
        if (wpys < OppoBrightUtils.MIN_LUX_LIMITI) {
            wpys = -1.0f;
        }
        int availh = (wallpaperWin.getFrameLw().bottom - wallpaperWin.getFrameLw().top) - dh;
        if (availh > 0) {
            offset = -((int) ((((float) availh) * wpy2) + 0.5f));
            wpy = wpy2;
        } else {
            wpy = wpy2;
            offset = 0;
        }
        int i2 = this.mLastWallpaperDisplayOffsetY;
        if (i2 != Integer.MIN_VALUE) {
            offset2 = offset + i2;
        } else {
            offset2 = offset;
        }
        if (wallpaperWin.mWallpaperY == wpy && wallpaperWin.mWallpaperYStep == wpys) {
            rawChanged = rawChanged2;
        } else {
            wallpaperWin.mWallpaperY = wpy;
            wallpaperWin.mWallpaperYStep = wpys;
            rawChanged = true;
        }
        boolean changed = wallpaperWin.mWinAnimator.setWallpaperOffset(offset3, offset2);
        if (rawChanged && (wallpaperWin.mAttrs.privateFlags & 4) != 0) {
            try {
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    try {
                        Slog.v("WindowManager", "Report new wp offset " + wallpaperWin + " x=" + wallpaperWin.mWallpaperX + " y=" + wallpaperWin.mWallpaperY);
                    } catch (RemoteException e) {
                    }
                }
                if (sync) {
                    this.mWaitingOnWallpaper = wallpaperWin;
                }
                try {
                    try {
                        try {
                            wallpaperWin.mClient.dispatchWallpaperOffsets(wallpaperWin.mWallpaperX, wallpaperWin.mWallpaperY, wallpaperWin.mWallpaperXStep, wallpaperWin.mWallpaperYStep, sync);
                            if (sync && this.mWaitingOnWallpaper != null) {
                                long start = SystemClock.uptimeMillis();
                                if (this.mLastWallpaperTimeoutTime + 10000 < start) {
                                    try {
                                        if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                                            Slog.v("WindowManager", "Waiting for offset complete...");
                                        }
                                        this.mService.mGlobalLock.wait(WALLPAPER_TIMEOUT);
                                    } catch (InterruptedException e2) {
                                    }
                                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                                        Slog.v("WindowManager", "Offset complete!");
                                    }
                                    if (WALLPAPER_TIMEOUT + start < SystemClock.uptimeMillis()) {
                                        Slog.i("WindowManager", "Timeout waiting for wallpaper to offset: " + wallpaperWin);
                                        this.mLastWallpaperTimeoutTime = start;
                                    }
                                }
                                this.mWaitingOnWallpaper = null;
                            }
                        } catch (RemoteException e3) {
                        }
                    } catch (RemoteException e4) {
                    }
                } catch (RemoteException e5) {
                }
            } catch (RemoteException e6) {
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public void setWindowWallpaperPosition(WindowState window, float x, float y, float xStep, float yStep) {
        if (window.mWallpaperX != x || window.mWallpaperY != y) {
            window.mWallpaperX = x;
            window.mWallpaperY = y;
            window.mWallpaperXStep = xStep;
            window.mWallpaperYStep = yStep;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindowWallpaperDisplayOffset(WindowState window, int x, int y) {
        if (window.mWallpaperDisplayOffsetX != x || window.mWallpaperDisplayOffsetY != y) {
            window.mWallpaperDisplayOffsetX = x;
            window.mWallpaperDisplayOffsetY = y;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    /* access modifiers changed from: package-private */
    public Bundle sendWindowWallpaperCommand(WindowState window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (!(window == this.mWallpaperTarget || window == this.mPrevWallpaperTarget)) {
            return null;
        }
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            this.mWallpaperTokens.get(curTokenNdx).sendWindowWallpaperCommand(action, x, y, z, extras, sync);
        }
        return null;
    }

    private void updateWallpaperOffsetLocked(WindowState changingTarget, boolean sync) {
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        WindowState target = this.mWallpaperTarget;
        if (target != null) {
            if (target.mWallpaperX >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperX = target.mWallpaperX;
            } else if (changingTarget.mWallpaperX >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperX = changingTarget.mWallpaperX;
            }
            if (target.mWallpaperY >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperY = target.mWallpaperY;
            } else if (changingTarget.mWallpaperY >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperY = changingTarget.mWallpaperY;
            }
            if (target.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetX = target.mWallpaperDisplayOffsetX;
            } else if (changingTarget.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetX = changingTarget.mWallpaperDisplayOffsetX;
            }
            if (target.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetY = target.mWallpaperDisplayOffsetY;
            } else if (changingTarget.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetY = changingTarget.mWallpaperDisplayOffsetY;
            }
            if (target.mWallpaperXStep >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperXStep = target.mWallpaperXStep;
            } else if (changingTarget.mWallpaperXStep >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperXStep = changingTarget.mWallpaperXStep;
            }
            if (target.mWallpaperYStep >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperYStep = target.mWallpaperYStep;
            } else if (changingTarget.mWallpaperYStep >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperYStep = changingTarget.mWallpaperYStep;
            }
        }
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            this.mWallpaperTokens.get(curTokenNdx).updateWallpaperOffset(dw, dh, sync);
        }
    }

    /* access modifiers changed from: package-private */
    public void clearLastWallpaperTimeoutTime() {
        this.mLastWallpaperTimeoutTime = 0;
    }

    /* access modifiers changed from: package-private */
    public void wallpaperCommandComplete(IBinder window) {
        WindowState windowState = this.mWaitingOnWallpaper;
        if (windowState != null && windowState.mClient.asBinder() == window) {
            this.mWaitingOnWallpaper = null;
            this.mService.mGlobalLock.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void wallpaperOffsetsComplete(IBinder window) {
        WindowState windowState = this.mWaitingOnWallpaper;
        if (windowState != null && windowState.mClient.asBinder() == window) {
            this.mWaitingOnWallpaper = null;
            this.mService.mGlobalLock.notifyAll();
        }
    }

    private void findWallpaperTarget() {
        this.mFindResults.reset();
        if (this.mDisplayContent.isStackVisible(5)) {
            this.mFindResults.setUseTopWallpaperAsTarget(true);
        }
        this.mDisplayContent.forAllWindows(this.mFindWallpaperTargetFunction, true);
        if (this.mFindResults.wallpaperTarget == null && this.mFindResults.useTopWallpaperAsTarget) {
            FindWallpaperTargetResult findWallpaperTargetResult = this.mFindResults;
            findWallpaperTargetResult.setWallpaperTarget(findWallpaperTargetResult.topWallpaper);
        }
    }

    private boolean isFullscreen(WindowManager.LayoutParams attrs) {
        return attrs.x == 0 && attrs.y == 0 && attrs.width == -1 && attrs.height == -1;
    }

    private void updateWallpaperWindowsTarget(FindWallpaperTargetResult result) {
        WindowState windowState;
        WindowState wallpaperTarget = result.wallpaperTarget;
        if (this.mWallpaperTarget == wallpaperTarget || ((windowState = this.mPrevWallpaperTarget) != null && windowState == wallpaperTarget)) {
            WindowState prevWallpaperTarget = this.mPrevWallpaperTarget;
            if (prevWallpaperTarget != null && !prevWallpaperTarget.isAnimatingLw()) {
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.v("WindowManager", "No longer animating wallpaper targets!");
                }
                this.mPrevWallpaperTarget = null;
                this.mWallpaperTarget = wallpaperTarget;
                return;
            }
            return;
        }
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
            Slog.v("WindowManager", "New wallpaper target: " + wallpaperTarget + " prevTarget: " + this.mWallpaperTarget);
        }
        this.mPrevWallpaperTarget = null;
        WindowState prevWallpaperTarget2 = this.mWallpaperTarget;
        this.mWallpaperTarget = wallpaperTarget;
        if (wallpaperTarget != null && prevWallpaperTarget2 != null) {
            boolean oldAnim = prevWallpaperTarget2.isAnimatingLw();
            boolean foundAnim = wallpaperTarget.isAnimatingLw();
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                Slog.v("WindowManager", "New animation: " + foundAnim + " old animation: " + oldAnim);
            }
            if (foundAnim && oldAnim && this.mDisplayContent.getWindow(new Predicate() {
                /* class com.android.server.wm.$$Lambda$WallpaperController$Gy7houdzET4VmpY0QJ2vNX1b7k */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return WallpaperController.lambda$updateWallpaperWindowsTarget$1(WindowState.this, (WindowState) obj);
                }
            }) != null) {
                boolean oldTargetHidden = true;
                boolean newTargetHidden = wallpaperTarget.mAppToken != null && wallpaperTarget.mAppToken.hiddenRequested;
                if (prevWallpaperTarget2.mAppToken == null || !prevWallpaperTarget2.mAppToken.hiddenRequested) {
                    oldTargetHidden = false;
                }
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.v("WindowManager", "Animating wallpapers: old: " + prevWallpaperTarget2 + " hidden=" + oldTargetHidden + " new: " + wallpaperTarget + " hidden=" + newTargetHidden);
                }
                this.mPrevWallpaperTarget = prevWallpaperTarget2;
                if (newTargetHidden && !oldTargetHidden) {
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                        Slog.v("WindowManager", "Old wallpaper still the target.");
                    }
                    this.mWallpaperTarget = prevWallpaperTarget2;
                } else if (newTargetHidden == oldTargetHidden && !this.mDisplayContent.mOpeningApps.contains(wallpaperTarget.mAppToken) && (this.mDisplayContent.mOpeningApps.contains(prevWallpaperTarget2.mAppToken) || this.mDisplayContent.mClosingApps.contains(prevWallpaperTarget2.mAppToken))) {
                    this.mWallpaperTarget = prevWallpaperTarget2;
                }
                result.setWallpaperTarget(wallpaperTarget);
            }
        }
    }

    static /* synthetic */ boolean lambda$updateWallpaperWindowsTarget$1(WindowState prevWallpaperTarget, WindowState w) {
        return w == prevWallpaperTarget;
    }

    private void updateWallpaperTokens(boolean visible) {
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            WallpaperWindowToken token = this.mWallpaperTokens.get(curTokenNdx);
            token.updateWallpaperWindows(visible);
            token.getDisplayContent().assignWindowLayers(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void adjustWallpaperWindows() {
        boolean visible = false;
        this.mDisplayContent.mWallpaperMayChange = false;
        findWallpaperTarget();
        updateWallpaperWindowsTarget(this.mFindResults);
        WindowState windowState = this.mWallpaperTarget;
        if (windowState != null && isWallpaperVisible(windowState)) {
            visible = true;
        }
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
            Slog.v("WindowManager", "Wallpaper visibility: " + visible + " at display " + this.mDisplayContent.getDisplayId());
        }
        if (visible) {
            if (this.mWallpaperTarget.mWallpaperX >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperX = this.mWallpaperTarget.mWallpaperX;
                this.mLastWallpaperXStep = this.mWallpaperTarget.mWallpaperXStep;
            }
            if (this.mWallpaperTarget.mWallpaperY >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperY = this.mWallpaperTarget.mWallpaperY;
                this.mLastWallpaperYStep = this.mWallpaperTarget.mWallpaperYStep;
            }
            if (this.mWallpaperTarget.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetX = this.mWallpaperTarget.mWallpaperDisplayOffsetX;
            }
            if (this.mWallpaperTarget.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetY = this.mWallpaperTarget.mWallpaperDisplayOffsetY;
            }
        }
        updateWallpaperTokens(visible);
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
            Slog.d("WindowManager", "New wallpaper: target=" + this.mWallpaperTarget + " prev=" + this.mPrevWallpaperTarget);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean processWallpaperDrawPendingTimeout() {
        if (this.mWallpaperDrawState != 1) {
            return false;
        }
        this.mWallpaperDrawState = 2;
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_WALLPAPER) {
            Slog.v("WindowManager", "*** WALLPAPER DRAW TIMEOUT");
        }
        if (this.mService.getRecentsAnimationController() != null) {
            this.mService.getRecentsAnimationController().startAnimation();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean wallpaperTransitionReady() {
        boolean transitionReady = true;
        boolean wallpaperReady = true;
        int curTokenIndex = this.mWallpaperTokens.size() - 1;
        while (true) {
            if (curTokenIndex < 0 || 1 == 0) {
                break;
            } else if (this.mWallpaperTokens.get(curTokenIndex).hasVisibleNotDrawnWallpaper()) {
                wallpaperReady = false;
                if (this.mWallpaperDrawState != 2) {
                    transitionReady = false;
                }
                if (this.mWallpaperDrawState == 0) {
                    this.mWallpaperDrawState = 1;
                    this.mService.mH.removeMessages(39, this);
                    this.mService.mH.sendMessageDelayed(this.mService.mH.obtainMessage(39, this), 500);
                }
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    Slog.v("WindowManager", "Wallpaper should be visible but has not been drawn yet. mWallpaperDrawState=" + this.mWallpaperDrawState);
                }
            } else {
                curTokenIndex--;
            }
        }
        if (wallpaperReady) {
            this.mWallpaperDrawState = 0;
            this.mService.mH.removeMessages(39, this);
        }
        return transitionReady;
    }

    /* access modifiers changed from: package-private */
    public void adjustWallpaperWindowsForAppTransitionIfNeeded(ArraySet<AppWindowToken> openingApps, ArraySet<AppWindowToken> changingApps) {
        boolean adjust = false;
        if ((this.mDisplayContent.pendingLayoutChanges & 4) != 0) {
            adjust = true;
        } else {
            int i = openingApps.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (openingApps.valueAt(i).windowsCanBeWallpaperTarget()) {
                    adjust = true;
                    break;
                } else {
                    i--;
                }
            }
            if (!adjust) {
                int i2 = changingApps.size() - 1;
                while (true) {
                    if (i2 < 0) {
                        break;
                    } else if (changingApps.valueAt(i2).windowsCanBeWallpaperTarget()) {
                        adjust = true;
                        break;
                    } else {
                        i2--;
                    }
                }
            }
        }
        if (adjust) {
            adjustWallpaperWindows();
        }
    }

    /* access modifiers changed from: package-private */
    public void addWallpaperToken(WallpaperWindowToken token) {
        this.mWallpaperTokens.add(token);
    }

    /* access modifiers changed from: package-private */
    public void removeWallpaperToken(WallpaperWindowToken token) {
        this.mWallpaperTokens.remove(token);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean canScreenshotWallpaper() {
        return canScreenshotWallpaper(getTopVisibleWallpaper());
    }

    private boolean canScreenshotWallpaper(WindowState wallpaperWindowState) {
        if (!this.mService.mPolicy.isScreenOn()) {
            if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                Slog.i("WindowManager", "Attempted to take screenshot while display was off.");
            }
            return false;
        } else if (wallpaperWindowState != null) {
            return true;
        } else {
            if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                Slog.i("WindowManager", "No visible wallpaper to screenshot");
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public Bitmap screenshotWallpaperLocked() {
        WindowState wallpaperWindowState = getTopVisibleWallpaper();
        if (!canScreenshotWallpaper(wallpaperWindowState)) {
            return null;
        }
        Rect bounds = wallpaperWindowState.getBounds();
        bounds.offsetTo(0, 0);
        SurfaceControl.ScreenshotGraphicBuffer wallpaperBuffer = SurfaceControl.captureLayers(wallpaperWindowState.getSurfaceControl().getHandle(), bounds, 1.0f);
        if (wallpaperBuffer != null) {
            return Bitmap.wrapHardwareBuffer(wallpaperBuffer.getGraphicBuffer(), wallpaperBuffer.getColorSpace());
        }
        Slog.w("WindowManager", "Failed to screenshot wallpaper");
        return null;
    }

    private WindowState getTopVisibleWallpaper() {
        this.mTmpTopWallpaper = null;
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            this.mWallpaperTokens.get(curTokenNdx).forAllWindows((ToBooleanFunction<WindowState>) new ToBooleanFunction() {
                /* class com.android.server.wm.$$Lambda$WallpaperController$3kGUJhX6nW41Z26JaiCQelxXZr8 */

                public final boolean apply(Object obj) {
                    return WallpaperController.this.lambda$getTopVisibleWallpaper$2$WallpaperController((WindowState) obj);
                }
            }, true);
        }
        return this.mTmpTopWallpaper;
    }

    public /* synthetic */ boolean lambda$getTopVisibleWallpaper$2$WallpaperController(WindowState w) {
        WindowStateAnimator winAnim = w.mWinAnimator;
        if (winAnim == null || !winAnim.getShown() || winAnim.mLastAlpha <= OppoBrightUtils.MIN_LUX_LIMITI) {
            return false;
        }
        this.mTmpTopWallpaper = w;
        return true;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("displayId=");
        pw.println(this.mDisplayContent.getDisplayId());
        pw.print(prefix);
        pw.print("mWallpaperTarget=");
        pw.println(this.mWallpaperTarget);
        if (this.mPrevWallpaperTarget != null) {
            pw.print(prefix);
            pw.print("mPrevWallpaperTarget=");
            pw.println(this.mPrevWallpaperTarget);
        }
        pw.print(prefix);
        pw.print("mLastWallpaperX=");
        pw.print(this.mLastWallpaperX);
        pw.print(" mLastWallpaperY=");
        pw.println(this.mLastWallpaperY);
        if (this.mLastWallpaperDisplayOffsetX != Integer.MIN_VALUE || this.mLastWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
            pw.print(prefix);
            pw.print("mLastWallpaperDisplayOffsetX=");
            pw.print(this.mLastWallpaperDisplayOffsetX);
            pw.print(" mLastWallpaperDisplayOffsetY=");
            pw.println(this.mLastWallpaperDisplayOffsetY);
        }
    }

    /* access modifiers changed from: private */
    public static final class FindWallpaperTargetResult {
        boolean resetTopWallpaper;
        WindowState topWallpaper;
        boolean useTopWallpaperAsTarget;
        WindowState wallpaperTarget;

        private FindWallpaperTargetResult() {
            this.topWallpaper = null;
            this.useTopWallpaperAsTarget = false;
            this.wallpaperTarget = null;
            this.resetTopWallpaper = false;
        }

        /* access modifiers changed from: package-private */
        public void setTopWallpaper(WindowState win) {
            this.topWallpaper = win;
        }

        /* access modifiers changed from: package-private */
        public void setWallpaperTarget(WindowState win) {
            this.wallpaperTarget = win;
        }

        /* access modifiers changed from: package-private */
        public void setUseTopWallpaperAsTarget(boolean topWallpaperAsTarget) {
            this.useTopWallpaperAsTarget = topWallpaperAsTarget;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.topWallpaper = null;
            this.wallpaperTarget = null;
            this.useTopWallpaperAsTarget = false;
            this.resetTopWallpaper = false;
        }
    }
}
