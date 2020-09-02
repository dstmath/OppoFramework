package com.android.server.display;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Bundle;

public interface IOppoBrightness extends IOppoCommonFeature {
    public static final int DC_MODE_BRIGHT_EDGE = 260;
    public static final IOppoBrightness DEFAULT = new IOppoBrightness() {
        /* class com.android.server.display.IOppoBrightness.AnonymousClass1 */
    };
    public static final String NAME = "IOppoBrightness";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoBrightness;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void resetBrightnessAdj() {
    }

    default int changeMinBrightness(int brightness) {
        return brightness;
    }

    default void setBrightnessNoAnimation(boolean noAnimation) {
    }

    default boolean isFingerprintOpticalSupport() {
        return false;
    }

    default boolean isScreenUnderLightSensorSupport() {
        return false;
    }

    default boolean isUseAutoBrightness() {
        return false;
    }

    default boolean isManualSetAutoBrightness() {
        return false;
    }

    default void notifySfRepaintEverything() {
    }

    default boolean enableDebug() {
        return false;
    }

    default boolean isLowPowerMode() {
        return false;
    }

    default int getReduceBrightnessMode() {
        return 0;
    }

    default float getReduceBrightnessRate() {
        return 1.0f;
    }

    default boolean isReduceBrightnessAnimating() {
        return false;
    }

    default int getMinimumScreenBrightnessSetting() {
        return 0;
    }

    default boolean getmScreenGlobalHBMSupport() {
        return false;
    }

    default int getHBM_EXTEND_MAXBRIGHTNESS() {
        return 0;
    }

    default int getDefaultScreenBrightnessSetting() {
        return 0;
    }

    default int getBRIGHTNESS_RAMP_RATE_FAST() {
        return 0;
    }

    default int getBRIGHTNESS_RAMP_RATE_SLOW() {
        return 0;
    }

    default boolean getmShouldFastRate() {
        return false;
    }

    default int getBRIGHTNESS_RAMP_RATE_SCREENON() {
        return 0;
    }

    default int getmInverseMode() {
        return 0;
    }

    default int getINVERSE_ON() {
        return 0;
    }

    default int adjustInverseModeBrightness(int brightness) {
        return brightness;
    }

    default int getsGlobalHbmSellMode() {
        return 0;
    }

    default int getTEN_BITS_MAXBRIGHTNESS() {
        return 0;
    }

    default boolean getmSetBrihgtnessSlide() {
        return false;
    }

    default String getGLOBAL_HBM_SELL_MODE() {
        return OppoBrightUtils.GLOBAL_HBM_SELL_MODE;
    }

    default boolean getsHbmAutoBrightness() {
        return false;
    }

    default void setmShouldFastRate(boolean ShouldFastRate) {
    }

    default void setmSetBrihgtnessSlide(boolean SetBrihgtnessSlide) {
    }

    default boolean isSpecialAdj(float value) {
        return false;
    }

    default void setsHbmAutoBrightness(boolean HbmAutoBrightness) {
    }

    default int getMaximumScreenBrightnessSetting() {
        return 0;
    }

    default long getPOCKET_RIGNING_STATE_TIMEOUT() {
        return 0;
    }

    default int getPOCKET_RINGING_STATE() {
        return 0;
    }

    default int getPhoneState() {
        return 0;
    }

    default IOppoBrightness getAIBrightness() {
        return DEFAULT;
    }

    default boolean setStateChanged(int msgId, Bundle extraData) {
        return false;
    }
}
