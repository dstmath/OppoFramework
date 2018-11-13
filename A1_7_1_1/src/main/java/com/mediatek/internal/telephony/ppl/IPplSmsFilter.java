package com.mediatek.internal.telephony.ppl;

import android.os.Bundle;

public interface IPplSmsFilter {
    public static final String KEY_DST_ADDR = "dstAddr";
    public static final String KEY_FORMAT = "format";
    public static final String KEY_MSG_CONTENT = "msgContent";
    public static final String KEY_PDUS = "pdus";
    public static final String KEY_SIM_ID = "simId";
    public static final String KEY_SMS_TYPE = "smsType";
    public static final String KEY_SRC_ADDR = "srdAddr";
    public static final String KEY_SUB_ID = "subId";

    boolean pplFilter(Bundle bundle);
}
