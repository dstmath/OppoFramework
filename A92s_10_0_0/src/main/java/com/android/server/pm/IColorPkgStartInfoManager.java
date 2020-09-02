package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorPkgStartInfoManager extends IOppoCommonFeature {
    public static final IColorPkgStartInfoManager DEFAULT = new IColorPkgStartInfoManager() {
        /* class com.android.server.pm.IColorPkgStartInfoManager.AnonymousClass1 */
    };
    public static final String NAME = "ColorPkgStartInfoManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorPkgStartInfoManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean removePkgFromNotLaunchedList(String pkg, boolean notify) {
        return false;
    }

    default boolean addPkgToNotLaunchedList(String pkg) {
        return false;
    }
}
