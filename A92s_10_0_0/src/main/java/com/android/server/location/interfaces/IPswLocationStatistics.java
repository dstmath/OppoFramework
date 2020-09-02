package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.Bundle;
import com.android.server.location.LocationRequestStatistics;

public interface IPswLocationStatistics extends IOppoCommonFeature {
    public static final IPswLocationStatistics DEFAULT = new IPswLocationStatistics() {
        /* class com.android.server.location.interfaces.IPswLocationStatistics.AnonymousClass1 */
    };
    public static final int GEOCODER_ERROR_FAILED = 3;
    public static final int GEOCODER_ERROR_NO_RESPONSE = 2;
    public static final int GEOCODER_ERROR_NO_SERVICE = 1;
    public static final int GEOCODER_ERROR_REPEAT_REQUEST = 4;
    public static final int GNSS_STRATEGY_OF_LIGHT_STILL_CONDITION = 2;
    public static final int GNSS_STRATEGY_OF_STILL_CONDITION = 1;
    public static final int NLP_ERROR_FAILED = 3;
    public static final int NLP_ERROR_NO_NETWORK = 5;
    public static final int NLP_ERROR_NO_RESPONSE = 2;
    public static final int NLP_ERROR_NO_SERVICE = 1;
    public static final int NLP_ERROR_REPEAT_REQUEST = 4;
    public static final String Name = "IPswLocationStatistics";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswLocationStatistics;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context, LocationRequestStatistics locationRequestStatistics) {
    }

    default boolean handleCommand(String provider, String command, Bundle extras) {
        return false;
    }

    default void stopRequesting(String packageName, String providerName, String hash) {
    }

    default void startRequesting(String packageName, String providerName, long intervalMs, boolean isForeground, String hash) {
    }

    default void recordGnssNavigatingStarted(long interval) {
    }

    default void recordGnssNavigatingStopped() {
    }

    default void recordGnssPowerSaveStarted(int strategyCode) {
    }

    default void recordGnssPowerSaveStopped(int strategyCode) {
    }

    default void recordHeldWakelock(String name) {
    }

    default void recordReleaseWakelock(String name) {
    }

    default void recordNlpNavigatingStarted() {
    }

    default void recordNlpNavigatingStopped() {
    }

    default void recordNlpError(int code) {
    }

    default void recordNlpScanWifiTotal(String packageName) {
    }

    default void recordNlpScanWifiSucceed(String packageName) {
    }

    default void recordGeocoderRequestStarted() {
    }

    default void recordGeocoderRequestStopped() {
    }

    default void recordGeocoderError(int code) {
    }

    default void forceStopStatistics() {
    }

    default void startPowerStatistics() {
    }

    default void stopPowerStatistics() {
    }

    default void resetPowerStatistics() {
    }

    default String collectPowerStatistics() {
        return "";
    }
}
