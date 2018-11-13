package android.hardware.light.V2_0;

import java.util.ArrayList;

public final class Status {
    public static final int BRIGHTNESS_NOT_SUPPORTED = 2;
    public static final int LIGHT_NOT_SUPPORTED = 1;
    public static final int SUCCESS = 0;
    public static final int UNKNOWN = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "LIGHT_NOT_SUPPORTED";
        }
        if (o == 2) {
            return "BRIGHTNESS_NOT_SUPPORTED";
        }
        if (o == 3) {
            return "UNKNOWN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("LIGHT_NOT_SUPPORTED");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("BRIGHTNESS_NOT_SUPPORTED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("UNKNOWN");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
