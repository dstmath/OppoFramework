package com.android.org.conscrypt;

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
public final class NativeConstants {
    public static final int EVP_PKEY_EC = 408;
    public static final int EVP_PKEY_RSA = 6;
    public static final int EXFLAG_CA = 16;
    public static final int EXFLAG_CRITICAL = 512;
    public static final boolean HAS_EVP_AEAD = true;
    public static final int OPENSSL_EC_NAMED_CURVE = 0;
    public static final int POINT_CONVERSION_COMPRESSED = 2;
    public static final int POINT_CONVERSION_UNCOMPRESSED = 4;
    public static final int RSA_NO_PADDING = 3;
    public static final int RSA_PKCS1_OAEP_PADDING = 4;
    public static final int RSA_PKCS1_PADDING = 1;
    public static final int RSA_PKCS1_PSS_PADDING = 6;
    public static final int SSL3_RT_MAX_PACKET_SIZE = 16709;
    public static final int SSL_CB_ACCEPT_EXIT = 8194;
    public static final int SSL_CB_ACCEPT_LOOP = 8193;
    public static final int SSL_CB_ALERT = 16384;
    public static final int SSL_CB_CONNECT_EXIT = 4098;
    public static final int SSL_CB_CONNECT_LOOP = 4097;
    public static final int SSL_CB_EXIT = 2;
    public static final int SSL_CB_HANDSHAKE_DONE = 32;
    public static final int SSL_CB_HANDSHAKE_START = 16;
    public static final int SSL_CB_LOOP = 1;
    public static final int SSL_CB_READ = 4;
    public static final int SSL_CB_READ_ALERT = 16388;
    public static final int SSL_CB_WRITE = 8;
    public static final int SSL_CB_WRITE_ALERT = 16392;
    public static final int SSL_MODE_CBC_RECORD_SPLITTING = 256;
    public static final int SSL_MODE_HANDSHAKE_CUTTHROUGH = 128;
    public static final int SSL_MODE_SEND_FALLBACK_SCSV = 1024;
    public static final int SSL_OP_CIPHER_SERVER_PREFERENCE = 4194304;
    public static final int SSL_OP_NO_SESSION_RESUMPTION_ON_RENEGOTIATION = 0;
    public static final int SSL_OP_NO_SSLv3 = 33554432;
    public static final int SSL_OP_NO_TICKET = 16384;
    public static final int SSL_OP_NO_TLSv1 = 67108864;
    public static final int SSL_OP_NO_TLSv1_1 = 268435456;
    public static final int SSL_OP_NO_TLSv1_2 = 134217728;
    public static final int SSL_RECEIVED_SHUTDOWN = 2;
    public static final int SSL_SENT_SHUTDOWN = 1;
    public static final int SSL_ST_ACCEPT = 8192;
    public static final int SSL_ST_CONNECT = 4096;
    public static final int SSL_ST_INIT = 12288;
    public static final int SSL_ST_MASK = 4095;
    public static final int SSL_ST_OK = 3;
    public static final int SSL_ST_RENEGOTIATE = 12292;
    public static final int SSL_VERIFY_FAIL_IF_NO_PEER_CERT = 2;
    public static final int SSL_VERIFY_NONE = 0;
    public static final int SSL_VERIFY_PEER = 1;
    public static final int TLS_CT_ECDSA_FIXED_ECDH = 66;
    public static final int TLS_CT_ECDSA_SIGN = 64;
    public static final int TLS_CT_RSA_FIXED_DH = 3;
    public static final int TLS_CT_RSA_FIXED_ECDH = 65;
    public static final int TLS_CT_RSA_SIGN = 1;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.NativeConstants.<init>():void, dex: 
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
    public NativeConstants() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.NativeConstants.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.NativeConstants.<init>():void");
    }
}
