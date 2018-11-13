package com.suntek.mway.rcs.client.aidl.plugin.entity.contact;

public enum ContactAction {
    CONTACT_ACTION_READ,
    CONTACT_ACTION_ADD,
    CONTACT_ACTION_DELETE,
    CONTACT_ACTION_UPDATE,
    CONTACT_ACTION_UNKNOWN;

    public static ContactAction valueOf(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        throw new IndexOutOfBoundsException("Invalid ordinal");
    }
}
