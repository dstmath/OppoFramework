package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IPswGnssDiagnosticTool extends IOppoCommonFeature {
    public static final IPswGnssDiagnosticTool DEFAULT = new IPswGnssDiagnosticTool() {
        /* class com.android.server.location.interfaces.IPswGnssDiagnosticTool.AnonymousClass1 */
    };
    public static final String Name = "IPswGnssDiagnosticTool";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswGnssDiagnosticTool;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void refreshRequestTimer() {
    }

    default void storeSatellitesInfo(int svCount, int usedSvcount, int cn0) {
    }

    default void storeAppSvInfo(int maxCn0, float speed) {
    }

    default void incomingNewGpsUsingApp(String providerName, String apkName) {
    }

    default void removingGpsUsingApp(String providerName, String apkName) {
    }
}
