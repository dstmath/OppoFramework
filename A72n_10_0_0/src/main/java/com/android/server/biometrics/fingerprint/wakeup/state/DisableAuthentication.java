package com.android.server.biometrics.fingerprint.wakeup.state;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.Looper;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.biometrics.fingerprint.wakeup.UnlockController;
import java.util.ArrayList;

public class DisableAuthentication extends UnlockState {
    private String TAG = "FingerprintService.DisableAuthentication";

    public DisableAuthentication(Context c, FingerprintService.IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.state.UnlockState
    public synchronized void updateState() {
        boolean state3 = false;
        this.mState = (this.mOrderedEvent == UnlockController.OrderedEvents.TOUCHDOWN_TOUCHUP && this.mIsScreenOff && this.mAuthenticated != -1 && this.mAuthenticated != 0) || ((this.mOrderedEvent == UnlockController.OrderedEvents.TOUCHDOWN_POWERKEY || this.mOrderedEvent == UnlockController.OrderedEvents.POWERKEY_TOUCHDOWN) && this.mIsScreenOff && this.mAuthenticated != -1) || ((this.mOrderedEvent == UnlockController.OrderedEvents.POWERKEY_TOUCHDOWN && !this.mIsScreenOff && this.mAuthenticated != -1) || (this.mOrderedEvent == UnlockController.OrderedEvents.TOUCHDOWN_POWERKEY && !this.mIsScreenOff && this.mAuthenticated != -1));
        boolean state2 = this.mOrderedEvent == UnlockController.OrderedEvents.POWERKEY_TOUCHDOWN && !this.mIsScreenOff && this.mAuthenticated != -1;
        if (this.mOrderedEvent == UnlockController.OrderedEvents.TOUCHDOWN_POWERKEY && !this.mIsScreenOff && this.mAuthenticated != -1) {
            state3 = true;
        }
        if (state2) {
            LogUtil.d(this.TAG, "powerkey screenon delay 150");
        }
        if (state3) {
            LogUtil.d(this.TAG, "powerkey screenon delay 0");
        }
        notifyStateChanged(this.mState, this.mFingerprintInfo, this.tokenByte);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.state.UnlockState
    public void finishAction(Fingerprint mFingerprintInfo, ArrayList<Byte> token) {
        this.mIUnLocker.reauthentication(mFingerprintInfo, token);
    }
}
