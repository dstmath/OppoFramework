package android.telecom;

import android.telecom.Connection.Listener;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$4SVh5muPQdDUeBsBoEG9OejHF-s implements Consumer {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f39-$f0;

    /* renamed from: android.telecom.-$Lambda$4SVh5muPQdDUeBsBoEG9OejHF-s$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f40-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f41-$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((Connection) this.f41-$f1).lambda$-android_telecom_Connection_104672(this.f40-$f0, (Listener) arg0);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj) {
            this.f40-$f0 = i;
            this.f41-$f1 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        ((Connection) this.f39-$f0).lambda$-android_telecom_Connection_105297((Listener) arg0);
    }

    private final /* synthetic */ void $m$1(Object arg0) {
        ((Connection) this.f39-$f0).lambda$-android_telecom_Connection_104063((Listener) arg0);
    }

    private final /* synthetic */ void $m$2(Object arg0) {
        ((Connection) this.f39-$f0).lambda$-android_telecom_Connection_104986((Listener) arg0);
    }

    public /* synthetic */ -$Lambda$4SVh5muPQdDUeBsBoEG9OejHF-s(byte b, Object obj) {
        this.$id = b;
        this.f39-$f0 = obj;
    }

    public final void accept(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                $m$0(obj);
                return;
            case (byte) 1:
                $m$1(obj);
                return;
            case (byte) 2:
                $m$2(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}
