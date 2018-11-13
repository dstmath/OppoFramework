package com.android.server.job;

import android.app.job.IJobCallback.Stub;
import android.app.job.IJobService;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
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
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.job.controllers.JobStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
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
public class JobServiceContext extends Stub implements ServiceConnection {
    private static final boolean DCS_ENABLED = false;
    private static final boolean DEBUG = false;
    private static final long EXECUTING_TIMESLICE_MILLIS = 600000;
    private static final int MSG_CALLBACK = 1;
    private static final int MSG_CANCEL = 3;
    private static final int MSG_SERVICE_BOUND = 2;
    private static final int MSG_SHUTDOWN_EXECUTION = 4;
    private static final int MSG_TIMEOUT = 0;
    private static final int MSG_UPDATE_PARAMETER = 101;
    public static final int NO_PREFERRED_UID = -1;
    private static final long OP_TIMEOUT_MILLIS = 8000;
    private static final String TAG = "JobServiceContext";
    static final int VERB_BINDING = 0;
    static final int VERB_EXECUTING = 2;
    static final int VERB_FINISHED = 4;
    static final int VERB_STARTING = 1;
    static final int VERB_STOPPING = 3;
    private static final String[] VERB_STRINGS = null;
    private static final int defaultMaxActiveJobsPerService = 0;
    @GuardedBy("mLock")
    private boolean mAvailable;
    BattIdleJobStartRcd mBattIdleJobStartRcd;
    private BatteryManagerInternal mBatteryManagerInternal;
    private final IBatteryStats mBatteryStats;
    private final Handler mCallbackHandler;
    private AtomicBoolean mCancelled;
    private final JobCompletedListener mCompletedListener;
    private final Context mContext;
    private long mExecutionStartTimeElapsed;
    private final JobPackageTracker mJobPackageTracker;
    private final Object mLock;
    private JobParameters mParams;
    private int mPreferredUid;
    private JobStatus mRunningJob;
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

    private class JobServiceHandler extends Handler {
        JobServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            boolean workOngoing = true;
            switch (message.what) {
                case 0:
                    handleOpTimeoutH();
                    break;
                case 1:
                    if (JobServiceContext.DEBUG) {
                        Slog.d(JobServiceContext.TAG, "MSG_CALLBACK of : " + JobServiceContext.this.mRunningJob + " v:" + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb]);
                    }
                    JobServiceContext.this.removeOpTimeOut();
                    if (JobServiceContext.this.mVerb != 1) {
                        if (JobServiceContext.this.mVerb != 2 && JobServiceContext.this.mVerb != 3) {
                            if (JobServiceContext.DEBUG) {
                                Slog.d(JobServiceContext.TAG, "Unrecognised callback: " + JobServiceContext.this.mRunningJob);
                                break;
                            }
                        }
                        boolean reschedule = message.arg2 == 1;
                        if (JobServiceContext.this.mVerb == 2) {
                            uploadJobFinishEvent(JobServiceContext.this.mRunningJob, reschedule);
                        }
                        handleFinishedH(reschedule);
                        break;
                    }
                    if (message.arg2 != 1) {
                        workOngoing = false;
                    }
                    handleStartedH(workOngoing);
                    break;
                    break;
                case 2:
                    JobServiceContext.this.removeOpTimeOut();
                    handleServiceBoundH();
                    break;
                case 3:
                    if (JobServiceContext.this.mVerb != 4) {
                        JobServiceContext.this.mParams.setStopReason(message.arg1);
                        if (message.arg1 == 2) {
                            int uid;
                            JobServiceContext jobServiceContext = JobServiceContext.this;
                            if (JobServiceContext.this.mRunningJob != null) {
                                uid = JobServiceContext.this.mRunningJob.getUid();
                            } else {
                                uid = -1;
                            }
                            jobServiceContext.mPreferredUid = uid;
                        }
                        handleCancelH();
                        break;
                    }
                    if (JobServiceContext.DEBUG) {
                        Slog.d(JobServiceContext.TAG, "Trying to process cancel for torn-down context, ignoring.");
                    }
                    return;
                case 4:
                    closeAndCleanupJobH(true);
                    break;
                case 101:
                    handleUpdateParamH();
                    break;
                default:
                    Slog.e(JobServiceContext.TAG, "Unrecognised message: " + message);
                    break;
            }
        }

        private void handleServiceBoundH() {
            if (JobServiceContext.DEBUG) {
                Slog.d(JobServiceContext.TAG, "MSG_SERVICE_BOUND for " + JobServiceContext.this.mRunningJob.toShortString());
            }
            if (JobServiceContext.this.mVerb != 0) {
                Slog.e(JobServiceContext.TAG, "Sending onStartJob for a job that isn't pending. " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb]);
                closeAndCleanupJobH(false);
            } else if (JobServiceContext.this.mCancelled.get()) {
                if (JobServiceContext.DEBUG) {
                    Slog.d(JobServiceContext.TAG, "Job cancelled while waiting for bind to complete. " + JobServiceContext.this.mRunningJob);
                }
                closeAndCleanupJobH(true);
            } else {
                try {
                    JobServiceContext.this.mVerb = 1;
                    JobServiceContext.this.scheduleOpTimeOut();
                    JobServiceContext.this.service.startJob(JobServiceContext.this.mParams);
                } catch (RemoteException e) {
                    Slog.e(JobServiceContext.TAG, "Error sending onStart message to '" + JobServiceContext.this.mRunningJob.getServiceComponent().getShortClassName() + "' ", e);
                } catch (IllegalArgumentException e2) {
                    Slog.e(JobServiceContext.TAG, "IllegalArgumentException when startJob " + e2);
                }
            }
        }

        private void handleStartedH(boolean workOngoing) {
            switch (JobServiceContext.this.mVerb) {
                case 1:
                    JobServiceContext.this.mVerb = 2;
                    if (!workOngoing) {
                        handleFinishedH(false);
                        return;
                    } else if (JobServiceContext.this.mCancelled.get()) {
                        if (JobServiceContext.DEBUG) {
                            Slog.d(JobServiceContext.TAG, "Job cancelled while waiting for onStartJob to complete.");
                        }
                        handleCancelH();
                        return;
                    } else {
                        JobServiceContext.this.scheduleOpTimeOut();
                        return;
                    }
                default:
                    Slog.e(JobServiceContext.TAG, "Handling started job but job wasn't starting! Was " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb] + ".");
                    return;
            }
        }

        private void handleFinishedH(boolean reschedule) {
            switch (JobServiceContext.this.mVerb) {
                case 2:
                case 3:
                    closeAndCleanupJobH(reschedule);
                    return;
                default:
                    Slog.e(JobServiceContext.TAG, "Got an execution complete message for a job that wasn't beingexecuted. Was " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb] + ".");
                    return;
            }
        }

        private void handleCancelH() {
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobServiceContext.TAG, "Handling cancel for: " + JobServiceContext.this.mRunningJob.getJobId() + " " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb]);
            }
            switch (JobServiceContext.this.mVerb) {
                case 0:
                case 1:
                    JobServiceContext.this.mCancelled.set(true);
                    break;
                case 2:
                    if (!hasMessages(1)) {
                        sendStopMessageH();
                        break;
                    }
                    return;
                case 3:
                    break;
                default:
                    Slog.e(JobServiceContext.TAG, "Cancelling a job without a valid verb: " + JobServiceContext.this.mVerb);
                    break;
            }
        }

        private void handleOpTimeoutH() {
            switch (JobServiceContext.this.mVerb) {
                case 0:
                    Slog.e(JobServiceContext.TAG, "Time-out while trying to bind " + JobServiceContext.this.mRunningJob.toShortString() + ", dropping.");
                    closeAndCleanupJobH(false);
                    return;
                case 1:
                    Slog.e(JobServiceContext.TAG, "No response from client for onStartJob '" + JobServiceContext.this.mRunningJob.toShortString());
                    closeAndCleanupJobH(false);
                    return;
                case 2:
                    Slog.i(JobServiceContext.TAG, "Client timed out while executing (no jobFinished received). sending onStop. " + JobServiceContext.this.mRunningJob.toShortString());
                    JobServiceContext.this.mParams.setStopReason(3);
                    sendStopMessageH();
                    return;
                case 3:
                    Slog.e(JobServiceContext.TAG, "No response from client for onStopJob, '" + JobServiceContext.this.mRunningJob.toShortString());
                    closeAndCleanupJobH(true);
                    return;
                default:
                    Slog.e(JobServiceContext.TAG, "Handling timeout for an invalid job state: " + JobServiceContext.this.mRunningJob.toShortString() + ", dropping.");
                    closeAndCleanupJobH(false);
                    return;
            }
        }

        private void handleUpdateParamH() {
            if (JobServiceContext.this.mRunningJob == null) {
                if (JobServiceContext.DEBUG) {
                    Slog.d(JobServiceContext.TAG, "Trying to update param for torn-down context, ignoring.");
                }
                return;
            }
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobServiceContext.TAG, "Handling update param for: " + JobServiceContext.this.mRunningJob.getJobId() + " " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb]);
            }
            if (JobServiceContext.this.mVerb != 2) {
                if (JobServiceContext.DEBUG) {
                    Slog.d(JobServiceContext.TAG, "Trying to update param for invalid verb, ignoring.");
                }
                return;
            }
            try {
                JobServiceContext.this.service.updateJobParameters(JobServiceContext.this.mParams);
            } catch (RemoteException e) {
                Slog.e(JobServiceContext.TAG, "Error updating parameter to client.", e);
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
                OppoStatistics.onCommon(JobServiceContext.this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "job_finish", map, false);
            }
        }

        private void uploadJObTimeOut(int verb) {
            if (JobServiceContext.this.mRunningJob != null) {
                JobInfo jobInfo = JobServiceContext.this.mRunningJob.getJob();
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
                            eventMap.put("jobId", String.valueOf(JobServiceContext.this.mRunningJob.getJobId()));
                            eventMap.put("state", state);
                            if (jobInfo.getService().flattenToShortString() != null) {
                                eventMap.put("componentName", jobInfo.getService().flattenToShortString());
                            }
                            OppoStatistics.onCommon(JobServiceContext.this.mContext, "20120008", "jobscheduler_timeout", eventMap, false);
                        }
                    }
                }
            }
        }

        private void sendStopMessageH() {
            JobServiceContext.this.removeOpTimeOut();
            if (JobServiceContext.this.mVerb != 2) {
                Slog.e(JobServiceContext.TAG, "Sending onStopJob for a job that isn't started. " + JobServiceContext.this.mRunningJob);
                closeAndCleanupJobH(false);
                return;
            }
            try {
                JobServiceContext.this.mVerb = 3;
                JobServiceContext.this.scheduleOpTimeOut();
                JobServiceContext.this.service.stopJob(JobServiceContext.this.mParams);
            } catch (RemoteException e) {
                Slog.e(JobServiceContext.TAG, "Error sending onStopJob to client.", e);
                closeAndCleanupJobH(false);
            }
        }

        /* JADX WARNING: Missing block: B:20:0x0086, code:
            com.android.server.job.JobServiceContext.-wrap0(r10.this$0);
            removeMessages(1);
            removeMessages(2);
            removeMessages(3);
            removeMessages(4);
     */
        /* JADX WARNING: Missing block: B:21:0x0099, code:
            if (r0 == null) goto L_0x00a4;
     */
        /* JADX WARNING: Missing block: B:22:0x009b, code:
            com.android.server.job.JobServiceContext.-get4(r10.this$0).onJobCompleted(r0, r11);
     */
        /* JADX WARNING: Missing block: B:23:0x00a4, code:
            com.android.server.job.JobServiceContext.-wrap2(r10.this$0);
     */
        /* JADX WARNING: Missing block: B:24:0x00a9, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void closeAndCleanupJobH(boolean reschedule) {
            synchronized (JobServiceContext.this.mLock) {
                if (JobServiceContext.this.mVerb == 4) {
                    return;
                }
                JobStatus completedJob = JobServiceContext.this.mRunningJob;
                JobServiceContext.this.mJobPackageTracker.noteInactive(completedJob);
                try {
                    JobServiceContext.this.mBatteryStats.noteJobFinish(JobServiceContext.this.mRunningJob.getBatteryName(), JobServiceContext.this.mRunningJob.getSourceUid());
                } catch (RemoteException e) {
                    Slog.w(JobServiceContext.TAG, "mBatteryStats.noteJobFinish RemoteException: " + e);
                } catch (NullPointerException e2) {
                    Slog.w(JobServiceContext.TAG, "mBatteryStats.noteJobFinish NullPointerException: " + e2);
                }
                if (JobServiceContext.this.mWakeLock != null) {
                    JobServiceContext.this.mWakeLock.release();
                }
                try {
                    JobServiceContext.this.mContext.unbindService(JobServiceContext.this);
                } catch (Exception e3) {
                    Slog.w(JobServiceContext.TAG, "mContext.unbindService fail, exception: " + e3);
                }
                JobServiceContext.this.mWakeLock = null;
                JobServiceContext.this.mRunningJob = null;
                JobServiceContext.this.mParams = null;
                JobServiceContext.this.mVerb = 4;
                JobServiceContext.this.mCancelled.set(false);
                JobServiceContext.this.service = null;
                JobServiceContext.this.mAvailable = true;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.job.JobServiceContext.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.job.JobServiceContext.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.JobServiceContext.<clinit>():void");
    }

    JobServiceContext(JobSchedulerService service, IBatteryStats batteryStats, JobPackageTracker tracker, Looper looper) {
        this(service.getContext(), service.getLock(), batteryStats, tracker, service, looper);
        this.mBatteryManagerInternal = service.mBatteryManagerInternal;
    }

    JobServiceContext(Context context, Object lock, IBatteryStats batteryStats, JobPackageTracker tracker, JobCompletedListener completedListener, Looper looper) {
        this.mCancelled = new AtomicBoolean();
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
                this.mParams = new JobParameters(this, job.getJobId(), job.getExtras(), isDeadlineExpired, triggeredUris, triggeredAuthorities);
                this.mExecutionStartTimeElapsed = SystemClock.elapsedRealtime();
                if (this.mParams != null) {
                    this.mParams.setOppoExtraStr(job.getOppoExtraStr());
                }
                this.mVerb = 0;
                scheduleOpTimeOut();
                Intent intent = new Intent().setComponent(job.getServiceComponent());
                intent.putExtra("BINDSERVICE_FROM_JOB", true);
                if (this.mContext.bindServiceAsUser(intent, this, 5, new UserHandle(job.getUserId()))) {
                    try {
                        this.mBatteryStats.noteJobStart(job.getBatteryName(), job.getSourceUid());
                    } catch (RemoteException e) {
                    }
                    this.mJobPackageTracker.noteActive(job);
                    this.mAvailable = false;
                    return true;
                }
                if (DEBUG) {
                    Slog.d(TAG, job.getServiceComponent().getShortClassName() + " unavailable.");
                }
                this.mRunningJob = null;
                this.mParams = null;
                this.mExecutionStartTimeElapsed = 0;
                this.mVerb = 4;
                removeOpTimeOut();
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
            if (0 != jobInfo.getIntervalMillis()) {
                eventMap.put("periodic", String.valueOf(jobInfo.getIntervalMillis() / 1000));
            }
            if (0 != jobInfo.getMaxExecutionDelayMillis()) {
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

    JobStatus getRunningJob() {
        JobStatus job;
        synchronized (this.mLock) {
            job = this.mRunningJob;
        }
        if (job == null) {
            return null;
        }
        return new JobStatus(job);
    }

    JobStatus getRunningJobUnsafeLocked() {
        return this.mRunningJob;
    }

    void cancelExecutingJob(int reason) {
        this.mCallbackHandler.obtainMessage(3, reason, 0).sendToTarget();
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
                this.mCallbackHandler.obtainMessage(101).sendToTarget();
                return true;
            }
        }
    }

    void preemptExecutingJob() {
        Message m = this.mCallbackHandler.obtainMessage(3);
        m.arg1 = 2;
        m.sendToTarget();
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

    public void jobFinished(int jobId, boolean reschedule) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(1, jobId, reschedule ? 1 : 0).sendToTarget();
        }
    }

    public void acknowledgeStopMessage(int jobId, boolean reschedule) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(1, jobId, reschedule ? 1 : 0).sendToTarget();
        }
    }

    public void acknowledgeStartMessage(int jobId, boolean ongoing) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(1, jobId, ongoing ? 1 : 0).sendToTarget();
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        JobStatus runningJob;
        synchronized (this.mLock) {
            runningJob = this.mRunningJob;
        }
        if (runningJob == null || !name.equals(runningJob.getServiceComponent())) {
            this.mCallbackHandler.obtainMessage(4).sendToTarget();
            return;
        }
        this.service = IJobService.Stub.asInterface(service);
        WakeLock wl = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, runningJob.getTag());
        wl.setWorkSource(new WorkSource(runningJob.getSourceUid()));
        wl.setReferenceCounted(false);
        wl.acquire();
        synchronized (this.mLock) {
            if (this.mWakeLock != null) {
                Slog.w(TAG, "Bound new job " + runningJob + " but live wakelock " + this.mWakeLock + " tag=" + this.mWakeLock.getTag());
                this.mWakeLock.release();
            }
            this.mWakeLock = wl;
        }
        this.mCallbackHandler.obtainMessage(2).sendToTarget();
    }

    public void onServiceDisconnected(ComponentName name) {
        this.mCallbackHandler.obtainMessage(4).sendToTarget();
    }

    private boolean verifyCallingUid() {
        synchronized (this.mLock) {
            if (this.mRunningJob == null || Binder.getCallingUid() != this.mRunningJob.getUid()) {
                if (DEBUG) {
                    Slog.d(TAG, "Stale callback received, ignoring.");
                }
                return false;
            }
            return true;
        }
    }

    private void scheduleOpTimeOut() {
        removeOpTimeOut();
        long timeoutMillis = this.mVerb == 2 ? 600000 : OP_TIMEOUT_MILLIS;
        if (this.mRunningJob != null && this.mRunningJob.isOppoJob() && this.mVerb == 2) {
            Slog.d(TAG, "scheduleOpTimeOut set oppo job timeout to 2 hour");
            timeoutMillis = 7200000;
        }
        if (DEBUG) {
            Slog.d(TAG, "Scheduling time out for '" + this.mRunningJob.getServiceComponent().getShortClassName() + "' jId: " + this.mParams.getJobId() + ", in " + (timeoutMillis / 1000) + " s");
        }
        this.mCallbackHandler.sendMessageDelayed(this.mCallbackHandler.obtainMessage(0), timeoutMillis);
        this.mTimeoutElapsed = SystemClock.elapsedRealtime() + timeoutMillis;
    }

    private void removeOpTimeOut() {
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
            if (!(cpuThermalStart == null || cpuThermalStart.isEmpty())) {
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
            if (!(cpuThermal == null || cpuThermal.isEmpty())) {
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
