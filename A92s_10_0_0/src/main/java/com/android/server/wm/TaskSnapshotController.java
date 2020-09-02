package com.android.server.wm;

import android.app.ActivityManager;
import android.common.ColorFrameworkFactory;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.os.Handler;
import android.util.ArraySet;
import android.util.Slog;
import android.view.SurfaceControl;
import android.view.ThreadedRenderer;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.DisplayThread;
import com.android.server.display.OppoBrightUtils;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.TaskSnapshotSurface;
import com.android.server.wm.utils.InsetUtils;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.function.Consumer;

class TaskSnapshotController {
    @VisibleForTesting
    static final int SNAPSHOT_MODE_APP_THEME = 1;
    @VisibleForTesting
    static final int SNAPSHOT_MODE_NONE = 2;
    @VisibleForTesting
    static final int SNAPSHOT_MODE_REAL = 0;
    private static final String TAG = "WindowManager";
    private final TaskSnapshotCache mCache;
    private final float mFullSnapshotScale;
    private final Handler mHandler = new Handler();
    private final boolean mIsRunningOnIoT;
    private final boolean mIsRunningOnTv;
    private final boolean mIsRunningOnWear;
    private final TaskSnapshotLoader mLoader;
    private final TaskSnapshotPersister mPersister;
    private final WindowManagerService mService;
    private final ArraySet<Task> mSkipClosingAppSnapshotTasks = new ArraySet<>();
    private final Rect mTmpRect = new Rect();
    private final ArraySet<Task> mTmpTasks = new ArraySet<>();

    TaskSnapshotController(WindowManagerService service) {
        this.mService = service;
        this.mPersister = new TaskSnapshotPersister(this.mService, $$Lambda$OPdXuZQLetMnocdH6XV32JbNQ3I.INSTANCE);
        this.mLoader = new TaskSnapshotLoader(this.mPersister);
        this.mCache = new TaskSnapshotCache(this.mService, this.mLoader);
        this.mIsRunningOnTv = this.mService.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        this.mIsRunningOnIoT = this.mService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.embedded");
        this.mIsRunningOnWear = this.mService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mFullSnapshotScale = this.mService.mContext.getResources().getFloat(17105058);
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mPersister.start();
    }

    /* access modifiers changed from: package-private */
    public void onTransitionStarting(DisplayContent displayContent) {
        handleClosingApps(displayContent.mClosingApps);
    }

    /* access modifiers changed from: package-private */
    public void notifyAppVisibilityChanged(AppWindowToken appWindowToken, boolean visible) {
        if (!visible) {
            handleClosingApps(Sets.newArraySet(new AppWindowToken[]{appWindowToken}));
        }
    }

    private void handleClosingApps(ArraySet<AppWindowToken> closingApps) {
        if (!shouldDisableSnapshots()) {
            getClosingTasks(closingApps, this.mTmpTasks);
            snapshotTasks(this.mTmpTasks);
            this.mSkipClosingAppSnapshotTasks.clear();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void addSkipClosingAppSnapshotTasks(ArraySet<Task> tasks) {
        this.mSkipClosingAppSnapshotTasks.addAll((ArraySet<? extends Task>) tasks);
    }

    /* access modifiers changed from: package-private */
    public void snapshotTasks(ArraySet<Task> tasks) {
        ActivityManager.TaskSnapshot snapshot;
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.valueAt(i);
            int mode = getSnapshotMode(task);
            if (mode != 0) {
                if (!(mode == 1 || mode == 2)) {
                    snapshot = null;
                }
            } else {
                snapshot = snapshotTask(task);
            }
            if (snapshot != null) {
                GraphicBuffer buffer = snapshot.getSnapshot();
                if (buffer.getWidth() == 0 || buffer.getHeight() == 0) {
                    buffer.destroy();
                    Slog.e("WindowManager", "Invalid task snapshot dimensions " + buffer.getWidth() + "x" + buffer.getHeight());
                } else {
                    boolean systemDarkMode = task.getConfiguration() != null && (task.getConfiguration().uiMode & 48) == 32;
                    ActivityRecord rootActivity = task.mTaskRecord.getRootActivity();
                    if (rootActivity != null) {
                        DisplayThread.getHandler().post(new Runnable(rootActivity.packageName, systemDarkMode, snapshot) {
                            /* class com.android.server.wm.$$Lambda$TaskSnapshotController$nX4Icvk7n_r8KkiAQoNDQLLIbSE */
                            private final /* synthetic */ String f$1;
                            private final /* synthetic */ boolean f$2;
                            private final /* synthetic */ ActivityManager.TaskSnapshot f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            public final void run() {
                                TaskSnapshotController.this.lambda$snapshotTasks$0$TaskSnapshotController(this.f$1, this.f$2, this.f$3);
                            }
                        });
                    } else {
                        snapshot.setInDarkMode(systemDarkMode);
                    }
                    this.mCache.putSnapshot(task, snapshot);
                    this.mPersister.persistSnapshot(task.mTaskId, task.mUserId, snapshot);
                    task.onSnapshotChanged(snapshot);
                }
            }
        }
    }

    public /* synthetic */ void lambda$snapshotTasks$0$TaskSnapshotController(String packageName, boolean systemDarkMode, ActivityManager.TaskSnapshot tempSnapshot) {
        boolean isInDarkMode = ColorFrameworkFactory.getInstance().getColorDarkModeManager().isDarkModePage(packageName, systemDarkMode);
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (tempSnapshot != null) {
                    tempSnapshot.setInDarkMode(isInDarkMode);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot getSnapshot(int taskId, int userId, boolean restoreFromDisk, boolean reducedResolution) {
        return this.mCache.getSnapshot(taskId, userId, restoreFromDisk, reducedResolution || TaskSnapshotPersister.DISABLE_FULL_SIZED_BITMAPS);
    }

    /* access modifiers changed from: package-private */
    public WindowManagerPolicy.StartingSurface createStartingSurface(AppWindowToken token, ActivityManager.TaskSnapshot snapshot) {
        return TaskSnapshotSurface.create(this.mService, token, snapshot);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.AppWindowToken.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
     arg types: [com.android.server.wm.-$$Lambda$TaskSnapshotController$XvttjThMNmtqsxNIojE6a1vsPus, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
      com.android.server.wm.AppWindowToken.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean */
    private AppWindowToken findAppTokenForSnapshot(Task task) {
        for (int i = task.getChildCount() - 1; i >= 0; i--) {
            AppWindowToken appWindowToken = (AppWindowToken) task.getChildAt(i);
            if (appWindowToken != null && appWindowToken.isSurfaceShowing() && appWindowToken.findMainWindow() != null && appWindowToken.forAllWindows((ToBooleanFunction<WindowState>) $$Lambda$TaskSnapshotController$XvttjThMNmtqsxNIojE6a1vsPus.INSTANCE, true)) {
                return appWindowToken;
            }
        }
        return null;
    }

    static /* synthetic */ boolean lambda$findAppTokenForSnapshot$1(WindowState ws) {
        return ws.mWinAnimator != null && ws.mWinAnimator.getShown() && ws.mWinAnimator.mLastAlpha > OppoBrightUtils.MIN_LUX_LIMITI;
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.ScreenshotGraphicBuffer createTaskSnapshot(Task task, float scaleFraction) {
        return createTaskSnapshot(task, scaleFraction, null);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.ScreenshotGraphicBuffer createTaskSnapshot(Task task, float scaleFraction, Rect overrideCrop) {
        GraphicBuffer buffer;
        if (task.getSurfaceControl() == null) {
            if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                Slog.w("WindowManager", "Failed to take screenshot. No surface control for " + task);
            }
            return null;
        }
        task.getBounds(this.mTmpRect);
        this.mTmpRect.offsetTo(0, 0);
        if (overrideCrop != null) {
            this.mTmpRect.set(overrideCrop);
        }
        SurfaceControl.ScreenshotGraphicBuffer screenshotBuffer = SurfaceControl.captureLayers(task.getSurfaceControl().getHandle(), this.mTmpRect, scaleFraction);
        if (screenshotBuffer != null) {
            buffer = screenshotBuffer.getGraphicBuffer();
        } else {
            buffer = null;
        }
        if (buffer == null || buffer.getWidth() <= 1 || buffer.getHeight() <= 1) {
            return null;
        }
        return screenshotBuffer;
    }

    private ActivityManager.TaskSnapshot snapshotTask(Task task) {
        return snapshotTask(task, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x0111  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x012a  */
    private ActivityManager.TaskSnapshot snapshotTask(Task task, boolean ignoreScreenState) {
        float scaleFraction;
        int snapshotOrientation;
        WindowState mainWindow;
        boolean z;
        Rect overrideCrop;
        SurfaceControl.ScreenshotGraphicBuffer screenshotBuffer;
        int surfaceOrientation;
        if (ignoreScreenState || this.mService.mPolicy.isScreenOn()) {
            AppWindowToken appWindowToken = findAppTokenForSnapshot(task);
            if (appWindowToken == null) {
                if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                    Slog.w("WindowManager", "Failed to take screenshot. No visible windows for " + task);
                }
                return null;
            } else if (appWindowToken.hasCommittedReparentToAnimationLeash()) {
                if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                    Slog.w("WindowManager", "Failed to take screenshot. App is animating " + appWindowToken);
                }
                return null;
            } else {
                boolean isLowRamDevice = ActivityManager.isLowRamDeviceStatic();
                if (isLowRamDevice) {
                    scaleFraction = this.mPersister.getReducedScale();
                } else {
                    scaleFraction = this.mFullSnapshotScale;
                }
                WindowState mainWindow2 = appWindowToken.findMainWindow();
                if (mainWindow2 == null) {
                    Slog.w("WindowManager", "Failed to take screenshot. No main window for " + task);
                    return null;
                }
                int snapshotOrientation2 = appWindowToken.getTask().getConfiguration().orientation;
                Rect overrideCrop2 = null;
                try {
                    WindowSurfaceController mainSurface = task.getTopFullscreenAppToken().findMainWindow().mWinAnimator.mSurfaceController;
                    int surfaceW = mainSurface.getWidth();
                    int surfaceH = mainSurface.getHeight();
                    mainWindow = mainWindow2;
                    if (((double) surfaceW) > ((double) surfaceH) * 1.5d) {
                        surfaceOrientation = 2;
                    } else {
                        surfaceOrientation = 1;
                    }
                    if (snapshotOrientation2 != surfaceOrientation) {
                        try {
                            if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                                Slog.w("WindowManager", "rotation not match for " + task + " surface:[" + surfaceW + "," + surfaceH + "] bound:" + task.getBounds() + ", screenshot with surface rotation");
                            }
                            snapshotOrientation2 = surfaceOrientation;
                            z = false;
                        } catch (Exception e) {
                            z = false;
                            snapshotOrientation = snapshotOrientation2;
                            overrideCrop = null;
                            screenshotBuffer = createTaskSnapshot(task, scaleFraction, overrideCrop);
                            if (screenshotBuffer != null) {
                            }
                        }
                        try {
                            overrideCrop2 = new Rect(0, 0, surfaceW, surfaceH);
                        } catch (Exception e2) {
                            snapshotOrientation = snapshotOrientation2;
                            overrideCrop = null;
                            screenshotBuffer = createTaskSnapshot(task, scaleFraction, overrideCrop);
                            if (screenshotBuffer != null) {
                            }
                        }
                    } else {
                        z = false;
                    }
                    snapshotOrientation = snapshotOrientation2;
                    overrideCrop = overrideCrop2;
                } catch (Exception e3) {
                    mainWindow = mainWindow2;
                    z = false;
                    snapshotOrientation = snapshotOrientation2;
                    overrideCrop = null;
                    screenshotBuffer = createTaskSnapshot(task, scaleFraction, overrideCrop);
                    if (screenshotBuffer != null) {
                    }
                }
                screenshotBuffer = createTaskSnapshot(task, scaleFraction, overrideCrop);
                if (screenshotBuffer != null) {
                    if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                        Slog.w("WindowManager", "Failed to take screenshot for " + task);
                    }
                    return null;
                }
                return new ActivityManager.TaskSnapshot(appWindowToken.mActivityComponent, screenshotBuffer.getGraphicBuffer(), screenshotBuffer.getColorSpace(), snapshotOrientation, getInsets(mainWindow), isLowRamDevice, scaleFraction, true, task.getWindowingMode(), getSystemUiVisibility(task), (!appWindowToken.fillsParent() || (mainWindow.getAttrs().format != -1 ? true : z)) ? true : z);
            }
        } else {
            if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                Slog.i("WindowManager", "Attempted to take screenshot while display was off.");
            }
            return null;
        }
    }

    private boolean shouldDisableSnapshots() {
        return this.mIsRunningOnWear || this.mIsRunningOnTv || this.mIsRunningOnIoT;
    }

    private Rect getInsets(WindowState state) {
        Rect insets = minRect(state.getContentInsets(), state.getStableInsets());
        InsetUtils.addInsets(insets, state.mAppToken.getLetterboxInsets());
        return insets;
    }

    private Rect minRect(Rect rect1, Rect rect2) {
        return new Rect(Math.min(rect1.left, rect2.left), Math.min(rect1.top, rect2.top), Math.min(rect1.right, rect2.right), Math.min(rect1.bottom, rect2.bottom));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void getClosingTasks(ArraySet<AppWindowToken> closingApps, ArraySet<Task> outClosingTasks) {
        outClosingTasks.clear();
        for (int i = closingApps.size() - 1; i >= 0; i--) {
            Task task = closingApps.valueAt(i).getTask();
            if (task != null && !task.isVisible() && !this.mSkipClosingAppSnapshotTasks.contains(task)) {
                outClosingTasks.add(task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getSnapshotMode(Task task) {
        AppWindowToken topChild = (AppWindowToken) task.getTopChild();
        if (!task.isActivityTypeStandardOrUndefined() && !task.isActivityTypeAssistant()) {
            return 2;
        }
        if (topChild == null || !topChild.shouldUseAppThemeSnapshot()) {
            return 0;
        }
        return 1;
    }

    private ActivityManager.TaskSnapshot drawAppThemeSnapshot(Task task) {
        WindowState mainWindow;
        AppWindowToken topChild = (AppWindowToken) task.getTopChild();
        if (topChild == null || (mainWindow = topChild.findMainWindow()) == null) {
            return null;
        }
        int color = ColorUtils.setAlphaComponent(task.getTaskDescription().getBackgroundColor(), 255);
        WindowManager.LayoutParams attrs = mainWindow.getAttrs();
        TaskSnapshotSurface.SystemBarBackgroundPainter decorPainter = new TaskSnapshotSurface.SystemBarBackgroundPainter(attrs.flags, attrs.privateFlags, attrs.systemUiVisibility, task.getTaskDescription(), this.mFullSnapshotScale);
        int width = (int) (((float) task.getBounds().width()) * this.mFullSnapshotScale);
        int height = (int) (((float) task.getBounds().height()) * this.mFullSnapshotScale);
        RenderNode node = RenderNode.create("TaskSnapshotController", null);
        node.setLeftTopRightBottom(0, 0, width, height);
        node.setClipToBounds(false);
        RecordingCanvas c = node.start(width, height);
        c.drawColor(color);
        decorPainter.setInsets(mainWindow.getContentInsets(), mainWindow.getStableInsets());
        decorPainter.drawDecors(c, null);
        node.end(c);
        Bitmap hwBitmap = ThreadedRenderer.createHardwareBitmap(node, width, height);
        if (hwBitmap == null) {
            return null;
        }
        return new ActivityManager.TaskSnapshot(topChild.mActivityComponent, hwBitmap.createGraphicBufferHandle(), hwBitmap.getColorSpace(), topChild.getConfiguration().orientation, getInsets(mainWindow), ActivityManager.isLowRamDeviceStatic(), this.mFullSnapshotScale, false, task.getWindowingMode(), getSystemUiVisibility(task), false);
    }

    /* access modifiers changed from: package-private */
    public void onAppRemoved(AppWindowToken wtoken) {
        this.mCache.onAppRemoved(wtoken);
    }

    /* access modifiers changed from: package-private */
    public void onAppDied(AppWindowToken wtoken) {
        this.mCache.onAppDied(wtoken);
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskRemovedFromRecents(int taskId, int userId) {
        this.mCache.onTaskRemoved(taskId);
        this.mPersister.onTaskRemovedFromRecents(taskId, userId);
    }

    /* access modifiers changed from: package-private */
    public void removeObsoleteTaskFiles(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
        this.mPersister.removeObsoleteFiles(persistentTaskIds, runningUserIds);
    }

    /* access modifiers changed from: package-private */
    public void setPersisterPaused(boolean paused) {
        this.mPersister.setPaused(paused);
    }

    /* access modifiers changed from: package-private */
    public void screenTurningOff(WindowManagerPolicy.ScreenOffListener listener) {
        if (shouldDisableSnapshots()) {
            listener.onScreenOff();
        } else {
            this.mHandler.post(new Runnable(listener) {
                /* class com.android.server.wm.$$Lambda$TaskSnapshotController$Tj7bQvjfkzsOjJOdJXBpqCZnW1Q */
                private final /* synthetic */ WindowManagerPolicy.ScreenOffListener f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TaskSnapshotController.this.lambda$screenTurningOff$3$TaskSnapshotController(this.f$1);
                }
            });
        }
    }

    /* JADX INFO: finally extract failed */
    public /* synthetic */ void lambda$screenTurningOff$3$TaskSnapshotController(WindowManagerPolicy.ScreenOffListener listener) {
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mTmpTasks.clear();
                    this.mService.mRoot.forAllTasks(new Consumer() {
                        /* class com.android.server.wm.$$Lambda$TaskSnapshotController$pF831VjVO7J7eXZhalKp1CJKNC4 */

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            TaskSnapshotController.this.lambda$screenTurningOff$2$TaskSnapshotController((Task) obj);
                        }
                    });
                    snapshotTasks(this.mTmpTasks);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            listener.onScreenOff();
        }
    }

    public /* synthetic */ void lambda$screenTurningOff$2$TaskSnapshotController(Task task) {
        if (task.isVisible()) {
            this.mTmpTasks.add(task);
        }
    }

    public Bitmap takeSnapshotForKgd(AppWindowToken focusedApp) {
        ActivityManager.TaskSnapshot snapshot;
        Task task = focusedApp.getTask();
        if (task == null || !task.isVisible() || (snapshot = snapshotTask(task, true)) == null) {
            return null;
        }
        return Bitmap.wrapHardwareBuffer(snapshot.getSnapshot(), snapshot.getColorSpace());
    }

    private int getSystemUiVisibility(Task task) {
        WindowState topFullscreenWindow;
        AppWindowToken topFullscreenToken = task.getTopFullscreenAppToken();
        if (topFullscreenToken != null) {
            topFullscreenWindow = topFullscreenToken.getTopFullscreenWindow();
        } else {
            topFullscreenWindow = null;
        }
        if (topFullscreenWindow != null) {
            return topFullscreenWindow.getSystemUiVisibility();
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "mFullSnapshotScale=" + this.mFullSnapshotScale);
        this.mCache.dump(pw, prefix);
    }
}
