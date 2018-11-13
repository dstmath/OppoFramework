package cm.android.mdm.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodSignature {
    public static String generateSignature(Method method) {
        if (method == null) {
            return "";
        }
        return generateSignature(method.getName(), method.getParameterTypes());
    }

    public static String generateSignature(String methodName, Class<?>... parameterTypes) {
        if (methodName == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append('(');
        if (parameterTypes != null) {
            for (Class<?> parameterType : parameterTypes) {
                result.append(getInternalName(parameterType));
            }
        }
        result.append(')');
        return result.toString();
    }

    private static Class<?> getClass(ClassLoader classLoader, String internalName) {
        if (internalName.startsWith("[")) {
            return Array.newInstance(getClass(classLoader, internalName.substring(1)), 0).getClass();
        }
        if (internalName.equals("Z")) {
            return Boolean.TYPE;
        }
        if (internalName.equals("B")) {
            return Byte.TYPE;
        }
        if (internalName.equals("S")) {
            return Short.TYPE;
        }
        if (internalName.equals("I")) {
            return Integer.TYPE;
        }
        if (internalName.equals("J")) {
            return Long.TYPE;
        }
        if (internalName.equals("F")) {
            return Float.TYPE;
        }
        if (internalName.equals("D")) {
            return Double.TYPE;
        }
        if (internalName.equals("C")) {
            return Character.TYPE;
        }
        if (internalName.equals("V")) {
            return Void.TYPE;
        }
        String name = internalName.substring(1, internalName.length() - 1).replace('/', '.');
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(name);
            error.initCause(e);
            throw error;
        }
    }

    private static String getInternalName(Class<?> c) {
        if (c.isArray()) {
            return '[' + getInternalName(c.getComponentType());
        }
        if (c == Boolean.TYPE) {
            return "Z";
        }
        if (c == Byte.TYPE) {
            return "B";
        }
        if (c == Short.TYPE) {
            return "S";
        }
        if (c == Integer.TYPE) {
            return "I";
        }
        if (c == Long.TYPE) {
            return "J";
        }
        if (c == Float.TYPE) {
            return "F";
        }
        if (c == Double.TYPE) {
            return "D";
        }
        if (c == Character.TYPE) {
            return "C";
        }
        if (c == Void.TYPE) {
            return "V";
        }
        return 'L' + c.getName().replace('.', '/') + ';';
    }

    public static List<String> getMethodSignatures(Class<?> clazz) {
        List<String> signatures = new ArrayList();
        for (Method method : clazz.getDeclaredMethods()) {
            signatures.add(generateSignature(method));
        }
        return signatures;
    }
}
