package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IPswCoarseToFine extends IOppoCommonFeature {
    public static final IPswCoarseToFine DEFAULT = new IPswCoarseToFine() {
        /* class com.android.server.location.interfaces.IPswCoarseToFine.AnonymousClass1 */
    };
    public static final String Name = "IPswCoarseToFine";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswCoarseToFine;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean isAllowCoarseToFine(String packageName) {
        return false;
    }
}
