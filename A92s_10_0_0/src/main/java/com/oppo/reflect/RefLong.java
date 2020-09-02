package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Field;

public class RefLong {
    private static final String TAG = "RefLong";
    private Field field;

    public RefLong(Class cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public long get(Object object) {
        try {
            return this.field.getLong(object);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public void set(Object obj, long value) {
        try {
            this.field.setLong(obj, value);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
