package android.os;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Color;
import android.os.IRecoverySystemProgressListener.Stub;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class RecoverySystem {
    public static final File BLOCK_MAP_FILE = null;
    private static final File DEFAULT_KEYSTORE = null;
    private static final File LAST_INSTALL_FILE = null;
    private static final String LAST_PREFIX = "last_";
    private static final File LOG_FILE = null;
    private static final int LOG_FILE_MAX_LENGTH = 65536;
    private static final long PUBLISH_PROGRESS_INTERVAL_MS = 500;
    private static final File RECOVERY_DIR = null;
    private static final String TAG = "RecoverySystem";
    public static final File UNCRYPT_PACKAGE_FILE = null;
    public static final File UNCRYPT_STATUS_FILE = null;
    private static final Object sRequestLock = null;
    private final IRecoverySystem mService;

    /* renamed from: android.os.RecoverySystem$1 */
    static class AnonymousClass1 extends InputStream {
        int lastPercent;
        long lastPublishTime;
        long soFar;
        long toRead;
        final /* synthetic */ int val$commentSize;
        final /* synthetic */ long val$fileLen;
        final /* synthetic */ ProgressListener val$listenerForInner;
        final /* synthetic */ RandomAccessFile val$raf;
        final /* synthetic */ long val$startTimeMillis;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: android.os.RecoverySystem.1.<init>(long, int, long, java.io.RandomAccessFile, android.os.RecoverySystem$ProgressListener):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(long r1, int r3, long r4, java.io.RandomAccessFile r6, android.os.RecoverySystem.ProgressListener r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: android.os.RecoverySystem.1.<init>(long, int, long, java.io.RandomAccessFile, android.os.RecoverySystem$ProgressListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.1.<init>(long, int, long, java.io.RandomAccessFile, android.os.RecoverySystem$ProgressListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.os.RecoverySystem.1.read(byte[], int, int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int read(byte[] r1, int r2, int r3) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.os.RecoverySystem.1.read(byte[], int, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.1.read(byte[], int, int):int");
        }

        public int read() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    /* renamed from: android.os.RecoverySystem$2 */
    static class AnonymousClass2 extends Stub {
        int lastProgress;
        long lastPublishTime;
        final /* synthetic */ ProgressListener val$listener;
        final /* synthetic */ Handler val$progressHandler;

        /* renamed from: android.os.RecoverySystem$2$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ AnonymousClass2 this$1;
            final /* synthetic */ ProgressListener val$listener;
            final /* synthetic */ long val$now;
            final /* synthetic */ int val$progress;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.os.RecoverySystem.2.1.<init>(android.os.RecoverySystem$2, int, long, android.os.RecoverySystem$ProgressListener):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            AnonymousClass1(android.os.RecoverySystem.AnonymousClass2 r1, int r2, long r3, android.os.RecoverySystem.ProgressListener r5) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.os.RecoverySystem.2.1.<init>(android.os.RecoverySystem$2, int, long, android.os.RecoverySystem$ProgressListener):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.2.1.<init>(android.os.RecoverySystem$2, int, long, android.os.RecoverySystem$ProgressListener):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.os.RecoverySystem.2.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.os.RecoverySystem.2.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.2.1.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.os.RecoverySystem.2.<init>(android.os.Handler, android.os.RecoverySystem$ProgressListener):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass2(android.os.Handler r1, android.os.RecoverySystem.ProgressListener r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.os.RecoverySystem.2.<init>(android.os.Handler, android.os.RecoverySystem$ProgressListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.2.<init>(android.os.Handler, android.os.RecoverySystem$ProgressListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.os.RecoverySystem.2.onProgress(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onProgress(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.os.RecoverySystem.2.onProgress(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.2.onProgress(int):void");
        }
    }

    /* renamed from: android.os.RecoverySystem$3 */
    static class AnonymousClass3 extends BroadcastReceiver {
        final /* synthetic */ ConditionVariable val$condition;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.os.RecoverySystem.3.<init>(android.os.ConditionVariable):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass3(android.os.ConditionVariable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.os.RecoverySystem.3.<init>(android.os.ConditionVariable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.3.<init>(android.os.ConditionVariable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.os.RecoverySystem.3.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.os.RecoverySystem.3.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.3.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: android.os.RecoverySystem$4 */
    static class AnonymousClass4 extends BroadcastReceiver {
        final /* synthetic */ ConditionVariable val$condition;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.os.RecoverySystem.4.<init>(android.os.ConditionVariable):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass4(android.os.ConditionVariable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.os.RecoverySystem.4.<init>(android.os.ConditionVariable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.4.<init>(android.os.ConditionVariable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.os.RecoverySystem.4.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.os.RecoverySystem.4.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.4.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    public interface ProgressListener {
        void onProgress(int i);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.os.RecoverySystem.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.os.RecoverySystem.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.<clinit>():void");
    }

    private static HashSet<X509Certificate> getTrustedCerts(File keystore) throws IOException, GeneralSecurityException {
        HashSet<X509Certificate> trusted = new HashSet();
        if (keystore == null) {
            keystore = DEFAULT_KEYSTORE;
        }
        ZipFile zip = new ZipFile(keystore);
        InputStream is;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                is = zip.getInputStream((ZipEntry) entries.nextElement());
                trusted.add((X509Certificate) cf.generateCertificate(is));
                is.close();
            }
            zip.close();
            return trusted;
        } catch (Throwable th) {
            zip.close();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static void verifyPackage(java.io.File r30, android.os.RecoverySystem.ProgressListener r31, java.io.File r32) throws java.io.IOException, java.security.GeneralSecurityException {
        /*
        r3 = r30.length();
        r8 = new java.io.RandomAccessFile;
        r2 = "r";
        r0 = r30;
        r8.<init>(r0, r2);
        r6 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0042 }
        if (r31 == 0) goto L_0x001a;	 Catch:{ all -> 0x0042 }
    L_0x0014:
        r2 = 0;	 Catch:{ all -> 0x0042 }
        r0 = r31;	 Catch:{ all -> 0x0042 }
        r0.onProgress(r2);	 Catch:{ all -> 0x0042 }
    L_0x001a:
        r28 = 6;	 Catch:{ all -> 0x0042 }
        r28 = r3 - r28;	 Catch:{ all -> 0x0042 }
        r0 = r28;	 Catch:{ all -> 0x0042 }
        r8.seek(r0);	 Catch:{ all -> 0x0042 }
        r2 = 6;	 Catch:{ all -> 0x0042 }
        r0 = new byte[r2];	 Catch:{ all -> 0x0042 }
        r16 = r0;	 Catch:{ all -> 0x0042 }
        r0 = r16;	 Catch:{ all -> 0x0042 }
        r8.readFully(r0);	 Catch:{ all -> 0x0042 }
        r2 = 2;	 Catch:{ all -> 0x0042 }
        r2 = r16[r2];	 Catch:{ all -> 0x0042 }
        r9 = -1;	 Catch:{ all -> 0x0042 }
        if (r2 != r9) goto L_0x0039;	 Catch:{ all -> 0x0042 }
    L_0x0033:
        r2 = 3;	 Catch:{ all -> 0x0042 }
        r2 = r16[r2];	 Catch:{ all -> 0x0042 }
        r9 = -1;	 Catch:{ all -> 0x0042 }
        if (r2 == r9) goto L_0x0047;	 Catch:{ all -> 0x0042 }
    L_0x0039:
        r2 = new java.security.SignatureException;	 Catch:{ all -> 0x0042 }
        r9 = "no signature in file (no footer)";	 Catch:{ all -> 0x0042 }
        r2.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r2;	 Catch:{ all -> 0x0042 }
    L_0x0042:
        r2 = move-exception;
        r8.close();
        throw r2;
    L_0x0047:
        r2 = 4;
        r2 = r16[r2];	 Catch:{ all -> 0x0042 }
        r2 = r2 & 255;	 Catch:{ all -> 0x0042 }
        r9 = 5;	 Catch:{ all -> 0x0042 }
        r9 = r16[r9];	 Catch:{ all -> 0x0042 }
        r9 = r9 & 255;	 Catch:{ all -> 0x0042 }
        r9 = r9 << 8;	 Catch:{ all -> 0x0042 }
        r5 = r2 | r9;	 Catch:{ all -> 0x0042 }
        r2 = 0;	 Catch:{ all -> 0x0042 }
        r2 = r16[r2];	 Catch:{ all -> 0x0042 }
        r2 = r2 & 255;	 Catch:{ all -> 0x0042 }
        r9 = 1;	 Catch:{ all -> 0x0042 }
        r9 = r16[r9];	 Catch:{ all -> 0x0042 }
        r9 = r9 & 255;	 Catch:{ all -> 0x0042 }
        r9 = r9 << 8;	 Catch:{ all -> 0x0042 }
        r21 = r2 | r9;	 Catch:{ all -> 0x0042 }
        r2 = r5 + 22;	 Catch:{ all -> 0x0042 }
        r15 = new byte[r2];	 Catch:{ all -> 0x0042 }
        r2 = r5 + 22;	 Catch:{ all -> 0x0042 }
        r0 = (long) r2;	 Catch:{ all -> 0x0042 }
        r28 = r0;	 Catch:{ all -> 0x0042 }
        r28 = r3 - r28;	 Catch:{ all -> 0x0042 }
        r0 = r28;	 Catch:{ all -> 0x0042 }
        r8.seek(r0);	 Catch:{ all -> 0x0042 }
        r8.readFully(r15);	 Catch:{ all -> 0x0042 }
        r2 = 0;	 Catch:{ all -> 0x0042 }
        r2 = r15[r2];	 Catch:{ all -> 0x0042 }
        r9 = 80;	 Catch:{ all -> 0x0042 }
        if (r2 != r9) goto L_0x0084;	 Catch:{ all -> 0x0042 }
    L_0x007d:
        r2 = 1;	 Catch:{ all -> 0x0042 }
        r2 = r15[r2];	 Catch:{ all -> 0x0042 }
        r9 = 75;	 Catch:{ all -> 0x0042 }
        if (r2 == r9) goto L_0x008d;	 Catch:{ all -> 0x0042 }
    L_0x0084:
        r2 = new java.security.SignatureException;	 Catch:{ all -> 0x0042 }
        r9 = "no signature in file (bad footer)";	 Catch:{ all -> 0x0042 }
        r2.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r2;	 Catch:{ all -> 0x0042 }
    L_0x008d:
        r2 = 2;	 Catch:{ all -> 0x0042 }
        r2 = r15[r2];	 Catch:{ all -> 0x0042 }
        r9 = 5;	 Catch:{ all -> 0x0042 }
        if (r2 != r9) goto L_0x0084;	 Catch:{ all -> 0x0042 }
    L_0x0093:
        r2 = 3;	 Catch:{ all -> 0x0042 }
        r2 = r15[r2];	 Catch:{ all -> 0x0042 }
        r9 = 6;	 Catch:{ all -> 0x0042 }
        if (r2 != r9) goto L_0x0084;	 Catch:{ all -> 0x0042 }
    L_0x0099:
        r17 = 4;	 Catch:{ all -> 0x0042 }
    L_0x009b:
        r2 = r15.length;	 Catch:{ all -> 0x0042 }
        r2 = r2 + -3;	 Catch:{ all -> 0x0042 }
        r0 = r17;	 Catch:{ all -> 0x0042 }
        if (r0 >= r2) goto L_0x00ca;	 Catch:{ all -> 0x0042 }
    L_0x00a2:
        r2 = r15[r17];	 Catch:{ all -> 0x0042 }
        r9 = 80;	 Catch:{ all -> 0x0042 }
        if (r2 != r9) goto L_0x00c7;	 Catch:{ all -> 0x0042 }
    L_0x00a8:
        r2 = r17 + 1;	 Catch:{ all -> 0x0042 }
        r2 = r15[r2];	 Catch:{ all -> 0x0042 }
        r9 = 75;	 Catch:{ all -> 0x0042 }
        if (r2 != r9) goto L_0x00c7;	 Catch:{ all -> 0x0042 }
    L_0x00b0:
        r2 = r17 + 2;	 Catch:{ all -> 0x0042 }
        r2 = r15[r2];	 Catch:{ all -> 0x0042 }
        r9 = 5;	 Catch:{ all -> 0x0042 }
        if (r2 != r9) goto L_0x00c7;	 Catch:{ all -> 0x0042 }
    L_0x00b7:
        r2 = r17 + 3;	 Catch:{ all -> 0x0042 }
        r2 = r15[r2];	 Catch:{ all -> 0x0042 }
        r9 = 6;	 Catch:{ all -> 0x0042 }
        if (r2 != r9) goto L_0x00c7;	 Catch:{ all -> 0x0042 }
    L_0x00be:
        r2 = new java.security.SignatureException;	 Catch:{ all -> 0x0042 }
        r9 = "EOCD marker found after start of EOCD";	 Catch:{ all -> 0x0042 }
        r2.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r2;	 Catch:{ all -> 0x0042 }
    L_0x00c7:
        r17 = r17 + 1;	 Catch:{ all -> 0x0042 }
        goto L_0x009b;	 Catch:{ all -> 0x0042 }
    L_0x00ca:
        r10 = new sun.security.pkcs.PKCS7;	 Catch:{ all -> 0x0042 }
        r2 = new java.io.ByteArrayInputStream;	 Catch:{ all -> 0x0042 }
        r9 = r5 + 22;	 Catch:{ all -> 0x0042 }
        r9 = r9 - r21;	 Catch:{ all -> 0x0042 }
        r0 = r21;	 Catch:{ all -> 0x0042 }
        r2.<init>(r15, r9, r0);	 Catch:{ all -> 0x0042 }
        r10.<init>(r2);	 Catch:{ all -> 0x0042 }
        r14 = r10.getCertificates();	 Catch:{ all -> 0x0042 }
        if (r14 == 0) goto L_0x00e3;	 Catch:{ all -> 0x0042 }
    L_0x00e0:
        r2 = r14.length;	 Catch:{ all -> 0x0042 }
        if (r2 != 0) goto L_0x00ec;	 Catch:{ all -> 0x0042 }
    L_0x00e3:
        r2 = new java.security.SignatureException;	 Catch:{ all -> 0x0042 }
        r9 = "signature contains no certificates";	 Catch:{ all -> 0x0042 }
        r2.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r2;	 Catch:{ all -> 0x0042 }
    L_0x00ec:
        r2 = 0;	 Catch:{ all -> 0x0042 }
        r13 = r14[r2];	 Catch:{ all -> 0x0042 }
        r20 = r13.getPublicKey();	 Catch:{ all -> 0x0042 }
        r23 = r10.getSignerInfos();	 Catch:{ all -> 0x0042 }
        if (r23 == 0) goto L_0x00fe;	 Catch:{ all -> 0x0042 }
    L_0x00f9:
        r0 = r23;	 Catch:{ all -> 0x0042 }
        r2 = r0.length;	 Catch:{ all -> 0x0042 }
        if (r2 != 0) goto L_0x0107;	 Catch:{ all -> 0x0042 }
    L_0x00fe:
        r2 = new java.security.SignatureException;	 Catch:{ all -> 0x0042 }
        r9 = "signature contains no signedData";	 Catch:{ all -> 0x0042 }
        r2.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r2;	 Catch:{ all -> 0x0042 }
    L_0x0107:
        r2 = 0;	 Catch:{ all -> 0x0042 }
        r22 = r23[r2];	 Catch:{ all -> 0x0042 }
        r25 = 0;	 Catch:{ all -> 0x0042 }
        if (r32 != 0) goto L_0x0110;	 Catch:{ all -> 0x0042 }
    L_0x010e:
        r32 = DEFAULT_KEYSTORE;	 Catch:{ all -> 0x0042 }
    L_0x0110:
        r24 = getTrustedCerts(r32);	 Catch:{ all -> 0x0042 }
        r12 = r24.iterator();	 Catch:{ all -> 0x0042 }
    L_0x0118:
        r2 = r12.hasNext();	 Catch:{ all -> 0x0042 }
        if (r2 == 0) goto L_0x0132;	 Catch:{ all -> 0x0042 }
    L_0x011e:
        r11 = r12.next();	 Catch:{ all -> 0x0042 }
        r11 = (java.security.cert.X509Certificate) r11;	 Catch:{ all -> 0x0042 }
        r2 = r11.getPublicKey();	 Catch:{ all -> 0x0042 }
        r0 = r20;	 Catch:{ all -> 0x0042 }
        r2 = r2.equals(r0);	 Catch:{ all -> 0x0042 }
        if (r2 == 0) goto L_0x0118;	 Catch:{ all -> 0x0042 }
    L_0x0130:
        r25 = 1;	 Catch:{ all -> 0x0042 }
    L_0x0132:
        if (r25 != 0) goto L_0x013d;	 Catch:{ all -> 0x0042 }
    L_0x0134:
        r2 = new java.security.SignatureException;	 Catch:{ all -> 0x0042 }
        r9 = "signature doesn't match any trusted key";	 Catch:{ all -> 0x0042 }
        r2.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r2;	 Catch:{ all -> 0x0042 }
    L_0x013d:
        r28 = 0;	 Catch:{ all -> 0x0042 }
        r0 = r28;	 Catch:{ all -> 0x0042 }
        r8.seek(r0);	 Catch:{ all -> 0x0042 }
        r19 = r31;	 Catch:{ all -> 0x0042 }
        r2 = new android.os.RecoverySystem$1;	 Catch:{ all -> 0x0042 }
        r9 = r31;	 Catch:{ all -> 0x0042 }
        r2.<init>(r3, r5, r6, r8, r9);	 Catch:{ all -> 0x0042 }
        r0 = r22;	 Catch:{ all -> 0x0042 }
        r26 = r10.verify(r0, r2);	 Catch:{ all -> 0x0042 }
        r18 = java.lang.Thread.interrupted();	 Catch:{ all -> 0x0042 }
        if (r31 == 0) goto L_0x0160;	 Catch:{ all -> 0x0042 }
    L_0x0159:
        r2 = 100;	 Catch:{ all -> 0x0042 }
        r0 = r31;	 Catch:{ all -> 0x0042 }
        r0.onProgress(r2);	 Catch:{ all -> 0x0042 }
    L_0x0160:
        if (r18 == 0) goto L_0x016b;	 Catch:{ all -> 0x0042 }
    L_0x0162:
        r2 = new java.security.SignatureException;	 Catch:{ all -> 0x0042 }
        r9 = "verification was interrupted";	 Catch:{ all -> 0x0042 }
        r2.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r2;	 Catch:{ all -> 0x0042 }
    L_0x016b:
        if (r26 != 0) goto L_0x0176;	 Catch:{ all -> 0x0042 }
    L_0x016d:
        r2 = new java.security.SignatureException;	 Catch:{ all -> 0x0042 }
        r9 = "signature digest verification failed";	 Catch:{ all -> 0x0042 }
        r2.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r2;	 Catch:{ all -> 0x0042 }
    L_0x0176:
        r8.close();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.verifyPackage(java.io.File, android.os.RecoverySystem$ProgressListener, java.io.File):void");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static void processPackage(android.content.Context r6, java.io.File r7, android.os.RecoverySystem.ProgressListener r8, android.os.Handler r9) throws java.io.IOException {
        /*
        r0 = r7.getCanonicalPath();
        r4 = "/data/";
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x000e;
    L_0x000d:
        return;
    L_0x000e:
        r4 = "recovery";
        r3 = r6.getSystemService(r4);
        r3 = (android.os.RecoverySystem) r3;
        r2 = 0;
        if (r8 == 0) goto L_0x0022;
    L_0x001a:
        if (r9 == 0) goto L_0x0031;
    L_0x001c:
        r1 = r9;
    L_0x001d:
        r2 = new android.os.RecoverySystem$2;
        r2.<init>(r1, r8);
    L_0x0022:
        r4 = r3.uncrypt(r0, r2);
        if (r4 != 0) goto L_0x003b;
    L_0x0028:
        r4 = new java.io.IOException;
        r5 = "process package failed";
        r4.<init>(r5);
        throw r4;
    L_0x0031:
        r1 = new android.os.Handler;
        r4 = r6.getMainLooper();
        r1.<init>(r4);
        goto L_0x001d;
    L_0x003b:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.processPackage(android.content.Context, java.io.File, android.os.RecoverySystem$ProgressListener, android.os.Handler):void");
    }

    public static void processPackage(Context context, File packageFile, ProgressListener listener) throws IOException {
        processPackage(context, packageFile, listener, null);
    }

    public static void installPackage(Context context, File packageFile) throws IOException {
        installPackage(context, packageFile, false);
    }

    public static void installPackage(Context context, File packageFile, boolean processed) throws IOException {
        synchronized (sRequestLock) {
            LOG_FILE.delete();
            UNCRYPT_PACKAGE_FILE.delete();
            String filename = packageFile.getCanonicalPath();
            Log.w(TAG, "!!! REBOOTING TO INSTALL " + filename + " !!!");
            boolean securityUpdate = filename.endsWith("_s.zip");
            if (filename.startsWith("/data/")) {
                if (!processed) {
                    FileWriter uncryptFile = new FileWriter(UNCRYPT_PACKAGE_FILE);
                    try {
                        uncryptFile.write(filename + "\n");
                        if (!(UNCRYPT_PACKAGE_FILE.setReadable(true, false) && UNCRYPT_PACKAGE_FILE.setWritable(true, false))) {
                            Log.e(TAG, "Error setting permission for " + UNCRYPT_PACKAGE_FILE);
                        }
                        BLOCK_MAP_FILE.delete();
                    } finally {
                        uncryptFile.close();
                    }
                } else if (!BLOCK_MAP_FILE.exists()) {
                    Log.e(TAG, "Package claimed to have been processed but failed to find the block map file.");
                    throw new IOException("Failed to find block map file");
                }
                filename = "@/cache/recovery/block.map";
            }
            String filenameArg = "--update_package=" + filename + "\n";
            String securityArg = "--security\n";
            String command = filenameArg + ("--locale=" + Locale.getDefault().toString() + "\n");
            if (securityUpdate) {
                command = command + "--security\n";
            }
            if (((RecoverySystem) context.getSystemService("recovery")).setupBcb(command)) {
                ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot(PowerManager.REBOOT_RECOVERY_UPDATE);
                throw new IOException("Reboot failed (no permissions?)");
            }
            throw new IOException("Setup BCB failed");
        }
    }

    public static void scheduleUpdateOnBoot(Context context, File packageFile) throws IOException {
        String filename = packageFile.getCanonicalPath();
        boolean securityUpdate = filename.endsWith("_s.zip");
        if (filename.startsWith("/data/")) {
            filename = "@/cache/recovery/block.map";
        }
        String filenameArg = "--update_package=" + filename + "\n";
        String securityArg = "--security\n";
        String command = filenameArg + ("--locale=" + Locale.getDefault().toString() + "\n");
        if (securityUpdate) {
            command = command + "--security\n";
        }
        if (!((RecoverySystem) context.getSystemService("recovery")).setupBcb(command)) {
            throw new IOException("schedule update on boot failed");
        }
    }

    public static void cancelScheduledUpdate(Context context) throws IOException {
        if (!((RecoverySystem) context.getSystemService("recovery")).clearBcb()) {
            throw new IOException("cancel scheduled update failed");
        }
    }

    public static void rebootWipeUserData(Context context) throws IOException {
        rebootWipeUserData(context, false, context.getPackageName(), false);
    }

    public static void rebootWipeUserData(Context context, String reason) throws IOException {
        rebootWipeUserData(context, false, reason, false);
    }

    public static void rebootWipeUserData(Context context, boolean shutdown) throws IOException {
        rebootWipeUserData(context, shutdown, context.getPackageName(), false);
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static void rebootWipeUserData(android.content.Context r20, boolean r21, java.lang.String r22, boolean r23) throws java.io.IOException {
        /*
        r1 = "user";
        r0 = r20;
        r19 = r0.getSystemService(r1);
        r19 = (android.os.UserManager) r19;
        if (r23 != 0) goto L_0x0021;
    L_0x000d:
        r1 = "no_factory_reset";
        r0 = r19;
        r1 = r0.hasUserRestriction(r1);
        if (r1 == 0) goto L_0x0021;
    L_0x0018:
        r1 = new java.lang.SecurityException;
        r3 = "Wiping data is not allowed for this user.";
        r1.<init>(r3);
        throw r1;
    L_0x0021:
        r11 = new android.os.ConditionVariable;
        r11.<init>();
        r2 = new android.content.Intent;
        r1 = "android.intent.action.MASTER_CLEAR_NOTIFICATION";
        r2.<init>(r1);
        r1 = 268435456; // 0x10000000 float:2.5243549E-29 double:1.32624737E-315;
        r2.addFlags(r1);
        r3 = android.os.UserHandle.SYSTEM;
        r4 = "android.permission.MASTER_CLEAR";
        r5 = new android.os.RecoverySystem$3;
        r5.<init>(r11);
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r1 = r20;
        r1.sendOrderedBroadcastAsUser(r2, r3, r4, r5, r6, r7, r8, r9);
        r11.block();
        r18 = 0;
        if (r21 == 0) goto L_0x0050;
    L_0x004d:
        r18 = "--shutdown_after";
    L_0x0050:
        r16 = 0;
        r1 = android.text.TextUtils.isEmpty(r22);
        if (r1 != 0) goto L_0x0070;
    L_0x0058:
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r3 = "--reason=";
        r1 = r1.append(r3);
        r3 = sanitizeArg(r22);
        r1 = r1.append(r3);
        r16 = r1.toString();
    L_0x0070:
        r1 = "mount";	 Catch:{ RemoteException -> 0x00fc }
        r17 = android.os.ServiceManager.getService(r1);	 Catch:{ RemoteException -> 0x00fc }
        r14 = android.os.storage.IMountService.Stub.asInterface(r17);	 Catch:{ RemoteException -> 0x00fc }
        r1 = "SystemLocale";	 Catch:{ RemoteException -> 0x00fc }
        r3 = "";	 Catch:{ RemoteException -> 0x00fc }
        r14.setField(r1, r3);	 Catch:{ RemoteException -> 0x00fc }
        r1 = "PatternVisible";	 Catch:{ RemoteException -> 0x00fc }
        r3 = "";	 Catch:{ RemoteException -> 0x00fc }
        r14.setField(r1, r3);	 Catch:{ RemoteException -> 0x00fc }
        r1 = "PasswordVisible";	 Catch:{ RemoteException -> 0x00fc }
        r3 = "";	 Catch:{ RemoteException -> 0x00fc }
        r14.setField(r1, r3);	 Catch:{ RemoteException -> 0x00fc }
    L_0x0096:
        r1 = "ro.oppo.operator";
        r3 = "EX";
        r10 = android.os.SystemProperties.get(r1, r3);
        r1 = "EX";
        r1 = r10.equals(r1);
        if (r1 != 0) goto L_0x00af;
    L_0x00a9:
        r1 = "persist.sys.netlockoperator";
        android.os.SystemProperties.set(r1, r10);
    L_0x00af:
        r1 = "ro.oppo.region.netlock";
        r3 = "EX";
        r15 = android.os.SystemProperties.get(r1, r3);
        r1 = "EX";
        r1 = r15.equals(r1);
        if (r1 != 0) goto L_0x00c8;
    L_0x00c2:
        r1 = "persist.sys.netlockoperator";
        android.os.SystemProperties.set(r1, r15);
    L_0x00c8:
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r3 = "--locale=";
        r1 = r1.append(r3);
        r3 = java.util.Locale.getDefault();
        r3 = r3.toString();
        r1 = r1.append(r3);
        r13 = r1.toString();
        r1 = 4;
        r1 = new java.lang.String[r1];
        r3 = 0;
        r1[r3] = r18;
        r3 = "--wipe_data";
        r4 = 1;
        r1[r4] = r3;
        r3 = 2;
        r1[r3] = r16;
        r3 = 3;
        r1[r3] = r13;
        r0 = r20;
        bootCommand(r0, r1);
        return;
    L_0x00fc:
        r12 = move-exception;
        r12.printStackTrace();
        goto L_0x0096;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.rebootWipeUserData(android.content.Context, boolean, java.lang.String, boolean):void");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static void rebootFormatUserData(android.content.Context r14, boolean r15, java.lang.String r16, boolean r17) throws java.io.IOException {
        /*
        r0 = "user";
        r13 = r14.getSystemService(r0);
        r13 = (android.os.UserManager) r13;
        if (r17 != 0) goto L_0x001d;
    L_0x000b:
        r0 = "no_factory_reset";
        r0 = r13.hasUserRestriction(r0);
        if (r0 == 0) goto L_0x001d;
    L_0x0014:
        r0 = new java.lang.SecurityException;
        r2 = "Wiping data is not allowed for this user.";
        r0.<init>(r2);
        throw r0;
    L_0x001d:
        r9 = new android.os.ConditionVariable;
        r9.<init>();
        r1 = new android.content.Intent;
        r0 = "android.intent.action.MASTER_CLEAR_NOTIFICATION";
        r1.<init>(r0);
        r0 = 268435456; // 0x10000000 float:2.5243549E-29 double:1.32624737E-315;
        r1.addFlags(r0);
        r2 = android.os.UserHandle.OWNER;
        r3 = "android.permission.MASTER_CLEAR";
        r4 = new android.os.RecoverySystem$4;
        r4.<init>(r9);
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r0 = r14;
        r0.sendOrderedBroadcastAsUser(r1, r2, r3, r4, r5, r6, r7, r8);
        r9.block();
        r12 = 0;
        if (r15 == 0) goto L_0x004a;
    L_0x0047:
        r12 = "--shutdown_after";
    L_0x004a:
        r11 = 0;
        r0 = android.text.TextUtils.isEmpty(r16);
        if (r0 != 0) goto L_0x0069;
    L_0x0051:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r2 = "--reason=";
        r0 = r0.append(r2);
        r2 = sanitizeArg(r16);
        r0 = r0.append(r2);
        r11 = r0.toString();
    L_0x0069:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r2 = "--locale=";
        r0 = r0.append(r2);
        r2 = java.util.Locale.getDefault();
        r2 = r2.toString();
        r0 = r0.append(r2);
        r10 = r0.toString();
        r0 = 4;
        r0 = new java.lang.String[r0];
        r2 = 0;
        r0[r2] = r12;
        r2 = "--format_data";
        r3 = 1;
        r0[r3] = r2;
        r2 = 2;
        r0[r2] = r11;
        r2 = 3;
        r0[r2] = r10;
        bootCommand(r14, r0);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.rebootFormatUserData(android.content.Context, boolean, java.lang.String, boolean):void");
    }

    public static void rebootWipeCache(Context context) throws IOException {
        rebootWipeCache(context, context.getPackageName());
    }

    public static void rebootWipeCache(Context context, String reason) throws IOException {
        String reasonArg = null;
        if (!TextUtils.isEmpty(reason)) {
            reasonArg = "--reason=" + sanitizeArg(reason);
        }
        String localeArg = "--locale=" + Locale.getDefault().toString();
        String[] strArr = new String[3];
        strArr[0] = "--wipe_cache";
        strArr[1] = reasonArg;
        strArr[2] = localeArg;
        bootCommand(context, strArr);
    }

    public static void rebootWipeAb(Context context, File packageFile, String reason) throws IOException {
        String reasonArg = null;
        if (!TextUtils.isEmpty(reason)) {
            reasonArg = "--reason=" + sanitizeArg(reason);
        }
        String filenameArg = "--wipe_package=" + packageFile.getCanonicalPath();
        String localeArg = "--locale=" + Locale.getDefault().toString();
        String[] strArr = new String[4];
        strArr[0] = "--wipe_ab";
        strArr[1] = filenameArg;
        strArr[2] = reasonArg;
        strArr[3] = localeArg;
        bootCommand(context, strArr);
    }

    private static void bootCommand(Context context, String... args) throws IOException {
        LOG_FILE.delete();
        StringBuilder command = new StringBuilder();
        for (String arg : args) {
            if (!TextUtils.isEmpty(arg)) {
                command.append(arg);
                command.append("\n");
            }
        }
        ((RecoverySystem) context.getSystemService("recovery")).rebootRecoveryWithCommand(command.toString());
        throw new IOException("Reboot failed (no permissions?)");
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a0 A:{SYNTHETIC, Splitter: B:33:0x00a0} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01ab A:{Catch:{ IOException -> 0x00a6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00a5 A:{SYNTHETIC, Splitter: B:36:0x00a5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void parseLastInstallLog(Context context) {
        Throwable th;
        Throwable th2 = null;
        BufferedReader in = null;
        IOException e;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(LAST_INSTALL_FILE));
            int bytesWrittenInMiB = -1;
            int bytesStashedInMiB = -1;
            int timeTotal = -1;
            int uncryptTime = -1;
            int sourceVersion = -1;
            while (true) {
                try {
                    String line = in2.readLine();
                    if (line == null) {
                        break;
                    }
                    int numIndex = line.indexOf(58);
                    if (numIndex != -1 && numIndex + 1 < line.length()) {
                        try {
                            long parsedNum = Long.parseLong(line.substring(numIndex + 1).trim());
                            try {
                                int scaled;
                                if (line.startsWith("bytes")) {
                                    scaled = Math.toIntExact(parsedNum / 1048576);
                                } else {
                                    scaled = Math.toIntExact(parsedNum);
                                }
                                if (line.startsWith(DropBoxManager.EXTRA_TIME)) {
                                    timeTotal = scaled;
                                } else if (line.startsWith("uncrypt_time")) {
                                    uncryptTime = scaled;
                                } else if (line.startsWith("source_build")) {
                                    sourceVersion = scaled;
                                } else if (line.startsWith("bytes_written")) {
                                    if (bytesWrittenInMiB == -1) {
                                        bytesWrittenInMiB = scaled;
                                    } else {
                                        bytesWrittenInMiB += scaled;
                                    }
                                } else if (line.startsWith("bytes_stashed")) {
                                    if (bytesStashedInMiB == -1) {
                                        bytesStashedInMiB = scaled;
                                    } else {
                                        bytesStashedInMiB += scaled;
                                    }
                                }
                            } catch (ArithmeticException e2) {
                                Log.e(TAG, "Number overflows in " + line);
                            }
                        } catch (NumberFormatException e3) {
                            Log.e(TAG, "Failed to parse numbers in " + line);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (in != null) {
                    }
                    if (th2 == null) {
                    }
                }
            }
            if (timeTotal != -1) {
                MetricsLogger.histogram(context, "ota_time_total", timeTotal);
            }
            if (uncryptTime != -1) {
                MetricsLogger.histogram(context, "ota_uncrypt_time", uncryptTime);
            }
            if (sourceVersion != -1) {
                MetricsLogger.histogram(context, "ota_source_version", sourceVersion);
            }
            if (bytesWrittenInMiB != -1) {
                MetricsLogger.histogram(context, "ota_written_in_MiBs", bytesWrittenInMiB);
            }
            if (bytesStashedInMiB != -1) {
                MetricsLogger.histogram(context, "ota_stashed_in_MiBs", bytesStashedInMiB);
            }
            if (in2 != null) {
                try {
                    in2.close();
                } catch (Throwable th4) {
                    th2 = th4;
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (IOException e4) {
                    e = e4;
                    in = in2;
                }
            } else {
                return;
            }
            Log.e(TAG, "Failed to read lines in last_install", e);
        } catch (Throwable th5) {
            th = th5;
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (IOException e5) {
                    e = e5;
                }
            } else {
                throw th;
            }
        }
    }

    public static String handleAftermath(Context context) {
        String log = null;
        try {
            log = FileUtils.readTextFile(LOG_FILE, Color.RED, "...\n");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No recovery log file");
        } catch (IOException e2) {
            Log.e(TAG, "Error reading recovery log", e2);
        }
        if (log != null) {
            parseLastInstallLog(context);
        }
        boolean reservePackage = BLOCK_MAP_FILE.exists();
        if (!reservePackage && UNCRYPT_PACKAGE_FILE.exists()) {
            String filename = null;
            try {
                filename = FileUtils.readTextFile(UNCRYPT_PACKAGE_FILE, 0, null);
            } catch (IOException e22) {
                Log.e(TAG, "Error reading uncrypt file", e22);
            }
            if (filename != null && filename.startsWith("/data")) {
                if (UNCRYPT_PACKAGE_FILE.delete()) {
                    Log.i(TAG, "Deleted: " + filename);
                } else {
                    Log.e(TAG, "Can't delete: " + filename);
                }
            }
        }
        String[] names = RECOVERY_DIR.list();
        int i = 0;
        while (names != null && i < names.length) {
            if (!(names[i].startsWith(LAST_PREFIX) || ((reservePackage && names[i].equals(BLOCK_MAP_FILE.getName())) || (reservePackage && names[i].equals(UNCRYPT_PACKAGE_FILE.getName()))))) {
                recursiveDelete(new File(RECOVERY_DIR, names[i]));
            }
            i++;
        }
        return log;
    }

    private static void recursiveDelete(File name) {
        if (name.isDirectory()) {
            String[] files = name.list();
            int i = 0;
            while (files != null && i < files.length) {
                recursiveDelete(new File(name, files[i]));
                i++;
            }
        }
        if (name.delete()) {
            Log.i(TAG, "Deleted: " + name);
        } else {
            Log.e(TAG, "Can't delete: " + name);
        }
    }

    private boolean uncrypt(String packageFile, IRecoverySystemProgressListener listener) {
        try {
            return this.mService.uncrypt(packageFile, listener);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean setupBcb(String command) {
        try {
            return this.mService.setupBcb(command);
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean clearBcb() {
        try {
            return this.mService.clearBcb();
        } catch (RemoteException e) {
            return false;
        }
    }

    private void rebootRecoveryWithCommand(String command) {
        try {
            this.mService.rebootRecoveryWithCommand(command);
        } catch (RemoteException e) {
        }
    }

    private static String sanitizeArg(String arg) {
        return arg.replace(0, '?').replace(10, '?');
    }

    public RecoverySystem() {
        this.mService = null;
    }

    public RecoverySystem(IRecoverySystem service) {
        this.mService = service;
    }
}
