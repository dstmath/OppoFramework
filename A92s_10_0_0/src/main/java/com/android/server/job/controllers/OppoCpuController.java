package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.OppoJobSchedulerService;
import com.android.server.job.controllers.cpu.CpuUtils;
import com.color.util.ColorTypeCastingHelper;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OppoCpuController extends StateController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final int MSG_START_COLLECT = 1001;
    private static final int MSG_STOP_COLLECT = 1002;
    private static final int MSG_UPDATE_CPU = 1003;
    private static boolean OPPODEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String OPPO_CPU_MONITOR_NOTIFY = "oppo.intent.action.OPPO_CPU_MONITOR_NOTIFY";
    private static final String PROP_SAVED_CPU_LEVEL = "oppo.saved.cpu.level";
    private static final String PROP_SAVED_CPU_VALUE = "oppo.saved.cpu.value";
    private static final String TAG = "JobScheduler.Cpu";
    private static volatile OppoCpuController sController;
    private static final Object sCreationLock = new Object();
    DecimalFormat cpuDf = new DecimalFormat("######0.0");
    final HandlerThread cpuThread = new HandlerThread("CpuJob", 10);
    OppoGuardElfConfigUtil mConfig = OppoGuardElfConfigUtil.getInstance();
    /* access modifiers changed from: private */
    public boolean mCpuAllow = true;
    /* access modifiers changed from: private */
    public CpuTrackerHandler mCpuHandler = null;
    private int mCpuHeavyCount = 0;
    /* access modifiers changed from: private */
    public int mCpuLevel = 0;
    private int mCpuMiddleCount = 0;
    private int mCpuNormalCount = 0;
    /* access modifiers changed from: private */
    public final Object mCpuObject = new Object();
    private int mCpuSlightCount = 0;
    CpuTraker mCpuTraker;
    CpuUtils mCpuUtils;
    /* access modifiers changed from: private */
    public float mCurrentPercent;
    private long mLastNotifyAppTime;
    private OppoJobSchedulerService mService;
    /* access modifiers changed from: private */
    public List<JobStatus> mTrackedTasks = new ArrayList();

    public OppoCpuController(JobSchedulerService service) {
        super(service);
        this.cpuThread.start();
        this.mCpuHandler = new CpuTrackerHandler(this.cpuThread.getLooper());
        initCpuTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        OppoBaseJobStatus baseJobStatus = typeCasting(jobStatus);
        if (baseJobStatus != null && baseJobStatus.hasCpuConstraint()) {
            synchronized (this.mCpuObject) {
                synchronized (this.mTrackedTasks) {
                    if (OPPODEBUG) {
                        Slog.d(TAG, "maybeStartTrackingJob job " + jobStatus.getJob() + ", mCpuLevel=" + this.mCpuLevel);
                    }
                    this.mTrackedTasks.add(jobStatus);
                    baseJobStatus.setCpuConstraintSatisfied(this.mCpuLevel == 0 ? true : DEBUG);
                    baseJobStatus.cpuLevel.getAndSet(this.mCpuLevel);
                    baseJobStatus.oldCpuLevel.getAndSet(this.mCpuLevel);
                }
            }
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
        OppoBaseJobStatus baseJobStatus = typeCasting(jobStatus);
        if (baseJobStatus != null && baseJobStatus.hasCpuConstraint()) {
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
            /* class com.android.server.job.controllers.OppoCpuController.AnonymousClass1 */

            public void run() {
                boolean allow;
                boolean reportChange = OppoCpuController.DEBUG;
                synchronized (OppoCpuController.this.mCpuObject) {
                    if (OppoCpuController.DEBUG) {
                        Slog.d(OppoCpuController.TAG, "maybeReportNewCpuState:  " + OppoCpuController.this.mCpuLevel + ", allow=" + OppoCpuController.this.mCpuAllow);
                    }
                    synchronized (OppoCpuController.this.mTrackedTasks) {
                        for (JobStatus ts : OppoCpuController.this.mTrackedTasks) {
                            OppoBaseJobStatus baseJobStatus = OppoCpuController.typeCasting(ts);
                            if (baseJobStatus != null) {
                                int previousLevel = baseJobStatus.cpuLevel.getAndSet(OppoCpuController.this.mCpuLevel);
                                baseJobStatus.oldCpuLevel.getAndSet(previousLevel);
                                boolean previousAllow = baseJobStatus.isCpuConstraintSatisfied();
                                if (previousAllow) {
                                    allow = OppoCpuController.this.mCpuAllow;
                                } else {
                                    allow = OppoCpuController.this.mCpuLevel == 0 ? true : OppoCpuController.DEBUG;
                                }
                                baseJobStatus.setCpuConstraintSatisfied(allow);
                                if (!(previousLevel == OppoCpuController.this.mCpuLevel && previousAllow == allow)) {
                                    reportChange = true;
                                }
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

    /* access modifiers changed from: private */
    public class CpuTrackerHandler extends Handler {
        public CpuTrackerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 1001:
                    if (OppoCpuController.this.mConfig.getTotalCpuMonitorSwitch() && OppoCpuController.this.mCpuUtils != null) {
                        OppoCpuController.this.mCpuUtils.update();
                        sendEmptyMessageDelayed(1003, OppoCpuController.this.mConfig.getIntervalTotalCpuSample());
                        return;
                    }
                    return;
                case 1002:
                    removeMessages(1003);
                    OppoCpuController.this.stopCpuConstraint();
                    return;
                case 1003:
                    if (OppoCpuController.this.mCpuUtils != null) {
                        OppoCpuController.this.mCpuUtils.update();
                        OppoCpuController oppoCpuController = OppoCpuController.this;
                        float unused = oppoCpuController.mCurrentPercent = oppoCpuController.mCpuUtils.getSpecialPercent();
                        OppoCpuController oppoCpuController2 = OppoCpuController.this;
                        oppoCpuController2.handleCpuUpdate(oppoCpuController2.mCurrentPercent);
                        if (OppoCpuController.this.mConfig.getTotalCpuMonitorSwitch()) {
                            sendEmptyMessageDelayed(1003, OppoCpuController.this.mConfig.getIntervalTotalCpuSample());
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

    /* access modifiers changed from: private */
    public void handleCpuUpdate(float percent) {
        int reportLevel;
        int i = (percent > ((float) this.mConfig.getThreshTotalCpuSlight()) ? 1 : (percent == ((float) this.mConfig.getThreshTotalCpuSlight()) ? 0 : -1));
        boolean z = DEBUG;
        if (i < 0) {
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
            if (this.mCpuLevel < 3) {
                z = true;
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

    /* access modifiers changed from: private */
    public void stopCpuConstraint() {
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
        OppoJobSchedulerService oppoJobSchedulerService;
        boolean notify = DEBUG;
        if (this.mCpuSlightCount > this.mConfig.getThreshCountContinuousSlight() || this.mCpuMiddleCount > this.mConfig.getThreshCountContinuousMiddle() || this.mCpuHeavyCount > this.mConfig.getThreshCountContinuousHeavy()) {
            notify = true;
        }
        if (notify && SystemClock.uptimeMillis() - this.mLastNotifyAppTime < this.mConfig.getIntervalAppCpuKill()) {
            notify = DEBUG;
        }
        if (!notify || (oppoJobSchedulerService = this.mService) == null || !oppoJobSchedulerService.isRunningHighCpuJobs()) {
            return notify;
        }
        if (OPPODEBUG) {
            Slog.d(TAG, "skip notify for running cpu jobs");
        }
        return DEBUG;
    }

    private void notifyHighCpuEventForApp(float percent, int level) {
        if (OPPODEBUG) {
            Slog.d(TAG, "notifyHighCpuEventForApp  percent=" + percent);
        }
        this.mLastNotifyAppTime = SystemClock.uptimeMillis();
        Intent intent = new Intent(OPPO_CPU_MONITOR_NOTIFY);
        intent.putExtra("percent", (int) percent);
        intent.putExtra("level", level);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void initCpuTracking() {
        this.mCpuUtils = CpuUtils.getInstance();
        this.mCpuTraker = new CpuTraker();
        this.mCpuTraker.startTracking();
    }

    private class CpuTraker extends BroadcastReceiver {
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

    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
    }

    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
    }

    /* access modifiers changed from: private */
    public static OppoBaseJobStatus typeCasting(JobStatus js) {
        if (js != null) {
            return (OppoBaseJobStatus) ColorTypeCastingHelper.typeCasting(OppoBaseJobStatus.class, js);
        }
        return null;
    }
}
