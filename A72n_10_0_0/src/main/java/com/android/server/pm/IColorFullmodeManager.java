package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.PackageParser;

public interface IColorFullmodeManager extends IOppoCommonFeature {
    public static final IColorFullmodeManager DEFAULT = new IColorFullmodeManager() {
        /* class com.android.server.pm.IColorFullmodeManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorFullmodeManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorFullmodeManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean isClosedSuperFirewall() {
        return false;
    }

    default void trySetClosedSuperFirewall(PackageParser.Package pkg) {
    }

    default void setClosedSuperFirewall(boolean mode) {
    }
}
