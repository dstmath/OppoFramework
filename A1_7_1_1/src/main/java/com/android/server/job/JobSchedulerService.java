package com.android.server.job;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.IUidObserver;
import android.app.job.IJobScheduler.Stub;
import android.app.job.JobInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.ArrayUtils;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.OppoAlarmManagerHelper;
import com.android.server.OppoBPMHelper;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.SystemService;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.am.OppoProcessManagerHelper;
import com.android.server.am.OppoProtectEyeManagerService;
import com.android.server.am.OppoProtectEyeManagerService.ActivityChangedListener;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.job.JobStore.JobStatusFunctor;
import com.android.server.job.controllers.AppIdleController;
import com.android.server.job.controllers.BatteryController;
import com.android.server.job.controllers.BatteryIdleController;
import com.android.server.job.controllers.ConnectivityController;
import com.android.server.job.controllers.ContentObserverController;
import com.android.server.job.controllers.DeviceIdleJobsController;
import com.android.server.job.controllers.IdleController;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.OppoAppChangeController;
import com.android.server.job.controllers.StateController;
import com.android.server.job.controllers.TimeController;
import com.android.server.job.controllers.cpu.OppoCpuController;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.util.EmptyArray;
import oppo.util.OppoStatistics;

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
public final class JobSchedulerService extends SystemService implements StateChangedListener, JobCompletedListener {
    private static final boolean BATTIDLE_DCS_ENABLED = false;
    public static boolean DEBUG = false;
    private static final boolean ENFORCE_MAX_JOBS = true;
    private static final int MAX_JOBS_PER_APP = 100;
    private static final int MAX_JOB_CONTEXTS_COUNT = 16;
    static final int MSG_CHECK_JOB = 1;
    static final int MSG_CHECK_JOB_GREEDY = 3;
    static final int MSG_JOB_EXPIRED = 0;
    static final int MSG_STOP_JOB = 2;
    public static boolean OPPODEBUG = false;
    static final String TAG = "JobSchedulerService";
    static final List<String> THERMAL_TYPE = null;
    private static HashMap<String, String> mThermalPath;
    final List<JobServiceContext> mActiveServices;
    private ActivityChangedListener mActivityChangedListener;
    BatteryManagerInternal mBatteryManagerInternal;
    IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver;
    private AtomicBoolean mColorOsLowPowerModeEnabled;
    final Constants mConstants;
    List<StateController> mControllers;
    final JobHandler mHandler;
    final JobPackageTracker mJobPackageTracker;
    final JobSchedulerStub mJobSchedulerStub;
    private Handler mJobServiceHandler;
    final JobStore mJobs;
    ArrayList<JobServiceContext> mListBattIdleJobStart;
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    final Object mLock;
    int mMaxActiveJobs;
    final ArrayList<JobStatus> mPendingJobs;
    PowerManager mPowerManager;
    private PowerManagerInternal mPowerManagerInternal;
    boolean mReadyToRock;
    boolean mReportedActive;
    int[] mStartedUsers;
    boolean[] mTmpAssignAct;
    JobStatus[] mTmpAssignContextIdToJobMap;
    int[] mTmpAssignPreferredUidForContext;
    private volatile String mTopPkg;
    private final IUidObserver mUidObserver;
    final SparseIntArray mUidPriorityOverride;

    private final class Constants extends ContentObserver {
        private static final int DEFAULT_BG_CRITICAL_JOB_COUNT = 1;
        private static final int DEFAULT_BG_LOW_JOB_COUNT = 1;
        private static final int DEFAULT_BG_MODERATE_JOB_COUNT = 4;
        private static final int DEFAULT_BG_NORMAL_JOB_COUNT = 6;
        private static final int DEFAULT_FG_JOB_COUNT = 4;
        private static final float DEFAULT_HEAVY_USE_FACTOR = 0.9f;
        private static final int DEFAULT_MIN_CHARGING_COUNT = 1;
        private static final int DEFAULT_MIN_CONNECTIVITY_COUNT = 1;
        private static final int DEFAULT_MIN_CONTENT_COUNT = 1;
        private static final int DEFAULT_MIN_IDLE_COUNT = 1;
        private static final int DEFAULT_MIN_READY_JOBS_COUNT = 1;
        private static final float DEFAULT_MODERATE_USE_FACTOR = 0.5f;
        private static final String KEY_BG_CRITICAL_JOB_COUNT = "bg_critical_job_count";
        private static final String KEY_BG_LOW_JOB_COUNT = "bg_low_job_count";
        private static final String KEY_BG_MODERATE_JOB_COUNT = "bg_moderate_job_count";
        private static final String KEY_BG_NORMAL_JOB_COUNT = "bg_normal_job_count";
        private static final String KEY_FG_JOB_COUNT = "fg_job_count";
        private static final String KEY_HEAVY_USE_FACTOR = "heavy_use_factor";
        private static final String KEY_MIN_CHARGING_COUNT = "min_charging_count";
        private static final String KEY_MIN_CONNECTIVITY_COUNT = "min_connectivity_count";
        private static final String KEY_MIN_CONTENT_COUNT = "min_content_count";
        private static final String KEY_MIN_IDLE_COUNT = "min_idle_count";
        private static final String KEY_MIN_READY_JOBS_COUNT = "min_ready_jobs_count";
        private static final String KEY_MODERATE_USE_FACTOR = "moderate_use_factor";
        static final int MIN_CPU_COUNT = 1;
        static final int MIN_FORE_COUNT = 1;
        int BG_CRITICAL_JOB_COUNT = 1;
        int BG_LOW_JOB_COUNT = 1;
        int BG_MODERATE_JOB_COUNT = 4;
        int BG_NORMAL_JOB_COUNT = 6;
        int FG_JOB_COUNT = 4;
        float HEAVY_USE_FACTOR = DEFAULT_HEAVY_USE_FACTOR;
        int MIN_CHARGING_COUNT = 1;
        int MIN_CONNECTIVITY_COUNT = 1;
        int MIN_CONTENT_COUNT = 1;
        int MIN_IDLE_COUNT = 1;
        int MIN_READY_JOBS_COUNT = 1;
        float MODERATE_USE_FACTOR = 0.5f;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Global.getUriFor("job_scheduler_constants"), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (JobSchedulerService.this.mLock) {
                try {
                    this.mParser.setString(Global.getString(this.mResolver, "alarm_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(JobSchedulerService.TAG, "Bad device idle settings", e);
                }
                this.MIN_IDLE_COUNT = this.mParser.getInt(KEY_MIN_IDLE_COUNT, 1);
                this.MIN_CHARGING_COUNT = this.mParser.getInt(KEY_MIN_CHARGING_COUNT, 1);
                this.MIN_CONNECTIVITY_COUNT = this.mParser.getInt(KEY_MIN_CONNECTIVITY_COUNT, 1);
                this.MIN_CONTENT_COUNT = this.mParser.getInt(KEY_MIN_CONTENT_COUNT, 1);
                this.MIN_READY_JOBS_COUNT = this.mParser.getInt(KEY_MIN_READY_JOBS_COUNT, 1);
                this.HEAVY_USE_FACTOR = this.mParser.getFloat(KEY_HEAVY_USE_FACTOR, DEFAULT_HEAVY_USE_FACTOR);
                this.MODERATE_USE_FACTOR = this.mParser.getFloat(KEY_MODERATE_USE_FACTOR, 0.5f);
                this.FG_JOB_COUNT = this.mParser.getInt(KEY_FG_JOB_COUNT, 4);
                this.BG_NORMAL_JOB_COUNT = this.mParser.getInt(KEY_BG_NORMAL_JOB_COUNT, 6);
                if (this.FG_JOB_COUNT + this.BG_NORMAL_JOB_COUNT > 16) {
                    this.BG_NORMAL_JOB_COUNT = 16 - this.FG_JOB_COUNT;
                }
                this.BG_MODERATE_JOB_COUNT = this.mParser.getInt(KEY_BG_MODERATE_JOB_COUNT, 4);
                if (this.FG_JOB_COUNT + this.BG_MODERATE_JOB_COUNT > 16) {
                    this.BG_MODERATE_JOB_COUNT = 16 - this.FG_JOB_COUNT;
                }
                this.BG_LOW_JOB_COUNT = this.mParser.getInt(KEY_BG_LOW_JOB_COUNT, 1);
                if (this.FG_JOB_COUNT + this.BG_LOW_JOB_COUNT > 16) {
                    this.BG_LOW_JOB_COUNT = 16 - this.FG_JOB_COUNT;
                }
                this.BG_CRITICAL_JOB_COUNT = this.mParser.getInt(KEY_BG_CRITICAL_JOB_COUNT, 1);
                if (this.FG_JOB_COUNT + this.BG_CRITICAL_JOB_COUNT > 16) {
                    this.BG_CRITICAL_JOB_COUNT = 16 - this.FG_JOB_COUNT;
                }
            }
            return;
        }

        void dump(PrintWriter pw) {
            pw.println("  Settings:");
            pw.print("    ");
            pw.print(KEY_MIN_IDLE_COUNT);
            pw.print("=");
            pw.print(this.MIN_IDLE_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_CHARGING_COUNT);
            pw.print("=");
            pw.print(this.MIN_CHARGING_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_CONNECTIVITY_COUNT);
            pw.print("=");
            pw.print(this.MIN_CONNECTIVITY_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_CONTENT_COUNT);
            pw.print("=");
            pw.print(this.MIN_CONTENT_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_READY_JOBS_COUNT);
            pw.print("=");
            pw.print(this.MIN_READY_JOBS_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_HEAVY_USE_FACTOR);
            pw.print("=");
            pw.print(this.HEAVY_USE_FACTOR);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MODERATE_USE_FACTOR);
            pw.print("=");
            pw.print(this.MODERATE_USE_FACTOR);
            pw.println();
            pw.print("    ");
            pw.print(KEY_FG_JOB_COUNT);
            pw.print("=");
            pw.print(this.FG_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_NORMAL_JOB_COUNT);
            pw.print("=");
            pw.print(this.BG_NORMAL_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_MODERATE_JOB_COUNT);
            pw.print("=");
            pw.print(this.BG_MODERATE_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_LOW_JOB_COUNT);
            pw.print("=");
            pw.print(this.BG_LOW_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_CRITICAL_JOB_COUNT);
            pw.print("=");
            pw.print(this.BG_CRITICAL_JOB_COUNT);
            pw.println();
        }
    }

    private class JobHandler extends Handler {
        private final MaybeReadyJobQueueFunctor mMaybeQueueFunctor = new MaybeReadyJobQueueFunctor();
        private final ReadyJobQueueFunctor mReadyQueueFunctor = new ReadyJobQueueFunctor();

        class MaybeReadyJobQueueFunctor implements JobStatusFunctor {
            int backoffCount;
            int chargingCount;
            int connectivityCount;
            int contentCount;
            int cpuCount = 0;
            int foreCount = 0;
            int idleCount;
            List<JobStatus> runnableJobs;

            public MaybeReadyJobQueueFunctor() {
                reset();
            }

            public void process(JobStatus job) {
                if (JobHandler.this.isReadyToBeExecutedLocked(job)) {
                    try {
                        if (ActivityManagerNative.getDefault().getAppStartMode(job.getUid(), job.getJob().getService().getPackageName()) == 2) {
                            Slog.w(JobSchedulerService.TAG, "Aborting job " + job.getUid() + ":" + job.getJob().toString() + " -- package not allowed to start");
                            JobSchedulerService.this.mHandler.obtainMessage(2, job).sendToTarget();
                            return;
                        }
                    } catch (RemoteException e) {
                    }
                    if (job.getNumFailures() > 0) {
                        this.backoffCount++;
                    }
                    if (job.hasIdleConstraint()) {
                        this.idleCount++;
                    }
                    if (job.hasConnectivityConstraint() || job.hasUnmeteredConstraint() || job.hasNotRoamingConstraint()) {
                        this.connectivityCount++;
                    }
                    if (job.hasChargingConstraint()) {
                        this.chargingCount++;
                    }
                    if (job.hasContentTriggerConstraint()) {
                        this.contentCount++;
                    }
                    if (job.hasCpuConstraint()) {
                        this.cpuCount++;
                    }
                    if (job.hasProtectForeConstraint()) {
                        this.foreCount++;
                    }
                    if (this.runnableJobs == null) {
                        this.runnableJobs = new ArrayList();
                    }
                    this.runnableJobs.add(job);
                } else if (JobHandler.this.areJobConstraintsNotSatisfiedLocked(job)) {
                    JobSchedulerService.this.uploadJobStopEvent(job);
                    JobSchedulerService.this.stopJobOnServiceContextLocked(job, 1);
                } else if (JobHandler.this.isReadyToUpdateCpuParameter(job)) {
                    JobSchedulerService.this.updateParamterOnServiceContextLocked(job);
                }
            }

            public void postProcess() {
                if (this.backoffCount > 0 || this.idleCount >= JobSchedulerService.this.mConstants.MIN_IDLE_COUNT || this.connectivityCount >= JobSchedulerService.this.mConstants.MIN_CONNECTIVITY_COUNT || this.chargingCount >= JobSchedulerService.this.mConstants.MIN_CHARGING_COUNT || this.contentCount >= JobSchedulerService.this.mConstants.MIN_CONTENT_COUNT || this.foreCount >= 1 || this.cpuCount >= 1 || (this.runnableJobs != null && this.runnableJobs.size() >= JobSchedulerService.this.mConstants.MIN_READY_JOBS_COUNT)) {
                    if (JobSchedulerService.DEBUG) {
                        Slog.d(JobSchedulerService.TAG, "maybeQueueReadyJobsForExecutionLockedH: Running jobs.");
                    }
                    if (this.runnableJobs != null) {
                        JobSchedulerService.this.noteJobsPending(this.runnableJobs);
                        JobSchedulerService.this.mPendingJobs.addAll(this.runnableJobs);
                    }
                } else if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "maybeQueueReadyJobsForExecutionLockedH: Not running anything.");
                }
                reset();
            }

            private void reset() {
                this.chargingCount = 0;
                this.idleCount = 0;
                this.backoffCount = 0;
                this.connectivityCount = 0;
                this.contentCount = 0;
                this.foreCount = 0;
                this.cpuCount = 0;
                this.runnableJobs = null;
            }
        }

        class ReadyJobQueueFunctor implements JobStatusFunctor {
            ArrayList<JobStatus> newReadyJobs;

            ReadyJobQueueFunctor() {
            }

            public void process(JobStatus job) {
                if (JobHandler.this.isReadyToBeExecutedLocked(job)) {
                    if (JobSchedulerService.DEBUG) {
                        Slog.d(JobSchedulerService.TAG, "    queued " + job.toShortString());
                    }
                    if (this.newReadyJobs == null) {
                        this.newReadyJobs = new ArrayList();
                    }
                    this.newReadyJobs.add(job);
                } else if (JobHandler.this.areJobConstraintsNotSatisfiedLocked(job)) {
                    JobSchedulerService.this.uploadJobStopEvent(job);
                    JobSchedulerService.this.stopJobOnServiceContextLocked(job, 1);
                } else if (JobHandler.this.isReadyToUpdateCpuParameter(job)) {
                    JobSchedulerService.this.updateParamterOnServiceContextLocked(job);
                }
            }

            public void postProcess() {
                if (this.newReadyJobs != null) {
                    JobSchedulerService.this.noteJobsPending(this.newReadyJobs);
                    JobSchedulerService.this.mPendingJobs.addAll(this.newReadyJobs);
                }
                this.newReadyJobs = null;
            }
        }

        public JobHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:9:0x0011, code:
            switch(r5.what) {
                case 0: goto L_0x001f;
                case 1: goto L_0x0055;
                case 2: goto L_0x0078;
                case 3: goto L_0x006b;
                default: goto L_0x0014;
            };
     */
        /* JADX WARNING: Missing block: B:10:0x0014, code:
            maybeRunPendingJobsH();
            removeMessages(1);
     */
        /* JADX WARNING: Missing block: B:11:0x001b, code:
            return;
     */
        /* JADX WARNING: Missing block: B:15:0x001f, code:
            r2 = r4.this$0.mLock;
     */
        /* JADX WARNING: Missing block: B:16:0x0023, code:
            monitor-enter(r2);
     */
        /* JADX WARNING: Missing block: B:18:?, code:
            r0 = r5.obj;
     */
        /* JADX WARNING: Missing block: B:19:0x0028, code:
            if (r0 == null) goto L_0x0034;
     */
        /* JADX WARNING: Missing block: B:21:0x0032, code:
            if (r4.this$0.mPendingJobs.contains(r0) == false) goto L_0x0039;
     */
        /* JADX WARNING: Missing block: B:22:0x0034, code:
            queueReadyJobsForExecutionLockedH();
     */
        /* JADX WARNING: Missing block: B:23:0x0037, code:
            monitor-exit(r2);
     */
        /* JADX WARNING: Missing block: B:26:0x0041, code:
            if (r4.this$0.mJobs.containsJob(r0) == false) goto L_0x0034;
     */
        /* JADX WARNING: Missing block: B:27:0x0043, code:
            r4.this$0.mJobPackageTracker.notePending(r0);
            r4.this$0.mPendingJobs.add(r0);
     */
        /* JADX WARNING: Missing block: B:31:0x0055, code:
            r2 = r4.this$0.mLock;
     */
        /* JADX WARNING: Missing block: B:32:0x0059, code:
            monitor-enter(r2);
     */
        /* JADX WARNING: Missing block: B:35:0x005e, code:
            if (r4.this$0.mReportedActive == false) goto L_0x0067;
     */
        /* JADX WARNING: Missing block: B:36:0x0060, code:
            queueReadyJobsForExecutionLockedH();
     */
        /* JADX WARNING: Missing block: B:41:?, code:
            maybeQueueReadyJobsForExecutionLockedH();
     */
        /* JADX WARNING: Missing block: B:42:0x006b, code:
            r1 = r4.this$0.mLock;
     */
        /* JADX WARNING: Missing block: B:43:0x006f, code:
            monitor-enter(r1);
     */
        /* JADX WARNING: Missing block: B:45:?, code:
            queueReadyJobsForExecutionLockedH();
     */
        /* JADX WARNING: Missing block: B:46:0x0073, code:
            monitor-exit(r1);
     */
        /* JADX WARNING: Missing block: B:50:0x0078, code:
            com.android.server.job.JobSchedulerService.-wrap6(r4.this$0, (com.android.server.job.controllers.JobStatus) r5.obj, null);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message message) {
            synchronized (JobSchedulerService.this.mLock) {
                if (!JobSchedulerService.this.mReadyToRock) {
                }
            }
        }

        private void queueReadyJobsForExecutionLockedH() {
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "queuing all ready jobs for execution:");
            }
            JobSchedulerService.this.noteJobsNonpending(JobSchedulerService.this.mPendingJobs);
            JobSchedulerService.this.mPendingJobs.clear();
            JobSchedulerService.this.mJobs.forEachJob(this.mReadyQueueFunctor);
            this.mReadyQueueFunctor.postProcess();
            if (JobSchedulerService.DEBUG) {
                int queuedJobs = JobSchedulerService.this.mPendingJobs.size();
                if (queuedJobs == 0) {
                    Slog.d(JobSchedulerService.TAG, "No jobs pending.");
                } else {
                    Slog.d(JobSchedulerService.TAG, queuedJobs + " jobs queued.");
                }
            }
        }

        private void maybeQueueReadyJobsForExecutionLockedH() {
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Maybe queuing ready jobs...");
            }
            JobSchedulerService.this.noteJobsNonpending(JobSchedulerService.this.mPendingJobs);
            JobSchedulerService.this.mPendingJobs.clear();
            JobSchedulerService.this.mJobs.forEachJob(this.mMaybeQueueFunctor);
            this.mMaybeQueueFunctor.postProcess();
        }

        private boolean isReadyToBeExecutedLocked(JobStatus job) {
            boolean jobReady = job.isReady();
            boolean jobPending = JobSchedulerService.this.mPendingJobs.contains(job);
            boolean jobActive = JobSchedulerService.this.isCurrentlyActiveLocked(job);
            int userId = job.getUserId();
            boolean userStarted = ArrayUtils.contains(JobSchedulerService.this.mStartedUsers, userId);
            try {
                boolean componentPresent = AppGlobals.getPackageManager().getServiceInfo(job.getServiceComponent(), 268435456, userId) != null;
                if (JobSchedulerService.DEBUG) {
                    Slog.v(JobSchedulerService.TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " ready=" + jobReady + " pending=" + jobPending + " active=" + jobActive + " userStarted=" + userStarted + " componentPresent=" + componentPresent);
                }
                boolean satisfied = userStarted && componentPresent && jobReady && !jobPending && !jobActive;
                String pkgName = null;
                if (!(job.getJob() == null || job.getJob().getService() == null)) {
                    pkgName = job.getJob().getService().getPackageName();
                }
                if (pkgName == null) {
                    return satisfied;
                }
                if (satisfied && OppoProcessManagerHelper.isInStrictMode() && OppoProcessManagerHelper.isDelayAppJob(job.getUid(), pkgName)) {
                    satisfied = false;
                    if (JobSchedulerService.OPPODEBUG) {
                        Slog.d(JobSchedulerService.TAG, "in strict mode. job: " + job);
                    }
                }
                if (satisfied && JobSchedulerService.this.mColorOsLowPowerModeEnabled.get() && !job.getJob().isSystemApp() && !pkgName.equals(JobSchedulerService.this.mTopPkg)) {
                    satisfied = false;
                    if (JobSchedulerService.OPPODEBUG) {
                        Slog.d(JobSchedulerService.TAG, "in coloros power save mode. job: " + job);
                    }
                }
                return satisfied;
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        }

        private boolean areJobConstraintsNotSatisfiedLocked(JobStatus job) {
            return !job.isReady() ? JobSchedulerService.this.isCurrentlyActiveLocked(job) : false;
        }

        private boolean isReadyToUpdateCpuParameter(JobStatus job) {
            if (job.hasCpuConstraint() && JobSchedulerService.this.isCurrentlyActiveLocked(job) && job.oldCpuLevel.get() != job.cpuLevel.get()) {
                return true;
            }
            return false;
        }

        private void maybeRunPendingJobsH() {
            JobSchedulerService.this.mListBattIdleJobStart.clear();
            synchronized (JobSchedulerService.this.mLock) {
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "pending queue: " + JobSchedulerService.this.mPendingJobs.size() + " jobs.");
                }
                JobSchedulerService.this.assignJobsToContextsLocked();
                JobSchedulerService.this.reportActive();
            }
            JobSchedulerService.this.rcdBattIdleStart();
        }
    }

    final class JobSchedulerStub extends Stub {
        private final SparseArray<Boolean> mPersistCache = new SparseArray();

        JobSchedulerStub() {
        }

        private void enforceValidJobRequest(int uid, JobInfo job) {
            IPackageManager pm = AppGlobals.getPackageManager();
            ComponentName service = job.getService();
            try {
                ServiceInfo si = pm.getServiceInfo(service, 786432, UserHandle.getUserId(uid));
                if (si == null) {
                    throw new IllegalArgumentException("No such service " + service);
                } else if (si.applicationInfo.uid != uid) {
                    throw new IllegalArgumentException("uid " + uid + " cannot schedule job in " + service.getPackageName());
                } else if (!"android.permission.BIND_JOB_SERVICE".equals(si.permission)) {
                    throw new IllegalArgumentException("Scheduled service " + service + " does not require android.permission.BIND_JOB_SERVICE permission");
                }
            } catch (RemoteException e) {
            }
        }

        private boolean canPersistJobs(int pid, int uid) {
            boolean canPersist;
            synchronized (this.mPersistCache) {
                Boolean cached = (Boolean) this.mPersistCache.get(uid);
                if (cached != null) {
                    canPersist = cached.booleanValue();
                } else {
                    canPersist = JobSchedulerService.this.getContext().checkPermission("android.permission.RECEIVE_BOOT_COMPLETED", pid, uid) == 0;
                    this.mPersistCache.put(uid, Boolean.valueOf(canPersist));
                }
            }
            return canPersist;
        }

        public int schedule(JobInfo job) throws RemoteException {
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Scheduling job: " + job.toString());
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            enforceValidJobRequest(uid, job);
            if (!job.isPersisted() || canPersistJobs(pid, uid)) {
                if ((job.getFlags() & 1) != 0) {
                    JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", JobSchedulerService.TAG);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    int schedule = JobSchedulerService.this.schedule(job, uid);
                    return schedule;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("Error: requested job be persisted without holding RECEIVE_BOOT_COMPLETED permission.");
            }
        }

        public int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag) throws RemoteException {
            int callerUid = Binder.getCallingUid();
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Caller uid " + callerUid + " scheduling job: " + job.toString() + " on behalf of " + packageName);
            }
            if (packageName == null) {
                throw new NullPointerException("Must specify a package for scheduleAsPackage()");
            } else if (JobSchedulerService.this.getContext().checkCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS") != 0) {
                throw new SecurityException("Caller uid " + callerUid + " not permitted to schedule jobs for other apps");
            } else {
                if ((job.getFlags() & 1) != 0) {
                    JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", JobSchedulerService.TAG);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    int scheduleAsPackage = JobSchedulerService.this.scheduleAsPackage(job, callerUid, packageName, userId, tag);
                    return scheduleAsPackage;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public List<JobInfo> getAllPendingJobs() throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                List<JobInfo> pendingJobs = JobSchedulerService.this.getPendingJobs(uid);
                return pendingJobs;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public JobInfo getPendingJob(int jobId) throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                JobInfo pendingJob = JobSchedulerService.this.getPendingJob(uid, jobId);
                return pendingJob;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void cancelAll() throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJobsForUid(uid, true);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void cancel(int jobId) throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJob(uid, jobId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.DUMP", JobSchedulerService.TAG);
            long identityToken = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.dumpInternal(pw, args);
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
            new JobSchedulerShellCommand(JobSchedulerService.this).exec(this, in, out, err, args, resultReceiver);
        }
    }

    final class LocalService implements JobSchedulerInternal {
        LocalService() {
        }

        public List<JobInfo> getSystemScheduledPendingJobs() {
            final List<JobInfo> pendingJobs;
            synchronized (JobSchedulerService.this.mLock) {
                pendingJobs = new ArrayList();
                JobSchedulerService.this.mJobs.forEachJob(1000, new JobStatusFunctor() {
                    public void process(JobStatus job) {
                        if (job.getJob().isPeriodic() || !JobSchedulerService.this.isCurrentlyActiveLocked(job)) {
                            pendingJobs.add(job.getJob());
                        }
                    }
                });
            }
            return pendingJobs;
        }

        public void cancelJobsForUid(int uid) {
            if (JobSchedulerService.DEBUG) {
                Slog.i(JobSchedulerService.TAG, "cancelJobsForUid by SYSTEM! uid = " + uid);
            }
            JobSchedulerService.this.cancelJobsForUid(uid, true);
        }
    }

    private class RemovePendingJobRunnable implements Runnable {
        private JobStatus mPendingJob;

        public RemovePendingJobRunnable(JobStatus pendingJob) {
            this.mPendingJob = pendingJob;
        }

        public void run() {
            try {
                if (this.mPendingJob != null && this.mPendingJob.getServiceComponent() != null && OppoAppStartupManager.getInstance().isRemovePendingJob(this.mPendingJob.getServiceComponent().getPackageName())) {
                    JobSchedulerService.this.stopTrackingJob(this.mPendingJob, null, false);
                }
            } catch (Exception e) {
                Slog.w(JobSchedulerService.TAG, "removePendingJob failed");
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.job.JobSchedulerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.job.JobSchedulerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.JobSchedulerService.<clinit>():void");
    }

    private String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            return uri.getSchemeSpecificPart();
        }
        return null;
    }

    public Object getLock() {
        return this.mLock;
    }

    public JobStore getJobStore() {
        return this.mJobs;
    }

    public void onStartUser(int userHandle) {
        this.mStartedUsers = ArrayUtils.appendInt(this.mStartedUsers, userHandle);
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public void onUnlockUser(int userHandle) {
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public void onStopUser(int userHandle) {
        this.mStartedUsers = ArrayUtils.removeInt(this.mStartedUsers, userHandle);
    }

    /* JADX WARNING: Missing block: B:7:0x0017, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean minIntervalConstraint(JobInfo job, int uId) {
        if (uId < 10000) {
            return true;
        }
        PackageManager packageManager = getContext().getPackageManager();
        if (packageManager == null || packageManager.isFullFunctionMode() || job.getService() == null) {
            return true;
        }
        String jobPkg = job.getService().getPackageName();
        if (jobPkg == null) {
            return true;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(jobPkg, DumpState.DUMP_PREFERRED_XML);
        } catch (NameNotFoundException e) {
            Slog.d(TAG, "getApplicationInfo NameNotFoundException. pkg = " + jobPkg);
        }
        if (appInfo == null || (appInfo.flags & 1) != 0) {
            return true;
        }
        long minInterval = OppoGuardElfConfigUtil.getInstance().getThreshJobMinInterval() * 1000;
        if (job.isPeriodic()) {
            if (job.getIntervalMillis() < minInterval) {
                job.setIntervalMillis(minInterval);
            }
        } else if (job.hasLateConstraint() && job.getMaxExecutionDelayMillis() < minInterval) {
            job.setMaxExecutionDelayMillis(minInterval);
        }
        return false;
    }

    public int schedule(JobInfo job, int uId) {
        job.setSysApp(minIntervalConstraint(job, uId));
        return scheduleAsPackage(job, uId, null, -1, null);
    }

    public int scheduleAsPackage(JobInfo job, int uId, String packageName, int userId, String tag) {
        JobStatus jobStatus = JobStatus.createFromJobInfo(job, uId, packageName, userId, tag);
        try {
            if (ActivityManagerNative.getDefault().getAppStartMode(uId, job.getService().getPackageName()) == 2) {
                Slog.w(TAG, "Not scheduling job " + uId + ":" + job.toString() + " -- package not allowed to start");
                return 0;
            }
        } catch (RemoteException e) {
        }
        if (DEBUG) {
            Slog.d(TAG, "SCHEDULE: " + jobStatus.toShortString());
        }
        synchronized (this.mLock) {
            if (packageName == null) {
                if (this.mJobs.countJobsForUid(uId) > 100) {
                    Slog.w(TAG, "Too many jobs for uid " + uId);
                    throw new IllegalStateException("Apps may not schedule more than 100 distinct jobs");
                }
            }
            JobStatus toCancel = this.mJobs.getJobByUidAndJobId(uId, job.getId());
            if (toCancel != null) {
                Slog.d(TAG, "new job pkgname = " + jobStatus.getSourcePackageName());
                Slog.d(TAG, "old job pkgname = " + toCancel.getSourcePackageName());
                if (jobStatus.getSourcePackageName().equals(toCancel.getSourcePackageName())) {
                    cancelJobImpl(toCancel, jobStatus);
                }
            }
            startTrackingJob(jobStatus, toCancel);
            uploadJobScheduleEvent(jobStatus);
            uploadScheduleBattIdleJob(jobStatus);
        }
        this.mHandler.obtainMessage(1).sendToTarget();
        return 1;
    }

    public List<JobInfo> getPendingJobs(int uid) {
        ArrayList<JobInfo> outList;
        synchronized (this.mLock) {
            List<JobStatus> jobs = this.mJobs.getJobsByUid(uid);
            outList = new ArrayList(jobs.size());
            for (int i = jobs.size() - 1; i >= 0; i--) {
                outList.add(((JobStatus) jobs.get(i)).getJob());
            }
        }
        return outList;
    }

    private String getJobType(JobInfo job) {
        String type = IElsaManager.EMPTY_PACKAGE;
        if (job.isPeriodic()) {
            type = type + "Periodic";
        }
        if (job.isRequireCharging()) {
            type = type + "Charging";
        }
        if (job.isRequireDeviceIdle()) {
            type = type + "DeviceIdle";
        }
        if (job.getNetworkType() != 0) {
            type = type + "NetWork";
        }
        if (job.isPersisted()) {
            return type + "Persist";
        }
        return type;
    }

    private boolean isCtsRunning(Context context) {
        boolean isCtsRunning = false;
        if (context == null) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            isCtsRunning = packageManager.isFullFunctionMode();
        }
        return isCtsRunning;
    }

    private boolean isSystemApp(Context context, String pkg) {
        if (pkg == null || context == null) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(pkg, 128);
            if (info == null || (info.flags & 1) == 0) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            Slog.i(TAG, IElsaManager.EMPTY_PACKAGE + e);
            return false;
        }
    }

    public JobInfo getPendingJob(int uid, int jobId) {
        synchronized (this.mLock) {
            List<JobStatus> jobs = this.mJobs.getJobsByUid(uid);
            for (int i = jobs.size() - 1; i >= 0; i--) {
                JobStatus job = (JobStatus) jobs.get(i);
                if (job.getJobId() == jobId) {
                    JobInfo job2 = job.getJob();
                    return job2;
                }
            }
            return null;
        }
    }

    void cancelJobsForUser(int userHandle) {
        List<JobStatus> jobsForUser;
        synchronized (this.mLock) {
            jobsForUser = this.mJobs.getJobsByUser(userHandle);
        }
        for (int i = 0; i < jobsForUser.size(); i++) {
            cancelJobImpl((JobStatus) jobsForUser.get(i), null);
        }
    }

    void cancelJobsForPackageAndUid(String pkgName, int uid) {
        List<JobStatus> jobsForUid;
        synchronized (this.mLock) {
            jobsForUid = this.mJobs.getJobsByUid(uid);
        }
        for (int i = jobsForUid.size() - 1; i >= 0; i--) {
            JobStatus job = (JobStatus) jobsForUid.get(i);
            if (job.getSourcePackageName().equals(pkgName)) {
                cancelJobImpl(job, null);
            }
        }
    }

    public void cancelJobsForUid(int uid, boolean forceAll) {
        List<JobStatus> jobsForUid;
        synchronized (this.mLock) {
            jobsForUid = this.mJobs.getJobsByUid(uid);
        }
        for (int i = 0; i < jobsForUid.size(); i++) {
            JobStatus toRemove = (JobStatus) jobsForUid.get(i);
            if (!forceAll) {
                try {
                    if (ActivityManagerNative.getDefault().getAppStartMode(uid, toRemove.getServiceComponent().getPackageName()) != 2) {
                    }
                } catch (RemoteException e) {
                }
            }
            cancelJobImpl(toRemove, null);
        }
    }

    public void cancelJob(int uid, int jobId) {
        JobStatus toCancel;
        synchronized (this.mLock) {
            toCancel = this.mJobs.getJobByUidAndJobId(uid, jobId);
        }
        if (toCancel != null) {
            cancelJobImpl(toCancel, null);
        }
    }

    private void cancelJobImpl(JobStatus cancelled, JobStatus incomingJob) {
        if (DEBUG) {
            Slog.d(TAG, "CANCEL: " + cancelled.toShortString());
        }
        stopTrackingJob(cancelled, incomingJob, true);
        synchronized (this.mLock) {
            if (this.mPendingJobs.remove(cancelled)) {
                this.mJobPackageTracker.noteNonpending(cancelled);
            }
            stopJobOnServiceContextLocked(cancelled, 0);
            reportActive();
        }
    }

    void updateUidState(int uid, int procState) {
        synchronized (this.mLock) {
            if (procState == 2) {
                this.mUidPriorityOverride.put(uid, 40);
            } else if (procState <= 4) {
                this.mUidPriorityOverride.put(uid, 30);
            } else {
                this.mUidPriorityOverride.delete(uid);
            }
        }
    }

    public void onDeviceIdleStateChanged(boolean deviceIdle) {
        synchronized (this.mLock) {
            if (deviceIdle) {
                for (int i = 0; i < this.mActiveServices.size(); i++) {
                    JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                    JobStatus executing = jsc.getRunningJob();
                    if (executing != null && (executing.getFlags() & 1) == 0) {
                        jsc.cancelExecutingJob(4);
                    }
                }
            } else {
                if (!(!this.mReadyToRock || this.mLocalDeviceIdleController == null || this.mReportedActive)) {
                    this.mReportedActive = true;
                    this.mLocalDeviceIdleController.setJobsActive(true);
                }
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    void reportActive() {
        boolean active = this.mPendingJobs.size() > 0;
        if (this.mPendingJobs.size() <= 0) {
            for (int i = 0; i < this.mActiveServices.size(); i++) {
                JobStatus job = ((JobServiceContext) this.mActiveServices.get(i)).getRunningJob();
                if (job != null && (job.getJob().getFlags() & 1) == 0 && !job.dozeWhitelisted) {
                    active = true;
                    break;
                }
            }
        }
        if (this.mReportedActive != active) {
            this.mReportedActive = active;
            if (this.mLocalDeviceIdleController != null) {
                this.mLocalDeviceIdleController.setJobsActive(active);
            }
        }
    }

    public JobSchedulerService(Context context) {
        super(context);
        this.mJobServiceHandler = null;
        this.mLock = new Object();
        this.mJobPackageTracker = new JobPackageTracker();
        this.mActiveServices = new ArrayList();
        this.mPendingJobs = new ArrayList();
        this.mStartedUsers = EmptyArray.INT;
        this.mMaxActiveJobs = 1;
        this.mUidPriorityOverride = new SparseIntArray();
        this.mTmpAssignContextIdToJobMap = new JobStatus[16];
        this.mTmpAssignAct = new boolean[16];
        this.mTmpAssignPreferredUidForContext = new int[16];
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Removed duplicated region for block: B:77:0x0247 A:{Splitter: B:18:0x0079, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
            /* JADX WARNING: Missing block: B:95:?, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "Receieved: " + action);
                }
                String pkgName;
                int pkgUid;
                int userId;
                if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                    pkgName = JobSchedulerService.this.getPackageName(intent);
                    pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
                    if (pkgName == null || pkgUid == -1) {
                        Slog.w(JobSchedulerService.TAG, "PACKAGE_CHANGED for " + pkgName + " / uid " + pkgUid);
                        return;
                    }
                    String[] changedComponents = intent.getStringArrayExtra("android.intent.extra.changed_component_name_list");
                    if (changedComponents != null) {
                        int i = 0;
                        int length = changedComponents.length;
                        while (i < length) {
                            if (changedComponents[i].equals(pkgName)) {
                                if (JobSchedulerService.DEBUG) {
                                    Slog.d(JobSchedulerService.TAG, "Package state change: " + pkgName);
                                }
                                try {
                                    userId = UserHandle.getUserId(pkgUid);
                                    int state = AppGlobals.getPackageManager().getApplicationEnabledSetting(pkgName, userId);
                                    if (state == 2 || state == 3) {
                                        if (JobSchedulerService.DEBUG) {
                                            Slog.d(JobSchedulerService.TAG, "Removing jobs for package " + pkgName + " in user " + userId);
                                        }
                                        JobSchedulerService.this.cancelJobsForUid(pkgUid, true);
                                        return;
                                    }
                                    return;
                                } catch (RemoteException e) {
                                }
                            } else {
                                i++;
                            }
                        }
                    }
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                        int uidRemoved = intent.getIntExtra("android.intent.extra.UID", -1);
                        if (JobSchedulerService.DEBUG) {
                            Slog.d(JobSchedulerService.TAG, "Removing jobs for uid: " + uidRemoved);
                        }
                        JobSchedulerService.this.cancelJobsForUid(uidRemoved, true);
                    }
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    userId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    if (JobSchedulerService.DEBUG) {
                        Slog.d(JobSchedulerService.TAG, "Removing jobs for user: " + userId);
                    }
                    JobSchedulerService.this.cancelJobsForUser(userId);
                } else if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
                    pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
                    pkgName = intent.getData().getSchemeSpecificPart();
                    if (pkgUid != -1) {
                        List<JobStatus> jobsForUid;
                        synchronized (JobSchedulerService.this.mLock) {
                            jobsForUid = JobSchedulerService.this.mJobs.getJobsByUid(pkgUid);
                        }
                        for (int i2 = jobsForUid.size() - 1; i2 >= 0; i2--) {
                            if (((JobStatus) jobsForUid.get(i2)).getSourcePackageName().equals(pkgName)) {
                                if (JobSchedulerService.DEBUG) {
                                    Slog.d(JobSchedulerService.TAG, "Restart query: package " + pkgName + " at uid " + pkgUid + " has jobs");
                                }
                                setResultCode(-1);
                                return;
                            }
                        }
                    }
                } else if ("android.intent.action.PACKAGE_RESTARTED".equals(action)) {
                    pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
                    pkgName = intent.getData().getSchemeSpecificPart();
                    if (pkgUid != -1) {
                        if (JobSchedulerService.DEBUG) {
                            Slog.d(JobSchedulerService.TAG, "Removing jobs for pkg " + pkgName + " at uid " + pkgUid);
                        }
                        if (!OppoAlarmManagerHelper.isFilterRemovePackage(pkgName)) {
                            JobSchedulerService.this.cancelJobsForPackageAndUid(pkgName, pkgUid);
                        }
                    }
                }
            }
        };
        this.mUidObserver = new IUidObserver.Stub() {
            public void onUidStateChanged(int uid, int procState) throws RemoteException {
                JobSchedulerService.this.updateUidState(uid, procState);
            }

            public void onUidGone(int uid) throws RemoteException {
                JobSchedulerService.this.updateUidState(uid, 16);
            }

            public void onUidActive(int uid) throws RemoteException {
            }

            public void onUidIdle(int uid) throws RemoteException {
                JobSchedulerService.this.cancelJobsForUid(uid, false);
            }
        };
        this.mListBattIdleJobStart = new ArrayList();
        this.mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
        this.mTopPkg = IElsaManager.EMPTY_PACKAGE;
        this.mActivityChangedListener = new ActivityChangedListener() {
            public void onActivityChanged(String prePkg, String nextPkg) {
                if (nextPkg != null && !nextPkg.equals(JobSchedulerService.this.mTopPkg) && !"com.coloros.recents".equals(nextPkg)) {
                    JobSchedulerService.this.mTopPkg = nextPkg;
                    if (JobSchedulerService.this.mColorOsLowPowerModeEnabled.get()) {
                        JobSchedulerService.this.mHandler.removeMessages(0);
                        JobSchedulerService.this.mHandler.obtainMessage(0).sendToTarget();
                    }
                }
            }
        };
        this.mHandler = new JobHandler(context.getMainLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mJobSchedulerStub = new JobSchedulerStub();
        this.mJobs = JobStore.initAndGet(this);
        this.mControllers = new ArrayList();
        this.mControllers.add(ConnectivityController.get(this));
        this.mControllers.add(TimeController.get(this));
        this.mControllers.add(IdleController.get(this));
        this.mControllers.add(BatteryController.get(this));
        this.mControllers.add(AppIdleController.get(this));
        this.mControllers.add(ContentObserverController.get(this));
        this.mControllers.add(DeviceIdleJobsController.get(this));
        this.mControllers.add(BatteryIdleController.get(this));
        this.mControllers.add(OppoAppChangeController.get(this));
        this.mControllers.add(OppoCpuController.get(this));
        OppoBPMHelper.setJobSchedulerService(this);
        HandlerThread thread = new HandlerThread("OppoJobServiceHandler");
        thread.start();
        this.mJobServiceHandler = new Handler(thread.getLooper());
    }

    public void onStart() {
        publishLocalService(JobSchedulerInternal.class, new LocalService());
        publishBinderService("jobscheduler", this.mJobSchedulerStub);
    }

    public void onBootPhase(int phase) {
        if (500 == phase) {
            this.mConstants.start(getContext().getContentResolver());
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
            filter.addDataScheme("package");
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_REMOVED"), null, null);
            this.mPowerManager = (PowerManager) getContext().getSystemService("power");
            IntentFilter stopfilter = new IntentFilter("android.intent.action.PACKAGE_RESTARTED");
            stopfilter.addDataScheme("package");
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, stopfilter, null, null);
            try {
                ActivityManagerNative.getDefault().registerUidObserver(this.mUidObserver, 7);
            } catch (RemoteException e) {
            }
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
            this.mPowerManagerInternal.registerColorOsLowPowerModeObserver(new LowPowerModeListener() {
                public void onLowPowerModeChanged(boolean enabled) {
                    JobSchedulerService.this.onLowPowerModeChangedInternal(enabled);
                }
            });
            this.mColorOsLowPowerModeEnabled.set(this.mPowerManagerInternal.getColorOsLowPowerModeEnabled());
            OppoProtectEyeManagerService.setActivityChangedListener(this.mActivityChangedListener);
            getCpuThermalPath();
        } else if (phase == 600) {
            this.mBatteryManagerInternal = (BatteryManagerInternal) -wrap1(BatteryManagerInternal.class);
            synchronized (this.mLock) {
                this.mReadyToRock = true;
                this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
                this.mLocalDeviceIdleController = (com.android.server.DeviceIdleController.LocalService) LocalServices.getService(com.android.server.DeviceIdleController.LocalService.class);
                for (int i = 0; i < 16; i++) {
                    this.mActiveServices.add(new JobServiceContext(this, this.mBatteryStats, this.mJobPackageTracker, getContext().getMainLooper()));
                }
                this.mJobs.forEachJob(new JobStatusFunctor() {
                    public void process(JobStatus job) {
                        for (int controller = 0; controller < JobSchedulerService.this.mControllers.size(); controller++) {
                            ((StateController) JobSchedulerService.this.mControllers.get(controller)).maybeStartTrackingJobLocked(job, null);
                        }
                    }
                });
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    private void startTrackingJob(JobStatus jobStatus, JobStatus lastJob) {
        synchronized (this.mLock) {
            boolean update = this.mJobs.add(jobStatus);
            if (this.mReadyToRock) {
                for (int i = 0; i < this.mControllers.size(); i++) {
                    StateController controller = (StateController) this.mControllers.get(i);
                    if (update) {
                        controller.maybeStopTrackingJobLocked(jobStatus, null, true);
                    }
                    controller.maybeStartTrackingJobLocked(jobStatus, lastJob);
                }
            }
        }
    }

    private boolean stopTrackingJob(JobStatus jobStatus, JobStatus incomingJob, boolean writeBack) {
        boolean removed;
        synchronized (this.mLock) {
            removed = this.mJobs.remove(jobStatus, writeBack);
            if (removed && this.mReadyToRock) {
                for (int i = 0; i < this.mControllers.size(); i++) {
                    ((StateController) this.mControllers.get(i)).maybeStopTrackingJobLocked(jobStatus, incomingJob, false);
                }
            }
        }
        return removed;
    }

    private boolean stopJobOnServiceContextLocked(JobStatus job, int reason) {
        int i = 0;
        while (i < this.mActiveServices.size()) {
            JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus executing = jsc.getRunningJob();
            if (executing == null || !executing.matches(job.getUid(), job.getJobId())) {
                i++;
            } else {
                jsc.cancelExecutingJob(reason);
                return true;
            }
        }
        return false;
    }

    private boolean updateParamterOnServiceContextLocked(JobStatus job) {
        int i = 0;
        while (i < this.mActiveServices.size()) {
            JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus executing = jsc.getRunningJob();
            if (executing == null || !executing.matches(job.getUid(), job.getJobId())) {
                i++;
            } else {
                Slog.d(TAG, "update for task " + job.getTag() + ", level=" + job.cpuLevel.get());
                jsc.updateExecutingParameter(job.cpuLevel.get());
                return true;
            }
        }
        return false;
    }

    public boolean isRunningHighCpuJobs() {
        synchronized (this.mJobs) {
            for (int i = 0; i < this.mActiveServices.size(); i++) {
                JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                if (jsc.getRunningJob() != null) {
                    JobStatus js = jsc.getRunningJob();
                    if (js != null && js.hasCpuConstraint()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void uploadJobStopEvent(JobStatus job) {
        if (job != null && job.getServiceComponent() != null && job.getJob() != null && job.getJob().getService() != null && job.isOppoJob()) {
            boolean foreStopped = job.hasProtectForeConstraint() && !job.isConstraintSatisfied(2048);
            boolean cpuStopped = job.hasCpuConstraint() && !job.isConstraintSatisfied(4096);
            if (foreStopped || cpuStopped) {
                HashMap<String, String> map = new HashMap();
                map.put("pkgname", job.getServiceComponent().getPackageName());
                map.put("componentName", job.getJob().getService().flattenToShortString());
                map.put("jobId", String.valueOf(job.getJobId()));
                map.put("foreStop", String.valueOf(foreStopped));
                map.put("cpuStop", String.valueOf(cpuStopped));
                map.put("cpuValue", SystemProperties.get("oppo.saved.cpu.value"));
                map.put("foreType", String.valueOf(job.getProtectForeType()));
                OppoStatistics.onCommon(getContext(), DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "job_stop", map, false);
            }
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void uploadJobScheduleEvent(JobStatus job) {
        if (job != null && job.getServiceComponent() != null && job.getJob() != null && job.getJob().getService() != null && job.isOppoJob()) {
            HashMap<String, String> map = new HashMap();
            map.put("pkgname", job.getServiceComponent().getPackageName());
            map.put("componentName", job.getJob().getService().flattenToShortString());
            map.put("jobId", String.valueOf(job.getJobId()));
            map.put("foreCons", String.valueOf(job.hasProtectForeConstraint()));
            map.put("foreType", String.valueOf(job.getProtectForeType()));
            map.put("foreSatis", String.valueOf(job.isConstraintSatisfied(2048)));
            map.put("cpuCons", String.valueOf(job.hasCpuConstraint()));
            map.put("cpuSatis", String.valueOf(job.isConstraintSatisfied(4096)));
            map.put("cpuValue", SystemProperties.get("oppo.saved.cpu.value"));
            map.put("delay", String.valueOf(job.getJob().getMinLatencyMillis()));
            map.put("deadline", String.valueOf(job.getJob().getMaxExecutionDelayMillis()));
            OppoStatistics.onCommon(getContext(), DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "job_schedule", map, false);
        }
    }

    private boolean isCurrentlyActiveLocked(JobStatus job) {
        for (int i = 0; i < this.mActiveServices.size(); i++) {
            JobStatus running = ((JobServiceContext) this.mActiveServices.get(i)).getRunningJobUnsafeLocked();
            if (running != null && running.matches(job.getUid(), job.getJobId())) {
                return true;
            }
        }
        return false;
    }

    void noteJobsPending(List<JobStatus> jobs) {
        for (int i = jobs.size() - 1; i >= 0; i--) {
            this.mJobPackageTracker.notePending((JobStatus) jobs.get(i));
        }
    }

    void noteJobsNonpending(List<JobStatus> jobs) {
        for (int i = jobs.size() - 1; i >= 0; i--) {
            this.mJobPackageTracker.noteNonpending((JobStatus) jobs.get(i));
        }
    }

    private JobStatus getRescheduleJobForFailure(JobStatus failureToReschedule) {
        long delayMillis;
        long elapsedNowMillis = SystemClock.elapsedRealtime();
        JobInfo job = failureToReschedule.getJob();
        long initialBackoffMillis = job.getInitialBackoffMillis();
        int backoffAttempts = failureToReschedule.getNumFailures() + 1;
        switch (job.getBackoffPolicy()) {
            case 0:
                delayMillis = initialBackoffMillis * ((long) backoffAttempts);
                break;
            case 1:
                break;
            default:
                if (DEBUG) {
                    Slog.v(TAG, "Unrecognised back-off policy, defaulting to exponential.");
                    break;
                }
                break;
        }
        delayMillis = (long) Math.scalb((float) initialBackoffMillis, backoffAttempts - 1);
        delayMillis = Math.min(delayMillis, 18000000);
        if (job.getOppoJob()) {
            delayMillis = 0;
        }
        JobStatus newJob = new JobStatus(failureToReschedule, elapsedNowMillis + delayMillis, JobStatus.NO_LATEST_RUNTIME, backoffAttempts);
        for (int ic = 0; ic < this.mControllers.size(); ic++) {
            ((StateController) this.mControllers.get(ic)).rescheduleForFailure(newJob, failureToReschedule);
        }
        return newJob;
    }

    private JobStatus getRescheduleJobForPeriodic(JobStatus periodicToReschedule) {
        long elapsedNow = SystemClock.elapsedRealtime();
        long runEarly = 0;
        if (periodicToReschedule.hasDeadlineConstraint()) {
            runEarly = Math.max(periodicToReschedule.getLatestRunTimeElapsed() - elapsedNow, 0);
        }
        long flex = periodicToReschedule.getJob().getFlexMillis();
        long newLatestRuntimeElapsed = (elapsedNow + runEarly) + periodicToReschedule.getJob().getIntervalMillis();
        long newEarliestRunTimeElapsed = newLatestRuntimeElapsed - flex;
        if (DEBUG) {
            Slog.v(TAG, "Rescheduling executed periodic. New execution window [" + (newEarliestRunTimeElapsed / 1000) + ", " + (newLatestRuntimeElapsed / 1000) + "]s");
        }
        return new JobStatus(periodicToReschedule, newEarliestRunTimeElapsed, newLatestRuntimeElapsed, 0);
    }

    public void onJobCompleted(JobStatus jobStatus, boolean needsReschedule) {
        if (DEBUG) {
            Slog.d(TAG, "Completed " + jobStatus + ", reschedule=" + needsReschedule);
        }
        if (stopTrackingJob(jobStatus, null, !jobStatus.getJob().isPeriodic())) {
            if (needsReschedule) {
                startTrackingJob(getRescheduleJobForFailure(jobStatus), jobStatus);
            } else if (jobStatus.getJob().isPeriodic()) {
                startTrackingJob(getRescheduleJobForPeriodic(jobStatus), jobStatus);
            }
            reportActive();
            this.mHandler.obtainMessage(3).sendToTarget();
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "Could not find job to remove. Was job removed while executing?");
        }
        this.mHandler.obtainMessage(3).sendToTarget();
    }

    public void onControllerStateChanged() {
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public void onRunJobNow(JobStatus jobStatus) {
        this.mHandler.obtainMessage(0, jobStatus).sendToTarget();
    }

    void rcdBattIdleStart() {
    }

    private String getJobPkgName(JobStatus js) {
        JobInfo jobInfo = js.getJob();
        if (jobInfo == null || jobInfo.getService() == null || jobInfo.getService().getPackageName() == null) {
            return js.getBatteryName();
        }
        return jobInfo.getService().getPackageName();
    }

    private int adjustJobPriority(int curPriority, JobStatus job) {
        if (curPriority >= 40) {
            return curPriority;
        }
        float factor = this.mJobPackageTracker.getLoadFactor(job);
        if (factor >= this.mConstants.HEAVY_USE_FACTOR) {
            return curPriority - 80;
        }
        if (factor >= this.mConstants.MODERATE_USE_FACTOR) {
            return curPriority - 40;
        }
        return curPriority;
    }

    private int evaluateJobPriorityLocked(JobStatus job) {
        int priority = job.getPriority();
        if (priority >= 30) {
            return adjustJobPriority(priority, job);
        }
        int override = this.mUidPriorityOverride.get(job.getSourceUid(), 0);
        if (override != 0) {
            return adjustJobPriority(override, job);
        }
        return adjustJobPriority(priority, job);
    }

    /* JADX WARNING: Removed duplicated region for block: B:96:0x00f6 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0157  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void assignJobsToContextsLocked() {
        int memLevel;
        int i;
        if (DEBUG) {
            Slog.d(TAG, printPendingQueue());
        }
        try {
            memLevel = ActivityManagerNative.getDefault().getMemoryTrimLevel();
        } catch (RemoteException e) {
            memLevel = 0;
        }
        switch (memLevel) {
            case 1:
                this.mMaxActiveJobs = this.mConstants.BG_MODERATE_JOB_COUNT;
                break;
            case 2:
                this.mMaxActiveJobs = this.mConstants.BG_LOW_JOB_COUNT;
                break;
            case 3:
                this.mMaxActiveJobs = this.mConstants.BG_CRITICAL_JOB_COUNT;
                break;
            default:
                this.mMaxActiveJobs = this.mConstants.BG_NORMAL_JOB_COUNT;
                break;
        }
        JobStatus[] contextIdToJobMap = this.mTmpAssignContextIdToJobMap;
        boolean[] act = this.mTmpAssignAct;
        int[] preferredUidForContext = this.mTmpAssignPreferredUidForContext;
        int numActive = 0;
        int numForeground = 0;
        for (i = 0; i < 16; i++) {
            JobServiceContext js = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus status = js.getRunningJob();
            contextIdToJobMap[i] = status;
            if (status != null) {
                numActive++;
                if (status.lastEvaluatedPriority >= 40) {
                    numForeground++;
                }
            }
            act[i] = false;
            preferredUidForContext[i] = js.getPreferredUid();
        }
        if (DEBUG) {
            Slog.d(TAG, printContextIdToJobMap(contextIdToJobMap, "running jobs initial"));
        }
        for (i = 0; i < this.mPendingJobs.size(); i++) {
            JobStatus nextPending = (JobStatus) this.mPendingJobs.get(i);
            if (findJobContextIdFromMap(nextPending, contextIdToJobMap) == -1) {
                int priority = evaluateJobPriorityLocked(nextPending);
                nextPending.lastEvaluatedPriority = priority;
                int minPriority = Integer.MAX_VALUE;
                int minPriorityContextId = -1;
                for (int j = 0; j < 16; j++) {
                    JobStatus job = contextIdToJobMap[j];
                    int preferredUid = preferredUidForContext[j];
                    if (job == null) {
                        if ((numActive < this.mMaxActiveJobs || (priority >= 40 && numForeground < this.mConstants.FG_JOB_COUNT)) && (preferredUid == nextPending.getUid() || preferredUid == -1)) {
                            minPriorityContextId = j;
                            if (minPriorityContextId == -1) {
                                contextIdToJobMap[minPriorityContextId] = nextPending;
                                act[minPriorityContextId] = true;
                                numActive++;
                                if (priority >= 40) {
                                    numForeground++;
                                }
                            }
                        }
                    } else {
                        if (job.getUid() == nextPending.getUid() && evaluateJobPriorityLocked(job) < nextPending.lastEvaluatedPriority && minPriority > nextPending.lastEvaluatedPriority) {
                            minPriority = nextPending.lastEvaluatedPriority;
                            minPriorityContextId = j;
                        }
                    }
                }
                if (minPriorityContextId == -1) {
                }
            }
        }
        if (DEBUG) {
            Slog.d(TAG, printContextIdToJobMap(contextIdToJobMap, "running jobs final"));
        }
        this.mJobPackageTracker.noteConcurrency(numActive, numForeground);
        for (i = 0; i < 16; i++) {
            boolean preservePreferredUid = false;
            if (act[i]) {
                if (((JobServiceContext) this.mActiveServices.get(i)).getRunningJob() != null) {
                    if (DEBUG) {
                        Slog.d(TAG, "preempting job: " + ((JobServiceContext) this.mActiveServices.get(i)).getRunningJob());
                    }
                    ((JobServiceContext) this.mActiveServices.get(i)).preemptExecutingJob();
                    preservePreferredUid = true;
                } else {
                    JobStatus pendingJob = contextIdToJobMap[i];
                    if (DEBUG) {
                        Slog.d(TAG, "About to run job on context " + String.valueOf(i) + ", job: " + pendingJob);
                    }
                    for (int ic = 0; ic < this.mControllers.size(); ic++) {
                        ((StateController) this.mControllers.get(ic)).prepareForExecutionLocked(pendingJob);
                    }
                    if (!((JobServiceContext) this.mActiveServices.get(i)).executeRunnableJob(pendingJob)) {
                        Slog.d(TAG, "Error executing " + pendingJob);
                        this.mJobServiceHandler.post(new RemovePendingJobRunnable(pendingJob));
                    }
                    if (this.mPendingJobs.remove(pendingJob)) {
                        this.mJobPackageTracker.noteNonpending(pendingJob);
                    }
                }
            }
            if (!preservePreferredUid) {
                ((JobServiceContext) this.mActiveServices.get(i)).clearPreferredUid();
            }
        }
    }

    int findJobContextIdFromMap(JobStatus jobStatus, JobStatus[] map) {
        int i = 0;
        while (i < map.length) {
            if (map[i] != null && map[i].matches(jobStatus.getUid(), jobStatus.getJobId())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    int executeRunCommand(String pkgName, int userId, int jobId, boolean force) {
        if (DEBUG) {
            Slog.v(TAG, "executeRunCommand(): " + pkgName + "/" + userId + " " + jobId + " f=" + force);
        }
        try {
            int uid = AppGlobals.getPackageManager().getPackageUid(pkgName, 0, userId);
            if (uid < 0) {
                return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            synchronized (this.mLock) {
                JobStatus js = this.mJobs.getJobByUidAndJobId(uid, jobId);
                if (js == null) {
                    return JobSchedulerShellCommand.CMD_ERR_NO_JOB;
                }
                js.overrideState = force ? 2 : 1;
                if (js.isConstraintsSatisfied()) {
                    this.mHandler.obtainMessage(3).sendToTarget();
                } else {
                    js.overrideState = 0;
                    return JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS;
                }
            }
        } catch (RemoteException e) {
        }
        return 0;
    }

    private String printContextIdToJobMap(JobStatus[] map, String initial) {
        StringBuilder s = new StringBuilder(initial + ": ");
        for (int i = 0; i < map.length; i++) {
            s.append("(").append(map[i] == null ? -1 : map[i].getJobId()).append(map[i] == null ? -1 : map[i].getUid()).append(")");
        }
        return s.toString();
    }

    private String printPendingQueue() {
        StringBuilder s = new StringBuilder("Pending queue: ");
        Iterator<JobStatus> it = this.mPendingJobs.iterator();
        while (it.hasNext()) {
            JobStatus js = (JobStatus) it.next();
            s.append("(").append(js.getJob().getId()).append(", ").append(js.getUid()).append(") ");
        }
        return s.toString();
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Job Scheduler (jobscheduler) dump options:");
        pw.println("  [-h] [package] ...");
        pw.println("    -h: print this help");
        pw.println("  [package] is an optional package name to limit the output to.");
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x007c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dumpInternal(PrintWriter pw, String[] args) {
        int filterUid = -1;
        if (!ArrayUtils.isEmpty(args)) {
            int opti = 0;
            while (opti < args.length) {
                String arg = args[opti];
                if ("-h".equals(arg)) {
                    dumpHelp(pw);
                    return;
                } else if ("-a".equals(arg)) {
                    opti++;
                } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                    if ("log".equals(arg)) {
                        dynamicLogConfig(pw, args);
                        return;
                    }
                    if (opti < args.length) {
                        String pkg = args[opti];
                        try {
                            filterUid = getContext().getPackageManager().getPackageUid(pkg, DumpState.DUMP_PREFERRED_XML);
                        } catch (NameNotFoundException e) {
                            pw.println("Invalid package: " + pkg);
                            return;
                        }
                    }
                } else {
                    pw.println("Unknown option: " + arg);
                    return;
                }
            }
            if (opti < args.length) {
            }
        }
        int filterUidFinal = UserHandle.getAppId(filterUid);
        long now = SystemClock.elapsedRealtime();
        synchronized (this.mLock) {
            JobStatus job;
            int i;
            int priority;
            this.mConstants.dump(pw);
            pw.println();
            pw.println("Started users: " + Arrays.toString(this.mStartedUsers));
            pw.print("Registered ");
            pw.print(this.mJobs.size());
            pw.println(" jobs:");
            if (this.mJobs.size() > 0) {
                List<JobStatus> jobs = this.mJobs.mJobSet.getAllJobs();
                Collections.sort(jobs, new Comparator<JobStatus>() {
                    public int compare(JobStatus o1, JobStatus o2) {
                        int i = -1;
                        int uid1 = o1.getUid();
                        int uid2 = o2.getUid();
                        int id1 = o1.getJobId();
                        int id2 = o2.getJobId();
                        if (uid1 != uid2) {
                            if (uid1 >= uid2) {
                                i = 1;
                            }
                            return i;
                        }
                        if (id1 >= id2) {
                            i = id1 > id2 ? 1 : 0;
                        }
                        return i;
                    }
                });
                for (JobStatus job2 : jobs) {
                    pw.print("  JOB #");
                    job2.printUniqueId(pw);
                    pw.print(": ");
                    pw.println(job2.toShortStringExceptUniqueId());
                    if (job2.shouldDump(filterUidFinal)) {
                        job2.dump(pw, "    ", true);
                        pw.print("    Ready: ");
                        pw.print(this.mHandler.isReadyToBeExecutedLocked(job2));
                        pw.print(" (job=");
                        pw.print(job2.isReady());
                        pw.print(" pending=");
                        pw.print(this.mPendingJobs.contains(job2));
                        pw.print(" active=");
                        pw.print(isCurrentlyActiveLocked(job2));
                        pw.print(" user=");
                        pw.print(ArrayUtils.contains(this.mStartedUsers, job2.getUserId()));
                        pw.println(")");
                    }
                }
            } else {
                pw.println("  None.");
            }
            for (i = 0; i < this.mControllers.size(); i++) {
                pw.println();
                ((StateController) this.mControllers.get(i)).dumpControllerStateLocked(pw, filterUidFinal);
            }
            pw.println();
            pw.println("Uid priority overrides:");
            for (i = 0; i < this.mUidPriorityOverride.size(); i++) {
                int uid = this.mUidPriorityOverride.keyAt(i);
                if (filterUidFinal == -1 || filterUidFinal == UserHandle.getAppId(uid)) {
                    pw.print("  ");
                    pw.print(UserHandle.formatUid(uid));
                    pw.print(": ");
                    pw.println(this.mUidPriorityOverride.valueAt(i));
                }
            }
            pw.println();
            this.mJobPackageTracker.dump(pw, IElsaManager.EMPTY_PACKAGE, filterUidFinal);
            pw.println();
            if (this.mJobPackageTracker.dumpHistory(pw, IElsaManager.EMPTY_PACKAGE, filterUidFinal)) {
                pw.println();
            }
            pw.println("Pending queue:");
            for (i = 0; i < this.mPendingJobs.size(); i++) {
                job2 = (JobStatus) this.mPendingJobs.get(i);
                pw.print("  Pending #");
                pw.print(i);
                pw.print(": ");
                pw.println(job2.toShortString());
                job2.dump(pw, "    ", false);
                priority = evaluateJobPriorityLocked(job2);
                if (priority != 0) {
                    pw.print("    Evaluated priority: ");
                    pw.println(priority);
                }
                pw.print("    Tag: ");
                pw.println(job2.getTag());
            }
            pw.println();
            pw.println("Active jobs:");
            for (i = 0; i < this.mActiveServices.size(); i++) {
                JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                pw.print("  Slot #");
                pw.print(i);
                pw.print(": ");
                if (jsc.getRunningJob() == null) {
                    pw.println("inactive");
                } else {
                    pw.println(jsc.getRunningJob().toShortString());
                    pw.print("    Running for: ");
                    TimeUtils.formatDuration(now - jsc.getExecutionStartTimeElapsed(), pw);
                    pw.print(", timeout at: ");
                    TimeUtils.formatDuration(jsc.getTimeoutElapsed() - now, pw);
                    pw.println();
                    jsc.getRunningJob().dump(pw, "    ", false);
                    priority = evaluateJobPriorityLocked(jsc.getRunningJob());
                    if (priority != 0) {
                        pw.print("    Evaluated priority: ");
                        pw.println(priority);
                    }
                }
            }
            if (filterUid == -1) {
                pw.println();
                pw.print("mReadyToRock=");
                pw.println(this.mReadyToRock);
                pw.print("mReportedActive=");
                pw.println(this.mReportedActive);
                pw.print("mMaxActiveJobs=");
                pw.println(this.mMaxActiveJobs);
            }
        }
        pw.println();
    }

    private void dynamicLogConfig(PrintWriter pw, String[] args) {
        if (args != null && args.length != 0 && pw != null) {
            String type = args[0];
            if (type != null && type.equals("log")) {
                if (args.length == 1) {
                    pw.println("dumpsys jobscheduler log 0/1");
                } else if (args.length == 2) {
                    String value = args[1];
                    if (value != null) {
                        if (value.equals("0")) {
                            pw.println("JobScheduler DEBUG OFF");
                            DEBUG = false;
                        } else if (value.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                            pw.println("JobScheduler DEBUG ON");
                            DEBUG = true;
                        } else {
                            pw.println("Invalid argument! Input 0 or 1.");
                        }
                    }
                }
            }
        }
    }

    public void stopStrictMode() {
        this.mHandler.obtainMessage(0).sendToTarget();
        if (DEBUG) {
            Slog.d(TAG, "stopStrictMode");
        }
    }

    private void onLowPowerModeChangedInternal(boolean enabled) {
        this.mColorOsLowPowerModeEnabled.set(enabled);
        this.mHandler.obtainMessage(0).sendToTarget();
    }

    private void uploadScheduleBattIdleJob(JobStatus job) {
        JobInfo jobInfo = job.getJob();
        if (jobInfo != null && jobInfo.isRequireBattIdle()) {
            Map<String, String> eventMap = new HashMap();
            String pkgName = getJobPkgName(job);
            eventMap.put("pkgname", pkgName);
            eventMap.put("jobId", String.valueOf(job.getJobId()));
            if (!(jobInfo.getService() == null || jobInfo.getService().flattenToShortString() == null)) {
                eventMap.put("componentName", jobInfo.getService().flattenToShortString());
            }
            if (OPPODEBUG) {
                Slog.d(TAG, "ScheduleBattIdleJob: pkgName=" + pkgName);
            }
            OppoStatistics.onCommon(getContext(), DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "battIdle_job_schedule", eventMap, false);
        }
    }

    private void getCpuThermalPath() {
    }

    /* JADX WARNING: Missing block: B:8:0x0011, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static HashMap<String, Integer> getCpuThermal() {
        synchronized (mThermalPath) {
            if (mThermalPath == null || mThermalPath.isEmpty()) {
            } else {
                HashMap<String, Integer> cpuThermal = new HashMap();
                for (Entry<String, String> ent : mThermalPath.entrySet()) {
                    String type = (String) ent.getKey();
                    int thermal = readIntFromFile((String) ent.getValue());
                    if (type.equals(THERMAL_TYPE.get(0))) {
                        type = "PoPMem";
                    } else if (type.equals(THERMAL_TYPE.get(1))) {
                        type = "Gpu";
                    } else if (type.equals(THERMAL_TYPE.get(2))) {
                        type = "Cpu0123";
                    } else if (type.equals(THERMAL_TYPE.get(3))) {
                        type = "Cpu4";
                    }
                    cpuThermal.put(type, Integer.valueOf(thermal));
                }
                return cpuThermal;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0085 A:{SYNTHETIC, Splitter: B:23:0x0085} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readStrFromFile(String path) {
        String tempString;
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(path)));
            try {
                tempString = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1) {
                        Slog.e(TAG, "readStrFromFile io close exception :" + e1.getMessage());
                    }
                }
                reader = reader2;
            } catch (IOException e2) {
                e = e2;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            tempString = null;
            try {
                Slog.e(TAG, "readStrFromFile io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readStrFromFile io close exception :" + e12.getMessage());
                    }
                }
                return tempString;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.e(TAG, "readStrFromFile io close exception :" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        return tempString;
    }

    private static int readIntFromFile(String path) {
        String str = readStrFromFile(path);
        int result = 0;
        if (str == null || IElsaManager.EMPTY_PACKAGE.equals(str)) {
            return result;
        }
        try {
            return Integer.valueOf(str).intValue();
        } catch (NumberFormatException e) {
            Slog.e(TAG, "readIntFromFile NumberFormatException:" + e.getMessage());
            return result;
        }
    }
}
