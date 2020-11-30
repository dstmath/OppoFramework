package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;

public class BigDecimalCodec implements ObjectDeserializer, ObjectSerializer {
    static final BigDecimal HIGH = BigDecimal.valueOf(9007199254740991L);
    static final BigDecimal LOW = BigDecimal.valueOf(-9007199254740991L);
    public static final BigDecimalCodec instance = new BigDecimalCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        String outText;
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        BigDecimal val = (BigDecimal) object;
        int scale = val.scale();
        if (!out.isEnabled(SerializerFeature.WriteBigDecimalAsPlain) || scale < -100 || scale >= 100) {
            outText = val.toString();
        } else {
            outText = val.toPlainString();
        }
        if (scale != 0 || outText.length() < 16 || !SerializerFeature.isEnabled(features, out.features, SerializerFeature.BrowserCompatible) || (val.compareTo(LOW) >= 0 && val.compareTo(HIGH) <= 0)) {
            out.write(outText);
            if (out.isEnabled(SerializerFeature.WriteClassName) && fieldType != BigDecimal.class && val.scale() == 0) {
                out.write(46);
                return;
            }
            return;
        }
        out.writeString(outText);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        try {
            return (T) deserialze(parser);
        } catch (Exception ex) {
            throw new JSONException("parseDecimal error, field : " + fieldName, ex);
        }
    }

    public static <T> T deserialze(DefaultJSONParser parser) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            T t = (T) lexer.decimalValue();
            lexer.nextToken(16);
            return t;
        } else if (lexer.token() == 3) {
            T t2 = (T) lexer.decimalValue();
            lexer.nextToken(16);
            return t2;
        } else {
            Object value = parser.parse();
            if (value == null) {
                return null;
            }
            return (T) TypeUtils.castToBigDecimal(value);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
