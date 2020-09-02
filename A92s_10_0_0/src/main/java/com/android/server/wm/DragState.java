package com.android.server.wm;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.util.Slog;
import android.view.Display;
import android.view.DragEvent;
import android.view.IWindow;
import android.view.InputApplicationHandle;
import android.view.InputChannel;
import android.view.InputWindowHandle;
import android.view.SurfaceControl;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.server.LocalServices;
import com.android.server.display.OppoBrightUtils;
import com.android.server.usb.descriptors.UsbACInterface;
import com.mediatek.server.wm.WmsExt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public class DragState {
    private static final String ANIMATED_PROPERTY_ALPHA = "alpha";
    private static final String ANIMATED_PROPERTY_SCALE = "scale";
    private static final String ANIMATED_PROPERTY_X = "x";
    private static final String ANIMATED_PROPERTY_Y = "y";
    private static final int DRAG_FLAGS_URI_ACCESS = 3;
    private static final int DRAG_FLAGS_URI_PERMISSIONS = 195;
    private static final long MAX_ANIMATION_DURATION_MS = 375;
    private static final long MIN_ANIMATION_DURATION_MS = 195;
    volatile boolean mAnimationCompleted = false;
    private ValueAnimator mAnimator;
    boolean mCrossProfileCopyAllowed;
    private final Interpolator mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
    float mCurrentX;
    float mCurrentY;
    ClipData mData;
    ClipDescription mDataDescription;
    DisplayContent mDisplayContent;
    /* access modifiers changed from: private */
    public Point mDisplaySize = new Point();
    final DragDropController mDragDropController;
    boolean mDragInProgress;
    boolean mDragResult;
    int mFlags;
    InputInterceptor mInputInterceptor;
    SurfaceControl mInputSurface;
    private boolean mIsClosing;
    IBinder mLocalWin;
    ArrayList<WindowState> mNotifiedWindows;
    float mOriginalAlpha;
    float mOriginalX;
    float mOriginalY;
    int mPid;
    final WindowManagerService mService;
    int mSourceUserId;
    SurfaceControl mSurfaceControl;
    WindowState mTargetWindow;
    float mThumbOffsetX;
    float mThumbOffsetY;
    private final Rect mTmpClipRect = new Rect();
    IBinder mToken;
    int mTouchSource;
    private final SurfaceControl.Transaction mTransaction;
    IBinder mTransferTouchFromToken;
    int mUid;

    DragState(WindowManagerService service, DragDropController controller, IBinder token, SurfaceControl surface, int flags, IBinder localWin) {
        this.mService = service;
        this.mDragDropController = controller;
        this.mToken = token;
        this.mSurfaceControl = surface;
        this.mFlags = flags;
        this.mLocalWin = localWin;
        this.mNotifiedWindows = new ArrayList<>();
        this.mTransaction = service.mTransactionFactory.make();
    }

    /* access modifiers changed from: package-private */
    public boolean isClosing() {
        return this.mIsClosing;
    }

    private void hideInputSurface() {
        SurfaceControl surfaceControl = this.mInputSurface;
        if (surfaceControl != null) {
            this.mTransaction.hide(surfaceControl).apply();
        }
    }

    private void showInputSurface() {
        if (this.mInputSurface == null) {
            WindowManagerService windowManagerService = this.mService;
            this.mInputSurface = windowManagerService.makeSurfaceBuilder(windowManagerService.mRoot.getDisplayContent(this.mDisplayContent.getDisplayId()).getSession()).setContainerLayer().setName("Drag and Drop Input Consumer").build();
        }
        InputWindowHandle h = getInputWindowHandle();
        if (h == null) {
            Slog.w(WmsExt.TAG, "Drag is in progress but there is no drag window handle.");
            return;
        }
        this.mTransaction.show(this.mInputSurface);
        this.mTransaction.setInputWindowInfo(this.mInputSurface, h);
        this.mTransaction.setLayer(this.mInputSurface, Integer.MAX_VALUE);
        this.mTmpClipRect.set(0, 0, this.mDisplaySize.x, this.mDisplaySize.y);
        this.mTransaction.setWindowCrop(this.mInputSurface, this.mTmpClipRect);
        this.mTransaction.transferTouchFocus(this.mTransferTouchFromToken, h.token);
        this.mTransferTouchFromToken = null;
        this.mTransaction.syncInputWindows();
        this.mTransaction.apply();
    }

    /* access modifiers changed from: package-private */
    public void closeLocked() {
        float y;
        float y2;
        this.mIsClosing = true;
        if (this.mInputInterceptor != null) {
            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                Slog.d(WmsExt.TAG, "unregistering drag input channel");
            }
            this.mDragDropController.sendHandlerMessage(1, this.mInputInterceptor);
            this.mInputInterceptor = null;
        }
        hideInputSurface();
        if (this.mDragInProgress) {
            int myPid = Process.myPid();
            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                Slog.d(WmsExt.TAG, "broadcasting DRAG_ENDED");
            }
            Iterator<WindowState> it = this.mNotifiedWindows.iterator();
            while (it.hasNext()) {
                WindowState ws = it.next();
                if (this.mDragResult || ws.mSession.mPid != this.mPid) {
                    y = 0.0f;
                    y2 = 0.0f;
                } else {
                    float x = this.mCurrentX;
                    y = this.mCurrentY;
                    y2 = x;
                }
                DragEvent evt = DragEvent.obtain(4, y2, y, null, null, null, null, this.mDragResult);
                try {
                    ws.mClient.dispatchDragEvent(evt);
                } catch (RemoteException e) {
                    Slog.w(WmsExt.TAG, "Unable to drag-end window " + ws);
                }
                if (myPid != ws.mSession.mPid) {
                    evt.recycle();
                }
            }
            this.mNotifiedWindows.clear();
            this.mDragInProgress = false;
        }
        if (isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
            this.mService.restorePointerIconLocked(this.mDisplayContent, this.mCurrentX, this.mCurrentY);
            this.mTouchSource = 0;
        }
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            this.mTransaction.reparent(surfaceControl, null).apply();
            this.mSurfaceControl = null;
        }
        if (this.mAnimator != null && !this.mAnimationCompleted) {
            Slog.wtf(WmsExt.TAG, "Unexpectedly destroying mSurfaceControl while animation is running");
        }
        this.mFlags = 0;
        this.mLocalWin = null;
        this.mToken = null;
        this.mData = null;
        this.mThumbOffsetY = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mThumbOffsetX = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mNotifiedWindows = null;
        this.mDragDropController.onDragStateClosedLocked(this);
    }

    class InputInterceptor {
        InputChannel mClientChannel;
        InputApplicationHandle mDragApplicationHandle = new InputApplicationHandle(new Binder());
        InputWindowHandle mDragWindowHandle;
        DragInputEventReceiver mInputEventReceiver;
        InputChannel mServerChannel;

        InputInterceptor(Display display) {
            InputChannel[] channels = InputChannel.openInputChannelPair("drag");
            this.mServerChannel = channels[0];
            this.mClientChannel = channels[1];
            DragState.this.mService.mInputManager.registerInputChannel(this.mServerChannel, null);
            this.mInputEventReceiver = new DragInputEventReceiver(this.mClientChannel, DragState.this.mService.mH.getLooper(), DragState.this.mDragDropController);
            InputApplicationHandle inputApplicationHandle = this.mDragApplicationHandle;
            inputApplicationHandle.name = "drag";
            inputApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
            this.mDragWindowHandle = new InputWindowHandle(inputApplicationHandle, (IWindow) null, display.getDisplayId());
            InputWindowHandle inputWindowHandle = this.mDragWindowHandle;
            inputWindowHandle.name = "drag";
            inputWindowHandle.token = this.mServerChannel.getToken();
            this.mDragWindowHandle.layer = DragState.this.getDragLayerLocked();
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
            inputWindowHandle4.frameRight = DragState.this.mDisplaySize.x;
            this.mDragWindowHandle.frameBottom = DragState.this.mDisplaySize.y;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d(WmsExt.TAG, "Pausing rotation during drag");
            }
            DragState.this.mDisplayContent.pauseRotationLocked();
        }

        /* access modifiers changed from: package-private */
        public void tearDown() {
            DragState.this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
            this.mClientChannel.dispose();
            this.mServerChannel.dispose();
            this.mClientChannel = null;
            this.mServerChannel = null;
            this.mDragWindowHandle = null;
            this.mDragApplicationHandle = null;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d(WmsExt.TAG, "Resuming rotation after drag");
            }
            DragState.this.mDisplayContent.resumeRotationLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public InputChannel getInputChannel() {
        InputInterceptor inputInterceptor = this.mInputInterceptor;
        if (inputInterceptor == null) {
            return null;
        }
        return inputInterceptor.mServerChannel;
    }

    /* access modifiers changed from: package-private */
    public InputWindowHandle getInputWindowHandle() {
        InputInterceptor inputInterceptor = this.mInputInterceptor;
        if (inputInterceptor == null) {
            return null;
        }
        return inputInterceptor.mDragWindowHandle;
    }

    /* access modifiers changed from: package-private */
    public void register(Display display) {
        display.getRealSize(this.mDisplaySize);
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d(WmsExt.TAG, "registering drag input channel");
        }
        if (this.mInputInterceptor != null) {
            Slog.e(WmsExt.TAG, "Duplicate register of drag input channel");
            return;
        }
        this.mInputInterceptor = new InputInterceptor(display);
        showInputSurface();
    }

    /* access modifiers changed from: package-private */
    public int getDragLayerLocked() {
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(2016) * 10000) + 1000;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
     arg types: [com.android.server.wm.-$$Lambda$DragState$-yUFIMrhYYccZ0gwd6eVcpAE93o, int]
     candidates:
      com.android.server.wm.DisplayContent.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void */
    /* access modifiers changed from: package-private */
    public void broadcastDragStartedLocked(float touchX, float touchY) {
        this.mCurrentX = touchX;
        this.mOriginalX = touchX;
        this.mCurrentY = touchY;
        this.mOriginalY = touchY;
        ClipData clipData = this.mData;
        this.mDataDescription = clipData != null ? clipData.getDescription() : null;
        this.mNotifiedWindows.clear();
        this.mDragInProgress = true;
        this.mSourceUserId = UserHandle.getUserId(this.mUid);
        this.mCrossProfileCopyAllowed = true ^ ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).getUserRestriction(this.mSourceUserId, "no_cross_profile_copy_paste");
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d(WmsExt.TAG, "broadcasting DRAG_STARTED at (" + touchX + ", " + touchY + ")");
        }
        this.mDisplayContent.forAllWindows((Consumer<WindowState>) new Consumer(touchX, touchY) {
            /* class com.android.server.wm.$$Lambda$DragState$yUFIMrhYYccZ0gwd6eVcpAE93o */
            private final /* synthetic */ float f$1;
            private final /* synthetic */ float f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DragState.this.lambda$broadcastDragStartedLocked$0$DragState(this.f$1, this.f$2, (WindowState) obj);
            }
        }, false);
    }

    public /* synthetic */ void lambda$broadcastDragStartedLocked$0$DragState(float touchX, float touchY, WindowState w) {
        sendDragStartedLocked(w, touchX, touchY, this.mDataDescription);
    }

    private void sendDragStartedLocked(WindowState newWin, float touchX, float touchY, ClipDescription desc) {
        if (this.mDragInProgress && isValidDropTarget(newWin)) {
            DragEvent event = obtainDragEvent(newWin, 1, touchX, touchY, null, desc, null, null, false);
            try {
                newWin.mClient.dispatchDragEvent(event);
                this.mNotifiedWindows.add(newWin);
                if (Process.myPid() == newWin.mSession.mPid) {
                    return;
                }
            } catch (RemoteException e) {
                Slog.w(WmsExt.TAG, "Unable to drag-start window " + newWin);
                if (Process.myPid() == newWin.mSession.mPid) {
                    return;
                }
            } catch (Throwable th) {
                if (Process.myPid() != newWin.mSession.mPid) {
                    event.recycle();
                }
                throw th;
            }
            event.recycle();
        }
    }

    private boolean isValidDropTarget(WindowState targetWin) {
        if (targetWin == null || !targetWin.isPotentialDragTarget()) {
            return false;
        }
        if (((this.mFlags & 256) == 0 || !targetWindowSupportsGlobalDrag(targetWin)) && this.mLocalWin != targetWin.mClient.asBinder()) {
            return false;
        }
        if (this.mCrossProfileCopyAllowed || this.mSourceUserId == UserHandle.getUserId(targetWin.getOwningUid())) {
            return true;
        }
        return false;
    }

    private boolean targetWindowSupportsGlobalDrag(WindowState targetWin) {
        return targetWin.mAppToken == null || targetWin.mAppToken.mTargetSdk >= 24;
    }

    /* access modifiers changed from: package-private */
    public void sendDragStartedIfNeededLocked(WindowState newWin) {
        if (this.mDragInProgress && !isWindowNotified(newWin)) {
            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                Slog.d(WmsExt.TAG, "need to send DRAG_STARTED to new window " + newWin);
            }
            sendDragStartedLocked(newWin, this.mCurrentX, this.mCurrentY, this.mDataDescription);
        }
    }

    private boolean isWindowNotified(WindowState newWin) {
        Iterator<WindowState> it = this.mNotifiedWindows.iterator();
        while (it.hasNext()) {
            if (it.next() == newWin) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void endDragLocked() {
        if (this.mAnimator == null) {
            if (!this.mDragResult) {
                this.mAnimator = createReturnAnimationLocked();
            } else {
                closeLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelDragLocked(boolean skipAnimation) {
        if (this.mAnimator == null) {
            if (!this.mDragInProgress || skipAnimation) {
                closeLocked();
            } else {
                this.mAnimator = createCancelAnimationLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyMoveLocked(float x, float y) {
        if (this.mAnimator == null) {
            this.mCurrentX = x;
            this.mCurrentY = y;
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(WmsExt.TAG, ">>> OPEN TRANSACTION notifyMoveLocked");
            }
            this.mTransaction.setPosition(this.mSurfaceControl, x - this.mThumbOffsetX, y - this.mThumbOffsetY).apply();
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                Slog.i(WmsExt.TAG, "  DRAG " + this.mSurfaceControl + ": pos=(" + ((int) (x - this.mThumbOffsetX)) + "," + ((int) (y - this.mThumbOffsetY)) + ")");
            }
            notifyLocationLocked(x, y);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyLocationLocked(float x, float y) {
        WindowState touchedWin;
        WindowState touchedWin2 = this.mDisplayContent.getTouchableWinAtPointLocked(x, y);
        if (touchedWin2 == null || isWindowNotified(touchedWin2)) {
            touchedWin = touchedWin2;
        } else {
            touchedWin = null;
        }
        try {
            int myPid = Process.myPid();
            if (!(touchedWin == this.mTargetWindow || this.mTargetWindow == null)) {
                if (WindowManagerDebugConfig.DEBUG_DRAG) {
                    Slog.d(WmsExt.TAG, "sending DRAG_EXITED to " + this.mTargetWindow);
                }
                DragEvent evt = obtainDragEvent(this.mTargetWindow, 6, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, null, null, null, null, false);
                this.mTargetWindow.mClient.dispatchDragEvent(evt);
                if (myPid != this.mTargetWindow.mSession.mPid) {
                    evt.recycle();
                }
            }
            if (touchedWin != null) {
                DragEvent evt2 = obtainDragEvent(touchedWin, 2, x, y, null, null, null, null, false);
                touchedWin.mClient.dispatchDragEvent(evt2);
                if (myPid != touchedWin.mSession.mPid) {
                    evt2.recycle();
                }
            }
        } catch (RemoteException e) {
            Slog.w(WmsExt.TAG, "can't send drag notification to windows");
        }
        this.mTargetWindow = touchedWin;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00ab, code lost:
        if (r9 != r13.mSession.mPid) goto L_0x00ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ad, code lost:
        r2.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00d3, code lost:
        if (r3 == r13.mSession.mPid) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00d6, code lost:
        r20.mToken = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d8, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e0  */
    public void notifyDropLocked(float x, float y) {
        IDragAndDropPermissions iDragAndDropPermissions;
        int myPid;
        ClipData clipData;
        ClipData clipData2;
        if (this.mAnimator == null) {
            this.mCurrentX = x;
            this.mCurrentY = y;
            WindowState touchedWin = this.mDisplayContent.getTouchableWinAtPointLocked(x, y);
            if (!isWindowNotified(touchedWin)) {
                this.mDragResult = false;
                endDragLocked();
                return;
            }
            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                Slog.d(WmsExt.TAG, "sending DROP to " + touchedWin);
            }
            int targetUserId = UserHandle.getUserId(touchedWin.getOwningUid());
            int i = this.mFlags;
            if ((i & 256) == 0 || (i & 3) == 0 || (clipData2 = this.mData) == null) {
                iDragAndDropPermissions = null;
            } else {
                iDragAndDropPermissions = new DragAndDropPermissionsHandler(clipData2, this.mUid, touchedWin.getOwningPackage(), this.mFlags & 195, this.mSourceUserId, targetUserId);
            }
            int i2 = this.mSourceUserId;
            if (!(i2 == targetUserId || (clipData = this.mData) == null)) {
                clipData.fixUris(i2);
            }
            int myPid2 = Process.myPid();
            IBinder token = touchedWin.mClient.asBinder();
            DragEvent evt = obtainDragEvent(touchedWin, 3, x, y, null, null, this.mData, iDragAndDropPermissions, false);
            try {
                touchedWin.mClient.dispatchDragEvent(evt);
                this.mDragDropController.sendTimeoutMessage(0, token);
            } catch (RemoteException e) {
                myPid = myPid2;
                Slog.w(WmsExt.TAG, "can't send drop notification to win " + touchedWin);
                endDragLocked();
            } catch (Throwable th) {
                th = th;
                if (myPid != touchedWin.mSession.mPid) {
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInProgress() {
        return this.mDragInProgress;
    }

    private static DragEvent obtainDragEvent(WindowState win, int action, float x, float y, Object localState, ClipDescription description, ClipData data, IDragAndDropPermissions dragAndDropPermissions, boolean result) {
        return DragEvent.obtain(action, win.translateToWindowX(x), win.translateToWindowY(y), localState, description, data, dragAndDropPermissions, result);
    }

    private ValueAnimator createReturnAnimationLocked() {
        float f = this.mCurrentX;
        float f2 = this.mThumbOffsetX;
        float[] fArr = {f - f2, this.mOriginalX - f2};
        float f3 = this.mCurrentY;
        float f4 = this.mThumbOffsetY;
        float[] fArr2 = {f3 - f4, this.mOriginalY - f4};
        float f5 = this.mOriginalAlpha;
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, fArr), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, fArr2), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, 1.0f, 1.0f), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, f5, f5 / 2.0f));
        float translateX = this.mOriginalX - this.mCurrentX;
        float translateY = this.mOriginalY - this.mCurrentY;
        long duration = ((long) ((Math.sqrt((double) ((translateX * translateX) + (translateY * translateY))) / Math.sqrt((double) ((this.mDisplaySize.x * this.mDisplaySize.x) + (this.mDisplaySize.y * this.mDisplaySize.y)))) * 180.0d)) + MIN_ANIMATION_DURATION_MS;
        AnimationListener listener = new AnimationListener();
        animator.setDuration(duration);
        animator.setInterpolator(this.mCubicEaseOutInterpolator);
        animator.addListener(listener);
        animator.addUpdateListener(listener);
        this.mService.mAnimationHandler.post(new Runnable(animator) {
            /* class com.android.server.wm.$$Lambda$DragState$4E4tzlfJ9AKYEiVk7F8SFlBLwPc */
            private final /* synthetic */ ValueAnimator f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.start();
            }
        });
        return animator;
    }

    private ValueAnimator createCancelAnimationLocked() {
        float f = this.mCurrentX;
        float[] fArr = {f - this.mThumbOffsetX, f};
        float f2 = this.mCurrentY;
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, fArr), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, f2 - this.mThumbOffsetY, f2), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, 1.0f, OppoBrightUtils.MIN_LUX_LIMITI), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, this.mOriginalAlpha, 0.0f));
        AnimationListener listener = new AnimationListener();
        animator.setDuration(MIN_ANIMATION_DURATION_MS);
        animator.setInterpolator(this.mCubicEaseOutInterpolator);
        animator.addListener(listener);
        animator.addUpdateListener(listener);
        this.mService.mAnimationHandler.post(new Runnable(animator) {
            /* class com.android.server.wm.$$Lambda$DragState$WVn6eGpkutjNAUr_QLMbFLA5qw */
            private final /* synthetic */ ValueAnimator f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.start();
            }
        });
        return animator;
    }

    private boolean isFromSource(int source) {
        return (this.mTouchSource & source) == source;
    }

    /* access modifiers changed from: package-private */
    public void overridePointerIconLocked(int touchSource) {
        this.mTouchSource = touchSource;
        if (isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
            InputManager.getInstance().setPointerIconType(1021);
        }
    }

    private class AnimationListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private AnimationListener() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0065, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0066, code lost:
            r0.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0069, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0060, code lost:
            r2 = move-exception;
         */
        public void onAnimationUpdate(ValueAnimator animation) {
            SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
            transaction.setPosition(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_X)).floatValue(), ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_Y)).floatValue());
            transaction.setAlpha(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_ALPHA)).floatValue());
            transaction.setMatrix(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_SCALE)).floatValue(), OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_SCALE)).floatValue());
            transaction.apply();
            transaction.close();
        }

        public void onAnimationStart(Animator animator) {
        }

        public void onAnimationCancel(Animator animator) {
        }

        public void onAnimationRepeat(Animator animator) {
        }

        public void onAnimationEnd(Animator animator) {
            DragState dragState = DragState.this;
            dragState.mAnimationCompleted = true;
            dragState.mDragDropController.sendHandlerMessage(2, null);
        }
    }
}
