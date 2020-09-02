package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;

public interface IColorAppStoreTraffic extends IOppoCommonFeature {
    public static final IColorAppStoreTraffic DEFAULT = new IColorAppStoreTraffic() {
        /* class com.android.server.wm.IColorAppStoreTraffic.AnonymousClass1 */
    };
    public static final String NAME = "IColorAppStoreTraffic";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppStoreTraffic;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityTaskManagerServiceEx atmsEx) {
    }

    default void collectJumpStoreTracking(String callingPackage, String calledPackageName, Intent ephemeralIntent, int callingUid, String cpnClassName) {
    }

    default boolean interceptForStoreTraffic(Intent intent, String callingPackage, String calledPackageName, String cpnClassName, int userId) {
        return false;
    }

    default void setDynamicDebugSwitch(boolean on) {
    }
}
