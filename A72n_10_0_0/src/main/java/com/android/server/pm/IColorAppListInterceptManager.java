package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;

public interface IColorAppListInterceptManager extends IOppoCommonFeature {
    public static final IColorAppListInterceptManager DEFAULT = new IColorAppListInterceptManager() {
        /* class com.android.server.pm.IColorAppListInterceptManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAppListInterceptManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppListInterceptManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean loadHideAppConfigurations() {
        return false;
    }

    default boolean shouldFilterTask(Intent intent) {
        return false;
    }
}
