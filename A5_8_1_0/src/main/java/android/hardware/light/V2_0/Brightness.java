package android.hardware.light.V2_0;

import java.util.ArrayList;

public final class Brightness {
    public static final int LOW_PERSISTENCE = 2;
    public static final int SENSOR = 1;
    public static final int USER = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "USER";
        }
        if (o == 1) {
            return "SENSOR";
        }
        if (o == 2) {
            return "LOW_PERSISTENCE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("USER");
        if ((o & 1) == 1) {
            list.add("SENSOR");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("LOW_PERSISTENCE");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
