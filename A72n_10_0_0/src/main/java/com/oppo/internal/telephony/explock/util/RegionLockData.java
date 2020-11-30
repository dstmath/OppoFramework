package com.oppo.internal.telephony.explock.util;

import android.text.TextUtils;

public class RegionLockData {
    public static final int COUNTRY_INDEX = 0;
    public static final int TYPE_SIZE = 3;
    private String mRegionLockCountry;
    private String mRegionLockStatus;

    public void setRegionLockCountry(String country) {
        this.mRegionLockCountry = country;
    }

    public void setRegionLockStatus(String status) {
        this.mRegionLockStatus = status;
    }

    public String getRegionLockCountry() {
        return this.mRegionLockCountry;
    }

    public String getRegionLockStatus() {
        return this.mRegionLockStatus;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(this.mRegionLockCountry)) {
            stringBuilder.append(this.mRegionLockCountry);
            stringBuilder.append("$");
        }
        if (!TextUtils.isEmpty(this.mRegionLockStatus)) {
            stringBuilder.append(this.mRegionLockStatus);
            stringBuilder.append("$");
        }
        return stringBuilder.toString();
    }
}
