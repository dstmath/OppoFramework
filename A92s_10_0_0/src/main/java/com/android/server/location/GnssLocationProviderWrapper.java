package com.android.server.location;

import com.android.server.location.interfaces.IOppoGnssLocationProvider;
import com.color.util.ColorTypeCastingHelper;

public class GnssLocationProviderWrapper {
    public static void wakeGps(GnssLocationProvider gnssLocationProvider) {
        IOppoGnssLocationProvider baseGnssLocationProvider = typeCasting(gnssLocationProvider);
        if (baseGnssLocationProvider != null) {
            baseGnssLocationProvider.wakeGps();
        }
    }

    public static void enterPSMode(GnssLocationProvider gnssLocationProvider) {
        IOppoGnssLocationProvider baseGnssLocationProvider = typeCasting(gnssLocationProvider);
        if (baseGnssLocationProvider != null) {
            baseGnssLocationProvider.enterPSMode();
        }
    }

    public static IOppoGnssLocationProvider typeCasting(GnssLocationProvider gnssLocationProvider) {
        return (IOppoGnssLocationProvider) ColorTypeCastingHelper.typeCasting(IOppoGnssLocationProvider.class, gnssLocationProvider);
    }
}
