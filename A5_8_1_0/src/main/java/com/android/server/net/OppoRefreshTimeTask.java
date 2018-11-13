package com.android.server.net;

import android.util.Slog;
import android.util.TrustedTime;

public class OppoRefreshTimeTask {
    private static final boolean DEBUG = false;
    private static final int MAX_COUNT_TO_WAIT = 4;
    private static final long MIN_FORCE_UPDATE_TIME_INTERVAL = 12000;
    private static final String TAG = "NetworkStatsService.OppoRefreshTimeTask";
    private static final int TIME_TO_WAIT_PER_TIME_IN_MILLS = 100;
    private static OppoRefreshTimeTask mInstall = null;
    private long mLastSysCurTimeForceUpdate = 0;
    private RefreshTimeThread mRefreshTimeThread = null;
    private final TrustedTime mTime;

    private class RefreshTimeThread extends Thread {
        private boolean mIsWaiting = false;
        private boolean mRefreshRes = false;
        private boolean mRunning = true;

        public void exitRunning() {
            synchronized (this) {
                this.mRunning = false;
                notify();
            }
        }

        public void notifyToRefreshTime() {
            if (this.mIsWaiting) {
                synchronized (this) {
                    this.mRefreshRes = false;
                    notify();
                }
            }
        }

        public boolean getRefreshRes() {
            return this.mRefreshRes;
        }

        public void run() {
            while (this.mRunning) {
                synchronized (this) {
                    try {
                        this.mIsWaiting = true;
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                this.mIsWaiting = false;
                if (OppoRefreshTimeTask.this.mTime != null) {
                    OppoRefreshTimeTask.this.mTime.forceRefresh();
                }
                this.mRefreshRes = true;
            }
        }
    }

    public OppoRefreshTimeTask(TrustedTime time) {
        this.mTime = time;
        this.mRefreshTimeThread = new RefreshTimeThread();
        this.mRefreshTimeThread.start();
    }

    public static OppoRefreshTimeTask getInstall(TrustedTime time) {
        if (mInstall == null) {
            mInstall = new OppoRefreshTimeTask(time);
        }
        return mInstall;
    }

    public void triggerRefreshTime() {
        this.mRefreshTimeThread.notifyToRefreshTime();
        int queryTimeCount = 0;
        while (!this.mRefreshTimeThread.getRefreshRes() && queryTimeCount < 4) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Slog.w(TAG, "sleep failed!", e);
            }
            queryTimeCount++;
        }
    }

    public boolean forceUpdateFreqControl() {
        long curTime = System.currentTimeMillis();
        if (0 == this.mLastSysCurTimeForceUpdate) {
            this.mLastSysCurTimeForceUpdate = curTime;
            return true;
        }
        boolean allowUpdate = curTime - this.mLastSysCurTimeForceUpdate >= MIN_FORCE_UPDATE_TIME_INTERVAL;
        if (allowUpdate) {
            this.mLastSysCurTimeForceUpdate = curTime;
        }
        return allowUpdate;
    }
}
