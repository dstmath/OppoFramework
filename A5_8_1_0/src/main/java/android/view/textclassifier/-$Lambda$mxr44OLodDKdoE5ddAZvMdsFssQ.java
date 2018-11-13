package android.view.textclassifier;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

final /* synthetic */ class -$Lambda$mxr44OLodDKdoE5ddAZvMdsFssQ implements OnClickListener {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f102-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f103-$f1;

    private final /* synthetic */ void $m$0(View arg0) {
        ((Context) this.f102-$f0).startActivity((Intent) this.f103-$f1);
    }

    public /* synthetic */ -$Lambda$mxr44OLodDKdoE5ddAZvMdsFssQ(Object obj, Object obj2) {
        this.f102-$f0 = obj;
        this.f103-$f1 = obj2;
    }

    public final void onClick(View view) {
        $m$0(view);
    }
}
