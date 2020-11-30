package com.alibaba.fastjson.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ParameterizedTypeImpl implements ParameterizedType {
    private final Type[] actualTypeArguments;
    private final Type ownerType;
    private final Type rawType;

    public ParameterizedTypeImpl(Type[] actualTypeArguments2, Type ownerType2, Type rawType2) {
        this.actualTypeArguments = actualTypeArguments2;
        this.ownerType = ownerType2;
        this.rawType = rawType2;
    }

    public Type[] getActualTypeArguments() {
        return this.actualTypeArguments;
    }

    public Type getOwnerType() {
        return this.ownerType;
    }

    public Type getRawType() {
        return this.rawType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterizedTypeImpl that = (ParameterizedTypeImpl) o;
        if (!Arrays.equals(this.actualTypeArguments, that.actualTypeArguments)) {
            return false;
        }
        if (this.ownerType == null ? that.ownerType != null : !this.ownerType.equals(that.ownerType)) {
            return false;
        }
        if (this.rawType != null) {
            return this.rawType.equals(that.rawType);
        }
        if (that.rawType == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = 31 * ((31 * (this.actualTypeArguments != null ? Arrays.hashCode(this.actualTypeArguments) : 0)) + (this.ownerType != null ? this.ownerType.hashCode() : 0));
        if (this.rawType != null) {
            i = this.rawType.hashCode();
        }
        return hashCode + i;
    }
}
