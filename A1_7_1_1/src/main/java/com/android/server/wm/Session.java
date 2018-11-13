package com.android.server.wm;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.ClipData;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.os.UserHandle;
import android.util.Slog;
import android.view.Display;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession.Stub;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManager.LayoutParams;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import java.io.PrintWriter;

final class Session extends Stub implements DeathRecipient {
    final IWindowSessionCallback mCallback;
    final boolean mCanAddInternalSystemWindow;
    final boolean mCanHideNonSystemOverlayWindows;
    final IInputMethodClient mClient;
    boolean mClientDead = false;
    final IInputContext mInputContext;
    float mLastReportedAnimatorScale;
    int mNumWindow = 0;
    final int mPid;
    final WindowManagerService mService;
    final String mStringName;
    SurfaceSession mSurfaceSession;
    final int mUid;

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00fc A:{Splitter: B:18:0x00a1, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:36:0x00ef, code:
            if (r10.mService.mInputMethodManager != null) goto L_0x00f1;
     */
    /* JADX WARNING: Missing block: B:37:0x00f1, code:
            r10.mService.mInputMethodManager.removeClient(r13);
     */
    /* JADX WARNING: Missing block: B:38:0x00f8, code:
            android.os.Binder.restoreCallingIdentity(r4);
     */
    /* JADX WARNING: Missing block: B:40:0x00fd, code:
            android.os.Binder.restoreCallingIdentity(r4);
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Session(WindowManagerService service, IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) {
        boolean z;
        boolean z2 = true;
        this.mService = service;
        this.mCallback = callback;
        this.mClient = client;
        this.mInputContext = inputContext;
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
        if (service.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mCanAddInternalSystemWindow = z;
        if (service.mContext.checkCallingOrSelfPermission("android.permission.HIDE_NON_SYSTEM_OVERLAY_WINDOWS") != 0) {
            z2 = false;
        }
        this.mCanHideNonSystemOverlayWindows = z2;
        this.mLastReportedAnimatorScale = service.getCurrentAnimatorScale();
        StringBuilder sb = new StringBuilder();
        sb.append("Session{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" ");
        sb.append(this.mPid);
        if (this.mUid < 10000) {
            sb.append(":");
            sb.append(this.mUid);
        } else {
            sb.append(":u");
            sb.append(UserHandle.getUserId(this.mUid));
            sb.append('a');
            sb.append(UserHandle.getAppId(this.mUid));
        }
        sb.append("}");
        this.mStringName = sb.toString();
        synchronized (this.mService.mWindowMap) {
            if (this.mService.mInputMethodManager == null && this.mService.mHaveInputMethods) {
                IBinder b = ServiceManager.getService("input_method");
                this.mService.mInputMethodManager = IInputMethodManager.Stub.asInterface(b);
            }
        }
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mService.mInputMethodManager != null) {
                this.mService.mInputMethodManager.addClient(client, inputContext, this.mUid, this.mPid);
            } else {
                client.setUsingInputMethod(false);
            }
            client.asBinder().linkToDeath(this, 0);
            Binder.restoreCallingIdentity(ident);
        } catch (RemoteException e) {
        } catch (Throwable th) {
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf("WindowManager", "Window Session Crash", e);
            }
            throw e;
        }
    }

    public void binderDied() {
        try {
            if (this.mService.mInputMethodManager != null) {
                this.mService.mInputMethodManager.removeClient(this.mClient);
            }
        } catch (RemoteException e) {
        }
        synchronized (this.mService.mWindowMap) {
            this.mClient.asBinder().unlinkToDeath(this, 0);
            this.mClientDead = true;
            killSessionLocked();
        }
    }

    public int add(IWindow window, int seq, LayoutParams attrs, int viewVisibility, Rect outContentInsets, Rect outStableInsets, InputChannel outInputChannel) {
        return addToDisplay(window, seq, attrs, viewVisibility, 0, outContentInsets, outStableInsets, null, outInputChannel);
    }

    public int addToDisplay(IWindow window, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) {
        return this.mService.addWindow(this, window, seq, attrs, viewVisibility, displayId, outContentInsets, outStableInsets, outOutsets, outInputChannel);
    }

    public int addWithoutInputChannel(IWindow window, int seq, LayoutParams attrs, int viewVisibility, Rect outContentInsets, Rect outStableInsets) {
        return addToDisplayWithoutInputChannel(window, seq, attrs, viewVisibility, 0, outContentInsets, outStableInsets);
    }

    public int addToDisplayWithoutInputChannel(IWindow window, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets) {
        return this.mService.addWindow(this, window, seq, attrs, viewVisibility, displayId, outContentInsets, outStableInsets, null, null);
    }

    public void remove(IWindow window) {
        this.mService.removeWindow(this, window);
    }

    public void repositionChild(IWindow window, int left, int top, int right, int bottom, long deferTransactionUntilFrame, Rect outFrame) {
        this.mService.repositionChild(this, window, left, top, right, bottom, deferTransactionUntilFrame, outFrame);
    }

    public void prepareToReplaceWindows(IBinder appToken, boolean childrenOnly) {
        this.mService.setReplacingWindows(appToken, childrenOnly);
    }

    public int relayout(IWindow window, int seq, LayoutParams attrs, int requestedWidth, int requestedHeight, int viewFlags, int flags, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outsets, Rect outBackdropFrame, Configuration outConfig, Surface outSurface) {
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.d("WindowManager", ">>>>>> ENTERED relayout from " + Binder.getCallingPid());
        }
        int res = this.mService.relayoutWindow(this, window, seq, attrs, requestedWidth, requestedHeight, viewFlags, flags, outFrame, outOverscanInsets, outContentInsets, outVisibleInsets, outStableInsets, outsets, outBackdropFrame, outConfig, outSurface);
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.d("WindowManager", "<<<<<< EXITING relayout to " + Binder.getCallingPid());
        }
        return res;
    }

    public void performDeferredDestroy(IWindow window) {
        this.mService.performDeferredDestroyWindow(this, window);
    }

    public boolean outOfMemory(IWindow window) {
        return this.mService.outOfMemoryWindow(this, window);
    }

    public void setTransparentRegion(IWindow window, Region region) {
        this.mService.setTransparentRegionWindow(this, window, region);
    }

    public void setInsets(IWindow window, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableArea) {
        this.mService.setInsetsWindow(this, window, touchableInsets, contentInsets, visibleInsets, touchableArea);
    }

    public void getDisplayFrame(IWindow window, Rect outDisplayFrame) {
        this.mService.getWindowDisplayFrame(this, window, outDisplayFrame);
    }

    public void finishDrawing(IWindow window) {
        if (WindowManagerService.localLOGV) {
            Slog.v("WindowManager", "IWindow finishDrawing called for " + window);
        }
        Trace.traceBegin(32, "wmFinishDrawing");
        this.mService.finishDrawingWindow(this, window);
        Trace.traceEnd(32);
    }

    public void setInTouchMode(boolean mode) {
        synchronized (this.mService.mWindowMap) {
            this.mService.mInTouchMode = mode;
        }
    }

    public boolean getInTouchMode() {
        boolean z;
        synchronized (this.mService.mWindowMap) {
            z = this.mService.mInTouchMode;
        }
        return z;
    }

    public boolean performHapticFeedback(IWindow window, int effectId, boolean always) {
        boolean performHapticFeedbackLw;
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                performHapticFeedbackLw = this.mService.mPolicy.performHapticFeedbackLw(this.mService.windowForClientLocked(this, window, true), effectId, always);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return performHapticFeedbackLw;
    }

    public IBinder prepareDrag(IWindow window, int flags, int width, int height, Surface outSurface) {
        return this.mService.prepareDragSurface(window, this.mSurfaceSession, flags, width, height, outSurface);
    }

    public boolean performDrag(IWindow window, IBinder dragToken, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) {
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "perform drag: win=" + window + " data=" + data);
        }
        synchronized (this.mService.mWindowMap) {
            if (this.mService.mDragState == null) {
                Slog.w("WindowManager", "No drag prepared");
                throw new IllegalStateException("performDrag() without prepareDrag()");
            } else if (dragToken != this.mService.mDragState.mToken) {
                Slog.w("WindowManager", "Performing mismatched drag");
                throw new IllegalStateException("performDrag() does not match prepareDrag()");
            } else {
                WindowState callingWin = this.mService.windowForClientLocked(null, window, false);
                if (callingWin == null) {
                    Slog.w("WindowManager", "Bad requesting window " + window);
                    return false;
                }
                this.mService.mH.removeMessages(20, window.asBinder());
                DisplayContent displayContent = callingWin.getDisplayContent();
                if (displayContent == null) {
                    return false;
                }
                Display display = displayContent.getDisplay();
                this.mService.mDragState.register(display);
                this.mService.mInputMonitor.updateInputWindowsLw(true);
                if (this.mService.mInputManager.transferTouchFocus(callingWin.mInputChannel, this.mService.mDragState.mServerChannel)) {
                    this.mService.mDragState.mData = data;
                    this.mService.mDragState.broadcastDragStartedLw(touchX, touchY);
                    this.mService.mDragState.overridePointerIconLw(touchSource);
                    this.mService.mDragState.mThumbOffsetX = thumbCenterX;
                    this.mService.mDragState.mThumbOffsetY = thumbCenterY;
                    SurfaceControl surfaceControl = this.mService.mDragState.mSurfaceControl;
                    if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                        Slog.i("WindowManager", ">>> OPEN TRANSACTION performDrag");
                    }
                    SurfaceControl.openTransaction();
                    try {
                        surfaceControl.setPosition(touchX - thumbCenterX, touchY - thumbCenterY);
                        surfaceControl.setLayer(this.mService.mDragState.getDragLayerLw());
                        surfaceControl.setLayerStack(display.getLayerStack());
                        surfaceControl.show();
                        this.mService.mDragState.notifyLocationLw(touchX, touchY);
                        return true;
                    } finally {
                        SurfaceControl.closeTransaction();
                        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                            Slog.i("WindowManager", "<<< CLOSE TRANSACTION performDrag");
                        }
                    }
                } else {
                    Slog.e("WindowManager", "Unable to transfer touch focus");
                    this.mService.mDragState.unregister();
                    this.mService.mDragState = null;
                    this.mService.mInputMonitor.updateInputWindowsLw(true);
                    return false;
                }
            }
        }
    }

    public boolean startMovingTask(IWindow window, float startX, float startY) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "startMovingTask: {" + startX + "," + startY + "}");
        }
        long ident = Binder.clearCallingIdentity();
        try {
            boolean startMovingTask = this.mService.startMovingTask(window, startX, startY);
            return startMovingTask;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void reportDropResult(IWindow window, boolean consumed) {
        IBinder token = window.asBinder();
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "Drop result=" + consumed + " reported by " + token);
        }
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mService.mDragState == null) {
                    Slog.w("WindowManager", "Drop result given but no drag in progress");
                    Binder.restoreCallingIdentity(ident);
                } else if (this.mService.mDragState.mToken != token) {
                    Slog.w("WindowManager", "Invalid drop-result claim by " + window);
                    throw new IllegalStateException("reportDropResult() by non-recipient");
                } else {
                    this.mService.mH.removeMessages(21, window.asBinder());
                    if (this.mService.windowForClientLocked(null, window, false) == null) {
                        Slog.w("WindowManager", "Bad result-reporting window " + window);
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    this.mService.mDragState.mDragResult = consumed;
                    this.mService.mDragState.endDragLw();
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void cancelDragAndDrop(IBinder dragToken) {
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "cancelDragAndDrop");
        }
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mService.mDragState == null) {
                    Slog.w("WindowManager", "cancelDragAndDrop() without prepareDrag()");
                    throw new IllegalStateException("cancelDragAndDrop() without prepareDrag()");
                } else if (this.mService.mDragState.mToken != dragToken) {
                    Slog.w("WindowManager", "cancelDragAndDrop() does not match prepareDrag()");
                    throw new IllegalStateException("cancelDragAndDrop() does not match prepareDrag()");
                } else {
                    this.mService.mDragState.mDragResult = false;
                    this.mService.mDragState.cancelDragLw();
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void dragRecipientEntered(IWindow window) {
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "Drag into new candidate view @ " + window.asBinder());
        }
    }

    public void dragRecipientExited(IWindow window) {
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "Drag from old candidate view @ " + window.asBinder());
        }
    }

    public void setWallpaperPosition(IBinder window, float x, float y, float xStep, float yStep) {
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mService.mWallpaperControllerLocked.setWindowWallpaperPosition(this.mService.windowForClientLocked(this, window, true), x, y, xStep, yStep);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void wallpaperOffsetsComplete(IBinder window) {
        synchronized (this.mService.mWindowMap) {
            this.mService.mWallpaperControllerLocked.wallpaperOffsetsComplete(window);
        }
    }

    public void setWallpaperDisplayOffset(IBinder window, int x, int y) {
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mService.mWallpaperControllerLocked.setWindowWallpaperDisplayOffset(this.mService.windowForClientLocked(this, window, true), x, y);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        Bundle sendWindowWallpaperCommand;
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                sendWindowWallpaperCommand = this.mService.mWallpaperControllerLocked.sendWindowWallpaperCommand(this.mService.windowForClientLocked(this, window, true), action, x, y, z, extras, sync);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return sendWindowWallpaperCommand;
    }

    public void wallpaperCommandComplete(IBinder window, Bundle result) {
        synchronized (this.mService.mWindowMap) {
            this.mService.mWallpaperControllerLocked.wallpaperCommandComplete(window);
        }
    }

    public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
        synchronized (this.mService.mWindowMap) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mService.onRectangleOnScreenRequested(token, rectangle);
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public IWindowId getWindowId(IBinder window) {
        return this.mService.getWindowId(window);
    }

    public void pokeDrawLock(IBinder window) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mService.pokeDrawLock(this, window);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void updatePointerIcon(IWindow window) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mService.updatePointerIcon(window);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    void windowAddedLocked() {
        if (this.mSurfaceSession == null) {
            if (WindowManagerService.localLOGV) {
                Slog.v("WindowManager", "First window added to " + this + ", creating SurfaceSession");
            }
            this.mSurfaceSession = new SurfaceSession();
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                Slog.i("WindowManager", "  NEW SURFACE SESSION " + this.mSurfaceSession);
            }
            this.mService.mSessions.add(this);
            if (this.mLastReportedAnimatorScale != this.mService.getCurrentAnimatorScale()) {
                this.mService.dispatchNewAnimatorScaleLocked(this);
            }
        }
        this.mNumWindow++;
    }

    void windowRemovedLocked() {
        this.mNumWindow--;
        killSessionLocked();
    }

    void killSessionLocked() {
        if (this.mNumWindow <= 0 && this.mClientDead) {
            this.mService.mSessions.remove(this);
            if (this.mSurfaceSession != null) {
                if (WindowManagerService.localLOGV) {
                    Slog.v("WindowManager", "Last window removed from " + this + ", destroying " + this.mSurfaceSession);
                }
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i("WindowManager", "  KILL SURFACE SESSION " + this.mSurfaceSession);
                }
                try {
                    this.mSurfaceSession.kill();
                } catch (Exception e) {
                    Slog.w("WindowManager", "Exception thrown when killing surface session " + this.mSurfaceSession + " in session " + this + ": " + e.toString());
                }
                this.mSurfaceSession = null;
            }
        }
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mNumWindow=");
        pw.print(this.mNumWindow);
        pw.print(" mClientDead=");
        pw.print(this.mClientDead);
        pw.print(" mSurfaceSession=");
        pw.println(this.mSurfaceSession);
    }

    public String toString() {
        return this.mStringName;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2016-09-20 : Add for Surface detection", property = OppoRomType.ROM)
    public void setLastSurfaceAppName(String name) {
        this.mService.setLastSurfaceAppName(name);
    }
}
