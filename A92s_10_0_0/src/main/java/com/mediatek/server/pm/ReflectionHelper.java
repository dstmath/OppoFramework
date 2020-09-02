package com.mediatek.server.pm;

import android.util.Slog;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* compiled from: PmsExtImpl */
class ReflectionHelper {
    ReflectionHelper() {
    }

    public static Class getNonPublicInnerClass(Class targetCls, String innerClsName) {
        Class[] innerClasses = targetCls.getDeclaredClasses();
        for (Class cls : innerClasses) {
            if (cls.toString().contains(innerClsName)) {
                return cls;
            }
        }
        return null;
    }

    public static Field getNonPublicField(Class cls, String fieldName) {
        try {
            return cls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getObjectValue(Field field, Object targetObject) {
        field.setAccessible(true);
        try {
            return field.get(targetObject);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static boolean getBooleanValue(Field field, Object tarObject) {
        field.setAccessible(true);
        try {
            return field.getBoolean(tarObject);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public static int getIntValue(Field field, Object tarObject) {
        field.setAccessible(true);
        try {
            return field.getInt(tarObject);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    public static Object getObjectValue(Class cls, String fieldName, Object targetObject) {
        return getObjectValue(getNonPublicField(cls, fieldName), targetObject);
    }

    public static boolean getBooleanValue(Class cls, String fieldName, Object tarObject) {
        return getBooleanValue(getNonPublicField(cls, fieldName), tarObject);
    }

    public static int getIntValue(Class cls, String fieldName) {
        return getIntValue(getNonPublicField(cls, fieldName), cls);
    }

    public static Method getMethod(Class cls, String methodName, Class... params) {
        Method retMethod = null;
        try {
            retMethod = cls.getDeclaredMethod(methodName, params);
            if (retMethod != null) {
                retMethod.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return retMethod;
    }

    public static Object callMethod(Method method, Object object, Object... params) {
        Object ret = null;
        if (method == null) {
            return null;
        }
        try {
            ret = method.invoke(object, params);
            Slog.d("PmsExtImpl", "callMethod:" + method.getName() + " ret=" + ret);
            return ret;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ret;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return ret;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return ret;
        }
    }

    public static void setFieldValue(Class cls, Object obj, String fieldName, Object value) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        setFieldValue(obj.getClass(), obj, fieldName, value);
    }
}
