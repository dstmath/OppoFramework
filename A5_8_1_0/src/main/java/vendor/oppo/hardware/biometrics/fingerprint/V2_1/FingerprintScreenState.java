package vendor.oppo.hardware.biometrics.fingerprint.V2_1;

import java.util.ArrayList;

public final class FingerprintScreenState {
    public static final int FINGERPRINT_SCREEN_OFF = 0;
    public static final int FINGERPRINT_SCREEN_ON = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "FINGERPRINT_SCREEN_OFF";
        }
        if (o == 1) {
            return "FINGERPRINT_SCREEN_ON";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("FINGERPRINT_SCREEN_OFF");
        if ((o & 1) == 1) {
            list.add("FINGERPRINT_SCREEN_ON");
            flipped = 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
