package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import java.lang.reflect.Type;

public class StackTraceElementDeserializer implements ObjectDeserializer {
    public static final StackTraceElementDeserializer instance = new StackTraceElementDeserializer();

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken();
            return null;
        } else if (lexer.token() == 12 || lexer.token() == 16) {
            String declaringClass = null;
            String methodName = null;
            String fileName = null;
            int lineNumber = 0;
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
                if ("className".equals(key)) {
                    if (lexer.token() == 8) {
                        declaringClass = null;
                    } else if (lexer.token() == 4) {
                        declaringClass = lexer.stringVal();
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if ("methodName".equals(key)) {
                    if (lexer.token() == 8) {
                        methodName = null;
                    } else if (lexer.token() == 4) {
                        methodName = lexer.stringVal();
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if ("fileName".equals(key)) {
                    if (lexer.token() == 8) {
                        fileName = null;
                    } else if (lexer.token() == 4) {
                        fileName = lexer.stringVal();
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if ("lineNumber".equals(key)) {
                    if (lexer.token() == 8) {
                        lineNumber = 0;
                    } else if (lexer.token() == 2) {
                        lineNumber = lexer.intValue();
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if ("nativeMethod".equals(key)) {
                    if (lexer.token() == 8) {
                        lexer.nextToken(16);
                    } else if (lexer.token() == 6) {
                        lexer.nextToken(16);
                    } else if (lexer.token() == 7) {
                        lexer.nextToken(16);
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if (key == JSON.DEFAULT_TYPE_KEY) {
                    if (lexer.token() == 4) {
                        String elementType = lexer.stringVal();
                        if (!elementType.equals("java.lang.StackTraceElement")) {
                            throw new JSONException("syntax error : " + elementType);
                        }
                    } else if (lexer.token() != 8) {
                        throw new JSONException("syntax error");
                    }
                } else if ("moduleName".equals(key)) {
                    if (lexer.token() != 8) {
                        if (lexer.token() == 4) {
                            lexer.stringVal();
                        } else {
                            throw new JSONException("syntax error");
                        }
                    }
                } else if ("moduleVersion".equals(key)) {
                    if (lexer.token() != 8) {
                        if (lexer.token() == 4) {
                            lexer.stringVal();
                        } else {
                            throw new JSONException("syntax error");
                        }
                    }
                } else if (!"classLoaderName".equals(key)) {
                    throw new JSONException("syntax error : " + key);
                } else if (lexer.token() != 8) {
                    if (lexer.token() == 4) {
                        lexer.stringVal();
                    } else {
                        throw new JSONException("syntax error");
                    }
                }
                if (lexer.token() == 13) {
                    lexer.nextToken(16);
                    break;
                }
            }
            return (T) new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
        } else {
            throw new JSONException("syntax error: " + JSONToken.name(lexer.token()));
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 12;
    }
}
