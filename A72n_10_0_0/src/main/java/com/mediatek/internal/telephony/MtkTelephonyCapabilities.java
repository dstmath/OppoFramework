package com.mediatek.internal.telephony;

import android.telephony.Rlog;
import com.android.internal.telephony.Phone;

public class MtkTelephonyCapabilities {
    private static final String LOG_TAG = "MtkTelCapability";

    private MtkTelephonyCapabilities() {
    }

    public static boolean supportsEcm(Phone phone) {
        Rlog.d(LOG_TAG, "supportsEcm: Phone type = " + phone.getPhoneType() + " Ims Phone = " + phone.getImsPhone());
        if (phone.getPhoneType() == 2 || phone.getPhoneType() == 1 || phone.getImsPhone() != null) {
            return true;
        }
        return false;
    }
}
