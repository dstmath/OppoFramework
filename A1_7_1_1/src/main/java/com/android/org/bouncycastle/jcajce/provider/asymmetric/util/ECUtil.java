package com.android.org.bouncycastle.jcajce.provider.asymmetric.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.nist.NISTNamedCurves;
import com.android.org.bouncycastle.asn1.sec.SECNamedCurves;
import com.android.org.bouncycastle.asn1.x9.X962NamedCurves;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.crypto.ec.CustomNamedCurves;

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
public class ECUtil {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.<init>():void, dex: 
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
    public ECUtil() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.generatePrivateKeyParameter(java.security.PrivateKey):com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter, dex: 
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
    public static com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter generatePrivateKeyParameter(java.security.PrivateKey r1) throws java.security.InvalidKeyException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.generatePrivateKeyParameter(java.security.PrivateKey):com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.generatePrivateKeyParameter(java.security.PrivateKey):com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.generatePublicKeyParameter(java.security.PublicKey):com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter, dex: 
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
    public static com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter generatePublicKeyParameter(java.security.PublicKey r1) throws java.security.InvalidKeyException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.generatePublicKeyParameter(java.security.PublicKey):com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.generatePublicKeyParameter(java.security.PublicKey):com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getNamedCurveOid(com.android.org.bouncycastle.jce.spec.ECParameterSpec):com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, dex: 
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
    public static com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier getNamedCurveOid(com.android.org.bouncycastle.jce.spec.ECParameterSpec r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getNamedCurveOid(com.android.org.bouncycastle.jce.spec.ECParameterSpec):com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getNamedCurveOid(com.android.org.bouncycastle.jce.spec.ECParameterSpec):com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getNamedCurveOid(java.lang.String):com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, dex: 
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
    public static com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier getNamedCurveOid(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getNamedCurveOid(java.lang.String):com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getNamedCurveOid(java.lang.String):com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getOrderBitLength(java.math.BigInteger, java.math.BigInteger):int, dex: 
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
    public static int getOrderBitLength(java.math.BigInteger r1, java.math.BigInteger r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getOrderBitLength(java.math.BigInteger, java.math.BigInteger):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil.getOrderBitLength(java.math.BigInteger, java.math.BigInteger):int");
    }

    static int[] convertMidTerms(int[] k) {
        int[] res = new int[3];
        if (k.length == 1) {
            res[0] = k[0];
        } else if (k.length != 3) {
            throw new IllegalArgumentException("Only Trinomials and pentanomials supported");
        } else if (k[0] < k[1] && k[0] < k[2]) {
            res[0] = k[0];
            if (k[1] < k[2]) {
                res[1] = k[1];
                res[2] = k[2];
            } else {
                res[1] = k[2];
                res[2] = k[1];
            }
        } else if (k[1] < k[2]) {
            res[0] = k[1];
            if (k[0] < k[2]) {
                res[1] = k[0];
                res[2] = k[2];
            } else {
                res[1] = k[2];
                res[2] = k[0];
            }
        } else {
            res[0] = k[2];
            if (k[0] < k[1]) {
                res[1] = k[0];
                res[2] = k[1];
            } else {
                res[1] = k[1];
                res[2] = k[0];
            }
        }
        return res;
    }

    private static ASN1ObjectIdentifier lookupOidByName(String name) {
        ASN1ObjectIdentifier oid = X962NamedCurves.getOID(name);
        if (oid != null) {
            return oid;
        }
        oid = SECNamedCurves.getOID(name);
        if (oid == null) {
            return NISTNamedCurves.getOID(name);
        }
        return oid;
    }

    public static X9ECParameters getNamedCurveByOid(ASN1ObjectIdentifier oid) {
        X9ECParameters params = CustomNamedCurves.getByOID(oid);
        if (params != null) {
            return params;
        }
        params = X962NamedCurves.getByOID(oid);
        if (params == null) {
            params = SECNamedCurves.getByOID(oid);
        }
        if (params == null) {
            return NISTNamedCurves.getByOID(oid);
        }
        return params;
    }

    public static X9ECParameters getNamedCurveByName(String curveName) {
        X9ECParameters params = CustomNamedCurves.getByName(curveName);
        if (params != null) {
            return params;
        }
        params = X962NamedCurves.getByName(curveName);
        if (params == null) {
            params = SECNamedCurves.getByName(curveName);
        }
        if (params == null) {
            return NISTNamedCurves.getByName(curveName);
        }
        return params;
    }

    public static String getCurveName(ASN1ObjectIdentifier oid) {
        String name = X962NamedCurves.getName(oid);
        if (name != null) {
            return name;
        }
        name = SECNamedCurves.getName(oid);
        if (name == null) {
            return NISTNamedCurves.getName(oid);
        }
        return name;
    }
}
