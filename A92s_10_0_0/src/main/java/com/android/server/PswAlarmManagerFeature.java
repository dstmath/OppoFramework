package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseLongArray;
import com.android.server.AlarmManagerService;
import com.android.server.AlarmUpdateHelper;
import com.android.server.display.color.DisplayTransformManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class PswAlarmManagerFeature implements IPswAlarmManagerFeature, AlarmUpdateHelper.Callbacks {
    private static final String TAG = "PswAlarmManagerFeature";
    private static boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private ArrayList<AlarmManagerService.Batch> mAlarmBatches;
    final Comparator<AlarmManagerService.Alarm> mAlarmDispatchComparator;
    private AlarmUpdateHelper mAlarmUpdateHelper;
    private Context mContext;
    private boolean mInteractive;
    private boolean mIsInited;
    private SparseLongArray mLastAllowWhileIdleDispatch;
    private long mMaxDelayTime;
    private boolean mNetstateInteractive;
    private AlarmManagerService.Alarm mNextWakeFromIdle;
    private SparseArray<ArrayList<AlarmManagerService.Alarm>> mPendingBackgroundAlarms;
    private AlarmManagerService.Alarm mPendingIdleUntil;
    private ArrayList<AlarmManagerService.Alarm> mPendingImportantNonWakeupAlarms;
    private ArrayList<AlarmManagerService.Alarm> mPendingNonWakeupAlarms;
    private IPswAlarmManagerCallback mPswAlarmManagerCallback;
    private long mStartCurrentDelayTime;
    private long mTotalDelayTime;
    private ArrayList<AlarmManagerService.Alarm> mTriggerListNonWakeup;

    private PswAlarmManagerFeature() {
        this.mPswAlarmManagerCallback = null;
        this.mPendingImportantNonWakeupAlarms = new ArrayList<>();
        this.mTriggerListNonWakeup = new ArrayList<>();
        this.mInteractive = true;
        this.mNetstateInteractive = true;
        this.mPendingNonWakeupAlarms = null;
        this.mAlarmBatches = null;
        this.mLastAllowWhileIdleDispatch = null;
        this.mPendingBackgroundAlarms = null;
        this.mPendingIdleUntil = null;
        this.mNextWakeFromIdle = null;
        this.mAlarmDispatchComparator = new Comparator<AlarmManagerService.Alarm>() {
            /* class com.android.server.PswAlarmManagerFeature.AnonymousClass1 */

            public int compare(AlarmManagerService.Alarm lhs, AlarmManagerService.Alarm rhs) {
                if (lhs.priorityClass.priority < rhs.priorityClass.priority) {
                    return -1;
                }
                if (lhs.priorityClass.priority > rhs.priorityClass.priority) {
                    return 1;
                }
                if (lhs.whenElapsed < rhs.whenElapsed) {
                    return -1;
                }
                if (lhs.whenElapsed > rhs.whenElapsed) {
                    return 1;
                }
                return 0;
            }
        };
        this.mIsInited = false;
    }

    private static class InstanceHolder {
        static final PswAlarmManagerFeature INSTANCE = new PswAlarmManagerFeature();

        private InstanceHolder() {
        }
    }

    public static PswAlarmManagerFeature getInstance(Context context) {
        if (mOppoDebug) {
            Slog.d(TAG, "getInstance.");
        }
        PswAlarmManagerFeature instance = InstanceHolder.INSTANCE;
        instance.init(context);
        return instance;
    }

    private void init(Context context) {
        if (!this.mIsInited) {
            if (context == null) {
                Slog.e(TAG, "failed to init for null context!");
                return;
            }
            if (mOppoDebug) {
                Slog.d(TAG, "PswAlarmManagerFeature init.");
            }
            this.mContext = context;
            this.mAlarmUpdateHelper = new AlarmUpdateHelper(this, this.mContext);
            this.mIsInited = true;
        }
    }

    @Override // com.android.server.IPswAlarmManagerFeature
    public void setPswAlarmManagerCallback(IPswAlarmManagerCallback callback) {
        if (mOppoDebug) {
            Slog.d(IPswAlarmManagerFeature.NAME, "impl setPswAlarmManagerCallback");
        }
        this.mPswAlarmManagerCallback = callback;
    }

    @Override // com.android.server.IPswAlarmManagerFeature
    public boolean isPswAlarmManagerSupport() {
        Slog.d(IPswAlarmManagerFeature.NAME, "impl isPswAlarmManagerSupport");
        return true;
    }

    @Override // com.android.server.IPswAlarmManagerFeature
    public void setMainVariables(ArrayList<AlarmManagerService.Alarm> pendingNonWakeupAlarms, ArrayList<AlarmManagerService.Batch> alarmBatches, SparseLongArray lastAllowWhileIdleDispatch, SparseArray<ArrayList<AlarmManagerService.Alarm>> pendingBackgroundAlarms, AlarmManagerService.Alarm pendingIdleUntil, AlarmManagerService.Alarm nextWakeFromIdle, long startCurrentDelayTime, long totalDelayTime, long maxDelayTime) {
        Slog.d(IPswAlarmManagerFeature.NAME, "impl setMainVariables");
        this.mPendingNonWakeupAlarms = pendingNonWakeupAlarms;
        this.mAlarmBatches = alarmBatches;
        this.mLastAllowWhileIdleDispatch = lastAllowWhileIdleDispatch;
        this.mPendingBackgroundAlarms = pendingBackgroundAlarms;
        this.mPendingIdleUntil = pendingIdleUntil;
        this.mNextWakeFromIdle = nextWakeFromIdle;
        this.mStartCurrentDelayTime = startCurrentDelayTime;
        this.mTotalDelayTime = totalDelayTime;
        this.mMaxDelayTime = maxDelayTime;
    }

    @Override // com.android.server.IPswAlarmManagerFeature
    public int convertType(int type, PendingIntent operation, String packageName) {
        Slog.d(IPswAlarmManagerFeature.NAME, "impl convertType");
        return this.mAlarmUpdateHelper.convertType(type, operation, packageName);
    }

    @Override // com.android.server.IPswAlarmManagerFeature
    public int getPendingImportantNonWakeupAlarmsSize() {
        Slog.d(IPswAlarmManagerFeature.NAME, "impl getPendingImportantNonWakeupAlarmsSize");
        return this.mPendingImportantNonWakeupAlarms.size();
    }

    @Override // com.android.server.IPswAlarmManagerFeature
    public boolean interactiveStateChangedLocked(boolean interactive, ArrayList<AlarmManagerService.Alarm> mPendingNonWakeupAlarms2, long mNonInteractiveStartTime, long mTotalDelayTime2, long mMaxDelayTime2, final PendingIntent mTimeTickSender, long mNonInteractiveTime) {
        Slog.d(IPswAlarmManagerFeature.NAME, "impl interactiveStateChangedLocked");
        if (this.mInteractive == interactive) {
            return true;
        }
        this.mInteractive = interactive;
        long nowELAPSED = SystemClock.elapsedRealtime();
        if (interactive) {
            mPendingNonWakeupAlarms2.addAll(this.mPendingImportantNonWakeupAlarms);
            this.mPendingImportantNonWakeupAlarms.clear();
            boolean hasTimeTick = false;
            if (nowELAPSED - mNonInteractiveStartTime > 60000) {
                Iterator<AlarmManagerService.Alarm> it = mPendingNonWakeupAlarms2.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    AlarmManagerService.Alarm a = it.next();
                    if (a.operation != null && "android.intent.action.TIME_TICK".equals(a.operation.getIntent().getAction())) {
                        hasTimeTick = true;
                        break;
                    }
                }
            } else {
                hasTimeTick = true;
            }
            if (!hasTimeTick) {
                if (mOppoDebug) {
                    Slog.d(TAG, "Sechdule a TIME_TICK");
                }
                this.mPswAlarmManagerCallback.onPostDelayed(new Runnable() {
                    /* class com.android.server.PswAlarmManagerFeature.AnonymousClass2 */

                    public void run() {
                        try {
                            if (mTimeTickSender != null) {
                                mTimeTickSender.send();
                            }
                        } catch (PendingIntent.CanceledException e) {
                        }
                    }
                }, DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION);
            }
            if (mPendingNonWakeupAlarms2.size() > 0) {
                long thisDelayTime = nowELAPSED - this.mStartCurrentDelayTime;
                long j = mTotalDelayTime2 + thisDelayTime;
                if (mMaxDelayTime2 < thisDelayTime) {
                }
                this.mPswAlarmManagerCallback.onCalculateDeliveryPriorities(mPendingNonWakeupAlarms2);
                Collections.sort(mPendingNonWakeupAlarms2, this.mAlarmDispatchComparator);
                this.mPswAlarmManagerCallback.onDeliverAlarmsLocked(mPendingNonWakeupAlarms2, nowELAPSED);
                if (mOppoDebug) {
                    Slog.d(TAG, "mPendingNonWakeupAlarms = " + mPendingNonWakeupAlarms2);
                }
                this.mPswAlarmManagerCallback.onClearPendingNonWakeupAlarmLocked(nowELAPSED, mPendingNonWakeupAlarms2);
            }
            if (mNonInteractiveStartTime <= 0 || nowELAPSED - mNonInteractiveStartTime <= mNonInteractiveTime) {
            }
        }
        this.mAlarmUpdateHelper.interactiveStateChangedLocked(interactive, nowELAPSED);
        return true;
    }

    public void netStateChangedLocked(boolean b, ArrayList<AlarmManagerService.Alarm> mPendingNonWakeupAlarms2) {
        Slog.d(IPswAlarmManagerFeature.NAME, "impl netStateChangedLocked");
        if (this.mNetstateInteractive != b) {
            this.mNetstateInteractive = b;
            if (b) {
                long nowELAPSED = SystemClock.elapsedRealtime();
                mPendingNonWakeupAlarms2.addAll(this.mPendingImportantNonWakeupAlarms);
                this.mPendingImportantNonWakeupAlarms.clear();
                if (mPendingNonWakeupAlarms2.size() > 0) {
                    this.mPswAlarmManagerCallback.onCalculateDeliveryPriorities(mPendingNonWakeupAlarms2);
                    Collections.sort(mPendingNonWakeupAlarms2, this.mAlarmDispatchComparator);
                    this.mPswAlarmManagerCallback.onDeliverAlarmsLocked(mPendingNonWakeupAlarms2, nowELAPSED);
                    if (mOppoDebug) {
                        Slog.v(TAG, "pendingAlarms = " + mPendingNonWakeupAlarms2);
                    }
                    this.mPswAlarmManagerCallback.onClearPendingNonWakeupAlarmLocked(nowELAPSED, mPendingNonWakeupAlarms2);
                }
            }
        }
    }

    @Override // com.android.server.IPswAlarmManagerFeature
    public void clearTriggerListNonWakeup() {
        Slog.d(IPswAlarmManagerFeature.NAME, "impl clearTriggerListNonWakeup");
        this.mTriggerListNonWakeup.clear();
    }

    @Override // com.android.server.IPswAlarmManagerFeature
    public boolean pswAlarmTrigger(ArrayList<AlarmManagerService.Alarm> triggerList) {
        Slog.d(IPswAlarmManagerFeature.NAME, "impl pswAlarmTrigger");
        long nowELAPSED = SystemClock.elapsedRealtime();
        boolean hasWakeup = triggerAlarmsOptLocked(triggerList, this.mTriggerListNonWakeup, nowELAPSED, System.currentTimeMillis());
        boolean bAllowNonWakeupDelay = this.mPswAlarmManagerCallback.onCheckAllowNonWakeupDelayLocked(nowELAPSED);
        if (bAllowNonWakeupDelay && this.mPendingNonWakeupAlarms.size() == 0) {
            this.mPendingImportantNonWakeupAlarms.size();
        }
        this.mPendingNonWakeupAlarms.addAll(this.mTriggerListNonWakeup);
        this.mPswAlarmManagerCallback.onRescheduleKernelAlarmsLocked();
        this.mPswAlarmManagerCallback.onUpdateNextAlarmClockLocked();
        if (hasWakeup) {
            if (this.mPendingNonWakeupAlarms.size() > 0 && !bAllowNonWakeupDelay) {
                this.mPswAlarmManagerCallback.onCalculateDeliveryPriorities(this.mPendingNonWakeupAlarms);
                triggerList.addAll(this.mPendingNonWakeupAlarms);
                this.mPswAlarmManagerCallback.onClearPendingNonWakeupAlarmLocked(nowELAPSED, this.mPendingNonWakeupAlarms);
            }
            if (this.mPendingImportantNonWakeupAlarms.size() > 0) {
                this.mPswAlarmManagerCallback.onCalculateDeliveryPriorities(this.mPendingImportantNonWakeupAlarms);
                triggerList.addAll(this.mPendingImportantNonWakeupAlarms);
                this.mPendingImportantNonWakeupAlarms.clear();
            }
            if (triggerList.size() > 0) {
                Collections.sort(triggerList, this.mAlarmDispatchComparator);
                long thisDelayTime = nowELAPSED - this.mStartCurrentDelayTime;
                this.mTotalDelayTime += thisDelayTime;
                if (this.mMaxDelayTime < thisDelayTime) {
                    this.mMaxDelayTime = thisDelayTime;
                }
            }
            ArraySet<Pair<String, Integer>> triggerPackages = new ArraySet<>();
            for (int i = 0; i < triggerList.size(); i++) {
                AlarmManagerService.Alarm a = triggerList.get(i);
                if (!this.mPswAlarmManagerCallback.isExemptFromAppStandby(a)) {
                    triggerPackages.add(Pair.create(a.sourcePackage, Integer.valueOf(UserHandle.getUserId(a.creatorUid))));
                }
            }
            this.mPswAlarmManagerCallback.onDeliverAlarmsLocked(triggerList, nowELAPSED);
            this.mPswAlarmManagerCallback.onReorderAlarmsBasedOnStandbyBuckets(triggerPackages);
            this.mPswAlarmManagerCallback.onRescheduleKernelAlarmsLocked();
            this.mPswAlarmManagerCallback.onUpdateNextAlarmClockLocked();
            return true;
        } else if (!bAllowNonWakeupDelay || this.mPendingNonWakeupAlarms.size() > 30) {
            if (this.mPendingNonWakeupAlarms.size() > 0) {
                this.mPswAlarmManagerCallback.onCalculateDeliveryPriorities(this.mPendingNonWakeupAlarms);
                triggerList.addAll(this.mPendingNonWakeupAlarms);
                this.mPswAlarmManagerCallback.onClearPendingNonWakeupAlarmLocked(nowELAPSED, this.mPendingNonWakeupAlarms);
            }
            if (this.mPendingImportantNonWakeupAlarms.size() > 0) {
                this.mPswAlarmManagerCallback.onCalculateDeliveryPriorities(this.mPendingImportantNonWakeupAlarms);
                triggerList.addAll(this.mPendingImportantNonWakeupAlarms);
                this.mPendingImportantNonWakeupAlarms.clear();
            }
            if (triggerList.size() > 0) {
                Collections.sort(triggerList, this.mAlarmDispatchComparator);
                long thisDelayTime2 = nowELAPSED - this.mStartCurrentDelayTime;
                this.mTotalDelayTime += thisDelayTime2;
                if (this.mMaxDelayTime < thisDelayTime2) {
                    this.mMaxDelayTime = thisDelayTime2;
                }
            }
            this.mPswAlarmManagerCallback.onRescheduleKernelAlarmsLocked();
            this.mPswAlarmManagerCallback.onUpdateNextAlarmClockLocked();
            this.mPswAlarmManagerCallback.onDeliverAlarmsLocked(triggerList, nowELAPSED);
            return true;
        } else {
            if (triggerList.size() > 0) {
                this.mPendingImportantNonWakeupAlarms.addAll(triggerList);
            }
            this.mPswAlarmManagerCallback.onRescheduleKernelAlarmsLocked();
            this.mPswAlarmManagerCallback.onUpdateNextAlarmClockLocked();
            return true;
        }
    }

    /* JADX INFO: Multiple debug info for r2v49 long: [D('minTime' long), D('hasWakeup' boolean)] */
    private boolean triggerAlarmsOptLocked(ArrayList<AlarmManagerService.Alarm> triggerList, ArrayList<AlarmManagerService.Alarm> mTriggerListNonWakeup2, long nowELAPSED, long nowRTC) {
        AlarmManagerService.Batch batch;
        boolean z;
        boolean needRepeat;
        boolean hasWakeup;
        boolean hasWakeup2;
        boolean needRepeat2;
        boolean z2;
        long j = nowELAPSED;
        boolean hasWakeup3 = false;
        boolean needRepeat3 = false;
        while (true) {
            if (this.mAlarmBatches.size() <= 0) {
                break;
            }
            AlarmManagerService.Batch batch2 = this.mAlarmBatches.get(0);
            if (batch2.start > j) {
                break;
            }
            this.mAlarmBatches.remove(0);
            int N = batch2.size();
            int i = 0;
            while (i < N) {
                AlarmManagerService.Alarm alarm = batch2.get(i);
                if ((alarm.flags & 4) != 0) {
                    batch = batch2;
                    long lastTime = this.mLastAllowWhileIdleDispatch.get(alarm.creatorUid, -1);
                    hasWakeup = hasWakeup3;
                    needRepeat = needRepeat3;
                    long minTime = lastTime + this.mPswAlarmManagerCallback.getWhileIdleMinIntervalLocked(alarm.creatorUid);
                    if (lastTime >= 0 && j < minTime) {
                        if (mOppoDebug) {
                            Slog.d(TAG, alarm + " too frequent, last = " + lastTime + ", now = " + j);
                        }
                        alarm.whenElapsed = minTime;
                        alarm.expectedWhenElapsed = minTime;
                        if (alarm.maxWhenElapsed < minTime) {
                            alarm.maxWhenElapsed = minTime;
                        }
                        alarm.expectedMaxWhenElapsed = alarm.maxWhenElapsed;
                        this.mPswAlarmManagerCallback.onSetImplLocked(alarm, true, false);
                        hasWakeup3 = hasWakeup;
                        needRepeat3 = needRepeat;
                        z = false;
                        i++;
                        j = nowELAPSED;
                        batch2 = batch;
                    }
                } else {
                    hasWakeup = hasWakeup3;
                    needRepeat = needRepeat3;
                    batch = batch2;
                }
                if (this.mPswAlarmManagerCallback.isBackgroundRestricted(alarm)) {
                    ArrayList<AlarmManagerService.Alarm> alarmsForUid = this.mPendingBackgroundAlarms.get(alarm.creatorUid);
                    if (alarmsForUid == null) {
                        alarmsForUid = new ArrayList<>();
                        this.mPendingBackgroundAlarms.put(alarm.creatorUid, alarmsForUid);
                    }
                    alarmsForUid.add(alarm);
                    hasWakeup3 = hasWakeup;
                    needRepeat3 = needRepeat;
                    z = false;
                    i++;
                    j = nowELAPSED;
                    batch2 = batch;
                } else {
                    alarm.count = 1;
                    if (alarm.wakeup) {
                        triggerList.add(alarm);
                        hasWakeup2 = true;
                        needRepeat2 = true;
                    } else if (this.mAlarmUpdateHelper.isImportantAlarm(alarm)) {
                        triggerList.add(alarm);
                        needRepeat2 = true;
                        hasWakeup2 = hasWakeup;
                    } else {
                        mTriggerListNonWakeup2.add(alarm);
                        needRepeat2 = false;
                        hasWakeup2 = hasWakeup;
                    }
                    if (this.mPendingIdleUntil == alarm) {
                        this.mPendingIdleUntil = null;
                        this.mPswAlarmManagerCallback.onRebatchAllAlarmsLocked(false);
                        this.mPswAlarmManagerCallback.onRestorePendingWhileIdleAlarmsLocked();
                    }
                    if (this.mNextWakeFromIdle == alarm) {
                        this.mNextWakeFromIdle = null;
                        z2 = false;
                        this.mPswAlarmManagerCallback.onRebatchAllAlarmsLocked(false);
                    } else {
                        z2 = false;
                    }
                    if (alarm.repeatInterval <= 0 || !needRepeat2) {
                        z = z2;
                    } else {
                        alarm.count = (int) (((long) alarm.count) + ((j - alarm.expectedWhenElapsed) / alarm.repeatInterval));
                        long delta = ((long) alarm.count) * alarm.repeatInterval;
                        long nextElapsed = alarm.whenElapsed + delta;
                        z = false;
                        this.mPswAlarmManagerCallback.onSetImplLocked(alarm.type, alarm.when + delta, nextElapsed, alarm.windowLength, AlarmManagerService.maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName);
                    }
                    if (alarm.wakeup) {
                        hasWakeup3 = true;
                    } else {
                        hasWakeup3 = hasWakeup2;
                    }
                    AlarmManager.AlarmClockInfo alarmClockInfo = alarm.alarmClock;
                    needRepeat3 = needRepeat2;
                    i++;
                    j = nowELAPSED;
                    batch2 = batch;
                }
            }
            j = nowELAPSED;
        }
        this.mPswAlarmManagerCallback.onCalculateDeliveryPriorities(triggerList);
        Collections.sort(triggerList, this.mAlarmDispatchComparator);
        if (mOppoDebug) {
            Slog.v(TAG, "TriggerAlarmsOpt Wakeup...");
            for (int i2 = 0; i2 < triggerList.size(); i2++) {
                Slog.v(TAG, "Triggering alarm #" + i2 + ": " + triggerList.get(i2));
            }
            Slog.v(TAG, "TriggerAlarmsOpt NonWakeup...");
            for (int j2 = 0; j2 < mTriggerListNonWakeup2.size(); j2++) {
                Slog.v(TAG, "Triggering alarm #" + j2 + ": " + triggerList.get(j2));
            }
        }
        return hasWakeup3;
    }

    @Override // com.android.server.AlarmUpdateHelper.Callbacks
    public void netStateChanged(boolean state) {
    }
}
