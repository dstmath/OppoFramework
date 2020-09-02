package com.oppo.internal.telephony.explock.util;

public enum MethodParamType {
    backSimLockData(300);
    
    private int mCode;

    private MethodParamType(int code) {
        this.mCode = code;
    }

    public static MethodParamType get(int code) {
        MethodParamType[] values = values();
        for (MethodParamType type : values) {
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
