package com.android.server.autofill;

import android.content.IntentSender;
import android.os.ICancellationSignal;

final /* synthetic */ class -$Lambda$tQjSpU6IVjrOfYzILn21rTYl4Vo implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f118-$f0;

    /* renamed from: com.android.server.autofill.-$Lambda$tQjSpU6IVjrOfYzILn21rTYl4Vo$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f119-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f120-$f1;

        private final /* synthetic */ void $m$0() {
            ((RemoteFillService) this.f119-$f0).m118lambda$-com_android_server_autofill_RemoteFillService_11852((PendingRequest) this.f120-$f1);
        }

        private final /* synthetic */ void $m$1() {
            ((Session) this.f119-$f0).m125lambda$-com_android_server_autofill_Session_27469((IntentSender) this.f120-$f1);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj, Object obj2) {
            this.$id = b;
            this.f119-$f0 = obj;
            this.f120-$f1 = obj2;
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

    private final /* synthetic */ void $m$0() {
        ((PendingRequest) this.f118-$f0).m120x7a9992cd();
    }

    private final /* synthetic */ void $m$1() {
        RemoteFillService.m115lambda$-com_android_server_autofill_RemoteFillService_11516((ICancellationSignal) this.f118-$f0);
    }

    private final /* synthetic */ void $m$2() {
        ((Session) this.f118-$f0).lambda$-com_android_server_autofill_Session_25476();
    }

    public /* synthetic */ -$Lambda$tQjSpU6IVjrOfYzILn21rTYl4Vo(byte b, Object obj) {
        this.$id = b;
        this.f118-$f0 = obj;
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
