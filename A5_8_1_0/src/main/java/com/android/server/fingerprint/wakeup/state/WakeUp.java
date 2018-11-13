package com.android.server.fingerprint.wakeup.state;

import android.content.Context;
import android.os.Looper;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.UnlockController.OrderedEvents;
import com.android.server.policy.PhoneWindowManager;

public class WakeUp extends UnlockState {
    private String TAG = "FingerprintService.WakeUp";

    public WakeUp(Context c, IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    public synchronized void updateState() {
        boolean z = false;
        synchronized (this) {
            if ((this.mOrderedEvent == OrderedEvents.TOUCHDOWN || this.mOrderedEvent == OrderedEvents.HOMEKEY_TOUCHDOWN) && this.mIsScreenOff && this.mAuthenticated == -1) {
                z = true;
            }
            this.mState = z;
            if (!this.mIsScreenOff) {
                resetWakeupMode("screenon");
            }
            boolean exitWakeUp = (isWakeUpEnabled() || (this.mIsExitWakeUpMode ^ 1) == 0) ? false : this.mOrderedEvent == OrderedEvents.TOUCHDOWN_TOUCHUP || this.mOrderedEvent == OrderedEvents.TOUCHUP;
            if (exitWakeUp) {
                exitWakeUpMode();
                LogUtil.d(this.TAG, "exitWakeUpMode when the third touch mWakeupContinuouslyCount = " + this.mWakeupContinuouslyCount);
            }
            notifyStateChanged(this.mState, this.mAuthenticatedInfo);
        }
    }

    public void finishAction(AuthenticatedInfo authenticatedInfo) {
        LogUtil.d(this.TAG, "finishAction, mIsExitWakeUpMode = " + this.mIsExitWakeUpMode);
        if (this.mIsExitWakeUpMode) {
            LogUtil.d(this.TAG, " had already in exitWakeUpMode,just return");
        } else if (isWakeUpEnabled() || (this.mIsExitWakeUpMode ^ 1) == 0) {
            getFPMS().blockScreenOn("android.service.fingerprint:WAKEUP");
            updateScreenOffWakeUpTime();
        } else {
            LogUtil.d(this.TAG, "exitWakeUpMode, skip wakeUp by fingerprint");
        }
    }

    public void resetWakeupMode(String reason) {
        LogUtil.d(this.TAG, "resetWakeupMode mIsExitWakeUpMode = " + this.mIsExitWakeUpMode);
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
        boolean z = false;
        if (this.mIsExitWakeUpMode) {
            LogUtil.d(this.TAG, "enterWakeUpMode");
            this.mIsExitWakeUpMode = false;
            if (this.mIUnLocker.continueIdentify() == 0) {
                z = true;
            }
            return z;
        }
        LogUtil.d(this.TAG, "not in fusing ,reject enterWakeUpMode");
        return false;
    }

    public boolean exitWakeUpMode() {
        boolean z = true;
        if (this.mIsExitWakeUpMode) {
            LogUtil.d(this.TAG, "in fusing ,reject exitWakeUpMode");
            return false;
        }
        LogUtil.d(this.TAG, "exitWakeUpMode");
        this.mIsExitWakeUpMode = true;
        if (this.mIUnLocker.pauseIdentify() != 0) {
            z = false;
        }
        return z;
    }
}
