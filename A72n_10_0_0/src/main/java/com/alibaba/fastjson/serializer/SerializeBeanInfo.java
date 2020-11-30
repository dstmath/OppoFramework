package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.util.FieldInfo;

public class SerializeBeanInfo {
    protected final Class<?> beanType;
    protected int features;
    protected final FieldInfo[] fields;
    protected final JSONType jsonType;
    protected final FieldInfo[] sortedFields;
    protected final String typeKey;
    protected final String typeName;

    public SerializeBeanInfo(Class<?> beanType2, JSONType jsonType2, String typeName2, String typeKey2, int features2, FieldInfo[] fields2, FieldInfo[] sortedFields2) {
        this.beanType = beanType2;
        this.jsonType = jsonType2;
        this.typeName = typeName2;
        this.typeKey = typeKey2;
        this.features = features2;
        this.fields = fields2;
        this.sortedFields = sortedFields2;
    }
}
