package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.generators.OpenSSLPBEParametersGenerator;
import com.android.org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import com.android.org.bouncycastle.crypto.generators.PKCS5S1ParametersGenerator;
import com.android.org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public interface PBE {
    public static final int MD5 = 0;
    public static final int OPENSSL = 3;
    public static final int PKCS12 = 2;
    public static final int PKCS5S1 = 0;
    public static final int PKCS5S1_UTF8 = 4;
    public static final int PKCS5S2 = 1;
    public static final int PKCS5S2_UTF8 = 5;
    public static final int SHA1 = 1;
    public static final int SHA256 = 4;

    public static class Util {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public Util() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.convertPassword(int, javax.crypto.spec.PBEKeySpec):byte[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static byte[] convertPassword(int r1, javax.crypto.spec.PBEKeySpec r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.convertPassword(int, javax.crypto.spec.PBEKeySpec):byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.convertPassword(int, javax.crypto.spec.PBEKeySpec):byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey, java.security.spec.AlgorithmParameterSpec):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static com.android.org.bouncycastle.crypto.CipherParameters makePBEMacParameters(com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey r1, java.security.spec.AlgorithmParameterSpec r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey, java.security.spec.AlgorithmParameterSpec):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey, java.security.spec.AlgorithmParameterSpec):com.android.org.bouncycastle.crypto.CipherParameters");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(javax.crypto.SecretKey, int, int, int, javax.crypto.spec.PBEParameterSpec):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static com.android.org.bouncycastle.crypto.CipherParameters makePBEMacParameters(javax.crypto.SecretKey r1, int r2, int r3, int r4, javax.crypto.spec.PBEParameterSpec r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(javax.crypto.SecretKey, int, int, int, javax.crypto.spec.PBEParameterSpec):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(javax.crypto.SecretKey, int, int, int, javax.crypto.spec.PBEParameterSpec):com.android.org.bouncycastle.crypto.CipherParameters");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(javax.crypto.spec.PBEKeySpec, int, int, int):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static com.android.org.bouncycastle.crypto.CipherParameters makePBEMacParameters(javax.crypto.spec.PBEKeySpec r1, int r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(javax.crypto.spec.PBEKeySpec, int, int, int):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEMacParameters(javax.crypto.spec.PBEKeySpec, int, int, int):com.android.org.bouncycastle.crypto.CipherParameters");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey, java.security.spec.AlgorithmParameterSpec, java.lang.String):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static com.android.org.bouncycastle.crypto.CipherParameters makePBEParameters(com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey r1, java.security.spec.AlgorithmParameterSpec r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey, java.security.spec.AlgorithmParameterSpec, java.lang.String):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey, java.security.spec.AlgorithmParameterSpec, java.lang.String):com.android.org.bouncycastle.crypto.CipherParameters");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(javax.crypto.spec.PBEKeySpec, int, int, int, int):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static com.android.org.bouncycastle.crypto.CipherParameters makePBEParameters(javax.crypto.spec.PBEKeySpec r1, int r2, int r3, int r4, int r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(javax.crypto.spec.PBEKeySpec, int, int, int, int):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(javax.crypto.spec.PBEKeySpec, int, int, int, int):com.android.org.bouncycastle.crypto.CipherParameters");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(byte[], int, int, int, int, java.security.spec.AlgorithmParameterSpec, java.lang.String):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static com.android.org.bouncycastle.crypto.CipherParameters makePBEParameters(byte[] r1, int r2, int r3, int r4, int r5, java.security.spec.AlgorithmParameterSpec r6, java.lang.String r7) throws java.security.InvalidAlgorithmParameterException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(byte[], int, int, int, int, java.security.spec.AlgorithmParameterSpec, java.lang.String):com.android.org.bouncycastle.crypto.CipherParameters, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util.makePBEParameters(byte[], int, int, int, int, java.security.spec.AlgorithmParameterSpec, java.lang.String):com.android.org.bouncycastle.crypto.CipherParameters");
        }

        private static PBEParametersGenerator makePBEGenerator(int type, int hash) {
            if (type == 0 || type == 4) {
                switch (hash) {
                    case 0:
                        return new PKCS5S1ParametersGenerator(AndroidDigestFactory.getMD5());
                    case 1:
                        return new PKCS5S1ParametersGenerator(AndroidDigestFactory.getSHA1());
                    default:
                        throw new IllegalStateException("PKCS5 scheme 1 only supports MD2, MD5 and SHA1.");
                }
            } else if (type == 1 || type == 5) {
                switch (hash) {
                    case 0:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getMD5());
                    case 1:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA1());
                    case 4:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA256());
                    default:
                        throw new IllegalStateException("unknown digest scheme for PBE PKCS5S2 encryption.");
                }
            } else if (type != 2) {
                return new OpenSSLPBEParametersGenerator();
            } else {
                switch (hash) {
                    case 0:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getMD5());
                    case 1:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA1());
                    case 4:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA256());
                    default:
                        throw new IllegalStateException("unknown digest scheme for PBE encryption.");
                }
            }
        }
    }
}
