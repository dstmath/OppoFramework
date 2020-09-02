package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IPswNavigationStatusController extends IOppoCommonFeature {
    public static final IPswNavigationStatusController DEFAULT = new IPswNavigationStatusController() {
        /* class com.android.server.location.interfaces.IPswNavigationStatusController.AnonymousClass1 */
    };
    public static final String Name = "IPswNavigationStatusController";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswNavigationStatusController;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init() {
    }

    default void startController() {
    }

    default void stopController() {
    }

    default void setUp() {
    }

    default void setDebug(boolean isDebug) {
    }

    default boolean resistStartGps() {
        return false;
    }

    default int getNavigateMode() {
        return -1;
    }

    default void setPowerSaveForDump(int powerSaveType) {
    }

    default boolean powerSaveEnabled() {
        return false;
    }
}
