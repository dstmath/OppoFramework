package com.android.org.bouncycastle.jcajce.provider.asymmetric;

import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;

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
public class EC {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.asymmetric.ec.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("KeyAgreement.ECDH", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyAgreementSpi$DH");
            registerOid(provider, X9ObjectIdentifiers.id_ecPublicKey, "EC", new com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi.EC());
            registerOid(provider, X9ObjectIdentifiers.dhSinglePass_stdDH_sha1kdf_scheme, "EC", new com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi.EC());
            provider.addAlgorithm("KeyFactory.EC", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi$EC");
            provider.addAlgorithm("KeyPairGenerator.EC", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi$EC");
            provider.addAlgorithm("Signature.SHA1withECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA");
            provider.addAlgorithm("Signature.NONEwithECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSAnone");
            provider.addAlgorithm("Alg.Alias.Signature.ECDSA", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.ECDSAwithSHA1", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.SHA1WITHECDSA", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.ECDSAWITHSHA1", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.SHA1WithECDSA", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.ECDSAWithSHA1", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.1.2.840.10045.4.1", "SHA1withECDSA");
            addSignatureAlgorithm(provider, "SHA224", "ECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA224", X9ObjectIdentifiers.ecdsa_with_SHA224);
            addSignatureAlgorithm(provider, "SHA256", "ECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA256", X9ObjectIdentifiers.ecdsa_with_SHA256);
            addSignatureAlgorithm(provider, "SHA384", "ECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA384", X9ObjectIdentifiers.ecdsa_with_SHA384);
            addSignatureAlgorithm(provider, "SHA512", "ECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA512", X9ObjectIdentifiers.ecdsa_with_SHA512);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.EC.<init>():void, dex: 
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
    public EC() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.EC.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.EC.<init>():void");
    }
}
