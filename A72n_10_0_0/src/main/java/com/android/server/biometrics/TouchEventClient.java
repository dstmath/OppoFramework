package com.android.server.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.fingerprint.touchmode.TouchEventMonitorMode;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import java.util.ArrayList;
import vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;

public abstract class TouchEventClient extends ClientMonitor {
    public static String TAG = "FingerprintService.TouchEventClient";
    private boolean isClientKeyguard;
    private IBiometricsFingerprint mFingerprintDeamon = null;
    private TouchEventMonitorMode mTouchEventMonitorMode = null;

    public abstract void sendMonitorEventTriggered(int i, String str);

    public abstract boolean sendTouchDownEvent();

    public abstract boolean sendTouchUpEvent();

    public TouchEventClient(Context context, Constants constants, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, boolean restricted, String owner, int cookie, boolean isKeyguard, TouchEventMonitorMode touchEventMonitorMode, IBiometricsFingerprint fingerprintDeamon) {
        super(context, constants, daemon, halDeviceId, token, listener, targetUserId, groupId, restricted, owner, cookie);
        this.mTouchEventMonitorMode = touchEventMonitorMode;
        this.isClientKeyguard = isKeyguard;
        this.mFingerprintDeamon = fingerprintDeamon;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public void notifyUserActivity() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.LoggableMonitor
    public int statsAction() {
        return 5;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int start() {
        try {
            if (this.mFingerprintDeamon == null) {
                LogUtil.w(TAG, "startSetTouchEventListener: no fingerprintd!");
                return -1;
            }
            int result = this.mFingerprintDeamon.setTouchEventListener();
            if (result != 0) {
                String str = TAG;
                LogUtil.w(str, "startSetTouchEventListener failed, result=" + result);
                onError(getHalDeviceId(), 1, 0);
                return result;
            }
            if (this.isClientKeyguard && this.mTouchEventMonitorMode != null) {
                this.mTouchEventMonitorMode.startTouchMonitor();
            }
            return 0;
        } catch (RemoteException e) {
            String str2 = TAG;
            LogUtil.w(str2, "startSetTouchEventListener failed, e=" + e);
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int stop(boolean initiatedByClient) {
        TouchEventMonitorMode touchEventMonitorMode;
        if (this.mAlreadyCancelled) {
            LogUtil.w(TAG, "stopTouchEventListener already cancelled!");
            return 0;
        }
        if (this.isClientKeyguard && (touchEventMonitorMode = this.mTouchEventMonitorMode) != null) {
            touchEventMonitorMode.stopTouchMonitor();
        }
        try {
            int result = getDaemonWrapper().cancel();
            if (result != 0) {
                String str = TAG;
                LogUtil.w(str, "stopTouchEventListener failed, result=" + result);
                return result;
            }
            if (initiatedByClient) {
                onError(getHalDeviceId(), 5, 0);
            }
            this.mAlreadyCancelled = true;
            return 0;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "stopTouchEventListener failed", e);
            return 3;
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnumerationResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        LogUtil.w(TAG, "onEnumerationResult() called for TouchEventMode!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean authenticated, ArrayList<Byte> arrayList) {
        LogUtil.w(TAG, "onAuthenticated() called for  TouchEventMode!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnrollResult(BiometricAuthenticator.Identifier identifier, int rem) {
        LogUtil.w(TAG, "onEnrollResult() called for TouchEventMode!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) {
        LogUtil.w(TAG, "onRemoved() called for TouchEventMode!");
        return true;
    }
}
