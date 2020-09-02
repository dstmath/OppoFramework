package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.android.server.AlarmManagerService;
import com.android.server.content.SyncOperation;
import com.android.server.job.IColorJobSchedulerServiceInner;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.controllers.JobStatus;
import java.util.ArrayList;

public interface IColorStrictModeManager extends IOppoCommonFeature {
    public static final IColorStrictModeManager DEFAULT = new IColorStrictModeManager() {
        /* class com.android.server.IColorStrictModeManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorStrictModeManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorStrictModeManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context, Object lock, AlarmManagerService ams, Looper loop, IColorAlarmManagerServiceInner inner) {
    }

    default void init(Handler handler, JobSchedulerService jss, IColorJobSchedulerServiceInner inner) {
    }

    default void stopStrictMode() {
    }

    default void filterTriggerListForStrictMode(ArrayList<AlarmManagerService.Alarm> arrayList) {
    }

    default boolean isJobSatisfiedInStrictMode(boolean componentPresent, JobStatus job) {
        return componentPresent;
    }

    default boolean isDelayAppSync(SyncOperation op, ComponentName targetComponent, int targetUid) {
        return true;
    }

    default void stopStrictModeOnJob() {
    }

    default void handleApplicationSwitch(int prePkgUid, String prePkgName, int nextPkgUid, String nextPkgName) {
    }

    default boolean isStrictMode() {
        return false;
    }

    default boolean isDelayAppJob(int uid, String pkgName) {
        return false;
    }
}
