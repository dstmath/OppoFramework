package vendor.qti.hardware.soter.V1_0;

import java.util.ArrayList;

public final class SoterErrorCode {
    public static final int SOTER_ERROR_ASK_NOT_READY = -5;
    public static final int SOTER_ERROR_ATTK_ALREADY_PROVISIONED = -20;
    public static final int SOTER_ERROR_ATTK_DIGEST_NOT_MATCH = -3;
    public static final int SOTER_ERROR_ATTK_DIGEST_NOT_READY = -4;
    public static final int SOTER_ERROR_ATTK_IS_VALID = -1;
    public static final int SOTER_ERROR_ATTK_NOT_EXIST = -2;
    public static final int SOTER_ERROR_ATTK_NOT_PROVISIONED = -13;
    public static final int SOTER_ERROR_AUTH_KEY_NOT_READY = -6;
    public static final int SOTER_ERROR_CHALLENGE_NULL = -203;
    public static final int SOTER_ERROR_INSUFFICIENT_BUFFER_SPACE = -22;
    public static final int SOTER_ERROR_INVALID_ARGUMENT = -16;
    public static final int SOTER_ERROR_INVALID_AUTHORIZATION_TIMEOUT = -27;
    public static final int SOTER_ERROR_INVALID_KEY_BLOB = -25;
    public static final int SOTER_ERROR_INVALID_TAG = -15;
    public static final int SOTER_ERROR_IS_AUTHING = -9;
    public static final int SOTER_ERROR_KEY_EXPORT_OPTIONS_INVALID = -28;
    public static final int SOTER_ERROR_KNAME_NULL = -202;
    public static final int SOTER_ERROR_MEMORY_ALLOCATION_FAILED = -11;
    public static final int SOTER_ERROR_NO_AUTH_KEY_MATCHED = -8;
    public static final int SOTER_ERROR_OK = 0;
    public static final int SOTER_ERROR_OPERATEID_NULL = -204;
    public static final int SOTER_ERROR_OTHERS = -10;
    public static final int SOTER_ERROR_SECURE_HW_COMMUNICATION_FAILED = -18;
    public static final int SOTER_ERROR_SESSION_OUT_OF_TIME = -7;
    public static final int SOTER_ERROR_SOTER_NOT_ENABLED = -12;
    public static final int SOTER_ERROR_UID_NULL = -201;
    public static final int SOTER_ERROR_UNEXPECTED_NULL_POINTER = -29;
    public static final int SOTER_ERROR_UNKNOWN_ERROR = -1000;
    public static final int SOTER_ERROR_UNSUPPORTED_DIGEST = -23;
    public static final int SOTER_ERROR_UNSUPPORTED_KEY_SIZE = -17;
    public static final int SOTER_ERROR_UNSUPPORTED_PADDING_MODE = -24;
    public static final int SOTER_ERROR_VERIFICATION_FAILED = -26;
    public static final int SOTER_RPMB_NOT_PROVISIONED = -21;
    public static final int SOTER_SECURITY_STATE_FAILURE = -14;
    public static final int SOTER_WRAPPERERROR_UNKNOWN = -200;

    public static final String toString(int o) {
        if (o == 0) {
            return "SOTER_ERROR_OK";
        }
        if (o == -1) {
            return "SOTER_ERROR_ATTK_IS_VALID";
        }
        if (o == -2) {
            return "SOTER_ERROR_ATTK_NOT_EXIST";
        }
        if (o == -3) {
            return "SOTER_ERROR_ATTK_DIGEST_NOT_MATCH";
        }
        if (o == -4) {
            return "SOTER_ERROR_ATTK_DIGEST_NOT_READY";
        }
        if (o == -5) {
            return "SOTER_ERROR_ASK_NOT_READY";
        }
        if (o == -6) {
            return "SOTER_ERROR_AUTH_KEY_NOT_READY";
        }
        if (o == -7) {
            return "SOTER_ERROR_SESSION_OUT_OF_TIME";
        }
        if (o == -8) {
            return "SOTER_ERROR_NO_AUTH_KEY_MATCHED";
        }
        if (o == -9) {
            return "SOTER_ERROR_IS_AUTHING";
        }
        if (o == -10) {
            return "SOTER_ERROR_OTHERS";
        }
        if (o == -11) {
            return "SOTER_ERROR_MEMORY_ALLOCATION_FAILED";
        }
        if (o == -12) {
            return "SOTER_ERROR_SOTER_NOT_ENABLED";
        }
        if (o == -13) {
            return "SOTER_ERROR_ATTK_NOT_PROVISIONED";
        }
        if (o == -14) {
            return "SOTER_SECURITY_STATE_FAILURE";
        }
        if (o == -15) {
            return "SOTER_ERROR_INVALID_TAG";
        }
        if (o == -16) {
            return "SOTER_ERROR_INVALID_ARGUMENT";
        }
        if (o == -17) {
            return "SOTER_ERROR_UNSUPPORTED_KEY_SIZE";
        }
        if (o == -18) {
            return "SOTER_ERROR_SECURE_HW_COMMUNICATION_FAILED";
        }
        if (o == -20) {
            return "SOTER_ERROR_ATTK_ALREADY_PROVISIONED";
        }
        if (o == -21) {
            return "SOTER_RPMB_NOT_PROVISIONED";
        }
        if (o == -22) {
            return "SOTER_ERROR_INSUFFICIENT_BUFFER_SPACE";
        }
        if (o == -23) {
            return "SOTER_ERROR_UNSUPPORTED_DIGEST";
        }
        if (o == -24) {
            return "SOTER_ERROR_UNSUPPORTED_PADDING_MODE";
        }
        if (o == -25) {
            return "SOTER_ERROR_INVALID_KEY_BLOB";
        }
        if (o == -26) {
            return "SOTER_ERROR_VERIFICATION_FAILED";
        }
        if (o == -27) {
            return "SOTER_ERROR_INVALID_AUTHORIZATION_TIMEOUT";
        }
        if (o == -28) {
            return "SOTER_ERROR_KEY_EXPORT_OPTIONS_INVALID";
        }
        if (o == -29) {
            return "SOTER_ERROR_UNEXPECTED_NULL_POINTER";
        }
        if (o == -200) {
            return "SOTER_WRAPPERERROR_UNKNOWN";
        }
        if (o == -201) {
            return "SOTER_ERROR_UID_NULL";
        }
        if (o == -202) {
            return "SOTER_ERROR_KNAME_NULL";
        }
        if (o == -203) {
            return "SOTER_ERROR_CHALLENGE_NULL";
        }
        if (o == -204) {
            return "SOTER_ERROR_OPERATEID_NULL";
        }
        if (o == -1000) {
            return "SOTER_ERROR_UNKNOWN_ERROR";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SOTER_ERROR_OK");
        if ((o & -1) == -1) {
            list.add("SOTER_ERROR_ATTK_IS_VALID");
            flipped = 0 | -1;
        }
        if ((o & -2) == -2) {
            list.add("SOTER_ERROR_ATTK_NOT_EXIST");
            flipped |= -2;
        }
        if ((o & -3) == -3) {
            list.add("SOTER_ERROR_ATTK_DIGEST_NOT_MATCH");
            flipped |= -3;
        }
        if ((o & -4) == -4) {
            list.add("SOTER_ERROR_ATTK_DIGEST_NOT_READY");
            flipped |= -4;
        }
        if ((o & -5) == -5) {
            list.add("SOTER_ERROR_ASK_NOT_READY");
            flipped |= -5;
        }
        if ((o & -6) == -6) {
            list.add("SOTER_ERROR_AUTH_KEY_NOT_READY");
            flipped |= -6;
        }
        if ((o & -7) == -7) {
            list.add("SOTER_ERROR_SESSION_OUT_OF_TIME");
            flipped |= -7;
        }
        if ((o & -8) == -8) {
            list.add("SOTER_ERROR_NO_AUTH_KEY_MATCHED");
            flipped |= -8;
        }
        if ((o & -9) == -9) {
            list.add("SOTER_ERROR_IS_AUTHING");
            flipped |= -9;
        }
        if ((o & -10) == -10) {
            list.add("SOTER_ERROR_OTHERS");
            flipped |= -10;
        }
        if ((o & -11) == -11) {
            list.add("SOTER_ERROR_MEMORY_ALLOCATION_FAILED");
            flipped |= -11;
        }
        if ((o & -12) == -12) {
            list.add("SOTER_ERROR_SOTER_NOT_ENABLED");
            flipped |= -12;
        }
        if ((o & -13) == -13) {
            list.add("SOTER_ERROR_ATTK_NOT_PROVISIONED");
            flipped |= -13;
        }
        if ((o & -14) == -14) {
            list.add("SOTER_SECURITY_STATE_FAILURE");
            flipped |= -14;
        }
        if ((o & -15) == -15) {
            list.add("SOTER_ERROR_INVALID_TAG");
            flipped |= -15;
        }
        if ((o & -16) == -16) {
            list.add("SOTER_ERROR_INVALID_ARGUMENT");
            flipped |= -16;
        }
        if ((o & -17) == -17) {
            list.add("SOTER_ERROR_UNSUPPORTED_KEY_SIZE");
            flipped |= -17;
        }
        if ((o & -18) == -18) {
            list.add("SOTER_ERROR_SECURE_HW_COMMUNICATION_FAILED");
            flipped |= -18;
        }
        if ((o & -20) == -20) {
            list.add("SOTER_ERROR_ATTK_ALREADY_PROVISIONED");
            flipped |= -20;
        }
        if ((o & -21) == -21) {
            list.add("SOTER_RPMB_NOT_PROVISIONED");
            flipped |= -21;
        }
        if ((o & -22) == -22) {
            list.add("SOTER_ERROR_INSUFFICIENT_BUFFER_SPACE");
            flipped |= -22;
        }
        if ((o & -23) == -23) {
            list.add("SOTER_ERROR_UNSUPPORTED_DIGEST");
            flipped |= -23;
        }
        if ((o & -24) == -24) {
            list.add("SOTER_ERROR_UNSUPPORTED_PADDING_MODE");
            flipped |= -24;
        }
        if ((o & -25) == -25) {
            list.add("SOTER_ERROR_INVALID_KEY_BLOB");
            flipped |= -25;
        }
        if ((o & -26) == -26) {
            list.add("SOTER_ERROR_VERIFICATION_FAILED");
            flipped |= -26;
        }
        if ((o & -27) == -27) {
            list.add("SOTER_ERROR_INVALID_AUTHORIZATION_TIMEOUT");
            flipped |= -27;
        }
        if ((o & -28) == -28) {
            list.add("SOTER_ERROR_KEY_EXPORT_OPTIONS_INVALID");
            flipped |= -28;
        }
        if ((o & -29) == -29) {
            list.add("SOTER_ERROR_UNEXPECTED_NULL_POINTER");
            flipped |= -29;
        }
        if ((o & SOTER_WRAPPERERROR_UNKNOWN) == -200) {
            list.add("SOTER_WRAPPERERROR_UNKNOWN");
            flipped |= SOTER_WRAPPERERROR_UNKNOWN;
        }
        if ((o & SOTER_ERROR_UID_NULL) == -201) {
            list.add("SOTER_ERROR_UID_NULL");
            flipped |= SOTER_ERROR_UID_NULL;
        }
        if ((o & SOTER_ERROR_KNAME_NULL) == -202) {
            list.add("SOTER_ERROR_KNAME_NULL");
            flipped |= SOTER_ERROR_KNAME_NULL;
        }
        if ((o & SOTER_ERROR_CHALLENGE_NULL) == -203) {
            list.add("SOTER_ERROR_CHALLENGE_NULL");
            flipped |= SOTER_ERROR_CHALLENGE_NULL;
        }
        if ((o & SOTER_ERROR_OPERATEID_NULL) == -204) {
            list.add("SOTER_ERROR_OPERATEID_NULL");
            flipped |= SOTER_ERROR_OPERATEID_NULL;
        }
        if ((o & SOTER_ERROR_UNKNOWN_ERROR) == -1000) {
            list.add("SOTER_ERROR_UNKNOWN_ERROR");
            flipped |= SOTER_ERROR_UNKNOWN_ERROR;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
