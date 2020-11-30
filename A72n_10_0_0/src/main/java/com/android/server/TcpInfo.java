package com.android.server;

import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class TcpInfo {
    @VisibleForTesting
    static final int LOST_OFFSET = getFieldOffset(Field.LOST);
    @VisibleForTesting
    static final int RETRANSMITS_OFFSET = getFieldOffset(Field.RETRANSMITS);
    @VisibleForTesting
    static final int SEGS_IN_OFFSET = getFieldOffset(Field.SEGS_IN);
    @VisibleForTesting
    static final int SEGS_OUT_OFFSET = getFieldOffset(Field.SEGS_OUT);
    private static final String TAG = "TcpInfo";
    final int mLost;
    final int mRetransmits;
    final int mSegsIn;
    final int mSegsOut;

    public enum Field {
        STATE(1),
        CASTATE(1),
        RETRANSMITS(1),
        PROBES(1),
        BACKOFF(1),
        OPTIONS(1),
        WSCALE(1),
        DELIVERY_RATE_APP_LIMITED(1),
        RTO(4),
        ATO(4),
        SND_MSS(4),
        RCV_MSS(4),
        UNACKED(4),
        SACKED(4),
        LOST(4),
        RETRANS(4),
        FACKETS(4),
        LAST_DATA_SENT(4),
        LAST_ACK_SENT(4),
        LAST_DATA_RECV(4),
        LAST_ACK_RECV(4),
        PMTU(4),
        RCV_SSTHRESH(4),
        RTT(4),
        RTTVAR(4),
        SND_SSTHRESH(4),
        SND_CWND(4),
        ADVMSS(4),
        REORDERING(4),
        RCV_RTT(4),
        RCV_SPACE(4),
        TOTAL_RETRANS(4),
        PACING_RATE(8),
        MAX_PACING_RATE(8),
        BYTES_ACKED(8),
        BYTES_RECEIVED(8),
        SEGS_OUT(4),
        SEGS_IN(4),
        NOTSENT_BYTES(4),
        MIN_RTT(4),
        DATA_SEGS_IN(4),
        DATA_SEGS_OUT(4),
        DELIVERY_RATE(8),
        BUSY_TIME(8),
        RWND_LIMITED(8),
        SNDBUF_LIMITED(8);
        
        public final int size;

        private Field(int s) {
            this.size = s;
        }
    }

    private static int getFieldOffset(Field needle) {
        int offset = 0;
        Field[] values = Field.values();
        for (Field field : values) {
            if (field == needle) {
                return offset;
            }
            offset += field.size;
        }
        throw new IllegalArgumentException("Unknown field");
    }

    private TcpInfo(ByteBuffer bytes, int infolen) {
        if (SEGS_IN_OFFSET + Field.SEGS_IN.size <= infolen) {
            int start = bytes.position();
            this.mSegsIn = bytes.getInt(SEGS_IN_OFFSET + start);
            this.mSegsOut = bytes.getInt(SEGS_OUT_OFFSET + start);
            this.mLost = bytes.getInt(LOST_OFFSET + start);
            this.mRetransmits = bytes.get(RETRANSMITS_OFFSET + start);
            bytes.position(Math.min(infolen + start, bytes.limit()));
            return;
        }
        throw new IllegalArgumentException("Length " + infolen + " is less than required.");
    }

    @VisibleForTesting
    TcpInfo(int retransmits, int lost, int segsOut, int segsIn) {
        this.mRetransmits = retransmits;
        this.mLost = lost;
        this.mSegsOut = segsOut;
        this.mSegsIn = segsIn;
    }

    public static TcpInfo parse(ByteBuffer bytes, int infolen) {
        try {
            return new TcpInfo(bytes, infolen);
        } catch (IllegalArgumentException | IndexOutOfBoundsException | BufferOverflowException | BufferUnderflowException e) {
            Log.e(TAG, "parsing error.", e);
            return null;
        }
    }

    private static String decodeWscale(byte num) {
        return String.valueOf((num >> 4) & 15) + ":" + String.valueOf(num & UsbDescriptor.DESCRIPTORTYPE_BOS);
    }

    @VisibleForTesting
    static String getTcpStateName(int state) {
        switch (state) {
            case 1:
                return "TCP_ESTABLISHED";
            case 2:
                return "TCP_SYN_SENT";
            case 3:
                return "TCP_SYN_RECV";
            case 4:
                return "TCP_FIN_WAIT1";
            case 5:
                return "TCP_FIN_WAIT2";
            case 6:
                return "TCP_TIME_WAIT";
            case 7:
                return "TCP_CLOSE";
            case 8:
                return "TCP_CLOSE_WAIT";
            case 9:
                return "TCP_LAST_ACK";
            case 10:
                return "TCP_LISTEN";
            case 11:
                return "TCP_CLOSING";
            default:
                return "UNKNOWN:" + Integer.toString(state);
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TcpInfo)) {
            return false;
        }
        TcpInfo other = (TcpInfo) obj;
        if (this.mSegsIn == other.mSegsIn && this.mSegsOut == other.mSegsOut && this.mRetransmits == other.mRetransmits && this.mLost == other.mLost) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mLost), Integer.valueOf(this.mRetransmits), Integer.valueOf(this.mSegsIn), Integer.valueOf(this.mSegsOut));
    }

    public String toString() {
        return "TcpInfo{lost=" + this.mLost + ", retransmit=" + this.mRetransmits + ", received=" + this.mSegsIn + ", sent=" + this.mSegsOut + "}";
    }
}
