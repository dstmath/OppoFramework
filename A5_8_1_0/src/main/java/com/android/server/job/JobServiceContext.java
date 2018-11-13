package com.android.server.job;

import android.app.ActivityManager;
import android.app.job.IJobCallback.Stub;
import android.app.job.IJobService;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.server.coloros.OppoListManager;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.job.controllers.JobStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import oppo.util.OppoStatistics;

public final class JobServiceContext implements ServiceConnection {
    private static final boolean DCS_ENABLED = false;
    private static final boolean DEBUG = JobSchedulerService.DEBUG;
    private static final long EXECUTING_TIMESLICE_MILLIS = 600000;
    private static final int MSG_TIMEOUT = 0;
    private static final int MSG_UPDATE_PARAMETER = 101;
    public static final int NO_PREFERRED_UID = -1;
    private static final long OP_BIND_TIMEOUT_MILLIS = 18000;
    private static final long OP_TIMEOUT_MILLIS = 8000;
    private static final String TAG = "JobServiceContext";
    static final int VERB_BINDING = 0;
    static final int VERB_EXECUTING = 2;
    static final int VERB_FINISHED = 4;
    static final int VERB_STARTING = 1;
    static final int VERB_STOPPING = 3;
    private static final String[] VERB_STRINGS = new String[]{"VERB_BINDING", "VERB_STARTING", "VERB_EXECUTING", "VERB_STOPPING", "VERB_FINISHED"};
    @GuardedBy("mLock")
    private boolean mAvailable;
    BattIdleJobStartRcd mBattIdleJobStartRcd;
    private BatteryManagerInternal mBatteryManagerInternal;
    private final IBatteryStats mBatteryStats;
    private final Handler mCallbackHandler;
    private boolean mCancelled;
    private final JobCompletedListener mCompletedListener;
    private final Context mContext;
    private long mExecutionStartTimeElapsed;
    private final JobPackageTracker mJobPackageTracker;
    private final Object mLock;
    private JobParameters mParams;
    private int mPreferredUid;
    private JobCallback mRunningCallback;
    private JobStatus mRunningJob;
    public String mStoppedReason;
    public long mStoppedTime;
    private long mTimeoutElapsed;
    int mVerb;
    private WakeLock mWakeLock;
    IJobService service;

    class BattIdleJobStartRcd {
        String mBattIdleJob;
        int mBattTemperature;
        HashMap<String, Integer> mCpuThermal;
        boolean mIsBattIdleSatisfied;
        ArrayList<String> mListBattIdleJob;
        ArrayList<String> mListOtherJob;

        BattIdleJobStartRcd(ArrayList<String> listBattIdleJob, ArrayList<String> listOtherJob, HashMap<String, Integer> cpuThermal, String battIdleJob, int battTemperature, boolean isBattIdleSatisfied) {
            this.mListBattIdleJob = listBattIdleJob;
            this.mListOtherJob = listOtherJob;
            this.mCpuThermal = cpuThermal;
            this.mBattIdleJob = battIdleJob;
            this.mBattTemperature = battTemperature;
            this.mIsBattIdleSatisfied = isBattIdleSatisfied;
        }
    }

    final class JobCallback extends Stub {
        public String mStoppedReason;
        public long mStoppedTime;

        JobCallback() {
        }

        public void acknowledgeStartMessage(int jobId, boolean ongoing) {
            JobServiceContext.this.doAcknowledgeStartMessage(this, jobId, ongoing);
        }

        public void acknowledgeStopMessage(int jobId, boolean reschedule) {
            JobServiceContext.this.doAcknowledgeStopMessage(this, jobId, reschedule);
        }

        public JobWorkItem dequeueWork(int jobId) {
            return JobServiceContext.this.doDequeueWork(this, jobId);
        }

        public boolean completeWork(int jobId, int workId) {
            return JobServiceContext.this.doCompleteWork(this, jobId, workId);
        }

        public void jobFinished(int jobId, boolean reschedule) {
            JobServiceContext.this.doJobFinished(this, jobId, reschedule);
        }
    }

    private class JobServiceHandler extends Handler {
        JobServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    synchronized (JobServiceContext.this.mLock) {
                        if (message.obj == JobServiceContext.this.mRunningCallback) {
                            JobServiceContext.this.handleOpTimeoutLocked();
                        } else {
                            JobCallback jc = message.obj;
                            StringBuilder sb = new StringBuilder(128);
                            sb.append("Ignoring timeout of no longer active job");
                            if (jc.mStoppedReason != null) {
                                sb.append(", stopped ");
                                TimeUtils.formatDuration(SystemClock.elapsedRealtime() - jc.mStoppedTime, sb);
                                sb.append(" because: ");
                                sb.append(jc.mStoppedReason);
                            }
                            Slog.w(JobServiceContext.TAG, sb.toString());
                        }
                    }
                    return;
                default:
                    Slog.e(JobServiceContext.TAG, "Unrecognised message: " + message);
                    return;
            }
        }
    }

    JobServiceContext(JobSchedulerService service, IBatteryStats batteryStats, JobPackageTracker tracker, Looper looper) {
        this(service.getContext(), service.getLock(), batteryStats, tracker, service, looper);
        this.mBatteryManagerInternal = service.mBatteryManagerInternal;
    }

    JobServiceContext(Context context, Object lock, IBatteryStats batteryStats, JobPackageTracker tracker, JobCompletedListener completedListener, Looper looper) {
        this.mContext = context;
        this.mLock = lock;
        this.mBatteryStats = batteryStats;
        this.mJobPackageTracker = tracker;
        this.mCallbackHandler = new JobServiceHandler(looper);
        this.mCompletedListener = completedListener;
        this.mAvailable = true;
        this.mVerb = 4;
        this.mPreferredUid = -1;
    }

    boolean executeRunnableJob(JobStatus job) {
        synchronized (this.mLock) {
            if (this.mAvailable) {
                this.mPreferredUid = -1;
                this.mRunningJob = job;
                this.mRunningCallback = new JobCallback();
                boolean isDeadlineExpired = job.hasDeadlineConstraint() ? job.getLatestRunTimeElapsed() < SystemClock.elapsedRealtime() : false;
                Uri[] triggeredUris = null;
                if (job.changedUris != null) {
                    triggeredUris = new Uri[job.changedUris.size()];
                    job.changedUris.toArray(triggeredUris);
                }
                String[] triggeredAuthorities = null;
                if (job.changedAuthorities != null) {
                    triggeredAuthorities = new String[job.changedAuthorities.size()];
                    job.changedAuthorities.toArray(triggeredAuthorities);
                }
                JobInfo ji = job.getJob();
                this.mParams = new JobParameters(this.mRunningCallback, job.getJobId(), ji.getExtras(), ji.getTransientExtras(), ji.getClipData(), ji.getClipGrantFlags(), isDeadlineExpired, triggeredUris, triggeredAuthorities);
                this.mExecutionStartTimeElapsed = SystemClock.elapsedRealtime();
                if (this.mParams != null) {
                    this.mParams.setOppoExtraStr(job.getOppoExtraStr());
                }
                job.clearPersistedUtcTimes();
                this.mVerb = 0;
                scheduleOpTimeOutLocked();
                Intent intent = new Intent().setComponent(job.getServiceComponent());
                intent.putExtra("BINDSERVICE_FROM_JOB", true);
                if (this.mContext.bindServiceAsUser(intent, this, 5, new UserHandle(job.getUserId()))) {
                    try {
                        this.mBatteryStats.noteJobStart(job.getBatteryName(), job.getSourceUid());
                    } catch (RemoteException e) {
                    }
                    this.mJobPackageTracker.noteActive(job);
                    this.mAvailable = false;
                    this.mStoppedReason = null;
                    this.mStoppedTime = 0;
                    return true;
                }
                if (DEBUG) {
                    Slog.d(TAG, job.getServiceComponent().getShortClassName() + " unavailable.");
                }
                this.mRunningJob = null;
                this.mRunningCallback = null;
                this.mParams = null;
                this.mExecutionStartTimeElapsed = 0;
                this.mVerb = 4;
                removeOpTimeOutLocked();
                return false;
            }
            Slog.e(TAG, "Starting new runnable but context is unavailable > Error.");
            return false;
        }
    }

    private void uploadJobExecute(JobStatus job, boolean isDeadlineExpired) {
        JobInfo jobInfo = job.getJob();
        if (jobInfo != null && jobInfo.getService() != null && jobInfo.getService().getPackageName() != null) {
            Map<String, String> eventMap = new HashMap();
            eventMap.put("pkgname", jobInfo.getService().getPackageName());
            eventMap.put("jobId", String.valueOf(job.getJobId()));
            if (!(jobInfo.getService() == null || jobInfo.getService().flattenToShortString() == null)) {
                eventMap.put("componentName", jobInfo.getService().flattenToShortString());
            }
            if (jobInfo.isPeriodic()) {
                if (0 != jobInfo.getIntervalMillis()) {
                    eventMap.put("periodic", String.valueOf(jobInfo.getIntervalMillis() / 1000));
                }
            } else if (0 != jobInfo.getMaxExecutionDelayMillis()) {
                eventMap.put("deadline", String.valueOf(jobInfo.getMaxExecutionDelayMillis() / 1000));
            }
            if (jobInfo.isRequireBattIdle()) {
                eventMap.put("battIdle", "true");
            }
            if (jobInfo.isRequireCharging()) {
                eventMap.put("charging", "true");
            }
            if (jobInfo.getNetworkType() != 0) {
                eventMap.put("connectivity", "true");
            }
            if (jobInfo.isRequireDeviceIdle()) {
                eventMap.put("idle", "true");
            }
            if (isDeadlineExpired) {
                eventMap.put("isDeadlineExpired", "true");
            }
            OppoStatistics.onCommon(this.mContext, "20120008", "jobscheduler_execute", eventMap, false);
        }
    }

    JobStatus getRunningJobLocked() {
        return this.mRunningJob;
    }

    private String getRunningJobNameLocked() {
        return this.mRunningJob != null ? this.mRunningJob.toShortString() : "<null>";
    }

    void cancelExecutingJobLocked(int reason, String debugReason) {
        doCancelLocked(reason, debugReason);
    }

    boolean updateExecutingParameter(int level) {
        synchronized (this.mLock) {
            if (this.mVerb != 2) {
                Slog.e(TAG, "can only update running parameters");
                return false;
            } else if (this.mParams == null || this.mRunningJob == null) {
                Slog.e(TAG, "updateRunningParameters mParams or mRunningJob is null.");
                return false;
            } else {
                this.mParams.setCpuLevel(level);
                handleUpdateParamH();
                return true;
            }
        }
    }

    void preemptExecutingJobLocked() {
        doCancelLocked(2, "cancelled due to preemption");
    }

    int getPreferredUid() {
        return this.mPreferredUid;
    }

    void clearPreferredUid() {
        this.mPreferredUid = -1;
    }

    long getExecutionStartTimeElapsed() {
        return this.mExecutionStartTimeElapsed;
    }

    long getTimeoutElapsed() {
        return this.mTimeoutElapsed;
    }

    boolean timeoutIfExecutingLocked(String pkgName, int userId, boolean matchJobId, int jobId) {
        JobStatus executing = getRunningJobLocked();
        if (executing == null || ((userId != -1 && userId != executing.getUserId()) || ((pkgName != null && !pkgName.equals(executing.getSourcePackageName())) || ((matchJobId && jobId != executing.getJobId()) || this.mVerb != 2)))) {
            return false;
        }
        this.mParams.setStopReason(3);
        sendStopMessageLocked("force timeout from shell");
        return true;
    }

    void doJobFinished(JobCallback cb, int jobId, boolean reschedule) {
        doCallback(cb, reschedule, "app called jobFinished");
    }

    void doAcknowledgeStopMessage(JobCallback cb, int jobId, boolean reschedule) {
        doCallback(cb, reschedule, null);
    }

    void doAcknowledgeStartMessage(JobCallback cb, int jobId, boolean ongoing) {
        doCallback(cb, ongoing, "finished start");
    }

    JobWorkItem doDequeueWork(JobCallback cb, int jobId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                assertCallerLocked(cb);
                if (this.mVerb != 3 && this.mVerb != 4) {
                    JobWorkItem work = this.mRunningJob.dequeueWorkLocked();
                    if (work == null && (this.mRunningJob.hasExecutingWorkLocked() ^ 1) != 0) {
                        doCallbackLocked(false, "last work dequeued");
                    }
                    Binder.restoreCallingIdentity(ident);
                    return work;
                }
            }
            return null;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean doCompleteWork(JobCallback cb, int jobId, int workId) {
        long ident = Binder.clearCallingIdentity();
        try {
            boolean completeWorkLocked;
            synchronized (this.mLock) {
                assertCallerLocked(cb);
                completeWorkLocked = this.mRunningJob.completeWorkLocked(ActivityManager.getService(), workId);
            }
            return completeWorkLocked;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mLock) {
            JobStatus runningJob = this.mRunningJob;
            if (runningJob == null || (name.equals(runningJob.getServiceComponent()) ^ 1) != 0) {
                closeAndCleanupJobLocked(true, "connected for different component");
                return;
            }
            this.service = IJobService.Stub.asInterface(service);
            WakeLock wl = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, runningJob.getTag());
            wl.setWorkSource(new WorkSource(runningJob.getSourceUid()));
            wl.setReferenceCounted(false);
            wl.acquire();
            if (this.mWakeLock != null) {
                Slog.w(TAG, "Bound new job " + runningJob + " but live wakelock " + this.mWakeLock + " tag=" + this.mWakeLock.getTag());
                this.mWakeLock.release();
            }
            this.mWakeLock = wl;
            doServiceBoundLocked();
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mLock) {
            closeAndCleanupJobLocked(true, "unexpectedly disconnected");
        }
    }

    private boolean verifyCallerLocked(JobCallback cb) {
        if (this.mRunningCallback == cb) {
            return true;
        }
        if (DEBUG) {
            Slog.d(TAG, "Stale callback received, ignoring.");
        }
        return false;
    }

    private void assertCallerLocked(JobCallback cb) {
        if (!verifyCallerLocked(cb)) {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Caller no longer running");
            if (cb.mStoppedReason != null) {
                sb.append(", last stopped ");
                TimeUtils.formatDuration(SystemClock.elapsedRealtime() - cb.mStoppedTime, sb);
                sb.append(" because: ");
                sb.append(cb.mStoppedReason);
            }
            throw new SecurityException(sb.toString());
        }
    }

    void doServiceBoundLocked() {
        removeOpTimeOutLocked();
        handleServiceBoundLocked();
    }

    void doCallback(JobCallback cb, boolean reschedule, String reason) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                if (verifyCallerLocked(cb)) {
                    doCallbackLocked(reschedule, reason);
                    Binder.restoreCallingIdentity(ident);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    void doCallbackLocked(boolean reschedule, String reason) {
        if (DEBUG) {
            Slog.d(TAG, "doCallback of : " + this.mRunningJob + " v:" + VERB_STRINGS[this.mVerb]);
        }
        removeOpTimeOutLocked();
        if (this.mVerb == 1) {
            handleStartedLocked(reschedule);
        } else if (this.mVerb == 2 || this.mVerb == 3) {
            handleFinishedLocked(reschedule, reason);
        } else if (DEBUG) {
            Slog.d(TAG, "Unrecognised callback: " + this.mRunningJob);
        }
    }

    void doCancelLocked(int arg1, String debugReason) {
        if (this.mVerb == 4) {
            if (DEBUG) {
                Slog.d(TAG, "Trying to process cancel for torn-down context, ignoring.");
            }
            return;
        }
        this.mParams.setStopReason(arg1);
        if (arg1 == 2) {
            int uid;
            if (this.mRunningJob != null) {
                uid = this.mRunningJob.getUid();
            } else {
                uid = -1;
            }
            this.mPreferredUid = uid;
        }
        handleCancelLocked(debugReason);
    }

    private void handleServiceBoundLocked() {
        if (DEBUG) {
            Slog.d(TAG, "handleServiceBound for " + getRunningJobNameLocked());
        }
        if (this.mVerb != 0) {
            Slog.e(TAG, "Sending onStartJob for a job that isn't pending. " + VERB_STRINGS[this.mVerb]);
            closeAndCleanupJobLocked(false, "started job not pending");
        } else if (this.mCancelled) {
            if (DEBUG) {
                Slog.d(TAG, "Job cancelled while waiting for bind to complete. " + this.mRunningJob);
            }
            closeAndCleanupJobLocked(true, "cancelled while waiting for bind");
        } else {
            try {
                this.mVerb = 1;
                scheduleOpTimeOutLocked();
                this.service.startJob(this.mParams);
            } catch (Exception e) {
                Slog.e(TAG, "Error sending onStart message to '" + this.mRunningJob.getServiceComponent().getShortClassName() + "' ", e);
            }
        }
    }

    private void handleStartedLocked(boolean workOngoing) {
        switch (this.mVerb) {
            case 1:
                this.mVerb = 2;
                if (!workOngoing) {
                    handleFinishedLocked(false, "onStartJob returned false");
                    return;
                } else if (this.mCancelled) {
                    if (DEBUG) {
                        Slog.d(TAG, "Job cancelled while waiting for onStartJob to complete.");
                    }
                    handleCancelLocked(null);
                    return;
                } else {
                    scheduleOpTimeOutLocked();
                    return;
                }
            default:
                Slog.e(TAG, "Handling started job but job wasn't starting! Was " + VERB_STRINGS[this.mVerb] + ".");
                return;
        }
    }

    private void handleFinishedLocked(boolean reschedule, String reason) {
        switch (this.mVerb) {
            case 2:
            case 3:
                closeAndCleanupJobLocked(reschedule, reason);
                return;
            default:
                Slog.e(TAG, "Got an execution complete message for a job that wasn't beingexecuted. Was " + VERB_STRINGS[this.mVerb] + ".");
                return;
        }
    }

    private void handleCancelLocked(String reason) {
        if (JobSchedulerService.DEBUG) {
            Slog.d(TAG, "Handling cancel for: " + this.mRunningJob.getJobId() + " " + VERB_STRINGS[this.mVerb]);
        }
        switch (this.mVerb) {
            case 0:
            case 1:
                this.mCancelled = true;
                applyStoppedReasonLocked(reason);
                return;
            case 2:
                sendStopMessageLocked(reason);
                return;
            case 3:
                return;
            default:
                Slog.e(TAG, "Cancelling a job without a valid verb: " + this.mVerb);
                return;
        }
    }

    private void handleOpTimeoutLocked() {
        switch (this.mVerb) {
            case 0:
                Slog.w(TAG, "Time-out while trying to bind " + getRunningJobNameLocked() + ", dropping.");
                closeAndCleanupJobLocked(false, "timed out while binding");
                return;
            case 1:
                Slog.w(TAG, "No response from client for onStartJob " + getRunningJobNameLocked());
                closeAndCleanupJobLocked(false, "timed out while starting");
                return;
            case 2:
                Slog.i(TAG, "Client timed out while executing (no jobFinished received), sending onStop: " + getRunningJobNameLocked());
                this.mParams.setStopReason(3);
                sendStopMessageLocked("timeout while executing");
                return;
            case 3:
                Slog.w(TAG, "No response from client for onStopJob " + getRunningJobNameLocked());
                closeAndCleanupJobLocked(true, "timed out while stopping");
                return;
            default:
                Slog.e(TAG, "Handling timeout for an invalid job state: " + getRunningJobNameLocked() + ", dropping.");
                closeAndCleanupJobLocked(false, "invalid timeout");
                return;
        }
    }

    private void handleUpdateParamH() {
        if (this.mRunningJob == null) {
            if (DEBUG) {
                Slog.d(TAG, "Trying to update param for torn-down context, ignoring.");
            }
            return;
        }
        if (JobSchedulerService.DEBUG) {
            Slog.d(TAG, "Handling update param for: " + this.mRunningJob.getJobId() + " " + VERB_STRINGS[this.mVerb]);
        }
        if (this.mVerb != 2) {
            if (DEBUG) {
                Slog.d(TAG, "Trying to update param for invalid verb, ignoring.");
            }
            return;
        }
        try {
            this.service.updateJobParameters(this.mParams);
        } catch (RemoteException e) {
            Slog.e(TAG, "Error updating parameter to client.", e);
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void uploadJobFinishEvent(JobStatus job, boolean reschedule) {
        if (job != null && job.getServiceComponent() != null && job.getJob() != null && job.getJob().getService() != null && job.isOppoJob()) {
            HashMap<String, String> map = new HashMap();
            map.put("pkgname", job.getServiceComponent().getPackageName());
            map.put("componentName", job.getJob().getService().flattenToShortString());
            map.put("jobId", String.valueOf(job.getJobId()));
            map.put("reschedule", String.valueOf(reschedule));
            map.put("cpuCons", String.valueOf(job.hasCpuConstraint()));
            map.put("foreCons", String.valueOf(job.hasProtectForeConstraint()));
            map.put("foreType", String.valueOf(job.getProtectForeType()));
            OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "job_finish", map, false);
        }
    }

    private void uploadJObTimeOut(int verb) {
        if (this.mRunningJob != null) {
            JobInfo jobInfo = this.mRunningJob.getJob();
            if (jobInfo != null && jobInfo.getService() != null) {
                String pkgname = jobInfo.getService().getPackageName();
                if (pkgname != null) {
                    Object state;
                    switch (verb) {
                        case 0:
                            state = "binding";
                            break;
                        case 1:
                            state = "starting";
                            break;
                        case 2:
                            state = "executing";
                            break;
                        case 3:
                            state = "stoping";
                            break;
                        default:
                            state = null;
                            break;
                    }
                    if (state != null) {
                        Map<String, String> eventMap = new HashMap();
                        eventMap.put("pkgname", pkgname);
                        eventMap.put("jobId", String.valueOf(this.mRunningJob.getJobId()));
                        eventMap.put("state", state);
                        if (jobInfo.getService().flattenToShortString() != null) {
                            eventMap.put("componentName", jobInfo.getService().flattenToShortString());
                        }
                        OppoStatistics.onCommon(this.mContext, "20120008", "jobscheduler_timeout", eventMap, false);
                    }
                }
            }
        }
    }

    private void sendStopMessageLocked(String reason) {
        removeOpTimeOutLocked();
        if (this.mVerb != 2) {
            Slog.e(TAG, "Sending onStopJob for a job that isn't started. " + this.mRunningJob);
            closeAndCleanupJobLocked(false, reason);
            return;
        }
        try {
            applyStoppedReasonLocked(reason);
            this.mVerb = 3;
            scheduleOpTimeOutLocked();
            this.service.stopJob(this.mParams);
        } catch (RemoteException e) {
            Slog.e(TAG, "Error sending onStopJob to client.", e);
            closeAndCleanupJobLocked(true, "host crashed when trying to stop");
        }
    }

    private void closeAndCleanupJobLocked(boolean reschedule, String reason) {
        if (this.mVerb != 4) {
            applyStoppedReasonLocked(reason);
            JobStatus completedJob = this.mRunningJob;
            this.mJobPackageTracker.noteInactive(completedJob, this.mParams.getStopReason());
            try {
                this.mBatteryStats.noteJobFinish(this.mRunningJob.getBatteryName(), this.mRunningJob.getSourceUid(), this.mParams.getStopReason());
            } catch (RemoteException e) {
                Slog.w(TAG, "mBatteryStats.noteJobFinish RemoteException: " + e);
            } catch (NullPointerException e2) {
                Slog.w(TAG, "mBatteryStats.noteJobFinish NullPointerException: " + e2);
            }
            if (this.mWakeLock != null) {
                this.mWakeLock.release();
            }
            try {
                this.mContext.unbindService(this);
            } catch (Exception e3) {
                Slog.w(TAG, "mContext.unbindService fail, exception: " + e3);
            }
            this.mWakeLock = null;
            this.mRunningJob = null;
            this.mRunningCallback = null;
            this.mParams = null;
            this.mVerb = 4;
            this.mCancelled = false;
            this.service = null;
            this.mAvailable = true;
            removeOpTimeOutLocked();
            if (completedJob != null) {
                this.mCompletedListener.onJobCompletedLocked(completedJob, reschedule);
            }
        }
    }

    private void applyStoppedReasonLocked(String reason) {
        if (reason != null && this.mStoppedReason == null) {
            this.mStoppedReason = reason;
            this.mStoppedTime = SystemClock.elapsedRealtime();
            if (this.mRunningCallback != null) {
                this.mRunningCallback.mStoppedReason = this.mStoppedReason;
                this.mRunningCallback.mStoppedTime = this.mStoppedTime;
            }
        }
    }

    private void scheduleOpTimeOutLocked() {
        long timeoutMillis;
        removeOpTimeOutLocked();
        switch (this.mVerb) {
            case 0:
                timeoutMillis = OP_BIND_TIMEOUT_MILLIS;
                break;
            case 2:
                timeoutMillis = 600000;
                break;
            default:
                timeoutMillis = OP_TIMEOUT_MILLIS;
                break;
        }
        Object pkg = null;
        if (!(this.mRunningJob == null || this.mRunningJob.getJob() == null || this.mRunningJob.getJob().getService() == null)) {
            pkg = this.mRunningJob.getJob().getService().getPackageName();
        }
        if (this.mRunningJob != null && pkg != null && this.mVerb == 2 && OppoListManager.getInstance().getJobScheduleTimeoutWhiteList().contains(pkg)) {
            Slog.d(TAG, "scheduleOpTimeOut set oppo job timeout to 2 hour");
            timeoutMillis = 7200000;
        }
        if (DEBUG) {
            Slog.d(TAG, "Scheduling time out for '" + this.mRunningJob.getServiceComponent().getShortClassName() + "' jId: " + this.mParams.getJobId() + ", in " + (timeoutMillis / 1000) + " s");
        }
        this.mCallbackHandler.sendMessageDelayed(this.mCallbackHandler.obtainMessage(0, this.mRunningCallback), timeoutMillis);
        this.mTimeoutElapsed = SystemClock.elapsedRealtime() + timeoutMillis;
    }

    private void removeOpTimeOutLocked() {
        this.mCallbackHandler.removeMessages(0);
    }

    private void uploadBattIdleJob() {
        if (this.mBattIdleJobStartRcd != null) {
            int i;
            int thermalTotal;
            String type;
            int thermal;
            long executeTime = (SystemClock.elapsedRealtime() - this.mExecutionStartTimeElapsed) / 1000;
            HashMap<String, String> eventMap = new HashMap();
            ArrayList<String> listBattIdleJob = this.mBattIdleJobStartRcd.mListBattIdleJob;
            for (i = 0; i < listBattIdleJob.size(); i++) {
                eventMap.put("battIdleRun_" + i, (String) listBattIdleJob.get(i));
            }
            ArrayList<String> listOtherJob = this.mBattIdleJobStartRcd.mListOtherJob;
            for (i = 0; i < listOtherJob.size(); i++) {
                eventMap.put("OtherJobRun_" + i, (String) listOtherJob.get(i));
            }
            eventMap.put("numBattIdleJob", String.valueOf(listBattIdleJob.size()));
            eventMap.put("numOtherJob", String.valueOf(listOtherJob.size()));
            eventMap.put("executeTime", String.valueOf(executeTime));
            eventMap.put("battIdleJob", this.mBattIdleJobStartRcd.mBattIdleJob);
            eventMap.put("battIdleSatisfied", String.valueOf(this.mBattIdleJobStartRcd.mIsBattIdleSatisfied));
            eventMap.put("temperatureStart", String.valueOf(this.mBattIdleJobStartRcd.mBattTemperature / 10));
            int battTemperatureNow = -3000;
            if (this.mBatteryManagerInternal != null) {
                battTemperatureNow = this.mBatteryManagerInternal.getBatteryTemperature();
            }
            eventMap.put("temperatureEnd", String.valueOf(battTemperatureNow / 10));
            HashMap<String, Integer> cpuThermalStart = this.mBattIdleJobStartRcd.mCpuThermal;
            if (!(cpuThermalStart == null || (cpuThermalStart.isEmpty() ^ 1) == 0)) {
                thermalTotal = 0;
                for (Entry<String, Integer> ent : cpuThermalStart.entrySet()) {
                    type = (String) ent.getKey();
                    thermal = ((Integer) ent.getValue()).intValue();
                    thermalTotal += thermal;
                    eventMap.put("cpuTempStart" + type, String.valueOf(thermal / 10));
                }
                if (cpuThermalStart.size() > 0) {
                    eventMap.put("cpuTempAverStart", String.valueOf((thermalTotal / cpuThermalStart.size()) / 10));
                }
            }
            HashMap<String, Integer> cpuThermal = JobSchedulerService.getCpuThermal();
            if (!(cpuThermal == null || (cpuThermal.isEmpty() ^ 1) == 0)) {
                thermalTotal = 0;
                for (Entry<String, Integer> ent2 : cpuThermal.entrySet()) {
                    type = (String) ent2.getKey();
                    thermal = ((Integer) ent2.getValue()).intValue();
                    thermalTotal += thermal;
                    eventMap.put("cpuTempEnd" + type, String.valueOf(thermal / 10));
                }
                if (cpuThermal.size() > 0) {
                    eventMap.put("cpuTempAverEnd", String.valueOf((thermalTotal / cpuThermal.size()) / 10));
                }
            }
            if (JobSchedulerService.OPPODEBUG) {
                Slog.d(TAG, "BattIdleJob: mVerb=" + this.mVerb + ", TemperatureStart=" + this.mBattIdleJobStartRcd.mBattTemperature + ", TemperatureNow=" + battTemperatureNow + ", executeTime=" + executeTime);
            }
            OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "battIdle_job_rcd", eventMap, false);
            this.mBattIdleJobStartRcd = null;
        }
    }

    public void setBattIdleJobStartRcd(ArrayList<String> listBattIdleJob, ArrayList<String> listOtherJob, HashMap<String, Integer> cpuThermal, String battIdleJob, int battTemperature, boolean isBattIdleSatisfied) {
        this.mBattIdleJobStartRcd = new BattIdleJobStartRcd(listBattIdleJob, listOtherJob, cpuThermal, battIdleJob, battTemperature, isBattIdleSatisfied);
    }
}
