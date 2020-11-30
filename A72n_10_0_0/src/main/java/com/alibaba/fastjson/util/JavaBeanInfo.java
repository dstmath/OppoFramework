package com.alibaba.fastjson.util;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONPOJOBuilder;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JavaBeanInfo {
    public final Method buildMethod;
    public final Class<?> builderClass;
    public final Class<?> clazz;
    public final Constructor<?> creatorConstructor;
    public Type[] creatorConstructorParameterTypes;
    public String[] creatorConstructorParameters;
    public final Constructor<?> defaultConstructor;
    public final int defaultConstructorParameterSize;
    public final Method factoryMethod;
    public final FieldInfo[] fields;
    public final JSONType jsonType;
    public boolean kotlin;
    public Constructor<?> kotlinDefaultConstructor;
    public String[] orders;
    public final int parserFeatures;
    public final FieldInfo[] sortedFields;
    public final String typeKey;
    public final String typeName;

    /* JADX INFO: Multiple debug info for r0v24 java.lang.annotation.Annotation: [D('paramAnnotation' java.lang.annotation.Annotation), D('paramAnnotationArrays' java.lang.annotation.Annotation[][])] */
    public JavaBeanInfo(Class<?> clazz2, Class<?> builderClass2, Constructor<?> defaultConstructor2, Constructor<?> creatorConstructor2, Method factoryMethod2, Method buildMethod2, JSONType jsonType2, List<FieldInfo> fieldList) {
        boolean match;
        Annotation[][] paramAnnotationArrays;
        this.clazz = clazz2;
        this.builderClass = builderClass2;
        this.defaultConstructor = defaultConstructor2;
        this.creatorConstructor = creatorConstructor2;
        this.factoryMethod = factoryMethod2;
        this.parserFeatures = TypeUtils.getParserFeatures(clazz2);
        this.buildMethod = buildMethod2;
        this.jsonType = jsonType2;
        String[] strArr = null;
        if (jsonType2 != null) {
            String typeName2 = jsonType2.typeName();
            String typeKey2 = jsonType2.typeKey();
            this.typeKey = typeKey2.length() > 0 ? typeKey2 : null;
            if (typeName2.length() != 0) {
                this.typeName = typeName2;
            } else {
                this.typeName = clazz2.getName();
            }
            String[] orders2 = jsonType2.orders();
            this.orders = orders2.length != 0 ? orders2 : strArr;
        } else {
            this.typeName = clazz2.getName();
            this.typeKey = null;
            this.orders = null;
        }
        this.fields = new FieldInfo[fieldList.size()];
        fieldList.toArray(this.fields);
        FieldInfo[] sortedFields2 = new FieldInfo[this.fields.length];
        if (this.orders != null) {
            LinkedHashMap<String, FieldInfo> map = new LinkedHashMap<>(fieldList.size());
            FieldInfo[] fieldInfoArr = this.fields;
            for (FieldInfo field : fieldInfoArr) {
                map.put(field.name, field);
            }
            String[] strArr2 = this.orders;
            int i = 0;
            for (String item : strArr2) {
                FieldInfo field2 = map.get(item);
                if (field2 != null) {
                    sortedFields2[i] = field2;
                    map.remove(item);
                    i++;
                }
            }
            for (FieldInfo field3 : map.values()) {
                sortedFields2[i] = field3;
                i++;
            }
        } else {
            System.arraycopy(this.fields, 0, sortedFields2, 0, this.fields.length);
            Arrays.sort(sortedFields2);
        }
        this.sortedFields = Arrays.equals(this.fields, sortedFields2) ? this.fields : sortedFields2;
        if (defaultConstructor2 != null) {
            this.defaultConstructorParameterSize = defaultConstructor2.getParameterTypes().length;
        } else if (factoryMethod2 != null) {
            this.defaultConstructorParameterSize = factoryMethod2.getParameterTypes().length;
        } else {
            this.defaultConstructorParameterSize = 0;
        }
        if (creatorConstructor2 != null) {
            this.creatorConstructorParameterTypes = creatorConstructor2.getParameterTypes();
            this.kotlin = TypeUtils.isKotlin(clazz2);
            if (this.kotlin) {
                this.creatorConstructorParameters = TypeUtils.getKoltinConstructorParameters(clazz2);
                int i2 = 0;
                try {
                    this.kotlinDefaultConstructor = clazz2.getConstructor(new Class[0]);
                } catch (Throwable th) {
                }
                Annotation[][] paramAnnotationArrays2 = creatorConstructor2.getParameterAnnotations();
                int i3 = 0;
                while (i3 < this.creatorConstructorParameters.length && i3 < paramAnnotationArrays2.length) {
                    Annotation[] paramAnnotations = paramAnnotationArrays2[i3];
                    JSONField fieldAnnotation = null;
                    int length = paramAnnotations.length;
                    while (true) {
                        if (i2 >= length) {
                            paramAnnotationArrays = paramAnnotationArrays2;
                            break;
                        }
                        paramAnnotationArrays = paramAnnotationArrays2;
                        Annotation paramAnnotation = paramAnnotations[i2];
                        if (paramAnnotation instanceof JSONField) {
                            fieldAnnotation = (JSONField) paramAnnotation;
                            break;
                        } else {
                            i2++;
                            paramAnnotationArrays2 = paramAnnotationArrays;
                        }
                    }
                    if (fieldAnnotation != null) {
                        String fieldAnnotationName = fieldAnnotation.name();
                        if (fieldAnnotationName.length() > 0) {
                            this.creatorConstructorParameters[i3] = fieldAnnotationName;
                        }
                    }
                    i3++;
                    paramAnnotationArrays2 = paramAnnotationArrays;
                    i2 = 0;
                }
                return;
            }
            if (this.creatorConstructorParameterTypes.length != this.fields.length) {
                match = false;
            } else {
                match = true;
                int i4 = 0;
                while (true) {
                    if (i4 >= this.creatorConstructorParameterTypes.length) {
                        break;
                    } else if (this.creatorConstructorParameterTypes[i4] != this.fields[i4].fieldClass) {
                        match = false;
                        break;
                    } else {
                        i4++;
                    }
                }
            }
            if (!match) {
                this.creatorConstructorParameters = ASMUtils.lookupParameterNames(creatorConstructor2);
            }
        }
    }

    private static FieldInfo getField(List<FieldInfo> fieldList, String propertyName) {
        for (FieldInfo item : fieldList) {
            if (item.name.equals(propertyName)) {
                return item;
            }
            Field field = item.field;
            if (!(field == null || item.getAnnotation() == null || !field.getName().equals(propertyName))) {
                return item;
            }
        }
        return null;
    }

    static boolean add(List<FieldInfo> fieldList, FieldInfo field) {
        for (int i = fieldList.size() - 1; i >= 0; i--) {
            FieldInfo item = fieldList.get(i);
            if (item.name.equals(field.name) && (!item.getOnly || field.getOnly)) {
                if (item.fieldClass.isAssignableFrom(field.fieldClass)) {
                    fieldList.set(i, field);
                    return true;
                } else if (item.compareTo(field) >= 0) {
                    return false;
                } else {
                    fieldList.set(i, field);
                    return true;
                }
            }
        }
        fieldList.add(field);
        return true;
    }

    public static JavaBeanInfo build(Class<?> clazz2, Type type, PropertyNamingStrategy propertyNamingStrategy) {
        return build(clazz2, type, propertyNamingStrategy, false, TypeUtils.compatibleWithJavaBean, false);
    }

    public static JavaBeanInfo build(Class<?> clazz2, Type type, PropertyNamingStrategy propertyNamingStrategy, boolean fieldBased, boolean compatibleWithJavaBean) {
        return build(clazz2, type, propertyNamingStrategy, fieldBased, compatibleWithJavaBean, false);
    }

    /* JADX INFO: Multiple debug info for r11v1 java.lang.reflect.Constructor<?>: [D('propertyNamingStrategy' com.alibaba.fastjson.PropertyNamingStrategy), D('defaultConstructor' java.lang.reflect.Constructor<?>)] */
    /* JADX INFO: Multiple debug info for r22v2 'propertyNamingStrategy'  com.alibaba.fastjson.PropertyNamingStrategy: [D('constructors' java.lang.reflect.Constructor[]), D('propertyNamingStrategy' com.alibaba.fastjson.PropertyNamingStrategy)] */
    /* JADX INFO: Multiple debug info for r12v24 'builderClass'  java.lang.Class<?>: [D('jsonType' com.alibaba.fastjson.annotation.JSONType), D('builderClass' java.lang.Class<?>)] */
    /* JADX INFO: Multiple debug info for r11v29 'constructors'  java.lang.reflect.Constructor<?>[]: [D('constructors' java.lang.reflect.Constructor[]), D('defaultConstructor' java.lang.reflect.Constructor<?>)] */
    /* JADX INFO: Multiple debug info for r0v44 'className'  java.lang.String: [D('paramName' java.lang.String), D('className' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r2v56 java.lang.annotation.Annotation: [D('paramAnnotation' java.lang.annotation.Annotation), D('fieldAnnotation' com.alibaba.fastjson.annotation.JSONField)] */
    /* JADX INFO: Multiple debug info for r4v23 java.lang.reflect.Constructor<?>: [D('creatorConstructor' java.lang.reflect.Constructor<?>), D('constructor' java.lang.reflect.Constructor)] */
    /* JADX INFO: Multiple debug info for r0v62 java.lang.annotation.Annotation: [D('creatorConstructor' java.lang.reflect.Constructor<?>), D('paramAnnotation' java.lang.annotation.Annotation)] */
    /* JADX INFO: Multiple debug info for r1v176 java.lang.reflect.Field: [D('parameterName' java.lang.String), D('field' java.lang.reflect.Field)] */
    /* JADX INFO: Multiple debug info for r0v65 com.alibaba.fastjson.PropertyNamingStrategy: [D('jsonTypeNaming' com.alibaba.fastjson.PropertyNamingStrategy), D('propertyNamingStrategy' com.alibaba.fastjson.PropertyNamingStrategy)] */
    /* JADX WARNING: Code restructure failed: missing block: B:320:0x07eb, code lost:
        if (r8.length() < 4) goto L_0x07c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x085a, code lost:
        if (r12.startsWith("set") == false) goto L_0x0794;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:417:0x0ae2, code lost:
        if (r5.deserialize() == false) goto L_0x0ae5;
     */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0345  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0348  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x036f  */
    /* JADX WARNING: Removed duplicated region for block: B:250:0x0606  */
    /* JADX WARNING: Removed duplicated region for block: B:275:0x06ae  */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x06b2  */
    /* JADX WARNING: Removed duplicated region for block: B:284:0x06d3  */
    /* JADX WARNING: Removed duplicated region for block: B:315:0x07d5  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0a09  */
    /* JADX WARNING: Removed duplicated region for block: B:420:0x0af4  */
    /* JADX WARNING: Removed duplicated region for block: B:423:0x0b00  */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x0b74  */
    /* JADX WARNING: Removed duplicated region for block: B:438:0x0b98  */
    /* JADX WARNING: Removed duplicated region for block: B:471:0x09ea A[EDGE_INSN: B:471:0x09ea->B:378:0x09ea ?: BREAK  , SYNTHETIC] */
    public static JavaBeanInfo build(Class<?> clazz2, Type type, PropertyNamingStrategy propertyNamingStrategy, boolean fieldBased, boolean compatibleWithJavaBean, boolean jacksonCompatible) {
        Method[] methods;
        Constructor<?> creatorConstructor2;
        Constructor<?> defaultConstructor2;
        List<FieldInfo> fieldList;
        Constructor<?>[] constructors;
        Constructor<?> defaultConstructor3;
        PropertyNamingStrategy propertyNamingStrategy2;
        Field[] declaredFields;
        Constructor<?> defaultConstructor4;
        JSONType jsonType2;
        Type type2;
        Class<?> builderClass2;
        int i;
        Method buildMethod2;
        Method[] methods2;
        int length;
        int i2;
        int i3;
        int length2;
        int i4;
        boolean fieldBased2;
        Field[] declaredFields2;
        int i5;
        int i6;
        Method[] methodArr;
        int i7;
        PropertyNamingStrategy propertyNamingStrategy3;
        JSONField annotation;
        Field[] declaredFields3;
        String propertyName;
        Method buildMethod3;
        Field[] declaredFields4;
        int i8;
        Method[] methods3;
        int i9;
        Class<?> builderClass3;
        int i10;
        PropertyNamingStrategy propertyNamingStrategy4;
        Method method;
        String methodName;
        Class<?>[] types;
        Method method2;
        JSONField annotation2;
        String methodName2;
        String methodName3;
        String propertyName2;
        int i11;
        boolean z;
        Field field;
        JSONField fieldAnnotation;
        PropertyNamingStrategy propertyNamingStrategy5;
        Class<?> builderClass4;
        Field[] declaredFields5;
        Method[] methodArr2;
        String withPrefix;
        Constructor<?> defaultConstructor5;
        JSONPOJOBuilder builderAnno;
        JSONType jsonType3;
        int i12;
        int i13;
        Constructor<?>[] constructors2;
        String withPrefix2;
        Method method3;
        int parserFeatures2;
        int serialzeFeatures;
        int ordinal;
        Type type3;
        String withPrefix3;
        StringBuilder properNameBuilder;
        char c0;
        Method factoryMethod2;
        String[] paramNames;
        Constructor<?> creatorConstructor3;
        JSONField fieldAnnotation2;
        int serialzeFeatures2;
        String className;
        Field field2;
        int ordinal2;
        int parserFeatures3;
        String paramName;
        int parserFeatures4;
        char c;
        String[] lookupParameterNames;
        Constructor<?> creatorConstructor4;
        String[] paramNames2;
        Constructor<?> creatorConstructor5;
        JSONField fieldAnnotation3;
        int parserFeatures5;
        int serialzeFeatures3;
        int parserFeatures6;
        int ordinal3;
        String fieldName;
        String[] lookupParameterNames2;
        Field field3;
        PropertyNamingStrategy propertyNamingStrategy6;
        boolean z2 = jacksonCompatible;
        JSONType jsonType4 = (JSONType) TypeUtils.getAnnotation(clazz2, JSONType.class);
        PropertyNamingStrategy propertyNamingStrategy7 = (jsonType4 == null || (propertyNamingStrategy6 = jsonType4.naming()) == null || propertyNamingStrategy6 == PropertyNamingStrategy.CamelCase) ? propertyNamingStrategy : propertyNamingStrategy6;
        Class<?> builderClass5 = getBuilderClass(clazz2, jsonType4);
        Field[] declaredFields6 = clazz2.getDeclaredFields();
        Method[] methods4 = clazz2.getMethods();
        boolean kotlin2 = TypeUtils.isKotlin(clazz2);
        Constructor<?>[] constructors3 = clazz2.getDeclaredConstructors();
        Constructor<?> defaultConstructor6 = null;
        if (!kotlin2 || constructors3.length == 1) {
            if (builderClass5 == null) {
                defaultConstructor6 = getDefaultConstructor(clazz2, constructors3);
            } else {
                defaultConstructor6 = getDefaultConstructor(builderClass5, builderClass5.getDeclaredConstructors());
            }
        }
        Method buildMethod4 = null;
        Method factoryMethod3 = null;
        List<FieldInfo> fieldList2 = new ArrayList<>();
        if (fieldBased) {
            for (Class<?> currentClass = clazz2; currentClass != null; currentClass = currentClass.getSuperclass()) {
                computeFields(clazz2, type, propertyNamingStrategy7, fieldList2, currentClass.getDeclaredFields());
            }
            return new JavaBeanInfo(clazz2, builderClass5, defaultConstructor6, null, null, null, jsonType4, fieldList2);
        }
        Field[] declaredFields7 = declaredFields6;
        PropertyNamingStrategy propertyNamingStrategy8 = propertyNamingStrategy7;
        Constructor<?> defaultConstructor7 = defaultConstructor6;
        boolean isInterfaceOrAbstract = clazz2.isInterface() || Modifier.isAbstract(clazz2.getModifiers());
        if (!(defaultConstructor7 == null && builderClass5 == null) && !isInterfaceOrAbstract) {
            defaultConstructor2 = defaultConstructor7;
            fieldList = fieldList2;
            constructors = constructors3;
            methods = methods4;
            creatorConstructor2 = null;
        } else {
            Constructor<?>[] constructors4 = constructors3;
            Constructor<?> creatorConstructor6 = getCreatorConstructor(constructors4);
            if (creatorConstructor6 == null || isInterfaceOrAbstract) {
                defaultConstructor2 = defaultConstructor7;
                List<FieldInfo> fieldList3 = fieldList2;
                constructors = constructors4;
                Method[] methods5 = methods4;
                Method factoryMethod4 = getFactoryMethod(clazz2, methods5, z2);
                Method factoryMethod5 = factoryMethod4;
                if (factoryMethod4 != null) {
                    TypeUtils.setAccessible(factoryMethod5);
                    Class<?>[] types2 = factoryMethod5.getParameterTypes();
                    if (types2.length > 0) {
                        Annotation[][] paramAnnotationArrays = factoryMethod5.getParameterAnnotations();
                        String[] lookupParameterNames3 = null;
                        int i14 = 0;
                        while (i14 < types2.length) {
                            Annotation[] paramAnnotations = paramAnnotationArrays[i14];
                            JSONField fieldAnnotation4 = null;
                            int length3 = paramAnnotations.length;
                            int i15 = 0;
                            while (true) {
                                if (i15 >= length3) {
                                    creatorConstructor5 = creatorConstructor6;
                                    fieldAnnotation3 = fieldAnnotation4;
                                    break;
                                }
                                creatorConstructor5 = creatorConstructor6;
                                Annotation paramAnnotation = paramAnnotations[i15];
                                if (paramAnnotation instanceof JSONField) {
                                    fieldAnnotation3 = (JSONField) paramAnnotation;
                                    break;
                                }
                                i15++;
                                creatorConstructor6 = creatorConstructor5;
                                fieldAnnotation4 = fieldAnnotation4;
                            }
                            if (fieldAnnotation3 != null || (z2 && TypeUtils.isJacksonCreator(factoryMethod5))) {
                                String fieldName2 = null;
                                if (fieldAnnotation3 != null) {
                                    String fieldName3 = fieldAnnotation3.name();
                                    int ordinal4 = fieldAnnotation3.ordinal();
                                    serialzeFeatures3 = SerializerFeature.of(fieldAnnotation3.serialzeFeatures());
                                    parserFeatures6 = ordinal4;
                                    parserFeatures5 = Feature.of(fieldAnnotation3.parseFeatures());
                                    fieldName2 = fieldName3;
                                } else {
                                    serialzeFeatures3 = 0;
                                    parserFeatures5 = 0;
                                    parserFeatures6 = 0;
                                }
                                if (fieldName2 == null || fieldName2.length() == 0) {
                                    if (lookupParameterNames3 == null) {
                                        lookupParameterNames3 = ASMUtils.lookupParameterNames(factoryMethod5);
                                    }
                                    fieldName2 = lookupParameterNames3[i14];
                                }
                                add(fieldList3, new FieldInfo(fieldName2, clazz2, types2[i14], factoryMethod5.getGenericParameterTypes()[i14], TypeUtils.getField(clazz2, fieldName2, declaredFields7), parserFeatures6, serialzeFeatures3, parserFeatures5));
                                i14++;
                                fieldList3 = fieldList3;
                                types2 = types2;
                                factoryMethod5 = factoryMethod5;
                                creatorConstructor6 = creatorConstructor5;
                                methods5 = methods5;
                                z2 = jacksonCompatible;
                            } else {
                                throw new JSONException("illegal json creator");
                            }
                        }
                        return new JavaBeanInfo(clazz2, builderClass5, null, null, factoryMethod5, null, jsonType4, fieldList3);
                    }
                    creatorConstructor2 = creatorConstructor6;
                    factoryMethod2 = factoryMethod5;
                    methods = methods5;
                    fieldList = fieldList3;
                } else {
                    creatorConstructor2 = creatorConstructor6;
                    factoryMethod2 = factoryMethod5;
                    methods = methods5;
                    fieldList = fieldList3;
                    if (!isInterfaceOrAbstract) {
                        String className2 = clazz2.getName();
                        if (!kotlin2 || constructors.length <= 0) {
                            String[] paramNames3 = null;
                            for (Constructor<?> constructor : constructors) {
                                Class<?>[] parameterTypes = constructor.getParameterTypes();
                                if (!className2.equals("org.springframework.security.web.authentication.WebAuthenticationDetails") || parameterTypes.length != 2) {
                                    c = 0;
                                    if (!className2.equals("org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken") && parameterTypes.length == 3 && parameterTypes[c] == Object.class && parameterTypes[1] == Object.class && parameterTypes[2] == Collection.class) {
                                        creatorConstructor4 = constructor;
                                        creatorConstructor4.setAccessible(true);
                                        paramNames2 = new String[]{"principal", "credentials", "authorities"};
                                    } else if (!className2.equals("org.springframework.security.core.authority.SimpleGrantedAuthority") && parameterTypes.length == 1 && parameterTypes[c] == String.class) {
                                        creatorConstructor4 = constructor;
                                        paramNames2 = new String[]{"authority"};
                                    } else {
                                        if (!(((constructor.getModifiers() & 1) == 0 ? 1 : c) == 0 || (lookupParameterNames = ASMUtils.lookupParameterNames(constructor)) == null || lookupParameterNames.length == 0 || !(creatorConstructor2 == null || paramNames3 == null || lookupParameterNames.length > paramNames3.length))) {
                                            paramNames3 = lookupParameterNames;
                                            creatorConstructor2 = constructor;
                                        }
                                    }
                                } else {
                                    c = 0;
                                    if (parameterTypes[0] == String.class && parameterTypes[1] == String.class) {
                                        creatorConstructor4 = constructor;
                                        creatorConstructor4.setAccessible(true);
                                        paramNames2 = ASMUtils.lookupParameterNames(constructor);
                                    }
                                    if (!className2.equals("org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken")) {
                                    }
                                    if (!className2.equals("org.springframework.security.core.authority.SimpleGrantedAuthority")) {
                                    }
                                    paramNames3 = lookupParameterNames;
                                    creatorConstructor2 = constructor;
                                }
                                creatorConstructor3 = creatorConstructor4;
                                paramNames = paramNames2;
                            }
                            paramNames = paramNames3;
                            creatorConstructor3 = creatorConstructor2;
                            Class<?>[] types3 = null;
                            if (paramNames != null) {
                                types3 = creatorConstructor3.getParameterTypes();
                            }
                            Class<?>[] types4 = types3;
                            if (paramNames == null && types4.length == paramNames.length) {
                                Annotation[][] paramAnnotationArrays2 = creatorConstructor3.getParameterAnnotations();
                                int i16 = 0;
                                while (i16 < types4.length) {
                                    Annotation[] paramAnnotations2 = paramAnnotationArrays2[i16];
                                    String paramName2 = paramNames[i16];
                                    JSONField fieldAnnotation5 = null;
                                    int length4 = paramAnnotations2.length;
                                    int i17 = 0;
                                    while (true) {
                                        if (i17 >= length4) {
                                            fieldAnnotation2 = fieldAnnotation5;
                                            break;
                                        }
                                        Annotation paramAnnotation2 = paramAnnotations2[i17];
                                        if (paramAnnotation2 instanceof JSONField) {
                                            fieldAnnotation2 = (JSONField) paramAnnotation2;
                                            break;
                                        }
                                        i17++;
                                        fieldAnnotation5 = fieldAnnotation5;
                                        length4 = length4;
                                    }
                                    Class<?> fieldClass = types4[i16];
                                    Type fieldType = creatorConstructor3.getGenericParameterTypes()[i16];
                                    Field field4 = TypeUtils.getField(clazz2, paramName2, declaredFields7);
                                    if (field4 != null && fieldAnnotation2 == null) {
                                        fieldAnnotation2 = (JSONField) field4.getAnnotation(JSONField.class);
                                    }
                                    if (fieldAnnotation2 == null) {
                                        field2 = field4;
                                        if (!"org.springframework.security.core.userdetails.User".equals(className2) || !"password".equals(paramName2)) {
                                            parserFeatures4 = 0;
                                        } else {
                                            parserFeatures4 = Feature.InitStringFieldAsEmpty.mask;
                                        }
                                        className = className2;
                                        paramName = paramName2;
                                        serialzeFeatures2 = 0;
                                        ordinal2 = 0;
                                        parserFeatures3 = parserFeatures4;
                                    } else {
                                        field2 = field4;
                                        String nameAnnotated = fieldAnnotation2.name();
                                        if (nameAnnotated.length() != 0) {
                                            paramName2 = nameAnnotated;
                                        }
                                        int ordinal5 = fieldAnnotation2.ordinal();
                                        className = className2;
                                        serialzeFeatures2 = SerializerFeature.of(fieldAnnotation2.serialzeFeatures());
                                        ordinal2 = ordinal5;
                                        parserFeatures3 = Feature.of(fieldAnnotation2.parseFeatures());
                                        paramName = paramName2;
                                    }
                                    add(fieldList, new FieldInfo(paramName, clazz2, fieldClass, fieldType, field2, ordinal2, serialzeFeatures2, parserFeatures3));
                                    i16++;
                                    types4 = types4;
                                    paramNames = paramNames;
                                    creatorConstructor3 = creatorConstructor3;
                                    className2 = className;
                                }
                                creatorConstructor2 = creatorConstructor3;
                                if (!kotlin2 && !clazz2.getName().equals("javax.servlet.http.Cookie")) {
                                    return new JavaBeanInfo(clazz2, builderClass5, null, creatorConstructor2, null, null, jsonType4, fieldList);
                                }
                                factoryMethod3 = factoryMethod2;
                            } else {
                                throw new JSONException("default constructor not found. " + clazz2);
                            }
                        } else {
                            String[] paramNames4 = TypeUtils.getKoltinConstructorParameters(clazz2);
                            Constructor<?> creatorConstructor7 = TypeUtils.getKoltinConstructor(constructors, paramNames4);
                            TypeUtils.setAccessible(creatorConstructor7);
                            paramNames = paramNames4;
                            creatorConstructor3 = creatorConstructor7;
                        }
                        Class<?>[] types32 = null;
                        if (paramNames != null) {
                        }
                        Class<?>[] types42 = types32;
                        if (paramNames == null) {
                        }
                        throw new JSONException("default constructor not found. " + clazz2);
                    }
                }
                factoryMethod3 = factoryMethod2;
            } else {
                TypeUtils.setAccessible(creatorConstructor6);
                Class<?>[] types5 = creatorConstructor6.getParameterTypes();
                if (types5.length > 0) {
                    Annotation[][] paramAnnotationArrays3 = creatorConstructor6.getParameterAnnotations();
                    String[] lookupParameterNames4 = null;
                    int i18 = 0;
                    while (i18 < types5.length) {
                        Annotation[] paramAnnotations3 = paramAnnotationArrays3[i18];
                        JSONField fieldAnnotation6 = null;
                        int length5 = paramAnnotations3.length;
                        int i19 = 0;
                        while (true) {
                            if (i19 >= length5) {
                                break;
                            }
                            Annotation paramAnnotation3 = paramAnnotations3[i19];
                            if (paramAnnotation3 instanceof JSONField) {
                                fieldAnnotation6 = (JSONField) paramAnnotation3;
                                break;
                            }
                            i19++;
                        }
                        Class<?> fieldClass2 = types5[i18];
                        Type fieldType2 = creatorConstructor6.getGenericParameterTypes()[i18];
                        String fieldName4 = null;
                        Field field5 = null;
                        int serialzeFeatures4 = 0;
                        int parserFeatures7 = 0;
                        if (fieldAnnotation6 != null) {
                            field5 = TypeUtils.getField(clazz2, fieldAnnotation6.name(), declaredFields7);
                            int ordinal6 = fieldAnnotation6.ordinal();
                            int serialzeFeatures5 = SerializerFeature.of(fieldAnnotation6.serialzeFeatures());
                            ordinal3 = ordinal6;
                            int parserFeatures8 = Feature.of(fieldAnnotation6.parseFeatures());
                            fieldName4 = fieldAnnotation6.name();
                            parserFeatures7 = parserFeatures8;
                            serialzeFeatures4 = serialzeFeatures5;
                        } else {
                            ordinal3 = 0;
                        }
                        if (fieldName4 == null || fieldName4.length() == 0) {
                            if (lookupParameterNames4 == null) {
                                lookupParameterNames4 = ASMUtils.lookupParameterNames(creatorConstructor6);
                            }
                            fieldName = lookupParameterNames4[i18];
                        } else {
                            fieldName = fieldName4;
                        }
                        if (field5 == null) {
                            if (lookupParameterNames4 == null) {
                                if (kotlin2) {
                                    lookupParameterNames4 = TypeUtils.getKoltinConstructorParameters(clazz2);
                                } else {
                                    lookupParameterNames4 = ASMUtils.lookupParameterNames(creatorConstructor6);
                                }
                            }
                            if (lookupParameterNames4.length > i18) {
                                field3 = TypeUtils.getField(clazz2, lookupParameterNames4[i18], declaredFields7);
                                lookupParameterNames2 = lookupParameterNames4;
                                add(fieldList2, new FieldInfo(fieldName, clazz2, fieldClass2, fieldType2, field3, ordinal3, serialzeFeatures4, parserFeatures7));
                                i18++;
                                constructors4 = constructors4;
                                lookupParameterNames4 = lookupParameterNames2;
                                types5 = types5;
                                defaultConstructor7 = defaultConstructor7;
                            }
                        }
                        lookupParameterNames2 = lookupParameterNames4;
                        field3 = field5;
                        add(fieldList2, new FieldInfo(fieldName, clazz2, fieldClass2, fieldType2, field3, ordinal3, serialzeFeatures4, parserFeatures7));
                        i18++;
                        constructors4 = constructors4;
                        lookupParameterNames4 = lookupParameterNames2;
                        types5 = types5;
                        defaultConstructor7 = defaultConstructor7;
                    }
                }
                defaultConstructor2 = defaultConstructor7;
                constructors = constructors4;
                creatorConstructor2 = creatorConstructor6;
                fieldList = fieldList2;
                methods = methods4;
            }
        }
        if (defaultConstructor2 != null) {
            defaultConstructor3 = defaultConstructor2;
            TypeUtils.setAccessible(defaultConstructor3);
        } else {
            defaultConstructor3 = defaultConstructor2;
        }
        if (builderClass5 != null) {
            String withPrefix4 = null;
            JSONPOJOBuilder builderAnno2 = (JSONPOJOBuilder) builderClass5.getAnnotation(JSONPOJOBuilder.class);
            if (builderAnno2 != null) {
                withPrefix4 = builderAnno2.withPrefix();
            }
            if (withPrefix4 == null || withPrefix4.length() == 0) {
                withPrefix4 = "with";
            }
            String withPrefix5 = withPrefix4;
            Method[] methods6 = builderClass5.getMethods();
            int length6 = methods6.length;
            int i20 = 0;
            while (i20 < length6) {
                Method method4 = methods6[i20];
                if (!Modifier.isStatic(method4.getModifiers()) && method4.getReturnType().equals(builderClass5)) {
                    methodArr2 = methods6;
                    JSONField annotation3 = (JSONField) method4.getAnnotation(JSONField.class);
                    if (annotation3 == null) {
                        annotation3 = TypeUtils.getSuperMethodAnnotation(clazz2, method4);
                    }
                    if (annotation3 == null) {
                        method3 = method4;
                        i12 = i20;
                        i13 = length6;
                        withPrefix2 = withPrefix5;
                        builderAnno = builderAnno2;
                        defaultConstructor5 = defaultConstructor3;
                        builderClass4 = builderClass5;
                        jsonType3 = jsonType4;
                        declaredFields5 = declaredFields7;
                        propertyNamingStrategy5 = propertyNamingStrategy8;
                        type3 = type;
                        constructors2 = constructors;
                        ordinal = 0;
                        serialzeFeatures = 0;
                        parserFeatures2 = 0;
                    } else if (annotation3.deserialize()) {
                        ordinal = annotation3.ordinal();
                        serialzeFeatures = SerializerFeature.of(annotation3.serialzeFeatures());
                        parserFeatures2 = Feature.of(annotation3.parseFeatures());
                        if (annotation3.name().length() != 0) {
                            i12 = i20;
                            i13 = length6;
                            declaredFields5 = declaredFields7;
                            builderAnno = builderAnno2;
                            defaultConstructor5 = defaultConstructor3;
                            builderClass4 = builderClass5;
                            propertyNamingStrategy5 = propertyNamingStrategy8;
                            constructors2 = constructors;
                            jsonType3 = jsonType4;
                            add(fieldList, new FieldInfo(annotation3.name(), method4, null, clazz2, type, ordinal, serialzeFeatures, parserFeatures2, annotation3, null, null));
                            withPrefix = withPrefix5;
                            i20 = i12 + 1;
                            constructors = constructors2;
                            length6 = i13;
                            jsonType4 = jsonType3;
                            builderAnno2 = builderAnno;
                            defaultConstructor3 = defaultConstructor5;
                            withPrefix5 = withPrefix;
                            methods6 = methodArr2;
                            declaredFields7 = declaredFields5;
                            builderClass5 = builderClass4;
                            propertyNamingStrategy8 = propertyNamingStrategy5;
                        } else {
                            method3 = method4;
                            i12 = i20;
                            i13 = length6;
                            withPrefix2 = withPrefix5;
                            builderAnno = builderAnno2;
                            defaultConstructor5 = defaultConstructor3;
                            builderClass4 = builderClass5;
                            jsonType3 = jsonType4;
                            declaredFields5 = declaredFields7;
                            propertyNamingStrategy5 = propertyNamingStrategy8;
                            type3 = type;
                            constructors2 = constructors;
                        }
                    }
                    String methodName4 = method3.getName();
                    if (methodName4.startsWith("set")) {
                        if (methodName4.length() > 3) {
                            properNameBuilder = new StringBuilder(methodName4.substring(3));
                            withPrefix3 = withPrefix2;
                            c0 = properNameBuilder.charAt(0);
                            if (Character.isUpperCase(c0)) {
                                properNameBuilder.setCharAt(0, Character.toLowerCase(c0));
                                withPrefix = withPrefix3;
                                add(fieldList, new FieldInfo(properNameBuilder.toString(), method3, null, clazz2, type3, ordinal, serialzeFeatures, parserFeatures2, annotation3, null, null));
                                i20 = i12 + 1;
                                constructors = constructors2;
                                length6 = i13;
                                jsonType4 = jsonType3;
                                builderAnno2 = builderAnno;
                                defaultConstructor3 = defaultConstructor5;
                                withPrefix5 = withPrefix;
                                methods6 = methodArr2;
                                declaredFields7 = declaredFields5;
                                builderClass5 = builderClass4;
                                propertyNamingStrategy8 = propertyNamingStrategy5;
                            }
                            withPrefix = withPrefix3;
                            i20 = i12 + 1;
                            constructors = constructors2;
                            length6 = i13;
                            jsonType4 = jsonType3;
                            builderAnno2 = builderAnno;
                            defaultConstructor3 = defaultConstructor5;
                            withPrefix5 = withPrefix;
                            methods6 = methodArr2;
                            declaredFields7 = declaredFields5;
                            builderClass5 = builderClass4;
                            propertyNamingStrategy8 = propertyNamingStrategy5;
                        }
                    }
                    withPrefix3 = withPrefix2;
                    if (methodName4.startsWith(withPrefix3) && methodName4.length() > withPrefix3.length()) {
                        properNameBuilder = new StringBuilder(methodName4.substring(withPrefix3.length()));
                        c0 = properNameBuilder.charAt(0);
                        if (Character.isUpperCase(c0)) {
                        }
                    }
                    withPrefix = withPrefix3;
                    i20 = i12 + 1;
                    constructors = constructors2;
                    length6 = i13;
                    jsonType4 = jsonType3;
                    builderAnno2 = builderAnno;
                    defaultConstructor3 = defaultConstructor5;
                    withPrefix5 = withPrefix;
                    methods6 = methodArr2;
                    declaredFields7 = declaredFields5;
                    builderClass5 = builderClass4;
                    propertyNamingStrategy8 = propertyNamingStrategy5;
                } else {
                    methodArr2 = methods6;
                }
                i12 = i20;
                i13 = length6;
                withPrefix = withPrefix5;
                builderAnno = builderAnno2;
                defaultConstructor5 = defaultConstructor3;
                builderClass4 = builderClass5;
                jsonType3 = jsonType4;
                declaredFields5 = declaredFields7;
                propertyNamingStrategy5 = propertyNamingStrategy8;
                constructors2 = constructors;
                i20 = i12 + 1;
                constructors = constructors2;
                length6 = i13;
                jsonType4 = jsonType3;
                builderAnno2 = builderAnno;
                defaultConstructor3 = defaultConstructor5;
                withPrefix5 = withPrefix;
                methods6 = methodArr2;
                declaredFields7 = declaredFields5;
                builderClass5 = builderClass4;
                propertyNamingStrategy8 = propertyNamingStrategy5;
            }
            defaultConstructor4 = defaultConstructor3;
            jsonType2 = jsonType4;
            declaredFields = declaredFields7;
            propertyNamingStrategy2 = propertyNamingStrategy8;
            type2 = type;
            builderClass2 = builderClass5;
            if (builderClass2 != null) {
                JSONPOJOBuilder builderAnnotation = (JSONPOJOBuilder) builderClass2.getAnnotation(JSONPOJOBuilder.class);
                String buildMethodName = null;
                if (builderAnnotation != null) {
                    buildMethodName = builderAnnotation.buildMethod();
                }
                if (buildMethodName == null || buildMethodName.length() == 0) {
                    buildMethodName = "build";
                }
                i = 0;
                try {
                    buildMethod4 = builderClass2.getMethod(buildMethodName, new Class[0]);
                } catch (NoSuchMethodException | SecurityException e) {
                }
                if (buildMethod4 == null) {
                    try {
                        buildMethod2 = builderClass2.getMethod("create", new Class[0]);
                    } catch (NoSuchMethodException | SecurityException e2) {
                    }
                    if (buildMethod2 == null) {
                        TypeUtils.setAccessible(buildMethod2);
                        methods2 = methods;
                        length = methods2.length;
                        i2 = i;
                        while (true) {
                            i3 = 4;
                            if (i2 < length) {
                                break;
                            }
                            Method method5 = methods2[i2];
                            int ordinal7 = 0;
                            int serialzeFeatures6 = 0;
                            int parserFeatures9 = 0;
                            String methodName5 = method5.getName();
                            if (!Modifier.isStatic(method5.getModifiers())) {
                                Class<?> returnType = method5.getReturnType();
                                if ((returnType.equals(Void.TYPE) || returnType.equals(method5.getDeclaringClass())) && method5.getDeclaringClass() != Object.class) {
                                    Class<?>[] types6 = method5.getParameterTypes();
                                    if (types6.length == 0) {
                                        buildMethod3 = buildMethod2;
                                        i10 = i2;
                                        i9 = length;
                                        methods3 = methods2;
                                        i8 = i;
                                        builderClass3 = builderClass2;
                                        declaredFields4 = declaredFields;
                                        propertyNamingStrategy4 = propertyNamingStrategy2;
                                    } else if (types6.length > 2) {
                                        buildMethod3 = buildMethod2;
                                        i10 = i2;
                                        i9 = length;
                                        methods3 = methods2;
                                        i8 = i;
                                        builderClass3 = builderClass2;
                                        declaredFields4 = declaredFields;
                                        propertyNamingStrategy4 = propertyNamingStrategy2;
                                    } else {
                                        JSONField annotation4 = (JSONField) method5.getAnnotation(JSONField.class);
                                        if (annotation4 == null || types6.length != 2) {
                                            types = types6;
                                            methodName = methodName5;
                                            method = method5;
                                            i10 = i2;
                                            i9 = length;
                                            methods3 = methods2;
                                            builderClass3 = builderClass2;
                                            if (types.length == 1) {
                                                if (annotation4 == null) {
                                                    method2 = method;
                                                    annotation2 = TypeUtils.getSuperMethodAnnotation(clazz2, method2);
                                                } else {
                                                    method2 = method;
                                                    annotation2 = annotation4;
                                                }
                                                if (annotation2 == null) {
                                                    methodName2 = methodName;
                                                } else {
                                                    methodName2 = methodName;
                                                }
                                                if (annotation2 != null) {
                                                    if (annotation2.deserialize()) {
                                                        ordinal7 = annotation2.ordinal();
                                                        serialzeFeatures6 = SerializerFeature.of(annotation2.serialzeFeatures());
                                                        parserFeatures9 = Feature.of(annotation2.parseFeatures());
                                                        if (annotation2.name().length() != 0) {
                                                            add(fieldList, new FieldInfo(annotation2.name(), method2, null, clazz2, type2, ordinal7, serialzeFeatures6, parserFeatures9, annotation2, null, null));
                                                        }
                                                    }
                                                }
                                                if (annotation2 == null) {
                                                    methodName3 = methodName2;
                                                } else {
                                                    methodName3 = methodName2;
                                                }
                                                char c3 = methodName3.charAt(3);
                                                if (Character.isUpperCase(c3) || c3 > 512) {
                                                    propertyName2 = TypeUtils.compatibleWithJavaBean ? TypeUtils.decapitalize(methodName3.substring(3)) : Character.toLowerCase(methodName3.charAt(3)) + methodName3.substring(4);
                                                } else if (c3 == '_') {
                                                    propertyName2 = methodName3.substring(4);
                                                } else if (c3 == 'f') {
                                                    propertyName2 = methodName3.substring(3);
                                                } else if (methodName3.length() >= 5 && Character.isUpperCase(methodName3.charAt(4))) {
                                                    propertyName2 = TypeUtils.decapitalize(methodName3.substring(3));
                                                }
                                                Field field6 = TypeUtils.getField(clazz2, propertyName2, declaredFields);
                                                if (field6 == null) {
                                                    i11 = 0;
                                                    if (types[0] == Boolean.TYPE) {
                                                        StringBuilder sb = new StringBuilder();
                                                        sb.append("is");
                                                        sb.append(Character.toUpperCase(propertyName2.charAt(0)));
                                                        z = true;
                                                        sb.append(propertyName2.substring(1));
                                                        field6 = TypeUtils.getField(clazz2, sb.toString(), declaredFields);
                                                    } else {
                                                        z = true;
                                                    }
                                                } else {
                                                    z = true;
                                                    i11 = 0;
                                                }
                                                if (field6 != null) {
                                                    JSONField fieldAnnotation7 = (JSONField) field6.getAnnotation(JSONField.class);
                                                    if (fieldAnnotation7 != null) {
                                                        if (!fieldAnnotation7.deserialize()) {
                                                            buildMethod3 = buildMethod2;
                                                            i8 = i11;
                                                            declaredFields4 = declaredFields;
                                                        } else {
                                                            ordinal7 = fieldAnnotation7.ordinal();
                                                            serialzeFeatures6 = SerializerFeature.of(fieldAnnotation7.serialzeFeatures());
                                                            parserFeatures9 = Feature.of(fieldAnnotation7.parseFeatures());
                                                            if (fieldAnnotation7.name().length() != 0) {
                                                                i8 = i11;
                                                                declaredFields4 = declaredFields;
                                                                add(fieldList, new FieldInfo(fieldAnnotation7.name(), method2, field6, clazz2, type2, ordinal7, serialzeFeatures6, parserFeatures9, annotation2, fieldAnnotation7, null));
                                                                buildMethod3 = buildMethod2;
                                                            }
                                                        }
                                                        propertyNamingStrategy4 = propertyNamingStrategy2;
                                                    }
                                                    fieldAnnotation = fieldAnnotation7;
                                                    field = field6;
                                                    i8 = i11;
                                                    declaredFields4 = declaredFields;
                                                } else {
                                                    field = field6;
                                                    i8 = i11;
                                                    declaredFields4 = declaredFields;
                                                    fieldAnnotation = null;
                                                }
                                                if (propertyNamingStrategy2 != null) {
                                                    propertyName2 = propertyNamingStrategy2.translate(propertyName2);
                                                }
                                                buildMethod3 = buildMethod2;
                                                propertyNamingStrategy4 = propertyNamingStrategy2;
                                                add(fieldList, new FieldInfo(propertyName2, method2, field, clazz2, type2, ordinal7, serialzeFeatures6, parserFeatures9, annotation2, fieldAnnotation, null));
                                            }
                                            buildMethod3 = buildMethod2;
                                            declaredFields4 = declaredFields;
                                            propertyNamingStrategy4 = propertyNamingStrategy2;
                                            i8 = 0;
                                        } else if (types6[i] == String.class && types6[1] == Object.class) {
                                            i10 = i2;
                                            i9 = length;
                                            methods3 = methods2;
                                            builderClass3 = builderClass2;
                                            add(fieldList, new FieldInfo("", method5, null, clazz2, type2, 0, 0, 0, annotation4, null, null));
                                        } else {
                                            types = types6;
                                            methodName = methodName5;
                                            method = method5;
                                            i10 = i2;
                                            i9 = length;
                                            methods3 = methods2;
                                            builderClass3 = builderClass2;
                                            if (types.length == 1) {
                                            }
                                            buildMethod3 = buildMethod2;
                                            declaredFields4 = declaredFields;
                                            propertyNamingStrategy4 = propertyNamingStrategy2;
                                            i8 = 0;
                                        }
                                        buildMethod3 = buildMethod2;
                                        declaredFields4 = declaredFields;
                                        propertyNamingStrategy4 = propertyNamingStrategy2;
                                        i8 = 0;
                                    }
                                    i2 = i10 + 1;
                                    propertyNamingStrategy2 = propertyNamingStrategy4;
                                    builderClass2 = builderClass3;
                                    length = i9;
                                    methods2 = methods3;
                                    i = i8;
                                    declaredFields = declaredFields4;
                                    buildMethod2 = buildMethod3;
                                    type2 = type;
                                }
                            }
                            buildMethod3 = buildMethod2;
                            i10 = i2;
                            i9 = length;
                            methods3 = methods2;
                            i8 = i;
                            builderClass3 = builderClass2;
                            declaredFields4 = declaredFields;
                            propertyNamingStrategy4 = propertyNamingStrategy2;
                            i2 = i10 + 1;
                            propertyNamingStrategy2 = propertyNamingStrategy4;
                            builderClass2 = builderClass3;
                            length = i9;
                            methods2 = methods3;
                            i = i8;
                            declaredFields = declaredFields4;
                            buildMethod2 = buildMethod3;
                            type2 = type;
                        }
                        Field[] declaredFields8 = declaredFields;
                        PropertyNamingStrategy propertyNamingStrategy9 = propertyNamingStrategy2;
                        Type type4 = type;
                        computeFields(clazz2, type4, propertyNamingStrategy9, fieldList, clazz2.getFields());
                        Method[] methods7 = clazz2.getMethods();
                        length2 = methods7.length;
                        i4 = i;
                        while (i4 < length2) {
                            Method method6 = methods7[i4];
                            String methodName6 = method6.getName();
                            if (methodName6.length() < i3 || Modifier.isStatic(method6.getModifiers())) {
                                i6 = i3;
                                i5 = i4;
                                i7 = length2;
                                methodArr = methods7;
                                propertyNamingStrategy3 = propertyNamingStrategy9;
                                declaredFields2 = declaredFields8;
                            } else if (builderClass2 != null || !methodName6.startsWith("get")) {
                                i6 = i3;
                                i5 = i4;
                                i7 = length2;
                                methodArr = methods7;
                                propertyNamingStrategy3 = propertyNamingStrategy9;
                                declaredFields2 = declaredFields8;
                                i4 = i5 + 1;
                                propertyNamingStrategy9 = propertyNamingStrategy3;
                                length2 = i7;
                                methods7 = methodArr;
                                i3 = i6;
                                declaredFields8 = declaredFields2;
                                type4 = type;
                            } else {
                                if (!Character.isUpperCase(methodName6.charAt(3))) {
                                    i6 = i3;
                                    i5 = i4;
                                    i7 = length2;
                                    methodArr = methods7;
                                    propertyNamingStrategy3 = propertyNamingStrategy9;
                                    declaredFields2 = declaredFields8;
                                } else if (method6.getParameterTypes().length == 0 && ((Collection.class.isAssignableFrom(method6.getReturnType()) || Map.class.isAssignableFrom(method6.getReturnType()) || AtomicBoolean.class == method6.getReturnType() || AtomicInteger.class == method6.getReturnType() || AtomicLong.class == method6.getReturnType()) && ((annotation = (JSONField) method6.getAnnotation(JSONField.class)) == null || !annotation.deserialize()))) {
                                    if (annotation == null || annotation.name().length() <= 0) {
                                        propertyName = Character.toLowerCase(methodName6.charAt(3)) + methodName6.substring(i3);
                                        declaredFields3 = declaredFields8;
                                        Field field7 = TypeUtils.getField(clazz2, propertyName, declaredFields3);
                                        if (field7 != null) {
                                            JSONField fieldAnnotation8 = (JSONField) field7.getAnnotation(JSONField.class);
                                            if (fieldAnnotation8 != null) {
                                            }
                                        }
                                        if (propertyNamingStrategy9 != null) {
                                            propertyName = propertyNamingStrategy9.translate(propertyName);
                                        }
                                        if (getField(fieldList, propertyName) == null) {
                                            declaredFields2 = declaredFields3;
                                            i6 = i3;
                                            i5 = i4;
                                            i7 = length2;
                                            methodArr = methods7;
                                            propertyNamingStrategy3 = propertyNamingStrategy9;
                                            add(fieldList, new FieldInfo(propertyName, method6, null, clazz2, type4, 0, 0, 0, annotation, null, null));
                                        }
                                    } else {
                                        propertyName = annotation.name();
                                        declaredFields3 = declaredFields8;
                                        if (propertyNamingStrategy9 != null) {
                                        }
                                        if (getField(fieldList, propertyName) == null) {
                                        }
                                    }
                                    declaredFields2 = declaredFields3;
                                    i6 = i3;
                                    i5 = i4;
                                    i7 = length2;
                                    methodArr = methods7;
                                    propertyNamingStrategy3 = propertyNamingStrategy9;
                                } else {
                                    i6 = i3;
                                    i5 = i4;
                                    i7 = length2;
                                    methodArr = methods7;
                                    propertyNamingStrategy3 = propertyNamingStrategy9;
                                    declaredFields2 = declaredFields8;
                                }
                                i4 = i5 + 1;
                                propertyNamingStrategy9 = propertyNamingStrategy3;
                                length2 = i7;
                                methods7 = methodArr;
                                i3 = i6;
                                declaredFields8 = declaredFields2;
                                type4 = type;
                            }
                            i4 = i5 + 1;
                            propertyNamingStrategy9 = propertyNamingStrategy3;
                            length2 = i7;
                            methods7 = methodArr;
                            i3 = i6;
                            declaredFields8 = declaredFields2;
                            type4 = type;
                        }
                        if (fieldList.size() != 0) {
                            if (TypeUtils.isXmlField(clazz2)) {
                                fieldBased2 = true;
                            } else {
                                fieldBased2 = fieldBased;
                            }
                            if (fieldBased2) {
                                for (Class<?> currentClass2 = clazz2; currentClass2 != null; currentClass2 = currentClass2.getSuperclass()) {
                                    computeFields(clazz2, type, propertyNamingStrategy9, fieldList, declaredFields8);
                                }
                            }
                        }
                        return new JavaBeanInfo(clazz2, builderClass2, defaultConstructor4, creatorConstructor2, factoryMethod3, buildMethod2, jsonType2, fieldList);
                    }
                    throw new JSONException("buildMethod not found.");
                }
                buildMethod2 = buildMethod4;
                if (buildMethod2 == null) {
                }
            } else {
                i = 0;
            }
        } else {
            defaultConstructor4 = defaultConstructor3;
            jsonType2 = jsonType4;
            declaredFields = declaredFields7;
            propertyNamingStrategy2 = propertyNamingStrategy8;
            type2 = type;
            builderClass2 = builderClass5;
            i = 0;
        }
        buildMethod2 = null;
        methods2 = methods;
        length = methods2.length;
        i2 = i;
        while (true) {
            i3 = 4;
            if (i2 < length) {
            }
            i2 = i10 + 1;
            propertyNamingStrategy2 = propertyNamingStrategy4;
            builderClass2 = builderClass3;
            length = i9;
            methods2 = methods3;
            i = i8;
            declaredFields = declaredFields4;
            buildMethod2 = buildMethod3;
            type2 = type;
        }
        Field[] declaredFields82 = declaredFields;
        PropertyNamingStrategy propertyNamingStrategy92 = propertyNamingStrategy2;
        Type type42 = type;
        computeFields(clazz2, type42, propertyNamingStrategy92, fieldList, clazz2.getFields());
        Method[] methods72 = clazz2.getMethods();
        length2 = methods72.length;
        i4 = i;
        while (i4 < length2) {
        }
        if (fieldList.size() != 0) {
        }
        return new JavaBeanInfo(clazz2, builderClass2, defaultConstructor4, creatorConstructor2, factoryMethod3, buildMethod2, jsonType2, fieldList);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004b, code lost:
        if ((java.util.Map.class.isAssignableFrom(r5) || java.util.Collection.class.isAssignableFrom(r5) || java.util.concurrent.atomic.AtomicLong.class.equals(r5) || java.util.concurrent.atomic.AtomicInteger.class.equals(r5) || java.util.concurrent.atomic.AtomicBoolean.class.equals(r5)) == false) goto L_0x0013;
     */
    private static void computeFields(Class<?> clazz2, Type type, PropertyNamingStrategy propertyNamingStrategy, List<FieldInfo> fieldList, Field[] fields2) {
        String propertyName;
        for (Field field : fields2) {
            int modifiers = field.getModifiers();
            if ((modifiers & 8) == 0) {
                if ((modifiers & 16) != 0) {
                    Class<?> fieldType = field.getType();
                }
                boolean contains = false;
                Iterator<FieldInfo> it = fieldList.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (it.next().name.equals(field.getName())) {
                            contains = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!contains) {
                    int ordinal = 0;
                    int serialzeFeatures = 0;
                    int parserFeatures2 = 0;
                    String propertyName2 = field.getName();
                    JSONField fieldAnnotation = (JSONField) field.getAnnotation(JSONField.class);
                    if (fieldAnnotation != null) {
                        if (fieldAnnotation.deserialize()) {
                            ordinal = fieldAnnotation.ordinal();
                            serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                            parserFeatures2 = Feature.of(fieldAnnotation.parseFeatures());
                            if (fieldAnnotation.name().length() != 0) {
                                propertyName2 = fieldAnnotation.name();
                            }
                        }
                    }
                    if (propertyNamingStrategy != null) {
                        propertyName = propertyNamingStrategy.translate(propertyName2);
                    } else {
                        propertyName = propertyName2;
                    }
                    add(fieldList, new FieldInfo(propertyName, null, field, clazz2, type, ordinal, serialzeFeatures, parserFeatures2, null, fieldAnnotation, null));
                }
            }
        }
    }

    static Constructor<?> getDefaultConstructor(Class<?> clazz2, Constructor<?>[] constructors) {
        if (Modifier.isAbstract(clazz2.getModifiers())) {
            return null;
        }
        Constructor<?> defaultConstructor2 = null;
        int length = constructors.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Constructor<?> constructor = constructors[i];
            if (constructor.getParameterTypes().length == 0) {
                defaultConstructor2 = constructor;
                break;
            }
            i++;
        }
        if (defaultConstructor2 != null || !clazz2.isMemberClass() || Modifier.isStatic(clazz2.getModifiers())) {
            return defaultConstructor2;
        }
        for (Constructor<?> constructor2 : constructors) {
            Class<?>[] types = constructor2.getParameterTypes();
            if (types.length == 1 && types[0].equals(clazz2.getDeclaringClass())) {
                return constructor2;
            }
        }
        return defaultConstructor2;
    }

    public static Constructor<?> getCreatorConstructor(Constructor[] constructors) {
        Constructor constructor = null;
        for (Constructor constructor2 : constructors) {
            if (((JSONCreator) constructor2.getAnnotation(JSONCreator.class)) != null) {
                if (constructor == null) {
                    constructor = constructor2;
                } else {
                    throw new JSONException("multi-JSONCreator");
                }
            }
        }
        if (constructor != null) {
            return constructor;
        }
        for (Constructor constructor3 : constructors) {
            Annotation[][] paramAnnotationArrays = constructor3.getParameterAnnotations();
            if (paramAnnotationArrays.length != 0) {
                boolean match = true;
                int length = paramAnnotationArrays.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    Annotation[] paramAnnotationArray = paramAnnotationArrays[i];
                    boolean paramMatch = false;
                    int length2 = paramAnnotationArray.length;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length2) {
                            break;
                        } else if (paramAnnotationArray[i2] instanceof JSONField) {
                            paramMatch = true;
                            break;
                        } else {
                            i2++;
                        }
                    }
                    if (!paramMatch) {
                        match = false;
                        break;
                    }
                    i++;
                }
                if (!match) {
                    continue;
                } else if (constructor == null) {
                    constructor = constructor3;
                } else {
                    throw new JSONException("multi-JSONCreator");
                }
            }
        }
        if (constructor != null) {
            return constructor;
        }
        return constructor;
    }

    private static Method getFactoryMethod(Class<?> clazz2, Method[] methods, boolean jacksonCompatible) {
        Method factoryMethod2 = null;
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()) && clazz2.isAssignableFrom(method.getReturnType()) && ((JSONCreator) method.getAnnotation(JSONCreator.class)) != null) {
                if (factoryMethod2 == null) {
                    factoryMethod2 = method;
                } else {
                    throw new JSONException("multi-JSONCreator");
                }
            }
        }
        if (factoryMethod2 != null || !jacksonCompatible) {
            return factoryMethod2;
        }
        for (Method method2 : methods) {
            if (TypeUtils.isJacksonCreator(method2)) {
                return method2;
            }
        }
        return factoryMethod2;
    }

    public static Class<?> getBuilderClass(JSONType type) {
        return getBuilderClass(null, type);
    }

    public static Class<?> getBuilderClass(Class<?> clazz2, JSONType type) {
        Class<?> builderClass2;
        if (clazz2 != null && clazz2.getName().equals("org.springframework.security.web.savedrequest.DefaultSavedRequest")) {
            return TypeUtils.loadClass("org.springframework.security.web.savedrequest.DefaultSavedRequest$Builder");
        }
        if (type == null || (builderClass2 = type.builder()) == Void.class) {
            return null;
        }
        return builderClass2;
    }
}
