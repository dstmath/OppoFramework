package com.android.internal.telephony;

import android.telecom.ConferenceParticipant;
import android.telephony.Rlog;
import java.util.ArrayList;
import java.util.List;

public abstract class Call {
    /* renamed from: -com-android-internal-telephony-DriverCall$StateSwitchesValues */
    private static final /* synthetic */ int[] f2-com-android-internal-telephony-DriverCall$StateSwitchesValues = null;
    protected final String LOG_TAG = "Call";
    int colorosDisconnectCause = 3;
    public ArrayList<Connection> mConnections = new ArrayList();
    public State mState = State.IDLE;

    public enum SrvccState {
        NONE,
        STARTED,
        COMPLETED,
        FAILED,
        CANCELED
    }

    public enum State {
        IDLE,
        ACTIVE,
        HOLDING,
        DIALING,
        ALERTING,
        INCOMING,
        WAITING,
        DISCONNECTED,
        DISCONNECTING;

        public boolean isAlive() {
            return (this == IDLE || this == DISCONNECTED || this == DISCONNECTING) ? false : true;
        }

        public boolean isRinging() {
            return this == INCOMING || this == WAITING;
        }

        public boolean isDialing() {
            return this == DIALING || this == ALERTING;
        }

        public boolean isActive() {
            return this == ACTIVE || this == HOLDING || this == WAITING;
        }
    }

    /* renamed from: -getcom-android-internal-telephony-DriverCall$StateSwitchesValues */
    private static /* synthetic */ int[] m2xd9c92f69() {
        if (f2-com-android-internal-telephony-DriverCall$StateSwitchesValues != null) {
            return f2-com-android-internal-telephony-DriverCall$StateSwitchesValues;
        }
        int[] iArr = new int[com.android.internal.telephony.DriverCall.State.values().length];
        try {
            iArr[com.android.internal.telephony.DriverCall.State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.HOLDING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.INCOMING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[com.android.internal.telephony.DriverCall.State.WAITING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        f2-com-android-internal-telephony-DriverCall$StateSwitchesValues = iArr;
        return iArr;
    }

    public abstract List<Connection> getConnections();

    public abstract Phone getPhone();

    public abstract void hangup() throws CallStateException;

    public abstract boolean isMultiparty();

    public static State stateFromDCState(com.android.internal.telephony.DriverCall.State dcState) {
        switch (m2xd9c92f69()[dcState.ordinal()]) {
            case 1:
                return State.ACTIVE;
            case 2:
                return State.ALERTING;
            case 3:
                return State.DIALING;
            case 4:
                return State.HOLDING;
            case 5:
                return State.INCOMING;
            case 6:
                return State.WAITING;
            default:
                throw new RuntimeException("illegal call state:" + dcState);
        }
    }

    public boolean hasConnection(Connection c) {
        return c.getCall() == this;
    }

    public boolean hasConnections() {
        boolean z = false;
        List<Connection> connections = getConnections();
        if (connections == null) {
            return false;
        }
        if (connections.size() > 0) {
            z = true;
        }
        return z;
    }

    public State getState() {
        return this.mState;
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        return null;
    }

    public boolean isIdle() {
        return getState().isAlive() ^ 1;
    }

    public Connection getEarliestConnection() {
        long time = Long.MAX_VALUE;
        Connection earliest = null;
        List<Connection> l = getConnections();
        if (l.size() == 0) {
            return null;
        }
        int s = l.size();
        for (int i = 0; i < s; i++) {
            Connection c = (Connection) l.get(i);
            long t = c.getCreateTime();
            if (t < time) {
                earliest = c;
                time = t;
            }
        }
        return earliest;
    }

    public long getEarliestCreateTime() {
        long time = Long.MAX_VALUE;
        List<Connection> l = getConnections();
        if (l.size() == 0) {
            return 0;
        }
        int s = l.size();
        for (int i = 0; i < s; i++) {
            long t = ((Connection) l.get(i)).getCreateTime();
            if (t < time) {
                time = t;
            }
        }
        return time;
    }

    public long getEarliestConnectTime() {
        long time = Long.MAX_VALUE;
        List<Connection> l = getConnections();
        if (l.size() == 0) {
            return 0;
        }
        int s = l.size();
        for (int i = 0; i < s; i++) {
            long t = ((Connection) l.get(i)).getConnectTime();
            if (t < time) {
                time = t;
            }
        }
        return time;
    }

    public boolean isDialingOrAlerting() {
        return getState().isDialing();
    }

    public boolean isRinging() {
        return getState().isRinging();
    }

    public Connection getLatestConnection() {
        List<Connection> l = getConnections();
        if (l.size() == 0) {
            return null;
        }
        long time = 0;
        Connection latest = null;
        int s = l.size();
        for (int i = 0; i < s; i++) {
            Connection c = (Connection) l.get(i);
            long t = c.getCreateTime();
            if (t > time) {
                latest = c;
                time = t;
            }
        }
        return latest;
    }

    public void hangupIfAlive() {
        if (getState().isAlive()) {
            try {
                hangup();
            } catch (CallStateException ex) {
                Rlog.w("Call", " hangupIfActive: caught " + ex);
            }
        }
    }

    public void clearDisconnected() {
        for (int i = this.mConnections.size() - 1; i >= 0; i--) {
            if (((Connection) this.mConnections.get(i)).getState() == State.DISCONNECTED) {
                this.mConnections.remove(i);
            }
        }
        if (this.mConnections.size() == 0) {
            setState(State.IDLE);
        }
    }

    protected void setState(State newState) {
        this.mState = newState;
    }

    public void colorosSetCause(int cause) {
        this.colorosDisconnectCause = cause;
    }

    public int colorosGetCause() {
        return this.colorosDisconnectCause;
    }
}
