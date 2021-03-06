package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import java.io.PrintWriter;

public interface IPswOppoAmsUtilsFeatrue extends IOppoCommonFeature {
    public static final IPswOppoAmsUtilsFeatrue DEFAULT = new IPswOppoAmsUtilsFeatrue() {
        /* class com.android.server.wm.IPswOppoAmsUtilsFeatrue.AnonymousClass1 */
    };
    public static final String NAME = "IPswOppoAmsUtilsFeatrue";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswOppoAmsUtilsFeatrue;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void systemReady() {
    }

    default void init(ActivityTaskManagerService service) {
    }

    default void DumpEnvironment() {
    }

    default void saveSkippedFramesRecordToList(String processName, long dateTime, long skippedFrames) {
    }

    default void dumpSkippedFrames(PrintWriter pw) {
    }

    default void detectExceptionsForOIDT(Context context, int type, String pkgName, String pkgVersion, String reason) {
    }

    default boolean shouldDelayKeyguardServiceRestart(String shortName, boolean shouldDelay) {
        return false;
    }

    default void detectForgroundExceptions(Context context, String pkgName, String reason) {
    }

    default void saveActivityLaunchRecordToList(long dateTime, int pid, int userId, int activityRecordIdHashCode, String launchedActivityShortComponentName, int activityLaunchTime, int launchType) {
    }

    default void dumpActivityLaunchTime(PrintWriter pw) {
    }

    default void saveAmKillRecordToList(long dateTime, int pid, String processName, String reason) {
    }

    default void dumpAmKill(PrintWriter pw) {
    }

    default void saveSkippedFramesRecordToList(String processName, long dateTime, boolean isAnimation, boolean isForeground, long skippedFrames) {
    }
}
