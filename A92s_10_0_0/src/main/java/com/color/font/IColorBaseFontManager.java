package com.color.font;

import android.common.IOppoCommonFeature;
import android.content.res.Configuration;
import android.graphics.ColorTypefaceInjector;
import android.graphics.Typeface;
import android.widget.TextView;

public interface IColorBaseFontManager extends IOppoCommonFeature {
    default void createFontLink(String pkgName) {
    }

    default void deleteFontLink(String pkgName) {
    }

    default void handleFactoryReset() {
    }

    default Typeface flipTypeface(Typeface typeface) {
        return typeface;
    }

    default String getSystemFontConfig() {
        return ColorTypefaceInjector.SECOND_FONT_CONFIG_FILE;
    }

    default boolean isFlipFontUsed() {
        return false;
    }

    default void setCurrentAppName(String pkgName) {
    }

    default void setFlipFont(Configuration config, int changes) {
    }

    default void setFlipFontWhenUserChange(Configuration config, int changes) {
    }

    default void replaceFakeBoldToColorMedium(TextView textView, Typeface typeface, int style) {
    }

    default void updateTypefaceInCurrProcess(Configuration config) {
    }

    default void onCleanupUserForFont(int userId) {
    }
}
