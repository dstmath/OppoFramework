package com.android.server;

import java.util.Comparator;
import java.util.Map;

/* renamed from: com.android.server.-$$Lambda$OppoNetworkStackService$-kZRyor1pAhzCQcPgME4Ivn8-H4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OppoNetworkStackService$kZRyor1pAhzCQcPgME4Ivn8H4 implements Comparator {
    public static final /* synthetic */ $$Lambda$OppoNetworkStackService$kZRyor1pAhzCQcPgME4Ivn8H4 INSTANCE = new $$Lambda$OppoNetworkStackService$kZRyor1pAhzCQcPgME4Ivn8H4();

    private /* synthetic */ $$Lambda$OppoNetworkStackService$kZRyor1pAhzCQcPgME4Ivn8H4() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return ((Integer) ((Map.Entry) obj).getValue()).compareTo((Integer) ((Map.Entry) obj2).getValue());
    }
}
