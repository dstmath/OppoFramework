package com.android.server.wm;

import android.os.Debug;
import android.os.IBinder;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.input.InputManagerService;
import com.mediatek.server.wm.WmsExt;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class InputManagerCallback implements InputManagerService.WindowManagerCallbacks {
    private boolean mInputDevicesReady;
    private final Object mInputDevicesReadyMonitor = new Object();
    private boolean mInputDispatchEnabled;
    private boolean mInputDispatchFrozen;
    private String mInputFreezeReason = null;
    private final WindowManagerService mService;

    public InputManagerCallback(WindowManagerService service) {
        this.mService = service;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState
     arg types: [?[OBJECT, ARRAY], android.os.IBinder, int]
     candidates:
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.view.IWindow, boolean):com.android.server.wm.WindowState
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState */
    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public void notifyInputChannelBroken(IBinder token) {
        if (token != null) {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = this.mService.windowForClientLocked((Session) null, token, false);
                    if (windowState != null) {
                        Slog.i(WmsExt.TAG, "WINDOW DIED " + windowState);
                        windowState.removeIfPossible();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState
     arg types: [?[OBJECT, ARRAY], android.os.IBinder, int]
     candidates:
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.view.IWindow, boolean):com.android.server.wm.WindowState
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState */
    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public long notifyANR(IBinder token, String reason) {
        AppWindowToken appWindowToken = null;
        WindowState windowState = null;
        boolean aboveSystem = false;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                boolean z = false;
                if (!(token == null || (windowState = this.mService.windowForClientLocked((Session) null, token, false)) == null)) {
                    appWindowToken = windowState.mAppToken;
                }
                if (windowState != null) {
                    Slog.i(WmsExt.TAG, "Input event dispatching timed out sending to " + ((Object) windowState.mAttrs.getTitle()) + ".  Reason: " + reason);
                    if (windowState.mBaseLayer > this.mService.mPolicy.getWindowLayerFromTypeLw(2038, windowState.mOwnerCanAddInternalSystemWindow)) {
                        z = true;
                    }
                    aboveSystem = z;
                } else if (appWindowToken != null) {
                    Slog.i(WmsExt.TAG, "Input event dispatching timed out sending to application " + appWindowToken.stringName + ".  Reason: " + reason);
                } else {
                    Slog.i(WmsExt.TAG, "Input event dispatching timed out .  Reason: " + reason);
                }
                this.mService.saveANRStateLocked(appWindowToken, windowState, reason);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        this.mService.mAtmInternal.saveANRState(reason);
        if (appWindowToken != null && appWindowToken.appToken != null) {
            if (!appWindowToken.keyDispatchingTimedOut(reason, windowState != null ? windowState.mSession.mPid : -1)) {
                return appWindowToken.mInputDispatchingTimeoutNanos;
            }
        } else if (windowState != null) {
            long timeout = this.mService.mAmInternal.inputDispatchingTimedOut(windowState.mSession.mPid, aboveSystem, reason);
            if (timeout >= 0) {
                return 1000000 * timeout;
            }
        }
        return 0;
    }

    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public void notifyConfigurationChanged() {
        this.mService.sendNewConfiguration(0);
        synchronized (this.mInputDevicesReadyMonitor) {
            if (!this.mInputDevicesReady) {
                this.mInputDevicesReady = true;
                this.mInputDevicesReadyMonitor.notifyAll();
            }
        }
    }

    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
        this.mService.mPolicy.notifyLidSwitchChanged(whenNanos, lidOpen);
    }

    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public void notifyCameraLensCoverSwitchChanged(long whenNanos, boolean lensCovered) {
        this.mService.mPolicy.notifyCameraLensCoverSwitchChanged(whenNanos, lensCovered);
    }

    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.interceptKeyBeforeQueueing(event, policyFlags);
    }

    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public int interceptMotionBeforeQueueingNonInteractive(int displayId, long whenNanos, int policyFlags) {
        return this.mService.mPolicy.interceptMotionBeforeQueueingNonInteractive(displayId, whenNanos, policyFlags);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState
     arg types: [?[OBJECT, ARRAY], android.os.IBinder, int]
     candidates:
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.view.IWindow, boolean):com.android.server.wm.WindowState
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState */
    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public long interceptKeyBeforeDispatching(IBinder focus, KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.interceptKeyBeforeDispatching(this.mService.windowForClientLocked((Session) null, focus, false), event, policyFlags);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState
     arg types: [?[OBJECT, ARRAY], android.os.IBinder, int]
     candidates:
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.view.IWindow, boolean):com.android.server.wm.WindowState
      com.android.server.wm.WindowManagerService.windowForClientLocked(com.android.server.wm.Session, android.os.IBinder, boolean):com.android.server.wm.WindowState */
    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public KeyEvent dispatchUnhandledKey(IBinder focus, KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.dispatchUnhandledKey(this.mService.windowForClientLocked((Session) null, focus, false), event, policyFlags);
    }

    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public int getPointerLayer() {
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 1000;
    }

    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public int getPointerDisplayId() {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!this.mService.mForceDesktopModeOnExternalDisplays) {
                    return 0;
                }
                int firstExternalDisplayId = 0;
                for (int i = this.mService.mRoot.mChildren.size() - 1; i >= 0; i--) {
                    DisplayContent displayContent = (DisplayContent) this.mService.mRoot.mChildren.get(i);
                    if (displayContent.getWindowingMode() == 5) {
                        int displayId = displayContent.getDisplayId();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return displayId;
                    }
                    if (firstExternalDisplayId == 0 && displayContent.getDisplayId() != 0) {
                        firstExternalDisplayId = displayContent.getDisplayId();
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return firstExternalDisplayId;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    @Override // com.android.server.input.InputManagerService.WindowManagerCallbacks
    public void onPointerDownOutsideFocus(IBinder touchedToken) {
        this.mService.mH.obtainMessage(62, touchedToken).sendToTarget();
    }

    public boolean waitForInputDevicesReady(long timeoutMillis) {
        boolean z;
        synchronized (this.mInputDevicesReadyMonitor) {
            if (!this.mInputDevicesReady) {
                try {
                    this.mInputDevicesReadyMonitor.wait(timeoutMillis);
                } catch (InterruptedException e) {
                }
            }
            z = this.mInputDevicesReady;
        }
        return z;
    }

    public void freezeInputDispatchingLw() {
        if (!this.mInputDispatchFrozen) {
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v(WmsExt.TAG, "Freezing input dispatching");
            }
            this.mInputDispatchFrozen = true;
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                this.mInputFreezeReason = Debug.getCallers(6);
            }
            updateInputDispatchModeLw();
        }
    }

    public void thawInputDispatchingLw() {
        if (this.mInputDispatchFrozen) {
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v(WmsExt.TAG, "Thawing input dispatching");
            }
            this.mInputDispatchFrozen = false;
            this.mInputFreezeReason = null;
            updateInputDispatchModeLw();
        }
    }

    public void setEventDispatchingLw(boolean enabled) {
        if (this.mInputDispatchEnabled != enabled) {
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v(WmsExt.TAG, "Setting event dispatching to " + enabled);
            }
            this.mInputDispatchEnabled = enabled;
            updateInputDispatchModeLw();
        }
    }

    private void updateInputDispatchModeLw() {
        this.mService.mInputManager.setInputDispatchMode(this.mInputDispatchEnabled, this.mInputDispatchFrozen);
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        if (this.mInputFreezeReason != null) {
            pw.println(prefix + "mInputFreezeReason=" + this.mInputFreezeReason);
        }
    }
}
