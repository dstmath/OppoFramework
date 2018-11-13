package com.android.server.connectivity.tethering;

final /* synthetic */ class -$Lambda$M3tXj934m-dXV_AxdqUj05-IfpI implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f213-$f0;

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$M3tXj934m-dXV_AxdqUj05-IfpI$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ long f214-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f215-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f216-$f2;

        private final /* synthetic */ void $m$0() {
            ((OffloadTetheringStatsProvider) this.f215-$f1).m161x79328576(this.f214-$f0, (String) this.f216-$f2);
        }

        public /* synthetic */ AnonymousClass1(long j, Object obj, Object obj2) {
            this.f214-$f0 = j;
            this.f215-$f1 = obj;
            this.f216-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((OffloadTetheringStatsProvider) this.f213-$f0).m160x793213de();
    }

    public /* synthetic */ -$Lambda$M3tXj934m-dXV_AxdqUj05-IfpI(Object obj) {
        this.f213-$f0 = obj;
    }

    public final void run() {
        $m$0();
    }
}
