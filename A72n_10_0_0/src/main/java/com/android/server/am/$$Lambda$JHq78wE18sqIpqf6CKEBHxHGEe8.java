package com.android.server.am;

import com.android.internal.os.OppoBatteryStatsImpl;
import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.am.-$$Lambda$JHq78wE18sqIpqf6CKEBHxHGEe8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$JHq78wE18sqIpqf6CKEBHxHGEe8 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$JHq78wE18sqIpqf6CKEBHxHGEe8 INSTANCE = new $$Lambda$JHq78wE18sqIpqf6CKEBHxHGEe8();

    private /* synthetic */ $$Lambda$JHq78wE18sqIpqf6CKEBHxHGEe8() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((OppoBatteryStatsImpl) obj).updateProcStateCpuTimes(((Boolean) obj2).booleanValue(), ((Boolean) obj3).booleanValue());
    }
}
