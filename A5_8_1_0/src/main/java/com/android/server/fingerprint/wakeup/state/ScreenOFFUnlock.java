package com.android.server.fingerprint.wakeup.state;

import android.content.Context;
import android.os.Looper;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.UnlockController.OrderedEvents;

public class ScreenOFFUnlock extends UnlockState {
    private String TAG = "FingerprintService.ScreenOFFUnlock";

    public ScreenOFFUnlock(Context c, IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    public synchronized void updateState() {
        boolean z = false;
        synchronized (this) {
            if ((this.mOrderedEvent == OrderedEvents.TOUCHDOWN_HOMEKEY || this.mOrderedEvent == OrderedEvents.HOMEKEY_TOUCHDOWN) && this.mIsScreenOff && this.mAuthenticated != -1) {
                z = true;
            }
            this.mState = z;
            notifyStateChanged(this.mState, this.mAuthenticatedInfo);
        }
    }

    public void finishAction(AuthenticatedInfo authenticatedInfo) {
        LogUtil.d(this.TAG, "finishAction");
        if (authenticatedInfo != null) {
            this.mIUnLocker.dispatchScreenOffAuthenticatedEvent(authenticatedInfo.fingerId, authenticatedInfo.groupId);
        }
    }
}
