package com.android.server.job.controllers;

import android.app.AppGlobals;
import android.app.job.JobInfo;
import android.app.job.JobInfo.TriggerContentUri;
import android.content.ComponentName;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.format.DateUtils;
import android.util.ArraySet;
import android.util.TimeUtils;
import com.android.server.oppo.IElsaManager;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public final class JobStatus {
    static final int CONSTRAINTS_OF_INTEREST = 7867;
    static final int CONSTRAINT_APP_NOT_IDLE = 64;
    static final int CONSTRAINT_BATTERY_DILE = 1024;
    static final int CONSTRAINT_CHARGING = 1;
    static final int CONSTRAINT_CONNECTIVITY = 32;
    static final int CONSTRAINT_CONTENT_TRIGGER = 128;
    public static final int CONSTRAINT_CPU = 4096;
    static final int CONSTRAINT_DEADLINE = 4;
    static final int CONSTRAINT_DEVICE_NOT_DOZING = 256;
    public static final int CONSTRAINT_FORE_APP = 2048;
    static final int CONSTRAINT_IDLE = 8;
    static final int CONSTRAINT_NOT_ROAMING = 512;
    static final int CONSTRAINT_TIMING_DELAY = 2;
    static final int CONSTRAINT_UNMETERED = 16;
    public static final long DEFAULT_TRIGGER_MAX_DELAY = 120000;
    public static final long DEFAULT_TRIGGER_UPDATE_DELAY = 10000;
    public static final long MIN_TRIGGER_MAX_DELAY = 1000;
    public static final long MIN_TRIGGER_UPDATE_DELAY = 500;
    public static final long NO_EARLIEST_RUNTIME = 0;
    public static final long NO_LATEST_RUNTIME = Long.MAX_VALUE;
    public static final int OVERRIDE_FULL = 2;
    public static final int OVERRIDE_SOFT = 1;
    static final int SOFT_OVERRIDE_CONSTRAINTS = 11;
    final String batteryName;
    final int callingUid;
    public ArraySet<String> changedAuthorities;
    public ArraySet<Uri> changedUris;
    JobInstance contentObserverJobInstance;
    public final AtomicInteger cpuLevel;
    public boolean dozeWhitelisted;
    private long earliestRunTimeElapsedMillis;
    private boolean isOppoJob;
    final JobInfo job;
    public int lastEvaluatedPriority;
    private final long latestRunTimeElapsedMillis;
    private final int numFailures;
    public final AtomicInteger oldCpuLevel;
    public int overrideState;
    final int requiredConstraints;
    int satisfiedConstraints;
    final String sourcePackageName;
    final String sourceTag;
    final int sourceUid;
    final int sourceUserId;
    final String tag;

    public int getServiceToken() {
        return this.callingUid;
    }

    private JobStatus(JobInfo job, int callingUid, String sourcePackageName, int sourceUserId, String tag, int numFailures, long earliestRunTimeElapsedMillis, long latestRunTimeElapsedMillis) {
        String str;
        this.cpuLevel = new AtomicInteger();
        this.oldCpuLevel = new AtomicInteger();
        this.satisfiedConstraints = 0;
        this.overrideState = 0;
        this.job = job;
        this.callingUid = callingUid;
        int tempSourceUid = -1;
        if (!(sourceUserId == -1 || sourcePackageName == null)) {
            try {
                tempSourceUid = AppGlobals.getPackageManager().getPackageUid(sourcePackageName, 0, sourceUserId);
            } catch (RemoteException e) {
            }
        }
        if (tempSourceUid == -1) {
            this.sourceUid = callingUid;
            this.sourceUserId = UserHandle.getUserId(callingUid);
            this.sourcePackageName = job.getService().getPackageName();
            this.sourceTag = null;
        } else {
            this.sourceUid = tempSourceUid;
            this.sourceUserId = sourceUserId;
            this.sourcePackageName = sourcePackageName;
            this.sourceTag = tag;
        }
        if (this.sourceTag != null) {
            str = this.sourceTag + ":" + job.getService().getPackageName();
        } else {
            str = job.getService().flattenToShortString();
        }
        this.batteryName = str;
        this.tag = "*job*/" + this.batteryName;
        this.earliestRunTimeElapsedMillis = earliestRunTimeElapsedMillis;
        this.latestRunTimeElapsedMillis = latestRunTimeElapsedMillis;
        this.numFailures = numFailures;
        int requiredConstraints = 0;
        if (job.getNetworkType() == 1) {
            requiredConstraints = 32;
        }
        if (job.getNetworkType() == 2) {
            requiredConstraints |= 16;
        }
        if (job.getNetworkType() == 3) {
            requiredConstraints |= 512;
        }
        if (job.isRequireCharging()) {
            requiredConstraints |= 1;
        }
        if (earliestRunTimeElapsedMillis != 0) {
            requiredConstraints |= 2;
        }
        if (latestRunTimeElapsedMillis != NO_LATEST_RUNTIME) {
            requiredConstraints |= 4;
        }
        if (job.isRequireDeviceIdle()) {
            requiredConstraints |= 8;
        }
        if (job.getTriggerContentUris() != null) {
            requiredConstraints |= 128;
        }
        if (job.isRequireBattIdle()) {
            requiredConstraints |= 1024;
        }
        if (job.isRequireProtectFore()) {
            requiredConstraints |= 2048;
        }
        if (job.hasCpuConstraint()) {
            requiredConstraints |= 4096;
        }
        this.isOppoJob = job.getOppoJob();
        this.requiredConstraints = requiredConstraints;
    }

    public JobStatus(JobStatus jobStatus) {
        this(jobStatus.getJob(), jobStatus.getUid(), jobStatus.getSourcePackageName(), jobStatus.getSourceUserId(), jobStatus.getSourceTag(), jobStatus.getNumFailures(), jobStatus.getEarliestRunTime(), jobStatus.getLatestRunTimeElapsed());
    }

    public JobStatus(JobInfo job, int callingUid, String sourcePackageName, int sourceUserId, String sourceTag, long earliestRunTimeElapsedMillis, long latestRunTimeElapsedMillis) {
        this(job, callingUid, sourcePackageName, sourceUserId, sourceTag, 0, earliestRunTimeElapsedMillis, latestRunTimeElapsedMillis);
    }

    public JobStatus(JobStatus rescheduling, long newEarliestRuntimeElapsedMillis, long newLatestRuntimeElapsedMillis, int backoffAttempt) {
        this(rescheduling.job, rescheduling.getUid(), rescheduling.getSourcePackageName(), rescheduling.getSourceUserId(), rescheduling.getSourceTag(), backoffAttempt, newEarliestRuntimeElapsedMillis, newLatestRuntimeElapsedMillis);
    }

    public static JobStatus createFromJobInfo(JobInfo job, int callingUid, String sourcePackageName, int sourceUserId, String tag) {
        long latestRunTimeElapsedMillis;
        long earliestRunTimeElapsedMillis;
        long elapsedNow = SystemClock.elapsedRealtime();
        if (job.isPeriodic()) {
            latestRunTimeElapsedMillis = elapsedNow + job.getIntervalMillis();
            earliestRunTimeElapsedMillis = latestRunTimeElapsedMillis - job.getFlexMillis();
        } else {
            earliestRunTimeElapsedMillis = job.hasEarlyConstraint() ? elapsedNow + job.getMinLatencyMillis() : 0;
            latestRunTimeElapsedMillis = job.hasLateConstraint() ? elapsedNow + job.getMaxExecutionDelayMillis() : NO_LATEST_RUNTIME;
        }
        return new JobStatus(job, callingUid, sourcePackageName, sourceUserId, tag, 0, earliestRunTimeElapsedMillis, latestRunTimeElapsedMillis);
    }

    public JobInfo getJob() {
        return this.job;
    }

    public int getJobId() {
        return this.job.getId();
    }

    public void printUniqueId(PrintWriter pw) {
        UserHandle.formatUid(pw, this.callingUid);
        pw.print("/");
        pw.print(this.job.getId());
    }

    public int getNumFailures() {
        return this.numFailures;
    }

    public ComponentName getServiceComponent() {
        return this.job.getService();
    }

    public String getSourcePackageName() {
        return this.sourcePackageName;
    }

    public int getSourceUid() {
        return this.sourceUid;
    }

    public int getSourceUserId() {
        return this.sourceUserId;
    }

    public int getUserId() {
        return UserHandle.getUserId(this.callingUid);
    }

    public String getSourceTag() {
        return this.sourceTag;
    }

    public int getUid() {
        return this.callingUid;
    }

    public String getBatteryName() {
        return this.batteryName;
    }

    public String getTag() {
        return this.tag;
    }

    public PersistableBundle getExtras() {
        return this.job.getExtras();
    }

    public int getPriority() {
        return this.job.getPriority();
    }

    public int getFlags() {
        return this.job.getFlags();
    }

    public boolean hasConnectivityConstraint() {
        return (this.requiredConstraints & 32) != 0;
    }

    public boolean hasUnmeteredConstraint() {
        return (this.requiredConstraints & 16) != 0;
    }

    public boolean hasNotRoamingConstraint() {
        return (this.requiredConstraints & 512) != 0;
    }

    public boolean hasChargingConstraint() {
        return (this.requiredConstraints & 1) != 0;
    }

    public boolean hasBattIdleConstraint() {
        return (this.requiredConstraints & 1024) != 0;
    }

    public boolean hasProtectForeConstraint() {
        return (this.requiredConstraints & 2048) != 0;
    }

    public boolean hasCpuConstraint() {
        return (this.requiredConstraints & 4096) != 0;
    }

    public boolean isOppoJob() {
        return this.isOppoJob;
    }

    public String getOppoExtraStr() {
        return this.job.getOppoExtraStr();
    }

    public int getProtectForeType() {
        return this.job.getProtectForeType();
    }

    public boolean hasTimingDelayConstraint() {
        return (this.requiredConstraints & 2) != 0;
    }

    public boolean hasDeadlineConstraint() {
        return (this.requiredConstraints & 4) != 0;
    }

    public boolean hasIdleConstraint() {
        return (this.requiredConstraints & 8) != 0;
    }

    public boolean hasContentTriggerConstraint() {
        return (this.requiredConstraints & 128) != 0;
    }

    public long getTriggerContentUpdateDelay() {
        long time = this.job.getTriggerContentUpdateDelay();
        if (time < 0) {
            return 10000;
        }
        return Math.max(time, 500);
    }

    public long getTriggerContentMaxDelay() {
        long time = this.job.getTriggerContentMaxDelay();
        if (time < 0) {
            return DEFAULT_TRIGGER_MAX_DELAY;
        }
        return Math.max(time, 1000);
    }

    public boolean isPersisted() {
        return this.job.isPersisted();
    }

    public long getEarliestRunTime() {
        return this.earliestRunTimeElapsedMillis;
    }

    public long getLatestRunTimeElapsed() {
        return this.latestRunTimeElapsedMillis;
    }

    boolean setChargingConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(1, state);
    }

    boolean setBattIdleConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(1024, state);
    }

    boolean setProtectForeConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(2048, state);
    }

    public boolean setCpuConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(4096, state);
    }

    public boolean isCpuConstraintSatisfied() {
        return (this.satisfiedConstraints & 4096) != 0;
    }

    boolean setTimingDelayConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(2, state);
    }

    boolean setDeadlineConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(4, state);
    }

    boolean setIdleConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(8, state);
    }

    boolean setConnectivityConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(32, state);
    }

    boolean setUnmeteredConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(16, state);
    }

    boolean setNotRoamingConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(512, state);
    }

    boolean setAppNotIdleConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(64, state);
    }

    boolean setContentTriggerConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(128, state);
    }

    boolean setDeviceNotDozingConstraintSatisfied(boolean state, boolean whitelisted) {
        this.dozeWhitelisted = whitelisted;
        return setConstraintSatisfied(256, state);
    }

    boolean setConstraintSatisfied(int constraint, boolean state) {
        boolean old;
        if ((this.satisfiedConstraints & constraint) != 0) {
            old = true;
        } else {
            old = false;
        }
        if (old == state) {
            return false;
        }
        int i = this.satisfiedConstraints & (~constraint);
        if (!state) {
            constraint = 0;
        }
        this.satisfiedConstraints = i | constraint;
        return true;
    }

    public boolean isConstraintSatisfied(int constraint) {
        return (this.satisfiedConstraints & constraint) != 0;
    }

    public boolean shouldDump(int filterUid) {
        if (filterUid == -1 || UserHandle.getAppId(getUid()) == filterUid || UserHandle.getAppId(getSourceUid()) == filterUid) {
            return true;
        }
        return false;
    }

    public boolean isReady() {
        boolean deadlineSatisfied = (this.job.isPeriodic() || !hasDeadlineConstraint()) ? false : (this.satisfiedConstraints & 4) != 0;
        boolean notIdle = (this.satisfiedConstraints & 64) != 0;
        boolean notDozing = (this.satisfiedConstraints & 256) == 0 ? (this.job.getFlags() & 1) != 0 : true;
        if ((isConstraintsSatisfied() || deadlineSatisfied) && notIdle) {
            return notDozing;
        }
        return false;
    }

    public boolean isConstraintsSatisfied() {
        boolean z = true;
        if (this.overrideState == 2) {
            return true;
        }
        int req = this.requiredConstraints & CONSTRAINTS_OF_INTEREST;
        int sat = this.satisfiedConstraints & CONSTRAINTS_OF_INTEREST;
        if (this.overrideState == 1) {
            sat |= this.requiredConstraints & 11;
        }
        if ((sat & req) != req) {
            z = false;
        }
        return z;
    }

    public boolean matches(int uid, int jobId) {
        return this.job.getId() == jobId && this.callingUid == uid;
    }

    public String toString() {
        boolean z;
        String str;
        boolean z2 = true;
        StringBuilder append = new StringBuilder().append(String.valueOf(hashCode()).substring(0, 3)).append("..").append(":[").append(this.job.getService()).append(",jId=").append(this.job.getId()).append(",u").append(getUserId()).append(",suid=").append(getSourceUid()).append(",R=(").append(formatRunTime(this.earliestRunTimeElapsedMillis, 0)).append(",").append(formatRunTime(this.latestRunTimeElapsedMillis, NO_LATEST_RUNTIME)).append(")").append(",N=").append(this.job.getNetworkType()).append(",C=").append(this.job.isRequireCharging()).append(",I=").append(this.job.isRequireDeviceIdle()).append(",U=").append(this.job.getTriggerContentUris() != null).append(",F=").append(this.numFailures).append(",P=").append(this.job.isPersisted()).append(",ANI=");
        if ((this.satisfiedConstraints & 64) != 0) {
            z = true;
        } else {
            z = false;
        }
        StringBuilder append2 = append.append(z).append(",DND=");
        if ((this.satisfiedConstraints & 256) == 0) {
            z2 = false;
        }
        StringBuilder append3 = append2.append(z2);
        if (isReady()) {
            str = "(READY)";
        } else {
            str = IElsaManager.EMPTY_PACKAGE;
        }
        return append3.append(str).append("]").toString();
    }

    private String formatRunTime(long runtime, long defaultValue) {
        if (runtime == defaultValue) {
            return "none";
        }
        long nextRuntime = runtime - SystemClock.elapsedRealtime();
        if (nextRuntime > 0) {
            return DateUtils.formatElapsedTime(nextRuntime / 1000);
        }
        return "-" + DateUtils.formatElapsedTime(nextRuntime / -1000);
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" #");
        UserHandle.formatUid(sb, this.callingUid);
        sb.append("/");
        sb.append(this.job.getId());
        sb.append(' ');
        sb.append(this.batteryName);
        return sb.toString();
    }

    public String toShortStringExceptUniqueId() {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.batteryName);
        return sb.toString();
    }

    void dumpConstraints(PrintWriter pw, int constraints) {
        if ((constraints & 1) != 0) {
            pw.print(" CHARGING");
        }
        if ((constraints & 2) != 0) {
            pw.print(" TIMING_DELAY");
        }
        if ((constraints & 4) != 0) {
            pw.print(" DEADLINE");
        }
        if ((constraints & 8) != 0) {
            pw.print(" IDLE");
        }
        if ((constraints & 32) != 0) {
            pw.print(" CONNECTIVITY");
        }
        if ((constraints & 16) != 0) {
            pw.print(" UNMETERED");
        }
        if ((constraints & 512) != 0) {
            pw.print(" NOT_ROAMING");
        }
        if ((constraints & 64) != 0) {
            pw.print(" APP_NOT_IDLE");
        }
        if ((constraints & 128) != 0) {
            pw.print(" CONTENT_TRIGGER");
        }
        if ((constraints & 256) != 0) {
            pw.print(" DEVICE_NOT_DOZING");
        }
        if ((constraints & 1024) != 0) {
            pw.print(" BATT_IDLE");
        }
        if ((constraints & 2048) != 0) {
            pw.print(" PROTECT_FORE");
        }
        if ((constraints & 4096) != 0) {
            pw.print(" CPU");
        }
    }

    public void dump(PrintWriter pw, String prefix, boolean full) {
        int i;
        pw.print(prefix);
        UserHandle.formatUid(pw, this.callingUid);
        pw.print(" tag=");
        pw.println(this.tag);
        pw.print(prefix);
        pw.print("Source: uid=");
        UserHandle.formatUid(pw, getSourceUid());
        pw.print(" user=");
        pw.print(getSourceUserId());
        pw.print(" pkg=");
        pw.println(getSourcePackageName());
        if (full) {
            pw.print(prefix);
            pw.println("JobInfo:");
            pw.print(prefix);
            pw.print("  Service: ");
            pw.println(this.job.getService().flattenToShortString());
            if (this.job.isPeriodic()) {
                pw.print(prefix);
                pw.print("  PERIODIC: interval=");
                TimeUtils.formatDuration(this.job.getIntervalMillis(), pw);
                pw.print(" flex=");
                TimeUtils.formatDuration(this.job.getFlexMillis(), pw);
                pw.println();
            }
            if (this.job.isPersisted()) {
                pw.print(prefix);
                pw.println("  PERSISTED");
            }
            if (this.job.getPriority() != 0) {
                pw.print(prefix);
                pw.print("  Priority: ");
                pw.println(this.job.getPriority());
            }
            if (this.job.getFlags() != 0) {
                pw.print(prefix);
                pw.print("  Flags: ");
                pw.println(Integer.toHexString(this.job.getFlags()));
            }
            pw.print(prefix);
            pw.print("  Requires: charging=");
            pw.print(this.job.isRequireCharging());
            pw.print(" deviceIdle=");
            pw.println(this.job.isRequireDeviceIdle());
            if (this.job.getTriggerContentUris() != null) {
                pw.print(prefix);
                pw.println("  Trigger content URIs:");
                for (TriggerContentUri trig : this.job.getTriggerContentUris()) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print(Integer.toHexString(trig.getFlags()));
                    pw.print(' ');
                    pw.println(trig.getUri());
                }
                if (this.job.getTriggerContentUpdateDelay() >= 0) {
                    pw.print(prefix);
                    pw.print("  Trigger update delay: ");
                    TimeUtils.formatDuration(this.job.getTriggerContentUpdateDelay(), pw);
                    pw.println();
                }
                if (this.job.getTriggerContentMaxDelay() >= 0) {
                    pw.print(prefix);
                    pw.print("  Trigger max delay: ");
                    TimeUtils.formatDuration(this.job.getTriggerContentMaxDelay(), pw);
                    pw.println();
                }
            }
            if (this.job.getNetworkType() != 0) {
                pw.print(prefix);
                pw.print("  Network type: ");
                pw.println(this.job.getNetworkType());
            }
            if (this.job.getMinLatencyMillis() != 0) {
                pw.print(prefix);
                pw.print("  Minimum latency: ");
                TimeUtils.formatDuration(this.job.getMinLatencyMillis(), pw);
                pw.println();
            }
            if (this.job.getMaxExecutionDelayMillis() != 0) {
                pw.print(prefix);
                pw.print("  Max execution delay: ");
                TimeUtils.formatDuration(this.job.getMaxExecutionDelayMillis(), pw);
                pw.println();
            }
            pw.print(prefix);
            pw.print("  Backoff: policy=");
            pw.print(this.job.getBackoffPolicy());
            pw.print(" initial=");
            TimeUtils.formatDuration(this.job.getInitialBackoffMillis(), pw);
            pw.println();
            if (this.job.hasEarlyConstraint()) {
                pw.print(prefix);
                pw.println("  Has early constraint");
            }
            if (this.job.hasLateConstraint()) {
                pw.print(prefix);
                pw.println("  Has late constraint");
            }
        }
        pw.print(prefix);
        pw.print("Required constraints:");
        dumpConstraints(pw, this.requiredConstraints);
        pw.println();
        if (full) {
            pw.print(prefix);
            pw.print("Satisfied constraints:");
            dumpConstraints(pw, this.satisfiedConstraints);
            pw.println();
            pw.print(prefix);
            pw.print("Unsatisfied constraints:");
            dumpConstraints(pw, this.requiredConstraints & (~this.satisfiedConstraints));
            pw.println();
            if (this.dozeWhitelisted) {
                pw.print(prefix);
                pw.println("Doze whitelisted: true");
            }
        }
        if (this.changedAuthorities != null) {
            pw.print(prefix);
            pw.println("Changed authorities:");
            for (i = 0; i < this.changedAuthorities.size(); i++) {
                pw.print(prefix);
                pw.print("  ");
                pw.println((String) this.changedAuthorities.valueAt(i));
            }
            if (this.changedUris != null) {
                pw.print(prefix);
                pw.println("Changed URIs:");
                for (i = 0; i < this.changedUris.size(); i++) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(this.changedUris.valueAt(i));
                }
            }
        }
        pw.print(prefix);
        pw.print("Earliest run time: ");
        pw.println(formatRunTime(this.earliestRunTimeElapsedMillis, 0));
        pw.print(prefix);
        pw.print("Latest run time: ");
        pw.println(formatRunTime(this.latestRunTimeElapsedMillis, NO_LATEST_RUNTIME));
        if (this.numFailures != 0) {
            pw.print(prefix);
            pw.print("Num failures: ");
            pw.println(this.numFailures);
        }
    }
}
