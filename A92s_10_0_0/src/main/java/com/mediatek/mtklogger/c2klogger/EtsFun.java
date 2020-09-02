package com.mediatek.mtklogger.c2klogger;

import android.util.Log;

public class EtsFun extends EtsDevice {
    public static final byte AUTOMATIC = 0;
    public static final byte BAND_CLASS_0 = 0;
    public static final byte BAND_CLASS_1 = 1;
    public static final byte BAND_CLASS_10 = 10;
    public static final byte BAND_CLASS_11 = 11;
    public static final byte BAND_CLASS_12 = 12;
    public static final byte BAND_CLASS_13 = 13;
    public static final byte BAND_CLASS_14 = 14;
    public static final byte BAND_CLASS_15 = 15;
    public static final byte BAND_CLASS_16 = 16;
    public static final byte BAND_CLASS_2 = 2;
    public static final byte BAND_CLASS_3 = 3;
    public static final byte BAND_CLASS_4 = 4;
    public static final byte BAND_CLASS_5 = 5;
    public static final byte BAND_CLASS_6 = 6;
    public static final byte BAND_CLASS_7 = 7;
    public static final byte BAND_CLASS_8 = 8;
    public static final byte BAND_CLASS_9 = 9;
    public static final byte BOTH = 3;
    public static final byte CDMA_1XRTT = 0;
    public static final byte CDMA_EVDO = 1;
    public static final byte CSS = 1;
    public static final byte DISABLE = 1;
    public static final byte DIVERSITY = 2;
    public static final byte HWD = 2;
    public static final byte MAIN = 1;
    public static final byte MAIN_AND_DIVERSITY = 3;
    public static final byte MANUAL = 2;

    public Boolean open(String pathDev) {
        if (!create(pathDev).booleanValue()) {
            return false;
        }
        return true;
    }

    public void close() {
        destroy();
    }

    public Boolean configChannel(byte ctrlMode, byte ctrlUnit, byte rfMode, byte rfPath, byte band, short channel) {
        Log.v("via_ets", "pll config channel");
        byte[] dataChannel = EtsUtil.short2bytes(channel);
        boolean z = false;
        if (sendAndWait(new EtsMsg(1452, new byte[]{ctrlMode, ctrlUnit, rfMode, rfPath, band, dataChannel[0], dataChannel[1]}), 1452, 2000) != null) {
            z = true;
        }
        return Boolean.valueOf(z);
    }
}
