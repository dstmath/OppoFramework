package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.server.coloros.OppoListManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class BroadcastQueue extends OppoBaseBroadcastQueue {
    static final int BROADCAST_INTENT_MSG = 200;
    static final int BROADCAST_TIMEOUT_MSG = 201;
    static final int MAX_BROADCAST_HISTORY = (ActivityManager.isLowRamDeviceStatic() ? 10 : 50);
    static final int MAX_BROADCAST_SUMMARY_HISTORY = (ActivityManager.isLowRamDeviceStatic() ? 25 : 300);
    private static final String TAG = "BroadcastQueue";
    /* access modifiers changed from: private */
    public static final String TAG_BROADCAST = (TAG + ActivityManagerDebugConfig.POSTFIX_BROADCAST);
    private static final String TAG_MU = "BroadcastQueue_MU";
    final BroadcastRecord[] mBroadcastHistory = new BroadcastRecord[MAX_BROADCAST_HISTORY];
    final Intent[] mBroadcastSummaryHistory;
    boolean mBroadcastsScheduled;
    final BroadcastConstants mConstants;
    final boolean mDelayBehindServices;
    final BroadcastDispatcher mDispatcher;
    final BroadcastHandler mHandler;
    int mHistoryNext = 0;
    boolean mLogLatencyMetrics;
    private int mNextToken = 0;
    final ArrayList<BroadcastRecord> mParallelBroadcasts = new ArrayList<>();
    BroadcastRecord mPendingBroadcast;
    int mPendingBroadcastRecvIndex;
    boolean mPendingBroadcastTimeoutMessage;
    final String mQueueName;
    final ActivityManagerService mService;
    final SparseIntArray mSplitRefcounts = new SparseIntArray();
    final long[] mSummaryHistoryDispatchTime;
    final long[] mSummaryHistoryEnqueueTime;
    final long[] mSummaryHistoryFinishTime;
    int mSummaryHistoryNext;

    private final class BroadcastHandler extends Handler {
        public BroadcastHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            boolean doNext;
            switch (msg.what) {
                case 200:
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        String access$000 = BroadcastQueue.TAG_BROADCAST;
                        Slog.v(access$000, "Received BROADCAST_INTENT_MSG [" + BroadcastQueue.this.mQueueName + "]");
                    }
                    BroadcastQueue.this.processNextBroadcast(true);
                    return;
                case BroadcastQueue.BROADCAST_TIMEOUT_MSG /*{ENCODED_INT: 201}*/:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastQueue.this.broadcastTimeoutLocked(true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 202:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastRecord r = (BroadcastRecord) msg.obj;
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v(BroadcastQueue.TAG, "Received BROADCAST_NEXT_MSG ,finishReceiver , broadcastRecord = " + r);
                            }
                            OppoReceiverRecord receiverRecord = BroadcastQueue.this.getOppoReceiverRecord(r);
                            if (receiverRecord != null) {
                                receiverRecord.mHasFinish = true;
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

    private final class AppNotResponding implements Runnable {
        private final String mAnnotation;
        private final ProcessRecord mApp;

        public AppNotResponding(ProcessRecord app, String annotation) {
            this.mApp = app;
            this.mAnnotation = annotation;
        }

        public void run() {
            this.mApp.appNotResponding(null, null, null, null, false, this.mAnnotation);
        }
    }

    BroadcastQueue(ActivityManagerService service, Handler handler, String name, BroadcastConstants constants, boolean allowDelayBehindServices) {
        int i = MAX_BROADCAST_SUMMARY_HISTORY;
        this.mBroadcastSummaryHistory = new Intent[i];
        this.mSummaryHistoryNext = 0;
        this.mSummaryHistoryEnqueueTime = new long[i];
        this.mSummaryHistoryDispatchTime = new long[i];
        this.mSummaryHistoryFinishTime = new long[i];
        this.mBroadcastsScheduled = false;
        this.mPendingBroadcast = null;
        this.mLogLatencyMetrics = true;
        this.mService = service;
        if (OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).hasOppoBroadcastManager()) {
            this.mHandler = new BroadcastHandler(OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).getBroadcastThread().getLooper());
        } else {
            this.mHandler = new BroadcastHandler(handler.getLooper());
        }
        this.mQueueName = name;
        this.mDelayBehindServices = allowDelayBehindServices;
        this.mConstants = constants;
        this.mDispatcher = new BroadcastDispatcher(this, this.mConstants, this.mHandler, this.mService);
        OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).instanceBroadcastThread(this);
        this.mColorQueue = service.mColorAmsEx.getColorBroadcastQueueEx(this);
    }

    /* access modifiers changed from: package-private */
    public void start(ContentResolver resolver) {
        this.mDispatcher.start();
        this.mConstants.startObserving(this.mHandler, resolver);
    }

    public String toString() {
        return this.mQueueName;
    }

    public boolean isPendingBroadcastProcessLocked(int pid) {
        String str;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            StringBuilder sb = new StringBuilder();
            sb.append(" pid = ");
            sb.append(pid);
            sb.append(" mPendingBroadcast = ");
            sb.append(this.mPendingBroadcast);
            if (this.mPendingBroadcast != null) {
                str = " curApp.pid = " + this.mPendingBroadcast.curApp.pid;
            } else {
                str = " null";
            }
            sb.append(str);
            Slog.v(TAG, sb.toString());
        }
        BroadcastRecord broadcastRecord = this.mPendingBroadcast;
        return broadcastRecord != null && broadcastRecord.curApp.pid == pid;
    }

    public void enqueueParallelBroadcastLocked(BroadcastRecord r) {
        this.mParallelBroadcasts.add(r);
        enqueueBroadcastHelper(r);
        OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).adjustQueueIfNecessary(this.mParallelBroadcasts, r);
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "mParallelBroadcasts add: " + r + " mParallelBroadcasts " + this.mParallelBroadcasts);
        }
    }

    public void enqueueOrderedBroadcastLocked(BroadcastRecord r) {
        this.mDispatcher.enqueueOrderedBroadcastLocked(r);
        enqueueBroadcastHelper(r);
    }

    private void enqueueBroadcastHelper(BroadcastRecord r) {
        r.enqueueClockTime = System.currentTimeMillis();
        if (Trace.isTagEnabled(64)) {
            Trace.asyncTraceBegin(64, createBroadcastTraceTitle(r, 0), System.identityHashCode(r));
        }
    }

    public final BroadcastRecord replaceParallelBroadcastLocked(BroadcastRecord r) {
        return replaceBroadcastLocked(this.mParallelBroadcasts, r, "PARALLEL");
    }

    public final BroadcastRecord replaceOrderedBroadcastLocked(BroadcastRecord r) {
        return this.mDispatcher.replaceBroadcastLocked(r, "ORDERED");
    }

    private BroadcastRecord replaceBroadcastLocked(ArrayList<BroadcastRecord> queue, BroadcastRecord r, String typeForLogging) {
        Intent intent = r.intent;
        int i = queue.size() - 1;
        while (i > 0) {
            BroadcastRecord old = queue.get(i);
            if (old.userId != r.userId || !intent.filterEquals(old.intent)) {
                i--;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG_BROADCAST, "***** DROPPING " + typeForLogging + " [" + this.mQueueName + "]: " + intent);
                }
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG_BROADCAST, "queue: " + queue + " r " + r + " old " + old);
                }
                queue.set(i, r);
                return old;
            }
        }
        return null;
    }

    private final void processCurBroadcastLocked(BroadcastRecord r, ProcessRecord app, boolean skipOomAdj) throws RemoteException {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            String str = TAG_BROADCAST;
            Slog.v(str, "Process cur broadcast " + r + " for app " + app);
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
            app.curReceivers.add(r);
            app.forceProcessStateUpTo(12);
            this.mService.mProcessList.updateLruProcessLocked(app, false, null);
            if (!skipOomAdj) {
                this.mService.updateOomAdjLocked("updateOomAdj_meh");
            }
            r.intent.setComponent(r.curComponent);
            boolean started = false;
            try {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    String str2 = TAG_BROADCAST;
                    Slog.v(str2, "Delivering to component " + r.curComponent + ": " + r);
                }
                this.mService.notifyPackageUse(r.intent.getComponent().getPackageName(), 3);
                if (OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).hasOppoBroadcastManager()) {
                    OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).oppoScheduleReceiver(this, new Intent(r.intent), r, this.mService, app);
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        String str3 = TAG_BROADCAST;
                        Slog.v(str3, "Process cur broadcast " + r + " DELIVERED for app " + app + " r.ordered " + r.ordered);
                    }
                    boolean isGoogleCalendar = "com.google.android.calendar.APPWIDGET_REFRESH_MODEL".equals(r.intent.getAction());
                    if (isGoogleCalendar && ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG, "Is google calendar");
                    }
                    String name = r.curComponent != null ? r.curComponent.flattenToShortString() : null;
                    if (name != null && name.contains("com.android.server.cts.errors")) {
                        setMessageDelayFlagForBroadcastRecord(r, true);
                    }
                    if (!r.ordered && (name == null || !name.contains("com.android.server.cts.errors"))) {
                        Message msg = Message.obtain();
                        msg.what = 202;
                        msg.obj = r;
                        if (isGoogleCalendar) {
                            setMessageDelayFlagForBroadcastRecord(r, true);
                            this.mHandler.sendMessageDelayed(msg, 2000);
                        } else {
                            this.mHandler.sendMessage(msg);
                        }
                    }
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        String str4 = TAG_BROADCAST;
                        Slog.v(str4, "impl AOSP Process cur broadcast " + r + " DELIVERED for app " + app + " r.ordered " + r.ordered);
                    }
                    app.thread.scheduleReceiver(new Intent(r.intent), r.curReceiver, this.mService.compatibilityInfoForPackage(r.curReceiver.applicationInfo), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.userId, app.getReportedProcState());
                }
                started = true;
            } finally {
                if (!started) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        String str5 = TAG_BROADCAST;
                        Slog.v(str5, "Process cur broadcast " + r + ": NOT STARTED!");
                    }
                    r.receiver = null;
                    r.curApp = null;
                    app.curReceivers.remove(r);
                }
            }
        }
    }

    public boolean sendPendingBroadcastsLocked(ProcessRecord app) {
        BroadcastRecord br = this.mPendingBroadcast;
        if (br == null || br.curApp.pid <= 0 || br.curApp.pid != app.pid) {
            return false;
        }
        if (br.curApp != app) {
            Slog.e(TAG, "App mismatch when sending pending broadcast to " + app.processName + ", intended target is " + br.curApp.processName);
            return false;
        }
        try {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "mQueueName " + this.mQueueName + " sendPendingBroadcastsLocked mPendingBroadcast = " + this.mPendingBroadcast + " app = " + app);
            }
            this.mPendingBroadcast = null;
            processCurBroadcastLocked(br, app, false);
            return true;
        } catch (Exception e) {
            Slog.w(TAG, "Exception in new application, exception", e);
            if (br.curComponent != null) {
                Slog.w(TAG, "Exception in new application when starting receiver " + br.curComponent.flattenToShortString());
            }
            logBroadcastReceiverDiscardLocked(br);
            finishReceiverLocked(br, br.resultCode, br.resultData, br.resultExtras, br.resultAbort, false);
            scheduleBroadcastsLocked();
            br.state = 0;
            throw new RuntimeException(e.getMessage());
        }
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
        BroadcastRecord broadcastRecord;
        OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).skipCurrentReceiverLocked(this, app);
        BroadcastRecord r = null;
        BroadcastRecord curActive = this.mDispatcher.getActiveBroadcastLocked();
        if (curActive != null && curActive.curApp == app) {
            r = curActive;
        }
        if (r == null && (broadcastRecord = this.mPendingBroadcast) != null && broadcastRecord.curApp == app) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                String str = TAG_BROADCAST;
                Slog.v(str, "[" + this.mQueueName + "] skip & discard pending app " + r);
            }
            r = this.mPendingBroadcast;
        }
        if (r != null) {
            skipReceiverLocked(r);
        }
    }

    private void skipReceiverLocked(BroadcastRecord r) {
        logBroadcastReceiverDiscardLocked(r);
        finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
        scheduleBroadcastsLocked();
    }

    public void scheduleBroadcastsLocked() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            String str = TAG_BROADCAST;
            Slog.v(str, "Schedule broadcasts [" + this.mQueueName + "]: current=" + this.mBroadcastsScheduled);
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
        BroadcastHandler broadcastHandler = this.mHandler;
        broadcastHandler.sendMessage(broadcastHandler.obtainMessage(200, this));
        this.mBroadcastsScheduled = true;
        this.mLastTimeForDispatchMsg = System.currentTimeMillis();
    }

    public BroadcastRecord getMatchingOrderedReceiver(IBinder receiver) {
        BroadcastRecord br = this.mDispatcher.getActiveBroadcastLocked();
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            StringBuilder sb = new StringBuilder();
            sb.append("mQueueName : ");
            sb.append(this.mQueueName);
            sb.append(" br = ");
            sb.append(br);
            sb.append(" br.receiver ");
            sb.append(br != null ? br.receiver : null);
            Slog.v(TAG, sb.toString());
        }
        if (br == null || br.receiver != receiver) {
            return null;
        }
        return br;
    }

    private int nextSplitTokenLocked() {
        int next = this.mNextToken + 1;
        if (next <= 0) {
            next = 1;
        }
        this.mNextToken = next;
        return next;
    }

    private void postActivityStartTokenRemoval(ProcessRecord app, BroadcastRecord r) {
        String msgToken = (app.toShortString() + r.toString()).intern();
        this.mHandler.removeCallbacksAndMessages(msgToken);
        this.mHandler.postAtTime(new Runnable(r) {
            /* class com.android.server.am.$$Lambda$BroadcastQueue$u5X4lnAPSSN1Kjb_BebqIicVqK4 */
            private final /* synthetic */ BroadcastRecord f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ProcessRecord.this.removeAllowBackgroundActivityStartsToken(this.f$1);
            }
        }, msgToken, r.receiverTime + this.mConstants.ALLOW_BG_ACTIVITY_START_TIMEOUT);
    }

    public boolean finishReceiverLocked(BroadcastRecord r, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort, boolean waitForServices) {
        boolean z;
        ActivityInfo nextReceiver;
        BroadcastHandler broadcastHandler = this.mHandler;
        if (broadcastHandler != null && broadcastHandler.hasMessages(202, r)) {
            this.mHandler.removeMessages(202, r);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.i(TAG, "finishReceiverLocked : mQueueName = " + this.mQueueName + ", r= " + r.toString() + ", receiver=" + r.curReceiver);
                StringBuilder sb = new StringBuilder();
                sb.append("finishReceiverLocked : mQueueName = ");
                sb.append(this.mQueueName);
                sb.append(", mPendingBroadcast = ");
                sb.append(this.mPendingBroadcast);
                Slog.i(TAG, sb.toString());
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT && r != null) {
            Slog.i(TAG, "finishReceiverLocked : mQueueName = " + this.mQueueName + ",r = " + r.toString() + ", r.state = " + r.state + StringUtils.SPACE + Debug.getCallers(4));
        }
        int state = r.state;
        ActivityInfo receiver = r.curReceiver;
        long elapsed = SystemClock.uptimeMillis() - r.receiverTime;
        r.state = 0;
        if (state == 0) {
            Slog.w(TAG_BROADCAST, "finishReceiver [" + this.mQueueName + "] called but state is IDLE");
        }
        if (r.allowBackgroundActivityStarts && r.curApp != null) {
            if (elapsed > this.mConstants.ALLOW_BG_ACTIVITY_START_TIMEOUT) {
                r.curApp.removeAllowBackgroundActivityStartsToken(r);
            } else {
                postActivityStartTokenRemoval(r.curApp, r);
            }
        }
        if (r.nextReceiver > 0) {
            r.duration[r.nextReceiver - 1] = elapsed;
        }
        if (!r.timeoutExempt) {
            if (this.mConstants.SLOW_TIME > 0 && elapsed > this.mConstants.SLOW_TIME) {
                if (r.curApp != null && !UserHandle.isCore(r.curApp.uid)) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                        Slog.i(TAG_BROADCAST, "Broadcast receiver " + (r.nextReceiver - 1) + " was slow: " + receiver + " br=" + r);
                    }
                    if (r.curApp != null) {
                        this.mDispatcher.startDeferring(r.curApp.uid);
                    } else {
                        Slog.d(TAG_BROADCAST, "finish receiver curApp is null? " + r);
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                    Slog.i(TAG_BROADCAST, "Core uid " + r.curApp.uid + " receiver was slow but not deferring: " + receiver + " br=" + r);
                }
            }
        } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
            Slog.i(TAG_BROADCAST, "Finished broadcast " + r.intent.getAction() + " is exempt from deferral policy");
        }
        r.receiver = null;
        r.intent.setComponent(null);
        if (r.curApp != null && r.curApp.curReceivers.contains(r)) {
            r.curApp.curReceivers.remove(r);
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
        if (!resultAbort || (r.intent.getFlags() & 134217728) != 0) {
            r.resultAbort = false;
        } else {
            r.resultAbort = resultAbort;
        }
        if (!waitForServices || r.curComponent == null || !r.queue.mDelayBehindServices) {
            z = false;
        } else if (r.queue.mDispatcher.getActiveBroadcastLocked() == r) {
            if (r.nextReceiver < r.receivers.size()) {
                Object obj = r.receivers.get(r.nextReceiver);
                nextReceiver = obj instanceof ActivityInfo ? (ActivityInfo) obj : null;
            } else {
                nextReceiver = null;
            }
            if (receiver != null && nextReceiver != null && receiver.applicationInfo.uid == nextReceiver.applicationInfo.uid && receiver.processName.equals(nextReceiver.processName)) {
                z = false;
            } else if (this.mService.mServices.hasBackgroundServicesLocked(r.userId)) {
                Slog.i(TAG, "Delay finish: " + r.curComponent.flattenToShortString());
                r.state = 4;
                return false;
            } else {
                z = false;
            }
        } else {
            z = false;
        }
        r.curComponent = null;
        if (state == 1 || state == 3) {
            return true;
        }
        return z;
    }

    public void backgroundServicesFinishedLocked(int userId) {
        BroadcastRecord br = this.mDispatcher.getActiveBroadcastLocked();
        if (br != null && br.userId == userId && br.state == 4) {
            Slog.i(TAG, "Resuming delayed broadcast");
            br.curComponent = null;
            br.state = 0;
            processNextBroadcast(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
        if (app == null) {
            receiver.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
        } else if (app.thread != null) {
            try {
                app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode, data, extras, ordered, sticky, sendingUser, app.getReportedProcState());
            } catch (RemoteException ex) {
                synchronized (this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    Slog.w(TAG, "Can't deliver broadcast to " + app.processName + " (pid " + app.pid + "). Crashing it.");
                    app.scheduleCrash("can't deliver broadcast");
                    Slog.w(TAG, "can't deliver broadcast, let's cleanup...");
                    if (Process.getUidForPid(app.pid) == app.uid) {
                        this.mService.appDiedLocked(app);
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw ex;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception deliver broadcast to" + app, e);
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        } else {
            throw new RemoteException("app.thread must not be null");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x04d9  */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x04de  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x03c9  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x03cb  */
    private void deliverToRegisteredReceiverLocked(BroadcastRecord r, BroadcastFilter filter, boolean ordered, int index) {
        boolean skip;
        boolean skip2;
        boolean skip3;
        boolean skip4;
        boolean skip5;
        boolean skip6 = false;
        if (!this.mService.validateAssociationAllowedLocked(r.callerPackage, r.callingUid, filter.packageName, filter.owningUid)) {
            Slog.w(TAG, "Association not allowed: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") to " + filter.packageName + " through " + filter);
            skip6 = true;
        }
        if (!skip6 && !this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, filter.receiverList.uid)) {
            Slog.w(TAG, "Firewall blocked: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") to " + filter.packageName + " through " + filter);
            skip6 = true;
        }
        boolean z = true;
        if (filter.requiredPermission != null) {
            ActivityManagerService activityManagerService = this.mService;
            if (ActivityManagerService.checkComponentPermission(filter.requiredPermission, r.callingPid, r.callingUid, -1, true) != 0) {
                Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") requires " + filter.requiredPermission + " due to registered receiver " + filter);
                skip6 = true;
            } else {
                int opCode = AppOpsManager.permissionToOpCode(filter.requiredPermission);
                if (!(opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) == 0)) {
                    Slog.w(TAG, "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") requires appop " + AppOpsManager.permissionToOp(filter.requiredPermission) + " due to registered receiver " + filter);
                    skip6 = true;
                }
            }
        }
        if (skip6 || r.requiredPermissions == null || r.requiredPermissions.length <= 0) {
            skip5 = skip6;
        } else {
            int i = 0;
            while (i < r.requiredPermissions.length) {
                String requiredPermission = r.requiredPermissions[i];
                ActivityManagerService activityManagerService2 = this.mService;
                if (ActivityManagerService.checkComponentPermission(requiredPermission, filter.receiverList.pid, filter.receiverList.uid, -1, z) != 0) {
                    Slog.w(TAG, "Permission Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip = true;
                    break;
                }
                int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                if (appOp != -1 && appOp != r.appOp) {
                    if (this.mService.mAppOpsService.noteOperation(appOp, filter.receiverList.uid, filter.packageName) != 0) {
                        Slog.w(TAG, "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                        skip = true;
                        break;
                    }
                }
                i++;
                skip6 = skip6;
                z = true;
            }
            skip5 = skip6;
        }
        skip = skip5;
        if (skip) {
            skip4 = skip;
        } else if (r.requiredPermissions == null || r.requiredPermissions.length == 0) {
            ActivityManagerService activityManagerService3 = this.mService;
            skip4 = skip;
            if (ActivityManagerService.checkComponentPermission(null, filter.receiverList.pid, filter.receiverList.uid, -1, true) != 0) {
                Slog.w(TAG, "Permission Denial: security check failed when receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                skip2 = true;
                if (!(skip2 || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, filter.receiverList.uid, filter.packageName) == 0)) {
                    Slog.w(TAG, "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip2 = true;
                }
                if (!skip2 && (filter.receiverList.app == null || filter.receiverList.app.killed || filter.receiverList.app.isCrashing())) {
                    Slog.w(TAG, "Skipping deliver [" + this.mQueueName + "] " + r + " to " + filter.receiverList + ": process gone or crashing");
                    skip2 = true;
                }
                boolean visibleToInstantApps = (r.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) == 0;
                if (!skip2 && !visibleToInstantApps && filter.instantApp && filter.receiverList.uid != r.callingUid) {
                    Slog.w(TAG, "Instant App Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") due to sender " + r.callerPackage + " (uid " + r.callingUid + ") not specifying FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS");
                    skip2 = true;
                }
                if (!skip2 && !filter.visibleToInstantApp && r.callerInstantApp && filter.receiverList.uid != r.callingUid) {
                    Slog.w(TAG, "Instant App Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires receiver be visible to instant apps due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip2 = true;
                }
                if (!skip2 && filter.receiverList.app != null && OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).skipSpecialBroadcast(filter.receiverList.app, filter.packageName, r.intent, filter.receiverList.app.processName, filter.receiverList.app.uid, filter.receiverList.app.info)) {
                    skip2 = true;
                }
                if (!skip2 || OppoFeatureCache.get(IColorHansManager.DEFAULT).hansBroadcastIfNeeded(r, filter)) {
                    skip3 = skip2;
                } else {
                    skip3 = true;
                }
                if (!skip3) {
                    r.delivery[index] = 2;
                    return;
                } else if (!requestStartTargetPermissionsReviewIfNeededLocked(r, filter.packageName, filter.owningUserId)) {
                    r.delivery[index] = 2;
                    return;
                } else {
                    r.delivery[index] = 1;
                    if (ordered) {
                        r.receiver = filter.receiverList.receiver.asBinder();
                        r.curFilter = filter;
                        filter.receiverList.curBroadcast = r;
                        r.state = 2;
                        if (filter.receiverList.app != null) {
                            r.curApp = filter.receiverList.app;
                            filter.receiverList.app.curReceivers.add(r);
                            this.mService.updateOomAdjLocked(r.curApp, true, "updateOomAdj_startReceiver");
                        }
                    }
                    try {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                            Slog.i(TAG_BROADCAST, "Delivering to " + filter + " : " + r);
                        }
                        if (filter.receiverList.app == null || !filter.receiverList.app.inFullBackup) {
                            r.receiverTime = SystemClock.uptimeMillis();
                            maybeAddAllowBackgroundActivityStartsToken(filter.receiverList.app, r);
                            performReceiveLocked(filter.receiverList.app, filter.receiverList.receiver, new Intent(r.intent), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.initialSticky, r.userId);
                            if (r.allowBackgroundActivityStarts && !r.ordered) {
                                postActivityStartTokenRemoval(filter.receiverList.app, r);
                            }
                        } else if (ordered) {
                            skipReceiverLocked(r);
                        }
                        if (ordered) {
                            r.state = 3;
                            return;
                        }
                        return;
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Failure sending broadcast " + r.intent + " to " + filter + " , " + filter.receiverList.app, e);
                        StringBuilder sb = new StringBuilder();
                        sb.append("Failure sending broadcast ");
                        sb.append(r.intent);
                        Slog.w(TAG, sb.toString(), e);
                        if (filter.receiverList.app != null) {
                            filter.receiverList.app.removeAllowBackgroundActivityStartsToken(r);
                            if (ordered) {
                                filter.receiverList.app.curReceivers.remove(r);
                            }
                        }
                        if (ordered) {
                            r.receiver = null;
                            r.curFilter = null;
                            filter.receiverList.curBroadcast = null;
                            return;
                        }
                        return;
                    }
                }
            }
        } else {
            skip4 = skip;
        }
        skip2 = skip4;
        Slog.w(TAG, "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
        skip2 = true;
        Slog.w(TAG, "Skipping deliver [" + this.mQueueName + "] " + r + " to " + filter.receiverList + ": process gone or crashing");
        skip2 = true;
        if ((r.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) == 0) {
        }
        Slog.w(TAG, "Instant App Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") due to sender " + r.callerPackage + " (uid " + r.callingUid + ") not specifying FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS");
        skip2 = true;
        Slog.w(TAG, "Instant App Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires receiver be visible to instant apps due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
        skip2 = true;
        skip2 = true;
        if (!skip2) {
        }
        skip3 = skip2;
        if (!skip3) {
        }
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(BroadcastRecord receiverRecord, String receivingPackageName, final int receivingUserId) {
        boolean callerForeground;
        if (!this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(receivingPackageName, receivingUserId)) {
            return true;
        }
        if (receiverRecord.callerApp != null) {
            callerForeground = receiverRecord.callerApp.setSchedGroup != 0;
        } else {
            callerForeground = true;
        }
        if (!callerForeground || receiverRecord.intent.getComponent() == null) {
            Slog.w(TAG, "u" + receivingUserId + " Receiving a broadcast in package" + receivingPackageName + " requires a permissions review");
        } else {
            IIntentSender target = this.mService.mPendingIntentController.getIntentSender(1, receiverRecord.callerPackage, receiverRecord.callingUid, receiverRecord.userId, null, null, 0, new Intent[]{receiverRecord.intent}, new String[]{receiverRecord.intent.resolveType(this.mService.mContext.getContentResolver())}, 1409286144, null);
            final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(411041792);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", receivingPackageName);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i(TAG, "u" + receivingUserId + " Launching permission review for package " + receivingPackageName);
            }
            this.mHandler.post(new Runnable() {
                /* class com.android.server.am.BroadcastQueue.AnonymousClass1 */

                public void run() {
                    BroadcastQueue.this.mService.mContext.startActivityAsUser(intent, new UserHandle(receivingUserId));
                }
            });
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final void scheduleTempWhitelistLocked(int uid, long duration, BroadcastRecord r) {
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
            r.intent.getComponent().appendShortString(b);
        } else if (r.intent.getData() != null) {
            b.append(r.intent.getData());
        }
        this.mService.tempWhitelistUidLocked(uid, duration, b.toString());
    }

    /* access modifiers changed from: package-private */
    public final boolean isSignaturePerm(String[] perms) {
        if (perms == null) {
            return false;
        }
        IPackageManager pm = AppGlobals.getPackageManager();
        int i = perms.length - 1;
        while (i >= 0) {
            try {
                PermissionInfo pi = pm.getPermissionInfo(perms[i], PackageManagerService.PLATFORM_PACKAGE_NAME, 0);
                if (pi == null || (pi.protectionLevel & 31) != 2) {
                    return false;
                }
                i--;
            } catch (RemoteException e) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public final void processNextBroadcast(boolean fromMsg) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                processNextBroadcastLocked(fromMsg, false);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX DEBUG: Additional 2 move instruction added to help type inference */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v0, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v1, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v6, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v261, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v263, resolved type: int} */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:504:0x128e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:508:0x12de, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:509:0x12df, code lost:
        r10 = r6;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x044c  */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x0691 A[SYNTHETIC, Splitter:B:191:0x0691] */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x0751  */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x076c  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x077a  */
    /* JADX WARNING: Removed duplicated region for block: B:504:0x128e A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:491:0x1267] */
    /* JADX WARNING: Removed duplicated region for block: B:514:0x1306  */
    /* JADX WARNING: Removed duplicated region for block: B:517:0x134a  */
    /* JADX WARNING: Removed duplicated region for block: B:519:0x137f  */
    /* JADX WARNING: Removed duplicated region for block: B:543:0x14df A[LOOP:2: B:69:0x024b->B:543:0x14df, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:551:0x041f A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:553:0x07d0 A[SYNTHETIC] */
    public final void processNextBroadcastLocked(boolean fromMsg, boolean skipOomAdj) {
        int i;
        long firstTime;
        int parallelSize;
        boolean forceReceive;
        long allParallelCost;
        long j;
        boolean looped;
        BroadcastRecord r;
        boolean skip;
        boolean isSingleton;
        boolean skip2;
        BroadcastOptions brOptions;
        int allowed;
        boolean skip3;
        int opCode;
        BroadcastRecord r2;
        boolean sendResult;
        IIntentReceiver iIntentReceiver;
        BroadcastRecord defer;
        ProcessRecord proc;
        ProcessRecord processRecord;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG_BROADCAST, "processNextBroadcast [" + this.mQueueName + "]: " + this.mParallelBroadcasts.size() + " parallel broadcasts; " + this.mDispatcher.describeStateLocked());
        }
        this.mService.updateCpuStats();
        boolean z = false;
        if (fromMsg) {
            this.mBroadcastsScheduled = false;
        }
        long firstTime2 = SystemClock.uptimeMillis();
        int parallelSize2 = this.mParallelBroadcasts.size();
        while (true) {
            i = 1;
            if (this.mParallelBroadcasts.size() <= 0) {
                break;
            }
            BroadcastRecord r3 = this.mParallelBroadcasts.remove(0);
            r3.dispatchTime = SystemClock.uptimeMillis();
            r3.dispatchClockTime = System.currentTimeMillis();
            if (Trace.isTagEnabled(64)) {
                Trace.asyncTraceEnd(64, createBroadcastTraceTitle(r3, 0), System.identityHashCode(r3));
                Trace.asyncTraceBegin(64, createBroadcastTraceTitle(r3, 1), System.identityHashCode(r3));
            }
            int N = r3.receivers.size();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                Slog.v(TAG_BROADCAST, "Processing parallel broadcast [" + this.mQueueName + "] " + r3);
            }
            OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).adjustParallelBroadcastReceiversQueue(r3);
            boolean isBootCompleteBrodcast = false;
            isBootCompleteBrodcast = false;
            if (ActivityManagerService.DEBUG_COLOROS_AMS && "oppo.intent.action.BOOT_COMPLETED".equals(r3.intent.getAction())) {
                isBootCompleteBrodcast = true;
            }
            for (int i2 = 0; i2 < N; i2++) {
                Object target = r3.receivers.get(i2);
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST || isBootCompleteBrodcast) {
                    Slog.v(TAG_BROADCAST, "Delivering non-ordered on [" + this.mQueueName + "] to registered " + target + ": " + r3);
                }
                deliverToRegisteredReceiverLocked(r3, (BroadcastFilter) target, false, i2);
            }
            long costTime = SystemClock.uptimeMillis() - r3.dispatchTime;
            addBroadcastToHistoryLocked(r3);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT || costTime > 1000) {
                Slog.v(TAG_BROADCAST, "Done with parallel broadcast [" + this.mQueueName + "] " + r3 + ", cost=" + costTime + "ms, total=" + N);
            }
        }
        long allParallelCost2 = SystemClock.uptimeMillis() - firstTime2;
        if (allParallelCost2 > ((long) this.mAllowDebugTime)) {
            Slog.v(TAG_BROADCAST, "Done with all parallel broadcast [" + this.mQueueName + "] , cost=" + allParallelCost2 + "ms, total= " + parallelSize2);
        }
        if (this.mPendingBroadcast != null) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                Slog.v(TAG_BROADCAST, "processNextBroadcast [" + this.mQueueName + "]: waiting for " + this.mPendingBroadcast.curApp);
            }
            if (this.mPendingBroadcast.curApp.pid > 0) {
                synchronized (this.mService.mPidsSelfLocked) {
                    ProcessRecord proc2 = this.mService.mPidsSelfLocked.get(this.mPendingBroadcast.curApp.pid);
                    if (proc2 != null) {
                        if (!proc2.isCrashing()) {
                            processRecord = null;
                            proc = processRecord;
                        }
                    }
                    processRecord = 1;
                    proc = processRecord;
                }
            } else {
                ProcessRecord proc3 = (ProcessRecord) this.mService.mProcessList.mProcessNames.get(this.mPendingBroadcast.curApp.processName, this.mPendingBroadcast.curApp.uid);
                proc = (proc3 == null || !proc3.pendingStart) ? 1 : null;
            }
            if (proc != null) {
                Slog.w(TAG, "pending app  [" + this.mQueueName + "]" + this.mPendingBroadcast.curApp + " died before responding to broadcast");
                BroadcastRecord broadcastRecord = this.mPendingBroadcast;
                broadcastRecord.state = 0;
                broadcastRecord.nextReceiver = this.mPendingBroadcastRecvIndex;
                this.mPendingBroadcast = null;
            } else {
                return;
            }
        }
        boolean looped2 = false;
        while (true) {
            long now = SystemClock.uptimeMillis();
            BroadcastRecord r4 = this.mDispatcher.getNextBroadcastLocked(now);
            if (r4 == null) {
                this.mDispatcher.scheduleDeferralCheckLocked(z);
                this.mService.scheduleAppGcsLocked();
                if (looped2) {
                    this.mService.updateOomAdjLocked("updateOomAdj_startReceiver");
                }
                if (this.mService.mUserController.mBootCompleted && this.mLogLatencyMetrics) {
                    this.mLogLatencyMetrics = z;
                }
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG, "No more broadcasts pending, so all done!");
                    return;
                }
                return;
            }
            int numReceivers = r4.receivers != null ? r4.receivers.size() : z;
            OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).adjustOrderedBroadcastReceiversQueue(r4, numReceivers);
            if (!this.mService.mProcessesReady || r4.timeoutExempt || r4.dispatchTime <= 0) {
                firstTime = firstTime2;
                parallelSize = parallelSize2;
            } else {
                ArrayList<BroadcastRecord> orderedBroadcasts = this.mDispatcher.getOrderedBroadcasts();
                if (numReceivers > 0) {
                    if (!ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                        if (!ActivityManagerDebugConfig.DEBUG_AMS) {
                            firstTime = firstTime2;
                        } else if (now - r4.receiverTime <= 1000 && now - r4.dispatchTime <= 1500 && orderedBroadcasts.size() <= 10) {
                            firstTime = firstTime2;
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("Hung broadcast 2[");
                    sb.append(this.mQueueName);
                    sb.append("] print the time : now=");
                    sb.append(now);
                    sb.append(" enqueueClockTime=");
                    firstTime = firstTime2;
                    sb.append(r4.enqueueClockTime);
                    sb.append(" dispatchTime=");
                    sb.append(r4.dispatchTime);
                    sb.append(" startTime=");
                    sb.append(r4.receiverTime);
                    sb.append(" use time = ");
                    sb.append(now - r4.dispatchTime);
                    sb.append("  siganl use time = ");
                    sb.append(now - r4.receiverTime);
                    sb.append(" intent=");
                    sb.append(r4.intent);
                    sb.append(" numReceivers = ");
                    sb.append(numReceivers);
                    sb.append(" nextReceiver index = ");
                    sb.append(r4.nextReceiver);
                    sb.append(" Receiverinfo = ");
                    sb.append(r4.receivers.get(r4.nextReceiver - i >= 0 ? r4.nextReceiver - i : 0));
                    sb.append(" state = ");
                    sb.append(r4.state);
                    sb.append(StringUtils.SPACE);
                    sb.append(r4);
                    sb.append(" orderedBroadcasts.size() ");
                    sb.append(orderedBroadcasts.size());
                    Slog.d(TAG, sb.toString());
                } else {
                    firstTime = firstTime2;
                }
                if (numReceivers > 0) {
                    parallelSize = parallelSize2;
                    if (now > r4.dispatchTime + (this.mConstants.TIMEOUT * 2 * ((long) numReceivers)) && !this.mService.mAnrManager.isAnrDeferrable()) {
                        Slog.w(TAG, "Hung broadcast [" + this.mQueueName + "] discarded after timeout failure: now=" + now + " dispatchTime=" + r4.dispatchTime + " startTime=" + r4.receiverTime + " intent=" + r4.intent + " numReceivers=" + numReceivers + " nextReceiver=" + r4.nextReceiver + " state=" + r4.state);
                        broadcastTimeoutLocked(false);
                        r4.state = 0;
                        forceReceive = true;
                        if (r4.state != 0) {
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v(TAG, " r.nextReceiver " + r4.nextReceiver + " r.resultTo " + r4.resultTo + " r " + r4);
                            }
                            if (r4.receivers == null || r4.nextReceiver >= numReceivers || r4.resultAbort || forceReceive) {
                                if (r4.resultTo != null) {
                                    if (r4.splitToken != 0) {
                                        int newCount = this.mSplitRefcounts.get(r4.splitToken) - i;
                                        if (newCount == 0) {
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                                Slog.i(TAG_BROADCAST, "Sending broadcast completion for split token " + r4.splitToken + " : " + r4.intent.getAction());
                                            }
                                            this.mSplitRefcounts.delete(r4.splitToken);
                                        } else {
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                                Slog.i(TAG_BROADCAST, "Result refcount now " + newCount + " for split token " + r4.splitToken + " : " + r4.intent.getAction() + " - not sending completion yet");
                                            }
                                            this.mSplitRefcounts.put(r4.splitToken, newCount);
                                            sendResult = false;
                                            if (!sendResult) {
                                                try {
                                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                        try {
                                                            Slog.i(TAG_BROADCAST, "Finishing broadcast [" + this.mQueueName + "] " + r4.intent.getAction() + " app=" + r4.callerApp);
                                                        } catch (RemoteException e) {
                                                            e = e;
                                                            r2 = r4;
                                                            allParallelCost = allParallelCost2;
                                                            iIntentReceiver = null;
                                                            j = 64;
                                                        }
                                                    }
                                                    try {
                                                        allParallelCost = allParallelCost2;
                                                        iIntentReceiver = null;
                                                        j = 64;
                                                        j = 64;
                                                        j = 64;
                                                    } catch (RemoteException e2) {
                                                        e = e2;
                                                        r2 = r4;
                                                        allParallelCost = allParallelCost2;
                                                        iIntentReceiver = null;
                                                        j = 64;
                                                        r2.resultTo = iIntentReceiver;
                                                        Slog.w(TAG, "Failure [" + this.mQueueName + "] sending broadcast result of " + r2.intent, e);
                                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                        }
                                                        cancelBroadcastTimeoutLocked();
                                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                                                        }
                                                        addBroadcastToHistoryLocked(r2);
                                                        this.mService.addBroadcastStatLocked(r2.intent.getAction(), r2.callerPackage, r2.manifestCount, r2.manifestSkipCount, r2.finishTime - r2.dispatchTime);
                                                        this.mDispatcher.retireBroadcastLocked(r2);
                                                        r = null;
                                                        looped = true;
                                                        if (r == null) {
                                                        }
                                                    }
                                                    try {
                                                        performReceiveLocked(r4.callerApp, r4.resultTo, new Intent(r4.intent), r4.resultCode, r4.resultData, r4.resultExtras, false, false, r4.userId);
                                                        r2 = r4;
                                                        try {
                                                            r2.resultTo = null;
                                                        } catch (RemoteException e3) {
                                                            e = e3;
                                                        }
                                                    } catch (RemoteException e4) {
                                                        e = e4;
                                                        r2 = r4;
                                                        r2.resultTo = iIntentReceiver;
                                                        Slog.w(TAG, "Failure [" + this.mQueueName + "] sending broadcast result of " + r2.intent, e);
                                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                        }
                                                        cancelBroadcastTimeoutLocked();
                                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                                                        }
                                                        addBroadcastToHistoryLocked(r2);
                                                        this.mService.addBroadcastStatLocked(r2.intent.getAction(), r2.callerPackage, r2.manifestCount, r2.manifestSkipCount, r2.finishTime - r2.dispatchTime);
                                                        this.mDispatcher.retireBroadcastLocked(r2);
                                                        r = null;
                                                        looped = true;
                                                        if (r == null) {
                                                        }
                                                    }
                                                } catch (RemoteException e5) {
                                                    e = e5;
                                                    r2 = r4;
                                                    allParallelCost = allParallelCost2;
                                                    iIntentReceiver = null;
                                                    j = 64;
                                                    r2.resultTo = iIntentReceiver;
                                                    Slog.w(TAG, "Failure [" + this.mQueueName + "] sending broadcast result of " + r2.intent, e);
                                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                    }
                                                    cancelBroadcastTimeoutLocked();
                                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                                                    }
                                                    addBroadcastToHistoryLocked(r2);
                                                    this.mService.addBroadcastStatLocked(r2.intent.getAction(), r2.callerPackage, r2.manifestCount, r2.manifestSkipCount, r2.finishTime - r2.dispatchTime);
                                                    this.mDispatcher.retireBroadcastLocked(r2);
                                                    r = null;
                                                    looped = true;
                                                    if (r == null) {
                                                    }
                                                }
                                            } else {
                                                r2 = r4;
                                                allParallelCost = allParallelCost2;
                                                j = 64;
                                            }
                                        }
                                    }
                                    sendResult = true;
                                    if (!sendResult) {
                                    }
                                } else {
                                    r2 = r4;
                                    allParallelCost = allParallelCost2;
                                    j = 64;
                                }
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                    Slog.v(TAG_BROADCAST, "Cancelling BROADCAST_TIMEOUT_MSG");
                                }
                                cancelBroadcastTimeoutLocked();
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                                    Slog.v(TAG_BROADCAST, "Finished with ordered broadcast " + r2);
                                }
                                addBroadcastToHistoryLocked(r2);
                                if (r2.intent.getComponent() == null && r2.intent.getPackage() == null && (r2.intent.getFlags() & 1073741824) == 0) {
                                    this.mService.addBroadcastStatLocked(r2.intent.getAction(), r2.callerPackage, r2.manifestCount, r2.manifestSkipCount, r2.finishTime - r2.dispatchTime);
                                }
                                this.mDispatcher.retireBroadcastLocked(r2);
                                r = null;
                                looped = true;
                            } else {
                                if (!r4.deferred) {
                                    int receiverUid = r4.getReceiverUid(r4.receivers.get(r4.nextReceiver));
                                    if (this.mDispatcher.isDeferringLocked(receiverUid)) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                            Slog.i(TAG_BROADCAST, "Next receiver in " + r4 + " uid " + receiverUid + " at " + r4.nextReceiver + " is under deferral");
                                        }
                                        if (r4.nextReceiver + i == numReceivers) {
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                                Slog.i(TAG_BROADCAST, "Sole receiver of " + r4 + " is under deferral; setting aside and proceeding");
                                            }
                                            defer = r4;
                                            this.mDispatcher.retireBroadcastLocked(r4);
                                        } else {
                                            defer = r4.splitRecipientsLocked(receiverUid, r4.nextReceiver);
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                                Slog.i(TAG_BROADCAST, "Post split:");
                                                Slog.i(TAG_BROADCAST, "Original broadcast receivers:");
                                                for (int i3 = 0; i3 < r4.receivers.size(); i3++) {
                                                    Slog.i(TAG_BROADCAST, "  " + r4.receivers.get(i3));
                                                }
                                                Slog.i(TAG_BROADCAST, "Split receivers:");
                                                for (int i4 = 0; i4 < defer.receivers.size(); i4++) {
                                                    Slog.i(TAG_BROADCAST, "  " + defer.receivers.get(i4));
                                                }
                                            }
                                            if (r4.resultTo != null) {
                                                int token = r4.splitToken;
                                                if (token == 0) {
                                                    int nextSplitTokenLocked = nextSplitTokenLocked();
                                                    defer.splitToken = nextSplitTokenLocked;
                                                    r4.splitToken = nextSplitTokenLocked;
                                                    this.mSplitRefcounts.put(r4.splitToken, 2);
                                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                                        Slog.i(TAG_BROADCAST, "Broadcast needs split refcount; using new token " + r4.splitToken);
                                                    }
                                                } else {
                                                    int curCount = this.mSplitRefcounts.get(token);
                                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL && curCount == 0) {
                                                        Slog.wtf(TAG_BROADCAST, "Split refcount is zero with token for " + r4);
                                                    }
                                                    this.mSplitRefcounts.put(token, curCount + 1);
                                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                                        Slog.i(TAG_BROADCAST, "New split count for token " + token + " is " + (curCount + 1));
                                                    }
                                                }
                                            }
                                        }
                                        this.mDispatcher.addDeferredBroadcast(receiverUid, defer);
                                        r = null;
                                        allParallelCost = allParallelCost2;
                                        looped = true;
                                        j = 64;
                                    }
                                }
                                r = r4;
                                allParallelCost = allParallelCost2;
                                j = 64;
                                looped = looped2;
                            }
                            if (r == null) {
                                int recIdx = r.nextReceiver;
                                r.nextReceiver = recIdx + 1;
                                r.receiverTime = SystemClock.uptimeMillis();
                                if (recIdx == 0) {
                                    r.dispatchTime = r.receiverTime;
                                    r.dispatchClockTime = System.currentTimeMillis();
                                    if (this.mLogLatencyMetrics) {
                                        StatsLog.write(142, r.dispatchClockTime - r.enqueueClockTime);
                                    }
                                    if (Trace.isTagEnabled(j)) {
                                        Trace.asyncTraceEnd(j, createBroadcastTraceTitle(r, 0), System.identityHashCode(r));
                                        Trace.asyncTraceBegin(j, createBroadcastTraceTitle(r, 1), System.identityHashCode(r));
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                                        Slog.v(TAG_BROADCAST, "Processing ordered broadcast [" + this.mQueueName + "] " + r);
                                    }
                                }
                                if (!this.mPendingBroadcastTimeoutMessage) {
                                    long timeoutTime = r.receiverTime + this.mConstants.TIMEOUT;
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Submitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r + " at " + timeoutTime);
                                    }
                                    setBroadcastTimeoutLocked(timeoutTime);
                                }
                                BroadcastOptions brOptions2 = r.options;
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
                                        return;
                                    }
                                    if (filter.receiverList != null) {
                                        maybeAddAllowBackgroundActivityStartsToken(filter.receiverList.app, r);
                                    }
                                    if (brOptions2 != null && brOptions2.getTemporaryAppWhitelistDuration() > 0) {
                                        scheduleTempWhitelistLocked(filter.owningUid, brOptions2.getTemporaryAppWhitelistDuration(), r);
                                        return;
                                    }
                                    return;
                                }
                                ResolveInfo info = (ResolveInfo) nextReceiver;
                                ComponentName component = new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
                                boolean skip4 = false;
                                skip4 = false;
                                if (brOptions2 != null && (info.activityInfo.applicationInfo.targetSdkVersion < brOptions2.getMinManifestReceiverApiLevel() || info.activityInfo.applicationInfo.targetSdkVersion > brOptions2.getMaxManifestReceiverApiLevel())) {
                                    skip4 = true;
                                    if (this.mColorQueue != null) {
                                        skip4 = this.mColorQueue.isSkipThisStaticBroadcastReceivers(r.intent, info);
                                    }
                                }
                                if (!skip4 && !this.mService.validateAssociationAllowedLocked(r.callerPackage, r.callingUid, component.getPackageName(), info.activityInfo.applicationInfo.uid)) {
                                    Slog.w(TAG, "Association not allowed: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") to " + component.flattenToShortString());
                                    skip4 = true;
                                }
                                if (!skip4 && (!this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, info.activityInfo.applicationInfo.uid))) {
                                    Slog.w(TAG, "Firewall blocked: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") to " + component.flattenToShortString());
                                }
                                ActivityManagerService activityManagerService = this.mService;
                                int perm = ActivityManagerService.checkComponentPermission(info.activityInfo.permission, r.callingPid, r.callingUid, info.activityInfo.applicationInfo.uid, info.activityInfo.exported);
                                if (!skip4 && perm != 0) {
                                    if (!info.activityInfo.exported) {
                                        Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") is not exported from uid " + info.activityInfo.applicationInfo.uid + " due to receiver " + component.flattenToShortString());
                                    } else {
                                        Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") requires " + info.activityInfo.permission + " due to receiver " + component.flattenToShortString());
                                    }
                                    skip = true;
                                } else if (skip4 || info.activityInfo.permission == null || (opCode = AppOpsManager.permissionToOpCode(info.activityInfo.permission)) == -1 || this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) == 0) {
                                    skip = skip4;
                                } else {
                                    Slog.w(TAG, "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") requires appop " + AppOpsManager.permissionToOp(info.activityInfo.permission) + " due to registered receiver " + component.flattenToShortString());
                                    skip = true;
                                }
                                if (!skip && info.activityInfo.applicationInfo.uid != 1000 && r.requiredPermissions != null && r.requiredPermissions.length > 0) {
                                    int perm2 = perm;
                                    int i5 = 0;
                                    while (true) {
                                        if (i5 >= r.requiredPermissions.length) {
                                            break;
                                        }
                                        String requiredPermission = r.requiredPermissions[i5];
                                        try {
                                            perm2 = AppGlobals.getPackageManager().checkPermission(requiredPermission, info.activityInfo.applicationInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                        } catch (RemoteException e6) {
                                            perm2 = -1;
                                        }
                                        if (perm2 == 0) {
                                            int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                                            if (appOp != -1 && appOp != r.appOp && this.mService.mAppOpsService.noteOperation(appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) != 0) {
                                                Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + component.flattenToShortString() + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                                skip = true;
                                                break;
                                            }
                                            i5++;
                                        } else {
                                            Slog.w(TAG, "Permission Denial: receiving " + r.intent + " to " + component.flattenToShortString() + " requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                            skip = true;
                                            break;
                                        }
                                    }
                                }
                                if (!skip && (r.appOp < -1 || r.appOp >= 91)) {
                                    Slog.w(TAG, "Appop Denial with num_op error: receiving " + r.intent + " to " + component.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                    skip = true;
                                }
                                if (!(skip || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) == 0)) {
                                    Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + component.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                    skip = true;
                                }
                                try {
                                    isSingleton = this.mService.isSingleton(info.activityInfo.processName, info.activityInfo.applicationInfo, info.activityInfo.name, info.activityInfo.flags);
                                } catch (SecurityException e7) {
                                    Slog.w(TAG, e7.getMessage());
                                    skip = true;
                                    isSingleton = false;
                                }
                                if (!((info.activityInfo.flags & 1073741824) == 0 || ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS", info.activityInfo.applicationInfo.uid) == 0)) {
                                    Slog.w(TAG, "Permission Denial: Receiver " + component.flattenToShortString() + " requests FLAG_SINGLE_USER, but app does not hold " + "android.permission.INTERACT_ACROSS_USERS");
                                    skip = true;
                                }
                                if (!skip && info.activityInfo.applicationInfo.isInstantApp() && r.callingUid != info.activityInfo.applicationInfo.uid) {
                                    Slog.w(TAG, "Instant App Denial: receiving " + r.intent + " to " + component.flattenToShortString() + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ") Instant Apps do not support manifest receivers");
                                    skip = true;
                                }
                                if (!skip && r.callerInstantApp && (info.activityInfo.flags & DumpState.DUMP_DEXOPT) == 0 && r.callingUid != info.activityInfo.applicationInfo.uid) {
                                    Slog.w(TAG, "Instant App Denial: receiving " + r.intent + " to " + component.flattenToShortString() + " requires receiver have visibleToInstantApps set due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                    skip = true;
                                }
                                if (r.curApp != null && r.curApp.isCrashing()) {
                                    Slog.w(TAG, "Skipping deliver ordered [" + this.mQueueName + "] " + r + " to " + r.curApp + ": process crashing");
                                    skip = true;
                                }
                                if (!skip) {
                                    boolean isAvailable = false;
                                    try {
                                        isAvailable = AppGlobals.getPackageManager().isPackageAvailable(info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                    } catch (Exception e8) {
                                        Slog.w(TAG, "Exception getting recipient info for " + info.activityInfo.packageName, e8);
                                    }
                                    if (!isAvailable) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Skipping delivery to " + info.activityInfo.packageName + " / " + info.activityInfo.applicationInfo.uid + " : package no longer available");
                                        }
                                        skip = true;
                                    }
                                }
                                if (!skip && !requestStartTargetPermissionsReviewIfNeededLocked(r, info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid))) {
                                    skip = true;
                                }
                                if (!skip) {
                                    skip = OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventProcessBroadcast(r, info, "skipSpecialBroadcast");
                                }
                                if (!skip && !OppoFeatureCache.get(IColorHansManager.DEFAULT).hansBroadcastIfNeeded(r, info)) {
                                    skip = true;
                                }
                                if (!OppoFeatureCache.get(IColorHansManager.DEFAULT).hansBroadcastIfNeeded(r, info)) {
                                    skip = true;
                                }
                                if (!skip && OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).skipSpecialBroadcast(null, info.activityInfo.packageName, r.intent, info.activityInfo.applicationInfo.processName, info.activityInfo.applicationInfo.uid, info.activityInfo.applicationInfo)) {
                                    skip = true;
                                }
                                int receiverUid2 = info.activityInfo.applicationInfo.uid;
                                if (r.callingUid != 1000 && isSingleton && this.mService.isValidSingletonCall(r.callingUid, receiverUid2)) {
                                    info.activityInfo = this.mService.getActivityInfoForUser(info.activityInfo, 0);
                                }
                                BroadcastOptions targetProcess = info.activityInfo.processName;
                                ProcessRecord app = this.mService.getProcessRecordLocked(targetProcess, info.activityInfo.applicationInfo.uid, false);
                                if (!skip && (allowed = this.mService.getAppStartModeLocked(info.activityInfo.applicationInfo.uid, info.activityInfo.packageName, info.activityInfo.applicationInfo.targetSdkVersion, -1, true, false, false)) != 0) {
                                    if (allowed == 3) {
                                        Slog.w(TAG, "Background execution disabled: receiving " + r.intent + " to " + component.flattenToShortString());
                                        skip = true;
                                    } else if ((r.intent.getFlags() & DumpState.DUMP_VOLUMES) != 0 || (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & DumpState.DUMP_SERVICE_PERMISSIONS) == 0 && !isSignaturePerm(r.requiredPermissions))) {
                                        this.mService.addBackgroundCheckViolationLocked(r.intent.getAction(), component.getPackageName());
                                        Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + component.flattenToShortString());
                                        if (this.mColorQueue != null) {
                                            skip3 = this.mColorQueue.isSkipThisStaticBroadcastReceivers(r.intent, info);
                                        } else {
                                            skip3 = true;
                                        }
                                        if (OppoListManager.getInstance().isAllowBackgroundBroadcastAction(r.intent.getAction(), info.activityInfo.applicationInfo.packageName)) {
                                            skip = false;
                                        }
                                    } else if (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 && OppoListManager.getInstance().isSkipBroadcastFlagRestricted(r.callingUid, r.callerPackage, info.activityInfo.applicationInfo) && !OppoListManager.getInstance().isAllowBackgroundBroadcastAction(r.intent.getAction(), info.activityInfo.applicationInfo.packageName)) {
                                        Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + component.flattenToShortString());
                                        skip = true;
                                    }
                                }
                                if (!skip && !"android.intent.action.ACTION_SHUTDOWN".equals(r.intent.getAction()) && !this.mService.mUserController.isUserRunning(UserHandle.getUserId(info.activityInfo.applicationInfo.uid), 0)) {
                                    skip = true;
                                    Slog.w(TAG, "Skipping delivery to " + info.activityInfo.packageName + " / " + info.activityInfo.applicationInfo.uid + " : user is not running");
                                }
                                if (this.mService.mAmsExt.onBeforeStartProcessForStaticReceiver(info.activityInfo.packageName)) {
                                    skip = true;
                                }
                                if (!this.mService.mAmsExt.isComponentNeedsStart(info.activityInfo.packageName, "static_broadcast")) {
                                    skip2 = true;
                                } else {
                                    skip2 = skip;
                                }
                                if (skip2) {
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Skipping delivery of ordered [" + this.mQueueName + "] " + r + " for reason described above");
                                    }
                                    r.delivery[recIdx] = 2;
                                    r.receiver = null;
                                    r.curFilter = null;
                                    r.state = 0;
                                    r.manifestSkipCount++;
                                    scheduleBroadcastsLocked();
                                    return;
                                }
                                r.manifestCount++;
                                r.delivery[recIdx] = 1;
                                r.state = 1;
                                r.curComponent = component;
                                r.curReceiver = info.activityInfo;
                                if (ActivityManagerDebugConfig.DEBUG_MU && r.callingUid > 100000) {
                                    Slog.v(TAG_MU, "Updated broadcast record activity info for secondary user, " + info.activityInfo + ", callingUid = " + r.callingUid + ", uid = " + receiverUid2);
                                }
                                if (brOptions2 != null && brOptions2.getTemporaryAppWhitelistDuration() > 0) {
                                    scheduleTempWhitelistLocked(receiverUid2, brOptions2.getTemporaryAppWhitelistDuration(), r);
                                }
                                try {
                                    AppGlobals.getPackageManager().setPackageStoppedState(r.curComponent.getPackageName(), false, r.userId);
                                } catch (RemoteException e9) {
                                } catch (IllegalArgumentException e10) {
                                    Slog.w(TAG, "Failed trying to unstop package " + r.curComponent.getPackageName() + ": " + e10);
                                }
                                if (app == null || app.thread == null || app.killed) {
                                    brOptions = targetProcess;
                                } else {
                                    try {
                                        app.addPackage(info.activityInfo.packageName, info.activityInfo.applicationInfo.longVersionCode, this.mService.mProcessStats);
                                        maybeAddAllowBackgroundActivityStartsToken(app, r);
                                        try {
                                            processCurBroadcastLocked(r, app, skipOomAdj);
                                            return;
                                        } catch (RemoteException e11) {
                                            e = e11;
                                            brOptions = targetProcess;
                                            Slog.w(TAG, "Exception when sending broadcast to " + r.curComponent, e);
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            }
                                            if (!OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).validStartBroadcast(info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid))) {
                                            }
                                        } catch (RuntimeException e12) {
                                            e = e12;
                                            Slog.w(TAG, "Failed sending broadcast to " + r.curComponent + " with " + r.intent, e);
                                            logBroadcastReceiverDiscardLocked(r);
                                            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                            scheduleBroadcastsLocked();
                                            r.state = 0;
                                            return;
                                        }
                                    } catch (RemoteException e13) {
                                        e = e13;
                                        brOptions = targetProcess;
                                        Slog.w(TAG, "Exception when sending broadcast to " + r.curComponent, e);
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        }
                                        if (!OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).validStartBroadcast(info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid))) {
                                        }
                                    } catch (RuntimeException e14) {
                                    }
                                }
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                    Slog.v(TAG_BROADCAST, "Need to start app [" + this.mQueueName + "] " + ((String) brOptions) + " for broadcast " + r);
                                }
                                if (!OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).validStartBroadcast(info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid))) {
                                    Slog.d(IColorAbnormalAppManager.TAG, "unable to start " + info.activityInfo.packageName + " for broadcast : package is restricted");
                                    finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, true);
                                    scheduleBroadcastsLocked();
                                    return;
                                }
                                if ((r.intent.getFlags() & 268435456) != 0) {
                                    cancelBroadcastTimeoutLocked();
                                    long timeoutTime2 = r.receiverTime + (this.mConstants.TIMEOUT * 2);
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "ReSubmitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r + " at " + timeoutTime2 + " for fgBroadcast.");
                                    }
                                    setBroadcastTimeoutLocked(timeoutTime2);
                                }
                                if (OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventProcessBroadcast(r, info, "startProcess")) {
                                    finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                    scheduleBroadcastsLocked();
                                    return;
                                }
                                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).monitorAppStartupInfo(r.callingPid, r.callingUid, r.callerApp, r.intent, info.activityInfo.applicationInfo, "broadcast");
                                ProcessRecord startProcessLocked = this.mService.startProcessLocked(brOptions, info.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, new HostingRecord("broadcast", r.curComponent), (r.intent.getFlags() & DumpState.DUMP_APEX) != 0, false, false);
                                r.curApp = startProcessLocked;
                                if (startProcessLocked == null) {
                                    Slog.w(TAG, "Unable to launch app " + info.activityInfo.applicationInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiverUid2 + " for broadcast " + r.intent + ": process is bad");
                                    logBroadcastReceiverDiscardLocked(r);
                                    finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                    scheduleBroadcastsLocked();
                                    r.state = 0;
                                    return;
                                }
                                maybeAddAllowBackgroundActivityStartsToken(r.curApp, r);
                                this.mPendingBroadcast = r;
                                this.mPendingBroadcastRecvIndex = recIdx;
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                    Slog.v(TAG, "mPendingBroadcast curApp = " + this.mPendingBroadcast.curApp + " pid = " + this.mPendingBroadcast.curApp.pid);
                                    return;
                                }
                                return;
                            }
                            i = 1;
                            z = false;
                            parallelSize2 = parallelSize;
                            firstTime2 = firstTime;
                            allParallelCost2 = allParallelCost;
                            looped2 = looped;
                        } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.d(TAG_BROADCAST, "processNextBroadcast(" + this.mQueueName + ") called when not idle (state=" + r4.state + ")");
                            return;
                        } else {
                            return;
                        }
                    }
                } else {
                    parallelSize = parallelSize2;
                }
            }
            forceReceive = false;
            if (r4.state != 0) {
            }
        }
    }

    private void maybeAddAllowBackgroundActivityStartsToken(ProcessRecord proc, BroadcastRecord r) {
        if (r != null && proc != null && r.allowBackgroundActivityStarts) {
            this.mHandler.removeCallbacksAndMessages((proc.toShortString() + r.toString()).intern());
            proc.addAllowBackgroundActivityStartsToken(r);
        }
    }

    /* access modifiers changed from: package-private */
    public final void setBroadcastTimeoutLocked(long timeoutTime) {
        if (!this.mPendingBroadcastTimeoutMessage) {
            this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(BROADCAST_TIMEOUT_MSG, this), timeoutTime);
            this.mPendingBroadcastTimeoutMessage = true;
            this.mService.mAnrManager.sendBroadcastMonitorMessage(timeoutTime, this.mConstants.TIMEOUT);
        }
    }

    /* access modifiers changed from: package-private */
    public final void cancelBroadcastTimeoutLocked() {
        if (this.mPendingBroadcastTimeoutMessage) {
            this.mHandler.removeMessages(BROADCAST_TIMEOUT_MSG, this);
            this.mPendingBroadcastTimeoutMessage = false;
            this.mService.mAnrManager.removeBroadcastMonitorMessage();
        }
    }

    /* access modifiers changed from: package-private */
    public final void broadcastTimeoutLocked(boolean fromMsg) {
        Object curReceiver;
        final ProcessRecord app;
        String anrMessage;
        boolean debugging = false;
        if (fromMsg) {
            this.mPendingBroadcastTimeoutMessage = false;
            this.mService.mAnrManager.removeBroadcastMonitorMessage();
        }
        if (!this.mDispatcher.isEmpty() && this.mDispatcher.getActiveBroadcastLocked() != null) {
            long now = SystemClock.uptimeMillis();
            final BroadcastRecord r = this.mDispatcher.getActiveBroadcastLocked();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST && r != null) {
                Slog.w(TAG, "Timeout of broadcast1111 " + r + " - receiver=" + r.receiver + ", started " + (now - r.receiverTime) + "ms ago  ");
            }
            if (fromMsg) {
                if (this.mService.mAnrManager.isAnrDeferrable()) {
                    setBroadcastTimeoutLocked(SystemClock.uptimeMillis() + this.mConstants.TIMEOUT);
                    return;
                } else if (this.mService.mProcessesReady) {
                    if (!r.timeoutExempt) {
                        long timeoutTime = r.receiverTime + this.mConstants.TIMEOUT;
                        if (timeoutTime > now) {
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v(TAG_BROADCAST, "Premature timeout [" + this.mQueueName + "] @ " + now + ": resetting BROADCAST_TIMEOUT_MSG for " + timeoutTime);
                            }
                            setBroadcastTimeoutLocked(timeoutTime);
                            return;
                        }
                    } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.i(TAG_BROADCAST, "Broadcast timeout but it's exempt: " + r.intent.getAction());
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            if (r.state == 4) {
                StringBuilder sb = new StringBuilder();
                sb.append("Waited long enough for: ");
                sb.append(r.curComponent != null ? r.curComponent.flattenToShortString() : "(null)");
                Slog.i(TAG, sb.toString());
                r.curComponent = null;
                r.state = 0;
                processNextBroadcast(false);
                return;
            }
            if (r.curApp != null && r.curApp.isDebugging()) {
                debugging = true;
            }
            Slog.w(TAG, "Timeout of broadcast " + r + " - receiver=" + r.receiver + ", started " + (now - r.receiverTime) + "ms ago");
            r.receiverTime = now;
            if (!debugging) {
                r.anrCount++;
            }
            ProcessRecord app2 = null;
            if (r.nextReceiver > 0) {
                Object curReceiver2 = r.receivers.get(r.nextReceiver - 1);
                r.delivery[r.nextReceiver - 1] = 3;
                curReceiver = curReceiver2;
            } else {
                curReceiver = r.curReceiver;
            }
            Slog.w(TAG, "Receiver during timeout: " + curReceiver + " r.state " + r.state);
            logBroadcastReceiverDiscardLocked(r);
            if (curReceiver == null || !(curReceiver instanceof BroadcastFilter)) {
                app = r.curApp;
            } else {
                BroadcastFilter bf = (BroadcastFilter) curReceiver;
                if (!(bf.receiverList.pid == 0 || bf.receiverList.pid == ActivityManagerService.MY_PID)) {
                    synchronized (this.mService.mPidsSelfLocked) {
                        app2 = this.mService.mPidsSelfLocked.get(bf.receiverList.pid);
                    }
                }
                app = app2;
            }
            new Thread(new Runnable() {
                /* class com.android.server.am.BroadcastQueue.AnonymousClass2 */

                public void run() {
                    ProcessRecord processRecord;
                    if (r.ordered && (processRecord = app) != null && processRecord.thread != null) {
                        int flag = 0;
                        try {
                            if (r.intent != null) {
                                flag = r.intent.getFlags();
                            }
                            app.thread.getBroadcastState(flag);
                            Slog.w(BroadcastQueue.TAG, "Timeout receiver in proc " + app + " broadcast " + r);
                        } catch (Exception e) {
                            Slog.v(BroadcastQueue.TAG, "Exception " + e + " record " + r);
                        }
                    }
                }
            }).start();
            if (app != null) {
                anrMessage = "Broadcast of " + r.intent.toString();
            } else {
                anrMessage = null;
            }
            if (this.mPendingBroadcast == r) {
                this.mPendingBroadcast = null;
            }
            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            scheduleBroadcastsLocked();
            if (!debugging && anrMessage != null) {
                this.mHandler.post(new AppNotResponding(app, anrMessage));
            }
        }
    }

    private final int ringAdvance(int x, int increment, int ringSize) {
        int x2 = x + increment;
        if (x2 < 0) {
            return ringSize - 1;
        }
        if (x2 >= ringSize) {
            return 0;
        }
        return x2;
    }

    private final void addBroadcastToHistoryLocked(BroadcastRecord original) {
        if (original.callingUid >= 0) {
            original.finishTime = SystemClock.uptimeMillis();
            if (Trace.isTagEnabled(64)) {
                Trace.asyncTraceEnd(64, createBroadcastTraceTitle(original, 1), System.identityHashCode(original));
            }
            BroadcastRecord historyRecord = original.maybeStripForHistory();
            BroadcastRecord[] broadcastRecordArr = this.mBroadcastHistory;
            int i = this.mHistoryNext;
            broadcastRecordArr[i] = historyRecord;
            this.mHistoryNext = ringAdvance(i, 1, MAX_BROADCAST_HISTORY);
            this.mBroadcastSummaryHistory[this.mSummaryHistoryNext] = historyRecord.intent;
            this.mSummaryHistoryEnqueueTime[this.mSummaryHistoryNext] = historyRecord.enqueueClockTime;
            this.mSummaryHistoryDispatchTime[this.mSummaryHistoryNext] = historyRecord.dispatchClockTime;
            this.mSummaryHistoryFinishTime[this.mSummaryHistoryNext] = System.currentTimeMillis();
            this.mSummaryHistoryNext = ringAdvance(this.mSummaryHistoryNext, 1, MAX_BROADCAST_SUMMARY_HISTORY);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean cleanupDisabledPackageReceiversLocked(String packageName, Set<String> filterByClasses, int userId, boolean doit) {
        boolean didSomething = false;
        for (int i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
            didSomething |= this.mParallelBroadcasts.get(i).cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        return didSomething | this.mDispatcher.cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
    }

    /* access modifiers changed from: package-private */
    public final void logBroadcastReceiverDiscardLocked(BroadcastRecord r) {
        int logIndex = r.nextReceiver - 1;
        if (logIndex < 0 || logIndex >= r.receivers.size()) {
            if (logIndex < 0) {
                Slog.w(TAG, "Discarding broadcast before first receiver is invoked: " + r);
            }
            EventLog.writeEvent((int) EventLogTags.AM_BROADCAST_DISCARD_APP, -1, Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(r.nextReceiver), "NONE");
            return;
        }
        Object curReceiver = r.receivers.get(logIndex);
        if (curReceiver instanceof BroadcastFilter) {
            BroadcastFilter bf = (BroadcastFilter) curReceiver;
            EventLog.writeEvent((int) EventLogTags.AM_BROADCAST_DISCARD_FILTER, Integer.valueOf(bf.owningUserId), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(logIndex), Integer.valueOf(System.identityHashCode(bf)));
            return;
        }
        ResolveInfo ri = (ResolveInfo) curReceiver;
        EventLog.writeEvent((int) EventLogTags.AM_BROADCAST_DISCARD_APP, Integer.valueOf(UserHandle.getUserId(ri.activityInfo.applicationInfo.uid)), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(logIndex), ri.toString());
    }

    private String createBroadcastTraceTitle(BroadcastRecord record, int state) {
        Object[] objArr = new Object[4];
        objArr[0] = state == 0 ? "in queue" : "dispatched";
        String str = "";
        objArr[1] = record.callerPackage == null ? str : record.callerPackage;
        objArr[2] = record.callerApp == null ? "process unknown" : record.callerApp.toShortString();
        if (record.intent != null) {
            str = record.intent.getAction();
        }
        objArr[3] = str;
        return String.format("Broadcast %s from %s (%s) %s", objArr);
    }

    /* access modifiers changed from: package-private */
    public boolean isIdle() {
        return this.mParallelBroadcasts.isEmpty() && this.mDispatcher.isEmpty() && this.mPendingBroadcast == null;
    }

    /* access modifiers changed from: package-private */
    public void cancelDeferrals() {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mDispatcher.cancelDeferralsLocked();
                scheduleBroadcastsLocked();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String describeState() {
        String str;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                str = this.mParallelBroadcasts.size() + " parallel; " + this.mDispatcher.describeStateLocked();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        int i;
        int lastIndex;
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mQueueName);
        for (int i2 = this.mParallelBroadcasts.size() - 1; i2 >= 0; i2--) {
            this.mParallelBroadcasts.get(i2).writeToProto(proto, 2246267895810L);
        }
        this.mDispatcher.writeToProto(proto, 2246267895811L);
        BroadcastRecord broadcastRecord = this.mPendingBroadcast;
        if (broadcastRecord != null) {
            broadcastRecord.writeToProto(proto, 1146756268036L);
        }
        int lastIndex2 = this.mHistoryNext;
        int ringIndex = lastIndex2;
        do {
            i = -1;
            ringIndex = ringAdvance(ringIndex, -1, MAX_BROADCAST_HISTORY);
            BroadcastRecord r = this.mBroadcastHistory[ringIndex];
            if (r != null) {
                r.writeToProto(proto, 2246267895813L);
                continue;
            }
        } while (ringIndex != lastIndex2);
        int i3 = this.mSummaryHistoryNext;
        int ringIndex2 = i3;
        int lastIndex3 = i3;
        while (true) {
            int ringIndex3 = ringAdvance(ringIndex2, i, MAX_BROADCAST_SUMMARY_HISTORY);
            Intent intent = this.mBroadcastSummaryHistory[ringIndex3];
            if (intent == null) {
                lastIndex = lastIndex3;
            } else {
                long summaryToken = proto.start(2246267895814L);
                lastIndex = lastIndex3;
                intent.writeToProto(proto, 1146756268033L, false, true, true, false);
                proto.write(1112396529666L, this.mSummaryHistoryEnqueueTime[ringIndex3]);
                proto.write(1112396529667L, this.mSummaryHistoryDispatchTime[ringIndex3]);
                proto.write(1112396529668L, this.mSummaryHistoryFinishTime[ringIndex3]);
                proto.end(summaryToken);
            }
            if (ringIndex3 == lastIndex) {
                proto.end(token);
                return;
            }
            lastIndex3 = lastIndex;
            ringIndex2 = ringIndex3;
            i = -1;
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        boolean needSep2;
        String str;
        int lastIndex;
        boolean printed;
        BroadcastRecord broadcastRecord;
        String str2 = dumpPackage;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String str3 = ":";
        if (!this.mParallelBroadcasts.isEmpty() || !this.mDispatcher.isEmpty() || this.mPendingBroadcast != null) {
            boolean printed2 = false;
            boolean needSep3 = needSep;
            for (int i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
                BroadcastRecord br = this.mParallelBroadcasts.get(i);
                if (str2 == null || str2.equals(br.callerPackage)) {
                    if (!printed2) {
                        if (needSep3) {
                            pw.println();
                        }
                        needSep3 = true;
                        printed2 = true;
                        pw.println("  Active broadcasts [" + this.mQueueName + "]:");
                    }
                    pw.println("  Active Broadcast " + this.mQueueName + " #" + i + str3);
                    br.dump(pw, "    ", sdf);
                }
            }
            this.mDispatcher.dumpLocked(pw, str2, this.mQueueName, sdf);
            if (str2 == null || ((broadcastRecord = this.mPendingBroadcast) != null && str2.equals(broadcastRecord.callerPackage))) {
                pw.println();
                pw.println("  Pending broadcast [" + this.mQueueName + "]:");
                BroadcastRecord broadcastRecord2 = this.mPendingBroadcast;
                if (broadcastRecord2 != null) {
                    broadcastRecord2.dump(pw, "    ", sdf);
                } else {
                    pw.println("    (null)");
                }
                needSep2 = true;
            } else {
                needSep2 = needSep3;
            }
        } else {
            needSep2 = needSep;
        }
        this.mConstants.dump(pw);
        boolean printed3 = false;
        int i2 = -1;
        int lastIndex2 = this.mHistoryNext;
        int ringIndex = lastIndex2;
        while (true) {
            int ringIndex2 = ringAdvance(ringIndex, -1, MAX_BROADCAST_HISTORY);
            BroadcastRecord r = this.mBroadcastHistory[ringIndex2];
            if (r == null) {
                str = str3;
            } else {
                i2++;
                if (str2 == null || str2.equals(r.callerPackage)) {
                    if (!printed3) {
                        if (needSep2) {
                            pw.println();
                        }
                        needSep2 = true;
                        pw.println("  Historical broadcasts [" + this.mQueueName + "]:");
                        printed = true;
                    } else {
                        printed = printed3;
                    }
                    if (dumpAll) {
                        pw.print("  Historical Broadcast " + this.mQueueName + " #");
                        pw.print(i2);
                        pw.println(str3);
                        r.dump(pw, "    ", sdf);
                        str = str3;
                    } else {
                        pw.print("  #");
                        pw.print(i2);
                        pw.print(": ");
                        pw.println(r);
                        pw.print("    ");
                        str = str3;
                        pw.println(r.intent.toShortString(false, true, true, false));
                        if (!(r.targetComp == null || r.targetComp == r.intent.getComponent())) {
                            pw.print("    targetComp: ");
                            pw.println(r.targetComp.toShortString());
                        }
                        Bundle bundle = r.intent.getExtras();
                        if (bundle != null) {
                            pw.print("    extras: ");
                            pw.println(bundle.toString());
                        }
                    }
                    printed3 = printed;
                } else {
                    str = str3;
                }
            }
            ringIndex = ringIndex2;
            if (ringIndex == lastIndex2) {
                break;
            }
            str2 = dumpPackage;
            lastIndex2 = lastIndex2;
            str3 = str;
        }
        if (str2 == null) {
            int lastIndex3 = this.mSummaryHistoryNext;
            int ringIndex3 = lastIndex3;
            if (dumpAll) {
                printed3 = false;
                i2 = -1;
            } else {
                int j = i2;
                while (j > 0 && ringIndex3 != lastIndex3) {
                    ringIndex3 = ringAdvance(ringIndex3, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                    if (this.mBroadcastHistory[ringIndex3] != null) {
                        j--;
                    }
                }
            }
            while (true) {
                ringIndex3 = ringAdvance(ringIndex3, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                Intent intent = this.mBroadcastSummaryHistory[ringIndex3];
                if (intent != null) {
                    if (!printed3) {
                        if (needSep2) {
                            pw.println();
                        }
                        pw.println("  Historical broadcasts summary [" + this.mQueueName + "]:");
                        printed3 = true;
                        needSep2 = true;
                    }
                    if (!dumpAll && i2 >= 50) {
                        pw.println("  ...");
                        break;
                    }
                    i2++;
                    pw.print("  #");
                    pw.print(i2);
                    pw.print(": ");
                    pw.println(intent.toShortString(false, true, true, false));
                    pw.print("    ");
                    lastIndex = lastIndex3;
                    TimeUtils.formatDuration(this.mSummaryHistoryDispatchTime[ringIndex3] - this.mSummaryHistoryEnqueueTime[ringIndex3], pw);
                    pw.print(" dispatch ");
                    TimeUtils.formatDuration(this.mSummaryHistoryFinishTime[ringIndex3] - this.mSummaryHistoryDispatchTime[ringIndex3], pw);
                    pw.println(" finish");
                    pw.print("    enq=");
                    pw.print(sdf.format(new Date(this.mSummaryHistoryEnqueueTime[ringIndex3])));
                    pw.print(" disp=");
                    pw.print(sdf.format(new Date(this.mSummaryHistoryDispatchTime[ringIndex3])));
                    pw.print(" fin=");
                    pw.println(sdf.format(new Date(this.mSummaryHistoryFinishTime[ringIndex3])));
                    Bundle bundle2 = intent.getExtras();
                    if (bundle2 != null) {
                        pw.print("    extras: ");
                        pw.println(bundle2.toString());
                    }
                    printed3 = printed3;
                } else {
                    lastIndex = lastIndex3;
                }
                if (ringIndex3 == lastIndex) {
                    break;
                }
                lastIndex3 = lastIndex;
            }
        }
        return needSep2;
    }

    @Override // com.android.server.am.OppoBaseBroadcastQueue
    public Handler getBroadcastHandler() {
        return this.mHandler;
    }

    @Override // com.android.server.am.OppoBaseBroadcastQueue
    public String getBroadcastQueueName() {
        return this.mQueueName;
    }

    @Override // com.android.server.am.OppoBaseBroadcastQueue
    public int getOrderedBroadcastsSize() {
        ArrayList<BroadcastRecord> list = this.mDispatcher.getOrderedBroadcasts();
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override // com.android.server.am.OppoBaseBroadcastQueue
    public void requestProcessNextBroadcastLocked(boolean fromMsg, boolean skipOomAdj) {
        processNextBroadcastLocked(fromMsg, skipOomAdj);
    }
}
