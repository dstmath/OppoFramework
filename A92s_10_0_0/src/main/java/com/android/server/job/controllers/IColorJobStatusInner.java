package com.android.server.job.controllers;

import android.app.job.JobInfo;

public interface IColorJobStatusInner {
    default JobInfo getJobInfo() {
        return null;
    }

    default int getIntRequiredConstraintsVal() {
        return 0;
    }

    default int getIntSatisfiedConstraintsVal() {
        return 0;
    }

    default int getIntConstraintsOfInterestVal() {
        return 0;
    }

    default int getIntSoftOverrideConstraintsVal() {
        return 0;
    }
}
