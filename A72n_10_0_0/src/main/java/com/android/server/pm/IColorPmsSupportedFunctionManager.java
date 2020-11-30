package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorPmsSupportedFunctionManager extends IOppoCommonFeature {
    public static final IColorPmsSupportedFunctionManager DEFAULT = new IColorPmsSupportedFunctionManager() {
        /* class com.android.server.pm.IColorPmsSupportedFunctionManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorPmsSupportedFunctionManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorPmsSupportedFunctionManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean isSupportSessionWrite() {
        return false;
    }

    default void setSupportSessionWrite(boolean support) {
    }
}
