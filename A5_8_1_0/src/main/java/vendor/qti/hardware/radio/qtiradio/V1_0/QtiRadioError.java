package vendor.qti.hardware.radio.qtiradio.V1_0;

import java.util.ArrayList;

public final class QtiRadioError {
    public static final int GENERIC_FAILURE = 2;
    public static final int NONE = 0;
    public static final int RADIO_NOT_AVAILABLE = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "RADIO_NOT_AVAILABLE";
        }
        if (o == 2) {
            return "GENERIC_FAILURE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("RADIO_NOT_AVAILABLE");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("GENERIC_FAILURE");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
