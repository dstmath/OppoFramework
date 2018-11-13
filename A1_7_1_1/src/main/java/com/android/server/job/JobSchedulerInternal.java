package com.android.server.job;

import android.app.job.JobInfo;
import java.util.List;

public interface JobSchedulerInternal {
    void cancelJobsForUid(int i);

    List<JobInfo> getSystemScheduledPendingJobs();
}
