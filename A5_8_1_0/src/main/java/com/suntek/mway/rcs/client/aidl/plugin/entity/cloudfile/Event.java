package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

public enum Event {
    started,
    success,
    error,
    canceled,
    progress,
    paused,
    resumed,
    pendding,
    sub_started;

    public static Event valueOf(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        throw new IndexOutOfBoundsException("Invalid ordinal");
    }
}
