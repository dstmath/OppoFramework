package com.android.server.fingerprint.wakeup.state;

import android.content.Context;
import android.os.Looper;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.UnlockController.OrderedEvents;

public class DisableAuthentication extends UnlockState {
    private String TAG = "FingerprintService.DisableAuthentication";

    public DisableAuthentication(Context c, IUnLocker unLocker, Looper looper) {
        super(c, unLocker, looper);
    }

    public synchronized void updateState() {
        boolean z = true;
        synchronized (this) {
            if (this.mOrderedEvent != OrderedEvents.TOUCHDOWN_TOUCHUP || !this.mIsScreenOff || this.mAuthenticated == -1 || this.mAuthenticated == 0) {
                if (!((this.mOrderedEvent == OrderedEvents.TOUCHDOWN_POWERKEY || this.mOrderedEvent == OrderedEvents.POWERKEY_TOUCHDOWN) && this.mIsScreenOff && this.mAuthenticated != -1) && (this.mOrderedEvent != OrderedEvents.POWERKEY_TOUCHDOWN || (this.mIsScreenOff ^ 1) == 0 || this.mAuthenticated == -1)) {
                    if (this.mOrderedEvent != OrderedEvents.TOUCHDOWN_POWERKEY || (this.mIsScreenOff ^ 1) == 0) {
                        z = false;
                    } else if (this.mAuthenticated == -1) {
                        z = false;
                    }
                }
            }
            this.mState = z;
            boolean state2 = (this.mOrderedEvent != OrderedEvents.POWERKEY_TOUCHDOWN || (this.mIsScreenOff ^ 1) == 0) ? false : this.mAuthenticated != -1;
            boolean state3 = (this.mOrderedEvent != OrderedEvents.TOUCHDOWN_POWERKEY || (this.mIsScreenOff ^ 1) == 0) ? false : this.mAuthenticated != -1;
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
