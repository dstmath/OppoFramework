package com.android.server.job;

import android.content.Context;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.OppoBaseJobStatus;
import com.color.util.ColorTypeCastingHelper;

public class OppoJobSchedulerService extends JobSchedulerService {
    static final int MIN_CPU_COUNT = 1;
    static final int MIN_FORE_COUNT = 1;
    private static final String TAG = "JobScheduler";
    int cpuCount = 0;
    int foreCount = 0;

    public OppoJobSchedulerService(Context context) {
        super(context);
    }

    @Override // com.android.server.job.OppoBaseJobSchedulerService
    public void acceptForMaybeReadyJobQueueFunctor(JobStatus job) {
        if (typeCasting(job) != null && typeCasting(job).hasCpuConstraint()) {
            this.cpuCount++;
        }
        if (typeCasting(job) != null && typeCasting(job).hasProtectForeConstraint()) {
            this.foreCount++;
        }
    }

    @Override // com.android.server.job.OppoBaseJobSchedulerService
    public boolean readyForPostProcess() {
        return this.foreCount >= 1 || this.cpuCount >= 1;
    }

    @Override // com.android.server.job.OppoBaseJobSchedulerService
    public void resetForMaybeReadyJobQueueFunctor() {
        this.foreCount = 0;
        this.cpuCount = 0;
    }

    public boolean isRunningHighCpuJobs() {
        synchronized (this.mJobs) {
            for (int i = 0; i < this.mActiveServices.size(); i++) {
                JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                if (jsc.getRunningJobLocked() != null) {
                    JobStatus js = jsc.getRunningJobLocked();
                    if (typeCasting(js) != null && typeCasting(js).hasCpuConstraint()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static OppoBaseJobStatus typeCasting(JobStatus jobStatus) {
        return (OppoBaseJobStatus) ColorTypeCastingHelper.typeCasting(OppoBaseJobStatus.class, jobStatus);
    }
}
