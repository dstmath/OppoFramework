package com.alibaba.fastjson.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

public class GenericArrayTypeImpl implements GenericArrayType {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Type genericComponentType;

    public GenericArrayTypeImpl(Type genericComponentType2) {
        this.genericComponentType = genericComponentType2;
    }

    public Type getGenericComponentType() {
        return this.genericComponentType;
    }

    public String toString() {
        Type genericComponentType2 = getGenericComponentType();
        StringBuilder builder = new StringBuilder();
        if (genericComponentType2 instanceof Class) {
            builder.append(((Class) genericComponentType2).getName());
        } else {
            builder.append(genericComponentType2.toString());
        }
        builder.append("[]");
        return builder.toString();
    }

    public boolean equals(Object obj) {
        if (obj instanceof GenericArrayType) {
            return this.genericComponentType.equals(((GenericArrayType) obj).getGenericComponentType());
        }
        return false;
    }

    public int hashCode() {
        return this.genericComponentType.hashCode();
    }
}
