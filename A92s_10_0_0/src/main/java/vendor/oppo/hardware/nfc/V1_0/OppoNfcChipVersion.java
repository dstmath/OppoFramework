package vendor.oppo.hardware.nfc.V1_0;

import java.util.ArrayList;

public final class OppoNfcChipVersion {
    public static final byte CXD2248 = 8;
    public static final byte CXD2252 = 9;
    public static final byte MAX = 10;
    public static final byte NONE = -1;
    public static final byte NQ310 = 1;
    public static final byte NQ330 = 0;
    public static final byte SN100F = 3;
    public static final byte SN100T = 2;
    public static final byte SN100U = 4;
    public static final byte ST21H = 5;
    public static final byte ST54H = 6;
    public static final byte ST54J = 7;

    public static final String toString(byte o) {
        if (o == 0) {
            return "NQ330";
        }
        if (o == 1) {
            return "NQ310";
        }
        if (o == 2) {
            return "SN100T";
        }
        if (o == 3) {
            return "SN100F";
        }
        if (o == 4) {
            return "SN100U";
        }
        if (o == 5) {
            return "ST21H";
        }
        if (o == 6) {
            return "ST54H";
        }
        if (o == 7) {
            return "ST54J";
        }
        if (o == 8) {
            return "CXD2248";
        }
        if (o == 9) {
            return "CXD2252";
        }
        if (o == 10) {
            return "MAX";
        }
        if (o == -1) {
            return "NONE";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        list.add("NQ330");
        if ((o & 1) == 1) {
            list.add("NQ310");
            flipped = (byte) (0 | 1);
        }
        if ((o & 2) == 2) {
            list.add("SN100T");
            flipped = (byte) (flipped | 2);
        }
        if ((o & 3) == 3) {
            list.add("SN100F");
            flipped = (byte) (flipped | 3);
        }
        if ((o & 4) == 4) {
            list.add("SN100U");
            flipped = (byte) (flipped | 4);
        }
        if ((o & 5) == 5) {
            list.add("ST21H");
            flipped = (byte) (flipped | 5);
        }
        if ((o & 6) == 6) {
            list.add("ST54H");
            flipped = (byte) (flipped | 6);
        }
        if ((o & 7) == 7) {
            list.add("ST54J");
            flipped = (byte) (flipped | 7);
        }
        if ((o & 8) == 8) {
            list.add("CXD2248");
            flipped = (byte) (flipped | 8);
        }
        if ((o & 9) == 9) {
            list.add("CXD2252");
            flipped = (byte) (flipped | 9);
        }
        if ((o & 10) == 10) {
            list.add("MAX");
            flipped = (byte) (flipped | 10);
        }
        if ((o & -1) == -1) {
            list.add("NONE");
            flipped = (byte) (flipped | -1);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}
