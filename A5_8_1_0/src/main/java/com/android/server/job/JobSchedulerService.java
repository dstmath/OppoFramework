package com.android.server.job;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.IUidObserver;
import android.app.IUidObserver.Stub;
import android.app.job.IJobScheduler;
import android.app.job.JobInfo;
import android.app.job.JobWorkItem;
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
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.provider.Settings.Global;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.OppoAlarmManagerHelper;
import com.android.server.OppoBPMHelper;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.SystemService;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.am.OppoAppSwitchManager;
import com.android.server.am.OppoAppSwitchManager.ActivityChangedListener;
import com.android.server.am.OppoProcessManagerHelper;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.job.JobSchedulerInternal.JobStorePersistStats;
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
import com.android.server.job.controllers.StorageController;
import com.android.server.job.controllers.TimeController;
import com.android.server.job.controllers.cpu.OppoCpuController;
import com.android.server.location.LocationFudger;
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
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.util.EmptyArray;
import oppo.util.OppoStatistics;

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
    public static boolean OPPODEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    static final String TAG = "JobSchedulerService";
    static final List<String> THERMAL_TYPE = Arrays.asList(new String[]{"tsens_tz_sensor2", "tsens_tz_sensor10", "tsens_tz_sensor9", "tsens_tz_sensor4"});
    static final Comparator<JobStatus> mEnqueueTimeComparator = -$Lambda$MZyz9fgevtnL7iKUFvjeGfWQ-E8.$INST$0;
    private static HashMap<String, String> mThermalPath = new HashMap();
    final List<JobServiceContext> mActiveServices = new ArrayList();
    private ActivityChangedListener mActivityChangedListener = new ActivityChangedListener() {
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
    final SparseIntArray mBackingUpUids = new SparseIntArray();
    BatteryController mBatteryController;
    BatteryManagerInternal mBatteryManagerInternal;
    IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:88:0x0302 A:{Splitter: B:18:0x0094, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
        /* JADX WARNING: Missing block: B:106:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Receieved: " + action);
            }
            String pkgName = JobSchedulerService.this.getPackageName(intent);
            int pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
            int userId;
            if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
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
                                    JobSchedulerService.this.cancelJobsForPackageAndUid(pkgName, pkgUid, "app disabled");
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
                    JobSchedulerService.this.cancelJobsForPackageAndUid(pkgName, uidRemoved, "app uninstalled");
                }
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                userId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "Removing jobs for user: " + userId);
                }
                JobSchedulerService.this.cancelJobsForUser(userId);
            } else if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
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
            } else if ("android.intent.action.PACKAGE_RESTARTED".equals(action) && pkgUid != -1) {
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "Removing jobs for pkg " + pkgName + " at uid " + pkgUid);
                }
                boolean doCancel = OppoAlarmManagerHelper.isFilterRemovePackage(pkgName) ^ 1;
                if (!(doCancel || pkgUid == 1000)) {
                    int jobCount;
                    synchronized (JobSchedulerService.this.mLock) {
                        jobCount = JobSchedulerService.this.mJobs.countJobsForUid(pkgUid);
                    }
                    if (jobCount > 100) {
                        Slog.w(JobSchedulerService.TAG, "Too many jobs for uid " + pkgUid + ", clear for forcestop");
                        doCancel = true;
                    }
                }
                if (doCancel) {
                    JobSchedulerService.this.cancelJobsForPackageAndUid(pkgName, pkgUid, "app force stopped");
                }
            }
        }
    };
    private AtomicBoolean mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
    final Constants mConstants;
    List<StateController> mControllers;
    final JobHandler mHandler;
    final JobPackageTracker mJobPackageTracker = new JobPackageTracker();
    final JobSchedulerStub mJobSchedulerStub;
    private Handler mJobServiceHandler = null;
    private final Runnable mJobTimeUpdater = new com.android.server.job.-$Lambda$MZyz9fgevtnL7iKUFvjeGfWQ-E8.AnonymousClass1(this);
    final JobStore mJobs;
    ArrayList<JobServiceContext> mListBattIdleJobStart = new ArrayList();
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    final Object mLock = new Object();
    int mMaxActiveJobs = 1;
    private final MaybeReadyJobQueueFunctor mMaybeQueueFunctor = new MaybeReadyJobQueueFunctor();
    final ArrayList<JobStatus> mPendingJobs = new ArrayList();
    PowerManager mPowerManager;
    private PowerManagerInternal mPowerManagerInternal;
    private final ReadyJobQueueFunctor mReadyQueueFunctor = new ReadyJobQueueFunctor();
    boolean mReadyToRock;
    boolean mReportedActive;
    int[] mStartedUsers = EmptyArray.INT;
    StorageController mStorageController;
    private final BroadcastReceiver mTimeSetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.TIME_SET".equals(intent.getAction()) && JobSchedulerService.this.mJobs.clockNowValidToInflate(System.currentTimeMillis())) {
                Slog.i(JobSchedulerService.TAG, "RTC now valid; recalculating persisted job windows");
                context.unregisterReceiver(this);
                FgThread.getHandler().post(JobSchedulerService.this.mJobTimeUpdater);
            }
        }
    };
    boolean[] mTmpAssignAct = new boolean[16];
    JobStatus[] mTmpAssignContextIdToJobMap = new JobStatus[16];
    int[] mTmpAssignPreferredUidForContext = new int[16];
    private volatile String mTopPkg = "";
    private final IUidObserver mUidObserver = new Stub() {
        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            JobSchedulerService.this.updateUidState(uid, procState);
        }

        public void onUidGone(int uid, boolean disabled) {
            JobSchedulerService.this.updateUidState(uid, 17);
            if (disabled) {
                JobSchedulerService.this.cancelJobsForUid(uid, "uid gone");
            }
        }

        public void onUidActive(int uid) throws RemoteException {
        }

        public void onUidIdle(int uid, boolean disabled) {
            if (disabled) {
                JobSchedulerService.this.cancelJobsForUid(uid, "app uid idle");
            }
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    };
    final SparseIntArray mUidPriorityOverride = new SparseIntArray();

    private final class Constants extends ContentObserver {
        private static final int DEFAULT_BG_CRITICAL_JOB_COUNT = 1;
        private static final int DEFAULT_BG_LOW_JOB_COUNT = 1;
        private static final int DEFAULT_BG_MODERATE_JOB_COUNT = 4;
        private static final int DEFAULT_BG_NORMAL_JOB_COUNT = 6;
        private static final int DEFAULT_FG_JOB_COUNT = 4;
        private static final float DEFAULT_HEAVY_USE_FACTOR = 0.9f;
        private static final int DEFAULT_MAX_STANDARD_RESCHEDULE_COUNT = Integer.MAX_VALUE;
        private static final int DEFAULT_MAX_WORK_RESCHEDULE_COUNT = Integer.MAX_VALUE;
        private static final int DEFAULT_MIN_BATTERY_NOT_LOW_COUNT = 1;
        private static final int DEFAULT_MIN_CHARGING_COUNT = 1;
        private static final int DEFAULT_MIN_CONNECTIVITY_COUNT = 1;
        private static final int DEFAULT_MIN_CONTENT_COUNT = 1;
        private static final long DEFAULT_MIN_EXP_BACKOFF_TIME = 10000;
        private static final int DEFAULT_MIN_IDLE_COUNT = 1;
        private static final long DEFAULT_MIN_LINEAR_BACKOFF_TIME = 10000;
        private static final int DEFAULT_MIN_READY_JOBS_COUNT = 1;
        private static final int DEFAULT_MIN_STORAGE_NOT_LOW_COUNT = 1;
        private static final float DEFAULT_MODERATE_USE_FACTOR = 0.5f;
        private static final String KEY_BG_CRITICAL_JOB_COUNT = "bg_critical_job_count";
        private static final String KEY_BG_LOW_JOB_COUNT = "bg_low_job_count";
        private static final String KEY_BG_MODERATE_JOB_COUNT = "bg_moderate_job_count";
        private static final String KEY_BG_NORMAL_JOB_COUNT = "bg_normal_job_count";
        private static final String KEY_FG_JOB_COUNT = "fg_job_count";
        private static final String KEY_HEAVY_USE_FACTOR = "heavy_use_factor";
        private static final String KEY_MAX_STANDARD_RESCHEDULE_COUNT = "max_standard_reschedule_count";
        private static final String KEY_MAX_WORK_RESCHEDULE_COUNT = "max_work_reschedule_count";
        private static final String KEY_MIN_BATTERY_NOT_LOW_COUNT = "min_battery_not_low_count";
        private static final String KEY_MIN_CHARGING_COUNT = "min_charging_count";
        private static final String KEY_MIN_CONNECTIVITY_COUNT = "min_connectivity_count";
        private static final String KEY_MIN_CONTENT_COUNT = "min_content_count";
        private static final String KEY_MIN_EXP_BACKOFF_TIME = "min_exp_backoff_time";
        private static final String KEY_MIN_IDLE_COUNT = "min_idle_count";
        private static final String KEY_MIN_LINEAR_BACKOFF_TIME = "min_linear_backoff_time";
        private static final String KEY_MIN_READY_JOBS_COUNT = "min_ready_jobs_count";
        private static final String KEY_MIN_STORAGE_NOT_LOW_COUNT = "min_storage_not_low_count";
        private static final String KEY_MODERATE_USE_FACTOR = "moderate_use_factor";
        static final int MIN_CPU_COUNT = 1;
        static final int MIN_FORE_COUNT = 1;
        int BG_CRITICAL_JOB_COUNT = 1;
        int BG_LOW_JOB_COUNT = 1;
        int BG_MODERATE_JOB_COUNT = 4;
        int BG_NORMAL_JOB_COUNT = 6;
        int FG_JOB_COUNT = 4;
        float HEAVY_USE_FACTOR = DEFAULT_HEAVY_USE_FACTOR;
        int MAX_STANDARD_RESCHEDULE_COUNT = Integer.MAX_VALUE;
        int MAX_WORK_RESCHEDULE_COUNT = Integer.MAX_VALUE;
        int MIN_BATTERY_NOT_LOW_COUNT = 1;
        int MIN_CHARGING_COUNT = 1;
        int MIN_CONNECTIVITY_COUNT = 1;
        int MIN_CONTENT_COUNT = 1;
        long MIN_EXP_BACKOFF_TIME = 10000;
        int MIN_IDLE_COUNT = 1;
        long MIN_LINEAR_BACKOFF_TIME = 10000;
        int MIN_READY_JOBS_COUNT = 1;
        int MIN_STORAGE_NOT_LOW_COUNT = 1;
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
                    this.mParser.setString(Global.getString(this.mResolver, "job_scheduler_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(JobSchedulerService.TAG, "Bad jobscheduler settings", e);
                }
                this.MIN_IDLE_COUNT = this.mParser.getInt(KEY_MIN_IDLE_COUNT, 1);
                this.MIN_CHARGING_COUNT = this.mParser.getInt(KEY_MIN_CHARGING_COUNT, 1);
                this.MIN_BATTERY_NOT_LOW_COUNT = this.mParser.getInt(KEY_MIN_BATTERY_NOT_LOW_COUNT, 1);
                this.MIN_STORAGE_NOT_LOW_COUNT = this.mParser.getInt(KEY_MIN_STORAGE_NOT_LOW_COUNT, 1);
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
                this.MAX_STANDARD_RESCHEDULE_COUNT = this.mParser.getInt(KEY_MAX_STANDARD_RESCHEDULE_COUNT, Integer.MAX_VALUE);
                this.MAX_WORK_RESCHEDULE_COUNT = this.mParser.getInt(KEY_MAX_WORK_RESCHEDULE_COUNT, Integer.MAX_VALUE);
                this.MIN_LINEAR_BACKOFF_TIME = this.mParser.getLong(KEY_MIN_LINEAR_BACKOFF_TIME, 10000);
                this.MIN_EXP_BACKOFF_TIME = this.mParser.getLong(KEY_MIN_EXP_BACKOFF_TIME, 10000);
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
            pw.print(KEY_MIN_BATTERY_NOT_LOW_COUNT);
            pw.print("=");
            pw.print(this.MIN_BATTERY_NOT_LOW_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_STORAGE_NOT_LOW_COUNT);
            pw.print("=");
            pw.print(this.MIN_STORAGE_NOT_LOW_COUNT);
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
            pw.print("    ");
            pw.print(KEY_MAX_STANDARD_RESCHEDULE_COUNT);
            pw.print("=");
            pw.print(this.MAX_STANDARD_RESCHEDULE_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MAX_WORK_RESCHEDULE_COUNT);
            pw.print("=");
            pw.print(this.MAX_WORK_RESCHEDULE_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_LINEAR_BACKOFF_TIME);
            pw.print("=");
            pw.print(this.MIN_LINEAR_BACKOFF_TIME);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_EXP_BACKOFF_TIME);
            pw.print("=");
            pw.print(this.MIN_EXP_BACKOFF_TIME);
            pw.println();
        }
    }

    private final class JobHandler extends Handler {
        public JobHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            synchronized (JobSchedulerService.this.mLock) {
                if (JobSchedulerService.this.mReadyToRock) {
                    switch (message.what) {
                        case 0:
                            JobStatus runNow = message.obj;
                            if (runNow != null && JobSchedulerService.this.isReadyToBeExecutedLocked(runNow)) {
                                JobSchedulerService.this.mJobPackageTracker.notePending(runNow);
                                JobSchedulerService.addOrderedItem(JobSchedulerService.this.mPendingJobs, runNow, JobSchedulerService.mEnqueueTimeComparator);
                                break;
                            }
                            JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                            break;
                            break;
                        case 1:
                            if (!JobSchedulerService.this.mReportedActive) {
                                JobSchedulerService.this.maybeQueueReadyJobsForExecutionLocked();
                                break;
                            } else {
                                JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                                break;
                            }
                        case 2:
                            JobSchedulerService.this.cancelJobImplLocked((JobStatus) message.obj, null, "app no longer allowed to run");
                            break;
                        case 3:
                            JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                            break;
                    }
                    JobSchedulerService.this.maybeRunPendingJobsLocked();
                    removeMessages(1);
                    return;
                }
            }
        }
    }

    final class JobSchedulerStub extends IJobScheduler.Stub {
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
                    int scheduleAsPackage = JobSchedulerService.this.scheduleAsPackage(job, null, uid, null, -1, null);
                    return scheduleAsPackage;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("Error: requested job be persisted without holding RECEIVE_BOOT_COMPLETED permission.");
            }
        }

        public int enqueue(JobInfo job, JobWorkItem work) throws RemoteException {
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Enqueueing job: " + job.toString() + " work: " + work);
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            enforceValidJobRequest(uid, job);
            if (job.isPersisted()) {
                throw new IllegalArgumentException("Can't enqueue work for persisted jobs");
            } else if (work == null) {
                throw new NullPointerException("work is null");
            } else {
                if ((job.getFlags() & 1) != 0) {
                    JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", JobSchedulerService.TAG);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    int scheduleAsPackage = JobSchedulerService.this.scheduleAsPackage(job, work, uid, null, -1, null);
                    return scheduleAsPackage;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
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
                    int scheduleAsPackage = JobSchedulerService.this.scheduleAsPackage(job, null, callerUid, packageName, userId, tag);
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
                JobSchedulerService.this.cancelJobsForUid(uid, "cancelAll() called by app");
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
            if (DumpUtils.checkDumpAndUsageStatsPermission(JobSchedulerService.this.getContext(), JobSchedulerService.TAG, pw)) {
                long identityToken = Binder.clearCallingIdentity();
                try {
                    JobSchedulerService.this.dumpInternal(pw, args);
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new JobSchedulerShellCommand(JobSchedulerService.this).exec(this, in, out, err, args, callback, resultReceiver);
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
                        if (job.getJob().isPeriodic() || (JobSchedulerService.this.isCurrentlyActiveLocked(job) ^ 1) != 0) {
                            pendingJobs.add(job.getJob());
                        }
                    }
                });
            }
            return pendingJobs;
        }

        public void addBackingUpUid(int uid) {
            synchronized (JobSchedulerService.this.mLock) {
                JobSchedulerService.this.mBackingUpUids.put(uid, uid);
            }
        }

        public void removeBackingUpUid(int uid) {
            synchronized (JobSchedulerService.this.mLock) {
                JobSchedulerService.this.mBackingUpUids.delete(uid);
                if (JobSchedulerService.this.mJobs.countJobsForUid(uid) > 0) {
                    JobSchedulerService.this.mHandler.obtainMessage(1).sendToTarget();
                }
            }
        }

        public void clearAllBackingUpUids() {
            synchronized (JobSchedulerService.this.mLock) {
                if (JobSchedulerService.this.mBackingUpUids.size() > 0) {
                    JobSchedulerService.this.mBackingUpUids.clear();
                    JobSchedulerService.this.mHandler.obtainMessage(1).sendToTarget();
                }
            }
        }

        public JobStorePersistStats getPersistStats() {
            JobStorePersistStats jobStorePersistStats;
            synchronized (JobSchedulerService.this.mLock) {
                jobStorePersistStats = new JobStorePersistStats(JobSchedulerService.this.mJobs.getPersistStats());
            }
            return jobStorePersistStats;
        }
    }

    final class MaybeReadyJobQueueFunctor implements JobStatusFunctor {
        int backoffCount;
        int batteryNotLowCount;
        int chargingCount;
        int connectivityCount;
        int contentCount;
        int cpuCount = 0;
        int foreCount = 0;
        int idleCount;
        List<JobStatus> runnableJobs;
        int storageNotLowCount;

        public MaybeReadyJobQueueFunctor() {
            reset();
        }

        public void process(JobStatus job) {
            if (JobSchedulerService.this.isReadyToBeExecutedLocked(job)) {
                try {
                    if (ActivityManager.getService().isAppStartModeDisabled(job.getUid(), job.getJob().getService().getPackageName())) {
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
                if (job.hasConnectivityConstraint()) {
                    this.connectivityCount++;
                }
                if (job.hasChargingConstraint()) {
                    this.chargingCount++;
                }
                if (job.hasBatteryNotLowConstraint()) {
                    this.batteryNotLowCount++;
                }
                if (job.hasStorageNotLowConstraint()) {
                    this.storageNotLowCount++;
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
            } else if (JobSchedulerService.this.isReadyToUpdateCpuParameter(job)) {
                JobSchedulerService.this.updateParamterOnServiceContextLocked(job);
            }
        }

        public void postProcess() {
            if (this.backoffCount > 0 || this.idleCount >= JobSchedulerService.this.mConstants.MIN_IDLE_COUNT || this.connectivityCount >= JobSchedulerService.this.mConstants.MIN_CONNECTIVITY_COUNT || this.chargingCount >= JobSchedulerService.this.mConstants.MIN_CHARGING_COUNT || this.batteryNotLowCount >= JobSchedulerService.this.mConstants.MIN_BATTERY_NOT_LOW_COUNT || this.storageNotLowCount >= JobSchedulerService.this.mConstants.MIN_STORAGE_NOT_LOW_COUNT || this.contentCount >= JobSchedulerService.this.mConstants.MIN_CONTENT_COUNT || this.foreCount >= 1 || this.cpuCount >= 1 || (this.runnableJobs != null && this.runnableJobs.size() >= JobSchedulerService.this.mConstants.MIN_READY_JOBS_COUNT)) {
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "maybeQueueReadyJobsForExecutionLocked: Running jobs.");
                }
                if (this.runnableJobs != null) {
                    JobSchedulerService.this.noteJobsPending(this.runnableJobs);
                    JobSchedulerService.this.mPendingJobs.addAll(this.runnableJobs);
                }
                if (JobSchedulerService.this.mPendingJobs.size() > 1) {
                    JobSchedulerService.this.mPendingJobs.sort(JobSchedulerService.mEnqueueTimeComparator);
                }
            } else if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "maybeQueueReadyJobsForExecutionLocked: Not running anything.");
            }
            reset();
        }

        private void reset() {
            this.chargingCount = 0;
            this.idleCount = 0;
            this.backoffCount = 0;
            this.connectivityCount = 0;
            this.batteryNotLowCount = 0;
            this.storageNotLowCount = 0;
            this.contentCount = 0;
            this.foreCount = 0;
            this.cpuCount = 0;
            this.runnableJobs = null;
        }
    }

    final class ReadyJobQueueFunctor implements JobStatusFunctor {
        ArrayList<JobStatus> newReadyJobs;

        ReadyJobQueueFunctor() {
        }

        public void process(JobStatus job) {
            if (JobSchedulerService.this.isReadyToBeExecutedLocked(job)) {
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "    queued " + job.toShortString());
                }
                if (this.newReadyJobs == null) {
                    this.newReadyJobs = new ArrayList();
                }
                this.newReadyJobs.add(job);
            } else if (JobSchedulerService.this.isReadyToUpdateCpuParameter(job)) {
                JobSchedulerService.this.updateParamterOnServiceContextLocked(job);
            }
        }

        public void postProcess() {
            if (this.newReadyJobs != null) {
                JobSchedulerService.this.noteJobsPending(this.newReadyJobs);
                JobSchedulerService.this.mPendingJobs.addAll(this.newReadyJobs);
                if (JobSchedulerService.this.mPendingJobs.size() > 1) {
                    JobSchedulerService.this.mPendingJobs.sort(JobSchedulerService.mEnqueueTimeComparator);
                }
            }
            this.newReadyJobs = null;
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
                    synchronized (JobSchedulerService.this.mLock) {
                        JobSchedulerService.this.stopTrackingJobLocked(this.mPendingJob, null, false);
                    }
                }
            } catch (Exception e) {
                Slog.w(JobSchedulerService.TAG, "removePendingJob failed");
            }
        }
    }

    /* renamed from: lambda$-com_android_server_job_JobSchedulerService_24882 */
    static /* synthetic */ int m104lambda$-com_android_server_job_JobSchedulerService_24882(JobStatus o1, JobStatus o2) {
        if (o1.enqueueTime < o2.enqueueTime) {
            return -1;
        }
        return o1.enqueueTime > o2.enqueueTime ? 1 : 0;
    }

    static <T> void addOrderedItem(ArrayList<T> array, T newItem, Comparator<T> comparator) {
        int where = Collections.binarySearch(array, newItem, comparator);
        if (where < 0) {
            where = ~where;
        }
        array.add(where, newItem);
    }

    private String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        return uri != null ? uri.getSchemeSpecificPart() : null;
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
        if (packageManager == null || packageManager.isClosedSuperFirewall() || job.getService() == null) {
            return true;
        }
        String jobPkg = job.getService().getPackageName();
        if (jobPkg == null) {
            return true;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(jobPkg, 8192);
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

    /* JADX WARNING: Missing block: B:41:0x00fd, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int scheduleAsPackage(JobInfo job, JobWorkItem work, int uId, String packageName, int userId, String tag) {
        try {
            if (ActivityManager.getService().isAppStartModeDisabled(uId, job.getService().getPackageName())) {
                Slog.w(TAG, "Not scheduling job " + uId + ":" + job.toString() + " -- package not allowed to start");
                return 0;
            }
        } catch (RemoteException e) {
        }
        job.setSysApp(minIntervalConstraint(job, uId));
        synchronized (this.mLock) {
            JobStatus toCancel = this.mJobs.getJobByUidAndJobId(uId, job.getId());
            if (work == null || toCancel == null || !toCancel.getJob().equals(job)) {
                JobStatus jobStatus = JobStatus.createFromJobInfo(job, uId, packageName, userId, tag);
                if (DEBUG) {
                    Slog.d(TAG, "SCHEDULE: " + jobStatus.toShortString());
                }
                if (packageName != null || this.mJobs.countJobsForUid(uId) <= 100) {
                    jobStatus.prepareLocked(ActivityManager.getService());
                    if (toCancel != null) {
                        cancelJobImplLocked(toCancel, jobStatus, "job rescheduled by app");
                    }
                    if (work != null) {
                        jobStatus.enqueueWorkLocked(ActivityManager.getService(), work);
                    }
                    startTrackingJobLocked(jobStatus, toCancel);
                    uploadScheduleBattIdleJob(jobStatus);
                    if (isReadyToBeExecutedLocked(jobStatus)) {
                        this.mJobPackageTracker.notePending(jobStatus);
                        addOrderedItem(this.mPendingJobs, jobStatus, mEnqueueTimeComparator);
                        maybeRunPendingJobsLocked();
                    }
                } else {
                    Slog.w(TAG, "Too many jobs for uid " + uId);
                    throw new IllegalStateException("Apps may not schedule more than 100 distinct jobs");
                }
            }
            toCancel.enqueueWorkLocked(ActivityManager.getService(), work);
            return 1;
        }
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
        String type = "";
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
        if (context == null) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        return packageManager != null ? packageManager.isClosedSuperFirewall() : false;
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
            Slog.i(TAG, "" + e);
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
        synchronized (this.mLock) {
            List<JobStatus> jobsForUser = this.mJobs.getJobsByUser(userHandle);
            for (int i = 0; i < jobsForUser.size(); i++) {
                cancelJobImplLocked((JobStatus) jobsForUser.get(i), null, "user removed");
            }
        }
    }

    private void cancelJobsForNonExistentUsers() {
        UserManagerInternal umi = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        synchronized (this.mLock) {
            this.mJobs.removeJobsOfNonUsers(umi.getUserIds());
        }
    }

    void cancelJobsForPackageAndUid(String pkgName, int uid, String reason) {
        if ("android".equals(pkgName)) {
            Slog.wtfStack(TAG, "Can't cancel all jobs for system package");
            return;
        }
        synchronized (this.mLock) {
            List<JobStatus> jobsForUid = this.mJobs.getJobsByUid(uid);
            for (int i = jobsForUid.size() - 1; i >= 0; i--) {
                JobStatus job = (JobStatus) jobsForUid.get(i);
                if (job.getSourcePackageName().equals(pkgName)) {
                    cancelJobImplLocked(job, null, reason);
                }
            }
        }
    }

    public void cancelJobsForUid(int uid, String reason) {
        if (uid == 1000) {
            Slog.wtfStack(TAG, "Can't cancel all jobs for system uid");
            return;
        }
        synchronized (this.mLock) {
            List<JobStatus> jobsForUid = this.mJobs.getJobsByUid(uid);
            for (int i = 0; i < jobsForUid.size(); i++) {
                cancelJobImplLocked((JobStatus) jobsForUid.get(i), null, reason);
            }
        }
    }

    public void cancelJob(int uid, int jobId) {
        synchronized (this.mLock) {
            JobStatus toCancel = this.mJobs.getJobByUidAndJobId(uid, jobId);
            if (toCancel != null) {
                cancelJobImplLocked(toCancel, null, "cancel() called by app");
            }
        }
    }

    private void cancelJobImplLocked(JobStatus cancelled, JobStatus incomingJob, String reason) {
        if (DEBUG) {
            Slog.d(TAG, "CANCEL: " + cancelled.toShortString());
        }
        cancelled.unprepareLocked(ActivityManager.getService());
        stopTrackingJobLocked(cancelled, incomingJob, true);
        if (this.mPendingJobs.remove(cancelled)) {
            this.mJobPackageTracker.noteNonpending(cancelled);
        }
        stopJobOnServiceContextLocked(cancelled, 0, reason);
        reportActiveLocked();
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
                    JobStatus executing = jsc.getRunningJobLocked();
                    if (executing != null && (executing.getFlags() & 1) == 0) {
                        jsc.cancelExecutingJobLocked(4, "cancelled due to doze");
                    }
                }
            } else if (this.mReadyToRock) {
                if (!(this.mLocalDeviceIdleController == null || this.mReportedActive)) {
                    this.mReportedActive = true;
                    this.mLocalDeviceIdleController.setJobsActive(true);
                }
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    void reportActiveLocked() {
        boolean active = this.mPendingJobs.size() > 0;
        if (this.mPendingJobs.size() <= 0) {
            for (int i = 0; i < this.mActiveServices.size(); i++) {
                JobStatus job = ((JobServiceContext) this.mActiveServices.get(i)).getRunningJobLocked();
                if (job != null && (job.getJob().getFlags() & 1) == 0 && (job.dozeWhitelisted ^ 1) != 0) {
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
        this.mHandler = new JobHandler(context.getMainLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mJobSchedulerStub = new JobSchedulerStub();
        this.mJobs = JobStore.initAndGet(this);
        this.mControllers = new ArrayList();
        this.mControllers.add(ConnectivityController.get(this));
        this.mControllers.add(TimeController.get(this));
        this.mControllers.add(IdleController.get(this));
        this.mBatteryController = BatteryController.get(this);
        this.mControllers.add(this.mBatteryController);
        this.mStorageController = StorageController.get(this);
        this.mControllers.add(this.mStorageController);
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
        if (!this.mJobs.jobTimesInflatedValid()) {
            Slog.w(TAG, "!!! RTC not yet good; tracking time updates for job scheduling");
            context.registerReceiver(this.mTimeSetReceiver, new IntentFilter("android.intent.action.TIME_SET"));
        }
    }

    /* renamed from: lambda$-com_android_server_job_JobSchedulerService_52414 */
    /* synthetic */ void m105lambda$-com_android_server_job_JobSchedulerService_52414() {
        ArrayList<JobStatus> toRemove = new ArrayList();
        ArrayList<JobStatus> toAdd = new ArrayList();
        synchronized (this.mLock) {
            getJobStore().getRtcCorrectedJobsLocked(toAdd, toRemove);
            int N = toAdd.size();
            for (int i = 0; i < N; i++) {
                JobStatus oldJob = (JobStatus) toRemove.get(i);
                JobStatus newJob = (JobStatus) toAdd.get(i);
                if (DEBUG) {
                    Slog.v(TAG, "  replacing " + oldJob + " with " + newJob);
                }
                cancelJobImplLocked(oldJob, newJob, "deferred rtc calculation");
            }
        }
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
                ActivityManager.getService().registerUidObserver(this.mUidObserver, 7, -1, null);
            } catch (RemoteException e) {
            }
            cancelJobsForNonExistentUsers();
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
            this.mPowerManagerInternal.registerColorOsLowPowerModeObserver(new LowPowerModeListener() {
                public int getServiceType() {
                    return 0;
                }

                public void onLowPowerModeChanged(PowerSaveState state) {
                    JobSchedulerService.this.onLowPowerModeChangedInternal(state.batterySaverEnabled);
                }
            });
            this.mColorOsLowPowerModeEnabled.set(this.mPowerManagerInternal.getColorOsLowPowerModeEnabled());
            OppoAppSwitchManager.getInstance().setActivityChangedListener(this.mActivityChangedListener);
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

    private void startTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if (!jobStatus.isPreparedLocked()) {
            Slog.wtf(TAG, "Not yet prepared when started tracking: " + jobStatus);
        }
        jobStatus.enqueueTime = SystemClock.elapsedRealtime();
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

    private boolean stopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean writeBack) {
        jobStatus.stopTrackingJobLocked(ActivityManager.getService(), incomingJob);
        boolean removed = this.mJobs.remove(jobStatus, writeBack);
        if (removed && this.mReadyToRock) {
            for (int i = 0; i < this.mControllers.size(); i++) {
                ((StateController) this.mControllers.get(i)).maybeStopTrackingJobLocked(jobStatus, incomingJob, false);
            }
        }
        return removed;
    }

    private boolean stopJobOnServiceContextLocked(JobStatus job, int reason, String debugReason) {
        int i = 0;
        while (i < this.mActiveServices.size()) {
            JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus executing = jsc.getRunningJobLocked();
            if (executing == null || !executing.matches(job.getUid(), job.getJobId())) {
                i++;
            } else {
                jsc.cancelExecutingJobLocked(reason, debugReason);
                return true;
            }
        }
        return false;
    }

    private boolean updateParamterOnServiceContextLocked(JobStatus job) {
        int i = 0;
        while (i < this.mActiveServices.size()) {
            JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus executing = jsc.getRunningJobLocked();
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
                if (jsc.getRunningJobLocked() != null) {
                    JobStatus js = jsc.getRunningJobLocked();
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
            boolean foreStopped = job.hasProtectForeConstraint() ? job.isConstraintSatisfied(2048) ^ 1 : false;
            boolean cpuStopped = job.hasCpuConstraint() ? job.isConstraintSatisfied(4096) ^ 1 : false;
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
            JobStatus running = ((JobServiceContext) this.mActiveServices.get(i)).getRunningJobLocked();
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

    private JobStatus getRescheduleJobForFailureLocked(JobStatus failureToReschedule) {
        long backoff;
        long delayMillis;
        long elapsedNowMillis = SystemClock.elapsedRealtime();
        JobInfo job = failureToReschedule.getJob();
        long initialBackoffMillis = job.getInitialBackoffMillis();
        int backoffAttempts = failureToReschedule.getNumFailures() + 1;
        if (failureToReschedule.hasWorkLocked()) {
            if (backoffAttempts > this.mConstants.MAX_WORK_RESCHEDULE_COUNT) {
                Slog.w(TAG, "Not rescheduling " + failureToReschedule + ": attempt #" + backoffAttempts + " > work limit " + this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT);
                return null;
            }
        } else if (backoffAttempts > this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT) {
            Slog.w(TAG, "Not rescheduling " + failureToReschedule + ": attempt #" + backoffAttempts + " > std limit " + this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT);
            return null;
        }
        switch (job.getBackoffPolicy()) {
            case 0:
                backoff = initialBackoffMillis;
                if (initialBackoffMillis < this.mConstants.MIN_LINEAR_BACKOFF_TIME) {
                    backoff = this.mConstants.MIN_LINEAR_BACKOFF_TIME;
                }
                delayMillis = backoff * ((long) backoffAttempts);
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
        backoff = initialBackoffMillis;
        if (initialBackoffMillis < this.mConstants.MIN_EXP_BACKOFF_TIME) {
            backoff = this.mConstants.MIN_EXP_BACKOFF_TIME;
        }
        delayMillis = (long) Math.scalb((float) backoff, backoffAttempts - 1);
        delayMillis = Math.min(delayMillis, 18000000);
        if (job.getOppoJob()) {
            delayMillis = Math.min(delayMillis, LocationFudger.FASTEST_INTERVAL_MS);
            Slog.i(TAG, "getRescheduleJobForFailureLocked set job delay " + delayMillis);
        }
        JobStatus newJob = new JobStatus(failureToReschedule, elapsedNowMillis + delayMillis, JobStatus.NO_LATEST_RUNTIME, backoffAttempts, failureToReschedule.getLastSuccessfulRunTime(), System.currentTimeMillis());
        for (int ic = 0; ic < this.mControllers.size(); ic++) {
            ((StateController) this.mControllers.get(ic)).rescheduleForFailureLocked(newJob, failureToReschedule);
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
        return new JobStatus(periodicToReschedule, newEarliestRunTimeElapsed, newLatestRuntimeElapsed, 0, System.currentTimeMillis(), periodicToReschedule.getLastFailedRunTime());
    }

    public void onJobCompletedLocked(JobStatus jobStatus, boolean needsReschedule) {
        if (DEBUG) {
            Slog.d(TAG, "Completed " + jobStatus + ", reschedule=" + needsReschedule);
        }
        JobStatus rescheduledJob = needsReschedule ? getRescheduleJobForFailureLocked(jobStatus) : null;
        if (stopTrackingJobLocked(jobStatus, rescheduledJob, jobStatus.getJob().isPeriodic() ^ 1)) {
            if (rescheduledJob != null) {
                try {
                    rescheduledJob.prepareLocked(ActivityManager.getService());
                } catch (SecurityException e) {
                    Slog.w(TAG, "Unable to regrant job permissions for " + rescheduledJob);
                }
                startTrackingJobLocked(rescheduledJob, jobStatus);
            } else if (jobStatus.getJob().isPeriodic()) {
                JobStatus rescheduledPeriodic = getRescheduleJobForPeriodic(jobStatus);
                try {
                    rescheduledPeriodic.prepareLocked(ActivityManager.getService());
                } catch (SecurityException e2) {
                    Slog.w(TAG, "Unable to regrant job permissions for " + rescheduledPeriodic);
                }
                startTrackingJobLocked(rescheduledPeriodic, jobStatus);
            }
            jobStatus.unprepareLocked(ActivityManager.getService());
            reportActiveLocked();
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

    private void stopNonReadyActiveJobsLocked() {
        for (int i = 0; i < this.mActiveServices.size(); i++) {
            JobServiceContext serviceContext = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus running = serviceContext.getRunningJobLocked();
            if (!(running == null || (running.isReady() ^ 1) == 0)) {
                serviceContext.cancelExecutingJobLocked(1, "cancelled due to unsatisfied constraints");
            }
        }
    }

    private void queueReadyJobsForExecutionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "queuing all ready jobs for execution:");
        }
        noteJobsNonpending(this.mPendingJobs);
        this.mPendingJobs.clear();
        stopNonReadyActiveJobsLocked();
        this.mJobs.forEachJob(this.mReadyQueueFunctor);
        this.mReadyQueueFunctor.postProcess();
        if (DEBUG) {
            int queuedJobs = this.mPendingJobs.size();
            if (queuedJobs == 0) {
                Slog.d(TAG, "No jobs pending.");
            } else {
                Slog.d(TAG, queuedJobs + " jobs queued.");
            }
        }
    }

    private void maybeQueueReadyJobsForExecutionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "Maybe queuing ready jobs...");
        }
        noteJobsNonpending(this.mPendingJobs);
        this.mPendingJobs.clear();
        stopNonReadyActiveJobsLocked();
        this.mJobs.forEachJob(this.mMaybeQueueFunctor);
        this.mMaybeQueueFunctor.postProcess();
    }

    private boolean isReadyToBeExecutedLocked(JobStatus job) {
        boolean jobReady = job.isReady();
        if (DEBUG) {
            Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " ready=" + jobReady);
        }
        if (!jobReady) {
            return false;
        }
        boolean jobExists = this.mJobs.containsJob(job);
        int userId = job.getUserId();
        boolean userStarted = ArrayUtils.contains(this.mStartedUsers, userId);
        if (DEBUG) {
            Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " exists=" + jobExists + " userStarted=" + userStarted);
        }
        if (!jobExists || (userStarted ^ 1) != 0) {
            return false;
        }
        boolean jobPending = this.mPendingJobs.contains(job);
        boolean jobActive = isCurrentlyActiveLocked(job);
        if (DEBUG) {
            Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " pending=" + jobPending + " active=" + jobActive);
        }
        if (jobPending || jobActive) {
            return false;
        }
        try {
            boolean componentPresent = AppGlobals.getPackageManager().getServiceInfo(job.getServiceComponent(), 268435456, userId) != null;
            if (DEBUG) {
                Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " componentPresent=" + componentPresent);
            }
            boolean satisfied = componentPresent;
            String pkgName = null;
            if (!(job.getJob() == null || job.getJob().getService() == null)) {
                pkgName = job.getJob().getService().getPackageName();
            }
            if (pkgName == null) {
                return satisfied;
            }
            if (satisfied && OppoProcessManagerHelper.isInStrictMode() && OppoProcessManagerHelper.isDelayAppJob(job.getUid(), pkgName)) {
                satisfied = false;
                if (OPPODEBUG) {
                    Slog.d(TAG, "in strict mode. job: " + job);
                }
            }
            if (satisfied && this.mColorOsLowPowerModeEnabled.get() && !job.getJob().isSystemApp() && (pkgName.equals(this.mTopPkg) ^ 1) != 0) {
                satisfied = false;
                if (OPPODEBUG) {
                    Slog.d(TAG, "in coloros power save mode. job: " + job);
                }
            }
            return satisfied;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    private boolean isReadyToUpdateCpuParameter(JobStatus job) {
        if (job.hasCpuConstraint() && isCurrentlyActiveLocked(job) && job.oldCpuLevel.get() != job.cpuLevel.get()) {
            return true;
        }
        return false;
    }

    private void maybeRunPendingJobsLocked() {
        this.mListBattIdleJobStart.clear();
        if (DEBUG) {
            Slog.d(TAG, "pending queue: " + this.mPendingJobs.size() + " jobs.");
        }
        assignJobsToContextsLocked();
        reportActiveLocked();
        rcdBattIdleStart();
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

    /* JADX WARNING: Removed duplicated region for block: B:101:0x00f6 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0157  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void assignJobsToContextsLocked() {
        int memLevel;
        int i;
        if (DEBUG) {
            Slog.d(TAG, printPendingQueue());
        }
        try {
            memLevel = ActivityManager.getService().getMemoryTrimLevel();
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
            JobStatus status = js.getRunningJobLocked();
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
                if (((JobServiceContext) this.mActiveServices.get(i)).getRunningJobLocked() != null) {
                    if (DEBUG) {
                        Slog.d(TAG, "preempting job: " + ((JobServiceContext) this.mActiveServices.get(i)).getRunningJobLocked());
                    }
                    ((JobServiceContext) this.mActiveServices.get(i)).preemptExecutingJobLocked();
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
                    if (pendingJob.hasBattIdleConstraint() && OPPODEBUG) {
                        Slog.d(TAG, "start run battIdle Job: " + pendingJob);
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
            IPackageManager packageManager = AppGlobals.getPackageManager();
            if (userId == -1) {
                userId = 0;
            }
            int uid = packageManager.getPackageUid(pkgName, 0, userId);
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
                    queueReadyJobsForExecutionLocked();
                    maybeRunPendingJobsLocked();
                } else {
                    js.overrideState = 0;
                    return JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS;
                }
            }
        } catch (RemoteException e) {
        }
        return 0;
    }

    int executeTimeoutCommand(PrintWriter pw, String pkgName, int userId, boolean hasJobId, int jobId) {
        if (DEBUG) {
            Slog.v(TAG, "executeTimeoutCommand(): " + pkgName + "/" + userId + " " + jobId);
        }
        synchronized (this.mLock) {
            boolean foundSome = false;
            for (int i = 0; i < this.mActiveServices.size(); i++) {
                JobServiceContext jc = (JobServiceContext) this.mActiveServices.get(i);
                JobStatus js = jc.getRunningJobLocked();
                if (jc.timeoutIfExecutingLocked(pkgName, userId, hasJobId, jobId)) {
                    foundSome = true;
                    pw.print("Timing out: ");
                    js.printUniqueId(pw);
                    pw.print(" ");
                    pw.println(js.getServiceComponent().flattenToShortString());
                }
            }
            if (!foundSome) {
                pw.println("No matching executing jobs found.");
            }
        }
        return 0;
    }

    void setMonitorBattery(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mBatteryController != null) {
                this.mBatteryController.getTracker().setMonitorBatteryLocked(enabled);
            }
        }
    }

    int getBatterySeq() {
        int seq;
        synchronized (this.mLock) {
            seq = this.mBatteryController != null ? this.mBatteryController.getTracker().getSeq() : -1;
        }
        return seq;
    }

    boolean getBatteryCharging() {
        boolean isOnStablePower;
        synchronized (this.mLock) {
            isOnStablePower = this.mBatteryController != null ? this.mBatteryController.getTracker().isOnStablePower() : false;
        }
        return isOnStablePower;
    }

    boolean getBatteryNotLow() {
        boolean isBatteryNotLow;
        synchronized (this.mLock) {
            isBatteryNotLow = this.mBatteryController != null ? this.mBatteryController.getTracker().isBatteryNotLow() : false;
        }
        return isBatteryNotLow;
    }

    int getStorageSeq() {
        int seq;
        synchronized (this.mLock) {
            seq = this.mStorageController != null ? this.mStorageController.getTracker().getSeq() : -1;
        }
        return seq;
    }

    boolean getStorageNotLow() {
        boolean isStorageNotLow;
        synchronized (this.mLock) {
            isStorageNotLow = this.mStorageController != null ? this.mStorageController.getTracker().isStorageNotLow() : false;
        }
        return isStorageNotLow;
    }

    int getJobState(PrintWriter pw, String pkgName, int userId, int jobId) {
        try {
            IPackageManager packageManager = AppGlobals.getPackageManager();
            if (userId == -1) {
                userId = 0;
            }
            int uid = packageManager.getPackageUid(pkgName, 0, userId);
            if (uid < 0) {
                pw.print("unknown(");
                pw.print(pkgName);
                pw.println(")");
                return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            synchronized (this.mLock) {
                JobStatus js = this.mJobs.getJobByUidAndJobId(uid, jobId);
                if (DEBUG) {
                    Slog.d(TAG, "get-job-state " + uid + "/" + jobId + ": " + js);
                }
                if (js == null) {
                    pw.print("unknown(");
                    UserHandle.formatUid(pw, uid);
                    pw.print("/jid");
                    pw.print(jobId);
                    pw.println(")");
                    return JobSchedulerShellCommand.CMD_ERR_NO_JOB;
                }
                boolean printed = false;
                if (this.mPendingJobs.contains(js)) {
                    pw.print("pending");
                    printed = true;
                }
                if (isCurrentlyActiveLocked(js)) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("active");
                }
                if (!ArrayUtils.contains(this.mStartedUsers, js.getUserId())) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("user-stopped");
                }
                if (this.mBackingUpUids.indexOfKey(js.getSourceUid()) >= 0) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("backing-up");
                }
                boolean componentPresent = false;
                try {
                    componentPresent = AppGlobals.getPackageManager().getServiceInfo(js.getServiceComponent(), 268435456, js.getUserId()) != null;
                } catch (RemoteException e) {
                }
                if (!componentPresent) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("no-component");
                }
                if (js.isReady()) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("ready");
                }
                if (!printed) {
                    pw.print("waiting");
                }
                pw.println();
            }
        } catch (RemoteException e2) {
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

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0068  */
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
                            filterUid = getContext().getPackageManager().getPackageUid(pkg, DumpState.DUMP_CHANGES);
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
        long nowElapsed = SystemClock.elapsedRealtime();
        long nowUptime = SystemClock.uptimeMillis();
        synchronized (this.mLock) {
            JobStatus job;
            int i;
            int uid;
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
                        job2.dump(pw, "    ", true, nowElapsed);
                        pw.print("    Ready: ");
                        pw.print(isReadyToBeExecutedLocked(job2));
                        pw.print(" (job=");
                        pw.print(job2.isReady());
                        pw.print(" user=");
                        pw.print(ArrayUtils.contains(this.mStartedUsers, job2.getUserId()));
                        pw.print(" !pending=");
                        pw.print(this.mPendingJobs.contains(job2) ^ 1);
                        pw.print(" !active=");
                        pw.print(isCurrentlyActiveLocked(job2) ^ 1);
                        pw.print(" !backingup=");
                        pw.print(this.mBackingUpUids.indexOfKey(job2.getSourceUid()) < 0);
                        pw.print(" comp=");
                        boolean componentPresent = false;
                        try {
                            componentPresent = AppGlobals.getPackageManager().getServiceInfo(job2.getServiceComponent(), 268435456, job2.getUserId()) != null;
                        } catch (RemoteException e2) {
                        }
                        pw.print(componentPresent);
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
                uid = this.mUidPriorityOverride.keyAt(i);
                if (filterUidFinal == -1 || filterUidFinal == UserHandle.getAppId(uid)) {
                    pw.print("  ");
                    pw.print(UserHandle.formatUid(uid));
                    pw.print(": ");
                    pw.println(this.mUidPriorityOverride.valueAt(i));
                }
            }
            if (this.mBackingUpUids.size() > 0) {
                pw.println();
                pw.println("Backing up uids:");
                boolean first = true;
                for (i = 0; i < this.mBackingUpUids.size(); i++) {
                    uid = this.mBackingUpUids.keyAt(i);
                    if (filterUidFinal == -1 || filterUidFinal == UserHandle.getAppId(uid)) {
                        if (first) {
                            pw.print("  ");
                            first = false;
                        } else {
                            pw.print(", ");
                        }
                        pw.print(UserHandle.formatUid(uid));
                    }
                }
                pw.println();
            }
            pw.println();
            this.mJobPackageTracker.dump(pw, "", filterUidFinal);
            pw.println();
            if (this.mJobPackageTracker.dumpHistory(pw, "", filterUidFinal)) {
                pw.println();
            }
            pw.println("Pending queue:");
            for (i = 0; i < this.mPendingJobs.size(); i++) {
                job2 = (JobStatus) this.mPendingJobs.get(i);
                pw.print("  Pending #");
                pw.print(i);
                pw.print(": ");
                pw.println(job2.toShortString());
                job2.dump(pw, "    ", false, nowElapsed);
                priority = evaluateJobPriorityLocked(job2);
                if (priority != 0) {
                    pw.print("    Evaluated priority: ");
                    pw.println(priority);
                }
                pw.print("    Tag: ");
                pw.println(job2.getTag());
                pw.print("    Enq: ");
                TimeUtils.formatDuration(job2.madePending - nowUptime, pw);
                pw.println();
            }
            pw.println();
            pw.println("Active jobs:");
            for (i = 0; i < this.mActiveServices.size(); i++) {
                JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                pw.print("  Slot #");
                pw.print(i);
                pw.print(": ");
                job2 = jsc.getRunningJobLocked();
                if (job2 != null) {
                    pw.println(job2.toShortString());
                    pw.print("    Running for: ");
                    TimeUtils.formatDuration(nowElapsed - jsc.getExecutionStartTimeElapsed(), pw);
                    pw.print(", timeout at: ");
                    TimeUtils.formatDuration(jsc.getTimeoutElapsed() - nowElapsed, pw);
                    pw.println();
                    job2.dump(pw, "    ", false, nowElapsed);
                    priority = evaluateJobPriorityLocked(jsc.getRunningJobLocked());
                    if (priority != 0) {
                        pw.print("    Evaluated priority: ");
                        pw.println(priority);
                    }
                    pw.print("    Active at ");
                    TimeUtils.formatDuration(job2.madeActive - nowUptime, pw);
                    pw.print(", pending for ");
                    TimeUtils.formatDuration(job2.madeActive - job2.madePending, pw);
                    pw.println();
                } else if (jsc.mStoppedReason != null) {
                    pw.print("inactive since ");
                    TimeUtils.formatDuration(jsc.mStoppedTime, nowElapsed, pw);
                    pw.print(", stopped because: ");
                    pw.println(jsc.mStoppedReason);
                } else {
                    pw.println("inactive");
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
            pw.println();
            pw.print("PersistStats: ");
            pw.println(this.mJobs.getPersistStats());
        }
        pw.println();
    }

    private void dynamicLogConfig(PrintWriter pw, String[] args) {
        if (args != null && args.length != 0 && pw != null) {
            String type = args[0];
            if (type != null && (type.equals("log") ^ 1) == 0) {
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
        if (jobInfo != null && (jobInfo.isRequireBattIdle() ^ 1) == 0) {
            String pkgName = getJobPkgName(job);
            if (OPPODEBUG) {
                Slog.d(TAG, "ScheduleBattIdleJob: pkgName=" + pkgName + ", job: " + job);
            }
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
        if (str == null || ("".equals(str) ^ 1) == 0) {
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
