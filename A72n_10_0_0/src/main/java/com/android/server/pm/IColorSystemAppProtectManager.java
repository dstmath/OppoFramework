package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.PackageParser;

public interface IColorSystemAppProtectManager extends IOppoCommonFeature {
    public static final IColorSystemAppProtectManager DEFAULT = new IColorSystemAppProtectManager() {
        /* class com.android.server.pm.IColorSystemAppProtectManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorSystemAppProtectManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSystemAppProtectManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean skipScanInvalidSystemApp(PackageParser.Package pkg) {
        return false;
    }
}
