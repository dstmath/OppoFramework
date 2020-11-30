package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.io.IOException;
import java.lang.reflect.Type;

public class StringCodec implements ObjectDeserializer, ObjectSerializer {
    public static StringCodec instance = new StringCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        write(serializer, (String) object);
    }

    public void write(JSONSerializer serializer, String value) {
        SerializeWriter out = serializer.out;
        if (value == null) {
            out.writeNull(SerializerFeature.WriteNullStringAsEmpty);
        } else {
            out.writeString(value);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        if (clazz == StringBuffer.class) {
            JSONLexer lexer = parser.lexer;
            if (lexer.token() == 4) {
                String val = lexer.stringVal();
                lexer.nextToken(16);
                return (T) new StringBuffer(val);
            }
            Object value = parser.parse();
            if (value == null) {
                return null;
            }
            return (T) new StringBuffer(value.toString());
        } else if (clazz != StringBuilder.class) {
            return (T) deserialze(parser);
        } else {
            JSONLexer lexer2 = parser.lexer;
            if (lexer2.token() == 4) {
                String val2 = lexer2.stringVal();
                lexer2.nextToken(16);
                return (T) new StringBuilder(val2);
            }
            Object value2 = parser.parse();
            if (value2 == null) {
                return null;
            }
            return (T) new StringBuilder(value2.toString());
        }
    }

    public static <T> T deserialze(DefaultJSONParser parser) {
        JSONLexer lexer = parser.getLexer();
        if (lexer.token() == 4) {
            T t = (T) lexer.stringVal();
            lexer.nextToken(16);
            return t;
        } else if (lexer.token() == 2) {
            T t2 = (T) lexer.numberString();
            lexer.nextToken(16);
            return t2;
        } else {
            Object value = parser.parse();
            if (value == null) {
                return null;
            }
            return (T) value.toString();
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 4;
    }
}
