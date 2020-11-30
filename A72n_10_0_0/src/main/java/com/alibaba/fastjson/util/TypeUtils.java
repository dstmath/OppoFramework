package com.alibaba.fastjson.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.EnumDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.CalendarCodec;
import com.alibaba.fastjson.serializer.SerializeBeanInfo;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TypeUtils {
    private static volatile boolean classXmlAccessorType_error = false;
    private static volatile Class class_Clob = null;
    private static volatile boolean class_Clob_error = false;
    private static Class<? extends Annotation> class_JacksonCreator = null;
    private static boolean class_JacksonCreator_error = false;
    private static Class<? extends Annotation> class_ManyToMany = null;
    private static boolean class_ManyToMany_error = false;
    private static Class<? extends Annotation> class_OneToMany = null;
    private static boolean class_OneToMany_error = false;
    private static volatile Class class_XmlAccessType = null;
    private static volatile Class class_XmlAccessorType = null;
    public static boolean compatibleWithFieldName;
    public static boolean compatibleWithJavaBean;
    private static volatile Field field_XmlAccessType_FIELD = null;
    private static volatile Object field_XmlAccessType_FIELD_VALUE = null;
    private static volatile Map<Class, String[]> kotlinIgnores;
    private static volatile boolean kotlinIgnores_error;
    private static volatile boolean kotlin_class_klass_error;
    private static volatile boolean kotlin_error;
    private static volatile Constructor kotlin_kclass_constructor;
    private static volatile Method kotlin_kclass_getConstructors;
    private static volatile Method kotlin_kfunction_getParameters;
    private static volatile Method kotlin_kparameter_getName;
    private static volatile Class kotlin_metadata;
    private static volatile boolean kotlin_metadata_error;
    private static ConcurrentMap<String, Class<?>> mappings = new ConcurrentHashMap(256, 0.75f, 1);
    private static Method method_HibernateIsInitialized = null;
    private static boolean method_HibernateIsInitialized_error = false;
    private static volatile Method method_XmlAccessorType_value = null;
    private static Class<?> optionalClass;
    private static boolean optionalClassInited = false;
    private static Method oracleDateMethod;
    private static boolean oracleDateMethodInited = false;
    private static Method oracleTimestampMethod;
    private static boolean oracleTimestampMethodInited = false;
    private static Class<?> pathClass;
    private static boolean pathClass_error = false;
    private static boolean setAccessibleEnable = true;
    private static Class<? extends Annotation> transientClass;
    private static boolean transientClassInited = false;

    static {
        compatibleWithJavaBean = false;
        compatibleWithFieldName = false;
        try {
            compatibleWithJavaBean = "true".equals(IOUtils.getStringProperty(IOUtils.FASTJSON_COMPATIBLEWITHJAVABEAN));
            compatibleWithFieldName = "true".equals(IOUtils.getStringProperty(IOUtils.FASTJSON_COMPATIBLEWITHFIELDNAME));
        } catch (Throwable th) {
        }
        addBaseClassMappings();
    }

    public static boolean isXmlField(Class clazz) {
        Annotation annotation;
        if (class_XmlAccessorType == null && !classXmlAccessorType_error) {
            try {
                class_XmlAccessorType = Class.forName("javax.xml.bind.annotation.XmlAccessorType");
            } catch (Throwable th) {
                classXmlAccessorType_error = true;
            }
        }
        if (class_XmlAccessorType == null || (annotation = clazz.getAnnotation(class_XmlAccessorType)) == null) {
            return false;
        }
        if (method_XmlAccessorType_value == null && !classXmlAccessorType_error) {
            try {
                method_XmlAccessorType_value = class_XmlAccessorType.getMethod("value", new Class[0]);
            } catch (Throwable th2) {
                classXmlAccessorType_error = true;
            }
        }
        if (method_XmlAccessorType_value == null) {
            return false;
        }
        Object value = null;
        if (!classXmlAccessorType_error) {
            try {
                value = method_XmlAccessorType_value.invoke(annotation, new Object[0]);
            } catch (Throwable th3) {
                classXmlAccessorType_error = true;
            }
        }
        if (value == null) {
            return false;
        }
        if (class_XmlAccessType == null && !classXmlAccessorType_error) {
            try {
                class_XmlAccessType = Class.forName("javax.xml.bind.annotation.XmlAccessType");
                field_XmlAccessType_FIELD = class_XmlAccessType.getField("FIELD");
                field_XmlAccessType_FIELD_VALUE = field_XmlAccessType_FIELD.get(null);
            } catch (Throwable th4) {
                classXmlAccessorType_error = true;
            }
        }
        if (value == field_XmlAccessType_FIELD_VALUE) {
            return true;
        }
        return false;
    }

    public static Annotation getXmlAccessorType(Class clazz) {
        if (class_XmlAccessorType == null && !classXmlAccessorType_error) {
            try {
                class_XmlAccessorType = Class.forName("javax.xml.bind.annotation.XmlAccessorType");
            } catch (Throwable th) {
                classXmlAccessorType_error = true;
            }
        }
        if (class_XmlAccessorType == null) {
            return null;
        }
        return clazz.getAnnotation(class_XmlAccessorType);
    }

    public static boolean isClob(Class clazz) {
        if (class_Clob == null && !class_Clob_error) {
            try {
                class_Clob = Class.forName("java.sql.Clob");
            } catch (Throwable th) {
                class_Clob_error = true;
            }
        }
        if (class_Clob == null) {
            return false;
        }
        return class_Clob.isAssignableFrom(clazz);
    }

    public static String castToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static Byte castToByte(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return Byte.valueOf(byteValue((BigDecimal) value));
        }
        if (value instanceof Number) {
            return Byte.valueOf(((Number) value).byteValue());
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            return Byte.valueOf(Byte.parseByte(strVal));
        }
        throw new JSONException("can not cast to byte, value : " + value);
    }

    public static Character castToChar(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Character) {
            return (Character) value;
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0) {
                return null;
            }
            if (strVal.length() == 1) {
                return Character.valueOf(strVal.charAt(0));
            }
            throw new JSONException("can not cast to char, value : " + value);
        }
        throw new JSONException("can not cast to char, value : " + value);
    }

    public static Short castToShort(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return Short.valueOf(shortValue((BigDecimal) value));
        }
        if (value instanceof Number) {
            return Short.valueOf(((Number) value).shortValue());
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            return Short.valueOf(Short.parseShort(strVal));
        }
        throw new JSONException("can not cast to short, value : " + value);
    }

    public static BigDecimal castToBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        String strVal = value.toString();
        if (strVal.length() == 0) {
            return null;
        }
        if (!(value instanceof Map) || ((Map) value).size() != 0) {
            return new BigDecimal(strVal);
        }
        return null;
    }

    public static BigInteger castToBigInteger(Object value) {
        int scale;
        if (value == null) {
            return null;
        }
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if ((value instanceof Float) || (value instanceof Double)) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        if ((value instanceof BigDecimal) && (scale = ((BigDecimal) value).scale()) > -1000 && scale < 1000) {
            return ((BigDecimal) value).toBigInteger();
        }
        String strVal = value.toString();
        if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
            return null;
        }
        return new BigInteger(strVal);
    }

    public static Float castToFloat(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return Float.valueOf(((Number) value).floatValue());
        }
        if (value instanceof String) {
            String strVal = value.toString();
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(44) != 0) {
                strVal = strVal.replaceAll(",", "");
            }
            return Float.valueOf(Float.parseFloat(strVal));
        }
        throw new JSONException("can not cast to float, value : " + value);
    }

    public static Double castToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return Double.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            String strVal = value.toString();
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(44) != 0) {
                strVal = strVal.replaceAll(",", "");
            }
            return Double.valueOf(Double.parseDouble(strVal));
        }
        throw new JSONException("can not cast to double, value : " + value);
    }

    public static Date castToDate(Object value) {
        return castToDate(value, null);
    }

    public static Date castToDate(Object value, String format) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        }
        long longValue = -1;
        if (value instanceof BigDecimal) {
            return new Date(longValue((BigDecimal) value));
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        if (value instanceof String) {
            String strVal = (String) value;
            JSONScanner dateLexer = new JSONScanner(strVal);
            try {
                if (dateLexer.scanISO8601DateIfMatch(false)) {
                    return dateLexer.getCalendar().getTime();
                }
                dateLexer.close();
                if (strVal.startsWith("/Date(") && strVal.endsWith(")/")) {
                    strVal = strVal.substring(6, strVal.length() - 2);
                }
                if (strVal.indexOf(45) > 0 || strVal.indexOf(43) > 0) {
                    if (format == null) {
                        if (strVal.length() == JSON.DEFFAULT_DATE_FORMAT.length() || (strVal.length() == 22 && JSON.DEFFAULT_DATE_FORMAT.equals("yyyyMMddHHmmssSSSZ"))) {
                            format = JSON.DEFFAULT_DATE_FORMAT;
                        } else if (strVal.length() == 10) {
                            format = "yyyy-MM-dd";
                        } else if (strVal.length() == "yyyy-MM-dd HH:mm:ss".length()) {
                            format = "yyyy-MM-dd HH:mm:ss";
                        } else if (strVal.length() == 29 && strVal.charAt(26) == ':' && strVal.charAt(28) == '0') {
                            format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
                        } else {
                            format = "yyyy-MM-dd HH:mm:ss.SSS";
                        }
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat(format, JSON.defaultLocale);
                    dateFormat.setTimeZone(JSON.defaultTimeZone);
                    try {
                        return dateFormat.parse(strVal);
                    } catch (ParseException e) {
                        throw new JSONException("can not cast to Date, value : " + strVal);
                    }
                } else if (strVal.length() == 0) {
                    return null;
                } else {
                    longValue = Long.parseLong(strVal);
                }
            } finally {
                dateLexer.close();
            }
        }
        if (longValue != -1) {
            return new Date(longValue);
        }
        Class<?> clazz = value.getClass();
        if ("oracle.sql.TIMESTAMP".equals(clazz.getName())) {
            if (oracleTimestampMethod == null && !oracleTimestampMethodInited) {
                try {
                    oracleTimestampMethod = clazz.getMethod("toJdbc", new Class[0]);
                } catch (NoSuchMethodException e2) {
                } catch (Throwable th) {
                    oracleTimestampMethodInited = true;
                    throw th;
                }
                oracleTimestampMethodInited = true;
            }
            try {
                return (Date) oracleTimestampMethod.invoke(value, new Object[0]);
            } catch (Exception e3) {
                throw new JSONException("can not cast oracle.sql.TIMESTAMP to Date", e3);
            }
        } else if ("oracle.sql.DATE".equals(clazz.getName())) {
            if (oracleDateMethod == null && !oracleDateMethodInited) {
                try {
                    oracleDateMethod = clazz.getMethod("toJdbc", new Class[0]);
                } catch (NoSuchMethodException e4) {
                } catch (Throwable th2) {
                    oracleDateMethodInited = true;
                    throw th2;
                }
                oracleDateMethodInited = true;
            }
            try {
                return (Date) oracleDateMethod.invoke(value, new Object[0]);
            } catch (Exception e5) {
                throw new JSONException("can not cast oracle.sql.DATE to Date", e5);
            }
        } else {
            throw new JSONException("can not cast to Date, value : " + value);
        }
    }

    public static java.sql.Date castToSqlDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return (java.sql.Date) value;
        }
        if (value instanceof Date) {
            return new java.sql.Date(((Date) value).getTime());
        }
        if (value instanceof Calendar) {
            return new java.sql.Date(((Calendar) value).getTimeInMillis());
        }
        long longValue = 0;
        if (value instanceof BigDecimal) {
            longValue = longValue((BigDecimal) value);
        } else if (value instanceof Number) {
            longValue = ((Number) value).longValue();
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (isNumber(strVal)) {
                longValue = Long.parseLong(strVal);
            } else {
                JSONScanner scanner = new JSONScanner(strVal);
                if (scanner.scanISO8601DateIfMatch(false)) {
                    longValue = scanner.getCalendar().getTime().getTime();
                } else {
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
            }
        }
        if (longValue > 0) {
            return new java.sql.Date(longValue);
        }
        throw new JSONException("can not cast to Date, value : " + value);
    }

    public static long longExtractValue(Number number) {
        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).longValueExact();
        }
        return number.longValue();
    }

    public static Time castToSqlTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Time) {
            return (Time) value;
        }
        if (value instanceof Date) {
            return new Time(((Date) value).getTime());
        }
        if (value instanceof Calendar) {
            return new Time(((Calendar) value).getTimeInMillis());
        }
        long longValue = 0;
        if (value instanceof BigDecimal) {
            longValue = longValue((BigDecimal) value);
        } else if (value instanceof Number) {
            longValue = ((Number) value).longValue();
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 || "null".equalsIgnoreCase(strVal)) {
                return null;
            }
            if (isNumber(strVal)) {
                longValue = Long.parseLong(strVal);
            } else {
                JSONScanner scanner = new JSONScanner(strVal);
                if (scanner.scanISO8601DateIfMatch(false)) {
                    longValue = scanner.getCalendar().getTime().getTime();
                } else {
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
            }
        }
        if (longValue > 0) {
            return new Time(longValue);
        }
        throw new JSONException("can not cast to Date, value : " + value);
    }

    public static Timestamp castToTimestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Calendar) {
            return new Timestamp(((Calendar) value).getTimeInMillis());
        }
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        if (value instanceof Date) {
            return new Timestamp(((Date) value).getTime());
        }
        long longValue = 0;
        if (value instanceof BigDecimal) {
            longValue = longValue((BigDecimal) value);
        } else if (value instanceof Number) {
            longValue = ((Number) value).longValue();
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.endsWith(".000000000")) {
                strVal = strVal.substring(0, strVal.length() - 10);
            } else if (strVal.endsWith(".000000")) {
                strVal = strVal.substring(0, strVal.length() - 7);
            }
            if (isNumber(strVal)) {
                longValue = Long.parseLong(strVal);
            } else {
                JSONScanner scanner = new JSONScanner(strVal);
                if (scanner.scanISO8601DateIfMatch(false)) {
                    longValue = scanner.getCalendar().getTime().getTime();
                } else {
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
            }
        }
        if (longValue > 0) {
            return new Timestamp(longValue);
        }
        throw new JSONException("can not cast to Timestamp, value : " + value);
    }

    public static boolean isNumber(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static Long castToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return Long.valueOf(longValue((BigDecimal) value));
        }
        if (value instanceof Number) {
            return Long.valueOf(((Number) value).longValue());
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(44) != 0) {
                strVal = strVal.replaceAll(",", "");
            }
            try {
                return Long.valueOf(Long.parseLong(strVal));
            } catch (NumberFormatException e) {
                JSONScanner dateParser = new JSONScanner(strVal);
                Calendar calendar = null;
                if (dateParser.scanISO8601DateIfMatch(false)) {
                    calendar = dateParser.getCalendar();
                }
                dateParser.close();
                if (calendar != null) {
                    return Long.valueOf(calendar.getTimeInMillis());
                }
            }
        }
        if (value instanceof Map) {
            Map map = (Map) value;
            if (map.size() == 2 && map.containsKey("andIncrement") && map.containsKey("andDecrement")) {
                Iterator iter = map.values().iterator();
                iter.next();
                return castToLong(iter.next());
            }
        }
        throw new JSONException("can not cast to long, value : " + value);
    }

    public static byte byteValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }
        int scale = decimal.scale();
        if (scale < -100 || scale > 100) {
            return decimal.byteValueExact();
        }
        return decimal.byteValue();
    }

    public static short shortValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }
        int scale = decimal.scale();
        if (scale < -100 || scale > 100) {
            return decimal.shortValueExact();
        }
        return decimal.shortValue();
    }

    public static int intValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }
        int scale = decimal.scale();
        if (scale < -100 || scale > 100) {
            return decimal.intValueExact();
        }
        return decimal.intValue();
    }

    public static long longValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }
        int scale = decimal.scale();
        if (scale < -100 || scale > 100) {
            return decimal.longValueExact();
        }
        return decimal.longValue();
    }

    public static Integer castToInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof BigDecimal) {
            return Integer.valueOf(intValue((BigDecimal) value));
        }
        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(44) != 0) {
                strVal = strVal.replaceAll(",", "");
            }
            return Integer.valueOf(Integer.parseInt(strVal));
        } else if (value instanceof Boolean) {
            return Integer.valueOf(((Boolean) value).booleanValue() ? 1 : 0);
        } else {
            if (value instanceof Map) {
                Map map = (Map) value;
                if (map.size() == 2 && map.containsKey("andIncrement") && map.containsKey("andDecrement")) {
                    Iterator iter = map.values().iterator();
                    iter.next();
                    return castToInt(iter.next());
                }
            }
            throw new JSONException("can not cast to int, value : " + value);
        }
    }

    public static byte[] castToBytes(Object value) {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof String) {
            return IOUtils.decodeBase64((String) value);
        }
        throw new JSONException("can not cast to int, value : " + value);
    }

    public static Boolean castToBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        boolean z = false;
        if (value instanceof BigDecimal) {
            if (intValue((BigDecimal) value) == 1) {
                z = true;
            }
            return Boolean.valueOf(z);
        } else if (value instanceof Number) {
            if (((Number) value).intValue() == 1) {
                z = true;
            }
            return Boolean.valueOf(z);
        } else {
            if (value instanceof String) {
                String strVal = (String) value;
                if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                    return null;
                }
                if ("true".equalsIgnoreCase(strVal) || "1".equals(strVal)) {
                    return Boolean.TRUE;
                }
                if ("false".equalsIgnoreCase(strVal) || "0".equals(strVal)) {
                    return Boolean.FALSE;
                }
                if ("Y".equalsIgnoreCase(strVal) || "T".equals(strVal)) {
                    return Boolean.TRUE;
                }
                if ("F".equalsIgnoreCase(strVal) || "N".equals(strVal)) {
                    return Boolean.FALSE;
                }
            }
            throw new JSONException("can not cast to boolean, value : " + value);
        }
    }

    public static <T> T castToJavaBean(Object obj, Class<T> clazz) {
        return (T) cast(obj, (Class<Object>) clazz, ParserConfig.getGlobalInstance());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public static <T> T cast(Object obj, Class<T> clazz, ParserConfig config) {
        Calendar calendar;
        if (obj == 0) {
            if (clazz == Integer.TYPE) {
                return (T) 0;
            }
            if (clazz == Long.TYPE) {
                return (T) 0L;
            }
            if (clazz == Short.TYPE) {
                return (T) 0;
            }
            if (clazz == Byte.TYPE) {
                return (T) (byte) 0;
            }
            if (clazz == Float.TYPE) {
                return (T) Float.valueOf(0.0f);
            }
            if (clazz == Double.TYPE) {
                return (T) Double.valueOf(0.0d);
            }
            if (clazz == Boolean.TYPE) {
                return (T) Boolean.FALSE;
            }
            return null;
        } else if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        } else if (clazz == obj.getClass()) {
            return obj;
        } else {
            if (!(obj instanceof Map)) {
                if (clazz.isArray()) {
                    if (obj instanceof Collection) {
                        Collection<Object> collection = (Collection) obj;
                        int index = 0;
                        T t = (T) Array.newInstance(clazz.getComponentType(), collection.size());
                        for (Object item : collection) {
                            Array.set(t, index, cast(item, (Class<Object>) clazz.getComponentType(), config));
                            index++;
                        }
                        return t;
                    } else if (clazz == byte[].class) {
                        return (T) castToBytes(obj);
                    }
                }
                if (clazz.isAssignableFrom(obj.getClass())) {
                    return obj;
                }
                if (clazz == Boolean.TYPE || clazz == Boolean.class) {
                    return (T) castToBoolean(obj);
                }
                if (clazz == Byte.TYPE || clazz == Byte.class) {
                    return (T) castToByte(obj);
                }
                if (clazz == Character.TYPE || clazz == Character.class) {
                    return (T) castToChar(obj);
                }
                if (clazz == Short.TYPE || clazz == Short.class) {
                    return (T) castToShort(obj);
                }
                if (clazz == Integer.TYPE || clazz == Integer.class) {
                    return (T) castToInt(obj);
                }
                if (clazz == Long.TYPE || clazz == Long.class) {
                    return (T) castToLong(obj);
                }
                if (clazz == Float.TYPE || clazz == Float.class) {
                    return (T) castToFloat(obj);
                }
                if (clazz == Double.TYPE || clazz == Double.class) {
                    return (T) castToDouble(obj);
                }
                if (clazz == String.class) {
                    return (T) castToString(obj);
                }
                if (clazz == BigDecimal.class) {
                    return (T) castToBigDecimal(obj);
                }
                if (clazz == BigInteger.class) {
                    return (T) castToBigInteger(obj);
                }
                if (clazz == Date.class) {
                    return (T) castToDate(obj);
                }
                if (clazz == java.sql.Date.class) {
                    return (T) castToSqlDate(obj);
                }
                if (clazz == Time.class) {
                    return (T) castToSqlTime(obj);
                }
                if (clazz == Timestamp.class) {
                    return (T) castToTimestamp(obj);
                }
                if (clazz.isEnum()) {
                    return (T) castToEnum(obj, clazz, config);
                }
                if (Calendar.class.isAssignableFrom(clazz)) {
                    Date date = castToDate(obj);
                    if (clazz == Calendar.class) {
                        calendar = (T) Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
                    } else {
                        try {
                            calendar = (T) clazz.newInstance();
                        } catch (Exception e) {
                            throw new JSONException("can not cast to : " + clazz.getName(), e);
                        }
                    }
                    calendar.setTime(date);
                    return (T) calendar;
                }
                String className = clazz.getName();
                if (className.equals("javax.xml.datatype.XMLGregorianCalendar")) {
                    Date date2 = castToDate(obj);
                    Calendar calendar2 = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
                    calendar2.setTime(date2);
                    return (T) CalendarCodec.instance.createXMLGregorianCalendar(calendar2);
                }
                if (obj instanceof String) {
                    String strVal = (String) obj;
                    if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                        return null;
                    }
                    if (clazz == Currency.class) {
                        return (T) Currency.getInstance(strVal);
                    }
                    if (clazz == Locale.class) {
                        return (T) toLocale(strVal);
                    }
                    if (className.startsWith("java.time.")) {
                        return (T) JSON.parseObject(JSON.toJSONString(strVal), clazz);
                    }
                }
                if (config.getDeserializers().get(clazz) != null) {
                    return (T) JSON.parseObject(JSON.toJSONString(obj), clazz);
                }
                throw new JSONException("can not cast to : " + clazz.getName());
            } else if (clazz == Map.class) {
                return obj;
            } else {
                return (clazz != Object.class || ((Map) obj).containsKey(JSON.DEFAULT_TYPE_KEY)) ? (T) castToJavaBean((Map) obj, clazz, config) : obj;
            }
        }
    }

    public static Locale toLocale(String strVal) {
        String[] items = strVal.split("_");
        if (items.length == 1) {
            return new Locale(items[0]);
        }
        if (items.length == 2) {
            return new Locale(items[0], items[1]);
        }
        return new Locale(items[0], items[1], items[2]);
    }

    public static <T> T castToEnum(Object obj, Class<T> clazz, ParserConfig mapping) {
        try {
            if (obj instanceof String) {
                String name = (String) obj;
                if (name.length() == 0) {
                    return null;
                }
                if (mapping == null) {
                    mapping = ParserConfig.getGlobalInstance();
                }
                ObjectDeserializer derializer = mapping.getDeserializer(clazz);
                return derializer instanceof EnumDeserializer ? (T) ((EnumDeserializer) derializer).getEnumByHashCode(fnv1a_64(name)) : (T) Enum.valueOf(clazz, name);
            }
            if (obj instanceof BigDecimal) {
                int ordinal = intValue((BigDecimal) obj);
                T[] enumConstants = clazz.getEnumConstants();
                if (ordinal < enumConstants.length) {
                    return enumConstants[ordinal];
                }
            }
            if (obj instanceof Number) {
                int ordinal2 = ((Number) obj).intValue();
                T[] enumConstants2 = clazz.getEnumConstants();
                if (ordinal2 < enumConstants2.length) {
                    return enumConstants2[ordinal2];
                }
            }
            throw new JSONException("can not cast to : " + clazz.getName());
        } catch (Exception ex) {
            throw new JSONException("can not cast to : " + clazz.getName(), ex);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public static <T> T cast(Object obj, Type type, ParserConfig mapping) {
        if (obj == 0) {
            return null;
        }
        if (type instanceof Class) {
            return (T) cast(obj, (Class<Object>) ((Class) type), mapping);
        }
        if (type instanceof ParameterizedType) {
            return (T) cast(obj, (ParameterizedType) type, mapping);
        }
        if (obj instanceof String) {
            String strVal = (String) obj;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
        }
        if (type instanceof TypeVariable) {
            return obj;
        }
        throw new JSONException("can not cast to : " + type);
    }

    public static <T> T cast(Object obj, ParameterizedType type, ParserConfig mapping) {
        Object itemValue;
        Object itemValue2;
        Type rawTye = type.getRawType();
        if (rawTye == List.class || rawTye == ArrayList.class) {
            Type itemType = type.getActualTypeArguments()[0];
            if (obj instanceof List) {
                List listObj = (List) obj;
                T t = (T) new ArrayList(listObj.size());
                for (int i = 0; i < listObj.size(); i++) {
                    Object item = listObj.get(i);
                    if (!(itemType instanceof Class)) {
                        itemValue2 = cast(item, itemType, mapping);
                    } else if (item == null || item.getClass() != JSONObject.class) {
                        itemValue2 = cast(item, (Class<Object>) ((Class) itemType), mapping);
                    } else {
                        itemValue2 = ((JSONObject) item).toJavaObject((Class) itemType, mapping, 0);
                    }
                    t.add(itemValue2);
                }
                return t;
            }
        }
        if (rawTye == Set.class || rawTye == HashSet.class || rawTye == TreeSet.class || rawTye == Collection.class || rawTye == List.class || rawTye == ArrayList.class) {
            Type itemType2 = type.getActualTypeArguments()[0];
            if (obj instanceof Iterable) {
                Collection collection = (rawTye == Set.class || rawTye == HashSet.class) ? (T) new HashSet() : rawTye == TreeSet.class ? (T) new TreeSet() : (T) new ArrayList();
                for (Object item2 : (Iterable) obj) {
                    if (!(itemType2 instanceof Class)) {
                        itemValue = cast(item2, itemType2, mapping);
                    } else if (item2 == null || item2.getClass() != JSONObject.class) {
                        itemValue = cast(item2, (Class<Object>) ((Class) itemType2), mapping);
                    } else {
                        itemValue = ((JSONObject) item2).toJavaObject((Class) itemType2, mapping, 0);
                    }
                    collection.add(itemValue);
                }
                return (T) collection;
            }
        }
        if (rawTye == Map.class || rawTye == HashMap.class) {
            Type keyType = type.getActualTypeArguments()[0];
            Type valueType = type.getActualTypeArguments()[1];
            if (obj instanceof Map) {
                T t2 = (T) new HashMap();
                for (Map.Entry entry : ((Map) obj).entrySet()) {
                    t2.put(cast(entry.getKey(), keyType, mapping), cast(entry.getValue(), valueType, mapping));
                }
                return t2;
            }
        }
        if ((obj instanceof String) && ((String) obj).length() == 0) {
            return null;
        }
        if (type.getActualTypeArguments().length == 1 && (type.getActualTypeArguments()[0] instanceof WildcardType)) {
            return (T) cast(obj, rawTye, mapping);
        }
        if (rawTye == Map.Entry.class && (obj instanceof Map) && ((Map) obj).size() == 1) {
            return (T) ((Map.Entry) ((Map) obj).entrySet().iterator().next());
        }
        if (rawTye instanceof Class) {
            if (mapping == null) {
                mapping = ParserConfig.global;
            }
            ObjectDeserializer deserializer = mapping.getDeserializer(rawTye);
            if (deserializer != null) {
                return (T) deserializer.deserialze(new DefaultJSONParser(JSON.toJSONString(obj), mapping), type, null);
            }
        }
        throw new JSONException("can not cast to : " + type);
    }

    public static <T> T castToJavaBean(Map<String, Object> map, Class<T> clazz, ParserConfig config) {
        JSONObject object;
        int lineNumber;
        if (clazz == StackTraceElement.class) {
            try {
                String declaringClass = (String) map.get("className");
                String methodName = (String) map.get("methodName");
                String fileName = (String) map.get("fileName");
                Number value = (Number) map.get("lineNumber");
                if (value == null) {
                    lineNumber = 0;
                } else if (value instanceof BigDecimal) {
                    lineNumber = ((BigDecimal) value).intValueExact();
                } else {
                    lineNumber = value.intValue();
                    return (T) new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
                }
                return (T) new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
            } catch (Exception e) {
                throw new JSONException(e.getMessage(), e);
            }
        } else {
            Object iClassObject = map.get(JSON.DEFAULT_TYPE_KEY);
            if (iClassObject instanceof String) {
                String className = (String) iClassObject;
                if (config == null) {
                    config = ParserConfig.global;
                }
                Class<?> loadClazz = config.checkAutoType(className, null);
                if (loadClazz == null) {
                    throw new ClassNotFoundException(className + " not found");
                } else if (!loadClazz.equals(clazz)) {
                    return (T) castToJavaBean(map, loadClazz, config);
                }
            }
            if (clazz.isInterface()) {
                if (map instanceof JSONObject) {
                    object = (JSONObject) map;
                } else {
                    object = new JSONObject(map);
                }
                if (config == null) {
                    config = ParserConfig.getGlobalInstance();
                }
                if (config.getDeserializers().get(clazz) != null) {
                    return (T) JSON.parseObject(JSON.toJSONString(object), clazz);
                }
                return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, object);
            }
            if (clazz == Locale.class) {
                Object arg0 = map.get("language");
                Object arg1 = map.get("country");
                if (arg0 instanceof String) {
                    String language = (String) arg0;
                    if (arg1 instanceof String) {
                        return (T) new Locale(language, (String) arg1);
                    }
                    if (arg1 == null) {
                        return (T) new Locale(language);
                    }
                }
            }
            if (clazz == String.class && (map instanceof JSONObject)) {
                return (T) map.toString();
            }
            if (clazz == LinkedHashMap.class && (map instanceof JSONObject)) {
                T t = (T) ((JSONObject) map).getInnerMap();
                if (t instanceof LinkedHashMap) {
                    return t;
                }
                new LinkedHashMap().putAll(t);
            }
            if (config == null) {
                config = ParserConfig.getGlobalInstance();
            }
            JavaBeanDeserializer javaBeanDeser = null;
            ObjectDeserializer deserizer = config.getDeserializer(clazz);
            if (deserizer instanceof JavaBeanDeserializer) {
                javaBeanDeser = (JavaBeanDeserializer) deserizer;
            }
            if (javaBeanDeser != null) {
                return (T) javaBeanDeser.createInstance(map, config);
            }
            throw new JSONException("can not get javaBeanDeserializer. " + clazz.getName());
        }
    }

    private static void addBaseClassMappings() {
        mappings.put("byte", Byte.TYPE);
        mappings.put("short", Short.TYPE);
        mappings.put("int", Integer.TYPE);
        mappings.put("long", Long.TYPE);
        mappings.put("float", Float.TYPE);
        mappings.put("double", Double.TYPE);
        mappings.put("boolean", Boolean.TYPE);
        mappings.put("char", Character.TYPE);
        mappings.put("[byte", byte[].class);
        mappings.put("[short", short[].class);
        mappings.put("[int", int[].class);
        mappings.put("[long", long[].class);
        mappings.put("[float", float[].class);
        mappings.put("[double", double[].class);
        mappings.put("[boolean", boolean[].class);
        mappings.put("[char", char[].class);
        mappings.put("[B", byte[].class);
        mappings.put("[S", short[].class);
        mappings.put("[I", int[].class);
        mappings.put("[J", long[].class);
        mappings.put("[F", float[].class);
        mappings.put("[D", double[].class);
        mappings.put("[C", char[].class);
        mappings.put("[Z", boolean[].class);
        Class[] classes = {Object.class, Cloneable.class, loadClass("java.lang.AutoCloseable"), Exception.class, RuntimeException.class, IllegalAccessError.class, IllegalAccessException.class, IllegalArgumentException.class, IllegalMonitorStateException.class, IllegalStateException.class, IllegalThreadStateException.class, IndexOutOfBoundsException.class, InstantiationError.class, InstantiationException.class, InternalError.class, InterruptedException.class, LinkageError.class, NegativeArraySizeException.class, NoClassDefFoundError.class, NoSuchFieldError.class, NoSuchFieldException.class, NoSuchMethodError.class, NoSuchMethodException.class, NullPointerException.class, NumberFormatException.class, OutOfMemoryError.class, SecurityException.class, StackOverflowError.class, StringIndexOutOfBoundsException.class, TypeNotPresentException.class, VerifyError.class, StackTraceElement.class, HashMap.class, Hashtable.class, TreeMap.class, IdentityHashMap.class, WeakHashMap.class, LinkedHashMap.class, HashSet.class, LinkedHashSet.class, TreeSet.class, ArrayList.class, TimeUnit.class, ConcurrentHashMap.class, loadClass("java.util.concurrent.ConcurrentSkipListMap"), loadClass("java.util.concurrent.ConcurrentSkipListSet"), AtomicInteger.class, AtomicLong.class, Collections.EMPTY_MAP.getClass(), Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Number.class, String.class, BigDecimal.class, BigInteger.class, BitSet.class, Calendar.class, Date.class, Locale.class, UUID.class, Time.class, java.sql.Date.class, Timestamp.class, SimpleDateFormat.class, JSONObject.class, JSONPObject.class, JSONArray.class};
        for (Class clazz : classes) {
            if (clazz != null) {
                mappings.put(clazz.getName(), clazz);
            }
        }
        for (String className : new String[]{"java.util.Collections$UnmodifiableMap"}) {
            Class<?> clazz2 = loadClass(className);
            if (clazz2 == null) {
                break;
            }
            mappings.put(clazz2.getName(), clazz2);
        }
        for (String className2 : new String[]{"java.awt.Rectangle", "java.awt.Point", "java.awt.Font", "java.awt.Color"}) {
            Class<?> clazz3 = loadClass(className2);
            if (clazz3 == null) {
                break;
            }
            mappings.put(clazz3.getName(), clazz3);
        }
        for (String className3 : new String[]{"org.springframework.util.LinkedMultiValueMap", "org.springframework.util.LinkedCaseInsensitiveMap", "org.springframework.remoting.support.RemoteInvocation", "org.springframework.remoting.support.RemoteInvocationResult", "org.springframework.security.web.savedrequest.DefaultSavedRequest", "org.springframework.security.web.savedrequest.SavedCookie", "org.springframework.security.web.csrf.DefaultCsrfToken", "org.springframework.security.web.authentication.WebAuthenticationDetails", "org.springframework.security.core.context.SecurityContextImpl", "org.springframework.security.authentication.UsernamePasswordAuthenticationToken", "org.springframework.security.core.authority.SimpleGrantedAuthority", "org.springframework.security.core.userdetails.User", "org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken", "org.springframework.security.oauth2.common.DefaultOAuth2AccessToken", "org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken", "org.springframework.cache.support.NullValue"}) {
            Class<?> clazz4 = loadClass(className3);
            if (clazz4 != null) {
                mappings.put(clazz4.getName(), clazz4);
            }
        }
    }

    public static void clearClassMapping() {
        mappings.clear();
        addBaseClassMappings();
    }

    public static void addMapping(String className, Class<?> clazz) {
        mappings.put(className, clazz);
    }

    public static Class<?> loadClass(String className) {
        return loadClass(className, null);
    }

    public static boolean isPath(Class<?> clazz) {
        if (pathClass == null && !pathClass_error) {
            try {
                pathClass = Class.forName("java.nio.file.Path");
            } catch (Throwable th) {
                pathClass_error = true;
            }
        }
        if (pathClass != null) {
            return pathClass.isAssignableFrom(clazz);
        }
        return false;
    }

    public static Class<?> getClassFromMapping(String className) {
        return mappings.get(className);
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader) {
        return loadClass(className, classLoader, false);
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader, boolean cache) {
        if (className == null || className.length() == 0 || className.length() > 128) {
            return null;
        }
        Class<?> clazz = mappings.get(className);
        if (clazz != null) {
            return clazz;
        }
        if (className.charAt(0) == '[') {
            return Array.newInstance(loadClass(className.substring(1), classLoader), 0).getClass();
        }
        if (className.startsWith("L") && className.endsWith(";")) {
            return loadClass(className.substring(1, className.length() - 1), classLoader);
        }
        if (classLoader != null) {
            try {
                Class<?> clazz2 = classLoader.loadClass(className);
                if (cache) {
                    mappings.put(className, clazz2);
                }
                return clazz2;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (!(contextClassLoader == null || contextClassLoader == classLoader)) {
                Class<?> clazz3 = contextClassLoader.loadClass(className);
                if (cache) {
                    mappings.put(className, clazz3);
                }
                return clazz3;
            }
        } catch (Throwable th) {
        }
        try {
            Class<?> clazz4 = Class.forName(className);
            if (cache) {
                mappings.put(className, clazz4);
            }
            return clazz4;
        } catch (Throwable th2) {
            return clazz;
        }
    }

    public static SerializeBeanInfo buildBeanInfo(Class<?> beanType, Map<String, String> aliasMap, PropertyNamingStrategy propertyNamingStrategy) {
        return buildBeanInfo(beanType, aliasMap, propertyNamingStrategy, false);
    }

    public static SerializeBeanInfo buildBeanInfo(Class<?> beanType, Map<String, String> aliasMap, PropertyNamingStrategy propertyNamingStrategy, boolean fieldBased) {
        String typeKey;
        String typeName;
        PropertyNamingStrategy propertyNamingStrategy2;
        int features;
        String[] orders;
        List<FieldInfo> fieldInfoList;
        List<FieldInfo> sortedFieldList;
        PropertyNamingStrategy propertyNamingStrategy3;
        JSONType jsonType = (JSONType) getAnnotation(beanType, JSONType.class);
        String typeKey2 = null;
        if (jsonType != null) {
            String[] orders2 = jsonType.orders();
            String typeName2 = jsonType.typeName();
            if (typeName2.length() == 0) {
                typeName2 = null;
            }
            PropertyNamingStrategy jsonTypeNaming = jsonType.naming();
            if (jsonTypeNaming != PropertyNamingStrategy.CamelCase) {
                propertyNamingStrategy3 = jsonTypeNaming;
            } else {
                propertyNamingStrategy3 = propertyNamingStrategy;
            }
            int features2 = SerializerFeature.of(jsonType.serialzeFeatures());
            Class<?> supperClass = beanType.getSuperclass();
            while (supperClass != null && supperClass != Object.class) {
                JSONType superJsonType = (JSONType) getAnnotation(supperClass, JSONType.class);
                if (superJsonType == null) {
                    break;
                }
                typeKey2 = superJsonType.typeKey();
                if (typeKey2.length() != 0) {
                    break;
                }
                supperClass = supperClass.getSuperclass();
            }
            String typeKey3 = typeKey2;
            for (Class<?> interfaceClass : beanType.getInterfaces()) {
                JSONType superJsonType2 = (JSONType) getAnnotation(interfaceClass, JSONType.class);
                if (superJsonType2 != null) {
                    typeKey3 = superJsonType2.typeKey();
                    if (typeKey3.length() != 0) {
                        break;
                    }
                }
            }
            if (typeKey3 != null && typeKey3.length() == 0) {
                typeKey3 = null;
            }
            orders = orders2;
            typeName = typeName2;
            propertyNamingStrategy2 = propertyNamingStrategy3;
            features = features2;
            typeKey = typeKey3;
        } else {
            propertyNamingStrategy2 = propertyNamingStrategy;
            orders = null;
            typeName = null;
            typeKey = null;
            features = 0;
        }
        Map<String, Field> fieldCacheMap = new HashMap<>();
        ParserConfig.parserAllFieldToCache(beanType, fieldCacheMap);
        if (fieldBased) {
            fieldInfoList = computeGettersWithFieldBase(beanType, aliasMap, false, propertyNamingStrategy2);
        } else {
            fieldInfoList = computeGetters(beanType, jsonType, aliasMap, fieldCacheMap, false, propertyNamingStrategy2);
        }
        FieldInfo[] fields = new FieldInfo[fieldInfoList.size()];
        fieldInfoList.toArray(fields);
        if (orders == null || orders.length == 0) {
            sortedFieldList = new ArrayList<>(fieldInfoList);
            Collections.sort(sortedFieldList);
        } else if (fieldBased) {
            sortedFieldList = computeGettersWithFieldBase(beanType, aliasMap, true, propertyNamingStrategy2);
        } else {
            sortedFieldList = computeGetters(beanType, jsonType, aliasMap, fieldCacheMap, true, propertyNamingStrategy2);
        }
        FieldInfo[] sortedFields = new FieldInfo[sortedFieldList.size()];
        sortedFieldList.toArray(sortedFields);
        if (Arrays.equals(sortedFields, fields)) {
            sortedFields = fields;
        }
        return new SerializeBeanInfo(beanType, jsonType, typeName, typeKey, features, fields, sortedFields);
    }

    public static List<FieldInfo> computeGettersWithFieldBase(Class<?> clazz, Map<String, String> aliasMap, boolean sorted, PropertyNamingStrategy propertyNamingStrategy) {
        Map<String, FieldInfo> fieldInfoMap = new LinkedHashMap<>();
        for (Class<?> currentClass = clazz; currentClass != null; currentClass = currentClass.getSuperclass()) {
            computeFields(currentClass, aliasMap, propertyNamingStrategy, fieldInfoMap, currentClass.getDeclaredFields());
        }
        return getFieldInfos(clazz, sorted, fieldInfoMap);
    }

    public static List<FieldInfo> computeGetters(Class<?> clazz, Map<String, String> aliasMap) {
        return computeGetters(clazz, aliasMap, true);
    }

    public static List<FieldInfo> computeGetters(Class<?> clazz, Map<String, String> aliasMap, boolean sorted) {
        Map<String, Field> fieldCacheMap = new HashMap<>();
        ParserConfig.parserAllFieldToCache(clazz, fieldCacheMap);
        return computeGetters(clazz, (JSONType) getAnnotation(clazz, JSONType.class), aliasMap, fieldCacheMap, sorted, PropertyNamingStrategy.CamelCase);
    }

    /* JADX INFO: Multiple debug info for r11v10 'annotation'  com.alibaba.fastjson.annotation.JSONField: [D('fieldInfoMap' java.util.Map<java.lang.String, com.alibaba.fastjson.util.FieldInfo>), D('annotation' com.alibaba.fastjson.annotation.JSONField)] */
    /* JADX INFO: Multiple debug info for r0v70 com.alibaba.fastjson.annotation.JSONField: [D('annotation' com.alibaba.fastjson.annotation.JSONField), D('field' java.lang.reflect.Field)] */
    /* JADX INFO: Multiple debug info for r1v69 int: [D('paramAnnotationArrays' java.lang.annotation.Annotation[][]), D('annotation' com.alibaba.fastjson.annotation.JSONField)] */
    /* JADX INFO: Multiple debug info for r0v82 java.lang.String[]: [D('constructors' java.lang.reflect.Constructor[]), D('paramNames_sorted' java.lang.String[])] */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x036c, code lost:
        if (r0 == null) goto L_0x0272;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x04b2, code lost:
        if (r0 == null) goto L_0x0400;
     */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x03e7  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x03f8  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x01b7  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x026a  */
    public static List<FieldInfo> computeGetters(Class<?> clazz, JSONType jsonType, Map<String, String> aliasMap, Map<String, Field> fieldCacheMap, boolean sorted, PropertyNamingStrategy propertyNamingStrategy) {
        Method[] methods;
        int i;
        int i2;
        Map<String, FieldInfo> fieldInfoMap;
        Annotation[][] paramAnnotationArrays;
        String[] paramNames;
        Annotation[][] paramAnnotationArrays2;
        String methodName;
        Constructor[] constructors;
        int ordinal;
        short[] paramNameMapping;
        JSONField annotation;
        Map<String, FieldInfo> fieldInfoMap2;
        Method method;
        String methodName2;
        int ordinal2;
        JSONField annotation2;
        String methodName3;
        JSONField annotation3;
        Map<String, FieldInfo> fieldInfoMap3;
        String propertyName;
        int parserFeatures;
        int parserFeatures2;
        JSONField fieldAnnotation;
        String propertyName2;
        String propertyName3;
        int parserFeatures3;
        int serialzeFeatures;
        JSONField fieldAnnotation2;
        Boolean fieldAnnotationAndNameExists;
        char ch;
        String propertyName4;
        JSONField annotation4;
        Annotation[][] paramAnnotationArrays3;
        Field field;
        Map<String, Field> map = fieldCacheMap;
        Map<String, FieldInfo> fieldInfoMap4 = new LinkedHashMap<>();
        boolean kotlin = isKotlin(clazz);
        Constructor[] constructors2 = null;
        Annotation[][] paramAnnotationArrays4 = null;
        String[] paramNames2 = null;
        short[] paramNameMapping2 = null;
        Method[] methods2 = clazz.getMethods();
        int length = methods2.length;
        int i3 = 0;
        while (i3 < length) {
            Method method2 = methods2[i3];
            String methodName4 = method2.getName();
            int ordinal3 = 0;
            int serialzeFeatures2 = 0;
            int parserFeatures4 = 0;
            String label = null;
            if (Modifier.isStatic(method2.getModifiers())) {
                paramAnnotationArrays = paramAnnotationArrays4;
            } else {
                paramAnnotationArrays = paramAnnotationArrays4;
                if (!method2.getReturnType().equals(Void.TYPE) && method2.getParameterTypes().length == 0 && method2.getReturnType() != ClassLoader.class && ((!methodName4.equals("getMetaClass") || !method2.getReturnType().getName().equals("groovy.lang.MetaClass")) && ((!methodName4.equals("getSuppressed") || method2.getDeclaringClass() != Throwable.class) && (!kotlin || !isKotlinIgnore(clazz, methodName4))))) {
                    boolean fieldAnnotationAndNameExists2 = false;
                    JSONField annotation5 = (JSONField) method2.getAnnotation(JSONField.class);
                    if (annotation5 == null) {
                        annotation5 = getSuperMethodAnnotation(clazz, method2);
                    }
                    if (annotation5 != null || !kotlin) {
                        annotation4 = annotation5;
                        paramNameMapping = paramNameMapping2;
                        ordinal = 0;
                        methodName = methodName4;
                        constructors = constructors2;
                        paramNames = paramNames2;
                        paramAnnotationArrays2 = paramAnnotationArrays;
                    } else {
                        if (constructors2 == null) {
                            constructors2 = clazz.getDeclaredConstructors();
                            Constructor creatorConstructor = getKoltinConstructor(constructors2);
                            if (creatorConstructor != null) {
                                paramAnnotationArrays = creatorConstructor.getParameterAnnotations();
                                paramNames2 = getKoltinConstructorParameters(clazz);
                                if (paramNames2 != null) {
                                    String[] paramNames_sorted = new String[paramNames2.length];
                                    annotation4 = annotation5;
                                    System.arraycopy(paramNames2, 0, paramNames_sorted, 0, paramNames2.length);
                                    Arrays.sort(paramNames_sorted);
                                    short[] paramNameMapping3 = new short[paramNames2.length];
                                    short p = 0;
                                    while (true) {
                                        ordinal = ordinal3;
                                        if (p >= paramNames2.length) {
                                            break;
                                        }
                                        paramNameMapping3[Arrays.binarySearch(paramNames_sorted, paramNames2[p])] = p;
                                        p = (short) (p + 1);
                                        ordinal3 = ordinal;
                                    }
                                    paramNames2 = paramNames_sorted;
                                    paramNameMapping2 = paramNameMapping3;
                                    paramAnnotationArrays3 = paramAnnotationArrays;
                                    constructors2 = constructors2;
                                }
                            }
                            annotation4 = annotation5;
                            ordinal = 0;
                            paramAnnotationArrays3 = paramAnnotationArrays;
                        } else {
                            annotation4 = annotation5;
                            ordinal = 0;
                            paramAnnotationArrays3 = paramAnnotationArrays;
                        }
                        if (paramNames2 == null || paramNameMapping2 == null || !methodName4.startsWith("get")) {
                            constructors = constructors2;
                            paramAnnotationArrays2 = paramAnnotationArrays3;
                            paramNames = paramNames2;
                            methodName = methodName4;
                        } else {
                            String propertyName5 = decapitalize(methodName4.substring(3));
                            int p2 = Arrays.binarySearch(paramNames2, propertyName5);
                            if (p2 < 0) {
                                int i4 = 0;
                                while (true) {
                                    constructors = constructors2;
                                    methodName = methodName4;
                                    if (i4 >= paramNames2.length) {
                                        break;
                                    } else if (propertyName5.equalsIgnoreCase(paramNames2[i4])) {
                                        p2 = i4;
                                        break;
                                    } else {
                                        i4++;
                                        constructors2 = constructors;
                                        methodName4 = methodName;
                                    }
                                }
                            } else {
                                constructors = constructors2;
                                methodName = methodName4;
                            }
                            if (p2 >= 0) {
                                Annotation[] paramAnnotations = paramAnnotationArrays3[paramNameMapping2[p2]];
                                if (paramAnnotations != null) {
                                    int length2 = paramAnnotations.length;
                                    paramAnnotationArrays2 = paramAnnotationArrays3;
                                    int i5 = 0;
                                    while (true) {
                                        if (i5 >= length2) {
                                            paramNames = paramNames2;
                                            break;
                                        }
                                        Annotation paramAnnotation = paramAnnotations[i5];
                                        paramNames = paramNames2;
                                        if (paramAnnotation instanceof JSONField) {
                                            annotation4 = (JSONField) paramAnnotation;
                                            break;
                                        }
                                        i5++;
                                        length2 = length2;
                                        paramNames2 = paramNames;
                                    }
                                } else {
                                    paramAnnotationArrays2 = paramAnnotationArrays3;
                                    paramNames = paramNames2;
                                }
                                if (annotation4 == null && (field = ParserConfig.getFieldFromCache(propertyName5, map)) != null) {
                                    annotation = (JSONField) field.getAnnotation(JSONField.class);
                                    paramNameMapping = paramNameMapping2;
                                    if (annotation == null) {
                                        if (annotation.serialize()) {
                                            ordinal2 = annotation.ordinal();
                                            serialzeFeatures2 = SerializerFeature.of(annotation.serialzeFeatures());
                                            parserFeatures4 = Feature.of(annotation.parseFeatures());
                                            if (annotation.name().length() != 0) {
                                                String propertyName6 = annotation.name();
                                                if (aliasMap == null || (propertyName6 = aliasMap.get(propertyName6)) != null) {
                                                    i2 = i3;
                                                    i = length;
                                                    methods = methods2;
                                                    fieldInfoMap2 = fieldInfoMap4;
                                                    fieldInfoMap2.put(propertyName6, new FieldInfo(propertyName6, method2, null, clazz, null, ordinal2, serialzeFeatures2, parserFeatures4, annotation, null, null));
                                                    fieldInfoMap = fieldInfoMap2;
                                                    paramNameMapping2 = paramNameMapping;
                                                    constructors2 = constructors;
                                                    paramAnnotationArrays4 = paramAnnotationArrays2;
                                                    paramNames2 = paramNames;
                                                    i3 = i2 + 1;
                                                    fieldInfoMap4 = fieldInfoMap;
                                                    length = i;
                                                    methods2 = methods;
                                                    map = fieldCacheMap;
                                                }
                                            } else {
                                                method = method2;
                                                i2 = i3;
                                                i = length;
                                                methods = methods2;
                                                fieldInfoMap2 = fieldInfoMap4;
                                                methodName2 = methodName;
                                                annotation2 = annotation;
                                                if (annotation2.label().length() != 0) {
                                                    label = annotation2.label();
                                                }
                                            }
                                        }
                                        i2 = i3;
                                        i = length;
                                        methods = methods2;
                                        fieldInfoMap = fieldInfoMap4;
                                        paramNameMapping2 = paramNameMapping;
                                        constructors2 = constructors;
                                        paramAnnotationArrays4 = paramAnnotationArrays2;
                                        paramNames2 = paramNames;
                                        i3 = i2 + 1;
                                        fieldInfoMap4 = fieldInfoMap;
                                        length = i;
                                        methods2 = methods;
                                        map = fieldCacheMap;
                                    } else {
                                        method = method2;
                                        i2 = i3;
                                        i = length;
                                        methods = methods2;
                                        fieldInfoMap2 = fieldInfoMap4;
                                        methodName2 = methodName;
                                        annotation2 = annotation;
                                        ordinal2 = ordinal;
                                    }
                                    if (!methodName2.startsWith("get")) {
                                        if (methodName2.length() >= 4 && !methodName2.equals("getClass") && (!methodName2.equals("getDeclaringClass") || !clazz.isEnum())) {
                                            char c3 = methodName2.charAt(3);
                                            if (Character.isUpperCase(c3) || c3 > 512) {
                                                if (compatibleWithJavaBean) {
                                                    propertyName4 = decapitalize(methodName2.substring(3));
                                                } else {
                                                    propertyName4 = Character.toLowerCase(methodName2.charAt(3)) + methodName2.substring(4);
                                                }
                                                propertyName3 = getPropertyNameByCompatibleFieldName(map, methodName2, propertyName4, 3);
                                            } else if (c3 == '_') {
                                                propertyName3 = methodName2.substring(4);
                                            } else if (c3 == 'f') {
                                                propertyName3 = methodName2.substring(3);
                                            } else if (methodName2.length() >= 5 && Character.isUpperCase(methodName2.charAt(4))) {
                                                propertyName3 = decapitalize(methodName2.substring(3));
                                            }
                                            String propertyName7 = propertyName3;
                                            if (!isJSONTypeIgnore(clazz, propertyName7)) {
                                                Field field2 = ParserConfig.getFieldFromCache(propertyName7, map);
                                                if (field2 == null && propertyName7.length() > 1 && (ch = propertyName7.charAt(1)) >= 'A' && ch <= 'Z') {
                                                    field2 = ParserConfig.getFieldFromCache(decapitalize(methodName2.substring(3)), map);
                                                }
                                                JSONField fieldAnnotation3 = null;
                                                if (field2 == null || (fieldAnnotation3 = (JSONField) field2.getAnnotation(JSONField.class)) == null) {
                                                    fieldAnnotationAndNameExists = false;
                                                    parserFeatures3 = parserFeatures4;
                                                    serialzeFeatures = serialzeFeatures2;
                                                    fieldAnnotation2 = fieldAnnotation3;
                                                } else if (fieldAnnotation3.serialize()) {
                                                    int ordinal4 = fieldAnnotation3.ordinal();
                                                    int of = SerializerFeature.of(fieldAnnotation3.serialzeFeatures());
                                                    int of2 = Feature.of(fieldAnnotation3.parseFeatures());
                                                    if (fieldAnnotation3.name().length() != 0) {
                                                        fieldAnnotationAndNameExists2 = true;
                                                        propertyName7 = fieldAnnotation3.name();
                                                        if (aliasMap != null) {
                                                            propertyName7 = aliasMap.get(propertyName7);
                                                        }
                                                    }
                                                    if (fieldAnnotation3.label().length() != 0) {
                                                        fieldAnnotation2 = fieldAnnotation3;
                                                        label = fieldAnnotation3.label();
                                                    } else {
                                                        fieldAnnotation2 = fieldAnnotation3;
                                                    }
                                                    ordinal2 = ordinal4;
                                                    serialzeFeatures = of;
                                                    fieldAnnotationAndNameExists = fieldAnnotationAndNameExists2;
                                                    parserFeatures3 = of2;
                                                }
                                                if (aliasMap == null || (propertyName7 = aliasMap.get(propertyName7)) != null) {
                                                    if (propertyNamingStrategy != null && !fieldAnnotationAndNameExists.booleanValue()) {
                                                        propertyName7 = propertyNamingStrategy.translate(propertyName7);
                                                    }
                                                    methodName3 = methodName2;
                                                    annotation3 = annotation2;
                                                    FieldInfo fieldInfo = new FieldInfo(propertyName7, method, field2, clazz, null, ordinal2, serialzeFeatures, parserFeatures3, annotation2, fieldAnnotation2, label);
                                                    fieldInfoMap3 = fieldInfoMap2;
                                                    fieldInfoMap3.put(propertyName7, fieldInfo);
                                                    serialzeFeatures2 = serialzeFeatures;
                                                    parserFeatures4 = parserFeatures3;
                                                }
                                            }
                                        }
                                        fieldInfoMap = fieldInfoMap2;
                                        paramNameMapping2 = paramNameMapping;
                                        constructors2 = constructors;
                                        paramAnnotationArrays4 = paramAnnotationArrays2;
                                        paramNames2 = paramNames;
                                        i3 = i2 + 1;
                                        fieldInfoMap4 = fieldInfoMap;
                                        length = i;
                                        methods2 = methods;
                                        map = fieldCacheMap;
                                    } else {
                                        methodName3 = methodName2;
                                        annotation3 = annotation2;
                                        fieldInfoMap3 = fieldInfoMap2;
                                    }
                                    if (methodName3.startsWith("is")) {
                                        if (methodName3.length() >= 3 && (method.getReturnType() == Boolean.TYPE || method.getReturnType() == Boolean.class)) {
                                            char c2 = methodName3.charAt(2);
                                            if (Character.isUpperCase(c2)) {
                                                if (compatibleWithJavaBean) {
                                                    propertyName2 = decapitalize(methodName3.substring(2));
                                                } else {
                                                    propertyName2 = Character.toLowerCase(methodName3.charAt(2)) + methodName3.substring(3);
                                                }
                                                propertyName = getPropertyNameByCompatibleFieldName(map, methodName3, propertyName2, 2);
                                            } else if (c2 == '_') {
                                                propertyName = methodName3.substring(3);
                                            } else if (c2 == 'f') {
                                                propertyName = methodName3.substring(2);
                                            }
                                            if (!isJSONTypeIgnore(clazz, propertyName)) {
                                                Field field3 = ParserConfig.getFieldFromCache(propertyName, map);
                                                if (field3 == null) {
                                                    field3 = ParserConfig.getFieldFromCache(methodName3, map);
                                                }
                                                JSONField fieldAnnotation4 = null;
                                                if (field3 == null || (fieldAnnotation4 = (JSONField) field3.getAnnotation(JSONField.class)) == null) {
                                                    parserFeatures = parserFeatures4;
                                                    parserFeatures2 = serialzeFeatures2;
                                                    fieldAnnotation = fieldAnnotation4;
                                                } else if (fieldAnnotation4.serialize()) {
                                                    int ordinal5 = fieldAnnotation4.ordinal();
                                                    int serialzeFeatures3 = SerializerFeature.of(fieldAnnotation4.serialzeFeatures());
                                                    int parserFeatures5 = Feature.of(fieldAnnotation4.parseFeatures());
                                                    if (fieldAnnotation4.name().length() != 0) {
                                                        propertyName = fieldAnnotation4.name();
                                                        if (aliasMap != null) {
                                                            propertyName = aliasMap.get(propertyName);
                                                        }
                                                    }
                                                    if (fieldAnnotation4.label().length() != 0) {
                                                        fieldAnnotation = fieldAnnotation4;
                                                        ordinal2 = ordinal5;
                                                        parserFeatures2 = serialzeFeatures3;
                                                        parserFeatures = parserFeatures5;
                                                        label = fieldAnnotation4.label();
                                                    } else {
                                                        fieldAnnotation = fieldAnnotation4;
                                                        ordinal2 = ordinal5;
                                                        parserFeatures2 = serialzeFeatures3;
                                                        parserFeatures = parserFeatures5;
                                                    }
                                                }
                                                if (aliasMap == null || (propertyName = aliasMap.get(propertyName)) != null) {
                                                    if (propertyNamingStrategy != null) {
                                                        propertyName = propertyNamingStrategy.translate(propertyName);
                                                    }
                                                    if (!fieldInfoMap3.containsKey(propertyName)) {
                                                        fieldInfoMap = fieldInfoMap3;
                                                        fieldInfoMap.put(propertyName, new FieldInfo(propertyName, method, field3, clazz, null, ordinal2, parserFeatures2, parserFeatures, annotation3, fieldAnnotation, label));
                                                        paramNameMapping2 = paramNameMapping;
                                                        constructors2 = constructors;
                                                        paramAnnotationArrays4 = paramAnnotationArrays2;
                                                        paramNames2 = paramNames;
                                                        i3 = i2 + 1;
                                                        fieldInfoMap4 = fieldInfoMap;
                                                        length = i;
                                                        methods2 = methods;
                                                        map = fieldCacheMap;
                                                    }
                                                }
                                            }
                                        }
                                        fieldInfoMap = fieldInfoMap3;
                                        paramNameMapping2 = paramNameMapping;
                                        constructors2 = constructors;
                                        paramAnnotationArrays4 = paramAnnotationArrays2;
                                        paramNames2 = paramNames;
                                        i3 = i2 + 1;
                                        fieldInfoMap4 = fieldInfoMap;
                                        length = i;
                                        methods2 = methods;
                                        map = fieldCacheMap;
                                    }
                                    fieldInfoMap = fieldInfoMap3;
                                    paramNameMapping2 = paramNameMapping;
                                    constructors2 = constructors;
                                    paramAnnotationArrays4 = paramAnnotationArrays2;
                                    paramNames2 = paramNames;
                                    i3 = i2 + 1;
                                    fieldInfoMap4 = fieldInfoMap;
                                    length = i;
                                    methods2 = methods;
                                    map = fieldCacheMap;
                                }
                            } else {
                                paramAnnotationArrays2 = paramAnnotationArrays3;
                                paramNames = paramNames2;
                            }
                        }
                        paramNameMapping = paramNameMapping2;
                    }
                    annotation = annotation4;
                    if (annotation == null) {
                    }
                    if (!methodName2.startsWith("get")) {
                    }
                    if (methodName3.startsWith("is")) {
                    }
                    fieldInfoMap = fieldInfoMap3;
                    paramNameMapping2 = paramNameMapping;
                    constructors2 = constructors;
                    paramAnnotationArrays4 = paramAnnotationArrays2;
                    paramNames2 = paramNames;
                    i3 = i2 + 1;
                    fieldInfoMap4 = fieldInfoMap;
                    length = i;
                    methods2 = methods;
                    map = fieldCacheMap;
                }
            }
            i2 = i3;
            i = length;
            methods = methods2;
            fieldInfoMap = fieldInfoMap4;
            paramAnnotationArrays4 = paramAnnotationArrays;
            i3 = i2 + 1;
            fieldInfoMap4 = fieldInfoMap;
            length = i;
            methods2 = methods;
            map = fieldCacheMap;
        }
        computeFields(clazz, aliasMap, propertyNamingStrategy, fieldInfoMap4, clazz.getFields());
        return getFieldInfos(clazz, sorted, fieldInfoMap4);
    }

    private static List<FieldInfo> getFieldInfos(Class<?> clazz, boolean sorted, Map<String, FieldInfo> fieldInfoMap) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        String[] orders = null;
        JSONType annotation = (JSONType) getAnnotation(clazz, JSONType.class);
        if (annotation != null) {
            orders = annotation.orders();
        }
        if (orders == null || orders.length <= 0) {
            for (FieldInfo fieldInfo : fieldInfoMap.values()) {
                fieldInfoList.add(fieldInfo);
            }
            if (sorted) {
                Collections.sort(fieldInfoList);
            }
        } else {
            LinkedHashMap<String, FieldInfo> map = new LinkedHashMap<>(fieldInfoList.size());
            for (FieldInfo field : fieldInfoMap.values()) {
                map.put(field.name, field);
            }
            for (String item : orders) {
                FieldInfo field2 = map.get(item);
                if (field2 != null) {
                    fieldInfoList.add(field2);
                    map.remove(item);
                }
            }
            for (FieldInfo field3 : map.values()) {
                fieldInfoList.add(field3);
            }
        }
        return fieldInfoList;
    }

    private static void computeFields(Class<?> clazz, Map<String, String> aliasMap, PropertyNamingStrategy propertyNamingStrategy, Map<String, FieldInfo> fieldInfoMap, Field[] fields) {
        String propertyName;
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                JSONField fieldAnnotation = (JSONField) field.getAnnotation(JSONField.class);
                int ordinal = 0;
                int serialzeFeatures = 0;
                int parserFeatures = 0;
                String propertyName2 = field.getName();
                String label = null;
                if (fieldAnnotation != null) {
                    if (fieldAnnotation.serialize()) {
                        ordinal = fieldAnnotation.ordinal();
                        serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                        parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                        if (fieldAnnotation.name().length() != 0) {
                            propertyName2 = fieldAnnotation.name();
                        }
                        if (fieldAnnotation.label().length() != 0) {
                            label = fieldAnnotation.label();
                        }
                    }
                }
                if (aliasMap == null || (propertyName2 = aliasMap.get(propertyName2)) != null) {
                    if (propertyNamingStrategy != null) {
                        propertyName = propertyNamingStrategy.translate(propertyName2);
                    } else {
                        propertyName = propertyName2;
                    }
                    if (!fieldInfoMap.containsKey(propertyName)) {
                        fieldInfoMap.put(propertyName, new FieldInfo(propertyName, null, field, clazz, null, ordinal, serialzeFeatures, parserFeatures, null, fieldAnnotation, label));
                    }
                }
            }
        }
    }

    private static String getPropertyNameByCompatibleFieldName(Map<String, Field> fieldCacheMap, String methodName, String propertyName, int fromIdx) {
        if (!compatibleWithFieldName || fieldCacheMap.containsKey(propertyName)) {
            return propertyName;
        }
        String tempPropertyName = methodName.substring(fromIdx);
        return fieldCacheMap.containsKey(tempPropertyName) ? tempPropertyName : propertyName;
    }

    public static JSONField getSuperMethodAnnotation(Class<?> clazz, Method method) {
        JSONField annotation;
        JSONField annotation2;
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            Class<?>[] types = method.getParameterTypes();
            for (Class<?> interfaceClass : interfaces) {
                Method[] methods = interfaceClass.getMethods();
                for (Method interfaceMethod : methods) {
                    Class<?>[] interfaceTypes = interfaceMethod.getParameterTypes();
                    if (interfaceTypes.length == types.length && interfaceMethod.getName().equals(method.getName())) {
                        boolean match = true;
                        int i = 0;
                        while (true) {
                            if (i >= types.length) {
                                break;
                            } else if (!interfaceTypes[i].equals(types[i])) {
                                match = false;
                                break;
                            } else {
                                i++;
                            }
                        }
                        if (match && (annotation2 = (JSONField) interfaceMethod.getAnnotation(JSONField.class)) != null) {
                            return annotation2;
                        }
                    }
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && Modifier.isAbstract(superClass.getModifiers())) {
            Class<?>[] types2 = method.getParameterTypes();
            Method[] methods2 = superClass.getMethods();
            for (Method interfaceMethod2 : methods2) {
                Class<?>[] interfaceTypes2 = interfaceMethod2.getParameterTypes();
                if (interfaceTypes2.length == types2.length && interfaceMethod2.getName().equals(method.getName())) {
                    boolean match2 = true;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= types2.length) {
                            break;
                        } else if (!interfaceTypes2[i2].equals(types2[i2])) {
                            match2 = false;
                            break;
                        } else {
                            i2++;
                        }
                    }
                    if (match2 && (annotation = (JSONField) interfaceMethod2.getAnnotation(JSONField.class)) != null) {
                        return annotation;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isJSONTypeIgnore(Class<?> clazz, String propertyName) {
        String[] fields;
        JSONType jsonType = (JSONType) getAnnotation(clazz, JSONType.class);
        if (jsonType != null) {
            String[] fields2 = jsonType.includes();
            if (fields2.length > 0) {
                for (String str : fields2) {
                    if (propertyName.equals(str)) {
                        return false;
                    }
                }
                return true;
            }
            for (String str2 : jsonType.ignores()) {
                if (propertyName.equals(str2)) {
                    return true;
                }
            }
        }
        return (clazz.getSuperclass() == Object.class || clazz.getSuperclass() == null || !isJSONTypeIgnore(clazz.getSuperclass(), propertyName)) ? false : true;
    }

    public static boolean isGenericParamType(Type type) {
        if (type instanceof ParameterizedType) {
            return true;
        }
        if (!(type instanceof Class)) {
            return false;
        }
        Type superType = ((Class) type).getGenericSuperclass();
        if (superType == Object.class || !isGenericParamType(superType)) {
            return false;
        }
        return true;
    }

    public static Type getGenericParamType(Type type) {
        if (!(type instanceof ParameterizedType) && (type instanceof Class)) {
            return getGenericParamType(((Class) type).getGenericSuperclass());
        }
        return type;
    }

    public static Type unwrapOptional(Type type) {
        if (!optionalClassInited) {
            try {
                optionalClass = Class.forName("java.util.Optional");
            } catch (Exception e) {
            } catch (Throwable th) {
                optionalClassInited = true;
                throw th;
            }
            optionalClassInited = true;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() == optionalClass) {
                return parameterizedType.getActualTypeArguments()[0];
            }
        }
        return type;
    }

    public static Class<?> getClass(Type type) {
        if (type.getClass() == Class.class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof TypeVariable) {
            Type boundType = ((TypeVariable) type).getBounds()[0];
            if (boundType instanceof Class) {
                return (Class) boundType;
            }
            return getClass(boundType);
        } else if (!(type instanceof WildcardType)) {
            return Object.class;
        } else {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getClass(upperBounds[0]);
            }
            return Object.class;
        }
    }

    public static Field getField(Class<?> clazz, String fieldName, Field[] declaredFields) {
        char c0;
        char c1;
        for (Field field : declaredFields) {
            String itemName = field.getName();
            if (fieldName.equals(itemName)) {
                return field;
            }
            if (fieldName.length() > 2 && (c0 = fieldName.charAt(0)) >= 'a' && c0 <= 'z' && (c1 = fieldName.charAt(1)) >= 'A' && c1 <= 'Z' && fieldName.equalsIgnoreCase(itemName)) {
                return field;
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass == null || superClass == Object.class) {
            return null;
        }
        return getField(superClass, fieldName, superClass.getDeclaredFields());
    }

    public static int getSerializeFeatures(Class<?> clazz) {
        JSONType annotation = (JSONType) getAnnotation(clazz, JSONType.class);
        if (annotation == null) {
            return 0;
        }
        return SerializerFeature.of(annotation.serialzeFeatures());
    }

    public static int getParserFeatures(Class<?> clazz) {
        JSONType annotation = (JSONType) getAnnotation(clazz, JSONType.class);
        if (annotation == null) {
            return 0;
        }
        return Feature.of(annotation.parseFeatures());
    }

    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    static void setAccessible(AccessibleObject obj) {
        if (setAccessibleEnable && !obj.isAccessible()) {
            try {
                obj.setAccessible(true);
            } catch (AccessControlException e) {
                setAccessibleEnable = false;
            }
        }
    }

    public static Type getCollectionItemType(Type fieldType) {
        if (fieldType instanceof ParameterizedType) {
            return getCollectionItemType((ParameterizedType) fieldType);
        }
        if (fieldType instanceof Class) {
            return getCollectionItemType((Class<?>) ((Class) fieldType));
        }
        return Object.class;
    }

    private static Type getCollectionItemType(Class<?> clazz) {
        if (clazz.getName().startsWith("java.")) {
            return Object.class;
        }
        return getCollectionItemType(getCollectionSuperType(clazz));
    }

    private static Type getCollectionItemType(ParameterizedType parameterizedType) {
        Type rawType = parameterizedType.getRawType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (rawType == Collection.class) {
            return getWildcardTypeUpperBounds(actualTypeArguments[0]);
        }
        Class<?> rawClass = (Class) rawType;
        Map<TypeVariable, Type> actualTypeMap = createActualTypeMap(rawClass.getTypeParameters(), actualTypeArguments);
        Type superType = getCollectionSuperType(rawClass);
        if (!(superType instanceof ParameterizedType)) {
            return getCollectionItemType((Class<?>) ((Class) superType));
        }
        Class<?> superClass = getRawClass(superType);
        Type[] superClassTypeParameters = ((ParameterizedType) superType).getActualTypeArguments();
        if (superClassTypeParameters.length > 0) {
            return getCollectionItemType(makeParameterizedType(superClass, superClassTypeParameters, actualTypeMap));
        }
        return getCollectionItemType(superClass);
    }

    private static Type getCollectionSuperType(Class<?> clazz) {
        Type assignable = null;
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type type : genericInterfaces) {
            Class<?> rawClass = getRawClass(type);
            if (rawClass == Collection.class) {
                return type;
            }
            if (Collection.class.isAssignableFrom(rawClass)) {
                assignable = type;
            }
        }
        return assignable == null ? clazz.getGenericSuperclass() : assignable;
    }

    private static Map<TypeVariable, Type> createActualTypeMap(TypeVariable[] typeParameters, Type[] actualTypeArguments) {
        int length = typeParameters.length;
        Map<TypeVariable, Type> actualTypeMap = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            actualTypeMap.put(typeParameters[i], actualTypeArguments[i]);
        }
        return actualTypeMap;
    }

    private static ParameterizedType makeParameterizedType(Class<?> rawClass, Type[] typeParameters, Map<TypeVariable, Type> actualTypeMap) {
        int length = typeParameters.length;
        Type[] actualTypeArguments = new Type[length];
        for (int i = 0; i < length; i++) {
            actualTypeArguments[i] = getActualType(typeParameters[i], actualTypeMap);
        }
        return new ParameterizedTypeImpl(actualTypeArguments, null, rawClass);
    }

    private static Type getActualType(Type typeParameter, Map<TypeVariable, Type> actualTypeMap) {
        if (typeParameter instanceof TypeVariable) {
            return actualTypeMap.get(typeParameter);
        }
        if (typeParameter instanceof ParameterizedType) {
            return makeParameterizedType(getRawClass(typeParameter), ((ParameterizedType) typeParameter).getActualTypeArguments(), actualTypeMap);
        }
        if (typeParameter instanceof GenericArrayType) {
            return new GenericArrayTypeImpl(getActualType(((GenericArrayType) typeParameter).getGenericComponentType(), actualTypeMap));
        }
        return typeParameter;
    }

    private static Type getWildcardTypeUpperBounds(Type type) {
        if (!(type instanceof WildcardType)) {
            return type;
        }
        Type[] upperBounds = ((WildcardType) type).getUpperBounds();
        return upperBounds.length > 0 ? upperBounds[0] : Object.class;
    }

    public static Class<?> getCollectionItemClass(Type fieldType) {
        if (!(fieldType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type actualTypeArgument = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        if (actualTypeArgument instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) actualTypeArgument).getUpperBounds();
            if (upperBounds.length == 1) {
                actualTypeArgument = upperBounds[0];
            }
        }
        if (actualTypeArgument instanceof Class) {
            Class<?> itemClass = (Class) actualTypeArgument;
            if (Modifier.isPublic(itemClass.getModifiers())) {
                return itemClass;
            }
            throw new JSONException("can not create ASMParser");
        }
        throw new JSONException("can not create ASMParser");
    }

    public static Type checkPrimitiveArray(GenericArrayType genericArrayType) {
        Type genericComponentType = genericArrayType.getGenericComponentType();
        String prefix = "[";
        while (genericComponentType instanceof GenericArrayType) {
            genericComponentType = ((GenericArrayType) genericComponentType).getGenericComponentType();
            prefix = prefix + prefix;
        }
        if (!(genericComponentType instanceof Class)) {
            return genericArrayType;
        }
        Class<?> ck = (Class) genericComponentType;
        if (!ck.isPrimitive()) {
            return genericArrayType;
        }
        try {
            if (ck == Boolean.TYPE) {
                return Class.forName(prefix + "Z");
            } else if (ck == Character.TYPE) {
                return Class.forName(prefix + "C");
            } else if (ck == Byte.TYPE) {
                return Class.forName(prefix + "B");
            } else if (ck == Short.TYPE) {
                return Class.forName(prefix + "S");
            } else if (ck == Integer.TYPE) {
                return Class.forName(prefix + "I");
            } else if (ck == Long.TYPE) {
                return Class.forName(prefix + "J");
            } else if (ck == Float.TYPE) {
                return Class.forName(prefix + "F");
            } else if (ck != Double.TYPE) {
                return genericArrayType;
            } else {
                return Class.forName(prefix + "D");
            }
        } catch (ClassNotFoundException e) {
            return genericArrayType;
        }
    }

    /* JADX INFO: Multiple debug info for r1v21 java.util.EnumSet: [D('itemType' java.lang.reflect.Type), D('list' java.util.Collection)] */
    public static Collection createCollection(Type type) {
        Type itemType;
        Class<?> rawClass = getRawClass(type);
        if (rawClass == AbstractCollection.class || rawClass == Collection.class) {
            return new ArrayList();
        }
        if (rawClass.isAssignableFrom(HashSet.class)) {
            return new HashSet();
        }
        if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
            return new LinkedHashSet();
        }
        if (rawClass.isAssignableFrom(TreeSet.class)) {
            return new TreeSet();
        }
        if (rawClass.isAssignableFrom(ArrayList.class)) {
            return new ArrayList();
        }
        if (rawClass.isAssignableFrom(EnumSet.class)) {
            if (type instanceof ParameterizedType) {
                itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
            return EnumSet.noneOf((Class) itemType);
        } else if (rawClass.isAssignableFrom(Queue.class)) {
            return new LinkedList();
        } else {
            try {
                return (Collection) rawClass.newInstance();
            } catch (Exception e) {
                throw new JSONException("create instance error, class " + rawClass.getName());
            }
        }
    }

    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            return getRawClass(((ParameterizedType) type).getRawType());
        }
        throw new JSONException("TODO");
    }

    public static boolean isProxy(Class<?> clazz) {
        for (Class<?> item : clazz.getInterfaces()) {
            String interfaceName = item.getName();
            if (interfaceName.equals("net.sf.cglib.proxy.Factory") || interfaceName.equals("org.springframework.cglib.proxy.Factory") || interfaceName.equals("javassist.util.proxy.ProxyObject") || interfaceName.equals("org.apache.ibatis.javassist.util.proxy.ProxyObject") || interfaceName.equals("org.hibernate.proxy.HibernateProxy")) {
                return true;
            }
        }
        return false;
    }

    /* JADX DEBUG: Type inference failed for r1v6. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends java.lang.annotation.Annotation> */
    public static boolean isTransient(Method method) {
        if (method == null) {
            return false;
        }
        if (!transientClassInited) {
            try {
                transientClass = Class.forName("java.beans.Transient");
            } catch (Exception e) {
            } catch (Throwable th) {
                transientClassInited = true;
                throw th;
            }
            transientClassInited = true;
        }
        if (transientClass == null || method.getAnnotation(transientClass) == null) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Type inference failed for r1v7. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends java.lang.annotation.Annotation> */
    public static boolean isAnnotationPresentOneToMany(Method method) {
        if (method == null) {
            return false;
        }
        if (class_OneToMany == null && !class_OneToMany_error) {
            try {
                class_OneToMany = Class.forName("javax.persistence.OneToMany");
            } catch (Throwable th) {
                class_OneToMany_error = true;
            }
        }
        if (class_OneToMany == null || !method.isAnnotationPresent(class_OneToMany)) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Type inference failed for r1v9. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends java.lang.annotation.Annotation> */
    public static boolean isAnnotationPresentManyToMany(Method method) {
        if (method == null) {
            return false;
        }
        if (class_ManyToMany == null && !class_ManyToMany_error) {
            try {
                class_ManyToMany = Class.forName("javax.persistence.ManyToMany");
            } catch (Throwable th) {
                class_ManyToMany_error = true;
            }
        }
        if (class_ManyToMany == null) {
            return false;
        }
        if (method.isAnnotationPresent(class_OneToMany) || method.isAnnotationPresent(class_ManyToMany)) {
            return true;
        }
        return false;
    }

    public static boolean isHibernateInitialized(Object object) {
        if (object == null) {
            return false;
        }
        if (method_HibernateIsInitialized == null && !method_HibernateIsInitialized_error) {
            try {
                method_HibernateIsInitialized = Class.forName("org.hibernate.Hibernate").getMethod("isInitialized", Object.class);
            } catch (Throwable th) {
                method_HibernateIsInitialized_error = true;
            }
        }
        if (method_HibernateIsInitialized != null) {
            try {
                return ((Boolean) method_HibernateIsInitialized.invoke(null, object)).booleanValue();
            } catch (Throwable th2) {
            }
        }
        return true;
    }

    public static double parseDouble(String str) {
        int len = str.length();
        if (len > 10) {
            return Double.parseDouble(str);
        }
        boolean negative = false;
        long longValue = 0;
        int scale = 0;
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch == '-' && i == 0) {
                negative = true;
            } else if (ch == '.') {
                if (scale != 0) {
                    return Double.parseDouble(str);
                }
                scale = (len - i) - 1;
            } else if (ch < '0' || ch > '9') {
                return Double.parseDouble(str);
            } else {
                longValue = (10 * longValue) + ((long) (ch - '0'));
            }
        }
        if (negative) {
            longValue = -longValue;
        }
        switch (scale) {
            case 0:
                return (double) longValue;
            case 1:
                return ((double) longValue) / 10.0d;
            case 2:
                return ((double) longValue) / 100.0d;
            case 3:
                return ((double) longValue) / 1000.0d;
            case 4:
                return ((double) longValue) / 10000.0d;
            case 5:
                return ((double) longValue) / 100000.0d;
            case JSONToken.TRUE /* 6 */:
                return ((double) longValue) / 1000000.0d;
            case JSONToken.FALSE /* 7 */:
                return ((double) longValue) / 1.0E7d;
            case JSONToken.NULL /* 8 */:
                return ((double) longValue) / 1.0E8d;
            case 9:
                return ((double) longValue) / 1.0E9d;
            default:
                return Double.parseDouble(str);
        }
    }

    public static float parseFloat(String str) {
        int len = str.length();
        if (len >= 10) {
            return Float.parseFloat(str);
        }
        boolean negative = false;
        long longValue = 0;
        int scale = 0;
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch == '-' && i == 0) {
                negative = true;
            } else if (ch == '.') {
                if (scale != 0) {
                    return Float.parseFloat(str);
                }
                scale = (len - i) - 1;
            } else if (ch < '0' || ch > '9') {
                return Float.parseFloat(str);
            } else {
                longValue = (10 * longValue) + ((long) (ch - '0'));
            }
        }
        if (negative) {
            longValue = -longValue;
        }
        switch (scale) {
            case 0:
                return (float) longValue;
            case 1:
                return ((float) longValue) / 10.0f;
            case 2:
                return ((float) longValue) / 100.0f;
            case 3:
                return ((float) longValue) / 1000.0f;
            case 4:
                return ((float) longValue) / 10000.0f;
            case 5:
                return ((float) longValue) / 100000.0f;
            case JSONToken.TRUE /* 6 */:
                return ((float) longValue) / 1000000.0f;
            case JSONToken.FALSE /* 7 */:
                return ((float) longValue) / 1.0E7f;
            case JSONToken.NULL /* 8 */:
                return ((float) longValue) / 1.0E8f;
            case 9:
                return ((float) longValue) / 1.0E9f;
            default:
                return Float.parseFloat(str);
        }
    }

    public static long fnv1a_64_lower(String key) {
        long hashCode = -3750763034362895579L;
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            if (!(ch == '_' || ch == '-')) {
                if (ch >= 'A' && ch <= 'Z') {
                    ch = (char) (ch + ' ');
                }
                hashCode = (hashCode ^ ((long) ch)) * 1099511628211L;
            }
        }
        return hashCode;
    }

    public static long fnv1a_64(String key) {
        long hashCode = -3750763034362895579L;
        for (int i = 0; i < key.length(); i++) {
            hashCode = (hashCode ^ ((long) key.charAt(i))) * 1099511628211L;
        }
        return hashCode;
    }

    public static boolean isKotlin(Class clazz) {
        if (kotlin_metadata == null && !kotlin_metadata_error) {
            try {
                kotlin_metadata = Class.forName("kotlin.Metadata");
            } catch (Throwable th) {
                kotlin_metadata_error = true;
            }
        }
        if (kotlin_metadata == null || !clazz.isAnnotationPresent(kotlin_metadata)) {
            return false;
        }
        return true;
    }

    public static Constructor getKoltinConstructor(Constructor[] constructors) {
        return getKoltinConstructor(constructors, null);
    }

    public static Constructor getKoltinConstructor(Constructor[] constructors, String[] paramNames) {
        Constructor creatorConstructor = null;
        for (Constructor constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if ((paramNames == null || parameterTypes.length == paramNames.length) && ((parameterTypes.length <= 0 || !parameterTypes[parameterTypes.length - 1].getName().equals("kotlin.jvm.internal.DefaultConstructorMarker")) && (creatorConstructor == null || creatorConstructor.getParameterTypes().length < parameterTypes.length))) {
                creatorConstructor = constructor;
            }
        }
        return creatorConstructor;
    }

    public static String[] getKoltinConstructorParameters(Class clazz) {
        if (kotlin_kclass_constructor == null && !kotlin_class_klass_error) {
            try {
                kotlin_kclass_constructor = Class.forName("kotlin.reflect.jvm.internal.KClassImpl").getConstructor(Class.class);
            } catch (Throwable th) {
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_kclass_constructor == null) {
            return null;
        }
        if (kotlin_kclass_getConstructors == null && !kotlin_class_klass_error) {
            try {
                kotlin_kclass_getConstructors = Class.forName("kotlin.reflect.jvm.internal.KClassImpl").getMethod("getConstructors", new Class[0]);
            } catch (Throwable th2) {
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_kfunction_getParameters == null && !kotlin_class_klass_error) {
            try {
                kotlin_kfunction_getParameters = Class.forName("kotlin.reflect.KFunction").getMethod("getParameters", new Class[0]);
            } catch (Throwable th3) {
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_kparameter_getName == null && !kotlin_class_klass_error) {
            try {
                kotlin_kparameter_getName = Class.forName("kotlin.reflect.KParameter").getMethod("getName", new Class[0]);
            } catch (Throwable th4) {
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_error) {
            return null;
        }
        Object constructor = null;
        try {
            Iterator iterator = ((Iterable) kotlin_kclass_getConstructors.invoke(kotlin_kclass_constructor.newInstance(clazz), new Object[0])).iterator();
            while (iterator.hasNext()) {
                Object item = iterator.next();
                List parameters = (List) kotlin_kfunction_getParameters.invoke(item, new Object[0]);
                if (constructor == null || parameters.size() != 0) {
                    constructor = item;
                }
                iterator.hasNext();
            }
            List parameters2 = (List) kotlin_kfunction_getParameters.invoke(constructor, new Object[0]);
            String[] names = new String[parameters2.size()];
            for (int i = 0; i < parameters2.size(); i++) {
                names[i] = (String) kotlin_kparameter_getName.invoke(parameters2.get(i), new Object[0]);
            }
            return names;
        } catch (Throwable e) {
            e.printStackTrace();
            kotlin_error = true;
            return null;
        }
    }

    private static boolean isKotlinIgnore(Class clazz, String methodName) {
        if (kotlinIgnores == null && !kotlinIgnores_error) {
            try {
                Map<Class, String[]> map = new HashMap<>();
                map.put(Class.forName("kotlin.ranges.CharRange"), new String[]{"getEndInclusive", "isEmpty"});
                map.put(Class.forName("kotlin.ranges.IntRange"), new String[]{"getEndInclusive", "isEmpty"});
                map.put(Class.forName("kotlin.ranges.LongRange"), new String[]{"getEndInclusive", "isEmpty"});
                map.put(Class.forName("kotlin.ranges.ClosedFloatRange"), new String[]{"getEndInclusive", "isEmpty"});
                map.put(Class.forName("kotlin.ranges.ClosedDoubleRange"), new String[]{"getEndInclusive", "isEmpty"});
                kotlinIgnores = map;
            } catch (Throwable th) {
                kotlinIgnores_error = true;
            }
        }
        if (kotlinIgnores == null) {
            return false;
        }
        String[] ignores = kotlinIgnores.get(clazz);
        if (ignores == null || Arrays.binarySearch(ignores, methodName) < 0) {
            return false;
        }
        return true;
    }

    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass) {
        A a = (A) clazz.getAnnotation(annotationClass);
        if (a != null) {
            return a;
        }
        if (clazz.getAnnotations().length <= 0) {
            return null;
        }
        for (Annotation annotation : clazz.getAnnotations()) {
            A a2 = (A) annotation.annotationType().getAnnotation(annotationClass);
            if (a2 != null) {
                return a2;
            }
        }
        return null;
    }

    /* JADX DEBUG: Type inference failed for r1v7. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends java.lang.annotation.Annotation> */
    public static boolean isJacksonCreator(Method method) {
        if (method == null) {
            return false;
        }
        if (class_JacksonCreator == null && !class_JacksonCreator_error) {
            try {
                class_JacksonCreator = Class.forName("com.fasterxml.jackson.annotation.JsonCreator");
            } catch (Throwable th) {
                class_JacksonCreator_error = true;
            }
        }
        if (class_JacksonCreator == null || !method.isAnnotationPresent(class_JacksonCreator)) {
            return false;
        }
        return true;
    }
}
