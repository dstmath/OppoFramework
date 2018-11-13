package com.android.server.face;

import java.util.concurrent.CountDownLatch;

public class PendingResult<R> {
    private CountDownLatch mLatch = new CountDownLatch(1);
    private volatile R mResult;

    PendingResult(R defResult) {
        this.mResult = defResult;
    }

    public R await() {
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
        }
        return this.mResult;
    }

    public void setResult(R result) {
        this.mResult = result;
        this.mLatch.countDown();
    }

    public void cancel() {
        this.mLatch.countDown();
    }
}
