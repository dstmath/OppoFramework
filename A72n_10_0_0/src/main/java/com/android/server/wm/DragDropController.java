package com.android.server.wm;

import android.content.ClipData;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import android.view.IWindow;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import com.android.internal.util.Preconditions;
import com.android.server.wm.DragState;
import com.android.server.wm.WindowManagerInternal;
import com.mediatek.server.wm.WmsExt;
import java.util.concurrent.atomic.AtomicReference;

/* access modifiers changed from: package-private */
public class DragDropController {
    private static final float DRAG_SHADOW_ALPHA_TRANSPARENT = 0.7071f;
    private static final long DRAG_TIMEOUT_MS = 5000;
    static final int MSG_ANIMATION_END = 2;
    static final int MSG_DRAG_END_TIMEOUT = 0;
    static final int MSG_TEAR_DOWN_DRAG_AND_DROP_INPUT = 1;
    private AtomicReference<WindowManagerInternal.IDragDropCallback> mCallback = new AtomicReference<>(new WindowManagerInternal.IDragDropCallback() {
        /* class com.android.server.wm.DragDropController.AnonymousClass1 */
    });
    private DragState mDragState;
    private final Handler mHandler;
    private WindowManagerService mService;

    /* access modifiers changed from: package-private */
    public boolean dragDropActiveLocked() {
        DragState dragState = this.mDragState;
        return dragState != null && !dragState.isClosing();
    }

    /* access modifiers changed from: package-private */
    public void registerCallback(WindowManagerInternal.IDragDropCallback callback) {
        Preconditions.checkNotNull(callback);
        this.mCallback.set(callback);
    }

    DragDropController(WindowManagerService service, Looper looper) {
        this.mService = service;
        this.mHandler = new DragHandler(service, looper);
    }

    /* access modifiers changed from: package-private */
    public void sendDragStartedIfNeededLocked(WindowState window) {
        this.mDragState.sendDragStartedIfNeededLocked(window);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x02e2  */
    public IBinder performDrag(SurfaceSession session, int callerPid, int callerUid, IWindow window, int flags, SurfaceControl surface, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) {
        Throwable th;
        SurfaceControl surface2;
        Throwable th2;
        Throwable th3;
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d(WmsExt.TAG, "perform drag: win=" + window + " surface=" + surface + " flags=" + Integer.toHexString(flags) + " data=" + data);
        }
        IBinder dragToken = new Binder();
        boolean callbackResult = this.mCallback.get().prePerformDrag(window, dragToken, touchSource, touchX, touchY, thumbCenterX, thumbCenterY, data);
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!callbackResult) {
                        try {
                            Slog.w(WmsExt.TAG, "IDragDropCallback rejects the performDrag request");
                            if (surface != null) {
                                try {
                                    surface.release();
                                } catch (Throwable th4) {
                                    th3 = th4;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th3;
                                }
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            this.mCallback.get().postPerformDrag();
                            return null;
                        } catch (Throwable th5) {
                            th2 = th5;
                            surface2 = surface;
                        }
                    } else if (dragDropActiveLocked()) {
                        Slog.w(WmsExt.TAG, "Drag already in progress");
                        if (surface != null) {
                            surface.release();
                        }
                        if (this.mDragState != null && !this.mDragState.isInProgress()) {
                            this.mDragState.closeLocked();
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        this.mCallback.get().postPerformDrag();
                        return null;
                    } else {
                        WindowState callingWin = this.mService.windowForClientLocked((Session) null, window, false);
                        if (callingWin == null) {
                            Slog.w(WmsExt.TAG, "Bad requesting window " + window);
                            if (surface != null) {
                                surface.release();
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            this.mCallback.get().postPerformDrag();
                            return null;
                        }
                        DisplayContent displayContent = callingWin.getDisplayContent();
                        if (displayContent == null) {
                            Slog.w(WmsExt.TAG, "display content is null");
                            if (surface != null) {
                                surface.release();
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            this.mCallback.get().postPerformDrag();
                            return null;
                        }
                        float alpha = (flags & 512) == 0 ? DRAG_SHADOW_ALPHA_TRANSPARENT : 1.0f;
                        try {
                        } catch (Throwable th6) {
                            th2 = th6;
                            surface2 = surface;
                            if (surface2 != null) {
                            }
                            this.mDragState.closeLocked();
                            throw th2;
                        }
                        try {
                            try {
                                this.mDragState = new DragState(this.mService, this, new Binder(), surface, flags, window.asBinder());
                                surface2 = null;
                                try {
                                    this.mDragState.mPid = callerPid;
                                    this.mDragState.mUid = callerUid;
                                    this.mDragState.mOriginalAlpha = alpha;
                                } catch (Throwable th7) {
                                    th2 = th7;
                                    if (surface2 != null) {
                                    }
                                    this.mDragState.closeLocked();
                                    throw th2;
                                }
                            } catch (Throwable th8) {
                                th2 = th8;
                                surface2 = surface;
                                if (surface2 != null) {
                                }
                                this.mDragState.closeLocked();
                                throw th2;
                            }
                            try {
                                this.mDragState.mToken = dragToken;
                                this.mDragState.mDisplayContent = displayContent;
                                if (!this.mCallback.get().registerInputChannel(this.mDragState, displayContent.getDisplay(), this.mService.mInputManager, callingWin.mInputChannel)) {
                                    Slog.e(WmsExt.TAG, "Unable to transfer touch focus");
                                    if (0 != 0) {
                                        try {
                                            surface2.release();
                                        } catch (Throwable th9) {
                                            th3 = th9;
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            throw th3;
                                        }
                                    }
                                    if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                        this.mDragState.closeLocked();
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    this.mCallback.get().postPerformDrag();
                                    return null;
                                }
                                this.mDragState.mData = data;
                                try {
                                    this.mDragState.broadcastDragStartedLocked(touchX, touchY);
                                    try {
                                        this.mDragState.overridePointerIconLocked(touchSource);
                                        this.mDragState.mThumbOffsetX = thumbCenterX;
                                    } catch (Throwable th10) {
                                        th2 = th10;
                                        if (surface2 != null) {
                                        }
                                        this.mDragState.closeLocked();
                                        throw th2;
                                    }
                                } catch (Throwable th11) {
                                    th2 = th11;
                                    if (surface2 != null) {
                                    }
                                    this.mDragState.closeLocked();
                                    throw th2;
                                }
                                try {
                                    this.mDragState.mThumbOffsetY = thumbCenterY;
                                    SurfaceControl surfaceControl = this.mDragState.mSurfaceControl;
                                    if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                                        Slog.i(WmsExt.TAG, ">>> OPEN TRANSACTION performDrag");
                                    }
                                    SurfaceControl.Transaction transaction = callingWin.getPendingTransaction();
                                    transaction.setAlpha(surfaceControl, this.mDragState.mOriginalAlpha);
                                    transaction.setPosition(surfaceControl, touchX - thumbCenterX, touchY - thumbCenterY);
                                    transaction.show(surfaceControl);
                                    displayContent.reparentToOverlay(transaction, surfaceControl);
                                    callingWin.scheduleAnimation();
                                    if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                                        Slog.i(WmsExt.TAG, "<<< CLOSE TRANSACTION performDrag");
                                    }
                                    this.mDragState.notifyLocationLocked(touchX, touchY);
                                    if (0 != 0) {
                                        try {
                                            surface2.release();
                                        } catch (Throwable th12) {
                                            th3 = th12;
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            throw th3;
                                        }
                                    }
                                    if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                        this.mDragState.closeLocked();
                                    }
                                } catch (Throwable th13) {
                                    th2 = th13;
                                    if (surface2 != null) {
                                    }
                                    this.mDragState.closeLocked();
                                    throw th2;
                                }
                                try {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    this.mCallback.get().postPerformDrag();
                                    return dragToken;
                                } catch (Throwable th14) {
                                    th = th14;
                                    this.mCallback.get().postPerformDrag();
                                    throw th;
                                }
                            } catch (Throwable th15) {
                                th2 = th15;
                                if (surface2 != null) {
                                }
                                this.mDragState.closeLocked();
                                throw th2;
                            }
                        } catch (Throwable th16) {
                            th2 = th16;
                            surface2 = surface;
                            if (surface2 != null) {
                                surface2.release();
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                            throw th2;
                        }
                    }
                } catch (Throwable th17) {
                    th2 = th17;
                    surface2 = surface;
                    if (surface2 != null) {
                    }
                    this.mDragState.closeLocked();
                    throw th2;
                }
            }
        } catch (Throwable th18) {
            th = th18;
            this.mCallback.get().postPerformDrag();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void reportDropResult(IWindow window, boolean consumed) {
        IBinder token = window.asBinder();
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d(WmsExt.TAG, "Drop result=" + consumed + " reported by " + token);
        }
        this.mCallback.get().preReportDropResult(window, consumed);
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mDragState == null) {
                        Slog.w(WmsExt.TAG, "Drop result given but no drag in progress");
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (this.mDragState.mToken == token) {
                        this.mHandler.removeMessages(0, window.asBinder());
                        if (this.mService.windowForClientLocked((Session) null, window, false) == null) {
                            Slog.w(WmsExt.TAG, "Bad result-reporting window " + window);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            this.mCallback.get().postReportDropResult();
                            return;
                        }
                        this.mDragState.mDragResult = consumed;
                        this.mDragState.endDragLocked();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        this.mCallback.get().postReportDropResult();
                    } else {
                        Slog.w(WmsExt.TAG, "Invalid drop-result claim by " + window);
                        throw new IllegalStateException("reportDropResult() by non-recipient");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            this.mCallback.get().postReportDropResult();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void cancelDragAndDrop(IBinder dragToken, boolean skipAnimation) {
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d(WmsExt.TAG, "cancelDragAndDrop");
        }
        this.mCallback.get().preCancelDragAndDrop(dragToken);
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mDragState == null) {
                        Slog.w(WmsExt.TAG, "cancelDragAndDrop() without prepareDrag()");
                        throw new IllegalStateException("cancelDragAndDrop() without prepareDrag()");
                    } else if (this.mDragState.mToken == dragToken) {
                        this.mDragState.mDragResult = false;
                        this.mDragState.cancelDragLocked(skipAnimation);
                    } else {
                        Slog.w(WmsExt.TAG, "cancelDragAndDrop() does not match prepareDrag()");
                        throw new IllegalStateException("cancelDragAndDrop() does not match prepareDrag()");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            this.mCallback.get().postCancelDragAndDrop();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleMotionEvent(boolean keepHandling, float newX, float newY) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (dragDropActiveLocked()) {
                    if (keepHandling) {
                        this.mDragState.notifyMoveLocked(newX, newY);
                    } else {
                        this.mDragState.notifyDropLocked(newX, newY);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dragRecipientEntered(IWindow window) {
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d(WmsExt.TAG, "Drag into new candidate view @ " + window.asBinder());
        }
    }

    /* access modifiers changed from: package-private */
    public void dragRecipientExited(IWindow window) {
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d(WmsExt.TAG, "Drag from old candidate view @ " + window.asBinder());
        }
    }

    /* access modifiers changed from: package-private */
    public void sendHandlerMessage(int what, Object arg) {
        this.mHandler.obtainMessage(what, arg).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void sendTimeoutMessage(int what, Object arg) {
        this.mHandler.removeMessages(what, arg);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, arg), 5000);
    }

    /* access modifiers changed from: package-private */
    public void onDragStateClosedLocked(DragState dragState) {
        if (this.mDragState != dragState) {
            Slog.wtf(WmsExt.TAG, "Unknown drag state is closed");
        } else {
            this.mDragState = null;
        }
    }

    private class DragHandler extends Handler {
        private final WindowManagerService mService;

        DragHandler(WindowManagerService service, Looper looper) {
            super(looper);
            this.mService = service;
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                IBinder win = (IBinder) msg.obj;
                if (WindowManagerDebugConfig.DEBUG_DRAG) {
                    Slog.w(WmsExt.TAG, "Timeout ending drag to win " + win);
                }
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (DragDropController.this.mDragState != null) {
                            DragDropController.this.mDragState.mDragResult = false;
                            DragDropController.this.mDragState.endDragLocked();
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 1) {
                if (WindowManagerDebugConfig.DEBUG_DRAG) {
                    Slog.d(WmsExt.TAG, "Drag ending; tearing down input channel");
                }
                DragState.InputInterceptor interceptor = (DragState.InputInterceptor) msg.obj;
                if (interceptor != null) {
                    synchronized (this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            interceptor.tearDown();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            } else if (i == 2) {
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (DragDropController.this.mDragState == null) {
                            Slog.wtf(WmsExt.TAG, "mDragState unexpectedly became null while plyaing animation");
                            return;
                        }
                        DragDropController.this.mDragState.closeLocked();
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }
    }
}
