package com.android.server.am;

import android.app.IActivityController;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.RemoteException;
import android.util.Slog;

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
public class OppoActivityControlerScheduler {
    private static final int CASE_TYPE_GETPARAMETERS = 3;
    private static final int CASE_TYPE_RESUMING = 2;
    private static final int CASE_TYPE_STARTING = 1;
    private static boolean DEBUG = false;
    private static final int MAX_TIME_TO_WAIT = 20000;
    private static final String TAG = "OppoActivityControlerScheduler";
    private static final int TIME_TO_WAIT_FOR_BROADCAST = 1000;
    private static final int TIME_TO_WAIT_FOR_BROADCAST_IN_MILLS = 1;
    private static final int TIME_TO_WAIT_PER_TIME_IN_MILLS = 5;
    private AudioManager mAudioManager;
    private Context mContext;
    private IActivityController mController;
    private SchedulerThread mSchedulerThread;

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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoActivityControlerScheduler.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoActivityControlerScheduler.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoActivityControlerScheduler.<clinit>():void");
    }

    public OppoActivityControlerScheduler(IActivityController controller) {
        this.mSchedulerThread = null;
        this.mController = null;
        this.mAudioManager = null;
        this.mContext = null;
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
