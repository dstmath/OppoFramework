package com.android.server.coloros;

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ReflectionUtil {
    public static String TAG;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.coloros.ReflectionUtil.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.coloros.ReflectionUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.coloros.ReflectionUtil.<clinit>():void");
    }

    public static Class getClass(String className) {
        Class c = null;
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, Log.getStackTraceString(e));
            return c;
        }
    }

    public static Field getField(Class c, String fieldName) {
        Field f = null;
        try {
            f = c.getDeclaredField(fieldName);
            if (f == null) {
                return f;
            }
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    public static Field getField(String className, String fieldName) {
        Field f = null;
        try {
            Class c = getClass(className);
            if (c == null) {
                return null;
            }
            f = getField(c, fieldName);
            return f;
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    public static Field getField(Object obj, String fieldName) {
        Field f = null;
        try {
            return getField(obj.getClass(), fieldName);
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
            return f;
        }
    }

    public static Object getObject(Field f, Object o) {
        Object obj = null;
        try {
            return f.get(o);
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
            return obj;
        }
    }

    public static int getInt(Object o, String fieldName, int val) {
        Field f = getField(o, fieldName);
        if (f == null) {
            return val;
        }
        Object obj = getObject(f, o);
        if (obj == null) {
            return val;
        }
        try {
            val = ((Integer) obj).intValue();
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return val;
    }

    public static int getStaticInt(Class c, String fieldName, int val) {
        Field f = getField(c, fieldName);
        if (f == null) {
            return val;
        }
        Object obj = getObject(f, null);
        if (obj == null) {
            return val;
        }
        try {
            val = ((Integer) obj).intValue();
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return val;
    }

    public static int getStaticInt(String className, String fieldName, int val) {
        Field f = getField(className, fieldName);
        if (f == null) {
            return val;
        }
        Object obj = getObject(f, null);
        if (obj == null) {
            return val;
        }
        try {
            val = ((Integer) obj).intValue();
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return val;
    }

    public static Object invokeAdaptiveNonStaticMethod(Object instanceObj, String methodName, Object[] params) {
        Method invokeMethod = null;
        Object value = null;
        if (methodName == null) {
            return null;
        }
        try {
            int i;
            Method[] methods = instanceObj.getClass().getDeclaredMethods();
            for (i = 0; i < methods.length; i++) {
                if (methodName.equals(methods[i].getName())) {
                    invokeMethod = methods[i];
                    break;
                }
            }
            int pTypeCount = invokeMethod.getParameterTypes().length;
            if (pTypeCount != params.length) {
                int min;
                if (pTypeCount > params.length) {
                    min = params.length;
                } else {
                    min = pTypeCount;
                }
                Object[] new_params = new Object[min];
                for (i = 0; i < min; i++) {
                    new_params[i] = params[i];
                }
                invokeMethod.setAccessible(true);
                value = invokeMethod.invoke(instanceObj, new_params);
            } else {
                invokeMethod.setAccessible(true);
                value = invokeMethod.invoke(instanceObj, params);
            }
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return value;
    }

    public static Object invokeAdaptiveStaticMethod(Class c, String methodName, Object[] params) {
        Method invokeMethod = null;
        Object value = null;
        if (methodName == null) {
            return null;
        }
        try {
            int i;
            Method[] methods = c.getDeclaredMethods();
            for (i = 0; i < methods.length; i++) {
                if (methodName.equals(methods[i].getName())) {
                    invokeMethod = methods[i];
                    break;
                }
            }
            int pTypeCount = invokeMethod.getParameterTypes().length;
            if (pTypeCount != params.length) {
                int min;
                if (pTypeCount > params.length) {
                    min = params.length;
                } else {
                    min = pTypeCount;
                }
                Object[] new_params = new Object[min];
                for (i = 0; i < min; i++) {
                    new_params[i] = params[i];
                }
                invokeMethod.setAccessible(true);
                value = invokeMethod.invoke(null, new_params);
            } else {
                invokeMethod.setAccessible(true);
                value = invokeMethod.invoke(null, params);
            }
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return value;
    }

    public static Object invokeNonStaticMethod(Object instance, Class[] paramTypes, String methodName, Object[] params) {
        Object value = null;
        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, paramTypes);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            value = method.invoke(instance, params);
            return value;
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    public static Object invokeStaticMethod(Class c, Class[] param_types, String methodName, Object[] params) {
        Object value = null;
        try {
            Method method = c.getDeclaredMethod(methodName, param_types);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            value = method.invoke(null, params);
            return value;
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    public static Object invokeStaticMethod(String className, Class[] param_types, String methodName, Object[] params) {
        Class c = null;
        try {
            c = Class.forName(className);
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        if (c != null) {
            return invokeStaticMethod(c, param_types, methodName, params);
        }
        return null;
    }
}
