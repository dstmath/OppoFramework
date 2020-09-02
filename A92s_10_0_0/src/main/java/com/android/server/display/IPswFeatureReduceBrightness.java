package com.android.server.display;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

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

    default boolean isReduceBrightnessAnimating() {
        return false;
    }

    default void reducebrightness(int brightness, int rate) {
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
}
