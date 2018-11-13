package com.android.server.fingerprint;

final /* synthetic */ class -$Lambda$NsdFXKe2P39OH-qCAY_zqOMIIsg implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f246-$f0;

    private final /* synthetic */ void $m$0() {
        ((FingerprintService) this.f246-$f0).-com_android_server_fingerprint_FingerprintService-mthref-0();
    }

    private final /* synthetic */ void $m$1() {
        ((FingerprintService) this.f246-$f0).-com_android_server_fingerprint_FingerprintService-mthref-1();
    }

    public /* synthetic */ -$Lambda$NsdFXKe2P39OH-qCAY_zqOMIIsg(byte b, Object obj) {
        this.$id = b;
        this.f246-$f0 = obj;
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
