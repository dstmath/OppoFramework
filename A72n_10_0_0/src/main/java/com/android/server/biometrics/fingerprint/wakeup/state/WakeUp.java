package com.android.server.biometrics.fingerprint.wakeup.state;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.Looper;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.biometrics.fingerprint.wakeup.UnlockController;
import com.android.server.policy.PhoneWindowManager;
import java.util.ArrayList;

public class WakeUp extends UnlockState {
    private String TAG = "FingerprintService.WakeUp";

    public WakeUp(Context c, FingerprintService.IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.state.UnlockState
    public synchronized void updateState() {
        boolean exitWakeUp = true;
        this.mState = (this.mOrderedEvent == UnlockController.OrderedEvents.TOUCHDOWN || this.mOrderedEvent == UnlockController.OrderedEvents.HOMEKEY_TOUCHDOWN) && this.mIsScreenOff && this.mAuthenticated == -1;
        if (!this.mIsScreenOff) {
            resetWakeupMode("screenon");
        }
        if (isWakeUpEnabled() || this.mIsExitWakeUpMode || !(this.mOrderedEvent == UnlockController.OrderedEvents.TOUCHDOWN_TOUCHUP || this.mOrderedEvent == UnlockController.OrderedEvents.TOUCHUP)) {
            exitWakeUp = false;
        }
        if (exitWakeUp) {
            exitWakeUpMode();
            LogUtil.d(this.TAG, "exitWakeUpMode when the third touch mWakeupContinuouslyCount = " + this.mWakeupContinuouslyCount);
        }
        notifyStateChanged(this.mState, this.mFingerprintInfo, this.tokenByte);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.state.UnlockState
    public void finishAction(Fingerprint mFingerprintInfo, ArrayList<Byte> arrayList) {
        String str = this.TAG;
        LogUtil.d(str, "finishAction, mIsExitWakeUpMode = " + this.mIsExitWakeUpMode);
        if (this.mIsExitWakeUpMode) {
            LogUtil.d(this.TAG, " had already in exitWakeUpMode,just return");
        } else if (isWakeUpEnabled() || this.mIsExitWakeUpMode) {
            getFPMS().blockScreenOn("android.service.fingerprint:WAKEUP");
            updateScreenOffWakeUpTime();
        } else {
            LogUtil.d(this.TAG, "exitWakeUpMode, skip wakeUp by fingerprint");
        }
    }

    public void resetWakeupMode(String reason) {
        String str = this.TAG;
        LogUtil.d(str, "resetWakeupMode mIsExitWakeUpMode = " + this.mIsExitWakeUpMode);
        this.mWakeupContinuouslyCount = 0;
        if (this.mIsExitWakeUpMode) {
            enterWakeUpMode();
            if (PhoneWindowManager.SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                getFPMS().blockScreenOn("android.service.fingerprint:WAKEUP");
            }
        }
    }

    public void updateScreenOffWakeUpTime() {
        this.mWakeupContinuouslyCount++;
        LogUtil.d(this.TAG, "updateScreenOffWakeUpTime, Count = " + this.mWakeupContinuouslyCount);
    }

    public boolean isWakeUpEnabled() {
        if (this.mWakeupContinuouslyCount >= this.WAKEUP_SPEEDTIME_LIMIT_TO_FUSEING) {
            return false;
        }
        return true;
    }

    public boolean enterWakeUpMode() {
        if (!this.mIsExitWakeUpMode) {
            LogUtil.d(this.TAG, "not in fusing ,reject enterWakeUpMode");
            return false;
        }
        LogUtil.d(this.TAG, "enterWakeUpMode");
        this.mIsExitWakeUpMode = false;
        if (this.mIUnLocker.continueIdentify() == 0) {
            return true;
        }
        return false;
    }

    public boolean exitWakeUpMode() {
        if (this.mIsExitWakeUpMode) {
            LogUtil.d(this.TAG, "in fusing ,reject exitWakeUpMode");
            return false;
        }
        LogUtil.d(this.TAG, "exitWakeUpMode");
        this.mIsExitWakeUpMode = true;
        if (this.mIUnLocker.pauseIdentify() == 0) {
            return true;
        }
        return false;
    }
}
