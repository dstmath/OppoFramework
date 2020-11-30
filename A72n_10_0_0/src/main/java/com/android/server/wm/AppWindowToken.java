package com.android.server.wm;

import android.app.ActivityManager;
import android.app.WindowConfiguration;
import android.common.ColorFrameworkFactory;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.util.ArraySet;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.IApplicationToken;
import android.view.InputApplicationHandle;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationDefinition;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.Animation;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.AttributeCache;
import com.android.server.LocalServices;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.color.ColorDisplayService;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.RemoteAnimationController;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/* access modifiers changed from: package-private */
public class AppWindowToken extends OppoBaseAppWindowToken implements WindowManagerService.AppFreezeListener, ConfigurationContainerListener {
    private static final int STARTING_WINDOW_TYPE_NONE = 0;
    private static final int STARTING_WINDOW_TYPE_SNAPSHOT = 1;
    private static final int STARTING_WINDOW_TYPE_SPLASH_SCREEN = 2;
    private static final String TAG = "WindowManager";
    @VisibleForTesting
    static final int Z_BOOST_BASE = 800570000;
    boolean allDrawn;
    final IApplicationToken appToken;
    boolean deferClearAllDrawn;
    boolean firstWindowDrawn;
    boolean hiddenRequested;
    boolean inPendingTransaction;
    final ComponentName mActivityComponent;
    ActivityRecord mActivityRecord;
    private final Runnable mAddStartingWindow;
    private boolean mAlwaysFocusable;
    private AnimatingAppWindowTokenRegistry mAnimatingAppWindowTokenRegistry;
    SurfaceControl mAnimationBoundsLayer;
    boolean mAppStopped;
    private boolean mCanTurnScreenOn;
    private boolean mClientHidden;
    private final ColorDisplayService.ColorTransformController mColorTransformController;
    boolean mDeferHidingClient;
    private boolean mDisablePreviewScreenshots;
    boolean mEnteringAnimation;
    private boolean mFillsParent;
    private boolean mFreezingScreen;
    ArrayDeque<Rect> mFrozenBounds;
    ArrayDeque<Configuration> mFrozenMergedConfig;
    private boolean mHiddenSetFromTransferredStartingWindow;
    final InputApplicationHandle mInputApplicationHandle;
    long mInputDispatchingTimeoutNanos;
    boolean mIsExiting;
    private boolean mLastAllDrawn;
    private AppSaturationInfo mLastAppSaturationInfo;
    private boolean mLastContainsDismissKeyguardWindow;
    private boolean mLastContainsShowWhenLockedWindow;
    private Task mLastParent;
    private boolean mLastSurfaceShowing;
    private long mLastTransactionSequence;
    boolean mLaunchTaskBehind;
    private Letterbox mLetterbox;
    boolean mNeedsAnimationBoundsLayer;
    @VisibleForTesting
    boolean mNeedsZBoost;
    private int mNumDrawnWindows;
    private int mNumInterestingWindows;
    private int mPendingRelaunchCount;
    private RemoteAnimationDefinition mRemoteAnimationDefinition;
    private boolean mRemovingFromDisplay;
    private boolean mReparenting;
    private final WindowState.UpdateReportedVisibilityResults mReportedVisibilityResults;
    int mRotationAnimationHint;
    boolean mShowForAllUsers;
    private Rect mSizeCompatBounds;
    private float mSizeCompatScale;
    StartingData mStartingData;
    int mTargetSdk;
    private AppWindowThumbnail mThumbnail;
    private final Point mTmpPoint;
    private final Rect mTmpPrevBounds;
    private final Rect mTmpRect;
    private int mTransit;
    private SurfaceControl mTransitChangeLeash;
    private int mTransitFlags;
    private final Rect mTransitStartRect;
    private boolean mUseTransferredAnimation;
    final boolean mVoiceInteraction;
    private boolean mWillCloseOrEnterPip;
    boolean removed;
    private boolean reportedDrawn;
    boolean reportedVisible;
    boolean startingDisplayed;
    boolean startingMoved;
    WindowManagerPolicy.StartingSurface startingSurface;
    WindowState startingWindow;

    public /* synthetic */ void lambda$new$1$AppWindowToken(float[] matrix, float[] translation) {
        this.mWmService.mH.post(new Runnable(matrix, translation) {
            /* class com.android.server.wm.$$Lambda$AppWindowToken$fbAn0RqOBB6FcyKBQMtQpZ1Ec */
            private final /* synthetic */ float[] f$1;
            private final /* synthetic */ float[] f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AppWindowToken.this.lambda$new$0$AppWindowToken(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$AppWindowToken(float[] matrix, float[] translation) {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mLastAppSaturationInfo == null) {
                    this.mLastAppSaturationInfo = new AppSaturationInfo();
                }
                this.mLastAppSaturationInfo.setSaturation(matrix, translation);
                updateColorTransform();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    AppWindowToken(WindowManagerService service, IApplicationToken token, ComponentName activityComponent, boolean voiceInteraction, DisplayContent dc, long inputDispatchingTimeoutNanos, boolean fullscreen, boolean showForAllUsers, int targetSdk, int orientation, int rotationAnimationHint, boolean launchTaskBehind, boolean alwaysFocusable, ActivityRecord activityRecord) {
        this(service, token, activityComponent, voiceInteraction, dc, fullscreen);
        this.mActivityRecord = activityRecord;
        this.mActivityRecord.registerConfigurationChangeListener(this);
        this.mInputDispatchingTimeoutNanos = inputDispatchingTimeoutNanos;
        this.mShowForAllUsers = showForAllUsers;
        this.mTargetSdk = targetSdk;
        this.mOrientation = orientation;
        this.mLaunchTaskBehind = launchTaskBehind;
        this.mAlwaysFocusable = alwaysFocusable;
        this.mRotationAnimationHint = rotationAnimationHint;
        setHidden(true);
        this.hiddenRequested = true;
        ((ColorDisplayService.ColorDisplayServiceInternal) LocalServices.getService(ColorDisplayService.ColorDisplayServiceInternal.class)).attachColorTransformController(activityRecord.packageName, activityRecord.mUserId, new WeakReference<>(this.mColorTransformController));
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    AppWindowToken(WindowManagerService service, IApplicationToken token, ComponentName activityComponent, boolean voiceInteraction, DisplayContent dc, boolean fillsParent) {
        super(service, token != null ? token.asBinder() : null, 2, true, dc, false);
        this.mRemovingFromDisplay = false;
        this.mLastTransactionSequence = Long.MIN_VALUE;
        this.mReportedVisibilityResults = new WindowState.UpdateReportedVisibilityResults();
        this.mFrozenBounds = new ArrayDeque<>();
        this.mFrozenMergedConfig = new ArrayDeque<>();
        this.mSizeCompatScale = 1.0f;
        this.mCanTurnScreenOn = true;
        this.mLastSurfaceShowing = true;
        this.mTransitStartRect = new Rect();
        this.mTransitChangeLeash = null;
        this.mTmpPoint = new Point();
        this.mTmpRect = new Rect();
        this.mTmpPrevBounds = new Rect();
        this.mColorTransformController = new ColorDisplayService.ColorTransformController() {
            /* class com.android.server.wm.$$Lambda$AppWindowToken$cwsF3cyeJjO4UiuaM07w8TBc698 */

            @Override // com.android.server.display.color.ColorDisplayService.ColorTransformController
            public final void applyAppSaturation(float[] fArr, float[] fArr2) {
                AppWindowToken.this.lambda$new$1$AppWindowToken(fArr, fArr2);
            }
        };
        this.mAddStartingWindow = new Runnable() {
            /* class com.android.server.wm.AppWindowToken.AnonymousClass1 */

            /* JADX WARNING: Code restructure failed: missing block: B:13:0x003f, code lost:
                com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:14:0x0044, code lost:
                if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_STARTING_WINDOW == false) goto L_0x0064;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:15:0x0046, code lost:
                android.util.Slog.v("WindowManager", "Add starting " + r7 + ": startingData=" + r1);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x0064, code lost:
                r0 = null;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:19:0x006b, code lost:
                r0 = r1.createStartingSurface(r7.this$0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:20:0x006d, code lost:
                r2 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:21:0x006e, code lost:
                android.util.Slog.w("WindowManager", "Exception when adding starting window", r2);
             */
            public void run() {
                WindowManagerPolicy.StartingSurface surface;
                synchronized (AppWindowToken.this.mWmService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        AppWindowToken.this.mWmService.mAnimationHandler.removeCallbacks(this);
                        if (AppWindowToken.this.mStartingData == null) {
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v("WindowManager", "startingData was nulled out before handling mAddStartingWindow: " + AppWindowToken.this);
                            }
                            return;
                        }
                        StartingData startingData = AppWindowToken.this.mStartingData;
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
                if (surface != null) {
                    boolean abort = false;
                    synchronized (AppWindowToken.this.mWmService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (!AppWindowToken.this.removed) {
                                if (AppWindowToken.this.mStartingData != null) {
                                    AppWindowToken.this.startingSurface = surface;
                                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && !abort) {
                                        Slog.v("WindowManager", "Added starting " + AppWindowToken.this + ": startingWindow=" + AppWindowToken.this.startingWindow + " startingView=" + AppWindowToken.this.startingSurface);
                                    }
                                }
                            }
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v("WindowManager", "Aborted starting " + AppWindowToken.this + ": removed=" + AppWindowToken.this.removed + " startingData=" + AppWindowToken.this.mStartingData);
                            }
                            AppWindowToken.this.startingWindow = null;
                            AppWindowToken.this.mStartingData = null;
                            abort = true;
                            Slog.v("WindowManager", "Added starting " + AppWindowToken.this + ": startingWindow=" + AppWindowToken.this.startingWindow + " startingView=" + AppWindowToken.this.startingSurface);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (abort) {
                        surface.remove();
                    }
                } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Surface returned was null: " + AppWindowToken.this);
                }
            }
        };
        this.appToken = token;
        this.mActivityComponent = activityComponent;
        this.mVoiceInteraction = voiceInteraction;
        this.mFillsParent = fillsParent;
        this.mInputApplicationHandle = new InputApplicationHandle(this.appToken.asBinder());
    }

    /* access modifiers changed from: package-private */
    public void onFirstWindowDrawn(WindowState win, WindowStateAnimator winAnimator) {
        this.firstWindowDrawn = true;
        removeDeadWindows();
        if (this.startingWindow != null) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v("WindowManager", "Finish starting " + win.mToken + ": first real window is shown, no animation");
            }
            win.cancelAnimation();
        }
        removeStartingWindow();
        updateReportedVisibilityLocked();
    }

    /* access modifiers changed from: package-private */
    public void updateReportedVisibilityLocked() {
        if (this.appToken != null) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "Update reported visibility: " + this);
            }
            int count = this.mChildren.size();
            this.mReportedVisibilityResults.reset();
            for (int i = 0; i < count; i++) {
                ((WindowState) this.mChildren.get(i)).updateReportedVisibility(this.mReportedVisibilityResults);
            }
            int numInteresting = this.mReportedVisibilityResults.numInteresting;
            int numVisible = this.mReportedVisibilityResults.numVisible;
            int numDrawn = this.mReportedVisibilityResults.numDrawn;
            boolean nowGone = this.mReportedVisibilityResults.nowGone;
            boolean nowVisible = false;
            boolean nowDrawn = numInteresting > 0 && numDrawn >= numInteresting;
            if (numInteresting > 0 && numVisible >= numInteresting && !isHidden()) {
                nowVisible = true;
            }
            if (!nowGone) {
                if (!nowDrawn) {
                    nowDrawn = this.reportedDrawn;
                }
                if (!nowVisible) {
                    nowVisible = this.reportedVisible;
                }
            }
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "VIS " + this + ": interesting=" + numInteresting + " visible=" + numVisible);
            }
            if (nowDrawn != this.reportedDrawn) {
                ActivityRecord activityRecord = this.mActivityRecord;
                if (activityRecord != null) {
                    activityRecord.onWindowsDrawn(nowDrawn, SystemClock.uptimeMillis());
                }
                this.reportedDrawn = nowDrawn;
            }
            if (nowVisible != this.reportedVisible) {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v("WindowManager", "Visibility changed in " + this + ": vis=" + nowVisible);
                }
                this.reportedVisible = nowVisible;
                if (this.mActivityRecord == null) {
                    return;
                }
                if (nowVisible) {
                    onWindowsVisible();
                } else {
                    onWindowsGone();
                }
            }
        }
    }

    private void onWindowsGone() {
        if (this.mActivityRecord != null) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "Reporting gone in " + this.mActivityRecord.appToken);
            }
            this.mActivityRecord.onWindowsGone();
        }
    }

    private void onWindowsVisible() {
        if (this.mActivityRecord != null) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "Reporting visible in " + this.mActivityRecord.appToken);
            }
            this.mActivityRecord.onWindowsVisible();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isClientHidden() {
        return this.mClientHidden;
    }

    /* access modifiers changed from: package-private */
    public void setClientHidden(boolean hideClient) {
        if (this.mClientHidden == hideClient) {
            return;
        }
        if (!hideClient || !this.mDeferHidingClient) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "setClientHidden: " + this + " clientHidden=" + hideClient + " Callers=" + Debug.getCallers(5));
            }
            this.mClientHidden = hideClient;
            sendAppVisibilityToClients();
        }
    }

    /* access modifiers changed from: package-private */
    public void setVisibility(boolean visible, boolean deferHidingClient) {
        WindowState win;
        AppWindowToken focusedToken;
        AppTransition appTransition = getDisplayContent().mAppTransition;
        if (visible || !this.hiddenRequested) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "setAppVisibility(" + this.appToken + ", visible=" + visible + "): " + appTransition + " hidden=" + isHidden() + " hiddenRequested=" + this.hiddenRequested + " Callers=" + Debug.getCallers(6));
            }
            DisplayContent displayContent = getDisplayContent();
            displayContent.mOpeningApps.remove(this);
            displayContent.mClosingApps.remove(this);
            if (isInChangeTransition()) {
                clearChangeLeash(getPendingTransaction(), true);
            }
            displayContent.mChangingApps.remove(this);
            this.waitingToShow = false;
            this.hiddenRequested = !visible;
            this.mDeferHidingClient = deferHidingClient;
            if (!visible) {
                removeDeadWindows();
            } else {
                if (!appTransition.isTransitionSet() && appTransition.isReady()) {
                    displayContent.mOpeningApps.add(this);
                }
                this.startingMoved = false;
                if (isHidden() || this.mAppStopped) {
                    clearAllDrawn();
                    if (isHidden()) {
                        this.waitingToShow = true;
                        forAllWindows((Consumer<WindowState>) new Consumer() {
                            /* class com.android.server.wm.$$Lambda$AppWindowToken$4kocSWikaWAaHRKuROwZYlNZbA */

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                AppWindowToken.this.lambda$setVisibility$2$AppWindowToken((WindowState) obj);
                            }
                        }, true);
                    }
                }
                setClientHidden(false);
                requestUpdateWallpaperIfNeeded();
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.v("WindowManager", "No longer Stopped: " + this);
                }
                this.mAppStopped = false;
                transferStartingWindowFromHiddenAboveTokenIfNeeded();
            }
            if (!okToAnimate() || !appTransition.isTransitionSet()) {
                commitVisibility(null, visible, -1, true, this.mVoiceInteraction);
                updateReportedVisibilityLocked();
                return;
            }
            this.inPendingTransaction = true;
            if (visible) {
                displayContent.mOpeningApps.add(this);
                this.mEnteringAnimation = true;
            } else {
                displayContent.mClosingApps.add(this);
                this.mEnteringAnimation = false;
            }
            if (!(appTransition.getAppTransition() != 16 || (win = getDisplayContent().findFocusedWindow()) == null || (focusedToken = win.mAppToken) == null)) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.d("WindowManager", "TRANSIT_TASK_OPEN_BEHIND,  adding " + focusedToken + " to mOpeningApps");
                }
                focusedToken.setHidden(true);
                displayContent.mOpeningApps.add(focusedToken);
            }
            reportDescendantOrientationChangeIfNeeded();
        } else if (!deferHidingClient && this.mDeferHidingClient) {
            this.mDeferHidingClient = deferHidingClient;
            setClientHidden(true);
        }
    }

    public /* synthetic */ void lambda$setVisibility$2$AppWindowToken(WindowState w) {
        if (OppoWMSDynamicLogConfig.DEBUG_WMS && w.mWinAnimator.mDrawState == 4 && !w.mWinAnimator.mLastHidden && !this.mAppStopped) {
            Slog.w("WindowManager", "Not resetDrawState w " + w);
        }
        if (w.mWinAnimator.mDrawState != 4) {
            return;
        }
        if (w.mWinAnimator.mLastHidden || this.mAppStopped) {
            w.mWinAnimator.resetDrawState();
            w.resetLastContentInsets();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean commitVisibility(WindowManager.LayoutParams lp, boolean visible, int transit, boolean performLayout, boolean isVoiceInteraction) {
        boolean z;
        boolean delayed = false;
        this.inPendingTransaction = false;
        this.mHiddenSetFromTransferredStartingWindow = false;
        boolean visibilityChanged = false;
        if (isHidden() == visible || ((isHidden() && this.mIsExiting) || (visible && waitingForReplacement()))) {
            AccessibilityController accessibilityController = this.mWmService.mAccessibilityController;
            boolean changed = false;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Changing app " + this + " hidden=" + isHidden() + " performLayout=" + performLayout);
            }
            boolean runningAppAnimation = false;
            if (transit != -1) {
                if (this.mUseTransferredAnimation) {
                    runningAppAnimation = isReallyAnimating();
                } else if (applyAnimationLocked(lp, transit, visible, isVoiceInteraction)) {
                    runningAppAnimation = true;
                }
                delayed = runningAppAnimation;
                WindowState window = findMainWindow();
                if (!(window == null || accessibilityController == null)) {
                    accessibilityController.onAppWindowTransitionLocked(window, transit);
                }
                changed = true;
            }
            int windowsCount = this.mChildren.size();
            for (int i = 0; i < windowsCount; i++) {
                changed |= ((WindowState) this.mChildren.get(i)).onAppVisibilityChanged(visible, runningAppAnimation);
            }
            setHidden(!visible);
            this.hiddenRequested = !visible;
            visibilityChanged = true;
            if (!visible) {
                stopFreezingScreen(true, true);
            } else {
                WindowState windowState = this.startingWindow;
                if (windowState != null && !windowState.isDrawnLw()) {
                    this.startingWindow.clearPolicyVisibilityFlag(1);
                    this.startingWindow.mLegacyPolicyVisibilityAfterAnim = false;
                }
                WindowManagerService windowManagerService = this.mWmService;
                Objects.requireNonNull(windowManagerService);
                forAllWindows((Consumer<WindowState>) new Consumer() {
                    /* class com.android.server.wm.$$Lambda$2KrtdmjrY7Nagc4IRqzCk9gDuQU */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        WindowManagerService.this.makeWindowFreezingScreenIfNeededLocked((WindowState) obj);
                    }
                }, true);
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "commitVisibility: " + this + ": hidden=" + isHidden() + " hiddenRequested=" + this.hiddenRequested);
            }
            if (changed) {
                getDisplayContent().getInputMonitor().setUpdateInputWindowsNeededLw();
                if (performLayout) {
                    z = false;
                    this.mWmService.updateFocusedWindowLocked(3, false);
                    this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
                } else {
                    z = false;
                }
                getDisplayContent().getInputMonitor().updateInputWindowsLw(z);
            } else {
                z = false;
            }
        } else {
            z = false;
        }
        this.mUseTransferredAnimation = z;
        if (isReallyAnimating()) {
            delayed = true;
        } else {
            onAnimationFinished();
        }
        for (int i2 = this.mChildren.size() - 1; i2 >= 0 && !delayed; i2--) {
            if (((WindowState) this.mChildren.get(i2)).isSelfOrChildAnimating()) {
                delayed = true;
            }
        }
        if (visibilityChanged) {
            if (visible && !delayed) {
                this.mEnteringAnimation = true;
                this.mWmService.mActivityManagerAppTransitionNotifier.onAppTransitionFinishedLocked(this.token);
            }
            if (visible || !isReallyAnimating()) {
                setClientHidden(!visible);
            }
            if (!getDisplayContent().mClosingApps.contains(this) && !getDisplayContent().mOpeningApps.contains(this)) {
                getDisplayContent().getDockedDividerController().notifyAppVisibilityChanged();
                this.mWmService.mTaskSnapshotController.notifyAppVisibilityChanged(this, visible);
            }
            if (isHidden() && !delayed && !getDisplayContent().mAppTransition.isTransitionSet()) {
                SurfaceControl.openTransaction();
                for (int i3 = this.mChildren.size() - 1; i3 >= 0; i3--) {
                    ((WindowState) this.mChildren.get(i3)).mWinAnimator.hide("immediately hidden");
                }
                SurfaceControl.closeTransaction();
            }
            reportDescendantOrientationChangeIfNeeded();
        }
        return delayed;
    }

    private void reportDescendantOrientationChangeIfNeeded() {
        if (this.mActivityRecord.getRequestedConfigurationOrientation() != getConfiguration().orientation && getOrientationIgnoreVisibility() != -2) {
            ActivityRecord activityRecord = this.mActivityRecord;
            onDescendantOrientationChanged(activityRecord.mayFreezeScreenLocked(activityRecord.app) ? this.mActivityRecord.appToken : null, this.mActivityRecord);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState getTopFullscreenWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.mChildren.get(i);
            if (win != null && win.mAttrs.isFullscreen()) {
                return win;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowState findMainWindow() {
        return findMainWindow(true);
    }

    /* access modifiers changed from: package-private */
    public WindowState findMainWindow(boolean includeStartingApp) {
        WindowState candidate = null;
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            WindowState win = (WindowState) this.mChildren.get(j);
            int type = win.mAttrs.type;
            if (type == 1 || (includeStartingApp && type == 3)) {
                if (!win.mAnimatingExit) {
                    return win;
                }
                candidate = win;
            }
        }
        return candidate;
    }

    /* access modifiers changed from: package-private */
    public boolean windowsAreFocusable() {
        if (this.mTargetSdk < 29) {
            ActivityRecord activityRecord = this.mActivityRecord;
            AppWindowToken topFocusedAppOfMyProcess = this.mWmService.mRoot.mTopFocusedAppByProcess.get(Integer.valueOf((activityRecord == null || activityRecord.app == null) ? 0 : this.mActivityRecord.app.getPid()));
            if (!(topFocusedAppOfMyProcess == null || topFocusedAppOfMyProcess == this)) {
                return false;
            }
        }
        if (getWindowConfiguration().canReceiveKeys() || this.mAlwaysFocusable) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isVisible() {
        return !isHidden();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.WindowToken
    public void removeImmediately() {
        onRemovedFromDisplay();
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.unregisterConfigurationChangeListener(this);
        }
        super.removeImmediately();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeIfPossible() {
        this.mIsExiting = false;
        removeAllWindowsIfPossible();
        removeImmediately();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean checkCompleteDeferredRemoval() {
        if (this.mIsExiting) {
            removeIfPossible();
        }
        return super.checkCompleteDeferredRemoval();
    }

    /* access modifiers changed from: package-private */
    public void onRemovedFromDisplay() {
        if (!this.mRemovingFromDisplay) {
            this.mRemovingFromDisplay = true;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Removing app token: " + this);
            }
            boolean delayed = commitVisibility(null, false, -1, true, this.mVoiceInteraction);
            getDisplayContent().mOpeningApps.remove(this);
            getDisplayContent().mChangingApps.remove(this);
            getDisplayContent().mUnknownAppVisibilityController.appRemovedOrHidden(this);
            this.mWmService.mTaskSnapshotController.onAppRemoved(this);
            this.waitingToShow = false;
            if (getDisplayContent().mClosingApps.contains(this)) {
                delayed = true;
            } else if (getDisplayContent().mAppTransition.isTransitionSet()) {
                getDisplayContent().mClosingApps.add(this);
                delayed = true;
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Removing app " + this + " delayed=" + delayed + " animation=" + getAnimation() + " animating=" + isSelfAnimating());
            }
            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT) {
                Slog.v("WindowManager", "removeAppToken: " + this + " delayed=" + delayed + " Callers=" + Debug.getCallers(4));
            }
            if (this.mStartingData != null) {
                removeStartingWindow();
            }
            if (isSelfAnimating()) {
                getDisplayContent().mNoAnimationNotifyOnTransitionFinished.add(this.token);
            }
            TaskStack stack = getStack();
            if (!delayed || isEmpty()) {
                cancelAnimation();
                if (stack != null) {
                    stack.mExitingAppTokens.remove(this);
                }
                removeIfPossible();
            } else {
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT) {
                    Slog.v("WindowManager", "removeAppToken make exiting: " + this);
                }
                if (stack != null) {
                    stack.mExitingAppTokens.add(this);
                }
                this.mIsExiting = true;
            }
            this.removed = true;
            stopFreezingScreen(true, true);
            DisplayContent dc = getDisplayContent();
            if (dc.mFocusedApp == this) {
                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                    Slog.v("WindowManager", "Removing focused app token:" + this + " displayId=" + dc.getDisplayId());
                }
                dc.setFocusedApp(null);
                this.mWmService.updateFocusedWindowLocked(0, true);
            }
            Letterbox letterbox = this.mLetterbox;
            if (letterbox != null) {
                letterbox.destroy();
                this.mLetterbox = null;
            }
            if (!delayed) {
                updateReportedVisibilityLocked();
            }
            this.mRemovingFromDisplay = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAnimatingFlags() {
        boolean wallpaperMightChange = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            wallpaperMightChange |= ((WindowState) this.mChildren.get(i)).clearAnimatingFlags();
        }
        if (wallpaperMightChange) {
            requestUpdateWallpaperIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void destroySurfaces() {
        destroySurfaces(false);
    }

    private void destroySurfaces(boolean cleanupOnResume) {
        boolean destroyedSomething = false;
        ArrayList<WindowState> children = new ArrayList<>(this.mChildren);
        for (int i = children.size() - 1; i >= 0; i--) {
            WindowState win = children.get(i);
            if (!OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).handleDestroySurfaces(this.mActivityRecord.packageName, win.mAttrs.type)) {
                destroyedSomething |= win.destroySurface(cleanupOnResume, this.mAppStopped);
            }
        }
        if (destroyedSomething) {
            getDisplayContent().assignWindowLayers(true);
            updateLetterboxSurface(null);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAppResumed(boolean wasStopped) {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "notifyAppResumed: wasStopped=" + wasStopped + StringUtils.SPACE + this);
        }
        this.mAppStopped = false;
        setCanTurnScreenOn(true);
        if (!wasStopped) {
            destroySurfaces(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAppStopped() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "notifyAppStopped: " + this);
        }
        this.mAppStopped = true;
        destroySurfaces();
        removeStartingWindow();
    }

    /* access modifiers changed from: package-private */
    public void clearAllDrawn() {
        this.allDrawn = false;
        this.deferClearAllDrawn = false;
    }

    /* access modifiers changed from: package-private */
    public Task getTask() {
        return (Task) getParent();
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack() {
        Task task = getTask();
        if (task != null) {
            return task.mStack;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry;
        super.onParentChanged();
        Task task = getTask();
        if (!this.mReparenting) {
            if (task == null) {
                getDisplayContent().mClosingApps.remove(this);
            } else {
                Task task2 = this.mLastParent;
                if (!(task2 == null || task2.mStack == null)) {
                    task.mStack.mExitingAppTokens.remove(this);
                }
            }
        }
        TaskStack stack = getStack();
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry2 = this.mAnimatingAppWindowTokenRegistry;
        if (animatingAppWindowTokenRegistry2 != null) {
            animatingAppWindowTokenRegistry2.notifyFinished(this);
        }
        if (stack != null) {
            animatingAppWindowTokenRegistry = stack.getAnimatingAppWindowTokenRegistry();
        } else {
            animatingAppWindowTokenRegistry = null;
        }
        this.mAnimatingAppWindowTokenRegistry = animatingAppWindowTokenRegistry;
        this.mLastParent = task;
        updateColorTransform();
    }

    /* access modifiers changed from: package-private */
    public void onTaskParentChanged() {
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry;
        TaskStack stack = getStack();
        cancelAnimation();
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry2 = this.mAnimatingAppWindowTokenRegistry;
        if (animatingAppWindowTokenRegistry2 != null) {
            animatingAppWindowTokenRegistry2.notifyFinished(this);
        }
        if (stack != null) {
            animatingAppWindowTokenRegistry = stack.getAnimatingAppWindowTokenRegistry();
        } else {
            animatingAppWindowTokenRegistry = null;
        }
        this.mAnimatingAppWindowTokenRegistry = animatingAppWindowTokenRegistry;
    }

    /* access modifiers changed from: package-private */
    public void postWindowRemoveStartingWindowCleanup(WindowState win) {
        if (this.startingWindow == win) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Notify removed startingWindow " + win);
            }
            removeStartingWindow();
        } else if (this.mChildren.size() == 0) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Nulling last startingData");
            }
            this.mStartingData = null;
            if (this.mHiddenSetFromTransferredStartingWindow) {
                setHidden(true);
            }
        } else if (this.mChildren.size() == 1 && this.startingSurface != null && !isRelaunching()) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Last window, removing starting window " + win);
            }
            removeStartingWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeDeadWindows() {
        for (int winNdx = this.mChildren.size() - 1; winNdx >= 0; winNdx--) {
            WindowState win = (WindowState) this.mChildren.get(winNdx);
            if (win.mAppDied) {
                if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.w("WindowManager", "removeDeadWindows: " + win);
                }
                win.mDestroying = true;
                win.removeIfPossible();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasWindowsAlive() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (!((WindowState) this.mChildren.get(i)).mAppDied) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceWindows(boolean animate) {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d("WindowManager", "Marking app token " + this + " with replacing windows.");
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceWindow(animate);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceChildWindows() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d("WindowManager", "Marking app token " + this + " with replacing child windows.");
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceChildWindows();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWillReplaceWindows() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d("WindowManager", "Resetting app token " + this + " of replacing window marks.");
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).clearWillReplaceWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public void requestUpdateWallpaperIfNeeded() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).requestUpdateWallpaperIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isRelaunching() {
        return this.mPendingRelaunchCount > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldFreezeBounds() {
        Task task = getTask();
        if (task == null || task.inFreeformWindowingMode()) {
            return false;
        }
        return getTask().isDragResizing();
    }

    /* access modifiers changed from: package-private */
    public void startRelaunching() {
        if (shouldFreezeBounds()) {
            freezeBounds();
        }
        detachChildren();
        this.mPendingRelaunchCount++;
    }

    /* access modifiers changed from: package-private */
    public void detachChildren() {
        SurfaceControl.openTransaction();
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).mWinAnimator.detachChildren();
        }
        SurfaceControl.closeTransaction();
    }

    /* access modifiers changed from: package-private */
    public void finishRelaunching() {
        unfreezeBounds();
        int i = this.mPendingRelaunchCount;
        if (i > 0) {
            this.mPendingRelaunchCount = i - 1;
        } else {
            checkKeyguardFlagsChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearRelaunching() {
        if (this.mPendingRelaunchCount != 0) {
            unfreezeBounds();
            this.mPendingRelaunchCount = 0;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowToken
    public boolean isFirstChildWindowGreaterThanSecond(WindowState newWindow, WindowState existingWindow) {
        int type1 = newWindow.mAttrs.type;
        int type2 = existingWindow.mAttrs.type;
        if (type1 == 1 && type2 != 1) {
            return false;
        }
        if (type1 == 1 || type2 != 1) {
            return (type1 == 3 && type2 != 3) || type1 == 3 || type2 != 3;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken
    public void addWindow(WindowState w) {
        super.addWindow(w);
        boolean gotReplacementWindow = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            gotReplacementWindow |= ((WindowState) this.mChildren.get(i)).setReplacementWindowIfNeeded(w);
        }
        if (gotReplacementWindow) {
            this.mWmService.scheduleWindowReplacementTimeouts(this);
        }
        checkKeyguardFlagsChanged();
    }

    /* access modifiers changed from: package-private */
    public void removeChild(WindowState child) {
        if (this.mChildren.contains(child)) {
            super.removeChild((AppWindowToken) child);
            checkKeyguardFlagsChanged();
            updateLetterboxSurface(child);
        }
    }

    private boolean waitingForReplacement() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).waitingForReplacement()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onWindowReplacementTimeout() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onWindowReplacementTimeout();
        }
    }

    /* access modifiers changed from: package-private */
    public void reparent(Task task, int position) {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i("WindowManager", "reparent: moving app token=" + this + " to task=" + task.mTaskId + " at " + position);
        }
        if (task != null) {
            Task currentTask = getTask();
            if (task == currentTask) {
                throw new IllegalArgumentException("window token=" + this + " already child of task=" + currentTask);
            } else if (currentTask.mStack == task.mStack) {
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.i("WindowManager", "reParentWindowToken: removing window token=" + this + " from task=" + currentTask);
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
                getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
            } else {
                throw new IllegalArgumentException("window token=" + this + " current task=" + currentTask + " belongs to a different stack than " + task);
            }
        } else {
            throw new IllegalArgumentException("reparent: could not find task");
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.WindowToken
    public void onDisplayChanged(DisplayContent dc) {
        Task task;
        DisplayContent prevDc = this.mDisplayContent;
        super.onDisplayChanged(dc);
        if (prevDc != null && prevDc != this.mDisplayContent) {
            if (prevDc.mOpeningApps.remove(this)) {
                this.mDisplayContent.mOpeningApps.add(this);
                this.mDisplayContent.prepareAppTransition(prevDc.mAppTransition.getAppTransition(), true);
                this.mDisplayContent.executeAppTransition();
            }
            if (prevDc.mChangingApps.remove(this)) {
                clearChangeLeash(getPendingTransaction(), true);
            }
            prevDc.mClosingApps.remove(this);
            if (prevDc.mFocusedApp == this) {
                prevDc.setFocusedApp(null);
                TaskStack stack = dc.getTopStack();
                if (!(stack == null || (task = (Task) stack.getTopChild()) == null || task.getTopChild() != this)) {
                    dc.setFocusedApp(this);
                }
            }
            Letterbox letterbox = this.mLetterbox;
            if (letterbox != null) {
                letterbox.onMovedToDisplay(this.mDisplayContent.getDisplayId());
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
            this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppLayoutChanges(int changes, String reason) {
        if (!this.mChildren.isEmpty()) {
            DisplayContent dc = getDisplayContent();
            dc.pendingLayoutChanges |= changes;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                this.mWmService.mWindowPlacerLocked.debugLayoutRepeats(reason, dc.pendingLayoutChanges);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeReplacedWindowIfNeeded(WindowState replacement) {
        int i = this.mChildren.size() - 1;
        while (i >= 0 && !((WindowState) this.mChildren.get(i)).removeReplacedWindowIfNeeded(replacement)) {
            i--;
        }
    }

    /* access modifiers changed from: package-private */
    public void startFreezingScreen() {
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            WindowManagerService.logWithStack("WindowManager", "Set freezing of " + this.appToken + ": hidden=" + isHidden() + " freezing=" + this.mFreezingScreen + " hiddenRequested=" + this.hiddenRequested);
        }
        if (!this.hiddenRequested) {
            if (!this.mFreezingScreen) {
                this.mFreezingScreen = true;
                this.mWmService.registerAppFreezeListener(this);
                this.mWmService.mAppsFreezingScreen++;
                if (this.mWmService.mAppsFreezingScreen == 1) {
                    this.mWmService.startFreezingDisplayLocked(0, 0, getDisplayContent());
                    this.mWmService.mH.removeMessages(17);
                    this.mWmService.mH.sendEmptyMessageDelayed(17, 2000);
                }
            }
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                ((WindowState) this.mChildren.get(i)).onStartFreezingScreen();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopFreezingScreen(boolean unfreezeSurfaceNow, boolean force) {
        if (this.mFreezingScreen) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Clear freezing of " + this + " force=" + force);
            }
            int count = this.mChildren.size();
            boolean unfrozeWindows = false;
            for (int i = 0; i < count; i++) {
                unfrozeWindows |= ((WindowState) this.mChildren.get(i)).onStopFreezingScreen();
            }
            if (force || unfrozeWindows) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "No longer freezing: " + this);
                }
                this.mFreezingScreen = false;
                this.mWmService.unregisterAppFreezeListener(this);
                WindowManagerService windowManagerService = this.mWmService;
                windowManagerService.mAppsFreezingScreen--;
                this.mWmService.mLastFinishedFreezeSource = this;
            }
            if (unfreezeSurfaceNow) {
                if (unfrozeWindows) {
                    this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
                }
                this.mWmService.stopFreezingDisplayLocked();
            }
        }
    }

    @Override // com.android.server.wm.WindowManagerService.AppFreezeListener
    public void onAppFreezeTimeout() {
        Slog.w("WindowManager", "Force clearing freeze: " + this);
        stopFreezingScreen(true, true);
    }

    /* access modifiers changed from: package-private */
    public void transferStartingWindowFromHiddenAboveTokenIfNeeded() {
        Task task = getTask();
        if (task != null) {
            for (int i = task.mChildren.size() - 1; i >= 0; i--) {
                AppWindowToken fromToken = (AppWindowToken) task.mChildren.get(i);
                if (fromToken == this) {
                    return;
                }
                if (fromToken.hiddenRequested && transferStartingWindow(fromToken.token)) {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean transferStartingWindow(IBinder transferFrom) {
        AppWindowToken fromToken = getDisplayContent().getAppWindowToken(transferFrom);
        if (fromToken == null) {
            return false;
        }
        WindowState tStartingWindow = fromToken.startingWindow;
        if (tStartingWindow != null && fromToken.startingSurface != null) {
            getDisplayContent().mSkipAppTransitionAnimation = true;
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Moving existing starting " + tStartingWindow + " from " + fromToken + " to " + this);
            }
            long origId = Binder.clearCallingIdentity();
            try {
                this.mStartingData = fromToken.mStartingData;
                this.startingSurface = fromToken.startingSurface;
                this.startingDisplayed = fromToken.startingDisplayed;
                fromToken.startingDisplayed = false;
                this.startingWindow = tStartingWindow;
                this.reportedVisible = fromToken.reportedVisible;
                fromToken.mStartingData = null;
                fromToken.startingSurface = null;
                fromToken.startingWindow = null;
                fromToken.startingMoved = true;
                tStartingWindow.mToken = this;
                tStartingWindow.mAppToken = this;
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Removing starting " + tStartingWindow + " from " + fromToken);
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
                if (!fromToken.isHidden()) {
                    setHidden(false);
                    this.hiddenRequested = false;
                    this.mHiddenSetFromTransferredStartingWindow = true;
                }
                setClientHidden(fromToken.mClientHidden);
                transferAnimation(fromToken);
                this.mUseTransferredAnimation = true;
                this.mWmService.updateFocusedWindowLocked(3, true);
                getDisplayContent().setLayoutNeeded();
                this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
                return true;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else if (fromToken.mStartingData == null) {
            return false;
        } else {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Moving pending starting from " + fromToken + " to " + this);
            }
            this.mStartingData = fromToken.mStartingData;
            fromToken.mStartingData = null;
            fromToken.startingMoved = true;
            scheduleAddStartingWindow();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLastWindow(WindowState win) {
        return this.mChildren.size() == 1 && this.mChildren.get(0) == win;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onAppTransitionDone() {
        this.sendingToBottom = false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public int getOrientation(int candidate) {
        if (candidate == 3) {
            return this.mOrientation;
        }
        if (this.sendingToBottom || getDisplayContent().mClosingApps.contains(this)) {
            return -2;
        }
        if (isVisible() || (getDisplayContent().mOpeningApps.contains(this) && !"com.coloros.calculator/com.android.calculator2.activity.DispatcherActivity".equals(this.mActivityComponent.flattenToShortString()))) {
            return this.mOrientation;
        }
        return -2;
    }

    /* access modifiers changed from: package-private */
    public int getOrientationIgnoreVisibility() {
        return this.mOrientation;
    }

    /* access modifiers changed from: package-private */
    public boolean inSizeCompatMode() {
        return this.mSizeCompatBounds != null;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken
    public float getSizeCompatScale() {
        return inSizeCompatMode() ? this.mSizeCompatScale : super.getSizeCompatScale();
    }

    /* access modifiers changed from: package-private */
    public Rect getResolvedOverrideBounds() {
        return getResolvedOverrideConfiguration().windowConfiguration.getBounds();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.OppoBaseAppWindowToken, com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        Rect stackBounds;
        int prevWinMode = getWindowingMode();
        this.mTmpPrevBounds.set(getBounds());
        super.onConfigurationChanged(newParentConfig);
        Task task = getTask();
        Rect overrideBounds = getResolvedOverrideBounds();
        if (task != null && !overrideBounds.isEmpty() && (task.mTaskRecord == null || task.mTaskRecord.getConfiguration().orientation == newParentConfig.orientation)) {
            Rect taskBounds = task.getBounds();
            if ((overrideBounds.width() != taskBounds.width() || overrideBounds.height() > taskBounds.height()) && !inSplitScreenWindowingMode()) {
                calculateCompatBoundsTransformation(newParentConfig);
                updateSurfacePosition();
            } else if (this.mSizeCompatBounds != null) {
                this.mSizeCompatBounds = null;
                this.mSizeCompatScale = 1.0f;
                updateSurfacePosition();
            }
        }
        int winMode = getWindowingMode();
        if (prevWinMode != winMode) {
            if (prevWinMode != 0 && winMode == 2) {
                this.mDisplayContent.mPinnedStackControllerLocked.resetReentrySnapFraction(this);
            } else if (prevWinMode == 2 && winMode != 0 && !isHidden()) {
                TaskStack pinnedStack = this.mDisplayContent.getPinnedStack();
                if (pinnedStack != null) {
                    if (pinnedStack.lastAnimatingBoundsWasToFullscreen()) {
                        stackBounds = pinnedStack.mPreAnimationBounds;
                    } else {
                        stackBounds = this.mTmpRect;
                        pinnedStack.getBounds(stackBounds);
                    }
                    this.mDisplayContent.mPinnedStackControllerLocked.saveReentrySnapFraction(this, stackBounds);
                }
            } else if (shouldStartChangeTransition(prevWinMode, winMode)) {
                initializeChangeTransition(this.mTmpPrevBounds);
            }
        }
    }

    private boolean shouldStartChangeTransition(int prevWinMode, int newWinMode) {
        if (this.mWmService.mDisableTransitionAnimation || !isVisible() || getDisplayContent().mAppTransition.isTransitionSet() || getSurfaceControl() == null) {
            return false;
        }
        if ((prevWinMode == 5) != (newWinMode == 5)) {
            return true;
        }
        return false;
    }

    private void initializeChangeTransition(Rect startBounds) {
        SurfaceControl.ScreenshotGraphicBuffer snapshot;
        this.mDisplayContent.prepareAppTransition(27, false, 0, false);
        this.mDisplayContent.mChangingApps.add(this);
        this.mTransitStartRect.set(startBounds);
        SurfaceControl.Builder parent = makeAnimationLeash().setParent(getAnimationLeashParent());
        this.mTransitChangeLeash = parent.setName(getSurfaceControl() + " - interim-change-leash").build();
        SurfaceControl.Transaction t = getPendingTransaction();
        t.setWindowCrop(this.mTransitChangeLeash, startBounds.width(), startBounds.height());
        t.setPosition(this.mTransitChangeLeash, (float) startBounds.left, (float) startBounds.top);
        t.show(this.mTransitChangeLeash);
        t.reparent(getSurfaceControl(), this.mTransitChangeLeash);
        onAnimationLeashCreated(t, this.mTransitChangeLeash);
        ArraySet<Integer> activityTypes = new ArraySet<>();
        activityTypes.add(Integer.valueOf(getActivityType()));
        RemoteAnimationAdapter adapter = this.mDisplayContent.mAppTransitionController.getRemoteAnimationOverride(this, 27, activityTypes);
        if (adapter == null || adapter.getChangeNeedsSnapshot()) {
            Task task = getTask();
            if (this.mThumbnail == null && task != null && !hasCommittedReparentToAnimationLeash() && (snapshot = this.mWmService.mTaskSnapshotController.createTaskSnapshot(task, 1.0f)) != null) {
                this.mThumbnail = new AppWindowThumbnail(t, this, snapshot.getGraphicBuffer(), true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInChangeTransition() {
        return this.mTransitChangeLeash != null || AppTransition.isChangeTransit(this.mTransit);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AppWindowThumbnail getThumbnail() {
        return this.mThumbnail;
    }

    private void calculateCompatBoundsTransformation(Configuration newParentConfig) {
        if (newParentConfig.windowConfiguration.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
            this.mSizeCompatBounds = null;
            this.mSizeCompatScale = 1.0f;
            return;
        }
        Rect parentAppBounds = newParentConfig.windowConfiguration.getAppBounds();
        Rect parentBounds = newParentConfig.windowConfiguration.getBounds();
        Rect viewportBounds = parentAppBounds != null ? parentAppBounds : parentBounds;
        Rect appBounds = getWindowConfiguration().getAppBounds();
        Rect contentBounds = appBounds != null ? appBounds : getResolvedOverrideBounds();
        float contentW = (float) contentBounds.width();
        float contentH = (float) contentBounds.height();
        float viewportW = (float) viewportBounds.width();
        float viewportH = (float) viewportBounds.height();
        this.mSizeCompatScale = (contentW > viewportW || contentH > viewportH) ? Math.min(viewportW / contentW, viewportH / contentH) : 1.0f;
        int offsetX = ((int) (((viewportW - (this.mSizeCompatScale * contentW)) + 1.0f) * 0.5f)) + viewportBounds.left;
        if (this.mWmService != null) {
            offsetX = OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).calculateCompatBoundsTransformation(offsetX, viewportBounds, viewportW, contentW, this.mSizeCompatScale);
        }
        if (this.mSizeCompatBounds == null) {
            this.mSizeCompatBounds = new Rect();
        }
        this.mSizeCompatBounds.set(contentBounds);
        this.mSizeCompatBounds.offsetTo(0, 0);
        this.mSizeCompatBounds.scale(this.mSizeCompatScale);
        this.mSizeCompatBounds.top = parentBounds.top;
        this.mSizeCompatBounds.bottom += viewportBounds.top;
        this.mSizeCompatBounds.left += offsetX;
        this.mSizeCompatBounds.right += offsetX;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public Rect getBounds() {
        if (this.mSizeCompatBounds == null) {
            return super.getBounds();
        }
        if (getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
            return super.getBounds();
        }
        return this.mSizeCompatBounds;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public boolean matchParentBounds() {
        WindowContainer parent;
        if (!super.matchParentBounds() && (parent = getParent()) != null && !parent.getBounds().equals(getResolvedOverrideBounds())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void checkAppWindowsReadyToShow() {
        boolean z = this.allDrawn;
        if (z != this.mLastAllDrawn) {
            this.mLastAllDrawn = z;
            if (z) {
                if (this.mFreezingScreen) {
                    showAllWindowsLocked();
                    stopFreezingScreen(false, true);
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.i("WindowManager", "Setting mOrientationChangeComplete=true because wtoken " + this + " numInteresting=" + this.mNumInterestingWindows + " numDrawn=" + this.mNumDrawnWindows);
                    }
                    setAppLayoutChanges(4, "checkAppWindowsReadyToShow: freezingScreen");
                    return;
                }
                setAppLayoutChanges(8, "checkAppWindowsReadyToShow");
                if (!getDisplayContent().mOpeningApps.contains(this) && canShowWindows()) {
                    showAllWindowsLocked();
                }
            }
        }
    }

    private boolean allDrawnStatesConsidered() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState child = (WindowState) this.mChildren.get(i);
            if (child.mightAffectAllDrawn() && !child.getDrawnStateEvaluated()) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void updateAllDrawn() {
        int numInteresting;
        if (!this.allDrawn && (numInteresting = this.mNumInterestingWindows) > 0 && allDrawnStatesConsidered() && this.mNumDrawnWindows >= numInteresting && !isRelaunching()) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "allDrawn: " + this + " interesting=" + numInteresting + " drawn=" + this.mNumDrawnWindows);
            }
            this.allDrawn = true;
            if (this.mDisplayContent != null) {
                this.mDisplayContent.setLayoutNeeded();
            }
            this.mWmService.mH.obtainMessage(32, this.token).sendToTarget();
            TaskStack pinnedStack = this.mDisplayContent.getPinnedStack();
            if (pinnedStack != null) {
                pinnedStack.onAllWindowsDrawn();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean keyDispatchingTimedOut(String reason, int windowPid) {
        ActivityRecord activityRecord = this.mActivityRecord;
        return activityRecord != null && activityRecord.keyDispatchingTimedOut(reason, windowPid);
    }

    /* access modifiers changed from: package-private */
    public boolean updateDrawnWindowStates(WindowState w) {
        w.setDrawnStateEvaluated(true);
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE && w == this.startingWindow) {
            Slog.d("WindowManager", "updateWindows: starting " + w + " isOnScreen=" + w.isOnScreen() + " allDrawn=" + this.allDrawn + " freezingScreen=" + this.mFreezingScreen);
        }
        if (this.allDrawn && !this.mFreezingScreen) {
            return false;
        }
        if (this.mLastTransactionSequence != ((long) this.mWmService.mTransactionSequence)) {
            this.mLastTransactionSequence = (long) this.mWmService.mTransactionSequence;
            this.mNumDrawnWindows = 0;
            this.startingDisplayed = false;
            this.mNumInterestingWindows = findMainWindow(false) != null ? 1 : 0;
        }
        WindowStateAnimator winAnimator = w.mWinAnimator;
        if (this.allDrawn || !w.mightAffectAllDrawn()) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v("WindowManager", "Eval win " + w + ": isDrawn=" + w.isDrawnLw() + ", isAnimationSet=" + isSelfAnimating());
            if (!w.isDrawnLw()) {
                Slog.v("WindowManager", "Not displayed: s=" + winAnimator.mSurfaceController + " pv=" + w.isVisibleByPolicy() + " mDrawState=" + winAnimator.drawStateToString() + " ph=" + w.isParentWindowHidden() + " th=" + this.hiddenRequested + " a=" + isSelfAnimating());
            }
        }
        if (w != this.startingWindow) {
            if (!w.isInteresting()) {
                return false;
            }
            if (findMainWindow(false) != w) {
                this.mNumInterestingWindows++;
            }
            if (!w.isDrawnLw()) {
                return false;
            }
            this.mNumDrawnWindows++;
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "tokenMayBeDrawn: " + this + " w=" + w + " numInteresting=" + this.mNumInterestingWindows + " freezingScreen=" + this.mFreezingScreen + " mAppFreezing=" + w.mAppFreezing);
            }
            return true;
        } else if (!w.isDrawnLw()) {
            return false;
        } else {
            ActivityRecord activityRecord = this.mActivityRecord;
            if (activityRecord != null) {
                activityRecord.onStartingWindowDrawn(SystemClock.uptimeMillis());
            }
            this.startingDisplayed = true;
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void layoutLetterbox(WindowState winHint) {
        WindowState w = findMainWindow();
        if (w == null) {
            return;
        }
        if (winHint == null || w == winHint) {
            boolean needsLetterbox = false;
            if ((w.isDrawnLw() || w.mWinAnimator.mSurfaceDestroyDeferred || w.isDragResizeChanged()) && w.isLetterboxedAppWindow() && fillsParent()) {
                needsLetterbox = true;
            }
            if (needsLetterbox) {
                if (this.mLetterbox == null) {
                    this.mLetterbox = new Letterbox(new Supplier() {
                        /* class com.android.server.wm.$$Lambda$AppWindowToken$kWpxOpxJiMwx92ZTbqi9WL8d2s */

                        @Override // java.util.function.Supplier
                        public final Object get() {
                            return AppWindowToken.this.lambda$layoutLetterbox$3$AppWindowToken();
                        }
                    });
                    this.mLetterbox.attachInput(w);
                }
                getPosition(this.mTmpPoint);
                this.mLetterbox.layout((inMultiWindowMode() || getStack() == null) ? getTask().getDisplayedBounds() : getStack().getDisplayedBounds(), w.getFrameLw(), this.mTmpPoint);
                return;
            }
            Letterbox letterbox = this.mLetterbox;
            if (letterbox != null) {
                letterbox.hide();
            }
        }
    }

    public /* synthetic */ SurfaceControl.Builder lambda$layoutLetterbox$3$AppWindowToken() {
        return makeChildSurface(null);
    }

    /* access modifiers changed from: package-private */
    public void updateLetterboxSurface(WindowState winHint) {
        WindowState w = findMainWindow();
        if (w == winHint || winHint == null || w == null) {
            layoutLetterbox(winHint);
            Letterbox letterbox = this.mLetterbox;
            if (letterbox != null && letterbox.needsApplySurfaceChanges()) {
                this.mLetterbox.applySurfaceChanges(getPendingTransaction());
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (!this.mIsExiting || waitingForReplacement()) {
            return forAllWindowsUnchecked(callback, traverseTopToBottom);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void forAllAppWindows(Consumer<AppWindowToken> callback) {
        callback.accept(this);
    }

    /* access modifiers changed from: package-private */
    public boolean forAllWindowsUnchecked(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        return super.forAllWindows(callback, traverseTopToBottom);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken
    public AppWindowToken asAppWindowToken() {
        return this;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0196 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0197  */
    public boolean addStartingWindow(String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents) {
        int windowFlags2;
        if (!okToDisplay() || this.mStartingData != null) {
            return false;
        }
        WindowState mainWin = findMainWindow();
        if ((mainWin != null && mainWin.mWinAnimator.getShown()) || !OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).allowUseSnapshot(this, newTask, taskSwitch, processRunning, activityCreated)) {
            return false;
        }
        ActivityManager.TaskSnapshot snapshot = this.mWmService.mTaskSnapshotController.getSnapshot(getTask().mTaskId, getTask().mUserId, false, false);
        boolean currentDarkMode = true;
        int type = OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).getStartingWindowType(0, 2, 1);
        if (type == -1) {
            type = getStartingWindowType(newTask, taskSwitch, processRunning, allowTaskSnapshot, activityCreated, fromRecents, snapshot);
        }
        if (type == 1) {
            if (getTask().getConfiguration() == null || (getTask().getConfiguration().uiMode & 48) != 32) {
                currentDarkMode = false;
            }
            boolean isInDarkMode = ColorFrameworkFactory.getInstance().getColorDarkModeManager().isDarkModePage(this.mActivityRecord.packageName, currentDarkMode);
            if (snapshot != null && snapshot.isInDarkMode() != isInDarkMode) {
                return false;
            }
            WindowState topWin = (WindowState) getTopChild();
            if (topWin == null || topWin.mAttrs.isFullscreen()) {
                return createSnapshot(snapshot);
            }
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Skip adding snapshot starting window for non fullscreen top window, " + this);
            }
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Checking theme of starting window: 0x" + Integer.toHexString(theme));
        }
        if (theme != 0) {
            AttributeCache.Entry ent = AttributeCache.instance().get(pkg, theme, R.styleable.Window, this.mWmService.mCurrentUserId);
            if (ent == null) {
                return false;
            }
            boolean windowIsTranslucent = ent.array.getBoolean(5, false);
            boolean windowIsFloating = ent.array.getBoolean(4, false);
            boolean windowShowWallpaper = ent.array.getBoolean(14, false);
            boolean windowDisableStarting = ent.array.getBoolean(12, false);
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Translucent=" + windowIsTranslucent + " Floating=" + windowIsFloating + " ShowWallpaper=" + windowShowWallpaper);
            }
            if (windowIsTranslucent && !OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).checkSplashWindowFlag()) {
                return false;
            }
            if ((windowIsFloating || windowDisableStarting) && !OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).checkSplashWindowFlag()) {
                return false;
            }
            if (windowShowWallpaper) {
                if (getDisplayContent().mWallpaperController.getWallpaperTarget() != null) {
                    return false;
                }
                windowFlags2 = windowFlags | DumpState.DUMP_DEXOPT;
                if (!transferStartingWindow(transferFrom)) {
                    return true;
                }
                if (type != 2) {
                    return false;
                }
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Creating SplashScreenStartingData");
                }
                this.mStartingData = new SplashScreenStartingData(this.mWmService, pkg, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags2, getMergedOverrideConfiguration());
                scheduleAddStartingWindow();
                return true;
            }
        }
        windowFlags2 = windowFlags;
        if (!transferStartingWindow(transferFrom)) {
        }
    }

    private boolean createSnapshot(ActivityManager.TaskSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Creating SnapshotStartingData");
        }
        this.mStartingData = new SnapshotStartingData(this.mWmService, snapshot);
        scheduleAddStartingWindow();
        return true;
    }

    /* access modifiers changed from: package-private */
    public void scheduleAddStartingWindow() {
        if (!this.mWmService.mAnimationHandler.hasCallbacks(this.mAddStartingWindow)) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Enqueueing ADD_STARTING");
            }
            this.mWmService.mAnimationHandler.postAtFrontOfQueue(this.mAddStartingWindow);
        }
    }

    private int getStartingWindowType(boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents, ActivityManager.TaskSnapshot snapshot) {
        if (getDisplayContent().mAppTransition.getAppTransition() == 19) {
            return 0;
        }
        if (newTask || !processRunning || (taskSwitch && !activityCreated)) {
            return 2;
        }
        if (!taskSwitch || !allowTaskSnapshot) {
            return 0;
        }
        if (this.mWmService.mLowRamTaskSnapshotsAndRecents) {
            return 2;
        }
        if (snapshot == null) {
            return 0;
        }
        if (snapshotOrientationSameAsTask(snapshot)) {
            return 1;
        }
        if (!fromRecents && !OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).clearStartingWindowWhenSnapshotDiffOrientation(this)) {
            return 2;
        }
        return 0;
    }

    private boolean snapshotOrientationSameAsTask(ActivityManager.TaskSnapshot snapshot) {
        if (snapshot != null && getTask().getConfiguration().orientation == snapshot.getOrientation()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void removeStartingWindow() {
        if (this.startingWindow == null) {
            if (this.mStartingData != null) {
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Clearing startingData for token=" + this);
                }
                this.mStartingData = null;
            }
        } else if (this.mStartingData != null) {
            WindowManagerPolicy.StartingSurface surface = this.startingSurface;
            this.mStartingData = null;
            this.startingSurface = null;
            this.startingWindow = null;
            this.startingDisplayed = false;
            if (surface != null) {
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Schedule remove starting " + this + " startingWindow=" + this.startingWindow + " startingView=" + this.startingSurface + " Callers=" + Debug.getCallers(5));
                }
                if (!OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).interceptRemoveStartingWindow(this.mActivityRecord.packageName, this.mWmService.mAnimationHandler, surface)) {
                    this.mWmService.mAnimationHandler.post(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$AppWindowToken$4wx593XO55AcDD3O91QAS0fIHY */

                        public final void run() {
                            AppWindowToken.lambda$removeStartingWindow$4(WindowManagerPolicy.StartingSurface.this);
                        }
                    });
                }
            } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "startingWindow was set but startingSurface==null, couldn't remove");
            }
        } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Tried to remove starting window but startingWindow was null:" + this);
        }
    }

    static /* synthetic */ void lambda$removeStartingWindow$4(WindowManagerPolicy.StartingSurface surface) {
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Removing startingView=" + surface);
        }
        try {
            surface.remove();
        } catch (Exception e) {
            Slog.w("WindowManager", "Exception when removing starting window", e);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean fillsParent() {
        return this.mFillsParent;
    }

    /* access modifiers changed from: package-private */
    public void setFillsParent(boolean fillsParent) {
        this.mFillsParent = fillsParent;
    }

    /* access modifiers changed from: package-private */
    public boolean containsDismissKeyguardWindow() {
        if (isRelaunching()) {
            return this.mLastContainsDismissKeyguardWindow;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if ((((WindowState) this.mChildren.get(i)).mAttrs.flags & DumpState.DUMP_CHANGES) != 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean containsShowWhenLockedWindow() {
        if (isRelaunching()) {
            return this.mLastContainsShowWhenLockedWindow;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if ((((WindowState) this.mChildren.get(i)).mAttrs.flags & DumpState.DUMP_FROZEN) != 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void checkKeyguardFlagsChanged() {
        boolean containsDismissKeyguard = containsDismissKeyguardWindow();
        boolean containsShowWhenLocked = containsShowWhenLockedWindow();
        if (!(containsDismissKeyguard == this.mLastContainsDismissKeyguardWindow && containsShowWhenLocked == this.mLastContainsShowWhenLockedWindow)) {
            this.mWmService.notifyKeyguardFlagsChanged(null, getDisplayContent().getDisplayId());
        }
        this.mLastContainsDismissKeyguardWindow = containsDismissKeyguard;
        this.mLastContainsShowWhenLockedWindow = containsShowWhenLocked;
    }

    /* access modifiers changed from: package-private */
    public WindowState getImeTargetBelowWindow(WindowState w) {
        int index = this.mChildren.indexOf(w);
        if (index <= 0) {
            return null;
        }
        WindowState target = (WindowState) this.mChildren.get(index - 1);
        if (target.canBeImeTarget()) {
            return target;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowState getHighestAnimLayerWindow(WindowState currentTarget) {
        WindowState candidate = null;
        for (int i = this.mChildren.indexOf(currentTarget); i >= 0; i--) {
            WindowState w = (WindowState) this.mChildren.get(i);
            if (!w.mRemoved && candidate == null) {
                candidate = w;
            }
        }
        return candidate;
    }

    /* access modifiers changed from: package-private */
    public void setDisablePreviewScreenshots(boolean disable) {
        this.mDisablePreviewScreenshots = disable;
    }

    /* access modifiers changed from: package-private */
    public void setCanTurnScreenOn(boolean canTurnScreenOn) {
        this.mCanTurnScreenOn = canTurnScreenOn;
    }

    /* access modifiers changed from: package-private */
    public boolean canTurnScreenOn() {
        return this.mCanTurnScreenOn;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUseAppThemeSnapshot() {
        return this.mDisablePreviewScreenshots || forAllWindows($$Lambda$AppWindowToken$Zf9XP8X2PGWYnn5VrENXlB2pEI.INSTANCE, true);
    }

    static /* synthetic */ boolean lambda$shouldUseAppThemeSnapshot$5(WindowState w) {
        if ((w.mAttrs.type != 2011 || w.isVisibleLw()) && (w.mAttrs.flags & 8192) != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl getAppAnimationLayer() {
        int i;
        if (isActivityTypeHome()) {
            i = 2;
        } else if (needsZBoost()) {
            i = 1;
        } else {
            i = 0;
        }
        return getAppAnimationLayer(i);
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer, com.android.server.wm.OppoBaseAppWindowToken
    public SurfaceControl getAnimationLeashParent() {
        if (!inPinnedWindowingMode()) {
            return getAppAnimationLayer();
        }
        return getStack().getSurfaceControl();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldAnimate(int transit) {
        boolean isSplitScreenPrimary = getWindowingMode() == 3;
        boolean allowSplitScreenPrimaryAnimation = transit != 13;
        RecentsAnimationController controller = this.mWmService.getRecentsAnimationController();
        if (controller == null || !controller.isAnimatingTask(getTask()) || !controller.shouldCancelWithDeferredScreenshot()) {
            return !isSplitScreenPrimary || allowSplitScreenPrimaryAnimation;
        }
        return false;
    }

    private SurfaceControl createAnimationBoundsLayer(SurfaceControl.Transaction t) {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i("WindowManager", "Creating animation bounds layer");
        }
        SurfaceControl.Builder parent = makeAnimationLeash().setParent(getAnimationLeashParent());
        SurfaceControl boundsLayer = parent.setName(getSurfaceControl() + " - animation-bounds").build();
        t.show(boundsLayer);
        return boundsLayer;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public Rect getDisplayedBounds() {
        Task task = getTask();
        if (task != null) {
            Rect overrideDisplayedBounds = task.getOverrideDisplayedBounds();
            if (!overrideDisplayedBounds.isEmpty()) {
                return overrideDisplayedBounds;
            }
        }
        return getBounds();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect getAnimationBounds(int appStackClipMode) {
        if (appStackClipMode != 1 || getStack() == null) {
            return getTask() != null ? getTask().getBounds() : getBounds();
        }
        return getStack().getBounds();
    }

    /* access modifiers changed from: package-private */
    public boolean applyAnimationLocked(WindowManager.LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
        boolean z;
        AnimationAdapter adapter;
        AnimationAdapter adapter2;
        float windowCornerRadius;
        boolean z2 = false;
        if (this.mWmService.mDisableTransitionAnimation || !shouldAnimate(transit)) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v("WindowManager", "applyAnimation: transition animation is disabled or skipped. atoken=" + this);
            }
            cancelAnimation();
            return false;
        }
        Trace.traceBegin(32, "AWT#applyAnimationLocked");
        if (okToAnimate()) {
            AnimationAdapter thumbnailAdapter = null;
            int appStackClipMode = getDisplayContent().mAppTransition.getAppStackClipMode();
            this.mTmpRect.set(getAnimationBounds(appStackClipMode));
            this.mTmpPoint.set(this.mTmpRect.left, this.mTmpRect.top);
            this.mTmpRect.offsetTo(0, 0);
            boolean isChanging = AppTransition.isChangeTransit(transit) && enter && getDisplayContent().mChangingApps.contains(this);
            if (getDisplayContent().mAppTransition.getRemoteAnimationController() != null && !this.mSurfaceAnimator.isAnimationStartDelayed()) {
                RemoteAnimationController.RemoteAnimationRecord adapters = getDisplayContent().mAppTransition.getRemoteAnimationController().createRemoteAnimationRecord(this, this.mTmpPoint, this.mTmpRect, isChanging ? this.mTransitStartRect : null);
                adapter = adapters.mAdapter;
                thumbnailAdapter = adapters.mThumbnailAdapter;
                z = true;
            } else if (isChanging) {
                float durationScale = this.mWmService.getTransitionAnimationScaleLocked();
                this.mTmpRect.offsetTo(this.mTmpPoint.x, this.mTmpPoint.y);
                adapter = new LocalAnimationAdapter(new WindowChangeAnimationSpec(this.mTransitStartRect, this.mTmpRect, getDisplayContent().getDisplayInfo(), durationScale, true, false), this.mWmService.mSurfaceAnimationRunner);
                if (this.mThumbnail != null) {
                    thumbnailAdapter = new LocalAnimationAdapter(new WindowChangeAnimationSpec(this.mTransitStartRect, this.mTmpRect, getDisplayContent().getDisplayInfo(), durationScale, true, true), this.mWmService.mSurfaceAnimationRunner);
                }
                this.mTransit = transit;
                this.mTransitFlags = getDisplayContent().mAppTransition.getTransitFlags();
                z = true;
            } else {
                if (appStackClipMode == 0) {
                    z2 = true;
                }
                this.mNeedsAnimationBoundsLayer = z2;
                Animation a = loadAnimation(lp, transit, enter, isVoiceInteraction);
                if (a != null) {
                    if (!inMultiWindowMode()) {
                        windowCornerRadius = getDisplayContent().getWindowCornerRadius();
                    } else {
                        windowCornerRadius = 0.0f;
                    }
                    AnimationAdapter adapter3 = new LocalAnimationAdapter(new WindowAnimationSpec(a, this.mTmpPoint, this.mTmpRect, getDisplayContent().mAppTransition.canSkipFirstFrame(), appStackClipMode, true, windowCornerRadius), this.mWmService.mSurfaceAnimationRunner);
                    if (a.getZAdjustment() == 1) {
                        this.mNeedsZBoost = true;
                    }
                    this.mTransit = transit;
                    this.mTransitFlags = getDisplayContent().mAppTransition.getTransitFlags();
                    adapter2 = adapter3;
                } else {
                    adapter2 = null;
                }
                z = true;
                adapter = OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).getZoomAnimationAdapter(adapter2, this, a, appStackClipMode, this.mTransitStartRect, transit, enter);
            }
            if (adapter != null) {
                startAnimation(getPendingTransaction(), adapter, isVisible() ^ z);
                if (adapter.getShowWallpaper()) {
                    this.mDisplayContent.pendingLayoutChanges |= 4;
                }
                if (thumbnailAdapter != null) {
                    this.mThumbnail.startAnimation(getPendingTransaction(), thumbnailAdapter, isVisible() ^ z);
                }
            }
        } else {
            cancelAnimation();
        }
        Trace.traceEnd(32);
        return isReallyAnimating();
    }

    private Animation loadAnimation(WindowManager.LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
        Rect surfaceInsets;
        boolean enter2;
        DisplayContent displayContent = getTask().getDisplayContent();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        int width = displayInfo.appWidth;
        int height = displayInfo.appHeight;
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v("WindowManager", "applyAnimation: atoken=" + this);
        }
        WindowState win = findMainWindow();
        boolean freeform = false;
        Rect frame = new Rect(0, 0, width, height);
        Rect displayFrame = new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        Rect insets = new Rect();
        Rect stableInsets = new Rect();
        if (win != null && win.inFreeformWindowingMode()) {
            freeform = true;
        }
        if (win != null) {
            if (freeform) {
                frame.set(win.getFrameLw());
            } else if (win.isLetterboxedAppWindow()) {
                frame.set(getTask().getBounds());
            } else if (win.isDockedResizing()) {
                frame.set(getTask().getParent().getBounds());
            } else {
                frame.set(win.getContainingFrame());
            }
            Rect surfaceInsets2 = win.getAttrs().surfaceInsets;
            win.getContentInsets(insets);
            win.getStableInsets(stableInsets);
            surfaceInsets = surfaceInsets2;
        } else {
            surfaceInsets = null;
        }
        if (this.mLaunchTaskBehind) {
            enter2 = false;
        } else {
            enter2 = enter;
        }
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.d("WindowManager", "Loading animation for app transition. transit=" + AppTransition.appTransitionToString(transit) + " enter=" + enter2 + " frame=" + frame + " insets=" + insets + " surfaceInsets=" + surfaceInsets);
        }
        Configuration displayConfig = displayContent.getConfiguration();
        Animation a = getDisplayContent().mAppTransition.loadAnimation(lp, transit, enter2, displayConfig.uiMode, displayConfig.orientation, frame, displayFrame, insets, surfaceInsets, stableInsets, isVoiceInteraction, freeform, getTask().mTaskId);
        if (a == null && isActivityTypeHome() && !enter2 && this.mDisplayContent.mSkipAppTransitionAnimation) {
            a = OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).createAnimationForLauncherExit();
        }
        if (a != null) {
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                WindowManagerService.logWithStack("WindowManager", "Loaded animation " + a + " for " + this);
            }
            a.initialize(frame.width(), frame.height(), width, height);
            a.scaleCurrentDuration(this.mWmService.getTransitionAnimationScaleLocked());
        }
        return a;
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public boolean shouldDeferAnimationFinish(Runnable endDeferFinishCallback) {
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry = this.mAnimatingAppWindowTokenRegistry;
        return animatingAppWindowTokenRegistry != null && animatingAppWindowTokenRegistry.notifyAboutToFinish(this, endDeferFinishCallback);
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer, com.android.server.wm.OppoBaseAppWindowToken
    public void onAnimationLeashLost(SurfaceControl.Transaction t) {
        super.onAnimationLeashLost(t);
        SurfaceControl surfaceControl = this.mAnimationBoundsLayer;
        if (surfaceControl != null) {
            t.remove(surfaceControl);
            this.mAnimationBoundsLayer = null;
        }
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry = this.mAnimatingAppWindowTokenRegistry;
        if (animatingAppWindowTokenRegistry != null) {
            animatingAppWindowTokenRegistry.notifyFinished(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void setLayer(SurfaceControl.Transaction t, int layer) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.setLayer(this.mSurfaceControl, layer);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void setRelativeLayer(SurfaceControl.Transaction t, SurfaceControl relativeTo, int layer) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.setRelativeLayer(this.mSurfaceControl, relativeTo, layer);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void reparentSurfaceControl(SurfaceControl.Transaction t, SurfaceControl newParent) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.reparent(this.mSurfaceControl, newParent);
        }
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer, com.android.server.wm.OppoBaseAppWindowToken
    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        int layer;
        if (!inPinnedWindowingMode()) {
            layer = getPrefixOrderIndex();
        } else {
            layer = getParent().getPrefixOrderIndex();
        }
        if (this.mNeedsZBoost) {
            layer += Z_BOOST_BASE;
        }
        if (!this.mNeedsAnimationBoundsLayer) {
            leash.setLayer(layer);
        }
        getDisplayContent().assignStackOrdering();
        SurfaceControl surfaceControl = this.mTransitChangeLeash;
        if (leash != surfaceControl) {
            if (surfaceControl != null) {
                clearChangeLeash(t, false);
            }
            AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry = this.mAnimatingAppWindowTokenRegistry;
            if (animatingAppWindowTokenRegistry != null) {
                animatingAppWindowTokenRegistry.notifyStarting(this);
            }
            if (this.mNeedsAnimationBoundsLayer) {
                this.mTmpRect.setEmpty();
                Task task = getTask();
                if (getDisplayContent().mAppTransitionController.isTransitWithinTask(getTransit(), task)) {
                    task.getBounds(this.mTmpRect);
                } else {
                    TaskStack stack = getStack();
                    if (stack != null) {
                        stack.getBounds(this.mTmpRect);
                    } else {
                        return;
                    }
                }
                this.mAnimationBoundsLayer = createAnimationBoundsLayer(t);
                OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).adjustWindowCropForLeash(this, this.mTmpRect);
                t.setWindowCrop(this.mAnimationBoundsLayer, this.mTmpRect);
                t.setLayer(this.mAnimationBoundsLayer, layer);
                t.reparent(leash, this.mAnimationBoundsLayer);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void showAllWindowsLocked() {
        forAllWindows((Consumer<WindowState>) $$Lambda$AppWindowToken$GO_44j7HKFWrNpwWGQ4totlKXW8.INSTANCE, false);
    }

    static /* synthetic */ void lambda$showAllWindowsLocked$6(WindowState windowState) {
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v("WindowManager", "performing show on: " + windowState);
        }
        windowState.performShowLocked();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void onAnimationFinished() {
        super.onAnimationFinished();
        Trace.traceBegin(32, "AWT#onAnimationFinished");
        this.mTransit = -1;
        boolean z = false;
        this.mTransitFlags = 0;
        this.mNeedsZBoost = false;
        this.mNeedsAnimationBoundsLayer = false;
        setAppLayoutChanges(12, "AppWindowToken");
        clearThumbnail();
        if (isHidden() && this.hiddenRequested) {
            z = true;
        }
        setClientHidden(z);
        getDisplayContent().computeImeTargetIfNeeded(this);
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v("WindowManager", "Animation done in " + this + ": reportedVisible=" + this.reportedVisible + " okToDisplay=" + okToDisplay() + " okToAnimate=" + okToAnimate() + " startingDisplayed=" + this.startingDisplayed);
        }
        AppWindowThumbnail appWindowThumbnail = this.mThumbnail;
        if (appWindowThumbnail != null) {
            appWindowThumbnail.destroy();
            this.mThumbnail = null;
        }
        new ArrayList<>(this.mChildren).forEach($$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4.INSTANCE);
        getDisplayContent().mAppTransition.notifyAppTransitionFinishedLocked(this.token);
        scheduleAnimation();
        this.mActivityRecord.onAnimationFinished();
        Trace.traceEnd(32);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isAppAnimating() {
        return isSelfAnimating();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isSelfAnimating() {
        return isWaitingForTransitionStart() || isReallyAnimating();
    }

    private boolean isReallyAnimating() {
        return super.isSelfAnimating();
    }

    private void clearChangeLeash(SurfaceControl.Transaction t, boolean cancel) {
        if (this.mTransitChangeLeash != null) {
            if (cancel) {
                clearThumbnail();
                SurfaceControl sc = getSurfaceControl();
                if (!(getParentSurfaceControl() == null || sc == null)) {
                    t.reparent(sc, getParentSurfaceControl());
                }
            }
            t.hide(this.mTransitChangeLeash);
            t.remove(this.mTransitChangeLeash);
            this.mTransitChangeLeash = null;
            if (cancel) {
                onAnimationLeashLost(t);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void cancelAnimation() {
        cancelAnimationOnly();
        clearThumbnail();
        clearChangeLeash(getPendingTransaction(), true);
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimationOnly() {
        super.cancelAnimation();
    }

    /* access modifiers changed from: package-private */
    public boolean isWaitingForTransitionStart() {
        return getDisplayContent().mAppTransition.isTransitionSet() && (getDisplayContent().mOpeningApps.contains(this) || getDisplayContent().mClosingApps.contains(this) || getDisplayContent().mChangingApps.contains(this));
    }

    public int getTransit() {
        return this.mTransit;
    }

    /* access modifiers changed from: package-private */
    public int getTransitFlags() {
        return this.mTransitFlags;
    }

    /* access modifiers changed from: package-private */
    public void attachThumbnailAnimation() {
        if (isReallyAnimating()) {
            int taskId = getTask().mTaskId;
            GraphicBuffer thumbnailHeader = getDisplayContent().mAppTransition.getAppTransitionThumbnailHeader(taskId);
            if (thumbnailHeader != null) {
                clearThumbnail();
                this.mThumbnail = new AppWindowThumbnail(getPendingTransaction(), this, thumbnailHeader);
                this.mThumbnail.startAnimation(getPendingTransaction(), loadThumbnailAnimation(thumbnailHeader));
            } else if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.d("WindowManager", "No thumbnail header bitmap for: " + taskId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void attachCrossProfileAppsThumbnailAnimation() {
        int thumbnailDrawableRes;
        if (isReallyAnimating()) {
            clearThumbnail();
            WindowState win = findMainWindow();
            if (win != null) {
                Rect frame = win.getFrameLw();
                if (getTask().mUserId == this.mWmService.mCurrentUserId) {
                    thumbnailDrawableRes = 201852336;
                } else {
                    thumbnailDrawableRes = 17302361;
                }
                GraphicBuffer thumbnail = getDisplayContent().mAppTransition.createCrossProfileAppsThumbnail(thumbnailDrawableRes, frame);
                if (thumbnail != null) {
                    this.mThumbnail = new AppWindowThumbnail(getPendingTransaction(), this, thumbnail);
                    this.mThumbnail.startAnimation(getPendingTransaction(), getDisplayContent().mAppTransition.createCrossProfileAppsThumbnailAnimationLocked(win.getFrameLw()), new Point(frame.left, frame.top));
                }
            }
        }
    }

    private Animation loadThumbnailAnimation(GraphicBuffer thumbnailHeader) {
        Rect appRect;
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        WindowState win = findMainWindow();
        if (win != null) {
            appRect = win.getContentFrameLw();
        } else {
            appRect = new Rect(0, 0, displayInfo.appWidth, displayInfo.appHeight);
        }
        Rect insets = win != null ? win.getContentInsets() : null;
        Configuration displayConfig = this.mDisplayContent.getConfiguration();
        return getDisplayContent().mAppTransition.createThumbnailAspectScaleAnimationLocked(appRect, insets, thumbnailHeader, getTask().mTaskId, displayConfig.uiMode, displayConfig.orientation);
    }

    private void clearThumbnail() {
        AppWindowThumbnail appWindowThumbnail = this.mThumbnail;
        if (appWindowThumbnail != null) {
            appWindowThumbnail.destroy();
            this.mThumbnail = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        this.mRemoteAnimationDefinition = definition;
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationDefinition getRemoteAnimationDefinition() {
        return this.mRemoteAnimationDefinition;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.WindowToken
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        String str;
        super.dump(pw, prefix, dumpAll);
        if (this.appToken != null) {
            pw.println(prefix + "app=true mVoiceInteraction=" + this.mVoiceInteraction);
        }
        pw.println(prefix + "component=" + this.mActivityComponent.flattenToShortString());
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
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("hiddenRequested=");
        sb.append(this.hiddenRequested);
        sb.append(" mClientHidden=");
        sb.append(this.mClientHidden);
        if (this.mDeferHidingClient) {
            str = " mDeferHidingClient=" + this.mDeferHidingClient;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append(" reportedDrawn=");
        sb.append(this.reportedDrawn);
        sb.append(" reportedVisible=");
        sb.append(this.reportedVisible);
        pw.println(sb.toString());
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
        if (this.mNumInterestingWindows != 0 || this.mNumDrawnWindows != 0 || this.allDrawn || this.mLastAllDrawn) {
            pw.print(prefix);
            pw.print("mNumInterestingWindows=");
            pw.print(this.mNumInterestingWindows);
            pw.print(" mNumDrawnWindows=");
            pw.print(this.mNumDrawnWindows);
            pw.print(" inPendingTransaction=");
            pw.print(this.inPendingTransaction);
            pw.print(" allDrawn=");
            pw.print(this.allDrawn);
            pw.print(" lastAllDrawn=");
            pw.print(this.mLastAllDrawn);
            pw.println(")");
        }
        if (this.inPendingTransaction) {
            pw.print(prefix);
            pw.print("inPendingTransaction=");
            pw.println(this.inPendingTransaction);
        }
        if (this.mStartingData != null || this.removed || this.firstWindowDrawn || this.mIsExiting) {
            pw.print(prefix);
            pw.print("startingData=");
            pw.print(this.mStartingData);
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
        if (!(this.mSizeCompatScale == 1.0f && this.mSizeCompatBounds == null)) {
            pw.println(prefix + "mSizeCompatScale=" + this.mSizeCompatScale + " mSizeCompatBounds=" + this.mSizeCompatBounds);
        }
        if (this.mRemovingFromDisplay) {
            pw.println(prefix + "mRemovingFromDisplay=" + this.mRemovingFromDisplay);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken
    public void setHidden(boolean hidden) {
        super.setHidden(hidden);
        if (hidden) {
            this.mDisplayContent.mPinnedStackControllerLocked.resetReentrySnapFraction(this);
        }
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void prepareSurfaces() {
        boolean show = !isHidden() || super.isSelfAnimating();
        if (this.mSurfaceControl != null) {
            if (show && !this.mLastSurfaceShowing) {
                getPendingTransaction().show(this.mSurfaceControl);
            } else if (!show && this.mLastSurfaceShowing) {
                getPendingTransaction().hide(this.mSurfaceControl);
            }
        }
        AppWindowThumbnail appWindowThumbnail = this.mThumbnail;
        if (appWindowThumbnail != null) {
            appWindowThumbnail.setShowing(getPendingTransaction(), show);
        }
        this.mLastSurfaceShowing = show;
        super.prepareSurfaces();
    }

    /* access modifiers changed from: package-private */
    public boolean isSurfaceShowing() {
        return this.mLastSurfaceShowing;
    }

    /* access modifiers changed from: package-private */
    public boolean isFreezingScreen() {
        return this.mFreezingScreen;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean needsZBoost() {
        return this.mNeedsZBoost || super.needsZBoost();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.WindowToken, com.android.server.wm.OppoBaseAppWindowToken, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            writeNameToProto(proto, 1138166333441L);
            super.writeToProto(proto, 1146756268034L, logLevel);
            proto.write(1133871366147L, this.mLastSurfaceShowing);
            proto.write(1133871366148L, isWaitingForTransitionStart());
            proto.write(1133871366149L, isReallyAnimating());
            AppWindowThumbnail appWindowThumbnail = this.mThumbnail;
            if (appWindowThumbnail != null) {
                appWindowThumbnail.writeToProto(proto, 1146756268038L);
            }
            proto.write(1133871366151L, this.mFillsParent);
            proto.write(1133871366152L, this.mAppStopped);
            proto.write(1133871366153L, this.hiddenRequested);
            proto.write(1133871366154L, this.mClientHidden);
            proto.write(1133871366155L, this.mDeferHidingClient);
            proto.write(1133871366156L, this.reportedDrawn);
            proto.write(1133871366157L, this.reportedVisible);
            proto.write(1120986464270L, this.mNumInterestingWindows);
            proto.write(1120986464271L, this.mNumDrawnWindows);
            proto.write(1133871366160L, this.allDrawn);
            proto.write(1133871366161L, this.mLastAllDrawn);
            proto.write(1133871366162L, this.removed);
            WindowState windowState = this.startingWindow;
            if (windowState != null) {
                windowState.writeIdentifierToProto(proto, 1146756268051L);
            }
            proto.write(1133871366164L, this.startingDisplayed);
            proto.write(1133871366165L, this.startingMoved);
            proto.write(1133871366166L, this.mHiddenSetFromTransferredStartingWindow);
            Iterator<Rect> it = this.mFrozenBounds.iterator();
            while (it.hasNext()) {
                it.next().writeToProto(proto, 2246267895831L);
            }
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeNameToProto(ProtoOutputStream proto, long fieldId) {
        IApplicationToken iApplicationToken = this.appToken;
        if (iApplicationToken != null) {
            try {
                proto.write(fieldId, iApplicationToken.getName());
            } catch (RemoteException e) {
                Slog.e("WindowManager", e.toString());
            }
        }
    }

    @Override // com.android.server.wm.WindowToken, com.android.server.wm.OppoBaseAppWindowToken
    public String toString() {
        if (this.stringName == null) {
            this.stringName = "AppWindowToken{" + Integer.toHexString(System.identityHashCode(this)) + " token=" + this.token + '}';
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.stringName);
        sb.append(this.mIsExiting ? " mIsExiting=" : "");
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public Rect getLetterboxInsets() {
        Letterbox letterbox = this.mLetterbox;
        if (letterbox != null) {
            return letterbox.getInsets();
        }
        return new Rect();
    }

    /* access modifiers changed from: package-private */
    public void getLetterboxInnerBounds(Rect outBounds) {
        Letterbox letterbox = this.mLetterbox;
        if (letterbox != null) {
            outBounds.set(letterbox.getInnerFrame());
        } else {
            outBounds.setEmpty();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLetterboxOverlappingWith(Rect rect) {
        Letterbox letterbox = this.mLetterbox;
        return letterbox != null && letterbox.isOverlappingWith(rect);
    }

    /* access modifiers changed from: package-private */
    public void setWillCloseOrEnterPip(boolean willCloseOrEnterPip) {
        this.mWillCloseOrEnterPip = willCloseOrEnterPip;
    }

    /* access modifiers changed from: package-private */
    public boolean isClosingOrEnteringPip() {
        return (isAnimating() && this.hiddenRequested) || this.mWillCloseOrEnterPip;
    }

    /* access modifiers changed from: package-private */
    public boolean canShowWindows() {
        return this.allDrawn && (!isReallyAnimating() || !hasNonDefaultColorWindow());
    }

    private boolean hasNonDefaultColorWindow() {
        return forAllWindows((ToBooleanFunction<WindowState>) $$Lambda$AppWindowToken$fPUApbLk_vYcjY_mIHRDEOCqbZU.INSTANCE, true);
    }

    static /* synthetic */ boolean lambda$hasNonDefaultColorWindow$7(WindowState ws) {
        return ws.mAttrs.getColorMode() != 0;
    }

    private void updateColorTransform() {
        if (this.mSurfaceControl != null && this.mLastAppSaturationInfo != null) {
            getPendingTransaction().setColorTransform(this.mSurfaceControl, this.mLastAppSaturationInfo.mMatrix, this.mLastAppSaturationInfo.mTranslation);
            this.mWmService.scheduleAnimationLocked();
        }
    }

    /* access modifiers changed from: private */
    public static class AppSaturationInfo {
        float[] mMatrix;
        float[] mTranslation;

        private AppSaturationInfo() {
            this.mMatrix = new float[9];
            this.mTranslation = new float[3];
        }

        /* access modifiers changed from: package-private */
        public void setSaturation(float[] matrix, float[] translation) {
            float[] fArr = this.mMatrix;
            System.arraycopy(matrix, 0, fArr, 0, fArr.length);
            float[] fArr2 = this.mTranslation;
            System.arraycopy(translation, 0, fArr2, 0, fArr2.length);
        }
    }
}
