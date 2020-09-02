package com.oppo.internal.telephony.rus;

import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import java.util.HashMap;

public final class RusUpdateNWLimitQoS extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdateNWLimitQoS";
    private final String SEPARATOR = ",";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("feature_enable") && rusData.containsKey("threshold_ul") && rusData.containsKey("threshold_dl") && rusData.containsKey("plmn")) {
            String enable = rusData.get("feature_enable");
            String qos_st = rusData.get("threshold_ul") + "," + rusData.get("threshold_dl") + "," + rusData.get("plmn");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",feature_enable:" + enable + ",qos_st:" + qos_st);
            sendAtCommand(enable, qos_st);
        }
    }

    private void sendAtCommand(String enable, String nw_limit_qos) {
        printLog(TAG, "on Succeed nw_limit_qos = " + enable + "," + nw_limit_qos);
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            try {
                Phone phone = this.sProxyPhones[i];
                phone.invokeOemRilRequestStrings(new String[]{"AT+EGQOS=" + enable + "," + "\"" + nw_limit_qos + "\"", ""}, (Message) null);
                i++;
            } catch (Exception e) {
                printLog(TAG, "hanlder doNVwrite wrong");
                e.printStackTrace();
                return;
            }
        }
    }
}
