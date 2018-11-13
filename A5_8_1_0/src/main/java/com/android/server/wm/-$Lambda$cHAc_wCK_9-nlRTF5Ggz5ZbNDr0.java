package com.android.server.wm;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0 implements Consumer {
    /* renamed from: -$f0 */
    private final /* synthetic */ int f417-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f418-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f419-$f2;

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f420-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f421-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f422-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f423-$f3;

        private final /* synthetic */ void $m$0(Object arg0) {
            RootWindowContainer.m212lambda$-com_android_server_wm_RootWindowContainer_48318((ArrayList) this.f421-$f1, (PrintWriter) this.f422-$f2, (int[]) this.f423-$f3, this.f420-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(boolean z, Object obj, Object obj2, Object obj3) {
            this.f420-$f0 = z;
            this.f421-$f1 = obj;
            this.f422-$f2 = obj2;
            this.f423-$f3 = obj3;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0$2 */
    final /* synthetic */ class AnonymousClass2 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f424-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f425-$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            RootWindowContainer.m210lambda$-com_android_server_wm_RootWindowContainer_18051(this.f425-$f1, this.f424-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass2(boolean z, int i) {
            this.f424-$f0 = z;
            this.f425-$f1 = i;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        RootWindowContainer.m209lambda$-com_android_server_wm_RootWindowContainer_13182((String) this.f418-$f1, (ArrayList) this.f419-$f2, this.f417-$f0, (WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0(int i, Object obj, Object obj2) {
        this.f417-$f0 = i;
        this.f418-$f1 = obj;
        this.f419-$f2 = obj2;
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
