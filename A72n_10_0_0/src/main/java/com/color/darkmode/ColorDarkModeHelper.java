package com.color.darkmode;

import android.view.OppoWindowManager;

public class ColorDarkModeHelper {
    public static void registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
        try {
            new OppoWindowManager().registerOnUiModeConfigurationChangeFinishListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
        try {
            new OppoWindowManager().unregisterOnUiModeConfigurationChangeFinishListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
