package com.mediatek.internal.telephony.gsm.cbutil;

public class WhamTuple {
    public static final int WHAM_TUPLE_TYPE_SHARE = 1;
    public static final int WHAM_TUPLE_TYPE_UNIQUE = 0;
    public int mMsgId;
    public int mSerialNumber;
    public int mType;

    public WhamTuple(int type, int msgId, int serialNumber) {
        this.mType = type;
        this.mMsgId = msgId;
        this.mSerialNumber = serialNumber;
    }

    public String toString() {
        return "WhamTuple {type = " + this.mType + ", MsgId=0x" + Integer.toHexString(this.mMsgId) + ", mSerialNumber=0x" + Integer.toHexString(this.mSerialNumber) + '}';
    }
}
