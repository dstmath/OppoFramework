package com.oppo.internal.telephony.explock.util;

public enum LockDataType {
    dLockOperator(0),
    dLockStatus(1),
    dLockImsi(2),
    dLockContractDays(3),
    dLockFirstBindTime(4),
    dLockIccid(5),
    dLockLastBindTime(6),
    dLockUnlockDate(7),
    rLockCountry(9),
    rLockStatus(10);
    
    private int mCode;

    private LockDataType(int code) {
        this.mCode = code;
    }

    public static LockDataType get(int code) {
        LockDataType[] values = values();
        for (LockDataType type : values) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    public int getCode() {
        return this.mCode;
    }
}
