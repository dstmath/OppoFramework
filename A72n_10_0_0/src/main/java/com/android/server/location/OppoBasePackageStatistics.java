package com.android.server.location;

public abstract class OppoBasePackageStatistics {
    public boolean mIsRecord = false;
    public long mLastDurationMs = 0;

    /* access modifiers changed from: package-private */
    public abstract long getLastDurationMs();

    OppoBasePackageStatistics() {
    }

    public void setRecord(boolean record) {
        this.mIsRecord = record;
    }

    public boolean isRecord() {
        return this.mIsRecord;
    }
}
