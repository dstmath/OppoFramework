package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Slog;
import android.view.IApplicationToken;
import android.view.SurfaceControl;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.StartingSurface;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.am.ActivityManagerService;
import com.android.server.input.InputApplicationHandle;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;

class AppWindowToken extends WindowToken implements AppFreezeListener {
    private static final String TAG = "WindowManager";
    boolean allDrawn;
    boolean allDrawnExcludingSaved;
    final IApplicationToken appToken;
    boolean deferClearAllDrawn;
    boolean firstWindowDrawn;
    boolean hiddenRequested;
    boolean inPendingTransaction;
    boolean layoutConfigChanges;
    private boolean mAlwaysFocusable;
    final AppWindowAnimator mAppAnimator;
    boolean mAppStopped;
    private final Rect mBounds;
    private boolean mCanTurnScreenOn;
    private boolean mClientHidden;
    boolean mCloseFromFreeform;
    boolean mDeferHidingClient;
    private boolean mDisablePreviewScreenshots;
    boolean mEnteringAnimation;
    private boolean mFillsParent;
    boolean mFinishing;
    boolean mFromFreeform;
    ArrayDeque<Rect> mFrozenBounds;
    ArrayDeque<Configuration> mFrozenMergedConfig;
    boolean mHasSnapShot;
    private boolean mHiddenSetFromTransferredStartingWindow;
    final InputApplicationHandle mInputApplicationHandle;
    long mInputDispatchingTimeoutNanos;
    boolean mIsExiting;
    private boolean mLastContainsDismissKeyguardWindow;
    private boolean mLastContainsShowWhenLockedWindow;
    Task mLastParent;
    private long mLastTransactionSequence;
    boolean mLaunchTaskBehind;
    private int mNumDrawnWindows;
    private int mNumDrawnWindowsExcludingSaved;
    private int mNumInterestingWindows;
    private int mNumInterestingWindowsExcludingSaved;
    private int mPendingRelaunchCount;
    private boolean mRemovingFromDisplay;
    private boolean mReparenting;
    private final UpdateReportedVisibilityResults mReportedVisibilityResults;
    long mResumeLauncherTime;
    int mRotationAnimationHint;
    boolean mShowForAllUsers;
    boolean mSplitStoped;
    int mTargetSdk;
    final boolean mVoiceInteraction;
    boolean removed;
    private boolean reportedDrawn;
    boolean reportedVisible;
    StartingData startingData;
    boolean startingDisplayed;
    boolean startingMoved;
    StartingSurface startingSurface;
    WindowState startingWindow;

    AppWindowToken(WindowManagerService service, IApplicationToken token, boolean voiceInteraction, DisplayContent dc, long inputDispatchingTimeoutNanos, boolean fullscreen, boolean showForAllUsers, int targetSdk, int orientation, int rotationAnimationHint, int configChanges, boolean launchTaskBehind, boolean alwaysFocusable, AppWindowContainerController controller, Configuration overrideConfig, Rect bounds) {
        this(service, token, voiceInteraction, dc, fullscreen, overrideConfig, bounds);
        setController(controller);
        this.mInputDispatchingTimeoutNanos = inputDispatchingTimeoutNanos;
        this.mShowForAllUsers = showForAllUsers;
        this.mTargetSdk = targetSdk;
        this.mOrientation = orientation;
        this.layoutConfigChanges = (configChanges & 1152) != 0;
        this.mLaunchTaskBehind = launchTaskBehind;
        this.mAlwaysFocusable = alwaysFocusable;
        this.mRotationAnimationHint = rotationAnimationHint;
        this.hidden = true;
        this.hiddenRequested = true;
    }

    AppWindowToken(WindowManagerService service, IApplicationToken token, boolean voiceInteraction, DisplayContent dc, boolean fillsParent, Configuration overrideConfig, Rect bounds) {
        IBinder iBinder = null;
        if (token != null) {
            iBinder = token.asBinder();
        }
        super(service, iBinder, 2, true, dc, false);
        this.mRemovingFromDisplay = false;
        this.mLastTransactionSequence = Long.MIN_VALUE;
        this.mReportedVisibilityResults = new UpdateReportedVisibilityResults();
        this.mBounds = new Rect();
        this.mFrozenBounds = new ArrayDeque();
        this.mFrozenMergedConfig = new ArrayDeque();
        this.mFromFreeform = false;
        this.mCloseFromFreeform = false;
        this.mResumeLauncherTime = 0;
        this.mCanTurnScreenOn = true;
        this.appToken = token;
        this.mVoiceInteraction = voiceInteraction;
        this.mFillsParent = fillsParent;
        this.mInputApplicationHandle = new InputApplicationHandle(this);
        this.mAppAnimator = new AppWindowAnimator(this, service);
        if (overrideConfig != null) {
            onOverrideConfigurationChanged(overrideConfig);
        }
        if (bounds != null) {
            this.mBounds.set(bounds);
        }
    }

    void onOverrideConfigurationChanged(Configuration overrideConfiguration, Rect bounds) {
        onOverrideConfigurationChanged(overrideConfiguration);
        if (!this.mBounds.equals(bounds)) {
            this.mBounds.set(bounds);
            onResize();
        }
    }

    void getBounds(Rect outBounds) {
        outBounds.set(this.mBounds);
    }

    boolean hasBounds() {
        return this.mBounds.isEmpty() ^ 1;
    }

    void onFirstWindowDrawn(WindowState win, WindowStateAnimator winAnimator) {
        this.firstWindowDrawn = true;
        removeDeadWindows();
        if (this.startingWindow != null) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "Finish starting " + win.mToken + ": first real window is shown, no animation");
            }
            winAnimator.clearAnimation();
            if (getController() != null) {
                getController().removeStartingWindow();
            }
        }
        updateReportedVisibilityLocked();
    }

    void updateReportedVisibilityLocked() {
        if (this.appToken != null) {
            int count = this.mChildren.size();
            this.mReportedVisibilityResults.reset();
            for (int i = 0; i < count; i++) {
                ((WindowState) this.mChildren.get(i)).updateReportedVisibility(this.mReportedVisibilityResults);
            }
            int numInteresting = this.mReportedVisibilityResults.numInteresting;
            int numVisible = this.mReportedVisibilityResults.numVisible;
            int numDrawn = this.mReportedVisibilityResults.numDrawn;
            boolean nowGone = this.mReportedVisibilityResults.nowGone;
            boolean nowDrawn = numInteresting > 0 && numDrawn >= numInteresting;
            boolean nowVisible = (numInteresting <= 0 || numVisible < numInteresting) ? false : this.hidden ^ 1;
            if (!nowGone) {
                if (!nowDrawn) {
                    nowDrawn = this.reportedDrawn;
                }
                if (!nowVisible) {
                    nowVisible = this.reportedVisible;
                }
            }
            AppWindowContainerController controller = getController();
            if (nowDrawn != this.reportedDrawn) {
                if (nowDrawn && controller != null) {
                    controller.reportWindowsDrawn();
                }
                this.reportedDrawn = nowDrawn;
            }
            if (nowVisible != this.reportedVisible) {
                this.reportedVisible = nowVisible;
                if (controller != null) {
                    if (nowVisible) {
                        controller.reportWindowsVisible();
                    } else {
                        controller.reportWindowsGone();
                    }
                }
            }
        }
    }

    boolean isClientHidden() {
        return this.mClientHidden;
    }

    void setClientHidden(boolean hideClient) {
        if (this.mClientHidden != hideClient && (!hideClient || !this.mDeferHidingClient)) {
            this.mClientHidden = hideClient;
            sendAppVisibilityToClients();
        }
    }

    boolean setVisibility(LayoutParams lp, boolean visible, int transit, boolean performLayout, boolean isVoiceInteraction) {
        int i;
        boolean delayed = false;
        this.inPendingTransaction = false;
        this.mHiddenSetFromTransferredStartingWindow = false;
        setClientHidden(visible ^ 1);
        boolean visibilityChanged = false;
        if (this.hidden == visible || ((this.hidden && this.mIsExiting) || (visible && waitingForReplacement()))) {
            AccessibilityController accessibilityController = this.mService.mAccessibilityController;
            int changed = false;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Changing app " + this + " hidden=" + this.hidden + " performLayout=" + performLayout);
            }
            boolean runningAppAnimation = false;
            if (this.mAppAnimator.animation == AppWindowAnimator.sDummyAnimation) {
                this.mAppAnimator.setNullAnimation();
            }
            if (transit != -1) {
                if (this.mService.applyAnimationLocked(this, lp, transit, visible, isVoiceInteraction)) {
                    runningAppAnimation = true;
                    delayed = true;
                }
                WindowState window = findMainWindow();
                if (!(window == null || accessibilityController == null || getDisplayContent().getDisplayId() != 0)) {
                    accessibilityController.onAppWindowTransitionLocked(window, transit);
                }
                changed = true;
            }
            for (i = 0; i < this.mChildren.size(); i++) {
                changed |= ((WindowState) this.mChildren.get(i)).onAppVisibilityChanged(visible, runningAppAnimation);
            }
            boolean z = visible ^ 1;
            this.hiddenRequested = z;
            this.hidden = z;
            visibilityChanged = true;
            if (visible) {
                if (!(this.startingWindow == null || (this.startingWindow.isDrawnLw() ^ 1) == 0)) {
                    this.startingWindow.mPolicyVisibility = false;
                    this.startingWindow.mPolicyVisibilityAfterAnim = false;
                }
                WindowManagerService windowManagerService = this.mService;
                windowManagerService.getClass();
                forAllWindows((Consumer) new -$Lambda$YIZfR4m-B8z_tYbP2x4OJ3o7OYE((byte) 2, windowManagerService), true);
            } else {
                stopFreezingScreen(true, true);
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "setVisibility: " + this + ": hidden=" + this.hidden + " hiddenRequested=" + this.hiddenRequested);
            }
            if (changed != 0) {
                this.mService.mInputMonitor.setUpdateInputWindowsNeededLw();
                if (performLayout) {
                    this.mService.updateFocusedWindowLocked(3, false);
                    this.mService.mWindowPlacerLocked.performSurfacePlacement();
                }
                this.mService.mInputMonitor.updateInputWindowsLw(false);
            }
        }
        if (this.mAppAnimator.animation != null) {
            delayed = true;
        }
        for (i = this.mChildren.size() - 1; i >= 0 && (delayed ^ 1) != 0; i--) {
            if (((WindowState) this.mChildren.get(i)).isWindowAnimationSet()) {
                delayed = true;
            }
        }
        if (visibilityChanged) {
            if (visible && (delayed ^ 1) != 0) {
                this.mEnteringAnimation = true;
                this.mService.mActivityManagerAppTransitionNotifier.onAppTransitionFinishedLocked(this.token);
            }
            if (!(!this.hidden || (delayed ^ 1) == 0 || (this.mService.mAppTransition.isTransitionSet() ^ 1) == 0)) {
                SurfaceControl.openTransaction();
                for (i = this.mChildren.size() - 1; i >= 0; i--) {
                    ((WindowState) this.mChildren.get(i)).mWinAnimator.hide("immediately hidden");
                }
                SurfaceControl.closeTransaction();
            }
            if (!(this.mService.mClosingApps.contains(this) || (this.mService.mOpeningApps.contains(this) ^ 1) == 0)) {
                this.mService.getDefaultDisplayContentLocked().getDockedDividerController().notifyAppVisibilityChanged();
                this.mService.mTaskSnapshotController.notifyAppVisibilityChanged(this, visible);
            }
        }
        return delayed;
    }

    WindowState getTopFullscreenWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.mChildren.get(i);
            if (win != null && win.mAttrs.isFullscreen()) {
                return win;
            }
        }
        return null;
    }

    WindowState findMainWindow() {
        WindowState candidate = null;
        int j = this.mChildren.size();
        while (j > 0) {
            j--;
            WindowState win = (WindowState) this.mChildren.get(j);
            int type = win.mAttrs.type;
            if (type == 1 || type == 3) {
                if (!win.mAnimatingExit) {
                    return win;
                }
                candidate = win;
            }
        }
        return candidate;
    }

    boolean windowsAreFocusable() {
        return !StackId.canReceiveKeys(getTask().mStack.mStackId) ? this.mAlwaysFocusable : true;
    }

    AppWindowContainerController getController() {
        WindowContainerController controller = super.getController();
        return controller != null ? (AppWindowContainerController) controller : null;
    }

    boolean isVisible() {
        return this.hidden ^ 1;
    }

    void removeImmediately() {
        onRemovedFromDisplay();
        super.removeImmediately();
    }

    void removeIfPossible() {
        this.mIsExiting = false;
        removeAllWindowsIfPossible();
        removeImmediately();
    }

    boolean checkCompleteDeferredRemoval() {
        if (this.mIsExiting) {
            removeIfPossible();
        }
        return super.checkCompleteDeferredRemoval();
    }

    void onRemovedFromDisplay() {
        if (!this.mRemovingFromDisplay) {
            this.mRemovingFromDisplay = true;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Removing app token: " + this);
            }
            boolean delayed = setVisibility(null, false, -1, true, this.mVoiceInteraction);
            this.mService.mOpeningApps.remove(this);
            this.mService.mUnknownAppVisibilityController.appRemovedOrHidden(this);
            this.mService.mTaskSnapshotController.onAppRemoved(this);
            this.waitingToShow = false;
            if (this.mService.mClosingApps.contains(this)) {
                delayed = true;
            } else if (this.mService.mAppTransition.isTransitionSet()) {
                this.mService.mClosingApps.add(this);
                delayed = true;
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Removing app " + this + " delayed=" + delayed + " animation=" + this.mAppAnimator.animation + " animating=" + this.mAppAnimator.animating);
            }
            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT) {
                Slog.v(TAG, "removeAppToken: " + this + " delayed=" + delayed + " Callers=" + Debug.getCallers(4));
            }
            if (!(this.startingData == null || getController() == null)) {
                getController().removeStartingWindow();
            }
            if (this.mAppAnimator.animating) {
                this.mService.mNoAnimationNotifyOnTransitionFinished.add(this.token);
            }
            TaskStack stack = getStack();
            if (!delayed || (isEmpty() ^ 1) == 0) {
                this.mAppAnimator.clearAnimation();
                this.mAppAnimator.animating = false;
                if (stack != null) {
                    stack.mExitingAppTokens.remove(this);
                }
                removeIfPossible();
            } else {
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT) {
                    Slog.v(TAG, "removeAppToken make exiting: " + this);
                }
                if (stack != null) {
                    stack.mExitingAppTokens.add(this);
                }
                this.mIsExiting = true;
            }
            this.removed = true;
            stopFreezingScreen(true, true);
            if (this.mService.mFocusedApp == this) {
                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                    Slog.v(TAG, "Removing focused app token:" + this);
                }
                this.mService.mFocusedApp = null;
                this.mService.updateFocusedWindowLocked(0, true);
                this.mService.mInputMonitor.setFocusedAppLw(null);
            }
            if (!delayed) {
                updateReportedVisibilityLocked();
            }
            this.mRemovingFromDisplay = false;
        }
    }

    void clearAnimatingFlags() {
        boolean wallpaperMightChange = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            wallpaperMightChange |= ((WindowState) this.mChildren.get(i)).clearAnimatingFlags();
        }
        if (wallpaperMightChange) {
            requestUpdateWallpaperIfNeeded();
        }
    }

    void destroySurfaces() {
        destroySurfaces(false);
    }

    private void destroySurfaces(boolean cleanupOnResume) {
        boolean destroyedSomething = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            destroyedSomething |= ((WindowState) this.mChildren.get(i)).destroySurface(cleanupOnResume, this.mAppStopped);
        }
        if (destroyedSomething) {
            getDisplayContent().assignWindowLayers(true);
        }
    }

    void notifyAppResumed(boolean wasStopped) {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v(TAG, "notifyAppResumed: wasStopped=" + wasStopped + " " + this);
        }
        if (toString().contains("com.oppo.launcher/.Launcher")) {
            if (this.mService.mPolicy.isGestureAnimSupport()) {
                this.mService.mPolicy.handleOpeningSpecialApp(2, ActivityManagerService.OPPO_LAUNCHER);
            }
            this.mResumeLauncherTime = SystemClock.uptimeMillis();
        }
        this.mAppStopped = false;
        this.mSplitStoped = false;
        setCanTurnScreenOn(true);
        if (!wasStopped) {
            destroySurfaces(true);
        }
    }

    void notifyAppStopped() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v(TAG, "notifyAppStopped: " + this);
        }
        this.mAppStopped = true;
        this.mSplitStoped = true;
        destroySurfaces();
        if (getController() != null) {
            getController().removeStartingWindow();
        }
    }

    boolean shouldSaveSurface() {
        return this.allDrawn;
    }

    private boolean canRestoreSurfaces() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).canRestoreSurface()) {
                return true;
            }
        }
        return false;
    }

    private void clearWasVisibleBeforeClientHidden() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).clearWasVisibleBeforeClientHidden();
        }
    }

    boolean isAnimatingInvisibleWithSavedSurface() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).isAnimatingInvisibleWithSavedSurface()) {
                return true;
            }
        }
        return false;
    }

    void stopUsingSavedSurfaceLocked() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).stopUsingSavedSurface();
        }
        destroySurfaces();
    }

    void markSavedSurfaceExiting() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).markSavedSurfaceExiting();
        }
    }

    void restoreSavedSurfaceForInterestingWindows() {
        boolean z = false;
        if (canRestoreSurfaces()) {
            int interestingNotDrawn = -1;
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                interestingNotDrawn = ((WindowState) this.mChildren.get(i)).restoreSavedSurfaceForInterestingWindow();
            }
            if (!this.allDrawn) {
                if (interestingNotDrawn == 0) {
                    z = true;
                }
                this.allDrawn = z;
                if (this.allDrawn) {
                    this.mService.mH.obtainMessage(32, this.token).sendToTarget();
                }
            }
            clearWasVisibleBeforeClientHidden();
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.d(TAG, "restoreSavedSurfaceForInterestingWindows: " + this + " allDrawn=" + this.allDrawn + " interestingNotDrawn=" + interestingNotDrawn);
            }
            return;
        }
        clearWasVisibleBeforeClientHidden();
    }

    void destroySavedSurfaces() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).destroySavedSurface();
        }
    }

    void clearAllDrawn() {
        this.allDrawn = false;
        this.deferClearAllDrawn = false;
        this.allDrawnExcludingSaved = false;
    }

    Task getTask() {
        return (Task) getParent();
    }

    TaskStack getStack() {
        Task task = getTask();
        if (task != null) {
            return task.mStack;
        }
        return null;
    }

    void onParentSet() {
        super.onParentSet();
        Task task = getTask();
        if (!this.mReparenting) {
            if (task == null) {
                this.mService.mClosingApps.remove(this);
            } else if (!(this.mLastParent == null || this.mLastParent.mStack == null)) {
                task.mStack.mExitingAppTokens.remove(this);
            }
        }
        this.mLastParent = task;
    }

    void postWindowRemoveStartingWindowCleanup(WindowState win) {
        if (this.startingWindow == win) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Notify removed startingWindow " + win);
            }
            if (getController() != null) {
                getController().removeStartingWindow();
            }
        } else if (this.mChildren.size() == 0) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Nulling last startingData");
            }
            this.startingData = null;
            if (this.mHiddenSetFromTransferredStartingWindow) {
                this.hidden = true;
            }
        } else if (this.mChildren.size() == 1 && this.startingSurface != null && (isRelaunching() ^ 1) != 0) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Last window, removing starting window " + win);
            }
            if (getController() != null) {
                getController().removeStartingWindow();
            }
        }
    }

    void removeDeadWindows() {
        for (int winNdx = this.mChildren.size() - 1; winNdx >= 0; winNdx--) {
            WindowState win = (WindowState) this.mChildren.get(winNdx);
            if (win.mAppDied) {
                if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.w(TAG, "removeDeadWindows: " + win);
                }
                win.mDestroying = true;
                win.removeIfPossible();
            }
        }
    }

    boolean hasWindowsAlive() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (!((WindowState) this.mChildren.get(i)).mAppDied) {
                return true;
            }
        }
        return false;
    }

    void setWillReplaceWindows(boolean animate) {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d(TAG, "Marking app token " + this + " with replacing windows.");
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceWindow(animate);
        }
        if (animate) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "setWillReplaceWindow() Setting dummy animation on: " + this);
            }
            this.mAppAnimator.setDummyAnimation();
        }
    }

    void setWillReplaceChildWindows() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d(TAG, "Marking app token " + this + " with replacing child windows.");
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceChildWindows();
        }
    }

    void clearWillReplaceWindows() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d(TAG, "Resetting app token " + this + " of replacing window marks.");
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).clearWillReplaceWindow();
        }
    }

    void requestUpdateWallpaperIfNeeded() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).requestUpdateWallpaperIfNeeded();
        }
    }

    boolean isRelaunching() {
        return this.mPendingRelaunchCount > 0;
    }

    boolean shouldFreezeBounds() {
        Task task = getTask();
        if (task == null || task.inFreeformWorkspace()) {
            return false;
        }
        return getTask().isDragResizing();
    }

    void startRelaunching() {
        if (shouldFreezeBounds()) {
            freezeBounds();
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).mWinAnimator.detachChildren();
        }
        this.mPendingRelaunchCount++;
    }

    void finishRelaunching() {
        unfreezeBounds();
        if (this.mPendingRelaunchCount > 0) {
            this.mPendingRelaunchCount--;
        } else {
            checkKeyguardFlagsChanged();
        }
    }

    void clearRelaunching() {
        if (this.mPendingRelaunchCount != 0) {
            unfreezeBounds();
            this.mPendingRelaunchCount = 0;
            this.mFromFreeform = false;
        }
    }

    protected boolean isFirstChildWindowGreaterThanSecond(WindowState newWindow, WindowState existingWindow) {
        int type1 = newWindow.mAttrs.type;
        int type2 = existingWindow.mAttrs.type;
        if (type1 == 1 && type2 != 1) {
            return false;
        }
        if (type1 == 1 || type2 != 1) {
            return (type1 == 3 && type2 != 3) || type1 == 3 || type2 != 3;
        } else {
            return true;
        }
    }

    void addWindow(WindowState w) {
        super.addWindow(w);
        boolean gotReplacementWindow = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            gotReplacementWindow |= ((WindowState) this.mChildren.get(i)).setReplacementWindowIfNeeded(w);
        }
        if (gotReplacementWindow) {
            this.mService.scheduleWindowReplacementTimeouts(this);
        }
        checkKeyguardFlagsChanged();
    }

    void removeChild(WindowState child) {
        super.removeChild(child);
        checkKeyguardFlagsChanged();
    }

    private boolean waitingForReplacement() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).waitingForReplacement()) {
                return true;
            }
        }
        return false;
    }

    void onWindowReplacementTimeout() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onWindowReplacementTimeout();
        }
    }

    void reparent(Task task, int position) {
        Task currentTask = getTask();
        if (task == currentTask) {
            throw new IllegalArgumentException("window token=" + this + " already child of task=" + currentTask);
        } else if (currentTask.mStack != task.mStack) {
            throw new IllegalArgumentException("window token=" + this + " current task=" + currentTask + " belongs to a different stack than " + task);
        } else {
            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                Slog.i(TAG, "reParentWindowToken: removing window token=" + this + " from task=" + currentTask);
            }
            DisplayContent prevDisplayContent = getDisplayContent();
            this.mReparenting = true;
            getParent().removeChild(this);
            task.addChild(this, position);
            this.mReparenting = false;
            DisplayContent displayContent = task.getDisplayContent();
            displayContent.setLayoutNeeded();
            if (prevDisplayContent != displayContent) {
                onDisplayChanged(displayContent);
                prevDisplayContent.setLayoutNeeded();
            }
        }
    }

    private void freezeBounds() {
        Task task = getTask();
        this.mFrozenBounds.offer(new Rect(task.mPreparedFrozenBounds));
        if (task.mPreparedFrozenMergedConfig.equals(Configuration.EMPTY)) {
            this.mFrozenMergedConfig.offer(new Configuration(task.getConfiguration()));
        } else {
            this.mFrozenMergedConfig.offer(new Configuration(task.mPreparedFrozenMergedConfig));
        }
        task.mPreparedFrozenMergedConfig.unset();
    }

    private void unfreezeBounds() {
        if (!this.mFrozenBounds.isEmpty()) {
            this.mFrozenBounds.remove();
            if (!this.mFrozenMergedConfig.isEmpty()) {
                this.mFrozenMergedConfig.remove();
            }
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                ((WindowState) this.mChildren.get(i)).onUnfreezeBounds();
            }
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    void setAppLayoutChanges(int changes, String reason) {
        if (!this.mChildren.isEmpty()) {
            DisplayContent dc = getDisplayContent();
            dc.pendingLayoutChanges |= changes;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                this.mService.mWindowPlacerLocked.debugLayoutRepeats(reason, dc.pendingLayoutChanges);
            }
        }
    }

    void removeReplacedWindowIfNeeded(WindowState replacement) {
        int i = this.mChildren.size() - 1;
        while (i >= 0 && !((WindowState) this.mChildren.get(i)).removeReplacedWindowIfNeeded(replacement)) {
            i--;
        }
    }

    void startFreezingScreen() {
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            WindowManagerService.logWithStack(TAG, "Set freezing of " + this.appToken + ": hidden=" + this.hidden + " freezing=" + this.mAppAnimator.freezingScreen + " hiddenRequested=" + this.hiddenRequested);
        }
        if (!this.hiddenRequested) {
            if (!this.mAppAnimator.freezingScreen) {
                this.mAppAnimator.freezingScreen = true;
                this.mService.registerAppFreezeListener(this);
                this.mAppAnimator.lastFreezeDuration = 0;
                WindowManagerService windowManagerService = this.mService;
                windowManagerService.mAppsFreezingScreen++;
                if (this.mService.mAppsFreezingScreen == 1) {
                    this.mService.startFreezingDisplayLocked(false, 0, 0, getDisplayContent());
                    this.mService.mH.removeMessages(17);
                    this.mService.mH.sendEmptyMessageDelayed(17, 2000);
                }
            }
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                ((WindowState) this.mChildren.get(i)).onStartFreezingScreen();
            }
        }
    }

    void stopFreezingScreen(boolean unfreezeSurfaceNow, boolean force) {
        if (this.mAppAnimator.freezingScreen) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Clear freezing of " + this + " force=" + force);
            }
            int unfrozeWindows = 0;
            for (int i = 0; i < this.mChildren.size(); i++) {
                unfrozeWindows |= ((WindowState) this.mChildren.get(i)).onStopFreezingScreen();
            }
            if (force || unfrozeWindows != 0) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(TAG, "No longer freezing: " + this);
                }
                this.mAppAnimator.freezingScreen = false;
                this.mService.unregisterAppFreezeListener(this);
                this.mAppAnimator.lastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mService.mDisplayFreezeTime);
                WindowManagerService windowManagerService = this.mService;
                windowManagerService.mAppsFreezingScreen--;
                this.mService.mLastFinishedFreezeSource = this;
            }
            if (unfreezeSurfaceNow) {
                if (unfrozeWindows != 0) {
                    this.mService.mWindowPlacerLocked.performSurfacePlacement();
                }
                this.mService.stopFreezingDisplayLocked();
            }
        }
    }

    public void onAppFreezeTimeout() {
        Slog.w(TAG, "Force clearing freeze: " + this);
        stopFreezingScreen(true, true);
    }

    boolean transferStartingWindow(IBinder transferFrom) {
        AppWindowToken fromToken = getDisplayContent().getAppWindowToken(transferFrom);
        if (fromToken == null) {
            return false;
        }
        WindowState tStartingWindow = fromToken.startingWindow;
        if (tStartingWindow != null && fromToken.startingSurface != null) {
            this.mService.mSkipAppTransitionAnimation = true;
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Moving existing starting " + tStartingWindow + " from " + fromToken + " to " + this);
            }
            long origId = Binder.clearCallingIdentity();
            this.startingData = fromToken.startingData;
            this.startingSurface = fromToken.startingSurface;
            this.startingDisplayed = fromToken.startingDisplayed;
            fromToken.startingDisplayed = false;
            this.startingWindow = tStartingWindow;
            this.reportedVisible = fromToken.reportedVisible;
            fromToken.startingData = null;
            fromToken.startingSurface = null;
            fromToken.startingWindow = null;
            fromToken.startingMoved = true;
            tStartingWindow.mToken = this;
            tStartingWindow.mAppToken = this;
            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Removing starting " + tStartingWindow + " from " + fromToken);
            }
            fromToken.removeChild(tStartingWindow);
            fromToken.postWindowRemoveStartingWindowCleanup(tStartingWindow);
            fromToken.mHiddenSetFromTransferredStartingWindow = false;
            addWindow(tStartingWindow);
            if (fromToken.allDrawn) {
                this.allDrawn = true;
                this.deferClearAllDrawn = fromToken.deferClearAllDrawn;
            }
            if (fromToken.firstWindowDrawn) {
                this.firstWindowDrawn = true;
            }
            if (!fromToken.hidden) {
                this.hidden = false;
                this.hiddenRequested = false;
                this.mHiddenSetFromTransferredStartingWindow = true;
            }
            setClientHidden(fromToken.mClientHidden);
            fromToken.mAppAnimator.transferCurrentAnimation(this.mAppAnimator, tStartingWindow.mWinAnimator);
            this.mService.updateFocusedWindowLocked(3, true);
            getDisplayContent().setLayoutNeeded();
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
            Binder.restoreCallingIdentity(origId);
            return true;
        } else if (fromToken.startingData != null) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Moving pending starting from " + fromToken + " to " + this);
            }
            this.startingData = fromToken.startingData;
            fromToken.startingData = null;
            fromToken.startingMoved = true;
            if (getController() != null) {
                getController().scheduleAddStartingWindow();
            }
            return true;
        } else {
            AppWindowAnimator tAppAnimator = fromToken.mAppAnimator;
            AppWindowAnimator wAppAnimator = this.mAppAnimator;
            if (tAppAnimator.thumbnail != null) {
                if (wAppAnimator.thumbnail != null) {
                    wAppAnimator.thumbnail.destroy();
                }
                wAppAnimator.thumbnail = tAppAnimator.thumbnail;
                wAppAnimator.thumbnailLayer = tAppAnimator.thumbnailLayer;
                wAppAnimator.thumbnailAnimation = tAppAnimator.thumbnailAnimation;
                tAppAnimator.thumbnail = null;
            }
            return false;
        }
    }

    boolean isLastWindow(WindowState win) {
        return this.mChildren.size() == 1 && this.mChildren.get(0) == win;
    }

    void setAllAppWinAnimators() {
        ArrayList<WindowStateAnimator> allAppWinAnimators = this.mAppAnimator.mAllAppWinAnimators;
        allAppWinAnimators.clear();
        int windowsCount = this.mChildren.size();
        for (int j = 0; j < windowsCount; j++) {
            ((WindowState) this.mChildren.get(j)).addWinAnimatorToList(allAppWinAnimators);
        }
    }

    void onAppTransitionDone() {
        this.sendingToBottom = false;
        if (!this.mService.mPolicy.isGestureAnimSupport()) {
            return;
        }
        if (toString().contains("com.oppo.launcher/.Launcher")) {
            this.mService.mPolicy.handleOpeningSpecialApp(3, ActivityManagerService.OPPO_LAUNCHER);
        } else if (!toString().contains("com.coloros.recents/.RecentsActivity")) {
        } else {
            if (this.mClientHidden) {
                this.mService.mPolicy.handleOpeningSpecialApp(4, "com.coloros.recents");
            } else {
                this.mService.mPolicy.handleOpeningSpecialApp(3, "com.coloros.recents");
            }
        }
    }

    boolean isOnTop() {
        return ((Task) getParent()).getTopunFinishChild() == this ? getParent().isOnTop() : false;
    }

    int getOrientation(int candidate) {
        boolean z = true;
        if (candidate == 3) {
            return this.mOrientation;
        }
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v(TAG, "getOrientation " + this + " mFinishing= " + this.mFinishing + " " + this.mService.mClosingApps.contains(this) + " isVisible " + isVisible() + " " + this.mService.mOpeningApps.contains(this) + " isOnTop " + isOnTop() + " mOrientation " + this.mOrientation + " sendingToBottom " + this.sendingToBottom);
        }
        if (this.mOrientation != 1 || this.token == null || this.token.toString() == null || !this.token.toString().contains("com.tencent.connect.common.AssistActivity") || (this.token.toString().contains("com.netease.cloudmusic") ^ 1) == 0) {
            if (!this.sendingToBottom) {
                z = this.mService.mClosingApps.contains(this);
            }
            if (z || (!isVisible() && !this.mService.mOpeningApps.contains(this) && !isOnTop())) {
                return -2;
            }
            return this.mOrientation;
        }
        Slog.v(TAG, "AssistActivity " + this.token.toString());
        return -1;
    }

    int getOrientationIgnoreVisibility() {
        return this.mOrientation;
    }

    void checkAppWindowsReadyToShow() {
        if (this.allDrawn != this.mAppAnimator.allDrawn) {
            this.mAppAnimator.allDrawn = this.allDrawn;
            if (this.allDrawn) {
                if (this.mAppAnimator.freezingScreen) {
                    this.mAppAnimator.showAllWindowsLocked();
                    stopFreezingScreen(false, true);
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.i(TAG, "Setting mOrientationChangeComplete=true because wtoken " + this + " numInteresting=" + this.mNumInterestingWindows + " numDrawn=" + this.mNumDrawnWindows);
                    }
                    setAppLayoutChanges(4, "checkAppWindowsReadyToShow: freezingScreen");
                } else {
                    setAppLayoutChanges(8, "checkAppWindowsReadyToShow");
                    if (!this.mService.mOpeningApps.contains(this)) {
                        this.mService.mAnimator.orAnimating(this.mAppAnimator.showAllWindowsLocked());
                    }
                }
            }
        }
    }

    private boolean allDrawnStatesConsidered() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState child = (WindowState) this.mChildren.get(i);
            if (child.mightAffectAllDrawn(false) && (child.getDrawnStateEvaluated() ^ 1) != 0) {
                return false;
            }
        }
        return true;
    }

    void updateAllDrawn() {
        int numInteresting;
        if (!this.allDrawn) {
            numInteresting = this.mNumInterestingWindows;
            if (numInteresting > 0 && allDrawnStatesConsidered() && this.mNumDrawnWindows >= numInteresting && (isRelaunching() ^ 1) != 0) {
                this.allDrawn = true;
                if (this.mDisplayContent != null) {
                    this.mDisplayContent.setLayoutNeeded();
                }
                this.mService.mH.obtainMessage(32, this.token).sendToTarget();
                TaskStack pinnedStack = this.mDisplayContent.getStackById(4);
                if (pinnedStack != null) {
                    pinnedStack.onAllWindowsDrawn();
                }
            }
        }
        if (!this.allDrawnExcludingSaved) {
            numInteresting = this.mNumInterestingWindowsExcludingSaved;
            if (numInteresting > 0 && this.mNumDrawnWindowsExcludingSaved >= numInteresting) {
                this.allDrawnExcludingSaved = true;
                if (this.mDisplayContent != null) {
                    this.mDisplayContent.setLayoutNeeded();
                }
                if (isAnimatingInvisibleWithSavedSurface() && (this.mService.mFinishedEarlyAnim.contains(this) ^ 1) != 0) {
                    this.mService.mFinishedEarlyAnim.add(this);
                }
            }
        }
    }

    boolean updateDrawnWindowStates(WindowState w) {
        w.setDrawnStateEvaluated(true);
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE && w == this.startingWindow) {
            Slog.d(TAG, "updateWindows: starting " + w + " isOnScreen=" + w.isOnScreen() + " allDrawn=" + this.allDrawn + " freezingScreen=" + this.mAppAnimator.freezingScreen);
        }
        if (this.allDrawn && this.allDrawnExcludingSaved && (this.mAppAnimator.freezingScreen ^ 1) != 0) {
            return false;
        }
        if (this.mLastTransactionSequence != ((long) this.mService.mTransactionSequence)) {
            this.mLastTransactionSequence = (long) this.mService.mTransactionSequence;
            this.mNumDrawnWindows = 0;
            this.mNumInterestingWindows = 0;
            this.mNumInterestingWindowsExcludingSaved = 0;
            this.mNumDrawnWindowsExcludingSaved = 0;
            this.startingDisplayed = false;
        }
        WindowStateAnimator winAnimator = w.mWinAnimator;
        boolean isInterestingAndDrawn = false;
        if (!this.allDrawn && w.mightAffectAllDrawn(false)) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Eval win " + w + ": isDrawn=" + w.isDrawnLw() + ", isAnimationSet=" + winAnimator.isAnimationSet());
                if (!w.isDrawnLw()) {
                    Slog.v(TAG, "Not displayed: s=" + winAnimator.mSurfaceController + " pv=" + w.mPolicyVisibility + " mDrawState=" + winAnimator.drawStateToString() + " ph=" + w.isParentWindowHidden() + " th=" + this.hiddenRequested + " a=" + winAnimator.mAnimating);
                }
            }
            if (w != this.startingWindow) {
                if (w.isInteresting()) {
                    this.mNumInterestingWindows++;
                    if (w.isDrawnLw()) {
                        this.mNumDrawnWindows++;
                        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.v(TAG, "tokenMayBeDrawn: " + this + " w=" + w + " numInteresting=" + this.mNumInterestingWindows + " freezingScreen=" + this.mAppAnimator.freezingScreen + " mAppFreezing=" + w.mAppFreezing);
                        }
                        isInterestingAndDrawn = true;
                    }
                }
            } else if (w.isDrawnLw()) {
                if (getController() != null) {
                    getController().reportStartingWindowDrawn();
                }
                this.startingDisplayed = true;
            }
        }
        if (!this.allDrawnExcludingSaved && w.mightAffectAllDrawn(true) && w != this.startingWindow && w.isInteresting()) {
            this.mNumInterestingWindowsExcludingSaved++;
            if (w.isDrawnLw() && (w.isAnimatingWithSavedSurface() ^ 1) != 0) {
                this.mNumDrawnWindowsExcludingSaved++;
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(TAG, "tokenMayBeDrawnExcludingSaved: " + this + " w=" + w + " numInteresting=" + this.mNumInterestingWindowsExcludingSaved + " freezingScreen=" + this.mAppAnimator.freezingScreen + " mAppFreezing=" + w.mAppFreezing);
                }
                isInterestingAndDrawn = true;
            }
        }
        return isInterestingAndDrawn;
    }

    void stepAppWindowsAnimation(long currentTime) {
        this.mAppAnimator.wasAnimating = this.mAppAnimator.animating;
        if (this.mAppAnimator.stepAnimationLocked(currentTime)) {
            this.mAppAnimator.animating = true;
            this.mService.mAnimator.setAnimating(true);
            this.mService.mAnimator.mAppWindowAnimating = true;
        } else if (this.mAppAnimator.wasAnimating) {
            setAppLayoutChanges(4, WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS ? "appToken " + this + " done" : null);
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "updateWindowsApps...: done animating " + this);
            }
        }
    }

    boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (!this.mIsExiting || (waitingForReplacement() ^ 1) == 0) {
            return forAllWindowsUnchecked(callback, traverseTopToBottom);
        }
        return false;
    }

    boolean forAllWindowsUnchecked(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        return super.forAllWindows((ToBooleanFunction) callback, traverseTopToBottom);
    }

    AppWindowToken asAppWindowToken() {
        return this;
    }

    boolean fillsParent() {
        return this.mFillsParent;
    }

    void setFillsParent(boolean fillsParent) {
        this.mFillsParent = fillsParent;
    }

    void setFinishing(boolean finishing) {
        this.mFinishing = finishing;
    }

    boolean containsDismissKeyguardWindow() {
        if (isRelaunching()) {
            return this.mLastContainsDismissKeyguardWindow;
        }
        int i = this.mChildren.size() - 1;
        while (i >= 0) {
            if ((((WindowState) this.mChildren.get(i)).mAttrs.flags & DumpState.DUMP_CHANGES) == 0) {
                i--;
            } else if ((this.mChildren.get(i) instanceof WindowState) && OppoInterceptWindow.getInstance().interceptWindow(this.mService.mContext, (WindowState) this.mChildren.get(i))) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    boolean containsShowWhenLockedWindow() {
        if (isRelaunching()) {
            return this.mLastContainsShowWhenLockedWindow;
        }
        int i = this.mChildren.size() - 1;
        while (i >= 0) {
            if ((((WindowState) this.mChildren.get(i)).mAttrs.flags & DumpState.DUMP_FROZEN) == 0) {
                i--;
            } else if ((this.mChildren.get(i) instanceof WindowState) && OppoInterceptWindow.getInstance().interceptWindow(this.mService.mContext, (WindowState) this.mChildren.get(i))) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    void checkKeyguardFlagsChanged() {
        boolean containsDismissKeyguard = containsDismissKeyguardWindow();
        boolean containsShowWhenLocked = containsShowWhenLockedWindow();
        if (!(containsDismissKeyguard == this.mLastContainsDismissKeyguardWindow && containsShowWhenLocked == this.mLastContainsShowWhenLockedWindow)) {
            this.mService.notifyKeyguardFlagsChanged(null);
        }
        this.mLastContainsDismissKeyguardWindow = containsDismissKeyguard;
        this.mLastContainsShowWhenLockedWindow = containsShowWhenLocked;
    }

    WindowState getImeTargetBelowWindow(WindowState w) {
        int index = this.mChildren.indexOf(w);
        if (index > 0) {
            WindowState target = (WindowState) this.mChildren.get(index - 1);
            if (target.canBeImeTarget()) {
                return target;
            }
        }
        return null;
    }

    int getLowestAnimLayer() {
        for (int i = 0; i < this.mChildren.size(); i++) {
            WindowState w = (WindowState) this.mChildren.get(i);
            if (!w.mRemoved) {
                return w.mWinAnimator.mAnimLayer;
            }
        }
        return Integer.MAX_VALUE;
    }

    WindowState getHighestAnimLayerWindow(WindowState currentTarget) {
        WindowState candidate = null;
        for (int i = this.mChildren.indexOf(currentTarget); i >= 0; i--) {
            WindowState w = (WindowState) this.mChildren.get(i);
            if (!w.mRemoved && (candidate == null || w.mWinAnimator.mAnimLayer > candidate.mWinAnimator.mAnimLayer)) {
                candidate = w;
            }
        }
        return candidate;
    }

    void setDisablePreviewScreenshots(boolean disable) {
        this.mDisablePreviewScreenshots = disable;
    }

    void setCanTurnScreenOn(boolean canTurnScreenOn) {
        this.mCanTurnScreenOn = canTurnScreenOn;
    }

    boolean canTurnScreenOn() {
        return this.mCanTurnScreenOn;
    }

    boolean shouldUseAppThemeSnapshot() {
        return !this.mDisablePreviewScreenshots ? forAllWindows(-$Lambda$lByfYr6ieFYh5pmaqCgCKVVzuwA.$INST$0, true) : true;
    }

    /* renamed from: lambda$-com_android_server_wm_AppWindowToken_73893 */
    static /* synthetic */ boolean m262lambda$-com_android_server_wm_AppWindowToken_73893(WindowState w) {
        boolean z = false;
        if (w.mAttrs.type == 2011 && (w.isVisibleLw() ^ 1) != 0) {
            return false;
        }
        if ((w.mAttrs.flags & 8192) != 0) {
            z = true;
        }
        return z;
    }

    int getAnimLayerAdjustment() {
        return this.mAppAnimator.animLayerAdjustment;
    }

    void dump(PrintWriter pw, String prefix) {
        super.dump(pw, prefix);
        if (this.appToken != null) {
            pw.println(prefix + "app=true mVoiceInteraction=" + this.mVoiceInteraction);
        }
        if (WindowManagerDebugConfig.DEBUG_STACK) {
            pw.print(prefix);
            pw.print("task=");
            pw.println(getTask());
        } else {
            pw.print(prefix);
            pw.print("taskId=");
            if (getTask() != null) {
                pw.println(getTask().mTaskId);
            }
        }
        pw.print(prefix);
        pw.print(" mFillsParent=");
        pw.print(this.mFillsParent);
        pw.print(" mOrientation=");
        pw.println(this.mOrientation);
        pw.println(prefix + "hiddenRequested=" + this.hiddenRequested + " mClientHidden=" + this.mClientHidden + (this.mDeferHidingClient ? " mDeferHidingClient=" + this.mDeferHidingClient : "") + " reportedDrawn=" + this.reportedDrawn + " reportedVisible=" + this.reportedVisible);
        if (this.paused) {
            pw.print(prefix);
            pw.print("paused=");
            pw.println(this.paused);
        }
        if (this.mAppStopped) {
            pw.print(prefix);
            pw.print("mAppStopped=");
            pw.println(this.mAppStopped);
        }
        if (this.mNumInterestingWindows != 0 || this.mNumDrawnWindows != 0 || this.allDrawn || this.mAppAnimator.allDrawn) {
            pw.print(prefix);
            pw.print("mNumInterestingWindows=");
            pw.print(this.mNumInterestingWindows);
            pw.print(" mNumDrawnWindows=");
            pw.print(this.mNumDrawnWindows);
            pw.print(" inPendingTransaction=");
            pw.print(this.inPendingTransaction);
            pw.print(" allDrawn=");
            pw.print(this.allDrawn);
            pw.print(" (animator=");
            pw.print(this.mAppAnimator.allDrawn);
            pw.println(")");
        }
        if (this.inPendingTransaction) {
            pw.print(prefix);
            pw.print("inPendingTransaction=");
            pw.println(this.inPendingTransaction);
        }
        if (this.startingData != null || this.removed || this.firstWindowDrawn || this.mIsExiting) {
            pw.print(prefix);
            pw.print("startingData=");
            pw.print(this.startingData);
            pw.print(" removed=");
            pw.print(this.removed);
            pw.print(" firstWindowDrawn=");
            pw.print(this.firstWindowDrawn);
            pw.print(" mIsExiting=");
            pw.println(this.mIsExiting);
        }
        if (this.startingWindow != null || this.startingSurface != null || this.startingDisplayed || this.startingMoved || this.mHiddenSetFromTransferredStartingWindow) {
            pw.print(prefix);
            pw.print("startingWindow=");
            pw.print(this.startingWindow);
            pw.print(" startingSurface=");
            pw.print(this.startingSurface);
            pw.print(" startingDisplayed=");
            pw.print(this.startingDisplayed);
            pw.print(" startingMoved=");
            pw.print(this.startingMoved);
            pw.println(" mHiddenSetFromTransferredStartingWindow=" + this.mHiddenSetFromTransferredStartingWindow);
        }
        if (!this.mFrozenBounds.isEmpty()) {
            pw.print(prefix);
            pw.print("mFrozenBounds=");
            pw.println(this.mFrozenBounds);
            pw.print(prefix);
            pw.print("mFrozenMergedConfig=");
            pw.println(this.mFrozenMergedConfig);
        }
        if (this.mPendingRelaunchCount != 0) {
            pw.print(prefix);
            pw.print("mPendingRelaunchCount=");
            pw.println(this.mPendingRelaunchCount);
        }
        if (getController() != null) {
            pw.print(prefix);
            pw.print("controller=");
            pw.println(getController());
        }
        if (this.mRemovingFromDisplay) {
            pw.println(prefix + "mRemovingFromDisplay=" + this.mRemovingFromDisplay);
        }
        if (this.mAppAnimator.isAnimating()) {
            this.mAppAnimator.dump(pw, prefix + "  ");
        }
    }

    public String toString() {
        if (this.stringName == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("AppWindowToken{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" token=");
            sb.append(this.token);
            sb.append('}');
            this.stringName = sb.toString();
        }
        return this.stringName + (this.mIsExiting ? " mIsExiting=" : "");
    }

    WindowState getTopWindow() {
        int size = this.mChildren.size();
        if (size > 0) {
            return (WindowState) this.mChildren.get(size - 1);
        }
        return null;
    }
}
