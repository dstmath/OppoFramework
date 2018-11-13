package com.android.server.locksettings;

import android.app.admin.PasswordMetrics;

final /* synthetic */ class -$Lambda$uuAdbltCNvfImff6TxhVt9IC9Qw implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ int f256-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f257-$f1;

    /* renamed from: com.android.server.locksettings.-$Lambda$uuAdbltCNvfImff6TxhVt9IC9Qw$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f258-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f259-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f260-$f2;

        private final /* synthetic */ void $m$0() {
            ((LockSettingsService) this.f259-$f1).m175x9ba2ae4f((PasswordMetrics) this.f260-$f2, this.f258-$f0);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj, Object obj2) {
            this.f258-$f0 = i;
            this.f259-$f1 = obj;
            this.f260-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((LockSettingsService) this.f257-$f1).m176x9ba3130d(this.f256-$f0);
    }

    public /* synthetic */ -$Lambda$uuAdbltCNvfImff6TxhVt9IC9Qw(int i, Object obj) {
        this.f256-$f0 = i;
        this.f257-$f1 = obj;
    }

    public final void run() {
        $m$0();
    }
}
