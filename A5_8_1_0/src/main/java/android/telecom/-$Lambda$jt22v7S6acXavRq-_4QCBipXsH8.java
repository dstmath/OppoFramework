package android.telecom;

import android.telecom.RemoteConnection.Callback;

final /* synthetic */ class -$Lambda$jt22v7S6acXavRq-_4QCBipXsH8 implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f42-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f43-$f1;

    private final /* synthetic */ void $m$0() {
        ((Callback) this.f42-$f0).lambda$-android_telecom_RemoteConnection_58806((RemoteConnection) this.f43-$f1);
    }

    private final /* synthetic */ void $m$1() {
        ((Callback) this.f42-$f0).lambda$-android_telecom_RemoteConnection_57723((RemoteConnection) this.f43-$f1);
    }

    private final /* synthetic */ void $m$2() {
        ((Callback) this.f42-$f0).lambda$-android_telecom_RemoteConnection_58451((RemoteConnection) this.f43-$f1);
    }

    public /* synthetic */ -$Lambda$jt22v7S6acXavRq-_4QCBipXsH8(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f42-$f0 = obj;
        this.f43-$f1 = obj2;
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
