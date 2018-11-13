package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
public abstract class AbstractExecutorService implements ExecutorService {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f126-assertionsDisabled = false;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.AbstractExecutorService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.AbstractExecutorService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.AbstractExecutorService.<clinit>():void");
    }

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new FutureTask(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask(callable);
    }

    public Future<?> submit(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

    public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    /* JADX WARNING: Missing block: B:27:0x0068, code:
            if (r7 != null) goto L_0x00a1;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r6 = new java.util.concurrent.ExecutionException();
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            throw r6;
     */
    /* JADX WARNING: Missing block: B:50:0x00a1, code:
            r6 = r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks, boolean timed, long nanos) throws InterruptedException, ExecutionException, TimeoutException {
        Throwable th;
        if (tasks == null) {
            throw new NullPointerException();
        }
        int ntasks = tasks.size();
        if (ntasks == 0) {
            throw new IllegalArgumentException();
        }
        long deadline;
        ArrayList<Future<T>> futures = new ArrayList(ntasks);
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService(this);
        ExecutionException ee = null;
        if (timed) {
            try {
                deadline = System.nanoTime() + nanos;
            } catch (Throwable th2) {
                th = th2;
            }
        } else {
            deadline = 0;
        }
        Iterator<? extends Callable<T>> it = tasks.iterator();
        futures.add(ecs.submit((Callable) it.next()));
        ntasks--;
        int active = 1;
        while (true) {
            ExecutionException ee2;
            try {
                ee2 = ee;
                Future<T> f = ecs.poll();
                if (f == null) {
                    if (ntasks > 0) {
                        ntasks--;
                        futures.add(ecs.submit((Callable) it.next()));
                        active++;
                    } else if (active == 0) {
                        break;
                    } else if (timed) {
                        f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                        if (f == null) {
                            throw new TimeoutException();
                        }
                        nanos = deadline - System.nanoTime();
                    } else {
                        f = ecs.take();
                    }
                }
                if (f != null) {
                    active--;
                    T t = f.get();
                    cancelAll(futures);
                    return t;
                }
                ee = ee2;
            } catch (ExecutionException eex) {
                ee = eex;
            } catch (Throwable rex) {
                ee = new ExecutionException(rex);
            } catch (Throwable th3) {
                th = th3;
                ee = ee2;
                cancelAll(futures);
                throw th;
            }
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TimeoutException e) {
            if (f126-assertionsDisabled) {
                return null;
            }
            throw new AssertionError();
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        ArrayList<Future<T>> futures = new ArrayList(tasks.size());
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = newTaskFor(t);
                futures.add(f);
                execute(f);
            }
            int size = futures.size();
            for (int i = 0; i < size; i++) {
                Future<T> f2 = (Future) futures.get(i);
                if (!f2.isDone()) {
                    try {
                        f2.get();
                    } catch (CancellationException e) {
                    } catch (ExecutionException e2) {
                    }
                }
            }
            return futures;
        } catch (Throwable th) {
            cancelAll(futures);
        }
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        ArrayList<Future<T>> futures = new ArrayList(tasks.size());
        int j = 0;
        try {
            for (Callable<T> t : tasks) {
                futures.add(newTaskFor(t));
            }
            int size = futures.size();
            for (int i = 0; i < size; i++) {
                long j2;
                if (i == 0) {
                    j2 = nanos;
                } else {
                    j2 = deadline - System.nanoTime();
                }
                if (j2 <= 0) {
                    cancelAll(futures, j);
                    return futures;
                }
                execute((Runnable) futures.get(i));
            }
            while (j < size) {
                Future<T> f = (Future) futures.get(j);
                if (!f.isDone()) {
                    try {
                        f.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                    } catch (CancellationException e) {
                    } catch (ExecutionException e2) {
                    } catch (TimeoutException e3) {
                    }
                }
                j++;
            }
            return futures;
        } catch (Throwable th) {
            cancelAll(futures);
        }
    }

    private static <T> void cancelAll(ArrayList<Future<T>> futures) {
        cancelAll(futures, 0);
    }

    private static <T> void cancelAll(ArrayList<Future<T>> futures, int j) {
        int size = futures.size();
        while (j < size) {
            ((Future) futures.get(j)).cancel(true);
            j++;
        }
    }
}
