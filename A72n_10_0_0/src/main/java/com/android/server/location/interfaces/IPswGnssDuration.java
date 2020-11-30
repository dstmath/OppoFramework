package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IPswGnssDuration extends IOppoCommonFeature {
    public static final IPswGnssDuration DEFAULT = new IPswGnssDuration() {
        /* class com.android.server.location.interfaces.IPswGnssDuration.AnonymousClass1 */
    };
    public static final String Name = "IPswGnssDuration";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswGnssDuration;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean isExpROM() {
        return true;
    }

    default boolean isFeedBackGnssDuration(long gpsDuration, boolean isRecord, String packageName) {
        return false;
    }
}
