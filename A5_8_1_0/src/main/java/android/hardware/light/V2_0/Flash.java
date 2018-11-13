package android.hardware.light.V2_0;

import java.util.ArrayList;

public final class Flash {
    public static final int HARDWARE = 2;
    public static final int NONE = 0;
    public static final int TIMED = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "TIMED";
        }
        if (o == 2) {
            return "HARDWARE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("TIMED");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("HARDWARE");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
