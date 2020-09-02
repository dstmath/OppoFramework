package com.android.server;

import com.android.server.MtkConnectivityService;
import java.util.function.ToIntFunction;

/* renamed from: com.android.server.-$$Lambda$MtkConnectivityService$-MKKdpO7XQK1qJQw2DuuYW6-L6E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MtkConnectivityService$MKKdpO7XQK1qJQw2DuuYW6L6E implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$MtkConnectivityService$MKKdpO7XQK1qJQw2DuuYW6L6E INSTANCE = new $$Lambda$MtkConnectivityService$MKKdpO7XQK1qJQw2DuuYW6L6E();

    private /* synthetic */ $$Lambda$MtkConnectivityService$MKKdpO7XQK1qJQw2DuuYW6L6E() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((MtkConnectivityService.NetworkRequestInfo) obj).request.requestId;
    }
}
