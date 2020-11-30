package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.ParserConfig;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapDeserializer implements ObjectDeserializer {
    public static MapDeserializer instance = new MapDeserializer();

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v2, types: [java.util.Map] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Map<Object, Object> map;
        if (type == JSONObject.class && parser.getFieldTypeResolver() == null) {
            return (T) parser.parseObject();
        }
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken(16);
            return null;
        }
        boolean unmodifiableMap = (type instanceof Class) && "java.util.Collections$UnmodifiableMap".equals(((Class) type).getName());
        if ((lexer.getFeatures() & Feature.OrderedField.mask) != 0) {
            map = createMap(type, lexer.getFeatures());
        } else {
            map = createMap(type);
        }
        ParseContext context = parser.getContext();
        try {
            parser.setContext(context, map, fieldName);
            T t = (T) deserialze(parser, type, fieldName, map);
            if (unmodifiableMap) {
                t = Collections.unmodifiableMap(t);
            }
            return t;
        } finally {
            parser.setContext(context);
        }
    }

    /* access modifiers changed from: protected */
    public Object deserialze(DefaultJSONParser parser, Type type, Object fieldName, Map map) {
        Type valueType;
        if (!(type instanceof ParameterizedType)) {
            return parser.parseObject(map, fieldName);
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type keyType = parameterizedType.getActualTypeArguments()[0];
        if (map.getClass().getName().equals("org.springframework.util.LinkedMultiValueMap")) {
            valueType = List.class;
        } else {
            valueType = parameterizedType.getActualTypeArguments()[1];
        }
        if (String.class == keyType) {
            return parseMap(parser, map, valueType, fieldName);
        }
        return parseMap(parser, map, keyType, valueType, fieldName);
    }

    public static Map parseMap(DefaultJSONParser parser, Map<String, Object> map, Type valueType, Object fieldName) {
        String key;
        Object value;
        JSONLexer lexer = parser.lexer;
        int token = lexer.token();
        int i = 0;
        if (token != 12) {
            String msg = "syntax error, expect {, actual " + lexer.tokenName();
            if (fieldName instanceof String) {
                msg = (msg + ", fieldName ") + fieldName;
            }
            String msg2 = (msg + ", ") + lexer.info();
            if (token != 4) {
                JSONArray array = new JSONArray();
                parser.parseArray(array, fieldName);
                if (array.size() == 1) {
                    Object first = array.get(0);
                    if (first instanceof JSONObject) {
                        return (JSONObject) first;
                    }
                }
            }
            throw new JSONException(msg2);
        }
        ParseContext context = parser.getContext();
        while (true) {
            try {
                lexer.skipWhitespace();
                char ch = lexer.getCurrent();
                if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (ch == ',') {
                        lexer.next();
                        lexer.skipWhitespace();
                        ch = lexer.getCurrent();
                    }
                }
                if (ch == '\"') {
                    key = lexer.scanSymbol(parser.getSymbolTable(), '\"');
                    lexer.skipWhitespace();
                    if (lexer.getCurrent() != ':') {
                        throw new JSONException("expect ':' at " + lexer.pos());
                    }
                } else if (ch == '}') {
                    lexer.next();
                    lexer.resetStringPosition();
                    lexer.nextToken(16);
                    parser.setContext(context);
                    return map;
                } else if (ch == '\'') {
                    if (lexer.isEnabled(Feature.AllowSingleQuotes)) {
                        key = lexer.scanSymbol(parser.getSymbolTable(), '\'');
                        lexer.skipWhitespace();
                        if (lexer.getCurrent() != ':') {
                            throw new JSONException("expect ':' at " + lexer.pos());
                        }
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if (lexer.isEnabled(Feature.AllowUnQuotedFieldNames)) {
                    key = lexer.scanSymbolUnQuoted(parser.getSymbolTable());
                    lexer.skipWhitespace();
                    char ch2 = lexer.getCurrent();
                    if (ch2 != ':') {
                        throw new JSONException("expect ':' at " + lexer.pos() + ", actual " + ch2);
                    }
                } else {
                    throw new JSONException("syntax error");
                }
                lexer.next();
                lexer.skipWhitespace();
                lexer.getCurrent();
                lexer.resetStringPosition();
                if (key != JSON.DEFAULT_TYPE_KEY || lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    lexer.nextToken();
                    if (i != 0) {
                        parser.setContext(context);
                    }
                    if (lexer.token() == 8) {
                        value = null;
                        lexer.nextToken();
                    } else {
                        value = parser.parseObject(valueType, key);
                    }
                    map.put(key, value);
                    parser.checkMapResolve(map, key);
                    parser.setContext(context, value, key);
                    parser.setContext(context);
                    int tok = lexer.token();
                    if (tok == 20 || tok == 15) {
                        parser.setContext(context);
                    } else if (tok == 13) {
                        lexer.nextToken();
                        parser.setContext(context);
                        return map;
                    }
                } else {
                    String typeName = lexer.scanSymbol(parser.getSymbolTable(), '\"');
                    ParserConfig config = parser.getConfig();
                    Class<?> clazz = config.checkAutoType(typeName, null, lexer.getFeatures());
                    if (Map.class.isAssignableFrom(clazz)) {
                        lexer.nextToken(16);
                        if (lexer.token() == 13) {
                            lexer.nextToken(16);
                            return map;
                        }
                    } else {
                        ObjectDeserializer deserializer = config.getDeserializer(clazz);
                        lexer.nextToken(16);
                        parser.setResolveStatus(2);
                        if (context != null && !(fieldName instanceof Integer)) {
                            parser.popContext();
                        }
                        Map map2 = (Map) deserializer.deserialze(parser, clazz, fieldName);
                        parser.setContext(context);
                        return map2;
                    }
                }
                i++;
            } finally {
                parser.setContext(context);
            }
        }
        parser.setContext(context);
        return map;
    }

    public static Object parseMap(DefaultJSONParser parser, Map<Object, Object> map, Type keyType, Type valueType, Object fieldName) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 12 || lexer.token() == 16) {
            ObjectDeserializer keyDeserializer = parser.getConfig().getDeserializer(keyType);
            ObjectDeserializer valueDeserializer = parser.getConfig().getDeserializer(valueType);
            lexer.nextToken(keyDeserializer.getFastMatchToken());
            ParseContext context = parser.getContext();
            while (lexer.token() != 13) {
                try {
                    if (lexer.token() != 4 || !lexer.isRef() || lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                        if (map.size() == 0 && lexer.token() == 4 && JSON.DEFAULT_TYPE_KEY.equals(lexer.stringVal()) && !lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                            lexer.nextTokenWithColon(4);
                            lexer.nextToken(16);
                            if (lexer.token() == 13) {
                                lexer.nextToken();
                                return map;
                            }
                            lexer.nextToken(keyDeserializer.getFastMatchToken());
                        }
                        Object key = keyDeserializer.deserialze(parser, keyType, null);
                        if (lexer.token() == 17) {
                            lexer.nextToken(valueDeserializer.getFastMatchToken());
                            Object value = valueDeserializer.deserialze(parser, valueType, key);
                            parser.checkMapResolve(map, key);
                            map.put(key, value);
                            if (lexer.token() == 16) {
                                lexer.nextToken(keyDeserializer.getFastMatchToken());
                            }
                        } else {
                            throw new JSONException("syntax error, expect :, actual " + lexer.token());
                        }
                    } else {
                        Object object = null;
                        lexer.nextTokenWithColon(4);
                        if (lexer.token() == 4) {
                            String ref = lexer.stringVal();
                            if ("..".equals(ref)) {
                                object = context.parent.object;
                            } else if ("$".equals(ref)) {
                                ParseContext rootContext = context;
                                while (rootContext.parent != null) {
                                    rootContext = rootContext.parent;
                                }
                                object = rootContext.object;
                            } else {
                                parser.addResolveTask(new DefaultJSONParser.ResolveTask(context, ref));
                                parser.setResolveStatus(1);
                            }
                            lexer.nextToken(13);
                            if (lexer.token() == 13) {
                                lexer.nextToken(16);
                                parser.setContext(context);
                                return object;
                            }
                            throw new JSONException("illegal ref");
                        }
                        throw new JSONException("illegal ref, " + JSONToken.name(lexer.token()));
                    }
                } finally {
                    parser.setContext(context);
                }
            }
            lexer.nextToken(16);
            parser.setContext(context);
            return map;
        }
        throw new JSONException("syntax error, expect {, actual " + lexer.tokenName());
    }

    public Map<Object, Object> createMap(Type type) {
        return createMap(type, JSON.DEFAULT_GENERATE_FEATURE);
    }

    public Map<Object, Object> createMap(Type type, int featrues) {
        if (type == Properties.class) {
            return new Properties();
        }
        if (type == Hashtable.class) {
            return new Hashtable();
        }
        if (type == IdentityHashMap.class) {
            return new IdentityHashMap();
        }
        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap();
        }
        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap();
        }
        if (type == Map.class) {
            return (Feature.OrderedField.mask & featrues) != 0 ? new LinkedHashMap() : new HashMap();
        }
        if (type == HashMap.class) {
            return new HashMap();
        }
        if (type == LinkedHashMap.class) {
            return new LinkedHashMap();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (EnumMap.class.equals(rawType)) {
                return new EnumMap((Class) parameterizedType.getActualTypeArguments()[0]);
            }
            return createMap(rawType, featrues);
        }
        Class<?> clazz = (Class) type;
        if (clazz.isInterface()) {
            throw new JSONException("unsupport type " + type);
        } else if ("java.util.Collections$UnmodifiableMap".equals(clazz.getName())) {
            return new HashMap();
        } else {
            try {
                return (Map) clazz.newInstance();
            } catch (Exception e) {
                throw new JSONException("unsupport type " + type, e);
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 12;
    }
}
