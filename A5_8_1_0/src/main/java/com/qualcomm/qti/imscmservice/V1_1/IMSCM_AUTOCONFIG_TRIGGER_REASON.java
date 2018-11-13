package com.qualcomm.qti.imscmservice.V1_1;

import java.util.ArrayList;

public final class IMSCM_AUTOCONFIG_TRIGGER_REASON {
    public static final int IMSCM_AUTOCONFIG_CLIENT_CHANGE = 4;
    public static final int IMSCM_AUTOCONFIG_DEVICE_UPGRADE = 5;
    public static final int IMSCM_AUTOCONFIG_FACTORY_RESET = 6;
    public static final int IMSCM_AUTOCONFIG_INVALID_CREDENTIAL = 3;
    public static final int IMSCM_AUTOCONFIG_INVALID_TOKEN = 2;
    public static final int IMSCM_AUTOCONFIG_REFRESH_TOKEN = 1;
    public static final int IMSCM_AUTOCONFIG_USER_REQUEST = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMSCM_AUTOCONFIG_USER_REQUEST";
        }
        if (o == 1) {
            return "IMSCM_AUTOCONFIG_REFRESH_TOKEN";
        }
        if (o == 2) {
            return "IMSCM_AUTOCONFIG_INVALID_TOKEN";
        }
        if (o == 3) {
            return "IMSCM_AUTOCONFIG_INVALID_CREDENTIAL";
        }
        if (o == 4) {
            return "IMSCM_AUTOCONFIG_CLIENT_CHANGE";
        }
        if (o == 5) {
            return "IMSCM_AUTOCONFIG_DEVICE_UPGRADE";
        }
        if (o == 6) {
            return "IMSCM_AUTOCONFIG_FACTORY_RESET";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("IMSCM_AUTOCONFIG_USER_REQUEST");
        if ((o & 1) == 1) {
            list.add("IMSCM_AUTOCONFIG_REFRESH_TOKEN");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("IMSCM_AUTOCONFIG_INVALID_TOKEN");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMSCM_AUTOCONFIG_INVALID_CREDENTIAL");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("IMSCM_AUTOCONFIG_CLIENT_CHANGE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("IMSCM_AUTOCONFIG_DEVICE_UPGRADE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("IMSCM_AUTOCONFIG_FACTORY_RESET");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
