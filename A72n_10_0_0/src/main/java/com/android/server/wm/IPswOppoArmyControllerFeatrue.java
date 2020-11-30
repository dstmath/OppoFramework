package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;

public interface IPswOppoArmyControllerFeatrue extends IOppoCommonFeature {
    public static final IPswOppoArmyControllerFeatrue DEFAULT = new IPswOppoArmyControllerFeatrue() {
        /* class com.android.server.wm.IPswOppoArmyControllerFeatrue.AnonymousClass1 */
    };
    public static final String NAME = "IPswOppoArmyControllerFeatrue";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswOppoArmyControllerFeatrue;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context, OppoArmyService armyService) {
    }

    default void systemReady() {
    }

    default boolean isRunningDisallowed(String pkgName) {
        return false;
    }

    default void showDisallowedRunningAppDialog() {
    }

    default boolean isAllowedForceStop(Context context, String packageName) {
        return false;
    }

    default boolean allowCallerKillProcess(Context context, int uid) {
        return false;
    }
}
