package com.android.server.pm;

import java.io.File;
import java.util.List;

final /* synthetic */ class -$Lambda$i1ZZeLvwPPAZVBl_nnQ0C2t5oMs implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ int f34-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f35-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f36-$f2;

    private final /* synthetic */ void $m$0() {
        ((MyPackageMonitor) this.f35-$f1).lambda$-com_android_server_pm_LauncherAppsService$LauncherAppsImpl$MyPackageMonitor_38712((String) this.f36-$f2, this.f34-$f0);
    }

    private final /* synthetic */ void $m$1() {
        ((PackageManagerService) this.f35-$f1).m43lambda$-com_android_server_pm_PackageManagerService_173593((List) this.f36-$f2, this.f34-$f0);
    }

    private final /* synthetic */ void $m$2() {
        ((ParallelPackageParser) this.f35-$f1).m86lambda$-com_android_server_pm_ParallelPackageParser_3950((File) this.f36-$f2, this.f34-$f0);
    }

    private final /* synthetic */ void $m$3() {
        ((ShortcutService) this.f35-$f1).m80lambda$-com_android_server_pm_ShortcutService_55280(this.f34-$f0, (String) this.f36-$f2);
    }

    public /* synthetic */ -$Lambda$i1ZZeLvwPPAZVBl_nnQ0C2t5oMs(byte b, int i, Object obj, Object obj2) {
        this.$id = b;
        this.f34-$f0 = i;
        this.f35-$f1 = obj;
        this.f36-$f2 = obj2;
    }

    public final void run() {
        switch (this.$id) {
            case (byte) 0:
                $m$0();
                return;
            case (byte) 1:
                $m$1();
                return;
            case (byte) 2:
                $m$2();
                return;
            case (byte) 3:
                $m$3();
                return;
            default:
                throw new AssertionError();
        }
    }
}
