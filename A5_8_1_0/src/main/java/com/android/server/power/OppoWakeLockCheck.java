package com.android.server.power;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.WorkSource;
import java.io.PrintWriter;
import java.util.ArrayList;

class OppoWakeLockCheck {
    private static final boolean ADBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String ATAG = "OppoWakeLockCheck";
    private static final long DEVICEIDLE_ON_CHECK_DELAY = 300;
    private static final int MSG_DEVICEIDLE_ON = 10;
    private static final int MSG_PARTIAL_WAKELOCK_ACQUIRE = 3;
    private static final int MSG_PARTIAL_WAKELOCK_CHECK = 1;
    private static final int MSG_PARTIAL_WAKELOCK_RELEASE = 4;
    public static final int MSG_PARTIAL_WAKELOCK_TIMEOUT = 7;
    public static final int MSG_POSSIBLE_PLAYER = 9;
    private static final int MSG_SCREEN_OFF = 5;
    private static final int MSG_SCREEN_ON = 6;
    private static final int MSG_SCREEN_ON_WAKELOCK_CHECK = 2;
    private static final long SCREEN_ON_WAKELOCK_CHECK_DELAY = 30000;
    private volatile boolean isScreenOff = false;
    private final CommonUtil mCommonUtil;
    private WorkerHandler mHandler;
    private final OppoPartialWakeLockCheck mPartialWakeLock;
    private final OppoScreenOnWakeLockCheck mScreenOnWakeLock;
    private final SuspendBlocker mSuspendBlocker;
    private boolean msgPartialWakelockSent = false;
    private boolean msgScreenOnWakelockSent = false;

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.wakeLockHeldContinuouslyCheck(1);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 2:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mScreenOnWakeLock.check();
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 3:
                    OppoWakeLockCheck.this.mPartialWakeLock.noteStartWakeLock(msg.getData());
                    return;
                case 4:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.noteStopWakeLock(msg.getData());
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 5:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.onScreenOff(1);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 6:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.onScreenOn(1);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 7:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.wakeLockTimeout(msg);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 9:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.possiblePlayer(msg);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 10:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.onDeviceIdle();
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                default:
                    return;
            }
        }
    }

    public OppoWakeLockCheck(ArrayList<WakeLock> wakeLocks, Object lock, Context context, PowerManagerService pms, SuspendBlocker suspendBlocker) {
        HandlerThread ht = new HandlerThread(ATAG);
        ht.start();
        this.mHandler = new WorkerHandler(ht.getLooper());
        this.mSuspendBlocker = suspendBlocker;
        this.mCommonUtil = new CommonUtil(context);
        this.mPartialWakeLock = new OppoPartialWakeLockCheck(wakeLocks, lock, context, pms, this.mCommonUtil, ADBG, this.mHandler);
        this.mScreenOnWakeLock = new OppoScreenOnWakeLockCheck(wakeLocks, lock, context, pms, this.mCommonUtil, ADBG);
    }

    public boolean canSyncWakeLockAcq(int uid, String tag) {
        return this.mPartialWakeLock.canSyncWakeLockAcq(uid, tag);
    }

    public void PartialWakelockCheckStart() {
        if (!this.msgPartialWakelockSent) {
            this.msgPartialWakelockSent = true;
            this.mPartialWakeLock.clearSyncWakelock();
            this.mHandler.sendEmptyMessage(5);
        }
        this.isScreenOff = true;
    }

    public void PartialWakelockCheckStop() {
        if (this.msgPartialWakelockSent) {
            this.msgPartialWakelockSent = false;
            this.mPartialWakeLock.clearSyncWakelock();
            this.mHandler.sendEmptyMessage(6);
        }
        this.isScreenOff = false;
    }

    public void screenOnWakelockCheckStart() {
        if (!this.msgScreenOnWakelockSent) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 30000);
            this.msgScreenOnWakelockSent = true;
        }
    }

    public void screenOnWakelockCheckStop() {
        if (this.msgScreenOnWakelockSent) {
            this.mHandler.removeMessages(2);
            this.msgScreenOnWakelockSent = false;
        }
    }

    public void noteWakeLockChange(WakeLock wl, boolean acquire) {
        int msg;
        if (acquire) {
            msg = 3;
        } else {
            msg = 4;
        }
        this.mPartialWakeLock.noteWakeLockChange(wl, msg, this.mHandler, wl.mWorkSource);
    }

    public void noteWorkSourceChange(WakeLock wl, WorkSource newWorkSource) {
        this.mPartialWakeLock.noteWakeLockChange(wl, 4, this.mHandler, wl.mWorkSource);
        this.mPartialWakeLock.noteWakeLockChange(wl, 3, this.mHandler, newWorkSource);
    }

    public void dumpPossibleMusicPlayer(PrintWriter pw) {
        this.mPartialWakeLock.dumpPossibleMusicPlayer(pw);
    }

    public void logSwitch(boolean enable) {
        this.mPartialWakeLock.logSwitch(enable);
    }

    public void dumpCameraState(PrintWriter pw) {
        this.mPartialWakeLock.dumpCameraState(pw);
    }

    public void onDeviceIdle() {
        this.mHandler.sendEmptyMessageDelayed(10, 300);
    }

    public boolean allowAcquireWakelock(String pkg, int flags, WorkSource ws, int ownerUid) {
        if (this.isScreenOff) {
            return this.mPartialWakeLock.allowAcquireWakelock(pkg, flags, ws, ownerUid);
        }
        return true;
    }
}
