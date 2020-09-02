package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface IOppoBatteryServiceFeature extends IOppoCommonFeature {
    public static final IOppoBatteryServiceFeature DEFAULT = new IOppoBatteryServiceFeature() {
        /* class com.android.server.IOppoBatteryServiceFeature.AnonymousClass1 */
    };
    public static final String NAME = "IOppoBatteryServiceFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoBatteryServiceFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void onStart() {
        Slog.d(NAME, "default onStart");
    }

    default void onHwStatusChanedLocked() {
        Slog.d(NAME, "default onHwStatusChanedLocked");
    }

    default int updateHealthInfoBatteryStatus(int plugType, int lastPlugType) {
        Slog.d(NAME, "default updateHealthInfoBatteryStatus");
        return -1;
    }

    default void onPluggedChanedLocked(int plugType, int lastPlugType) {
        Slog.d(NAME, "default onPluggedChanedLocked");
    }

    default void onValuesChangeLocked(int batteryTemperature, int plugType, int batteryLevel) {
        Slog.d(NAME, "default onValuesChangeLocked");
    }

    default int getChargerVoltage() {
        Slog.d(NAME, "default getChargerVoltage");
        return 0;
    }

    default boolean getChargeFastCharger() {
        Slog.d(NAME, "default getChargeFastCharger");
        return false;
    }

    default boolean dumpAddition(FileDescriptor fd, PrintWriter pw, String[] args) {
        Slog.d(NAME, "default dumpAddition");
        return false;
    }

    default boolean shutdownIfOverTempLocked() {
        Slog.d(NAME, "default oppoShutdownIfOverTempLocked");
        return false;
    }

    default void printArgs(String[] args) {
        Slog.d(NAME, "default printArgs");
    }

    default void printOppoBatteryFeature(PrintWriter pw) {
        Slog.d(NAME, "default printOppoBatteryFeature");
    }

    default void updateOppoBatteryInfo() {
        Slog.d(NAME, "default updateOppoBatteryInfo");
    }
}
