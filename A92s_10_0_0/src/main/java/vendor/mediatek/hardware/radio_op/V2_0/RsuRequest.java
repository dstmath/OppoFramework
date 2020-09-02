package vendor.mediatek.hardware.radio_op.V2_0;

import java.util.ArrayList;

public final class RsuRequest {
    public static final int RSU_REQUEST_GET_LOCK_STATUS = 5;
    public static final int RSU_REQUEST_GET_LOCK_VERSION = 3;
    public static final int RSU_REQUEST_GET_SHARED_KEY = 1;
    public static final int RSU_REQUEST_INIT_REQUEST = 0;
    public static final int RSU_REQUEST_RESET_LOCK_DATA = 4;
    public static final int RSU_REQUEST_UNLOCK_TIMER = 50;
    public static final int RSU_REQUEST_UPDATE_LOCK_DATA = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "RSU_REQUEST_INIT_REQUEST";
        }
        if (o == 1) {
            return "RSU_REQUEST_GET_SHARED_KEY";
        }
        if (o == 2) {
            return "RSU_REQUEST_UPDATE_LOCK_DATA";
        }
        if (o == 3) {
            return "RSU_REQUEST_GET_LOCK_VERSION";
        }
        if (o == 4) {
            return "RSU_REQUEST_RESET_LOCK_DATA";
        }
        if (o == 5) {
            return "RSU_REQUEST_GET_LOCK_STATUS";
        }
        if (o == 50) {
            return "RSU_REQUEST_UNLOCK_TIMER";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("RSU_REQUEST_INIT_REQUEST");
        if ((o & 1) == 1) {
            list.add("RSU_REQUEST_GET_SHARED_KEY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("RSU_REQUEST_UPDATE_LOCK_DATA");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("RSU_REQUEST_GET_LOCK_VERSION");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("RSU_REQUEST_RESET_LOCK_DATA");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("RSU_REQUEST_GET_LOCK_STATUS");
            flipped |= 5;
        }
        if ((o & 50) == 50) {
            list.add("RSU_REQUEST_UNLOCK_TIMER");
            flipped |= 50;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
