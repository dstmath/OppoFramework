package com.android.server.am;

import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import com.color.util.ColorTypeCastingHelper;

/* access modifiers changed from: package-private */
public final class OppoReceiverRecord {
    static final int BROADCAST_TIMEOUT_MSG_APP = 1;
    static final String TAG = "OppoReceiverRecord";
    ProcessRecord curApp;
    final BroadcastAppHandler mHandler = new BroadcastAppHandler(this.mLooper);
    boolean mHasFinish;
    Intent mIntent;
    Looper mLooper;
    OppoBaseBroadcastQueue mQueue;
    IBinder mReceiver;
    final ActivityManagerService mService;
    BroadcastRecord r;

    /* access modifiers changed from: private */
    public final class BroadcastAppHandler extends Handler {
        public BroadcastAppHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                synchronized (OppoReceiverRecord.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        OppoReceiverRecord.this.broadcastTimeoutLocked(true);
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }
    }

    OppoReceiverRecord(ActivityManagerService Service, OppoBaseBroadcastQueue queue, BroadcastRecord record, ProcessRecord app, IBinder receiver, Intent intent, Looper looper) {
        this.mService = Service;
        this.mQueue = queue;
        this.r = record;
        this.curApp = app;
        this.mReceiver = receiver;
        this.mIntent = intent;
        this.mLooper = looper;
    }

    /* access modifiers changed from: package-private */
    public final void broadcastTimeoutLocked(boolean fromMsg) {
        boolean z = ActivityManagerDebugConfig.DEBUG_BROADCAST;
        Thread thread = Thread.currentThread();
        Slog.v(TAG, "ReceiverRecordbroadcastTimeoutLocked this  " + this + "curApp " + this.curApp + " thread.getName() " + thread.getName());
        String anrMessage = null;
        synchronized (this) {
            if (!(this.curApp == null || this.curApp.pid == 0)) {
                if (this.r.intent != null) {
                    anrMessage = "Broadcast of " + this.r.intent.toString();
                } else {
                    anrMessage = "Broadcast of " + this.r;
                }
            }
            if (this.curApp != null) {
                typeCaseToParent(this.curApp).removeAllOppoReceiverRecords(this.mQueue);
            }
        }
        if (anrMessage != null) {
            try {
                if (this.curApp != null) {
                    this.mHandler.post(new AppNotResponding(this.curApp, anrMessage));
                    this.r = null;
                    this.curApp = null;
                    this.mIntent = null;
                    this.mLooper = null;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception in broadcastTimeoutLocked, exception", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized void cancelBroadcastTimeoutLocked() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.i(TAG, "cancelBroadcastTimeoutLocked this " + this + Debug.getCallers(4));
        }
        this.mHandler.removeMessages(1, this);
        this.r = null;
        this.curApp = null;
        this.mIntent = null;
        this.mLooper = null;
    }

    /* access modifiers changed from: package-private */
    public final synchronized void setBroadcastTimeoutLocked(long timeoutTime) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "setBroadcastTimeoutLocked this " + this);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, this), timeoutTime);
    }

    /* access modifiers changed from: private */
    public final class AppNotResponding implements Runnable {
        private final String mAnnotation;
        private final ProcessRecord mApp;

        public AppNotResponding(ProcessRecord app, String annotation) {
            this.mApp = app;
            this.mAnnotation = annotation;
        }

        public void run() {
            ProcessRecord processRecord = this.mApp;
            if (processRecord != null) {
                processRecord.appNotResponding(null, null, null, null, false, this.mAnnotation);
            } else {
                Slog.w(OppoReceiverRecord.TAG, "mApp is null ,not need to send not respond");
            }
        }
    }

    public IBinder getBinder() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "getBinder  mReceiver " + this.mReceiver);
        }
        return this.mReceiver;
    }

    public Intent getIntent() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "getIntent  mIntent " + this.mIntent);
        }
        return this.mIntent;
    }

    public ProcessRecord getApp() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "getApp  app= " + this.curApp);
        }
        return this.curApp;
    }

    public BroadcastRecord getBroadcastRecord() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "getBroadcastRecord  r= " + this.r);
        }
        return this.r;
    }

    public OppoBaseBroadcastQueue getBroadcastQueue() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "getBroadcastQueue  mQueue= " + this.mQueue);
        }
        return this.mQueue;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OppoReceiverRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(", mQueueName:");
        sb.append(this.mQueue.getBroadcastQueueName());
        sb.append(", r:" + this.r);
        sb.append(", curApp:" + this.curApp);
        sb.append(", mIntent:" + this.mIntent);
        sb.append(", mHandler:" + this.mHandler);
        return sb.toString();
    }

    private OppoBaseProcessRecord typeCaseToParent(ProcessRecord queue) {
        return (OppoBaseProcessRecord) ColorTypeCastingHelper.typeCasting(OppoBaseProcessRecord.class, queue);
    }
}
