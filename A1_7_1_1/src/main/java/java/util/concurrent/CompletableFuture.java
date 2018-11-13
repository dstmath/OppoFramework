package java.util.concurrent;

import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import sun.misc.Unsafe;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    */
public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {
    static final int ASYNC = 1;
    private static final Executor ASYNC_POOL = null;
    static final int NESTED = -1;
    private static final long NEXT = 0;
    static final AltResult NIL = null;
    private static final long RESULT = 0;
    static final int SPINS = 0;
    private static final long STACK = 0;
    static final int SYNC = 0;
    private static final Unsafe U = null;
    private static final boolean USE_COMMON_POOL = false;
    volatile Object result;
    volatile Completion stack;

    static final class AltResult {
        final Throwable ex;

        AltResult(Throwable x) {
            this.ex = x;
        }
    }

    public interface AsynchronousCompletionTask {
    }

    static final class AsyncRun extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        CompletableFuture<Void> dep;
        Runnable fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.AsyncRun.<init>(java.util.concurrent.CompletableFuture, java.lang.Runnable):void, dex: 
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
        AsyncRun(java.util.concurrent.CompletableFuture<java.lang.Void> r1, java.lang.Runnable r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.AsyncRun.<init>(java.util.concurrent.CompletableFuture, java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncRun.<init>(java.util.concurrent.CompletableFuture, java.lang.Runnable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncRun.exec():boolean, dex: 
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
        public final boolean exec() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncRun.exec():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncRun.exec():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncRun.getRawResult():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object getRawResult() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncRun.getRawResult():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncRun.getRawResult():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.AsyncRun.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.AsyncRun.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncRun.run():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncRun.setRawResult(java.lang.Object):void, dex: 
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
        public /* bridge */ /* synthetic */ void setRawResult(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncRun.setRawResult(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncRun.setRawResult(java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.AsyncRun.setRawResult(java.lang.Void):void, dex: 
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
        public final void setRawResult(java.lang.Void r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.AsyncRun.setRawResult(java.lang.Void):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncRun.setRawResult(java.lang.Void):void");
        }

        public final Void getRawResult() {
            return null;
        }
    }

    static final class AsyncSupply<T> extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        CompletableFuture<T> dep;
        Supplier<? extends T> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.AsyncSupply.<init>(java.util.concurrent.CompletableFuture, java.util.function.Supplier):void, dex: 
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
        AsyncSupply(java.util.concurrent.CompletableFuture<T> r1, java.util.function.Supplier<? extends T> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.AsyncSupply.<init>(java.util.concurrent.CompletableFuture, java.util.function.Supplier):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncSupply.<init>(java.util.concurrent.CompletableFuture, java.util.function.Supplier):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncSupply.exec():boolean, dex: 
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
        public final boolean exec() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncSupply.exec():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncSupply.exec():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncSupply.getRawResult():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object getRawResult() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncSupply.getRawResult():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncSupply.getRawResult():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.AsyncSupply.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.AsyncSupply.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncSupply.run():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncSupply.setRawResult(java.lang.Object):void, dex: 
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
        public /* bridge */ /* synthetic */ void setRawResult(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.AsyncSupply.setRawResult(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncSupply.setRawResult(java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.AsyncSupply.setRawResult(java.lang.Void):void, dex: 
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
        public final void setRawResult(java.lang.Void r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.AsyncSupply.setRawResult(java.lang.Void):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.AsyncSupply.setRawResult(java.lang.Void):void");
        }

        public final Void getRawResult() {
            return null;
        }
    }

    static abstract class Completion extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        volatile Completion next;

        abstract boolean isLive();

        abstract CompletableFuture<?> tryFire(int i);

        Completion() {
        }

        public final void run() {
            tryFire(1);
        }

        public final boolean exec() {
            tryFire(1);
            return false;
        }

        public /* bridge */ /* synthetic */ Object getRawResult() {
            return getRawResult();
        }

        public final Void getRawResult() {
            return null;
        }

        public /* bridge */ /* synthetic */ void setRawResult(Object v) {
            setRawResult((Void) v);
        }

        public final void setRawResult(Void v) {
        }
    }

    static abstract class UniCompletion<T, V> extends Completion {
        CompletableFuture<V> dep;
        Executor executor;
        CompletableFuture<T> src;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.concurrent.CompletableFuture.UniCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex:  in method: java.util.concurrent.CompletableFuture.UniCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.concurrent.CompletableFuture.UniCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
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
        UniCompletion(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<V> r2, java.util.concurrent.CompletableFuture<T> r3) {
            /*
            // Can't load method instructions: Load method exception: null in method: java.util.concurrent.CompletableFuture.UniCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex:  in method: java.util.concurrent.CompletableFuture.UniCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniCompletion.claim():boolean, dex: 
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
        final boolean claim() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniCompletion.claim():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniCompletion.claim():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniCompletion.isLive():boolean, dex: 
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
        final boolean isLive() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniCompletion.isLive():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniCompletion.isLive():boolean");
        }
    }

    static abstract class BiCompletion<T, U, V> extends UniCompletion<T, V> {
        CompletableFuture<U> snd;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.BiCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
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
        BiCompletion(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<V> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.concurrent.CompletableFuture<U> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.BiCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiCompletion.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void");
        }
    }

    static final class BiAccept<T, U> extends BiCompletion<T, U, Void> {
        BiConsumer<? super T, ? super U> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.BiAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiConsumer):void, dex: 
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
        BiAccept(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<java.lang.Void> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.concurrent.CompletableFuture<U> r4, java.util.function.BiConsumer<? super T, ? super U> r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.BiAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiConsumer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiConsumer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.BiAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
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
        final java.util.concurrent.CompletableFuture<java.lang.Void> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.BiAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>");
        }
    }

    static final class BiApply<T, U, V> extends BiCompletion<T, U, V> {
        BiFunction<? super T, ? super U, ? extends V> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.BiApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiFunction):void, dex: 
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
        BiApply(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<V> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.concurrent.CompletableFuture<U> r4, java.util.function.BiFunction<? super T, ? super U, ? extends V> r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.BiApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiFunction):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiFunction):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.BiApply.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
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
        final java.util.concurrent.CompletableFuture<V> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.BiApply.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiApply.tryFire(int):java.util.concurrent.CompletableFuture<V>");
        }
    }

    static final class BiRelay<T, U> extends BiCompletion<T, U, Void> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.BiRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
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
        BiRelay(java.util.concurrent.CompletableFuture<java.lang.Void> r1, java.util.concurrent.CompletableFuture<T> r2, java.util.concurrent.CompletableFuture<U> r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.BiRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.BiRelay.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
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
        final java.util.concurrent.CompletableFuture<java.lang.Void> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.BiRelay.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiRelay.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>");
        }
    }

    static final class BiRun<T, U> extends BiCompletion<T, U, Void> {
        Runnable fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.BiRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void, dex: 
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
        BiRun(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<java.lang.Void> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.concurrent.CompletableFuture<U> r4, java.lang.Runnable r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.BiRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.BiRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
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
        final java.util.concurrent.CompletableFuture<java.lang.Void> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.BiRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.BiRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>");
        }
    }

    static final class Canceller implements BiConsumer<Object, Throwable> {
        final Future<?> f;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.Canceller.<init>(java.util.concurrent.Future):void, dex: 
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
        Canceller(java.util.concurrent.Future<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.Canceller.<init>(java.util.concurrent.Future):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Canceller.<init>(java.util.concurrent.Future):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.Canceller.accept(java.lang.Object, java.lang.Object):void, dex: 
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
        public /* bridge */ /* synthetic */ void accept(java.lang.Object r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.Canceller.accept(java.lang.Object, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Canceller.accept(java.lang.Object, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.Canceller.accept(java.lang.Object, java.lang.Throwable):void, dex: 
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
        public void accept(java.lang.Object r1, java.lang.Throwable r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.Canceller.accept(java.lang.Object, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Canceller.accept(java.lang.Object, java.lang.Throwable):void");
        }
    }

    static final class CoCompletion extends Completion {
        BiCompletion<?, ?, ?> base;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.CoCompletion.<init>(java.util.concurrent.CompletableFuture$BiCompletion):void, dex: 
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
        CoCompletion(java.util.concurrent.CompletableFuture.BiCompletion<?, ?, ?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.CoCompletion.<init>(java.util.concurrent.CompletableFuture$BiCompletion):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.CoCompletion.<init>(java.util.concurrent.CompletableFuture$BiCompletion):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.CoCompletion.isLive():boolean, dex: 
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
        final boolean isLive() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.CoCompletion.isLive():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.CoCompletion.isLive():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.CoCompletion.tryFire(int):java.util.concurrent.CompletableFuture<?>, dex: 
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
        final java.util.concurrent.CompletableFuture<?> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.CoCompletion.tryFire(int):java.util.concurrent.CompletableFuture<?>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.CoCompletion.tryFire(int):java.util.concurrent.CompletableFuture<?>");
        }
    }

    static final class DelayedCompleter<U> implements Runnable {
        final CompletableFuture<U> f;
        final U u;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.DelayedCompleter.<init>(java.util.concurrent.CompletableFuture, java.lang.Object):void, dex: 
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
        DelayedCompleter(java.util.concurrent.CompletableFuture<U> r1, U r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.DelayedCompleter.<init>(java.util.concurrent.CompletableFuture, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.DelayedCompleter.<init>(java.util.concurrent.CompletableFuture, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.DelayedCompleter.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.DelayedCompleter.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.DelayedCompleter.run():void");
        }
    }

    static final class DelayedExecutor implements Executor {
        final long delay;
        final Executor executor;
        final TimeUnit unit;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: java.util.concurrent.CompletableFuture.DelayedExecutor.<init>(long, java.util.concurrent.TimeUnit, java.util.concurrent.Executor):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        DelayedExecutor(long r1, java.util.concurrent.TimeUnit r3, java.util.concurrent.Executor r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: java.util.concurrent.CompletableFuture.DelayedExecutor.<init>(long, java.util.concurrent.TimeUnit, java.util.concurrent.Executor):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.DelayedExecutor.<init>(long, java.util.concurrent.TimeUnit, java.util.concurrent.Executor):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.DelayedExecutor.execute(java.lang.Runnable):void, dex: 
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
        public void execute(java.lang.Runnable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.DelayedExecutor.execute(java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.DelayedExecutor.execute(java.lang.Runnable):void");
        }
    }

    static final class Delayer {
        static final ScheduledThreadPoolExecutor delayer = null;

        static final class DaemonThreadFactory implements ThreadFactory {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.Delayer.DaemonThreadFactory.<init>():void, dex: 
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
            DaemonThreadFactory() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.Delayer.DaemonThreadFactory.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Delayer.DaemonThreadFactory.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.Delayer.DaemonThreadFactory.newThread(java.lang.Runnable):java.lang.Thread, dex: 
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
            public java.lang.Thread newThread(java.lang.Runnable r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.Delayer.DaemonThreadFactory.newThread(java.lang.Runnable):java.lang.Thread, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Delayer.DaemonThreadFactory.newThread(java.lang.Runnable):java.lang.Thread");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.Delayer.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.Delayer.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Delayer.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.Delayer.<init>():void, dex: 
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
        Delayer() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.Delayer.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Delayer.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.Delayer.delay(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>, dex: 
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
        static java.util.concurrent.ScheduledFuture<?> delay(java.lang.Runnable r1, long r2, java.util.concurrent.TimeUnit r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.Delayer.delay(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Delayer.delay(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>");
        }
    }

    static final class MinimalStage<T> extends CompletableFuture<T> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.MinimalStage.<init>():void, dex: 
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
        MinimalStage() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.MinimalStage.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.MinimalStage.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.MinimalStage.<init>(java.lang.Object):void, dex: 
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
        MinimalStage(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.MinimalStage.<init>(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.MinimalStage.<init>(java.lang.Object):void");
        }

        public <U> CompletableFuture<U> newIncompleteFuture() {
            return new MinimalStage();
        }

        public T get() {
            throw new UnsupportedOperationException();
        }

        public T get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        public T getNow(T t) {
            throw new UnsupportedOperationException();
        }

        public T join() {
            throw new UnsupportedOperationException();
        }

        public boolean complete(T t) {
            throw new UnsupportedOperationException();
        }

        public boolean completeExceptionally(Throwable ex) {
            throw new UnsupportedOperationException();
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        public void obtrudeValue(T t) {
            throw new UnsupportedOperationException();
        }

        public void obtrudeException(Throwable ex) {
            throw new UnsupportedOperationException();
        }

        public boolean isDone() {
            throw new UnsupportedOperationException();
        }

        public boolean isCancelled() {
            throw new UnsupportedOperationException();
        }

        public boolean isCompletedExceptionally() {
            throw new UnsupportedOperationException();
        }

        public int getNumberOfDependents() {
            throw new UnsupportedOperationException();
        }

        public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
            throw new UnsupportedOperationException();
        }

        public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
            throw new UnsupportedOperationException();
        }

        public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        public CompletableFuture<T> completeOnTimeout(T t, long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    }

    static final class OrAccept<T, U extends T> extends BiCompletion<T, U, Void> {
        Consumer<? super T> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.OrAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Consumer):void, dex: 
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
        OrAccept(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<java.lang.Void> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.concurrent.CompletableFuture<U> r4, java.util.function.Consumer<? super T> r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.OrAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Consumer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.OrAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Consumer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.OrAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
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
        final java.util.concurrent.CompletableFuture<java.lang.Void> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.OrAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.OrAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>");
        }
    }

    static final class OrApply<T, U extends T, V> extends BiCompletion<T, U, V> {
        Function<? super T, ? extends V> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.OrApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void, dex: 
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
        OrApply(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<V> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.concurrent.CompletableFuture<U> r4, java.util.function.Function<? super T, ? extends V> r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.OrApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.OrApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.OrApply.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
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
        final java.util.concurrent.CompletableFuture<V> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.OrApply.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.OrApply.tryFire(int):java.util.concurrent.CompletableFuture<V>");
        }
    }

    static final class OrRelay<T, U> extends BiCompletion<T, U, Object> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.OrRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
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
        OrRelay(java.util.concurrent.CompletableFuture<java.lang.Object> r1, java.util.concurrent.CompletableFuture<T> r2, java.util.concurrent.CompletableFuture<U> r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.OrRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.OrRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.OrRelay.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Object>, dex: 
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
        final java.util.concurrent.CompletableFuture<java.lang.Object> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.OrRelay.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Object>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.OrRelay.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Object>");
        }
    }

    static final class OrRun<T, U> extends BiCompletion<T, U, Void> {
        Runnable fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.OrRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void, dex: 
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
        OrRun(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<java.lang.Void> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.concurrent.CompletableFuture<U> r4, java.lang.Runnable r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.OrRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.OrRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.OrRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
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
        final java.util.concurrent.CompletableFuture<java.lang.Void> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.OrRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.OrRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>");
        }
    }

    static final class Signaller extends Completion implements ManagedBlocker {
        final long deadline;
        boolean interrupted;
        final boolean interruptible;
        long nanos;
        volatile Thread thread;

        Signaller(boolean interruptible, long nanos, long deadline) {
            this.thread = Thread.currentThread();
            this.interruptible = interruptible;
            this.nanos = nanos;
            this.deadline = deadline;
        }

        final CompletableFuture<?> tryFire(int ignore) {
            Thread w = this.thread;
            if (w != null) {
                this.thread = null;
                LockSupport.unpark(w);
            }
            return null;
        }

        public boolean isReleasable() {
            if (Thread.interrupted()) {
                this.interrupted = true;
            }
            if (this.interrupted && this.interruptible) {
                return true;
            }
            if (this.deadline != 0) {
                if (this.nanos <= 0) {
                    return true;
                }
                long nanoTime = this.deadline - System.nanoTime();
                this.nanos = nanoTime;
                if (nanoTime <= 0) {
                    return true;
                }
            }
            if (this.thread != null) {
                return false;
            }
            return true;
        }

        public boolean block() {
            while (!isReleasable()) {
                if (this.deadline == 0) {
                    LockSupport.park(this);
                } else {
                    LockSupport.parkNanos(this, this.nanos);
                }
            }
            return true;
        }

        final boolean isLive() {
            return this.thread != null;
        }
    }

    static final class TaskSubmitter implements Runnable {
        final Runnable action;
        final Executor executor;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.TaskSubmitter.<init>(java.util.concurrent.Executor, java.lang.Runnable):void, dex: 
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
        TaskSubmitter(java.util.concurrent.Executor r1, java.lang.Runnable r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.TaskSubmitter.<init>(java.util.concurrent.Executor, java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.TaskSubmitter.<init>(java.util.concurrent.Executor, java.lang.Runnable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.TaskSubmitter.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.TaskSubmitter.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.TaskSubmitter.run():void");
        }
    }

    static final class ThreadPerTaskExecutor implements Executor {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.ThreadPerTaskExecutor.<init>():void, dex: 
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
        ThreadPerTaskExecutor() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.ThreadPerTaskExecutor.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.ThreadPerTaskExecutor.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.ThreadPerTaskExecutor.execute(java.lang.Runnable):void, dex: 
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
        public void execute(java.lang.Runnable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.ThreadPerTaskExecutor.execute(java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.ThreadPerTaskExecutor.execute(java.lang.Runnable):void");
        }
    }

    static final class Timeout implements Runnable {
        final CompletableFuture<?> f;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.Timeout.<init>(java.util.concurrent.CompletableFuture):void, dex: 
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
        Timeout(java.util.concurrent.CompletableFuture<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.Timeout.<init>(java.util.concurrent.CompletableFuture):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Timeout.<init>(java.util.concurrent.CompletableFuture):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.Timeout.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.Timeout.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.Timeout.run():void");
        }
    }

    static final class UniAccept<T> extends UniCompletion<T, Void> {
        Consumer<? super T> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Consumer):void, dex: 
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
        UniAccept(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<java.lang.Void> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.function.Consumer<? super T> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Consumer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniAccept.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Consumer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
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
        final java.util.concurrent.CompletableFuture<java.lang.Void> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniAccept.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>");
        }
    }

    static final class UniApply<T, V> extends UniCompletion<T, V> {
        Function<? super T, ? extends V> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void, dex: 
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
        UniApply(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<V> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.function.Function<? super T, ? extends V> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniApply.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniApply.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
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
        final java.util.concurrent.CompletableFuture<V> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniApply.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniApply.tryFire(int):java.util.concurrent.CompletableFuture<V>");
        }
    }

    static final class UniCompose<T, V> extends UniCompletion<T, V> {
        Function<? super T, ? extends CompletionStage<V>> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniCompose.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void, dex: 
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
        UniCompose(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<V> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.function.Function<? super T, ? extends java.util.concurrent.CompletionStage<V>> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniCompose.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniCompose.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniCompose.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
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
        final java.util.concurrent.CompletableFuture<V> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniCompose.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniCompose.tryFire(int):java.util.concurrent.CompletableFuture<V>");
        }
    }

    static final class UniExceptionally<T> extends UniCompletion<T, T> {
        Function<? super Throwable, ? extends T> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniExceptionally.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void, dex: 
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
        UniExceptionally(java.util.concurrent.CompletableFuture<T> r1, java.util.concurrent.CompletableFuture<T> r2, java.util.function.Function<? super java.lang.Throwable, ? extends T> r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniExceptionally.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniExceptionally.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.Function):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniExceptionally.tryFire(int):java.util.concurrent.CompletableFuture<T>, dex: 
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
        final java.util.concurrent.CompletableFuture<T> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniExceptionally.tryFire(int):java.util.concurrent.CompletableFuture<T>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniExceptionally.tryFire(int):java.util.concurrent.CompletableFuture<T>");
        }
    }

    static final class UniHandle<T, V> extends UniCompletion<T, V> {
        BiFunction<? super T, Throwable, ? extends V> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniHandle.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiFunction):void, dex: 
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
        UniHandle(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<V> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.function.BiFunction<? super T, java.lang.Throwable, ? extends V> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniHandle.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiFunction):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniHandle.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiFunction):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniHandle.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
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
        final java.util.concurrent.CompletableFuture<V> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniHandle.tryFire(int):java.util.concurrent.CompletableFuture<V>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniHandle.tryFire(int):java.util.concurrent.CompletableFuture<V>");
        }
    }

    static final class UniRelay<T> extends UniCompletion<T, T> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.UniRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
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
        UniRelay(java.util.concurrent.CompletableFuture<T> r1, java.util.concurrent.CompletableFuture<T> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.CompletableFuture.UniRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniRelay.<init>(java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniRelay.tryFire(int):java.util.concurrent.CompletableFuture<T>, dex: 
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
        final java.util.concurrent.CompletableFuture<T> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniRelay.tryFire(int):java.util.concurrent.CompletableFuture<T>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniRelay.tryFire(int):java.util.concurrent.CompletableFuture<T>");
        }
    }

    static final class UniRun<T> extends UniCompletion<T, Void> {
        Runnable fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void, dex: 
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
        UniRun(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<java.lang.Void> r2, java.util.concurrent.CompletableFuture<T> r3, java.lang.Runnable r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniRun.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.lang.Runnable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
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
        final java.util.concurrent.CompletableFuture<java.lang.Void> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniRun.tryFire(int):java.util.concurrent.CompletableFuture<java.lang.Void>");
        }
    }

    static final class UniWhenComplete<T> extends UniCompletion<T, T> {
        BiConsumer<? super T, ? super Throwable> fn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniWhenComplete.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiConsumer):void, dex: 
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
        UniWhenComplete(java.util.concurrent.Executor r1, java.util.concurrent.CompletableFuture<T> r2, java.util.concurrent.CompletableFuture<T> r3, java.util.function.BiConsumer<? super T, ? super java.lang.Throwable> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CompletableFuture.UniWhenComplete.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiConsumer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniWhenComplete.<init>(java.util.concurrent.Executor, java.util.concurrent.CompletableFuture, java.util.concurrent.CompletableFuture, java.util.function.BiConsumer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniWhenComplete.tryFire(int):java.util.concurrent.CompletableFuture<T>, dex: 
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
        final java.util.concurrent.CompletableFuture<T> tryFire(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CompletableFuture.UniWhenComplete.tryFire(int):java.util.concurrent.CompletableFuture<T>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.UniWhenComplete.tryFire(int):java.util.concurrent.CompletableFuture<T>");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CompletableFuture.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CompletableFuture.<clinit>():void");
    }

    final boolean internalComplete(Object r) {
        return U.compareAndSwapObject(this, RESULT, null, r);
    }

    final boolean casStack(Completion cmp, Completion val) {
        return U.compareAndSwapObject(this, STACK, cmp, val);
    }

    final boolean tryPushStack(Completion c) {
        Completion h = this.stack;
        lazySetNext(c, h);
        return U.compareAndSwapObject(this, STACK, h, c);
    }

    final void pushStack(Completion c) {
        do {
        } while (!tryPushStack(c));
    }

    final boolean completeNull() {
        return U.compareAndSwapObject(this, RESULT, null, NIL);
    }

    final Object encodeValue(T t) {
        return t == null ? NIL : t;
    }

    final boolean completeValue(T t) {
        Object obj;
        Unsafe unsafe = U;
        long j = RESULT;
        if (t == null) {
            obj = NIL;
        } else {
            T obj2 = t;
        }
        return unsafe.compareAndSwapObject(this, j, null, obj2);
    }

    static AltResult encodeThrowable(Throwable x) {
        if (!(x instanceof CompletionException)) {
            x = new CompletionException(x);
        }
        return new AltResult(x);
    }

    final boolean completeThrowable(Throwable x) {
        return U.compareAndSwapObject(this, RESULT, null, encodeThrowable(x));
    }

    static Object encodeThrowable(Throwable x, Object r) {
        if (!(x instanceof CompletionException)) {
            x = new CompletionException(x);
        } else if ((r instanceof AltResult) && x == ((AltResult) r).ex) {
            return r;
        }
        return new AltResult(x);
    }

    final boolean completeThrowable(Throwable x, Object r) {
        return U.compareAndSwapObject(this, RESULT, null, encodeThrowable(x, r));
    }

    Object encodeOutcome(T t, Throwable x) {
        if (x == null) {
            return t == null ? NIL : t;
        } else {
            return encodeThrowable(x);
        }
    }

    static Object encodeRelay(Object r) {
        if (!(r instanceof AltResult)) {
            return r;
        }
        Throwable x = ((AltResult) r).ex;
        if (x == null || (x instanceof CompletionException)) {
            return r;
        }
        return new AltResult(new CompletionException(x));
    }

    final boolean completeRelay(Object r) {
        return U.compareAndSwapObject(this, RESULT, null, encodeRelay(r));
    }

    private static <T> T reportGet(Object r) throws InterruptedException, ExecutionException {
        if (r == null) {
            throw new InterruptedException();
        } else if (r instanceof AltResult) {
            Throwable x = ((AltResult) r).ex;
            if (x == null) {
                return null;
            }
            if (x instanceof CancellationException) {
                throw ((CancellationException) x);
            }
            if (x instanceof CompletionException) {
                Throwable cause = x.getCause();
                if (cause != null) {
                    x = cause;
                }
            }
            throw new ExecutionException(x);
        } else {
            T t = r;
            return r;
        }
    }

    private static <T> T reportJoin(Object r) {
        if (r instanceof AltResult) {
            Throwable x = ((AltResult) r).ex;
            if (x == null) {
                return null;
            }
            if (x instanceof CancellationException) {
                throw ((CancellationException) x);
            } else if (x instanceof CompletionException) {
                throw ((CompletionException) x);
            } else {
                throw new CompletionException(x);
            }
        }
        T t = r;
        return r;
    }

    static Executor screenExecutor(Executor e) {
        if (!USE_COMMON_POOL && e == ForkJoinPool.commonPool()) {
            return ASYNC_POOL;
        }
        if (e != null) {
            return e;
        }
        throw new NullPointerException();
    }

    static void lazySetNext(Completion c, Completion next) {
        U.putOrderedObject(c, NEXT, next);
    }

    final void postComplete() {
        CompletableFuture<?> f = this;
        while (true) {
            Completion h = f.stack;
            if (h == null) {
                if (f != this) {
                    f = this;
                    h = this.stack;
                    if (h == null) {
                        return;
                    }
                }
                return;
            }
            Completion t = h.next;
            if (f.casStack(h, t)) {
                if (t != null) {
                    if (f != this) {
                        pushStack(h);
                    } else {
                        h.next = null;
                    }
                }
                CompletableFuture<?> d = h.tryFire(-1);
                f = d == null ? this : d;
            }
        }
    }

    final void cleanStack() {
        Completion p = null;
        Completion q = this.stack;
        while (q != null) {
            Completion s = q.next;
            if (q.isLive()) {
                p = q;
                q = s;
            } else if (p == null) {
                casStack(q, s);
                q = this.stack;
            } else {
                p.next = s;
                if (p.isLive()) {
                    q = s;
                } else {
                    p = null;
                    q = this.stack;
                }
            }
        }
    }

    final void push(UniCompletion<?, ?> c) {
        if (c != null) {
            while (this.result == null && !tryPushStack(c)) {
                lazySetNext(c, null);
            }
        }
    }

    final CompletableFuture<T> postFire(CompletableFuture<?> a, int mode) {
        if (!(a == null || a.stack == null)) {
            if (mode < 0 || a.result == null) {
                a.cleanStack();
            } else {
                a.postComplete();
            }
        }
        if (!(this.result == null || this.stack == null)) {
            if (mode < 0) {
                return this;
            }
            postComplete();
        }
        return null;
    }

    final <S> boolean uniApply(CompletableFuture<S> a, Function<? super S, ? extends T> f, UniApply<S, T> c) {
        if (a != null) {
            S r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    completeValue(f.apply(r));
                }
                return true;
            }
        }
        return false;
    }

    private <V> CompletableFuture<V> uniApplyStage(Executor e, Function<? super T, ? extends V> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<V> d = newIncompleteFuture();
        if (!(e == null && d.uniApply(this, f, null))) {
            UniApply<T, V> c = new UniApply(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final <S> boolean uniAccept(CompletableFuture<S> a, Consumer<? super S> f, UniAccept<S> c) {
        if (a != null) {
            S r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    f.accept(r);
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Void> uniAcceptStage(Executor e, Consumer<? super T> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = newIncompleteFuture();
        if (!(e == null && d.uniAccept(this, f, null))) {
            UniAccept<T> c = new UniAccept(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final boolean uniRun(CompletableFuture<?> a, Runnable f, UniRun<?> c) {
        if (a != null) {
            Object r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        }
                    }
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    f.run();
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Void> uniRunStage(Executor e, Runnable f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = newIncompleteFuture();
        if (!(e == null && d.uniRun(this, f, null))) {
            UniRun<T> c = new UniRun(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final boolean uniWhenComplete(CompletableFuture<T> a, BiConsumer<? super T, ? super Throwable> f, UniWhenComplete<T> c) {
        Throwable x = null;
        if (a != null) {
            T r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    Object t;
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            if (x == null) {
                                x = ex;
                            } else if (x != ex) {
                                x.addSuppressed(ex);
                            }
                        }
                    }
                    if (r instanceof AltResult) {
                        x = ((AltResult) r).ex;
                        t = null;
                    } else {
                        T tr = r;
                        T t2 = r;
                    }
                    f.accept(t2, x);
                    if (x == null) {
                        internalComplete(r);
                        return true;
                    }
                    completeThrowable(x, r);
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<T> uniWhenCompleteStage(Executor e, BiConsumer<? super T, ? super Throwable> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<T> d = newIncompleteFuture();
        if (!(e == null && d.uniWhenComplete(this, f, null))) {
            UniWhenComplete<T> c = new UniWhenComplete(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final <S> boolean uniHandle(CompletableFuture<S> a, BiFunction<? super S, Throwable, ? extends T> f, UniHandle<S, T> c) {
        if (a != null) {
            S r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    Throwable x;
                    Object s;
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        x = ((AltResult) r).ex;
                        s = null;
                    } else {
                        x = null;
                        S ss = r;
                        S s2 = r;
                    }
                    completeValue(f.apply(s2, x));
                }
                return true;
            }
        }
        return false;
    }

    private <V> CompletableFuture<V> uniHandleStage(Executor e, BiFunction<? super T, Throwable, ? extends V> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<V> d = newIncompleteFuture();
        if (!(e == null && d.uniHandle(this, f, null))) {
            UniHandle<T, V> c = new UniHandle(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final boolean uniExceptionally(CompletableFuture<T> a, Function<? super Throwable, ? extends T> f, UniExceptionally<T> c) {
        if (a != null) {
            Object r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    try {
                        if (r instanceof AltResult) {
                            Throwable x = ((AltResult) r).ex;
                            if (x != null) {
                                if (c != null && !c.claim()) {
                                    return false;
                                }
                                completeValue(f.apply(x));
                            }
                        }
                        internalComplete(r);
                    } catch (Throwable ex) {
                        completeThrowable(ex);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<T> uniExceptionallyStage(Function<Throwable, ? extends T> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<T> d = newIncompleteFuture();
        if (!d.uniExceptionally(this, f, null)) {
            UniExceptionally<T> c = new UniExceptionally(d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final boolean uniRelay(CompletableFuture<T> a) {
        if (a != null) {
            Object r = a.result;
            if (r != null) {
                if (this.result == null) {
                    completeRelay(r);
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<T> uniCopyStage() {
        CompletableFuture<T> d = newIncompleteFuture();
        Object r = this.result;
        if (r != null) {
            d.completeRelay(r);
        } else {
            UniRelay<T> c = new UniRelay(d, this);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    private MinimalStage<T> uniAsMinimalStage() {
        Object r = this.result;
        if (r != null) {
            return new MinimalStage(encodeRelay(r));
        }
        MinimalStage<T> d = new MinimalStage();
        UniRelay<T> c = new UniRelay(d, this);
        push(c);
        c.tryFire(0);
        return d;
    }

    final <S> boolean uniCompose(CompletableFuture<S> a, Function<? super S, ? extends CompletionStage<T>> f, UniCompose<S, T> c) {
        if (a != null) {
            S r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    CompletableFuture<T> g = ((CompletionStage) f.apply(r)).toCompletableFuture();
                    if (g.result == null || !uniRelay(g)) {
                        UniRelay<T> copy = new UniRelay(this, g);
                        g.push(copy);
                        copy.tryFire(0);
                        if (this.result == null) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private <V> CompletableFuture<V> uniComposeStage(Executor e, Function<? super T, ? extends CompletionStage<V>> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<V> d = newIncompleteFuture();
        if (e == null) {
            T r = this.result;
            if (r != null) {
                if (r instanceof AltResult) {
                    Throwable x = ((AltResult) r).ex;
                    if (x != null) {
                        d.result = encodeThrowable(x, r);
                        return d;
                    }
                    r = null;
                }
                try {
                    CompletableFuture<V> g = ((CompletionStage) f.apply(r)).toCompletableFuture();
                    Object s = g.result;
                    if (s != null) {
                        d.completeRelay(s);
                    } else {
                        UniRelay<V> c = new UniRelay(d, g);
                        g.push(c);
                        c.tryFire(0);
                    }
                    return d;
                } catch (Throwable ex) {
                    d.result = encodeThrowable(ex);
                    return d;
                }
            }
        }
        UniCompose<T, V> c2 = new UniCompose(e, d, this, f);
        push(c2);
        c2.tryFire(0);
        return d;
    }

    final void bipush(CompletableFuture<?> b, BiCompletion<?, ?, ?> c) {
        if (c != null) {
            while (true) {
                Object r = this.result;
                if (r == null && !tryPushStack(c)) {
                    lazySetNext(c, null);
                } else if (b != null && b != this && b.result == null) {
                    Completion q = r != null ? c : new CoCompletion(c);
                    while (b.result == null && !b.tryPushStack(q)) {
                        lazySetNext(q, null);
                    }
                    return;
                } else {
                    return;
                }
            }
            if (b != null) {
            }
        }
    }

    final CompletableFuture<T> postFire(CompletableFuture<?> a, CompletableFuture<?> b, int mode) {
        if (!(b == null || b.stack == null)) {
            if (mode < 0 || b.result == null) {
                b.cleanStack();
            } else {
                b.postComplete();
            }
        }
        return postFire(a, mode);
    }

    final <R, S> boolean biApply(CompletableFuture<R> a, CompletableFuture<S> b, BiFunction<? super R, ? super S, ? extends T> f, BiApply<R, S, T> c) {
        if (a != null) {
            R r = a.result;
            if (!(r == null || b == null)) {
                S s = b.result;
                if (!(s == null || f == null)) {
                    if (this.result == null) {
                        Throwable x;
                        if (r instanceof AltResult) {
                            x = ((AltResult) r).ex;
                            if (x != null) {
                                completeThrowable(x, r);
                            } else {
                                r = null;
                            }
                        }
                        if (s instanceof AltResult) {
                            x = ((AltResult) s).ex;
                            if (x != null) {
                                completeThrowable(x, s);
                            } else {
                                s = null;
                            }
                        }
                        if (c != null) {
                            try {
                                if (!c.claim()) {
                                    return false;
                                }
                            } catch (Throwable ex) {
                                completeThrowable(ex);
                            }
                        }
                        S ss = s;
                        completeValue(f.apply(r, s));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private <U, V> CompletableFuture<V> biApplyStage(Executor e, CompletionStage<U> o, BiFunction<? super T, ? super U, ? extends V> f) {
        if (f != null) {
            CompletableFuture<U> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<V> d = newIncompleteFuture();
                if (!(e == null && d.biApply(this, b, f, null))) {
                    BiApply<T, U, V> c = new BiApply(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    final <R, S> boolean biAccept(CompletableFuture<R> a, CompletableFuture<S> b, BiConsumer<? super R, ? super S> f, BiAccept<R, S> c) {
        if (a != null) {
            R r = a.result;
            if (!(r == null || b == null)) {
                S s = b.result;
                if (!(s == null || f == null)) {
                    if (this.result == null) {
                        Throwable x;
                        if (r instanceof AltResult) {
                            x = ((AltResult) r).ex;
                            if (x != null) {
                                completeThrowable(x, r);
                            } else {
                                r = null;
                            }
                        }
                        if (s instanceof AltResult) {
                            x = ((AltResult) s).ex;
                            if (x != null) {
                                completeThrowable(x, s);
                            } else {
                                s = null;
                            }
                        }
                        if (c != null) {
                            try {
                                if (!c.claim()) {
                                    return false;
                                }
                            } catch (Throwable ex) {
                                completeThrowable(ex);
                            }
                        }
                        S ss = s;
                        f.accept(r, s);
                        completeNull();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private <U> CompletableFuture<Void> biAcceptStage(Executor e, CompletionStage<U> o, BiConsumer<? super T, ? super U> f) {
        if (f != null) {
            CompletableFuture<U> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (!(e == null && d.biAccept(this, b, f, null))) {
                    BiAccept<T, U> c = new BiAccept(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    final boolean biRun(CompletableFuture<?> a, CompletableFuture<?> b, Runnable f, BiRun<?, ?> c) {
        if (a != null) {
            Object r = a.result;
            if (!(r == null || b == null)) {
                Object s = b.result;
                if (!(s == null || f == null)) {
                    if (this.result == null) {
                        Throwable x;
                        if (r instanceof AltResult) {
                            x = ((AltResult) r).ex;
                            if (x != null) {
                                completeThrowable(x, r);
                            }
                        }
                        if (s instanceof AltResult) {
                            x = ((AltResult) s).ex;
                            if (x != null) {
                                completeThrowable(x, s);
                            }
                        }
                        if (c != null) {
                            try {
                                if (!c.claim()) {
                                    return false;
                                }
                            } catch (Throwable ex) {
                                completeThrowable(ex);
                            }
                        }
                        f.run();
                        completeNull();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private CompletableFuture<Void> biRunStage(Executor e, CompletionStage<?> o, Runnable f) {
        if (f != null) {
            CompletableFuture<?> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (!(e == null && d.biRun(this, b, f, null))) {
                    BiRun<T, ?> c = new BiRun(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    boolean biRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        if (a != null) {
            Object r = a.result;
            if (!(r == null || b == null)) {
                Object s = b.result;
                if (s != null) {
                    if (this.result == null) {
                        Throwable x;
                        if (r instanceof AltResult) {
                            x = ((AltResult) r).ex;
                            if (x != null) {
                                completeThrowable(x, r);
                            }
                        }
                        if (s instanceof AltResult) {
                            x = ((AltResult) s).ex;
                            if (x != null) {
                                completeThrowable(x, s);
                            }
                        }
                        completeNull();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    static CompletableFuture<Void> andTree(CompletableFuture<?>[] cfs, int lo, int hi) {
        CompletableFuture<Void> d = new CompletableFuture();
        if (lo > hi) {
            d.result = NIL;
        } else {
            CompletableFuture<?> a;
            int mid = (lo + hi) >>> 1;
            if (lo == mid) {
                a = cfs[lo];
            } else {
                a = andTree(cfs, lo, mid);
            }
            if (a != null) {
                CompletableFuture<?> b = lo == hi ? a : hi == mid + 1 ? cfs[hi] : andTree(cfs, mid + 1, hi);
                if (b != null) {
                    if (!d.biRelay(a, b)) {
                        BiRelay<?, ?> c = new BiRelay(d, a, b);
                        a.bipush(b, c);
                        c.tryFire(0);
                    }
                }
            }
            throw new NullPointerException();
        }
        return d;
    }

    final void orpush(CompletableFuture<?> b, BiCompletion<?, ?, ?> c) {
        if (c != null) {
            while (true) {
                if ((b != null && b.result != null) || this.result != null) {
                    return;
                }
                if (!tryPushStack(c)) {
                    lazySetNext(c, null);
                } else if (b != null && b != this && b.result == null) {
                    Completion q = new CoCompletion(c);
                    while (this.result == null && b.result == null && !b.tryPushStack(q)) {
                        lazySetNext(q, null);
                    }
                    return;
                } else {
                    return;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r2 != null) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final <R, S extends R> boolean orApply(CompletableFuture<R> a, CompletableFuture<S> b, Function<? super R, ? extends T> f, OrApply<R, S, T> c) {
        if (!(a == null || b == null)) {
            R r = a.result;
            if (r == null) {
                r = b.result;
            }
            if (f != null) {
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    R rr = r;
                    completeValue(f.apply(r));
                }
                return true;
            }
        }
        return false;
    }

    private <U extends T, V> CompletableFuture<V> orApplyStage(Executor e, CompletionStage<U> o, Function<? super T, ? extends V> f) {
        if (f != null) {
            CompletableFuture<U> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<V> d = newIncompleteFuture();
                if (!(e == null && d.orApply(this, b, f, null))) {
                    OrApply<T, U, V> c = new OrApply(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r2 != null) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final <R, S extends R> boolean orAccept(CompletableFuture<R> a, CompletableFuture<S> b, Consumer<? super R> f, OrAccept<R, S> c) {
        if (!(a == null || b == null)) {
            R r = a.result;
            if (r == null) {
                r = b.result;
            }
            if (f != null) {
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    R rr = r;
                    f.accept(r);
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private <U extends T> CompletableFuture<Void> orAcceptStage(Executor e, CompletionStage<U> o, Consumer<? super T> f) {
        if (f != null) {
            CompletableFuture<U> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (!(e == null && d.orAccept(this, b, f, null))) {
                    OrAccept<T, U> c = new OrAccept(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r2 != null) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean orRun(CompletableFuture<?> a, CompletableFuture<?> b, Runnable f, OrRun<?, ?> c) {
        if (!(a == null || b == null)) {
            Object r = a.result;
            if (r == null) {
                r = b.result;
            }
            if (f != null) {
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        }
                    }
                    f.run();
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Void> orRunStage(Executor e, CompletionStage<?> o, Runnable f) {
        if (f != null) {
            CompletableFuture<?> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (!(e == null && d.orRun(this, b, f, null))) {
                    OrRun<T, ?> c = new OrRun(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r0 != null) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean orRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        if (!(a == null || b == null)) {
            Object r = a.result;
            if (r == null) {
                r = b.result;
            }
            if (this.result == null) {
                completeRelay(r);
            }
            return true;
        }
        return false;
    }

    static CompletableFuture<Object> orTree(CompletableFuture<?>[] cfs, int lo, int hi) {
        CompletableFuture<Object> d = new CompletableFuture();
        if (lo <= hi) {
            CompletableFuture<?> a;
            int mid = (lo + hi) >>> 1;
            if (lo == mid) {
                a = cfs[lo];
            } else {
                a = orTree(cfs, lo, mid);
            }
            if (a != null) {
                CompletableFuture<?> b = lo == hi ? a : hi == mid + 1 ? cfs[hi] : orTree(cfs, mid + 1, hi);
                if (b != null) {
                    if (!d.orRelay(a, b)) {
                        OrRelay<?, ?> c = new OrRelay(d, a, b);
                        a.orpush(b, c);
                        c.tryFire(0);
                    }
                }
            }
            throw new NullPointerException();
        }
        return d;
    }

    static <U> CompletableFuture<U> asyncSupplyStage(Executor e, Supplier<U> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<U> d = new CompletableFuture();
        e.execute(new AsyncSupply(d, f));
        return d;
    }

    static CompletableFuture<Void> asyncRunStage(Executor e, Runnable f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = new CompletableFuture();
        e.execute(new AsyncRun(d, f));
        return d;
    }

    private Object waitingGet(boolean interruptible) {
        Object r;
        Signaller q = null;
        boolean queued = false;
        int spins = SPINS;
        while (true) {
            r = this.result;
            if (r == null) {
                if (spins <= 0) {
                    if (q != null) {
                        if (queued) {
                            try {
                                ForkJoinPool.managedBlock(q);
                            } catch (InterruptedException e) {
                                q.interrupted = true;
                            }
                            if (q.interrupted && interruptible) {
                                break;
                            }
                        }
                        queued = tryPushStack(q);
                    } else {
                        q = new Signaller(interruptible, 0, 0);
                    }
                } else if (ThreadLocalRandom.nextSecondarySeed() >= 0) {
                    spins--;
                }
            } else {
                break;
            }
        }
        if (q != null) {
            q.thread = null;
            if (q.interrupted) {
                if (interruptible) {
                    cleanStack();
                } else {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (r != null) {
            postComplete();
        }
        return r;
    }

    private Object timedGet(long nanos) throws TimeoutException {
        if (Thread.interrupted()) {
            return null;
        }
        if (nanos > 0) {
            Object r;
            long d = System.nanoTime() + nanos;
            long deadline = d == 0 ? 1 : d;
            Signaller q = null;
            boolean queued = false;
            while (true) {
                r = this.result;
                if (r != null) {
                    break;
                } else if (q == null) {
                    q = new Signaller(true, nanos, deadline);
                } else if (!queued) {
                    queued = tryPushStack(q);
                } else if (q.nanos <= 0) {
                    break;
                } else {
                    try {
                        ForkJoinPool.managedBlock(q);
                    } catch (InterruptedException e) {
                        q.interrupted = true;
                    }
                    if (q.interrupted) {
                        break;
                    }
                }
            }
            if (q != null) {
                q.thread = null;
            }
            if (r != null) {
                postComplete();
            } else {
                cleanStack();
            }
            if (r != null || (q != null && q.interrupted)) {
                return r;
            }
        }
        throw new TimeoutException();
    }

    public CompletableFuture() {
    }

    CompletableFuture(Object r) {
        this.result = r;
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return asyncSupplyStage(ASYNC_POOL, supplier);
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        return asyncSupplyStage(screenExecutor(executor), supplier);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return asyncRunStage(ASYNC_POOL, runnable);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        return asyncRunStage(screenExecutor(executor), runnable);
    }

    public static <U> CompletableFuture<U> completedFuture(U value) {
        if (value == null) {
            value = NIL;
        }
        return new CompletableFuture(value);
    }

    public boolean isDone() {
        return this.result != null;
    }

    public T get() throws InterruptedException, ExecutionException {
        Object r = this.result;
        if (r == null) {
            r = waitingGet(true);
        }
        return reportGet(r);
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        Object r = this.result;
        if (r == null) {
            r = timedGet(nanos);
        }
        return reportGet(r);
    }

    public T join() {
        Object r = this.result;
        if (r == null) {
            r = waitingGet(false);
        }
        return reportJoin(r);
    }

    public T getNow(T valueIfAbsent) {
        Object r = this.result;
        return r == null ? valueIfAbsent : reportJoin(r);
    }

    public boolean complete(T value) {
        boolean triggered = completeValue(value);
        postComplete();
        return triggered;
    }

    public boolean completeExceptionally(Throwable ex) {
        if (ex == null) {
            throw new NullPointerException();
        }
        boolean triggered = internalComplete(new AltResult(ex));
        postComplete();
        return triggered;
    }

    public /* bridge */ /* synthetic */ CompletionStage thenApply(Function fn) {
        return thenApply(fn);
    }

    public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return uniApplyStage(null, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenApplyAsync(Function fn) {
        return thenApplyAsync(fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return uniApplyStage(defaultExecutor(), fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenApplyAsync(Function fn, Executor executor) {
        return thenApplyAsync(fn, executor);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        return uniApplyStage(screenExecutor(executor), fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenAccept(Consumer action) {
        return thenAccept(action);
    }

    public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return uniAcceptStage(null, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenAcceptAsync(Consumer action) {
        return thenAcceptAsync(action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
        return uniAcceptStage(defaultExecutor(), action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenAcceptAsync(Consumer action, Executor executor) {
        return thenAcceptAsync(action, executor);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return uniAcceptStage(screenExecutor(executor), action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenRun(Runnable action) {
        return thenRun(action);
    }

    public CompletableFuture<Void> thenRun(Runnable action) {
        return uniRunStage(null, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenRunAsync(Runnable action) {
        return thenRunAsync(action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action) {
        return uniRunStage(defaultExecutor(), action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenRunAsync(Runnable action, Executor executor) {
        return thenRunAsync(action, executor);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        return uniRunStage(screenExecutor(executor), action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenCombine(CompletionStage other, BiFunction fn) {
        return thenCombine(other, fn);
    }

    public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return biApplyStage(null, other, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenCombineAsync(CompletionStage other, BiFunction fn) {
        return thenCombineAsync(other, fn);
    }

    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return biApplyStage(defaultExecutor(), other, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenCombineAsync(CompletionStage other, BiFunction fn, Executor executor) {
        return thenCombineAsync(other, fn, executor);
    }

    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return biApplyStage(screenExecutor(executor), other, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenAcceptBoth(CompletionStage other, BiConsumer action) {
        return thenAcceptBoth(other, action);
    }

    public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return biAcceptStage(null, other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenAcceptBothAsync(CompletionStage other, BiConsumer action) {
        return thenAcceptBothAsync(other, action);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return biAcceptStage(defaultExecutor(), other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenAcceptBothAsync(CompletionStage other, BiConsumer action, Executor executor) {
        return thenAcceptBothAsync(other, action, executor);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return biAcceptStage(screenExecutor(executor), other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage runAfterBoth(CompletionStage other, Runnable action) {
        return runAfterBoth(other, action);
    }

    public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return biRunStage(null, other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage runAfterBothAsync(CompletionStage other, Runnable action) {
        return runAfterBothAsync(other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return biRunStage(defaultExecutor(), other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage runAfterBothAsync(CompletionStage other, Runnable action, Executor executor) {
        return runAfterBothAsync(other, action, executor);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return biRunStage(screenExecutor(executor), other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage applyToEither(CompletionStage other, Function fn) {
        return applyToEither(other, fn);
    }

    public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return orApplyStage(null, other, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage applyToEitherAsync(CompletionStage other, Function fn) {
        return applyToEitherAsync(other, fn);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return orApplyStage(defaultExecutor(), other, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage applyToEitherAsync(CompletionStage other, Function fn, Executor executor) {
        return applyToEitherAsync(other, fn, executor);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return orApplyStage(screenExecutor(executor), other, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage acceptEither(CompletionStage other, Consumer action) {
        return acceptEither(other, action);
    }

    public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return orAcceptStage(null, other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage acceptEitherAsync(CompletionStage other, Consumer action) {
        return acceptEitherAsync(other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return orAcceptStage(defaultExecutor(), other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage acceptEitherAsync(CompletionStage other, Consumer action, Executor executor) {
        return acceptEitherAsync(other, action, executor);
    }

    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        return orAcceptStage(screenExecutor(executor), other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage runAfterEither(CompletionStage other, Runnable action) {
        return runAfterEither(other, action);
    }

    public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return orRunStage(null, other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage runAfterEitherAsync(CompletionStage other, Runnable action) {
        return runAfterEitherAsync(other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return orRunStage(defaultExecutor(), other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage runAfterEitherAsync(CompletionStage other, Runnable action, Executor executor) {
        return runAfterEitherAsync(other, action, executor);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return orRunStage(screenExecutor(executor), other, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenCompose(Function fn) {
        return thenCompose(fn);
    }

    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return uniComposeStage(null, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenComposeAsync(Function fn) {
        return thenComposeAsync(fn);
    }

    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return uniComposeStage(defaultExecutor(), fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage thenComposeAsync(Function fn, Executor executor) {
        return thenComposeAsync(fn, executor);
    }

    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        return uniComposeStage(screenExecutor(executor), fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage whenComplete(BiConsumer action) {
        return whenComplete(action);
    }

    public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return uniWhenCompleteStage(null, action);
    }

    public /* bridge */ /* synthetic */ CompletionStage whenCompleteAsync(BiConsumer action) {
        return whenCompleteAsync(action);
    }

    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return uniWhenCompleteStage(defaultExecutor(), action);
    }

    public /* bridge */ /* synthetic */ CompletionStage whenCompleteAsync(BiConsumer action, Executor executor) {
        return whenCompleteAsync(action, executor);
    }

    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return uniWhenCompleteStage(screenExecutor(executor), action);
    }

    public /* bridge */ /* synthetic */ CompletionStage handle(BiFunction fn) {
        return handle(fn);
    }

    public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return uniHandleStage(null, fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage handleAsync(BiFunction fn) {
        return handleAsync(fn);
    }

    public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return uniHandleStage(defaultExecutor(), fn);
    }

    public /* bridge */ /* synthetic */ CompletionStage handleAsync(BiFunction fn, Executor executor) {
        return handleAsync(fn, executor);
    }

    public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return uniHandleStage(screenExecutor(executor), fn);
    }

    public CompletableFuture<T> toCompletableFuture() {
        return this;
    }

    public /* bridge */ /* synthetic */ CompletionStage exceptionally(Function fn) {
        return exceptionally(fn);
    }

    public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return uniExceptionallyStage(fn);
    }

    public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
        return andTree(cfs, 0, cfs.length - 1);
    }

    public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
        return orTree(cfs, 0, cfs.length - 1);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled;
        if (this.result == null) {
            cancelled = internalComplete(new AltResult(new CancellationException()));
        } else {
            cancelled = false;
        }
        postComplete();
        return !cancelled ? isCancelled() : true;
    }

    public boolean isCancelled() {
        Object r = this.result;
        if (r instanceof AltResult) {
            return ((AltResult) r).ex instanceof CancellationException;
        }
        return false;
    }

    public boolean isCompletedExceptionally() {
        AltResult r = this.result;
        return (r instanceof AltResult) && r != NIL;
    }

    public void obtrudeValue(T value) {
        if (value == null) {
            value = NIL;
        }
        this.result = value;
        postComplete();
    }

    public void obtrudeException(Throwable ex) {
        if (ex == null) {
            throw new NullPointerException();
        }
        this.result = new AltResult(ex);
        postComplete();
    }

    public int getNumberOfDependents() {
        int count = 0;
        for (Completion p = this.stack; p != null; p = p.next) {
            count++;
        }
        return count;
    }

    public String toString() {
        String str;
        Object r = this.result;
        int count = 0;
        for (Completion p = this.stack; p != null; p = p.next) {
            count++;
        }
        StringBuilder append = new StringBuilder().append(super.toString());
        if (r == null) {
            if (count == 0) {
                str = "[Not completed]";
            } else {
                str = "[Not completed, " + count + " dependents]";
            }
        } else if (!(r instanceof AltResult) || ((AltResult) r).ex == null) {
            str = "[Completed normally]";
        } else {
            str = "[Completed exceptionally]";
        }
        return append.append(str).toString();
    }

    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new CompletableFuture();
    }

    public Executor defaultExecutor() {
        return ASYNC_POOL;
    }

    public CompletableFuture<T> copy() {
        return uniCopyStage();
    }

    public CompletionStage<T> minimalCompletionStage() {
        return uniAsMinimalStage();
    }

    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
        if (supplier == null || executor == null) {
            throw new NullPointerException();
        }
        executor.execute(new AsyncSupply(this, supplier));
        return this;
    }

    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
        return completeAsync(supplier, defaultExecutor());
    }

    public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException();
        }
        if (this.result == null) {
            whenComplete(new Canceller(Delayer.delay(new Timeout(this), timeout, unit)));
        }
        return this;
    }

    public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException();
        }
        if (this.result == null) {
            whenComplete(new Canceller(Delayer.delay(new DelayedCompleter(this, value), timeout, unit)));
        }
        return this;
    }

    public static Executor delayedExecutor(long delay, TimeUnit unit, Executor executor) {
        if (unit != null && executor != null) {
            return new DelayedExecutor(delay, unit, executor);
        }
        throw new NullPointerException();
    }

    public static Executor delayedExecutor(long delay, TimeUnit unit) {
        if (unit != null) {
            return new DelayedExecutor(delay, unit, ASYNC_POOL);
        }
        throw new NullPointerException();
    }

    public static <U> CompletionStage<U> completedStage(U value) {
        if (value == null) {
            value = NIL;
        }
        return new MinimalStage(value);
    }

    public static <U> CompletableFuture<U> failedFuture(Throwable ex) {
        if (ex != null) {
            return new CompletableFuture(new AltResult(ex));
        }
        throw new NullPointerException();
    }

    public static <U> CompletionStage<U> failedStage(Throwable ex) {
        if (ex != null) {
            return new MinimalStage(new AltResult(ex));
        }
        throw new NullPointerException();
    }
}
