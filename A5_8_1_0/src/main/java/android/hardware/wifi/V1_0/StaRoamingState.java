package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class StaRoamingState {
    public static final byte DISABLED = (byte) 0;
    public static final byte ENABLED = (byte) 1;

    public static final String toString(byte o) {
        if (o == (byte) 0) {
            return "DISABLED";
        }
        if (o == (byte) 1) {
            return "ENABLED";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList();
        byte flipped = (byte) 0;
        list.add("DISABLED");
        if ((o & 1) == 1) {
            list.add("ENABLED");
            flipped = (byte) 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}
