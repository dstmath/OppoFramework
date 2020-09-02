package com.oppo.internal.telephony.rus;

import android.os.Message;
import java.util.HashMap;

public final class RusUpdateJioSecodary extends RusBase {
    private static final String TAG = "RusUpdateJioSecodary";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("jio_setting")) {
            String value = rusData.get("jio_setting");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",jio_setting:" + value);
            if ("1".equals(value) || "0".equals(value)) {
                setJioSecodary(value);
            }
        }
    }

    private void setJioSecodary(String value) {
        String[] cmd = new String[2];
        if ("1".equals(value)) {
            cmd[0] = "AT+ELOSCUST=0,0";
            cmd[1] = "";
        } else {
            cmd[0] = "AT+ELOSCUST=1,1";
            cmd[1] = "";
        }
        try {
            this.mPhone.invokeOemRilRequestStrings(cmd, (Message) null);
        } catch (Exception e) {
            printLog(TAG, "hanlder doNVwrite wrong");
            e.printStackTrace();
        }
    }
}
