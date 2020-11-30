package com.color.inner.mediatek.telephony;

import android.util.Log;

public class MtkIccSmsStorageStatusWrapper {
    public static final int INVALID_RESULT = -1;
    public static final String TAG = "MtkIccSmsStorageStatusWrapper";
    private Object mMtkIccSmsStorageStatus;

    public MtkIccSmsStorageStatusWrapper(Object mtkIccSmsStorageStatus) {
        this.mMtkIccSmsStorageStatus = mtkIccSmsStorageStatus;
    }

    public int getUsedCount() {
        try {
            return ((Integer) this.mMtkIccSmsStorageStatus.getClass().getDeclaredMethod("getUsedCount", new Class[0]).invoke(this.mMtkIccSmsStorageStatus, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public int getTotalCount() {
        try {
            return ((Integer) this.mMtkIccSmsStorageStatus.getClass().getDeclaredMethod("getTotalCount", new Class[0]).invoke(this.mMtkIccSmsStorageStatus, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public int getUnused() {
        try {
            return ((Integer) this.mMtkIccSmsStorageStatus.getClass().getDeclaredMethod("getUnused", new Class[0]).invoke(this.mMtkIccSmsStorageStatus, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public void reset() {
        try {
            this.mMtkIccSmsStorageStatus.getClass().getDeclaredMethod("reset", new Class[0]).invoke(this.mMtkIccSmsStorageStatus, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
