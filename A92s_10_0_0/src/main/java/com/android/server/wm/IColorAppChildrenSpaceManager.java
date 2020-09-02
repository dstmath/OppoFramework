package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.ActivityInfo;
import java.util.Collections;
import java.util.List;

public interface IColorAppChildrenSpaceManager extends IOppoCommonFeature {
    public static final IColorAppChildrenSpaceManager DEFAULT = new IColorAppChildrenSpaceManager() {
        /* class com.android.server.wm.IColorAppChildrenSpaceManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAppChildrenSpaceManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppChildrenSpaceManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityTaskManagerServiceEx atms) {
    }

    default boolean isChildSpaceMode() {
        return false;
    }

    default void setChildSpaceMode(boolean childSpaceMode) {
    }

    default List<String> getAllowLaunchApps() {
        return Collections.emptyList();
    }

    default void setAllowLaunchApps(List<String> list) {
    }

    default boolean handleChildrenSpaceAppLaunch(ActivityInfo info) {
        return false;
    }
}
