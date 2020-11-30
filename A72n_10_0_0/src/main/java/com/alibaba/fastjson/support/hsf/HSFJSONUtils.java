package com.alibaba.fastjson.support.hsf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexerBase;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.SymbolTable;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class HSFJSONUtils {
    static final char[] fieldName_argsObjs = "\"argsObjs\"".toCharArray();
    static final char[] fieldName_argsTypes = "\"argsTypes\"".toCharArray();
    static final char[] fieldName_type = "\"@type\":".toCharArray();
    static final SymbolTable typeSymbolTable = new SymbolTable(1024);

    public static Object[] parseInvocationArguments(String json, MethodLocator methodLocator) {
        Object[] values;
        DefaultJSONParser parser = new DefaultJSONParser(json);
        JSONLexerBase lexer = (JSONLexerBase) parser.getLexer();
        Object[] values2 = null;
        ParseContext rootContext = parser.setContext(null, null);
        int token = lexer.token();
        int i = 0;
        if (token == 12) {
            String[] typeNames = lexer.scanFieldStringArray(fieldName_argsTypes, -1, typeSymbolTable);
            if (typeNames == null && lexer.matchStat == -2 && "com.alibaba.fastjson.JSONObject".equals(lexer.scanFieldString(fieldName_type))) {
                typeNames = lexer.scanFieldStringArray(fieldName_argsTypes, -1, typeSymbolTable);
            }
            Method method = methodLocator.findMethod(typeNames);
            if (method == null) {
                lexer.close();
                JSONObject jsonObject = JSON.parseObject(json);
                Method method2 = methodLocator.findMethod((String[]) jsonObject.getObject("argsTypes", (Class<Object>) String[].class));
                JSONArray argsObjs = jsonObject.getJSONArray("argsObjs");
                if (argsObjs == null) {
                    values = null;
                } else {
                    Type[] argTypes = method2.getGenericParameterTypes();
                    Object[] values3 = new Object[argTypes.length];
                    while (i < argTypes.length) {
                        values3[i] = argsObjs.getObject(i, argTypes[i]);
                        i++;
                    }
                    values = values3;
                }
                return values;
            }
            Type[] argTypes2 = method.getGenericParameterTypes();
            lexer.skipWhitespace();
            if (lexer.getCurrent() == ',') {
                lexer.next();
            }
            if (lexer.matchField2(fieldName_argsObjs)) {
                lexer.nextToken();
                ParseContext context = parser.setContext(rootContext, null, "argsObjs");
                Object[] values4 = parser.parseArray(argTypes2);
                context.object = values4;
                parser.accept(13);
                parser.handleResovleTask(null);
                values2 = values4;
            }
            parser.close();
            return values2;
        } else if (token != 14) {
            return null;
        } else {
            String[] typeNames2 = lexer.scanFieldStringArray(null, -1, typeSymbolTable);
            lexer.skipWhitespace();
            char ch = lexer.getCurrent();
            if (ch == ']') {
                Type[] argTypes3 = methodLocator.findMethod(null).getGenericParameterTypes();
                Object[] values5 = new Object[typeNames2.length];
                while (i < typeNames2.length) {
                    Type argType = argTypes3[i];
                    String typeName = typeNames2[i];
                    if (argType != String.class) {
                        values5[i] = TypeUtils.cast(typeName, argType, parser.getConfig());
                    } else {
                        values5[i] = typeName;
                    }
                    i++;
                }
                return values5;
            }
            if (ch == ',') {
                lexer.next();
                lexer.skipWhitespace();
            }
            lexer.nextToken(14);
            Object[] values6 = parser.parseArray(methodLocator.findMethod(typeNames2).getGenericParameterTypes());
            lexer.close();
            return values6;
        }
    }
}
