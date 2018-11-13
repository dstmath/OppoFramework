package com.android.server.wm;

import android.app.ActivityThread;
import android.content.res.Configuration;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Debug;
import android.os.Trace;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import com.android.server.am.ActivityManagerService;
import com.android.server.pm.CompatibilityHelper;
import java.io.PrintWriter;
import java.util.ArrayList;

class WindowSurfacePlacer {
    static final int SET_FORCE_HIDING_CHANGED = 4;
    static final int SET_ORIENTATION_CHANGE_COMPLETE = 8;
    static final int SET_TURN_ON_SCREEN = 16;
    static final int SET_UPDATE_ROTATION = 1;
    static final int SET_WALLPAPER_ACTION_PENDING = 32;
    static final int SET_WALLPAPER_MAY_CHANGE = 2;
    private static final String TAG = "WindowManager";
    private int mDeferDepth = 0;
    private boolean mInLayout = false;
    boolean mLayoutCalled = false;
    private int mLayoutRepeatCount;
    private final ArrayList<SurfaceControl> mPendingDestroyingSurfaces = new ArrayList();
    private final Runnable mPerformSurfacePlacement;
    private final WindowManagerService mService;
    private final SparseIntArray mTempTransitionReasons = new SparseIntArray();
    private final Rect mTmpContentRect = new Rect();
    private final LayerAndToken mTmpLayerAndToken = new LayerAndToken();
    private final Rect mTmpStartRect = new Rect();
    private boolean mTraversalScheduled;
    private final WallpaperController mWallpaperControllerLocked;

    private static final class LayerAndToken {
        public int layer;
        public AppWindowToken token;

        /* synthetic */ LayerAndToken(LayerAndToken -this0) {
            this();
        }

        private LayerAndToken() {
        }
    }

    public WindowSurfacePlacer(WindowManagerService service) {
        this.mService = service;
        this.mWallpaperControllerLocked = this.mService.mRoot.mWallpaperController;
        this.mPerformSurfacePlacement = new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 13, this);
    }

    /* renamed from: lambda$-com_android_server_wm_WindowSurfacePlacer_6052 */
    /* synthetic */ void m261lambda$-com_android_server_wm_WindowSurfacePlacer_6052() {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                performSurfacePlacement();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void deferLayout() {
        this.mDeferDepth++;
    }

    void continueLayout() {
        this.mDeferDepth--;
        if (this.mDeferDepth <= 0) {
            performSurfacePlacement();
        }
    }

    boolean isLayoutDeferred() {
        return this.mDeferDepth > 0;
    }

    final void performSurfacePlacement() {
        performSurfacePlacement(false);
    }

    final void performSurfacePlacement(boolean force) {
        if (this.mDeferDepth <= 0 || (force ^ 1) == 0) {
            int loopCount = 6;
            do {
                this.mTraversalScheduled = false;
                performSurfacePlacementLoop();
                this.mService.mAnimationHandler.removeCallbacks(this.mPerformSurfacePlacement);
                loopCount--;
                if (!this.mTraversalScheduled) {
                    break;
                }
            } while (loopCount > 0);
            this.mService.mRoot.mWallpaperActionPending = false;
        }
    }

    private void performSurfacePlacementLoop() {
        if (this.mInLayout) {
            if (WindowManagerDebugConfig.DEBUG) {
                throw new RuntimeException("Recursive call!");
            }
            Slog.w(TAG, "performLayoutAndPlaceSurfacesLocked called while in layout. Callers=" + Debug.getCallers(3));
        } else if (!this.mService.mWaitingForConfig && this.mService.mDisplayReady) {
            Trace.traceBegin(32, "wmLayout");
            this.mInLayout = true;
            this.mLayoutCalled = true;
            boolean recoveringMemory = false;
            if (!this.mService.mForceRemoves.isEmpty()) {
                recoveringMemory = true;
                while (!this.mService.mForceRemoves.isEmpty()) {
                    WindowState ws = (WindowState) this.mService.mForceRemoves.remove(0);
                    Slog.i(TAG, "Force removing: " + ws);
                    ws.removeImmediately();
                }
                Slog.w(TAG, "Due to memory failure, waiting a bit for next layout");
                Object tmp = new Object();
                synchronized (tmp) {
                    try {
                        tmp.wait(250);
                    } catch (InterruptedException e) {
                    }
                }
            }
            try {
                this.mService.mRoot.performSurfacePlacement(recoveringMemory);
                this.mInLayout = false;
                if (this.mService.mRoot.isLayoutNeeded()) {
                    int i = this.mLayoutRepeatCount + 1;
                    this.mLayoutRepeatCount = i;
                    if (i < 6) {
                        requestTraversal();
                    } else {
                        Slog.e(TAG, "Performed 6 layouts in a row. Skipping");
                        this.mLayoutRepeatCount = 0;
                    }
                } else {
                    this.mLayoutRepeatCount = 0;
                }
                if (this.mService.mWindowsChanged && (this.mService.mWindowChangeListeners.isEmpty() ^ 1) != 0) {
                    this.mService.mH.removeMessages(19);
                    this.mService.mH.sendEmptyMessage(19);
                }
            } catch (RuntimeException e2) {
                this.mInLayout = false;
                Slog.wtf(TAG, "Unhandled exception while laying out windows", e2);
            }
            Trace.traceEnd(32);
        }
    }

    void debugLayoutRepeats(String msg, int pendingLayoutChanges) {
        if (this.mLayoutRepeatCount >= 4) {
            Slog.v(TAG, "Layouts looping: " + msg + ", mPendingLayoutChanges = 0x" + Integer.toHexString(pendingLayoutChanges));
        }
    }

    boolean isInLayout() {
        return this.mInLayout;
    }

    int handleAppTransitionReadyLocked() {
        int appsCount = this.mService.mOpeningApps.size();
        if (!transitionGoodToGo(appsCount, this.mTempTransitionReasons)) {
            return 0;
        }
        int i;
        AppWindowToken wtoken;
        AppWindowAnimator openingAppAnimator;
        AppWindowAnimator closingAppAnimator;
        Trace.traceBegin(32, "AppTransitionReady");
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "**** GOOD TO GO, Callers=" + Debug.getCallers(6));
        } else if (WindowManagerDebugConfig.DEBUG_WMS) {
            Slog.v(TAG, "**** GOOD TO GO");
        }
        if (this.mService.mHasWindowFreezing) {
            this.mService.mHasWindowFreezed = true;
            this.mService.mHasWindowFreezing = false;
        }
        int transit = this.mService.mAppTransition.getAppTransition();
        if (this.mService.mSkipAppTransitionAnimation && (AppTransition.isKeyguardGoingAwayTransit(transit) ^ 1) != 0) {
            transit = -1;
        }
        this.mService.mSkipAppTransitionAnimation = false;
        this.mService.mNoAnimationNotifyOnTransitionFinished.clear();
        this.mService.mH.removeMessages(13);
        DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
        this.mService.mRoot.mWallpaperMayChange = false;
        LayoutParams animLp = null;
        int bestAnimLayer = -1;
        boolean fullscreenAnim = false;
        boolean voiceInteraction = false;
        for (i = 0; i < appsCount; i++) {
            wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i);
            if (!(wtoken.toString().contains("com.tencent.mm/.plugin.luckymoney.ui") || ActivityThread.inCptWhiteList(CompatibilityHelper.MM_MONEY_LUCKY_CHECK, wtoken.toString()))) {
                wtoken.clearAnimatingFlags();
                wtoken.mFromFreeform = false;
            }
        }
        this.mWallpaperControllerLocked.adjustWallpaperWindowsForAppTransitionIfNeeded(displayContent, this.mService.mOpeningApps);
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        boolean openingAppHasWallpaper = false;
        boolean closingAppHasWallpaper = false;
        int closingAppsCount = this.mService.mClosingApps.size();
        appsCount = closingAppsCount + this.mService.mOpeningApps.size();
        for (i = 0; i < appsCount; i++) {
            if (i < closingAppsCount) {
                wtoken = (AppWindowToken) this.mService.mClosingApps.valueAt(i);
                if (wallpaperTarget != null && wtoken.windowsCanBeWallpaperTarget()) {
                    closingAppHasWallpaper = true;
                }
            } else {
                wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i - closingAppsCount);
                if (wallpaperTarget != null && wtoken.windowsCanBeWallpaperTarget()) {
                    openingAppHasWallpaper = true;
                }
            }
            voiceInteraction |= wtoken.mVoiceInteraction;
            WindowState ws;
            if (wtoken.fillsParent()) {
                ws = wtoken.findMainWindow();
                if (ws != null) {
                    if (transit != 7) {
                        animLp = ws.mAttrs;
                        bestAnimLayer = ws.mLayer;
                        fullscreenAnim = true;
                    } else if (ws.mLayer > bestAnimLayer) {
                        animLp = ws.mAttrs;
                        bestAnimLayer = ws.mLayer;
                        fullscreenAnim = true;
                    }
                }
            } else if (!fullscreenAnim) {
                ws = wtoken.findMainWindow();
                if (ws != null && ws.mLayer > bestAnimLayer) {
                    animLp = ws.mAttrs;
                    bestAnimLayer = ws.mLayer;
                }
            }
        }
        transit = maybeUpdateTransitToWallpaper(transit, openingAppHasWallpaper, closingAppHasWallpaper);
        if (!this.mService.mPolicy.allowAppAnimationsLw()) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Animations disallowed by keyguard or dream.");
            }
            animLp = null;
        }
        processApplicationsAnimatingInPlace(transit);
        this.mTmpLayerAndToken.token = null;
        handleClosingApps(transit, animLp, voiceInteraction, this.mTmpLayerAndToken);
        AppWindowToken topClosingApp = this.mTmpLayerAndToken.token;
        AppWindowToken topOpeningApp = handleOpeningApps(transit, animLp, voiceInteraction, this.mTmpLayerAndToken.layer);
        this.mService.mAppTransition.setLastAppTransition(transit, topOpeningApp, topClosingApp);
        if (topOpeningApp == null) {
            openingAppAnimator = null;
        } else {
            openingAppAnimator = topOpeningApp.mAppAnimator;
        }
        if (topClosingApp == null) {
            closingAppAnimator = null;
        } else {
            closingAppAnimator = topClosingApp.mAppAnimator;
        }
        int flags = this.mService.mAppTransition.getTransitFlags();
        int layoutRedo = this.mService.mAppTransition.goodToGo(transit, openingAppAnimator, closingAppAnimator, this.mService.mOpeningApps, this.mService.mClosingApps);
        handleNonAppWindowsInTransition(transit, flags);
        this.mService.mAppTransition.postAnimationCallback();
        this.mService.mAppTransition.clear();
        this.mService.mTaskSnapshotController.onTransitionStarting();
        this.mService.mOpeningApps.clear();
        this.mService.mClosingApps.clear();
        this.mService.mUnknownAppVisibilityController.clear();
        displayContent.setLayoutNeeded();
        this.mService.getDefaultDisplayContentLocked().computeImeTarget(true);
        this.mService.updateFocusedWindowLocked(2, true);
        this.mService.mFocusMayChange = false;
        this.mService.mH.obtainMessage(47, this.mTempTransitionReasons.clone()).sendToTarget();
        Trace.traceEnd(32);
        return (layoutRedo | 1) | 2;
    }

    private AppWindowToken handleOpeningApps(int transit, LayoutParams animLp, boolean voiceInteraction, int topClosingLayer) {
        AppWindowToken topOpeningApp = null;
        int appsCount = this.mService.mOpeningApps.size();
        int i = 0;
        while (i < appsCount) {
            AppWindowToken wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i);
            AppWindowAnimator appAnimator = wtoken.mAppAnimator;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Now opening app" + wtoken);
            }
            if (!appAnimator.usingTransferredAnimation) {
                appAnimator.clearThumbnail();
                appAnimator.setNullAnimation();
            }
            if (!wtoken.setVisibility(animLp, true, transit, false, voiceInteraction)) {
                this.mService.mNoAnimationNotifyOnTransitionFinished.add(wtoken.token);
            }
            wtoken.updateReportedVisibilityLocked();
            wtoken.waitingToShow = false;
            wtoken.setAllAppWinAnimators();
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, ">>> OPEN TRANSACTION handleAppTransitionReadyLocked()");
            }
            this.mService.openSurfaceTransaction();
            try {
                this.mService.mAnimator.orAnimating(appAnimator.showAllWindowsLocked());
                WindowAnimator windowAnimator = this.mService.mAnimator;
                windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
                int topOpeningLayer = 0;
                if (animLp != null) {
                    int layer = wtoken.getHighestAnimLayer();
                    if (topOpeningApp == null || layer > 0) {
                        topOpeningApp = wtoken;
                        topOpeningLayer = layer;
                    }
                }
                if (this.mService.mAppTransition.isNextAppTransitionThumbnailUp()) {
                    createThumbnailAppAnimator(transit, wtoken, topOpeningLayer, topClosingLayer);
                }
                i++;
            } finally {
                this.mService.closeSurfaceTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION handleAppTransitionReadyLocked()");
                }
            }
        }
        if (this.mService.mPolicy.isGestureAnimSupport() && topOpeningApp != null && topOpeningApp.toString().contains("com.oppo.launcher/.Launcher")) {
            this.mService.mPolicy.handleOpeningSpecialApp(0, ActivityManagerService.OPPO_LAUNCHER);
        }
        return topOpeningApp;
    }

    private void handleClosingApps(int transit, LayoutParams animLp, boolean voiceInteraction, LayerAndToken layerAndToken) {
        int appsCount = this.mService.mClosingApps.size();
        if (this.mService.mOpeningApps.size() > 0 && appsCount > 0) {
            this.mService.mAppTransition.setAppWindowTokenLocked((AppWindowToken) this.mService.mClosingApps.valueAt(0), (AppWindowToken) this.mService.mOpeningApps.valueAt(0));
        }
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = (AppWindowToken) this.mService.mClosingApps.valueAt(i);
            wtoken.markSavedSurfaceExiting();
            AppWindowAnimator appAnimator = wtoken.mAppAnimator;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Now closing app " + wtoken);
            }
            appAnimator.clearThumbnail();
            appAnimator.setNullAnimation();
            wtoken.setVisibility(animLp, false, transit, false, voiceInteraction);
            wtoken.updateReportedVisibilityLocked();
            wtoken.allDrawn = true;
            wtoken.deferClearAllDrawn = false;
            if (!(wtoken.startingWindow == null || (wtoken.startingWindow.mAnimatingExit ^ 1) == 0 || wtoken.getController() == null)) {
                wtoken.getController().removeStartingWindow();
            }
            WindowAnimator windowAnimator = this.mService.mAnimator;
            windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
            if (animLp != null) {
                int layer = wtoken.getHighestAnimLayer();
                if (layerAndToken.token == null || layer > layerAndToken.layer) {
                    layerAndToken.token = wtoken;
                    layerAndToken.layer = layer;
                }
            }
            if (this.mService.mAppTransition.isNextAppTransitionThumbnailDown()) {
                createThumbnailAppAnimator(transit, wtoken, 0, layerAndToken.layer);
            }
        }
    }

    private void handleNonAppWindowsInTransition(int transit, int flags) {
        boolean z = true;
        if (transit == 20 && (flags & 4) != 0 && (flags & 2) == 0) {
            Animation anim = this.mService.mPolicy.createKeyguardWallpaperExit((flags & 1) != 0);
            if (anim != null) {
                this.mService.getDefaultDisplayContentLocked().mWallpaperController.startWallpaperAnimation(anim);
            }
        }
        if (transit == 20 || transit == 21) {
            boolean z2;
            DisplayContent defaultDisplayContentLocked = this.mService.getDefaultDisplayContentLocked();
            if (transit == 21) {
                z2 = true;
            } else {
                z2 = false;
            }
            if ((flags & 1) == 0) {
                z = false;
            }
            defaultDisplayContentLocked.startKeyguardExitOnNonAppWindows(z2, z);
        }
    }

    private boolean transitionGoodToGo(int appsCount, SparseIntArray outReasons) {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || this.mService.mAppTransition.isTimeout()) {
            Slog.v(TAG, "Checking " + appsCount + " opening apps (frozen=" + this.mService.mDisplayFrozen + " timeout=" + this.mService.mAppTransition.isTimeout() + ")...");
        }
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
        outReasons.clear();
        if (this.mService.mAppTransition.isTimeout()) {
            return true;
        }
        if (screenRotationAnimation != null && screenRotationAnimation.isAnimating() && this.mService.rotationNeedsUpdateLocked()) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Delaying app transition for screen rotation animation to finish");
            }
            return false;
        }
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Check opening app=" + wtoken + ": allDrawn=" + wtoken.allDrawn + " startingDisplayed=" + wtoken.startingDisplayed + " startingMoved=" + wtoken.startingMoved + " isRelaunching()=" + wtoken.isRelaunching());
            }
            boolean drawnBeforeRestoring = wtoken.allDrawn;
            wtoken.restoreSavedSurfaceForInterestingWindows();
            int allDrawn = wtoken.allDrawn ? wtoken.isRelaunching() ^ 1 : 0;
            if (allDrawn == 0 && (wtoken.startingDisplayed ^ 1) != 0 && (wtoken.startingMoved ^ 1) != 0) {
                return false;
            }
            TaskStack stack = wtoken.getStack();
            int stackId = stack != null ? stack.mStackId : -1;
            int i2;
            if (allDrawn != 0) {
                if (drawnBeforeRestoring) {
                    i2 = 2;
                } else {
                    i2 = 0;
                }
                outReasons.put(stackId, i2);
            } else {
                if (wtoken.startingData instanceof SplashScreenStartingData) {
                    i2 = 1;
                } else {
                    i2 = 4;
                }
                outReasons.put(stackId, i2);
            }
        }
        if (this.mService.mAppTransition.isFetchingAppTransitionsSpecs()) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "isFetchingAppTransitionSpecs=true");
            }
            return false;
        } else if (this.mService.mUnknownAppVisibilityController.allResolved()) {
            boolean wallpaperReady;
            if (this.mWallpaperControllerLocked.isWallpaperVisible()) {
                wallpaperReady = this.mWallpaperControllerLocked.wallpaperTransitionReady();
            } else {
                wallpaperReady = true;
            }
            return wallpaperReady;
        } else {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "unknownApps is not empty: " + this.mService.mUnknownAppVisibilityController.getDebugMessage());
            }
            return false;
        }
    }

    private int maybeUpdateTransitToWallpaper(int transit, boolean openingAppHasWallpaper, boolean closingAppHasWallpaper) {
        if (transit == 0) {
            return 0;
        }
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        WindowState oldWallpaper = this.mWallpaperControllerLocked.isWallpaperTargetAnimating() ? null : wallpaperTarget;
        ArraySet<AppWindowToken> openingApps = this.mService.mOpeningApps;
        ArraySet<AppWindowToken> closingApps = this.mService.mClosingApps;
        boolean openingCanBeWallpaperTarget = canBeWallpaperTarget(openingApps);
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "New wallpaper target=" + wallpaperTarget + ", oldWallpaper=" + oldWallpaper + ", openingApps=" + openingApps + ", closingApps=" + closingApps);
        }
        this.mService.mAnimateWallpaperWithTarget = false;
        if (openingCanBeWallpaperTarget && transit == 20) {
            transit = 21;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "New transit: " + AppTransition.appTransitionToString(21));
            }
        } else if (!AppTransition.isKeyguardGoingAwayTransit(transit)) {
            if (closingAppHasWallpaper && openingAppHasWallpaper) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "Wallpaper animation!");
                }
                switch (transit) {
                    case 6:
                    case 8:
                    case 10:
                        transit = 14;
                        break;
                    case 7:
                    case 9:
                    case 11:
                        transit = 15;
                        break;
                }
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit: " + AppTransition.appTransitionToString(transit));
                }
            } else if (oldWallpaper != null && (this.mService.mOpeningApps.isEmpty() ^ 1) != 0 && (openingApps.contains(oldWallpaper.mAppToken) ^ 1) != 0 && closingApps.contains(oldWallpaper.mAppToken)) {
                transit = 12;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit away from wallpaper: " + AppTransition.appTransitionToString(12));
                }
            } else if (wallpaperTarget != null && wallpaperTarget.isVisibleLw() && (openingApps.contains(wallpaperTarget.mAppToken) || transit == 10)) {
                transit = 13;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit into wallpaper: " + AppTransition.appTransitionToString(13));
                }
            } else {
                this.mService.mAnimateWallpaperWithTarget = true;
            }
        }
        return transit;
    }

    private boolean canBeWallpaperTarget(ArraySet<AppWindowToken> apps) {
        for (int i = apps.size() - 1; i >= 0; i--) {
            if (((AppWindowToken) apps.valueAt(i)).windowsCanBeWallpaperTarget()) {
                return true;
            }
        }
        return false;
    }

    private void processApplicationsAnimatingInPlace(int transit) {
        if (transit == 17) {
            WindowState win = this.mService.getDefaultDisplayContentLocked().findFocusedWindow();
            if (win != null) {
                AppWindowToken wtoken = win.mAppToken;
                AppWindowAnimator appAnimator = wtoken.mAppAnimator;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "Now animating app in place " + wtoken);
                }
                appAnimator.clearThumbnail();
                appAnimator.setNullAnimation();
                this.mService.updateTokenInPlaceLocked(wtoken, transit);
                wtoken.updateReportedVisibilityLocked();
                wtoken.setAllAppWinAnimators();
                WindowAnimator windowAnimator = this.mService.mAnimator;
                windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
                this.mService.mAnimator.orAnimating(appAnimator.showAllWindowsLocked());
            }
        }
    }

    private void createThumbnailAppAnimator(int transit, AppWindowToken appToken, int openingLayer, int closingLayer) {
        AppWindowAnimator openingAppAnimator = appToken == null ? null : appToken.mAppAnimator;
        if (openingAppAnimator != null && openingAppAnimator.animation != null) {
            if (this.mService.mPolicy.isGestureAnimSupport() && (this.mService.mClosingApps.isEmpty() ^ 1) != 0 && ((AppWindowToken) this.mService.mClosingApps.valueAt(0)).toString().contains("com.oppo.launcher/.Launcher")) {
                WindowState topWindow = appToken.getTopWindow();
                if (topWindow != null && topWindow.toString().contains("Splash Screen") && (topWindow.getAttrs().format == -2 || topWindow.getAttrs().format == -3)) {
                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                        Slog.d(TAG, "createThumbnailAppAnimator   topWindow.getAttrs().format " + topWindow.getAttrs().format + " topWindow " + topWindow.toString());
                    }
                    return;
                }
            }
            int taskId = appToken.getTask().mTaskId;
            GraphicBuffer thumbnailHeader = this.mService.mAppTransition.getAppTransitionThumbnailHeader(taskId);
            if (thumbnailHeader == null) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.d(TAG, "No thumbnail header bitmap for: " + taskId);
                }
                return;
            }
            Rect dirty = new Rect(0, 0, thumbnailHeader.getWidth(), thumbnailHeader.getHeight());
            try {
                Animation anim;
                DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
                Display display = displayContent.getDisplay();
                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                WindowState window = appToken.findMainWindow();
                SurfaceControl surfaceControl = new SurfaceControl(this.mService.mFxSession, "thumbnail anim", dirty.width(), dirty.height(), -3, 4, appToken.windowType, window != null ? window.mOwnerUid : Binder.getCallingUid());
                surfaceControl.setLayerStack(display.getLayerStack());
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i(TAG, "  THUMBNAIL " + surfaceControl + ": CREATE");
                }
                Surface drawSurface = new Surface();
                drawSurface.copyFrom(surfaceControl);
                drawSurface.attachAndQueueBuffer(thumbnailHeader);
                drawSurface.release();
                if (this.mService.mAppTransition.isNextThumbnailTransitionAspectScaled()) {
                    Rect appRect;
                    WindowState win = appToken.findMainWindow();
                    if (win != null) {
                        appRect = win.getContentFrameLw();
                    } else {
                        appRect = new Rect(0, 0, displayInfo.appWidth, displayInfo.appHeight);
                    }
                    Rect insets = win != null ? win.mContentInsets : null;
                    Configuration displayConfig = displayContent.getConfiguration();
                    if (this.mService.mPolicy.isGestureAnimSupport() && (this.mService.mClosingApps.isEmpty() ^ 1) != 0 && ((AppWindowToken) this.mService.mClosingApps.valueAt(0)).toString().contains("com.oppo.launcher/.Launcher")) {
                        anim = this.mService.mAppTransition.createLauncherThumbnailAspectScaleAnimationLocked(appRect, insets, thumbnailHeader, taskId, displayConfig.uiMode, displayConfig.orientation);
                    } else {
                        anim = this.mService.mAppTransition.createThumbnailAspectScaleAnimationLocked(appRect, insets, thumbnailHeader, taskId, displayConfig.uiMode, displayConfig.orientation);
                    }
                    openingAppAnimator.thumbnailForceAboveLayer = Math.max(openingLayer, closingLayer);
                    openingAppAnimator.deferThumbnailDestruction = this.mService.mAppTransition.isNextThumbnailTransitionScaleUp() ^ 1;
                } else {
                    anim = this.mService.mAppTransition.createThumbnailScaleAnimationLocked(displayInfo.appWidth, displayInfo.appHeight, transit, thumbnailHeader);
                }
                anim.restrictDuration(10000);
                anim.scaleCurrentDuration(this.mService.getTransitionAnimationScaleLocked());
                openingAppAnimator.thumbnail = surfaceControl;
                openingAppAnimator.thumbnailLayer = openingLayer;
                openingAppAnimator.thumbnailAnimation = anim;
                this.mService.mAppTransition.getNextAppTransitionStartRect(taskId, this.mTmpStartRect);
            } catch (Throwable e) {
                Slog.e(TAG, "Can't allocate thumbnail/Canvas surface w=" + dirty.width() + " h=" + dirty.height(), e);
                openingAppAnimator.clearThumbnail();
            }
        }
    }

    void requestTraversal() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            this.mService.mAnimationHandler.post(this.mPerformSurfacePlacement);
        }
    }

    void destroyAfterTransaction(SurfaceControl surface) {
        this.mPendingDestroyingSurfaces.add(surface);
    }

    void destroyPendingSurfaces() {
        for (int i = this.mPendingDestroyingSurfaces.size() - 1; i >= 0; i--) {
            ((SurfaceControl) this.mPendingDestroyingSurfaces.get(i)).destroy();
        }
        this.mPendingDestroyingSurfaces.clear();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "mTraversalScheduled=" + this.mTraversalScheduled);
        pw.println(prefix + "mHoldScreenWindow=" + this.mService.mRoot.mHoldScreenWindow);
        pw.println(prefix + "mObscuringWindow=" + this.mService.mRoot.mObscuringWindow);
    }
}
