package com.oppo.internal.telephony.rus;

import android.os.Message;
import java.util.HashMap;

public final class RusUpdateNr5gDisableMode extends RusBase {
    public static final int DISABLE_BOTH_SA_AND_NSA_MODE = 1;
    public static final int DISABLE_NONE_MODE = 7;
    public static final int DISABLE_NSA_MODE = 3;
    public static final int DISABLE_SA_MODE = 5;
    private static final String TAG = "RusUpdateNr5gDisableMode";

    public RusUpdateNr5gDisableMode() {
        this.mRebootExecute = false;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("nr5g_disable_mode")) {
            String value = rusData.get("nr5g_disable_mode");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",nr5g_disable_mode:" + value);
            sendCommand(value);
        } else {
            printLog(TAG, "containsKey not contain nr5g_disable_mode");
        }
    }

    private void sendCommand(String nw_value) {
        printLog(TAG, "on Succeed nr5g_disable_mode = " + nw_value);
        int value = Integer.parseInt(nw_value);
        if (value == 7 || value == 5 || value == 3 || value == 1) {
            String cmd = "AT+E5GOPT=" + nw_value;
            if (this.sProxyPhones != null) {
                for (int i = 0; i < this.sProxyPhones.length; i++) {
                    if (this.sProxyPhones[i] != null) {
                        this.sProxyPhones[i].invokeOemRilRequestStrings(new String[]{cmd, ""}, (Message) null);
                    } else {
                        printLog(TAG, "sProxyPhones[" + i + "] null");
                    }
                }
                return;
            }
            printLog(TAG, "sProxyPhones null");
            return;
        }
        printLog(TAG, "mode unknown:" + value);
    }
}
