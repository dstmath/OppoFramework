package java.sql;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
public class Types {
    public static final int ARRAY = 2003;
    public static final int BIGINT = -5;
    public static final int BINARY = -2;
    public static final int BIT = -7;
    public static final int BLOB = 2004;
    public static final int BOOLEAN = 16;
    public static final int CHAR = 1;
    public static final int CLOB = 2005;
    public static final int DATALINK = 70;
    public static final int DATE = 91;
    public static final int DECIMAL = 3;
    public static final int DISTINCT = 2001;
    public static final int DOUBLE = 8;
    public static final int FLOAT = 6;
    public static final int INTEGER = 4;
    public static final int JAVA_OBJECT = 2000;
    public static final int LONGNVARCHAR = -16;
    public static final int LONGVARBINARY = -4;
    public static final int LONGVARCHAR = -1;
    public static final int NCHAR = -15;
    public static final int NCLOB = 2011;
    public static final int NULL = 0;
    public static final int NUMERIC = 2;
    public static final int NVARCHAR = -9;
    public static final int OTHER = 1111;
    public static final int REAL = 7;
    public static final int REF = 2006;
    public static final int ROWID = -8;
    public static final int SMALLINT = 5;
    public static final int SQLXML = 2009;
    public static final int STRUCT = 2002;
    public static final int TIME = 92;
    public static final int TIMESTAMP = 93;
    public static final int TINYINT = -6;
    public static final int VARBINARY = -3;
    public static final int VARCHAR = 12;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.sql.Types.<init>():void, dex: 
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
    private Types() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.sql.Types.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.sql.Types.<init>():void");
    }
}
