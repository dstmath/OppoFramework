package com.color.font;

import android.content.res.Configuration;
import android.graphics.ColorTypefaceInjector;
import android.graphics.Typeface;
import android.widget.TextView;

public class ColorDummyFontManager implements IColorFontManager {
    private static volatile ColorDummyFontManager sInstance = null;

    public static ColorDummyFontManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyFontManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyFontManager();
                }
            }
        }
        return sInstance;
    }

    ColorDummyFontManager() {
    }

    @Override // com.color.font.IColorBaseFontManager
    public void createFontLink(String pkgName) {
    }

    @Override // com.color.font.IColorBaseFontManager
    public void deleteFontLink(String pkgName) {
    }

    @Override // com.color.font.IColorBaseFontManager
    public void handleFactoryReset() {
    }

    @Override // com.color.font.IColorBaseFontManager
    public Typeface flipTypeface(Typeface typeface) {
        return typeface;
    }

    @Override // com.color.font.IColorBaseFontManager
    public String getSystemFontConfig() {
        return ColorTypefaceInjector.SECOND_FONT_CONFIG_FILE;
    }

    @Override // com.color.font.IColorBaseFontManager
    public boolean isFlipFontUsed() {
        return false;
    }

    @Override // com.color.font.IColorBaseFontManager
    public void setCurrentAppName(String pkgName) {
    }

    @Override // com.color.font.IColorBaseFontManager
    public void setFlipFont(Configuration config, int changes) {
    }

    @Override // com.color.font.IColorBaseFontManager
    public void setFlipFontWhenUserChange(Configuration config, int changes) {
    }

    @Override // com.color.font.IColorBaseFontManager
    public void replaceFakeBoldToColorMedium(TextView textView, Typeface typeface, int style) {
    }

    @Override // com.color.font.IColorBaseFontManager
    public void updateTypefaceInCurrProcess(Configuration config) {
    }

    @Override // com.color.font.IColorBaseFontManager
    public void onCleanupUserForFont(int userId) {
    }
}
