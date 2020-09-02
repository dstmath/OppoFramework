package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInfoLite;
import com.android.server.am.IColorActivityManagerServiceEx;
import java.io.File;

public interface IColorAppInstallProgressManager extends IOppoCommonFeature {
    public static final IColorAppInstallProgressManager DEFAULT = new IColorAppInstallProgressManager() {
        /* class com.android.server.pm.IColorAppInstallProgressManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAppInstallProgressManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppInstallProgressManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx, IColorActivityManagerServiceEx amsEx) {
    }

    default void sendOppoStartInstallBro(File stagedDir, String packageName, String installerPackageName, int userId, boolean isFromPackageInstaller) {
    }

    default void sendFailBroCauseByNeedVerify(String packageName, String installerPackageName, int userId) {
    }

    default void sendFailBroInCopyFinishStage(int ret, PackageInfoLite pkgLite, String installerPackageName, int userId) {
    }

    default void sendFailBroInInstallFinishStage(int ret, String packageName, String installerPackageName, int userId) {
    }

    default int tryRetstrictPackgeInstall(String installerPackageName, PackageInfoLite pkgLite, IPackageInstallObserver2 observer) {
        return -1;
    }
}
