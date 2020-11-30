package com.mediatek.internal.telephony.cdma.pluscode;

public class SidMccMnc {
    public int mMccMnc;
    public int mSid;

    public SidMccMnc() {
        this.mSid = -1;
        this.mMccMnc = -1;
    }

    public SidMccMnc(int sid, int mccMnc) {
        this.mSid = sid;
        this.mMccMnc = mccMnc;
    }

    public SidMccMnc(SidMccMnc t) {
        copyFrom(t);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(SidMccMnc t) {
        this.mSid = t.mSid;
        this.mMccMnc = t.mMccMnc;
    }

    public int getSid() {
        return this.mSid;
    }

    public int getMccMnc() {
        return this.mMccMnc;
    }

    public String toString() {
        return "Sid =" + this.mSid + ", MccMnc = " + this.mMccMnc;
    }
}
