package com.android.server.policy;

import android.os.Looper;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import android.view.InputEventReceiver.Factory;

final /* synthetic */ class -$Lambda$Nd7e3Murb8x7RqelLk3bI3c3rfY implements Factory {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f348-$f0;

    private final /* synthetic */ InputEventReceiver $m$0(InputChannel arg0, Looper arg1) {
        return ((PhoneWindowManager) this.f348-$f0).m38lambda$-com_android_server_policy_PhoneWindowManager_309504(arg0, arg1);
    }

    private final /* synthetic */ InputEventReceiver $m$1(InputChannel arg0, Looper arg1) {
        return ((PhoneWindowManager) this.f348-$f0).m37lambda$-com_android_server_policy_PhoneWindowManager_293161(arg0, arg1);
    }

    public /* synthetic */ -$Lambda$Nd7e3Murb8x7RqelLk3bI3c3rfY(byte b, Object obj) {
        this.$id = b;
        this.f348-$f0 = obj;
    }

    public final InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(inputChannel, looper);
            case (byte) 1:
                return $m$1(inputChannel, looper);
            default:
                throw new AssertionError();
        }
    }
}
