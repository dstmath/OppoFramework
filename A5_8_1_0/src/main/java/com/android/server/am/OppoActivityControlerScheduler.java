package com.android.server.am;

import android.app.IActivityController;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;

public class OppoActivityControlerScheduler {
    private static final int CASE_TYPE_GETPARAMETERS = 3;
    private static final int CASE_TYPE_RESUMING = 2;
    private static final int CASE_TYPE_STARTING = 1;
    private static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int MAX_TIME_TO_WAIT = 20000;
    private static final String TAG = "OppoActivityControlerScheduler";
    private static final int TIME_TO_WAIT_FOR_BROADCAST = 1000;
    private static final int TIME_TO_WAIT_FOR_BROADCAST_IN_MILLS = 1;
    private static final int TIME_TO_WAIT_PER_TIME_IN_MILLS = 5;
    private AudioManager mAudioManager = null;
    private Context mContext = null;
    private IActivityController mController = null;
    private SchedulerThread mSchedulerThread = null;

    private class SchedulerThread extends Thread {
        private TransactionData mCurTransactionData = null;
        private boolean mIsWaiting = false;
        private boolean mRefreshRes = false;
        private boolean mRunning = true;

        public void exitRunning() {
            synchronized (this) {
                this.mRunning = false;
                notify();
            }
        }

        public void notifyToSchedulerTransaction(TransactionData data) {
            if (this.mIsWaiting) {
                synchronized (this) {
                    this.mRefreshRes = false;
                    this.mCurTransactionData = data;
                    notify();
                }
            } else if (OppoActivityControlerScheduler.DEBUG) {
                Slog.d(OppoActivityControlerScheduler.TAG, "notifyToSchedulerTransaction:not in waiting status.");
            }
        }

        public boolean getRefreshRes() {
            return this.mRefreshRes;
        }

        public boolean getIsWaiting() {
            return this.mIsWaiting;
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
                if (this.mRunning) {
                    this.mIsWaiting = false;
                    if (OppoActivityControlerScheduler.DEBUG) {
                        Slog.d(OppoActivityControlerScheduler.TAG, "SchedulerThread.run:begin.");
                    }
                    if (this.mCurTransactionData != null) {
                        switch (this.mCurTransactionData.mCaseType) {
                            case 1:
                                try {
                                    if (OppoActivityControlerScheduler.this.mController != null) {
                                        this.mCurTransactionData.mProcessRes = OppoActivityControlerScheduler.this.mController.activityStarting(this.mCurTransactionData.mIntent, this.mCurTransactionData.mParam);
                                        break;
                                    }
                                } catch (RemoteException e2) {
                                    Slog.w(OppoActivityControlerScheduler.TAG, "call activityStarting failed!", e2);
                                    this.mCurTransactionData.mNoException = false;
                                    break;
                                } catch (Exception e3) {
                                    Slog.w(OppoActivityControlerScheduler.TAG, "call activityStarting failed!", e3);
                                    this.mCurTransactionData.mNoException = false;
                                    break;
                                }
                                break;
                            case 2:
                                try {
                                    if (OppoActivityControlerScheduler.this.mController != null) {
                                        this.mCurTransactionData.mProcessRes = OppoActivityControlerScheduler.this.mController.activityResuming(this.mCurTransactionData.mParam);
                                        break;
                                    }
                                } catch (RemoteException e22) {
                                    Slog.w(OppoActivityControlerScheduler.TAG, "call activityResuming failed!", e22);
                                    this.mCurTransactionData.mNoException = false;
                                    break;
                                } catch (Exception e32) {
                                    Slog.w(OppoActivityControlerScheduler.TAG, "call activityResuming failed!", e32);
                                    this.mCurTransactionData.mNoException = false;
                                    break;
                                }
                                break;
                            case 3:
                                if (OppoActivityControlerScheduler.this.mContext != null) {
                                    try {
                                        OppoActivityControlerScheduler.this.mAudioManager = (AudioManager) OppoActivityControlerScheduler.this.mContext.getSystemService("audio");
                                        this.mCurTransactionData.mResult = OppoActivityControlerScheduler.this.mAudioManager.getParameters(this.mCurTransactionData.mParam);
                                        if (OppoActivityControlerScheduler.DEBUG) {
                                            Slog.d(OppoActivityControlerScheduler.TAG, "get result pid is " + this.mCurTransactionData.mResult);
                                            break;
                                        }
                                    } catch (Exception e322) {
                                        Slog.w(OppoActivityControlerScheduler.TAG, "call getParameters failed!", e322);
                                        this.mCurTransactionData.mNoException = false;
                                        break;
                                    }
                                }
                                break;
                        }
                    }
                    if (OppoActivityControlerScheduler.DEBUG) {
                        Slog.d(OppoActivityControlerScheduler.TAG, "SchedulerThread.run:end.");
                    }
                    this.mRefreshRes = true;
                } else {
                    return;
                }
            }
        }
    }

    private class TransactionData {
        int mCaseType;
        Intent mIntent;
        boolean mNoException = true;
        String mParam;
        boolean mProcessRes = true;
        String mResult = null;

        public TransactionData(int caseType, Intent intent, String param) {
            this.mCaseType = caseType;
            this.mIntent = intent;
            this.mParam = param;
        }
    }

    public OppoActivityControlerScheduler(IActivityController controller) {
        this.mController = controller;
        this.mSchedulerThread = new SchedulerThread();
        this.mSchedulerThread.start();
    }

    public void exitRunningScheduler() {
        this.mSchedulerThread.exitRunning();
    }

    public boolean scheduleActivityStarting(Intent intent, String pkg) throws RemoteException {
        long beginTime = System.currentTimeMillis();
        if (DEBUG) {
            Slog.d(TAG, "scheduleActivityStarting begin for:" + pkg);
        }
        TransactionData data = new TransactionData(1, intent, pkg);
        this.mSchedulerThread.notifyToSchedulerTransaction(data);
        long timeCostInMills = 0;
        while (!this.mSchedulerThread.getRefreshRes() && timeCostInMills <= 20000) {
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                Slog.w(TAG, "scheduleActivityStarting sleep failed!", e);
            }
            timeCostInMills = System.currentTimeMillis() - beginTime;
        }
        boolean processRes = data != null ? data.mProcessRes : true;
        if (data != null ? data.mNoException : true) {
            long endTime = System.currentTimeMillis();
            if (DEBUG) {
                Slog.d(TAG, "scheduleActivityStarting for:" + pkg + ", cost:" + (endTime - beginTime));
            }
            return processRes;
        }
        throw new RemoteException("scheduleActivityStarting failed!");
    }

    public boolean scheduleActivityResuming(String pkg) throws RemoteException {
        long beginTime = System.currentTimeMillis();
        if (DEBUG) {
            Slog.d(TAG, "scheduleActivityResuming begin for:" + pkg);
        }
        TransactionData data = new TransactionData(2, null, pkg);
        this.mSchedulerThread.notifyToSchedulerTransaction(data);
        long timeCostInMills = 0;
        while (!this.mSchedulerThread.getRefreshRes() && timeCostInMills <= 20000) {
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                Slog.w(TAG, "scheduleActivityResuming sleep failed!", e);
            }
            timeCostInMills = System.currentTimeMillis() - beginTime;
        }
        boolean processRes = data != null ? data.mProcessRes : true;
        if (data != null ? data.mNoException : true) {
            long endTime = System.currentTimeMillis();
            if (DEBUG) {
                Slog.d(TAG, "scheduleActivityResuming for:" + pkg + ", cost:" + (endTime - beginTime));
            }
            return processRes;
        }
        throw new RemoteException("scheduleActivityResuming failed!");
    }

    public String scheduleGetParameters(Context context, String pid) {
        this.mContext = context;
        long beginTime = System.currentTimeMillis();
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.d(TAG, "scheduleGetParameters begin for: " + pid);
        }
        TransactionData data = new TransactionData(3, null, pid);
        if (this.mSchedulerThread.getIsWaiting()) {
            this.mSchedulerThread.notifyToSchedulerTransaction(data);
            long timeCostInMills = 0;
            while (!this.mSchedulerThread.getRefreshRes()) {
                if (timeCostInMills > 1000) {
                    data.mResult = "block";
                    break;
                }
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    Slog.w(TAG, "scheduleGetParameters sleep failed!", e);
                }
                timeCostInMills = System.currentTimeMillis() - beginTime;
            }
            long endTime = System.currentTimeMillis();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.d(TAG, "scheduleGetParameters for:" + pid + ", cost:" + (endTime - beginTime));
            }
            return data.mResult;
        }
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.d(TAG, "mSchedulerThread.mIsWaiting == false");
        }
        data.mResult = "block";
        return data.mResult;
    }
}
