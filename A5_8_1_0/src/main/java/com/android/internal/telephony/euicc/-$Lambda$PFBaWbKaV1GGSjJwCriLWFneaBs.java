package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.content.Intent;

final /* synthetic */ class -$Lambda$PFBaWbKaV1GGSjJwCriLWFneaBs implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ int f32-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f33-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f34-$f2;
    /* renamed from: -$f3 */
    private final /* synthetic */ Object f35-$f3;

    private final /* synthetic */ void $m$0() {
        ((EuiccController) this.f33-$f1).lambda$-com_android_internal_telephony_euicc_EuiccController_38558((PendingIntent) this.f34-$f2, this.f32-$f0, (Intent) this.f35-$f3);
    }

    public /* synthetic */ -$Lambda$PFBaWbKaV1GGSjJwCriLWFneaBs(int i, Object obj, Object obj2, Object obj3) {
        this.f32-$f0 = i;
        this.f33-$f1 = obj;
        this.f34-$f2 = obj2;
        this.f35-$f3 = obj3;
    }

    public final void run() {
        $m$0();
    }
}
