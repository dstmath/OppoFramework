package com.android.server.wifi;

import android.net.wifi.ScanResult;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.-$$Lambda$OppoWifiNetworkFactory$sb_l6LjQ9B2ghq3sXTwvQVdeRx8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OppoWifiNetworkFactory$sb_l6LjQ9B2ghq3sXTwvQVdeRx8 implements Function {
    public static final /* synthetic */ $$Lambda$OppoWifiNetworkFactory$sb_l6LjQ9B2ghq3sXTwvQVdeRx8 INSTANCE = new $$Lambda$OppoWifiNetworkFactory$sb_l6LjQ9B2ghq3sXTwvQVdeRx8();

    private /* synthetic */ $$Lambda$OppoWifiNetworkFactory$sb_l6LjQ9B2ghq3sXTwvQVdeRx8() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((ScanResult) obj).level);
    }
}
