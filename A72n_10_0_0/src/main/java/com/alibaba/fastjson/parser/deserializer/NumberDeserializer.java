package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Type;
import java.math.BigDecimal;

public class NumberDeserializer implements ObjectDeserializer {
    public static final NumberDeserializer instance = new NumberDeserializer();

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            if (clazz == Double.TYPE || clazz == Double.class) {
                String val = lexer.numberString();
                lexer.nextToken(16);
                return (T) Double.valueOf(Double.parseDouble(val));
            }
            long val2 = lexer.longValue();
            lexer.nextToken(16);
            if (clazz == Short.TYPE || clazz == Short.class) {
                if (val2 <= 32767 && val2 >= -32768) {
                    return (T) Short.valueOf((short) ((int) val2));
                }
                throw new JSONException("short overflow : " + val2);
            } else if (clazz != Byte.TYPE && clazz != Byte.class) {
                return (val2 < -2147483648L || val2 > 2147483647L) ? (T) Long.valueOf(val2) : (T) Integer.valueOf((int) val2);
            } else {
                if (val2 <= 127 && val2 >= -128) {
                    return (T) Byte.valueOf((byte) ((int) val2));
                }
                throw new JSONException("short overflow : " + val2);
            }
        } else if (lexer.token() == 3) {
            if (clazz == Double.TYPE || clazz == Double.class) {
                String val3 = lexer.numberString();
                lexer.nextToken(16);
                return (T) Double.valueOf(Double.parseDouble(val3));
            } else if (clazz == Short.TYPE || clazz == Short.class) {
                BigDecimal val4 = lexer.decimalValue();
                lexer.nextToken(16);
                return (T) Short.valueOf(TypeUtils.shortValue(val4));
            } else if (clazz == Byte.TYPE || clazz == Byte.class) {
                BigDecimal val5 = lexer.decimalValue();
                lexer.nextToken(16);
                return (T) Byte.valueOf(TypeUtils.byteValue(val5));
            } else {
                T t = (T) lexer.decimalValue();
                lexer.nextToken(16);
                return t;
            }
        } else if (lexer.token() != 18 || !"NaN".equals(lexer.stringVal())) {
            Object value = parser.parse();
            if (value == null) {
                return null;
            }
            if (clazz == Double.TYPE || clazz == Double.class) {
                try {
                    return (T) TypeUtils.castToDouble(value);
                } catch (Exception ex) {
                    throw new JSONException("parseDouble error, field : " + fieldName, ex);
                }
            } else if (clazz == Short.TYPE || clazz == Short.class) {
                try {
                    return (T) TypeUtils.castToShort(value);
                } catch (Exception ex2) {
                    throw new JSONException("parseShort error, field : " + fieldName, ex2);
                }
            } else if (clazz != Byte.TYPE && clazz != Byte.class) {
                return (T) TypeUtils.castToBigDecimal(value);
            } else {
                try {
                    return (T) TypeUtils.castToByte(value);
                } catch (Exception ex3) {
                    throw new JSONException("parseByte error, field : " + fieldName, ex3);
                }
            }
        } else {
            lexer.nextToken();
            if (clazz == Double.class) {
                return (T) Double.valueOf(Double.NaN);
            }
            if (clazz == Float.class) {
                return (T) Float.valueOf(Float.NaN);
            }
            return null;
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
