package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONStreamAware;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.deserializer.Jdk8DateCodec;
import com.alibaba.fastjson.parser.deserializer.OptionalCodec;
import com.alibaba.fastjson.spi.Module;
import com.alibaba.fastjson.support.moneta.MonetaCodec;
import com.alibaba.fastjson.support.springfox.SwaggerJsonSerializer;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.IdentityHashMap;
import com.alibaba.fastjson.util.ServiceLoader;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.File;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;
import org.w3c.dom.Node;

public class SerializeConfig {
    private static boolean awtError = false;
    public static final SerializeConfig globalInstance = new SerializeConfig();
    private static boolean guavaError = false;
    private static boolean jdk8Error = false;
    private static boolean jodaError = false;
    private static boolean jsonnullError = false;
    private static boolean oracleJdbcError = false;
    private static boolean springfoxError = false;
    private boolean asm;
    private ASMSerializerFactory asmFactory;
    private long[] denyClasses;
    private final boolean fieldBased;
    private List<Module> modules;
    public PropertyNamingStrategy propertyNamingStrategy;
    private final IdentityHashMap<Type, ObjectSerializer> serializers;
    protected String typeKey;

    public String getTypeKey() {
        return this.typeKey;
    }

    public void setTypeKey(String typeKey2) {
        this.typeKey = typeKey2;
    }

    private final JavaBeanSerializer createASMSerializer(SerializeBeanInfo beanInfo) throws Exception {
        JavaBeanSerializer serializer = this.asmFactory.createJavaBeanSerializer(beanInfo);
        for (int i = 0; i < serializer.sortedGetters.length; i++) {
            Class<?> fieldClass = serializer.sortedGetters[i].fieldInfo.fieldClass;
            if (fieldClass.isEnum() && !(getObjectWriter(fieldClass) instanceof EnumSerializer)) {
                serializer.writeDirect = false;
            }
        }
        return serializer;
    }

    public final ObjectSerializer createJavaBeanSerializer(Class<?> clazz) {
        String className = clazz.getName();
        if (Arrays.binarySearch(this.denyClasses, TypeUtils.fnv1a_64(className)) < 0) {
            SerializeBeanInfo beanInfo = TypeUtils.buildBeanInfo(clazz, null, this.propertyNamingStrategy, this.fieldBased);
            if (beanInfo.fields.length != 0 || !Iterable.class.isAssignableFrom(clazz)) {
                return createJavaBeanSerializer(beanInfo);
            }
            return MiscCodec.instance;
        }
        throw new JSONException("not support class : " + className);
    }

    /* JADX INFO: Multiple debug info for r6v0 java.lang.Class<?>: [D('clazz' java.lang.Class<?>), D('serializerClass' java.lang.Class<?>)] */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0164, code lost:
        r5 = false;
     */
    public ObjectSerializer createJavaBeanSerializer(SerializeBeanInfo beanInfo) {
        JSONType jsonType;
        FieldInfo[] fieldInfoArr;
        JSONType jsonType2 = beanInfo.jsonType;
        boolean asm2 = this.asm && !this.fieldBased;
        if (jsonType2 != null) {
            Class<?> serializerClass = jsonType2.serializer();
            if (serializerClass != Void.class) {
                try {
                    Object seralizer = serializerClass.newInstance();
                    if (seralizer instanceof ObjectSerializer) {
                        return (ObjectSerializer) seralizer;
                    }
                } catch (Throwable th) {
                }
            }
            if (!jsonType2.asm()) {
                asm2 = false;
            }
            if (asm2) {
                SerializerFeature[] serialzeFeatures = jsonType2.serialzeFeatures();
                int length = serialzeFeatures.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    SerializerFeature feature = serialzeFeatures[i];
                    if (SerializerFeature.WriteNonStringValueAsString == feature || SerializerFeature.WriteEnumUsingToString == feature || SerializerFeature.NotWriteDefaultValue == feature || SerializerFeature.BrowserCompatible == feature) {
                        asm2 = false;
                    } else {
                        i++;
                    }
                }
                asm2 = false;
            }
            if (asm2 && jsonType2.serialzeFilters().length != 0) {
                asm2 = false;
            }
        }
        Class<?> serializerClass2 = beanInfo.beanType;
        if (!Modifier.isPublic(beanInfo.beanType.getModifiers())) {
            return new JavaBeanSerializer(beanInfo);
        }
        if ((asm2 && this.asmFactory.classLoader.isExternalClass(serializerClass2)) || serializerClass2 == Serializable.class || serializerClass2 == Object.class) {
            asm2 = false;
        }
        if (asm2 && !ASMUtils.checkName(serializerClass2.getSimpleName())) {
            asm2 = false;
        }
        if (asm2 && beanInfo.beanType.isInterface()) {
            asm2 = false;
        }
        if (asm2) {
            FieldInfo[] fieldInfoArr2 = beanInfo.fields;
            int length2 = fieldInfoArr2.length;
            boolean asm3 = asm2;
            int i2 = 0;
            while (true) {
                if (i2 < length2) {
                    FieldInfo fieldInfo = fieldInfoArr2[i2];
                    Field field = fieldInfo.field;
                    if (field != null && !field.getType().equals(fieldInfo.fieldClass)) {
                        asm2 = false;
                        break;
                    }
                    Method method = fieldInfo.method;
                    if (method != null && !method.getReturnType().equals(fieldInfo.fieldClass)) {
                        asm2 = false;
                        break;
                    }
                    JSONField annotation = fieldInfo.getAnnotation();
                    if (annotation == null) {
                        fieldInfoArr = fieldInfoArr2;
                        jsonType = jsonType2;
                    } else {
                        String format = annotation.format();
                        if (format.length() == 0 || (fieldInfo.fieldClass == String.class && "trim".equals(format))) {
                            if (ASMUtils.checkName(annotation.name()) && !annotation.jsonDirect() && annotation.serializeUsing() == Void.class) {
                                if (annotation.unwrapped()) {
                                    break;
                                }
                                SerializerFeature[] serialzeFeatures2 = annotation.serialzeFeatures();
                                int length3 = serialzeFeatures2.length;
                                int i3 = 0;
                                while (true) {
                                    if (i3 >= length3) {
                                        fieldInfoArr = fieldInfoArr2;
                                        jsonType = jsonType2;
                                        break;
                                    }
                                    fieldInfoArr = fieldInfoArr2;
                                    SerializerFeature feature2 = serialzeFeatures2[i3];
                                    jsonType = jsonType2;
                                    if (SerializerFeature.WriteNonStringValueAsString == feature2 || SerializerFeature.WriteEnumUsingToString == feature2 || SerializerFeature.NotWriteDefaultValue == feature2 || SerializerFeature.BrowserCompatible == feature2 || SerializerFeature.WriteClassName == feature2) {
                                        asm3 = false;
                                    } else {
                                        i3++;
                                        fieldInfoArr2 = fieldInfoArr;
                                        jsonType2 = jsonType;
                                    }
                                }
                                asm3 = false;
                                if (TypeUtils.isAnnotationPresentOneToMany(method) || TypeUtils.isAnnotationPresentManyToMany(method)) {
                                    asm2 = false;
                                }
                            }
                        }
                    }
                    i2++;
                    fieldInfoArr2 = fieldInfoArr;
                    jsonType2 = jsonType;
                } else {
                    asm2 = asm3;
                    break;
                }
            }
            asm2 = false;
        }
        if (asm2) {
            try {
                ObjectSerializer asmSerializer = createASMSerializer(beanInfo);
                if (asmSerializer != null) {
                    return asmSerializer;
                }
            } catch (ClassCastException | ClassFormatError | ClassNotFoundException e) {
            } catch (OutOfMemoryError e2) {
                if (e2.getMessage().indexOf("Metaspace") != -1) {
                    throw e2;
                }
            } catch (Throwable e3) {
                throw new JSONException("create asm serializer error, verson 1.2.58, class " + serializerClass2, e3);
            }
        }
        return new JavaBeanSerializer(beanInfo);
    }

    public boolean isAsmEnable() {
        return this.asm;
    }

    public void setAsmEnable(boolean asmEnable) {
        if (!ASMUtils.IS_ANDROID) {
            this.asm = asmEnable;
        }
    }

    public static SerializeConfig getGlobalInstance() {
        return globalInstance;
    }

    public SerializeConfig() {
        this((int) IdentityHashMap.DEFAULT_SIZE);
    }

    public SerializeConfig(boolean fieldBase) {
        this(IdentityHashMap.DEFAULT_SIZE, fieldBase);
    }

    public SerializeConfig(int tableSize) {
        this(tableSize, false);
    }

    public SerializeConfig(int tableSize, boolean fieldBase) {
        this.asm = !ASMUtils.IS_ANDROID;
        this.typeKey = JSON.DEFAULT_TYPE_KEY;
        this.denyClasses = new long[]{4165360493669296979L, 4446674157046724083L};
        this.modules = new ArrayList();
        this.fieldBased = fieldBase;
        this.serializers = new IdentityHashMap<>(tableSize);
        try {
            if (this.asm) {
                this.asmFactory = new ASMSerializerFactory();
            }
        } catch (Throwable th) {
            this.asm = false;
        }
        initSerializers();
    }

    private void initSerializers() {
        put(Boolean.class, (ObjectSerializer) BooleanCodec.instance);
        put(Character.class, (ObjectSerializer) CharacterCodec.instance);
        put(Byte.class, (ObjectSerializer) IntegerCodec.instance);
        put(Short.class, (ObjectSerializer) IntegerCodec.instance);
        put(Integer.class, (ObjectSerializer) IntegerCodec.instance);
        put(Long.class, (ObjectSerializer) LongCodec.instance);
        put(Float.class, (ObjectSerializer) FloatCodec.instance);
        put(Double.class, (ObjectSerializer) DoubleSerializer.instance);
        put(BigDecimal.class, (ObjectSerializer) BigDecimalCodec.instance);
        put(BigInteger.class, (ObjectSerializer) BigIntegerCodec.instance);
        put(String.class, (ObjectSerializer) StringCodec.instance);
        put(byte[].class, (ObjectSerializer) PrimitiveArraySerializer.instance);
        put(short[].class, (ObjectSerializer) PrimitiveArraySerializer.instance);
        put(int[].class, (ObjectSerializer) PrimitiveArraySerializer.instance);
        put(long[].class, (ObjectSerializer) PrimitiveArraySerializer.instance);
        put(float[].class, (ObjectSerializer) PrimitiveArraySerializer.instance);
        put(double[].class, (ObjectSerializer) PrimitiveArraySerializer.instance);
        put(boolean[].class, (ObjectSerializer) PrimitiveArraySerializer.instance);
        put(char[].class, (ObjectSerializer) PrimitiveArraySerializer.instance);
        put(Object[].class, (ObjectSerializer) ObjectArrayCodec.instance);
        put(Class.class, (ObjectSerializer) MiscCodec.instance);
        put(SimpleDateFormat.class, (ObjectSerializer) MiscCodec.instance);
        put(Currency.class, (ObjectSerializer) new MiscCodec());
        put(TimeZone.class, (ObjectSerializer) MiscCodec.instance);
        put(InetAddress.class, (ObjectSerializer) MiscCodec.instance);
        put(Inet4Address.class, (ObjectSerializer) MiscCodec.instance);
        put(Inet6Address.class, (ObjectSerializer) MiscCodec.instance);
        put(InetSocketAddress.class, (ObjectSerializer) MiscCodec.instance);
        put(File.class, (ObjectSerializer) MiscCodec.instance);
        put(Appendable.class, (ObjectSerializer) AppendableSerializer.instance);
        put(StringBuffer.class, (ObjectSerializer) AppendableSerializer.instance);
        put(StringBuilder.class, (ObjectSerializer) AppendableSerializer.instance);
        put(Charset.class, (ObjectSerializer) ToStringSerializer.instance);
        put(Pattern.class, (ObjectSerializer) ToStringSerializer.instance);
        put(Locale.class, (ObjectSerializer) ToStringSerializer.instance);
        put(URI.class, (ObjectSerializer) ToStringSerializer.instance);
        put(URL.class, (ObjectSerializer) ToStringSerializer.instance);
        put(UUID.class, (ObjectSerializer) ToStringSerializer.instance);
        put(AtomicBoolean.class, (ObjectSerializer) AtomicCodec.instance);
        put(AtomicInteger.class, (ObjectSerializer) AtomicCodec.instance);
        put(AtomicLong.class, (ObjectSerializer) AtomicCodec.instance);
        put(AtomicReference.class, (ObjectSerializer) ReferenceCodec.instance);
        put(AtomicIntegerArray.class, (ObjectSerializer) AtomicCodec.instance);
        put(AtomicLongArray.class, (ObjectSerializer) AtomicCodec.instance);
        put(WeakReference.class, (ObjectSerializer) ReferenceCodec.instance);
        put(SoftReference.class, (ObjectSerializer) ReferenceCodec.instance);
        put(LinkedList.class, (ObjectSerializer) CollectionCodec.instance);
    }

    public void addFilter(Class<?> clazz, SerializeFilter filter) {
        ObjectSerializer serializer = getObjectWriter(clazz);
        if (serializer instanceof SerializeFilterable) {
            SerializeFilterable filterable = (SerializeFilterable) serializer;
            if (this == globalInstance || filterable != MapSerializer.instance) {
                filterable.addFilter(filter);
                return;
            }
            MapSerializer newMapSer = new MapSerializer();
            put((Type) clazz, (ObjectSerializer) newMapSer);
            newMapSer.addFilter(filter);
        }
    }

    public void config(Class<?> clazz, SerializerFeature feature, boolean value) {
        ObjectSerializer serializer = getObjectWriter(clazz, false);
        if (serializer == null) {
            SerializeBeanInfo beanInfo = TypeUtils.buildBeanInfo(clazz, null, this.propertyNamingStrategy);
            if (value) {
                beanInfo.features |= feature.mask;
            } else {
                beanInfo.features &= ~feature.mask;
            }
            put((Type) clazz, createJavaBeanSerializer(beanInfo));
        } else if (serializer instanceof JavaBeanSerializer) {
            SerializeBeanInfo beanInfo2 = ((JavaBeanSerializer) serializer).beanInfo;
            int originalFeaturs = beanInfo2.features;
            if (value) {
                beanInfo2.features |= feature.mask;
            } else {
                beanInfo2.features &= ~feature.mask;
            }
            if (originalFeaturs != beanInfo2.features && serializer.getClass() != JavaBeanSerializer.class) {
                put((Type) clazz, createJavaBeanSerializer(beanInfo2));
            }
        }
    }

    public ObjectSerializer getObjectWriter(Class<?> clazz) {
        return getObjectWriter(clazz, true);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:186:0x0381 */
    private ObjectSerializer getObjectWriter(Class<?> clazz, boolean create) {
        Class handlerClass;
        ClassLoader classLoader;
        ObjectSerializer writer = this.serializers.get(clazz);
        if (writer == null) {
            try {
                for (Object o : ServiceLoader.load(AutowiredObjectSerializer.class, Thread.currentThread().getContextClassLoader())) {
                    if (o instanceof AutowiredObjectSerializer) {
                        AutowiredObjectSerializer autowired = (AutowiredObjectSerializer) o;
                        for (Type forType : autowired.getAutowiredFor()) {
                            put(forType, (ObjectSerializer) autowired);
                        }
                    }
                }
            } catch (ClassCastException e) {
            }
            writer = this.serializers.get(clazz);
        }
        if (writer == null && (classLoader = JSON.class.getClassLoader()) != Thread.currentThread().getContextClassLoader()) {
            try {
                for (Object o2 : ServiceLoader.load(AutowiredObjectSerializer.class, classLoader)) {
                    if (o2 instanceof AutowiredObjectSerializer) {
                        AutowiredObjectSerializer autowired2 = (AutowiredObjectSerializer) o2;
                        for (Type forType2 : autowired2.getAutowiredFor()) {
                            put(forType2, (ObjectSerializer) autowired2);
                        }
                    }
                }
            } catch (ClassCastException e2) {
            }
            writer = this.serializers.get(clazz);
        }
        for (Module module : this.modules) {
            writer = module.createSerializer(this, clazz);
            if (writer != null) {
                this.serializers.put(clazz, writer);
                return writer;
            }
        }
        if (writer != null) {
            return writer;
        }
        String className = clazz.getName();
        if (Map.class.isAssignableFrom(clazz)) {
            ObjectSerializer objectSerializer = MapSerializer.instance;
            writer = objectSerializer;
            put((Type) clazz, objectSerializer);
        } else if (List.class.isAssignableFrom(clazz)) {
            ObjectSerializer objectSerializer2 = ListSerializer.instance;
            writer = objectSerializer2;
            put((Type) clazz, objectSerializer2);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            ObjectSerializer objectSerializer3 = CollectionCodec.instance;
            writer = objectSerializer3;
            put((Type) clazz, objectSerializer3);
        } else if (Date.class.isAssignableFrom(clazz)) {
            ObjectSerializer objectSerializer4 = DateCodec.instance;
            writer = objectSerializer4;
            put((Type) clazz, objectSerializer4);
        } else if (JSONAware.class.isAssignableFrom(clazz)) {
            ObjectSerializer objectSerializer5 = JSONAwareSerializer.instance;
            writer = objectSerializer5;
            put((Type) clazz, objectSerializer5);
        } else if (JSONSerializable.class.isAssignableFrom(clazz)) {
            ObjectSerializer objectSerializer6 = JSONSerializableSerializer.instance;
            writer = objectSerializer6;
            put((Type) clazz, objectSerializer6);
        } else if (JSONStreamAware.class.isAssignableFrom(clazz)) {
            ObjectSerializer objectSerializer7 = MiscCodec.instance;
            writer = objectSerializer7;
            put((Type) clazz, objectSerializer7);
        } else if (clazz.isEnum()) {
            JSONType jsonType = (JSONType) TypeUtils.getAnnotation(clazz, JSONType.class);
            if (jsonType == null || !jsonType.serializeEnumAsJavaBean()) {
                ObjectSerializer objectSerializer8 = EnumSerializer.instance;
                writer = objectSerializer8;
                put((Type) clazz, objectSerializer8);
            } else {
                ObjectSerializer createJavaBeanSerializer = createJavaBeanSerializer(clazz);
                writer = createJavaBeanSerializer;
                put((Type) clazz, createJavaBeanSerializer);
            }
        } else {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass.isEnum()) {
                JSONType jsonType2 = (JSONType) TypeUtils.getAnnotation(superClass, JSONType.class);
                if (jsonType2 == null || !jsonType2.serializeEnumAsJavaBean()) {
                    ObjectSerializer objectSerializer9 = EnumSerializer.instance;
                    writer = objectSerializer9;
                    put((Type) clazz, objectSerializer9);
                } else {
                    ObjectSerializer createJavaBeanSerializer2 = createJavaBeanSerializer(clazz);
                    writer = createJavaBeanSerializer2;
                    put((Type) clazz, createJavaBeanSerializer2);
                }
            } else if (clazz.isArray()) {
                Class<?> componentType = clazz.getComponentType();
                ObjectSerializer arraySerializer = new ArraySerializer(componentType, getObjectWriter(componentType));
                writer = arraySerializer;
                put((Type) clazz, arraySerializer);
            } else if (Throwable.class.isAssignableFrom(clazz)) {
                SerializeBeanInfo beanInfo = TypeUtils.buildBeanInfo(clazz, null, this.propertyNamingStrategy);
                beanInfo.features |= SerializerFeature.WriteClassName.mask;
                ObjectSerializer javaBeanSerializer = new JavaBeanSerializer(beanInfo);
                writer = javaBeanSerializer;
                put((Type) clazz, javaBeanSerializer);
            } else if (TimeZone.class.isAssignableFrom(clazz) || Map.Entry.class.isAssignableFrom(clazz)) {
                ObjectSerializer objectSerializer10 = MiscCodec.instance;
                writer = objectSerializer10;
                put((Type) clazz, objectSerializer10);
            } else if (Appendable.class.isAssignableFrom(clazz)) {
                ObjectSerializer objectSerializer11 = AppendableSerializer.instance;
                writer = objectSerializer11;
                put((Type) clazz, objectSerializer11);
            } else if (Charset.class.isAssignableFrom(clazz)) {
                ObjectSerializer objectSerializer12 = ToStringSerializer.instance;
                writer = objectSerializer12;
                put((Type) clazz, objectSerializer12);
            } else if (Enumeration.class.isAssignableFrom(clazz)) {
                ObjectSerializer objectSerializer13 = EnumerationSerializer.instance;
                writer = objectSerializer13;
                put((Type) clazz, objectSerializer13);
            } else if (Calendar.class.isAssignableFrom(clazz) || XMLGregorianCalendar.class.isAssignableFrom(clazz)) {
                ObjectSerializer objectSerializer14 = CalendarCodec.instance;
                writer = objectSerializer14;
                put((Type) clazz, objectSerializer14);
            } else if (TypeUtils.isClob(clazz)) {
                ObjectSerializer objectSerializer15 = ClobSeriliazer.instance;
                writer = objectSerializer15;
                put((Type) clazz, objectSerializer15);
            } else if (TypeUtils.isPath(clazz)) {
                ObjectSerializer objectSerializer16 = ToStringSerializer.instance;
                writer = objectSerializer16;
                put((Type) clazz, objectSerializer16);
            } else if (Iterator.class.isAssignableFrom(clazz)) {
                ObjectSerializer objectSerializer17 = MiscCodec.instance;
                writer = objectSerializer17;
                put((Type) clazz, objectSerializer17);
            } else if (Node.class.isAssignableFrom(clazz)) {
                ObjectSerializer objectSerializer18 = MiscCodec.instance;
                writer = objectSerializer18;
                put((Type) clazz, objectSerializer18);
            } else {
                int i = 0;
                if (className.startsWith("java.awt.") && AwtCodec.support(clazz) && !awtError) {
                    try {
                        String[] names = {"java.awt.Color", "java.awt.Font", "java.awt.Point", "java.awt.Rectangle"};
                        for (String name : names) {
                            if (name.equals(className)) {
                                Type cls = Class.forName(name);
                                ObjectSerializer writer2 = AwtCodec.instance;
                                put(cls, writer2);
                                return writer2;
                            }
                        }
                    } catch (Throwable th) {
                        awtError = true;
                    }
                }
                if (!jdk8Error && (className.startsWith("java.time.") || className.startsWith("java.util.Optional") || className.equals("java.util.concurrent.atomic.LongAdder") || className.equals("java.util.concurrent.atomic.DoubleAdder"))) {
                    try {
                        String[] names2 = {"java.time.LocalDateTime", "java.time.LocalDate", "java.time.LocalTime", "java.time.ZonedDateTime", "java.time.OffsetDateTime", "java.time.OffsetTime", "java.time.ZoneOffset", "java.time.ZoneRegion", "java.time.Period", "java.time.Duration", "java.time.Instant"};
                        for (String name2 : names2) {
                            if (name2.equals(className)) {
                                Type cls2 = Class.forName(name2);
                                ObjectSerializer writer3 = Jdk8DateCodec.instance;
                                put(cls2, writer3);
                                return writer3;
                            }
                        }
                        String[] names3 = {"java.util.Optional", "java.util.OptionalDouble", "java.util.OptionalInt", "java.util.OptionalLong"};
                        for (String name3 : names3) {
                            if (name3.equals(className)) {
                                Type cls3 = Class.forName(name3);
                                ObjectSerializer writer4 = OptionalCodec.instance;
                                put(cls3, writer4);
                                return writer4;
                            }
                        }
                        String[] names4 = {"java.util.concurrent.atomic.LongAdder", "java.util.concurrent.atomic.DoubleAdder"};
                        for (String name4 : names4) {
                            if (name4.equals(className)) {
                                Type cls4 = Class.forName(name4);
                                ObjectSerializer writer5 = AdderSerializer.instance;
                                put(cls4, writer5);
                                return writer5;
                            }
                        }
                    } catch (Throwable th2) {
                        jdk8Error = true;
                    }
                }
                if (!oracleJdbcError && className.startsWith("oracle.sql.")) {
                    try {
                        String[] names5 = {"oracle.sql.DATE", "oracle.sql.TIMESTAMP"};
                        for (String name5 : names5) {
                            if (name5.equals(className)) {
                                Type cls5 = Class.forName(name5);
                                ObjectSerializer writer6 = DateCodec.instance;
                                put(cls5, writer6);
                                return writer6;
                            }
                        }
                    } catch (Throwable th3) {
                        oracleJdbcError = true;
                    }
                }
                if (!springfoxError && className.equals("springfox.documentation.spring.web.json.Json")) {
                    try {
                        Type cls6 = Class.forName("springfox.documentation.spring.web.json.Json");
                        ObjectSerializer objectSerializer19 = SwaggerJsonSerializer.instance;
                        writer = objectSerializer19;
                        put(cls6, objectSerializer19);
                        return writer;
                    } catch (ClassNotFoundException e3) {
                        springfoxError = true;
                    }
                }
                if (!guavaError && className.startsWith("com.google.common.collect.")) {
                    try {
                        String[] names6 = {"com.google.common.collect.HashMultimap", "com.google.common.collect.LinkedListMultimap", "com.google.common.collect.LinkedHashMultimap", "com.google.common.collect.ArrayListMultimap", "com.google.common.collect.TreeMultimap"};
                        for (String name6 : names6) {
                            if (name6.equals(className)) {
                                Type cls7 = Class.forName(name6);
                                ObjectSerializer writer7 = GuavaCodec.instance;
                                put(cls7, writer7);
                                return writer7;
                            }
                        }
                    } catch (ClassNotFoundException e4) {
                        guavaError = true;
                    }
                }
                if (!jsonnullError && className.equals("net.sf.json.JSONNull")) {
                    try {
                        Type cls8 = Class.forName("net.sf.json.JSONNull");
                        ObjectSerializer objectSerializer20 = MiscCodec.instance;
                        writer = objectSerializer20;
                        put(cls8, objectSerializer20);
                        return writer;
                    } catch (ClassNotFoundException e5) {
                        jsonnullError = true;
                    }
                }
                if (!jodaError && className.startsWith("org.joda.")) {
                    try {
                        String[] names7 = {"org.joda.time.LocalDate", "org.joda.time.LocalDateTime", "org.joda.time.LocalTime", "org.joda.time.Instant", "org.joda.time.DateTime", "org.joda.time.Period", "org.joda.time.Duration", "org.joda.time.DateTimeZone", "org.joda.time.UTCDateTimeZone", "org.joda.time.tz.CachedDateTimeZone", "org.joda.time.tz.FixedDateTimeZone"};
                        for (String name7 : names7) {
                            if (name7.equals(className)) {
                                Type cls9 = Class.forName(name7);
                                ObjectSerializer writer8 = JodaCodec.instance;
                                put(cls9, writer8);
                                return writer8;
                            }
                        }
                    } catch (ClassNotFoundException e6) {
                        jodaError = true;
                    }
                }
                if ("java.nio.HeapByteBuffer".equals(className)) {
                    ObjectSerializer writer9 = ByteBufferCodec.instance;
                    put((Type) clazz, writer9);
                    return writer9;
                } else if ("org.javamoney.moneta.Money".equals(className)) {
                    ObjectSerializer writer10 = MonetaCodec.instance;
                    put((Type) clazz, writer10);
                    return writer10;
                } else {
                    Class[] interfaces = clazz.getInterfaces();
                    if (interfaces.length == 1 && interfaces[0].isAnnotation()) {
                        put((Type) clazz, AnnotationSerializer.instance);
                        return AnnotationSerializer.instance;
                    } else if (TypeUtils.isProxy(clazz)) {
                        ObjectSerializer superWriter = getObjectWriter(clazz.getSuperclass());
                        put((Type) clazz, superWriter);
                        return superWriter;
                    } else {
                        if (Proxy.isProxyClass(clazz)) {
                            Class handlerClass2 = null;
                            if (interfaces.length != 2) {
                                int length = interfaces.length;
                                while (true) {
                                    if (i >= length) {
                                        handlerClass = handlerClass2;
                                        break;
                                    }
                                    Class proxiedInterface = interfaces[i];
                                    if (!proxiedInterface.getName().startsWith("org.springframework.aop.")) {
                                        if (handlerClass2 != null) {
                                            handlerClass = null;
                                            break;
                                        }
                                        handlerClass2 = proxiedInterface;
                                    }
                                    i++;
                                }
                            } else {
                                handlerClass = interfaces[1];
                            }
                            if (handlerClass != null) {
                                ObjectSerializer superWriter2 = getObjectWriter(handlerClass);
                                put((Type) clazz, superWriter2);
                                return superWriter2;
                            }
                        }
                        if (create) {
                            writer = createJavaBeanSerializer(clazz);
                            put((Type) clazz, writer);
                        }
                    }
                }
            }
        }
        if (writer == null) {
            return this.serializers.get(clazz);
        }
        return writer;
    }

    public final ObjectSerializer get(Type key) {
        return this.serializers.get(key);
    }

    public boolean put(Object type, Object value) {
        return put((Type) type, (ObjectSerializer) value);
    }

    public boolean put(Type type, ObjectSerializer value) {
        return this.serializers.put(type, value);
    }

    public void configEnumAsJavaBean(Class<? extends Enum>... enumClasses) {
        for (Class<? extends Enum> enumClass : enumClasses) {
            put((Type) enumClass, createJavaBeanSerializer(enumClass));
        }
    }

    public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy2) {
        this.propertyNamingStrategy = propertyNamingStrategy2;
    }

    public void clearSerializers() {
        this.serializers.clear();
        initSerializers();
    }

    public void register(Module module) {
        this.modules.add(module);
    }
}
