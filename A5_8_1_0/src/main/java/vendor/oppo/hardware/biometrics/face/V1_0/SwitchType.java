package vendor.oppo.hardware.biometrics.face.V1_0;

import java.util.ArrayList;

public final class SwitchType {
    public static final int TYPE_CLOSE_EYE_DETECT = 1;

    public static final String toString(int o) {
        if (o == 1) {
            return "TYPE_CLOSE_EYE_DETECT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("TYPE_CLOSE_EYE_DETECT");
            flipped = 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
