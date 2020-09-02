package com.android.server;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.pm.PackageManagerService;
import com.android.server.usage.AppStandbyController;
import java.util.Calendar;

public class MountServiceIdler extends JobService {
    private static long DAY_IN_MILLIS = (HOUR_IN_MILLIS * 24);
    private static long HIGH_BATTERY_LEVEL = 60;
    private static long HOUR_IN_MILLIS = AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
    private static int MOUNT_JOB_ID = 808;
    private static final String TAG = "MountServiceIdler";
    private static ComponentName sIdleService = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, MountServiceIdler.class.getName());
    /* access modifiers changed from: private */
    public Runnable mFinishCallback = new Runnable() {
        /* class com.android.server.MountServiceIdler.AnonymousClass1 */

        public void run() {
            Slog.i(MountServiceIdler.TAG, "Got mount service completion callback");
            synchronized (MountServiceIdler.this.mFinishCallback) {
                if (MountServiceIdler.this.mStarted) {
                    MountServiceIdler.this.jobFinished(MountServiceIdler.this.mJobParams, false);
                    boolean unused = MountServiceIdler.this.mStarted = false;
                }
            }
            MountServiceIdler.scheduleIdlePass(MountServiceIdler.this);
        }
    };
    /* access modifiers changed from: private */
    public JobParameters mJobParams;
    /* access modifiers changed from: private */
    public boolean mStarted;

    public boolean onStartJob(JobParameters params) {
        try {
            ActivityManager.getService().performIdleMaintenance();
        } catch (RemoteException e) {
        }
        this.mJobParams = params;
        StorageManagerService ms = StorageManagerService.sSelf;
        if (ms == null || !ms.idleMaintable()) {
            Slog.w(TAG, "try start job, but not idle");
            return false;
        }
        synchronized (this.mFinishCallback) {
            this.mStarted = true;
        }
        ms.runIdleMaint(this.mFinishCallback);
        return true;
    }

    public boolean onStopJob(JobParameters params) {
        StorageManagerService ms = StorageManagerService.sSelf;
        if (ms != null) {
            Slog.w(TAG, "stop job, abort idle maint");
            ms.abortIdleMaint(null);
            synchronized (this.mFinishCallback) {
                this.mStarted = false;
            }
        }
        return false;
    }

    public static void scheduleIdlePass(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService("jobscheduler");
        long timeToMidnight = tomorrowMidnight().getTimeInMillis() - System.currentTimeMillis();
        JobInfo.Builder builder = new JobInfo.Builder(MOUNT_JOB_ID, sIdleService);
        StorageManagerService ms = StorageManagerService.sSelf;
        if (ms == null || !ms.maintAborted()) {
            Slog.w(TAG, "schedule tomorrow job");
            if (!Environment.isWhiteListMcp()) {
                builder.setRequiresCharging(true);
            } else {
                Slog.i(TAG, "isWhiteListMcp,scheduleIdlePass, not require charging");
            }
            builder.setRequiresCharging(true);
            builder.setMinimumLatency(timeToMidnight);
            tm.schedule(builder.build());
        }
    }

    public static void scheduleIdlePass(Context context, long delayMillis) {
        JobScheduler tm = (JobScheduler) context.getSystemService("jobscheduler");
        JobInfo.Builder builder = new JobInfo.Builder(MOUNT_JOB_ID, sIdleService);
        StorageManagerService ms = StorageManagerService.sSelf;
        if (ms != null) {
            int batteryLevel = ((BatteryManager) context.getSystemService("batterymanager")).getIntProperty(4);
            long lastMaintenance = ms.lastMaintenance();
            long noMaintenanceTime = 0;
            if (lastMaintenance != 0) {
                noMaintenanceTime = System.currentTimeMillis() - lastMaintenance;
            }
            Slog.w(TAG, "schedule immediate job, delayMillis:" + delayMillis + ", noMaintenanceTime:" + noMaintenanceTime + ", battery level:" + batteryLevel);
            if (noMaintenanceTime < DAY_IN_MILLIS * 7 || ((long) batteryLevel) < HIGH_BATTERY_LEVEL) {
                builder.setRequiresCharging(true);
            }
            builder.setMinimumLatency(delayMillis);
            tm.schedule(builder.build());
        }
    }

    public static void cancleSchedule(Context context) {
        ((JobScheduler) context.getSystemService("jobscheduler")).cancel(MOUNT_JOB_ID);
        Slog.w(TAG, "cancel job");
    }

    private static Calendar tomorrowMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(11, 23);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        if (calendar.getTimeInMillis() - System.currentTimeMillis() <= 0) {
            calendar.add(5, 1);
        }
        return calendar;
    }
}
