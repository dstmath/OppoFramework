package com.android.server.wm;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Slog;
import android.view.Display;
import android.view.DragEvent;
import android.view.InputChannel;
import android.view.SurfaceControl;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.server.display.OppoBrightUtils;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputWindowHandle;
import com.android.server.usb.descriptors.UsbACInterface;
import java.util.ArrayList;
import java.util.function.Consumer;

class DragState {
    private static final int DRAG_FLAGS_URI_ACCESS = 3;
    private static final int DRAG_FLAGS_URI_PERMISSIONS = 195;
    private static final long MAX_ANIMATION_DURATION_MS = 375;
    private static final long MIN_ANIMATION_DURATION_MS = 195;
    private Animation mAnimation;
    boolean mCrossProfileCopyAllowed;
    private final Interpolator mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
    float mCurrentX;
    float mCurrentY;
    ClipData mData;
    ClipDescription mDataDescription;
    DisplayContent mDisplayContent;
    private Point mDisplaySize = new Point();
    boolean mDragInProgress;
    boolean mDragResult;
    int mFlags;
    InputInterceptor mInputInterceptor;
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
    IBinder mToken;
    int mTouchSource;
    final Transformation mTransformation = new Transformation();
    int mUid;

    class InputInterceptor {
        InputChannel mClientChannel;
        InputApplicationHandle mDragApplicationHandle = new InputApplicationHandle(null);
        InputWindowHandle mDragWindowHandle;
        DragInputEventReceiver mInputEventReceiver;
        InputChannel mServerChannel;

        InputInterceptor(Display display) {
            InputChannel[] channels = InputChannel.openInputChannelPair("drag");
            this.mServerChannel = channels[0];
            this.mClientChannel = channels[1];
            DragState.this.mService.mInputManager.registerInputChannel(this.mServerChannel, null);
            WindowManagerService windowManagerService = DragState.this.mService;
            windowManagerService.getClass();
            this.mInputEventReceiver = new DragInputEventReceiver(this.mClientChannel, DragState.this.mService.mH.getLooper());
            this.mDragWindowHandle = new InputWindowHandle(this.mDragApplicationHandle, null, null, display.getDisplayId());
            this.mDragWindowHandle.name = "drag";
            this.mDragWindowHandle.inputChannel = this.mServerChannel;
            this.mDragWindowHandle.layer = DragState.this.getDragLayerLw();
            this.mDragWindowHandle.layoutParamsFlags = 0;
            this.mDragWindowHandle.layoutParamsType = 2016;
            this.mDragWindowHandle.dispatchingTimeoutNanos = 5000000000L;
            this.mDragWindowHandle.visible = true;
            this.mDragWindowHandle.canReceiveKeys = false;
            this.mDragWindowHandle.hasFocus = true;
            this.mDragWindowHandle.hasWallpaper = false;
            this.mDragWindowHandle.paused = false;
            this.mDragWindowHandle.ownerPid = Process.myPid();
            this.mDragWindowHandle.ownerUid = Process.myUid();
            this.mDragWindowHandle.inputFeatures = 0;
            this.mDragWindowHandle.scaleFactor = 1.0f;
            this.mDragWindowHandle.touchableRegion.setEmpty();
            this.mDragWindowHandle.frameLeft = 0;
            this.mDragWindowHandle.frameTop = 0;
            this.mDragWindowHandle.frameRight = DragState.this.mDisplaySize.x;
            this.mDragWindowHandle.frameBottom = DragState.this.mDisplaySize.y;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "Pausing rotation during drag");
            }
            DragState.this.mService.pauseRotationLocked();
        }

        void tearDown() {
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
                Slog.d("WindowManager", "Resuming rotation after drag");
            }
            DragState.this.mService.resumeRotationLocked();
        }
    }

    DragState(WindowManagerService service, IBinder token, SurfaceControl surface, int flags, IBinder localWin) {
        this.mService = service;
        this.mToken = token;
        this.mSurfaceControl = surface;
        this.mFlags = flags;
        this.mLocalWin = localWin;
        this.mNotifiedWindows = new ArrayList();
    }

    void reset() {
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.destroy();
        }
        this.mSurfaceControl = null;
        this.mFlags = 0;
        this.mLocalWin = null;
        this.mToken = null;
        this.mData = null;
        this.mThumbOffsetY = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mThumbOffsetX = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mNotifiedWindows = null;
    }

    InputChannel getInputChannel() {
        return this.mInputInterceptor == null ? null : this.mInputInterceptor.mServerChannel;
    }

    InputWindowHandle getInputWindowHandle() {
        return this.mInputInterceptor == null ? null : this.mInputInterceptor.mDragWindowHandle;
    }

    void register(Display display) {
        display.getRealSize(this.mDisplaySize);
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "registering drag input channel");
        }
        if (this.mInputInterceptor != null) {
            Slog.e("WindowManager", "Duplicate register of drag input channel");
            return;
        }
        this.mInputInterceptor = new InputInterceptor(display);
        this.mService.mInputMonitor.updateInputWindowsLw(true);
    }

    void unregister() {
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "unregistering drag input channel");
        }
        if (this.mInputInterceptor == null) {
            Slog.e("WindowManager", "Unregister of nonexistent drag input channel");
            return;
        }
        this.mService.mH.obtainMessage(44, this.mInputInterceptor).sendToTarget();
        this.mInputInterceptor = null;
        this.mService.mInputMonitor.updateInputWindowsLw(true);
    }

    int getDragLayerLw() {
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(2016) * 10000) + 1000;
    }

    void broadcastDragStartedLw(float touchX, float touchY) {
        ClipDescription clipDescription = null;
        this.mCurrentX = touchX;
        this.mOriginalX = touchX;
        this.mCurrentY = touchY;
        this.mOriginalY = touchY;
        if (this.mData != null) {
            clipDescription = this.mData.getDescription();
        }
        this.mDataDescription = clipDescription;
        this.mNotifiedWindows.clear();
        this.mDragInProgress = true;
        this.mSourceUserId = UserHandle.getUserId(this.mUid);
        try {
            this.mCrossProfileCopyAllowed = ((IUserManager) ServiceManager.getService("user")).getUserRestrictions(this.mSourceUserId).getBoolean("no_cross_profile_copy_paste") ^ 1;
        } catch (RemoteException e) {
            Slog.e("WindowManager", "Remote Exception calling UserManager: " + e);
            this.mCrossProfileCopyAllowed = false;
        }
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "broadcasting DRAG_STARTED at (" + touchX + ", " + touchY + ")");
        }
        this.mDisplayContent.forAllWindows((Consumer) new -$Lambda$FvxFgi8YP28QafNQRN6x4H9YAdU(touchX, touchY, this), false);
    }

    /* renamed from: lambda$-com_android_server_wm_DragState_10561 */
    /* synthetic */ void m250lambda$-com_android_server_wm_DragState_10561(float touchX, float touchY, WindowState w) {
        sendDragStartedLw(w, touchX, touchY, this.mDataDescription);
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendDragStartedLw(WindowState newWin, float touchX, float touchY, ClipDescription desc) {
        if (this.mDragInProgress && isValidDropTarget(newWin)) {
            DragEvent event = obtainDragEvent(newWin, 1, touchX, touchY, null, desc, null, null, false);
            try {
                newWin.mClient.dispatchDragEvent(event);
                this.mNotifiedWindows.add(newWin);
                if (Process.myPid() != newWin.mSession.mPid) {
                    event.recycle();
                }
            } catch (RemoteException e) {
                Slog.w("WindowManager", "Unable to drag-start window " + newWin);
                if (Process.myPid() != newWin.mSession.mPid) {
                    event.recycle();
                }
            } catch (Throwable th) {
                if (Process.myPid() != newWin.mSession.mPid) {
                    event.recycle();
                }
                throw th;
            }
        }
    }

    private boolean isValidDropTarget(WindowState targetWin) {
        boolean z = true;
        if (targetWin == null || !targetWin.isPotentialDragTarget()) {
            return false;
        }
        if (((this.mFlags & 256) == 0 || (targetWindowSupportsGlobalDrag(targetWin) ^ 1) != 0) && this.mLocalWin != targetWin.mClient.asBinder()) {
            return false;
        }
        if (!(this.mCrossProfileCopyAllowed || this.mSourceUserId == UserHandle.getUserId(targetWin.getOwningUid()))) {
            z = false;
        }
        return z;
    }

    private boolean targetWindowSupportsGlobalDrag(WindowState targetWin) {
        if (targetWin.mAppToken == null || targetWin.mAppToken.mTargetSdk >= 24) {
            return true;
        }
        return false;
    }

    void sendDragStartedIfNeededLw(WindowState newWin) {
        if (this.mDragInProgress && !isWindowNotified(newWin)) {
            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                Slog.d("WindowManager", "need to send DRAG_STARTED to new window " + newWin);
            }
            sendDragStartedLw(newWin, this.mCurrentX, this.mCurrentY, this.mDataDescription);
        }
    }

    private boolean isWindowNotified(WindowState newWin) {
        for (WindowState ws : this.mNotifiedWindows) {
            if (ws == newWin) {
                return true;
            }
        }
        return false;
    }

    private void broadcastDragEndedLw() {
        int myPid = Process.myPid();
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "broadcasting DRAG_ENDED");
        }
        for (WindowState ws : this.mNotifiedWindows) {
            float x = OppoBrightUtils.MIN_LUX_LIMITI;
            float y = OppoBrightUtils.MIN_LUX_LIMITI;
            if (!this.mDragResult && ws.mSession.mPid == this.mPid) {
                x = this.mCurrentX;
                y = this.mCurrentY;
            }
            DragEvent evt = DragEvent.obtain(4, x, y, null, null, null, null, this.mDragResult);
            try {
                ws.mClient.dispatchDragEvent(evt);
            } catch (RemoteException e) {
                Slog.w("WindowManager", "Unable to drag-end window " + ws);
            }
            if (myPid != ws.mSession.mPid) {
                evt.recycle();
            }
        }
        this.mNotifiedWindows.clear();
        this.mDragInProgress = false;
    }

    void endDragLw() {
        if (this.mAnimation == null) {
            if (this.mDragResult) {
                cleanUpDragLw();
                return;
            }
            this.mAnimation = createReturnAnimationLocked();
            this.mService.scheduleAnimationLocked();
        }
    }

    void cancelDragLw() {
        if (this.mAnimation == null) {
            this.mAnimation = createCancelAnimationLocked();
            this.mService.scheduleAnimationLocked();
        }
    }

    private void cleanUpDragLw() {
        broadcastDragEndedLw();
        if (isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
            this.mService.restorePointerIconLocked(this.mDisplayContent, this.mCurrentX, this.mCurrentY);
        }
        unregister();
        reset();
        this.mService.mDragState = null;
    }

    void notifyMoveLw(float x, float y) {
        if (this.mAnimation == null) {
            this.mCurrentX = x;
            this.mCurrentY = y;
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION notifyMoveLw");
            }
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setPosition(x - this.mThumbOffsetX, y - this.mThumbOffsetY);
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i("WindowManager", "  DRAG " + this.mSurfaceControl + ": pos=(" + ((int) (x - this.mThumbOffsetX)) + "," + ((int) (y - this.mThumbOffsetY)) + ")");
                }
                this.mService.closeSurfaceTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION notifyMoveLw");
                }
                notifyLocationLw(x, y);
            } catch (Throwable th) {
                this.mService.closeSurfaceTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION notifyMoveLw");
                }
            }
        }
    }

    void notifyLocationLw(float x, float y) {
        WindowState touchedWin = this.mDisplayContent.getTouchableWinAtPointLocked(x, y);
        if (!(touchedWin == null || (isWindowNotified(touchedWin) ^ 1) == 0)) {
            touchedWin = null;
        }
        try {
            DragEvent evt;
            int myPid = Process.myPid();
            if (!(touchedWin == this.mTargetWindow || this.mTargetWindow == null)) {
                if (WindowManagerDebugConfig.DEBUG_DRAG) {
                    Slog.d("WindowManager", "sending DRAG_EXITED to " + this.mTargetWindow);
                }
                evt = obtainDragEvent(this.mTargetWindow, 6, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, null, null, null, null, false);
                this.mTargetWindow.mClient.dispatchDragEvent(evt);
                if (myPid != this.mTargetWindow.mSession.mPid) {
                    evt.recycle();
                }
            }
            if (touchedWin != null) {
                evt = obtainDragEvent(touchedWin, 2, x, y, null, null, null, null, false);
                touchedWin.mClient.dispatchDragEvent(evt);
                if (myPid != touchedWin.mSession.mPid) {
                    evt.recycle();
                }
            }
        } catch (RemoteException e) {
            Slog.w("WindowManager", "can't send drag notification to windows");
        }
        this.mTargetWindow = touchedWin;
    }

    boolean notifyDropLw(float x, float y) {
        if (this.mAnimation != null) {
            return false;
        }
        this.mCurrentX = x;
        this.mCurrentY = y;
        WindowState touchedWin = this.mDisplayContent.getTouchableWinAtPointLocked(x, y);
        if (isWindowNotified(touchedWin)) {
            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                Slog.d("WindowManager", "sending DROP to " + touchedWin);
            }
            int targetUserId = UserHandle.getUserId(touchedWin.getOwningUid());
            DragAndDropPermissionsHandler dragAndDropPermissionsHandler = null;
            if (!((this.mFlags & 256) == 0 || (this.mFlags & 3) == 0)) {
                dragAndDropPermissionsHandler = new DragAndDropPermissionsHandler(this.mData, this.mUid, touchedWin.getOwningPackage(), this.mFlags & 195, this.mSourceUserId, targetUserId);
            }
            if (this.mSourceUserId != targetUserId) {
                this.mData.fixUris(this.mSourceUserId);
            }
            int myPid = Process.myPid();
            IBinder token = touchedWin.mClient.asBinder();
            DragEvent evt = obtainDragEvent(touchedWin, 3, x, y, null, null, this.mData, dragAndDropPermissionsHandler, false);
            try {
                touchedWin.mClient.dispatchDragEvent(evt);
                this.mService.mH.removeMessages(21, token);
                this.mService.mH.sendMessageDelayed(this.mService.mH.obtainMessage(21, token), FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
                if (myPid != touchedWin.mSession.mPid) {
                    evt.recycle();
                }
                this.mToken = token;
                return false;
            } catch (RemoteException e) {
                Slog.w("WindowManager", "can't send drop notification to win " + touchedWin);
                if (myPid != touchedWin.mSession.mPid) {
                    evt.recycle();
                }
                return true;
            } catch (Throwable th) {
                if (myPid != touchedWin.mSession.mPid) {
                    evt.recycle();
                }
                throw th;
            }
        }
        this.mDragResult = false;
        return true;
    }

    private static DragEvent obtainDragEvent(WindowState win, int action, float x, float y, Object localState, ClipDescription description, ClipData data, IDragAndDropPermissions dragAndDropPermissions, boolean result) {
        return DragEvent.obtain(action, win.translateToWindowX(x), win.translateToWindowY(y), localState, description, data, dragAndDropPermissions, result);
    }

    boolean stepAnimationLocked(long currentTimeMs) {
        if (this.mAnimation == null) {
            return false;
        }
        this.mTransformation.clear();
        if (this.mAnimation.getTransformation(currentTimeMs, this.mTransformation)) {
            this.mTransformation.getMatrix().postTranslate(this.mCurrentX - this.mThumbOffsetX, this.mCurrentY - this.mThumbOffsetY);
            float[] tmpFloats = this.mService.mTmpFloats;
            this.mTransformation.getMatrix().getValues(tmpFloats);
            this.mSurfaceControl.setPosition(tmpFloats[2], tmpFloats[5]);
            this.mSurfaceControl.setAlpha(this.mTransformation.getAlpha());
            this.mSurfaceControl.setMatrix(tmpFloats[0], tmpFloats[3], tmpFloats[1], tmpFloats[4]);
            return true;
        }
        cleanUpDragLw();
        return false;
    }

    private Animation createReturnAnimationLocked() {
        AnimationSet set = new AnimationSet(false);
        float translateX = this.mOriginalX - this.mCurrentX;
        float translateY = this.mOriginalY - this.mCurrentY;
        set.addAnimation(new TranslateAnimation(OppoBrightUtils.MIN_LUX_LIMITI, translateX, OppoBrightUtils.MIN_LUX_LIMITI, translateY));
        set.addAnimation(new AlphaAnimation(this.mOriginalAlpha, this.mOriginalAlpha / 2.0f));
        set.setDuration(MIN_ANIMATION_DURATION_MS + ((long) ((Math.sqrt((double) ((translateX * translateX) + (translateY * translateY))) / Math.sqrt((double) ((this.mDisplaySize.x * this.mDisplaySize.x) + (this.mDisplaySize.y * this.mDisplaySize.y)))) * 180.0d)));
        set.setInterpolator(this.mCubicEaseOutInterpolator);
        set.initialize(0, 0, 0, 0);
        set.start();
        return set;
    }

    private Animation createCancelAnimationLocked() {
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(new ScaleAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI, 1.0f, OppoBrightUtils.MIN_LUX_LIMITI, this.mThumbOffsetX, this.mThumbOffsetY));
        set.addAnimation(new AlphaAnimation(this.mOriginalAlpha, OppoBrightUtils.MIN_LUX_LIMITI));
        set.setDuration(MIN_ANIMATION_DURATION_MS);
        set.setInterpolator(this.mCubicEaseOutInterpolator);
        set.initialize(0, 0, 0, 0);
        set.start();
        return set;
    }

    private boolean isFromSource(int source) {
        return (this.mTouchSource & source) == source;
    }

    void overridePointerIconLw(int touchSource) {
        this.mTouchSource = touchSource;
        if (isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
            InputManager.getInstance().setPointerIconType(1021);
        }
    }
}
