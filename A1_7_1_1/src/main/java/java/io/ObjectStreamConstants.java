package java.io;

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
public interface ObjectStreamConstants {
    public static final int PROTOCOL_VERSION_1 = 1;
    public static final int PROTOCOL_VERSION_2 = 2;
    public static final byte SC_BLOCK_DATA = (byte) 8;
    public static final byte SC_ENUM = (byte) 16;
    public static final byte SC_EXTERNALIZABLE = (byte) 4;
    public static final byte SC_SERIALIZABLE = (byte) 2;
    public static final byte SC_WRITE_METHOD = (byte) 1;
    public static final short STREAM_MAGIC = (short) -21267;
    public static final short STREAM_VERSION = (short) 5;
    public static final SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION = null;
    public static final SerializablePermission SUBSTITUTION_PERMISSION = null;
    public static final byte TC_ARRAY = (byte) 117;
    public static final byte TC_BASE = (byte) 112;
    public static final byte TC_BLOCKDATA = (byte) 119;
    public static final byte TC_BLOCKDATALONG = (byte) 122;
    public static final byte TC_CLASS = (byte) 118;
    public static final byte TC_CLASSDESC = (byte) 114;
    public static final byte TC_ENDBLOCKDATA = (byte) 120;
    public static final byte TC_ENUM = (byte) 126;
    public static final byte TC_EXCEPTION = (byte) 123;
    public static final byte TC_LONGSTRING = (byte) 124;
    public static final byte TC_MAX = (byte) 126;
    public static final byte TC_NULL = (byte) 112;
    public static final byte TC_OBJECT = (byte) 115;
    public static final byte TC_PROXYCLASSDESC = (byte) 125;
    public static final byte TC_REFERENCE = (byte) 113;
    public static final byte TC_RESET = (byte) 121;
    public static final byte TC_STRING = (byte) 116;
    public static final int baseWireHandle = 8257536;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.ObjectStreamConstants.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.ObjectStreamConstants.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectStreamConstants.<clinit>():void");
    }
}
