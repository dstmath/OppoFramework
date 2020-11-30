package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FloatCodec implements ObjectDeserializer, ObjectSerializer {
    public static FloatCodec instance = new FloatCodec();
    private NumberFormat decimalFormat;

    public FloatCodec() {
    }

    public FloatCodec(DecimalFormat decimalFormat2) {
        this.decimalFormat = decimalFormat2;
    }

    public FloatCodec(String decimalFormat2) {
        this(new DecimalFormat(decimalFormat2));
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        float floatValue = ((Float) object).floatValue();
        if (this.decimalFormat != null) {
            out.write(this.decimalFormat.format((double) floatValue));
        } else {
            out.writeFloat(floatValue, true);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        try {
            return (T) deserialze(parser);
        } catch (Exception ex) {
            throw new JSONException("parseLong error, field : " + fieldName, ex);
        }
    }

    public static <T> T deserialze(DefaultJSONParser parser) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            String val = lexer.numberString();
            lexer.nextToken(16);
            return (T) Float.valueOf(Float.parseFloat(val));
        } else if (lexer.token() == 3) {
            float val2 = lexer.floatValue();
            lexer.nextToken(16);
            return (T) Float.valueOf(val2);
        } else {
            Object value = parser.parse();
            if (value == null) {
                return null;
            }
            return (T) TypeUtils.castToFloat(value);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
