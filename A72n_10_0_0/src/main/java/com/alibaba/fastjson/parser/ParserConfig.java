package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.asm.ClassReader;
import com.alibaba.fastjson.asm.TypeCollector;
import com.alibaba.fastjson.parser.deserializer.ASMDeserializerFactory;
import com.alibaba.fastjson.parser.deserializer.ArrayListTypeFieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.AutowiredObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.DefaultFieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.EnumDeserializer;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.JSONPDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.Jdk8DateCodec;
import com.alibaba.fastjson.parser.deserializer.MapDeserializer;
import com.alibaba.fastjson.parser.deserializer.NumberDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.OptionalCodec;
import com.alibaba.fastjson.parser.deserializer.PropertyProcessable;
import com.alibaba.fastjson.parser.deserializer.PropertyProcessableDeserializer;
import com.alibaba.fastjson.parser.deserializer.SqlDateDeserializer;
import com.alibaba.fastjson.parser.deserializer.StackTraceElementDeserializer;
import com.alibaba.fastjson.parser.deserializer.ThrowableDeserializer;
import com.alibaba.fastjson.parser.deserializer.TimeDeserializer;
import com.alibaba.fastjson.serializer.AtomicCodec;
import com.alibaba.fastjson.serializer.AwtCodec;
import com.alibaba.fastjson.serializer.BigDecimalCodec;
import com.alibaba.fastjson.serializer.BigIntegerCodec;
import com.alibaba.fastjson.serializer.BooleanCodec;
import com.alibaba.fastjson.serializer.ByteBufferCodec;
import com.alibaba.fastjson.serializer.CalendarCodec;
import com.alibaba.fastjson.serializer.CharArrayCodec;
import com.alibaba.fastjson.serializer.CharacterCodec;
import com.alibaba.fastjson.serializer.CollectionCodec;
import com.alibaba.fastjson.serializer.DateCodec;
import com.alibaba.fastjson.serializer.FloatCodec;
import com.alibaba.fastjson.serializer.GuavaCodec;
import com.alibaba.fastjson.serializer.IntegerCodec;
import com.alibaba.fastjson.serializer.JodaCodec;
import com.alibaba.fastjson.serializer.LongCodec;
import com.alibaba.fastjson.serializer.MiscCodec;
import com.alibaba.fastjson.serializer.ObjectArrayCodec;
import com.alibaba.fastjson.serializer.ReferenceCodec;
import com.alibaba.fastjson.serializer.StringCodec;
import com.alibaba.fastjson.spi.Module;
import com.alibaba.fastjson.support.moneta.MonetaCodec;
import com.alibaba.fastjson.util.ASMClassLoader;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.IdentityHashMap;
import com.alibaba.fastjson.util.JavaBeanInfo;
import com.alibaba.fastjson.util.ServiceLoader;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.AccessControlException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.xml.datatype.XMLGregorianCalendar;

public class ParserConfig {
    public static final String AUTOTYPE_ACCEPT = "fastjson.parser.autoTypeAccept";
    public static final String AUTOTYPE_SUPPORT_PROPERTY = "fastjson.parser.autoTypeSupport";
    public static final boolean AUTO_SUPPORT = "true".equals(IOUtils.getStringProperty(AUTOTYPE_SUPPORT_PROPERTY));
    private static final String[] AUTO_TYPE_ACCEPT_LIST;
    public static final String[] DENYS = splitItemsFormProperty(IOUtils.getStringProperty(DENY_PROPERTY));
    public static final String DENY_PROPERTY = "fastjson.parser.deny";
    private static boolean awtError = false;
    public static ParserConfig global = new ParserConfig();
    private static boolean guavaError = false;
    private static boolean jdk8Error = false;
    private static boolean jodaError = false;
    private long[] acceptHashCodes;
    private boolean asmEnable;
    protected ASMDeserializerFactory asmFactory;
    private boolean autoTypeSupport;
    public boolean compatibleWithJavaBean;
    protected ClassLoader defaultClassLoader;
    private long[] denyHashCodes;
    private final IdentityHashMap<Type, ObjectDeserializer> deserializers;
    public final boolean fieldBased;
    private boolean jacksonCompatible;
    private List<Module> modules;
    public PropertyNamingStrategy propertyNamingStrategy;
    public final SymbolTable symbolTable;
    private final ConcurrentMap<String, Class<?>> typeMapping;

    static {
        String[] items = splitItemsFormProperty(IOUtils.getStringProperty(AUTOTYPE_ACCEPT));
        if (items == null) {
            items = new String[0];
        }
        AUTO_TYPE_ACCEPT_LIST = items;
    }

    public static ParserConfig getGlobalInstance() {
        return global;
    }

    public ParserConfig() {
        this(false);
    }

    public ParserConfig(boolean fieldBase) {
        this(null, null, fieldBase);
    }

    public ParserConfig(ClassLoader parentClassLoader) {
        this(null, parentClassLoader, false);
    }

    public ParserConfig(ASMDeserializerFactory asmFactory2) {
        this(asmFactory2, null, false);
    }

    private ParserConfig(ASMDeserializerFactory asmFactory2, ClassLoader parentClassLoader, boolean fieldBased2) {
        this.deserializers = new IdentityHashMap<>();
        this.typeMapping = new ConcurrentHashMap(16, 0.75f, 1);
        this.asmEnable = !ASMUtils.IS_ANDROID;
        this.symbolTable = new SymbolTable(4096);
        this.autoTypeSupport = AUTO_SUPPORT;
        this.jacksonCompatible = false;
        this.compatibleWithJavaBean = TypeUtils.compatibleWithJavaBean;
        this.modules = new ArrayList();
        this.denyHashCodes = new long[]{-8720046426850100497L, -8165637398350707645L, -8109300701639721088L, -8083514888460375884L, -7966123100503199569L, -7921218830998286408L, -7768608037458185275L, -7766605818834748097L, -6835437086156813536L, -6179589609550493385L, -5194641081268104286L, -4837536971810737970L, -4082057040235125754L, -3935185854875733362L, -2753427844400776271L, -2364987994247679115L, -2262244760619952081L, -1872417015366588117L, -1589194880214235129L, -254670111376247151L, -190281065685395680L, 33238344207745342L, 313864100207897507L, 1073634739308289776L, 1203232727967308606L, 1459860845934817624L, 1502845958873959152L, 3547627781654598988L, 3730752432285826863L, 3794316665763266033L, 4147696707147271408L, 4904007817188630457L, 5347909877633654828L, 5450448828334921485L, 5688200883751798389L, 5751393439502795295L, 5944107969236155580L, 6742705432718011780L, 7017492163108594270L, 7179336928365889465L, 7442624256860549330L, 8389032537095247355L, 8409640769019589119L, 8838294710098435315L};
        long[] hashCodes = new long[(AUTO_TYPE_ACCEPT_LIST.length + 1)];
        for (int i = 0; i < AUTO_TYPE_ACCEPT_LIST.length; i++) {
            hashCodes[i] = TypeUtils.fnv1a_64(AUTO_TYPE_ACCEPT_LIST[i]);
        }
        hashCodes[hashCodes.length - 1] = -6293031534589903644L;
        Arrays.sort(hashCodes);
        this.acceptHashCodes = hashCodes;
        this.fieldBased = fieldBased2;
        if (asmFactory2 == null && !ASMUtils.IS_ANDROID) {
            if (parentClassLoader == null) {
                try {
                    asmFactory2 = new ASMDeserializerFactory(new ASMClassLoader());
                } catch (ExceptionInInitializerError e) {
                } catch (AccessControlException e2) {
                } catch (NoClassDefFoundError e3) {
                }
            } else {
                asmFactory2 = new ASMDeserializerFactory(parentClassLoader);
            }
        }
        this.asmFactory = asmFactory2;
        if (asmFactory2 == null) {
            this.asmEnable = false;
        }
        initDeserializers();
        addItemsToDeny(DENYS);
        addItemsToAccept(AUTO_TYPE_ACCEPT_LIST);
    }

    private void initDeserializers() {
        this.deserializers.put(SimpleDateFormat.class, MiscCodec.instance);
        this.deserializers.put(Timestamp.class, SqlDateDeserializer.instance_timestamp);
        this.deserializers.put(Date.class, SqlDateDeserializer.instance);
        this.deserializers.put(Time.class, TimeDeserializer.instance);
        this.deserializers.put(java.util.Date.class, DateCodec.instance);
        this.deserializers.put(Calendar.class, CalendarCodec.instance);
        this.deserializers.put(XMLGregorianCalendar.class, CalendarCodec.instance);
        this.deserializers.put(JSONObject.class, MapDeserializer.instance);
        this.deserializers.put(JSONArray.class, CollectionCodec.instance);
        this.deserializers.put(Map.class, MapDeserializer.instance);
        this.deserializers.put(HashMap.class, MapDeserializer.instance);
        this.deserializers.put(LinkedHashMap.class, MapDeserializer.instance);
        this.deserializers.put(TreeMap.class, MapDeserializer.instance);
        this.deserializers.put(ConcurrentMap.class, MapDeserializer.instance);
        this.deserializers.put(ConcurrentHashMap.class, MapDeserializer.instance);
        this.deserializers.put(Collection.class, CollectionCodec.instance);
        this.deserializers.put(List.class, CollectionCodec.instance);
        this.deserializers.put(ArrayList.class, CollectionCodec.instance);
        this.deserializers.put(Object.class, JavaObjectDeserializer.instance);
        this.deserializers.put(String.class, StringCodec.instance);
        this.deserializers.put(StringBuffer.class, StringCodec.instance);
        this.deserializers.put(StringBuilder.class, StringCodec.instance);
        this.deserializers.put(Character.TYPE, CharacterCodec.instance);
        this.deserializers.put(Character.class, CharacterCodec.instance);
        this.deserializers.put(Byte.TYPE, NumberDeserializer.instance);
        this.deserializers.put(Byte.class, NumberDeserializer.instance);
        this.deserializers.put(Short.TYPE, NumberDeserializer.instance);
        this.deserializers.put(Short.class, NumberDeserializer.instance);
        this.deserializers.put(Integer.TYPE, IntegerCodec.instance);
        this.deserializers.put(Integer.class, IntegerCodec.instance);
        this.deserializers.put(Long.TYPE, LongCodec.instance);
        this.deserializers.put(Long.class, LongCodec.instance);
        this.deserializers.put(BigInteger.class, BigIntegerCodec.instance);
        this.deserializers.put(BigDecimal.class, BigDecimalCodec.instance);
        this.deserializers.put(Float.TYPE, FloatCodec.instance);
        this.deserializers.put(Float.class, FloatCodec.instance);
        this.deserializers.put(Double.TYPE, NumberDeserializer.instance);
        this.deserializers.put(Double.class, NumberDeserializer.instance);
        this.deserializers.put(Boolean.TYPE, BooleanCodec.instance);
        this.deserializers.put(Boolean.class, BooleanCodec.instance);
        this.deserializers.put(Class.class, MiscCodec.instance);
        this.deserializers.put(char[].class, new CharArrayCodec());
        this.deserializers.put(AtomicBoolean.class, BooleanCodec.instance);
        this.deserializers.put(AtomicInteger.class, IntegerCodec.instance);
        this.deserializers.put(AtomicLong.class, LongCodec.instance);
        this.deserializers.put(AtomicReference.class, ReferenceCodec.instance);
        this.deserializers.put(WeakReference.class, ReferenceCodec.instance);
        this.deserializers.put(SoftReference.class, ReferenceCodec.instance);
        this.deserializers.put(UUID.class, MiscCodec.instance);
        this.deserializers.put(TimeZone.class, MiscCodec.instance);
        this.deserializers.put(Locale.class, MiscCodec.instance);
        this.deserializers.put(Currency.class, MiscCodec.instance);
        this.deserializers.put(Inet4Address.class, MiscCodec.instance);
        this.deserializers.put(Inet6Address.class, MiscCodec.instance);
        this.deserializers.put(InetSocketAddress.class, MiscCodec.instance);
        this.deserializers.put(File.class, MiscCodec.instance);
        this.deserializers.put(URI.class, MiscCodec.instance);
        this.deserializers.put(URL.class, MiscCodec.instance);
        this.deserializers.put(Pattern.class, MiscCodec.instance);
        this.deserializers.put(Charset.class, MiscCodec.instance);
        this.deserializers.put(JSONPath.class, MiscCodec.instance);
        this.deserializers.put(Number.class, NumberDeserializer.instance);
        this.deserializers.put(AtomicIntegerArray.class, AtomicCodec.instance);
        this.deserializers.put(AtomicLongArray.class, AtomicCodec.instance);
        this.deserializers.put(StackTraceElement.class, StackTraceElementDeserializer.instance);
        this.deserializers.put(Serializable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Cloneable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Comparable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Closeable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(JSONPObject.class, new JSONPDeserializer());
    }

    private static String[] splitItemsFormProperty(String property) {
        if (property == null || property.length() <= 0) {
            return null;
        }
        return property.split(",");
    }

    public void configFromPropety(Properties properties) {
        addItemsToDeny(splitItemsFormProperty(properties.getProperty(DENY_PROPERTY)));
        addItemsToAccept(splitItemsFormProperty(properties.getProperty(AUTOTYPE_ACCEPT)));
        String property = properties.getProperty(AUTOTYPE_SUPPORT_PROPERTY);
        if ("true".equals(property)) {
            this.autoTypeSupport = true;
        } else if ("false".equals(property)) {
            this.autoTypeSupport = false;
        }
    }

    private void addItemsToDeny(String[] items) {
        if (items != null) {
            for (String item : items) {
                addDeny(item);
            }
        }
    }

    private void addItemsToAccept(String[] items) {
        if (items != null) {
            for (String item : items) {
                addAccept(item);
            }
        }
    }

    public boolean isAutoTypeSupport() {
        return this.autoTypeSupport;
    }

    public void setAutoTypeSupport(boolean autoTypeSupport2) {
        this.autoTypeSupport = autoTypeSupport2;
    }

    public boolean isAsmEnable() {
        return this.asmEnable;
    }

    public void setAsmEnable(boolean asmEnable2) {
        this.asmEnable = asmEnable2;
    }

    public IdentityHashMap<Type, ObjectDeserializer> getDerializers() {
        return this.deserializers;
    }

    public IdentityHashMap<Type, ObjectDeserializer> getDeserializers() {
        return this.deserializers;
    }

    public ObjectDeserializer getDeserializer(Type type) {
        ObjectDeserializer derializer = this.deserializers.get(type);
        if (derializer != null) {
            return derializer;
        }
        if (type instanceof Class) {
            return getDeserializer((Class) type, type);
        }
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return getDeserializer((Class) rawType, type);
            }
            return getDeserializer(rawType);
        }
        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getDeserializer(upperBounds[0]);
            }
        }
        return JavaObjectDeserializer.instance;
    }

    public ObjectDeserializer getDeserializer(Class<?> clazz, Type type) {
        ObjectDeserializer derializer;
        Class<?> mappingTo;
        Type type2 = type;
        ObjectDeserializer derializer2 = this.deserializers.get(type2);
        if (derializer2 != null) {
            return derializer2;
        }
        if (type2 == null) {
            type2 = clazz;
        }
        ObjectDeserializer derializer3 = this.deserializers.get(type2);
        if (derializer3 != null) {
            return derializer3;
        }
        JSONType annotation = (JSONType) TypeUtils.getAnnotation(clazz, JSONType.class);
        if (!(annotation == null || (mappingTo = annotation.mappingTo()) == Void.class)) {
            return getDeserializer(mappingTo, mappingTo);
        }
        if ((type2 instanceof WildcardType) || (type2 instanceof TypeVariable) || (type2 instanceof ParameterizedType)) {
            derializer3 = this.deserializers.get(clazz);
        }
        if (derializer3 != null) {
            return derializer3;
        }
        ObjectDeserializer derializer4 = derializer3;
        for (Module module : this.modules) {
            derializer4 = module.createDeserializer(this, clazz);
            if (derializer4 != null) {
                putDeserializer(type2, derializer4);
                return derializer4;
            }
        }
        String className = clazz.getName().replace('$', '.');
        if (className.startsWith("java.awt.") && AwtCodec.support(clazz) && !awtError) {
            String[] names = {"java.awt.Point", "java.awt.Font", "java.awt.Rectangle", "java.awt.Color"};
            try {
                for (String name : names) {
                    if (name.equals(className)) {
                        IdentityHashMap<Type, ObjectDeserializer> identityHashMap = this.deserializers;
                        Class<?> cls = Class.forName(name);
                        ObjectDeserializer derializer5 = AwtCodec.instance;
                        identityHashMap.put(cls, derializer5);
                        return derializer5;
                    }
                }
            } catch (Throwable th) {
                awtError = true;
            }
            derializer4 = AwtCodec.instance;
        }
        if (!jdk8Error) {
            try {
                if (className.startsWith("java.time.")) {
                    String[] names2 = {"java.time.LocalDateTime", "java.time.LocalDate", "java.time.LocalTime", "java.time.ZonedDateTime", "java.time.OffsetDateTime", "java.time.OffsetTime", "java.time.ZoneOffset", "java.time.ZoneRegion", "java.time.ZoneId", "java.time.Period", "java.time.Duration", "java.time.Instant"};
                    for (String name2 : names2) {
                        if (name2.equals(className)) {
                            IdentityHashMap<Type, ObjectDeserializer> identityHashMap2 = this.deserializers;
                            Class<?> cls2 = Class.forName(name2);
                            ObjectDeserializer derializer6 = Jdk8DateCodec.instance;
                            identityHashMap2.put(cls2, derializer6);
                            return derializer6;
                        }
                    }
                } else if (className.startsWith("java.util.Optional")) {
                    String[] names3 = {"java.util.Optional", "java.util.OptionalDouble", "java.util.OptionalInt", "java.util.OptionalLong"};
                    for (String name3 : names3) {
                        if (name3.equals(className)) {
                            IdentityHashMap<Type, ObjectDeserializer> identityHashMap3 = this.deserializers;
                            Class<?> cls3 = Class.forName(name3);
                            ObjectDeserializer derializer7 = OptionalCodec.instance;
                            identityHashMap3.put(cls3, derializer7);
                            return derializer7;
                        }
                    }
                }
            } catch (Throwable th2) {
                jdk8Error = true;
            }
        }
        if (!jodaError) {
            try {
                if (className.startsWith("org.joda.time.")) {
                    String[] names4 = {"org.joda.time.DateTime", "org.joda.time.LocalDate", "org.joda.time.LocalDateTime", "org.joda.time.LocalTime", "org.joda.time.Instant", "org.joda.time.Period", "org.joda.time.Duration", "org.joda.time.DateTimeZone", "org.joda.time.format.DateTimeFormatter"};
                    for (String name4 : names4) {
                        if (name4.equals(className)) {
                            IdentityHashMap<Type, ObjectDeserializer> identityHashMap4 = this.deserializers;
                            Class<?> cls4 = Class.forName(name4);
                            ObjectDeserializer derializer8 = JodaCodec.instance;
                            identityHashMap4.put(cls4, derializer8);
                            return derializer8;
                        }
                    }
                }
            } catch (Throwable th3) {
                jodaError = true;
            }
        }
        if (!guavaError && className.startsWith("com.google.common.collect.")) {
            try {
                String[] names5 = {"com.google.common.collect.HashMultimap", "com.google.common.collect.LinkedListMultimap", "com.google.common.collect.LinkedHashMultimap", "com.google.common.collect.ArrayListMultimap", "com.google.common.collect.TreeMultimap"};
                for (String name5 : names5) {
                    if (name5.equals(className)) {
                        IdentityHashMap<Type, ObjectDeserializer> identityHashMap5 = this.deserializers;
                        Class<?> cls5 = Class.forName(name5);
                        ObjectDeserializer derializer9 = GuavaCodec.instance;
                        identityHashMap5.put(cls5, derializer9);
                        return derializer9;
                    }
                }
            } catch (ClassNotFoundException e) {
                guavaError = true;
            }
        }
        ObjectDeserializer derializer10 = derializer4;
        if (className.equals("java.nio.ByteBuffer")) {
            IdentityHashMap<Type, ObjectDeserializer> identityHashMap6 = this.deserializers;
            ObjectDeserializer objectDeserializer = ByteBufferCodec.instance;
            derializer10 = objectDeserializer;
            identityHashMap6.put(clazz, objectDeserializer);
        }
        ObjectDeserializer derializer11 = derializer10;
        if (className.equals("java.nio.file.Path")) {
            IdentityHashMap<Type, ObjectDeserializer> identityHashMap7 = this.deserializers;
            ObjectDeserializer objectDeserializer2 = MiscCodec.instance;
            derializer11 = objectDeserializer2;
            identityHashMap7.put(clazz, objectDeserializer2);
        }
        ObjectDeserializer derializer12 = derializer11;
        if (clazz == Map.Entry.class) {
            IdentityHashMap<Type, ObjectDeserializer> identityHashMap8 = this.deserializers;
            ObjectDeserializer objectDeserializer3 = MiscCodec.instance;
            derializer12 = objectDeserializer3;
            identityHashMap8.put(clazz, objectDeserializer3);
        }
        if (className.equals("org.javamoney.moneta.Money")) {
            IdentityHashMap<Type, ObjectDeserializer> identityHashMap9 = this.deserializers;
            ObjectDeserializer objectDeserializer4 = MonetaCodec.instance;
            derializer12 = objectDeserializer4;
            identityHashMap9.put(clazz, objectDeserializer4);
        }
        try {
            for (AutowiredObjectDeserializer autowired : ServiceLoader.load(AutowiredObjectDeserializer.class, Thread.currentThread().getContextClassLoader())) {
                for (Type forType : autowired.getAutowiredFor()) {
                    this.deserializers.put(forType, autowired);
                }
            }
        } catch (Exception e2) {
        }
        if (derializer12 == null) {
            derializer12 = this.deserializers.get(type2);
        }
        if (derializer12 != null) {
            return derializer12;
        }
        if (clazz.isEnum()) {
            if (this.jacksonCompatible) {
                for (Method method : clazz.getMethods()) {
                    if (TypeUtils.isJacksonCreator(method)) {
                        ObjectDeserializer derializer13 = createJavaBeanDeserializer(clazz, type2);
                        putDeserializer(type2, derializer13);
                        return derializer13;
                    }
                }
            }
            JSONType jsonType = (JSONType) clazz.getAnnotation(JSONType.class);
            if (jsonType != null) {
                try {
                    ObjectDeserializer derializer14 = (ObjectDeserializer) jsonType.deserializer().newInstance();
                    this.deserializers.put(clazz, derializer14);
                    return derializer14;
                } catch (Throwable th4) {
                }
            }
            derializer = new EnumDeserializer(clazz);
        } else if (clazz.isArray()) {
            derializer = ObjectArrayCodec.instance;
        } else if (clazz == Set.class || clazz == HashSet.class || clazz == Collection.class || clazz == List.class || clazz == ArrayList.class) {
            derializer = CollectionCodec.instance;
        } else if (Collection.class.isAssignableFrom(clazz)) {
            derializer = CollectionCodec.instance;
        } else if (Map.class.isAssignableFrom(clazz)) {
            derializer = MapDeserializer.instance;
        } else if (Throwable.class.isAssignableFrom(clazz)) {
            derializer = new ThrowableDeserializer(this, clazz);
        } else if (PropertyProcessable.class.isAssignableFrom(clazz)) {
            derializer = new PropertyProcessableDeserializer(clazz);
        } else if (clazz == InetAddress.class) {
            derializer = MiscCodec.instance;
        } else {
            derializer = createJavaBeanDeserializer(clazz, type2);
        }
        putDeserializer(type2, derializer);
        return derializer;
    }

    public void initJavaBeanDeserializers(Class<?>... classes) {
        if (classes != null) {
            for (Class<?> type : classes) {
                if (type != null) {
                    putDeserializer(type, createJavaBeanDeserializer(type, type));
                }
            }
        }
    }

    public ObjectDeserializer createJavaBeanDeserializer(Class<?> clazz, Type type) {
        boolean asmEnable2 = this.asmEnable & (!this.fieldBased);
        if (asmEnable2) {
            JSONType jsonType = (JSONType) TypeUtils.getAnnotation(clazz, JSONType.class);
            if (jsonType != null) {
                Class<?> deserializerClass = jsonType.deserializer();
                if (deserializerClass != Void.class) {
                    try {
                        Object deseralizer = deserializerClass.newInstance();
                        if (deseralizer instanceof ObjectDeserializer) {
                            return (ObjectDeserializer) deseralizer;
                        }
                    } catch (Throwable th) {
                    }
                }
                asmEnable2 = jsonType.asm();
            }
            if (asmEnable2) {
                Class<?> superClass = JavaBeanInfo.getBuilderClass(clazz, jsonType);
                if (superClass == null) {
                    superClass = clazz;
                }
                while (true) {
                    if (Modifier.isPublic(superClass.getModifiers())) {
                        superClass = superClass.getSuperclass();
                        if (superClass != Object.class) {
                            if (superClass == null) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        asmEnable2 = false;
                        break;
                    }
                }
            }
        }
        if (clazz.getTypeParameters().length != 0) {
            asmEnable2 = false;
        }
        if (asmEnable2 && this.asmFactory != null && this.asmFactory.classLoader.isExternalClass(clazz)) {
            asmEnable2 = false;
        }
        if (asmEnable2) {
            asmEnable2 = ASMUtils.checkName(clazz.getSimpleName());
        }
        if (asmEnable2) {
            if (clazz.isInterface()) {
                asmEnable2 = false;
            }
            JavaBeanInfo beanInfo = JavaBeanInfo.build(clazz, type, this.propertyNamingStrategy, false, TypeUtils.compatibleWithJavaBean, this.jacksonCompatible);
            if (asmEnable2 && beanInfo.fields.length > 200) {
                asmEnable2 = false;
            }
            Constructor<?> defaultConstructor = beanInfo.defaultConstructor;
            if (asmEnable2 && defaultConstructor == null && !clazz.isInterface()) {
                asmEnable2 = false;
            }
            FieldInfo[] fieldInfoArr = beanInfo.fields;
            int length = fieldInfoArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                FieldInfo fieldInfo = fieldInfoArr[i];
                if (!fieldInfo.getOnly) {
                    Class<?> fieldClass = fieldInfo.fieldClass;
                    if (Modifier.isPublic(fieldClass.getModifiers())) {
                        if (!fieldClass.isMemberClass() || Modifier.isStatic(fieldClass.getModifiers())) {
                            if (fieldInfo.getMember() != null && !ASMUtils.checkName(fieldInfo.getMember().getName())) {
                                asmEnable2 = false;
                                break;
                            }
                            JSONField annotation = fieldInfo.getAnnotation();
                            if ((annotation == null || (ASMUtils.checkName(annotation.name()) && annotation.format().length() == 0 && annotation.deserializeUsing() == Void.class && !annotation.unwrapped())) && (fieldInfo.method == null || fieldInfo.method.getParameterTypes().length <= 1)) {
                                if (fieldClass.isEnum() && !(getDeserializer(fieldClass) instanceof EnumDeserializer)) {
                                    asmEnable2 = false;
                                    break;
                                }
                                i++;
                            }
                        } else {
                            asmEnable2 = false;
                            break;
                        }
                    } else {
                        asmEnable2 = false;
                        break;
                    }
                } else {
                    asmEnable2 = false;
                    break;
                }
            }
            asmEnable2 = false;
        }
        if (asmEnable2 && clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            asmEnable2 = false;
        }
        if (asmEnable2 && TypeUtils.isXmlField(clazz)) {
            asmEnable2 = false;
        }
        if (!asmEnable2) {
            return new JavaBeanDeserializer(this, clazz, type);
        }
        JavaBeanInfo beanInfo2 = JavaBeanInfo.build(clazz, type, this.propertyNamingStrategy);
        try {
            return this.asmFactory.createJavaBeanDeserializer(this, beanInfo2);
        } catch (NoSuchMethodException e) {
            return new JavaBeanDeserializer(this, clazz, type);
        } catch (JSONException e2) {
            return new JavaBeanDeserializer(this, beanInfo2);
        } catch (Exception e3) {
            throw new JSONException("create asm deserializer error, " + clazz.getName(), e3);
        }
    }

    public FieldDeserializer createFieldDeserializer(ParserConfig mapping, JavaBeanInfo beanInfo, FieldInfo fieldInfo) {
        Class<?> clazz = beanInfo.clazz;
        Class<?> fieldClass = fieldInfo.fieldClass;
        Class<?> deserializeUsing = null;
        JSONField annotation = fieldInfo.getAnnotation();
        if (annotation != null && (deserializeUsing = annotation.deserializeUsing()) == Void.class) {
            deserializeUsing = null;
        }
        if (deserializeUsing == null && (fieldClass == List.class || fieldClass == ArrayList.class)) {
            return new ArrayListTypeFieldDeserializer(mapping, clazz, fieldInfo);
        }
        return new DefaultFieldDeserializer(mapping, clazz, fieldInfo);
    }

    public void putDeserializer(Type type, ObjectDeserializer deserializer) {
        this.deserializers.put(type, deserializer);
    }

    public ObjectDeserializer getDeserializer(FieldInfo fieldInfo) {
        return getDeserializer(fieldInfo.fieldClass, fieldInfo.fieldType);
    }

    public boolean isPrimitive(Class<?> clazz) {
        return isPrimitive2(clazz);
    }

    public static boolean isPrimitive2(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == Boolean.class || clazz == Character.class || clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class || clazz == Float.class || clazz == Double.class || clazz == BigInteger.class || clazz == BigDecimal.class || clazz == String.class || clazz == java.util.Date.class || clazz == Date.class || clazz == Time.class || clazz == Timestamp.class || clazz.isEnum();
    }

    public static void parserAllFieldToCache(Class<?> clazz, Map<String, Field> fieldCacheMap) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (!fieldCacheMap.containsKey(fieldName)) {
                fieldCacheMap.put(fieldName, field);
            }
        }
        if (!(clazz.getSuperclass() == null || clazz.getSuperclass() == Object.class)) {
            parserAllFieldToCache(clazz.getSuperclass(), fieldCacheMap);
        }
    }

    public static Field getFieldFromCache(String fieldName, Map<String, Field> fieldCacheMap) {
        Field field = fieldCacheMap.get(fieldName);
        if (field == null) {
            field = fieldCacheMap.get("_" + fieldName);
        }
        if (field == null) {
            field = fieldCacheMap.get("m_" + fieldName);
        }
        if (field != null) {
            return field;
        }
        char c0 = fieldName.charAt(0);
        if (c0 >= 'a' && c0 <= 'z') {
            char[] chars = fieldName.toCharArray();
            chars[0] = (char) (chars[0] - ' ');
            field = fieldCacheMap.get(new String(chars));
        }
        if (fieldName.length() <= 2) {
            return field;
        }
        char c1 = fieldName.charAt(1);
        if (fieldName.length() <= 2 || c0 < 'a' || c0 > 'z' || c1 < 'A' || c1 > 'Z') {
            return field;
        }
        for (Map.Entry<String, Field> entry : fieldCacheMap.entrySet()) {
            if (fieldName.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return field;
    }

    public ClassLoader getDefaultClassLoader() {
        return this.defaultClassLoader;
    }

    public void setDefaultClassLoader(ClassLoader defaultClassLoader2) {
        this.defaultClassLoader = defaultClassLoader2;
    }

    public void addDeny(String name) {
        if (name != null && name.length() != 0) {
            long hash = TypeUtils.fnv1a_64(name);
            if (Arrays.binarySearch(this.denyHashCodes, hash) < 0) {
                long[] hashCodes = new long[(this.denyHashCodes.length + 1)];
                hashCodes[hashCodes.length - 1] = hash;
                System.arraycopy(this.denyHashCodes, 0, hashCodes, 0, this.denyHashCodes.length);
                Arrays.sort(hashCodes);
                this.denyHashCodes = hashCodes;
            }
        }
    }

    public void addAccept(String name) {
        if (name != null && name.length() != 0) {
            long hash = TypeUtils.fnv1a_64(name);
            if (Arrays.binarySearch(this.acceptHashCodes, hash) < 0) {
                long[] hashCodes = new long[(this.acceptHashCodes.length + 1)];
                hashCodes[hashCodes.length - 1] = hash;
                System.arraycopy(this.acceptHashCodes, 0, hashCodes, 0, this.acceptHashCodes.length);
                Arrays.sort(hashCodes);
                this.acceptHashCodes = hashCodes;
            }
        }
    }

    public Class<?> checkAutoType(Class type) {
        if (this.deserializers.get(type) != null) {
            return type;
        }
        return checkAutoType(type.getName(), null, JSON.DEFAULT_PARSER_FEATURE);
    }

    public Class<?> checkAutoType(String typeName, Class<?> expectClass) {
        return checkAutoType(typeName, expectClass, JSON.DEFAULT_PARSER_FEATURE);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0237, code lost:
        com.alibaba.fastjson.util.IOUtils.close(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x023a, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0236, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0300  */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x0306  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0236 A[ExcHandler: all (r0v13 'th' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r5 
      PHI: (r5v19 'is' java.io.InputStream) = (r5v17 'is' java.io.InputStream), (r5v20 'is' java.io.InputStream), (r5v20 'is' java.io.InputStream) binds: [B:86:0x01e5, B:94:0x0222, B:95:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:86:0x01e5] */
    public Class<?> checkAutoType(String typeName, Class<?> expectClass, int features) {
        boolean expectClassFlag;
        long h3;
        Class<?> clazz;
        boolean cacheClass;
        if (typeName == null) {
            return null;
        }
        if (typeName.length() >= 192 || typeName.length() < 3) {
            throw new JSONException("autoType is not support. " + typeName);
        }
        if (expectClass == null) {
            expectClassFlag = false;
        } else if (expectClass == Object.class || expectClass == Serializable.class || expectClass == Cloneable.class || expectClass == Closeable.class || expectClass == EventListener.class || expectClass == Iterable.class || expectClass == Collection.class) {
            expectClassFlag = false;
        } else {
            expectClassFlag = true;
        }
        String className = typeName.replace('$', '.');
        long h1 = (-3750763034362895579L ^ ((long) className.charAt(0))) * 1099511628211L;
        if (h1 == -5808493101479473382L) {
            throw new JSONException("autoType is not support. " + typeName);
        } else if ((((long) className.charAt(className.length() - 1)) ^ h1) * 1099511628211L != 655701488918567152L) {
            long h32 = (((((-3750763034362895579L ^ ((long) className.charAt(0))) * 1099511628211L) ^ ((long) className.charAt(1))) * 1099511628211L) ^ ((long) className.charAt(2))) * 1099511628211L;
            if (this.autoTypeSupport || expectClassFlag) {
                clazz = null;
                long hash = h32;
                int i = 3;
                while (i < className.length()) {
                    long hash2 = (hash ^ ((long) className.charAt(i))) * 1099511628211L;
                    if (Arrays.binarySearch(this.acceptHashCodes, hash2) >= 0 && (clazz = TypeUtils.loadClass(typeName, this.defaultClassLoader, true)) != null) {
                        return clazz;
                    }
                    if (Arrays.binarySearch(this.denyHashCodes, hash2) < 0 || TypeUtils.getClassFromMapping(typeName) != null) {
                        i++;
                        h32 = h32;
                        hash = hash2;
                    } else {
                        throw new JSONException("autoType is not support. " + typeName);
                    }
                }
                h3 = h32;
            } else {
                h3 = h32;
                clazz = null;
            }
            if (clazz == null) {
                clazz = TypeUtils.getClassFromMapping(typeName);
            }
            if (clazz == null) {
                clazz = this.deserializers.findClass(typeName);
            }
            if (clazz == null) {
                clazz = this.typeMapping.get(typeName);
            }
            if (clazz == null) {
                if (!this.autoTypeSupport) {
                    long hash3 = h3;
                    int i2 = 3;
                    while (i2 < className.length()) {
                        hash3 = (hash3 ^ ((long) className.charAt(i2))) * 1099511628211L;
                        if (Arrays.binarySearch(this.denyHashCodes, hash3) >= 0) {
                            throw new JSONException("autoType is not support. " + typeName);
                        } else if (Arrays.binarySearch(this.acceptHashCodes, hash3) >= 0) {
                            if (clazz == null) {
                                clazz = TypeUtils.loadClass(typeName, this.defaultClassLoader, true);
                            }
                            if (expectClass == null || !expectClass.isAssignableFrom(clazz)) {
                                return clazz;
                            }
                            throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
                        } else {
                            i2++;
                            className = className;
                            h1 = h1;
                        }
                    }
                }
                boolean jsonType = false;
                InputStream is = null;
                try {
                    String resource = typeName.replace('.', '/') + ".class";
                    if (this.defaultClassLoader != null) {
                        is = this.defaultClassLoader.getResourceAsStream(resource);
                    } else {
                        is = ParserConfig.class.getClassLoader().getResourceAsStream(resource);
                    }
                    if (is != null) {
                        ClassReader classReader = new ClassReader(is, true);
                        cacheClass = false;
                        TypeCollector visitor = new TypeCollector("<clinit>", new Class[0]);
                        classReader.accept(visitor);
                        jsonType = visitor.hasJsonType();
                    } else {
                        cacheClass = false;
                    }
                } catch (Exception e) {
                    cacheClass = false;
                } catch (Throwable th) {
                }
                IOUtils.close(is);
                int mask = Feature.SupportAutoType.mask;
                boolean autoTypeSupport2 = (!this.autoTypeSupport && (features & mask) == 0 && (JSON.DEFAULT_PARSER_FEATURE & mask) == 0) ? cacheClass : true;
                if (clazz == null && (autoTypeSupport2 || jsonType || expectClassFlag)) {
                    if (autoTypeSupport2 || jsonType) {
                        cacheClass = true;
                    }
                    clazz = TypeUtils.loadClass(typeName, this.defaultClassLoader, cacheClass);
                }
                if (clazz != null) {
                    if (jsonType) {
                        TypeUtils.addMapping(typeName, clazz);
                        return clazz;
                    } else if (ClassLoader.class.isAssignableFrom(clazz) || DataSource.class.isAssignableFrom(clazz) || RowSet.class.isAssignableFrom(clazz)) {
                        throw new JSONException("autoType is not support. " + typeName);
                    } else if (expectClass != null) {
                        if (expectClass.isAssignableFrom(clazz)) {
                            TypeUtils.addMapping(typeName, clazz);
                            return clazz;
                        }
                        throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
                    } else if (JavaBeanInfo.build(clazz, clazz, this.propertyNamingStrategy).creatorConstructor != null) {
                        if (autoTypeSupport2) {
                            throw new JSONException("autoType is not support. " + typeName);
                        } else if (!autoTypeSupport2) {
                            if (clazz != null) {
                                TypeUtils.addMapping(typeName, clazz);
                            }
                            return clazz;
                        } else {
                            throw new JSONException("autoType is not support. " + typeName);
                        }
                    }
                }
                if (!autoTypeSupport2) {
                }
            } else if (expectClass == null || clazz == HashMap.class || expectClass.isAssignableFrom(clazz)) {
                return clazz;
            } else {
                throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
            }
        } else {
            throw new JSONException("autoType is not support. " + typeName);
        }
    }

    public void clearDeserializers() {
        this.deserializers.clear();
        initDeserializers();
    }

    public boolean isJacksonCompatible() {
        return this.jacksonCompatible;
    }

    public void setJacksonCompatible(boolean jacksonCompatible2) {
        this.jacksonCompatible = jacksonCompatible2;
    }

    public void register(String typeName, Class type) {
        this.typeMapping.putIfAbsent(typeName, type);
    }

    public void register(Module module) {
        this.modules.add(module);
    }
}
