package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ComponentName;
import android.content.Context;

public interface IColorAppSwitchManager extends IOppoCommonFeature {
    public static final IColorAppSwitchManager DEFAULT = new IColorAppSwitchManager() {
        /* class com.android.server.wm.IColorAppSwitchManager.AnonymousClass1 */
    };
    public static final String NAME = "ColorAppSwitch";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppSwitchManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init() {
    }

    default String getPrePkgName() {
        return null;
    }

    default String getNextPkgName() {
        return null;
    }

    default int getPrePkgUid() {
        return 0;
    }

    default int getNextPkgUid() {
        return 0;
    }

    default void setActivityChangedListener(ActivityChangedListener activityChangedListener) {
    }

    default void removeActivityChangedListener(ActivityChangedListener activityChangedListener) {
    }

    default void handleActivitySwitch(Context context, ActivityRecord prev, ActivityRecord next, boolean dontWaitForPause, ComponentName lastCpn) {
    }

    default void handleActivitySwitch(Context context, ActivityRecord prev, ActivityRecord next, boolean userLeaving) {
    }

    public interface ActivityChangedListener {
        default void onActivityChanged(String prePkg, String nextPkg) {
        }
    }
}
