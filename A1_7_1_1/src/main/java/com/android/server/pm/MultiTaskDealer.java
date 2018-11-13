package com.android.server.pm;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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
public class MultiTaskDealer {
    private static final boolean DEBUG_TASK = false;
    public static final String PACKAGEMANAGER_SCANER = "packagescan";
    public static final String TAG = "MultiTaskDealer";
    private static HashMap<String, WeakReference<MultiTaskDealer>> map;
    private ThreadPoolExecutor mExecutor;
    private ReentrantLock mLock;
    private boolean mNeedNotifyEnd;
    private Object mObjWaitAll;
    private int mTaskCount;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.MultiTaskDealer.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.MultiTaskDealer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.MultiTaskDealer.<clinit>():void");
    }

    public static MultiTaskDealer getDealer(String name) {
        WeakReference<MultiTaskDealer> ref = (WeakReference) map.get(name);
        if (ref != null) {
            return (MultiTaskDealer) ref.get();
        }
        return null;
    }

    public static MultiTaskDealer startDealer(String name, int taskCount) {
        MultiTaskDealer dealer = getDealer(name);
        if (dealer != null) {
            return dealer;
        }
        dealer = new MultiTaskDealer(name, taskCount);
        map.put(name, new WeakReference(dealer));
        return dealer;
    }

    public void startLock() {
        this.mLock.lock();
    }

    public void endLock() {
        this.mLock.unlock();
    }

    public MultiTaskDealer(final String name, int taskCount) {
        this.mTaskCount = 0;
        this.mNeedNotifyEnd = false;
        this.mObjWaitAll = new Object();
        this.mLock = new ReentrantLock();
        String taskName = name;
        int i = taskCount;
        int i2 = taskCount;
        this.mExecutor = new ThreadPoolExecutor(i, i2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, name + "-" + this.mCount.getAndIncrement());
            }
        }) {
            protected void afterExecute(Runnable r, Throwable t) {
                if (t != null) {
                    t.printStackTrace();
                }
                MultiTaskDealer.this.TaskCompleteNotify(r);
                super.afterExecute(r, t);
            }

            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
            }
        };
    }

    public void addTask(Runnable task) {
        synchronized (this.mObjWaitAll) {
            this.mTaskCount++;
        }
        this.mExecutor.execute(task);
    }

    private void TaskCompleteNotify(Runnable task) {
        synchronized (this.mObjWaitAll) {
            this.mTaskCount--;
            if (this.mTaskCount <= 0 && this.mNeedNotifyEnd) {
                this.mObjWaitAll.notify();
            }
        }
    }

    public void waitAll() {
        synchronized (this.mObjWaitAll) {
            if (this.mTaskCount > 0) {
                this.mNeedNotifyEnd = true;
                try {
                    this.mObjWaitAll.wait();
                } catch (Exception e) {
                }
                this.mNeedNotifyEnd = false;
            }
        }
    }
}
