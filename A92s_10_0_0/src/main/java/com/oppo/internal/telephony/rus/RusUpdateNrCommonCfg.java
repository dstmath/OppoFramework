package com.oppo.internal.telephony.rus;

import android.content.Context;
import android.provider.Settings;
import java.util.HashMap;

public final class RusUpdateNrCommonCfg extends RusBase {
    private static final String TAG = "RusUpdateNrCommonCfg";
    int[] mCfgData = new int[this.mConfigParaNameArray.length];
    String[] mConfigParaNameArray = {"support_optimized_nr", "cellId_save_timeout", "cellId_list_size", "nrstate_smooth_delay", "nr_modem_reset_smooth_enabled", "nr_modem_reset_smooth_time"};
    private Context mContext;

    public RusUpdateNrCommonCfg() {
        this.mRebootExecute = false;
        this.mContext = this.mPhone.getContext();
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("support_optimized_nr")) {
            int index = 0;
            while (true) {
                String[] strArr = this.mConfigParaNameArray;
                if (index < strArr.length) {
                    String str = rusData.get(strArr[index]);
                    if (str != null) {
                        this.mCfgData[index] = Integer.parseInt(str);
                    }
                    index++;
                } else {
                    String config = "support_optimized_nr=" + this.mCfgData[0] + ";cellId_save_timeout=" + this.mCfgData[1] + ";cellId_list_size=" + this.mCfgData[2] + ";nrstate_smooth_delay=" + this.mCfgData[3] + ";nr_modem_reset_smooth_enabled=" + this.mCfgData[4] + ";nr_modem_reset_smooth_time=" + this.mCfgData[5] + ";";
                    printLog(TAG, "executeRusCommand config is : " + config);
                    Settings.Global.putString(this.mContext.getContentResolver(), "oppo_nr_common_cfg", config);
                    return;
                }
            }
        }
    }
}
