package com.android.server;

import android.os.SystemProperties;

public class ColorSystemThemeChooser {
    private static final String BRAND_REALME = "realme";
    public static boolean sIsDeviceRealme = BRAND_REALME.equalsIgnoreCase(SystemProperties.get("ro.product.brand.sub", ""));

    private ColorSystemThemeChooser() {
    }

    public static int getDefaultSystemTheme() {
        return sIsDeviceRealme ? ColorSystemThemeEx.DEFAULT_SYSTEM_THEME_REALME : ColorSystemThemeEx.DEFAULT_SYSTEM_THEME;
    }
}
