package com.android.server.policy;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

final /* synthetic */ class -$Lambda$k6uOVlk6EqgDgfUMuhedgW8Qb2I implements OnCancelListener {
    /* renamed from: -$f0 */
    private final /* synthetic */ int f343-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f344-$f1;

    /* renamed from: com.android.server.policy.-$Lambda$k6uOVlk6EqgDgfUMuhedgW8Qb2I$1 */
    final /* synthetic */ class AnonymousClass1 implements OnClickListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f345-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f346-$f1;

        private final /* synthetic */ void $m$0(DialogInterface arg0, int arg1) {
            ((AccessibilityShortcutController) this.f346-$f1).m110x4afd6771(this.f345-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj) {
            this.f345-$f0 = i;
            this.f346-$f1 = obj;
        }

        public final void onClick(DialogInterface dialogInterface, int i) {
            $m$0(dialogInterface, i);
        }
    }

    private final /* synthetic */ void $m$0(DialogInterface arg0) {
        ((AccessibilityShortcutController) this.f344-$f1).m111x4afd7313(this.f343-$f0, arg0);
    }

    public /* synthetic */ -$Lambda$k6uOVlk6EqgDgfUMuhedgW8Qb2I(int i, Object obj) {
        this.f343-$f0 = i;
        this.f344-$f1 = obj;
    }

    public final void onCancel(DialogInterface dialogInterface) {
        $m$0(dialogInterface);
    }
}
