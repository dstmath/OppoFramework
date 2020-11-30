package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class FieldSerializer implements Comparable<FieldSerializer> {
    protected boolean browserCompatible;
    protected boolean disableCircularReferenceDetect = false;
    private final String double_quoted_fieldPrefix;
    protected int features;
    protected BeanContext fieldContext;
    public final FieldInfo fieldInfo;
    private String format;
    protected boolean persistenceXToMany = false;
    private RuntimeSerializerInfo runtimeInfo;
    protected boolean serializeUsing = false;
    private String single_quoted_fieldPrefix;
    private String un_quoted_fieldPrefix;
    protected boolean writeEnumUsingName = false;
    protected boolean writeEnumUsingToString = false;
    protected final boolean writeNull;

    public FieldSerializer(Class<?> beanType, FieldInfo fieldInfo2) {
        JSONType jsonType;
        boolean z = false;
        this.fieldInfo = fieldInfo2;
        this.fieldContext = new BeanContext(beanType, fieldInfo2);
        if (beanType != null && ((fieldInfo2.isEnum || fieldInfo2.fieldClass == Long.TYPE || fieldInfo2.fieldClass == Long.class || fieldInfo2.fieldClass == BigInteger.class || fieldInfo2.fieldClass == BigDecimal.class) && (jsonType = (JSONType) TypeUtils.getAnnotation(beanType, JSONType.class)) != null)) {
            SerializerFeature[] serialzeFeatures = jsonType.serialzeFeatures();
            for (SerializerFeature feature : serialzeFeatures) {
                if (feature == SerializerFeature.WriteEnumUsingToString) {
                    this.writeEnumUsingToString = true;
                } else if (feature == SerializerFeature.WriteEnumUsingName) {
                    this.writeEnumUsingName = true;
                } else if (feature == SerializerFeature.DisableCircularReferenceDetect) {
                    this.disableCircularReferenceDetect = true;
                } else if (feature == SerializerFeature.BrowserCompatible) {
                    this.features |= SerializerFeature.BrowserCompatible.mask;
                    this.browserCompatible = true;
                }
            }
        }
        fieldInfo2.setAccessible();
        this.double_quoted_fieldPrefix = '\"' + fieldInfo2.name + "\":";
        boolean writeNull2 = false;
        JSONField annotation = fieldInfo2.getAnnotation();
        if (annotation != null) {
            SerializerFeature[] serialzeFeatures2 = annotation.serialzeFeatures();
            int length = serialzeFeatures2.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if ((serialzeFeatures2[i].getMask() & SerializerFeature.WRITE_MAP_NULL_FEATURES) != 0) {
                    writeNull2 = true;
                    break;
                } else {
                    i++;
                }
            }
            this.format = annotation.format();
            if (this.format.trim().length() == 0) {
                this.format = null;
            }
            SerializerFeature[] serialzeFeatures3 = annotation.serialzeFeatures();
            for (SerializerFeature feature2 : serialzeFeatures3) {
                if (feature2 == SerializerFeature.WriteEnumUsingToString) {
                    this.writeEnumUsingToString = true;
                } else if (feature2 == SerializerFeature.WriteEnumUsingName) {
                    this.writeEnumUsingName = true;
                } else if (feature2 == SerializerFeature.DisableCircularReferenceDetect) {
                    this.disableCircularReferenceDetect = true;
                } else if (feature2 == SerializerFeature.BrowserCompatible) {
                    this.browserCompatible = true;
                }
            }
            this.features = SerializerFeature.of(annotation.serialzeFeatures());
        }
        this.writeNull = writeNull2;
        this.persistenceXToMany = (TypeUtils.isAnnotationPresentOneToMany(fieldInfo2.method) || TypeUtils.isAnnotationPresentManyToMany(fieldInfo2.method)) ? true : z;
    }

    public void writePrefix(JSONSerializer serializer) throws IOException {
        SerializeWriter out = serializer.out;
        if (!out.quoteFieldNames) {
            if (this.un_quoted_fieldPrefix == null) {
                this.un_quoted_fieldPrefix = this.fieldInfo.name + ":";
            }
            out.write(this.un_quoted_fieldPrefix);
        } else if (out.useSingleQuotes) {
            if (this.single_quoted_fieldPrefix == null) {
                this.single_quoted_fieldPrefix = '\'' + this.fieldInfo.name + "':";
            }
            out.write(this.single_quoted_fieldPrefix);
        } else {
            out.write(this.double_quoted_fieldPrefix);
        }
    }

    public Object getPropertyValueDirect(Object object) throws InvocationTargetException, IllegalAccessException {
        Object fieldValue = this.fieldInfo.get(object);
        if (!this.persistenceXToMany || TypeUtils.isHibernateInitialized(fieldValue)) {
            return fieldValue;
        }
        return null;
    }

    public Object getPropertyValue(Object object) throws InvocationTargetException, IllegalAccessException {
        Object propertyValue = this.fieldInfo.get(object);
        if (this.format == null || propertyValue == null || this.fieldInfo.fieldClass != Date.class) {
            return propertyValue;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(this.format, JSON.defaultLocale);
        dateFormat.setTimeZone(JSON.defaultTimeZone);
        return dateFormat.format(propertyValue);
    }

    public int compareTo(FieldSerializer o) {
        return this.fieldInfo.compareTo(o.fieldInfo);
    }

    /* JADX INFO: Multiple debug info for r0v1 com.alibaba.fastjson.serializer.FieldSerializer$RuntimeSerializerInfo: [D('runtimeFieldClass' java.lang.Class<?>), D('runtimeInfo' com.alibaba.fastjson.serializer.FieldSerializer$RuntimeSerializerInfo)] */
    public void writeValue(JSONSerializer serializer, Object propertyValue) throws Exception {
        ObjectSerializer valueSerializer;
        Class<?> runtimeFieldClass;
        if (this.runtimeInfo == null) {
            if (propertyValue == null) {
                runtimeFieldClass = this.fieldInfo.fieldClass;
                if (runtimeFieldClass == Byte.TYPE) {
                    runtimeFieldClass = Byte.class;
                } else if (runtimeFieldClass == Short.TYPE) {
                    runtimeFieldClass = Short.class;
                } else if (runtimeFieldClass == Integer.TYPE) {
                    runtimeFieldClass = Integer.class;
                } else if (runtimeFieldClass == Long.TYPE) {
                    runtimeFieldClass = Long.class;
                } else if (runtimeFieldClass == Float.TYPE) {
                    runtimeFieldClass = Float.class;
                } else if (runtimeFieldClass == Double.TYPE) {
                    runtimeFieldClass = Double.class;
                } else if (runtimeFieldClass == Boolean.TYPE) {
                    runtimeFieldClass = Boolean.class;
                }
            } else {
                runtimeFieldClass = propertyValue.getClass();
            }
            ObjectSerializer fieldSerializer = null;
            JSONField fieldAnnotation = this.fieldInfo.getAnnotation();
            if (fieldAnnotation == null || fieldAnnotation.serializeUsing() == Void.class) {
                if (this.format != null) {
                    if (runtimeFieldClass == Double.TYPE || runtimeFieldClass == Double.class) {
                        fieldSerializer = new DoubleSerializer(this.format);
                    } else if (runtimeFieldClass == Float.TYPE || runtimeFieldClass == Float.class) {
                        fieldSerializer = new FloatCodec(this.format);
                    }
                }
                if (fieldSerializer == null) {
                    fieldSerializer = serializer.getObjectWriter(runtimeFieldClass);
                }
            } else {
                fieldSerializer = (ObjectSerializer) fieldAnnotation.serializeUsing().newInstance();
                this.serializeUsing = true;
            }
            this.runtimeInfo = new RuntimeSerializerInfo(fieldSerializer, runtimeFieldClass);
        }
        RuntimeSerializerInfo runtimeInfo2 = this.runtimeInfo;
        int fieldFeatures = (this.disableCircularReferenceDetect ? this.fieldInfo.serialzeFeatures | SerializerFeature.DisableCircularReferenceDetect.mask : this.fieldInfo.serialzeFeatures) | this.features;
        if (propertyValue == null) {
            SerializeWriter out = serializer.out;
            if (this.fieldInfo.fieldClass != Object.class || !out.isEnabled(SerializerFeature.WRITE_MAP_NULL_FEATURES)) {
                Class<?> runtimeFieldClass2 = runtimeInfo2.runtimeFieldClass;
                if (Number.class.isAssignableFrom(runtimeFieldClass2)) {
                    out.writeNull(this.features, SerializerFeature.WriteNullNumberAsZero.mask);
                } else if (String.class == runtimeFieldClass2) {
                    out.writeNull(this.features, SerializerFeature.WriteNullStringAsEmpty.mask);
                } else if (Boolean.class == runtimeFieldClass2) {
                    out.writeNull(this.features, SerializerFeature.WriteNullBooleanAsFalse.mask);
                } else if (Collection.class.isAssignableFrom(runtimeFieldClass2)) {
                    out.writeNull(this.features, SerializerFeature.WriteNullListAsEmpty.mask);
                } else {
                    ObjectSerializer fieldSerializer2 = runtimeInfo2.fieldSerializer;
                    if (!out.isEnabled(SerializerFeature.WRITE_MAP_NULL_FEATURES) || !(fieldSerializer2 instanceof JavaBeanSerializer)) {
                        fieldSerializer2.write(serializer, null, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures);
                    } else {
                        out.writeNull();
                    }
                }
            } else {
                out.writeNull();
            }
        } else {
            if (this.fieldInfo.isEnum) {
                if (this.writeEnumUsingName) {
                    serializer.out.writeString(((Enum) propertyValue).name());
                    return;
                } else if (this.writeEnumUsingToString) {
                    serializer.out.writeString(((Enum) propertyValue).toString());
                    return;
                }
            }
            Class<?> valueClass = propertyValue.getClass();
            if (valueClass == runtimeInfo2.runtimeFieldClass || this.serializeUsing) {
                valueSerializer = runtimeInfo2.fieldSerializer;
            } else {
                valueSerializer = serializer.getObjectWriter(valueClass);
            }
            if (this.format == null || (valueSerializer instanceof DoubleSerializer) || (valueSerializer instanceof FloatCodec)) {
                if (this.fieldInfo.unwrapped) {
                    if (valueSerializer instanceof JavaBeanSerializer) {
                        ((JavaBeanSerializer) valueSerializer).write(serializer, propertyValue, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures, true);
                        return;
                    } else if (valueSerializer instanceof MapSerializer) {
                        ((MapSerializer) valueSerializer).write(serializer, propertyValue, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures, true);
                        return;
                    }
                }
                if ((this.features & SerializerFeature.WriteClassName.mask) == 0 || valueClass == this.fieldInfo.fieldClass || !JavaBeanSerializer.class.isInstance(valueSerializer)) {
                    if (this.browserCompatible && propertyValue != null && (this.fieldInfo.fieldClass == Long.TYPE || this.fieldInfo.fieldClass == Long.class)) {
                        long value = ((Long) propertyValue).longValue();
                        if (value > 9007199254740991L || value < -9007199254740991L) {
                            serializer.getWriter().writeString(Long.toString(value));
                            return;
                        }
                    }
                    valueSerializer.write(serializer, propertyValue, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures);
                    return;
                }
                ((JavaBeanSerializer) valueSerializer).write(serializer, propertyValue, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures, false);
            } else if (valueSerializer instanceof ContextObjectSerializer) {
                ((ContextObjectSerializer) valueSerializer).write(serializer, propertyValue, this.fieldContext);
            } else {
                serializer.writeWithFormat(propertyValue, this.format);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class RuntimeSerializerInfo {
        final ObjectSerializer fieldSerializer;
        final Class<?> runtimeFieldClass;

        public RuntimeSerializerInfo(ObjectSerializer fieldSerializer2, Class<?> runtimeFieldClass2) {
            this.fieldSerializer = fieldSerializer2;
            this.runtimeFieldClass = runtimeFieldClass2;
        }
    }
}
