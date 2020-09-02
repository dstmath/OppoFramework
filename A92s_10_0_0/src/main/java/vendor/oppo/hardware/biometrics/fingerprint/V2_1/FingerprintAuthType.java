package vendor.oppo.hardware.biometrics.fingerprint.V2_1;

import java.util.ArrayList;

public final class FingerprintAuthType {
    public static final int TYPE_KEYGUARD = 1;
    public static final int TYPE_OTHER = 3;
    public static final int TYPE_PAY = 2;

    public static final String toString(int o) {
        if (o == 1) {
            return "TYPE_KEYGUARD";
        }
        if (o == 2) {
            return "TYPE_PAY";
        }
        if (o == 3) {
            return "TYPE_OTHER";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("TYPE_KEYGUARD");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TYPE_PAY");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("TYPE_OTHER");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
