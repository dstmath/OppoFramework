package com.android.server;

import android.app.PendingIntent;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.AlarmManagerService;
import com.android.server.OppoBaseAlarmManagerService;
import com.android.server.UiModeManagerService;
import com.android.server.am.IColorAbnormalAppManager;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import com.android.server.voiceinteraction.DatabaseHelper;
import com.color.util.ColorTypeCastingHelper;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* access modifiers changed from: package-private */
public class OppoAlarmWakeupDetection {
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR = "android.intent.action.OPPO_GUARD_ELF_MONITOR";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final long ADVANCE_TIME = 10;
    private static final String ATAG = "AlarmWakeupCheck";
    private static final boolean DBG = false;
    private static final int MAX_FREQUENT_ALARM_RCD_SIZE = 20;
    private static final int MAX_INTENT_LIST_SIZE = 3;
    private static final int MAX_UPLOAD_NUM = 2;
    private static final int MIN_ALARM_NUM = 15;
    private static final int MIN_NUM_ALARM_RCD = 5;
    private static final long MIN_SCREEN_OFF_INTERVAL = 3300000;
    private static final int MSG_CHECK_ALARM_WAKEUP = 101;
    private static final int MSG_WAKEUP_NUM_RECORD = 102;
    private static final boolean REAL_WAKEUP_CHECK = true;
    private static final long THRESHOLD_REPORT_DCS = 90000;
    private static final long THRESHOLD_RESET_CHECK = 1200000;
    private static final long THRESHOLD_SERIOUS_SEC_PER_ALARM = 180;
    private static final long THRESHOLD_SERIOUS_SEC_PER_WAKEUP = 300;
    private static final long THRESHOLD_WARNNING_SEC_PER_WAKEUP = 360;
    private static final long THRESHOLD_WORST_SEC_PER_WAKEUP = 60;
    private final List<String> ACTION_SYSTEMUI_NOT_RESTRICT_ALARM = Arrays.asList("com.android.dreams.alwaysondisplay.ACTION_AOD", "com.android.dreams.alwaysondisplay.ACTION_WAKE_AOD", "com.oppo.systemui.osfp.updateui", "com.android.systemui.aod.UPDATE_TIME", "com.android.systemui.aod.WAKEUP_TIME", "com.android.systemui.aod.HIDE_TIME");
    private final boolean ADBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private final String SYSTEM_UI_PKG = "com.android.systemui";
    private final Comparator<AlarmIntentRecord> comparatorIntent = new Comparator<AlarmIntentRecord>() {
        /* class com.android.server.OppoAlarmWakeupDetection.AnonymousClass1 */

        public int compare(AlarmIntentRecord lhs, AlarmIntentRecord rhs) {
            if (lhs.mNumAlarm < rhs.mNumAlarm) {
                return 1;
            }
            if (lhs.mNumAlarm > rhs.mNumAlarm) {
                return -1;
            }
            return 0;
        }
    };
    private boolean isSyncAlarmWakeupFrequent = false;
    private AlarmDetectionReceiver mAlarmDetectionReceiver = new AlarmDetectionReceiver();
    private AlarmManagerService mAlarmMS;
    private OppoBaseAlarmManagerService mBaseAlarmMs;
    private final SparseArray<ArrayMap<String, AlarmManagerService.BroadcastStats>> mBroadcastStats;
    private int mCntAlarmWakeup;
    private int mCntAlarmWakeupBak;
    private Context mContext;
    private WorkerHandler mHandlerTimeout;
    private ArrayList<AlarmWakeupRecord> mListFrequent = new ArrayList<>();
    private List<String> mListPkgAlarmWakeupThisTime = new ArrayList();
    private final Object mLock;
    private ArrayList<AlarmWakeupRecord> mReportList = new ArrayList<>();
    private boolean mScreenOffRecorded = false;
    public boolean mScreenOn = true;
    private long mScreenoffElapsed;
    private OppoSysStateManager mSysState;
    private PowerManager.WakeLock mWakeLockCheck;
    private PowerManager.WakeLock mWakeLockReport;
    private long thresholdSeriousPerAlarm = THRESHOLD_SERIOUS_SEC_PER_ALARM;
    private long thresholdSeriousPerWakeup = 300;
    private long thresholdWarningPerWakeup = THRESHOLD_WARNNING_SEC_PER_WAKEUP;
    private long thresholdWorstPerWakeup = THRESHOLD_WORST_SEC_PER_WAKEUP;

    /* access modifiers changed from: private */
    public class AlarmIntentRecord {
        String mIntentAction;
        boolean mIsWakeup;
        int mNumAlarm;

        AlarmIntentRecord(String intentAction, int numAlarm, boolean isWakeup) {
            this.mIntentAction = intentAction;
            this.mNumAlarm = numAlarm;
            this.mIsWakeup = isWakeup;
            if (this.mIntentAction == null) {
                this.mIntentAction = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            }
        }
    }

    /* access modifiers changed from: private */
    public class AlarmWakeupRecord {
        boolean isForcestopReported = false;
        boolean isSerious = false;
        boolean isWorst = false;
        ArrayList<AlarmIntentRecord> mAlarmIntentRecordList;
        boolean mIsWakeup;
        boolean mNotRestrictApp = false;
        int mNumCanceledWakeup;
        String mPkgName;
        long mTotalCheckTime;
        int mUid;
        long mWakeupInterval;
        final Object obj = new Object();

        public AlarmWakeupRecord(int uid, long wakeupInterval, long totalCheckTime, String pkgName, ArrayList<AlarmIntentRecord> alarmIntentRecordList, boolean notRestrictApp, boolean isWakeup, int numCanceledWakeup) {
            boolean z = false;
            this.mUid = uid;
            this.mWakeupInterval = wakeupInterval;
            this.mTotalCheckTime = totalCheckTime;
            this.mPkgName = pkgName;
            this.mAlarmIntentRecordList = alarmIntentRecordList;
            this.isSerious = wakeupInterval <= OppoAlarmWakeupDetection.this.thresholdSeriousPerWakeup;
            this.isWorst = wakeupInterval <= OppoAlarmWakeupDetection.this.thresholdWorstPerWakeup ? true : z;
            this.mNotRestrictApp = notRestrictApp;
            this.mIsWakeup = isWakeup;
            this.mNumCanceledWakeup = numCanceledWakeup;
        }

        public void update(long wakeupInterval, long totalCheckTime, ArrayList<AlarmIntentRecord> alarmIntentRecordList, boolean isWakeup, int numCanceledWakeup) {
            if (!this.mIsWakeup || isWakeup) {
                synchronized (this.obj) {
                    this.mIsWakeup = isWakeup;
                    this.mNumCanceledWakeup = numCanceledWakeup;
                    this.mWakeupInterval = wakeupInterval;
                    this.mTotalCheckTime = totalCheckTime;
                    this.mAlarmIntentRecordList = alarmIntentRecordList;
                    boolean z = true;
                    this.isSerious = wakeupInterval <= OppoAlarmWakeupDetection.this.thresholdSeriousPerWakeup;
                    if (wakeupInterval > OppoAlarmWakeupDetection.this.thresholdWorstPerWakeup) {
                        z = false;
                    }
                    this.isWorst = z;
                }
            }
        }

        public String getReportString(boolean reportIntent) {
            String str;
            synchronized (this.obj) {
                StringBuilder sb = new StringBuilder();
                sb.append("[ ");
                sb.append(this.mPkgName);
                sb.append(" ]    cycle:");
                sb.append(Long.toString(this.mWakeupInterval));
                sb.append("s    duration:");
                sb.append(Long.toString(this.mTotalCheckTime));
                sb.append("s    ");
                if (this.mIsWakeup) {
                    sb.append("alarm_type:wakeups    ");
                } else {
                    sb.append("alarm_type:alarms    ");
                }
                if (this.mNotRestrictApp) {
                    sb.append("{ not restrict app }    ");
                }
                sb.append("canceledWakeup( ");
                sb.append(this.mNumCanceledWakeup);
                sb.append(" )    ");
                if (reportIntent) {
                    int len = this.mAlarmIntentRecordList.size();
                    sb.append(len);
                    sb.append(" Intents    ");
                    int i = 0;
                    while (i < len && i < 5) {
                        AlarmIntentRecord record = this.mAlarmIntentRecordList.get(i);
                        sb.append("{  ");
                        if (record.mIntentAction != null) {
                            sb.append("intent=");
                            sb.append(record.mIntentAction);
                            sb.append("    ");
                        }
                        if (record.mIsWakeup) {
                            sb.append(record.mNumAlarm);
                            sb.append(" wakes  }    ");
                        } else {
                            sb.append(record.mNumAlarm);
                            sb.append(" alarms  }    ");
                        }
                        i++;
                    }
                }
                str = sb.toString();
            }
            return str;
        }
    }

    public OppoAlarmWakeupDetection(Context context, Object lock, AlarmManagerService alarmMS, SparseArray<ArrayMap<String, AlarmManagerService.BroadcastStats>> broadcastStats, Looper loop) {
        this.mContext = context;
        this.mLock = lock;
        this.mAlarmMS = alarmMS;
        this.mBaseAlarmMs = typeCastingAlarmManagerService(this.mAlarmMS);
        this.mBroadcastStats = broadcastStats;
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLockCheck = pm.newWakeLock(1, "alarmCheck");
        this.mWakeLockCheck.setReferenceCounted(false);
        this.mWakeLockReport = pm.newWakeLock(1, "alarmCheckReport");
        this.mHandlerTimeout = new WorkerHandler(loop);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mAlarmDetectionReceiver, filter, null, this.mHandlerTimeout);
        this.mSysState = OppoSysStateManager.getInstance();
        this.mSysState.initOppoGuardElfRcv(context, this.mHandlerTimeout);
        this.mScreenoffElapsed = SystemClock.elapsedRealtime();
    }

    public void alarmTriggerFrequentDetection(AlarmManagerService.Alarm alarm, AlarmManagerService.BroadcastStats bs, long nowELAPSED, boolean isPendingIntentCanceled) {
        String alarmAction;
        if (alarm.type == 2 || alarm.type == 0) {
            if (!alarm.matches("com.android.systemui") || alarm.operation == null || (alarmAction = alarm.operation.getIntent().getAction()) == null || alarmAction.equals("") || !this.ACTION_SYSTEMUI_NOT_RESTRICT_ALARM.contains(alarmAction)) {
                OppoBaseAlarmManagerService.BaseBroadcastStats baseBs = typeCastingBroadcastStats(bs);
                long timeWakeupInterval = nowELAPSED - baseBs.lastTimeWakeup;
                long totalCheckTime = (baseBs.lastTimeWakeup - baseBs.timestampWakupCountReset) / 1000;
                baseBs.lastTimeWakeup = nowELAPSED;
                if (!this.mScreenOn && this.mScreenOffRecorded && bs.mPackageName != null && OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).isScreenOffRestrict()) {
                    baseBs.numWakeupScreeoff++;
                    if (isPendingIntentCanceled) {
                        baseBs.numCanceledWakeup++;
                        if (this.ADBG) {
                            Slog.d(ATAG, "alarmTriggerFrequentDetection: PendingIntentCanceled alarm " + alarm);
                        }
                    }
                    int i = this.mCntAlarmWakeup;
                    if (i != this.mCntAlarmWakeupBak) {
                        this.mCntAlarmWakeupBak = i;
                        this.mListPkgAlarmWakeupThisTime.clear();
                    }
                    if (!this.mListPkgAlarmWakeupThisTime.contains(bs.mPackageName)) {
                        this.mListPkgAlarmWakeupThisTime.add(bs.mPackageName);
                        baseBs.numRealWakeupScreeoff++;
                        if (baseBs.numRealWakeupScreeoff == 1) {
                            resetState(bs, nowELAPSED, false);
                        } else if (timeWakeupInterval >= THRESHOLD_RESET_CHECK) {
                            frequentAlarmRecord(bs, totalCheckTime);
                            resetState(bs, nowELAPSED, false);
                        } else if (!this.mSysState.isNotRestrictPkgAlarm(bs.mPackageName)) {
                            alarmWakeupDetection(bs, nowELAPSED);
                        }
                    }
                }
            }
        }
    }

    public void canceledPendingIntentDetection(AlarmManagerService.Alarm alarm, long nowELAPSED) {
        AlarmManagerService.BroadcastStats bs;
        if ((alarm.type == 2 || alarm.type == 0) && (bs = this.mBaseAlarmMs.getStatsLockedForGuardElf(alarm.operation)) != null) {
            alarmTriggerFrequentDetection(alarm, bs, nowELAPSED, true);
        }
    }

    public int SyncAlarmHandle(int type, PendingIntent pi) {
        String tag;
        if (this.mScreenOn || ((type != 0 && type != 2) || !this.isSyncAlarmWakeupFrequent || pi.getCreatorUid() != 1000 || (tag = pi.getTag("")) == null || !tag.endsWith("syncmanager.SYNC_ALARM"))) {
            return type;
        }
        if (type == 0) {
            return 1;
        }
        if (type == 2) {
            return 3;
        }
        return type;
    }

    public void countAlarmWakeup() {
        if (!this.mScreenOn) {
            this.mCntAlarmWakeup++;
        }
    }

    private boolean isPkgAlarmFrequentDetected(AlarmManagerService.BroadcastStats bs) {
        int len = this.mReportList.size();
        for (int i = 0; i < len; i++) {
            if (bs.mPackageName.equals(this.mReportList.get(i).mPkgName)) {
                return true;
            }
        }
        return false;
    }

    private void frequentAlarmRecord(AlarmManagerService.BroadcastStats bs, long totalCheckTime) {
        if (this.mListFrequent.size() <= 20 && isPkgAlarmFrequentDetected(bs)) {
            OppoBaseAlarmManagerService.BaseBroadcastStats baseBs = typeCastingBroadcastStats(bs);
            int deltaRealWakeup = (baseBs.numRealWakeupScreeoff - baseBs.numRealWakeupWhenReset) - 1;
            if (deltaRealWakeup < 5) {
                return;
            }
            if (totalCheckTime > 0) {
                long numSecondsPerWakeup = totalCheckTime / ((long) deltaRealWakeup);
                if (numSecondsPerWakeup < this.thresholdSeriousPerWakeup - ADVANCE_TIME) {
                    ArrayList<AlarmIntentRecord> alarmIntentRecordList = new ArrayList<>();
                    for (int is = 0; is < bs.filterStats.size(); is++) {
                        AlarmManagerService.FilterStats fs = bs.filterStats.valueAt(is);
                        int fsNumWakeup = fs.numWakeup - typeCastingFilterStats(fs).numWakeupWhenReset;
                        if (fsNumWakeup > 0) {
                            alarmIntentRecordList.add(new AlarmIntentRecord(fs.mTag, fsNumWakeup, true));
                        }
                    }
                    Collections.sort(alarmIntentRecordList, this.comparatorIntent);
                    if (alarmIntentRecordList.size() > 3) {
                        for (int i = alarmIntentRecordList.size() - 1; i >= 3; i--) {
                            alarmIntentRecordList.remove(i);
                        }
                    }
                    boolean isNotStrictApp = OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).isNotRestrictApp(bs.mPackageName);
                    this.mListFrequent.add(new AlarmWakeupRecord(bs.mUid, numSecondsPerWakeup, totalCheckTime, bs.mPackageName, alarmIntentRecordList, isNotStrictApp, true, baseBs.numCanceledWakeup));
                    if (this.ADBG) {
                        Slog.d(ATAG, "frequentAlarmRecord: Uid=" + bs.mUid + ", pkgName:" + bs.mPackageName + ", wakeup system every " + numSecondsPerWakeup + " seconds, totalCheckTime=" + totalCheckTime + ", deltaRealWakeup=" + deltaRealWakeup + ", isNotStrictApp" + isNotStrictApp);
                    }
                }
            }
        }
    }

    public void dumpFrequentAlarm(PrintWriter pw) {
        long screenoffInterval = SystemClock.elapsedRealtime() - this.mScreenoffElapsed;
        if (!this.mScreenOn || screenoffInterval < MIN_SCREEN_OFF_INTERVAL) {
            pw.println("screen off interval too short.");
            return;
        }
        synchronized (this.mLock) {
            StringBuilder sb = new StringBuilder(128);
            if (this.mCntAlarmWakeup > 0) {
                sb.setLength(0);
                sb.append("CycleAlarmWakeup:  ");
                BatteryStats.formatTimeMs(sb, screenoffInterval / ((long) this.mCntAlarmWakeup));
                pw.println(sb.toString());
                if (this.ADBG) {
                    Slog.d(ATAG, "dumpFrequentAlarm: mCntAlarmWakeup=" + this.mCntAlarmWakeup + ", strCycleAlarmWakeup=" + sb.toString());
                }
            }
        }
    }

    private void alarmWakeupDetection(AlarmManagerService.BroadcastStats bs, long nowELAPSED) {
        OppoBaseAlarmManagerService.BaseBroadcastStats baseBs = typeCastingBroadcastStats(bs);
        int deltaRealWakeup = baseBs.numRealWakeupScreeoff - baseBs.numRealWakeupWhenReset;
        if (deltaRealWakeup > 0 && deltaRealWakeup % 5 == 0) {
            long totalCheckTime = (baseBs.lastTimeWakeup - baseBs.timestampWakupCountReset) / 1000;
            if (totalCheckTime > 0) {
                long numSecondsPerWakeup = totalCheckTime / ((long) deltaRealWakeup);
                if (numSecondsPerWakeup < this.thresholdSeriousPerWakeup - ADVANCE_TIME) {
                    this.mWakeLockCheck.acquire(500);
                    Message msg = this.mHandlerTimeout.obtainMessage();
                    msg.what = 101;
                    Bundle data = new Bundle();
                    data.putInt(WatchlistLoggingHandler.WatchlistEventKeys.UID, bs.mUid);
                    data.putString("pkg", bs.mPackageName);
                    data.putLong("totalCheckTime", totalCheckTime);
                    data.putLong("numSecondsPerWakeup", numSecondsPerWakeup);
                    data.putBoolean("isRealCheck", true);
                    msg.setData(data);
                    this.mHandlerTimeout.sendMessage(msg);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void alarmWakeupHandle(Bundle data) {
        Object obj;
        Throwable th;
        AlarmWakeupRecord record;
        if (!this.mScreenOn && data != null) {
            String pkgName = data.getString("pkg");
            int uid = data.getInt(WatchlistLoggingHandler.WatchlistEventKeys.UID);
            long totalCheckTime = data.getLong("totalCheckTime");
            long numSecondsPerWakeup = data.getLong("numSecondsPerWakeup");
            if (pkgName != null) {
                boolean isNotStrict = OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).isNotRestrictApp(pkgName);
                boolean isRealCheck = data.getBoolean("isRealCheck");
                Object obj2 = this.mLock;
                synchronized (obj2) {
                    try {
                        ArrayMap<String, AlarmManagerService.BroadcastStats> uidStats = this.mBroadcastStats.get(uid);
                        if (uidStats == null) {
                            try {
                            } catch (Throwable th2) {
                                th = th2;
                                obj = obj2;
                                throw th;
                            }
                        } else {
                            AlarmManagerService.BroadcastStats bs = uidStats.get(pkgName);
                            if (bs != null) {
                                ArrayList<AlarmIntentRecord> alarmIntentRecordList = new ArrayList<>();
                                for (int is = 0; is < bs.filterStats.size(); is++) {
                                    AlarmManagerService.FilterStats fs = bs.filterStats.valueAt(is);
                                    int fsNumWakeup = fs.numWakeup - typeCastingFilterStats(fs).numWakeupWhenReset;
                                    if (fsNumWakeup > 0) {
                                        alarmIntentRecordList.add(new AlarmIntentRecord(fs.mTag, fsNumWakeup, true));
                                        if (!this.isSyncAlarmWakeupFrequent && fsNumWakeup >= 5) {
                                            if (bs.mUid == 1000 && fs.mTag != null) {
                                                if (fs.mTag.endsWith("syncmanager.SYNC_ALARM")) {
                                                    if (totalCheckTime / ((long) fsNumWakeup) <= this.thresholdSeriousPerWakeup) {
                                                        this.isSyncAlarmWakeupFrequent = true;
                                                        if (this.ADBG) {
                                                            Slog.d(ATAG, "alarmWakeupHandle: isSyncAlarmWakeupFrequent set true!");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Collections.sort(alarmIntentRecordList, this.comparatorIntent);
                                obj = obj2;
                                try {
                                    record = addReportList(bs.mUid, numSecondsPerWakeup, totalCheckTime, pkgName, alarmIntentRecordList, isNotStrict, true, typeCastingBroadcastStats(bs).numCanceledWakeup);
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                                try {
                                    if (record != null) {
                                        reportFrequentAlarmListForceStop(record, isRealCheck);
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            }
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        obj = obj2;
                        throw th;
                    }
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
            int i = msg.what;
            if (i == 101) {
                OppoAlarmWakeupDetection.this.alarmWakeupHandle(msg.getData());
                if (!OppoAlarmWakeupDetection.this.mHandlerTimeout.hasMessages(101) && OppoAlarmWakeupDetection.this.mWakeLockCheck.isHeld()) {
                    OppoAlarmWakeupDetection.this.mWakeLockCheck.release();
                }
            } else if (i == 102) {
                OppoAlarmWakeupDetection.this.wakeupNumRecord();
                if (OppoAlarmWakeupDetection.this.mWakeLockCheck.isHeld()) {
                    OppoAlarmWakeupDetection.this.mWakeLockCheck.release();
                }
            }
        }
    }

    private class AlarmDetectionReceiver extends BroadcastReceiver {
        private AlarmDetectionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                OppoAlarmWakeupDetection.this.mScreenOffRecorded = false;
                OppoAlarmWakeupDetection oppoAlarmWakeupDetection = OppoAlarmWakeupDetection.this;
                oppoAlarmWakeupDetection.mScreenOn = true;
                if (oppoAlarmWakeupDetection.mHandlerTimeout.hasMessages(102)) {
                    OppoAlarmWakeupDetection.this.mHandlerTimeout.removeMessages(102);
                }
                OppoAlarmWakeupDetection.this.isSyncAlarmWakeupFrequent = false;
                OppoAlarmWakeupDetection.this.mListFrequent.clear();
                OppoAlarmWakeupDetection.this.mReportList.clear();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                OppoAlarmWakeupDetection oppoAlarmWakeupDetection2 = OppoAlarmWakeupDetection.this;
                oppoAlarmWakeupDetection2.mScreenOn = false;
                oppoAlarmWakeupDetection2.isSyncAlarmWakeupFrequent = false;
                OppoAlarmWakeupDetection.this.mReportList.clear();
                OppoAlarmWakeupDetection.this.mListFrequent.clear();
                OppoAlarmWakeupDetection.this.mWakeLockCheck.acquire(1000);
                OppoAlarmWakeupDetection.this.thresholdWarningPerWakeup = OppoGuardElfConfigUtil.getInstance().getThresholdWarningIntervalPerWakeup();
                OppoAlarmWakeupDetection.this.thresholdSeriousPerWakeup = OppoGuardElfConfigUtil.getInstance().getThresholdIntervalPerWakeup();
                OppoAlarmWakeupDetection.this.thresholdWorstPerWakeup = OppoGuardElfConfigUtil.getInstance().getThresholdWorstIntervalPerWakeup();
                OppoAlarmWakeupDetection.this.thresholdSeriousPerAlarm = OppoGuardElfConfigUtil.getInstance().getThresholdSeriousIntervalPerAlarm();
                OppoAlarmWakeupDetection.this.mHandlerTimeout.sendEmptyMessage(102);
                OppoAlarmWakeupDetection.this.mScreenoffElapsed = SystemClock.elapsedRealtime();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wakeupNumRecord() {
        synchronized (this.mLock) {
            for (int iu = 0; iu < this.mBroadcastStats.size(); iu++) {
                ArrayMap<String, AlarmManagerService.BroadcastStats> uidStats = this.mBroadcastStats.valueAt(iu);
                for (int ip = 0; ip < uidStats.size(); ip++) {
                    resetState(uidStats.valueAt(ip), this.mScreenoffElapsed, true);
                }
            }
            this.mCntAlarmWakeup = 0;
            this.mCntAlarmWakeupBak = -1;
        }
        this.mScreenOffRecorded = true;
    }

    private void resetState(AlarmManagerService.BroadcastStats bs, long wakeupStartTime, boolean screenoff) {
        OppoBaseAlarmManagerService.BaseBroadcastStats baseBs = typeCastingBroadcastStats(bs);
        if (screenoff) {
            baseBs.numWakeupScreeoff = 0;
            baseBs.numRealWakeupScreeoff = 0;
        }
        baseBs.numWakeupWhenReset = baseBs.numWakeupScreeoff;
        baseBs.numRealWakeupWhenReset = baseBs.numRealWakeupScreeoff;
        baseBs.timestampWakupCountReset = wakeupStartTime;
        baseBs.numCanceledWakeup = 0;
        for (int is = 0; is < bs.filterStats.size(); is++) {
            AlarmManagerService.FilterStats fs = bs.filterStats.valueAt(is);
            OppoBaseAlarmManagerService.BaseFilterStats baseFs = typeCastingFilterStats(fs);
            if (screenoff) {
                baseFs.numWakeupWhenScreenoff = fs.numWakeup;
            }
            baseFs.numWakeupWhenReset = fs.numWakeup;
        }
    }

    private AlarmWakeupRecord getFrequentAlarm(String pkgName) {
        int len = this.mReportList.size();
        for (int i = 0; i < len; i++) {
            AlarmWakeupRecord alarmWakeupRecord = this.mReportList.get(i);
            if (pkgName.equals(alarmWakeupRecord.mPkgName)) {
                return alarmWakeupRecord;
            }
        }
        return null;
    }

    private AlarmWakeupRecord addReportList(int uid, long wakeupInterval, long totalCheckTime, String pkgName, ArrayList<AlarmIntentRecord> alarmIntentRecordList, boolean notRestrictApp, boolean isWakeup, int numCanceledWakeup) {
        if (pkgName == null) {
            return null;
        }
        AlarmWakeupRecord alarmWakeupRecord = getFrequentAlarm(pkgName);
        if (alarmWakeupRecord != null) {
            alarmWakeupRecord.update(wakeupInterval, totalCheckTime, alarmIntentRecordList, isWakeup, numCanceledWakeup);
            return alarmWakeupRecord;
        }
        AlarmWakeupRecord alarmWakeupRecord2 = new AlarmWakeupRecord(uid, wakeupInterval, totalCheckTime, pkgName, alarmIntentRecordList, notRestrictApp, isWakeup, numCanceledWakeup);
        this.mReportList.add(alarmWakeupRecord2);
        return alarmWakeupRecord2;
    }

    private void reportFrequentAlarmListForceStop(AlarmWakeupRecord record, boolean isRealcheck) {
        ArrayList<String> uidList = new ArrayList<>();
        if (record != null && record.isSerious && record.mPkgName != null) {
            record.isForcestopReported = true;
            String reportString = record.getReportString(true);
            uidList.add(reportString);
            if (this.ADBG) {
                Slog.d(ATAG, "FrequentAlarmListF: reportString=" + reportString);
            }
            if (uidList.size() > 0) {
                Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                intent.putStringArrayListExtra("data", uidList);
                intent.putExtra(DatabaseHelper.SoundModelContract.KEY_TYPE, "alarm");
                intent.putExtra("isRealcheck", isRealcheck);
                this.mContext.sendBroadcast(intent);
                this.mWakeLockReport.acquire(500);
            }
        }
    }

    private boolean isSystemPackage(String name) {
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = this.mContext.getPackageManager().getPackageInfo(name, 128);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (pkgInfo == null || (pkgInfo.applicationInfo.flags & 1) == 0) {
            return false;
        }
        return true;
    }

    private boolean isOppoPackage(String name) {
        if (name.contains(".oppo.") || name.contains(".coloros.")) {
            return true;
        }
        return false;
    }

    private static OppoBaseAlarmManagerService.BaseBroadcastStats typeCastingBroadcastStats(AlarmManagerService.BroadcastStats bs) {
        return (OppoBaseAlarmManagerService.BaseBroadcastStats) ColorTypeCastingHelper.typeCasting(OppoBaseAlarmManagerService.BaseBroadcastStats.class, bs);
    }

    private static OppoBaseAlarmManagerService.BaseFilterStats typeCastingFilterStats(AlarmManagerService.FilterStats fs) {
        return (OppoBaseAlarmManagerService.BaseFilterStats) ColorTypeCastingHelper.typeCasting(OppoBaseAlarmManagerService.BaseFilterStats.class, fs);
    }

    private static OppoBaseAlarmManagerService typeCastingAlarmManagerService(AlarmManagerService alarmMs) {
        return (OppoBaseAlarmManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseAlarmManagerService.class, alarmMs);
    }
}
