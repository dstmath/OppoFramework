package com.android.server;

import android.content.Context;
import android.net.Network;

final /* synthetic */ class -$Lambda$Ganck_s9Kl5o2K6eVDoQTKLc-6g implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f21-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f22-$f1;

    private final /* synthetic */ void $m$0() {
        ((ConnectivityService) this.f21-$f0).m5lambda$-com_android_server_ConnectivityService_119811((Network) this.f22-$f1);
    }

    private final /* synthetic */ void $m$1() {
        ((ContextHubSystemService) this.f21-$f0).m0lambda$-com_android_server_ContextHubSystemService_1237((Context) this.f22-$f1);
    }

    private final /* synthetic */ void $m$2() {
        SystemServerInitThreadPool.m6lambda$-com_android_server_SystemServerInitThreadPool_2249((String) this.f21-$f0, (Runnable) this.f22-$f1);
    }

    public /* synthetic */ -$Lambda$Ganck_s9Kl5o2K6eVDoQTKLc-6g(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f21-$f0 = obj;
        this.f22-$f1 = obj2;
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
