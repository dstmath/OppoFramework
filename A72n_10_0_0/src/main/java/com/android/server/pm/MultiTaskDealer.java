package com.android.server.pm;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MultiTaskDealer {
    private static final boolean DEBUG_TASK = false;
    public static final String PACKAGEMANAGER_SCANER = "packagescan";
    public static final String TAG = "MultiTaskDealer";
    private static HashMap<String, WeakReference<MultiTaskDealer>> map = new HashMap<>();
    private ThreadPoolExecutor mExecutor;
    private ReentrantLock mLock = new ReentrantLock();
    private boolean mNeedNotifyEnd = false;
    private Object mObjWaitAll = new Object();
    private int mTaskCount = 0;

    public static MultiTaskDealer getDealer(String name) {
        WeakReference<MultiTaskDealer> ref = map.get(name);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    public static MultiTaskDealer startDealer(String name, int taskCount) {
        MultiTaskDealer dealer = getDealer(name);
        if (dealer != null) {
            return dealer;
        }
        MultiTaskDealer dealer2 = new MultiTaskDealer(name, taskCount);
        map.put(name, new WeakReference<>(dealer2));
        return dealer2;
    }

    public void startLock() {
        this.mLock.lock();
    }

    public void endLock() {
        this.mLock.unlock();
    }

    public MultiTaskDealer(final String name, int taskCount) {
        this.mExecutor = new ThreadPoolExecutor(taskCount, taskCount, 5, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory() {
            /* class com.android.server.pm.MultiTaskDealer.AnonymousClass1 */
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, name + "-" + this.mCount.getAndIncrement());
                thread.setPriority(10);
                return thread;
            }
        }) {
            /* class com.android.server.pm.MultiTaskDealer.AnonymousClass2 */

            /* access modifiers changed from: protected */
            public void afterExecute(Runnable r, Throwable t) {
                if (t != null) {
                    t.printStackTrace();
                }
                MultiTaskDealer.this.TaskCompleteNotify(r);
                super.afterExecute(r, t);
            }

            /* access modifiers changed from: protected */
            public void beforeExecute(Thread t, Runnable r) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    public void shutdownNow() {
        this.mExecutor.shutdownNow();
    }
}
