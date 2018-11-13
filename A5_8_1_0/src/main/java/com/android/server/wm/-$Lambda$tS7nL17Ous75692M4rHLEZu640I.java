package com.android.server.wm;

import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$tS7nL17Ous75692M4rHLEZu640I implements Predicate {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ int f438-$f0;

    private final /* synthetic */ boolean $m$0(Object arg0) {
        return DisplayContent.m218lambda$-com_android_server_wm_DisplayContent_118377(this.f438-$f0, (WindowState) arg0);
    }

    private final /* synthetic */ boolean $m$1(Object arg0) {
        return DisplayContent.m219lambda$-com_android_server_wm_DisplayContent_118576(this.f438-$f0, (WindowState) arg0);
    }

    private final /* synthetic */ boolean $m$2(Object arg0) {
        return DisplayContent.m220lambda$-com_android_server_wm_DisplayContent_118798(this.f438-$f0, (WindowState) arg0);
    }

    private final /* synthetic */ boolean $m$3(Object arg0) {
        return RootWindowContainer.m211lambda$-com_android_server_wm_RootWindowContainer_18760(this.f438-$f0, (WindowState) arg0);
    }

    private final /* synthetic */ boolean $m$4(Object arg0) {
        return WindowManagerService.m16lambda$-com_android_server_wm_WindowManagerService_241334(this.f438-$f0, (WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$tS7nL17Ous75692M4rHLEZu640I(byte b, int i) {
        this.$id = b;
        this.f438-$f0 = i;
    }

    public final boolean test(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(obj);
            case (byte) 1:
                return $m$1(obj);
            case (byte) 2:
                return $m$2(obj);
            case (byte) 3:
                return $m$3(obj);
            case (byte) 4:
                return $m$4(obj);
            default:
                throw new AssertionError();
        }
    }
}
