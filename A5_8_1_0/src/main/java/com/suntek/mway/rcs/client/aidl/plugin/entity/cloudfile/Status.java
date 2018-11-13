package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

public enum Status {
    waitting,
    running,
    pendding,
    succeed,
    paused,
    canceled,
    failed;

    public static Status valueOf(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        throw new IndexOutOfBoundsException("Invalid ordinal");
    }
}
