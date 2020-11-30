package com.oppo.internal.telephony.rus;

import android.content.Context;
import android.provider.Settings;
import com.android.internal.util.XmlUtils;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class RusUpdateSmart5gCfg extends RusBase {
    private static final String TAG = "RusUpdateSmart5gCfg";
    public final int APK_CFG_LENGTH = 5;
    public final int BASIC_CFG_LENGTH = 18;
    public final int LIGHT_CFG_LENGTH = 6;
    public final int SCENES_CFG_LENGTH = 4;
    String[] mApkCfgData = new String[5];
    int[] mBasicCfgData = new int[18];
    private Context mContext;
    int[] mLightCfgData = new int[6];
    int[] mScenesCfgData = new int[4];

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public boolean parseRusXML(XmlPullParser parser, HashMap<String, String> rusData) {
        XmlPullParserException e;
        Exception e2;
        boolean z = true;
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return true;
        }
        parser.getName();
        try {
            String cfgtype = parser.getName();
            if ("basic_cfg".equals(cfgtype)) {
                try {
                    String cfgvalue = parser.getAttributeValue(null, "ver_num") + "@" + parser.getAttributeValue(null, "feature_enable") + "@" + parser.getAttributeValue(null, "stats_duration") + "@" + parser.getAttributeValue(null, "scg_add_speed") + "@" + parser.getAttributeValue(null, "scg_fail_speed") + "@" + parser.getAttributeValue(null, "dis_endc_timer") + "@" + parser.getAttributeValue(null, "reward_scg_fail_speed") + "@" + parser.getAttributeValue(null, "reward_dis_endc_timer") + "@" + parser.getAttributeValue(null, "charging_enable") + "@" + parser.getAttributeValue(null, "hotspot_enable") + "@" + parser.getAttributeValue(null, "sib_no_nr_enable") + "@" + parser.getAttributeValue(null, "screenoff_only_enable") + "@" + parser.getAttributeValue(null, "lowbat_heavy_enable") + "@" + parser.getAttributeValue(null, "lowbat_thres") + "@" + parser.getAttributeValue(null, "lowbat_stats_avg_speed") + "@" + parser.getAttributeValue(null, "poorlte_rsrp_thres") + "@" + parser.getAttributeValue(null, "poorlte_rsrq_thres") + "@" + parser.getAttributeValue(null, "poorlte_bw_thres");
                    try {
                        printLog(TAG, "cfgtype:" + cfgtype + ",cfgvalue:" + cfgvalue);
                        rusData.put(cfgtype, cfgvalue);
                        XmlUtils.nextElement(parser);
                        return false;
                    } catch (XmlPullParserException e3) {
                        e = e3;
                        z = true;
                        printLog(TAG, e.toString());
                        return z;
                    } catch (Exception e4) {
                        e2 = e4;
                        printLog(TAG, e2.toString());
                        return true;
                    }
                } catch (XmlPullParserException e5) {
                    e = e5;
                    z = true;
                    printLog(TAG, e.toString());
                    return z;
                } catch (Exception e6) {
                    e2 = e6;
                    printLog(TAG, e2.toString());
                    return true;
                }
            } else if ("light_cfg".equals(cfgtype)) {
                String cfgvalue2 = parser.getAttributeValue(null, "ver_num") + "@" + parser.getAttributeValue(null, "feature_enable") + "@" + parser.getAttributeValue(null, "screenoff_speed") + "@" + parser.getAttributeValue(null, "lowbat_speed") + "@" + parser.getAttributeValue(null, "lowbat_thres") + "@" + parser.getAttributeValue(null, "dis_endc_timer");
                printLog(TAG, "cfgtype:" + cfgtype + ",cfgvalue:" + cfgvalue2);
                rusData.put(cfgtype, cfgvalue2);
                XmlUtils.nextElement(parser);
                return false;
            } else if ("scenes_cfg".equals(cfgtype)) {
                String cfgvalue3 = parser.getAttributeValue(null, "ver_num") + "@" + parser.getAttributeValue(null, "scg_add_speed") + "@" + parser.getAttributeValue(null, "scg_add_speed_lowbat") + "@" + parser.getAttributeValue(null, "scenes_prohibit_masks");
                printLog(TAG, "cfgtype:" + cfgtype + ",cfgvalue:" + cfgvalue3);
                rusData.put(cfgtype, cfgvalue3);
                XmlUtils.nextElement(parser);
                return false;
            } else if (!"apk_cfg".equals(cfgtype)) {
                return true;
            } else {
                String cfgvalue4 = parser.getAttributeValue(null, "ver_num") + "@" + parser.getAttributeValue(null, "scg_add_speed") + "@" + parser.getAttributeValue(null, "scg_add_speed_lowbat") + "@" + parser.getAttributeValue(null, "restrict") + "@" + parser.getAttributeValue(null, "black");
                printLog(TAG, "cfgtype:" + cfgtype + ",cfgvalue:" + cfgvalue4);
                rusData.put(cfgtype, cfgvalue4);
                XmlUtils.nextElement(parser);
                return false;
            }
        } catch (XmlPullParserException e7) {
            e = e7;
            printLog(TAG, e.toString());
            return z;
        } catch (Exception e8) {
            e2 = e8;
            printLog(TAG, e2.toString());
            return true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return;
        }
        printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ", basic_cfg:" + rusData.get("basic_cfg") + ", light_cfg:" + rusData.get("light_cfg") + ", scenes_cfg:" + rusData.get("scenes_cfg") + ", apk_cfg:" + rusData.get("apk_cfg"));
        for (Map.Entry<String, String> entry : rusData.entrySet()) {
            String key = entry.getKey();
            String strcfg = entry.getValue();
            if ("basic_cfg".equals(key)) {
                String[] cfgitem = strcfg.split("@");
                int item_idx = 0;
                while (item_idx < cfgitem.length && item_idx < 18) {
                    if (cfgitem[item_idx] != null) {
                        this.mBasicCfgData[item_idx] = Integer.parseInt(cfgitem[item_idx]);
                    }
                    item_idx++;
                }
                printLog(TAG, "The ver_num is:" + this.mBasicCfgData[0] + " the feature enable is:" + this.mBasicCfgData[1] + "stats_duration is:" + this.mBasicCfgData[2] + "scg_add_speed is:" + this.mBasicCfgData[3] + "scg_fail_speed is:" + this.mBasicCfgData[4] + "dis_endc_timer" + this.mBasicCfgData[5] + "reward_scg_fail_speed" + this.mBasicCfgData[6] + "reward_dis_endc_timer" + this.mBasicCfgData[7] + "charging_enable is:" + this.mBasicCfgData[8] + "hotspot_enable is:" + this.mBasicCfgData[9] + "sib_no_nr_enable is:" + this.mBasicCfgData[10] + "screenoff_only_enable is:" + this.mBasicCfgData[11] + "lowbat_heavy_enable is:" + this.mBasicCfgData[12] + "lowbat_thres is:" + this.mBasicCfgData[13] + "lowbat_stats_avg_speed is:" + this.mBasicCfgData[14] + "poorlte_rsrp_thres is:" + this.mBasicCfgData[15] + "poorlte_rsrq_thres is:" + this.mBasicCfgData[16] + "poorlte_bw_thres is:" + this.mBasicCfgData[17]);
                this.mContext = this.mPhone.getContext();
                if (this.mContext != null) {
                    String smart5gBsicPara = "ver_num=" + this.mBasicCfgData[0] + ";stats_duration=" + this.mBasicCfgData[2] + ";scg_add_speed=" + this.mBasicCfgData[3] + ";scg_fail_speed=" + this.mBasicCfgData[4] + ";dis_endc_timer=" + this.mBasicCfgData[5] + ";reward_scg_fail_speed=" + this.mBasicCfgData[6] + ";reward_dis_endc_timer=" + this.mBasicCfgData[7] + ";charging_enable=" + this.mBasicCfgData[8] + ";hotspot_enable=" + this.mBasicCfgData[9] + ";sib_no_nr_enable=" + this.mBasicCfgData[10] + ";screenoff_only_enable=" + this.mBasicCfgData[11] + ";lowbat_heavy_enable=" + this.mBasicCfgData[12] + ";lowbat_thres=" + this.mBasicCfgData[13] + ";lowbat_stats_avg_speed=" + this.mBasicCfgData[14] + ";poorlte_rsrp_thres=" + this.mBasicCfgData[15] + ";poorlte_rsrq_thres=" + this.mBasicCfgData[16] + ";poorlte_bw_thres=" + this.mBasicCfgData[17];
                    printLog(TAG, "smart5gBsicPara is " + smart5gBsicPara);
                    Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrBasicCfgPara", smart5gBsicPara);
                } else {
                    printLog(TAG, "Context is null!");
                }
            } else if ("light_cfg".equals(key)) {
                String[] cfgitem2 = strcfg.split("@");
                int item_idx2 = 0;
                while (item_idx2 < cfgitem2.length && item_idx2 < 6) {
                    if (cfgitem2[item_idx2] != null) {
                        this.mLightCfgData[item_idx2] = Integer.parseInt(cfgitem2[item_idx2]);
                    }
                    item_idx2++;
                }
                printLog(TAG, "The ver_num is:" + this.mLightCfgData[0] + "the feature enable is:" + this.mLightCfgData[1] + "screenoff_speed is:" + this.mLightCfgData[2] + "lowbat_speed is:" + this.mLightCfgData[3] + "lowbat_thres is:" + this.mLightCfgData[4] + "dis_endc_timer is:" + this.mLightCfgData[5]);
                this.mContext = this.mPhone.getContext();
                if (this.mContext != null) {
                    int[] iArr = this.mLightCfgData;
                    if (iArr[1] == 0 || 1 == iArr[1]) {
                        Settings.Global.putInt(this.mContext.getContentResolver(), "light_smart_fiveg", this.mLightCfgData[1]);
                        printLog(TAG, " after Rus mSwitch is:" + Settings.Global.getInt(this.mContext.getContentResolver(), "light_smart_fiveg", 221));
                    }
                    String smart5gLightPara = "ver_num=" + this.mLightCfgData[0] + ";screenoff_speed=" + this.mLightCfgData[2] + ";lowbat_speed=" + this.mLightCfgData[3] + ";lowbat_thres=" + this.mLightCfgData[4] + ";dis_endc_timer=" + this.mLightCfgData[5];
                    printLog(TAG, "smart5gLightPara is " + smart5gLightPara);
                    Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrLightPara", smart5gLightPara);
                } else {
                    printLog(TAG, "Context is null!");
                }
            } else if ("scenes_cfg".equals(key)) {
                String[] cfgitem3 = strcfg.split("@");
                int item_idx3 = 0;
                while (item_idx3 < cfgitem3.length && item_idx3 < 4) {
                    if (cfgitem3[item_idx3] != null) {
                        this.mScenesCfgData[item_idx3] = Integer.parseInt(cfgitem3[item_idx3]);
                    }
                    item_idx3++;
                }
                printLog(TAG, "The ver_num is:" + this.mScenesCfgData[0] + "the mask is:" + this.mScenesCfgData[3]);
                this.mContext = this.mPhone.getContext();
                if (this.mContext != null) {
                    String scenesCfgPara = "ver_num=" + this.mScenesCfgData[0] + ";scg_add_speed=" + this.mScenesCfgData[1] + ";scg_add_speed_lowbat=" + this.mScenesCfgData[2] + ";scenes_prohibit_masks=" + this.mScenesCfgData[3];
                    printLog(TAG, "scenesCfgPara is " + scenesCfgPara);
                    Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrScenesPara", scenesCfgPara);
                } else {
                    printLog(TAG, "Context is null!");
                }
            } else if ("apk_cfg".equals(key)) {
                String[] cfgitem4 = strcfg.split("@");
                int i = 0;
                while (i < cfgitem4.length && i < 5) {
                    if (cfgitem4[i] != null) {
                        this.mApkCfgData[i] = cfgitem4[i];
                    }
                    i++;
                }
                printLog(TAG, "The ver_num is:" + this.mApkCfgData[0] + "the restrict is:" + this.mApkCfgData[3] + "the black is:" + this.mApkCfgData[4]);
                this.mContext = this.mPhone.getContext();
                if (this.mContext != null) {
                    String smart5gApkPara = "ver_num=" + this.mApkCfgData[0] + ";scg_add_speed=" + this.mApkCfgData[1] + ";scg_add_speed_lowbat=" + this.mApkCfgData[2] + ";restrict=" + this.mApkCfgData[3] + ";black=" + this.mApkCfgData[4];
                    printLog(TAG, "smart5gApkPara is " + smart5gApkPara);
                    Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrApkPara", smart5gApkPara);
                } else {
                    printLog(TAG, "Context is null!");
                }
            }
        }
    }
}
