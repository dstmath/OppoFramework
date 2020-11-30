package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorInstallThreadsControlManager extends IOppoCommonFeature {
    public static final IColorInstallThreadsControlManager DEFAULT = new IColorInstallThreadsControlManager() {
        /* class com.android.server.pm.IColorInstallThreadsControlManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorInstallThreadsControlManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorInstallThreadsControlManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void updateOdexThreads(String installerPackageName, int installFlags) {
    }
}
