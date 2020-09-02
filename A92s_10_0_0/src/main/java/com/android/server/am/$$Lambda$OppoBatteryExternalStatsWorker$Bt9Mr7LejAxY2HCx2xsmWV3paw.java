package com.android.server.am;

import java.util.concurrent.ThreadFactory;

/* renamed from: com.android.server.am.-$$Lambda$OppoBatteryExternalStatsWorker$Bt9Mr7LejAxY2HCx2xsmWV3p-aw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OppoBatteryExternalStatsWorker$Bt9Mr7LejAxY2HCx2xsmWV3paw implements ThreadFactory {
    public static final /* synthetic */ $$Lambda$OppoBatteryExternalStatsWorker$Bt9Mr7LejAxY2HCx2xsmWV3paw INSTANCE = new $$Lambda$OppoBatteryExternalStatsWorker$Bt9Mr7LejAxY2HCx2xsmWV3paw();

    private /* synthetic */ $$Lambda$OppoBatteryExternalStatsWorker$Bt9Mr7LejAxY2HCx2xsmWV3paw() {
    }

    public final Thread newThread(Runnable runnable) {
        return OppoBatteryExternalStatsWorker.lambda$new$1(runnable);
    }
}
