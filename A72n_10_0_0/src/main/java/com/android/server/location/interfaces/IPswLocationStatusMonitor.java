package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.Bundle;

public interface IPswLocationStatusMonitor extends IOppoCommonFeature {
    public static final IPswLocationStatusMonitor DEFAULT = new IPswLocationStatusMonitor() {
        /* class com.android.server.location.interfaces.IPswLocationStatusMonitor.AnonymousClass1 */
    };
    public static final int GPS_BG_PERMISSION_NOT_GRANT = 2;
    public static final int GPS_OTHER_JAVA_ISSUE = 32;
    public static final int GPS_PERMISSION_NOT_GRANT = 1;
    public static final int GPS_REPORT_BLOCKED = 8;
    public static final int GPS_REPORT_INTERVAL_CHANGED = 16;
    public static final int GPS_SWITCH_NOT_ENABLE = 4;
    public static final int GPS_TTFF_ERROR = 64;
    public static final int MAP_NAVIGATION_ERROR = 128;
    public static final String Name = "IPswLocationStatusMonitor";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswLocationStatusMonitor;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context) {
    }

    default void startRecordMonitor() {
    }

    default void stopRecordMonitor() {
    }

    default void recordPermissionNotGranted(int allowedResolutionLevel, int requiredResolutionLevel, String extraString) {
    }

    default void recordBgPermissionDisable(String packageName) {
    }

    default void recordLocationDisable(String provider, String packageName) {
    }

    default void recordLocationBlocked(String packageName) {
    }

    default void recordLocationIntervalChanged(String packageName) {
    }

    default void setGpsBackgroundFlag(String packageName, boolean flag) {
    }

    default void updateForeground(String packageName, String providerName, boolean isForeground) {
    }

    default void startRequesting(String packageName, String providerName) {
    }

    default void stopRequesting(String packageName, String providerName) {
    }

    default void sendExtraCommand(String providerName, String command, Bundle extras) {
    }

    default void checkLocationHasChanged(String provider, String packageName, int hashCode) {
    }

    default int generateStatusChangedExtra(String provider, String packageName, Bundle extras, int status) {
        return 0;
    }
}
