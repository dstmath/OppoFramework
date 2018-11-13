package com.android.server.autofill.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter.FilterListener;

final /* synthetic */ class -$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694 implements OnItemClickListener {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f150-$f0;

    /* renamed from: com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f151-$f0;

        private final /* synthetic */ void $m$0() {
            ((AnchoredWindow) this.f151-$f0).-com_android_server_autofill_ui_FillUi$AutofillWindowPresenter-mthref-0();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f151-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694$2 */
    final /* synthetic */ class AnonymousClass2 implements FilterListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f152-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f153-$f1;

        private final /* synthetic */ void $m$0(int arg0) {
            ((FillUi) this.f153-$f1).m136lambda$-com_android_server_autofill_ui_FillUi_7794(this.f152-$f0, arg0);
        }

        public /* synthetic */ AnonymousClass2(int i, Object obj) {
            this.f152-$f0 = i;
            this.f153-$f1 = obj;
        }

        public final void onFilterComplete(int i) {
            $m$0(i);
        }
    }

    private final /* synthetic */ void $m$0(AdapterView arg0, View arg1, int arg2, long arg3) {
        ((FillUi) this.f150-$f0).m135lambda$-com_android_server_autofill_ui_FillUi_7197(arg0, arg1, arg2, arg3);
    }

    public /* synthetic */ -$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694(Object obj) {
        this.f150-$f0 = obj;
    }

    public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
        $m$0(adapterView, view, i, j);
    }
}
