package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManagerInternal;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class BatteryController extends StateController {
    private static final String TAG = "JobScheduler.Batt";
    private static volatile BatteryController sController;
    private static final Object sCreationLock = null;
    private ChargingTracker mChargeTracker;
    private List<JobStatus> mTrackedTasks;

    public class ChargingTracker extends BroadcastReceiver {
        private boolean mBatteryHealthy;
        private boolean mCharging;

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_LOW");
            filter.addAction("android.intent.action.BATTERY_OKAY");
            filter.addAction("android.os.action.CHARGING");
            filter.addAction("android.os.action.DISCHARGING");
            BatteryController.this.mContext.registerReceiver(this, filter);
            BatteryManagerInternal batteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
            this.mBatteryHealthy = !batteryManagerInternal.getBatteryLevelLow();
            this.mCharging = batteryManagerInternal.isPowered(7);
        }

        boolean isOnStablePower() {
            return this.mCharging ? this.mBatteryHealthy : false;
        }

        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        public void onReceiveInternal(Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.BATTERY_LOW".equals(action)) {
                if (BatteryController.DEBUG) {
                    Slog.d(BatteryController.TAG, "Battery life too low to do work. @ " + SystemClock.elapsedRealtime());
                }
                this.mBatteryHealthy = false;
            } else if ("android.intent.action.BATTERY_OKAY".equals(action)) {
                if (BatteryController.DEBUG) {
                    Slog.d(BatteryController.TAG, "Battery life healthy enough to do work. @ " + SystemClock.elapsedRealtime());
                }
                this.mBatteryHealthy = true;
                BatteryController.this.maybeReportNewChargingState();
            } else if ("android.os.action.CHARGING".equals(action)) {
                if (BatteryController.DEBUG) {
                    Slog.d(BatteryController.TAG, "Received charging intent, fired @ " + SystemClock.elapsedRealtime());
                }
                this.mCharging = true;
                BatteryController.this.maybeReportNewChargingState();
            } else if ("android.os.action.DISCHARGING".equals(action)) {
                if (BatteryController.DEBUG) {
                    Slog.d(BatteryController.TAG, "Disconnected from power.");
                }
                this.mCharging = false;
                BatteryController.this.maybeReportNewChargingState();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.BatteryController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.BatteryController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.BatteryController.<clinit>():void");
    }

    public static BatteryController get(JobSchedulerService taskManagerService) {
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new BatteryController(taskManagerService, taskManagerService.getContext(), taskManagerService.getLock());
            }
        }
        return sController;
    }

    public ChargingTracker getTracker() {
        return this.mChargeTracker;
    }

    public static BatteryController getForTesting(StateChangedListener stateChangedListener, Context context) {
        return new BatteryController(stateChangedListener, context, new Object());
    }

    private BatteryController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mTrackedTasks = new ArrayList();
        this.mChargeTracker = new ChargingTracker();
        this.mChargeTracker.startTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        boolean isOnStablePower = this.mChargeTracker.isOnStablePower();
        if (taskStatus.hasChargingConstraint()) {
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setChargingConstraintSatisfied(isOnStablePower);
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.hasChargingConstraint()) {
            this.mTrackedTasks.remove(taskStatus);
        }
    }

    private void maybeReportNewChargingState() {
        boolean stablePower = this.mChargeTracker.isOnStablePower();
        if (DEBUG) {
            Slog.d(TAG, "maybeReportNewChargingState: " + stablePower);
        }
        boolean reportChange = false;
        synchronized (this.mLock) {
            for (JobStatus ts : this.mTrackedTasks) {
                if (ts.setChargingConstraintSatisfied(stablePower) != stablePower) {
                    reportChange = true;
                }
            }
        }
        if (reportChange) {
            this.mStateChangedListener.onControllerStateChanged();
        }
        if (stablePower) {
            this.mStateChangedListener.onRunJobNow(null);
        }
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("Battery: stable power = ");
        pw.println(this.mChargeTracker.isOnStablePower());
        pw.print("Tracking ");
        pw.print(this.mTrackedTasks.size());
        pw.println(":");
        for (int i = 0; i < this.mTrackedTasks.size(); i++) {
            JobStatus js = (JobStatus) this.mTrackedTasks.get(i);
            if (js.shouldDump(filterUid)) {
                pw.print("  #");
                js.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, js.getSourceUid());
                pw.println();
            }
        }
    }
}
