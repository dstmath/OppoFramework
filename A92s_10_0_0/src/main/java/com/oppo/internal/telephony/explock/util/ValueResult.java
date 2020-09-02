package com.oppo.internal.telephony.explock.util;

public class ValueResult {
    private Object mDeviceValue = null;
    private boolean mIsGetValueSuccess = false;
    private Object mRegionValue = null;

    public Object getDeviceValue() {
        return this.mDeviceValue;
    }

    public void setDeviceValue(Object value) {
        this.mDeviceValue = value;
    }

    public Object getRegionValue() {
        return this.mRegionValue;
    }

    public void setRegionValue(Object value) {
        this.mRegionValue = value;
    }

    public boolean isGetValueSuccess() {
        return this.mIsGetValueSuccess;
    }

    public void setGetValueSuccess(boolean success) {
        this.mIsGetValueSuccess = success;
    }
}
