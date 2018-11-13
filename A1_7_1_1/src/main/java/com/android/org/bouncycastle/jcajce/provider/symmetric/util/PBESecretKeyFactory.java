package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

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
public class PBESecretKeyFactory extends BaseSecretKeyFactory implements PBE {
    private int digest;
    private boolean forCipher;
    private int ivSize;
    private int keySize;
    private int scheme;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.<init>(java.lang.String, com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, boolean, int, int, int, int):void, dex:  in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.<init>(java.lang.String, com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, boolean, int, int, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.<init>(java.lang.String, com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, boolean, int, int, int, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public PBESecretKeyFactory(java.lang.String r1, com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier r2, boolean r3, int r4, int r5, int r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.<init>(java.lang.String, com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, boolean, int, int, int, int):void, dex:  in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.<init>(java.lang.String, com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, boolean, int, int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.<init>(java.lang.String, com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier, boolean, int, int, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.engineGenerateSecret(java.security.spec.KeySpec):javax.crypto.SecretKey, dex: 
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
    protected javax.crypto.SecretKey engineGenerateSecret(java.security.spec.KeySpec r1) throws java.security.spec.InvalidKeySpecException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.engineGenerateSecret(java.security.spec.KeySpec):javax.crypto.SecretKey, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory.engineGenerateSecret(java.security.spec.KeySpec):javax.crypto.SecretKey");
    }
}
