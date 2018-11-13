package com.android.server.wifi;

import java.util.ArrayList;

final /* synthetic */ class -$Lambda$ajG-TMshB5D_BrXpSiGRWlXJ7-M implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ int f45-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f46-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f47-$f2;

    private final /* synthetic */ void $m$0() {
        ((WifiServiceImpl) this.f46-$f1).lambda$-com_android_server_wifi_WifiServiceImpl_47016((String) this.f47-$f2, this.f45-$f0);
    }

    private final /* synthetic */ void $m$1() {
        ((ChipEventCallback) this.f46-$f1).m47x8c753766((ArrayList) this.f47-$f2, this.f45-$f0);
    }

    public /* synthetic */ -$Lambda$ajG-TMshB5D_BrXpSiGRWlXJ7-M(byte b, int i, Object obj, Object obj2) {
        this.$id = b;
        this.f45-$f0 = i;
        this.f46-$f1 = obj;
        this.f47-$f2 = obj2;
    }

    public final void run() {
        switch (this.$id) {
            case (byte) 0:
                $m$0();
                return;
            case (byte) 1:
                $m$1();
                return;
            default:
                throw new AssertionError();
        }
    }
}
