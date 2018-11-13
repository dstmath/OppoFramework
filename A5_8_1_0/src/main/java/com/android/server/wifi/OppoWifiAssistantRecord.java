package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;

public class OppoWifiAssistantRecord {
    private static final String ANT_STR = "ant=";
    private static final String BRS_STR = "brs=";
    private static final String BSSID_STR = "bssid=";
    private static final String CAP_STR = "cap=";
    private static final String CE_STR = "ce=";
    private static final String CFC_STR = "cfc=";
    private static final String CONKEY_STR = "conkey=";
    private static final String CSC_STR = "csc=";
    private static final String HF_STR = "hf=";
    private static final String INT_STR = "int=";
    private static final String LST_STR = "lst=";
    private static final String MS_STR = "ms=";
    private static final String NFC_STR = "nfc=";
    private static final String NID_STR = "id=";
    private static final String NQL_STR = "nql=";
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
    public long mInternetTime;
    public boolean mIs5G;
    public boolean mIsCaptive;
    public long mLastuseTime;
    public int mMaxSpeed;
    public int mNetFailCount;
    public int[] mNetQualitys;
    public int mNetid;
    public boolean mNetworkValid;
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
        for (int i = 0; i < this.mNetQualitys.length; i++) {
            this.mNetQualitys[i] = -1;
        }
        this.mScore = 0;
        this.mNetFailCount = 0;
        this.mMaxSpeed = 0;
        this.mAverageSpeed = 0;
        this.mPosition = null;
        this.mIs5G = false;
        this.mNetworkValid = false;
        this.mConnExp = false;
        this.mIsCaptive = false;
        this.mWifiConfiguration = null;
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
        for (int i = 0; i < this.mNetQualitys.length; i++) {
            this.mNetQualitys[i] = -1;
        }
        this.mNetFailCount = netFailCount;
        this.mMaxSpeed = maxSpeed;
        this.mNetworkValid = networkValid;
        this.mWifiConfiguration = wcf;
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
        for (int i = 0; i < this.mNetQualitys.length; i++) {
            this.mNetQualitys[i] = -1;
        }
        this.mNetFailCount = netFailCount;
        this.mMaxSpeed = maxSpeed;
        this.mAverageSpeed = averageSpeed;
        this.mPosition = position;
        this.mIs5G = is5G;
        this.mNetworkValid = networkValid;
        this.mWifiConfiguration = wcf;
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
        for (int i = 0; i < this.mNetQualitys.length; i++) {
            this.mNetQualitys[i] = mwr.mNetQualitys[i];
        }
        this.mScore = mwr.mScore;
        this.mNetFailCount = mwr.mNetFailCount;
        this.mMaxSpeed = mwr.mMaxSpeed;
        this.mAverageSpeed = mwr.mAverageSpeed;
        this.mPosition = mwr.mPosition;
        this.mIs5G = mwr.mIs5G;
        this.mNetworkValid = mwr.mNetworkValid;
        this.mConnExp = mwr.mConnExp;
        this.mIsCaptive = mwr.mIsCaptive;
        this.mWifiConfiguration = mwr.mWifiConfiguration;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        if (this.mWifiConfiguration != null) {
            sbuf.append(this.mWifiConfiguration.networkId);
        } else {
            sbuf.append(-1);
        }
        sbuf.append("\t");
        sbuf.append(this.mConfigkey).append("\t");
        sbuf.append(this.mBssid).append("\t");
        sbuf.append(this.mBestRssi).append("\t");
        sbuf.append(this.mConnSuccCout).append("\t");
        sbuf.append(this.mConnFailCount).append("\t");
        sbuf.append(this.mAccessNetTime).append("\t");
        sbuf.append(this.mInternetTime).append("\t");
        sbuf.append(this.mLastuseTime).append("\t");
        sbuf.append(this.mNetFailCount).append("\t");
        sbuf.append(this.mMaxSpeed).append("\t");
        sbuf.append(this.mIs5G).append("\t");
        sbuf.append(this.mNetworkValid).append("\t");
        sbuf.append(this.mConnExp).append("\t");
        sbuf.append(this.mScore).append("\t");
        for (int i = 0; i < this.mNetQualitys.length; i++) {
            sbuf.append(this.mNetQualitys[i]);
            if (i + 1 != this.mNetQualitys.length) {
                sbuf.append(",");
            }
        }
        return sbuf.toString();
    }

    public String toTagString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(NID_STR);
        if (this.mWifiConfiguration != null) {
            sbuf.append(this.mWifiConfiguration.networkId);
        } else {
            sbuf.append(-1);
        }
        sbuf.append("\t");
        sbuf.append(CONKEY_STR).append(this.mConfigkey).append("\t");
        sbuf.append(BSSID_STR).append(this.mBssid).append("\t");
        sbuf.append(BRS_STR).append(this.mBestRssi).append("\t");
        sbuf.append(CSC_STR).append(this.mConnSuccCout).append("\t");
        sbuf.append(CFC_STR).append(this.mConnFailCount).append("\t");
        sbuf.append(ANT_STR).append(this.mAccessNetTime).append("\t");
        sbuf.append(INT_STR).append(this.mInternetTime).append("\t");
        sbuf.append(LST_STR).append(this.mLastuseTime).append("\t");
        sbuf.append(NFC_STR).append(this.mNetFailCount).append("\t");
        sbuf.append(MS_STR).append(this.mMaxSpeed).append("\t");
        sbuf.append(HF_STR).append(this.mIs5G).append("\t");
        sbuf.append(NV_STR).append(this.mNetworkValid).append("\t");
        sbuf.append(CE_STR).append(this.mConnExp).append("\t");
        sbuf.append(SCORE_STR).append(this.mScore).append("\t");
        sbuf.append(NQL_STR);
        for (int i = 0; i < this.mNetQualitys.length; i++) {
            sbuf.append(this.mNetQualitys[i]);
            if (i + 1 != this.mNetQualitys.length) {
                sbuf.append(",");
            }
        }
        sbuf.append("\t");
        sbuf.append(CAP_STR).append(this.mIsCaptive);
        return sbuf.toString();
    }
}
