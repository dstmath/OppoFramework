package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Field;

public class RefStaticInt {
    private static final String TAG = "RefStaticInt";
    private Field field;

    public RefStaticInt(Class<?> cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public int get() {
        try {
            return this.field.getInt(null);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public void set(int value) {
        try {
            this.field.setInt(null, value);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
