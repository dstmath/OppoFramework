package com.android.server.am;

import com.android.internal.os.OppoBatteryStatsImpl;
import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.am.-$$Lambda$peAozoNuO5jEUjYVHIqlPdoTWYI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$peAozoNuO5jEUjYVHIqlPdoTWYI implements TriConsumer {
    public static final /* synthetic */ $$Lambda$peAozoNuO5jEUjYVHIqlPdoTWYI INSTANCE = new $$Lambda$peAozoNuO5jEUjYVHIqlPdoTWYI();

    private /* synthetic */ $$Lambda$peAozoNuO5jEUjYVHIqlPdoTWYI() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((OppoBatteryStatsImpl) obj).copyFromAllUidsCpuTimes(((Boolean) obj2).booleanValue(), ((Boolean) obj3).booleanValue());
    }
}
