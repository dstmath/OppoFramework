package com.mediatek.server;

import android.app.AlarmManager;
import android.app.IAlarmListener;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.text.format.Time;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

public class MtkAlarmManagerService extends AlarmManagerService {
    static final long POWER_OFF_ALARM_BUFFER_TIME = 150000;
    boolean mIsWFDConnected = false;
    private long mNativeData;
    private Object mPowerOffAlarmLock = new Object();
    private final ArrayList<AlarmManagerService.Alarm> mPoweroffAlarms = new ArrayList<>();
    WFDStatusChangedReceiver mWFDStatusChangedReceiver;
    private Object mWaitThreadlock = new Object();

    public MtkAlarmManagerService(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void registerWFDStatusChangeReciever() {
        this.mWFDStatusChangedReceiver = new WFDStatusChangedReceiver();
    }

    /* access modifiers changed from: protected */
    public boolean isWFDConnected() {
        return this.mIsWFDConnected;
    }

    public void onStart() {
        MtkAlarmManagerService.super.onStart();
        if (this.mInjector != null) {
            this.mNativeData = this.mInjector.getNativeData();
        }
    }

    class WFDStatusChangedReceiver extends BroadcastReceiver {
        public WFDStatusChangedReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
            MtkAlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(intent.getAction())) {
                MtkAlarmManagerService.this.mIsWFDConnected = 2 == intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS").getActiveDisplayState();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isPowerOffAlarmType(int type) {
        if (type != 7) {
            return false;
        }
        return true;
    }

    class PowerOffAlarmComparator implements Comparator {
        PowerOffAlarmComparator() {
        }

        @Override // java.util.Comparator
        public int compare(Object o1, Object o2) {
            AlarmManagerService.Alarm e1 = (AlarmManagerService.Alarm) o1;
            AlarmManagerService.Alarm e2 = (AlarmManagerService.Alarm) o2;
            if (e1.when > e2.when) {
                return 1;
            }
            if (e1.when == e2.when) {
                return 0;
            }
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public boolean schedulePoweroffAlarm(int type, long triggerAtTime, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock, String callingPackage) {
        long triggerAtTime2;
        Object obj;
        if (type != 7) {
            return true;
        }
        if (this.mNativeData == -1) {
            Slog.w("AlarmManager", "alarm driver not open ,return!");
            return false;
        }
        Slog.d("AlarmManager", "alarm set type 7 , package name " + operation.getTargetPackage());
        String packageName = operation.getTargetPackage();
        long nowTime = System.currentTimeMillis();
        if (packageName.equals("com.android.settings") || packageName.equals("com.coloros.alarmclock")) {
            triggerAtTime2 = triggerAtTime;
        } else {
            triggerAtTime2 = triggerAtTime - nowTime > POWER_OFF_ALARM_BUFFER_TIME ? triggerAtTime - POWER_OFF_ALARM_BUFFER_TIME : triggerAtTime;
        }
        if (triggerAtTime2 < nowTime) {
            Slog.w("AlarmManager", "PowerOff alarm set time is wrong! nowTime = " + nowTime + " ; triggerAtTime = " + triggerAtTime2);
            return false;
        }
        Slog.d("AlarmManager", "PowerOff alarm TriggerTime = " + triggerAtTime2 + " now = " + nowTime);
        Object obj2 = this.mPowerOffAlarmLock;
        synchronized (obj2) {
            try {
                removePoweroffAlarmLocked(operation.getTargetPackage());
                obj = obj2;
                addPoweroffAlarmLocked(new AlarmManagerService.Alarm(type, triggerAtTime2, 0, 0, 0, interval, operation, directReceiver, listenerTag, workSource, 0, alarmClock, UserHandle.getCallingUserId(), callingPackage));
                if (this.mPoweroffAlarms.size() > 0) {
                    Slog.i("AlarmManager", "whb mPoweroffAlarms.size=" + this.mPoweroffAlarms.size());
                    Collections.sort(this.mPoweroffAlarms, new PowerOffAlarmComparator());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    for (int i = 0; i < this.mPoweroffAlarms.size(); i++) {
                        String time = sdf.format(new Date(this.mPoweroffAlarms.get(i).when));
                        Slog.i("AlarmManager", "whb alram.when=" + time);
                    }
                    Slog.w("AlarmManager", "whb resetPoweroffAlarm 1");
                    resetPoweroffAlarm(this.mPoweroffAlarms.get(0));
                }
                return true;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updatePoweroffAlarmtoNowRtc() {
        updatePoweroffAlarm(System.currentTimeMillis());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
        return;
     */
    private void updatePoweroffAlarm(long nowRTC) {
        synchronized (this.mPowerOffAlarmLock) {
            if (this.mPoweroffAlarms.size() != 0) {
                if (this.mPoweroffAlarms.get(0).when <= nowRTC) {
                    Iterator<AlarmManagerService.Alarm> it = this.mPoweroffAlarms.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        } else if (it.next().when > nowRTC) {
                            break;
                        } else {
                            Slog.w("AlarmManager", "power off alarm update deleted");
                            it.remove();
                        }
                    }
                    if (this.mPoweroffAlarms.size() > 0) {
                        Collections.sort(this.mPoweroffAlarms, new PowerOffAlarmComparator());
                        resetPoweroffAlarm(this.mPoweroffAlarms.get(0));
                    }
                }
            }
        }
    }

    private int addPoweroffAlarmLocked(AlarmManagerService.Alarm alarm) {
        ArrayList<AlarmManagerService.Alarm> alarmList = this.mPoweroffAlarms;
        int index = Collections.binarySearch(alarmList, alarm, sIncreasingTimeOrder);
        if (index < 0) {
            index = (0 - index) - 1;
        }
        Slog.v("AlarmManager", "Adding alarm " + alarm + " at " + index);
        alarmList.add(index, alarm);
        Slog.v("AlarmManager", "alarms: " + alarmList.size() + " type: " + alarm.type);
        int position = 0;
        Iterator<AlarmManagerService.Alarm> it = alarmList.iterator();
        while (it.hasNext()) {
            AlarmManagerService.Alarm a = it.next();
            Time time = new Time();
            time.set(a.when);
            Slog.v("AlarmManager", position + ": " + time.format("%b %d %I:%M:%S %p") + " " + a.operation.getTargetPackage());
            position++;
        }
        return index;
    }

    private void removePoweroffAlarmLocked(String packageName) {
        ArrayList<AlarmManagerService.Alarm> alarmList = this.mPoweroffAlarms;
        if (alarmList.size() > 0) {
            Iterator<AlarmManagerService.Alarm> it = alarmList.iterator();
            while (it.hasNext()) {
                if (it.next().operation.getTargetPackage().equals(packageName)) {
                    it.remove();
                }
            }
        }
    }

    private void resetPoweroffAlarm(AlarmManagerService.Alarm alarm) {
        String setPackageName = alarm.operation.getTargetPackage();
        long latestTime = alarm.when;
        long j = this.mNativeData;
        if (j == 0 || j == -1) {
            Slog.i("AlarmManager", " do not set alarm to RTC when fd close ");
            return;
        }
        if (setPackageName.equals("com.android.deskclock")) {
            set(this.mNativeData, 7, latestTime / 1000, (latestTime % 1000) * 1000 * 1000);
        } else if (setPackageName.equals("com.mediatek.sqa8.aging")) {
            set(this.mNativeData, 7, latestTime / 1000, (latestTime % 1000) * 1000 * 1000);
        } else if (setPackageName.equals("com.coloros.alarmclock")) {
            SystemProperties.set("persist.sys.bootpackage", "2");
            set(this.mNativeData, 7, latestTime / 1000, (latestTime % 1000) * 1000 * 1000);
            SystemProperties.set("sys.power_off_alarm", Long.toString(latestTime / 1000));
        } else if (setPackageName.equals("com.android.settings") || setPackageName.equals("com.oppo.engineermode")) {
            SystemProperties.set("persist.sys.bootpackage", "2");
            set(this.mNativeData, 7, latestTime / 1000, (latestTime % 1000) * 1000 * 1000);
            SystemProperties.set("sys.power_off_alarm", Long.toString(latestTime / 1000));
        } else {
            Slog.w("AlarmManager", "unknown package (" + setPackageName + ") to set power off alarm");
        }
        Slog.i("AlarmManager", "reset power off alarm is " + setPackageName);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(latestTime));
        Slog.i("AlarmManager", "sys.power_off_alarm is " + time);
    }

    public void cancelPoweroffAlarmImpl(String name) {
        Slog.i("AlarmManager", "remove power off alarm pacakge name " + name);
        synchronized (this.mPowerOffAlarmLock) {
            removePoweroffAlarmLocked(name);
            if (!(this.mNativeData == 0 || this.mNativeData == -1)) {
                if (name.equals("com.android.deskclock")) {
                    set(this.mNativeData, 7, 0, 0);
                } else if (name.equals("com.coloros.alarmclock")) {
                    set(this.mNativeData, 7, 0, 0);
                    SystemProperties.set("sys.power_off_alarm", Long.toString(0));
                } else if (name.equals("com.android.settings") || name.equals("com.oppo.engineermode")) {
                    set(this.mNativeData, 7, 0, 0);
                    SystemProperties.set("sys.power_off_alarm", Long.toString(0));
                }
            }
            if (this.mPoweroffAlarms.size() > 0) {
                Collections.sort(this.mPoweroffAlarms, new PowerOffAlarmComparator());
                resetPoweroffAlarm(this.mPoweroffAlarms.get(0));
            }
        }
    }
}
