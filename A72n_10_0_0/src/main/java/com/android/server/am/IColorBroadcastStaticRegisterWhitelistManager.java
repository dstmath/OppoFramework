package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public interface IColorBroadcastStaticRegisterWhitelistManager extends IOppoCommonFeature {
    public static final IColorBroadcastStaticRegisterWhitelistManager DEFAULT = new IColorBroadcastStaticRegisterWhitelistManager() {
        /* class com.android.server.am.IColorBroadcastStaticRegisterWhitelistManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorBroadcastStaticRegisterWhitelistManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorBroadcastStaticRegisterWhitelistManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean isSkipThisStaticBroadcastReceivers(Intent intent, ResolveInfo info) {
        return true;
    }

    default void init() {
    }
}
