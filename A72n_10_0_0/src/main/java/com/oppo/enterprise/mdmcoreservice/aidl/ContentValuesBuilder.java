package com.oppo.enterprise.mdmcoreservice.aidl;

import android.content.ContentValues;

public class ContentValuesBuilder {
    private final ContentValues mContentValues;

    public ContentValuesBuilder(ContentValues contentValues) {
        this.mContentValues = contentValues;
    }

    public ContentValuesBuilder put(String key, String value) {
        this.mContentValues.put(key, value);
        return this;
    }

    public ContentValuesBuilder put(String key, Integer value) {
        this.mContentValues.put(key, value);
        return this;
    }

    public ContentValuesBuilder put(String key, byte[] value) {
        this.mContentValues.put(key, value);
        return this;
    }

    public ContentValuesBuilder putNull(String key) {
        this.mContentValues.putNull(key);
        return this;
    }
}
