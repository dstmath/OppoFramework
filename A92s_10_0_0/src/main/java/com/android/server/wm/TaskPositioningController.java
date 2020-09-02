package com.android.server.wm;

import android.app.IActivityTaskManager;
import android.common.OppoFeatureCache;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Slog;
import android.view.Display;
import android.view.IWindow;
import android.view.InputWindowHandle;
import android.view.SurfaceControl;
import com.android.internal.annotations.GuardedBy;
import com.android.server.input.InputManagerService;
import com.mediatek.server.wm.WmsExt;

class TaskPositioningController {
    private final IActivityTaskManager mActivityManager;
    private final Handler mHandler;
    private final InputManagerService mInputManager;
    private SurfaceControl mInputSurface;
    private DisplayContent mPositioningDisplay;
    private final WindowManagerService mService;
    @GuardedBy({"WindowManagerSerivce.mWindowMap"})
    private TaskPositioner mTaskPositioner;
    private final Rect mTmpClipRect = new Rect();
    private IBinder mTransferTouchFromToken;

    /* access modifiers changed from: package-private */
    public boolean isPositioningLocked() {
        return this.mTaskPositioner != null;
    }

    /* access modifiers changed from: package-private */
    public InputWindowHandle getDragWindowHandleLocked() {
        TaskPositioner taskPositioner = this.mTaskPositioner;
        if (taskPositioner != null) {
            return taskPositioner.mDragWindowHandle;
        }
        return null;
    }

    TaskPositioningController(WindowManagerService service, InputManagerService inputManager, IActivityTaskManager activityManager, Looper looper) {
        this.mService = service;
        this.mInputManager = inputManager;
        this.mActivityManager = activityManager;
        this.mHandler = new Handler(looper);
    }

    /* access modifiers changed from: package-private */
    public void hideInputSurface(SurfaceControl.Transaction t, int displayId) {
        SurfaceControl surfaceControl;
        DisplayContent displayContent = this.mPositioningDisplay;
        if (displayContent != null && displayContent.getDisplayId() == displayId && (surfaceControl = this.mInputSurface) != null) {
            t.hide(surfaceControl);
        }
    }

    /* access modifiers changed from: package-private */
    public void showInputSurface(SurfaceControl.Transaction t, int displayId) {
        DisplayContent displayContent = this.mPositioningDisplay;
        if (displayContent != null && displayContent.getDisplayId() == displayId) {
            DisplayContent dc = this.mService.mRoot.getDisplayContent(displayId);
            if (this.mInputSurface == null) {
                this.mInputSurface = this.mService.makeSurfaceBuilder(dc.getSession()).setContainerLayer().setName("Drag and Drop Input Consumer").build();
            }
            InputWindowHandle h = getDragWindowHandleLocked();
            if (h == null) {
                Slog.w(WmsExt.TAG, "Drag is in progress but there is no drag window handle.");
                return;
            }
            t.show(this.mInputSurface);
            t.setInputWindowInfo(this.mInputSurface, h);
            t.setLayer(this.mInputSurface, Integer.MAX_VALUE);
            Display display = dc.getDisplay();
            Point p = new Point();
            display.getRealSize(p);
            this.mTmpClipRect.set(0, 0, p.x, p.y);
            t.setWindowCrop(this.mInputSurface, this.mTmpClipRect);
            t.transferTouchFocus(this.mTransferTouchFromToken, h.token);
            this.mTransferTouchFromToken = null;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.view.IWindow, boolean):com.android.server.wm.WindowState
     arg types: [?[OBJECT, ARRAY], android.view.IWindow, int]
     candidates:
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.view.IWindow, boolean):com.android.server.wm.WindowState */
    /* access modifiers changed from: package-private */
    public boolean startMovingTask(IWindow window, float startX, float startY) {
        Throwable th;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                WindowState win = this.mService.windowForClientLocked((Session) null, window, false);
                try {
                    if (!startPositioningLocked(win, false, false, startX, startY)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    try {
                        this.mActivityManager.setFocusedTask(win.getTask().mTaskId);
                        return true;
                    } catch (RemoteException e) {
                        return true;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleTapOutsideTask(DisplayContent displayContent, int x, int y) {
        this.mHandler.post(new Runnable(displayContent, x, y) {
            /* class com.android.server.wm.$$Lambda$TaskPositioningController$u0oAwi82CbAGo2JAsAc_9ZLi70 */
            private final /* synthetic */ DisplayContent f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                TaskPositioningController.this.lambda$handleTapOutsideTask$0$TaskPositioningController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$handleTapOutsideTask$0$TaskPositioningController(DisplayContent displayContent, int x, int y) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Task task = displayContent.findTaskForResizePoint(x, y);
                if (task != null) {
                    OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).debugLogUtil(ColorZoomWindowDebugUtil.KEY_TPCONTROLLER_HANDLETAPOUTSIDETASK, task, Integer.valueOf(x), Integer.valueOf(y));
                    if (startPositioningLocked(task.getTopVisibleAppMainWindow(), true, task.preserveOrientationOnResize(), (float) x, (float) y)) {
                        try {
                            this.mActivityManager.setFocusedTask(task.mTaskId);
                        } catch (RemoteException e) {
                        }
                    } else {
                        return;
                    }
                }
                OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).handleTapOutsideTask(displayContent, task, x, y);
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean startPositioningLocked(WindowState win, boolean resize, boolean preserveOrientation, float startX, float startY) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d(WmsExt.TAG, "startPositioningLocked: win=" + win + ", resize=" + resize + ", preserveOrientation=" + preserveOrientation + ", {" + startX + ", " + startY + "}");
        }
        if (win == null || win.getAppToken() == null) {
            Slog.w(WmsExt.TAG, "startPositioningLocked: Bad window " + win);
            return false;
        } else if (win.mInputChannel == null) {
            Slog.wtf(WmsExt.TAG, "startPositioningLocked: " + win + " has no input channel,  probably being removed");
            return false;
        } else {
            DisplayContent displayContent = win.getDisplayContent();
            if (displayContent == null) {
                Slog.w(WmsExt.TAG, "startPositioningLocked: Invalid display content " + win);
                return false;
            }
            this.mPositioningDisplay = displayContent;
            this.mTaskPositioner = TaskPositioner.create(this.mService);
            WindowState transferFocusFromWin = win;
            if (!(displayContent.mCurrentFocus == null || displayContent.mCurrentFocus == win || displayContent.mCurrentFocus.mAppToken != win.mAppToken)) {
                transferFocusFromWin = displayContent.mCurrentFocus;
            }
            this.mTransferTouchFromToken = transferFocusFromWin.mInputChannel.getToken();
            this.mTaskPositioner.register(displayContent);
            this.mTaskPositioner.startDrag(win, resize, preserveOrientation, startX, startY);
            return true;
        }
    }

    public void finishTaskPositioning(IWindow window) {
        TaskPositioner taskPositioner = this.mTaskPositioner;
        if (taskPositioner != null && taskPositioner.mClientCallback == window.asBinder()) {
            finishTaskPositioning();
        }
    }

    /* access modifiers changed from: package-private */
    public void finishTaskPositioning() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$TaskPositioningController$z3n1stJjOdhDbXXrvPlvlqmON6k */

            public final void run() {
                TaskPositioningController.this.lambda$finishTaskPositioning$1$TaskPositioningController();
            }
        });
    }

    public /* synthetic */ void lambda$finishTaskPositioning$1$TaskPositioningController() {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d(WmsExt.TAG, "finishPositioning");
        }
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                cleanUpTaskPositioner();
                this.mPositioningDisplay = null;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void cleanUpTaskPositioner() {
        TaskPositioner positioner = this.mTaskPositioner;
        if (positioner != null) {
            this.mTaskPositioner = null;
            positioner.unregister();
        }
    }
}
