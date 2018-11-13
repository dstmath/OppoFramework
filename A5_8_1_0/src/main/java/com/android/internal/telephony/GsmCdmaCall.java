package com.android.internal.telephony;

import com.android.internal.telephony.Call.State;
import java.util.List;

public class GsmCdmaCall extends Call {
    GsmCdmaCallTracker mOwner;

    public GsmCdmaCall(GsmCdmaCallTracker owner) {
        this.mOwner = owner;
    }

    public List<Connection> getConnections() {
        return this.mConnections;
    }

    public Phone getPhone() {
        return this.mOwner.getPhone();
    }

    public boolean isMultiparty() {
        return this.mConnections.size() > 1;
    }

    public void hangup() throws CallStateException {
        this.mOwner.hangup(this);
    }

    public String toString() {
        return this.mState.toString();
    }

    public void attach(Connection conn, DriverCall dc) {
        this.mConnections.add(conn);
        this.mState = Call.stateFromDCState(dc.state);
        conn.mPreState = this.mState;
    }

    public void attachFake(Connection conn, State state) {
        this.mConnections.add(conn);
        this.mState = state;
        conn.mPreState = state;
    }

    public boolean connectionDisconnected(GsmCdmaConnection conn) {
        if (this.mState != State.DISCONNECTED) {
            boolean hasOnlyDisconnectedConnections = true;
            int s = this.mConnections.size();
            for (int i = 0; i < s; i++) {
                if (((Connection) this.mConnections.get(i)).getState() != State.DISCONNECTED) {
                    hasOnlyDisconnectedConnections = false;
                    break;
                }
            }
            if (hasOnlyDisconnectedConnections) {
                this.mState = State.DISCONNECTED;
                return true;
            }
        }
        return false;
    }

    public void detach(GsmCdmaConnection conn) {
        this.mConnections.remove(conn);
        if (this.mConnections.size() == 0) {
            this.mState = State.IDLE;
        }
    }

    boolean update(GsmCdmaConnection conn, DriverCall dc) {
        boolean changed = false;
        State newState = Call.stateFromDCState(dc.state);
        if (newState != this.mState) {
            this.mState = newState;
            changed = true;
            if (!(this.mState == State.DISCONNECTED || this.mState == State.DISCONNECTING)) {
                conn.mPreState = this.mState;
            }
        }
        return changed;
    }

    boolean isFull() {
        return this.mConnections.size() == this.mOwner.getMaxConnectionsPerCall();
    }

    void onHangupLocal() {
        int s = this.mConnections.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) this.mConnections.get(i)).onHangupLocal();
        }
        this.mState = State.DISCONNECTING;
    }
}
