package java.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import sun.misc.Unsafe;

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
    */
public class ForkJoinPool extends AbstractExecutorService {
    private static final int ABASE = 0;
    private static final long AC_MASK = -281474976710656L;
    private static final int AC_SHIFT = 48;
    private static final long AC_UNIT = 281474976710656L;
    private static final long ADD_WORKER = 140737488355328L;
    private static final int ASHIFT = 0;
    private static final int COMMON_MAX_SPARES = 0;
    static final int COMMON_PARALLELISM = 0;
    private static final long CTL = 0;
    private static final int DEFAULT_COMMON_MAX_SPARES = 256;
    static final int EVENMASK = 65534;
    static final int FIFO_QUEUE = Integer.MIN_VALUE;
    private static final long IDLE_TIMEOUT_MS = 2000;
    static final int IS_OWNED = 1;
    static final int LIFO_QUEUE = 0;
    static final int MAX_CAP = 32767;
    static final int MODE_MASK = -65536;
    static final int POLL_LIMIT = 1023;
    private static final long RUNSTATE = 0;
    private static final int SEED_INCREMENT = -1640531527;
    private static final int SHUTDOWN = Integer.MIN_VALUE;
    static final int SMASK = 65535;
    static final int SPARE_WORKER = 131072;
    private static final long SP_MASK = 4294967295L;
    static final int SQMASK = 126;
    static final int SS_SEQ = 65536;
    private static final int STARTED = 1;
    private static final int STOP = 2;
    private static final long TC_MASK = 281470681743360L;
    private static final int TC_SHIFT = 32;
    private static final long TC_UNIT = 4294967296L;
    private static final int TERMINATED = 4;
    private static final long TIMEOUT_SLOP_MS = 20;
    private static final Unsafe U = null;
    private static final long UC_MASK = -4294967296L;
    static final int UNREGISTERED = 262144;
    static final int UNSIGNALLED = Integer.MIN_VALUE;
    static final ForkJoinPool common = null;
    public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory = null;
    static final RuntimePermission modifyThreadPermission = null;
    private static int poolNumberSequence;
    AuxState auxState;
    final int config;
    volatile long ctl;
    final ForkJoinWorkerThreadFactory factory;
    volatile int runState;
    final UncaughtExceptionHandler ueh;
    volatile WorkQueue[] workQueues;
    final String workerNamePrefix;

    public interface ManagedBlocker {
        boolean block() throws InterruptedException;

        boolean isReleasable();
    }

    private static final class AuxState extends ReentrantLock {
        private static final long serialVersionUID = -6001602636862214147L;
        long indexSeed;
        volatile long stealCount;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.AuxState.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AuxState() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.AuxState.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.AuxState.<init>():void");
        }
    }

    public interface ForkJoinWorkerThreadFactory {
        ForkJoinWorkerThread newThread(ForkJoinPool forkJoinPool);
    }

    private static final class DefaultForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
        /* synthetic */ DefaultForkJoinWorkerThreadFactory(DefaultForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory) {
            this();
        }

        private DefaultForkJoinWorkerThreadFactory() {
        }

        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool);
        }
    }

    private static final class EmptyTask extends ForkJoinTask<Void> {
        private static final long serialVersionUID = -7721805057305804111L;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.EmptyTask.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        EmptyTask() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.EmptyTask.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.EmptyTask.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.EmptyTask.getRawResult():java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object getRawResult() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.EmptyTask.getRawResult():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.EmptyTask.getRawResult():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.EmptyTask.setRawResult(java.lang.Object):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ void setRawResult(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.EmptyTask.setRawResult(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.EmptyTask.setRawResult(java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.EmptyTask.setRawResult(java.lang.Void):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public final void setRawResult(java.lang.Void r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.EmptyTask.setRawResult(java.lang.Void):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.EmptyTask.setRawResult(java.lang.Void):void");
        }

        public final Void getRawResult() {
            return null;
        }

        public final boolean exec() {
            return true;
        }
    }

    private static final class InnocuousForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
        private static final AccessControlContext innocuousAcc = null;

        /* renamed from: java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory$1 */
        class AnonymousClass1 implements PrivilegedAction<ForkJoinWorkerThread> {
            final /* synthetic */ InnocuousForkJoinWorkerThreadFactory this$1;
            final /* synthetic */ ForkJoinPool val$pool;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.<init>(java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory, java.util.concurrent.ForkJoinPool):void, dex: 
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
            AnonymousClass1(java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory r1, java.util.concurrent.ForkJoinPool r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.<init>(java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory, java.util.concurrent.ForkJoinPool):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.<init>(java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory, java.util.concurrent.ForkJoinPool):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.run():java.lang.Object, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public /* bridge */ /* synthetic */ java.lang.Object run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.run():java.lang.Object, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.run():java.lang.Object");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.run():java.util.concurrent.ForkJoinWorkerThread, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public java.util.concurrent.ForkJoinWorkerThread run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.run():java.util.concurrent.ForkJoinWorkerThread, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.1.run():java.util.concurrent.ForkJoinWorkerThread");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private InnocuousForkJoinWorkerThreadFactory() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<init>(java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        /* synthetic */ InnocuousForkJoinWorkerThreadFactory(java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<init>(java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.<init>(java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory):void");
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
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public final java.util.concurrent.ForkJoinWorkerThread newThread(java.util.concurrent.ForkJoinPool r3) {
            /*
            r2 = this;
            r0 = new java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory$1;
            r0.<init>(r2, r3);
            r1 = innocuousAcc;
            r0 = java.security.AccessController.doPrivileged(r0, r1);
            r0 = (java.util.concurrent.ForkJoinWorkerThread) r0;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.InnocuousForkJoinWorkerThreadFactory.newThread(java.util.concurrent.ForkJoinPool):java.util.concurrent.ForkJoinWorkerThread");
        }
    }

    static final class WorkQueue {
        private static final int ABASE = 0;
        private static final int ASHIFT = 0;
        static final int INITIAL_QUEUE_CAPACITY = 8192;
        static final int MAXIMUM_QUEUE_CAPACITY = 67108864;
        private static final long QLOCK = 0;
        private static final Unsafe U = null;
        ForkJoinTask<?>[] array;
        volatile int base;
        int config;
        volatile ForkJoinTask<?> currentJoin;
        volatile ForkJoinTask<?> currentSteal;
        int hint;
        int nsteals;
        final ForkJoinWorkerThread owner;
        volatile Thread parker;
        final ForkJoinPool pool;
        volatile int qlock;
        volatile int scanState;
        int stackPred;
        int top;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ForkJoinPool.WorkQueue.<init>(java.util.concurrent.ForkJoinPool, java.util.concurrent.ForkJoinWorkerThread):void, dex: 
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
        WorkQueue(java.util.concurrent.ForkJoinPool r1, java.util.concurrent.ForkJoinWorkerThread r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ForkJoinPool.WorkQueue.<init>(java.util.concurrent.ForkJoinPool, java.util.concurrent.ForkJoinWorkerThread):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.<init>(java.util.concurrent.ForkJoinPool, java.util.concurrent.ForkJoinWorkerThread):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.growAndSharedPush(java.util.concurrent.ForkJoinTask):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private void growAndSharedPush(java.util.concurrent.ForkJoinTask<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.growAndSharedPush(java.util.concurrent.ForkJoinTask):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.growAndSharedPush(java.util.concurrent.ForkJoinTask):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.cancelAll():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final void cancelAll() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.cancelAll():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.cancelAll():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.getPoolIndex():int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final int getPoolIndex() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.getPoolIndex():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.getPoolIndex():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.growArray():java.util.concurrent.ForkJoinTask<?>[], dex: 
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
        final java.util.concurrent.ForkJoinTask<?>[] growArray() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.growArray():java.util.concurrent.ForkJoinTask<?>[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.growArray():java.util.concurrent.ForkJoinTask<?>[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.isApparentlyUnblocked():boolean, dex: 
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
        final boolean isApparentlyUnblocked() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.isApparentlyUnblocked():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.isApparentlyUnblocked():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.isEmpty():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final boolean isEmpty() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.isEmpty():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.isEmpty():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.localPollAndExec():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final void localPollAndExec() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.localPollAndExec():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.localPollAndExec():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.localPopAndExec():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final void localPopAndExec() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.localPopAndExec():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.localPopAndExec():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.nextLocalTask():java.util.concurrent.ForkJoinTask<?>, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final java.util.concurrent.ForkJoinTask<?> nextLocalTask() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.nextLocalTask():java.util.concurrent.ForkJoinTask<?>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.nextLocalTask():java.util.concurrent.ForkJoinTask<?>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.peek():java.util.concurrent.ForkJoinTask<?>, dex: 
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
        final java.util.concurrent.ForkJoinTask<?> peek() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.peek():java.util.concurrent.ForkJoinTask<?>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.peek():java.util.concurrent.ForkJoinTask<?>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.poll():java.util.concurrent.ForkJoinTask<?>, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final java.util.concurrent.ForkJoinTask<?> poll() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.poll():java.util.concurrent.ForkJoinTask<?>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.poll():java.util.concurrent.ForkJoinTask<?>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.pollAndExecCC(java.util.concurrent.CountedCompleter):int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final int pollAndExecCC(java.util.concurrent.CountedCompleter<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.pollAndExecCC(java.util.concurrent.CountedCompleter):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.pollAndExecCC(java.util.concurrent.CountedCompleter):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.pollAt(int):java.util.concurrent.ForkJoinTask<?>, dex: 
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
        final java.util.concurrent.ForkJoinTask<?> pollAt(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.pollAt(int):java.util.concurrent.ForkJoinTask<?>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.pollAt(int):java.util.concurrent.ForkJoinTask<?>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.pop():java.util.concurrent.ForkJoinTask<?>, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final java.util.concurrent.ForkJoinTask<?> pop() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.pop():java.util.concurrent.ForkJoinTask<?>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.pop():java.util.concurrent.ForkJoinTask<?>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.popCC(java.util.concurrent.CountedCompleter, int):java.util.concurrent.CountedCompleter<?>, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final java.util.concurrent.CountedCompleter<?> popCC(java.util.concurrent.CountedCompleter<?> r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.popCC(java.util.concurrent.CountedCompleter, int):java.util.concurrent.CountedCompleter<?>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.popCC(java.util.concurrent.CountedCompleter, int):java.util.concurrent.CountedCompleter<?>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.push(java.util.concurrent.ForkJoinTask):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final void push(java.util.concurrent.ForkJoinTask<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.push(java.util.concurrent.ForkJoinTask):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.push(java.util.concurrent.ForkJoinTask):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.queueSize():int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final int queueSize() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.queueSize():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.queueSize():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.runTask(java.util.concurrent.ForkJoinTask):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final void runTask(java.util.concurrent.ForkJoinTask<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.WorkQueue.runTask(java.util.concurrent.ForkJoinTask):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.runTask(java.util.concurrent.ForkJoinTask):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: java.util.concurrent.ForkJoinPool.WorkQueue.sharedPush(java.util.concurrent.ForkJoinTask):int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final int sharedPush(java.util.concurrent.ForkJoinTask<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: java.util.concurrent.ForkJoinPool.WorkQueue.sharedPush(java.util.concurrent.ForkJoinTask):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.sharedPush(java.util.concurrent.ForkJoinTask):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.transferStealCount(java.util.concurrent.ForkJoinPool):void, dex: 
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
        final void transferStealCount(java.util.concurrent.ForkJoinPool r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ForkJoinPool.WorkQueue.transferStealCount(java.util.concurrent.ForkJoinPool):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.transferStealCount(java.util.concurrent.ForkJoinPool):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.tryRemoveAndExec(java.util.concurrent.ForkJoinTask):boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final boolean tryRemoveAndExec(java.util.concurrent.ForkJoinTask<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.tryRemoveAndExec(java.util.concurrent.ForkJoinTask):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.tryRemoveAndExec(java.util.concurrent.ForkJoinTask):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.trySharedUnpush(java.util.concurrent.ForkJoinTask):boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final boolean trySharedUnpush(java.util.concurrent.ForkJoinTask<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.trySharedUnpush(java.util.concurrent.ForkJoinTask):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.trySharedUnpush(java.util.concurrent.ForkJoinTask):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.tryUnpush(java.util.concurrent.ForkJoinTask):boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        final boolean tryUnpush(java.util.concurrent.ForkJoinTask<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ForkJoinPool.WorkQueue.tryUnpush(java.util.concurrent.ForkJoinTask):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.WorkQueue.tryUnpush(java.util.concurrent.ForkJoinTask):boolean");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ForkJoinPool.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.<clinit>():void");
    }

    private static void checkPermission() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(modifyThreadPermission);
        }
    }

    private static final synchronized int nextPoolId() {
        int i;
        synchronized (ForkJoinPool.class) {
            i = poolNumberSequence + 1;
            poolNumberSequence = i;
        }
        return i;
    }

    private void tryInitialize(boolean checkTermination) {
        int n = 1;
        if (this.runState == 0) {
            int p = this.config & 65535;
            if (p > 1) {
                n = p - 1;
            }
            n |= n >>> 1;
            n |= n >>> 2;
            n |= n >>> 4;
            n |= n >>> 8;
            n = (((n | (n >>> 16)) + 1) << 1) & 65535;
            AuxState aux = new AuxState();
            WorkQueue[] ws = new WorkQueue[n];
            synchronized (modifyThreadPermission) {
                if (this.runState == 0) {
                    this.workQueues = ws;
                    this.auxState = aux;
                    this.runState = 1;
                }
            }
        }
        if (checkTermination && this.runState < 0) {
            tryTerminate(false, false);
            throw new RejectedExecutionException();
        }
    }

    private boolean createWorker(boolean isSpare) {
        ForkJoinWorkerThreadFactory fac = this.factory;
        Throwable ex = null;
        ForkJoinWorkerThread forkJoinWorkerThread = null;
        if (fac != null) {
            try {
                forkJoinWorkerThread = fac.newThread(this);
                if (forkJoinWorkerThread != null) {
                    if (isSpare) {
                        WorkQueue q = forkJoinWorkerThread.workQueue;
                        if (q != null) {
                            q.config |= 131072;
                        }
                    }
                    forkJoinWorkerThread.start();
                    return true;
                }
            } catch (Throwable rex) {
                ex = rex;
            }
        }
        deregisterWorker(forkJoinWorkerThread, ex);
        return false;
    }

    private void tryAddWorker(long c) {
        while (true) {
            long nc = ((AC_UNIT + c) & AC_MASK) | ((TC_UNIT + c) & TC_MASK);
            if (this.ctl == c) {
                if (U.compareAndSwapLong(this, CTL, c, nc)) {
                    createWorker(false);
                    return;
                }
            }
            c = this.ctl;
            if ((ADD_WORKER & c) == 0 || ((int) c) != 0) {
                return;
            }
        }
    }

    final WorkQueue registerWorker(ForkJoinWorkerThread wt) {
        wt.setDaemon(true);
        UncaughtExceptionHandler handler = this.ueh;
        if (handler != null) {
            wt.setUncaughtExceptionHandler(handler);
        }
        WorkQueue w = new WorkQueue(this, wt);
        int i = 0;
        int mode = this.config & MODE_MASK;
        AuxState aux = this.auxState;
        if (aux != null) {
            aux.lock();
            try {
                long j = aux.indexSeed - 1640531527;
                aux.indexSeed = j;
                int s = (int) j;
                WorkQueue[] ws = this.workQueues;
                if (ws != null) {
                    int n = ws.length;
                    if (n > 0) {
                        int m = n - 1;
                        i = m & ((s << 1) | 1);
                        if (ws[i] != null) {
                            int probes = 0;
                            int step = n <= 4 ? 2 : ((n >>> 1) & EVENMASK) + 2;
                            while (true) {
                                i = (i + step) & m;
                                Object[] ws2;
                                if (ws2[i] == null) {
                                    break;
                                }
                                probes++;
                                if (probes >= n) {
                                    n <<= 1;
                                    ws2 = (WorkQueue[]) Arrays.copyOf(ws2, n);
                                    this.workQueues = ws2;
                                    m = n - 1;
                                    probes = 0;
                                }
                            }
                        }
                        w.hint = s;
                        w.config = i | mode;
                        w.scanState = (2147418112 & s) | i;
                        ws[i] = w;
                    }
                }
                aux.unlock();
            } catch (Throwable th) {
                aux.unlock();
            }
        }
        wt.setName(this.workerNamePrefix.concat(Integer.toString(i >>> 1)));
        return w;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x008d A:{SYNTHETIC, EDGE_INSN: B:52:0x008d->B:28:0x008d ?: BREAK  , EDGE_INSN: B:52:0x008d->B:28:0x008d ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x008f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void deregisterWorker(ForkJoinWorkerThread wt, Throwable ex) {
        WorkQueue[] ws;
        WorkQueue w = null;
        if (wt != null) {
            w = wt.workQueue;
            if (w != null) {
                int idx = w.config & 65535;
                int ns = w.nsteals;
                AuxState aux = this.auxState;
                if (aux != null) {
                    aux.lock();
                    try {
                        ws = this.workQueues;
                        if (ws != null && ws.length > idx && ws[idx] == w) {
                            ws[idx] = null;
                        }
                        aux.stealCount += (long) ns;
                    } finally {
                        aux.unlock();
                    }
                }
            }
        }
        if (w == null || (w.config & 262144) == 0) {
            long c;
            while (true) {
                Unsafe unsafe = U;
                long j = CTL;
                c = this.ctl;
                if (unsafe.compareAndSwapLong(this, j, c, (((c - AC_UNIT) & AC_MASK) | ((c - TC_UNIT) & TC_MASK)) | (SP_MASK & c))) {
                    break;
                }
            }
            if (w != null) {
                w.currentSteal = null;
                w.qlock = -1;
                w.cancelAll();
            }
            while (tryTerminate(false, false) >= 0 && w != null && w.array != null) {
                ws = this.workQueues;
                if (ws != null) {
                    break;
                }
                int wl = ws.length;
                if (wl <= 0) {
                    break;
                }
                c = this.ctl;
                int sp = (int) c;
                if (sp != 0) {
                    if (tryRelease(c, ws[(wl - 1) & sp], AC_UNIT)) {
                        break;
                    }
                } else if (ex != null && (ADD_WORKER & c) != 0) {
                    tryAddWorker(c);
                }
            }
            if (ex != null) {
                ForkJoinTask.helpExpungeStaleExceptions();
                return;
            } else {
                ForkJoinTask.rethrow(ex);
                return;
            }
        }
        if (w != null) {
        }
        while (tryTerminate(false, false) >= 0) {
            ws = this.workQueues;
            if (ws != null) {
            }
        }
        if (ex != null) {
        }
    }

    final void signalWork() {
        while (true) {
            long c = this.ctl;
            if (c < 0) {
                int sp = (int) c;
                if (sp != 0) {
                    WorkQueue[] ws = this.workQueues;
                    if (ws != null) {
                        int i = sp & 65535;
                        if (ws.length > i) {
                            WorkQueue v = ws[i];
                            if (v != null) {
                                int ns = sp & Integer.MAX_VALUE;
                                long nc = (((long) v.stackPred) & SP_MASK) | ((AC_UNIT + c) & UC_MASK);
                                if (sp == v.scanState) {
                                    if (U.compareAndSwapLong(this, CTL, c, nc)) {
                                        v.scanState = ns;
                                        LockSupport.unpark(v.parker);
                                        return;
                                    }
                                }
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                    return;
                } else if ((ADD_WORKER & c) != 0) {
                    tryAddWorker(c);
                    return;
                } else {
                    return;
                }
            }
            return;
        }
    }

    private boolean tryRelease(long c, WorkQueue v, long inc) {
        int sp = (int) c;
        int ns = sp & Integer.MAX_VALUE;
        if (v != null) {
            long nc = (((long) v.stackPred) & SP_MASK) | ((c + inc) & UC_MASK);
            if (sp == v.scanState) {
                if (U.compareAndSwapLong(this, CTL, c, nc)) {
                    v.scanState = ns;
                    LockSupport.unpark(v.parker);
                    return true;
                }
            }
        }
        return false;
    }

    private void tryReactivate(WorkQueue w, WorkQueue[] ws, int r) {
        long c = this.ctl;
        int sp = (int) c;
        if (sp != 0 && w != null && ws != null) {
            int wl = ws.length;
            if (wl > 0 && ((sp ^ r) & 65536) == 0) {
                WorkQueue v = ws[(wl - 1) & sp];
                if (v != null) {
                    long nc = (((long) v.stackPred) & SP_MASK) | ((AC_UNIT + c) & UC_MASK);
                    int ns = sp & Integer.MAX_VALUE;
                    if (w.scanState < 0 && v.scanState == sp) {
                        if (U.compareAndSwapLong(this, CTL, c, nc)) {
                            v.scanState = ns;
                            LockSupport.unpark(v.parker);
                        }
                    }
                }
            }
        }
    }

    private void inactivate(WorkQueue w, int ss) {
        int ns = (65536 + ss) | Integer.MIN_VALUE;
        long lc = ((long) ns) & SP_MASK;
        if (w != null) {
            w.scanState = ns;
            long c;
            long nc;
            do {
                c = this.ctl;
                nc = lc | ((c - AC_UNIT) & UC_MASK);
                w.stackPred = (int) c;
            } while (!U.compareAndSwapLong(this, CTL, c, nc));
        }
    }

    private int awaitWork(WorkQueue w) {
        if (w == null || w.scanState >= 0) {
            return 0;
        }
        long c = this.ctl;
        if (((int) (c >> 48)) + (this.config & 65535) <= 0) {
            return timedAwaitWork(w, c);
        }
        if ((this.runState & 2) != 0) {
            w.qlock = -1;
            return -1;
        } else if (w.scanState >= 0) {
            return 0;
        } else {
            w.parker = Thread.currentThread();
            if (w.scanState < 0) {
                LockSupport.park(this);
            }
            w.parker = null;
            if ((this.runState & 2) != 0) {
                w.qlock = -1;
                return -1;
            } else if (w.scanState >= 0) {
                return 0;
            } else {
                Thread.interrupted();
                return 0;
            }
        }
    }

    private int timedAwaitWork(WorkQueue w, long c) {
        int stat = 0;
        int scale = 1 - ((short) ((int) (c >>> 32)));
        if (scale <= 0) {
            scale = 1;
        }
        long deadline = (((long) scale) * IDLE_TIMEOUT_MS) + System.currentTimeMillis();
        if (this.runState < 0) {
            stat = tryTerminate(false, false);
            if (stat <= 0) {
                return stat;
            }
        }
        if (w == null || w.scanState >= 0) {
            return stat;
        }
        w.parker = Thread.currentThread();
        if (w.scanState < 0) {
            LockSupport.parkUntil(this, deadline);
        }
        w.parker = null;
        if ((this.runState & 2) != 0) {
            w.qlock = -1;
            return -1;
        }
        int ss = w.scanState;
        if (ss >= 0 || Thread.interrupted() || ((int) c) != ss) {
            return stat;
        }
        AuxState aux = this.auxState;
        if (aux == null || this.ctl != c || deadline - System.currentTimeMillis() > TIMEOUT_SLOP_MS) {
            return stat;
        }
        aux.lock();
        try {
            int cfg = w.config;
            int idx = cfg & 65535;
            long nc = ((c - TC_UNIT) & UC_MASK) | (((long) w.stackPred) & SP_MASK);
            if ((this.runState & 2) == 0) {
                WorkQueue[] ws = this.workQueues;
                if (ws != null && idx < ws.length && idx >= 0 && ws[idx] == w) {
                    if (U.compareAndSwapLong(this, CTL, c, nc)) {
                        ws[idx] = null;
                        w.config = 262144 | cfg;
                        w.qlock = -1;
                        stat = -1;
                    }
                }
            }
            aux.unlock();
            return stat;
        } catch (Throwable th) {
            aux.unlock();
        }
    }

    private boolean tryDropSpare(WorkQueue w) {
        if (w != null && w.isEmpty()) {
            WorkQueue[] ws;
            boolean dropped;
            do {
                long c = this.ctl;
                if (((short) ((int) (c >> 32))) > (short) 0) {
                    int sp = (int) c;
                    if (sp != 0 || ((int) (c >> 48)) > 0) {
                        ws = this.workQueues;
                        if (ws != null) {
                            int wl = ws.length;
                            if (wl > 0) {
                                if (sp == 0) {
                                    dropped = U.compareAndSwapLong(this, CTL, c, (((c - AC_UNIT) & AC_MASK) | ((c - TC_UNIT) & TC_MASK)) | (SP_MASK & c));
                                    continue;
                                } else {
                                    WorkQueue v = ws[(wl - 1) & sp];
                                    if (v == null || v.scanState != sp) {
                                        dropped = false;
                                        continue;
                                    } else {
                                        boolean canDrop;
                                        long nc = ((long) v.stackPred) & SP_MASK;
                                        if (w == v || w.scanState >= 0) {
                                            canDrop = true;
                                            nc |= (AC_MASK & c) | ((c - TC_UNIT) & TC_MASK);
                                        } else {
                                            canDrop = false;
                                            nc |= ((AC_UNIT + c) & AC_MASK) | (TC_MASK & c);
                                        }
                                        if (U.compareAndSwapLong(this, CTL, c, nc)) {
                                            v.scanState = Integer.MAX_VALUE & sp;
                                            LockSupport.unpark(v.parker);
                                            dropped = canDrop;
                                            continue;
                                        } else {
                                            dropped = false;
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } while (!dropped);
            int cfg = w.config;
            int idx = cfg & 65535;
            if (idx >= 0 && idx < ws.length && ws[idx] == w) {
                ws[idx] = null;
            }
            w.config = 262144 | cfg;
            w.qlock = -1;
            return true;
        }
        return false;
    }

    final void runWorker(WorkQueue w) {
        w.growArray();
        int bound = (w.config & 131072) != 0 ? 0 : 1023;
        long seed = ((long) w.hint) * -2685821657736338717L;
        if ((this.runState & 2) == 0) {
            long r = seed == 0 ? 1 : seed;
            while (true) {
                if (bound != 0 || !tryDropSpare(w)) {
                    int step = ((int) (r >>> 48)) | 1;
                    r ^= r >>> 12;
                    r ^= r << 25;
                    r ^= r >>> 27;
                    if (scan(w, bound, step, (int) r) < 0 && awaitWork(w) < 0) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private int scan(WorkQueue w, int bound, int step, int r) {
        WorkQueue[] ws = this.workQueues;
        if (ws == null || w == null) {
            return 0;
        }
        int wl = ws.length;
        if (wl <= 0) {
            return 0;
        }
        int m = wl - 1;
        int origin = m & r;
        int idx = origin;
        int npolls = 0;
        int ss = w.scanState;
        while (true) {
            WorkQueue q = ws[idx];
            if (q != null) {
                int i = q.base;
                if (i - q.top < 0) {
                    ForkJoinTask<?>[] a = q.array;
                    if (a != null) {
                        int al = a.length;
                        if (al > 0) {
                            long offset = (((long) ((al - 1) & i)) << ASHIFT) + ((long) ABASE);
                            ForkJoinTask<?> t = (ForkJoinTask) U.getObjectVolatile(a, offset);
                            if (t == null) {
                                return 0;
                            }
                            int b = i + 1;
                            if (i != q.base) {
                                return 0;
                            }
                            if (ss < 0) {
                                tryReactivate(w, ws, r);
                                return 0;
                            } else if (!U.compareAndSwapObject(a, offset, t, null)) {
                                return 0;
                            } else {
                                q.base = b;
                                w.currentSteal = t;
                                if (b != q.top) {
                                    signalWork();
                                }
                                w.runTask(t);
                                npolls++;
                                if (npolls > bound) {
                                    return 0;
                                }
                            }
                        }
                    }
                }
            }
            if (npolls != 0) {
                return 0;
            }
            idx = (idx + step) & m;
            if (idx != origin) {
                continue;
            } else if (ss < 0) {
                return ss;
            } else {
                if (r >= 0) {
                    inactivate(w, ss);
                    return 0;
                }
                r <<= 1;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0055  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final int helpComplete(WorkQueue w, CountedCompleter<?> task, int maxTasks) {
        int s = 0;
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            int wl = ws.length;
            if (wl > 1 && task != null && w != null) {
                int m = wl - 1;
                int mode = w.config;
                int r = ~mode;
                int origin = r & m;
                int k = origin;
                int step = 3;
                int h = 1;
                int oldSum = 0;
                int checkSum = 0;
                while (true) {
                    s = task.status;
                    if (s < 0) {
                        break;
                    }
                    if (h == 1) {
                        CountedCompleter<?> p = w.popCC(task, mode);
                        if (p != null) {
                            p.doExec();
                            if (maxTasks != 0) {
                                maxTasks--;
                                if (maxTasks == 0) {
                                    break;
                                }
                            }
                            origin = k;
                            checkSum = 0;
                            oldSum = 0;
                        }
                    }
                    int i = k | 1;
                    if (i >= 0 && i <= m) {
                        WorkQueue q = ws[i];
                        if (q != null) {
                            h = q.pollAndExecCC(task);
                            if (h < 0) {
                                checkSum += h;
                            }
                            if (h <= 0) {
                                if (h == 1 && maxTasks != 0) {
                                    maxTasks--;
                                    if (maxTasks == 0) {
                                        break;
                                    }
                                }
                                step = (r >>> 16) | 3;
                                r ^= r << 13;
                                r ^= r >>> 17;
                                r ^= r << 5;
                                origin = r & m;
                                k = origin;
                                checkSum = 0;
                                oldSum = 0;
                            } else {
                                k = (k + step) & m;
                                if (k == origin) {
                                    int oldSum2 = checkSum;
                                    if (oldSum == checkSum) {
                                        break;
                                    }
                                    checkSum = 0;
                                    oldSum = oldSum2;
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                    h = 0;
                    if (h <= 0) {
                    }
                }
            }
        }
        return s;
    }

    /* JADX WARNING: Missing block: B:19:0x004b, code:
            r15.hint = r13;
     */
    /* JADX WARNING: Missing block: B:21:0x0051, code:
            if (r22.status < 0) goto L_0x000c;
     */
    /* JADX WARNING: Missing block: B:22:0x0053, code:
            r9 = r24.base;
            r11 = r11 + r9;
            r18 = r24.currentJoin;
            r6 = null;
            r3 = r24.array;
     */
    /* JADX WARNING: Missing block: B:23:0x0063, code:
            if (r3 == null) goto L_0x00d6;
     */
    /* JADX WARNING: Missing block: B:24:0x0065, code:
            r8 = r3.length;
     */
    /* JADX WARNING: Missing block: B:25:0x0066, code:
            if (r8 <= 0) goto L_0x00d6;
     */
    /* JADX WARNING: Missing block: B:26:0x0068, code:
            r4 = (((long) ((r8 - 1) & r9)) << ASHIFT) + ((long) ABASE);
            r6 = (java.util.concurrent.ForkJoinTask) U.getObjectVolatile(r3, r4);
     */
    /* JADX WARNING: Missing block: B:27:0x0082, code:
            if (r6 == null) goto L_0x00d6;
     */
    /* JADX WARNING: Missing block: B:28:0x0084, code:
            r10 = r9 + 1;
     */
    /* JADX WARNING: Missing block: B:29:0x008a, code:
            if (r9 != r24.base) goto L_0x00d5;
     */
    /* JADX WARNING: Missing block: B:31:0x0090, code:
            if (r15.currentJoin != r22) goto L_0x000c;
     */
    /* JADX WARNING: Missing block: B:33:0x0098, code:
            if (r24.currentSteal != r22) goto L_0x000c;
     */
    /* JADX WARNING: Missing block: B:35:0x009e, code:
            if (r22.status < 0) goto L_0x000c;
     */
    /* JADX WARNING: Missing block: B:37:0x00a7, code:
            if (U.compareAndSwapObject(r3, r4, r6, null) == false) goto L_0x0115;
     */
    /* JADX WARNING: Missing block: B:38:0x00a9, code:
            r24.base = r10;
            r33.currentSteal = r6;
            r23 = r33.top;
     */
    /* JADX WARNING: Missing block: B:39:0x00b7, code:
            r6.doExec();
            r33.currentSteal = r21;
     */
    /* JADX WARNING: Missing block: B:40:0x00c4, code:
            if (r34.status >= 0) goto L_0x00fc;
     */
    /* JADX WARNING: Missing block: B:44:0x00d5, code:
            r9 = r10;
     */
    /* JADX WARNING: Missing block: B:45:0x00d6, code:
            if (r6 != null) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:47:0x00dc, code:
            if (r9 != r24.base) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:49:0x00e4, code:
            if ((r9 - r24.top) < 0) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:50:0x00e6, code:
            r22 = r18;
     */
    /* JADX WARNING: Missing block: B:51:0x00e8, code:
            if (r18 != null) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:53:0x00f0, code:
            if (r18 != r24.currentJoin) goto L_0x000c;
     */
    /* JADX WARNING: Missing block: B:54:0x00f2, code:
            r20 = r11;
     */
    /* JADX WARNING: Missing block: B:55:0x00f6, code:
            if (r19 == r11) goto L_0x00c6;
     */
    /* JADX WARNING: Missing block: B:56:0x00f8, code:
            r19 = r20;
     */
    /* JADX WARNING: Missing block: B:58:0x0102, code:
            if (r33.top != r23) goto L_0x0106;
     */
    /* JADX WARNING: Missing block: B:59:0x0104, code:
            r9 = r10;
     */
    /* JADX WARNING: Missing block: B:60:0x0106, code:
            r6 = r33.pop();
     */
    /* JADX WARNING: Missing block: B:61:0x010a, code:
            if (r6 == null) goto L_0x000c;
     */
    /* JADX WARNING: Missing block: B:62:0x010c, code:
            r33.currentSteal = r6;
     */
    /* JADX WARNING: Missing block: B:63:0x0111, code:
            r15 = r24;
     */
    /* JADX WARNING: Missing block: B:64:0x0115, code:
            r9 = r10;
     */
    /* JADX WARNING: Missing block: B:74:0x000c, code:
            continue;
     */
    /* JADX WARNING: Missing block: B:75:0x000c, code:
            continue;
     */
    /* JADX WARNING: Missing block: B:76:0x000c, code:
            continue;
     */
    /* JADX WARNING: Missing block: B:77:0x000c, code:
            continue;
     */
    /* JADX WARNING: Missing block: B:78:0x000c, code:
            continue;
     */
    /* JADX WARNING: Missing block: B:95:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:97:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void helpStealer(WorkQueue w, ForkJoinTask<?> task) {
        if (task != null && w != null) {
            ForkJoinTask<?> ps = w.currentSteal;
            int oldSum = 0;
            while (w.tryRemoveAndExec(task) && task.status >= 0) {
                WorkQueue[] ws = this.workQueues;
                if (ws != null) {
                    int wl = ws.length;
                    if (wl > 0) {
                        int m = wl - 1;
                        int checkSum = 0;
                        WorkQueue j = w;
                        ForkJoinTask<?> subtask = task;
                        while (subtask.status >= 0) {
                            int h = j.hint | 1;
                            int k = 0;
                            while (true) {
                                int i = ((k << 1) + h) & m;
                                WorkQueue v = ws[i];
                                if (v != null) {
                                    if (v.currentSteal == subtask) {
                                        break;
                                    }
                                    checkSum += v.base;
                                }
                                k++;
                                if (k > m) {
                                    return;
                                }
                            }
                        }
                    }
                    return;
                }
                return;
            }
        }
    }

    private boolean tryCompensate(WorkQueue w) {
        long c = this.ctl;
        WorkQueue[] ws = this.workQueues;
        int pc = this.config & 65535;
        int ac = pc + ((int) (c >> 48));
        int tc = pc + ((short) ((int) (c >> 32)));
        if (!(w == null || w.qlock < 0 || pc == 0 || ws == null)) {
            int wl = ws.length;
            if (wl > 0) {
                int m = wl - 1;
                boolean busy = true;
                for (int i = 0; i <= m; i++) {
                    int k = (i << 1) | 1;
                    if (k <= m && k >= 0) {
                        WorkQueue v = ws[k];
                        if (v != null && v.scanState >= 0 && v.currentSteal == null) {
                            busy = false;
                            break;
                        }
                    }
                }
                if (!busy || this.ctl != c) {
                    return false;
                }
                int sp = (int) c;
                if (sp != 0) {
                    return tryRelease(c, ws[m & sp], 0);
                } else if (tc >= pc && ac > 1 && w.isEmpty()) {
                    return U.compareAndSwapLong(this, CTL, c, ((c - AC_UNIT) & AC_MASK) | (281474976710655L & c));
                } else if (tc >= MAX_CAP || (this == common && tc >= COMMON_MAX_SPARES + pc)) {
                    throw new RejectedExecutionException("Thread limit exceeded replacing blocked worker");
                } else {
                    boolean isSpare = tc >= pc;
                    if (U.compareAndSwapLong(this, CTL, c, (AC_MASK & c) | ((TC_UNIT + c) & TC_MASK))) {
                        return createWorker(isSpare);
                    }
                    return false;
                }
            }
        }
        return false;
    }

    final int awaitJoin(WorkQueue w, ForkJoinTask<?> task, long deadline) {
        int s = 0;
        if (w != null) {
            ForkJoinTask<?> prevJoin = w.currentJoin;
            if (task != null) {
                s = task.status;
                if (s >= 0) {
                    w.currentJoin = task;
                    CountedCompleter cc = task instanceof CountedCompleter ? (CountedCompleter) task : null;
                    do {
                        if (cc != null) {
                            helpComplete(w, cc, 0);
                        } else {
                            helpStealer(w, task);
                        }
                        s = task.status;
                        if (s < 0) {
                            break;
                        }
                        long ms;
                        if (deadline != 0) {
                            long ns = deadline - System.nanoTime();
                            if (ns <= 0) {
                                break;
                            }
                            ms = TimeUnit.NANOSECONDS.toMillis(ns);
                            if (ms <= 0) {
                                ms = 1;
                            }
                        } else {
                            ms = 0;
                        }
                        if (tryCompensate(w)) {
                            task.internalWait(ms);
                            U.getAndAddLong(this, CTL, AC_UNIT);
                        }
                        s = task.status;
                    } while (s >= 0);
                    w.currentJoin = prevJoin;
                }
            }
        }
        return s;
    }

    private WorkQueue findNonEmptyStealQueue() {
        int r = ThreadLocalRandom.nextSecondarySeed();
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            int wl = ws.length;
            if (wl > 0) {
                int m = wl - 1;
                int origin = r & m;
                int k = origin;
                int oldSum = 0;
                int checkSum = 0;
                while (true) {
                    WorkQueue q = ws[k];
                    if (q != null) {
                        int b = q.base;
                        if (b - q.top < 0) {
                            return q;
                        }
                        checkSum += b;
                    }
                    k = (k + 1) & m;
                    if (k == origin) {
                        int oldSum2 = checkSum;
                        if (oldSum == checkSum) {
                            break;
                        }
                        checkSum = 0;
                        oldSum = oldSum2;
                    }
                }
            }
        }
        return null;
    }

    final void helpQuiescePool(WorkQueue w) {
        ForkJoinTask<?> ps = w.currentSteal;
        int wc = w.config;
        boolean active = true;
        while (true) {
            ForkJoinTask<?> t;
            if (wc >= 0) {
                t = w.pop();
                if (t != null) {
                    w.currentSteal = t;
                    t.doExec();
                    w.currentSteal = ps;
                }
            }
            WorkQueue q = findNonEmptyStealQueue();
            long c;
            if (q != null) {
                if (!active) {
                    active = true;
                    U.getAndAddLong(this, CTL, AC_UNIT);
                }
                t = q.pollAt(q.base);
                if (t != null) {
                    w.currentSteal = t;
                    t.doExec();
                    w.currentSteal = ps;
                    int i = w.nsteals + 1;
                    w.nsteals = i;
                    if (i < 0) {
                        w.transferStealCount(this);
                    }
                }
            } else if (active) {
                c = this.ctl;
                if (U.compareAndSwapLong(this, CTL, c, ((c - AC_UNIT) & AC_MASK) | (281474976710655L & c))) {
                    active = false;
                }
            } else {
                c = this.ctl;
                if (((int) (c >> 48)) + (this.config & 65535) <= 0) {
                    if (U.compareAndSwapLong(this, CTL, c, c + AC_UNIT)) {
                        return;
                    }
                } else {
                    continue;
                }
            }
        }
    }

    final ForkJoinTask<?> nextTaskFor(WorkQueue w) {
        ForkJoinTask<?> t;
        do {
            t = w.nextLocalTask();
            if (t != null) {
                return t;
            }
            WorkQueue q = findNonEmptyStealQueue();
            if (q == null) {
                return null;
            }
            t = q.pollAt(q.base);
        } while (t == null);
        return t;
    }

    static int getSurplusQueuedTaskCount() {
        int i = 0;
        Thread t = Thread.currentThread();
        if (!(t instanceof ForkJoinWorkerThread)) {
            return 0;
        }
        ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t;
        ForkJoinPool pool = wt.pool;
        int p = pool.config & 65535;
        WorkQueue q = wt.workQueue;
        int n = q.top - q.base;
        int a = ((int) (pool.ctl >> 48)) + p;
        p >>>= 1;
        if (a <= p) {
            p >>>= 1;
            if (a > p) {
                i = 1;
            } else {
                p >>>= 1;
                if (a > p) {
                    i = 2;
                } else if (a > (p >>> 1)) {
                    i = 4;
                } else {
                    i = 8;
                }
            }
        }
        return n - i;
    }

    private int tryTerminate(boolean now, boolean enable) {
        while (true) {
            int rs = this.runState;
            if (rs < 0) {
                long oldSum;
                long checkSum;
                WorkQueue[] ws;
                long oldSum2;
                if ((rs & 2) != 0) {
                    oldSum = 0;
                } else {
                    if (!now) {
                        oldSum = 0;
                        loop1:
                        while (true) {
                            checkSum = this.ctl;
                            if (((int) (checkSum >> 48)) + (this.config & 65535) > 0) {
                                return 0;
                            }
                            ws = this.workQueues;
                            if (ws != null) {
                                for (WorkQueue w : ws) {
                                    if (w != null) {
                                        int b = w.base;
                                        checkSum += (long) b;
                                        if (w.currentSteal == null && b == w.top) {
                                        }
                                    }
                                }
                            }
                            oldSum2 = checkSum;
                            if (oldSum == checkSum) {
                                break;
                            }
                            oldSum = oldSum2;
                        }
                        return 0;
                    }
                    Unsafe unsafe;
                    long j;
                    do {
                        unsafe = U;
                        j = RUNSTATE;
                        rs = this.runState;
                    } while (!unsafe.compareAndSwapInt(this, j, rs, rs | 2));
                }
                oldSum = 0;
                while (true) {
                    checkSum = this.ctl;
                    ws = this.workQueues;
                    if (ws != null) {
                        for (WorkQueue w2 : ws) {
                            if (w2 != null) {
                                w2.cancelAll();
                                checkSum += (long) w2.base;
                                if (w2.qlock >= 0) {
                                    w2.qlock = -1;
                                    ForkJoinWorkerThread wt = w2.owner;
                                    if (wt != null) {
                                        try {
                                            wt.interrupt();
                                        } catch (Throwable th) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                    oldSum2 = checkSum;
                    if (oldSum == checkSum) {
                        break;
                    }
                    oldSum = oldSum2;
                }
                if (((short) ((int) (this.ctl >>> 32))) + (this.config & 65535) <= 0) {
                    this.runState = -2147483641;
                    synchronized (this) {
                        notifyAll();
                    }
                }
                return -1;
            } else if (enable && this != common) {
                if (rs == 0) {
                    tryInitialize(false);
                } else {
                    U.compareAndSwapInt(this, RUNSTATE, rs, rs | Integer.MIN_VALUE);
                }
            }
        }
        return 1;
    }

    private void tryCreateExternalQueue(int index) {
        AuxState aux = this.auxState;
        if (aux != null && index >= 0) {
            WorkQueue q = new WorkQueue(this, null);
            q.config = index;
            q.scanState = Integer.MAX_VALUE;
            q.qlock = 1;
            boolean installed = false;
            aux.lock();
            try {
                WorkQueue[] ws = this.workQueues;
                if (ws != null && index < ws.length && ws[index] == null) {
                    ws[index] = q;
                    installed = true;
                }
                aux.unlock();
                if (installed) {
                    try {
                        q.growArray();
                    } finally {
                        q.qlock = 0;
                    }
                }
            } catch (Throwable th) {
                aux.unlock();
            }
        }
    }

    final void externalPush(ForkJoinTask<?> task) {
        int r = ThreadLocalRandom.getProbe();
        if (r == 0) {
            ThreadLocalRandom.localInit();
            r = ThreadLocalRandom.getProbe();
        }
        while (true) {
            int rs = this.runState;
            WorkQueue[] ws = this.workQueues;
            if (rs > 0 && ws != null) {
                int wl = ws.length;
                if (wl > 0) {
                    int k = ((wl - 1) & r) & 126;
                    WorkQueue q = ws[k];
                    if (q == null) {
                        tryCreateExternalQueue(k);
                    } else {
                        int stat = q.sharedPush(task);
                        if (stat >= 0) {
                            if (stat == 0) {
                                signalWork();
                                return;
                            }
                            r = ThreadLocalRandom.advanceProbe(r);
                        } else {
                            return;
                        }
                    }
                }
            }
            tryInitialize(true);
        }
    }

    private <T> ForkJoinTask<T> externalSubmit(ForkJoinTask<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
            if (w.pool == this) {
                WorkQueue q = w.workQueue;
                if (q != null) {
                    q.push(task);
                    return task;
                }
            }
        }
        externalPush(task);
        return task;
    }

    static WorkQueue commonSubmitterQueue() {
        ForkJoinPool p = common;
        int r = ThreadLocalRandom.getProbe();
        if (p == null) {
            return null;
        }
        WorkQueue[] ws = p.workQueues;
        if (ws == null) {
            return null;
        }
        int wl = ws.length;
        if (wl > 0) {
            return ws[((wl - 1) & r) & 126];
        }
        return null;
    }

    final boolean tryExternalUnpush(ForkJoinTask<?> task) {
        int r = ThreadLocalRandom.getProbe();
        WorkQueue[] ws = this.workQueues;
        if (ws == null) {
            return false;
        }
        int wl = ws.length;
        if (wl <= 0) {
            return false;
        }
        WorkQueue w = ws[((wl - 1) & r) & 126];
        if (w != null) {
            return w.trySharedUnpush(task);
        }
        return false;
    }

    final int externalHelpComplete(CountedCompleter<?> task, int maxTasks) {
        int r = ThreadLocalRandom.getProbe();
        WorkQueue[] ws = this.workQueues;
        if (ws == null) {
            return 0;
        }
        int wl = ws.length;
        if (wl > 0) {
            return helpComplete(ws[((wl - 1) & r) & 126], task, maxTasks);
        }
        return 0;
    }

    public ForkJoinPool() {
        this(Math.min(MAX_CAP, Runtime.getRuntime().availableProcessors()), defaultForkJoinWorkerThreadFactory, null, false);
    }

    public ForkJoinPool(int parallelism) {
        this(parallelism, defaultForkJoinWorkerThreadFactory, null, false);
    }

    public ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, UncaughtExceptionHandler handler, boolean asyncMode) {
        int checkParallelism = checkParallelism(parallelism);
        ForkJoinWorkerThreadFactory checkFactory = checkFactory(factory);
        int i = asyncMode ? Integer.MIN_VALUE : 0;
        String str = "ForkJoinPool-" + nextPoolId() + "-worker-";
        this(checkParallelism, checkFactory, handler, i, str);
        checkPermission();
    }

    private static int checkParallelism(int parallelism) {
        if (parallelism > 0 && parallelism <= MAX_CAP) {
            return parallelism;
        }
        throw new IllegalArgumentException();
    }

    private static ForkJoinWorkerThreadFactory checkFactory(ForkJoinWorkerThreadFactory factory) {
        if (factory != null) {
            return factory;
        }
        throw new NullPointerException();
    }

    private ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, UncaughtExceptionHandler handler, int mode, String workerNamePrefix) {
        this.workerNamePrefix = workerNamePrefix;
        this.factory = factory;
        this.ueh = handler;
        this.config = (65535 & parallelism) | mode;
        long np = (long) (-parallelism);
        this.ctl = ((np << 48) & AC_MASK) | ((np << 32) & TC_MASK);
    }

    public static ForkJoinPool commonPool() {
        return common;
    }

    public <T> T invoke(ForkJoinTask<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        externalSubmit(task);
        return task.join();
    }

    public void execute(ForkJoinTask<?> task) {
        externalSubmit(task);
    }

    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask) {
            job = (ForkJoinTask) task;
        } else {
            job = new RunnableExecuteAction(task);
        }
        externalSubmit(job);
    }

    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        return externalSubmit(task);
    }

    public /* bridge */ /* synthetic */ Future submit(Callable task) {
        return submit(task);
    }

    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        return externalSubmit(new AdaptedCallable(task));
    }

    public /* bridge */ /* synthetic */ Future submit(Runnable task, Object result) {
        return submit(task, result);
    }

    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        return externalSubmit(new AdaptedRunnable(task, result));
    }

    public /* bridge */ /* synthetic */ Future submit(Runnable task) {
        return submit(task);
    }

    public ForkJoinTask<?> submit(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask) {
            job = (ForkJoinTask) task;
        } else {
            job = new AdaptedRunnableAction(task);
        }
        return externalSubmit(job);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        ArrayList<Future<T>> futures = new ArrayList(tasks.size());
        int size;
        int i;
        try {
            for (Callable<T> t : tasks) {
                ForkJoinTask<T> f = new AdaptedCallable(t);
                futures.add(f);
                externalSubmit(f);
            }
            size = futures.size();
            for (i = 0; i < size; i++) {
                ((ForkJoinTask) futures.get(i)).quietlyJoin();
            }
            return futures;
        } catch (Throwable th) {
            size = futures.size();
            for (i = 0; i < size; i++) {
                ((Future) futures.get(i)).cancel(false);
            }
        }
    }

    public ForkJoinWorkerThreadFactory getFactory() {
        return this.factory;
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.ueh;
    }

    public int getParallelism() {
        int par = this.config & 65535;
        return par > 0 ? par : 1;
    }

    public static int getCommonPoolParallelism() {
        return COMMON_PARALLELISM;
    }

    public int getPoolSize() {
        return (this.config & 65535) + ((short) ((int) (this.ctl >>> 32)));
    }

    public boolean getAsyncMode() {
        return (this.config & Integer.MIN_VALUE) != 0;
    }

    public int getRunningThreadCount() {
        int rc = 0;
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            for (int i = 1; i < ws.length; i += 2) {
                WorkQueue w = ws[i];
                if (w != null && w.isApparentlyUnblocked()) {
                    rc++;
                }
            }
        }
        return rc;
    }

    public int getActiveThreadCount() {
        int r = (this.config & 65535) + ((int) (this.ctl >> 48));
        return r <= 0 ? 0 : r;
    }

    public boolean isQuiescent() {
        return (this.config & 65535) + ((int) (this.ctl >> 48)) <= 0;
    }

    public long getStealCount() {
        AuxState sc = this.auxState;
        long count = sc == null ? 0 : sc.stealCount;
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            for (int i = 1; i < ws.length; i += 2) {
                WorkQueue w = ws[i];
                if (w != null) {
                    count += (long) w.nsteals;
                }
            }
        }
        return count;
    }

    public long getQueuedTaskCount() {
        long count = 0;
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            for (int i = 1; i < ws.length; i += 2) {
                WorkQueue w = ws[i];
                if (w != null) {
                    count += (long) w.queueSize();
                }
            }
        }
        return count;
    }

    public int getQueuedSubmissionCount() {
        int count = 0;
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            for (int i = 0; i < ws.length; i += 2) {
                WorkQueue w = ws[i];
                if (w != null) {
                    count += w.queueSize();
                }
            }
        }
        return count;
    }

    public boolean hasQueuedSubmissions() {
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            for (int i = 0; i < ws.length; i += 2) {
                WorkQueue w = ws[i];
                if (w != null && !w.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected ForkJoinTask<?> pollSubmission() {
        int r = ThreadLocalRandom.nextSecondarySeed();
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            int wl = ws.length;
            if (wl > 0) {
                int m = wl - 1;
                for (int i = 0; i < wl; i++) {
                    WorkQueue w = ws[(i << 1) & m];
                    if (w != null) {
                        ForkJoinTask<?> t = w.poll();
                        if (t != null) {
                            return t;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        int count = 0;
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            for (WorkQueue w : ws) {
                if (w != null) {
                    while (true) {
                        ForkJoinTask<?> t = w.poll();
                        if (t == null) {
                            break;
                        }
                        c.add(t);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public String toString() {
        String level;
        long qt = 0;
        long qs = 0;
        int rc = 0;
        AuxState sc = this.auxState;
        long st = sc == null ? 0 : sc.stealCount;
        long c = this.ctl;
        WorkQueue[] ws = this.workQueues;
        if (ws != null) {
            for (int i = 0; i < ws.length; i++) {
                WorkQueue w = ws[i];
                if (w != null) {
                    int size = w.queueSize();
                    if ((i & 1) == 0) {
                        qs += (long) size;
                    } else {
                        qt += (long) size;
                        st += (long) w.nsteals;
                        if (w.isApparentlyUnblocked()) {
                            rc++;
                        }
                    }
                }
            }
        }
        int pc = this.config & 65535;
        int tc = pc + ((short) ((int) (c >>> 32)));
        int ac = pc + ((int) (c >> 48));
        if (ac < 0) {
            ac = 0;
        }
        int rs = this.runState;
        if ((rs & 4) != 0) {
            level = "Terminated";
        } else if ((rs & 2) != 0) {
            level = "Terminating";
        } else if ((Integer.MIN_VALUE & rs) != 0) {
            level = "Shutting down";
        } else {
            level = "Running";
        }
        return super.toString() + "[" + level + ", parallelism = " + pc + ", size = " + tc + ", active = " + ac + ", running = " + rc + ", steals = " + st + ", tasks = " + qt + ", submissions = " + qs + "]";
    }

    public void shutdown() {
        checkPermission();
        tryTerminate(false, true);
    }

    public List<Runnable> shutdownNow() {
        checkPermission();
        tryTerminate(true, true);
        return Collections.emptyList();
    }

    public boolean isTerminated() {
        return (this.runState & 4) != 0;
    }

    public boolean isTerminating() {
        int rs = this.runState;
        if ((rs & 2) == 0 || (rs & 4) != 0) {
            return false;
        }
        return true;
    }

    public boolean isShutdown() {
        return (this.runState & Integer.MIN_VALUE) != 0;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        } else if (this == common) {
            awaitQuiescence(timeout, unit);
            return false;
        } else {
            long nanos = unit.toNanos(timeout);
            if (isTerminated()) {
                return true;
            }
            if (nanos <= 0) {
                return false;
            }
            long deadline = System.nanoTime() + nanos;
            synchronized (this) {
                while (!isTerminated()) {
                    if (nanos <= 0) {
                        return false;
                    }
                    long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
                    if (millis <= 0) {
                        millis = 1;
                    }
                    wait(millis);
                    nanos = deadline - System.nanoTime();
                }
                return true;
            }
        }
    }

    public boolean awaitQuiescence(long timeout, TimeUnit unit) {
        long nanos = unit.toNanos(timeout);
        Thread thread = Thread.currentThread();
        if (thread instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread) thread;
            if (wt.pool == this) {
                helpQuiescePool(wt.workQueue);
                return true;
            }
        }
        long startTime = System.nanoTime();
        int r = 0;
        boolean found = true;
        while (!isQuiescent()) {
            WorkQueue[] ws = this.workQueues;
            if (ws == null) {
                break;
            }
            int wl = ws.length;
            if (wl <= 0) {
                break;
            }
            if (!found) {
                if (System.nanoTime() - startTime > nanos) {
                    return false;
                }
                Thread.yield();
            }
            found = false;
            int m = wl - 1;
            int j = (m + 1) << 2;
            int r2 = r;
            while (j >= 0) {
                r = r2 + 1;
                int k = r2 & m;
                if (k <= m && k >= 0) {
                    WorkQueue q = ws[k];
                    if (q != null) {
                        int b = q.base;
                        if (b - q.top < 0) {
                            found = true;
                            ForkJoinTask<?> t = q.pollAt(b);
                            if (t != null) {
                                t.doExec();
                            }
                        }
                    } else {
                        continue;
                    }
                }
                j--;
                r2 = r;
            }
            r = r2;
        }
        return true;
    }

    static void quiesceCommonPool() {
        common.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    public static void managedBlock(ManagedBlocker blocker) throws InterruptedException {
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t;
            ForkJoinPool p = wt.pool;
            if (p != null) {
                WorkQueue w = wt.workQueue;
                while (!blocker.isReleasable()) {
                    if (p.tryCompensate(w)) {
                        do {
                            try {
                                if (blocker.isReleasable()) {
                                    break;
                                }
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                U.getAndAddLong(p, CTL, AC_UNIT);
                            }
                        } while (!blocker.block());
                        U.getAndAddLong(p, CTL, AC_UNIT);
                        return;
                    }
                }
                return;
            }
        }
        while (!blocker.isReleasable()) {
            if (blocker.block()) {
                return;
            }
        }
    }

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new AdaptedRunnable(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new AdaptedCallable(callable);
    }

    static ForkJoinPool makeCommonPool() {
        int parallelism = -1;
        ForkJoinWorkerThreadFactory factory = null;
        UncaughtExceptionHandler handler = null;
        try {
            String pp = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
            String fp = System.getProperty("java.util.concurrent.ForkJoinPool.common.threadFactory");
            String hp = System.getProperty("java.util.concurrent.ForkJoinPool.common.exceptionHandler");
            if (pp != null) {
                parallelism = Integer.parseInt(pp);
            }
            if (fp != null) {
                factory = (ForkJoinWorkerThreadFactory) ClassLoader.getSystemClassLoader().loadClass(fp).newInstance();
            }
            if (hp != null) {
                handler = (UncaughtExceptionHandler) ClassLoader.getSystemClassLoader().loadClass(hp).newInstance();
            }
        } catch (Exception e) {
        }
        if (factory == null) {
            if (System.getSecurityManager() == null) {
                factory = defaultForkJoinWorkerThreadFactory;
            } else {
                factory = new InnocuousForkJoinWorkerThreadFactory();
            }
        }
        if (parallelism < 0) {
            parallelism = Runtime.getRuntime().availableProcessors() - 1;
            if (parallelism <= 0) {
                parallelism = 1;
            }
        }
        if (parallelism > MAX_CAP) {
            parallelism = MAX_CAP;
        }
        return new ForkJoinPool(parallelism, factory, handler, 0, "ForkJoinPool.commonPool-worker-");
    }
}
