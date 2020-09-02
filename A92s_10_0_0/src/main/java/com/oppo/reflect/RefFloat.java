package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Field;

public class RefFloat {
    private static final String TAG = "RefFloat";
    private Field field;

    public RefFloat(Class cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public float get(Object object) {
        try {
            return this.field.getFloat(object);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0.0f;
        }
    }

    public void set(Object obj, float value) {
        try {
            this.field.setFloat(obj, value);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
