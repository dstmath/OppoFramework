package com.qualcomm.qti.imscmservice.V1_0;

import java.util.ArrayList;

public final class IMSCM_CONNECTION_EVENT {
    public static final int IMSCONNECTION_EVENT_SERVICE_ALLOWED = 2;
    public static final int IMSCONNECTION_EVENT_SERVICE_CREATED = 6;
    public static final int IMSCONNECTION_EVENT_SERVICE_FORCEFUL_CLOSE = 4;
    public static final int IMSCONNECTION_EVENT_SERVICE_NOTALLOWED = 3;
    public static final int IMSCONNECTION_EVENT_SERVICE_NOTREGISTERED = 0;
    public static final int IMSCONNECTION_EVENT_SERVICE_REGISTERED = 1;
    public static final int IMSCONNECTION_EVENT_SERVICE_TERMINATE_CONNECTION = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMSCONNECTION_EVENT_SERVICE_NOTREGISTERED";
        }
        if (o == 1) {
            return "IMSCONNECTION_EVENT_SERVICE_REGISTERED";
        }
        if (o == 2) {
            return "IMSCONNECTION_EVENT_SERVICE_ALLOWED";
        }
        if (o == 3) {
            return "IMSCONNECTION_EVENT_SERVICE_NOTALLOWED";
        }
        if (o == 4) {
            return "IMSCONNECTION_EVENT_SERVICE_FORCEFUL_CLOSE";
        }
        if (o == 5) {
            return "IMSCONNECTION_EVENT_SERVICE_TERMINATE_CONNECTION";
        }
        if (o == 6) {
            return "IMSCONNECTION_EVENT_SERVICE_CREATED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("IMSCONNECTION_EVENT_SERVICE_NOTREGISTERED");
        if ((o & 1) == 1) {
            list.add("IMSCONNECTION_EVENT_SERVICE_REGISTERED");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("IMSCONNECTION_EVENT_SERVICE_ALLOWED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMSCONNECTION_EVENT_SERVICE_NOTALLOWED");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("IMSCONNECTION_EVENT_SERVICE_FORCEFUL_CLOSE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("IMSCONNECTION_EVENT_SERVICE_TERMINATE_CONNECTION");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("IMSCONNECTION_EVENT_SERVICE_CREATED");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
