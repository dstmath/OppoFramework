package com.oppo.internal.telephony.rus;

import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import java.util.HashMap;

public final class RusUpdateMtkPreferBandParam extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdateMtkPreferBandParam";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("prefer_band_param_enable")) {
            String value = rusData.get("prefer_band_param_enable");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",prefer_band_param_enable:" + value);
            if ("1".equals(value) || "0".equals(value)) {
                sendAtCommand(value);
            }
        }
    }

    private void sendAtCommand(String prefer_band_value) {
        printLog(TAG, "on Succeed prefer_band_param_enable = " + prefer_band_value);
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            try {
                printLog(TAG, "phone id:=" + i);
                Phone phone = this.sProxyPhones[i];
                phone.invokeOemRilRequestStrings(new String[]{"AT+ESBP=5,\"SBP_MOBILITY_BAND_PREFERENCE\"," + prefer_band_value, ""}, (Message) null);
            } catch (Exception e) {
                printLog(TAG, "hanlder doNVwrite wrong");
                e.printStackTrace();
                return;
            }
        }
    }
}
