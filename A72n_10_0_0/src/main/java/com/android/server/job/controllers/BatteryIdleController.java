package com.android.server.job.controllers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManagerInternal;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.job.JobSchedulerService;
import com.android.server.pm.PackageManagerService;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BatteryIdleController extends StateController {
    private static final String ACTION_SCREENOFF_IDLE = "com.android.server.task.controllers.BatteryIdleController.ACTION_SCREENOFF_IDLE";
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final int IDLE_DELAY = 300000;
    private static boolean OPPODEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "JobScheduler.BattIdle";
    private static final int THRESHOLD_BATT_LOW = 30;
    private static final int THRESHOLD_BATT_NORMAL = 70;
    private static volatile BatteryIdleController sController;
    private static final Object sCreationLock = new Object();
    private BattIdleTracker mBattIdleTracker = new BattIdleTracker();
    private int mIdleDelay = 300000;
    private int mThresholdBattLow = 30;
    private int mThresholdBattNormal = 70;
    private List<JobStatus> mTrackedTasks = new ArrayList();

    @VisibleForTesting
    public BattIdleTracker getTracker() {
        return this.mBattIdleTracker;
    }

    public BatteryIdleController(JobSchedulerService service) {
        super(service);
        this.mBattIdleTracker.startTracking();
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        boolean isBattIdle = this.mBattIdleTracker.isBattIdle();
        OppoBaseJobStatus baseJobStatus = typeCastingJobStatus(taskStatus);
        if (baseJobStatus != null && baseJobStatus.hasBattIdleConstraint()) {
            synchronized (this.mTrackedTasks) {
                this.mTrackedTasks.add(taskStatus);
                baseJobStatus.setBattIdleConstraintSatisfied(isBattIdle);
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        OppoBaseJobStatus baseJobStatus = typeCastingJobStatus(taskStatus);
        if (baseJobStatus != null && baseJobStatus.hasBattIdleConstraint()) {
            synchronized (this.mTrackedTasks) {
                this.mTrackedTasks.remove(taskStatus);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeReportNewChargingState() {
        boolean isBattIdle = this.mBattIdleTracker.isBattIdle();
        if (DEBUG) {
            Slog.d(TAG, "maybeReportNewChargingState: " + isBattIdle);
        }
        boolean reportChange = false;
        synchronized (this.mTrackedTasks) {
            for (JobStatus ts : this.mTrackedTasks) {
                OppoBaseJobStatus baseJobStatus = typeCastingJobStatus(ts);
                if (baseJobStatus != null && baseJobStatus.setBattIdleConstraintSatisfied(isBattIdle)) {
                    reportChange = true;
                }
            }
        }
        if (reportChange) {
            this.mStateChangedListener.onControllerStateChanged();
        }
        if (isBattIdle) {
            this.mStateChangedListener.onRunJobNow(null);
        }
    }

    public class BattIdleTracker extends BroadcastReceiver {
        private AlarmManager mAlarm;
        private boolean mBattIdle = false;
        private int mBatteryLevel;
        private int mBatteryLevelPlugIn;
        private PendingIntent mIdleTriggerIntent;
        private boolean mIsOnStablePower = false;
        private int mPlugType = 0;
        private boolean mPluged = false;
        private boolean mPowerIncrease = false;
        private boolean mScreenOff = false;
        private boolean mScreenOffIdle = false;
        private boolean mScreenOffPluged = false;

        public BattIdleTracker() {
            this.mAlarm = (AlarmManager) BatteryIdleController.this.mContext.getSystemService("alarm");
            this.mIdleTriggerIntent = PendingIntent.getBroadcast(BatteryIdleController.this.mContext, 0, new Intent(BatteryIdleController.ACTION_SCREENOFF_IDLE).setPackage(PackageManagerService.PLATFORM_PACKAGE_NAME).setFlags(1073741824), 0);
        }

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_CHANGED");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction(BatteryIdleController.ACTION_SCREENOFF_IDLE);
            BatteryIdleController.this.mContext.registerReceiver(this, filter);
            this.mBatteryLevel = ((BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class)).getBatteryLevel();
            this.mBatteryLevelPlugIn = this.mBatteryLevel;
        }

        /* access modifiers changed from: package-private */
        public boolean isBattIdle() {
            return this.mBattIdle;
        }

        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        @VisibleForTesting
        public void onReceiveInternal(Intent intent) {
            PowerManager pm;
            String action = intent.getAction();
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                battChangeHandle(intent);
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                this.mScreenOff = false;
                this.mScreenOffIdle = false;
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                this.mScreenOff = true;
            } else if (action.equals(BatteryIdleController.ACTION_SCREENOFF_IDLE) && (pm = (PowerManager) BatteryIdleController.this.mContext.getSystemService("power")) != null && !pm.isInteractive()) {
                this.mScreenOffIdle = true;
                if (BatteryIdleController.OPPODEBUG) {
                    Slog.d(BatteryIdleController.TAG, "mScreenOffIdle set true.");
                }
            }
            screenoffIdleAlarmHandle();
            battIdleHandle();
        }

        private void battChangeHandle(Intent intent) {
            int i = this.mBatteryLevel;
            this.mBatteryLevel = getBatteryLevel(intent);
            int oldPlugType = this.mPlugType;
            this.mPlugType = intent.getIntExtra("plugged", 0);
            if (this.mPlugType != 0) {
                this.mPluged = true;
                if (!this.mIsOnStablePower) {
                    if (oldPlugType == 0) {
                        this.mBatteryLevelPlugIn = this.mBatteryLevel;
                        BatteryIdleController.this.mThresholdBattLow = OppoGuardElfConfigUtil.getInstance().getThreshBattIdleLowLevel();
                        BatteryIdleController.this.mThresholdBattNormal = OppoGuardElfConfigUtil.getInstance().getThreshBattIdleNormalLevel();
                    } else if (this.mBatteryLevel > this.mBatteryLevelPlugIn) {
                        this.mPowerIncrease = true;
                    }
                    if (this.mBatteryLevel >= BatteryIdleController.this.mThresholdBattNormal || (this.mPowerIncrease && this.mBatteryLevel >= BatteryIdleController.this.mThresholdBattLow)) {
                        this.mIsOnStablePower = true;
                        if (BatteryIdleController.OPPODEBUG) {
                            Slog.d(BatteryIdleController.TAG, "mIsOnStablePower set true.");
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            this.mPluged = false;
            this.mPowerIncrease = false;
            if (this.mIsOnStablePower) {
                this.mIsOnStablePower = false;
                if (BatteryIdleController.OPPODEBUG) {
                    Slog.d(BatteryIdleController.TAG, "mIsOnStablePower set false.");
                }
            }
        }

        private void screenoffIdleAlarmHandle() {
            if (!this.mScreenOff || !this.mPluged) {
                if (this.mScreenOffPluged) {
                    this.mScreenOffPluged = false;
                    this.mAlarm.cancel(this.mIdleTriggerIntent);
                    if (BatteryIdleController.OPPODEBUG) {
                        Slog.d(BatteryIdleController.TAG, "cancel alarm.");
                    }
                }
            } else if (!this.mScreenOffPluged) {
                this.mScreenOffPluged = true;
                BatteryIdleController.this.mIdleDelay = OppoGuardElfConfigUtil.getInstance().getThreshBattIdleDelay() * 1000;
                long nowElapsed = SystemClock.elapsedRealtime();
                this.mAlarm.setExact(2, ((long) BatteryIdleController.this.mIdleDelay) + nowElapsed, this.mIdleTriggerIntent);
            }
        }

        private void battIdleHandle() {
            boolean stateChange = false;
            if (!this.mScreenOffIdle || !this.mIsOnStablePower) {
                if (this.mBattIdle) {
                    this.mBattIdle = false;
                    stateChange = true;
                }
            } else if (!this.mBattIdle) {
                this.mBattIdle = true;
                stateChange = true;
            }
            if (stateChange) {
                if (BatteryIdleController.OPPODEBUG) {
                    Slog.d(BatteryIdleController.TAG, "mBattIdle = " + this.mBattIdle);
                }
                BatteryIdleController.this.maybeReportNewChargingState();
            }
        }

        private int getBatteryLevel(Intent batteryChangedIntent) {
            return (batteryChangedIntent.getIntExtra("level", 0) * 100) / batteryChangedIntent.getIntExtra("scale", 100);
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
    }

    private static OppoBaseJobStatus typeCastingJobStatus(JobStatus jobStatus) {
        return (OppoBaseJobStatus) ColorTypeCastingHelper.typeCasting(OppoBaseJobStatus.class, jobStatus);
    }
}
