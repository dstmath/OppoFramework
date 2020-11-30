package com.android.server.display;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.android.server.display.color.DisplayTransformManager;

public interface IPswFeatureReduceBrightness extends IOppoCommonFeature {
    public static final IPswFeatureReduceBrightness DEFAULT = new IPswFeatureReduceBrightness() {
        /* class com.android.server.display.IPswFeatureReduceBrightness.AnonymousClass1 */
    };
    public static final String NAME = "IPswFeatureReduceBrightness";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswFeatureReduceBrightness;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IPswDisplayPowerControllerReduceBrightnessInner inner) {
    }

    default void init() {
    }

    default int getReduceBrightnessMode() {
        return 0;
    }

    default float getReduceBrightnessRate() {
        return 1.0f;
    }

    default int getuseReduceBrightness() {
        return 0;
    }

    default int getReduceBrightnessLowLimit() {
        return DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION;
    }

    default int getReduceBrightnessHghLimit() {
        return OppoBrightUtils.TEN_BITS_MAXBRIGHTNESS;
    }

    default boolean isReduceBrightnessAnimating() {
        return false;
    }

    default void reducebrightness(int brightness, int rate) {
    }

    default void reducebrightness2(int brightness, int rate) {
    }

    default int getbrightness() {
        return 0;
    }

    default int getrate() {
        return 0;
    }

    default void registerByNewImpl() {
    }

    default void unregisterByNewImpl() {
    }

    default boolean getmRegistAppSwitch() {
        return false;
    }

    default int getTempLimit() {
        return 0;
    }

    default int getbrightnessbackup() {
        return 0;
    }

    default void caculateFrameRate(int pid) {
    }
}
