package vendor.oppo.hardware.biometrics.face.V1_0;

import java.util.ArrayList;

public final class FaceAcquiredInfo {
    public static final int ACQUIRED_BRIGHT = 106;
    public static final int ACQUIRED_DARK = 103;
    public static final int ACQUIRED_DOWN = 110;
    public static final int ACQUIRED_FAR_FACE = 6;
    public static final int ACQUIRED_GOOD = 0;
    public static final int ACQUIRED_HACKER = 104;
    public static final int ACQUIRED_IMAGER_DIRTY = 3;
    public static final int ACQUIRED_INSUFFICIENT = 2;
    public static final int ACQUIRED_LEFT = 107;
    public static final int ACQUIRED_LOW_SIMILARITY = 105;
    public static final int ACQUIRED_NEAR_FACE = 7;
    public static final int ACQUIRED_NO_FACE = 101;
    public static final int ACQUIRED_PARTIAL = 1;
    public static final int ACQUIRED_RIGHT = 108;
    public static final int ACQUIRED_SHIFTING = 102;
    public static final int ACQUIRED_TOO_FAST = 5;
    public static final int ACQUIRED_TOO_SLOW = 4;
    public static final int ACQUIRED_UP = 109;
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
            return "ACQUIRED_FAR_FACE";
        }
        if (o == 7) {
            return "ACQUIRED_NEAR_FACE";
        }
        if (o == 101) {
            return "ACQUIRED_NO_FACE";
        }
        if (o == 102) {
            return "ACQUIRED_SHIFTING";
        }
        if (o == 103) {
            return "ACQUIRED_DARK";
        }
        if (o == 104) {
            return "ACQUIRED_HACKER";
        }
        if (o == 105) {
            return "ACQUIRED_LOW_SIMILARITY";
        }
        if (o == 106) {
            return "ACQUIRED_BRIGHT";
        }
        if (o == 107) {
            return "ACQUIRED_LEFT";
        }
        if (o == 108) {
            return "ACQUIRED_RIGHT";
        }
        if (o == 109) {
            return "ACQUIRED_UP";
        }
        if (o == 110) {
            return "ACQUIRED_DOWN";
        }
        if (o == 1000) {
            return "ACQUIRED_VENDOR_BASE";
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
            list.add("ACQUIRED_FAR_FACE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("ACQUIRED_NEAR_FACE");
            flipped |= 7;
        }
        if ((o & 101) == 101) {
            list.add("ACQUIRED_NO_FACE");
            flipped |= 101;
        }
        if ((o & 102) == 102) {
            list.add("ACQUIRED_SHIFTING");
            flipped |= 102;
        }
        if ((o & 103) == 103) {
            list.add("ACQUIRED_DARK");
            flipped |= 103;
        }
        if ((o & 104) == 104) {
            list.add("ACQUIRED_HACKER");
            flipped |= 104;
        }
        if ((o & 105) == 105) {
            list.add("ACQUIRED_LOW_SIMILARITY");
            flipped |= 105;
        }
        if ((o & 106) == 106) {
            list.add("ACQUIRED_BRIGHT");
            flipped |= 106;
        }
        if ((o & 107) == 107) {
            list.add("ACQUIRED_LEFT");
            flipped |= 107;
        }
        if ((o & 108) == 108) {
            list.add("ACQUIRED_RIGHT");
            flipped |= 108;
        }
        if ((o & 109) == 109) {
            list.add("ACQUIRED_UP");
            flipped |= 109;
        }
        if ((o & 110) == 110) {
            list.add("ACQUIRED_DOWN");
            flipped |= 110;
        }
        if ((o & 1000) == 1000) {
            list.add("ACQUIRED_VENDOR_BASE");
            flipped |= 1000;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
