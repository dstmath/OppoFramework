package com.android.server.display;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.display.BrightnessConfiguration;
import android.os.Bundle;
import android.os.Looper;
import android.util.Slog;
import com.android.server.display.IColorAutomaticBrightnessController;
import com.android.server.display.ai.bean.ModelConfig;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

public class ColorDummyAutomaticBrightnessController implements IColorAutomaticBrightnessController {
    private static final String ABC_CLASSNAME = "com.android.server.display.ColorAutomaticBrightnessController";
    private static final String TAG = "ColorDummyAutomaticBrightnessController";
    private static volatile ColorDummyAutomaticBrightnessController sInstance = null;

    public static ColorDummyAutomaticBrightnessController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ColorDummyAutomaticBrightnessController.class) {
                if (sInstance == null) {
                    sInstance = createColorAutomaticBrightnessController(context);
                }
            }
        }
        return sInstance;
    }

    private static ColorDummyAutomaticBrightnessController createColorAutomaticBrightnessController(Context context) {
        Slog.i(TAG, "createColorDummyAutomaticBrightnessController reflect");
        try {
            return (ColorDummyAutomaticBrightnessController) Class.forName(ABC_CLASSNAME).getDeclaredConstructor(Context.class).newInstance(context);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InstantiationException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return null;
        }
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void init(IColorAutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, Sensor lightSensor, ColorBaseBrightnessMappingStrategy mapper, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, ModelConfig modelConfig, long darkeningLightDebounce) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public BrightnessConfiguration getDefaultConfig() {
        return null;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public int getAutomaticScreenBrightness() {
        return 0;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setAutomaticScreenBrightness(int automaticScreenBrightness) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public float getAutomaticScreenBrightnessAdjustment() {
        return OppoBrightUtils.MIN_LUX_LIMITI;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void resetShortTermModel() {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public boolean isDefaultConfig() {
        return false;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void dump(PrintWriter pw) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public boolean setLoggingEnabled(boolean loggingEnabled) {
        return false;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public int getAutoRate() {
        return 0;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public float getNextChange(int targetY, float currY, float timeDelta) {
        return OppoBrightUtils.MIN_LUX_LIMITI;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setStateChanged(int msgId, Bundle extraData) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void printAutoLuxInterval() {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public boolean isPocketRingingState() {
        return false;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setPocketRingingState(boolean state) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public int getCameraMode() {
        return 0;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setCameraMode(int mode) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setCameraUseAdjustmentSetting(boolean enable) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setCameraBacklight(boolean enable) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setGalleryBacklight(boolean enable) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public int getBrightnessBoost() {
        return 0;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public boolean getmProximityNear() {
        return false;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public int getmAIBrightness() {
        return 0;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public boolean getmLightSensorEnabled() {
        return false;
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setLux(float lux) {
    }

    @Override // com.android.server.display.IColorAutomaticBrightnessController
    public void setBrightnessBoost(int mode) {
    }
}
