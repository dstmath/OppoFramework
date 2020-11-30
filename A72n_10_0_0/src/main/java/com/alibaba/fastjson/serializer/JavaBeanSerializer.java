package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaBeanSerializer extends SerializeFilterable implements ObjectSerializer {
    protected SerializeBeanInfo beanInfo;
    protected final FieldSerializer[] getters;
    private volatile transient long[] hashArray;
    private volatile transient short[] hashArrayMapping;
    protected final FieldSerializer[] sortedGetters;

    public JavaBeanSerializer(Class<?> beanType) {
        this(beanType, (Map<String, String>) null);
    }

    public JavaBeanSerializer(Class<?> beanType, String... aliasList) {
        this(beanType, createAliasMap(aliasList));
    }

    static Map<String, String> createAliasMap(String... aliasList) {
        Map<String, String> aliasMap = new HashMap<>();
        for (String alias : aliasList) {
            aliasMap.put(alias, alias);
        }
        return aliasMap;
    }

    public Class<?> getType() {
        return this.beanInfo.beanType;
    }

    public JavaBeanSerializer(Class<?> beanType, Map<String, String> aliasMap) {
        this(TypeUtils.buildBeanInfo(beanType, aliasMap, null));
    }

    public JavaBeanSerializer(SerializeBeanInfo beanInfo2) {
        this.beanInfo = beanInfo2;
        this.sortedGetters = new FieldSerializer[beanInfo2.sortedFields.length];
        for (int i = 0; i < this.sortedGetters.length; i++) {
            this.sortedGetters[i] = new FieldSerializer(beanInfo2.beanType, beanInfo2.sortedFields[i]);
        }
        if (beanInfo2.fields == beanInfo2.sortedFields) {
            this.getters = this.sortedGetters;
        } else {
            this.getters = new FieldSerializer[beanInfo2.fields.length];
            boolean hashNotMatch = false;
            int i2 = 0;
            while (true) {
                if (i2 >= this.getters.length) {
                    break;
                }
                FieldSerializer fieldSerializer = getFieldSerializer(beanInfo2.fields[i2].name);
                if (fieldSerializer == null) {
                    hashNotMatch = true;
                    break;
                } else {
                    this.getters[i2] = fieldSerializer;
                    i2++;
                }
            }
            if (hashNotMatch) {
                System.arraycopy(this.sortedGetters, 0, this.getters, 0, this.sortedGetters.length);
            }
        }
        if (beanInfo2.jsonType != null) {
            for (Class<? extends SerializeFilter> filterClass : beanInfo2.jsonType.serialzeFilters()) {
                try {
                    addFilter((SerializeFilter) filterClass.getConstructor(new Class[0]).newInstance(new Object[0]));
                } catch (Exception e) {
                }
            }
        }
        if (beanInfo2.jsonType != null) {
            for (Class<? extends SerializeFilter> filterClass2 : beanInfo2.jsonType.serialzeFilters()) {
                try {
                    addFilter((SerializeFilter) filterClass2.getConstructor(new Class[0]).newInstance(new Object[0]));
                } catch (Exception e2) {
                }
            }
        }
    }

    public void writeDirectNonContext(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        write(serializer, object, fieldName, fieldType, features);
    }

    public void writeAsArray(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        write(serializer, object, fieldName, fieldType, features);
    }

    public void writeAsArrayNonContext(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        write(serializer, object, fieldName, fieldType, features);
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        write(serializer, object, fieldName, fieldType, features, false);
    }

    public void writeNoneASM(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        write(serializer, object, fieldName, fieldType, features, false);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0111, code lost:
        if (r0.fieldTransient != false) goto L_0x0123;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0143, code lost:
        if (applyLabel(r40, r0.label) == false) goto L_0x0148;
     */
    /* JADX WARNING: Removed duplicated region for block: B:396:0x053f A[SYNTHETIC, Splitter:B:396:0x053f] */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0568 A[SYNTHETIC, Splitter:B:403:0x0568] */
    /* JADX WARNING: Removed duplicated region for block: B:407:0x0583 A[ADDED_TO_REGION, Catch:{ all -> 0x057e }] */
    /* JADX WARNING: Removed duplicated region for block: B:416:0x05ca A[Catch:{ all -> 0x057e }] */
    /* JADX WARNING: Removed duplicated region for block: B:419:0x05e8 A[Catch:{ all -> 0x057e }] */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x05ef A[Catch:{ all -> 0x057e }] */
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features, boolean unwrapped) throws IOException {
        FieldSerializer[] getters2;
        SerialContext parent;
        SerialContext parent2;
        InvocationTargetException ex;
        SerialContext parent3;
        Exception e;
        Throwable cause;
        SerialContext parent4;
        char c;
        FieldSerializer fieldSerializer;
        Field field;
        FieldInfo fieldInfo;
        char endSeperator;
        FieldSerializer[] getters3;
        char seperator;
        char startSeperator;
        int i;
        char newSeperator;
        boolean notApply;
        FieldSerializer errorFieldSerializer;
        Object propertyValue;
        char seperator2;
        Type type;
        Type type2 = fieldType;
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
        } else if (!writeReference(serializer, object, features)) {
            if (out.sortField) {
                getters2 = this.sortedGetters;
            } else {
                getters2 = this.getters;
            }
            FieldSerializer[] getters4 = getters2;
            SerialContext parent5 = serializer.context;
            if (!this.beanInfo.beanType.isEnum()) {
                parent = parent5;
                serializer.setContext(parent5, object, fieldName, this.beanInfo.features, features);
            } else {
                parent = parent5;
            }
            boolean writeAsArray = isWriteAsArray(serializer, features);
            FieldSerializer errorFieldSerializer2 = null;
            char startSeperator2 = writeAsArray ? '[' : '{';
            char endSeperator2 = writeAsArray ? ']' : '}';
            if (!unwrapped) {
                try {
                    out.append(startSeperator2);
                } catch (Exception e2) {
                    e = e2;
                    parent3 = parent;
                } catch (Throwable th) {
                    ex = th;
                    parent2 = parent;
                    serializer.context = parent2;
                    throw ex;
                }
            }
            try {
                if (getters4.length > 0 && out.isEnabled(SerializerFeature.PrettyFormat)) {
                    serializer.incrementIndent();
                    serializer.println();
                }
                boolean commaFlag = false;
                if (!((this.beanInfo.features & SerializerFeature.WriteClassName.mask) == 0 && (SerializerFeature.WriteClassName.mask & features) == 0 && !serializer.isWriteClassName(type2, object))) {
                    Class<?> objClass = object.getClass();
                    if (objClass == type2 || !(type2 instanceof WildcardType)) {
                        type = type2;
                    } else {
                        type = TypeUtils.getClass(fieldType);
                    }
                    if (objClass != type) {
                        writeClassName(serializer, this.beanInfo.typeKey, object);
                        commaFlag = true;
                    }
                }
                char seperator3 = commaFlag ? ',' : 0;
                boolean writeClassName = out.isEnabled(SerializerFeature.WriteClassName);
                boolean directWritePrefix = out.quoteFieldNames && !out.useSingleQuotes;
                char newSeperator2 = writeBefore(serializer, object, seperator3);
                boolean commaFlag2 = newSeperator2 == ',';
                boolean skipTransient = out.isEnabled(SerializerFeature.SkipTransientField);
                boolean ignoreNonFieldGetter = out.isEnabled(SerializerFeature.IgnoreNonFieldGetter);
                boolean commaFlag3 = commaFlag2;
                int i2 = 0;
                while (i2 < getters4.length) {
                    try {
                        fieldSerializer = getters4[i2];
                        field = fieldSerializer.fieldInfo.field;
                        fieldInfo = fieldSerializer.fieldInfo;
                        parent4 = parent;
                    } catch (Exception e3) {
                        e = e3;
                        parent3 = parent;
                        String errorMessage = "write javaBean error, fastjson version 1.2.58";
                        if (object != null) {
                        }
                        parent2 = parent3;
                        if (fieldName == null) {
                        }
                        if (e.getMessage() != null) {
                        }
                        cause = null;
                        if (e instanceof InvocationTargetException) {
                        }
                        if (cause == null) {
                        }
                        throw new JSONException(errorMessage, cause);
                    } catch (Throwable th2) {
                        ex = th2;
                        parent2 = parent;
                        serializer.context = parent2;
                        throw ex;
                    }
                    try {
                        String fieldInfoName = fieldInfo.name;
                        Class<?> fieldClass = fieldInfo.fieldClass;
                        if (skipTransient && field != null) {
                            try {
                            } catch (Exception e4) {
                                e = e4;
                                parent3 = parent4;
                                String errorMessage2 = "write javaBean error, fastjson version 1.2.58";
                                if (object != null) {
                                }
                                parent2 = parent3;
                                if (fieldName == null) {
                                }
                                if (e.getMessage() != null) {
                                }
                                cause = null;
                                if (e instanceof InvocationTargetException) {
                                }
                                if (cause == null) {
                                }
                                throw new JSONException(errorMessage2, cause);
                            } catch (Throwable th3) {
                                ex = th3;
                                parent2 = parent4;
                                serializer.context = parent2;
                                throw ex;
                            }
                        }
                        if (!ignoreNonFieldGetter || field != null) {
                            if (applyName(serializer, object, fieldInfoName)) {
                                notApply = false;
                            }
                            if (writeAsArray) {
                                notApply = true;
                                if (this.beanInfo.typeKey == null || !fieldInfoName.equals(this.beanInfo.typeKey) || !serializer.isWriteClassName(type2, object)) {
                                    if (notApply) {
                                        propertyValue = null;
                                    } else {
                                        try {
                                            propertyValue = fieldSerializer.getPropertyValueDirect(object);
                                        } catch (InvocationTargetException ex2) {
                                            errorFieldSerializer = fieldSerializer;
                                            try {
                                                if (out.isEnabled(SerializerFeature.IgnoreErrorGetter)) {
                                                    propertyValue = null;
                                                } else {
                                                    throw ex2;
                                                }
                                            } catch (Exception e5) {
                                                e = e5;
                                                parent3 = parent4;
                                                errorFieldSerializer2 = errorFieldSerializer;
                                                String errorMessage22 = "write javaBean error, fastjson version 1.2.58";
                                                if (object != null) {
                                                }
                                                parent2 = parent3;
                                                if (fieldName == null) {
                                                }
                                                if (e.getMessage() != null) {
                                                }
                                                cause = null;
                                                if (e instanceof InvocationTargetException) {
                                                }
                                                if (cause == null) {
                                                }
                                                throw new JSONException(errorMessage22, cause);
                                            } catch (Throwable th4) {
                                                ex = th4;
                                                parent2 = parent4;
                                                serializer.context = parent2;
                                                throw ex;
                                            }
                                        } catch (Exception e6) {
                                            e = e6;
                                            parent3 = parent4;
                                            errorFieldSerializer2 = errorFieldSerializer;
                                            String errorMessage222 = "write javaBean error, fastjson version 1.2.58";
                                            if (object != null) {
                                                try {
                                                    errorMessage222 = errorMessage222 + ", class " + object.getClass().getName();
                                                } catch (Throwable th5) {
                                                    ex = th5;
                                                    parent2 = parent3;
                                                    serializer.context = parent2;
                                                    throw ex;
                                                }
                                            }
                                            parent2 = parent3;
                                            if (fieldName == null) {
                                                try {
                                                    errorMessage222 = errorMessage222 + ", fieldName : " + fieldName;
                                                } catch (Throwable th6) {
                                                    ex = th6;
                                                    serializer.context = parent2;
                                                    throw ex;
                                                }
                                            } else if (!(errorFieldSerializer2 == null || errorFieldSerializer2.fieldInfo == null)) {
                                                FieldInfo fieldInfo2 = errorFieldSerializer2.fieldInfo;
                                                if (fieldInfo2.method != null) {
                                                    errorMessage222 = errorMessage222 + ", method : " + fieldInfo2.method.getName();
                                                } else {
                                                    errorMessage222 = errorMessage222 + ", fieldName : " + errorFieldSerializer2.fieldInfo.name;
                                                }
                                            }
                                            if (e.getMessage() != null) {
                                                errorMessage222 = errorMessage222 + ", " + e.getMessage();
                                            }
                                            cause = null;
                                            if (e instanceof InvocationTargetException) {
                                                cause = e.getCause();
                                            }
                                            if (cause == null) {
                                                cause = e;
                                            }
                                            throw new JSONException(errorMessage222, cause);
                                        } catch (Throwable th7) {
                                            ex = th7;
                                            parent2 = parent4;
                                            serializer.context = parent2;
                                            throw ex;
                                        }
                                    }
                                    errorFieldSerializer = errorFieldSerializer2;
                                    if (!apply(serializer, object, fieldInfoName, propertyValue)) {
                                        seperator = seperator3;
                                        newSeperator = newSeperator2;
                                        i = i2;
                                        endSeperator = endSeperator2;
                                        startSeperator = startSeperator2;
                                        getters3 = getters4;
                                    } else {
                                        if (fieldClass == String.class) {
                                            seperator2 = seperator3;
                                            try {
                                                if ("trim".equals(fieldInfo.format) && propertyValue != null) {
                                                    propertyValue = ((String) propertyValue).trim();
                                                }
                                            } catch (Exception e7) {
                                                e = e7;
                                                parent3 = parent4;
                                                errorFieldSerializer2 = errorFieldSerializer;
                                                String errorMessage2222 = "write javaBean error, fastjson version 1.2.58";
                                                if (object != null) {
                                                }
                                                parent2 = parent3;
                                                if (fieldName == null) {
                                                }
                                                if (e.getMessage() != null) {
                                                }
                                                cause = null;
                                                if (e instanceof InvocationTargetException) {
                                                }
                                                if (cause == null) {
                                                }
                                                throw new JSONException(errorMessage2222, cause);
                                            } catch (Throwable th8) {
                                                ex = th8;
                                                parent2 = parent4;
                                                serializer.context = parent2;
                                                throw ex;
                                            }
                                        } else {
                                            seperator2 = seperator3;
                                        }
                                        String key = processKey(serializer, object, fieldInfoName, propertyValue);
                                        getters3 = getters4;
                                        seperator = seperator2;
                                        newSeperator = newSeperator2;
                                        i = i2;
                                        endSeperator = endSeperator2;
                                        startSeperator = startSeperator2;
                                        Object propertyValue2 = processValue(serializer, fieldSerializer.fieldContext, object, fieldInfoName, propertyValue);
                                        if (propertyValue2 == null) {
                                            int serialzeFeatures = fieldInfo.serialzeFeatures;
                                            if (this.beanInfo.jsonType != null) {
                                                serialzeFeatures |= SerializerFeature.of(this.beanInfo.jsonType.serialzeFeatures());
                                            }
                                            if (fieldClass == Boolean.class) {
                                                int defaultMask = SerializerFeature.WriteNullBooleanAsFalse.mask;
                                                int mask = SerializerFeature.WriteMapNullValue.mask | defaultMask;
                                                if (writeAsArray || (serialzeFeatures & mask) != 0 || (out.features & mask) != 0) {
                                                    if (!((serialzeFeatures & defaultMask) == 0 && (out.features & defaultMask) == 0)) {
                                                        propertyValue2 = false;
                                                    }
                                                }
                                            } else if (fieldClass == String.class) {
                                                int defaultMask2 = SerializerFeature.WriteNullStringAsEmpty.mask;
                                                int mask2 = SerializerFeature.WriteMapNullValue.mask | defaultMask2;
                                                if (writeAsArray || (serialzeFeatures & mask2) != 0 || (out.features & mask2) != 0) {
                                                    if (!((serialzeFeatures & defaultMask2) == 0 && (out.features & defaultMask2) == 0)) {
                                                        propertyValue2 = "";
                                                    }
                                                }
                                            } else if (Number.class.isAssignableFrom(fieldClass)) {
                                                int defaultMask3 = SerializerFeature.WriteNullNumberAsZero.mask;
                                                int mask3 = SerializerFeature.WriteMapNullValue.mask | defaultMask3;
                                                if (writeAsArray || (serialzeFeatures & mask3) != 0 || (out.features & mask3) != 0) {
                                                    if (!((serialzeFeatures & defaultMask3) == 0 && (out.features & defaultMask3) == 0)) {
                                                        propertyValue2 = 0;
                                                    }
                                                }
                                            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                                                int defaultMask4 = SerializerFeature.WriteNullListAsEmpty.mask;
                                                int mask4 = SerializerFeature.WriteMapNullValue.mask | defaultMask4;
                                                if (writeAsArray || (serialzeFeatures & mask4) != 0 || (out.features & mask4) != 0) {
                                                    if (!((serialzeFeatures & defaultMask4) == 0 && (out.features & defaultMask4) == 0)) {
                                                        propertyValue2 = Collections.emptyList();
                                                    }
                                                }
                                            } else if (!writeAsArray && !fieldSerializer.writeNull && !out.isEnabled(SerializerFeature.WriteMapNullValue.mask)) {
                                            }
                                        }
                                        if (!(propertyValue2 == null || (!out.notWriteDefaultValue && (fieldInfo.serialzeFeatures & SerializerFeature.NotWriteDefaultValue.mask) == 0 && (this.beanInfo.features & SerializerFeature.NotWriteDefaultValue.mask) == 0))) {
                                            Class<?> fieldCLass = fieldInfo.fieldClass;
                                            if (fieldCLass != Byte.TYPE || !(propertyValue2 instanceof Byte) || ((Byte) propertyValue2).byteValue() != 0) {
                                                if (fieldCLass != Short.TYPE || !(propertyValue2 instanceof Short) || ((Short) propertyValue2).shortValue() != 0) {
                                                    if (fieldCLass != Integer.TYPE || !(propertyValue2 instanceof Integer) || ((Integer) propertyValue2).intValue() != 0) {
                                                        if (fieldCLass != Long.TYPE || !(propertyValue2 instanceof Long) || ((Long) propertyValue2).longValue() != 0) {
                                                            if (fieldCLass != Float.TYPE || !(propertyValue2 instanceof Float) || ((Float) propertyValue2).floatValue() != 0.0f) {
                                                                if (fieldCLass != Double.TYPE || !(propertyValue2 instanceof Double) || ((Double) propertyValue2).doubleValue() != 0.0d) {
                                                                    if (fieldCLass == Boolean.TYPE && (propertyValue2 instanceof Boolean) && !((Boolean) propertyValue2).booleanValue()) {
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (commaFlag3) {
                                            if (!fieldInfo.unwrapped || !(propertyValue2 instanceof Map) || ((Map) propertyValue2).size() != 0) {
                                                out.write(44);
                                                if (out.isEnabled(SerializerFeature.PrettyFormat)) {
                                                    serializer.println();
                                                }
                                            }
                                        }
                                        if (key != fieldInfoName) {
                                            if (!writeAsArray) {
                                                out.writeFieldName(key, true);
                                            }
                                            serializer.write(propertyValue2);
                                        } else if (propertyValue != propertyValue2) {
                                            if (!writeAsArray) {
                                                fieldSerializer.writePrefix(serializer);
                                            }
                                            serializer.write(propertyValue2);
                                        } else {
                                            if (!writeAsArray && (writeClassName || !fieldInfo.unwrapped)) {
                                                if (directWritePrefix) {
                                                    out.write(fieldInfo.name_chars, 0, fieldInfo.name_chars.length);
                                                } else {
                                                    fieldSerializer.writePrefix(serializer);
                                                }
                                            }
                                            if (!writeAsArray) {
                                                JSONField fieldAnnotation = fieldInfo.getAnnotation();
                                                if (fieldClass == String.class && (fieldAnnotation == null || fieldAnnotation.serializeUsing() == Void.class)) {
                                                    if (propertyValue2 == null) {
                                                        if ((out.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                            if ((fieldSerializer.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                out.writeNull();
                                                            }
                                                        }
                                                        out.writeString("");
                                                    } else {
                                                        String propertyValueString = (String) propertyValue2;
                                                        if (out.useSingleQuotes) {
                                                            out.writeStringWithSingleQuote(propertyValueString);
                                                        } else {
                                                            out.writeStringWithDoubleQuote(propertyValueString, (char) 0);
                                                        }
                                                    }
                                                } else if (!fieldInfo.unwrapped || !(propertyValue2 instanceof Map) || ((Map) propertyValue2).size() != 0) {
                                                    fieldSerializer.writeValue(serializer, propertyValue2);
                                                } else {
                                                    commaFlag3 = false;
                                                    errorFieldSerializer2 = errorFieldSerializer;
                                                    i2 = i + 1;
                                                    newSeperator2 = newSeperator;
                                                    startSeperator2 = startSeperator;
                                                    seperator3 = seperator;
                                                    parent = parent4;
                                                    getters4 = getters3;
                                                    endSeperator2 = endSeperator;
                                                    type2 = fieldType;
                                                }
                                            } else {
                                                fieldSerializer.writeValue(serializer, propertyValue2);
                                            }
                                        }
                                        boolean fieldUnwrappedNull = false;
                                        if (fieldInfo.unwrapped && (propertyValue2 instanceof Map)) {
                                            Map map = (Map) propertyValue2;
                                            if (map.size() == 0) {
                                                fieldUnwrappedNull = true;
                                            } else if (!serializer.isEnabled(SerializerFeature.WriteMapNullValue)) {
                                                boolean hasNotNull = false;
                                                Iterator it = map.values().iterator();
                                                while (true) {
                                                    if (!it.hasNext()) {
                                                        break;
                                                    } else if (it.next() != null) {
                                                        hasNotNull = true;
                                                        break;
                                                    }
                                                }
                                                if (!hasNotNull) {
                                                    fieldUnwrappedNull = true;
                                                }
                                            }
                                        }
                                        if (!fieldUnwrappedNull) {
                                            commaFlag3 = true;
                                            errorFieldSerializer2 = errorFieldSerializer;
                                            i2 = i + 1;
                                            newSeperator2 = newSeperator;
                                            startSeperator2 = startSeperator;
                                            seperator3 = seperator;
                                            parent = parent4;
                                            getters4 = getters3;
                                            endSeperator2 = endSeperator;
                                            type2 = fieldType;
                                        }
                                    }
                                    errorFieldSerializer2 = errorFieldSerializer;
                                    i2 = i + 1;
                                    newSeperator2 = newSeperator;
                                    startSeperator2 = startSeperator;
                                    seperator3 = seperator;
                                    parent = parent4;
                                    getters4 = getters3;
                                    endSeperator2 = endSeperator;
                                    type2 = fieldType;
                                }
                            } else {
                                seperator = seperator3;
                                newSeperator = newSeperator2;
                                i = i2;
                                endSeperator = endSeperator2;
                                startSeperator = startSeperator2;
                                getters3 = getters4;
                                i2 = i + 1;
                                newSeperator2 = newSeperator;
                                startSeperator2 = startSeperator;
                                seperator3 = seperator;
                                parent = parent4;
                                getters4 = getters3;
                                endSeperator2 = endSeperator;
                                type2 = fieldType;
                            }
                        }
                        seperator = seperator3;
                        newSeperator = newSeperator2;
                        i = i2;
                        endSeperator = endSeperator2;
                        startSeperator = startSeperator2;
                        getters3 = getters4;
                        i2 = i + 1;
                        newSeperator2 = newSeperator;
                        startSeperator2 = startSeperator;
                        seperator3 = seperator;
                        parent = parent4;
                        getters4 = getters3;
                        endSeperator2 = endSeperator;
                        type2 = fieldType;
                    } catch (Exception e8) {
                        e = e8;
                        parent3 = parent4;
                        String errorMessage22222 = "write javaBean error, fastjson version 1.2.58";
                        if (object != null) {
                        }
                        parent2 = parent3;
                        if (fieldName == null) {
                        }
                        if (e.getMessage() != null) {
                        }
                        cause = null;
                        if (e instanceof InvocationTargetException) {
                        }
                        if (cause == null) {
                        }
                        throw new JSONException(errorMessage22222, cause);
                    } catch (Throwable th9) {
                        ex = th9;
                        parent2 = parent4;
                        serializer.context = parent2;
                        throw ex;
                    }
                }
                parent4 = parent;
                c = 0;
                if (commaFlag3) {
                    c = ',';
                }
            } catch (Exception e9) {
                e = e9;
                parent3 = parent;
                String errorMessage222222 = "write javaBean error, fastjson version 1.2.58";
                if (object != null) {
                }
                parent2 = parent3;
                if (fieldName == null) {
                }
                if (e.getMessage() != null) {
                }
                cause = null;
                if (e instanceof InvocationTargetException) {
                }
                if (cause == null) {
                }
                throw new JSONException(errorMessage222222, cause);
            } catch (Throwable th10) {
                ex = th10;
                parent2 = parent;
                serializer.context = parent2;
                throw ex;
            }
            try {
                writeAfter(serializer, object, c);
                try {
                    if (getters4.length > 0) {
                        try {
                            if (out.isEnabled(SerializerFeature.PrettyFormat)) {
                                serializer.decrementIdent();
                                serializer.println();
                            }
                        } catch (Exception e10) {
                            e = e10;
                            parent3 = parent4;
                            String errorMessage2222222 = "write javaBean error, fastjson version 1.2.58";
                            if (object != null) {
                            }
                            parent2 = parent3;
                            if (fieldName == null) {
                            }
                            if (e.getMessage() != null) {
                            }
                            cause = null;
                            if (e instanceof InvocationTargetException) {
                            }
                            if (cause == null) {
                            }
                            throw new JSONException(errorMessage2222222, cause);
                        } catch (Throwable th11) {
                            ex = th11;
                            parent2 = parent4;
                            serializer.context = parent2;
                            throw ex;
                        }
                    }
                    if (!unwrapped) {
                        out.append(endSeperator2);
                    }
                    serializer.context = parent4;
                } catch (Exception e11) {
                    e = e11;
                    parent3 = parent4;
                    String errorMessage22222222 = "write javaBean error, fastjson version 1.2.58";
                    if (object != null) {
                    }
                    parent2 = parent3;
                    if (fieldName == null) {
                    }
                    if (e.getMessage() != null) {
                    }
                    cause = null;
                    if (e instanceof InvocationTargetException) {
                    }
                    if (cause == null) {
                    }
                    throw new JSONException(errorMessage22222222, cause);
                } catch (Throwable th12) {
                    ex = th12;
                    parent2 = parent4;
                    serializer.context = parent2;
                    throw ex;
                }
            } catch (Exception e12) {
                e = e12;
                parent3 = parent4;
                String errorMessage222222222 = "write javaBean error, fastjson version 1.2.58";
                if (object != null) {
                }
                parent2 = parent3;
                if (fieldName == null) {
                }
                if (e.getMessage() != null) {
                }
                cause = null;
                if (e instanceof InvocationTargetException) {
                }
                if (cause == null) {
                }
                throw new JSONException(errorMessage222222222, cause);
            } catch (Throwable th13) {
                ex = th13;
                parent2 = parent4;
                serializer.context = parent2;
                throw ex;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void writeClassName(JSONSerializer serializer, String typeKey, Object object) {
        if (typeKey == null) {
            typeKey = serializer.config.typeKey;
        }
        serializer.out.writeFieldName(typeKey, false);
        String typeName = this.beanInfo.typeName;
        if (typeName == null) {
            Class<?> clazz = object.getClass();
            if (TypeUtils.isProxy(clazz)) {
                clazz = clazz.getSuperclass();
            }
            typeName = clazz.getName();
        }
        serializer.write(typeName);
    }

    public boolean writeReference(JSONSerializer serializer, Object object, int fieldFeatures) {
        SerialContext context = serializer.context;
        int mask = SerializerFeature.DisableCircularReferenceDetect.mask;
        if (context == null || (context.features & mask) != 0 || (fieldFeatures & mask) != 0 || serializer.references == null || !serializer.references.containsKey(object)) {
            return false;
        }
        serializer.writeReference(object);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isWriteAsArray(JSONSerializer serializer) {
        return isWriteAsArray(serializer, 0);
    }

    /* access modifiers changed from: protected */
    public boolean isWriteAsArray(JSONSerializer serializer, int fieldFeatrues) {
        int mask = SerializerFeature.BeanToArray.mask;
        return ((this.beanInfo.features & mask) == 0 && !serializer.out.beanToArray && (fieldFeatrues & mask) == 0) ? false : true;
    }

    public Object getFieldValue(Object object, String key) {
        FieldSerializer fieldDeser = getFieldSerializer(key);
        if (fieldDeser != null) {
            try {
                return fieldDeser.getPropertyValue(object);
            } catch (InvocationTargetException ex) {
                throw new JSONException("getFieldValue error." + key, ex);
            } catch (IllegalAccessException ex2) {
                throw new JSONException("getFieldValue error." + key, ex2);
            }
        } else {
            throw new JSONException("field not found. " + key);
        }
    }

    public Object getFieldValue(Object object, String key, long keyHash, boolean throwFieldNotFoundException) {
        FieldSerializer fieldDeser = getFieldSerializer(keyHash);
        if (fieldDeser != null) {
            try {
                return fieldDeser.getPropertyValue(object);
            } catch (InvocationTargetException ex) {
                throw new JSONException("getFieldValue error." + key, ex);
            } catch (IllegalAccessException ex2) {
                throw new JSONException("getFieldValue error." + key, ex2);
            }
        } else if (!throwFieldNotFoundException) {
            return null;
        } else {
            throw new JSONException("field not found. " + key);
        }
    }

    public FieldSerializer getFieldSerializer(String key) {
        if (key == null) {
            return null;
        }
        int low = 0;
        int high = this.sortedGetters.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = this.sortedGetters[mid].fieldInfo.name.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp <= 0) {
                return this.sortedGetters[mid];
            } else {
                high = mid - 1;
            }
        }
        return null;
    }

    public FieldSerializer getFieldSerializer(long hash) {
        int p_t;
        PropertyNamingStrategy[] namingStrategies = null;
        int i = 0;
        if (this.hashArray == null) {
            namingStrategies = PropertyNamingStrategy.values();
            long[] hashArray2 = new long[(this.sortedGetters.length * namingStrategies.length)];
            int index = 0;
            int i2 = 0;
            while (i2 < this.sortedGetters.length) {
                String name = this.sortedGetters[i2].fieldInfo.name;
                int index2 = index + 1;
                hashArray2[index] = TypeUtils.fnv1a_64(name);
                for (PropertyNamingStrategy propertyNamingStrategy : namingStrategies) {
                    String name_t = propertyNamingStrategy.translate(name);
                    if (!name.equals(name_t)) {
                        hashArray2[index2] = TypeUtils.fnv1a_64(name_t);
                        index2++;
                    }
                }
                i2++;
                index = index2;
            }
            Arrays.sort(hashArray2, 0, index);
            this.hashArray = new long[index];
            System.arraycopy(hashArray2, 0, this.hashArray, 0, index);
        }
        int pos = Arrays.binarySearch(this.hashArray, hash);
        if (pos < 0) {
            return null;
        }
        if (this.hashArrayMapping == null) {
            if (namingStrategies == null) {
                namingStrategies = PropertyNamingStrategy.values();
            }
            short[] mapping = new short[this.hashArray.length];
            Arrays.fill(mapping, (short) -1);
            int i3 = 0;
            while (i3 < this.sortedGetters.length) {
                String name2 = this.sortedGetters[i3].fieldInfo.name;
                int p = Arrays.binarySearch(this.hashArray, TypeUtils.fnv1a_64(name2));
                if (p >= 0) {
                    mapping[p] = (short) i3;
                }
                for (int j = i; j < namingStrategies.length; j++) {
                    String name_t2 = namingStrategies[j].translate(name2);
                    if (!name2.equals(name_t2) && (p_t = Arrays.binarySearch(this.hashArray, TypeUtils.fnv1a_64(name_t2))) >= 0) {
                        mapping[p_t] = (short) i3;
                    }
                }
                i3++;
                i = 0;
            }
            this.hashArrayMapping = mapping;
        }
        short s = this.hashArrayMapping[pos];
        if (s != -1) {
            return this.sortedGetters[s];
        }
        return null;
    }

    public List<Object> getFieldValues(Object object) throws Exception {
        List<Object> fieldValues = new ArrayList<>(this.sortedGetters.length);
        for (FieldSerializer getter : this.sortedGetters) {
            fieldValues.add(getter.getPropertyValue(object));
        }
        return fieldValues;
    }

    public List<Object> getObjectFieldValues(Object object) throws Exception {
        List<Object> fieldValues = new ArrayList<>(this.sortedGetters.length);
        FieldSerializer[] fieldSerializerArr = this.sortedGetters;
        for (FieldSerializer getter : fieldSerializerArr) {
            Class fieldClass = getter.fieldInfo.fieldClass;
            if (!fieldClass.isPrimitive() && !fieldClass.getName().startsWith("java.lang.")) {
                fieldValues.add(getter.getPropertyValue(object));
            }
        }
        return fieldValues;
    }

    public int getSize(Object object) throws Exception {
        int size = 0;
        for (FieldSerializer getter : this.sortedGetters) {
            if (getter.getPropertyValueDirect(object) != null) {
                size++;
            }
        }
        return size;
    }

    public Set<String> getFieldNames(Object object) throws Exception {
        Set<String> fieldNames = new HashSet<>();
        FieldSerializer[] fieldSerializerArr = this.sortedGetters;
        for (FieldSerializer getter : fieldSerializerArr) {
            if (getter.getPropertyValueDirect(object) != null) {
                fieldNames.add(getter.fieldInfo.name);
            }
        }
        return fieldNames;
    }

    public Map<String, Object> getFieldValuesMap(Object object) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>(this.sortedGetters.length);
        FieldSerializer[] fieldSerializerArr = this.sortedGetters;
        for (FieldSerializer getter : fieldSerializerArr) {
            map.put(getter.fieldInfo.name, getter.getPropertyValue(object));
        }
        return map;
    }

    /* access modifiers changed from: protected */
    public BeanContext getBeanContext(int orinal) {
        return this.sortedGetters[orinal].fieldContext;
    }

    /* access modifiers changed from: protected */
    public Type getFieldType(int ordinal) {
        return this.sortedGetters[ordinal].fieldInfo.fieldType;
    }

    /* access modifiers changed from: protected */
    public char writeBefore(JSONSerializer jsonBeanDeser, Object object, char seperator) {
        if (jsonBeanDeser.beforeFilters != null) {
            for (BeforeFilter beforeFilter : jsonBeanDeser.beforeFilters) {
                seperator = beforeFilter.writeBefore(jsonBeanDeser, object, seperator);
            }
        }
        if (this.beforeFilters != null) {
            for (BeforeFilter beforeFilter2 : this.beforeFilters) {
                seperator = beforeFilter2.writeBefore(jsonBeanDeser, object, seperator);
            }
        }
        return seperator;
    }

    /* access modifiers changed from: protected */
    public char writeAfter(JSONSerializer jsonBeanDeser, Object object, char seperator) {
        if (jsonBeanDeser.afterFilters != null) {
            for (AfterFilter afterFilter : jsonBeanDeser.afterFilters) {
                seperator = afterFilter.writeAfter(jsonBeanDeser, object, seperator);
            }
        }
        if (this.afterFilters != null) {
            for (AfterFilter afterFilter2 : this.afterFilters) {
                seperator = afterFilter2.writeAfter(jsonBeanDeser, object, seperator);
            }
        }
        return seperator;
    }

    /* access modifiers changed from: protected */
    public boolean applyLabel(JSONSerializer jsonBeanDeser, String label) {
        if (jsonBeanDeser.labelFilters != null) {
            for (LabelFilter propertyFilter : jsonBeanDeser.labelFilters) {
                if (!propertyFilter.apply(label)) {
                    return false;
                }
            }
        }
        if (this.labelFilters == null) {
            return true;
        }
        for (LabelFilter propertyFilter2 : this.labelFilters) {
            if (!propertyFilter2.apply(label)) {
                return false;
            }
        }
        return true;
    }
}
