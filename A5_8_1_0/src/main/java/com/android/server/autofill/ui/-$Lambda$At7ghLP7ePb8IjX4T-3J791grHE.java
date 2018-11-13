package com.android.server.autofill.ui;

import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.view.View;
import android.view.View.OnClickListener;

final /* synthetic */ class -$Lambda$At7ghLP7ePb8IjX4T-3J791grHE implements OnClickListener {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f122-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f123-$f1;

    private final /* synthetic */ void $m$0(View arg0) {
        ((FillUi) this.f122-$f0).m134lambda$-com_android_server_autofill_ui_FillUi_5115((FillResponse) this.f123-$f1, arg0);
    }

    private final /* synthetic */ void $m$1(View arg0) {
        ((SaveUi) this.f122-$f0).m138lambda$-com_android_server_autofill_ui_SaveUi_10978((SaveInfo) this.f123-$f1, arg0);
    }

    public /* synthetic */ -$Lambda$At7ghLP7ePb8IjX4T-3J791grHE(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f122-$f0 = obj;
        this.f123-$f1 = obj2;
    }

    public final void onClick(View view) {
        switch (this.$id) {
            case (byte) 0:
                $m$0(view);
                return;
            case (byte) 1:
                $m$1(view);
                return;
            default:
                throw new AssertionError();
        }
    }
}
