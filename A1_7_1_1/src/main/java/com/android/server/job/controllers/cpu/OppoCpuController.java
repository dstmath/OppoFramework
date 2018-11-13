package com.android.server.job.controllers.cpu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.StateController;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoCpuController extends StateController {
    private static final int MSG_START_COLLECT = 1001;
    private static final int MSG_STOP_COLLECT = 1002;
    private static final int MSG_UPDATE_CPU = 1003;
    private static boolean OPPODEBUG = false;
    private static final String OPPO_CPU_MONITOR_NOTIFY = "oppo.intent.action.OPPO_CPU_MONITOR_NOTIFY";
    private static final String PROP_SAVED_CPU_LEVEL = "oppo.saved.cpu.level";
    private static final String PROP_SAVED_CPU_VALUE = "oppo.saved.cpu.value";
    private static final String TAG = "JobScheduler.Cpu";
    private static volatile OppoCpuController sController;
    private static final Object sCreationLock = null;
    DecimalFormat cpuDf;
    final HandlerThread cpuThread;
    OppoGuardElfConfigUtil mConfig;
    private Context mContext;
    private boolean mCpuAllow;
    private CpuTrackerHandler mCpuHandler;
    private int mCpuHeavyCount;
    private int mCpuLevel;
    private int mCpuMiddleCount;
    private int mCpuNormalCount;
    private final Object mCpuObject;
    private int mCpuSlightCount;
    CpuTraker mCpuTraker;
    CpuUtils mCpuUtils;
    private float mCurrentPercent;
    private long mLastNotifyAppTime;
    private JobSchedulerService mService;
    private List<JobStatus> mTrackedTasks;

    private class CpuTrackerHandler extends Handler {
        public CpuTrackerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 1001:
                    if (OppoCpuController.this.mConfig.getTotalCpuMonitorSwitch() && OppoCpuController.this.mCpuUtils != null) {
                        OppoCpuController.this.mCpuUtils.update();
                        sendEmptyMessageDelayed(OppoCpuController.MSG_UPDATE_CPU, OppoCpuController.this.mConfig.getIntervalTotalCpuSample());
                        return;
                    }
                    return;
                case 1002:
                    removeMessages(OppoCpuController.MSG_UPDATE_CPU);
                    OppoCpuController.this.stopCpuConstraint();
                    return;
                case OppoCpuController.MSG_UPDATE_CPU /*1003*/:
                    if (OppoCpuController.this.mCpuUtils != null) {
                        OppoCpuController.this.mCpuUtils.update();
                        OppoCpuController.this.mCurrentPercent = OppoCpuController.this.mCpuUtils.getSpecialPercent();
                        OppoCpuController.this.handleCpuUpdate(OppoCpuController.this.mCurrentPercent);
                        if (OppoCpuController.this.mConfig.getTotalCpuMonitorSwitch()) {
                            sendEmptyMessageDelayed(OppoCpuController.MSG_UPDATE_CPU, OppoCpuController.this.mConfig.getIntervalTotalCpuSample());
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class CpuTraker extends BroadcastReceiver {
        /* synthetic */ CpuTraker(OppoCpuController this$0, CpuTraker cpuTraker) {
            this();
        }

        private CpuTraker() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                OppoCpuController.this.mCpuHandler.sendEmptyMessage(1001);
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                OppoCpuController.this.mCpuHandler.sendEmptyMessage(1002);
            }
        }

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            OppoCpuController.this.mContext.registerReceiver(this, filter);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.cpu.OppoCpuController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.cpu.OppoCpuController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.cpu.OppoCpuController.<clinit>():void");
    }

    public static OppoCpuController get(JobSchedulerService taskManagerService) {
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new OppoCpuController(taskManagerService, taskManagerService.getContext(), taskManagerService.getLock());
            }
        }
        return sController;
    }

    public static OppoCpuController getForTesting(StateChangedListener stateChangedListener, Context context) {
        return new OppoCpuController(stateChangedListener, context, new Object());
    }

    private OppoCpuController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mCpuLevel = 0;
        this.mCpuAllow = true;
        this.mTrackedTasks = new ArrayList();
        this.mCpuNormalCount = 0;
        this.mCpuSlightCount = 0;
        this.mCpuMiddleCount = 0;
        this.mCpuHeavyCount = 0;
        this.cpuDf = new DecimalFormat("######0.0");
        this.cpuThread = new HandlerThread("CpuJob", 10);
        this.mCpuHandler = null;
        this.mCpuObject = new Object();
        if (stateChangedListener instanceof JobSchedulerService) {
            this.mService = (JobSchedulerService) stateChangedListener;
        }
        this.mContext = context;
        this.mConfig = OppoGuardElfConfigUtil.getInstance();
        this.cpuThread.start();
        this.mCpuHandler = new CpuTrackerHandler(this.cpuThread.getLooper());
        initCpuTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        boolean z = false;
        if (jobStatus != null && jobStatus.hasCpuConstraint()) {
            synchronized (this.mCpuObject) {
                synchronized (this.mTrackedTasks) {
                    if (OPPODEBUG) {
                        Slog.d(TAG, "maybeStartTrackingJob job " + jobStatus.getJob() + ", mCpuLevel=" + this.mCpuLevel);
                    }
                    this.mTrackedTasks.add(jobStatus);
                    if (this.mCpuLevel == 0) {
                        z = true;
                    }
                    jobStatus.setCpuConstraintSatisfied(z);
                    jobStatus.cpuLevel.getAndSet(this.mCpuLevel);
                    jobStatus.oldCpuLevel.getAndSet(this.mCpuLevel);
                }
            }
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
        if (jobStatus != null && jobStatus.hasCpuConstraint()) {
            synchronized (this.mTrackedTasks) {
                if (OPPODEBUG) {
                    Slog.d(TAG, "maybeStopTrackingJob job " + jobStatus.getJob());
                }
                this.mTrackedTasks.remove(jobStatus);
            }
        }
    }

    private void maybeReportNewCpuState() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                boolean reportChange = false;
                synchronized (OppoCpuController.this.mCpuObject) {
                    if (OppoCpuController.DEBUG) {
                        Slog.d(OppoCpuController.TAG, "maybeReportNewCpuState:  " + OppoCpuController.this.mCpuLevel + ", allow=" + OppoCpuController.this.mCpuAllow);
                    }
                    synchronized (OppoCpuController.this.mTrackedTasks) {
                        for (JobStatus ts : OppoCpuController.this.mTrackedTasks) {
                            int previousLevel = ts.cpuLevel.getAndSet(OppoCpuController.this.mCpuLevel);
                            ts.oldCpuLevel.getAndSet(previousLevel);
                            boolean previousAllow = ts.isCpuConstraintSatisfied();
                            boolean allow = previousAllow ? OppoCpuController.this.mCpuAllow : OppoCpuController.this.mCpuLevel == 0;
                            ts.setCpuConstraintSatisfied(allow);
                            if (previousLevel != OppoCpuController.this.mCpuLevel || previousAllow != allow) {
                                reportChange = true;
                            }
                        }
                    }
                }
                if (reportChange) {
                    OppoCpuController.this.mStateChangedListener.onControllerStateChanged();
                }
            }
        });
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.println("OppoCpuController.");
        pw.println("OppoCpuController : mCpuLevel = " + this.mCpuLevel);
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

    private void handleCpuUpdate(float percent) {
        int reportLevel;
        boolean z = true;
        if (percent < ((float) this.mConfig.getThreshTotalCpuSlight())) {
            this.mCpuNormalCount++;
            if (this.mCpuNormalCount >= 2) {
                this.mCpuHeavyCount = 0;
                this.mCpuMiddleCount = 0;
                this.mCpuSlightCount = 0;
            }
        } else if (percent < ((float) this.mConfig.getThreshTotalCpuMiddle())) {
            this.mCpuHeavyCount = 0;
            this.mCpuMiddleCount = 0;
            this.mCpuNormalCount = 0;
            this.mCpuSlightCount++;
        } else if (percent < ((float) this.mConfig.getThreshTotalCpuHeavy())) {
            this.mCpuSlightCount++;
            this.mCpuMiddleCount++;
            this.mCpuHeavyCount = 0;
            this.mCpuNormalCount = 0;
        } else {
            this.mCpuNormalCount = 0;
            this.mCpuSlightCount++;
            this.mCpuMiddleCount++;
            this.mCpuHeavyCount++;
        }
        synchronized (this.mCpuObject) {
            if (this.mCpuHeavyCount >= this.mConfig.getThreshCountContinuousHeavy()) {
                this.mCpuLevel = 3;
            } else if (this.mCpuMiddleCount >= this.mConfig.getThreshCountContinuousMiddle()) {
                this.mCpuLevel = 2;
            } else if (this.mCpuSlightCount >= this.mConfig.getThreshCountContinuousSlight()) {
                this.mCpuLevel = 1;
            } else if (this.mCpuNormalCount >= 2) {
                this.mCpuLevel = 0;
            }
            if (this.mCpuLevel >= 3) {
                z = false;
            }
            this.mCpuAllow = z;
            reportLevel = this.mCpuLevel;
            if (OPPODEBUG) {
                Slog.d(TAG, "cpu percent: " + this.cpuDf.format((double) percent) + ", count: " + this.mCpuNormalCount + "/" + this.mCpuSlightCount + "/" + this.mCpuMiddleCount + "/" + this.mCpuHeavyCount + ", level=" + this.mCpuLevel);
            }
        }
        maybeReportNewCpuState();
        if (shouldNotifyAbnormalCpuEvent()) {
            notifyHighCpuEventForApp(percent, reportLevel);
        }
    }

    private void stopCpuConstraint() {
        synchronized (this.mCpuObject) {
            this.mCpuHeavyCount = 0;
            this.mCpuMiddleCount = 0;
            this.mCpuSlightCount = 0;
            this.mCpuNormalCount = 0;
            this.mCpuLevel = 0;
            this.mCpuAllow = true;
        }
        maybeReportNewCpuState();
    }

    private boolean shouldNotifyAbnormalCpuEvent() {
        boolean notify = false;
        if (this.mCpuSlightCount > this.mConfig.getThreshCountContinuousSlight() || this.mCpuMiddleCount > this.mConfig.getThreshCountContinuousMiddle() || this.mCpuHeavyCount > this.mConfig.getThreshCountContinuousHeavy()) {
            notify = true;
        }
        if (notify && SystemClock.uptimeMillis() - this.mLastNotifyAppTime < this.mConfig.getIntervalAppCpuKill()) {
            notify = false;
        }
        if (!notify || this.mService == null || !this.mService.isRunningHighCpuJobs()) {
            return notify;
        }
        if (OPPODEBUG) {
            Slog.d(TAG, "skip notify for running cpu jobs");
        }
        return false;
    }

    private void notifyHighCpuEventForApp(float percent, int level) {
        if (OPPODEBUG) {
            Slog.d(TAG, "notifyHighCpuEventForApp  percent=" + percent);
        }
        this.mLastNotifyAppTime = SystemClock.uptimeMillis();
        Intent intent = new Intent(OPPO_CPU_MONITOR_NOTIFY);
        intent.putExtra("percent", (int) percent);
        intent.putExtra("level", level);
        this.mContext.sendBroadcast(intent);
    }

    private void initCpuTracking() {
        this.mCpuUtils = CpuUtils.getInstance();
        this.mCpuTraker = new CpuTraker(this, null);
        this.mCpuTraker.startTracking();
    }
}
