package com.qualcomm.qti.imscmservice.V1_0;

import java.util.ArrayList;

public final class QIMSCM_IPTYPE_ENUM {
    public static final int QIMSCM_IPTYPE_UNKNOWN = 0;
    public static final int QIMSCM_IPTYPE_V4 = 1;
    public static final int QIMSCM_IPTYPE_V6 = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "QIMSCM_IPTYPE_UNKNOWN";
        }
        if (o == 1) {
            return "QIMSCM_IPTYPE_V4";
        }
        if (o == 2) {
            return "QIMSCM_IPTYPE_V6";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("QIMSCM_IPTYPE_UNKNOWN");
        if ((o & 1) == 1) {
            list.add("QIMSCM_IPTYPE_V4");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("QIMSCM_IPTYPE_V6");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
