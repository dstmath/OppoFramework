package android.telephony;

import android.os.Bundle;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public final class OppoCellLocation {
    private static final String LOG_TAG = "OppoCellLocation";

    public static CellLocation newFromBundle(Bundle bundle) {
        int phoneTypeFromBundle;
        try {
            int phoneType = TelephonyManager.getDefault().getCurrentPhoneType(SubscriptionManager.getDefaultDataSubscriptionId());
            if (bundle != null && bundle.containsKey("type") && ((phoneTypeFromBundle = bundle.getInt("type", 0)) == 2 || phoneTypeFromBundle == 1)) {
                phoneType = phoneTypeFromBundle;
            }
            if (phoneType == 1) {
                return new GsmCellLocation(bundle);
            }
            if (phoneType != 2) {
                return null;
            }
            return new CdmaCellLocation(bundle);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "error, newFromBundle");
            return null;
        }
    }
}
