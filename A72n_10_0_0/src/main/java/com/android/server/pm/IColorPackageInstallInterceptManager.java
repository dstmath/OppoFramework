package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInstaller;
import java.io.File;

public interface IColorPackageInstallInterceptManager extends IOppoCommonFeature {
    public static final IColorPackageInstallInterceptManager DEFAULT = new IColorPackageInstallInterceptManager() {
        /* class com.android.server.pm.IColorPackageInstallInterceptManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorPackageInstallInterceptManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorPackageInstallInterceptManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean allowInterceptAdbInstallInInstallStage(int installerUid, PackageInstaller.SessionParams sessionParams, File stagedDir, String packageName, IPackageInstallObserver2 observer) {
        return false;
    }

    default boolean allowInterceptSilentInstallerInStallStage(String installerPackageName, PackageInstaller.SessionParams sessionParams, File stagedDir, String packageName, IPackageInstallObserver2 observer, String installerPackageNameFromUid) {
        return false;
    }

    default void handleForAdbSessionInstallerObserver(String packageName, int ret) {
    }

    default void handForAdbSessionInstallerCancel(String packageName) {
    }

    default void systemReady() {
    }
}
