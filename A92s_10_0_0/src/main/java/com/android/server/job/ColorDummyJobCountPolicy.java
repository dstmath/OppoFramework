package com.android.server.job;

import android.app.job.JobInfo;
import android.content.Context;
import android.util.Slog;

public class ColorDummyJobCountPolicy implements IJobCountPolicy {
    @Override // com.android.server.job.IJobCountPolicy
    public void handleAbnormalJobCount(Context context, JobStore jobs, int uid, int maxCount) {
        Slog.w(JobSchedulerService.TAG, "Too many jobs for uid " + uid);
        throw new IllegalStateException("Apps may not schedule more than " + maxCount + " distinct jobs");
    }

    @Override // com.android.server.job.IJobCountPolicy
    public void startSystemJobSameIdMonitor(Context context, JobInfo job, int uid) {
    }
}
