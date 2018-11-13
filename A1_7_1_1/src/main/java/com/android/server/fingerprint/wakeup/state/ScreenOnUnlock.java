package com.android.server.fingerprint.wakeup.state;

import android.content.Context;
import android.os.Looper;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.UnlockController.ORDERED_EVENTS;

public class ScreenOnUnlock extends UnlockState {
    private String TAG = "FingerprintService.ScreenOnUnlock";

    public ScreenOnUnlock(Context c, IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    /* JADX WARNING: Missing block: B:14:0x0021, code:
            if (r3.mOrderedEvent == com.android.server.fingerprint.wakeup.UnlockController.ORDERED_EVENTS.TOUCHDOWN_HOMEKEY) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void updateState() {
        boolean z = false;
        synchronized (this) {
            if (!(this.mOrderedEvent == ORDERED_EVENTS.TOUCHDOWN || this.mOrderedEvent == ORDERED_EVENTS.HOMEKEY_TOUCHDOWN)) {
            }
            if (!(this.mIsScreenOff || this.mAuthenticated == -1)) {
                z = true;
            }
            this.mState = z;
            notifyStateChanged(this.mState, this.mAuthenticatedInfo);
        }
    }

    public void finishAction(AuthenticatedInfo authenticatedInfo) {
        LogUtil.d(this.TAG, "finishAction");
        if (authenticatedInfo != null) {
            this.mIUnLocker.dispatchScreenOnAuthenticatedEvent(authenticatedInfo.fingerId, authenticatedInfo.groupId);
        }
    }
}
