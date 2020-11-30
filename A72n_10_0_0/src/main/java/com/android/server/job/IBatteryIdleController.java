package com.android.server.job;

import android.app.job.JobInfo;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import com.android.server.job.controllers.StateController;
import java.util.List;

public interface IBatteryIdleController extends IOppoCommonFeature {
    public static final IBatteryIdleController DEFAULT = new IBatteryIdleController() {
        /* class com.android.server.job.IBatteryIdleController.AnonymousClass1 */
    };
    public static final String NAME = "IBatteryIdleController";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IBatteryIdleController;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void addController(List<StateController> list, JobSchedulerService service) {
    }

    default void updateSystemFlag(Context context, JobInfo job, int uid) {
    }
}
