package com.android.server.display;

import android.hardware.display.BrightnessConfiguration;
import java.io.PrintWriter;

public abstract class ColorBaseBrightnessMappingStrategy {
    public abstract void clearUserDataPoints();

    public abstract void dump(PrintWriter printWriter);

    public abstract float getAutoBrightnessAdjustment();

    public abstract BrightnessConfiguration getDefaultConfig();

    public abstract boolean hasUserDataPoints();

    public abstract boolean isDefaultConfig();

    public abstract boolean setBrightnessConfiguration(BrightnessConfiguration brightnessConfiguration);

    public abstract boolean setLoggingEnabled(boolean z);
}
