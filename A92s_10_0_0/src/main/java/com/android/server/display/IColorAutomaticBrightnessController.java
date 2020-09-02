package com.android.server.display;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.display.BrightnessConfiguration;
import android.os.Bundle;
import android.os.Looper;
import com.android.server.display.ai.bean.ModelConfig;
import java.io.PrintWriter;

public interface IColorAutomaticBrightnessController {

    public interface Callbacks {
        void updateBrightness();
    }

    void configure(boolean z, float f, boolean z2, boolean z3);

    void dump(PrintWriter printWriter);

    int getAutoRate();

    int getAutomaticScreenBrightness();

    float getAutomaticScreenBrightnessAdjustment();

    int getBrightnessBoost();

    int getCameraMode();

    BrightnessConfiguration getDefaultConfig();

    float getNextChange(int i, float f, float f2);

    int getmAIBrightness();

    boolean getmLightSensorEnabled();

    boolean getmProximityNear();

    void init(Callbacks callbacks, Looper looper, SensorManager sensorManager, Sensor sensor, ColorBaseBrightnessMappingStrategy colorBaseBrightnessMappingStrategy, int i, int i2, float f, int i3, ModelConfig modelConfig, long j);

    boolean isDefaultConfig();

    boolean isPocketRingingState();

    void printAutoLuxInterval();

    void resetShortTermModel();

    void setAutomaticScreenBrightness(int i);

    void setBrightnessBoost(int i);

    void setCameraBacklight(boolean z);

    void setCameraMode(int i);

    void setCameraUseAdjustmentSetting(boolean z);

    void setGalleryBacklight(boolean z);

    boolean setLoggingEnabled(boolean z);

    void setLux(float f);

    void setPocketRingingState(boolean z);

    void setStateChanged(int i, Bundle bundle);
}
