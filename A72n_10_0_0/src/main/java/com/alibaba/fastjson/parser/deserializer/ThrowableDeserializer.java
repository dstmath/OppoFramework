package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.ParserConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ThrowableDeserializer extends JavaBeanDeserializer {
    public ThrowableDeserializer(ParserConfig mapping, Class<?> clazz) {
        super(mapping, clazz, clazz);
    }

    /* JADX WARNING: Removed duplicated region for block: B:92:0x0198  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0106 A[SYNTHETIC] */
    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer, com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        boolean z;
        String message;
        Throwable ex;
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken();
            return null;
        }
        if (parser.getResolveStatus() == 2) {
            parser.setResolveStatus(0);
        } else if (lexer.token() != 12) {
            throw new JSONException("syntax error");
        }
        Class<?> exClass = null;
        if (type != null && (type instanceof Class)) {
            Class<?> clazz = (Class) type;
            if (Throwable.class.isAssignableFrom(clazz)) {
                exClass = clazz;
            }
        }
        StackTraceElement[] stackTrace = null;
        String message2 = null;
        Throwable cause = null;
        Map<String, Object> otherValues = null;
        while (true) {
            String key = lexer.scanSymbol(parser.getSymbolTable());
            if (key == null) {
                if (lexer.token() == 13) {
                    lexer.nextToken(16);
                    break;
                } else if (lexer.token() == 16 && lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                }
            }
            lexer.nextTokenWithColon(4);
            if (!JSON.DEFAULT_TYPE_KEY.equals(key)) {
                if (!"message".equals(key)) {
                    if ("cause".equals(key)) {
                        z = false;
                        cause = (Throwable) deserialze(parser, null, "cause");
                    } else {
                        z = false;
                        if ("stackTrace".equals(key)) {
                            stackTrace = (StackTraceElement[]) parser.parseObject((Class<Object>) StackTraceElement[].class);
                        } else {
                            if (otherValues == null) {
                                otherValues = new HashMap<>();
                            }
                            otherValues.put(key, parser.parse());
                        }
                    }
                    if (lexer.token() != 13) {
                        lexer.nextToken(16);
                        break;
                    }
                } else {
                    if (lexer.token() == 8) {
                        message = null;
                    } else if (lexer.token() == 4) {
                        message = lexer.stringVal();
                    } else {
                        throw new JSONException("syntax error");
                    }
                    lexer.nextToken();
                    message2 = message;
                }
            } else if (lexer.token() == 4) {
                Class<?> exClass2 = parser.getConfig().checkAutoType(lexer.stringVal(), Throwable.class, lexer.getFeatures());
                lexer.nextToken(16);
                exClass = exClass2;
            } else {
                throw new JSONException("syntax error");
            }
            z = false;
            if (lexer.token() != 13) {
            }
        }
        if (exClass == null) {
            ex = (T) new Exception(message2, cause);
        } else if (Throwable.class.isAssignableFrom(exClass)) {
            try {
                Throwable ex2 = createException(message2, cause, exClass);
                ex = ex2 == null ? (T) new Exception(message2, cause) : (T) ex2;
            } catch (Exception e) {
                throw new JSONException("create instance error", e);
            }
        } else {
            throw new JSONException("type not match, not Throwable. " + exClass.getName());
        }
        if (stackTrace != null) {
            ex.setStackTrace(stackTrace);
        }
        if (otherValues != null) {
            JavaBeanDeserializer exBeanDeser = null;
            if (exClass != null) {
                if (exClass == this.clazz) {
                    exBeanDeser = this;
                } else {
                    ObjectDeserializer exDeser = parser.getConfig().getDeserializer(exClass);
                    if (exDeser instanceof JavaBeanDeserializer) {
                        exBeanDeser = (JavaBeanDeserializer) exDeser;
                    }
                }
            }
            if (exBeanDeser != null) {
                for (Map.Entry<String, Object> entry : otherValues.entrySet()) {
                    Object value = entry.getValue();
                    FieldDeserializer fieldDeserializer = exBeanDeser.getFieldDeserializer(entry.getKey());
                    if (fieldDeserializer != null) {
                        fieldDeserializer.setValue(ex, value);
                    }
                }
            }
        }
        return (T) ex;
    }

    private Throwable createException(String message, Throwable cause, Class<?> exClass) throws Exception {
        Constructor<?> causeConstructor = null;
        Constructor<?>[] constructors = exClass.getConstructors();
        Constructor<?> messageConstructor = null;
        Constructor<?> defaultConstructor = null;
        for (Constructor<?> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 0) {
                defaultConstructor = constructor;
            } else if (types.length == 1 && types[0] == String.class) {
                messageConstructor = constructor;
            } else if (types.length == 2 && types[0] == String.class && types[1] == Throwable.class) {
                causeConstructor = constructor;
            }
        }
        if (causeConstructor != null) {
            return (Throwable) causeConstructor.newInstance(message, cause);
        } else if (messageConstructor != null) {
            return (Throwable) messageConstructor.newInstance(message);
        } else if (defaultConstructor != null) {
            return (Throwable) defaultConstructor.newInstance(new Object[0]);
        } else {
            return null;
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer, com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer
    public int getFastMatchToken() {
        return 12;
    }
}
