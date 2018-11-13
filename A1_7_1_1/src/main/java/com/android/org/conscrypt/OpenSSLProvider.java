package com.android.org.conscrypt;

import java.security.Provider;

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
public final class OpenSSLProvider extends Provider {
    private static final String PREFIX = null;
    public static final String PROVIDER_NAME = "AndroidOpenSSL";
    private static final String STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.ECPrivateKey";
    private static final String STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.RSAPrivateKey";
    private static final String STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.RSAPublicKey";
    private static final long serialVersionUID = 2996752495318905136L;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLProvider.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLProvider.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLProvider.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 0 in method: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void, dex:  in method: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 0 in method: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: 0
        	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public OpenSSLProvider(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: 0 in method: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void, dex:  in method: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLProvider.<init>(java.lang.String):void");
    }

    public OpenSSLProvider() {
        this(PROVIDER_NAME);
    }

    private void putMacImplClass(String algorithm, String className) {
        putImplClassWithKeyConstraints("Mac." + algorithm, PREFIX + className, PREFIX + "OpenSSLKeyHolder", "RAW");
    }

    private void putSymmetricCipherImplClass(String transformation, String className) {
        putImplClassWithKeyConstraints("Cipher." + transformation, PREFIX + className, null, "RAW");
    }

    private void putRSACipherImplClass(String transformation, String className) {
        putImplClassWithKeyConstraints("Cipher." + transformation, PREFIX + className, PREFIX + "OpenSSLRSAPrivateKey" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + PREFIX + "OpenSSLRSAPublicKey" + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, null);
    }

    private void putSignatureImplClass(String algorithm, String className) {
        putImplClassWithKeyConstraints("Signature." + algorithm, PREFIX + className, PREFIX + "OpenSSLKeyHolder" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, "PKCS#8|X.509");
    }

    private void putRAWRSASignatureImplClass(String className) {
        putImplClassWithKeyConstraints("Signature.NONEwithRSA", PREFIX + className, PREFIX + "OpenSSLRSAPrivateKey" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + PREFIX + "OpenSSLRSAPublicKey" + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, null);
    }

    private void putECDHKeyAgreementImplClass(String className) {
        putImplClassWithKeyConstraints("KeyAgreement.ECDH", PREFIX + className, PREFIX + "OpenSSLKeyHolder" + "|" + STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME, "PKCS#8");
    }

    private void putImplClassWithKeyConstraints(String typeAndAlgName, String fullyQualifiedClassName, String supportedKeyClasses, String supportedKeyFormats) {
        put(typeAndAlgName, fullyQualifiedClassName);
        if (supportedKeyClasses != null) {
            put(typeAndAlgName + " SupportedKeyClasses", supportedKeyClasses);
        }
        if (supportedKeyFormats != null) {
            put(typeAndAlgName + " SupportedKeyFormats", supportedKeyFormats);
        }
    }
}
