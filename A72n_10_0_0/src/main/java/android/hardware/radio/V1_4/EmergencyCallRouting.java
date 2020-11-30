package android.hardware.radio.V1_4;

import android.database.sqlite.SQLiteGlobal;
import com.android.internal.telephony.IccCardConstants;
import java.util.ArrayList;

public final class EmergencyCallRouting {
    public static final int EMERGENCY = 1;
    public static final int NORMAL = 2;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        if (o == 1) {
            return "EMERGENCY";
        }
        if (o == 2) {
            return SQLiteGlobal.SYNC_MODE_FULL;
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add(IccCardConstants.INTENT_VALUE_ICC_UNKNOWN);
        if ((o & 1) == 1) {
            list.add("EMERGENCY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add(SQLiteGlobal.SYNC_MODE_FULL);
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
