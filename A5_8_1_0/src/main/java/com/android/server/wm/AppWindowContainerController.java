package com.android.server.wm;

import android.app.ActivityManager.TaskSnapshot;
import android.app.ActivityThread;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Trace;
import android.util.Slog;
import android.view.IApplicationToken;
import android.view.WindowManagerPolicy.StartingSurface;
import com.android.internal.R;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.pm.CompatibilityHelper;

public class AppWindowContainerController extends WindowContainerController<AppWindowToken, AppWindowContainerListener> {
    private static final int STARTING_WINDOW_TYPE_NONE = 0;
    private static final int STARTING_WINDOW_TYPE_SNAPSHOT = 1;
    private static final int STARTING_WINDOW_TYPE_SPLASH_SCREEN = 2;
    private final Runnable mAddStartingWindow;
    private final Handler mHandler;
    private final Runnable mOnWindowsGone;
    private final Runnable mOnWindowsVisible;
    private final IApplicationToken mToken;

    private final class H extends Handler {
        public static final int NOTIFY_STARTING_WINDOW_DRAWN = 2;
        public static final int NOTIFY_WINDOWS_DRAWN = 1;

        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (AppWindowContainerController.this.mListener != null) {
                        ((AppWindowContainerListener) AppWindowContainerController.this.mListener).onWindowsDrawn(msg.getWhen());
                        break;
                    }
                    return;
                case 2:
                    if (AppWindowContainerController.this.mListener != null) {
                        ((AppWindowContainerListener) AppWindowContainerController.this.mListener).onStartingWindowDrawn(msg.getWhen());
                        break;
                    }
                    return;
            }
        }
    }

    /* renamed from: lambda$-com_android_server_wm_AppWindowContainerController_4335 */
    /* synthetic */ void m48lambda$-com_android_server_wm_AppWindowContainerController_4335() {
        if (this.mListener != null) {
            ((AppWindowContainerListener) this.mListener).onWindowsVisible();
        }
    }

    /* renamed from: lambda$-com_android_server_wm_AppWindowContainerController_4626 */
    /* synthetic */ void m49lambda$-com_android_server_wm_AppWindowContainerController_4626() {
        if (this.mListener != null) {
            ((AppWindowContainerListener) this.mListener).onWindowsGone();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0117  */
    /* JADX WARNING: Missing block: B:13:0x0027, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:14:0x002a, code:
            if (r3 != null) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:16:0x002e, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_STARTING_WINDOW == false) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:17:0x0030, code:
            android.util.Slog.v("WindowManager", "startingData was nulled out before handling mAddStartingWindow: " + r9.mContainer);
     */
    /* JADX WARNING: Missing block: B:18:0x004c, code:
            return;
     */
    /* JADX WARNING: Missing block: B:22:0x0055, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_STARTING_WINDOW == false) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:23:0x0057, code:
            android.util.Slog.v("WindowManager", "Add starting " + r9 + ": startingData=" + r1.startingData);
     */
    /* JADX WARNING: Missing block: B:24:0x007e, code:
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            r4 = r3.createStartingSurface(r1);
     */
    /* JADX WARNING: Missing block: B:48:0x011b, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:49:0x011c, code:
            android.util.Slog.w("WindowManager", "Exception when adding starting window", r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    /* renamed from: lambda$-com_android_server_wm_AppWindowContainerController_4915 */
    /* synthetic */ void m50lambda$-com_android_server_wm_AppWindowContainerController_4915() {
        AppWindowToken container;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    StartingData startingData = ((AppWindowToken) this.mContainer).startingData;
                    container = this.mContainer;
                } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "mContainer was null while trying to add starting window");
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        boolean abort;
        if (surface != null) {
            abort = false;
            synchronized (this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (container.removed || container.startingData == null) {
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Aborted starting " + container + ": removed=" + container.removed + " startingData=" + container.startingData);
                        }
                        container.startingWindow = null;
                        container.startingData = null;
                        abort = true;
                    } else {
                        container.startingSurface = surface;
                    }
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && (abort ^ 1) != 0) {
                        Slog.v("WindowManager", "Added starting " + this.mContainer + ": startingWindow=" + container.startingWindow + " startingView=" + container.startingSurface);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (abort) {
                surface.remove();
            }
        } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Surface returned was null: " + this.mContainer);
        }
        if (abort) {
        }
    }

    public AppWindowContainerController(TaskWindowContainerController taskController, IApplicationToken token, AppWindowContainerListener listener, int index, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, boolean alwaysFocusable, int targetSdkVersion, int rotationAnimationHint, long inputDispatchingTimeoutNanos, Configuration overrideConfig, Rect bounds) {
        this(taskController, token, listener, index, requestedOrientation, fullscreen, showForAllUsers, configChanges, voiceInteraction, launchTaskBehind, alwaysFocusable, targetSdkVersion, rotationAnimationHint, inputDispatchingTimeoutNanos, WindowManagerService.getInstance(), overrideConfig, bounds);
    }

    public AppWindowContainerController(TaskWindowContainerController taskController, IApplicationToken token, AppWindowContainerListener listener, int index, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, boolean alwaysFocusable, int targetSdkVersion, int rotationAnimationHint, long inputDispatchingTimeoutNanos, WindowManagerService service, Configuration overrideConfig, Rect bounds) {
        super(listener, service);
        this.mOnWindowsVisible = new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 2, this);
        this.mOnWindowsGone = new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 3, this);
        this.mAddStartingWindow = new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 4, this);
        this.mHandler = new H(service.mH.getLooper());
        this.mToken = token;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mRoot.getAppWindowToken(this.mToken.asBinder()) != null) {
                    Slog.w("WindowManager", "Attempted to add existing app token: " + this.mToken);
                } else {
                    Task task = (Task) taskController.mContainer;
                    if (task == null) {
                        throw new IllegalArgumentException("AppWindowContainerController: invalid  controller=" + taskController);
                    }
                    AppWindowToken atoken = createAppWindow(this.mService, token, voiceInteraction, task.getDisplayContent(), inputDispatchingTimeoutNanos, fullscreen, showForAllUsers, targetSdkVersion, requestedOrientation, rotationAnimationHint, configChanges, launchTaskBehind, alwaysFocusable, this, overrideConfig, bounds);
                    if (WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v("WindowManager", "addAppToken: " + atoken + " controller=" + taskController + " at " + index);
                    }
                    task.addChild(atoken, index);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    AppWindowToken createAppWindow(WindowManagerService service, IApplicationToken token, boolean voiceInteraction, DisplayContent dc, long inputDispatchingTimeoutNanos, boolean fullscreen, boolean showForAllUsers, int targetSdk, int orientation, int rotationAnimationHint, int configChanges, boolean launchTaskBehind, boolean alwaysFocusable, AppWindowContainerController controller, Configuration overrideConfig, Rect bounds) {
        return new AppWindowToken(service, token, voiceInteraction, dc, inputDispatchingTimeoutNanos, fullscreen, showForAllUsers, targetSdk, orientation, rotationAnimationHint, configChanges, launchTaskBehind, alwaysFocusable, controller, overrideConfig, bounds);
    }

    public void removeContainer(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                if (dc == null) {
                    Slog.w("WindowManager", "removeAppToken: Attempted to remove binder token: " + this.mToken + " from non-existing displayId=" + displayId);
                } else {
                    dc.removeAppToken(this.mToken.asBinder());
                    super.removeContainer();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void removeContainer() {
        throw new UnsupportedOperationException("Use removeContainer(displayId) instead.");
    }

    public void reparent(TaskWindowContainerController taskController, int position) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.i("WindowManager", "reparent: moving app token=" + this.mToken + " to task=" + taskController + " at " + position);
                }
                if (this.mContainer != null) {
                    Task task = taskController.mContainer;
                    if (task == null) {
                        throw new IllegalArgumentException("reparent: could not find task=" + taskController);
                    }
                    ((AppWindowToken) this.mContainer).reparent(task, position);
                    ((AppWindowToken) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.i("WindowManager", "reparent: could not find app token=" + this.mToken);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public Configuration setOrientation(int requestedOrientation, int displayId, Configuration displayConfig, boolean freezeScreenIfNeeded) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to set orientation of non-existing app token: " + this.mToken);
                } else {
                    ((AppWindowToken) this.mContainer).setOrientation(requestedOrientation);
                    Configuration updateOrientationFromAppTokens = this.mService.updateOrientationFromAppTokens(displayConfig, freezeScreenIfNeeded ? this.mToken.asBinder() : null, displayId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return updateOrientationFromAppTokens;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return null;
    }

    public int getOrientation() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    int orientationIgnoreVisibility = ((AppWindowToken) this.mContainer).getOrientationIgnoreVisibility();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return orientationIgnoreVisibility;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return -1;
    }

    public void onOverrideConfigurationChanged(Configuration overrideConfiguration, Rect bounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((AppWindowToken) this.mContainer).onOverrideConfigurationChanged(overrideConfiguration, bounds);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setDisablePreviewScreenshots(boolean disable) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to set disable screenshots of non-existing app token: " + this.mToken);
                } else {
                    ((AppWindowToken) this.mContainer).setDisablePreviewScreenshots(disable);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0042, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:19:0x0045, code:
            return;
     */
    /* JADX WARNING: Missing block: B:64:0x019c, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:65:0x019f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setVisibility(boolean visible, boolean deferHidingClient) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to set visibility of non-existing app token: " + this.mToken);
                } else {
                    AppWindowToken wtoken = this.mContainer;
                    if (visible || !wtoken.hiddenRequested) {
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ORIENTATION || WindowManagerDebugConfig.DEBUG_WMS) {
                            Slog.v("WindowManager", "setAppVisibility(" + this.mToken + ", visible=" + visible + "): " + this.mService.mAppTransition + " hidden=" + wtoken.hidden + " hiddenRequested=" + wtoken.hiddenRequested + " Callers=" + Debug.getCallers(6));
                        }
                        this.mService.mOpeningApps.remove(wtoken);
                        this.mService.mClosingApps.remove(wtoken);
                        wtoken.waitingToShow = false;
                        wtoken.hiddenRequested = visible ^ 1;
                        wtoken.mDeferHidingClient = deferHidingClient;
                        if (visible) {
                            if (!this.mService.mAppTransition.isTransitionSet() && this.mService.mAppTransition.isReady()) {
                                this.mService.mOpeningApps.add(wtoken);
                            }
                            wtoken.startingMoved = false;
                            if (wtoken.hidden || wtoken.mAppStopped) {
                                wtoken.clearAllDrawn();
                                if (wtoken.hidden) {
                                    wtoken.waitingToShow = true;
                                }
                                if (wtoken.isClientHidden()) {
                                    wtoken.setClientHidden(false);
                                }
                            }
                            wtoken.requestUpdateWallpaperIfNeeded();
                            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.v("WindowManager", "No longer Stopped: " + wtoken);
                            }
                            wtoken.mAppStopped = false;
                        } else {
                            wtoken.removeDeadWindows();
                            wtoken.setVisibleBeforeClientHidden();
                        }
                        if (wtoken.okToAnimate() && this.mService.mAppTransition.isTransitionSet()) {
                            if (wtoken.mAppAnimator.usingTransferredAnimation && wtoken.mAppAnimator.animation == null) {
                                Slog.wtf("WindowManager", "Will NOT set dummy animation on: " + wtoken + ", using null transferred animation!");
                            }
                            if (!wtoken.mAppAnimator.usingTransferredAnimation && (!wtoken.startingDisplayed || this.mService.mSkipAppTransitionAnimation)) {
                                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                    Slog.v("WindowManager", "Setting dummy animation on: " + wtoken);
                                }
                                wtoken.mAppAnimator.setDummyAnimation();
                            }
                            wtoken.inPendingTransaction = true;
                            if (visible) {
                                this.mService.mOpeningApps.add(wtoken);
                                wtoken.mEnteringAnimation = true;
                            } else {
                                this.mService.mClosingApps.add(wtoken);
                                wtoken.mEnteringAnimation = false;
                            }
                            if (this.mService.mAppTransition.getAppTransition() == 16) {
                                WindowState win = this.mService.getDefaultDisplayContentLocked().findFocusedWindow();
                                if (win != null) {
                                    AppWindowToken focusedToken = win.mAppToken;
                                    if (focusedToken != null) {
                                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                            Slog.d("WindowManager", "TRANSIT_TASK_OPEN_BEHIND,  adding " + focusedToken + " to mOpeningApps");
                                        }
                                        focusedToken.hidden = true;
                                        this.mService.mOpeningApps.add(focusedToken);
                                    }
                                }
                            }
                        } else {
                            wtoken.setVisibility(null, visible, -1, true, wtoken.mVoiceInteraction);
                            wtoken.updateReportedVisibilityLocked();
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    } else if (!deferHidingClient && wtoken.mDeferHidingClient) {
                        wtoken.mDeferHidingClient = deferHidingClient;
                        wtoken.setClientHidden(true);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void oppoSnapShot() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    this.mService.mTaskSnapshotController.oppoSnapshotTask((AppWindowToken) this.mContainer);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyUnknownVisibilityLaunched() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    this.mService.mUnknownAppVisibilityController.notifyLaunched((AppWindowToken) this.mContainer);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean addStartingWindow(String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "setAppStartingWindow: token=" + this.mToken + " pkg=" + pkg + " transferFrom=" + transferFrom + " newTask=" + newTask + " taskSwitch=" + taskSwitch + " processRunning=" + processRunning + " allowTaskSnapshot=" + allowTaskSnapshot);
                }
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to set icon of non-existing app token: " + this.mToken);
                } else if (!((AppWindowToken) this.mContainer).okToDisplay()) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } else if (((AppWindowToken) this.mContainer).startingData != null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } else {
                    WindowState mainWin = ((AppWindowToken) this.mContainer).findMainWindow();
                    if (mainWin == null || !mainWin.mWinAnimator.getShown()) {
                        boolean isInSaveSurfaceList;
                        TaskSnapshot snapshot = this.mService.mTaskSnapshotController.getSnapshot(((AppWindowToken) this.mContainer).getTask().mTaskId, ((AppWindowToken) this.mContainer).getTask().mUserId, false, false);
                        if (pkg != null) {
                            isInSaveSurfaceList = ActivityThread.inCptWhiteList(CompatibilityHelper.FORCE_ENABLE_SAVE_SURFACE, pkg);
                        } else {
                            isInSaveSurfaceList = false;
                        }
                        Slog.d("WindowManager", "<chenqy> isInSaveSurfaceList: " + isInSaveSurfaceList + " pkg: " + pkg);
                        int type = getStartingWindowType(newTask, taskSwitch, processRunning, allowTaskSnapshot, activityCreated, fromRecents, isInSaveSurfaceList, snapshot);
                        if (type == 1) {
                            boolean createSnapshot = createSnapshot(snapshot);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return createSnapshot;
                        }
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Checking theme of starting window: 0x" + Integer.toHexString(theme));
                        }
                        if (theme != 0) {
                            Entry ent = AttributeCache.instance().get(pkg, theme, R.styleable.Window, this.mService.mCurrentUserId);
                            if (ent == null) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return false;
                            }
                            boolean windowIsTranslucent = ent.array.getBoolean(5, false);
                            boolean windowIsFloating = ent.array.getBoolean(4, false);
                            boolean windowShowWallpaper = ent.array.getBoolean(14, false);
                            boolean windowDisableStarting = ent.array.getBoolean(12, false);
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v("WindowManager", "Translucent=" + windowIsTranslucent + " Floating=" + windowIsFloating + " ShowWallpaper=" + windowShowWallpaper);
                            }
                            if (windowIsTranslucent) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return false;
                            } else if (windowIsFloating || windowDisableStarting) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return false;
                            } else if (windowShowWallpaper) {
                                if (((AppWindowToken) this.mContainer).getDisplayContent().mWallpaperController.getWallpaperTarget() == null) {
                                    windowFlags |= DumpState.DUMP_DEXOPT;
                                } else {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    return false;
                                }
                            }
                        }
                        if (((AppWindowToken) this.mContainer).transferStartingWindow(transferFrom)) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return true;
                        } else if (type != 2) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        } else {
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v("WindowManager", "Creating SplashScreenStartingData");
                            }
                            ((AppWindowToken) this.mContainer).startingData = new SplashScreenStartingData(this.mService, pkg, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags, ((AppWindowToken) this.mContainer).getMergedOverrideConfiguration());
                            scheduleAddStartingWindow();
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return true;
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return false;
    }

    private int getStartingWindowType(boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents, boolean isInSaveSurfaceList, TaskSnapshot snapshot) {
        int i = 0;
        if (this.mService.mAppTransition.getAppTransition() == 19) {
            return 0;
        }
        if (newTask || (processRunning ^ 1) != 0 || (taskSwitch && (activityCreated ^ 1) != 0)) {
            return 2;
        }
        WindowManagerService windowManagerService = this.mService;
        if ((!WindowManagerService.mEnableSaveSurface && (isInSaveSurfaceList ^ 1) != 0) || !taskSwitch || !allowTaskSnapshot) {
            return 0;
        }
        if (snapshot != null) {
            i = (snapshotOrientationSameAsTask(snapshot) || fromRecents) ? 1 : 2;
        }
        return i;
    }

    void scheduleAddStartingWindow() {
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Enqueueing ADD_STARTING");
        }
        this.mService.mAnimationHandler.postAtFrontOfQueue(this.mAddStartingWindow);
    }

    private boolean createSnapshot(TaskSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Creating SnapshotStartingData");
        }
        ((AppWindowToken) this.mContainer).startingData = new SnapshotStartingData(this.mService, snapshot);
        scheduleAddStartingWindow();
        return true;
    }

    private boolean snapshotOrientationSameAsTask(TaskSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }
        return ((AppWindowToken) this.mContainer).getTask().getConfiguration().orientation == snapshot.getOrientation();
    }

    /* JADX WARNING: Missing block: B:22:0x007c, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:23:0x007f, code:
            return;
     */
    /* JADX WARNING: Missing block: B:29:0x00a1, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:30:0x00a4, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeStartingWindow() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (((AppWindowToken) this.mContainer).startingWindow == null) {
                    if (((AppWindowToken) this.mContainer).startingData != null) {
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Clearing startingData for token=" + this.mContainer);
                        }
                        ((AppWindowToken) this.mContainer).startingData = null;
                    }
                } else if (((AppWindowToken) this.mContainer).startingData != null) {
                    StartingSurface surface = ((AppWindowToken) this.mContainer).startingSurface;
                    ((AppWindowToken) this.mContainer).startingData = null;
                    ((AppWindowToken) this.mContainer).startingSurface = null;
                    ((AppWindowToken) this.mContainer).startingWindow = null;
                    ((AppWindowToken) this.mContainer).startingDisplayed = false;
                    if (surface != null) {
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Schedule remove starting " + this.mContainer + " startingWindow=" + ((AppWindowToken) this.mContainer).startingWindow + " startingView=" + ((AppWindowToken) this.mContainer).startingSurface);
                        }
                        this.mService.mAnimationHandler.post(new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 5, surface));
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v("WindowManager", "startingWindow was set but startingSurface==null, couldn't remove");
                    }
                } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Tried to remove starting window but startingWindow was null:" + this.mContainer);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* renamed from: lambda$-com_android_server_wm_AppWindowContainerController_33638 */
    static /* synthetic */ void m47lambda$-com_android_server_wm_AppWindowContainerController_33638(StartingSurface surface) {
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Removing startingView=" + surface);
        }
        try {
            surface.remove();
        } catch (Exception e) {
            Slog.w("WindowManager", "Exception when removing starting window", e);
        }
    }

    public void pauseKeyDispatching() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    this.mService.mInputMonitor.pauseDispatchingLw((WindowToken) this.mContainer);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void resumeKeyDispatching() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    this.mService.mInputMonitor.resumeDispatchingLw((WindowToken) this.mContainer);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyWindowFreezing(boolean freezing, int stackId) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mService.mHasWindowFreezing = freezing;
                this.mService.mFreezingStackId = stackId;
                this.mService.oppoStartFreezingDisplayLocked();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyAppResumed(boolean wasStopped) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to notify resumed of non-existing app token: " + this.mToken);
                } else {
                    ((AppWindowToken) this.mContainer).notifyAppResumed(wasStopped);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyAppStopped() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to notify stopped of non-existing app token: " + this.mToken);
                } else {
                    ((AppWindowToken) this.mContainer).notifyAppStopped();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0058, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:17:0x005b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startFreezingScreen(int configChanges) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to freeze screen with non-existing app token: " + this.mContainer);
                } else {
                    if (configChanges == 0) {
                        if (((AppWindowToken) this.mContainer).okToDisplay()) {
                            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.v("WindowManager", "Skipping set freeze of " + this.mToken);
                            }
                        }
                    }
                    ((AppWindowToken) this.mContainer).startFreezingScreen();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void stopFreezingScreen(boolean force) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                } else {
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Clear freezing of " + this.mToken + ": hidden=" + ((AppWindowToken) this.mContainer).hidden + " freezing=" + ((AppWindowToken) this.mContainer).mAppAnimator.freezingScreen);
                    }
                    ((AppWindowToken) this.mContainer).stopFreezingScreen(true, force);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public Bitmap screenshotApplications(int displayId, int width, int height, float frameScale) {
        try {
            Trace.traceBegin(32, "screenshotApplications");
            synchronized (this.mWindowMap) {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayContent dc = this.mRoot.getDisplayContentOrCreate(displayId);
                if (dc == null) {
                    if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                        Slog.i("WindowManager", "Screenshot of " + this.mToken + ": returning null. No Display for displayId=" + displayId);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Trace.traceEnd(32);
                    return null;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                Bitmap screenshotApplications = dc.screenshotApplications(this.mToken.asBinder(), width, height, false, frameScale, Config.RGB_565, false, false);
                Trace.traceEnd(32);
                return screenshotApplications;
            }
        } catch (Throwable th) {
            Trace.traceEnd(32);
        }
    }

    void reportStartingWindowDrawn() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
    }

    void reportWindowsDrawn() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
    }

    void reportWindowsVisible() {
        this.mHandler.post(this.mOnWindowsVisible);
    }

    void reportWindowsGone() {
        this.mHandler.post(this.mOnWindowsGone);
    }

    boolean keyDispatchingTimedOut(String reason, int windowPid) {
        return this.mListener != null ? ((AppWindowContainerListener) this.mListener).keyDispatchingTimedOut(reason, windowPid) : false;
    }

    public String toString() {
        return "AppWindowContainerController{ token=" + this.mToken + " mContainer=" + this.mContainer + " mListener=" + this.mListener + "}";
    }
}
