package com.android.server.job;

public interface IColorJobSchedulerServiceInner {
    default int getMsgJobExpiredValue() {
        return 0;
    }
}
