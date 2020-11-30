package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

public class CollectionCodec implements ObjectDeserializer, ObjectSerializer {
    public static final CollectionCodec instance = new CollectionCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        Type elementType = null;
        if (out.isEnabled(SerializerFeature.WriteClassName) || SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName)) {
            elementType = TypeUtils.getCollectionItemType(fieldType);
        }
        Collection<?> collection = (Collection) object;
        SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        if (out.isEnabled(SerializerFeature.WriteClassName)) {
            if (HashSet.class == collection.getClass()) {
                out.append((CharSequence) "Set");
            } else if (TreeSet.class == collection.getClass()) {
                out.append((CharSequence) "TreeSet");
            }
        }
        int i = 0;
        try {
            out.append('[');
            for (Object item : collection) {
                int i2 = i + 1;
                if (i != 0) {
                    out.append(',');
                }
                if (item == null) {
                    out.writeNull();
                } else {
                    Class<?> clazz = item.getClass();
                    if (clazz == Integer.class) {
                        out.writeInt(((Integer) item).intValue());
                    } else if (clazz == Long.class) {
                        out.writeLong(((Long) item).longValue());
                        if (out.isEnabled(SerializerFeature.WriteClassName)) {
                            out.write(76);
                        }
                    } else {
                        ObjectSerializer itemSerializer = serializer.getObjectWriter(clazz);
                        if (!SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName) || !(itemSerializer instanceof JavaBeanSerializer)) {
                            itemSerializer.write(serializer, item, Integer.valueOf(i2 - 1), elementType, features);
                        } else {
                            ((JavaBeanSerializer) itemSerializer).writeNoneASM(serializer, item, Integer.valueOf(i2 - 1), elementType, features);
                        }
                    }
                }
                i = i2;
            }
            out.append(']');
        } finally {
            serializer.context = context;
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        if (parser.lexer.token() == 8) {
            parser.lexer.nextToken(16);
            return null;
        } else if (type == JSONArray.class) {
            T t = (T) new JSONArray();
            parser.parseArray((Collection) t);
            return t;
        } else {
            T t2 = (T) TypeUtils.createCollection(type);
            parser.parseArray(TypeUtils.getCollectionItemType(type), t2, fieldName);
            return t2;
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 14;
    }
}
