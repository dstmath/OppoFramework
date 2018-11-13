package com.qualcomm.qti.imscmservice.V1_0;

import java.util.ArrayList;

public final class IMSCM_MESSAGE_TYPE {
    public static final int IMSCM_MESSAGE_TYPE_INVALID_MAX = 2;
    public static final int IMSCM_MESSAGE_TYPE_REQUEST = 0;
    public static final int IMSCM_MESSAGE_TYPE_RESPONSE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMSCM_MESSAGE_TYPE_REQUEST";
        }
        if (o == 1) {
            return "IMSCM_MESSAGE_TYPE_RESPONSE";
        }
        if (o == 2) {
            return "IMSCM_MESSAGE_TYPE_INVALID_MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("IMSCM_MESSAGE_TYPE_REQUEST");
        if ((o & 1) == 1) {
            list.add("IMSCM_MESSAGE_TYPE_RESPONSE");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("IMSCM_MESSAGE_TYPE_INVALID_MAX");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
