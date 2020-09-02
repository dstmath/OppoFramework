package com.oppo.internal.telephony.rus;

import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import java.util.HashMap;

public final class RusUpdateMdtGps extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdateMdtGps";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("mdt_gps_enable")) {
            String value = rusData.get("mdt_gps_enable");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",mdt_gps_enable:" + value);
            if ("1".equals(value) || "0".equals(value)) {
                sendAtCommand(value);
            }
        }
    }

    private void sendAtCommand(String mdt_value) {
        printLog(TAG, "on Succeed mdt_gps_enable = " + mdt_value);
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            try {
                printLog(TAG, "phone id:=" + i);
                Phone phone = this.sProxyPhones[i];
                phone.invokeOemRilRequestStrings(new String[]{"AT+ESBP=5,\"SBP_MDT_OBTAINLOCATION_OPEN_GPS\"," + mdt_value, ""}, (Message) null);
                i++;
            } catch (Exception e) {
                printLog(TAG, "hanlder doNVwrite wrong");
                e.printStackTrace();
                return;
            }
        }
    }
}
