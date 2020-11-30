package com.android.server;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public abstract class OppoStateWatch {
    static final int CHECK_STATE_MSG = 1;
    static final String TAG = "StateWatchTag";
    boolean mChecking = false;
    final StateWatchHandler mHandler = new StateWatchHandler(OppoCheckBlockedException.getInstance().getCheckLoop());
    int mStateFailCount = 0;

    public abstract void dealAction();

    public abstract int getCheckCount();

    public abstract int getCheckInterval();

    public abstract boolean isStateOk();

    /* access modifiers changed from: package-private */
    public boolean isCheckEnable() {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean remedyAction() {
        return false;
    }

    private final class StateWatchHandler extends Handler {
        public StateWatchHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                synchronized (this) {
                    if (OppoStateWatch.this.isStateOk()) {
                        OppoStateWatch.this.mStateFailCount = 0;
                        OppoStateWatch.this.mChecking = false;
                    } else {
                        OppoStateWatch.this.mStateFailCount++;
                        if (OppoStateWatch.this.mStateFailCount >= OppoStateWatch.this.getCheckCount()) {
                            try {
                                if (OppoStateWatch.this.remedyAction()) {
                                    SystemClock.sleep(5000);
                                    if (OppoStateWatch.this.isStateOk()) {
                                        OppoStateWatch.this.mStateFailCount = 0;
                                        OppoStateWatch.this.mChecking = false;
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                Log.i(OppoStateWatch.TAG, "remedyAction exception e = " + e);
                            }
                            OppoStateWatch.this.dealAction();
                            OppoStateWatch.this.mStateFailCount = 0;
                        } else {
                            OppoStateWatch.this.mHandler.removeMessages(1);
                            OppoStateWatch.this.mHandler.sendMessageDelayed(OppoStateWatch.this.mHandler.obtainMessage(1), (long) OppoStateWatch.this.getCheckInterval());
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void triggerDetect() {
        synchronized (this) {
            if (isCheckEnable() && !this.mChecking) {
                this.mChecking = true;
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDCSSendEnable() {
        return true;
    }
}
