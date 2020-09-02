package com.android.server.wm;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Slog;
import android.view.IWindow;
import android.view.IWindowManager;
import android.view.WindowManager;
import com.android.server.ColorLocalServices;
import com.android.server.LocalServices;

public abstract class OppoBaseWindowManagerService extends IWindowManager.Stub {
    protected static final String COLOROS_FLOAT = "com.coloros.floatassistant";
    static final int COLOR_WMS_MAIN_HANLDER = 1;
    private static final String TAG = "WindowManager";
    protected static final String TICKER_PANEL = "TickerPanel";
    IColorFreeformManager mColorFreeformManager = null;
    IColorWindowManagerServiceEx mColorWmsEx = null;
    public IColorWindowManagerServiceInner mColorWmsInner = null;
    Runnable mDockedForDrawnCallback;
    protected FingerprintManager mFingerprintManager;
    protected int mFreezingStackId;
    protected boolean mHasWindowFreezed;
    protected boolean mHasWindowFreezing;
    protected boolean mIsRotationLockForBootAnimation = false;
    protected ScreenRotationAnimation mOppoScreenRotationAnimation;
    IPswWindowManagerServiceEx mPswWmsEx = null;
    protected boolean mStartFreeform = false;

    /* access modifiers changed from: package-private */
    public abstract boolean killNotDrawnAppsWhenFrozen();

    /* access modifiers changed from: protected */
    public void onOppoStart() {
        warn("onStart");
    }

    /* access modifiers changed from: protected */
    public void onOppoSystemReady() {
        warn("onOppoSystemReady");
    }

    /* access modifiers changed from: protected */
    public void handleOppoMessage(Message msg, int whichHandler) {
        warn("handleOppoMessage");
    }

    /* access modifiers changed from: protected */
    public TaskPositioner createTaskPositioner(WindowManagerService service) {
        return new TaskPositioner(service);
    }

    private final void warn(String methodName) {
        Slog.w("WindowManager", methodName + " not implemented");
    }

    /* access modifiers changed from: protected */
    public IColorFreeformManager getColorFreeformManager() {
        if (this.mColorFreeformManager == null) {
            this.mColorFreeformManager = (IColorFreeformManager) ColorLocalServices.getService(IColorFreeformManager.class);
        }
        return this.mColorFreeformManager;
    }

    public void resgisterOppoWindowManagerInternal() {
        LocalServices.addService(OppoWindowManagerInternal.class, new OppoWindowManagerInternalImpl());
    }

    private class OppoWindowManagerInternalImpl extends OppoWindowManagerInternal {
        private OppoWindowManagerInternalImpl() {
        }

        @Override // com.android.server.wm.OppoWindowManagerInternal
        public void notifyWindowStateChange(Bundle options) {
            if (OppoBaseWindowManagerService.this.mColorWmsEx != null) {
                OppoBaseWindowManagerService.this.mColorWmsEx.notifyWindowStateChange(options);
            }
        }
    }

    /* access modifiers changed from: protected */
    public WindowState createWindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, WindowManager.LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow) {
        return new WindowState(service, s, c, token, parentWindow, appOp, seq, a, viewVisibility, ownerId, ownerCanAddInternalSystemWindow);
    }

    /* access modifiers changed from: protected */
    public TaskStack createTaskStack(WindowManagerService service, int stackId, ActivityStack stack) {
        return new TaskStack(service, stackId, stack);
    }

    public IColorDisplayPolicyEx getColorDisplayPolicyEx(DisplayPolicy displayPolicy) {
        IColorWindowManagerServiceEx iColorWindowManagerServiceEx = this.mColorWmsEx;
        if (iColorWindowManagerServiceEx != null) {
            return iColorWindowManagerServiceEx.getColorDisplayPolicyEx(displayPolicy);
        }
        return null;
    }

    public IColorWindowManagerServiceInner createColorWindowManagerServiceInner() {
        return null;
    }

    public void handleUiModeChanged() {
    }

    public void setRotationLockForBootAnimation(boolean isLock) {
        this.mIsRotationLockForBootAnimation = isLock;
    }

    /* access modifiers changed from: package-private */
    public boolean isRotationLockForBootAnimation() {
        return this.mIsRotationLockForBootAnimation;
    }

    /* access modifiers changed from: protected */
    public FingerprintManager getFingerprintOnSplitscreen(Context context, Task task) {
        DisplayContent display = task.getDisplayContent();
        if (display == null || !display.hasSplitScreenPrimaryStack()) {
            return null;
        }
        if (this.mFingerprintManager == null) {
            this.mFingerprintManager = (FingerprintManager) context.getSystemService(FingerprintManager.class);
        }
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager;
        }
        return null;
    }

    public void updateAppOpsState(String packageName, Boolean state) {
    }
}
