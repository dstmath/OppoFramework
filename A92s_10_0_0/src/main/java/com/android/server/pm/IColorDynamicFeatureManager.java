package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.FeatureInfo;

public interface IColorDynamicFeatureManager extends IOppoCommonFeature {
    public static final IColorDynamicFeatureManager DEFAULT = new IColorDynamicFeatureManager() {
        /* class com.android.server.pm.IColorDynamicFeatureManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorDynamicFeatureManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDynamicFeatureManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx ex) {
    }

    default boolean loadRegionFeature(String name) {
        return false;
    }

    default FeatureInfo[] getOppoSystemAvailableFeatures() {
        return new FeatureInfo[0];
    }

    default boolean hasOppoSystemFeature(String name) {
        return false;
    }
}
