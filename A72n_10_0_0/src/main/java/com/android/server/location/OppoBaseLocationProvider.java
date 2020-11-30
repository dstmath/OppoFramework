package com.android.server.location;

public abstract class OppoBaseLocationProvider {
    public abstract boolean isNetworkWhiteList(String str);

    public abstract void transProviderStatusToMonitor(boolean z);
}
