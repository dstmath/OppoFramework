package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Field;

public class RefObject<T> {
    private static final String TAG = "RefObject";
    private Field field;

    public RefObject(Class<?> cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public T get(Object object) {
        try {
            return this.field.get(object);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public void set(Object obj, T value) {
        try {
            this.field.set(obj, value);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
