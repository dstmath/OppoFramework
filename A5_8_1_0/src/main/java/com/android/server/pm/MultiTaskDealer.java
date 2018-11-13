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
    private static HashMap<String, WeakReference<MultiTaskDealer>> map = new HashMap();
    private ThreadPoolExecutor mExecutor;
    private ReentrantLock mLock = new ReentrantLock();
    private boolean mNeedNotifyEnd = false;
    private Object mObjWaitAll = new Object();
    private int mTaskCount = 0;

    public static MultiTaskDealer getDealer(String name) {
        WeakReference<MultiTaskDealer> ref = (WeakReference) map.get(name);
        return ref != null ? (MultiTaskDealer) ref.get() : null;
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
