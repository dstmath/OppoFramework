package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.util.IOUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

public class JSONSerializer extends SerializeFilterable {
    protected final SerializeConfig config;
    protected SerialContext context;
    private DateFormat dateFormat;
    private String dateFormatPattern;
    private String indent;
    private int indentCount;
    protected Locale locale;
    public final SerializeWriter out;
    protected IdentityHashMap<Object, SerialContext> references;
    protected TimeZone timeZone;

    public JSONSerializer() {
        this(new SerializeWriter(), SerializeConfig.getGlobalInstance());
    }

    public JSONSerializer(SerializeWriter out2) {
        this(out2, SerializeConfig.getGlobalInstance());
    }

    public JSONSerializer(SerializeConfig config2) {
        this(new SerializeWriter(), config2);
    }

    public JSONSerializer(SerializeWriter out2, SerializeConfig config2) {
        this.indentCount = 0;
        this.indent = "\t";
        this.references = null;
        this.timeZone = JSON.defaultTimeZone;
        this.locale = JSON.defaultLocale;
        this.out = out2;
        this.config = config2;
    }

    public String getDateFormatPattern() {
        if (this.dateFormat instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) this.dateFormat).toPattern();
        }
        return this.dateFormatPattern;
    }

    public DateFormat getDateFormat() {
        if (this.dateFormat == null && this.dateFormatPattern != null) {
            this.dateFormat = new SimpleDateFormat(this.dateFormatPattern, this.locale);
            this.dateFormat.setTimeZone(this.timeZone);
        }
        return this.dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat2) {
        this.dateFormat = dateFormat2;
        if (this.dateFormatPattern != null) {
            this.dateFormatPattern = null;
        }
    }

    public void setDateFormat(String dateFormat2) {
        this.dateFormatPattern = dateFormat2;
        if (this.dateFormat != null) {
            this.dateFormat = null;
        }
    }

    public SerialContext getContext() {
        return this.context;
    }

    public void setContext(SerialContext context2) {
        this.context = context2;
    }

    public void setContext(SerialContext parent, Object object, Object fieldName, int features) {
        setContext(parent, object, fieldName, features, 0);
    }

    public void setContext(SerialContext parent, Object object, Object fieldName, int features, int fieldFeatures) {
        if (!this.out.disableCircularReferenceDetect) {
            this.context = new SerialContext(parent, object, fieldName, features, fieldFeatures);
            if (this.references == null) {
                this.references = new IdentityHashMap<>();
            }
            this.references.put(object, this.context);
        }
    }

    public void setContext(Object object, Object fieldName) {
        setContext(this.context, object, fieldName, 0);
    }

    public void popContext() {
        if (this.context != null) {
            this.context = this.context.parent;
        }
    }

    public final boolean isWriteClassName(Type fieldType, Object obj) {
        return this.out.isEnabled(SerializerFeature.WriteClassName) && !(fieldType == null && this.out.isEnabled(SerializerFeature.NotWriteRootClassName) && (this.context == null || this.context.parent == null));
    }

    public boolean containsReference(Object value) {
        SerialContext refContext;
        if (this.references == null || (refContext = this.references.get(value)) == null || value == Collections.emptyMap()) {
            return false;
        }
        Object fieldName = refContext.fieldName;
        if (fieldName == null || (fieldName instanceof Integer) || (fieldName instanceof String)) {
            return true;
        }
        return false;
    }

    public void writeReference(Object object) {
        SerialContext context2 = this.context;
        if (object == context2.object) {
            this.out.write("{\"$ref\":\"@\"}");
            return;
        }
        SerialContext parentContext = context2.parent;
        if (parentContext == null || object != parentContext.object) {
            SerialContext rootContext = context2;
            while (rootContext.parent != null) {
                rootContext = rootContext.parent;
            }
            if (object == rootContext.object) {
                this.out.write("{\"$ref\":\"$\"}");
                return;
            }
            this.out.write("{\"$ref\":\"");
            this.out.write(this.references.get(object).toString());
            this.out.write("\"}");
            return;
        }
        this.out.write("{\"$ref\":\"..\"}");
    }

    public boolean checkValue(SerializeFilterable filterable) {
        return (this.valueFilters != null && this.valueFilters.size() > 0) || (this.contextValueFilters != null && this.contextValueFilters.size() > 0) || ((filterable.valueFilters != null && filterable.valueFilters.size() > 0) || ((filterable.contextValueFilters != null && filterable.contextValueFilters.size() > 0) || this.out.writeNonStringValueAsString));
    }

    public boolean hasNameFilters(SerializeFilterable filterable) {
        return (this.nameFilters != null && this.nameFilters.size() > 0) || (filterable.nameFilters != null && filterable.nameFilters.size() > 0);
    }

    public boolean hasPropertyFilters(SerializeFilterable filterable) {
        return (this.propertyFilters != null && this.propertyFilters.size() > 0) || (filterable.propertyFilters != null && filterable.propertyFilters.size() > 0);
    }

    public int getIndentCount() {
        return this.indentCount;
    }

    public void incrementIndent() {
        this.indentCount++;
    }

    public void decrementIdent() {
        this.indentCount--;
    }

    public void println() {
        this.out.write(10);
        for (int i = 0; i < this.indentCount; i++) {
            this.out.write(this.indent);
        }
    }

    public SerializeWriter getWriter() {
        return this.out;
    }

    public String toString() {
        return this.out.toString();
    }

    public void config(SerializerFeature feature, boolean state) {
        this.out.config(feature, state);
    }

    public boolean isEnabled(SerializerFeature feature) {
        return this.out.isEnabled(feature);
    }

    public void writeNull() {
        this.out.writeNull();
    }

    public SerializeConfig getMapping() {
        return this.config;
    }

    public static void write(Writer out2, Object object) {
        SerializeWriter writer = new SerializeWriter();
        try {
            new JSONSerializer(writer).write(object);
            writer.writeTo(out2);
            writer.close();
        } catch (IOException ex) {
            throw new JSONException(ex.getMessage(), ex);
        } catch (Throwable th) {
            writer.close();
            throw th;
        }
    }

    public static void write(SerializeWriter out2, Object object) {
        new JSONSerializer(out2).write(object);
    }

    public final void write(Object object) {
        if (object == null) {
            this.out.writeNull();
            return;
        }
        try {
            getObjectWriter(object.getClass()).write(this, object, null, null, 0);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public final void writeAs(Object object, Class type) {
        if (object == null) {
            this.out.writeNull();
            return;
        }
        try {
            getObjectWriter(type).write(this, object, null, null, 0);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public final void writeWithFieldName(Object object, Object fieldName) {
        writeWithFieldName(object, fieldName, null, 0);
    }

    /* access modifiers changed from: protected */
    public final void writeKeyValue(char seperator, String key, Object value) {
        if (seperator != 0) {
            this.out.write(seperator);
        }
        this.out.writeFieldName(key);
        write(value);
    }

    public final void writeWithFieldName(Object object, Object fieldName, Type fieldType, int fieldFeatures) {
        if (object == null) {
            try {
                this.out.writeNull();
            } catch (IOException e) {
                throw new JSONException(e.getMessage(), e);
            }
        } else {
            getObjectWriter(object.getClass()).write(this, object, fieldName, fieldType, fieldFeatures);
        }
    }

    public final void writeWithFormat(Object object, String format) {
        GZIPOutputStream gzipOut;
        if (object instanceof Date) {
            if ("unixtime".equals(format)) {
                this.out.writeInt((int) (((Date) object).getTime() / 1000));
                return;
            }
            DateFormat dateFormat2 = getDateFormat();
            if (dateFormat2 == null) {
                try {
                    dateFormat2 = new SimpleDateFormat(format, this.locale);
                } catch (IllegalArgumentException e) {
                    dateFormat2 = new SimpleDateFormat(format.replaceAll("T", "'T'"), this.locale);
                }
                dateFormat2.setTimeZone(this.timeZone);
            }
            this.out.writeString(dateFormat2.format((Date) object));
        } else if (object instanceof byte[]) {
            byte[] bytes = (byte[]) object;
            if ("gzip".equals(format) || "gzip,base64".equals(format)) {
                try {
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    if (bytes.length < 512) {
                        gzipOut = new GZIPOutputStream(byteOut, bytes.length);
                    } else {
                        gzipOut = new GZIPOutputStream(byteOut);
                    }
                    gzipOut.write(bytes);
                    gzipOut.finish();
                    this.out.writeByteArray(byteOut.toByteArray());
                    IOUtils.close(gzipOut);
                } catch (IOException ex) {
                    throw new JSONException("write gzipBytes error", ex);
                } catch (Throwable th) {
                    IOUtils.close(null);
                    throw th;
                }
            } else if ("hex".equals(format)) {
                this.out.writeHex(bytes);
            } else {
                this.out.writeByteArray(bytes);
            }
        } else if (object instanceof Collection) {
            Collection collection = (Collection) object;
            Iterator iterator = collection.iterator();
            this.out.write(91);
            for (int i = 0; i < collection.size(); i++) {
                Object item = iterator.next();
                if (i != 0) {
                    this.out.write(44);
                }
                writeWithFormat(item, format);
            }
            this.out.write(93);
        } else {
            write(object);
        }
    }

    public final void write(String text) {
        StringCodec.instance.write(this, text);
    }

    public ObjectSerializer getObjectWriter(Class<?> clazz) {
        return this.config.getObjectWriter(clazz);
    }

    public void close() {
        this.out.close();
    }
}
