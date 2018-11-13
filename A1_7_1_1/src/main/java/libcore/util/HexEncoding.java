package libcore.util;

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
public class HexEncoding {
    private static final char[] HEX_DIGITS = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: libcore.util.HexEncoding.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: libcore.util.HexEncoding.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.util.HexEncoding.<clinit>():void");
    }

    private HexEncoding() {
    }

    public static char[] encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    public static char[] encode(byte[] data, int offset, int len) {
        char[] result = new char[(len * 2)];
        for (int i = 0; i < len; i++) {
            byte b = data[offset + i];
            int resultIndex = i * 2;
            result[resultIndex] = HEX_DIGITS[(b >>> 4) & 15];
            result[resultIndex + 1] = HEX_DIGITS[b & 15];
        }
        return result;
    }

    public static byte[] decode(char[] encoded, boolean allowSingleChar) throws IllegalArgumentException {
        byte[] result = new byte[((encoded.length + 1) / 2)];
        int resultOffset = 0;
        int i = 0;
        if (allowSingleChar) {
            if (encoded.length % 2 != 0) {
                resultOffset = 1;
                result[0] = (byte) toDigit(encoded, 0);
                i = 1;
            }
        } else if (encoded.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid input length: " + encoded.length);
        }
        int len = encoded.length;
        int resultOffset2 = resultOffset;
        while (i < len) {
            resultOffset = resultOffset2 + 1;
            result[resultOffset2] = (byte) ((toDigit(encoded, i) << 4) | toDigit(encoded, i + 1));
            i += 2;
            resultOffset2 = resultOffset;
        }
        return result;
    }

    private static int toDigit(char[] str, int offset) throws IllegalArgumentException {
        int pseudoCodePoint = str[offset];
        if (48 <= pseudoCodePoint && pseudoCodePoint <= 57) {
            return pseudoCodePoint - 48;
        }
        if (97 <= pseudoCodePoint && pseudoCodePoint <= 102) {
            return (pseudoCodePoint - 97) + 10;
        }
        if (65 <= pseudoCodePoint && pseudoCodePoint <= 70) {
            return (pseudoCodePoint - 65) + 10;
        }
        throw new IllegalArgumentException("Illegal char: " + str[offset] + " at offset " + offset);
    }
}
