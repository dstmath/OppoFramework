package com.android.server.am;

import com.android.server.am.UserController.AnonymousClass4;

final /* synthetic */ class -$Lambda$5yQSwWrsRDcxoFuTXgyaBIqPvDw implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ boolean f90-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ int f91-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f92-$f2;

    private final /* synthetic */ void $m$0() {
        ((ActivityStackSupervisor) this.f92-$f2).lambda$-com_android_server_am_ActivityStackSupervisor_125516(this.f91-$f1, this.f90-$f0);
    }

    private final /* synthetic */ void $m$1() {
        ((AnonymousClass4) this.f92-$f2).m34lambda$-com_android_server_am_UserController$4_25326(this.f91-$f1, this.f90-$f0);
    }

    public /* synthetic */ -$Lambda$5yQSwWrsRDcxoFuTXgyaBIqPvDw(byte b, boolean z, int i, Object obj) {
        this.$id = b;
        this.f90-$f0 = z;
        this.f91-$f1 = i;
        this.f92-$f2 = obj;
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
