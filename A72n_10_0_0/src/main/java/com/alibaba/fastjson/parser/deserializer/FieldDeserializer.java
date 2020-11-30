package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.util.FieldInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class FieldDeserializer {
    protected BeanContext beanContext;
    protected final Class<?> clazz;
    public final FieldInfo fieldInfo;

    public abstract void parseField(DefaultJSONParser defaultJSONParser, Object obj, Type type, Map<String, Object> map);

    public FieldDeserializer(Class<?> clazz2, FieldInfo fieldInfo2) {
        this.clazz = clazz2;
        this.fieldInfo = fieldInfo2;
    }

    public int getFastMatchToken() {
        return 0;
    }

    public void setValue(Object object, boolean value) {
        setValue(object, Boolean.valueOf(value));
    }

    public void setValue(Object object, int value) {
        setValue(object, Integer.valueOf(value));
    }

    public void setValue(Object object, long value) {
        setValue(object, Long.valueOf(value));
    }

    public void setValue(Object object, String value) {
        setValue(object, (Object) value);
    }

    public void setValue(Object object, Object value) {
        if (value != null || !this.fieldInfo.fieldClass.isPrimitive()) {
            if (this.fieldInfo.fieldClass == String.class && this.fieldInfo.format != null && this.fieldInfo.format.equals("trim")) {
                value = ((String) value).trim();
            }
            try {
                Method method = this.fieldInfo.method;
                if (method == null) {
                    Field field = this.fieldInfo.field;
                    if (this.fieldInfo.getOnly) {
                        if (this.fieldInfo.fieldClass == AtomicInteger.class) {
                            AtomicInteger atomic = (AtomicInteger) field.get(object);
                            if (atomic != null) {
                                atomic.set(((AtomicInteger) value).get());
                            }
                        } else if (this.fieldInfo.fieldClass == AtomicLong.class) {
                            AtomicLong atomic2 = (AtomicLong) field.get(object);
                            if (atomic2 != null) {
                                atomic2.set(((AtomicLong) value).get());
                            }
                        } else if (this.fieldInfo.fieldClass == AtomicBoolean.class) {
                            AtomicBoolean atomic3 = (AtomicBoolean) field.get(object);
                            if (atomic3 != null) {
                                atomic3.set(((AtomicBoolean) value).get());
                            }
                        } else if (Map.class.isAssignableFrom(this.fieldInfo.fieldClass)) {
                            Map map = (Map) field.get(object);
                            if (map != null) {
                                if (map == Collections.emptyMap()) {
                                    return;
                                }
                                if (!map.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                                    map.putAll((Map) value);
                                }
                            }
                        } else {
                            Collection collection = (Collection) field.get(object);
                            if (!(collection == null || value == null)) {
                                if (collection != Collections.emptySet() && collection != Collections.emptyList()) {
                                    if (!collection.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                                        collection.clear();
                                        collection.addAll((Collection) value);
                                    }
                                }
                            }
                        }
                    } else if (field != null) {
                        field.set(object, value);
                    }
                } else if (!this.fieldInfo.getOnly) {
                    method.invoke(object, value);
                } else if (this.fieldInfo.fieldClass == AtomicInteger.class) {
                    AtomicInteger atomic4 = (AtomicInteger) method.invoke(object, new Object[0]);
                    if (atomic4 != null) {
                        atomic4.set(((AtomicInteger) value).get());
                    }
                } else if (this.fieldInfo.fieldClass == AtomicLong.class) {
                    AtomicLong atomic5 = (AtomicLong) method.invoke(object, new Object[0]);
                    if (atomic5 != null) {
                        atomic5.set(((AtomicLong) value).get());
                    }
                } else if (this.fieldInfo.fieldClass == AtomicBoolean.class) {
                    AtomicBoolean atomic6 = (AtomicBoolean) method.invoke(object, new Object[0]);
                    if (atomic6 != null) {
                        atomic6.set(((AtomicBoolean) value).get());
                    }
                } else if (Map.class.isAssignableFrom(method.getReturnType())) {
                    Map map2 = (Map) method.invoke(object, new Object[0]);
                    if (map2 != null) {
                        if (map2 == Collections.emptyMap()) {
                            return;
                        }
                        if (!map2.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                            map2.putAll((Map) value);
                        }
                    }
                } else {
                    Collection collection2 = (Collection) method.invoke(object, new Object[0]);
                    if (!(collection2 == null || value == null)) {
                        if (collection2 != Collections.emptySet() && collection2 != Collections.emptyList()) {
                            if (!collection2.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                                collection2.clear();
                                collection2.addAll((Collection) value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new JSONException("set property error, " + this.clazz.getName() + "#" + this.fieldInfo.name, e);
            }
        }
    }

    public void setWrappedValue(String key, Object value) {
        throw new JSONException("TODO");
    }
}
