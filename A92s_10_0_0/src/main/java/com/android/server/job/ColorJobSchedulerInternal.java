package com.android.server.job;

public interface ColorJobSchedulerInternal {
    public static final int PENDING_FAIL = 1;
    public static final int PENDING_PROCESSING = 2;
    public static final int RESTORE_IGNORE = 1;
    public static final int RESTORE_SUCCESS = 2;

    default int pendingJobs(int uid) {
        return 1;
    }

    default int restoreJobs(int uid) {
        return 1;
    }
}
