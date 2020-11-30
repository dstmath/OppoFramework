package com.alibaba.fastjson.support.jaxrs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

@Consumes({"*/*"})
@Produces({"*/*"})
@Provider
public class FastJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
    public static final Class<?>[] DEFAULT_UNREADABLES = {InputStream.class, Reader.class};
    public static final Class<?>[] DEFAULT_UNWRITABLES = {InputStream.class, OutputStream.class, Writer.class, StreamingOutput.class, Response.class};
    @Deprecated
    protected Charset charset;
    private Class<?>[] clazzes;
    @Deprecated
    protected String dateFormat;
    private FastJsonConfig fastJsonConfig;
    @Deprecated
    protected SerializerFeature[] features;
    @Deprecated
    protected SerializeFilter[] filters;
    private boolean pretty;
    @Context
    protected Providers providers;

    public FastJsonConfig getFastJsonConfig() {
        return this.fastJsonConfig;
    }

    public void setFastJsonConfig(FastJsonConfig fastJsonConfig2) {
        this.fastJsonConfig = fastJsonConfig2;
    }

    public FastJsonProvider() {
        this.charset = Charset.forName("UTF-8");
        this.features = new SerializerFeature[0];
        this.filters = new SerializeFilter[0];
        this.fastJsonConfig = new FastJsonConfig();
        this.clazzes = null;
    }

    public FastJsonProvider(Class<?>[] clazzes2) {
        this.charset = Charset.forName("UTF-8");
        this.features = new SerializerFeature[0];
        this.filters = new SerializeFilter[0];
        this.fastJsonConfig = new FastJsonConfig();
        this.clazzes = null;
        this.clazzes = clazzes2;
    }

    public FastJsonProvider setPretty(boolean p) {
        this.pretty = p;
        return this;
    }

    @Deprecated
    public FastJsonProvider(String charset2) {
        this.charset = Charset.forName("UTF-8");
        this.features = new SerializerFeature[0];
        this.filters = new SerializeFilter[0];
        this.fastJsonConfig = new FastJsonConfig();
        this.clazzes = null;
        this.fastJsonConfig.setCharset(Charset.forName(charset2));
    }

    @Deprecated
    public Charset getCharset() {
        return this.fastJsonConfig.getCharset();
    }

    @Deprecated
    public void setCharset(Charset charset2) {
        this.fastJsonConfig.setCharset(charset2);
    }

    @Deprecated
    public String getDateFormat() {
        return this.fastJsonConfig.getDateFormat();
    }

    @Deprecated
    public void setDateFormat(String dateFormat2) {
        this.fastJsonConfig.setDateFormat(dateFormat2);
    }

    @Deprecated
    public SerializerFeature[] getFeatures() {
        return this.fastJsonConfig.getSerializerFeatures();
    }

    @Deprecated
    public void setFeatures(SerializerFeature... features2) {
        this.fastJsonConfig.setSerializerFeatures(features2);
    }

    @Deprecated
    public SerializeFilter[] getFilters() {
        return this.fastJsonConfig.getSerializeFilters();
    }

    @Deprecated
    public void setFilters(SerializeFilter... filters2) {
        this.fastJsonConfig.setSerializeFilters(filters2);
    }

    /* access modifiers changed from: protected */
    public boolean isAssignableFrom(Class<?> type, Class<?>[] classes) {
        if (type == null) {
            return false;
        }
        for (Class<?> cls : classes) {
            if (cls.isAssignableFrom(type)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isValidType(Class<?> type, Annotation[] classAnnotations) {
        if (type == null) {
            return false;
        }
        if (this.clazzes == null) {
            return true;
        }
        for (Class<?> cls : this.clazzes) {
            if (cls == type) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean hasMatchingMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        String subtype = mediaType.getSubtype();
        if ("json".equalsIgnoreCase(subtype) || subtype.endsWith("+json") || "javascript".equals(subtype) || "x-javascript".equals(subtype) || "x-json".equals(subtype) || "x-www-form-urlencoded".equalsIgnoreCase(subtype) || subtype.endsWith("x-www-form-urlencoded")) {
            return true;
        }
        return false;
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (hasMatchingMediaType(mediaType) && isAssignableFrom(type, DEFAULT_UNWRITABLES)) {
            return isValidType(type, annotations);
        }
        return false;
    }

    public long getSize(Object t, Class<?> cls, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public void writeTo(Object obj, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream entityStream) throws IOException, WebApplicationException {
        FastJsonConfig fastJsonConfig2 = locateConfigProvider(type, mediaType);
        SerializerFeature[] serializerFeatures = fastJsonConfig2.getSerializerFeatures();
        if (this.pretty) {
            if (serializerFeatures == null) {
                serializerFeatures = new SerializerFeature[]{SerializerFeature.PrettyFormat};
            } else {
                List<SerializerFeature> featureList = new ArrayList<>(Arrays.asList(serializerFeatures));
                featureList.add(SerializerFeature.PrettyFormat);
                serializerFeatures = (SerializerFeature[]) featureList.toArray(serializerFeatures);
            }
            fastJsonConfig2.setSerializerFeatures(serializerFeatures);
        }
        try {
            JSON.writeJSONString(entityStream, fastJsonConfig2.getCharset(), obj, fastJsonConfig2.getSerializeConfig(), fastJsonConfig2.getSerializeFilters(), fastJsonConfig2.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, fastJsonConfig2.getSerializerFeatures());
            entityStream.flush();
        } catch (JSONException ex) {
            throw new WebApplicationException(ex);
        }
    }

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (hasMatchingMediaType(mediaType) && isAssignableFrom(type, DEFAULT_UNREADABLES)) {
            return isValidType(type, annotations);
        }
        return false;
    }

    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            FastJsonConfig fastJsonConfig2 = locateConfigProvider(type, mediaType);
            return JSON.parseObject(entityStream, fastJsonConfig2.getCharset(), genericType, fastJsonConfig2.getParserConfig(), fastJsonConfig2.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, fastJsonConfig2.getFeatures());
        } catch (JSONException ex) {
            throw new WebApplicationException(ex);
        }
    }

    /* access modifiers changed from: protected */
    public FastJsonConfig locateConfigProvider(Class<?> type, MediaType mediaType) {
        if (this.providers != null) {
            ContextResolver<FastJsonConfig> resolver = this.providers.getContextResolver(FastJsonConfig.class, mediaType);
            if (resolver == null) {
                resolver = this.providers.getContextResolver(FastJsonConfig.class, (MediaType) null);
            }
            if (resolver != null) {
                return (FastJsonConfig) resolver.getContext(type);
            }
        }
        return this.fastJsonConfig;
    }
}
