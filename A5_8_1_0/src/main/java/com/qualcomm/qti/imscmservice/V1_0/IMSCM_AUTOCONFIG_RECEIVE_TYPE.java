package com.qualcomm.qti.imscmservice.V1_0;

import java.util.ArrayList;

public final class IMSCM_AUTOCONFIG_RECEIVE_TYPE {
    public static final int IMSCM_AUTOCONFIG_CLIENT_REQUEST = 1;
    public static final int IMSCM_AUTOCONFIG_SERVER_UPDATE = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMSCM_AUTOCONFIG_SERVER_UPDATE";
        }
        if (o == 1) {
            return "IMSCM_AUTOCONFIG_CLIENT_REQUEST";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("IMSCM_AUTOCONFIG_SERVER_UPDATE");
        if ((o & 1) == 1) {
            list.add("IMSCM_AUTOCONFIG_CLIENT_REQUEST");
            flipped = 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
