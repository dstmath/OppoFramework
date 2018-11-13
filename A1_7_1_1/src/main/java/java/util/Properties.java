package java.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Types;
import sun.misc.DoubleConsts;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class Properties extends Hashtable<Object, Object> {
    private static final char[] hexDigit = null;
    private static final long serialVersionUID = 4112578634029874840L;
    protected Properties defaults;

    class LineReader {
        byte[] inByteBuf;
        char[] inCharBuf;
        int inLimit;
        int inOff;
        InputStream inStream;
        char[] lineBuf;
        Reader reader;

        public LineReader(InputStream inStream) {
            this.lineBuf = new char[1024];
            this.inLimit = 0;
            this.inOff = 0;
            this.inStream = inStream;
            this.inByteBuf = new byte[8192];
        }

        public LineReader(Reader reader) {
            this.lineBuf = new char[1024];
            this.inLimit = 0;
            this.inOff = 0;
            this.reader = reader;
            this.inCharBuf = new char[8192];
        }

        int readLine() throws IOException {
            int len = 0;
            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLF = false;
            while (true) {
                int read;
                char c;
                if (this.inOff >= this.inLimit) {
                    if (this.inStream == null) {
                        read = this.reader.read(this.inCharBuf);
                    } else {
                        read = this.inStream.read(this.inByteBuf);
                    }
                    this.inLimit = read;
                    this.inOff = 0;
                    if (this.inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        return len;
                    }
                }
                int i;
                if (this.inStream != null) {
                    byte[] bArr = this.inByteBuf;
                    i = this.inOff;
                    this.inOff = i + 1;
                    c = (char) (bArr[i] & 255);
                } else {
                    char[] cArr = this.inCharBuf;
                    i = this.inOff;
                    this.inOff = i + 1;
                    c = cArr[i];
                }
                if (skipLF) {
                    skipLF = false;
                    if (c == 10) {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (!Character.isWhitespace(c) && (appendedLineBegin || !(c == 13 || c == 10))) {
                        skipWhiteSpace = false;
                        appendedLineBegin = false;
                    }
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        isCommentLine = true;
                    }
                }
                if (c != 10 && c != 13) {
                    int len2 = len + 1;
                    this.lineBuf[len] = c;
                    if (len2 == this.lineBuf.length) {
                        int newLength = this.lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(this.lineBuf, 0, buf, 0, this.lineBuf.length);
                        this.lineBuf = buf;
                    }
                    if (c != '\\') {
                        precedingBackslash = false;
                    } else if (precedingBackslash) {
                        precedingBackslash = false;
                    } else {
                        precedingBackslash = true;
                    }
                    len = len2;
                } else if (isCommentLine || len == 0) {
                    isCommentLine = false;
                    isNewLine = true;
                    skipWhiteSpace = true;
                    len = 0;
                } else {
                    if (this.inOff >= this.inLimit) {
                        if (this.inStream == null) {
                            read = this.reader.read(this.inCharBuf);
                        } else {
                            read = this.inStream.read(this.inByteBuf);
                        }
                        this.inLimit = read;
                        this.inOff = 0;
                        if (this.inLimit <= 0) {
                            return len;
                        }
                    }
                    if (!precedingBackslash) {
                        return len;
                    }
                    len--;
                    skipWhiteSpace = true;
                    appendedLineBegin = true;
                    precedingBackslash = false;
                    if (c == 13) {
                        skipLF = true;
                    }
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.Properties.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.Properties.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.Properties.<clinit>():void");
    }

    public Properties() {
        this(null);
    }

    public Properties(Properties defaults) {
        this.defaults = defaults;
    }

    public synchronized Object setProperty(String key, String value) {
        return put(key, value);
    }

    public synchronized void load(Reader reader) throws IOException {
        load0(new LineReader(reader));
    }

    public synchronized void load(InputStream inStream) throws IOException {
        load0(new LineReader(inStream));
    }

    private void load0(LineReader lr) throws IOException {
        char[] convtBuf = new char[1024];
        while (true) {
            int limit = lr.readLine();
            if (limit >= 0) {
                char c;
                int keyLen = 0;
                int valueStart = limit;
                boolean hasSep = false;
                boolean precedingBackslash = false;
                while (keyLen < limit) {
                    c = lr.lineBuf[keyLen];
                    if ((c != '=' && c != ':') || precedingBackslash) {
                        if (Character.isWhitespace(c) && !precedingBackslash) {
                            valueStart = keyLen + 1;
                            break;
                        } else {
                            precedingBackslash = c == '\\' ? !precedingBackslash : false;
                            keyLen++;
                        }
                    } else {
                        valueStart = keyLen + 1;
                        hasSep = true;
                        break;
                    }
                }
                while (valueStart < limit) {
                    c = lr.lineBuf[valueStart];
                    if (!Character.isWhitespace(c)) {
                        if (hasSep || (c != '=' && c != ':')) {
                            break;
                        }
                        hasSep = true;
                    }
                    valueStart++;
                }
                put(loadConvert(lr.lineBuf, 0, keyLen, convtBuf), loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf));
            } else {
                return;
            }
        }
    }

    private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char[] out = convtBuf;
        int end = off + len;
        int outLen = 0;
        int off2 = off;
        while (off2 < end) {
            int outLen2;
            off = off2 + 1;
            char aChar = in[off2];
            if (aChar == '\\') {
                off2 = off + 1;
                aChar = in[off];
                if (aChar == 'u') {
                    int value = 0;
                    int i = 0;
                    while (i < 4) {
                        off = off2 + 1;
                        aChar = in[off2];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = ((value << 4) + aChar) - 48;
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case Types.DATALINK /*70*/:
                                value = (((value << 4) + 10) + aChar) - 65;
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (((value << 4) + 10) + aChar) - 97;
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                        i++;
                        off2 = off;
                    }
                    outLen2 = outLen + 1;
                    out[outLen] = (char) value;
                    off = off2;
                } else {
                    if (aChar == 't') {
                        aChar = 9;
                    } else if (aChar == 'r') {
                        aChar = 13;
                    } else if (aChar == 'n') {
                        aChar = 10;
                    } else if (aChar == 'f') {
                        aChar = 12;
                    }
                    outLen2 = outLen + 1;
                    out[outLen] = aChar;
                    off = off2;
                }
            } else {
                outLen2 = outLen + 1;
                out[outLen] = aChar;
            }
            outLen = outLen2;
            off2 = off;
        }
        return new String(out, 0, outLen);
    }

    private String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);
        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            if (aChar <= '=' || aChar >= 127) {
                switch (aChar) {
                    case 9:
                        outBuffer.append('\\');
                        outBuffer.append('t');
                        break;
                    case 10:
                        outBuffer.append('\\');
                        outBuffer.append('n');
                        break;
                    case 12:
                        outBuffer.append('\\');
                        outBuffer.append('f');
                        break;
                    case 13:
                        outBuffer.append('\\');
                        outBuffer.append('r');
                        break;
                    case ' ':
                        if (x == 0 || escapeSpace) {
                            outBuffer.append('\\');
                        }
                        outBuffer.append(' ');
                        break;
                    case '!':
                    case '#':
                    case ':':
                    case '=':
                        outBuffer.append('\\');
                        outBuffer.append(aChar);
                        break;
                    default:
                        int i;
                        if (aChar < ' ' || aChar > '~') {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        if ((i & escapeUnicode) == 0) {
                            outBuffer.append(aChar);
                            break;
                        }
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 15));
                        outBuffer.append(toHex((aChar >> 8) & 15));
                        outBuffer.append(toHex((aChar >> 4) & 15));
                        outBuffer.append(toHex(aChar & 15));
                        break;
                        break;
                }
            } else if (aChar == '\\') {
                outBuffer.append('\\');
                outBuffer.append('\\');
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    private static void writeComments(BufferedWriter bw, String comments) throws IOException {
        bw.write("#");
        int len = comments.length();
        int current = 0;
        int last = 0;
        char[] uu = new char[6];
        uu[0] = '\\';
        uu[1] = 'u';
        while (current < len) {
            char c = comments.charAt(current);
            if (c > 255 || c == 10 || c == 13) {
                if (last != current) {
                    bw.write(comments.substring(last, current));
                }
                if (c > 255) {
                    uu[2] = toHex((c >> 12) & 15);
                    uu[3] = toHex((c >> 8) & 15);
                    uu[4] = toHex((c >> 4) & 15);
                    uu[5] = toHex(c & 15);
                    bw.write(new String(uu));
                } else {
                    bw.newLine();
                    if (c == 13 && current != len - 1 && comments.charAt(current + 1) == 10) {
                        current++;
                    }
                    if (current == len - 1 || !(comments.charAt(current + 1) == '#' || comments.charAt(current + 1) == '!')) {
                        bw.write("#");
                    }
                }
                last = current + 1;
            }
            current++;
        }
        if (last != current) {
            bw.write(comments.substring(last, current));
        }
        bw.newLine();
    }

    @Deprecated
    public void save(OutputStream out, String comments) {
        try {
            store(out, comments);
        } catch (IOException e) {
        }
    }

    public void store(Writer writer, String comments) throws IOException {
        BufferedWriter writer2;
        if (writer instanceof BufferedWriter) {
            writer2 = (BufferedWriter) writer;
        } else {
            writer2 = new BufferedWriter(writer);
        }
        store0(writer2, comments, false);
    }

    public void store(OutputStream out, String comments) throws IOException {
        store0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")), comments, true);
    }

    private void store0(BufferedWriter bw, String comments, boolean escUnicode) throws IOException {
        if (comments != null) {
            writeComments(bw, comments);
        }
        bw.write("#" + new Date().toString());
        bw.newLine();
        synchronized (this) {
            Enumeration e = keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String val = (String) get(key);
                key = saveConvert(key, true, escUnicode);
                bw.write(key + "=" + saveConvert(val, false, escUnicode));
                bw.newLine();
            }
        }
        bw.flush();
    }

    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        if (in == null) {
            throw new NullPointerException();
        }
        XMLUtils.load(this, in);
        in.close();
    }

    public void storeToXML(OutputStream os, String comment) throws IOException {
        if (os == null) {
            throw new NullPointerException();
        }
        storeToXML(os, comment, "UTF-8");
    }

    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        if (os == null) {
            throw new NullPointerException();
        }
        XMLUtils.save(this, os, comment, encoding);
    }

    public String getProperty(String key) {
        String sval = null;
        Object oval = super.get(key);
        if (oval instanceof String) {
            sval = (String) oval;
        }
        return (sval != null || this.defaults == null) ? sval : this.defaults.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return val == null ? defaultValue : val;
    }

    public Enumeration<?> propertyNames() {
        Hashtable h = new Hashtable();
        enumerate(h);
        return h.keys();
    }

    public Set<String> stringPropertyNames() {
        Hashtable<String, String> h = new Hashtable();
        enumerateStringProperties(h);
        return h.keySet();
    }

    public void list(PrintStream out) {
        out.println("-- listing properties --");
        Hashtable h = new Hashtable();
        enumerate(h);
        Enumeration e = h.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String val = (String) h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        Hashtable h = new Hashtable();
        enumerate(h);
        Enumeration e = h.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String val = (String) h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    private synchronized void enumerate(Hashtable h) {
        if (this.defaults != null) {
            this.defaults.enumerate(h);
        }
        Enumeration e = keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            h.put(key, get(key));
        }
    }

    private synchronized void enumerateStringProperties(Hashtable<String, String> h) {
        if (this.defaults != null) {
            this.defaults.enumerateStringProperties(h);
        }
        Enumeration e = keys();
        while (e.hasMoreElements()) {
            Object k = e.nextElement();
            Object v = get(k);
            if ((k instanceof String) && (v instanceof String)) {
                h.put((String) k, (String) v);
            }
        }
    }

    private static char toHex(int nibble) {
        return hexDigit[nibble & 15];
    }
}
