package android.net.ip;

import android.net.ip.IpManager.AnonymousClass2;

final /* synthetic */ class -$Lambda$EIaFPv5OO8Upo9X60vMtrcUNUEQ implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f41-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f42-$f1;

    private final /* synthetic */ void $m$0() {
        ((PacketListener) this.f41-$f0).m95x5922028((String) this.f42-$f1);
    }

    private final /* synthetic */ void $m$1() {
        ((AnonymousClass2) this.f41-$f0).m98lambda$-android_net_ip_IpManager$2_26954((String) this.f42-$f1);
    }

    public /* synthetic */ -$Lambda$EIaFPv5OO8Upo9X60vMtrcUNUEQ(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f41-$f0 = obj;
        this.f42-$f1 = obj2;
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
