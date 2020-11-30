package com.oppo.internal.telephony.rus;

import android.content.Context;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import java.util.HashMap;

public final class RusUpdateOosLpmCfg extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdateOosLpmCfg";
    private Context mContext;

    public RusUpdateOosLpmCfg() {
        this.mRebootExecute = true;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("feature_enable") && rusData.containsKey("recovery_threshold") && rusData.containsKey("inactive_fullband_timer") && rusData.containsKey("inactive_sniffer_timer")) {
            int enableInDeviceIdle = 0;
            int enableInLightDeviceIdle = 0;
            String strtemp = rusData.get("enable_in_device_idle");
            int i = 0;
            if (strtemp != null) {
                enableInDeviceIdle = 1 == Byte.parseByte(strtemp) ? 1 : 0;
            }
            String strtemp2 = rusData.get("enable_in_light_device_idle");
            if (strtemp2 != null) {
                if (1 == Byte.parseByte(strtemp2)) {
                    i = 1;
                }
                enableInLightDeviceIdle = i;
            }
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",feature_enable:" + rusData.get("feature_enable") + ",recovery_threshold:" + rusData.get("recovery_threshold") + ",inactive_fullband_timer:" + rusData.get("inactive_fullband_timer") + ",inactive_sniffer_timer:" + rusData.get("inactive_sniffer_timer") + ",enable_in_device_idle:" + enableInDeviceIdle + ",enable_in_light_device_idle:" + enableInLightDeviceIdle);
            StringBuilder sb = new StringBuilder();
            sb.append("AT+ESBP=5,\"SBP_INACTIVE_MODE_SERVICE_RECOVERY\",");
            sb.append(rusData.get("feature_enable"));
            sendAtCommand(sb.toString());
            if ("1".equals(rusData.get("feature_enable"))) {
                sendAtCommand("AT+ESRVREC = " + rusData.get("recovery_threshold") + "," + rusData.get("inactive_fullband_timer") + "," + rusData.get("inactive_sniffer_timer"));
            }
            this.mContext = this.mPhone.getContext();
            if (this.mContext != null) {
                String dozeCfg = "enable_in_device_idle=" + enableInDeviceIdle + ";enable_in_light_device_idle=" + enableInLightDeviceIdle + ";";
                printLog(TAG, "OosDozeCfg " + dozeCfg);
                Settings.Global.putString(this.mContext.getContentResolver(), "OosDozeCfg", dozeCfg);
                return;
            }
            printLog(TAG, "Context is null!");
        }
    }

    private void sendAtCommand(String strcmd) {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            try {
                this.sProxyPhones[i].invokeOemRilRequestStrings(new String[]{strcmd, ""}, (Message) null);
            } catch (Exception e) {
                printLog(TAG, "hanlder doNVwrite wrong");
                e.printStackTrace();
                return;
            }
        }
    }
}
