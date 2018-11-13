package com.android.server.job;

import com.android.server.job.JobStore.JobStatusFunctor;
import com.android.server.job.controllers.JobStatus;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$uHhK2abi5qBUVZxkpfjqb2-WntE implements JobStatusFunctor {
    /* renamed from: -$f0 */
    private final /* synthetic */ long f249-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f250-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f251-$f2;

    private final /* synthetic */ void $m$0(JobStatus arg0) {
        JobStore.m174lambda$-com_android_server_job_JobStore_6419(this.f249-$f0, (ArrayList) this.f250-$f1, (ArrayList) this.f251-$f2, arg0);
    }

    public /* synthetic */ -$Lambda$uHhK2abi5qBUVZxkpfjqb2-WntE(long j, Object obj, Object obj2) {
        this.f249-$f0 = j;
        this.f250-$f1 = obj;
        this.f251-$f2 = obj2;
    }

    public final void process(JobStatus jobStatus) {
        $m$0(jobStatus);
    }
}
