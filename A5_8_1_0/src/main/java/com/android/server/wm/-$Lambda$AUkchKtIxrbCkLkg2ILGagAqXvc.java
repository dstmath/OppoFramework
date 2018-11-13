package com.android.server.wm;

import android.view.WindowManagerPolicy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$AUkchKtIxrbCkLkg2ILGagAqXvc implements Consumer {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f364-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f365-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f366-$f2;

    /* renamed from: com.android.server.wm.-$Lambda$AUkchKtIxrbCkLkg2ILGagAqXvc$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f367-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ boolean f368-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f369-$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            DisplayContent.m222lambda$-com_android_server_wm_DisplayContent_132118((WindowManagerPolicy) this.f369-$f2, this.f367-$f0, this.f368-$f1, (WindowState) arg0);
        }

        private final /* synthetic */ void $m$1(Object arg0) {
            WindowManagerService.m17lambda$-com_android_server_wm_WindowManagerService_359249(this.f367-$f0, this.f368-$f1, (ArrayList) this.f369-$f2, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(byte b, boolean z, boolean z2, Object obj) {
            this.$id = b;
            this.f367-$f0 = z;
            this.f368-$f1 = z2;
            this.f369-$f2 = obj;
        }

        public final void accept(Object obj) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(obj);
                    return;
                case (byte) 1:
                    $m$1(obj);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        DisplayContent.m221lambda$-com_android_server_wm_DisplayContent_131265((PrintWriter) this.f364-$f0, (String) this.f365-$f1, (int[]) this.f366-$f2, (WindowState) arg0);
    }

    private final /* synthetic */ void $m$1(Object arg0) {
        ((WindowManagerService) this.f364-$f0).m19lambda$-com_android_server_wm_WindowManagerService_112335((String) this.f365-$f1, (Boolean) this.f366-$f2, (WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$AUkchKtIxrbCkLkg2ILGagAqXvc(byte b, Object obj, Object obj2, Object obj3) {
        this.$id = b;
        this.f364-$f0 = obj;
        this.f365-$f1 = obj2;
        this.f366-$f2 = obj3;
    }

    public final void accept(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                $m$0(obj);
                return;
            case (byte) 1:
                $m$1(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}
