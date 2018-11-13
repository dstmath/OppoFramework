package com.android.org.conscrypt.ct;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CTConstants {
    public static final int CERTIFICATE_LENGTH_BYTES = 3;
    public static final int EXTENSIONS_LENGTH_BYTES = 2;
    public static final int HASH_ALGORITHM_LENGTH = 1;
    public static final int ISSUER_KEY_HASH_LENGTH = 32;
    public static final int LOGID_LENGTH = 32;
    public static final int LOG_ENTRY_TYPE_LENGTH = 2;
    public static final String OCSP_SCT_LIST_OID = "1.3.6.1.4.1.11129.2.4.5";
    public static final int SCT_LIST_LENGTH_BYTES = 2;
    public static final int SERIALIZED_SCT_LENGTH_BYTES = 2;
    public static final int SIGNATURE_ALGORITHM_LENGTH = 1;
    public static final int SIGNATURE_LENGTH_BYTES = 2;
    public static final int SIGNATURE_TYPE_LENGTH = 1;
    public static final int TIMESTAMP_LENGTH = 8;
    public static final int VERSION_LENGTH = 1;
    public static final String X509_SCT_LIST_OID = "1.3.6.1.4.1.11129.2.4.2";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.ct.CTConstants.<init>():void, dex: 
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
    public CTConstants() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.ct.CTConstants.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.ct.CTConstants.<init>():void");
    }
}
