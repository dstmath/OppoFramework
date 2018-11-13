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
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
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
public class BatteryIdleController extends StateController {
    private static final String ACTION_SCREENOFF_IDLE = "com.android.server.task.controllers.BatteryIdleController.ACTION_SCREENOFF_IDLE";
    private static final int IDLE_DELAY = 300000;
    private static boolean OPPODEBUG = false;
    private static final String TAG = "JobScheduler.BattIdle";
    private static final int THRESHOLD_BATT_LOW = 30;
    private static final int THRESHOLD_BATT_NORMAL = 70;
    private static volatile BatteryIdleController sController;
    private static final Object sCreationLock = null;
    private BattIdleTracker mBattIdleTracker;
    private int mIdleDelay;
    private int mThresholdBattLow;
    private int mThresholdBattNormal;
    private List<JobStatus> mTrackedTasks;

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
            this.mIdleTriggerIntent = PendingIntent.getBroadcast(BatteryIdleController.this.mContext, 0, new Intent(BatteryIdleController.ACTION_SCREENOFF_IDLE).setPackage("android").setFlags(1073741824), 0);
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

        boolean isBattIdle() {
            return this.mBattIdle;
        }

        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        public void onReceiveInternal(Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                battChangeHandle(intent);
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                this.mScreenOff = false;
                this.mScreenOffIdle = false;
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                this.mScreenOff = true;
            } else if (action.equals(BatteryIdleController.ACTION_SCREENOFF_IDLE)) {
                PowerManager pm = (PowerManager) BatteryIdleController.this.mContext.getSystemService("power");
                if (!(pm == null || pm.isInteractive())) {
                    this.mScreenOffIdle = true;
                    if (BatteryIdleController.OPPODEBUG) {
                        Slog.d(BatteryIdleController.TAG, "mScreenOffIdle set true.");
                    }
                }
            }
            screenoffIdleAlarmHandle();
            battIdleHandle();
        }

        private void battChangeHandle(Intent intent) {
            int oldBatteryLevel = this.mBatteryLevel;
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
            if (this.mScreenOff && this.mPluged) {
                if (!this.mScreenOffPluged) {
                    this.mScreenOffPluged = true;
                    BatteryIdleController.this.mIdleDelay = OppoGuardElfConfigUtil.getInstance().getThreshBattIdleDelay() * 1000;
                    this.mAlarm.setExact(2, SystemClock.elapsedRealtime() + ((long) BatteryIdleController.this.mIdleDelay), this.mIdleTriggerIntent);
                }
            } else if (this.mScreenOffPluged) {
                this.mScreenOffPluged = false;
                this.mAlarm.cancel(this.mIdleTriggerIntent);
                if (BatteryIdleController.OPPODEBUG) {
                    Slog.d(BatteryIdleController.TAG, "cancel alarm.");
                }
            }
        }

        private void battIdleHandle() {
            boolean stateChange = false;
            if (this.mScreenOffIdle && this.mIsOnStablePower) {
                if (!this.mBattIdle) {
                    this.mBattIdle = true;
                    stateChange = true;
                }
            } else if (this.mBattIdle) {
                this.mBattIdle = false;
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
            int level = batteryChangedIntent.getIntExtra("level", 0);
            return (level * 100) / batteryChangedIntent.getIntExtra("scale", 100);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.BatteryIdleController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.BatteryIdleController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.BatteryIdleController.<clinit>():void");
    }

    public static BatteryIdleController get(JobSchedulerService taskManagerService) {
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new BatteryIdleController(taskManagerService, taskManagerService.getContext(), taskManagerService.getLock());
            }
        }
        return sController;
    }

    public BattIdleTracker getTracker() {
        return this.mBattIdleTracker;
    }

    public static BatteryIdleController getForTesting(StateChangedListener stateChangedListener, Context context) {
        return new BatteryIdleController(stateChangedListener, context, new Object());
    }

    private BatteryIdleController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mThresholdBattLow = 30;
        this.mThresholdBattNormal = 70;
        this.mIdleDelay = IDLE_DELAY;
        this.mTrackedTasks = new ArrayList();
        this.mBattIdleTracker = new BattIdleTracker();
        this.mBattIdleTracker.startTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        boolean isBattIdle = this.mBattIdleTracker.isBattIdle();
        if (taskStatus.hasBattIdleConstraint()) {
            synchronized (this.mTrackedTasks) {
                this.mTrackedTasks.add(taskStatus);
                taskStatus.setBattIdleConstraintSatisfied(isBattIdle);
            }
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.hasBattIdleConstraint()) {
            synchronized (this.mTrackedTasks) {
                this.mTrackedTasks.remove(taskStatus);
            }
        }
    }

    private void maybeReportNewChargingState() {
        boolean isBattIdle = this.mBattIdleTracker.isBattIdle();
        if (DEBUG) {
            Slog.d(TAG, "maybeReportNewChargingState: " + isBattIdle);
        }
        boolean reportChange = false;
        synchronized (this.mTrackedTasks) {
            for (JobStatus ts : this.mTrackedTasks) {
                if (ts.setBattIdleConstraintSatisfied(isBattIdle)) {
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

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.println("BattIdle.");
        pw.println("Batt Idle: " + this.mBattIdleTracker.isBattIdle());
        synchronized (this.mTrackedTasks) {
            Iterator<JobStatus> it = this.mTrackedTasks.iterator();
            if (it.hasNext()) {
                pw.print(String.valueOf(((JobStatus) it.next()).hashCode()));
            }
            while (it.hasNext()) {
                pw.print("," + String.valueOf(((JobStatus) it.next()).hashCode()));
            }
            pw.println();
        }
    }
}
