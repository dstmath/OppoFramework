package com.android.server.utils;

final /* synthetic */ class -$Lambda$luWxpSWBY1-S73qs-S0xFqWHvIs implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f355-$f0;

    /* renamed from: com.android.server.utils.-$Lambda$luWxpSWBY1-S73qs-S0xFqWHvIs$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ long f356-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f357-$f1;

        private final /* synthetic */ void $m$0() {
            ((com.android.server.utils.ManagedApplicationService.AnonymousClass1) this.f357-$f1).m91xbff5c2e4(this.f356-$f0);
        }

        private final /* synthetic */ void $m$1() {
            ((com.android.server.utils.ManagedApplicationService.AnonymousClass1) this.f357-$f1).m92xbff633f9(this.f356-$f0);
        }

        private final /* synthetic */ void $m$2() {
            ((com.android.server.utils.ManagedApplicationService.AnonymousClass1) this.f357-$f1).m93xbff718fc(this.f356-$f0);
        }

        private final /* synthetic */ void $m$3() {
            ((ManagedApplicationService) this.f357-$f1).m90lambda$-com_android_server_utils_ManagedApplicationService_17383(this.f356-$f0);
        }

        public /* synthetic */ AnonymousClass1(byte b, long j, Object obj) {
            this.$id = b;
            this.f356-$f0 = j;
            this.f357-$f1 = obj;
        }

        public final void run() {
            switch (this.$id) {
                case (byte) 0:
                    $m$0();
                    return;
                case (byte) 1:
                    $m$1();
                    return;
                case (byte) 2:
                    $m$2();
                    return;
                case (byte) 3:
                    $m$3();
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private final /* synthetic */ void $m$0() {
        ((ManagedApplicationService) this.f355-$f0).-com_android_server_utils_ManagedApplicationService-mthref-0();
    }

    public /* synthetic */ -$Lambda$luWxpSWBY1-S73qs-S0xFqWHvIs(Object obj) {
        this.f355-$f0 = obj;
    }

    public final void run() {
        $m$0();
    }
}
