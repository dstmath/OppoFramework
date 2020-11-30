package com.android.server.am;

public abstract class OppoBaseHostingRecord {
    private boolean mIsRPLaunch;
    private long mOrder;

    public boolean isRPLaunch() {
        return this.mIsRPLaunch;
    }

    public void setRPLaunch(boolean rpLaunch) {
        this.mIsRPLaunch = rpLaunch;
    }

    public long getOrder() {
        return this.mOrder;
    }

    public void setOrder(long order) {
        this.mOrder = order;
    }
}
