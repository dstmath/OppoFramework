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
import java.util.concurrent.atomic.AtomicLong;

public class LongCodec implements ObjectDeserializer, ObjectSerializer {
    public static LongCodec instance = new LongCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        long value = ((Long) object).longValue();
        out.writeLong(value);
        if (out.isEnabled(SerializerFeature.WriteClassName) && value <= 2147483647L && value >= -2147483648L && fieldType != Long.class && fieldType != Long.TYPE) {
            out.write(76);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        Long longObject;
        JSONLexer lexer = parser.lexer;
        try {
            int token = lexer.token();
            if (token == 2) {
                long longValue = lexer.longValue();
                lexer.nextToken(16);
                longObject = (T) Long.valueOf(longValue);
            } else if (token == 3) {
                Long longObject2 = Long.valueOf(TypeUtils.longValue(lexer.decimalValue()));
                lexer.nextToken(16);
                longObject = (T) longObject2;
            } else {
                if (token == 12) {
                    JSONObject jsonObject = new JSONObject(true);
                    parser.parseObject((Map) jsonObject);
                    longObject = (T) TypeUtils.castToLong(jsonObject);
                } else {
                    longObject = (T) TypeUtils.castToLong(parser.parse());
                }
                if (longObject == null) {
                    return null;
                }
            }
            return clazz == AtomicLong.class ? (T) new AtomicLong(longObject.longValue()) : (T) longObject;
        } catch (Exception ex) {
            throw new JSONException("parseLong error, field : " + fieldName, ex);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
