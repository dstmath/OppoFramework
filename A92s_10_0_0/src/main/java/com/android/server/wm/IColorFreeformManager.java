package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.android.server.am.IColorActivityManagerServiceEx;
import com.android.server.wm.ActivityStack;

public interface IColorFreeformManager extends IOppoCommonFeature {
    public static final IColorFreeformManager DEFAULT = new IColorFreeformManager() {
        /* class com.android.server.wm.IColorFreeformManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorFreeformManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorFreeformManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx ams, IColorWindowManagerServiceEx wms) {
    }

    default boolean isSupportFreeform() {
        return false;
    }

    default boolean inFullscreenCpnList(String cpn) {
        return false;
    }

    default boolean inNextNeedFullscreenCpnList(String cpn) {
        return false;
    }

    default void resetParentInfo() {
    }

    default void setParentInfo(ActivityStack stack) {
    }

    default void handleFreeformDied(boolean isFreeform, ActivityRecord r) {
    }

    default void handleParentDied(int pid) {
    }

    default ActivityStack.ActivityState getParentState() {
        return null;
    }

    default void handleApplicationSwitch(String prePkgName, String nextPkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm) {
    }

    default boolean exitFreeformIfNeed(ActivityStack targetStack) {
        return false;
    }

    default boolean skipPauseBackStackIfNeed(ActivityStack stack, ActivityRecord resuming) {
        return false;
    }

    default void oppoStartFreezingDisplayLocked() {
    }

    default void oppoStopFreezingDisplayLocked() {
    }
}
