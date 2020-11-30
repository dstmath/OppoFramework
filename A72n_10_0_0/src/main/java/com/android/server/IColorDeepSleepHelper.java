package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.Handler;
import com.android.server.AlarmManagerService;
import com.android.server.job.controllers.IColorJobStatusInner;
import com.android.server.job.controllers.JobStatus;

public interface IColorDeepSleepHelper extends IOppoCommonFeature {
    public static final IColorDeepSleepHelper DEFAULT = new IColorDeepSleepHelper() {
        /* class com.android.server.IColorDeepSleepHelper.AnonymousClass1 */
    };
    public static final String NAME = "IColorDeepSleepHelper";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDeepSleepHelper;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context ctx, Handler handler, AlarmManagerService ams, Object lock) {
    }

    default void init(JobStatus Jobs, IColorJobStatusInner inner) {
    }

    default boolean filterDeepSleepAlarm(String pkg, String tag) {
        return false;
    }

    default boolean sendDeepSleepBroadcast(String pkg) {
        return false;
    }

    default boolean isInDeepSleep() {
        return false;
    }

    default boolean ruleMatchDeepSleepAlarm(AlarmManagerService.Alarm alarm) {
        return false;
    }

    default void handleMatchDeepSleepAlarm() {
    }

    default void restoreDeepSleepPendingWhileIdleAlarmsLocked() {
    }

    default boolean handleMatchDeepSleepRuleJob(boolean deadlineSatisfied, boolean notDozing) {
        return true;
    }

    default void setDeviceIdleMode(boolean isDeviceIdleMode) {
    }
}
