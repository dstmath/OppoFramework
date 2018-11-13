package vendor.oppo.hardware.biometrics.fingerprintpay.V1_0;

import java.util.ArrayList;

public final class FPayStatusCode {
    public static final int ERROR_FAILED = -2;
    public static final int ERROR_NOT_SUPPORTED = -1;
    public static final int STATUS_OK = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "STATUS_OK";
        }
        if (o == -1) {
            return "ERROR_NOT_SUPPORTED";
        }
        if (o == -2) {
            return "ERROR_FAILED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("STATUS_OK");
        if ((o & -1) == -1) {
            list.add("ERROR_NOT_SUPPORTED");
            flipped = -1;
        }
        if ((o & -2) == -2) {
            list.add("ERROR_FAILED");
            flipped |= -2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
