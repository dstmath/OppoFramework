package com.android.server.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.fingerprint.EngineeringInfo;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import java.util.ArrayList;
import vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;

public abstract class EngineeringClient extends ClientMonitor {
    public static String TAG = "FingerprintService.EngineeringClient";
    private IBiometricsFingerprint mFingerprintDeamon = null;
    private int mtype;

    public abstract void sendEngineeringInfoUpdated(EngineeringInfo engineeringInfo);

    public EngineeringClient(Context context, Constants constants, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, boolean restricted, String owner, int cookie, int type, IBiometricsFingerprint fingerprintDeamon) {
        super(context, constants, daemon, halDeviceId, token, listener, targetUserId, groupId, restricted, owner, cookie);
        this.mtype = type;
        this.mFingerprintDeamon = fingerprintDeamon;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public void notifyUserActivity() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.LoggableMonitor
    public int statsAction() {
        return 6;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int start() {
        try {
            if (this.mFingerprintDeamon == null) {
                LogUtil.w(TAG, "startGetEngineeringInfo: no fingerprintd!");
                return -1;
            }
            String str = TAG;
            LogUtil.w(str, "start  Engineering test package:" + getOwnerString());
            int result = this.mFingerprintDeamon.getEngineeringInfo(this.mtype);
            if (result == 0) {
                return 0;
            }
            String str2 = TAG;
            LogUtil.w(str2, "startGetEngineeringInfo failed, result=" + result);
            return result;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startGetEngineeringInfo failed", e);
            return 0;
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int stop(boolean initiatedByClient) {
        if (this.mAlreadyCancelled) {
            LogUtil.w(TAG, "stopGetEngineeringInfo already cancelled!");
            return 0;
        }
        try {
            int result = getDaemonWrapper().cancel();
            if (result != 0) {
                String str = TAG;
                LogUtil.w(str, "stopGetEngineeringInfo failed, result=" + result);
                return result;
            }
            if (initiatedByClient) {
                onError(getHalDeviceId(), 5, 0);
            }
            this.mAlreadyCancelled = true;
            return 0;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "stopGetEngineeringInfo failed", e);
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
