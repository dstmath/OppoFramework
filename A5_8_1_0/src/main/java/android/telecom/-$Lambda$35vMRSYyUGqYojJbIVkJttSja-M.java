package android.telecom;

import android.telecom.Call.Callback;

final /* synthetic */ class -$Lambda$35vMRSYyUGqYojJbIVkJttSja-M implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ int f36-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f37-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f38-$f2;

    private final /* synthetic */ void $m$0() {
        ((Callback) this.f37-$f1).lambda$-android_telecom_Call_82863((Call) this.f38-$f2, this.f36-$f0);
    }

    private final /* synthetic */ void $m$1() {
        ((Callback) this.f37-$f1).lambda$-android_telecom_Call_75715((Call) this.f38-$f2, this.f36-$f0);
    }

    private final /* synthetic */ void $m$2() {
        ((Callback) this.f37-$f1).lambda$-android_telecom_Call_75374((Call) this.f38-$f2, this.f36-$f0);
    }

    private final /* synthetic */ void $m$3() {
        ((RemoteConnection.Callback) this.f37-$f1).lambda$-android_telecom_RemoteConnection_58084((RemoteConnection) this.f38-$f2, this.f36-$f0);
    }

    public /* synthetic */ -$Lambda$35vMRSYyUGqYojJbIVkJttSja-M(byte b, int i, Object obj, Object obj2) {
        this.$id = b;
        this.f36-$f0 = i;
        this.f37-$f1 = obj;
        this.f38-$f2 = obj2;
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
