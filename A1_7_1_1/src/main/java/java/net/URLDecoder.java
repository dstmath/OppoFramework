package java.net;

import java.io.UnsupportedEncodingException;

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
public class URLDecoder {
    static String dfltEncName;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.net.URLDecoder.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.net.URLDecoder.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.URLDecoder.<clinit>():void");
    }

    @Deprecated
    public static String decode(String s) {
        String str = null;
        try {
            return decode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String decode(String s, String enc) throws UnsupportedEncodingException {
        int i;
        boolean needToChange = false;
        int numChars = s.length();
        if (numChars > 500) {
            i = numChars / 2;
        } else {
            i = numChars;
        }
        StringBuffer sb = new StringBuffer(i);
        int i2 = 0;
        if (enc.length() == 0) {
            throw new UnsupportedEncodingException("URLDecoder: empty string enc parameter");
        }
        byte[] bArr = null;
        while (i2 < numChars) {
            char c = s.charAt(i2);
            switch (c) {
                case '%':
                    int pos;
                    if (bArr == null) {
                        try {
                            bArr = new byte[((numChars - i2) / 3)];
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                        }
                    }
                    int pos2 = 0;
                    while (true) {
                        pos = pos2;
                        if (i2 + 2 >= numChars || c != '%') {
                            if (i2 < numChars || c != '%') {
                                sb.append(new String(bArr, 0, pos, enc));
                                needToChange = true;
                                break;
                            }
                            throw new IllegalArgumentException("URLDecoder: Incomplete trailing escape (%) pattern");
                        } else if (isValidHexChar(s.charAt(i2 + 1)) && isValidHexChar(s.charAt(i2 + 2))) {
                            int v = Integer.parseInt(s.substring(i2 + 1, i2 + 3), 16);
                            if (v < 0) {
                                throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - negative value : " + s.substring(i2, i2 + 3));
                            }
                            pos2 = pos + 1;
                            bArr[pos] = (byte) v;
                            i2 += 3;
                            if (i2 < numChars) {
                                c = s.charAt(i2);
                            }
                        } else {
                            throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern : " + s.substring(i2, i2 + 3));
                        }
                    }
                    if (i2 < numChars) {
                    }
                    sb.append(new String(bArr, 0, pos, enc));
                    needToChange = true;
                    break;
                case '+':
                    sb.append(' ');
                    i2++;
                    needToChange = true;
                    break;
                default:
                    sb.append(c);
                    i2++;
                    break;
            }
        }
        return needToChange ? sb.toString() : s;
    }

    private static boolean isValidHexChar(char c) {
        if ('0' <= c && c <= '9') {
            return true;
        }
        if ('a' > c || c > 'f') {
            return 'A' <= c && c <= 'F';
        } else {
            return true;
        }
    }
}
