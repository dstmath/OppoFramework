package android.hardware.light.V2_0;

import java.util.ArrayList;

public final class Type {
    public static final int ATTENTION = 5;
    public static final int BACKLIGHT = 0;
    public static final int BATTERY = 3;
    public static final int BLUETOOTH = 6;
    public static final int BUTTONS = 2;
    public static final int COUNT = 8;
    public static final int KEYBOARD = 1;
    public static final int NOTIFICATIONS = 4;
    public static final int WIFI = 7;

    public static final String toString(int o) {
        if (o == 0) {
            return "BACKLIGHT";
        }
        if (o == 1) {
            return "KEYBOARD";
        }
        if (o == 2) {
            return "BUTTONS";
        }
        if (o == 3) {
            return "BATTERY";
        }
        if (o == 4) {
            return "NOTIFICATIONS";
        }
        if (o == 5) {
            return "ATTENTION";
        }
        if (o == 6) {
            return "BLUETOOTH";
        }
        if (o == 7) {
            return "WIFI";
        }
        if (o == 8) {
            return "COUNT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("BACKLIGHT");
        if ((o & 1) == 1) {
            list.add("KEYBOARD");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("BUTTONS");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("BATTERY");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("NOTIFICATIONS");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ATTENTION");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("BLUETOOTH");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("WIFI");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("COUNT");
            flipped |= 8;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
