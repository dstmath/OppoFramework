package vendor.oppo.hardware.biometrics.fingerprintpay.V1_0;

import java.util.ArrayList;

public final class IFAAStatusCode {
    public static final int IFAA_ERR_AUTHENTICATOR_SIGN = 2046820371;
    public static final int IFAA_ERR_BAD_ACCESS = 2046820354;
    public static final int IFAA_ERR_BAD_PARAM = 2046820355;
    public static final int IFAA_ERR_BUF_TOO_SHORT = 2046820357;
    public static final int IFAA_ERR_ERASE = 2046820366;
    public static final int IFAA_ERR_GEN_RESPONSE = 2046820368;
    public static final int IFAA_ERR_GET_AUTHENTICATOR_VERSION = 2046820373;
    public static final int IFAA_ERR_GET_DEVICEID = 2046820369;
    public static final int IFAA_ERR_GET_ID_LIST = 2046820372;
    public static final int IFAA_ERR_GET_LAST_IDENTIFIED_RESULT = 2046820370;
    public static final int IFAA_ERR_HASH = 2046820360;
    public static final int IFAA_ERR_KEY_GEN = 2046820363;
    public static final int IFAA_ERR_NOT_MATCH = 2046820367;
    public static final int IFAA_ERR_NO_OPTIONAL_LEVEL = 2046820375;
    public static final int IFAA_ERR_OUT_OF_MEM = 2046820358;
    public static final int IFAA_ERR_READ = 2046820364;
    public static final int IFAA_ERR_SIGN = 2046820361;
    public static final int IFAA_ERR_SUCCESS = 0;
    public static final int IFAA_ERR_TIMEOUT = 2046820359;
    public static final int IFAA_ERR_UNKNOWN = 2046820353;
    public static final int IFAA_ERR_UNKNOWN_CMD = 2046820356;
    public static final int IFAA_ERR_UN_INITIALIZED = 2046820374;
    public static final int IFAA_ERR_VERIFY = 2046820362;
    public static final int IFAA_ERR_WRITE = 2046820365;

    public static final String toString(int o) {
        if (o == 0) {
            return "IFAA_ERR_SUCCESS";
        }
        if (o == 2046820353) {
            return "IFAA_ERR_UNKNOWN";
        }
        if (o == 2046820354) {
            return "IFAA_ERR_BAD_ACCESS";
        }
        if (o == 2046820355) {
            return "IFAA_ERR_BAD_PARAM";
        }
        if (o == 2046820356) {
            return "IFAA_ERR_UNKNOWN_CMD";
        }
        if (o == 2046820357) {
            return "IFAA_ERR_BUF_TOO_SHORT";
        }
        if (o == 2046820358) {
            return "IFAA_ERR_OUT_OF_MEM";
        }
        if (o == 2046820359) {
            return "IFAA_ERR_TIMEOUT";
        }
        if (o == 2046820360) {
            return "IFAA_ERR_HASH";
        }
        if (o == 2046820361) {
            return "IFAA_ERR_SIGN";
        }
        if (o == 2046820362) {
            return "IFAA_ERR_VERIFY";
        }
        if (o == 2046820363) {
            return "IFAA_ERR_KEY_GEN";
        }
        if (o == 2046820364) {
            return "IFAA_ERR_READ";
        }
        if (o == 2046820365) {
            return "IFAA_ERR_WRITE";
        }
        if (o == 2046820366) {
            return "IFAA_ERR_ERASE";
        }
        if (o == 2046820367) {
            return "IFAA_ERR_NOT_MATCH";
        }
        if (o == 2046820368) {
            return "IFAA_ERR_GEN_RESPONSE";
        }
        if (o == 2046820369) {
            return "IFAA_ERR_GET_DEVICEID";
        }
        if (o == 2046820370) {
            return "IFAA_ERR_GET_LAST_IDENTIFIED_RESULT";
        }
        if (o == 2046820371) {
            return "IFAA_ERR_AUTHENTICATOR_SIGN";
        }
        if (o == 2046820372) {
            return "IFAA_ERR_GET_ID_LIST";
        }
        if (o == 2046820373) {
            return "IFAA_ERR_GET_AUTHENTICATOR_VERSION";
        }
        if (o == 2046820374) {
            return "IFAA_ERR_UN_INITIALIZED";
        }
        if (o == 2046820375) {
            return "IFAA_ERR_NO_OPTIONAL_LEVEL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("IFAA_ERR_SUCCESS");
        if ((o & IFAA_ERR_UNKNOWN) == 2046820353) {
            list.add("IFAA_ERR_UNKNOWN");
            flipped = 0 | IFAA_ERR_UNKNOWN;
        }
        if ((o & IFAA_ERR_BAD_ACCESS) == 2046820354) {
            list.add("IFAA_ERR_BAD_ACCESS");
            flipped |= IFAA_ERR_BAD_ACCESS;
        }
        if ((o & IFAA_ERR_BAD_PARAM) == 2046820355) {
            list.add("IFAA_ERR_BAD_PARAM");
            flipped |= IFAA_ERR_BAD_PARAM;
        }
        if ((o & IFAA_ERR_UNKNOWN_CMD) == 2046820356) {
            list.add("IFAA_ERR_UNKNOWN_CMD");
            flipped |= IFAA_ERR_UNKNOWN_CMD;
        }
        if ((o & IFAA_ERR_BUF_TOO_SHORT) == 2046820357) {
            list.add("IFAA_ERR_BUF_TOO_SHORT");
            flipped |= IFAA_ERR_BUF_TOO_SHORT;
        }
        if ((o & IFAA_ERR_OUT_OF_MEM) == 2046820358) {
            list.add("IFAA_ERR_OUT_OF_MEM");
            flipped |= IFAA_ERR_OUT_OF_MEM;
        }
        if ((o & IFAA_ERR_TIMEOUT) == 2046820359) {
            list.add("IFAA_ERR_TIMEOUT");
            flipped |= IFAA_ERR_TIMEOUT;
        }
        if ((o & IFAA_ERR_HASH) == 2046820360) {
            list.add("IFAA_ERR_HASH");
            flipped |= IFAA_ERR_HASH;
        }
        if ((o & IFAA_ERR_SIGN) == 2046820361) {
            list.add("IFAA_ERR_SIGN");
            flipped |= IFAA_ERR_SIGN;
        }
        if ((o & IFAA_ERR_VERIFY) == 2046820362) {
            list.add("IFAA_ERR_VERIFY");
            flipped |= IFAA_ERR_VERIFY;
        }
        if ((o & IFAA_ERR_KEY_GEN) == 2046820363) {
            list.add("IFAA_ERR_KEY_GEN");
            flipped |= IFAA_ERR_KEY_GEN;
        }
        if ((o & IFAA_ERR_READ) == 2046820364) {
            list.add("IFAA_ERR_READ");
            flipped |= IFAA_ERR_READ;
        }
        if ((o & IFAA_ERR_WRITE) == 2046820365) {
            list.add("IFAA_ERR_WRITE");
            flipped |= IFAA_ERR_WRITE;
        }
        if ((o & IFAA_ERR_ERASE) == 2046820366) {
            list.add("IFAA_ERR_ERASE");
            flipped |= IFAA_ERR_ERASE;
        }
        if ((o & IFAA_ERR_NOT_MATCH) == 2046820367) {
            list.add("IFAA_ERR_NOT_MATCH");
            flipped |= IFAA_ERR_NOT_MATCH;
        }
        if ((2046820368 & o) == 2046820368) {
            list.add("IFAA_ERR_GEN_RESPONSE");
            flipped |= IFAA_ERR_GEN_RESPONSE;
        }
        if ((2046820369 & o) == 2046820369) {
            list.add("IFAA_ERR_GET_DEVICEID");
            flipped |= IFAA_ERR_GET_DEVICEID;
        }
        if ((2046820370 & o) == 2046820370) {
            list.add("IFAA_ERR_GET_LAST_IDENTIFIED_RESULT");
            flipped |= IFAA_ERR_GET_LAST_IDENTIFIED_RESULT;
        }
        if ((2046820371 & o) == 2046820371) {
            list.add("IFAA_ERR_AUTHENTICATOR_SIGN");
            flipped |= IFAA_ERR_AUTHENTICATOR_SIGN;
        }
        if ((2046820372 & o) == 2046820372) {
            list.add("IFAA_ERR_GET_ID_LIST");
            flipped |= IFAA_ERR_GET_ID_LIST;
        }
        if ((2046820373 & o) == 2046820373) {
            list.add("IFAA_ERR_GET_AUTHENTICATOR_VERSION");
            flipped |= IFAA_ERR_GET_AUTHENTICATOR_VERSION;
        }
        if ((2046820374 & o) == 2046820374) {
            list.add("IFAA_ERR_UN_INITIALIZED");
            flipped |= IFAA_ERR_UN_INITIALIZED;
        }
        if ((2046820375 & o) == 2046820375) {
            list.add("IFAA_ERR_NO_OPTIONAL_LEVEL");
            flipped |= IFAA_ERR_NO_OPTIONAL_LEVEL;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
