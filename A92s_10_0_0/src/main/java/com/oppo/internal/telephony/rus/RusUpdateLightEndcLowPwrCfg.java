package com.oppo.internal.telephony.rus;

import android.content.Context;
import android.provider.Settings;
import java.util.HashMap;

public final class RusUpdateLightEndcLowPwrCfg extends RusBase {
    private static final String TAG = "RusUpdateLightEndcLowPwrCfg";
    public final int LIGHT_ENDC_LOW_PWR_CFG_LENGTH = 5;
    private Context mContext;
    int[] mLightEndcLowPwrCfgData = new int[5];
    String[] mLightEndcLowPwrParaNameArray = {"feature_enable", "screenoff_speed", "lowbat_speed", "lowbat_thres", "dis_endc_timer"};

    public RusUpdateLightEndcLowPwrCfg() {
        this.mRebootExecute = false;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("feature_enable")) {
            for (int index = 0; index < 5; index++) {
                String[] strArr = this.mLightEndcLowPwrParaNameArray;
                if (index >= strArr.length) {
                    break;
                }
                String mStrtemp = rusData.get(strArr[index]);
                if (mStrtemp != null) {
                    this.mLightEndcLowPwrCfgData[index] = Integer.parseInt(mStrtemp);
                }
            }
            printLog(TAG, " the feature enable is:" + this.mLightEndcLowPwrCfgData[0] + "screenoff_speed is:" + this.mLightEndcLowPwrCfgData[1] + "lowbat_speed is:" + this.mLightEndcLowPwrCfgData[2] + "lowbat_thres is:" + this.mLightEndcLowPwrCfgData[3] + "dis_endc_timer is:" + this.mLightEndcLowPwrCfgData[4]);
            this.mContext = this.mPhone.getContext();
            if (this.mContext != null) {
                int[] iArr = this.mLightEndcLowPwrCfgData;
                if (iArr[0] == 0 || 1 == iArr[0]) {
                    Settings.Global.putInt(this.mContext.getContentResolver(), "light_smart_fiveg", this.mLightEndcLowPwrCfgData[0]);
                    printLog(TAG, " after Rus mSwitch is:" + Settings.Global.getInt(this.mContext.getContentResolver(), "light_smart_fiveg", 221));
                }
                String mLightEndcLowPwrPara = "screenoff_speed=" + this.mLightEndcLowPwrCfgData[1] + ";lowbat_speed=" + this.mLightEndcLowPwrCfgData[2] + ";lowbat_thres=" + this.mLightEndcLowPwrCfgData[3] + ";dis_endc_timer=" + this.mLightEndcLowPwrCfgData[4] + ";";
                printLog(TAG, "LightEndcLowPwrPara is " + mLightEndcLowPwrPara);
                Settings.Global.putString(this.mContext.getContentResolver(), "LightEndcLowPwrPara", mLightEndcLowPwrPara);
                return;
            }
            printLog(TAG, "Context is null!");
        }
    }
}
