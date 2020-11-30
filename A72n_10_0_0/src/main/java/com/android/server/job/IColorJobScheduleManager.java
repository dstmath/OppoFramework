package com.android.server.job;

import android.app.job.IJobService;
import android.app.job.JobParameters;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.StateController;
import java.util.List;

public interface IColorJobScheduleManager extends IOppoCommonFeature {
    public static final IColorJobScheduleManager DEFAULT = new IColorJobScheduleManager() {
        /* class com.android.server.job.IColorJobScheduleManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorJobScheduleManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorJobScheduleManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorJobSchedulerServiceEx jssEx) {
    }

    default void initConstructor(List<StateController> list, JobSchedulerService service) {
    }

    default long getJobScheduleTimeoutIfNeed(JobStatus job, int verb) {
        return 0;
    }

    default boolean updateExecutingParameter(IJobService service, Object lock, JobParameters params, JobStatus runningJob, int level, int verb) {
        return false;
    }

    default boolean updateParamterOnServiceContextLocked(JobStatus job) {
        return false;
    }

    default boolean isSatisfied(boolean satisfied, JobStatus job, String pkgName) {
        return false;
    }

    default boolean needCheck() {
        return false;
    }
}
