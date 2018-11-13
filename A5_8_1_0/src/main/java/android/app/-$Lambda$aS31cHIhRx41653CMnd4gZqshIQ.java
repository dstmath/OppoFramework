package android.app;

import android.app.KeyguardManager.KeyguardDismissCallback;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$aS31cHIhRx41653CMnd4gZqshIQ implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f40-$f0;

    private final /* synthetic */ void $m$0() {
        ((Dialog) this.f40-$f0).-android_app_Dialog-mthref-0();
    }

    private final /* synthetic */ void $m$1() {
        ((EnterTransitionCoordinator) this.f40-$f0).m12lambda$-android_app_EnterTransitionCoordinator_8442();
    }

    private final /* synthetic */ void $m$2() {
        ((Fragment) this.f40-$f0).-android_app_Fragment-mthref-0();
    }

    private final /* synthetic */ void $m$3() {
        FragmentTransition.setViewVisibility((ArrayList) this.f40-$f0, 4);
    }

    private final /* synthetic */ void $m$4() {
        ((KeyguardDismissCallback) this.f40-$f0).-android_app_KeyguardManager$1-mthref-2();
    }

    private final /* synthetic */ void $m$5() {
        ((KeyguardDismissCallback) this.f40-$f0).-android_app_KeyguardManager$1-mthref-0();
    }

    private final /* synthetic */ void $m$6() {
        ((KeyguardDismissCallback) this.f40-$f0).-android_app_KeyguardManager$1-mthref-1();
    }

    private final /* synthetic */ void $m$7() {
        ((Args) this.f40-$f0).m19lambda$-android_app_LoadedApk$ReceiverDispatcher$Args_56044();
    }

    public /* synthetic */ -$Lambda$aS31cHIhRx41653CMnd4gZqshIQ(byte b, Object obj) {
        this.$id = b;
        this.f40-$f0 = obj;
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
            case (byte) 4:
                $m$4();
                return;
            case (byte) 5:
                $m$5();
                return;
            case (byte) 6:
                $m$6();
                return;
            case (byte) 7:
                $m$7();
                return;
            default:
                throw new AssertionError();
        }
    }
}
