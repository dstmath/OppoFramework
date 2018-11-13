package com.android.server.wm;

import android.app.ActivityThread;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Debug;
import android.os.OppoAssertTip;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.provider.Settings.Global;
import android.util.ArraySet;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import com.android.server.am.ActivityManagerService;
import com.android.server.display.OppoBrightUtils;
import com.android.server.pm.CompatibilityHelper;
import com.mediatek.multiwindow.MultiWindowManager;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
class WindowSurfacePlacer {
    static final int SET_FORCE_HIDING_CHANGED = 4;
    static final int SET_ORIENTATION_CHANGE_COMPLETE = 8;
    static final int SET_TURN_ON_SCREEN = 16;
    static final int SET_UPDATE_ROTATION = 1;
    static final int SET_WALLPAPER_ACTION_PENDING = 32;
    static final int SET_WALLPAPER_MAY_CHANGE = 2;
    private static final String TAG = null;
    private float mButtonBrightness;
    private int mDeferDepth;
    private boolean mDisplayHasContent;
    private Session mHoldScreen;
    WindowState mHoldScreenWindow;
    private boolean mInLayout;
    private Object mLastWindowFreezeSource;
    boolean mLayoutCalled;
    private int mLayoutRepeatCount;
    private boolean mObscureApplicationContentOnSecondaryDisplays;
    private boolean mObscured;
    WindowState mObsuringWindow;
    boolean mOrientationChangeComplete;
    private final ArrayList<SurfaceControl> mPendingDestroyingSurfaces;
    private int mPreferredModeId;
    private float mPreferredRefreshRate;
    private float mScreenBrightness;
    private final WindowManagerService mService;
    private boolean mSustainedPerformanceModeCurrent;
    private boolean mSustainedPerformanceModeEnabled;
    private boolean mSyswin;
    private final Rect mTmpContentRect;
    private final LayerAndToken mTmpLayerAndToken;
    private final Rect mTmpStartRect;
    private boolean mTraversalScheduled;
    private boolean mUpdateRotation;
    private long mUserActivityTimeout;
    boolean mWallpaperActionPending;
    private final WallpaperController mWallpaperControllerLocked;
    private boolean mWallpaperForceHidingChanged;
    boolean mWallpaperMayChange;

    private static final class LayerAndToken {
        public int layer;
        public AppWindowToken token;

        /* synthetic */ LayerAndToken(LayerAndToken layerAndToken) {
            this();
        }

        private LayerAndToken() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowSurfacePlacer.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowSurfacePlacer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowSurfacePlacer.<clinit>():void");
    }

    public WindowSurfacePlacer(WindowManagerService service) {
        this.mInLayout = false;
        this.mWallpaperMayChange = false;
        this.mOrientationChangeComplete = true;
        this.mWallpaperActionPending = false;
        this.mWallpaperForceHidingChanged = false;
        this.mLastWindowFreezeSource = null;
        this.mHoldScreen = null;
        this.mObscured = false;
        this.mSyswin = false;
        this.mScreenBrightness = -1.0f;
        this.mButtonBrightness = -1.0f;
        this.mUserActivityTimeout = -1;
        this.mUpdateRotation = false;
        this.mTmpStartRect = new Rect();
        this.mTmpContentRect = new Rect();
        this.mDisplayHasContent = false;
        this.mObscureApplicationContentOnSecondaryDisplays = false;
        this.mPreferredRefreshRate = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mPreferredModeId = 0;
        this.mDeferDepth = 0;
        this.mSustainedPerformanceModeEnabled = false;
        this.mSustainedPerformanceModeCurrent = false;
        this.mHoldScreenWindow = null;
        this.mObsuringWindow = null;
        this.mTmpLayerAndToken = new LayerAndToken();
        this.mPendingDestroyingSurfaces = new ArrayList();
        this.mLayoutCalled = false;
        this.mService = service;
        this.mWallpaperControllerLocked = this.mService.mWallpaperControllerLocked;
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

    final void performSurfacePlacement() {
        if (this.mDeferDepth <= 0) {
            int loopCount = 6;
            do {
                this.mTraversalScheduled = false;
                performSurfacePlacementLoop();
                this.mService.mH.removeMessages(4);
                loopCount--;
                if (!this.mTraversalScheduled) {
                    break;
                }
            } while (loopCount > 0);
            this.mWallpaperActionPending = false;
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
                    this.mService.removeWindowInnerLocked(ws);
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
                performSurfacePlacementInner(recoveringMemory);
                this.mInLayout = false;
                if (this.mService.needsLayout()) {
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
                if (this.mService.mWindowsChanged && !this.mService.mWindowChangeListeners.isEmpty()) {
                    this.mService.mH.removeMessages(19);
                    this.mService.mH.sendEmptyMessage(19);
                }
            } catch (RuntimeException e2) {
                this.mInLayout = false;
                Slog.e(TAG, "Unhandled exception while laying out windows", e2);
            }
            Trace.traceEnd(32);
        }
    }

    void debugLayoutRepeats(String msg, int pendingLayoutChanges) {
        if (this.mLayoutRepeatCount >= 4) {
            Slog.v(TAG, "Layouts looping: " + msg + ", mPendingLayoutChanges = 0x" + Integer.toHexString(pendingLayoutChanges));
        }
    }

    private void performSurfacePlacementInner(boolean recoveringMemory) {
        int displayNdx;
        DisplayContent displayContent;
        int i;
        int stackNdx;
        AppTokenList exitingAppTokens;
        WindowState win;
        if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
            Slog.v(TAG, "performSurfacePlacementInner: entry. Called by " + Debug.getCallers(3));
        }
        boolean updateInputWindowsNeeded = false;
        if (this.mService.mFocusMayChange) {
            this.mService.mFocusMayChange = false;
            updateInputWindowsNeeded = this.mService.updateFocusedWindowLocked(3, false);
        }
        int numDisplays = this.mService.mDisplayContents.size();
        for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            displayContent = (DisplayContent) this.mService.mDisplayContents.valueAt(displayNdx);
            for (i = displayContent.mExitingTokens.size() - 1; i >= 0; i--) {
                ((WindowToken) displayContent.mExitingTokens.get(i)).hasVisible = false;
            }
        }
        for (stackNdx = this.mService.mStackIdToStack.size() - 1; stackNdx >= 0; stackNdx--) {
            exitingAppTokens = ((TaskStack) this.mService.mStackIdToStack.valueAt(stackNdx)).mExitingAppTokens;
            for (int tokenNdx = exitingAppTokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                ((AppWindowToken) exitingAppTokens.get(tokenNdx)).hasVisible = false;
            }
        }
        this.mHoldScreen = null;
        this.mHoldScreenWindow = null;
        this.mObsuringWindow = null;
        this.mScreenBrightness = -1.0f;
        this.mButtonBrightness = -1.0f;
        this.mUserActivityTimeout = -1;
        this.mObscureApplicationContentOnSecondaryDisplays = false;
        this.mSustainedPerformanceModeCurrent = false;
        WindowManagerService windowManagerService = this.mService;
        windowManagerService.mTransactionSequence++;
        DisplayContent defaultDisplay = this.mService.getDefaultDisplayContentLocked();
        DisplayInfo defaultInfo = defaultDisplay.getDisplayInfo();
        int defaultDw = defaultInfo.logicalWidth;
        int defaultDh = defaultInfo.logicalHeight;
        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
            Slog.i(TAG, ">>> OPEN TRANSACTION performLayoutAndPlaceSurfaces");
        }
        SurfaceControl.openTransaction();
        try {
            applySurfaceChangesTransaction(recoveringMemory, numDisplays, defaultDw, defaultDh);
            SurfaceControl.closeTransaction();
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, "<<< CLOSE TRANSACTION performLayoutAndPlaceSurfaces");
            }
        } catch (RuntimeException e) {
            Slog.wtf(TAG, "Unhandled exception in Window Manager", e);
            SurfaceControl.closeTransaction();
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, "<<< CLOSE TRANSACTION performLayoutAndPlaceSurfaces");
            }
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, "<<< CLOSE TRANSACTION performLayoutAndPlaceSurfaces");
            }
        }
        WindowList defaultWindows = defaultDisplay.getWindowList();
        if (this.mService.mAppTransition.isReady()) {
            defaultDisplay.pendingLayoutChanges |= handleAppTransitionReadyLocked(defaultWindows);
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                debugLayoutRepeats("after handleAppTransitionReadyLocked", defaultDisplay.pendingLayoutChanges);
            }
        }
        if (!this.mService.mAnimator.mAppWindowAnimating && this.mService.mAppTransition.isRunning()) {
            defaultDisplay.pendingLayoutChanges |= this.mService.handleAnimatingStoppedAndTransitionLocked();
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                debugLayoutRepeats("after handleAnimStopAndXitionLock", defaultDisplay.pendingLayoutChanges);
            }
        }
        if (this.mWallpaperForceHidingChanged && defaultDisplay.pendingLayoutChanges == 0 && !this.mService.mAppTransition.isReady()) {
            defaultDisplay.pendingLayoutChanges |= 1;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                debugLayoutRepeats("after animateAwayWallpaperLocked", defaultDisplay.pendingLayoutChanges);
            }
        }
        this.mWallpaperForceHidingChanged = false;
        if (this.mWallpaperMayChange) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                Slog.v(TAG, "Wallpaper may change!  Adjusting");
            }
            defaultDisplay.pendingLayoutChanges |= 4;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                debugLayoutRepeats("WallpaperMayChange", defaultDisplay.pendingLayoutChanges);
            }
        }
        if (this.mService.mFocusMayChange) {
            this.mService.mFocusMayChange = false;
            if (this.mService.updateFocusedWindowLocked(2, false)) {
                updateInputWindowsNeeded = true;
                defaultDisplay.pendingLayoutChanges |= 8;
            }
        }
        if (this.mService.needsLayout()) {
            defaultDisplay.pendingLayoutChanges |= 1;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                debugLayoutRepeats("mLayoutNeeded", defaultDisplay.pendingLayoutChanges);
            }
        }
        for (i = this.mService.mResizingWindows.size() - 1; i >= 0; i--) {
            win = (WindowState) this.mService.mResizingWindows.get(i);
            if (!win.mAppFreezing) {
                if (win.mAppToken != null) {
                    win.mAppToken.destroySavedSurfaces();
                }
                win.reportResized();
                this.mService.mResizingWindows.remove(i);
            }
        }
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION && this.mService.mDisplayFrozen) {
            Slog.v(TAG, "With display frozen, orientationChangeComplete=" + this.mOrientationChangeComplete);
        }
        if (this.mOrientationChangeComplete) {
            boolean rmFreezeTimeoutMsg = false;
            if (this.mService.mWindowsFreezingScreen != 0) {
                this.mService.mWindowsFreezingScreen = 0;
                this.mService.mLastFinishedFreezeSource = this.mLastWindowFreezeSource;
                this.mService.mH.removeMessages(11);
                rmFreezeTimeoutMsg = true;
            }
            this.mService.stopFreezingDisplayLocked();
            if (rmFreezeTimeoutMsg && this.mService.mDisplayFrozen) {
                Slog.w(TAG, "stopFreezing but still frozen status! mWaitingForConfig:" + this.mService.mWaitingForConfig + " mAppsFreezingScreen:" + this.mService.mAppsFreezingScreen + " mWindowsFreezingScreen:" + this.mService.mWindowsFreezingScreen + " mClientFreezingScreen:" + this.mService.mClientFreezingScreen + " mOpeningApps.isEmpty():" + this.mService.mOpeningApps.isEmpty() + " mDisableFrozenBySecuritypermission:" + this.mService.mDisableFrozenBySecuritypermission);
            }
        }
        boolean wallpaperDestroyed = false;
        i = this.mService.mDestroySurface.size();
        if (i > 0) {
            do {
                i--;
                win = (WindowState) this.mService.mDestroySurface.get(i);
                win.mDestroying = false;
                if (this.mService.mInputMethodWindow == win) {
                    this.mService.mInputMethodWindow = null;
                }
                if (this.mWallpaperControllerLocked.isWallpaperTarget(win)) {
                    wallpaperDestroyed = true;
                }
                win.destroyOrSaveSurface();
            } while (i > 0);
            this.mService.mDestroySurface.clear();
        }
        for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<WindowToken> exitingTokens = ((DisplayContent) this.mService.mDisplayContents.valueAt(displayNdx)).mExitingTokens;
            for (i = exitingTokens.size() - 1; i >= 0; i--) {
                WindowToken token = (WindowToken) exitingTokens.get(i);
                if (!token.hasVisible) {
                    exitingTokens.remove(i);
                    if (token.windowType == 2013) {
                        this.mWallpaperControllerLocked.removeWallpaperToken(token);
                    }
                }
            }
        }
        for (stackNdx = this.mService.mStackIdToStack.size() - 1; stackNdx >= 0; stackNdx--) {
            exitingAppTokens = ((TaskStack) this.mService.mStackIdToStack.valueAt(stackNdx)).mExitingAppTokens;
            for (i = exitingAppTokens.size() - 1; i >= 0; i--) {
                AppWindowToken token2 = (AppWindowToken) exitingAppTokens.get(i);
                if (!(token2.hasVisible || this.mService.mClosingApps.contains(token2) || (token2.mIsExiting && !token2.allAppWindows.isEmpty()))) {
                    token2.mAppAnimator.clearAnimation();
                    token2.mAppAnimator.animating = false;
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT) {
                        Slog.v(TAG, "performLayout: App token exiting now removed" + token2);
                    }
                    token2.removeAppFromTaskLocked();
                }
            }
        }
        if (wallpaperDestroyed) {
            defaultDisplay.pendingLayoutChanges |= 4;
            defaultDisplay.layoutNeeded = true;
        }
        for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            displayContent = (DisplayContent) this.mService.mDisplayContents.valueAt(displayNdx);
            if (displayContent.pendingLayoutChanges != 0) {
                displayContent.layoutNeeded = true;
            }
        }
        this.mService.mInputMonitor.updateInputWindowsLw(true);
        this.mService.setHoldScreenLocked(this.mHoldScreen);
        if (!this.mService.mDisplayFrozen) {
            if (this.mScreenBrightness < OppoBrightUtils.MIN_LUX_LIMITI || this.mScreenBrightness > 1.0f) {
                this.mService.mPowerManagerInternal.setScreenBrightnessOverrideFromWindowManager(-1);
            } else {
                this.mService.mPowerManagerInternal.setScreenBrightnessOverrideFromWindowManager(toBrightnessOverride(this.mScreenBrightness));
            }
            if (this.mButtonBrightness < OppoBrightUtils.MIN_LUX_LIMITI || this.mButtonBrightness > 1.0f) {
                this.mService.mPowerManagerInternal.setButtonBrightnessOverrideFromWindowManager(-1);
            } else {
                this.mService.mPowerManagerInternal.setButtonBrightnessOverrideFromWindowManager(toBrightnessOverride(this.mButtonBrightness));
            }
            this.mService.mPowerManagerInternal.setUserActivityTimeoutOverrideFromWindowManager(this.mUserActivityTimeout);
        }
        if (this.mSustainedPerformanceModeCurrent != this.mSustainedPerformanceModeEnabled) {
            this.mSustainedPerformanceModeEnabled = this.mSustainedPerformanceModeCurrent;
            this.mService.mPowerManagerInternal.powerHint(6, this.mSustainedPerformanceModeEnabled ? 1 : 0);
        }
        if (this.mService.mTurnOnScreen) {
            if (this.mService.mAllowTheaterModeWakeFromLayout || Global.getInt(this.mService.mContext.getContentResolver(), "theater_mode_on", 0) == 0) {
                if (!WindowManagerService.IS_USER_BUILD || WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_POWER) {
                    Slog.v(TAG, "Turning screen on after layout!");
                }
                this.mService.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server.wm:TURN_ON");
            }
            this.mService.mTurnOnScreen = false;
        }
        if (this.mUpdateRotation) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d(TAG, "Performing post-rotate rotation");
            }
            if (this.mService.updateRotationUncheckedLocked(false)) {
                this.mService.mH.sendEmptyMessage(18);
            } else {
                this.mUpdateRotation = false;
            }
        }
        if (!(this.mService.mWaitingForDrawnCallback == null && (!this.mOrientationChangeComplete || defaultDisplay.layoutNeeded || this.mUpdateRotation))) {
            this.mService.checkDrawnWindowsLocked();
        }
        int N = this.mService.mPendingRemove.size();
        if (N > 0) {
            if (this.mService.mPendingRemoveTmp.length < N) {
                this.mService.mPendingRemoveTmp = new WindowState[(N + 10)];
            }
            this.mService.mPendingRemove.toArray(this.mService.mPendingRemoveTmp);
            this.mService.mPendingRemove.clear();
            DisplayContentList<DisplayContent> displayList = new DisplayContentList();
            for (i = 0; i < N; i++) {
                WindowState w = this.mService.mPendingRemoveTmp[i];
                this.mService.removeWindowInnerLocked(w);
                displayContent = w.getDisplayContent();
                if (!(displayContent == null || displayList.contains(displayContent))) {
                    displayList.add(displayContent);
                }
            }
            for (DisplayContent displayContent2 : displayList) {
                this.mService.mLayersController.assignLayersLocked(displayContent2.getWindowList());
                displayContent2.layoutNeeded = true;
            }
        }
        for (displayNdx = this.mService.mDisplayContents.size() - 1; displayNdx >= 0; displayNdx--) {
            ((DisplayContent) this.mService.mDisplayContents.valueAt(displayNdx)).checkForDeferredActions();
        }
        if (updateInputWindowsNeeded) {
            this.mService.mInputMonitor.updateInputWindowsLw(false);
        }
        this.mService.setFocusTaskRegionLocked();
        if (!(!MultiWindowManager.isSupported() || this.mService.mCurrentFocus == null || this.mService.mCurrentFocus.getTask() == null)) {
            this.mService.showOrHideRestoreButton(this.mService.mCurrentFocus);
        }
        this.mService.enableScreenIfNeededLocked();
        this.mService.scheduleAnimationLocked();
        this.mService.mWindowPlacerLocked.destroyPendingSurfaces();
        if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
            Slog.e(TAG, "performSurfacePlacementInner exit: animating=" + this.mService.mAnimator.isAnimating());
        }
    }

    private void applySurfaceChangesTransaction(boolean recoveringMemory, int numDisplays, int defaultDw, int defaultDh) {
        if (this.mService.mWatermark != null) {
            this.mService.mWatermark.positionSurface(defaultDw, defaultDh);
        }
        if (this.mService.mStrictModeFlash != null) {
            this.mService.mStrictModeFlash.positionSurface(defaultDw, defaultDh);
        }
        if (this.mService.mCircularDisplayMask != null) {
            this.mService.mCircularDisplayMask.positionSurface(defaultDw, defaultDh, this.mService.mRotation);
        }
        if (this.mService.mEmulatorDisplayOverlay != null) {
            this.mService.mEmulatorDisplayOverlay.positionSurface(defaultDw, defaultDh, this.mService.mRotation);
        }
        boolean focusDisplayed = false;
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            int i;
            WindowState w;
            DisplayContent displayContent = (DisplayContent) this.mService.mDisplayContents.valueAt(displayNdx);
            boolean updateAllDrawn = false;
            WindowList windows = displayContent.getWindowList();
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int displayId = displayContent.getDisplayId();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            int innerDw = displayInfo.appWidth;
            int innerDh = displayInfo.appHeight;
            boolean isDefaultDisplay = displayId == 0;
            this.mDisplayHasContent = false;
            this.mPreferredRefreshRate = OppoBrightUtils.MIN_LUX_LIMITI;
            this.mPreferredModeId = 0;
            int repeats = 0;
            while (true) {
                repeats++;
                if (repeats <= 6) {
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                        debugLayoutRepeats("On entry to LockedInner", displayContent.pendingLayoutChanges);
                    }
                    if ((displayContent.pendingLayoutChanges & 4) != 0 && this.mWallpaperControllerLocked.adjustWallpaperWindows()) {
                        this.mService.mLayersController.assignLayersLocked(windows);
                        displayContent.layoutNeeded = true;
                    }
                    if (isDefaultDisplay && (displayContent.pendingLayoutChanges & 2) != 0) {
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                            Slog.v(TAG, "Computing new config from layout");
                        }
                        if (this.mService.updateOrientationFromAppTokensLocked(true)) {
                            displayContent.layoutNeeded = true;
                            this.mService.mH.sendEmptyMessage(18);
                        }
                    }
                    if ((displayContent.pendingLayoutChanges & 1) != 0) {
                        displayContent.layoutNeeded = true;
                    }
                    if (repeats < 4) {
                        performLayoutLockedInner(displayContent, repeats == 1, false);
                    } else {
                        Slog.w(TAG, "Layout repeat skipped after too many iterations");
                    }
                    displayContent.pendingLayoutChanges = 0;
                    if (isDefaultDisplay) {
                        this.mService.mPolicy.beginPostLayoutPolicyLw(dw, dh);
                        for (i = windows.size() - 1; i >= 0; i--) {
                            w = (WindowState) windows.get(i);
                            if (w.mHasSurface) {
                                OppoInterceptWindow.getInstance().interceptWindow(this.mService.mContext, w, this.mService.mPolicy);
                                this.mService.mPolicy.applyPostLayoutPolicyLw(w, w.mAttrs, w.mAttachedWindow);
                            }
                        }
                        displayContent.pendingLayoutChanges |= this.mService.mPolicy.finishPostLayoutPolicyLw();
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                            debugLayoutRepeats("after finishPostLayoutPolicyLw", displayContent.pendingLayoutChanges);
                        }
                    }
                    if (displayContent.pendingLayoutChanges == 0) {
                        break;
                    }
                } else {
                    Slog.w(TAG, "Animation repeat aborted after too many iterations");
                    displayContent.layoutNeeded = false;
                    break;
                }
            }
            this.mObscured = false;
            this.mSyswin = false;
            displayContent.resetDimming();
            boolean someoneLosingFocus = !this.mService.mLosingFocus.isEmpty();
            for (i = windows.size() - 1; i >= 0; i--) {
                w = (WindowState) windows.get(i);
                Task task = w.getTask();
                boolean obscuredChanged = w.mObscured != this.mObscured;
                w.mObscured = this.mObscured;
                if (!this.mObscured) {
                    handleNotObscuredLocked(w, displayInfo);
                }
                w.applyDimLayerIfNeeded();
                if (isDefaultDisplay && obscuredChanged && this.mWallpaperControllerLocked.isWallpaperTarget(w) && w.isVisibleLw()) {
                    this.mWallpaperControllerLocked.updateWallpaperVisibility();
                }
                WindowStateAnimator winAnimator = w.mWinAnimator;
                if (w.hasMoved()) {
                    int left = w.mFrame.left;
                    int top = w.mFrame.top;
                    boolean adjustedForMinimizedDockOrIme;
                    if (task == null) {
                        adjustedForMinimizedDockOrIme = false;
                    } else if (task.mStack.isAdjustedForMinimizedDockedStack()) {
                        adjustedForMinimizedDockOrIme = true;
                    } else {
                        adjustedForMinimizedDockOrIme = task.mStack.isAdjustedForIme();
                    }
                    if (this.mService.okToDisplay() && (w.mAttrs.privateFlags & 64) == 0 && !w.isDragResizing() && !adjustedForMinimizedDockOrIme && ((task == null || w.getTask().mStack.hasMovementAnimations()) && !w.mWinAnimator.mLastHidden)) {
                        winAnimator.setMoveAnimation(left, top);
                    }
                    if (this.mService.mAccessibilityController != null && displayId == 0) {
                        this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                    }
                    try {
                        w.mClient.moved(left, top);
                    } catch (RemoteException e) {
                    }
                    w.mMovedByResize = false;
                }
                w.mContentChanged = false;
                if (w.mHasSurface) {
                    boolean committed = winAnimator.commitFinishDrawingLocked();
                    if (isDefaultDisplay && committed) {
                        if (w.mAttrs.type == 2023) {
                            displayContent.pendingLayoutChanges |= 1;
                            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                                debugLayoutRepeats("dream and commitFinishDrawingLocked true", displayContent.pendingLayoutChanges);
                            }
                        }
                        if ((w.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                                Slog.v(TAG, "First draw done in potential wallpaper target " + w);
                            }
                            this.mWallpaperMayChange = true;
                            displayContent.pendingLayoutChanges |= 4;
                            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                                debugLayoutRepeats("wallpaper and commitFinishDrawingLocked true", displayContent.pendingLayoutChanges);
                            }
                        }
                    }
                    if (!(winAnimator.isAnimationStarting() || winAnimator.isWaitingForOpening())) {
                        winAnimator.computeShownFrameLocked();
                    }
                    winAnimator.setSurfaceBoundariesLocked(recoveringMemory);
                }
                AppWindowToken atoken = w.mAppToken;
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && atoken != null && w == atoken.startingWindow) {
                    Slog.d(TAG, "updateWindows: starting " + w + " isOnScreen=" + w.isOnScreen() + " allDrawn=" + atoken.allDrawn + " freezingScreen=" + atoken.mAppAnimator.freezingScreen);
                }
                if (!(atoken == null || (atoken.allDrawn && atoken.allDrawnExcludingSaved && !atoken.mAppAnimator.freezingScreen))) {
                    if (atoken.lastTransactionSequence != ((long) this.mService.mTransactionSequence)) {
                        atoken.lastTransactionSequence = (long) this.mService.mTransactionSequence;
                        atoken.numDrawnWindows = 0;
                        atoken.numInterestingWindows = 0;
                        atoken.numInterestingWindowsExcludingSaved = 0;
                        atoken.numDrawnWindowsExclusingSaved = 0;
                        atoken.startingDisplayed = false;
                    }
                    if (!atoken.allDrawn && w.mightAffectAllDrawn(false)) {
                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.v(TAG, "Eval win " + w + ": isDrawn=" + w.isDrawnLw() + ", isAnimationSet=" + winAnimator.isAnimationSet());
                            if (!w.isDrawnLw()) {
                                Slog.v(TAG, "Not displayed: s=" + winAnimator.mSurfaceController + " pv=" + w.mPolicyVisibility + " mDrawState=" + winAnimator.drawStateToString() + " ah=" + w.mAttachedHidden + " th=" + atoken.hiddenRequested + " a=" + winAnimator.mAnimating);
                            }
                        }
                        if (w != atoken.startingWindow) {
                            if (w.isInteresting()) {
                                atoken.numInterestingWindows++;
                                if (w.isDrawnLw()) {
                                    atoken.numDrawnWindows++;
                                    if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                        Slog.v(TAG, "tokenMayBeDrawn: " + atoken + " w=" + w + " numInteresting=" + atoken.numInterestingWindows + " freezingScreen=" + atoken.mAppAnimator.freezingScreen + " mAppFreezing=" + w.mAppFreezing);
                                    }
                                    updateAllDrawn = true;
                                }
                            }
                        } else if (w.isDrawnLw()) {
                            this.mService.mH.sendEmptyMessage(50);
                            atoken.startingDisplayed = true;
                        }
                    }
                    if (!atoken.allDrawnExcludingSaved && w.mightAffectAllDrawn(true) && w != atoken.startingWindow && w.isInteresting()) {
                        atoken.numInterestingWindowsExcludingSaved++;
                        if (w.isDrawnLw() && !w.isAnimatingWithSavedSurface()) {
                            atoken.numDrawnWindowsExclusingSaved++;
                            if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.v(TAG, "tokenMayBeDrawnExcludingSaved: " + atoken + " w=" + w + " numInteresting=" + atoken.numInterestingWindowsExcludingSaved + " freezingScreen=" + atoken.mAppAnimator.freezingScreen + " mAppFreezing=" + w.mAppFreezing);
                            }
                            updateAllDrawn = true;
                        }
                    }
                }
                if (isDefaultDisplay && someoneLosingFocus && w == this.mService.mCurrentFocus && w.isDisplayedLw()) {
                    focusDisplayed = true;
                }
                this.mService.updateResizingWindows(w);
            }
            this.mService.mDisplayManagerInternal.setDisplayProperties(displayId, this.mDisplayHasContent, this.mPreferredRefreshRate, this.mPreferredModeId, true);
            this.mService.getDisplayContentLocked(displayId).stopDimmingIfNeeded();
            if (updateAllDrawn) {
                updateAllDrawnLocked(displayContent);
            }
        }
        if (focusDisplayed) {
            this.mService.mH.sendEmptyMessage(3);
        }
        this.mService.mDisplayManagerInternal.performTraversalInTransactionFromWindowManager();
    }

    boolean isInLayout() {
        return this.mInLayout;
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x01e4  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x0419  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0287  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void performLayoutLockedInner(DisplayContent displayContent, boolean initial, boolean updateInputWindows) {
        if (displayContent.layoutNeeded) {
            int i;
            WindowState win;
            displayContent.layoutNeeded = false;
            WindowList windows = displayContent.getWindowList();
            boolean isDefaultDisplay = displayContent.isDefaultDisplay;
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            if (this.mService.mInputConsumer != null) {
                this.mService.mInputConsumer.layout(dw, dh);
            }
            if (this.mService.mWallpaperInputConsumer != null) {
                this.mService.mWallpaperInputConsumer.layout(dw, dh);
            }
            int N = windows.size();
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v(TAG, "-------------------------------------");
                Slog.v(TAG, "performLayout: needed=" + displayContent.layoutNeeded + " dw=" + dw + " dh=" + dh);
            }
            boolean is169Compat = false;
            this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, dw, dh, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
            if (isDefaultDisplay) {
                this.mService.mSystemDecorLayer = this.mService.mPolicy.getSystemDecorLayerLw();
                this.mService.mScreenRect.set(0, 0, dw, dh);
            }
            this.mService.mPolicy.getContentRectLw(this.mTmpContentRect);
            displayContent.resize(this.mTmpContentRect);
            int seq = this.mService.mLayoutSeq + 1;
            if (seq < 0) {
                seq = 0;
            }
            this.mService.mLayoutSeq = seq;
            boolean behindDream = false;
            int topAttached = -1;
            for (i = N - 1; i >= 0; i--) {
                boolean gone;
                AppWindowToken atoken;
                win = (WindowState) windows.get(i);
                if (behindDream) {
                    if (this.mService.mPolicy.canBeForceHidden(win, win.mAttrs)) {
                        gone = true;
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT && !win.mLayoutAttached) {
                            Slog.v(TAG, "1ST PASS " + win + ": gone=" + gone + " mHaveFrame=" + win.mHaveFrame + " mLayoutAttached=" + win.mLayoutAttached + " screen changed=" + win.isConfigChanged());
                            atoken = win.mAppToken;
                            if (gone) {
                                Slog.v(TAG, "  VIS: mViewVisibility=" + win.mViewVisibility + " mRelayoutCalled=" + win.mRelayoutCalled + " hidden=" + win.mRootToken.hidden + " hiddenRequested=" + (atoken != null ? atoken.hiddenRequested : false) + " mAttachedHidden=" + win.mAttachedHidden);
                            } else {
                                Slog.v(TAG, "  GONE: mViewVisibility=" + win.mViewVisibility + " mRelayoutCalled=" + win.mRelayoutCalled + " hidden=" + win.mRootToken.hidden + " hiddenRequested=" + (atoken != null ? atoken.hiddenRequested : false) + " mAttachedHidden=" + win.mAttachedHidden);
                            }
                        }
                        if (!this.mService.isGoneForDismissDockedStack(win) && (!gone || !win.mHaveFrame || win.mLayoutNeeded || ((win.isConfigChanged() || win.setReportResizeHints()) && !win.isGoneForLayoutLw() && ((win.mAttrs.privateFlags & 1024) != 0 || (win.mHasSurface && win.mAppToken != null && win.mAppToken.layoutConfigChanges))))) {
                            if (win.mLayoutAttached) {
                                if (win != null && !win.isInMultiWindowMode() && win.isDisplayCompat()) {
                                    win.mSystemUiVisibility |= 16384;
                                    if (this.mService.mRotation == 0 || this.mService.mRotation == 2) {
                                        this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, dw, -3, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
                                    } else if (this.mService.mRotation == 1 || this.mService.mRotation == 3) {
                                        this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, -3, dh, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
                                    }
                                    if (this.mService.mInputMethodWindow != null) {
                                        this.mService.mPolicy.reLayoutInputMethod(this.mService.mInputMethodWindow, null);
                                    }
                                    is169Compat = true;
                                } else if (is169Compat && win != null && (win.getAttrs().packageName.contains(ActivityManagerService.OPPO_LAUNCHER) || win.getAttrs().packageName.contains("com.coloros.recents") || win.getAttrs().packageName.contains("com.android.systemui"))) {
                                    if (this.mService.mRotation == 0 || this.mService.mRotation == 2) {
                                        this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, dw, -2, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
                                    } else if (this.mService.mRotation == 1 || this.mService.mRotation == 3) {
                                        this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, -2, dh, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
                                    }
                                }
                                if (initial) {
                                    win.mContentChanged = false;
                                }
                                if (win.mAttrs.type == 2023) {
                                    behindDream = true;
                                }
                                win.mLayoutNeeded = false;
                                win.prelayout();
                                this.mService.mPolicy.layoutWindowLw(win, null);
                                win.mLayoutSeq = seq;
                                Task task = win.getTask();
                                if (task != null) {
                                    displayContent.mDimLayerController.updateDimLayer(task);
                                }
                                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                    Slog.v(TAG, "  LAYOUT: mFrame=" + win.mFrame + " mContainingFrame=" + win.mContainingFrame + " mDisplayFrame=" + win.mDisplayFrame);
                                }
                            } else if (topAttached < 0) {
                                topAttached = i;
                            }
                        }
                    }
                }
                gone = win.isGoneForLayoutLw();
                Slog.v(TAG, "1ST PASS " + win + ": gone=" + gone + " mHaveFrame=" + win.mHaveFrame + " mLayoutAttached=" + win.mLayoutAttached + " screen changed=" + win.isConfigChanged());
                atoken = win.mAppToken;
                if (gone) {
                }
                if (win.mLayoutAttached) {
                }
            }
            boolean attachedBehindDream = false;
            for (i = topAttached; i >= 0; i--) {
                win = (WindowState) windows.get(i);
                if (win.mLayoutAttached) {
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.v(TAG, "2ND PASS " + win + " mHaveFrame=" + win.mHaveFrame + " mViewVisibility=" + win.mViewVisibility + " mRelayoutCalled=" + win.mRelayoutCalled);
                    }
                    if (attachedBehindDream) {
                        if (this.mService.mPolicy.canBeForceHidden(win, win.mAttrs)) {
                        }
                    }
                    if ((win.mViewVisibility != 8 && win.mRelayoutCalled) || !win.mHaveFrame || win.mLayoutNeeded) {
                        if (initial) {
                            win.mContentChanged = false;
                        }
                        if (this.mService.mInputMethodWindow == win.mAttachedWindow && is169Compat) {
                            this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, dw, dh, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
                        }
                        win.mLayoutNeeded = false;
                        win.prelayout();
                        this.mService.mPolicy.layoutWindowLw(win, win.mAttachedWindow);
                        if (this.mService.mInputMethodWindow == win.mAttachedWindow && is169Compat) {
                            if (this.mService.mRotation == 0 || this.mService.mRotation == 2) {
                                this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, dw, -3, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
                            } else if (this.mService.mRotation == 1 || this.mService.mRotation == 3) {
                                this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, -3, dh, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
                            }
                        }
                        win.mLayoutSeq = seq;
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                            Slog.v(TAG, "  LAYOUT: mFrame=" + win.mFrame + " mContainingFrame=" + win.mContainingFrame + " mDisplayFrame=" + win.mDisplayFrame);
                        }
                    }
                } else if (win.mAttrs.type == 2023) {
                    attachedBehindDream = behindDream;
                }
            }
            this.mService.mInputMonitor.setUpdateInputWindowsNeededLw();
            if (updateInputWindows) {
                this.mService.mInputMonitor.updateInputWindowsLw(false);
            }
            this.mService.mPolicy.finishLayoutLw();
            this.mService.mH.sendEmptyMessage(41);
        }
    }

    private int handleAppTransitionReadyLocked(WindowList windows) {
        int appsCount = this.mService.mOpeningApps.size();
        if (!transitionGoodToGo(appsCount)) {
            return 0;
        }
        int i;
        AppWindowToken wtoken;
        AppWindowToken lowerWallpaperAppToken;
        AppWindowAnimator openingAppAnimator;
        AppWindowAnimator closingAppAnimator;
        Trace.traceBegin(4128, "AppTransitionReady");
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "**** GOOD TO GO, Callers=" + Debug.getCallers(6));
        } else if (WindowManagerService.DEBUG_WMS) {
            Slog.v(TAG, "**** GOOD TO GO");
        }
        int transit = this.mService.mAppTransition.getAppTransition();
        if (this.mService.mSkipAppTransitionAnimation) {
            transit = -1;
        }
        this.mService.mSkipAppTransitionAnimation = false;
        this.mService.mNoAnimationNotifyOnTransitionFinished.clear();
        this.mService.mH.removeMessages(13);
        this.mService.rebuildAppWindowListLocked();
        this.mWallpaperMayChange = false;
        LayoutParams animLp = null;
        int bestAnimLayer = -1;
        boolean fullscreenAnim = false;
        boolean voiceInteraction = false;
        for (i = 0; i < appsCount; i++) {
            wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i);
            if (!wtoken.toString().contains("com.tencent.mm/.plugin.luckymoney.ui")) {
                if (!ActivityThread.inCptWhiteList(CompatibilityHelper.MM_MONEY_LUCKY_CHECK, wtoken.toString())) {
                    wtoken.clearAnimatingFlags();
                }
            }
        }
        DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
        if ((displayContent.pendingLayoutChanges & 4) != 0 && this.mWallpaperControllerLocked.adjustWallpaperWindows()) {
            this.mService.mLayersController.assignLayersLocked(windows);
            displayContent.layoutNeeded = true;
        }
        WindowState lowerWallpaperTarget = this.mWallpaperControllerLocked.getLowerWallpaperTarget();
        WindowState upperWallpaperTarget = this.mWallpaperControllerLocked.getUpperWallpaperTarget();
        boolean openingAppHasWallpaper = false;
        boolean closingAppHasWallpaper = false;
        AppWindowToken upperWallpaperAppToken;
        if (lowerWallpaperTarget == null) {
            upperWallpaperAppToken = null;
            lowerWallpaperAppToken = null;
        } else {
            lowerWallpaperAppToken = lowerWallpaperTarget.mAppToken;
            upperWallpaperAppToken = upperWallpaperTarget.mAppToken;
        }
        int closingAppsCount = this.mService.mClosingApps.size();
        appsCount = closingAppsCount + this.mService.mOpeningApps.size();
        for (i = 0; i < appsCount; i++) {
            if (i < closingAppsCount) {
                wtoken = (AppWindowToken) this.mService.mClosingApps.valueAt(i);
                if (wtoken == lowerWallpaperAppToken || wtoken == upperWallpaperAppToken) {
                    closingAppHasWallpaper = true;
                }
            } else {
                wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i - closingAppsCount);
                if (wtoken == lowerWallpaperAppToken || wtoken == upperWallpaperAppToken) {
                    openingAppHasWallpaper = true;
                }
            }
            voiceInteraction |= wtoken.voiceInteraction;
            WindowState ws;
            if (wtoken.appFullscreen) {
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
        transit = maybeUpdateTransitToWallpaper(transit, openingAppHasWallpaper, closingAppHasWallpaper, lowerWallpaperTarget, upperWallpaperTarget);
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
        this.mService.mAppTransition.goodToGo(openingAppAnimator, closingAppAnimator, this.mService.mOpeningApps, this.mService.mClosingApps);
        this.mService.mAppTransition.postAnimationCallback();
        this.mService.mAppTransition.clear();
        this.mService.mOpeningApps.clear();
        this.mService.mClosingApps.clear();
        displayContent.layoutNeeded = true;
        if (windows == this.mService.getDefaultWindowListLocked() && !this.mService.moveInputMethodWindowsIfNeededLocked(true)) {
            this.mService.mLayersController.assignLayersLocked(windows);
        }
        this.mService.updateFocusedWindowLocked(2, true);
        this.mService.mFocusMayChange = false;
        this.mService.notifyActivityDrawnForKeyguard();
        Trace.traceEnd(4128);
        return 3;
    }

    private AppWindowToken handleOpeningApps(int transit, LayoutParams animLp, boolean voiceInteraction, int topClosingLayer) {
        AppWindowToken topOpeningApp = null;
        int appsCount = this.mService.mOpeningApps.size();
        int i = 0;
        while (i < appsCount) {
            int j;
            AppWindowToken wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i);
            AppWindowAnimator appAnimator = wtoken.mAppAnimator;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Now opening app" + wtoken);
            }
            if (!appAnimator.usingTransferredAnimation) {
                appAnimator.clearThumbnail();
                appAnimator.setNullAnimation();
            }
            wtoken.inPendingTransaction = false;
            if (!this.mService.setTokenVisibilityLocked(wtoken, animLp, true, transit, false, voiceInteraction)) {
                this.mService.mNoAnimationNotifyOnTransitionFinished.add(wtoken.token);
            }
            wtoken.updateReportedVisibilityLocked();
            wtoken.waitingToShow = false;
            appAnimator.mAllAppWinAnimators.clear();
            int windowsCount = wtoken.allAppWindows.size();
            for (j = 0; j < windowsCount; j++) {
                appAnimator.mAllAppWinAnimators.add(((WindowState) wtoken.allAppWindows.get(j)).mWinAnimator);
            }
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, ">>> OPEN TRANSACTION handleAppTransitionReadyLocked()");
            }
            SurfaceControl.openTransaction();
            try {
                this.mService.mAnimator.orAnimating(appAnimator.showAllWindowsLocked());
                WindowAnimator windowAnimator = this.mService.mAnimator;
                windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
                int topOpeningLayer = 0;
                if (animLp != null) {
                    int layer = -1;
                    for (j = 0; j < wtoken.allAppWindows.size(); j++) {
                        WindowState win = (WindowState) wtoken.allAppWindows.get(j);
                        if (win.mWinAnimator.mAnimLayer > layer) {
                            layer = win.mWinAnimator.mAnimLayer;
                        }
                    }
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
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION handleAppTransitionReadyLocked()");
                }
            }
        }
        return topOpeningApp;
    }

    private void handleClosingApps(int transit, LayoutParams animLp, boolean voiceInteraction, LayerAndToken layerAndToken) {
        int appsCount = this.mService.mClosingApps.size();
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = (AppWindowToken) this.mService.mClosingApps.valueAt(i);
            wtoken.markSavedSurfaceExiting();
            AppWindowAnimator appAnimator = wtoken.mAppAnimator;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Now closing app " + wtoken);
            }
            appAnimator.clearThumbnail();
            appAnimator.setNullAnimation();
            wtoken.inPendingTransaction = false;
            this.mService.setTokenVisibilityLocked(wtoken, animLp, false, transit, false, voiceInteraction);
            wtoken.updateReportedVisibilityLocked();
            wtoken.allDrawn = true;
            wtoken.deferClearAllDrawn = false;
            if (!(wtoken.startingWindow == null || wtoken.startingWindow.mAnimatingExit)) {
                this.mService.scheduleRemoveStartingWindowLocked(wtoken);
            }
            WindowAnimator windowAnimator = this.mService.mAnimator;
            windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
            if (animLp != null) {
                int layer = -1;
                for (int j = 0; j < wtoken.windows.size(); j++) {
                    WindowState win = (WindowState) wtoken.windows.get(j);
                    if (win.mWinAnimator.mAnimLayer > layer) {
                        layer = win.mWinAnimator.mAnimLayer;
                    }
                }
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

    private boolean transitionGoodToGo(int appsCount) {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || this.mService.mAppTransition.isTimeout()) {
            Slog.v(TAG, "Checking " + appsCount + " opening apps (frozen=" + this.mService.mDisplayFrozen + " timeout=" + this.mService.mAppTransition.isTimeout() + ")...");
        }
        if (this.mService.mAppTransition.isTimeout() && WindowManagerService.DEBUG_WMS) {
            try {
                final StringBuilder sb = new StringBuilder(1024);
                addAssertMessage(sb, appsCount);
                new Thread() {
                    public void run() {
                        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS || WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                            Slog.d(WindowSurfacePlacer.TAG, "--- time out show assert --- ");
                            OppoAssertTip.getInstance().requestShowAssertMessage(sb.toString());
                        }
                    }
                }.start();
            } catch (Exception e) {
                Slog.e(TAG, "Exception when show assert!", e);
            }
        }
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
        int reason = 3;
        if (this.mService.mAppTransition.isTimeout()) {
            this.mService.mH.obtainMessage(47, 3, 0).sendToTarget();
            return true;
        } else if (screenRotationAnimation != null && screenRotationAnimation.isAnimating() && this.mService.rotationNeedsUpdateLocked()) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Delaying app transition for screen rotation animation to finish");
            }
            return false;
        } else {
            for (int i = 0; i < appsCount; i++) {
                AppWindowToken wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i);
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "Check opening app=" + wtoken + ": allDrawn=" + wtoken.allDrawn + " startingDisplayed=" + wtoken.startingDisplayed + " startingMoved=" + wtoken.startingMoved + " isRelaunching()=" + wtoken.isRelaunching());
                }
                if (wtoken.isRelaunching()) {
                    return false;
                }
                boolean drawnBeforeRestoring = wtoken.allDrawn;
                wtoken.restoreSavedSurfaces();
                if (!wtoken.allDrawn && !wtoken.startingDisplayed && !wtoken.startingMoved) {
                    return false;
                }
                if (!wtoken.allDrawn) {
                    reason = 1;
                } else if (drawnBeforeRestoring) {
                    reason = 2;
                } else {
                    reason = 0;
                }
            }
            if (this.mService.mAppTransition.isFetchingAppTransitionsSpecs()) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "isFetchingAppTransitionSpecs=true");
                }
                return false;
            }
            boolean wallpaperReady;
            if (this.mWallpaperControllerLocked.isWallpaperVisible()) {
                wallpaperReady = this.mWallpaperControllerLocked.wallpaperTransitionReady();
            } else {
                wallpaperReady = true;
            }
            if (!wallpaperReady) {
                return false;
            }
            this.mService.mH.obtainMessage(47, reason, 0).sendToTarget();
            return true;
        }
    }

    private int maybeUpdateTransitToWallpaper(int transit, boolean openingAppHasWallpaper, boolean closingAppHasWallpaper, WindowState lowerWallpaperTarget, WindowState upperWallpaperTarget) {
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        WindowState oldWallpaper = this.mWallpaperControllerLocked.isWallpaperTargetAnimating() ? null : wallpaperTarget;
        ArraySet<AppWindowToken> openingApps = this.mService.mOpeningApps;
        ArraySet<AppWindowToken> closingApps = this.mService.mClosingApps;
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "New wallpaper target=" + wallpaperTarget + ", oldWallpaper=" + oldWallpaper + ", lower target=" + lowerWallpaperTarget + ", upper target=" + upperWallpaperTarget + ", openingApps=" + openingApps + ", closingApps=" + closingApps);
        }
        this.mService.mAnimateWallpaperWithTarget = false;
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
        } else if (oldWallpaper != null && !this.mService.mOpeningApps.isEmpty() && !openingApps.contains(oldWallpaper.mAppToken) && closingApps.contains(oldWallpaper.mAppToken)) {
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
        return transit;
    }

    private void handleNotObscuredLocked(WindowState w, DisplayInfo dispInfo) {
        LayoutParams attrs = w.mAttrs;
        if (attrs.type == 2004) {
            this.mService.mKeyguard = w;
        }
        int attrFlags = attrs.flags;
        boolean canBeSeen = w.isDisplayedLw();
        int privateflags = attrs.privateFlags;
        if (canBeSeen && w.isObscuringFullscreen(dispInfo)) {
            if (!this.mObscured) {
                this.mObsuringWindow = w;
            }
            this.mObscured = true;
        }
        if (w.mHasSurface && canBeSeen) {
            if ((attrFlags & 128) != 0) {
                this.mHoldScreen = w.mSession;
                this.mHoldScreenWindow = w;
            } else if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON && w == this.mService.mLastWakeLockHoldingWindow) {
                Slog.d("DebugKeepScreenOn", "handleNotObscuredLocked: " + w + " was holding " + "screen wakelock but no longer has FLAG_KEEP_SCREEN_ON!!! called by" + Debug.getCallers(10));
            }
            if (!this.mSyswin && w.mAttrs.screenBrightness >= OppoBrightUtils.MIN_LUX_LIMITI && this.mScreenBrightness < OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mScreenBrightness = w.mAttrs.screenBrightness;
            }
            if (!this.mSyswin && w.mAttrs.buttonBrightness >= OppoBrightUtils.MIN_LUX_LIMITI && this.mButtonBrightness < OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mButtonBrightness = w.mAttrs.buttonBrightness;
            }
            if (!this.mSyswin && w.mAttrs.userActivityTimeout >= 0 && this.mUserActivityTimeout < 0) {
                this.mUserActivityTimeout = w.mAttrs.userActivityTimeout;
            }
            int type = attrs.type;
            if (type == 2008 || type == 2010 || (attrs.privateFlags & 1024) != 0) {
                this.mSyswin = true;
            }
            DisplayContent displayContent = w.getDisplayContent();
            if (displayContent != null && displayContent.isDefaultDisplay) {
                if (type == 2023 || (attrs.privateFlags & 1024) != 0) {
                    this.mObscureApplicationContentOnSecondaryDisplays = true;
                }
                this.mDisplayHasContent = true;
            } else if (displayContent != null && (!this.mObscureApplicationContentOnSecondaryDisplays || (this.mObscured && type == 2009))) {
                this.mDisplayHasContent = true;
            }
            if (this.mPreferredRefreshRate == OppoBrightUtils.MIN_LUX_LIMITI && w.mAttrs.preferredRefreshRate != OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mPreferredRefreshRate = w.mAttrs.preferredRefreshRate;
            }
            if (this.mPreferredModeId == 0 && w.mAttrs.preferredDisplayModeId != 0) {
                this.mPreferredModeId = w.mAttrs.preferredDisplayModeId;
            }
            if ((DumpState.DUMP_DOMAIN_PREFERRED & privateflags) != 0) {
                this.mSustainedPerformanceModeCurrent = true;
            }
        }
    }

    private void updateAllDrawnLocked(DisplayContent displayContent) {
        ArrayList<TaskStack> stacks = displayContent.getStacks();
        for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ArrayList<Task> tasks = ((TaskStack) stacks.get(stackNdx)).getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                    int numInteresting;
                    AppWindowToken wtoken = (AppWindowToken) tokens.get(tokenNdx);
                    if (!wtoken.allDrawn) {
                        numInteresting = wtoken.numInterestingWindows;
                        if (numInteresting > 0 && wtoken.numDrawnWindows >= numInteresting) {
                            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG, "allDrawn: " + wtoken + " interesting=" + numInteresting + " drawn=" + wtoken.numDrawnWindows);
                            }
                            wtoken.allDrawn = true;
                            displayContent.layoutNeeded = true;
                            this.mService.mH.obtainMessage(32, wtoken.token).sendToTarget();
                        }
                    }
                    if (!wtoken.allDrawnExcludingSaved) {
                        numInteresting = wtoken.numInterestingWindowsExcludingSaved;
                        if (numInteresting > 0 && wtoken.numDrawnWindowsExclusingSaved >= numInteresting) {
                            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG, "allDrawnExcludingSaved: " + wtoken + " interesting=" + numInteresting + " drawn=" + wtoken.numDrawnWindowsExclusingSaved);
                            }
                            wtoken.allDrawnExcludingSaved = true;
                            displayContent.layoutNeeded = true;
                            if (wtoken.isAnimatingInvisibleWithSavedSurface() && !this.mService.mFinishedEarlyAnim.contains(wtoken)) {
                                this.mService.mFinishedEarlyAnim.add(wtoken);
                            }
                        }
                    }
                }
            }
        }
    }

    private static int toBrightnessOverride(float value) {
        return (int) (((float) PowerManager.BRIGHTNESS_MULTIBITS_ON) * value);
    }

    private void processApplicationsAnimatingInPlace(int transit) {
        if (transit == 17) {
            WindowState win = this.mService.findFocusedWindowLocked(this.mService.getDefaultDisplayContentLocked());
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
                appAnimator.mAllAppWinAnimators.clear();
                int N = wtoken.allAppWindows.size();
                for (int j = 0; j < N; j++) {
                    appAnimator.mAllAppWinAnimators.add(((WindowState) wtoken.allAppWindows.get(j)).mWinAnimator);
                }
                WindowAnimator windowAnimator = this.mService.mAnimator;
                windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
                this.mService.mAnimator.orAnimating(appAnimator.showAllWindowsLocked());
            }
        }
    }

    private void createThumbnailAppAnimator(int transit, AppWindowToken appToken, int openingLayer, int closingLayer) {
        AppWindowAnimator openingAppAnimator = appToken == null ? null : appToken.mAppAnimator;
        if (openingAppAnimator != null && openingAppAnimator.animation != null) {
            int taskId = appToken.mTask.mTaskId;
            Bitmap thumbnailHeader = this.mService.mAppTransition.getAppTransitionThumbnailHeader(taskId);
            if (thumbnailHeader == null || thumbnailHeader.getConfig() == Config.ALPHA_8) {
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
                SurfaceControl surfaceControl = new SurfaceControl(this.mService.mFxSession, "thumbnail anim", dirty.width(), dirty.height(), -3, 4);
                surfaceControl.setLayerStack(display.getLayerStack());
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i(TAG, "  THUMBNAIL " + surfaceControl + ": CREATE");
                }
                Surface drawSurface = new Surface();
                drawSurface.copyFrom(surfaceControl);
                Canvas c = drawSurface.lockCanvas(dirty);
                c.drawBitmap(thumbnailHeader, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, null);
                drawSurface.unlockCanvasAndPost(c);
                drawSurface.release();
                if (this.mService.mAppTransition.isNextThumbnailTransitionAspectScaled()) {
                    Rect appRect;
                    WindowState win = appToken.findMainWindow();
                    if (win != null) {
                        appRect = win.getContentFrameLw();
                    } else {
                        appRect = new Rect(0, 0, displayInfo.appWidth, displayInfo.appHeight);
                    }
                    anim = this.mService.mAppTransition.createThumbnailAspectScaleAnimationLocked(appRect, win != null ? win.mContentInsets : null, thumbnailHeader, taskId, this.mService.mCurConfiguration.uiMode, this.mService.mCurConfiguration.orientation);
                    openingAppAnimator.thumbnailForceAboveLayer = Math.max(openingLayer, closingLayer);
                    openingAppAnimator.deferThumbnailDestruction = !this.mService.mAppTransition.isNextThumbnailTransitionScaleUp();
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

    boolean copyAnimToLayoutParamsLocked() {
        boolean doRequest = false;
        int bulkUpdateParams = this.mService.mAnimator.mBulkUpdateParams;
        if ((bulkUpdateParams & 1) != 0) {
            this.mUpdateRotation = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 2) != 0) {
            this.mWallpaperMayChange = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 4) != 0) {
            this.mWallpaperForceHidingChanged = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 8) == 0) {
            this.mOrientationChangeComplete = false;
        } else {
            this.mOrientationChangeComplete = true;
            this.mLastWindowFreezeSource = this.mService.mAnimator.mLastWindowFreezeSource;
            if (this.mService.mWindowsFreezingScreen != 0) {
                doRequest = true;
            }
        }
        if ((bulkUpdateParams & 16) != 0) {
            this.mService.mTurnOnScreen = true;
        }
        if ((bulkUpdateParams & 32) != 0) {
            this.mWallpaperActionPending = true;
        }
        return doRequest;
    }

    void requestTraversal() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            this.mService.mH.sendEmptyMessage(4);
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
        pw.print(prefix);
        pw.print("mTraversalScheduled=");
        pw.println(this.mTraversalScheduled);
        pw.print(prefix);
        pw.print("mHoldScreenWindow=");
        pw.println(this.mHoldScreenWindow);
        pw.print(prefix);
        pw.print("mObsuringWindow=");
        pw.println(this.mObsuringWindow);
    }

    private void addAssertMessage(StringBuilder sb, int appsCount) {
        sb.append("app transition time out after 5s! \n");
        sb.append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date(System.currentTimeMillis())));
        sb.append("\n");
        for (int wtokenNdx = 0; wtokenNdx < appsCount; wtokenNdx++) {
            AppWindowToken wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(wtokenNdx);
            Slog.d(TAG, "addAssertMessage mService.mOpeningApps " + this.mService.mOpeningApps);
            Slog.d(TAG, "addAssertMessage wtoken.allAppWindows " + wtoken.allAppWindows);
            if (wtoken.allAppWindows.isEmpty()) {
                sb.append("\n");
                sb.append(wtoken);
                sb.append("\n");
                sb.append("No window,please first feedback bug to APP");
                sb.append("\n");
                sb.append("Want more explanation can from ruyu.wu of android team,thank you!\n");
                return;
            }
            for (int winNdx = wtoken.allAppWindows.size() - 1; winNdx >= 0; winNdx--) {
                WindowState w = (WindowState) wtoken.allAppWindows.get(winNdx);
                Slog.d(TAG, "addAssertMessage w " + w + " " + w.mHasSurface + " " + w.isInteresting());
                if (!(w == null || w == wtoken.startingWindow || !w.isInteresting() || w.mWinAnimator == null)) {
                    Slog.d(TAG, "w.mWinAnimator.mDrawState " + w.mWinAnimator.mDrawState + " " + w.isReadyForDisplayIgnoringKeyguard());
                    if (w.mWinAnimator.mDrawState == 0 || w.mWinAnimator.mDrawState == 1) {
                        sb.append("\n");
                        sb.append(w);
                        sb.append("\n");
                        sb.append(wtoken);
                        sb.append("\n");
                        sb.append("Not finish drawed,please first feedback bug to APP");
                        sb.append("\n");
                        sb.append("Want more explanation can from ruyu.wu of android team,thank you!\n");
                        sb.append("\n");
                        return;
                    }
                    sb.append(w);
                    sb.append("\n");
                    sb.append("mHasSurface: ");
                    sb.append(w.mHasSurface);
                    sb.append("\n");
                    sb.append("isReadyForDisplay: ");
                    sb.append(w.isReadyForDisplayIgnoringKeyguard());
                    sb.append("\n");
                    sb.append("mDrawState: ");
                    sb.append(w.mWinAnimator.mDrawState);
                    sb.append("\n");
                }
            }
            if (!false) {
                sb.append(wtoken);
                sb.append("\n");
                sb.append("allDrawn=");
                sb.append(wtoken.allDrawn);
                sb.append("\n");
            }
        }
        sb.append("Please send this log to ruyu.wu of android team, thank you!\n");
    }
}
