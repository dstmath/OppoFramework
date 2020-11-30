package com.alibaba.fastjson.util;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.asm.ClassReader;
import com.alibaba.fastjson.asm.TypeCollector;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ASMUtils {
    public static final boolean IS_ANDROID = isAndroid(JAVA_VM_NAME);
    public static final String JAVA_VM_NAME = System.getProperty("java.vm.name");

    public static boolean isAndroid(String vmName) {
        if (vmName == null) {
            return false;
        }
        String lowerVMName = vmName.toLowerCase();
        if (lowerVMName.contains("dalvik") || lowerVMName.contains("lemur")) {
            return true;
        }
        return false;
    }

    public static String desc(Method method) {
        Class<?>[] types = method.getParameterTypes();
        StringBuilder buf = new StringBuilder((types.length + 1) << 4);
        buf.append('(');
        for (Class<?> cls : types) {
            buf.append(desc(cls));
        }
        buf.append(')');
        buf.append(desc(method.getReturnType()));
        return buf.toString();
    }

    public static String desc(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getPrimitiveLetter(returnType);
        }
        if (returnType.isArray()) {
            return "[" + desc(returnType.getComponentType());
        }
        return "L" + type(returnType) + ";";
    }

    public static String type(Class<?> parameterType) {
        if (parameterType.isArray()) {
            return "[" + desc(parameterType.getComponentType());
        } else if (!parameterType.isPrimitive()) {
            return parameterType.getName().replace('.', '/');
        } else {
            return getPrimitiveLetter(parameterType);
        }
    }

    public static String getPrimitiveLetter(Class<?> type) {
        if (Integer.TYPE == type) {
            return "I";
        }
        if (Void.TYPE == type) {
            return "V";
        }
        if (Boolean.TYPE == type) {
            return "Z";
        }
        if (Character.TYPE == type) {
            return "C";
        }
        if (Byte.TYPE == type) {
            return "B";
        }
        if (Short.TYPE == type) {
            return "S";
        }
        if (Float.TYPE == type) {
            return "F";
        }
        if (Long.TYPE == type) {
            return "J";
        }
        if (Double.TYPE == type) {
            return "D";
        }
        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    public static Type getMethodType(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName, new Class[0]).getGenericReturnType();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean checkName(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < 1 || c > 127 || c == '.') {
                return false;
            }
        }
        return true;
    }

    public static String[] lookupParameterNames(AccessibleObject methodOrCtor) {
        Class<?> declaringClass;
        String name;
        Class<?>[] types;
        Annotation[][] parameterAnnotations;
        ClassReader reader;
        int i = 0;
        if (IS_ANDROID) {
            return new String[0];
        }
        if (methodOrCtor instanceof Method) {
            Method method = (Method) methodOrCtor;
            types = method.getParameterTypes();
            name = method.getName();
            declaringClass = method.getDeclaringClass();
            parameterAnnotations = method.getParameterAnnotations();
        } else {
            Constructor<?> constructor = (Constructor) methodOrCtor;
            types = constructor.getParameterTypes();
            declaringClass = constructor.getDeclaringClass();
            name = "<init>";
            parameterAnnotations = constructor.getParameterAnnotations();
        }
        if (types.length == 0) {
            return new String[0];
        }
        ClassLoader classLoader = declaringClass.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        String className = declaringClass.getName();
        InputStream is = classLoader.getResourceAsStream(className.replace('.', '/') + ".class");
        if (is == null) {
            return new String[0];
        }
        try {
            ClassReader reader2 = new ClassReader(is, false);
            TypeCollector visitor = new TypeCollector(name, types);
            reader2.accept(visitor);
            String[] parameterNames = visitor.getParameterNamesForMethod();
            int i2 = 0;
            while (i2 < parameterNames.length) {
                Annotation[] annotations = parameterAnnotations[i2];
                if (annotations != null) {
                    int j = i;
                    while (j < annotations.length) {
                        if (annotations[j] instanceof JSONField) {
                            String fieldName = ((JSONField) annotations[j]).name();
                            reader = reader2;
                            if (fieldName != null && fieldName.length() > 0) {
                                parameterNames[i2] = fieldName;
                            }
                        } else {
                            reader = reader2;
                        }
                        j++;
                        reader2 = reader;
                    }
                }
                i2++;
                reader2 = reader2;
                i = 0;
            }
            return parameterNames;
        } catch (IOException e) {
            return new String[0];
        } finally {
            IOUtils.close(is);
        }
    }
}
