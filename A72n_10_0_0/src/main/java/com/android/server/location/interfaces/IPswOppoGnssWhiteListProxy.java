package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.location.Location;

public interface IPswOppoGnssWhiteListProxy extends IOppoCommonFeature {
    public static final IPswOppoGnssWhiteListProxy DEFAULT = new IPswOppoGnssWhiteListProxy() {
        /* class com.android.server.location.interfaces.IPswOppoGnssWhiteListProxy.AnonymousClass1 */
    };
    public static final String Name = "IPswOppoGnssWhiteListProxy";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswOppoGnssWhiteListProxy;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean isNetworkUseablechanged(String providerName, boolean providerUsable) {
        return false;
    }

    default boolean inNetworkLocationWhiteList(boolean providerUsable, String packageName) {
        return false;
    }

    default boolean isAllowedPassLocationAccess(String packageName) {
        return false;
    }

    default boolean isAllowedChangeChipData(String provider, String command) {
        return false;
    }

    default boolean isLocationInteractive() {
        return false;
    }

    default boolean improveAccForApps(Location location, String packageName) {
        return false;
    }
}
