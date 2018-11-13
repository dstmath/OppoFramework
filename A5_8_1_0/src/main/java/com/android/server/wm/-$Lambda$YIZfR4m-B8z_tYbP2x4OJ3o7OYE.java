package com.android.server.wm;

import android.util.SparseArray;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$YIZfR4m-B8z_tYbP2x4OJ3o7OYE implements Consumer {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f435-$f0;

    private final /* synthetic */ void $m$0(Object arg0) {
        MagnifiedViewport.m263xba8cd1c4((SparseArray) this.f435-$f0, (WindowState) arg0);
    }

    private final /* synthetic */ void $m$1(Object arg0) {
        WindowsForAccessibilityObserver.m264x5704f50((SparseArray) this.f435-$f0, (WindowState) arg0);
    }

    private final /* synthetic */ void $m$10(Object arg0) {
        ((DisplayContent) this.f435-$f0).m227lambda$-com_android_server_wm_DisplayContent_121013((WindowState) arg0);
    }

    private final /* synthetic */ void $m$11(Object arg0) {
        ((WindowState) arg0).mWinAnimator.enableSurfaceTrace((FileDescriptor) this.f435-$f0);
    }

    private final /* synthetic */ void $m$12(Object arg0) {
        ((DisplayContent) this.f435-$f0).m233lambda$-com_android_server_wm_DisplayContent_162227((WindowState) arg0);
    }

    private final /* synthetic */ void $m$13(Object arg0) {
        ((DisplayContent) this.f435-$f0).m230lambda$-com_android_server_wm_DisplayContent_137986((WindowState) arg0);
    }

    private final /* synthetic */ void $m$14(Object arg0) {
        ((RootWindowContainer) this.f435-$f0).m216lambda$-com_android_server_wm_RootWindowContainer_7306((WindowState) arg0);
    }

    private final /* synthetic */ void $m$15(Object arg0) {
        ((RootWindowContainer) this.f435-$f0).m214lambda$-com_android_server_wm_RootWindowContainer_18316((WindowState) arg0);
    }

    private final /* synthetic */ void $m$16(Object arg0) {
        ((TaskSnapshotController) this.f435-$f0).m258lambda$-com_android_server_wm_TaskSnapshotController_16554((Task) arg0);
    }

    private final /* synthetic */ void $m$17(Object arg0) {
        ((WindowLayersController) this.f435-$f0).m248lambda$-com_android_server_wm_WindowLayersController_4392((WindowState) arg0);
    }

    private final /* synthetic */ void $m$18(Object arg0) {
        ((PrintWriter) this.f435-$f0).println((WindowState) arg0);
    }

    private final /* synthetic */ void $m$19(Object arg0) {
        ((ArrayList) this.f435-$f0).add((WindowState) arg0);
    }

    private final /* synthetic */ void $m$2(Object arg0) {
        ((WindowManagerService) this.f435-$f0).-com_android_server_wm_AppWindowToken-mthref-0((WindowState) arg0);
    }

    private final /* synthetic */ void $m$3(Object arg0) {
        ((DisplayContent) this.f435-$f0).m234lambda$-com_android_server_wm_DisplayContent_17710((WindowState) arg0);
    }

    private final /* synthetic */ void $m$4(Object arg0) {
        ((DisplayContent) this.f435-$f0).m235lambda$-com_android_server_wm_DisplayContent_20614((WindowState) arg0);
    }

    private final /* synthetic */ void $m$5(Object arg0) {
        ((DisplayContent) this.f435-$f0).m236lambda$-com_android_server_wm_DisplayContent_22563((WindowState) arg0);
    }

    private final /* synthetic */ void $m$6(Object arg0) {
        ((DisplayContent) this.f435-$f0).m238lambda$-com_android_server_wm_DisplayContent_25904((WindowState) arg0);
    }

    private final /* synthetic */ void $m$7(Object arg0) {
        ((DisplayContent) this.f435-$f0).m239lambda$-com_android_server_wm_DisplayContent_39107((WindowState) arg0);
    }

    private final /* synthetic */ void $m$8(Object arg0) {
        ((DisplayContent) this.f435-$f0).m241lambda$-com_android_server_wm_DisplayContent_43131((WindowState) arg0);
    }

    private final /* synthetic */ void $m$9(Object arg0) {
        ((DisplayContent) this.f435-$f0).m242lambda$-com_android_server_wm_DisplayContent_43336((WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$YIZfR4m-B8z_tYbP2x4OJ3o7OYE(byte b, Object obj) {
        this.$id = b;
        this.f435-$f0 = obj;
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
            case (byte) 4:
                $m$4(obj);
                return;
            case (byte) 5:
                $m$5(obj);
                return;
            case (byte) 6:
                $m$6(obj);
                return;
            case (byte) 7:
                $m$7(obj);
                return;
            case (byte) 8:
                $m$8(obj);
                return;
            case (byte) 9:
                $m$9(obj);
                return;
            case (byte) 10:
                $m$10(obj);
                return;
            case (byte) 11:
                $m$11(obj);
                return;
            case (byte) 12:
                $m$12(obj);
                return;
            case (byte) 13:
                $m$13(obj);
                return;
            case (byte) 14:
                $m$14(obj);
                return;
            case (byte) 15:
                $m$15(obj);
                return;
            case (byte) 16:
                $m$16(obj);
                return;
            case (byte) 17:
                $m$17(obj);
                return;
            case (byte) 18:
                $m$18(obj);
                return;
            case (byte) 19:
                $m$19(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}
