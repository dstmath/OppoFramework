package com.mediatek.internal.telephony;

public class MtkEtwsUtils {
    public static final int ETWS_PDU_LENGTH = 56;

    public static byte[] intToBytes(int value) {
        byte[] ret = new byte[4];
        for (int i = 0; i < 4; i++) {
            ret[3 - i] = (byte) (value & 255);
            value >>= 8;
        }
        return ret;
    }

    public static int bytesToInt(byte[] values) {
        if (values == null || values.length == 0 || values.length > 4) {
            throw new RuntimeException("valid byte array");
        }
        int ret = 0;
        int len = values.length - 1;
        for (int i = 0; i < len; i++) {
            ret = (ret | (values[i] & 255)) << 8;
        }
        return ret | (values[len] & 255);
    }
}
