package com.oppo.reflect;

import android.app.slice.SliceItem;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RefStaticMethod<T> {
    private Method method;

    public RefStaticMethod(Class<?> cls, Field field) throws NoSuchMethodException {
        if (field.isAnnotationPresent(MethodParams.class)) {
            Class<?>[] types = ((MethodParams) field.getAnnotation(MethodParams.class)).value();
            for (Class<?> cls2 : types) {
            }
            this.method = cls.getDeclaredMethod(field.getName(), types);
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
            boolean arrayset = false;
            String[] typeNames = ((MethodReflectParams) field.getAnnotation(MethodReflectParams.class)).value();
            Class<?>[] types2 = new Class[typeNames.length];
            Class<?>[] types22 = new Class[typeNames.length];
            for (int i2 = 0; i2 < typeNames.length; i2++) {
                Class<?> type = getProtoType(typeNames[i2]);
                if (type == null) {
                    try {
                        type = Class.forName(typeNames[i2]);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                types2[i2] = type;
                if ("java.util.HashSet".equals(typeNames[i2])) {
                    arrayset = true;
                    Class<?> type2 = type;
                    try {
                        type2 = Class.forName("android.util.ArraySet");
                    } catch (ClassNotFoundException e2) {
                        e2.printStackTrace();
                    }
                    if (type2 != null) {
                        types22[i2] = type2;
                    } else {
                        types22[i2] = type;
                    }
                } else {
                    types22[i2] = type;
                }
            }
            try {
                this.method = cls.getDeclaredMethod(field.getName(), types2);
            } catch (Exception e3) {
                e3.printStackTrace();
                if (arrayset) {
                    this.method = cls.getDeclaredMethod(field.getName(), types22);
                }
            }
            this.method.setAccessible(true);
        }
        if (this.method == null) {
            throw new NoSuchMethodException(field.getName());
        }
    }

    static Class<?> getProtoType(String typeName) {
        if (typeName.equals(SliceItem.FORMAT_INT)) {
            return Integer.TYPE;
        }
        if (typeName.equals("long")) {
            return Long.TYPE;
        }
        if (typeName.equals("boolean")) {
            return Boolean.TYPE;
        }
        if (typeName.equals("byte")) {
            return Byte.TYPE;
        }
        if (typeName.equals("short")) {
            return Short.TYPE;
        }
        if (typeName.equals("char")) {
            return Character.TYPE;
        }
        if (typeName.equals("float")) {
            return Float.TYPE;
        }
        if (typeName.equals("double")) {
            return Double.TYPE;
        }
        if (typeName.equals("void")) {
            return Void.TYPE;
        }
        return null;
    }

    public T call(Object... params) {
        try {
            return (T) this.method.invoke(null, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public T callWithException(Object... params) throws Throwable {
        try {
            return (T) this.method.invoke(null, params);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
    }
}
