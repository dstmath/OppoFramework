package com.oppo.internal.telephony.explock.util;

import android.text.TextUtils;

public class DeviceLockData {
    public static final int OP_INDEX = 0;
    public static final int TYPE_SIZE = 9;
    private String mFirstBindTime;
    private String mLastBindTime;
    private String mLockedDays;
    private String mLockedICCID;
    private String mLockedIMSI;
    private String mLockedOperator;
    private String mLockedStatus;
    private String mUnlockDate;

    public void setLockedOperator(String lockedOperator) {
        this.mLockedOperator = lockedOperator;
    }

    public void setLockedState(String lockedStatus) {
        this.mLockedStatus = lockedStatus;
    }

    public void setLockedIMSI(String lockedIMSI) {
        this.mLockedIMSI = lockedIMSI;
    }

    public void setContractDays(String lockedDays) {
        this.mLockedDays = lockedDays;
    }

    public void setFirstBindTime(String firstBindTime) {
        this.mFirstBindTime = firstBindTime;
    }

    public void setLockedICCID(String lockedICCID) {
        this.mLockedICCID = lockedICCID;
    }

    public void setLastBindTime(String lastBindTime) {
        this.mLastBindTime = lastBindTime;
    }

    public void setUnlockDate(String unlockDate) {
        this.mUnlockDate = unlockDate;
    }

    public String getLockedOperator() {
        return this.mLockedOperator;
    }

    public String getLockedState() {
        return this.mLockedStatus;
    }

    public String getLockedIMSI() {
        return this.mLockedIMSI;
    }

    public String getContractDays() {
        return this.mLockedDays;
    }

    public String getFirstBindTime() {
        return this.mFirstBindTime;
    }

    public String getLockedICCID() {
        return this.mLockedICCID;
    }

    public String getLastBindTime() {
        return this.mLastBindTime;
    }

    public String getUnlockDate() {
        return this.mUnlockDate;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(this.mLockedOperator)) {
            stringBuilder.append(this.mLockedOperator);
            stringBuilder.append("$");
        }
        if (!TextUtils.isEmpty(this.mLockedStatus)) {
            stringBuilder.append(this.mLockedStatus);
            stringBuilder.append("$");
        }
        if (!TextUtils.isEmpty(this.mLockedIMSI)) {
            stringBuilder.append(this.mLockedIMSI);
            stringBuilder.append("$");
        }
        if (!TextUtils.isEmpty(this.mLockedDays)) {
            stringBuilder.append(this.mLockedDays);
            stringBuilder.append("$");
        }
        if (!TextUtils.isEmpty(this.mFirstBindTime)) {
            stringBuilder.append(this.mFirstBindTime);
            stringBuilder.append("$");
        }
        if (!TextUtils.isEmpty(this.mLockedICCID)) {
            stringBuilder.append(this.mLockedICCID);
            stringBuilder.append("$");
        }
        if (!TextUtils.isEmpty(this.mLastBindTime)) {
            stringBuilder.append(this.mLastBindTime);
            stringBuilder.append("$");
        }
        if (!TextUtils.isEmpty(this.mUnlockDate)) {
            stringBuilder.append(this.mUnlockDate);
            stringBuilder.append("$");
        }
        return stringBuilder.toString();
    }
}
