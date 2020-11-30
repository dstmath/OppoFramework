package com.oppo.internal.telephony.recovery;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.text.TextUtils;

public class OFastRecoveryConfig {
    private static final String DEFAULT_CONFIG = "0,-110,3000,1000,100,1000,2,3,8,3,3,30,14,0,300,300,86400,1,1,1,90,10,4,6,7,6,7200,300,0,1,1,1";
    public static final String PERSIST_CONFIG_KEY = "persist.oppo.network.pdp_recovery.config";
    public static final String SYS_CONFIG_URI = "oppo_pdp_recovery";
    public static final int TAC_MODE_FORE_USE = 2;
    public static final int TAC_MODE_NOT_USE = 0;
    public static final int TAC_MODE_USE_LIST = 1;
    private static final String TAG = "OFastRecoveryConfig";
    public boolean enable = false;
    public int m5GActionIntvl = 6;
    public int m5GRecoveryMaxCount = 10;
    public int m5GRx0Count = 4;
    public int m5gCellBlacklistTimeout = 7200;
    public int m5gCellCheckIntvl = 300;
    public boolean mActionByPass = false;
    public int mActionIntvl = 30;
    public long mCheckIntval = 3;
    public boolean mCleanAllConnectionEnable = true;
    public boolean mCloset5gEnable = true;
    public boolean mDetachAttachEnable = true;
    public int mDnsCheckIntvl = 300;
    public int mDnsEffectiveTime = 90;
    public int mDnsFailCount = 3;
    public boolean mGetDataCallListEnable = true;
    public int mIpSpeedMultiple = 2;
    public int mMaxCountPerDay = 8;
    public long mMinRsrp = -110;
    public boolean mRadioPowerEnable = true;
    public int mRadioPowerIntvl = 86400;
    public boolean mRemove5GNrEnable = true;
    public int mRx0CountInvalidTh = 6;
    public int mRx0CountThreshold = 14;
    public long mRxSlowSpeed = 100;
    public long mRxThresholdSpeed = 3000;
    public long mTcpMinTxPacketSpeed = 3;
    public int mTxRxCheckCount = 7;
    public long mTxSlowSpeed = 1000;
    public long mTxThresholdSpeed = 1000;
    public int mTxrxCheckIntvl = 300;
    public int mUseTacListMode = 0;

    public OFastRecoveryConfig() {
        parseDefaultConfig(this, DEFAULT_CONFIG);
    }

    private static void parseDefaultConfig(OFastRecoveryConfig config, String str) {
        String[] split = str.split(",");
        boolean z = false;
        config.enable = Integer.parseInt(split[0]) == 1;
        config.mMinRsrp = Long.parseLong(split[1]);
        config.mRxThresholdSpeed = Long.parseLong(split[2]);
        config.mTxThresholdSpeed = Long.parseLong(split[3]);
        config.mRxSlowSpeed = Long.parseLong(split[4]);
        config.mTxSlowSpeed = Long.parseLong(split[5]);
        config.mIpSpeedMultiple = Integer.parseInt(split[6]);
        config.mTcpMinTxPacketSpeed = Long.parseLong(split[7]);
        config.mMaxCountPerDay = Integer.parseInt(split[8]);
        config.mCheckIntval = Long.parseLong(split[9]);
        config.mDnsFailCount = Integer.parseInt(split[10]);
        config.mActionIntvl = Integer.parseInt(split[11]);
        config.mRx0CountThreshold = Integer.parseInt(split[12]);
        config.mActionByPass = Integer.parseInt(split[13]) == 1;
        config.mDnsCheckIntvl = Integer.parseInt(split[14]);
        config.mTxrxCheckIntvl = Integer.parseInt(split[15]);
        config.mRadioPowerIntvl = Integer.parseInt(split[16]);
        config.mRemove5GNrEnable = Integer.parseInt(split[17]) == 1;
        config.mDetachAttachEnable = Integer.parseInt(split[18]) == 1;
        config.mRadioPowerEnable = Integer.parseInt(split[19]) == 1;
        config.mDnsEffectiveTime = Integer.parseInt(split[20]);
        config.m5GRecoveryMaxCount = Integer.parseInt(split[21]);
        config.m5GRx0Count = Integer.parseInt(split[22]);
        config.m5GActionIntvl = Integer.parseInt(split[23]);
        config.mTxRxCheckCount = Integer.parseInt(split[24]);
        config.mRx0CountInvalidTh = Integer.parseInt(split[25]);
        config.m5gCellBlacklistTimeout = Integer.parseInt(split[26]);
        config.m5gCellCheckIntvl = Integer.parseInt(split[27]);
        config.mUseTacListMode = Integer.parseInt(split[28]);
        config.mGetDataCallListEnable = Integer.parseInt(split[29]) == 1;
        config.mCleanAllConnectionEnable = Integer.parseInt(split[30]) == 1;
        if (Integer.parseInt(split[31]) == 1) {
            z = true;
        }
        config.mCloset5gEnable = z;
    }

    public String toString() {
        return "OFastRecoveryConfig{enable=" + this.enable + ", mMinRsrp=" + this.mMinRsrp + ", mRxThresholdSpeed=" + this.mRxThresholdSpeed + ", mTxThresholdSpeed=" + this.mTxThresholdSpeed + ", mRxSlowSpeed=" + this.mRxSlowSpeed + ", mTxSlowSpeed=" + this.mTxSlowSpeed + ", mIpSpeedMultiple=" + this.mIpSpeedMultiple + ", mTcpMinTxPacketSpeed=" + this.mTcpMinTxPacketSpeed + ", mMaxCountPerDay=" + this.mMaxCountPerDay + ", mCheckIntval=" + this.mCheckIntval + ", mDnsFailCount=" + this.mDnsFailCount + ", mActionIntvl=" + this.mActionIntvl + ", mRx0CountThreshold=" + this.mRx0CountThreshold + ", mActionByPass=" + this.mActionByPass + ", mDnsCheckIntvl=" + this.mDnsCheckIntvl + ", mTxrxCheckIntvl=" + this.mTxrxCheckIntvl + ", mRadioPowerIntvl=" + this.mRadioPowerIntvl + ", mRemove5GNrEnable=" + this.mRemove5GNrEnable + ", mDetachAttachEnable=" + this.mDetachAttachEnable + ", mRadioPowerEnable=" + this.mRadioPowerEnable + ", mDnsEffectiveTime=" + this.mDnsEffectiveTime + ", m5GRecoveryMaxCount=" + this.m5GRecoveryMaxCount + ", m5GRx0Count=" + this.m5GRx0Count + ", m5GActionIntvl=" + this.m5GActionIntvl + ", mTxRxCheckCount=" + this.mTxRxCheckCount + ", mRx0CountInvalidTh=" + this.mRx0CountInvalidTh + ", m5gCellBlacklistTimeout=" + this.m5gCellBlacklistTimeout + ", m5gCellCheckIntvl=" + this.m5gCellCheckIntvl + ", mUseTacListMode=" + this.mUseTacListMode + ", mGetDataCallListEnable=" + this.mGetDataCallListEnable + ", mCleanAllConnectionEnable=" + this.mCleanAllConnectionEnable + ", mCloset5gEnable=" + this.mCloset5gEnable + '}';
    }

    public boolean isConfigValid() {
        int i;
        if (this.mRxThresholdSpeed < 0 || this.mTxThresholdSpeed < 0 || this.mRxSlowSpeed < 0 || this.mTxSlowSpeed < 0 || this.mIpSpeedMultiple <= 0 || this.mTcpMinTxPacketSpeed < 0 || this.mMaxCountPerDay < 1 || this.mCheckIntval < 1 || this.mDnsFailCount < 1 || this.mActionIntvl < 1 || this.mRx0CountThreshold < 1 || this.mDnsCheckIntvl < 1 || this.mTxrxCheckIntvl < 1 || this.mRadioPowerIntvl < 1 || this.mDnsEffectiveTime < 1 || this.m5GRecoveryMaxCount < 0 || this.m5GRx0Count < 1 || this.m5GActionIntvl < 1 || this.mTxRxCheckCount < 1 || this.mRx0CountInvalidTh < 1 || this.m5gCellBlacklistTimeout < 100 || this.m5gCellCheckIntvl < 10 || (i = this.mUseTacListMode) < 0 || i > 2) {
            return false;
        }
        return true;
    }

    public static OFastRecoveryConfig parseConfig(String str) {
        try {
            OFastRecoveryConfig config = new OFastRecoveryConfig();
            parseDefaultConfig(config, str);
            if (config.isConfigValid()) {
                return config;
            }
            throw new Exception("OFastRecoveryConfig config invalid:" + config);
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "parseConfig failed" + e.getMessage());
            return null;
        }
    }

    private static void parsePersistConfig(OFastRecoveryConfig config, String str) {
        try {
            Rlog.d(TAG, "parsePersistConfig: str:" + str);
            String[] split = str.split(",");
            if (split.length >= 1) {
                boolean enable2 = false;
                if (Integer.parseInt(split[0]) == 1) {
                    enable2 = true;
                }
                config.enable = enable2;
                int mode = Integer.parseInt(split[1]);
                if (mode >= 0 && mode <= 2) {
                    config.mUseTacListMode = mode;
                }
                Rlog.d(TAG, "parsePersistConfig update result: " + config);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "parsePersistConfig failed!");
        }
    }

    public String genPersistConfigStr() {
        return (this.enable ? 1 : 0) + "," + this.mUseTacListMode;
    }

    public static OFastRecoveryConfig getSystemConfig(Context context) {
        OFastRecoveryConfig tmpCfg;
        OFastRecoveryConfig cfg = new OFastRecoveryConfig();
        try {
            String configStr = Settings.Global.getString(context.getContentResolver(), SYS_CONFIG_URI);
            if (!TextUtils.isEmpty(configStr) && (tmpCfg = parseConfig(configStr)) != null) {
                cfg = tmpCfg;
            }
            String configStr2 = SystemProperties.get(PERSIST_CONFIG_KEY, "");
            if (!TextUtils.isEmpty(configStr2)) {
                parsePersistConfig(cfg, configStr2);
            }
            return cfg;
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "initRecoveryConfig failed " + e.getMessage());
            return cfg;
        }
    }
}
