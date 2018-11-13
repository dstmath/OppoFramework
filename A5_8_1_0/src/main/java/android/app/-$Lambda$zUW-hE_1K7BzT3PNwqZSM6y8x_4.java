package android.app;

import android.app.WallpaperManager.OnColorsChangedListener;
import android.util.Pair;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$zUW-hE_1K7BzT3PNwqZSM6y8x_4 implements Predicate {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f49-$f0;

    /* renamed from: android.app.-$Lambda$zUW-hE_1K7BzT3PNwqZSM6y8x_4$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f50-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f51-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f52-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f53-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f54-$f4;

        private final /* synthetic */ void $m$0() {
            ((Globals) this.f52-$f2).m21lambda$-android_app_WallpaperManager$Globals_13505((Pair) this.f53-$f3, (WallpaperColors) this.f54-$f4, this.f50-$f0, this.f51-$f1);
        }

        public /* synthetic */ AnonymousClass1(int i, int i2, Object obj, Object obj2, Object obj3) {
            this.f50-$f0 = i;
            this.f51-$f1 = i2;
            this.f52-$f2 = obj;
            this.f53-$f3 = obj2;
            this.f54-$f4 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ boolean $m$0(Object arg0) {
        return Globals.m20lambda$-android_app_WallpaperManager$Globals_12540((OnColorsChangedListener) this.f49-$f0, (Pair) arg0);
    }

    public /* synthetic */ -$Lambda$zUW-hE_1K7BzT3PNwqZSM6y8x_4(Object obj) {
        this.f49-$f0 = obj;
    }

    public final boolean test(Object obj) {
        return $m$0(obj);
    }
}
