package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.os.Bundle;
import com.android.server.pm.PackageManagerService;

public interface IColorPackageInstallStatisticManager extends IOppoCommonFeature {
    public static final IColorPackageInstallStatisticManager DEFAULT = new IColorPackageInstallStatisticManager() {
        /* class com.android.server.pm.IColorPackageInstallStatisticManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorPackageInstallStatisticManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorPackageInstallStatisticManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default void packageInstallInfoCollect(boolean logCallerInfo, boolean logCallerAppInfo, int extraPid, int extraUid, int pid, int uid, String callingPackage, Intent intent) {
    }

    default void packageInstallInfoCollectForExp(String callerApp, String callingPackage, Intent intent) {
    }

    default void sendNonSilentInstallBroadcastExp(String installerPackageName, PackageManagerService.OriginInfo origin, PackageManagerService.PackageInstalledInfo res, int childPackageCount, int childPackageIndex) {
    }

    default String addRunningInstallerPackageName(PackageManagerService.OriginInfo origin, String installerPackageName, int installerUid, boolean isCtsAppInstall) {
        return installerPackageName;
    }

    default void sendDcsSilentInstallBroadcast(String packageName, Bundle extras, String installerPackageName, int userId) {
    }
}
