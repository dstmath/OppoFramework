package com.oppo.internal.telephony.rus;

import android.os.Message;
import android.telephony.TelephonyManager;
import java.util.HashMap;

public final class RusUpdateMtkCdmaOosLpmCfg extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdateMtkCdmaOosLpmCfg";

    public RusUpdateMtkCdmaOosLpmCfg() {
        this.mRebootExecute = true;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("feature_enable") && rusData.containsKey("recovery_threshold") && rusData.containsKey("inactive_fullband_timer") && rusData.containsKey("inactive_sniffer_timer") && rusData.containsKey("inactive_mode") && rusData.containsKey("inactive_scan_time") && rusData.containsKey("inactive_sleep_time")) {
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",feature_enable:" + rusData.get("feature_enable") + ",recovery_threshold:" + rusData.get("recovery_threshold") + ",inactive_fullband_timer:" + rusData.get("inactive_fullband_timer") + ",inactive_sniffer_timer:" + rusData.get("inactive_sniffer_timer") + ",inactive_mode:" + rusData.get("inactive_mode") + ",inactive_scan_time:" + rusData.get("inactive_scan_time") + ",inactive_sleep_time:" + rusData.get("inactive_sleep_time"));
            StringBuilder sb = new StringBuilder();
            sb.append("AT+ESBP=5,\"SBP_INACTIVE_MODE_SERVICE_RECOVERY_GMSS_CONTROL\",");
            sb.append(rusData.get("feature_enable"));
            sendAtCommand(sb.toString());
            if ("1".equals(rusData.get("feature_enable"))) {
                sendAtCommand("AT+ESRVREC = " + rusData.get("recovery_threshold") + "," + rusData.get("inactive_fullband_timer") + "," + rusData.get("inactive_sniffer_timer") + ",,," + rusData.get("inactive_scan_time") + "," + rusData.get("inactive_sleep_time"));
            }
        }
    }

    private void sendAtCommand(String strcmd) {
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            try {
                this.sProxyPhones[i].invokeOemRilRequestStrings(new String[]{strcmd, ""}, (Message) null);
                i++;
            } catch (Exception e) {
                printLog(TAG, "hanlder sendAtCommand wrong");
                e.printStackTrace();
                return;
            }
        }
    }
}
