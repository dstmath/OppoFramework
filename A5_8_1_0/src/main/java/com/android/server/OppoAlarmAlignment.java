package com.android.server;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.am.OppoAbnormalAppManager;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.job.controllers.JobStatus;

class OppoAlarmAlignment {
    private static final String ACTION_ALIGN_FIRST_DELAY = "com.oppo.intent.action.ALIGN_FIRST_DELAY";
    private static final String ACTION_ALIGN_TICK = "com.oppo.intent.action.ALIGN_TICK";
    private static final long ALIGN_INTERVAL = 300000;
    private static final int MSG_RETRY_ALIGN_TICK_SET = 3;
    private static final int MSG_SCREEN_OFF = 2;
    private static final int MSG_SCREEN_ON = 1;
    private static final String TAG = "AlarmManager_AlarmAlign";
    private final boolean ADBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private AlarmManagerService mAlarmMS;
    private AlignClockReceiver mAlignClockReceiver;
    private long mAlignFirstDelay = 0;
    private PendingIntent mAlignFirstDelaySender;
    private long mAlignInterval = 300000;
    private PendingIntent mAlignTickSender;
    private Context mContext;
    private WorkerHandler mHandler;
    private final Object mLock;
    private volatile boolean mNeedAlign = false;
    public volatile boolean mNeedRebatch = false;
    private OppoSysStateManager mSysState;
    private WakeLock mWakeLock;

    class AlignClockReceiver extends BroadcastReceiver {
        private volatile long mAlignElapsed = 0;

        public AlignClockReceiver(Context context, Handler handler) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(OppoAlarmAlignment.ACTION_ALIGN_TICK);
            filter.addAction(OppoAlarmAlignment.ACTION_ALIGN_FIRST_DELAY);
            context.registerReceiver(this, filter, null, handler);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OppoAlarmAlignment.ACTION_ALIGN_TICK)) {
                if (OppoAlarmAlignment.this.mAlarmMS.mInteractive || (OppoAlarmAlignment.this.mSysState.isCharging() ^ 1) == 0) {
                    Slog.d(OppoAlarmAlignment.TAG, "ACTION_ALIGN_TICK: mAlarmMS.mInteractive=" + OppoAlarmAlignment.this.mAlarmMS.mInteractive + ", mSysState.isCharging()=" + OppoAlarmAlignment.this.mSysState.isCharging());
                } else {
                    scheduleAlignTickEvent(OppoAlarmAlignment.this.mAlignInterval, false);
                }
            } else if (!intent.getAction().equals(OppoAlarmAlignment.ACTION_ALIGN_FIRST_DELAY)) {
            } else {
                if (OppoAlarmAlignment.this.mAlarmMS.mInteractive || (OppoAlarmAlignment.this.mSysState.isCharging() ^ 1) == 0) {
                    Slog.d(OppoAlarmAlignment.TAG, "ACTION_ALIGN_FIRST_DELAY: mAlarmMS.mInteractive=" + OppoAlarmAlignment.this.mAlarmMS.mInteractive + ", mSysState.isCharging()=" + OppoAlarmAlignment.this.mSysState.isCharging());
                    return;
                }
                if (OppoAlarmAlignment.this.ADBG) {
                    Slog.d(OppoAlarmAlignment.TAG, "ACTION_ALIGN_FIRST_DELAY: scheduleAlignTickEvent");
                }
                scheduleAlignTickEvent(OppoAlarmAlignment.this.mAlignInterval, true);
            }
        }

        public void scheduleAlignTickEvent(long alignInterval, boolean screenOffSwitch) {
            synchronized (OppoAlarmAlignment.this.mLock) {
                long nowElapsed = SystemClock.elapsedRealtime();
                if (screenOffSwitch) {
                    this.mAlignElapsed = nowElapsed;
                }
                long lastAlign = this.mAlignElapsed;
                long foundFirstElapsed = OppoAlarmAlignment.this.findFirstElapsedTimeWakeupNonStandalone(nowElapsed);
                if (JobStatus.NO_LATEST_RUNTIME == foundFirstElapsed) {
                    if (OppoAlarmAlignment.this.ADBG) {
                        Slog.d(OppoAlarmAlignment.TAG, "scheduleAlignTickEvent: Long.MAX_VALUE == foundFirstElapsed.");
                    }
                    this.mAlignElapsed += alignInterval;
                } else {
                    while (foundFirstElapsed > this.mAlignElapsed) {
                        this.mAlignElapsed += alignInterval;
                    }
                }
                if (this.mAlignElapsed == lastAlign) {
                    this.mAlignElapsed += alignInterval;
                    if (OppoAlarmAlignment.this.ADBG) {
                        Slog.d(OppoAlarmAlignment.TAG, "scheduleAlignTickEvent: add alignInterval.");
                    }
                }
                if (OppoAlarmAlignment.this.ADBG) {
                    Slog.d(OppoAlarmAlignment.TAG, "scheduleAlignTickEvent: nowElapsed=" + nowElapsed + ", mAlignElapsed=" + this.mAlignElapsed);
                }
                OppoAlarmAlignment.this.mNeedRebatch = true;
                if (screenOffSwitch) {
                    OppoAlarmAlignment.this.mNeedAlign = true;
                }
                OppoAlarmAlignment.this.mAlarmMS.setImplIntelnalLocked(3, this.mAlignElapsed, this.mAlignElapsed, 0, this.mAlignElapsed, 0, OppoAlarmAlignment.this.mAlignTickSender, 1073741832, false, null, null, UserHandle.getCallingUserId());
            }
        }

        public void cancelAlignTickEvent() {
            OppoAlarmAlignment.this.mAlarmMS.removeImpl(OppoAlarmAlignment.this.mAlignTickSender);
        }

        public void scheduleAlignFirstDelayEvent(long delay) {
            long nowElapsed = SystemClock.elapsedRealtime();
            long when = nowElapsed + delay;
            if (OppoAlarmAlignment.this.ADBG) {
                Slog.d(OppoAlarmAlignment.TAG, "scheduleAlignFirstDelayEvent: nowElapsed=" + nowElapsed + ", when=" + when);
            }
            synchronized (OppoAlarmAlignment.this.mLock) {
                OppoAlarmAlignment.this.mAlarmMS.setImplIntelnalLocked(3, when, when, 0, when, 0, OppoAlarmAlignment.this.mAlignFirstDelaySender, 8, false, null, null, UserHandle.getCallingUserId());
            }
        }

        public void cancelAlignFirstDelayEvent() {
            OppoAlarmAlignment.this.mAlarmMS.removeImpl(OppoAlarmAlignment.this.mAlignFirstDelaySender);
        }
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            OppoAlarmAlignment.this.mWakeLock.acquire();
            switch (msg.what) {
                case 1:
                    if (OppoAlarmAlignment.this.ADBG) {
                        Slog.d(OppoAlarmAlignment.TAG, "handleMessage: MSG_SCREEN_ON");
                    }
                    OppoAlarmAlignment.this.mAlignClockReceiver.cancelAlignTickEvent();
                    OppoAlarmAlignment.this.mAlignClockReceiver.cancelAlignFirstDelayEvent();
                    OppoAlarmAlignment.this.mHandler.removeMessages(3);
                    OppoAlarmAlignment.this.mNeedAlign = false;
                    break;
                case 2:
                    OppoAlarmAlignment.this.mAlignInterval = (OppoAlarmManagerHelper.getAlignInterval() * 60) * 1000;
                    OppoAlarmAlignment.this.mAlignFirstDelay = (OppoAlarmManagerHelper.getAlignFirstDelay() * 60) * 1000;
                    if (OppoAlarmAlignment.this.ADBG) {
                        Slog.d(OppoAlarmAlignment.TAG, "handleMessage: MSG_SCREEN_OFF, mAlignInterval=" + OppoAlarmAlignment.this.mAlignInterval + "ms, mAlignFirstDelay=" + OppoAlarmAlignment.this.mAlignFirstDelay + "ms");
                    }
                    if (!OppoAlarmAlignment.this.mSysState.isCharging()) {
                        if (0 != OppoAlarmAlignment.this.mAlignFirstDelay) {
                            OppoAlarmAlignment.this.mAlignClockReceiver.scheduleAlignFirstDelayEvent(OppoAlarmAlignment.this.mAlignFirstDelay);
                            break;
                        } else {
                            OppoAlarmAlignment.this.mAlignClockReceiver.scheduleAlignTickEvent(OppoAlarmAlignment.this.mAlignInterval, true);
                            break;
                        }
                    } else if (OppoAlarmAlignment.this.ADBG) {
                        Slog.d(OppoAlarmAlignment.TAG, "handleMessage: MSG_SCREEN_OFF. isCharging=" + OppoAlarmAlignment.this.mSysState.isCharging());
                        break;
                    }
                    break;
                case 3:
                    if (OppoAlarmAlignment.this.ADBG) {
                        Slog.d(OppoAlarmAlignment.TAG, "handleMessage: MSG_RETRY_ALIGN_TICK_SET, mNeedAlign=" + OppoAlarmAlignment.this.mNeedAlign);
                    }
                    if (OppoAlarmAlignment.this.mNeedAlign) {
                        OppoAlarmAlignment.this.mAlignClockReceiver.scheduleAlignTickEvent(OppoAlarmAlignment.this.mAlignInterval, false);
                        break;
                    }
                    break;
            }
            OppoAlarmAlignment.this.mWakeLock.release();
        }
    }

    public OppoAlarmAlignment(Context context, Object lock, AlarmManagerService alarmMS, Looper loop) {
        this.mContext = context;
        this.mLock = lock;
        this.mAlarmMS = alarmMS;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "alarmAlign");
        this.mWakeLock.setReferenceCounted(false);
        this.mHandler = new WorkerHandler(loop);
        this.mSysState = OppoSysStateManager.getInstance();
        this.mAlignTickSender = PendingIntent.getBroadcastAsUser(context, 0, new Intent(ACTION_ALIGN_TICK).addFlags(1342177280), 0, UserHandle.ALL);
        this.mAlignFirstDelaySender = PendingIntent.getBroadcastAsUser(context, 0, new Intent(ACTION_ALIGN_FIRST_DELAY).addFlags(1342177280), 0, UserHandle.ALL);
        this.mAlignClockReceiver = new AlignClockReceiver(context, this.mHandler);
    }

    public void onScreenOn() {
        this.mHandler.sendEmptyMessageDelayed(1, 50);
    }

    public void onScreenOff() {
        this.mHandler.sendEmptyMessageDelayed(2, 50);
    }

    public boolean isAlignTick(int flags) {
        return (1073741824 & flags) != 0;
    }

    public void reScheduleAlignTick() {
        if (!this.mAlarmMS.mInteractive && (this.mSysState.isCharging() ^ 1) != 0 && this.mNeedAlign) {
            this.mHandler.removeMessages(3);
            this.mHandler.sendEmptyMessageDelayed(3, 50);
        }
    }

    public void alignWithSys(Alarm a) {
        if (!this.mAlarmMS.mInteractive && (this.mNeedAlign ^ 1) == 0 && !this.mSysState.isCharging() && (a.flags & 1) == 0) {
            long alignElapsed = this.mAlignClockReceiver.mAlignElapsed;
            long alarmWhenElapsed = a.whenElapsed;
            if (a.whenElapsedAllowWhileIdle != 0) {
                alarmWhenElapsed = a.whenElapsedAllowWhileIdle;
                a.whenElapsedAllowWhileIdle = 0;
            }
            if (alarmWhenElapsed <= alignElapsed) {
                if ((alarmWhenElapsed != alignElapsed || a.maxWhenElapsed != alignElapsed) && !ignoreAlarm(a)) {
                    while (alarmWhenElapsed <= alignElapsed - this.mAlignInterval) {
                        alignElapsed -= this.mAlignInterval;
                    }
                    long nowElapsed = SystemClock.elapsedRealtime();
                    if (alignElapsed >= alarmWhenElapsed && alignElapsed >= nowElapsed && alignElapsed > 0) {
                        a.whenElapsed = alignElapsed;
                        a.maxWhenElapsed = alignElapsed;
                    }
                }
            }
        }
    }

    private boolean ignoreAlarm(Alarm a) {
        String pkgName;
        boolean ignore = false;
        if (a.operation != null) {
            pkgName = a.operation.getTargetPackage();
        } else {
            pkgName = a.packageName;
        }
        if (pkgName == null) {
            if (this.ADBG) {
                Slog.d(TAG, "ignoreAlarm: pkg name is null");
            }
            return false;
        }
        int uid = UserHandle.getAppId(a.creatorUid);
        if (OppoAbnormalAppManager.getInstance().isNotRestrictApp(pkgName) && OppoAlarmManagerHelper.isInAlignWhiteList(pkgName)) {
            ignore = true;
        } else if (OppoAlarmManagerHelper.containKeyWord(pkgName)) {
            ignore = true;
        } else if (OppoAlarmManagerHelper.isInAlignEnforcedWhiteList(pkgName)) {
            ignore = true;
        }
        return ignore;
    }

    private Batch findFirstWakeupNonStandaloneBatch() {
        synchronized (this.mLock) {
            int N = this.mAlarmMS.mAlarmBatches.size();
            for (int i = 0; i < N; i++) {
                Batch b = (Batch) this.mAlarmMS.mAlarmBatches.get(i);
                if (b.hasWakeups() && (b.flags & 1) == 0) {
                    return b;
                }
            }
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:41:0x00b9, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private long findFirstElapsedTimeWakeupNonStandalone(long nowElapsed) {
        synchronized (this.mLock) {
            int i;
            long whenElapsed = JobStatus.NO_LATEST_RUNTIME;
            Batch batch = null;
            int NB = this.mAlarmMS.mAlarmBatches.size();
            for (i = 0; i < NB; i++) {
                Batch b = (Batch) this.mAlarmMS.mAlarmBatches.get(i);
                if (b.hasWakeups() && (b.flags & 1) == 0) {
                    batch = b;
                    break;
                }
            }
            if (batch == null) {
                return JobStatus.NO_LATEST_RUNTIME;
            }
            int NA = batch.size();
            for (i = 0; i < NA; i++) {
                Alarm a = batch.get(i);
                if ((a.type == 2 || a.type == 0) && (a.flags & 1) == 0 && a.whenElapsed > nowElapsed && a.whenElapsed < whenElapsed) {
                    whenElapsed = a.whenElapsed;
                }
            }
            if (this.ADBG && JobStatus.NO_LATEST_RUNTIME == whenElapsed) {
                Slog.d(TAG, "findFirstElapsedTimeWakeupNonStandalone: Batch =" + batch);
                for (i = 0; i < NA; i++) {
                    Slog.d(TAG, "alarm(" + i + ") =" + batch.get(i));
                }
            }
        }
    }
}
