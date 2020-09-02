package com.oppo.internal.telephony.explock.util;

import com.oppo.internal.telephony.explock.RegionLockConstant;

public enum MethodType {
    readSimLockDataFromRPMB(RegionLockConstant.EVENT_UPDATE_POWER_RADIO),
    writeSimLockDataFromRPMB(RegionLockConstant.EVENT_NETWORK_LOCK_LOCKED);
    
    private int mCode;

    private MethodType(int code) {
        this.mCode = code;
    }

    public static MethodType get(int code) {
        MethodType[] values = values();
        for (MethodType type : values) {
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
