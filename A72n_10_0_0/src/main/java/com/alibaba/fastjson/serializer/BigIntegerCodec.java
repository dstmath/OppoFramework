package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;

public class BigIntegerCodec implements ObjectDeserializer, ObjectSerializer {
    private static final BigInteger HIGH = BigInteger.valueOf(9007199254740991L);
    private static final BigInteger LOW = BigInteger.valueOf(-9007199254740991L);
    public static final BigIntegerCodec instance = new BigIntegerCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        BigInteger val = (BigInteger) object;
        String str = val.toString();
        if (str.length() < 16 || !SerializerFeature.isEnabled(features, out.features, SerializerFeature.BrowserCompatible) || (val.compareTo(LOW) >= 0 && val.compareTo(HIGH) <= 0)) {
            out.write(str);
        } else {
            out.writeString(str);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser);
    }

    public static <T> T deserialze(DefaultJSONParser parser) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            String val = lexer.numberString();
            lexer.nextToken(16);
            return (T) new BigInteger(val);
        }
        Object value = parser.parse();
        if (value == null) {
            return null;
        }
        return (T) TypeUtils.castToBigInteger(value);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
