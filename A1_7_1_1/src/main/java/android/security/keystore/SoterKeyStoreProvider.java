package android.security.keystore;

import android.security.KeyStore;
import java.security.KeyPair;
import java.security.Provider;
import java.security.UnrecoverableKeyException;

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
public class SoterKeyStoreProvider extends Provider {
    private static final String ANDROID_PACKAGE_NAME = "android.security.keystore";
    public static final String PROVIDER_NAME = "SoterKeyStore";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.<init>():void, dex: 
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
    public SoterKeyStoreProvider() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.getAndroidKeyStorePrivateKey(android.security.keystore.AndroidKeyStorePublicKey):android.security.keystore.AndroidKeyStorePrivateKey, dex: 
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
    public static android.security.keystore.AndroidKeyStorePrivateKey getAndroidKeyStorePrivateKey(android.security.keystore.AndroidKeyStorePublicKey r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.getAndroidKeyStorePrivateKey(android.security.keystore.AndroidKeyStorePublicKey):android.security.keystore.AndroidKeyStorePrivateKey, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.getAndroidKeyStorePrivateKey(android.security.keystore.AndroidKeyStorePublicKey):android.security.keystore.AndroidKeyStorePrivateKey");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.getAndroidKeyStorePublicKey(java.lang.String, java.lang.String, byte[]):android.security.keystore.AndroidKeyStorePublicKey, dex: 
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
    public static android.security.keystore.AndroidKeyStorePublicKey getAndroidKeyStorePublicKey(java.lang.String r1, java.lang.String r2, byte[] r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.getAndroidKeyStorePublicKey(java.lang.String, java.lang.String, byte[]):android.security.keystore.AndroidKeyStorePublicKey, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.getAndroidKeyStorePublicKey(java.lang.String, java.lang.String, byte[]):android.security.keystore.AndroidKeyStorePublicKey");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.getJsonPublicKey(java.lang.String, java.lang.String, byte[]):android.security.keystore.AndroidKeyStorePublicKey, dex: 
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
    public static android.security.keystore.AndroidKeyStorePublicKey getJsonPublicKey(java.lang.String r1, java.lang.String r2, byte[] r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.getJsonPublicKey(java.lang.String, java.lang.String, byte[]):android.security.keystore.AndroidKeyStorePublicKey, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.getJsonPublicKey(java.lang.String, java.lang.String, byte[]):android.security.keystore.AndroidKeyStorePublicKey");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.SoterKeyStoreProvider.install():void, dex: 
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
    public static void install() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.SoterKeyStoreProvider.install():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.install():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePrivateKey, dex: 
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
    public static android.security.keystore.AndroidKeyStorePrivateKey loadAndroidKeyStorePrivateKeyFromKeystore(android.security.KeyStore r1, java.lang.String r2) throws java.security.UnrecoverableKeyException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePrivateKey, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePrivateKey");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.loadAndroidKeyStorePublicKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePublicKey, dex: 
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
    public static android.security.keystore.AndroidKeyStorePublicKey loadAndroidKeyStorePublicKeyFromKeystore(android.security.KeyStore r1, java.lang.String r2) throws java.security.UnrecoverableKeyException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.loadAndroidKeyStorePublicKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePublicKey, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.loadAndroidKeyStorePublicKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePublicKey");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.loadJsonPublicKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePublicKey, dex: 
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
    public static android.security.keystore.AndroidKeyStorePublicKey loadJsonPublicKeyFromKeystore(android.security.KeyStore r1, java.lang.String r2) throws java.security.UnrecoverableKeyException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.loadJsonPublicKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePublicKey, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.loadJsonPublicKeyFromKeystore(android.security.KeyStore, java.lang.String):android.security.keystore.AndroidKeyStorePublicKey");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.putKeyFactoryImpl(java.lang.String):void, dex: 
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
    private void putKeyFactoryImpl(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterKeyStoreProvider.putKeyFactoryImpl(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreProvider.putKeyFactoryImpl(java.lang.String):void");
    }

    public static KeyPair loadAndroidKeyStoreKeyPairFromKeystore(KeyStore keyStore, String privateKeyAlias) throws UnrecoverableKeyException {
        AndroidKeyStorePublicKey publicKey = loadAndroidKeyStorePublicKeyFromKeystore(keyStore, privateKeyAlias);
        return new KeyPair(publicKey, getAndroidKeyStorePrivateKey(publicKey));
    }
}
