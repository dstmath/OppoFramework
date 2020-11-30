package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.location.Location;

public interface IPswFastNetworkLocation extends IOppoCommonFeature {
    public static final IPswFastNetworkLocation DEFAULT = new IPswFastNetworkLocation() {
        /* class com.android.server.location.interfaces.IPswFastNetworkLocation.AnonymousClass1 */
    };
    public static final String Name = "IPswFastNetworkLocation";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswFastNetworkLocation;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void setLastLocation(Location location) {
    }

    default Location getValidLocation() {
        return null;
    }
}
