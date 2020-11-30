package com.color.font;

import android.common.OppoFeatureList;

public interface IColorFontManager extends IColorBaseFontManager {
    public static final IColorFontManager DEFAULT = new IColorFontManager() {
        /* class com.color.font.IColorFontManager.AnonymousClass1 */
    };
    public static final String NAME = "ColorFontManager";

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorFontManager;
    }

    @Override // android.common.IOppoCommonFeature
    default IColorFontManager getDefault() {
        return DEFAULT;
    }
}
