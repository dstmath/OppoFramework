package com.android.server.fingerprint.wakeup.state;

import android.content.Context;
import android.os.Looper;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.UnlockController.ORDERED_EVENTS;

public class DisableAuthentication extends UnlockState {
    private String TAG = "FingerprintService.DisableAuthentication";

    public DisableAuthentication(Context c, IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    public synchronized void updateState() {
        boolean z = true;
        synchronized (this) {
            if (this.mOrderedEvent != ORDERED_EVENTS.TOUCHDOWN_TOUCHUP || !this.mIsScreenOff || this.mAuthenticated == -1 || this.mAuthenticated == 0) {
                if (!((this.mOrderedEvent == ORDERED_EVENTS.TOUCHDOWN_POWERKEY || this.mOrderedEvent == ORDERED_EVENTS.POWERKEY_TOUCHDOWN) && this.mIsScreenOff && this.mAuthenticated != -1) && (this.mOrderedEvent != ORDERED_EVENTS.POWERKEY_TOUCHDOWN || this.mIsScreenOff || this.mAuthenticated == -1)) {
                    if (this.mOrderedEvent != ORDERED_EVENTS.TOUCHDOWN_POWERKEY || this.mIsScreenOff) {
                        z = false;
                    } else if (this.mAuthenticated == -1) {
                        z = false;
                    }
                }
            }
            this.mState = z;
            boolean state2 = (this.mOrderedEvent != ORDERED_EVENTS.POWERKEY_TOUCHDOWN || this.mIsScreenOff) ? false : this.mAuthenticated != -1;
            boolean state3 = (this.mOrderedEvent != ORDERED_EVENTS.TOUCHDOWN_POWERKEY || this.mIsScreenOff) ? false : this.mAuthenticated != -1;
            if (state2) {
                LogUtil.d(this.TAG, "powerkey screenon delay 150");
            }
            if (state3) {
                LogUtil.d(this.TAG, "powerkey screenon delay 0");
            }
            notifyStateChanged(this.mState, this.mAuthenticatedInfo);
        }
    }

    public void finishAction(AuthenticatedInfo authenticatedInfo) {
        this.mIUnLocker.reauthentication();
    }
}
