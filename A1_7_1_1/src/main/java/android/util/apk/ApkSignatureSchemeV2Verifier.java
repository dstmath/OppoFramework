package android.util.apk;

import android.util.ArrayMap;
import android.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import libcore.io.Os;

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
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ApkSignatureSchemeV2Verifier {
    private static final int APK_SIGNATURE_SCHEME_V2_BLOCK_ID = 1896449818;
    private static final long APK_SIG_BLOCK_MAGIC_HI = 3617552046287187010L;
    private static final long APK_SIG_BLOCK_MAGIC_LO = 2334950737559900225L;
    private static final int APK_SIG_BLOCK_MIN_SIZE = 32;
    private static final int CHUNK_SIZE_BYTES = 1048576;
    private static final int CONTENT_DIGEST_CHUNKED_SHA256 = 1;
    private static final int CONTENT_DIGEST_CHUNKED_SHA512 = 2;
    public static final int SF_ATTRIBUTE_ANDROID_APK_SIGNED_ID = 2;
    public static final String SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME = "X-Android-APK-Signed";
    private static final int SIGNATURE_DSA_WITH_SHA256 = 769;
    private static final int SIGNATURE_ECDSA_WITH_SHA256 = 513;
    private static final int SIGNATURE_ECDSA_WITH_SHA512 = 514;
    private static final int SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA256 = 259;
    private static final int SIGNATURE_RSA_PKCS1_V1_5_WITH_SHA512 = 260;
    private static final int SIGNATURE_RSA_PSS_WITH_SHA256 = 257;
    private static final int SIGNATURE_RSA_PSS_WITH_SHA512 = 258;

    private interface DataSource {
        void feedIntoMessageDigests(MessageDigest[] messageDigestArr, long j, int i) throws IOException;

        long size();
    }

    private static final class ByteBufferDataSource implements DataSource {
        private final ByteBuffer mBuf;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.<init>(java.nio.ByteBuffer):void, dex: 
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
        public ByteBufferDataSource(java.nio.ByteBuffer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.<init>(java.nio.ByteBuffer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.<init>(java.nio.ByteBuffer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.feedIntoMessageDigests(java.security.MessageDigest[], long, int):void, dex: 
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
        public void feedIntoMessageDigests(java.security.MessageDigest[] r1, long r2, int r4) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.feedIntoMessageDigests(java.security.MessageDigest[], long, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.feedIntoMessageDigests(java.security.MessageDigest[], long, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.size():long, dex: 
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
        public long size() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.size():long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.ByteBufferDataSource.size():long");
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
    private static final class MemoryMappedFileDataSource implements DataSource {
        private static final long MEMORY_PAGE_SIZE_BYTES = 0;
        private static final Os OS = null;
        private final FileDescriptor mFd;
        private final long mFilePosition;
        private final long mSize;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<init>(java.io.FileDescriptor, long, long):void, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<init>(java.io.FileDescriptor, long, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<init>(java.io.FileDescriptor, long, long):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public MemoryMappedFileDataSource(java.io.FileDescriptor r1, long r2, long r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<init>(java.io.FileDescriptor, long, long):void, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<init>(java.io.FileDescriptor, long, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.<init>(java.io.FileDescriptor, long, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.feedIntoMessageDigests(java.security.MessageDigest[], long, int):void, dex: 
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
        public void feedIntoMessageDigests(java.security.MessageDigest[] r1, long r2, int r4) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.feedIntoMessageDigests(java.security.MessageDigest[], long, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.feedIntoMessageDigests(java.security.MessageDigest[], long, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.size():long, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.size():long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.size():long, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public long size() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.size():long, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.size():long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.MemoryMappedFileDataSource.size():long");
        }
    }

    private static class SignatureInfo {
        private final long apkSigningBlockOffset;
        private final long centralDirOffset;
        private final ByteBuffer eocd;
        private final long eocdOffset;
        private final ByteBuffer signatureBlock;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get0(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ long m76-get0(android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get0(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get0(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get1(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get1(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get1(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -get1 */
        static /* synthetic */ long m77-get1(android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get1(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get1(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get1(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get2(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):java.nio.ByteBuffer, dex: 
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
        /* renamed from: -get2 */
        static /* synthetic */ java.nio.ByteBuffer m78-get2(android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get2(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):java.nio.ByteBuffer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get2(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):java.nio.ByteBuffer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get3(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex: 
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
        /* renamed from: -get3 */
        static /* synthetic */ long m79-get3(android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get3(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get3(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get4(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):java.nio.ByteBuffer, dex: 
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
        /* renamed from: -get4 */
        static /* synthetic */ java.nio.ByteBuffer m80-get4(android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get4(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):java.nio.ByteBuffer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.-get4(android.util.apk.ApkSignatureSchemeV2Verifier$SignatureInfo):java.nio.ByteBuffer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.<init>(java.nio.ByteBuffer, long, long, long, java.nio.ByteBuffer):void, dex: 
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
        private SignatureInfo(java.nio.ByteBuffer r1, long r2, long r4, long r6, java.nio.ByteBuffer r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.<init>(java.nio.ByteBuffer, long, long, long, java.nio.ByteBuffer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.SignatureInfo.<init>(java.nio.ByteBuffer, long, long, long, java.nio.ByteBuffer):void");
        }

        /* synthetic */ SignatureInfo(ByteBuffer signatureBlock, long apkSigningBlockOffset, long centralDirOffset, long eocdOffset, ByteBuffer eocd, SignatureInfo signatureInfo) {
            this(signatureBlock, apkSigningBlockOffset, centralDirOffset, eocdOffset, eocd);
        }
    }

    public static class SignatureNotFoundException extends Exception {
        private static final long serialVersionUID = 1;

        public SignatureNotFoundException(String message) {
            super(message);
        }

        public SignatureNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class WrappedX509Certificate extends X509Certificate {
        private final X509Certificate wrapped;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.<init>(java.security.cert.X509Certificate):void, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.<init>(java.security.cert.X509Certificate):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.<init>(java.security.cert.X509Certificate):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public WrappedX509Certificate(java.security.cert.X509Certificate r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.<init>(java.security.cert.X509Certificate):void, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.<init>(java.security.cert.X509Certificate):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.<init>(java.security.cert.X509Certificate):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.checkValidity():void, dex: 
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
        public void checkValidity() throws java.security.cert.CertificateExpiredException, java.security.cert.CertificateNotYetValidException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.checkValidity():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.checkValidity():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.checkValidity(java.util.Date):void, dex: 
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
        public void checkValidity(java.util.Date r1) throws java.security.cert.CertificateExpiredException, java.security.cert.CertificateNotYetValidException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.checkValidity(java.util.Date):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.checkValidity(java.util.Date):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getBasicConstraints():int, dex: 
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
        public int getBasicConstraints() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getBasicConstraints():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getBasicConstraints():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getCriticalExtensionOIDs():java.util.Set<java.lang.String>, dex: 
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
        public java.util.Set<java.lang.String> getCriticalExtensionOIDs() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getCriticalExtensionOIDs():java.util.Set<java.lang.String>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getCriticalExtensionOIDs():java.util.Set<java.lang.String>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getEncoded():byte[], dex: 
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
        public byte[] getEncoded() throws java.security.cert.CertificateEncodingException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getEncoded():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getEncoded():byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getExtensionValue(java.lang.String):byte[], dex: 
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
        public byte[] getExtensionValue(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getExtensionValue(java.lang.String):byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getExtensionValue(java.lang.String):byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getIssuerDN():java.security.Principal, dex: 
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
        public java.security.Principal getIssuerDN() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getIssuerDN():java.security.Principal, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getIssuerDN():java.security.Principal");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getIssuerUniqueID():boolean[], dex: 
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
        public boolean[] getIssuerUniqueID() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getIssuerUniqueID():boolean[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getIssuerUniqueID():boolean[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getKeyUsage():boolean[], dex: 
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
        public boolean[] getKeyUsage() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getKeyUsage():boolean[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getKeyUsage():boolean[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNonCriticalExtensionOIDs():java.util.Set<java.lang.String>, dex: 
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
        public java.util.Set<java.lang.String> getNonCriticalExtensionOIDs() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNonCriticalExtensionOIDs():java.util.Set<java.lang.String>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNonCriticalExtensionOIDs():java.util.Set<java.lang.String>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNotAfter():java.util.Date, dex: 
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
        public java.util.Date getNotAfter() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNotAfter():java.util.Date, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNotAfter():java.util.Date");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNotBefore():java.util.Date, dex: 
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
        public java.util.Date getNotBefore() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNotBefore():java.util.Date, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getNotBefore():java.util.Date");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getPublicKey():java.security.PublicKey, dex: 
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
        public java.security.PublicKey getPublicKey() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getPublicKey():java.security.PublicKey, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getPublicKey():java.security.PublicKey");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSerialNumber():java.math.BigInteger, dex: 
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
        public java.math.BigInteger getSerialNumber() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSerialNumber():java.math.BigInteger, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSerialNumber():java.math.BigInteger");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgName():java.lang.String, dex: 
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
        public java.lang.String getSigAlgName() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgName():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgName():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgOID():java.lang.String, dex: 
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
        public java.lang.String getSigAlgOID() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgOID():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgOID():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgParams():byte[], dex: 
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
        public byte[] getSigAlgParams() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgParams():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSigAlgParams():byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSignature():byte[], dex: 
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
        public byte[] getSignature() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSignature():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSignature():byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSubjectDN():java.security.Principal, dex: 
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
        public java.security.Principal getSubjectDN() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSubjectDN():java.security.Principal, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSubjectDN():java.security.Principal");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSubjectUniqueID():boolean[], dex: 
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
        public boolean[] getSubjectUniqueID() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSubjectUniqueID():boolean[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getSubjectUniqueID():boolean[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getTBSCertificate():byte[], dex: 
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
        public byte[] getTBSCertificate() throws java.security.cert.CertificateEncodingException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getTBSCertificate():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getTBSCertificate():byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getVersion():int, dex: 
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
        public int getVersion() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getVersion():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.getVersion():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.hasUnsupportedCriticalExtension():boolean, dex: 
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
        public boolean hasUnsupportedCriticalExtension() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.hasUnsupportedCriticalExtension():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.hasUnsupportedCriticalExtension():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.toString():java.lang.String, dex: 
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
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.toString():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.verify(java.security.PublicKey):void, dex: 
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
        public void verify(java.security.PublicKey r1) throws java.security.cert.CertificateException, java.security.NoSuchAlgorithmException, java.security.InvalidKeyException, java.security.NoSuchProviderException, java.security.SignatureException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.verify(java.security.PublicKey):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.verify(java.security.PublicKey):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.verify(java.security.PublicKey, java.lang.String):void, dex: 
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
        public void verify(java.security.PublicKey r1, java.lang.String r2) throws java.security.cert.CertificateException, java.security.NoSuchAlgorithmException, java.security.InvalidKeyException, java.security.NoSuchProviderException, java.security.SignatureException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.verify(java.security.PublicKey, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.WrappedX509Certificate.verify(java.security.PublicKey, java.lang.String):void");
        }
    }

    private static class VerbatimX509Certificate extends WrappedX509Certificate {
        private byte[] encodedVerbatim;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.<init>(java.security.cert.X509Certificate, byte[]):void, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.<init>(java.security.cert.X509Certificate, byte[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.<init>(java.security.cert.X509Certificate, byte[]):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public VerbatimX509Certificate(java.security.cert.X509Certificate r1, byte[] r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.<init>(java.security.cert.X509Certificate, byte[]):void, dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.<init>(java.security.cert.X509Certificate, byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.<init>(java.security.cert.X509Certificate, byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.getEncoded():byte[], dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.getEncoded():byte[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.getEncoded():byte[], dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public byte[] getEncoded() throws java.security.cert.CertificateEncodingException {
            /*
            // Can't load method instructions: Load method exception: null in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.getEncoded():byte[], dex:  in method: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.getEncoded():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.apk.ApkSignatureSchemeV2Verifier.VerbatimX509Certificate.getEncoded():byte[]");
        }
    }

    public ApkSignatureSchemeV2Verifier() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0025 A:{SYNTHETIC, Splitter: B:24:0x0025} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0038 A:{Catch:{ SignatureNotFoundException -> 0x002b }} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x002a A:{SYNTHETIC, Splitter: B:27:0x002a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean hasSignature(String apkFile) throws IOException {
        Throwable th;
        Throwable th2 = null;
        RandomAccessFile apk = null;
        try {
            RandomAccessFile apk2 = new RandomAccessFile(apkFile, "r");
            try {
                findSignature(apk2);
                if (apk2 != null) {
                    try {
                        apk2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return true;
                }
                try {
                    throw th2;
                } catch (SignatureNotFoundException e) {
                    apk = apk2;
                }
            } catch (Throwable th4) {
                th = th4;
                apk = apk2;
                if (apk != null) {
                    try {
                        apk.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (SignatureNotFoundException e2) {
                        return false;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (apk != null) {
            }
            if (th2 == null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0021 A:{SYNTHETIC, Splitter: B:18:0x0021} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0026  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static X509Certificate[][] verify(String apkFile) throws SignatureNotFoundException, SecurityException, IOException {
        Throwable th;
        Throwable th2 = null;
        RandomAccessFile apk = null;
        try {
            RandomAccessFile apk2 = new RandomAccessFile(apkFile, "r");
            try {
                X509Certificate[][] verify = verify(apk2);
                if (apk2 != null) {
                    try {
                        apk2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return verify;
                }
                throw th2;
            } catch (Throwable th4) {
                th = th4;
                apk = apk2;
                if (apk != null) {
                    try {
                        apk.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (apk != null) {
            }
            if (th2 == null) {
            }
        }
    }

    private static X509Certificate[][] verify(RandomAccessFile apk) throws SignatureNotFoundException, SecurityException, IOException {
        return verify(apk.getFD(), findSignature(apk));
    }

    private static SignatureInfo findSignature(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = getEocd(apk);
        ByteBuffer eocd = eocdAndOffsetInFile.first;
        long eocdOffset = ((Long) eocdAndOffsetInFile.second).longValue();
        if (ZipUtils.isZip64EndOfCentralDirectoryLocatorPresent(apk, eocdOffset)) {
            throw new SignatureNotFoundException("ZIP64 APK not supported");
        }
        long centralDirOffset = getCentralDirOffset(eocd, eocdOffset);
        Pair<ByteBuffer, Long> apkSigningBlockAndOffsetInFile = findApkSigningBlock(apk, centralDirOffset);
        ByteBuffer apkSigningBlock = apkSigningBlockAndOffsetInFile.first;
        return new SignatureInfo(findApkSignatureSchemeV2Block(apkSigningBlock), ((Long) apkSigningBlockAndOffsetInFile.second).longValue(), centralDirOffset, eocdOffset, eocd, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0033 A:{ExcHandler: java.io.IOException (r14_0 'e' java.lang.Exception), Splitter: B:8:0x0023} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0033 A:{ExcHandler: java.io.IOException (r14_0 'e' java.lang.Exception), Splitter: B:8:0x0023} */
    /* JADX WARNING: Missing block: B:11:0x0033, code:
            r14 = move-exception;
     */
    /* JADX WARNING: Missing block: B:13:0x0056, code:
            throw new java.lang.SecurityException("Failed to parse/verify signer #" + r18 + " block", r14);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static X509Certificate[][] verify(FileDescriptor apkFileDescriptor, SignatureInfo signatureInfo) throws SecurityException {
        int signerCount = 0;
        Map<Integer, byte[]> contentDigests = new ArrayMap();
        List<X509Certificate[]> signerCerts = new ArrayList();
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            try {
                ByteBuffer signers = getLengthPrefixedSlice(SignatureInfo.m80-get4(signatureInfo));
                while (signers.hasRemaining()) {
                    signerCount++;
                    try {
                        signerCerts.add(verifySigner(getLengthPrefixedSlice(signers), contentDigests, certFactory));
                    } catch (Exception e) {
                    }
                }
                if (signerCount < 1) {
                    throw new SecurityException("No signers found");
                } else if (contentDigests.isEmpty()) {
                    throw new SecurityException("No content digests found");
                } else {
                    verifyIntegrity(contentDigests, apkFileDescriptor, SignatureInfo.m76-get0(signatureInfo), SignatureInfo.m77-get1(signatureInfo), SignatureInfo.m79-get3(signatureInfo), SignatureInfo.m78-get2(signatureInfo));
                    return (X509Certificate[][]) signerCerts.toArray(new X509Certificate[signerCerts.size()][]);
                }
            } catch (IOException e2) {
                throw new SecurityException("Failed to read list of signers", e2);
            }
        } catch (CertificateException e3) {
            throw new RuntimeException("Failed to obtain X.509 CertificateFactory", e3);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x0164 A:{ExcHandler: java.io.IOException (r17_1 'e' java.lang.Throwable), Splitter: B:44:0x014b} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0036 A:{ExcHandler: java.io.IOException (r17_0 'e' java.lang.Throwable), Splitter: B:4:0x001d} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0109 A:{ExcHandler: java.security.NoSuchAlgorithmException (r18_0 'e' java.lang.Throwable), Splitter: B:29:0x00b7} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0109 A:{ExcHandler: java.security.NoSuchAlgorithmException (r18_0 'e' java.lang.Throwable), Splitter: B:29:0x00b7} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0109 A:{ExcHandler: java.security.NoSuchAlgorithmException (r18_0 'e' java.lang.Throwable), Splitter: B:29:0x00b7} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0109 A:{ExcHandler: java.security.NoSuchAlgorithmException (r18_0 'e' java.lang.Throwable), Splitter: B:29:0x00b7} */
    /* JADX WARNING: Missing block: B:9:0x0036, code:
            r17 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x005a, code:
            throw new java.lang.SecurityException("Failed to parse signature record #" + r33, r17);
     */
    /* JADX WARNING: Missing block: B:37:0x0109, code:
            r18 = move-exception;
     */
    /* JADX WARNING: Missing block: B:39:0x0134, code:
            throw new java.lang.SecurityException("Failed to verify " + r21 + " signature", r18);
     */
    /* JADX WARNING: Missing block: B:49:0x0164, code:
            r17 = move-exception;
     */
    /* JADX WARNING: Missing block: B:51:0x0186, code:
            throw new java.io.IOException("Failed to parse digest record #" + r14, r17);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static X509Certificate[] verifySigner(ByteBuffer signerBlock, Map<Integer, byte[]> contentDigests, CertificateFactory certFactory) throws SecurityException, IOException {
        int sigAlgorithm;
        ByteBuffer signedData = getLengthPrefixedSlice(signerBlock);
        ByteBuffer signatures = getLengthPrefixedSlice(signerBlock);
        byte[] publicKeyBytes = readLengthPrefixedByteArray(signerBlock);
        int signatureCount = 0;
        int bestSigAlgorithm = -1;
        byte[] bestSigAlgorithmSignatureBytes = null;
        List<Integer> signaturesSigAlgorithms = new ArrayList();
        while (signatures.hasRemaining()) {
            signatureCount++;
            try {
                ByteBuffer signature = getLengthPrefixedSlice(signatures);
                if (signature.remaining() < 8) {
                    throw new SecurityException("Signature record too short");
                }
                sigAlgorithm = signature.getInt();
                signaturesSigAlgorithms.add(Integer.valueOf(sigAlgorithm));
                if (isSupportedSignatureAlgorithm(sigAlgorithm) && (bestSigAlgorithm == -1 || compareSignatureAlgorithm(sigAlgorithm, bestSigAlgorithm) > 0)) {
                    bestSigAlgorithm = sigAlgorithm;
                    bestSigAlgorithmSignatureBytes = readLengthPrefixedByteArray(signature);
                }
            } catch (Throwable e) {
            }
        }
        if (bestSigAlgorithm != -1) {
            String keyAlgorithm = getSignatureAlgorithmJcaKeyAlgorithm(bestSigAlgorithm);
            Pair<String, ? extends AlgorithmParameterSpec> signatureAlgorithmParams = getSignatureAlgorithmJcaSignatureAlgorithm(bestSigAlgorithm);
            String jcaSignatureAlgorithm = signatureAlgorithmParams.first;
            AlgorithmParameterSpec jcaSignatureAlgorithmParams = signatureAlgorithmParams.second;
            try {
                PublicKey publicKey = KeyFactory.getInstance(keyAlgorithm).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                Signature sig = Signature.getInstance(jcaSignatureAlgorithm);
                sig.initVerify(publicKey);
                if (jcaSignatureAlgorithmParams != null) {
                    sig.setParameter(jcaSignatureAlgorithmParams);
                }
                sig.update(signedData);
                if (sig.verify(bestSigAlgorithmSignatureBytes)) {
                    Object contentDigest = null;
                    signedData.clear();
                    ByteBuffer digests = getLengthPrefixedSlice(signedData);
                    List<Integer> digestsSigAlgorithms = new ArrayList();
                    int digestCount = 0;
                    while (digests.hasRemaining()) {
                        digestCount++;
                        try {
                            ByteBuffer digest = getLengthPrefixedSlice(digests);
                            if (digest.remaining() < 8) {
                                throw new IOException("Record too short");
                            }
                            sigAlgorithm = digest.getInt();
                            digestsSigAlgorithms.add(Integer.valueOf(sigAlgorithm));
                            if (sigAlgorithm == bestSigAlgorithm) {
                                contentDigest = readLengthPrefixedByteArray(digest);
                            }
                        } catch (Throwable e2) {
                        }
                    }
                    if (signaturesSigAlgorithms.equals(digestsSigAlgorithms)) {
                        int digestAlgorithm = getSignatureAlgorithmContentDigestAlgorithm(bestSigAlgorithm);
                        byte[] previousSignerDigest = (byte[]) contentDigests.put(Integer.valueOf(digestAlgorithm), contentDigest);
                        if (previousSignerDigest == null || MessageDigest.isEqual(previousSignerDigest, contentDigest)) {
                            ByteBuffer certificates = getLengthPrefixedSlice(signedData);
                            List<X509Certificate> certs = new ArrayList();
                            int certificateCount = 0;
                            while (certificates.hasRemaining()) {
                                certificateCount++;
                                byte[] encodedCert = readLengthPrefixedByteArray(certificates);
                                try {
                                    certs.add(new VerbatimX509Certificate((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert)), encodedCert));
                                } catch (Throwable e3) {
                                    throw new SecurityException("Failed to decode certificate #" + certificateCount, e3);
                                }
                            }
                            if (certs.isEmpty()) {
                                throw new SecurityException("No certificates listed");
                            }
                            if (Arrays.equals(publicKeyBytes, ((X509Certificate) certs.get(0)).getPublicKey().getEncoded())) {
                                return (X509Certificate[]) certs.toArray(new X509Certificate[certs.size()]);
                            }
                            throw new SecurityException("Public key mismatch between certificate and signature record");
                        }
                        throw new SecurityException(getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm) + " contents digest does not match the digest specified by a preceding signer");
                    }
                    throw new SecurityException("Signature algorithms don't match between digests and signatures records");
                }
                throw new SecurityException(jcaSignatureAlgorithm + " signature did not verify");
            } catch (Throwable e4) {
            }
        } else if (signatureCount == 0) {
            throw new SecurityException("No signatures found");
        } else {
            throw new SecurityException("No supported signatures found");
        }
    }

    private static void verifyIntegrity(Map<Integer, byte[]> expectedDigests, FileDescriptor apkFileDescriptor, long apkSigningBlockOffset, long centralDirOffset, long eocdOffset, ByteBuffer eocdBuf) throws SecurityException {
        if (expectedDigests.isEmpty()) {
            throw new SecurityException("No digests provided");
        }
        DataSource beforeApkSigningBlock = new MemoryMappedFileDataSource(apkFileDescriptor, 0, apkSigningBlockOffset);
        DataSource centralDir = new MemoryMappedFileDataSource(apkFileDescriptor, centralDirOffset, eocdOffset - centralDirOffset);
        eocdBuf = eocdBuf.duplicate();
        eocdBuf.order(ByteOrder.LITTLE_ENDIAN);
        ZipUtils.setZipEocdCentralDirectoryOffset(eocdBuf, apkSigningBlockOffset);
        DataSource byteBufferDataSource = new ByteBufferDataSource(eocdBuf);
        int[] digestAlgorithms = new int[expectedDigests.size()];
        int digestAlgorithmCount = 0;
        for (Integer intValue : expectedDigests.keySet()) {
            digestAlgorithms[digestAlgorithmCount] = intValue.intValue();
            digestAlgorithmCount++;
        }
        try {
            byte[][] actualDigests = computeContentDigests(digestAlgorithms, new DataSource[]{beforeApkSigningBlock, centralDir, byteBufferDataSource});
            int i = 0;
            while (i < digestAlgorithms.length) {
                int digestAlgorithm = digestAlgorithms[i];
                if (MessageDigest.isEqual((byte[]) expectedDigests.get(Integer.valueOf(digestAlgorithm)), actualDigests[i])) {
                    i++;
                } else {
                    throw new SecurityException(getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm) + " digest of contents did not verify");
                }
            }
        } catch (Throwable e) {
            throw new SecurityException("Failed to compute digest(s) of contents", e);
        }
    }

    private static byte[][] computeContentDigests(int[] digestAlgorithms, DataSource[] contents) throws DigestException {
        long totalChunkCountLong = 0;
        for (DataSource input : contents) {
            totalChunkCountLong += getChunkCount(input.size());
        }
        if (totalChunkCountLong >= 2097151) {
            throw new DigestException("Too many chunks: " + totalChunkCountLong);
        }
        int i;
        byte[] concatenationOfChunkCountAndChunkDigests;
        String jcaAlgorithmName;
        int digestAlgorithm;
        int totalChunkCount = (int) totalChunkCountLong;
        byte[][] digestsOfChunks = new byte[digestAlgorithms.length][];
        for (i = 0; i < digestAlgorithms.length; i++) {
            concatenationOfChunkCountAndChunkDigests = new byte[((totalChunkCount * getContentDigestAlgorithmOutputSizeBytes(digestAlgorithms[i])) + 5)];
            concatenationOfChunkCountAndChunkDigests[0] = (byte) 90;
            setUnsignedInt32LittleEndian(totalChunkCount, concatenationOfChunkCountAndChunkDigests, 1);
            digestsOfChunks[i] = concatenationOfChunkCountAndChunkDigests;
        }
        byte[] chunkContentPrefix = new byte[5];
        chunkContentPrefix[0] = (byte) -91;
        int chunkIndex = 0;
        MessageDigest[] mds = new MessageDigest[digestAlgorithms.length];
        i = 0;
        while (i < digestAlgorithms.length) {
            jcaAlgorithmName = getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithms[i]);
            try {
                mds[i] = MessageDigest.getInstance(jcaAlgorithmName);
                i++;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(jcaAlgorithmName + " digest not supported", e);
            }
        }
        int dataSourceIndex = 0;
        for (DataSource input2 : contents) {
            long inputOffset = 0;
            long inputRemaining = input2.size();
            while (inputRemaining > 0) {
                int chunkSize = (int) Math.min(inputRemaining, 1048576);
                setUnsignedInt32LittleEndian(chunkSize, chunkContentPrefix, 1);
                for (MessageDigest update : mds) {
                    update.update(chunkContentPrefix);
                }
                try {
                    input2.feedIntoMessageDigests(mds, inputOffset, chunkSize);
                    for (i = 0; i < digestAlgorithms.length; i++) {
                        digestAlgorithm = digestAlgorithms[i];
                        concatenationOfChunkCountAndChunkDigests = digestsOfChunks[i];
                        int expectedDigestSizeBytes = getContentDigestAlgorithmOutputSizeBytes(digestAlgorithm);
                        MessageDigest md = mds[i];
                        int actualDigestSizeBytes = md.digest(concatenationOfChunkCountAndChunkDigests, (chunkIndex * expectedDigestSizeBytes) + 5, expectedDigestSizeBytes);
                        if (actualDigestSizeBytes != expectedDigestSizeBytes) {
                            throw new RuntimeException("Unexpected output size of " + md.getAlgorithm() + " digest: " + actualDigestSizeBytes);
                        }
                    }
                    inputOffset += (long) chunkSize;
                    inputRemaining -= (long) chunkSize;
                    chunkIndex++;
                } catch (IOException e2) {
                    throw new DigestException("Failed to digest chunk #" + chunkIndex + " of section #" + dataSourceIndex, e2);
                }
            }
            dataSourceIndex++;
        }
        byte[][] result = new byte[digestAlgorithms.length][];
        i = 0;
        while (i < digestAlgorithms.length) {
            digestAlgorithm = digestAlgorithms[i];
            byte[] input3 = digestsOfChunks[i];
            jcaAlgorithmName = getContentDigestAlgorithmJcaDigestAlgorithm(digestAlgorithm);
            try {
                result[i] = MessageDigest.getInstance(jcaAlgorithmName).digest(input3);
                i++;
            } catch (NoSuchAlgorithmException e3) {
                throw new RuntimeException(jcaAlgorithmName + " digest not supported", e3);
            }
        }
        return result;
    }

    private static Pair<ByteBuffer, Long> getEocd(RandomAccessFile apk) throws IOException, SignatureNotFoundException {
        Pair<ByteBuffer, Long> eocdAndOffsetInFile = ZipUtils.findZipEndOfCentralDirectoryRecord(apk);
        if (eocdAndOffsetInFile != null) {
            return eocdAndOffsetInFile;
        }
        throw new SignatureNotFoundException("Not an APK file: ZIP End of Central Directory record not found");
    }

    private static long getCentralDirOffset(ByteBuffer eocd, long eocdOffset) throws SignatureNotFoundException {
        long centralDirOffset = ZipUtils.getZipEocdCentralDirectoryOffset(eocd);
        if (centralDirOffset >= eocdOffset) {
            throw new SignatureNotFoundException("ZIP Central Directory offset out of range: " + centralDirOffset + ". ZIP End of Central Directory offset: " + eocdOffset);
        } else if (centralDirOffset + ZipUtils.getZipEocdCentralDirectorySizeBytes(eocd) == eocdOffset) {
            return centralDirOffset;
        } else {
            throw new SignatureNotFoundException("ZIP Central Directory is not immediately followed by End of Central Directory");
        }
    }

    private static final long getChunkCount(long inputSizeBytes) {
        return ((inputSizeBytes + 1048576) - 1) / 1048576;
    }

    private static boolean isSupportedSignatureAlgorithm(int sigAlgorithm) {
        switch (sigAlgorithm) {
            case 257:
            case 258:
            case 259:
            case 260:
            case 513:
            case 514:
            case 769:
                return true;
            default:
                return false;
        }
    }

    private static int compareSignatureAlgorithm(int sigAlgorithm1, int sigAlgorithm2) {
        return compareContentDigestAlgorithm(getSignatureAlgorithmContentDigestAlgorithm(sigAlgorithm1), getSignatureAlgorithmContentDigestAlgorithm(sigAlgorithm2));
    }

    private static int compareContentDigestAlgorithm(int digestAlgorithm1, int digestAlgorithm2) {
        switch (digestAlgorithm1) {
            case 1:
                switch (digestAlgorithm2) {
                    case 1:
                        return 0;
                    case 2:
                        return -1;
                    default:
                        throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
                }
            case 2:
                switch (digestAlgorithm2) {
                    case 1:
                        return 1;
                    case 2:
                        return 0;
                    default:
                        throw new IllegalArgumentException("Unknown digestAlgorithm2: " + digestAlgorithm2);
                }
            default:
                throw new IllegalArgumentException("Unknown digestAlgorithm1: " + digestAlgorithm1);
        }
    }

    private static int getSignatureAlgorithmContentDigestAlgorithm(int sigAlgorithm) {
        switch (sigAlgorithm) {
            case 257:
            case 259:
            case 513:
            case 769:
                return 1;
            case 258:
            case 260:
            case 514:
                return 2;
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
        }
    }

    private static String getContentDigestAlgorithmJcaDigestAlgorithm(int digestAlgorithm) {
        switch (digestAlgorithm) {
            case 1:
                return "SHA-256";
            case 2:
                return "SHA-512";
            default:
                throw new IllegalArgumentException("Unknown content digest algorthm: " + digestAlgorithm);
        }
    }

    private static int getContentDigestAlgorithmOutputSizeBytes(int digestAlgorithm) {
        switch (digestAlgorithm) {
            case 1:
                return 32;
            case 2:
                return 64;
            default:
                throw new IllegalArgumentException("Unknown content digest algorthm: " + digestAlgorithm);
        }
    }

    private static String getSignatureAlgorithmJcaKeyAlgorithm(int sigAlgorithm) {
        switch (sigAlgorithm) {
            case 257:
            case 258:
            case 259:
            case 260:
                return "RSA";
            case 513:
            case 514:
                return "EC";
            case 769:
                return "DSA";
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
        }
    }

    private static Pair<String, ? extends AlgorithmParameterSpec> getSignatureAlgorithmJcaSignatureAlgorithm(int sigAlgorithm) {
        switch (sigAlgorithm) {
            case 257:
                return Pair.create("SHA256withRSA/PSS", new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
            case 258:
                return Pair.create("SHA512withRSA/PSS", new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1));
            case 259:
                return Pair.create("SHA256withRSA", null);
            case 260:
                return Pair.create("SHA512withRSA", null);
            case 513:
                return Pair.create("SHA256withECDSA", null);
            case 514:
                return Pair.create("SHA512withECDSA", null);
            case 769:
                return Pair.create("SHA256withDSA", null);
            default:
                throw new IllegalArgumentException("Unknown signature algorithm: 0x" + Long.toHexString((long) (sigAlgorithm & -1)));
        }
    }

    private static ByteBuffer sliceFromTo(ByteBuffer source, int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start: " + start);
        } else if (end < start) {
            throw new IllegalArgumentException("end < start: " + end + " < " + start);
        } else {
            int capacity = source.capacity();
            if (end > source.capacity()) {
                throw new IllegalArgumentException("end > capacity: " + end + " > " + capacity);
            }
            int originalLimit = source.limit();
            int originalPosition = source.position();
            try {
                source.position(0);
                source.limit(end);
                source.position(start);
                ByteBuffer result = source.slice();
                result.order(source.order());
                return result;
            } finally {
                source.position(0);
                source.limit(originalLimit);
                source.position(originalPosition);
            }
        }
    }

    private static ByteBuffer getByteBuffer(ByteBuffer source, int size) throws BufferUnderflowException {
        if (size < 0) {
            throw new IllegalArgumentException("size: " + size);
        }
        int originalLimit = source.limit();
        int position = source.position();
        int limit = position + size;
        if (limit < position || limit > originalLimit) {
            throw new BufferUnderflowException();
        }
        source.limit(limit);
        try {
            ByteBuffer result = source.slice();
            result.order(source.order());
            source.position(limit);
            return result;
        } finally {
            source.limit(originalLimit);
        }
    }

    private static ByteBuffer getLengthPrefixedSlice(ByteBuffer source) throws IOException {
        if (source.remaining() < 4) {
            throw new IOException("Remaining buffer too short to contain length of length-prefixed field. Remaining: " + source.remaining());
        }
        int len = source.getInt();
        if (len < 0) {
            throw new IllegalArgumentException("Negative length");
        } else if (len <= source.remaining()) {
            return getByteBuffer(source, len);
        } else {
            throw new IOException("Length-prefixed field longer than remaining buffer. Field length: " + len + ", remaining: " + source.remaining());
        }
    }

    private static byte[] readLengthPrefixedByteArray(ByteBuffer buf) throws IOException {
        int len = buf.getInt();
        if (len < 0) {
            throw new IOException("Negative length");
        } else if (len > buf.remaining()) {
            throw new IOException("Underflow while reading length-prefixed value. Length: " + len + ", available: " + buf.remaining());
        } else {
            byte[] result = new byte[len];
            buf.get(result);
            return result;
        }
    }

    private static void setUnsignedInt32LittleEndian(int value, byte[] result, int offset) {
        result[offset] = (byte) (value & 255);
        result[offset + 1] = (byte) ((value >>> 8) & 255);
        result[offset + 2] = (byte) ((value >>> 16) & 255);
        result[offset + 3] = (byte) ((value >>> 24) & 255);
    }

    private static Pair<ByteBuffer, Long> findApkSigningBlock(RandomAccessFile apk, long centralDirOffset) throws IOException, SignatureNotFoundException {
        if (centralDirOffset < 32) {
            throw new SignatureNotFoundException("APK too small for APK Signing Block. ZIP Central Directory offset: " + centralDirOffset);
        }
        ByteBuffer footer = ByteBuffer.allocate(24);
        footer.order(ByteOrder.LITTLE_ENDIAN);
        apk.seek(centralDirOffset - ((long) footer.capacity()));
        apk.readFully(footer.array(), footer.arrayOffset(), footer.capacity());
        if (footer.getLong(8) == APK_SIG_BLOCK_MAGIC_LO && footer.getLong(16) == APK_SIG_BLOCK_MAGIC_HI) {
            long apkSigBlockSizeInFooter = footer.getLong(0);
            if (apkSigBlockSizeInFooter < ((long) footer.capacity()) || apkSigBlockSizeInFooter > 2147483639) {
                throw new SignatureNotFoundException("APK Signing Block size out of range: " + apkSigBlockSizeInFooter);
            }
            int totalSize = (int) (8 + apkSigBlockSizeInFooter);
            long apkSigBlockOffset = centralDirOffset - ((long) totalSize);
            if (apkSigBlockOffset < 0) {
                throw new SignatureNotFoundException("APK Signing Block offset out of range: " + apkSigBlockOffset);
            }
            ByteBuffer apkSigBlock = ByteBuffer.allocate(totalSize);
            apkSigBlock.order(ByteOrder.LITTLE_ENDIAN);
            apk.seek(apkSigBlockOffset);
            apk.readFully(apkSigBlock.array(), apkSigBlock.arrayOffset(), apkSigBlock.capacity());
            long apkSigBlockSizeInHeader = apkSigBlock.getLong(0);
            if (apkSigBlockSizeInHeader == apkSigBlockSizeInFooter) {
                return Pair.create(apkSigBlock, Long.valueOf(apkSigBlockOffset));
            }
            throw new SignatureNotFoundException("APK Signing Block sizes in header and footer do not match: " + apkSigBlockSizeInHeader + " vs " + apkSigBlockSizeInFooter);
        }
        throw new SignatureNotFoundException("No APK Signing Block before ZIP Central Directory");
    }

    private static ByteBuffer findApkSignatureSchemeV2Block(ByteBuffer apkSigningBlock) throws SignatureNotFoundException {
        checkByteOrderLittleEndian(apkSigningBlock);
        ByteBuffer pairs = sliceFromTo(apkSigningBlock, 8, apkSigningBlock.capacity() - 24);
        int entryCount = 0;
        while (pairs.hasRemaining()) {
            entryCount++;
            if (pairs.remaining() < 8) {
                throw new SignatureNotFoundException("Insufficient data to read size of APK Signing Block entry #" + entryCount);
            }
            long lenLong = pairs.getLong();
            if (lenLong < 4 || lenLong > 2147483647L) {
                throw new SignatureNotFoundException("APK Signing Block entry #" + entryCount + " size out of range: " + lenLong);
            }
            int len = (int) lenLong;
            int nextEntryPos = pairs.position() + len;
            if (len > pairs.remaining()) {
                throw new SignatureNotFoundException("APK Signing Block entry #" + entryCount + " size out of range: " + len + ", available: " + pairs.remaining());
            } else if (pairs.getInt() == APK_SIGNATURE_SCHEME_V2_BLOCK_ID) {
                return getByteBuffer(pairs, len - 4);
            } else {
                pairs.position(nextEntryPos);
            }
        }
        throw new SignatureNotFoundException("No APK Signature Scheme v2 block in APK Signing Block");
    }

    private static void checkByteOrderLittleEndian(ByteBuffer buffer) {
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("ByteBuffer byte order must be little endian");
        }
    }
}
