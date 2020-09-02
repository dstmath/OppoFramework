package com.android.server.job;

import android.os.Message;
import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoJobSchedulerServiceEx extends IOppoCommonManagerServiceEx {
    JobSchedulerService getJobSchedulerService();

    void handleMessage(Message message, int i);
}
