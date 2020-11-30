package com.android.server.display;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.hardware.display.DisplayManagerInternal;

public interface IPswFeatureBrightness extends IOppoCommonFeature {
    public static final IPswFeatureBrightness DEFAULT = new IPswFeatureBrightness() {
        /* class com.android.server.display.IPswFeatureBrightness.AnonymousClass1 */
    };
    public static final String NAME = "IPswFeatureBrightness";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswFeatureBrightness;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void systemReady() {
    }

    default boolean notifyBrightnessSetting(IColorAutomaticBrightnessController mAutomaticBrightnessController, int brightness, boolean mAppliedTemporaryBrightness, boolean mAppliedTemporaryAutoBrightnessAdjustment, int mCurrentScreenBrightnessSetting, boolean slowChange) {
        return false;
    }

    default int applydimmingbrightness(int brightness, int SCREEN_DIM_MINIMUM_REDUCTION) {
        return 0;
    }

    default void caculateBrightness(boolean slowChange, boolean autoBrightnessEnabled, int brightness, IColorAutomaticBrightnessController mAutomaticBrightnessController) {
    }

    default int getrate() {
        return 0;
    }

    default int getbrightness() {
        return 0;
    }

    default int putBrightnessTodatabase(int target) {
        return 0;
    }

    default int getGlobalHbmSellMode() {
        return 0;
    }

    default float caculateautoBrightnessAdjustment(float autoBrightnessAdjustment) {
        return 1.0f;
    }

    default void setQuickDarkToBrightStatus(DisplayManagerInternal.DisplayPowerRequest powerrequest) {
    }

    default void init(IPswDisplayPowerControllerAutoBrightnessInner inner) {
    }
}
