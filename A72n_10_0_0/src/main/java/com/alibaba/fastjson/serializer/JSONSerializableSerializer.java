package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class JSONSerializableSerializer implements ObjectSerializer {
    public static JSONSerializableSerializer instance = new JSONSerializableSerializer();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        JSONSerializable jsonSerializable = (JSONSerializable) object;
        if (jsonSerializable == null) {
            serializer.writeNull();
        } else {
            jsonSerializable.write(serializer, fieldName, fieldType, features);
        }
    }
}
