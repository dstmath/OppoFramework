package com.android.server.biometrics.fingerprint.wakeup.state;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.Looper;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.biometrics.fingerprint.wakeup.UnlockController;
import java.util.ArrayList;

public class ScreenOn extends UnlockState {
    private String TAG = "FingerprintService.ScreenOn";

    public ScreenOn(Context c, FingerprintService.IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.state.UnlockState
    public synchronized void updateState() {
        this.mState = this.mOrderedEvent == UnlockController.OrderedEvents.HOMEKEY && this.mIsScreenOff;
        notifyStateChanged(this.mState, this.mFingerprintInfo, this.tokenByte);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.state.UnlockState
    public void finishAction(Fingerprint mFingerprintInfo, ArrayList<Byte> arrayList) {
        LogUtil.d(this.TAG, "finishAction");
        getFPMS().wakeupNormal();
    }
}
