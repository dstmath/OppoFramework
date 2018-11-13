package com.android.server.policy;

final /* synthetic */ class -$Lambda$pV_TcBBXJOcgD8CpVRVZuDc_ff8 implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f347-$f0;

    private final /* synthetic */ void $m$0() {
        ((GlobalActions) this.f347-$f0).-com_android_server_policy_GlobalActions-mthref-0();
    }

    private final /* synthetic */ void $m$1() {
        ((PhoneWindowManager) this.f347-$f0).m39lambda$-com_android_server_policy_PhoneWindowManager_65786();
    }

    private final /* synthetic */ void $m$2() {
        ((PhoneWindowManager) this.f347-$f0).m40lambda$-com_android_server_policy_PhoneWindowManager_66054();
    }

    public /* synthetic */ -$Lambda$pV_TcBBXJOcgD8CpVRVZuDc_ff8(byte b, Object obj) {
        this.$id = b;
        this.f347-$f0 = obj;
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
            default:
                throw new AssertionError();
        }
    }
}
