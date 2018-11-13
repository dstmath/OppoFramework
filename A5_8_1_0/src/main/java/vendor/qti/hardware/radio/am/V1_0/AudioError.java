package vendor.qti.hardware.radio.am.V1_0;

import java.util.ArrayList;

public final class AudioError {
    public static final int AUDIO_GENERIC_FAILURE = 1;
    public static final int AUDIO_STATUS_OK = 0;
    public static final int AUDIO_STATUS_SERVER_DIED = 100;

    public static final String toString(int o) {
        if (o == 0) {
            return "AUDIO_STATUS_OK";
        }
        if (o == 1) {
            return "AUDIO_GENERIC_FAILURE";
        }
        if (o == 100) {
            return "AUDIO_STATUS_SERVER_DIED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("AUDIO_STATUS_OK");
        if ((o & 1) == 1) {
            list.add("AUDIO_GENERIC_FAILURE");
            flipped = 1;
        }
        if ((o & 100) == 100) {
            list.add("AUDIO_STATUS_SERVER_DIED");
            flipped |= 100;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
