package android.security.keystore;

import android.security.KeyStore;
import android.security.keystore.KeyProperties.Digest;
import java.math.BigInteger;
import java.security.KeyPairGeneratorSpi;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

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
public class SoterKeyStoreKeyPairRSAGeneratorSpi extends KeyPairGeneratorSpi {
    private static final int RSA_DEFAULT_KEY_SIZE = 2048;
    private static final int RSA_MAX_KEY_SIZE = 8192;
    private static final int RSA_MIN_KEY_SIZE = 512;
    private static volatile SecureRandom sRng;
    private boolean isAutoAddCounterWhenGetPublicKey;
    private boolean isAutoSignedWithAttkWhenGetPublicKey;
    private boolean isAutoSignedWithCommonkWhenGetPublicKey;
    private boolean isForSoter;
    private boolean isNeedNextAttk;
    private boolean isSecmsgFidCounterSignedWhenSign;
    private String mAutoSignedKeyNameWhenGetPublicKey;
    private boolean mEncryptionAtRestRequired;
    private String mEntryAlias;
    private String mJcaKeyAlgorithm;
    private int mKeySizeBits;
    private KeyStore mKeyStore;
    private int mKeymasterAlgorithm;
    private int[] mKeymasterBlockModes;
    private int[] mKeymasterDigests;
    private int[] mKeymasterEncryptionPaddings;
    private int[] mKeymasterPurposes;
    private int[] mKeymasterSignaturePaddings;
    private final int mOriginalKeymasterAlgorithm;
    private BigInteger mRSAPublicExponent;
    private SecureRandom mRng;
    private KeyGenParameterSpec mSpec;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public SoterKeyStoreKeyPairRSAGeneratorSpi() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.addAlgorithmSpecificParameters(android.security.keymaster.KeymasterArguments):void, dex: 
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
    private void addAlgorithmSpecificParameters(android.security.keymaster.KeymasterArguments r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.addAlgorithmSpecificParameters(android.security.keymaster.KeymasterArguments):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.addAlgorithmSpecificParameters(android.security.keymaster.KeymasterArguments):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.checkValidKeySize(int, int):void, dex: 
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
    private static void checkValidKeySize(int r1, int r2) throws java.security.InvalidAlgorithmParameterException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.checkValidKeySize(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.checkValidKeySize(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.constructKeyGenerationArguments():android.security.keymaster.KeymasterArguments, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private android.security.keymaster.KeymasterArguments constructKeyGenerationArguments() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.constructKeyGenerationArguments():android.security.keymaster.KeymasterArguments, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.constructKeyGenerationArguments():android.security.keymaster.KeymasterArguments");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateKeystoreKeyPair(java.lang.String, android.security.keymaster.KeymasterArguments, byte[], int):void, dex: 
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
    private void generateKeystoreKeyPair(java.lang.String r1, android.security.keymaster.KeymasterArguments r2, byte[] r3, int r4) throws java.security.ProviderException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateKeystoreKeyPair(java.lang.String, android.security.keymaster.KeymasterArguments, byte[], int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateKeystoreKeyPair(java.lang.String, android.security.keymaster.KeymasterArguments, byte[], int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificate(java.security.PrivateKey, java.security.PublicKey):java.security.cert.X509Certificate, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private java.security.cert.X509Certificate generateSelfSignedCertificate(java.security.PrivateKey r1, java.security.PublicKey r2) throws java.security.cert.CertificateParsingException, java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificate(java.security.PrivateKey, java.security.PublicKey):java.security.cert.X509Certificate, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificate(java.security.PrivateKey, java.security.PublicKey):java.security.cert.X509Certificate");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateBytes(java.security.KeyPair):byte[], dex: 
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
    private byte[] generateSelfSignedCertificateBytes(java.security.KeyPair r1) throws java.security.ProviderException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateBytes(java.security.KeyPair):byte[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateBytes(java.security.KeyPair):byte[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateWithFakeSignature(java.security.PublicKey):java.security.cert.X509Certificate, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private java.security.cert.X509Certificate generateSelfSignedCertificateWithFakeSignature(java.security.PublicKey r1) throws java.io.IOException, java.security.cert.CertificateParsingException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateWithFakeSignature(java.security.PublicKey):java.security.cert.X509Certificate, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateWithFakeSignature(java.security.PublicKey):java.security.cert.X509Certificate");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateWithValidSignature(java.security.PrivateKey, java.security.PublicKey, java.lang.String):java.security.cert.X509Certificate, dex: 
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
    private java.security.cert.X509Certificate generateSelfSignedCertificateWithValidSignature(java.security.PrivateKey r1, java.security.PublicKey r2, java.lang.String r3) throws java.lang.Exception {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateWithValidSignature(java.security.PrivateKey, java.security.PublicKey, java.lang.String):java.security.cert.X509Certificate, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateSelfSignedCertificateWithValidSignature(java.security.PrivateKey, java.security.PublicKey, java.lang.String):java.security.cert.X509Certificate");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.getCertificateSignatureAlgorithm(int, int, android.security.keystore.KeyGenParameterSpec):java.lang.String, dex: 
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
    private static java.lang.String getCertificateSignatureAlgorithm(int r1, int r2, android.security.keystore.KeyGenParameterSpec r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.getCertificateSignatureAlgorithm(int, int, android.security.keystore.KeyGenParameterSpec):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.getCertificateSignatureAlgorithm(int, int, android.security.keystore.KeyGenParameterSpec):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.getRealKeyBlobByKeyName(java.lang.String):byte[], dex: 
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
    private byte[] getRealKeyBlobByKeyName(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.getRealKeyBlobByKeyName(java.lang.String):byte[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.getRealKeyBlobByKeyName(java.lang.String):byte[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initAlgorithmSpecificParameters():void, dex: 
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
    private void initAlgorithmSpecificParameters() throws java.security.InvalidAlgorithmParameterException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initAlgorithmSpecificParameters():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initAlgorithmSpecificParameters():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.loadKeystoreKeyPair(java.lang.String):java.security.KeyPair, dex: 
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
    private java.security.KeyPair loadKeystoreKeyPair(java.lang.String r1) throws java.security.ProviderException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.loadKeystoreKeyPair(java.lang.String):java.security.KeyPair, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.loadKeystoreKeyPair(java.lang.String):java.security.KeyPair");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.resetAll():void, dex: 
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
    private void resetAll() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.resetAll():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.resetAll():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.storeCertificate(java.lang.String, byte[], int, java.lang.String):void, dex: 
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
    private void storeCertificate(java.lang.String r1, byte[] r2, int r3, java.lang.String r4) throws java.security.ProviderException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.storeCertificate(java.lang.String, byte[], int, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.storeCertificate(java.lang.String, byte[], int, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateKeyPair():java.security.KeyPair, dex: 
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
    public java.security.KeyPair generateKeyPair() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateKeyPair():java.security.KeyPair, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.generateKeyPair():java.security.KeyPair");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initialize(int, java.security.SecureRandom):void, dex: 
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
    public void initialize(int r1, java.security.SecureRandom r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initialize(int, java.security.SecureRandom):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initialize(int, java.security.SecureRandom):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void, dex: 
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
    public void initialize(java.security.spec.AlgorithmParameterSpec r1, java.security.SecureRandom r2) throws java.security.InvalidAlgorithmParameterException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void");
    }

    private static int getDefaultKeySize(int keymasterAlgorithm) {
        return 2048;
    }

    private static String[] getSupportedEcdsaSignatureDigests() {
        return new String[]{KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, "SHA-256", KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512};
    }

    private static Set<Integer> getAvailableKeymasterSignatureDigests(String[] authorizedKeyDigests, String[] supportedSignatureDigests) {
        int i = 0;
        Set<Integer> authorizedKeymasterKeyDigests = new HashSet();
        for (int keymasterDigest : Digest.allToKeymaster(authorizedKeyDigests)) {
            authorizedKeymasterKeyDigests.add(Integer.valueOf(keymasterDigest));
        }
        Set<Integer> supportedKeymasterSignatureDigests = new HashSet();
        int[] allToKeymaster = Digest.allToKeymaster(supportedSignatureDigests);
        int length = allToKeymaster.length;
        while (i < length) {
            supportedKeymasterSignatureDigests.add(Integer.valueOf(allToKeymaster[i]));
            i++;
        }
        Set<Integer> result = new HashSet(supportedKeymasterSignatureDigests);
        result.retainAll(authorizedKeymasterKeyDigests);
        return result;
    }
}
