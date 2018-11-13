package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

public enum SdkType {
    Static,
    Service,
    Plugin,
    None;

    public static SdkType valueOf(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        throw new IndexOutOfBoundsException("Invalid ordinal");
    }
}
