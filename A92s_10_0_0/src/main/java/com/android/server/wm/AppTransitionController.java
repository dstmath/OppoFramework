package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.os.Debug;
import android.os.SystemClock;
import android.os.Trace;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationDefinition;
import android.view.WindowManager;
import android.view.animation.Animation;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.pm.DumpState;
import java.util.Iterator;
import java.util.function.Predicate;

public class AppTransitionController {
    private static final String TAG = "WindowManager";
    private final DisplayContent mDisplayContent;
    private RemoteAnimationDefinition mRemoteAnimationDefinition = null;
    private final WindowManagerService mService;
    private final SparseIntArray mTempTransitionReasons = new SparseIntArray();
    private final WallpaperController mWallpaperControllerLocked;

    AppTransitionController(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
        this.mWallpaperControllerLocked = this.mDisplayContent.mWallpaperController;
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        this.mRemoteAnimationDefinition = definition;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0279  */
    public void handleAppTransitionReady() {
        boolean allowAnimations;
        AppWindowToken animLpToken;
        AppWindowToken topOpeningApp;
        AppWindowToken topClosingApp;
        AppWindowToken topChangingApp;
        this.mTempTransitionReasons.clear();
        if (transitionGoodToGo(this.mDisplayContent.mOpeningApps, this.mTempTransitionReasons) && transitionGoodToGo(this.mDisplayContent.mChangingApps, this.mTempTransitionReasons)) {
            Trace.traceBegin(32, "AppTransitionReady");
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "**** GOOD TO GO, Callers=" + Debug.getCallers(6));
            } else if (OppoWMSDynamicLogConfig.DEBUG_WMS) {
                Slog.v("WindowManager", "**** GOOD TO GO");
            }
            if (this.mService.mHasWindowFreezing) {
                WindowManagerService windowManagerService = this.mService;
                windowManagerService.mHasWindowFreezed = true;
                windowManagerService.mHasWindowFreezing = false;
            }
            AppTransition appTransition = this.mDisplayContent.mAppTransition;
            int transit = appTransition.getAppTransition();
            if (OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).checkSkipTransitionAnimation(this.mDisplayContent) && this.mDisplayContent.mSkipAppTransitionAnimation && !AppTransition.isKeyguardGoingAwayTransit(transit)) {
                transit = -1;
            }
            if (!OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).skipAppTransitionAnimation()) {
                this.mDisplayContent.mSkipAppTransitionAnimation = false;
            }
            this.mDisplayContent.mNoAnimationNotifyOnTransitionFinished.clear();
            appTransition.removeAppTransitionTimeoutCallbacks();
            DisplayContent displayContent = this.mDisplayContent;
            displayContent.mWallpaperMayChange = false;
            int appCount = displayContent.mOpeningApps.size();
            for (int i = 0; i < appCount; i++) {
                ((AppWindowToken) this.mDisplayContent.mOpeningApps.valueAtUnchecked(i)).clearAnimatingFlags();
            }
            int appCount2 = this.mDisplayContent.mChangingApps.size();
            for (int i2 = 0; i2 < appCount2; i2++) {
                ((AppWindowToken) this.mDisplayContent.mChangingApps.valueAtUnchecked(i2)).clearAnimatingFlags();
            }
            this.mWallpaperControllerLocked.adjustWallpaperWindowsForAppTransitionIfNeeded(this.mDisplayContent.mOpeningApps, this.mDisplayContent.mChangingApps);
            boolean hasWallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget() != null;
            int transit2 = maybeUpdateTransitToWallpaper(maybeUpdateTransitToTranslucentAnim(transit), canBeWallpaperTarget(this.mDisplayContent.mOpeningApps) && hasWallpaperTarget, canBeWallpaperTarget(this.mDisplayContent.mClosingApps) && hasWallpaperTarget);
            ArraySet<Integer> activityTypes = collectActivityTypes(this.mDisplayContent.mOpeningApps, this.mDisplayContent.mClosingApps, this.mDisplayContent.mChangingApps);
            if (transit == 1000) {
                allowAnimations = false;
            } else {
                allowAnimations = this.mDisplayContent.getDisplayPolicy().allowAppAnimationsLw();
            }
            if (allowAnimations) {
                animLpToken = findAnimLayoutParamsToken(transit2, activityTypes);
            } else {
                animLpToken = null;
            }
            if (allowAnimations) {
                topOpeningApp = getTopApp(this.mDisplayContent.mOpeningApps, false);
            } else {
                topOpeningApp = null;
            }
            if (allowAnimations) {
                topClosingApp = getTopApp(this.mDisplayContent.mClosingApps, false);
            } else {
                topClosingApp = null;
            }
            if (allowAnimations) {
                topChangingApp = getTopApp(this.mDisplayContent.mChangingApps, false);
            } else {
                topChangingApp = null;
            }
            WindowManager.LayoutParams animLp = getAnimLp(animLpToken);
            overrideWithRemoteAnimationIfSet(animLpToken, transit2, activityTypes);
            boolean voiceInteraction = containsVoiceInteraction(this.mDisplayContent.mOpeningApps) || containsVoiceInteraction(this.mDisplayContent.mOpeningApps) || containsVoiceInteraction(this.mDisplayContent.mChangingApps);
            this.mService.mSurfaceAnimationRunner.deferStartingAnimations();
            try {
                processApplicationsAnimatingInPlace(transit2);
                handleClosingApps(transit2, animLp, voiceInteraction);
                handleOpeningApps(transit2, animLp, voiceInteraction);
                handleChangingApps(transit2, animLp, voiceInteraction);
                try {
                    appTransition.setLastAppTransition(transit2, topOpeningApp, topClosingApp, topChangingApp);
                    int flags = appTransition.getTransitFlags();
                    int layoutRedo = appTransition.goodToGo(transit2, topOpeningApp, this.mDisplayContent.mOpeningApps);
                    try {
                        handleNonAppWindowsInTransition(transit2, flags);
                        appTransition.postAnimationCallback();
                        appTransition.clear();
                        this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                        if (OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).skipAppTransitionAnimation()) {
                            this.mDisplayContent.mSkipAppTransitionAnimation = false;
                        }
                        this.mService.mTaskSnapshotController.onTransitionStarting(this.mDisplayContent);
                        this.mDisplayContent.mOpeningApps.clear();
                        this.mDisplayContent.mClosingApps.clear();
                        this.mDisplayContent.mChangingApps.clear();
                        this.mDisplayContent.mUnknownAppVisibilityController.clear();
                        this.mDisplayContent.setLayoutNeeded();
                        this.mDisplayContent.computeImeTarget(true);
                        this.mService.mAtmInternal.notifyAppTransitionStarting(this.mTempTransitionReasons.clone(), SystemClock.uptimeMillis());
                        Trace.traceEnd(32);
                        this.mDisplayContent.pendingLayoutChanges |= layoutRedo | 1 | 2;
                    } catch (Throwable th) {
                        th = th;
                        this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                        if (OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).skipAppTransitionAnimation()) {
                            this.mDisplayContent.mSkipAppTransitionAnimation = false;
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                    if (OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).skipAppTransitionAnimation()) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                if (OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).skipAppTransitionAnimation()) {
                }
                throw th;
            }
        }
    }

    private static WindowManager.LayoutParams getAnimLp(AppWindowToken wtoken) {
        WindowState mainWindow = wtoken != null ? wtoken.findMainWindow() : null;
        if (mainWindow != null) {
            return mainWindow.mAttrs;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationAdapter getRemoteAnimationOverride(AppWindowToken animLpToken, int transit, ArraySet<Integer> activityTypes) {
        RemoteAnimationAdapter adapter;
        RemoteAnimationDefinition definition = animLpToken.getRemoteAnimationDefinition();
        if (definition != null && (adapter = definition.getAdapter(transit, activityTypes)) != null) {
            return adapter;
        }
        RemoteAnimationAdapter adapter2 = this.mRemoteAnimationDefinition;
        if (adapter2 == null) {
            return null;
        }
        return adapter2.getAdapter(transit, activityTypes);
    }

    private void overrideWithRemoteAnimationIfSet(AppWindowToken animLpToken, int transit, ArraySet<Integer> activityTypes) {
        RemoteAnimationAdapter adapter;
        if (transit != 26 && animLpToken != null && (adapter = getRemoteAnimationOverride(animLpToken, transit, activityTypes)) != null) {
            animLpToken.getDisplayContent().mAppTransition.overridePendingAppTransitionRemote(adapter);
        }
    }

    private AppWindowToken findAnimLayoutParamsToken(int transit, ArraySet<Integer> activityTypes) {
        ArraySet<AppWindowToken> closingApps = this.mDisplayContent.mClosingApps;
        ArraySet<AppWindowToken> openingApps = this.mDisplayContent.mOpeningApps;
        ArraySet<AppWindowToken> changingApps = this.mDisplayContent.mChangingApps;
        AppWindowToken result = lookForHighestTokenWithFilter(closingApps, openingApps, changingApps, new Predicate(transit, activityTypes) {
            /* class com.android.server.wm.$$Lambda$AppTransitionController$YfQg1m68hbvcHoXbvzomyslzuaU */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ ArraySet f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppTransitionController.lambda$findAnimLayoutParamsToken$0(this.f$0, this.f$1, (AppWindowToken) obj);
            }
        });
        if (result != null) {
            return result;
        }
        AppWindowToken result2 = lookForHighestTokenWithFilter(closingApps, openingApps, changingApps, $$Lambda$AppTransitionController$ESsBJ2royCDDfelW3z7cgYH5q2I.INSTANCE);
        if (result2 != null) {
            return result2;
        }
        return lookForHighestTokenWithFilter(closingApps, openingApps, changingApps, $$Lambda$AppTransitionController$j4jrKo6PKtYRjRfPVQMMiQB02jg.INSTANCE);
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$0(int transit, ArraySet activityTypes, AppWindowToken w) {
        return w.getRemoteAnimationDefinition() != null && w.getRemoteAnimationDefinition().hasTransition(transit, activityTypes);
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$1(AppWindowToken w) {
        return w.fillsParent() && w.findMainWindow() != null;
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$2(AppWindowToken w) {
        return w.findMainWindow() != null;
    }

    private static ArraySet<Integer> collectActivityTypes(ArraySet<AppWindowToken> array1, ArraySet<AppWindowToken> array2, ArraySet<AppWindowToken> array3) {
        ArraySet<Integer> result = new ArraySet<>();
        for (int i = array1.size() - 1; i >= 0; i--) {
            result.add(Integer.valueOf(array1.valueAt(i).getActivityType()));
        }
        for (int i2 = array2.size() - 1; i2 >= 0; i2--) {
            result.add(Integer.valueOf(array2.valueAt(i2).getActivityType()));
        }
        for (int i3 = array3.size() - 1; i3 >= 0; i3--) {
            result.add(Integer.valueOf(array3.valueAt(i3).getActivityType()));
        }
        return result;
    }

    private static AppWindowToken lookForHighestTokenWithFilter(ArraySet<AppWindowToken> array1, ArraySet<AppWindowToken> array2, ArraySet<AppWindowToken> array3, Predicate<AppWindowToken> filter) {
        AppWindowToken wtoken;
        int array2base = array1.size();
        int array3base = array2.size() + array2base;
        int count = array3.size() + array3base;
        int bestPrefixOrderIndex = Integer.MIN_VALUE;
        AppWindowToken bestToken = null;
        for (int i = 0; i < count; i++) {
            if (i < array2base) {
                wtoken = array1.valueAt(i);
            } else if (i < array3base) {
                wtoken = array2.valueAt(i - array2base);
            } else {
                wtoken = array3.valueAt(i - array3base);
            }
            int prefixOrderIndex = wtoken.getPrefixOrderIndex();
            if (filter.test(wtoken) && prefixOrderIndex > bestPrefixOrderIndex) {
                bestPrefixOrderIndex = prefixOrderIndex;
                bestToken = wtoken;
            }
        }
        return bestToken;
    }

    private boolean containsVoiceInteraction(ArraySet<AppWindowToken> apps) {
        for (int i = apps.size() - 1; i >= 0; i--) {
            if (apps.valueAt(i).mVoiceInteraction) {
                return true;
            }
        }
        return false;
    }

    private boolean isOpenAppsContainHome() {
        ArraySet<AppWindowToken> openingApps = this.mDisplayContent.mOpeningApps;
        int appsCount = openingApps.size();
        for (int i = 0; i < appsCount; i++) {
            if (openingApps.valueAt(i).isActivityTypeHome()) {
                return true;
            }
        }
        return false;
    }

    private void handleOpeningApps(int transit, WindowManager.LayoutParams animLp, boolean voiceInteraction) {
        int transit2 = transit;
        if (transit2 == 8 && isOpenAppsContainHome()) {
            if (OppoWMSDynamicLogConfig.DEBUG_WMS || WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.d("WindowManager", "changing home activity transit to :" + 13);
            }
            transit2 = 13;
        }
        ArraySet<AppWindowToken> openingApps = this.mDisplayContent.mOpeningApps;
        int appsCount = openingApps.size();
        int i = 0;
        while (i < appsCount) {
            AppWindowToken wtoken = openingApps.valueAt(i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Now opening app" + wtoken);
            }
            if (!wtoken.commitVisibility(animLp, true, transit2, false, voiceInteraction)) {
                this.mDisplayContent.mNoAnimationNotifyOnTransitionFinished.add(wtoken.token);
            }
            wtoken.updateReportedVisibilityLocked();
            wtoken.waitingToShow = false;
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION handleAppTransitionReady()");
            }
            this.mService.openSurfaceTransaction();
            try {
                wtoken.showAllWindowsLocked();
                if (this.mDisplayContent.mAppTransition.isNextAppTransitionThumbnailUp()) {
                    wtoken.attachThumbnailAnimation();
                } else if (this.mDisplayContent.mAppTransition.isNextAppTransitionOpenCrossProfileApps()) {
                    wtoken.attachCrossProfileAppsThumbnailAnimation();
                }
                i++;
            } finally {
                this.mService.closeSurfaceTransaction("handleAppTransitionReady");
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION handleAppTransitionReady()");
                }
            }
        }
    }

    private void handleClosingApps(int transit, WindowManager.LayoutParams animLp, boolean voiceInteraction) {
        ArraySet<AppWindowToken> closingApps = this.mDisplayContent.mClosingApps;
        int appsCount = closingApps.size();
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = closingApps.valueAt(i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Now closing app " + wtoken);
            }
            wtoken.commitVisibility(animLp, false, (!wtoken.isActivityTypeHome() || !this.mDisplayContent.mSkipAppTransitionAnimation || AppTransition.isKeyguardGoingAwayTransit(transit)) ? transit : OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).getAppTransit(transit), false, voiceInteraction);
            wtoken.updateReportedVisibilityLocked();
            wtoken.allDrawn = true;
            wtoken.deferClearAllDrawn = false;
            if (wtoken.startingWindow != null && !wtoken.startingWindow.mAnimatingExit) {
                wtoken.removeStartingWindow();
            }
            if (this.mDisplayContent.mAppTransition.isNextAppTransitionThumbnailDown()) {
                wtoken.attachThumbnailAnimation();
            }
            if (wtoken.isActivityTypeHome() && this.mDisplayContent.mSkipAppTransitionAnimation && !AppTransition.isKeyguardGoingAwayTransit(transit)) {
                transit = OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).resetTransit(transit);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void handleChangingApps(int transit, WindowManager.LayoutParams animLp, boolean voiceInteraction) {
        ArraySet<AppWindowToken> apps = this.mDisplayContent.mChangingApps;
        int appsCount = apps.size();
        int i = 0;
        while (i < appsCount) {
            AppWindowToken wtoken = apps.valueAt(i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Now changing app" + wtoken);
            }
            wtoken.cancelAnimationOnly();
            wtoken.applyAnimationLocked(null, transit, true, false);
            wtoken.updateReportedVisibilityLocked();
            this.mService.openSurfaceTransaction();
            try {
                wtoken.showAllWindowsLocked();
                this.mService.closeSurfaceTransaction("handleChangingApps");
                i++;
            } catch (Throwable th) {
                this.mService.closeSurfaceTransaction("handleChangingApps");
                throw th;
            }
        }
    }

    private void handleNonAppWindowsInTransition(int transit, int flags) {
        boolean z = false;
        if (transit == 20 && (flags & 4) != 0 && (flags & 2) == 0) {
            Animation anim = this.mService.mPolicy.createKeyguardWallpaperExit((flags & 1) != 0);
            if (anim != null) {
                this.mDisplayContent.mWallpaperController.startWallpaperAnimation(anim);
            }
        }
        if (transit == 20 || transit == 21) {
            DisplayContent displayContent = this.mDisplayContent;
            boolean z2 = transit == 21;
            if ((flags & 1) != 0) {
                z = true;
            }
            displayContent.startKeyguardExitOnNonAppWindows(z2, z);
        }
    }

    private boolean transitionGoodToGo(ArraySet<AppWindowToken> apps, SparseIntArray outReasons) {
        int i;
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v("WindowManager", "Checking " + apps.size() + " opening apps (frozen=" + this.mService.mDisplayFrozen + " timeout=" + this.mDisplayContent.mAppTransition.isTimeout() + ")...");
        }
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
        if (this.mDisplayContent.mAppTransition.isTimeout()) {
            return true;
        }
        if (screenRotationAnimation == null || !screenRotationAnimation.isAnimating() || !this.mService.getDefaultDisplayContentLocked().rotationNeedsUpdate()) {
            for (int i2 = 0; i2 < apps.size(); i2++) {
                AppWindowToken wtoken = apps.valueAt(i2);
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v("WindowManager", "Check opening app=" + wtoken + ": allDrawn=" + wtoken.allDrawn + " startingDisplayed=" + wtoken.startingDisplayed + " startingMoved=" + wtoken.startingMoved + " isRelaunching()=" + wtoken.isRelaunching() + " startingWindow=" + wtoken.startingWindow);
                }
                boolean allDrawn = wtoken.allDrawn && !wtoken.isRelaunching();
                if (!allDrawn && !wtoken.startingDisplayed && !wtoken.startingMoved) {
                    return false;
                }
                int windowingMode = wtoken.getWindowingMode();
                if (allDrawn) {
                    outReasons.put(windowingMode, 2);
                } else {
                    if (wtoken.mStartingData instanceof SplashScreenStartingData) {
                        i = 1;
                    } else {
                        i = 4;
                    }
                    outReasons.put(windowingMode, i);
                }
            }
            if (this.mDisplayContent.mAppTransition.isFetchingAppTransitionsSpecs()) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v("WindowManager", "isFetchingAppTransitionSpecs=true");
                }
                return false;
            } else if (!this.mDisplayContent.mUnknownAppVisibilityController.allResolved()) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v("WindowManager", "unknownApps is not empty: " + this.mDisplayContent.mUnknownAppVisibilityController.getDebugMessage());
                }
                return false;
            } else {
                if (!this.mWallpaperControllerLocked.isWallpaperVisible() || this.mWallpaperControllerLocked.wallpaperTransitionReady()) {
                    return true;
                }
                return false;
            }
        } else {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Delaying app transition for screen rotation animation to finish");
            }
            return false;
        }
    }

    private int maybeUpdateTransitToWallpaper(int transit, boolean openingAppHasWallpaper, boolean closingAppHasWallpaper) {
        WindowState oldWallpaper;
        if (transit == 0 || transit == 26 || transit == 19 || AppTransition.isChangeTransit(transit)) {
            return transit;
        }
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        boolean showWallpaper = (wallpaperTarget == null || (wallpaperTarget.mAttrs.flags & DumpState.DUMP_DEXOPT) == 0) ? false : true;
        if (this.mWallpaperControllerLocked.isWallpaperTargetAnimating() || !showWallpaper) {
            oldWallpaper = null;
        } else {
            oldWallpaper = wallpaperTarget;
        }
        ArraySet<AppWindowToken> openingApps = this.mDisplayContent.mOpeningApps;
        ArraySet<AppWindowToken> closingApps = this.mDisplayContent.mClosingApps;
        AppWindowToken topOpeningApp = getTopApp(this.mDisplayContent.mOpeningApps, false);
        AppWindowToken topClosingApp = getTopApp(this.mDisplayContent.mClosingApps, true);
        boolean openingCanBeWallpaperTarget = canBeWallpaperTarget(openingApps);
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v("WindowManager", "New wallpaper target=" + wallpaperTarget + ", oldWallpaper=" + oldWallpaper + ", openingApps=" + openingApps + ", closingApps=" + closingApps);
        }
        if (openingCanBeWallpaperTarget && transit == 20) {
            transit = 21;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "New transit: " + AppTransition.appTransitionToString(21));
            }
        } else if (!AppTransition.isKeyguardGoingAwayTransit(transit)) {
            if (closingAppHasWallpaper && openingAppHasWallpaper) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v("WindowManager", "Wallpaper animation!");
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
                    Slog.v("WindowManager", "New transit: " + AppTransition.appTransitionToString(transit));
                }
            } else if (oldWallpaper != null && !this.mDisplayContent.mOpeningApps.isEmpty() && !openingApps.contains(oldWallpaper.mAppToken) && closingApps.contains(oldWallpaper.mAppToken) && topClosingApp == oldWallpaper.mAppToken) {
                transit = 12;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v("WindowManager", "New transit away from wallpaper: " + AppTransition.appTransitionToString(12));
                }
            } else if (wallpaperTarget != null && wallpaperTarget.isVisibleLw() && openingApps.contains(wallpaperTarget.mAppToken) && topOpeningApp == wallpaperTarget.mAppToken && transit != 25) {
                transit = 13;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v("WindowManager", "New transit into wallpaper: " + AppTransition.appTransitionToString(13));
                }
            }
        }
        return transit;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int maybeUpdateTransitToTranslucentAnim(int transit) {
        if (AppTransition.isChangeTransit(transit)) {
            return transit;
        }
        boolean taskOrActivity = AppTransition.isTaskTransit(transit) || AppTransition.isActivityTransit(transit);
        boolean allOpeningVisible = true;
        boolean allTranslucentOpeningApps = !this.mDisplayContent.mOpeningApps.isEmpty();
        for (int i = this.mDisplayContent.mOpeningApps.size() - 1; i >= 0; i--) {
            AppWindowToken token = this.mDisplayContent.mOpeningApps.valueAt(i);
            if (!token.isVisible()) {
                allOpeningVisible = false;
                if (token.fillsParent()) {
                    allTranslucentOpeningApps = false;
                }
            }
        }
        boolean allTranslucentClosingApps = !this.mDisplayContent.mClosingApps.isEmpty();
        int i2 = this.mDisplayContent.mClosingApps.size() - 1;
        while (true) {
            if (i2 < 0) {
                break;
            } else if (this.mDisplayContent.mClosingApps.valueAt(i2).fillsParent()) {
                allTranslucentClosingApps = false;
                break;
            } else {
                i2--;
            }
        }
        if (taskOrActivity && allTranslucentClosingApps && allOpeningVisible) {
            return 25;
        }
        if (!taskOrActivity || !allTranslucentOpeningApps || !this.mDisplayContent.mClosingApps.isEmpty()) {
            return transit;
        }
        return 24;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isTransitWithinTask(int transit, Task task) {
        if (task == null || !this.mDisplayContent.mChangingApps.isEmpty()) {
            return false;
        }
        if (transit != 6 && transit != 7 && transit != 18) {
            return false;
        }
        Iterator<AppWindowToken> it = this.mDisplayContent.mOpeningApps.iterator();
        while (it.hasNext()) {
            if (it.next().getTask() != task) {
                return false;
            }
        }
        Iterator<AppWindowToken> it2 = this.mDisplayContent.mClosingApps.iterator();
        while (it2.hasNext()) {
            if (it2.next().getTask() != task) {
                return false;
            }
        }
        return true;
    }

    private boolean canBeWallpaperTarget(ArraySet<AppWindowToken> apps) {
        for (int i = apps.size() - 1; i >= 0; i--) {
            if (apps.valueAt(i).windowsCanBeWallpaperTarget()) {
                return true;
            }
        }
        return false;
    }

    private AppWindowToken getTopApp(ArraySet<AppWindowToken> apps, boolean ignoreHidden) {
        int prefixOrderIndex;
        int topPrefixOrderIndex = Integer.MIN_VALUE;
        AppWindowToken topApp = null;
        for (int i = apps.size() - 1; i >= 0; i--) {
            AppWindowToken app = apps.valueAt(i);
            if ((!ignoreHidden || !app.isHidden()) && (prefixOrderIndex = app.getPrefixOrderIndex()) > topPrefixOrderIndex) {
                topPrefixOrderIndex = prefixOrderIndex;
                topApp = app;
            }
        }
        return topApp;
    }

    private void processApplicationsAnimatingInPlace(int transit) {
        WindowState win;
        if (transit == 17 && (win = this.mDisplayContent.findFocusedWindow()) != null) {
            AppWindowToken wtoken = win.mAppToken;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Now animating app in place " + wtoken);
            }
            wtoken.cancelAnimation();
            wtoken.applyAnimationLocked(null, transit, false, false);
            wtoken.updateReportedVisibilityLocked();
            wtoken.showAllWindowsLocked();
        }
    }
}
