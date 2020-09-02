package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IPswLbsCustomize extends IOppoCommonFeature {
    public static final IPswLbsCustomize DEFAULT = new IPswLbsCustomize() {
        /* class com.android.server.location.interfaces.IPswLbsCustomize.AnonymousClass1 */
    };
    public static final String Name = "IPswLbsCustomize";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswLbsCustomize;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean isForceGnssDisabled() {
        return false;
    }

    default void getAppInfoForTr(String methodName, String providerName, int pid, String packageName) {
    }

    default void setDebug(boolean isDebug) {
    }
}
