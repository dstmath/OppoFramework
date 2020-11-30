package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Enumeration;

public class EnumerationSerializer implements ObjectSerializer {
    public static EnumerationSerializer instance = new EnumerationSerializer();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        Type elementType = null;
        if (out.isEnabled(SerializerFeature.WriteClassName) && (fieldType instanceof ParameterizedType)) {
            elementType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        }
        Enumeration<?> e = (Enumeration) object;
        SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        int i = 0;
        try {
            out.append('[');
            while (e.hasMoreElements()) {
                Object item = e.nextElement();
                int i2 = i + 1;
                if (i != 0) {
                    out.append(',');
                }
                if (item == null) {
                    out.writeNull();
                } else {
                    serializer.getObjectWriter(item.getClass()).write(serializer, item, Integer.valueOf(i2 - 1), elementType, 0);
                }
                i = i2;
            }
            out.append(']');
        } finally {
            serializer.context = context;
        }
    }
}
