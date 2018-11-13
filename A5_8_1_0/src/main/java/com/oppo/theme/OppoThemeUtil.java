package com.oppo.theme;

import java.util.ArrayList;

public class OppoThemeUtil {
    public static final String DATA_THEME_FOLDER = "/data/theme";
    public static final String DATA_THEME_PATH = "/data/theme/";
    public static final String SYSTEM_THEME_DEFAULT_PATH = "/system/media/theme/default/";
    public static final ArrayList<String> THEME_CHANGED_IGNORE_PKGS = new ArrayList();

    static {
        THEME_CHANGED_IGNORE_PKGS.add("com.coloros.bootreg");
    }
}
