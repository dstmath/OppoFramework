package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.JSONPathException;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessable;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.parser.deserializer.ExtraTypeProvider;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.FieldTypeResolver;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.MapDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.PropertyProcessable;
import com.alibaba.fastjson.parser.deserializer.ResolveFieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.ThrowableDeserializer;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.serializer.IntegerCodec;
import com.alibaba.fastjson.serializer.LongCodec;
import com.alibaba.fastjson.serializer.StringCodec;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.Closeable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DefaultJSONParser implements Closeable {
    public static final int NONE = 0;
    public static final int NeedToResolve = 1;
    public static final int TypeNameRedirect = 2;
    private static final Set<Class<?>> primitiveClasses = new HashSet();
    private String[] autoTypeAccept;
    private boolean autoTypeEnable;
    protected ParserConfig config;
    protected ParseContext context;
    private ParseContext[] contextArray;
    private int contextArrayIndex;
    private DateFormat dateFormat;
    private String dateFormatPattern;
    private List<ExtraProcessor> extraProcessors;
    private List<ExtraTypeProvider> extraTypeProviders;
    protected FieldTypeResolver fieldTypeResolver;
    public final Object input;
    protected transient BeanContext lastBeanContext;
    public final JSONLexer lexer;
    public int resolveStatus;
    private List<ResolveTask> resolveTaskList;
    public final SymbolTable symbolTable;

    static {
        for (Class<?> clazz : new Class[]{Boolean.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigInteger.class, BigDecimal.class, String.class}) {
            primitiveClasses.add(clazz);
        }
    }

    public String getDateFomartPattern() {
        return this.dateFormatPattern;
    }

    public DateFormat getDateFormat() {
        if (this.dateFormat == null) {
            this.dateFormat = new SimpleDateFormat(this.dateFormatPattern, this.lexer.getLocale());
            this.dateFormat.setTimeZone(this.lexer.getTimeZone());
        }
        return this.dateFormat;
    }

    public void setDateFormat(String dateFormat2) {
        this.dateFormatPattern = dateFormat2;
        this.dateFormat = null;
    }

    public void setDateFomrat(DateFormat dateFormat2) {
        this.dateFormat = dateFormat2;
    }

    public DefaultJSONParser(String input2) {
        this(input2, ParserConfig.getGlobalInstance(), JSON.DEFAULT_PARSER_FEATURE);
    }

    public DefaultJSONParser(String input2, ParserConfig config2) {
        this(input2, new JSONScanner(input2, JSON.DEFAULT_PARSER_FEATURE), config2);
    }

    public DefaultJSONParser(String input2, ParserConfig config2, int features) {
        this(input2, new JSONScanner(input2, features), config2);
    }

    public DefaultJSONParser(char[] input2, int length, ParserConfig config2, int features) {
        this(input2, new JSONScanner(input2, length, features), config2);
    }

    public DefaultJSONParser(JSONLexer lexer2) {
        this(lexer2, ParserConfig.getGlobalInstance());
    }

    public DefaultJSONParser(JSONLexer lexer2, ParserConfig config2) {
        this((Object) null, lexer2, config2);
    }

    public DefaultJSONParser(Object input2, JSONLexer lexer2, ParserConfig config2) {
        this.dateFormatPattern = JSON.DEFFAULT_DATE_FORMAT;
        this.contextArrayIndex = 0;
        this.resolveStatus = 0;
        this.extraTypeProviders = null;
        this.extraProcessors = null;
        this.fieldTypeResolver = null;
        this.autoTypeAccept = null;
        this.lexer = lexer2;
        this.input = input2;
        this.config = config2;
        this.symbolTable = config2.symbolTable;
        int ch = lexer2.getCurrent();
        if (ch == 123) {
            lexer2.next();
            ((JSONLexerBase) lexer2).token = 12;
        } else if (ch == 91) {
            lexer2.next();
            ((JSONLexerBase) lexer2).token = 14;
        } else {
            lexer2.nextToken();
        }
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public String getInput() {
        if (this.input instanceof char[]) {
            return new String((char[]) this.input);
        }
        return this.input.toString();
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:401:0x030d */
    /* JADX DEBUG: Multi-variable search result rejected for r3v11, resolved type: java.lang.Object */
    /* JADX DEBUG: Multi-variable search result rejected for r16v0, resolved type: java.lang.Object */
    /* JADX DEBUG: Multi-variable search result rejected for r5v11, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public final Object parseObject(Map object, Object fieldName) {
        ParseContext context2;
        Throwable th;
        String str;
        Object obj;
        char c;
        Number number;
        Number number2;
        Map input2;
        Map map;
        Object[] objArr;
        Object obj2;
        Class<?> clazz;
        Exception e;
        JavaBeanDeserializer javaBeanDeserializer;
        FieldDeserializer fieldDeserializer;
        Number number3;
        Object obj3 = fieldName;
        JSONLexer lexer2 = this.lexer;
        if (lexer2.token() == 8) {
            lexer2.nextToken();
            return null;
        } else if (lexer2.token() == 13) {
            lexer2.nextToken();
            return object;
        } else if (lexer2.token() == 4 && lexer2.stringVal().length() == 0) {
            lexer2.nextToken();
            return object;
        } else if (lexer2.token() == 12 || lexer2.token() == 16) {
            ParseContext context3 = this.context;
            try {
                Map<String, Object> map2 = object instanceof JSONObject ? ((JSONObject) object).getInnerMap() : object;
                context2 = context3;
                boolean setContextFlag = false;
                while (true) {
                    try {
                        lexer2.skipWhitespace();
                        char ch = lexer2.getCurrent();
                        if (lexer2.isEnabled(Feature.AllowArbitraryCommas)) {
                            while (ch == ',') {
                                lexer2.next();
                                lexer2.skipWhitespace();
                                ch = lexer2.getCurrent();
                            }
                        }
                        boolean isObjectKey = false;
                        char c2 = '0';
                        if (ch == '\"') {
                            str = lexer2.scanSymbol(this.symbolTable, '\"');
                            lexer2.skipWhitespace();
                            if (lexer2.getCurrent() != ':') {
                                throw new JSONException("expect ':' at " + lexer2.pos() + ", name " + ((Object) str));
                            }
                        } else if (ch == '}') {
                            lexer2.next();
                            lexer2.resetStringPosition();
                            lexer2.nextToken();
                            if (!setContextFlag) {
                                if (this.context != null && obj3 == this.context.fieldName && object == this.context.object) {
                                    context2 = this.context;
                                } else {
                                    ParseContext contextR = setContext(object, fieldName);
                                    if (context2 == null) {
                                        context2 = contextR;
                                    }
                                }
                            }
                            setContext(context2);
                            return object;
                        } else if (ch == '\'') {
                            if (lexer2.isEnabled(Feature.AllowSingleQuotes)) {
                                str = lexer2.scanSymbol(this.symbolTable, '\'');
                                lexer2.skipWhitespace();
                                if (lexer2.getCurrent() != ':') {
                                    throw new JSONException("expect ':' at " + lexer2.pos());
                                }
                            } else {
                                throw new JSONException("syntax error");
                            }
                        } else if (ch == 26) {
                            throw new JSONException("syntax error");
                        } else if (ch == ',') {
                            throw new JSONException("syntax error");
                        } else if ((ch >= '0' && ch <= '9') || ch == '-') {
                            lexer2.resetStringPosition();
                            lexer2.scanNumber();
                            try {
                                if (lexer2.token() == 2) {
                                    number3 = lexer2.integerValue();
                                } else {
                                    number3 = lexer2.decimalValue(true);
                                }
                                String str2 = number3;
                                String str3 = str2;
                                if (lexer2.isEnabled(Feature.NonStringKeyAsString)) {
                                    str3 = str2.toString();
                                }
                                str = str3;
                                if (lexer2.getCurrent() != ':') {
                                    throw new JSONException("parse number key error" + lexer2.info());
                                }
                            } catch (NumberFormatException e2) {
                                throw new JSONException("parse number key error" + lexer2.info());
                            }
                        } else if (ch == '{' || ch == '[') {
                            lexer2.nextToken();
                            str = parse();
                            isObjectKey = true;
                        } else if (lexer2.isEnabled(Feature.AllowUnQuotedFieldNames)) {
                            str = lexer2.scanSymbolUnQuoted(this.symbolTable);
                            lexer2.skipWhitespace();
                            char ch2 = lexer2.getCurrent();
                            if (ch2 != ':') {
                                throw new JSONException("expect ':' at " + lexer2.pos() + ", actual " + ch2);
                            }
                        } else {
                            throw new JSONException("syntax error");
                        }
                        if (!isObjectKey) {
                            lexer2.next();
                            lexer2.skipWhitespace();
                        }
                        char ch3 = lexer2.getCurrent();
                        lexer2.resetStringPosition();
                        if (str != JSON.DEFAULT_TYPE_KEY || lexer2.isEnabled(Feature.DisableSpecialKeyDetect)) {
                            obj = null;
                            if (str != "$ref" || context2 == null || lexer2.isEnabled(Feature.DisableSpecialKeyDetect)) {
                                boolean parentIsArray = true;
                                if (!setContextFlag) {
                                    if (this.context != null && obj3 == this.context.fieldName && object == this.context.object) {
                                        context2 = this.context;
                                    } else {
                                        ParseContext contextR2 = setContext(object, fieldName);
                                        if (context2 == null) {
                                            context2 = contextR2;
                                        }
                                        setContextFlag = true;
                                    }
                                }
                                if (object.getClass() == JSONObject.class && str == null) {
                                    str = "null";
                                }
                                if (ch3 == '\"') {
                                    lexer2.scanString();
                                    String strValue = lexer2.stringVal();
                                    number = strValue;
                                    if (lexer2.isEnabled(Feature.AllowISO8601DateFormat)) {
                                        JSONScanner iso8601Lexer = new JSONScanner(strValue);
                                        if (iso8601Lexer.scanISO8601DateIfMatch()) {
                                            number = iso8601Lexer.getCalendar().getTime();
                                        }
                                        iso8601Lexer.close();
                                    }
                                    map2.put(str, number);
                                } else if ((ch3 < '0' || ch3 > '9') && ch3 != '-') {
                                    if (ch3 == '[') {
                                        lexer2.nextToken();
                                        JSONArray list = new JSONArray();
                                        if (obj3 == null || fieldName.getClass() != Integer.class) {
                                            parentIsArray = false;
                                        }
                                        if (obj3 == null) {
                                            setContext(context2);
                                        }
                                        parseArray(list, str);
                                        if (lexer2.isEnabled(Feature.UseObjectArray)) {
                                            objArr = list.toArray();
                                        } else {
                                            objArr = list;
                                        }
                                        map2.put(str, objArr);
                                        if (lexer2.token() == 13) {
                                            lexer2.nextToken();
                                            setContext(context2);
                                            return object;
                                        } else if (lexer2.token() != 16) {
                                            throw new JSONException("syntax error");
                                        }
                                    } else if (ch3 == '{') {
                                        lexer2.nextToken();
                                        if (obj3 == null || fieldName.getClass() != Integer.class) {
                                            parentIsArray = false;
                                        }
                                        if (lexer2.isEnabled(Feature.CustomMapDeserializer)) {
                                            MapDeserializer mapDeserializer = (MapDeserializer) this.config.getDeserializer(Map.class);
                                            if ((lexer2.getFeatures() & Feature.OrderedField.mask) != 0) {
                                                map = mapDeserializer.createMap(Map.class, lexer2.getFeatures());
                                            } else {
                                                map = mapDeserializer.createMap(Map.class);
                                            }
                                            input2 = map;
                                        } else {
                                            input2 = new JSONObject(lexer2.isEnabled(Feature.OrderedField));
                                        }
                                        ParseContext ctxLocal = null;
                                        if (!parentIsArray) {
                                            ctxLocal = setContext(context2, input2, str);
                                        }
                                        Map map3 = null;
                                        boolean objParsed = false;
                                        if (this.fieldTypeResolver != null) {
                                            Type fieldType = this.fieldTypeResolver.resolve(object, str != null ? str.toString() : null);
                                            if (fieldType != null) {
                                                map3 = this.config.getDeserializer(fieldType).deserialze(this, fieldType, str);
                                                objParsed = true;
                                            }
                                        }
                                        if (!objParsed) {
                                            map3 = parseObject(input2, str);
                                        }
                                        if (!(ctxLocal == null || input2 == map3)) {
                                            ctxLocal.object = object;
                                        }
                                        if (str != null) {
                                            checkMapResolve(object, str.toString());
                                        }
                                        map2.put(str, map3);
                                        if (parentIsArray) {
                                            setContext(map3, str);
                                        }
                                        if (lexer2.token() == 13) {
                                            lexer2.nextToken();
                                            setContext(context2);
                                            setContext(context2);
                                            return object;
                                        } else if (lexer2.token() != 16) {
                                            throw new JSONException("syntax error, " + lexer2.tokenName());
                                        } else if (parentIsArray) {
                                            popContext();
                                        } else {
                                            setContext(context2);
                                        }
                                    } else {
                                        lexer2.nextToken();
                                        map2.put(str, parse());
                                        if (lexer2.token() == 13) {
                                            lexer2.nextToken();
                                            setContext(context2);
                                            return object;
                                        }
                                        c = 16;
                                        if (lexer2.token() != 16) {
                                            throw new JSONException("syntax error, position at " + lexer2.pos() + ", name " + ((Object) str));
                                        }
                                        obj3 = fieldName;
                                    }
                                    c = 16;
                                    obj3 = fieldName;
                                } else {
                                    lexer2.scanNumber();
                                    if (lexer2.token() == 2) {
                                        number2 = lexer2.integerValue();
                                    } else {
                                        number2 = lexer2.decimalValue(lexer2.isEnabled(Feature.UseBigDecimal));
                                    }
                                    number = number2;
                                    map2.put(str, number);
                                }
                                lexer2.skipWhitespace();
                                char ch4 = lexer2.getCurrent();
                                if (ch4 == ',') {
                                    lexer2.next();
                                    c = 16;
                                    obj3 = fieldName;
                                } else if (ch4 == '}') {
                                    lexer2.next();
                                    lexer2.resetStringPosition();
                                    lexer2.nextToken();
                                    setContext(number, str);
                                    setContext(context2);
                                    return object;
                                } else {
                                    throw new JSONException("syntax error, position at " + lexer2.pos() + ", name " + ((Object) str));
                                }
                            } else {
                                lexer2.nextToken(4);
                                if (lexer2.token() == 4) {
                                    String ref = lexer2.stringVal();
                                    lexer2.nextToken(13);
                                    if (lexer2.token() == 16) {
                                        map2.put(str, ref);
                                    } else {
                                        Object refValue = null;
                                        if ("@".equals(ref)) {
                                            if (this.context != null) {
                                                ParseContext thisContext = this.context;
                                                Object thisObj = thisContext.object;
                                                if ((thisObj instanceof Object[]) || (thisObj instanceof Collection)) {
                                                    refValue = thisObj;
                                                } else if (thisContext.parent != null) {
                                                    refValue = thisContext.parent.object;
                                                }
                                            }
                                        } else if ("..".equals(ref)) {
                                            if (context2.object != null) {
                                                refValue = context2.object;
                                            } else {
                                                addResolveTask(new ResolveTask(context2, ref));
                                                setResolveStatus(1);
                                            }
                                        } else if ("$".equals(ref)) {
                                            ParseContext rootContext = context2;
                                            while (rootContext.parent != null) {
                                                rootContext = rootContext.parent;
                                            }
                                            if (rootContext.object != null) {
                                                refValue = rootContext.object;
                                            } else {
                                                addResolveTask(new ResolveTask(rootContext, ref));
                                                setResolveStatus(1);
                                            }
                                        } else {
                                            addResolveTask(new ResolveTask(context2, ref));
                                            setResolveStatus(1);
                                        }
                                        if (lexer2.token() == 13) {
                                            lexer2.nextToken(16);
                                            setContext(context2);
                                            return refValue;
                                        }
                                        throw new JSONException("syntax error, " + lexer2.info());
                                    }
                                } else {
                                    throw new JSONException("illegal ref, " + JSONToken.name(lexer2.token()));
                                }
                            }
                        } else {
                            String typeName = lexer2.scanSymbol(this.symbolTable, '\"');
                            if (lexer2.isEnabled(Feature.IgnoreAutoType)) {
                                obj = null;
                            } else {
                                if (object == null || !object.getClass().getName().equals(typeName)) {
                                    boolean allDigits = true;
                                    int i = 0;
                                    while (true) {
                                        if (i >= typeName.length()) {
                                            break;
                                        }
                                        char c3 = typeName.charAt(i);
                                        if (c3 < c2 || c3 > '9') {
                                            allDigits = false;
                                        } else {
                                            i++;
                                            c2 = '0';
                                        }
                                    }
                                    if (!allDigits) {
                                        obj2 = null;
                                        clazz = this.config.checkAutoType(typeName, null, lexer2.getFeatures());
                                    } else {
                                        obj2 = null;
                                        clazz = null;
                                    }
                                } else {
                                    clazz = object.getClass();
                                    obj2 = null;
                                }
                                if (clazz == null) {
                                    map2.put(JSON.DEFAULT_TYPE_KEY, typeName);
                                    obj = obj2;
                                } else {
                                    lexer2.nextToken(16);
                                    if (lexer2.token() == 13) {
                                        lexer2.nextToken(16);
                                        Object instance = null;
                                        try {
                                            ObjectDeserializer deserializer = this.config.getDeserializer(clazz);
                                            if (deserializer instanceof JavaBeanDeserializer) {
                                                JavaBeanDeserializer javaBeanDeserializer2 = (JavaBeanDeserializer) deserializer;
                                                instance = javaBeanDeserializer2.createInstance(this, clazz);
                                                Iterator it = map2.entrySet().iterator();
                                                while (it.hasNext()) {
                                                    Map.Entry entry = (Map.Entry) it.next();
                                                    Object entryKey = entry.getKey();
                                                    try {
                                                        if (!(entryKey instanceof String) || (fieldDeserializer = javaBeanDeserializer2.getFieldDeserializer((String) entryKey)) == null) {
                                                            javaBeanDeserializer = javaBeanDeserializer2;
                                                        } else {
                                                            javaBeanDeserializer = javaBeanDeserializer2;
                                                            fieldDeserializer.setValue(instance, entry.getValue());
                                                        }
                                                        deserializer = deserializer;
                                                        isObjectKey = isObjectKey;
                                                        it = it;
                                                        javaBeanDeserializer2 = javaBeanDeserializer;
                                                    } catch (Exception e3) {
                                                        e = e3;
                                                        throw new JSONException("create instance error", e);
                                                    }
                                                }
                                            }
                                            Object instance2 = instance;
                                            if (instance == null) {
                                                if (clazz == Cloneable.class) {
                                                    instance2 = new HashMap();
                                                } else if ("java.util.Collections$EmptyMap".equals(typeName)) {
                                                    instance2 = Collections.emptyMap();
                                                } else if ("java.util.Collections$UnmodifiableMap".equals(typeName)) {
                                                    instance2 = Collections.unmodifiableMap(new HashMap());
                                                } else {
                                                    instance2 = clazz.newInstance();
                                                }
                                            }
                                            setContext(context2);
                                            return instance2;
                                        } catch (Exception e4) {
                                            e = e4;
                                            throw new JSONException("create instance error", e);
                                        }
                                    } else {
                                        setResolveStatus(2);
                                        if (this.context != null && obj3 != null && !(obj3 instanceof Integer) && !(this.context.fieldName instanceof Integer)) {
                                            popContext();
                                        }
                                        if (object.size() > 0) {
                                            Object newObj = TypeUtils.cast((Object) object, (Class<Object>) clazz, this.config);
                                            parseObject(newObj);
                                            setContext(context2);
                                            return newObj;
                                        }
                                        ObjectDeserializer deserializer2 = this.config.getDeserializer(clazz);
                                        Class deserClass = deserializer2.getClass();
                                        if (JavaBeanDeserializer.class.isAssignableFrom(deserClass) && deserClass != JavaBeanDeserializer.class && deserClass != ThrowableDeserializer.class) {
                                            setResolveStatus(0);
                                        } else if (deserializer2 instanceof MapDeserializer) {
                                            setResolveStatus(0);
                                        }
                                        Object obj4 = deserializer2.deserialze(this, clazz, obj3);
                                        setContext(context2);
                                        return obj4;
                                    }
                                }
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        setContext(context2);
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                context2 = context3;
                setContext(context2);
                throw th;
            }
        } else {
            throw new JSONException("syntax error, expect {, actual " + lexer2.tokenName() + ", " + lexer2.info());
        }
    }

    public ParserConfig getConfig() {
        return this.config;
    }

    public void setConfig(ParserConfig config2) {
        this.config = config2;
    }

    public <T> T parseObject(Class<T> clazz) {
        return (T) parseObject(clazz, (Object) null);
    }

    public <T> T parseObject(Type type) {
        return (T) parseObject(type, (Object) null);
    }

    public <T> T parseObject(Type type, Object fieldName) {
        int token = this.lexer.token();
        if (token == 8) {
            this.lexer.nextToken();
            return null;
        }
        if (token == 4) {
            if (type == byte[].class) {
                T t = (T) this.lexer.bytesValue();
                this.lexer.nextToken();
                return t;
            } else if (type == char[].class) {
                String strVal = this.lexer.stringVal();
                this.lexer.nextToken();
                return (T) strVal.toCharArray();
            }
        }
        ObjectDeserializer derializer = this.config.getDeserializer(type);
        try {
            return derializer.getClass() == JavaBeanDeserializer.class ? (T) ((JavaBeanDeserializer) derializer).deserialze(this, type, fieldName, 0) : (T) derializer.deserialze(this, type, fieldName);
        } catch (JSONException e) {
            throw e;
        } catch (Throwable e2) {
            throw new JSONException(e2.getMessage(), e2);
        }
    }

    public <T> List<T> parseArray(Class<T> clazz) {
        List<T> array = new ArrayList<>();
        parseArray((Class<?>) clazz, (Collection) array);
        return array;
    }

    public void parseArray(Class<?> clazz, Collection array) {
        parseArray((Type) clazz, array);
    }

    public void parseArray(Type type, Collection array) {
        parseArray(type, array, null);
    }

    /* JADX INFO: finally extract failed */
    public void parseArray(Type type, Collection array, Object fieldName) {
        ObjectDeserializer deserializer;
        Object val;
        String value;
        int token = this.lexer.token();
        if (token == 21 || token == 22) {
            this.lexer.nextToken();
            token = this.lexer.token();
        }
        if (token == 14) {
            if (Integer.TYPE == type) {
                deserializer = IntegerCodec.instance;
                this.lexer.nextToken(2);
            } else if (String.class == type) {
                deserializer = StringCodec.instance;
                this.lexer.nextToken(4);
            } else {
                deserializer = this.config.getDeserializer(type);
                this.lexer.nextToken(deserializer.getFastMatchToken());
            }
            ParseContext context2 = this.context;
            setContext(array, fieldName);
            int i = 0;
            while (true) {
                try {
                    if (this.lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                        while (this.lexer.token() == 16) {
                            this.lexer.nextToken();
                        }
                    }
                    if (this.lexer.token() == 15) {
                        setContext(context2);
                        this.lexer.nextToken(16);
                        return;
                    }
                    if (Integer.TYPE == type) {
                        array.add(IntegerCodec.instance.deserialze(this, null, null));
                    } else if (String.class == type) {
                        if (this.lexer.token() == 4) {
                            String value2 = this.lexer.stringVal();
                            this.lexer.nextToken(16);
                            value = value2;
                        } else {
                            Object obj = parse();
                            value = obj == null ? null : obj.toString();
                        }
                        array.add(value);
                    } else {
                        if (this.lexer.token() == 8) {
                            this.lexer.nextToken();
                            val = null;
                        } else {
                            val = deserializer.deserialze(this, type, Integer.valueOf(i));
                        }
                        array.add(val);
                        checkListResolve(array);
                    }
                    if (this.lexer.token() == 16) {
                        this.lexer.nextToken(deserializer.getFastMatchToken());
                    }
                    i++;
                } catch (Throwable th) {
                    setContext(context2);
                    throw th;
                }
            }
        } else {
            throw new JSONException("expect '[', but " + JSONToken.name(token) + ", " + this.lexer.info());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x0152  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0192 A[EDGE_INSN: B:74:0x0192->B:66:0x0192 ?: BREAK  , SYNTHETIC] */
    public Object[] parseArray(Type[] types) {
        Object value;
        Object value2;
        Object value3;
        Object obj = null;
        int i = 8;
        if (this.lexer.token() == 8) {
            this.lexer.nextToken(16);
            return null;
        } else if (this.lexer.token() == 14) {
            Object[] list = new Object[types.length];
            int i2 = 0;
            if (types.length == 0) {
                this.lexer.nextToken(15);
                if (this.lexer.token() == 15) {
                    this.lexer.nextToken(16);
                    return new Object[0];
                }
                throw new JSONException("syntax error");
            }
            this.lexer.nextToken(2);
            while (i2 < types.length) {
                if (this.lexer.token() == i) {
                    value2 = null;
                    this.lexer.nextToken(16);
                } else {
                    Type type = types[i2];
                    if (type == Integer.TYPE || type == Integer.class) {
                        if (this.lexer.token() == 2) {
                            value = Integer.valueOf(this.lexer.intValue());
                            this.lexer.nextToken(16);
                        } else {
                            value = TypeUtils.cast(parse(), type, this.config);
                        }
                        list[i2] = value;
                        if (this.lexer.token() == 15) {
                            break;
                        } else if (this.lexer.token() == 16) {
                            if (i2 == types.length - 1) {
                                this.lexer.nextToken(15);
                            } else {
                                this.lexer.nextToken(2);
                            }
                            i2++;
                            obj = null;
                            i = 8;
                        } else {
                            throw new JSONException("syntax error :" + JSONToken.name(this.lexer.token()));
                        }
                    } else {
                        if (type == String.class) {
                            if (this.lexer.token() == 4) {
                                value3 = this.lexer.stringVal();
                                this.lexer.nextToken(16);
                            } else {
                                value3 = TypeUtils.cast(parse(), type, this.config);
                            }
                            value = value3;
                        } else {
                            boolean isArray = false;
                            Class<?> componentType = null;
                            if (i2 == types.length - 1 && (type instanceof Class)) {
                                Class<?> clazz = (Class) type;
                                isArray = clazz.isArray();
                                componentType = clazz.getComponentType();
                            }
                            if (!isArray || this.lexer.token() == 14) {
                                value2 = this.config.getDeserializer(type).deserialze(this, type, Integer.valueOf(i2));
                            } else {
                                List<Object> varList = new ArrayList<>();
                                ObjectDeserializer derializer = this.config.getDeserializer(componentType);
                                int fastMatch = derializer.getFastMatchToken();
                                if (this.lexer.token() != 15) {
                                    while (true) {
                                        varList.add(derializer.deserialze(this, type, obj));
                                        if (this.lexer.token() != 16) {
                                            break;
                                        }
                                        this.lexer.nextToken(fastMatch);
                                        obj = null;
                                    }
                                    if (this.lexer.token() != 15) {
                                        throw new JSONException("syntax error :" + JSONToken.name(this.lexer.token()));
                                    }
                                }
                                value = TypeUtils.cast(varList, type, this.config);
                            }
                        }
                        list[i2] = value;
                        if (this.lexer.token() == 15) {
                        }
                    }
                }
                value = value2;
                list[i2] = value;
                if (this.lexer.token() == 15) {
                }
            }
            if (this.lexer.token() == 15) {
                this.lexer.nextToken(16);
                return list;
            }
            throw new JSONException("syntax error");
        } else {
            throw new JSONException("syntax error : " + this.lexer.tokenName());
        }
    }

    public void parseObject(Object object) {
        Object fieldValue;
        Class<?> clazz = object.getClass();
        JavaBeanDeserializer beanDeser = null;
        ObjectDeserializer deserizer = this.config.getDeserializer(clazz);
        if (deserizer instanceof JavaBeanDeserializer) {
            beanDeser = (JavaBeanDeserializer) deserizer;
        }
        if (this.lexer.token() == 12 || this.lexer.token() == 16) {
            while (true) {
                String key = this.lexer.scanSymbol(this.symbolTable);
                if (key == null) {
                    if (this.lexer.token() == 13) {
                        this.lexer.nextToken(16);
                        return;
                    } else if (this.lexer.token() == 16 && this.lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    }
                }
                FieldDeserializer fieldDeser = null;
                if (beanDeser != null) {
                    fieldDeser = beanDeser.getFieldDeserializer(key);
                }
                if (fieldDeser != null) {
                    Class<?> fieldClass = fieldDeser.fieldInfo.fieldClass;
                    Type fieldType = fieldDeser.fieldInfo.fieldType;
                    if (fieldClass == Integer.TYPE) {
                        this.lexer.nextTokenWithColon(2);
                        fieldValue = IntegerCodec.instance.deserialze(this, fieldType, null);
                    } else if (fieldClass == String.class) {
                        this.lexer.nextTokenWithColon(4);
                        fieldValue = StringCodec.deserialze(this);
                    } else if (fieldClass == Long.TYPE) {
                        this.lexer.nextTokenWithColon(2);
                        fieldValue = LongCodec.instance.deserialze(this, fieldType, null);
                    } else {
                        ObjectDeserializer fieldValueDeserializer = this.config.getDeserializer(fieldClass, fieldType);
                        this.lexer.nextTokenWithColon(fieldValueDeserializer.getFastMatchToken());
                        fieldValue = fieldValueDeserializer.deserialze(this, fieldType, null);
                    }
                    fieldDeser.setValue(object, fieldValue);
                    if (this.lexer.token() != 16 && this.lexer.token() == 13) {
                        this.lexer.nextToken(16);
                        return;
                    }
                } else if (this.lexer.isEnabled(Feature.IgnoreNotMatch)) {
                    this.lexer.nextTokenWithColon();
                    parse();
                    if (this.lexer.token() == 13) {
                        this.lexer.nextToken();
                        return;
                    }
                } else {
                    throw new JSONException("setter not found, class " + clazz.getName() + ", property " + key);
                }
            }
        } else {
            throw new JSONException("syntax error, expect {, actual " + this.lexer.tokenName());
        }
    }

    public Object parseArrayWithType(Type collectionType) {
        if (this.lexer.token() == 8) {
            this.lexer.nextToken();
            return null;
        }
        Type[] actualTypes = ((ParameterizedType) collectionType).getActualTypeArguments();
        if (actualTypes.length == 1) {
            Type actualTypeArgument = actualTypes[0];
            if (actualTypeArgument instanceof Class) {
                List<Object> array = new ArrayList<>();
                parseArray((Class) actualTypeArgument, (Collection) array);
                return array;
            } else if (actualTypeArgument instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) actualTypeArgument;
                Type upperBoundType = wildcardType.getUpperBounds()[0];
                if (!Object.class.equals(upperBoundType)) {
                    List<Object> array2 = new ArrayList<>();
                    parseArray((Class) upperBoundType, (Collection) array2);
                    return array2;
                } else if (wildcardType.getLowerBounds().length == 0) {
                    return parse();
                } else {
                    throw new JSONException("not support type : " + collectionType);
                }
            } else {
                if (actualTypeArgument instanceof TypeVariable) {
                    TypeVariable<?> typeVariable = (TypeVariable) actualTypeArgument;
                    Type[] bounds = typeVariable.getBounds();
                    if (bounds.length == 1) {
                        Type boundType = bounds[0];
                        if (boundType instanceof Class) {
                            List<Object> array3 = new ArrayList<>();
                            parseArray((Class) boundType, (Collection) array3);
                            return array3;
                        }
                    } else {
                        throw new JSONException("not support : " + typeVariable);
                    }
                }
                if (actualTypeArgument instanceof ParameterizedType) {
                    List<Object> array4 = new ArrayList<>();
                    parseArray((ParameterizedType) actualTypeArgument, array4);
                    return array4;
                }
                throw new JSONException("TODO : " + collectionType);
            }
        } else {
            throw new JSONException("not support type " + collectionType);
        }
    }

    public void acceptType(String typeName) {
        JSONLexer lexer2 = this.lexer;
        lexer2.nextTokenWithColon();
        if (lexer2.token() != 4) {
            throw new JSONException("type not match error");
        } else if (typeName.equals(lexer2.stringVal())) {
            lexer2.nextToken();
            if (lexer2.token() == 16) {
                lexer2.nextToken();
            }
        } else {
            throw new JSONException("type not match error");
        }
    }

    public int getResolveStatus() {
        return this.resolveStatus;
    }

    public void setResolveStatus(int resolveStatus2) {
        this.resolveStatus = resolveStatus2;
    }

    public Object getObject(String path) {
        for (int i = 0; i < this.contextArrayIndex; i++) {
            if (path.equals(this.contextArray[i].toString())) {
                return this.contextArray[i].object;
            }
        }
        return null;
    }

    public void checkListResolve(Collection array) {
        if (this.resolveStatus != 1) {
            return;
        }
        if (array instanceof List) {
            ResolveTask task = getLastResolveTask();
            task.fieldDeserializer = new ResolveFieldDeserializer(this, (List) array, array.size() - 1);
            task.ownerContext = this.context;
            setResolveStatus(0);
            return;
        }
        ResolveTask task2 = getLastResolveTask();
        task2.fieldDeserializer = new ResolveFieldDeserializer(array);
        task2.ownerContext = this.context;
        setResolveStatus(0);
    }

    public void checkMapResolve(Map object, Object fieldName) {
        if (this.resolveStatus == 1) {
            ResolveFieldDeserializer fieldResolver = new ResolveFieldDeserializer(object, fieldName);
            ResolveTask task = getLastResolveTask();
            task.fieldDeserializer = fieldResolver;
            task.ownerContext = this.context;
            setResolveStatus(0);
        }
    }

    public Object parseObject(Map object) {
        return parseObject(object, (Object) null);
    }

    public JSONObject parseObject() {
        Object parsedObject = parseObject((Map) new JSONObject(this.lexer.isEnabled(Feature.OrderedField)));
        if (parsedObject instanceof JSONObject) {
            return (JSONObject) parsedObject;
        }
        if (parsedObject == null) {
            return null;
        }
        return new JSONObject((Map) parsedObject);
    }

    public final void parseArray(Collection array) {
        parseArray(array, (Object) null);
    }

    public final void parseArray(Collection array, Object fieldName) {
        Object value;
        Object value2;
        Object value3;
        Object value4;
        JSONLexer lexer2 = this.lexer;
        if (lexer2.token() == 21 || lexer2.token() == 22) {
            lexer2.nextToken();
        }
        if (lexer2.token() == 14) {
            lexer2.nextToken(4);
            ParseContext context2 = this.context;
            setContext(array, fieldName);
            int i = 0;
            while (true) {
                try {
                    if (lexer2.isEnabled(Feature.AllowArbitraryCommas)) {
                        while (lexer2.token() == 16) {
                            lexer2.nextToken();
                        }
                    }
                    switch (lexer2.token()) {
                        case 2:
                            Object value5 = lexer2.integerValue();
                            lexer2.nextToken(16);
                            value = value5;
                            break;
                        case 3:
                            if (lexer2.isEnabled(Feature.UseBigDecimal)) {
                                value2 = lexer2.decimalValue(true);
                            } else {
                                value2 = lexer2.decimalValue(false);
                            }
                            lexer2.nextToken(16);
                            value = value2;
                            break;
                        case 4:
                            String stringLiteral = lexer2.stringVal();
                            lexer2.nextToken(16);
                            if (!lexer2.isEnabled(Feature.AllowISO8601DateFormat)) {
                                value = stringLiteral;
                                break;
                            } else {
                                JSONScanner iso8601Lexer = new JSONScanner(stringLiteral);
                                if (iso8601Lexer.scanISO8601DateIfMatch()) {
                                    value3 = iso8601Lexer.getCalendar().getTime();
                                } else {
                                    value3 = stringLiteral;
                                }
                                iso8601Lexer.close();
                                value = value3;
                                break;
                            }
                        case JSONToken.TRUE /* 6 */:
                            Object value6 = Boolean.TRUE;
                            lexer2.nextToken(16);
                            value = value6;
                            break;
                        case JSONToken.FALSE /* 7 */:
                            Object value7 = Boolean.FALSE;
                            lexer2.nextToken(16);
                            value = value7;
                            break;
                        case JSONToken.NULL /* 8 */:
                            value = null;
                            lexer2.nextToken(4);
                            break;
                        case JSONToken.LBRACE /* 12 */:
                            value4 = parseObject(new JSONObject(lexer2.isEnabled(Feature.OrderedField)), Integer.valueOf(i));
                            value = value4;
                            break;
                        case 14:
                            Collection items = new JSONArray();
                            parseArray(items, Integer.valueOf(i));
                            if (!lexer2.isEnabled(Feature.UseObjectArray)) {
                                value = items;
                                break;
                            } else {
                                value4 = items.toArray();
                                value = value4;
                                break;
                            }
                        case JSONToken.RBRACKET /* 15 */:
                            lexer2.nextToken(16);
                            return;
                        case JSONToken.EOF /* 20 */:
                            throw new JSONException("unclosed jsonArray");
                        case 23:
                            value = null;
                            lexer2.nextToken(4);
                            break;
                        default:
                            value = parse();
                            break;
                    }
                    array.add(value == 1 ? 1 : 0);
                    checkListResolve(array);
                    if (lexer2.token() == 16) {
                        lexer2.nextToken(4);
                    }
                    i++;
                } finally {
                    setContext(context2);
                }
            }
        } else {
            throw new JSONException("syntax error, expect [, actual " + JSONToken.name(lexer2.token()) + ", pos " + lexer2.pos() + ", fieldName " + fieldName);
        }
    }

    public ParseContext getContext() {
        return this.context;
    }

    public List<ResolveTask> getResolveTaskList() {
        if (this.resolveTaskList == null) {
            this.resolveTaskList = new ArrayList(2);
        }
        return this.resolveTaskList;
    }

    public void addResolveTask(ResolveTask task) {
        if (this.resolveTaskList == null) {
            this.resolveTaskList = new ArrayList(2);
        }
        this.resolveTaskList.add(task);
    }

    public ResolveTask getLastResolveTask() {
        return this.resolveTaskList.get(this.resolveTaskList.size() - 1);
    }

    public List<ExtraProcessor> getExtraProcessors() {
        if (this.extraProcessors == null) {
            this.extraProcessors = new ArrayList(2);
        }
        return this.extraProcessors;
    }

    public List<ExtraTypeProvider> getExtraTypeProviders() {
        if (this.extraTypeProviders == null) {
            this.extraTypeProviders = new ArrayList(2);
        }
        return this.extraTypeProviders;
    }

    public FieldTypeResolver getFieldTypeResolver() {
        return this.fieldTypeResolver;
    }

    public void setFieldTypeResolver(FieldTypeResolver fieldTypeResolver2) {
        this.fieldTypeResolver = fieldTypeResolver2;
    }

    public void setContext(ParseContext context2) {
        if (!this.lexer.isEnabled(Feature.DisableCircularReferenceDetect)) {
            this.context = context2;
        }
    }

    public void popContext() {
        if (!this.lexer.isEnabled(Feature.DisableCircularReferenceDetect)) {
            this.context = this.context.parent;
            if (this.contextArrayIndex > 0) {
                this.contextArrayIndex--;
                this.contextArray[this.contextArrayIndex] = null;
            }
        }
    }

    public ParseContext setContext(Object object, Object fieldName) {
        if (this.lexer.isEnabled(Feature.DisableCircularReferenceDetect)) {
            return null;
        }
        return setContext(this.context, object, fieldName);
    }

    public ParseContext setContext(ParseContext parent, Object object, Object fieldName) {
        if (this.lexer.isEnabled(Feature.DisableCircularReferenceDetect)) {
            return null;
        }
        this.context = new ParseContext(parent, object, fieldName);
        addContext(this.context);
        return this.context;
    }

    private void addContext(ParseContext context2) {
        int i = this.contextArrayIndex;
        this.contextArrayIndex = i + 1;
        if (this.contextArray == null) {
            this.contextArray = new ParseContext[8];
        } else if (i >= this.contextArray.length) {
            ParseContext[] newArray = new ParseContext[((this.contextArray.length * 3) / 2)];
            System.arraycopy(this.contextArray, 0, newArray, 0, this.contextArray.length);
            this.contextArray = newArray;
        }
        this.contextArray[i] = context2;
    }

    public Object parse() {
        return parse(null);
    }

    public Object parseKey() {
        if (this.lexer.token() != 18) {
            return parse(null);
        }
        String value = this.lexer.stringVal();
        this.lexer.nextToken(16);
        return value;
    }

    public Object parse(Object fieldName) {
        JSONLexer lexer2 = this.lexer;
        switch (lexer2.token()) {
            case 2:
                Number intValue = lexer2.integerValue();
                lexer2.nextToken();
                return intValue;
            case 3:
                Object value = lexer2.decimalValue(lexer2.isEnabled(Feature.UseBigDecimal));
                lexer2.nextToken();
                return value;
            case 4:
                String stringLiteral = lexer2.stringVal();
                lexer2.nextToken(16);
                if (lexer2.isEnabled(Feature.AllowISO8601DateFormat)) {
                    JSONScanner iso8601Lexer = new JSONScanner(stringLiteral);
                    try {
                        if (iso8601Lexer.scanISO8601DateIfMatch()) {
                            return iso8601Lexer.getCalendar().getTime();
                        }
                        iso8601Lexer.close();
                    } finally {
                        iso8601Lexer.close();
                    }
                }
                return stringLiteral;
            case 5:
            case 10:
            case 11:
            case JSONToken.RBRACE /* 13 */:
            case JSONToken.RBRACKET /* 15 */:
            case 16:
            case 17:
            case JSONToken.FIELD_NAME /* 19 */:
            case 24:
            case 25:
            default:
                throw new JSONException("syntax error, " + lexer2.info());
            case JSONToken.TRUE /* 6 */:
                lexer2.nextToken();
                return Boolean.TRUE;
            case JSONToken.FALSE /* 7 */:
                lexer2.nextToken();
                return Boolean.FALSE;
            case JSONToken.NULL /* 8 */:
                lexer2.nextToken();
                return null;
            case 9:
                lexer2.nextToken(18);
                if (lexer2.token() == 18) {
                    lexer2.nextToken(10);
                    accept(10);
                    long time = lexer2.integerValue().longValue();
                    accept(2);
                    accept(11);
                    return new Date(time);
                }
                throw new JSONException("syntax error");
            case JSONToken.LBRACE /* 12 */:
                return parseObject(new JSONObject(lexer2.isEnabled(Feature.OrderedField)), fieldName);
            case 14:
                JSONArray array = new JSONArray();
                parseArray(array, fieldName);
                if (lexer2.isEnabled(Feature.UseObjectArray)) {
                    return array.toArray();
                }
                return array;
            case JSONToken.IDENTIFIER /* 18 */:
                if ("NaN".equals(lexer2.stringVal())) {
                    lexer2.nextToken();
                    return null;
                }
                throw new JSONException("syntax error, " + lexer2.info());
            case JSONToken.EOF /* 20 */:
                if (lexer2.isBlankInput()) {
                    return null;
                }
                throw new JSONException("unterminated json string, " + lexer2.info());
            case 21:
                lexer2.nextToken();
                HashSet<Object> set = new HashSet<>();
                parseArray(set, fieldName);
                return set;
            case 22:
                lexer2.nextToken();
                TreeSet<Object> treeSet = new TreeSet<>();
                parseArray(treeSet, fieldName);
                return treeSet;
            case 23:
                lexer2.nextToken();
                return null;
            case JSONToken.HEX /* 26 */:
                byte[] bytes = lexer2.bytesValue();
                lexer2.nextToken();
                return bytes;
        }
    }

    public void config(Feature feature, boolean state) {
        this.lexer.config(feature, state);
    }

    public boolean isEnabled(Feature feature) {
        return this.lexer.isEnabled(feature);
    }

    public JSONLexer getLexer() {
        return this.lexer;
    }

    public final void accept(int token) {
        JSONLexer lexer2 = this.lexer;
        if (lexer2.token() == token) {
            lexer2.nextToken();
            return;
        }
        throw new JSONException("syntax error, expect " + JSONToken.name(token) + ", actual " + JSONToken.name(lexer2.token()));
    }

    public final void accept(int token, int nextExpectToken) {
        JSONLexer lexer2 = this.lexer;
        if (lexer2.token() == token) {
            lexer2.nextToken(nextExpectToken);
        } else {
            throwException(token);
        }
    }

    public void throwException(int token) {
        throw new JSONException("syntax error, expect " + JSONToken.name(token) + ", actual " + JSONToken.name(this.lexer.token()));
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        JSONLexer lexer2 = this.lexer;
        try {
            if (lexer2.isEnabled(Feature.AutoCloseSource)) {
                if (lexer2.token() != 20) {
                    throw new JSONException("not close json text, token : " + JSONToken.name(lexer2.token()));
                }
            }
        } finally {
            lexer2.close();
        }
    }

    public Object resolveReference(String ref) {
        if (this.contextArray == null) {
            return null;
        }
        int i = 0;
        while (i < this.contextArray.length && i < this.contextArrayIndex) {
            ParseContext context2 = this.contextArray[i];
            if (context2.toString().equals(ref)) {
                return context2.object;
            }
            i++;
        }
        return null;
    }

    public void handleResovleTask(Object value) {
        Object refValue;
        if (this.resolveTaskList != null) {
            int size = this.resolveTaskList.size();
            for (int i = 0; i < size; i++) {
                ResolveTask task = this.resolveTaskList.get(i);
                String ref = task.referenceValue;
                Object object = null;
                if (task.ownerContext != null) {
                    object = task.ownerContext.object;
                }
                if (ref.startsWith("$")) {
                    refValue = getObject(ref);
                    if (refValue == null) {
                        try {
                            refValue = JSONPath.eval(value, ref);
                        } catch (JSONPathException e) {
                        }
                    }
                } else {
                    refValue = task.context.object;
                }
                FieldDeserializer fieldDeser = task.fieldDeserializer;
                if (fieldDeser != null) {
                    if (refValue != null && refValue.getClass() == JSONObject.class && fieldDeser.fieldInfo != null && !Map.class.isAssignableFrom(fieldDeser.fieldInfo.fieldClass)) {
                        refValue = JSONPath.eval(this.contextArray[0].object, ref);
                    }
                    fieldDeser.setValue(object, refValue);
                }
            }
        }
    }

    public static class ResolveTask {
        public final ParseContext context;
        public FieldDeserializer fieldDeserializer;
        public ParseContext ownerContext;
        public final String referenceValue;

        public ResolveTask(ParseContext context2, String referenceValue2) {
            this.context = context2;
            this.referenceValue = referenceValue2;
        }
    }

    public void parseExtra(Object object, String key) {
        Object value;
        this.lexer.nextTokenWithColon();
        Type type = null;
        if (this.extraTypeProviders != null) {
            for (ExtraTypeProvider extraProvider : this.extraTypeProviders) {
                type = extraProvider.getExtraType(object, key);
            }
        }
        if (type == null) {
            value = parse();
        } else {
            value = parseObject(type);
        }
        if (object instanceof ExtraProcessable) {
            ((ExtraProcessable) object).processExtra(key, value);
            return;
        }
        if (this.extraProcessors != null) {
            for (ExtraProcessor process : this.extraProcessors) {
                process.processExtra(object, key, value);
            }
        }
        if (this.resolveStatus == 1) {
            this.resolveStatus = 0;
        }
    }

    /* JADX INFO: Multiple debug info for r0v2 com.alibaba.fastjson.parser.ParseContext: [D('msg' java.lang.String), D('context' com.alibaba.fastjson.parser.ParseContext)] */
    public Object parse(PropertyProcessable object, Object fieldName) {
        String key;
        Object value;
        int i = 0;
        if (this.lexer.token() != 12) {
            String msg = "syntax error, expect {, actual " + this.lexer.tokenName();
            if (fieldName instanceof String) {
                msg = (msg + ", fieldName ") + fieldName;
            }
            String msg2 = (msg + ", ") + this.lexer.info();
            JSONArray array = new JSONArray();
            parseArray(array, fieldName);
            if (array.size() == 1) {
                Object first = array.get(0);
                if (first instanceof JSONObject) {
                    return (JSONObject) first;
                }
            }
            throw new JSONException(msg2);
        }
        ParseContext context2 = this.context;
        while (true) {
            try {
                this.lexer.skipWhitespace();
                char ch = this.lexer.getCurrent();
                if (this.lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (ch == ',') {
                        this.lexer.next();
                        this.lexer.skipWhitespace();
                        ch = this.lexer.getCurrent();
                    }
                }
                if (ch == '\"') {
                    key = this.lexer.scanSymbol(this.symbolTable, '\"');
                    this.lexer.skipWhitespace();
                    if (this.lexer.getCurrent() != ':') {
                        throw new JSONException("expect ':' at " + this.lexer.pos());
                    }
                } else if (ch == '}') {
                    this.lexer.next();
                    this.lexer.resetStringPosition();
                    this.lexer.nextToken(16);
                    setContext(context2);
                    return object;
                } else if (ch == '\'') {
                    if (this.lexer.isEnabled(Feature.AllowSingleQuotes)) {
                        key = this.lexer.scanSymbol(this.symbolTable, '\'');
                        this.lexer.skipWhitespace();
                        if (this.lexer.getCurrent() != ':') {
                            throw new JSONException("expect ':' at " + this.lexer.pos());
                        }
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if (this.lexer.isEnabled(Feature.AllowUnQuotedFieldNames)) {
                    key = this.lexer.scanSymbolUnQuoted(this.symbolTable);
                    this.lexer.skipWhitespace();
                    char ch2 = this.lexer.getCurrent();
                    if (ch2 != ':') {
                        throw new JSONException("expect ':' at " + this.lexer.pos() + ", actual " + ch2);
                    }
                } else {
                    throw new JSONException("syntax error");
                }
                this.lexer.next();
                this.lexer.skipWhitespace();
                this.lexer.getCurrent();
                this.lexer.resetStringPosition();
                if (key != JSON.DEFAULT_TYPE_KEY || this.lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    this.lexer.nextToken();
                    if (i != 0) {
                        setContext(context2);
                    }
                    Type valueType = object.getType(key);
                    if (this.lexer.token() == 8) {
                        value = null;
                        this.lexer.nextToken();
                    } else {
                        value = parseObject(valueType, key);
                    }
                    object.apply(key, value);
                    setContext(context2, value, key);
                    setContext(context2);
                    int tok = this.lexer.token();
                    if (tok == 20 || tok == 15) {
                        setContext(context2);
                    } else if (tok == 13) {
                        this.lexer.nextToken();
                        setContext(context2);
                        return object;
                    }
                } else {
                    Class<?> clazz = this.config.checkAutoType(this.lexer.scanSymbol(this.symbolTable, '\"'), null, this.lexer.getFeatures());
                    if (Map.class.isAssignableFrom(clazz)) {
                        this.lexer.nextToken(16);
                        if (this.lexer.token() == 13) {
                            this.lexer.nextToken(16);
                            return object;
                        }
                    } else {
                        ObjectDeserializer deserializer = this.config.getDeserializer(clazz);
                        this.lexer.nextToken(16);
                        setResolveStatus(2);
                        if (context2 != null && !(fieldName instanceof Integer)) {
                            popContext();
                        }
                        Map map = (Map) deserializer.deserialze(this, clazz, fieldName);
                        setContext(context2);
                        return map;
                    }
                }
                i++;
            } finally {
                setContext(context2);
            }
        }
        setContext(context2);
        return object;
    }
}
