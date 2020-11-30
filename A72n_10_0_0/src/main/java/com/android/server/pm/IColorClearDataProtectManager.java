package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorClearDataProtectManager extends IOppoCommonFeature {
    public static final IColorClearDataProtectManager DEFAULT = new IColorClearDataProtectManager() {
        /* class com.android.server.pm.IColorClearDataProtectManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorClearDataProtectManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorClearDataProtectManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx ex) {
    }

    default void interceptClearUserDataIfNeeded(String packageName) throws SecurityException {
    }
}
