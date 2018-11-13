package com.android.server.pm;

import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ implements Consumer {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f289-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f290-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f291-$f2;

    /* renamed from: com.android.server.pm.-$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f292-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f293-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f294-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f295-$f3;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutUser) this.f292-$f0).m199lambda$-com_android_server_pm_ShortcutUser_18707((ShortcutService) this.f293-$f1, (int[]) this.f294-$f2, (int[]) this.f295-$f3, (ShortcutPackage) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3, Object obj4) {
            this.f292-$f0 = obj;
            this.f293-$f1 = obj2;
            this.f294-$f2 = obj3;
            this.f295-$f3 = obj4;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        ((ShortcutUser) this.f289-$f0).m198lambda$-com_android_server_pm_ShortcutUser_18284((ShortcutService) this.f290-$f1, (int[]) this.f291-$f2, (ShortcutLauncher) arg0);
    }

    public /* synthetic */ -$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ(Object obj, Object obj2, Object obj3) {
        this.f289-$f0 = obj;
        this.f290-$f1 = obj2;
        this.f291-$f2 = obj3;
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
