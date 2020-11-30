package com.android.server.job;

import android.app.job.JobInfo;
import android.content.Context;

public interface IJobCountPolicy {
    public static final String NAME = "JobCountPolicy";

    void handleAbnormalJobCount(Context context, JobStore jobStore, int i, int i2);

    void startSystemJobSameIdMonitor(Context context, JobInfo jobInfo, int i);
}
