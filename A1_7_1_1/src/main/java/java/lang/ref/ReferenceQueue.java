package java.lang.ref;

import sun.misc.Cleaner;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class ReferenceQueue<T> {
    private static final Reference sQueueNextUnenqueued = null;
    public static Reference<?> unenqueued;
    private Reference<? extends T> head;
    private final Object lock;
    private Reference<? extends T> tail;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.ref.ReferenceQueue.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.ref.ReferenceQueue.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ref.ReferenceQueue.<clinit>():void");
    }

    public ReferenceQueue() {
        this.head = null;
        this.tail = null;
        this.lock = new Object();
    }

    private boolean enqueueLocked(Reference<? extends T> r) {
        if (r.queueNext != null) {
            return false;
        }
        if (r instanceof Cleaner) {
            ((Cleaner) r).clean();
            r.queueNext = sQueueNextUnenqueued;
            return true;
        }
        if (this.tail == null) {
            this.head = r;
        } else {
            this.tail.queueNext = r;
        }
        this.tail = r;
        this.tail.queueNext = r;
        return true;
    }

    boolean isEnqueued(Reference<? extends T> reference) {
        boolean z = false;
        synchronized (this.lock) {
            if (!(reference.queueNext == null || reference.queueNext == sQueueNextUnenqueued)) {
                z = true;
            }
        }
        return z;
    }

    boolean enqueue(Reference<? extends T> reference) {
        synchronized (this.lock) {
            if (enqueueLocked(reference)) {
                this.lock.notifyAll();
                return true;
            }
            return false;
        }
    }

    private Reference<? extends T> reallyPollLocked() {
        if (this.head == null) {
            return null;
        }
        Reference<? extends T> r = this.head;
        if (this.head == this.tail) {
            this.tail = null;
            this.head = null;
        } else {
            this.head = this.head.queueNext;
        }
        r.queueNext = sQueueNextUnenqueued;
        return r;
    }

    public Reference<? extends T> poll() {
        synchronized (this.lock) {
            if (this.head == null) {
                return null;
            }
            Reference<? extends T> reallyPollLocked = reallyPollLocked();
            return reallyPollLocked;
        }
    }

    public Reference<? extends T> remove(long timeout) throws IllegalArgumentException, InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout value");
        }
        synchronized (this.lock) {
            Reference<? extends T> r = reallyPollLocked();
            if (r != null) {
                return r;
            }
            long start = timeout == 0 ? 0 : System.nanoTime();
            while (true) {
                this.lock.wait(timeout);
                r = reallyPollLocked();
                if (r != null) {
                    return r;
                } else if (timeout != 0) {
                    long end = System.nanoTime();
                    timeout -= (end - start) / 1000000;
                    if (timeout <= 0) {
                        return null;
                    }
                    start = end;
                }
            }
        }
    }

    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0);
    }

    public static void enqueuePending(Reference<?> list) {
        Reference<?> start = list;
        do {
            ReferenceQueue queue = list.queue;
            Reference<?> next;
            if (queue == null) {
                next = list.pendingNext;
                list.pendingNext = list;
                list = next;
                continue;
            } else {
                synchronized (queue.lock) {
                    do {
                        next = list.pendingNext;
                        list.pendingNext = list;
                        queue.enqueueLocked(list);
                        list = next;
                        if (next == start) {
                            break;
                        }
                    } while (next.queue == queue);
                    queue.lock.notifyAll();
                }
            }
        } while (list != start);
    }

    static void add(Reference<?> list) {
        synchronized (ReferenceQueue.class) {
            if (unenqueued == null) {
                unenqueued = list;
            } else {
                Reference<?> last = unenqueued;
                while (last.pendingNext != unenqueued) {
                    last = last.pendingNext;
                }
                last.pendingNext = list;
                last = list;
                while (last.pendingNext != list) {
                    last = last.pendingNext;
                }
                last.pendingNext = unenqueued;
            }
            ReferenceQueue.class.notifyAll();
        }
    }
}
