package com.android.server.biometrics.fingerprint.wakeup.state;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.Looper;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.power.FingerprintPowerManager;
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.biometrics.fingerprint.wakeup.UnlockController;
import java.util.ArrayList;

public abstract class UnlockState {
    private String TAG = "FingerprintService.UnlockState";
    public long WAKEUP_SPEEDTIME_LIMIT_TO_FUSEING = 3;
    public Object mAuthInfoSyncLock = new Object();
    public int mAuthenticated = -1;
    public Context mContext;
    public Fingerprint mFingerprintInfo = null;
    private ExHandler mHandler;
    public FingerprintService.IUnLocker mIUnLocker;
    public boolean mIsExitWakeUpMode = false;
    public boolean mIsScreenOff;
    public UnlockController.OrderedEvents mOrderedEvent = UnlockController.OrderedEvents.TOUCHUP;
    public boolean mState;
    public long mWakeupContinuouslyCount = 0;
    public ArrayList<Byte> tokenByte = null;

    public abstract void finishAction(Fingerprint fingerprint, ArrayList<Byte> arrayList);

    public abstract void updateState();

    public UnlockState(Context context, FingerprintService.IUnLocker unLocker, Looper looper) {
        this.mContext = context;
        this.mIUnLocker = unLocker;
        this.mHandler = new ExHandler(looper);
    }

    public void dispatchOrderedEvent(UnlockController.OrderedEvents event) {
        this.mOrderedEvent = event;
        updateState();
    }

    public void dispatchAuthenticated(Fingerprint mfingerInfo, ArrayList<Byte> token) {
        synchronized (this.mAuthInfoSyncLock) {
            this.mAuthenticated = mfingerInfo.getBiometricId();
            this.mFingerprintInfo = mfingerInfo;
            this.tokenByte = token;
            updateState();
        }
    }

    public void dispatchScreenOff(boolean isScreenOff) {
        this.mIsScreenOff = isScreenOff;
        updateState();
    }

    public void resetAuthenticated() {
        LogUtil.d(this.TAG, "resetAuthenticated");
        synchronized (this.mAuthInfoSyncLock) {
            this.mAuthenticated = -1;
            this.mFingerprintInfo = null;
            this.tokenByte = null;
        }
    }

    public void notifyStateChanged(boolean state, final Fingerprint mfingerInfo, final ArrayList<Byte> token) {
        String str = this.TAG;
        LogUtil.w(str, "mState = " + this.mState + ", isScreenOFF = " + getFPMS().isScreenOFF() + ", mIsScreenOff = " + this.mIsScreenOff + ", mAuthenticated = " + this.mAuthenticated + ", mOrderedEvent = " + this.mOrderedEvent);
        if (state) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.wakeup.state.UnlockState.AnonymousClass1 */

                public void run() {
                    UnlockState.this.finishAction(mfingerInfo, token);
                }
            });
        }
    }

    public FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }
}
