package com.android.server.job;

import android.common.OppoFeatureCache;
import android.content.Context;
import com.android.server.ColorFeatureManager;
import com.android.server.ColorLocalServices;
import com.android.server.ColorServiceFactory;
import com.android.server.ColorServiceRegistry;
import com.android.server.ColorTraceMonitor;

public class ColorJobSchedulerServiceEx extends ColorDummyJobSchedulerServiceEx {
    private Context mContext;

    public ColorJobSchedulerServiceEx(Context context, JobSchedulerService jss) {
        super(context, jss);
        this.mContext = context;
        init(context, jss);
    }

    public Context getContext() {
        return this.mContext;
    }

    public void onStart() {
    }

    public void systemReady() {
        ColorServiceRegistry.getInstance().serviceReady(28);
    }

    private void init(Context context, JobSchedulerService jss) {
        ColorServiceRegistry.getInstance().serviceInit(8, this);
        registerColorJobInit();
    }

    private void registerColorJobInit() {
        ColorDummyJobCountPolicy colorDummyJobCountPolicy;
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IBatteryIdleController.DEFAULT, new Object[0]));
        if (ColorFeatureManager.isSupport("JobCountPolicy")) {
            colorDummyJobCountPolicy = new ColorJobCountPolicy();
        } else {
            colorDummyJobCountPolicy = new ColorDummyJobCountPolicy();
        }
        ColorLocalServices.addService(IJobCountPolicy.class, (IJobCountPolicy) ColorTraceMonitor.getTraceMonitor(colorDummyJobCountPolicy, new Class[]{IJobCountPolicy.class}, "JobCountPolicy"));
    }
}
