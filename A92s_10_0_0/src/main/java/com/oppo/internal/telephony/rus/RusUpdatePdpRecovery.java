package com.oppo.internal.telephony.rus;

import android.content.Context;
import android.provider.Settings;
import com.android.internal.telephony.OppoRlog;
import com.oppo.internal.telephony.recovery.OFastRecoveryConfig;
import com.oppo.internal.telephony.recovery.OppoRecoveryTacConfig;
import java.util.HashMap;

public final class RusUpdatePdpRecovery extends RusBase {
    private static final String TAG = "RusUpdatePdpRecovery";
    int[] mCfgData = new int[this.mConfigParaNameArray.length];
    String[] mConfigParaNameArray = {"feature_enable", "min_rsrp", "rx_threshold_speed", "tx_threshold_speed", "rx_slow_speed", "tx_slow_speed", "ip_speed_multiple", "tcp_min_tx_packet_speed", "max_count_per_day", "check_interval", "dns_fail_count", "action_interval", "rx0_count_threshold", "action_bypass", "dns_fail_intvl", "txrx_fail_intvl", "radio_power_intvl", "close_5gnr_enable", "deatch_enable", "radio_reset_enable", "dns_effective_time", "fg_recovery_max_count", "fg_r0count", "fg_action_intvl", "txrx_check_count", "rx0count_invalid_th", "cell_blacklist_5g_timeout", "cell_blacklist_check_intvl", "tac_check_mode", "get_datacall_list_enable", "clean_connections_enable", "close_5g_enable"};
    private Context mContext;
    String mTacCfgName = "tac_cfg_list";

    public RusUpdatePdpRecovery() {
        this.mRebootExecute = false;
        this.mContext = this.mPhone.getContext();
    }

    private boolean configValidCheck(String str) {
        OFastRecoveryConfig cfg = OFastRecoveryConfig.parseConfig(str);
        if (cfg != null && cfg.isConfigValid()) {
            return true;
        }
        OppoRlog.Rlog.w(TAG, "configValidCheck failed!:" + cfg);
        return false;
    }

    private String getPdpParamConfig(HashMap<String, String> mRusData) {
        int index = 0;
        while (true) {
            String[] strArr = this.mConfigParaNameArray;
            if (index >= strArr.length) {
                break;
            }
            String str = mRusData.get(strArr[index]);
            if (str != null) {
                this.mCfgData[index] = Integer.parseInt(str);
            }
            index++;
        }
        String config = this.mCfgData[0] + "," + this.mCfgData[1] + "," + this.mCfgData[2] + "," + this.mCfgData[3] + "," + this.mCfgData[4] + "," + this.mCfgData[5] + "," + this.mCfgData[6] + "," + this.mCfgData[7] + "," + this.mCfgData[8] + "," + this.mCfgData[9] + "," + this.mCfgData[10] + "," + this.mCfgData[11] + "," + this.mCfgData[12] + "," + this.mCfgData[13] + "," + this.mCfgData[14] + "," + this.mCfgData[15] + "," + this.mCfgData[16] + "," + this.mCfgData[17] + "," + this.mCfgData[18] + "," + this.mCfgData[19] + "," + this.mCfgData[20] + "," + this.mCfgData[21] + "," + this.mCfgData[22] + "," + this.mCfgData[23] + "," + this.mCfgData[24] + "," + this.mCfgData[25] + "," + this.mCfgData[26] + "," + this.mCfgData[27] + "," + this.mCfgData[28] + "," + this.mCfgData[29] + "," + this.mCfgData[30] + "," + this.mCfgData[31];
        printLog(TAG, "getPdpParamConfig config is : " + config);
        if (configValidCheck(config)) {
            return config;
        }
        OppoRlog.Rlog.d(TAG, "getPdpParamConfig config check failed!");
        return null;
    }

    private String getTacCfg(HashMap<String, String> mRusData) {
        String str = mRusData.get(this.mTacCfgName);
        if (str != null && OppoRecoveryTacConfig.parseConfig(str) != null) {
            return str;
        }
        OppoRlog.Rlog.d(TAG, "mTacCfgName config failed!!" + str);
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> mRusData, boolean isReboot) {
        if (mRusData == null) {
            try {
                printLog(TAG, "defaultValue is null ");
            } catch (Exception e) {
                e.printStackTrace();
                OppoRlog.Rlog.e(TAG, "executeRusCommand failed!" + e.getMessage());
            }
        } else if (mRusData.containsKey("feature_enable")) {
            String pdpConfig = getPdpParamConfig(mRusData);
            String tacConfig = getTacCfg(mRusData);
            if (pdpConfig != null) {
                if (tacConfig != null) {
                    Settings.Global.putString(this.mContext.getContentResolver(), OFastRecoveryConfig.SYS_CONFIG_URI, pdpConfig);
                    Settings.Global.putString(this.mContext.getContentResolver(), OppoRecoveryTacConfig.SYS_TAC_URI, tacConfig);
                    OppoRlog.Rlog.d(TAG, "executeRusCommand succ! pdpcfg:" + pdpConfig + "; taccfg:" + tacConfig);
                    return;
                }
            }
            OppoRlog.Rlog.d(TAG, "executeRusCommand config check failed!");
        } else {
            OppoRlog.Rlog.e(TAG, "executeRusCommand config donot contain feature_enable");
        }
    }
}
