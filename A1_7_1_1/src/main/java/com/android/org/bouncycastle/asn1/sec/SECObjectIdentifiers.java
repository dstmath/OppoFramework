package com.android.org.bouncycastle.asn1.sec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;

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
public interface SECObjectIdentifiers {
    public static final ASN1ObjectIdentifier dhSinglePass_cofactorDH_sha224kdf_scheme = null;
    public static final ASN1ObjectIdentifier dhSinglePass_cofactorDH_sha256kdf_scheme = null;
    public static final ASN1ObjectIdentifier dhSinglePass_cofactorDH_sha384kdf_scheme = null;
    public static final ASN1ObjectIdentifier dhSinglePass_cofactorDH_sha512kdf_scheme = null;
    public static final ASN1ObjectIdentifier dhSinglePass_stdDH_sha224kdf_scheme = null;
    public static final ASN1ObjectIdentifier dhSinglePass_stdDH_sha256kdf_scheme = null;
    public static final ASN1ObjectIdentifier dhSinglePass_stdDH_sha384kdf_scheme = null;
    public static final ASN1ObjectIdentifier dhSinglePass_stdDH_sha512kdf_scheme = null;
    public static final ASN1ObjectIdentifier ellipticCurve = null;
    public static final ASN1ObjectIdentifier mqvFull_sha224kdf_scheme = null;
    public static final ASN1ObjectIdentifier mqvFull_sha256kdf_scheme = null;
    public static final ASN1ObjectIdentifier mqvFull_sha384kdf_scheme = null;
    public static final ASN1ObjectIdentifier mqvFull_sha512kdf_scheme = null;
    public static final ASN1ObjectIdentifier mqvSinglePass_sha224kdf_scheme = null;
    public static final ASN1ObjectIdentifier mqvSinglePass_sha256kdf_scheme = null;
    public static final ASN1ObjectIdentifier mqvSinglePass_sha384kdf_scheme = null;
    public static final ASN1ObjectIdentifier mqvSinglePass_sha512kdf_scheme = null;
    public static final ASN1ObjectIdentifier secg_scheme = null;
    public static final ASN1ObjectIdentifier secp112r1 = null;
    public static final ASN1ObjectIdentifier secp112r2 = null;
    public static final ASN1ObjectIdentifier secp128r1 = null;
    public static final ASN1ObjectIdentifier secp128r2 = null;
    public static final ASN1ObjectIdentifier secp160k1 = null;
    public static final ASN1ObjectIdentifier secp160r1 = null;
    public static final ASN1ObjectIdentifier secp160r2 = null;
    public static final ASN1ObjectIdentifier secp192k1 = null;
    public static final ASN1ObjectIdentifier secp192r1 = null;
    public static final ASN1ObjectIdentifier secp224k1 = null;
    public static final ASN1ObjectIdentifier secp224r1 = null;
    public static final ASN1ObjectIdentifier secp256k1 = null;
    public static final ASN1ObjectIdentifier secp256r1 = null;
    public static final ASN1ObjectIdentifier secp384r1 = null;
    public static final ASN1ObjectIdentifier secp521r1 = null;
    public static final ASN1ObjectIdentifier sect113r1 = null;
    public static final ASN1ObjectIdentifier sect113r2 = null;
    public static final ASN1ObjectIdentifier sect131r1 = null;
    public static final ASN1ObjectIdentifier sect131r2 = null;
    public static final ASN1ObjectIdentifier sect163k1 = null;
    public static final ASN1ObjectIdentifier sect163r1 = null;
    public static final ASN1ObjectIdentifier sect163r2 = null;
    public static final ASN1ObjectIdentifier sect193r1 = null;
    public static final ASN1ObjectIdentifier sect193r2 = null;
    public static final ASN1ObjectIdentifier sect233k1 = null;
    public static final ASN1ObjectIdentifier sect233r1 = null;
    public static final ASN1ObjectIdentifier sect239k1 = null;
    public static final ASN1ObjectIdentifier sect283k1 = null;
    public static final ASN1ObjectIdentifier sect283r1 = null;
    public static final ASN1ObjectIdentifier sect409k1 = null;
    public static final ASN1ObjectIdentifier sect409r1 = null;
    public static final ASN1ObjectIdentifier sect571k1 = null;
    public static final ASN1ObjectIdentifier sect571r1 = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.asn1.sec.SECObjectIdentifiers.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.asn1.sec.SECObjectIdentifiers.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.sec.SECObjectIdentifiers.<clinit>():void");
    }
}
