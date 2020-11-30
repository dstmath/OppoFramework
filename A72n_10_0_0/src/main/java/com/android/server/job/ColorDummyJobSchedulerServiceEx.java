package com.android.server.job;

import android.content.Context;
import com.android.server.ColorLocalServices;

public class ColorDummyJobSchedulerServiceEx extends OppoDummyJobSchedulerServiceEx implements IColorJobSchedulerServiceEx {
    private static final String TAG = "ColorDummyJobSchedulerServiceEx";
    private Context mContext;

    public ColorDummyJobSchedulerServiceEx(Context context, JobSchedulerService jss) {
        super(context, jss);
        this.mContext = context;
    }

    @Override // com.android.server.job.IColorJobSchedulerServiceEx
    public Context getContext() {
        return this.mContext;
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void systemReady() {
    }

    /* access modifiers changed from: package-private */
    public void registerDummyColorCustomManager() {
        ColorLocalServices.addService(IJobCountPolicy.class, new ColorDummyJobCountPolicy());
    }
}
