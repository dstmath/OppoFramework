package vendor.oppo.hardware.biometrics.face.V1_0;

import java.util.ArrayList;

public final class FaceLogType {
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_PERF = 3;

    public static final String toString(int o) {
        if (o == 1) {
            return "TYPE_LOG";
        }
        if (o == 2) {
            return "TYPE_IMAGE";
        }
        if (o == 3) {
            return "TYPE_PERF";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("TYPE_LOG");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("TYPE_IMAGE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("TYPE_PERF");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
