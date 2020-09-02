package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.VibrationEffect;
import android.util.Log;
import java.io.PrintWriter;

public interface IOppoVibratorFeature extends IOppoCommonFeature {
    public static final IOppoVibratorFeature DEFAULT = new IOppoVibratorFeature() {
        /* class com.android.server.IOppoVibratorFeature.AnonymousClass1 */
    };
    public static final String NAME = "IOppoVibratorFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoVibratorFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void setOppoVibratorCallback(IOppoVibratorCallback callback) {
        Log.d(NAME, "default setOppoVibratorCallback");
    }

    default void logVibratorPatterns(long[] timings, int[] amplitudes, int len) {
        Log.d(NAME, "default logVibratorPatterns");
    }

    default void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        Log.d(NAME, "default dynamicallyConfigLogTag");
    }

    default boolean cancelScreenOffReceiver() {
        Log.d(NAME, "default cancelScreenOffReceiver");
        return false;
    }

    default boolean ignoreVibrateForOneShotEffect(VibrationEffect curVibEffect, VibrationEffect newEffect) {
        Log.d(NAME, "default ignoreVibrateForOneShotEffect");
        return false;
    }

    default void updateOppoVibratorStopStatus(boolean isReadyToStop) {
        Log.d(NAME, "default updateOppoVibratorStopStatus");
    }

    default boolean isReadyToStopVibrator() {
        Log.d(NAME, "default isReadyToStopVibrator");
        return false;
    }

    default VibrationEffect applyLinearMotorVibrator(int uid, String opPkg, VibrationEffect effect) {
        Log.d(NAME, "default applyLinearMotorVibrator");
        return null;
    }

    default void cancelLinearMotorVibrator() {
        Log.d(NAME, "default cancelLinearMotorVibrator");
    }

    default boolean startCustomizeVibratorLocked(VibrationEffect effect, int vibUid, int vibUsageHint) {
        Log.d(NAME, "default startCustomizeVibrator");
        return false;
    }

    default boolean isOppoNativeVibrationEffect(VibrationEffect effect) {
        Log.d(NAME, "default isOppoNativeVibrationEffect");
        return false;
    }

    default boolean isOppoNativeWaveformEffect(VibrationEffect effect) {
        Log.d(NAME, "default isOppoNativeWaveformEffect");
        return false;
    }

    default void turnOffLinearMotorVibrator() {
        Log.d(NAME, "default turnOffLinearMotorVibrator");
    }
}
