package com.android.server.wifi;

import android.os.IBinder.DeathRecipient;

final /* synthetic */ class -$Lambda$RPzhR64WIMgTSfYC8KVwFhmhzoc implements DeathRecipient {
    /* renamed from: -$f0 */
    private final /* synthetic */ int f2-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f3-$f1;

    private final /* synthetic */ void $m$0() {
        ((RttServiceImpl) this.f3-$f1).m28lambda$-com_android_server_wifi_RttService$RttServiceImpl_1874(this.f2-$f0);
    }

    public /* synthetic */ -$Lambda$RPzhR64WIMgTSfYC8KVwFhmhzoc(int i, Object obj) {
        this.f2-$f0 = i;
        this.f3-$f1 = obj;
    }

    public final void binderDied() {
        $m$0();
    }
}
