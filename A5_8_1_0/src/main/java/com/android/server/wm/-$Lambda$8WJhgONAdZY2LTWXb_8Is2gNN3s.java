package com.android.server.wm;

import android.util.SparseIntArray;
import android.view.WindowManagerPolicy;
import java.util.ArrayList;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$8WJhgONAdZY2LTWXb_8Is2gNN3s implements Consumer {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f362-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f363-$f1;

    private final /* synthetic */ void $m$0(Object arg0) {
        ((DisplayContent) this.f362-$f0).m231lambda$-com_android_server_wm_DisplayContent_138550((WindowManagerPolicy) this.f363-$f1, (WindowState) arg0);
    }

    private final /* synthetic */ void $m$1(Object arg0) {
        ((OppoWindowManagerService) this.f362-$f0).m208lambda$-com_android_server_wm_OppoWindowManagerService_17035((ArrayList) this.f363-$f1, (WindowState) arg0);
    }

    private final /* synthetic */ void $m$2(Object arg0) {
        ((RootWindowContainer) this.f362-$f0).m215lambda$-com_android_server_wm_RootWindowContainer_21423((SparseIntArray) this.f363-$f1, (WindowState) arg0);
    }

    private final /* synthetic */ void $m$3(Object arg0) {
        ((WindowManagerService) this.f362-$f0).m20lambda$-com_android_server_wm_WindowManagerService_117511((String) this.f363-$f1, (WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$8WJhgONAdZY2LTWXb_8Is2gNN3s(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f362-$f0 = obj;
        this.f363-$f1 = obj2;
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
            case (byte) 3:
                $m$3(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}
