package com.android.server.display;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Slog;
import display.DevicePropertyHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/* access modifiers changed from: package-private */
public class AnimatorRateDelegate {
    private static final int EIGHT_BITS_BRIGHT_MAX_RATE = 90;
    private static final int ELEVEN_BITS_BRIGHT_MAX_RATE = 500;
    private static final String PLATFORM_MTK = "oppo.hw.manufacturer.mtk";
    private static final String PLATFORM_QUALCOMM = "oppo.hw.manufacturer.qualcomm";
    private static final String SPECIAL_LIGHT_SENSOR2 = "stk3210";
    private static final String SPECIAL_LIGHT_SENSOR3 = "cm36286s";
    private static final String SPECIAL_LIGHT_SENSOR4 = "stk3220";
    private static final String SPECIAL_LIGHT_SENSOR5 = "apds9922_s";
    private static final int TEN_BITS_BRIGHT_MAX_RATE = 120;
    private int mAutoRate = 0;
    private Context mContext;
    private float mDeltaLux = 0.0f;
    private boolean mStkCmLightSensor = false;

    public AnimatorRateDelegate(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public void calculateRateForIndex(int lastAutoBrightness, int newAutoBrightness) {
        if (DevicePropertyHelper.sBrightnessBitsConfig != 1) {
            this.mAutoRate = caclurateRateForIndex(this.mDeltaLux, lastAutoBrightness, newAutoBrightness);
            if (ColorAutomaticBrightnessController.DEBUG) {
                Slog.d("ColorAutomaticBrightnessController", "caclurateRateForIndex: lastAutoBrightness=" + lastAutoBrightness + " ,newAutoBrightness=" + newAutoBrightness + " ,mDeltaLux=" + this.mDeltaLux + " ,mAutoRate=" + this.mAutoRate);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void calculateRate(float lux, float lastObservedLux) {
        if (DevicePropertyHelper.sBrightnessBitsConfig == 1) {
            this.mAutoRate = caclurateRateImpl(lux, lastObservedLux);
            if (ColorAutomaticBrightnessController.DEBUG) {
                Slog.d("ColorAutomaticBrightnessController", "caclurateRate: lux=" + lux + " ,mLastObservedLux=" + lastObservedLux + " ,mAutoRate=" + this.mAutoRate);
                return;
            }
            return;
        }
        this.mDeltaLux = lux - lastObservedLux;
    }

    /* access modifiers changed from: package-private */
    public int getAutoRate() {
        return this.mAutoRate;
    }

    private int caclurateRateImpl(float nowLux, float lastLux) {
        if (3 == DevicePropertyHelper.sBrightnessBitsConfig) {
            if (nowLux > lastLux) {
                return Math.round((((nowLux - lastLux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            int rate = Math.round((((lastLux - nowLux) * 100.0f) + 449850.0f) / 8999.0f);
            if (rate > 120) {
                return 120;
            }
            return rate;
        } else if (2 == DevicePropertyHelper.sBrightnessBitsConfig) {
            if (nowLux > lastLux) {
                return Math.round((((nowLux - lastLux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            return Math.round((((lastLux - nowLux) * 30.0f) + 134955.0f) / 8999.0f);
        } else if (nowLux > lastLux) {
            int rate2 = Math.round((((nowLux - lastLux) * 20.0f) + 49979.0f) / 4999.0f);
            if (!this.mStkCmLightSensor) {
                return rate2;
            }
            int rate3 = rate2 + rate2;
            if (rate3 > EIGHT_BITS_BRIGHT_MAX_RATE) {
                return EIGHT_BITS_BRIGHT_MAX_RATE;
            }
            return rate3;
        } else {
            int rate4 = Math.round((((lastLux - nowLux) * 14.0f) + 29980.0f) / 4999.0f);
            if (this.mStkCmLightSensor) {
                rate4 += rate4;
            }
            if (rate4 > 30) {
                return 30;
            }
            return rate4;
        }
    }

    private int caclurateRateForIndex(float lux, int originBrightness, int targetBrightness) {
        if (3 == DevicePropertyHelper.sBrightnessBitsConfig) {
            if (targetBrightness >= originBrightness) {
                int rate = Math.round(((150.0f * lux) + 809850.0f) / 9000.0f);
                if (targetBrightness <= DevicePropertyHelper.sBrightnessIntConfig[3]) {
                    rate = EIGHT_BITS_BRIGHT_MAX_RATE;
                }
                if (originBrightness <= DevicePropertyHelper.sBrightnessIntConfig[3] && targetBrightness >= DevicePropertyHelper.sBrightnessIntConfig[4]) {
                    rate = Math.round(((100.0f * lux) + 2699900.0f) / 9000.0f);
                }
                if (originBrightness >= DevicePropertyHelper.sBrightnessIntConfig[4] && targetBrightness >= DevicePropertyHelper.sBrightnessIntConfig[4]) {
                    rate = Math.round(((120.0f * lux) + 1079880.0f) / 9000.0f);
                }
                if (!this.mStkCmLightSensor) {
                    return rate;
                }
                int rate2 = rate + rate;
                if (rate2 > ELEVEN_BITS_BRIGHT_MAX_RATE) {
                    return ELEVEN_BITS_BRIGHT_MAX_RATE;
                }
                return rate2;
            }
            int rate3 = Math.round(((Math.abs(lux) * 80.0f) + 359920.0f) / 9000.0f);
            if (this.mStkCmLightSensor) {
                rate3 += rate3;
            }
            if (rate3 > 120) {
                return 120;
            }
            return rate3;
        } else if (2 != DevicePropertyHelper.sBrightnessBitsConfig) {
            return 0;
        } else {
            if (targetBrightness >= originBrightness) {
                return Math.round(((Math.abs(lux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            int rate4 = Math.round(((Math.abs(lux) * 96.0f) + 215880.0f) / 8999.0f);
            if (this.mStkCmLightSensor) {
                rate4 += rate4;
            }
            if (rate4 > 120) {
                return 120;
            }
            return rate4;
        }
    }

    public void isSpecialSensor() {
        String lightsensor_version = getDeviceVersion().toLowerCase(Locale.US);
        if (lightsensor_version.contains(SPECIAL_LIGHT_SENSOR2) || lightsensor_version.contains(SPECIAL_LIGHT_SENSOR3) || lightsensor_version.contains(SPECIAL_LIGHT_SENSOR4) || lightsensor_version.contains(SPECIAL_LIGHT_SENSOR5)) {
            this.mStkCmLightSensor = true;
        }
        Slog.i("ColorAutomaticBrightnessController", "mStkCmLightSensor = " + this.mStkCmLightSensor);
    }

    private String getDeviceVersion() {
        boolean isPlatformQualcomm = this.mContext.getPackageManager().hasSystemFeature(PLATFORM_QUALCOMM);
        boolean isPlatformMtk = this.mContext.getPackageManager().hasSystemFeature(PLATFORM_MTK);
        Slog.i("ColorAutomaticBrightnessController", "mPlatformQualcomm = " + isPlatformQualcomm + " mPlatformMtk " + isPlatformMtk);
        if (isPlatformQualcomm) {
            return getQualcommDeviceVersion();
        }
        if (isPlatformMtk) {
            return getMtkDeviceVersion();
        }
        return "UNKNOWN";
    }

    private String getMtkDeviceVersion() {
        File file = new File("/proc/devinfo/Sensor_alsps");
        String version = "UNKNOWN";
        if (!file.exists()) {
            Slog.e("ColorAutomaticBrightnessController", "File /proc/devinfo/Sensor_alsps is not exist...");
            return version;
        } else if (!file.canRead()) {
            Slog.e("ColorAutomaticBrightnessController", "No permission to read /proc/devinfo/Sensor_alsps");
            return version;
        } else {
            BufferedReader bufferedReader = null;
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = bufferedReader2.readLine();
                    if (line != null) {
                        Slog.i("ColorAutomaticBrightnessController", line);
                        if (line.contains("Device version:")) {
                            String[] lineSplit = line.split(":");
                            if (lineSplit.length > 1) {
                                version = lineSplit[1].trim();
                                Slog.i("ColorAutomaticBrightnessController", "Device version:" + version);
                            }
                        }
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                            Slog.i("ColorAutomaticBrightnessController", "bufferedReader.close IOException caught");
                        }
                    }
                }
                bufferedReader2.close();
            } catch (FileNotFoundException e2) {
                Slog.i("ColorAutomaticBrightnessController", "FileNotFoundException caught");
                if (0 != 0) {
                    bufferedReader.close();
                }
            } catch (IOException e3) {
                Slog.i("ColorAutomaticBrightnessController", "IOException caught");
                if (0 != 0) {
                    bufferedReader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e4) {
                        Slog.i("ColorAutomaticBrightnessController", "bufferedReader.close IOException caught");
                    }
                }
                throw th;
            }
            return version;
        }
    }

    private String getQualcommDeviceVersion() {
        String type = "UNKNOWN";
        SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        List<Sensor> sensors = sensorManager != null ? sensorManager.getSensorList(8) : null;
        if (sensors == null) {
            return type;
        }
        for (int i = 0; i < sensors.size(); i++) {
            Sensor sensor = sensors.get(i);
            if (sensor.getType() == 8) {
                String[] splitSensorName = sensor.getName().split(" ");
                String sensorVersion = sensor.getName();
                if (splitSensorName.length > 1) {
                    sensorVersion = splitSensorName[0];
                }
                Slog.d("ColorAutomaticBrightnessController", "sensorVersion : " + sensorVersion);
                type = sensorVersion;
                String[] sensorMode = sensor.getName().split("-");
                if (sensorMode.length > 1) {
                    type = sensorMode[0];
                }
            }
        }
        return type;
    }
}
