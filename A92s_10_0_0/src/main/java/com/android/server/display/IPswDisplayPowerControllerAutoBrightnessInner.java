package com.android.server.display;

public interface IPswDisplayPowerControllerAutoBrightnessInner {
    public static final IPswDisplayPowerControllerAutoBrightnessInner DEFAULT = new IPswDisplayPowerControllerAutoBrightnessInner() {
        /* class com.android.server.display.IPswDisplayPowerControllerAutoBrightnessInner.AnonymousClass1 */
    };

    default int getmScreenBrightnessRangeMinimum() {
        return 0;
    }

    default int getmScreenBrightnessRangeMaximum() {
        return 0;
    }

    default void setmScreenBrightnessDefault(int ScreenBrightnessDefault) {
    }

    default void setmScreenBrightnessRangeMinimum(int ScreenBrightnessRangeMinimum) {
    }

    default void setmScreenBrightnessRangeMaximum(int ScreenBrightnessRangeMaximum) {
    }

    default int getmScreenBrightnessDimConfig() {
        return 0;
    }

    default void BRIGHTNESS_BOOST_Delayed() {
    }

    default void putIntForBrightness(int target) {
    }
}
