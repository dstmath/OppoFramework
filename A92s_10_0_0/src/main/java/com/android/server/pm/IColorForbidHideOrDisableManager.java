package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorForbidHideOrDisableManager extends IOppoCommonFeature {
    public static final IColorForbidHideOrDisableManager DEFAULT = new IColorForbidHideOrDisableManager() {
        /* class com.android.server.pm.IColorForbidHideOrDisableManager.AnonymousClass1 */
    };
    public static final String NAME = "ColorForbidHideOrDisableManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorForbidHideOrDisableManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean isPackageForbidHidden(boolean hidden, String packageName) {
        return false;
    }

    default boolean isPackageForbidDisabled(int callingUid, int newState, String packageName) {
        return false;
    }
}
