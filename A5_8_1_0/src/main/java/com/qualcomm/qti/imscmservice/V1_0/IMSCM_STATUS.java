package com.qualcomm.qti.imscmservice.V1_0;

import java.util.ArrayList;

public final class IMSCM_STATUS {
    public static final int IMSCM_STATUS_DEINIT = 0;
    public static final int IMSCM_STATUS_FAILURE = 3;
    public static final int IMSCM_STATUS_INIT_IN_PROGRESS = 1;
    public static final int IMSCM_STATUS_SERVICE_CLOSED = 6;
    public static final int IMSCM_STATUS_SERVICE_CLOSING = 5;
    public static final int IMSCM_STATUS_SERVICE_DIED = 4;
    public static final int IMSCM_STATUS_SERVICE_NOT_SUPPORTED = 8;
    public static final int IMSCM_STATUS_SERVICE_RESTARTED = 7;
    public static final int IMSCM_STATUS_SUCCESS = 2;
    public static final int QIMSCM_STATUS_SERVICE_UNKNOWN = 9;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMSCM_STATUS_DEINIT";
        }
        if (o == 1) {
            return "IMSCM_STATUS_INIT_IN_PROGRESS";
        }
        if (o == 2) {
            return "IMSCM_STATUS_SUCCESS";
        }
        if (o == 3) {
            return "IMSCM_STATUS_FAILURE";
        }
        if (o == 4) {
            return "IMSCM_STATUS_SERVICE_DIED";
        }
        if (o == 5) {
            return "IMSCM_STATUS_SERVICE_CLOSING";
        }
        if (o == 6) {
            return "IMSCM_STATUS_SERVICE_CLOSED";
        }
        if (o == 7) {
            return "IMSCM_STATUS_SERVICE_RESTARTED";
        }
        if (o == 8) {
            return "IMSCM_STATUS_SERVICE_NOT_SUPPORTED";
        }
        if (o == 9) {
            return "QIMSCM_STATUS_SERVICE_UNKNOWN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("IMSCM_STATUS_DEINIT");
        if ((o & 1) == 1) {
            list.add("IMSCM_STATUS_INIT_IN_PROGRESS");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("IMSCM_STATUS_SUCCESS");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMSCM_STATUS_FAILURE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("IMSCM_STATUS_SERVICE_DIED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("IMSCM_STATUS_SERVICE_CLOSING");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("IMSCM_STATUS_SERVICE_CLOSED");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("IMSCM_STATUS_SERVICE_RESTARTED");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("IMSCM_STATUS_SERVICE_NOT_SUPPORTED");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("QIMSCM_STATUS_SERVICE_UNKNOWN");
            flipped |= 9;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
