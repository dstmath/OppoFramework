package com.android.server.job.controllers;

import android.app.AlarmManager;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.JobSchedulerService;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

public final class TimeController extends StateController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final String TAG = "JobScheduler.Time";
    private final String DEADLINE_TAG = "*job.deadline*";
    private final String DELAY_TAG = "*job.delay*";
    private AlarmManager mAlarmService = null;
    private final boolean mChainedAttributionEnabled = this.mService.isChainedAttributionEnabled();
    private final AlarmManager.OnAlarmListener mDeadlineExpiredListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.job.controllers.TimeController.AnonymousClass1 */

        public void onAlarm() {
            if (TimeController.DEBUG) {
                Slog.d(TimeController.TAG, "Deadline-expired alarm fired");
            }
            TimeController.this.checkExpiredDeadlinesAndResetAlarm();
        }
    };
    private final Handler mHandler = new Handler(this.mContext.getMainLooper());
    private long mNextDelayExpiredElapsedMillis = JobStatus.NO_LATEST_RUNTIME;
    private final AlarmManager.OnAlarmListener mNextDelayExpiredListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.job.controllers.TimeController.AnonymousClass2 */

        public void onAlarm() {
            if (TimeController.DEBUG) {
                Slog.d(TimeController.TAG, "Delay-expired alarm fired");
            }
            TimeController.this.checkExpiredDelaysAndResetAlarm();
        }
    };
    private long mNextJobExpiredElapsedMillis = JobStatus.NO_LATEST_RUNTIME;
    private final TcConstants mTcConstants = new TcConstants(this.mHandler);
    private final List<JobStatus> mTrackedJobs = new LinkedList();

    public TimeController(JobSchedulerService service) {
        super(service);
    }

    @Override // com.android.server.job.controllers.StateController
    public void onSystemServicesReady() {
        this.mTcConstants.start(this.mContext.getContentResolver());
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStartTrackingJobLocked(JobStatus job, JobStatus lastJob) {
        if (job.hasTimingDelayConstraint() || job.hasDeadlineConstraint()) {
            maybeStopTrackingJobLocked(job, null, false);
            long nowElapsedMillis = JobSchedulerService.sElapsedRealtimeClock.millis();
            if (job.hasDeadlineConstraint() && evaluateDeadlineConstraint(job, nowElapsedMillis)) {
                return;
            }
            if (!job.hasTimingDelayConstraint() || !evaluateTimingDelayConstraint(job, nowElapsedMillis) || job.hasDeadlineConstraint()) {
                boolean isInsert = false;
                List<JobStatus> list = this.mTrackedJobs;
                ListIterator<JobStatus> it = list.listIterator(list.size());
                while (true) {
                    if (it.hasPrevious()) {
                        if (it.previous().getLatestRunTimeElapsed() < job.getLatestRunTimeElapsed()) {
                            isInsert = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (isInsert) {
                    it.next();
                }
                it.add(job);
                job.setTrackingController(32);
                WorkSource ws = deriveWorkSource(job.getSourceUid(), job.getSourcePackageName());
                boolean hasDeadlineConstraint = job.hasDeadlineConstraint();
                long delayExpiredElapsed = JobStatus.NO_LATEST_RUNTIME;
                long deadlineExpiredElapsed = hasDeadlineConstraint ? job.getLatestRunTimeElapsed() : Long.MAX_VALUE;
                if (job.hasTimingDelayConstraint()) {
                    delayExpiredElapsed = job.getEarliestRunTime();
                }
                if (this.mTcConstants.SKIP_NOT_READY_JOBS) {
                    if (wouldBeReadyWithConstraintLocked(job, Integer.MIN_VALUE)) {
                        maybeUpdateDelayAlarmLocked(delayExpiredElapsed, ws);
                    }
                    if (wouldBeReadyWithConstraintLocked(job, 1073741824)) {
                        maybeUpdateDeadlineAlarmLocked(deadlineExpiredElapsed, ws);
                        return;
                    }
                    return;
                }
                maybeUpdateDelayAlarmLocked(delayExpiredElapsed, ws);
                maybeUpdateDeadlineAlarmLocked(deadlineExpiredElapsed, ws);
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStopTrackingJobLocked(JobStatus job, JobStatus incomingJob, boolean forUpdate) {
        if (job.clearTrackingController(32) && this.mTrackedJobs.remove(job)) {
            checkExpiredDelaysAndResetAlarm();
            checkExpiredDeadlinesAndResetAlarm();
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void evaluateStateLocked(JobStatus job) {
        if (this.mTcConstants.SKIP_NOT_READY_JOBS) {
            long nowElapsedMillis = JobSchedulerService.sElapsedRealtimeClock.millis();
            boolean isAlarmForJob = true;
            if (job.hasDeadlineConstraint() && !job.isConstraintSatisfied(1073741824) && job.getLatestRunTimeElapsed() <= this.mNextJobExpiredElapsedMillis) {
                if (evaluateDeadlineConstraint(job, nowElapsedMillis)) {
                    checkExpiredDeadlinesAndResetAlarm();
                    checkExpiredDelaysAndResetAlarm();
                } else {
                    boolean isAlarmForJob2 = job.getLatestRunTimeElapsed() == this.mNextJobExpiredElapsedMillis;
                    boolean wouldBeReady = wouldBeReadyWithConstraintLocked(job, 1073741824);
                    if ((isAlarmForJob2 && !wouldBeReady) || (!isAlarmForJob2 && wouldBeReady)) {
                        checkExpiredDeadlinesAndResetAlarm();
                    }
                }
            }
            if (job.hasTimingDelayConstraint() && !job.isConstraintSatisfied(Integer.MIN_VALUE) && job.getEarliestRunTime() <= this.mNextDelayExpiredElapsedMillis) {
                if (evaluateTimingDelayConstraint(job, nowElapsedMillis)) {
                    checkExpiredDelaysAndResetAlarm();
                    return;
                }
                if (job.getEarliestRunTime() != this.mNextDelayExpiredElapsedMillis) {
                    isAlarmForJob = false;
                }
                boolean wouldBeReady2 = wouldBeReadyWithConstraintLocked(job, Integer.MIN_VALUE);
                if ((isAlarmForJob && !wouldBeReady2) || (!isAlarmForJob && wouldBeReady2)) {
                    checkExpiredDelaysAndResetAlarm();
                }
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void reevaluateStateLocked(int uid) {
        checkExpiredDeadlinesAndResetAlarm();
        checkExpiredDelaysAndResetAlarm();
    }

    private boolean canStopTrackingJobLocked(JobStatus job) {
        return (!job.hasTimingDelayConstraint() || job.isConstraintSatisfied(Integer.MIN_VALUE)) && (!job.hasDeadlineConstraint() || job.isConstraintSatisfied(1073741824));
    }

    private void ensureAlarmServiceLocked() {
        if (this.mAlarmService == null) {
            this.mAlarmService = (AlarmManager) this.mContext.getSystemService("alarm");
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void checkExpiredDeadlinesAndResetAlarm() {
        synchronized (this.mLock) {
            long nextExpiryTime = JobStatus.NO_LATEST_RUNTIME;
            int nextExpiryUid = 0;
            String nextExpiryPackageName = null;
            long nowElapsedMillis = JobSchedulerService.sElapsedRealtimeClock.millis();
            ListIterator<JobStatus> it = this.mTrackedJobs.listIterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                JobStatus job = it.next();
                if (job.hasDeadlineConstraint()) {
                    if (evaluateDeadlineConstraint(job, nowElapsedMillis)) {
                        if (job.isReady()) {
                            this.mStateChangedListener.onRunJobNow(job);
                        }
                        it.remove();
                    } else if (!this.mTcConstants.SKIP_NOT_READY_JOBS || wouldBeReadyWithConstraintLocked(job, 1073741824)) {
                        nextExpiryTime = job.getLatestRunTimeElapsed();
                        nextExpiryUid = job.getSourceUid();
                        nextExpiryPackageName = job.getSourcePackageName();
                    } else if (DEBUG) {
                        Slog.i(TAG, "Skipping " + job + " because deadline won't make it ready.");
                    }
                }
            }
            setDeadlineExpiredAlarmLocked(nextExpiryTime, deriveWorkSource(nextExpiryUid, nextExpiryPackageName));
        }
    }

    private boolean evaluateDeadlineConstraint(JobStatus job, long nowElapsedMillis) {
        if (job.getLatestRunTimeElapsed() > nowElapsedMillis) {
            return false;
        }
        if (job.hasTimingDelayConstraint()) {
            job.setTimingDelayConstraintSatisfied(true);
        }
        job.setDeadlineConstraintSatisfied(true);
        return true;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void checkExpiredDelaysAndResetAlarm() {
        synchronized (this.mLock) {
            long nowElapsedMillis = JobSchedulerService.sElapsedRealtimeClock.millis();
            long nextDelayTime = JobStatus.NO_LATEST_RUNTIME;
            int nextDelayUid = 0;
            String nextDelayPackageName = null;
            boolean ready = false;
            Iterator<JobStatus> it = this.mTrackedJobs.iterator();
            while (it.hasNext()) {
                JobStatus job = it.next();
                if (job != null) {
                    if (job.hasTimingDelayConstraint()) {
                        if (evaluateTimingDelayConstraint(job, nowElapsedMillis)) {
                            if (canStopTrackingJobLocked(job)) {
                                it.remove();
                            }
                            if (job.isReady()) {
                                ready = true;
                            }
                        } else if (!this.mTcConstants.SKIP_NOT_READY_JOBS || wouldBeReadyWithConstraintLocked(job, Integer.MIN_VALUE)) {
                            long jobDelayTime = job.getEarliestRunTime();
                            if (nextDelayTime > jobDelayTime) {
                                nextDelayTime = jobDelayTime;
                                nextDelayUid = job.getSourceUid();
                                nextDelayPackageName = job.getSourcePackageName();
                            }
                        } else if (DEBUG) {
                            Slog.i(TAG, "Skipping " + job + " because delay won't make it ready.");
                        }
                    }
                }
            }
            if (ready) {
                this.mStateChangedListener.onControllerStateChanged();
            }
            setDelayExpiredAlarmLocked(nextDelayTime, deriveWorkSource(nextDelayUid, nextDelayPackageName));
        }
    }

    private WorkSource deriveWorkSource(int uid, String packageName) {
        WorkSource workSource;
        if (this.mChainedAttributionEnabled) {
            WorkSource ws = new WorkSource();
            ws.createWorkChain().addNode(uid, packageName).addNode(1000, JobSchedulerService.TAG);
            return ws;
        }
        if (packageName != null) {
            workSource = new WorkSource(uid, packageName);
        }
        return workSource;
    }

    private boolean evaluateTimingDelayConstraint(JobStatus job, long nowElapsedMillis) {
        if (job.getEarliestRunTime() > nowElapsedMillis) {
            return false;
        }
        job.setTimingDelayConstraintSatisfied(true);
        return true;
    }

    private void maybeUpdateDelayAlarmLocked(long delayExpiredElapsed, WorkSource ws) {
        if (delayExpiredElapsed < this.mNextDelayExpiredElapsedMillis) {
            setDelayExpiredAlarmLocked(delayExpiredElapsed, ws);
        }
    }

    private void maybeUpdateDeadlineAlarmLocked(long deadlineExpiredElapsed, WorkSource ws) {
        if (deadlineExpiredElapsed < this.mNextJobExpiredElapsedMillis) {
            setDeadlineExpiredAlarmLocked(deadlineExpiredElapsed, ws);
        }
    }

    private void setDelayExpiredAlarmLocked(long alarmTimeElapsedMillis, WorkSource ws) {
        long alarmTimeElapsedMillis2 = maybeAdjustAlarmTime(alarmTimeElapsedMillis);
        if (this.mNextDelayExpiredElapsedMillis != alarmTimeElapsedMillis2) {
            this.mNextDelayExpiredElapsedMillis = alarmTimeElapsedMillis2;
            updateAlarmWithListenerLocked("*job.delay*", this.mNextDelayExpiredListener, this.mNextDelayExpiredElapsedMillis, ws);
        }
    }

    private void setDeadlineExpiredAlarmLocked(long alarmTimeElapsedMillis, WorkSource ws) {
        long alarmTimeElapsedMillis2 = maybeAdjustAlarmTime(alarmTimeElapsedMillis);
        if (this.mNextJobExpiredElapsedMillis != alarmTimeElapsedMillis2) {
            this.mNextJobExpiredElapsedMillis = alarmTimeElapsedMillis2;
            updateAlarmWithListenerLocked("*job.deadline*", this.mDeadlineExpiredListener, this.mNextJobExpiredElapsedMillis, ws);
        }
    }

    private long maybeAdjustAlarmTime(long proposedAlarmTimeElapsedMillis) {
        return Math.max(proposedAlarmTimeElapsedMillis, JobSchedulerService.sElapsedRealtimeClock.millis());
    }

    private void updateAlarmWithListenerLocked(String tag, AlarmManager.OnAlarmListener listener, long alarmTimeElapsed, WorkSource ws) {
        ensureAlarmServiceLocked();
        if (alarmTimeElapsed == JobStatus.NO_LATEST_RUNTIME) {
            this.mAlarmService.cancel(listener);
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "Setting " + tag + " for: " + alarmTimeElapsed);
        }
        this.mAlarmService.set(2, alarmTimeElapsed, -1, 0, tag, listener, null, ws);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void recheckAlarmsLocked() {
        checkExpiredDeadlinesAndResetAlarm();
        checkExpiredDelaysAndResetAlarm();
    }

    @VisibleForTesting
    class TcConstants extends ContentObserver {
        private static final boolean DEFAULT_SKIP_NOT_READY_JOBS = true;
        private static final String KEY_SKIP_NOT_READY_JOBS = "skip_not_ready_jobs";
        public boolean SKIP_NOT_READY_JOBS = true;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        TcConstants(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: private */
        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("job_scheduler_time_controller_constants"), false, this);
            onChange(true, null);
        }

        /* JADX INFO: Multiple debug info for r1v1 boolean: [D('e' java.lang.Exception), D('oldVal' boolean)] */
        public void onChange(boolean selfChange, Uri uri) {
            try {
                this.mParser.setString(Settings.Global.getString(this.mResolver, "job_scheduler_time_controller_constants"));
            } catch (Exception e) {
                Slog.e(TimeController.TAG, "Bad jobscheduler time controller settings", e);
            }
            boolean oldVal = this.SKIP_NOT_READY_JOBS;
            this.SKIP_NOT_READY_JOBS = this.mParser.getBoolean(KEY_SKIP_NOT_READY_JOBS, true);
            if (oldVal != this.SKIP_NOT_READY_JOBS) {
                synchronized (TimeController.this.mLock) {
                    TimeController.this.recheckAlarmsLocked();
                }
            }
        }

        /* access modifiers changed from: private */
        public void dump(IndentingPrintWriter pw) {
            pw.println();
            pw.println("TimeController:");
            pw.increaseIndent();
            pw.printPair(KEY_SKIP_NOT_READY_JOBS, Boolean.valueOf(this.SKIP_NOT_READY_JOBS)).println();
            pw.decreaseIndent();
        }

        /* access modifiers changed from: private */
        public void dump(ProtoOutputStream proto) {
            long tcToken = proto.start(1146756268057L);
            proto.write(1133871366145L, this.SKIP_NOT_READY_JOBS);
            proto.end(tcToken);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public TcConstants getTcConstants() {
        return this.mTcConstants;
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        long nowElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
        pw.println("Elapsed clock: " + nowElapsed);
        pw.print("Next delay alarm in ");
        TimeUtils.formatDuration(this.mNextDelayExpiredElapsedMillis, nowElapsed, pw);
        pw.println();
        pw.print("Next deadline alarm in ");
        TimeUtils.formatDuration(this.mNextJobExpiredElapsedMillis, nowElapsed, pw);
        pw.println();
        pw.println();
        for (JobStatus ts : this.mTrackedJobs) {
            if (predicate.test(ts)) {
                pw.print("#");
                ts.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, ts.getSourceUid());
                pw.print(": Delay=");
                if (ts.hasTimingDelayConstraint()) {
                    TimeUtils.formatDuration(ts.getEarliestRunTime(), nowElapsed, pw);
                } else {
                    pw.print("N/A");
                }
                pw.print(", Deadline=");
                if (ts.hasDeadlineConstraint()) {
                    TimeUtils.formatDuration(ts.getLatestRunTimeElapsed(), nowElapsed, pw);
                } else {
                    pw.print("N/A");
                }
                pw.println();
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        long token = proto.start(fieldId);
        long mToken = proto.start(1146756268040L);
        long nowElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
        proto.write(1112396529665L, nowElapsed);
        proto.write(1112396529666L, this.mNextDelayExpiredElapsedMillis - nowElapsed);
        proto.write(1112396529667L, this.mNextJobExpiredElapsedMillis - nowElapsed);
        for (JobStatus ts : this.mTrackedJobs) {
            if (predicate.test(ts)) {
                long tsToken = proto.start(2246267895812L);
                ts.writeToShortProto(proto, 1146756268033L);
                proto.write(1133871366147L, ts.hasTimingDelayConstraint());
                proto.write(1112396529668L, ts.getEarliestRunTime() - nowElapsed);
                proto.write(1133871366149L, ts.hasDeadlineConstraint());
                proto.write(1112396529670L, ts.getLatestRunTimeElapsed() - nowElapsed);
                proto.end(tsToken);
                token = token;
            }
        }
        proto.end(mToken);
        proto.end(token);
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpConstants(IndentingPrintWriter pw) {
        this.mTcConstants.dump(pw);
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpConstants(ProtoOutputStream proto) {
        this.mTcConstants.dump(proto);
    }
}
