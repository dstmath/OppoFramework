package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

public class ReferenceCodec implements ObjectDeserializer, ObjectSerializer {
    public static final ReferenceCodec instance = new ReferenceCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        Object item;
        if (object instanceof AtomicReference) {
            item = ((AtomicReference) object).get();
        } else {
            item = ((Reference) object).get();
        }
        serializer.write(item);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        ParameterizedType paramType = (ParameterizedType) type;
        Object itemObject = parser.parseObject(paramType.getActualTypeArguments()[0]);
        Type rawType = paramType.getRawType();
        if (rawType == AtomicReference.class) {
            return (T) new AtomicReference(itemObject);
        }
        if (rawType == WeakReference.class) {
            return (T) new WeakReference(itemObject);
        }
        if (rawType == SoftReference.class) {
            return (T) new SoftReference(itemObject);
        }
        throw new UnsupportedOperationException(rawType.toString());
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 12;
    }
}
