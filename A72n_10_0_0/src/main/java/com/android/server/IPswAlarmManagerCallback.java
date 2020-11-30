package com.android.server;

import android.app.AlarmManager;
import android.app.IAlarmListener;
import android.app.PendingIntent;
import android.os.WorkSource;
import android.util.ArraySet;
import android.util.Pair;
import com.android.server.AlarmManagerService;
import java.util.ArrayList;

public interface IPswAlarmManagerCallback {
    boolean IsBackgroundRestricted(AlarmManagerService.Alarm alarm);

    long getWhileIdleMinIntervalLocked(int i);

    boolean isBackgroundRestricted(AlarmManagerService.Alarm alarm);

    boolean isExemptFromAppStandby(AlarmManagerService.Alarm alarm);

    void onCalculateDeliveryPriorities(ArrayList<AlarmManagerService.Alarm> arrayList);

    boolean onCheckAllowNonWakeupDelayLocked(long j);

    void onClearPendingNonWakeupAlarmLocked(long j, ArrayList<AlarmManagerService.Alarm> arrayList);

    void onDeliverAlarmsLocked(ArrayList<AlarmManagerService.Alarm> arrayList, long j);

    void onPostDelayed(Runnable runnable, int i);

    void onRebatchAllAlarmsLocked(boolean z);

    void onReorderAlarmsBasedOnStandbyBuckets(ArraySet<Pair<String, Integer>> arraySet);

    void onRescheduleKernelAlarmsLocked();

    void onRestorePendingWhileIdleAlarmsLocked();

    void onSetImplLocked(int i, long j, long j2, long j3, long j4, long j5, PendingIntent pendingIntent, IAlarmListener iAlarmListener, String str, int i2, boolean z, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClockInfo, int i3, String str2);

    void onSetImplLocked(AlarmManagerService.Alarm alarm, boolean z, boolean z2);

    void onUpdateNextAlarmClockLocked();
}
