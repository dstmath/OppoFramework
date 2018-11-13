package com.mediatek.gba;

import java.util.Hashtable;

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
class GbaCipherSuite {
    static final byte[] CODE_SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = null;
    static final byte[] CODE_SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_SSL_DHE_DSS_WITH_DES_CBC_SHA = null;
    static final byte[] CODE_SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = null;
    static final byte[] CODE_SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_SSL_DHE_RSA_WITH_DES_CBC_SHA = null;
    static final byte[] CODE_SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA = null;
    static final byte[] CODE_SSL_DH_anon_EXPORT_WITH_RC4_40_MD5 = null;
    static final byte[] CODE_SSL_DH_anon_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_SSL_DH_anon_WITH_DES_CBC_SHA = null;
    static final byte[] CODE_SSL_DH_anon_WITH_RC4_128_MD5 = null;
    static final byte[] CODE_SSL_NULL_WITH_NULL_NULL = null;
    static final byte[] CODE_SSL_RSA_EXPORT_WITH_DES40_CBC_SHA = null;
    static final byte[] CODE_SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = null;
    static final byte[] CODE_SSL_RSA_EXPORT_WITH_RC4_40_MD5 = null;
    static final byte[] CODE_SSL_RSA_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_SSL_RSA_WITH_DES_CBC_SHA = null;
    static final byte[] CODE_SSL_RSA_WITH_NULL_MD5 = null;
    static final byte[] CODE_SSL_RSA_WITH_NULL_SHA = null;
    static final byte[] CODE_SSL_RSA_WITH_RC4_128_MD5 = null;
    static final byte[] CODE_SSL_RSA_WITH_RC4_128_SHA = null;
    static final byte[] CODE_TLS_DHE_DSS_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_DHE_DSS_WITH_AES_256_CBC_SHA = null;
    static final byte[] CODE_TLS_DHE_RSA_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_DHE_RSA_WITH_AES_256_CBC_SHA = null;
    static final byte[] CODE_TLS_DH_anon_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_DH_anon_WITH_AES_256_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_NULL_SHA = null;
    static final byte[] CODE_TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = null;
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_NULL_SHA = null;
    static final byte[] CODE_TLS_ECDHE_RSA_WITH_RC4_128_SHA = null;
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_NULL_SHA = null;
    static final byte[] CODE_TLS_ECDH_ECDSA_WITH_RC4_128_SHA = null;
    static final byte[] CODE_TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_RSA_WITH_NULL_SHA = null;
    static final byte[] CODE_TLS_ECDH_RSA_WITH_RC4_128_SHA = null;
    static final byte[] CODE_TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_anon_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_anon_WITH_AES_256_CBC_SHA = null;
    static final byte[] CODE_TLS_ECDH_anon_WITH_NULL_SHA = null;
    static final byte[] CODE_TLS_ECDH_anon_WITH_RC4_128_SHA = null;
    static final byte[] CODE_TLS_RSA_WITH_AES_128_CBC_SHA = null;
    static final byte[] CODE_TLS_RSA_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = null;
    static final GbaCipherSuite SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite SSL_DHE_DSS_WITH_DES_CBC_SHA = null;
    static final GbaCipherSuite SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = null;
    static final GbaCipherSuite SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite SSL_DHE_RSA_WITH_DES_CBC_SHA = null;
    static final GbaCipherSuite SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA = null;
    static final GbaCipherSuite SSL_DH_anon_EXPORT_WITH_RC4_40_MD5 = null;
    static final GbaCipherSuite SSL_DH_anon_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite SSL_DH_anon_WITH_DES_CBC_SHA = null;
    static final GbaCipherSuite SSL_DH_anon_WITH_RC4_128_MD5 = null;
    static final GbaCipherSuite SSL_NULL_WITH_NULL_NULL = null;
    static final GbaCipherSuite SSL_RSA_EXPORT_WITH_DES40_CBC_SHA = null;
    static final GbaCipherSuite SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = null;
    static final GbaCipherSuite SSL_RSA_EXPORT_WITH_RC4_40_MD5 = null;
    static final GbaCipherSuite SSL_RSA_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite SSL_RSA_WITH_DES_CBC_SHA = null;
    static final GbaCipherSuite SSL_RSA_WITH_NULL_MD5 = null;
    static final GbaCipherSuite SSL_RSA_WITH_NULL_SHA = null;
    static final GbaCipherSuite SSL_RSA_WITH_RC4_128_MD5 = null;
    static final GbaCipherSuite SSL_RSA_WITH_RC4_128_SHA = null;
    private static final GbaCipherSuite[] SUITES_BY_CODE_0x00 = null;
    private static final GbaCipherSuite[] SUITES_BY_CODE_0xc0 = null;
    static final GbaCipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_NULL_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_NULL_SHA = null;
    static final GbaCipherSuite TLS_ECDHE_RSA_WITH_RC4_128_SHA = null;
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_NULL_SHA = null;
    static final GbaCipherSuite TLS_ECDH_ECDSA_WITH_RC4_128_SHA = null;
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_NULL_SHA = null;
    static final GbaCipherSuite TLS_ECDH_RSA_WITH_RC4_128_SHA = null;
    static final GbaCipherSuite TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_anon_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_anon_WITH_AES_256_CBC_SHA = null;
    static final GbaCipherSuite TLS_ECDH_anon_WITH_NULL_SHA = null;
    static final GbaCipherSuite TLS_ECDH_anon_WITH_RC4_128_SHA = null;
    static final GbaCipherSuite TLS_RSA_WITH_AES_128_CBC_SHA = null;
    static final GbaCipherSuite TLS_RSA_WITH_AES_256_CBC_SHA = null;
    private static final Hashtable<String, GbaCipherSuite> mSuiteByName = null;
    private final byte[] mCipherSuiteCode;
    private final String mCipherSuiteName;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.gba.GbaCipherSuite.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.gba.GbaCipherSuite.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaCipherSuite.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.gba.GbaCipherSuite.<init>(java.lang.String, byte[]):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private GbaCipherSuite(java.lang.String r1, byte[] r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.gba.GbaCipherSuite.<init>(java.lang.String, byte[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaCipherSuite.<init>(java.lang.String, byte[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.gba.GbaCipherSuite.getByName(java.lang.String):com.mediatek.gba.GbaCipherSuite, dex: 
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
    public static com.mediatek.gba.GbaCipherSuite getByName(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.gba.GbaCipherSuite.getByName(java.lang.String):com.mediatek.gba.GbaCipherSuite, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaCipherSuite.getByName(java.lang.String):com.mediatek.gba.GbaCipherSuite");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.gba.GbaCipherSuite.registerCipherSuitesByCode(com.mediatek.gba.GbaCipherSuite[]):int, dex: 
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
    private static int registerCipherSuitesByCode(com.mediatek.gba.GbaCipherSuite[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.gba.GbaCipherSuite.registerCipherSuitesByCode(com.mediatek.gba.GbaCipherSuite[]):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaCipherSuite.registerCipherSuitesByCode(com.mediatek.gba.GbaCipherSuite[]):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.gba.GbaCipherSuite.getCode():byte[], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public byte[] getCode() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.gba.GbaCipherSuite.getCode():byte[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaCipherSuite.getCode():byte[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.gba.GbaCipherSuite.getName():java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public java.lang.String getName() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.gba.GbaCipherSuite.getName():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaCipherSuite.getName():java.lang.String");
    }
}
