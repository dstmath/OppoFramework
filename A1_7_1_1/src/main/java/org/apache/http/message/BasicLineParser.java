package org.apache.http.message;

import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@Deprecated
public class BasicLineParser implements LineParser {
    public static final BasicLineParser DEFAULT = null;
    protected final ProtocolVersion protocol;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.http.message.BasicLineParser.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.http.message.BasicLineParser.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicLineParser.<clinit>():void");
    }

    public BasicLineParser(ProtocolVersion proto) {
        if (proto == null) {
            proto = HttpVersion.HTTP_1_1;
        }
        this.protocol = proto;
    }

    public BasicLineParser() {
        this(null);
    }

    public static final ProtocolVersion parseProtocolVersion(String value, LineParser parser) throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException("Value to parse may not be null.");
        }
        if (parser == null) {
            parser = DEFAULT;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        return parser.parseProtocolVersion(buffer, new ParserCursor(0, value.length()));
    }

    public ProtocolVersion parseProtocolVersion(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        } else {
            String protoname = this.protocol.getProtocol();
            int protolength = protoname.length();
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            skipWhitespace(buffer, cursor);
            int i = cursor.getPos();
            if ((i + protolength) + 4 > indexTo) {
                throw new ParseException("Not a valid protocol version: " + buffer.substring(indexFrom, indexTo));
            }
            boolean ok = true;
            int j = 0;
            while (ok && j < protolength) {
                ok = buffer.charAt(i + j) == protoname.charAt(j);
                j++;
            }
            if (ok) {
                ok = buffer.charAt(i + protolength) == '/';
            }
            if (ok) {
                i += protolength + 1;
                int period = buffer.indexOf(46, i, indexTo);
                if (period == -1) {
                    throw new ParseException("Invalid protocol version number: " + buffer.substring(indexFrom, indexTo));
                }
                try {
                    int major = Integer.parseInt(buffer.substringTrimmed(i, period));
                    i = period + 1;
                    int blank = buffer.indexOf(32, i, indexTo);
                    if (blank == -1) {
                        blank = indexTo;
                    }
                    try {
                        int minor = Integer.parseInt(buffer.substringTrimmed(i, blank));
                        cursor.updatePos(blank);
                        return createProtocolVersion(major, minor);
                    } catch (NumberFormatException e) {
                        throw new ParseException("Invalid protocol minor version number: " + buffer.substring(indexFrom, indexTo));
                    }
                } catch (NumberFormatException e2) {
                    throw new ParseException("Invalid protocol major version number: " + buffer.substring(indexFrom, indexTo));
                }
            }
            throw new ParseException("Not a valid protocol version: " + buffer.substring(indexFrom, indexTo));
        }
    }

    protected ProtocolVersion createProtocolVersion(int major, int minor) {
        return this.protocol.forVersion(major, minor);
    }

    public boolean hasProtocolVersion(CharArrayBuffer buffer, ParserCursor cursor) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        } else {
            int index = cursor.getPos();
            String protoname = this.protocol.getProtocol();
            int protolength = protoname.length();
            if (buffer.length() < protolength + 4) {
                return false;
            }
            if (index < 0) {
                index = (buffer.length() - 4) - protolength;
            } else if (index == 0) {
                while (index < buffer.length() && HTTP.isWhitespace(buffer.charAt(index))) {
                    index++;
                }
            }
            if ((index + protolength) + 4 > buffer.length()) {
                return false;
            }
            boolean ok = true;
            int j = 0;
            while (ok && j < protolength) {
                ok = buffer.charAt(index + j) == protoname.charAt(j);
                j++;
            }
            if (ok) {
                ok = buffer.charAt(index + protolength) == '/';
            }
            return ok;
        }
    }

    public static final RequestLine parseRequestLine(String value, LineParser parser) throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException("Value to parse may not be null.");
        }
        if (parser == null) {
            parser = DEFAULT;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        return parser.parseRequestLine(buffer, new ParserCursor(0, value.length()));
    }

    public RequestLine parseRequestLine(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        } else {
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            try {
                skipWhitespace(buffer, cursor);
                int i = cursor.getPos();
                int blank = buffer.indexOf(32, i, indexTo);
                if (blank < 0) {
                    throw new ParseException("Invalid request line: " + buffer.substring(indexFrom, indexTo));
                }
                String method = buffer.substringTrimmed(i, blank);
                cursor.updatePos(blank);
                skipWhitespace(buffer, cursor);
                i = cursor.getPos();
                blank = buffer.indexOf(32, i, indexTo);
                if (blank < 0) {
                    throw new ParseException("Invalid request line: " + buffer.substring(indexFrom, indexTo));
                }
                String uri = buffer.substringTrimmed(i, blank);
                cursor.updatePos(blank);
                ProtocolVersion ver = parseProtocolVersion(buffer, cursor);
                skipWhitespace(buffer, cursor);
                if (cursor.atEnd()) {
                    return createRequestLine(method, uri, ver);
                }
                throw new ParseException("Invalid request line: " + buffer.substring(indexFrom, indexTo));
            } catch (IndexOutOfBoundsException e) {
                throw new ParseException("Invalid request line: " + buffer.substring(indexFrom, indexTo));
            }
        }
    }

    protected RequestLine createRequestLine(String method, String uri, ProtocolVersion ver) {
        return new BasicRequestLine(method, uri, ver);
    }

    public static final StatusLine parseStatusLine(String value, LineParser parser) throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException("Value to parse may not be null.");
        }
        if (parser == null) {
            parser = DEFAULT;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        return parser.parseStatusLine(buffer, new ParserCursor(0, value.length()));
    }

    public StatusLine parseStatusLine(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        } else {
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            try {
                String reasonPhrase;
                ProtocolVersion ver = parseProtocolVersion(buffer, cursor);
                skipWhitespace(buffer, cursor);
                int i = cursor.getPos();
                int blank = buffer.indexOf(32, i, indexTo);
                if (blank < 0) {
                    blank = indexTo;
                }
                int statusCode = Integer.parseInt(buffer.substringTrimmed(i, blank));
                i = blank;
                if (i < indexTo) {
                    reasonPhrase = buffer.substringTrimmed(i, indexTo);
                } else {
                    reasonPhrase = "";
                }
                return createStatusLine(ver, statusCode, reasonPhrase);
            } catch (NumberFormatException e) {
                throw new ParseException("Unable to parse status code from status line: " + buffer.substring(indexFrom, indexTo));
            } catch (IndexOutOfBoundsException e2) {
                throw new ParseException("Invalid status line: " + buffer.substring(indexFrom, indexTo));
            }
        }
    }

    protected StatusLine createStatusLine(ProtocolVersion ver, int status, String reason) {
        return new BasicStatusLine(ver, status, reason);
    }

    public static final Header parseHeader(String value, LineParser parser) throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException("Value to parse may not be null");
        }
        if (parser == null) {
            parser = DEFAULT;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        return parser.parseHeader(buffer);
    }

    public Header parseHeader(CharArrayBuffer buffer) throws ParseException {
        return new BufferedHeader(buffer);
    }

    protected void skipWhitespace(CharArrayBuffer buffer, ParserCursor cursor) {
        int pos = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        while (pos < indexTo && HTTP.isWhitespace(buffer.charAt(pos))) {
            pos++;
        }
        cursor.updatePos(pos);
    }
}
