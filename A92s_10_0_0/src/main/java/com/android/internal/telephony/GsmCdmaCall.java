package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.telephony.Call;
import java.util.List;

public class GsmCdmaCall extends Call {
    GsmCdmaCallTracker mOwner;

    public GsmCdmaCall(GsmCdmaCallTracker owner) {
        this.mOwner = owner;
    }

    @Override // com.android.internal.telephony.Call
    public List<Connection> getConnections() {
        return this.mConnections;
    }

    @Override // com.android.internal.telephony.Call
    public Phone getPhone() {
        return this.mOwner.getPhone();
    }

    @Override // com.android.internal.telephony.Call
    public boolean isMultiparty() {
        return this.mConnections.size() > 1;
    }

    @Override // com.android.internal.telephony.Call
    public void hangup() throws CallStateException {
        this.mOwner.hangup(this);
    }

    public String toString() {
        return this.mState.toString();
    }

    public void attach(Connection conn, DriverCall dc) {
        this.mConnections.add(conn);
        this.mState = stateFromDCState(dc.state);
        conn.mPreState = this.mState;
    }

    @UnsupportedAppUsage
    public void attachFake(Connection conn, Call.State state) {
        this.mConnections.add(conn);
        this.mState = state;
        conn.mPreState = state;
    }

    public boolean connectionDisconnected(GsmCdmaConnection conn) {
        if (this.mState == Call.State.DISCONNECTED) {
            return false;
        }
        boolean hasOnlyDisconnectedConnections = true;
        int i = 0;
        int s = this.mConnections.size();
        while (true) {
            if (i >= s) {
                break;
            } else if (((Connection) this.mConnections.get(i)).getState() != Call.State.DISCONNECTED) {
                hasOnlyDisconnectedConnections = false;
                break;
            } else {
                i++;
            }
        }
        if (!hasOnlyDisconnectedConnections) {
            return false;
        }
        this.mState = Call.State.DISCONNECTED;
        return true;
    }

    public void detach(GsmCdmaConnection conn) {
        this.mConnections.remove(conn);
        if (this.mConnections.size() == 0) {
            this.mState = Call.State.IDLE;
        }
    }

    public boolean update(GsmCdmaConnection conn, DriverCall dc) {
        boolean changed = false;
        Call.State newState = stateFromDCState(dc.state);
        if (newState != this.mState) {
            this.mState = newState;
            changed = true;
            if (!(this.mState == Call.State.DISCONNECTED || this.mState == Call.State.DISCONNECTING)) {
                conn.mPreState = this.mState;
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public boolean isFull() {
        return this.mConnections.size() == this.mOwner.getMaxConnectionsPerCall();
    }

    public void onHangupLocal() {
        int s = this.mConnections.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) this.mConnections.get(i)).onHangupLocal();
        }
        this.mState = Call.State.DISCONNECTING;
    }
}
