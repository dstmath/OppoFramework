package com.suntek.mway.rcs.client.aidl.plugin.entity.contact;

public enum IntervalAction {
    INTERVAL_SYNC_ONE_DAY,
    INTERVAL_SYNC_ONE_WEEK,
    INTERVAL_SYNC_ONE_MONTH,
    INTERVAL_SYNC_SELF_DEFINE;

    public static IntervalAction valueOf(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        throw new IndexOutOfBoundsException("Invalid ordinal");
    }
}
