package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IntegerCodec implements ObjectDeserializer, ObjectSerializer {
    public static IntegerCodec instance = new IntegerCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        Number value = (Number) object;
        if (value == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        if (object instanceof Long) {
            out.writeLong(value.longValue());
        } else {
            out.writeInt(value.intValue());
        }
        if (out.isEnabled(SerializerFeature.WriteClassName)) {
            Class<?> clazz = value.getClass();
            if (clazz == Byte.class) {
                out.write(66);
            } else if (clazz == Short.class) {
                out.write(83);
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        Integer intObj;
        JSONLexer lexer = parser.lexer;
        int token = lexer.token();
        if (token == 8) {
            lexer.nextToken(16);
            return null;
        }
        if (token == 2) {
            try {
                int val = lexer.intValue();
                lexer.nextToken(16);
                intObj = (T) Integer.valueOf(val);
            } catch (Exception ex) {
                String message = "parseInt error";
                if (fieldName != null) {
                    message = message + ", field : " + fieldName;
                }
                throw new JSONException(message, ex);
            }
        } else if (token == 3) {
            Integer intObj2 = Integer.valueOf(TypeUtils.intValue(lexer.decimalValue()));
            lexer.nextToken(16);
            intObj = (T) intObj2;
        } else if (token == 12) {
            JSONObject jsonObject = new JSONObject(true);
            parser.parseObject((Map) jsonObject);
            intObj = (T) TypeUtils.castToInt(jsonObject);
        } else {
            intObj = (T) TypeUtils.castToInt(parser.parse());
        }
        return clazz == AtomicInteger.class ? (T) new AtomicInteger(intObj.intValue()) : (T) intObj;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
