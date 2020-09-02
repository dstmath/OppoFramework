package android.telephony;

import android.os.PersistableBundle;

public class OppoCarrierConfigManager {
    private static final String LOG_TAG = "OppoCarrierConfigManager";
    public static final String OPPO_DUAL_LTE_AVAILABLE = "config_oppo_dual_lte_available_bool";

    public static void putDefault(PersistableBundle defaults) {
        defaults.putBoolean(OPPO_DUAL_LTE_AVAILABLE, true);
    }
}
