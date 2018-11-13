package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SimHotswapState {
    public static final int SIM_HOTSWAP_STATE_INVALID = 0;
    public static final int SIM_HOTSWAP_STATE_SIM_PLUG_IN = 1;
    public static final int SIM_HOTSWAP_STATE_SIM_PLUG_OUT = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "SIM_HOTSWAP_STATE_INVALID";
        }
        if (o == 1) {
            return "SIM_HOTSWAP_STATE_SIM_PLUG_IN";
        }
        if (o == 2) {
            return "SIM_HOTSWAP_STATE_SIM_PLUG_OUT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("SIM_HOTSWAP_STATE_INVALID");
        if ((o & 1) == 1) {
            list.add("SIM_HOTSWAP_STATE_SIM_PLUG_IN");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("SIM_HOTSWAP_STATE_SIM_PLUG_OUT");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
