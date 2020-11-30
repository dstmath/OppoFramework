package com.mediatek.internal.telephony.phb;

import android.os.Parcel;

public class PBMemStorage {
    public static final int INT_NOT_SET = -1;
    public static final String STRING_NOT_SET = "";
    private String mStorage = "";
    private int mTotal = -1;
    private int mUsed = -1;

    public static PBMemStorage createFromParcel(Parcel source) {
        PBMemStorage p = new PBMemStorage();
        p.mStorage = source.readString();
        p.mUsed = source.readInt();
        p.mTotal = source.readInt();
        return p;
    }

    public String toString() {
        return super.toString() + ";storage: " + this.mStorage + ",used: " + this.mUsed + ",total:" + this.mTotal;
    }

    public void setStorage(String sStorage) {
        this.mStorage = sStorage;
    }

    public void setUsed(int iUsed) {
        this.mUsed = iUsed;
    }

    public void setTotal(int iTotal) {
        this.mTotal = iTotal;
    }

    public String getStorage() {
        return this.mStorage;
    }

    public int getUsed() {
        return this.mUsed;
    }

    public int getTotal() {
        return this.mTotal;
    }
}
