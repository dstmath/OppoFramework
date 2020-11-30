package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

public class BooleanCodec implements ObjectDeserializer, ObjectSerializer {
    public static final BooleanCodec instance = new BooleanCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        Boolean value = (Boolean) object;
        if (value == null) {
            out.writeNull(SerializerFeature.WriteNullBooleanAsFalse);
        } else if (value.booleanValue()) {
            out.write("true");
        } else {
            out.write("false");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0053 A[RETURN] */
    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        Boolean boolObj;
        Boolean boolObj2;
        JSONLexer lexer = parser.lexer;
        try {
            if (lexer.token() == 6) {
                lexer.nextToken(16);
                boolObj = (T) Boolean.TRUE;
            } else if (lexer.token() == 7) {
                lexer.nextToken(16);
                boolObj = Boolean.FALSE;
            } else {
                if (lexer.token() == 2) {
                    int intValue = lexer.intValue();
                    lexer.nextToken(16);
                    if (intValue == 1) {
                        boolObj2 = Boolean.TRUE;
                    } else {
                        boolObj2 = Boolean.FALSE;
                    }
                    boolObj = (T) boolObj2;
                } else {
                    Object value = parser.parse();
                    if (value == null) {
                        return null;
                    }
                    boolObj = (T) TypeUtils.castToBoolean(value);
                }
                return clazz != AtomicBoolean.class ? (T) new AtomicBoolean(boolObj.booleanValue()) : (T) boolObj;
            }
            if (clazz != AtomicBoolean.class) {
            }
        } catch (Exception ex) {
            throw new JSONException("parseBoolean error, field : " + fieldName, ex);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 6;
    }
}
