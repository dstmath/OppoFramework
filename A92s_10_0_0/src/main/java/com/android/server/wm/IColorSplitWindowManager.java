package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import com.android.server.am.IColorActivityManagerServiceEx;

public interface IColorSplitWindowManager extends IOppoCommonFeature {
    public static final IColorSplitWindowManager DEFAULT = new IColorSplitWindowManager() {
        /* class com.android.server.wm.IColorSplitWindowManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorSplitWindowManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSplitWindowManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default void swapDockedFullscreenStack() {
    }

    default boolean isSwapToPrimaryStack(String reason) {
        return false;
    }

    default void adjustActivityResizeMode(ActivityInfo aInfo) {
    }

    default boolean shouldHandleForPrimaryStack(ActivityStack currentStack, ActivityStack nextFocusableStack, String reason) {
        return false;
    }

    default boolean handleBackKeyForPrimaryStack(ActivityStack currentStack, ActivityStack nextFocusableStack, String reason) {
        return false;
    }

    default boolean moveTaskToBackForSplitWindowMode(ActivityStack currentStack) {
        return false;
    }

    default int getSplitScreenState(Intent intent) {
        return -1;
    }

    default boolean isInForbidActivityList(TaskRecord task) {
        return false;
    }

    default int splitScreenForEdgePanel(Intent intent, int userId) {
        return 0;
    }
}
