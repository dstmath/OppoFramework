package com.android.server.job;

import java.util.Comparator;

final /* synthetic */ class -$Lambda$MZyz9fgevtnL7iKUFvjeGfWQ-E8 implements Comparator {
    public static final /* synthetic */ -$Lambda$MZyz9fgevtnL7iKUFvjeGfWQ-E8 $INST$0 = new -$Lambda$MZyz9fgevtnL7iKUFvjeGfWQ-E8();

    /* renamed from: com.android.server.job.-$Lambda$MZyz9fgevtnL7iKUFvjeGfWQ-E8$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f252-$f0;

        private final /* synthetic */ void $m$0() {
            ((JobSchedulerService) this.f252-$f0).m105lambda$-com_android_server_job_JobSchedulerService_52414();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f252-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private /* synthetic */ -$Lambda$MZyz9fgevtnL7iKUFvjeGfWQ-E8() {
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
