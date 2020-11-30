package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

public class CharArrayCodec implements ObjectDeserializer {
    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v3, resolved type: char[] */
    /* JADX WARN: Multi-variable type inference failed */
    public static <T> T deserialze(DefaultJSONParser parser) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 4) {
            String val = lexer.stringVal();
            lexer.nextToken(16);
            return (T) val.toCharArray();
        } else if (lexer.token() == 2) {
            Number val2 = lexer.integerValue();
            lexer.nextToken(16);
            return (T) val2.toString().toCharArray();
        } else {
            Object value = parser.parse();
            if (value instanceof String) {
                return (T) ((String) value).toCharArray();
            }
            if (value instanceof Collection) {
                Collection<?> collection = (Collection) value;
                boolean accept = true;
                Iterator<?> it = collection.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Object item = it.next();
                    if ((item instanceof String) && ((String) item).length() != 1) {
                        accept = false;
                        break;
                    }
                }
                if (accept) {
                    char[] chars = new char[collection.size()];
                    int pos = 0;
                    Iterator<?> it2 = collection.iterator();
                    while (it2.hasNext()) {
                        chars[pos] = ((String) it2.next()).charAt(0);
                        pos++;
                    }
                    return chars;
                }
                throw new JSONException("can not cast to char[]");
            } else if (value == null) {
                return null;
            } else {
                return (T) JSON.toJSONString(value).toCharArray();
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 4;
    }
}
