package com.qualcomm.qti.imscmservice.V1_1;

import java.util.ArrayList;

public final class IMSCM_AUTOCONFIG_REQUEST_STATUS {
    public static final int IMSCM_AUTOCONFIG_CONNECTION_OFF = 0;
    public static final int IMSCM_AUTOCONFIG_CONNECTION_ON = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMSCM_AUTOCONFIG_CONNECTION_OFF";
        }
        if (o == 1) {
            return "IMSCM_AUTOCONFIG_CONNECTION_ON";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("IMSCM_AUTOCONFIG_CONNECTION_OFF");
        if ((o & 1) == 1) {
            list.add("IMSCM_AUTOCONFIG_CONNECTION_ON");
            flipped = 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
