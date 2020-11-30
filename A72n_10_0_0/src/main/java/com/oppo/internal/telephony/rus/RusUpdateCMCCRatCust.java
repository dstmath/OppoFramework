package com.oppo.internal.telephony.rus;

import android.os.SystemProperties;
import java.util.HashMap;

public final class RusUpdateCMCCRatCust extends RusBase {
    private static final String TAG = "RusUpdateCMCCRatCustom";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("cmccratcust_setting")) {
            String value = rusData.get("cmccratcust_setting");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",cmccratcust_setting:" + value);
            if ("1".equals(value) || "0".equals(value)) {
                SystemProperties.set("persist.radio.cmcc.ratcust", value);
            }
        }
    }
}
