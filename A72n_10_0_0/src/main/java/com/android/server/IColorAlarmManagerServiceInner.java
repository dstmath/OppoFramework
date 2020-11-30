package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.WorkSource;
import com.android.server.AlarmManagerService;
import java.util.ArrayList;

public interface IColorAlarmManagerServiceInner {
    public static final IColorAlarmManagerServiceInner DEFAULT = new IColorAlarmManagerServiceInner() {
        /* class com.android.server.IColorAlarmManagerServiceInner.AnonymousClass1 */
    };

    default AlarmManagerService.BroadcastStats getStatsLocked(PendingIntent pi) {
        return null;
    }

    default void updateNextAlarmClockLocked() {
    }

    default void setImplIntelnalLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, int flags, boolean doValidate, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock, int userId) {
    }

    default void decrementAlarmCount(int uid, int decrement) {
    }

    default void deliverPendingAlarmsForHans(ArrayList<AlarmManagerService.Alarm> arrayList) {
    }
}
