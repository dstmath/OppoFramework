package libcore.icu;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import libcore.util.NativeAllocationRegistry;

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
public final class NativeConverter {
    private static final NativeAllocationRegistry registry = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: libcore.icu.NativeConverter.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: libcore.icu.NativeConverter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.NativeConverter.<clinit>():void");
    }

    public static native Charset charsetForName(String str);

    public static native void closeConverter(long j);

    public static native boolean contains(String str, String str2);

    public static native int decode(long j, byte[] bArr, int i, char[] cArr, int i2, int[] iArr, boolean z);

    public static native int encode(long j, char[] cArr, int i, byte[] bArr, int i2, int[] iArr, boolean z);

    public static native String[] getAvailableCharsetNames();

    public static native float getAveBytesPerChar(long j);

    public static native float getAveCharsPerByte(long j);

    public static native int getMaxBytesPerChar(long j);

    public static native int getMinBytesPerChar(long j);

    public static native long getNativeFinalizer();

    public static native long getNativeSize();

    public static native byte[] getSubstitutionBytes(long j);

    public static native long openConverter(String str);

    public static native void resetByteToChar(long j);

    public static native void resetCharToByte(long j);

    private static native void setCallbackDecode(long j, int i, int i2, String str);

    private static native void setCallbackEncode(long j, int i, int i2, byte[] bArr);

    public static void registerConverter(Object referrent, long converterHandle) {
        registry.registerNativeAllocation(referrent, converterHandle);
    }

    private static int translateCodingErrorAction(CodingErrorAction action) {
        if (action == CodingErrorAction.REPORT) {
            return 0;
        }
        if (action == CodingErrorAction.IGNORE) {
            return 1;
        }
        if (action == CodingErrorAction.REPLACE) {
            return 2;
        }
        throw new AssertionError();
    }

    public static void setCallbackDecode(long converterHandle, CharsetDecoder decoder) {
        setCallbackDecode(converterHandle, translateCodingErrorAction(decoder.malformedInputAction()), translateCodingErrorAction(decoder.unmappableCharacterAction()), decoder.replacement());
    }

    public static void setCallbackEncode(long converterHandle, CharsetEncoder encoder) {
        setCallbackEncode(converterHandle, translateCodingErrorAction(encoder.malformedInputAction()), translateCodingErrorAction(encoder.unmappableCharacterAction()), encoder.replacement());
    }
}
