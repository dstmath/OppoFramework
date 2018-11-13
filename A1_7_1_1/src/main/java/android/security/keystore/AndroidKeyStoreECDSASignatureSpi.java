package android.security.keystore;

import java.io.ByteArrayOutputStream;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
abstract class AndroidKeyStoreECDSASignatureSpi extends AndroidKeyStoreSignatureSpiBase {
    private int mGroupSizeBits;
    private final int mKeymasterDigest;

    public static final class NONE extends AndroidKeyStoreECDSASignatureSpi {

        private static class TruncateToFieldSizeMessageStreamer implements KeyStoreCryptoOperationStreamer {
            private long mConsumedInputSizeBytes;
            private final KeyStoreCryptoOperationStreamer mDelegate;
            private final int mGroupSizeBits;
            private final ByteArrayOutputStream mInputBuffer;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer, int):void, dex:  in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer, int):void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
                	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            private TruncateToFieldSizeMessageStreamer(android.security.keystore.KeyStoreCryptoOperationStreamer r1, int r2) {
                /*
                // Can't load method instructions: Load method exception: null in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer, int):void, dex:  in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.<init>(android.security.keystore.KeyStoreCryptoOperationStreamer, int):void");
            }

            /* synthetic */ TruncateToFieldSizeMessageStreamer(KeyStoreCryptoOperationStreamer delegate, int groupSizeBits, TruncateToFieldSizeMessageStreamer truncateToFieldSizeMessageStreamer) {
                this(delegate, groupSizeBits);
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.doFinal(byte[], int, int, byte[], byte[]):byte[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public byte[] doFinal(byte[] r1, int r2, int r3, byte[] r4, byte[] r5) throws android.security.KeyStoreException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.doFinal(byte[], int, int, byte[], byte[]):byte[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.doFinal(byte[], int, int, byte[], byte[]):byte[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.getConsumedInputSizeBytes():long, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public long getConsumedInputSizeBytes() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.getConsumedInputSizeBytes():long, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.getConsumedInputSizeBytes():long");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.getProducedOutputSizeBytes():long, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public long getProducedOutputSizeBytes() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.getProducedOutputSizeBytes():long, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.getProducedOutputSizeBytes():long");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.update(byte[], int, int):byte[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public byte[] update(byte[] r1, int r2, int r3) throws android.security.KeyStoreException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.update(byte[], int, int):byte[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.TruncateToFieldSizeMessageStreamer.update(byte[], int, int):byte[]");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.<init>():void, dex: 
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
        public NONE() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.createMainDataStreamer(android.security.KeyStore, android.os.IBinder):android.security.keystore.KeyStoreCryptoOperationStreamer, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.createMainDataStreamer(android.security.KeyStore, android.os.IBinder):android.security.keystore.KeyStoreCryptoOperationStreamer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.NONE.createMainDataStreamer(android.security.KeyStore, android.os.IBinder):android.security.keystore.KeyStoreCryptoOperationStreamer");
        }
    }

    public static final class SHA1 extends AndroidKeyStoreECDSASignatureSpi {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA1.<init>():void, dex: 
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
        public SHA1() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA1.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA1.<init>():void");
        }
    }

    public static final class SHA224 extends AndroidKeyStoreECDSASignatureSpi {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA224.<init>():void, dex: 
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
        public SHA224() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA224.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA224.<init>():void");
        }
    }

    public static final class SHA256 extends AndroidKeyStoreECDSASignatureSpi {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA256.<init>():void, dex: 
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
        public SHA256() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA256.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA256.<init>():void");
        }
    }

    public static final class SHA384 extends AndroidKeyStoreECDSASignatureSpi {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA384.<init>():void, dex: 
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
        public SHA384() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA384.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA384.<init>():void");
        }
    }

    public static final class SHA512 extends AndroidKeyStoreECDSASignatureSpi {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA512.<init>():void, dex: 
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
        public SHA512() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA512.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.SHA512.<init>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.<init>(int):void, dex: 
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
    AndroidKeyStoreECDSASignatureSpi(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.<init>(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.<init>(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex: 
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
    protected final void addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.addAlgorithmSpecificParametersToBegin(android.security.keymaster.KeymasterArguments):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getAdditionalEntropyAmountForSign():int, dex: 
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
    protected final int getAdditionalEntropyAmountForSign() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getAdditionalEntropyAmountForSign():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getAdditionalEntropyAmountForSign():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getGroupSizeBits():int, dex:  in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getGroupSizeBits():int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getGroupSizeBits():int, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected final int getGroupSizeBits() {
        /*
        // Can't load method instructions: Load method exception: null in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getGroupSizeBits():int, dex:  in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getGroupSizeBits():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.getGroupSizeBits():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.initKey(android.security.keystore.AndroidKeyStoreKey):void, dex: 
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
    protected final void initKey(android.security.keystore.AndroidKeyStoreKey r1) throws java.security.InvalidKeyException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.initKey(android.security.keystore.AndroidKeyStoreKey):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.initKey(android.security.keystore.AndroidKeyStoreKey):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.resetAll():void, dex: 
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
    protected final void resetAll() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.resetAll():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.resetAll():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.resetWhilePreservingInitState():void, dex: 
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
    protected final void resetWhilePreservingInitState() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.resetWhilePreservingInitState():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.AndroidKeyStoreECDSASignatureSpi.resetWhilePreservingInitState():void");
    }
}
