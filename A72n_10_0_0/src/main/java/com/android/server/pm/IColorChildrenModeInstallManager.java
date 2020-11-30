package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.pm.IPackageDeleteObserver2;

public interface IColorChildrenModeInstallManager extends IOppoCommonFeature {
    public static final IColorChildrenModeInstallManager DEFAULT = new IColorChildrenModeInstallManager() {
        /* class com.android.server.pm.IColorChildrenModeInstallManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorChildrenModeInstallManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorChildrenModeInstallManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean prohibitChildInstallation(int userId, boolean isInstall) {
        return false;
    }

    default void init(Context ctx) {
    }

    default boolean prohibitDeleteInChildMode(int userId, String packageName, IPackageDeleteObserver2 observer, boolean isInstall) {
        return false;
    }
}
