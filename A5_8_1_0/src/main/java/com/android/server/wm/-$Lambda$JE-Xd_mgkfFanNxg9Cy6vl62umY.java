package com.android.server.wm;

final /* synthetic */ class -$Lambda$JE-Xd_mgkfFanNxg9Cy6vl62umY implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ int f380-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f381-$f1;

    /* renamed from: com.android.server.wm.-$Lambda$JE-Xd_mgkfFanNxg9Cy6vl62umY$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f382-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f383-$f1;

        private final /* synthetic */ void $m$0() {
            ((PinnedStackControllerCallback) this.f383-$f1).m251x80b93ab9(this.f382-$f0);
        }

        public /* synthetic */ AnonymousClass1(boolean z, Object obj) {
            this.f382-$f0 = z;
            this.f383-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((PinnedStackControllerCallback) this.f381-$f1).m252x80b942b2(this.f380-$f0);
    }

    public /* synthetic */ -$Lambda$JE-Xd_mgkfFanNxg9Cy6vl62umY(int i, Object obj) {
        this.f380-$f0 = i;
        this.f381-$f1 = obj;
    }

    public final void run() {
        $m$0();
    }
}
