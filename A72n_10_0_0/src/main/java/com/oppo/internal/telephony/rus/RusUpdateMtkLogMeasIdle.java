package com.oppo.internal.telephony.rus;

import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import java.util.HashMap;

public final class RusUpdateMtkLogMeasIdle extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdateMtkLogMeasIdle";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("LogMeasIdle_enable")) {
            String value = rusData.get("LogMeasIdle_enable");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",LogMeasIdle_enable:" + value);
            if ("1".equals(value) || "0".equals(value)) {
                sendAtCommand(value);
            }
        }
    }

    private void sendAtCommand(String LogMeasIdle_value) {
        printLog(TAG, "on Succeed LogMeasIdle_enable = " + LogMeasIdle_value);
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            try {
                printLog(TAG, "phone id:=" + i);
                Phone phone = this.sProxyPhones[i];
                phone.invokeOemRilRequestStrings(new String[]{"AT+ESBP=5,\"SBP_ERRC_CAPABILITY_OFF_BY_PLMN\"," + LogMeasIdle_value, ""}, (Message) null);
            } catch (Exception e) {
                printLog(TAG, "hanlder doNVwrite wrong");
                e.printStackTrace();
                return;
            }
        }
    }
}
