package com.oppo.enterprise.mdmcoreservice.help;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManagerHelper {
    private static volatile ThreadManagerHelper sThreadManager;
    private ExecutorService mThreadExecutor;

    public static ThreadManagerHelper getInstance() {
        ThreadManagerHelper threadManagerHelper;
        if (sThreadManager != null) {
            return sThreadManager;
        }
        synchronized (ThreadManagerHelper.class) {
            if (sThreadManager == null) {
                sThreadManager = new ThreadManagerHelper();
            }
            threadManagerHelper = sThreadManager;
        }
        return threadManagerHelper;
    }

    private ThreadManagerHelper() {
    }

    public void initThreadPool() {
        if (this.mThreadExecutor == null) {
            this.mThreadExecutor = Executors.newFixedThreadPool(3);
        }
    }

    public void postInTheadPool(Runnable runnable) {
        initThreadPool();
        this.mThreadExecutor.execute(runnable);
    }
}
