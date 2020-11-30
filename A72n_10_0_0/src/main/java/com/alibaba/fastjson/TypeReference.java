package com.alibaba.fastjson;

import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TypeReference<T> {
    public static final Type LIST_STRING = new TypeReference<List<String>>() {
        /* class com.alibaba.fastjson.TypeReference.AnonymousClass1 */
    }.getType();
    static ConcurrentMap<Type, Type> classTypeCache = new ConcurrentHashMap(16, 0.75f, 1);
    protected final Type type;

    protected TypeReference() {
        Type type2 = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Type cachedType = classTypeCache.get(type2);
        if (cachedType == null) {
            classTypeCache.putIfAbsent(type2, type2);
            cachedType = classTypeCache.get(type2);
        }
        this.type = cachedType;
    }

    protected TypeReference(Type... actualTypeArguments) {
        Class<?> thisClass = getClass();
        ParameterizedType argType = (ParameterizedType) ((ParameterizedType) thisClass.getGenericSuperclass()).getActualTypeArguments()[0];
        Type rawType = argType.getRawType();
        Type[] argTypes = argType.getActualTypeArguments();
        int actualIndex = 0;
        for (int i = 0; i < argTypes.length; i++) {
            if ((argTypes[i] instanceof TypeVariable) && actualIndex < actualTypeArguments.length) {
                argTypes[i] = actualTypeArguments[actualIndex];
                actualIndex++;
            }
            if (argTypes[i] instanceof GenericArrayType) {
                argTypes[i] = TypeUtils.checkPrimitiveArray((GenericArrayType) argTypes[i]);
            }
            if (argTypes[i] instanceof ParameterizedType) {
                argTypes[i] = handlerParameterizedType((ParameterizedType) argTypes[i], actualTypeArguments, actualIndex);
            }
        }
        Type key = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
        Type cachedType = classTypeCache.get(key);
        if (cachedType == null) {
            classTypeCache.putIfAbsent(key, key);
            cachedType = classTypeCache.get(key);
        }
        this.type = cachedType;
    }

    private Type handlerParameterizedType(ParameterizedType type2, Type[] actualTypeArguments, int actualIndex) {
        Class<?> thisClass = getClass();
        Type rawType = type2.getRawType();
        Type[] argTypes = type2.getActualTypeArguments();
        for (int i = 0; i < argTypes.length; i++) {
            if ((argTypes[i] instanceof TypeVariable) && actualIndex < actualTypeArguments.length) {
                argTypes[i] = actualTypeArguments[actualIndex];
                actualIndex++;
            }
            if (argTypes[i] instanceof GenericArrayType) {
                argTypes[i] = TypeUtils.checkPrimitiveArray((GenericArrayType) argTypes[i]);
            }
            if (argTypes[i] instanceof ParameterizedType) {
                return handlerParameterizedType((ParameterizedType) argTypes[i], actualTypeArguments, actualIndex);
            }
        }
        return new ParameterizedTypeImpl(argTypes, thisClass, rawType);
    }

    public Type getType() {
        return this.type;
    }
}
