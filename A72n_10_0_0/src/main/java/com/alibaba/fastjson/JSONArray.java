package com.alibaba.fastjson;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.RandomAccess;

public class JSONArray extends JSON implements Serializable, Cloneable, List<Object>, RandomAccess {
    private static final long serialVersionUID = 1;
    protected transient Type componentType;
    private final List<Object> list;
    protected transient Object relatedArray;

    public JSONArray() {
        this.list = new ArrayList();
    }

    public JSONArray(List<Object> list2) {
        this.list = list2;
    }

    public JSONArray(int initialCapacity) {
        this.list = new ArrayList(initialCapacity);
    }

    public Object getRelatedArray() {
        return this.relatedArray;
    }

    public void setRelatedArray(Object relatedArray2) {
        this.relatedArray = relatedArray2;
    }

    public Type getComponentType() {
        return this.componentType;
    }

    public void setComponentType(Type componentType2) {
        this.componentType = componentType2;
    }

    public int size() {
        return this.list.size();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public boolean contains(Object o) {
        return this.list.contains(o);
    }

    @Override // java.util.List, java.util.Collection, java.lang.Iterable
    public Iterator<Object> iterator() {
        return this.list.iterator();
    }

    public Object[] toArray() {
        return this.list.toArray();
    }

    @Override // java.util.List, java.util.Collection
    public <T> T[] toArray(T[] a) {
        return (T[]) this.list.toArray(a);
    }

    @Override // java.util.List, java.util.Collection
    public boolean add(Object e) {
        return this.list.add(e);
    }

    public JSONArray fluentAdd(Object e) {
        this.list.add(e);
        return this;
    }

    @Override // java.util.List
    public boolean remove(Object o) {
        return this.list.remove(o);
    }

    public JSONArray fluentRemove(Object o) {
        this.list.remove(o);
        return this;
    }

    @Override // java.util.List, java.util.Collection
    public boolean containsAll(Collection<?> c) {
        return this.list.containsAll(c);
    }

    @Override // java.util.List, java.util.Collection
    public boolean addAll(Collection<? extends Object> c) {
        return this.list.addAll(c);
    }

    public JSONArray fluentAddAll(Collection<? extends Object> c) {
        this.list.addAll(c);
        return this;
    }

    @Override // java.util.List
    public boolean addAll(int index, Collection<? extends Object> c) {
        return this.list.addAll(index, c);
    }

    public JSONArray fluentAddAll(int index, Collection<? extends Object> c) {
        this.list.addAll(index, c);
        return this;
    }

    @Override // java.util.List, java.util.Collection
    public boolean removeAll(Collection<?> c) {
        return this.list.removeAll(c);
    }

    public JSONArray fluentRemoveAll(Collection<?> c) {
        this.list.removeAll(c);
        return this;
    }

    @Override // java.util.List, java.util.Collection
    public boolean retainAll(Collection<?> c) {
        return this.list.retainAll(c);
    }

    public JSONArray fluentRetainAll(Collection<?> c) {
        this.list.retainAll(c);
        return this;
    }

    public void clear() {
        this.list.clear();
    }

    public JSONArray fluentClear() {
        this.list.clear();
        return this;
    }

    @Override // java.util.List
    public Object set(int index, Object element) {
        if (index == -1) {
            this.list.add(element);
            return null;
        } else if (this.list.size() > index) {
            return this.list.set(index, element);
        } else {
            for (int i = this.list.size(); i < index; i++) {
                this.list.add(null);
            }
            this.list.add(element);
            return null;
        }
    }

    public JSONArray fluentSet(int index, Object element) {
        set(index, element);
        return this;
    }

    @Override // java.util.List
    public void add(int index, Object element) {
        this.list.add(index, element);
    }

    public JSONArray fluentAdd(int index, Object element) {
        this.list.add(index, element);
        return this;
    }

    @Override // java.util.List
    public Object remove(int index) {
        return this.list.remove(index);
    }

    public JSONArray fluentRemove(int index) {
        this.list.remove(index);
        return this;
    }

    public int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return this.list.lastIndexOf(o);
    }

    @Override // java.util.List
    public ListIterator<Object> listIterator() {
        return this.list.listIterator();
    }

    @Override // java.util.List
    public ListIterator<Object> listIterator(int index) {
        return this.list.listIterator(index);
    }

    @Override // java.util.List
    public List<Object> subList(int fromIndex, int toIndex) {
        return this.list.subList(fromIndex, toIndex);
    }

    @Override // java.util.List
    public Object get(int index) {
        return this.list.get(index);
    }

    public JSONObject getJSONObject(int index) {
        Object value = this.list.get(index);
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        if (value instanceof Map) {
            return new JSONObject((Map) value);
        }
        return (JSONObject) toJSON(value);
    }

    public JSONArray getJSONArray(int index) {
        Object value = this.list.get(index);
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        if (value instanceof List) {
            return new JSONArray((List) value);
        }
        return (JSONArray) toJSON(value);
    }

    public <T> T getObject(int index, Class<T> clazz) {
        return (T) TypeUtils.castToJavaBean(this.list.get(index), clazz);
    }

    public <T> T getObject(int index, Type type) {
        Object obj = this.list.get(index);
        return type instanceof Class ? (T) TypeUtils.castToJavaBean(obj, (Class) type) : (T) JSON.parseObject(JSON.toJSONString(obj), type, new Feature[0]);
    }

    public Boolean getBoolean(int index) {
        Object value = get(index);
        if (value == null) {
            return null;
        }
        return TypeUtils.castToBoolean(value);
    }

    public boolean getBooleanValue(int index) {
        Object value = get(index);
        if (value == null) {
            return false;
        }
        return TypeUtils.castToBoolean(value).booleanValue();
    }

    public Byte getByte(int index) {
        return TypeUtils.castToByte(get(index));
    }

    public byte getByteValue(int index) {
        Byte byteVal = TypeUtils.castToByte(get(index));
        if (byteVal == null) {
            return 0;
        }
        return byteVal.byteValue();
    }

    public Short getShort(int index) {
        return TypeUtils.castToShort(get(index));
    }

    public short getShortValue(int index) {
        Short shortVal = TypeUtils.castToShort(get(index));
        if (shortVal == null) {
            return 0;
        }
        return shortVal.shortValue();
    }

    public Integer getInteger(int index) {
        return TypeUtils.castToInt(get(index));
    }

    public int getIntValue(int index) {
        Integer intVal = TypeUtils.castToInt(get(index));
        if (intVal == null) {
            return 0;
        }
        return intVal.intValue();
    }

    public Long getLong(int index) {
        return TypeUtils.castToLong(get(index));
    }

    public long getLongValue(int index) {
        Long longVal = TypeUtils.castToLong(get(index));
        if (longVal == null) {
            return 0;
        }
        return longVal.longValue();
    }

    public Float getFloat(int index) {
        return TypeUtils.castToFloat(get(index));
    }

    public float getFloatValue(int index) {
        Float floatValue = TypeUtils.castToFloat(get(index));
        if (floatValue == null) {
            return 0.0f;
        }
        return floatValue.floatValue();
    }

    public Double getDouble(int index) {
        return TypeUtils.castToDouble(get(index));
    }

    public double getDoubleValue(int index) {
        Double doubleValue = TypeUtils.castToDouble(get(index));
        if (doubleValue == null) {
            return 0.0d;
        }
        return doubleValue.doubleValue();
    }

    public BigDecimal getBigDecimal(int index) {
        return TypeUtils.castToBigDecimal(get(index));
    }

    public BigInteger getBigInteger(int index) {
        return TypeUtils.castToBigInteger(get(index));
    }

    public String getString(int index) {
        return TypeUtils.castToString(get(index));
    }

    public Date getDate(int index) {
        return TypeUtils.castToDate(get(index));
    }

    public java.sql.Date getSqlDate(int index) {
        return TypeUtils.castToSqlDate(get(index));
    }

    public Timestamp getTimestamp(int index) {
        return TypeUtils.castToTimestamp(get(index));
    }

    public <T> List<T> toJavaList(Class<T> clazz) {
        ArrayList arrayList = new ArrayList(size());
        ParserConfig config = ParserConfig.getGlobalInstance();
        Iterator<Object> it = iterator();
        while (it.hasNext()) {
            arrayList.add(TypeUtils.cast(it.next(), (Class<Object>) clazz, config));
        }
        return arrayList;
    }

    @Override // java.lang.Object
    public Object clone() {
        return new JSONArray(new ArrayList(this.list));
    }

    public boolean equals(Object obj) {
        return this.list.equals(obj);
    }

    public int hashCode() {
        return this.list.hashCode();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        JSONObject.SecureObjectInputStream.ensureFields();
        if (JSONObject.SecureObjectInputStream.fields != null && !JSONObject.SecureObjectInputStream.fields_error) {
            try {
                new JSONObject.SecureObjectInputStream(in).defaultReadObject();
                return;
            } catch (NotActiveException e) {
            }
        }
        in.defaultReadObject();
        for (Object item : this.list) {
            if (item != null) {
                ParserConfig.global.checkAutoType(item.getClass().getName(), null);
            }
        }
    }
}
