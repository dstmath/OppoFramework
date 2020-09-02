package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Field;

public class RefStaticObject<T> {
    private static final String TAG = "RefStaticObject";
    private Field field;

    public RefStaticObject(Class<?> cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public Class<?> type() {
        return this.field.getType();
    }

    public T get() {
        try {
            return this.field.get(null);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public void set(T obj) {
        try {
            this.field.set(null, obj);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
