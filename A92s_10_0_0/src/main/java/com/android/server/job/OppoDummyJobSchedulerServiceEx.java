package com.android.server.job;

import android.content.Context;
import android.os.Message;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class OppoDummyJobSchedulerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoJobSchedulerServiceEx {
    protected final JobSchedulerService mJobSchedulerService;

    public OppoDummyJobSchedulerServiceEx(Context context, JobSchedulerService jss) {
        super(context);
        this.mJobSchedulerService = jss;
    }

    @Override // com.android.server.job.IOppoJobSchedulerServiceEx
    public JobSchedulerService getJobSchedulerService() {
        return this.mJobSchedulerService;
    }

    @Override // com.android.server.job.IOppoJobSchedulerServiceEx
    public void handleMessage(Message msg, int whichHandler) {
    }
}
