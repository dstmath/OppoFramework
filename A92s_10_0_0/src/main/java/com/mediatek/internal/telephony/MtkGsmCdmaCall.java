package com.mediatek.internal.telephony;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.GsmCdmaCall;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaConnection;

public class MtkGsmCdmaCall extends GsmCdmaCall {
    public MtkGsmCdmaCall(GsmCdmaCallTracker owner) {
        super(owner);
    }

    public boolean isMultiparty() {
        int discConn = 0;
        for (int j = this.mConnections.size() - 1; j >= 0; j--) {
            if (((GsmCdmaConnection) this.mConnections.get(j)).getState() == Call.State.DISCONNECTED) {
                discConn++;
            }
        }
        if (this.mConnections.size() > 1 && this.mConnections.size() > 1 && this.mConnections.size() - discConn > 1 && getState() != Call.State.DIALING) {
            return true;
        }
        return false;
    }

    public void onHangupLocal() {
        int s = this.mConnections.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) this.mConnections.get(i)).onHangupLocal();
        }
        if (this.mConnections.size() != 0 && getState().isAlive()) {
            this.mState = Call.State.DISCONNECTING;
        }
    }
}
