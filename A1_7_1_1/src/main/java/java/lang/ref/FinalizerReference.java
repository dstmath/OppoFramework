package java.lang.ref;

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
public final class FinalizerReference<T> extends Reference<T> {
    private static final Object LIST_LOCK = null;
    private static FinalizerReference<?> head;
    public static final ReferenceQueue<Object> queue = null;
    private FinalizerReference<?> next;
    private FinalizerReference<?> prev;
    private T zombie;

    private static class Sentinel {
        boolean finalized;

        /* synthetic */ Sentinel(Sentinel sentinel) {
            this();
        }

        private Sentinel() {
            this.finalized = false;
        }

        protected synchronized void finalize() throws Throwable {
            if (this.finalized) {
                throw new AssertionError();
            }
            this.finalized = true;
            notifyAll();
        }

        synchronized void awaitFinalization(long timeout) throws InterruptedException {
            long endTime = System.nanoTime() + timeout;
            while (!this.finalized) {
                if (timeout != 0) {
                    long currentTime = System.nanoTime();
                    if (currentTime >= endTime) {
                        break;
                    }
                    long deltaTime = endTime - currentTime;
                    wait(deltaTime / 1000000, (int) (deltaTime % 1000000));
                } else {
                    wait();
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.ref.FinalizerReference.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.ref.FinalizerReference.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ref.FinalizerReference.<clinit>():void");
    }

    private native boolean makeCircularListIfUnenqueued();

    public FinalizerReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }

    public T get() {
        return this.zombie;
    }

    public void clear() {
        this.zombie = null;
    }

    public static void add(Object referent) {
        FinalizerReference<?> reference = new FinalizerReference(referent, queue);
        synchronized (LIST_LOCK) {
            reference.prev = null;
            reference.next = head;
            if (head != null) {
                head.prev = reference;
            }
            head = reference;
        }
    }

    public static void remove(FinalizerReference<?> reference) {
        synchronized (LIST_LOCK) {
            FinalizerReference<?> next = reference.next;
            FinalizerReference<?> prev = reference.prev;
            reference.next = null;
            reference.prev = null;
            if (prev != null) {
                prev.next = next;
            } else {
                head = next;
            }
            if (next != null) {
                next.prev = prev;
            }
        }
    }

    public static void finalizeAllEnqueued(long timeout) throws InterruptedException {
        Sentinel sentinel;
        do {
            sentinel = new Sentinel();
        } while (!enqueueSentinelReference(sentinel));
        sentinel.awaitFinalization(timeout);
    }

    private static boolean enqueueSentinelReference(Sentinel sentinel) {
        synchronized (LIST_LOCK) {
            for (FinalizerReference<?> r = head; r != null; r = r.next) {
                if (r.referent == sentinel) {
                    FinalizerReference<Sentinel> sentinelReference = r;
                    sentinelReference.referent = null;
                    sentinelReference.zombie = sentinel;
                    if (sentinelReference.makeCircularListIfUnenqueued()) {
                        ReferenceQueue.add(sentinelReference);
                        return true;
                    }
                    return false;
                }
            }
            throw new AssertionError("newly-created live Sentinel not on list!");
        }
    }
}
