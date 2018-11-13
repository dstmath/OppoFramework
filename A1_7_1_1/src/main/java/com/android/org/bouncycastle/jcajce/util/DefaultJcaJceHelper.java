package com.android.org.bouncycastle.jcajce.util;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

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
public class DefaultJcaJceHelper implements JcaJceHelper {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jcajce.util.DefaultJcaJceHelper.<init>():void, dex: 
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
    public DefaultJcaJceHelper() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jcajce.util.DefaultJcaJceHelper.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.util.DefaultJcaJceHelper.<init>():void");
    }

    public Cipher createCipher(String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(algorithm);
    }

    public Mac createMac(String algorithm) throws NoSuchAlgorithmException {
        return Mac.getInstance(algorithm);
    }

    public KeyAgreement createKeyAgreement(String algorithm) throws NoSuchAlgorithmException {
        return KeyAgreement.getInstance(algorithm);
    }

    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(String algorithm) throws NoSuchAlgorithmException {
        return AlgorithmParameterGenerator.getInstance(algorithm);
    }

    public AlgorithmParameters createAlgorithmParameters(String algorithm) throws NoSuchAlgorithmException {
        return AlgorithmParameters.getInstance(algorithm);
    }

    public KeyGenerator createKeyGenerator(String algorithm) throws NoSuchAlgorithmException {
        return KeyGenerator.getInstance(algorithm);
    }

    public KeyFactory createKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(algorithm);
    }

    public SecretKeyFactory createSecretKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        return SecretKeyFactory.getInstance(algorithm);
    }

    public KeyPairGenerator createKeyPairGenerator(String algorithm) throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance(algorithm);
    }

    public MessageDigest createDigest(String algorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm);
    }

    public Signature createSignature(String algorithm) throws NoSuchAlgorithmException {
        return Signature.getInstance(algorithm);
    }

    public CertificateFactory createCertificateFactory(String algorithm) throws CertificateException {
        return CertificateFactory.getInstance(algorithm);
    }
}
