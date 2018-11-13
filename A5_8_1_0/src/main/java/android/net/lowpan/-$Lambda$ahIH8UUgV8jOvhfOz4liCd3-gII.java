package android.net.lowpan;

import android.net.lowpan.LowpanScanner.AnonymousClass2;
import android.net.lowpan.LowpanScanner.Callback;
import java.util.function.ToIntFunction;

final /* synthetic */ class -$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII implements ToIntFunction {
    public static final /* synthetic */ -$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII $INST$0 = new -$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII();

    /* renamed from: android.net.lowpan.-$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f91-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f93-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f94-$f2;

        private final /* synthetic */ void $m$0() {
            AnonymousClass2.m45lambda$-android_net_lowpan_LowpanScanner$2_8042((Callback) this.f94-$f2, this.f91-$f0, this.f93-$f1);
        }

        public /* synthetic */ AnonymousClass1(int i, int i2, Object obj) {
            this.f91-$f0 = i;
            this.f93-$f1 = i2;
            this.f94-$f2 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private /* synthetic */ -$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII() {
    }

    public final int applyAsInt(Object obj) {
        return $m$0(obj);
    }
}
