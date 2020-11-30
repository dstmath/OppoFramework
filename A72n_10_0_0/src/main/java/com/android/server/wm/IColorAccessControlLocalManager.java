package com.android.server.wm;

import android.app.ActivityOptions;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.pm.ActivityInfo;

public interface IColorAccessControlLocalManager extends IOppoCommonFeature {
    public static final IColorAccessControlLocalManager DEFAULT = new IColorAccessControlLocalManager() {
        /* class com.android.server.wm.IColorAccessControlLocalManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAccessControlLocalManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAccessControlLocalManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void notifyInSplitScreenMode(ActivityStack stack) {
    }

    default void checkGoToSleep(ActivityRecord activity, int userId) {
    }

    default boolean interceptResumeActivity(ActivityRecord activity, int userId) {
        return false;
    }

    default void removeAccessControlPassAsUser(String packageName, int userId, boolean allUser) {
    }

    default void publish() {
    }

    default void onSystemReady() {
    }

    default void onSplitScreenModeDismissed(ActivityStack topFullscreenStack) {
    }

    default Intent checkStartActivityForAppLock(ActivityStackSupervisor supervisor, ActivityRecord sourceRecord, ActivityInfo aInfo, Intent intent, int requestCode, int realCallingUid, ActivityOptions options) {
        return null;
    }

    default boolean isAppUnlockPasswordActivity(ActivityRecord passwordActivity) {
        return false;
    }

    default boolean shouldAbortMoveTaskToFront(TaskRecord task) {
        return false;
    }

    default void notifyZoomWindowExit(ActivityStack stack, boolean toFullScreen) {
    }
}
