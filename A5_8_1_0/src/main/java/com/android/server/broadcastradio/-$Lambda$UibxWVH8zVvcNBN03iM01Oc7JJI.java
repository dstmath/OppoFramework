package com.android.server.broadcastradio;

import android.hardware.radio.RadioManager.BandConfig;
import android.hardware.radio.RadioManager.ProgramInfo;

final /* synthetic */ class -$Lambda$UibxWVH8zVvcNBN03iM01Oc7JJI implements RunnableThrowingRemoteException {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f173-$f0;

    /* renamed from: com.android.server.broadcastradio.-$Lambda$UibxWVH8zVvcNBN03iM01Oc7JJI$1 */
    final /* synthetic */ class AnonymousClass1 implements RunnableThrowingRemoteException {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f174-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f175-$f1;

        private final /* synthetic */ void $m$0() {
            ((TunerCallback) this.f174-$f0).m53lambda$-com_android_server_broadcastradio_TunerCallback_2650((BandConfig) this.f175-$f1);
        }

        private final /* synthetic */ void $m$1() {
            ((TunerCallback) this.f174-$f0).m54lambda$-com_android_server_broadcastradio_TunerCallback_2820((ProgramInfo) this.f175-$f1);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj, Object obj2) {
            this.$id = b;
            this.f174-$f0 = obj;
            this.f175-$f1 = obj2;
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

    /* renamed from: com.android.server.broadcastradio.-$Lambda$UibxWVH8zVvcNBN03iM01Oc7JJI$2 */
    final /* synthetic */ class AnonymousClass2 implements RunnableThrowingRemoteException {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f176-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f177-$f1;

        private final /* synthetic */ void $m$0() {
            ((TunerCallback) this.f177-$f1).m52lambda$-com_android_server_broadcastradio_TunerCallback_2499(this.f176-$f0);
        }

        public /* synthetic */ AnonymousClass2(int i, Object obj) {
            this.f176-$f0 = i;
            this.f177-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.broadcastradio.-$Lambda$UibxWVH8zVvcNBN03iM01Oc7JJI$3 */
    final /* synthetic */ class AnonymousClass3 implements RunnableThrowingRemoteException {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f178-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f179-$f1;

        private final /* synthetic */ void $m$0() {
            ((TunerCallback) this.f179-$f1).m57lambda$-com_android_server_broadcastradio_TunerCallback_3268(this.f178-$f0);
        }

        private final /* synthetic */ void $m$1() {
            ((TunerCallback) this.f179-$f1).m58lambda$-com_android_server_broadcastradio_TunerCallback_3430(this.f178-$f0);
        }

        private final /* synthetic */ void $m$2() {
            ((TunerCallback) this.f179-$f1).m56lambda$-com_android_server_broadcastradio_TunerCallback_3122(this.f178-$f0);
        }

        private final /* synthetic */ void $m$3() {
            ((TunerCallback) this.f179-$f1).m55lambda$-com_android_server_broadcastradio_TunerCallback_2972(this.f178-$f0);
        }

        public /* synthetic */ AnonymousClass3(byte b, boolean z, Object obj) {
            this.$id = b;
            this.f178-$f0 = z;
            this.f179-$f1 = obj;
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
        ((TunerCallback) this.f173-$f0).m59lambda$-com_android_server_broadcastradio_TunerCallback_3585();
    }

    private final /* synthetic */ void $m$1() {
        ((TunerCallback) this.f173-$f0).m60lambda$-com_android_server_broadcastradio_TunerCallback_3715();
    }

    public /* synthetic */ -$Lambda$UibxWVH8zVvcNBN03iM01Oc7JJI(byte b, Object obj) {
        this.$id = b;
        this.f173-$f0 = obj;
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
