package com.mediatek.internal.telephony.cdma.pluscode;

public class MccSidLtmOff {
    public static final int LTM_OFF_INVALID = 100;
    public int mLtmOffMax;
    public int mLtmOffMin;
    public int mMcc;
    public int mSid;

    public MccSidLtmOff() {
        this.mMcc = -1;
        this.mSid = -1;
        this.mLtmOffMin = 100;
        this.mLtmOffMax = 100;
    }

    public MccSidLtmOff(int mcc, int sid, int ltmOffMin, int ltmOffMax) {
        this.mMcc = mcc;
        this.mSid = sid;
        this.mLtmOffMin = ltmOffMin;
        this.mLtmOffMax = ltmOffMax;
    }

    public MccSidLtmOff(MccSidLtmOff t) {
        copyFrom(t);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(MccSidLtmOff t) {
        this.mMcc = t.mMcc;
        this.mSid = t.mSid;
        this.mLtmOffMin = t.mLtmOffMin;
        this.mLtmOffMax = t.mLtmOffMax;
    }

    public int getMcc() {
        return this.mMcc;
    }

    public int getSid() {
        return this.mSid;
    }

    public int getLtmOffMin() {
        return this.mLtmOffMin;
    }

    public int getLtmOffMax() {
        return this.mLtmOffMax;
    }
}
