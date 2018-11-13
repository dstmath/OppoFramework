package com.android.server.accessibility;

import android.view.accessibility.IAccessibilityManagerClient;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$kXhldx_ZxidxR4suyNIbZ545MMw implements Consumer {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ int f68-$f0;

    /* renamed from: com.android.server.accessibility.-$Lambda$kXhldx_ZxidxR4suyNIbZ545MMw$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f69-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f70-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f71-$f2;

        private final /* synthetic */ void $m$0() {
            ((AccessibilityManagerService) this.f70-$f1).m107xb0c4a53((UserState) this.f71-$f2, this.f69-$f0);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj, Object obj2) {
            this.f69-$f0 = i;
            this.f70-$f1 = obj;
            this.f71-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        MainHandler.m108x642a703e(this.f68-$f0, (IAccessibilityManagerClient) arg0);
    }

    private final /* synthetic */ void $m$1(Object arg0) {
        MainHandler.m109x642b638e(this.f68-$f0, (IAccessibilityManagerClient) arg0);
    }

    private final /* synthetic */ void $m$2(Object arg0) {
        AccessibilityManagerService.m106xb0c4d7d(this.f68-$f0, (IAccessibilityManagerClient) arg0);
    }

    public /* synthetic */ -$Lambda$kXhldx_ZxidxR4suyNIbZ545MMw(byte b, int i) {
        this.$id = b;
        this.f68-$f0 = i;
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
