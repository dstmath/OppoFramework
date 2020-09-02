package com.android.server.location;

public abstract class OppoNavigationStatusMonitor {
    public abstract int getNavigateMode();

    /* access modifiers changed from: protected */
    public abstract void init(OppoMotionConfig oppoMotionConfig);

    public abstract void resetStatus();

    public abstract void setDebug(boolean z);

    public abstract void startMonitor();

    public abstract void stopMonitor();
}
