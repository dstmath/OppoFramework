package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSONObject extends JSON implements Serializable, Cloneable, InvocationHandler, Map<String, Object> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final long serialVersionUID = 1;
    private final Map<String, Object> map;

    public JSONObject() {
        this(16, false);
    }

    public JSONObject(Map<String, Object> map2) {
        if (map2 != null) {
            this.map = map2;
            return;
        }
        throw new IllegalArgumentException("map is null.");
    }

    public JSONObject(boolean ordered) {
        this(16, ordered);
    }

    public JSONObject(int initialCapacity) {
        this(initialCapacity, false);
    }

    public JSONObject(int initialCapacity, boolean ordered) {
        if (ordered) {
            this.map = new LinkedHashMap(initialCapacity);
        } else {
            this.map = new HashMap(initialCapacity);
        }
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override // java.util.Map
    public Object get(Object key) {
        Object val = this.map.get(key);
        if (val != null || !(key instanceof Number)) {
            return val;
        }
        return this.map.get(key.toString());
    }

    public JSONObject getJSONObject(String key) {
        Object value = this.map.get(key);
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        if (value instanceof Map) {
            return new JSONObject((Map) value);
        }
        if (value instanceof String) {
            return JSON.parseObject((String) value);
        }
        return (JSONObject) toJSON(value);
    }

    public JSONArray getJSONArray(String key) {
        Object value = this.map.get(key);
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        if (value instanceof List) {
            return new JSONArray((List) value);
        }
        if (value instanceof String) {
            return (JSONArray) JSON.parse((String) value);
        }
        return (JSONArray) toJSON(value);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        return (T) TypeUtils.castToJavaBean(this.map.get(key), clazz);
    }

    public <T> T getObject(String key, Type type) {
        return (T) TypeUtils.cast(this.map.get(key), type, ParserConfig.getGlobalInstance());
    }

    public <T> T getObject(String key, TypeReference typeReference) {
        T t = (T) this.map.get(key);
        return typeReference == null ? t : (T) TypeUtils.cast(t, typeReference.getType(), ParserConfig.getGlobalInstance());
    }

    public Boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return TypeUtils.castToBoolean(value);
    }

    public byte[] getBytes(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return TypeUtils.castToBytes(value);
    }

    public boolean getBooleanValue(String key) {
        Boolean booleanVal = TypeUtils.castToBoolean(get(key));
        if (booleanVal == null) {
            return false;
        }
        return booleanVal.booleanValue();
    }

    public Byte getByte(String key) {
        return TypeUtils.castToByte(get(key));
    }

    public byte getByteValue(String key) {
        Byte byteVal = TypeUtils.castToByte(get(key));
        if (byteVal == null) {
            return 0;
        }
        return byteVal.byteValue();
    }

    public Short getShort(String key) {
        return TypeUtils.castToShort(get(key));
    }

    public short getShortValue(String key) {
        Short shortVal = TypeUtils.castToShort(get(key));
        if (shortVal == null) {
            return 0;
        }
        return shortVal.shortValue();
    }

    public Integer getInteger(String key) {
        return TypeUtils.castToInt(get(key));
    }

    public int getIntValue(String key) {
        Integer intVal = TypeUtils.castToInt(get(key));
        if (intVal == null) {
            return 0;
        }
        return intVal.intValue();
    }

    public Long getLong(String key) {
        return TypeUtils.castToLong(get(key));
    }

    public long getLongValue(String key) {
        Long longVal = TypeUtils.castToLong(get(key));
        if (longVal == null) {
            return 0;
        }
        return longVal.longValue();
    }

    public Float getFloat(String key) {
        return TypeUtils.castToFloat(get(key));
    }

    public float getFloatValue(String key) {
        Float floatValue = TypeUtils.castToFloat(get(key));
        if (floatValue == null) {
            return 0.0f;
        }
        return floatValue.floatValue();
    }

    public Double getDouble(String key) {
        return TypeUtils.castToDouble(get(key));
    }

    public double getDoubleValue(String key) {
        Double doubleValue = TypeUtils.castToDouble(get(key));
        if (doubleValue == null) {
            return 0.0d;
        }
        return doubleValue.doubleValue();
    }

    public BigDecimal getBigDecimal(String key) {
        return TypeUtils.castToBigDecimal(get(key));
    }

    public BigInteger getBigInteger(String key) {
        return TypeUtils.castToBigInteger(get(key));
    }

    public String getString(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public Date getDate(String key) {
        return TypeUtils.castToDate(get(key));
    }

    public java.sql.Date getSqlDate(String key) {
        return TypeUtils.castToSqlDate(get(key));
    }

    public Timestamp getTimestamp(String key) {
        return TypeUtils.castToTimestamp(get(key));
    }

    public Object put(String key, Object value) {
        return this.map.put(key, value);
    }

    public JSONObject fluentPut(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    @Override // java.util.Map
    public void putAll(Map<? extends String, ? extends Object> m) {
        this.map.putAll(m);
    }

    public JSONObject fluentPutAll(Map<? extends String, ? extends Object> m) {
        this.map.putAll(m);
        return this;
    }

    public void clear() {
        this.map.clear();
    }

    public JSONObject fluentClear() {
        this.map.clear();
        return this;
    }

    @Override // java.util.Map
    public Object remove(Object key) {
        return this.map.remove(key);
    }

    public JSONObject fluentRemove(Object key) {
        this.map.remove(key);
        return this;
    }

    @Override // java.util.Map
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override // java.util.Map
    public Collection<Object> values() {
        return this.map.values();
    }

    @Override // java.util.Map
    public Set<Map.Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    @Override // java.lang.Object
    public Object clone() {
        return new JSONObject(this.map instanceof LinkedHashMap ? new LinkedHashMap(this.map) : new HashMap(this.map));
    }

    public boolean equals(Object obj) {
        return this.map.equals(obj);
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    @Override // java.lang.reflect.InvocationHandler
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            if (method.getName().equals("equals")) {
                return Boolean.valueOf(equals(args[0]));
            }
            if (method.getReturnType() == Void.TYPE) {
                String name = null;
                JSONField annotation = (JSONField) method.getAnnotation(JSONField.class);
                if (!(annotation == null || annotation.name().length() == 0)) {
                    name = annotation.name();
                }
                if (name == null) {
                    String name2 = method.getName();
                    if (name2.startsWith("set")) {
                        String name3 = name2.substring(3);
                        if (name3.length() != 0) {
                            name = Character.toLowerCase(name3.charAt(0)) + name3.substring(1);
                        } else {
                            throw new JSONException("illegal setter");
                        }
                    } else {
                        throw new JSONException("illegal setter");
                    }
                }
                this.map.put(name, args[0]);
                return null;
            }
            throw new JSONException("illegal setter");
        } else if (parameterTypes.length != 0) {
            throw new UnsupportedOperationException(method.toGenericString());
        } else if (method.getReturnType() != Void.TYPE) {
            String name4 = null;
            JSONField annotation2 = (JSONField) method.getAnnotation(JSONField.class);
            if (!(annotation2 == null || annotation2.name().length() == 0)) {
                name4 = annotation2.name();
            }
            if (name4 == null) {
                String name5 = method.getName();
                if (name5.startsWith("get")) {
                    String name6 = name5.substring(3);
                    if (name6.length() != 0) {
                        name4 = Character.toLowerCase(name6.charAt(0)) + name6.substring(1);
                    } else {
                        throw new JSONException("illegal getter");
                    }
                } else if (name5.startsWith("is")) {
                    String name7 = name5.substring(2);
                    if (name7.length() != 0) {
                        name4 = Character.toLowerCase(name7.charAt(0)) + name7.substring(1);
                    } else {
                        throw new JSONException("illegal getter");
                    }
                } else if (name5.startsWith("hashCode")) {
                    return Integer.valueOf(hashCode());
                } else {
                    if (name5.startsWith("toString")) {
                        return toString();
                    }
                    throw new JSONException("illegal getter");
                }
            }
            return TypeUtils.cast(this.map.get(name4), method.getGenericReturnType(), ParserConfig.getGlobalInstance());
        } else {
            throw new JSONException("illegal getter");
        }
    }

    public Map<String, Object> getInnerMap() {
        return this.map;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        SecureObjectInputStream.ensureFields();
        if (SecureObjectInputStream.fields != null && !SecureObjectInputStream.fields_error) {
            try {
                new SecureObjectInputStream(in).defaultReadObject();
                return;
            } catch (NotActiveException e) {
            }
        }
        in.defaultReadObject();
        for (Map.Entry entry : this.map.entrySet()) {
            Object key = entry.getKey();
            if (key != null) {
                ParserConfig.global.checkAutoType(key.getClass());
            }
            Object value = entry.getValue();
            if (value != null) {
                ParserConfig.global.checkAutoType(value.getClass());
            }
        }
    }

    static class SecureObjectInputStream extends ObjectInputStream {
        static Field[] fields;
        static volatile boolean fields_error;

        static void ensureFields() {
            if (fields == null && !fields_error) {
                try {
                    Field[] declaredFields = ObjectInputStream.class.getDeclaredFields();
                    String[] fieldnames = {"bin", "passHandle", "handles", "curContext"};
                    Field[] array = new Field[fieldnames.length];
                    for (int i = 0; i < fieldnames.length; i++) {
                        Field field = TypeUtils.getField(ObjectInputStream.class, fieldnames[i], declaredFields);
                        field.setAccessible(true);
                        array[i] = field;
                    }
                    fields = array;
                } catch (Throwable th) {
                    fields_error = true;
                }
            }
        }

        public SecureObjectInputStream(ObjectInputStream in) throws IOException {
            super(in);
            for (int i = 0; i < fields.length; i++) {
                try {
                    Field field = fields[i];
                    field.set(this, field.get(in));
                } catch (IllegalAccessException e) {
                    fields_error = true;
                    return;
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // java.io.ObjectInputStream
        public Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            if (name.length() > 2) {
                int index = name.lastIndexOf(91);
                if (index != -1) {
                    name = name.substring(index + 1);
                }
                if (name.length() > 2 && name.charAt(0) == 'L' && name.charAt(name.length() - 1) == ';') {
                    name = name.substring(1, name.length() - 1);
                }
                ParserConfig.global.checkAutoType(name, null, Feature.SupportAutoType.mask);
            }
            return super.resolveClass(desc);
        }

        /* access modifiers changed from: protected */
        @Override // java.io.ObjectInputStream
        public Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
            for (String interfacename : interfaces) {
                ParserConfig.global.checkAutoType(interfacename, null);
            }
            return super.resolveProxyClass(interfaces);
        }

        /* access modifiers changed from: protected */
        @Override // java.io.ObjectInputStream
        public void readStreamHeader() throws IOException, StreamCorruptedException {
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.alibaba.fastjson.JSONObject */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.alibaba.fastjson.JSON
    public <T> T toJavaObject(Class<T> clazz) {
        if (clazz == Map.class) {
            return this;
        }
        return (clazz != Object.class || containsKey(JSON.DEFAULT_TYPE_KEY)) ? (T) TypeUtils.castToJavaBean(this, clazz, ParserConfig.getGlobalInstance()) : this;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.alibaba.fastjson.JSONObject */
    /* JADX WARN: Multi-variable type inference failed */
    public <T> T toJavaObject(Class<T> clazz, ParserConfig config, int features) {
        if (clazz == Map.class) {
            return this;
        }
        return (clazz != Object.class || containsKey(JSON.DEFAULT_TYPE_KEY)) ? (T) TypeUtils.castToJavaBean(this, clazz, config) : this;
    }
}
