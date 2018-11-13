package com.android.server.autofill;

import android.content.Intent;
import android.content.IntentSender;
import android.service.autofill.Dataset;
import android.service.autofill.ValueFinder;
import android.view.autofill.AutofillId;

final /* synthetic */ class -$Lambda$TkN02ChLwiW_wnL90EeXYJOcz-Q implements ValueFinder {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f105-$f0;

    /* renamed from: com.android.server.autofill.-$Lambda$TkN02ChLwiW_wnL90EeXYJOcz-Q$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f106-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f107-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f108-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f109-$f3;

        private final /* synthetic */ void $m$0() {
            ((Session) this.f107-$f1).lambda$-com_android_server_autofill_Session_23958(this.f106-$f0, (IntentSender) this.f108-$f2, (Intent) this.f109-$f3);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj, Object obj2, Object obj3) {
            this.f106-$f0 = i;
            this.f107-$f1 = obj;
            this.f108-$f2 = obj2;
            this.f109-$f3 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.autofill.-$Lambda$TkN02ChLwiW_wnL90EeXYJOcz-Q$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f110-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f111-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f112-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f113-$f3;

        private final /* synthetic */ void $m$0() {
            ((Session) this.f112-$f2).m124lambda$-com_android_server_autofill_Session_24580(this.f110-$f0, this.f111-$f1, (Dataset) this.f113-$f3);
        }

        public /* synthetic */ AnonymousClass2(int i, int i2, Object obj, Object obj2) {
            this.f110-$f0 = i;
            this.f111-$f1 = i2;
            this.f112-$f2 = obj;
            this.f113-$f3 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ String $m$0(AutofillId arg0) {
        return ((Session) this.f105-$f0).lambda$-com_android_server_autofill_Session_37701(arg0);
    }

    public /* synthetic */ -$Lambda$TkN02ChLwiW_wnL90EeXYJOcz-Q(Object obj) {
        this.f105-$f0 = obj;
    }

    public final String findByAutofillId(AutofillId autofillId) {
        return $m$0(autofillId);
    }
}
