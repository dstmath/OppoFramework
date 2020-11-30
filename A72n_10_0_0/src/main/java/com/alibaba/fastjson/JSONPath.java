package com.alibaba.fastjson;

import com.alibaba.fastjson.asm.Opcodes;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexerBase;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.FieldSerializer;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public class JSONPath implements JSONAware {
    static final long LENGTH = -1580386065683472715L;
    static final long SIZE = 5614464919154503228L;
    private static ConcurrentMap<String, JSONPath> pathCache = new ConcurrentHashMap(Opcodes.IOR, 0.75f, 1);
    private boolean hasRefSegment;
    private ParserConfig parserConfig;
    private final String path;
    private Segment[] segments;
    private SerializeConfig serializeConfig;

    /* access modifiers changed from: package-private */
    public interface Filter {
        boolean apply(JSONPath jSONPath, Object obj, Object obj2, Object obj3);
    }

    /* access modifiers changed from: package-private */
    public enum Operator {
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        LIKE,
        NOT_LIKE,
        RLIKE,
        NOT_RLIKE,
        IN,
        NOT_IN,
        BETWEEN,
        NOT_BETWEEN,
        And,
        Or,
        REG_MATCH
    }

    /* access modifiers changed from: package-private */
    public interface Segment {
        Object eval(JSONPath jSONPath, Object obj, Object obj2);

        void extract(JSONPath jSONPath, DefaultJSONParser defaultJSONParser, Context context);
    }

    public JSONPath(String path2) {
        this(path2, SerializeConfig.getGlobalInstance(), ParserConfig.getGlobalInstance());
    }

    public JSONPath(String path2, SerializeConfig serializeConfig2, ParserConfig parserConfig2) {
        if (path2 == null || path2.length() == 0) {
            throw new JSONPathException("json-path can not be null or empty");
        }
        this.path = path2;
        this.serializeConfig = serializeConfig2;
        this.parserConfig = parserConfig2;
    }

    /* access modifiers changed from: protected */
    public void init() {
        if (this.segments == null) {
            if ("*".equals(this.path)) {
                this.segments = new Segment[]{WildCardSegment.instance};
                return;
            }
            JSONPathParser parser = new JSONPathParser(this.path);
            this.segments = parser.explain();
            this.hasRefSegment = parser.hasRefSegment;
        }
    }

    public Object eval(Object rootObject) {
        if (rootObject == null) {
            return null;
        }
        init();
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; i++) {
            currentObject = this.segments[i].eval(this, rootObject, currentObject);
        }
        return currentObject;
    }

    public Object extract(DefaultJSONParser parser) {
        boolean eval;
        if (parser == null) {
            return null;
        }
        init();
        if (this.hasRefSegment) {
            return eval(parser.parse());
        }
        if (this.segments.length == 0) {
            return parser.parse();
        }
        Context context = null;
        int i = 0;
        while (i < this.segments.length) {
            Segment segment = this.segments[i];
            boolean eval2 = true;
            boolean last = i == this.segments.length - 1;
            if (context == null || context.object == null) {
                if (!last) {
                    Segment nextSegment = this.segments[i + 1];
                    if ((segment instanceof PropertySegment) && ((PropertySegment) segment).deep && ((nextSegment instanceof ArrayAccessSegment) || (nextSegment instanceof MultiIndexSegment) || (nextSegment instanceof MultiPropertySegment) || (nextSegment instanceof SizeSegment) || (nextSegment instanceof PropertySegment) || (nextSegment instanceof FilterSegment))) {
                        eval = true;
                    } else if ((nextSegment instanceof ArrayAccessSegment) && ((ArrayAccessSegment) nextSegment).index < 0) {
                        eval = true;
                    } else if (nextSegment instanceof FilterSegment) {
                        eval = true;
                    } else if (segment instanceof WildCardSegment) {
                        eval = true;
                    } else {
                        eval = false;
                    }
                    eval2 = eval;
                }
                context = new Context(context, eval2);
                segment.extract(this, parser, context);
            } else {
                context.object = segment.eval(this, null, context.object);
            }
            i++;
        }
        return context.object;
    }

    /* access modifiers changed from: private */
    public static class Context {
        final boolean eval;
        Object object;
        final Context parent;

        public Context(Context parent2, boolean eval2) {
            this.parent = parent2;
            this.eval = eval2;
        }
    }

    public boolean contains(Object rootObject) {
        if (rootObject == null) {
            return false;
        }
        init();
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; i++) {
            currentObject = this.segments[i].eval(this, rootObject, currentObject);
            if (currentObject == null) {
                return false;
            }
            if (currentObject == Collections.EMPTY_LIST && (currentObject instanceof List)) {
                return ((List) currentObject).contains(currentObject);
            }
        }
        return true;
    }

    public boolean containsValue(Object rootObject, Object value) {
        Object currentObject = eval(rootObject);
        if (currentObject == value) {
            return true;
        }
        if (currentObject == null) {
            return false;
        }
        if (!(currentObject instanceof Iterable)) {
            return eq(currentObject, value);
        }
        for (Object item : (Iterable) currentObject) {
            if (eq(item, value)) {
                return true;
            }
        }
        return false;
    }

    public int size(Object rootObject) {
        if (rootObject == null) {
            return -1;
        }
        init();
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; i++) {
            currentObject = this.segments[i].eval(this, rootObject, currentObject);
        }
        return evalSize(currentObject);
    }

    public Set<?> keySet(Object rootObject) {
        if (rootObject == null) {
            return null;
        }
        init();
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; i++) {
            currentObject = this.segments[i].eval(this, rootObject, currentObject);
        }
        return evalKeySet(currentObject);
    }

    public void arrayAdd(Object rootObject, Object... values) {
        if (values != null && values.length != 0 && rootObject != null) {
            init();
            int i = 0;
            Object parentObject = null;
            Object currentObject = rootObject;
            for (int i2 = 0; i2 < this.segments.length; i2++) {
                if (i2 == this.segments.length - 1) {
                    parentObject = currentObject;
                }
                currentObject = this.segments[i2].eval(this, rootObject, currentObject);
            }
            if (currentObject == null) {
                throw new JSONPathException("value not found in path " + this.path);
            } else if (currentObject instanceof Collection) {
                Collection collection = (Collection) currentObject;
                int length = values.length;
                while (i < length) {
                    collection.add(values[i]);
                    i++;
                }
            } else {
                Class<?> resultClass = currentObject.getClass();
                if (resultClass.isArray()) {
                    int length2 = Array.getLength(currentObject);
                    Object descArray = Array.newInstance(resultClass.getComponentType(), values.length + length2);
                    System.arraycopy(currentObject, 0, descArray, 0, length2);
                    while (i < values.length) {
                        Array.set(descArray, length2 + i, values[i]);
                        i++;
                    }
                    Segment lastSegment = this.segments[this.segments.length - 1];
                    if (lastSegment instanceof PropertySegment) {
                        ((PropertySegment) lastSegment).setValue(this, parentObject, descArray);
                    } else if (lastSegment instanceof ArrayAccessSegment) {
                        ((ArrayAccessSegment) lastSegment).setValue(this, parentObject, descArray);
                    } else {
                        throw new UnsupportedOperationException();
                    }
                } else {
                    throw new JSONException("unsupported array put operation. " + resultClass);
                }
            }
        }
    }

    public boolean remove(Object rootObject) {
        if (rootObject == null) {
            return false;
        }
        init();
        Object parentObject = null;
        Object currentObject = rootObject;
        int i = 0;
        while (true) {
            if (i >= this.segments.length) {
                break;
            } else if (i == this.segments.length - 1) {
                parentObject = currentObject;
                break;
            } else {
                currentObject = this.segments[i].eval(this, rootObject, currentObject);
                if (currentObject == null) {
                    break;
                }
                i++;
            }
        }
        if (parentObject == null) {
            return false;
        }
        Segment lastSegment = this.segments[this.segments.length - 1];
        if (lastSegment instanceof PropertySegment) {
            PropertySegment propertySegment = (PropertySegment) lastSegment;
            if ((parentObject instanceof Collection) && this.segments.length > 1) {
                Segment parentSegment = this.segments[this.segments.length - 2];
                if ((parentSegment instanceof RangeSegment) || (parentSegment instanceof MultiIndexSegment)) {
                    boolean removedOnce = false;
                    for (Object item : (Collection) parentObject) {
                        if (propertySegment.remove(this, item)) {
                            removedOnce = true;
                        }
                    }
                    return removedOnce;
                }
            }
            return propertySegment.remove(this, parentObject);
        } else if (lastSegment instanceof ArrayAccessSegment) {
            return ((ArrayAccessSegment) lastSegment).remove(this, parentObject);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean set(Object rootObject, Object value) {
        return set(rootObject, value, true);
    }

    public boolean set(Object rootObject, Object value, boolean p) {
        Object obj;
        if (rootObject == null) {
            return false;
        }
        init();
        Object parentObject = null;
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; i++) {
            parentObject = currentObject;
            Segment segment = this.segments[i];
            currentObject = segment.eval(this, rootObject, currentObject);
            if (currentObject == null) {
                Segment nextSegment = null;
                if (i < this.segments.length - 1) {
                    nextSegment = this.segments[i + 1];
                }
                Object newObj = null;
                if (nextSegment instanceof PropertySegment) {
                    JavaBeanDeserializer beanDeserializer = null;
                    Class<?> fieldClass = null;
                    if (segment instanceof PropertySegment) {
                        String propertyName = ((PropertySegment) segment).propertyName;
                        JavaBeanDeserializer parentBeanDeserializer = getJavaBeanDeserializer(parentObject.getClass());
                        if (parentBeanDeserializer != null) {
                            fieldClass = parentBeanDeserializer.getFieldDeserializer(propertyName).fieldInfo.fieldClass;
                            beanDeserializer = getJavaBeanDeserializer(fieldClass);
                        }
                    }
                    if (beanDeserializer == null) {
                        obj = new JSONObject();
                    } else if (beanDeserializer.beanInfo.defaultConstructor == null) {
                        return false;
                    } else {
                        obj = beanDeserializer.createInstance((DefaultJSONParser) null, fieldClass);
                    }
                    newObj = obj;
                } else if (nextSegment instanceof ArrayAccessSegment) {
                    newObj = new JSONArray();
                }
                if (newObj == null) {
                    break;
                } else if (!(segment instanceof PropertySegment)) {
                    if (!(segment instanceof ArrayAccessSegment)) {
                        break;
                    }
                    ((ArrayAccessSegment) segment).setValue(this, parentObject, newObj);
                    currentObject = newObj;
                } else {
                    ((PropertySegment) segment).setValue(this, parentObject, newObj);
                    currentObject = newObj;
                }
            }
        }
        if (parentObject == null) {
            return false;
        }
        Segment lastSegment = this.segments[this.segments.length - 1];
        if (lastSegment instanceof PropertySegment) {
            ((PropertySegment) lastSegment).setValue(this, parentObject, value);
            return true;
        } else if (lastSegment instanceof ArrayAccessSegment) {
            return ((ArrayAccessSegment) lastSegment).setValue(this, parentObject, value);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static Object eval(Object rootObject, String path2) {
        return compile(path2).eval(rootObject);
    }

    public static int size(Object rootObject, String path2) {
        JSONPath jsonpath = compile(path2);
        return jsonpath.evalSize(jsonpath.eval(rootObject));
    }

    public static Set<?> keySet(Object rootObject, String path2) {
        JSONPath jsonpath = compile(path2);
        return jsonpath.evalKeySet(jsonpath.eval(rootObject));
    }

    public static boolean contains(Object rootObject, String path2) {
        if (rootObject == null) {
            return false;
        }
        return compile(path2).contains(rootObject);
    }

    public static boolean containsValue(Object rootObject, String path2, Object value) {
        return compile(path2).containsValue(rootObject, value);
    }

    public static void arrayAdd(Object rootObject, String path2, Object... values) {
        compile(path2).arrayAdd(rootObject, values);
    }

    public static boolean set(Object rootObject, String path2, Object value) {
        return compile(path2).set(rootObject, value);
    }

    public static boolean remove(Object root, String path2) {
        return compile(path2).remove(root);
    }

    public static JSONPath compile(String path2) {
        if (path2 != null) {
            JSONPath jsonpath = pathCache.get(path2);
            if (jsonpath != null) {
                return jsonpath;
            }
            JSONPath jsonpath2 = new JSONPath(path2);
            if (pathCache.size() >= 1024) {
                return jsonpath2;
            }
            pathCache.putIfAbsent(path2, jsonpath2);
            return pathCache.get(path2);
        }
        throw new JSONPathException("jsonpath can not be null");
    }

    public static Object read(String json, String path2) {
        return compile(path2).eval(JSON.parse(json));
    }

    public static Object extract(String json, String path2, ParserConfig config, int features, Feature... optionFeatures) {
        DefaultJSONParser parser = new DefaultJSONParser(json, config, features | Feature.OrderedField.mask);
        Object result = compile(path2).extract(parser);
        parser.lexer.close();
        return result;
    }

    public static Object extract(String json, String path2) {
        return extract(json, path2, ParserConfig.global, JSON.DEFAULT_PARSER_FEATURE, new Feature[0]);
    }

    public static Map<String, Object> paths(Object javaObject) {
        return paths(javaObject, SerializeConfig.globalInstance);
    }

    public static Map<String, Object> paths(Object javaObject, SerializeConfig config) {
        Map<Object, String> values = new IdentityHashMap<>();
        Map<String, Object> paths = new HashMap<>();
        paths(values, paths, "/", javaObject, config);
        return paths;
    }

    private static void paths(Map<Object, String> values, Map<String, Object> paths, String parent, Object javaObject, SerializeConfig config) {
        StringBuilder sb;
        StringBuilder sb2;
        StringBuilder sb3;
        StringBuilder sb4;
        if (javaObject != null) {
            if (values.put(javaObject, parent) != null) {
                if (!((javaObject instanceof String) || (javaObject instanceof Number) || (javaObject instanceof Date) || (javaObject instanceof UUID))) {
                    return;
                }
            }
            paths.put(parent, javaObject);
            if (javaObject instanceof Map) {
                for (Map.Entry entry : ((Map) javaObject).entrySet()) {
                    Object key = entry.getKey();
                    if (key instanceof String) {
                        if (parent.equals("/")) {
                            sb4 = new StringBuilder();
                        } else {
                            sb4 = new StringBuilder();
                            sb4.append(parent);
                        }
                        sb4.append("/");
                        sb4.append(key);
                        paths(values, paths, sb4.toString(), entry.getValue(), config);
                    }
                }
            } else if (javaObject instanceof Collection) {
                int i = 0;
                for (Object item : (Collection) javaObject) {
                    if (parent.equals("/")) {
                        sb3 = new StringBuilder();
                    } else {
                        sb3 = new StringBuilder();
                        sb3.append(parent);
                    }
                    sb3.append("/");
                    sb3.append(i);
                    paths(values, paths, sb3.toString(), item, config);
                    i++;
                }
            } else {
                Class<?> clazz = javaObject.getClass();
                if (clazz.isArray()) {
                    int len = Array.getLength(javaObject);
                    for (int i2 = 0; i2 < len; i2++) {
                        Object item2 = Array.get(javaObject, i2);
                        if (parent.equals("/")) {
                            sb2 = new StringBuilder();
                        } else {
                            sb2 = new StringBuilder();
                            sb2.append(parent);
                        }
                        sb2.append("/");
                        sb2.append(i2);
                        paths(values, paths, sb2.toString(), item2, config);
                    }
                } else if (!ParserConfig.isPrimitive2(clazz) && !clazz.isEnum()) {
                    ObjectSerializer serializer = config.getObjectWriter(clazz);
                    if (serializer instanceof JavaBeanSerializer) {
                        try {
                            for (Map.Entry<String, Object> entry2 : ((JavaBeanSerializer) serializer).getFieldValuesMap(javaObject).entrySet()) {
                                String key2 = entry2.getKey();
                                if (key2 instanceof String) {
                                    if (parent.equals("/")) {
                                        sb = new StringBuilder();
                                        sb.append("/");
                                        sb.append(key2);
                                    } else {
                                        sb = new StringBuilder();
                                        sb.append(parent);
                                        sb.append("/");
                                        sb.append(key2);
                                    }
                                    paths(values, paths, sb.toString(), entry2.getValue(), config);
                                }
                            }
                        } catch (Exception e) {
                            throw new JSONException("toJSON error", e);
                        }
                    }
                }
            }
        }
    }

    public String getPath() {
        return this.path;
    }

    /* access modifiers changed from: package-private */
    public static class JSONPathParser {
        private char ch;
        private boolean hasRefSegment;
        private int level;
        private final String path;
        private int pos;

        public JSONPathParser(String path2) {
            this.path = path2;
            next();
        }

        /* access modifiers changed from: package-private */
        public void next() {
            String str = this.path;
            int i = this.pos;
            this.pos = i + 1;
            this.ch = str.charAt(i);
        }

        /* access modifiers changed from: package-private */
        public char getNextChar() {
            return this.path.charAt(this.pos);
        }

        /* access modifiers changed from: package-private */
        public boolean isEOF() {
            return this.pos >= this.path.length();
        }

        /* access modifiers changed from: package-private */
        public Segment readSegement() {
            if (this.level == 0 && this.path.length() == 1) {
                if (isDigitFirst(this.ch)) {
                    return new ArrayAccessSegment(this.ch - '0');
                }
                if ((this.ch >= 97 && this.ch <= 'z') || (this.ch >= 'A' && this.ch <= 'Z')) {
                    return new PropertySegment(Character.toString(this.ch), false);
                }
            }
            while (!isEOF()) {
                skipWhitespace();
                if (this.ch == '$') {
                    next();
                } else if (this.ch == '.' || this.ch == '/') {
                    int c0 = this.ch;
                    boolean deep = false;
                    next();
                    if (c0 == 46 && this.ch == '.') {
                        next();
                        deep = true;
                        if (this.path.length() > this.pos + 3 && this.ch == '[' && this.path.charAt(this.pos) == '*' && this.path.charAt(this.pos + 1) == ']' && this.path.charAt(this.pos + 2) == '.') {
                            next();
                            next();
                            next();
                            next();
                        }
                    }
                    if (this.ch == '*') {
                        if (!isEOF()) {
                            next();
                        }
                        return deep ? WildCardSegment.instance_deep : WildCardSegment.instance;
                    } else if (isDigitFirst(this.ch)) {
                        return parseArrayAccess(false);
                    } else {
                        String propertyName = readName();
                        if (this.ch != '(') {
                            return new PropertySegment(propertyName, deep);
                        }
                        next();
                        if (this.ch == ')') {
                            if (!isEOF()) {
                                next();
                            }
                            if ("size".equals(propertyName) || "length".equals(propertyName)) {
                                return SizeSegment.instance;
                            }
                            if ("max".equals(propertyName)) {
                                return MaxSegment.instance;
                            }
                            if ("min".equals(propertyName)) {
                                return MinSegment.instance;
                            }
                            if ("keySet".equals(propertyName)) {
                                return KeySetSegment.instance;
                            }
                            throw new JSONPathException("not support jsonpath : " + this.path);
                        }
                        throw new JSONPathException("not support jsonpath : " + this.path);
                    }
                } else if (this.ch == '[') {
                    return parseArrayAccess(true);
                } else {
                    if (this.level == 0) {
                        return new PropertySegment(readName(), false);
                    }
                    throw new JSONPathException("not support jsonpath : " + this.path);
                }
            }
            return null;
        }

        public final void skipWhitespace() {
            while (this.ch <= ' ') {
                if (this.ch == ' ' || this.ch == '\r' || this.ch == '\n' || this.ch == '\t' || this.ch == '\f' || this.ch == '\b') {
                    next();
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public Segment parseArrayAccess(boolean acceptBracket) {
            Object object = parseArrayAccessFilter(acceptBracket);
            if (object instanceof Segment) {
                return (Segment) object;
            }
            return new FilterSegment((Filter) object);
        }

        /* JADX INFO: Multiple debug info for r10v14 java.lang.String: [D('startsWithValue' java.lang.String), D('endsWithValue' java.lang.String)] */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Removed duplicated region for block: B:288:0x0413 A[LOOP:10: B:286:0x040d->B:288:0x0413, LOOP_END] */
        /* JADX WARNING: Removed duplicated region for block: B:295:0x0427  */
        /* JADX WARNING: Removed duplicated region for block: B:297:0x042e  */
        public Object parseArrayAccessFilter(boolean acceptBracket) {
            Filter filter;
            Filter filter2;
            char c;
            String startsWithValue;
            String endsWithValue;
            String[] containsValues;
            String[] containsValues2;
            Operator op;
            Filter filter3;
            int end;
            if (acceptBracket) {
                accept('[');
            }
            boolean predicateFlag = false;
            if (this.ch == '?') {
                next();
                accept('(');
                predicateFlag = true;
            }
            if (predicateFlag || IOUtils.firstIdentifier(this.ch) || this.ch == '\\' || this.ch == '@') {
                if (this.ch == '@') {
                    next();
                    accept('.');
                }
                String propertyName = readName();
                skipWhitespace();
                if (predicateFlag && this.ch == ')') {
                    next();
                    Filter filter4 = new NotNullSegement(propertyName);
                    while (this.ch == ' ') {
                        next();
                    }
                    if (this.ch == '&' || this.ch == '|') {
                        filter4 = filterRest(filter4);
                    }
                    if (acceptBracket) {
                        accept(']');
                    }
                    return filter4;
                } else if (!acceptBracket || this.ch != ']') {
                    Operator op2 = readOp();
                    skipWhitespace();
                    if (op2 == Operator.BETWEEN || op2 == Operator.NOT_BETWEEN) {
                        boolean not = op2 == Operator.NOT_BETWEEN;
                        Object startValue = readValue();
                        if ("and".equalsIgnoreCase(readName())) {
                            Object endValue = readValue();
                            if (startValue == null || endValue == null) {
                                throw new JSONPathException(this.path);
                            } else if (JSONPath.isInt(startValue.getClass()) && JSONPath.isInt(endValue.getClass())) {
                                return new IntBetweenSegement(propertyName, TypeUtils.longExtractValue((Number) startValue), TypeUtils.longExtractValue((Number) endValue), not);
                            } else {
                                throw new JSONPathException(this.path);
                            }
                        } else {
                            throw new JSONPathException(this.path);
                        }
                    } else if (op2 == Operator.IN || op2 == Operator.NOT_IN) {
                        boolean z = true;
                        boolean z2 = true;
                        boolean not2 = op2 == Operator.NOT_IN;
                        accept('(');
                        List<Object> valueList = new JSONArray();
                        valueList.add(readValue());
                        while (true) {
                            skipWhitespace();
                            if (this.ch != ',') {
                                break;
                            }
                            next();
                            valueList.add(readValue());
                            z2 = z2;
                            z = z;
                        }
                        boolean isInt = true;
                        boolean isIntObj = true;
                        boolean isString = true;
                        for (Object item : valueList) {
                            if (item != null) {
                                Class<?> clazz = item.getClass();
                                if (!(!isInt || clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class)) {
                                    isIntObj = false;
                                    isInt = false;
                                }
                                if (isString && clazz != String.class) {
                                    isString = false;
                                }
                            } else if (isInt) {
                                isInt = false;
                            }
                        }
                        if (valueList.size() == 1 && valueList.get(0) == null) {
                            if (not2) {
                                filter = new NotNullSegement(propertyName);
                            } else {
                                filter = new NullSegement(propertyName);
                            }
                            while (this.ch == ' ') {
                                next();
                            }
                            if (this.ch == '&' || this.ch == '|') {
                                filter = filterRest(filter);
                            }
                            accept(')');
                            if (predicateFlag) {
                                accept(')');
                            }
                            if (acceptBracket) {
                                accept(']');
                            }
                            return filter;
                        } else if (isInt) {
                            if (valueList.size() == 1) {
                                long value = TypeUtils.longExtractValue((Number) valueList.get(0));
                                Filter filter5 = new IntOpSegement(propertyName, value, not2 ? Operator.NE : Operator.EQ);
                                while (this.ch == ' ') {
                                    next();
                                    value = value;
                                }
                                if (this.ch == '&' || this.ch == '|') {
                                    filter5 = filterRest(filter5);
                                }
                                accept(')');
                                if (predicateFlag) {
                                    accept(')');
                                }
                                if (acceptBracket) {
                                    accept(']');
                                }
                                return filter5;
                            }
                            long[] values = new long[valueList.size()];
                            for (int i = 0; i < values.length; i++) {
                                values[i] = TypeUtils.longExtractValue((Number) valueList.get(i));
                            }
                            Filter filter6 = new IntInSegement(propertyName, values, not2);
                            while (this.ch == ' ') {
                                next();
                            }
                            if (this.ch == '&' || this.ch == '|') {
                                filter6 = filterRest(filter6);
                            }
                            accept(')');
                            if (predicateFlag) {
                                accept(')');
                            }
                            if (acceptBracket) {
                                accept(']');
                            }
                            return filter6;
                        } else if (!isString) {
                            if (isIntObj) {
                                Long[] values2 = new Long[valueList.size()];
                                for (int i2 = 0; i2 < values2.length; i2++) {
                                    Number item2 = (Number) valueList.get(i2);
                                    if (item2 != null) {
                                        values2[i2] = Long.valueOf(TypeUtils.longExtractValue(item2));
                                    }
                                }
                                Filter filter7 = new IntObjInSegement(propertyName, values2, not2);
                                while (this.ch == ' ') {
                                    next();
                                }
                                if (this.ch == '&' || this.ch == '|') {
                                    filter7 = filterRest(filter7);
                                }
                                accept(')');
                                if (predicateFlag) {
                                    accept(')');
                                }
                                if (acceptBracket) {
                                    accept(']');
                                }
                                return filter7;
                            }
                            throw new UnsupportedOperationException();
                        } else if (valueList.size() == 1) {
                            Filter filter8 = new StringOpSegement(propertyName, (String) valueList.get(0), not2 ? Operator.NE : Operator.EQ);
                            while (this.ch == ' ') {
                                next();
                            }
                            if (this.ch == '&' || this.ch == '|') {
                                filter8 = filterRest(filter8);
                            }
                            accept(')');
                            if (predicateFlag) {
                                accept(')');
                            }
                            if (acceptBracket) {
                                accept(']');
                            }
                            return filter8;
                        } else {
                            String[] values3 = new String[valueList.size()];
                            valueList.toArray(values3);
                            Filter filter9 = new StringInSegement(propertyName, values3, not2);
                            while (this.ch == ' ') {
                                next();
                            }
                            if (this.ch == '&' || this.ch == '|') {
                                filter9 = filterRest(filter9);
                            }
                            accept(')');
                            if (predicateFlag) {
                                accept(')');
                            }
                            if (acceptBracket) {
                                accept(']');
                            }
                            return filter9;
                        }
                    } else if (this.ch == '\'' || this.ch == '\"') {
                        String strValue = readString();
                        if (op2 == Operator.RLIKE) {
                            filter2 = new RlikeSegement(propertyName, strValue, false);
                        } else if (op2 == Operator.NOT_RLIKE) {
                            filter2 = new RlikeSegement(propertyName, strValue, true);
                        } else if (op2 == Operator.LIKE || op2 == Operator.NOT_LIKE) {
                            while (strValue.indexOf("%%") != -1) {
                                strValue = strValue.replaceAll("%%", "%");
                            }
                            boolean not3 = op2 == Operator.NOT_LIKE;
                            int p0 = strValue.indexOf(37);
                            if (p0 == -1) {
                                if (op2 == Operator.LIKE) {
                                    op = Operator.EQ;
                                } else {
                                    op = Operator.NE;
                                }
                                filter2 = new StringOpSegement(propertyName, strValue, op);
                                c = '&';
                            } else {
                                String[] items = strValue.split("%");
                                if (p0 != 0) {
                                    if (strValue.charAt(strValue.length() - 1) == '%') {
                                        if (items.length == 1) {
                                            endsWithValue = items[0];
                                        } else {
                                            containsValues2 = items;
                                        }
                                    } else if (items.length == 1) {
                                        endsWithValue = items[0];
                                    } else if (items.length == 2) {
                                        endsWithValue = items[0];
                                        startsWithValue = items[1];
                                        containsValues = null;
                                        c = '&';
                                        filter2 = new MatchSegement(propertyName, endsWithValue, startsWithValue, containsValues, not3);
                                    } else {
                                        String endsWithValue2 = items[0];
                                        String endsWithValue3 = items[items.length - 1];
                                        containsValues = new String[(items.length - 2)];
                                        System.arraycopy(items, 1, containsValues, 0, containsValues.length);
                                        endsWithValue = endsWithValue2;
                                        startsWithValue = endsWithValue3;
                                        c = '&';
                                        filter2 = new MatchSegement(propertyName, endsWithValue, startsWithValue, containsValues, not3);
                                    }
                                    startsWithValue = null;
                                    containsValues = null;
                                    c = '&';
                                    filter2 = new MatchSegement(propertyName, endsWithValue, startsWithValue, containsValues, not3);
                                } else if (strValue.charAt(strValue.length() - 1) == '%') {
                                    containsValues2 = new String[(items.length - 1)];
                                    System.arraycopy(items, 1, containsValues2, 0, containsValues2.length);
                                } else {
                                    String endsWithValue4 = items[items.length - 1];
                                    if (items.length > 2) {
                                        String[] containsValues3 = new String[(items.length - 2)];
                                        System.arraycopy(items, 1, containsValues3, 0, containsValues3.length);
                                        startsWithValue = endsWithValue4;
                                        containsValues = containsValues3;
                                        endsWithValue = null;
                                        c = '&';
                                        filter2 = new MatchSegement(propertyName, endsWithValue, startsWithValue, containsValues, not3);
                                    } else {
                                        startsWithValue = endsWithValue4;
                                        endsWithValue = null;
                                        containsValues = null;
                                        c = '&';
                                        filter2 = new MatchSegement(propertyName, endsWithValue, startsWithValue, containsValues, not3);
                                    }
                                }
                                containsValues = containsValues2;
                                endsWithValue = null;
                                startsWithValue = null;
                                c = '&';
                                filter2 = new MatchSegement(propertyName, endsWithValue, startsWithValue, containsValues, not3);
                            }
                            while (this.ch == ' ') {
                                next();
                            }
                            if (this.ch == c || this.ch == '|') {
                                filter2 = filterRest(filter2);
                            }
                            if (predicateFlag) {
                                accept(')');
                            }
                            if (acceptBracket) {
                                accept(']');
                            }
                            return filter2;
                        } else {
                            filter2 = new StringOpSegement(propertyName, strValue, op2);
                        }
                        c = '&';
                        while (this.ch == ' ') {
                        }
                        filter2 = filterRest(filter2);
                        if (predicateFlag) {
                        }
                        if (acceptBracket) {
                        }
                        return filter2;
                    } else if (isDigitFirst(this.ch)) {
                        long value2 = readLongValue();
                        double doubleValue = 0.0d;
                        if (this.ch == '.') {
                            doubleValue = readDoubleValue(value2);
                        }
                        if (doubleValue == 0.0d) {
                            filter3 = new IntOpSegement(propertyName, value2, op2);
                        } else {
                            filter3 = new DoubleOpSegement(propertyName, doubleValue, op2);
                        }
                        while (this.ch == ' ') {
                            next();
                        }
                        if (this.ch == '&' || this.ch == '|') {
                            filter3 = filterRest(filter3);
                        }
                        if (predicateFlag) {
                            accept(')');
                        }
                        if (acceptBracket) {
                            accept(']');
                        }
                        return filter3;
                    } else if (this.ch == '$') {
                        RefOpSegement filter10 = new RefOpSegement(propertyName, readSegement(), op2);
                        this.hasRefSegment = true;
                        while (this.ch == ' ') {
                            next();
                        }
                        if (predicateFlag) {
                            accept(')');
                        }
                        if (acceptBracket) {
                            accept(']');
                        }
                        return filter10;
                    } else if (this.ch == '/') {
                        int flags = 0;
                        StringBuilder regBuf = new StringBuilder();
                        while (true) {
                            next();
                            if (this.ch == '/') {
                                break;
                            } else if (this.ch == '\\') {
                                next();
                                regBuf.append(this.ch);
                            } else {
                                regBuf.append(this.ch);
                            }
                        }
                        next();
                        if (this.ch == 'i') {
                            next();
                            flags = 0 | 2;
                        }
                        RegMatchSegement filter11 = new RegMatchSegement(propertyName, Pattern.compile(regBuf.toString(), flags), op2);
                        if (predicateFlag) {
                            accept(')');
                        }
                        if (acceptBracket) {
                            accept(']');
                        }
                        return filter11;
                    } else {
                        if (this.ch == 110) {
                            if ("null".equals(readName())) {
                                Filter filter12 = null;
                                if (op2 == Operator.EQ) {
                                    filter12 = new NullSegement(propertyName);
                                } else if (op2 == Operator.NE) {
                                    filter12 = new NotNullSegement(propertyName);
                                }
                                if (filter12 != null) {
                                    while (this.ch == ' ') {
                                        next();
                                    }
                                    if (this.ch == '&' || this.ch == '|') {
                                        filter12 = filterRest(filter12);
                                    }
                                }
                                if (predicateFlag) {
                                    accept(')');
                                }
                                accept(']');
                                if (filter12 != null) {
                                    return filter12;
                                }
                                throw new UnsupportedOperationException();
                            }
                        } else if (this.ch == 't') {
                            if ("true".equals(readName())) {
                                Filter filter13 = null;
                                if (op2 == Operator.EQ) {
                                    filter13 = new ValueSegment(propertyName, Boolean.TRUE, true);
                                } else if (op2 == Operator.NE) {
                                    filter13 = new ValueSegment(propertyName, Boolean.TRUE, false);
                                }
                                if (filter13 != null) {
                                    while (this.ch == ' ') {
                                        next();
                                    }
                                    if (this.ch == '&' || this.ch == '|') {
                                        filter13 = filterRest(filter13);
                                    }
                                }
                                if (predicateFlag) {
                                    accept(')');
                                }
                                accept(']');
                                if (filter13 != null) {
                                    return filter13;
                                }
                                throw new UnsupportedOperationException();
                            }
                        } else if (this.ch == 'f' && "false".equals(readName())) {
                            Filter filter14 = null;
                            if (op2 == Operator.EQ) {
                                filter14 = new ValueSegment(propertyName, Boolean.FALSE, true);
                            } else if (op2 == Operator.NE) {
                                filter14 = new ValueSegment(propertyName, Boolean.FALSE, false);
                            }
                            if (filter14 != null) {
                                while (this.ch == ' ') {
                                    next();
                                }
                                if (this.ch == '&' || this.ch == '|') {
                                    filter14 = filterRest(filter14);
                                }
                            }
                            if (predicateFlag) {
                                accept(')');
                            }
                            accept(']');
                            if (filter14 != null) {
                                return filter14;
                            }
                            throw new UnsupportedOperationException();
                        }
                        throw new UnsupportedOperationException();
                    }
                } else {
                    next();
                    Filter filter15 = new NotNullSegement(propertyName);
                    while (this.ch == ' ') {
                        next();
                    }
                    if (this.ch == '&' || this.ch == '|') {
                        filter15 = filterRest(filter15);
                    }
                    accept(')');
                    if (predicateFlag) {
                        accept(')');
                    }
                    if (acceptBracket) {
                        accept(']');
                    }
                    return filter15;
                }
            } else {
                int start = this.pos - 1;
                char startCh = this.ch;
                while (this.ch != ']' && this.ch != '/' && !isEOF() && (this.ch != '.' || predicateFlag || predicateFlag || startCh == '\'')) {
                    if (this.ch == '\\') {
                        next();
                    }
                    next();
                }
                if (acceptBracket) {
                    end = this.pos - 1;
                } else if (this.ch == 47 || this.ch == '.') {
                    end = this.pos - 1;
                } else {
                    end = this.pos;
                }
                String text = this.path.substring(start, end);
                if (text.indexOf("\\.") != -1) {
                    String propName = text.replaceAll("\\\\\\.", "\\.");
                    if (propName.indexOf("\\-") != -1) {
                        propName = propName.replaceAll("\\\\-", "-");
                    }
                    if (predicateFlag) {
                        accept(')');
                    }
                    return new PropertySegment(propName, false);
                }
                Segment segment = buildArraySegement(text);
                if (acceptBracket && !isEOF()) {
                    accept(']');
                }
                return segment;
            }
        }

        /* access modifiers changed from: package-private */
        public Filter filterRest(Filter filter) {
            boolean and = this.ch == '&';
            if ((this.ch != '&' || getNextChar() != '&') && (this.ch != '|' || getNextChar() != '|')) {
                return filter;
            }
            next();
            next();
            while (this.ch == ' ') {
                next();
            }
            return new FilterGroup(filter, (Filter) parseArrayAccessFilter(false), and);
        }

        /* access modifiers changed from: protected */
        public long readLongValue() {
            int beginIndex = this.pos - 1;
            if (this.ch == '+' || this.ch == '-') {
                next();
            }
            while (this.ch >= '0' && this.ch <= '9') {
                next();
            }
            return Long.parseLong(this.path.substring(beginIndex, this.pos - 1));
        }

        /* access modifiers changed from: protected */
        public double readDoubleValue(long longValue) {
            int beginIndex = this.pos - 1;
            next();
            while (this.ch >= '0' && this.ch <= '9') {
                next();
            }
            return Double.parseDouble(this.path.substring(beginIndex, this.pos - 1)) + ((double) longValue);
        }

        /* access modifiers changed from: protected */
        public Object readValue() {
            skipWhitespace();
            if (isDigitFirst(this.ch)) {
                return Long.valueOf(readLongValue());
            }
            if (this.ch == '\"' || this.ch == '\'') {
                return readString();
            }
            if (this.ch != 'n') {
                throw new UnsupportedOperationException();
            } else if ("null".equals(readName())) {
                return null;
            } else {
                throw new JSONPathException(this.path);
            }
        }

        static boolean isDigitFirst(char ch2) {
            return ch2 == '-' || ch2 == '+' || (ch2 >= '0' && ch2 <= '9');
        }

        /* access modifiers changed from: protected */
        public Operator readOp() {
            Operator op = null;
            if (this.ch == '=') {
                next();
                if (this.ch == '~') {
                    next();
                    op = Operator.REG_MATCH;
                } else if (this.ch == '=') {
                    next();
                    op = Operator.EQ;
                } else {
                    op = Operator.EQ;
                }
            } else if (this.ch == '!') {
                next();
                accept('=');
                op = Operator.NE;
            } else if (this.ch == '<') {
                next();
                if (this.ch == '=') {
                    next();
                    op = Operator.LE;
                } else {
                    op = Operator.LT;
                }
            } else if (this.ch == '>') {
                next();
                if (this.ch == '=') {
                    next();
                    op = Operator.GE;
                } else {
                    op = Operator.GT;
                }
            }
            if (op != null) {
                return op;
            }
            String name = readName();
            if ("not".equalsIgnoreCase(name)) {
                skipWhitespace();
                String name2 = readName();
                if ("like".equalsIgnoreCase(name2)) {
                    return Operator.NOT_LIKE;
                }
                if ("rlike".equalsIgnoreCase(name2)) {
                    return Operator.NOT_RLIKE;
                }
                if ("in".equalsIgnoreCase(name2)) {
                    return Operator.NOT_IN;
                }
                if ("between".equalsIgnoreCase(name2)) {
                    return Operator.NOT_BETWEEN;
                }
                throw new UnsupportedOperationException();
            } else if ("nin".equalsIgnoreCase(name)) {
                return Operator.NOT_IN;
            } else {
                if ("like".equalsIgnoreCase(name)) {
                    return Operator.LIKE;
                }
                if ("rlike".equalsIgnoreCase(name)) {
                    return Operator.RLIKE;
                }
                if ("in".equalsIgnoreCase(name)) {
                    return Operator.IN;
                }
                if ("between".equalsIgnoreCase(name)) {
                    return Operator.BETWEEN;
                }
                throw new UnsupportedOperationException();
            }
        }

        /* access modifiers changed from: package-private */
        public String readName() {
            skipWhitespace();
            if (this.ch == '\\' || Character.isJavaIdentifierStart(this.ch)) {
                StringBuilder buf = new StringBuilder();
                while (!isEOF()) {
                    if (this.ch == '\\') {
                        next();
                        buf.append(this.ch);
                        if (isEOF()) {
                            return buf.toString();
                        }
                        next();
                    } else if (!Character.isJavaIdentifierPart(this.ch)) {
                        break;
                    } else {
                        buf.append(this.ch);
                        next();
                    }
                }
                if (isEOF() && Character.isJavaIdentifierPart(this.ch)) {
                    buf.append(this.ch);
                }
                return buf.toString();
            }
            throw new JSONPathException("illeal jsonpath syntax. " + this.path);
        }

        /* access modifiers changed from: package-private */
        public String readString() {
            char quoate = this.ch;
            next();
            int beginIndex = this.pos - 1;
            while (this.ch != quoate && !isEOF()) {
                next();
            }
            String strValue = this.path.substring(beginIndex, isEOF() ? this.pos : this.pos - 1);
            accept(quoate);
            return strValue;
        }

        /* access modifiers changed from: package-private */
        public void accept(char expect) {
            if (this.ch != expect) {
                throw new JSONPathException("expect '" + expect + ", but '" + this.ch + "'");
            } else if (!isEOF()) {
                next();
            }
        }

        public Segment[] explain() {
            if (this.path == null || this.path.length() == 0) {
                throw new IllegalArgumentException();
            }
            Segment[] segments = new Segment[8];
            while (true) {
                Segment segment = readSegement();
                if (segment == null) {
                    break;
                }
                if (segment instanceof PropertySegment) {
                    PropertySegment propertySegment = (PropertySegment) segment;
                    if (!propertySegment.deep && propertySegment.propertyName.equals("*")) {
                    }
                }
                if (this.level == segments.length) {
                    Segment[] t = new Segment[((this.level * 3) / 2)];
                    System.arraycopy(segments, 0, t, 0, this.level);
                    segments = t;
                }
                int i = this.level;
                this.level = i + 1;
                segments[i] = segment;
            }
            if (this.level == segments.length) {
                return segments;
            }
            Segment[] result = new Segment[this.level];
            System.arraycopy(segments, 0, result, 0, this.level);
            return result;
        }

        /* access modifiers changed from: package-private */
        public Segment buildArraySegement(String indexText) {
            int indexTextLen = indexText.length();
            int i = 0;
            char firstChar = indexText.charAt(0);
            char lastChar = indexText.charAt(indexTextLen - 1);
            int commaIndex = indexText.indexOf(44);
            int end = -1;
            int step = 1;
            if (indexText.length() <= 2 || firstChar != '\'' || lastChar != '\'') {
                int colonIndex = indexText.indexOf(58);
                if (commaIndex == -1 && colonIndex == -1) {
                    if (TypeUtils.isNumber(indexText)) {
                        try {
                            return new ArrayAccessSegment(Integer.parseInt(indexText));
                        } catch (NumberFormatException e) {
                            return new PropertySegment(indexText, false);
                        }
                    } else {
                        if (indexText.charAt(0) == '\"' && indexText.charAt(indexText.length() - 1) == '\"') {
                            indexText = indexText.substring(1, indexText.length() - 1);
                        }
                        return new PropertySegment(indexText, false);
                    }
                } else if (commaIndex != -1) {
                    String[] indexesText = indexText.split(",");
                    int[] indexes = new int[indexesText.length];
                    while (i < indexesText.length) {
                        indexes[i] = Integer.parseInt(indexesText[i]);
                        i++;
                    }
                    return new MultiIndexSegment(indexes);
                } else if (colonIndex != -1) {
                    String[] indexesText2 = indexText.split(":");
                    int[] indexes2 = new int[indexesText2.length];
                    for (int i2 = 0; i2 < indexesText2.length; i2++) {
                        String str = indexesText2[i2];
                        if (str.length() != 0) {
                            indexes2[i2] = Integer.parseInt(str);
                        } else if (i2 == 0) {
                            indexes2[i2] = 0;
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }
                    int start = indexes2[0];
                    if (indexes2.length > 1) {
                        end = indexes2[1];
                    }
                    if (indexes2.length == 3) {
                        step = indexes2[2];
                    }
                    if (end >= 0 && end < start) {
                        throw new UnsupportedOperationException("end must greater than or equals start. start " + start + ",  end " + end);
                    } else if (step > 0) {
                        return new RangeSegment(start, end, step);
                    } else {
                        throw new UnsupportedOperationException("step must greater than zero : " + step);
                    }
                } else {
                    throw new UnsupportedOperationException();
                }
            } else if (commaIndex == -1) {
                return new PropertySegment(indexText.substring(1, indexTextLen - 1), false);
            } else {
                String[] indexesText3 = indexText.split(",");
                String[] propertyNames = new String[indexesText3.length];
                while (i < indexesText3.length) {
                    String indexesTextItem = indexesText3[i];
                    propertyNames[i] = indexesTextItem.substring(1, indexesTextItem.length() - 1);
                    i++;
                }
                return new MultiPropertySegment(propertyNames);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class SizeSegment implements Segment {
        public static final SizeSegment instance = new SizeSegment();

        SizeSegment() {
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Integer eval(JSONPath path, Object rootObject, Object currentObject) {
            return Integer.valueOf(path.evalSize(currentObject));
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public static class MaxSegment implements Segment {
        public static final MaxSegment instance = new MaxSegment();

        MaxSegment() {
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            Object max = null;
            if (rootObject instanceof Collection) {
                for (Object next : (Collection) rootObject) {
                    if (next != null) {
                        if (max == null) {
                            max = next;
                        } else if (JSONPath.compare(max, next) < 0) {
                            max = next;
                        }
                    }
                }
                return max;
            }
            throw new UnsupportedOperationException();
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public static class MinSegment implements Segment {
        public static final MinSegment instance = new MinSegment();

        MinSegment() {
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            Object min = null;
            if (rootObject instanceof Collection) {
                for (Object next : (Collection) rootObject) {
                    if (next != null) {
                        if (min == null) {
                            min = next;
                        } else if (JSONPath.compare(min, next) > 0) {
                            min = next;
                        }
                    }
                }
                return min;
            }
            throw new UnsupportedOperationException();
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            throw new UnsupportedOperationException();
        }
    }

    static int compare(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            return ((Comparable) a).compareTo(b);
        }
        Class typeA = a.getClass();
        Class typeB = b.getClass();
        if (typeA == BigDecimal.class) {
            if (typeB == Integer.class) {
                b = new BigDecimal(((Integer) b).intValue());
            } else if (typeB == Long.class) {
                b = new BigDecimal(((Long) b).longValue());
            } else if (typeB == Float.class) {
                b = new BigDecimal((double) ((Float) b).floatValue());
            } else if (typeB == Double.class) {
                b = new BigDecimal(((Double) b).doubleValue());
            }
        } else if (typeA == Long.class) {
            if (typeB == Integer.class) {
                b = new Long((long) ((Integer) b).intValue());
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal(((Long) a).longValue());
            } else if (typeB == Float.class) {
                a = new Float((float) ((Long) a).longValue());
            } else if (typeB == Double.class) {
                a = new Double((double) ((Long) a).longValue());
            }
        } else if (typeA == Integer.class) {
            if (typeB == Long.class) {
                a = new Long((long) ((Integer) a).intValue());
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal(((Integer) a).intValue());
            } else if (typeB == Float.class) {
                a = new Float((float) ((Integer) a).intValue());
            } else if (typeB == Double.class) {
                a = new Double((double) ((Integer) a).intValue());
            }
        } else if (typeA == Double.class) {
            if (typeB == Integer.class) {
                b = new Double((double) ((Integer) b).intValue());
            } else if (typeB == Long.class) {
                b = new Double((double) ((Long) b).longValue());
            } else if (typeB == Float.class) {
                b = new Double((double) ((Float) b).floatValue());
            }
        } else if (typeA == Float.class) {
            if (typeB == Integer.class) {
                b = new Float((float) ((Integer) b).intValue());
            } else if (typeB == Long.class) {
                b = new Float((float) ((Long) b).longValue());
            } else if (typeB == Double.class) {
                a = new Double((double) ((Float) a).floatValue());
            }
        }
        return ((Comparable) a).compareTo(b);
    }

    /* access modifiers changed from: package-private */
    public static class KeySetSegment implements Segment {
        public static final KeySetSegment instance = new KeySetSegment();

        KeySetSegment() {
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            return path.evalKeySet(currentObject);
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public static class PropertySegment implements Segment {
        private final boolean deep;
        private final String propertyName;
        private final long propertyNameHash;

        public PropertySegment(String propertyName2, boolean deep2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.deep = deep2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            if (!this.deep) {
                return path.getPropertyValue(currentObject, this.propertyName, this.propertyNameHash);
            }
            List<Object> results = new ArrayList<>();
            path.deepScan(currentObject, this.propertyName, results);
            return results;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            Object value;
            Object value2;
            JSONArray array;
            Object value3;
            JSONLexerBase lexer = (JSONLexerBase) parser.lexer;
            if (this.deep && context.object == null) {
                context.object = new JSONArray();
            }
            if (lexer.token() == 14) {
                if (!"*".equals(this.propertyName)) {
                    lexer.nextToken();
                    if (this.deep) {
                        array = (JSONArray) context.object;
                    } else {
                        array = new JSONArray();
                    }
                    while (true) {
                        int i = lexer.token();
                        if (i != 12) {
                            if (i != 14) {
                                switch (i) {
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case JSONToken.TRUE /* 6 */:
                                    case JSONToken.FALSE /* 7 */:
                                    case JSONToken.NULL /* 8 */:
                                        lexer.nextToken();
                                        break;
                                }
                            } else if (this.deep) {
                                extract(path, parser, context);
                            } else {
                                lexer.skipObject(false);
                            }
                        } else if (this.deep) {
                            extract(path, parser, context);
                        } else {
                            int matchStat = lexer.seekObjectToField(this.propertyNameHash, this.deep);
                            if (matchStat == 3) {
                                int i2 = lexer.token();
                                if (i2 == 2) {
                                    value3 = lexer.integerValue();
                                    lexer.nextToken();
                                } else if (i2 != 4) {
                                    value3 = parser.parse();
                                } else {
                                    value3 = lexer.stringVal();
                                    lexer.nextToken();
                                }
                                array.add(value3);
                                if (lexer.token() == 13) {
                                    lexer.nextToken();
                                } else {
                                    lexer.skipObject(false);
                                }
                            } else if (matchStat == -1) {
                                continue;
                            } else if (!this.deep) {
                                lexer.skipObject(false);
                            } else {
                                throw new UnsupportedOperationException(lexer.info());
                            }
                        }
                        if (lexer.token() == 15) {
                            lexer.nextToken();
                            if (!this.deep && array.size() > 0) {
                                context.object = array;
                                return;
                            }
                            return;
                        } else if (lexer.token() == 16) {
                            lexer.nextToken();
                        } else {
                            throw new JSONException("illegal json : " + lexer.info());
                        }
                    }
                }
            } else if (this.deep) {
                while (true) {
                    int matchStat2 = lexer.seekObjectToField(this.propertyNameHash, this.deep);
                    if (matchStat2 != -1) {
                        if (matchStat2 == 3) {
                            if (context.eval) {
                                switch (lexer.token()) {
                                    case 2:
                                        value = lexer.integerValue();
                                        lexer.nextToken(16);
                                        break;
                                    case 3:
                                        value = lexer.decimalValue();
                                        lexer.nextToken(16);
                                        break;
                                    case 4:
                                        value = lexer.stringVal();
                                        lexer.nextToken(16);
                                        break;
                                    default:
                                        value = parser.parse();
                                        break;
                                }
                                if (context.eval) {
                                    if (context.object instanceof List) {
                                        List list = (List) context.object;
                                        if (list.size() != 0 || !(value instanceof List)) {
                                            list.add(value);
                                        } else {
                                            context.object = value;
                                        }
                                    } else {
                                        context.object = value;
                                    }
                                }
                            }
                        } else if (matchStat2 == 1 || matchStat2 == 2) {
                            extract(path, parser, context);
                        }
                    } else {
                        return;
                    }
                }
            } else if (lexer.seekObjectToField(this.propertyNameHash, this.deep) == 3 && context.eval) {
                switch (lexer.token()) {
                    case 2:
                        value2 = lexer.integerValue();
                        lexer.nextToken(16);
                        break;
                    case 3:
                        value2 = lexer.decimalValue();
                        lexer.nextToken(16);
                        break;
                    case 4:
                        value2 = lexer.stringVal();
                        lexer.nextToken(16);
                        break;
                    default:
                        value2 = parser.parse();
                        break;
                }
                if (context.eval) {
                    context.object = value2;
                }
            }
        }

        public void setValue(JSONPath path, Object parent, Object value) {
            if (this.deep) {
                path.deepSet(parent, this.propertyName, this.propertyNameHash, value);
            } else {
                path.setPropertyValue(parent, this.propertyName, this.propertyNameHash, value);
            }
        }

        public boolean remove(JSONPath path, Object parent) {
            return path.removePropertyValue(parent, this.propertyName);
        }
    }

    /* access modifiers changed from: package-private */
    public static class MultiPropertySegment implements Segment {
        private final String[] propertyNames;
        private final long[] propertyNamesHash;

        public MultiPropertySegment(String[] propertyNames2) {
            this.propertyNames = propertyNames2;
            this.propertyNamesHash = new long[propertyNames2.length];
            for (int i = 0; i < this.propertyNamesHash.length; i++) {
                this.propertyNamesHash[i] = TypeUtils.fnv1a_64(propertyNames2[i]);
            }
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            List<Object> fieldValues = new ArrayList<>(this.propertyNames.length);
            for (int i = 0; i < this.propertyNames.length; i++) {
                fieldValues.add(path.getPropertyValue(currentObject, this.propertyNames[i], this.propertyNamesHash[i]));
            }
            return fieldValues;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            JSONArray array;
            Object value;
            JSONLexerBase lexer = (JSONLexerBase) parser.lexer;
            if (context.object == null) {
                JSONArray jSONArray = new JSONArray();
                array = jSONArray;
                context.object = jSONArray;
            } else {
                array = (JSONArray) context.object;
            }
            for (int i = array.size(); i < this.propertyNamesHash.length; i++) {
                array.add(null);
            }
            do {
                int index = lexer.seekObjectToField(this.propertyNamesHash);
                if (lexer.matchStat == 3) {
                    switch (lexer.token()) {
                        case 2:
                            value = lexer.integerValue();
                            lexer.nextToken(16);
                            break;
                        case 3:
                            value = lexer.decimalValue();
                            lexer.nextToken(16);
                            break;
                        case 4:
                            value = lexer.stringVal();
                            lexer.nextToken(16);
                            break;
                        default:
                            value = parser.parse();
                            break;
                    }
                    array.set(index, value);
                } else {
                    return;
                }
            } while (lexer.token() == 16);
        }
    }

    /* access modifiers changed from: package-private */
    public static class WildCardSegment implements Segment {
        public static final WildCardSegment instance = new WildCardSegment(false);
        public static final WildCardSegment instance_deep = new WildCardSegment(true);
        private boolean deep;

        private WildCardSegment(boolean deep2) {
            this.deep = deep2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            if (!this.deep) {
                return path.getPropertyValues(currentObject);
            }
            List<Object> values = new ArrayList<>();
            path.deepGetPropertyValues(currentObject, values);
            return values;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            if (context.eval) {
                Object object = parser.parse();
                if (this.deep) {
                    List<Object> values = new ArrayList<>();
                    path.deepGetPropertyValues(object, values);
                    context.object = values;
                    return;
                } else if (object instanceof JSONObject) {
                    Collection<Object> values2 = ((JSONObject) object).values();
                    JSONArray array = new JSONArray(values2.size());
                    for (Object value : values2) {
                        array.add(value);
                    }
                    context.object = array;
                    return;
                } else if (object instanceof JSONArray) {
                    context.object = object;
                    return;
                }
            }
            throw new JSONException("TODO");
        }
    }

    /* access modifiers changed from: package-private */
    public static class ArrayAccessSegment implements Segment {
        private final int index;

        public ArrayAccessSegment(int index2) {
            this.index = index2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            return path.getArrayItem(currentObject, this.index);
        }

        public boolean setValue(JSONPath path, Object currentObject, Object value) {
            return path.setArrayItem(path, currentObject, this.index, value);
        }

        public boolean remove(JSONPath path, Object currentObject) {
            return path.removeArrayItem(path, currentObject, this.index);
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            if (((JSONLexerBase) parser.lexer).seekArrayToItem(this.index) && context.eval) {
                context.object = parser.parse();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class MultiIndexSegment implements Segment {
        private final int[] indexes;

        public MultiIndexSegment(int[] indexes2) {
            this.indexes = indexes2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            List<Object> items = new JSONArray(this.indexes.length);
            for (int i = 0; i < this.indexes.length; i++) {
                items.add(path.getArrayItem(currentObject, this.indexes[i]));
            }
            return items;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            if (context.eval) {
                Object object = parser.parse();
                if (object instanceof List) {
                    int[] indexes2 = new int[this.indexes.length];
                    boolean noneNegative = false;
                    System.arraycopy(this.indexes, 0, indexes2, 0, indexes2.length);
                    if (indexes2[0] >= 0) {
                        noneNegative = true;
                    }
                    List list = (List) object;
                    if (noneNegative) {
                        for (int i = list.size() - 1; i >= 0; i--) {
                            if (Arrays.binarySearch(indexes2, i) < 0) {
                                list.remove(i);
                            }
                        }
                        context.object = list;
                        return;
                    }
                }
            }
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public static class RangeSegment implements Segment {
        private final int end;
        private final int start;
        private final int step;

        public RangeSegment(int start2, int end2, int step2) {
            this.start = start2;
            this.end = end2;
            this.step = step2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            int size = SizeSegment.instance.eval(path, rootObject, currentObject).intValue();
            int start2 = this.start >= 0 ? this.start : this.start + size;
            int end2 = this.end >= 0 ? this.end : this.end + size;
            int array_size = ((end2 - start2) / this.step) + 1;
            if (array_size == -1) {
                return null;
            }
            List<Object> items = new ArrayList<>(array_size);
            int i = start2;
            while (i <= end2 && i < size) {
                items.add(path.getArrayItem(currentObject, i));
                i += this.step;
            }
            return items;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public static class NotNullSegement implements Filter {
        private final String propertyName;
        private final long propertyNameHash;

        public NotNullSegement(String propertyName2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            return path.getPropertyValue(item, this.propertyName, this.propertyNameHash) != null;
        }
    }

    /* access modifiers changed from: package-private */
    public static class NullSegement implements Filter {
        private final String propertyName;
        private final long propertyNameHash;

        public NullSegement(String propertyName2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            return path.getPropertyValue(item, this.propertyName, this.propertyNameHash) == null;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ValueSegment implements Filter {
        private boolean eq = true;
        private final String propertyName;
        private final long propertyNameHash;
        private final Object value;

        public ValueSegment(String propertyName2, Object value2, boolean eq2) {
            if (value2 != null) {
                this.propertyName = propertyName2;
                this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
                this.value = value2;
                this.eq = eq2;
                return;
            }
            throw new IllegalArgumentException("value is null");
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            boolean result = this.value.equals(path.getPropertyValue(item, this.propertyName, this.propertyNameHash));
            if (!this.eq) {
                return !result;
            }
            return result;
        }
    }

    /* access modifiers changed from: package-private */
    public static class IntInSegement implements Filter {
        private final boolean not;
        private final String propertyName;
        private final long propertyNameHash;
        private final long[] values;

        public IntInSegement(String propertyName2, long[] values2, boolean not2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.values = values2;
            this.not = not2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            if (propertyValue instanceof Number) {
                long longPropertyValue = TypeUtils.longExtractValue((Number) propertyValue);
                for (long value : this.values) {
                    if (value == longPropertyValue) {
                        return !this.not;
                    }
                }
            }
            return this.not;
        }
    }

    /* access modifiers changed from: package-private */
    public static class IntBetweenSegement implements Filter {
        private final long endValue;
        private final boolean not;
        private final String propertyName;
        private final long propertyNameHash;
        private final long startValue;

        public IntBetweenSegement(String propertyName2, long startValue2, long endValue2, boolean not2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.startValue = startValue2;
            this.endValue = endValue2;
            this.not = not2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            if (propertyValue instanceof Number) {
                long longPropertyValue = TypeUtils.longExtractValue((Number) propertyValue);
                if (longPropertyValue >= this.startValue && longPropertyValue <= this.endValue) {
                    return !this.not;
                }
            }
            return this.not;
        }
    }

    /* access modifiers changed from: package-private */
    public static class IntObjInSegement implements Filter {
        private final boolean not;
        private final String propertyName;
        private final long propertyNameHash;
        private final Long[] values;

        public IntObjInSegement(String propertyName2, Long[] values2, boolean not2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.values = values2;
            this.not = not2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            int i = 0;
            if (propertyValue == null) {
                Long[] lArr = this.values;
                int length = lArr.length;
                while (i < length) {
                    if (lArr[i] == null) {
                        return !this.not;
                    }
                    i++;
                }
                return this.not;
            }
            if (propertyValue instanceof Number) {
                long longPropertyValue = TypeUtils.longExtractValue((Number) propertyValue);
                Long[] lArr2 = this.values;
                int length2 = lArr2.length;
                while (i < length2) {
                    Long value = lArr2[i];
                    if (value != null && value.longValue() == longPropertyValue) {
                        return !this.not;
                    }
                    i++;
                }
            }
            return this.not;
        }
    }

    /* access modifiers changed from: package-private */
    public static class StringInSegement implements Filter {
        private final boolean not;
        private final String propertyName;
        private final long propertyNameHash;
        private final String[] values;

        public StringInSegement(String propertyName2, String[] values2, boolean not2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.values = values2;
            this.not = not2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            String[] strArr = this.values;
            for (String value : strArr) {
                if (value == propertyValue) {
                    return !this.not;
                }
                if (value != null && value.equals(propertyValue)) {
                    return !this.not;
                }
            }
            return this.not;
        }
    }

    /* access modifiers changed from: package-private */
    public static class IntOpSegement implements Filter {
        private final Operator op;
        private final String propertyName;
        private final long propertyNameHash;
        private final long value;
        private BigDecimal valueDecimal;
        private Double valueDouble;
        private Float valueFloat;

        public IntOpSegement(String propertyName2, long value2, Operator op2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.value = value2;
            this.op = op2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null || !(propertyValue instanceof Number)) {
                return false;
            }
            if (propertyValue instanceof BigDecimal) {
                if (this.valueDecimal == null) {
                    this.valueDecimal = BigDecimal.valueOf(this.value);
                }
                int result = this.valueDecimal.compareTo((BigDecimal) propertyValue);
                switch (AnonymousClass1.$SwitchMap$com$alibaba$fastjson$JSONPath$Operator[this.op.ordinal()]) {
                    case 1:
                        if (result == 0) {
                            return true;
                        }
                        return false;
                    case 2:
                        if (result != 0) {
                            return true;
                        }
                        return false;
                    case 3:
                        if (result <= 0) {
                            return true;
                        }
                        return false;
                    case 4:
                        if (result < 0) {
                            return true;
                        }
                        return false;
                    case 5:
                        if (result >= 0) {
                            return true;
                        }
                        return false;
                    case JSONToken.TRUE /* 6 */:
                        if (result > 0) {
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            } else if (propertyValue instanceof Float) {
                if (this.valueFloat == null) {
                    this.valueFloat = Float.valueOf((float) this.value);
                }
                int result2 = this.valueFloat.compareTo((Float) propertyValue);
                switch (AnonymousClass1.$SwitchMap$com$alibaba$fastjson$JSONPath$Operator[this.op.ordinal()]) {
                    case 1:
                        if (result2 == 0) {
                            return true;
                        }
                        return false;
                    case 2:
                        if (result2 != 0) {
                            return true;
                        }
                        return false;
                    case 3:
                        if (result2 <= 0) {
                            return true;
                        }
                        return false;
                    case 4:
                        if (result2 < 0) {
                            return true;
                        }
                        return false;
                    case 5:
                        if (result2 >= 0) {
                            return true;
                        }
                        return false;
                    case JSONToken.TRUE /* 6 */:
                        if (result2 > 0) {
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            } else if (propertyValue instanceof Double) {
                if (this.valueDouble == null) {
                    this.valueDouble = Double.valueOf((double) this.value);
                }
                int result3 = this.valueDouble.compareTo((Double) propertyValue);
                switch (AnonymousClass1.$SwitchMap$com$alibaba$fastjson$JSONPath$Operator[this.op.ordinal()]) {
                    case 1:
                        if (result3 == 0) {
                            return true;
                        }
                        return false;
                    case 2:
                        if (result3 != 0) {
                            return true;
                        }
                        return false;
                    case 3:
                        if (result3 <= 0) {
                            return true;
                        }
                        return false;
                    case 4:
                        if (result3 < 0) {
                            return true;
                        }
                        return false;
                    case 5:
                        if (result3 >= 0) {
                            return true;
                        }
                        return false;
                    case JSONToken.TRUE /* 6 */:
                        if (result3 > 0) {
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            } else {
                long longValue = TypeUtils.longExtractValue((Number) propertyValue);
                switch (AnonymousClass1.$SwitchMap$com$alibaba$fastjson$JSONPath$Operator[this.op.ordinal()]) {
                    case 1:
                        if (longValue == this.value) {
                            return true;
                        }
                        return false;
                    case 2:
                        if (longValue != this.value) {
                            return true;
                        }
                        return false;
                    case 3:
                        if (longValue >= this.value) {
                            return true;
                        }
                        return false;
                    case 4:
                        if (longValue > this.value) {
                            return true;
                        }
                        return false;
                    case 5:
                        if (longValue <= this.value) {
                            return true;
                        }
                        return false;
                    case JSONToken.TRUE /* 6 */:
                        if (longValue < this.value) {
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            }
        }
    }

    /* renamed from: com.alibaba.fastjson.JSONPath$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$alibaba$fastjson$JSONPath$Operator = new int[Operator.values().length];

        static {
            try {
                $SwitchMap$com$alibaba$fastjson$JSONPath$Operator[Operator.EQ.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$alibaba$fastjson$JSONPath$Operator[Operator.NE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$alibaba$fastjson$JSONPath$Operator[Operator.GE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$alibaba$fastjson$JSONPath$Operator[Operator.GT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$alibaba$fastjson$JSONPath$Operator[Operator.LE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$alibaba$fastjson$JSONPath$Operator[Operator.LT.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class DoubleOpSegement implements Filter {
        private final Operator op;
        private final String propertyName;
        private final long propertyNameHash;
        private final double value;

        public DoubleOpSegement(String propertyName2, double value2, Operator op2) {
            this.propertyName = propertyName2;
            this.value = value2;
            this.op = op2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null || !(propertyValue instanceof Number)) {
                return false;
            }
            double doubleValue = ((Number) propertyValue).doubleValue();
            switch (AnonymousClass1.$SwitchMap$com$alibaba$fastjson$JSONPath$Operator[this.op.ordinal()]) {
                case 1:
                    if (doubleValue == this.value) {
                        return true;
                    }
                    return false;
                case 2:
                    if (doubleValue != this.value) {
                        return true;
                    }
                    return false;
                case 3:
                    if (doubleValue >= this.value) {
                        return true;
                    }
                    return false;
                case 4:
                    if (doubleValue > this.value) {
                        return true;
                    }
                    return false;
                case 5:
                    if (doubleValue <= this.value) {
                        return true;
                    }
                    return false;
                case JSONToken.TRUE /* 6 */:
                    if (doubleValue < this.value) {
                        return true;
                    }
                    return false;
                default:
                    return false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class RefOpSegement implements Filter {
        private final Operator op;
        private final String propertyName;
        private final long propertyNameHash;
        private final Segment refSgement;

        public RefOpSegement(String propertyName2, Segment refSgement2, Operator op2) {
            this.propertyName = propertyName2;
            this.refSgement = refSgement2;
            this.op = op2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null || !(propertyValue instanceof Number)) {
                return false;
            }
            Object refValue = this.refSgement.eval(path, rootObject, rootObject);
            if ((refValue instanceof Integer) || (refValue instanceof Long) || (refValue instanceof Short) || (refValue instanceof Byte)) {
                long value = TypeUtils.longExtractValue((Number) refValue);
                if ((propertyValue instanceof Integer) || (propertyValue instanceof Long) || (propertyValue instanceof Short) || (propertyValue instanceof Byte)) {
                    long longValue = TypeUtils.longExtractValue((Number) propertyValue);
                    switch (AnonymousClass1.$SwitchMap$com$alibaba$fastjson$JSONPath$Operator[this.op.ordinal()]) {
                        case 1:
                            if (longValue == value) {
                                return true;
                            }
                            return false;
                        case 2:
                            if (longValue != value) {
                                return true;
                            }
                            return false;
                        case 3:
                            if (longValue >= value) {
                                return true;
                            }
                            return false;
                        case 4:
                            if (longValue > value) {
                                return true;
                            }
                            return false;
                        case 5:
                            if (longValue <= value) {
                                return true;
                            }
                            return false;
                        case JSONToken.TRUE /* 6 */:
                            if (longValue < value) {
                                return true;
                            }
                            return false;
                    }
                } else if (propertyValue instanceof BigDecimal) {
                    int result = BigDecimal.valueOf(value).compareTo((BigDecimal) propertyValue);
                    switch (AnonymousClass1.$SwitchMap$com$alibaba$fastjson$JSONPath$Operator[this.op.ordinal()]) {
                        case 1:
                            if (result == 0) {
                                return true;
                            }
                            return false;
                        case 2:
                            if (result != 0) {
                                return true;
                            }
                            return false;
                        case 3:
                            if (result <= 0) {
                                return true;
                            }
                            return false;
                        case 4:
                            if (result < 0) {
                                return true;
                            }
                            return false;
                        case 5:
                            if (result >= 0) {
                                return true;
                            }
                            return false;
                        case JSONToken.TRUE /* 6 */:
                            if (result > 0) {
                                return true;
                            }
                            return false;
                        default:
                            return false;
                    }
                }
            }
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public static class MatchSegement implements Filter {
        private final String[] containsValues;
        private final String endsWithValue;
        private final int minLength;
        private final boolean not;
        private final String propertyName;
        private final long propertyNameHash;
        private final String startsWithValue;

        public MatchSegement(String propertyName2, String startsWithValue2, String endsWithValue2, String[] containsValues2, boolean not2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.startsWithValue = startsWithValue2;
            this.endsWithValue = endsWithValue2;
            this.containsValues = containsValues2;
            this.not = not2;
            int len = startsWithValue2 != null ? 0 + startsWithValue2.length() : 0;
            len = endsWithValue2 != null ? len + endsWithValue2.length() : len;
            if (containsValues2 != null) {
                for (String item : containsValues2) {
                    len += item.length();
                }
            }
            this.minLength = len;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            String strPropertyValue = propertyValue.toString();
            if (strPropertyValue.length() < this.minLength) {
                return this.not;
            }
            int start = 0;
            if (this.startsWithValue != null) {
                if (!strPropertyValue.startsWith(this.startsWithValue)) {
                    return this.not;
                }
                start = 0 + this.startsWithValue.length();
            }
            if (this.containsValues != null) {
                String[] strArr = this.containsValues;
                for (String containsValue : strArr) {
                    int index = strPropertyValue.indexOf(containsValue, start);
                    if (index == -1) {
                        return this.not;
                    }
                    start = index + containsValue.length();
                }
            }
            if (this.endsWithValue == null || strPropertyValue.endsWith(this.endsWithValue)) {
                return !this.not;
            }
            return this.not;
        }
    }

    /* access modifiers changed from: package-private */
    public static class RlikeSegement implements Filter {
        private final boolean not;
        private final Pattern pattern;
        private final String propertyName;
        private final long propertyNameHash;

        public RlikeSegement(String propertyName2, String pattern2, boolean not2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.pattern = Pattern.compile(pattern2);
            this.not = not2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            boolean match = this.pattern.matcher(propertyValue.toString()).matches();
            if (this.not) {
                return !match;
            }
            return match;
        }
    }

    /* access modifiers changed from: package-private */
    public static class StringOpSegement implements Filter {
        private final Operator op;
        private final String propertyName;
        private final long propertyNameHash;
        private final String value;

        public StringOpSegement(String propertyName2, String value2, Operator op2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.value = value2;
            this.op = op2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (this.op == Operator.EQ) {
                return this.value.equals(propertyValue);
            }
            if (this.op == Operator.NE) {
                return !this.value.equals(propertyValue);
            }
            if (propertyValue == null) {
                return false;
            }
            int compareResult = this.value.compareTo(propertyValue.toString());
            if (this.op == Operator.GE) {
                if (compareResult <= 0) {
                    return true;
                }
                return false;
            } else if (this.op == Operator.GT) {
                if (compareResult < 0) {
                    return true;
                }
                return false;
            } else if (this.op == Operator.LE) {
                if (compareResult >= 0) {
                    return true;
                }
                return false;
            } else if (this.op != Operator.LT || compareResult <= 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class RegMatchSegement implements Filter {
        private final Operator op;
        private final Pattern pattern;
        private final String propertyName;
        private final long propertyNameHash;

        public RegMatchSegement(String propertyName2, Pattern pattern2, Operator op2) {
            this.propertyName = propertyName2;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName2);
            this.pattern = pattern2;
            this.op = op2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            return this.pattern.matcher(propertyValue.toString()).matches();
        }
    }

    public static class FilterSegment implements Segment {
        private final Filter filter;

        public FilterSegment(Filter filter2) {
            this.filter = filter2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public Object eval(JSONPath path, Object rootObject, Object currentObject) {
            if (currentObject == null) {
                return null;
            }
            List<Object> items = new JSONArray();
            if (currentObject instanceof Iterable) {
                for (Object item : (Iterable) currentObject) {
                    if (this.filter.apply(path, rootObject, currentObject, item)) {
                        items.add(item);
                    }
                }
                return items;
            } else if (this.filter.apply(path, rootObject, currentObject, currentObject)) {
                return currentObject;
            } else {
                return null;
            }
        }

        @Override // com.alibaba.fastjson.JSONPath.Segment
        public void extract(JSONPath path, DefaultJSONParser parser, Context context) {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public static class FilterGroup implements Filter {
        private boolean and;
        private List<Filter> fitlers = new ArrayList(2);

        public FilterGroup(Filter left, Filter right, boolean and2) {
            this.fitlers.add(left);
            this.fitlers.add(right);
            this.and = and2;
        }

        @Override // com.alibaba.fastjson.JSONPath.Filter
        public boolean apply(JSONPath path, Object rootObject, Object currentObject, Object item) {
            if (this.and) {
                for (Filter fitler : this.fitlers) {
                    if (!fitler.apply(path, rootObject, currentObject, item)) {
                        return false;
                    }
                }
                return true;
            }
            for (Filter fitler2 : this.fitlers) {
                if (fitler2.apply(path, rootObject, currentObject, item)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public Object getArrayItem(Object currentObject, int index) {
        if (currentObject == null) {
            return null;
        }
        if (currentObject instanceof List) {
            List list = (List) currentObject;
            if (index >= 0) {
                if (index < list.size()) {
                    return list.get(index);
                }
                return null;
            } else if (Math.abs(index) <= list.size()) {
                return list.get(list.size() + index);
            } else {
                return null;
            }
        } else if (currentObject.getClass().isArray()) {
            int arrayLenth = Array.getLength(currentObject);
            if (index >= 0) {
                if (index < arrayLenth) {
                    return Array.get(currentObject, index);
                }
                return null;
            } else if (Math.abs(index) <= arrayLenth) {
                return Array.get(currentObject, arrayLenth + index);
            } else {
                return null;
            }
        } else if (currentObject instanceof Map) {
            Map map = (Map) currentObject;
            Object value = map.get(Integer.valueOf(index));
            if (value == null) {
                return map.get(Integer.toString(index));
            }
            return value;
        } else if (currentObject instanceof Collection) {
            int i = 0;
            for (Object item : (Collection) currentObject) {
                if (i == index) {
                    return item;
                }
                i++;
            }
            return null;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean setArrayItem(JSONPath path2, Object currentObject, int index, Object value) {
        if (currentObject instanceof List) {
            List list = (List) currentObject;
            if (index >= 0) {
                list.set(index, value);
            } else {
                list.set(list.size() + index, value);
            }
            return true;
        }
        Class<?> clazz = currentObject.getClass();
        if (clazz.isArray()) {
            int arrayLenth = Array.getLength(currentObject);
            if (index >= 0) {
                if (index < arrayLenth) {
                    Array.set(currentObject, index, value);
                }
            } else if (Math.abs(index) <= arrayLenth) {
                Array.set(currentObject, arrayLenth + index, value);
            }
            return true;
        }
        throw new JSONPathException("unsupported set operation." + clazz);
    }

    public boolean removeArrayItem(JSONPath path2, Object currentObject, int index) {
        if (currentObject instanceof List) {
            List list = (List) currentObject;
            if (index < 0) {
                int newIndex = list.size() + index;
                if (newIndex < 0) {
                    return false;
                }
                list.remove(newIndex);
                return true;
            } else if (index >= list.size()) {
                return false;
            } else {
                list.remove(index);
                return true;
            }
        } else {
            Class<?> clazz = currentObject.getClass();
            throw new JSONPathException("unsupported set operation." + clazz);
        }
    }

    /* access modifiers changed from: protected */
    public Collection<Object> getPropertyValues(Object currentObject) {
        JavaBeanSerializer beanSerializer = getJavaBeanSerializer(currentObject.getClass());
        if (beanSerializer != null) {
            try {
                return beanSerializer.getFieldValues(currentObject);
            } catch (Exception e) {
                throw new JSONPathException("jsonpath error, path " + this.path, e);
            }
        } else if (currentObject instanceof Map) {
            return ((Map) currentObject).values();
        } else {
            if (currentObject instanceof Collection) {
                return (Collection) currentObject;
            }
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: protected */
    public void deepGetPropertyValues(Object currentObject, List<Object> outValues) {
        Class<?> currentClass = currentObject.getClass();
        JavaBeanSerializer beanSerializer = getJavaBeanSerializer(currentClass);
        Collection collection = null;
        if (beanSerializer != null) {
            try {
                collection = beanSerializer.getFieldValues(currentObject);
            } catch (Exception e) {
                throw new JSONPathException("jsonpath error, path " + this.path, e);
            }
        } else if (currentObject instanceof Map) {
            collection = ((Map) currentObject).values();
        } else if (currentObject instanceof Collection) {
            collection = (Collection) currentObject;
        }
        if (collection != null) {
            for (Object fieldValue : collection) {
                if (fieldValue == null || ParserConfig.isPrimitive2(fieldValue.getClass())) {
                    outValues.add(fieldValue);
                } else {
                    deepGetPropertyValues(fieldValue, outValues);
                }
            }
            return;
        }
        throw new UnsupportedOperationException(currentClass.getName());
    }

    static boolean eq(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getClass() == b.getClass()) {
            return a.equals(b);
        }
        if (!(a instanceof Number)) {
            return a.equals(b);
        }
        if (b instanceof Number) {
            return eqNotNull((Number) a, (Number) b);
        }
        return false;
    }

    static boolean eqNotNull(Number a, Number b) {
        Class clazzA = a.getClass();
        boolean isIntA = isInt(clazzA);
        Class clazzB = b.getClass();
        boolean isIntB = isInt(clazzB);
        if (a instanceof BigDecimal) {
            BigDecimal decimalA = (BigDecimal) a;
            if (isIntB) {
                return decimalA.equals(BigDecimal.valueOf(TypeUtils.longExtractValue(b)));
            }
        }
        if (isIntA) {
            if (isIntB) {
                return a.longValue() == b.longValue();
            }
            if (b instanceof BigInteger) {
                return BigInteger.valueOf(a.longValue()).equals((BigInteger) a);
            }
        }
        if (isIntB && (a instanceof BigInteger)) {
            return ((BigInteger) a).equals(BigInteger.valueOf(TypeUtils.longExtractValue(b)));
        }
        boolean isDoubleA = isDouble(clazzA);
        boolean isDoubleB = isDouble(clazzB);
        if ((!isDoubleA || !isDoubleB) && ((!isDoubleA || !isIntB) && (!isDoubleB || !isIntA))) {
            return false;
        }
        return a.doubleValue() == b.doubleValue();
    }

    protected static boolean isDouble(Class<?> clazzA) {
        return clazzA == Float.class || clazzA == Double.class;
    }

    protected static boolean isInt(Class<?> clazzA) {
        return clazzA == Byte.class || clazzA == Short.class || clazzA == Integer.class || clazzA == Long.class;
    }

    /* access modifiers changed from: protected */
    public Object getPropertyValue(Object currentObject, String propertyName, long propertyNameHash) {
        if (currentObject == null) {
            return null;
        }
        if (currentObject instanceof Map) {
            Map map = (Map) currentObject;
            Object val = map.get(propertyName);
            if (val != null) {
                return val;
            }
            if (SIZE == propertyNameHash || LENGTH == propertyNameHash) {
                return Integer.valueOf(map.size());
            }
            return val;
        }
        JavaBeanSerializer beanSerializer = getJavaBeanSerializer(currentObject.getClass());
        if (beanSerializer != null) {
            try {
                return beanSerializer.getFieldValue(currentObject, propertyName, propertyNameHash, false);
            } catch (Exception e) {
                throw new JSONPathException("jsonpath error, path " + this.path + ", segement " + propertyName, e);
            }
        } else {
            int i = 0;
            if (currentObject instanceof List) {
                List list = (List) currentObject;
                if (SIZE == propertyNameHash || LENGTH == propertyNameHash) {
                    return Integer.valueOf(list.size());
                }
                List<Object> fieldValues = null;
                while (i < list.size()) {
                    Object obj = list.get(i);
                    if (obj == list) {
                        if (fieldValues == null) {
                            fieldValues = new JSONArray(list.size());
                        }
                        fieldValues.add(obj);
                    } else {
                        Object itemValue = getPropertyValue(obj, propertyName, propertyNameHash);
                        if (itemValue instanceof Collection) {
                            Collection collection = (Collection) itemValue;
                            if (fieldValues == null) {
                                fieldValues = new JSONArray(list.size());
                            }
                            fieldValues.addAll(collection);
                        } else if (itemValue != null) {
                            if (fieldValues == null) {
                                fieldValues = new JSONArray(list.size());
                            }
                            fieldValues.add(itemValue);
                        }
                    }
                    i++;
                }
                if (fieldValues == null) {
                    return Collections.emptyList();
                }
                return fieldValues;
            } else if (currentObject instanceof Object[]) {
                Object[] array = (Object[]) currentObject;
                if (SIZE == propertyNameHash || LENGTH == propertyNameHash) {
                    return Integer.valueOf(array.length);
                }
                List<Object> fieldValues2 = new JSONArray(array.length);
                while (i < array.length) {
                    Object obj2 = array[i];
                    if (obj2 == array) {
                        fieldValues2.add(obj2);
                    } else {
                        Object itemValue2 = getPropertyValue(obj2, propertyName, propertyNameHash);
                        if (itemValue2 instanceof Collection) {
                            fieldValues2.addAll((Collection) itemValue2);
                        } else if (itemValue2 != null) {
                            fieldValues2.add(itemValue2);
                        }
                    }
                    i++;
                }
                return fieldValues2;
            } else {
                if (currentObject instanceof Enum) {
                    Enum e2 = (Enum) currentObject;
                    if (-4270347329889690746L == propertyNameHash) {
                        return e2.name();
                    }
                    if (-1014497654951707614L == propertyNameHash) {
                        return Integer.valueOf(e2.ordinal());
                    }
                }
                if (!(currentObject instanceof Calendar)) {
                    return null;
                }
                Calendar e3 = (Calendar) currentObject;
                if (8963398325558730460L == propertyNameHash) {
                    return Integer.valueOf(e3.get(1));
                }
                if (-811277319855450459L == propertyNameHash) {
                    return Integer.valueOf(e3.get(2));
                }
                if (-3851359326990528739L == propertyNameHash) {
                    return Integer.valueOf(e3.get(5));
                }
                if (4647432019745535567L == propertyNameHash) {
                    return Integer.valueOf(e3.get(11));
                }
                if (6607618197526598121L == propertyNameHash) {
                    return Integer.valueOf(e3.get(12));
                }
                if (-6586085717218287427L == propertyNameHash) {
                    return Integer.valueOf(e3.get(13));
                }
                return null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void deepScan(Object currentObject, String propertyName, List<Object> results) {
        if (currentObject != null) {
            if (currentObject instanceof Map) {
                for (Map.Entry entry : ((Map) currentObject).entrySet()) {
                    Object val = entry.getValue();
                    if (propertyName.equals(entry.getKey())) {
                        if (val instanceof Collection) {
                            results.addAll((Collection) val);
                        } else {
                            results.add(val);
                        }
                    } else if (val != null && !ParserConfig.isPrimitive2(val.getClass())) {
                        deepScan(val, propertyName, results);
                    }
                }
            } else if (currentObject instanceof Collection) {
                for (Object next : (Collection) currentObject) {
                    if (!ParserConfig.isPrimitive2(next.getClass())) {
                        deepScan(next, propertyName, results);
                    }
                }
            } else {
                JavaBeanSerializer beanSerializer = getJavaBeanSerializer(currentObject.getClass());
                if (beanSerializer != null) {
                    try {
                        FieldSerializer fieldDeser = beanSerializer.getFieldSerializer(propertyName);
                        if (fieldDeser != null) {
                            try {
                                results.add(fieldDeser.getPropertyValueDirect(currentObject));
                            } catch (InvocationTargetException ex) {
                                throw new JSONException("getFieldValue error." + propertyName, ex);
                            } catch (IllegalAccessException ex2) {
                                throw new JSONException("getFieldValue error." + propertyName, ex2);
                            }
                        } else {
                            for (Object val2 : beanSerializer.getFieldValues(currentObject)) {
                                deepScan(val2, propertyName, results);
                            }
                        }
                    } catch (Exception e) {
                        throw new JSONPathException("jsonpath error, path " + this.path + ", segement " + propertyName, e);
                    }
                } else if (currentObject instanceof List) {
                    List list = (List) currentObject;
                    for (int i = 0; i < list.size(); i++) {
                        deepScan(list.get(i), propertyName, results);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void deepSet(Object currentObject, String propertyName, long propertyNameHash, Object value) {
        if (currentObject != null) {
            if (currentObject instanceof Map) {
                Map map = (Map) currentObject;
                if (map.containsKey(propertyName)) {
                    map.get(propertyName);
                    map.put(propertyName, value);
                    return;
                }
                for (Object val : map.values()) {
                    deepSet(val, propertyName, propertyNameHash, value);
                }
                return;
            }
            Class<?> currentClass = currentObject.getClass();
            JavaBeanDeserializer beanDeserializer = getJavaBeanDeserializer(currentClass);
            if (beanDeserializer != null) {
                try {
                    FieldDeserializer fieldDeser = beanDeserializer.getFieldDeserializer(propertyName);
                    if (fieldDeser != null) {
                        fieldDeser.setValue(currentObject, value);
                        return;
                    }
                    for (Object val2 : getJavaBeanSerializer(currentClass).getObjectFieldValues(currentObject)) {
                        deepSet(val2, propertyName, propertyNameHash, value);
                    }
                } catch (Exception e) {
                    throw new JSONPathException("jsonpath error, path " + this.path + ", segement " + propertyName, e);
                }
            } else if (currentObject instanceof List) {
                List list = (List) currentObject;
                for (int i = 0; i < list.size(); i++) {
                    deepSet(list.get(i), propertyName, propertyNameHash, value);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean setPropertyValue(Object parent, String name, long propertyNameHash, Object value) {
        if (parent instanceof Map) {
            ((Map) parent).put(name, value);
            return true;
        } else if (parent instanceof List) {
            for (Object element : (List) parent) {
                if (element != null) {
                    setPropertyValue(element, name, propertyNameHash, value);
                }
            }
            return true;
        } else {
            ObjectDeserializer derializer = this.parserConfig.getDeserializer(parent.getClass());
            JavaBeanDeserializer beanDerializer = null;
            if (derializer instanceof JavaBeanDeserializer) {
                beanDerializer = (JavaBeanDeserializer) derializer;
            }
            if (beanDerializer != null) {
                FieldDeserializer fieldDeserializer = beanDerializer.getFieldDeserializer(propertyNameHash);
                if (fieldDeserializer == null) {
                    return false;
                }
                fieldDeserializer.setValue(parent, value);
                return true;
            }
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: protected */
    public boolean removePropertyValue(Object parent, String name) {
        if (parent instanceof Map) {
            return ((Map) parent).remove(name) != null;
        }
        ObjectDeserializer derializer = this.parserConfig.getDeserializer(parent.getClass());
        JavaBeanDeserializer beanDerializer = null;
        if (derializer instanceof JavaBeanDeserializer) {
            beanDerializer = (JavaBeanDeserializer) derializer;
        }
        if (beanDerializer != null) {
            FieldDeserializer fieldDeserializer = beanDerializer.getFieldDeserializer(name);
            if (fieldDeserializer == null) {
                return false;
            }
            fieldDeserializer.setValue(parent, (String) null);
            return true;
        }
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public JavaBeanSerializer getJavaBeanSerializer(Class<?> currentClass) {
        ObjectSerializer serializer = this.serializeConfig.getObjectWriter(currentClass);
        if (serializer instanceof JavaBeanSerializer) {
            return (JavaBeanSerializer) serializer;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public JavaBeanDeserializer getJavaBeanDeserializer(Class<?> currentClass) {
        ObjectDeserializer deserializer = this.parserConfig.getDeserializer(currentClass);
        if (deserializer instanceof JavaBeanDeserializer) {
            return (JavaBeanDeserializer) deserializer;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int evalSize(Object currentObject) {
        if (currentObject == null) {
            return -1;
        }
        if (currentObject instanceof Collection) {
            return ((Collection) currentObject).size();
        }
        if (currentObject instanceof Object[]) {
            return ((Object[]) currentObject).length;
        }
        if (currentObject.getClass().isArray()) {
            return Array.getLength(currentObject);
        }
        if (currentObject instanceof Map) {
            int count = 0;
            for (Object value : ((Map) currentObject).values()) {
                if (value != null) {
                    count++;
                }
            }
            return count;
        }
        JavaBeanSerializer beanSerializer = getJavaBeanSerializer(currentObject.getClass());
        if (beanSerializer == null) {
            return -1;
        }
        try {
            return beanSerializer.getSize(currentObject);
        } catch (Exception e) {
            throw new JSONPathException("evalSize error : " + this.path, e);
        }
    }

    /* access modifiers changed from: package-private */
    public Set<?> evalKeySet(Object currentObject) {
        JavaBeanSerializer beanSerializer;
        if (currentObject == null) {
            return null;
        }
        if (currentObject instanceof Map) {
            return ((Map) currentObject).keySet();
        }
        if ((currentObject instanceof Collection) || (currentObject instanceof Object[]) || currentObject.getClass().isArray() || (beanSerializer = getJavaBeanSerializer(currentObject.getClass())) == null) {
            return null;
        }
        try {
            return beanSerializer.getFieldNames(currentObject);
        } catch (Exception e) {
            throw new JSONPathException("evalKeySet error : " + this.path, e);
        }
    }

    @Override // com.alibaba.fastjson.JSONAware
    public String toJSONString() {
        return JSON.toJSONString(this.path);
    }
}
