package com.android.server.job;

import android.app.job.IJobService;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.OppoBaseJobInfo;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.BatteryManagerInternal;
import com.android.internal.app.IBatteryStats;
import com.android.server.job.controllers.JobStatus;
import com.android.server.oppo.TemperatureProvider;
import com.color.util.ColorTypeCastingHelper;
import java.util.HashMap;
import java.util.Map;
import oppo.util.OppoStatistics;

public abstract class OppoBaseJobServiceContext {
    protected static final boolean DCS_ENABLED = false;
    protected BatteryManagerInternal mBatteryManagerInternal;
    private final IBatteryStats mBatteryStats;
    private final Context mContext;
    private final JobPackageTracker mJobPackageTracker;
    private final Object mLock;

    /* access modifiers changed from: package-private */
    public abstract JobParameters getJobParameters();

    /* access modifiers changed from: package-private */
    public abstract JobStatus getJobStatus();

    /* access modifiers changed from: package-private */
    public abstract IJobService getService();

    /* access modifiers changed from: package-private */
    public abstract int getVerb();

    public OppoBaseJobServiceContext(Context context, Object lock, IBatteryStats batteryStats, JobPackageTracker tracker) {
        this.mContext = context;
        this.mLock = lock;
        this.mBatteryStats = batteryStats;
        this.mJobPackageTracker = tracker;
    }

    /* access modifiers changed from: protected */
    public void uploadJobExecute(JobStatus job, boolean isDeadlineExpired) {
        JobInfo jobInfo = job.getJob();
        OppoBaseJobInfo baseJobInfo = typeCasting(jobInfo);
        if (jobInfo != null && baseJobInfo != null && jobInfo.getService() != null && jobInfo.getService().getPackageName() != null) {
            Map<String, String> eventMap = new HashMap<>();
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
            if (baseJobInfo.isRequireBattIdle()) {
                eventMap.put("battIdle", TemperatureProvider.SWITCH_ON);
            }
            if (jobInfo.isRequireCharging()) {
                eventMap.put("charging", TemperatureProvider.SWITCH_ON);
            }
            if (jobInfo.getNetworkType() != 0) {
                eventMap.put("connectivity", TemperatureProvider.SWITCH_ON);
            }
            if (jobInfo.isRequireDeviceIdle()) {
                eventMap.put("idle", TemperatureProvider.SWITCH_ON);
            }
            if (isDeadlineExpired) {
                eventMap.put("isDeadlineExpired", TemperatureProvider.SWITCH_ON);
            }
            OppoStatistics.onCommon(this.mContext, "20120008", "jobscheduler_execute", eventMap, false);
        }
    }

    /* access modifiers changed from: protected */
    public void uploadJObTimeOut(int verb) {
        JobInfo jobInfo;
        String pkgname;
        String state;
        if (getJobStatus() != null && (jobInfo = getJobStatus().getJob()) != null && jobInfo.getService() != null && (pkgname = jobInfo.getService().getPackageName()) != null) {
            if (verb == 0) {
                state = "binding";
            } else if (verb == 1) {
                state = "starting";
            } else if (verb == 2) {
                state = "executing";
            } else if (verb != 3) {
                state = null;
            } else {
                state = "stoping";
            }
            if (state != null) {
                Map<String, String> eventMap = new HashMap<>();
                eventMap.put("pkgname", pkgname);
                eventMap.put("jobId", String.valueOf(getJobStatus().getJobId()));
                eventMap.put("state", state);
                if (jobInfo.getService().flattenToShortString() != null) {
                    eventMap.put("componentName", jobInfo.getService().flattenToShortString());
                }
                OppoStatistics.onCommon(this.mContext, "20120008", "jobscheduler_timeout", eventMap, false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateExecutingParameter(int level) {
        return OppoFeatureCache.get(IColorJobScheduleManager.DEFAULT).updateExecutingParameter(getService(), this.mLock, getJobParameters(), getJobStatus(), level, getVerb());
    }

    private static OppoBaseJobInfo typeCasting(JobInfo jobInfo) {
        if (jobInfo != null) {
            return (OppoBaseJobInfo) ColorTypeCastingHelper.typeCasting(OppoBaseJobInfo.class, jobInfo);
        }
        return null;
    }
}
