package com.android.server.am;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.EventLog;
import android.util.LogPrinter;
import android.util.Printer;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.LocationManagerService;
import com.android.server.ServiceThread;
import com.android.server.coloros.OppoListManager;
import com.android.server.job.controllers.JobStatus;
import com.mediatek.am.AMEventHookData.PackageStoppedStatusChanged;
import com.mediatek.am.AMEventHookData.ReadyToStartDynamicReceiver;
import com.mediatek.am.AMEventHookData.ReadyToStartStaticReceiver;
import com.mediatek.anrmanager.ANRManager;
import com.mediatek.anrmanager.ANRManager.IAnrBroadcastQueue;
import com.mediatek.server.am.AMEventHook.Event;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
public final class BroadcastQueue {
    static final int BROADCAST_INTENT_MSG = 200;
    static final int BROADCAST_NEXT_MSG = 203;
    static final int BROADCAST_TIMEOUT_MSG = 201;
    static final int MAX_BROADCAST_HISTORY = 0;
    static final int MAX_BROADCAST_SUMMARY_HISTORY = 0;
    static final int SCHEDULE_TEMP_WHITELIST_MSG = 202;
    private static final String TAG = "BroadcastQueue";
    private static final String TAG_BROADCAST = null;
    private static final String TAG_MU = "BroadcastQueue_MU";
    private int mAllowDebugTime;
    final AnrBroadcastQueue mAnrBroadcastQueue;
    final BroadcastRecord[] mBroadcastHistory;
    final Intent[] mBroadcastSummaryHistory;
    boolean mBroadcastsScheduled;
    final boolean mDelayBehindServices;
    final BroadcastHandler mHandler;
    private final ServiceThread mHandlerThread;
    int mHistoryNext;
    private long mLastTimeForDispatchMsg;
    private Printer mLogPrinterForMsgDump;
    final ArrayList<BroadcastRecord> mOrderedBroadcasts;
    final ArrayList<BroadcastRecord> mParallelBroadcasts;
    BroadcastRecord mPendingBroadcast;
    int mPendingBroadcastRecvIndex;
    boolean mPendingBroadcastTimeoutMessage;
    final String mQueueName;
    final SparseArray<ReceiverRecord> mReceiverRecords;
    final ActivityManagerService mService;
    final long[] mSummaryHistoryDispatchTime;
    final long[] mSummaryHistoryEnqueueTime;
    final long[] mSummaryHistoryFinishTime;
    int mSummaryHistoryNext;
    final long mTimeoutPeriod;
    final long mTimeoutPeriodForApp;

    class AnrBroadcastQueue implements IAnrBroadcastQueue {
        AnrBroadcastQueue() {
        }

        public int getOrderedBroadcastsPid() {
            int pid = -1;
            synchronized (BroadcastQueue.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (BroadcastQueue.this.mOrderedBroadcasts.size() > 0) {
                        BroadcastRecord br = (BroadcastRecord) BroadcastQueue.this.mOrderedBroadcasts.get(0);
                        if (!(br == null || br.curApp == null)) {
                            pid = br.curApp.pid;
                        }
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return pid;
        }
    }

    private final class AppNotResponding implements Runnable {
        private final String mAnnotation;
        private final ProcessRecord mApp;

        public AppNotResponding(ProcessRecord app, String annotation) {
            this.mApp = app;
            this.mAnnotation = annotation;
        }

        public void run() {
            BroadcastQueue.this.mService.mAppErrors.appNotResponding(this.mApp, null, null, false, this.mAnnotation);
        }
    }

    private final class BroadcastHandler extends Handler {
        public BroadcastHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(BroadcastQueue.TAG_BROADCAST, "Received BROADCAST_INTENT_MSG " + BroadcastQueue.this.mQueueName);
                    }
                    BroadcastQueue.this.processNextBroadcast(true);
                    return;
                case BroadcastQueue.BROADCAST_TIMEOUT_MSG /*201*/:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastQueue.this.broadcastTimeoutLocked(true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case BroadcastQueue.SCHEDULE_TEMP_WHITELIST_MSG /*202*/:
                    LocalService dic = BroadcastQueue.this.mService.mLocalDeviceIdleController;
                    if (dic != null) {
                        dic.addPowerSaveTempWhitelistAppDirect(UserHandle.getAppId(msg.arg1), (long) msg.arg2, true, (String) msg.obj);
                        return;
                    }
                    return;
                case BroadcastQueue.BROADCAST_NEXT_MSG /*203*/:
                    boolean doNext = false;
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastRecord r = msg.obj;
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v(BroadcastQueue.TAG, "Received BROADCAST_NEXT_MSG ,finishReceiver , broadcastRecord = " + r);
                            }
                            doNext = BroadcastQueue.this.finishReceiverLocked(r, 0, null, null, false, true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (doNext) {
                        BroadcastQueue.this.processNextBroadcast(false);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.BroadcastQueue.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.BroadcastQueue.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.BroadcastQueue.<clinit>():void");
    }

    BroadcastQueue(ActivityManagerService service, ServiceThread thread, String name, long timeoutPeriod, boolean allowDelayBehindServices) {
        this.mParallelBroadcasts = new ArrayList();
        this.mOrderedBroadcasts = new ArrayList();
        this.mBroadcastHistory = new BroadcastRecord[MAX_BROADCAST_HISTORY];
        this.mHistoryNext = 0;
        this.mBroadcastSummaryHistory = new Intent[MAX_BROADCAST_SUMMARY_HISTORY];
        this.mSummaryHistoryNext = 0;
        this.mSummaryHistoryEnqueueTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
        this.mSummaryHistoryDispatchTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
        this.mSummaryHistoryFinishTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
        this.mBroadcastsScheduled = false;
        this.mPendingBroadcast = null;
        this.mTimeoutPeriodForApp = 300000;
        this.mReceiverRecords = new SparseArray();
        this.mLastTimeForDispatchMsg = 0;
        this.mLogPrinterForMsgDump = new LogPrinter(3, TAG);
        this.mAllowDebugTime = 1000;
        this.mService = service;
        this.mHandler = new BroadcastHandler(thread.getLooper());
        this.mQueueName = name;
        this.mTimeoutPeriod = timeoutPeriod;
        this.mDelayBehindServices = allowDelayBehindServices;
        this.mHandlerThread = new ServiceThread(name, -2, false);
        this.mHandlerThread.start();
        this.mAnrBroadcastQueue = new AnrBroadcastQueue();
    }

    public boolean isPendingBroadcastProcessLocked(int pid) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, " pid = " + pid + " mPendingBroadcast = " + this.mPendingBroadcast + (this.mPendingBroadcast != null ? " curApp.pid = " + this.mPendingBroadcast.curApp.pid : " null"));
        }
        if (this.mPendingBroadcast == null || this.mPendingBroadcast.curApp.pid != pid) {
            return false;
        }
        return true;
    }

    public void enqueueParallelBroadcastLocked(BroadcastRecord r) {
        this.mParallelBroadcasts.add(r);
        r.enqueueClockTime = System.currentTimeMillis();
        OppoBroadcastManager.getInstance(this.mService).adjustQueueIfNecessary(this.mParallelBroadcasts, r);
    }

    public void enqueueOrderedBroadcastLocked(BroadcastRecord r) {
        this.mOrderedBroadcasts.add(r);
        r.enqueueClockTime = System.currentTimeMillis();
        OppoBroadcastManager.getInstance(this.mService).adjustQueueIfNecessary(this.mOrderedBroadcasts, r);
    }

    public final boolean replaceParallelBroadcastLocked(BroadcastRecord r) {
        int i = this.mParallelBroadcasts.size() - 1;
        while (i >= 0) {
            Intent curIntent = ((BroadcastRecord) this.mParallelBroadcasts.get(i)).intent;
            if (r == null || !r.intent.filterEquals(curIntent)) {
                i--;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG_BROADCAST, "***** DROPPING PARALLEL [" + this.mQueueName + "]: " + r.intent);
                }
                this.mParallelBroadcasts.set(i, r);
                return true;
            }
        }
        return false;
    }

    public final boolean replaceOrderedBroadcastLocked(BroadcastRecord r) {
        for (int i = this.mOrderedBroadcasts.size() - 1; i > 0; i--) {
            if (r.intent.filterEquals(((BroadcastRecord) this.mOrderedBroadcasts.get(i)).intent)) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG_BROADCAST, "***** DROPPING ORDERED [" + this.mQueueName + "]: " + r.intent);
                }
                this.mOrderedBroadcasts.set(i, r);
                return true;
            }
        }
        return false;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "ZhiYong.Lin@Plf.Framework, modify for BPM", property = OppoRomType.ROM)
    private final void processCurBroadcastLocked(BroadcastRecord r, ProcessRecord app) throws RemoteException {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + " for app " + app);
        }
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.i(TAG, "processCurBroadcastLocked app.thread " + app.thread + Debug.getCallers(4));
            Slog.v(TAG, "processCurBroadcastLocked thread.getName() " + Thread.currentThread().getName());
        }
        if (app.thread == null) {
            throw new RemoteException();
        } else if (app.inFullBackup) {
            skipReceiverLocked(r);
        } else {
            r.receiver = app.thread.asBinder();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "processCurBroadcastLocked r: " + r + " r.receiver = " + r.receiver);
            }
            r.curApp = app;
            app.curReceiver = r;
            oppoSetProcReceive(true, r, app);
            app.forceProcessStateUpTo(11);
            this.mService.updateLruProcessLocked(app, false, null);
            this.mService.updateOomAdjLocked();
            r.intent.setComponent(r.curComponent);
            boolean started = false;
            try {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v(TAG_BROADCAST, "Delivering to component " + r.curComponent + ": " + r);
                }
                if (OppoProcessManagerHelper.checkBroadcast(this, app, r)) {
                    this.mService.notifyPackageUse(r.intent.getComponent().getPackageName(), 3);
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.d(TAG_BROADCAST, "BDC-Delivering broadcast: " + r.intent + ", queue=" + this.mQueueName + ", ordered=" + r.ordered + ", app=" + app + ", receiver=" + r.receiver);
                    }
                    Intent intent = new Intent(r.intent);
                    ReceiverRecord receiverRecord = new ReceiverRecord(this.mService, this, r, r.curApp, app.thread.asBinder(), intent, this.mHandlerThread.getLooper());
                    app.thread.scheduleReceiver(intent, r.curReceiver, this.mService.compatibilityInfoForPackageLocked(r.curReceiver.applicationInfo), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.userId, app.repProcState, receiverRecord.hashCode());
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + " DELIVERED for app " + app + " r.ordered " + r.ordered);
                    }
                    started = true;
                    if (!r.ordered) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG, "receiverRecord.hashCode() = " + receiverRecord.hashCode() + " receiverRecord = " + receiverRecord);
                        }
                        this.mReceiverRecords.put(receiverRecord.hashCode(), receiverRecord);
                        synchronized (app.receiverRecords) {
                            app.receiverRecords.add(receiverRecord);
                        }
                        receiverRecord.setBroadcastTimeoutLocked(300000);
                        Message msg = Message.obtain();
                        msg.what = BROADCAST_NEXT_MSG;
                        msg.obj = r;
                        this.mHandler.sendMessage(msg);
                    }
                }
                if (!started) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + ": NOT STARTED!");
                    }
                    r.receiver = null;
                    r.curApp = null;
                    oppoSetProcReceive(false, r, app);
                    if (app.foreCurReceiver != null) {
                        app.curReceiver = app.foreCurReceiver;
                    } else if (app.backCurReceiver != null) {
                        app.curReceiver = app.backCurReceiver;
                    } else if (app.oppoforeCurReceiver != null) {
                        app.curReceiver = app.oppoforeCurReceiver;
                    } else if (app.oppobackCurReceiver != null) {
                        app.curReceiver = app.oppobackCurReceiver;
                    } else {
                        app.curReceiver = null;
                    }
                }
            } catch (Throwable th) {
                if (!started) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + ": NOT STARTED!");
                    }
                    r.receiver = null;
                    r.curApp = null;
                    oppoSetProcReceive(false, r, app);
                    if (app.foreCurReceiver != null) {
                        app.curReceiver = app.foreCurReceiver;
                    } else if (app.backCurReceiver != null) {
                        app.curReceiver = app.backCurReceiver;
                    } else if (app.oppoforeCurReceiver != null) {
                        app.curReceiver = app.oppoforeCurReceiver;
                    } else if (app.oppobackCurReceiver != null) {
                        app.curReceiver = app.oppobackCurReceiver;
                    } else {
                        app.curReceiver = null;
                    }
                }
            }
        }
    }

    public boolean sendPendingBroadcastsLocked(ProcessRecord app) {
        boolean didSomething = false;
        BroadcastRecord br = this.mPendingBroadcast;
        if (br != null && br.curApp.pid == app.pid) {
            if (br.curApp != app) {
                Slog.e(TAG, "App mismatch when sending pending broadcast to " + app.processName + ", intended target is " + br.curApp.processName);
                return false;
            }
            try {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG, "mQueueName " + this.mQueueName + " sendPendingBroadcastsLocked mPendingBroadcast = " + this.mPendingBroadcast + " app = " + app);
                }
                this.mPendingBroadcast = null;
                if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                    ReadyToStartStaticReceiver eventData = ReadyToStartStaticReceiver.createInstance();
                    Object[] objArr = new Object[3];
                    objArr[0] = app.info.packageName;
                    objArr[1] = br.callerPackage;
                    objArr[2] = Integer.valueOf(br.callingUid);
                    eventData.set(objArr);
                    this.mService.getAMEventHook().hook(Event.AM_ReadyToStartStaticReceiver, eventData);
                }
                processCurBroadcastLocked(br, app);
                didSomething = true;
            } catch (Exception e) {
                Slog.w(TAG, "Exception in new application when starting receiver " + (br.curComponent != null ? br.curComponent.flattenToShortString() : "(null)"), e);
                logBroadcastReceiverDiscardLocked(br);
                finishReceiverLocked(br, br.resultCode, br.resultData, br.resultExtras, br.resultAbort, false);
                scheduleBroadcastsLocked();
                br.state = 0;
                throw new RuntimeException(e.getMessage());
            }
        }
        return didSomething;
    }

    public void skipPendingBroadcastLocked(int pid) {
        BroadcastRecord br = this.mPendingBroadcast;
        if (br != null && br.curApp.pid == pid) {
            br.state = 0;
            br.nextReceiver = this.mPendingBroadcastRecvIndex;
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "mQueueName " + this.mQueueName + " skipPendingBroadcastLocked mPendingBroadcast = " + this.mPendingBroadcast + " pid = " + pid);
            }
            this.mPendingBroadcast = null;
            scheduleBroadcastsLocked();
        }
    }

    public void skipCurrentReceiverLocked(ProcessRecord app) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST && app != null) {
            Slog.i(TAG, "skipCurrentReceiverLocked : mQueueName = " + this.mQueueName + ",app = " + app.toShortString() + " " + Debug.getCallers(8));
        }
        boolean reschedule = false;
        BroadcastRecord r = null;
        if (app != null && app.foreCurReceiver != null && app.foreCurReceiver.queue == this) {
            r = app.foreCurReceiver;
        } else if (app != null && app.backCurReceiver != null && app.backCurReceiver.queue == this) {
            r = app.backCurReceiver;
        } else if (app != null && app.oppoforeCurReceiver != null && app.oppoforeCurReceiver.queue == this) {
            r = app.oppoforeCurReceiver;
        } else if (!(app == null || app.oppobackCurReceiver == null || app.oppobackCurReceiver.queue != this)) {
            r = app.oppobackCurReceiver;
        }
        if (r == null) {
            synchronized (app.receiverRecords) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG, app + "app.receiverRecords = " + app.receiverRecords.size());
                }
                for (int i = app.receiverRecords.size() - 1; i >= 0; i--) {
                    ReceiverRecord receiverRecord = (ReceiverRecord) app.receiverRecords.get(i);
                    if (receiverRecord != null && receiverRecord.mQueue == this) {
                        receiverRecord.cancelBroadcastTimeoutLocked();
                        this.mReceiverRecords.remove(receiverRecord.hashCode());
                        app.receiverRecords.remove(receiverRecord);
                    }
                }
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG, " app.receiverRecords after remove = " + app.receiverRecords.size());
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "skipCurrentReceiverLocked r = " + r);
        }
        if (r != null && oppoProcessBroadcastFinish(app, r)) {
            reschedule = true;
            r = null;
        }
        if (r != null && r.queue == this) {
            logBroadcastReceiverDiscardLocked(r);
            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            reschedule = true;
        }
        r = this.mPendingBroadcast;
        if (r != null && r.curApp == app) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "[" + this.mQueueName + "] skip & discard pending app " + r);
            }
            logBroadcastReceiverDiscardLocked(r);
            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            reschedule = true;
        }
        if (reschedule) {
            scheduleBroadcastsLocked();
        }
    }

    private void skipReceiverLocked(BroadcastRecord r) {
        logBroadcastReceiverDiscardLocked(r);
        finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
        scheduleBroadcastsLocked();
    }

    public void scheduleBroadcastsLocked() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG_BROADCAST, "Schedule broadcasts [" + this.mQueueName + "]: current=" + this.mBroadcastsScheduled);
        }
        if (this.mBroadcastsScheduled) {
            if (System.currentTimeMillis() - this.mLastTimeForDispatchMsg > JobStatus.DEFAULT_TRIGGER_MAX_DELAY) {
                Slog.d(TAG, "Schedule broadcasts:Bad suitation happend, maybe we lost the BROADCAST_INTENT_MSG msg!");
                this.mHandler.dump(this.mLogPrinterForMsgDump, "msgQueue");
                this.mBroadcastsScheduled = false;
            } else {
                return;
            }
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(200, this));
        this.mBroadcastsScheduled = true;
        this.mLastTimeForDispatchMsg = System.currentTimeMillis();
    }

    public BroadcastRecord getMatchingOrderedReceiver(IBinder receiver) {
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Object obj;
                String str = TAG;
                StringBuilder append = new StringBuilder().append("mQueueName : ").append(this.mQueueName).append(" r = ").append(r).append(" r.receiver ");
                if (r != null) {
                    obj = r.receiver;
                } else {
                    obj = null;
                }
                Slog.v(str, append.append(obj).toString());
            }
            if (r == null || r.receiver != receiver) {
                return null;
            }
            return r;
        }
        return null;
    }

    public ReceiverRecord getMatchingNotOrderedReceiver(IBinder receiver, int hasCode) {
        ReceiverRecord receiverRecord = (ReceiverRecord) this.mReceiverRecords.get(hasCode);
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "getMatchingNotOrderedReceiver receiverRecord " + receiverRecord);
        }
        if (receiverRecord == null) {
            return null;
        }
        ProcessRecord app = receiverRecord.getApp();
        if (app != null) {
            synchronized (app.receiverRecords) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG, "app.receiverRecords size = " + app.receiverRecords.size() + " app " + app + " app.receiverRecords = " + app.receiverRecords);
                }
                app.receiverRecords.remove(receiverRecord);
            }
        }
        this.mReceiverRecords.remove(hasCode);
        return receiverRecord;
    }

    public boolean finishReceiverLocked(BroadcastRecord r, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort, boolean waitForServices) {
        int state = r.state;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT && r != null) {
            Slog.i(TAG, "finishReceiverLocked : mQueueName = " + this.mQueueName + ",r = " + r.toString() + ", state = " + state + " " + Debug.getCallers(4));
        }
        ActivityInfo receiver = r.curReceiver;
        r.state = 0;
        if (state == 0) {
            Slog.w(TAG, "finishReceiver [" + this.mQueueName + "] called but state is IDLE");
        }
        r.receiver = null;
        r.intent.setComponent(null);
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST && r.curApp != null) {
            Slog.v(TAG, "r.curApp  = " + r.curApp + " r.curApp.foreCurReceiver " + r.curApp.foreCurReceiver + " r.curApp.backCurReceiver " + r.curApp.backCurReceiver + " r.curApp.oppoforeCurReceiver " + r.curApp.oppoforeCurReceiver + " r.curApp.oppobackCurReceiver " + r.curApp.oppobackCurReceiver);
        }
        if (r.curApp != null && (r.curApp.foreCurReceiver == r || r.curApp.backCurReceiver == r || r.curApp.oppoforeCurReceiver == r || r.curApp.oppobackCurReceiver == r)) {
            oppoSetProcReceive(false, r, r.curApp);
            if (r.curApp.foreCurReceiver != null) {
                r.curApp.curReceiver = r.curApp.foreCurReceiver;
            } else if (r.curApp.backCurReceiver != null) {
                r.curApp.curReceiver = r.curApp.backCurReceiver;
            } else if (r.curApp.oppoforeCurReceiver != null) {
                r.curApp.curReceiver = r.curApp.oppoforeCurReceiver;
            } else if (r.curApp.oppobackCurReceiver != null) {
                r.curApp.curReceiver = r.curApp.oppobackCurReceiver;
            } else {
                r.curApp.curReceiver = null;
            }
        }
        if (r.curFilter != null) {
            r.curFilter.receiverList.curBroadcast = null;
        }
        r.curFilter = null;
        r.curReceiver = null;
        r.curApp = null;
        this.mPendingBroadcast = null;
        r.resultCode = resultCode;
        r.resultData = resultData;
        r.resultExtras = resultExtras;
        if (resultAbort && (r.intent.getFlags() & 134217728) == 0) {
            r.resultAbort = resultAbort;
        } else {
            r.resultAbort = false;
        }
        if (waitForServices && r.curComponent != null && r.queue.mDelayBehindServices && r.queue.mOrderedBroadcasts.size() > 0 && r.queue.mOrderedBroadcasts.get(0) == r) {
            ActivityInfo nextReceiver;
            if (r.nextReceiver < r.receivers.size()) {
                Object obj = r.receivers.get(r.nextReceiver);
                nextReceiver = obj instanceof ActivityInfo ? (ActivityInfo) obj : null;
            } else {
                nextReceiver = null;
            }
            if ((receiver == null || nextReceiver == null || receiver.applicationInfo.uid != nextReceiver.applicationInfo.uid || !receiver.processName.equals(nextReceiver.processName)) && this.mService.mServices.hasBackgroundServices(r.userId)) {
                ServiceMap smap = (ServiceMap) this.mService.mServices.mServiceMap.get(r.userId);
                if (smap != null) {
                    Slog.d(TAG_BROADCAST, "BDC-mStartingBackground size = " + smap.mStartingBackground.size() + " mStartingBackground = " + smap.mStartingBackground + " mMaxStartingBackground = " + this.mService.mServices.mMaxStartingBackground);
                }
                Slog.i(TAG, "Delay finish: " + r.curComponent.flattenToShortString());
                r.state = 4;
                return false;
            }
        }
        r.curComponent = null;
        boolean z = state != 1 ? state == 3 : true;
        return z;
    }

    public void backgroundServicesFinishedLocked(int userId) {
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord br = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (br.userId == userId && br.state == 4) {
                Slog.i(TAG, "Resuming delayed broadcast");
                br.curComponent = null;
                br.state = 0;
                processNextBroadcast(false);
            }
        }
    }

    void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
        if (app == null) {
            receiver.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
        } else if (app.thread != null) {
            try {
                app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode, data, extras, ordered, sticky, sendingUser, app.repProcState);
            } catch (RemoteException ex) {
                synchronized (this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    Slog.w(TAG, "Can't deliver broadcast to " + app.processName + " (pid " + app.pid + "). Crashing it.");
                    app.scheduleCrash("can't deliver broadcast");
                    Slog.w(TAG, "can't deliver broadcast, let's cleanup...");
                    this.mService.appDiedLocked(app);
                    throw ex;
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        } else {
            throw new RemoteException("app.thread must not be null");
        }
    }

    private void deliverToRegisteredReceiverLocked(BroadcastRecord r, BroadcastFilter filter, boolean ordered, int index) {
        boolean skip = false;
        if (filter.requiredPermission != null) {
            if (this.mService.checkComponentPermission(filter.requiredPermission, r.callingPid, r.callingUid, -1, true) != 0) {
                Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires " + filter.requiredPermission + " due to registered receiver " + filter);
                skip = true;
            } else {
                int opCode = AppOpsManager.permissionToOpCode(filter.requiredPermission);
                if (opCode < 0 && opCode != -1) {
                    Slog.w(TAG, "opCode = " + opCode + "is Illegal,skip deliver.");
                    skip = true;
                } else if (opCode != -1) {
                    if (this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) != 0) {
                        Slog.w(TAG, "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(filter.requiredPermission) + " due to registered receiver " + filter);
                        skip = true;
                    }
                }
            }
        }
        if (!skip && r.requiredPermissions != null && r.requiredPermissions.length > 0) {
            int i = 0;
            while (i < r.requiredPermissions.length) {
                String requiredPermission = r.requiredPermissions[i];
                if (this.mService.checkComponentPermission(requiredPermission, filter.receiverList.pid, filter.receiverList.uid, -1, true) == 0) {
                    int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                    if (appOp < 0 && appOp != -1) {
                        Slog.w(TAG, "appOp = " + appOp + "is Illegal,skip deliver.");
                        skip = true;
                        break;
                    }
                    if (!(appOp == -1 || appOp == r.appOp)) {
                        if (this.mService.mAppOpsService.noteOperation(appOp, filter.receiverList.uid, filter.packageName) != 0) {
                            Slog.w(TAG, "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                            skip = true;
                            break;
                        }
                    }
                    i++;
                } else {
                    Slog.w(TAG, "Permission Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip = true;
                    break;
                }
            }
        }
        if (!skip && ((r.requiredPermissions == null || r.requiredPermissions.length == 0) && this.mService.checkComponentPermission(null, filter.receiverList.pid, filter.receiverList.uid, -1, true) != 0)) {
            Slog.w(TAG, "Permission Denial: security check failed when receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
            skip = true;
        }
        if (!(skip || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, filter.receiverList.uid, filter.packageName) == 0)) {
            Slog.w(TAG, "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
            skip = true;
        }
        if (!skip && this.mService.checkAllowBackgroundLocked(filter.receiverList.uid, filter.packageName, -1, true) == 2) {
            Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")");
            skip = true;
        }
        if (!this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, filter.receiverList.uid)) {
            skip = true;
        }
        if (!skip && (filter.receiverList.app == null || filter.receiverList.app.crashing)) {
            Slog.w(TAG, "Skipping deliver [" + this.mQueueName + "] " + r + " to " + filter.receiverList + ": process crashing");
            skip = true;
        }
        if (!(skip || filter.receiverList.app == null || !OppoBroadcastManager.getInstance(this.mService).skipSpecialBroadcast(filter.receiverList.app, filter.packageName, r.intent, filter.receiverList.app.processName, filter.receiverList.app.uid, filter.receiverList.app.info))) {
            skip = true;
        }
        if (!skip) {
            skip = OppoProcessManagerHelper.skipBroadcast(filter, r, ordered);
        }
        if (skip) {
            r.delivery[index] = 2;
            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.d(TAG_BROADCAST, "BDC-Skip broadcast: " + r.intent + ", queue=" + this.mQueueName + ", ordered=" + ordered + ", filter=" + filter + ", broadcastRecord=" + r + ", receiver=" + r.receiver + ", #" + index);
            }
            return;
        }
        if (Build.isPermissionReviewRequired()) {
            if (!requestStartTargetPermissionsReviewIfNeededLocked(r, filter.packageName, filter.owningUserId)) {
                r.delivery[index] = 2;
                if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.d(TAG_BROADCAST, "BDC-Skip by permissions, broadcast: " + r.intent + ", queue=" + this.mQueueName + ", ordered=" + ordered + ", filter=" + filter + ", broadcastRecord=" + r + ", receiver=" + r.receiver + ", #" + index);
                }
                return;
            }
        }
        r.delivery[index] = 1;
        if (ordered) {
            r.receiver = filter.receiverList.receiver.asBinder();
            r.curFilter = filter;
            filter.receiverList.curBroadcast = r;
            r.state = 2;
            if (filter.receiverList.app != null) {
                r.curApp = filter.receiverList.app;
                filter.receiverList.app.curReceiver = r;
                oppoSetProcReceive(true, r, filter.receiverList.app);
                this.mService.updateOomAdjLocked(r.curApp);
            }
        }
        try {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                Slog.i(TAG_BROADCAST, "Delivering to " + filter + " : " + r);
            }
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.d(TAG_BROADCAST, "BDC-Delivering broadcast: " + r.intent + ", queue=" + this.mQueueName + ", ordered=" + ordered + ", filter=" + filter + ", broadcastRecord=" + r + ", receiver=" + r.receiver + ", #" + index);
            }
            if (filter.receiverList.app == null || !filter.receiverList.app.inFullBackup) {
                if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                    ReadyToStartDynamicReceiver eventData = ReadyToStartDynamicReceiver.createInstance();
                    Object[] objArr = new Object[3];
                    objArr[0] = filter.packageName;
                    objArr[1] = r.callerPackage;
                    objArr[2] = Integer.valueOf(r.callingUid);
                    eventData.set(objArr);
                    this.mService.getAMEventHook().hook(Event.AM_ReadyToStartDynamicReceiver, eventData);
                }
                performReceiveLocked(filter.receiverList.app, filter.receiverList.receiver, new Intent(r.intent), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.initialSticky, r.userId);
            } else if (ordered) {
                skipReceiverLocked(r);
            }
            if (ordered) {
                r.state = 3;
            }
        } catch (Throwable e) {
            Slog.w(TAG, "Failure sending broadcast " + r.intent + " to " + filter + " , " + filter.receiverList.app, e);
            if (ordered) {
                r.receiver = null;
                r.curFilter = null;
                filter.receiverList.curBroadcast = null;
                if (filter.receiverList.app != null) {
                    oppoSetProcReceive(false, r, filter.receiverList.app);
                    if (filter.receiverList.app.foreCurReceiver != null) {
                        filter.receiverList.app.curReceiver = filter.receiverList.app.foreCurReceiver;
                    } else if (filter.receiverList.app.backCurReceiver != null) {
                        filter.receiverList.app.curReceiver = filter.receiverList.app.backCurReceiver;
                    } else if (filter.receiverList.app.oppoforeCurReceiver != null) {
                        filter.receiverList.app.curReceiver = filter.receiverList.app.oppoforeCurReceiver;
                    } else if (filter.receiverList.app.oppobackCurReceiver != null) {
                        filter.receiverList.app.curReceiver = filter.receiverList.app.oppobackCurReceiver;
                    } else {
                        filter.receiverList.app.curReceiver = null;
                    }
                }
            }
        }
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(BroadcastRecord receiverRecord, String receivingPackageName, int receivingUserId) {
        if (!this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(receivingPackageName, receivingUserId)) {
            return true;
        }
        boolean callerForeground = receiverRecord.callerApp != null ? receiverRecord.callerApp.setSchedGroup != 0 : true;
        if (!callerForeground || receiverRecord.intent.getComponent() == null) {
            Slog.w(TAG, "u" + receivingUserId + " Receiving a broadcast in package" + receivingPackageName + " requires a permissions review");
        } else {
            ActivityManagerService activityManagerService = this.mService;
            String str = receiverRecord.callerPackage;
            int i = receiverRecord.callingUid;
            int i2 = receiverRecord.userId;
            Intent[] intentArr = new Intent[1];
            intentArr[0] = receiverRecord.intent;
            String[] strArr = new String[1];
            strArr[0] = receiverRecord.intent.resolveType(this.mService.mContext.getContentResolver());
            IIntentSender target = activityManagerService.getIntentSenderLocked(1, str, i, i2, null, null, 0, intentArr, strArr, 1409286144, null);
            final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", receivingPackageName);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW || !ActivityManagerService.IS_USER_BUILD) {
                Slog.i(TAG, "u" + receivingUserId + " Launching permission review for package " + receivingPackageName);
            }
            final int i3 = receivingUserId;
            this.mHandler.post(new Runnable() {
                public void run() {
                    BroadcastQueue.this.mService.mContext.startActivityAsUser(intent, new UserHandle(i3));
                }
            });
        }
        return false;
    }

    final void scheduleTempWhitelistLocked(int uid, long duration, BroadcastRecord r) {
        if (duration > 2147483647L) {
            duration = 2147483647L;
        }
        StringBuilder b = new StringBuilder();
        b.append("broadcast:");
        UserHandle.formatUid(b, r.callingUid);
        b.append(":");
        if (r.intent.getAction() != null) {
            b.append(r.intent.getAction());
        } else if (r.intent.getComponent() != null) {
            b.append(r.intent.getComponent().flattenToShortString());
        } else if (r.intent.getData() != null) {
            b.append(r.intent.getData());
        }
        this.mHandler.obtainMessage(SCHEDULE_TEMP_WHITELIST_MSG, uid, (int) duration, b.toString()).sendToTarget();
    }

    /* JADX WARNING: Removed duplicated region for block: B:446:0x0701 A:{SYNTHETIC} */
    /* JADX WARNING: Missing block: B:71:0x0317, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:72:0x031a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:99:0x04aa, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:100:0x04ad, code:
            return;
     */
    /* JADX WARNING: Missing block: B:175:0x0895, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:176:0x0898, code:
            return;
     */
    /* JADX WARNING: Missing block: B:435:0x1596, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:436:0x1599, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "ZhiYong.Lin@Plf.Framework, modify for BPMliumei@Plf.Framework, 2013/12/04:Modify for autostart manager", property = OppoRomType.ROM)
    final void processNextBroadcast(boolean fromMsg) {
        synchronized (this.mService) {
            BroadcastRecord r;
            int i;
            ActivityManagerService.boostPriorityForLockedSection();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.i(TAG, "processNextBroadcast [" + this.mQueueName + "]: " + this.mParallelBroadcasts.size() + " broadcasts, " + this.mOrderedBroadcasts.size() + " ordered broadcasts " + Debug.getCallers(4));
                Slog.v(TAG, "processNextBroadcast thread.getName() " + Thread.currentThread().getName());
            }
            this.mService.updateCpuStats();
            if (fromMsg) {
                this.mBroadcastsScheduled = false;
            }
            long firstTime = SystemClock.uptimeMillis();
            int parallelSize = this.mParallelBroadcasts.size();
            while (this.mParallelBroadcasts.size() > 0) {
                r = (BroadcastRecord) this.mParallelBroadcasts.remove(0);
                r.dispatchTime = SystemClock.uptimeMillis();
                r.dispatchClockTime = System.currentTimeMillis();
                int N = r.receivers.size();
                if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v(TAG_BROADCAST, "BDC-Processing parallel broadcast [" + this.mQueueName + "] " + r + ", " + N + " receivers");
                }
                OppoBroadcastManager.getInstance(this.mService).adjustParallelBroadcastReceiversQueue(r);
                for (i = 0; i < N; i++) {
                    Object target = r.receivers.get(i);
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Delivering non-ordered on [" + this.mQueueName + "] to registered " + target + ": " + r);
                    }
                    deliverToRegisteredReceiverLocked(r, (BroadcastFilter) target, false, i);
                }
                long costTime = SystemClock.uptimeMillis() - r.dispatchTime;
                addBroadcastToHistoryLocked(r);
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT || costTime > 1000) {
                    Slog.v(TAG_BROADCAST, "Done with parallel broadcast [" + this.mQueueName + "] " + r + ", cost=" + costTime + "ms" + ", total=" + N);
                }
            }
            long allParallelCost = SystemClock.uptimeMillis() - firstTime;
            if (allParallelCost > ((long) this.mAllowDebugTime)) {
                Slog.v(TAG_BROADCAST, "Done with all parallel broadcast [" + this.mQueueName + "] " + ", cost=" + allParallelCost + "ms" + ", total= " + parallelSize);
            }
            if (this.mPendingBroadcast != null) {
                ProcessRecord proc;
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v(TAG_BROADCAST, "processNextBroadcast [" + this.mQueueName + "]: waiting for " + this.mPendingBroadcast.curApp);
                }
                synchronized (this.mService.mPidsSelfLocked) {
                    proc = (ProcessRecord) this.mService.mPidsSelfLocked.get(this.mPendingBroadcast.curApp.pid);
                    try {
                    } catch (Throwable th) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
                if (proc != null ? proc.crashing : true) {
                    Slog.w(TAG, "pending app  [" + this.mQueueName + "]" + this.mPendingBroadcast.curApp + " died before responding to broadcast");
                    this.mPendingBroadcast.state = 0;
                    this.mPendingBroadcast.nextReceiver = this.mPendingBroadcastRecvIndex;
                    this.mPendingBroadcast = null;
                } else {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
            }
            boolean looped = false;
            while (this.mOrderedBroadcasts.size() != 0) {
                r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
                boolean forceReceive = false;
                int numReceivers = r.receivers != null ? r.receivers.size() : 0;
                OppoBroadcastManager.getInstance(this.mService).adjustOrderedBroadcastReceiversQueue(r, numReceivers);
                if (this.mService.mProcessesReady && r.dispatchTime > 0) {
                    long now = SystemClock.uptimeMillis();
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT && numReceivers > 0) {
                        int i2;
                        String str = TAG;
                        StringBuilder append = new StringBuilder().append("Hung broadcast 2222222[").append(this.mQueueName).append("] print the time :").append(" now=").append(now).append(" enqueueClockTime=").append(r.enqueueClockTime).append(" dispatchTime=").append(r.dispatchTime).append(" startTime=").append(r.receiverTime).append(" use time = ").append(now - r.dispatchTime).append("  siganl use time = ").append(now - r.receiverTime).append(" intent=").append(r.intent).append(" numReceivers = ").append(numReceivers).append(" nextReceiver index = ").append(r.nextReceiver).append(" Receiverinfo = ");
                        List list = r.receivers;
                        if (r.nextReceiver - 1 >= 0) {
                            i2 = r.nextReceiver - 1;
                        } else {
                            i2 = 0;
                        }
                        Slog.d(str, append.append(list.get(i2)).append(" state = ").append(r.state).append(" ").append(r).toString());
                    }
                    if (numReceivers > 0 && now > r.dispatchTime + ((this.mTimeoutPeriod * 2) * ((long) numReceivers))) {
                        ActivityManagerService activityManagerService = this.mService;
                        if (!ActivityManagerService.mANRManager.isAnrDeferrable()) {
                            Slog.w(TAG, "Hung broadcast [" + this.mQueueName + "] discarded after timeout failure:" + " now=" + now + " dispatchTime=" + r.dispatchTime + " startTime=" + r.receiverTime + " intent=" + r.intent + " numReceivers=" + numReceivers + " nextReceiver=" + r.nextReceiver + " state=" + r.state);
                            broadcastTimeoutLocked(false);
                            forceReceive = true;
                            r.state = 0;
                        }
                    }
                }
                if (r.state == 0) {
                    Object[] objArr;
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG, " r.nextReceiver " + r.nextReceiver + " r.resultTo " + r.resultTo + " r " + r);
                    }
                    if (r.receivers != null && r.nextReceiver < numReceivers) {
                        if (!(r.resultAbort || forceReceive)) {
                            if (r != null) {
                                long timeoutTime;
                                int recIdx = r.nextReceiver;
                                r.nextReceiver = recIdx + 1;
                                r.receiverTime = SystemClock.uptimeMillis();
                                if (recIdx == 0) {
                                    r.dispatchTime = r.receiverTime;
                                    r.dispatchClockTime = System.currentTimeMillis();
                                    if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                                        Slog.v(TAG_BROADCAST, "BDC-Processing ordered broadcast [" + this.mQueueName + "] " + r + ", " + r.receivers.size() + " receivers");
                                    }
                                }
                                if (!this.mPendingBroadcastTimeoutMessage) {
                                    timeoutTime = r.receiverTime + this.mTimeoutPeriod;
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Submitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r + " at " + timeoutTime);
                                    }
                                    setBroadcastTimeoutLocked(timeoutTime);
                                }
                                BroadcastOptions brOptions = r.options;
                                Object nextReceiver = r.receivers.get(recIdx);
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                    Slog.v(TAG, "nextReceiver = " + nextReceiver);
                                }
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                    Slog.v(TAG, "nextReceiver instanceof BroadcastFilter = " + (nextReceiver instanceof BroadcastFilter));
                                }
                                if (nextReceiver instanceof BroadcastFilter) {
                                    BroadcastFilter filter = (BroadcastFilter) nextReceiver;
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Delivering ordered [" + this.mQueueName + "] to registered " + filter + ": " + r);
                                    }
                                    deliverToRegisteredReceiverLocked(r, filter, r.ordered, recIdx);
                                    if (r.receiver == null || !r.ordered) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Quick finishing [" + this.mQueueName + "]: ordered=" + r.ordered + " receiver=" + r.receiver);
                                        }
                                        r.state = 0;
                                        scheduleBroadcastsLocked();
                                    } else if (brOptions != null && brOptions.getTemporaryAppWhitelistDuration() > 0) {
                                        scheduleTempWhitelistLocked(filter.owningUid, brOptions.getTemporaryAppWhitelistDuration(), r);
                                    }
                                } else {
                                    ResolveInfo info = (ResolveInfo) nextReceiver;
                                    ComponentName componentName = new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
                                    if (ActivityManagerService.IS_ENG_BUILD || ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.d(TAG_BROADCAST, r + ", #" + recIdx + " " + info.activityInfo);
                                    }
                                    boolean skip = false;
                                    if (brOptions != null && (info.activityInfo.applicationInfo.targetSdkVersion < brOptions.getMinManifestReceiverApiLevel() || info.activityInfo.applicationInfo.targetSdkVersion > brOptions.getMaxManifestReceiverApiLevel())) {
                                        skip = true;
                                        if (r.intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                                            String dstPkg = info.activityInfo.applicationInfo.packageName;
                                            if (dstPkg != null && OppoListManager.getInstance().getAllowManifestNetBroList().contains(dstPkg)) {
                                                skip = false;
                                            }
                                        }
                                    }
                                    int perm = this.mService.checkComponentPermission(info.activityInfo.permission, r.callingPid, r.callingUid, info.activityInfo.applicationInfo.uid, info.activityInfo.exported);
                                    if (!skip && perm != 0) {
                                        if (info.activityInfo.exported) {
                                            Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires " + info.activityInfo.permission + " due to receiver " + componentName.flattenToShortString());
                                        } else {
                                            Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " is not exported from uid " + info.activityInfo.applicationInfo.uid + " due to receiver " + componentName.flattenToShortString());
                                        }
                                        skip = true;
                                    } else if (!(skip || info.activityInfo.permission == null)) {
                                        int opCode = AppOpsManager.permissionToOpCode(info.activityInfo.permission);
                                        if (opCode != -1) {
                                            if (this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) != 0) {
                                                Slog.w(TAG, "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(info.activityInfo.permission) + " due to registered receiver " + componentName.flattenToShortString());
                                                skip = true;
                                            }
                                        }
                                    }
                                    if (!skip && info.activityInfo.applicationInfo.uid != 1000 && r.requiredPermissions != null && r.requiredPermissions.length > 0) {
                                        for (String requiredPermission : r.requiredPermissions) {
                                            try {
                                                perm = AppGlobals.getPackageManager().checkPermission(requiredPermission, info.activityInfo.applicationInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                            } catch (RemoteException e) {
                                                perm = -1;
                                            }
                                            if (perm != 0) {
                                                Slog.w(TAG, "Permission Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                                skip = true;
                                                break;
                                            }
                                            int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                                            if (!(appOp == -1 || appOp == r.appOp)) {
                                                if (this.mService.mAppOpsService.noteOperation(appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) != 0) {
                                                    Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                                    skip = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (!(skip || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) == 0)) {
                                        Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                        skip = true;
                                    }
                                    if (!skip) {
                                        skip = !this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, info.activityInfo.applicationInfo.uid);
                                    }
                                    boolean isSingleton = false;
                                    try {
                                        isSingleton = this.mService.isSingleton(info.activityInfo.processName, info.activityInfo.applicationInfo, info.activityInfo.name, info.activityInfo.flags);
                                    } catch (SecurityException e2) {
                                        Slog.w(TAG, e2.getMessage());
                                        skip = true;
                                    }
                                    if (!((info.activityInfo.flags & 1073741824) == 0 || ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS", info.activityInfo.applicationInfo.uid) == 0)) {
                                        Slog.w(TAG, "Permission Denial: Receiver " + componentName.flattenToShortString() + " requests FLAG_SINGLE_USER, but app does not hold " + "android.permission.INTERACT_ACROSS_USERS");
                                        skip = true;
                                    }
                                    if (skip) {
                                        r.manifestSkipCount++;
                                    } else {
                                        r.manifestCount++;
                                    }
                                    if (r.curApp != null && r.curApp.crashing) {
                                        Slog.w(TAG, "Skipping deliver ordered [" + this.mQueueName + "] " + r + " to " + r.curApp + ": process crashing");
                                        skip = true;
                                    }
                                    if (!skip) {
                                        boolean isAvailable = false;
                                        try {
                                            isAvailable = AppGlobals.getPackageManager().isPackageAvailable(info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                        } catch (Throwable e3) {
                                            Slog.w(TAG, "Exception getting recipient info for " + info.activityInfo.packageName, e3);
                                        }
                                        if (!isAvailable) {
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                Slog.v(TAG_BROADCAST, "Skipping delivery to " + info.activityInfo.packageName + " / " + info.activityInfo.applicationInfo.uid + " : package no longer available");
                                            }
                                            skip = true;
                                        }
                                    }
                                    if (Build.isPermissionReviewRequired() && !skip) {
                                        if (!requestStartTargetPermissionsReviewIfNeededLocked(r, info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid))) {
                                            skip = true;
                                        }
                                    }
                                    if (!skip) {
                                        skip = OppoAppStartupManager.getInstance().handleSpecialBroadcast(r.intent, r.callerApp, info.activityInfo.packageName);
                                    }
                                    if (OppoBroadcastManager.getInstance(this.mService).skipSpecialBroadcast(null, info.activityInfo.packageName, r.intent, info.activityInfo.applicationInfo.processName, info.activityInfo.applicationInfo.uid, info.activityInfo.applicationInfo)) {
                                        skip = true;
                                    }
                                    int receiverUid = info.activityInfo.applicationInfo.uid;
                                    if (r.callingUid != 1000 && isSingleton && this.mService.isValidSingletonCall(r.callingUid, receiverUid)) {
                                        info.activityInfo = this.mService.getActivityInfoForUser(info.activityInfo, 0);
                                    }
                                    String targetProcess = info.activityInfo.processName;
                                    ProcessRecord app = this.mService.getProcessRecordLocked(targetProcess, info.activityInfo.applicationInfo.uid, false);
                                    if (!skip) {
                                        int allowed = this.mService.checkAllowBackgroundLocked(info.activityInfo.applicationInfo.uid, info.activityInfo.packageName, -1, false);
                                        if (allowed != 0) {
                                            if (allowed == 2) {
                                                Slog.w(TAG, "Background execution disabled: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                                skip = true;
                                            } else if ((r.intent.getFlags() & 8388608) != 0 || (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & 16777216) == 0)) {
                                                Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                                skip = true;
                                            }
                                        }
                                    }
                                    if (skip) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Skipping delivery of ordered [" + this.mQueueName + "] " + r + " for whatever reason");
                                        }
                                        r.delivery[recIdx] = 2;
                                        r.receiver = null;
                                        r.curFilter = null;
                                        r.state = 0;
                                        scheduleBroadcastsLocked();
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    }
                                    r.delivery[recIdx] = 1;
                                    r.state = 1;
                                    r.curComponent = componentName;
                                    r.curReceiver = info.activityInfo;
                                    if (ActivityManagerDebugConfig.DEBUG_MU && r.callingUid > 100000) {
                                        Slog.v(TAG_MU, "Updated broadcast record activity info for secondary user, " + info.activityInfo + ", callingUid = " + r.callingUid + ", uid = " + info.activityInfo.applicationInfo.uid);
                                    }
                                    if (brOptions != null && brOptions.getTemporaryAppWhitelistDuration() > 0) {
                                        scheduleTempWhitelistLocked(receiverUid, brOptions.getTemporaryAppWhitelistDuration(), r);
                                    }
                                    try {
                                        AppGlobals.getPackageManager().setPackageStoppedState(r.curComponent.getPackageName(), false, UserHandle.getUserId(r.callingUid));
                                    } catch (RemoteException e4) {
                                    } catch (IllegalArgumentException e5) {
                                        Slog.w(TAG, "Failed trying to unstop package " + r.curComponent.getPackageName() + ": " + e5);
                                    }
                                    if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                                        PackageStoppedStatusChanged eventData1 = PackageStoppedStatusChanged.createInstance();
                                        objArr = new Object[3];
                                        objArr[0] = r.curComponent.getPackageName();
                                        objArr[1] = Integer.valueOf(0);
                                        objArr[2] = "processNextBroadcast";
                                        eventData1.set(objArr);
                                        this.mService.getAMEventHook().hook(Event.AM_PackageStoppedStatusChanged, eventData1);
                                    }
                                    if (!(app == null || app.thread == null)) {
                                        try {
                                            app.addPackage(info.activityInfo.packageName, info.activityInfo.applicationInfo.versionCode, this.mService.mProcessStats);
                                            if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                                                ReadyToStartStaticReceiver eventData = ReadyToStartStaticReceiver.createInstance();
                                                objArr = new Object[3];
                                                objArr[0] = info.activityInfo.packageName;
                                                objArr[1] = r.callerPackage;
                                                objArr[2] = Integer.valueOf(r.callingUid);
                                                eventData.set(objArr);
                                                this.mService.getAMEventHook().hook(Event.AM_ReadyToStartStaticReceiver, eventData);
                                            }
                                            processCurBroadcastLocked(r, app);
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        } catch (Throwable e6) {
                                            Slog.w(TAG, "Exception when sending broadcast to " + r.curComponent, e6);
                                        } catch (Throwable e7) {
                                            Slog.wtf(TAG, "Failed sending broadcast to " + r.curComponent + " with " + r.intent, e7);
                                            logBroadcastReceiverDiscardLocked(r);
                                            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                            scheduleBroadcastsLocked();
                                            r.state = 0;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Need to start app [" + this.mQueueName + "] " + targetProcess + " for broadcast " + r);
                                    }
                                    if (OppoAbnormalAppManager.getInstance().validStartBroadcast(info.activityInfo.packageName)) {
                                        Slog.d(OppoAbnormalAppManager.TAG, "unable to start " + info.activityInfo.packageName + " for broadcast : package is restricted");
                                        finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, true);
                                        scheduleBroadcastsLocked();
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    } else if (OppoAutostartManager.checkAutoBootForbiddenStart(this, info, r)) {
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    } else {
                                        if (!(r.intent == null || r.callingUid == 1000)) {
                                            if (r.callerApp != null && !OppoAppStartupManager.getInstance().isAllowStartFromBroadCast(r.callerApp, null, r.callingUid, r.intent, info)) {
                                                finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                                scheduleBroadcastsLocked();
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                return;
                                            } else if (r.callerApp == null && !OppoAppStartupManager.getInstance().isAllowStartFromBroadCast(r.callingUid, r.callingPid, r.intent, info)) {
                                                finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                                scheduleBroadcastsLocked();
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                return;
                                            }
                                        }
                                        OppoAppStartupManager.getInstance().handleProcessStartupInfo(r.callingPid, r.callingUid, r.callerApp, r.intent, info.activityInfo.applicationInfo, OppoProcessManager.RESUME_REASON_BROADCAST_STR);
                                        if ((r.intent.getFlags() & 268435456) != 0) {
                                            cancelBroadcastTimeoutLocked();
                                            timeoutTime = r.receiverTime + (this.mTimeoutPeriod * 2);
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                Slog.v(TAG_BROADCAST, "ReSubmitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r + " at " + timeoutTime + " for fgBroadcast.");
                                            }
                                            setBroadcastTimeoutLocked(timeoutTime);
                                        }
                                        ProcessRecord startProcessLocked = this.mService.startProcessLocked(targetProcess, info.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, OppoProcessManager.RESUME_REASON_BROADCAST_STR, r.curComponent, (r.intent.getFlags() & 33554432) != 0, false, false);
                                        r.curApp = startProcessLocked;
                                        if (startProcessLocked == null) {
                                            Slog.w(TAG, "Unable to launch app " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + ": process is bad");
                                            logBroadcastReceiverDiscardLocked(r);
                                            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                            scheduleBroadcastsLocked();
                                            r.state = 0;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                        this.mPendingBroadcast = r;
                                        this.mPendingBroadcastRecvIndex = recIdx;
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG, "mPendingBroadcast curApp = " + this.mPendingBroadcast.curApp + " pid = " + this.mPendingBroadcast.curApp.pid);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (r.resultTo != null) {
                        try {
                            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.i(TAG_BROADCAST, "BDC-Finishing broadcast [" + this.mQueueName + "] " + r.intent.getAction() + " app=" + r.callerApp + " receiver=" + r.resultTo);
                            }
                            if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                                ReadyToStartDynamicReceiver eventData2 = ReadyToStartDynamicReceiver.createInstance();
                                objArr = new Object[3];
                                objArr[0] = r.callerPackage;
                                objArr[1] = r.callerPackage;
                                objArr[2] = Integer.valueOf(r.callingUid);
                                eventData2.set(objArr);
                                this.mService.getAMEventHook().hook(Event.AM_ReadyToStartDynamicReceiver, eventData2);
                            }
                            performReceiveLocked(r.callerApp, r.resultTo, new Intent(r.intent), r.resultCode, r.resultData, r.resultExtras, false, false, r.userId);
                            r.resultTo = null;
                        } catch (Throwable e62) {
                            r.resultTo = null;
                            Slog.w(TAG, "Failure [" + this.mQueueName + "] sending broadcast result of " + r.intent, e62);
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Cancelling BROADCAST_TIMEOUT_MSG");
                    }
                    cancelBroadcastTimeoutLocked();
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                        Slog.v(TAG_BROADCAST, "Finished with ordered broadcast " + r);
                    }
                    addBroadcastToHistoryLocked(r);
                    if (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & 1073741824) == 0) {
                        this.mService.addBroadcastStatLocked(r.intent.getAction(), r.callerPackage, r.manifestCount, r.manifestSkipCount, r.finishTime - r.dispatchTime);
                    }
                    this.mOrderedBroadcasts.remove(0);
                    r = null;
                    looped = true;
                    continue;
                    if (r != null) {
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.d(TAG_BROADCAST, "processNextBroadcast(" + this.mQueueName + ") called when not idle (state=" + r.state + ")");
                }
            }
            this.mService.scheduleAppGcsLocked();
            if (looped) {
                this.mService.updateOomAdjLocked();
            }
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "No more broadcasts pending, so all done!");
            }
        }
    }

    final void setBroadcastTimeoutLocked(long timeoutTime) {
        if (!this.mPendingBroadcastTimeoutMessage) {
            this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(BROADCAST_TIMEOUT_MSG, this), timeoutTime);
            this.mPendingBroadcastTimeoutMessage = true;
            if (2 == ANRManager.enableANRDebuggingMechanism()) {
                this.mService.mAnrHandler.sendMessageAtTime(this.mService.mAnrHandler.obtainMessage(1001, this.mAnrBroadcastQueue), timeoutTime - (this.mTimeoutPeriod / 2));
            }
        }
    }

    final void cancelBroadcastTimeoutLocked() {
        if (this.mPendingBroadcastTimeoutMessage) {
            this.mHandler.removeMessages(BROADCAST_TIMEOUT_MSG, this);
            this.mPendingBroadcastTimeoutMessage = false;
            if (2 == ANRManager.enableANRDebuggingMechanism()) {
                this.mService.mAnrHandler.removeMessages(1001, this.mAnrBroadcastQueue);
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "ZhiYong.Lin@Plf.Framework, modify for BPM", property = OppoRomType.ROM)
    final void broadcastTimeoutLocked(boolean fromMsg) {
        if (fromMsg) {
            this.mPendingBroadcastTimeoutMessage = false;
            if (2 == ANRManager.enableANRDebuggingMechanism()) {
                this.mService.mAnrHandler.removeMessages(1001, this.mAnrBroadcastQueue);
            }
        }
        if (this.mOrderedBroadcasts.size() != 0) {
            long now = SystemClock.uptimeMillis();
            final BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST && r != null) {
                Slog.w(TAG, "Timeout of broadcast1111 " + r + " - receiver=" + r.receiver + ", started " + (now - r.receiverTime) + "ms ago  ");
            }
            if (fromMsg) {
                if (this.mService.mProcessesReady) {
                    ActivityManagerService activityManagerService = this.mService;
                    if (ActivityManagerService.mANRManager.isAnrDeferrable()) {
                        Slog.d(TAG, "Skip BROADCAST_TIMEOUT ANR: " + r);
                        this.mService.mDidDexOpt = true;
                    }
                }
                if (this.mService.mDidDexOpt) {
                    this.mService.mDidDexOpt = false;
                    setBroadcastTimeoutLocked(SystemClock.uptimeMillis() + this.mTimeoutPeriod);
                    return;
                } else if (this.mService.mProcessesReady) {
                    long timeoutTime = r.receiverTime + this.mTimeoutPeriod;
                    if (timeoutTime > now) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG_BROADCAST, "Premature timeout [" + this.mQueueName + "] @ " + now + ": resetting BROADCAST_TIMEOUT_MSG for " + timeoutTime);
                        }
                        setBroadcastTimeoutLocked(timeoutTime);
                        return;
                    }
                } else {
                    return;
                }
            }
            BroadcastRecord br = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (br.state == 4) {
                Slog.i(TAG, "Waited long enough for: " + (br.curComponent != null ? br.curComponent.flattenToShortString() : "(null)"));
                br.curComponent = null;
                br.state = 0;
                processNextBroadcast(false);
                return;
            }
            Slog.w(TAG, "Timeout of broadcast " + r + " - receiver=" + r.receiver + ", started " + (now - r.receiverTime) + "ms ago");
            r.receiverTime = now;
            r.anrCount++;
            if (r.nextReceiver <= 0) {
                Slog.w(TAG, "Timeout on receiver with nextReceiver <= 0");
                return;
            }
            ProcessRecord app = null;
            String anrMessage = null;
            BroadcastFilter curReceiver = r.receivers.get(r.nextReceiver - 1);
            r.delivery[r.nextReceiver - 1] = 3;
            Slog.w(TAG, "Receiver during timeout: " + curReceiver + " r.state " + r.state);
            logBroadcastReceiverDiscardLocked(r);
            if (curReceiver instanceof BroadcastFilter) {
                BroadcastFilter bf = curReceiver;
                if (!(bf.receiverList.pid == 0 || bf.receiverList.pid == ActivityManagerService.MY_PID)) {
                    synchronized (this.mService.mPidsSelfLocked) {
                        app = (ProcessRecord) this.mService.mPidsSelfLocked.get(bf.receiverList.pid);
                    }
                }
            } else {
                app = r.curApp;
            }
            BroadcastRecord record = r;
            final ProcessRecord processRecord = app;
            new Thread(new Runnable() {
                public void run() {
                    if (r.ordered && processRecord != null && processRecord.thread != null) {
                        int flag = 0;
                        try {
                            if (r.intent != null) {
                                flag = r.intent.getFlags();
                            }
                            Slog.w(BroadcastQueue.TAG, "Timeout receiver state " + processRecord.thread.getBroadcastState(flag) + " in proc " + processRecord + " broadcast " + r);
                        } catch (RemoteException e) {
                            Slog.v(BroadcastQueue.TAG, "RemoteException " + e + " record " + r);
                        } catch (Exception e2) {
                            Slog.e(BroadcastQueue.TAG, "Exception when get broadcast state!", e2);
                        }
                    }
                }
            }).start();
            if (app != null) {
                anrMessage = "Broadcast of " + r.intent.toString();
            }
            if (this.mPendingBroadcast == r) {
                this.mPendingBroadcast = null;
            }
            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            scheduleBroadcastsLocked();
            if (anrMessage != null && !OppoProcessManagerHelper.checkProcessWhileBroadcastTimeout(app)) {
                this.mHandler.post(new AppNotResponding(app, anrMessage));
            }
        }
    }

    private final int ringAdvance(int x, int increment, int ringSize) {
        x += increment;
        if (x < 0) {
            return ringSize - 1;
        }
        if (x >= ringSize) {
            return 0;
        }
        return x;
    }

    private final void addBroadcastToHistoryLocked(BroadcastRecord r) {
        if (r.callingUid >= 0) {
            r.finishTime = SystemClock.uptimeMillis();
            if (ActivityManagerService.IS_ENG_BUILD || ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.d(TAG_BROADCAST, (r.ordered ? "Ordered" : "Non-ordered") + " [" + this.mQueueName + "] " + r + ", " + (r.receivers != null ? Integer.valueOf(r.receivers.size()) : "null") + " receivers" + ", Total: " + (r.finishTime - r.enqueueTime) + ", Waiting: " + (r.dispatchTime - r.enqueueTime) + ", Processing: " + (r.finishTime - r.dispatchTime));
            }
            this.mBroadcastHistory[this.mHistoryNext] = r;
            this.mHistoryNext = ringAdvance(this.mHistoryNext, 1, MAX_BROADCAST_HISTORY);
            this.mBroadcastSummaryHistory[this.mSummaryHistoryNext] = r.intent;
            this.mSummaryHistoryEnqueueTime[this.mSummaryHistoryNext] = r.enqueueClockTime;
            this.mSummaryHistoryDispatchTime[this.mSummaryHistoryNext] = r.dispatchClockTime;
            this.mSummaryHistoryFinishTime[this.mSummaryHistoryNext] = System.currentTimeMillis();
            this.mSummaryHistoryNext = ringAdvance(this.mSummaryHistoryNext, 1, MAX_BROADCAST_SUMMARY_HISTORY);
        }
    }

    boolean cleanupDisabledPackageReceiversLocked(String packageName, Set<String> filterByClasses, int userId, boolean doit) {
        int i;
        boolean didSomething = false;
        for (i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
            didSomething |= ((BroadcastRecord) this.mParallelBroadcasts.get(i)).cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        for (i = this.mOrderedBroadcasts.size() - 1; i >= 0; i--) {
            didSomething |= ((BroadcastRecord) this.mOrderedBroadcasts.get(i)).cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        return didSomething;
    }

    final void logBroadcastReceiverDiscardLocked(BroadcastRecord r) {
        int logIndex = r.nextReceiver - 1;
        Object[] objArr;
        if (logIndex < 0 || logIndex >= r.receivers.size()) {
            if (logIndex < 0) {
                Slog.w(TAG, "Discarding broadcast before first receiver is invoked: " + r);
            }
            objArr = new Object[5];
            objArr[0] = Integer.valueOf(-1);
            objArr[1] = Integer.valueOf(System.identityHashCode(r));
            objArr[2] = r.intent.getAction();
            objArr[3] = Integer.valueOf(r.nextReceiver);
            objArr[4] = "NONE";
            EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_APP, objArr);
            return;
        }
        BroadcastFilter curReceiver = r.receivers.get(logIndex);
        if (curReceiver instanceof BroadcastFilter) {
            BroadcastFilter bf = curReceiver;
            objArr = new Object[5];
            objArr[0] = Integer.valueOf(bf.owningUserId);
            objArr[1] = Integer.valueOf(System.identityHashCode(r));
            objArr[2] = r.intent.getAction();
            objArr[3] = Integer.valueOf(logIndex);
            objArr[4] = Integer.valueOf(System.identityHashCode(bf));
            EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_FILTER, objArr);
            return;
        }
        ResolveInfo ri = (ResolveInfo) curReceiver;
        objArr = new Object[5];
        objArr[0] = Integer.valueOf(UserHandle.getUserId(ri.activityInfo.applicationInfo.uid));
        objArr[1] = Integer.valueOf(System.identityHashCode(r));
        objArr[2] = r.intent.getAction();
        objArr[3] = Integer.valueOf(logIndex);
        objArr[4] = ri.toString();
        EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_APP, objArr);
    }

    /* JADX WARNING: Missing block: B:47:0x0194, code:
            if (r24.equals(r18.mPendingBroadcast.callerPackage) != false) goto L_0x0196;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        boolean printed;
        int i;
        Bundle bundle;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (this.mParallelBroadcasts.size() > 0 || this.mOrderedBroadcasts.size() > 0 || this.mPendingBroadcast != null) {
            BroadcastRecord br;
            printed = false;
            for (i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
                br = (BroadcastRecord) this.mParallelBroadcasts.get(i);
                if (dumpPackage != null) {
                    if (!dumpPackage.equals(br.callerPackage)) {
                    }
                }
                if (!printed) {
                    if (needSep) {
                        pw.println();
                    }
                    needSep = true;
                    printed = true;
                    pw.println("  Active broadcasts [" + this.mQueueName + "]:");
                }
                pw.println("  Active Broadcast " + this.mQueueName + " #" + i + ":");
                br.dump(pw, "    ", sdf);
            }
            printed = false;
            needSep = true;
            for (i = this.mOrderedBroadcasts.size() - 1; i >= 0; i--) {
                br = (BroadcastRecord) this.mOrderedBroadcasts.get(i);
                if (dumpPackage != null) {
                    if (!dumpPackage.equals(br.callerPackage)) {
                    }
                }
                if (!printed) {
                    if (needSep) {
                        pw.println();
                    }
                    needSep = true;
                    printed = true;
                    pw.println("  Active ordered broadcasts [" + this.mQueueName + "]:");
                }
                pw.println("  Active Ordered Broadcast " + this.mQueueName + " #" + i + ":");
                ((BroadcastRecord) this.mOrderedBroadcasts.get(i)).dump(pw, "    ", sdf);
            }
            if (dumpPackage != null) {
                if (this.mPendingBroadcast != null) {
                }
            }
            if (needSep) {
                pw.println();
            }
            pw.println("  Pending broadcast [" + this.mQueueName + "]:");
            if (this.mPendingBroadcast != null) {
                this.mPendingBroadcast.dump(pw, "    ", sdf);
            } else {
                pw.println("    (null)");
            }
            needSep = true;
        }
        printed = false;
        i = -1;
        int lastIndex = this.mHistoryNext;
        int ringIndex = lastIndex;
        do {
            ringIndex = ringAdvance(ringIndex, -1, MAX_BROADCAST_HISTORY);
            BroadcastRecord r = this.mBroadcastHistory[ringIndex];
            if (r != null) {
                i++;
                if (dumpPackage != null) {
                    if (!dumpPackage.equals(r.callerPackage)) {
                        continue;
                    }
                }
                if (!printed) {
                    if (needSep) {
                        pw.println();
                    }
                    needSep = true;
                    pw.println("  Historical broadcasts [" + this.mQueueName + "]:");
                    printed = true;
                }
                if (dumpAll) {
                    pw.print("  Historical Broadcast " + this.mQueueName + " #");
                    pw.print(i);
                    pw.println(":");
                    r.dump(pw, "    ", sdf);
                    continue;
                } else {
                    pw.print("  #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(r);
                    pw.print("    ");
                    pw.println(r.intent.toShortString(false, true, true, false));
                    if (!(r.targetComp == null || r.targetComp == r.intent.getComponent())) {
                        pw.print("    targetComp: ");
                        pw.println(r.targetComp.toShortString());
                    }
                    bundle = r.intent.getExtras();
                    if (bundle != null) {
                        pw.print("    extras: ");
                        pw.println(bundle.toString());
                        continue;
                    } else {
                        continue;
                    }
                }
            }
        } while (ringIndex != lastIndex);
        if (dumpPackage == null) {
            int ringIndex2;
            ringIndex = this.mSummaryHistoryNext;
            lastIndex = ringIndex;
            if (dumpAll) {
                printed = false;
                i = -1;
                ringIndex2 = ringIndex;
            } else {
                int j = i;
                ringIndex2 = ringIndex;
                while (j > 0 && ringIndex2 != ringIndex) {
                    ringIndex2 = ringAdvance(ringIndex2, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                    if (this.mBroadcastHistory[ringIndex2] != null) {
                        j--;
                    }
                }
            }
            do {
                ringIndex2 = ringAdvance(ringIndex2, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                Intent intent = this.mBroadcastSummaryHistory[ringIndex2];
                if (intent != null) {
                    if (!printed) {
                        if (needSep) {
                            pw.println();
                        }
                        needSep = true;
                        pw.println("  Historical broadcasts summary [" + this.mQueueName + "]:");
                        printed = true;
                    }
                    if (!dumpAll && i >= 50) {
                        pw.println("  ...");
                        ringIndex = ringIndex2;
                        break;
                    }
                    i++;
                    pw.print("  #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(intent.toShortString(false, true, true, false));
                    pw.print("    ");
                    TimeUtils.formatDuration(this.mSummaryHistoryDispatchTime[ringIndex2] - this.mSummaryHistoryEnqueueTime[ringIndex2], pw);
                    pw.print(" dispatch ");
                    TimeUtils.formatDuration(this.mSummaryHistoryFinishTime[ringIndex2] - this.mSummaryHistoryDispatchTime[ringIndex2], pw);
                    pw.println(" finish");
                    pw.print("    enq=");
                    pw.print(sdf.format(new Date(this.mSummaryHistoryEnqueueTime[ringIndex2])));
                    pw.print(" disp=");
                    pw.print(sdf.format(new Date(this.mSummaryHistoryDispatchTime[ringIndex2])));
                    pw.print(" fin=");
                    pw.println(sdf.format(new Date(this.mSummaryHistoryFinishTime[ringIndex2])));
                    bundle = intent.getExtras();
                    if (bundle != null) {
                        pw.print("    extras: ");
                        pw.println(bundle.toString());
                        continue;
                    } else {
                        continue;
                    }
                }
            } while (ringIndex2 != ringIndex);
            ringIndex = ringIndex2;
        }
        return needSep;
    }

    private void oppoSetProcReceive(boolean flag, BroadcastRecord r, ProcessRecord app) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.i(TAG, "flag  = " + flag + " app " + app + " this.mQueueName " + this.mQueueName);
        }
        if (flag) {
            if ("foreground".equals(this.mQueueName)) {
                app.foreCurReceiver = r;
            } else if ("background".equals(this.mQueueName)) {
                app.backCurReceiver = r;
            } else if ("oppoforeground".equals(this.mQueueName)) {
                app.oppoforeCurReceiver = r;
            } else if ("oppobackground".equals(this.mQueueName)) {
                app.oppobackCurReceiver = r;
            }
        } else if ("foreground".equals(this.mQueueName)) {
            app.foreCurReceiver = null;
        } else if ("background".equals(this.mQueueName)) {
            app.backCurReceiver = null;
        } else if ("oppoforeground".equals(this.mQueueName)) {
            app.oppoforeCurReceiver = null;
        } else if ("oppobackground".equals(this.mQueueName)) {
            app.oppobackCurReceiver = null;
        }
    }

    private boolean oppoProcessBroadcastFinish(ProcessRecord app, BroadcastRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.i(TAG, "app.foreCurReceiver  = " + app.foreCurReceiver + " app.backCurReceiver " + app.backCurReceiver + " this.mQueueName " + this.mQueueName);
        }
        if (app.foreCurReceiver != null && app.foreCurReceiver.queue == this && app.foreCurReceiver != r) {
            Slog.v(TAG, "finish broadcast app.foreCurReceiver = " + app.foreCurReceiver);
            logBroadcastReceiverDiscardLocked(app.foreCurReceiver);
            finishReceiverLocked(app.foreCurReceiver, app.foreCurReceiver.resultCode, app.foreCurReceiver.resultData, app.foreCurReceiver.resultExtras, app.foreCurReceiver.resultAbort, false);
            return true;
        } else if (app.backCurReceiver != null && app.backCurReceiver.queue == this && app.backCurReceiver != r) {
            Slog.v(TAG, "finish broadcast app.backReceiver = " + app.backCurReceiver);
            logBroadcastReceiverDiscardLocked(app.backCurReceiver);
            finishReceiverLocked(app.backCurReceiver, app.backCurReceiver.resultCode, app.backCurReceiver.resultData, app.backCurReceiver.resultExtras, app.backCurReceiver.resultAbort, false);
            return true;
        } else if (app.oppoforeCurReceiver != null && app.oppoforeCurReceiver.queue == this && app.oppoforeCurReceiver != r) {
            Slog.v(TAG, "finish broadcast app.oppoforeCurReceiver = " + app.oppoforeCurReceiver);
            logBroadcastReceiverDiscardLocked(app.oppoforeCurReceiver);
            finishReceiverLocked(app.oppoforeCurReceiver, app.oppoforeCurReceiver.resultCode, app.oppoforeCurReceiver.resultData, app.oppoforeCurReceiver.resultExtras, app.oppoforeCurReceiver.resultAbort, false);
            return true;
        } else if (app.oppobackCurReceiver == null || app.oppobackCurReceiver.queue != this || app.oppobackCurReceiver == r) {
            return false;
        } else {
            Slog.v(TAG, "finish broadcast app.oppobackCurReceiver = " + app.oppobackCurReceiver);
            logBroadcastReceiverDiscardLocked(app.oppobackCurReceiver);
            finishReceiverLocked(app.oppobackCurReceiver, app.oppobackCurReceiver.resultCode, app.oppobackCurReceiver.resultData, app.oppobackCurReceiver.resultExtras, app.oppobackCurReceiver.resultAbort, false);
            return true;
        }
    }
}
