package com.oppo.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RefMethod<T> {
    private Method method;

    public RefMethod(Class<?> cls, Field field) throws NoSuchMethodException {
        if (field.isAnnotationPresent(MethodParams.class)) {
            this.method = cls.getDeclaredMethod(field.getName(), ((MethodParams) field.getAnnotation(MethodParams.class)).value());
            this.method.setAccessible(true);
        } else if (!field.isAnnotationPresent(MethodReflectParams.class)) {
            Method[] declaredMethods = cls.getDeclaredMethods();
            int length = declaredMethods.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                Method method2 = declaredMethods[i];
                if (method2.getName().equals(field.getName())) {
                    this.method = method2;
                    this.method.setAccessible(true);
                    break;
                }
                i++;
            }
        } else {
            String[] typeNames = ((MethodReflectParams) field.getAnnotation(MethodReflectParams.class)).value();
            Class<?>[] types = new Class[typeNames.length];
            for (int i2 = 0; i2 < typeNames.length; i2++) {
                Class<?> type = RefStaticMethod.getProtoType(typeNames[i2]);
                if (type == null) {
                    try {
                        type = Class.forName(typeNames[i2]);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                types[i2] = type;
            }
            this.method = cls.getDeclaredMethod(field.getName(), types);
            this.method.setAccessible(true);
        }
        if (this.method == null) {
            throw new NoSuchMethodException(field.getName());
        }
    }

    public T call(Object receiver, Object... args) {
        try {
            return (T) this.method.invoke(receiver, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
                return null;
            }
            e.printStackTrace();
            return null;
        } catch (Throwable e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public T callWithException(Object receiver, Object... args) throws Throwable {
        try {
            return (T) this.method.invoke(receiver, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
    }

    public Class<?>[] paramList() {
        return this.method.getParameterTypes();
    }
}
