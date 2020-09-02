package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Field;

public class RefInt {
    private static final String TAG = "RefInt";
    private Field field;

    public RefInt(Class cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public int get(Object object) {
        try {
            return this.field.getInt(object);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public void set(Object obj, int intValue) {
        try {
            this.field.setInt(obj, intValue);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
