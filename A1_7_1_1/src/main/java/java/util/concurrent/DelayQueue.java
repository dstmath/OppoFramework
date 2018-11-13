package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DelayQueue<E extends Delayed> extends AbstractQueue<E> implements BlockingQueue<E> {
    private final Condition available;
    private Thread leader;
    private final transient ReentrantLock lock;
    private final PriorityQueue<E> q;

    private class Itr implements Iterator<E> {
        final Object[] array;
        int cursor;
        int lastRet;
        final /* synthetic */ DelayQueue this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.DelayQueue.Itr.<init>(java.util.concurrent.DelayQueue, java.lang.Object[]):void, dex: 
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
        Itr(java.util.concurrent.DelayQueue r1, java.lang.Object[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.DelayQueue.Itr.<init>(java.util.concurrent.DelayQueue, java.lang.Object[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.DelayQueue.Itr.<init>(java.util.concurrent.DelayQueue, java.lang.Object[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.DelayQueue.Itr.hasNext():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public boolean hasNext() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.DelayQueue.Itr.hasNext():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.DelayQueue.Itr.hasNext():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.DelayQueue.Itr.next():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object next() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.DelayQueue.Itr.next():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.DelayQueue.Itr.next():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.DelayQueue.Itr.next():E, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public E next() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.DelayQueue.Itr.next():E, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.DelayQueue.Itr.next():E");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.concurrent.DelayQueue.Itr.remove():void, dex:  in method: java.util.concurrent.DelayQueue.Itr.remove():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.concurrent.DelayQueue.Itr.remove():void, dex: 
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
        public void remove() {
            /*
            // Can't load method instructions: Load method exception: null in method: java.util.concurrent.DelayQueue.Itr.remove():void, dex:  in method: java.util.concurrent.DelayQueue.Itr.remove():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.DelayQueue.Itr.remove():void");
        }
    }

    public DelayQueue() {
        this.lock = new ReentrantLock();
        this.q = new PriorityQueue();
        this.available = this.lock.newCondition();
    }

    public DelayQueue(Collection<? extends E> c) {
        this.lock = new ReentrantLock();
        this.q = new PriorityQueue();
        this.available = this.lock.newCondition();
        addAll(c);
    }

    public /* bridge */ /* synthetic */ boolean add(Object e) {
        return add((Delayed) e);
    }

    public boolean add(E e) {
        return offer((Delayed) e);
    }

    public /* bridge */ /* synthetic */ boolean offer(Object e) {
        return offer((Delayed) e);
    }

    public boolean offer(E e) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.q.offer(e);
            if (this.q.peek() == e) {
                this.leader = null;
                this.available.signal();
            }
            lock.unlock();
            return true;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public /* bridge */ /* synthetic */ void put(Object e) throws InterruptedException {
        put((Delayed) e);
    }

    public void put(E e) {
        offer((Delayed) e);
    }

    public /* bridge */ /* synthetic */ boolean offer(Object e, long timeout, TimeUnit unit) throws InterruptedException {
        return offer((Delayed) e, timeout, unit);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer((Delayed) e);
    }

    public /* bridge */ /* synthetic */ Object poll() {
        return poll();
    }

    public E poll() {
        E e = null;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Delayed first = (Delayed) this.q.peek();
            if (first != null && first.getDelay(TimeUnit.NANOSECONDS) <= 0) {
                Delayed e2 = (Delayed) this.q.poll();
            }
            lock.unlock();
            return e2;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public /* bridge */ /* synthetic */ Object take() throws InterruptedException {
        return take();
    }

    public E take() throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            Thread thisThread;
            try {
                Delayed first = (Delayed) this.q.peek();
                if (first == null) {
                    this.available.await();
                } else {
                    long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay <= 0) {
                        Delayed delayed = (Delayed) this.q.poll();
                        if (this.leader == null && this.q.peek() != null) {
                            this.available.signal();
                        }
                        lock.unlock();
                        return delayed;
                    } else if (this.leader != null) {
                        this.available.await();
                    } else {
                        thisThread = Thread.currentThread();
                        this.leader = thisThread;
                        this.available.awaitNanos(delay);
                        if (this.leader == thisThread) {
                            this.leader = null;
                        }
                    }
                }
            } catch (Throwable th) {
                if (this.leader == null && this.q.peek() != null) {
                    this.available.signal();
                }
                lock.unlock();
            }
        }
    }

    public /* bridge */ /* synthetic */ Object poll(long timeout, TimeUnit unit) throws InterruptedException {
        return poll(timeout, unit);
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            Thread thisThread;
            try {
                Delayed first = (Delayed) this.q.peek();
                if (first != null) {
                    long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay <= 0) {
                        Delayed delayed = (Delayed) this.q.poll();
                        if (this.leader == null && this.q.peek() != null) {
                            this.available.signal();
                        }
                        lock.unlock();
                        return delayed;
                    } else if (nanos <= 0) {
                        if (this.leader == null && this.q.peek() != null) {
                            this.available.signal();
                        }
                        lock.unlock();
                        return null;
                    } else {
                        if (nanos >= delay) {
                            if (this.leader == null) {
                                thisThread = Thread.currentThread();
                                this.leader = thisThread;
                                nanos -= delay - this.available.awaitNanos(delay);
                                if (this.leader == thisThread) {
                                    this.leader = null;
                                }
                            }
                        }
                        nanos = this.available.awaitNanos(nanos);
                    }
                } else if (nanos <= 0) {
                    if (this.leader == null && this.q.peek() != null) {
                        this.available.signal();
                    }
                    lock.unlock();
                    return null;
                } else {
                    nanos = this.available.awaitNanos(nanos);
                }
            } catch (Throwable th) {
                if (this.leader == null && this.q.peek() != null) {
                    this.available.signal();
                }
                lock.unlock();
            }
        }
    }

    public /* bridge */ /* synthetic */ Object peek() {
        return peek();
    }

    public E peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Delayed delayed = (Delayed) this.q.peek();
            return delayed;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int size = this.q.size();
            return size;
        } finally {
            lock.unlock();
        }
    }

    private E peekExpired() {
        Delayed first = (Delayed) this.q.peek();
        return (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) ? null : first;
    }

    public int drainTo(Collection<? super E> c) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else {
            ReentrantLock lock = this.lock;
            lock.lock();
            int n = 0;
            while (true) {
                try {
                    E e = peekExpired();
                    if (e == null) {
                        break;
                    }
                    c.add(e);
                    this.q.poll();
                    n++;
                } finally {
                    lock.unlock();
                }
            }
            return n;
        }
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else if (maxElements <= 0) {
            return 0;
        } else {
            ReentrantLock lock = this.lock;
            lock.lock();
            int n = 0;
            while (n < maxElements) {
                try {
                    E e = peekExpired();
                    if (e == null) {
                        break;
                    }
                    c.add(e);
                    this.q.poll();
                    n++;
                } catch (Throwable th) {
                    lock.unlock();
                }
            }
            lock.unlock();
            return n;
        }
    }

    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.q.clear();
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] toArray = this.q.toArray();
            return toArray;
        } finally {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            T[] toArray = this.q.toArray(a);
            return toArray;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean remove = this.q.remove(o);
            return remove;
        } finally {
            lock.unlock();
        }
    }

    void removeEQ(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Iterator<E> it = this.q.iterator();
            while (it.hasNext()) {
                if (o == it.next()) {
                    it.remove();
                    break;
                }
            }
            lock.unlock();
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr(this, toArray());
    }
}
