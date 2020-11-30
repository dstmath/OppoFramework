package com.mediatek.internal.telephony.cdma.pluscode;

import android.os.Build;
import android.telephony.Rlog;

public class CdmaPlusCodeUtils extends DefaultPlusCodeUtils {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    private static final String LOG_TAG = "CdmaPlusCodeUtils";

    public String checkMccBySidLtmOff(String mccMnc) {
        log("checkMccBySidLtmOff mccMnc=" + mccMnc);
        return PlusCodeToIddNddUtils.checkMccBySidLtmOff(mccMnc);
    }

    public boolean canFormatPlusToIddNdd() {
        log("canFormatPlusToIddNdd");
        return PlusCodeToIddNddUtils.canFormatPlusToIddNdd();
    }

    public boolean canFormatPlusCodeForSms() {
        log("canFormatPlusCodeForSms");
        return PlusCodeToIddNddUtils.canFormatPlusCodeForSms();
    }

    public String replacePlusCodeWithIddNdd(String number) {
        log("replacePlusCodeWithIddNdd number=" + Rlog.pii(LOG_TAG, number));
        return PlusCodeToIddNddUtils.replacePlusCodeWithIddNdd(number);
    }

    public String replacePlusCodeForSms(String number) {
        if (DBG) {
            log("replacePlusCodeForSms number=" + Rlog.pii(LOG_TAG, number));
        }
        return PlusCodeToIddNddUtils.replacePlusCodeForSms(number);
    }

    public String removeIddNddAddPlusCodeForSms(String number) {
        if (DBG) {
            log("removeIddNddAddPlusCodeForSms number=" + Rlog.pii(LOG_TAG, number));
        }
        return PlusCodeToIddNddUtils.removeIddNddAddPlusCodeForSms(number);
    }

    public String removeIddNddAddPlusCode(String number) {
        log("removeIddNddAddPlusCode number=" + Rlog.pii(LOG_TAG, number));
        return PlusCodeToIddNddUtils.removeIddNddAddPlusCode(number);
    }

    private static void log(String string) {
        if (DBG) {
            Rlog.d(LOG_TAG, string);
        }
    }
}
