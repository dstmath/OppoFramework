package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.IPackageDeleteObserver2;

public interface IColorForbidUninstallAppManager extends IOppoCommonFeature {
    public static final IColorForbidUninstallAppManager DEFAULT = new IColorForbidUninstallAppManager() {
        /* class com.android.server.pm.IColorForbidUninstallAppManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorForbidUninstallAppManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorForbidUninstallAppManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean isForbidDeletePackage(int uid, int userId, String packageName, IPackageDeleteObserver2 observer) {
        return false;
    }
}
