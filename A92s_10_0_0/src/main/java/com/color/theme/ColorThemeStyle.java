package com.color.theme;

import com.oppo.theme.IColorThemeStyle;

public class ColorThemeStyle implements IColorThemeStyle {
    public static final int DEFAULT_DIALOG_THEME = 201523207;
    public static final int DEFAULT_SYSTEM_THEME = 201523202;

    public int getSystemThemeStyle(int theme) {
        return DEFAULT_SYSTEM_THEME;
    }

    public int getDialogThemeStyle(int theme) {
        return DEFAULT_DIALOG_THEME;
    }
}
