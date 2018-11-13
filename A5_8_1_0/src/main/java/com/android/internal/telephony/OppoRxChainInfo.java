package com.android.internal.telephony;

public class OppoRxChainInfo {
    private int mEcio;
    private int mIsRadioTurned;
    private int mPhase;
    private int mRscp;
    private int mRsrp;
    private int mRxPwr;

    public OppoRxChainInfo(int mIsRadioTurned, int mRxPwr, int mEcio, int mRscp, int mRsrp, int mPhase) {
        this.mIsRadioTurned = mIsRadioTurned;
        this.mRxPwr = mRxPwr;
        this.mEcio = mEcio;
        this.mRscp = mRscp;
        this.mRsrp = mRsrp;
        this.mPhase = mPhase;
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
