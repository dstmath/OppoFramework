package com.oppo.theme;

import java.util.ArrayList;

public class OppoThemeUtil {
    public static final String ACCESS_CHANGE_SETTING = "access_color_setting";
    public static final String DATA_THEME_FOLDER = "/data/theme";
    public static final String DATA_THEME_PATH = "/data/theme/";
    public static final float DEFAULT_DETECT_MASK_BORDER_OFFSET = 0.065f;
    public static final String SYSTEM_THEME_DEFAULT_PATH = "/system/media/theme/default/";
    public static final ArrayList<String> THEME_CHANGED_IGNORE_PKGS = new ArrayList<>();
    public static final String THEME_CHANGE_SETTING = "theme_change_setting";
    public static final String THEME_FLAG_SETTING = "theme_flag_setting";

    static {
        THEME_CHANGED_IGNORE_PKGS.add("com.coloros.bootreg");
    }
}
