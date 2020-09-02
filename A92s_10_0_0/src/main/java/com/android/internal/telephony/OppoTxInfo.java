package com.android.internal.telephony;

public class OppoTxInfo {
    private int mIsInTraffic;
    private int mTxPwr;

    public OppoTxInfo(int mIsInTraffic2, int mTxPwr2) {
        this.mIsInTraffic = mIsInTraffic2;
        this.mTxPwr = mTxPwr2;
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
