package com.android.server.autofill;

import android.service.autofill.FillResponse;

final /* synthetic */ class -$Lambda$JYqZriexGNVTrQ5cwTlcgjPSZFY implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f97-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f98-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f99-$f2;

    /* renamed from: com.android.server.autofill.-$Lambda$JYqZriexGNVTrQ5cwTlcgjPSZFY$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f100-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f101-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f102-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f103-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f104-$f4;

        private final /* synthetic */ void $m$0() {
            ((RemoteFillService) this.f102-$f2).m116lambda$-com_android_server_autofill_RemoteFillService_10790((PendingRequest) this.f103-$f3, this.f100-$f0, (FillResponse) this.f104-$f4, this.f101-$f1);
        }

        public /* synthetic */ AnonymousClass1(int i, int i2, Object obj, Object obj2, Object obj3) {
            this.f100-$f0 = i;
            this.f101-$f1 = i2;
            this.f102-$f2 = obj;
            this.f103-$f3 = obj2;
            this.f104-$f4 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((RemoteFillService) this.f97-$f0).m117lambda$-com_android_server_autofill_RemoteFillService_11195((PendingRequest) this.f98-$f1, (CharSequence) this.f99-$f2);
    }

    private final /* synthetic */ void $m$1() {
        ((RemoteFillService) this.f97-$f0).m119lambda$-com_android_server_autofill_RemoteFillService_12197((PendingRequest) this.f98-$f1, (CharSequence) this.f99-$f2);
    }

    public /* synthetic */ -$Lambda$JYqZriexGNVTrQ5cwTlcgjPSZFY(byte b, Object obj, Object obj2, Object obj3) {
        this.$id = b;
        this.f97-$f0 = obj;
        this.f98-$f1 = obj2;
        this.f99-$f2 = obj3;
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
