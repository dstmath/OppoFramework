package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.util.Log;

public interface IPswNewNetworkTimeUpdateServiceFeature extends IOppoCommonFeature {
    public static final IPswNewNetworkTimeUpdateServiceFeature DEFAULT = new IPswNewNetworkTimeUpdateServiceFeature() {
        /* class com.android.server.IPswNewNetworkTimeUpdateServiceFeature.AnonymousClass1 */
    };
    public static final String NAME = "IPswNewNetworkTimeUpdateServiceFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswNewNetworkTimeUpdateServiceFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean isNeedSkipPollNetworkTime() {
        Log.d(NAME, "default isNeedSkipPollNetworkTime");
        return false;
    }

    default void checkSystemTime() {
        Log.d(NAME, "default checkSystemTime");
    }
}
