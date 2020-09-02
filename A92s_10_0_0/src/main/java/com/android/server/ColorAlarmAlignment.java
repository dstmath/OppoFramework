package com.android.server;

import android.app.AlarmManager;
import android.app.IAlarmListener;
import android.app.PendingIntent;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import com.android.server.OppoBaseAlarmManagerService;
import com.android.server.am.IColorAbnormalAppManager;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;

public class ColorAlarmAlignment implements IColorAlarmAlignment {
    private static final String ACTION_ALIGN_FIRST_DELAY = "com.oppo.intent.action.ALIGN_FIRST_DELAY";
    private static final String ACTION_ALIGN_TICK = "com.oppo.intent.action.ALIGN_TICK";
    private static final long ALIGN_INTERVAL = 300000;
    private static final int MSG_RETRY_ALIGN_TICK_SET = 3;
    private static final int MSG_SCREEN_OFF = 2;
    private static final int MSG_SCREEN_ON = 1;
    public static final String TAG = "OppoAlarmAlignment";
    private static ColorAlarmAlignment mInstance = null;
    /* access modifiers changed from: private */
    public final boolean ADBG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    /* access modifiers changed from: private */
    public ColorAcmeAlarmPolicy mAcmeAlarmPolicy = null;
    /* access modifiers changed from: private */
    public AlarmManagerService mAlarmMS;
    /* access modifiers changed from: private */
    public AlignClockReceiver mAlignClockReceiver;
    /* access modifiers changed from: private */
    public long mAlignFirstDelay = 0;
    /* access modifiers changed from: private */
    public PendingIntent mAlignFirstDelaySender;
    /* access modifiers changed from: private */
    public long mAlignInterval = ALIGN_INTERVAL;
    /* access modifiers changed from: private */
    public PendingIntent mAlignTickSender;
    private OppoBaseAlarmManagerService mBaseAlarmMS;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public WorkerHandler mHandler;
    /* access modifiers changed from: private */
    public volatile boolean mInAcmeState = false;
    private boolean mInit = false;
    /* access modifiers changed from: private */
    public Object mLock;
    /* access modifiers changed from: private */
    public volatile boolean mNeedAlign = false;
    public volatile boolean mNeedRebatch = false;
    /* access modifiers changed from: private */
    public long mScreenOffElapsed = 0;
    /* access modifiers changed from: private */
    public OppoSysStateManager mSysState;
    /* access modifiers changed from: private */
    public PowerManager.WakeLock mWakeLock;

    private ColorAlarmAlignment() {
    }

    public static ColorAlarmAlignment getInstance() {
        if (mInstance == null) {
            synchronized (ColorAlarmAlignment.class) {
                if (mInstance == null) {
                    mInstance = new ColorAlarmAlignment();
                }
            }
        }
        return mInstance;
    }

    public void initArgs(Context context, Object lock, AlarmManagerService alarmMS, Looper loop) {
        this.mContext = context;
        this.mLock = lock;
        this.mAlarmMS = alarmMS;
        this.mBaseAlarmMS = typeCasting(alarmMS);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "alarmAlign");
        this.mWakeLock.setReferenceCounted(false);
        this.mHandler = new WorkerHandler(loop);
        this.mSysState = OppoSysStateManager.getInstance();
        this.mAlignTickSender = PendingIntent.getBroadcastAsUser(context, 0, new Intent(ACTION_ALIGN_TICK).addFlags(1342177280), 0, UserHandle.ALL);
        this.mAlignFirstDelaySender = PendingIntent.getBroadcastAsUser(context, 0, new Intent(ACTION_ALIGN_FIRST_DELAY).addFlags(1342177280), 0, UserHandle.ALL);
        this.mAlignClockReceiver = new AlignClockReceiver(context, this.mHandler);
        this.mAcmeAlarmPolicy = new ColorAcmeAlarmPolicy();
        this.mAcmeAlarmPolicy.init(this.mContext, this.mHandler);
        this.mInit = true;
    }

    /* access modifiers changed from: package-private */
    public class AlignClockReceiver extends BroadcastReceiver {
        /* access modifiers changed from: private */
        public volatile long mAlignElapsed = 0;

        public AlignClockReceiver(Context context, Handler handler) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ColorAlarmAlignment.ACTION_ALIGN_TICK);
            filter.addAction(ColorAlarmAlignment.ACTION_ALIGN_FIRST_DELAY);
            context.registerReceiver(this, filter, null, handler);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ColorAlarmAlignment.ACTION_ALIGN_TICK)) {
                if (ColorAlarmAlignment.this.mAlarmMS.mInteractive || ColorAlarmAlignment.this.mSysState.isCharging()) {
                    Slog.d(ColorAlarmAlignment.TAG, "ACTION_ALIGN_TICK: mAlarmMS.mInteractive=" + ColorAlarmAlignment.this.mAlarmMS.mInteractive + ", mSysState.isCharging()=" + ColorAlarmAlignment.this.mSysState.isCharging());
                    return;
                }
                scheduleAlignTickEvent(ColorAlarmAlignment.this.mAlignInterval, false);
            } else if (!intent.getAction().equals(ColorAlarmAlignment.ACTION_ALIGN_FIRST_DELAY)) {
            } else {
                if (ColorAlarmAlignment.this.mAlarmMS.mInteractive || ColorAlarmAlignment.this.mSysState.isCharging()) {
                    Slog.d(ColorAlarmAlignment.TAG, "ACTION_ALIGN_FIRST_DELAY: mAlarmMS.mInteractive=" + ColorAlarmAlignment.this.mAlarmMS.mInteractive + ", mSysState.isCharging()=" + ColorAlarmAlignment.this.mSysState.isCharging());
                    return;
                }
                if (ColorAlarmAlignment.this.ADBG) {
                    Slog.d(ColorAlarmAlignment.TAG, "ACTION_ALIGN_FIRST_DELAY: scheduleAlignTickEvent");
                }
                scheduleAlignTickEvent(ColorAlarmAlignment.this.mAlignInterval, true);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0030, code lost:
            return;
         */
        public void scheduleAlignTickEvent(long alignInterval, boolean screenOffSwitch) {
            long alignInterval2 = alignInterval;
            synchronized (ColorAlarmAlignment.this.mLock) {
                if (alignInterval2 <= 0) {
                    try {
                        if (ColorAlarmAlignment.this.ADBG) {
                            Slog.d(ColorAlarmAlignment.TAG, "scheduleAlignTickEvent: alignInterval = " + alignInterval2);
                        }
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    long nowElapsed = SystemClock.elapsedRealtime();
                    if (screenOffSwitch) {
                        this.mAlignElapsed = nowElapsed;
                    }
                    ColorAlarmAlignment.this.mAcmeAlarmPolicy.triggerAlignTickEvent(ColorAlarmAlignment.this.mContext, nowElapsed, ColorAlarmAlignment.this.mScreenOffElapsed);
                    boolean restrict = ColorAlarmAlignment.this.mAcmeAlarmPolicy.isInAcmeState();
                    if (ColorAlarmAlignment.this.mInAcmeState != restrict) {
                        boolean unused = ColorAlarmAlignment.this.mInAcmeState = restrict;
                        if (restrict) {
                            alignInterval2 = ColorAlarmAlignment.this.mAlignInterval = ColorAlarmManagerHelper.getInstance().getAcmeAlignInterval() * 60 * 1000;
                            try {
                                Slog.d(ColorAlarmAlignment.TAG, "scheduleAlignTickEvent, change align interval to " + ColorAlarmAlignment.this.mAlignInterval + " for acme restrict");
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } else {
                            alignInterval2 = ColorAlarmAlignment.this.mAlignInterval = ColorAlarmManagerHelper.getInstance().getAlignInterval() * 60 * 1000;
                            Slog.d(ColorAlarmAlignment.TAG, "scheduleAlignTickEvent, restore align interval to " + ColorAlarmAlignment.this.mAlignInterval + " for normal restrict");
                        }
                    }
                    long lastAlign = this.mAlignElapsed;
                    long foundFirstElapsed = ColorAlarmAlignment.this.findFirstElapsedTimeWakeupNonStandalone(nowElapsed);
                    if (Long.MAX_VALUE == foundFirstElapsed) {
                        if (ColorAlarmAlignment.this.ADBG) {
                            Slog.d(ColorAlarmAlignment.TAG, "scheduleAlignTickEvent: Long.MAX_VALUE == foundFirstElapsed.");
                        }
                        this.mAlignElapsed += alignInterval2;
                    } else if (foundFirstElapsed > this.mAlignElapsed) {
                        this.mAlignElapsed = (alignInterval2 - ((foundFirstElapsed - this.mAlignElapsed) % alignInterval2)) + foundFirstElapsed;
                    }
                    if (this.mAlignElapsed == lastAlign) {
                        this.mAlignElapsed += alignInterval2;
                        if (ColorAlarmAlignment.this.ADBG) {
                            Slog.d(ColorAlarmAlignment.TAG, "scheduleAlignTickEvent: add alignInterval.");
                        }
                    }
                    if (ColorAlarmAlignment.this.ADBG) {
                        Slog.d(ColorAlarmAlignment.TAG, "scheduleAlignTickEvent: nowElapsed=" + nowElapsed + ", mAlignElapsed=" + this.mAlignElapsed);
                    }
                    ColorAlarmAlignment.this.mNeedRebatch = true;
                    if (screenOffSwitch) {
                        boolean unused2 = ColorAlarmAlignment.this.mNeedAlign = true;
                    }
                    ColorAlarmAlignment.this.getInner().setImplIntelnalLocked(3, this.mAlignElapsed, this.mAlignElapsed, 0, this.mAlignElapsed, 0, ColorAlarmAlignment.this.mAlignTickSender, 1073741832, false, (WorkSource) null, (AlarmManager.AlarmClockInfo) null, UserHandle.getCallingUserId());
                }
            }
        }

        public void cancelAlignTickEvent() {
            ColorAlarmAlignment.this.mAlarmMS.removeImpl(ColorAlarmAlignment.this.mAlignTickSender, (IAlarmListener) null);
        }

        public void scheduleAlignFirstDelayEvent(long delay) {
            long nowElapsed = SystemClock.elapsedRealtime();
            long when = nowElapsed + delay;
            if (ColorAlarmAlignment.this.ADBG) {
                Slog.d(ColorAlarmAlignment.TAG, "scheduleAlignFirstDelayEvent: nowElapsed=" + nowElapsed + ", when=" + when);
            }
            synchronized (ColorAlarmAlignment.this.mLock) {
                try {
                    ColorAlarmAlignment.this.getInner().setImplIntelnalLocked(3, when, when, 0, when, 0, ColorAlarmAlignment.this.mAlignFirstDelaySender, 8, false, (WorkSource) null, (AlarmManager.AlarmClockInfo) null, UserHandle.getCallingUserId());
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }

        public void cancelAlignFirstDelayEvent() {
            ColorAlarmAlignment.this.mAlarmMS.removeImpl(ColorAlarmAlignment.this.mAlignFirstDelaySender, (IAlarmListener) null);
        }
    }

    public void onScreenOn() {
        if (isInit()) {
            this.mHandler.sendEmptyMessageDelayed(1, 50);
        }
    }

    public void onScreenOff() {
        if (isInit()) {
            this.mHandler.sendEmptyMessageDelayed(2, 50);
        }
    }

    public boolean isAlignTick(int flags) {
        return (1073741824 & flags) != 0;
    }

    public void reScheduleAlignTick() {
        if (isInit() && !this.mAlarmMS.mInteractive && !this.mSysState.isCharging() && this.mNeedAlign) {
            this.mHandler.removeMessages(3);
            this.mHandler.sendEmptyMessageDelayed(3, 50);
        }
    }

    public void alignWithSys(AlarmManagerService.Alarm a) {
        if (!isInit() || this.mAlarmMS.mInteractive || !this.mNeedAlign || this.mSysState.isCharging()) {
            return;
        }
        if (((a.flags & 1) == 0 || this.mInAcmeState) && this.mAlignInterval > 0) {
            OppoBaseAlarmManagerService.BaseAlarm baseAlarm = typeCasting(a);
            long alignElapsed = this.mAlignClockReceiver.mAlignElapsed;
            long alarmWhenElapsed = a.whenElapsed;
            if (!(baseAlarm == null || baseAlarm.whenElapsedAllowWhileIdle == 0)) {
                alarmWhenElapsed = baseAlarm.whenElapsedAllowWhileIdle;
                baseAlarm.whenElapsedAllowWhileIdle = 0;
            }
            if (alarmWhenElapsed <= alignElapsed) {
                if (!(alarmWhenElapsed == alignElapsed && a.maxWhenElapsed == alignElapsed) && !ignoreAlarm(a)) {
                    long alignElapsed2 = ((alignElapsed - alarmWhenElapsed) % this.mAlignInterval) + alarmWhenElapsed;
                    long nowElapsed = SystemClock.elapsedRealtime();
                    if (alignElapsed2 < alarmWhenElapsed || alignElapsed2 < nowElapsed || alignElapsed2 <= 0) {
                        OppoBaseAlarmManagerService oppoBaseAlarmManagerService = this.mBaseAlarmMS;
                        if (oppoBaseAlarmManagerService == null || !oppoBaseAlarmManagerService.getRestoringFlagForAlarmAlignment()) {
                            long j = this.mScreenOffElapsed;
                            long j2 = this.mAlignInterval;
                            if (nowElapsed - j >= j2 || alignElapsed2 >= j - j2) {
                                alignElapsed2 = this.mAlignClockReceiver.mAlignElapsed;
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                    a.whenElapsed = alignElapsed2;
                    a.maxWhenElapsed = alignElapsed2;
                }
            }
        }
    }

    public boolean isNeedRebatch() {
        return this.mNeedRebatch;
    }

    public void setNeedRebatch(boolean isNeed) {
        this.mNeedRebatch = isNeed;
    }

    private boolean ignoreAlarm(AlarmManagerService.Alarm a) {
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
        UserHandle.getAppId(a.creatorUid);
        boolean isNotRestrictApp = OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).isNotRestrictApp(pkgName);
        boolean alignWhiteList = OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).isInAlignWhiteList(pkgName);
        boolean keyWord = OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).containKeyWord(pkgName);
        boolean alignEnforcedWhiteList = OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).isInAlignEnforcedWhiteList(pkgName);
        if (isNotRestrictApp && alignWhiteList) {
            ignore = true;
        } else if (keyWord) {
            ignore = true;
        } else if (alignEnforcedWhiteList) {
            ignore = true;
        }
        if (keyWord || !this.mInAcmeState) {
            return ignore;
        }
        boolean restricted = this.mAcmeAlarmPolicy.isRestrictedByAcme(this.mContext, a, pkgName, alignWhiteList && isNotRestrictApp, alignEnforcedWhiteList);
        if (this.ADBG) {
            Slog.d(TAG, a.toString() + " acme" + restricted + ", normal = " + ignore);
        }
        return !restricted;
    }

    private AlarmManagerService.Batch findFirstWakeupNonStandaloneBatch() {
        synchronized (this.mLock) {
            int N = this.mAlarmMS.mAlarmBatches.size();
            for (int i = 0; i < N; i++) {
                AlarmManagerService.Batch b = (AlarmManagerService.Batch) this.mAlarmMS.mAlarmBatches.get(i);
                if (b.hasWakeups() && (b.flags & 1) == 0) {
                    return b;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00ac, code lost:
        return r1;
     */
    public long findFirstElapsedTimeWakeupNonStandalone(long nowElapsed) {
        synchronized (this.mLock) {
            long whenElapsed = Long.MAX_VALUE;
            AlarmManagerService.Batch batch = null;
            int NB = this.mAlarmMS.mAlarmBatches.size();
            int i = 0;
            while (true) {
                if (i >= NB) {
                    break;
                }
                AlarmManagerService.Batch b = (AlarmManagerService.Batch) this.mAlarmMS.mAlarmBatches.get(i);
                if (b.hasWakeups() && (b.flags & 1) == 0) {
                    batch = b;
                    break;
                }
                i++;
            }
            if (batch == null) {
                return Long.MAX_VALUE;
            }
            int NA = batch.size();
            for (int i2 = 0; i2 < NA; i2++) {
                AlarmManagerService.Alarm a = batch.get(i2);
                if ((a.type == 2 || a.type == 0) && (a.flags & 1) == 0 && a.whenElapsed > nowElapsed && a.whenElapsed < whenElapsed) {
                    whenElapsed = a.whenElapsed;
                }
            }
            if (this.ADBG && Long.MAX_VALUE == whenElapsed) {
                Slog.d(TAG, "findFirstElapsedTimeWakeupNonStandalone: Batch =" + batch);
                for (int i3 = 0; i3 < NA; i3++) {
                    AlarmManagerService.Alarm a2 = batch.get(i3);
                    Slog.d(TAG, "alarm(" + i3 + ") =" + a2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ColorAlarmAlignment.this.mWakeLock.acquire();
            int i = msg.what;
            if (i == 1) {
                if (ColorAlarmAlignment.this.ADBG) {
                    Slog.d(ColorAlarmAlignment.TAG, "handleMessage: MSG_SCREEN_ON");
                }
                ColorAlarmAlignment.this.mAlignClockReceiver.cancelAlignTickEvent();
                ColorAlarmAlignment.this.mAlignClockReceiver.cancelAlignFirstDelayEvent();
                ColorAlarmAlignment.this.mHandler.removeMessages(3);
                boolean unused = ColorAlarmAlignment.this.mNeedAlign = false;
                ColorAlarmAlignment.this.mAcmeAlarmPolicy.triggerScreenOn(ColorAlarmAlignment.this.mContext);
            } else if (i == 2) {
                long unused2 = ColorAlarmAlignment.this.mAlignInterval = OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).getAlignInterval() * 60 * 1000;
                long unused3 = ColorAlarmAlignment.this.mAlignFirstDelay = OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).getAlignFirstDelay() * 60 * 1000;
                if (ColorAlarmAlignment.this.ADBG) {
                    Slog.d(ColorAlarmAlignment.TAG, "handleMessage: MSG_SCREEN_OFF, mAlignInterval=" + ColorAlarmAlignment.this.mAlignInterval + "ms, mAlignFirstDelay=" + ColorAlarmAlignment.this.mAlignFirstDelay + "ms");
                }
                long unused4 = ColorAlarmAlignment.this.mScreenOffElapsed = SystemClock.elapsedRealtime();
                if (!ColorAlarmAlignment.this.mSysState.isCharging()) {
                    if (0 == ColorAlarmAlignment.this.mAlignFirstDelay) {
                        ColorAlarmAlignment.this.mAlignClockReceiver.scheduleAlignTickEvent(ColorAlarmAlignment.this.mAlignInterval, true);
                    } else {
                        ColorAlarmAlignment.this.mAlignClockReceiver.scheduleAlignFirstDelayEvent(ColorAlarmAlignment.this.mAlignFirstDelay);
                    }
                    ColorAlarmAlignment.this.mAcmeAlarmPolicy.triggerScreenOff(ColorAlarmAlignment.this.mContext);
                } else if (ColorAlarmAlignment.this.ADBG) {
                    Slog.d(ColorAlarmAlignment.TAG, "handleMessage: MSG_SCREEN_OFF. isCharging=" + ColorAlarmAlignment.this.mSysState.isCharging());
                }
            } else if (i == 3) {
                if (ColorAlarmAlignment.this.ADBG) {
                    Slog.d(ColorAlarmAlignment.TAG, "handleMessage: MSG_RETRY_ALIGN_TICK_SET, mNeedAlign=" + ColorAlarmAlignment.this.mNeedAlign);
                }
                if (ColorAlarmAlignment.this.mNeedAlign) {
                    ColorAlarmAlignment.this.mAlignClockReceiver.scheduleAlignTickEvent(ColorAlarmAlignment.this.mAlignInterval, false);
                }
            }
            ColorAlarmAlignment.this.mWakeLock.release();
        }
    }

    private boolean isInit() {
        if (this.mInit) {
            return true;
        }
        Slog.e(TAG, "service no init");
        return false;
    }

    private static OppoBaseAlarmManagerService.BaseAlarm typeCasting(AlarmManagerService.Alarm alarm) {
        if (alarm != null) {
            return (OppoBaseAlarmManagerService.BaseAlarm) ColorTypeCastingHelper.typeCasting(OppoBaseAlarmManagerService.BaseAlarm.class, alarm);
        }
        return null;
    }

    private static OppoBaseAlarmManagerService typeCasting(AlarmManagerService ams) {
        if (ams != null) {
            return (OppoBaseAlarmManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseAlarmManagerService.class, ams);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public IColorAlarmManagerServiceInner getInner() {
        OppoBaseAlarmManagerService baseAlarm = typeCasting(this.mAlarmMS);
        if (baseAlarm == null || baseAlarm.mColorAlarmMSInner == null) {
            return IColorAlarmManagerServiceInner.DEFAULT;
        }
        return baseAlarm.mColorAlarmMSInner;
    }
}
