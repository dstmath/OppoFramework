package vendor.oppo.hardware.biometrics.fingerprint.V2_1;

import java.util.ArrayList;

public final class FingerprintAcquiredInfo {
    public static final int ACQUIRED_ALREADY_ENROLLED = 1002;
    public static final int ACQUIRED_GOOD = 0;
    public static final int ACQUIRED_IMAGER_DIRTY = 3;
    public static final int ACQUIRED_INSUFFICIENT = 2;
    public static final int ACQUIRED_PARTIAL = 1;
    public static final int ACQUIRED_TOO_FAST = 5;
    public static final int ACQUIRED_TOO_SIMILAR = 1001;
    public static final int ACQUIRED_TOO_SLOW = 4;
    public static final int ACQUIRED_VENDOR = 6;
    public static final int ACQUIRED_VENDOR_BASE = 1000;

    public static final String toString(int o) {
        if (o == 0) {
            return "ACQUIRED_GOOD";
        }
        if (o == 1) {
            return "ACQUIRED_PARTIAL";
        }
        if (o == 2) {
            return "ACQUIRED_INSUFFICIENT";
        }
        if (o == 3) {
            return "ACQUIRED_IMAGER_DIRTY";
        }
        if (o == 4) {
            return "ACQUIRED_TOO_SLOW";
        }
        if (o == 5) {
            return "ACQUIRED_TOO_FAST";
        }
        if (o == 6) {
            return "ACQUIRED_VENDOR";
        }
        if (o == 1000) {
            return "ACQUIRED_VENDOR_BASE";
        }
        if (o == 1001) {
            return "ACQUIRED_TOO_SIMILAR";
        }
        if (o == ACQUIRED_ALREADY_ENROLLED) {
            return "ACQUIRED_ALREADY_ENROLLED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("ACQUIRED_GOOD");
        if ((o & 1) == 1) {
            list.add("ACQUIRED_PARTIAL");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("ACQUIRED_INSUFFICIENT");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("ACQUIRED_IMAGER_DIRTY");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("ACQUIRED_TOO_SLOW");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ACQUIRED_TOO_FAST");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("ACQUIRED_VENDOR");
            flipped |= 6;
        }
        if ((o & 1000) == 1000) {
            list.add("ACQUIRED_VENDOR_BASE");
            flipped |= 1000;
        }
        if ((o & 1001) == 1001) {
            list.add("ACQUIRED_TOO_SIMILAR");
            flipped |= 1001;
        }
        if ((o & ACQUIRED_ALREADY_ENROLLED) == ACQUIRED_ALREADY_ENROLLED) {
            list.add("ACQUIRED_ALREADY_ENROLLED");
            flipped |= ACQUIRED_ALREADY_ENROLLED;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
