package com.mediatek.internal.telephony.cdma.pluscode;

import android.os.Build;
import android.telephony.Rlog;

public class DefaultPlusCodeUtils implements IPlusCodeUtils {
    public static final boolean DBG = "eng".equals(Build.TYPE);
    private static final String LOG_TAG = "DefaultPlusCodeUtils";

    @Override // com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils
    public String checkMccBySidLtmOff(String mccMnc) {
        log("checkMccBySidLtmOff mccMnc=" + mccMnc);
        return mccMnc;
    }

    @Override // com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils
    public boolean canFormatPlusToIddNdd() {
        log("canFormatPlusToIddNdd");
        return false;
    }

    @Override // com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils
    public boolean canFormatPlusCodeForSms() {
        log("canFormatPlusCodeForSms");
        return false;
    }

    @Override // com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils
    public String replacePlusCodeWithIddNdd(String number) {
        log("replacePlusCodeWithIddNdd number=" + Rlog.pii(LOG_TAG, number));
        return number;
    }

    @Override // com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils
    public String replacePlusCodeForSms(String number) {
        log("replacePlusCodeForSms number=" + Rlog.pii(LOG_TAG, number));
        return number;
    }

    @Override // com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils
    public String removeIddNddAddPlusCodeForSms(String number) {
        log("removeIddNddAddPlusCodeForSms number=" + Rlog.pii(LOG_TAG, number));
        return number;
    }

    @Override // com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils
    public String removeIddNddAddPlusCode(String number) {
        log("removeIddNddAddPlusCode number=" + Rlog.pii(LOG_TAG, number));
        return number;
    }

    private static void log(String string) {
        if (DBG) {
            Rlog.d(LOG_TAG, string);
        }
    }
}
