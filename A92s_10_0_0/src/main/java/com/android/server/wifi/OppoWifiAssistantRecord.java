package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;

public class OppoWifiAssistantRecord {
    private static final String ANT_STR = "ant=";
    private static final String BL_CAP2_STR = "bl_cap2=";
    private static final String BRS_STR = "brs=";
    private static final String BSSID_STR = "bssid=";
    private static final String CAP2_STR = "cap2=";
    private static final String CAP_STR = "cap=";
    private static final String CE_STR = "ce=";
    private static final String CFC_STR = "cfc=";
    private static final String CONKEY_STR = "conkey=";
    private static final String CSC_STR = "csc=";
    private static final String HF_STR = "hf=";
    private static final String HLS_STR = "hls=";
    private static final String INT_STR = "int=";
    private static final String LST_STR = "lst=";
    private static final String MS_STR = "ms=";
    private static final String NFC_STR = "nfc=";
    private static final String NID_STR = "id=";
    private static final String NQL_STR = "nql=";
    private static final String NV2_STR = "nv2=";
    private static final String NV_STR = "nv=";
    private static final String SCORE_STR = "sc=";
    public static final int TYPE_COUNT = 4;
    public long mAccessNetTime;
    public int mAverageSpeed;
    public int mBestRssi;
    public String mBssid;
    public String mConfigkey;
    public boolean mConnExp;
    public int mConnFailCount;
    public int mConnSuccCout;
    public OppoHistoryRecordQueue mHistoryRecordQueue;
    public long mInternetTime;
    public boolean mIs5G;
    public boolean mIsBlacklistCap2;
    public boolean mIsCaptive;
    public boolean mIsCaptive2;
    public boolean mIsHistoryLoss;
    public long mLastuseTime;
    public int mMaxSpeed;
    public int mNetFailCount;
    public int[] mNetQualitys;
    public int mNetid;
    public boolean mNetworkValid;
    public boolean mNetworkValid2;
    public String mPosition;
    public int mRssi;
    public int mScore;
    public WifiConfiguration mWifiConfiguration;

    public OppoWifiAssistantRecord() {
        this.mConfigkey = null;
        this.mBssid = null;
        this.mNetid = -1;
        this.mRssi = WifiConfiguration.INVALID_RSSI;
        this.mBestRssi = WifiConfiguration.INVALID_RSSI;
        this.mConnSuccCout = 0;
        this.mConnFailCount = 0;
        this.mAccessNetTime = 0;
        this.mInternetTime = 0;
        this.mLastuseTime = 0;
        this.mNetQualitys = new int[4];
        int i = 0;
        while (true) {
            int[] iArr = this.mNetQualitys;
            if (i < iArr.length) {
                iArr[i] = -1;
                i++;
            } else {
                this.mScore = 0;
                this.mNetFailCount = 0;
                this.mMaxSpeed = 0;
                this.mAverageSpeed = 0;
                this.mPosition = null;
                this.mIs5G = false;
                this.mNetworkValid = false;
                this.mNetworkValid2 = false;
                this.mConnExp = false;
                this.mIsCaptive = false;
                this.mIsHistoryLoss = false;
                this.mIsCaptive2 = false;
                this.mIsBlacklistCap2 = false;
                this.mWifiConfiguration = null;
                this.mHistoryRecordQueue = null;
                return;
            }
        }
    }

    public OppoWifiAssistantRecord(String configkey, String bssid, int netid, int rssi, int connSuccCout, int connFailCount, long accessNetTime, long lastuseTime, int netFailCount, int maxSpeed, boolean networkValid, WifiConfiguration wcf) {
        this.mConfigkey = configkey;
        this.mBssid = bssid;
        this.mNetid = netid;
        this.mRssi = rssi;
        this.mConnSuccCout = connSuccCout;
        this.mConnFailCount = connFailCount;
        this.mAccessNetTime = accessNetTime;
        this.mLastuseTime = lastuseTime;
        this.mNetQualitys = new int[4];
        int i = 0;
        while (true) {
            int[] iArr = this.mNetQualitys;
            if (i < iArr.length) {
                iArr[i] = -1;
                i++;
            } else {
                this.mNetFailCount = netFailCount;
                this.mMaxSpeed = maxSpeed;
                this.mNetworkValid = networkValid;
                this.mNetworkValid2 = false;
                this.mWifiConfiguration = wcf;
                return;
            }
        }
    }

    public OppoWifiAssistantRecord(String configkey, String bssid, int netid, int rssi, int bestRssi, int connSuccCout, int connFailCount, long accessNetTime, long lastuseTime, int netFailCount, int maxSpeed, int averageSpeed, String position, boolean is5G, boolean networkValid, WifiConfiguration wcf) {
        this.mConfigkey = configkey;
        this.mBssid = bssid;
        this.mNetid = netid;
        this.mRssi = rssi;
        this.mConnSuccCout = connSuccCout;
        this.mConnFailCount = connFailCount;
        this.mAccessNetTime = accessNetTime;
        this.mLastuseTime = lastuseTime;
        this.mNetQualitys = new int[4];
        int i = 0;
        while (true) {
            int[] iArr = this.mNetQualitys;
            if (i < iArr.length) {
                iArr[i] = -1;
                i++;
            } else {
                this.mNetFailCount = netFailCount;
                this.mMaxSpeed = maxSpeed;
                this.mAverageSpeed = averageSpeed;
                this.mPosition = position;
                this.mIs5G = is5G;
                this.mNetworkValid = networkValid;
                this.mNetworkValid2 = false;
                this.mWifiConfiguration = wcf;
                return;
            }
        }
    }

    public OppoWifiAssistantRecord(OppoWifiAssistantRecord mwr) {
        this.mConfigkey = mwr.mConfigkey;
        this.mBssid = mwr.mBssid;
        this.mNetid = mwr.mNetid;
        this.mRssi = mwr.mRssi;
        this.mBestRssi = mwr.mBestRssi;
        this.mConnSuccCout = mwr.mConnSuccCout;
        this.mConnFailCount = mwr.mConnFailCount;
        this.mAccessNetTime = mwr.mAccessNetTime;
        this.mInternetTime = mwr.mInternetTime;
        this.mLastuseTime = mwr.mLastuseTime;
        this.mNetQualitys = new int[4];
        int i = 0;
        while (true) {
            int[] iArr = this.mNetQualitys;
            if (i < iArr.length) {
                iArr[i] = mwr.mNetQualitys[i];
                i++;
            } else {
                this.mScore = mwr.mScore;
                this.mNetFailCount = mwr.mNetFailCount;
                this.mMaxSpeed = mwr.mMaxSpeed;
                this.mAverageSpeed = mwr.mAverageSpeed;
                this.mPosition = mwr.mPosition;
                this.mIs5G = mwr.mIs5G;
                this.mNetworkValid = mwr.mNetworkValid;
                this.mNetworkValid2 = mwr.mNetworkValid2;
                this.mConnExp = mwr.mConnExp;
                this.mIsCaptive = mwr.mIsCaptive;
                this.mIsHistoryLoss = mwr.mIsHistoryLoss;
                this.mIsCaptive2 = mwr.mIsCaptive2;
                this.mIsBlacklistCap2 = mwr.mIsBlacklistCap2;
                this.mWifiConfiguration = mwr.mWifiConfiguration;
                this.mHistoryRecordQueue = mwr.mHistoryRecordQueue;
                return;
            }
        }
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        WifiConfiguration wifiConfiguration = this.mWifiConfiguration;
        if (wifiConfiguration != null) {
            sbuf.append(wifiConfiguration.networkId);
        } else {
            sbuf.append(-1);
        }
        sbuf.append("\t");
        sbuf.append(this.mConfigkey + "\t");
        sbuf.append(this.mBssid + "\t");
        sbuf.append(this.mBestRssi + "\t");
        sbuf.append(this.mConnSuccCout + "\t");
        sbuf.append(this.mConnFailCount + "\t");
        sbuf.append(this.mAccessNetTime + "\t");
        sbuf.append(this.mInternetTime + "\t");
        sbuf.append(this.mLastuseTime + "\t");
        sbuf.append(this.mNetFailCount + "\t");
        sbuf.append(this.mMaxSpeed + "\t");
        sbuf.append(this.mIs5G + "\t");
        sbuf.append(this.mNetworkValid + "\t");
        sbuf.append(this.mConnExp + "\t");
        sbuf.append(this.mScore + "\t");
        int i = 0;
        while (true) {
            int[] iArr = this.mNetQualitys;
            if (i >= iArr.length) {
                return sbuf.toString();
            }
            sbuf.append(iArr[i]);
            if (i + 1 != this.mNetQualitys.length) {
                sbuf.append(",");
            }
            i++;
        }
    }

    public String toTagString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(NID_STR);
        WifiConfiguration wifiConfiguration = this.mWifiConfiguration;
        if (wifiConfiguration != null) {
            sbuf.append(wifiConfiguration.networkId);
        } else {
            sbuf.append(-1);
        }
        sbuf.append("\t");
        sbuf.append(CONKEY_STR + this.mConfigkey + "\t");
        sbuf.append(BSSID_STR + this.mBssid + "\t");
        sbuf.append(BRS_STR + this.mBestRssi + "\t");
        sbuf.append(CSC_STR + this.mConnSuccCout + "\t");
        sbuf.append(CFC_STR + this.mConnFailCount + "\t");
        sbuf.append(ANT_STR + this.mAccessNetTime + "\t");
        sbuf.append(INT_STR + this.mInternetTime + "\t");
        sbuf.append(LST_STR + this.mLastuseTime + "\t");
        sbuf.append(NFC_STR + this.mNetFailCount + "\t");
        sbuf.append(MS_STR + this.mMaxSpeed + "\t");
        sbuf.append(HF_STR + this.mIs5G + "\t");
        sbuf.append(NV_STR + this.mNetworkValid + "\t");
        sbuf.append(NV2_STR + this.mNetworkValid2 + "\t");
        sbuf.append(CE_STR + this.mConnExp + "\t");
        sbuf.append(SCORE_STR + this.mScore + "\t");
        sbuf.append(NQL_STR);
        int i = 0;
        while (true) {
            int[] iArr = this.mNetQualitys;
            if (i >= iArr.length) {
                break;
            }
            sbuf.append(iArr[i]);
            if (i + 1 != this.mNetQualitys.length) {
                sbuf.append(",");
            }
            i++;
        }
        sbuf.append("\t");
        sbuf.append(CAP_STR + this.mIsCaptive + "\t");
        if (this.mHistoryRecordQueue != null) {
            sbuf.append(HLS_STR + this.mHistoryRecordQueue.toStorageString() + "\t");
        }
        sbuf.append(CAP2_STR + this.mIsCaptive2 + "\t");
        StringBuilder sb = new StringBuilder();
        sb.append(BL_CAP2_STR);
        sb.append(this.mIsBlacklistCap2);
        sbuf.append(sb.toString());
        return sbuf.toString();
    }
}
