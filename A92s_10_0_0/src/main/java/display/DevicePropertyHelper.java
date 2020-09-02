package display;

import android.os.SystemProperties;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class DevicePropertyHelper {
    private static final String DEFAULT_MANUFACTURE = "UNKNOWN";
    private static final String DEVICE_PATH = "/proc/devinfo";
    private static final String KEY_DEVICE_MANUFACTURE = "Device manufacture:";
    private static final String KEY_DEVICE_VERSION = "Device version:";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_BOE = "ro.lcd.backlight.config_boe";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_BOE_TENBITS = "ro.lcd.backlight.boe_tenbit";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_DSJM = "ro.lcd.backlight.config_dsjm";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_JDI = "ro.lcd.backlight.config_jdi";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG = "ro.lcd.backlight.config_samsung";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG_TENBITS = "ro.lcd.backlight.samsung_tenbit";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_TIANMA = "ro.lcd.backlight.config_tianma";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_TRULY = "ro.lcd.backlight.config_truly";
    private static String LCD_DEVICE_MANUFACTURE = "unknown";
    private static String LCD_DEVICE_VERSION = "unknown";
    private static final String LCD_MANUFACTURE_BOE = "boe";
    private static final String LCD_MANUFACTURE_BOE_TENBITS = "boe1024";
    private static final String LCD_MANUFACTURE_DSJM = "dsjm";
    private static final String LCD_MANUFACTURE_JDI = "jdi";
    private static final String LCD_MANUFACTURE_SAMSUNG = "samsung";
    private static final String LCD_MANUFACTURE_SAMSUNG_TENBITS = "samsung1024";
    private static final String LCD_MANUFACTURE_TIANMA = "tianma";
    private static final String LCD_MANUFACTURE_TRULY = "truly";
    private static final int SCREEN_AUTO_BRIGHTNESS_ELEVENBITS_LENGTH = 10;
    private static final int SCREEN_AUTO_BRIGHTNESS_OFFSET = 2;
    private static final int SCREEN_DEFAULT_BRIGHTNESS_OFFSET = 1;
    private static final String TAG = "DevicePropertyHelper";
    public static int sBrightnessBitsConfig = 1;
    public static int[] sBrightnessIntConfig;

    public static void getScreenAutoBrightnessConfig() {
        String config_string_from_property;
        String lcd_manufacture = getDeviceManufacture("lcd").toLowerCase(Locale.US);
        if (lcd_manufacture.contains(LCD_MANUFACTURE_BOE)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_BOE, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_TRULY)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_TRULY, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_TIANMA)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_TIANMA, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_SAMSUNG)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_JDI)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_JDI, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_DSJM)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_DSJM, "");
        } else {
            config_string_from_property = null;
        }
        if (lcd_manufacture.contains(LCD_MANUFACTURE_BOE_TENBITS)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_BOE_TENBITS, "");
            sBrightnessBitsConfig = 2;
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_SAMSUNG_TENBITS)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG_TENBITS, "");
            sBrightnessBitsConfig = 2;
        }
        Slog.i(TAG, "config_string_from_property : " + config_string_from_property + "  lcd_manufacture = " + lcd_manufacture);
        if (config_string_from_property != null) {
            String[] brightness_string_config = config_string_from_property.trim().split(",");
            if (brightness_string_config.length > 2) {
                int config_length = -1;
                try {
                    config_length = Integer.valueOf(brightness_string_config[0]).intValue();
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "config_length NumberFormatException:" + e.getMessage());
                }
                if (sBrightnessBitsConfig != 2 && config_length >= 10) {
                    sBrightnessBitsConfig = 3;
                }
                if (config_length + 1 != brightness_string_config.length) {
                    sBrightnessIntConfig = null;
                    return;
                }
                sBrightnessIntConfig = new int[(config_length - 1)];
                int i = 0;
                while (i < config_length - 1) {
                    try {
                        sBrightnessIntConfig[i] = Integer.valueOf(brightness_string_config[i + 2]).intValue();
                        Slog.i(TAG, "mBrightness_int_config : " + sBrightnessIntConfig[i]);
                        i++;
                    } catch (NumberFormatException e2) {
                        Slog.e(TAG, "mBrightness_int_config NumberFormatException:" + e2.getMessage());
                        sBrightnessIntConfig = null;
                        return;
                    }
                }
            }
        }
    }

    private static String getDeviceManufacture(String deviceName) {
        String filePath = "/proc/devinfo/" + deviceName;
        File file = new File(filePath);
        String manufacture = DEFAULT_MANUFACTURE;
        if (!file.exists()) {
            Slog.e(TAG, "File " + filePath + " is not exist...");
            return DEFAULT_MANUFACTURE;
        } else if (!file.canRead()) {
            Slog.e(TAG, "No permission to read " + filePath);
            return DEFAULT_MANUFACTURE;
        } else {
            BufferedReader bufferedReader = null;
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = bufferedReader2.readLine();
                    if (line != null) {
                        Slog.i(TAG, line);
                        if (line.contains(KEY_DEVICE_VERSION)) {
                            String[] lineSplit = line.split(":");
                            if (lineSplit.length > 1) {
                                manufacture = lineSplit[1].trim();
                                Slog.i(TAG, "version : " + manufacture);
                                LCD_DEVICE_VERSION = manufacture;
                            }
                        }
                        if (line.contains(KEY_DEVICE_MANUFACTURE)) {
                            String[] lineSplit2 = line.split(":");
                            if (lineSplit2.length > 1) {
                                manufacture = lineSplit2[1].trim();
                                Slog.i(TAG, "manufacture : " + manufacture);
                                LCD_DEVICE_MANUFACTURE = manufacture;
                                break;
                            }
                        }
                    }
                }
                try {
                    bufferedReader2.close();
                    break;
                } catch (IOException e) {
                    Slog.i(TAG, "bufferedReader.close IOException caught");
                }
            } catch (FileNotFoundException e2) {
                Slog.i(TAG, "FileNotFoundException caught");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e3) {
                Slog.i(TAG, "IOException caught");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Throwable th) {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e4) {
                        Slog.i(TAG, "bufferedReader.close IOException caught");
                    }
                }
                throw th;
            }
            return manufacture;
        }
    }
}
