package com.oppo.internal.telephony.emergency;

import android.util.Log;

public class EccEntry implements Comparable<EccEntry> {
    public static final String ECC_ATTR_CAT_LEN = "Cat_len";
    public static final String ECC_ATTR_CAT_VAL = "Cat_val";
    public static final String ECC_ATTR_ECC = "Ecc";
    public static final String ECC_ATTR_MASK = "Mask";
    public static final String ECC_ATTR_MCC = "Mcc";
    public static final String ECC_ATTR_MNC = "Mnc";
    public static final String ECC_ATTR_MODE = "Mode";
    public static final String ECC_ATTR_SPECIAL = "Special";
    public static final String ECC_ENTRY_TAG = "EccEntry";
    private int mCatVal;
    private int mCatlen;
    private String mEcc;
    private int mMask;
    private int mMcc;
    private int mMnc;
    private int mMode;
    private int mSpecial;

    public EccEntry() {
    }

    public EccEntry(int mcc, int mnc, int mask, String ecc, int catlen, int catval, int mode, int special) {
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mMask = mask;
        this.mEcc = ecc;
        this.mCatlen = catlen;
        this.mCatVal = catval;
        this.mMode = mode;
        this.mSpecial = special;
    }

    public int compareTo(EccEntry o) {
        return 0;
    }

    public void setMcc(int mcc) {
        this.mMcc = mcc;
    }

    public int getMcc() {
        return this.mMcc;
    }

    public void setMnc(int mnc) {
        this.mMnc = mnc;
    }

    public int getMnc() {
        return this.mMnc;
    }

    public void setMask(int mask) {
        this.mMask = mask;
    }

    public int getMask() {
        return this.mMask;
    }

    public void setEcc(String ecc) {
        this.mEcc = ecc;
    }

    public String getEcc() {
        return this.mEcc;
    }

    public void setCatlen(int catlen) {
        this.mCatlen = catlen;
    }

    public int getCatlen() {
        return this.mCatlen;
    }

    public void setCatval(int catval) {
        this.mCatVal = catval;
    }

    public int getCatval() {
        return this.mCatVal;
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    public int getMode() {
        return this.mMode;
    }

    public void setSpecial(int special) {
        this.mSpecial = special;
    }

    public int getSpecial() {
        return this.mSpecial;
    }

    public String toString() {
        return "mMCC = " + this.mMcc + ", mMnc = " + this.mMnc + " , mMask = " + this.mMask + " , mEcc = " + this.mEcc + " , mCatlen = " + this.mCatlen + " , mCatVal = " + this.mCatVal + " , mMode = " + this.mMode + " , mSpecial = " + this.mSpecial;
    }

    private static void log(String msg) {
        Log.d(ECC_ENTRY_TAG, msg);
    }
}
