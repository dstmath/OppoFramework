package com.android.server.power;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.WorkSource;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.power.PowerManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ColorWakeLockCheck implements IColorWakeLockCheck {
    private static final boolean ADBG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) ADBG);
    public static final String ATAG = "OppoWakeLockCheck";
    private static final long DEVICEIDLE_ON_CHECK_DELAY = 300;
    private static final int MSG_DEVICEIDLE_ON = 10;
    private static final int MSG_PARTIAL_WAKELOCK_ACQUIRE = 3;
    private static final int MSG_PARTIAL_WAKELOCK_CHECK = 1;
    private static final int MSG_PARTIAL_WAKELOCK_RELEASE = 4;
    private static final int MSG_SCREEN_OFF = 5;
    private static final int MSG_SCREEN_ON = 6;
    private static final int MSG_SCREEN_ON_WAKELOCK_CHECK = 2;
    private static final long SCREEN_ON_WAKELOCK_CHECK_DELAY = 30000;
    private volatile boolean isScreenOff = ADBG;
    private CommonUtil mCommonUtil = null;
    private WorkerHandler mHandler;
    private ColorPartialWakeLockCheck mPartialWakeLock = null;
    private ColorScreenOnWakeLockCheck mScreenOnWakeLock = null;
    private SuspendBlocker mSuspendBlocker = null;
    private boolean msgPartialWakelockSent = ADBG;
    private boolean msgScreenOnWakelockSent = ADBG;

    public void initArgs(ArrayList<PowerManagerService.WakeLock> wakeLocks, Object lock, Context context, PowerManagerService pms, SuspendBlocker suspendBlocker) {
        HandlerThread ht = new HandlerThread(ATAG);
        ht.start();
        this.mHandler = new WorkerHandler(ht.getLooper());
        this.mSuspendBlocker = suspendBlocker;
        this.mCommonUtil = new CommonUtil(context);
        this.mPartialWakeLock = new ColorPartialWakeLockCheck(wakeLocks, lock, context, pms, this.mCommonUtil, ADBG, this.mHandler);
        this.mScreenOnWakeLock = new ColorScreenOnWakeLockCheck(wakeLocks, lock, context, pms, this.mCommonUtil, ADBG);
    }

    public void PartialWakelockCheckStart() {
        if (!this.msgPartialWakelockSent) {
            this.msgPartialWakelockSent = true;
            this.mPartialWakeLock.clearSyncWakelock();
            this.mHandler.sendEmptyMessage(5);
        }
        this.isScreenOff = true;
    }

    public ArrayList<Integer> getMusicPlayerList() {
        return this.mPartialWakeLock.getMusicPlayerList();
    }

    public void PartialWakelockCheckStop() {
        if (this.msgPartialWakelockSent) {
            this.msgPartialWakelockSent = ADBG;
            this.mPartialWakeLock.clearSyncWakelock();
            this.mHandler.sendEmptyMessage(6);
        }
        this.isScreenOff = ADBG;
    }

    public void screenOnWakelockCheckStart() {
        if (!this.msgScreenOnWakelockSent) {
            WorkerHandler workerHandler = this.mHandler;
            workerHandler.sendMessageDelayed(workerHandler.obtainMessage(2), 30000);
            this.msgScreenOnWakelockSent = true;
        }
    }

    public void screenOnWakelockCheckStop() {
        if (this.msgScreenOnWakelockSent) {
            this.mHandler.removeMessages(2);
            this.msgScreenOnWakelockSent = ADBG;
        }
    }

    public void noteWakeLockChange(PowerManagerService.WakeLock wl, boolean acquire) {
        int msg;
        if (acquire) {
            msg = 3;
        } else {
            msg = 4;
        }
        this.mPartialWakeLock.noteWakeLockChange(wl, msg, this.mHandler, wl.mWorkSource);
    }

    public void noteWorkSourceChange(PowerManagerService.WakeLock wl, WorkSource newWorkSource) {
        this.mPartialWakeLock.noteWakeLockChange(wl, 4, this.mHandler, wl.mWorkSource);
        this.mPartialWakeLock.noteWakeLockChange(wl, 3, this.mHandler, newWorkSource);
    }

    public boolean canSyncWakeLockAcq(int uid, String tag) {
        return this.mPartialWakeLock.canSyncWakeLockAcq(uid, tag);
    }

    public boolean allowAcquireWakelock(String pkg, int flags, WorkSource ws, int ownerUid) {
        return true;
    }

    public void allowAcquireShortimeHandle(IBinder lock, String pkgRelease, int flags, WorkSource ws, int ownerUid) {
        this.mPartialWakeLock.allowAcquireShortimeHandle(lock, pkgRelease, flags, ws, ownerUid);
    }

    public void onDeviceIdle() {
        this.mHandler.sendEmptyMessageDelayed(10, DEVICEIDLE_ON_CHECK_DELAY);
    }

    public void logSwitch(boolean enable) {
        this.mPartialWakeLock.logSwitch(enable);
    }

    public void dumpPossibleMusicPlayer(PrintWriter pw) {
        this.mPartialWakeLock.dumpPossibleMusicPlayer(pw);
    }

    public void dumpCameraState(PrintWriter pw) {
        this.mPartialWakeLock.dumpCameraState(pw);
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ColorWakeLockCheck.this.mSuspendBlocker.acquire();
                    ColorWakeLockCheck.this.mPartialWakeLock.wakeLockHeldContinuouslyCheck(1);
                    ColorWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 2:
                    ColorWakeLockCheck.this.mSuspendBlocker.acquire();
                    ColorWakeLockCheck.this.mScreenOnWakeLock.check();
                    ColorWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 3:
                    ColorWakeLockCheck.this.mPartialWakeLock.noteStartWakeLock(msg.getData());
                    return;
                case 4:
                    ColorWakeLockCheck.this.mSuspendBlocker.acquire();
                    ColorWakeLockCheck.this.mPartialWakeLock.noteStopWakeLock(msg.getData());
                    ColorWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 5:
                    ColorWakeLockCheck.this.mSuspendBlocker.acquire();
                    ColorWakeLockCheck.this.mPartialWakeLock.onScreenOff(1);
                    ColorWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 6:
                    ColorWakeLockCheck.this.mSuspendBlocker.acquire();
                    ColorWakeLockCheck.this.mPartialWakeLock.onScreenOn(1);
                    ColorWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /* 7 */:
                    ColorWakeLockCheck.this.mSuspendBlocker.acquire();
                    ColorWakeLockCheck.this.mPartialWakeLock.wakeLockTimeout(msg);
                    ColorWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 8:
                    ColorWakeLockCheck.this.mPartialWakeLock.relasShortTimeWl(msg.getData());
                    return;
                case ColorStartingWindowRUSHelper.FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT /* 9 */:
                    ColorWakeLockCheck.this.mSuspendBlocker.acquire();
                    ColorWakeLockCheck.this.mPartialWakeLock.possiblePlayer(msg);
                    ColorWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 10:
                    ColorWakeLockCheck.this.mSuspendBlocker.acquire();
                    ColorWakeLockCheck.this.mPartialWakeLock.onDeviceIdle();
                    ColorWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                default:
                    return;
            }
        }
    }
}
