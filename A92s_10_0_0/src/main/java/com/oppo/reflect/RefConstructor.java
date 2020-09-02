package com.oppo.reflect;

import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class RefConstructor<T> {
    private static final String TAG = "RefConstructor";
    private Constructor<?> ctor;

    public RefConstructor(Class<?> cls, Field field) throws NoSuchMethodException {
        if (field.isAnnotationPresent(MethodParams.class)) {
            this.ctor = cls.getDeclaredConstructor(field.getAnnotation(MethodParams.class).value());
        } else if (field.isAnnotationPresent(MethodReflectParams.class)) {
            String[] values = field.getAnnotation(MethodReflectParams.class).value();
            Class[] parameterTypes = new Class[values.length];
            int N = 0;
            while (N < values.length) {
                try {
                    parameterTypes[N] = Class.forName(values[N]);
                    N++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.ctor = cls.getDeclaredConstructor(parameterTypes);
        } else {
            this.ctor = cls.getDeclaredConstructor(new Class[0]);
        }
        Constructor<?> constructor = this.ctor;
        if (constructor != null && !constructor.isAccessible()) {
            this.ctor.setAccessible(true);
        }
    }

    public T newInstance() {
        try {
            return this.ctor.newInstance(new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public T newInstance(Object... params) {
        try {
            return this.ctor.newInstance(params);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
