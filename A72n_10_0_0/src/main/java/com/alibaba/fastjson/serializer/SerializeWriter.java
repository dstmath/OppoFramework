package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.IdentityHashMap;
import com.alibaba.fastjson.util.RyuDouble;
import com.alibaba.fastjson.util.RyuFloat;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;

public final class SerializeWriter extends Writer {
    private static int BUFFER_THRESHOLD;
    private static final ThreadLocal<char[]> bufLocal = new ThreadLocal<>();
    private static final ThreadLocal<byte[]> bytesBufLocal = new ThreadLocal<>();
    static final int nonDirectFeatures = (((((((((0 | SerializerFeature.UseSingleQuotes.mask) | SerializerFeature.BrowserCompatible.mask) | SerializerFeature.PrettyFormat.mask) | SerializerFeature.WriteEnumUsingToString.mask) | SerializerFeature.WriteNonStringValueAsString.mask) | SerializerFeature.WriteSlashAsSpecial.mask) | SerializerFeature.IgnoreErrorGetter.mask) | SerializerFeature.WriteClassName.mask) | SerializerFeature.NotWriteDefaultValue.mask);
    protected boolean beanToArray;
    protected boolean browserSecure;
    protected char[] buf;
    protected int count;
    protected boolean disableCircularReferenceDetect;
    protected int features;
    protected char keySeperator;
    protected int maxBufSize;
    protected boolean notWriteDefaultValue;
    protected boolean quoteFieldNames;
    protected long sepcialBits;
    protected boolean sortField;
    protected boolean useSingleQuotes;
    protected boolean writeDirect;
    protected boolean writeEnumUsingName;
    protected boolean writeEnumUsingToString;
    protected boolean writeNonStringValueAsString;
    private final Writer writer;

    static {
        int serializer_buffer_threshold;
        BUFFER_THRESHOLD = 131072;
        try {
            String prop = IOUtils.getStringProperty("fastjson.serializer_buffer_threshold");
            if (prop != null && prop.length() > 0 && (serializer_buffer_threshold = Integer.parseInt(prop)) >= 64 && serializer_buffer_threshold <= 65536) {
                BUFFER_THRESHOLD = serializer_buffer_threshold * 1024;
            }
        } catch (Throwable th) {
        }
    }

    public SerializeWriter() {
        this((Writer) null);
    }

    public SerializeWriter(Writer writer2) {
        this(writer2, JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.EMPTY);
    }

    public SerializeWriter(SerializerFeature... features2) {
        this((Writer) null, features2);
    }

    public SerializeWriter(Writer writer2, SerializerFeature... features2) {
        this(writer2, 0, features2);
    }

    public SerializeWriter(Writer writer2, int defaultFeatures, SerializerFeature... features2) {
        this.maxBufSize = -1;
        this.writer = writer2;
        this.buf = bufLocal.get();
        if (this.buf != null) {
            bufLocal.set(null);
        } else {
            this.buf = new char[2048];
        }
        int featuresValue = defaultFeatures;
        for (SerializerFeature feature : features2) {
            featuresValue |= feature.getMask();
        }
        this.features = featuresValue;
        computeFeatures();
    }

    public int getMaxBufSize() {
        return this.maxBufSize;
    }

    public void setMaxBufSize(int maxBufSize2) {
        if (maxBufSize2 >= this.buf.length) {
            this.maxBufSize = maxBufSize2;
            return;
        }
        throw new JSONException("must > " + this.buf.length);
    }

    public int getBufferLength() {
        return this.buf.length;
    }

    public SerializeWriter(int initialSize) {
        this((Writer) null, initialSize);
    }

    public SerializeWriter(Writer writer2, int initialSize) {
        this.maxBufSize = -1;
        this.writer = writer2;
        if (initialSize > 0) {
            this.buf = new char[initialSize];
            computeFeatures();
            return;
        }
        throw new IllegalArgumentException("Negative initial size: " + initialSize);
    }

    public void config(SerializerFeature feature, boolean state) {
        if (state) {
            this.features |= feature.getMask();
            if (feature == SerializerFeature.WriteEnumUsingToString) {
                this.features &= ~SerializerFeature.WriteEnumUsingName.getMask();
            } else if (feature == SerializerFeature.WriteEnumUsingName) {
                this.features &= ~SerializerFeature.WriteEnumUsingToString.getMask();
            }
        } else {
            this.features &= ~feature.getMask();
        }
        computeFeatures();
    }

    /* access modifiers changed from: protected */
    public void computeFeatures() {
        boolean z = false;
        this.quoteFieldNames = (this.features & SerializerFeature.QuoteFieldNames.mask) != 0;
        this.useSingleQuotes = (this.features & SerializerFeature.UseSingleQuotes.mask) != 0;
        this.sortField = (this.features & SerializerFeature.SortField.mask) != 0;
        this.disableCircularReferenceDetect = (this.features & SerializerFeature.DisableCircularReferenceDetect.mask) != 0;
        this.beanToArray = (this.features & SerializerFeature.BeanToArray.mask) != 0;
        this.writeNonStringValueAsString = (this.features & SerializerFeature.WriteNonStringValueAsString.mask) != 0;
        this.notWriteDefaultValue = (this.features & SerializerFeature.NotWriteDefaultValue.mask) != 0;
        this.writeEnumUsingName = (this.features & SerializerFeature.WriteEnumUsingName.mask) != 0;
        this.writeEnumUsingToString = (this.features & SerializerFeature.WriteEnumUsingToString.mask) != 0;
        this.writeDirect = this.quoteFieldNames && (this.features & nonDirectFeatures) == 0 && (this.beanToArray || this.writeEnumUsingName);
        this.keySeperator = this.useSingleQuotes ? '\'' : '\"';
        if ((this.features & SerializerFeature.BrowserSecure.mask) != 0) {
            z = true;
        }
        this.browserSecure = z;
        this.sepcialBits = this.browserSecure ? 5764610843043954687L : (this.features & SerializerFeature.WriteSlashAsSpecial.mask) != 0 ? 140758963191807L : 21474836479L;
    }

    public boolean isSortField() {
        return this.sortField;
    }

    public boolean isNotWriteDefaultValue() {
        return this.notWriteDefaultValue;
    }

    public boolean isEnabled(SerializerFeature feature) {
        return (this.features & feature.mask) != 0;
    }

    public boolean isEnabled(int feature) {
        return (this.features & feature) != 0;
    }

    @Override // java.io.Writer
    public void write(int c) {
        int newcount = this.count + 1;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(newcount);
            } else {
                flush();
                newcount = 1;
            }
        }
        this.buf[this.count] = (char) c;
        this.count = newcount;
    }

    @Override // java.io.Writer
    public void write(char[] c, int off, int len) {
        if (off < 0 || off > c.length || len < 0 || off + len > c.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len != 0) {
            int newcount = this.count + len;
            if (newcount > this.buf.length) {
                if (this.writer == null) {
                    expandCapacity(newcount);
                } else {
                    do {
                        int rest = this.buf.length - this.count;
                        System.arraycopy(c, off, this.buf, this.count, rest);
                        this.count = this.buf.length;
                        flush();
                        len -= rest;
                        off += rest;
                    } while (len > this.buf.length);
                    newcount = len;
                }
            }
            System.arraycopy(c, off, this.buf, this.count, len);
            this.count = newcount;
        }
    }

    public void expandCapacity(int minimumCapacity) {
        char[] charsLocal;
        if (this.maxBufSize == -1 || minimumCapacity < this.maxBufSize) {
            int newCapacity = this.buf.length + (this.buf.length >> 1) + 1;
            if (newCapacity < minimumCapacity) {
                newCapacity = minimumCapacity;
            }
            char[] newValue = new char[newCapacity];
            System.arraycopy(this.buf, 0, newValue, 0, this.count);
            if (this.buf.length < BUFFER_THRESHOLD && ((charsLocal = bufLocal.get()) == null || charsLocal.length < this.buf.length)) {
                bufLocal.set(this.buf);
            }
            this.buf = newValue;
            return;
        }
        throw new JSONException("serialize exceeded MAX_OUTPUT_LENGTH=" + this.maxBufSize + ", minimumCapacity=" + minimumCapacity);
    }

    @Override // java.lang.Appendable, java.io.Writer, java.io.Writer
    public SerializeWriter append(CharSequence csq) {
        String s = csq == null ? "null" : csq.toString();
        write(s, 0, s.length());
        return this;
    }

    @Override // java.lang.Appendable, java.io.Writer, java.io.Writer
    public SerializeWriter append(CharSequence csq, int start, int end) {
        String s = (csq == null ? "null" : csq).subSequence(start, end).toString();
        write(s, 0, s.length());
        return this;
    }

    @Override // java.lang.Appendable, java.io.Writer, java.io.Writer
    public SerializeWriter append(char c) {
        write(c);
        return this;
    }

    @Override // java.io.Writer
    public void write(String str, int off, int len) {
        int newcount = this.count + len;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(newcount);
            } else {
                do {
                    int rest = this.buf.length - this.count;
                    str.getChars(off, off + rest, this.buf, this.count);
                    this.count = this.buf.length;
                    flush();
                    len -= rest;
                    off += rest;
                } while (len > this.buf.length);
                newcount = len;
            }
        }
        str.getChars(off, off + len, this.buf, this.count);
        this.count = newcount;
    }

    public void writeTo(Writer out) throws IOException {
        if (this.writer == null) {
            out.write(this.buf, 0, this.count);
            return;
        }
        throw new UnsupportedOperationException("writer not null");
    }

    public void writeTo(OutputStream out, String charsetName) throws IOException {
        writeTo(out, Charset.forName(charsetName));
    }

    public void writeTo(OutputStream out, Charset charset) throws IOException {
        writeToEx(out, charset);
    }

    public int writeToEx(OutputStream out, Charset charset) throws IOException {
        if (this.writer != null) {
            throw new UnsupportedOperationException("writer not null");
        } else if (charset == IOUtils.UTF8) {
            return encodeToUTF8(out);
        } else {
            byte[] bytes = new String(this.buf, 0, this.count).getBytes(charset);
            out.write(bytes);
            return bytes.length;
        }
    }

    public char[] toCharArray() {
        if (this.writer == null) {
            char[] newValue = new char[this.count];
            System.arraycopy(this.buf, 0, newValue, 0, this.count);
            return newValue;
        }
        throw new UnsupportedOperationException("writer not null");
    }

    public char[] toCharArrayForSpringWebSocket() {
        if (this.writer == null) {
            char[] newValue = new char[(this.count - 2)];
            System.arraycopy(this.buf, 1, newValue, 0, this.count - 2);
            return newValue;
        }
        throw new UnsupportedOperationException("writer not null");
    }

    public byte[] toBytes(String charsetName) {
        Charset charset;
        if (charsetName == null || "UTF-8".equals(charsetName)) {
            charset = IOUtils.UTF8;
        } else {
            charset = Charset.forName(charsetName);
        }
        return toBytes(charset);
    }

    public byte[] toBytes(Charset charset) {
        if (this.writer != null) {
            throw new UnsupportedOperationException("writer not null");
        } else if (charset == IOUtils.UTF8) {
            return encodeToUTF8Bytes();
        } else {
            return new String(this.buf, 0, this.count).getBytes(charset);
        }
    }

    private int encodeToUTF8(OutputStream out) throws IOException {
        int bytesLength = (int) (((double) this.count) * 3.0d);
        byte[] bytes = bytesBufLocal.get();
        if (bytes == null) {
            bytes = new byte[IdentityHashMap.DEFAULT_SIZE];
            bytesBufLocal.set(bytes);
        }
        if (bytes.length < bytesLength) {
            bytes = new byte[bytesLength];
        }
        int position = IOUtils.encodeUTF8(this.buf, 0, this.count, bytes);
        out.write(bytes, 0, position);
        return position;
    }

    private byte[] encodeToUTF8Bytes() {
        int bytesLength = (int) (((double) this.count) * 3.0d);
        byte[] bytes = bytesBufLocal.get();
        if (bytes == null) {
            bytes = new byte[IdentityHashMap.DEFAULT_SIZE];
            bytesBufLocal.set(bytes);
        }
        if (bytes.length < bytesLength) {
            bytes = new byte[bytesLength];
        }
        int position = IOUtils.encodeUTF8(this.buf, 0, this.count, bytes);
        byte[] copy = new byte[position];
        System.arraycopy(bytes, 0, copy, 0, position);
        return copy;
    }

    public int size() {
        return this.count;
    }

    public String toString() {
        return new String(this.buf, 0, this.count);
    }

    @Override // java.io.Closeable, java.io.Writer, java.lang.AutoCloseable
    public void close() {
        if (this.writer != null && this.count > 0) {
            flush();
        }
        if (this.buf.length <= BUFFER_THRESHOLD) {
            bufLocal.set(this.buf);
        }
        this.buf = null;
    }

    @Override // java.io.Writer
    public void write(String text) {
        if (text == null) {
            writeNull();
        } else {
            write(text, 0, text.length());
        }
    }

    public void writeInt(int i) {
        if (i == Integer.MIN_VALUE) {
            write("-2147483648");
            return;
        }
        int size = i < 0 ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
        int newcount = this.count + size;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(newcount);
            } else {
                char[] chars = new char[size];
                IOUtils.getChars(i, size, chars);
                write(chars, 0, chars.length);
                return;
            }
        }
        IOUtils.getChars(i, newcount, this.buf);
        this.count = newcount;
    }

    /* JADX INFO: Multiple debug info for r4v0 char[]: [D('CA' char[]), D('emptyString' java.lang.String)] */
    public void writeByteArray(byte[] bytes) {
        if (isEnabled(SerializerFeature.WriteClassName.mask)) {
            writeHex(bytes);
            return;
        }
        int bytesLen = bytes.length;
        char quote = this.useSingleQuotes ? '\'' : '\"';
        if (bytesLen == 0) {
            write(this.useSingleQuotes ? "''" : "\"\"");
            return;
        }
        char[] CA = IOUtils.CA;
        int eLen = (bytesLen / 3) * 3;
        int offset = this.count;
        int newcount = this.count + ((((bytesLen - 1) / 3) + 1) << 2) + 2;
        int i = 0;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                write(quote);
                int s = 0;
                while (s < eLen) {
                    int s2 = s + 1;
                    int s3 = s2 + 1;
                    int i2 = ((bytes[s] & 255) << 16) | ((bytes[s2] & 255) << 8) | (bytes[s3] & 255);
                    write(CA[(i2 >>> 18) & 63]);
                    write(CA[(i2 >>> 12) & 63]);
                    write(CA[(i2 >>> 6) & 63]);
                    write(CA[i2 & 63]);
                    s = s3 + 1;
                }
                int left = bytesLen - eLen;
                if (left > 0) {
                    int i3 = (bytes[eLen] & 255) << 10;
                    if (left == 2) {
                        i = (bytes[bytesLen - 1] & 255) << 2;
                    }
                    int i4 = i | i3;
                    write(CA[i4 >> 12]);
                    write(CA[(i4 >>> 6) & 63]);
                    write(left == 2 ? CA[i4 & 63] : '=');
                    write(61);
                }
                write(quote);
                return;
            }
            expandCapacity(newcount);
        }
        this.count = newcount;
        this.buf[offset] = quote;
        int s4 = 0;
        int s5 = offset + 1;
        while (s4 < eLen) {
            int s6 = s4 + 1;
            int s7 = s6 + 1;
            int i5 = ((bytes[s4] & 255) << 16) | ((bytes[s6] & 255) << 8);
            int s8 = s7 + 1;
            int i6 = i5 | (bytes[s7] & 255);
            int d = s5 + 1;
            this.buf[s5] = CA[(i6 >>> 18) & 63];
            int d2 = d + 1;
            this.buf[d] = CA[(i6 >>> 12) & 63];
            int d3 = d2 + 1;
            this.buf[d2] = CA[(i6 >>> 6) & 63];
            this.buf[d3] = CA[i6 & 63];
            s4 = s8;
            s5 = d3 + 1;
        }
        int left2 = bytesLen - eLen;
        if (left2 > 0) {
            int i7 = (bytes[eLen] & 255) << 10;
            if (left2 == 2) {
                i = (bytes[bytesLen - 1] & 255) << 2;
            }
            int i8 = i7 | i;
            this.buf[newcount - 5] = CA[i8 >> 12];
            this.buf[newcount - 4] = CA[(i8 >>> 6) & 63];
            this.buf[newcount - 3] = left2 == 2 ? CA[i8 & 63] : '=';
            this.buf[newcount - 2] = '=';
        }
        this.buf[newcount - 1] = quote;
    }

    public void writeHex(byte[] bytes) {
        int newcount = this.count + (bytes.length * 2) + 3;
        int i = 0;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                char[] chars = new char[(bytes.length + 3)];
                int pos = 0 + 1;
                chars[0] = 'x';
                int pos2 = pos + 1;
                chars[pos] = '\'';
                while (i < bytes.length) {
                    int a = bytes[i] & 255;
                    int b0 = a >> 4;
                    int b1 = a & 15;
                    int pos3 = pos2 + 1;
                    chars[pos2] = (char) ((b0 < 10 ? 48 : 55) + b0);
                    pos2 = pos3 + 1;
                    chars[pos3] = (char) ((b1 < 10 ? 48 : 55) + b1);
                    i++;
                }
                int i2 = pos2 + 1;
                chars[pos2] = '\'';
                try {
                    this.writer.write(chars);
                    return;
                } catch (IOException ex) {
                    throw new JSONException("writeBytes error.", ex);
                }
            } else {
                expandCapacity(newcount);
            }
        }
        char[] cArr = this.buf;
        int i3 = this.count;
        this.count = i3 + 1;
        cArr[i3] = 'x';
        char[] cArr2 = this.buf;
        int i4 = this.count;
        this.count = i4 + 1;
        cArr2[i4] = '\'';
        while (i < bytes.length) {
            int a2 = bytes[i] & 255;
            int b02 = a2 >> 4;
            int b12 = a2 & 15;
            char[] cArr3 = this.buf;
            int i5 = this.count;
            this.count = i5 + 1;
            cArr3[i5] = (char) ((b02 < 10 ? 48 : 55) + b02);
            char[] cArr4 = this.buf;
            int i6 = this.count;
            this.count = i6 + 1;
            cArr4[i6] = (char) ((b12 < 10 ? 48 : 55) + b12);
            i++;
        }
        char[] cArr5 = this.buf;
        int i7 = this.count;
        this.count = i7 + 1;
        cArr5[i7] = '\'';
    }

    public void writeFloat(float value, boolean checkWriteClassName) {
        if (value != value || value == Float.POSITIVE_INFINITY || value == Float.NEGATIVE_INFINITY) {
            writeNull();
            return;
        }
        int newcount = this.count + 15;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(newcount);
            } else {
                String str = RyuFloat.toString(value);
                write(str, 0, str.length());
                if (checkWriteClassName && isEnabled(SerializerFeature.WriteClassName)) {
                    write(70);
                    return;
                }
                return;
            }
        }
        this.count += RyuFloat.toString(value, this.buf, this.count);
        if (checkWriteClassName && isEnabled(SerializerFeature.WriteClassName)) {
            write(70);
        }
    }

    public void writeDouble(double value, boolean checkWriteClassName) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            writeNull();
            return;
        }
        int newcount = this.count + 24;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(newcount);
            } else {
                String str = RyuDouble.toString(value);
                write(str, 0, str.length());
                if (checkWriteClassName && isEnabled(SerializerFeature.WriteClassName)) {
                    write(68);
                    return;
                }
                return;
            }
        }
        this.count += RyuDouble.toString(value, this.buf, this.count);
        if (checkWriteClassName && isEnabled(SerializerFeature.WriteClassName)) {
            write(68);
        }
    }

    public void writeEnum(Enum<?> value) {
        if (value == null) {
            writeNull();
            return;
        }
        String strVal = null;
        if (this.writeEnumUsingName && !this.writeEnumUsingToString) {
            strVal = value.name();
        } else if (this.writeEnumUsingToString) {
            strVal = value.toString();
        }
        if (strVal != null) {
            char quote = isEnabled(SerializerFeature.UseSingleQuotes) ? '\'' : '\"';
            write(quote);
            write(strVal);
            write(quote);
            return;
        }
        writeInt(value.ordinal());
    }

    public void writeLongAndChar(long i, char c) throws IOException {
        writeLong(i);
        write(c);
    }

    public void writeLong(long i) {
        boolean needQuotationMark = isEnabled(SerializerFeature.BrowserCompatible) && !isEnabled(SerializerFeature.WriteClassName) && (i > 9007199254740991L || i < -9007199254740991L);
        if (i != Long.MIN_VALUE) {
            int size = i < 0 ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
            int newcount = this.count + size;
            if (needQuotationMark) {
                newcount += 2;
            }
            if (newcount > this.buf.length) {
                if (this.writer == null) {
                    expandCapacity(newcount);
                } else {
                    char[] chars = new char[size];
                    IOUtils.getChars(i, size, chars);
                    if (needQuotationMark) {
                        write(34);
                        write(chars, 0, chars.length);
                        write(34);
                        return;
                    }
                    write(chars, 0, chars.length);
                    return;
                }
            }
            if (needQuotationMark) {
                this.buf[this.count] = '\"';
                IOUtils.getChars(i, newcount - 1, this.buf);
                this.buf[newcount - 1] = '\"';
            } else {
                IOUtils.getChars(i, newcount, this.buf);
            }
            this.count = newcount;
        } else if (needQuotationMark) {
            write("\"-9223372036854775808\"");
        } else {
            write("-9223372036854775808");
        }
    }

    public void writeNull() {
        write("null");
    }

    public void writeNull(SerializerFeature feature) {
        writeNull(0, feature.mask);
    }

    public void writeNull(int beanFeatures, int feature) {
        if ((beanFeatures & feature) == 0 && (this.features & feature) == 0) {
            writeNull();
        } else if (feature == SerializerFeature.WriteNullListAsEmpty.mask) {
            write("[]");
        } else if (feature == SerializerFeature.WriteNullStringAsEmpty.mask) {
            writeString("");
        } else if (feature == SerializerFeature.WriteNullBooleanAsFalse.mask) {
            write("false");
        } else if (feature == SerializerFeature.WriteNullNumberAsZero.mask) {
            write(48);
        } else {
            writeNull();
        }
    }

    /* JADX INFO: Multiple debug info for r3v5 int: [D('len' int), D('srcPos' int)] */
    /* JADX INFO: Multiple debug info for r3v6 int: [D('len' int), D('srcPos' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0358, code lost:
        if ((r27.sepcialBits & (1 << r13)) == 0) goto L_0x035d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x0546, code lost:
        if (r13 != '>') goto L_0x059c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x0365  */
    public void writeStringWithDoubleQuote(String text, char seperator) {
        int bufIndex;
        int specialCount;
        boolean special;
        if (text == null) {
            writeNull();
            if (seperator != 0) {
                write(seperator);
                return;
            }
            return;
        }
        int len = text.length();
        int newcount = this.count + len + 2;
        if (seperator != 0) {
            newcount++;
        }
        char c = ')';
        char c2 = '/';
        char c3 = '\"';
        char c4 = '\b';
        char c5 = '\f';
        char c6 = '\\';
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                write(34);
                int i = 0;
                while (i < text.length()) {
                    char ch = text.charAt(i);
                    if (!isEnabled(SerializerFeature.BrowserSecure) || !(ch == '(' || ch == c || ch == '<' || ch == '>')) {
                        if (isEnabled(SerializerFeature.BrowserCompatible)) {
                            if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\"' || ch == '/' || ch == '\\') {
                                write(92);
                                write(IOUtils.replaceChars[ch]);
                            } else if (ch < ' ') {
                                write(92);
                                write(117);
                                write(48);
                                write(48);
                                write(IOUtils.ASCII_CHARS[ch * 2]);
                                write(IOUtils.ASCII_CHARS[(ch * 2) + 1]);
                            } else if (ch >= 127) {
                                write(92);
                                write(117);
                                write(IOUtils.DIGITS[(ch >>> '\f') & 15]);
                                write(IOUtils.DIGITS[(ch >>> '\b') & 15]);
                                write(IOUtils.DIGITS[(ch >>> 4) & 15]);
                                write(IOUtils.DIGITS[ch & 15]);
                            }
                        } else if ((ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] != 0) || (ch == '/' && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                            write(92);
                            if (IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
                                write(117);
                                write(IOUtils.DIGITS[(ch >>> '\f') & 15]);
                                write(IOUtils.DIGITS[(ch >>> '\b') & 15]);
                                write(IOUtils.DIGITS[(ch >>> 4) & 15]);
                                write(IOUtils.DIGITS[ch & 15]);
                            } else {
                                write(IOUtils.replaceChars[ch]);
                            }
                        }
                        write(ch);
                    } else {
                        write(92);
                        write(117);
                        write(IOUtils.DIGITS[(ch >>> '\f') & 15]);
                        write(IOUtils.DIGITS[(ch >>> '\b') & 15]);
                        write(IOUtils.DIGITS[(ch >>> 4) & 15]);
                        write(IOUtils.DIGITS[ch & 15]);
                    }
                    i++;
                    c = ')';
                }
                write(34);
                if (seperator != 0) {
                    write(seperator);
                    return;
                }
                return;
            }
            expandCapacity(newcount);
        }
        int start = this.count + 1;
        int end = start + len;
        this.buf[this.count] = '\"';
        text.getChars(0, len, this.buf, start);
        this.count = newcount;
        if (isEnabled(SerializerFeature.BrowserCompatible)) {
            int lastSpecialIndex = -1;
            int newcount2 = newcount;
            for (int i2 = start; i2 < end; i2++) {
                char ch2 = this.buf[i2];
                if (ch2 == '\"' || ch2 == '/' || ch2 == '\\') {
                    lastSpecialIndex = i2;
                    newcount2++;
                } else if (ch2 == '\b' || ch2 == '\f' || ch2 == '\n' || ch2 == '\r' || ch2 == '\t') {
                    lastSpecialIndex = i2;
                    newcount2++;
                } else if (ch2 < ' ') {
                    lastSpecialIndex = i2;
                    newcount2 += 5;
                } else if (ch2 >= 127) {
                    lastSpecialIndex = i2;
                    newcount2 += 5;
                }
            }
            if (newcount2 > this.buf.length) {
                expandCapacity(newcount2);
            }
            this.count = newcount2;
            int i3 = lastSpecialIndex;
            while (i3 >= start) {
                char ch3 = this.buf[i3];
                if (ch3 == c4 || ch3 == c5 || ch3 == '\n' || ch3 == '\r' || ch3 == '\t') {
                    System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 2, (end - i3) - 1);
                    this.buf[i3] = '\\';
                    this.buf[i3 + 1] = IOUtils.replaceChars[ch3];
                    end++;
                } else if (ch3 == c3 || ch3 == c2 || ch3 == '\\') {
                    System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 2, (end - i3) - 1);
                    this.buf[i3] = '\\';
                    this.buf[i3 + 1] = ch3;
                    end++;
                } else if (ch3 < ' ') {
                    System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 6, (end - i3) - 1);
                    this.buf[i3] = '\\';
                    this.buf[i3 + 1] = 'u';
                    this.buf[i3 + 2] = '0';
                    this.buf[i3 + 3] = '0';
                    this.buf[i3 + 4] = IOUtils.ASCII_CHARS[ch3 * 2];
                    this.buf[i3 + 5] = IOUtils.ASCII_CHARS[(ch3 * 2) + 1];
                    end += 5;
                } else if (ch3 >= 127) {
                    System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 6, (end - i3) - 1);
                    this.buf[i3] = '\\';
                    this.buf[i3 + 1] = 'u';
                    this.buf[i3 + 2] = IOUtils.DIGITS[(ch3 >>> '\f') & 15];
                    this.buf[i3 + 3] = IOUtils.DIGITS[(ch3 >>> '\b') & 15];
                    this.buf[i3 + 4] = IOUtils.DIGITS[(ch3 >>> 4) & 15];
                    this.buf[i3 + 5] = IOUtils.DIGITS[ch3 & 15];
                    end += 5;
                }
                i3--;
                c2 = '/';
                c3 = '\"';
                c4 = '\b';
                c5 = '\f';
            }
            if (seperator != 0) {
                this.buf[this.count - 2] = '\"';
                this.buf[this.count - 1] = seperator;
                return;
            }
            this.buf[this.count - 1] = '\"';
            return;
        }
        int specialCount2 = 0;
        int firstSpecialIndex = -1;
        char lastSpecial = 0;
        int lastSpecialIndex2 = -1;
        int newcount3 = newcount;
        int i4 = start;
        while (i4 < end) {
            char ch4 = this.buf[i4];
            if (ch4 < ']') {
                if (ch4 < '@') {
                    specialCount = specialCount2;
                } else {
                    specialCount = specialCount2;
                }
                if (ch4 != c6) {
                    special = false;
                    if (special) {
                        specialCount2 = specialCount + 1;
                        lastSpecialIndex2 = i4;
                        lastSpecial = ch4;
                        if (ch4 == '(' || ch4 == ')' || ch4 == '<' || ch4 == '>' || (ch4 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch4] == 4)) {
                            newcount3 += 4;
                        }
                        if (firstSpecialIndex == -1) {
                            firstSpecialIndex = i4;
                        }
                        i4++;
                        c6 = '\\';
                    }
                }
                special = true;
                if (special) {
                }
            } else if (ch4 < 127 || !(ch4 == 8232 || ch4 == 8233 || ch4 < 160)) {
                specialCount = specialCount2;
            } else {
                if (firstSpecialIndex == -1) {
                    firstSpecialIndex = i4;
                }
                specialCount2++;
                lastSpecialIndex2 = i4;
                lastSpecial = ch4;
                newcount3 += 4;
                i4++;
                c6 = '\\';
            }
            specialCount2 = specialCount;
            i4++;
            c6 = '\\';
        }
        if (specialCount2 > 0) {
            int newcount4 = newcount3 + specialCount2;
            if (newcount4 > this.buf.length) {
                expandCapacity(newcount4);
            }
            this.count = newcount4;
            if (specialCount2 == 1) {
                if (lastSpecial == 8232) {
                    System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 6, (end - lastSpecialIndex2) - 1);
                    this.buf[lastSpecialIndex2] = '\\';
                    int lastSpecialIndex3 = lastSpecialIndex2 + 1;
                    this.buf[lastSpecialIndex3] = 'u';
                    int lastSpecialIndex4 = lastSpecialIndex3 + 1;
                    this.buf[lastSpecialIndex4] = '2';
                    int lastSpecialIndex5 = lastSpecialIndex4 + 1;
                    this.buf[lastSpecialIndex5] = '0';
                    int lastSpecialIndex6 = lastSpecialIndex5 + 1;
                    this.buf[lastSpecialIndex6] = '2';
                    this.buf[lastSpecialIndex6 + 1] = '8';
                } else if (lastSpecial == 8233) {
                    System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 6, (end - lastSpecialIndex2) - 1);
                    this.buf[lastSpecialIndex2] = '\\';
                    int lastSpecialIndex7 = lastSpecialIndex2 + 1;
                    this.buf[lastSpecialIndex7] = 'u';
                    int lastSpecialIndex8 = lastSpecialIndex7 + 1;
                    this.buf[lastSpecialIndex8] = '2';
                    int lastSpecialIndex9 = lastSpecialIndex8 + 1;
                    this.buf[lastSpecialIndex9] = '0';
                    int lastSpecialIndex10 = lastSpecialIndex9 + 1;
                    this.buf[lastSpecialIndex10] = '2';
                    this.buf[lastSpecialIndex10 + 1] = '9';
                } else {
                    if (lastSpecial != '(' && lastSpecial != ')' && lastSpecial != '<') {
                        if (lastSpecial != '>') {
                            if (lastSpecial >= IOUtils.specicalFlags_doubleQuotes.length || IOUtils.specicalFlags_doubleQuotes[lastSpecial] != 4) {
                                System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 2, (end - lastSpecialIndex2) - 1);
                                this.buf[lastSpecialIndex2] = '\\';
                                this.buf[lastSpecialIndex2 + 1] = IOUtils.replaceChars[lastSpecial];
                            } else {
                                System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 6, (end - lastSpecialIndex2) - 1);
                                int bufIndex2 = lastSpecialIndex2 + 1;
                                this.buf[lastSpecialIndex2] = '\\';
                                int bufIndex3 = bufIndex2 + 1;
                                this.buf[bufIndex2] = 'u';
                                int bufIndex4 = bufIndex3 + 1;
                                this.buf[bufIndex3] = IOUtils.DIGITS[(lastSpecial >>> '\f') & 15];
                                int bufIndex5 = bufIndex4 + 1;
                                this.buf[bufIndex4] = IOUtils.DIGITS[(lastSpecial >>> '\b') & 15];
                                int bufIndex6 = bufIndex5 + 1;
                                this.buf[bufIndex5] = IOUtils.DIGITS[(lastSpecial >>> 4) & 15];
                                int i5 = bufIndex6 + 1;
                                this.buf[bufIndex6] = IOUtils.DIGITS[lastSpecial & 15];
                            }
                        }
                    }
                    System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 6, (end - lastSpecialIndex2) - 1);
                    this.buf[lastSpecialIndex2] = '\\';
                    int lastSpecialIndex11 = lastSpecialIndex2 + 1;
                    this.buf[lastSpecialIndex11] = 'u';
                    int lastSpecialIndex12 = lastSpecialIndex11 + 1;
                    this.buf[lastSpecialIndex12] = IOUtils.DIGITS[(lastSpecial >>> '\f') & 15];
                    int lastSpecialIndex13 = lastSpecialIndex12 + 1;
                    this.buf[lastSpecialIndex13] = IOUtils.DIGITS[(lastSpecial >>> '\b') & 15];
                    int lastSpecialIndex14 = lastSpecialIndex13 + 1;
                    this.buf[lastSpecialIndex14] = IOUtils.DIGITS[(lastSpecial >>> 4) & 15];
                    this.buf[lastSpecialIndex14 + 1] = IOUtils.DIGITS[lastSpecial & 15];
                }
            } else if (specialCount2 > 1) {
                int end2 = end;
                int bufIndex7 = firstSpecialIndex;
                for (int i6 = firstSpecialIndex - start; i6 < text.length(); i6++) {
                    char ch5 = text.charAt(i6);
                    if (this.browserSecure) {
                        if (ch5 != '(') {
                            if (ch5 != ')') {
                                if (ch5 != '<') {
                                }
                            }
                        }
                        int bufIndex8 = bufIndex7 + 1;
                        this.buf[bufIndex7] = '\\';
                        int bufIndex9 = bufIndex8 + 1;
                        this.buf[bufIndex8] = 'u';
                        int bufIndex10 = bufIndex9 + 1;
                        this.buf[bufIndex9] = IOUtils.DIGITS[(ch5 >>> '\f') & 15];
                        int bufIndex11 = bufIndex10 + 1;
                        this.buf[bufIndex10] = IOUtils.DIGITS[(ch5 >>> '\b') & 15];
                        int bufIndex12 = bufIndex11 + 1;
                        this.buf[bufIndex11] = IOUtils.DIGITS[(ch5 >>> 4) & 15];
                        this.buf[bufIndex12] = IOUtils.DIGITS[ch5 & 15];
                        end2 += 5;
                        bufIndex7 = bufIndex12 + 1;
                    } else {
                        if ((ch5 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch5] != 0) || (ch5 == '/' && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                            int bufIndex13 = bufIndex7 + 1;
                            this.buf[bufIndex7] = '\\';
                            if (IOUtils.specicalFlags_doubleQuotes[ch5] == 4) {
                                int bufIndex14 = bufIndex13 + 1;
                                this.buf[bufIndex13] = 'u';
                                int bufIndex15 = bufIndex14 + 1;
                                this.buf[bufIndex14] = IOUtils.DIGITS[(ch5 >>> '\f') & 15];
                                int bufIndex16 = bufIndex15 + 1;
                                this.buf[bufIndex15] = IOUtils.DIGITS[(ch5 >>> '\b') & 15];
                                int bufIndex17 = bufIndex16 + 1;
                                this.buf[bufIndex16] = IOUtils.DIGITS[(ch5 >>> 4) & 15];
                                bufIndex = bufIndex17 + 1;
                                this.buf[bufIndex17] = IOUtils.DIGITS[ch5 & 15];
                                end2 += 5;
                            } else {
                                bufIndex = bufIndex13 + 1;
                                this.buf[bufIndex13] = IOUtils.replaceChars[ch5];
                                end2++;
                            }
                        } else if (ch5 == 8232 || ch5 == 8233) {
                            int bufIndex18 = bufIndex7 + 1;
                            this.buf[bufIndex7] = '\\';
                            int bufIndex19 = bufIndex18 + 1;
                            this.buf[bufIndex18] = 'u';
                            int bufIndex20 = bufIndex19 + 1;
                            this.buf[bufIndex19] = IOUtils.DIGITS[(ch5 >>> '\f') & 15];
                            int bufIndex21 = bufIndex20 + 1;
                            this.buf[bufIndex20] = IOUtils.DIGITS[(ch5 >>> '\b') & 15];
                            int bufIndex22 = bufIndex21 + 1;
                            this.buf[bufIndex21] = IOUtils.DIGITS[(ch5 >>> 4) & 15];
                            this.buf[bufIndex22] = IOUtils.DIGITS[ch5 & 15];
                            end2 += 5;
                            bufIndex7 = bufIndex22 + 1;
                        } else {
                            bufIndex = bufIndex7 + 1;
                            this.buf[bufIndex7] = ch5;
                        }
                        bufIndex7 = bufIndex;
                    }
                }
            }
        }
        if (seperator != 0) {
            this.buf[this.count - 2] = '\"';
            this.buf[this.count - 1] = seperator;
            return;
        }
        this.buf[this.count - 1] = '\"';
    }

    /* JADX INFO: Multiple debug info for r3v5 int: [D('len' int), D('srcPos' int)] */
    /* JADX INFO: Multiple debug info for r3v6 int: [D('len' int), D('srcPos' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x0527, code lost:
        if (r13 != '>') goto L_0x057d;
     */
    public void writeStringWithDoubleQuote(char[] text, char seperator) {
        int bufIndex;
        if (text == null) {
            writeNull();
            if (seperator != 0) {
                write(seperator);
                return;
            }
            return;
        }
        int len = text.length;
        int newcount = this.count + len + 2;
        if (seperator != 0) {
            newcount++;
        }
        char c = ')';
        char c2 = '/';
        char c3 = '\"';
        char c4 = '\b';
        char c5 = '\f';
        char c6 = '\\';
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                write(34);
                int i = 0;
                while (i < text.length) {
                    char ch = text[i];
                    if (!isEnabled(SerializerFeature.BrowserSecure) || !(ch == '(' || ch == c || ch == '<' || ch == '>')) {
                        if (isEnabled(SerializerFeature.BrowserCompatible)) {
                            if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\"' || ch == '/' || ch == '\\') {
                                write(92);
                                write(IOUtils.replaceChars[ch]);
                            } else if (ch < ' ') {
                                write(92);
                                write(117);
                                write(48);
                                write(48);
                                write(IOUtils.ASCII_CHARS[ch * 2]);
                                write(IOUtils.ASCII_CHARS[(ch * 2) + 1]);
                            } else if (ch >= 127) {
                                write(92);
                                write(117);
                                write(IOUtils.DIGITS[(ch >>> '\f') & 15]);
                                write(IOUtils.DIGITS[(ch >>> '\b') & 15]);
                                write(IOUtils.DIGITS[(ch >>> 4) & 15]);
                                write(IOUtils.DIGITS[ch & 15]);
                            }
                        } else if ((ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] != 0) || (ch == '/' && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                            write(92);
                            if (IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
                                write(117);
                                write(IOUtils.DIGITS[(ch >>> '\f') & 15]);
                                write(IOUtils.DIGITS[(ch >>> '\b') & 15]);
                                write(IOUtils.DIGITS[(ch >>> 4) & 15]);
                                write(IOUtils.DIGITS[ch & 15]);
                            } else {
                                write(IOUtils.replaceChars[ch]);
                            }
                        }
                        write(ch);
                    } else {
                        write(92);
                        write(117);
                        write(IOUtils.DIGITS[(ch >>> '\f') & 15]);
                        write(IOUtils.DIGITS[(ch >>> '\b') & 15]);
                        write(IOUtils.DIGITS[(ch >>> 4) & 15]);
                        write(IOUtils.DIGITS[ch & 15]);
                    }
                    i++;
                    c = ')';
                }
                write(34);
                if (seperator != 0) {
                    write(seperator);
                    return;
                }
                return;
            }
            expandCapacity(newcount);
        }
        int start = this.count + 1;
        int end = start + len;
        this.buf[this.count] = '\"';
        System.arraycopy(text, 0, this.buf, start, text.length);
        this.count = newcount;
        if (isEnabled(SerializerFeature.BrowserCompatible)) {
            int lastSpecialIndex = -1;
            int newcount2 = newcount;
            for (int i2 = start; i2 < end; i2++) {
                char ch2 = this.buf[i2];
                if (ch2 == '\"' || ch2 == '/' || ch2 == '\\') {
                    lastSpecialIndex = i2;
                    newcount2++;
                } else if (ch2 == '\b' || ch2 == '\f' || ch2 == '\n' || ch2 == '\r' || ch2 == '\t') {
                    lastSpecialIndex = i2;
                    newcount2++;
                } else if (ch2 < ' ') {
                    lastSpecialIndex = i2;
                    newcount2 += 5;
                } else if (ch2 >= 127) {
                    lastSpecialIndex = i2;
                    newcount2 += 5;
                }
            }
            if (newcount2 > this.buf.length) {
                expandCapacity(newcount2);
            }
            this.count = newcount2;
            int i3 = lastSpecialIndex;
            while (i3 >= start) {
                char ch3 = this.buf[i3];
                if (ch3 == c4 || ch3 == c5 || ch3 == '\n' || ch3 == '\r' || ch3 == '\t') {
                    System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 2, (end - i3) - 1);
                    this.buf[i3] = '\\';
                    this.buf[i3 + 1] = IOUtils.replaceChars[ch3];
                    end++;
                } else if (ch3 == c3 || ch3 == c2 || ch3 == '\\') {
                    System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 2, (end - i3) - 1);
                    this.buf[i3] = '\\';
                    this.buf[i3 + 1] = ch3;
                    end++;
                } else if (ch3 < ' ') {
                    System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 6, (end - i3) - 1);
                    this.buf[i3] = '\\';
                    this.buf[i3 + 1] = 'u';
                    this.buf[i3 + 2] = '0';
                    this.buf[i3 + 3] = '0';
                    this.buf[i3 + 4] = IOUtils.ASCII_CHARS[ch3 * 2];
                    this.buf[i3 + 5] = IOUtils.ASCII_CHARS[(ch3 * 2) + 1];
                    end += 5;
                } else if (ch3 >= 127) {
                    System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 6, (end - i3) - 1);
                    this.buf[i3] = '\\';
                    this.buf[i3 + 1] = 'u';
                    this.buf[i3 + 2] = IOUtils.DIGITS[(ch3 >>> '\f') & 15];
                    this.buf[i3 + 3] = IOUtils.DIGITS[(ch3 >>> '\b') & 15];
                    this.buf[i3 + 4] = IOUtils.DIGITS[(ch3 >>> 4) & 15];
                    this.buf[i3 + 5] = IOUtils.DIGITS[ch3 & 15];
                    end += 5;
                }
                i3--;
                c2 = '/';
                c3 = '\"';
                c4 = '\b';
                c5 = '\f';
            }
            if (seperator != 0) {
                this.buf[this.count - 2] = '\"';
                this.buf[this.count - 1] = seperator;
                return;
            }
            this.buf[this.count - 1] = '\"';
            return;
        }
        int specialCount = 0;
        int firstSpecialIndex = -1;
        char lastSpecial = 0;
        int lastSpecialIndex2 = -1;
        int newcount3 = newcount;
        int i4 = start;
        while (i4 < end) {
            char ch4 = this.buf[i4];
            if (ch4 < ']') {
                if ((ch4 < '@' && (this.sepcialBits & (1 << ch4)) != 0) || ch4 == c6) {
                    specialCount++;
                    lastSpecialIndex2 = i4;
                    lastSpecial = ch4;
                    if (ch4 == '(' || ch4 == ')' || ch4 == '<' || ch4 == '>' || (ch4 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch4] == 4)) {
                        newcount3 += 4;
                    }
                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = i4;
                    }
                }
            } else if (ch4 >= 127 && (ch4 == 8232 || ch4 == 8233 || ch4 < 160)) {
                if (firstSpecialIndex == -1) {
                    firstSpecialIndex = i4;
                }
                specialCount++;
                lastSpecialIndex2 = i4;
                lastSpecial = ch4;
                newcount3 += 4;
            }
            i4++;
            c6 = '\\';
        }
        if (specialCount > 0) {
            int newcount4 = newcount3 + specialCount;
            if (newcount4 > this.buf.length) {
                expandCapacity(newcount4);
            }
            this.count = newcount4;
            if (specialCount == 1) {
                if (lastSpecial == 8232) {
                    System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 6, (end - lastSpecialIndex2) - 1);
                    this.buf[lastSpecialIndex2] = '\\';
                    int lastSpecialIndex3 = lastSpecialIndex2 + 1;
                    this.buf[lastSpecialIndex3] = 'u';
                    int lastSpecialIndex4 = lastSpecialIndex3 + 1;
                    this.buf[lastSpecialIndex4] = '2';
                    int lastSpecialIndex5 = lastSpecialIndex4 + 1;
                    this.buf[lastSpecialIndex5] = '0';
                    int lastSpecialIndex6 = lastSpecialIndex5 + 1;
                    this.buf[lastSpecialIndex6] = '2';
                    this.buf[lastSpecialIndex6 + 1] = '8';
                } else if (lastSpecial == 8233) {
                    System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 6, (end - lastSpecialIndex2) - 1);
                    this.buf[lastSpecialIndex2] = '\\';
                    int lastSpecialIndex7 = lastSpecialIndex2 + 1;
                    this.buf[lastSpecialIndex7] = 'u';
                    int lastSpecialIndex8 = lastSpecialIndex7 + 1;
                    this.buf[lastSpecialIndex8] = '2';
                    int lastSpecialIndex9 = lastSpecialIndex8 + 1;
                    this.buf[lastSpecialIndex9] = '0';
                    int lastSpecialIndex10 = lastSpecialIndex9 + 1;
                    this.buf[lastSpecialIndex10] = '2';
                    this.buf[lastSpecialIndex10 + 1] = '9';
                } else {
                    if (lastSpecial != '(' && lastSpecial != ')' && lastSpecial != '<') {
                        if (lastSpecial != '>') {
                            if (lastSpecial >= IOUtils.specicalFlags_doubleQuotes.length || IOUtils.specicalFlags_doubleQuotes[lastSpecial] != 4) {
                                System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 2, (end - lastSpecialIndex2) - 1);
                                this.buf[lastSpecialIndex2] = '\\';
                                this.buf[lastSpecialIndex2 + 1] = IOUtils.replaceChars[lastSpecial];
                            } else {
                                System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 6, (end - lastSpecialIndex2) - 1);
                                int bufIndex2 = lastSpecialIndex2 + 1;
                                this.buf[lastSpecialIndex2] = '\\';
                                int bufIndex3 = bufIndex2 + 1;
                                this.buf[bufIndex2] = 'u';
                                int bufIndex4 = bufIndex3 + 1;
                                this.buf[bufIndex3] = IOUtils.DIGITS[(lastSpecial >>> '\f') & 15];
                                int bufIndex5 = bufIndex4 + 1;
                                this.buf[bufIndex4] = IOUtils.DIGITS[(lastSpecial >>> '\b') & 15];
                                int bufIndex6 = bufIndex5 + 1;
                                this.buf[bufIndex5] = IOUtils.DIGITS[(lastSpecial >>> 4) & 15];
                                int i5 = bufIndex6 + 1;
                                this.buf[bufIndex6] = IOUtils.DIGITS[lastSpecial & 15];
                            }
                        }
                    }
                    System.arraycopy(this.buf, lastSpecialIndex2 + 1, this.buf, lastSpecialIndex2 + 6, (end - lastSpecialIndex2) - 1);
                    this.buf[lastSpecialIndex2] = '\\';
                    int lastSpecialIndex11 = lastSpecialIndex2 + 1;
                    this.buf[lastSpecialIndex11] = 'u';
                    int lastSpecialIndex12 = lastSpecialIndex11 + 1;
                    this.buf[lastSpecialIndex12] = IOUtils.DIGITS[(lastSpecial >>> '\f') & 15];
                    int lastSpecialIndex13 = lastSpecialIndex12 + 1;
                    this.buf[lastSpecialIndex13] = IOUtils.DIGITS[(lastSpecial >>> '\b') & 15];
                    int lastSpecialIndex14 = lastSpecialIndex13 + 1;
                    this.buf[lastSpecialIndex14] = IOUtils.DIGITS[(lastSpecial >>> 4) & 15];
                    this.buf[lastSpecialIndex14 + 1] = IOUtils.DIGITS[lastSpecial & 15];
                }
            } else if (specialCount > 1) {
                int end2 = end;
                int bufIndex7 = firstSpecialIndex;
                for (int i6 = firstSpecialIndex - start; i6 < text.length; i6++) {
                    char ch5 = text[i6];
                    if (this.browserSecure) {
                        if (ch5 != '(') {
                            if (ch5 != ')') {
                                if (ch5 != '<') {
                                }
                            }
                        }
                        int bufIndex8 = bufIndex7 + 1;
                        this.buf[bufIndex7] = '\\';
                        int bufIndex9 = bufIndex8 + 1;
                        this.buf[bufIndex8] = 'u';
                        int bufIndex10 = bufIndex9 + 1;
                        this.buf[bufIndex9] = IOUtils.DIGITS[(ch5 >>> '\f') & 15];
                        int bufIndex11 = bufIndex10 + 1;
                        this.buf[bufIndex10] = IOUtils.DIGITS[(ch5 >>> '\b') & 15];
                        int bufIndex12 = bufIndex11 + 1;
                        this.buf[bufIndex11] = IOUtils.DIGITS[(ch5 >>> 4) & 15];
                        this.buf[bufIndex12] = IOUtils.DIGITS[ch5 & 15];
                        end2 += 5;
                        bufIndex7 = bufIndex12 + 1;
                    } else {
                        if ((ch5 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch5] != 0) || (ch5 == '/' && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                            int bufIndex13 = bufIndex7 + 1;
                            this.buf[bufIndex7] = '\\';
                            if (IOUtils.specicalFlags_doubleQuotes[ch5] == 4) {
                                int bufIndex14 = bufIndex13 + 1;
                                this.buf[bufIndex13] = 'u';
                                int bufIndex15 = bufIndex14 + 1;
                                this.buf[bufIndex14] = IOUtils.DIGITS[(ch5 >>> '\f') & 15];
                                int bufIndex16 = bufIndex15 + 1;
                                this.buf[bufIndex15] = IOUtils.DIGITS[(ch5 >>> '\b') & 15];
                                int bufIndex17 = bufIndex16 + 1;
                                this.buf[bufIndex16] = IOUtils.DIGITS[(ch5 >>> 4) & 15];
                                bufIndex = bufIndex17 + 1;
                                this.buf[bufIndex17] = IOUtils.DIGITS[ch5 & 15];
                                end2 += 5;
                            } else {
                                bufIndex = bufIndex13 + 1;
                                this.buf[bufIndex13] = IOUtils.replaceChars[ch5];
                                end2++;
                            }
                        } else if (ch5 == 8232 || ch5 == 8233) {
                            int bufIndex18 = bufIndex7 + 1;
                            this.buf[bufIndex7] = '\\';
                            int bufIndex19 = bufIndex18 + 1;
                            this.buf[bufIndex18] = 'u';
                            int bufIndex20 = bufIndex19 + 1;
                            this.buf[bufIndex19] = IOUtils.DIGITS[(ch5 >>> '\f') & 15];
                            int bufIndex21 = bufIndex20 + 1;
                            this.buf[bufIndex20] = IOUtils.DIGITS[(ch5 >>> '\b') & 15];
                            int bufIndex22 = bufIndex21 + 1;
                            this.buf[bufIndex21] = IOUtils.DIGITS[(ch5 >>> 4) & 15];
                            this.buf[bufIndex22] = IOUtils.DIGITS[ch5 & 15];
                            end2 += 5;
                            bufIndex7 = bufIndex22 + 1;
                        } else {
                            bufIndex = bufIndex7 + 1;
                            this.buf[bufIndex7] = ch5;
                        }
                        bufIndex7 = bufIndex;
                    }
                }
            }
        }
        if (seperator != 0) {
            this.buf[this.count - 2] = '\"';
            this.buf[this.count - 1] = seperator;
            return;
        }
        this.buf[this.count - 1] = '\"';
    }

    public void writeFieldNameDirect(String text) {
        int len = text.length();
        int newcount = this.count + len + 3;
        if (newcount > this.buf.length) {
            expandCapacity(newcount);
        }
        this.buf[this.count] = '\"';
        text.getChars(0, len, this.buf, this.count + 1);
        this.count = newcount;
        this.buf[this.count - 2] = '\"';
        this.buf[this.count - 1] = ':';
    }

    public void write(List<String> list) {
        int offset;
        if (list.isEmpty()) {
            write("[]");
            return;
        }
        int offset2 = this.count;
        int i = 0;
        int list_size = list.size();
        while (i < list_size) {
            String text = list.get(i);
            boolean hasSpecial = false;
            if (text == null) {
                hasSpecial = true;
            } else {
                int len = text.length();
                for (int j = 0; j < len; j++) {
                    char ch = text.charAt(j);
                    boolean z = ch < ' ' || ch > '~' || ch == '\"' || ch == '\\';
                    hasSpecial = z;
                    if (z) {
                        break;
                    }
                }
            }
            if (hasSpecial) {
                this.count = offset2;
                write(91);
                for (int j2 = 0; j2 < list.size(); j2++) {
                    String text2 = list.get(j2);
                    if (j2 != 0) {
                        write(44);
                    }
                    if (text2 == null) {
                        write("null");
                    } else {
                        writeStringWithDoubleQuote(text2, (char) 0);
                    }
                }
                write(93);
                return;
            }
            int newcount = text.length() + offset2 + 3;
            if (i == list.size() - 1) {
                newcount++;
            }
            if (newcount > this.buf.length) {
                this.count = offset2;
                expandCapacity(newcount);
            }
            if (i == 0) {
                offset = offset2 + 1;
                this.buf[offset2] = '[';
            } else {
                this.buf[offset2] = ',';
                offset = offset2 + 1;
            }
            int offset3 = offset + 1;
            this.buf[offset] = '\"';
            text.getChars(0, text.length(), this.buf, offset3);
            int offset4 = offset3 + text.length();
            this.buf[offset4] = '\"';
            i++;
            offset2 = offset4 + 1;
        }
        this.buf[offset2] = ']';
        this.count = offset2 + 1;
    }

    public void writeFieldValue(char seperator, String name, char value) {
        write(seperator);
        writeFieldName(name);
        if (value == 0) {
            writeString("\u0000");
        } else {
            writeString(Character.toString(value));
        }
    }

    public void writeFieldValue(char seperator, String name, boolean value) {
        if (!this.quoteFieldNames) {
            write(seperator);
            writeFieldName(name);
            write(value);
            return;
        }
        int intSize = value ? 4 : 5;
        int nameLen = name.length();
        int newcount = this.count + nameLen + 4 + intSize;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                write(seperator);
                writeString(name);
                write(58);
                write(value);
                return;
            }
            expandCapacity(newcount);
        }
        int start = this.count;
        this.count = newcount;
        this.buf[start] = seperator;
        int nameEnd = start + nameLen + 1;
        this.buf[start + 1] = this.keySeperator;
        name.getChars(0, nameLen, this.buf, start + 2);
        this.buf[nameEnd + 1] = this.keySeperator;
        if (value) {
            System.arraycopy(":true".toCharArray(), 0, this.buf, nameEnd + 2, 5);
        } else {
            System.arraycopy(":false".toCharArray(), 0, this.buf, nameEnd + 2, 6);
        }
    }

    public void write(boolean value) {
        if (value) {
            write("true");
        } else {
            write("false");
        }
    }

    public void writeFieldValue(char seperator, String name, int value) {
        if (value == Integer.MIN_VALUE || !this.quoteFieldNames) {
            write(seperator);
            writeFieldName(name);
            writeInt(value);
            return;
        }
        int intSize = value < 0 ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
        int nameLen = name.length();
        int newcount = this.count + nameLen + 4 + intSize;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                write(seperator);
                writeFieldName(name);
                writeInt(value);
                return;
            }
            expandCapacity(newcount);
        }
        int start = this.count;
        this.count = newcount;
        this.buf[start] = seperator;
        int nameEnd = start + nameLen + 1;
        this.buf[start + 1] = this.keySeperator;
        name.getChars(0, nameLen, this.buf, start + 2);
        this.buf[nameEnd + 1] = this.keySeperator;
        this.buf[nameEnd + 2] = ':';
        IOUtils.getChars(value, this.count, this.buf);
    }

    public void writeFieldValue(char seperator, String name, long value) {
        if (value == Long.MIN_VALUE || !this.quoteFieldNames || isEnabled(SerializerFeature.BrowserCompatible.mask)) {
            write(seperator);
            writeFieldName(name);
            writeLong(value);
            return;
        }
        int intSize = value < 0 ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
        int nameLen = name.length();
        int newcount = this.count + nameLen + 4 + intSize;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                write(seperator);
                writeFieldName(name);
                writeLong(value);
                return;
            }
            expandCapacity(newcount);
        }
        int start = this.count;
        this.count = newcount;
        this.buf[start] = seperator;
        int nameEnd = start + nameLen + 1;
        this.buf[start + 1] = this.keySeperator;
        name.getChars(0, nameLen, this.buf, start + 2);
        this.buf[nameEnd + 1] = this.keySeperator;
        this.buf[nameEnd + 2] = ':';
        IOUtils.getChars(value, this.count, this.buf);
    }

    public void writeFieldValue(char seperator, String name, float value) {
        write(seperator);
        writeFieldName(name);
        writeFloat(value, false);
    }

    public void writeFieldValue(char seperator, String name, double value) {
        write(seperator);
        writeFieldName(name);
        writeDouble(value, false);
    }

    public void writeFieldValue(char seperator, String name, String value) {
        if (!this.quoteFieldNames) {
            write(seperator);
            writeFieldName(name);
            if (value == null) {
                writeNull();
            } else {
                writeString(value);
            }
        } else if (this.useSingleQuotes) {
            write(seperator);
            writeFieldName(name);
            if (value == null) {
                writeNull();
            } else {
                writeString(value);
            }
        } else if (isEnabled(SerializerFeature.BrowserCompatible)) {
            write(seperator);
            writeStringWithDoubleQuote(name, ':');
            writeStringWithDoubleQuote(value, (char) 0);
        } else {
            writeFieldValueStringWithDoubleQuoteCheck(seperator, name, value);
        }
    }

    /* JADX INFO: Multiple debug info for r3v8 int: [D('nameLen' int), D('srcPos' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x02ec, code lost:
        if (r5 != '>') goto L_0x0342;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00de, code lost:
        if ((r29.sepcialBits & (1 << r11)) == 0) goto L_0x00e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x010e, code lost:
        if (com.alibaba.fastjson.util.IOUtils.specicalFlags_doubleQuotes[r11] == 4) goto L_0x0116;
     */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x0120 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ee  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x011b  */
    public void writeFieldValueStringWithDoubleQuoteCheck(char seperator, String name, String value) {
        int valueLen;
        int newcount;
        int bufIndex;
        int nameStart;
        int valueLen2;
        boolean special;
        int nameLen = name.length();
        int newcount2 = this.count;
        if (value == null) {
            valueLen = 4;
            newcount = newcount2 + nameLen + 8;
        } else {
            valueLen = value.length();
            newcount = newcount2 + nameLen + valueLen + 6;
        }
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                write(seperator);
                writeStringWithDoubleQuote(name, ':');
                writeStringWithDoubleQuote(value, (char) 0);
                return;
            }
            expandCapacity(newcount);
        }
        this.buf[this.count] = seperator;
        int nameStart2 = this.count + 2;
        int nameEnd = nameStart2 + nameLen;
        this.buf[this.count + 1] = '\"';
        name.getChars(0, nameLen, this.buf, nameStart2);
        this.count = newcount;
        this.buf[nameEnd] = '\"';
        int index = nameEnd + 1;
        int index2 = index + 1;
        this.buf[index] = ':';
        if (value == null) {
            int index3 = index2 + 1;
            this.buf[index2] = 'n';
            int index4 = index3 + 1;
            this.buf[index3] = 'u';
            int index5 = index4 + 1;
            this.buf[index4] = 'l';
            int i = index5 + 1;
            this.buf[index5] = 'l';
            return;
        }
        int index6 = index2 + 1;
        this.buf[index2] = '\"';
        int valueEnd = index6 + valueLen;
        value.getChars(0, valueLen, this.buf, index6);
        int specialCount = 0;
        int firstSpecialIndex = -1;
        char lastSpecial = 0;
        int lastSpecialIndex = -1;
        int newcount3 = newcount;
        int i2 = index6;
        while (i2 < valueEnd) {
            char ch = this.buf[i2];
            if (ch >= ']') {
                if (ch >= 127 && (ch == 8232 || ch == 8233 || ch < 160)) {
                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = i2;
                    }
                    specialCount++;
                    lastSpecial = ch;
                    newcount3 += 4;
                    lastSpecialIndex = i2;
                }
                valueLen2 = valueLen;
                nameStart = nameStart2;
            } else {
                if (ch < '@') {
                    valueLen2 = valueLen;
                    nameStart = nameStart2;
                } else {
                    valueLen2 = valueLen;
                    nameStart = nameStart2;
                }
                if (ch != '\\') {
                    special = false;
                    if (!special) {
                        specialCount++;
                        if (ch != '(' && ch != ')' && ch != '<' && ch != '>') {
                            if (ch < IOUtils.specicalFlags_doubleQuotes.length) {
                            }
                            if (firstSpecialIndex == -1) {
                                firstSpecialIndex = i2;
                            }
                            lastSpecialIndex = i2;
                            lastSpecial = ch;
                        }
                        newcount3 += 4;
                        if (firstSpecialIndex == -1) {
                        }
                        lastSpecialIndex = i2;
                        lastSpecial = ch;
                    }
                }
                special = true;
                if (!special) {
                }
            }
            i2++;
            valueLen = valueLen2;
            nameStart2 = nameStart;
        }
        if (specialCount > 0) {
            int newcount4 = newcount3 + specialCount;
            if (newcount4 > this.buf.length) {
                expandCapacity(newcount4);
            }
            this.count = newcount4;
            if (specialCount == 1) {
                if (lastSpecial == 8232) {
                    System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 6, (valueEnd - lastSpecialIndex) - 1);
                    this.buf[lastSpecialIndex] = '\\';
                    int lastSpecialIndex2 = lastSpecialIndex + 1;
                    this.buf[lastSpecialIndex2] = 'u';
                    int lastSpecialIndex3 = lastSpecialIndex2 + 1;
                    this.buf[lastSpecialIndex3] = '2';
                    int lastSpecialIndex4 = lastSpecialIndex3 + 1;
                    this.buf[lastSpecialIndex4] = '0';
                    int lastSpecialIndex5 = lastSpecialIndex4 + 1;
                    this.buf[lastSpecialIndex5] = '2';
                    this.buf[lastSpecialIndex5 + 1] = '8';
                } else if (lastSpecial == 8233) {
                    System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 6, (valueEnd - lastSpecialIndex) - 1);
                    this.buf[lastSpecialIndex] = '\\';
                    int lastSpecialIndex6 = lastSpecialIndex + 1;
                    this.buf[lastSpecialIndex6] = 'u';
                    int lastSpecialIndex7 = lastSpecialIndex6 + 1;
                    this.buf[lastSpecialIndex7] = '2';
                    int lastSpecialIndex8 = lastSpecialIndex7 + 1;
                    this.buf[lastSpecialIndex8] = '0';
                    int lastSpecialIndex9 = lastSpecialIndex8 + 1;
                    this.buf[lastSpecialIndex9] = '2';
                    this.buf[lastSpecialIndex9 + 1] = '9';
                } else {
                    if (lastSpecial != '(' && lastSpecial != ')' && lastSpecial != '<') {
                        if (lastSpecial != '>') {
                            if (lastSpecial >= IOUtils.specicalFlags_doubleQuotes.length || IOUtils.specicalFlags_doubleQuotes[lastSpecial] != 4) {
                                System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, (valueEnd - lastSpecialIndex) - 1);
                                this.buf[lastSpecialIndex] = '\\';
                                this.buf[lastSpecialIndex + 1] = IOUtils.replaceChars[lastSpecial];
                            } else {
                                System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 6, (valueEnd - lastSpecialIndex) - 1);
                                int bufIndex2 = lastSpecialIndex + 1;
                                this.buf[lastSpecialIndex] = '\\';
                                int bufIndex3 = bufIndex2 + 1;
                                this.buf[bufIndex2] = 'u';
                                int bufIndex4 = bufIndex3 + 1;
                                this.buf[bufIndex3] = IOUtils.DIGITS[(lastSpecial >>> '\f') & 15];
                                int bufIndex5 = bufIndex4 + 1;
                                this.buf[bufIndex4] = IOUtils.DIGITS[(lastSpecial >>> '\b') & 15];
                                int bufIndex6 = bufIndex5 + 1;
                                this.buf[bufIndex5] = IOUtils.DIGITS[(lastSpecial >>> 4) & 15];
                                int i3 = bufIndex6 + 1;
                                this.buf[bufIndex6] = IOUtils.DIGITS[lastSpecial & 15];
                            }
                        }
                    }
                    System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 6, (valueEnd - lastSpecialIndex) - 1);
                    int bufIndex7 = lastSpecialIndex + 1;
                    this.buf[lastSpecialIndex] = '\\';
                    int bufIndex8 = bufIndex7 + 1;
                    this.buf[bufIndex7] = 'u';
                    int bufIndex9 = bufIndex8 + 1;
                    this.buf[bufIndex8] = IOUtils.DIGITS[(lastSpecial >>> '\f') & 15];
                    int bufIndex10 = bufIndex9 + 1;
                    this.buf[bufIndex9] = IOUtils.DIGITS[(lastSpecial >>> '\b') & 15];
                    int bufIndex11 = bufIndex10 + 1;
                    this.buf[bufIndex10] = IOUtils.DIGITS[(lastSpecial >>> 4) & 15];
                    int i4 = bufIndex11 + 1;
                    this.buf[bufIndex11] = IOUtils.DIGITS[lastSpecial & 15];
                }
            } else if (specialCount > 1) {
                int bufIndex12 = firstSpecialIndex;
                for (int i5 = firstSpecialIndex - index6; i5 < value.length(); i5++) {
                    char ch2 = value.charAt(i5);
                    if (this.browserSecure) {
                        if (ch2 != '(') {
                            if (ch2 != ')') {
                                if (ch2 != '<') {
                                }
                            }
                        }
                        int bufIndex13 = bufIndex12 + 1;
                        this.buf[bufIndex12] = '\\';
                        int bufIndex14 = bufIndex13 + 1;
                        this.buf[bufIndex13] = 'u';
                        int bufIndex15 = bufIndex14 + 1;
                        this.buf[bufIndex14] = IOUtils.DIGITS[(ch2 >>> '\f') & 15];
                        int bufIndex16 = bufIndex15 + 1;
                        this.buf[bufIndex15] = IOUtils.DIGITS[(ch2 >>> '\b') & 15];
                        int bufIndex17 = bufIndex16 + 1;
                        this.buf[bufIndex16] = IOUtils.DIGITS[(ch2 >>> 4) & 15];
                        this.buf[bufIndex17] = IOUtils.DIGITS[ch2 & 15];
                        valueEnd += 5;
                        bufIndex12 = bufIndex17 + 1;
                    } else if ((ch2 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch2] != 0) || (ch2 == '/' && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                        int bufIndex18 = bufIndex12 + 1;
                        this.buf[bufIndex12] = '\\';
                        if (IOUtils.specicalFlags_doubleQuotes[ch2] == 4) {
                            int bufIndex19 = bufIndex18 + 1;
                            this.buf[bufIndex18] = 'u';
                            int bufIndex20 = bufIndex19 + 1;
                            this.buf[bufIndex19] = IOUtils.DIGITS[(ch2 >>> '\f') & 15];
                            int bufIndex21 = bufIndex20 + 1;
                            this.buf[bufIndex20] = IOUtils.DIGITS[(ch2 >>> '\b') & 15];
                            int bufIndex22 = bufIndex21 + 1;
                            this.buf[bufIndex21] = IOUtils.DIGITS[(ch2 >>> 4) & 15];
                            bufIndex = bufIndex22 + 1;
                            this.buf[bufIndex22] = IOUtils.DIGITS[ch2 & 15];
                            valueEnd += 5;
                        } else {
                            bufIndex = bufIndex18 + 1;
                            this.buf[bufIndex18] = IOUtils.replaceChars[ch2];
                            valueEnd++;
                        }
                        bufIndex12 = bufIndex;
                    } else if (ch2 == 8232 || ch2 == 8233) {
                        int bufIndex23 = bufIndex12 + 1;
                        this.buf[bufIndex12] = '\\';
                        int bufIndex24 = bufIndex23 + 1;
                        this.buf[bufIndex23] = 'u';
                        int bufIndex25 = bufIndex24 + 1;
                        this.buf[bufIndex24] = IOUtils.DIGITS[(ch2 >>> '\f') & 15];
                        int bufIndex26 = bufIndex25 + 1;
                        this.buf[bufIndex25] = IOUtils.DIGITS[(ch2 >>> '\b') & 15];
                        int bufIndex27 = bufIndex26 + 1;
                        this.buf[bufIndex26] = IOUtils.DIGITS[(ch2 >>> 4) & 15];
                        this.buf[bufIndex27] = IOUtils.DIGITS[ch2 & 15];
                        valueEnd += 5;
                        bufIndex12 = bufIndex27 + 1;
                    } else {
                        this.buf[bufIndex12] = ch2;
                        bufIndex12++;
                    }
                }
            }
        }
        this.buf[this.count - 1] = '\"';
    }

    public void writeFieldValueStringWithDoubleQuote(char seperator, String name, String value) {
        int nameLen = name.length();
        int newcount = this.count;
        int valueLen = value.length();
        int newcount2 = newcount + nameLen + valueLen + 6;
        if (newcount2 > this.buf.length) {
            if (this.writer != null) {
                write(seperator);
                writeStringWithDoubleQuote(name, ':');
                writeStringWithDoubleQuote(value, (char) 0);
                return;
            }
            expandCapacity(newcount2);
        }
        this.buf[this.count] = seperator;
        int nameStart = this.count + 2;
        int nameEnd = nameStart + nameLen;
        this.buf[this.count + 1] = '\"';
        name.getChars(0, nameLen, this.buf, nameStart);
        this.count = newcount2;
        this.buf[nameEnd] = '\"';
        int index = nameEnd + 1;
        int index2 = index + 1;
        this.buf[index] = ':';
        this.buf[index2] = '\"';
        value.getChars(0, valueLen, this.buf, index2 + 1);
        this.buf[this.count - 1] = '\"';
    }

    public void writeFieldValue(char seperator, String name, Enum<?> value) {
        if (value == null) {
            write(seperator);
            writeFieldName(name);
            writeNull();
        } else if (this.writeEnumUsingName && !this.writeEnumUsingToString) {
            writeEnumFieldValue(seperator, name, value.name());
        } else if (this.writeEnumUsingToString) {
            writeEnumFieldValue(seperator, name, value.toString());
        } else {
            writeFieldValue(seperator, name, value.ordinal());
        }
    }

    private void writeEnumFieldValue(char seperator, String name, String value) {
        if (this.useSingleQuotes) {
            writeFieldValue(seperator, name, value);
        } else {
            writeFieldValueStringWithDoubleQuote(seperator, name, value);
        }
    }

    public void writeFieldValue(char seperator, String name, BigDecimal value) {
        String str;
        write(seperator);
        writeFieldName(name);
        if (value == null) {
            writeNull();
            return;
        }
        int scale = value.scale();
        if (!isEnabled(SerializerFeature.WriteBigDecimalAsPlain) || scale < -100 || scale >= 100) {
            str = value.toString();
        } else {
            str = value.toPlainString();
        }
        write(str);
    }

    public void writeString(String text, char seperator) {
        if (this.useSingleQuotes) {
            writeStringWithSingleQuote(text);
            write(seperator);
            return;
        }
        writeStringWithDoubleQuote(text, seperator);
    }

    public void writeString(String text) {
        if (this.useSingleQuotes) {
            writeStringWithSingleQuote(text);
        } else {
            writeStringWithDoubleQuote(text, (char) 0);
        }
    }

    public void writeString(char[] chars) {
        if (this.useSingleQuotes) {
            writeStringWithSingleQuote(chars);
        } else {
            writeStringWithDoubleQuote(new String(chars), (char) 0);
        }
    }

    /* access modifiers changed from: protected */
    public void writeStringWithSingleQuote(String text) {
        char c;
        if (text == null) {
            int newcount = this.count + 4;
            if (newcount > this.buf.length) {
                expandCapacity(newcount);
            }
            "null".getChars(0, 4, this.buf, this.count);
            this.count = newcount;
            return;
        }
        int len = text.length();
        int newcount2 = this.count + len + 2;
        char c2 = '/';
        char c3 = '\\';
        if (newcount2 > this.buf.length) {
            if (this.writer != null) {
                write(39);
                for (int i = 0; i < text.length(); i++) {
                    char ch = text.charAt(i);
                    if (ch <= '\r' || ch == '\\' || ch == '\'' || (ch == '/' && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                        write(92);
                        write(IOUtils.replaceChars[ch]);
                    } else {
                        write(ch);
                    }
                }
                write(39);
                return;
            }
            expandCapacity(newcount2);
        }
        int start = this.count + 1;
        int end = start + len;
        this.buf[this.count] = '\'';
        text.getChars(0, len, this.buf, start);
        this.count = newcount2;
        char lastSpecial = 0;
        int lastSpecialIndex = -1;
        int specialCount = 0;
        int i2 = start;
        while (i2 < end) {
            char ch2 = this.buf[i2];
            if (ch2 <= '\r' || ch2 == '\\' || ch2 == '\'' || (ch2 == c2 && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                specialCount++;
                lastSpecial = ch2;
                lastSpecialIndex = i2;
            }
            i2++;
            c2 = '/';
        }
        int newcount3 = newcount2 + specialCount;
        if (newcount3 > this.buf.length) {
            expandCapacity(newcount3);
        }
        this.count = newcount3;
        if (specialCount == 1) {
            System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, (end - lastSpecialIndex) - 1);
            this.buf[lastSpecialIndex] = '\\';
            this.buf[lastSpecialIndex + 1] = IOUtils.replaceChars[lastSpecial];
        } else if (specialCount > 1) {
            System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, (end - lastSpecialIndex) - 1);
            this.buf[lastSpecialIndex] = '\\';
            int lastSpecialIndex2 = lastSpecialIndex + 1;
            this.buf[lastSpecialIndex2] = IOUtils.replaceChars[lastSpecial];
            int end2 = end + 1;
            int i3 = lastSpecialIndex2 - 2;
            while (i3 >= start) {
                char ch3 = this.buf[i3];
                if (ch3 > '\r' && ch3 != c3 && ch3 != '\'') {
                    if (ch3 != '/' || !isEnabled(SerializerFeature.WriteSlashAsSpecial)) {
                        c = c3;
                        i3--;
                        c3 = c;
                    }
                }
                System.arraycopy(this.buf, i3 + 1, this.buf, i3 + 2, (end2 - i3) - 1);
                c = '\\';
                this.buf[i3] = '\\';
                this.buf[i3 + 1] = IOUtils.replaceChars[ch3];
                end2++;
                i3--;
                c3 = c;
            }
        }
        this.buf[this.count - 1] = '\'';
    }

    /* JADX INFO: Multiple debug info for r3v0 int: [D('newcount' int), D('len' int)] */
    /* access modifiers changed from: protected */
    public void writeStringWithSingleQuote(char[] chars) {
        char c;
        if (chars == null) {
            int newcount = this.count + 4;
            if (newcount > this.buf.length) {
                expandCapacity(newcount);
            }
            "null".getChars(0, 4, this.buf, this.count);
            this.count = newcount;
            return;
        }
        int len = chars.length;
        int newcount2 = this.count + len + 2;
        char c2 = '/';
        char c3 = '\\';
        if (newcount2 > this.buf.length) {
            if (this.writer != null) {
                write(39);
                for (char ch : chars) {
                    if (ch <= '\r' || ch == '\\' || ch == '\'' || (ch == '/' && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                        write(92);
                        write(IOUtils.replaceChars[ch]);
                    } else {
                        write(ch);
                    }
                }
                write(39);
                return;
            }
            expandCapacity(newcount2);
        }
        int start = this.count + 1;
        int end = start + len;
        this.buf[this.count] = '\'';
        System.arraycopy(chars, 0, this.buf, start, chars.length);
        this.count = newcount2;
        char lastSpecial = 0;
        int lastSpecialIndex = -1;
        int specialCount = 0;
        int i = start;
        while (i < end) {
            char ch2 = this.buf[i];
            if (ch2 <= '\r' || ch2 == '\\' || ch2 == '\'' || (ch2 == c2 && isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                specialCount++;
                lastSpecial = ch2;
                lastSpecialIndex = i;
            }
            i++;
            c2 = '/';
        }
        int newcount3 = newcount2 + specialCount;
        if (newcount3 > this.buf.length) {
            expandCapacity(newcount3);
        }
        this.count = newcount3;
        if (specialCount == 1) {
            System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, (end - lastSpecialIndex) - 1);
            this.buf[lastSpecialIndex] = '\\';
            this.buf[lastSpecialIndex + 1] = IOUtils.replaceChars[lastSpecial];
        } else if (specialCount > 1) {
            System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, (end - lastSpecialIndex) - 1);
            this.buf[lastSpecialIndex] = '\\';
            int lastSpecialIndex2 = lastSpecialIndex + 1;
            this.buf[lastSpecialIndex2] = IOUtils.replaceChars[lastSpecial];
            int end2 = end + 1;
            int i2 = lastSpecialIndex2 - 2;
            while (i2 >= start) {
                char ch3 = this.buf[i2];
                if (ch3 > '\r' && ch3 != c3 && ch3 != '\'') {
                    if (ch3 != '/' || !isEnabled(SerializerFeature.WriteSlashAsSpecial)) {
                        c = c3;
                        i2--;
                        c3 = c;
                    }
                }
                System.arraycopy(this.buf, i2 + 1, this.buf, i2 + 2, (end2 - i2) - 1);
                c = '\\';
                this.buf[i2] = '\\';
                this.buf[i2 + 1] = IOUtils.replaceChars[ch3];
                end2++;
                i2--;
                c3 = c;
            }
        }
        this.buf[this.count - 1] = '\'';
    }

    public void writeFieldName(String key) {
        writeFieldName(key, false);
    }

    public void writeFieldName(String key, boolean checkSpecial) {
        if (key == null) {
            write("null:");
        } else if (this.useSingleQuotes) {
            if (this.quoteFieldNames) {
                writeStringWithSingleQuote(key);
                write(58);
                return;
            }
            writeKeyWithSingleQuoteIfHasSpecial(key);
        } else if (this.quoteFieldNames) {
            writeStringWithDoubleQuote(key, ':');
        } else {
            boolean hashSpecial = key.length() == 0;
            int i = 0;
            while (true) {
                if (i >= key.length()) {
                    break;
                }
                char ch = key.charAt(i);
                if ((ch < '@' && (this.sepcialBits & (1 << ch)) != 0) || ch == '\\') {
                    hashSpecial = true;
                    break;
                }
                i++;
            }
            if (hashSpecial) {
                writeStringWithDoubleQuote(key, ':');
                return;
            }
            write(key);
            write(58);
        }
    }

    private void writeKeyWithSingleQuoteIfHasSpecial(String text) {
        char c;
        byte[] specicalFlags_singleQuotes = IOUtils.specicalFlags_singleQuotes;
        int len = text.length();
        int newcount = this.count + len + 1;
        char c2 = '\\';
        int i = 0;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(newcount);
            } else if (len == 0) {
                write(39);
                write(39);
                write(58);
                return;
            } else {
                boolean hasSpecial = false;
                int i2 = 0;
                while (true) {
                    if (i2 < len) {
                        char ch = text.charAt(i2);
                        if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) {
                            hasSpecial = true;
                            break;
                        }
                        i2++;
                    } else {
                        break;
                    }
                }
                if (hasSpecial) {
                    write(39);
                }
                while (i < len) {
                    char ch2 = text.charAt(i);
                    if (ch2 >= specicalFlags_singleQuotes.length || specicalFlags_singleQuotes[ch2] == 0) {
                        write(ch2);
                    } else {
                        write(92);
                        write(IOUtils.replaceChars[ch2]);
                    }
                    i++;
                }
                if (hasSpecial) {
                    write(39);
                }
                write(58);
                return;
            }
        }
        if (len == 0) {
            if (this.count + 3 > this.buf.length) {
                expandCapacity(this.count + 3);
            }
            char[] cArr = this.buf;
            int i3 = this.count;
            this.count = i3 + 1;
            cArr[i3] = '\'';
            char[] cArr2 = this.buf;
            int i4 = this.count;
            this.count = i4 + 1;
            cArr2[i4] = '\'';
            char[] cArr3 = this.buf;
            int i5 = this.count;
            this.count = i5 + 1;
            cArr3[i5] = ':';
            return;
        }
        int start = this.count;
        int end = start + len;
        text.getChars(0, len, this.buf, start);
        this.count = newcount;
        boolean hasSpecial2 = false;
        int newcount2 = newcount;
        int i6 = start;
        while (i6 < end) {
            char ch3 = this.buf[i6];
            if (ch3 >= specicalFlags_singleQuotes.length || specicalFlags_singleQuotes[ch3] == 0) {
                c = c2;
            } else if (!hasSpecial2) {
                newcount2 += 3;
                if (newcount2 > this.buf.length) {
                    expandCapacity(newcount2);
                }
                this.count = newcount2;
                System.arraycopy(this.buf, i6 + 1, this.buf, i6 + 3, (end - i6) - 1);
                System.arraycopy(this.buf, 0, this.buf, 1, i6);
                this.buf[start] = '\'';
                int i7 = i6 + 1;
                this.buf[i7] = '\\';
                i6 = i7 + 1;
                this.buf[i6] = IOUtils.replaceChars[ch3];
                end += 2;
                this.buf[this.count - 2] = '\'';
                hasSpecial2 = true;
                c = '\\';
            } else {
                newcount2++;
                if (newcount2 > this.buf.length) {
                    expandCapacity(newcount2);
                }
                this.count = newcount2;
                System.arraycopy(this.buf, i6 + 1, this.buf, i6 + 2, end - i6);
                c = '\\';
                this.buf[i6] = '\\';
                i6++;
                this.buf[i6] = IOUtils.replaceChars[ch3];
                end++;
            }
            i6++;
            c2 = c;
            i = 0;
        }
        this.buf[newcount2 - 1] = ':';
    }

    @Override // java.io.Writer, java.io.Flushable
    public void flush() {
        if (this.writer != null) {
            try {
                this.writer.write(this.buf, 0, this.count);
                this.writer.flush();
                this.count = 0;
            } catch (IOException e) {
                throw new JSONException(e.getMessage(), e);
            }
        }
    }
}
