package com.android.server.fingerprint.wakeup.state;

import android.content.Context;
import android.os.Looper;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.UnlockController.ORDERED_EVENTS;

public class ScreenOn extends UnlockState {
    private String TAG = "FingerprintService.ScreenOn";

    public ScreenOn(Context c, IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    public synchronized void updateState() {
        this.mState = this.mOrderedEvent == ORDERED_EVENTS.HOMEKEY ? this.mIsScreenOff : false;
        notifyStateChanged(this.mState, this.mAuthenticatedInfo);
    }

    public void finishAction(AuthenticatedInfo authenticatedInfo) {
        LogUtil.d(this.TAG, "finishAction");
        getFPMS().wakeupNormal();
    }
}
