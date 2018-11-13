package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EVP_CIPHER_CTX;
import com.android.org.conscrypt.util.EmptyArray;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class OpenSSLCipher extends CipherSpi {
    private int blockSize;
    protected byte[] encodedKey;
    private boolean encrypting;
    protected byte[] iv;
    protected Mode mode;
    private Padding padding;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static abstract class EVP_AEAD extends OpenSSLCipher {
        private static final int DEFAULT_TAG_SIZE_BITS = 128;
        private static int lastGlobalMessageSize;
        private byte[] aad;
        protected byte[] buf;
        protected int bufCount;
        protected long evpAead;
        private int tagLengthInBytes;

        public static abstract class AES extends EVP_AEAD {
            private static final int AES_BLOCK_SIZE = 16;

            public static class GCM extends AES {
                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.<init>():void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                public GCM() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.<init>():void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.<init>():void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.checkSupportedMode(com.android.org.conscrypt.OpenSSLCipher$Mode):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                protected void checkSupportedMode(com.android.org.conscrypt.OpenSSLCipher.Mode r1) throws java.security.NoSuchAlgorithmException {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.checkSupportedMode(com.android.org.conscrypt.OpenSSLCipher$Mode):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.checkSupportedMode(com.android.org.conscrypt.OpenSSLCipher$Mode):void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.getEVP_AEAD(int):long, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                protected long getEVP_AEAD(int r1) throws java.security.InvalidKeyException {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.getEVP_AEAD(int):long, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.GCM.getEVP_AEAD(int):long");
                }
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            protected AES(com.android.org.conscrypt.OpenSSLCipher.Mode r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.checkSupportedKeySize(int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            protected void checkSupportedKeySize(int r1) throws java.security.InvalidKeyException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.checkSupportedKeySize(int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.AES.checkSupportedKeySize(int):void");
            }

            protected String getBaseCipherName() {
                return "AES";
            }

            protected int getCipherBlockSize() {
                return 16;
            }

            protected int getOutputSizeForUpdate(int inputLen) {
                return 0;
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode):void, dex: 
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
        public EVP_AEAD(com.android.org.conscrypt.OpenSSLCipher.Mode r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.expand(int):void, dex:  in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.expand(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.expand(int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private void expand(int r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.expand(int):void, dex:  in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.expand(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.expand(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.reset():void, dex:  in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.reset():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.reset():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private void reset() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.reset():void, dex:  in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.reset():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.reset():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.checkSupportedPadding(com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
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
        protected void checkSupportedPadding(com.android.org.conscrypt.OpenSSLCipher.Padding r1) throws javax.crypto.NoSuchPaddingException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.checkSupportedPadding(com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.checkSupportedPadding(com.android.org.conscrypt.OpenSSLCipher$Padding):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.doFinalInternal(byte[], int, int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected int doFinalInternal(byte[] r1, int r2, int r3) throws javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.doFinalInternal(byte[], int, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.doFinalInternal(byte[], int, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineGetParameters():java.security.AlgorithmParameters, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected java.security.AlgorithmParameters engineGetParameters() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineGetParameters():java.security.AlgorithmParameters, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineGetParameters():java.security.AlgorithmParameters");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineInitInternal(byte[], java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void, dex: 
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
        protected void engineInitInternal(byte[] r1, java.security.spec.AlgorithmParameterSpec r2, java.security.SecureRandom r3) throws java.security.InvalidKeyException, java.security.InvalidAlgorithmParameterException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineInitInternal(byte[], java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineInitInternal(byte[], java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineUpdateAAD(byte[], int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected void engineUpdateAAD(byte[] r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineUpdateAAD(byte[], int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.engineUpdateAAD(byte[], int, int):void");
        }

        protected abstract long getEVP_AEAD(int i) throws InvalidKeyException;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.getOutputSizeForFinal(int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected int getOutputSizeForFinal(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.getOutputSizeForFinal(int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.getOutputSizeForFinal(int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.updateInternal(byte[], int, int, byte[], int, int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected int updateInternal(byte[] r1, int r2, int r3, byte[] r4, int r5, int r6) throws javax.crypto.ShortBufferException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.updateInternal(byte[], int, int, byte[], int, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_AEAD.updateInternal(byte[], int, int, byte[], int, int):int");
        }
    }

    public static abstract class EVP_CIPHER extends OpenSSLCipher {
        protected boolean calledUpdate;
        private final EVP_CIPHER_CTX cipherCtx;
        private int modeBlockSize;

        public static class AES extends EVP_CIPHER {
            /* renamed from: -com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues */
            private static final /* synthetic */ int[] f0-com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues = null;
            /* renamed from: -com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues */
            private static final /* synthetic */ int[] f1-com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues = null;
            private static final int AES_BLOCK_SIZE = 16;

            public static class CBC extends AES {

                public static class NoPadding extends CBC {
                    /*  JADX ERROR: Method load error
                        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.CBC.NoPadding.<init>():void, dex: 
                        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                        	... 9 more
                        */
                    public NoPadding() {
                        /*
                        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.CBC.NoPadding.<init>():void, dex: 
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.CBC.NoPadding.<init>():void");
                    }
                }

                public static class PKCS5Padding extends CBC {
                    public PKCS5Padding() {
                        super(Padding.PKCS5PADDING);
                    }
                }

                public CBC(Padding padding) {
                    super(Mode.CBC, padding);
                }
            }

            public static class CTR extends AES {
                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.CTR.<init>():void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                public CTR() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.CTR.<init>():void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.CTR.<init>():void");
                }
            }

            public static class ECB extends AES {

                public static class NoPadding extends ECB {
                    /*  JADX ERROR: Method load error
                        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.NoPadding.<init>():void, dex: 
                        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                        	... 9 more
                        */
                    public NoPadding() {
                        /*
                        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.NoPadding.<init>():void, dex: 
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.NoPadding.<init>():void");
                    }
                }

                public static class PKCS5Padding extends ECB {
                    /*  JADX ERROR: Method load error
                        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.PKCS5Padding.<init>():void, dex: 
                        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                        	... 9 more
                        */
                    public PKCS5Padding() {
                        /*
                        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.PKCS5Padding.<init>():void, dex: 
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.PKCS5Padding.<init>():void");
                    }
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.<init>(com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                public ECB(com.android.org.conscrypt.OpenSSLCipher.Padding r1) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.<init>(com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.AES.ECB.<init>(com.android.org.conscrypt.OpenSSLCipher$Padding):void");
                }
            }

            /* renamed from: -getcom-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues */
            private static /* synthetic */ int[] m0-getcom-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues() {
                if (f0-com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues != null) {
                    return f0-com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues;
                }
                int[] iArr = new int[Mode.values().length];
                try {
                    iArr[Mode.CBC.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[Mode.CTR.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[Mode.ECB.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[Mode.GCM.ordinal()] = 6;
                } catch (NoSuchFieldError e4) {
                }
                f0-com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues = iArr;
                return iArr;
            }

            /* renamed from: -getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues */
            private static /* synthetic */ int[] m1x8f9012d2() {
                if (f1-com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues != null) {
                    return f1-com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues;
                }
                int[] iArr = new int[Padding.values().length];
                try {
                    iArr[Padding.ISO10126PADDING.ordinal()] = 6;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[Padding.NOPADDING.ordinal()] = 1;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[Padding.PKCS5PADDING.ordinal()] = 2;
                } catch (NoSuchFieldError e3) {
                }
                f1-com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues = iArr;
                return iArr;
            }

            protected AES(Mode mode, Padding padding) {
                super(mode, padding);
            }

            protected void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                switch (keyLength) {
                    case 16:
                    case 24:
                    case 32:
                        return;
                    default:
                        throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes");
                }
            }

            protected void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                switch (m0-getcom-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues()[mode.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                        return;
                    default:
                        throw new NoSuchAlgorithmException("Unsupported mode " + mode.toString());
                }
            }

            protected void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
                switch (m1x8f9012d2()[padding.ordinal()]) {
                    case 1:
                    case 2:
                        return;
                    default:
                        throw new NoSuchPaddingException("Unsupported padding " + padding.toString());
                }
            }

            protected String getBaseCipherName() {
                return "AES";
            }

            protected String getCipherName(int keyLength, Mode mode) {
                return "aes-" + (keyLength * 8) + "-" + mode.toString().toLowerCase(Locale.US);
            }

            protected int getCipherBlockSize() {
                return 16;
            }
        }

        public static class ARC4 extends EVP_CIPHER {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.ARC4.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public ARC4() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.ARC4.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.ARC4.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.ARC4.checkSupportedKeySize(int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            protected void checkSupportedKeySize(int r1) throws java.security.InvalidKeyException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.ARC4.checkSupportedKeySize(int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.ARC4.checkSupportedKeySize(int):void");
            }

            protected String getBaseCipherName() {
                return "ARCFOUR";
            }

            protected String getCipherName(int keySize, Mode mode) {
                return "rc4";
            }

            protected void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                throw new NoSuchAlgorithmException("ARC4 does not support modes");
            }

            protected void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
                throw new NoSuchPaddingException("ARC4 does not support padding");
            }

            protected int getCipherBlockSize() {
                return 0;
            }

            protected boolean supportsVariableSizeKey() {
                return true;
            }
        }

        /*  JADX ERROR: NullPointerException in pass: ReSugarCode
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
            	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public static class DESEDE extends EVP_CIPHER {
            /* renamed from: -com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues */
            private static final /* synthetic */ int[] f2-com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues = null;
            private static int DES_BLOCK_SIZE;

            public static class CBC extends DESEDE {

                public static class NoPadding extends CBC {
                    /*  JADX ERROR: Method load error
                        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.NoPadding.<init>():void, dex: 
                        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                        	... 9 more
                        */
                    public NoPadding() {
                        /*
                        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.NoPadding.<init>():void, dex: 
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.NoPadding.<init>():void");
                    }
                }

                public static class PKCS5Padding extends CBC {
                    /*  JADX ERROR: Method load error
                        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.PKCS5Padding.<init>():void, dex: 
                        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                        	... 9 more
                        */
                    public PKCS5Padding() {
                        /*
                        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.PKCS5Padding.<init>():void, dex: 
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.PKCS5Padding.<init>():void");
                    }
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.<init>(com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                public CBC(com.android.org.conscrypt.OpenSSLCipher.Padding r1) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.<init>(com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.CBC.<init>(com.android.org.conscrypt.OpenSSLCipher$Padding):void");
                }
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.-getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues():int[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            /* renamed from: -getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues */
            private static /* synthetic */ int[] m2x8f9012d2() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.-getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues():int[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.-getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues():int[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.<clinit>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            static {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.<clinit>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.<clinit>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode, com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public DESEDE(com.android.org.conscrypt.OpenSSLCipher.Mode r1, com.android.org.conscrypt.OpenSSLCipher.Padding r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode, com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.<init>(com.android.org.conscrypt.OpenSSLCipher$Mode, com.android.org.conscrypt.OpenSSLCipher$Padding):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedKeySize(int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            protected void checkSupportedKeySize(int r1) throws java.security.InvalidKeyException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedKeySize(int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedKeySize(int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedMode(com.android.org.conscrypt.OpenSSLCipher$Mode):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            protected void checkSupportedMode(com.android.org.conscrypt.OpenSSLCipher.Mode r1) throws java.security.NoSuchAlgorithmException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedMode(com.android.org.conscrypt.OpenSSLCipher$Mode):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedMode(com.android.org.conscrypt.OpenSSLCipher$Mode):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedPadding(com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            protected void checkSupportedPadding(com.android.org.conscrypt.OpenSSLCipher.Padding r1) throws javax.crypto.NoSuchPaddingException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedPadding(com.android.org.conscrypt.OpenSSLCipher$Padding):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.checkSupportedPadding(com.android.org.conscrypt.OpenSSLCipher$Padding):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.getCipherName(int, com.android.org.conscrypt.OpenSSLCipher$Mode):java.lang.String, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            protected java.lang.String getCipherName(int r1, com.android.org.conscrypt.OpenSSLCipher.Mode r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.getCipherName(int, com.android.org.conscrypt.OpenSSLCipher$Mode):java.lang.String, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.EVP_CIPHER.DESEDE.getCipherName(int, com.android.org.conscrypt.OpenSSLCipher$Mode):java.lang.String");
            }

            protected String getBaseCipherName() {
                return "DESede";
            }

            protected int getCipherBlockSize() {
                return DES_BLOCK_SIZE;
            }
        }

        protected abstract String getCipherName(int i, Mode mode);

        public EVP_CIPHER(Mode mode, Padding padding) {
            super(mode, padding);
            this.cipherCtx = new EVP_CIPHER_CTX(NativeCrypto.EVP_CIPHER_CTX_new());
        }

        protected void engineInitInternal(byte[] encodedKey, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            byte[] iv;
            if (params instanceof IvParameterSpec) {
                iv = ((IvParameterSpec) params).getIV();
            } else {
                iv = null;
            }
            long cipherType = NativeCrypto.EVP_get_cipherbyname(getCipherName(encodedKey.length, this.mode));
            if (cipherType == 0) {
                throw new InvalidAlgorithmParameterException("Cannot find name for key length = " + (encodedKey.length * 8) + " and mode = " + this.mode);
            }
            boolean encrypting = isEncrypting();
            int expectedIvLength = NativeCrypto.EVP_CIPHER_iv_length(cipherType);
            if (iv != null || expectedIvLength == 0) {
                if (expectedIvLength == 0 && iv != null) {
                    throw new InvalidAlgorithmParameterException("IV not used in " + this.mode + " mode");
                } else if (!(iv == null || iv.length == expectedIvLength)) {
                    throw new InvalidAlgorithmParameterException("expected IV length of " + expectedIvLength + " but was " + iv.length);
                }
            } else if (encrypting) {
                iv = new byte[expectedIvLength];
                if (random == null) {
                    random = new SecureRandom();
                }
                random.nextBytes(iv);
            } else {
                throw new InvalidAlgorithmParameterException("IV must be specified in " + this.mode + " mode");
            }
            this.iv = iv;
            if (supportsVariableSizeKey()) {
                NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, cipherType, null, null, encrypting);
                NativeCrypto.EVP_CIPHER_CTX_set_key_length(this.cipherCtx, encodedKey.length);
                NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, 0, encodedKey, iv, isEncrypting());
            } else {
                NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, cipherType, encodedKey, iv, encrypting);
            }
            NativeCrypto.EVP_CIPHER_CTX_set_padding(this.cipherCtx, getPadding() == Padding.PKCS5PADDING);
            this.modeBlockSize = NativeCrypto.EVP_CIPHER_CTX_block_size(this.cipherCtx);
            this.calledUpdate = false;
        }

        protected int updateInternal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset, int maximumLen) throws ShortBufferException {
            int intialOutputOffset = outputOffset;
            int bytesLeft = output.length - outputOffset;
            if (bytesLeft < maximumLen) {
                throw new ShortBufferException("output buffer too small during update: " + bytesLeft + " < " + maximumLen);
            }
            outputOffset += NativeCrypto.EVP_CipherUpdate(this.cipherCtx, output, outputOffset, input, inputOffset, inputLen);
            this.calledUpdate = true;
            return outputOffset - intialOutputOffset;
        }

        protected int doFinalInternal(byte[] output, int outputOffset, int maximumLen) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException {
            int initialOutputOffset = outputOffset;
            if (!isEncrypting() && !this.calledUpdate) {
                return 0;
            }
            int writtenBytes;
            int bytesLeft = output.length - outputOffset;
            if (bytesLeft >= maximumLen) {
                writtenBytes = NativeCrypto.EVP_CipherFinal_ex(this.cipherCtx, output, outputOffset);
            } else {
                byte[] lastBlock = new byte[maximumLen];
                writtenBytes = NativeCrypto.EVP_CipherFinal_ex(this.cipherCtx, lastBlock, 0);
                if (writtenBytes > bytesLeft) {
                    throw new ShortBufferException("buffer is too short: " + writtenBytes + " > " + bytesLeft);
                } else if (writtenBytes > 0) {
                    System.arraycopy(lastBlock, 0, output, outputOffset, writtenBytes);
                }
            }
            outputOffset += writtenBytes;
            reset();
            return outputOffset - initialOutputOffset;
        }

        protected int getOutputSizeForFinal(int inputLen) {
            int i = 0;
            if (this.modeBlockSize == 1) {
                return inputLen;
            }
            int buffered = NativeCrypto.get_EVP_CIPHER_CTX_buf_len(this.cipherCtx);
            if (getPadding() == Padding.NOPADDING) {
                return buffered + inputLen;
            }
            int i2;
            int i3 = inputLen + buffered;
            if (NativeCrypto.get_EVP_CIPHER_CTX_final_used(this.cipherCtx)) {
                i2 = this.modeBlockSize;
            } else {
                i2 = 0;
            }
            int totalLen = i3 + i2;
            if (totalLen % this.modeBlockSize != 0 || isEncrypting()) {
                i = this.modeBlockSize;
            }
            totalLen += i;
            return totalLen - (totalLen % this.modeBlockSize);
        }

        protected int getOutputSizeForUpdate(int inputLen) {
            return getOutputSizeForFinal(inputLen);
        }

        private void reset() {
            NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, 0, this.encodedKey, this.iv, isEncrypting());
            this.calledUpdate = false;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    protected enum Mode {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.Mode.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.Mode.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.Mode.<clinit>():void");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    protected enum Padding {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.Padding.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.OpenSSLCipher.Padding.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLCipher.Padding.<clinit>():void");
        }
    }

    protected abstract void checkSupportedKeySize(int i) throws InvalidKeyException;

    protected abstract void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException;

    protected abstract void checkSupportedPadding(Padding padding) throws NoSuchPaddingException;

    protected abstract int doFinalInternal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException;

    protected abstract void engineInitInternal(byte[] bArr, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract String getBaseCipherName();

    protected abstract int getCipherBlockSize();

    protected abstract int getOutputSizeForFinal(int i);

    protected abstract int getOutputSizeForUpdate(int i);

    protected abstract int updateInternal(byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4) throws ShortBufferException;

    protected OpenSSLCipher() {
        this.mode = Mode.ECB;
        this.padding = Padding.PKCS5PADDING;
    }

    protected OpenSSLCipher(Mode mode, Padding padding) {
        this.mode = Mode.ECB;
        this.padding = Padding.PKCS5PADDING;
        this.mode = mode;
        this.padding = padding;
        this.blockSize = getCipherBlockSize();
    }

    protected boolean supportsVariableSizeKey() {
        return false;
    }

    protected boolean supportsVariableSizeIv() {
        return false;
    }

    protected void engineSetMode(String modeStr) throws NoSuchAlgorithmException {
        try {
            Mode mode = Mode.valueOf(modeStr.toUpperCase(Locale.US));
            checkSupportedMode(mode);
            this.mode = mode;
        } catch (IllegalArgumentException e) {
            NoSuchAlgorithmException newE = new NoSuchAlgorithmException("No such mode: " + modeStr);
            newE.initCause(e);
            throw newE;
        }
    }

    protected void engineSetPadding(String paddingStr) throws NoSuchPaddingException {
        try {
            Padding padding = Padding.valueOf(paddingStr.toUpperCase(Locale.US));
            checkSupportedPadding(padding);
            this.padding = padding;
        } catch (IllegalArgumentException e) {
            NoSuchPaddingException newE = new NoSuchPaddingException("No such padding: " + paddingStr);
            newE.initCause(e);
            throw newE;
        }
    }

    protected Padding getPadding() {
        return this.padding;
    }

    protected int engineGetBlockSize() {
        return this.blockSize;
    }

    protected int engineGetOutputSize(int inputLen) {
        return getOutputSizeForFinal(inputLen);
    }

    protected byte[] engineGetIV() {
        return this.iv;
    }

    protected AlgorithmParameters engineGetParameters() {
        if (this.iv == null || this.iv.length <= 0) {
            return null;
        }
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance(getBaseCipherName());
            params.init(this.iv);
            return params;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (IOException e2) {
            return null;
        }
    }

    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        checkAndSetEncodedKey(opmode, key);
        try {
            engineInitInternal(this.encodedKey, null, random);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        checkAndSetEncodedKey(opmode, key);
        engineInitInternal(this.encodedKey, params, random);
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AlgorithmParameterSpec spec;
        if (params != null) {
            try {
                spec = params.getParameterSpec(IvParameterSpec.class);
            } catch (InvalidParameterSpecException e) {
                throw new InvalidAlgorithmParameterException("Params must be convertible to IvParameterSpec", e);
            }
        }
        spec = null;
        engineInit(opmode, key, spec, random);
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        byte[] output;
        int maximumLen = getOutputSizeForUpdate(inputLen);
        if (maximumLen > 0) {
            output = new byte[maximumLen];
        } else {
            output = EmptyArray.BYTE;
        }
        try {
            int bytesWritten = updateInternal(input, inputOffset, inputLen, output, 0, maximumLen);
            if (output.length == bytesWritten) {
                return output;
            }
            if (bytesWritten == 0) {
                return EmptyArray.BYTE;
            }
            return Arrays.copyOfRange(output, 0, bytesWritten);
        } catch (ShortBufferException e) {
            throw new RuntimeException("calculated buffer size was wrong: " + maximumLen);
        }
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        return updateInternal(input, inputOffset, inputLen, output, outputOffset, getOutputSizeForUpdate(inputLen));
    }

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        int bytesWritten;
        int maximumLen = getOutputSizeForFinal(inputLen);
        byte[] output = new byte[maximumLen];
        if (inputLen > 0) {
            try {
                bytesWritten = updateInternal(input, inputOffset, inputLen, output, 0, maximumLen);
            } catch (ShortBufferException e) {
                throw new RuntimeException("our calculated buffer was too small", e);
            }
        }
        bytesWritten = 0;
        try {
            bytesWritten += doFinalInternal(output, bytesWritten, maximumLen - bytesWritten);
            if (bytesWritten == output.length) {
                return output;
            }
            if (bytesWritten == 0) {
                return EmptyArray.BYTE;
            }
            return Arrays.copyOfRange(output, 0, bytesWritten);
        } catch (ShortBufferException e2) {
            throw new RuntimeException("our calculated buffer was too small", e2);
        }
    }

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (output == null) {
            throw new NullPointerException("output == null");
        }
        int bytesWritten;
        int maximumLen = getOutputSizeForFinal(inputLen);
        if (inputLen > 0) {
            bytesWritten = updateInternal(input, inputOffset, inputLen, output, outputOffset, maximumLen);
            outputOffset += bytesWritten;
            maximumLen -= bytesWritten;
        } else {
            bytesWritten = 0;
        }
        return doFinalInternal(output, outputOffset, maximumLen) + bytesWritten;
    }

    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        try {
            byte[] encoded = key.getEncoded();
            return engineDoFinal(encoded, 0, encoded.length);
        } catch (BadPaddingException e) {
            IllegalBlockSizeException newE = new IllegalBlockSizeException();
            newE.initCause(e);
            throw newE;
        }
    }

    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        try {
            byte[] encoded = engineDoFinal(wrappedKey, 0, wrappedKey.length);
            if (wrappedKeyType == 1) {
                return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePublic(new X509EncodedKeySpec(encoded));
            }
            if (wrappedKeyType == 2) {
                return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePrivate(new PKCS8EncodedKeySpec(encoded));
            }
            if (wrappedKeyType == 3) {
                return new SecretKeySpec(encoded, wrappedKeyAlgorithm);
            }
            throw new UnsupportedOperationException("wrappedKeyType == " + wrappedKeyType);
        } catch (IllegalBlockSizeException e) {
            throw new InvalidKeyException(e);
        } catch (BadPaddingException e2) {
            throw new InvalidKeyException(e2);
        } catch (InvalidKeySpecException e3) {
            throw new InvalidKeyException(e3);
        }
    }

    private byte[] checkAndSetEncodedKey(int opmode, Key key) throws InvalidKeyException {
        if (opmode == 1 || opmode == 3) {
            this.encrypting = true;
        } else if (opmode == 2 || opmode == 4) {
            this.encrypting = false;
        } else {
            throw new InvalidParameterException("Unsupported opmode " + opmode);
        }
        if (key instanceof SecretKey) {
            byte[] encodedKey = key.getEncoded();
            if (encodedKey == null) {
                throw new InvalidKeyException("key.getEncoded() == null");
            }
            checkSupportedKeySize(encodedKey.length);
            this.encodedKey = encodedKey;
            return encodedKey;
        }
        throw new InvalidKeyException("Only SecretKey is supported");
    }

    protected boolean isEncrypting() {
        return this.encrypting;
    }
}
