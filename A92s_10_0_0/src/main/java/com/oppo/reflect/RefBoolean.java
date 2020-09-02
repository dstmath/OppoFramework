package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Field;

public class RefBoolean {
    private static final String TAG = "RefBoolean";
    private Field field;

    public RefBoolean(Class<?> cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public boolean get(Object object) {
        try {
            return this.field.getBoolean(object);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public void set(Object obj, boolean value) {
        try {
            this.field.setBoolean(obj, value);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
