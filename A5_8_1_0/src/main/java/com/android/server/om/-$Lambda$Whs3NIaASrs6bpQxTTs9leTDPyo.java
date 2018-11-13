package com.android.server.om;

final /* synthetic */ class -$Lambda$Whs3NIaASrs6bpQxTTs9leTDPyo implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f282-$f0;

    /* renamed from: com.android.server.om.-$Lambda$Whs3NIaASrs6bpQxTTs9leTDPyo$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f283-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f284-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f285-$f2;

        private final /* synthetic */ void $m$0() {
            ((OverlayChangeListener) this.f284-$f1).m191x4eedba5b(this.f283-$f0, (String) this.f285-$f2);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj, Object obj2) {
            this.f283-$f0 = i;
            this.f284-$f1 = obj;
            this.f285-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((OverlayManagerService) this.f282-$f0).m190lambda$-com_android_server_om_OverlayManagerService_9907();
    }

    private final /* synthetic */ void $m$1() {
        ((OverlayManagerService) this.f282-$f0).m189lambda$-com_android_server_om_OverlayManagerService_31347();
    }

    public /* synthetic */ -$Lambda$Whs3NIaASrs6bpQxTTs9leTDPyo(byte b, Object obj) {
        this.$id = b;
        this.f282-$f0 = obj;
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
