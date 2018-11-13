package com.android.internal.widget;

import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;

final /* synthetic */ class -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s implements OnMenuItemClickListener {
    public static final /* synthetic */ -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s $INST$0 = new -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s();

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$1 */
    final /* synthetic */ class AnonymousClass1 implements OnClickListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f144-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f145-$f1;

        private final /* synthetic */ void $m$0(View arg0) {
            ((FloatingToolbarPopup) this.f144-$f0).m43xf8dd7a0d((ImageButton) this.f145-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.f144-$f0 = obj;
            this.f145-$f1 = obj2;
        }

        public final void onClick(View view) {
            $m$0(view);
        }
    }

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$2 */
    final /* synthetic */ class AnonymousClass2 implements OnItemClickListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f146-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f147-$f1;

        private final /* synthetic */ void $m$0(AdapterView arg0, View arg1, int arg2, long arg3) {
            ((FloatingToolbarPopup) this.f146-$f0).m44xf8ddfcf9((OverflowPanel) this.f147-$f1, arg0, arg1, arg2, arg3);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.f146-$f0 = obj;
            this.f147-$f1 = obj2;
        }

        public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
            $m$0(adapterView, view, i, j);
        }
    }

    private /* synthetic */ -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s() {
    }

    public final boolean onMenuItemClick(MenuItem menuItem) {
        return $m$0(menuItem);
    }
}
