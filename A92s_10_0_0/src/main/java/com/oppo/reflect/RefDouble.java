package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Field;

public class RefDouble {
    private static final String TAG = "RefDouble";
    private Field field;

    public RefDouble(Class cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public double get(Object object) {
        try {
            return this.field.getDouble(object);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0.0d;
        }
    }

    public void set(Object obj, double value) {
        try {
            this.field.setDouble(obj, value);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
