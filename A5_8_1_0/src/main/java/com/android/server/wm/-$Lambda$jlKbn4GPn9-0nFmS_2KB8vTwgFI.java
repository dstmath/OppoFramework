package com.android.server.wm;

import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.WindowManagerPolicy.ScreenOffListener;
import com.android.internal.app.IAssistScreenshotReceiver;

final /* synthetic */ class -$Lambda$jlKbn4GPn9-0nFmS_2KB8vTwgFI implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f433-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f434-$f1;

    private final /* synthetic */ void $m$0() {
        ((AppTransition) this.f433-$f0).m201lambda$-com_android_server_wm_AppTransition_99110((IAppTransitionAnimationSpecsFuture) this.f434-$f1);
    }

    private final /* synthetic */ void $m$1() {
        ((TaskSnapshotController) this.f433-$f0).m257lambda$-com_android_server_wm_TaskSnapshotController_16389((ScreenOffListener) this.f434-$f1);
    }

    private final /* synthetic */ void $m$2() {
        ((WindowManagerService) this.f433-$f0).m21lambda$-com_android_server_wm_WindowManagerService_182233((Runnable) this.f434-$f1);
    }

    private final /* synthetic */ void $m$3() {
        ((WindowManagerService) this.f433-$f0).m22lambda$-com_android_server_wm_WindowManagerService_215638((IAssistScreenshotReceiver) this.f434-$f1);
    }

    public /* synthetic */ -$Lambda$jlKbn4GPn9-0nFmS_2KB8vTwgFI(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f433-$f0 = obj;
        this.f434-$f1 = obj2;
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
