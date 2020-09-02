package com.android.internal.telephony;

public class OppoRxChainInfo {
    private int mEcio;
    private int mIsRadioTurned;
    private int mPhase;
    private int mRscp;
    private int mRsrp;
    private int mRxPwr;

    public OppoRxChainInfo(int mIsRadioTurned2, int mRxPwr2, int mEcio2, int mRscp2, int mRsrp2, int mPhase2) {
        this.mIsRadioTurned = mIsRadioTurned2;
        this.mRxPwr = mRxPwr2;
        this.mEcio = mEcio2;
        this.mRscp = mRscp2;
        this.mRsrp = mRsrp2;
        this.mPhase = mPhase2;
    }

    public int getIsRadioTurned() {
        return this.mIsRadioTurned;
    }

    public int getRxPwr() {
        return this.mRxPwr;
    }

    public int getEcio() {
        return this.mEcio;
    }

    public int getRscp() {
        return this.mRscp;
    }

    public int getRsrp() {
        return this.mRsrp;
    }

    public int getPhase() {
        return this.mPhase;
    }

    public String toString() {
        return "mIsRadioTurned=" + this.mIsRadioTurned + ", mRxPwr=" + this.mRxPwr + ", mEcio=" + this.mEcio + ", mRscp=" + this.mRscp + ", mRsrp=" + this.mRsrp + ", mPhase=" + this.mPhase;
    }
}
