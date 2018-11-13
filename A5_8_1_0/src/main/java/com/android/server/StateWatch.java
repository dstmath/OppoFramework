package com.android.server;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.face.FaceDaemonWrapper;

public abstract class StateWatch {
    static final int CHECK_STATE_MSG = 1;
    static final String TAG = "StateWatchTag";
    boolean mChecking = false;
    final StateWatchHandler mHandler = new StateWatchHandler(CheckBlockedException.getInstance().getCheckLoop());
    int mStateFailCount = 0;

    private final class StateWatchHandler extends Handler {
        public StateWatchHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (this) {
                        if (!StateWatch.this.isStateOk()) {
                            StateWatch stateWatch = StateWatch.this;
                            stateWatch.mStateFailCount++;
                            if (StateWatch.this.mStateFailCount < StateWatch.this.getCheckCount()) {
                                StateWatch.this.mHandler.removeMessages(1);
                                StateWatch.this.mHandler.sendMessageDelayed(StateWatch.this.mHandler.obtainMessage(1), (long) StateWatch.this.getCheckInterval());
                                break;
                            }
                            try {
                                if (StateWatch.this.RemedyAction()) {
                                    SystemClock.sleep(FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
                                    if (StateWatch.this.isStateOk()) {
                                        StateWatch.this.mStateFailCount = 0;
                                        StateWatch.this.mChecking = false;
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                Log.i(StateWatch.TAG, "RemedyAction exception e = " + e);
                            }
                            StateWatch.this.dealAction();
                            StateWatch.this.mStateFailCount = 0;
                            break;
                        }
                        StateWatch.this.mStateFailCount = 0;
                        StateWatch.this.mChecking = false;
                        break;
                    }
            }
        }
    }

    abstract void dealAction();

    abstract int getCheckCount();

    abstract int getCheckInterval();

    abstract boolean isStateOk();

    StateWatch() {
    }

    boolean isCheckEnable() {
        return true;
    }

    boolean RemedyAction() {
        return false;
    }

    void triggerDetect() {
        synchronized (this) {
            if (isCheckEnable() && (this.mChecking ^ 1) != 0) {
                this.mChecking = true;
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
            }
        }
    }

    boolean isDCSSendEnable() {
        return true;
    }
}
