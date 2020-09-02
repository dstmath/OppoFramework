package com.mediatek.mtklogger.c2klogger;

public class EtsMsgProgCmd extends EtsMsg {
    public static byte sEarse = 0;
    public static short sID = 1202;
    public static byte sWaite = 1;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public EtsMsgProgCmd(int sequence, byte type, byte section, byte[] data) {
        super(sID, new byte[((data == null ? 0 : data.length) + 10)]);
        System.arraycopy(EtsUtil.int2bytes(sequence), 0, this.mData, 0, 4);
        this.mData[4] = type;
        this.mData[5] = section;
        if (data != null) {
            System.arraycopy(EtsUtil.short2bytes(EtsUtil.checkSum(data)), 0, this.mData, 6, 2);
            System.arraycopy(EtsUtil.short2bytes((short) (data.length & 65535)), 0, this.mData, 8, 2);
            System.arraycopy(data, 0, this.mData, 10, data.length);
            return;
        }
        this.mData[6] = 0;
        this.mData[7] = 0;
        this.mData[8] = 0;
        this.mData[9] = 0;
    }
}
