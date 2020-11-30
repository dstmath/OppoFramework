package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorDataFreeManager extends IOppoCommonFeature {
    public static final IColorDataFreeManager DEFAULT = new IColorDataFreeManager() {
        /* class com.android.server.pm.IColorDataFreeManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorDataFreeManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDataFreeManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init() {
    }

    default boolean startDataFree() {
        return false;
    }

    default void generatePlaceHolderFiles() {
    }
}
