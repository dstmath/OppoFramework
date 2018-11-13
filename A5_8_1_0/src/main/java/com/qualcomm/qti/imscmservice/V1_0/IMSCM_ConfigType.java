package com.qualcomm.qti.imscmservice.V1_0;

import java.util.ArrayList;

public final class IMSCM_ConfigType {
    public static final int IMSCM_AUTO_CONFIG = 2;
    public static final int IMSCM_DEVICE_CONFIG = 1;
    public static final int IMSCM_USER_CONFIG = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMSCM_USER_CONFIG";
        }
        if (o == 1) {
            return "IMSCM_DEVICE_CONFIG";
        }
        if (o == 2) {
            return "IMSCM_AUTO_CONFIG";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("IMSCM_USER_CONFIG");
        if ((o & 1) == 1) {
            list.add("IMSCM_DEVICE_CONFIG");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("IMSCM_AUTO_CONFIG");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
