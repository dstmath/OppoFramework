package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.location.Address;
import android.location.GeocoderParams;
import android.os.WorkSource;
import com.android.internal.location.ProviderRequest;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.LocationProviderProxy;
import java.util.List;

public interface IPswNlpProxy extends IOppoCommonFeature {
    public static final IPswNlpProxy DEFAULT = new IPswNlpProxy() {
        /* class com.android.server.location.interfaces.IPswNlpProxy.AnonymousClass1 */
    };
    public static final String Name = "IPswNlpProxy";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswNlpProxy;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default GeocoderProxy getActiveGeocoderProxy() {
        return null;
    }

    default LocationProviderProxy getActiveNpProxy() {
        return null;
    }

    default void setRequestTry(ProviderRequest requests, WorkSource workSource) {
    }

    default String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> list) {
        return null;
    }

    default String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> list) {
        return null;
    }

    default String getOppoNlpId() {
        return null;
    }
}
