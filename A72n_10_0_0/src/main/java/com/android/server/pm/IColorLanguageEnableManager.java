package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import java.util.List;

public interface IColorLanguageEnableManager extends IOppoCommonFeature {
    public static final IColorLanguageEnableManager DEFAULT = new IColorLanguageEnableManager() {
        /* class com.android.server.pm.IColorLanguageEnableManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorLanguageEnableManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorLanguageEnableManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean setEnabledLanguagePackages(int userId, String targetPackageName, List<String> list) {
        return false;
    }
}
