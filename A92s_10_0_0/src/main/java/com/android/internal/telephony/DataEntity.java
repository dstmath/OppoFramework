package com.android.internal.telephony;

public class DataEntity {
    public int band = -1;
    public int gsm = -90;
    public boolean isSwitch = true;
    public int lte = -110;
    public RAT rat = RAT.UNKOWN;
    public int wcdma = -100;

    public enum RAT {
        UNKOWN,
        GSM,
        WCDMA,
        LTE
    }

    public DataEntity() {
    }

    public DataEntity(boolean isClear) {
        if (isClear) {
            this.gsm = -1;
            this.wcdma = -1;
            this.lte = -1;
            this.band = -1;
        }
    }

    public String toString() {
        return "isSwitch:" + this.isSwitch + ",gsm:" + this.gsm + ",wcdma:" + this.wcdma + ",lte:" + this.lte + ",band:" + this.band;
    }
}
