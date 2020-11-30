package com.android.internal.telephony;

public class OppoNcellInfo {
    private int mArfcn;
    private int mRat = -1;
    private int mRssi;

    public OppoNcellInfo(int rat, int arfcn, int rssi) {
        this.mRat = rat;
        this.mArfcn = arfcn;
        this.mRssi = rssi;
    }

    public int getRat() {
        return this.mRat;
    }

    public int getArfcn() {
        return this.mArfcn;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public String toString() {
        return "[" + this.mRat + " " + this.mArfcn + " " + this.mRssi + "]";
    }
}
