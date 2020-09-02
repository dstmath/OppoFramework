package com.oppo.internal.telephony.rus;

import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import java.util.HashMap;

public final class RusUpdateNwAcBarCfg extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdateNwAcBarCfg";
    String[] mNwAcBarSbpNameArray = {"SBP_AC_BAR_ENHANCE", "SBP_SSAC_BAR_ENHANCE", "SBP_SKIP_AC_BAR_FOR_HIGH_PRIORITY_TASKS"};

    public RusUpdateNwAcBarCfg() {
        this.mRebootExecute = false;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("SBP_AC_BAR_ENHANCE")) {
            int index = 0;
            while (true) {
                String[] strArr = this.mNwAcBarSbpNameArray;
                if (index < strArr.length) {
                    String value = rusData.get(strArr[index]);
                    if (value != null && ("1".equals(value) || "0".equals(value))) {
                        sendAtCommand(this.mNwAcBarSbpNameArray[index], value);
                    }
                    index++;
                } else {
                    return;
                }
            }
        }
    }

    private void sendAtCommand(String sbp_name, String sbp_value) {
        printLog(TAG, "sendAtCommand: " + sbp_name + "=" + sbp_value);
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            try {
                Phone phone = this.sProxyPhones[i];
                phone.invokeOemRilRequestStrings(new String[]{"AT+ESBP=5,\"" + sbp_name + "\"," + sbp_value, ""}, (Message) null);
                i++;
            } catch (Exception e) {
                printLog(TAG, TAG);
                e.printStackTrace();
                return;
            }
        }
    }
}
