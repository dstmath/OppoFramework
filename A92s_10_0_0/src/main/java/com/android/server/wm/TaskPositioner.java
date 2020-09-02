package com.android.server.wm;

import android.app.IActivityTaskManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.Display;
import android.view.IWindow;
import android.view.InputApplicationHandle;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputWindowHandle;
import android.view.MotionEvent;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class TaskPositioner extends OppoBaseTaskPositioner implements IBinder.DeathRecipient {
    private static final int CTRL_BOTTOM = 8;
    private static final int CTRL_LEFT = 1;
    private static final int CTRL_NONE = 0;
    private static final int CTRL_RIGHT = 2;
    private static final int CTRL_TOP = 4;
    private static final boolean DEBUG_ORIENTATION_VIOLATIONS = false;
    @VisibleForTesting
    static final float MIN_ASPECT = 1.2f;
    public static final float RESIZING_HINT_ALPHA = 0.5f;
    public static final int RESIZING_HINT_DURATION_MS = 0;
    static final int SIDE_MARGIN_DIP = 100;
    private static final String TAG = "WindowManager";
    private static final String TAG_LOCAL = "TaskPositioner";
    private static Factory sFactory;
    /* access modifiers changed from: private */
    public final IActivityTaskManager mActivityManager;
    IBinder mClientCallback;
    InputChannel mClientChannel;
    private int mCtrlType;
    private DisplayContent mDisplayContent;
    private final DisplayMetrics mDisplayMetrics;
    InputApplicationHandle mDragApplicationHandle;
    @VisibleForTesting
    boolean mDragEnded;
    InputWindowHandle mDragWindowHandle;
    private WindowPositionerEventReceiver mInputEventReceiver;
    private final Point mMaxVisibleSize;
    private int mMinVisibleHeight;
    private int mMinVisibleWidth;
    private boolean mPreserveOrientation;
    /* access modifiers changed from: private */
    public boolean mResizing;
    InputChannel mServerChannel;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    private int mSideMargin;
    private float mStartDragX;
    private float mStartDragY;
    private boolean mStartOrientationWasLandscape;
    @VisibleForTesting
    Task mTask;
    /* access modifiers changed from: private */
    public Rect mTmpRect;
    /* access modifiers changed from: private */
    public final Rect mWindowDragBounds;
    private final Rect mWindowOriginalBounds;

    @Retention(RetentionPolicy.SOURCE)
    @interface CtrlType {
    }

    private final class WindowPositionerEventReceiver extends BatchedInputEventReceiver {
        public WindowPositionerEventReceiver(InputChannel inputChannel, Looper looper, Choreographer choreographer) {
            super(inputChannel, looper, choreographer);
        }

        /* JADX INFO: finally extract failed */
        public void onInputEvent(InputEvent event) {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                MotionEvent motionEvent = (MotionEvent) event;
                boolean handled = false;
                try {
                    if (TaskPositioner.this.mDragEnded) {
                        finishInputEvent(event, true);
                        return;
                    }
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    int action = motionEvent.getAction();
                    if (action != 0) {
                        if (action == 1) {
                            if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                                Slog.w("WindowManager", "ACTION_UP @ {" + newX + ", " + newY + "}");
                            }
                            TaskPositioner.this.mDragEnded = true;
                        } else if (action == 2) {
                            if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                                Slog.w("WindowManager", "ACTION_MOVE @ {" + newX + ", " + newY + "}");
                            }
                            synchronized (TaskPositioner.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    TaskPositioner.this.mDragEnded = TaskPositioner.this.notifyMoveLocked(newX, newY);
                                    TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                                } catch (Throwable th) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            if (!TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds)) {
                                Trace.traceBegin(32, "wm.TaskPositioner.resizeTask");
                                try {
                                    TaskPositioner.this.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 1);
                                } catch (RemoteException e) {
                                }
                                Trace.traceEnd(32);
                            }
                        } else if (action == 3) {
                            if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                                Slog.w("WindowManager", "ACTION_CANCEL @ {" + newX + ", " + newY + "}");
                            }
                            TaskPositioner.this.mDragEnded = true;
                        }
                    } else if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
                        Slog.w("WindowManager", "ACTION_DOWN @ {" + newX + ", " + newY + "}");
                    }
                    if (TaskPositioner.this.mDragEnded) {
                        boolean wasResizing = TaskPositioner.this.mResizing;
                        synchronized (TaskPositioner.this.mService.mGlobalLock) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                TaskPositioner.this.endDragLocked();
                                TaskPositioner.this.mTask.getDimBounds(TaskPositioner.this.mTmpRect);
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        if (wasResizing) {
                            try {
                                if (!TaskPositioner.this.mTmpRect.equals(TaskPositioner.this.mWindowDragBounds)) {
                                    TaskPositioner.this.mActivityManager.resizeTask(TaskPositioner.this.mTask.mTaskId, TaskPositioner.this.mWindowDragBounds, 3);
                                }
                            } catch (RemoteException e2) {
                            }
                        }
                        TaskPositioner.this.mService.mTaskPositioningController.finishTaskPositioning();
                    }
                    handled = true;
                    finishInputEvent(event, handled);
                } catch (Exception e3) {
                    Slog.e("WindowManager", "Exception caught by drag handleMotion", e3);
                } catch (Throwable th2) {
                    finishInputEvent(event, false);
                    throw th2;
                }
            }
        }
    }

    @VisibleForTesting
    TaskPositioner(WindowManagerService service, IActivityTaskManager activityManager) {
        this.mDisplayMetrics = new DisplayMetrics();
        this.mTmpRect = new Rect();
        this.mWindowOriginalBounds = new Rect();
        this.mWindowDragBounds = new Rect();
        this.mMaxVisibleSize = new Point();
        this.mCtrlType = 0;
        this.mService = service;
        this.mActivityManager = activityManager;
    }

    TaskPositioner(WindowManagerService service) {
        this(service, service.mActivityTaskManager);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect getWindowDragBounds() {
        return this.mWindowDragBounds;
    }

    /* access modifiers changed from: package-private */
    public void register(DisplayContent displayContent) {
        Display display = displayContent.getDisplay();
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "Registering task positioner");
        }
        if (this.mClientChannel != null) {
            Slog.e("WindowManager", "Task positioner already registered");
            return;
        }
        this.mDisplayContent = displayContent;
        display.getMetrics(this.mDisplayMetrics);
        InputChannel[] channels = InputChannel.openInputChannelPair("WindowManager");
        this.mServerChannel = channels[0];
        this.mClientChannel = channels[1];
        this.mService.mInputManager.registerInputChannel(this.mServerChannel, null);
        this.mInputEventReceiver = new WindowPositionerEventReceiver(this.mClientChannel, this.mService.mAnimationHandler.getLooper(), this.mService.mAnimator.getChoreographer());
        this.mDragApplicationHandle = new InputApplicationHandle(new Binder());
        InputApplicationHandle inputApplicationHandle = this.mDragApplicationHandle;
        inputApplicationHandle.name = "WindowManager";
        inputApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mDragWindowHandle = new InputWindowHandle(inputApplicationHandle, (IWindow) null, display.getDisplayId());
        InputWindowHandle inputWindowHandle = this.mDragWindowHandle;
        inputWindowHandle.name = "WindowManager";
        inputWindowHandle.token = this.mServerChannel.getToken();
        this.mDragWindowHandle.layer = this.mService.getDragLayerLocked();
        InputWindowHandle inputWindowHandle2 = this.mDragWindowHandle;
        inputWindowHandle2.layoutParamsFlags = 0;
        inputWindowHandle2.layoutParamsType = 2016;
        inputWindowHandle2.dispatchingTimeoutNanos = 5000000000L;
        inputWindowHandle2.visible = true;
        inputWindowHandle2.canReceiveKeys = false;
        inputWindowHandle2.hasFocus = true;
        inputWindowHandle2.hasWallpaper = false;
        inputWindowHandle2.paused = false;
        inputWindowHandle2.ownerPid = Process.myPid();
        this.mDragWindowHandle.ownerUid = Process.myUid();
        InputWindowHandle inputWindowHandle3 = this.mDragWindowHandle;
        inputWindowHandle3.inputFeatures = 0;
        inputWindowHandle3.scaleFactor = 1.0f;
        inputWindowHandle3.touchableRegion.setEmpty();
        InputWindowHandle inputWindowHandle4 = this.mDragWindowHandle;
        inputWindowHandle4.frameLeft = 0;
        inputWindowHandle4.frameTop = 0;
        Point p = new Point();
        display.getRealSize(p);
        this.mDragWindowHandle.frameRight = p.x;
        this.mDragWindowHandle.frameBottom = p.y;
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.d("WindowManager", "Pausing rotation during re-position");
        }
        this.mDisplayContent.pauseRotationLocked();
        this.mDisplayContent.getInputMonitor().updateInputWindowsLw(true);
        this.mSideMargin = WindowManagerService.dipToPixel(100, this.mDisplayMetrics);
        this.mMinVisibleWidth = WindowManagerService.dipToPixel(48, this.mDisplayMetrics);
        this.mMinVisibleHeight = WindowManagerService.dipToPixel(32, this.mDisplayMetrics);
        display.getRealSize(this.mMaxVisibleSize);
        this.mDragEnded = false;
    }

    private class DisposeInputRunnable implements Runnable {
        private InputChannel mDisposeClientChannel;
        private WindowPositionerEventReceiver mDisposeInputEventReceiver;
        private InputChannel mDisposeServerChannel;

        public DisposeInputRunnable(WindowPositionerEventReceiver disposeInputEventReceiver, InputChannel disposeClientChannel, InputChannel disposeServerChannel) {
            this.mDisposeInputEventReceiver = disposeInputEventReceiver;
            this.mDisposeClientChannel = disposeClientChannel;
            this.mDisposeServerChannel = disposeServerChannel;
        }

        public void run() {
            this.mDisposeInputEventReceiver.dispose();
            this.mDisposeClientChannel.dispose();
            this.mDisposeServerChannel.dispose();
        }
    }

    /* access modifiers changed from: package-private */
    public void unregister() {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "Unregistering task positioner");
        }
        if (this.mClientChannel == null) {
            Slog.e("WindowManager", "Task positioner not registered");
            return;
        }
        this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
        this.mService.mAnimationHandler.post(new DisposeInputRunnable(this.mInputEventReceiver, this.mClientChannel, this.mServerChannel));
        this.mInputEventReceiver = null;
        this.mClientChannel = null;
        this.mServerChannel = null;
        this.mDragWindowHandle = null;
        this.mDragApplicationHandle = null;
        this.mDragEnded = true;
        this.mDisplayContent.getInputMonitor().updateInputWindowsLw(true);
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.d("WindowManager", "Resuming rotation after re-position");
        }
        this.mDisplayContent.resumeRotationLocked();
        this.mDisplayContent = null;
        this.mClientCallback.unlinkToDeath(this, 0);
    }

    /* access modifiers changed from: package-private */
    public void startDrag(WindowState win, boolean resize, boolean preserveOrientation, float startX, float startY) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "startDrag: win=" + win + ", resize=" + resize + ", preserveOrientation=" + preserveOrientation + ", {" + startX + ", " + startY + "}");
        }
        try {
            this.mClientCallback = win.mClient.asBinder();
            this.mClientCallback.linkToDeath(this, 0);
            this.mTask = win.getTask();
            this.mTask.getBounds(this.mTmpRect);
            startDrag(resize, preserveOrientation, startX, startY, this.mTmpRect);
        } catch (RemoteException e) {
            this.mService.mTaskPositioningController.finishTaskPositioning();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public void startDrag(boolean resize, boolean preserveOrientation, float startX, float startY, Rect startBounds) {
        boolean z = false;
        this.mCtrlType = 0;
        this.mStartDragX = startX;
        this.mStartDragY = startY;
        this.mPreserveOrientation = preserveOrientation;
        if (resize) {
            if (startX < ((float) startBounds.left)) {
                this.mCtrlType |= 1;
            }
            if (startX > ((float) startBounds.right)) {
                this.mCtrlType |= 2;
            }
            if (startY < ((float) startBounds.top)) {
                this.mCtrlType |= 4;
            }
            if (startY > ((float) startBounds.bottom)) {
                this.mCtrlType |= 8;
            }
            this.mResizing = this.mCtrlType != 0;
        }
        if (startBounds.width() >= startBounds.height()) {
            z = true;
        }
        this.mStartOrientationWasLandscape = z;
        this.mWindowOriginalBounds.set(startBounds);
        if (this.mResizing) {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    notifyMoveLocked(startX, startY);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mService.mH.post(new Runnable(startBounds) {
                /* class com.android.server.wm.$$Lambda$TaskPositioner$TE0EjYzJeOSFARmUlY6wF3y3c2U */
                private final /* synthetic */ Rect f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TaskPositioner.this.lambda$startDrag$0$TaskPositioner(this.f$1);
                }
            });
        }
        this.mWindowDragBounds.set(startBounds);
    }

    public /* synthetic */ void lambda$startDrag$0$TaskPositioner(Rect startBounds) {
        try {
            this.mActivityManager.resizeTask(this.mTask.mTaskId, startBounds, 3);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    public void endDragLocked() {
        this.mResizing = false;
        this.mTask.setDragResizing(false, 0);
    }

    /* access modifiers changed from: private */
    public boolean notifyMoveLocked(float x, float y) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "notifyMoveLocked: {" + x + "," + y + "}");
        }
        if (this.mCtrlType != 0) {
            resizeDrag(x, y);
            this.mTask.setDragResizing(true, 0);
            return false;
        }
        this.mTask.mStack.getDimBounds(this.mTmpRect);
        Rect stableBounds = new Rect();
        this.mDisplayContent.getStableRect(stableBounds);
        this.mTmpRect.intersect(stableBounds);
        int nX = (int) x;
        int nY = (int) y;
        if (!this.mTmpRect.contains(nX, nY)) {
            nX = Math.min(Math.max(nX, this.mTmpRect.left), this.mTmpRect.right);
            nY = Math.min(Math.max(nY, this.mTmpRect.top), this.mTmpRect.bottom);
        }
        updateWindowDragBounds(nX, nY, this.mTmpRect);
        return false;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void resizeDrag(float x, float y) {
        char c;
        int height;
        int width1;
        int height2;
        int width2;
        int height1;
        int width12;
        int height12;
        int deltaX = Math.round(x - this.mStartDragX);
        int deltaY = Math.round(y - this.mStartDragY);
        int left = this.mWindowOriginalBounds.left;
        int top = this.mWindowOriginalBounds.top;
        int right = this.mWindowOriginalBounds.right;
        int bottom = this.mWindowOriginalBounds.bottom;
        if (!this.mPreserveOrientation) {
            c = 0;
        } else {
            c = this.mStartOrientationWasLandscape ? (char) 39322 : 21845;
        }
        int width = right - left;
        int height3 = bottom - top;
        int i = this.mCtrlType;
        if ((i & 1) != 0) {
            width = Math.max(this.mMinVisibleWidth, width - deltaX);
        } else if ((i & 2) != 0) {
            width = Math.max(this.mMinVisibleWidth, width + deltaX);
        }
        int i2 = this.mCtrlType;
        if ((i2 & 4) != 0) {
            height3 = Math.max(this.mMinVisibleHeight, height3 - deltaY);
        } else if ((i2 & 8) != 0) {
            height3 = Math.max(this.mMinVisibleHeight, height3 + deltaY);
        }
        float aspect = ((float) width) / ((float) height3);
        if (!this.mPreserveOrientation || ((!this.mStartOrientationWasLandscape || aspect >= MIN_ASPECT) && (this.mStartOrientationWasLandscape || ((double) aspect) <= 0.8333333002196431d))) {
            width1 = width;
            height = height3;
        } else {
            if (this.mStartOrientationWasLandscape) {
                int width13 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, width));
                height1 = Math.min(height3, Math.round(((float) width13) / MIN_ASPECT));
                if (height1 < this.mMinVisibleHeight) {
                    height1 = this.mMinVisibleHeight;
                    width1 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, Math.round(((float) height1) * MIN_ASPECT)));
                } else {
                    width1 = width13;
                }
                int height22 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, height3));
                width2 = Math.max(width, Math.round(((float) height22) * MIN_ASPECT));
                if (width2 < this.mMinVisibleWidth) {
                    width2 = this.mMinVisibleWidth;
                    height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, Math.round(((float) width2) / MIN_ASPECT)));
                } else {
                    height2 = height22;
                }
            } else {
                int width14 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, width));
                int height13 = Math.max(height3, Math.round(((float) width14) * MIN_ASPECT));
                if (height13 < this.mMinVisibleHeight) {
                    int height14 = this.mMinVisibleHeight;
                    width12 = Math.max(this.mMinVisibleWidth, Math.min(this.mMaxVisibleSize.x, Math.round(((float) height14) / MIN_ASPECT)));
                    height12 = height14;
                } else {
                    width12 = width14;
                    height12 = height13;
                }
                int height23 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, height3));
                width2 = Math.min(width, Math.round(((float) height23) / MIN_ASPECT));
                if (width2 < this.mMinVisibleWidth) {
                    width2 = this.mMinVisibleWidth;
                    height2 = Math.max(this.mMinVisibleHeight, Math.min(this.mMaxVisibleSize.y, Math.round(((float) width2) * MIN_ASPECT)));
                } else {
                    height2 = height23;
                }
            }
            if ((width > right - left || height3 > bottom - top) == (width1 * height1 > width2 * height2)) {
                height = height1;
            } else {
                width1 = width2;
                height = height2;
            }
        }
        updateDraggedBounds(left, top, right, bottom, width1, height);
    }

    /* access modifiers changed from: package-private */
    public void updateDraggedBounds(int left, int top, int right, int bottom, int newWidth, int newHeight) {
        if ((this.mCtrlType & 1) != 0) {
            left = right - newWidth;
        } else {
            right = left + newWidth;
        }
        if ((this.mCtrlType & 4) != 0) {
            top = bottom - newHeight;
        } else {
            bottom = top + newHeight;
        }
        this.mWindowDragBounds.set(left, top, right, bottom);
        checkBoundsForOrientationViolations(this.mWindowDragBounds);
    }

    private void checkBoundsForOrientationViolations(Rect bounds) {
    }

    private void updateWindowDragBounds(int x, int y, Rect stackBounds) {
        int offsetX = Math.round(((float) x) - this.mStartDragX);
        int offsetY = Math.round(((float) y) - this.mStartDragY);
        this.mWindowDragBounds.set(this.mWindowOriginalBounds);
        int maxLeft = stackBounds.right - this.mMinVisibleWidth;
        int minLeft = (stackBounds.left + this.mMinVisibleWidth) - this.mWindowOriginalBounds.width();
        int minTop = stackBounds.top;
        this.mWindowDragBounds.offsetTo(Math.min(Math.max(this.mWindowOriginalBounds.left + offsetX, minLeft), maxLeft), Math.min(Math.max(this.mWindowOriginalBounds.top + offsetY, minTop), stackBounds.bottom - this.mMinVisibleHeight));
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "updateWindowDragBounds: " + this.mWindowDragBounds);
        }
    }

    public String toShortString() {
        return "WindowManager";
    }

    static void setFactory(Factory factory) {
        sFactory = factory;
    }

    static TaskPositioner create(WindowManagerService service) {
        if (sFactory == null) {
            sFactory = new Factory() {
                /* class com.android.server.wm.TaskPositioner.AnonymousClass1 */
            };
        }
        return sFactory.create(service);
    }

    public void binderDied() {
        this.mService.mTaskPositioningController.finishTaskPositioning();
    }

    interface Factory {
        default TaskPositioner create(WindowManagerService service) {
            return new TaskPositioner(service);
        }
    }
}
