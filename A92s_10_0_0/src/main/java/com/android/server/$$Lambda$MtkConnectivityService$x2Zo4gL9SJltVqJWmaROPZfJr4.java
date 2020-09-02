package com.android.server;

import com.android.server.connectivity.NetworkAgentInfo;
import java.util.function.ToIntFunction;

/* renamed from: com.android.server.-$$Lambda$MtkConnectivityService$x2Zo4gL9SJltVq-JWmaROPZfJr4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MtkConnectivityService$x2Zo4gL9SJltVqJWmaROPZfJr4 implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$MtkConnectivityService$x2Zo4gL9SJltVqJWmaROPZfJr4 INSTANCE = new $$Lambda$MtkConnectivityService$x2Zo4gL9SJltVqJWmaROPZfJr4();

    private /* synthetic */ $$Lambda$MtkConnectivityService$x2Zo4gL9SJltVqJWmaROPZfJr4() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((NetworkAgentInfo) obj).network.netId;
    }
}
