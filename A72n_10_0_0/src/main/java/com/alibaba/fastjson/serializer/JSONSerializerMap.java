package com.alibaba.fastjson.serializer;

import java.lang.reflect.Type;

@Deprecated
public class JSONSerializerMap extends SerializeConfig {
    public final boolean put(Class<?> clazz, ObjectSerializer serializer) {
        return super.put((Type) clazz, serializer);
    }
}
