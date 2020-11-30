package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.JSONStreamAware;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

public class MiscCodec implements ObjectDeserializer, ObjectSerializer {
    private static boolean FILE_RELATIVE_PATH_SUPPORT;
    public static final MiscCodec instance = new MiscCodec();
    private static Method method_paths_get;
    private static boolean method_paths_get_error = false;

    static {
        FILE_RELATIVE_PATH_SUPPORT = false;
        FILE_RELATIVE_PATH_SUPPORT = "true".equals(IOUtils.getStringProperty("fastjson.deserializer.fileRelativePathSupport"));
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        String pattern;
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        Class<?> objClass = object.getClass();
        if (objClass == SimpleDateFormat.class) {
            pattern = ((SimpleDateFormat) object).toPattern();
            if (out.isEnabled(SerializerFeature.WriteClassName) && object.getClass() != fieldType) {
                out.write(123);
                out.writeFieldName(JSON.DEFAULT_TYPE_KEY);
                serializer.write(object.getClass().getName());
                out.writeFieldValue(',', "val", pattern);
                out.write(125);
                return;
            }
        } else if (objClass == Class.class) {
            pattern = ((Class) object).getName();
        } else if (objClass == InetSocketAddress.class) {
            InetSocketAddress address = (InetSocketAddress) object;
            InetAddress inetAddress = address.getAddress();
            out.write(123);
            if (inetAddress != null) {
                out.writeFieldName("address");
                serializer.write(inetAddress);
                out.write(44);
            }
            out.writeFieldName("port");
            out.writeInt(address.getPort());
            out.write(125);
            return;
        } else if (object instanceof File) {
            pattern = ((File) object).getPath();
        } else if (object instanceof InetAddress) {
            pattern = ((InetAddress) object).getHostAddress();
        } else if (object instanceof TimeZone) {
            pattern = ((TimeZone) object).getID();
        } else if (object instanceof Currency) {
            pattern = ((Currency) object).getCurrencyCode();
        } else if (object instanceof JSONStreamAware) {
            ((JSONStreamAware) object).writeJSONString(out);
            return;
        } else if (object instanceof Iterator) {
            writeIterator(serializer, out, (Iterator) object);
            return;
        } else if (object instanceof Iterable) {
            writeIterator(serializer, out, ((Iterable) object).iterator());
            return;
        } else if (object instanceof Map.Entry) {
            Map.Entry entry = (Map.Entry) object;
            Object objKey = entry.getKey();
            Object objVal = entry.getValue();
            if (objKey instanceof String) {
                String key = (String) objKey;
                if (objVal instanceof String) {
                    out.writeFieldValueStringWithDoubleQuoteCheck('{', key, (String) objVal);
                } else {
                    out.write(123);
                    out.writeFieldName(key);
                    serializer.write(objVal);
                }
            } else {
                out.write(123);
                serializer.write(objKey);
                out.write(58);
                serializer.write(objVal);
            }
            out.write(125);
            return;
        } else if (object.getClass().getName().equals("net.sf.json.JSONNull")) {
            out.writeNull();
            return;
        } else if (object instanceof Node) {
            pattern = toString((Node) object);
        } else {
            throw new JSONException("not support class : " + objClass);
        }
        out.writeString(pattern);
    }

    private static String toString(Node node) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource domSource = new DOMSource(node);
            StringWriter out = new StringWriter();
            transformer.transform(domSource, new StreamResult(out));
            return out.toString();
        } catch (TransformerException e) {
            throw new JSONException("xml node to string error", e);
        }
    }

    /* access modifiers changed from: protected */
    public void writeIterator(JSONSerializer serializer, SerializeWriter out, Iterator<?> it) {
        int i = 0;
        out.write(91);
        while (it.hasNext()) {
            if (i != 0) {
                out.write(44);
            }
            serializer.write(it.next());
            i++;
        }
        out.write(93);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        Object objVal;
        String strVal;
        JSONLexer lexer = parser.lexer;
        int port = 0;
        if (clazz != InetSocketAddress.class) {
            if (parser.resolveStatus == 2) {
                parser.resolveStatus = 0;
                parser.accept(16);
                if (lexer.token() != 4) {
                    throw new JSONException("syntax error");
                } else if ("val".equals(lexer.stringVal())) {
                    lexer.nextToken();
                    parser.accept(17);
                    objVal = parser.parse();
                    parser.accept(13);
                } else {
                    throw new JSONException("syntax error");
                }
            } else {
                objVal = parser.parse();
            }
            if (objVal == null) {
                strVal = null;
            } else if (objVal instanceof String) {
                strVal = (String) objVal;
            } else if (objVal instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) objVal;
                if (clazz == Currency.class) {
                    String currency = jsonObject.getString("currency");
                    if (currency != null) {
                        return (T) Currency.getInstance(currency);
                    }
                    String symbol = jsonObject.getString("currencyCode");
                    if (symbol != null) {
                        return (T) Currency.getInstance(symbol);
                    }
                }
                return clazz == Map.Entry.class ? (T) jsonObject.entrySet().iterator().next() : (T) jsonObject.toJavaObject(clazz);
            } else {
                throw new JSONException("expect string");
            }
            if (strVal == null || strVal.length() == 0) {
                return null;
            }
            if (clazz == UUID.class) {
                return (T) UUID.fromString(strVal);
            }
            if (clazz == URI.class) {
                return (T) URI.create(strVal);
            }
            if (clazz == URL.class) {
                try {
                    return (T) new URL(strVal);
                } catch (MalformedURLException e) {
                    throw new JSONException("create url error", e);
                }
            } else if (clazz == Pattern.class) {
                return (T) Pattern.compile(strVal);
            } else {
                if (clazz == Locale.class) {
                    return (T) TypeUtils.toLocale(strVal);
                }
                if (clazz == SimpleDateFormat.class) {
                    T t = (T) new SimpleDateFormat(strVal, lexer.getLocale());
                    t.setTimeZone(lexer.getTimeZone());
                    return t;
                } else if (clazz == InetAddress.class || clazz == Inet4Address.class || clazz == Inet6Address.class) {
                    try {
                        return (T) InetAddress.getByName(strVal);
                    } catch (UnknownHostException e2) {
                        throw new JSONException("deserialize inet adress error", e2);
                    }
                } else if (clazz == File.class) {
                    if (strVal.indexOf("..") < 0 || FILE_RELATIVE_PATH_SUPPORT) {
                        return (T) new File(strVal);
                    }
                    throw new JSONException("file relative path not support.");
                } else if (clazz == TimeZone.class) {
                    return (T) TimeZone.getTimeZone(strVal);
                } else {
                    if (clazz instanceof ParameterizedType) {
                        clazz = ((ParameterizedType) clazz).getRawType();
                    }
                    if (clazz == Class.class) {
                        return (T) TypeUtils.loadClass(strVal, parser.getConfig().getDefaultClassLoader(), false);
                    }
                    if (clazz == Charset.class) {
                        return (T) Charset.forName(strVal);
                    }
                    if (clazz == Currency.class) {
                        return (T) Currency.getInstance(strVal);
                    }
                    if (clazz == JSONPath.class) {
                        return (T) new JSONPath(strVal);
                    }
                    if (clazz instanceof Class) {
                        String className = ((Class) clazz).getName();
                        if (className.equals("java.nio.file.Path")) {
                            try {
                                if (method_paths_get == null && !method_paths_get_error) {
                                    method_paths_get = TypeUtils.loadClass("java.nio.file.Paths").getMethod("get", String.class, String[].class);
                                }
                                if (method_paths_get != null) {
                                    return (T) method_paths_get.invoke(null, strVal, new String[0]);
                                }
                                throw new JSONException("Path deserialize erorr");
                            } catch (NoSuchMethodException e3) {
                                method_paths_get_error = true;
                            } catch (IllegalAccessException ex) {
                                throw new JSONException("Path deserialize erorr", ex);
                            } catch (InvocationTargetException ex2) {
                                throw new JSONException("Path deserialize erorr", ex2);
                            }
                        }
                        throw new JSONException("MiscCodec not support " + className);
                    }
                    throw new JSONException("MiscCodec not support " + clazz.toString());
                }
            }
        } else if (lexer.token() == 8) {
            lexer.nextToken();
            return null;
        } else {
            parser.accept(12);
            InetAddress address = null;
            while (true) {
                int port2 = port;
                String key = lexer.stringVal();
                lexer.nextToken(17);
                if (key.equals("address")) {
                    parser.accept(17);
                    address = (InetAddress) parser.parseObject((Class<Object>) InetAddress.class);
                } else if (key.equals("port")) {
                    parser.accept(17);
                    if (lexer.token() == 2) {
                        port2 = lexer.intValue();
                        lexer.nextToken();
                    } else {
                        throw new JSONException("port is not int");
                    }
                } else {
                    parser.accept(17);
                    parser.parse();
                }
                if (lexer.token() == 16) {
                    lexer.nextToken();
                    port = port2;
                } else {
                    parser.accept(13);
                    return (T) new InetSocketAddress(address, port2);
                }
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 4;
    }
}
