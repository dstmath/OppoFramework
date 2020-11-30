package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GuavaCodec implements ObjectDeserializer, ObjectSerializer {
    public static GuavaCodec instance = new GuavaCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter serializeWriter = serializer.out;
        if (object instanceof Multimap) {
            serializer.write(((Multimap) object).asMap());
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        if (type != ArrayListMultimap.class) {
            return null;
        }
        T t = (T) ArrayListMultimap.create();
        for (Map.Entry entry : parser.parseObject().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Collection) {
                t.putAll(entry.getKey(), (List) value);
            } else {
                t.put(entry.getKey(), value);
            }
        }
        return t;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 0;
    }
}
