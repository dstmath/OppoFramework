package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Time;

public class TimeDeserializer implements ObjectDeserializer {
    public static final TimeDeserializer instance = new TimeDeserializer();

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        long longVal;
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 16) {
            lexer.nextToken(4);
            if (lexer.token() == 4) {
                lexer.nextTokenWithColon(2);
                if (lexer.token() == 2) {
                    long time = lexer.longValue();
                    lexer.nextToken(13);
                    if (lexer.token() == 13) {
                        lexer.nextToken(16);
                        return (T) new Time(time);
                    }
                    throw new JSONException("syntax error");
                }
                throw new JSONException("syntax error");
            }
            throw new JSONException("syntax error");
        }
        T t = (T) parser.parse();
        if (t == null) {
            return null;
        }
        if (t instanceof Time) {
            return t;
        }
        if (t instanceof BigDecimal) {
            return (T) new Time(TypeUtils.longValue(t));
        }
        if (t instanceof Number) {
            return (T) new Time(t.longValue());
        }
        if (t instanceof String) {
            T strVal = t;
            if (strVal.length() == 0) {
                return null;
            }
            JSONScanner dateLexer = new JSONScanner(strVal);
            if (dateLexer.scanISO8601DateIfMatch()) {
                longVal = dateLexer.getCalendar().getTimeInMillis();
            } else {
                boolean isDigit = true;
                int i = 0;
                while (true) {
                    if (i >= strVal.length()) {
                        break;
                    }
                    char ch = strVal.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isDigit = false;
                    } else {
                        i++;
                    }
                }
                isDigit = false;
                if (!isDigit) {
                    dateLexer.close();
                    return (T) Time.valueOf(strVal);
                }
                longVal = Long.parseLong(strVal);
            }
            dateLexer.close();
            return (T) new Time(longVal);
        }
        throw new JSONException("parse error");
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
