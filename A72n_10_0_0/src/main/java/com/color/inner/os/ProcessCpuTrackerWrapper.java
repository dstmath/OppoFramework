package com.color.inner.os;

import com.android.internal.os.ProcessCpuTracker;

public class ProcessCpuTrackerWrapper {
    private ProcessCpuTracker mProcessCpuTracker;

    public ProcessCpuTrackerWrapper(boolean includeThreads) {
        this.mProcessCpuTracker = new ProcessCpuTracker(includeThreads);
    }

    public void update() {
        this.mProcessCpuTracker.update();
    }

    public String printCurrentState(long now) {
        return this.mProcessCpuTracker.printCurrentState(now);
    }

    public String printCurrentLoad() {
        return this.mProcessCpuTracker.printCurrentLoad();
    }
}
