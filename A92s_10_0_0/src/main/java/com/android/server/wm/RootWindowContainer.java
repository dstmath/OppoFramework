package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Trace;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.EventLogTags;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

class RootWindowContainer extends WindowContainer<DisplayContent> implements ConfigurationContainerListener {
    private static final int SET_SCREEN_BRIGHTNESS_OVERRIDE = 1;
    private static final int SET_USER_ACTIVITY_TIMEOUT = 2;
    private static final String TAG = "WindowManager";
    private static final Consumer<WindowState> sRemoveReplacedWindowsConsumer = $$Lambda$RootWindowContainer$Vvv8jzH2oSE9eakZwTuKd5NpsU.INSTANCE;
    private final Consumer<WindowState> mCloseSystemDialogsConsumer = new Consumer() {
        /* class com.android.server.wm.$$Lambda$RootWindowContainer$qT2ficAmvrvFcBdiJIGNKxJ8Z9Q */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            RootWindowContainer.this.lambda$new$0$RootWindowContainer((WindowState) obj);
        }
    };
    private String mCloseSystemDialogsReason;
    private final SurfaceControl.Transaction mDisplayTransaction = new SurfaceControl.Transaction();
    private final Handler mHandler;
    private Session mHoldScreen = null;
    WindowState mHoldScreenWindow = null;
    private Object mLastWindowFreezeSource = null;
    private boolean mObscureApplicationContentOnSecondaryDisplays = false;
    WindowState mObscuringWindow = null;
    boolean mOrientationChangeComplete = true;
    private RootActivityContainer mRootActivityContainer;
    private float mScreenBrightness = -1.0f;
    private boolean mSustainedPerformanceModeCurrent = false;
    private boolean mSustainedPerformanceModeEnabled = false;
    final HashMap<Integer, AppWindowToken> mTopFocusedAppByProcess = new HashMap<>();
    private int mTopFocusedDisplayId = -1;
    private boolean mUpdateRotation = false;
    private long mUserActivityTimeout = -1;
    boolean mWallpaperActionPending = false;

    public /* synthetic */ void lambda$new$0$RootWindowContainer(WindowState w) {
        if (w.mHasSurface) {
            try {
                w.mClient.closeSystemDialogs(this.mCloseSystemDialogsReason);
            } catch (RemoteException e) {
            }
        }
    }

    static /* synthetic */ void lambda$static$1(WindowState w) {
        AppWindowToken aToken = w.mAppToken;
        if (aToken != null) {
            aToken.removeReplacedWindowIfNeeded(w);
        }
    }

    RootWindowContainer(WindowManagerService service) {
        super(service);
        this.mHandler = new MyHandler(service.mH.getLooper());
    }

    /* access modifiers changed from: package-private */
    public void setRootActivityContainer(RootActivityContainer container) {
        this.mRootActivityContainer = container;
        if (container != null) {
            container.registerConfigurationChangeListener(this);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
        this.mTopFocusedAppByProcess.clear();
        boolean changed = false;
        int topFocusedDisplayId = -1;
        int i = this.mChildren.size();
        while (true) {
            i--;
            if (i < 0) {
                break;
            }
            DisplayContent dc = (DisplayContent) this.mChildren.get(i);
            changed |= dc.updateFocusedWindowLocked(mode, updateInputWindows, topFocusedDisplayId);
            WindowState newFocus = dc.mCurrentFocus;
            if (newFocus != null) {
                int pidOfNewFocus = newFocus.mSession.mPid;
                if (this.mTopFocusedAppByProcess.get(Integer.valueOf(pidOfNewFocus)) == null) {
                    this.mTopFocusedAppByProcess.put(Integer.valueOf(pidOfNewFocus), newFocus.mAppToken);
                }
                if (topFocusedDisplayId == -1) {
                    topFocusedDisplayId = dc.getDisplayId();
                }
            } else if (topFocusedDisplayId == -1 && dc.mFocusedApp != null) {
                topFocusedDisplayId = dc.getDisplayId();
            }
        }
        if (topFocusedDisplayId == -1) {
            topFocusedDisplayId = 0;
        }
        if (this.mTopFocusedDisplayId != topFocusedDisplayId) {
            this.mTopFocusedDisplayId = topFocusedDisplayId;
            this.mWmService.mInputManager.setFocusedDisplay(topFocusedDisplayId);
            this.mWmService.mPolicy.setTopFocusedDisplay(topFocusedDisplayId);
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v("WindowManager", "New topFocusedDisplayId=" + topFocusedDisplayId);
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getTopFocusedDisplayContent() {
        DisplayContent dc = getDisplayContent(this.mTopFocusedDisplayId);
        return dc != null ? dc : getDisplayContent(0);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onChildPositionChanged() {
        this.mWmService.updateFocusedWindowLocked(0, !this.mWmService.mPerDisplayFocusEnabled);
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getDisplayContent(int displayId) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent current = (DisplayContent) this.mChildren.get(i);
            if (current.getDisplayId() == displayId) {
                return current;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent createDisplayContent(Display display, ActivityDisplay activityDisplay) {
        int displayId = display.getDisplayId();
        DisplayContent existing = getDisplayContent(displayId);
        if (existing != null) {
            existing.mAcitvityDisplay = activityDisplay;
            existing.initializeDisplayOverrideConfiguration();
            return existing;
        }
        DisplayContent dc = new DisplayContent(display, this.mWmService, activityDisplay);
        if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
            Slog.v("WindowManager", "Adding display=" + display);
        }
        this.mWmService.mDisplayWindowSettings.applySettingsToDisplayLocked(dc);
        dc.initializeDisplayOverrideConfiguration();
        if (this.mWmService.mDisplayManagerInternal != null) {
            this.mWmService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, dc.getDisplayInfo());
            dc.configureDisplayPolicy();
        }
        this.mWmService.reconfigureDisplayLocked(dc);
        return dc;
    }

    /* access modifiers changed from: package-private */
    public void onSettingsRetrieved() {
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            DisplayContent displayContent = (DisplayContent) this.mChildren.get(displayNdx);
            if (this.mWmService.mDisplayWindowSettings.updateSettingsForDisplay(displayContent)) {
                displayContent.initializeDisplayOverrideConfiguration();
                this.mWmService.reconfigureDisplayLocked(displayContent);
                if (displayContent.isDefaultDisplay) {
                    this.mWmService.mAtmService.updateConfigurationLocked(this.mWmService.computeNewConfiguration(displayContent.getDisplayId()), null, false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutNeeded() {
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            if (((DisplayContent) this.mChildren.get(displayNdx)).isLayoutNeeded()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void getWindowsByName(ArrayList<WindowState> output, String name) {
        int objectId = 0;
        try {
            objectId = Integer.parseInt(name, 16);
            name = null;
        } catch (RuntimeException e) {
        }
        getWindowsByName(output, name, objectId);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [com.android.server.wm.-$$Lambda$RootWindowContainer$O6gArs92KbWUhitra1og4WTg69c, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    private void getWindowsByName(ArrayList<WindowState> output, String name, int objectId) {
        forAllWindows((Consumer<WindowState>) new Consumer(name, output, objectId) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$O6gArs92KbWUhitra1og4WTg69c */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ ArrayList f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RootWindowContainer.lambda$getWindowsByName$2(this.f$0, this.f$1, this.f$2, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$getWindowsByName$2(String name, ArrayList output, int objectId, WindowState w) {
        if (name != null) {
            if (w.mAttrs.getTitle().toString().contains(name)) {
                output.add(w);
            }
        } else if (System.identityHashCode(w) == objectId) {
            output.add(w);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
     arg types: [com.android.server.wm.-$$Lambda$RootWindowContainer$IlD1lD49ui7gQmU2NkxgnXIhlOo, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean */
    /* access modifiers changed from: package-private */
    public boolean isAnyNonToastWindowVisibleForUid(int callingUid) {
        return forAllWindows((ToBooleanFunction<WindowState>) new ToBooleanFunction(callingUid) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$IlD1lD49ui7gQmU2NkxgnXIhlOo */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean apply(Object obj) {
                return RootWindowContainer.lambda$isAnyNonToastWindowVisibleForUid$3(this.f$0, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ boolean lambda$isAnyNonToastWindowVisibleForUid$3(int callingUid, WindowState w) {
        return w.getOwningUid() == callingUid && w.mAttrs.type != 2005 && w.mAttrs.type != 3 && w.isVisibleNow();
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getAppWindowToken(IBinder binder) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken atoken = ((DisplayContent) this.mChildren.get(i)).getAppWindowToken(binder);
            if (atoken != null) {
                return atoken;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowToken getWindowToken(IBinder binder) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowToken wtoken = ((DisplayContent) this.mChildren.get(i)).getWindowToken(binder);
            if (wtoken != null) {
                return wtoken;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getWindowTokenDisplay(WindowToken token) {
        if (token == null) {
            return null;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent dc = (DisplayContent) this.mChildren.get(i);
            if (dc.getWindowToken(token.token) == token) {
                return dc;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setDisplayOverrideConfigurationIfNeeded(Configuration newConfiguration, DisplayContent displayContent) {
        if (displayContent.getRequestedOverrideConfiguration().diff(newConfiguration) != 0) {
            displayContent.onRequestedOverrideConfigurationChanged(newConfiguration);
            if (displayContent.getDisplayId() == 0) {
                setGlobalConfigurationIfNeeded(newConfiguration);
            }
        }
    }

    private void setGlobalConfigurationIfNeeded(Configuration newConfiguration) {
        if (getConfiguration().diff(newConfiguration) != 0) {
            onConfigurationChanged(newConfiguration);
        }
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        prepareFreezingTaskBounds();
        super.onConfigurationChanged(newParentConfig);
        OppoFeatureCache.get(IColorWatermarkManager.DEFAULT).onConfigurationChanged(newParentConfig);
    }

    private void prepareFreezingTaskBounds() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((DisplayContent) this.mChildren.get(i)).prepareFreezingTaskBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack(int windowingMode, int activityType) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            TaskStack stack = ((DisplayContent) this.mChildren.get(i)).getStack(windowingMode, activityType);
            if (stack != null) {
                return stack;
            }
        }
        return null;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [com.android.server.wm.-$$Lambda$RootWindowContainer$vMW2dyMvZQ0PDhptvNKN5WXpK_w, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    /* access modifiers changed from: package-private */
    public void setSecureSurfaceState(int userId, boolean disabled) {
        forAllWindows((Consumer<WindowState>) new Consumer(userId, disabled) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$vMW2dyMvZQ0PDhptvNKN5WXpK_w */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RootWindowContainer.lambda$setSecureSurfaceState$4(this.f$0, this.f$1, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$setSecureSurfaceState$4(int userId, boolean disabled, WindowState w) {
        if (w.mHasSurface && userId == UserHandle.getUserId(w.mOwnerUid)) {
            w.mWinAnimator.setSecureLocked(disabled);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [com.android.server.wm.-$$Lambda$RootWindowContainer$jHLZ5ssJOPMd9KJ4tf6FHZ8ZLXI, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    /* access modifiers changed from: package-private */
    public void updateHiddenWhileSuspendedState(ArraySet<String> packages, boolean suspended) {
        forAllWindows((Consumer<WindowState>) new Consumer(packages, suspended) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$jHLZ5ssJOPMd9KJ4tf6FHZ8ZLXI */
            private final /* synthetic */ ArraySet f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RootWindowContainer.lambda$updateHiddenWhileSuspendedState$5(this.f$0, this.f$1, (WindowState) obj);
            }
        }, false);
    }

    static /* synthetic */ void lambda$updateHiddenWhileSuspendedState$5(ArraySet packages, boolean suspended, WindowState w) {
        if (packages.contains(w.getOwningPackage())) {
            w.setHiddenWhileSuspended(suspended);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateAppOpsState() {
        this.mWmService.updateAppOpsStateForFloatWindow();
    }

    static /* synthetic */ boolean lambda$canShowStrictModeViolation$6(int pid, WindowState w) {
        return w.mSession.mPid == pid && w.isVisibleLw();
    }

    /* access modifiers changed from: package-private */
    public boolean canShowStrictModeViolation(int pid) {
        return getWindow(new Predicate(pid) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpor3so3blHoI */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return RootWindowContainer.lambda$canShowStrictModeViolation$6(this.f$0, (WindowState) obj);
            }
        }) != null;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [java.util.function.Consumer<com.android.server.wm.WindowState>, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    /* access modifiers changed from: package-private */
    public void closeSystemDialogs(String reason) {
        this.mCloseSystemDialogsReason = reason;
        forAllWindows(this.mCloseSystemDialogsConsumer, false);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [java.util.function.Consumer<com.android.server.wm.WindowState>, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    /* access modifiers changed from: package-private */
    public void removeReplacedWindows() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            Slog.i("WindowManager", ">>> OPEN TRANSACTION removeReplacedWindows");
        }
        this.mWmService.openSurfaceTransaction();
        try {
            forAllWindows(sRemoveReplacedWindowsConsumer, true);
        } finally {
            this.mWmService.closeSurfaceTransaction("removeReplacedWindows");
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                Slog.i("WindowManager", "<<< CLOSE TRANSACTION removeReplacedWindows");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPendingLayoutChanges(WindowAnimator animator) {
        boolean hasChanges = false;
        int count = this.mChildren.size();
        for (int i = 0; i < count; i++) {
            int pendingChanges = animator.getPendingLayoutChanges(((DisplayContent) this.mChildren.get(i)).getDisplayId());
            if ((pendingChanges & 4) != 0) {
                animator.mBulkUpdateParams |= 8;
            }
            if (pendingChanges != 0) {
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [com.android.server.wm.-$$Lambda$RootWindowContainer$utugHDPHgMp2b3JwigOH_-Y0P1Q, int]
     candidates:
      com.android.server.wm.DisplayContent.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowManagerService.logSurface(com.android.server.wm.WindowState, java.lang.String, boolean):void
     arg types: [com.android.server.wm.WindowState, java.lang.String, int]
     candidates:
      com.android.server.wm.WindowManagerService.logSurface(android.view.SurfaceControl, java.lang.String, java.lang.String):void
      com.android.server.wm.WindowManagerService.logSurface(com.android.server.wm.WindowState, java.lang.String, boolean):void */
    /* access modifiers changed from: package-private */
    public boolean reclaimSomeSurfaceMemory(WindowStateAnimator winAnimator, String operation, boolean secure) {
        WindowSurfaceController surfaceController = winAnimator.mSurfaceController;
        boolean leakedSurface = false;
        boolean displayNdx = false;
        EventLog.writeEvent((int) EventLogTags.WM_NO_SURFACE_MEMORY, winAnimator.mWin.toString(), Integer.valueOf(winAnimator.mSession.mPid), operation);
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            Slog.i("WindowManager", "Out of memory for surface!  Looking for leaks...");
            int numDisplays = this.mChildren.size();
            for (int displayNdx2 = 0; displayNdx2 < numDisplays; displayNdx2++) {
                leakedSurface |= ((DisplayContent) this.mChildren.get(displayNdx2)).destroyLeakedSurfaces();
            }
            if (!leakedSurface) {
                Slog.w("WindowManager", "No leaked surfaces; killing applications!");
                SparseIntArray pidCandidates = new SparseIntArray();
                boolean killedApps = false;
                int displayNdx3 = 0;
                while (displayNdx3 < numDisplays) {
                    try {
                        ((DisplayContent) this.mChildren.get(displayNdx3)).forAllWindows((Consumer<WindowState>) new Consumer(pidCandidates) {
                            /* class com.android.server.wm.$$Lambda$RootWindowContainer$utugHDPHgMp2b3JwigOH_Y0P1Q */
                            private final /* synthetic */ SparseIntArray f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                RootWindowContainer.this.lambda$reclaimSomeSurfaceMemory$7$RootWindowContainer(this.f$1, (WindowState) obj);
                            }
                        }, false);
                        if (pidCandidates.size() > 0) {
                            int[] pids = new int[pidCandidates.size()];
                            for (int i = 0; i < pids.length; i++) {
                                pids[i] = pidCandidates.keyAt(i);
                            }
                            try {
                                try {
                                    if (this.mWmService.mActivityManager.killPids(pids, "Free memory", secure)) {
                                        killedApps = true;
                                    }
                                } catch (RemoteException e) {
                                } catch (Throwable th) {
                                    th = th;
                                    Binder.restoreCallingIdentity(callingIdentity);
                                    throw th;
                                }
                            } catch (RemoteException e2) {
                            }
                        }
                        displayNdx3++;
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(callingIdentity);
                        throw th;
                    }
                }
                displayNdx = killedApps;
            }
            if (leakedSurface || displayNdx) {
                try {
                    Slog.w("WindowManager", "Looks like we have reclaimed some memory, clearing surface for retry.");
                    if (surfaceController != null) {
                        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
                            WindowManagerService.logSurface(winAnimator.mWin, "RECOVER DESTROY", false);
                        }
                        winAnimator.destroySurface();
                        if (winAnimator.mWin.mAppToken != null) {
                            winAnimator.mWin.mAppToken.removeStartingWindow();
                        }
                    }
                    try {
                        winAnimator.mWin.mClient.dispatchGetNewSurface();
                    } catch (RemoteException e3) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(callingIdentity);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(callingIdentity);
            if (leakedSurface || displayNdx) {
                return true;
            }
            return false;
        } catch (Throwable th4) {
            th = th4;
            Binder.restoreCallingIdentity(callingIdentity);
            throw th;
        }
    }

    public /* synthetic */ void lambda$reclaimSomeSurfaceMemory$7$RootWindowContainer(SparseIntArray pidCandidates, WindowState w) {
        if (!this.mWmService.mForceRemoves.contains(w)) {
            WindowStateAnimator wsa = w.mWinAnimator;
            if (wsa.mSurfaceController != null) {
                pidCandidates.append(wsa.mSession.mPid, wsa.mSession.mPid);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void performSurfacePlacement(boolean recoveringMemory) {
        Trace.traceBegin(32, "performSurfacePlacement");
        try {
            performSurfacePlacementNoTrace(recoveringMemory);
        } finally {
            Trace.traceEnd(32);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0235, code lost:
        android.util.Slog.d("WindowManager", "Performing post-rotate rotation");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x023a, code lost:
        r14.mUpdateRotation = updateRotationUnchecked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0244, code lost:
        if (r14.mWmService.mWaitingForDrawnCallback != null) goto L_0x0254;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0248, code lost:
        if (r14.mOrientationChangeComplete == false) goto L_0x0259;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x024e, code lost:
        if (isLayoutNeeded() != false) goto L_0x0259;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0252, code lost:
        if (r14.mUpdateRotation != false) goto L_0x0259;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0254, code lost:
        r14.mWmService.checkDrawnWindowsLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0259, code lost:
        r3 = r14.mWmService.mPendingRemove.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0261, code lost:
        if (r3 <= 0) goto L_0x02ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0268, code lost:
        if (r14.mWmService.mPendingRemoveTmp.length >= r3) goto L_0x0272;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x026a, code lost:
        r14.mWmService.mPendingRemoveTmp = new com.android.server.wm.WindowState[(r3 + 10)];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x0272, code lost:
        r14.mWmService.mPendingRemove.toArray(r14.mWmService.mPendingRemoveTmp);
        r14.mWmService.mPendingRemove.clear();
        r5 = new java.util.ArrayList<>();
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x028a, code lost:
        if (r1 >= r3) goto L_0x02a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x028c, code lost:
        r9 = r14.mWmService.mPendingRemoveTmp[r1];
        r9.removeImmediately();
        r10 = r9.getDisplayContent();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0299, code lost:
        if (r10 == null) goto L_0x02a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x029f, code lost:
        if (r5.contains(r10) != false) goto L_0x02a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02a1, code lost:
        r5.add(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x02a4, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x02a7, code lost:
        r9 = r5.size() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x02ac, code lost:
        if (r9 < 0) goto L_0x02ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x02ae, code lost:
        r5.get(r9).assignWindowLayers(true);
        r9 = r9 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x02ba, code lost:
        r5 = r14.mChildren.size() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x02c1, code lost:
        if (r5 < 0) goto L_0x02d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x02c3, code lost:
        ((com.android.server.wm.DisplayContent) r14.mChildren.get(r5)).checkCompleteDeferredRemoval();
        r5 = r5 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x02d1, code lost:
        forAllDisplays(com.android.server.wm.$$Lambda$RootWindowContainer$XbbIpkF4p2mF3v0qeXeat_w3E.INSTANCE);
        r14.mWmService.enableScreenIfNeededLocked();
        r14.mWmService.scheduleAnimationLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x02e2, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_WINDOW_TRACE == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x02e4, code lost:
        android.util.Slog.e("WindowManager", "performSurfacePlacementInner exit: animating=" + r14.mWmService.mAnimator.isAnimating());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x008d, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS != false) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x008f, code lost:
        android.util.Slog.i("WindowManager", "<<< CLOSE TRANSACTION performLayoutAndPlaceSurfaces");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00a7, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS == false) goto L_0x00aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00aa, code lost:
        r14.mWmService.mAnimator.executeAfterPrepareSurfacesRunnables();
        checkAppTransitionReady(r7);
        r0 = r14.mWmService.getRecentsAnimationController();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00ba, code lost:
        if (r0 == null) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00bc, code lost:
        r0.checkAnimationReady(r6.mWallpaperController);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00c1, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00c2, code lost:
        if (r1 >= r2) goto L_0x00ed;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00c4, code lost:
        r9 = (com.android.server.wm.DisplayContent) r14.mChildren.get(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ce, code lost:
        if (r9.mWallpaperMayChange == false) goto L_0x00ea;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00d2, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT == false) goto L_0x00d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00d4, code lost:
        android.util.Slog.v("WindowManager", "Wallpaper may change!  Adjusting");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d9, code lost:
        r9.pendingLayoutChanges |= 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00e1, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS == false) goto L_0x00ea;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00e3, code lost:
        r7.debugLayoutRepeats("WallpaperMayChange", r9.pendingLayoutChanges);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ea, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00f2, code lost:
        if (r14.mWmService.mFocusMayChange == false) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00f4, code lost:
        r14.mWmService.mFocusMayChange = false;
        r14.mWmService.updateFocusedWindowLocked(2, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0101, code lost:
        if (isLayoutNeeded() == false) goto L_0x0113;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0103, code lost:
        r6.pendingLayoutChanges |= 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x010a, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS == false) goto L_0x0113;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x010c, code lost:
        r7.debugLayoutRepeats("mLayoutNeeded", r6.pendingLayoutChanges);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0113, code lost:
        handleResizingWindows();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0118, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_ORIENTATION == false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x011e, code lost:
        if (r14.mWmService.mDisplayFrozen == false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0120, code lost:
        android.util.Slog.v("WindowManager", "With display frozen, orientationChangeComplete=" + r14.mOrientationChangeComplete);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0138, code lost:
        if (r14.mOrientationChangeComplete == false) goto L_0x015e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x013e, code lost:
        if (r14.mWmService.mSimulateWindowFreezing != false) goto L_0x015e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0144, code lost:
        if (r14.mWmService.mWindowsFreezingScreen == 0) goto L_0x0159;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0146, code lost:
        r14.mWmService.mWindowsFreezingScreen = 0;
        r14.mWmService.mLastFinishedFreezeSource = r14.mLastWindowFreezeSource;
        r14.mWmService.mH.removeMessages(11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0159, code lost:
        r14.mWmService.stopFreezingDisplayLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0162, code lost:
        if (r14.mWmService.mHasWindowFreezed == false) goto L_0x0179;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0164, code lost:
        r14.mWmService.mHasWindowFreezed = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x016e, code lost:
        if (r14.mWmService.getColorFreeformManager() == null) goto L_0x0179;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0170, code lost:
        r14.mWmService.getColorFreeformManager().oppoStopFreezingDisplayLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0179, code lost:
        r1 = r14.mWmService.mDestroySurface.size();
        r10 = -1;
        r10 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0182, code lost:
        if (r1 <= 0) goto L_0x01bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0184, code lost:
        r1 = r1 - 1;
        r11 = r14.mWmService.mDestroySurface.get(r1);
        r11.mDestroying = false;
        r12 = r11.getDisplayContent();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0197, code lost:
        if (r12.mInputMethodWindow != r11) goto L_0x019c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0199, code lost:
        r12.setInputMethodWindowLocked(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01a2, code lost:
        if (r12.mWallpaperController.isWallpaperTarget(r11) == false) goto L_0x01aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01a4, code lost:
        r12.pendingLayoutChanges |= 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01aa, code lost:
        r11.destroySurfaceUnchecked();
        r11.mWinAnimator.destroyPreservedSurfaceLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01b2, code lost:
        if (r1 > 0) goto L_0x0184;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x01b4, code lost:
        r14.mWmService.mDestroySurface.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x01bb, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01bc, code lost:
        if (r3 >= r2) goto L_0x01cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01be, code lost:
        ((com.android.server.wm.DisplayContent) r14.mChildren.get(r3)).removeExistingTokensIfPossible();
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01cc, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01cd, code lost:
        if (r3 >= r2) goto L_0x01e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01cf, code lost:
        r11 = (com.android.server.wm.DisplayContent) r14.mChildren.get(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01d9, code lost:
        if (r11.pendingLayoutChanges == 0) goto L_0x01de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01db, code lost:
        r11.setLayoutNeeded();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01de, code lost:
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01e1, code lost:
        r14.mWmService.setHoldScreenLocked(r14.mHoldScreen);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01ec, code lost:
        if (r14.mWmService.mDisplayFrozen != false) goto L_0x021b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01ee, code lost:
        r3 = r14.mScreenBrightness;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01f3, code lost:
        if (r3 < com.android.server.display.OppoBrightUtils.MIN_LUX_LIMITI) goto L_0x0202;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01f9, code lost:
        if (r3 <= 1.0f) goto L_0x01fc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01fc, code lost:
        r10 = toBrightnessOverride(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0202, code lost:
        r14.mHandler.obtainMessage(1, r10, 0).sendToTarget();
        r14.mHandler.obtainMessage(2, java.lang.Long.valueOf(r14.mUserActivityTimeout)).sendToTarget();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x021b, code lost:
        r3 = r14.mSustainedPerformanceModeCurrent;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x021f, code lost:
        if (r3 == r14.mSustainedPerformanceModeEnabled) goto L_0x022d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0221, code lost:
        r14.mSustainedPerformanceModeEnabled = r3;
        r14.mWmService.mPowerManagerInternal.powerHint(6, r14.mSustainedPerformanceModeEnabled ? 1 : 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x022f, code lost:
        if (r14.mUpdateRotation == false) goto L_0x0240;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0233, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_ORIENTATION == false) goto L_0x023a;
     */
    public void performSurfacePlacementNoTrace(boolean recoveringMemory) {
        if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
            Slog.v("WindowManager", "performSurfacePlacementInner: entry. Called by " + Debug.getCallers(3));
        }
        if (this.mWmService.mFocusMayChange) {
            this.mWmService.mFocusMayChange = false;
            this.mWmService.updateFocusedWindowLocked(3, false);
        }
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ((DisplayContent) this.mChildren.get(displayNdx)).setExitingTokensHasVisible(false);
        }
        this.mHoldScreen = null;
        this.mScreenBrightness = -1.0f;
        this.mUserActivityTimeout = -1;
        this.mObscureApplicationContentOnSecondaryDisplays = false;
        this.mSustainedPerformanceModeCurrent = false;
        this.mWmService.mTransactionSequence++;
        DisplayContent defaultDisplay = this.mWmService.getDefaultDisplayContentLocked();
        WindowSurfacePlacer surfacePlacer = this.mWmService.mWindowPlacerLocked;
        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
            Slog.i("WindowManager", ">>> OPEN TRANSACTION performLayoutAndPlaceSurfaces");
        }
        Trace.traceBegin(32, "applySurfaceChanges");
        this.mWmService.openSurfaceTransaction();
        try {
            applySurfaceChangesTransaction(recoveringMemory);
            this.mWmService.closeSurfaceTransaction("performLayoutAndPlaceSurfaces");
            Trace.traceEnd(32);
        } catch (RuntimeException e) {
            Slog.wtf("WindowManager", "Unhandled exception in Window Manager", e);
            this.mWmService.closeSurfaceTransaction("performLayoutAndPlaceSurfaces");
            Trace.traceEnd(32);
        } catch (Throwable th) {
            this.mWmService.closeSurfaceTransaction("performLayoutAndPlaceSurfaces");
            Trace.traceEnd(32);
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", "<<< CLOSE TRANSACTION performLayoutAndPlaceSurfaces");
            }
            throw th;
        }
    }

    static /* synthetic */ void lambda$performSurfacePlacementNoTrace$8(DisplayContent dc) {
        dc.getInputMonitor().updateInputWindowsLw(true);
        dc.updateSystemGestureExclusion();
        dc.updateTouchExcludeRegion();
    }

    private void checkAppTransitionReady(WindowSurfacePlacer surfacePlacer) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent curDisplay = (DisplayContent) this.mChildren.get(i);
            if (curDisplay.mAppTransition.isReady()) {
                curDisplay.mAppTransitionController.handleAppTransitionReady();
                if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                    surfacePlacer.debugLayoutRepeats("after handleAppTransitionReady", curDisplay.pendingLayoutChanges);
                }
            }
            if (curDisplay.mAppTransition.isRunning() && !curDisplay.isAppAnimating()) {
                curDisplay.handleAnimatingStoppedAndTransition();
                if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                    surfacePlacer.debugLayoutRepeats("after handleAnimStopAndXitionLock", curDisplay.pendingLayoutChanges);
                }
            }
        }
    }

    private void applySurfaceChangesTransaction(boolean recoveringMemory) {
        this.mHoldScreenWindow = null;
        this.mObscuringWindow = null;
        DisplayInfo defaultInfo = this.mWmService.getDefaultDisplayContentLocked().getDisplayInfo();
        int defaultDw = defaultInfo.logicalWidth;
        int defaultDh = defaultInfo.logicalHeight;
        if (this.mWmService.mWatermark != null) {
            this.mWmService.mWatermark.positionSurface(defaultDw, defaultDh);
        }
        OppoFeatureCache.get(IColorWatermarkManager.DEFAULT).positionSurface(defaultDw, defaultDh);
        if (this.mWmService.mStrictModeFlash != null) {
            this.mWmService.mStrictModeFlash.positionSurface(defaultDw, defaultDh);
        }
        if (this.mWmService.mCircularDisplayMask != null) {
            this.mWmService.mCircularDisplayMask.positionSurface(defaultDw, defaultDh, this.mWmService.getDefaultDisplayRotation());
        }
        if (this.mWmService.mEmulatorDisplayOverlay != null) {
            this.mWmService.mEmulatorDisplayOverlay.positionSurface(defaultDw, defaultDh, this.mWmService.getDefaultDisplayRotation());
        }
        int count = this.mChildren.size();
        for (int j = 0; j < count; j++) {
            ((DisplayContent) this.mChildren.get(j)).applySurfaceChangesTransaction(recoveringMemory);
        }
        this.mWmService.mDisplayManagerInternal.performTraversal(this.mDisplayTransaction);
        SurfaceControl.mergeToGlobalTransaction(this.mDisplayTransaction);
    }

    private void handleResizingWindows() {
        for (int i = this.mWmService.mResizingWindows.size() - 1; i >= 0; i--) {
            WindowState win = this.mWmService.mResizingWindows.get(i);
            if (!win.mAppFreezing && !win.getDisplayContent().mWaitingForConfig) {
                win.reportResized();
                this.mWmService.mResizingWindows.remove(i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean handleNotObscuredLocked(WindowState w, boolean obscured, boolean syswin) {
        WindowManager.LayoutParams attrs = w.mAttrs;
        int attrFlags = attrs.flags;
        boolean onScreen = w.isOnScreen();
        boolean canBeSeen = w.isDisplayedLw();
        int privateflags = attrs.privateFlags;
        boolean displayHasContent = false;
        if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON) {
            Slog.d("DebugKeepScreenOn", "handleNotObscuredLocked w: " + w + ", w.mHasSurface: " + w.mHasSurface + ", w.isOnScreen(): " + onScreen + ", w.isDisplayedLw(): " + w.isDisplayedLw() + ", w.mAttrs.userActivityTimeout: " + w.mAttrs.userActivityTimeout);
        }
        if (w.mHasSurface && onScreen && !syswin && w.mAttrs.userActivityTimeout >= 0 && this.mUserActivityTimeout < 0) {
            this.mUserActivityTimeout = w.mAttrs.userActivityTimeout;
            if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON) {
                Slog.d("WindowManager", "mUserActivityTimeout set to " + this.mUserActivityTimeout);
            }
        }
        if (w.mHasSurface && canBeSeen) {
            if ((attrFlags & 128) != 0) {
                this.mHoldScreen = w.mSession;
                this.mHoldScreenWindow = w;
            } else if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON && w == this.mWmService.mLastWakeLockHoldingWindow) {
                Slog.d("DebugKeepScreenOn", "handleNotObscuredLocked: " + w + " was holding screen wakelock but no longer has FLAG_KEEP_SCREEN_ON!!! called by" + Debug.getCallers(10));
            }
            if (!syswin && w.mAttrs.screenBrightness >= OppoBrightUtils.MIN_LUX_LIMITI && this.mScreenBrightness < OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mScreenBrightness = w.mAttrs.screenBrightness;
            }
            int type = attrs.type;
            DisplayContent displayContent = w.getDisplayContent();
            if (displayContent != null && displayContent.isDefaultDisplay) {
                if (type == 2023 || (attrs.privateFlags & 1024) != 0) {
                    this.mObscureApplicationContentOnSecondaryDisplays = true;
                }
                displayHasContent = true;
            } else if (displayContent != null && (!this.mObscureApplicationContentOnSecondaryDisplays || (obscured && type == 2009))) {
                displayHasContent = true;
            }
            if ((262144 & privateflags) != 0) {
                this.mSustainedPerformanceModeCurrent = true;
            }
        }
        return displayHasContent;
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationUnchecked() {
        boolean changed = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((DisplayContent) this.mChildren.get(i)).updateRotationAndSendNewConfigIfNeeded()) {
                changed = true;
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public boolean copyAnimToLayoutParams() {
        boolean doRequest = false;
        int bulkUpdateParams = this.mWmService.mAnimator.mBulkUpdateParams;
        if ((bulkUpdateParams & 1) != 0) {
            this.mUpdateRotation = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 4) == 0) {
            this.mOrientationChangeComplete = false;
        } else {
            this.mOrientationChangeComplete = true;
            this.mLastWindowFreezeSource = this.mWmService.mAnimator.mLastWindowFreezeSource;
            if (this.mWmService.mWindowsFreezingScreen != 0) {
                doRequest = true;
            }
        }
        if ((bulkUpdateParams & 8) != 0) {
            this.mWallpaperActionPending = true;
        }
        return doRequest;
    }

    private static int toBrightnessOverride(float value) {
        return (int) (((float) PowerManager.BRIGHTNESS_MULTIBITS_ON) * value);
    }

    private final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                RootWindowContainer.this.mWmService.mPowerManagerInternal.setScreenBrightnessOverrideFromWindowManager(msg.arg1);
            } else if (i == 2) {
                RootWindowContainer.this.mWmService.mPowerManagerInternal.setUserActivityTimeoutOverrideFromWindowManager(((Long) msg.obj).longValue());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpDisplayContents(PrintWriter pw) {
        pw.println("WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)");
        if (this.mWmService.mDisplayReady) {
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                ((DisplayContent) this.mChildren.get(i)).dump(pw, "  ", true);
            }
            return;
        }
        pw.println("  NO DISPLAY");
    }

    /* access modifiers changed from: package-private */
    public void dumpTopFocusedDisplayId(PrintWriter pw) {
        pw.print("  mTopFocusedDisplayId=");
        pw.println(this.mTopFocusedDisplayId);
    }

    /* access modifiers changed from: package-private */
    public void dumpLayoutNeededDisplayIds(PrintWriter pw) {
        if (isLayoutNeeded()) {
            pw.print("  mLayoutNeeded on displays=");
            int count = this.mChildren.size();
            for (int displayNdx = 0; displayNdx < count; displayNdx++) {
                DisplayContent displayContent = (DisplayContent) this.mChildren.get(displayNdx);
                if (displayContent.isLayoutNeeded()) {
                    pw.print(displayContent.getDisplayId());
                }
            }
            pw.println();
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [com.android.server.wm.-$$Lambda$RootWindowContainer$y9wG_endhUBCwGznyjN4RSIYTyg, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    /* access modifiers changed from: package-private */
    public void dumpWindowsNoHeader(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        forAllWindows((Consumer<WindowState>) new Consumer(windows, pw, new int[1], dumpAll) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$y9wG_endhUBCwGznyjN4RSIYTyg */
            private final /* synthetic */ ArrayList f$0;
            private final /* synthetic */ PrintWriter f$1;
            private final /* synthetic */ int[] f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RootWindowContainer.lambda$dumpWindowsNoHeader$9(this.f$0, this.f$1, this.f$2, this.f$3, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$dumpWindowsNoHeader$9(ArrayList windows, PrintWriter pw, int[] index, boolean dumpAll, WindowState w) {
        if (windows == null || windows.contains(w)) {
            pw.println("  Window #" + index[0] + StringUtils.SPACE + w + ":");
            w.dump(pw, "    ", dumpAll || windows != null);
            index[0] = index[0] + 1;
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpTokens(PrintWriter pw, boolean dumpAll) {
        pw.println("  All tokens:");
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((DisplayContent) this.mChildren.get(i)).dumpTokens(pw, dumpAll);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [com.android.server.wm.-$$Lambda$RootWindowContainer$vY6OsmBRhacjqomBMjSm_N8AJAs, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            if (this.mWmService.mDisplayReady) {
                int count = this.mChildren.size();
                for (int i = 0; i < count; i++) {
                    ((DisplayContent) this.mChildren.get(i)).writeToProto(proto, 2246267895810L, logLevel);
                }
            }
            if (logLevel == 0) {
                forAllWindows((Consumer<WindowState>) new Consumer(proto) {
                    /* class com.android.server.wm.$$Lambda$RootWindowContainer$vY6OsmBRhacjqomBMjSm_N8AJAs */
                    private final /* synthetic */ ProtoOutputStream f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ((WindowState) obj).writeIdentifierToProto(this.f$0, 2246267895811L);
                    }
                }, true);
            }
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public String getName() {
        return "ROOT";
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(int, com.android.server.wm.WindowContainer, boolean):void
     arg types: [int, com.android.server.wm.DisplayContent, boolean]
     candidates:
      com.android.server.wm.RootWindowContainer.positionChildAt(int, com.android.server.wm.DisplayContent, boolean):void
      MutableMD:(int, com.android.server.wm.WindowContainer, boolean):void */
    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, DisplayContent child, boolean includingParents) {
        super.positionChildAt(position, (WindowContainer) child, includingParents);
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer != null) {
            rootActivityContainer.onChildPositionChanged(child.mAcitvityDisplay, position);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(int, com.android.server.wm.WindowContainer, boolean):void
     arg types: [int, com.android.server.wm.DisplayContent, int]
     candidates:
      com.android.server.wm.RootWindowContainer.positionChildAt(int, com.android.server.wm.DisplayContent, boolean):void
      MutableMD:(int, com.android.server.wm.WindowContainer, boolean):void */
    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, DisplayContent child) {
        super.positionChildAt(position, (WindowContainer) child, false);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void scheduleAnimation() {
        this.mWmService.scheduleAnimationLocked();
    }

    /* access modifiers changed from: protected */
    public void removeChild(DisplayContent dc) {
        super.removeChild((WindowContainer) dc);
        if (this.mTopFocusedDisplayId == dc.getDisplayId()) {
            this.mWmService.updateFocusedWindowLocked(0, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void forAllDisplays(Consumer<DisplayContent> callback) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            callback.accept((DisplayContent) this.mChildren.get(i));
        }
    }

    /* access modifiers changed from: package-private */
    public void forAllDisplayPolicies(Consumer<DisplayPolicy> callback) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            callback.accept(((DisplayContent) this.mChildren.get(i)).getDisplayPolicy());
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState getCurrentInputMethodWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent displayContent = (DisplayContent) this.mChildren.get(i);
            if (displayContent.mInputMethodWindow != null) {
                return displayContent.mInputMethodWindow;
            }
        }
        return null;
    }

    public WindowList<DisplayContent> getDisplayContents() {
        return this.mChildren;
    }
}
