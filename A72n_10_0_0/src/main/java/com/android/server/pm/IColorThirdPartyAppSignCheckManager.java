package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.PackageParser;

public interface IColorThirdPartyAppSignCheckManager extends IOppoCommonFeature {
    public static final IColorThirdPartyAppSignCheckManager DEFAULT = new IColorThirdPartyAppSignCheckManager() {
        /* class com.android.server.pm.IColorThirdPartyAppSignCheckManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorThirdPartyAppSignCheckManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorThirdPartyAppSignCheckManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean isIllegalAppNameAsOppoPackage(PackageParser.Package pkg, String installerPackageName, int installFlags) {
        return false;
    }

    default boolean isForbidInstallAppByCert(PackageParser.Package pkg) {
        return false;
    }
}
