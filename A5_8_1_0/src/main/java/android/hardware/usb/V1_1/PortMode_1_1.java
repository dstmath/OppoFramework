package android.hardware.usb.V1_1;

import java.util.ArrayList;

public final class PortMode_1_1 {
    public static final int AUDIO_ACCESSORY = 4;
    public static final int DEBUG_ACCESSORY = 8;
    public static final int DFP = 2;
    public static final int DRP = 3;
    public static final int NONE = 0;
    public static final int NUM_MODES = 4;
    public static final int NUM_MODES_1_1 = 16;
    public static final int UFP = 1;

    public static final String toString(int o) {
        if (o == 4) {
            return "AUDIO_ACCESSORY";
        }
        if (o == 8) {
            return "DEBUG_ACCESSORY";
        }
        if (o == 16) {
            return "NUM_MODES_1_1";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 4) == 4) {
            list.add("AUDIO_ACCESSORY");
            flipped = 4;
        }
        if ((o & 8) == 8) {
            list.add("DEBUG_ACCESSORY");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("NUM_MODES_1_1");
            flipped |= 16;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
