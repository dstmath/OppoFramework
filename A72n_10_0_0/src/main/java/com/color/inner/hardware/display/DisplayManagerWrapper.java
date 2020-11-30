package com.color.inner.hardware.display;

import android.hardware.display.DisplayManager;

public class DisplayManagerWrapper {
    private DisplayManagerWrapper() {
    }

    public static void setTemporaryAutoBrightnessAdjustment(DisplayManager displayManager, float adjustment) {
        displayManager.setTemporaryAutoBrightnessAdjustment(adjustment);
    }
}
