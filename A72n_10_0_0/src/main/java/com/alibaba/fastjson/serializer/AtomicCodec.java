package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicCodec implements ObjectDeserializer, ObjectSerializer {
    public static final AtomicCodec instance = new AtomicCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object instanceof AtomicInteger) {
            out.writeInt(((AtomicInteger) object).get());
        } else if (object instanceof AtomicLong) {
            out.writeLong(((AtomicLong) object).get());
        } else if (object instanceof AtomicBoolean) {
            out.append((CharSequence) (((AtomicBoolean) object).get() ? "true" : "false"));
        } else if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
        } else {
            int i = 0;
            if (object instanceof AtomicIntegerArray) {
                AtomicIntegerArray array = (AtomicIntegerArray) object;
                int len = array.length();
                out.write(91);
                while (i < len) {
                    int val = array.get(i);
                    if (i != 0) {
                        out.write(44);
                    }
                    out.writeInt(val);
                    i++;
                }
                out.write(93);
                return;
            }
            AtomicLongArray array2 = (AtomicLongArray) object;
            int len2 = array2.length();
            out.write(91);
            while (i < len2) {
                long val2 = array2.get(i);
                if (i != 0) {
                    out.write(44);
                }
                out.writeLong(val2);
                i++;
            }
            out.write(93);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        if (parser.lexer.token() == 8) {
            parser.lexer.nextToken(16);
            return null;
        }
        JSONArray array = new JSONArray();
        parser.parseArray(array);
        int i = 0;
        if (clazz == AtomicIntegerArray.class) {
            T t = (T) new AtomicIntegerArray(array.size());
            while (i < array.size()) {
                t.set(i, array.getInteger(i).intValue());
                i++;
            }
            return t;
        }
        T t2 = (T) new AtomicLongArray(array.size());
        while (i < array.size()) {
            t2.set(i, array.getLong(i).longValue());
            i++;
        }
        return t2;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 14;
    }
}
