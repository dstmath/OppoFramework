package com.android.server.wifi;

import com.android.server.wifi.WifiNative.VendorHalDeathEventHandler;

final /* synthetic */ class -$Lambda$YuIVlKWZZmb4gGMvJqVJEVQ4abs implements VendorHalDeathEventHandler {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f4-$f0;

    private final /* synthetic */ void $m$0() {
        ((WifiStateMachine) this.f4-$f0).m93lambda$-com_android_server_wifi_WifiStateMachine_13337();
    }

    public /* synthetic */ -$Lambda$YuIVlKWZZmb4gGMvJqVJEVQ4abs(Object obj) {
        this.f4-$f0 = obj;
    }

    public final void onDeath() {
        $m$0();
    }
}
