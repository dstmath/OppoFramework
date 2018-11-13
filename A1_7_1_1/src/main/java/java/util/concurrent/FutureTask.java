package java.util.concurrent;

import java.util.concurrent.locks.LockSupport;
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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class FutureTask<V> implements RunnableFuture<V> {
    private static final int CANCELLED = 4;
    private static final int COMPLETING = 1;
    private static final int EXCEPTIONAL = 3;
    private static final int INTERRUPTED = 6;
    private static final int INTERRUPTING = 5;
    private static final int NEW = 0;
    private static final int NORMAL = 2;
    private static final long RUNNER = 0;
    private static final long STATE = 0;
    private static final Unsafe U = null;
    private static final long WAITERS = 0;
    private Callable<V> callable;
    private Object outcome;
    private volatile Thread runner;
    private volatile int state;
    private volatile WaitNode waiters;

    static final class WaitNode {
        volatile WaitNode next;
        volatile Thread thread = Thread.currentThread();

        WaitNode() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.FutureTask.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.FutureTask.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.FutureTask.<clinit>():void");
    }

    private V report(int s) throws ExecutionException {
        Object x = this.outcome;
        if (s == 2) {
            return x;
        }
        if (s >= 4) {
            throw new CancellationException();
        }
        throw new ExecutionException((Throwable) x);
    }

    public FutureTask(Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        this.callable = callable;
        this.state = 0;
    }

    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = 0;
    }

    public boolean isCancelled() {
        return this.state >= 4;
    }

    public boolean isDone() {
        return this.state != 0;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean compareAndSwapInt;
        if (this.state == 0) {
            compareAndSwapInt = U.compareAndSwapInt(this, STATE, 0, mayInterruptIfRunning ? 5 : 4);
        } else {
            compareAndSwapInt = false;
        }
        if (!compareAndSwapInt) {
            return false;
        }
        if (mayInterruptIfRunning) {
            try {
                Thread t = this.runner;
                if (t != null) {
                    t.interrupt();
                }
                U.putOrderedInt(this, STATE, 6);
            } catch (Throwable th) {
                finishCompletion();
            }
        }
        finishCompletion();
        return true;
    }

    public V get() throws InterruptedException, ExecutionException {
        int s = this.state;
        if (s <= 1) {
            s = awaitDone(false, 0);
        }
        return report(s);
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null) {
            throw new NullPointerException();
        }
        int s = this.state;
        if (s <= 1) {
            s = awaitDone(true, unit.toNanos(timeout));
            if (s <= 1) {
                throw new TimeoutException();
            }
        }
        return report(s);
    }

    protected void done() {
    }

    protected void set(V v) {
        if (U.compareAndSwapInt(this, STATE, 0, 1)) {
            this.outcome = v;
            U.putOrderedInt(this, STATE, 2);
            finishCompletion();
        }
    }

    protected void setException(Throwable t) {
        if (U.compareAndSwapInt(this, STATE, 0, 1)) {
            this.outcome = t;
            U.putOrderedInt(this, STATE, 3);
            finishCompletion();
        }
    }

    public void run() {
        int s;
        if (this.state == 0) {
            if (U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread())) {
                Object result;
                boolean ran;
                try {
                    Callable<V> c = this.callable;
                    if (c != null && this.state == 0) {
                        result = c.call();
                        ran = true;
                        if (ran) {
                            set(result);
                        }
                    }
                } catch (Throwable th) {
                    this.runner = null;
                    s = this.state;
                    if (s >= 5) {
                        handlePossibleCancellationInterrupt(s);
                    }
                }
                this.runner = null;
                s = this.state;
                if (s >= 5) {
                    handlePossibleCancellationInterrupt(s);
                }
            }
        }
    }

    protected boolean runAndReset() {
        if (this.state == 0) {
            if (U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread())) {
                boolean z;
                boolean ran = false;
                int s = this.state;
                try {
                    Callable<V> c = this.callable;
                    if (c != null && s == 0) {
                        c.call();
                        ran = true;
                    }
                } catch (Throwable th) {
                    this.runner = null;
                    s = this.state;
                    if (s >= 5) {
                        handlePossibleCancellationInterrupt(s);
                    }
                }
                this.runner = null;
                s = this.state;
                if (s >= 5) {
                    handlePossibleCancellationInterrupt(s);
                }
                if (ran && s == 0) {
                    z = true;
                } else {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    private void handlePossibleCancellationInterrupt(int s) {
        if (s == 5) {
            while (this.state == 5) {
                Thread.yield();
            }
        }
    }

    private void finishCompletion() {
        WaitNode q;
        do {
            q = this.waiters;
            if (q == null) {
                break;
            }
        } while (!U.compareAndSwapObject(this, WAITERS, q, null));
        while (true) {
            Thread t = q.thread;
            if (t != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
            WaitNode next = q.next;
            if (next == null) {
                break;
            }
            q.next = null;
            q = next;
        }
        done();
        this.callable = null;
    }

    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        long startTime = 0;
        WaitNode q = null;
        boolean queued = false;
        while (true) {
            int s = this.state;
            if (s > 1) {
                if (q != null) {
                    q.thread = null;
                }
                return s;
            } else if (s == 1) {
                Thread.yield();
            } else if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            } else if (q == null) {
                if (timed && nanos <= 0) {
                    return s;
                }
                q = new WaitNode();
            } else if (!queued) {
                Unsafe unsafe = U;
                long j = WAITERS;
                WaitNode waitNode = this.waiters;
                q.next = waitNode;
                queued = unsafe.compareAndSwapObject(this, j, waitNode, q);
            } else if (timed) {
                long parkNanos;
                if (startTime == 0) {
                    startTime = System.nanoTime();
                    if (startTime == 0) {
                        startTime = 1;
                    }
                    parkNanos = nanos;
                } else {
                    long elapsed = System.nanoTime() - startTime;
                    if (elapsed >= nanos) {
                        removeWaiter(q);
                        return this.state;
                    }
                    parkNanos = nanos - elapsed;
                }
                if (this.state < 1) {
                    LockSupport.parkNanos(this, parkNanos);
                }
            } else {
                LockSupport.park(this);
            }
        }
    }

    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            while (true) {
                WaitNode pred = null;
                WaitNode q = this.waiters;
                while (q != null) {
                    WaitNode s = q.next;
                    if (q.thread != null) {
                        pred = q;
                    } else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) {
                        }
                    } else {
                        if (U.compareAndSwapObject(this, WAITERS, q, s)) {
                        }
                    }
                    q = s;
                }
                return;
            }
        }
    }
}
