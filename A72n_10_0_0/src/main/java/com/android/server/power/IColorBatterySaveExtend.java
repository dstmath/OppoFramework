package com.android.server.power;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;

public interface IColorBatterySaveExtend extends IOppoCommonFeature {
    public static final IColorBatterySaveExtend DEFAULT = new IColorBatterySaveExtend() {
        /* class com.android.server.power.IColorBatterySaveExtend.AnonymousClass1 */
    };
    public static final String NAME = "IColorBatterySaveExtend";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorBatterySaveExtend;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init() {
    }

    default boolean isClosedSuperFirewall(PackageManager packageManager) {
        return false;
    }

    default void onBatterySaveChanged(ContentResolver contentResolver, boolean enable) {
    }

    default void setSystemStartup(boolean startup) {
    }

    default void onAdjustBrightnessChanged(ContentResolver contentResolver, boolean adjustBrightnessDisabled) {
    }

    default int getGpsMode(Context context) {
        return 0;
    }

    default int getGpsMode(Context context, String settings, String keyGpsMode) {
        return 0;
    }

    default boolean getFireWallDisabled(Context context, String settings, String keyFirewallDisabled) {
        return false;
    }

    default boolean getAdjustBrightnessDisabled(Context context, String settings, String keyAdjustBrightnessDisabled) {
        return false;
    }

    default boolean getVibrationDisabledConfig(Context context, String settings, String keyVibrationDisabledConfig) {
        return false;
    }

    default boolean getOptionalSensorsDisabled(Context context, String settings, String keyOptionalSensorsDisabled) {
        return false;
    }

    default boolean getAodDisabled(Context context, String settings, String keyAodDisabled) {
        return false;
    }

    default boolean getLaunchBoostDisabled(Context context, String settings, String keyLaunchBoostDisabled) {
        return false;
    }

    default boolean getSoundTriggerDisabled(Context context, String settings, String key) {
        return false;
    }
}
