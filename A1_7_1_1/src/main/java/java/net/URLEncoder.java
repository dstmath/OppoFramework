package java.net;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;

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
public class URLEncoder {
    static final int caseDiff = 32;
    static String dfltEncName;
    static BitSet dontNeedEncoding;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.URLEncoder.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.URLEncoder.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.URLEncoder.<clinit>():void");
    }

    private URLEncoder() {
    }

    @Deprecated
    public static String encode(String s) {
        String str = null;
        try {
            return encode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String encode(String s, String enc) throws UnsupportedEncodingException {
        boolean needToChange = false;
        StringBuffer out = new StringBuffer(s.length());
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        if (enc == null) {
            throw new NullPointerException("charsetName");
        }
        try {
            Charset charset = Charset.forName(enc);
            int i = 0;
            while (i < s.length()) {
                int c = s.charAt(i);
                if (dontNeedEncoding.get(c)) {
                    if (c == 32) {
                        c = 43;
                        needToChange = true;
                    }
                    out.append((char) c);
                    i++;
                } else {
                    BitSet bitSet;
                    do {
                        charArrayWriter.write(c);
                        if (c >= 55296 && c <= 56319 && i + 1 < s.length()) {
                            int d = s.charAt(i + 1);
                            if (d >= 56320 && d <= 57343) {
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                        i++;
                        if (i >= s.length()) {
                            break;
                        }
                        bitSet = dontNeedEncoding;
                        c = s.charAt(i);
                    } while (!bitSet.get(c));
                    charArrayWriter.flush();
                    byte[] ba = new String(charArrayWriter.toCharArray()).getBytes(charset);
                    for (int j = 0; j < ba.length; j++) {
                        out.append('%');
                        char ch = Character.forDigit((ba[j] >> 4) & 15, 16);
                        if (Character.isLetter(ch)) {
                            ch = (char) (ch - 32);
                        }
                        out.append(ch);
                        ch = Character.forDigit(ba[j] & 15, 16);
                        if (Character.isLetter(ch)) {
                            ch = (char) (ch - 32);
                        }
                        out.append(ch);
                    }
                    charArrayWriter.reset();
                    needToChange = true;
                }
            }
            return needToChange ? out.toString() : s;
        } catch (IllegalCharsetNameException e) {
            throw new UnsupportedEncodingException(enc);
        } catch (UnsupportedCharsetException e2) {
            throw new UnsupportedEncodingException(enc);
        }
    }
}
