package com.suntek.mway.rcs.client.aidl.plugin.entity.contact;

public enum SyncAction {
    CONTACT_DOWNLOAD,
    CONTACT_DOWNLOAD_APPEND,
    CONTACT_UPLOAD,
    CONTACT_UPLOAD_APPEND;

    public static SyncAction valueOf(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        throw new IndexOutOfBoundsException("Invalid ordinal");
    }
}
