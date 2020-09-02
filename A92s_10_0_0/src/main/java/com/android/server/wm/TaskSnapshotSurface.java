package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.WindowConfiguration;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.GraphicBuffer;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.IWindowSession;
import android.view.InsetsState;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.policy.DecorView;
import com.android.internal.view.BaseIWindow;
import com.android.server.display.OppoBrightUtils;
import com.android.server.policy.WindowManagerPolicy;
import java.util.Objects;

class TaskSnapshotSurface implements WindowManagerPolicy.StartingSurface {
    private static final int FLAG_INHERIT_EXCLUDES = 830922808;
    private static final int MSG_REPORT_DRAW = 0;
    private static final int PRIVATE_FLAG_INHERITS = 131072;
    private static final long SIZE_MISMATCH_MINIMUM_TIME_MS = 450;
    private static final String TAG = "WindowManager";
    private static final String TITLE_FORMAT = "SnapshotStartingWindow for taskId=%s";
    private static String mName;
    /* access modifiers changed from: private */
    public static Handler sHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.server.wm.TaskSnapshotSurface.AnonymousClass1 */

        public void handleMessage(Message msg) {
            boolean hasDrawn;
            if (msg.what == 0) {
                TaskSnapshotSurface surface = (TaskSnapshotSurface) msg.obj;
                synchronized (surface.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        hasDrawn = surface.mHasDrawn;
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
                if (hasDrawn) {
                    surface.reportDrawn();
                }
            }
        }
    };
    private final Paint mBackgroundPaint = new Paint();
    private SurfaceControl mChildSurfaceControl;
    private final Rect mContentInsets = new Rect();
    private final Rect mFrame = new Rect();
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mHasDrawn;
    /* access modifiers changed from: private */
    public final int mOrientationOnCreation;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    private final IWindowSession mSession;
    private long mShownTime;
    private boolean mSizeMismatch;
    private ActivityManager.TaskSnapshot mSnapshot;
    private final Rect mStableInsets = new Rect();
    private final int mStatusBarColor;
    private final Surface mSurface;
    private SurfaceControl mSurfaceControl;
    @VisibleForTesting
    final SystemBarBackgroundPainter mSystemBarBackgroundPainter;
    private final Rect mTaskBounds;
    private final CharSequence mTitle;
    private final Window mWindow;

    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01d5, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x01e4, code lost:
        r51 = r0;
        r53 = r4;
        r52 = r0;
        r55 = r7;
        r54 = r0;
        r56 = r0;
        r57 = r11;
        r58 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:?, code lost:
        r0 = r11.addToDisplay(r0, r0.mSeq, r0, 8, r61.getDisplayContent().getDisplayId(), r0, r9, r9, r9, r12, (android.view.InputChannel) null, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0207, code lost:
        if (r0 >= 0) goto L_0x0234;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0209, code lost:
        android.util.Slog.w("WindowManager", "Failed to add snapshot starting window res=" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x021f, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0224, code lost:
        r51 = r0;
        r53 = r4;
        r55 = r7;
        r56 = r0;
        r57 = r11;
        r58 = r0;
        r52 = r0;
        r54 = r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0291  */
    static TaskSnapshotSurface create(WindowManagerService service, AppWindowToken token, ActivityManager.TaskSnapshot snapshot) {
        boolean mZoomWindow;
        int windowFlags;
        int windowPrivateFlags;
        int currentOrientation;
        Window window;
        IWindowSession session;
        Rect tmpFrame;
        int sysUiVis;
        Rect tmpContentInsets;
        Rect taskBounds;
        Rect tmpStableInsets;
        ActivityManager.TaskDescription taskDescription;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window2 = new Window();
        IWindowSession session2 = WindowManagerGlobal.getWindowSession();
        window2.setSession(session2);
        SurfaceControl surfaceControl = new SurfaceControl();
        Rect tmpRect = new Rect();
        DisplayCutout.ParcelableWrapper tmpCutout = new DisplayCutout.ParcelableWrapper();
        Rect tmpFrame2 = new Rect();
        Rect tmpContentInsets2 = new Rect();
        Rect tmpStableInsets2 = new Rect();
        InsetsState mTmpInsetsState = new InsetsState();
        MergedConfiguration tmpMergedConfiguration = new MergedConfiguration();
        ActivityManager.TaskDescription taskDescription2 = new ActivityManager.TaskDescription();
        taskDescription2.setBackgroundColor(-1);
        synchronized (service.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                WindowState mainWindow = token.findMainWindow();
                Task task = token.getTask();
                if (task == null) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        try {
                            sb.append("TaskSnapshotSurface.create: Failed to find task for token=");
                            sb.append(token);
                            Slog.w("WindowManager", sb.toString());
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        } catch (Throwable th) {
                            th = th;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } else {
                    try {
                        AppWindowToken topFullscreenToken = token.getTask().getTopFullscreenAppToken();
                        if (topFullscreenToken == null) {
                            try {
                                StringBuilder sb2 = new StringBuilder();
                                try {
                                    sb2.append("TaskSnapshotSurface.create: Failed to find top fullscreen for task=");
                                    sb2.append(task);
                                    Slog.w("WindowManager", sb2.toString());
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    return null;
                                } catch (Throwable th4) {
                                    th = th4;
                                    while (true) {
                                        break;
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                while (true) {
                                    break;
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } else {
                            try {
                                WindowState topFullscreenWindow = topFullscreenToken.getTopFullscreenWindow();
                                if (mainWindow != null) {
                                    if (topFullscreenWindow != null) {
                                        mZoomWindow = mainWindow.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM;
                                        try {
                                            int sysUiVis2 = topFullscreenWindow.getSystemUiVisibility();
                                            windowFlags = topFullscreenWindow.getAttrs().flags;
                                            windowPrivateFlags = topFullscreenWindow.getAttrs().privateFlags;
                                            layoutParams.packageName = mainWindow.getAttrs().packageName;
                                            layoutParams.windowAnimations = mainWindow.getAttrs().windowAnimations;
                                            layoutParams.dimAmount = mainWindow.getAttrs().dimAmount;
                                            layoutParams.type = 3;
                                            layoutParams.format = snapshot.getSnapshot().getFormat();
                                            layoutParams.flags = (windowFlags & -830922809) | 8 | 16;
                                            layoutParams.privateFlags = windowPrivateFlags & 131072;
                                            layoutParams.token = token.token;
                                            layoutParams.width = -1;
                                            layoutParams.height = -1;
                                            layoutParams.systemUiVisibility = sysUiVis2;
                                            layoutParams.setTitle(String.format(TITLE_FORMAT, Integer.valueOf(task.mTaskId)));
                                            if (token.toString() != null) {
                                                try {
                                                    mName = token.toString();
                                                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                                        Slog.d("WindowManager", " create name = " + mName);
                                                    }
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                    while (true) {
                                                        break;
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                            }
                                            ActivityManager.TaskDescription td = task.getTaskDescription();
                                            if (td != null) {
                                                taskDescription2.copyFrom(td);
                                            }
                                            Rect taskBounds2 = new Rect();
                                            task.getBounds(taskBounds2);
                                            currentOrientation = topFullscreenWindow.getConfiguration().orientation;
                                        } catch (Throwable th7) {
                                            th = th7;
                                            while (true) {
                                                break;
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    }
                                }
                                try {
                                    Slog.w("WindowManager", "TaskSnapshotSurface.create: Failed to find main window for token=" + token);
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    return null;
                                } catch (Throwable th8) {
                                    th = th8;
                                    while (true) {
                                        break;
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                while (true) {
                                    break;
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (Throwable th11) {
                th = th11;
                while (true) {
                    break;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        TaskSnapshotSurface snapshotSurface = new TaskSnapshotSurface(service, window, surfaceControl, snapshot, layoutParams.getTitle(), taskDescription, sysUiVis, windowFlags, windowPrivateFlags, taskBounds, currentOrientation);
        window.setOuter(snapshotSurface);
        try {
            try {
                session.relayout(window, window.mSeq, layoutParams, -1, -1, 0, 0, -1, tmpFrame, tmpRect, tmpContentInsets, tmpRect, tmpStableInsets, tmpRect, tmpRect, tmpCutout, tmpMergedConfiguration, surfaceControl, mTmpInsetsState);
            } catch (RemoteException e) {
            }
        } catch (RemoteException e2) {
        }
        snapshotSurface.setFrames(tmpFrame, tmpContentInsets, tmpStableInsets);
        if (mZoomWindow) {
            snapshotSurface.mSizeMismatch = false;
        }
        snapshotSurface.drawSnapshot();
        return snapshotSurface;
        snapshotSurface.setFrames(tmpFrame, tmpContentInsets, tmpStableInsets);
        if (mZoomWindow) {
        }
        snapshotSurface.drawSnapshot();
        return snapshotSurface;
    }

    @VisibleForTesting
    TaskSnapshotSurface(WindowManagerService service, Window window, SurfaceControl surfaceControl, ActivityManager.TaskSnapshot snapshot, CharSequence title, ActivityManager.TaskDescription taskDescription, int sysUiVis, int windowFlags, int windowPrivateFlags, Rect taskBounds, int currentOrientation) {
        this.mService = service;
        this.mSurface = new Surface();
        this.mHandler = new Handler(this.mService.mH.getLooper());
        this.mSession = WindowManagerGlobal.getWindowSession();
        this.mWindow = window;
        this.mSurfaceControl = surfaceControl;
        this.mSnapshot = snapshot;
        this.mTitle = title;
        int backgroundColor = taskDescription.getBackgroundColor();
        this.mBackgroundPaint.setColor(backgroundColor != 0 ? backgroundColor : -1);
        this.mTaskBounds = taskBounds;
        this.mSystemBarBackgroundPainter = new SystemBarBackgroundPainter(windowFlags, windowPrivateFlags, sysUiVis, taskDescription, 1.0f);
        this.mStatusBarColor = taskDescription.getStatusBarColor();
        this.mOrientationOnCreation = currentOrientation;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0061, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a1, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a4, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00a6, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ab, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_STARTING_WINDOW == false) goto L_0x00b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ad, code lost:
        android.util.Slog.v("WindowManager", "Removing snapshot surface");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b4, code lost:
        r9.mSession.remove(r9.mWindow);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        return;
     */
    @Override // com.android.server.policy.WindowManagerPolicy.StartingSurface
    public void remove() {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long now = SystemClock.uptimeMillis();
                if (mName != null && ((mName.contains("com.coloros.browser") || mName.contains("com.android.browser")) && this.mSizeMismatch)) {
                    this.mSizeMismatch = false;
                    this.mHandler.postAtTime(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$OevXHSXgaSE351ZqRnMoA024MM */

                        public final void run() {
                            TaskSnapshotSurface.this.remove();
                        }
                    }, this.mShownTime + 1000);
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v("WindowManager", "Defer removing snapshot surface (com.coloros.browser) in " + (now - this.mShownTime) + "ms");
                    }
                } else if (this.mSizeMismatch && now - this.mShownTime < SIZE_MISMATCH_MINIMUM_TIME_MS) {
                    this.mHandler.postAtTime(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$OevXHSXgaSE351ZqRnMoA024MM */

                        public final void run() {
                            TaskSnapshotSurface.this.remove();
                        }
                    }, this.mShownTime + SIZE_MISMATCH_MINIMUM_TIME_MS);
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v("WindowManager", "Defer removing snapshot surface in " + (now - this.mShownTime) + "ms");
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setFrames(Rect frame, Rect contentInsets, Rect stableInsets) {
        this.mFrame.set(frame);
        this.mContentInsets.set(contentInsets);
        this.mStableInsets.set(stableInsets);
        this.mSizeMismatch = (this.mFrame.width() == this.mSnapshot.getSnapshot().getWidth() && this.mFrame.height() == this.mSnapshot.getSnapshot().getHeight()) ? false : true;
        this.mSizeMismatch = false;
        this.mSystemBarBackgroundPainter.setInsets(contentInsets, stableInsets);
    }

    /* JADX INFO: finally extract failed */
    private void drawSnapshot() {
        GraphicBuffer buffer = this.mSnapshot.getSnapshot();
        this.mSurface.copyFrom(this.mSurfaceControl);
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Drawing snapshot surface sizeMismatch=" + this.mSizeMismatch);
        }
        if (this.mSizeMismatch) {
            drawSizeMismatchSnapshot(buffer);
        } else {
            drawSizeMatchSnapshot(buffer);
        }
        String str = mName;
        if (str != null && (str.contains("com.coloros.browser") || mName.contains("com.android.browser"))) {
            this.mSizeMismatch = true;
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "setFrames mSizeMismatch = " + this.mSizeMismatch);
            }
        }
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mShownTime = SystemClock.uptimeMillis();
                this.mHasDrawn = true;
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        reportDrawn();
        this.mSnapshot = null;
    }

    private void drawSizeMatchSnapshot(GraphicBuffer buffer) {
        this.mSurface.attachAndQueueBuffer(buffer);
        this.mSurface.release();
    }

    /* JADX INFO: finally extract failed */
    private void drawSizeMismatchSnapshot(GraphicBuffer buffer) {
        Rect frame;
        if (this.mSurface.isValid()) {
            SurfaceSession session = new SurfaceSession();
            boolean aspectRatioMismatch = Math.abs((((float) buffer.getWidth()) / ((float) buffer.getHeight())) - (((float) this.mFrame.width()) / ((float) this.mFrame.height()))) > 0.01f;
            this.mChildSurfaceControl = new SurfaceControl.Builder(session).setName(((Object) this.mTitle) + " - task-snapshot-surface").setBufferSize(buffer.getWidth(), buffer.getHeight()).setFormat(buffer.getFormat()).setParent(this.mSurfaceControl).build();
            Surface surface = new Surface();
            surface.copyFrom(this.mChildSurfaceControl);
            SurfaceControl.openTransaction();
            try {
                this.mChildSurfaceControl.show();
                if (aspectRatioMismatch) {
                    Rect crop = calculateSnapshotCrop();
                    frame = calculateSnapshotFrame(crop);
                    this.mChildSurfaceControl.setWindowCrop(crop);
                    this.mChildSurfaceControl.setPosition((float) frame.left, (float) frame.top);
                } else {
                    frame = null;
                }
                float scale = 1.0f / this.mSnapshot.getScale();
                this.mChildSurfaceControl.setMatrix(scale, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, scale);
                SurfaceControl.closeTransaction();
                surface.attachAndQueueBuffer(buffer);
                surface.release();
                if (aspectRatioMismatch) {
                    Canvas c = this.mSurface.lockCanvas(null);
                    drawBackgroundAndBars(c, frame);
                    this.mSurface.unlockCanvasAndPost(c);
                    this.mSurface.release();
                }
            } catch (Throwable th) {
                SurfaceControl.closeTransaction();
                throw th;
            }
        } else {
            throw new IllegalStateException("mSurface does not hold a valid surface.");
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect calculateSnapshotCrop() {
        Rect rect = new Rect();
        int i = 0;
        rect.set(0, 0, this.mSnapshot.getSnapshot().getWidth(), this.mSnapshot.getSnapshot().getHeight());
        Rect insets = this.mSnapshot.getContentInsets();
        boolean isTop = this.mTaskBounds.top == 0 && this.mFrame.top == 0;
        int scale = (int) (((float) insets.left) * this.mSnapshot.getScale());
        if (!isTop) {
            i = (int) (((float) insets.top) * this.mSnapshot.getScale());
        }
        rect.inset(scale, i, (int) (((float) insets.right) * this.mSnapshot.getScale()), (int) (((float) insets.bottom) * this.mSnapshot.getScale()));
        return rect;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect calculateSnapshotFrame(Rect crop) {
        Rect frame = new Rect(crop);
        float scale = this.mSnapshot.getScale();
        frame.scale(1.0f / scale);
        frame.offsetTo((int) (((float) (-crop.left)) / scale), (int) (((float) (-crop.top)) / scale));
        frame.offset(DecorView.getColorViewLeftInset(this.mStableInsets.left, this.mContentInsets.left), 0);
        return frame;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void drawBackgroundAndBars(Canvas c, Rect frame) {
        float f;
        int statusBarHeight = this.mSystemBarBackgroundPainter.getStatusBarColorViewHeight();
        boolean fillVertically = true;
        boolean fillHorizontally = c.getWidth() > frame.right;
        if (c.getHeight() <= frame.bottom) {
            fillVertically = false;
        }
        if (fillHorizontally) {
            float f2 = (float) frame.right;
            float f3 = Color.alpha(this.mStatusBarColor) == 255 ? (float) statusBarHeight : OppoBrightUtils.MIN_LUX_LIMITI;
            float width = (float) c.getWidth();
            if (fillVertically) {
                f = (float) frame.bottom;
            } else {
                f = (float) c.getHeight();
            }
            c.drawRect(f2, f3, width, f, this.mBackgroundPaint);
        }
        if (fillVertically) {
            c.drawRect(OppoBrightUtils.MIN_LUX_LIMITI, (float) frame.bottom, (float) c.getWidth(), (float) c.getHeight(), this.mBackgroundPaint);
        }
        this.mSystemBarBackgroundPainter.drawDecors(c, frame);
    }

    /* access modifiers changed from: private */
    public void reportDrawn() {
        try {
            this.mSession.finishDrawing(this.mWindow);
        } catch (RemoteException e) {
        }
    }

    @VisibleForTesting
    static class Window extends BaseIWindow {
        private TaskSnapshotSurface mOuter;

        Window() {
        }

        public void setOuter(TaskSnapshotSurface outer) {
            this.mOuter = outer;
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
            TaskSnapshotSurface taskSnapshotSurface;
            if (!(mergedConfiguration == null || (taskSnapshotSurface = this.mOuter) == null || taskSnapshotSurface.mOrientationOnCreation == mergedConfiguration.getMergedConfiguration().orientation)) {
                Handler access$400 = TaskSnapshotSurface.sHandler;
                TaskSnapshotSurface taskSnapshotSurface2 = this.mOuter;
                Objects.requireNonNull(taskSnapshotSurface2);
                access$400.post(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$OevXHSXgaSE351ZqRnMoA024MM */

                    public final void run() {
                        TaskSnapshotSurface.this.remove();
                    }
                });
            }
            if (reportDraw) {
                TaskSnapshotSurface.sHandler.obtainMessage(0, this.mOuter).sendToTarget();
            }
        }
    }

    static class SystemBarBackgroundPainter {
        private final Rect mContentInsets = new Rect();
        private final int mNavigationBarColor;
        private final Paint mNavigationBarPaint = new Paint();
        private final float mScale;
        private final Rect mStableInsets = new Rect();
        private final int mStatusBarColor;
        private final Paint mStatusBarPaint = new Paint();
        private final int mSysUiVis;
        private final int mWindowFlags;
        private final int mWindowPrivateFlags;

        SystemBarBackgroundPainter(int windowFlags, int windowPrivateFlags, int sysUiVis, ActivityManager.TaskDescription taskDescription, float scale) {
            this.mWindowFlags = windowFlags;
            this.mWindowPrivateFlags = windowPrivateFlags;
            this.mSysUiVis = sysUiVis;
            this.mScale = scale;
            Context context = ActivityThread.currentActivityThread().getSystemUiContext();
            int semiTransparent = context.getColor(17170985);
            this.mStatusBarColor = DecorView.calculateBarColor(windowFlags, 67108864, semiTransparent, taskDescription.getStatusBarColor(), sysUiVis, 8192, taskDescription.getEnsureStatusBarContrastWhenTransparent());
            this.mNavigationBarColor = DecorView.calculateBarColor(windowFlags, 134217728, semiTransparent, taskDescription.getNavigationBarColor(), sysUiVis, 16, taskDescription.getEnsureNavigationBarContrastWhenTransparent() && context.getResources().getBoolean(17891484));
            this.mStatusBarPaint.setColor(this.mStatusBarColor);
            this.mNavigationBarPaint.setColor(this.mNavigationBarColor);
        }

        /* access modifiers changed from: package-private */
        public void setInsets(Rect contentInsets, Rect stableInsets) {
            this.mContentInsets.set(contentInsets);
            this.mStableInsets.set(stableInsets);
        }

        /* access modifiers changed from: package-private */
        public int getStatusBarColorViewHeight() {
            if (DecorView.STATUS_BAR_COLOR_VIEW_ATTRIBUTES.isVisible(this.mSysUiVis, this.mStatusBarColor, this.mWindowFlags, (this.mWindowPrivateFlags & 131072) != 0)) {
                return (int) (((float) DecorView.getColorViewTopInset(this.mStableInsets.top, this.mContentInsets.top)) * this.mScale);
            }
            return 0;
        }

        private boolean isNavigationBarColorViewVisible() {
            return DecorView.NAVIGATION_BAR_COLOR_VIEW_ATTRIBUTES.isVisible(this.mSysUiVis, this.mNavigationBarColor, this.mWindowFlags, (this.mWindowPrivateFlags & 131072) != 0);
        }

        /* access modifiers changed from: package-private */
        public void drawDecors(Canvas c, Rect alreadyDrawnFrame) {
            drawStatusBarBackground(c, alreadyDrawnFrame, getStatusBarColorViewHeight());
            drawNavigationBarBackground(c);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void drawStatusBarBackground(Canvas c, Rect alreadyDrawnFrame, int statusBarHeight) {
            if (statusBarHeight > 0 && Color.alpha(this.mStatusBarColor) != 0) {
                if (alreadyDrawnFrame == null || c.getWidth() > alreadyDrawnFrame.right) {
                    c.drawRect((float) (alreadyDrawnFrame != null ? alreadyDrawnFrame.right : 0), OppoBrightUtils.MIN_LUX_LIMITI, (float) (c.getWidth() - ((int) (((float) DecorView.getColorViewRightInset(this.mStableInsets.right, this.mContentInsets.right)) * this.mScale))), (float) statusBarHeight, this.mStatusBarPaint);
                }
            }
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void drawNavigationBarBackground(Canvas c) {
            Rect navigationBarRect = new Rect();
            DecorView.getNavigationBarRect(c.getWidth(), c.getHeight(), this.mStableInsets, this.mContentInsets, navigationBarRect, this.mScale);
            if (isNavigationBarColorViewVisible() && Color.alpha(this.mNavigationBarColor) != 0 && !navigationBarRect.isEmpty()) {
                c.drawRect(navigationBarRect, this.mNavigationBarPaint);
            }
        }
    }
}
