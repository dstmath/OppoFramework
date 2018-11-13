package android.security.keystore;

import android.os.IBinder;
import android.security.KeyStore;
import java.io.ByteArrayOutputStream;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    */
abstract class AndroidKeyStoreAuthenticatedAESCipherSpi extends AndroidKeyStoreCipherSpiBase {
    private static final int BLOCK_SIZE_BYTES = 16;
    private byte[] mIv;
    private boolean mIvHasBeenUsed;
    private final int mKeymasterBlockMode;
    private final int mKeymasterPadding;

    private static class AdditionalAuthenticationDataStream implements Stream {
        private final KeyStore mKeyStore;
        private final IBinder mOperationToken;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.<init>(android.security.KeyStore, android.os.IBinder):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private AdditionalAuthenticationDataStream(android.security.KeyStore r1, android.os.IBinder r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.<init>(android.security.KeyStore, android.os.IBinder):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.<init>(android.security.KeyStore, android.os.IBinder):void");
        }

        /* synthetic */ AdditionalAuthenticationDataStream(KeyStore keyStore, IBinder operationToken, AdditionalAuthenticationDataStream additionalAuthenticationDataStream) {
            this(keyStore, operationToken);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.finish(byte[], byte[]):android.security.keymaster.OperationResult, dex: 
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
        public android.security.keymaster.OperationResult finish(byte[] r1, byte[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.finish(byte[], byte[]):android.security.keymaster.OperationResult, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.finish(byte[], byte[]):android.security.keymaster.OperationResult");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.update(byte[]):android.security.keymaster.OperationResult, dex: 
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
        public android.security.keymaster.OperationResult update(byte[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.update(byte[]):android.security.keymaster.OperationResult, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.AdditionalAuthenticationDataStream.update(byte[]):android.security.keymaster.OperationResult");
        }
    }

    private static class BufferAllOutputUntilDoFinalStreamer implements KeyStoreCryptoOperationStreamer {
        private ByteArrayOutputStream mBufferedOutput;
        private final KeyStoreCryptoOperationStreamer mDelegate;
        private long mProducedOutputSizeBytes;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private BufferAllOutputUntilDoFinalStreamer(android.security.keystore.KeyStoreCryptoOperationStreamer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer):void");
        }

        /* synthetic */ BufferAllOutputUntilDoFinalStreamer(KeyStoreCryptoOperationStreamer delegate, BufferAllOutputUntilDoFinalStreamer bufferAllOutputUntilDoFinalStreamer) {
            this(delegate);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.doFinal(byte[], int, int, byte[], byte[]):byte[], dex: 
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
        public byte[] doFinal(byte[] r1, int r2, int r3, byte[] r4, byte[] r5) throws android.security.KeyStoreException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.doFinal(byte[], int, int, byte[], byte[]):byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.doFinal(byte[], int, int, byte[], byte[]):byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.getConsumedInputSizeBytes():long, dex: 
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
        public long getConsumedInputSizeBytes() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.getConsumedInputSizeBytes():long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.getConsumedInputSizeBytes():long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.getProducedOutputSizeBytes():long, dex: 
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
        public long getProducedOutputSizeBytes() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.getProducedOutputSizeBytes():long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.getProducedOutputSizeBytes():long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.update(byte[], int, int):byte[], dex: 
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
        public byte[] update(byte[] r1, int r2, int r3) throws android.security.KeyStoreException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.update(byte[], int, int):byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.BufferAllOutputUntilDoFinalStreamer.update(byte[], int, int):byte[]");
        }
    }

    static abstract class GCM extends AndroidKeyStoreAuthenticatedAESCipherSpi {
        private static final int DEFAULT_TAG_LENGTH_BITS = 128;
        private static final int IV_LENGTH_BYTES = 12;
        private static final int MAX_SUPPORTED_TAG_LENGTH_BITS = 128;
        static final int MIN_SUPPORTED_TAG_LENGTH_BITS = 96;
        private int mTagLengthBits;

        public static final class NoPadding extends GCM {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.<init>():void, dex: 
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
            public NoPadding() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.engineGetOutputSize(int):int, dex: 
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
            protected final int engineGetOutputSize(int r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.engineGetOutputSize(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.engineGetOutputSize(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.finalize():void, dex: 
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
            public /* bridge */ /* synthetic */ void finalize() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.finalize():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.NoPadding.finalize():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.<init>(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        GCM(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex:  in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex: 
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
        protected final void addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex:  in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.createMainDataStreamer(android.security.KeyStore, android.os.IBinder):android.security.keystore.KeyStoreCryptoOperationStreamer, dex: 
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
        protected android.security.keystore.KeyStoreCryptoOperationStreamer createMainDataStreamer(android.security.KeyStore r1, android.os.IBinder r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.createMainDataStreamer(android.security.KeyStore, android.os.IBinder):android.security.keystore.KeyStoreCryptoOperationStreamer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.createMainDataStreamer(android.security.KeyStore, android.os.IBinder):android.security.keystore.KeyStoreCryptoOperationStreamer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.engineGetParameters():java.security.AlgorithmParameters, dex: 
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
        protected final java.security.AlgorithmParameters engineGetParameters() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.engineGetParameters():java.security.AlgorithmParameters, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.engineGetParameters():java.security.AlgorithmParameters");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.getAdditionalEntropyAmountForBegin():int, dex: 
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
        protected final int getAdditionalEntropyAmountForBegin() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.getAdditionalEntropyAmountForBegin():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.getAdditionalEntropyAmountForBegin():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.getTagLengthBits():int, dex: 
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
        protected final int getTagLengthBits() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.getTagLengthBits():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.getTagLengthBits():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters():void, dex: 
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
        protected final void initAlgorithmSpecificParameters() throws java.security.InvalidKeyException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters(java.security.AlgorithmParameters):void, dex: 
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
        protected final void initAlgorithmSpecificParameters(java.security.AlgorithmParameters r1) throws java.security.InvalidAlgorithmParameterException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters(java.security.AlgorithmParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters(java.security.AlgorithmParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters(java.security.spec.AlgorithmParameterSpec):void, dex: 
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
        protected final void initAlgorithmSpecificParameters(java.security.spec.AlgorithmParameterSpec r1) throws java.security.InvalidAlgorithmParameterException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters(java.security.spec.AlgorithmParameterSpec):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.initAlgorithmSpecificParameters(java.security.spec.AlgorithmParameterSpec):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.resetAll():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected final void resetAll() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.resetAll():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.resetAll():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.resetWhilePreservingInitState():void, dex: 
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
        protected final void resetWhilePreservingInitState() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.resetWhilePreservingInitState():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.GCM.resetWhilePreservingInitState():void");
        }

        protected final KeyStoreCryptoOperationStreamer createAdditionalAuthenticationDataStreamer(KeyStore keyStore, IBinder operationToken) {
            return new KeyStoreCryptoOperationChunkedStreamer(new AdditionalAuthenticationDataStream(keyStore, operationToken, null));
        }

        protected final int getAdditionalEntropyAmountForFinish() {
            return 0;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.<init>(int, int):void, dex: 
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
    AndroidKeyStoreAuthenticatedAESCipherSpi(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.<init>(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.<init>(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex: 
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
    protected void addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.engineGetIV():byte[], dex: 
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
    protected final byte[] engineGetIV() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.engineGetIV():byte[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.engineGetIV():byte[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.getIv():byte[], dex: 
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
    protected byte[] getIv() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.getIv():byte[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.getIv():byte[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.initKey(int, java.security.Key):void, dex: 
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
    protected final void initKey(int r1, java.security.Key r2) throws java.security.InvalidKeyException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.initKey(int, java.security.Key):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.initKey(int, java.security.Key):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.loadAlgorithmSpecificParametersFromBeginResult(android.security.keymaster.KeymasterArguments):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected final void loadAlgorithmSpecificParametersFromBeginResult(android.security.keymaster.KeymasterArguments r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.loadAlgorithmSpecificParametersFromBeginResult(android.security.keymaster.KeymasterArguments):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.loadAlgorithmSpecificParametersFromBeginResult(android.security.keymaster.KeymasterArguments):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.resetAll():void, dex: 
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
    protected void resetAll() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.resetAll():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.resetAll():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.setIv(byte[]):void, dex: 
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
    protected void setIv(byte[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.setIv(byte[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi.setIv(byte[]):void");
    }

    protected final int engineGetBlockSize() {
        return 16;
    }
}
