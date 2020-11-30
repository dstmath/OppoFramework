package com.alibaba.fastjson.util;

import com.alibaba.fastjson.annotation.JSONField;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class FieldInfo implements Comparable<FieldInfo> {
    public final String[] alternateNames;
    public final Class<?> declaringClass;
    public final Field field;
    public final boolean fieldAccess;
    private final JSONField fieldAnnotation;
    public final Class<?> fieldClass;
    public final boolean fieldTransient;
    public final Type fieldType;
    public final String format;
    public final boolean getOnly;
    public final boolean isEnum;
    public final boolean jsonDirect;
    public final String label;
    public final Method method;
    private final JSONField methodAnnotation;
    public final String name;
    public final char[] name_chars;
    private int ordinal = 0;
    public final int parserFeatures;
    public final int serialzeFeatures;
    public final boolean unwrapped;

    public FieldInfo(String name2, Class<?> declaringClass2, Class<?> fieldClass2, Type fieldType2, Field field2, int ordinal2, int serialzeFeatures2, int parserFeatures2) {
        ordinal2 = ordinal2 < 0 ? 0 : ordinal2;
        this.name = name2;
        this.declaringClass = declaringClass2;
        this.fieldClass = fieldClass2;
        this.fieldType = fieldType2;
        this.method = null;
        this.field = field2;
        this.ordinal = ordinal2;
        this.serialzeFeatures = serialzeFeatures2;
        this.parserFeatures = parserFeatures2;
        this.isEnum = fieldClass2.isEnum();
        if (field2 != null) {
            int modifiers = field2.getModifiers();
            this.fieldAccess = (modifiers & 1) != 0 || this.method == null;
            this.fieldTransient = Modifier.isTransient(modifiers);
        } else {
            this.fieldTransient = false;
            this.fieldAccess = false;
        }
        this.name_chars = genFieldNameChars();
        if (field2 != null) {
            TypeUtils.setAccessible(field2);
        }
        this.label = "";
        this.fieldAnnotation = null;
        this.methodAnnotation = null;
        this.getOnly = false;
        this.jsonDirect = false;
        this.unwrapped = false;
        this.format = null;
        this.alternateNames = new String[0];
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x001e, code lost:
        if (r7.equals(r8) != false) goto L_0x0025;
     */
    public FieldInfo(String name2, Method method2, Field field2, Class<?> clazz, Type type, int ordinal2, int serialzeFeatures2, int parserFeatures2, JSONField fieldAnnotation2, JSONField methodAnnotation2, String label2) {
        String fieldName;
        int ordinal3;
        boolean jsonDirect2;
        boolean getOnly2;
        boolean z;
        Type fieldClass2;
        Type fieldType2;
        Type genericFieldType;
        String str;
        if (field2 != null) {
            fieldName = field2.getName();
            str = name2;
        } else {
            str = name2;
        }
        fieldName = str;
        if (ordinal2 < 0) {
            ordinal3 = 0;
        } else {
            ordinal3 = ordinal2;
        }
        this.name = fieldName;
        this.method = method2;
        this.field = field2;
        this.ordinal = ordinal3;
        this.serialzeFeatures = serialzeFeatures2;
        this.parserFeatures = parserFeatures2;
        this.fieldAnnotation = fieldAnnotation2;
        this.methodAnnotation = methodAnnotation2;
        if (field2 != null) {
            int modifiers = field2.getModifiers();
            this.fieldAccess = (modifiers & 1) != 0 || method2 == null;
            this.fieldTransient = Modifier.isTransient(modifiers) || TypeUtils.isTransient(method2);
        } else {
            this.fieldAccess = false;
            this.fieldTransient = false;
        }
        if (label2 == null || label2.length() <= 0) {
            this.label = "";
        } else {
            this.label = label2;
        }
        String format2 = null;
        JSONField annotation = getAnnotation();
        if (annotation != null) {
            format2 = annotation.format();
            format2 = format2.trim().length() == 0 ? null : format2;
            jsonDirect2 = annotation.jsonDirect();
            this.unwrapped = annotation.unwrapped();
            this.alternateNames = annotation.alternateNames();
        } else {
            jsonDirect2 = false;
            this.unwrapped = false;
            this.alternateNames = new String[0];
        }
        this.format = format2;
        this.name_chars = genFieldNameChars();
        if (method2 != null) {
            TypeUtils.setAccessible(method2);
        }
        if (field2 != null) {
            TypeUtils.setAccessible(field2);
        }
        if (method2 != null) {
            Type[] parameterTypes = method2.getParameterTypes();
            getOnly2 = false;
            if (parameterTypes.length == 1) {
                fieldClass2 = parameterTypes[0];
                fieldType2 = method2.getGenericParameterTypes()[0];
                z = true;
            } else {
                if (parameterTypes.length == 2 && parameterTypes[0] == String.class) {
                    z = true;
                    if (parameterTypes[1] == Object.class) {
                        fieldType2 = parameterTypes[0];
                        fieldClass2 = fieldType2;
                    }
                } else {
                    z = true;
                }
                fieldClass2 = method2.getReturnType();
                fieldType2 = method2.getGenericReturnType();
                getOnly2 = true;
            }
            this.declaringClass = method2.getDeclaringClass();
        } else {
            z = true;
            fieldClass2 = field2.getType();
            fieldType2 = field2.getGenericType();
            this.declaringClass = field2.getDeclaringClass();
            getOnly2 = Modifier.isFinal(field2.getModifiers());
        }
        Class<?> fieldClass3 = fieldClass2;
        this.getOnly = getOnly2;
        this.jsonDirect = (!jsonDirect2 || fieldClass3 != String.class) ? false : z;
        if (clazz == null || fieldClass3 != Object.class || !(fieldType2 instanceof TypeVariable) || (genericFieldType = getInheritGenericType(clazz, type, (TypeVariable) fieldType2)) == null) {
            Type genericFieldType2 = fieldType2;
            if (!(fieldType2 instanceof Class)) {
                genericFieldType2 = getFieldType(clazz, type != null ? type : clazz, fieldType2);
                if (genericFieldType2 != fieldType2) {
                    if (genericFieldType2 instanceof ParameterizedType) {
                        fieldClass3 = TypeUtils.getClass(genericFieldType2);
                    } else if (genericFieldType2 instanceof Class) {
                        fieldClass3 = TypeUtils.getClass(genericFieldType2);
                    }
                }
            }
            this.fieldType = genericFieldType2;
            this.fieldClass = fieldClass3;
            this.isEnum = fieldClass3.isEnum();
            return;
        }
        this.fieldClass = TypeUtils.getClass(genericFieldType);
        this.fieldType = genericFieldType;
        this.isEnum = fieldClass3.isEnum();
    }

    /* access modifiers changed from: protected */
    public char[] genFieldNameChars() {
        int nameLen = this.name.length();
        char[] name_chars2 = new char[(nameLen + 3)];
        this.name.getChars(0, this.name.length(), name_chars2, 1);
        name_chars2[0] = '\"';
        name_chars2[nameLen + 1] = '\"';
        name_chars2[nameLen + 2] = ':';
        return name_chars2;
    }

    public <T extends Annotation> T getAnnation(Class<T> annotationClass) {
        if (annotationClass == JSONField.class) {
            return getAnnotation();
        }
        T annotatition = null;
        if (this.method != null) {
            annotatition = (T) this.method.getAnnotation(annotationClass);
        }
        return (annotatition != null || this.field == null) ? annotatition : (T) this.field.getAnnotation(annotationClass);
    }

    public static Type getFieldType(Class<?> clazz, Type type, Type fieldType2) {
        TypeVariable<?>[] typeVariables;
        ParameterizedType paramType;
        if (clazz == null || type == null) {
            return fieldType2;
        }
        if (fieldType2 instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) fieldType2).getGenericComponentType();
            Type componentTypeX = getFieldType(clazz, type, componentType);
            if (componentType != componentTypeX) {
                return Array.newInstance(TypeUtils.getClass(componentTypeX), 0).getClass();
            }
            return fieldType2;
        } else if (!TypeUtils.isGenericParamType(type)) {
            return fieldType2;
        } else {
            if (fieldType2 instanceof TypeVariable) {
                ParameterizedType paramType2 = (ParameterizedType) TypeUtils.getGenericParamType(type);
                TypeVariable<?> typeVar = (TypeVariable) fieldType2;
                TypeVariable<?>[] typeVariables2 = TypeUtils.getClass(paramType2).getTypeParameters();
                for (int i = 0; i < typeVariables2.length; i++) {
                    if (typeVariables2[i].getName().equals(typeVar.getName())) {
                        return paramType2.getActualTypeArguments()[i];
                    }
                }
            }
            if (fieldType2 instanceof ParameterizedType) {
                ParameterizedType parameterizedFieldType = (ParameterizedType) fieldType2;
                Type[] arguments = parameterizedFieldType.getActualTypeArguments();
                if (type instanceof ParameterizedType) {
                    paramType = (ParameterizedType) type;
                    typeVariables = clazz.getTypeParameters();
                } else if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
                    paramType = (ParameterizedType) clazz.getGenericSuperclass();
                    typeVariables = clazz.getSuperclass().getTypeParameters();
                } else {
                    paramType = parameterizedFieldType;
                    typeVariables = type.getClass().getTypeParameters();
                }
                if (getArgument(arguments, typeVariables, paramType.getActualTypeArguments())) {
                    return new ParameterizedTypeImpl(arguments, parameterizedFieldType.getOwnerType(), parameterizedFieldType.getRawType());
                }
            }
            return fieldType2;
        }
    }

    private static boolean getArgument(Type[] typeArgs, TypeVariable[] typeVariables, Type[] arguments) {
        if (arguments == null || typeVariables.length == 0) {
            return false;
        }
        boolean changed = false;
        for (int i = 0; i < typeArgs.length; i++) {
            Type typeArg = typeArgs[i];
            if (typeArg instanceof ParameterizedType) {
                ParameterizedType p_typeArg = (ParameterizedType) typeArg;
                Type[] p_typeArg_args = p_typeArg.getActualTypeArguments();
                if (getArgument(p_typeArg_args, typeVariables, arguments)) {
                    typeArgs[i] = new ParameterizedTypeImpl(p_typeArg_args, p_typeArg.getOwnerType(), p_typeArg.getRawType());
                    changed = true;
                }
            } else if (typeArg instanceof TypeVariable) {
                boolean changed2 = changed;
                for (int j = 0; j < typeVariables.length; j++) {
                    if (typeArg.equals(typeVariables[j])) {
                        typeArgs[i] = arguments[j];
                        changed2 = true;
                    }
                }
                changed = changed2;
            }
        }
        return changed;
    }

    private static Type getInheritGenericType(Class<?> clazz, Type type, TypeVariable<?> tv) {
        Class<?> class_gd = null;
        if (tv.getGenericDeclaration() instanceof Class) {
            class_gd = (Class) tv.getGenericDeclaration();
        }
        Type[] arguments = null;
        if (class_gd != clazz) {
            Type[] arguments2 = null;
            Class<?> c = clazz;
            while (c != null && c != Object.class && c != class_gd) {
                Type superType = c.getGenericSuperclass();
                if (superType instanceof ParameterizedType) {
                    Type[] p_superType_args = ((ParameterizedType) superType).getActualTypeArguments();
                    getArgument(p_superType_args, c.getTypeParameters(), arguments2);
                    arguments2 = p_superType_args;
                }
                c = c.getSuperclass();
            }
            arguments = arguments2;
        } else if (type instanceof ParameterizedType) {
            arguments = ((ParameterizedType) type).getActualTypeArguments();
        }
        if (arguments == null || class_gd == null) {
            return null;
        }
        TypeVariable<?>[] typeVariables = class_gd.getTypeParameters();
        for (int j = 0; j < typeVariables.length; j++) {
            if (tv.equals(typeVariables[j])) {
                return arguments[j];
            }
        }
        return null;
    }

    public String toString() {
        return this.name;
    }

    public Member getMember() {
        if (this.method != null) {
            return this.method;
        }
        return this.field;
    }

    /* access modifiers changed from: protected */
    public Class<?> getDeclaredClass() {
        if (this.method != null) {
            return this.method.getDeclaringClass();
        }
        if (this.field != null) {
            return this.field.getDeclaringClass();
        }
        return null;
    }

    public int compareTo(FieldInfo o) {
        if (this.ordinal < o.ordinal) {
            return -1;
        }
        if (this.ordinal > o.ordinal) {
            return 1;
        }
        int result = this.name.compareTo(o.name);
        if (result != 0) {
            return result;
        }
        Class<?> thisDeclaringClass = getDeclaredClass();
        Class<?> otherDeclaringClass = o.getDeclaredClass();
        if (!(thisDeclaringClass == null || otherDeclaringClass == null || thisDeclaringClass == otherDeclaringClass)) {
            if (thisDeclaringClass.isAssignableFrom(otherDeclaringClass)) {
                return -1;
            }
            if (otherDeclaringClass.isAssignableFrom(thisDeclaringClass)) {
                return 1;
            }
        }
        boolean oSameType = false;
        boolean isSampeType = this.field != null && this.field.getType() == this.fieldClass;
        if (o.field != null && o.field.getType() == o.fieldClass) {
            oSameType = true;
        }
        if (isSampeType && !oSameType) {
            return 1;
        }
        if (oSameType && !isSampeType) {
            return -1;
        }
        if (o.fieldClass.isPrimitive() && !this.fieldClass.isPrimitive()) {
            return 1;
        }
        if (this.fieldClass.isPrimitive() && !o.fieldClass.isPrimitive()) {
            return -1;
        }
        if (o.fieldClass.getName().startsWith("java.") && !this.fieldClass.getName().startsWith("java.")) {
            return 1;
        }
        if (!this.fieldClass.getName().startsWith("java.") || o.fieldClass.getName().startsWith("java.")) {
            return this.fieldClass.getName().compareTo(o.fieldClass.getName());
        }
        return -1;
    }

    public JSONField getAnnotation() {
        if (this.fieldAnnotation != null) {
            return this.fieldAnnotation;
        }
        return this.methodAnnotation;
    }

    public String getFormat() {
        return this.format;
    }

    public Object get(Object javaObject) throws IllegalAccessException, InvocationTargetException {
        if (this.method != null) {
            return this.method.invoke(javaObject, new Object[0]);
        }
        return this.field.get(javaObject);
    }

    public void set(Object javaObject, Object value) throws IllegalAccessException, InvocationTargetException {
        if (this.method != null) {
            this.method.invoke(javaObject, value);
            return;
        }
        this.field.set(javaObject, value);
    }

    public void setAccessible() throws SecurityException {
        if (this.method != null) {
            TypeUtils.setAccessible(this.method);
        } else {
            TypeUtils.setAccessible(this.field);
        }
    }
}
