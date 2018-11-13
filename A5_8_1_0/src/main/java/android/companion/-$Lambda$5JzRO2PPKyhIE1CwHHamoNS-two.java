package android.companion;

import android.app.PendingIntent;

final /* synthetic */ class -$Lambda$5JzRO2PPKyhIE1CwHHamoNS-two implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f64-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f65-$f1;

    private final /* synthetic */ void $m$0() {
        ((CallbackProxy) this.f64-$f0).m27xf0d39650((CharSequence) this.f65-$f1);
    }

    private final /* synthetic */ void $m$1() {
        ((CallbackProxy) this.f64-$f0).m26xf0d33b5c((PendingIntent) this.f65-$f1);
    }

    public /* synthetic */ -$Lambda$5JzRO2PPKyhIE1CwHHamoNS-two(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f64-$f0 = obj;
        this.f65-$f1 = obj2;
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
