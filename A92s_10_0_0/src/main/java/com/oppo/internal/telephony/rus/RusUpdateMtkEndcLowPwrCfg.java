package com.oppo.internal.telephony.rus;

import android.content.Context;
import android.provider.Settings;
import java.util.HashMap;

public final class RusUpdateMtkEndcLowPwrCfg extends RusBase {
    private static final String TAG = "RusUpdateMtkEndcLowPwrCfg";
    public final int ENDC_LOW_PWR_CFG_LENGTH = 19;
    private Context mContext;
    int[] mEndcLowPwrCfgData = new int[19];
    String[] mEndcLowPwrParaNameArray = {"feature_enable", "stats_duration", "scg_add_speed", "scg_fail_speed", "dis_endc_timer", "reward_scg_fail_speed", "reward_dis_endc_timer", "charging_enable", "hotspot_enable", "sib_no_nr_enable", "screenoff_only_enable", "lowbat_heavy_enable", "lowbat_thres", "lowbat_stats_avg_speed", "poorlte_rsrp_thres", "poorlte_rsrq_thres", "poorlte_bw_thres", "download_size_thres", "scenes_prohibit_masks"};

    public RusUpdateMtkEndcLowPwrCfg() {
        this.mRebootExecute = false;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("feature_enable")) {
            for (int index = 0; index < 19; index++) {
                String[] strArr = this.mEndcLowPwrParaNameArray;
                if (index >= strArr.length) {
                    break;
                }
                String mStrtemp = rusData.get(strArr[index]);
                if (mStrtemp != null) {
                    this.mEndcLowPwrCfgData[index] = Integer.parseInt(mStrtemp);
                }
            }
            printLog(TAG, " the feature enable is:" + this.mEndcLowPwrCfgData[0] + "stats_duration is:" + this.mEndcLowPwrCfgData[1] + "scg_add_speed is:" + this.mEndcLowPwrCfgData[2] + "scg_fail_speed is:" + this.mEndcLowPwrCfgData[3] + "dis_endc_timer is:" + this.mEndcLowPwrCfgData[4] + "reward_scg_fail_speed is:" + this.mEndcLowPwrCfgData[5] + "reward_dis_endc_timer is:" + this.mEndcLowPwrCfgData[6] + "charging_enable is:" + this.mEndcLowPwrCfgData[7] + "hotspot_enable is:" + this.mEndcLowPwrCfgData[8] + "sib_no_nr_enable is:" + this.mEndcLowPwrCfgData[9] + "screenoff_only_enable is:" + this.mEndcLowPwrCfgData[10] + "lowbat_heavy_enable is:" + this.mEndcLowPwrCfgData[11] + "lowbat_thres is:" + this.mEndcLowPwrCfgData[12] + "lowbat_stats_avg_speed is:" + this.mEndcLowPwrCfgData[13] + "poorlte_rsrp_thres is:" + this.mEndcLowPwrCfgData[14] + "poorlte_rsrq_thres is:" + this.mEndcLowPwrCfgData[15] + "poorlte_bw_thres is:" + this.mEndcLowPwrCfgData[16] + "download_size_thres is:" + this.mEndcLowPwrCfgData[17] + "scenes_prohibit_masks" + this.mEndcLowPwrCfgData[18]);
            this.mContext = this.mPhone.getContext();
            if (this.mContext != null) {
                String mEndcLowPwrPara = "stats_duration=" + this.mEndcLowPwrCfgData[1] + ";scg_add_speed=" + this.mEndcLowPwrCfgData[2] + ";scg_fail_speed=" + this.mEndcLowPwrCfgData[3] + ";dis_endc_timer=" + this.mEndcLowPwrCfgData[4] + ";reward_scg_fail_speed=" + this.mEndcLowPwrCfgData[5] + ";reward_dis_endc_timer=" + this.mEndcLowPwrCfgData[6] + ";charging_enable=" + this.mEndcLowPwrCfgData[7] + ";hotspot_enable=" + this.mEndcLowPwrCfgData[8] + ";sib_no_nr_enable=" + this.mEndcLowPwrCfgData[9] + ";screenoff_only_enable=" + this.mEndcLowPwrCfgData[10] + ";lowbat_heavy_enable=" + this.mEndcLowPwrCfgData[11] + ";lowbat_thres=" + this.mEndcLowPwrCfgData[12] + ";lowbat_stats_avg_speed=" + this.mEndcLowPwrCfgData[13] + ";poorlte_rsrp_thres=" + this.mEndcLowPwrCfgData[14] + ";poorlte_rsrq_thres=" + this.mEndcLowPwrCfgData[15] + ";poorlte_bw_thres=" + this.mEndcLowPwrCfgData[16] + ";download_size_thres=" + this.mEndcLowPwrCfgData[17] + ";scenes_prohibit_masks=" + this.mEndcLowPwrCfgData[18] + ";";
                printLog(TAG, "EndcLowPwrPara is " + mEndcLowPwrPara);
                Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrPara", mEndcLowPwrPara);
                return;
            }
            printLog(TAG, "Context is null!");
        }
    }
}
