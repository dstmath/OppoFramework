package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageParser;

public interface IColorSellModeManager extends IOppoCommonFeature {
    public static final IColorSellModeManager DEFAULT = new IColorSellModeManager() {
        /* class com.android.server.pm.IColorSellModeManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorSellModeManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSellModeManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx ex) {
    }

    default void clearSellModeIfNeeded() {
    }

    default void interceptScanSellModeIfNeeded(String name) throws PackageManagerException {
    }

    default boolean interceptUninstallSellModeIfNeeded(PackageParser.Package pkg, IPackageDeleteObserver2 observer) {
        return false;
    }
}
