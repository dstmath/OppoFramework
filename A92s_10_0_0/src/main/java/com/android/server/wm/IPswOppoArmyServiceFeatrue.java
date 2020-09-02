package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import java.util.List;

public interface IPswOppoArmyServiceFeatrue extends IOppoCommonFeature {
    public static final IPswOppoArmyServiceFeatrue DEFAULT = new IPswOppoArmyServiceFeatrue() {
        /* class com.android.server.wm.IPswOppoArmyServiceFeatrue.AnonymousClass1 */
    };
    public static final String NAME = "IPswOppoArmyServiceFeatrue";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswOppoArmyServiceFeatrue;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context) {
    }

    default void systemReady() {
    }

    default boolean addDisallowedRunningApp(List<String> list) {
        return false;
    }

    default boolean removeDisallowedRunningApp(List<String> list) {
        return false;
    }

    default List<String> getDisallowedRunningApp() {
        return null;
    }

    default void allowToUseSdcard(boolean allow) {
    }

    default boolean isRunningDisallowed(String pkgName) {
        return false;
    }
}
