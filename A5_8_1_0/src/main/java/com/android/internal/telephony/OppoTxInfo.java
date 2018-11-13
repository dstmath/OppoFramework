package com.android.internal.telephony;

public class OppoTxInfo {
    private int mIsInTraffic;
    private int mTxPwr;

    public OppoTxInfo(int mIsInTraffic, int mTxPwr) {
        this.mIsInTraffic = mIsInTraffic;
        this.mTxPwr = mTxPwr;
    }

    public int getIsInTraffic() {
        return this.mIsInTraffic;
    }

    public int getTxPwr() {
        return this.mTxPwr;
    }

    public String toString() {
        return "mIsInTraffic=" + this.mIsInTraffic + ", mTxPwr=" + this.mTxPwr;
    }
}
