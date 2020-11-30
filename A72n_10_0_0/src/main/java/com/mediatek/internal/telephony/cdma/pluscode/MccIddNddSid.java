package com.mediatek.internal.telephony.cdma.pluscode;

public class MccIddNddSid {
    public String mCc;
    public String mIdd;
    public int mMcc;
    public String mNdd;
    public int mSidMax;
    public int mSidMin;

    public MccIddNddSid() {
        this.mMcc = -1;
        this.mCc = null;
        this.mSidMin = -1;
        this.mSidMax = -1;
        this.mIdd = null;
        this.mNdd = null;
    }

    public MccIddNddSid(int mcc, String cc, int sidmin, int sidmax, String idd, String ndd) {
        this.mMcc = mcc;
        this.mCc = cc;
        this.mSidMin = sidmin;
        this.mSidMax = sidmax;
        this.mIdd = idd;
        this.mNdd = ndd;
    }

    public MccIddNddSid(MccIddNddSid t) {
        copyFrom(t);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(MccIddNddSid t) {
        this.mMcc = t.mMcc;
        this.mCc = t.mCc;
        this.mSidMin = t.mSidMin;
        this.mSidMax = t.mSidMax;
        this.mIdd = t.mIdd;
        this.mNdd = t.mNdd;
    }

    public int getMcc() {
        return this.mMcc;
    }

    public String getCc() {
        return this.mCc;
    }

    public int getSidMin() {
        return this.mSidMin;
    }

    public int getSidMax() {
        return this.mSidMax;
    }

    public String getIdd() {
        return this.mIdd;
    }

    public String getNdd() {
        return this.mNdd;
    }

    public String toString() {
        return "Mcc =" + this.mMcc + ", Cc = " + this.mCc + ", SidMin = " + this.mSidMin + ", SidMax = " + this.mSidMax + ", Idd = " + this.mIdd + ", Ndd = " + this.mNdd;
    }
}
