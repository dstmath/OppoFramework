package com.qualcomm.qti.imscmservice.V1_0;

import java.util.ArrayList;

public final class IMSCM_STATUSCODE {
    public static final int IMSCM_DISPATCHER_SEND_SUCCESS = 12;
    public static final int IMSCM_DNSQUERY_FAILURE = 9;
    public static final int IMSCM_DNSQUERY_PENDING = 8;
    public static final int IMSCM_FAILURE = 1;
    public static final int IMSCM_INVALID_FEATURE_TAG = 7;
    public static final int IMSCM_INVALID_LISTENER = 3;
    public static final int IMSCM_INVALID_MAX = 13;
    public static final int IMSCM_INVALID_PARAM = 4;
    public static final int IMSCM_MEMORY_ERROR = 2;
    public static final int IMSCM_MESSAGE_NOTALLOWED = 11;
    public static final int IMSCM_SERVICE_DIED = 10;
    public static final int IMSCM_SERVICE_NOTALLOWED = 5;
    public static final int IMSCM_SERVICE_UNAVAILABLE = 6;
    public static final int IMSCM_SUCCESS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "IMSCM_SUCCESS";
        }
        if (o == 1) {
            return "IMSCM_FAILURE";
        }
        if (o == 2) {
            return "IMSCM_MEMORY_ERROR";
        }
        if (o == 3) {
            return "IMSCM_INVALID_LISTENER";
        }
        if (o == 4) {
            return "IMSCM_INVALID_PARAM";
        }
        if (o == 5) {
            return "IMSCM_SERVICE_NOTALLOWED";
        }
        if (o == 6) {
            return "IMSCM_SERVICE_UNAVAILABLE";
        }
        if (o == 7) {
            return "IMSCM_INVALID_FEATURE_TAG";
        }
        if (o == 8) {
            return "IMSCM_DNSQUERY_PENDING";
        }
        if (o == 9) {
            return "IMSCM_DNSQUERY_FAILURE";
        }
        if (o == 10) {
            return "IMSCM_SERVICE_DIED";
        }
        if (o == 11) {
            return "IMSCM_MESSAGE_NOTALLOWED";
        }
        if (o == 12) {
            return "IMSCM_DISPATCHER_SEND_SUCCESS";
        }
        if (o == 13) {
            return "IMSCM_INVALID_MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("IMSCM_SUCCESS");
        if ((o & 1) == 1) {
            list.add("IMSCM_FAILURE");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("IMSCM_MEMORY_ERROR");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMSCM_INVALID_LISTENER");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("IMSCM_INVALID_PARAM");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("IMSCM_SERVICE_NOTALLOWED");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("IMSCM_SERVICE_UNAVAILABLE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("IMSCM_INVALID_FEATURE_TAG");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("IMSCM_DNSQUERY_PENDING");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("IMSCM_DNSQUERY_FAILURE");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("IMSCM_SERVICE_DIED");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("IMSCM_MESSAGE_NOTALLOWED");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("IMSCM_DISPATCHER_SEND_SUCCESS");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("IMSCM_INVALID_MAX");
            flipped |= 13;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
