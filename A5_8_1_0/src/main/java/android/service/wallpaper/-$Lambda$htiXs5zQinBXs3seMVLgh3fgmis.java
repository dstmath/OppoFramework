package android.service.wallpaper;

import android.service.wallpaper.WallpaperService.Engine;
import java.util.function.Supplier;

final /* synthetic */ class -$Lambda$htiXs5zQinBXs3seMVLgh3fgmis implements Supplier {
    public static final /* synthetic */ -$Lambda$htiXs5zQinBXs3seMVLgh3fgmis $INST$0 = new -$Lambda$htiXs5zQinBXs3seMVLgh3fgmis();

    /* renamed from: android.service.wallpaper.-$Lambda$htiXs5zQinBXs3seMVLgh3fgmis$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f31-$f0;

        private final /* synthetic */ void $m$0() {
            ((Engine) this.f31-$f0).-android_service_wallpaper_WallpaperService$Engine-mthref-0();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f31-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private /* synthetic */ -$Lambda$htiXs5zQinBXs3seMVLgh3fgmis() {
    }

    public final Object get() {
        return $m$0();
    }
}
