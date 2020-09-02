package com.android.server.am;

import android.app.IActivityController;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;

public class OppoActivityControlerScheduler {
    private static final int CASE_TYPE_CRASH = 4;
    private static final int CASE_TYPE_GETPARAMETERS = 3;
    private static final int CASE_TYPE_RESUMING = 2;
    private static final int CASE_TYPE_STARTING = 1;
    /* access modifiers changed from: private */
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int MAX_TIME_TO_WAIT = 20000;
    private static final String TAG = "OppoActivityControlerScheduler";
    private static final int TIME_TO_WAIT_FOR_BROADCAST = 10;
    private static final int TIME_TO_WAIT_FOR_BROADCAST_IN_MILLS = 1;
    private static final int TIME_TO_WAIT_PER_TIME_IN_MILLS = 5;
    /* access modifiers changed from: private */
    public AudioManager mAudioManager = null;
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public IActivityController mController = null;
    private SchedulerThread mSchedulerThread = null;

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
        boolean processRes = data.mProcessRes;
        if (data.mNoException) {
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
        boolean processRes = data.mProcessRes;
        if (data.mNoException) {
            long endTime = System.currentTimeMillis();
            if (DEBUG) {
                Slog.d(TAG, "scheduleActivityResuming for:" + pkg + ", cost:" + (endTime - beginTime));
            }
            return processRes;
        }
        throw new RemoteException("scheduleActivityResuming failed!");
    }

    public boolean scheduleAppCrash(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
        long beginTime = System.currentTimeMillis();
        TransactionData data = new TransactionData(4, null, null, new AppCrashData(processName, pid, shortMsg, longMsg, timeMillis, stackTrace));
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
        boolean processRes = data.mProcessRes;
        if (data.mNoException) {
            long endTime = System.currentTimeMillis();
            if (DEBUG) {
                Slog.d(TAG, "scheduleAppCrash for:" + processName + ", cost:" + (endTime - beginTime));
            }
            return processRes;
        }
        throw new RemoteException("scheduleAppCrash failed!");
    }

    public String scheduleGetParameters(Context context, String pid) {
        this.mContext = context;
        long beginTime = System.currentTimeMillis();
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.d(TAG, "scheduleGetParameters begin for: " + pid);
        }
        TransactionData data = new TransactionData(3, null, pid);
        if (!this.mSchedulerThread.getIsWaiting()) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.d(TAG, "mSchedulerThread.mIsWaiting == false");
            }
            data.mResult = "block";
            return data.mResult;
        }
        this.mSchedulerThread.notifyToSchedulerTransaction(data);
        long timeCostInMills = 0;
        while (true) {
            if (!this.mSchedulerThread.getRefreshRes()) {
                if (timeCostInMills > 10) {
                    data.mResult = "block";
                    break;
                }
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    Slog.w(TAG, "scheduleGetParameters sleep failed!", e);
                }
                timeCostInMills = System.currentTimeMillis() - beginTime;
            } else {
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.d(TAG, "scheduleGetParameters for:" + pid + ", cost:" + (endTime - beginTime));
        }
        return data.mResult;
    }

    private class TransactionData {
        int mCaseType;
        Object mExtraData;
        Intent mIntent;
        boolean mNoException;
        String mParam;
        boolean mProcessRes;
        String mResult;

        public TransactionData(int caseType, Intent intent, String param) {
            this.mCaseType = caseType;
            this.mIntent = intent;
            this.mParam = param;
            this.mProcessRes = true;
            this.mNoException = true;
            this.mResult = null;
            this.mExtraData = null;
        }

        public TransactionData(int caseType, Intent intent, String param, Object extraData) {
            this.mCaseType = caseType;
            this.mIntent = intent;
            this.mParam = param;
            this.mProcessRes = true;
            this.mNoException = true;
            this.mResult = null;
            this.mExtraData = extraData;
        }
    }

    private class AppCrashData {
        String mLongMsg;
        int mPid;
        String mProcessName;
        String mShortMsg;
        String mStackTrace;
        long mTimeMillis;

        public AppCrashData(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) {
            this.mProcessName = processName;
            this.mPid = pid;
            this.mShortMsg = shortMsg;
            this.mLongMsg = longMsg;
            this.mTimeMillis = timeMillis;
            this.mStackTrace = stackTrace;
        }
    }

    private class SchedulerThread extends Thread {
        private TransactionData mCurTransactionData = null;
        private boolean mIsWaiting = false;
        private boolean mRefreshRes = false;
        private boolean mRunning = true;

        public SchedulerThread() {
        }

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
                    TransactionData transactionData = this.mCurTransactionData;
                    if (transactionData != null) {
                        int i = transactionData.mCaseType;
                        if (i == 1) {
                            try {
                                if (OppoActivityControlerScheduler.this.mController != null) {
                                    this.mCurTransactionData.mProcessRes = OppoActivityControlerScheduler.this.mController.activityStarting(this.mCurTransactionData.mIntent, this.mCurTransactionData.mParam);
                                }
                            } catch (RemoteException e2) {
                                Slog.w(OppoActivityControlerScheduler.TAG, "call activityStarting failed!", e2);
                                this.mCurTransactionData.mNoException = false;
                            } catch (Exception e3) {
                                Slog.w(OppoActivityControlerScheduler.TAG, "call activityStarting failed!", e3);
                                this.mCurTransactionData.mNoException = false;
                            }
                        } else if (i == 2) {
                            try {
                                if (OppoActivityControlerScheduler.this.mController != null) {
                                    this.mCurTransactionData.mProcessRes = OppoActivityControlerScheduler.this.mController.activityResuming(this.mCurTransactionData.mParam);
                                }
                            } catch (RemoteException e4) {
                                Slog.w(OppoActivityControlerScheduler.TAG, "call activityResuming failed!", e4);
                                this.mCurTransactionData.mNoException = false;
                            } catch (Exception e5) {
                                Slog.w(OppoActivityControlerScheduler.TAG, "call activityResuming failed!", e5);
                                this.mCurTransactionData.mNoException = false;
                            }
                        } else if (i != 3) {
                            if (i == 4) {
                                try {
                                    if (OppoActivityControlerScheduler.this.mController != null) {
                                        if (this.mCurTransactionData.mExtraData != null) {
                                            AppCrashData crashData = (AppCrashData) this.mCurTransactionData.mExtraData;
                                            if (crashData != null) {
                                                this.mCurTransactionData.mProcessRes = OppoActivityControlerScheduler.this.mController.appCrashed(crashData.mProcessName, crashData.mPid, crashData.mShortMsg, crashData.mLongMsg, crashData.mTimeMillis, crashData.mStackTrace);
                                            }
                                        } else {
                                            this.mCurTransactionData.mProcessRes = true;
                                        }
                                    }
                                } catch (RemoteException e6) {
                                    Slog.w(OppoActivityControlerScheduler.TAG, "call activityResuming failed!", e6);
                                    this.mCurTransactionData.mNoException = false;
                                } catch (Exception e7) {
                                    Slog.w(OppoActivityControlerScheduler.TAG, "call activityResuming failed!", e7);
                                    this.mCurTransactionData.mNoException = false;
                                }
                            }
                        } else if (OppoActivityControlerScheduler.this.mContext != null) {
                            try {
                                AudioManager unused = OppoActivityControlerScheduler.this.mAudioManager = (AudioManager) OppoActivityControlerScheduler.this.mContext.getSystemService("audio");
                                this.mCurTransactionData.mResult = OppoActivityControlerScheduler.this.mAudioManager.getParameters(this.mCurTransactionData.mParam);
                                if (OppoActivityControlerScheduler.DEBUG) {
                                    Slog.d(OppoActivityControlerScheduler.TAG, "get result pid is " + this.mCurTransactionData.mResult);
                                }
                            } catch (Exception e8) {
                                Slog.w(OppoActivityControlerScheduler.TAG, "call getParameters failed!", e8);
                                this.mCurTransactionData.mNoException = false;
                            }
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
}
