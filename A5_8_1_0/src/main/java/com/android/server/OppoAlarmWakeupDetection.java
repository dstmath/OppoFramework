package com.android.server;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.am.OppoAbnormalAppManager;
import com.android.server.backup.RefactoredBackupManagerService;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class OppoAlarmWakeupDetection {
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR = "android.intent.action.OPPO_GUARD_ELF_MONITOR";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final String ATAG = "AlarmWakeupCheck";
    private static final boolean DBG = false;
    private static final int MAX_UPLOAD_NUM = 2;
    private static final int MIN_ALARM_NUM = 15;
    private static final long MIN_SCREEN_OFF_INTERVAL = 3300000;
    private static final int MSG_CHECK_ALARM_ALL_TYPE = 105;
    private static final int MSG_CHECK_ALARM_WAKEUP = 101;
    private static final int MSG_WAKEUP_NUM_RECORD = 102;
    private static final long THRESHOLD_REPORT_DCS = 90000;
    private static final long THRESHOLD_RESET_CHECK = 1200000;
    private static final long THRESHOLD_SERIOUS_SEC_PER_ALARM = 180;
    private static final long THRESHOLD_SERIOUS_SEC_PER_WAKEUP = 300;
    private static final long THRESHOLD_WARNNING_SEC_PER_WAKEUP = 360;
    private static final long THRESHOLD_WORST_SEC_PER_WAKEUP = 60;
    private final boolean ADBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private final Comparator<AlarmIntentRecord> comparatorIntent = new Comparator<AlarmIntentRecord>() {
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
    private AlarmDetectionReceiver mAlarmDetectionReceiver = new AlarmDetectionReceiver(this, null);
    private AlarmManagerService mAlarmMS;
    private final SparseArray<ArrayMap<String, BroadcastStats>> mBroadcastStats;
    private int mCntAlarmWakeup;
    private Context mContext;
    private WorkerHandler mHandlerTimeout;
    private final Object mLock;
    private ArrayList<AlarmWakeupRecord> mReportList = new ArrayList();
    private boolean mScreenOffRecorded = false;
    public boolean mScreenOn = true;
    private long mScreenoffElapsed;
    private OppoSysStateManager mSysState;
    private WakeLock mWakeLockCheck;
    private WakeLock mWakeLockReport;
    private long thresholdSeriousPerAlarm = THRESHOLD_SERIOUS_SEC_PER_ALARM;
    private long thresholdSeriousPerWakeup = 300;
    private long thresholdWarningPerWakeup = THRESHOLD_WARNNING_SEC_PER_WAKEUP;
    private long thresholdWorstPerWakeup = THRESHOLD_WORST_SEC_PER_WAKEUP;

    private class AlarmDetectionReceiver extends BroadcastReceiver {
        /* synthetic */ AlarmDetectionReceiver(OppoAlarmWakeupDetection this$0, AlarmDetectionReceiver -this1) {
            this();
        }

        private AlarmDetectionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                OppoAlarmWakeupDetection.this.mReportList.clear();
                OppoAlarmWakeupDetection.this.mScreenOffRecorded = false;
                OppoAlarmWakeupDetection.this.mScreenOn = true;
                if (OppoAlarmWakeupDetection.this.mHandlerTimeout.hasMessages(102)) {
                    OppoAlarmWakeupDetection.this.mHandlerTimeout.removeMessages(102);
                }
                OppoAlarmWakeupDetection.this.isSyncAlarmWakeupFrequent = false;
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                OppoAlarmWakeupDetection.this.mScreenOn = false;
                OppoAlarmWakeupDetection.this.isSyncAlarmWakeupFrequent = false;
                OppoAlarmWakeupDetection.this.mReportList.clear();
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

    private class AlarmIntentRecord {
        String mIntentAction;
        boolean mIsWakeup;
        int mNumAlarm;

        AlarmIntentRecord(String intentAction, int numAlarm, boolean isWakeup) {
            this.mIntentAction = intentAction;
            this.mNumAlarm = numAlarm;
            this.mIsWakeup = isWakeup;
            if (this.mIntentAction == null) {
                this.mIntentAction = Shell.NIGHT_MODE_STR_UNKNOWN;
            }
        }
    }

    private class AlarmWakeupRecord {
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
            this.mUid = uid;
            this.mWakeupInterval = wakeupInterval;
            this.mTotalCheckTime = totalCheckTime;
            this.mPkgName = pkgName;
            this.mAlarmIntentRecordList = alarmIntentRecordList;
            this.isSerious = wakeupInterval <= OppoAlarmWakeupDetection.this.thresholdSeriousPerWakeup;
            this.isWorst = wakeupInterval <= OppoAlarmWakeupDetection.this.thresholdWorstPerWakeup;
            this.mNotRestrictApp = notRestrictApp;
            this.mIsWakeup = isWakeup;
            this.mNumCanceledWakeup = numCanceledWakeup;
        }

        public void update(long wakeupInterval, long totalCheckTime, ArrayList<AlarmIntentRecord> alarmIntentRecordList, boolean isWakeup, int numCanceledWakeup) {
            boolean z = true;
            if (!this.mIsWakeup || (isWakeup ^ 1) == 0) {
                synchronized (this.obj) {
                    boolean z2;
                    this.mIsWakeup = isWakeup;
                    this.mNumCanceledWakeup = numCanceledWakeup;
                    this.mWakeupInterval = wakeupInterval;
                    this.mTotalCheckTime = totalCheckTime;
                    this.mAlarmIntentRecordList = alarmIntentRecordList;
                    if (wakeupInterval <= OppoAlarmWakeupDetection.this.thresholdSeriousPerWakeup) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    this.isSerious = z2;
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
                str = "";
                StringBuilder sb = new StringBuilder();
                sb.append("[ ").append(this.mPkgName).append(" ]    cycle:").append(Long.toString(this.mWakeupInterval)).append("s    duration:").append(Long.toString(this.mTotalCheckTime)).append("s    ");
                if (this.mIsWakeup) {
                    sb.append("alarm_type:wakeups    ");
                } else {
                    sb.append("alarm_type:alarms    ");
                }
                if (this.mNotRestrictApp) {
                    sb.append("{ not restrict app }    ");
                }
                sb.append("canceledWakeup( ").append(this.mNumCanceledWakeup).append(" )    ");
                if (reportIntent) {
                    int len = this.mAlarmIntentRecordList.size();
                    sb.append(len);
                    sb.append(" Intents    ");
                    int i = 0;
                    while (i < len && i < 5) {
                        AlarmIntentRecord record = (AlarmIntentRecord) this.mAlarmIntentRecordList.get(i);
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

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    OppoAlarmWakeupDetection.this.alarmWakeupHandle(msg.getData());
                    if (!OppoAlarmWakeupDetection.this.mHandlerTimeout.hasMessages(101) && OppoAlarmWakeupDetection.this.mWakeLockCheck.isHeld()) {
                        OppoAlarmWakeupDetection.this.mWakeLockCheck.release();
                        return;
                    }
                    return;
                case 102:
                    OppoAlarmWakeupDetection.this.wakeupNumRecord();
                    if (OppoAlarmWakeupDetection.this.mWakeLockCheck.isHeld()) {
                        OppoAlarmWakeupDetection.this.mWakeLockCheck.release();
                        return;
                    }
                    return;
                case 105:
                    OppoAlarmWakeupDetection.this.alarmAllTypeHandle(msg.getData());
                    if (!OppoAlarmWakeupDetection.this.mHandlerTimeout.hasMessages(105) && OppoAlarmWakeupDetection.this.mWakeLockCheck.isHeld()) {
                        OppoAlarmWakeupDetection.this.mWakeLockCheck.release();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public OppoAlarmWakeupDetection(Context context, Object lock, AlarmManagerService alarmMS, SparseArray<ArrayMap<String, BroadcastStats>> broadcastStats, Looper loop) {
        this.mContext = context;
        this.mLock = lock;
        this.mAlarmMS = alarmMS;
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

    /* JADX WARNING: Missing block: B:14:0x0055, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void alarmTriggerFrequentDetection(Alarm alarm, BroadcastStats bs, long nowELAPSED, boolean isPendingIntentCanceled) {
        boolean isWakeupAlarm = alarm.type != 2 ? alarm.type == 0 : true;
        long lastTimeWakeup = 0;
        if (isWakeupAlarm) {
            bs.numAllWakeup++;
            lastTimeWakeup = bs.lastTimeWakeup;
            bs.lastTimeWakeup = nowELAPSED;
            if (isPendingIntentCanceled) {
                bs.numCanceledWakeup++;
            }
        }
        bs.numAllTrigger++;
        long lastTimeTrigger = bs.lastTimeTrigger;
        bs.lastTimeTrigger = nowELAPSED;
        if (!this.mScreenOn && (this.mScreenOffRecorded ^ 1) == 0 && OppoAbnormalAppManager.getInstance().isScreenOffRestrict() && bs.mPackageName != null && !this.mSysState.isNotRestrictPkg(bs.mPackageName)) {
            if (isPendingIntentCanceled && this.ADBG) {
                Slog.d(ATAG, "alarmTriggerFrequentDetection: PendingIntentCanceled alarm " + alarm);
            }
            boolean alarmWakeupDetected = false;
            if (isWakeupAlarm) {
                if (bs.numAllWakeup == 1) {
                    resetState(bs, nowELAPSED, 0, true, false);
                } else {
                    alarmWakeupDetected = alarmWakeupDetection(bs, nowELAPSED, lastTimeWakeup);
                }
            }
            if (bs.numAllTrigger == 1) {
                resetState(bs, 0, nowELAPSED, false, true);
            } else if (!alarmWakeupDetected) {
                alarmAllTypeDetection(bs, nowELAPSED, lastTimeTrigger);
            }
        }
    }

    public void canceledPendingIntentDetection(Alarm alarm, long nowELAPSED) {
        if (alarm.type == 2 || alarm.type == 0) {
            BroadcastStats bs = this.mAlarmMS.getStatsLockedForGuardElf(alarm.operation);
            if (bs != null) {
                alarmTriggerFrequentDetection(alarm, bs, nowELAPSED, true);
            }
        }
    }

    public int SyncAlarmHandle(int type, PendingIntent pi) {
        if (this.mScreenOn || (type != 0 && type != 2)) {
            return type;
        }
        if (this.isSyncAlarmWakeupFrequent && pi.getCreatorUid() == 1000) {
            String tag = pi.getTag("");
            if (tag != null && tag.endsWith("syncmanager.SYNC_ALARM")) {
                int typeOld = type;
                if (type == 0) {
                    type = 1;
                } else if (type == 2) {
                    type = 3;
                }
            }
        }
        return type;
    }

    public void countAlarmWakeup() {
        if (!this.mScreenOn) {
            this.mCntAlarmWakeup++;
        }
    }

    public void dumpFrequentAlarm(PrintWriter pw) {
        long nowElapsed = SystemClock.elapsedRealtime();
        long screenoffInterval = nowElapsed - this.mScreenoffElapsed;
        if (screenoffInterval < MIN_SCREEN_OFF_INTERVAL) {
            pw.println("screen off interval too short.");
            return;
        }
        synchronized (this.mLock) {
            StringBuilder stringBuilder = new StringBuilder(128);
            for (int iu = 0; iu < this.mBroadcastStats.size(); iu++) {
                ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) this.mBroadcastStats.valueAt(iu);
                for (int ip = 0; ip < uidStats.size(); ip++) {
                    BroadcastStats bs = (BroadcastStats) uidStats.valueAt(ip);
                    long totalCheckTime = nowElapsed - bs.wakeupCountStartTime;
                    int numWakeup = bs.numAllWakeup - bs.numWakeupWhenScreenoff;
                    if (numWakeup >= 15 && totalCheckTime >= screenoffInterval / 2) {
                        long cycle = totalCheckTime / ((long) numWakeup);
                        if (cycle <= ColorOSDeviceIdleHelper.ALARM_WINDOW_LENGTH) {
                            stringBuilder.setLength(0);
                            stringBuilder.append("FrequentAlarm:  ");
                            stringBuilder.append("[");
                            stringBuilder.append(bs.mPackageName);
                            stringBuilder.append("] cycle(");
                            BatteryStats.formatTimeMs(stringBuilder, cycle);
                            stringBuilder.append(") checkTime(");
                            BatteryStats.formatTimeMs(stringBuilder, totalCheckTime);
                            stringBuilder.append(") cancled(");
                            stringBuilder.append(bs.numCanceledWakeup);
                            stringBuilder.append(") ");
                            ArrayList<AlarmIntentRecord> alarmIntentRecordList = new ArrayList();
                            for (int is = 0; is < bs.filterStats.size(); is++) {
                                FilterStats fs = (FilterStats) bs.filterStats.valueAt(is);
                                int fsNumWakeup = fs.numWakeup - fs.numWakeupWhenScreenoff;
                                if (fsNumWakeup > 0) {
                                    alarmIntentRecordList.add(new AlarmIntentRecord(fs.mTag, fsNumWakeup, true));
                                }
                            }
                            Collections.sort(alarmIntentRecordList, this.comparatorIntent);
                            stringBuilder.append("{ ");
                            int i = 0;
                            while (i < alarmIntentRecordList.size() && i < 2) {
                                AlarmIntentRecord record = (AlarmIntentRecord) alarmIntentRecordList.get(i);
                                if (record.mNumAlarm > 0) {
                                    long cycleIntent = totalCheckTime / ((long) record.mNumAlarm);
                                    if (cycleIntent > RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL) {
                                        break;
                                    }
                                    stringBuilder.append("< ");
                                    stringBuilder.append("tag[");
                                    stringBuilder.append(record.mIntentAction);
                                    stringBuilder.append("] cycle(");
                                    BatteryStats.formatTimeMs(stringBuilder, cycleIntent);
                                    stringBuilder.append(") >, ");
                                    i++;
                                } else {
                                    break;
                                }
                            }
                            stringBuilder.append(" }");
                            if (cycle <= THRESHOLD_REPORT_DCS) {
                                pw.println(stringBuilder.toString());
                                if (this.ADBG) {
                                    Slog.d(ATAG, "dumpFrequentAlarm: worst. " + stringBuilder.toString());
                                }
                            } else if (this.ADBG) {
                                Slog.d(ATAG, "dumpFrequentAlarm: serious. " + stringBuilder.toString());
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
            if (this.mCntAlarmWakeup > 0) {
                long cycleAlarmWakeup = screenoffInterval / ((long) this.mCntAlarmWakeup);
                stringBuilder.setLength(0);
                stringBuilder.append("CycleAlarmWakeup:  ");
                BatteryStats.formatTimeMs(stringBuilder, cycleAlarmWakeup);
                pw.println(stringBuilder.toString());
                if (this.ADBG) {
                    Slog.d(ATAG, "dumpFrequentAlarm: mCntAlarmWakeup=" + this.mCntAlarmWakeup + ", strCycleAlarmWakeup=" + stringBuilder.toString());
                }
            }
        }
    }

    private boolean alarmWakeupDetection(BroadcastStats bs, long nowELAPSED, long lastTimeWakeup) {
        boolean detected = false;
        if (nowELAPSED - lastTimeWakeup >= THRESHOLD_RESET_CHECK) {
            resetState(bs, nowELAPSED, 0, true, false);
            return false;
        }
        int numAllWakeup = bs.numAllWakeup - bs.numWakeupWhenScreenoff;
        if (numAllWakeup <= 0 || numAllWakeup % 5 != 0) {
            return false;
        }
        long totalCheckTime = (bs.lastTimeWakeup - bs.wakeupCountStartTime) / 1000;
        if (totalCheckTime <= 0) {
            return false;
        }
        long numSecondsPerWakeup = totalCheckTime / ((long) numAllWakeup);
        if (numSecondsPerWakeup <= this.thresholdSeriousPerWakeup) {
            this.mWakeLockCheck.acquire(500);
            Message msg = this.mHandlerTimeout.obtainMessage();
            msg.what = 101;
            Bundle data = new Bundle();
            data.putInt("uid", bs.mUid);
            data.putString("pkg", bs.mPackageName);
            data.putLong("totalCheckTime", totalCheckTime);
            data.putLong("numSecondsPerWakeup", numSecondsPerWakeup);
            msg.setData(data);
            this.mHandlerTimeout.sendMessage(msg);
            detected = true;
        }
        return detected;
    }

    private void alarmAllTypeDetection(BroadcastStats bs, long nowELAPSED, long lastTimeTrigger) {
        if (!isSystemPackage(bs.mPackageName)) {
            if (!isOppoPackage(bs.mPackageName)) {
                if (nowELAPSED - lastTimeTrigger >= THRESHOLD_RESET_CHECK) {
                    resetState(bs, 0, nowELAPSED, false, true);
                    return;
                }
                int numAlarm = bs.numAllTrigger - bs.numWhenScreenoff;
                if (numAlarm >= 5 && numAlarm % 5 == 0) {
                    long totalCheckTime = (bs.lastTimeTrigger - bs.triggerCountStartTime) / 1000;
                    if (totalCheckTime > 0) {
                        long numSecondsPerAlarm = totalCheckTime / ((long) numAlarm);
                        if (numSecondsPerAlarm <= this.thresholdSeriousPerAlarm) {
                            this.mWakeLockCheck.acquire(500);
                            Message msg = this.mHandlerTimeout.obtainMessage();
                            msg.what = 105;
                            Bundle data = new Bundle();
                            data.putInt("uid", bs.mUid);
                            data.putString("pkg", bs.mPackageName);
                            data.putLong("totalCheckTime", totalCheckTime);
                            data.putLong("numSecondsPerAlarm", numSecondsPerAlarm);
                            msg.setData(data);
                            this.mHandlerTimeout.sendMessage(msg);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:47:0x00ec, code:
            if (r18 == null) goto L_0x00f5;
     */
    /* JADX WARNING: Missing block: B:48:0x00ee, code:
            reportFrequentAlarmListForceStop(r18);
     */
    /* JADX WARNING: Missing block: B:49:0x00f5, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void alarmWakeupHandle(Bundle data) {
        if (!this.mScreenOn && data != null) {
            String pkgName = data.getString("pkg");
            int uid = data.getInt("uid");
            long totalCheckTime = data.getLong("totalCheckTime");
            long numSecondsPerWakeup = data.getLong("numSecondsPerWakeup");
            if (pkgName != null) {
                boolean isNotStrict = OppoAbnormalAppManager.getInstance().isNotRestrictApp(pkgName);
                synchronized (this.mLock) {
                    ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) this.mBroadcastStats.get(uid);
                    if (uidStats == null) {
                        return;
                    }
                    BroadcastStats bs = (BroadcastStats) uidStats.get(pkgName);
                    if (bs == null) {
                        return;
                    }
                    ArrayList<AlarmIntentRecord> alarmIntentRecordList = new ArrayList();
                    for (int is = 0; is < bs.filterStats.size(); is++) {
                        FilterStats fs = (FilterStats) bs.filterStats.valueAt(is);
                        int fsNumWakeup = fs.numWakeup - fs.numWakeupWhenScreenoff;
                        if (fsNumWakeup > 0) {
                            alarmIntentRecordList.add(new AlarmIntentRecord(fs.mTag, fsNumWakeup, true));
                            if (!this.isSyncAlarmWakeupFrequent && fsNumWakeup >= 5 && bs.mUid == 1000 && fs.mTag != null && fs.mTag.endsWith("syncmanager.SYNC_ALARM") && totalCheckTime / ((long) fsNumWakeup) <= this.thresholdSeriousPerWakeup) {
                                this.isSyncAlarmWakeupFrequent = true;
                                if (this.ADBG) {
                                    Slog.d(ATAG, "alarmWakeupHandle: isSyncAlarmWakeupFrequent set true!");
                                }
                            }
                        }
                    }
                    Collections.sort(alarmIntentRecordList, this.comparatorIntent);
                    AlarmWakeupRecord record = addReportList(bs.mUid, numSecondsPerWakeup, totalCheckTime, pkgName, alarmIntentRecordList, isNotStrict, true, bs.numCanceledWakeup);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:37:0x00af, code:
            if (r18 == null) goto L_0x00b8;
     */
    /* JADX WARNING: Missing block: B:38:0x00b1, code:
            reportFrequentAlarmListForceStop(r18);
     */
    /* JADX WARNING: Missing block: B:39:0x00b8, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void alarmAllTypeHandle(Bundle data) {
        if (!this.mScreenOn && data != null) {
            String pkgName = data.getString("pkg");
            int uid = data.getInt("uid");
            long totalCheckTime = data.getLong("totalCheckTime");
            long numSecondsPerAlarm = data.getLong("numSecondsPerAlarm");
            if (pkgName != null) {
                boolean isNotStrict = OppoAbnormalAppManager.getInstance().isNotRestrictApp(pkgName);
                synchronized (this.mLock) {
                    ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) this.mBroadcastStats.get(uid);
                    if (uidStats == null) {
                        return;
                    }
                    BroadcastStats bs = (BroadcastStats) uidStats.get(pkgName);
                    if (bs == null) {
                        return;
                    }
                    ArrayList<AlarmIntentRecord> alarmIntentRecordList = new ArrayList();
                    for (int is = 0; is < bs.filterStats.size(); is++) {
                        FilterStats fs = (FilterStats) bs.filterStats.valueAt(is);
                        int fsNumAlarm = fs.count - fs.numWhenScreenoff;
                        if (fsNumAlarm > 0) {
                            alarmIntentRecordList.add(new AlarmIntentRecord(fs.mTag, fsNumAlarm, fs.numWakeup != 0));
                        }
                    }
                    Collections.sort(alarmIntentRecordList, this.comparatorIntent);
                    AlarmWakeupRecord record = addReportList(bs.mUid, numSecondsPerAlarm, totalCheckTime, pkgName, alarmIntentRecordList, isNotStrict, false, bs.numCanceledWakeup);
                }
            }
        }
    }

    private void wakeupNumRecord() {
        synchronized (this.mLock) {
            for (int iu = 0; iu < this.mBroadcastStats.size(); iu++) {
                ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) this.mBroadcastStats.valueAt(iu);
                for (int ip = 0; ip < uidStats.size(); ip++) {
                    BroadcastStats bs = (BroadcastStats) uidStats.valueAt(ip);
                    resetState(bs, bs.lastTimeWakeup, bs.lastTimeTrigger, true, true);
                }
            }
            this.mCntAlarmWakeup = 0;
        }
        this.mScreenOffRecorded = true;
    }

    private void resetState(BroadcastStats bs, long wakeupStartTime, long triggerStartTime, boolean resetWakeup, boolean resetTrigger) {
        if (resetWakeup) {
            bs.numWakeupWhenScreenoff = bs.numAllWakeup;
            bs.wakeupCountStartTime = wakeupStartTime;
            bs.numCanceledWakeup = 0;
        }
        if (resetTrigger) {
            bs.numWhenScreenoff = bs.numAllTrigger;
            bs.triggerCountStartTime = triggerStartTime;
        }
        for (int is = 0; is < bs.filterStats.size(); is++) {
            FilterStats fs = (FilterStats) bs.filterStats.valueAt(is);
            if (resetWakeup) {
                fs.numWakeupWhenScreenoff = fs.numWakeup;
            }
            if (resetTrigger) {
                fs.numWhenScreenoff = fs.count;
            }
        }
    }

    private AlarmWakeupRecord getFrequentAlarm(String pkgName) {
        int len = this.mReportList.size();
        for (int i = 0; i < len; i++) {
            AlarmWakeupRecord alarmWakeupRecord = (AlarmWakeupRecord) this.mReportList.get(i);
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
        } else {
            alarmWakeupRecord = new AlarmWakeupRecord(uid, wakeupInterval, totalCheckTime, pkgName, alarmIntentRecordList, notRestrictApp, isWakeup, numCanceledWakeup);
            this.mReportList.add(alarmWakeupRecord);
        }
        return alarmWakeupRecord;
    }

    private void reportFrequentAlarmListForceStop(AlarmWakeupRecord record) {
        ArrayList<String> uidList = new ArrayList();
        if (record != null && (record.isSerious ^ 1) == 0 && record.mPkgName != null) {
            record.isForcestopReported = true;
            String reportString = record.getReportString(true);
            uidList.add(reportString);
            if (this.ADBG) {
                Slog.d(ATAG, "FrequentAlarmListF: reportString=" + reportString);
            }
            if (uidList.size() > 0) {
                Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                intent.putStringArrayListExtra("data", uidList);
                intent.putExtra(SoundModelContract.KEY_TYPE, "alarm");
                this.mContext.sendBroadcast(intent);
                this.mWakeLockReport.acquire(500);
            }
        }
    }

    private boolean isSystemPackage(String name) {
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = this.mContext.getPackageManager().getPackageInfo(name, 128);
        } catch (NameNotFoundException e) {
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
}
