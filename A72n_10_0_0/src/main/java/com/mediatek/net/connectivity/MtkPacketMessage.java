package com.mediatek.net.connectivity;

import android.util.Log;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MtkPacketMessage {
    private static final int INVALID_VALUE = -1;
    private static final int NFATTR_HEADER_SIZE = 4;
    private static final int NFA_ALIGN_TO = 4;
    private static final int NFMSG_SIZE = 4;
    public static final int NF_ACCEPT = 1;
    public static final int NF_DROP = 0;
    private static final int NLMSGHDR_SIZE = 16;
    private static final String TAG = "MtkPacketMessage";
    private static final int VERDICT_SIZE = 8;
    private final short NFQA_PACKET_HDR = 1;
    private final short NLMSG_ERROR = 2;
    private int mLen = INVALID_VALUE;
    private int mPacketId = INVALID_VALUE;
    private int mQueueNumber = INVALID_VALUE;
    private short mType = -1;

    public void parseFromBytes(byte[] bytes) {
        Log.i(TAG, "parseFromBytes " + Arrays.toString(bytes));
        if (bytes.length >= 20) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byte[] nlmsg = new byte[NLMSGHDR_SIZE];
            byteBuffer.get(nlmsg);
            parseHeader(nlmsg);
            if (!isErrorMessage(byteBuffer)) {
                byte[] nfmsg = new byte[4];
                byteBuffer.get(nfmsg);
                parseQueueNumber(nfmsg);
                byte[] nfatrr = new byte[((bytes.length - NLMSGHDR_SIZE) - 4)];
                byteBuffer.get(nfatrr);
                parsePacketId(nfatrr);
            }
        }
    }

    private boolean isErrorMessage(ByteBuffer byteBuffer) {
        if (this.mType != 2) {
            return false;
        }
        int errno = getReverseInt(byteBuffer);
        if (errno == 0) {
            Log.i(TAG, "This is ACK message ");
            return true;
        }
        Log.e(TAG, "parseFromBytes errno " + errno);
        return true;
    }

    private void parseHeader(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        this.mLen = getReverseInt(byteBuffer);
        this.mType = getReverseShort(byteBuffer);
    }

    private void parseQueueNumber(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.get();
        byteBuffer.get();
        this.mQueueNumber = byteBuffer.getShort();
    }

    private void parsePacketId(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        while (byteBuffer.remaining() > 4) {
            int nfaLen = getReverseShort(byteBuffer);
            short nfaType = getReverseShort(byteBuffer);
            Log.i(TAG, "parsePacketId nfa len/type/remaining " + nfaLen + "/" + ((int) nfaType) + "/" + byteBuffer.remaining());
            if (nfaLen % 4 > 0) {
                nfaLen = ((nfaLen / 4) + 1) * 4;
            }
            byte[] nfaBytes = new byte[(nfaLen - 4)];
            if (nfaBytes.length <= byteBuffer.remaining()) {
                byteBuffer.get(nfaBytes);
                if (nfaType == 1) {
                    this.mPacketId = ByteBuffer.wrap(nfaBytes).getInt();
                    return;
                }
            }
        }
    }

    private int getReverseInt(ByteBuffer buffer) {
        return Integer.reverseBytes(buffer.getInt());
    }

    private short getReverseShort(ByteBuffer buffer) {
        return Short.reverseBytes(buffer.getShort());
    }

    public short getType() {
        return this.mType;
    }

    public int getPacketId() {
        return this.mPacketId;
    }

    public int getQueueNumber() {
        return this.mQueueNumber;
    }

    public String toString() {
        return "MtkPacketMessage{ mLen=" + this.mLen + ", mType=" + ((int) this.mType) + ", mQueueNumber=" + this.mQueueNumber + ", mPacketId=" + this.mPacketId + '}';
    }

    public static byte[] getBindQueueMessage(int queueNumber) {
        byte[] bindMessage = {28, 0, 0, 0, 2, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 1, 0, 1, 0, 0, 0};
        updateQueueByte(bindMessage, queueNumber);
        return bindMessage;
    }

    public static byte[] getUnbindQueueMessage(int queueNumber) {
        byte[] unbindMessage = {28, 0, 0, 0, 2, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 1, 0, 2, 0, 0, 0};
        updateQueueByte(unbindMessage, queueNumber);
        return unbindMessage;
    }

    public static byte[] getSetModeMessage(int queueNumber) {
        byte[] modeMessage = {32, 0, 0, 0, 2, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 2, 0, 0, 0, -1, -1, 2, 0, 0, 0};
        updateQueueByte(modeMessage, queueNumber);
        return modeMessage;
    }

    public static byte[] getVerdictMessage(int queueNumber, int packetId, int response) {
        byte[] verdictPrefix = {32, 0, 0, 0, 1, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 2, 0};
        updateQueueByte(verdictPrefix, queueNumber);
        ByteBuffer byteBuffer = ByteBuffer.allocate(verdictPrefix.length + VERDICT_SIZE);
        byteBuffer.put(verdictPrefix);
        byteBuffer.putInt(response);
        byteBuffer.putInt(packetId);
        return byteBuffer.array();
    }

    private static void updateQueueByte(byte[] bytes, int queue) {
        byte[] queueBytes = ByteBuffer.allocate(4).putInt(queue).array();
        if (bytes.length >= 20) {
            bytes[18] = queueBytes[2];
            bytes[19] = queueBytes[3];
        }
    }
}
