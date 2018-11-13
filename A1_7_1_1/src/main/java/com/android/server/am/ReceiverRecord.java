package com.android.server.am;

import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;

final class ReceiverRecord {
    static final int BROADCAST_TIMEOUT_MSG_APP = 1;
    static final String TAG = "ReceiverRecord";
    ProcessRecord curApp;
    final BroadcastAppHandler mHandler = new BroadcastAppHandler(this.mLooper);
    Intent mIntent;
    Looper mLooper;
    BroadcastQueue mQueue;
    IBinder mReceiver;
    final ActivityManagerService mService;
    BroadcastRecord r;

    private final class AppNotResponding implements Runnable {
        private final String mAnnotation;
        private final ProcessRecord mApp;

        public AppNotResponding(ProcessRecord app, String annotation) {
            this.mApp = app;
            this.mAnnotation = annotation;
        }

        public void run() {
            if (this.mApp != null) {
                ReceiverRecord.this.mService.mAppErrors.appNotResponding(this.mApp, null, null, false, this.mAnnotation);
                return;
            }
            Slog.w(ReceiverRecord.TAG, "mApp is null ,not need to send not respond");
        }
    }

    private final class BroadcastAppHandler extends Handler {
        public BroadcastAppHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (ReceiverRecord.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ReceiverRecord.this.broadcastTimeoutLocked(true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    ReceiverRecord(ActivityManagerService Service, BroadcastQueue queue, BroadcastRecord record, ProcessRecord app, IBinder receiver, Intent intent, Looper looper) {
        this.mService = Service;
        this.mQueue = queue;
        this.r = record;
        this.curApp = app;
        this.mReceiver = receiver;
        this.mIntent = intent;
        this.mLooper = looper;
    }

    /* JADX WARNING: Missing block: B:38:0x0145, code:
            if (r0 == null) goto L_0x0180;
     */
    /* JADX WARNING: Missing block: B:41:0x0149, code:
            if (r10.curApp == null) goto L_0x0180;
     */
    /* JADX WARNING: Missing block: B:43:0x0151, code:
            if (com.android.server.am.OppoProcessManagerHelper.checkProcessWhileBroadcastTimeout(r10.curApp) == false) goto L_0x0168;
     */
    /* JADX WARNING: Missing block: B:44:0x0153, code:
            return;
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            r10.mHandler.post(new com.android.server.am.ReceiverRecord.AppNotResponding(r10, r10.curApp, r0));
            r10.r = null;
            r10.curApp = null;
            r10.mIntent = null;
            r10.mLooper = null;
     */
    /* JADX WARNING: Missing block: B:62:0x0181, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:63:0x0182, code:
            android.util.Slog.w(TAG, "Exception in broadcastTimeoutLocked, exception", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void broadcastTimeoutLocked(boolean fromMsg) {
        if (!ActivityManagerDebugConfig.DEBUG_BROADCAST) {
        }
        Slog.v(TAG, "ReceiverRecordbroadcastTimeoutLocked this  " + this + "curApp " + this.curApp + " thread.getName() " + Thread.currentThread().getName());
        String anrMessage = null;
        synchronized (this) {
            if (!(this.curApp == null || this.curApp.pid == 0)) {
                anrMessage = this.r.intent != null ? "Broadcast of " + this.r.intent.toString() : "Broadcast of " + this.r;
            }
            if (this.curApp != null) {
                synchronized (this.curApp.receiverRecords) {
                    try {
                        if (!ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        }
                        Slog.v(TAG, this.curApp + " curApp.receiverRecords.size() " + this.curApp.receiverRecords.size() + " " + this.curApp.receiverRecords);
                        int size = this.curApp.receiverRecords.size();
                        if (size > 0) {
                            for (int i = size - 1; i >= 0; i--) {
                                ReceiverRecord receiverRecord = (ReceiverRecord) this.curApp.receiverRecords.get(i);
                                Slog.v(TAG, "broadcastTimeoutLocked receiverRecord " + receiverRecord);
                                if (!(this.mQueue.mReceiverRecords == null || receiverRecord == null)) {
                                    this.mQueue.mReceiverRecords.remove(receiverRecord.hashCode());
                                }
                                this.curApp.receiverRecords.remove(receiverRecord);
                            }
                            if (!ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            }
                            Slog.v(TAG, this.curApp + " curApp.receiverRecords after remove = " + this.curApp.receiverRecords.size());
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        Slog.w(TAG, "Exception in broadcastTimeoutLocked ", e);
                    }
                }
            }
        }
        return;
    }

    final synchronized void cancelBroadcastTimeoutLocked() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.i(TAG, "cancelBroadcastTimeoutLocked this " + this + Debug.getCallers(4));
        }
        this.mHandler.removeMessages(1, this);
        this.r = null;
        this.curApp = null;
        this.mIntent = null;
        this.mLooper = null;
    }

    final synchronized void setBroadcastTimeoutLocked(long timeoutTime) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "setBroadcastTimeoutLocked this " + this);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, this), timeoutTime);
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

    public String toString() {
        return "ReceiverRecord{" + Integer.toHexString(System.identityHashCode(this)) + "mQueue.mQueueName " + this.mQueue.mQueueName + " r= " + this.r + " " + " curApp " + this.curApp + "mIntent " + this.mIntent + " mHandler " + this.mHandler + "}";
    }
}
