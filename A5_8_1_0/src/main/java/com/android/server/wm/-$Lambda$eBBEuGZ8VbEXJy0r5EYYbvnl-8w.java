package com.android.server.wm;

import android.content.Context;
import android.view.WindowManagerPolicy;
import com.android.server.input.InputManagerService;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w implements Consumer {
    /* renamed from: -$f0 */
    private final /* synthetic */ boolean f426-$f0;

    /* renamed from: com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f427-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ boolean f428-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ boolean f429-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f430-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f431-$f4;
        /* renamed from: -$f5 */
        private final /* synthetic */ Object f432-$f5;

        private final /* synthetic */ void $m$0() {
            WindowManagerService.sInstance = new OppoWindowManagerService((Context) this.f430-$f3, (InputManagerService) this.f431-$f4, this.f427-$f0, this.f428-$f1, this.f429-$f2, (WindowManagerPolicy) this.f432-$f5);
        }

        public /* synthetic */ AnonymousClass1(boolean z, boolean z2, boolean z3, Object obj, Object obj2, Object obj3) {
            this.f427-$f0 = z;
            this.f428-$f1 = z2;
            this.f429-$f2 = z3;
            this.f430-$f3 = obj;
            this.f431-$f4 = obj2;
            this.f432-$f5 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        ((WindowState) arg0).lambda$-com_android_server_wm_WindowManagerService_398773(this.f426-$f0);
    }

    public /* synthetic */ -$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w(boolean z) {
        this.f426-$f0 = z;
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
