package java.lang;

import android.system.Os;
import android.system.OsConstants;
import dalvik.system.VMRuntime;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.FinalizerReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.util.EmptyArray;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public final class Daemons {
    private static final long MAX_FINALIZE_NANOS = 120000000000L;
    private static final int NANOS_PER_MILLI = 1000000;
    private static final int NANOS_PER_SECOND = 1000000000;

    private static abstract class Daemon implements Runnable {
        private String name;
        private Thread thread;

        public abstract void run();

        protected Daemon(String name) {
            this.name = name;
        }

        public synchronized void start() {
            if (this.thread != null) {
                throw new IllegalStateException("already running");
            }
            this.thread = new Thread(ThreadGroup.systemThreadGroup, this, this.name);
            this.thread.setDaemon(true);
            this.thread.start();
        }

        protected synchronized boolean isRunning() {
            return this.thread != null;
        }

        public synchronized void interrupt() {
            interrupt(this.thread);
        }

        public synchronized void interrupt(Thread thread) {
            if (thread == null) {
                throw new IllegalStateException("not running");
            }
            thread.interrupt();
        }

        public void stop() {
            Thread threadToStop;
            synchronized (this) {
                threadToStop = this.thread;
                this.thread = null;
            }
            if (threadToStop == null) {
                throw new IllegalStateException("not running");
            }
            interrupt(threadToStop);
            while (true) {
                try {
                    threadToStop.join();
                    return;
                } catch (InterruptedException e) {
                } catch (OutOfMemoryError e2) {
                }
            }
        }

        public synchronized StackTraceElement[] getStackTrace() {
            return this.thread != null ? this.thread.getStackTrace() : EmptyArray.STACK_TRACE_ELEMENT;
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class FinalizerDaemon extends Daemon {
        private static final FinalizerDaemon INSTANCE = null;
        private Object finalizingObject;
        private volatile Object oldFinalizingObject;
        private final AtomicInteger progressCounter;
        private final ReferenceQueue<Object> queue;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.Daemons.FinalizerDaemon.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.Daemons.FinalizerDaemon.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Daemons.FinalizerDaemon.<clinit>():void");
        }

        FinalizerDaemon() {
            super("FinalizerDaemon");
            this.queue = FinalizerReference.queue;
            this.progressCounter = new AtomicInteger(0);
            this.finalizingObject = null;
        }

        public void run() {
            int localProgressCounter = this.progressCounter.get();
            while (isRunning()) {
                try {
                    FinalizerReference<?> finalizingReference = (FinalizerReference) this.queue.poll();
                    if (finalizingReference != null) {
                        this.finalizingObject = finalizingReference.get();
                        localProgressCounter++;
                        this.progressCounter.lazySet(localProgressCounter);
                    } else {
                        this.finalizingObject = null;
                        localProgressCounter++;
                        this.progressCounter.lazySet(localProgressCounter);
                        FinalizerWatchdogDaemon.INSTANCE.goToSleep();
                        finalizingReference = (FinalizerReference) this.queue.remove();
                        this.finalizingObject = finalizingReference.get();
                        localProgressCounter++;
                        this.progressCounter.set(localProgressCounter);
                        FinalizerWatchdogDaemon.INSTANCE.wakeUp();
                    }
                    doFinalize(finalizingReference);
                } catch (InterruptedException e) {
                } catch (OutOfMemoryError e2) {
                }
            }
        }

        @FindBugsSuppressWarnings({"FI_EXPLICIT_INVOCATION"})
        private void doFinalize(FinalizerReference<?> reference) {
            FinalizerReference.remove(reference);
            Object object = reference.get();
            reference.clear();
            try {
                object.finalize();
            } catch (Throwable th) {
                this.finalizingObject = null;
            }
            this.finalizingObject = null;
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class FinalizerWatchdogDaemon extends Daemon {
        private static final FinalizerWatchdogDaemon INSTANCE = null;
        private boolean needToWork;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.Daemons.FinalizerWatchdogDaemon.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.Daemons.FinalizerWatchdogDaemon.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Daemons.FinalizerWatchdogDaemon.<clinit>():void");
        }

        FinalizerWatchdogDaemon() {
            super("FinalizerWatchdogDaemon");
            this.needToWork = true;
        }

        public void run() {
            while (isRunning()) {
                if (sleepUntilNeeded()) {
                    Object finalizing = waitForFinalization();
                    if (!(finalizing == null || VMRuntime.getRuntime().isDebuggerActive())) {
                        finalizerTimedOut(finalizing);
                        return;
                    }
                }
            }
        }

        private synchronized boolean sleepUntilNeeded() {
            while (!this.needToWork) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return false;
                } catch (OutOfMemoryError e2) {
                    return false;
                }
            }
            return true;
        }

        private synchronized void goToSleep() {
            this.needToWork = false;
        }

        private synchronized void wakeUp() {
            this.needToWork = true;
            notify();
        }

        private synchronized boolean getNeedToWork() {
            return this.needToWork;
        }

        private boolean sleepFor(long durationNanos) {
            long startNanos = System.nanoTime();
            while (true) {
                long sleepMills = (durationNanos - (System.nanoTime() - startNanos)) / 1000000;
                if (sleepMills <= 0) {
                    return true;
                }
                try {
                    Thread.sleep(sleepMills);
                } catch (InterruptedException e) {
                    if (!isRunning()) {
                        return false;
                    }
                } catch (OutOfMemoryError e2) {
                    if (!isRunning()) {
                        return false;
                    }
                }
            }
        }

        private Object waitForFinalization() {
            long startCount = (long) FinalizerDaemon.INSTANCE.progressCounter.get();
            FinalizerDaemon.INSTANCE.oldFinalizingObject = FinalizerDaemon.INSTANCE.finalizingObject;
            if (sleepFor(Daemons.MAX_FINALIZE_NANOS) && getNeedToWork() && ((long) FinalizerDaemon.INSTANCE.progressCounter.get()) == startCount) {
                Object finalizing = FinalizerDaemon.INSTANCE.finalizingObject;
                sleepFor(500000000);
                if (getNeedToWork() && ((long) FinalizerDaemon.INSTANCE.progressCounter.get()) == startCount) {
                    return finalizing;
                }
                return null;
            }
            return null;
        }

        private static void finalizerTimedOut(Object object) {
            String message = object.getClass().getName() + ".finalize() timed out after " + 120 + " seconds";
            Exception syntheticException = new TimeoutException(message);
            syntheticException.setStackTrace(FinalizerDaemon.INSTANCE.getStackTrace());
            UncaughtExceptionHandler h = Thread.getDefaultUncaughtExceptionHandler();
            try {
                Os.kill(Os.getpid(), OsConstants.SIGQUIT);
                Thread.sleep(5000);
            } catch (Exception e) {
                System.logE("failed to send SIGQUIT", e);
            } catch (OutOfMemoryError e2) {
            }
            if (h == null) {
                System.logE(message, syntheticException);
                System.exit(2);
            }
            if (FinalizerDaemon.INSTANCE.oldFinalizingObject == FinalizerDaemon.INSTANCE.finalizingObject) {
                h.uncaughtException(Thread.currentThread(), syntheticException);
            }
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class HeapTaskDaemon extends Daemon {
        private static final HeapTaskDaemon INSTANCE = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.Daemons.HeapTaskDaemon.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.Daemons.HeapTaskDaemon.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Daemons.HeapTaskDaemon.<clinit>():void");
        }

        HeapTaskDaemon() {
            super("HeapTaskDaemon");
        }

        public synchronized void interrupt(Thread thread) {
            VMRuntime.getRuntime().stopHeapTaskProcessor();
        }

        public void run() {
            synchronized (this) {
                if (isRunning()) {
                    VMRuntime.getRuntime().startHeapTaskProcessor();
                }
            }
            VMRuntime.getRuntime().runHeapTasks();
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class ReferenceQueueDaemon extends Daemon {
        private static final ReferenceQueueDaemon INSTANCE = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.Daemons.ReferenceQueueDaemon.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.Daemons.ReferenceQueueDaemon.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Daemons.ReferenceQueueDaemon.<clinit>():void");
        }

        ReferenceQueueDaemon() {
            super("ReferenceQueueDaemon");
        }

        public void run() {
            while (isRunning()) {
                try {
                    Reference<?> list;
                    synchronized (ReferenceQueue.class) {
                        while (ReferenceQueue.unenqueued == null) {
                            ReferenceQueue.class.wait();
                        }
                        list = ReferenceQueue.unenqueued;
                        ReferenceQueue.unenqueued = null;
                    }
                    ReferenceQueue.enqueuePending(list);
                } catch (InterruptedException e) {
                } catch (OutOfMemoryError e2) {
                }
            }
        }
    }

    public static void start() {
        ReferenceQueueDaemon.INSTANCE.start();
        FinalizerDaemon.INSTANCE.start();
        FinalizerWatchdogDaemon.INSTANCE.start();
        HeapTaskDaemon.INSTANCE.start();
    }

    public static void stop() {
        HeapTaskDaemon.INSTANCE.stop();
        ReferenceQueueDaemon.INSTANCE.stop();
        FinalizerDaemon.INSTANCE.stop();
        FinalizerWatchdogDaemon.INSTANCE.stop();
    }

    public static void requestHeapTrim() {
        VMRuntime.getRuntime().requestHeapTrim();
    }

    public static void requestGC() {
        VMRuntime.getRuntime().requestConcurrentGC();
    }
}
